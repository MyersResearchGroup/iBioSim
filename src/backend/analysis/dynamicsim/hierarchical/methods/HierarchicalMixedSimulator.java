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
package backend.analysis.dynamicsim.hierarchical.methods;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import dataModels.util.exceptions.BioSimException;
import backend.analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.states.VectorWrapper;
import backend.analysis.dynamicsim.hierarchical.util.setup.ModelSetup;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public final class HierarchicalMixedSimulator extends HierarchicalSimulation
{

	private HierarchicalFBASimulator	fbaSim;
	private HierarchicalODERKSimulator	odeSim;
	private VectorWrapper wrapper;
	// private HierarchicalSimulation ssaSim;

	public HierarchicalMixedSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running,
			String[] interestingSpecies, String quantityType, String abstraction, double initialTime, double outputStartTime) throws IOException, XMLStreamException, BioSimException
	{
		super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, progress, printInterval, stoichAmpValue, running, interestingSpecies, quantityType, abstraction, initialTime, outputStartTime, SimType.MIXED);

	}

	@Override
	public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
	{
		if (!isInitialized)
		{
			setCurrentTime(0);
			this.wrapper = new VectorWrapper(initValues); 

			
			ModelSetup.setupModels(this, ModelType.HODE, wrapper);
			computeFixedPoint();


      setupForOutput(runNumber);
			isInitialized = true;
		}

	}

	@Override
	public void simulate()
	{

		if (!isInitialized)
		{
			try
			{
				initialize(0, getCurrentRun());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (XMLStreamException e)
			{
				e.printStackTrace();
			}
		}
		double nextEndTime = currentTime.getValue(0);
		while (currentTime.getValue() < timeLimit)
		{
			nextEndTime = currentTime.getValue() + getMaxTimeStep();

			if (nextEndTime > printTime)
			{
				nextEndTime = printTime;
			}

			if (nextEndTime > getTimeLimit())
			{
				nextEndTime = getTimeLimit();
			}

			odeSim.setTimeLimit(nextEndTime);
			fbaSim.simulate();

			computeAssignmentRules();

			odeSim.simulate();

			currentTime.setValue(nextEndTime);

			printToFile();
		}
	}

	@Override
	public void cancel()
	{
	}

	@Override
	public void clear()
	{
	}

	@Override
	public void setupForNewRun(int newRun)
	{
	}

	public void createODESim(HierarchicalModel topmodel, List<HierarchicalModel> odeModels) throws IOException, XMLStreamException
	{
			odeSim = new HierarchicalODERKSimulator(this, topmodel);
			odeSim.setListOfHierarchicalModels(odeModels);
	}

	public void createSSASim(HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels)
	{
		// TODO:
	}

	public void createFBASim(HierarchicalModel topmodel, Model model)
	{
		fbaSim = new HierarchicalFBASimulator(this, topmodel);
		fbaSim.setFBA(model);
	}

	@Override
	public void printStatisticsTSD()
	{
		// TODO Auto-generated method stub

	}
	
	VectorWrapper getVectorWrapper()
	{
	  return this.wrapper;
	}

}
