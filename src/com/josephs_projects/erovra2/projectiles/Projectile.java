package com.josephs_projects.erovra2.projectiles;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.net.NetworkAdapter.AddProjectile;
import com.josephs_projects.erovra2.net.NetworkAdapter.RemoveProjectile;

public abstract class Projectile implements Tickable, Renderable, Serializable {
	private static final long serialVersionUID = 6639190201386635313L;
	public Tuple position;
	public Tuple velocity;
	double direction;
	public int id;

	public double attack;
	public boolean dangerous = true;

	public ProjectileType type;

	private Nation nation;

	// Create projectile (SENDING/SINGLEPLAYER)
	public Projectile(Tuple position, Tuple velocity, Nation nation, double attack, ProjectileType type) {
		this.position = position;
		this.velocity = velocity.normalize().scalar(type.speed);
		this.nation = nation;
		this.attack = attack;
		this.type = type;
		this.nation = nation;
		direction = getRadian(velocity);
		Erovra2.world.add(this);

		this.id = hashCode();
		// Have to do this so that target is not null
		if (Erovra2.net != null && type != ProjectileType.SHELL)
			Erovra2.net.opQ.add(new AddProjectile(this));
		nation.projectiles.put(id, this);
	}

	// Create projectile (RECEIVING)
	public Projectile(Tuple position, Tuple velocity, Nation nation, double attack, ProjectileType type, int id) {
		this.position = position;
		this.velocity = velocity.normalize().scalar(type.speed);
		this.nation = nation;
		this.attack = attack;
		this.type = type;
		this.nation = nation;
		direction = getRadian(velocity);
		this.id = id;
		Erovra2.world.add(this);
		
		nation.projectiles.put(id, this);
	}

	public static Projectile makeProjectile(Tuple position, Tuple velocity, Nation nation, double attack,
			ProjectileType type, int id) {
		switch (type) {
		case AABULLET:
			return new AABullet(position, velocity, nation, attack, id);
		case BULLET:
			return new Bullet(position, velocity, nation, attack, id);
		case GROUNDTARGETBULLET:
			return new GroundTargetBullet(position, velocity, nation, attack, id);
		case SHELL:
			return new Shell(position, velocity, nation, attack, id);
		default:
			return null;
		}
	}

	@Override
	public void tick() {
		position = position.add(velocity);
		if (position.x < 0 || position.x > Erovra2.size * 64 || position.y < 0 || position.y > Erovra2.size * 64) {
			remove();
		}
	}

	public AffineTransform getAffineTransform(BufferedImage image) {
		AffineTransform align = new AffineTransform();
		align.translate(
				(position.x - Erovra2.terrain.offset.x) * Erovra2.zoom
						+ (Erovra2.apricot.width() - image.getWidth()) / 2,
				(position.y - Erovra2.terrain.offset.y) * Erovra2.zoom
						+ (Erovra2.apricot.height() - image.getHeight()) / 2);

		AffineTransform scaleCenter = new AffineTransform();
		scaleCenter.translate(image.getWidth() / 2, image.getHeight() / 2);
		scaleCenter.rotate(direction);
		scaleCenter.scale(Erovra2.zoom, Erovra2.zoom);
		scaleCenter.translate(-image.getWidth() / 2, -image.getHeight() / 2);

		align.concatenate(scaleCenter);
		return align;
	}

	public float getRadian(Tuple t) {
		if (t.y != 0)
			return (float) (Math.atan2(t.y, t.x));
		if (t.x == 0)
			return 0;
		if (t.x > 0)
			return -(float) Math.PI / 2;
		return (float) Math.PI / 2;
	}

	@Override
	public void render(Graphics2D g) {
		g.drawImage(type.image, getAffineTransform(type.image), null);
	}

	@Override
	public int getRenderOrder() {
		return 2;
	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
		nation.projectiles.remove(id);
		
		if(nation == Erovra2.home && Erovra2.net != null) {
			Erovra2.net.opQ.add(new RemoveProjectile(this));
		}
	}
	
	public void removeReceiver() {
		Erovra2.world.remove(this);
		nation.projectiles.remove(id);
	}
}
