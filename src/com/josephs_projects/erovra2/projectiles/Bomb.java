package com.josephs_projects.erovra2.projectiles;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.net.NetworkAdapter.AddProjectile;

public class Bomb extends Projectile {
	private static final long serialVersionUID = -6306728830380924870L;
	int height;

	public Bomb(Tuple position, Nation nation, double attack) {
		super(position, new Tuple(), nation, attack, ProjectileType.BOMB);
		this.height = 64;
		dangerous = false;
		// Do this so that target != null
		if (Erovra2.net != null)
			Erovra2.net.opQ.add(new AddProjectile(this));
	}

	public Bomb(Tuple position, int height, Nation nation, double attack, int id) {
		super(position, new Tuple(), nation, attack, ProjectileType.BOMB, id);
		this.height = height;
		dangerous = false;
	}

	@Override
	public void tick() {
		height -= 0.01;
		if (dangerous)
			remove();

		if (height < 1) {
			dangerous = true;
			if(Erovra2.net != null) {
//				Erovra2.net.opQ.add(new EndangerProjectile(this));
			}
		}

		super.tick();
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.SURFACE_AIR_LEVEL;
	}
}
