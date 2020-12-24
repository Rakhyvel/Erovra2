package com.josephs_projects.erovra2;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.josephs_projects.apricotLibrary.Lexicon;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.ai.AI;
import com.josephs_projects.erovra2.projectiles.Projectile;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Attacker;
import com.josephs_projects.erovra2.units.air.Fighter;
import com.josephs_projects.erovra2.units.air.Plane;
import com.josephs_projects.erovra2.units.buildings.Airfield;
import com.josephs_projects.erovra2.units.buildings.City;
import com.josephs_projects.erovra2.units.buildings.Factory;
import com.josephs_projects.erovra2.units.ground.Artillery;
import com.josephs_projects.erovra2.units.ground.Cavalry;
import com.josephs_projects.erovra2.units.ground.GroundUnit;

public class Nation implements Tickable, Serializable {
	private static final long serialVersionUID = 2991020575316353728L;

	/*
	 * Begin serializables These fields will be sent over the network, others will
	 * not.
	 */
	public volatile Map<Integer, Unit> units = new HashMap<>();
	public volatile Map<Integer, Projectile> projectiles = new HashMap<>();
	public Tuple capitalPoint;
	public City capital;
	public String name;
	// End serializables

	public Color color;

	public Nation enemyNation;

	public Set<Unit> knownUnits;
	public Set<Plane> knownPlanes;
	public double[][] visitedSpaces;
	public boolean[][] built;
	public ArrayList<City> cities;
	public Lexicon cityNames;

	public int coins;
	public int population;
	public int mobilized;
	public int bombs;

	public int[] cityCost;
	public int[] factoryCost;
	public int[] airfieldCost;

	public AI ai;

	public int unitsMade;
	public int unitsLost;

	public int infantryDivisions = 0;
	public int cavalryDivisions = 0;
	public int artilleryDivisions = 0;
	public int fighterDivisions = 0;
	public int attackerDivisions = 0;
	public int bomberDivisions = 0;

	public Nation(String name, Color color, AI ai) {
		this.name = name;
		this.color = color;
		this.ai = ai;
		init();
		this.color = color;

		int randNation = (int) (Math.random() * 9);
		switch (randNation) {
		case 0:
			cityNames = new Lexicon("/res/names/americanNames.txt", 3, 5);
			break;
		case 1:
			cityNames = new Lexicon("/res/names/chineseNames.txt", 3, 5);
			break;
		case 2:
			cityNames = new Lexicon("/res/names/englishNames.txt", 3, 5);
			break;
		case 3:
			cityNames = new Lexicon("/res/names/frenchNames.txt", 3, 5);
			break;
		case 4:
			cityNames = new Lexicon("/res/names/germanNames.txt", 3, 5);
			break;
		case 5:
			cityNames = new Lexicon("/res/names/italianNames.txt", 3, 5);
			break;
		case 6:
			cityNames = new Lexicon("/res/names/japaneseNames.txt", 3, 5);
			break;
		case 7:
			cityNames = new Lexicon("/res/names/russianNames.txt", 3, 5);
			break;
		case 8:
			cityNames = new Lexicon("/res/names/swedishNames.txt", 3, 5);
			break;
		}
	}

	public void init() {
		knownUnits = new HashSet<>();
		knownPlanes = new HashSet<>();
		visitedSpaces = new double[Erovra2.size * 2][Erovra2.size * 2];
		built = new boolean[Erovra2.size][Erovra2.size];
		coins = 40;
		population = 10;
		cities = new ArrayList<>();

		cityCost = new int[] { 5, 0, 0 };
		factoryCost = new int[] { 10, 0, 0 };
		airfieldCost = new int[] { 10, 0, 0 };
	}

	public void setCapital(Unit unit) {
		if (unit instanceof City) {
			City city = (City) unit;
			built[(int) city.position.x / 64][(int) city.position.y / 64] = true;
			city.changeToCapital();
			this.capital = city;
		}
	}

	@Override
	public void tick() {
		if (ai == null)
			return;
		ai.takeTurn(this);
	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
	}

	public boolean buyCity(Tuple position) {
		if (coins < cityCost[ResourceType.COIN])
			return false;
		Tuple cityPoint = new Tuple(((int) (position.x / 64)) * 64 + 32, ((int) (position.y / 64)) * 64 + 32);
		if (Erovra2.terrain.getHeight(cityPoint) < 0.5 || Erovra2.terrain.getHeight(cityPoint) > 1) {
			if (ai == null) {
				Erovra2.gui.messageContainer.addMessage("Cannot build city on water tile!",
						Erovra2.colorScheme.errorColor);
			}
			return false;
		}
		if (built[(int) cityPoint.x / 64][(int) cityPoint.y / 64]
				|| enemyNation.built[(int) cityPoint.x / 64][(int) cityPoint.y / 64]) {
			if (ai == null) {
				Erovra2.gui.messageContainer.addMessage("Cannot build city on top of another building!",
						new Color(248, 89, 81));
			}
			return false;
		}
		for (int i = 0; i < cities.size(); i++) {
			if (cityPoint.cabDist(cities.get(i).position) < 64 * 3) {
				if (ai == null) {
					Erovra2.gui.messageContainer.addMessage("Too close to " + cities.get(i).name + "!",
							new Color(248, 89, 81));
				}
				return false;
			}
		}
		for (int i = 0; i < enemyNation.cities.size(); i++) {
			if (cityPoint.cabDist(enemyNation.cities.get(i).position) < 64 * 3) {
				if (ai == null) {
					enemyNation.cities.get(i).engagedTicks = 1;
					Erovra2.gui.messageContainer.addMessage("Too close to " + enemyNation.cities.get(i).name + "!",
							new Color(248, 89, 81));
				}
				return false;
			}
		}

		new City(cityPoint, this);
		built[(int) cityPoint.x / 64][(int) cityPoint.y / 64] = true;
		coins -= cityCost[ResourceType.COIN];
		cityCost[ResourceType.COIN] *= 2;
		return true;
	}

	public boolean buyFactory(Tuple position) {
		if (coins < factoryCost[ResourceType.COIN])
			return false;
		Tuple cityPoint = new Tuple(((int) (position.x / 64)) * 64 + 32, ((int) (position.y / 64)) * 64 + 32);
		if (Erovra2.terrain.getHeight(cityPoint) < 0.5 || Erovra2.terrain.getHeight(cityPoint) > 1) {
			if (ai == null) {
				Erovra2.gui.messageContainer.addMessage("Cannot build factory on water tile!",
						Erovra2.colorScheme.errorColor);
			}
			return false;
		}
		if (built[(int) cityPoint.x / 64][(int) cityPoint.y / 64]
				|| enemyNation.built[(int) cityPoint.x / 64][(int) cityPoint.y / 64]) {
			if (ai == null) {
				Erovra2.gui.messageContainer.addMessage("Cannot build factory on another building!",
						Erovra2.colorScheme.errorColor);
			}
			return false;
		}
		City nearestCity = canBuildNextToCity(cityPoint);
		if (nearestCity == null) {
			if (ai == null) {
				Erovra2.gui.messageContainer.addMessage("Must build factory next to city!",
						Erovra2.colorScheme.errorColor);
			}
			return false;
		}

		new Factory(nearestCity.position.sub(cityPoint).scalar(0.25).add(cityPoint), nearestCity);
		built[(int) cityPoint.x / 64][(int) cityPoint.y / 64] = true;
		coins -= factoryCost[ResourceType.COIN];
		factoryCost[ResourceType.COIN] += 20;
		return true;
	}

	public boolean buyAirfield(Tuple position) {
		if (coins < airfieldCost[ResourceType.COIN])
			return false;
		Tuple cityPoint = new Tuple(((int) (position.x / 64)) * 64 + 32, ((int) (position.y / 64)) * 64 + 32);
		if (Erovra2.terrain.getHeight(cityPoint) < 0.5 || Erovra2.terrain.getHeight(cityPoint) > 1)
			return false;
		if (built[(int) cityPoint.x / 64][(int) cityPoint.y / 64]
				|| enemyNation.built[(int) cityPoint.x / 64][(int) cityPoint.y / 64])
			return false;
		City nearestCity = canBuildNextToCity(cityPoint);
		if (nearestCity == null)
			return false;

		new Airfield(nearestCity.position.sub(cityPoint).scalar(0.25).add(cityPoint), nearestCity);
		built[(int) cityPoint.x / 64][(int) cityPoint.y / 64] = true;
		coins -= airfieldCost[ResourceType.COIN];
		airfieldCost[ResourceType.COIN] += 15;
		return true;
	}

	public City canBuildNextToCity(Tuple position) {
		for (int i = 0; i < cities.size(); i++) {
			if (position.cabDist(cities.get(i).position) == 64) {
				return cities.get(i);
			}
		}

		return null;
	}

	public int countFightingUnits() {
		int retval = 0;
		for (Unit unit : units.values()) {
			if (unit instanceof GroundUnit)
				retval++;
			if (unit instanceof Attacker)
				retval += 10;
		}
		return retval;
	}

	public int countCavalry() {
		int retval = 0;
		for (Unit unit : units.values()) {
			if (unit instanceof Cavalry)
				retval++;
			if (unit instanceof Factory) {
				Factory fac = (Factory) unit;
				if (fac.order == UnitType.CAVALRY)
					retval++;
			}
		}
		return retval;
	}

	public int countArtillery() {
		int retval = 0;
		for (Unit unit : units.values()) {
			if (unit instanceof Artillery)
				retval++;
			if (unit instanceof Factory) {
				Factory fac = (Factory) unit;
				if (fac.order == UnitType.ARTILLERY)
					retval++;
			}
		}
		return retval;
	}

	public int countFighters() {
		int retval = 0;
		for (Unit unit : units.values()) {
			if (unit instanceof Fighter && unit.health > 20)
				retval++;
		}
		return retval;
	}

	public int countOpenLandTiles() {
		int retval = 0;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				Tuple space = new Tuple(x * 32 + 16, y * 32 + 16);
				if (visitedSpaces[x][y] < 0 && Erovra2.terrain.getHeight(space) > 0.5) {
					retval++;
					if (visitedSpaces[x][y] <= -1)
						retval++;
				}
			}
		}
		return retval;
	}

	public int getOther() {
		return knownUnits.size();
	}

	public int registerNewDivision(UnitType type) {
		switch (type) {
		case INFANTRY:
			return ++infantryDivisions;
		case CAVALRY:
			return ++cavalryDivisions;
		case ARTILLERY:
			return ++artilleryDivisions;
		case FIGHTER:
			return ++fighterDivisions;
		case ATTACKER:
			return ++attackerDivisions;
		case BOMBER:
			return ++bomberDivisions;
		default:
			return 0;
		}
	}

	public String registerNewDivisionOrdinal(UnitType type) {
		int divisionNumber = registerNewDivision(type);

		String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
		switch (divisionNumber % 100) {
		case 11:
		case 12:
		case 13:
			return divisionNumber + "th";
		default:
			return divisionNumber + sufixes[divisionNumber % 10];

		}
	}

	/**
	 * This is called by OrderButtons when they are retrieving the pointer to the
	 * array for the resource costs of buildings.
	 * 
	 * Since building costs change, and change depending on each nation, each nation
	 * should be responsible for keeping track of its own buildings costs.
	 * 
	 * It couldn't be stored in UnitType because that would be global for both
	 * naitons, which wouldn't make sense.
	 */
	public int[] getResource(UnitType type) {
		switch (type) {
		case CITY:
			return cityCost;
		case FACTORY:
			return factoryCost;
		case AIRFIELD:
			return airfieldCost;
		default:
			break;
		}
		return null;
	}
}
