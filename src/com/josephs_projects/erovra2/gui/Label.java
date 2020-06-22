package com.josephs_projects.erovra2.gui;

import java.awt.Font;
import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;

public class Label extends GUIObject {
	public String text = "";
	ColorScheme scheme;
	public boolean centered = false;
	private int width = 0;
	private int height = 0;
	public int fontSize = 18;
	boolean active = true;

	public Label(String text, ColorScheme scheme) {
		super(new Tuple());
		this.text = text;
		this.scheme = scheme;
		this.renderOrder = Erovra2.GUI_LEVEL;
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
		return renderOrder;
	}

	@Override
	public void render(Graphics2D g) {
		if (!shown)
			return;

		g.setFont(new Font("Arial", Font.PLAIN, fontSize));
		width = g.getFontMetrics(g.getFont()).stringWidth(text);
		height = g.getFontMetrics(g.getFont()).getHeight();
		int textWidth = centered ? width : 0;
		int textHeight = centered ? height : height;
		
		if (active) {
			g.setColor(scheme.textColor);
		} else {
			g.setColor(scheme.disabledTextColor);
		}
		g.drawString(text, (int) position.x - textWidth / 2, (int) position.y + textHeight / 2);
	}

	@Override
	public int height() {
		if (!shown)
			return 0;
		return height;
	}

	@Override
	public int width() {
		if (!shown)
			return 0;
		return width + margin;
	}

}
