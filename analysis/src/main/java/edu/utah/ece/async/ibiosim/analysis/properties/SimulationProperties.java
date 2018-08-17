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
package edu.utah.ece.async.ibiosim.analysis.properties;

import java.util.ArrayList;
import java.util.List;

import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * The simulation properties contains information associated with the simulation options.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public final class SimulationProperties extends CoreObservable {

	private int numSteps, run, startIndex;
	private double initialTime, outputStartTime, minTimeStep, maxTimeStep, printInterval, timeLimit, absError, relError;
	private String printer_id, printer_track_quantity, genStats;
	private long rndSeed;
	private List<String> intSpecies;

	SimulationProperties() {
		run = 1;
		initialTime = 0;
		outputStartTime = 0;
		maxTimeStep = Double.POSITIVE_INFINITY;
		minTimeStep = 0;
		printInterval = 1;
		absError = 1e-9;
		relError = 1e-9;
		rndSeed = 314159;
		printer_id = "tsd.printer";
		printer_track_quantity = "amount";
		genStats = "false";
		timeLimit = 100;
		intSpecies = new ArrayList<>();
		startIndex = 1;

	}

	/**
	 * Getter for absolute error.
	 *
	 * @return the absolute error.
	 */
	public double getAbsError() {
		return absError;
	}

	/**
	 * Getter for initial simulation time.
	 *
	 * @return the initial time.
	 */
	public double getInitialTime() {
		return initialTime;
	}

	/**
	 * Getter for minimum time step.
	 *
	 * @return the minimum time step.
	 */
	public double getMinTimeStep() {
		return minTimeStep;
	}

	/**
	 * Getter for maximum time step.
	 *
	 * @return the maximum time step.
	 */
	public double getMaxTimeStep() {
		return maxTimeStep;
	}

	/**
	 * Getter for output start time.
	 *
	 * @return the output start time.
	 */
	public double getOutputStartTime() {
		return outputStartTime;
	}

	/**
	 * Getter for printer id.
	 *
	 * @return the printer id
	 */
	public String getPrinter_id() {
		return printer_id;
	}

	/**
	 * Getter for printer track quantity (amount or concentration).
	 *
	 * @return the printer quantity type.
	 */
	public String getPrinter_track_quantity() {
		return printer_track_quantity;
	}

	/**
	 * Getter for print interval.
	 *
	 * @return the print interval
	 */
	public double getPrintInterval() {
		return printInterval;
	}

	/**
	 * Getter for relative error.
	 *
	 * @return the relative error.
	 */
	public double getRelError() {
		return relError;
	}

	/**
	 * Getter for random seed.
	 *
	 * @return the random seed.
	 */
	public long getRndSeed() {
		return rndSeed;
	}

	/**
	 * Getter for simulation time limit.
	 *
	 * @return the simulation time limit.
	 */
	public double getTimeLimit() {
		return timeLimit;
	}

	/**
	 * Getter for number of runs.
	 *
	 * @return the number of runs.
	 */
	public int getRun() {
		return run;
	}

	/**
	 * Setter for absolute error.
	 *
	 * @param absError
	 *          - a positive double corresponding to absolute error.
	 */
	public void setAbsError(double absError) {
		this.absError = absError;
	}

	/**
	 * Setter for initial simulation time.
	 *
	 * @param initialTime
	 *          - non-negative double corresponding to the initial time.
	 */
	public void setInitialTime(double initialTime) {
		this.initialTime = initialTime;
	}

	/**
	 * Setter for minimum time step.
	 *
	 * @param minTimeStep
	 *          - a positive double corresponding to the minimum time step.
	 */
	public void setMinTimeStep(double minTimeStep) {
		this.minTimeStep = minTimeStep;
	}

	/**
	 * Setter for maximum time step.
	 *
	 * @param maxTimeStep
	 *          - a positive double corresponding to the maximum time step.
	 */
	public void setMaxTimeStep(double maxTimeStep) {
		this.maxTimeStep = maxTimeStep;
	}

	/**
	 * Setter for output start time, the time when simulation starts reporting results.
	 *
	 * @param outputStartTime
	 *          - a non-negative double corresponding to the output start time.
	 */
	public void setOutputStartTime(double outputStartTime) {
		this.outputStartTime = outputStartTime;
	}

	/**
	 * Setter for printer id (tsd or null).
	 *
	 * @param printer_id
	 *          - the printer id.
	 */
	public void setPrinter_id(String printer_id) {
		this.printer_id = printer_id;
	}

	/**
	 * Setter for the track quantity.
	 *
	 * @param printer_track_quantity
	 *          - the printer track quantity (amount or concentration).
	 */
	public void setPrinter_track_quantity(String printer_track_quantity) {
		this.printer_track_quantity = printer_track_quantity;
	}

	/**
	 * Setter for the print interval.
	 *
	 * @param printInterval
	 *          - a positive double for the print interval.
	 */
	public void setPrintInterval(double printInterval) {
		this.printInterval = printInterval;
	}

	/**
	 * Setter for the relative error.
	 *
	 * @param relError
	 *          - a positive double for the relative error.
	 */
	public void setRelError(double relError) {
		this.relError = relError;
	}

	/**
	 * Setter for the random seed.
	 *
	 * @param rndSeed
	 *          - an arbitrary long to be used as the random seed.
	 */
	public void setRndSeed(long rndSeed) {
		this.rndSeed = rndSeed;
	}

	/**
	 * Setter for the number of runs.
	 *
	 * @param run
	 *          - a positive integer corresponding to the number of runs.
	 */
	public void setRun(int run) {
		this.run = run;
	}

	/**
	 * Setter for the simulation time limit.
	 *
	 * @param timeLimit
	 *          - a positive double corresponding to the time limit.
	 */
	public void setTimeLimit(double timeLimit) {
		this.timeLimit = timeLimit;
	}

	/**
	 * Getter for the number of steps.
	 *
	 * @return the number of steps.
	 */
	public int getNumSteps() {
		return numSteps;
	}

	/**
	 * Setter for the number of steps.
	 *
	 * @param numSteps
	 *          - a positive integer corresponding to the number of steps.
	 */
	public void setNumSteps(int numSteps) {
		this.numSteps = numSteps;
	}

	/**
	 * Add interesting species that needs to be tracked when reporting results.
	 */
	public void addIntSpecies(String species) {
		if (intSpecies == null) {
			intSpecies = new ArrayList<>();
		}
		intSpecies.add(species);
	}

	/**
	 * Getter for the list of species that need to have the results printed.
	 *
	 * @return the list of interesting species
	 */
	public List<String> getIntSpecies() {
		return intSpecies;
	}

	/**
	 * Getter for the generate statistics.
	 *
	 * @return the flag for generate statistics.
	 */
	public String getGenStats() {
		return genStats;
	}

	/**
	 * Setter for generate statistics.
	 *
	 * @param genStats
	 *          - the flag that indicates whether to print statistics or not.
	 */
	public void setGenStats(String genStats) {
		this.genStats = genStats;
	}

	/**
	 * Gets the start index.
	 *
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * Sets the start index.
	 *
	 * @param startIndex
	 *          - the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}
}
