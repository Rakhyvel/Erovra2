package com.josephs_projects.erovra2.gui;

import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;

public class LineBreak extends GUIObject {

	public LineBreak() {
		super(new Tuple());
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
		return 0;
	}

	@Override
	public void render(Graphics2D arg0) {
	}

	@Override
	public int height() {
		if(!shown)
			return 0;
		return (int)(Erovra2.apricot.height() - position.y) + 1;
	}

	@Override
	public int width() {
		return 0;
	}

}
