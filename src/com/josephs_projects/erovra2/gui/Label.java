package com.josephs_projects.erovra2.gui;

import java.awt.Font;
import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;

public class Label extends GUIObject {
	public String text = "";
	ColorScheme scheme;
	boolean centered = false;
	private int width = 0;
	private int height = 0;
	public int fontSize = 18;
	boolean disabled = false;

	public Label(String text, ColorScheme scheme) {
		super(new Tuple());
		this.text = text;
		this.scheme = scheme;
		Erovra2.world.add(this);
	}

	@Override
	public void input(InputEvent e) {
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
	public void render(Graphics2D g) {
		if (!shown)
			return;

		if (disabled) {
			g.setColor(scheme.disabledTextColor);
		} else {
			g.setColor(scheme.textColor);
		}
		g.setFont(new Font("Arial", Font.PLAIN, fontSize));
		width = g.getFontMetrics(g.getFont()).stringWidth(text);
		height = g.getFontMetrics(g.getFont()).getHeight();
		int textWidth = centered ? width : 0;
		int textHeight = centered ? height : height;
		g.drawString(text, (int) position.x - textWidth / 2, (int) position.y + textHeight / 2);
	}

	@Override
	public int height() {
		if(!shown)
			return 0;
		return height;
	}

	@Override
	public int width() {
		if(!shown)
			return 0;
		return width + margin;
	}

}
