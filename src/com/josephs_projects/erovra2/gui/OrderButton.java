package com.josephs_projects.erovra2.gui;

import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.World;
import com.josephs_projects.apricotLibrary.gui.ColorScheme;
import com.josephs_projects.apricotLibrary.gui.GUIObject;
import com.josephs_projects.apricotLibrary.gui.Label;
import com.josephs_projects.apricotLibrary.gui.Updatable;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.units.UnitType;

public class OrderButton extends GUIObject {
	int width;
	int height = 50;
	ColorScheme scheme;
	UnitType type;
	Label label;
	public boolean isHovered;
	boolean isClicked;
	public boolean active = true;
	public int borderSize = 0;
	public Updatable listener;
	public static BufferedImage[] icons;
	private boolean isBuilding; // Tells whether it should look to nation or to UnitType for resource info
	private int[] buildingRes; // Pointer to nation resource

	static {
		icons = new BufferedImage[3];
		try {
			icons[0] = Apricot.image.loadImage("/res/coinText.png");
			icons[1] = Apricot.image.loadImage("/res/oreText.png");
			icons[2] = Apricot.image.loadImage("/res/bomb.png");
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public OrderButton(UnitType type, int width, ColorScheme scheme, Apricot apricot, World world,
			Updatable listener) {
		super(new Tuple(), apricot, world);
		this.width = width;
		this.scheme = scheme;
		world.add(this);
		this.listener = listener;
		label = new Label("Build " + type.getName(), scheme, apricot, world);
		label.align = Label.Alignment.LEFT;
		label.fontSize = 17;
		this.type = type;
		margin = 0;
		isBuilding = type == UnitType.CITY || type == UnitType.FACTORY || type == UnitType.AIRFIELD;
		if(isBuilding) {
			buildingRes = Erovra2.home.getResource(type);
		}
	}

	@Override
	public void render(Graphics2D g) {
		if (!shown)
			return;
		label.active = this.active;
		if (!active)
			isHovered = false;

		// Draw background
		g.setColor(scheme.backgroundColor);
		g.fillRect((int) position.x, (int) position.y, width, height);
		if (isHovered) {
			g.setColor(scheme.highlightColor);
			g.fillRect((int) position.x, (int) position.y, width, height);
			apricot.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		}

		// Draw icon
		g.setFont(new Font("Arial", Font.PLAIN, 17));
		g.setColor(scheme.borderColor);
		g.drawLine((int) position.x + 50, (int) position.y, (int) position.x + 50,
				(int) position.y + height);
		g.drawImage(type.icon, (int) position.x + 9,
				(int) position.y + 16 - type.icon.getHeight() / 2 + 9, null);

		// Draw label
		label.position.x = (int) position.x + 58;
		label.position.y = (int) position.y + 11;

		// Draw resources
		int x = 0;
		for (int i = 0; i < 3; i++) {
			int res = 0;
			if (isBuilding) {
				res = buildingRes[i];
			} else {
				res = type.resources[i];
			}
			if (res == 0)
				continue;
			// Draw resource amount
			g.drawString(String.valueOf(res), (int) position.x + 58 + x, (int) position.y + 41);
			x += g.getFontMetrics(g.getFont()).stringWidth(String.valueOf(res)) + 4;
			// Draw resource icon
			g.drawImage(icons[i], (int) position.x + 58 + x, (int) position.y + 27, null);
			x += 27;
		}

		// Draw border
		g.setStroke(new BasicStroke(2));
		g.drawRect((int) position.x, (int) position.y, width, height);
	}

	/**
	 * When the button is pressed, it will send the updatable the text that is
	 * contained within the label.
	 */
	@Override
	public void input(InputEvent e) {
		if (!shown || !active) {
			return;
		}
		if (e == InputEvent.MOUSE_MOVED) {
			int diffX = (int) (apricot.mouse.position.x - position.x);
			int diffY = (int) (apricot.mouse.position.y - position.y);
			isHovered = (diffX > 0 && diffX < width) && (diffY > 0 && diffY < height);
		}
		if (e == InputEvent.WORLD_CHANGE) {
			isHovered = false;
		}
		if (e == InputEvent.MOUSE_LEFT_DOWN) {
			isClicked = isHovered;
		}
		if (e == InputEvent.MOUSE_LEFT_RELEASED) {
			// Call caller and let them know button is clicked.
			if (isClicked) {
				listener.update(label.text);
			}
			isClicked = false;
		}

	}

	@Override
	public void remove() {
		world.remove(this);
		label.remove();
	}

	public void setRenderOrder(int order) {
		this.renderOrder = order;
		label.renderOrder = order;
	}

	@Override
	public int getRenderOrder() {
		return renderOrder;
	}

	@Override
	public void setShown(boolean shown) {
		this.shown = shown;
		label.setShown(shown);
	}

	public void updatePosition(Tuple position) {
		this.position = position;
		if (label.align == Label.Alignment.CENTER) {
			label.updatePosition(position.add(new Tuple(width / 2, height / 2 - 4)));
		} else if (label.align == Label.Alignment.LEFT) {
			label.updatePosition(position.add(new Tuple(7, height / 2 - 4)));
		} else if (label.align == Label.Alignment.RIGHT) {
			label.updatePosition(position.add(new Tuple(width - label.width(), height / 2 - 4)));
		}
	}

	public int height() {
		if (!shown)
			return 0;
		return height + 2 * margin;
	}

	public int width() {
		if (!shown)
			return 0;
		return width + 2 * margin;
	}
}
