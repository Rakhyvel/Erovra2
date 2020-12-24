package com.josephs_projects.erovra2.ai;

import java.util.ConcurrentModificationException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.ResourceType;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.buildings.Building;
import com.josephs_projects.erovra2.units.buildings.City;
import com.josephs_projects.erovra2.units.buildings.Factory;
import com.josephs_projects.erovra2.units.ground.GroundUnit;
import com.josephs_projects.erovra2.units.ground.Infantry;

public class NewAI implements AI {
	private boolean setupVisited = false;

	@Override
	public void takeTurn(Nation nation) {
		if (!setupVisited) {
			setupVisitedSpaces(nation);
			setupVisited = true;
		}
		updateVisitedSpaces(nation);
		for (Unit unit : nation.units.values()) {
			if (unit instanceof GroundUnit) {
				moveGroundUnits(unit);
			} else if (unit instanceof Building) {
				produceBuildings(unit);
			}
		}
		infantryBuild(nation);
	}

	private void setupVisitedSpaces(Nation nation) {
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				nation.visitedSpaces[x][y] = (int) (2.56
						* nation.capital.position.dist(new Tuple(x * 32 + 16, y * 32 + 16))) + 500;
			}
		}
	}

	private void updateVisitedSpaces(Nation nation) {
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				if (nation.visitedSpaces[x][y] > 0) {
					nation.visitedSpaces[x][y] -= 0.1;
				} else if (nation.visitedSpaces[x][y] > -1) {
					nation.visitedSpaces[x][y] = 0;
				}
			}
		}
	}

	private void moveGroundUnits(Unit unit) {
		if (unit.position.dist(unit.getTarget()) > 1)
			return;
		if (unit.engaged)
			return;
		GroundUnit ground = (GroundUnit) unit;
		if (!groundTarget(ground, unit.nation)) {
			randomTarget(ground, unit.nation);
		}
	}

	/**
	 * If can buy a city, then find a tile that can be a city. If cannot buy a city,
	 * or could not find a tile that can build a city, If can buy a factory, then
	 * find a tile can be a factory.
	 * 
	 * if a tile has been found, and the tile is the tile that the infantry is in,
	 * then build it, why not?
	 * 
	 * @param nation
	 */
	private void produceBuildings(Unit unit) {
		if (unit instanceof City) {
			City city = (City) unit;
			city.recruitSwitch.value = true;
		}
		if (unit instanceof Factory) {
			Factory fac = (Factory) unit;
			if (fac.producing())
				return;

			if (Apricot.rand.nextDouble() < 0.5) {
				fac.startProduction(UnitType.CAVALRY);
			} else {
				fac.startProduction(UnitType.ARTILLERY);
			}
		}
	}

	/**
	 * Tells the first infantry it finds to go to a make a city if possible, or a
	 * factory if possible. Only one infantry should be targeted at a time. If the
	 * target and position are the same, the city or factory is bought
	 * 
	 * @param nation
	 */
	private void infantryBuild(Nation nation) {
		try {
			for (Unit unit : nation.units.values()) {
				if (unit instanceof Infantry) {
					Infantry inf = (Infantry) unit;
					if (inf.engaged)
						continue;
					if (inf.stuckIn)
						continue;
					Tuple cityPoint = searchForCity(inf, nation);
					Tuple factoryPoint = searchForFactory(inf, nation);
					if (nation.coins >= nation.cityCost[ResourceType.COIN] && cityPoint != null) {
						inf.setTarget(cityPoint);
						if (inf.position.dist(inf.getTarget()) < 32) {
							nation.buyCity(inf.position);
						}
						return;
					} else if (nation.coins >= nation.factoryCost[ResourceType.COIN] && factoryPoint != null) {
						inf.setTarget(factoryPoint);
						if (inf.position.dist(inf.getTarget()) < 32) {
							nation.buyFactory(inf.position);
						}
						return;
					}
				}
			}
		} catch (ConcurrentModificationException e) {

		}
	}

	/**
	 * This method finds a random target for a ground unit
	 * 
	 * @param ground Ground unit to randomly target
	 * @param nation Nation of ground unit
	 */
	public void randomTarget(GroundUnit ground, Nation nation) {
		double randX = Apricot.rand.nextDouble() - 0.5;
		double randY = Apricot.rand.nextDouble() - 0.5;
		Tuple newTarget = ground.position.add(new Tuple(randX, randY).normalize().scalar(64));
		if (nation.countFightingUnits() < nation.getOther() && newTarget.dist(nation.capital.position) > 100) {
			ground.setTarget(nation.capital.position);
			return;
		}
		if (Erovra2.terrain.getHeight(newTarget) > 0.5 && Erovra2.terrain.getHeight(newTarget) < 1)
			ground.setTarget(newTarget);
	}

	/**
	 * This method directs a ground unit to nearest enemy unit, or if one cannot be
	 * found, the nearest unvisited tile. If neither can be found, returns false.
	 * 
	 * @param ground Ground unit to target
	 * @param nation Nation of ground unit
	 * @return Whether or not a near enemy or unvisited tile is found
	 */
	public boolean groundTarget(GroundUnit ground, Nation nation) {
		// Search for closest unvisited tile
		Tuple closestTile = null;
		double tempDist = Double.POSITIVE_INFINITY;
		boolean foundEnemy = false;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				Tuple point = new Tuple(x * 32 + 16, y * 32 + 16);
				// Tile must be unvisited
				if (nation.visitedSpaces[x][y] > 0)
					continue;
				// If there is an enemy, don't go to just blank spaces
				if (foundEnemy && nation.visitedSpaces[x][y] > -1)
					continue;

				double score = ground.position.dist(point) + 64 * Math.random();

				// Must be land
				if (Erovra2.terrain.getHeight(point) <= 0.5)
					continue;

				// Must have direct line of sight to tile center
				if ((score < tempDist
						// For the first time foundEnemy is false and enemy is found
						|| (!foundEnemy && nation.visitedSpaces[x][y] <= -1)) && ground.lineOfSight(point)) {
					tempDist = score;
					closestTile = point;
					// If enemy is found
					if (nation.visitedSpaces[x][y] <= -1) {
						foundEnemy = true;
					}
				}
			}
		}

		// Set target to closest tile if there is
		if (closestTile != null) {
			ground.setTarget(closestTile);
			return true;
		}

		// Target not set, return false
		return false;
	}

	public Tuple searchForCity(Infantry infantry, Nation nation) {
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

				if (Erovra2.terrain.getOre(point) <= 0.66)
					continue;

				double distance = infantry.position.dist(point) - Erovra2.terrain.getHeight(point) * 10;
				if (distance > tempDistance)
					continue;

				if (!infantry.lineOfSight(point))
					continue;

				tempTarget = point;
				tempDistance = distance;
			}
		}
		return tempTarget;
	}

	public Tuple searchForFactory(Infantry inf, Nation nation) {
		double tempDistance = Double.POSITIVE_INFINITY;
		Tuple tempTarget = null;
		for (int y = 0; y < Erovra2.size; y++) {
			for (int x = 0; x < Erovra2.size; x++) {
				if (nation.built[x][y])
					continue;

				// Only go to empty squares
				Tuple point = new Tuple(x * 64 + 32, y * 64 + 32);
				City homeCity = nation.canBuildNextToCity(point);
				if (homeCity == null)
					continue;

				if (Erovra2.terrain.getOre(homeCity.position) < 0.66)
					continue;

				if (Erovra2.terrain.getHeight(point) < 0.5)
					continue;

				double distance = inf.position.dist(point);
				if (distance > tempDistance)
					continue;

				if (!inf.lineOfSight(point))
					continue;

				tempTarget = point;
				tempDistance = distance;
			}
		}
		return tempTarget;
	}
}
