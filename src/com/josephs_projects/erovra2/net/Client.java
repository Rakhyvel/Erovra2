package com.josephs_projects.erovra2.net;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.Terrain;
import com.josephs_projects.erovra2.ai.ControlAI;
import com.josephs_projects.erovra2.units.buildings.City;

public class Client extends NetworkAdapter {

	public Client(String address) {
		try {
			System.out.println("Attempting to join server...");
			socket = new Socket(address, 8801);

			System.out.println("Connection joined...");
			socket.setTcpNoDelay(true);

			out = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Output stream created...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Starting handshake...");
		try {
			in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			System.out.println("Input stream created...");

			Erovra2.terrain = new Terrain((float[][]) in.readObject());
			System.out.println("Terrain array received.");

			Erovra2.world.add(Erovra2.home = (Nation) in.readObject());
			System.out.println(Erovra2.home.name);
			Erovra2.home.ai = new ControlAI();
			Erovra2.home.init();
			Erovra2.world.add(Erovra2.enemy = (Nation) in.readObject());
			System.out.println(Erovra2.enemy.name);
			Erovra2.enemy.init();
			System.out.println("Nations received successfully.");

			Erovra2.setNationColors();
			Erovra2.home.enemyNation = Erovra2.enemy;
			Erovra2.enemy.enemyNation = Erovra2.home;

			Erovra2.home.setCapital(new City(Erovra2.home.capitalPoint, Erovra2.home));
			Erovra2.terrain.setOffset(new Tuple(Erovra2.size / 2 * 64, Erovra2.size / 2 * 64));

			super.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Connection ended.");
	}
}
