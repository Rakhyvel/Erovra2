package com.josephs_projects.erovra2.units.ground;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.projectiles.Shell;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.buildings.Building;

public class Artillery extends GroundUnit {
	private static Point[] decoration = new Point[5];
	private static Point[] dst = new Point[5];

	static {
		decoration[0] = new Point(0, 0);
		decoration[1] = new Point(16, 32);
		decoration[2] = new Point(0, 32);
		decoration[3] = new Point(16, 0);
		decoration[4] = new Point(8, 16);
		dst[0] = new Point();
		dst[1] = new Point();
		dst[2] = new Point();
		dst[3] = new Point();
		dst[4] = new Point();
	}

	public Artillery(Tuple position, Nation nation) {
		super(position, nation, UnitType.ARTILLERY);

		infoLabel.text = nation.registerNewDivisionOrdinal(type) + " Artillery Division";
	}

	public Artillery(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.ARTILLERY, id);
	}
	
	@Override
	public void render(Graphics2D g) {
		AffineTransform af = getAffineTransform(image);
		af.transform(decoration, 0, dst, 0, 5);
		super.render(g);
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;
		
		float deathOpacity = (float) Math.min(1, Math.max(0, (60 - deathTicks) / 60.0));
		g.setColor(new Color(0, 0, 0, deathOpacity));
		g.setStroke(new BasicStroke((float) (Erovra2.zoom + 1), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(dst[0].x, dst[0].y, dst[2].x, dst[2].y);
		g.drawLine(dst[0].x, dst[0].y, dst[3].x, dst[3].y);
		g.drawLine(dst[1].x, dst[1].y, dst[2].x, dst[2].y);
		g.drawLine(dst[1].x, dst[1].y, dst[3].x, dst[3].y);
		
		int width = (int)(6 * Erovra2.zoom);
		g.fillOval(dst[4].x - width / 2, dst[4].y - width / 2, width, width);
	}

	@Override
	public void attack() {
		// Find closest enemy unit
		Unit closest = null;
		double closestDistance = Double.POSITIVE_INFINITY;
		boolean onlyBuildings = true;
		List<Unit> units = new ArrayList<Unit>(nation.enemyNation.units.values());
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if(!(unit instanceof Building || unit instanceof GroundUnit))
				continue;
			double distance = unit.position.dist(position);
			if (unit.dead)
				continue;
			if (distance > 88)
				continue;
			// Prefer GroundUnits over Buildings
			if (distance < closestDistance) {
				if (unit instanceof Building) {
					if (!onlyBuildings)
						continue;
				} else {
					onlyBuildings = false;
				}
				closest = unit;
				closestDistance = distance;
				int x = (int) closest.position.x / 32;
				int y = (int) closest.position.y / 32;
				if (unit instanceof Building) {
					nation.visitedSpaces[x][y] = 0;
				} else {
					nation.visitedSpaces[x][y] = -1;
				}
			}
		}
		// Set engaged to false if none found
		if (closest == null) {
			setEngaged(false);
			return;
		}
		// Shoot enemy units if found
		setEngaged(true);
		setEngagedTicks();
		if (Erovra2.apricot.ticks % 200 == 0) {
			int x = (int) closest.position.x / 32;
			int y = (int) closest.position.y / 32;
			nation.visitedSpaces[x][y] = -1;
			setTarget(closest.position);
			new Shell(new Tuple(position), new Tuple(closest.position), nation, type.attack);
		}
		int x = (int) closest.position.x / 32;
		int y = (int) closest.position.y / 32;
		if (!(closest instanceof Building)) {
			if (x > 0 && position.dist(closest.position.add(new Tuple(-32, 0))) > 48)
				nation.visitedSpaces[x - 1][y] = -20;

			if (y > 0 && position.dist(closest.position.add(new Tuple(0, -32))) > 48)
				nation.visitedSpaces[x][y - 1] = -20;

			if (x < (Erovra2.size * 2) - 1 && position.dist(closest.position.add(new Tuple(32, 0))) > 48)
				nation.visitedSpaces[x + 1][y] = -20;

			if (y < (Erovra2.size * 2) - 1 && position.dist(closest.position.add(new Tuple(0, 32))) > 48)
				nation.visitedSpaces[x][y + 1] = -20;
		}
	}

}
