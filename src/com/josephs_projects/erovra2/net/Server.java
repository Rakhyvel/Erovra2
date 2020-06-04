package com.josephs_projects.erovra2.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.units.buildings.City;

public class Server extends NetworkAdapter {
	ServerSocket server;
	boolean running = true;

	public Server() {
		try {
			server = new ServerSocket(8801);
			System.out.println("IP address: " + InetAddress.getLocalHost().getHostAddress() + ":8801");

			this.socket = server.accept();
			System.out.println("Connection joined.");
			this.socket.setTcpNoDelay(true);

			out = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("Output stream created.");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("Starting handshake...");
		try {
			in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			System.out.println("Input stream created.");

			System.out.println("Sending terrain array...");
			out.writeObject(Erovra2.terrain.map);
			out.flush();

			System.out.println("Creating and sending nations...");
			Erovra2.startNewMatch();
			out.writeObject(Erovra2.enemy);
			out.flush();
			out.writeObject(Erovra2.home);
			out.flush();

			Erovra2.home.setCapital(new City(Erovra2.home.capitalPoint, Erovra2.home));
			Erovra2.terrain.setOffset(new Tuple(Erovra2.size / 2 * 64, Erovra2.size / 2 * 64));
			
			super.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Connection ended.");
	}
}
