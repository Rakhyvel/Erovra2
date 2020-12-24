package com.josephs_projects.erovra2.units;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.erovra2.Erovra2;

public enum UnitType {
	INFANTRY("ground", "ground", "icons/infantry", 1, 0.5, 0.2, 5, 1, 15, 5, 0), CAVALRY("ground", "ground", "icons/cavalry",  0.6, 0.5, 0.6, 5, 1, 15, 5, 0),
	ARTILLERY("ground", "ground", "icons/artillery",  0.5, 0.5, 0.2, 5, 1, 15, 5, 0),

	FIGHTER("air", "fighter", "units/air/fighter1",  0.5, 1.6, 1, 1, 1, 15, 5, 0), ATTACKER("air", "attacker", "units/air/attacker1",  0.5, 1.6, 0.6, 1, 1, 15, 5, 0),
	BOMBER("air", "bomber", "units/air/bomber1",  0.5, 1.6, 0.5, 1, 1, 15, 5, 1),

	CITY("buildings", "city", "units/buildings/city",  1, 0, 0, 0, 1, 5, 0, 0), FACTORY("buildings", "factory", "units/buildings/factory",  1, 0, 0, 0, 1, 15, 0, 0),
	AIRFIELD("buildings", "airfield", "units/buildings/airfield",  1, 0, 0, 0, 1, 15, 0, 0),

	BOMB("air", "bomber", "bomb",  1, 0, 0, 0, 0, 5, 5, 0);

	public String name;
	public String branch;
	double defense;
	public double attack;
	public double speed;
	public double population;
	public double scale;
	
	public int[] resources;

	public BufferedImage image1;
	public BufferedImage hit;
	public BufferedImage icon;
	
	AffineTransform getIconAT(BufferedImage image) {
		AffineTransform scaleCenter = new AffineTransform();

		double scale = 32.0 / Math.max(image.getWidth(), image.getHeight());
		scaleCenter.scale(scale, scale);

		return scaleCenter;
	}
	
	public String getName() {
		return toString().charAt(0) + toString().substring(1).toLowerCase();
	}

	UnitType(String branch, String name, String iconPath, double defense, double attack, double speed, double population, double scale, int coins, int ore, int bombs) {
		this.name = name;
		this.defense = defense;
		this.attack = attack;
		this.speed = speed;
		this.population = population;
		this.scale = scale;
		
		this.resources = new int[3];
		this.resources[0] = coins;
		this.resources[1] = ore;
		this.resources[2] = bombs;
		
		BufferedImage preIcon;

		try {
			if (branch.equals("air")) {
				image1 = Apricot.image.loadImage("/res/units/" + branch + "/" + name + "1.png");
				hit = Apricot.image.loadImage("/res/units/" + branch + "/" + name + "Hit.png");
				preIcon = Apricot.image.loadImage("/res/units/" + branch + "/" + name + "1.png");
			} else {
				image1 = Apricot.image.loadImage("/res/units/" + branch + "/" + name + ".png");
				hit = Apricot.image.loadImage("/res/units/" + branch + "/hit.png");
			}
			System.out.println(iconPath);
			preIcon = Apricot.image.loadImage("/res/" + iconPath + ".png");
			Apricot.image.overlayBlend(preIcon, Erovra2.friendlyColor);
			icon = new AffineTransformOp(getIconAT(preIcon), AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(preIcon, icon);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
