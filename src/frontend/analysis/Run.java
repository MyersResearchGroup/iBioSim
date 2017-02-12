package frontend.analysis;

import java.awt.Component;
import java.awt.Dimension;
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
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.DynamicSimulation;
import backend.analysis.dynamicsim.DynamicSimulation.SimulationType;
import backend.analysis.dynamicsim.flattened.Simulator;
import backend.analysis.fba.FluxBalanceAnalysis;
import backend.analysis.markov.BuildStateGraphThread;
import backend.analysis.markov.PerfromSteadyStateMarkovAnalysisThread;
import backend.analysis.markov.PerfromTransientMarkovAnalysisThread;
import backend.analysis.markov.StateGraph;
import backend.analysis.markov.StateGraph.Property;
import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.lpn.parser.Abstraction;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Translator;
import dataModels.util.GlobalConstants;
import dataModels.util.Message;
import dataModels.util.MutableString;
import dataModels.util.dataparser.DataParser;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.graph.Graph;
import frontend.main.Gui;
import frontend.main.Log;
import frontend.verification.AbstPane;

/**
 * This class creates the properties file that is given to the reb2sac program.
 * It also executes the reb2sac program.
 * 
 * @author Curtis Madsen
 */
public class Run implements ActionListener, Observer
{

  private Process       reb2sac;

  private final AnalysisView  analysisView;

  DynamicSimulation     dynSim  = null;

  StateGraph          sg;

  public Run(AnalysisView analysisView)
  {
    this.analysisView = analysisView;
  }

  /**
   * This method is given which buttons are selected and creates the
   * properties file from all the other information given.
   * 
   * @param useInterval
   * @param stem
   */
  public static void createProperties(double initialTime, double outputStartTime, double timeLimit, String useInterval, double printInterval, double minTimeStep, double timeStep,
      double absError, double relError, String outDir, long rndSeed, int run, int numPaths, String[] intSpecies, String printer_id,
      String printer_track_quantity, String genStats, String[] getFilename, String selectedButtons, Component component, String filename,
      double rap1, double rap2, double qss, int con, double stoichAmp, JList preAbs, JList loopAbs, JList postAbs, AbstPane abstPane,
      boolean mpde, boolean meanPath, boolean adaptive)
  {
    Properties abs = new Properties();
    if (selectedButtons.contains("abs") || selectedButtons.contains("nary"))
    {
      int gcmIndex = 1;
      for (int i = 0; i < preAbs.getModel().getSize(); i++)
      {
        String abstractionOption = (String) preAbs.getModel().getElementAt(i);
        if (abstractionOption.equals("complex-formation-and-sequestering-abstraction") || abstractionOption.equals("operator-site-reduction-abstraction"))
        {
          abs.setProperty("gcm.abstraction.method." + gcmIndex, abstractionOption);
          gcmIndex++;
        }
        else
        {
          abs.setProperty("reb2sac.abstraction.method.1." + (i + 1), abstractionOption);
        }
      }
      for (int i = 0; i < loopAbs.getModel().getSize(); i++)
      {
        abs.setProperty("reb2sac.abstraction.method.2." + (i + 1), (String) loopAbs.getModel().getElementAt(i));
      }
    }
    for (int i = 0; i < postAbs.getModel().getSize(); i++)
    {
      abs.setProperty("reb2sac.abstraction.method.3." + (i + 1), (String) postAbs.getModel().getElementAt(i));
    }
    abs.setProperty("simulation.printer", printer_id);
    abs.setProperty("simulation.printer.tracking.quantity", printer_track_quantity);
    for (int i = 0; i < intSpecies.length; i++)
    {
      if (!intSpecies[i].equals(""))
      {
        String[] split = intSpecies[i].split(" ");
        abs.setProperty("reb2sac.interesting.species." + (i + 1), split[0]);
        if (split.length > 1)
        {
          String[] levels = split[1].split(",");
          for (int j = 0; j < levels.length; j++)
          {
            abs.setProperty("reb2sac.concentration.level." + split[0] + "." + (j + 1), levels[j]);
          }
        }
      }
    }
    abs.setProperty("reb2sac.rapid.equilibrium.condition.1", "" + rap1);
    abs.setProperty("reb2sac.rapid.equilibrium.condition.2", "" + rap2);
    abs.setProperty("reb2sac.qssa.condition.1", "" + qss);
    abs.setProperty("reb2sac.operator.max.concentration.threshold", "" + con);
    abs.setProperty("reb2sac.diffusion.stoichiometry.amplification.value", "" + stoichAmp);
    abs.setProperty("reb2sac.generate.statistics", genStats);
    if (selectedButtons.contains("none"))
    {
      abs.setProperty("reb2sac.abstraction.method", "none");
    }
    if (selectedButtons.contains("expand"))
    {
      abs.setProperty("reb2sac.abstraction.method", "expand");
    }
    if (selectedButtons.contains("abs"))
    {
      abs.setProperty("reb2sac.abstraction.method", "abs");
    }
    else if (selectedButtons.contains("nary"))
    {
      abs.setProperty("reb2sac.abstraction.method", "nary");
    }
    if (abstPane != null)
    {
      for (Integer i = 0; i < abstPane.preAbsModel.size(); i++)
      {
        abs.setProperty("abstraction.transform." + abstPane.preAbsModel.getElementAt(i).toString(), "preloop" + i.toString());
      }
      for (Integer i = 0; i < abstPane.loopAbsModel.size(); i++)
      {
        if (abstPane.preAbsModel.contains(abstPane.loopAbsModel.getElementAt(i)))
        {
          String value = abs.getProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString());
          value = value + "mainloop" + i.toString();
          abs.setProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString(), value);
        }
        else
        {
          abs.setProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString(), "mainloop" + i.toString());
        }
      }
      for (Integer i = 0; i < abstPane.postAbsModel.size(); i++)
      {
        if (abstPane.preAbsModel.contains(abstPane.postAbsModel.getElementAt(i)) || abstPane.preAbsModel.contains(abstPane.postAbsModel.get(i)))
        {
          String value = abs.getProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString());
          value = value + "postloop" + i.toString();
          abs.setProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString(), value);
        }
        else
        {
          abs.setProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString(), "postloop" + i.toString());
        }
      }
      for (String s : abstPane.transforms)
      {
        if (!abstPane.preAbsModel.contains(s) && !abstPane.loopAbsModel.contains(s) && !abstPane.postAbsModel.contains(s))
        {
          abs.remove(s);
        }
      }
    }
    if (selectedButtons.contains("ODE"))
    {
      abs.setProperty("reb2sac.simulation.method", "ODE");
    }
    else if (selectedButtons.contains("monteCarlo"))
    {
      abs.setProperty("reb2sac.simulation.method", "monteCarlo");
      abs.setProperty("reb2sac.iSSA.number.paths", "" + numPaths);
      if (mpde)
      {
        abs.setProperty("reb2sac.iSSA.type", "mpde");
      }
      else if (meanPath)
      {
        abs.setProperty("reb2sac.iSSA.type", "meanPath");
      }
      else
      {
        abs.setProperty("reb2sac.iSSA.type", "medianPath");
      }
      if (adaptive)
      {
        abs.setProperty("reb2sac.iSSA.adaptive", "true");
      }
      else
      {
        abs.setProperty("reb2sac.iSSA.adaptive", "false");
      }
    }
    else if (selectedButtons.contains("markov"))
    {
      abs.setProperty("reb2sac.simulation.method", "markov");
    }
    else if (selectedButtons.contains("fba"))
    {
      abs.setProperty("reb2sac.simulation.method", "FBA");
    }
    else if (selectedButtons.contains("sbml"))
    {
      abs.setProperty("reb2sac.simulation.method", "SBML");
    }
    else if (selectedButtons.contains("dot"))
    {
      abs.setProperty("reb2sac.simulation.method", "Network");
    }
    else if (selectedButtons.contains("xhtml"))
    {
      abs.setProperty("reb2sac.simulation.method", "Browser");
    }
    else if (selectedButtons.contains("lhpn"))
    {
      abs.setProperty("reb2sac.simulation.method", "LPN");
    }
    if (!selectedButtons.contains("monteCarlo"))
    {
      abs.setProperty("simulation.initial.time", "" + initialTime);
      abs.setProperty("simulation.output.start.time", "" + outputStartTime);
      abs.setProperty("ode.simulation.time.limit", "" + timeLimit);
      if (useInterval.equals("Print Interval"))
      {
        abs.setProperty("ode.simulation.print.interval", "" + printInterval);
      }
      else if (useInterval.equals("Minimum Print Interval"))
      {
        abs.setProperty("ode.simulation.minimum.print.interval", "" + printInterval);
      }
      else
      {
        abs.setProperty("ode.simulation.number.steps", "" + ((int) printInterval));
      }
      if (timeStep == Double.MAX_VALUE)
      {
        abs.setProperty("ode.simulation.time.step", "inf");
      }
      else
      {
        abs.setProperty("ode.simulation.time.step", "" + timeStep);
      }
      abs.setProperty("ode.simulation.min.time.step", "" + minTimeStep);
      abs.setProperty("ode.simulation.absolute.error", "" + absError);
      abs.setProperty("ode.simulation.relative.error", "" + relError);
      abs.setProperty("ode.simulation.out.dir", outDir);
      abs.setProperty("monte.carlo.simulation.random.seed", "" + rndSeed);
      abs.setProperty("monte.carlo.simulation.runs", "" + run);
    }
    if (!selectedButtons.contains("ODE"))
    {
      abs.setProperty("simulation.initial.time", "" + initialTime);
      abs.setProperty("simulation.output.start.time", "" + outputStartTime);
      abs.setProperty("monte.carlo.simulation.time.limit", "" + timeLimit);
      if (useInterval.equals("Print Interval"))
      {
        abs.setProperty("monte.carlo.simulation.print.interval", "" + printInterval);
      }
      else if (useInterval.equals("Minimum Print Interval"))
      {
        abs.setProperty("monte.carlo.simulation.minimum.print.interval", "" + printInterval);
      }
      else
      {
        abs.setProperty("monte.carlo.simulation.number.steps", "" + ((int) printInterval));
      }
      if (timeStep == Double.MAX_VALUE)
      {
        abs.setProperty("monte.carlo.simulation.time.step", "inf");
      }
      else
      {
        abs.setProperty("monte.carlo.simulation.time.step", "" + timeStep);
      }
      abs.setProperty("monte.carlo.simulation.min.time.step", "" + minTimeStep);
      abs.setProperty("monte.carlo.simulation.random.seed", "" + rndSeed);
      abs.setProperty("monte.carlo.simulation.runs", "" + run);
      abs.setProperty("monte.carlo.simulation.out.dir", outDir);
    }
    abs.setProperty("simulation.run.termination.decider", "constraint");
    try
    {
      if (!getFilename[getFilename.length - 1].contains("."))
      {
        getFilename[getFilename.length - 1] += ".";
        filename += ".";
      }
      int cut = 0;
      for (int i = 0; i < getFilename[getFilename.length - 1].length(); i++)
      {
        if (getFilename[getFilename.length - 1].charAt(i) == '.')
        {
          cut = i;
        }
      }
      FileOutputStream store = new FileOutputStream(new File((filename.substring(0, filename.length() - getFilename[getFilename.length - 1].length())) + getFilename[getFilename.length - 1].substring(0, cut) + ".properties"));
      abs.store(store, getFilename[getFilename.length - 1].substring(0, cut) + " Properties");
      store.close();
    }
    catch (Exception except)
    {
      JOptionPane.showMessageDialog(component, "Unable To Save Properties File!" + "\nMake sure you select a model for abstraction.", "Unable To Save File", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Executes the reb2sac program. If ODE, monte carlo, or markov is selected,
   * this method creates a Graph object.
   * 
   * @param runTime
   * @param refresh
   * @throws XMLStreamException
   * @throws NumberFormatException
   */
  public int execute(String filename, JRadioButton fba, JRadioButton sbml, JRadioButton dot, JRadioButton xhtml, Component component,
      JRadioButton ode, JRadioButton monteCarlo, String sim, String printer_id, String printer_track_quantity, String outDir,
      JRadioButton nary, int naryRun, String[] intSpecies, Log log, Gui gui, JTabbedPane simTab, String root, JProgressBar progress,
      String simName, ModelEditor modelEditor, String direct, double initialTime, double outputStartTime, double timeLimit, double runTime, String modelFile, AbstPane abstPane,
      JRadioButton abstraction, JRadioButton expandReaction, String lpnProperty, double absError, double relError, double timeStep, double printInterval,
      int runs, long rndSeed, boolean refresh, JLabel progressLabel, JFrame running)
  {
    outDir = outDir.replace("\\", "/");
    filename = filename.replace("\\", "/");
    Runtime exec = Runtime.getRuntime();
    int exitValue = 255;
    while (outDir.split("/")[outDir.split("/").length - 1].equals("."))
    {
      outDir = outDir.substring(0, outDir.length() - 1 - outDir.split("/")[outDir.split("/").length - 1].length());
    }
    try
    {
      long time1, time2;
      time2 = -1;
      String directory = "";
      String theFile = "";
      String sbmlName = "";
      if (filename.lastIndexOf('/') >= 0)
      {
        directory = filename.substring(0, filename.lastIndexOf('/') + 1);
        theFile = filename.substring(filename.lastIndexOf('/') + 1);
      }
      if (filename.lastIndexOf('\\') >= 0)
      {
        directory = filename.substring(0, filename.lastIndexOf('\\') + 1);
        theFile = filename.substring(filename.lastIndexOf('\\') + 1);
      }
      File work = new File(directory);
      new FileWriter(new File(directory + GlobalConstants.separator + "running")).close();
      FileWriter logFile = new FileWriter(new File(directory + GlobalConstants.separator + "log.txt"));
      Properties properties = new Properties();
      properties.load(new FileInputStream(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".properties"));
      boolean genStats = Boolean.parseBoolean(properties.getProperty("reb2sac.generate.statistics"));
      String out = theFile;
      if (out.length() > 4 && out.substring(out.length() - 5, out.length()).equals(".sbml"))
      {
        out = out.substring(0, out.length() - 5);
      }
      else if (out.length() > 3 && out.substring(out.length() - 4, out.length()).equals(".xml"))
      {
        out = out.substring(0, out.length() - 4);
      }
      if (nary.isSelected() && modelEditor != null && (monteCarlo.isSelected() || xhtml.isSelected() || dot.isSelected()))
      {
        String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
        ArrayList<String> specs = new ArrayList<String>();
        ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
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
        bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
        time1 = System.nanoTime();
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
          String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
          if ((s != null) && (s.length() > 0))
          {
            Model m = bioModel.getSBMLDocument().getModel();
            for (int num = 0; num < m.getConstraintCount(); num++)
            {
              String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
              if (s.equals(constraint))
              {
                prop = Translator.convertProperty(m.getConstraint(num).getMath());
              }
            }
          }
        }
        MutableString mutProp = new MutableString(prop);
        LPN lpnFile = bioModel.convertToLHPN(specs, conLevel, mutProp);
        prop = mutProp.getString();
        if (lpnFile == null)
        {
          new File(directory + GlobalConstants.separator + "running").delete();
          logFile.close();
          return 0;
        }
        lpnFile.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
        Translator t1 = new Translator();
        if (abstraction.isSelected())
        {
          LPN lhpnFile = new LPN();
          lhpnFile.load(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
          Abstraction abst = new Abstraction(lhpnFile, abstPane);
          abst.abstractSTG(false);
          abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp");
          t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp", prop);
        }
        else
        {
          t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName, prop);
        }
        t1.setFilename(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName.replace(".lpn", ".xml"));
        t1.outputSBML();
      }
      if (nary.isSelected() && modelEditor == null && !sim.contains("markov-chain-analysis") && naryRun == 1)
      {
        log.addText("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=nary-level " + filename + "\n");
        logFile.write("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=nary-level " + filename + "\n\n");
        time1 = System.nanoTime();
        reb2sac = exec.exec(Gui.reb2sacExecutable + " --target.encoding=nary-level " + theFile, Gui.envp, work);
      }
      else if (fba.isSelected())
      {
        time1 = System.nanoTime();
        FluxBalanceAnalysis fluxBalanceAnalysis = new FluxBalanceAnalysis(directory + GlobalConstants.separator, theFile, absError);
        exitValue = fluxBalanceAnalysis.PerformFluxBalanceAnalysis();
      }
      else if (sbml.isSelected())
      {
        sbmlName = JOptionPane.showInputDialog(component, "Enter Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
        if (sbmlName != null && !sbmlName.trim().equals(""))
        {
          sbmlName = sbmlName.trim();
          if (!sbmlName.endsWith(".xml"))
          {
            sbmlName += ".xml";
          }
          File f = new File(root + GlobalConstants.separator + sbmlName);
          if (f.exists())
          {
            Object[] options = { "Overwrite", "Cancel" };
            int value = JOptionPane.showOptionDialog(component, "File already exists." + "\nDo you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (value == JOptionPane.YES_OPTION)
            {
              File dir = new File(root + GlobalConstants.separator + sbmlName);
              if (dir.isDirectory())
              {
                gui.deleteDir(dir);
              }
              else
              {
                System.gc();
                dir.delete();
              }
            }
            else
            {
              new File(directory + GlobalConstants.separator + "running").delete();
              logFile.close();
              return 0;
            }
          }
          if (modelFile.contains(".lpn"))
          {
            progress.setIndeterminate(true);
            time1 = System.nanoTime();
            Translator t1 = new Translator();
            if (abstraction.isSelected())
            {
              LPN lhpnFile = new LPN();
              lhpnFile.load(root + GlobalConstants.separator + modelFile);
              Abstraction abst = new Abstraction(lhpnFile, abstPane);
              abst.abstractSTG(false);
              abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile);
              t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile, lpnProperty);
            }
            else
            {
              t1.convertLPN2SBML(root + GlobalConstants.separator + modelFile, lpnProperty);
            }
            t1.setFilename(root + GlobalConstants.separator + sbmlName);
            t1.outputSBML();
            exitValue = 0;
            logFile.close();
            return exitValue;
          }
          else if (modelEditor != null && nary.isSelected())
          {
            String lpnName = modelFile.replace(".sbml", "").replace(".gcm", "").replace(".xml", "") + ".lpn";
            ArrayList<String> specs = new ArrayList<String>();
            ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
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
            progress.setIndeterminate(true);
            BioModel bioModel = new BioModel(root);
            bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
            if (bioModel.flattenModel(true) != null)
            {
              time1 = System.nanoTime();
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
                String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
                if ((s != null) && (s.length() > 0))
                {
                  Model m = bioModel.getSBMLDocument().getModel();
                  for (int num = 0; num < m.getConstraintCount(); num++)
                  {
                    String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
                    if (s.equals(constraint))
                    {
                      prop = Translator.convertProperty(m.getConstraint(num).getMath());
                    }
                  }
                }
              }
              MutableString mutProp = new MutableString(prop);
              LPN lpnFile = bioModel.convertToLHPN(specs, conLevel, mutProp);
              prop = mutProp.getString();
              if (lpnFile == null)
              {
                new File(directory + GlobalConstants.separator + "running").delete();
                logFile.close();
                return 0;
              }
              lpnFile.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
              Translator t1 = new Translator();
              if (abstraction.isSelected())
              {
                LPN lhpnFile = new LPN();
                lhpnFile.load(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName);
                Abstraction abst = new Abstraction(lhpnFile, abstPane);
                abst.abstractSTG(false);
                abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp");
                t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName + ".temp", prop);
              }
              else
              {
                t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + lpnName, prop);
              }
              t1.setFilename(root + GlobalConstants.separator + sbmlName);
              t1.outputSBML();
            }
            else
            {
              time1 = System.nanoTime();
              new File(directory + GlobalConstants.separator + "running").delete();
              logFile.close();
              return 0;
            }
            exitValue = 0;
          }
          else
          {
            if (analysisView.reb2sacAbstraction() && (abstraction.isSelected() || nary.isSelected()))
            {
              log.addText("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + GlobalConstants.separator + sbmlName + " " + filename + "\n");
              logFile.write("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + GlobalConstants.separator + sbmlName + " " + filename + "\n\n");
              time1 = System.nanoTime();
              reb2sac = exec.exec(Gui.reb2sacExecutable + " --target.encoding=sbml --out=" + ".." + GlobalConstants.separator + sbmlName + " " + theFile, Gui.envp, work);
            }
            else
            {
              log.addText("Outputting SBML file:\n" + root + GlobalConstants.separator + sbmlName + "\n");
              logFile.write("Outputting SBML file:\n" + root + GlobalConstants.separator + sbmlName + "\n\n");
              time1 = System.nanoTime();
              FileOutputStream fileOutput = new FileOutputStream(new File(root + GlobalConstants.separator + sbmlName));
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
          time1 = System.nanoTime();
        }
      }
      else if (dot.isSelected())
      {
        if (nary.isSelected() && modelEditor != null)
        {
          LPN lhpnFile = new LPN(log);
          lhpnFile.load(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".lpn");
          lhpnFile.printDot(directory + GlobalConstants.separator + theFile.replace(".sbml", "").replace(".xml", "") + ".dot");
          time1 = System.nanoTime();
          exitValue = 0;
        }
        else if (modelFile.contains(".lpn"))
        {
          LPN lhpnFile = new LPN();
          lhpnFile.load(root + GlobalConstants.separator + modelFile);
          if (abstraction.isSelected())
          {
            Abstraction abst = new Abstraction(lhpnFile, abstPane);
            abst.abstractSTG(false);
            abst.printDot(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".dot"));
          }
          else
          {
            lhpnFile.printDot(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".dot"));
          }
          time1 = System.nanoTime();
          exitValue = 0;
        }
        else
        {
          log.addText("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + filename + "\n");
          logFile.write("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + filename + "\n\n");
          time1 = System.nanoTime();
          reb2sac = exec.exec(Gui.reb2sacExecutable + " --target.encoding=dot --out=" + out + ".dot " + theFile, Gui.envp, work);
        }
      }
      else if (xhtml.isSelected())
      {
        log.addText("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=xhtml --out=" + out + ".xhtml " + filename + "\n");
        logFile.write("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=xhtml --out=" + out + ".xhtml " + filename + "\n\n");
        time1 = System.nanoTime();
        Simulator.expandArrays(filename, 1);

        reb2sac = exec.exec(Gui.reb2sacExecutable + " --target.encoding=xhtml --out=" + out + ".xhtml " + theFile, Gui.envp, work);
      }
      else
      {
        if (sim.equals("atacs"))
        {
          log.addText("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=hse2 " + filename + "\n");
          logFile.write("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=hse2 " + filename + "\n\n");
          time1 = System.nanoTime();
          reb2sac = exec.exec(Gui.reb2sacExecutable + " --target.encoding=hse2 " + theFile, Gui.envp, work);
        }
        else if (sim.equals("prism"))
        {
          String prop = null;
          time1 = System.nanoTime();
          progress.setIndeterminate(true);
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
            bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
            if (bioModel.flattenModel(true) != null)
            {
              time1 = System.nanoTime();
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
                String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
                if ((s != null) && (s.length() > 0))
                {
                  Model m = bioModel.getSBMLDocument().getModel();
                  for (int num = 0; num < m.getConstraintCount(); num++)
                  {
                    String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
                    if (s.equals(constraint))
                    {
                      prop = Translator.convertProperty(m.getConstraint(num).getMath());
                    }
                  }
                }
              }
              MutableString mutProp = new MutableString(prop);
              lhpnFile = bioModel.convertToLHPN(specs, conLevel, mutProp);
              prop = mutProp.getString();
              if (lhpnFile == null)
              {
                new File(directory + GlobalConstants.separator + "running").delete();
                logFile.close();
                return 0;
              }
              bioModel.convertLPN2PRISM(log, logFile, lhpnFile, filename.replace(".xml", ".prism"));
              Preferences biosimrc = Preferences.userRoot();
              String prismCmd = biosimrc.get("biosim.general.prism", "");
              log.addText("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl" + "\n");
              logFile.write("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl" + "\n");
              reb2sac = exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
              String error = "";
              String result = "";
              String fullLog = "";
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
              if (time2 == -1)
              {
                time2 = System.nanoTime();
              }
              running.setCursor(null);
              running.dispose();
              String time = createTimeString(time1, time2);
              if (!error.equals(""))
              {
                log.addText("Errors:\n" + error + "\n");
                logFile.write("Errors:\n" + error + "\n\n");
              }
              else if (!result.equals(""))
              {
                log.addText(result);
                logFile.write(result);
              }
              else
              {
                JTextArea messageArea = new JTextArea(fullLog);
                messageArea.setEditable(false);
                JScrollPane scroll = new JScrollPane();
                scroll.setMinimumSize(new Dimension(500, 500));
                scroll.setPreferredSize(new Dimension(500, 500));
                scroll.setViewportView(messageArea);
                JOptionPane.showMessageDialog(Gui.frame, scroll, "Verification Failed", JOptionPane.ERROR_MESSAGE);
              }
              log.addText("Total Verification Time: " + time + " for " + simName + "\n\n");
              logFile.write("Total Verification Time: " + time + " for " + simName + "\n\n\n");
              return 0;
            }
            else
            {
              new File(directory + GlobalConstants.separator + "running").delete();
              logFile.close();
              return 0;
            }
          }
        }
        else if (sim.contains("markov-chain-analysis") || sim.equals("reachability-analysis"))
        {
          String prop = null;
          time1 = System.nanoTime();
          progress.setIndeterminate(true);
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
            bioModel.load(root + GlobalConstants.separator + modelEditor.getRefFile());
            if (bioModel.flattenModel(true) != null)
            {
              time1 = System.nanoTime();
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
                String s = (String) JOptionPane.showInputDialog(component, "Select a property:", "Property Selection", JOptionPane.PLAIN_MESSAGE, null, propList.toArray(), null);
                if ((s != null) && (s.length() > 0))
                {
                  Model m = bioModel.getSBMLDocument().getModel();
                  for (int num = 0; num < m.getConstraintCount(); num++)
                  {
                    String constraint = SBMLutilities.myFormulaToString(m.getConstraint(num).getMath());
                    if (s.equals(constraint))
                    {
                      prop = Translator.convertProperty(m.getConstraint(num).getMath());
                    }
                  }
                }
              }
              MutableString mutProp = new MutableString(prop);
              lhpnFile = bioModel.convertToLHPN(specs, conLevel, mutProp);
              prop = mutProp.getString();
              if (lhpnFile == null)
              {
                new File(directory + GlobalConstants.separator + "running").delete();
                logFile.close();
                return 0;
              }
              lhpnFile.save(filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn");
              log.addText("Saving SBML file as LPN:\n" + filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn" + "\n");
              logFile.write("Saving SBML file as LPN:\n" + filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + ".lpn" + "\n\n");
            }
            else
            {
              new File(directory + GlobalConstants.separator + "running").delete();
              logFile.close();
              return 0;
            }
          }
          sg = new StateGraph(lhpnFile);
          sg.addObserver(this);
          BuildStateGraphThread buildStateGraph = new BuildStateGraphThread(sg, progress);
          buildStateGraph.start();
          buildStateGraph.join();
          log.addText("Number of states found: " + sg.getNumberOfStates());
          logFile.write("Number of states found: " + sg.getNumberOfStates() + "\n");
          log.addText("Number of transitions found: " + sg.getNumberOfTransitions());
          logFile.write("Number of transitions found: " + sg.getNumberOfTransitions() + "\n");
          log.addText("Memory used during state exploration: " + sg.getMemoryUsed() + "MB");
          logFile.write("Memory used during state exploration: " + sg.getMemoryUsed() + "MB\n");
          log.addText("Total memory used: " + sg.getTotalMemoryUsed() + "MB\n");
          logFile.write("Total memory used: " + sg.getTotalMemoryUsed() + "MB\n\n");
          if (sim.equals("reachability-analysis") && !sg.getStop())
          {
            time2 = System.nanoTime();
            Object[] options = { "Yes", "No" };
            int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (value == JOptionPane.YES_OPTION)
            {
              String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
              sg.outputStateGraph(graphFile, true);
              try
              {
                Runtime execGraph = Runtime.getRuntime();
                if (System.getProperty("os.name").contentEquals("Linux"))
                {
                  log.addText("Executing:\ndotty " + graphFile + "\n");
                  logFile.write("Executing:\ndotty " + graphFile + "\n\n");
                  execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                }
                else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
                {
                  log.addText("Executing:\nopen " + graphFile + "\n");
                  logFile.write("Executing:\nopen " + graphFile + "\n\n");
                  execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                }
                else
                {
                  log.addText("Executing:\ndotty " + graphFile + "\n");
                  logFile.write("Executing:\ndotty " + graphFile + "\n\n");
                  execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                }
              }
              catch (Exception e1)
              {
                JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
              }
            }
          }
          else if (sim.equals("steady-state-markov-chain-analysis"))
          {
            if (!sg.getStop())
            {
              log.addText("Performing steady state Markov chain analysis.\n");
              logFile.write("Performing steady state Markov chain analysis.\n\n");
              PerfromSteadyStateMarkovAnalysisThread performMarkovAnalysis = new PerfromSteadyStateMarkovAnalysisThread(sg, progress);
              if (modelFile.contains(".lpn"))
              {
                performMarkovAnalysis.start(absError, null);
              }
              else
              {
                BioModel gcm = new BioModel(root);
                gcm.load(root + GlobalConstants.separator + modelEditor.getRefFile());
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
                // TODO: THIS NEEDS FIXING
                /*
                 * for (int i = 0; i <
                 * gcmEditor.getGCM().getConditions().size();
                 * i++) { if
                 * (gcmEditor.getGCM().getConditions().
                 * get(i).startsWith("St")) {
                 * conditions.add(Translator
                 * .getProbpropExpression
                 * (gcmEditor.getGCM().getConditions().get(i)));
                 * } }
                 */
                performMarkovAnalysis.start(absError, propList);
              }
              performMarkovAnalysis.join();
              time2 = System.nanoTime();
              if (!sg.getStop())
              {
                String simrep = sg.getMarkovResults();
                if (simrep != null)
                {
                  FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                  simrepstream.write((simrep).getBytes());
                  simrepstream.close();
                }
                Object[] options = { "Yes", "No" };
                int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (value == JOptionPane.YES_OPTION)
                {
                  String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
                  sg.outputStateGraph(graphFile, true);
                  try
                  {
                    Runtime execGraph = Runtime.getRuntime();
                    if (System.getProperty("os.name").contentEquals("Linux"))
                    {
                      log.addText("Executing:\ndotty " + graphFile + "\n");
                      logFile.write("Executing:\ndotty " + graphFile + "\n\n");
                      execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                    }
                    else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
                    {
                      log.addText("Executing:\nopen " + graphFile + "\n");
                      logFile.write("Executing:\nopen " + graphFile + "\n\n");
                      execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                    }
                    else
                    {
                      log.addText("Executing:\ndotty " + graphFile + "\n");
                      logFile.write("Executing:\ndotty " + graphFile + "\n\n");
                      execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                    }
                  }
                  catch (Exception e1)
                  {
                    JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
                  }
                }
              }
            }
          }
          else if (sim.equals("transient-markov-chain-analysis"))
          {
            if (!sg.getStop())
            {
              log.addText("Performing transient Markov chain analysis with uniformization.\n");
              logFile.write("Performing transient Markov chain analysis with uniformization.\n\n");
              PerfromTransientMarkovAnalysisThread performMarkovAnalysis = new PerfromTransientMarkovAnalysisThread(sg, progress);
              time1 = System.nanoTime();
              if (prop != null)
              {
                String[] condition = Translator.getProbpropParts(Translator.getProbpropExpression(prop));
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
                performMarkovAnalysis.start(timeLimit, timeStep, printInterval, absError, condition, globallyTrue);
              }
              else
              {
                performMarkovAnalysis.start(timeLimit, timeStep, printInterval, absError, null, false);
              }
              performMarkovAnalysis.join();
              time2 = System.nanoTime();
              if (!sg.getStop())
              {
                String simrep = sg.getMarkovResults();
                if (simrep != null)
                {
                  FileOutputStream simrepstream = new FileOutputStream(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                  simrepstream.write((simrep).getBytes());
                  simrepstream.close();
                }
                Object[] options = { "Yes", "No" };
                int value = JOptionPane.showOptionDialog(Gui.frame, "The state graph contains " + sg.getNumberOfStates() + " states and " + sg.getNumberOfTransitions() + " transitions.\n" + "Do you want to view it in Graphviz?", "View State Graph", JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (value == JOptionPane.YES_OPTION)
                {
                  String graphFile = filename.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot";
                  sg.outputStateGraph(graphFile, true);
                  try
                  {
                    Runtime execGraph = Runtime.getRuntime();
                    if (System.getProperty("os.name").contentEquals("Linux"))
                    {
                      log.addText("Executing:\ndotty " + graphFile + "\n");
                      logFile.write("Executing:\ndotty " + graphFile + "\n\n");
                      execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                    }
                    else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
                    {
                      log.addText("Executing:\nopen " + graphFile + "\n");
                      logFile.write("Executing:\nopen " + graphFile + "\n\n");
                      execGraph.exec("open " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                    }
                    else
                    {
                      log.addText("Executing:\ndotty " + graphFile + "\n");
                      logFile.write("Executing:\ndotty " + graphFile + "\n\n");
                      execGraph.exec("dotty " + theFile.replace(".gcm", "").replace(".sbml", "").replace(".xml", "") + "_sg.dot", null, new File(directory));
                    }
                  }
                  catch (Exception e1)
                  {
                    JOptionPane.showMessageDialog(Gui.frame, "Error viewing state graph.", "Error", JOptionPane.ERROR_MESSAGE);
                  }
                }
                if (sg.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd"))
                {
                  if (refresh)
                  {
                    for (int i = 0; i < simTab.getComponentCount(); i++)
                    {
                      if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
                      {
                        if (simTab.getComponentAt(i) instanceof Graph)
                        {
                          ((Graph) simTab.getComponentAt(i)).refresh();
                        }
                      }
                    }
                  }
                }
              }
            }
            if (refresh)
            {
              for (int i = 0; i < simTab.getComponentCount(); i++)
              {
                if (simTab.getComponentAt(i).getName().equals("Histogram"))
                {
                  if (simTab.getComponentAt(i) instanceof Graph)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                  else
                  {
                    simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
                    simTab.getComponentAt(i).setName("Histogram");
                  }
                }
              }
            }
          }
          exitValue = 0;
        }
        else
        {
          Preferences biosimrc = Preferences.userRoot();
          String reactionAbstraction = abstraction == null ? "None" : abstraction.isSelected() ? "reactionAbstraction" : abstraction == null ? "None" : expandReaction.isSelected() ? "expandReaction" : "None";

          if (sim.equals("SSA-CR (Dynamic)"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            double minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));

            dynSim = new DynamicSimulation(SimulationType.CR);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }

            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, minTimeStep, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, 0, printer_track_quantity, genStats, simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("SSA-Direct (Dynamic)"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            double minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));

            dynSim = new DynamicSimulation(SimulationType.DIRECT);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, minTimeStep, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, 0, printer_track_quantity, genStats, simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("SSA-Direct (Flatten)"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            double minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));

            dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, minTimeStep, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, 0, printer_track_quantity, genStats, simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("SSA-Direct (Hierarchical)"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            double minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));

            dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_DIRECT);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, minTimeStep, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, 0, printer_track_quantity, genStats, simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("Mixed-Hierarchical"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            double minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));

            dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_MIXED);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, minTimeStep, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, absError, printer_track_quantity, genStats, simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("Hybrid-Hierarchical"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            double minTimeStep = Double.valueOf(properties.getProperty("monte.carlo.simulation.min.time.step"));

            dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_HYBRID);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, minTimeStep, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, absError, printer_track_quantity, genStats, simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("Runge-Kutta-Fehlberg (Dynamic)"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            dynSim = new DynamicSimulation(SimulationType.RK);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, 0.0, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, (int) Math.floor(timeLimit / printInterval), 0, absError, printer_track_quantity, genStats,
                simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("Runge-Kutta-Fehlberg (Hierarchical)"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, 0.0, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, absError, printer_track_quantity, genStats, simTab, reactionAbstraction, initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (sim.equals("Runge-Kutta-Fehlberg (Flatten)"))
          {

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            dynSim = new DynamicSimulation(SimulationType.HIERARCHICAL_RK);
            dynSim.addObserver(this);
            String SBMLFileName = directory + GlobalConstants.separator + theFile;
            if (direct != null && !direct.equals("."))
            {
              outDir = outDir + GlobalConstants.separator + direct;
            }
            dynSim.simulate(SBMLFileName, root, outDir + GlobalConstants.separator, timeLimit, timeStep, 0.0, rndSeed, progress, printInterval, runs, progressLabel, running, stoichAmpValue, intSpecies, 0, 0, absError, printer_track_quantity, genStats, simTab, reactionAbstraction,  initialTime, outputStartTime);
            exitValue = 0;
            new File(directory + GlobalConstants.separator + "running").delete();
            logFile.close();
            return exitValue;
          }
          else if (biosimrc.get("biosim.sim.command", "").equals(""))
          {

            time1 = System.nanoTime();
            log.addText("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=" + sim + " " + filename + "\n");
            logFile.write("Executing:\n" + Gui.reb2sacExecutable + " --target.encoding=" + sim + " " + filename + "\n\n");

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            Simulator.expandArrays(filename, stoichAmpValue);

            reb2sac = exec.exec(Gui.reb2sacExecutable + " --target.encoding=" + sim + " " + theFile, Gui.envp, work);
          }
          else
          {
            String command = biosimrc.get("biosim.sim.command", "");
            String fileStem = theFile.replaceAll(".xml", "");
            fileStem = fileStem.replaceAll(".sbml", "");
            command = command.replaceAll("filename", fileStem);
            command = command.replaceAll("sim", sim);
            log.addText(command + "\n");
            logFile.write(command + "\n\n");
            time1 = System.nanoTime();

            double stoichAmpValue = Double.parseDouble(properties.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));

            Simulator.expandArrays(filename, stoichAmpValue);

            reb2sac = exec.exec(command, null, work);
          }
        }
      }
      String error = "";
      try
      {
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
              time += (runNum * timeLimit);
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
          progress.setValue(prog);
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
      if (time2 == -1)
      {
        time2 = System.nanoTime();
      }
      String time = createTimeString(time1, time2);
      if (!error.equals(""))
      {
        log.addText("Errors:\n" + error + "\n");
        logFile.write("Errors:\n" + error + "\n\n");
      }
      log.addText("Total Simulation Time: " + time + " for " + simName + "\n\n");
      logFile.write("Total Simulation Time: " + time + " for " + simName + "\n\n\n");
      running.setCursor(null);
      running.dispose();
      if (exitValue != 0 && !fba.isSelected())
      {
        if (exitValue == 143)
        {
          JOptionPane.showMessageDialog(Gui.frame, "The simulation was" + " canceled by the user.", "Canceled Simulation", JOptionPane.ERROR_MESSAGE);
        }
        else if (exitValue == 139)
        {
          JOptionPane.showMessageDialog(Gui.frame, "The selected model is not a valid sbml file." + "\nYou must select an sbml file.", "Not An SBML File", JOptionPane.ERROR_MESSAGE);
        }
        else
        {
          JOptionPane.showMessageDialog(Gui.frame, "Error In Execution!\n" + "Bad Return Value!\n" + "The reb2sac program returned " + exitValue + " as an exit value.", "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
      else
      {
        if (nary.isSelected() && modelEditor == null && naryRun == 1)
        {
        }
        else if (fba.isSelected())
        {
          if (exitValue == 0)
          {
            if (refresh)
            {
              for (int i = 0; i < simTab.getComponentCount(); i++)
              {
                if (simTab.getComponentAt(i).getName().equals("Histogram"))
                {
                  if (simTab.getComponentAt(i) instanceof Graph)
                  {
                    ((Graph) simTab.getComponentAt(i)).setYLabel("Flux");
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                  else
                  {
                    if (new File(filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "sim-rep.txt").exists())
                    {
                      simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "Flux", gui, null, log, null, false, false));
                      simTab.getComponentAt(i).setName("Histogram");
                    }
                  }
                }
              }
            }
          }
          else if (exitValue == 1)
          {
            JOptionPane.showMessageDialog(Gui.frame, "Flux balance analysis did not converge.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == 2)
          {
            JOptionPane.showMessageDialog(Gui.frame, "Flux balance analysis failed.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -1)
          {
            JOptionPane.showMessageDialog(Gui.frame, "No flux balance constraints.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -2)
          {
            JOptionPane.showMessageDialog(Gui.frame, "Initial point must be strictly feasible.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -3)
          {
            JOptionPane.showMessageDialog(Gui.frame, "The FBA problem is infeasible.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -4)
          {
            JOptionPane.showMessageDialog(Gui.frame, "The FBA problem has a singular KKT system.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -5)
          {
            JOptionPane.showMessageDialog(Gui.frame, "The matrix must have at least one row.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -6)
          {
            JOptionPane.showMessageDialog(Gui.frame, "The matrix is singular.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -7)
          {
            JOptionPane.showMessageDialog(Gui.frame, "Equalities matrix A must have full rank.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -8)
          {
            JOptionPane.showMessageDialog(Gui.frame, "Miscellaneous FBA failure (see console for details).", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -9)
          {
            JOptionPane.showMessageDialog(Gui.frame, "Reaction in flux objective does not have a flux bound.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -10)
          {
            JOptionPane.showMessageDialog(Gui.frame, "All reactions must have flux bounds for FBA.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -11)
          {
            JOptionPane.showMessageDialog(Gui.frame, "No flux objectives.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -12)
          {
            JOptionPane.showMessageDialog(Gui.frame, "No active flux objective.", "Error", JOptionPane.ERROR_MESSAGE);
          }
          else if (exitValue == -13)
          {
            JOptionPane.showMessageDialog(Gui.frame, "Unknown flux balance analysis error.", "Error", JOptionPane.ERROR_MESSAGE);
          }
        }
        else if (sbml.isSelected())
        {
          if (sbmlName != null && !sbmlName.trim().equals(""))
          {
            if (!gui.updateOpenModelEditor(sbmlName))
            {
              try
              {
                ModelEditor gcm = new ModelEditor(root + GlobalConstants.separator, sbmlName, gui, log, false, null, null, null, false, false);
                gui.addTab(sbmlName, gcm, "Model Editor");
                gui.addToTree(sbmlName);
              }
              catch (Exception e)
              {
                e.printStackTrace();
              }
            }
            else
            {
              gui.getTab().setSelectedIndex(gui.getTab(sbmlName));
            }
            gui.enableTabMenu(gui.getTab().getSelectedIndex());
          }
        }
        else if (dot.isSelected())
        {
          if (System.getProperty("os.name").contentEquals("Linux"))
          {
            log.addText("Executing:\ndotty " + directory + out + ".dot" + "\n");
            logFile.write("Executing:\ndotty " + directory + out + ".dot" + "\n\n");
            exec.exec("dotty " + out + ".dot", null, work);
          }
          else if (System.getProperty("os.name").toLowerCase().startsWith("mac os"))
          {
            log.addText("Executing:\nopen " + directory + out + ".dot\n");
            logFile.write("Executing:\nopen " + directory + out + ".dot\n\n");
            exec.exec("open " + out + ".dot", null, work);
          }
          else
          {
            log.addText("Executing:\ndotty " + directory + out + ".dot" + "\n");
            logFile.write("Executing:\ndotty " + directory + out + ".dot" + "\n\n");
            exec.exec("dotty " + out + ".dot", null, work);
          }
        }
        else if (xhtml.isSelected())
        {
          Preferences biosimrc = Preferences.userRoot();
          String xhtmlCmd = biosimrc.get("biosim.general.browser", "");
          log.addText("Executing:\n" + xhtmlCmd + " " + directory + out + ".xhtml" + "\n");
          logFile.write("Executing:\n" + xhtmlCmd + " " + directory + out + ".xhtml" + "\n\n");
          exec.exec(xhtmlCmd + " " + out + ".xhtml", null, work);
        }
        else if (sim.equals("prism"))
        {
          Preferences biosimrc = Preferences.userRoot();
          String prismCmd = biosimrc.get("biosim.general.prism", "");
          log.addText("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl" + "\n");
          logFile.write("Executing:\n" + prismCmd + " " + directory + out + ".prism" + " " + directory + out + ".pctl" + "\n");
          exec.exec(prismCmd + " " + out + ".prism" + " " + out + ".pctl", null, work);
        }
        else if (sim.equals("atacs"))
        {
          log.addText("Executing:\natacs -T0.000001 -oqoflhsgllvA " + filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "out.hse\n");
          logFile.write("Executing:\natacs -T0.000001 -oqoflhsgllvA " + filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "out.hse\n\n");
          exec.exec("atacs -T0.000001 -oqoflhsgllvA out.hse", null, work);
          if (refresh)
          {
            for (int i = 0; i < simTab.getComponentCount(); i++)
            {
              if (simTab.getComponentAt(i).getName().equals("Histogram"))
              {
                if (simTab.getComponentAt(i) instanceof Graph)
                {
                  ((Graph) simTab.getComponentAt(i)).refresh();
                }
                else
                {
                  simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
                  simTab.getComponentAt(i).setName("Histogram");
                }
              }
            }
          }
        }
        else
        {
          if (ode.isSelected())
          {
            for (int i = 0; i < simTab.getComponentCount(); i++)
            {
              if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
              {
                if (simTab.getComponentAt(i) instanceof Graph)
                {
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  if (refresh)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                }
                else
                {
                  simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, true, false));
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  simTab.getComponentAt(i).setName("TSD Graph");
                }
              }
              if (refresh)
              {
                if (simTab.getComponentAt(i).getName().equals("Histogram"))
                {
                  if (simTab.getComponentAt(i) instanceof Graph)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                  else
                  {
                    if (new File(filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "sim-rep.txt").exists())
                    {
                      simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
                      simTab.getComponentAt(i).setName("Histogram");
                    }
                  }
                }
              }
            }
          }
          else if (monteCarlo.isSelected())
          {
            for (int i = 0; i < simTab.getComponentCount(); i++)
            {
              if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
              {
                if (simTab.getComponentAt(i) instanceof Graph)
                {
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  if (refresh)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                }
                else
                {
                  simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, true, false));
                  boolean outputM = true;
                  boolean outputV = true;
                  boolean outputS = true;
                  boolean outputTerm = false;
                  boolean warning = false;
                  ArrayList<String> run = new ArrayList<String>();
                  for (String f : work.list())
                  {
                    if (f.contains("mean"))
                    {
                      outputM = false;
                    }
                    else if (f.contains("variance"))
                    {
                      outputV = false;
                    }
                    else if (f.contains("standard_deviation"))
                    {
                      outputS = false;
                    }
                    if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
                    {
                      run.add(f);
                    }
                    else if (f.equals("term-time.txt"))
                    {
                      outputTerm = true;
                    }
                  }
                  if (genStats && (outputM || outputV || outputS))
                  {
                    warning = ((Graph) simTab.getComponentAt(i)).getWarning();
                    ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
                  }
                  new File(directory + GlobalConstants.separator + "running").delete();
                  logFile.close();
                  if (outputTerm)
                  {
                    ArrayList<String> dataLabels = new ArrayList<String>();
                    dataLabels.add("time");
                    ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
                    if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
                    {
                      try
                      {
                        Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
                        if (s.hasNextLine())
                        {
                          String[] ss = s.nextLine().split(" ");
                          if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                          {
                          }
                          else
                          {
                            for (String add : ss)
                            {
                              if (!add.equals("#total") && !add.equals("time-limit"))
                              {
                                dataLabels.add(add);
                                ArrayList<Double> times = new ArrayList<Double>();
                                terms.add(times);
                              }
                            }
                          }
                        }
                        s.close();
                      }
                      catch (Exception e)
                      {
                        e.printStackTrace();
                      }
                    }
                    Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
                    while (scan.hasNextLine())
                    {
                      String line = scan.nextLine();
                      String[] term = line.split(" ");
                      if (!dataLabels.contains(term[0]))
                      {
                        dataLabels.add(term[0]);
                        ArrayList<Double> times = new ArrayList<Double>();
                        times.add(Double.parseDouble(term[1]));
                        terms.add(times);
                      }
                      else
                      {
                        terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
                      }
                    }
                    scan.close();
                    ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
                    ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
                    for (int j = 0; j < dataLabels.size(); j++)
                    {
                      ArrayList<Double> temp = new ArrayList<Double>();
                      temp.add(0.0);
                      data.add(temp);
                      temp = new ArrayList<Double>();
                      temp.add(0.0);
                      percentData.add(temp);
                    }
                    for (double j = printInterval; j <= timeLimit; j += printInterval)
                    {
                      data.get(0).add(j);
                      percentData.get(0).add(j);
                      for (int k = 1; k < dataLabels.size(); k++)
                      {
                        data.get(k).add(data.get(k).get(data.get(k).size() - 1));
                        percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
                        for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
                        {
                          if (terms.get(k - 1).get(l) < j)
                          {
                            data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
                            percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
                            terms.get(k - 1).remove(l);
                          }
                        }
                      }
                    }
                    DataParser probData = new DataParser(dataLabels, data);
                    probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
                    probData = new DataParser(dataLabels, percentData);
                    probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
                  }
                  simTab.getComponentAt(i).setName("TSD Graph");
                }
              }
              if (refresh)
              {
                if (simTab.getComponentAt(i).getName().equals("Histogram"))
                {
                  if (simTab.getComponentAt(i) instanceof Graph)
                  {
                    ((Graph) simTab.getComponentAt(i)).refresh();
                  }
                  else
                  {
                    if (new File(filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "sim-rep.txt").exists())
                    {
                      simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
                      simTab.getComponentAt(i).setName("Histogram");
                    }
                  }
                }
              }
            }
          }
        }
      }
      new File(directory + GlobalConstants.separator + "running").delete();
      logFile.close();
    }
    catch (InterruptedException e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Error In Execution!", "Error In Execution", JOptionPane.ERROR_MESSAGE);
      e1.printStackTrace();
    }
    catch (IOException e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "File I/O Error!", "File I/O Error", JOptionPane.ERROR_MESSAGE);
      e1.printStackTrace();
    }
    return exitValue;
  }

  public static String createTimeString(long time1, long time2)
  {
    long minutes;
    long hours;
    long days;
    double secs = ((time2 - time1) / 1000000000.0);
    long seconds = ((time2 - time1) / 1000000000);
    secs = secs - seconds;
    minutes = seconds / 60;
    secs = seconds % 60 + secs;
    hours = minutes / 60;
    minutes = minutes % 60;
    days = hours / 24;
    hours = hours % 60;
    String time;
    String dayLabel;
    String hourLabel;
    String minuteLabel;
    String secondLabel;
    if (days == 1)
    {
      dayLabel = " day ";
    }
    else
    {
      dayLabel = " days ";
    }
    if (hours == 1)
    {
      hourLabel = " hour ";
    }
    else
    {
      hourLabel = " hours ";
    }
    if (minutes == 1)
    {
      minuteLabel = " minute ";
    }
    else
    {
      minuteLabel = " minutes ";
    }
    if (seconds == 1)
    {
      secondLabel = " second";
    }
    else
    {
      secondLabel = " seconds";
    }
    if (days != 0)
    {
      time = days + dayLabel + hours + hourLabel + minutes + minuteLabel + secs + secondLabel;
    }
    else if (hours != 0)
    {
      time = hours + hourLabel + minutes + minuteLabel + secs + secondLabel;
    }
    else if (minutes != 0)
    {
      time = minutes + minuteLabel + secs + secondLabel;
    }
    else
    {
      time = secs + secondLabel;
    }
    return time;
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

  @Override
  public void update(Observable o, Object arg) {
    // TODO Auto-generated method stub
    Message message = (Message) arg;
    
    if(message.isCancel())
    {
      JOptionPane.showMessageDialog(Gui.frame, "Simulation Canceled", "Canceled", JOptionPane.ERROR_MESSAGE);
    }
    else if(message.isConsole())
    {
      System.out.println(message.getMessage());
    }
    else if(message.isErrorDialog())
    {
      JOptionPane.showMessageDialog(Gui.frame, message.getMessage(), message.getTitle(), JOptionPane.ERROR_MESSAGE);
    }
    else if(message.isDialog())
    {
      JOptionPane.showMessageDialog(Gui.frame, message.getMessage(), message.getTitle(), JOptionPane.PLAIN_MESSAGE);
    }
  }
}
