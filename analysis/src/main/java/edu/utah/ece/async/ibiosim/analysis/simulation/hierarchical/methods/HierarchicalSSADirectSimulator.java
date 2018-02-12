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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods;

import java.io.IOException;
import java.util.PriorityQueue;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.io.HierarchicalWriter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.HierarchicalEventComparator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.TriggeredEventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalSSADirectSimulator extends HierarchicalSimulation
{
  private final boolean	print;

  private long			randomSeed;

  public HierarchicalSSADirectSimulator(AnalysisProperties properties) throws IOException, XMLStreamException, BioSimException
  {

    super(properties, SimType.HSSA);
    this.print = true;

  }
  
  public HierarchicalSSADirectSimulator(AnalysisProperties properties, boolean print) throws IOException, XMLStreamException, BioSimException
  {

    super(properties, SimType.HSSA);
    this.print = print;

  }


  @Override
  public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
  {
    if (!isInitialized)
    {
      SimulationProperties simProperties = properties.getSimulationProperties();
      currProgress = 0;
      
      setCurrentTime(simProperties.getInitialTime());
      ModelSetup.setupModels(this, ModelType.HSSA);
      computeFixedPoint();

      for(HierarchicalModel model : this.getListOfHierarchicalModels())
      {
        totalPropensity.addChild(model.getPropensity().getVariable());
      }
      totalPropensity.computeFunction(0);
      if (hasEvents)
      {
        triggeredEventList =
            new PriorityQueue<TriggeredEventNode>(1, new HierarchicalEventComparator());
        computeEvents();
      }

      setupForOutput(runNumber);
      isInitialized = true;
    }

  }

  /**
   * cancels the current run
   */
  @Override
  public void cancel()
  {
    setCancelFlag(true);
  }

  /**
   * clears data structures for new run
   */
  @Override
  public void clear()
  {

  }

  @Override
  public void setupForNewRun(int newRun) throws IOException
  {
    SimulationProperties simProperties = properties.getSimulationProperties();
    setCurrentTime(simProperties.getInitialTime());
    restoreInitialState();
    setupForOutput(newRun);
  }

  @Override
  public void simulate() throws IOException, XMLStreamException
  {

    SimulationProperties simProperties = properties.getSimulationProperties();
    double r1 = 0, r2 = 0, totalPropensity = 0, delta_t = 0, nextReactionTime = 0, previousTime = 0, nextEventTime = 0, nextMaxTime = 0;
    double timeLimit = simProperties.getTimeLimit();
    double maxTimeStep = simProperties.getMaxTimeStep();
    if (isSbmlHasErrorsFlag())
    {
      return;
    }

    if (!isInitialized)
    {
        this.initialize(randomSeed, 1);
    }
    printTime = simProperties.getOutputStartTime();
    previousTime = 0;
    
    while (currentTime.getState().getStateValue() < timeLimit && !isCancelFlag())
    {
      //			if (!HierarchicalUtilities.evaluateConstraints(constraintList))
      //			{
      //				return;
      //			}
      double currentTime = this.currentTime.getState().getStateValue();
      r1 = getRandomNumberGenerator().nextDouble();
      r2 = getRandomNumberGenerator().nextDouble();
      computePropensities();
      totalPropensity = getTotalPropensity();
      delta_t = computeNextTimeStep(r1, totalPropensity);
      nextReactionTime = currentTime + delta_t;
      nextEventTime = getNextEventTime();
      nextMaxTime = currentTime + maxTimeStep;
      previousTime = currentTime;

      if (nextReactionTime < nextEventTime && nextReactionTime < nextMaxTime)
      {
        setCurrentTime(nextReactionTime);
      }
      else if (nextEventTime <= nextMaxTime)
      {
        setCurrentTime(nextEventTime);
      }
      else
      {
        setCurrentTime(nextMaxTime);
      }

      if (currentTime > timeLimit)
      {
        break;
      }

      if (print)
      {
        printToFile();
      }

      if (currentTime == nextReactionTime)
      {
        update(true, false, false, r2, previousTime);
      }
      else if (currentTime == nextEventTime)
      {
        update(false, false, true, r2, previousTime);
      }
      else
      {
        update(false, true, false, r2, previousTime);
      }
    }

    if (!isCancelFlag())
    {
      setCurrentTime(timeLimit);
      update(false, true, true, r2, previousTime);

      if (print)
      {
        printToFile();
      }

    }
    
    closeWriter();
  }

  private void update(boolean reaction, boolean rateRule, boolean events, double r2, double previousTime)
  {
    if (reaction)
    {
      selectAndPerformReaction(r2);
    }

    if (rateRule)
    {
      fireRateRules(previousTime);
    }

    if (events)
    {
      computeEvents();
    }
    
    computeAssignmentRules();
    
  }
  
  private void computePropensities()
  {
    for(HierarchicalModel modelstate : this.getListOfHierarchicalModels())
    {
      for(ReactionNode node : modelstate.getReactions())
      {
        node.computePropensity(modelstate.getIndex());
      }
      modelstate.getPropensity().computeFunction(modelstate.getIndex());
    }
    
    this.totalPropensity.computeFunction(0);
  }

  private double computeNextTimeStep(double r1, double totalPropensity)
  {
    return Math.log(1 / r1) / totalPropensity;
  }

  
  private void fireRateRules(double previousTime)
  {

  }

  private void selectAndPerformReaction(double r2)
  {
    double sum = 0;
    double threshold = getTotalPropensity() * r2;
    for(HierarchicalModel model : this.getListOfHierarchicalModels())
    {
      for(ReactionNode node : model.getReactions())
      {
        sum += node.getState().getState(model.getIndex()).getStateValue();

        if(sum >= threshold)
        {
          node.fireReaction(model.getIndex(), sum - threshold);
          return;
        }
      }
    }

  }

  @Override
  public void printStatisticsTSD()
  {

  }

  private double getNextEventTime()
  {
    checkEvents();
    if (triggeredEventList != null && !triggeredEventList.isEmpty())
    {
      return triggeredEventList.peek().getFireTime();
    }
    return Double.POSITIVE_INFINITY;
  }
}
