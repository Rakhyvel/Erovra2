package com.josephs_projects.erovra2.units.buildings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.gui.Label;
import com.josephs_projects.apricotLibrary.gui.RockerSwitch;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.particles.Coin;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Bomber;
import com.josephs_projects.erovra2.units.ground.Infantry;

public class City extends Building {
	boolean capital;
	int workTimer = 18000;
	boolean producing = true;
	private static Point[] decoration = new Point[1];
	private static Point[] dst = new Point[1];
	public String name;
	Font bigFont = new Font("Arial", Font.PLAIN, 24);
	double oreMined = 0;
	Label oreMinedLabel = new Label("", Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);

	private Label recruitsLabel = new Label("New recruits: ", Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);
	public RockerSwitch recruitSwitch = new RockerSwitch("Recruitment ", 40, 20, Erovra2.colorScheme, Erovra2.apricot, Erovra2.world);

	List<Building> buildings = new ArrayList<>();

	static {
		// relative to a static version of the sprite
		// will be rotated and everything if need be
		decoration[0] = new Point(16, 20);
		dst[0] = new Point();
	}

	public City(Tuple position, Nation nation) {
		super(position, nation, UnitType.CITY);
		name = nation.cityNames.randName(6, 9);
		nation.cities.add(this);
		infoLabel.text = name;
		oreMinedLabel.fontSize = 14;
		info.addGUIObject(oreMinedLabel);
		recruitsLabel.fontSize = 17;
		nation.population += 10;

		info.addGUIObject(recruitsLabel);
		info.addGUIObject(recruitSwitch);
	}

	public City(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.CITY, id);
	}

	public City(Tuple position, Nation nation, String name, double oreMined) {
		super(position, nation, UnitType.CITY);
		this.name = name;
		this.oreMined = oreMined;
	}

	@Override
	public void tick() {
		if (Erovra2.apricot.ticks % 300 == 0) {
			new Coin(new Tuple(position), nation);
		}
		if ((Erovra2.net == null || nation == Erovra2.home) && workTimer == 0) {
			new Infantry(position, nation);
			if (nation == Erovra2.home)
				Erovra2.gui.messageContainer.addMessage("New recruits ready at " + name + "!", nation.color);
			workTimer = 18000;
		}
		if (recruitSwitch != null && recruitSwitch.value
				&& nation.mobilized <= nation.population - UnitType.INFANTRY.population)
			workTimer--;
		if (recruitSwitch != null && nation.mobilized > nation.population - UnitType.INFANTRY.population) {
			recruitSwitch.value = false;
		}
		double ore = Math.max(0, (Erovra2.terrain.getOre(position) - 0.5));
		oreMined += ore / 100.0;
		if (oreMinedLabel != null)
			oreMinedLabel.text = "Ore: " + (int) oreMined + "&o";
		super.tick();
	}

	@Override
	public void render(Graphics2D g) {
		AffineTransform af = getAffineTransform(image);
		af.transform(decoration, 0, dst, 0, 1);
		super.render(g);
		if (nation == Erovra2.enemy && engagedTicks <= 0 && !dead)
			return;

		g.setColor(Color.white);
		bigFont = new Font("sitka text", Font.PLAIN, (int) (15 * (Erovra2.zoom + 0.5)));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setFont(bigFont);
		if (name != null) {
			int width = g.getFontMetrics(bigFont).stringWidth(name);
			g.drawString(name, dst[0].x - width / 2, dst[0].y + (int) (24 * Erovra2.zoom));
		}
		
		g.setColor(new Color(0, 0, 0, 255));
		g.setStroke(new BasicStroke((float) (Erovra2.zoom + 1), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (Erovra2.zoom > 1.5) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		} else {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		
		int seconds = workTimer / 60;
		int minutes = seconds / 60;
		if (recruitsLabel != null)
			recruitsLabel.text = "Recruits: " + minutes + "m " + (seconds - minutes * 60) + "s";
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	public void changeToCapital() {
		try {
			image = Apricot.image.loadImage("/res/units/buildings/capital.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Apricot.image.overlayBlend(image, nation.color);
		this.capital = true;
		new Infantry(position, nation);
		Bomber f = new Bomber(position, nation);
		f.health = 50;
		recruitSwitch.value = true;
	}

	public void setProducing(boolean producing) {
		this.producing = producing;
	}

	@Override
	public void remove() {
		dead = false;
		deathTicks = 0;
		health = 100;

		nation.unitsLost++;
		nation.units.remove(id);
		nation.cities.remove(this);
		nation.enemyNation.units.put(id, this);
		nation.population -= 10;
		nation = nation.enemyNation;
		nation.population += 10;
		nation.cities.add(this);
		try {
			image = Apricot.image.loadImage("/res/units/buildings/city.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Apricot.image.overlayBlend(image, nation.color);
		for (Building b : buildings)
			b.remove();
	}
	
	public boolean containsAirfield() {
		for(Building b : buildings) {
			if(b instanceof Airfield) {
				return true;
			}
		}
		return false;
	}
}
