package edu.utah.ece.async.ibiosim.analysis;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.JProgressBar;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import edu.utah.ece.async.ibiosim.analysis.fba.FluxBalanceAnalysis;
import edu.utah.ece.async.ibiosim.analysis.markov.BuildStateGraphThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformSteadyStateMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.PerformTransientMarkovAnalysisThread;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph;
import edu.utah.ece.async.ibiosim.analysis.markov.StateGraph.Property;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.DynamicSimulation.SimulationType;
import edu.utah.ece.async.ibiosim.analysis.simulation.flattened.Simulator;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.MutableString;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.lema.verification.lpn.Abstraction;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Translator;

public class Run extends Observable
{

  private AnalysisProperties properties;
  private final Message message = new Message();
  //private FileWriter logFile;

  public void execute(String directory) throws IOException
  {
    //logFile = new FileWriter(new File(directory + GlobalConstants.separator + "log.txt"));
  }

  private int executeFBA(String directory, String theFile, boolean refresh, String filename, String printer_id, String outDir) throws XMLStreamException, IOException
  {
    int exitValue = 255;

    FluxBalanceAnalysis fluxBalanceAnalysis = new FluxBalanceAnalysis(directory + GlobalConstants.separator, theFile, properties.getAbsError());
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

  private int executeMarkov(String modelFile, String theFile, String sim, String root, String directory, String filename, String printer_id, String outDir, String lpnProperty, JProgressBar progress, boolean view) throws XMLStreamException, IOException, InterruptedException, BioSimException
  {
    String prop = null;
    LPN lhpnFile = null;
    if (modelFile.contains(".lpn"))
    {
      lhpnFile = new LPN();
      lhpnFile.load(root + GlobalConstants.separator + modelFile);
    }
    else
    {
      new File(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn").delete();
      ArrayList<String> specs = new ArrayList<String>();
      ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
      String[] intSpecies = properties.getIntSpecies();
      for (int i = 0; i < intSpecies.length; i++)
      {
        if (!intSpecies[i].equals(""))
        {
          String[] split = intSpecies[i].split(" ");
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
      BioModel bioModel = new BioModel(root);
      bioModel.load(root + GlobalConstants.separator + filename);
      if (bioModel.flattenModel(true) != null)
      {
        if (!lpnProperty.equals(""))
        {
          prop = lpnProperty;
        }
        ArrayList<String> propList = new ArrayList<String>();
        if (prop == null)
        {
          Model m = bioModel.getSBMLDocument().getModel();
          for (int num = 0; num < m.getConstraintCount(); num++)
          {
            String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
            if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
            {
              propList.add(constraint);
            }
          }
        }
        if (propList.size() > 0)
        {
          //	          String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
          //	          if ((s != null) && (s.length() > 0))
          //	          {
          //	            Model m = bioModel.getSBMLDocument().getModel();
          //	            for (int num = 0; num < m.getConstraintCount(); num++)
          //	            {
          //	              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
          //	              if (s.equals(constraint))
          //	              {
          //	                prop = Translator.convertProperty(m.getConstraint(num).getMath());
          //	              }
          //	            }
          //	          }
        }
        MutableString mutProp = new MutableString(prop);
        lhpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
        prop = mutProp.getString();
        if (lhpnFile == null)
        {
          new File(directory + GlobalConstants.separator + "running").delete();
          return 0;
        }
        lhpnFile.save(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
        this.message.setLog("Saving SBML file as LPN:\n" + filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
      }
      else
      {
        new File(directory + GlobalConstants.separator + "running").delete();
        return 0;
      }
    }
    StateGraph sg = new StateGraph(lhpnFile);

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
        if (modelFile.contains(".lpn"))
        {
          performMarkovAnalysis.start(properties.getAbsError(), null);
        }
        else
        {
          BioModel gcm = new BioModel(root);
          gcm.load(root + GlobalConstants.separator + filename);
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
          performMarkovAnalysis.start(properties.getAbsError(), propList);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop())
        {
          String simrep = sg.getMarkovResults();
          if (simrep != null)
          {
            FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          if(view)
          {
            viewStateGraph(filename, theFile, directory, sg);
          }
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
            performMarkovAnalysis.start(properties.getTimeLimit(), properties.getTimeStep(), properties.getPrintInterval(), properties.getAbsError(), condition, globallyTrue);

        }
        else
        {
          performMarkovAnalysis.start(properties.getTimeLimit(), properties.getTimeStep(), properties.getPrintInterval(), properties.getAbsError(), null, false);
        }
        performMarkovAnalysis.join();
        if (!sg.getStop())
        {
          String simrep = sg.getMarkovResults();
          if (simrep != null)
          {
            FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
            simrepstream.write((simrep).getBytes());
            simrepstream.close();
          }
          if(view)
          {
            viewStateGraph(filename, theFile, directory, sg);
          }
        }
      }
    }
    return 0;
  }

  private int executeNary(String modelFile, String directory, String simName, String root, String lpnProperty, boolean abstraction) throws XMLStreamException, IOException
  {
    String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
    ArrayList<String> specs = new ArrayList<String>();
    ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
    String[] intSpecies = properties.getIntSpecies();

    for (int i = 0; i < intSpecies.length; i++)
    {
      if (!intSpecies[i].equals(""))
      {
        String[] split = intSpecies[i].split(" ");
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
    BioModel bioModel = new BioModel(root);
    //TODO: check
    bioModel.load(root + GlobalConstants.separator + modelFile);
    String prop = null;
    if (!lpnProperty.equals(""))
    {
      prop = lpnProperty;
    }
    ArrayList<String> propList = new ArrayList<String>();
    if (prop == null)
    {
      Model m = bioModel.getSBMLDocument().getModel();
      for (int num = 0; num < m.getConstraintCount(); num++)
      {
        String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
        if (constraint.startsWith("G(") || constraint.startsWith("F(") || constraint.startsWith("U("))
        {
          propList.add(constraint);
        }
      }
    }
    if (propList.size() > 0)
    {
      //String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
      //	      if ((s != null) && (s.length() > 0))
      //	      {
      //	        Model m = bioModel.getSBMLDocument().getModel();
      //	        for (int num = 0; num < m.getConstraintCount(); num++)
      //	        {
      //	          String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
      //	          if (s.equals(constraint))
      //	          {
      //	            prop = Translator.convertProperty(m.getConstraint(num).getMath());
      //	          }
      //	        }
      //	      }
    }
    MutableString mutProp = new MutableString(prop);
    LPN lpnFile = LPN.convertToLHPN(specs, conLevel, mutProp, bioModel);
    prop = mutProp.getString();
    if (lpnFile == null)
    {
      new File(directory + GlobalConstants.separator + "running").delete();
      //logFile.close();
      return 0;
    }
    lpnFile.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
    try
    {
      Translator t1 = new Translator();
      if (abstraction)
      {
        LPN lhpnFile = new LPN();
        lhpnFile.load(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
        //Abstraction abst = new Abstraction(lhpnFile, abstPane.getAbstractionProperty());
        //abst.addObserver(this);
        //abst.abstractSTG(false);
        //abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp");
        t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp", prop);
      }
      else
      {
        t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName, prop);
      }
      t1.setFilename(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName.replace(".lpn", ".xml"));
      t1.outputSBML();
    }
    catch(BioSimException e)
    {
      e.printStackTrace();
    }
    return 0;
  }

  private int executeSimulation(String sim, String direct, String directory, String root, String filename, String outDir, String theFile, boolean abstraction, boolean expandReaction, Runtime exec, Properties properties, File work) throws IOException, InterruptedException
  {
    int exitValue = 0;
    Preferences biosimrc = Preferences.userRoot();
    String reactionAbstraction = abstraction ? "reactionAbstraction" : expandReaction ? "expandReaction" : "None";
    double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
    String SBMLFileName = directory + GlobalConstants.separator + theFile;
    String command = null;
    String[] env = null;
    DynamicSimulation dynSim;

    if (direct != null && !direct.equals("."))
    {
      outDir = outDir + GlobalConstants.separator + direct;
    }
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
    else if (biosimrc.get("biosim.sim.command", "").equals(""))
    {
      command = Executables.reb2sacExecutable + " --target.encoding=" + sim + " " + theFile;
      Simulator.expandArrays(filename, stoichAmpValue);
      env = Executables.envp;
      runJava = false;
    }
    else
    {
      command = biosimrc.get("biosim.sim.command", "");
      String fileStem = theFile.replaceAll(".xml", "");
      fileStem = fileStem.replaceAll(".sbml", "");
      command = command.replaceAll("filename", fileStem);
      command = command.replaceAll("sim", sim);
      runJava = false;
    }

    if(runJava)
    {
      if (direct != null && !direct.equals("."))
      {
        outDir = outDir + GlobalConstants.separator + direct;
      }
      //dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, 0.0, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, absError, printer_track_quantity, genStats, simTab, reactionAbstraction,  initialTime, outputStartTime);

      new File(directory + GlobalConstants.separator + "running").delete();
    }
    else
    {
      Simulator.expandArrays(filename, stoichAmpValue);
      message.setLog("Executing:\n" + command + "\n");
      this.notifyObservers(message);

      Process reb2sac = exec.exec(command, env, work);
      exitValue = reb2sac.waitFor();
    }   
    return exitValue;
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

}
