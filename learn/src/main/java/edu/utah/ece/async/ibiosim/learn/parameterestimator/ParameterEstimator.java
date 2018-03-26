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
package edu.utah.ece.async.ibiosim.learn.parameterestimator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalODERKSimulator;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.learn.genenet.Experiments;
import edu.utah.ece.async.ibiosim.learn.genenet.SpeciesCollection;
import edu.utah.ece.async.ibiosim.learn.parameterestimator.methods.sres.EvolutionMethodSetting;
import edu.utah.ece.async.ibiosim.learn.parameterestimator.methods.sres.Modelsettings;
import edu.utah.ece.async.ibiosim.learn.parameterestimator.methods.sres.ObjectiveSqureError;
import edu.utah.ece.async.ibiosim.learn.parameterestimator.methods.sres.SRES;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ParameterEstimator
{

	/**
	 * This function is used to execute parameter estimation from a given SBML file. The input model serves
	 * as a template and the existing parameters in the model will set the bounds to which parameter estimation will use.
	 * <p>
	 * In addition, the SBML file is used for simulation when estimating the values of the parameters. 
	 * 
	 * @param SBMLFileName: the input SBML file
	 * @param root: the directory where the experimental data is located
	 * @param parameterList: the list of parameters that needs to have the value estimated.
	 * @param experiments: data object that holds the experimental data.
	 * @param speciesCollection: data object that holds the species in the model.
	 * @return A new SBMLDocument containing the new parameter values.
	 * @throws IOException - when a file cannot be read or written.
	 * @throws XMLStreamException - when an SBML file cannot be parsed.
	 * @throws BioSimException - when simulation encounters a problem.
	 */
	public static SBMLDocument estimate(String SBMLFileName, String root, List<String> parameterList, Experiments experiments, SpeciesCollection speciesCollection) throws IOException, XMLStreamException, BioSimException
	{
	  AnalysisProperties properties = new AnalysisProperties("", SBMLFileName, root, false);
		int numberofparameters = parameterList.size();
		int sp = 0;
		int n = experiments.getExperiments().get(0).size() - 1;
		double ep = experiments.getExperiments().get(0).get(n).get(0);
		double[] lowerbounds = new double[numberofparameters];
		double[] upperbounds = new double[numberofparameters];
		HierarchicalSimulation sim = new HierarchicalODERKSimulator(properties, false);

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
		SRES.Solution solution = sres.run(200).getBestSolution();

		// TODO: report results: take average of error
		// TODO: weight mean square error. Add small value
		SBMLDocument doc = SBMLReader.read(new File(SBMLFileName));
		Model model = doc.getModel();
		
		for(int i = 0; i < parameterList.size(); i++)
		{
		  Parameter parameter = model.getParameter(parameterList.get(i));
		  
		  if(parameter != null)
		  {
		    parameter.setValue(solution.getFeatures()[i]);
		  }
		}
		System.out.println(solution.toString());
		return doc;
	}

}
