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
import java.util.Observable;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation.SimulationType;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.Simulator;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.MutableString;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObservable;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.CoreObservable;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObservable.RequestType;
import edu.utah.ece.async.lema.verification.lpn.Abstraction;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Translator;

public class Run extends CoreObservable implements ActionListener
{

  private final AnalysisProperties properties;
  private FileWriter logFile;

  private final Message message = new Message();
  private final Runtime exec = Runtime.getRuntime();

  private Process reb2sac;
  private StateGraph sg;
  private DynamicSimulation dynSim;

  private File work;

  public Run(AnalysisProperties properties)
  {
    this.properties = properties;
  }

  public int execute() throws IOException, XMLStreamException, InterruptedException, BioSimException
  {
    Runtime exec = Runtime.getRuntime();
    int exitValue = 255;
    long time1, time2;

    String modelFile = properties.getModelFile();
    String filename = properties.getFilename();
    
    work = new File(properties.getDirectory());
    
    if (properties.isNary() && properties.isGui() && (properties.isSsa() || properties.isXhtml() || properties.isDot()))
    {
      exitValue =  executeNary();
    }

    if (properties.isNary() && !properties.isGui() && !properties.getSim().contains("markov-chain-analysis") && properties.getSimulationProperties().getRun() == 1)
    {
      message.setLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=nary-level " + filename);
      this.notifyObservers(message);
      time1 = System.nanoTime();
      reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=nary-level " + modelFile, Executables.envp, work);
    }
    else if (properties.isFba())
    {
      time1 = System.nanoTime();
      exitValue =  executeFBA( );
    }
    else if (properties.isSbml())
    {
      time1 = System.nanoTime();
      exitValue = executeSBML();
    }
    else if (properties.isDot())
    {
      time1 = System.nanoTime();
      exitValue =  executeDot();
    }
    else if (properties.isXhtml())
    {
      time1 = System.nanoTime();
      exitValue = executeXhtml( );
    }
    else if (properties.getSim().equals("atacs"))
    {
      time1 = System.nanoTime();
      exitValue = executeAtacs( );
    }
    else if (properties.getSim().equals("prism"))
    {
      time1 = System.nanoTime();
      exitValue = executePrism( );
    }
    else if (properties.getSim().contains("markov-chain-analysis") || properties.getSim().equals("reachability-analysis"))
    {
      time1 = System.nanoTime();
      exitValue = executeMarkov();
    }
    else
    {
      time1 = System.nanoTime();
      exitValue = executeSimulation();

    }

    //updateReb2SacProgress();
    time2 = System.nanoTime();
    displayError(exitValue);


    String time = SBMLutilities.createTimeString(time1, time2);


    message.setLog("Total Simulation Time: " + time + "\n\n");
    this.notifyObservers(message);
    return exitValue;
  }

  private void displayError(int exitValue)
  {
    if (exitValue == 143)
    {
      message.setErrorDialog("The simulation was" + " canceled by the user.", "Canceled Simulation");
      this.notifyObservers(message);
    }
    else if (exitValue == 139)
    {
      message.setErrorDialog("The selected model is not a valid sbml file." + "\nYou must select an sbml file.", "Not An SBML File");
      this.notifyObservers(message);
    }
    else if(exitValue != 0)
    {
      message.setErrorDialog( "Error In Execution!\n" + "Bad Return Value!\n" + "The reb2sac program returned " + exitValue + " as an exit value.", "Error");
      this.notifyObservers(message);
    }
  }

  private int executeFBA() throws XMLStreamException, IOException
  {
    int exitValue = 255;

    FluxBalanceAnalysis fluxBalanceAnalysis = new FluxBalanceAnalysis(properties.getRoot() + File.separator, properties.getFilename(), properties.getSimulationProperties().getAbsError());
    exitValue = fluxBalanceAnalysis.PerformFluxBalanceAnalysis();

    if (exitValue == 1)
    {
      message.setErrorDialog("Error", "Flux balance analysis did not converge.");
    }
    else if (exitValue == 2)
    {
      message.setErrorDialog("Error", "Flux balance analysis failed.");
    }
    else if (exitValue == -1)
    {
      message.setErrorDialog("Error", "No flux balance constraints.");
    }
    else if (exitValue == -2)
    {
      message.setErrorDialog("Error", "Initial point must be strictly feasible.");
    }
    else if (exitValue == -3)
    {
      message.setErrorDialog("Error",  "The FBA problem is infeasible.");
    }
    else if (exitValue == -4)
    {
      message.setErrorDialog("Error", "The FBA problem has a singular KKT system.");
    }
    else if (exitValue == -5)
    {
      message.setErrorDialog("Error", "The matrix must have at least one row.");
    }
    else if (exitValue == -6)
    {
      message.setErrorDialog("Error", "The matrix is singular.");
    }
    else if (exitValue == -7)
    {
      message.setErrorDialog("Error", "Equalities matrix A must have full rank.");
    }
    else if (exitValue == -8)
    {
      message.setErrorDialog("Error", "Miscellaneous FBA failure (see console for details).");
    }
    else if (exitValue == -9)
    {
      message.setErrorDialog("Error", "Reaction in flux objective does not have a flux bound.");
    }
    else if (exitValue == -10)
    {
      message.setErrorDialog("Error",  "All reactions must have flux bounds for FBA.");
    }
    else if (exitValue == -11)
    {
      message.setErrorDialog("Error", "No flux objectives.");
    }
    else if (exitValue == -12)
    {
      message.setErrorDialog("Error", "No active flux objective.");
    }
    else if (exitValue == -13)
    {
      message.setErrorDialog("Error", "Unknown flux balance analysis error.");
    }
    if(exitValue != 0)
    {
      this.notifyObservers(message);
    }
    return exitValue;
  }

  private int executeMarkov() throws XMLStreamException, IOException, InterruptedException, BioSimException
  {
    String prop = null;
    LPN lhpnFile = null;
    String root = properties.getRoot();
    String filename = properties.getFilename();
    String sim = properties.getSim();
    JProgressBar progress = null;
    if (properties.getFilename().contains(".lpn"))
    {
      lhpnFile = new LPN();
      lhpnFile.load(root + File.separator + filename);
    }
    else
    {
      new File( properties.getFilename().replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
      ArrayList<String> specs = new ArrayList<String>();
      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
      retrieveSpeciesAndConLevels(specs, conLevel);

      BioModel bioModel = new BioModel(properties.getRoot());
      bioModel.load(root + File.separator + filename);
      if (bioModel.flattenModel(true) != null)
      {
        if (!properties.getVerificationProperties().getLpnProperty().equals(""))
        {
          prop = properties.getVerificationProperties().getLpnProperty();
        }
        else
        {
          prop = properties.getVerificationProperties().getConstraintProperty();
        }

        MutableString mutProp = new MutableString(prop);
        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
        prop = mutProp.getString();
        if (lhpnFile == null)
        {
          new File(properties.getRoot() + File.separator + "running").delete();
          return 0;
        }
        lhpnFile.save(properties.getFilename().replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
        this.message.setLog("Saving SBML file as LPN:\n" + properties.getFilename().replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
      }
      else
      {
        new File(properties.getRoot() + File.separator + "running").delete();
        return 0;
      }
    }
    sg = new StateGraph(lhpnFile);

    BuildStateGraphThread buildStateGraph = new BuildStateGraphThread(sg, progress);
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

    if (sim.equals("reachability-analysis") && !sg.getStop())
    {

    }
    else if (sim.equals("steady-state-markov-chain-analysis"))
    {
      if (!sg.getStop())
      {
        message.setLog("Performing steady state Markov chain analysis.");
        PerformSteadyStateMarkovAnalysisThread performMarkovAnalysis = new PerformSteadyStateMarkovAnalysisThread(sg, progress);
        if (filename.contains(".lpn"))
        {
          performMarkovAnalysis.start(properties.getSimulationProperties().getAbsError(), null);
        }
        else
        {
          BioModel gcm = new BioModel(root);
          gcm.load(root + File.separator + filename);
          ArrayList<Property> propList = new ArrayList<Property>();
          if (prop == null)
          {
            Model m = gcm.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++)
            {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (constraint.startsWith("St("))
              {
                propList.add(sg.createProperty(constraint.trim().replace(" ", ""), Translator.convertProperty(m.getConstraint(num).getMath())));
              }
            }
          }
          performMarkovAnalysis.start(properties.getSimulationProperties().getAbsError(), propList);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop())
        {
          String simrep = sg.getMarkovResults();
          if (simrep != null)
          {
            FileOutputStream simrepstream = new FileOutputStream(new File(root + File.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          //          if(view)
          //          {
          //            viewStateGraph(filename, theFile, directory, sg);
          //          }
        }
      }
    }
    else if (sim.equals("transient-markov-chain-analysis"))
    {
      if (!sg.getStop())
      {
        message.setLog("Performing transient Markov chain analysis with uniformization.");
        this.notifyObservers(message);
        PerformTransientMarkovAnalysisThread performMarkovAnalysis = new PerformTransientMarkovAnalysisThread(sg, progress);
        if (prop != null)
        {
          String[] condition;

          condition = Translator.getProbpropParts(Translator.getProbpropExpression(prop));
          boolean globallyTrue = false;
          if (prop.contains("PF"))
          {
            condition[0] = "true";
          }
          else if (prop.contains("PG"))
          {
            condition[0] = "true";
            globallyTrue = true;
          }
          performMarkovAnalysis.start(properties.getSimulationProperties().getTimeLimit(), properties.getSimulationProperties().getTimeStep(), properties.getSimulationProperties().getPrintInterval(), properties.getSimulationProperties().getAbsError(), condition, globallyTrue);

        }
        else
        {
          performMarkovAnalysis.start(properties.getSimulationProperties().getTimeLimit(), properties.getSimulationProperties().getTimeStep(), properties.getSimulationProperties().getPrintInterval(), properties.getSimulationProperties().getAbsError(), null, false);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop())
        {
          String simrep = sg.getMarkovResults();
          if (simrep != null)
          {
            FileOutputStream simrepstream = new FileOutputStream(new File(properties.getRoot() + File.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          //          if(view)
          //          {
          //            viewStateGraph(filename, theFile, directory, sg);
          //          }
        }
      }
    }
    return 0;
  }

  private int executeNary() throws XMLStreamException, IOException
  {
    String modelFile = properties.getModelFile();
    String directory = properties.getDirectory();
    String simName = properties.getSim();
    String root = properties.getRoot();
    String lpnProperty = properties.getVerificationProperties().getLpnProperty();
    boolean abstraction;
    String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
    ArrayList<String> specs = new ArrayList<String>();
    ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
    retrieveSpeciesAndConLevels(specs, conLevel);
    BioModel bioModel = new BioModel(root);
    //TODO: check
    bioModel.load(root + File.separator + modelFile);
    String prop = null;
    if (!lpnProperty.equals(""))
    {
      prop = lpnProperty;
    }
    MutableString mutProp = new MutableString(prop);
    LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
    prop = mutProp.getString();
    if (lpnFile == null)
    {
      new File(directory + File.separator + "running").delete();
      //logFile.close();
      return 0;
    }
    lpnFile.save(root + File.separator + simName + File.separator + lpnName);
    try
    {
      Translator t1 = new Translator();
      if (properties.isAbs())
      {
        LPN lhpnFile = new LPN();
        lhpnFile.load(root + File.separator + simName + File.separator + lpnName);
        Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
        abst.abstractSTG(false);
        abst.save(root + File.separator + simName + File.separator + lpnName + ".temp");
        t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName + ".temp", prop);
      }
      else
      {
        t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName, prop);
      }
      t1.setFilename(root + File.separator + simName + File.separator + lpnName.replace(".lpn", ".xml"));
      t1.outputSBML();
    }
    catch(BioSimException e)
    {
      e.printStackTrace();
    }
    return 0;
  }

  private int executeSimulation() throws IOException, InterruptedException, XMLStreamException, BioSimException
  {
    int exitValue = 0;
    String SBMLFileName = properties.getFilename();
    String command = null;
    String[] env = Executables.envp;

    JProgressBar progress = null;
    String sim = properties.getSim();


    boolean runJava = true;
    if (sim.equals("SSA-CR (Dynamic)"))
    {
      dynSim = new DynamicSimulation(SimulationType.CR);
    }
    else if (sim.equals("SSA-Direct (Dynamic)"))
    {
      dynSim = new DynamicSimulation(SimulationType.DIRECT);
    }
    else if (sim.equals("SSA-Direct (Flatten)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
    }
    else if (sim.equals("SSA-Direct (Hierarchical)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
    }
    else if (sim.equals("Mixed-Hierarchical"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_MIXED);
    }
    else if (sim.equals("Hybrid-Hierarchical"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_HYBRID);
    }
    else if (sim.equals("Runge-Kutta-Fehlberg (Dynamic)"))
    {
      dynSim = new DynamicSimulation(SimulationType.RK);
    }
    else if (sim.equals("Runge-Kutta-Fehlberg (Hierarchical)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
    }
    else if (sim.equals("Runge-Kutta-Fehlberg (Flatten)"))
    {
      dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
    }
    else if (sim.equals("atacs"))
    {
      exitValue = executeAtacs();
    }
    else if (sim.equals("prism"))
    {
      exitValue = executePrism();
    }
    else if (sim.contains("markov-chain-analysis") || sim.equals("reachability-analysis"))
    {
      exitValue = executeMarkov();
    }
    else if (properties.getCommand().equals(""))
    {
      command = Executables.reb2sacExecutable + " --target.encoding=" + sim + " " + properties.getFilename();
      Simulator.expandArrays(SBMLFileName, properties.getAdvancedProperties().getStoichAmp());
      runJava = false;
    }
    else
    {
      command = properties.getCommand();
      String fileStem = properties.getFilename().replaceAll(".xml", "").replaceAll(".sbml", "");;
      command = command.replaceAll("filename", fileStem);
      command = command.replaceAll("sim", sim);
      runJava = false;
    }

    if(runJava)
    {
      dynSim.simulate(properties);

      new File(properties.getRoot() + File.separator + "running").delete();
    }
    else
    {
      message.setLog("Executing:\n" + command + "\n");
      this.notifyObservers(message);

      reb2sac = exec.exec(command, env, work);
      updateReb2SacProgress();
      
      exitValue = reb2sac.waitFor();
    }   
    return exitValue;
  }

  private int executeAtacs() throws IOException, InterruptedException
  {
    int exitValue = 0;

    String modelFile = properties.getModelFile();
    message.setLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=hse2 " + modelFile);

    reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=hse2 " + modelFile, Executables.envp, work);
    message.setLog("Executing:\natacs -T0.000001 -oqoflhsgllvA " + properties.getFilename() + "out.hse");
    this.notifyObservers(message);
    exec.exec("atacs -T0.000001 -oqoflhsgllvA out.hse", null, work);


    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }

    return exitValue;
  }
  private int executePrism() throws IOException, InterruptedException, XMLStreamException, BioSimException
  {
    int exitValue = 255;
    String prop = null;
    String directory = null;
    String out = null;
    LPN lhpnFile = null;
    String root = properties.getRoot();
    if (properties.getFilename().contains(".lpn"))
    {
      lhpnFile = new LPN();
      lhpnFile.load(properties.getRoot() + File.separator + properties.getFilename());
    }
    else
    {
      new File(properties.getFilename().replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
      ArrayList<String> specs = new ArrayList<String>();
      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
      retrieveSpeciesAndConLevels(specs, conLevel);
      BioModel bioModel = new BioModel(root);
      bioModel.load(root + File.separator + properties.getFilename());
      if (bioModel.flattenModel(true) != null)
      {
        if (!properties.getVerificationProperties().getLpnProperty().equals(""))
        {
          prop = properties.getVerificationProperties().getLpnProperty();
        }
        else
        {
          prop = properties.getVerificationProperties().getConstraintProperty();
        }
        MutableString mutProp = new MutableString(prop);
        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
        prop = mutProp.getString();
        if (lhpnFile == null)
        {
          new File(properties.getRoot() + File.separator + "running").delete();
          return 0;
        }
        message.setLog("Saving SBML file as PRISM file:\n" + properties.getFilename().replace(".xml", ".prism"));
        this.notifyObservers(message);
        message.setLog("Saving PRISM Property file:\n" + properties.getFilename().replace(".xml", ".pctl"));
        this.notifyObservers(message);
        LPN.convertLPN2PRISM(logFile, lhpnFile, properties.getFilename().replace(".xml", ".prism"), 
          bioModel.getSBMLDocument());
        Preferences biosimrc = Preferences.userRoot();
        String prismCmd = biosimrc.get("biosim.general.prism", "");
        message.setLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl"); 
        this.notifyObservers(message);
        reb2sac = exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
        String error = "", result = "", fullLog = "";
        try
        {
          InputStream reb = reb2sac.getInputStream();
          InputStreamReader isr = new InputStreamReader(reb);
          BufferedReader br = new BufferedReader(isr);
          String line;
          while ((line = br.readLine()) != null)
          {
            fullLog += line + '\n';
            if (line.startsWith("Result:"))
            {
              result = line + '\n';
            }
          }
          InputStream reb2 = reb2sac.getErrorStream();
          int read = reb2.read();
          while (read != -1)
          {
            error += (char) read;
            read = reb2.read();
          }
          br.close();
          isr.close();
          reb.close();
          reb2.close();
        }
        catch (Exception e)
        {
          // e.printStackTrace();
        }
        if (reb2sac != null)
        {
          exitValue = reb2sac.waitFor();
        }
        if (!error.equals(""))
        {
          message.setLog("Errors:\n" + error + "\n");

          this.notifyObservers(message);
        }
        else if (!result.equals(""))
        {
          message.setLog(result);
          this.notifyObservers(message);
        }
        else
        {
          throw new BioSimException("Verification Failed!", "Verification could not be executed. Something went wrong.");
        }

        exitValue = 0;
      }
      else
      {
        new File(directory + File.separator + "running").delete();
        logFile.close();
        exitValue = 0;
      }
    }

    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }

    Preferences biosimrc = Preferences.userRoot();
    String prismCmd = biosimrc.get("biosim.general.prism", "");
    message.setLog("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl");
    this.notifyObservers(message);
    exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
    return exitValue;
  }

  private int executeDot() throws IOException, InterruptedException, BioSimException
  {
    int exitValue= 255;
    String out = properties.getModelFile().replace(".xml", ".dot");
    String outputFileName = properties.getFilename().replace(".sbml", "").replace(".xml", "") + ".dot";
    if (properties.isNary())
    {
      LPN lhpnFile = new LPN();
      lhpnFile.load(properties.getFilename().replace(".sbml", "").replace(".xml", "") + ".lpn");
      lhpnFile.printDot(outputFileName);
      exitValue = 0;
    }
    else if (properties.getFilename().contains(".lpn"))
    {
      LPN lhpnFile = new LPN();
      try {
        lhpnFile.load(properties.getFilename());
      } catch (BioSimException e) {
        e.printStackTrace();
      }
      if (properties.isAbs())
      {
        Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
        abst.abstractSTG(false);
        abst.printDot(properties.getRoot() + File.separator + properties.getSim() + File.separator + properties.getModelFile());
      }
      else
      {
        lhpnFile.printDot(properties.getRoot() + File.separator + properties.getSim() + File.separator + properties.getModelFile());
      }
      exitValue = 0;
    }
    else
    {
      String command = Executables.reb2sacExecutable + " --target.encoding=dot --out=" +  out + " " + properties.getFilename();
      message.setLog("Executing:\n" + command);
      this.notifyObservers(message);
      reb2sac = exec.exec(command, Executables.envp, work);
    }


    String command;
    if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
    {
      command = "open " + outputFileName;
    }
    else
    {
      command = "dotty " + outputFileName;
    }

    exec.exec(command);
    message.setLog("Executing:\n" + command);
    this.notifyObservers(message);

    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }

    return exitValue;
  }

  private int executeXhtml() throws IOException, InterruptedException
  {
    String outFullPath = properties.getFilename().replaceAll(".xml", ".xhtml");
    String outName = properties.getModelFile().replace(".xml", ".xhtml");

    int exitValue = 0;
    Simulator.expandArrays(outFullPath, 1);

    String command = Executables.reb2sacExecutable + " --target.encoding=xhtml --out=" + outName + " " + properties.getFilename();
    message.setLog("Executing:\n" + command);
    reb2sac = exec.exec(command, Executables.envp, work);

    Preferences biosimrc = Preferences.userRoot();
    String xhtmlCmd = biosimrc.get("biosim.general.browser", "");
    command = xhtmlCmd + " " + outFullPath;
    
    message.setLog("Executing:\n" + command);
    this.notifyObservers(message);
    exec.exec(command, null, work);

    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }

    return exitValue;
  }

  private int executeSBML() throws IOException, HeadlessException, XMLStreamException, InterruptedException
  {
    int exitValue = 255;

    String root = properties.getRoot();
    String modelFile = properties.getModelFile();
    String simName = properties.getSim();
    
    if(!parent.request(RequestType.REQUEST_STRING, message))
    {
      return exitValue;
    }
    String sbmlName = message.getMessage();
    
    if (sbmlName != null && !sbmlName.trim().equals(""))
    {
      sbmlName = sbmlName.trim();
      if (!sbmlName.endsWith(".xml"))
      {
        sbmlName += ".xml";
      }
      
      File f = new File(root + File.separator + sbmlName);
      if (f.exists())
      {
        if(!parent.request(RequestType.REQUEST_OVERWRITE, message))
        {
          return exitValue;
        }
        
        boolean option = message.getBoolean();
        
        if(option)
        {
          File dir = new File(root + File.separator + sbmlName);
          if (dir.isDirectory())
          {
            Utility.deleteDir(dir);
          }
          else
          {
            System.gc();
            dir.delete();
          }
          
        }
        else
        {
          new File(properties.getDirectory() + File.separator + "running").delete();
          return 0;
        }
      }
      if (modelFile.contains(".lpn"))
      {
        try
        {
          Translator t1 = new Translator();
          if (properties.isAbs())
          {
            LPN lhpnFile = new LPN();
            lhpnFile.load(root + File.separator + modelFile);
            Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
            abst.abstractSTG(false);
            abst.save(root + File.separator + properties.getSim() + File.separator + modelFile);
            t1.convertLPN2SBML(root + File.separator + simName + File.separator + modelFile, properties.getVerificationProperties().getLpnProperty());
          }
          else
          {
            t1.convertLPN2SBML(root + File.separator + modelFile, properties.getVerificationProperties().getLpnProperty());
          }
          t1.setFilename(root + File.separator + sbmlName);
          t1.outputSBML();
          exitValue = 0;
          logFile.close();
        }
        catch(BioSimException e)
        {
          e.printStackTrace();
        }
        return exitValue;
      }
      else if (properties.isGui() && properties.isNary())
      {
        String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
        ArrayList<String> specs = new ArrayList<String>();
        ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
        String directory = properties.getDirectory();
        retrieveSpeciesAndConLevels(specs, conLevel);
        BioModel bioModel = new BioModel(root);
        bioModel.load(root + File.separator + properties.getModelFile());

        if (bioModel.flattenModel(true) != null)
        {
          String lpnProperty = properties.getVerificationProperties().getLpnProperty();
          String prop = null;
          if (!lpnProperty.equals(""))
          {
            prop = lpnProperty;
          }
          else
          {
            prop = properties.getVerificationProperties().getConstraintProperty();
          }
          MutableString mutProp = new MutableString(prop);
          LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
          prop = mutProp.getString();
          if (lpnFile == null)
          {
            new File(directory + File.separator + "running").delete();
            logFile.close();
            return 0;
          }
          lpnFile.save(root + File.separator + simName + File.separator + lpnName);
          try
          {
            Translator t1 = new Translator();
            if (properties.isAbs())
            {
              LPN lhpnFile = new LPN();
              lhpnFile.load(root + File.separator + simName + File.separator + lpnName);
              Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
              abst.abstractSTG(false);
              abst.save(root + File.separator + simName + File.separator + lpnName + ".temp");
              t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName + ".temp", prop);
            }
            else
            {
              t1.convertLPN2SBML(root + File.separator + simName + File.separator + lpnName, prop);
            }
            t1.setFilename(root + File.separator + sbmlName);
            t1.outputSBML();
          }
          catch(BioSimException e)
          {
            e.printStackTrace();
          }
        }
        else
        {
          new File(directory + File.separator + "running").delete();
          logFile.close();
          return 0;
        }
        exitValue = 0;
      }
      else
      {
        String filename = properties.getFilename();
        if (reb2sacAbstraction() && (properties.isAbs() || properties.isNary()))
        {
          message.setLog("Executing:\n" + Executables.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + File.separator + sbmlName + " " + filename);
          this.notifyObservers(message);
          reb2sac = exec.exec(Executables.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + File.separator + sbmlName + " " + modelFile, Executables.envp, work);
        }
        else
        {
          message.setLog("Outputting SBML file:\n" + root + File.separator + sbmlName);
          this.notifyObservers(message);
          FileOutputStream fileOutput = new FileOutputStream(new File(root + File.separator + sbmlName));
          FileInputStream fileInput = new FileInputStream(new File(filename));
          int read = fileInput.read();
          while (read != -1)
          {
            fileOutput.write(read);
            read = fileInput.read();
          }
          fileInput.close();
          fileOutput.close();
          exitValue = 0;
        }
      }
    }
    else
    {
      return exitValue;
    }

    message.setString(sbmlName);
    parent.send(RequestType.ADD_FILE, message);
    
    if (reb2sac != null)
    {
      exitValue = reb2sac.waitFor();
    }

    return exitValue;
  }

  private boolean reb2sacAbstraction()
  {
    AdvancedProperties advProperties = properties.getAdvancedProperties();
    
    
    for (String abstractionOption : advProperties.getPreAbs())
    {
      if (!abstractionOption.equals("complex-formation-and-sequestering-abstraction") && !abstractionOption.equals("operator-site-reduction-abstraction"))
      {
        return true;
      }
    }
    
    return advProperties.getLoopAbs().size() > 0 || advProperties.getPostAbs().size() > 0;
    
  }
  private void retrieveSpeciesAndConLevels(ArrayList<String> specs, ArrayList<Object[]> conLevel)
  {
    List<String> intSpecies = properties.getSimulationProperties().getIntSpecies();
    for (int i = 0; i < intSpecies.size(); i++)
    {
      if (!intSpecies.get(i).equals(""))
      {
        String[] split = intSpecies.get(i).split(" ");
        if (split.length > 1)
        {
          String[] levels = split[1].split(",");
          if (levels.length > 0)
          {
            specs.add(split[0]);
            conLevel.add(levels);
          }
        }
      }
    }
  }
  
  private void updateReb2SacProgress() throws IOException
  {
    String error = "";
    try
    {
      double runTime = properties.getSimulationProperties().getRun() * properties.getSimulationProperties().getTimeLimit();
      InputStream reb = reb2sac.getInputStream();
      InputStreamReader isr = new InputStreamReader(reb);
      BufferedReader br = new BufferedReader(isr);
      String line;
      double time = 0;
      double oldTime = 0;
      int runNum = 0;
      int prog = 0;
      while ((line = br.readLine()) != null)
      {
        try
        {
          if (line.contains("Time"))
          {
            time = Double.parseDouble(line.substring(line.indexOf('=') + 1, line.length()));
            if (oldTime > time)
            {
              runNum++;
            }
            oldTime = time;
            time += (runNum * properties.getSimulationProperties().getTimeLimit());
            double d = ((time * 100) / runTime);
            String s = d + "";
            double decimal = Double.parseDouble(s.substring(s.indexOf('.'), s.length()));
            if (decimal >= 0.5)
            {
              prog = (int) (Math.ceil(d));
            }
            else
            {
              prog = (int) (d);
            }
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
        this.message.setInteger(prog);
        parent.send(RequestType.REQUEST_PROGRESS, message);
      }
      InputStream reb2 = reb2sac.getErrorStream();
      int read = reb2.read();
      while (read != -1)
      {
        error += (char) read;
        read = reb2.read();
      }
      br.close();
      isr.close();
      reb.close();
      reb2.close();
    }
    catch (Exception e)
    {
      // e.printStackTrace();
    }
    
    if (!error.equals(""))
    {
      message.setLog("Errors:\n" + error);
      this.notifyObservers(message);
    }
    
  }
  
  private void viewStateGraph(String filename, String theFile, String directory, StateGraph sg)
  {
    String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
    sg.outputStateGraph(graphFile, true);
    try
    {
      Runtime execGraph = Runtime.getRuntime();
      if (System.getProperty("os.name").contentEquals("Linux"))
      {
        message.setLog("Executing:\ndotty " + graphFile);
        execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
      }
      else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
      {
        message.setLog("Executing:\nopen " + graphFile);
        execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
      }
      else
      {
        message.setLog("Executing:\ndotty " + graphFile);
        execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
      }
    }
    catch (Exception e1)
    {
      message.setErrorDialog( "Error", "Error viewing state graph.");
    }
  }


  /**
   * This method is called if a button that cancels the simulation is pressed.
   */
  @Override
  public void actionPerformed(ActionEvent e)
  {

    if (dynSim != null)
    {
      dynSim.cancel();
    }
    if (reb2sac != null)
    {
      reb2sac.destroy();
    }
    if (sg != null)
    {
      sg.stop();
    }
  }
  
}
