package com.josephs_projects.erovra2.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.josephs_projects.apricotLibrary.Apricot;
import com.josephs_projects.apricotLibrary.interfaces.Tickable;
import com.josephs_projects.erovra2.Erovra2;
import com.josephs_projects.erovra2.Terrain;
import com.josephs_projects.erovra2.ai.GeneticAI;

class Test {
	public static boolean DQ;
	Terrain terrain;
	GeneticAI control = new GeneticAI();
	GeneticAI bestAI;
	double bestScore = Double.POSITIVE_INFINITY;
	double coinsOffset = 0;
	boolean firstGo = true;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@org.junit.jupiter.api.Test
	void test() {
		System.out.println("STARTING TOURNAMENT");
		int n = 72000;
		GeneticAI[] competitors = new GeneticAI[54];
		for (int i = 0; i < competitors.length; i++) {
			competitors[i] = new GeneticAI();
		}
		competitors = breed(competitors);
		for (int i = 0; i < n; i++) {
			System.out.println("Generation: " + i + " out of " + n);
			Map<Double, GeneticAI> winners = new HashMap<>();
			for (int j = 0; j < competitors.length; j++) {
				control = new GeneticAI();
				testTwoAI(winners, competitors[j], competitors[Apricot.rand.nextInt(competitors.length)]);
			}
			List<Double> times = new ArrayList<>(winners.keySet());
			times.sort(new Comparator() {

				@Override
				public int compare(Object arg0, Object arg1) {
					return (int) ((double) arg0 - (double) arg1);
				}

			});
			competitors = breed(winners, times);
			System.out.println();
			if (times.get(0) < bestScore && !firstGo) {
				System.out.println("NEW RECORD!");
				bestScore = times.get(0);
				bestAI = winners.get(times.get(0));
			}
			firstGo = false;
			System.out.println("Best AI: " + bestAI);
			System.out.println("Generation's AI: " + winners.get(times.get(0)));
			System.out.println("Generation's best score: " + times.get(0));
			System.out.println();
			coinsOffset += 0.2;
		}
	}

	void testTwoAI(Map<Double, GeneticAI> map, GeneticAI one, GeneticAI two) {
		Erovra2.geneticTournament = true;
		System.out.print("Running test...");
		Erovra2.startTournament(terrain);
		terrain = Erovra2.terrain;
		Erovra2.world.add(new Ender());
		Erovra2.home.ai = one;
		Erovra2.enemy.ai = control;
		try {
			Erovra2.apricot.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Erovra2.apricot.frame.dispose();
		double i = 0;
		if (Erovra2.home.capital.health <= 0 || Erovra2.home.unitsLost == 0 || Erovra2.home.unitsMade == 0
				|| Erovra2.enemy.unitsLost == 0 || Erovra2.enemy.unitsMade == 0) {
			i = 1000000000;
			while (map.containsKey(i)) {
				i += 0.001;
			}
			map.put(i, one);
		} else {
			double score = Erovra2.apricot.ticks * Erovra2.home.unitsLost * Erovra2.enemy.unitsMade
					/ Erovra2.home.unitsMade * Erovra2.enemy.unitsLost;
			while (map.containsKey(score + i)) {
				i += 0.001;
			}
			map.put(score + i, one);
		}
	}

	GeneticAI[] breed(Map<Double, GeneticAI> winners, List<Double> times) {
		GeneticAI[] retval = new GeneticAI[54];
		for (int i = 0; i < 44; i++) {
			retval[i] = winners.get(times.get(i));
		}
		for (int i = 44; i < retval.length; i++) {
			retval[i] = new GeneticAI(mutate(retval[0].genes, retval[1].genes));
		}
		return retval;
	}

	GeneticAI[] breed(GeneticAI[] winners) {
		GeneticAI[] retval = new GeneticAI[54];
		for (int i = 0; i < retval.length; i++) {
			retval[i] = new GeneticAI(mutate(winners[i].genes, winners[i].genes));
		}
		return retval;
	}

	double[] mutate(double[] genes1, double[] genes2) {
		double[] retval = new double[genes1.length];
		boolean useGenes1 = true;
		for (int i = 0; i < retval.length; i++) {
			if (Math.random() * 16 < 3) {
				useGenes1 = !useGenes1;
			}
			if (useGenes1) {
				retval[i] = genes1[i] * ((Math.random() - 0.5) * 0.0025 + 1)
						+ (Math.random() - 0.5) * genes1[i] * 0.0025;
			} else {
				retval[i] = genes2[i] * ((Math.random() - 0.5) * 0.0025 + 1)
						+ (Math.random() - 0.5) * genes2[i] * 0.0025;
			}
			if (Math.random() < 0.000416) {
				retval[i] = Apricot.rand.nextInt() / 2500000.0;
			}
		}
		return retval;
	}
}

class Ender implements Tickable {

	@Override
	public void tick() {
		if (Erovra2.apricot.ticks > 1080000) {
			Erovra2.apricot.running = false;
			System.out.print("DQ");
			remove();
		}
	}

	@Override
	public void remove() {
		Erovra2.world.remove(this);
	}

}