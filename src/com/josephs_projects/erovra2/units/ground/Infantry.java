package com.josephs_projects.erovra2.units.ground;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.gui.Button;
import com.josephs_projects.apricotLibrary.gui.GUIWrapper;
import com.josephs_projects.apricotLibrary.gui.Label;
import com.josephs_projects.apricotLibrary.gui.LineBreak;
import com.josephs_projects.apricotLibrary.gui.Updatable;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.units.UnitType;

public class Infantry extends GroundUnit implements Updatable {
	private static Point[] decoration = new Point[4];
	private static Point[] dst = new Point[4];

	private GUIWrapper actions = new GUIWrapper(new Tuple(0, 0), Erovra2.GUI_LEVEL, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
	private GUIWrapper actionButtons = new GUIWrapper(new Tuple(0, 0), Erovra2.GUI_LEVEL, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
	private Label actionLabel = new Label("Actions", Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
	private Button buildCityButton = new Button("Build city", 176, 30, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world, (Updatable)this);
	private Button buildFactoryButton = new Button("Build factory", 176, 30, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world, (Updatable)this);
	private Button buildAirfieldButton = new Button("Build airfield", 176, 30, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world, (Updatable)this);
	private Button testSoilButton = new Button("Test soil", 176, 30, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world, (Updatable)this);

	public boolean buildFactory = false;
	public boolean buildAirfield = false;
	public boolean buildCity = false;

	static {
		// relative to a static version of the sprite
		// will be rotated and everything if need be
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
		actions.addGUIObject(actionButtons);
		actionButtons.addGUIObject(buildCityButton);
		actionButtons.addGUIObject(buildFactoryButton);
		actionButtons.addGUIObject(buildAirfieldButton);
		actionButtons.addGUIObject(testSoilButton);

		focusedOptions.addGUIObject(new LineBreak(Erovra2.apricot, Erovra2.world));
		focusedOptions.addGUIObject(actions);
		focusedOptions.renderOrder = Erovra2.GUI_LEVEL;
		actions.renderOrder = Erovra2.GUI_LEVEL;

		infoLabel.text = nation.registerNewDivisionOrdinal(type) + " Infantry Legion";
		focusedOptions.padding = 2;
		actionButtons.padding = 0;
		actionButtons.border = 0;
		actionButtons.margin = 0;
		
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
		} else if (text.contains("soil")) {
			double ore = Erovra2.terrain.getOre(position);
			if (ore <= 0.5833) {
				Erovra2.gui.messageContainer.addMessage("Ore test: None", Color.white);
			} else if (ore <= 0.667) {
				Erovra2.gui.messageContainer.addMessage("Ore test: Poor", Color.white);
			} else if (ore <= 0.75) {
				Erovra2.gui.messageContainer.addMessage("Ore test: Fair", Color.white);
			} else if (ore <= 0.833) {
				Erovra2.gui.messageContainer.addMessage("Ore test: Good", Color.white);
			} else {
				Erovra2.gui.messageContainer.addMessage("Ore test: Excellent", Color.white);
			}
			Erovra2.terrain.testSoil(position);
		}
	}

	@Override
	public void render(Graphics2D g) {
		AffineTransform af = getAffineTransform(image);
		af.transform(decoration, 0, dst, 0, 4);
		super.render(g);
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;

		buildAirfieldButton.label.text = "Build airfield " + nation.airfieldCost + "&c";
		buildCityButton.label.text = "Build city " + nation.cityCost + "&c";
		buildFactoryButton.label.text = "Build factory " + nation.factoryCost + "&c";

		buildAirfieldButton.active = nation.coins >= nation.airfieldCost;
		buildCityButton.active = nation.coins >= nation.cityCost;
		buildFactoryButton.active = nation.coins >= nation.factoryCost;

		float deathOpacity = (float) Math.min(1, Math.max(0, (60 - deathTicks) / 60.0));
		g.setColor(new Color(0, 0, 0, deathOpacity));
		g.setStroke(new BasicStroke((float) (Erovra2.zoom + 1), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (Erovra2.zoom > 1.5) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		// Draw outline/cross
		// Background is done is GroundUnit base class
		g.drawLine(dst[0].x, dst[0].y, dst[1].x, dst[1].y);
		g.drawLine(dst[0].x, dst[0].y, dst[2].x, dst[2].y);
		g.drawLine(dst[0].x, dst[0].y, dst[3].x, dst[3].y);
		g.drawLine(dst[1].x, dst[1].y, dst[2].x, dst[2].y);
		g.drawLine(dst[1].x, dst[1].y, dst[3].x, dst[3].y);
		g.drawLine(dst[2].x, dst[2].y, dst[3].x, dst[3].y);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}
}
