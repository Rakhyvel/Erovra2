package com.josephs_projects.erovra2.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
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
	BufferedImage coin;
	BufferedImage ore;

	public Label(String text, ColorScheme scheme) {
		super(new Tuple());
		this.text = text;
		this.scheme = scheme;
		this.renderOrder = Erovra2.GUI_LEVEL;
		Erovra2.world.add(this);
		
		try {
			coin = Apricot.image.loadImage("/res/coinText.png");
			ore = Apricot.image.loadImage("/res/oreText.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		String tempText = text.replace("&c", "-- ").replace("&o", "-- ");
		g.setFont(new Font("Arial", Font.PLAIN, fontSize));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		width = g.getFontMetrics(g.getFont()).stringWidth(tempText);
		height = g.getFontMetrics(g.getFont()).getHeight();
		int textWidth = centered ? width : 0;
		int textHeight = centered ? height : height;

		g.setColor(new Color(30, 30, 30, 255));
		g.drawString(tempText, (int) position.x - textWidth / 2, (int) position.y + textHeight / 2 + 1);
		if (active) {
			g.setColor(scheme.textColor);
		} else {
			g.setColor(scheme.disabledTextColor);
		}
		g.drawString(tempText, (int) position.x - textWidth / 2, (int) position.y + textHeight / 2);
		
		if(text.indexOf("&c") != -1) {
			int x = g.getFontMetrics(g.getFont()).stringWidth(tempText.substring(0, text.replace("&o", "-- ").indexOf("&c")));
			g.drawImage(coin, (int) position.x - textWidth / 2 + x, (int) position.y + textHeight / 2 - 14, null);
		}
		if(text.indexOf("&o") != -1) {
			// The distance from the begining of the tempText to the left edge of the image
			int x = g.getFontMetrics(g.getFont()).stringWidth(tempText.substring(0, text.replace("&c", "-- ").indexOf("&o")));
			g.drawImage(ore, (int) position.x - textWidth / 2 + x, (int) position.y + textHeight / 2 - 14 - (fontSize-17) / 2, null);
		}
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
