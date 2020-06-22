package com.josephs_projects.erovra2.units.ground;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.projectiles.Bullet;
import com.josephs_projects.erovra2.projectiles.GroundTargetBullet;
import com.josephs_projects.erovra2.projectiles.Shell;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.buildings.Building;

public abstract class GroundUnit extends Unit {

	public GroundUnit(Tuple position, Nation nation, UnitType type) {
		super(position, nation, type);
		projectiles.add(Bullet.class);
		projectiles.add(Shell.class);
		projectiles.add(GroundTargetBullet.class);
		this.image = Apricot.image.overlayBlendOutOfPlace(type.image1, nation.color);
	}

	public GroundUnit(Tuple position, Nation nation, UnitType type, int id) {
		super(position, nation, type, id);
		projectiles.add(Bullet.class);
		projectiles.add(Shell.class);
		projectiles.add(GroundTargetBullet.class);
		this.image = Apricot.image.overlayBlendOutOfPlace(type.image1, nation.color);
	}

	@Override
	public void tick() {
		super.tick();
		hovered = boundingbox(Erovra2.terrain.getMousePosition());
	}

	/**
	 * Targets unit, then moves according to the target set. - Will try not to let
	 * unit get into water - Will replenish visitedSpaces when in center of tile
	 */
	@Override
	public void move() {
		// Check to see if direction is good before moving
		// Definitely could be fixed, make turning smarter
		if (Math.abs(direction - getRadian(position.sub(getTarget()))) > 0.2 * type.speed) {
			if (direction > getRadian(position.sub(getTarget()))) {
				direction -= 0.2 * type.speed;
			} else {
				direction += 0.2 * type.speed;
			}
			return;
		}
		if (engaged)
			return;

		if (position.dist(getTarget()) < 1 || velocity.magnitude() < 10)
			return;

		if (velocity == null)
			return;

		// Take step towards target based on speed
		Tuple nextStep = position.add(velocity.normalize().scalar(type.speed));
		if (Erovra2.terrain.getHeight(nextStep) < 0.5) {
			setTarget(position);
			return;
		}
		position = nextStep;

		nation.visitedSpaces[(int) position.x / 32][(int) position.y / 32] = 11000;
	}

	/**
	 * Checks for nearby unvisited/alert tiles
	 */
	@Override
	public boolean target() {
		// Search for closest unvisited tile
		Tuple closestTile = null;
		double bestScore = Double.POSITIVE_INFINITY;
		for (int y = 0; y < Erovra2.size * 2; y++) {
			for (int x = 0; x < Erovra2.size * 2; x++) {
				Tuple point = new Tuple(x * 32 + 16, y * 32 + 16);
				// Tile must be unvisited
				if (nation.visitedSpaces[x][y] > 0)
					continue;

				double score = position.dist(point) * nation.visitedSpaces[x][y];

				// Must be land
				if (Erovra2.terrain.getHeight(point) <= 0.5)
					continue;

				// Prefer alerted tiles over uncertain tiles
//				if (nation.visitedSpaces[x][y] <= -1) {
//					score *= -1 / (Erovra2.size * 64 * nation.visitedSpaces[x][y]);
//				}

//				if (nation.countFightingUnits() < nation.getOther() && point.dist(nation.capital.position) > 100)
//					continue;

				// Must have direct line of sight to tile center
				if (score < bestScore && lineOfSight(point)) {
					bestScore = score;
					closestTile = point;
				}
			}
		}

		// Set target to closest tile if there is
		if (closestTile != null) {
			setTarget(closestTile);
			return true;
		}

		// Target not set, return false
		return false;
	}

	/**
	 * Chooses a random target, if that target is on land, sets units target to that
	 * point. No side effects if not.
	 */
	public void randomTarget() {
		double randX = Apricot.rand.nextDouble() - 0.5;
		double randY = Apricot.rand.nextDouble() - 0.5;
		Tuple newTarget = position.add(new Tuple(randX, randY).normalize().scalar(64));
		if (nation.countFightingUnits() < nation.getOther() && newTarget.dist(nation.capital.position) > 400) {
			setTarget(nation.capital.position);
			return;
		}
		if (Erovra2.terrain.getHeight(newTarget) > 0.5 && Erovra2.terrain.getHeight(newTarget) < 1)
			setTarget(newTarget);
	}

	/**
	 * Checks nearby enemy units, shoots a bullet if there are any. - Prefers other
	 * GroundUnits over Buildings - sets visitedSpace of closest enemy to alert -
	 * Engaged is true iff enemy units found
	 */
	@Override
	public void attack() {
		// Find closest enemy unit
		Unit closest = null;
		double closestDistance = Double.POSITIVE_INFINITY;
		boolean onlyBuildings = true;
		List<Unit> units = new ArrayList<Unit>(nation.enemyNation.units.values());
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if (!(unit instanceof GroundUnit || unit instanceof Building)) {
				continue;
			}
			double distance = unit.position.dist(position);
			if (unit.dead)
				continue;
			if (distance > 48)
				continue;
			if (unit instanceof Building) {
				int x = (int) unit.position.x / 32;
				int y = (int) unit.position.y / 32;
				nation.visitedSpaces[x][y] = -20;
			}
			// Prefer GroundUnits over Buildings
			if (distance < closestDistance) {
				if (unit instanceof Building) {
					if (!onlyBuildings)
						continue;
				} else {
					onlyBuildings = false;
				}

				closest = unit;
				closestDistance = distance;
				int x = (int) closest.position.x / 32;
				int y = (int) closest.position.y / 32;
				if (unit instanceof Building) {
					nation.visitedSpaces[x][y] = 0;
				} else {
					nation.visitedSpaces[x][y] = -1;
				}
			}
		}
		// Set engaged to false if none found
		if (closest == null) {
			setEngaged(false);
			return;
		}
		// Shoot enemy units if found
		setEngaged(true);
		setEngagedTicks();
		if ((Erovra2.apricot.ticks - birthTick) % 60 == 0) {
			setTarget(closest.position);
			new Bullet(new Tuple(position),
					closest.position.sub(position.add(new Tuple(Math.random() * 20 - 10, Math.random() * 20 - 10))),
					nation, type.attack * (Erovra2.terrain.getHeight(position) / 10.0 + 37 / 40.0));
		}
		int x = (int) closest.position.x / 32;
		int y = (int) closest.position.y / 32;
		if (!(closest instanceof Building)) {
			if (x > 0 && position.dist(closest.position.add(new Tuple(-32, 0))) > 48)
				nation.visitedSpaces[x - 1][y] = -20;

			if (y > 0 && position.dist(closest.position.add(new Tuple(0, -32))) > 48)
				nation.visitedSpaces[x][y - 1] = -20;

			if (x < (Erovra2.size * 2) - 1 && position.dist(closest.position.add(new Tuple(32, 0))) > 48)
				nation.visitedSpaces[x + 1][y] = -20;

			if (y < (Erovra2.size * 2) - 1 && position.dist(closest.position.add(new Tuple(0, 32))) > 48)
				nation.visitedSpaces[x][y + 1] = -20;
		}
	}

	/**
	 * Detects if there is a straight line of ground from the unit's position to a
	 * given target
	 * 
	 * @param dest Target point to detect from position to
	 * @return Whether there is a straight line path of solid ground from position
	 *         to dest
	 */
	public boolean lineOfSight(Tuple dest) {
		Tuple increment = dest.sub(position).normalize();
		Tuple check = new Tuple(position);
		double distance = position.dist(dest);
		for (int i = 0; i < distance; i++) {
			check = check.add(increment);
			double height = Erovra2.terrain.getHeight(check);
			if (height > 1 || height < 0.5) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void input(InputEvent e) {
		if (nation.ai != null)
			return;
		super.input(e);

		if (e == InputEvent.MOUSE_LEFT_RELEASED && !Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_CONTROL)) {
			if (selected == this) {
				if (Erovra2.apricot.mouse.position.x < Erovra2.terrain.minimap.getWidth()
						&& Erovra2.apricot.mouse.position.y > Erovra2.apricot.height()
								- Erovra2.terrain.minimap.getHeight()) {
					int y = (int) Erovra2.apricot.mouse.position.y - (Erovra2.apricot.height()
							- Erovra2.terrain.minimap.getHeight());
					double scale = Erovra2.terrain.size / (double) Erovra2.terrain.minimap.getWidth();
					setTarget(new Tuple(Erovra2.apricot.mouse.position.x * scale, y * scale));
				} else {
					setTarget(Erovra2.terrain.getMousePosition());
				}
				selected = null;
			} else if (hovered && selected != this) {
				focused = null;
				selected = this;
			}
		}
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.SURFACE_LEVEL;
	}

	/**
	 * All coordinates are absolute coordinates, meaning they do not change as the
	 * map's offset and zoom changes.
	 * 
	 * @param mousePosition Position of the mouse in absolute coordinates
	 * @return Whether or not the mouse is touching the ground unit.
	 */
	@Override
	public boolean boundingbox(Tuple mousePosition) {
		if (nation.ai != null)
			return false;

		int x = (int) mousePosition.x;
		int y = (int) mousePosition.y;
		double dx = position.x - x;
		double dy = y - position.y;

		double sin = Math.sin(direction);
		double cos = Math.cos(direction);

		boolean checkLR = Math.abs(sin * dx + cos * dy) <= 16;
		boolean checkTB = Math.abs(cos * dx - sin * dy) <= 8;
		return checkLR && checkTB;
	}

}
