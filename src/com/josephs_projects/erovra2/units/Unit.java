package com.josephs_projects.erovra2.units;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.apricotLibrary.interfaces.InputListener;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.gui.GUIWrapper;
import com.josephs_projects.erovra2.gui.Label;
import com.josephs_projects.erovra2.gui.ProgressBar;
import com.josephs_projects.erovra2.gui.Updatable;
import com.josephs_projects.erovra2.net.NetworkAdapter.AddUnit;
import com.josephs_projects.erovra2.net.NetworkAdapter.EngageTickUnit;
import com.josephs_projects.erovra2.net.NetworkAdapter.EngageUnit;
import com.josephs_projects.erovra2.net.NetworkAdapter.HitUnit;
import com.josephs_projects.erovra2.net.NetworkAdapter.KillUnit;
import com.josephs_projects.erovra2.net.NetworkAdapter.RemoveUnit;
import com.josephs_projects.erovra2.projectiles.Projectile;
import com.josephs_projects.erovra2.units.air.Attacker;
import com.josephs_projects.erovra2.units.air.Fighter;
import com.josephs_projects.erovra2.units.air.Plane;
import com.josephs_projects.erovra2.units.buildings.Airfield;
import com.josephs_projects.erovra2.units.buildings.Building;
import com.josephs_projects.erovra2.units.buildings.City;
import com.josephs_projects.erovra2.units.buildings.Factory;
import com.josephs_projects.erovra2.units.ground.Artillery;
import com.josephs_projects.erovra2.units.ground.Cavalry;
import com.josephs_projects.erovra2.units.ground.GroundUnit;
import com.josephs_projects.erovra2.units.ground.Infantry;

public abstract class Unit implements Tickable, Renderable, InputListener, Updatable {
	public Tuple position;
	public Tuple velocity;
	private Tuple target;
	public double a = 0;
	public int id;

	protected transient BufferedImage image = null;
	protected transient BufferedImage hit = null;
	public int hitTimer = 0;
	public double direction;
	public double scale = 1;

	public double health = 100;
	public boolean engaged = false;

	public UnitType type;
	public List<Class<? extends Projectile>> projectiles = new ArrayList<>();

	public Nation nation;

	public boolean dead;
	public double deathTicks = 0;
	protected int birthTick = 0;
	public int engagedTicks = 0;

	public boolean hovered = false;
	public static Unit selected = null; // Can be made a list
	public static Unit focused = null;

	protected GUIWrapper focusedOptions = new GUIWrapper(new Tuple(0, 0));

	protected GUIWrapper info = new GUIWrapper(new Tuple(0, 0));
	protected ProgressBar healthBar = new ProgressBar(176, 9, Erovra2.colorScheme);
	protected Label infoLabel = new Label("UNDEF", Erovra2.colorScheme);
	protected Label attackLabel = new Label("A/D/S:  ", Erovra2.colorScheme);

	// Creating a unit (SINGLEPLAYER/SENDING SIDE)
	public Unit(Tuple position, Nation nation, UnitType type) {
		this.position = new Tuple(position);
		this.target = new Tuple(position);
		this.nation = nation;
		this.type = type;
		birthTick = Erovra2.apricot.ticks;
		Erovra2.world.add(this);
		hit = type.hit;

		id = hashCode();
		if (Erovra2.net != null) {
			Erovra2.net.opQ.add(new AddUnit(this));
		}
		nation.units.put(id, this);
		nation.unitsMade++;

		attackLabel.fontSize = 12;
		attackLabel.text += type.attack + " / " + type.defense + " / " + type.speed;

		info.addGUIObject(infoLabel);
		info.addGUIObject(attackLabel);
		info.addGUIObject(healthBar);

		focusedOptions.addGUIObject(info);
	}

	// Creating a unit (RECEIVING SIDE)
	public Unit(Tuple position, Nation nation, UnitType type, int id) {
		this.position = new Tuple(position);
		this.target = new Tuple(position);
		this.nation = nation;
		this.type = type;
		birthTick = Erovra2.apricot.ticks;
		this.id = id;
		Erovra2.world.add(this);
		hit = type.hit;
		nation.units.put(id, this);
	}

	public static Unit makeUnit(Tuple position, Nation nation, UnitType type, int id) {
		switch (type) {
		case INFANTRY:
			return new Infantry(position, nation, id);
		case CAVALRY:
			return new Cavalry(position, nation, id);
		case ARTILLERY:
			return new Artillery(position, nation, id);
		case CITY:
			return new City(position, nation, id);
		case FACTORY:
			return new Factory(position, nation, id);
		case AIRFIELD:
			return new Airfield(position, nation, id);
		case FIGHTER:
			return new Fighter(position, nation, id);
		case ATTACKER:
			return new Attacker(position, nation, id);
		default:
			System.out.println("Unit.makeUnit() could not determine unit type");
			return null;
		}
	}

	@Override
	public void tick() {
		if (dead) {
			deadAnimation();
			return;
		}

		if (hitTimer > 0)
			hitTimer--;
		engagedTicks--;

		detectHit();
		move();
		if (Erovra2.net == null || nation == Erovra2.home)
			attack();
	}

	public AffineTransform getAffineTransform(BufferedImage image) {
		AffineTransform scaleCenter = new AffineTransform();
		if (image == null) {
			return scaleCenter;
		}
		AffineTransform align = new AffineTransform();
		align.translate(
				(position.x - Erovra2.terrain.offset.x) * Erovra2.zoom
						+ (Erovra2.apricot.width() - image.getWidth()) / 2,
				(position.y - Erovra2.terrain.offset.y) * Erovra2.zoom
						+ (Erovra2.apricot.height() - image.getHeight()) / 2);

		scaleCenter.translate(image.getWidth() / 2, image.getHeight() / 2);
		scaleCenter.rotate(direction);
		scaleCenter.scale(Erovra2.zoom * scale, Erovra2.zoom * scale);
		scaleCenter.translate(-image.getWidth() / 2, -image.getHeight() / 2);

		align.concatenate(scaleCenter);
		return align;
	}

	@Override
	public void render(Graphics2D g) {
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;

		if (hitTimer > 0 && !dead) {
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, hitTimer / 18.0f));
			g.drawImage(hit, getAffineTransform(hit), null);
		} else if (((hovered && selected == null&& !Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_CONTROL)) || selected == this) && type != UnitType.AIRFIELD
				&& type != UnitType.FACTORY && type != UnitType.CITY) {
			g.drawImage(hit, getAffineTransform(hit), null);
		}
		float deathOpacity = (float) Math.min(1, Math.max(0, (60 - deathTicks) / 60.0));
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, deathOpacity));
		g.drawImage(image, getAffineTransform(image), null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));

		if (focusedOptions != null) {
			healthBar.progress = health / 100.0;
			focusedOptions.setShown(this == focused);
			focusedOptions
					.updatePosition(new Tuple(Erovra2.terrain.minimap.getWidth(), Erovra2.apricot.height() - 150));
		}
	}

	@Override
	public void input(InputEvent e) {
		if (nation.ai != null)
			return;

		if (e == InputEvent.MOUSE_RIGHT_RELEASED && !Erovra2.apricot.keyboard.keyDown(KeyEvent.VK_CONTROL)) {
			if (focused == this) {
				focused = null;
			} else if (hovered && focused != this) {
				focused = this;
			}
		}
	}

	@Override
	public void update(String text) {

	}

	/**
	 * Method called to determine target of unit
	 * 
	 * @return Whether or not the target has been updated or not
	 */
	public boolean target() {
		return false;
	}

	/**
	 * Method called to attack enemy units if applicable
	 */
	public void attack() {
	}

	/**
	 * Method called to change the units position if applicable
	 */
	public void move() {
	}

	/**
	 * Detects if any enemy nation projectiles are without unit - If not a Building
	 * unit, flanking attacks do more damage than head on attacks
	 */
	public void detectHit() {
		try {
			List<Projectile> projectiles = new ArrayList<>(nation.enemyNation.projectiles.values());
			for (int i = 0; i < projectiles.size(); i++) {
				Projectile p = projectiles.get(i);
				if (!this.projectiles.contains(p.getClass()))
					continue;
				if (p.dangerous && boundingBox(p.position)) {
					health -= p.attack / (type.defense * (Erovra2.terrain.getHeight(position) / 10.0 + 37 / 40.0));

					if (this instanceof Building) {
						nation.visitedSpaces[(int) p.position.x / 32][(int) p.position.y / 32] = -100;
					} else {
						health += (p.velocity.normalize().dot(position.sub(target).normalize()) - 1);
					}
					setEngaged(true);
					setEngagedTicks();
					hitTimer = 18;
					if (nation == Erovra2.home && Erovra2.net != null) {
						Erovra2.net.opQ.add(new HitUnit(id));
					}

					p.remove();
					if (health < 0 && (Erovra2.net == null || nation == Erovra2.home)) {
						dead = true;
						if (Erovra2.net != null) {
							Erovra2.net.opQ.add(new KillUnit(id));
						}
						break;
					}
				}
			}
		} catch (ConcurrentModificationException e) {
			System.out.println("Unit.detectHit(): Concurrent Modification");
		}
	}

	public void deadAnimation() {
		deathTicks++;
		hitTimer = 0;
		if (deathTicks > 60 && (Erovra2.net == null || nation == Erovra2.home)) {
			remove();
		}
	}

	/**
	 * Removes unit from Apricot registry, nation's list of units
	 */
	@Override
	public void remove() {
		nation.unitsLost++;
		Erovra2.world.remove(this);
		focusedOptions.remove();
		nation.units.remove(id);
		nation.knownUnits.remove(this);
		if (Erovra2.net != null) {
			Erovra2.net.opQ.add(new RemoveUnit(this));
			Erovra2.net.updatedPositions.remove(id);
		}
	}

	public void removeReceiver() {
		Erovra2.world.remove(this);
		nation.units.remove(id);
		nation.knownUnits.remove(this);
		if (Erovra2.net != null)
			Erovra2.net.updatedPositions.remove(id);
	}

	/**
	 * @param t Velocity vector of unit
	 * @return Angle unit is facing (in radians)
	 */
	public float getRadian(Tuple t) {
		if (t.y != 0)
			return (float) (Math.atan2(t.y, t.x));
		if (t.x == 0)
			return 0;
		if (t.x > 0)
			return -(float) Math.PI / 2;
		return (float) Math.PI / 2;
	}

	public boolean boundingBox(Tuple point) {
		return point.dist(position) < 20;
	}

	public void setTarget(Tuple point) {
		double distance = point.dist(target);
		target.copy(point);
		velocity = getTarget().sub(position);
		if ((distance > 2 || this instanceof Plane) && Erovra2.net != null && nation == Erovra2.home)
			Erovra2.net.updatedPositions.put(id, this);
	}

	public Tuple getTarget() {
		return target;
	}

	public void setEngagedTicks() {
		setEngagedTicksReceiver();
		if (nation == Erovra2.home && Erovra2.net != null) {
			Erovra2.net.opQ.add(new EngageTickUnit(id));
		}
	}

	public void setEngagedTicksReceiver() {
		if (type.speed == 0) {
			engagedTicks = 10000000;
			return;
		}
		engagedTicks = (int) (128 / type.speed);
		if (nation.knownUnits != null && this instanceof GroundUnit) {
			nation.knownUnits.add(this);
		}
	}

	public boolean getEngaged() {
		return engaged;
	}

	public void setEngaged(boolean engaged) {
		if ((engaged != this.engaged) && nation == Erovra2.home && Erovra2.net != null) {
			Erovra2.net.opQ.add(new EngageUnit(id, engaged));
		}
		this.engaged = engaged;
	}

	public void setTargetReceiving(Tuple target) {
		this.target = target;
	}

	public boolean boundingbox(Tuple mousePosition) {
		return mousePosition.dist(position) < 20;
	}
}
