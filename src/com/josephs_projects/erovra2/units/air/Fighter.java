package com.josephs_projects.erovra2.units.air;

import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.projectiles.AABullet;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.buildings.Building;
import com.josephs_projects.erovra2.units.ground.GroundUnit;

public class Fighter extends Plane {

	public Fighter(Tuple position, Nation nation) {
		super(position, nation, UnitType.FIGHTER);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
	}

	public Fighter(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.FIGHTER, id);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
	}

	@Override
	public void remove() {
		nation.enemyNation.knownFighters.remove(this);
		super.remove();
	}

	/**
	 * Only selects targets for attack, and fires bullets.
	 */
	@Override
	public void attack() {
		Unit tempUnit = null;
		double tempDistance = 320;
		List<Unit> units = new ArrayList<Unit>(nation.enemyNation.units.values());
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if (unit.dead)
				continue;
			if (!(unit instanceof Plane))
				continue;
			double distance = patrolPoint.dist(unit.position);
			if (distance > tempDistance)
				continue;
			tempUnit = unit;
			tempDistance = distance;
			if (unit instanceof Fighter) {
				nation.knownFighters.add((Fighter) unit);
			}
		}

		if (tempUnit == null) {
			focalPoint.copy(patrolPoint);
			return;
		}

		// Target leading
		focalPoint.copy(tempUnit.position);
		if (tempUnit.position.dist(position) > 120)
			return;
		setEngagedTicks();
		if ((Erovra2.apricot.ticks - birthTick) % 15 == 0
				&& patrolPoint.sub(position).normalize().dot(velocity.normalize()) > 0.9) {
			new AABullet(new Tuple(position),
					tempUnit.position.sub(position.add(new Tuple(Math.random() * 20 - 10, Math.random() * 20 - 10))),
					nation, type.attack);
		}

	}

	// AI Methods
	public boolean recon() {
		// Scout out close ground units
		for (Unit unit : nation.enemyNation.units.values()) {
			if (!(unit instanceof Building || unit instanceof GroundUnit))
				continue;
			if (unit.position.dist(position) > 200)
				continue;

			unit.setEngagedTicksReceiver();
			setEngagedTicks();
			nation.visitedSpaces[(int) unit.position.x / 32][(int) unit.position.y / 32] = -1;
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

				double distance = patrolPoint.dist(point);

				if (nation.countFightingUnits() < nation.getOther() && point.dist(nation.capital.position) > 400)
					continue;

				if (distance > tempDist)
					continue;

				tempDist = distance;
				closestTile = point;
			}
		}
		// Set PatrolPoint to closest friendly, return if changed
		if (closestTile != null && !patrolPoint.equals(closestTile)) {
			patrolPoint.copy(closestTile);
			return true;
		}

		// Target not set, return false
		return false;
	}

	// Find friendly attackers !(and bombers), go to
	// ?(nearest/weakest/strongest/most important)
	public boolean escort() {
		Tuple closest = null;
		double tempDist = Double.POSITIVE_INFINITY;
		for (Unit unit : nation.enemyNation.units.values()) {
			if (!(unit instanceof Attacker))
				continue;
			double distance = position.dist(unit.position);
			if (distance > tempDist)
				continue;
			closest = unit.position;
			tempDist = distance;
		}
		// Set PatrolPoint to closest friendly, return if changed
		if (closest != null && !patrolPoint.equals(closest)) {
			patrolPoint.copy(closest);
			return true;
		}

		// PatrolPoint not set, return false
		return false;
	}
}
