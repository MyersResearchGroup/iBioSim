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

public final class AnalysisProperties {


  private static enum UserInterval{
    MIN_PRINT_INTERVAL, NUM_STEPS, PRINT_INTERVAL;
  }	

  private static enum SimMethod{
    ODE, SSA, MARKOV, FBA, SBML, DOT, XHTML, LHPN;
  } 

  private static enum AbstractionMethod{
    NONE, EXPAND, ABSTRACTION, NARY;
  }

  private String filename, id, root, directory, outDir, modelFile, propertiesFile;
  private String        sim;
  private String        simProp;
  private String fileStem;
  
  
  private SimMethod method;
  private AbstractionMethod abs;
  private final AdvancedProperties advProperties;
  private final IncrementalProperties incProperties;
  private final SimulationProperties simProperties;
  private final VerificationProperties verifProperties;
  private final boolean gui;

  private final String[] hse2Command = new String[]{Executables.reb2sacExecutable, "--target.encoding=hse2", null};
  private final String[] atacsCommand = new String[]{"atacs", "-T0.000001", "-oqoflhsgllvA", null, "out.hse"};
  private final String[] sbmlCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=sbml", null, null};
  private final String[] reb2sacCommand = new String[]{Executables.reb2sacExecutable, null,null };
  private final String[] dotCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=dot",  null, null};
  private final String[] xhtmlCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=xhtml", null, null};
  private final String[] openBrowserCommand = new String[2];
  private final String[] openDotCommand = new String[2];
  private final String[] naryCommand = new String[]{Executables.reb2sacExecutable, "--target.encoding=nary-level", null};
  private List<String> tasks;

  private UserInterval userInterval;
  
  public AnalysisProperties(String id, String modelFile, String root, boolean isGui)
  {
    this.id = id;
    this.modelFile = modelFile;
    this.root = root;
    this.gui = isGui;

    this.directory = root + File.separator + id;
    this.fileStem = "";
    this.filename = directory + File.separator + modelFile;
    this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
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
  }

  public AdvancedProperties getAdvancedProperties()
  {
    return advProperties;
  }

  public IncrementalProperties getIncrementalProperties()
  {
    return incProperties;
  }

  public SimulationProperties getSimulationProperties()
  {
    return simProperties;
  }

  public VerificationProperties getVerificationProperties()
  {
    return verifProperties;
  }


  /**
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }




  /**
   * @return the outDir
   */
  public String getOutDir() {
    return outDir;
  }




  /**
   * @return the root
   */
  public String getRoot() {
    return root;
  }

  /**
   * @return the sim
   */
  public String getSim() {
    return sim;
  }
  /**
   * @return the simProp
   */
  public String getSimProp() {
    return simProp;
  }

  /**
   * @return the abs
   */
  public boolean isAbs() {
    return abs == AbstractionMethod.ABSTRACTION;
  }

  /**
   * @return the dot
   */
  public boolean isDot() {
    return method == SimMethod.DOT;
  }
  /**
   * @return the expand
   */
  public boolean isExpand() {
    return abs == AbstractionMethod.EXPAND;
  }
  /**
   * @return the fba
   */
  public boolean isFba() {
    return method == SimMethod.FBA;
  }

  public boolean isGui()
  {
    return gui;
  }

  /**
   * @return the lhpn
   */
  public boolean isLhpn() {
    return method == SimMethod.LHPN;
  }
  /**
   * @return the markov
   */
  public boolean isMarkov() {
    return method == SimMethod.MARKOV;
  }

  public boolean isMinPrintInterval()
  {
    return userInterval == UserInterval.MIN_PRINT_INTERVAL;
  }

  /**
   * @return the nary
   */
  public boolean isNary() {
    return abs == AbstractionMethod.NARY;
  }
  /**
   * @return the none
   */
  public boolean isNone() {
    return abs == AbstractionMethod.NONE;
  }
  public boolean isNumSteps()
  {
    return userInterval == UserInterval.NUM_STEPS;
  }
  /**
   * @return the ode
   */
  public boolean isOde() {
    return method == SimMethod.ODE;
  }
  public boolean isPrintInterval()
  {
    return userInterval == UserInterval.PRINT_INTERVAL;
  }
  /**
   * @return the sbml
   */
  public boolean isSbml() {
    return method == SimMethod.SBML;
  }
  /**
   * @return the ssa
   */
  public boolean isSsa() {
    return method == SimMethod.SSA;
  }
  /**
   * @return the xhtml
   */
  public boolean isXhtml() {
    return method == SimMethod.XHTML;
  }
  /**
   * @param abs the abs to set
   */
  public void setAbs() {
    this.abs = AbstractionMethod.ABSTRACTION;
  }


  /**
   * 
   */
  public void setDot() {
    method = SimMethod.DOT;
  }
  /**
   * @param expand the expand to set
   */
  public void setExpand() {
    this.abs = AbstractionMethod.EXPAND;
  }
  /**
   */
  public void setFba() {
    method = SimMethod.FBA;
  }





  /**
   * @param lhpn the lhpn to set
   */
  public void setLhpn() {
    method = SimMethod.LHPN;
  }



  /**
   *
   */
  public void setMarkov() {
    method = SimMethod.MARKOV;
  }



  public void setMinPrintInterval()
  {
    userInterval = UserInterval.MIN_PRINT_INTERVAL;
  }


  /**
   * @param nary the nary to set
   */
  public void setNary() {
    this.abs = AbstractionMethod.NARY;
  }
  /**
   * @param none the none to set
   */
  public void setNone() {
    this.abs = AbstractionMethod.NONE;
  }

  public void setNumSteps()
  {
    userInterval = UserInterval.NUM_STEPS;
  }
  /**
   */
  public void setOde() {
    method = SimMethod.ODE;
  }
  /**
   * @param outDir the outDir to set
   */
  public void setOutDir(String outDir) {
    this.outDir = outDir;
  }

  public void setPrintInterval()
  {
    userInterval = UserInterval.PRINT_INTERVAL;
  }

  /**
   */
  public void setSbml() {
    method = SimMethod.SBML;
  }
  /**
   * @param sim the sim to set
   */
  public void setSim(String sim) {
    this.sim = sim;
  }
  /**
   * @param simProp the simProp to set
   */
  public void setSimProp(String simProp) {
    this.simProp = simProp;
  }
  /**
   *
   */
  public void setSsa() {
    method = SimMethod.SSA;
  }


  /**
   * @param xhtml the xhtml to set
   */
  public void setXhtml() {
    method = SimMethod.XHTML;
  }





  /**
   * @return the method
   */
  public SimMethod getMethod() {
    return method;
  }


  /**
   * @param method the method to set
   */
  public void setMethod(SimMethod method) {
    this.method = method;
  }


  /**
   * @return the userInterval
   */
  public UserInterval getUserInterval() {
    return userInterval;
  }


  /**
   * @param userInterval the userInterval to set
   */
  public void setUserInterval(UserInterval userInterval) {
    this.userInterval = userInterval;
  }


  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.directory = root + File.separator + id;
    this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
    this.id = id;
  }


  /**
   * @param root the root to set
   */
  public void setRoot(String root) {
    this.root = root;
    this.directory = root + File.separator + id;
    this.filename = directory + File.separator + modelFile;
    this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
  }


  /**
   * @return the modelFile
   */
  public String getModelFile() {
    return modelFile;
  }

  /**
   * @return the directory
   */
  public String getDirectory() {
    return directory;
  }

  /**
   * @param modelFile the modelFile to set
   */
  public void setModelFile(String modelFile) {
    this.filename = directory + File.separator + modelFile;
    this.propertiesFile = root + File.separator + id + File.separator + modelFile.replace(".xml", ".properties");
    this.modelFile = modelFile;
  }


  /**
   * 
   * @return
   */
  public String getFileStem() {
    return fileStem;
  }

  /**
   * 
   * @param fileStem
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
   * 
   * @return
   */
  public String getPropertiesName() {
    return propertiesFile;
  }

  /**
   * 
   * @return
   */
  public List<String> getListOfTasks()
  {
    return tasks;
  }

  /**
   * 
   * @param task
   */
  public void addTask(String task)
  {
    tasks.add(task);
  }

  /**
   * 
   * @return
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
   * 
   * @return
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
   * 
   * @param newName
   * @return
   */
  public String[] getSbmlCommand(String newName)
  {

    String out = "--out=" + ".." + File.separator + newName;
    
    sbmlCommand[2] = out;
    sbmlCommand[3] = filename;
    
    return sbmlCommand;
  }
  
  /**
   * 
   * @return
   */
  public String[] getXhtmlCommand()
  {

    String out = "--out=" + modelFile.replace(".xml", ".xhtml");
    
    xhtmlCommand[2] = out;
    xhtmlCommand[3] = filename;
    
    return xhtmlCommand;
  }
  
  /**
   * 
   * @return
   */
  public String[] getDotCommand()
  {

    String out = "--out=" + modelFile.replace(".xml", ".dot");
    
    dotCommand[2] = out;
    dotCommand[3] = filename;
    
    return dotCommand;
  }
  
  /**
   * 
   * @return
   */
  public String[] getReb2sacCommand()
  {
    reb2sacCommand[1] = "--target.encoding=" + sim;
    reb2sacCommand[2] = filename;
    return reb2sacCommand;
  }
  
  /**
   * 
   * @return
   */
  public String[] getHse2Command()
  {
    hse2Command[2] = filename;
    return hse2Command;
  }
  
  /**
   * 
   * @return
   */
  public String[] getAtacsCommand()
  {
    atacsCommand[3] = filename;
    return atacsCommand;
  }
  
  /**
   * 
   * @return
   */
  public String[] getNaryCommand()
  {
    naryCommand[2] = filename;
    return naryCommand;
  }

}
