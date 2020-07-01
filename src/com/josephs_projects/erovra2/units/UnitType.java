package com.josephs_projects.erovra2.units;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;

public enum UnitType {
	INFANTRY("ground", "ground", 1, 0.5, 0.2, 5), CAVALRY("ground", "ground", .5, 0.5, 0.6, 5),
	ARTILLERY("ground", "ground", .5, 1.6, 0.2, 5),

	FIGHTER("air", "fighter", 0.5, 1.6, 1, 1), ATTACKER("air", "attacker", 0.5, 1.6, 0.6, 1),
//	BOMBER("air", "bomber", 0.5, 5, 0.75),

	CITY("buildings", "city", 1, 0, 0, 0), FACTORY("buildings", "factory", 1, 0, 0, 0),
	AIRFIELD("buildings", "airfield", 1, 0, 0, 0);

	public String name;
	public String branch;
	double defense;
	public double attack;
	public double speed;
	public double population;

	public BufferedImage image1;
	public BufferedImage hit;

	UnitType(String branch, String name, double defense, double attack, double speed, double population) {
		this.name = name;
		this.defense = defense;
		this.attack = attack;
		this.speed = speed;
		this.population = population;

		try {
			if (branch.equals("air")) {
				image1 = Apricot.image.loadImage("/res/units/" + branch + "/" + name + "1.png");
				hit = Apricot.image.loadImage("/res/units/" + branch + "/" + name + "Hit.png");
			} else {
				image1 = Apricot.image.loadImage("/res/units/" + branch + "/" + name + ".png");
				hit = Apricot.image.loadImage("/res/units/" + branch + "/hit.png");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
