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
package main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;

import dataModels.util.GlobalConstants;
import dataModels.util.exceptions.BioSimException;
import main.java.edu.utah.ece.async.analysis.dynamicsim.ParentSimulator;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.io.HierarchicalWriter;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.ConstraintNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.EventNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.FunctionNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.ReactionNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.VariableNode;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import main.java.edu.utah.ece.async.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

/**
 * This class provides the state variables of the simulation.
 *
 * @author Leandro Watanabe
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public abstract class HierarchicalSimulation implements ParentSimulator
{

  final private int SBML_LEVEL    = 3;
  final private int SBML_VERSION  = 1;

  public static enum SimType
  {
    HSSA, HODE, FBA, MIXED, NONE;
  }

  protected final VariableNode      currentTime;
  protected double            printTime;
  private BufferedWriter          bufferedTSDWriter;
  private boolean             cancelFlag;
  private boolean             constraintFailureFlag;
  private boolean             constraintFlag;
  private int               currentRun;
  private String[]            interestingSpecies;
  private double              maxTimeStep;
  private double              minTimeStep;
  private String              outputDirectory;
  private boolean             printConcentrations;
  private HashSet<String>         printConcentrationSpecies;
  private double              printInterval;
  private JProgressBar          progress;
  private JFrame              running;
  private String              SBMLFileName;
  private boolean             sbmlHasErrorsFlag;
  private String              separator;
  private boolean             stoichAmpBoolean;
  private double              stoichAmpGridValue;
  protected double            timeLimit;
  private String              rootDirectory;
  private FileWriter            TSDWriter;
  private SBMLDocument          document;
  private String              abstraction;
  private int               totalRuns;

  
  protected final ArrayList<Double> initValues;
  
  protected List<VariableNode>      variableList;

  protected double            initTotalPropensity;

  protected List<EventNode>       eventList;
  protected PriorityQueue<EventNode>    triggeredEventList;

  protected boolean           isInitialized;
  private int               numSubmodels;
  private boolean             isGrid;
  private Random              randomNumberGenerator;
  private HierarchicalModel       topmodel;
  private Map<String, HierarchicalModel>  submodels;
  final private SimType         type;
  private List<HierarchicalModel>     modules;
  private HierarchicalNode        totalPropensity;
  private double              initialTime, outputStartTime;

  public HierarchicalSimulation(String SBMLFileName, String rootDirectory, String outputDirectory, long randomSeed, int runs, double timeLimit, double maxTimeStep, double minTimeStep, JProgressBar progress, double printInterval, double stoichAmpValue, JFrame running, String[] interestingSpecies,
    String quantityType, String abstraction, double initialTime, double outputStartTime, SimType type) throws XMLStreamException, IOException, BioSimException
    {
    this.SBMLFileName = SBMLFileName;
    this.timeLimit = timeLimit;
    this.maxTimeStep = maxTimeStep;
    this.minTimeStep = minTimeStep;
    this.progress = progress;
    this.printInterval = printInterval;
    this.printTime = 0;
    this.rootDirectory = rootDirectory;
    this.outputDirectory = outputDirectory;
    this.running = running;
    this.printConcentrationSpecies = new HashSet<String>();
    this.interestingSpecies = interestingSpecies;
    this.document = SBMLReader.read(new File(SBMLFileName));
    this.abstraction = abstraction;
    this.totalRuns = runs;
    this.type = type;
    this.topmodel = new HierarchicalModel("topmodel");
    this.submodels = new HashMap<String, HierarchicalModel>(0);
    this.currentTime = new VariableNode("_time", StateType.SCALAR);

    this.currentRun = 1;
    this.randomNumberGenerator = new Random(randomSeed);
    this.initialTime = initialTime;
    this.outputStartTime = outputStartTime;

    this.initValues = new ArrayList<Double>();
    
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

    separator = GlobalConstants.separator;
    }

  public HierarchicalSimulation(HierarchicalSimulation copy)
  {
    this.SBMLFileName = copy.SBMLFileName;
    this.timeLimit = copy.timeLimit;
    this.maxTimeStep = copy.maxTimeStep;
    this.minTimeStep = copy.minTimeStep;
    this.progress = copy.progress;
    this.printInterval = copy.printInterval;
    this.rootDirectory = copy.rootDirectory;
    this.outputDirectory = copy.outputDirectory;
    this.running = copy.running;
    this.printConcentrationSpecies = copy.printConcentrationSpecies;
    this.interestingSpecies = copy.interestingSpecies;
    this.document = copy.document;
    this.abstraction = copy.abstraction;
    this.totalRuns = copy.totalRuns;
    this.type = copy.type;
    this.isGrid = copy.isGrid;
    this.topmodel = copy.topmodel;
    this.submodels = copy.submodels;
    this.separator = copy.separator;
    this.currentTime = copy.currentTime;
    this.randomNumberGenerator = copy.randomNumberGenerator;
    this.initValues = copy.initValues;
  }

  public void addModelState(HierarchicalModel modelstate)
  {

    if (modules == null)
    {
      modules = new ArrayList<HierarchicalModel>();
    }

    modules.add(modelstate);
  }

  public List<HierarchicalModel> getListOfModelStates()
  {
    return modules;
  }

  /**
   * @return the bufferedTSDWriter
   */
  public BufferedWriter getBufferedTSDWriter()
  {
    return bufferedTSDWriter;
  }

  /**
   * @return the cancelFlag
   */
  public boolean isCancelFlag()
  {
    return cancelFlag;
  }

  /**
   * @return the constraintFailureFlag
   */
  public boolean isConstraintFailureFlag()
  {
    return constraintFailureFlag;
  }

  /**
   * @return the constraintFlag
   */
  public boolean isConstraintFlag()
  {
    return constraintFlag;
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
  public String[] getInterestingSpecies()
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
   * @return the progress
   */
  public JProgressBar getProgress()
  {
    return progress;
  }

  /**
   * @return the running
   */
  public JFrame getRunning()
  {
    return running;
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
   * @return the separator
   */
  public String getSeparator()
  {
    return separator;
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
   * @return the tSDWriter
   */
  public FileWriter getTSDWriter()
  {
    return TSDWriter;
  }

  /**
   * @param bufferedTSDWriter
   *            the bufferedTSDWriter to set
   */
  public void setBufferedTSDWriter(BufferedWriter bufferedTSDWriter)
  {
    this.bufferedTSDWriter = bufferedTSDWriter;
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
   * @param constraintFailureFlag
   *            the constraintFailureFlag to set
   */
  public void setConstraintFailureFlag(boolean constraintFailureFlag)
  {
    this.constraintFailureFlag = constraintFailureFlag;
  }

  /**
   * @param constraintFlag
   *            the constraintFlag to set
   */
  public void setConstraintFlag(boolean constraintFlag)
  {
    this.constraintFlag = constraintFlag;
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
  public void setInterestingSpecies(String[] interestingSpecies)
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
   * @param progress
   *            the progress to set
   */
  public void setProgress(JProgressBar progress)
  {
    this.progress = progress;
  }

  /**
   * @param running
   *            the running to set
   */
  public void setRunning(JFrame running)
  {
    this.running = running;
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
   * @param separator
   *            the separator to set
   */
  public void setSeparator(String separator)
  {
    this.separator = separator;
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
   * @param tSDWriter
   *            the tSDWriter to set
   */
  public void setTSDWriter(FileWriter tSDWriter)
  {
    TSDWriter = tSDWriter;
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
   * @return the numSubmodels
   */
  public int getNumSubmodels()
  {
    return numSubmodels;
  }

  /**
   * @return the randomNumberGenerator
   */
  public Random getRandomNumberGenerator()
  {
    return randomNumberGenerator;
  }

  /**
   * @return the submodels
   */
  public Map<String, HierarchicalModel> getSubmodels()
  {
    return submodels;
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
   * @param numSubmodels
   *            the numSubmodels to set
   */
  public void setNumSubmodels(int numSubmodels)
  {
    this.numSubmodels = numSubmodels;
  }

  /**
   * @param submodels
   *            the submodels to set
   */
  public void addSubmodel(String id, HierarchicalModel modelstate)
  {
    if (submodels == null)
    {
      submodels = new HashMap<String, HierarchicalModel>();
    }
    submodels.put(id, modelstate);
  }

  /**
   * @param topmodel
   *            the topmodel to set
   */
  public void setTopmodel(HierarchicalModel topmodel)
  {
    this.topmodel = topmodel;
  }

  public HierarchicalModel getModelState(String id)
  {
    if (id.equals("topmodel"))
    {
      return topmodel;
    }
    return submodels.get(id);
  }

  public SimType getType()
  {
    return type;
  }

  protected void setupForOutput(int currentRun)
  {
    setCurrentRun(currentRun);
    try
    {
      setTSDWriter(new FileWriter(getOutputDirectory() + "run-" + currentRun + ".tsd"));
      setBufferedTSDWriter(new BufferedWriter(getTSDWriter()));
      getBufferedTSDWriter().write('(');
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected List<ReactionNode> getReactionList()
  {
    List<ReactionNode> reactions = new ArrayList<ReactionNode>();
    List<Integer> indexToSubmodel = new ArrayList<Integer>();
    for (HierarchicalModel submodel : modules)
    {
      for (int i = submodel.getNumOfReactions() - 1; i >= 0; i--)
      {
        ReactionNode node = submodel.getReaction(i);
        reactions.add(node);
        indexToSubmodel.add(submodel.getIndex());
        node.setIndexToSubmodel(indexToSubmodel);
      }
    }

    return reactions;

  }


  protected List<EventNode> getEventList()
  {

    List<EventNode> events = new ArrayList<EventNode>();

    for (HierarchicalModel modelstate : modules)
    {

      if (modelstate.getNumOfEvents() > 0)
      {
        events.addAll(modelstate.getEvents());
      }
    }

    return events;

  }

  protected List<VariableNode> getVariableList()
  {

    List<VariableNode> variableList = new ArrayList<VariableNode>();
    List<Integer> indexToSubmodel = new ArrayList<Integer>();
    
    for (HierarchicalModel modelstate : modules)
    {
      int size =  modelstate.getNumOfVariables();
      for (int i = 0; i < size; ++i)
      {
        VariableNode node = modelstate.getVariable(i);
        variableList.add(node);
        node.setIndexToSubmodel(indexToSubmodel);
        indexToSubmodel.add(i);
      }
    }

    return variableList;

  }

  protected List<ConstraintNode> getConstraintList()
  {

    List<ConstraintNode> constraints = new ArrayList<ConstraintNode>();

    List<Integer> indexToSubmodel = new ArrayList<Integer>();

    for (HierarchicalModel modelstate : modules)
    {
      for (int i = modelstate.getNumOfConstraints() - 1; i >= 0; i--)
      {
        ConstraintNode node = modelstate.getConstraint(i);
        constraints.add(node);
        node.setIndexToSubmodel(indexToSubmodel);
        indexToSubmodel.add(i);
      }
    }

    return constraints;

  }

  public double getInitialTime()
  {
    return initialTime;
  }

  public double getOutputStartTime()
  {
    return outputStartTime;
  }

  public void linkPropensities()
  {
    HierarchicalNode propensity;
    totalPropensity = new VariableNode("_totalPropensity", StateType.SCALAR);

    for (HierarchicalModel modelstate : modules)
    {

      if (modelstate.getNumOfReactions() > 0)
      {
        propensity = modelstate.createPropensity();
        for (ReactionNode node : modelstate.getReactions())
        {
          node.setTotalPropensityRef(totalPropensity);
          node.setModelPropensityRef(propensity);
        }
      }
    }

  }

  /**
   * Returns the total propensity of all model states.
   */
  protected double getTotalPropensity()
  {
    return totalPropensity != null ? totalPropensity.getValue(0) : 0;
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

  protected void printToFile()
  {
    while (currentTime.getValue() >= printTime && printTime <= getTimeLimit())
    {
      try
      {
        HierarchicalWriter.printToTSD(getBufferedTSDWriter(), getTopmodel(), getSubmodels(), getInterestingSpecies(), getPrintConcentrationSpecies(), printTime);
        getBufferedTSDWriter().write(",\n");
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }

      printTime = getRoundedDouble(printTime + getPrintInterval());

      if (getRunning() != null)
      {
        getRunning().setTitle("Progress (" + (int) ((getCurrentTime().getValue() / getTimeLimit()) * 100.0) + "%)");
      }
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

  protected void setInitialPropensity()
  {
    this.initTotalPropensity = totalPropensity.getValue(0);

    for (HierarchicalModel state : modules)
    {
      state.setInitPropensity(0);

      for (int i = state.getNumOfReactions() - 1; i >= 0; i--)
      {
        state.getReactions().get(i).setInitPropensity(0);
      }
    }
  }

  protected void restoreInitialPropensity()
  {
    totalPropensity.setValue(0, initTotalPropensity);
    for (HierarchicalModel state : modules)
    {
      state.restoreInitPropensity(0);

      for (int i = state.getNumOfReactions() - 1; i >= 0; i--)
      {
        state.getReactions().get(i).restoreInitPropensity(state.getIndex());
      }
    }
  }

  public List<HierarchicalModel> getListOfModules()
  {
    return modules;
  }

  public void setListOfModules(List<HierarchicalModel> modules)
  {
    this.modules = modules;
  }



  public void computeAssignmentRules()
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
  
    /**
     * Calculate fixed-point of initial assignments
     * 
     * @param modelstate
     * @param variables
     * @param math
     */
    public void computeFixedPoint()
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

          if(modelstate.getReactions() != null)
          {
            for(ReactionNode node : modelstate.getReactions())
            {
              changed = changed | node.computePropensity(modelstate.getIndex());
            }
          }
        }
      }

    }
}