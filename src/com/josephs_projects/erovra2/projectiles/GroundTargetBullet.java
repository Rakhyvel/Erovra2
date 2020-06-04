package com.josephs_projects.erovra2.projectiles;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;

public class GroundTargetBullet extends Projectile {
	private static final long serialVersionUID = -7051671491886423559L;

	public GroundTargetBullet(Tuple position, Tuple velocity, Nation nation, double attack) {
		super(position, velocity, nation, attack, ProjectileType.GROUNDTARGETBULLET);
	}

	public GroundTargetBullet(Tuple position, Tuple velocity, Nation nation, double attack, int id) {
		super(position, velocity, nation, attack, ProjectileType.GROUNDTARGETBULLET, id);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.SURFACE_AIR_LEVEL;
	}

}
