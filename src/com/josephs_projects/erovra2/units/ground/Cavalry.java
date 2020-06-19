package com.josephs_projects.erovra2.units.ground;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.units.UnitType;

public class Cavalry extends GroundUnit {
	private static Point[] decoration = new Point[4];
	private static Point[] dst = new Point[4];

	static {
		decoration[0] = new Point(0, 0);
		decoration[1] = new Point(16, 32);
		decoration[2] = new Point(0, 32);
		decoration[3] = new Point(16, 0);
		dst[0] = new Point();
		dst[1] = new Point();
		dst[2] = new Point();
		dst[3] = new Point();
	}

	public Cavalry(Tuple position, Nation nation) {
		super(position, nation, UnitType.CAVALRY);

		infoLabel.text = nation.registerNewDivisionOrdinal(type) + " Cavalry Division";
	}

	public Cavalry(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.CAVALRY, id);
	}

	@Override
	public void render(Graphics2D g) {
		AffineTransform af = getAffineTransform(image);
		af.transform(decoration, 0, dst, 0, 4);
		super.render(g);
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;

		float deathOpacity = (float) Math.min(1, Math.max(0, (60 - deathTicks) / 60.0));
		g.setColor(new Color(0, 0, 0, deathOpacity));
		g.setStroke(new BasicStroke((float) (Erovra2.zoom + 1), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(dst[0].x, dst[0].y, dst[1].x, dst[1].y);

		g.drawLine(dst[0].x, dst[0].y, dst[2].x, dst[2].y);
		g.drawLine(dst[0].x, dst[0].y, dst[3].x, dst[3].y);
		g.drawLine(dst[1].x, dst[1].y, dst[2].x, dst[2].y);
		g.drawLine(dst[1].x, dst[1].y, dst[3].x, dst[3].y);
	}

}
