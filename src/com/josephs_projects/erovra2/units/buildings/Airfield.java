package com.josephs_projects.erovra2.units.buildings;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.air.Attacker;
import com.josephs_projects.erovra2.units.air.Fighter;

public class Airfield extends Building {
	private int workTimer = 0;
	public UnitType order = null;

	public Airfield(Tuple position, Nation nation) {
		super(position, nation, UnitType.AIRFIELD);
	}

	public Airfield(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.AIRFIELD, id);
	}

	@Override
	public void tick() {
		if ((Erovra2.net == null || nation == Erovra2.home) && workTimer == 0 && order != null) {
			if (order == UnitType.FIGHTER) {
				new Fighter(position, nation);
			} else if (order == UnitType.ATTACKER) {
				new Attacker(position, nation);
			} // else if (order == UnitType.BOMBER) {
//				new Bomber(position, nation);
//			}
			order = null;
		}
		workTimer--;

		super.tick();
	}

	public void startProduction(UnitType order) {
		if (nation.population < nation.countFightingUnits())
			return;
		if (order == UnitType.FIGHTER) {
			if (nation.coins < 15)
				return;
			workTimer = 6000;
			nation.coins -= 15;
		}
		if (order == UnitType.ATTACKER) {
			if (nation.coins < 30)
				return;
			workTimer = 12000;
			nation.coins -= 30;
		}
		this.order = order;
	}

	public boolean producing() {
		return order != null;
	}

}
