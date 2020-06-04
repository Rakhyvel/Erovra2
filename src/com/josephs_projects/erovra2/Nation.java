package com.josephs_projects.erovra2;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.ai.AI;
import com.josephs_projects.erovra2.projectiles.Projectile;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Attacker;
import com.josephs_projects.erovra2.units.air.Fighter;
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
	public Set<Fighter> knownFighters;
	public double[][] visitedSpaces;
	public boolean[][] built;
	public ArrayList<City> cities;

	public int coins;
	public int population;

	private int cityCost;
	private int factoryCost;
	private int airfieldCost;

	public AI ai;

	public Nation(String name, Color color, AI ai) {
		this.name = name;
		this.color = color;
		this.ai = ai;
		init();
		this.color = color;
	}

	public void init() {
		knownUnits = new HashSet<>();
		knownFighters = new HashSet<>();
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
			for (int y = 0; y < Erovra2.size * 2; y++) {
				for (int x = 0; x < Erovra2.size * 2; x++) {
					visitedSpaces[x][y] = (int) (2.56 * city.position.dist(new Tuple(x * 32 + 16, y * 32 + 16))) + 500;
				}
			}
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
		if (coins < cityCost)
			return false;
		Tuple cityPoint = new Tuple(((int) (position.x / 64)) * 64 + 32, ((int) (position.y / 64)) * 64 + 32);
		if (Erovra2.terrain.getHeight(cityPoint) < 0.5 || Erovra2.terrain.getHeight(cityPoint) > 1)
			return false;
		if (built[(int) cityPoint.x / 64][(int) cityPoint.y / 64])
			return false;
		for(int i = 0; i < cities.size(); i++) {
			if(cityPoint.cabDist(cities.get(i).position) < 64 * 3) {
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
		boolean foundNearbyCity = false;
		for(int i = 0; i < cities.size(); i++) {
			if(cityPoint.cabDist(cities.get(i).position) == 64) {
				foundNearbyCity = true;
				break;
			}
		}
		
		if(!foundNearbyCity)
			return false;

		new Factory(cityPoint, this);
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
		boolean foundNearbyCity = false;
		for(int i = 0; i < cities.size(); i++) {
			if(cityPoint.cabDist(cities.get(i).position) == 64) {
				foundNearbyCity = true;
				break;
			}
		}
		
		if(!foundNearbyCity)
			return false;

		new Airfield(cityPoint, this);
		built[(int) cityPoint.x / 64][(int) cityPoint.y / 64] = true;
		coins -= airfieldCost;
		airfieldCost *= 2;
		return true;
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
			if (unit instanceof Fighter)
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
		return (int) (knownUnits.size() * 2);
	}
}
