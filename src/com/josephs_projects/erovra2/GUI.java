package com.josephs_projects.erovra2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.erovra2.gui.MessageContainer;
import com.josephs_projects.erovra2.units.Unit;

public class GUI implements Renderable {
	Nation nation;
	Font bigFont = new Font("Arial", Font.PLAIN, 18);
	BufferedImage coin;
	BufferedImage population;
	BufferedImage bomb;
	public MessageContainer messageContainer = new MessageContainer(new Tuple());
	public static int dashboardHeight = 201;

	public GUI(Nation nation) {
		this.nation = nation;
		Erovra2.world.add(this);
		try {
			coin = Apricot.image.loadImage("/res/coin.png");
			population = Apricot.image.loadImage("/res/population.png");
			bomb = Apricot.image.loadImage("/res/bomb.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render(Graphics2D g) {
		if (nation != Erovra2.home)
			return;

		if (Erovra2.net != null) {
			if (Erovra2.net.ping < 32) {
				g.setColor(Color.green);
			} else if (Erovra2.net.ping < 64) {
				g.setColor(Color.yellow);
			} else {
				g.setColor(Color.red);
			}
			g.drawString("Ping: " + String.valueOf((int) Erovra2.net.ping), 768 - 60, 28);
		}
		messageContainer.position.x = Erovra2.apricot.width() - 240;
		messageContainer.position.y = Erovra2.apricot.height() - dashboardHeight - 5;

		// Draw minimap
		g.setColor(Erovra2.colorScheme.borderColor);
		g.fillRect(0, Erovra2.apricot.height() - Erovra2.terrain.minimap.getHeight() - 2,
				Erovra2.terrain.minimap.getHeight() + 2, Erovra2.terrain.minimap.getHeight() + 2);
		g.drawImage(Erovra2.terrain.minimap, 0, Erovra2.apricot.height() - Erovra2.terrain.minimap.getHeight(), null);
		g.drawImage(Erovra2.terrain.oremap, 0, Erovra2.apricot.height() - Erovra2.terrain.minimap.getHeight(), null);

		// Draw friendly minimap units
		List<Unit> units = new ArrayList<Unit>(nation.units.values());
		g.setColor(nation.color);
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			int x = (int) (unit.position.x / (Erovra2.size * 64.0) * Erovra2.terrain.minimap.getHeight());
			int y = (int) (unit.position.y / (Erovra2.size * 64.0) * Erovra2.terrain.minimap.getHeight()
					- Erovra2.terrain.minimap.getHeight()) + Erovra2.apricot.height();
			g.fillRect(x - 3, y - 3, 6, 6);
		}

		// Draw enemy minimap units
		units = new ArrayList<Unit>(nation.enemyNation.units.values());
		g.setColor(nation.enemyNation.color);
		for (int i = 0; i < units.size(); i++) {
			Unit unit = units.get(i);
			if (unit.engagedTicks <= 0)
				continue;
			int x = (int) (unit.position.x / (Erovra2.size * 64.0) * Erovra2.terrain.minimap.getHeight());
			int y = (int) (unit.position.y / (Erovra2.size * 64.0) * Erovra2.terrain.minimap.getHeight()
					- Erovra2.terrain.minimap.getHeight()) + Erovra2.apricot.height();
			g.fillRect(x - 3, y - 3, 6, 6);
		}
		drawNationInfo(g);
		if (Unit.focused != null) {
			drawDashboard(g);
		}
	}

	private void drawNationInfo(Graphics2D g) {
		// Draw nation info rectangle
		g.setColor(Erovra2.colorScheme.backgroundColor);
		g.fillRect(0, 0, 140, 88);

		// Set font stuff
		g.setFont(new Font("Trebuchet", Font.PLAIN, 18));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setColor(Color.white);

		// Draw coin
		g.drawString(String.valueOf(nation.coins), 36, 25);
		g.drawImage(coin, 8, 8, null);

		// Draw population
		if (nation.mobilized >= nation.population) {
			g.setColor(Erovra2.colorScheme.errorColor);
		}
		g.drawString(addSuffix(nation.mobilized) + " / " + addSuffix(nation.population), 36, 52);
		g.drawImage(population, 8, 35, null);

		// Draw bombs
		g.drawString(String.valueOf(nation.bombs), 36, 79);
		g.drawImage(bomb, 8, 64, null);
	}

	private void drawDashboard(Graphics2D g) {
		g.setColor(Erovra2.colorScheme.borderColor);
		g.fillRect(Erovra2.terrain.minimap.getWidth(), Erovra2.apricot.height() - dashboardHeight - 2,
				Erovra2.apricot.width() - Erovra2.terrain.minimap.getWidth(), 2);
		g.setColor(Erovra2.colorScheme.backgroundColor);
		g.fillRect(Erovra2.terrain.minimap.getWidth() + 2, Erovra2.apricot.height() - dashboardHeight,
				Erovra2.apricot.width() - Erovra2.terrain.minimap.getWidth() - 2, dashboardHeight);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.GUI_LEVEL;
	}

	@Override
	public void remove() {

	}

	public String addSuffix(int population) {
		int i = 0;
		while (population >= 1000) {
			population /= 1000;
			i++;
		}
		switch (i) {
		case 0:
			return String.valueOf(population) + "k";
		case 1:
			return String.valueOf(population) + "m";
		case 2:
			return String.valueOf(population) + "b";
		case 3:
			return String.valueOf(population) + "t";
		default:
			return String.valueOf(population);
		}
	}

}
