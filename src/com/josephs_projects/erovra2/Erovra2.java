package com.josephs_projects.erovra2;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.World;
import com.josephs_projects.apricotLibrary.audio.AudioClip;
import com.josephs_projects.apricotLibrary.gui.ColorScheme;
import com.josephs_projects.apricotLibrary.gui.Updatable;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.apricotLibrary.interfaces.InputListener;
import com.josephs_projects.erovra2.ai.NewAI;
import com.josephs_projects.erovra2.net.Client;
import com.josephs_projects.erovra2.net.NetworkAdapter;
import com.josephs_projects.erovra2.net.Server;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.buildings.City;
import com.josephs_projects.erovra2.units.ground.Artillery;
import com.josephs_projects.erovra2.units.ground.Cavalry;
import com.josephs_projects.erovra2.units.ground.Infantry;

/*
 * Principles:
 * - Winning should not be random
 * - No pay to win
 * 
 * Name Ideas:
 * - Civitania
 * - 
 * 
 */

public class Erovra2 implements InputListener {
	public static Apricot apricot;
	public static World world;
	public Image icon = new ImageIcon(getClass().getResource("/res/icon.png")).getImage();

	public static NetworkAdapter net;

	public static Nation home;
	public static Nation enemy;

	public static Color friendlyColor = new Color(105, 105, 210);
	public static Color enemyColor = new Color(210, 105, 105);

	public static Terrain terrain;
	public static GUI gui;

	public static int size = 16;
	public static double zoom = 2;
	public static double dt = 16;

	public static final int TERRAIN_LEVEL = 0;
	public static final int BUILDING_LEVEL = 1;
	public static final int SURFACE_PROJECTILE_LEVEL = 2;
	public static final int SURFACE_LEVEL = 3;
	public static final int SURFACE_AIR_LEVEL = 4;
	public static final int AIR_LEVEL = 5;
	public static final int GUI_LEVEL = 6;
	public static final ColorScheme colorScheme = new ColorScheme(
			new Color(40, 40, 40, 180),  // backgroundColor
			new Color(250, 250, 250),    // borderColor   
			new Color(128, 128, 128, 180),// highlightColor
			new Color(250, 250, 250),    // textColor
			new Color(128, 128, 128),    // disabledTextColor
			enemyColor,      			 // errorColor 
			friendlyColor); // fillColor        

	public static AudioClip gun;
	public static AudioClip mortar;
	public static AudioClip explode;

	public static void main(String[] args) {
		apricot = new Apricot("Civitania", 1166, 640);
//		apricot.setIcon(new Erovra2().icon);
		world = new World();
		Apricot.rand.setSeed(0);

		try {
			gun = new AudioClip("src/res/audio/gun.wav");
			mortar = new AudioClip("src/res/audio/mortar.wav");
			explode = new AudioClip("src/res/audio/artFire.wav");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (args.length > 0) {
			if (args[0].equals("server")) {
				terrain = new Terrain(size * 64, Apricot.rand.nextInt());
				net = new Server();
				net.start();
			} else {
				net = new Client(args[0]);
				net.start();
			}
		} else {
			terrain = new Terrain(size * 64, Apricot.rand.nextInt());
			startNewMatch();
			gui = new GUI(home);
			Erovra2.home.setCapital(new City(Erovra2.home.capitalPoint, Erovra2.home));
			Erovra2.enemy.setCapital(new City(Erovra2.enemy.capitalPoint, Erovra2.enemy));
			Erovra2.terrain.setOffset(new Tuple(Erovra2.size / 2 * 64, Erovra2.size / 2 * 64));
			terrain.setOffset(home.capitalPoint);
		}

		apricot.setWorld(world);
		world.add(new Erovra2());
		apricot.start();
	}

	public static void startNewMatch() {
		home = new Nation("Home nation", friendlyColor, null);
		enemy = new Nation("Enemy nation", enemyColor, new NewAI());
		home.enemyNation = enemy;
		enemy.enemyNation = home;

		home.capitalPoint = findBestLocation(new Tuple((64 * size) - 64 - 32, (64 * size) - 32));
		enemy.capitalPoint = findBestLocation(new Tuple(64 + 32, 32));

		Erovra2.world.add(home);
		Erovra2.world.add(enemy);
	}

	public static Tuple findBestLocation(Tuple start) {
		// Search for closest unvisited tile
		Tuple closestTile = null;
		double tempDist = Double.POSITIVE_INFINITY;
		for (int y = 0; y < Erovra2.size; y++) {
			for (int x = 0; x < Erovra2.size; x++) {
				Tuple point = new Tuple(x * 64 + 32, y * 64 + 32);
				// Must be land
				if (terrain.getHeight(point) <= 0.5)
					continue;
				// Gotta start with the goods
				if (terrain.getOre(point) <= 0.66)
					continue;
				double score = start.dist(point) + 64 * Math.random();

				// Must have direct line of sight to tile center
				if (score < tempDist) {
					tempDist = score;
					closestTile = point;
				}
			}
		}

		return closestTile;
	}

	public static void setNationColors() {
		home.color = friendlyColor;
		enemy.color = enemyColor;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List copyList(List orig) {
		ArrayList retval = new ArrayList();
		for (int i = 0; i < orig.size(); i++) {
			retval.add(orig.get(i));
		}
		return retval;
	}

	public void input(InputEvent e) {
		if (e == InputEvent.KEY_RELEASED) {
			if (apricot.keyboard.lastKey == KeyEvent.VK_PERIOD) {
				dt /= 2;
				apricot.setDeltaT(dt);
				gui.messageContainer.addMessage("Time warp: " + String.format("%.0f", 16.0 / dt) + "x", Color.white);
			} else if (apricot.keyboard.lastKey == KeyEvent.VK_COMMA) {
				dt *= 2;
				apricot.setDeltaT(dt);
				gui.messageContainer.addMessage("Time warp: " + String.format("%.0f", 16.0 / dt) + "x", Color.white);
			} else if (apricot.keyboard.lastKey == KeyEvent.VK_SHIFT) {
				int infantryCounter = 0;
				int cavalryCounter = 0;
				int artilleryCounter = 0;
				for (Unit unit : home.units.values()) {
					if (unit instanceof Infantry) {
						infantryCounter++;
					} else if (unit instanceof Cavalry) {
						cavalryCounter++;
					} else if (unit instanceof Artillery) {
						artilleryCounter++;
					}
				}
				gui.messageContainer.addMessage(
						String.format("i: %d c: %d a: %d", infantryCounter, cavalryCounter, artilleryCounter),
						Color.white);
			}
		}
	}

	@Override
	public void remove() {
	}
}
class ButtonListener implements Updatable {

	@Override
	public void update(String name) {
		Unit.focused.update(name);
	}
	
}
