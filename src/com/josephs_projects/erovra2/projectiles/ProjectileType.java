package com.josephs_projects.erovra2.projectiles;

import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;

public enum ProjectileType {
	AABULLET("bullet", 4), BULLET("bullet", 4), GROUNDTARGETBULLET("bullet", 4), SHELL("shell", 0.9), BOMB("bomb", 0);

	public String name;
	public BufferedImage image;
	public double speed;

	ProjectileType(String name, double speed) {
		this.name = name;
		this.speed = speed;
		try {
			image = Apricot.image.loadImage("/res/projectiles/" + name + ".png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
