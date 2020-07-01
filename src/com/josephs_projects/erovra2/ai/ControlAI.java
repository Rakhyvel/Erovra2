package com.josephs_projects.erovra2.ai;

import java.util.ConcurrentModificationException;

import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Attacker;
import com.josephs_projects.erovra2.units.air.Fighter;
import com.josephs_projects.erovra2.units.buildings.Airfield;
import com.josephs_projects.erovra2.units.buildings.Factory;
import com.josephs_projects.erovra2.units.ground.GroundUnit;
import com.josephs_projects.erovra2.units.ground.Infantry;

public class ControlAI implements AI {
	@Override
	public void takeTurn(Nation nation) {
		try {
			boolean infantryBitchBoyTold = false;
			for (int y = 0; y < Erovra2.size * 2; y++) {
				for (int x = 0; x < Erovra2.size * 2; x++) {
					if (nation.visitedSpaces[x][y] >= 0) {
						nation.visitedSpaces[x][y] -= 0.1;
					} else {
					}
				}
			}

			// Move units
			for (Unit unit : nation.units.values()) {
				if (unit instanceof Fighter) {
					Fighter fighter = (Fighter) unit;
					if (fighter.health < 20) {
						fighter.patrolPoint.copy(nation.capital.position);
						continue;
					}
					if (fighter.engaged)
						continue;
					// Escort
					if (fighter.recon()) {
						continue;
					}
					if (fighter.escort()) {
						continue;
					}
					// Recon

				}
				if (unit instanceof Attacker) {
					Attacker attacker = (Attacker) unit;
					attacker.flyToAlertedTiles();
				}
				if (unit instanceof GroundUnit) {
					// Find a unit whose near their target
					if (unit.position.dist(unit.getTarget()) > 2)
						continue;
					if (unit.engaged)
						continue;
					GroundUnit ground = (GroundUnit) unit;

//			Central AI- Target unit
					if (unit instanceof Infantry && !infantryBitchBoyTold) {
						Infantry inf = (Infantry) unit;
					} else if (ground.target()) {
						;
					} else if (ground.getTarget().dist(ground.position) < 1) {
//						ground.randomTarget();
					}
				}
			}

			// Tell infantry to build city
			for (Unit unit : nation.units.values()) {
				if (!(unit instanceof Infantry))
					continue;
				Infantry inf = (Infantry) unit;

				// Need to break because of concurrent modification
				if (nation.buyCity(inf.position))
					break;
			}

//			if (nation.countFighters() < nation.knownFighters.size() + 1) {
//				for (Unit unit : nation.units.values()) {
//					// Tell infantry to build airfield
//					if (unit instanceof Infantry) {
//						Infantry inf = (Infantry) unit;
//						if (nation.buyAirfield(inf.position))
//							break;
//					}
//					// Tell airfield to produce fighters
//					if (unit instanceof Airfield) {
//						Airfield air = (Airfield) unit;
//
//						if (air.producing())
//							continue;
//
//						air.startProduction(UnitType.FIGHTER);
//					}
//				}
//			}

			// Need ground, keep a 25:1 land ground unit ratio
			if (80 > nation.countFightingUnits() || nation.countFightingUnits() < nation.knownUnits.size()) {
				for (Unit unit : nation.units.values()) {
					// Tell infantry to build factory
					if (unit instanceof Infantry) {
						Infantry inf = (Infantry) unit;

						if (inf.engaged)
							continue;

						// Need to break bc of concurrent mod
						if (nation.buyFactory(inf.position))
							return;
					}

					// Tell factory to produce
					if (unit instanceof Factory) {
						Factory fac = (Factory) unit;

						if (fac.producing())
							continue;

						// Keep a good 5:1 ratio of cav:arti
						if (nation.countCavalry() < nation.countArtillery() * 5) {
							fac.startProduction(UnitType.CAVALRY);
						} else {
							fac.startProduction(UnitType.ARTILLERY);
						}
					}

					// Tell airfield to produce fighters
					if (unit instanceof Airfield) {
						Airfield air = (Airfield) unit;

						if (air.producing())
							continue;

						air.startProduction(UnitType.ATTACKER);
					}
				}
				if (nation.capital != null)
					nation.capital.setProducing(true);
			} else {
//			nation.capital.setProducing(false);
			}
		} catch (ConcurrentModificationException e) {
			System.out.println("Concurrent mod");
		}
	}

}
