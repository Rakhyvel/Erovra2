package com.josephs_projects.erovra2.units.buildings;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.gui.Button;
import com.josephs_projects.erovra2.gui.GUIWrapper;
import com.josephs_projects.erovra2.gui.Label;
import com.josephs_projects.erovra2.gui.LineBreak;
import com.josephs_projects.erovra2.gui.Selector;
import com.josephs_projects.erovra2.gui.Updatable;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Plane;

public class Airfield extends Building implements Updatable {
	public UnitType order = null;
	City homeCity;
	private GUIWrapper actions = new GUIWrapper(new Tuple(0, 0));
	private Label hangarLabel = new Label("Hangar", Erovra2.colorScheme);
	private Label actionLabel = new Label("Actions", Erovra2.colorScheme);
	private Button launchButton = new Button("Launch", 176, 30, Erovra2.colorScheme);
	private Selector<Plane> hangarSelector = new Selector<>(176, 30, new Plane[4], Erovra2.colorScheme);

	public Airfield(Tuple position, City homeCity) {
		super(position, homeCity.nation, UnitType.AIRFIELD);

		this.homeCity = homeCity;
		homeCity.buildings.add(this);
		infoLabel.text = homeCity.name + " Airfield";

		actions.addGUIObject(hangarLabel);
		actions.addGUIObject(hangarSelector);
		actions.addGUIObject(new LineBreak());
		actions.addGUIObject(actionLabel);
		actions.addGUIObject(launchButton);

		focusedOptions.addGUIObject(actions);
	}

	public Airfield(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.AIRFIELD, id);
	}

	@Override
	public void update(String text) {
		if (text.contains("Launch") && hangarSelector.selected() != null) {
			hangarSelector.selected().stored = false;
			hangarSelector.clearSelected();
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
