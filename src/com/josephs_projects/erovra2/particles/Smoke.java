package com.josephs_projects.erovra2.particles;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.interfaces.Renderable;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.Erovra2;

public class Smoke implements Tickable, Renderable {
	Tuple position;
	int life = 0;
	BufferedImage image;
	double direction;
	double derivative;

	public Smoke(Tuple position) {
		this.position = new Tuple(position);
		Erovra2.world.add(this);
		try {
			image = Apricot.image.loadImage("/res/smoke.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		direction = Math.random() * Math.PI * 2;
		derivative = Math.random() * 0.05 - 0.025;
	}

	@Override
	public void tick() {
		life++;
		direction += derivative;
		if (life > 200)
			remove();
	}

	@Override
	public void render(Graphics2D g) {
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				(float)-Math.pow((life - 100) / 200.0f, 2) + (200 - life) / 400.0f + 0.25f));
		g.drawImage(image, getAffineTransform(image), null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
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
		scaleCenter.rotate(direction);
		scaleCenter.scale(Erovra2.zoom * ((life + 100) / 150.0), Erovra2.zoom * ((life + 100) / 150.0));
		scaleCenter.translate(-image.getWidth() / 2, -image.getHeight() / 2);

		align.concatenate(scaleCenter);
		return align;
	}
}
