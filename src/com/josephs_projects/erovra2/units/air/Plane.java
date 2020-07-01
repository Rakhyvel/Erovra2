package com.josephs_projects.erovra2.units.air;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.particles.Smoke;
import com.josephs_projects.erovra2.projectiles.AABullet;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;

public class Plane extends Unit {
	private transient BufferedImage left;
	private transient BufferedImage center;
	private transient BufferedImage right;
	public Tuple patrolPoint;
	public Tuple focalPoint = new Tuple();

	public Plane(Tuple position, Nation nation, UnitType type) {
		super(position, nation, type);
		try {
			left = Apricot.image.loadImage("/res/units/air/" + type.name + "1.png");
			center = Apricot.image.loadImage("/res/units/air/" + type.name + "2.png");
			right = Apricot.image.loadImage("/res/units/air/" + type.name + "3.png");
			hit = Apricot.image.loadImage("/res/units/air/" + type.name + "Hit.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Apricot.image.overlayBlend(left, nation.color);
		Apricot.image.overlayBlend(center, nation.color);
		Apricot.image.overlayBlend(right, nation.color);
		projectiles.add(AABullet.class);
		this.patrolPoint = new Tuple(position);
	}

	public Plane(Tuple position, Nation nation, UnitType type, int id) {
		super(position, nation, type, id);
		try {
			left = Apricot.image.loadImage("/res/units/air/" + type.name + "1.png");
			center = Apricot.image.loadImage("/res/units/air/" + type.name + "2.png");
			right = Apricot.image.loadImage("/res/units/air/" + type.name + "3.png");
			hit = Apricot.image.loadImage("/res/units/air/" + type.name + "Hit.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Apricot.image.overlayBlend(left, nation.color);
		Apricot.image.overlayBlend(center, nation.color);
		Apricot.image.overlayBlend(right, nation.color);
		projectiles.add(AABullet.class);
		this.patrolPoint = new Tuple(position);
	}

	@Override
	public void tick() {
		if (health < 20 && Erovra2.apricot.ticks % 8 == 0 && !Erovra2.geneticTournament) {
			new Smoke(position);
		}
		super.tick();
		hovered = boundingbox(Erovra2.terrain.getMousePosition());
	}

	@Override
	public void render(Graphics2D g) {		
		if(engagedTicks <= 0)
			return;
		
		int ticks = Erovra2.apricot.ticks / 2;
//		direction -= Math.sin(ticks * 0.01) * 0.01;
		float deathOpacity = (float) ((60 - deathTicks) / 60.0);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, deathOpacity));
		if (ticks % 4 == 0) {
			g.drawImage(left, getAffineTransform(left), null);
		} else if (ticks % 4 == 1) {
			g.drawImage(center, getAffineTransform(center), null);
		} else if (ticks % 4 == 2) {
			g.drawImage(right, getAffineTransform(right), null);
		} else if (ticks % 4 == 3) {
			g.drawImage(center, getAffineTransform(center), null);
		}

		if(nation == Erovra2.enemy)
			return;
		
		if (hitTimer > 0 && !dead) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, hitTimer / 18.0f));
			g.drawImage(hit, getAffineTransform(hit), null);
		} else if (hovered || selected == this) {
			g.drawImage(hit, getAffineTransform(hit), null);
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
	}

	@Override
	public void move() {
		if (Erovra2.net == null || nation == Erovra2.home)
			target();

		if (velocity == null)
			return;

		position = position.add(velocity.normalize().scalar(type.speed));
		if (!engaged) {
			for (int y = 0; y < Erovra2.size * 2; y++) {
				for (int x = 0; x < Erovra2.size * 2; x++) {
					if (nation.visitedSpaces[x][y] >= 0 && position.dist(new Tuple(x * 32 + 16, y * 32 + 16)) < 320)
						nation.visitedSpaces[x][y] = 11000;
				}
			}
		}
		if (position.x > Erovra2.size * 64) {
			a += 0.06f;
			position.x = Erovra2.size * 64;
		}
		if (position.x < 0) {
			a += 0.06f;
			position.x = 0;
		}
		if (position.y > Erovra2.size * 64) {
			a += 0.06f;
			position.y = Erovra2.size * 64;
		}
		if (position.y < 0) {
			a += 0.06f;
			position.y = 0;
		}
	}

	public boolean torget() {
		Tuple innerCircle = position.sub(patrolPoint).normalize().scalar(72);
		Tuple perpendicular = new Tuple(-innerCircle.y, innerCircle.x);

		setTarget(patrolPoint.add(innerCircle.add(perpendicular.scalar(1))));
		direction -= Math.PI / 2;
		return true;
	}

	@Override
	public boolean target() {
		Tuple innerCircle = position.sub(focalPoint).normalize();
		Tuple perpendicularVelocity = new Tuple(-velocity.y, velocity.x);
		double targetAlignment = velocity.normalize().dot(innerCircle);

		if (targetAlignment < 0.92 && position.dist(focalPoint) > 64) {
			// If plane is basically on target
			if (perpendicularVelocity.dot(innerCircle) > 0.03) {
				a += 0.03f;
			} else if (perpendicularVelocity.dot(innerCircle) < -0.03) {
				a -= 0.03f;
			}
		} else if (targetAlignment > -0.9 && position.dist(focalPoint) > 64) {
			// Spin around
			// Larger value means longer extend
			a += 0.03f;
		}
		setTarget(position.add(new Tuple(Math.sin(a), Math.cos(a))));
		direction -= Math.PI / 2;

		return true;
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.AIR_LEVEL;
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
					this.patrolPoint.copy(new Tuple(Erovra2.apricot.mouse.position.x * scale, y * scale));
				} else {
					this.patrolPoint.copy(Erovra2.terrain.getMousePosition());
				}
				selected = null;
			} else if (hovered && selected != this) {
				focused = null;
				selected = this;
			}
		}
	}

	@Override
	public void deadAnimation() {
		scale *= 0.99;
		position = position.add(velocity);

		deathTicks += 0.5;
		hitTimer = 0;
		if (deathTicks > 60) {
			remove();
		}
	}

	@Override
	public void setTarget(Tuple point) {
		super.setTarget(point);
		direction = getRadian(position.sub(getTarget()));
	}

}
