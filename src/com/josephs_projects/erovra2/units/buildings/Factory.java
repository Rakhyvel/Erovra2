package com.josephs_projects.erovra2.units.buildings;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
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
	private GUIWrapper actionButtons = new GUIWrapper(new Tuple(0, 0));
	private Label actionLabel = new Label("Actions", Erovra2.colorScheme);
	private Button buildCavalryButton = new Button("Cavalry 15&c 5&o", 176, 30, Erovra2.colorScheme);
	private Button buildArtilleryButton = new Button("Artillery 15&c 5&o", 176, 30, Erovra2.colorScheme);
	private Button buildFighterButton = new Button("Fighter 15&c 5&o", 176, 30, Erovra2.colorScheme);
	private Button buildAttackerButton = new Button("Attacker 15&c 5&o", 176, 30, Erovra2.colorScheme);
	private Button buildBomberButton = new Button("Bomber 15&c 5&o", 176, 30, Erovra2.colorScheme);

	private GUIWrapper orderActions = new GUIWrapper(new Tuple(0, 0));
	private Button cancelOrderButton = new Button("Cancel order", 176, 30, Erovra2.colorScheme);
	City homeCity;

	public Factory(Tuple position, City homeCity) {
		super(position, homeCity.nation, UnitType.FACTORY);
		actions.addGUIObject(actionLabel);
		actions.addGUIObject(actionButtons);
		actionButtons.addGUIObject(buildCavalryButton);
		actionButtons.addGUIObject(buildArtilleryButton);
		actionButtons.addGUIObject(buildFighterButton);
		actionButtons.addGUIObject(buildAttackerButton);
		actionButtons.addGUIObject(buildBomberButton);

		orderActions.addGUIObject(orderActionLabel);
		orderActions.addGUIObject(cancelOrderButton);

		this.homeCity = homeCity;
		homeCity.buildings.add(this);
		infoLabel.text = homeCity.name + " Factory";
		info.addGUIObject(currentOrderLabel);
		info.addGUIObject(autoSwitch);
		info.addGUIObject(new LineBreak());

		focusedOptions.addGUIObject(actions);
		focusedOptions.addGUIObject(orderActions);

		currentOrderLabel.fontSize = 17;
		actionButtons.padding = 0;
		actionButtons.margin = 0;
	}

	public Factory(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.FACTORY, id);
	}

	@Override
	public void tick() {
		if ((Erovra2.net == null || homeCity.nation == Erovra2.home) && workTimer == 0) {
			if (order == UnitType.ARTILLERY) {
				new Artillery(position, homeCity.nation);
			} else if (order == UnitType.CAVALRY) {
				new Cavalry(position, homeCity.nation);
			} else if (order == UnitType.FIGHTER) {
				new Fighter(homeCity.position, homeCity.nation);
			} else if (order == UnitType.ATTACKER) {
				new Attacker(homeCity.position, homeCity.nation);
			}
			if (order != null && nation.ai == null) {
				Erovra2.gui.messageContainer.addMessage("Order delivered at " + homeCity.name + " factory!",
						nation.color);
			}
			if (autoSwitch != null && autoSwitch.value) {
				startProduction(order);
				if(order == null)
					autoSwitch.value = false;
			} else {
				order = null;
			}
		}
		if (order != null && nation.mobilized <= nation.population - order.population)
			workTimer--;
		if (order != null && nation.mobilized > nation.population - order.population) {
			autoSwitch.value = false;
		}

		super.tick();
	}

	@Override
	public void render(Graphics2D g) {
		if (homeCity != null) {
			engagedTicks = homeCity.engagedTicks;
		} else {
			return;
		}
		super.render(g);
		if (homeCity.nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;
		if (currentOrderLabel == null)
			return;
		actions.setShown(order == null && focusedOptions.shown);
		orderActions.setShown(order != null && focusedOptions.shown);
		if (order == null) {
			currentOrderLabel.text = "Order: None";

			buildCavalryButton.active = homeCity.nation.coins >= 15 && homeCity.oreMined >= 5;
			buildArtilleryButton.active = homeCity.nation.coins >= 15 && homeCity.oreMined >= 5;
			buildFighterButton.active = homeCity.nation.coins >= 15 && homeCity.oreMined >= 5
					&& homeCity.containsAirfield();
			buildAttackerButton.active = homeCity.nation.coins >= 15 && homeCity.oreMined >= 5
					&& homeCity.containsAirfield();
			buildBomberButton.active = homeCity.nation.coins >= 15 && homeCity.oreMined >= 5
					&& homeCity.containsAirfield();
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
		if (text.contains("Cavalry")) {
			startProduction(UnitType.CAVALRY);
		} else if (text.contains("Artillery")) {
			startProduction(UnitType.ARTILLERY);
		} else if (text.contains("Fighter")) {
			startProduction(UnitType.FIGHTER);
		} else if (text.contains("Attacker")) {
			startProduction(UnitType.ATTACKER);
		} else if (text.contains("Cancel order")) {
			homeCity.nation.coins += (int) (coinRefund * (double) (workTimer) / totalWorkTimer);
			homeCity.oreMined += (int) (oreRefund * (double) (workTimer) / totalWorkTimer);
			order = null;
		}
	}

	public void startProduction(UnitType order) {
		if (homeCity.nation.coins < 15) {
			this.order = null;
			if (nation.ai == null) {
				Erovra2.gui.messageContainer.addMessage(
						homeCity.name + " factory cannot fulfill order due to lack of coins!", new Color(248, 89, 81));
			}
			return;
		}
		if (homeCity.oreMined < 5) {
			this.order = null;
			if (nation.ai == null) {
				Erovra2.gui.messageContainer.addMessage(
						homeCity.name + " factory cannot fulfill order due to lack of ore!", new Color(248, 89, 81));
			}
			return;
		}
		this.order = order;
		workTimer = 6000;
		totalWorkTimer = 6000;
		homeCity.nation.coins -= 15;
		homeCity.oreMined -= 5;
		coinRefund = 15;
		oreRefund = 5;
	}

	public boolean producing() {
		return order != null;
	}

	@Override
	public void detectHit() {
		// Factories cannot be destroyed by groundunits
	}

	@Override
	public void remove() {
		nation.units.remove(id);
		nation.enemyNation.units.put(id, this);
		nation = nation.enemyNation;
		try {
			image = Apricot.image.loadImage("/res/units/buildings/" + type.name + ".png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Apricot.image.overlayBlend(image, nation.color);
	}
}
