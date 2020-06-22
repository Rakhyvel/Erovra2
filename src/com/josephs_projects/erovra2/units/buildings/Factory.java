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
import com.josephs_projects.erovra2.units.ground.Artillery;
import com.josephs_projects.erovra2.units.ground.Cavalry;

public class Factory extends Building {
	private int workTimer = 0;
	public UnitType order = null;
	private int totalWorkTimer = 0;
	private int coinRefund = 0;
	private int oreRefund = 0;
	
	private Label currentOrderLabel = new Label("Order: None", Erovra2.colorScheme);
	private RockerSwitch autoSwitch = new RockerSwitch("Auto order ", 40, 20, Erovra2.colorScheme);
	private Label orderActionLabel = new Label("Actions", Erovra2.colorScheme);

	private GUIWrapper actions = new GUIWrapper(new Tuple(0, 0));
	private Label actionLabel = new Label("Actions", Erovra2.colorScheme);
	private Button buildCavalryButton = new Button("Order cavalry", 176, 30, Erovra2.colorScheme);
	private Button buildArtilleryButton = new Button("Order artillery", 176, 30, Erovra2.colorScheme);
	private Button buildFighterButton = new Button("Order fighter", 176, 30, Erovra2.colorScheme);
	private Button buildAttackerButton = new Button("Order attacker", 176, 30, Erovra2.colorScheme);
	private Button buildBomberButton = new Button("Order bomber", 176, 30, Erovra2.colorScheme);

	private GUIWrapper orderActions = new GUIWrapper(new Tuple(0, 0));
	private Button cancelOrderButton = new Button("Cancel order", 176, 30, Erovra2.colorScheme);
	City homeCity;

	public Factory(Tuple position, City homeCity) {
		super(position, homeCity.nation, UnitType.FACTORY);
		actions.addGUIObject(actionLabel);
		actions.addGUIObject(buildCavalryButton);
		actions.addGUIObject(buildArtilleryButton);
		actions.addGUIObject(buildFighterButton);
		actions.addGUIObject(buildAttackerButton);
		actions.addGUIObject(buildBomberButton);

		orderActions.addGUIObject(orderActionLabel);
		orderActions.addGUIObject(cancelOrderButton);

		this.homeCity = homeCity;
		infoLabel.text = homeCity.name + " Factory";
		info.addGUIObject(currentOrderLabel);
		info.addGUIObject(autoSwitch);
		info.addGUIObject(new LineBreak());

		focusedOptions.addGUIObject(actions);
		focusedOptions.addGUIObject(orderActions);

		currentOrderLabel.fontSize = 17;
	}

	public Factory(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.FACTORY, id);
	}

	@Override
	public void tick() {
		if ((Erovra2.net == null || nation == Erovra2.home) && workTimer == 0) {
			if (order == UnitType.ARTILLERY) {
				new Artillery(position, nation);
			} else if (order == UnitType.CAVALRY) {
				new Cavalry(position, nation);
			}
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
			buildCavalryButton.label.text = "Order cavalry 15c 5o";
			buildArtilleryButton.label.text = "Order artillery 15c 5o";
			
			buildCavalryButton.active = nation.coins >= 15 && homeCity.oreMined >= 5;
			buildArtilleryButton.active = nation.coins >= 15 && homeCity.oreMined >= 5;
			buildFighterButton.active = false;
			buildAttackerButton.active = false;
			buildBomberButton.active = false;
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
		if (text.contains("Order cavalry")) {
			startProduction(UnitType.CAVALRY);
		} else if (text.contains("Order artillery")) {
			startProduction(UnitType.ARTILLERY);
		} else if (text.contains("Cancel order")) {
			nation.coins += (int)(coinRefund * (double)(workTimer) / totalWorkTimer);
			homeCity.oreMined += (int)(oreRefund * (double)(workTimer) / totalWorkTimer);
			order = null;
		}
	}

	public void startProduction(UnitType order) {
		if (nation.population < nation.countFightingUnits()) {
			this.order = null;
			return;
		}
		if (nation.coins < 15) {
			this.order = null;
			return;
		}
		if (homeCity.oreMined < 5) {
			this.order = null;
			return;
		}
		this.order = order;
		workTimer = 6000;
		totalWorkTimer = 6000;
		nation.coins -= 15;
		homeCity.oreMined -= 5;
		coinRefund = 15;
		oreRefund = 5;
	}

	public boolean producing() {
		return workTimer >= 0;
	}
}
