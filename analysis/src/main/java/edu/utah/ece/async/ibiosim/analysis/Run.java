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
package edu.utah.ece.async.ibiosim.analysis;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import edu.utah.ece.async.ibiosim.analysis.fba.FluxBalanceAnalysis;
import edu.utah.ece.async.ibiosim.analysis.markov.BuildStateGraphThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformSteadyStateMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformTransientMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph.Property;
import edu.utah.ece.async.ibiosim.analysis.properties.AdvancedProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation.SimulationType;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.Simulator;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.MutableString;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObservable;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;
import edu.utah.ece.async.lema.verification.lpn.Abstraction;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Translator;

/**
 * Run class is used to execute an analysis method.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public class Run extends CoreObservable implements ActionListener {

  private final AnalysisProperties properties;
  private FileWriter logFile;

  private final Message message = new Message();
  private final Runtime exec = Runtime.getRuntime();

  private Process reb2sac;
  private StateGraph sg;
  private DynamicSimulation dynSim;

  private File work;

  /**
   * The Run class is a wrapper for all of the analysis methods. The run configurations are
   * specified by an {@link AnalysisProperties}, which describes the type of analysis to run
   * and the simulation parameters.
   *
   * @param properties
   *          - the analysis properties object that stores the simulation parameters.
   */
  public Run(AnalysisProperties properties) {
    this.properties = properties;
  }

  /**
   * When Run is executed, it loads up the parameters from {@link AnalysisProperties} and performs
   * analysis.
   *
   * @return error code. Value 0 indicates a success run.
   *
   * @throws IOException
   *           - io problem
   * @throws XMLStreamException
   *           - problem with the XML processing.
   * @throws InterruptedException
   *           - thred problem
   * @throws BioSimException
   *           - something went wrong with the analysis
   */
  public int execute(String outDir, String filename) throws IOException, XMLStreamException, InterruptedException, BioSimException {
    Runtime exec = Runtime.getRuntime();
    int exitValue = 255;
    long time1, time2;

    work = new File(outDir);
    new FileWriter(new File(outDir + File.separator + "running")).close();

    if (properties.isNary() && properties.isGui() && (properties.isSsa() || properties.isXhtml() || properties.isDot())) {
      exitValue = executeNary(filename);
    }

    if (properties.isNary() && !properties.isGui() && !properties.getSim().contains("markov-chain-analysis") && properties.getSimulationProperties().getRun() == 1) {
      message.setLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=nary-level " + "\"" + filename + "\"");
      this.notifyObservers(message);
      time1 = System.nanoTime();
      reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=nary-level " + "\"" + filename + "\"", Executables.envp, work);
    } else if (properties.isFba()) {
      time1 = System.nanoTime();
      exitValue = executeFBA(filename);
    } else if (properties.isSbml()) {
      time1 = System.nanoTime();
      exitValue = executeSBML(filename);
    } else if (properties.isDot()) {
      time1 = System.nanoTime();
      exitValue = executeDot(filename);
    } else if (properties.isXhtml()) {
      time1 = System.nanoTime();
      exitValue = executeXhtml(filename);
    } else if (properties.getSim().equals("atacs")) {
      time1 = System.nanoTime();
      exitValue = executeAtacs();
    } else if (properties.getSim().equals("prism")) {
      time1 = System.nanoTime();
      exitValue = executePrism(filename);
    } else if (properties.getSim().contains("markov-chain-analysis") || properties.getSim().equals("reachability-analysis")) {
      time1 = System.nanoTime();
      exitValue = executeMarkov(filename);
    } else {
      time1 = System.nanoTime();
      exitValue = executeSimulation(filename);

    }

    // updateReb2SacProgress();
    time2 = System.nanoTime();
    displayError(exitValue);

    String time = SBMLutilities.createTimeString(time1, time2);

    message.setLog("Total Simulation Time: " + time);
    this.notifyObservers(message);
    return exitValue;
  }

  private void displayError(int exitValue) {
    if (exitValue == 143) {
      message.setDialog("Canceled Simulation", "The simulation was" + " canceled by the user.");
      this.notifyObservers(message);
    } else if (exitValue == 139) {
      message.setErrorDialog("Not An SBML File", "The selected model is not a valid sbml file." + "\nYou must select an sbml file.");
      this.notifyObservers(message);
    } else if (exitValue != 0) {
      message.setErrorDialog("Error In Execution!", "Bad Return Value!\n" + "The reb2sac program returned " + exitValue + " as an exit value.");
      this.notifyObservers(message);
    }
  }

  private int executeFBA(String filename) throws XMLStreamException, IOException, BioSimException {
    int exitValue = 255;

    FluxBalanceAnalysis fluxBalanceAnalysis = new FluxBalanceAnalysis(properties.getDirectory() + File.separator, properties.getModelFile(), properties.getSimulationProperties().getAbsError());
    this.addObservable(fluxBalanceAnalysis);
    exitValue = fluxBalanceAnalysis.PerformFluxBalanceAnalysis();

    if (exitValue == 1) {
      message.setErrorDialog("Error", "Flux balance analysis did not converge.");
    } else if (exitValue == 2) {
      message.setErrorDialog("Error", "Flux balance analysis failed.");
    } else if (exitValue == -1) {
      message.setErrorDialog("Error", "No flux balance constraints.");
    } else if (exitValue == -2) {
      message.setErrorDialog("Error", "Initial point must be strictly feasible.");
    } else if (exitValue == -3) {
      message.setErrorDialog("Error", "The FBA problem is infeasible.");
    } else if (exitValue == -4) {
      message.setErrorDialog("Error", "The FBA problem has a singular KKT system.");
    } else if (exitValue == -5) {
      message.setErrorDialog("Error", "The matrix must have at least one row.");
    } else if (exitValue == -6) {
      message.setErrorDialog("Error", "The matrix is singular.");
    } else if (exitValue == -7) {
      message.setErrorDialog("Error", "Equalities matrix A must have full rank.");
    } else if (exitValue == -8) {
      message.setErrorDialog("Error", "Miscellaneous FBA failure (see console for details).");
    } else if (exitValue == -9) {
      message.setErrorDialog("Error", "Reaction in flux objective does not have a flux bound.");
    } else if (exitValue == -10) {
      message.setErrorDialog("Error", "All reactions must have flux bounds for FBA.");
    } else if (exitValue == -11) {
      message.setErrorDialog("Error", "No flux objectives.");
    } else if (exitValue == -12) {
      message.setErrorDialog("Error", "No active flux objective.");
    } else if (exitValue == -13) {
      message.setErrorDialog("Error", "Unknown flux balance analysis error.");
    }
    if (exitValue != 0) {
      this.notifyObservers(message);
    }
    return exitValue;
  }

  private int executeMarkov(String filename) throws XMLStreamException, IOException, InterruptedException, BioSimException {
    String prop = null;
    LPN lhpnFile = null;
    String root = properties.getRoot();
    String sim = properties.getSim();
    if (filename.contains(".lpn")) {
      lhpnFile = new LPN();
      lhpnFile.load(filename);
    } else {
      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
      ArrayList<String> specs = new ArrayList<>();
      ArrayList<Object[]> conLevel = new ArrayList<>();
      retrieveSpeciesAndConLevels(specs, conLevel);

      BioModel bioModel = BioModel.createBioModel(properties.getDirectory(), this);
      bioModel.load(filename);
      if (bioModel.flattenModel(true) != null) {
        if (properties.getVerificationProperties().getLpnProperty() != null && !properties.getVerificationProperties().getLpnProperty().equals("")) {
          prop = properties.getVerificationProperties().getLpnProperty();
        } else {
          prop = properties.getVerificationProperties().getConstraintProperty();
        }

        // TODO: this is temporary hack
        // Just takes first, but should be chosen from list
        if (sim.equals("transient-markov-chain-analysis")) {
          if (prop == null) {
            Model m = bioModel.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++) {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U(")) {
                prop = Translator.convertProperty(m.getConstraint(num).getMath());
                break;
              }
            }
          }
        }

        MutableString mutProp = new MutableString(prop);
        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
        prop = mutProp.getString();
        if (lhpnFile == null) {
          new File(properties.getDirectory() + File.separator + "running").delete();
          return 0;
        }
        lhpnFile.save(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
        this.message.setLog("Saving SBML file as LPN:\n" + filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
      } else {
        new File(properties.getDirectory() + File.separator + "running").delete();
        return 0;
      }
    }
    sg = new StateGraph(lhpnFile);
    this.addObservable(sg);
    BuildStateGraphThread buildStateGraph = new BuildStateGraphThread(sg, null);
    buildStateGraph.start();
    buildStateGraph.join();
    message.setLog("Number of states found: " + sg.getNumberOfStates());
    this.notifyObservers(message);
    message.setLog("Number of transitions found: " + sg.getNumberOfTransitions());
    this.notifyObservers(message);
    message.setLog("Memory used during state exploration: " + sg.getMemoryUsed() + "MB");
    this.notifyObservers(message);
    message.setLog("Total memory used: " + sg.getTotalMemoryUsed() + "MB");
    this.notifyObservers(message);

    if (sim.equals("reachability-analysis") && !sg.getStop()) {
      message.setDialog("View State Graph", "Do you want to view the state graph?");
      if (parent.request(RequestType.REQUEST_BOOLEAN, message)) {
        if (message.getBoolean()) {
          viewStateGraph(filename, properties.getModelFile(), properties.getDirectory(), sg);
        }
      }
    } else if (sim.equals("steady-state-markov-chain-analysis")) {
      if (!sg.getStop()) {
        message.setLog("Performing steady state Markov chain analysis.");
        PerformSteadyStateMarkovAnalysisThread performMarkovAnalysis = new PerformSteadyStateMarkovAnalysisThread(sg, null);
        if (filename.contains(".lpn")) {
          performMarkovAnalysis.start(properties.getSimulationProperties().getAbsError(), null);
        } else {
          BioModel gcm = BioModel.createBioModel(root, this);
          gcm.load(filename);
          ArrayList<Property> propList = new ArrayList<>();
          if (prop == null) {
            Model m = gcm.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++) {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (constraint.startsWith("St(")) {
                propList.add(sg.createProperty(m.getConstraint(num).getMetaId(), Translator.convertProperty(m.getConstraint(num).getMath())));
              }
            }
          }
          performMarkovAnalysis.start(properties.getSimulationProperties().getAbsError(), propList);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop()) {
          String simrep = sg.getMarkovResults();
          if (simrep != null) {
            FileOutputStream simrepstream = new FileOutputStream(new File(properties.getDirectory() + File.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          sg.outputTSD(properties.getDirectory() + File.separator + "percent-term-time.tsd");
          message.setDialog("View State Graph", "Do you want to view the state graph?");
          if (parent.request(RequestType.REQUEST_BOOLEAN, message)) {
            if (message.getBoolean()) {
              viewStateGraph(filename, properties.getModelFile(), properties.getDirectory(), sg);
            }
          }
        }
      }
    } else if (sim.equals("transient-markov-chain-analysis")) {
      if (!sg.getStop()) {
        message.setLog("Performing transient Markov chain analysis with uniformization.");
        this.notifyObservers(message);
        PerformTransientMarkovAnalysisThread performMarkovAnalysis = new PerformTransientMarkovAnalysisThread(sg, null);
        if (prop != null) {
          String[] condition;

          condition = Translator.getProbpropParts(Translator.getProbpropExpression(prop));
          boolean globallyTrue = false;
          if (prop.contains("PF")) {
            condition[0] = "true";
          } else if (prop.contains("PG")) {
            condition[0] = "true";
            globallyTrue = true;
          }
          performMarkovAnalysis.start(properties.getSimulationProperties().getTimeLimit(), properties.getSimulationProperties().getMaxTimeStep(), properties.getSimulationProperties().getPrintInterval(), properties.getSimulationProperties().getAbsError(), condition, globallyTrue);

        } else {
          performMarkovAnalysis.start(properties.getSimulationProperties().getTimeLimit(), properties.getSimulationProperties().getMaxTimeStep(), properties.getSimulationProperties().getPrintInterval(), properties.getSimulationProperties().getAbsError(), null, false);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop()) {
          String simrep = sg.getMarkovResults();
          if (simrep != null) {
            FileOutputStream simrepstream = new FileOutputStream(new File(properties.getDirectory() + File.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          sg.outputTSD(properties.getDirectory() + File.separator + "percent-term-time.tsd");
          message.setDialog("View State Graph", "Do you want to view the state graph?");
          if (parent.request(RequestType.REQUEST_BOOLEAN, message)) {
            if (message.getBoolean()) {
              viewStateGraph(filename, properties.getModelFile(), properties.getDirectory(), sg);
            }
          }
        }
      }
    }
    return 0;
  }

  private int executeNary(String filename) throws XMLStreamException, IOException, BioSimException {
    String modelFile = properties.getModelFile();
    String directory = properties.getDirectory();
    String simName = properties.getSim();
    String root = properties.getRoot();
    String lpnProperty = properties.getVerificationProperties().getLpnProperty();
    String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
    ArrayList<String> specs = new ArrayList<>();
    ArrayList<Object[]> conLevel = new ArrayList<>();
    retrieveSpeciesAndConLevels(specs, conLevel);
    BioModel bioModel = BioModel.createBioModel(properties.getRoot(), this);
    // TODO: check
    bioModel.load(root + File.separator + modelFile);
    String prop = null;
    if (!lpnProperty.equals("")) {
      prop = lpnProperty;
    }
    MutableString mutProp = new MutableString(prop);
    LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
    prop = mutProp.getString();
    if (lpnFile == null) {
      new File(directory + File.separator + "running").delete();
      // logFile.close();
      return 0;
    }
    lpnFile.save(root + File.separator + simName + File.separator + lpnName);
    try {
      Translator t1 = new Translator();
      if (properties.isAbs()) {
        LPN lhpnFile = new LPN();
        lhpnFile.load(root + File.separator + simName + File.separator + lpnName);
        Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
        abst.abstractSTG(false);
        abst.save(root + File.separator + simName + File.separator + lpnName + ".temp");
        t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName + ".temp", prop);
      } else {
        t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName, prop);
      }
      t1.setFilename(root + File.separator + simName + File.separator + lpnName.replace(".lpn", ".xml"));
      t1.outputSBML();
    }
    catch (BioSimException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private int executeSimulation(String filename) throws IOException, InterruptedException, XMLStreamException, BioSimException {
    int exitValue = 0;
    String SBMLFileName = filename;
    String[] env = Executables.envp;
    String sim = properties.getSim();

    boolean runJava = true;
    if (sim.equals("SSA-CR (Dynamic)")) {
      dynSim = new DynamicSimulation(SimulationType.CR);
      dynSim.addObservable(this);
    } else if (sim.equals("SSA-Direct (Dynamic)")) {
      dynSim = new DynamicSimulation(SimulationType.DIRECT);
      dynSim.addObservable(this);
    } else if (sim.equals("SSA-Direct (Flatten)")) {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
      dynSim.addObservable(this);
    } else if (sim.equals("SSA-Direct (Hierarchical)")) {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
      dynSim.addObservable(this);
    } else if (sim.equals("Mixed-Hierarchical")) {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_MIXED);
      dynSim.addObservable(this);
    } else if (sim.equals("Hybrid-Hierarchical")) {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_HYBRID);
      dynSim.addObservable(this);
    } else if (sim.equals("Runge-Kutta-Fehlberg (Dynamic)")) {
      dynSim = new DynamicSimulation(SimulationType.RK);
      dynSim.addObservable(this);
    } else if (sim.equals("Runge-Kutta-Fehlberg (Hierarchical)")) {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
      dynSim.addObservable(this);
    } else if (sim.equals("Runge-Kutta-Fehlberg (Flatten)")) {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
      dynSim.addObservable(this);
    } else if (sim.equals("atacs")) {
      exitValue = executeAtacs();
    } else if (sim.equals("prism")) {
      exitValue = executePrism(filename);
    } else if (sim.contains("markov-chain-analysis") || sim.equals("reachability-analysis")) {
      exitValue = executeMarkov(filename);
    } else {
      Simulator.expandArrays(SBMLFileName, properties.getAdvancedProperties().getStoichAmp());
      runJava = false;
    }

    if (runJava) {
      dynSim.simulate(properties, filename);

      new File(properties.getDirectory() + File.separator + "running").delete();
    } else {
      String[] command = properties.getReb2sacCommand(filename);

      message.setLog("Executing:\n" + SBMLutilities.commandString(command) + "\n");

      this.notifyObservers(message);

      reb2sac = exec.exec(command, env, work);
      ReadStream s1 = new ReadStream("stdin", reb2sac.getInputStream ());
      ReadStream s2 = new ReadStream("stderr", reb2sac.getErrorStream ());
      s1.start ();
      s2.start ();
      //updateReb2SacProgress();

      exitValue = reb2sac.waitFor();
    }
    return exitValue;
  }

  private int executeAtacs() throws IOException, InterruptedException {
    int exitValue = 0;

    String[] command = properties.getHse2Command();

    message.setLog("Executing:\n" + SBMLutilities.commandString(command));
    reb2sac = exec.exec(command, Executables.envp, work);

    command = properties.getAtacsCommand();

    message.setLog("Executing:\n" + SBMLutilities.commandString(command));
    this.notifyObservers(message);
    exec.exec(command, null, work);

    if (reb2sac != null) {
      exitValue = reb2sac.waitFor();
    }

    return exitValue;
  }

  private int executePrism(String filename) throws IOException, InterruptedException, XMLStreamException, BioSimException {
    int exitValue = 255;
    String prop = null;
    String directory = properties.getDirectory() + File.separator;
    String out = properties.getModelFile().replace(".xml", "").replaceAll(".lpn", "").replaceAll(".sbml", "");
    LPN lhpnFile = null;
    // String root = properties.getRoot();
    if (filename.contains(".lpn")) {
      lhpnFile = new LPN();
      lhpnFile.load(properties.getRoot() + File.separator + filename);
    } else {
      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
      ArrayList<String> specs = new ArrayList<>();
      ArrayList<Object[]> conLevel = new ArrayList<>();
      retrieveSpeciesAndConLevels(specs, conLevel);
      BioModel bioModel = BioModel.createBioModel(properties.getDirectory(), this);
      bioModel.load(filename);
      if (bioModel.flattenModel(true) != null) {
        if (properties.getVerificationProperties().getLpnProperty() != null && !properties.getVerificationProperties().getLpnProperty().equals("")) {
          prop = properties.getVerificationProperties().getLpnProperty();
        } else {
          prop = properties.getVerificationProperties().getConstraintProperty();
        }
        MutableString mutProp = new MutableString(prop);
        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
        prop = mutProp.getString();
        if (lhpnFile == null) {
          new File(properties.getDirectory() + File.separator + "running").delete();
          return 0;
        }
        message.setLog("Saving SBML file as PRISM file:\n" + filename.replace(".xml", ".prism"));
        this.notifyObservers(message);
        message.setLog("Saving PRISM Property file:\n" + filename.replace(".xml", ".pctl"));
        this.notifyObservers(message);
        LPN.convertLPN2PRISM(logFile, lhpnFile, filename.replace(".xml", ".prism"), bioModel.getSBMLDocument());
        Preferences biosimrc = Preferences.userRoot();
        String prismCmd = biosimrc.get("biosim.general.prism", "");
        if (prismCmd.contains("$prism")) {
          prismCmd = prismCmd.replace("$prism", directory + out + ".prism");
        } else {
          prismCmd = prismCmd + " " + directory + out + ".prism";
        }
        if (prismCmd.contains("$pctl")) {
          prismCmd = prismCmd.replace("$pctl", directory + out + ".pctl");
        } else {
          prismCmd = prismCmd + " " + directory + out + ".pctl";
        }
        message.setLog("Executing:\n" + prismCmd);
        this.notifyObservers(message);
        reb2sac = exec.exec(prismCmd, null, work);
        String error = "", result = "";
        try {
          InputStream reb = reb2sac.getInputStream();
          InputStreamReader isr = new InputStreamReader(reb);
          BufferedReader br = new BufferedReader(isr);
          String line;
          while ((line = br.readLine()) != null) {
            if (line.startsWith("Result")) {
              result = line + '\n';
            }
          }
          InputStream reb2 = reb2sac.getErrorStream();
          int read = reb2.read();
          while (read != -1) {
            error += (char) read;
            read = reb2.read();
          }
          br.close();
          isr.close();
          reb.close();
          reb2.close();
        }
        catch (Exception e) {
          // e.printStackTrace();
        }
        if (reb2sac != null) {
          exitValue = reb2sac.waitFor();
        }
        if (!error.equals("")) {
          message.setLog("Errors:\n" + error + "\n");

          this.notifyObservers(message);
        } else if (!result.equals("")) {
          message.setLog(result);
          this.notifyObservers(message);
        } else {
          throw new BioSimException("Verification Failed!", "Verification could not be executed. Something went wrong.");
        }

        exitValue = 0;
      } else {
        new File(directory + File.separator + "running").delete();
        logFile.close();
        exitValue = 0;
      }
    }

    if (reb2sac != null) {
      exitValue = reb2sac.waitFor();
    }

    return exitValue;
  }

  private int executeDot(String filename) throws IOException, InterruptedException, BioSimException {
    int exitValue = 255;
    String[] command;
    String outputFileName = filename.replace(".sbml", "").replace(".xml", "") + ".dot";
    if (properties.isNary()) {
      LPN lhpnFile = new LPN();
      lhpnFile.load(filename.replace(".sbml", "").replace(".xml", "") + ".lpn");
      lhpnFile.printDot(outputFileName);
      exitValue = 0;
    } else if (filename.contains(".lpn")) {
      LPN lhpnFile = new LPN();
      try {
        lhpnFile.load(filename);
      }
      catch (BioSimException e) {
        e.printStackTrace();
      }
      if (properties.isAbs()) {
        Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
        abst.abstractSTG(false);
        abst.printDot(properties.getRoot() + File.separator + properties.getSim() + File.separator + properties.getModelFile());
      } else {
        lhpnFile.printDot(properties.getRoot() + File.separator + properties.getSim() + File.separator + properties.getModelFile());
      }
      exitValue = 0;
    } else {
      command = properties.getDotCommand();
      message.setLog("Executing:\n" + SBMLutilities.commandString(command));
      this.notifyObservers(message);
      reb2sac = exec.exec(command, Executables.envp, work);
    }

    if (reb2sac != null) {
      exitValue = reb2sac.waitFor();
    }

    if (exitValue == 0) {
      command = properties.getOpenDotCommand();

      exec.exec(command);
      message.setLog("Executing:\n" + SBMLutilities.commandString(command));
      this.notifyObservers(message);
    }

    return exitValue;
  }

  private int executeXhtml(String filename) throws IOException, InterruptedException {
    int exitValue = 0;
    Simulator.expandArrays(filename, 1);

    String[] command = properties.getXhtmlCommand();
    message.setLog("Executing:\n" + SBMLutilities.commandString(command));
    reb2sac = exec.exec(command, Executables.envp, work);

    if (reb2sac != null) {
      exitValue = reb2sac.waitFor();
    }

    command = properties.getOpenBrowserCommand();

    message.setLog("Executing:\n" + SBMLutilities.commandString(command));
    this.notifyObservers(message);
    exec.exec(command, null, work);

    return exitValue;
  }

  private int executeSBML(String filename) throws IOException, HeadlessException, XMLStreamException, InterruptedException, BioSimException {
    int exitValue = 255;

    String root = properties.getRoot();
    String modelFile = properties.getModelFile();
    String simName = properties.getSim();

    if (!parent.request(RequestType.REQUEST_STRING, message)) { return exitValue; }
    String sbmlName = message.getMessage();

    if (sbmlName != null && !sbmlName.trim().equals("")) {
      sbmlName = sbmlName.trim();
      if (!sbmlName.endsWith(".xml")) {
        sbmlName += ".xml";
      }

      File f = new File(root + File.separator + sbmlName);
      if (f.exists()) {
        message.setDialog("Overwrite", "File already exists.\nDo you want to overwrite?");
        if (!parent.request(RequestType.REQUEST_BOOLEAN, message)) { return exitValue; }

        boolean option = message.getBoolean();

        if (option) {
          File dir = new File(root + File.separator + sbmlName);
          if (dir.isDirectory()) {
            Utility.deleteDir(dir);
          } else {
            System.gc();
            dir.delete();
          }

        } else {
          new File(properties.getDirectory() + File.separator + "running").delete();
          return 0;
        }
      }
      if (modelFile.contains(".lpn")) {
        try {
          Translator t1 = new Translator();
          if (properties.isAbs()) {
            LPN lhpnFile = new LPN();
            lhpnFile.load(root + File.separator + modelFile);
            Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
            abst.abstractSTG(false);
            abst.save(root + File.separator + properties.getSim() + File.separator + modelFile);
            t1.convertLPN2SBML(root + File.separator + simName + File.separator + modelFile, properties.getVerificationProperties().getLpnProperty());
          } else {
            t1.convertLPN2SBML(root + File.separator + modelFile, properties.getVerificationProperties().getLpnProperty());
          }
          t1.setFilename(root + File.separator + sbmlName);
          t1.outputSBML();
          exitValue = 0;
          logFile.close();
        }
        catch (BioSimException e) {
          e.printStackTrace();
        }
        return exitValue;
      } else if (properties.isGui() && properties.isNary()) {
        String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
        ArrayList<String> specs = new ArrayList<>();
        ArrayList<Object[]> conLevel = new ArrayList<>();
        String directory = properties.getDirectory();
        retrieveSpeciesAndConLevels(specs, conLevel);
        BioModel bioModel = BioModel.createBioModel(properties.getRoot(), this);
        bioModel.load(root + File.separator + properties.getModelFile());

        if (bioModel.flattenModel(true) != null) {
          String lpnProperty = properties.getVerificationProperties().getLpnProperty();
          String prop = null;
          if (!lpnProperty.equals("")) {
            prop = lpnProperty;
          } else {
            prop = properties.getVerificationProperties().getConstraintProperty();
          }
          MutableString mutProp = new MutableString(prop);
          LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
          prop = mutProp.getString();
          if (lpnFile == null) {
            new File(directory + File.separator + "running").delete();
            logFile.close();
            return 0;
          }
          lpnFile.save(root + File.separator + simName + File.separator + lpnName);
          try {
            Translator t1 = new Translator();
            if (properties.isAbs()) {
              LPN lhpnFile = new LPN();
              lhpnFile.load(root + File.separator + simName + File.separator + lpnName);
              Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
              abst.abstractSTG(false);
              abst.save(root + File.separator + simName + File.separator + lpnName + ".temp");
              t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName + ".temp", prop);
            } else {
              t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName, prop);
            }
            t1.setFilename(root + File.separator + sbmlName);
            t1.outputSBML();
          }
          catch (BioSimException e) {
            e.printStackTrace();
          }
        } else {
          new File(directory + File.separator + "running").delete();
          logFile.close();
          return 0;
        }
        exitValue = 0;
      } else {
        if (reb2sacAbstraction() && (properties.isAbs() || properties.isNary())) {
          String[] command = properties.getSbmlCommand(sbmlName);
          message.setLog("Executing:\n" + SBMLutilities.commandString(command));
          this.notifyObservers(message);
          reb2sac = exec.exec(command, Executables.envp, work);
        } else {
          message.setLog("Outputting SBML file:\n" + root + File.separator + sbmlName);
          this.notifyObservers(message);
          FileOutputStream fileOutput = new FileOutputStream(new File(root + File.separator + sbmlName));
          FileInputStream fileInput = new FileInputStream(new File(filename));
          int read = fileInput.read();
          while (read != -1) {
            fileOutput.write(read);
            read = fileInput.read();
          }
          fileInput.close();
          fileOutput.close();
          exitValue = 0;
        }
      }
    } else {
      return exitValue;
    }

    message.setString(sbmlName);
    parent.send(RequestType.ADD_FILE, message);

    if (reb2sac != null) {
      exitValue = reb2sac.waitFor();
    }

    return exitValue;
  }

  private boolean reb2sacAbstraction() {
    AdvancedProperties advProperties = properties.getAdvancedProperties();

    for (String abstractionOption : advProperties.getPreAbs()) {
      if (!abstractionOption.equals("complex-formation-and-sequestering-abstraction") && !abstractionOption.equals("operator-site-reduction-abstraction")) { return true; }
    }

    return advProperties.getLoopAbs().size() > 0 || advProperties.getPostAbs().size() > 0;

  }

  private void retrieveSpeciesAndConLevels(ArrayList<String> specs, ArrayList<Object[]> conLevel) {
    List<String> intSpecies = properties.getSimulationProperties().getIntSpecies();
    for (int i = 0; i < intSpecies.size(); i++) {
      if (!intSpecies.get(i).equals("")) {
        String[] split = intSpecies.get(i).split(" ");
        if (split.length > 1) {
          String[] levels = split[1].split(",");
          if (levels.length > 0) {
            specs.add(split[0]);
            conLevel.add(levels);
          }
        }
      }
    }
  }
  
  public class ReadStream implements Runnable {
	  String name;
	  InputStream is;
	  Thread thread; 

	  public ReadStream(String name, InputStream is) {
		  this.name = name;
		  this.is = is;
	  }       
	  public void start () {
		  thread = new Thread (this);
		  thread.start ();
	  }       
	  public void run () {
		  try {
			  InputStreamReader isr = new InputStreamReader (is);
			  BufferedReader br = new BufferedReader (isr);      	
			  double runTime = properties.getSimulationProperties().getRun() * properties.getSimulationProperties().getTimeLimit();
			  double time = 0;
			  double oldTime = 0;
			  int runNum = 0;
			  int prog = 0;
			  while (true) {
				  String line = br.readLine ();
				  if (line == null) break;
				  if (name.equals("stdin")) {
					  try {
						  if (line.contains("Time")) {
							  time = Double.parseDouble(line.substring(line.indexOf('=') + 1, line.length()));
							  if (oldTime > time) {
								  runNum++;
							  }
							  oldTime = time;
							  time += (runNum * properties.getSimulationProperties().getTimeLimit());
							  double d = ((time * 100) / runTime);
							  String s = d + "";
							  double decimal = Double.parseDouble(s.substring(s.indexOf('.'), s.length()));
							  if (decimal >= 0.5) {
								  prog = (int) (Math.ceil(d));
							  } else {
								  prog = (int) (d);
							  }
						  }
					  }
					  catch (Exception e) {
						  e.printStackTrace();
					  }
					  if (parent!=null) {
						  message.setInteger(prog);
						  parent.send(RequestType.REQUEST_PROGRESS, message);
					  } else{
						  System.out.println(line);
					  }
				  } else {
					  System.out.println ("[" + name + "] " + line);
				  }
			  }	
			  is.close ();    
		  } catch (Exception ex) {
			  System.out.println ("Problem reading stream " + name + "... :" + ex);
			  ex.printStackTrace ();
		  }
	  }
  }

  private void updateReb2SacProgress() throws IOException {
    String error = "";
    try {
      double runTime = properties.getSimulationProperties().getRun() * properties.getSimulationProperties().getTimeLimit();
      InputStream reb = reb2sac.getInputStream();
      InputStreamReader isr = new InputStreamReader(reb);
      BufferedReader br = new BufferedReader(isr);
      String line;
      double time = 0;
      double oldTime = 0;
      int runNum = 0;
      int prog = 0;
      while ((line = br.readLine()) != null) {
        try {
          if (line.contains("Time")) {
            time = Double.parseDouble(line.substring(line.indexOf('=') + 1, line.length()));
            if (oldTime > time) {
              runNum++;
            }
            oldTime = time;
            time += (runNum * properties.getSimulationProperties().getTimeLimit());
            double d = ((time * 100) / runTime);
            String s = d + "";
            double decimal = Double.parseDouble(s.substring(s.indexOf('.'), s.length()));
            if (decimal >= 0.5) {
              prog = (int) (Math.ceil(d));
            } else {
              prog = (int) (d);
            }
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
        this.message.setInteger(prog);
        parent.send(RequestType.REQUEST_PROGRESS, message);
      }
      InputStream reb2 = reb2sac.getErrorStream();
      int read = reb2.read();
      while (read != -1) {
        error += (char) read;
        read = reb2.read();
      }
      br.close();
      isr.close();
      reb.close();
      reb2.close();
    }
    catch (Exception e) {
      // e.printStackTrace();
    }

    if (!error.equals("")) {
      message.setLog("Errors:\n" + error);
      this.notifyObservers(message);
    }

  }

  private void viewStateGraph(String filename, String theFile, String directory, StateGraph sg) {
    String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
    sg.outputStateGraph(graphFile, true);
    try {
      Runtime execGraph = Runtime.getRuntime();
      if (System.getProperty("os.name").contentEquals("Linux")) {
        message.setLog("Executing:\ndotty " + graphFile);
        execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
      } else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
        message.setLog("Executing:\nopen " + graphFile);
        execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
      } else {
        message.setLog("Executing:\ndotty " + graphFile);
        execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
      }
    }
    catch (Exception e1) {
      message.setErrorDialog("Error", "Error viewing state graph.");
    }
  }

  /**
   * This method is called if a button that cancels the simulation is pressed.
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    if (dynSim != null) {
      dynSim.cancel();
    }
    if (reb2sac != null) {
      reb2sac.destroy();
    }
    if (sg != null) {
      sg.stop();
    }
  }

  @Override
  public boolean send(RequestType type, Message message) {
    if (type == RequestType.REQUEST_PROGRESS) {
      if (parent != null) { return parent.send(type, message); }
    }

    return false;
  }

}
