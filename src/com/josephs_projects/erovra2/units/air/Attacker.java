package com.josephs_projects.erovra2.units.air;

import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.projectiles.GroundTargetBullet;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.ground.GroundUnit;

public class Attacker extends Plane {

	public Attacker(Tuple position, Nation nation) {
		super(position, nation, UnitType.ATTACKER);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
		infoLabel.text = nation.registerNewDivisionOrdinal(type) + " Attacker Squadron";
	}

	public Attacker(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.ATTACKER, id);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
	}

	@Override
	public void remove() {
		nation.enemyNation.knownPlanes.remove(this);
		super.remove();
	}

	@Override
	public void attack() {
		Unit tempUnit = null;
		double tempDistance = 320;
		List<Unit> units = new ArrayList<Unit>(nation.enemyNation.units.values());
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if (unit.dead)
				continue;
			if (!(unit instanceof GroundUnit))
				continue;
			double distance = patrolPoint.dist(unit.position);
			if (distance > tempDistance)
				continue;
			tempUnit = unit;
			tempDistance = distance;
			nation.enemyNation.knownPlanes.add(this);
		}

		if (tempUnit == null) {
			focalPoint.copy(patrolPoint);
			setEngaged(false);
			return;
		}

		focalPoint.copy(tempUnit.position);
		if (tempUnit.position.dist(position) > 120)
			return;
		setEngaged(true);
		setEngagedTicks();

		double alignment = getAlignment(tempUnit.position);
		if ((Erovra2.apricot.ticks - birthTick) % 15 == 0 && alignment < 0.2 * type.speed) {
			new GroundTargetBullet(new Tuple(position), tempUnit.position.sub(position), nation, type.attack);
		}

	}

	private double getAlignment(Tuple point) {
		double first = Math.abs(direction + Math.PI / 2 - getRadian(position.sub(point)));
		double second = Math.abs(direction + Math.PI / 2 - getRadian(position.sub(point)) - 2 * Math.PI);
		double third = Math.abs(direction + Math.PI / 2 - getRadian(position.sub(point)) + 2 * Math.PI);
		return Math.min(first, Math.min(third, second));
	}

	// AI Methods
	public void flyToAlertedTiles() {
		// Search for closest unvisited tile
		Tuple closestTile = null;
		double tempDist = Double.POSITIVE_INFINITY;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				Tuple point = new Tuple(x * 32 + 16, y * 32 + 16);
				// Tile must be alerted
				if (nation.visitedSpaces[x][y] >= -0.1)
					continue;

				double score = position.dist(point);
				score *= -1 / (Erovra2.size * 64 * nation.visitedSpaces[x][y]);

				if (nation.countFightingUnits() < nation.getOther() && point.dist(nation.capital.position) > 400)
					continue;

				if (score < tempDist) {
					tempDist = score;
					closestTile = point;
				}
			}
		}

		// Set target to closest tile if there is
		if (closestTile != null) {
			patrolPoint.copy(closestTile);
		}
	}
}
