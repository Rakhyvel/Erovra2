package com.josephs_projects.erovra2.units.buildings;

import java.awt.Graphics2D;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.gui.Button;
import com.josephs_projects.erovra2.gui.GUIWrapper;
import com.josephs_projects.erovra2.gui.Label;
import com.josephs_projects.erovra2.gui.LineBreak;
import com.josephs_projects.erovra2.gui.RockerSwitch;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Attacker;
import com.josephs_projects.erovra2.units.air.Fighter;

public class Airfield extends Building {
	private int workTimer = 0;
	public UnitType order = null;
	private Label currentOrderLabel = new Label("Order: None", Erovra2.colorScheme);
	private RockerSwitch autoSwitch = new RockerSwitch("Auto order ", 40, 20, Erovra2.colorScheme);
	private Label orderActionLabel = new Label("Actions", Erovra2.colorScheme);

	private GUIWrapper actions = new GUIWrapper(new Tuple(0, 0));
	private Label actionLabel = new Label("Actions", Erovra2.colorScheme);
	private Button buildFighterButton = new Button("Order fighter", 176, 30, Erovra2.colorScheme);
	private Button buildAttackerButton = new Button("Order attacker", 176, 30, Erovra2.colorScheme);

	private GUIWrapper orderActions = new GUIWrapper(new Tuple(0, 0));
	private Button cancelOrderButton = new Button("Cancel order", 176, 30, Erovra2.colorScheme);
	City homeCity;

	public Airfield(Tuple position, City homeCity) {
		super(position, homeCity.nation, UnitType.AIRFIELD);
		actions.addGUIObject(actionLabel);
		actions.addGUIObject(buildFighterButton);
		actions.addGUIObject(buildAttackerButton);

		orderActions.addGUIObject(orderActionLabel);
		orderActions.addGUIObject(cancelOrderButton);

		this.homeCity = homeCity;
		infoLabel.text = homeCity.name + " Airfield";
		info.addGUIObject(currentOrderLabel);
		info.addGUIObject(autoSwitch);
		info.addGUIObject(new LineBreak());

		focusedOptions.addGUIObject(actions);
		focusedOptions.addGUIObject(orderActions);

		currentOrderLabel.fontSize = 17;
	}

	public Airfield(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.AIRFIELD, id);
	}

	@Override
	public void tick() {
		if ((Erovra2.net == null || nation == Erovra2.home) && workTimer == 0 && order != null) {
			if (order == UnitType.FIGHTER) {
				new Fighter(position, nation);
			} else if (order == UnitType.ATTACKER) {
				new Attacker(position, nation);
			} // else if (order == UnitType.BOMBER) {
//				new Bomber(position, nation);
//			}
			if (autoSwitch != null && autoSwitch.value) {
				startProduction(order);
			} else {
				order = null;
			}
		}
		workTimer--;

		super.tick();
	}

	@Override
	public void render(Graphics2D g) {
		super.render(g);
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;
		if (currentOrderLabel == null)
			return;
		actions.setShown(order == null && focusedOptions.shown);
		orderActions.setShown(order != null && focusedOptions.shown);
		if (order == null) {
			currentOrderLabel.text = "Order: None";

			buildFighterButton.label.text = "Order fighter (15)";
			buildAttackerButton.label.text = "Order attacker (30)";
		} else {
			int seconds = workTimer / 60;
			int minutes = seconds / 60;
			currentOrderLabel.text = "Order: " + order.toString().substring(0, 1)
					+ order.toString().substring(1).toLowerCase() + " " + minutes + "m " + (seconds - minutes * 60)
					+ "s";
		}
		focusedOptions.updatePosition(focusedOptions.position);
	}

	@Override
	public void update(String text) {
		if (text.contains("Order fighter")) {
			startProduction(UnitType.FIGHTER);
		} else if (text.contains("Order attacker")) {
			startProduction(UnitType.ATTACKER);
		} else if (text.contains("Cancel order")) {
			order = null;
		}
	}

	public void startProduction(UnitType order) {
		if (nation.population < nation.countFightingUnits())
			return;
		if (order == UnitType.FIGHTER) {
			if (nation.coins < 15) {
				this.order = null;
				return;
			}
			workTimer = 6000;
			nation.coins -= 15;
		}
		if (order == UnitType.ATTACKER) {
			if (nation.coins < 30) {
				this.order = null;
				return;
			}
			workTimer = 12000;
			nation.coins -= 30;
		}
		this.order = order;
	}

	public boolean producing() {
		return order != null;
	}

}
