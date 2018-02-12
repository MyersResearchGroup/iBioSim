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

import java.io.File;
import java.io.IOException;
import java.util.PriorityQueue;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.HighamHall54Integrator;

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
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

  private HighamHall54Integrator odecalc;
  private DifferentialEquations  de;
  private final VectorWrapper    vectorWrapper;
  private final boolean print;
  
  public HierarchicalODERKSimulator(AnalysisProperties properties) throws IOException, XMLStreamException, BioSimException {
    super(properties, SimType.HODE);
    this.vectorWrapper = new VectorWrapper();

    SimulationProperties simProperties = properties.getSimulationProperties();
    odecalc = new HighamHall54Integrator(simProperties.getMinTimeStep(), simProperties.getMaxTimeStep(),
      simProperties.getAbsError(), simProperties.getRelError());
    isInitialized = false;
    print = true;
    
  }
  
  public HierarchicalODERKSimulator(AnalysisProperties properties,
    boolean print) throws IOException, XMLStreamException, BioSimException {
    super(properties, SimType.HODE);
    this.vectorWrapper = new VectorWrapper();

    SimulationProperties simProperties = properties.getSimulationProperties();
    odecalc = new HighamHall54Integrator(simProperties.getMinTimeStep(), simProperties.getMaxTimeStep(),
      simProperties.getAbsError(), simProperties.getRelError());
    isInitialized = false;
    this.print = print;
    
  }


  public HierarchicalODERKSimulator(HierarchicalMixedSimulator sim,
    HierarchicalModel topmodel) throws IOException, XMLStreamException 
  {
    super(sim);
    SimulationProperties simProperties = properties.getSimulationProperties();
    this.odecalc = new HighamHall54Integrator(simProperties.getMinTimeStep(),
      simProperties.getMaxTimeStep(), simProperties.getAbsError(), simProperties.getRelError());
    this.isInitialized = true;
    print = false;
    this.printTime = 0;
    this.vectorWrapper = sim.getVectorWrapper();
    de = new DifferentialEquations();
    if (sim.hasEvents()) {
      HierarchicalEventHandler handler = new HierarchicalEventHandler();
      HierarchicalTriggeredEventHandler triggeredHandler =
          new HierarchicalTriggeredEventHandler();
      odecalc.addEventHandler(handler, simProperties.getPrintInterval(), 1e-20, 10000);
      odecalc.addEventHandler(triggeredHandler, simProperties.getPrintInterval(), 1e-20,
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
      SimulationProperties simProperties = properties.getSimulationProperties();
      currProgress = 0;
      setCurrentTime(simProperties.getInitialTime());
      ModelSetup.setupModels(this, ModelType.HODE, vectorWrapper);
      de = new DifferentialEquations();
      vectorWrapper.initStateValues();
      restoreInitialState();
      computeFixedPoint();
      if (hasEvents()) {
        HierarchicalEventHandler handler = new HierarchicalEventHandler();
        HierarchicalTriggeredEventHandler triggeredHandler =
            new HierarchicalTriggeredEventHandler();
        odecalc.addEventHandler(handler, simProperties.getPrintInterval(), 1e-20, 10000);
        odecalc.addEventHandler(triggeredHandler, simProperties.getPrintInterval(), 1e-20,
          10000);
        triggeredEventList =
            new PriorityQueue<TriggeredEventNode>(1, new HierarchicalEventComparator());
        computeEvents();
      }
      if (print) {
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

    SimulationProperties simProperties = properties.getSimulationProperties();
    double nextEndTime = 0;
    double timeLimit = simProperties.getTimeLimit();
    double maxTimeStep = simProperties.getMaxTimeStep();
    while (currentTime.getState().getStateValue() < timeLimit && !isCancelFlag()) 
    {

      // if (!HierarchicalUtilities.evaluateConstraints(constraintList))
      // {
      // return;
      // }
      nextEndTime = getRoundedDouble(currentTime.getState().getStateValue() + maxTimeStep);
      
      if (nextEndTime > printTime) {
        nextEndTime = printTime;
      }
      if (nextEndTime > timeLimit) {
        nextEndTime = timeLimit;
      }
      if (vectorWrapper.getSize() > 0) {
        try {
          odecalc.integrate(de, currentTime.getState().getStateValue(),
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
      if (print) {
        printToFile();
      }
    }
    if (print) 
    {
      printToFile();
      //closeWriter();
    }
    
  }


  @Override
  public void setupForNewRun(int newRun) throws IOException 
  {
    setCurrentTime(properties.getSimulationProperties().getInitialTime());
    restoreInitialState();
    setupForOutput(newRun);
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
          double oldValue = node.getState().getState(index).getRateValue();
          node.getState().getState(index).setRateValue(node.computeRateOfChange(index));
          double newValue = node.getState().getState(index).getRateValue();
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
      currentTime.getState().setStateValue(t);
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
      currentTime.getState().setStateValue(t);
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
      currentTime.getState().setStateValue(t);
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
      currentTime.getState().setStateValue(t);
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
