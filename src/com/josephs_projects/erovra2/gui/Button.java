package com.josephs_projects.erovra2.gui;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.units.Unit;

public class Button extends GUIObject {
	int width;
	int height;
	ColorScheme scheme;
	String text;
	boolean isHovered;
	boolean isClicked;

	public Button(String text, Tuple position, int width, int height, ColorScheme scheme) {
		super(position);
		this.width = width;
		this.height = height;
		this.scheme = scheme;
		this.text = text;
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
		Tuple offsetPosition = position.add(wrapper.position).add(new Tuple(-width / 2, -height / 2));

		g.fillRect((int) offsetPosition.x, (int) offsetPosition.y, width, height);

		g.setColor(scheme.borderColor);
		g.setStroke(new BasicStroke(2));
		g.drawRect((int) offsetPosition.x, (int) offsetPosition.y, width, height);

		g.setColor(scheme.textColor);
		g.setFont(new Font("Arial", Font.PLAIN, 24));
		int textWidth = g.getFontMetrics(g.getFont()).stringWidth(text);
		int textHeight = g.getFontMetrics(g.getFont()).getAscent();
		g.drawString(text, (int) offsetPosition.x - textWidth / 2 + width / 2,
				(int) offsetPosition.y + textHeight / 2 - 3 + height / 2);
	}

	@Override
	public void input(InputEvent e) {
		if (!shown)
			return;
		if (e == InputEvent.MOUSE_MOVED) {
			int diffX = (int) (position.x + wrapper.position.x - Erovra2.apricot.mouse.position.x + width / 2);
			int diffY = (int) (position.y + wrapper.position.y - Erovra2.apricot.mouse.position.y + height / 2);
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

	// !Remove from GUIWrapper itself!
	@Override
	public void remove() {

	}

	@Override
	public int getRenderOrder() {
		return Erovra2.GUI_LEVEL;
	}
}
