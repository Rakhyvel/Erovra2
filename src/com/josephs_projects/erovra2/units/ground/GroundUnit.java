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
import com.josephs_projects.erovra2.units.buildings.City;

public abstract class GroundUnit extends Unit {
	public boolean stuckIn = false;

	public GroundUnit(Tuple position, Nation nation, UnitType type) {
		super(position, nation, type);
		projectiles.add(Bullet.class);
		projectiles.add(Shell.class);
		projectiles.add(GroundTargetBullet.class);
		this.image = Apricot.image.overlayBlendOutOfPlace(type.image1, nation.color);
		nation.mobilized += 5;
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
		if (getAlignment(lookat) > 0.2 * type.speed) {
			if (direction > getRadian(position.sub(lookat))) {
				direction -= 0.2 * type.speed;
			} else {
				direction += 0.2 * type.speed;
			}
			return;
		}

		if (position.dist(getTarget()) < 1 || velocity.magnitude() < 10 || stuckIn)
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
	 * Checks nearby enemy units, shoots a bullet if there are any. - Prefers other
	 * GroundUnits over Buildings - sets visitedSpace of closest enemy to alert -
	 * Engaged is true iff enemy units found
	 */
	@Override
	public void attack() {
		stuckIn = false;
		// Find closest enemy unit
		Unit closest = null;
		double closestDistance = 68;
		boolean onlyBuildings = true;
		List<Unit> units = new ArrayList<Unit>(nation.enemyNation.units.values());
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if (!(unit instanceof GroundUnit || unit instanceof City)) {
				continue;
			}
			double distance = unit.position.dist(position);
			if (unit.dead)
				continue;
			if (distance > 68)
				continue;
			if (unit instanceof Building) {
				int x = (int) unit.position.x / 32;
				int y = (int) unit.position.y / 32;
				nation.visitedSpaces[x][y] = -20;
			}
			// Prefer GroundUnits over Buildings
			if (distance < closestDistance || onlyBuildings && unit instanceof GroundUnit) {
				if (unit instanceof GroundUnit) {
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
		if (closest.type != UnitType.CITY && closest.position.dist(position) < 48) {
			stuckIn = true;
			setTarget(position);
		}
		if (getTarget().cabDist(position) < 1 || stuckIn) {
			lookat.copy(closest.position);
		}
		setEngaged(true);
		setEngagedTicks();

		// Shoot enemy units if found
		double alignment = getAlignment(closest.position);
		if ((Erovra2.apricot.ticks - birthTick) % 60 == 0 && alignment < 0.2 * type.speed) {
			new Bullet(new Tuple(position), closest.position.sub(position), nation,
					type.attack * (Erovra2.terrain.getHeight(position) / 10.0 + 37 / 40.0));
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

	private double getAlignment(Tuple point) {
		double first = Math.abs(direction - getRadian(position.sub(point)));
		double second = Math.abs(direction - getRadian(position.sub(point)) - 2 * Math.PI);
		double third = Math.abs(direction - getRadian(position.sub(point)) + 2 * Math.PI);
		return Math.min(first, Math.min(third, second));
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
					int y = (int) Erovra2.apricot.mouse.position.y
							- (Erovra2.apricot.height() - Erovra2.terrain.minimap.getHeight());
					double scale = Erovra2.terrain.size / (double) Erovra2.terrain.minimap.getWidth();
					setTarget(new Tuple(Erovra2.apricot.mouse.position.x * scale, y * scale));
				} else {
					setTarget(Erovra2.terrain.getMousePosition());
					lookat.copy(getTarget());
				}
				selected = null;
			} else if (hovered && selected != this
					&& (selected == null || (selected != null && selected.getRenderOrder() < getRenderOrder()))) {
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
	
	@Override
	public void remove() {
		nation.mobilized -= type.population;
		super.remove();
	}

}
