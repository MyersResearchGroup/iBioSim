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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;

import edu.utah.ece.async.ibiosim.analysis.simulation.AbstractSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.io.HierarchicalTSDWriter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.io.HierarchicalWriter;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ConstraintNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.HierarchicalEventComparator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.TriggeredEventNode;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;


/**
 * This class provides the state variables of the simulation.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class HierarchicalSimulation extends AbstractSimulator
{

  final private int SBML_LEVEL    = 3;
  final private int SBML_VERSION  = 1;

  public static enum SimType
  {
    HSSA, HODE, FBA, MIXED, NONE;
  }

  protected final VariableNode      currentTime;
  protected final VariableNode            printTime;
  private boolean             cancelFlag;
  private int               currentRun;
  private Set<String>           interestingSpecies;
  private double              maxTimeStep;
  private double              minTimeStep;
  private String              outputDirectory;
  private boolean             printConcentrations;
  private HashSet<String>         printConcentrationSpecies;
  private double              printInterval;
  private String              SBMLFileName;
  private boolean             sbmlHasErrorsFlag;
  private boolean             stoichAmpBoolean;
  private double              stoichAmpGridValue;
  protected double            timeLimit;
  private String              rootDirectory;
  private SBMLDocument          document;
  private String              abstraction;
  private int               totalRuns;
  protected final ArrayList<Double> initValues;
  protected double            initTotalPropensity;
  protected PriorityQueue<TriggeredEventNode>    triggeredEventList;
  protected boolean           isInitialized;

  protected boolean hasEvents;

  private boolean             isGrid;
  private Random              randomNumberGenerator;
  private HierarchicalModel       topmodel;

  protected List<HierarchicalModel>     modules;
  protected FunctionNode        totalPropensity;
  private double              initialTime, outputStartTime;

  private HierarchicalWriter writer;

  protected double currProgress, maxProgress;

  final private SimType         type;
  final private StateType atomicType;
  final private StateType parentType;
  
  public HierarchicalSimulation(String SBMLFileName, String rootDirectory, String outputDirectory, long randomSeed, int runs, double timeLimit, double maxTimeStep, double minTimeStep, double printInterval, double stoichAmpValue,  String[] interestingSpecies,
    String quantityType, double initialTime, double outputStartTime, SimType type) throws XMLStreamException, IOException, BioSimException
  {
    this.SBMLFileName = SBMLFileName;
    this.timeLimit = timeLimit;
    this.maxTimeStep = maxTimeStep;
    this.minTimeStep = minTimeStep;
    this.printInterval = printInterval;
    this.printTime = new VariableNode("_printTime", StateType.SCALAR);
    this.printTime.setValue(0);
    this.rootDirectory = rootDirectory;
    this.outputDirectory = outputDirectory;
    this.printConcentrationSpecies = new HashSet<String>();
    
    this.parentType = StateType.SPARSE;
    if(type == SimType.HODE)
    {
      this.atomicType = StateType.VECTOR;
    }
    else
    {
      this.atomicType = StateType.SCALAR; 
    }
    
    if(interestingSpecies != null && interestingSpecies.length > 0)
    {
      this.interestingSpecies = new HashSet<String>();
      for(String species : interestingSpecies)
      {
        this.interestingSpecies.add(species);
      }
    }

    this.maxProgress = timeLimit * runs;
    this.document = SBMLReader.read(new File(SBMLFileName));
    this.totalRuns = runs;
    this.type = type;
    this.topmodel = new HierarchicalModel("topmodel", 0);
    this.currentTime = new VariableNode("_time", StateType.SCALAR);
    this.hasEvents = false;
    this.currentRun = 1;
    this.randomNumberGenerator = new Random(randomSeed);
    this.initialTime = initialTime;
    this.outputStartTime = outputStartTime;
    this.initValues = new ArrayList<Double>();
    this.writer = new HierarchicalTSDWriter();
    this.addPrintVariable("time", printTime, 0, false);

    this.totalPropensity = new FunctionNode(new VariableNode("propensity", StateType.SCALAR), new HierarchicalNode(Type.PLUS));


    if (quantityType != null)
    {
      String[] printConcentration = quantityType.replaceAll(" ", "").split(",");

      for (String s : printConcentration)
      {
        printConcentrationSpecies.add(s);
      }
    }

    if (stoichAmpValue <= 1.0)
    {
      stoichAmpBoolean = false;
    }
    else
    {
      stoichAmpBoolean = true;
      stoichAmpGridValue = stoichAmpValue;
    }

    SBMLErrorLog errors = document.getErrorLog();

    if (document.getErrorCount() > 0)
    {
      String errorString = "";

      for (int i = 0; i < errors.getErrorCount(); i++)
      {
        errorString += errors.getError(i);
      }


      throw new BioSimException("The SBML file contains " + document.getErrorCount() + " error(s):\n" + errorString, "Error!");
    }

  }

  public HierarchicalSimulation(HierarchicalSimulation copy)
  {
    this.SBMLFileName = copy.SBMLFileName;
    this.timeLimit = copy.timeLimit;
    this.maxTimeStep = copy.maxTimeStep;
    this.minTimeStep = copy.minTimeStep;
    this.printInterval = copy.printInterval;
    this.printTime = copy.printTime;
    this.rootDirectory = copy.rootDirectory;
    this.outputDirectory = copy.outputDirectory;
    this.printConcentrationSpecies = copy.printConcentrationSpecies;
    this.interestingSpecies = copy.interestingSpecies;
    this.document = copy.document;
    this.abstraction = copy.abstraction;
    this.totalRuns = copy.totalRuns;
    this.type = copy.type;
    this.isGrid = copy.isGrid;
    this.topmodel = copy.topmodel;
    this.currentTime = copy.currentTime;
    this.randomNumberGenerator = copy.randomNumberGenerator;
    this.initValues = copy.initValues;
    this.hasEvents = copy.hasEvents;
    this.atomicType = copy.atomicType;
    this.parentType = copy.parentType;
    //this.totalPropensity = copy.totalPropensity;
  }

  public void addModelState(HierarchicalModel modelstate)
  {

    if (modules == null)
    {
      modules = new ArrayList<HierarchicalModel>();
    }

    modules.add(modelstate);
  }

  public void addPrintVariable(String id, HierarchicalNode node, int index, boolean isConcentration)
  {
    writer.addVariable(id, node, index, isConcentration);
  }

  public List<HierarchicalModel> getListOfHierarchicalModels()
  {
    return modules;
  }

  public void setListOfHierarchicalModels(List<HierarchicalModel> modules)
  {
    this.modules = modules;
  }

  public void setHasEvents(boolean value)
  {
    this.hasEvents = value;
  }

  public boolean hasEvents()
  {
    return this.hasEvents;
  }

  /**
   * @return the cancelFlag
   */
  public boolean isCancelFlag()
  {
    return cancelFlag;
  }

  /**
   * @return the currentRun
   */
  public int getCurrentRun()
  {
    return currentRun;
  }

  /**
   * @return the currentTime
   */
  public VariableNode getCurrentTime()
  {
    return currentTime;
  }

  /**
   * @return the interestingSpecies
   */
  public Set<String> getInterestingSpecies()
  {
    return interestingSpecies;
  }

  /**
   * @return the maxTimeStep
   */
  public double getMaxTimeStep()
  {
    return maxTimeStep;
  }

  /**
   * @return the minTimeStep
   */
  public double getMinTimeStep()
  {
    return minTimeStep;
  }

  /**
   * @return the outputDirectory
   */
  public String getOutputDirectory()
  {
    return outputDirectory;
  }

  /**
   * @return the printConcentrations
   */
  public boolean isPrintConcentrations()
  {
    return printConcentrations;
  }

  /**
   * @return the printConcentrationSpecies
   */
  public HashSet<String> getPrintConcentrationSpecies()
  {
    return printConcentrationSpecies;
  }

  /**
   * @return the printInterval
   */
  public double getPrintInterval()
  {
    return printInterval;
  }

  /**
   * @return the sBML_LEVEL
   */
  public int getSBML_LEVEL()
  {
    return SBML_LEVEL;
  }

  /**
   * @return the sBML_VERSION
   */
  public int getSBML_VERSION()
  {
    return SBML_VERSION;
  }

  /**
   * @return the sBMLFileName
   */
  public String getSBMLFileName()
  {
    return SBMLFileName;
  }

  /**
   * @return the sbmlHasErrorsFlag
   */
  public boolean isSbmlHasErrorsFlag()
  {
    return sbmlHasErrorsFlag;
  }

  /**
   * @return the stoichAmpBoolean
   */
  public boolean isStoichAmpBoolean()
  {
    return stoichAmpBoolean;
  }

  /**
   * @return the stoichAmpGridValue
   */
  public double getStoichAmpGridValue()
  {
    return stoichAmpGridValue;
  }

  /**
   * @return the timeLimit
   */
  public double getTimeLimit()
  {
    return timeLimit;
  }

  /**
   * @return the rootDirectory
   */
  public String getRootDirectory()
  {
    return rootDirectory;
  }

  /**
   * @param cancelFlag
   *            the cancelFlag to set
   */
  public void setCancelFlag(boolean cancelFlag)
  {
    this.cancelFlag = cancelFlag;
  }

  /**
   * @param currentRun
   *            the currentRun to set
   */
  public void setCurrentRun(int currentRun)
  {
    this.currentRun = currentRun;
  }

  /**
   * @param currentTime
   *            the currentTime to set
   */
  public void setCurrentTime(double currentTime)
  {
    this.currentTime.setValue(currentTime);
  }

  /**
   * @param interestingSpecies
   *            the interestingSpecies to set
   */
  public void setInterestingSpecies(Set<String> interestingSpecies)
  {
    this.interestingSpecies = interestingSpecies;
  }

  /**
   * @param maxTimeStep
   *            the maxTimeStep to set
   */
  public void setMaxTimeStep(double maxTimeStep)
  {
    this.maxTimeStep = maxTimeStep;
  }

  /**
   * @param minTimeStep
   *            the minTimeStep to set
   */
  public void setMinTimeStep(double minTimeStep)
  {
    this.minTimeStep = minTimeStep;
  }

  /**
   * @param outputDirectory
   *            the outputDirectory to set
   */
  public void setOutputDirectory(String outputDirectory)
  {
    this.outputDirectory = outputDirectory;
  }

  /**
   * @param printConcentrations
   *            the printConcentrations to set
   */
  public void setPrintConcentrations(boolean printConcentrations)
  {
    this.printConcentrations = printConcentrations;
  }

  /**
   * @param printConcentrationSpecies
   *            the printConcentrationSpecies to set
   */
  public void setPrintConcentrationSpecies(HashSet<String> printConcentrationSpecies)
  {
    this.printConcentrationSpecies = printConcentrationSpecies;
  }

  /**
   * @param printInterval
   *            the printInterval to set
   */
  public void setPrintInterval(double printInterval)
  {
    this.printInterval = printInterval;
  }

  /**
   * @param sBMLFileName
   *            the sBMLFileName to set
   */
  public void setSBMLFileName(String sBMLFileName)
  {
    SBMLFileName = sBMLFileName;
  }

  /**
   * @param sbmlHasErrorsFlag
   *            the sbmlHasErrorsFlag to set
   */
  public void setSbmlHasErrorsFlag(boolean sbmlHasErrorsFlag)
  {
    this.sbmlHasErrorsFlag = sbmlHasErrorsFlag;
  }

  /**
   * @param stoichAmpBoolean
   *            the stoichAmpBoolean to set
   */
  public void setStoichAmpBoolean(boolean stoichAmpBoolean)
  {
    this.stoichAmpBoolean = stoichAmpBoolean;
  }

  /**
   * @param stoichAmpGridValue
   *            the stoichAmpGridValue to set
   */
  public void setStoichAmpGridValue(double stoichAmpGridValue)
  {
    this.stoichAmpGridValue = stoichAmpGridValue;
  }

  /**
   * @param timeLimit
   *            the timeLimit to set
   */
  public void setTimeLimit(double timeLimit)
  {
    this.timeLimit = timeLimit;
  }

  /**
   * @param rootDirectory
   *            the rootDirectory to set
   */
  public void setRootDirectory(String rootDirectory)
  {
    this.rootDirectory = rootDirectory;
  }

  /**
   * @return the document
   */
  public SBMLDocument getDocument()
  {
    return document;
  }

  /**
   * @param document
   *            the document to set
   */
  public void setDocument(SBMLDocument document)
  {
    this.document = document;
  }

  /**
   * 
   * @return
   */
  public String getAbstraction()
  {
    return abstraction;
  }

  /**
   * 
   * @param abstraction
   */
  public void setAbstraction(String abstraction)
  {
    this.abstraction = abstraction;
  }

  /**
   * 
   * @return
   */
  public int getTotalRuns()
  {
    return totalRuns;
  }

  /**
   * @return the randomNumberGenerator
   */
  public Random getRandomNumberGenerator()
  {
    return randomNumberGenerator;
  }



  /**
   * @return the topmodel
   */
  public HierarchicalModel getTopmodel()
  {
    return topmodel;
  }

  /**
   * @return the isGrid
   */
  public boolean isGrid()
  {
    return isGrid;
  }
  /**
   * @param isGrid
   *            the isGrid to set
   */
  public void setGrid(boolean isGrid)
  {
    this.isGrid = isGrid;
  }

  /**
   * @param topmodel
   *            the topmodel to set
   */
  public void setTopmodel(HierarchicalModel topmodel)
  {
    this.topmodel = topmodel;
  }

  public StateType getAtomicType()
  {
    return this.atomicType;
  }
  
  public StateType getCollectionType()
  {
    return this.parentType;
  }

  public SimType getSimType()
  {
    return type;
  }

  protected void setupForOutput(int currentRun)
  {
    setCurrentRun(currentRun);
    try
    {
      writer.init(getOutputDirectory() + File.separator + "run-" + currentRun + ".tsd");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public double getInitialTime()
  {
    return initialTime;
  }

  public double getOutputStartTime()
  {
    return outputStartTime;
  }

  /**
   * Returns the total propensity of all model states.
   */
  protected double getTotalPropensity()
  {
    return totalPropensity != null ? totalPropensity.getVariable().getValue() : 0;
  }

  public void setTopLevelValue(String variable, double value)
  {
    if (topmodel != null)
    {
      VariableNode variableNode = topmodel.getNode(variable);
      if (variableNode != null)
      {
        variableNode.setValue(0, value);
      }
    }
  }

  public double getTopLevelValue(String variable)
  {
    if (topmodel != null)
    {
      VariableNode variableNode = topmodel.getNode(variable);
      if (variableNode != null)
      {
        return variableNode.getValue(0);
      }
    }

    return Double.NaN;
  }

  protected void closeWriter()
  {
    try {
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  protected void printToFile()
  {
    while (currentTime.getValue() >= printTime.getValue() && printTime.getValue() <= getTimeLimit())
    {
      try
      {
        writer.print();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      currProgress += getPrintInterval();
      printTime.setValue(getRoundedDouble(printTime.getValue() + getPrintInterval()));

      message.setInteger((int)(Math.ceil(100*currProgress/maxProgress)));
      parent.send(RequestType.REQUEST_PROGRESS, message);
    }
  }

  private double getRoundedDouble(double value)
  {
    BigDecimal bd = new BigDecimal(value);
    bd = bd.setScale(6, BigDecimal.ROUND_HALF_EVEN);
    double newValue = bd.doubleValue();
    bd = null;
    return newValue;
  }


  protected void computeAssignmentRules()
  {

    boolean changed = true;
    while (changed)
    {
      changed = false;
      for(HierarchicalModel modelstate : this.modules)
      {
        if(modelstate.getAssignRules() != null)
          for (FunctionNode node : modelstate.getAssignRules())
          {
            changed = changed | node.computeFunction(modelstate.getIndex());
          }
      }
    }
  }

  protected void reevaluatePriorities()
  {
    PriorityQueue<TriggeredEventNode> tmp = new PriorityQueue<TriggeredEventNode>(1, new HierarchicalEventComparator());
    while (triggeredEventList != null && !triggeredEventList.isEmpty())
    {
      TriggeredEventNode eventState = triggeredEventList.poll();
      EventNode event = eventState.getParent();
      int index = eventState.getIndex();
      eventState.setPriority(event.evaluatePriority(index));
      tmp.add(eventState);
    }
    triggeredEventList = tmp;
  }


  protected void checkEvents()
  {
    for(HierarchicalModel model : modules)
    {
      int index = model.getIndex();
      for (EventNode event : model.getEvents())
      {
        if(event.isTriggeredAtTime(currentTime.getValue(), index))
        {
          event.setMaxDisabledTime(index, Double.NEGATIVE_INFINITY);
          event.setMinEnabledTime(index, Double.POSITIVE_INFINITY);
          TriggeredEventNode triggered = new TriggeredEventNode(index, event);
          triggered.setPriority(event.evaluatePriority(index));
          triggered.setFireTime(currentTime.getValue() + event.evaluateFireTime(index));
          if(event.isUseTriggerValue())
          {
            double[] eventAssignments = event.computeEventAssignmentValues(index, currentTime.getValue());
            
            if(eventAssignments != null)
            {
              triggered.setAssignmentValues(eventAssignments);  
            }
            
          }
          triggeredEventList.add(triggered);
          if(!event.isPersistent())
          {
            event.addTriggeredEvent(index, triggered);
          }
        }

      }
    }
  }

  protected void computeEvents()
  {
    boolean changed = true;
    double time = currentTime.getValue();

    while (changed)
    {
      changed = false;

      checkEvents();

      if (triggeredEventList != null && !triggeredEventList.isEmpty())
      {
        TriggeredEventNode eventState = triggeredEventList.peek();
        int index = eventState.getIndex();
        if (eventState.getFireTime() <= time)
        {
          triggeredEventList.poll();
          eventState.fireEvent(index, time);
          reevaluatePriorities();
          changed = true;
        }
        else
        {
          break;
        }
      }
    }

  }

  /**
   * Calculate fixed-point of initial assignments
   * 
   * @param modelstate
   * @param variables
   * @param math
   */
  protected void computeFixedPoint()
  {
    boolean changed = true;

    while (changed)
    {
      changed = false;

      for(HierarchicalModel modelstate : this.modules)
      {
        if(modelstate.getAssignRules() != null)
        {
          for(FunctionNode node : modelstate.getAssignRules())
          {
            changed = changed | node.computeFunction(modelstate.getIndex());
          }
        }

        if(modelstate.getInitAssignments() != null)
        {
          for(FunctionNode node : modelstate.getInitAssignments())
          {
            changed = changed | node.computeFunction(modelstate.getIndex());
          }
        }

        if(modelstate.getInitConcentration() != null)
        {
          for(FunctionNode node : modelstate.getInitConcentration())
          {
            if(!node.getVariable().hasRule() && !node.getVariable().hasInitRule())
            {
              changed = changed | node.computeFunction(modelstate.getIndex());
            }
          }
        }

        if(modelstate.getReactions() != null)
        {
          for(ReactionNode node : modelstate.getReactions())
          {
            changed = changed | node.computePropensity(modelstate.getIndex());
          }

          modelstate.getPropensity().computeFunction(modelstate.getIndex());
        }
      }
    }
  }

  public boolean evaluateConstraints()
  {
    boolean hasSuccess = true;
    for(HierarchicalModel model : modules)
    {
      for (ConstraintNode constraintNode : model.getConstraints())
      {
        hasSuccess = hasSuccess && constraintNode.evaluateConstraint(model.getIndex());
      }
    }
    return hasSuccess;
  }

}