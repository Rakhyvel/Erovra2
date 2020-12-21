package com.josephs_projects.erovra2.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.World;
import com.josephs_projects.apricotLibrary.gui.ColorScheme;
import com.josephs_projects.apricotLibrary.gui.GUIObject;
import com.josephs_projects.apricotLibrary.gui.Label;
import com.josephs_projects.apricotLibrary.gui.ProgressBar;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.units.air.Bomber;
import com.josephs_projects.erovra2.units.air.Plane;

public class Selector extends GUIObject {
	int width;
	int height;
	ColorScheme scheme;
	private Plane[] options;
	private Label[] labels;
	private ProgressBar[] healths;
	private int selected = -1;
	private int repaired = -1;
	BufferedImage wrench;
	BufferedImage bomb;

	public Selector(int width, int height, Plane[] array, ColorScheme scheme, Apricot apricot, World world) {
		super(new Tuple(), apricot, world);
		this.width = width;
		this.height = height;
		this.scheme = scheme;
		this.options = array;
		this.labels = new Label[options.length];
		this.healths = new ProgressBar[options.length];

		for (int i = 0; i < options.length; i++) {
			// Init labels
			labels[i] = new Label("", scheme, apricot, world);
			labels[i].fontSize = 17;
			labels[i].renderOrder = renderOrder + 1;

			// Init healths
			healths[i] = new ProgressBar(126, 9, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
			healths[i].progress = 1;
			healths[i].renderOrder = renderOrder + 1;
		}
		try {
			wrench = Apricot.image.loadImage("/res/wrench.png");
			bomb = Apricot.image.loadImage("/res/bomb.png");
		} catch (IOException e) {
			System.out.println(e);
		}

		world.add(this);
	}

	@Override
	public void render(Graphics2D g) {
		if (!shown)
			return;
		// Draw rectangle around options
		g.setStroke(new BasicStroke(2));
		for (int i = 0; i < options.length; i++) {
			g.setColor(scheme.disabledTextColor);
			if (options[i] == null) {
				g.drawRect((int) position.x, (int) position.y + i * height, width, height);
			}
		}

		// Draw option text
		for (int i = 0; i < options.length; i++) {
			if (options[i] != null) {
				Plane option = options[i];
				// Give highlight if selected
				if (i == selected) {
					g.setColor(scheme.highlightColor);
					g.fillRect((int) position.x, (int) position.y + i * height, width, height);
				}
				// Draw border around option (label will draw text)
				g.setColor(scheme.borderColor);
				g.drawRect((int) position.x, (int) position.y + i * height, width, height);
				labels[i].text = option.toString();
				healths[i].progress = option.health / 100.0;

				// Draw icon and icon border
				g.drawLine((int) position.x + 50, (int) position.y + i * height, (int) position.x + 50,
						(int) position.y + i * height + height);
				g.drawImage(option.type.icon, (int) position.x + 9,
						(int) position.y + i * height + 16 - option.type.icon.getHeight() / 2 + 9, null);
				if (i == repaired) {
					g.drawImage(wrench, (int) position.x + 9,
							(int) position.y + i * height + 9, null);
				}

				// Draw bomb icon
				if (option instanceof Bomber && ((Bomber) option).bombLoaded) {
					g.drawImage(bomb, (int) position.x + 163,
							(int) position.y + i * height + 5, null);
				}
			}
			// Label shown/x/y
			labels[i].shown = options[i] != null;
			labels[i].position.x = (int) position.x + 58;
			labels[i].position.y = (int) position.y + i * height + 11;

			// Healths shown/x/y
			healths[i].shown = options[i] != null;
			healths[i].position.x = (int) position.x + 58;
			healths[i].position.y = (int) position.y + i * height + 30;
		}
	}

	@Override
	public void input(InputEvent e) {
		if (!shown)
			return;

		if (e == InputEvent.MOUSE_LEFT_RELEASED) {
			if (apricot.mouse.position.y < position.y || apricot.mouse.position.x < position.x
					|| apricot.mouse.position.x > position.x + width) {
				selected = -1;
				return;
			}
			selected = (int) (apricot.mouse.position.y - position.y) / height;
			if (selected == repaired) {
				selected = -1;
			}
		}
	}

	@Override
	public void remove() {

	}

	@Override
	public void setShown(boolean shown) {
		this.shown = shown;
		for (int i = 0; i < labels.length; i++) {
			labels[i].setShown(shown);
			healths[i].setShown(shown);
		}
	}

	@Override
	public int getRenderOrder() {
		return renderOrder;
	}

	@Override
	public void updatePosition(Tuple position) {
		this.position = position;
	}

	public int height() {
		if (!shown)
			return 0;
		return height;
	}

	public int width() {
		if (!shown)
			return 0;
		return width + 2 * margin + 10;
	}

	public boolean add(Plane option) {
		for (int i = 0; i < options.length; i++) {
			if (options[i] == null) {
				options[i] = option;
				return true;
			}
		}
		return false;
	}

	public Plane selected() {
		if (selected < 0 || selected >= options.length)
			return null;
		return options[selected];
	}

	public void clearSelected() {
		options[selected] = null;
		labels[selected].text = "";
	}

	public void repairSelected() {
		repaired = selected;
	}

	public void cancelRepair() {
		repaired = -1;
	}

}
