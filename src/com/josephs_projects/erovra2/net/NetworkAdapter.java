package com.josephs_projects.erovra2.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.projectiles.Projectile;
import com.josephs_projects.erovra2.projectiles.ProjectileType;
import com.josephs_projects.erovra2.projectiles.Shell;
import com.josephs_projects.erovra2.units.Unit;
import com.josephs_projects.erovra2.units.UnitType;

public abstract class NetworkAdapter extends Thread {
	protected ObjectOutputStream out;
	protected ObjectInputStream in;
	protected Socket socket;
	public Queue<Operation> opQ = new LinkedList<>();

	private double lastMs;
	private double accumulator;
	private double number;
	public double ping;

	// Use a separate map for updating positions because positions can be updated
	// between pings. Using a map insures that the receiver only has to update to
	// the true, current position, and not several intermediate positions.
	public Map<Integer, Unit> updatedPositions = new HashMap<>();

	@Override
	public void run() {
		try {

			while (Erovra2.apricot.isAlive()) {
				try {
					send();
					receive();
				} catch (StreamCorruptedException e) {
					System.out.println(
							"Stream corrupted when reading unit, most likely sender had ConcurrentModification exception while sending. Disregarding pong.");
				} catch (SocketTimeoutException e) {
					System.out.println(
							"Socket read time out exception, probably just that the interent is slow right now. Recovering.");
				} catch (SocketException e) {
					System.out.println("Ughhhhh");
				}
			}
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void send() throws IOException {
		try {
			while (!opQ.isEmpty()) {
				try {
					out.writeObject(opQ.remove());
					out.flush();
					out.reset();
				} catch (NoSuchElementException e) {
					System.out.println("No such element.");
				}
			}
			List<Integer> updatedPositionsList = new ArrayList<>(updatedPositions.keySet());
			for (int i2 = 0; i2 < updatedPositionsList.size(); i2++) {
				Integer i = updatedPositionsList.get(i2);
				if (updatedPositions.get(i) == null)
					continue;
				out.writeObject(new UpdateUnitPosition(i, updatedPositions.get(i)));
				updatedPositions.remove(i);
				out.flush();
				out.reset();
			}
			out.writeObject(null);
		} catch (ConcurrentModificationException e) {
			System.out.println("Network Adapter: Concurrent Modification");
		}
	}

	protected void receive() throws SocketException, IOException, ClassNotFoundException {
		Object o;
		while ((o = in.readObject()) != null) {
			if (o instanceof AddUnit) {
				AddUnit add = (AddUnit) o;
				Unit.makeUnit(add.position, Erovra2.enemy, add.type, add.id);
				System.out.println("Adding a unit at " + add.position + " with id " + add.id);
			} else if (o instanceof RemoveUnit) {
				RemoveUnit remove = (RemoveUnit) o;
				try {
					Erovra2.enemy.units.get(remove.id).removeReceiver();
				} catch (NullPointerException e) {
					System.out.println("Null pointer exception in unit remove.");
				}
			} else if (o instanceof UpdateUnitPosition) {
				UpdateUnitPosition update = (UpdateUnitPosition) o;
				try {
					Erovra2.enemy.units.get(update.id).position = update.position;
					Erovra2.enemy.units.get(update.id).setTargetReceiving(update.target);
					Erovra2.enemy.units.get(update.id).velocity = update.velocity;
					Erovra2.enemy.units.get(update.id).direction = update.direction;
					Erovra2.enemy.units.get(update.id).a = update.a;
				} catch (NullPointerException e) {
					System.out.println("Null pointer exception, misunderstood id " + update.id);
				}
			} else if (o instanceof KillUnit) {
				KillUnit kill = (KillUnit) o;
				try {
					Erovra2.enemy.units.get(kill.id).dead = true;
				} catch (NullPointerException e) {
					System.out.println("Null pointer exception in killUnit.");
				}
			} else if (o instanceof EngageTickUnit) {
				EngageTickUnit engage = (EngageTickUnit) o;
				try {
					Erovra2.enemy.units.get(engage.id).setEngagedTicksReceiver();
				} catch (NullPointerException e) {
					System.out.println("Null pointer exception in engage tick. " + engage.id);
				}
			} else if (o instanceof EngageUnit) {
				EngageUnit engage = (EngageUnit) o;
				try {
					Erovra2.enemy.units.get(engage.id).engaged = engage.engaged;
				} catch (NullPointerException e) {
					System.out.println("Null pointer exception in engage.");
				}
			} else if (o instanceof HitUnit) {
				HitUnit engage = (HitUnit) o;
				try {
					Erovra2.enemy.units.get(engage.id).hitTimer = 18;
				} catch (NullPointerException e) {
					System.out.println("Null pointer exception in hit. " + engage.id);
				}
			} else if (o instanceof AddProjectile) {
				AddProjectile add = (AddProjectile) o;
				Projectile.makeProjectile(add.position, add.velocity, Erovra2.enemy, add.attack, add.type, add.id);
			} else if (o instanceof RemoveProjectile) {
				RemoveProjectile remove = (RemoveProjectile) o;
				try {
					Erovra2.home.projectiles.get(remove.id).removeReceiver();
					Erovra2.enemy.projectiles.get(remove.id).removeReceiver();
				} catch (NullPointerException e) {
//					System.out.println("Null pointer exception in projectile remove. " + remove.id + " " + remove.type);
				}
			}
		}
		accumulator += (System.currentTimeMillis() - lastMs);
		number++;
		if (accumulator > 500) {
			ping = accumulator / number;
			accumulator = 0;
			number = 1;
		}
		lastMs = System.currentTimeMillis();
	}

	public static class Operation implements Serializable {
		private static final long serialVersionUID = -8730318381754096269L;
	}

	/**
	 * Used to tell the other nation to add a Unit to their dumby enemy nation.
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class AddUnit extends Operation {
		private static final long serialVersionUID = -3093478262075363671L;
		public int id;
		public UnitType type;
		public Tuple position;

		public AddUnit(Unit unit) {
			this.id = unit.id;
			this.type = unit.type;
			this.position = unit.position;
		}
	}

	/**
	 * Used to tell the other nation to remove a unit from their dumby nation
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class RemoveUnit extends Operation {
		private static final long serialVersionUID = -4165499733737423860L;
		public int id;

		public RemoveUnit(Unit unit) {
			this.id = unit.id;
		}
	}

	/**
	 * Used to tell the other nation that the target of a unit has changed. The
	 * other nation should update the given units position, target, direction, and
	 * velocity.
	 * 
	 * @author josep
	 *
	 */
	public static class UpdateUnitPosition extends Operation {
		private static final long serialVersionUID = -7029418470059022430L;
		public int id;
		public Tuple position;
		public Tuple target;
		public Tuple velocity;
		public double direction;
		public double a;

		public UpdateUnitPosition(int id, Unit unit) {
			this.id = id;
			this.position = unit.position;
			this.target = unit.getTarget();
			this.velocity = unit.velocity;
			this.direction = unit.direction;
			this.a = unit.a;
		}
	}

	/**
	 * Used to tell the other nation to mark a unit as dead in their dumby nation.
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class KillUnit extends Operation {
		private static final long serialVersionUID = 9210976757202940610L;
		public int id;

		public KillUnit(int id) {
			this.id = id;
		}
	}

	/**
	 * Used to tell the other nation that they should set the given unit to be
	 * engaged in their dumby nation.
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class EngageTickUnit extends Operation {
		private static final long serialVersionUID = 4490006989240184035L;
		public int id;

		public EngageTickUnit(int id) {
			this.id = id;
		}
	}

	/**
	 * Used to let the other nation know that a given unit is set to engaged, and to
	 * mark it as such in the dumby nation.
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class EngageUnit extends Operation {
		private static final long serialVersionUID = 4490006989240184035L;
		public int id;
		public boolean engaged;

		public EngageUnit(int id, boolean engaged) {
			this.id = id;
			this.engaged = engaged;
		}
	}

	/**
	 * Used to tell the other nation that a given unit has been hit, and to update
	 * its HitTimer in the dumby nation.
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class HitUnit extends Operation {
		private static final long serialVersionUID = 6559581622393429507L;
		public int id;

		public HitUnit(int id) {
			this.id = id;
		}
	}

	/**
	 * Lets other nation know to add a given projectile to their dumby nations list
	 * of projectiles.
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class AddProjectile extends Operation {
		private static final long serialVersionUID = -7848618761407580176L;
		public int id;
		public ProjectileType type;
		public Tuple position;
		public Tuple velocity;
		public double attack;

		public AddProjectile(Projectile projectile) {
			this.id = projectile.id;
			this.type = projectile.type;
			this.position = projectile.position;
			if (type == ProjectileType.SHELL) {
				this.velocity = ((Shell) projectile).target;
			} else {
				this.velocity = projectile.velocity;
			}
			this.attack = projectile.attack;
		}
	}

	/**
	 * Lets the other nation know to remove a given projectile from their dumby
	 * nation's list of projectiles. May already be removed by the time message is
	 * received.
	 * 
	 * @author Joseph Shimel
	 *
	 */
	public static class RemoveProjectile extends Operation {
		private static final long serialVersionUID = -5084003283014069110L;
		public int id;
		public ProjectileType type;

		public RemoveProjectile(Projectile projectile) {
			this.id = projectile.id;
			this.type = projectile.type;
		}
	}

}
