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

  public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, double printInterval, double stoichAmpValue, 
    String[] interestingSpecies, String quantityType,  double initialTime, double outputStartTime) throws IOException, XMLStreamException, BioSimException
  {

    this(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit, maxTimeStep, minTimeStep, randomSeed, printInterval, stoichAmpValue, interestingSpecies, quantityType, initialTime, outputStartTime, true);
  }

  public HierarchicalSSADirectSimulator(String SBMLFileName, String rootDirectory, String outputDirectory, int runs, double timeLimit, double maxTimeStep, double minTimeStep, long randomSeed, double printInterval, double stoichAmpValue, 
    String[] interestingSpecies, String quantityType, double initialTime, double outputStartTime, boolean print) throws IOException, XMLStreamException, BioSimException
  {

    super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs, timeLimit, maxTimeStep, minTimeStep, printInterval, stoichAmpValue, interestingSpecies, quantityType, initialTime, outputStartTime, SimType.HSSA);
    this.print = print;
    this.randomSeed = randomSeed;

  }


  @Override
  public void initialize(long randomSeed, int runNumber) throws IOException, XMLStreamException
  {
    if (!isInitialized)
    {
      currProgress = 0;
      
      setCurrentTime(getInitialTime());
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
  public void setupForNewRun(int newRun)
  {
    setCurrentTime(getInitialTime());
    setupForOutput(newRun);
  }

  @Override
  public void simulate()
  {

    double r1 = 0, r2 = 0, totalPropensity = 0, delta_t = 0, nextReactionTime = 0, previousTime = 0, nextEventTime = 0, nextMaxTime = 0;

    if (isSbmlHasErrorsFlag())
    {
      return;
    }

    if (!isInitialized)
    {
      try
      {
        this.initialize(randomSeed, 1);
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
    printTime.setValue(getOutputStartTime());
    previousTime = 0;

    while (currentTime.getValue(0) < getTimeLimit() && !isCancelFlag())
    {
      //			if (!HierarchicalUtilities.evaluateConstraints(constraintList))
      //			{
      //				return;
      //			}

      r1 = getRandomNumberGenerator().nextDouble();
      r2 = getRandomNumberGenerator().nextDouble();
      totalPropensity = getTotalPropensity();
      delta_t = computeNextTimeStep(r1, totalPropensity);
      nextReactionTime = currentTime.getValue(0) + delta_t;
      nextEventTime = getNextEventTime();
      nextMaxTime = currentTime.getValue(0) + getMaxTimeStep();
      previousTime = currentTime.getValue(0);

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

      if (currentTime.getValue() > getTimeLimit())
      {
        break;
      }

      if (print)
      {
        printToFile();
      }

      if (currentTime.getValue(0) == nextReactionTime)
      {
        update(true, false, false, r2, previousTime);
      }
      else if (currentTime.getValue(0) == nextEventTime)
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
      setCurrentTime(getTimeLimit());
      update(false, true, true, r2, previousTime);

      if (print)
      {
        printToFile();
      }
      
      closeWriter();
    }
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
        sum += node.getValue(model.getIndex());

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
