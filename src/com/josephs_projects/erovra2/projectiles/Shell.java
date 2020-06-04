package com.josephs_projects.erovra2.projectiles;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.net.NetworkAdapter.AddProjectile;

public class Shell extends Projectile {
	private static final long serialVersionUID = -6306728830380924870L;
	public Tuple target;

	public Shell(Tuple position, Tuple target, Nation nation, double attack) {
		super(position, target.sub(position), nation, attack, ProjectileType.SHELL);
		this.target = target;
		dangerous = false;
		// Do this so that target != null
		if (Erovra2.net != null)
			Erovra2.net.opQ.add(new AddProjectile(this));
	}

	public Shell(Tuple position, Tuple target, Nation nation, double attack, int id) {
		super(position, target.sub(position), nation, attack, ProjectileType.SHELL, id);
		this.target = target;
		dangerous = false;
	}

	@Override
	public void tick() {
		if (dangerous)
			remove();

		if (position.dist(target) < 2) {
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
