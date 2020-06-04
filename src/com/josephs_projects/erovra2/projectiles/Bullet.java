package com.josephs_projects.erovra2.projectiles;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;

public class Bullet extends Projectile {
	private static final long serialVersionUID = -8356749066135099589L;

	public Bullet(Tuple position, Tuple velocity, Nation nation, double attack) {
		super(position, velocity, nation, attack, ProjectileType.BULLET);
	}

	public Bullet(Tuple position, Tuple velocity, Nation nation, double attack, int id) {
		super(position, velocity, nation, attack, ProjectileType.BULLET, id);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.SURFACE_PROJECTILE_LEVEL;
	}

}
