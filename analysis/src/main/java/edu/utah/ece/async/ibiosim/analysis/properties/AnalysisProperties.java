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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;

/**
 * The AnalysisProperties class incorporates all of the necessary information to run analysis in iBioSim,
 * where the different properties are separated in the following classes:
 * 
 * <ul>
 * <li> {@link AdvancedProperties}: for abstraction parameters.</li>
 * <li> {@link IncrementalProperties}: for parameters used in the incremental simulation methods.</li>
 * <li> {@link SimulationProperties}: for simulation paramters.</li>
 * <li> {@link VerificationProperties}: for verification parameters.</li>
 * </ul>
 * 
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public final class AnalysisProperties extends CoreObservable
{
  
  private static enum UserInterval
  {
    MIN_PRINT_INTERVAL, NUM_STEPS, PRINT_INTERVAL;
  }	

  private static enum SimMethod
  {
    ODE, SSA, MARKOV, FBA, SBML, DOT, XHTML, LHPN;
  } 

  private static enum AbstractionMethod
  {
    NONE, EXPAND, ABSTRACTION, NARY;
  }

  private String filename, id, root, directory, outDir, modelFile, propertiesFile;
  private String        sim;
  private String        simProp;
  private String fileStem;
  
  private SimMethod method;
  private AbstractionMethod abs;
  
  private final boolean gui;
  
  // Different properties
  private final AdvancedProperties advProperties;
  private final IncrementalProperties incProperties;
  private final SimulationProperties simProperties;
  private final VerificationProperties verifProperties;
  

  // Predefined reb2sac and atacs commands
  private final String[] hse2Command = new String[]{Executables.reb2sacExecutable, "--target.encoding=hse2", null};
  private final String[] atacsCommand = new String[]{"atacs", "-T0.000001", "-oqoflhsgllvA", null, "out.hse"};
  private final String[] sbmlCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=sbml", null, null};
  private final String[] reb2sacCommand = new String[]{Executables.reb2sacExecutable, null,null, null };
  private final String[] dotCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=dot",  null, null};
  private final String[] xhtmlCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=xhtml", null, null};
  private final String[] openBrowserCommand = new String[2];
  private final String[] openDotCommand = new String[2];
  private final String[] naryCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=nary-level", null};
  private List<String> tasks;

  private UserInterval userInterval;
  
  /**
   * Constructor for an analysis properties with default values.
   * 
   * @param id - task id of the analysis
   * @param modelFile - name of the model associated with the analysis.
   * @param root - directory of the project.
   * @param isGui - flag that indicates if the properties is created from the GUI.
   */
  public AnalysisProperties(String id, String modelFile, String root, boolean isGui)
  {
    this.id = id;
    this.modelFile = modelFile;
    this.root = root;
    this.gui = isGui;

    this.directory = root + File.separator + id;
    this.fileStem = "";
    this.filename = directory + File.separator + modelFile;
    if(modelFile != null)
    {
      this.propertiesFile = directory + File.separator + modelFile.replace(".xml", ".properties");
    }
    this.outDir = ".";
    this.method = SimMethod.ODE;
    this.abs = AbstractionMethod.NONE;
    this.userInterval = UserInterval.PRINT_INTERVAL;
    this.sim = "Runge-Kutta-Fehlberg (Hierarchical)";
    this.tasks = new ArrayList<String>();
    
    this.advProperties = new AdvancedProperties();
    this.incProperties = new IncrementalProperties();
    this.simProperties = new SimulationProperties();
    this.verifProperties = new VerificationProperties();
    
    this.addObservable(advProperties);
    this.addObservable(incProperties);
    this.addObservable(simProperties);
    this.addObservable(verifProperties);
  }

  /**
   * Getter for {@link AdvancedProperties}.
   * 
   * @return the advanced properties object.
   */
  public AdvancedProperties getAdvancedProperties()
  {
    return advProperties;
  }

  /**
   * Getter for {@link IncrementalProperties}.
   * 
   * @return the incremental properties object.
   */
  public IncrementalProperties getIncrementalProperties()
  {
    return incProperties;
  }

  /**
   * Getter for {@link SimulationProperties}.
   * 
   * @return the simulation properties object.
   */
  public SimulationProperties getSimulationProperties()
  {
    return simProperties;
  }

  /**
   * Getter for {@link VerificationProperties}.
   * 
   * @return the verification properties object.
   */
  public VerificationProperties getVerificationProperties()
  {
    return verifProperties;
  }


  /**
   * Getter for the associated SBML file name.
   * 
   * @return the name of the SBML file.
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Getter for the task id of the analysis.
   * 
   * @return the id of the analysis.
   */
  public String getId() {
    return id;
  }

  /**
   * Getter for the output directory.
   * 
   * @return the output directory path.
   */
  public String getOutDir() 
  {
    return outDir;
  }

  /**
   * Getter for the root directory of the associated task.
   * 
   * @return the root path.
   */
  public String getRoot() 
  {
    return root;
  }

  /**
   * Getter for the simulation name (e.g. rkf45, ode hierarchical, and ssa).
   * 
   * @return the simulation name.
   */
  public String getSim() 
  {
    return sim;
  }
  
  /**
   * Getter for simulation prop.
   * 
   * @return the simProp
   */
  public String getSimProp() 
  {
    return simProp;
  }

  /**
   * Check if the analysis properties is created from the GUI.
   * 
   * @return if the analysis properties is created from the GUI.
   */
  public boolean isGui()
  {
    return gui;
  }
  
  /**
   * Check if the analysis view is set to dot.
   * 
   * @return if analysis is dot.
   */
  public boolean isDot() 
  {
    return method == SimMethod.DOT;
  }

  /**
   * Check if the analysis view is set to FBA.
   * 
   * @return if analysis is fba.
   */
  public boolean isFba() 
  {
    return method == SimMethod.FBA;
  }

  /**
   * Check if the analysis view is set to labeled hybrid petri-net.
   * 
   * @return if analysis is lhpn.
   */
  public boolean isLhpn() 
  {
    return method == SimMethod.LHPN;
  }
  
  /**
   * Check if the analysis view is set to markov analysis.
   * 
   * @return if analysis is markov.
   */
  public boolean isMarkov() 
  {
    return method == SimMethod.MARKOV;
  }
  
  /**
   * Check if the analysis view is set to ODE.
   * 
   * @return if analysis is ODE.
   */
  public boolean isOde() 
  {
    return method == SimMethod.ODE;
  }
  
  /**
   * Check if the analysis view is set to generate SBML.
   * 
   * @return if analysis view is SBML.
   */
  public boolean isSbml() {
    return method == SimMethod.SBML;
  }
  
  /**
   * Check if the analysis view is set to SSA.
   * 
   * @return if analysis view is SSA.
   */
  public boolean isSsa() {
    return method == SimMethod.SSA;
  }
  
  /**
   * Check if the analysis view is set to XHTML report.
   * 
   * @return if analysis view is XHTML.
   */
  public boolean isXhtml() {
    return method == SimMethod.XHTML;
  }
  
  /**
   * Check if the abstraction is set to reaction-based abstraction.
   * 
   * @return if reaction-based abstraction should be applied.
   */
  public boolean isAbs() {
    return abs == AbstractionMethod.ABSTRACTION;
  }
  
  /**
   * Check if the abstraction method is set to expand reaction.
   * 
   * @return if reactions need to be expanded in the model.
   */
  public boolean isExpand() {
    return abs == AbstractionMethod.EXPAND;
  }

  /**
   * Check if the abstraction method is set to n-ary.
   * 
   * @return if abstraction is n-ary.
   */
  public boolean isNary() {
    return abs == AbstractionMethod.NARY;
  }
  
  /**
   * Check if the abstraction method is set to none.
   * 
   * @return if no abstraction should be applied to the model.
   */
  public boolean isNone() {
    return abs == AbstractionMethod.NONE;
  }
  
  
  
  /**
   * Check if the report interval is set to minimum print interval.
   * 
   * @return if report is determined by minimum print interval.
   */
  public boolean isMinPrintInterval()
  {
    return userInterval == UserInterval.MIN_PRINT_INTERVAL;
  }
  
  /**
   * Check if the report interval is determined by number of steps.
   * 
   * @return if the report is determined by number of steps.
   */
  public boolean isNumSteps()
  {
    return userInterval == UserInterval.NUM_STEPS;
  }
 
  /**
   * Check if the report is determined by a print interval.
   * 
   * @return if the report is determined by print interval.
   */
  public boolean isPrintInterval()
  {
    return userInterval == UserInterval.PRINT_INTERVAL;
  }

  /**
   * Set analysis view to use LHPN.
   */
  public void setLhpn() 
  {
    method = SimMethod.LHPN;
  }

  /**
   * Set analysis view to use Markov analysis.
   */
  public void setMarkov() 
  {
    method = SimMethod.MARKOV;
  }
  
  /**
   * Set analysis view to use ODE.
   */
  public void setOde() 
  {
    method = SimMethod.ODE;
  }
  
  /**
   * Set analysis view to use DOT.
   */
  public void setDot() 
  {
    method = SimMethod.DOT;
  }
  
  /**
   * Set analysis view to use FBA.
   */
  public void setFba() 
  {
    method = SimMethod.FBA;
  }

  /**
   * Set analysis view to use SBML generation.
   */
  public void setSbml() 
  {
    method = SimMethod.SBML;
  }
  
  /**
   * Set analysis method to use SSA.
   */
  public void setSsa() {
    method = SimMethod.SSA;
  }


  /**
   * Set analysis method to use XHTML.
   */
  public void setXhtml() {
    method = SimMethod.XHTML;
  }
  
  /**
   * Set abstraction method to use reaction-based abstraction.
   */
  public void setAbs() 
  {
    this.abs = AbstractionMethod.ABSTRACTION;
  }
  
  /**
   * Set abstraction method to use reaction expansion.
   */
  public void setExpand() 
  {
    this.abs = AbstractionMethod.EXPAND;
  }
  
  /**
   * Set abstraction method to use n-nary abstraction.
   */
  public void setNary() 
  {
    this.abs = AbstractionMethod.NARY;
  }
  
  /**
   * Set abstraction method to not use any method.
   */
  public void setNone() {
    this.abs = AbstractionMethod.NONE;
  }

  /**
   * Set report to use minimum print interval.
   */
  public void setMinPrintInterval()
  {
    userInterval = UserInterval.MIN_PRINT_INTERVAL;
  }
  
  /**
   * Set report to use print interval.
   */
  public void setPrintInterval()
  {
    userInterval = UserInterval.PRINT_INTERVAL;
  }

  /**
   * Set report number of steps.
   */
  public void setNumSteps()
  {
    userInterval = UserInterval.NUM_STEPS;
  }
  
  /**
   * Setter output directory.
   * 
   * @param the new path for the output directory.
   */
  public void setOutDir(String outDir) {
    this.outDir = outDir;
  }

  /**
   * Setter for the simulation name.
   * 
   * @param the name of the simulation method.
   */
  public void setSim(String sim) {
    this.sim = sim;
  }
  
  /**
   * Setter for the sim prop.
   * 
   * @param simProp 
   */
  public void setSimProp(String simProp) 
  {
    this.simProp = simProp;
  }

  /**
   * Set id of the analysis view. This should reflect in the directory name.
   * 
   * @param the new id.
   */
  public void setId(String id) {
    this.directory = root + File.separator + id;
    this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
    this.id = id;
  }


  /**
   * Set root directory. 
   * 
   * @param new path for the project root directory.
   */
  public void setRoot(String root) {
    this.root = root;
    this.directory = root + File.separator + id;
    this.filename = directory + File.separator + modelFile;
    this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
  }


  /**
   * Get name of the model.
   * 
   * @return the name of the model.
   */
  public String getModelFile() {
    return modelFile;
  }

  /**
   * Get directory of the analysis view.
   * 
   * @return the directory of the analysis view.
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * Set the name of the SBML model xml file.
   * 
   * @param name of the SBML model.
   */
  public void setModelFile(String modelFile) {
    this.filename = directory + File.separator + modelFile;
    this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
    this.modelFile = modelFile;
  }


  /**
   * Get file stem.
   * 
   * @return file stem.
   */
  public String getFileStem() {
    return fileStem;
  }

  /**
   * Set file stem.
   * 
   * @param file stem.
   */
  public void setFileStem(String fileStem) {
    if(fileStem.equals(""))
    {
      this.fileStem = "";
      this.directory = root + File.separator + id;
      this.filename = directory + File.separator + modelFile;
      this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
    }
    else
    {
      this.fileStem = fileStem;
      this.directory = root + File.separator + id + File.separator + fileStem ;
      this.filename = directory + File.separator + modelFile;
      this.propertiesFile = root + File.separator + id + File.separator + fileStem + File.separator + modelFile.replace(".xml", ".properties");
    }
   
  }

  /**
   * Get properties file name.
   * 
   * @return the path to the properties file.
   */
  public String getPropertiesFilename() {
    return propertiesFile;
  }

  /**
   * Get the list of tasks.
   * 
   * @return list of tasks.
   */
  public List<String> getListOfTasks()
  {
    return tasks;
  }

  /**
   * Add a task.
   * 
   * @param task to be added.
   */
  public void addTask(String task)
  {
    tasks.add(task);
  }

  /**
   * Get command for process to open a dot file.
   * 
   * @return  command to open dot.
   */
  public String[] getOpenDotCommand()
  {
    String out = filename.replace(".xml", ".dot");
    
    if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
    {
      openDotCommand[0] = "open";
    }
    else
    {
      openDotCommand[0] = "dotty";
    }
    
    openDotCommand[1] = out;
    
    return openDotCommand;
  }
  
  /**
   * Get command to open a xhtml file on a browser.
   * 
   * @return command to open a xhtml file.
   */
  public String[] getOpenBrowserCommand()
  {
    String out = filename.replace(".xml", ".xhtml");
    Preferences biosimrc = Preferences.userRoot();
    String xhtmlCmd = biosimrc.get("biosim.general.browser", "");
    
    
    openBrowserCommand[0] = xhtmlCmd;
    openBrowserCommand[1] = out;
    
    return openBrowserCommand;
  }
  
  /**
   * Get command that generates SBML files.
   * 
   * @param the name of the new SBML file.
   * @return command to generate SBML.
   */
  public String[] getSbmlCommand(String newName)
  {

    String out = "--out=" + ".." + File.separator + newName;
    
    sbmlCommand[2] = out;
    sbmlCommand[3] = filename;
    
    return sbmlCommand;
  }
  
  /**
   * Get command to generate xhtml report.
   * 
   * @return command to generate xhtml.
   */
  public String[] getXhtmlCommand()
  {

    String out = "--out=" + modelFile.replace(".xml", ".xhtml");
    
    xhtmlCommand[2] = out;
    xhtmlCommand[3] = filename;
    
    return xhtmlCommand;
  }
  
  /**
   * Get command to generate dot report.
   * 
   * @return command to generate dot file.
   */
  public String[] getDotCommand()
  {

    String out = "--out=" + modelFile.replace(".xml", ".dot");
    
    dotCommand[2] = out;
    dotCommand[3] = filename;
    
    return dotCommand;
  }
  
  /**
   * Get command to generate reb2sac simulation.
   * 
   * @return command for reb2sac simulation.
   */
  public String[] getReb2sacCommand(String filename)
  {
    reb2sacCommand[1] = "--target.encoding=" + sim;
    reb2sacCommand[2] = "--reb2sac.properties.file=" + this.getPropertiesFilename();
    reb2sacCommand[3] = filename;
    return reb2sacCommand;
  }
  
  /**
   * Get command for hse2.
   * 
   * @return hse2 command.
   */
  public String[] getHse2Command()
  {
    hse2Command[2] = filename;
    return hse2Command;
  }
  
  /**
   * Get command for atacs.
   * 
   * @return command for atacs.
   */
  public String[] getAtacsCommand()
  {
    atacsCommand[3] = filename;
    return atacsCommand;
  }
  
  /**
   * Get command for nary analysis.
   * 
   * @return command for nary.
   */
  public String[] getNaryCommand()
  {
    naryCommand[2] = filename;
    return naryCommand;
  }

}
