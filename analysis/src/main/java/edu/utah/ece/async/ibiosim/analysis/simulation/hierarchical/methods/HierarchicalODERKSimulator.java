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
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.HierarchicalEventComparator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.TriggeredEventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup.ModelSetup;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;

/**
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim
 *         Contributors </a>
 * @version %I%
 */
public final class HierarchicalODERKSimulator extends HierarchicalSimulation {

  private boolean                isSingleStep;
  private HighamHall54Integrator odecalc;
  private double                 relativeError, absoluteError;
  private DifferentialEquations  de;
  private final VectorWrapper    vectorWrapper;


  public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory,
    double timeLimit) throws IOException, XMLStreamException, BioSimException {
    this(SBMLFileName, rootDirectory, rootDirectory, 0, timeLimit,
      Double.POSITIVE_INFINITY, 0, Double.POSITIVE_INFINITY, 0,
      null, 1, 1e-6, 1e-9, "amount", 0, 0, false);
    isInitialized = false;
    isSingleStep = true;
    this.printTime.setValue(Double.POSITIVE_INFINITY);
  }


  public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory,
    String outputDirectory, int runs, double timeLimit, double maxTimeStep,
    long randomSeed, double printInterval,
    double stoichAmpValue, String[] interestingSpecies,
    int numSteps, double relError, double absError, String quantityType,
     double initialTime, double outputStartTime)
        throws IOException, XMLStreamException, BioSimException {
    this(SBMLFileName, rootDirectory, outputDirectory, runs, timeLimit,
      maxTimeStep, randomSeed, printInterval, stoichAmpValue,
      interestingSpecies, numSteps, relError, absError, quantityType,
       initialTime, outputStartTime, true);
    this.isInitialized = false;
    this.isSingleStep = false;
  }


  public HierarchicalODERKSimulator(String SBMLFileName, String rootDirectory,
    String outputDirectory, int runs, double timeLimit, double maxTimeStep,
    long randomSeed, double printInterval,
    double stoichAmpValue, String[] interestingSpecies,
    int numSteps, double relError, double absError, String quantityType,
    double initialTime, double outputStartTime,
    boolean print) throws IOException, XMLStreamException, BioSimException {
    super(SBMLFileName, rootDirectory, outputDirectory, randomSeed, runs,
      timeLimit, maxTimeStep, 0.0, printInterval, stoichAmpValue,
      interestingSpecies, quantityType, initialTime,
      outputStartTime, SimType.HODE);
    this.relativeError = relError;
    this.absoluteError = absError;
    this.isSingleStep = false;
    this.absoluteError = absoluteError == 0 ? 1e-12 : absoluteError;
    this.relativeError = absoluteError == 0 ? 1e-9 : relativeError;
    this.printTime.setValue(outputStartTime);
    this.vectorWrapper = new VectorWrapper(initValues);
    if (numSteps > 0) {
      setPrintInterval(timeLimit / numSteps);
    }
    odecalc = new HighamHall54Integrator(getMinTimeStep(), getMaxTimeStep(),
      absoluteError, relativeError);
    isInitialized = false;
  }


  public HierarchicalODERKSimulator(HierarchicalMixedSimulator sim,
    HierarchicalModel topmodel) throws IOException, XMLStreamException {
    super(sim);
    this.relativeError = 1e-6;
    this.absoluteError = 1e-6;
    this.odecalc = new HighamHall54Integrator(getMinTimeStep(),
      getMaxTimeStep(), absoluteError, relativeError);
    this.isInitialized = true;
    this.isSingleStep = true;
    this.printTime.setValue(0);
    this.vectorWrapper = sim.getVectorWrapper();
    de = new DifferentialEquations();
    if (sim.hasEvents()) {
      HierarchicalEventHandler handler = new HierarchicalEventHandler();
      HierarchicalTriggeredEventHandler triggeredHandler =
          new HierarchicalTriggeredEventHandler();
      odecalc.addEventHandler(handler, getPrintInterval(), 1e-20, 10000);
      odecalc.addEventHandler(triggeredHandler, getPrintInterval(), 1e-20,
        10000);
      triggeredEventList =
          new PriorityQueue<TriggeredEventNode>(1, new HierarchicalEventComparator());
      computeEvents();
    }
  }


  @Override
  public void cancel() {
    setCancelFlag(true);
  }


  @Override
  public void clear() {
  }


  @Override
  public void initialize(long randomSeed, int runNumber)
      throws IOException, XMLStreamException {
    if (!isInitialized) {
      currProgress = 0;
      setCurrentTime(getInitialTime());
      ModelSetup.setupModels(this, ModelType.HODE, vectorWrapper);
      de = new DifferentialEquations();
      computeFixedPoint();
      if (hasEvents()) {
        HierarchicalEventHandler handler = new HierarchicalEventHandler();
        HierarchicalTriggeredEventHandler triggeredHandler =
            new HierarchicalTriggeredEventHandler();
        odecalc.addEventHandler(handler, getPrintInterval(), 1e-20, 10000);
        odecalc.addEventHandler(triggeredHandler, getPrintInterval(), 1e-20,
          10000);
        triggeredEventList =
            new PriorityQueue<TriggeredEventNode>(1, new HierarchicalEventComparator());
        computeEvents();
      }
      if (!isSingleStep) {
        setupForOutput(runNumber);
      }
      isInitialized = true;
    }
  }


  @Override
  public void simulate() throws IOException, XMLStreamException {
    if (!isInitialized) {
        initialize(0, 1);
    }
    double nextEndTime = 0;
    while (currentTime.getValue() < getTimeLimit() && !isCancelFlag()) {
      // if (!HierarchicalUtilities.evaluateConstraints(constraintList))
      // {
      // return;
      // }
      nextEndTime = getRoundedDouble(currentTime.getValue() + getMaxTimeStep());
      
      if (nextEndTime > printTime.getValue()) {
        nextEndTime = printTime.getValue();
      }
      if (nextEndTime > getTimeLimit()) {
        nextEndTime = getTimeLimit();
      }
      if (vectorWrapper.getSize() > 0) {
        try {
          odecalc.integrate(de, currentTime.getValue(),
            vectorWrapper.getValues(), nextEndTime, vectorWrapper.getValues());
         
          computeAssignmentRules();
        } catch (NumberIsTooSmallException e) {
          setCurrentTime(nextEndTime);
        }
        catch (MaxCountExceededException e) {
          setCurrentTime(nextEndTime);
        }
      } else {
        setCurrentTime(nextEndTime);
      }
      if (!isSingleStep) {
        printToFile();
      }
    }
    if (!isSingleStep) 
    {
      printToFile();
      //closeWriter();
    }
    
  }


  @Override
  public void setupForNewRun(int newRun) {
  }


  @Override
  public void printStatisticsTSD() {
    // TODO Auto-generated method stub
  }
  
  private void computeRates()
  {
    boolean hasChanged = true;
    
    while(hasChanged)
    {
      hasChanged = false;
      for (HierarchicalModel hierarchicalModel : modules) {
        hasChanged |= hierarchicalModel.computePropensities();
      }
      
      for (HierarchicalModel hierarchicalModel : modules) {
        int index = hierarchicalModel.getIndex();
        for (VariableNode node : hierarchicalModel.getListOfVariables()) {
          double oldValue = node.getRate(index);
          node.setRateValue(index, node.computeRateOfChange(index));
          double newValue = node.getRate(index);
          hasChanged |= newValue != oldValue;
        }
      }
      computeAssignmentRules();
    }
  }

  public class HierarchicalEventHandler implements EventHandler {

    private double value = -1;


    @Override
    public void init(double t0, double[] y0, double t) {
    }


    @Override
    public double g(double t, double[] y) {
      double returnValue = -value;
      currentTime.setValue(t);
      vectorWrapper.setValues(y);
      computeRates();
      for (HierarchicalModel modelstate : modules) {
        int index = modelstate.getIndex();
        for (EventNode event : modelstate.getEvents()) {
            if (event.isTriggeredAtTime(t, index)) {
              returnValue = value;
          }
        }
      }
      return returnValue;
    }


    @Override
    public Action eventOccurred(double t, double[] y, boolean increasing) {
      value = -value;
      currentTime.setValue(t);
      vectorWrapper.setValues(y);
      computeRates();
      for (HierarchicalModel modelstate : modules) {
        int index = modelstate.getIndex();
        for (EventNode event : modelstate.getEvents()) {
            if(event.getMaxDisabledTime(index) > t)
            {
              event.setMaxDisabledTime(index, t);
            }
        }
      }

      computeEvents();
      return EventHandler.Action.STOP;
    }


    @Override
    public void resetState(double t, double[] y) {
    }
  }

  public class HierarchicalTriggeredEventHandler implements EventHandler {

    private double value = -1;


    @Override
    public void init(double t0, double[] y0, double t) {
    }


    @Override
    public double g(double t, double[] y) {
      currentTime.setValue(t);
      computeRates();
      if (!triggeredEventList.isEmpty()) {
        if (triggeredEventList.peek().getFireTime() <= t) {
          return value;
        }
      }
      return -value;
    }


    @Override
    public Action eventOccurred(double t, double[] y, boolean increasing) {
      value = -value;
      currentTime.setValue(t);
      vectorWrapper.setValues(y);
      computeRates();
      computeEvents();
      return EventHandler.Action.STOP;
    }


    @Override
    public void resetState(double t, double[] y) {
      // TODO Auto-generated method stub
    }
  }

  public class DifferentialEquations
  implements FirstOrderDifferentialEquations {

    @Override
    public int getDimension() {
      return vectorWrapper.getSize();
    }

    @Override
    public void computeDerivatives(double t, double[] y, double[] yDot)
        throws MaxCountExceededException, DimensionMismatchException 
    {
      
      if(Double.isNaN(t))
      {
        throw new MaxCountExceededException(t);
      }
      
      setCurrentTime(t);
      vectorWrapper.setValues(y);
      computeAssignmentRules();
      vectorWrapper.setRates(yDot);
      computeRates();
    }
    

    
  }
}
