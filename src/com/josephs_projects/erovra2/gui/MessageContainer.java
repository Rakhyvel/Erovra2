package com.josephs_projects.erovra2.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.apricotLibrary.World;
import com.josephs_projects.apricotLibrary.gui.GUIObject;
import com.josephs_projects.apricotLibrary.input.InputEvent;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.Erovra2;

public class MessageContainer {
	List<Message> messages = new ArrayList<>();
	public Tuple position;

	public MessageContainer(Tuple position) {
		this.position = position;
	}

	public void addMessage(String message, Color color) {
		for (Message m : messages) {
			m.position.y -= m.height;
		}
		if (message.length() > 35) {
			int lastSpace = findLastSpace(message);
			messages.add(new Message(message.substring(0, lastSpace), color, new Tuple(position), this, Erovra2.apricot, Erovra2.world));
			addMessage(message.substring(lastSpace).trim(), color);
		} else {
			messages.add(new Message(message, color, new Tuple(position), this, Erovra2.apricot, Erovra2.world));
		}
	}
	
	private int findLastSpace(String message) {
		for(int i = message.length() - 1; i >= 0; i--) {
			if(message.charAt(i) == ' ') {
				return i;
			}
		}
		return -1;
	}
}

class Message extends GUIObject implements Tickable {
	String message;
	Color color;
	int height = 18;
	int width;
	double fade = 1;
	int birthTick;
	MessageContainer container;

	public Message(String message, Color color, Tuple position, MessageContainer container, Apricot apricot, World world) {
		super(position, apricot, world);
		this.message = message;
		this.color = color;
		this.container = container;
		birthTick = Erovra2.apricot.ticks;
		Erovra2.world.add(this);
	}

	@Override
	public void tick() {
		if (birthTick + 200 * 16.0 / Erovra2.dt < Erovra2.apricot.ticks) {
			fade -= 1.0 / 120.0 * Erovra2.dt / 16.0;
		}
		if (fade <= 0) {
			remove();
		}
	}

	@Override
	public void input(InputEvent arg0) {
	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
		container.messages.remove(this);
	}

	@Override
	public int getRenderOrder() {
		return Erovra2.GUI_LEVEL;
	}

	@Override
	public void render(Graphics2D g) {
		g.setFont(new Font("Arial", Font.PLAIN, 16));
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		// Draw background
		g.setColor(new Color(0, 0, 0, (int) (102 * fade)));
		g.fillRect((int) position.x - 3, (int) position.y - height + 3, Erovra2.apricot.width() - (int) position.x + 3,
				height);

		// Draw Text
		width = g.getFontMetrics(g.getFont()).stringWidth(message);
		g.setColor(new Color(0, 0, 0, (int) (70 * fade)));
		g.drawString(message, (int) position.x, (int) position.y + 1);
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * fade)));
		g.drawString(message, (int) position.x, (int) position.y);
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public int width() {
		return width;
	}
}
