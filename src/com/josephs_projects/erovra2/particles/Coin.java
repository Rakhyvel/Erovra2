package com.josephs_projects.erovra2.particles;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;

public class Coin implements Tickable, Renderable {
	Tuple position;
	Tuple velocity;
	BufferedImage image;
	Nation nation;
	double speed = 1;

	public Coin(Tuple position, Nation nation) {
		this.position = position;
		this.velocity = nation.capitalPoint.sub(position).normalize();
		Erovra2.world.add(this);
		try {
			image = Apricot.image.loadImage("/res/coin.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.nation = nation;
	}

	@Override
	public void tick() {
		if (speed < 6)
			speed *= 1.1;
		position.inc(velocity.scalar(speed));
		if (position.cabDist(nation.capitalPoint) < 6) {
			nation.coins++;
			Erovra2.world.remove(this);
		}
	}

	@Override
	public void render(Graphics2D g) {
		if (nation != Erovra2.home)
			return;
		g.drawRenderedImage(image, getAffineTransform(image));
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.AIR_LEVEL;
	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
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
		scaleCenter.scale(0.5 * (Erovra2.zoom + 0.5), 0.5 * (Erovra2.zoom + 0.5));
		scaleCenter.translate(-image.getWidth() / 2, -image.getHeight() / 2);

		align.concatenate(scaleCenter);
		return align;
	}
}
