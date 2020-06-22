package com.josephs_projects.erovra2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.apricotLibrary.interfaces.InputListener;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.gui.Label;
import com.josephs_projects.erovra2.units.Unit;

public class Terrain implements Tickable, Renderable, InputListener {
	public float[][] map;
	public float[][] ore;
	public int size;

	private BufferedImage image;
	public BufferedImage minimap;
	private Label minimapInfoLabel = new Label("Click on the minimap to move unit", Erovra2.colorScheme);
	public BufferedImage oremap;
	public Tuple offset;
	private Tuple target = new Tuple();
	private Tuple mouseInitial = new Tuple(512, 512);
	private Tuple oldTarget = new Tuple();
	private Point2D[] srcGridPoints;
	private Point2D[] dstGridPoints;
	public boolean holdingCtrl = false;
	public boolean movingMouse = false;

	/**
	 * Creates terrain map. Used in singleplayer and server-side.
	 * 
	 * @param size Size in pixels for the map
	 * @param seed Seed to use for the map
	 */
	public Terrain(int size, int seed) {
		this.size = size;
		map = Apricot.noiseMap.normalize(Apricot.noiseMap.generate(size, seed, 4));
		ore = Apricot.noiseMap.normalize(Apricot.noiseMap.generate(size, seed, 3));

		// TEST CODE FOR MAPS
		Tuple[] peaks = { new Tuple(Apricot.rand.nextInt(size), Apricot.rand.nextInt(size)),
				new Tuple(Apricot.rand.nextInt(size), Apricot.rand.nextInt(size)) };

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				for (int i = 0; i < peaks.length; i++) {
					map[x][y] -= (float) (1
							* Math.sqrt((x - peaks[i].x) * (x - peaks[i].x) + (y - peaks[i].y) * (y - peaks[i].y)))
							/ size;
				}
			}
		}
		map = Apricot.noiseMap.normalize(map);

		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				map[x][y] = (float) (map[x][y] * 0.5f) + 0.1f;
			}
		}

		init();
	}

	/**
	 * Creates terrain object given a map. Used in multiplayer to reconstruct
	 * terrain.
	 * 
	 * @param map 2D float array that is sent by the server to the client
	 */
	public Terrain(float[][] map, float[][] ore) {
		this.size = map.length;
		this.map = map;
		this.ore = ore;
		init();
	}

	public Terrain(String path) {
		try {
			BufferedImage image = Apricot.image.loadImage(path);
			this.size = image.getWidth();
			Erovra2.size = size / 64;
			this.map = new float[size][size];
			for (int y = 0; y < size; y++) {
				for (int x = 0; x < size; x++) {
					map[x][y] = (image.getRGB(x, y) & 255) / 255.0f;
				}
			}
			init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets up background image, minimap image, and gridlines. Terrain is then added
	 * to world.
	 */
	private void init() {
		image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		paintTerrainImage(map, ((DataBufferInt) image.getRaster().getDataBuffer()).getData(), size, size);

		minimap = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
		paintTerrainImageMap(map, ((DataBufferInt) minimap.getRaster().getDataBuffer()).getData(), 250, 250);

		oremap = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
		paintOreImageMap(map, ((DataBufferInt) oremap.getRaster().getDataBuffer()).getData(), 250, 250);

		offset = new Tuple(512, 512);
		setupGridLines();
		
		minimapInfoLabel.fontSize = 10;
		minimapInfoLabel.renderOrder = 100;
		minimapInfoLabel.centered = true;

		Erovra2.world.add(this);
	}

	/**
	 * Sets up array of points representing the start and end points for the grid
	 * lines to be affine transformed later.
	 */
	private void setupGridLines() {
		srcGridPoints = new Point2D[size / 64 + 2];
		dstGridPoints = new Point2D[size / 64 + 2];

		srcGridPoints[0] = new Point(0, 0);
		dstGridPoints[0] = new Point(0, 0);
		srcGridPoints[1] = new Point(size, size);
		dstGridPoints[1] = new Point(size, size);
		srcGridPoints[size / 64 + 1] = new Point(size, size);
		dstGridPoints[size / 64 + 1] = new Point(size, size);

		for (int i = 2; i < srcGridPoints.length - 1; i++) {
			srcGridPoints[i] = new Point((i - 1) * 64, (i - 1) * 64);
			dstGridPoints[i] = new Point(i, 0);
		}
	}

	/**
	 * Translates and scales background image, and returns the corresponding affine
	 * transform
	 * 
	 * @param image Background image to transform
	 * @return The affine transform
	 */
	private AffineTransform getAffineTransform(BufferedImage image) {
		AffineTransform align = new AffineTransform();
		align.translate((size / 2 - offset.x) * Erovra2.zoom + (Erovra2.apricot.width() - image.getWidth()) / 2,
				(size / 2 - offset.y) * Erovra2.zoom + (Erovra2.apricot.height() - image.getHeight()) / 2);

		AffineTransform scaleCenter = new AffineTransform();
		scaleCenter.translate(image.getWidth() / 2, image.getHeight() / 2);
		scaleCenter.scale(Erovra2.zoom, Erovra2.zoom);
		scaleCenter.translate(-image.getWidth() / 2, -image.getHeight() / 2);

		align.concatenate(scaleCenter);
		return align;
	}

	@Override
	public void tick() {
		// If mouse is down, change target for map
		if (Erovra2.apricot.mouse.leftDown && holdingCtrl && movingMouse && Erovra2.apricot.mouse.getTuple().x >= 0) {
			target = oldTarget.add(mouseInitial.sub(Erovra2.apricot.mouse.getTuple()).scalar(1 / Erovra2.zoom));
		}

		if (target.dist(offset) < 1)
			return;
		// Map momentum code
		Tuple velocity = target.sub(offset).normalize().scalar(offset.dist(target) * 0.1);
		offset = offset.add(velocity);
	}

	@Override
	public void render(Graphics2D g) {
		// Draw background
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, Erovra2.apricot.width(), Erovra2.apricot.height());

		AffineTransform af = getAffineTransform(image);
		// Draw background image
		g.drawImage(image, getAffineTransform(image), null);

		// Draw grid lines
		srcGridPoints[size / 64 + 1] = new Point2D.Double(Erovra2.apricot.mouse.getTuple().x,
				Erovra2.apricot.mouse.getTuple().y);
		af.transform(srcGridPoints, 0, dstGridPoints, 0, dstGridPoints.length - 1);
		try {
			af.createInverse().transform(srcGridPoints, dstGridPoints.length - 1, dstGridPoints,
					dstGridPoints.length - 1, 1);
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}
		g.setColor(new Color(0, 0, 0, 60));
		g.setStroke(new BasicStroke(2));
		for (int i = 2; i < dstGridPoints.length - 1; i++) {
			g.drawLine((int) dstGridPoints[i].getX(), (int) dstGridPoints[0].getY(), (int) dstGridPoints[i].getX(),
					(int) dstGridPoints[1].getY());
			g.drawLine((int) dstGridPoints[0].getX(), (int) dstGridPoints[i].getY(), (int) dstGridPoints[1].getX(),
					(int) dstGridPoints[i].getY());
		}
		minimapInfoLabel.position = new Tuple(minimap.getWidth() / 2, Erovra2.apricot.height() - 30);
		minimapInfoLabel.shown = Erovra2.apricot.mouse.position.x < Erovra2.terrain.minimap.getWidth()
				&& Erovra2.apricot.mouse.position.y > Erovra2.apricot.height()
				- Erovra2.terrain.minimap.getHeight() && Unit.selected != null;
	}

	@Override
	public void input(InputEvent e) {
		if (e == InputEvent.KEY_PRESSED) {
			if (Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_EQUALS)) {
				Erovra2.zoom *= 1.1;
			}
			if (Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_MINUS)) {
				Erovra2.zoom /= 1.1;
			}
			if (Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_A)) {
				target.x -= 10;
			}
			if (Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_D)) {
				target.x += 10;
			}
			if (Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_W)) {
				target.y -= 10;
			}
			if (Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_S)) {
				target.y += 10;
			}
			if (Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_CONTROL)) {
				holdingCtrl = true;
			}
		}
		if (e == InputEvent.KEY_RELEASED) {
			holdingCtrl = false;
		}
		if (e == InputEvent.MOUSE_LEFT_DOWN) {
			movingMouse = true;
			mouseInitial.copy(Erovra2.apricot.mouse.getTuple());
			oldTarget.copy(target);
		}
		if (e == InputEvent.MOUSE_LEFT_RELEASED) {
			movingMouse = false;
		}
		if (e == InputEvent.MOUSEWHEEL_MOVED) {
			if (Erovra2.apricot.mouse.mouseWheelPosition < 0) {
				Erovra2.zoom *= 1.1;
			} else if (Erovra2.apricot.mouse.mouseWheelPosition > 0) {
				Erovra2.zoom /= 1.1;
			}
		}
		if (Erovra2.zoom < 0.75 * Erovra2.apricot.height() / (double)size)
			Erovra2.zoom = 0.75 * Erovra2.apricot.height() / (double)size;
	}

	public void setOffset(Tuple position) {
		oldTarget.copy(position);
		target.copy(position);
		offset.copy(position);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.BUILDING_LEVEL;
	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
	}

	/**
	 * Used for creating the image for the background
	 * 
	 * @param terrain
	 * @param terrainImg
	 * @param width
	 * @param height
	 */
	public void paintTerrainImage(float[][] terrain, int[] terrainImg, int width, int height) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float i = terrain[x][y];
				i = i * 2 - 0.5f;
				if (i < 0.5) {
					// Water
					i *= 2;
					i = i * i * i;
					terrainImg[x + y * width] = Apricot.color.ahsv(255, 210 - 15 * i, 0.90 - 0.5 * i, 0.35 + 0.35 * i);
				} else {
					// Land
					i = (i - 0.5f) * 2;
					i = (float) Math.sqrt(i);
					terrainImg[x + y * width] = Apricot.color.ahsv(255, 60 + 60 * i, (i * 0.1) + 0.35, .7 - i * 0.3);
				}

				if (isBorder(terrain, width, height, x, y, 0.5f, 1))
					terrainImg[x + y * width] = Apricot.color.argb(255, 216, 216, 216);
			}
		}
	}

	public void paintTerrainImageMap(float[][] terrain, int[] terrainImg, int width, int height) {
		double increment = terrain.length / (double) width;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float i = terrain[(int) (x * increment)][(int) (y * increment)];
				if (i < 0.5) {
					// Water
					terrainImg[x + y * width] = Apricot.color.ahsv(255, 201, 0.13, 1);
				} else {
					// Land
					if (i < 9 / 15.0) {
						terrainImg[x + y * width] = Apricot.color.ahsv(255, 126, 0.3, 0.8);
					} else if (i < 10 / 15.0) {
						terrainImg[x + y * width] = Apricot.color.ahsv(255, 104, 0.25, 0.825);
					} else if (i < 11 / 15.0) {
						terrainImg[x + y * width] = Apricot.color.ahsv(255, 82, 0.2, 0.85);
					} else if (i < 12 / 15.0) {
						terrainImg[x + y * width] = Apricot.color.ahsv(255, 60, 0.15, 0.875);
					} else {
						terrainImg[x + y * width] = Apricot.color.ahsv(255, 38, 0.1, 0.9);
					}
				}
				if (isBorder(terrain, width, height, (int) (x * increment), (int) (y * increment), 0.5f,
						(int) increment)) {
					terrainImg[x + y * width] = Apricot.color.argb(255, 163, 203, 218);
				}
			}
		}
	}

	public void paintOreImageMap(float[][] terrain, int[] terrainImg, int width, int height) {
		double increment = terrain.length / (double) width;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (terrain[(int) (x * increment)][(int) (y * increment)] < 0.5) {
					terrainImg[x + y * width] = Apricot.color.ahsv(0, 240, 1, 0.9);
				} else {
					float i = ore[(int) (x * increment)][(int) (y * increment)];
					if (i > 0.5) {
						terrainImg[x + y * width] = Apricot.color.ahsv(64, (int) (240 + 60 * i), 1, 1);
					}
				}
			}
		}
	}

	boolean isBorder(float[][] terrain, int width, int height, int x, int y, float z, int i) {
		boolean containsWater = terrain[x][y] <= z;
		if (x > 0)
			containsWater |= terrain[x - i][y] <= z;
		if (x < width - 1)
			containsWater |= terrain[x + i][y] <= z;
		if (y > 0)
			containsWater |= terrain[x][y - i] <= z;
		if (y < height - 1)
			containsWater |= terrain[x][y + i] <= z;

		boolean containsLand = terrain[x][y] >= z;
		if (x > 0)
			containsLand |= terrain[x - i][y] >= z;
		if (x < width - 1)
			containsLand |= terrain[x + i][y] >= z;
		if (y > 0)
			containsLand |= terrain[x][y - i] >= z;
		if (y < height - 1)
			containsLand |= terrain[x][y + i] >= z;

		return containsLand && containsWater;
	}

	public double getHeight(Tuple testPoint) {
		if (testPoint.x < 0 || testPoint.x >= size) {
			return -1;
		}
		if (testPoint.y < 0 || testPoint.y >= size) {
			return -1;
		}
		return map[(int) testPoint.x][(int) testPoint.y];
	}

	public Tuple getMousePosition() {
		if (dstGridPoints[size / 64 + 1] == null)
			return new Tuple();
		return new Tuple(dstGridPoints[size / 64 + 1].getX(), dstGridPoints[size / 64 + 1].getY());
	}

}
