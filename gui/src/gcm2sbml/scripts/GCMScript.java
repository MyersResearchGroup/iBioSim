package gcm2sbml.scripts;

import gcm2sbml.util.Utility;

import java.io.File;
import java.util.HashMap;

public class GCMScript {

	public void generateThresholds(String directory, String species,
			double bestTime, String type, int num) {
		double[] highs = new double[3 * num];
		double[] lows = new double[3 * num];

		for (int i = 1; i <= num; i++) {
			for (int j = 0; j < 3; j++) {
				double[] results = generateThreshold(directory + i, gate[j],
						species, bestTime);
				highs[3 * (i - 1) + j] = results[1];
				lows[3 * (i - 1) + j] = results[0];

			}
		}
		System.out.println("\nHigh:");
		for (int i = 0; i < highs.length; i++) {
			System.out.print(highs[i] + " ");
		}
		System.out.println("\nLow:");
		for (int i = 0; i < lows.length; i++) {
			System.out.print(lows[i] + " ");
		}
	}

	public double[] generateThreshold(String directory, String type,
			String species, double bestTime) {
		HashMap<String, double[]> results = null;
		double[] timeValues = null;
		double high = 0;
		double low = 0;
		int index = -1;
		results = Utility.calculateAverage(directory + File.separator + type
				+ experiment[0]);

		timeValues = results.get("time");
		for (int i = 0; i < timeValues.length; i++) {
			if (timeValues[i] > bestTime) {
				index = i - 1;
				break;
			}
		}

		low = results.get(species)[index];
		results = Utility.calculateAverage(directory + File.separator + type
				+ experiment[1]);
		high = results.get(species)[index];

		double range = (high - low) / 3.;
		return new double[] { low + range, high - range };
	}

	private String[] kind = { "coop", "rep", "promoter" };
	private String[] gate = { "maj", "tog", "si" };
	private String[] experiment = { "-h-high", "-h-low", "-l-high", "-l-low" };
	private static final String directory = "/home/shang/namphuon/muller";
}
