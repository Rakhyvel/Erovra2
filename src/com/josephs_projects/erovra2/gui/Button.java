package com.josephs_projects.erovra2.gui;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.units.Unit;

public class Button extends GUIObject {
	int width;
	int height;
	ColorScheme scheme;
	public String text;
	public Label label;
	boolean isHovered;
	boolean isClicked;

	public Button(String text, int width, int height, ColorScheme scheme) {
		super(new Tuple());
		this.width = width;
		this.height = height;
		this.scheme = scheme;
		this.text = text;
		Erovra2.world.add(this);
		label = new Label(text, scheme);
		label.centered = true;
		label.fontSize = 17;
	}

	@Override
	public void render(Graphics2D g) {
		if (!shown)
			return;

		if (isHovered) {
			g.setColor(scheme.highlightColor);
		} else {
			g.setColor(scheme.backgroundColor);
		}
		
		g.fillRect((int) position.x, (int) position.y, width, height);

		g.setColor(scheme.borderColor);
		g.setStroke(new BasicStroke(2));
		g.drawRect((int) position.x, (int) position.y, width, height);
	}

	@Override
	public void input(InputEvent e) {
		if (!shown)
			return;
		if (e == InputEvent.MOUSE_MOVED) {
			int diffX = (int) (Erovra2.apricot.mouse.position.x - position.x);
			int diffY = (int) (Erovra2.apricot.mouse.position.y - position.y);
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
				Unit.focused.update(text);
			}
			isClicked = false;
		}

	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.GUI_LEVEL;
	}
	
	@Override
	public void setShown(boolean shown) {
		this.shown = shown;
		label.setShown(shown);
	}
	
	public void updatePosition(Tuple position) {
		this.position = position;
		label.updatePosition(position.add(new Tuple(width / 2, height / 2 - 4)));
	}

	public int height() {
		if(!shown)
			return 0;
		return height + 2 * margin;
	}

	public int width() {
		if(!shown)
			return 0;
		return width + 2 * margin;
	}
}
