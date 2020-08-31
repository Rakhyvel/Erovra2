package com.josephs_projects.erovra2.units;

import java.awt.Color;
import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.erovra2.Erovra2;

public class DeathMarker implements Renderable {
	Tuple position;
	Color color;
	
	public DeathMarker(Tuple position, Color color) {
		this.position = position;
		this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 128);
		Erovra2.world.add(this);
	}

	@Override
	public void render(Graphics2D g) {
		g.setColor(color);
		g.fillRect((int)(position.x * 3/4.0) - 5, (int)(position.y * 3/4.0) - 5, 10, 10);
	}
	
	@Override
	public int getRenderOrder() {
		return 2;
	}

	@Override
	public void remove() {
		
	}

}
