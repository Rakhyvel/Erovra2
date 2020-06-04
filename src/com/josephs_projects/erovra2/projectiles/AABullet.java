package com.josephs_projects.erovra2.projectiles;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;

public class AABullet extends Projectile{
	private static final long serialVersionUID = 897555287201237110L;

	public AABullet(Tuple position, Tuple velocity, Nation nation, double attack) {
		super(position, velocity, nation, attack, ProjectileType.AABULLET);
	}

	public AABullet(Tuple position, Tuple velocity, Nation nation, double attack, int id) {
		super(position, velocity, nation, attack, ProjectileType.AABULLET, id);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.SURFACE_AIR_LEVEL;
	}
	
}
