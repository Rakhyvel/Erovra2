package com.josephs_projects.erovra2.units.air;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.gui.Button;
import com.josephs_projects.apricotLibrary.gui.Updatable;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.projectiles.Bomb;
import com.josephs_projects.erovra2.units.UnitType;

/*
 * Bombers drop bombs onto enemies. They are slower, but can attack buildings
 */
public class Bomber extends Plane {
	public boolean bombLoaded = true;
	boolean armed = false;

	private Button armButton = new Button("Arm bomb", 176, 30, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world, (Updatable) this);
	public Bomber(Tuple position, Nation nation) {
		super(position, nation, UnitType.BOMBER);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
		infoLabel.text = nation.registerNewDivisionOrdinal(type) + " Bomber Squadron";
		actionButtons.addGUIObject(armButton);
	}

	public Bomber(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.BOMBER, id);
		this.velocity = getTarget().sub(position).normalize().scalar(type.speed);
	}
	
	@Override
	public void update(String text) {
		if(text.equals("Arm bomb")) {
			armed = true;
			armButton.label.text = "Disarm bomb";
		} else if(text.equals("Disarm bomb")) {
			armed = false;
			armButton.label.text = "Arm bomb";
		} else {
			super.update(text);
		}
	}

	@Override
	public void remove() {
		nation.enemyNation.knownPlanes.remove(this);
		super.remove();
	}

	@Override
	public void attack() {
		focalPoint.copy(patrolPoint);
		setEngaged(false);
		if(position.dist(patrolPoint) < 1 && armed && bombLoaded) {
			System.out.println("Bomb!");
			new Bomb(new Tuple(position), nation, 100);
			bombLoaded = false;
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
