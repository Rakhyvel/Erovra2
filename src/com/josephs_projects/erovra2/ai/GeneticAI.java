package com.josephs_projects.erovra2.ai;

import java.util.ConcurrentModificationException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Attacker;
import com.josephs_projects.erovra2.units.air.Fighter;
import com.josephs_projects.erovra2.units.buildings.Airfield;
import com.josephs_projects.erovra2.units.buildings.Building;
import com.josephs_projects.erovra2.units.buildings.Factory;
import com.josephs_projects.erovra2.units.ground.GroundUnit;
import com.josephs_projects.erovra2.units.ground.Infantry;

public class GeneticAI implements AI {
	public double[] genes = new double[18];
	boolean setUpVisited = false;
	public int coinsSpent = 0;

	/*
	 * visitedSpaces distance multiplier visitedSpaces offset visitedSpaces decay
	 * fighter health limit groundUnit target distance knownFighters offset
	 * fightingUnit quota cavalryArtillery ratio
	 */
	private final int VISITED_SPACES_DISTANCE_MULTIPLIER = 0;
	private final int VISITED_SPACES_OFFSET = 1;
	private final int VISITED_SPACES_DECAY = 2;
	private final int FIGHTER_HEALTH_LIMIT = 3;
	private final int FIGHTER_BUILDING_ALERT = 4;
	private final int KNOWNFIGHTERS_OFFSET = 5;
	private final int FIGHTING_UNITS_QUOTA = 6;
	private final int CAVALRYARTILLERY_RATIO = 7;
	private final int FIGHTER_CAPITAL_DISTANCE = 8;
	private final int FIGHTER_OTHER_NATION_RATIO = 9;
	private final int ATTACKER_ALERT_SCORE = 10;
	private final int ATTACKER_CAPITAL_DISTANCE = 11;
	private final int GROUND_RAND_JUMP = 12;
	private final int GROUND_CAPITAL_DISTANCE = 13;
	private final int GROUND_TARGET_SCORE = 14;
	private final int FIGHTING_UNITS_RATIO = 15;
	private final int GROUND_UNIT_RAND_SCORE = 16;
	private final int FIGHTER_REGULAR_ALERT = 4;

	public GeneticAI() {
		genes = new double[] { 2.56, 500, 1, 20, -20, 0.4, 100, 5, 400, 1, -1, 400, 64, 400, 10000, 1000000000, 64,
				-1 };
	}

	public GeneticAI(double[] genes) {
		this.genes = genes;
	}

	@Override
	public void takeTurn(Nation nation) {
		if (!setUpVisited) {
			for (int y = 0; y < Erovra2.size * 2; y++) {
				for (int x = 0; x < Erovra2.size * 2; x++) {
					nation.visitedSpaces[x][y] = (int) (genes[VISITED_SPACES_DISTANCE_MULTIPLIER]
							* nation.capital.position.dist(new Tuple(x * 32 + 16, y * 32 + 16)))
							+ genes[VISITED_SPACES_OFFSET];
				}
			}
			setUpVisited = true;
		}
		boolean infantryBitchBoyTold = false;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				if (nation.visitedSpaces[x][y] > 0) {
					nation.visitedSpaces[x][y] -= genes[VISITED_SPACES_DECAY];
				} else if (nation.visitedSpaces[x][y] < -1) {
					nation.visitedSpaces[x][y] = 0;
				}
			}
		}

		try {
			// Tell infantry to build city
			for (Unit unit : nation.units.values()) {
				if (unit instanceof Infantry && !infantryBitchBoyTold) {
					Infantry inf = (Infantry) unit;
					if (!inf.buildCity) {
						inf.buildCity = true;
						inf.buildFactory = true;
						infantryBitchBoyTold = true;
						break;
					}
				}
			}
			
			// Move units
			for (Unit unit : nation.units.values()) {
				if (unit instanceof Fighter) {
					Fighter fighter = (Fighter) unit;
					if (fighter.health < genes[FIGHTER_HEALTH_LIMIT]) {
						fighter.patrolPoint.copy(nation.capital.position);
						continue;
					}
					if (fighter.engaged)
						continue;
					// Escort
					if (fighter.escort()) {
						continue;
					}
					if (fighterRecon(fighter, nation)) {
						continue;
					}
					// Recon

				} else if (unit instanceof Attacker) {
					Attacker attacker = (Attacker) unit;
					flyToAlertedTiles(attacker, nation);
				} else if (unit instanceof GroundUnit) {
					// Find a unit whose near their target
					if (unit.position.dist(unit.getTarget()) > 2)
						continue;
					if (unit.engaged)
						continue;
					GroundUnit ground = (GroundUnit) unit;

					if (unit instanceof Infantry && !((Infantry) unit).buildCity && !((Infantry) unit).buildFactory) {

					} else if (groundTarget(ground, nation)) {
						;
					} else if (ground.getTarget().dist(ground.position) < 1) {
						randomTarget(ground, nation);
					}
				}
			}

			if (nation.countFighters() <= nation.knownPlanes.size() * Math.abs(genes[KNOWNFIGHTERS_OFFSET])) {
				for (Unit unit : nation.units.values()) {
					// Tell infantry to build airfield
					if (unit instanceof Infantry && !infantryBitchBoyTold) {
						Infantry inf = (Infantry) unit;
						if (inf.buildAirfield) {
							infantryBitchBoyTold = true;
						} else {
							inf.buildAirfield = true;
						}
					}
					// Tell airfield to produce fighters
					if (unit instanceof Airfield) {
						Airfield air = (Airfield) unit;

						if (air.producing())
							continue;

						air.startProduction(UnitType.FIGHTER);
						coinsSpent += 15;
					}
				}
			}

			// Need ground, keep a 25:1 land ground unit ratio
			if (genes[FIGHTING_UNITS_QUOTA] > nation.countFightingUnits()
					|| nation.countFightingUnits() < nation.knownUnits.size()) {
				for (Unit unit : nation.units.values()) {

					// Tell factory to produce
					if (unit instanceof Factory) {
						Factory fac = (Factory) unit;

						if (fac.producing())
							continue;

						// Keep a good 5:1 ratio of cav:arti
						if (nation.countCavalry() < nation.countArtillery() * genes[CAVALRYARTILLERY_RATIO]) {
							fac.startProduction(UnitType.CAVALRY);
							coinsSpent += 15;
						} else {
							fac.startProduction(UnitType.ARTILLERY);
							coinsSpent += 15;
						}
					}

					// Tell airfield to produce fighters
					if (unit instanceof Airfield) {
						Airfield air = (Airfield) unit;

						if (air.producing())
							continue;

						air.startProduction(UnitType.ATTACKER);
						coinsSpent += 30;
					}
				}
				if (nation.capital != null)
					nation.capital.setProducing(true);
			} else {
				nation.capital.setProducing(false);
			}
		} catch (ConcurrentModificationException e) {
			System.out.println("Con Mod");
		}
	}

	private boolean fighterRecon(Fighter fighter, Nation nation) {
		// Scout out close ground units
		for (Unit unit : nation.enemyNation.units.values()) {
			if (!(unit instanceof Building || unit instanceof GroundUnit))
				continue;
			if (unit.position.dist(fighter.position) > 320)
				continue;

			unit.setEngagedTicksReceiver();
			fighter.setEngagedTicks();
			if (unit instanceof Building) {
				nation.visitedSpaces[(int) unit.position.x / 32][(int) unit.position.y
						/ 32] = genes[FIGHTER_BUILDING_ALERT];
			} else {
				nation.visitedSpaces[(int) unit.position.x / 32][(int) unit.position.y
						/ 32] = genes[FIGHTER_REGULAR_ALERT];
			}
		}

		// Search for closest unvisited tile
		Tuple closestTile = null;
		double tempDist = Double.POSITIVE_INFINITY;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				Tuple point = new Tuple(x * 32 + 16, y * 32 + 16);
				// Tile must be unvisited, nor necesarily a battle(?)
				if (nation.visitedSpaces[x][y] > 0 || nation.visitedSpaces[x][y] <= -1)
					continue;

				double distance = fighter.patrolPoint.dist(point);

				if (nation.countFightingUnits() < genes[FIGHTER_OTHER_NATION_RATIO] * nation.getOther()
						&& point.dist(nation.capital.position) > genes[FIGHTER_CAPITAL_DISTANCE])
					continue;

				if (distance > tempDist)
					continue;

				tempDist = distance;
				closestTile = point;
			}
		}
		// Set PatrolPoint to closest friendly, return if changed
		if (closestTile != null && !fighter.patrolPoint.equals(closestTile)) {
			fighter.patrolPoint.copy(closestTile);
			fighter.focalPoint.copy(closestTile);
			return true;
		}

		// Target not set, return false
		return false;
	}

	private void flyToAlertedTiles(Attacker attacker, Nation nation) {
		// Search for closest unvisited tile
		Tuple closestTile = null;
		double tempDist = Double.POSITIVE_INFINITY;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				Tuple point = new Tuple(x * 32 + 16, y * 32 + 16);
				// Tile must be alerted
				if (nation.visitedSpaces[x][y] > -1)
					continue;

				double score = attacker.position.dist(point);

				if (nation.countFightingUnits() < nation.getOther()
						&& point.dist(nation.capital.position) > genes[ATTACKER_CAPITAL_DISTANCE])
					continue;

				if (score < tempDist) {
					tempDist = score;
					closestTile = point;
				}
			}
		}

		// Set target to closest tile if there is
		if (closestTile != null) {
			attacker.patrolPoint.copy(closestTile);
		}
	}

	/**
	 * Chooses a random target, if that target is on land, sets units target to that
	 * point. No side effects if not.
	 */
	public void randomTarget(GroundUnit ground, Nation nation) {
		double randX = Apricot.rand.nextDouble() - 0.5;
		double randY = Apricot.rand.nextDouble() - 0.5;
		Tuple newTarget = ground.position.add(new Tuple(randX, randY).normalize().scalar(genes[GROUND_RAND_JUMP]));
		if (nation.countFightingUnits() < nation.getOther()
				&& newTarget.dist(nation.capital.position) > genes[GROUND_CAPITAL_DISTANCE]) {
			ground.setTarget(nation.capital.position);
			return;
		}
		if (Erovra2.terrain.getHeight(newTarget) > 0.5 && Erovra2.terrain.getHeight(newTarget) < 1)
			ground.setTarget(newTarget);
	}

	public boolean groundTarget(GroundUnit ground, Nation nation) {
		// Search for closest unvisited tile
		Tuple closestTile = null;
		double tempDist = Double.POSITIVE_INFINITY;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				Tuple point = new Tuple(x * 32 + 16, y * 32 + 16);
				// Tile must be unvisited
				if (nation.visitedSpaces[x][y] > 0)
					continue;

				double score = ground.position.dist(point) + genes[GROUND_TARGET_SCORE] * (nation.visitedSpaces[x][y])
						+ Math.random() * genes[GROUND_UNIT_RAND_SCORE];

				// Must be land
				if (Erovra2.terrain.getHeight(point) <= 0.5)
					continue;

//				if (nation.countFightingUnits() < genes[FIGHTING_UNITS_RATIO] * nation.getOther()
//						&& point.dist(nation.capital.position) > genes[GROUND_CAPITAL_DISTANCE])
//					continue;

				// Must have direct line of sight to tile center
				if (score < tempDist && ground.lineOfSight(point)) {
					tempDist = score;
					closestTile = point;
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

	public String toString() {
		String retval = "{";
		for (int i = 0; i < genes.length; i++) {
			retval += genes[i] + ", ";
		}
		retval += "}";
		return retval;
	}
}
