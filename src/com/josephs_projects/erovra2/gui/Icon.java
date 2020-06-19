package com.josephs_projects.erovra2.gui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;

public class Icon extends GUIObject {
	public BufferedImage image;

	public Icon(BufferedImage image) {
		super(new Tuple());
		this.image = image;
		Erovra2.world.add(this);
	}

	@Override
	public void input(InputEvent arg0) {
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
		if (image == null)
			return;
		g.drawImage(image, (int) position.x, (int) position.y, null);
	}

	@Override
	public int height() {
		if(image == null)
			return 0;
		return image.getHeight();
	}

	@Override
	public int width() {
		if(image == null)
			return 0;
		return image.getWidth();
	}

}
