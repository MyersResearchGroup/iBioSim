/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
/**
 * This class contains eperimental results.
 */

package edu.utah.ece.async.ibiosim.dataModels.biomodel.util;

import java.util.HashMap;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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

	/**
	 * Returns the time where the species has a certain value
	 * 
	 * @param species
	 *            the species of interest
	 * @param valuePoint
	 *            the value we are looking for
	 * @param timePoint
	 *            the time period to search from
	 * @return the time what which the species takes the value
	 */
	public double getTime(String species, double valuePoint, double timePoint) {
		//double time = timePoint;
		int index = (int) Math.floor(interpolateIndex(timeValue, timePoint));
		double[] speciesValues = results.get(species);
		for (; index < timeValue.length; index++) {
			if (index == 0) {
			} else if ((speciesValues[index - 1] <= valuePoint && speciesValues[index] >= valuePoint)
					|| (speciesValues[index - 1] >= valuePoint && speciesValues[index] <= valuePoint)) {
				break;
			
			} else if (index == timeValue.length - 1) {
				index = -1;
				break;
			}
		}
		
		if (index == -1) {
			return -1;
		}
		
		double speciesIndex = interpolateIndex(speciesValues, valuePoint, index-1);
		return interpolateValue(timeValue, speciesIndex);
	}

	public double[] getValues(String species) {
		return results.get(species);
	}

	private static double interpolateIndex(double[] times, double timePoint) {
		return interpolateIndex(times, timePoint, 0);
	}
	
	private static double interpolateIndex(double[] times, double timePoint, int index) {
		for (int i = index; i < times.length - 1; i++) {
			if (times[i] < timePoint && times[i + 1] >= timePoint) {
				return (timePoint - times[i + 1]) / (times[i] - times[i + 1])
						+ i + 1;
			}
			else if (times[i] >= timePoint && times[i + 1] < timePoint) {
				return (times[i + 1] - timePoint) / (times[i + 1] - times[i])
						+ i + 1;
			}
		}
		return -1;
	}
	

	private static double interpolateValue(double[] values, double index) {
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
