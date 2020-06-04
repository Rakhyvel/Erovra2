package com.josephs_projects.erovra2.units.air;

import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.projectiles.GroundTargetBullet;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.buildings.Building;
import com.josephs_projects.erovra2.units.ground.GroundUnit;

public class Attacker extends Plane {

	public Attacker(Tuple position, Nation nation) {
		super(position, nation, UnitType.ATTACKER);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
	}
	
	public Attacker(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.ATTACKER, id);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
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
			if(!(unit instanceof GroundUnit))
				continue;
			double distance = patrolPoint.dist(unit.position);
			if(distance > tempDistance)
				continue;
			tempUnit = unit;
			tempDistance = distance;
			int x = (int) tempUnit.position.x / 32;
			int y = (int) tempUnit.position.y / 32;
			if (unit instanceof Building) {
				nation.visitedSpaces[x][y] = 0;
			} else {
				nation.visitedSpaces[x][y] = -1;
			}
		}
		
		if(tempUnit == null) {
			patrolPoint.copy(nation.capital.position);
			setEngaged(false);
			return;
		}
		
		patrolPoint.copy(tempUnit.position);
		setEngaged(true);
		setEngagedTicks();
		if(tempUnit.position.sub(position).normalize().dot(velocity.normalize()) < 0.9)
			return;
		if(position.dist(patrolPoint) > 320)
			return;
		if((Erovra2.apricot.ticks - birthTick) % 60 == 0) {
			new GroundTargetBullet(new Tuple(position), tempUnit.position.sub(position.add(new Tuple(Math.random() * 20 - 10, Math.random() * 20 - 10))), nation, type.attack);
		}
		
		int x = (int) tempUnit.position.x / 32;
		int y = (int) tempUnit.position.y / 32;
		if (!(tempUnit instanceof Building)) {
			if (x > 0 && position.dist(tempUnit.position.add(new Tuple(-32, 0))) > 48)
				nation.visitedSpaces[x - 1][y] = -20;

			if (y > 0 && position.dist(tempUnit.position.add(new Tuple(0, -32))) > 48)
				nation.visitedSpaces[x][y - 1] = -20;

			if (x < (Erovra2.size * 2) - 1 && position.dist(tempUnit.position.add(new Tuple(32, 0))) > 48)
				nation.visitedSpaces[x + 1][y] = -20;

			if (y < (Erovra2.size * 2) - 1 && position.dist(tempUnit.position.add(new Tuple(0, 32))) > 48)
				nation.visitedSpaces[x][y + 1] = -20;
		}
		
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
