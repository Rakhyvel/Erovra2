package com.josephs_projects.erovra2.units.buildings;

import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.gui.Button;
import com.josephs_projects.apricotLibrary.gui.GUIWrapper;
import com.josephs_projects.apricotLibrary.gui.Label;
import com.josephs_projects.apricotLibrary.gui.LineBreak;
import com.josephs_projects.apricotLibrary.gui.Updatable;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.gui.Selector;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Bomber;
import com.josephs_projects.erovra2.units.air.Plane;

/**
 * Airfields should act as a place for the player to store, repair, and rearm
 * their air units
 */
public class Airfield extends Building implements Updatable {
	public UnitType order = null;
	City homeCity;
	private Label currentOrderLabel = new Label("", Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
	private GUIWrapper actions = new GUIWrapper(new Tuple(0, 0), Erovra2.GUI_LEVEL, Erovra2.colorScheme,
			Erovra2.apricot, Erovra2.world);
	private Label hangarLabel = new Label("Hangar", Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
	private Label actionLabel = new Label("Actions", Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
	private Button launchButton = new Button("Launch", 176, 30, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world,
			(Updatable) this);
	private Button repairButton = new Button("Repair & rearm", 176, 30, Erovra2.colorScheme, Erovra2.apricot,
			Erovra2.world,
			(Updatable) this);
	private Selector hangarSelector = new Selector(193, 50, new Plane[3], Erovra2.colorScheme, Erovra2.apricot,
			Erovra2.world);
	int repairTicks = 0;
	Plane inRepair = null;

	public Airfield(Tuple position, City homeCity) {
		super(position, homeCity.nation, UnitType.AIRFIELD);

		this.homeCity = homeCity;
		homeCity.buildings.add(this);
		infoLabel.text = homeCity.name + " Airfield";

		actions.addGUIObject(hangarLabel);
		actions.addGUIObject(hangarSelector);
		actions.addGUIObject(new LineBreak(Erovra2.apricot, Erovra2.world));
		actions.addGUIObject(actionLabel);
		actions.addGUIObject(launchButton);
		actions.addGUIObject(repairButton);
		info.addGUIObject(currentOrderLabel);
		currentOrderLabel.fontSize = 17;

		focusedOptions.addGUIObject(actions);
	}

	public Airfield(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.AIRFIELD, id);
	}

	@Override
	public void tick() {
		repairTicks--;
		if ((Erovra2.net == null || homeCity.nation == Erovra2.home) && repairTicks == 0) {
			repairTicks = -1;
			if (inRepair instanceof Bomber) {
				((Bomber) inRepair).bombLoaded = true;
			}
			inRepair = null;
			repairButton.label.text = "Repair & rearm";
			hangarSelector.cancelRepair();
		} else if (repairTicks > 0 && inRepair.health < 100) {
			inRepair.health += 1.0 / 60;
		}
		super.tick();
	}

	@Override
	public void render(Graphics2D g) {
		super.render(g);

		if (launchButton != null && repairButton != null && currentOrderLabel != null && hangarSelector != null) {
			launchButton.active = hangarSelector.selected() != null;
			repairButton.active = (hangarSelector.selected() != null && hangarSelector.selected().health < 99
					|| (hangarSelector.selected() instanceof Bomber
							&& !((Bomber) hangarSelector.selected()).bombLoaded))
					|| repairTicks > 0;

			if (repairTicks > 0) {
				int seconds = repairTicks / 60;
				int minutes = seconds / 60;
				currentOrderLabel.text = "Repair: " + minutes + "m " + (seconds - minutes * 60)
						+ "s";
			} else {
				currentOrderLabel.text = "";
			}
		}
	}

	@Override
	public void update(String text) {
		if (text.contains(launchButton.label.text) && hangarSelector.selected() != null
				&& hangarSelector.selected() != inRepair) {
			hangarSelector.selected().stored = false;
			hangarSelector.clearSelected();
		} else if (text.equals("Repair & rearm") && hangarSelector.selected() != null
				&& (hangarSelector.selected().health < 99 || (hangarSelector.selected() instanceof Bomber
						&& !((Bomber) hangarSelector.selected()).bombLoaded))) {
			int rearmTicks = (hangarSelector.selected() instanceof Bomber
					&& !((Bomber) hangarSelector.selected()).bombLoaded) ? 30 * 60 : 0;
			repairTicks = (int) (60 * (100 - hangarSelector.selected().health)) + rearmTicks;
			inRepair = hangarSelector.selected();
			repairButton.label.text = "Cancel repair";
			hangarSelector.repairSelected();
		} else if (text.equals("Cancel repair")) {
			repairTicks = -1;
			inRepair = null;
			repairButton.label.text = "Repair & rearm";
			hangarSelector.cancelRepair();
		}
	}

	public void startProduction(UnitType order) {
	}

	public boolean producing() {
		return order != null;
	}

	public void landAirplane(Plane plane) {
		plane.stored = true;
		hangarSelector.add(plane);
	}

}
