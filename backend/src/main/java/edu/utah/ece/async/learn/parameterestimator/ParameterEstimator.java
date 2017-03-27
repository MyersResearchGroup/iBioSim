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
package main.java.edu.utah.ece.async.learn.parameterestimator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;

import main.java.edu.utah.ece.async.util.exceptions.BioSimException;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.methods.HierarchicalODERKSimulator;
import main.java.edu.utah.ece.async.learn.genenet.Experiments;
import main.java.edu.utah.ece.async.learn.genenet.SpeciesCollection;
import main.java.edu.utah.ece.async.learn.parameterestimator.methods.sres.EvolutionMethodSetting;
import main.java.edu.utah.ece.async.learn.parameterestimator.methods.sres.Modelsettings;
import main.java.edu.utah.ece.async.learn.parameterestimator.methods.sres.ObjectiveSqureError;
import main.java.edu.utah.ece.async.learn.parameterestimator.methods.sres.SRES;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ParameterEstimator
{
	static double				relativeError		= 1e-6;
	static double				absoluteError		= 1e-9;
	static int					numSteps;
	static double				maxTimeStep			= Double.POSITIVE_INFINITY;
	static double				minTimeStep			= 0.0;
	static long					randomSeed			= 0;
	static int					runs				= 1;
	static double				stoichAmpValue		= 1.0;
	static boolean				genStats			= false;
	static String				selectedSimulator	= "";
	static ArrayList<String>	interestingSpecies	= new ArrayList<String>();
	static String				quantityType		= "amount";

	public static SBMLDocument estimate(String SBMLFileName, String root, List<String> parameterList, Experiments experiments, SpeciesCollection speciesCollection) throws IOException, XMLStreamException, BioSimException
	{

		int numberofparameters = parameterList.size();
		int sp = 0;
		int n = experiments.getExperiments().get(0).size() - 1;
		double ep = experiments.getExperiments().get(0).get(n).get(0);
		double[] lowerbounds = new double[numberofparameters];
		double[] upperbounds = new double[numberofparameters];
		HierarchicalSimulation sim = new HierarchicalODERKSimulator(SBMLFileName, root, 0);
		sim.initialize(randomSeed, 0);

		for (int i = 0; i < numberofparameters; i++)
		{
			lowerbounds[i] = sim.getTopLevelValue(parameterList.get(i)) / 100;
			upperbounds[i] = sim.getTopLevelValue(parameterList.get(i)) * 100;
		}
		Modelsettings M1 = new Modelsettings(experiments.getExperiments().get(0).get(0), speciesCollection.size(), sp, (int) ep, lowerbounds, upperbounds, false);
		// Objective objective1 = new ObjectiveSqureError(M1,0.1);

		EvolutionMethodSetting EMS = new EvolutionMethodSetting();
		ObjectiveSqureError TP = new ObjectiveSqureError(sim, experiments, parameterList, speciesCollection, M1, 0.1);

		SRES sres = new SRES(TP, EMS);
		// System.out.println("test");
		SRES.Solution solution = sres.run(200).getBestSolution();

		// TODO: report results: take average of error
		// TODO: weight mean square error. Add small value
		System.out.println(solution.toString());
		// TODO: copy best parameter to a new SBML document
		return null;
	}

}
