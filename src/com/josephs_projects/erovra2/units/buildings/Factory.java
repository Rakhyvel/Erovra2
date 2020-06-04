package com.josephs_projects.erovra2.units.buildings;

import com.josephs_projects.apricotLibrary.Tuple;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Nation;
import com.josephs_projects.erovra2.units.UnitType;
import com.josephs_projects.erovra2.units.ground.Artillery;
import com.josephs_projects.erovra2.units.ground.Cavalry;

public class Factory extends Building {
	private int workTimer = 0;
	public UnitType order = null;

	public Factory(Tuple position, Nation nation) {
		super(position, nation, UnitType.FACTORY);
	}

	public Factory(Tuple position, Nation nation, int id) {
		super(position, nation, UnitType.FACTORY, id);
	}

	@Override
	public void tick() {
		if ((Erovra2.net == null || nation == Erovra2.home) && workTimer == 0) {
			if (order == UnitType.ARTILLERY) {
				new Artillery(position, nation);
			} else if (order == UnitType.CAVALRY) {
				new Cavalry(position, nation);
			}
			order = null;
		}
		workTimer--;

		super.tick();
	}

	public void startProduction(UnitType order) {
		if (nation.population < nation.countFightingUnits())
			return;
		if (nation.coins < 15)
			return;
		this.order = order;
		workTimer = 6000;
		nation.coins -= 15;
	}

	public boolean producing() {
		return workTimer >= 0;
	}
}
