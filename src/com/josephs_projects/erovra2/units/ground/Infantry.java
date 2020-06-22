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
import com.josephs_projects.erovra2.gui.Label;
import com.josephs_projects.erovra2.gui.Updatable;
import com.josephs_projects.erovra2.units.UnitType;

public class Infantry extends GroundUnit implements Updatable {
	private static Point[] decoration = new Point[4];
	private static Point[] dst = new Point[4];
	
	private GUIWrapper actions = new GUIWrapper(new Tuple(0, 0));
	private Label actionLabel = new Label("Actions", Erovra2.colorScheme);
	private Button buildCityButton = new Button("Build city", 176, 30, Erovra2.colorScheme);
	private Button buildFactoryButton = new Button("Build factory", 176, 30, Erovra2.colorScheme);
	private Button buildAirfieldButton = new Button("Build airfield", 176, 30, Erovra2.colorScheme);

	public boolean buildFactory = false;
	public boolean buildAirfield = false;
	public boolean buildCity = false;

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

	public Infantry(Tuple position, Nation nation) {
		super(position, nation, UnitType.INFANTRY);

		actions.addGUIObject(actionLabel);
		actions.addGUIObject(buildCityButton);
		actions.addGUIObject(buildFactoryButton);
		actions.addGUIObject(buildAirfieldButton);
		
		focusedOptions.addGUIObject(actions);
		focusedOptions.renderOrder = Erovra2.GUI_LEVEL;
		actions.renderOrder = Erovra2.GUI_LEVEL;

		infoLabel.text = nation.registerNewDivisionOrdinal(type) + " Infantry Division";
	}

	public Infantry(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.INFANTRY, id);
	}

	@Override
	public void update(String text) {
		if (text.contains("city")) {
			nation.buyCity(position);
		} else if (text.contains("factory")) {
			nation.buyFactory(position);
		} else if (text.contains("airfield")) {
			nation.buyAirfield(position);
		}
	}

	@Override
	public void render(Graphics2D g) {
		AffineTransform af = getAffineTransform(image);
		af.transform(decoration, 0, dst, 0, 4);
		super.render(g);
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;

		buildAirfieldButton.label.text = "Build airfield (" + nation.airfieldCost + ")";
		buildCityButton.label.text = "Build city (" + nation.cityCost + ")";
		buildFactoryButton.label.text = "Build factory (" + nation.factoryCost + ")";
		
		buildAirfieldButton.active = nation.coins >= nation.airfieldCost;
		buildCityButton.active = nation.coins >= nation.cityCost;
		buildFactoryButton.active = nation.coins >= nation.factoryCost;

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
	public void tick() {
		super.tick();
		if (nation.ai != null) {
			if (buildFactory && nation.coins >= nation.factoryCost) {
				if (!nation.buyFactory(position)) {
					searchForFactory();
				} else {
					buildFactory = false;
				}
				return;
			}
			if (buildCity && nation.coins >= nation.cityCost) {
				if (!nation.buyCity(position)) {
					searchForCity();
				} else {
					buildCity = false;
				}
				return;
			}
			if (buildAirfield && nation.coins >= nation.airfieldCost) {
				if (!nation.buyAirfield(position)) {
					searchForFactory();
				} else {
					buildAirfield = false;
				}
				return;
			}
		}
	}

	public boolean searchForFactory() {
		double tempDistance = Double.POSITIVE_INFINITY;
		Tuple tempTarget = null;
		for (int y = 0; y < Erovra2.size; y++) {
			for (int x = 0; x < Erovra2.size; x++) {
				if (nation.built[x][y])
					continue;

				// Only go to empty squares
				Tuple point = new Tuple(x * 64 + 32, y * 64 + 32);
				if (nation.canBuildNextToCity(point) == null)
					continue;

				if (Erovra2.terrain.getHeight(point) < 0.5)
					continue;

				double distance = position.dist(point);
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

	public boolean searchForCity() {
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
