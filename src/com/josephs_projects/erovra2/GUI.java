package com.josephs_projects.erovra2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.erovra2.units.Unit;

public class GUI implements Renderable {
	Nation nation;
	Font bigFont = new Font("Arial", Font.PLAIN, 24);
	Font smallFont = new Font("Arial", Font.PLAIN, 16);
	BufferedImage coin;

	public GUI(Nation nation) {
		this.nation = nation;
		Erovra2.world.add(this);
		try {
			coin = Apricot.image.loadImage("/res/coin.png");
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
		// Draw minimap
		g.setColor(Erovra2.colorScheme.borderColor);
		g.fillRect(0, Erovra2.apricot.height() - Erovra2.terrain.minimap.getHeight() - 2,
				Erovra2.terrain.minimap.getHeight() + 2, Erovra2.terrain.minimap.getHeight() + 2);
		g.drawImage(Erovra2.terrain.minimap, 0, Erovra2.apricot.height() - Erovra2.terrain.minimap.getHeight(), null);

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
		drawDashboard(g);
	}

	private void drawNationInfo(Graphics2D g) {
		g.setColor(Erovra2.colorScheme.backgroundColor);
		g.fillRect(0, 0, 100, 32);
		g.setColor(Erovra2.colorScheme.textColor);
		g.setFont(bigFont);
		g.drawString(String.valueOf(nation.coins), 30, 24);
		g.drawImage(coin, 5, 5, null);
	}

	private void drawDashboard(Graphics2D g) {
		g.setColor(Erovra2.colorScheme.borderColor);
		g.fillRect(Erovra2.terrain.minimap.getWidth(), Erovra2.apricot.height() - 152,
				Erovra2.apricot.width() - Erovra2.terrain.minimap.getWidth(), 2);
		g.setColor(Erovra2.colorScheme.backgroundColor);
		g.fillRect(Erovra2.terrain.minimap.getWidth() + 2, Erovra2.apricot.height() - 150,
				Erovra2.apricot.width() - Erovra2.terrain.minimap.getWidth() - 2, 150);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.GUI_LEVEL;
	}

	@Override
	public void remove() {

	}

}
