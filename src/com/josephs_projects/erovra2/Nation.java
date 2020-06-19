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

	public int cityCost;
	public int factoryCost;
	public int airfieldCost;

	public AI ai;

	public int unitsMade;
	public int unitsLost;

	public int infantryDivisions = 0;
	public int cavalryDivisions = 0;
	public int artilleryDivisions = 0;

	public Nation(String name, Color color, AI ai) {
		this.name = name;
		this.color = color;
		this.ai = ai;
		init();
		this.color = color;

		int randNation = 8;//(int) (Math.random() * 9);
		switch (randNation) {
		case 0:
			cityNames = new Lexicon("src/res/names/americanNames.txt", 3, 5);
			break;
		case 1:
			cityNames = new Lexicon("src/res/names/chineseNames.txt", 3, 5);
			break;
		case 2:
			cityNames = new Lexicon("src/res/names/englishNames.txt", 3, 5);
			break;
		case 3:
			cityNames = new Lexicon("src/res/names/frenchNames.txt", 3, 5);
			break;
		case 4:
			cityNames = new Lexicon("src/res/names/germanNames.txt", 3, 5);
			break;
		case 5:
			cityNames = new Lexicon("src/res/names/italianNames.txt", 3, 5);
			break;
		case 6:
			cityNames = new Lexicon("src/res/names/japaneseNames.txt", 3, 5);
			break;
		case 7:
			cityNames = new Lexicon("src/res/names/russianNames.txt", 3, 5);
			break;
		case 8:
			cityNames = new Lexicon("src/res/names/swedishNames.txt", 3, 5);
			break;
		}
	}

	public void init() {
		knownUnits = new HashSet<>();
		knownPlanes = new HashSet<>();
		visitedSpaces = new double[Erovra2.size * 2][Erovra2.size * 2];
		built = new boolean[Erovra2.size][Erovra2.size];
		new GUI(this);
		coins = 40;
		population = 6;
		cities = new ArrayList<>();

		cityCost = 10;
		factoryCost = 15;
		airfieldCost = 15;
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
		if (Erovra2.geneticTournament && capital.health <= 0) {
			Erovra2.apricot.running = false;
		}

		if (ai == null)
			return;
		ai.takeTurn(this);
	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
	}

	public boolean buyCity(Tuple position) {
		if (coins < cityCost)
			return false;
		Tuple cityPoint = new Tuple(((int) (position.x / 64)) * 64 + 32, ((int) (position.y / 64)) * 64 + 32);
		if (Erovra2.terrain.getHeight(cityPoint) < 0.5 || Erovra2.terrain.getHeight(cityPoint) > 1)
			return false;
		if (built[(int) cityPoint.x / 64][(int) cityPoint.y / 64])
			return false;
		for (int i = 0; i < cities.size(); i++) {
			if (cityPoint.cabDist(cities.get(i).position) < 64 * 3) {
				return false;
			}
		}

		new City(cityPoint, this);
		built[(int) cityPoint.x / 64][(int) cityPoint.y / 64] = true;
		coins -= cityCost;
		population += 1;
		cityCost *= 2;
		return true;
	}

	public boolean buyFactory(Tuple position) {
		if (coins < factoryCost)
			return false;
		Tuple cityPoint = new Tuple(((int) (position.x / 64)) * 64 + 32, ((int) (position.y / 64)) * 64 + 32);
		if (Erovra2.terrain.getHeight(cityPoint) < 0.5 || Erovra2.terrain.getHeight(cityPoint) > 1)
			return false;
		if (built[(int) cityPoint.x / 64][(int) cityPoint.y / 64])
			return false;
		City nearestCity = canBuildNextToCity(cityPoint);
		if (nearestCity== null)
			return false;

		new Factory(nearestCity.position.sub(cityPoint).scalar(0.25).add(cityPoint), nearestCity);
		built[(int) cityPoint.x / 64][(int) cityPoint.y / 64] = true;
		coins -= factoryCost;
		factoryCost *= 2;
		return true;
	}

	public boolean buyAirfield(Tuple position) {
		if (coins < airfieldCost)
			return false;
		Tuple cityPoint = new Tuple(((int) (position.x / 64)) * 64 + 32, ((int) (position.y / 64)) * 64 + 32);
		if (Erovra2.terrain.getHeight(cityPoint) < 0.5 || Erovra2.terrain.getHeight(cityPoint) > 1)
			return false;
		if (built[(int) cityPoint.x / 64][(int) cityPoint.y / 64])
			return false;
		City nearestCity = canBuildNextToCity(cityPoint);
		if (nearestCity == null)
			return false;

		new Airfield(nearestCity.position.sub(cityPoint).scalar(0.25).add(cityPoint), nearestCity);
		built[(int) cityPoint.x / 64][(int) cityPoint.y / 64] = true;
		coins -= airfieldCost;
		airfieldCost *= 2;
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
}