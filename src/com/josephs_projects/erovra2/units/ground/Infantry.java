package com.josephs_projects.erovra2.units.ground;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.gui.Button;
import com.josephs_projects.erovra2.gui.GUIWrapper;
import com.josephs_projects.erovra2.gui.Updatable;
import com.josephs_projects.erovra2.units.UnitType;

public class Infantry extends GroundUnit implements Updatable {
	private static Point[] decoration = new Point[4];
	private static Point[] dst = new Point[4];
	private static GUIWrapper focusedOptions = new GUIWrapper(new Tuple(363, 643));

	static {
		decoration[0] = new Point(0, 0);
		decoration[1] = new Point(16, 32);
		decoration[2] = new Point(0, 32);
		decoration[3] = new Point(16, 0);
		dst[0] = new Point();
		dst[1] = new Point();
		dst[2] = new Point();
		dst[3] = new Point();

		focusedOptions.addGUIObject(new Button("Build city", new Tuple(111, 24), 212, 38, Erovra2.colorScheme));
		focusedOptions.addGUIObject(new Button("Build airfield", new Tuple(111, 67), 212, 38, Erovra2.colorScheme));
		focusedOptions.addGUIObject(new Button("Build factory", new Tuple(111, 110), 212, 38, Erovra2.colorScheme));
	}

	public Infantry(Tuple position, Nation nation) {
		super(position, nation, UnitType.INFANTRY);
	}

	public Infantry(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.INFANTRY, id);
	}

	@Override
	public void update(String text) {
		switch (text) {
		case "Build city":
			nation.buyCity(position);
			break;
		case "Build factory":
			nation.buyFactory(position);
			break;
		case "Build airfield":
			nation.buyAirfield(position);
			break;
		}
	}

	@Override
	public void render(Graphics2D g) {
		AffineTransform af = getAffineTransform(image);
		af.transform(decoration, 0, dst, 0, 4);
		super.render(g);
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;

		focusedOptions.setShown(this == focused);
		focusedOptions.position.x = Erovra2.terrain.minimap.getWidth();
		focusedOptions.position.y = Erovra2.apricot.height() - 150;

		float deathOpacity = (float) Math.min(1, Math.max(0, (60 - deathTicks) / 60.0));
		g.setColor(new Color(0, 0, 0, deathOpacity));
		g.setStroke(new BasicStroke((float) (Erovra2.zoom + 1), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(dst[0].x, dst[0].y, dst[1].x, dst[1].y);
		g.drawLine(dst[0].x, dst[0].y, dst[2].x, dst[2].y);
		g.drawLine(dst[0].x, dst[0].y, dst[3].x, dst[3].y);
		g.drawLine(dst[1].x, dst[1].y, dst[2].x, dst[2].y);
		g.drawLine(dst[1].x, dst[1].y, dst[3].x, dst[3].y);
		g.drawLine(dst[2].x, dst[2].y, dst[3].x, dst[3].y);
	}

	// AI METHODS
	public boolean housekeeping() {
		double tempDistance = Double.POSITIVE_INFINITY;
		Tuple tempTarget = null;
		for (int y = 0; y < Erovra2.size; y++) {
			for (int x = 0; x < Erovra2.size; x++) {
				if (nation.built[x][y])
					continue;

				// Only go to empty squares
				Tuple point = new Tuple(x * 64 + 32, y * 64 + 32);
				boolean exitFlag = false;
				for (int i = 0; i < nation.cities.size(); i++) {
					if (point.cabDist(nation.cities.get(i).position) < 64 * 3) {
						exitFlag = true;
						break;
					}
				}

				if (exitFlag)
					continue;

				if (Erovra2.terrain.getHeight(point) < 0.5)
					continue;

				double distance = position.dist(point) - Erovra2.terrain.getHeight(point) * 10;
				if (distance > tempDistance)
					continue;

				if (!lineOfSight(point))
					continue;

				tempTarget = point;
				tempDistance = distance;
			}
		}

		if (tempTarget != null) {
			setTarget(tempTarget);
			return true;
		}
		return false;
	}

}
