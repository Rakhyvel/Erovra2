package com.josephs_projects.erovra2.units.buildings;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.projectiles.Bullet;
import com.josephs_projects.erovra2.projectiles.Shell;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;

public class Building extends Unit {

	public Building(Tuple position, Nation nation, UnitType type) {
		super(position, nation, type);
		projectiles.add(Bullet.class);
		projectiles.add(Shell.class);
		this.image = Apricot.image.overlayBlendOutOfPlace(type.image1, nation.color);
	}

	public Building(Tuple position, Nation nation, UnitType type, int id) {
		super(position, nation, type, id);
		projectiles.add(Bullet.class);
		projectiles.add(Shell.class);
		this.image = Apricot.image.overlayBlendOutOfPlace(type.image1, nation.color);
	}

	@Override
	public void tick() {
		if (dead) {
			deadAnimation();
			return;
		}
		hovered = boundingBox(Erovra2.terrain.getMousePosition());

		if (hitTimer > 0)
			hitTimer--;

		detectHit();
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.BUILDING_LEVEL;
	}

	@Override
	public void remove() {
		super.remove();
		nation.built[(int) position.x / 64][(int) position.y / 64] = false;
	}

	@Override
	public boolean boundingBox(Tuple point) {
		return point.x < position.x + 6 && point.y < position.y + 6 && point.x > position.x - 6
				&& point.y > position.y - 6;
	}

}
