/**
 * This class contains eperimental results.
 */

package gcm2sbml.util;

import java.util.HashMap;

public class ExperimentResult {

	public ExperimentResult(HashMap<String, double[]> results) {
		this.results = results;
		timeValue = results.get(timeString);
	}
	
	public ExperimentResult(String tsdFile) {
		this(Utility.readFile(tsdFile));
	}
	
	public double getValue(String species, double timePoint) {
		double timeIndex = interpolateIndex(timeValue, timePoint);
		return interpolateValue(results.get(species), timeIndex);
	}

	private double interpolateIndex(double[] times, double timePoint) {
		for (int i = 0; i < times.length - 1; i++) {
			if (times[i] < timePoint && times[i + 1] >= timePoint) {
				return (timePoint - times[i + 1]) / (times[i] - times[i + 1])
						+ i + 1;
			}
		}
		return -1;
	}

	private double interpolateValue(double[] values, double index) {
		int prev = (int) Math.floor(index);
		if (Math.abs(prev - index) < 1e-10) {
			return values[prev];
		}
		return (values[prev + 1] - values[prev]) * (index - prev)
				+ values[prev];
	}

	private HashMap<String, double[]> results = null;
	private double[] timeValue = null;
	private String timeString = "time";
}
