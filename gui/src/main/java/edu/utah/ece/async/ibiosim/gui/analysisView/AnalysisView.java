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
package edu.utah.ece.async.ibiosim.gui.analysisView;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.xml.stream.XMLStreamException;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Algorithm;
import org.jlibsedml.AlgorithmParameter;
import org.jlibsedml.Annotation;
import org.jlibsedml.DataGenerator;
import org.jlibsedml.Libsedml;
import org.jlibsedml.OneStep;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.SedML;
import org.jlibsedml.Simulation;
import org.jlibsedml.SteadyState;
import org.jlibsedml.Task;
import org.jlibsedml.UniformTimeCourse;
import org.jlibsedml.Variable;
import org.jlibsedml.VariableSymbol;
import org.jlibsedml.modelsupport.KisaoOntology;
import org.jlibsedml.modelsupport.KisaoTerm;
//import org.jmathml.ASTNode;
import org.sbml.libsbml.ASTNode;

import edu.utah.ece.async.ibiosim.analysis.util.SEDMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.graphEditor.Graph;
import edu.utah.ece.async.ibiosim.gui.lpnEditor.LHPNEditor;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;
import edu.utah.ece.async.ibiosim.gui.util.Log;
import edu.utah.ece.async.ibiosim.gui.util.Utility;
import edu.utah.ece.async.ibiosim.gui.verificationView.AbstractionPanel;
import edu.utah.ece.async.lema.verification.lpn.Abstraction;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Translator;

/**
 * This class creates a GUI for analysis. It implements the ActionListener
 * class, the KeyListener class, the MouseListener class, and the Runnable
 * class. This allows the GUI to perform actions when buttons are pressed, text
 * is entered into a field, or the mouse is clicked. It also allows this class
 * to execute many analysis programs at the same time on different threads.
 * 
 * @author Curtis Madsen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class AnalysisView extends JPanel implements ActionListener, Runnable, MouseListener
{

  private static final long	serialVersionUID	= 3181014495993143825L;

  private JRadioButton		noAbstraction, expandReactions, reactionAbstraction, stateAbstraction;

  private JRadioButton		ODE, monteCarlo, markov, fba, sbml, dot, xhtml;

  private JTextField			initialTimeField, outputStartTimeField, limit, interval, minStep, step, relErr, absErr, seed, runs, fileStem;

  private JComboBox			intervalLabel;

  private JLabel				initialTimeLabel, outputStartTimeLabel, limitLabel, minStepLabel, stepLabel, relErrorLabel, errorLabel, seedLabel, runsLabel, fileStemLabel;

  private JLabel				description, explanation;

  private Object[]			preAbstractions		= new Object[0];

  private Object[]			loopAbstractions	= new Object[0];

  private Object[]			postAbstractions	= new Object[0];

  private JComboBox			simulators;																			// Combo

  private JLabel				simulatorsLabel;																		// Label

  private JTextField			rapid1, rapid2, qssa, maxCon, diffStoichAmp;											// advanced

  private JComboBox			bifurcation;

  private JRadioButton		mpde, meanPath, medianPath;

  private JRadioButton		adaptive, nonAdaptive;

  private JLabel				rapidLabel1, rapidLabel2, qssaLabel, maxConLabel, diffStoichAmpLabel, iSSATypeLabel, iSSAAdaptiveLabel, bifurcationLabel;

  private String				sbmlFile;																													// sbml

  private final String		root;

  private final Gui			gui;																														// reference

  private String				simName;																													// simulation

  private final Log			log;																														// the

  private final JTabbedPane	simTab;																													// the

  private ModelEditor			modelEditor;																												// model

  private JCheckBox			append, concentrations, genRuns, genStats;

  private JLabel				report;

  private String				sbmlProp;

  private boolean				change;

  private final Pattern		stemPat				= Pattern.compile("([a-zA-Z]|[0-9]|_)*");

  private JPanel				advanced;

  private JList				preAbs, loopAbs, postAbs;

  private JLabel				preAbsLabel, loopAbsLabel, postAbsLabel;

  private JButton				addPreAbs, rmPreAbs, editPreAbs;

  private JButton				addLoopAbs, rmLoopAbs, editLoopAbs;

  private JButton				addPostAbs, rmPostAbs, editPostAbs;

  private final String		modelFile;

  private final AbstractionPanel		lpnAbstraction;

  private JComboBox			transientProperties;

  private final JTextField	modelFileField, taskId;

  private JComboBox			subTaskList;

  private String				selectedMarkovSim	= null;

  private ArrayList<String>	interestingSpecies	= null;

  private SEDMLDocument		sedmlDoc			= null;

  private final Preferences	biosimrc;

  private double				initialTime;
  private double				outputStartTime;
  private double				timeLimit;
  private double				printInterval;
  private double				minTimeStep;
  private double				timeStep;
  private double				absError;
  private double				relError;
  private long				rndSeed;
  private int					run;
  private String				outDir;
  private String[]			intSpecies;
  private double				rap1;
  private double				rap2;
  private double				qss;
  private int					con;
  private String				printer_id;
  private String				printer_track_quantity;
  private String				sim;
  private String				simProp;

  /**
   * This is the constructor for the GUI. It initializes all the input fields,
   * puts them on panels, adds the panels to the frame, and then displays the
   * GUI.
   * 
   * @param gui
   *            - the main GUI window object
   * @param log
   *            - the log for the console
   * @param simTab
   *            - the tabbedPane this is to be added too
   * @param lpnAbstraction
   *            - the abstraction pane for LPN abstraction options
   * @param root
   *            - the project root directory path
   * @param simName
   *            - the name of the analysis view
   * @param modelFile
   *            - the SBML model file
   */
  public AnalysisView(Gui gui, Log log, JTabbedPane simTab, AbstractionPanel lpnAbstraction, String root, String simName, String modelFile)
  {

    super(new BorderLayout());
    this.gui = gui;
    this.log = log;
    this.simTab = simTab;
    this.lpnAbstraction = lpnAbstraction;
    this.root = root;
    this.simName = simName;
    this.modelFile = modelFile;
    if (modelFile.endsWith(".lpn"))
    {
      sbmlFile = root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".xml");
    }
    else
    {
      sbmlFile = root + GlobalConstants.separator + modelFile;
    }
    sbmlProp = root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile.replace(".lpn", ".xml");
    interestingSpecies = new ArrayList<String>();
    change = false;
    biosimrc = Preferences.userRoot();

    JPanel modelFilePanel = new JPanel();

    fileStemLabel = new JLabel("Task ID:");
    taskId = new JTextField(simName);
    taskId.setEditable(false);
    fileStem = new JTextField("", 15);
    subTaskList = new JComboBox();
    subTaskList.addItem("(none)");

    JLabel modelFileLabel = new JLabel("Model File:");
    modelFileField = new JTextField(this.modelFile);
    modelFileField.setEditable(false);
    modelFilePanel.add(fileStemLabel);
    modelFilePanel.add(taskId);
    modelFilePanel.add(fileStem);
    modelFilePanel.add(subTaskList);
    modelFilePanel.add(modelFileLabel);
    modelFilePanel.add(modelFileField);

    JPanel abstractionOptions = createAbstractionOptions();
    JPanel simulationTypeOptions = createSimulationTypeOptions();
    JPanel simulationOptions = createSimulationOptions();
    JPanel reportOptions = createReportOptions();
    createAdvancedOptionsTab();
    setDefaultOptions();

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(modelFilePanel, BorderLayout.NORTH);
    topPanel.add(abstractionOptions, BorderLayout.SOUTH);
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(topPanel, BorderLayout.NORTH);
    buttonPanel.add(simulationTypeOptions, BorderLayout.CENTER);
    buttonPanel.add(reportOptions, BorderLayout.SOUTH);
    this.add(buttonPanel, BorderLayout.NORTH);
    this.add(simulationOptions, BorderLayout.CENTER);
    loadPropertiesFile(simName, modelFile.replace(".xml", ""));
    loadSEDML("");
    subTaskList.addActionListener(this);
  }

  public int getNumRuns() {
    try {
      return Integer.parseInt(runs.getText());
    } catch (Exception e) {
      return 1;
    }
  }

  /* Creates the abstraction radio button options */
  private JPanel createAbstractionOptions()
  {
    JLabel choose = new JLabel("Abstraction:");
    noAbstraction = new JRadioButton("None");
    expandReactions = new JRadioButton("Expand Reactions");
    if (modelFile.contains(".lpn"))
    {
      reactionAbstraction = new JRadioButton("Safe net reductions");
    }
    else
    {
      reactionAbstraction = new JRadioButton("Reaction-based");
    }
    stateAbstraction = new JRadioButton("State-based");
    noAbstraction.addActionListener(this);
    expandReactions.addActionListener(this);
    reactionAbstraction.addActionListener(this);
    stateAbstraction.addActionListener(this);
    ButtonGroup abstractionButtons = new ButtonGroup();
    abstractionButtons.add(noAbstraction);
    abstractionButtons.add(expandReactions);
    abstractionButtons.add(reactionAbstraction);
    abstractionButtons.add(stateAbstraction);
    noAbstraction.setSelected(true);
    JPanel abstractionOptions = new JPanel();
    abstractionOptions.add(choose);
    abstractionOptions.add(noAbstraction);
    if (!modelFile.contains(".lpn"))
    {
      abstractionOptions.add(expandReactions);
    }
    abstractionOptions.add(reactionAbstraction);
    if (!modelFile.contains(".lpn"))
    {
      abstractionOptions.add(stateAbstraction);
    }
    return abstractionOptions;
  }

  /* Creates the radio buttons for selecting the simulation type */
  private JPanel createSimulationTypeOptions()
  {
    JLabel choose2 = new JLabel("Analysis Type:");
    ODE = new JRadioButton("ODE");
    monteCarlo = new JRadioButton("Monte Carlo");
    markov = new JRadioButton("Markov");
    fba = new JRadioButton("FBA");
    sbml = new JRadioButton("Model");
    dot = new JRadioButton("Network");
    xhtml = new JRadioButton("Browser");
    ODE.addActionListener(this);
    monteCarlo.addActionListener(this);
    markov.addActionListener(this);
    fba.addActionListener(this);
    sbml.addActionListener(this);
    dot.addActionListener(this);
    xhtml.addActionListener(this);
    ButtonGroup simulationTypeButtons = new ButtonGroup();
    simulationTypeButtons.add(ODE);
    simulationTypeButtons.add(monteCarlo);
    simulationTypeButtons.add(markov);
    simulationTypeButtons.add(fba);
    simulationTypeButtons.add(sbml);
    simulationTypeButtons.add(dot);
    simulationTypeButtons.add(xhtml);
    if (!Gui.isReb2sacFound())
    {
      dot.setEnabled(false);
      xhtml.setEnabled(false);
    }
    ODE.setSelected(true);
    JPanel simulationTypeOptions = new JPanel();
    simulationTypeOptions.add(choose2);
    simulationTypeOptions.add(ODE);
    simulationTypeOptions.add(monteCarlo);
    simulationTypeOptions.add(markov);
    simulationTypeOptions.add(fba);
    simulationTypeOptions.add(sbml);
    simulationTypeOptions.add(dot);
    simulationTypeOptions.add(xhtml);
    return simulationTypeOptions;
  }

  /* Set the default simulation options from the preferences */
  private void setDefaultOptions()
  {
    if (biosimrc.get("biosim.sim.abs", "").equals("None"))
    {
      noAbstraction.doClick();
    }
    else if (biosimrc.get("biosim.sim.abs", "").equals("Expand Reactions"))
    {
      expandReactions.doClick();
    }
    else if (biosimrc.get("biosim.sim.abs", "").equals("Reaction-based"))
    {
      reactionAbstraction.doClick();
    }
    else
    {
      stateAbstraction.doClick();
    }
    if (biosimrc.get("biosim.sim.type", "").equals("ODE"))
    {
      ODE.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
    }
    else if (biosimrc.get("biosim.sim.type", "").equals("Monte Carlo"))
    {
      monteCarlo.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
    }
    else if (biosimrc.get("biosim.sim.type", "").equals("Markov"))
    {
      markov.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
      if (!simulators.getSelectedItem().equals(biosimrc.get("biosim.sim.sim", "")))
      {
        selectedMarkovSim = biosimrc.get("biosim.sim.sim", "");
      }
    }
    else if (biosimrc.get("biosim.sim.type", "").equals("FBA"))
    {
      fba.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
    }
    else if (biosimrc.get("biosim.sim.type", "").equals("SBML"))
    {
      sbml.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
      sbml.doClick();
    }
    else if (biosimrc.get("biosim.sim.type", "").equals("Network"))
    {
      dot.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
      dot.doClick();
    }
    else
    {
      xhtml.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
      xhtml.doClick();
    }
  }

  /* Create the report option buttons */
  private JPanel createReportOptions()
  {
    JPanel reportOptions = new JPanel();
    report = new JLabel("Report Options:");
    reportOptions.add(report);

    concentrations = new JCheckBox("Report Concentrations");
    concentrations.setEnabled(true);
    reportOptions.add(concentrations);
    concentrations.addActionListener(this);

    genRuns = new JCheckBox("Do Not Generate Runs");
    genRuns.setEnabled(true);
    reportOptions.add(genRuns);
    genRuns.addActionListener(this);

    append = new JCheckBox("Append Simulation Runs");
    append.setEnabled(true);
    reportOptions.add(append);
    append.addActionListener(this);

    genStats = new JCheckBox("Generate Statistics");
    genStats.setEnabled(true);
    reportOptions.add(genStats);
    genStats.addActionListener(this);
    return reportOptions;
  }

  /* Create the simulation option fields */
  private JPanel createSimulationOptions()
  {
    explanation = new JLabel("Description Of Selected Simulator:     ");
    description = new JLabel("");
    simulatorsLabel = new JLabel("Possible Simulators/Analyzers:");
    simulators = new JComboBox();
    // simulators.setSelectedItem("rkf45");
    simulators.addActionListener(this);
    initialTimeLabel = new JLabel("Initial Time:");
    initialTimeField = new JTextField(biosimrc.get("biosim.sim.initial.time", ""), 15);
    outputStartTimeLabel = new JLabel("Output Start Time:");
    outputStartTimeField = new JTextField(biosimrc.get("biosim.sim.output.start.time", ""), 15);
    limitLabel = new JLabel("Time Limit:");
    limit = new JTextField(biosimrc.get("biosim.sim.limit", ""), 15);
    String[] intervalChoices = { "Print Interval", "Minimum Print Interval", "Number Of Steps" };
    intervalLabel = new JComboBox(intervalChoices);
    intervalLabel.setSelectedItem(biosimrc.get("biosim.sim.useInterval", ""));
    interval = new JTextField(biosimrc.get("biosim.sim.interval", ""), 15);
    minStepLabel = new JLabel("Minimum Time Step:");
    minStep = new JTextField(biosimrc.get("biosim.sim.min.step", ""), 15);
    stepLabel = new JLabel("Maximum Time Step:");
    step = new JTextField(biosimrc.get("biosim.sim.step", ""), 15);
    errorLabel = new JLabel("Absolute Error:");
    absErr = new JTextField(biosimrc.get("biosim.sim.error", ""), 15);
    relErrorLabel = new JLabel("Relative Error:");
    relErr = new JTextField(biosimrc.get("biosim.sim.relative.error", ""), 15);
    seedLabel = new JLabel("Random Seed:");
    seed = new JTextField(biosimrc.get("biosim.sim.seed", ""), 15);
    runsLabel = new JLabel("Runs:");
    runs = new JTextField(biosimrc.get("biosim.sim.runs", ""), 15);
    JPanel inputHolder = new JPanel(new BorderLayout());
    JPanel inputHolderLeft;
    JPanel inputHolderRight;
    if (modelFile.contains(".lpn"))
    {
      inputHolderLeft = new JPanel(new GridLayout(3, 1));
      inputHolderRight = new JPanel(new GridLayout(3, 1));
    }
    else
    {
      inputHolderLeft = new JPanel(new GridLayout(2, 1));
      inputHolderRight = new JPanel(new GridLayout(2, 1));
    }
    inputHolderLeft.add(simulatorsLabel);
    inputHolderRight.add(simulators);
    inputHolderLeft.add(explanation);
    inputHolderRight.add(description);
    if (modelFile.contains(".lpn"))
    {
      JLabel prop = new JLabel("Property:");
      String[] props = new String[] { "none" };
      LPN lpn = new LPN();
      try {
        lpn.load(root + GlobalConstants.separator + modelFile);
        String[] getProps = lpn.getProperties().toArray(new String[0]);
        props = new String[getProps.length + 1];
        props[0] = "none";
        for (int i = 0; i < getProps.length; i++)
        {
          props[i + 1] = getProps[i];
        }
        transientProperties = new JComboBox(props);
        transientProperties.setPreferredSize(new Dimension(5, 10));
        inputHolderLeft.add(prop);
        inputHolderRight.add(transientProperties);
      } catch (BioSimException e) {
        JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(), JOptionPane.ERROR_MESSAGE); 
        e.printStackTrace();
      }
      
    }	
    inputHolder.add(inputHolderLeft, "West");
    inputHolder.add(inputHolderRight, "Center");
    JPanel simulationOptions = new JPanel();
    simulationOptions.add(inputHolder);

    JPanel simOptionsHolder = new JPanel(new BorderLayout());
    JPanel simOptionsHolderLeft;
    JPanel simOptionsHolderRight;
    simOptionsHolderLeft = new JPanel(new GridLayout(5, 2));
    simOptionsHolderRight = new JPanel(new GridLayout(5, 2));
    simOptionsHolderLeft.add(initialTimeLabel);
    simOptionsHolderLeft.add(initialTimeField);
    simOptionsHolderRight.add(outputStartTimeLabel);
    simOptionsHolderRight.add(outputStartTimeField);
    simOptionsHolderLeft.add(limitLabel);
    simOptionsHolderLeft.add(limit);
    simOptionsHolderRight.add(intervalLabel);
    simOptionsHolderRight.add(interval);
    simOptionsHolderLeft.add(minStepLabel);
    simOptionsHolderLeft.add(minStep);
    simOptionsHolderRight.add(stepLabel);
    simOptionsHolderRight.add(step);
    simOptionsHolderLeft.add(errorLabel);
    simOptionsHolderLeft.add(absErr);
    simOptionsHolderRight.add(relErrorLabel);
    simOptionsHolderRight.add(relErr);
    simOptionsHolderLeft.add(seedLabel);
    simOptionsHolderLeft.add(seed);
    simOptionsHolderRight.add(runsLabel);
    simOptionsHolderRight.add(runs);
    simOptionsHolder.add(simOptionsHolderLeft, "West");
    simOptionsHolder.add(simOptionsHolderRight, "Center");
    simulationOptions.add(simOptionsHolder);
    return simulationOptions;
  }

  /**
   * This method performs different functions depending on what buttons are
   * pushed and what input fields contain data.
   */
  @Override
  public void actionPerformed(ActionEvent e)
  {
    change = true;
    if (e.getSource() == subTaskList) {
      String subTask = "";
      if (!((String)subTaskList.getSelectedItem()).equals("(none)")) {
        subTask = (String)subTaskList.getSelectedItem();
      }
      saveSEDML();
      loadSEDML(subTask);
    }
    else if (e.getSource() == noAbstraction || e.getSource() == expandReactions)
    {
      enableNoAbstraction();
    }
    else if (e.getSource() == reactionAbstraction)
    {
      enableReactionAbstraction();
    }
    else if (e.getSource() == stateAbstraction)
    {
      enableStateAbstraction();
    }
    else if (e.getSource() == ODE)
    {
      enableODE();
    }
    else if (e.getSource() == monteCarlo)
    {
      enableMonteCarlo();
    }
    else if (e.getSource() == markov)
    {
      enableMarkov();
    }
    else if (e.getSource() == fba)
    {
      enableFBA();
    }
    else if (e.getSource() == sbml || e.getSource() == dot || e.getSource() == xhtml)
    {
      enableSbmlDotAndXhtml();
    }
    else if (e.getSource() == mpde)
    {
      enableMPDEMethod();
    }
    else if (e.getSource() == meanPath)
    {
      enableMeanMedianPath();
    }
    else if (e.getSource() == medianPath)
    {
      enableMeanMedianPath();
    }
    else if (e.getSource() == simulators)
    {
      if (simulators.getItemCount() == 0)
      {
        description.setText("");
        disableiSSASimulatorOptions();
      }
      else if (simulators.getSelectedItem().equals("euler"))
      {
        description.setText("Euler method");
        enableEulerMethod();
      }
      else if (simulators.getSelectedItem().equals("gear1"))
      {
        description.setText("Gear method, M=1");
        enableODESimulator();
      }
      else if (simulators.getSelectedItem().equals("gear2"))
      {
        description.setText("Gear method, M=2");
        enableODESimulator();
      }
      else if (simulators.getSelectedItem().equals("rk4imp"))
      {
        description.setText("Implicit 4th order Runge-Kutta at Gaussian points");
        enableODESimulator();
      }
      else if (simulators.getSelectedItem().equals("rk8pd"))
      {
        description.setText("Embedded Runge-Kutta Prince-Dormand (8,9) method");
        enableODESimulator();
      }
      else if (simulators.getSelectedItem().equals("rkf45"))
      {
        description.setText("Embedded Runge-Kutta-Fehlberg (4, 5) method");
        enableODESimulator();
      }
      else if (((String) simulators.getSelectedItem()).equals("SSA-CR (Dynamic)"))
      {
        description.setText("SSA Composition and Rejection Method");
        enableSSASimulator();
      }
      else if (((String) simulators.getSelectedItem()).equals("SSA-Direct (Dynamic)"))
      {
        description.setText("SSA-Direct Method (Java)");
        enableSSASimulator();
      }
      else if (((String) simulators.getSelectedItem()).equals("Runge-Kutta-Fehlberg (Dynamic)"))
      {
        description.setText("Runge-Kutta-Fehlberg Method (java)");
        enableODESimulator();
      }
      else if (((String) simulators.getSelectedItem()).equals("Runge-Kutta-Fehlberg (Hierarchical)"))
      {
        description.setText("Runge-Kutta-Fehlberg Method on Hierarchical Models (java)");
        enableODESimulator();
      }
      else if (((String) simulators.getSelectedItem()).equals("Mixed-Hierarchical"))
      {
        description.setText("FBA+SSA+ODE Simulator");
        enableODESimulator();
      }
      else if (((String) simulators.getSelectedItem()).equals("Runge-Kutta-Fehlberg (Flatten)"))
      {
        description.setText("Runge-Kutta-Fehlberg Method on Flattened Models (java)");
        enableODESimulator();
      }
      else if (((String) simulators.getSelectedItem()).equals("Hierarchical-Hybrid"))
      {
        description.setText("Hybrid SSA/ODE on Hierarchical Models (java)");
        enableSSASimulator();
      }
      else if (((String) simulators.getSelectedItem()).contains("gillespie"))
      {
        description.setText("SSA-Direct Method");
        enableSSASimulator();
      }
      else if (((String) simulators.getSelectedItem()).contains("SSA-Direct (Flatten)"))
      {
        description.setText("SSA-Direct Method on Flattened Models (java)");
        enableSSASimulator();
      }
      else if (((String) simulators.getSelectedItem()).contains("SSA-Direct (Hierarchical)"))
      {
        description.setText("SSA-Direct Method on Hierarchical Models (java)");
        enableSSASimulator();
      }
      else if (((String) simulators.getSelectedItem()).contains("interactive"))
      {
        description.setText("Interactive SSA-Direct Method (java)");
        enableSSASimulator();
      }
      else if (simulators.getSelectedItem().equals("iSSA"))
      {
        description.setText("incremental SSA");
        enableiSSASimulator();
      }
      else if (simulators.getSelectedItem().equals("emc-sim"))
      {
        description.setText("Monte Carlo sim with jump count as independent variable");
        enableSSASimulator();
      }
      else if (simulators.getSelectedItem().equals("bunker"))
      {
        description.setText("Bunker's method");
        enableSSASimulator();
      }
      else if (simulators.getSelectedItem().equals("nmc"))
      {
        description.setText("Monte Carlo simulation with normally" + " distributed waiting time");
        enableSSASimulator();
      }
      else if (simulators.getSelectedItem().equals("ctmc-transient"))
      {
        description.setText("Transient Distribution Analysis");
        enableMarkovAnalyzer();
      }
      else if (simulators.getSelectedItem().equals("atacs"))
      {
        description.setText("ATACS Analysis Tool");
        enableMarkovAnalyzer();
      }
      else if (simulators.getSelectedItem().equals("prism"))
      {
        description.setText("PRISM Analysis Tool");
        enableMarkovAnalyzer();
      }
      else if (simulators.getSelectedItem().equals("reachability-analysis"))
      {
        description.setText("State Space Exploration");
        enableMarkovAnalyzer();
      }
      else if (simulators.getSelectedItem().equals("steady-state-markov-chain-analysis"))
      {
        description.setText("Steady State Markov Chain Analysis");
        enableMarkovAnalyzer();
      }
      else if (simulators.getSelectedItem().equals("transient-markov-chain-analysis"))
      {
        description.setText("Transient Markov Chain Analysis Using Uniformization");
        enableTransientMarkovAnalyzer();
      }
    }
    else if ((e.getSource() == addPreAbs) || (e.getSource() == addLoopAbs) || (e.getSource() == addPostAbs))
    {
      addAbstractionMethod(e);
    }
    else if (e.getSource() == rmPreAbs)
    {
      Utility.remove(preAbs);
    }
    else if (e.getSource() == rmLoopAbs)
    {
      Utility.remove(loopAbs);
    }
    else if (e.getSource() == rmPostAbs)
    {
      Utility.remove(postAbs);
    }
    else if (e.getSource() == append)
    {
      setupToAppendRuns(true);
    }
    else if (e.getSource() == fileStem)
    {
      System.out.println("ACTION");
    }
  }

  /* Add an abstraction method */
  private void addAbstractionMethod(ActionEvent e)
  {
    JPanel addAbsPanel = new JPanel(new BorderLayout());
    JComboBox absList = new JComboBox();
    if (e.getSource() == addPreAbs)
    {
      absList.addItem("complex-formation-and-sequestering-abstraction");
    }
    absList.addItem("operator-site-reduction-abstraction");
    absList.addItem("absolute-activation/inhibition-generator");
    absList.addItem("absolute-inhibition-generator");
    absList.addItem("birth-death-generator");
    absList.addItem("birth-death-generator2");
    absList.addItem("birth-death-generator3");
    absList.addItem("birth-death-generator4");
    absList.addItem("birth-death-generator5");
    absList.addItem("birth-death-generator6");
    absList.addItem("birth-death-generator7");
    absList.addItem("degradation-stoichiometry-amplifier");
    absList.addItem("degradation-stoichiometry-amplifier2");
    absList.addItem("degradation-stoichiometry-amplifier3");
    absList.addItem("degradation-stoichiometry-amplifier4");
    absList.addItem("degradation-stoichiometry-amplifier5");
    absList.addItem("degradation-stoichiometry-amplifier6");
    absList.addItem("degradation-stoichiometry-amplifier7");
    absList.addItem("degradation-stoichiometry-amplifier8");
    absList.addItem("dimer-to-monomer-substitutor");
    absList.addItem("dimerization-reduction");
    absList.addItem("dimerization-reduction-level-assignment");
    absList.addItem("distribute-transformer");
    absList.addItem("enzyme-kinetic-qssa-1");
    absList.addItem("enzyme-kinetic-rapid-equilibrium-1");
    absList.addItem("enzyme-kinetic-rapid-equilibrium-2");
    absList.addItem("final-state-generator");
    absList.addItem("inducer-structure-transformer");
    absList.addItem("irrelevant-species-remover");
    absList.addItem("kinetic-law-constants-simplifier");
    absList.addItem("max-concentration-reaction-adder");
    absList.addItem("modifier-constant-propagation");
    absList.addItem("modifier-structure-transformer");
    absList.addItem("multiple-products-reaction-eliminator");
    absList.addItem("multiple-reactants-reaction-eliminator");
    absList.addItem("nary-order-unary-transformer");
    absList.addItem("nary-order-unary-transformer2");
    absList.addItem("nary-order-unary-transformer3");
    absList.addItem("operator-site-forward-binding-remover");
    absList.addItem("operator-site-forward-binding-remover2");
    absList.addItem("pow-kinetic-law-transformer");
    absList.addItem("ppta");
    absList.addItem("reversible-reaction-structure-transformer");
    absList.addItem("reversible-to-irreversible-transformer");
    absList.addItem("similar-reaction-combiner");
    absList.addItem("single-reactant-product-reaction-eliminator");
    absList.addItem("stoichiometry-amplifier");
    absList.addItem("stoichiometry-amplifier2");
    absList.addItem("stoichiometry-amplifier3");
    absList.addItem("stop-flag-generator");
    addAbsPanel.add(absList, "Center");
    String[] options = { "Add", "Cancel" };
    int value = JOptionPane.showOptionDialog(Gui.frame, addAbsPanel, "Add abstraction method", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    if (value == JOptionPane.YES_OPTION)
    {
      if (e.getSource() == addPreAbs)
      {
        Utility.add(preAbs, absList.getSelectedItem());
      }
      else if (e.getSource() == addLoopAbs)
      {
        Utility.add(loopAbs, absList.getSelectedItem());
      }
      else
      {
        Utility.add(postAbs, absList.getSelectedItem());
      }
    }
  }

  /* Setup to append runs */
  private void setupToAppendRuns(boolean newSeed)
  {
    if (append.isSelected())
    {
      initialTimeLabel.setEnabled(false);
      initialTimeField.setEnabled(false);
      outputStartTimeLabel.setEnabled(false);
      outputStartTimeField.setEnabled(false);
      limit.setEnabled(false);
      interval.setEnabled(false);
      limitLabel.setEnabled(false);
      intervalLabel.setEnabled(false);
      if (newSeed)
      {
        Random rnd = new Random();
        seed.setText("" + rnd.nextInt());
      }
      // TODO: Does this part actually still work. Seems you need to look
      // at the runs themselves.
      int cut = 0;
      sbmlProp = sbmlProp.replace("\\", "/");
      String[] getFilename = sbmlProp.split("/");
      for (int i = 0; i < getFilename[getFilename.length - 1].length(); i++)
      {
        if (getFilename[getFilename.length - 1].charAt(i) == '.')
        {
          cut = i;
        }
      }
      String propName = sbmlProp.substring(0, sbmlProp.length() - getFilename[getFilename.length - 1].length()) + getFilename[getFilename.length - 1].substring(0, cut) + ".properties";
      try
      {
        if (new File(propName).exists())
        {
          Properties getProps = new Properties();
          FileInputStream load = new FileInputStream(new File(propName));
          getProps.load(load);
          load.close();
          if (getProps.containsKey("monte.carlo.simulation.time.limit"))
          {
            initialTimeField.setText(getProps.getProperty("simulation.initial.time"));
            outputStartTimeField.setText(getProps.getProperty("simulation.output.start.time"));
            minStep.setText(getProps.getProperty("monte.carlo.simulation.min.time.step"));
            step.setText(getProps.getProperty("monte.carlo.simulation.time.step"));
            limit.setText(getProps.getProperty("monte.carlo.simulation.time.limit"));
            interval.setText(getProps.getProperty("monte.carlo.simulation.print.interval"));
          }
        }
      }
      catch (IOException e1)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Unable to restore time limit and print interval.", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
    else
    {
      initialTimeField.setEnabled(true);
      initialTimeLabel.setEnabled(true);
      outputStartTimeField.setEnabled(true);
      outputStartTimeLabel.setEnabled(true);
      limit.setEnabled(true);
      limitLabel.setEnabled(true);
      interval.setEnabled(true);
      intervalLabel.setEnabled(true);
    }
  }

  private JFrame createProgressBar(JLabel label, JProgressBar progress, final JButton cancel)
  {
    final JFrame running = new JFrame("Progress");
    WindowListener w = new WindowListener()
    {
      @Override
      public void windowClosing(WindowEvent arg0)
      {
        cancel.doClick();
        running.dispose();
      }

      @Override
      public void windowOpened(WindowEvent arg0)
      {
      }

      @Override
      public void windowClosed(WindowEvent arg0)
      {
      }

      @Override
      public void windowIconified(WindowEvent arg0)
      {
      }

      @Override
      public void windowDeiconified(WindowEvent arg0)
      {
      }

      @Override
      public void windowActivated(WindowEvent arg0)
      {
      }

      @Override
      public void windowDeactivated(WindowEvent arg0)
      {
      }
    };
    running.addWindowListener(w);
    JPanel text = new JPanel();
    JPanel progBar = new JPanel();
    JPanel button = new JPanel();
    JPanel all = new JPanel(new BorderLayout());
    progress.setStringPainted(true);
    progress.setValue(0);
    text.add(label);
    progBar.add(progress);
    button.add(cancel);
    all.add(text, "North");
    all.add(progBar, "Center");
    all.add(button, "South");
    running.setContentPane(all);
    running.pack();
    Dimension screenSize;
    try
    {
      Toolkit tk = Toolkit.getDefaultToolkit();
      screenSize = tk.getScreenSize();
    }
    catch (AWTError awe)
    {
      screenSize = new Dimension(640, 480);
    }
    Dimension frameSize = running.getSize();

    if (frameSize.height > screenSize.height)
    {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width)
    {
      frameSize.width = screenSize.width;
    }
    int x = screenSize.width / 2 - frameSize.width / 2;
    int y = screenSize.height / 2 - frameSize.height / 2;
    running.setLocation(x, y);
    running.setVisible(true);
    running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    return running;
  }

  /**
   * If the run button is pressed, this method starts a new thread for the
   * simulation.
   * 
   * @param refresh
   * @throws XMLStreamException
   * @throws NumberFormatException
   */
  public void run(String direct, boolean refresh)
  {
    if (!save(direct))
    {
      return;
    }
    if (monteCarlo.isSelected() || ODE.isSelected())
    {
      if (append.isSelected())
      {
        String[] searchForRunFiles = new File(root + GlobalConstants.separator + outDir).list();
        for (String s : searchForRunFiles)
        {
          if (s.length() > 3 && new File(root + GlobalConstants.separator + outDir + GlobalConstants.separator + s).isFile() && (s.equals("mean.tsd") || s.equals("standard_deviation.tsd") || s.equals("variance.tsd")))
          {
            new File(root + GlobalConstants.separator + outDir + GlobalConstants.separator + s).delete();
          }
        }
      }
      else
      {
        String[] searchForRunFiles = new File(root + GlobalConstants.separator + outDir).list();
        for (String s : searchForRunFiles)
        {
          if (s.length() > 3 && s.substring(0, 4).equals("run-") && new File(root + GlobalConstants.separator + outDir + GlobalConstants.separator + s).isFile())
          {
            new File(root + GlobalConstants.separator + outDir + GlobalConstants.separator + s).delete();
          }
        }
      }
    }
    JProgressBar progress = new JProgressBar(0, 100);
    final JButton cancel = new JButton("Cancel");
    JFrame running;
    JLabel label;
    if (!direct.equals("."))
    {
      label = new JLabel("Running " + simName + " " + direct);
    }
    else
    {
      label = new JLabel("Running " + simName);
    }
    running = createProgressBar(label, progress, cancel);
    // int steps;
    double runTime = timeLimit * run;
    if (simulators.getSelectedItem().equals("iSSA"))
    {
      runTime = timeLimit;
    }
    Run runProgram = new Run(this);
    cancel.addActionListener(runProgram);
    gui.getExitButton().addActionListener(runProgram);
    if (monteCarlo.isSelected() || ODE.isSelected())
    {
      File[] files = new File(root + GlobalConstants.separator + outDir).listFiles();
      for (File f : files)
      {
        if (f.getName().contains("mean.") || f.getName().contains("standard_deviation.") || f.getName().contains("variance."))
        {
          f.delete();
        }
      }
    }
    int exit;
    String lpnProperty = "";
    if (transientProperties != null)
    {
      if (!((String) transientProperties.getSelectedItem()).equals("none"))
      {
        lpnProperty = ((String) transientProperties.getSelectedItem());
      }
    }
    String simulationName = simName;
    String directory = null;
    if (!direct.equals("."))
    {
      simulationName = simName + GlobalConstants.separator + direct;
      directory = direct;
    }
    try {
      exit = runProgram.execute(simProp, fba, sbml, dot, xhtml, Gui.frame, ODE, monteCarlo, sim, printer_id, printer_track_quantity, root
        + GlobalConstants.separator + simName, stateAbstraction, 1, intSpecies, log, gui, simTab, root, progress, simulationName, modelEditor, directory,
        initialTime, outputStartTime, timeLimit, runTime, modelFile, lpnAbstraction, reactionAbstraction, expandReactions, lpnProperty, absError, relError, timeStep, printInterval,
        run, rndSeed, refresh, label, running);
      if (stateAbstraction.isSelected() && modelEditor == null && !sim.contains("markov-chain-analysis") && exit == 0)
      {
        simProp = simProp.replace("\\", "/");
        Nary_Run nary_Run = new Nary_Run(this, simulators, simProp.split("/"), simProp, fba, sbml, dot, xhtml, stateAbstraction, ODE, monteCarlo,
          initialTime, outputStartTime, timeLimit, ((String) (intervalLabel.getSelectedItem())), printInterval, minTimeStep, timeStep, root + GlobalConstants.separator + simName,
          rndSeed, run, printer_id, printer_track_quantity, intSpecies, rap1, rap2, qss, con, log, gui, simTab, root, directory, modelFile,
          reactionAbstraction, lpnAbstraction, absError, relError);
        nary_Run.open();
      }
      running.setCursor(null);
      running.dispose();
      gui.getExitButton().removeActionListener(runProgram);
      if (append.isSelected())
      {
        Random rnd = new Random();
        seed.setText("" + rnd.nextInt());
      }
      for (int i = 0; i < gui.getTab().getTabCount(); i++)
      {
        if (gui.getTab().getComponentAt(i) instanceof Graph)
        {
          ((Graph) gui.getTab().getComponentAt(i)).refresh();
        }
      }
    } catch (BioSimException e) {
      JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(), JOptionPane.ERROR_MESSAGE);
      
    }
    

  }

  public int getStartIndex(String outDir) {
    if (append.isSelected())
    {
      String[] searchForRunFiles = new File(root + GlobalConstants.separator + outDir).list();
      int start = 1;
      for (String s : searchForRunFiles)
      {
        if (s.length() > 3 && s.substring(0, 4).equals("run-") && new File(root + GlobalConstants.separator + outDir + GlobalConstants.separator + s).isFile())
        {
          String getNumber = s.substring(4, s.length());
          String number = "";
          for (int i = 0; i < getNumber.length(); i++)
          {
            if (Character.isDigit(getNumber.charAt(i)))
            {
              number += getNumber.charAt(i);
            }
            else
            {
              break;
            }
          }
          start = Math.max(Integer.parseInt(number), start);
        }
      }
      return (start+1);
    }
    else
    {
      return 1;
    }
  }

  /**
   * Saves the simulate options.
   */
  public boolean save(String direct)
  {
    initialTime = 0.0;
    outputStartTime = 0.0;
    timeLimit = 100.0;
    printInterval = 1.0;
    minTimeStep = 0.0;
    timeStep = 1.0;
    absError = 1.0e-9;
    relError = 0.0;
    rndSeed = 314159;
    run = 1;
    intSpecies = getInterestingSpecies();
    String selectedButtons = "";
    rap1 = 0.1;
    rap2 = 0.1;
    qss = 0.1;
    con = 15;
    double stoichAmp = 1.0;
    printer_track_quantity = "amount";
    String generate_statistics = "false";
    sim = (String) simulators.getSelectedItem();
    simProp = sbmlProp;

    try
    {
      initialTime = Double.parseDouble(initialTimeField.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Initial Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (initialTime < 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Initial Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try
    {
      outputStartTime = Double.parseDouble(outputStartTimeField.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Output Start Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (outputStartTime < 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Output Start Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try
    {
      timeLimit = Double.parseDouble(limit.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Time Limit Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (timeLimit <= 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Time Limit Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }		

    try
    {
      if (((String) intervalLabel.getSelectedItem()).contains("Print Interval"))
      {
        printInterval = Double.parseDouble(interval.getText().trim());
        if (printInterval < 0)
        {
          JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Print Interval Field.", "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
        else if (printInterval == 0 && !((String) intervalLabel.getSelectedItem()).contains("Minimum"))
        {
          JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Print Interval Field.", "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
      else
      {
        printInterval = Integer.parseInt(interval.getText().trim());
        if (printInterval <= 0)
        {
          JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Number of Steps Field.", "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
    }
    catch (Exception e1)
    {
      if (((String) intervalLabel.getSelectedItem()).contains("Print Interval"))
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Real Number into the Print Interval Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter an Integer into the Number of Steps Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try
    {
      minTimeStep = Double.parseDouble(minStep.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Real Number into the Minimum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (minTimeStep < 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Minimum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if (step.getText().trim().equals("inf") && !sim.equals("euler"))
    {
      timeStep = Double.MAX_VALUE;
    }
    else if (step.getText().trim().equals("inf") && sim.equals("euler"))
    {
      JOptionPane.showMessageDialog(Gui.frame, "Cannot Select an Infinite Maximum Time Step with Euler Simulation.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    else
    {
      try
      {
        timeStep = Double.parseDouble(step.getText().trim());
      }
      catch (Exception e1)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Real Number into the Maximum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    if (timeStep <= 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Maximum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }	

    try
    {
      absError = Double.parseDouble(absErr.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Absolute Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (absError < 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Absolute Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try
    {
      relError = Double.parseDouble(relErr.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Relative Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (relError < 0)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Relative Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }


    try
    {
      rndSeed = Long.parseLong(seed.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter an Integer into the Random Seed Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try
    {
      run = Integer.parseInt(runs.getText().trim());
      if (run < 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Integer into the Runs Field." + "\nProceding with Default:   " + biosimrc.get("biosim.sim.runs", ""), "Error", JOptionPane.ERROR_MESSAGE);
        run = Integer.parseInt(biosimrc.get("biosim.sim.runs", ""));
        return false;
      }
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Integer into the Runs Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if (genRuns.isSelected())
    {
      printer_id = "null.printer";
    }
    else
    {
      printer_id = "tsd.printer";
    }
    if (concentrations.isSelected())
    {
      printer_track_quantity = "concentration";
    }
    if (genStats.isSelected())
    {
      generate_statistics = "true";
    }

    try
    {
      rap1 = Double.parseDouble(rapid1.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The" + " Rapid Equilibrium Condition 1 Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try
    {
      rap2 = Double.parseDouble(rapid2.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The" + " Rapid Equilibrium Condition 2 Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try
    {
      qss = Double.parseDouble(qssa.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The QSSA Condition Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try
    {
      con = Integer.parseInt(maxCon.getText().trim());
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter An Integer Into The Max" + " Concentration Threshold Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try
    {
      if (reactionAbstraction.isSelected())
      {
        stoichAmp = Double.parseDouble(diffStoichAmp.getText().trim());
      }
      else
      {
        stoichAmp = 1;
      }
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Double Into the Stoich." + " Amp. Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (noAbstraction.isSelected() && ODE.isSelected())
    {
      selectedButtons = "none_ODE";
    }
    else if (noAbstraction.isSelected() && monteCarlo.isSelected())
    {
      selectedButtons = "none_monteCarlo";
    }
    else if (expandReactions.isSelected() && ODE.isSelected())
    {
      selectedButtons = "expand_ODE";
    }
    else if (expandReactions.isSelected() && monteCarlo.isSelected())
    {
      selectedButtons = "expand_monteCarlo";
    }
    else if (reactionAbstraction.isSelected() && ODE.isSelected())
    {
      selectedButtons = "abs_ODE";
    }
    else if (reactionAbstraction.isSelected() && monteCarlo.isSelected())
    {
      selectedButtons = "abs_monteCarlo";
    }
    else if (stateAbstraction.isSelected() && monteCarlo.isSelected())
    {
      selectedButtons = "nary_monteCarlo";
    }
    else if (stateAbstraction.isSelected() && markov.isSelected())
    {
      selectedButtons = "nary_markov";
    }
    else if (reactionAbstraction.isSelected() && markov.isSelected())
    {
      selectedButtons = "abs_markov";
    }
    else if (reactionAbstraction.isSelected() && sbml.isSelected())
    {
      selectedButtons = "abs_sbml";
    }
    else if (stateAbstraction.isSelected() && sbml.isSelected())
    {
      selectedButtons = "nary_sbml";
    }
    else if (reactionAbstraction.isSelected() && dot.isSelected())
    {
      selectedButtons = "abs_dot";
    }
    else if (stateAbstraction.isSelected() && dot.isSelected())
    {
      selectedButtons = "nary_dot";
    }
    else if (noAbstraction.isSelected() && fba.isSelected())
    {
      selectedButtons = "none_fba";
    }
    else if (noAbstraction.isSelected() && markov.isSelected())
    {
      selectedButtons = "none_markov";
    }
    else if (noAbstraction.isSelected() && sbml.isSelected())
    {
      selectedButtons = "none_sbml";
    }
    else if (noAbstraction.isSelected() && dot.isSelected())
    {
      selectedButtons = "none_dot";
    }
    else if (noAbstraction.isSelected() && xhtml.isSelected())
    {
      selectedButtons = "none_xhtml";
    }
    else if (expandReactions.isSelected() && fba.isSelected())
    {
      selectedButtons = "expand_fba";
    }
    else if (expandReactions.isSelected() && markov.isSelected())
    {
      selectedButtons = "expand_markov";
    }
    else if (expandReactions.isSelected() && sbml.isSelected())
    {
      selectedButtons = "expand_sbml";
    }
    else if (expandReactions.isSelected() && dot.isSelected())
    {
      selectedButtons = "expand_dot";
    }
    else if (expandReactions.isSelected() && xhtml.isSelected())
    {
      selectedButtons = "expand_xhtml";
    }
    else if (reactionAbstraction.isSelected() && xhtml.isSelected())
    {
      selectedButtons = "abs_xhtml";
    }
    else if (stateAbstraction.isSelected() && xhtml.isSelected())
    {
      selectedButtons = "nary_xhtml";
    }
    // Run runProgram = new Run(this);
    int cut = 0;
    sbmlProp = sbmlProp.replace("\\", "/");
    String[] getFilename = sbmlProp.split("/");
    for (int i = 0; i < getFilename[getFilename.length - 1].length(); i++)
    {
      if (getFilename[getFilename.length - 1].charAt(i) == '.')
      {
        cut = i;
      }
    }
    boolean saveTopLevel = false;
    if (!direct.equals("."))
    {
      simProp = simProp.substring(0, simProp.length() - simProp.split("/")[simProp.split("/").length - 1].length()) + direct + GlobalConstants.separator + simProp.substring(simProp.length() - simProp.split("/")[simProp.split("/").length - 1].length());
      saveTopLevel = true;
    }
    String propName = simProp.substring(0, simProp.length() - getFilename[getFilename.length - 1].length()) + getFilename[getFilename.length - 1].substring(0, cut) + ".properties";
    log.addText("Creating properties file:\n" + propName + "\n");
    int numPaths = Integer.parseInt((String) (bifurcation.getSelectedItem()));
    createProperties(initialTime, outputStartTime, timeLimit, ((String) (intervalLabel.getSelectedItem())), printInterval, minTimeStep, timeStep, absError, relError, ".", rndSeed,
      run, numPaths, intSpecies, printer_id, printer_track_quantity, generate_statistics, sbmlProp.split("/"), selectedButtons, this,
      sbmlProp, rap1, rap2, qss, con, stoichAmp, preAbs, loopAbs, postAbs, lpnAbstraction, mpde.isSelected(), meanPath.isSelected(),
      adaptive.isSelected());
    if (direct.equals("."))
    {
      outDir = simName;
    }
    else
    {
      outDir = simName + GlobalConstants.separator + direct;
    }
    if (!runs.isEnabled())
    {
      for (String runs : new File(root + GlobalConstants.separator + outDir).list())
      {
        if (runs.length() >= 4)
        {
          String end = "";
          for (int j = 1; j < 5; j++)
          {
            end = runs.charAt(runs.length() - j) + end;
          }
          if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv"))
          {
            if (runs.contains("run-"))
            {
              run = Math.max(run, Integer.parseInt(runs.substring(4, runs.length() - end.length())));
            }
          }
        }
      }
    }
    String topLevelProps = sbmlProp.substring(0, sbmlProp.length() - getFilename[getFilename.length - 1].length()) + getFilename[getFilename.length - 1].substring(0, cut) + ".properties";
    try
    {
      Properties getProps = new Properties();
      FileInputStream load = new FileInputStream(new File(topLevelProps));
      getProps.load(load);
      load.close();
      getProps.setProperty("selected.simulator", sim);
      if (transientProperties != null)
      {
        getProps.setProperty("selected.property", (String) transientProperties.getSelectedItem());
      }
      if (!fileStem.getText().trim().equals(""))
      {
        new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + fileStem.getText().trim()).mkdir();
        getProps.setProperty("file.stem", fileStem.getText().trim());
      }
      if (monteCarlo.isSelected() || ODE.isSelected())
      {
        if (append.isSelected())
        {
          getProps.setProperty("monte.carlo.simulation.start.index", 
            getStartIndex(outDir) + "");
        }
        else
        {
          getProps.setProperty("monte.carlo.simulation.start.index", "1");
        }
      }
      FileOutputStream store = new FileOutputStream(new File(propName));
      getProps.store(store, getFilename[getFilename.length - 1].substring(0, cut) + " Properties");
      store.close();
      if (saveTopLevel)
      {
        store = new FileOutputStream(new File(topLevelProps));
        getProps.store(store, getFilename[getFilename.length - 1].substring(0, cut) + " Properties");
        store.close();
      }
      saveSEDML();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(Gui.frame, "Unable to add properties to property file.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    change = false;
    return true;
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
    double rap1, double rap2, double qss, int con, double stoichAmp, JList preAbs, JList loopAbs, JList postAbs, AbstractionPanel abstPane,
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
      for (Integer i = 0; i < abstPane.getAbstractionProperty().preAbsModel.size(); i++)
      {
        abs.setProperty("abstraction.transform." + abstPane.getAbstractionProperty().preAbsModel.getElementAt(i).toString(), "preloop" + i.toString());
      }
      for (Integer i = 0; i < abstPane.getAbstractionProperty().loopAbsModel.size(); i++)
      {
        if (abstPane.getAbstractionProperty().preAbsModel.contains(abstPane.getAbstractionProperty().loopAbsModel.getElementAt(i)))
        {
          String value = abs.getProperty("abstraction.transform." + abstPane.getAbstractionProperty().loopAbsModel.getElementAt(i).toString());
          value = value + "mainloop" + i.toString();
          abs.setProperty("abstraction.transform." + abstPane.getAbstractionProperty().loopAbsModel.getElementAt(i).toString(), value);
        }
        else
        {
          abs.setProperty("abstraction.transform." + abstPane.getAbstractionProperty().loopAbsModel.getElementAt(i).toString(), "mainloop" + i.toString());
        }
      }
      for (Integer i = 0; i < abstPane.getAbstractionProperty().postAbsModel.size(); i++)
      {
        if (abstPane.getAbstractionProperty().preAbsModel.contains(abstPane.getAbstractionProperty().postAbsModel.getElementAt(i)) || abstPane.getAbstractionProperty().preAbsModel.contains(abstPane.getAbstractionProperty().postAbsModel.get(i)))
        {
          String value = abs.getProperty("abstraction.transform." + abstPane.getAbstractionProperty().postAbsModel.getElementAt(i).toString());
          value = value + "postloop" + i.toString();
          abs.setProperty("abstraction.transform." + abstPane.getAbstractionProperty().postAbsModel.getElementAt(i).toString(), value);
        }
        else
        {
          abs.setProperty("abstraction.transform." + abstPane.getAbstractionProperty().postAbsModel.getElementAt(i).toString(), "postloop" + i.toString());
        }
      }
      for (String s : abstPane.getAbstractionProperty().transforms)
      {
        if (!abstPane.getAbstractionProperty().preAbsModel.contains(s) && !abstPane.getAbstractionProperty().loopAbsModel.contains(s) && !abstPane.getAbstractionProperty().postAbsModel.contains(s))
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

  private void saveSEDML() {
    double initialTime = Double.parseDouble(initialTimeField.getText().trim());
    double outputStartTime = Double.parseDouble(outputStartTimeField.getText().trim());
    double timeLimit = Double.parseDouble(limit.getText().trim());
    double printInterval = Double.parseDouble(interval.getText().trim());
    int numberOfSteps;
    if (((String) intervalLabel.getSelectedItem()).equals("Number Of Steps"))
    {
      numberOfSteps = Integer.parseInt(interval.getText().trim());
    }
    else
    {
      numberOfSteps = (int) Math.floor(timeLimit / printInterval) + 1;
    }
    SedML sedml = sedmlDoc.getSedMLModel();
    //List<Simulation> simulations = sedml.getSimulations();
    String taskId = simName;
    if (!fileStem.getText().trim().equals("")) {
      taskId = simName + "__" + fileStem.getText().trim();
    }
    AbstractTask task = sedml.getTaskWithId(taskId);
    Simulation simulation = sedml.getSimulation(taskId+"_sim");
    if (simulation != null) {
      sedml.removeSimulation(simulation);
    }
    //		Model model = sedml.getModelWithId(simName+"_model");
    //		if (model!=null) {
    //			sedml.removeModel(model);
    //		}
    if (task != null) {
      sedml.removeTask(task);
    }
    Algorithm algo = getAlgorithm();
    if (algo.getKisaoID().equals(GlobalConstants.KISAO_FBA)) {
      simulation = new SteadyState(taskId+"_sim", "", algo);
    } else if (algo.getKisaoID().equals(GlobalConstants.KISAO_GENERIC)) {
      simulation = new SteadyState(taskId+"_sim", "", algo);
      // TODO: need to deal with transient Markov chain method which is a type of UniformTimeCourse
    } else {
      simulation = new UniformTimeCourse(taskId+"_sim", "", initialTime, outputStartTime, timeLimit, numberOfSteps, algo);
      if (intervalLabel.getSelectedItem().equals("Print Interval")) {
        Element para = new Element("printInterval");
        para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
        para.setAttribute("Print_Interval", interval.getText());
        Annotation ann = new Annotation(para);
        simulation.addAnnotation(ann);
      } else if (intervalLabel.getSelectedItem().equals("Minimum Print Interval")) {
        Element para = new Element("printInterval");
        para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
        para.setAttribute("Minimum_Print_Interval", interval.getText());
        Annotation ann = new Annotation(para);
        simulation.addAnnotation(ann);
      }
    }
    sedml.addSimulation(simulation);
    //model = new Model(taskId+"_model", "", SUPPORTED_LANGUAGE.SBML_GENERIC.getURN(), modelFile);
    //sedml.addModel(model);
    task = new Task(taskId, "", taskId+"_model", simulation.getId());
    sedml.addTask(task);
    DataGenerator dataGen = sedml.getDataGeneratorWithId("time_"+taskId+"_dg");
    if (dataGen != null) {
      sedml.removeDataGenerator(dataGen);
    }
    Variable variable = new Variable("time_"+taskId,"",taskId,VariableSymbol.TIME);
    org.jmathml.ASTNode math = org.jlibsedml.Libsedml.parseFormulaString("time_"+taskId);
    dataGen = new DataGenerator("time_"+taskId+"_dg","time",math);
    dataGen.addVariable(variable);
    sedml.addDataGenerator(dataGen);
    for (int i = 0; i < simTab.getComponentCount(); i++)
    {
      if (simTab.getComponentAt(i) instanceof Graph)
      {
        ((Graph) simTab.getComponentAt(i)).saveSEDML(sedmlDoc,simName,null);
      }
    }
    gui.writeSEDMLDocument();
  }

  private void loadSEDML(String subTask)
  {
    sedmlDoc = gui.getSEDMLDocument();
    SedML sedml = sedmlDoc.getSedMLModel();
    subTaskList.removeAllItems();
    subTaskList.addItem("(none)");
    for (AbstractTask task : sedml.getTasks())
    {
      if (task.getId().startsWith(simName+"__")) {
        subTaskList.addItem(task.getId().replace(simName+"__",""));
      }
    }
    String taskId = simName;
    if (subTask!=null && !subTask.equals("")) {
      taskId = taskId + "__" + subTask;
      subTaskList.setSelectedItem(subTask);
    }
    AbstractTask task = sedml.getTaskWithId(taskId);
    if (task != null) {
      Simulation simulation = sedml.getSimulation(task.getSimulationReference());
      setAlgorithm(simulation.getAlgorithm());
      fileStem.setText(subTask);
      if (ODE.isSelected()||monteCarlo.isSelected())
      {
        // TODO: what java ODE simulator fails on 987
        if (simulation instanceof UniformTimeCourse) {
          UniformTimeCourse utcSimulation = (UniformTimeCourse) simulation;
          String printInterval = SEDMLutilities.getSEDBaseAnnotation(simulation, "printInterval", "Print_Interval", null);
          if (printInterval != null)
          {
            intervalLabel.setSelectedItem("Print Interval");
            interval.setText(printInterval);
          } else {
            printInterval = SEDMLutilities.getSEDBaseAnnotation(simulation, "printInterval", "Minimum_Print_Interval", null);
            if (printInterval != null) {
              intervalLabel.setSelectedItem("Minimum Print Interval");
              interval.setText(printInterval);
            } else {
              intervalLabel.setSelectedItem("Number Of Steps");
              interval.setText("" + utcSimulation.getNumberOfPoints());
            }
          }
          initialTimeField.setText("" + utcSimulation.getInitialTime());
          outputStartTimeField.setText("" + utcSimulation.getOutputStartTime());
          limit.setText("" + utcSimulation.getOutputEndTime());
        } else if (simulation instanceof OneStep) {
          OneStep osSimulation = (OneStep) simulation;
          intervalLabel.setSelectedItem("Number Of Steps");
          interval.setText("" + 1);
          initialTimeField.setText("" + 0);
          outputStartTimeField.setText("" + 0);
          limit.setText("" + osSimulation.getStep());
        }
      }
      else if (fba.isSelected())
      {
        absErr.setText("1e-4");
      }
    }
  }

  private Algorithm getAlgorithm()
  {
    Algorithm algorithm = null;
    Element para = new Element("analysis");
    para.setNamespace(Namespace.getNamespace("http://www.async.ece.utah.edu/iBioSim"));
    if (ODE.isSelected())
    {
      if (((String) simulators.getSelectedItem()).contains("euler"))
      {
        algorithm = new Algorithm(GlobalConstants.KISAO_EULER);
      }
      else if (((String) simulators.getSelectedItem()).contains("rk8pd"))
      {
        algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_PRINCE_DORMAND);
      }
      else if (((String) simulators.getSelectedItem()).contains("rkf45") || ((String) simulators.getSelectedItem()).contains("Runge-Kutta-Fehlberg"))
      {
        algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG);
        para.setAttribute("method", ((String) simulators.getSelectedItem()));
      } 
      else {
        algorithm = new Algorithm(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG);
        para.setAttribute("method", ((String) simulators.getSelectedItem()));
      }
    }
    else if (monteCarlo.isSelected())
    {
      if (((String) simulators.getSelectedItem()).equals("gillespie"))
      {
        algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
      }
      if (((String) simulators.getSelectedItem()).contains("Hierarchical"))
      {
        algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
      }
      else if (((String) simulators.getSelectedItem()).contains("SSA-CR"))
      {
        algorithm = new Algorithm(GlobalConstants.KISAO_SSA_CR);
      }
      else {
        algorithm = new Algorithm(GlobalConstants.KISAO_GILLESPIE_DIRECT);
        para.setAttribute("method", ((String) simulators.getSelectedItem()));
      }
    } 
    else if (fba.isSelected()) 
    {
      algorithm = new Algorithm(GlobalConstants.KISAO_FBA);
    }
    else
    {
      algorithm = new Algorithm(GlobalConstants.KISAO_GENERIC);
      if (sbml.isSelected()) {
        para.setAttribute("method", "Model");
      } else if (dot.isSelected()) {
        para.setAttribute("method", "Network");
      } else if (xhtml.isSelected()) {
        para.setAttribute("method", "Browser");
      } else {
        para.setAttribute("method", ((String) simulators.getSelectedItem()));
      }
    }
    if (expandReactions.isSelected()) {
      para.setAttribute("abstraction", "Expand Reactions");
    } else if (reactionAbstraction.isSelected()) {
      para.setAttribute("abstraction", "Reaction-based");
    } else if (stateAbstraction.isSelected()) {
      para.setAttribute("abstraction", "State-based");
    }
    Annotation ann = new Annotation(para);
    algorithm.addAnnotation(ann);
    AlgorithmParameter ap = new AlgorithmParameter(GlobalConstants.KISAO_MINIMUM_STEP_SIZE,minStep.getText());
    algorithm.addAlgorithmParameter(ap);
    ap = new AlgorithmParameter(GlobalConstants.KISAO_MAXIMUM_STEP_SIZE, step.getText());
    algorithm.addAlgorithmParameter(ap);
    ap = new AlgorithmParameter(GlobalConstants.KISAO_ABSOLUTE_TOLERANCE, absErr.getText());
    algorithm.addAlgorithmParameter(ap);
    ap = new AlgorithmParameter(GlobalConstants.KISAO_RELATIVE_TOLERANCE,relErr.getText());
    algorithm.addAlgorithmParameter(ap);
    ap = new AlgorithmParameter(GlobalConstants.KISAO_SEED,seed.getText());
    algorithm.addAlgorithmParameter(ap);
    ap = new AlgorithmParameter(GlobalConstants.KISAO_SAMPLES,runs.getText());
    algorithm.addAlgorithmParameter(ap);
    return algorithm;
  }

  private void setAlgorithm(Algorithm algorithm)
  {
    String kisaoId = algorithm.getKisaoID();
    KisaoTerm kt = KisaoOntology.getInstance().getTermById(kisaoId);
    String method = SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "method", null);
    if (kisaoId.equals(GlobalConstants.KISAO_EULER)) {
      ODE.setSelected(true);
      enableODE();
      simulators.setSelectedItem("euler");
    }
    else if (kisaoId.equals(GlobalConstants.KISAO_RUNGE_KUTTA_FEHLBERG))
    {
      ODE.setSelected(true); 
      enableODE();
      simulators.setSelectedItem((String)"rkf45");
      if (method!=null) {
        simulators.setSelectedItem(method);
      }
    } else if (kisaoId.equals(GlobalConstants.KISAO_RUNGE_KUTTA_PRINCE_DORMAND)) {
      ODE.setSelected(true);
      enableODE();
      simulators.setSelectedItem((String)"rk8pd");
    } else if (kisaoId.equals(GlobalConstants.KISAO_GILLESPIE) ||
        kisaoId.equals(GlobalConstants.KISAO_GILLESPIE_DIRECT)) {
      monteCarlo.setSelected(true);
      enableMonteCarlo();
      simulators.setSelectedItem((String)"gillespie");
      if (method!=null) {
        simulators.setSelectedItem(method);
      }
    } else if (kisaoId.equals(GlobalConstants.KISAO_SSA_CR)) {
      monteCarlo.setSelected(true);
      enableMonteCarlo();
      simulators.setSelectedItem("SSA-CR (Dynamic)");
    }
    else if (kisaoId.equals(GlobalConstants.KISAO_FBA))
    {
      fba.setSelected(true);
      enableFBA();
    } else if (kisaoId.equals(GlobalConstants.KISAO_GENERIC)) {
      if (method!=null) {
        if (method.equals("Model")) {
          sbml.setSelected(true);
        } else if (method.equals("Network")) {
          dot.setSelected(true);
        } else if (method.equals("Browser")) {
          xhtml.setSelected(true);
        } else {
          markov.setSelected(true);
          if (method!=null) {
            simulators.setSelectedItem(method);
          }
        }
      }
    } else if (kt==null || kt.is_a(KisaoOntology.ALGORITHM_WITH_DETERMINISTIC_RULES)) {
      ODE.setSelected(true);
      enableODE();
      simulators.setSelectedItem("rkf45");
    }
    else
    {
      monteCarlo.setSelected(true);
      enableMonteCarlo();
      simulators.setSelectedItem("gillespie");
    }
    String abstraction = SEDMLutilities.getSEDBaseAnnotation(algorithm, "analysis", "abstraction", null);
    if (abstraction!=null) {
      if (abstraction.equals("Expand Reactions")) {
        expandReactions.setSelected(true);
      } else if (abstraction.equals("Reaction-based")) {
        reactionAbstraction.setSelected(true);
      } else if (abstraction.equals("State-based")) {
        stateAbstraction.setSelected(true);
      } else {
        noAbstraction.setSelected(true);
      }
    } else {
      noAbstraction.setSelected(true);
    }
    for (AlgorithmParameter ap : algorithm.getListOfAlgorithmParameters()) {
      if (ap.getKisaoID().equals(GlobalConstants.KISAO_MINIMUM_STEP_SIZE)) {
        minStep.setText(ap.getValue());
      }
      else if (ap.getKisaoID().equals(GlobalConstants.KISAO_MAXIMUM_STEP_SIZE))
      {
        step.setText(ap.getValue());
      }
      else if (ap.getKisaoID().equals(GlobalConstants.KISAO_ABSOLUTE_TOLERANCE))
      {
        absErr.setText(ap.getValue());
      } else if (ap.getKisaoID().equals(GlobalConstants.KISAO_RELATIVE_TOLERANCE)) {
        relErr.setText(ap.getValue());
      } else if (ap.getKisaoID().equals(GlobalConstants.KISAO_SEED)) {
        seed.setText(ap.getValue());
      } else if (ap.getKisaoID().equals(GlobalConstants.KISAO_SAMPLES)) {
        runs.setText(ap.getValue());
      }
    }
  }

  /**
   * Loads the simulate options.
   */
  private void loadPropertiesFile(String simName, String modelName)
  {
    String openFile = root + GlobalConstants.separator + simName + GlobalConstants.separator + simName + ".properties";
    if (!(new File(openFile)).exists())
    {
      openFile = root + GlobalConstants.separator + simName + GlobalConstants.separator + modelName + ".properties";
      if (!(new File(openFile)).exists())
      {
        return;
      }
    }
    Properties load = new Properties();
    try
    {
      if (!openFile.equals(""))
      {
        FileInputStream in = new FileInputStream(new File(openFile));
        load.load(in);
        in.close();
        ArrayList<String> loadProperties = new ArrayList<String>();
        for (Object key : load.keySet())
        {
          String type = key.toString().substring(0, key.toString().indexOf('.'));
          if (type.equals("gcm"))
          {
            loadProperties.add(key.toString() + "=" + load.getProperty(key.toString()));
          }
          else if (key.equals("reb2sac.abstraction.method.0.1"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.0.1").equals("enzyme-kinetic-qssa-1"))
            {
              loadProperties.add("reb2sac.abstraction.method.0.1=" + load.getProperty("reb2sac.abstraction.method.0.1"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.0.2"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.0.2").equals("reversible-to-irreversible-transformer"))
            {
              loadProperties.add("reb2sac.abstraction.method.0.2=" + load.getProperty("reb2sac.abstraction.method.0.2"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.0.3"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.0.3").equals("multiple-products-reaction-eliminator"))
            {
              loadProperties.add("reb2sac.abstraction.method.0.3=" + load.getProperty("reb2sac.abstraction.method.0.3"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.0.4"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.0.4").equals("multiple-reactants-reaction-eliminator"))
            {
              loadProperties.add("reb2sac.abstraction.method.0.4=" + load.getProperty("reb2sac.abstraction.method.0.4"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.0.5"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.0.5").equals("single-reactant-product-reaction-eliminator"))
            {
              loadProperties.add("reb2sac.abstraction.method.0.5=" + load.getProperty("reb2sac.abstraction.method.0.5"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.0.6"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.0.6").equals("dimer-to-monomer-substitutor"))
            {
              loadProperties.add("reb2sac.abstraction.method.0.6=" + load.getProperty("reb2sac.abstraction.method.0.6"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.0.7"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.0.7").equals("inducer-structure-transformer"))
            {
              loadProperties.add("reb2sac.abstraction.method.0.7=" + load.getProperty("reb2sac.abstraction.method.0.7"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.1.1"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.1.1").equals("modifier-structure-transformer"))
            {
              loadProperties.add("reb2sac.abstraction.method.1.1=" + load.getProperty("reb2sac.abstraction.method.1.1"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.1.2"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.1.2").equals("modifier-constant-propagation"))
            {
              loadProperties.add("reb2sac.abstraction.method.1.2=" + load.getProperty("reb2sac.abstraction.method.1.2"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.1"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.1").equals("operator-site-forward-binding-remover"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.1=" + load.getProperty("reb2sac.abstraction.method.2.1"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.3"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.3").equals("enzyme-kinetic-rapid-equilibrium-1"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.3=" + load.getProperty("reb2sac.abstraction.method.2.3"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.4"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.4").equals("irrelevant-species-remover"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.4=" + load.getProperty("reb2sac.abstraction.method.2.4"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.5"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.5").equals("inducer-structure-transformer"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.5=" + load.getProperty("reb2sac.abstraction.method.2.5"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.6"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.6").equals("modifier-constant-propagation"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.6=" + load.getProperty("reb2sac.abstraction.method.2.6"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.7"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.7").equals("similar-reaction-combiner"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.7=" + load.getProperty("reb2sac.abstraction.method.2.7"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.8"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.8").equals("modifier-constant-propagation"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.8=" + load.getProperty("reb2sac.abstraction.method.2.8"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.2.2"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.2.2").equals("dimerization-reduction") && !load.getProperty("reb2sac.abstraction.method.2.2").equals("dimerization-reduction-level-assignment"))
            {
              loadProperties.add("reb2sac.abstraction.method.2.2=" + load.getProperty("reb2sac.abstraction.method.2.2"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.3.1"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.3.1").equals("kinetic-law-constants-simplifier") && !load.getProperty("reb2sac.abstraction.method.3.1").equals("reversible-to-irreversible-transformer")
                && !load.getProperty("reb2sac.abstraction.method.3.1").equals("nary-order-unary-transformer"))
            {
              loadProperties.add("reb2sac.abstraction.method.3.1=" + load.getProperty("reb2sac.abstraction.method.3.1"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.3.2"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.3.2").equals("kinetic-law-constants-simplifier") && !load.getProperty("reb2sac.abstraction.method.3.2").equals("modifier-constant-propagation"))
            {
              loadProperties.add("reb2sac.abstraction.method.3.2=" + load.getProperty("reb2sac.abstraction.method.3.2"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.3.3"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.3.3").equals("absolute-inhibition-generator"))
            {
              loadProperties.add("reb2sac.abstraction.method.3.3=" + load.getProperty("reb2sac.abstraction.method.3.3"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.3.4"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.3.4").equals("final-state-generator"))
            {
              loadProperties.add("reb2sac.abstraction.method.3.4=" + load.getProperty("reb2sac.abstraction.method.3.4"));
            }
          }
          else if (key.equals("reb2sac.abstraction.method.3.5"))
          {
            if (!load.getProperty("reb2sac.abstraction.method.3.5").equals("stop-flag-generator"))
            {
              loadProperties.add("reb2sac.abstraction.method.3.5=" + load.getProperty("reb2sac.abstraction.method.3.5"));
            }
          }
          else if (key.equals("reb2sac.nary.order.decider"))
          {
            if (!load.getProperty("reb2sac.nary.order.decider").equals("distinct"))
            {
              loadProperties.add("reb2sac.nary.order.decider=" + load.getProperty("reb2sac.nary.order.decider"));
            }
          }
          else if (key.equals("simulation.printer"))
          {
            if (!load.getProperty("simulation.printer").equals("tsd.printer"))
            {
              loadProperties.add("simulation.printer=" + load.getProperty("simulation.printer"));
            }
          }
          else if (key.equals("simulation.printer.tracking.quantity"))
          {
            if (!load.getProperty("simulation.printer.tracking.quantity").equals("amount"))
            {
              loadProperties.add("simulation.printer.tracking.quantity=" + load.getProperty("simulation.printer.tracking.quantity"));
            }
          }
          else if (((String) key).length() > 27 && ((String) key).substring(0, 28).equals("reb2sac.interesting.species."))
          {
          }
          else if (key.equals("reb2sac.rapid.equilibrium.condition.1"))
          {
          }
          else if (key.equals("reb2sac.rapid.equilibrium.condition.2"))
          {
          }
          else if (key.equals("reb2sac.qssa.condition.1"))
          {
          }
          else if (key.equals("reb2sac.operator.max.concentration.threshold"))
          {
          }
          else if (key.equals("reb2sac.diffusion.stoichiometry.amplification.value"))
          {
          }
          else if (key.equals("reb2sac.iSSA.number.paths"))
          {
          }
          else if (key.equals("reb2sac.iSSA.type"))
          {
          }
          else if (key.equals("reb2sac.iSSA.adaptive"))
          {
          }
          else if (key.equals("ode.simulation.time.limit"))
          {
          }
          else if (key.equals("simulation.initial.time"))
          {
          }
          else if (key.equals("simulation.output.start.time"))
          {
          }
          else if (key.equals("ode.simulation.print.interval"))
          {
          }
          else if (key.equals("ode.simulation.number.steps"))
          {
          }
          else if (key.equals("ode.simulation.min.time.step"))
          {
          }
          else if (key.equals("ode.simulation.time.step"))
          {
          }
          else if (key.equals("ode.simulation.absolute.error"))
          {
          }
          else if (key.equals("ode.simulation.out.dir"))
          {
          }
          else if (key.equals("monte.carlo.simulation.time.limit"))
          {
          }
          else if (key.equals("monte.carlo.simulation.print.interval"))
          {
          }
          else if (key.equals("monte.carlo.simulation.number.steps"))
          {
          }
          else if (key.equals("monte.carlo.simulation.min.time.step"))
          {
          }
          else if (key.equals("monte.carlo.simulation.time.step"))
          {
          }
          else if (key.equals("monte.carlo.simulation.random.seed"))
          {
          }
          else if (key.equals("monte.carlo.simulation.runs"))
          {
          }
          else if (key.equals("monte.carlo.simulation.out.dir"))
          {
          }
          else if (key.equals("simulation.run.termination.decider"))
          {
          }
          else if (key.equals("computation.analysis.sad.path"))
          {
          }
          else if (key.equals("simulation.time.series.species.level.file"))
          {
          }
          else if (key.equals("reb2sac.simulation.method"))
          {
          }
          else if (key.equals("reb2sac.abstraction.method"))
          {
          }
          else if (key.equals("selected.simulator"))
          {
          }
          else if (key.equals("file.stem"))
          {
          }
          else if (((String) key).length() > 36 && ((String) key).substring(0, 37).equals("simulation.run.termination.condition."))
          {
          }
          else if (((String) key).length() > 37 && ((String) key).substring(0, 38).equals("reb2sac.absolute.inhibition.threshold."))
          {
          }
          else if (((String) key).length() > 27 && ((String) key).substring(0, 28).equals("reb2sac.concentration.level."))
          {
          }
          else if (((String) key).length() > 19 && ((String) key).substring(0, 20).equals("reb2sac.final.state."))
          {
          }
          else if (key.equals("reb2sac.analysis.stop.enabled"))
          {
          }
          else if (key.equals("reb2sac.analysis.stop.rate"))
          {
          }
          else if (key.equals("monte.carlo.simulation.start.index"))
          {
          }
          else if (key.equals("abstraction.interesting") && lpnAbstraction != null)
          {
            String intVars = load.getProperty("abstraction.interesting");
            String[] array = intVars.split(" ");
            for (String s : array)
            {
              if (!s.equals(""))
              {
                lpnAbstraction.addIntVar(s);
              }
            }
          }
          else if (key.equals("abstraction.factor") && lpnAbstraction != null)
          {
            lpnAbstraction.factorField.setText(load.getProperty("abstraction.factor"));
          }
          else if (key.equals("abstraction.iterations") && lpnAbstraction != null)
          {
            lpnAbstraction.iterField.setText(load.getProperty("abstraction.iterations"));
          }
          else if (key.toString().startsWith("abstraction.transform"))
          {
            continue;
          }
          else
          {
            loadProperties.add(key + "=" + load.getProperty((String) key));
          }
        }
        HashMap<Integer, String> preOrder = new HashMap<Integer, String>();
        HashMap<Integer, String> loopOrder = new HashMap<Integer, String>();
        HashMap<Integer, String> postOrder = new HashMap<Integer, String>();
        HashMap<String, Boolean> containsXform = new HashMap<String, Boolean>();
        boolean containsAbstractions = false;
        if (lpnAbstraction != null)
        {
          for (String s : lpnAbstraction.getAbstractionProperty().transforms)
          {
            if (load.containsKey("abstraction.transform." + s))
            {
              if (load.getProperty("abstraction.transform." + s).contains("preloop"))
              {
                Pattern prePattern = Pattern.compile("preloop(\\d+)");
                Matcher intMatch = prePattern.matcher(load.getProperty("abstraction.transform." + s));
                if (intMatch.find())
                {
                  Integer index = Integer.parseInt(intMatch.group(1));
                  preOrder.put(index, s);
                }
                else
                {
                  lpnAbstraction.addPreXform(s);
                }
              }
              else
              {
                lpnAbstraction.getAbstractionProperty().preAbsModel.removeElement(s);
              }
              if (load.getProperty("abstraction.transform." + s).contains("mainloop"))
              {
                Pattern loopPattern = Pattern.compile("mainloop(\\d+)");
                Matcher intMatch = loopPattern.matcher(load.getProperty("abstraction.transform." + s));
                if (intMatch.find())
                {
                  Integer index = Integer.parseInt(intMatch.group(1));
                  loopOrder.put(index, s);
                }
                else
                {
                  lpnAbstraction.addLoopXform(s);
                }
              }
              else
              {
                lpnAbstraction.getAbstractionProperty().loopAbsModel.removeElement(s);
              }
              if (load.getProperty("abstraction.transform." + s).contains("postloop"))
              {
                Pattern postPattern = Pattern.compile("postloop(\\d+)");
                Matcher intMatch = postPattern.matcher(load.getProperty("abstraction.transform." + s));
                if (intMatch.find())
                {
                  Integer index = Integer.parseInt(intMatch.group(1));
                  postOrder.put(index, s);
                }
                else
                {
                  lpnAbstraction.addPostXform(s);
                }
              }
              else
              {
                lpnAbstraction.getAbstractionProperty().postAbsModel.removeElement(s);
              }
            }
            else if (containsAbstractions && !containsXform.get(s))
            {
              lpnAbstraction.getAbstractionProperty().preAbsModel.removeElement(s);
              lpnAbstraction.getAbstractionProperty().loopAbsModel.removeElement(s);
              lpnAbstraction.getAbstractionProperty().postAbsModel.removeElement(s);
            }
          }
          if (preOrder.size() > 0)
          {
            lpnAbstraction.getAbstractionProperty().preAbsModel.removeAllElements();
          }
          for (Integer j = 0; j < preOrder.size(); j++)
          {
            lpnAbstraction.getAbstractionProperty().preAbsModel.addElement(preOrder.get(j));
          }
          if (loopOrder.size() > 0)
          {
            lpnAbstraction.getAbstractionProperty().loopAbsModel.removeAllElements();
          }
          for (Integer j = 0; j < loopOrder.size(); j++)
          {
            lpnAbstraction.getAbstractionProperty().loopAbsModel.addElement(loopOrder.get(j));
          }
          if (postOrder.size() > 0)
          {
            lpnAbstraction.getAbstractionProperty().postAbsModel.removeAllElements();
          }
          for (Integer j = 0; j < postOrder.size(); j++)
          {
            lpnAbstraction.getAbstractionProperty().postAbsModel.addElement(postOrder.get(j));
          }
          lpnAbstraction.preAbs.setListData(lpnAbstraction.getAbstractionProperty().preAbsModel.toArray());
          lpnAbstraction.loopAbs.setListData(lpnAbstraction.getAbstractionProperty().loopAbsModel.toArray());
          lpnAbstraction.postAbs.setListData(lpnAbstraction.getAbstractionProperty().postAbsModel.toArray());
        }
        if (load.getProperty("reb2sac.abstraction.method").equals("none"))
        {
          noAbstraction.setSelected(true);
          enableNoAbstraction();
        }
        else if (load.getProperty("reb2sac.abstraction.method").equals("expand"))
        {
          expandReactions.setSelected(true);
          enableNoAbstraction();
        }
        else if (load.getProperty("reb2sac.abstraction.method").equals("abs"))
        {
          reactionAbstraction.setSelected(true);
          enableReactionAbstraction();
        }
        else
        {
          stateAbstraction.setSelected(true);
          enableStateAbstraction();
        }
        if (load.containsKey("ode.simulation.absolute.error"))
        {
          absErr.setText(load.getProperty("ode.simulation.absolute.error"));
        }
        else
        {
          absErr.setText("1.0E-9");
        }
        if (load.containsKey("ode.simulation.relative.error"))
        {
          relErr.setText(load.getProperty("ode.simulation.relative.error"));
        }
        else
        {
          relErr.setText("0.0");
        }
        if (load.containsKey("monte.carlo.simulation.time.step"))
        {
          step.setText(load.getProperty("monte.carlo.simulation.time.step"));
        }
        else
        {
          step.setText("inf");
        }
        if (load.containsKey("monte.carlo.simulation.min.time.step"))
        {
          minStep.setText(load.getProperty("monte.carlo.simulation.min.time.step"));
        }
        else
        {
          minStep.setText("0");
        }
        if (load.containsKey("monte.carlo.simulation.time.limit"))
        {
          limit.setText(load.getProperty("monte.carlo.simulation.time.limit"));
        }
        else
        {
          limit.setText("100.0");
        }
        if (load.containsKey("simulation.initial.time"))
        {
          initialTimeField.setText(load.getProperty("simulation.initial.time"));
        }
        else
        {
          initialTimeField.setText("0.0");
        }
        if (load.containsKey("simulation.output.start.time"))
        {
          outputStartTimeField.setText(load.getProperty("simulation.output.start.time"));
        }
        else
        {
          outputStartTimeField.setText("0.0");
        }
        if (load.containsKey("monte.carlo.simulation.print.interval"))
        {
          intervalLabel.setSelectedItem("Print Interval");
          interval.setText(load.getProperty("monte.carlo.simulation.print.interval"));
        }
        else if (load.containsKey("monte.carlo.simulation.minimum.print.interval"))
        {
          intervalLabel.setSelectedItem("Minimum Print Interval");
          interval.setText(load.getProperty("monte.carlo.simulation.minimum.print.interval"));
        }
        else if (load.containsKey("monte.carlo.simulation.number.steps"))
        {
          intervalLabel.setSelectedItem("Number Of Steps");
          interval.setText(load.getProperty("monte.carlo.simulation.number.steps"));
        }
        else
        {
          interval.setText("1.0");
        }
        if (load.containsKey("monte.carlo.simulation.random.seed"))
        {
          seed.setText(load.getProperty("monte.carlo.simulation.random.seed"));
        }
        if (load.containsKey("monte.carlo.simulation.runs"))
        {
          runs.setText(load.getProperty("monte.carlo.simulation.runs"));
        }
        if (load.containsKey("simulation.time.series.species.level.file"))
        {
          // usingSSA.doClick();
        }
        else
        {
          description.setEnabled(true);
          explanation.setEnabled(true);
          simulators.setEnabled(true);
          simulatorsLabel.setEnabled(true);
          if (!stateAbstraction.isSelected())
          {
            ODE.setEnabled(true);
          }
          else
          {
            markov.setEnabled(true);
          }
        }
        if (load.containsKey("simulation.printer.tracking.quantity"))
        {
          if (load.getProperty("simulation.printer.tracking.quantity").equals("concentration"))
          {
            concentrations.doClick();
          }
        }
        if (load.containsKey("simulation.printer"))
        {
          if (load.getProperty("simulation.printer").equals("null.printer"))
          {
            genRuns.doClick();
          }
        }
        if (load.containsKey("reb2sac.simulation.method"))
        {
          if (load.getProperty("reb2sac.simulation.method").equals("ODE"))
          {
            ODE.setSelected(true);
            if (load.containsKey("simulation.initial.time"))
            {
              initialTimeField.setText(load.getProperty("simulation.initial.time"));
            }
            else
            {
              initialTimeField.setText("0.0");
            }
            if (load.containsKey("simulation.output.start.time"))
            {
              outputStartTimeField.setText(load.getProperty("simulation.output.start.time"));
            }
            else
            {
              outputStartTimeField.setText("0.0");
            }
            if (load.containsKey("ode.simulation.time.limit"))
            {
              limit.setText(load.getProperty("ode.simulation.time.limit"));
            }
            if (load.containsKey("ode.simulation.print.interval"))
            {
              intervalLabel.setSelectedItem("Print Interval");
              interval.setText(load.getProperty("ode.simulation.print.interval"));
            }
            if (load.containsKey("ode.simulation.minimum.print.interval"))
            {
              intervalLabel.setSelectedItem("Minimum Print Interval");
              interval.setText(load.getProperty("ode.simulation.minimum.print.interval"));
            }
            else if (load.containsKey("ode.simulation.number.steps"))
            {
              intervalLabel.setSelectedItem("Number Of Steps");
              interval.setText(load.getProperty("ode.simulation.number.steps"));
            }
            if (load.containsKey("ode.simulation.time.step"))
            {
              step.setText(load.getProperty("ode.simulation.time.step"));
            }
            if (load.containsKey("ode.simulation.min.time.step"))
            {
              minStep.setText(load.getProperty("ode.simulation.min.time.step"));
            }
            enableODE();
            if (load.containsKey("selected.simulator"))
            {
              simulators.setSelectedItem(load.getProperty("selected.simulator"));
            }
            if (load.containsKey("file.stem"))
            {
              fileStem.setText(load.getProperty("file.stem"));
            }
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("monteCarlo"))
          {
            monteCarlo.setSelected(true);
            append.setEnabled(true);
            enableMonteCarlo();
            if (load.containsKey("selected.simulator"))
            {
              String simId = load.getProperty("selected.simulator");
              if (simId.equals("mpde"))
              {
                simulators.setSelectedItem("iSSA");
                mpde.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("median_path"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("median_path-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("mean_path-adaptive"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("median_path-adaptive"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path-adaptive-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("median_path-adaptive-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                adaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("mean_path-event"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("median_path-event"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("1");
              }
              else if (simId.equals("mean_path-event-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                meanPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else if (simId.equals("median_path-event-bifurcation"))
              {
                simulators.setSelectedItem("iSSA");
                medianPath.doClick();
                nonAdaptive.doClick();
                bifurcation.setSelectedItem("2");
              }
              else
              {
                simulators.setSelectedItem(simId);
              }
            }
            if (load.containsKey("file.stem"))
            {
              fileStem.setText(load.getProperty("file.stem"));
            }
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("markov"))
          {
            markov.setSelected(true);
            enableMarkov();
            if (load.containsKey("selected.simulator"))
            {
              selectedMarkovSim = load.getProperty("selected.simulator");
              simulators.setSelectedItem(selectedMarkovSim);
            }
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("FBA"))
          {
            fba.doClick();
            enableFBA();
            // absErr.setEnabled(false);
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("SBML"))
          {
            sbml.setSelected(true);
            enableSbmlDotAndXhtml();
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("Network"))
          {
            dot.setSelected(true);
            enableSbmlDotAndXhtml();
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("Browser"))
          {
            xhtml.setSelected(true);
            enableSbmlDotAndXhtml();
          }
          else if (load.getProperty("reb2sac.simulation.method").equals("LPN"))
          {
            enableSbmlDotAndXhtml();
          }
        }
        if (load.containsKey("reb2sac.abstraction.method"))
        {
          if (load.getProperty("reb2sac.abstraction.method").equals("none"))
          {
            noAbstraction.setSelected(true);
          }
          else if (load.getProperty("reb2sac.abstraction.method").equals("expand"))
          {
            expandReactions.setSelected(true);
          }
          else if (load.getProperty("reb2sac.abstraction.method").equals("abs"))
          {
            reactionAbstraction.setSelected(true);
          }
          else if (load.getProperty("reb2sac.abstraction.method").equals("nary"))
          {
            stateAbstraction.setSelected(true);
          }
        }
        if (load.containsKey("selected.property"))
        {
          if (transientProperties != null)
          {
            transientProperties.setSelectedItem(load.getProperty("selected.property"));
          }
        }
        ArrayList<String> getLists = new ArrayList<String>();
        int i = 1;
        while (load.containsKey("simulation.run.termination.condition." + i))
        {
          getLists.add(load.getProperty("simulation.run.termination.condition." + i));
          i++;
        }
        getLists = new ArrayList<String>();
        i = 1;
        while (load.containsKey("reb2sac.interesting.species." + i))
        {
          String species = load.getProperty("reb2sac.interesting.species." + i);
          int j = 2;
          String interesting = " ";
          if (load.containsKey("reb2sac.concentration.level." + species + ".1"))
          {
            interesting += load.getProperty("reb2sac.concentration.level." + species + ".1");
          }
          while (load.containsKey("reb2sac.concentration.level." + species + "." + j))
          {
            interesting += "," + load.getProperty("reb2sac.concentration.level." + species + "." + j);
            j++;
          }
          if (!interesting.equals(" "))
          {
            species += interesting;
          }
          getLists.add(species);
          i++;
        }
        for (String s : getLists)
        {
          String[] split1 = s.split(" ");

          // load the species and its thresholds into the list of
          // interesting species
          String speciesAndThresholds = split1[0];

          if (split1.length > 1)
          {
            speciesAndThresholds += " " + split1[1];
          }

          interestingSpecies.add(speciesAndThresholds);
        }

        getLists = new ArrayList<String>();
        i = 1;
        while (load.containsKey("gcm.abstraction.method." + i))
        {
          getLists.add(load.getProperty("gcm.abstraction.method." + i));
          i++;
        }
        i = 1;
        while (load.containsKey("reb2sac.abstraction.method.1." + i))
        {
          getLists.add(load.getProperty("reb2sac.abstraction.method.1." + i));
          i++;
        }
        preAbstractions = getLists.toArray();
        preAbs.setListData(preAbstractions);

        getLists = new ArrayList<String>();
        i = 1;
        while (load.containsKey("reb2sac.abstraction.method.2." + i))
        {
          getLists.add(load.getProperty("reb2sac.abstraction.method.2." + i));
          i++;
        }
        loopAbstractions = getLists.toArray();
        loopAbs.setListData(loopAbstractions);

        getLists = new ArrayList<String>();
        i = 1;
        while (load.containsKey("reb2sac.abstraction.method.3." + i))
        {
          getLists.add(load.getProperty("reb2sac.abstraction.method.3." + i));
          i++;
        }
        postAbstractions = getLists.toArray();
        postAbs.setListData(postAbstractions);

        if (load.containsKey("reb2sac.rapid.equilibrium.condition.1"))
        {
          rapid1.setText(load.getProperty("reb2sac.rapid.equilibrium.condition.1"));
        }
        if (load.containsKey("reb2sac.rapid.equilibrium.condition.2"))
        {
          rapid2.setText(load.getProperty("reb2sac.rapid.equilibrium.condition.2"));
        }
        if (load.containsKey("reb2sac.qssa.condition.1"))
        {
          qssa.setText(load.getProperty("reb2sac.qssa.condition.1"));
        }
        if (load.containsKey("reb2sac.operator.max.concentration.threshold"))
        {
          maxCon.setText(load.getProperty("reb2sac.operator.max.concentration.threshold"));
        }
        if (load.containsKey("reb2sac.diffusion.stoichiometry.amplification.value"))
        {
          diffStoichAmp.setText(load.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
        }
        if (load.containsKey("reb2sac.iSSA.number.paths"))
        {
          bifurcation.setSelectedItem(load.getProperty("reb2sac.iSSA.number.paths"));
        }
        if (load.containsKey("reb2sac.iSSA.type"))
        {
          String type = load.getProperty("reb2sac.iSSA.type");
          if (type.equals("mpde"))
          {
            mpde.doClick();
          }
          else if (type.equals("medianPath"))
          {
            medianPath.doClick();
          }
          else
          {
            meanPath.doClick();
          }
        }
        if (load.containsKey("reb2sac.iSSA.adaptive"))
        {
          String type = load.getProperty("reb2sac.iSSA.adaptive");
          if (type.equals("true"))
          {
            adaptive.doClick();
          }
          else
          {
            nonAdaptive.doClick();
          }
        }
      }
      else
      {
        if (load.containsKey("selected.simulator"))
        {
          simulators.setSelectedItem(load.getProperty("selected.simulator"));
        }
        if (load.containsKey("file.stem"))
        {
          fileStem.setText(load.getProperty("file.stem"));
        }
        if (load.containsKey("simulation.printer.tracking.quantity"))
        {
          if (load.getProperty("simulation.printer.tracking.quantity").equals("concentration"))
          {
            concentrations.doClick();
          }
        }
        if (load.containsKey("simulation.printer"))
        {
          if (load.getProperty("simulation.printer").equals("null.printer"))
          {
            genRuns.doClick();
          }
        }
      }
      change = false;
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      JOptionPane.showMessageDialog(Gui.frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
    }
  }

  public Graph createGraph(String open)
  {
    String outDir = root + GlobalConstants.separator + simName;
    String printer_id;
    printer_id = "tsd.printer";
    String printer_track_quantity = "amount";
    if (concentrations.isSelected())
    {
      printer_track_quantity = "concentration";
    }
    return new Graph(this, printer_track_quantity, simName + " simulation results", printer_id, outDir, "time", gui, open, log, null, true, false);
  }

  public void executeRun()
  {
    boolean ignoreSweep = false;
    if (sbml.isSelected() || dot.isSelected() || xhtml.isSelected())
    {
      ignoreSweep = true;
    }
    String stem = "";
    if (!fileStem.getText().trim().equals(""))
    {
      if (!(stemPat.matcher(fileStem.getText().trim()).matches()))
      {
        JOptionPane.showMessageDialog(Gui.frame, "A file stem can only contain letters, numbers, and underscores.", "Invalid File Stem", JOptionPane.ERROR_MESSAGE);
        return;
      }
      stem += fileStem.getText().trim();
    }
    for (int i = 0; i < gui.getTab().getTabCount(); i++)
    {
      if (modelEditor != null)
      {
        if (gui.getTitleAt(i).equals(modelEditor.getRefFile()))
        {
          if (gui.getTab().getComponentAt(i) instanceof ModelEditor)
          {
            ModelEditor gcm = ((ModelEditor) (gui.getTab().getComponentAt(i)));
            if (gcm.isDirty())
            {
              Object[] options = { "Yes", "No" };
              int value = JOptionPane.showOptionDialog(Gui.frame, "Do you want to save changes to " + modelEditor.getRefFile() + " before running the simulation?", "Save Changes", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
              if (value == JOptionPane.YES_OPTION)
              {
                gcm.save(false);
              }
            }
          }
        }
      }
      else
      {
        if (gui.getTitleAt(i).equals(modelFile))
        {
          if (gui.getTab().getComponentAt(i) instanceof LHPNEditor)
          {
            LHPNEditor lpn = ((LHPNEditor) (gui.getTab().getComponentAt(i)));
            if (lpn.isDirty())
            {
              Object[] options = { "Yes", "No" };
              int value = JOptionPane.showOptionDialog(Gui.frame, "Do you want to save changes to " + modelFile + " before running the simulation?", "Save Changes", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
              if (value == JOptionPane.YES_OPTION)
              {
                lpn.save();
              }
            }
          }
        }
      }
    }
    if (modelEditor != null)
    {
      saveSEDML();
      modelEditor.saveParams(true, stem, ignoreSweep, simulators.getSelectedItem().toString());
    }
    else
    {
      if (!stem.equals(""))
      {
      }
      Translator t1 = new Translator();
      if (reactionAbstraction.isSelected())
      {
        try {
          LPN lhpnFile = new LPN();
          lhpnFile.load(root + GlobalConstants.separator + modelFile);
          Abstraction abst = new Abstraction(lhpnFile, lpnAbstraction.getAbstractionProperty());
          abst.abstractSTG(false);
          abst.save(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile);
        
          if (transientProperties != null && !((String) transientProperties.getSelectedItem()).equals("none"))
          {

            t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile, ((String) transientProperties.getSelectedItem()));

          }
          else
          {
            t1.convertLPN2SBML(root + GlobalConstants.separator + simName + GlobalConstants.separator + modelFile, "");
          }
        } catch (BioSimException e) {
          // TODO Auto-generated catch block
          JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(), JOptionPane.ERROR_MESSAGE); 
          
          e.printStackTrace();
        }
      }
      else
      {
        try {
          if (transientProperties != null && !((String) transientProperties.getSelectedItem()).equals("none"))
          {
            t1.convertLPN2SBML(root + GlobalConstants.separator + modelFile, ((String) transientProperties.getSelectedItem()));
          }
          else
          {
            t1.convertLPN2SBML(root + GlobalConstants.separator + modelFile, "");
          }
        } catch (BioSimException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      t1.setFilename(root + GlobalConstants.separator + simName + GlobalConstants.separator + stem + GlobalConstants.separator + modelFile.replace(".lpn", ".xml"));
      t1.outputSBML();
      if (!stem.equals(""))
      {
        new AnalysisThread(this).start(stem, true);
      }
      else
      {
        new AnalysisThread(this).start(".", true);
      }
    }
  }

  public boolean noExpand()
  {
    return noAbstraction.isSelected();
  }

  // Reports which gcm abstraction options are selected
  public ArrayList<String> getGcmAbstractions()
  {
    ArrayList<String> gcmAbsList = new ArrayList<String>();
    ListModel preAbsList = preAbs.getModel();
    for (int i = 0; i < preAbsList.getSize(); i++)
    {
      String abstractionOption = (String) preAbsList.getElementAt(i);
      if (abstractionOption.equals("complex-formation-and-sequestering-abstraction") || abstractionOption.equals("operator-site-reduction-abstraction"))
      {
        gcmAbsList.add(abstractionOption);
      }
    }
    return gcmAbsList;
  }

  // Reports if any reb2sac abstraction options are selected
  public boolean reb2sacAbstraction()
  {
    ListModel preAbsList = preAbs.getModel();
    for (int i = 0; i < preAbsList.getSize(); i++)
    {
      String abstractionOption = (String) preAbsList.getElementAt(i);
      if (!abstractionOption.equals("complex-formation-and-sequestering-abstraction") && !abstractionOption.equals("operator-site-reduction-abstraction"))
      {
        return true;
      }
    }
    ListModel loopAbsList = loopAbs.getModel();
    if (loopAbsList.getSize() > 0)
    {
      return true;
    }
    ListModel postAbsList = postAbs.getModel();
    if (postAbsList.getSize() > 0)
    {
      return true;
    }
    return false;
  }

  public void setModelEditor(ModelEditor modelEditor)
  {
    this.modelEditor = modelEditor;
    if (markov.isSelected())
    {
      simulators.removeAllItems();
      simulators.addItem("steady-state-markov-chain-analysis");
      simulators.addItem("transient-markov-chain-analysis");
      simulators.addItem("reachability-analysis");
      simulators.addItem("prism");
      if (Gui.isReb2sacFound())
      {
        simulators.addItem("atacs");
        simulators.addItem("ctmc-transient");
      }
      if (selectedMarkovSim != null)
      {
        simulators.setSelectedItem(selectedMarkovSim);
      }
    }
    change = false;
  }

  public void setSim(String newSimName)
  {
    sbmlFile = sbmlFile.replace("\\", "/");
    sbmlProp = root + GlobalConstants.separator + newSimName + GlobalConstants.separator + sbmlFile.split("/")[sbmlFile.split("/").length - 1];
    simName = newSimName;
  }

  public boolean hasChanged()
  {
    return change;
  }

  public String[] getInterestingSpecies()
  {
    return interestingSpecies.toArray(new String[0]);
  }

  public ArrayList<String> getInterestingSpeciesAsArrayList()
  {
    return interestingSpecies;
  }

  /**
   * adds a string with the species ID and its threshold values to the
   * arraylist of interesting species
   * 
   * @param speciesAndThresholds
   */
  public void addInterestingSpecies(String speciesAndThresholds)
  {
    String species = speciesAndThresholds.split(" ")[0];
    for (int i = 0; i < interestingSpecies.size(); i++)
    {
      if (interestingSpecies.get(i).split(" ")[0].equals(species))
      {
        interestingSpecies.set(i, speciesAndThresholds);
        return;
      }
    }
    interestingSpecies.add(speciesAndThresholds);
  }

  /**
   * removes a string with the species ID and its threshold values from the
   * arraylist of interesting species
   * 
   * @param species
   */
  public void removeInterestingSpecies(String species)
  {
    for (int i = 0; i < interestingSpecies.size(); i++)
    {
      if (interestingSpecies.get(i).split(" ")[0].equals(species))
      {
        interestingSpecies.remove(i);
        return;
      }
    }
  }

  public Graph createProbGraph(String open)
  {
    String outDir = root + GlobalConstants.separator + simName;
    String printer_id;
    printer_id = "tsd.printer";
    String printer_track_quantity = "amount";
    if (concentrations.isSelected())
    {
      printer_track_quantity = "concentration";
    }
    return new Graph(this, printer_track_quantity, simName + " simulation results", printer_id, outDir, "time", gui, open, log, null, false, false);
  }

  public void run(ArrayList<AnalysisThread> threads, ArrayList<String> dirs, ArrayList<String> levelOne, String stem)
  {
    for (AnalysisThread thread : threads)
    {
      try
      {
        thread.join();
      }
      catch (InterruptedException e)
      {
      }
    }
    if (!dirs.isEmpty() && new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + stem + dirs.get(0) + GlobalConstants.separator + "sim-rep.txt").exists())
    {
      ArrayList<String> dataLabels = new ArrayList<String>();
      ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
      String spec = dirs.get(0).split("=")[0];
      dataLabels.add(spec);
      data.add(new ArrayList<Double>());
      for (String prefix : levelOne)
      {
        double val = Double.parseDouble(prefix.split("=")[1].split("_")[0]);
        data.get(0).add(val);
        for (String d : dirs)
        {
          if (d.startsWith(prefix))
          {
            String suffix = d.replace(prefix, "");
            ArrayList<String> vals = new ArrayList<String>();
            try
            {
              Scanner s = new Scanner(new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + stem + d + GlobalConstants.separator + "sim-rep.txt"));
              while (s.hasNextLine())
              {
                String[] ss = s.nextLine().split(" ");
                if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
                {
                }
                if (vals.size() == 0)
                {
                  for (String add : ss)
                  {
                    vals.add(add + suffix);
                  }
                }
                else
                {
                  for (int i = 0; i < ss.length; i++)
                  {
                    vals.set(i, vals.get(i) + " " + ss[i]);
                  }
                }
              }
              s.close();
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
            double total = 0;
            int i = 0;
            if (vals.get(0).split(" ")[0].startsWith("#total"))
            {
              total = Double.parseDouble(vals.get(0).split(" ")[1]);
              i = 1;
            }
            for (; i < vals.size(); i++)
            {
              int index;
              if (dataLabels.contains(vals.get(i).split(" ")[0]))
              {
                index = dataLabels.indexOf(vals.get(i).split(" ")[0]);
              }
              else
              {
                dataLabels.add(vals.get(i).split(" ")[0]);
                data.add(new ArrayList<Double>());
                index = dataLabels.size() - 1;
              }
              if (total == 0)
              {
                data.get(index).add(Double.parseDouble(vals.get(i).split(" ")[1]));
              }
              else
              {
                data.get(index).add(100 * ((Double.parseDouble(vals.get(i).split(" ")[1])) / total));
              }
            }
          }
        }
      }
      DataParser constData = new DataParser(dataLabels, data);
      constData.outputTSD(root + GlobalConstants.separator + simName + GlobalConstants.separator + "sim-rep.tsd");
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

  @Override
  public void run()
  {
  }

  public void createAdvancedOptionsTab()
  {
    preAbs = new JList();
    loopAbs = new JList();
    postAbs = new JList();
    preAbsLabel = new JLabel("Preprocess abstraction methods:");
    loopAbsLabel = new JLabel("Main loop abstraction methods:");
    postAbsLabel = new JLabel("Postprocess abstraction methods:");
    JPanel absHolder = new JPanel(new BorderLayout());
    JPanel listOfAbsLabelHolder = new JPanel(new GridLayout(1, 3));
    JPanel listOfAbsHolder = new JPanel(new GridLayout(1, 3));
    JPanel listOfAbsButtonHolder = new JPanel(new GridLayout(1, 3));
    JScrollPane preAbsScroll = new JScrollPane();
    JScrollPane loopAbsScroll = new JScrollPane();
    JScrollPane postAbsScroll = new JScrollPane();
    preAbsScroll.setMinimumSize(new Dimension(260, 200));
    preAbsScroll.setPreferredSize(new Dimension(276, 132));
    preAbsScroll.setViewportView(preAbs);
    loopAbsScroll.setMinimumSize(new Dimension(260, 200));
    loopAbsScroll.setPreferredSize(new Dimension(276, 132));
    loopAbsScroll.setViewportView(loopAbs);
    postAbsScroll.setMinimumSize(new Dimension(260, 200));
    postAbsScroll.setPreferredSize(new Dimension(276, 132));
    postAbsScroll.setViewportView(postAbs);
    addPreAbs = new JButton("Add");
    rmPreAbs = new JButton("Remove");
    editPreAbs = new JButton("Edit");
    JPanel preAbsButtonHolder = new JPanel();
    preAbsButtonHolder.add(addPreAbs);
    preAbsButtonHolder.add(rmPreAbs);
    addLoopAbs = new JButton("Add");
    rmLoopAbs = new JButton("Remove");
    editLoopAbs = new JButton("Edit");
    JPanel loopAbsButtonHolder = new JPanel();
    loopAbsButtonHolder.add(addLoopAbs);
    loopAbsButtonHolder.add(rmLoopAbs);
    addPostAbs = new JButton("Add");
    rmPostAbs = new JButton("Remove");
    editPostAbs = new JButton("Edit");
    JPanel postAbsButtonHolder = new JPanel();
    postAbsButtonHolder.add(addPostAbs);
    postAbsButtonHolder.add(rmPostAbs);
    listOfAbsLabelHolder.add(preAbsLabel);
    listOfAbsHolder.add(preAbsScroll);
    listOfAbsLabelHolder.add(loopAbsLabel);
    listOfAbsHolder.add(loopAbsScroll);
    listOfAbsLabelHolder.add(postAbsLabel);
    listOfAbsHolder.add(postAbsScroll);
    listOfAbsButtonHolder.add(preAbsButtonHolder);
    listOfAbsButtonHolder.add(loopAbsButtonHolder);
    listOfAbsButtonHolder.add(postAbsButtonHolder);
    absHolder.add(listOfAbsLabelHolder, "North");
    absHolder.add(listOfAbsHolder, "Center");
    absHolder.add(listOfAbsButtonHolder, "South");
    preAbs.setEnabled(false);
    loopAbs.setEnabled(false);
    postAbs.setEnabled(false);
    preAbs.addMouseListener(this);
    loopAbs.addMouseListener(this);
    postAbs.addMouseListener(this);
    preAbsLabel.setEnabled(false);
    loopAbsLabel.setEnabled(false);
    postAbsLabel.setEnabled(false);
    addPreAbs.setEnabled(false);
    rmPreAbs.setEnabled(false);
    editPreAbs.setEnabled(false);
    addPreAbs.addActionListener(this);
    rmPreAbs.addActionListener(this);
    editPreAbs.addActionListener(this);
    addLoopAbs.setEnabled(false);
    rmLoopAbs.setEnabled(false);
    editLoopAbs.setEnabled(false);
    addLoopAbs.addActionListener(this);
    rmLoopAbs.addActionListener(this);
    editLoopAbs.addActionListener(this);
    addPostAbs.setEnabled(false);
    rmPostAbs.setEnabled(false);
    editPostAbs.setEnabled(false);
    addPostAbs.addActionListener(this);
    rmPostAbs.addActionListener(this);
    editPostAbs.addActionListener(this);

    // Creates some abstraction options
    JPanel advancedGrid = new JPanel(new GridLayout(8, 4));
    advanced = new JPanel(new BorderLayout());

    rapidLabel1 = new JLabel("Rapid Equilibrium Condition 1:");
    rapid1 = new JTextField(biosimrc.get("biosim.sim.rapid1", ""), 15);
    rapidLabel2 = new JLabel("Rapid Equilibrium Condition 2:");
    rapid2 = new JTextField(biosimrc.get("biosim.sim.rapid2", ""), 15);
    qssaLabel = new JLabel("QSSA Condition:");
    qssa = new JTextField(biosimrc.get("biosim.sim.qssa", ""), 15);
    maxConLabel = new JLabel("Max Concentration Threshold:");
    maxCon = new JTextField(biosimrc.get("biosim.sim.concentration", ""), 15);
    diffStoichAmp = new JTextField("1.0", 15);
    diffStoichAmpLabel = new JLabel("Grid Diffusion Stoichiometry Amplification:");
    String[] options = { "1", "2" };

    mpde = new JRadioButton();
    mpde.setText("MPDE");
    mpde.addActionListener(this);
    meanPath = new JRadioButton();
    meanPath.setText("Mean Path");
    meanPath.addActionListener(this);
    medianPath = new JRadioButton();
    medianPath.setText("Median Path");
    medianPath.addActionListener(this);
    ButtonGroup iSSATypeButtons = new ButtonGroup();
    iSSATypeButtons.add(mpde);
    iSSATypeButtons.add(meanPath);
    iSSATypeButtons.add(medianPath);
    medianPath.setSelected(true);
    JPanel iSSAType = new JPanel(new GridLayout(1, 3));
    iSSAType.add(mpde);
    iSSAType.add(meanPath);
    iSSAType.add(medianPath);
    iSSATypeLabel = new JLabel("iSSA Type:");

    adaptive = new JRadioButton();
    adaptive.setText("Adaptive");
    nonAdaptive = new JRadioButton();
    nonAdaptive.setText("Non-adaptive");
    ButtonGroup iSSAAdaptiveButtons = new ButtonGroup();
    iSSAAdaptiveButtons.add(adaptive);
    iSSAAdaptiveButtons.add(nonAdaptive);
    adaptive.setSelected(true);
    JPanel iSSAAdaptive = new JPanel(new GridLayout(1, 2));
    iSSAAdaptive.add(adaptive);
    iSSAAdaptive.add(nonAdaptive);
    iSSAAdaptiveLabel = new JLabel("iSSA Adaptive:");

    bifurcation = new JComboBox(options);
    bifurcationLabel = new JLabel("Number of Paths to Follow with iSSA:");

    maxConLabel.setEnabled(false);
    maxCon.setEnabled(false);
    qssaLabel.setEnabled(false);
    qssa.setEnabled(false);
    rapidLabel1.setEnabled(false);
    rapid1.setEnabled(false);
    rapidLabel2.setEnabled(false);
    rapid2.setEnabled(false);
    diffStoichAmp.setEnabled(false);
    diffStoichAmpLabel.setEnabled(false);
    mpde.setEnabled(false);
    meanPath.setEnabled(false);
    medianPath.setEnabled(false);
    iSSATypeLabel.setEnabled(false);
    adaptive.setEnabled(false);
    nonAdaptive.setEnabled(false);
    iSSAAdaptiveLabel.setEnabled(false);
    bifurcation.setEnabled(false);
    bifurcationLabel.setEnabled(false);

    advancedGrid.add(rapidLabel1);
    advancedGrid.add(rapid1);
    advancedGrid.add(rapidLabel2);
    advancedGrid.add(rapid2);
    advancedGrid.add(qssaLabel);
    advancedGrid.add(qssa);
    advancedGrid.add(maxConLabel);
    advancedGrid.add(maxCon);
    advancedGrid.add(diffStoichAmpLabel);
    advancedGrid.add(diffStoichAmp);
    advancedGrid.add(iSSATypeLabel);
    advancedGrid.add(iSSAType);
    advancedGrid.add(iSSAAdaptiveLabel);
    advancedGrid.add(iSSAAdaptive);
    advancedGrid.add(bifurcationLabel);
    advancedGrid.add(bifurcation);
    JPanel advAbs = new JPanel(new BorderLayout());
    advAbs.add(absHolder, "Center");
    advAbs.add(advancedGrid, "South");
    advanced.add(advAbs, "North");
  }

  public JPanel getAdvanced()
  {
    JPanel constructPanel = new JPanel(new BorderLayout());
    constructPanel.add(advanced, "Center");
    return constructPanel;
  }

  public String getProperty()
  {
    if (transientProperties != null)
    {
      if (!((String) transientProperties.getSelectedItem()).equals("none"))
      {
        return ((String) transientProperties.getSelectedItem());
      }
      return "";
    }
    return null;
  }

  public String getSimID()
  {
    return fileStem.getText().trim();
  }

  public String getSimPath()
  {
    if (!fileStem.getText().trim().equals(""))
    {
      return root + GlobalConstants.separator + simName + GlobalConstants.separator + fileStem.getText().trim();
    }
    return root + GlobalConstants.separator + simName;
  }

  public String getSimName()
  {
    return simName;
  }

  public void updateBackgroundFile(String updatedFile)
  {
    modelFileField.setText(updatedFile);
  }

  public String getBackgroundFile()
  {
    return modelFileField.getText();
  }

  public void updateProperties()
  {
    if (transientProperties != null && modelFile.contains(".lpn"))
    {
      Object selected = transientProperties.getSelectedItem();
      String[] props = new String[] { "none" };
      LPN lpn = new LPN();
      try {
        lpn.load(root + GlobalConstants.separator + modelFile);
        String[] getProps = lpn.getProperties().toArray(new String[0]);
        props = new String[getProps.length + 1];
        props[0] = "none";
        for (int i = 0; i < getProps.length; i++)
        {
          props[i + 1] = getProps[i];
        }
        transientProperties.removeAllItems();
        for (String s : props)
        {
          transientProperties.addItem(s);
        }
        transientProperties.setSelectedItem(selected);
      } catch (BioSimException e) {
        JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(), JOptionPane.ERROR_MESSAGE); 
        e.printStackTrace();
      }
      
    }
  }

  private void enableAbstractionOptions(boolean enable, boolean edit)
  {
    preAbs.setEnabled(enable);
    loopAbs.setEnabled(enable);
    postAbs.setEnabled(enable);
    preAbsLabel.setEnabled(enable);
    loopAbsLabel.setEnabled(enable);
    postAbsLabel.setEnabled(enable);
    addPreAbs.setEnabled(edit);
    rmPreAbs.setEnabled(edit);
    editPreAbs.setEnabled(edit);
    addLoopAbs.setEnabled(edit);
    rmLoopAbs.setEnabled(edit);
    editLoopAbs.setEnabled(edit);
    addPostAbs.setEnabled(edit);
    rmPostAbs.setEnabled(edit);
    editPostAbs.setEnabled(edit);
    maxConLabel.setEnabled(enable);
    maxCon.setEnabled(enable);
    diffStoichAmpLabel.setEnabled(enable);
    diffStoichAmp.setEnabled(enable);
    qssaLabel.setEnabled(enable);
    qssa.setEnabled(enable);
    rapidLabel1.setEnabled(enable);
    rapid1.setEnabled(enable);
    rapidLabel2.setEnabled(enable);
    rapid2.setEnabled(enable);
  }

  /**
   * This method enables and disables the required fields for none and
   * abstraction.
   */
  private void enableNoAbstraction()
  {
    ODE.setEnabled(true);
    monteCarlo.setEnabled(true);
    fba.setEnabled(true);
    markov.setEnabled(false);
    enableAbstractionOptions(false, false);
    ArrayList<String> getLists = new ArrayList<String>();
    Object[] objects = getLists.toArray();
    preAbs.setListData(objects);
    loopAbs.setListData(objects);
    getLists = new ArrayList<String>();
    if (monteCarlo.isSelected())
    {
      getLists.add("distribute-transformer");
      getLists.add("reversible-to-irreversible-transformer");
    }
    if (monteCarlo.isSelected() || ODE.isSelected())
    {
      getLists.add("kinetic-law-constants-simplifier");
    }
    objects = getLists.toArray();
    postAbs.setListData(objects);
    if (markov.isSelected())
    {
      ODE.setSelected(true);
      enableODE();
    }
    if (modelFile.contains(".lpn") || modelFile.contains(".s") || modelFile.contains(".inst"))
    {
      markov.setEnabled(true);
    }
    if (!fba.isSelected() && !sbml.isSelected() && !xhtml.isSelected() && !dot.isSelected())
    {
      append.setEnabled(true);
      concentrations.setEnabled(true);
      genRuns.setEnabled(true);
      genStats.setEnabled(true);
      report.setEnabled(true);
      setupToAppendRuns(false);
    }
  }

  /**
   * This method enables and disables the required fields for none and
   * abstraction.
   */
  private void enableReactionAbstraction()
  {
    ODE.setEnabled(true);
    monteCarlo.setEnabled(true);
    fba.setEnabled(true);
    markov.setEnabled(false);
    enableAbstractionOptions(true, true);
    ArrayList<String> getLists = new ArrayList<String>();
    getLists.add("complex-formation-and-sequestering-abstraction");
    getLists.add("operator-site-reduction-abstraction");
    Object[] objects = getLists.toArray();
    preAbs.setListData(objects);
    getLists = new ArrayList<String>();
    objects = getLists.toArray();
    loopAbs.setListData(objects);
    getLists = new ArrayList<String>();
    if (monteCarlo.isSelected())
    {
      getLists.add("distribute-transformer");
      getLists.add("reversible-to-irreversible-transformer");
    }
    if (monteCarlo.isSelected() || ODE.isSelected())
    {
      getLists.add("kinetic-law-constants-simplifier");
    }
    objects = getLists.toArray();
    postAbs.setListData(objects);
    if (markov.isSelected())
    {
      ODE.setSelected(true);
      enableODE();
    }
    if (modelFile.contains(".lpn") || modelFile.contains(".s") || modelFile.contains(".inst"))
    {
      markov.setEnabled(true);
    }
    if (!fba.isSelected() && !sbml.isSelected() && !xhtml.isSelected() && !dot.isSelected())
    {
      append.setEnabled(true);
      concentrations.setEnabled(true);
      genRuns.setEnabled(true);
      genStats.setEnabled(true);
      report.setEnabled(true);
      setupToAppendRuns(false);
    }
  }

  /**
   * This method enables and disables the required fields for FBA.
   */
  private void enableFBA()
  {
    seed.setEnabled(false);
    seedLabel.setEnabled(false);
    runs.setEnabled(false);
    runsLabel.setEnabled(false);
    fileStem.setEnabled(false);
    fileStemLabel.setEnabled(false);
    minStepLabel.setEnabled(false);
    minStep.setEnabled(false);
    stepLabel.setEnabled(false);
    step.setEnabled(false);
    errorLabel.setEnabled(true);
    absErr.setEnabled(true);
    relErrorLabel.setEnabled(true);
    relErr.setEnabled(true);
    initialTimeLabel.setEnabled(false);
    initialTimeField.setEnabled(false);
    outputStartTimeLabel.setEnabled(false);
    outputStartTimeField.setEnabled(false);
    limitLabel.setEnabled(false);
    limit.setEnabled(false);
    intervalLabel.setEnabled(false);
    interval.setEnabled(false);
    simulators.setEnabled(false);
    simulatorsLabel.setEnabled(false);
    explanation.setEnabled(false);
    description.setEnabled(false);
    reactionAbstraction.setEnabled(true);
    stateAbstraction.setEnabled(false);
    fileStem.setText("");
    ArrayList<String> getLists = new ArrayList<String>();
    Object[] objects = getLists.toArray();
    postAbs.setListData(objects);
    append.setEnabled(false);
    concentrations.setEnabled(false);
    genRuns.setEnabled(false);
    genStats.setEnabled(false);
    absErr.setEnabled(true);
    report.setEnabled(false);
    disableiSSASimulatorOptions();
  }

  /* Enable options for iSSA Mean or Median Path method */
  private void enableMeanMedianPath()
  {
    adaptive.setEnabled(true);
    nonAdaptive.setEnabled(true);
    iSSAAdaptiveLabel.setEnabled(true);
    bifurcation.setEnabled(true);
    bifurcationLabel.setEnabled(true);
  }

  /* Enable options for an ODE simulator */
  private void enableODESimulator()
  {
    minStep.setEnabled(true);
    minStepLabel.setEnabled(true);
    step.setEnabled(true);
    stepLabel.setEnabled(true);
    absErr.setEnabled(true);
    errorLabel.setEnabled(true);
    relErr.setEnabled(true);
    relErrorLabel.setEnabled(true);
    disableiSSASimulatorOptions();
  }

  /* Enable options for an SSA simulator */
  private void enableSSASimulator()
  {
    minStep.setEnabled(true);
    minStepLabel.setEnabled(true);
    step.setEnabled(true);
    stepLabel.setEnabled(true);
    errorLabel.setEnabled(false);
    absErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    relErr.setEnabled(false);
    disableiSSASimulatorOptions();
  }

  /* Enable options for an iSSA simulator */
  private void enableiSSASimulator()
  {
    minStep.setEnabled(true);
    minStepLabel.setEnabled(true);
    step.setEnabled(true);
    stepLabel.setEnabled(true);
    errorLabel.setEnabled(false);
    absErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    relErr.setEnabled(false);
    mpde.setEnabled(true);
    meanPath.setEnabled(true);
    medianPath.setEnabled(true);
    iSSATypeLabel.setEnabled(true);
    if (mpde.isSelected())
    {
      adaptive.setEnabled(false);
      nonAdaptive.setEnabled(false);
      iSSAAdaptiveLabel.setEnabled(false);
      bifurcation.setEnabled(false);
      bifurcationLabel.setEnabled(false);
    }
    else
    {
      adaptive.setEnabled(true);
      nonAdaptive.setEnabled(true);
      iSSAAdaptiveLabel.setEnabled(true);
      bifurcation.setEnabled(true);
      bifurcationLabel.setEnabled(true);
    }
  }

  /* Disable options for an iSSA simulator */
  private void disableiSSASimulatorOptions()
  {
    mpde.setEnabled(false);
    meanPath.setEnabled(false);
    medianPath.setEnabled(false);
    iSSATypeLabel.setEnabled(false);
    adaptive.setEnabled(false);
    nonAdaptive.setEnabled(false);
    iSSAAdaptiveLabel.setEnabled(false);
    bifurcation.setEnabled(false);
    bifurcationLabel.setEnabled(false);
  }

  /* Enable options for a Markov analyzer */
  private void enableMarkovAnalyzer()
  {
    minStep.setEnabled(false);
    minStepLabel.setEnabled(false);
    step.setEnabled(false);
    stepLabel.setEnabled(false);
    errorLabel.setEnabled(false);
    absErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    relErr.setEnabled(false);
    initialTimeLabel.setEnabled(false);
    initialTimeField.setEnabled(false);
    outputStartTimeLabel.setEnabled(false);
    outputStartTimeField.setEnabled(false);
    limitLabel.setEnabled(false);
    limit.setEnabled(false);
    intervalLabel.setEnabled(false);
    interval.setEnabled(false);
    disableiSSASimulatorOptions();
  }

  /* Enable options for a transient Markov analyzer */
  private void enableTransientMarkovAnalyzer()
  {
    minStep.setEnabled(false);
    minStepLabel.setEnabled(false);
    step.setEnabled(true);
    stepLabel.setEnabled(true);
    errorLabel.setEnabled(true);
    absErr.setEnabled(true);
    relErrorLabel.setEnabled(true);
    relErr.setEnabled(true);
    initialTimeLabel.setEnabled(false);
    initialTimeField.setEnabled(false);
    outputStartTimeLabel.setEnabled(false);
    outputStartTimeField.setEnabled(false);
    limitLabel.setEnabled(true);
    limit.setEnabled(true);
    intervalLabel.setEnabled(true);
    interval.setEnabled(true);
    disableiSSASimulatorOptions();
  }

  /* Enable options for Euler method */
  private void enableEulerMethod()
  {
    minStep.setEnabled(true);
    minStepLabel.setEnabled(true);
    step.setEnabled(true);
    stepLabel.setEnabled(true);
    absErr.setEnabled(false);
    errorLabel.setEnabled(false);
    relErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    disableiSSASimulatorOptions();
  }

  /* Enable options for MPDE method */
  private void enableMPDEMethod()
  {
    nonAdaptive.setSelected(true);
    adaptive.setEnabled(false);
    nonAdaptive.setEnabled(false);
    iSSAAdaptiveLabel.setEnabled(false);
    bifurcation.setEnabled(false);
    bifurcationLabel.setEnabled(false);
  }

  /**
   * This method enables and disables the required fields for sbml, dot, and
   * xhtml.
   */
  private void enableSbmlDotAndXhtml()
  {
    seed.setEnabled(false);
    seedLabel.setEnabled(false);
    runs.setEnabled(false);
    runsLabel.setEnabled(false);
    fileStem.setEnabled(false);
    fileStemLabel.setEnabled(false);
    minStepLabel.setEnabled(false);
    minStep.setEnabled(false);
    stepLabel.setEnabled(false);
    step.setEnabled(false);
    errorLabel.setEnabled(false);
    absErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    relErr.setEnabled(false);
    initialTimeLabel.setEnabled(false);
    initialTimeField.setEnabled(false);
    outputStartTimeLabel.setEnabled(false);
    outputStartTimeField.setEnabled(false);
    limitLabel.setEnabled(false);
    limit.setEnabled(false);
    intervalLabel.setEnabled(false);
    interval.setEnabled(false);
    simulators.setEnabled(false);
    simulatorsLabel.setEnabled(false);
    explanation.setEnabled(false);
    description.setEnabled(false);
    reactionAbstraction.setEnabled(true);
    stateAbstraction.setEnabled(true);
    fileStem.setText("");
    ArrayList<String> getLists = new ArrayList<String>();
    // getLists.add("kinetic-law-constants-simplifier");
    Object[] objects = getLists.toArray();
    postAbs.setListData(objects);
    append.setEnabled(false);
    concentrations.setEnabled(false);
    genRuns.setEnabled(false);
    genStats.setEnabled(false);
    report.setEnabled(false);
    disableiSSASimulatorOptions();
  }

  /**
   * This method enables and disables the required fields for Markov analysis.
   */
  private void enableMarkov()
  {
    seed.setEnabled(false);
    seedLabel.setEnabled(false);
    runs.setEnabled(false);
    runsLabel.setEnabled(false);
    fileStem.setEnabled(true);
    fileStemLabel.setEnabled(true);
    minStepLabel.setEnabled(false);
    minStep.setEnabled(false);
    stepLabel.setEnabled(false);
    step.setEnabled(false);
    errorLabel.setEnabled(false);
    absErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    relErr.setEnabled(false);
    initialTimeLabel.setEnabled(false);
    initialTimeField.setEnabled(false);
    outputStartTimeLabel.setEnabled(false);
    outputStartTimeField.setEnabled(false);
    limitLabel.setEnabled(false);
    limit.setEnabled(false);
    intervalLabel.setEnabled(false);
    interval.setEnabled(false);
    simulators.setEnabled(true);
    simulatorsLabel.setEnabled(true);
    explanation.setEnabled(true);
    description.setEnabled(true);
    simulators.removeAllItems();
    reactionAbstraction.setEnabled(true);
    stateAbstraction.setEnabled(true);
    if (modelEditor != null || modelFile.contains(".lpn"))
    {
      simulators.addItem("steady-state-markov-chain-analysis");
      simulators.addItem("transient-markov-chain-analysis");
      simulators.addItem("reachability-analysis");
      simulators.addItem("prism");
    }
    if (Gui.isReb2sacFound())
    {
      simulators.addItem("atacs");
      simulators.addItem("ctmc-transient");
    }
    ArrayList<String> getLists = new ArrayList<String>();
    // getLists.add("kinetic-law-constants-simplifier");
    Object[] objects = getLists.toArray();
    postAbs.setListData(objects);
    append.setEnabled(false);
    concentrations.setEnabled(false);
    genRuns.setEnabled(false);
    genStats.setEnabled(false);
    report.setEnabled(false);
    disableiSSASimulatorOptions();
  }

  /**
   * This method enables and disables the required fields for Monte Carlo
   * analysis.
   */
  private void enableMonteCarlo()
  {
    seed.setEnabled(true);
    seedLabel.setEnabled(true);
    runs.setEnabled(true);
    runsLabel.setEnabled(true);
    fileStem.setEnabled(true);
    fileStemLabel.setEnabled(true);
    minStepLabel.setEnabled(true);
    minStep.setEnabled(true);
    stepLabel.setEnabled(true);
    step.setEnabled(true);
    errorLabel.setEnabled(false);
    absErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    relErr.setEnabled(false);
    initialTimeLabel.setEnabled(true);
    initialTimeField.setEnabled(true);
    outputStartTimeLabel.setEnabled(true);
    outputStartTimeField.setEnabled(true);
    limitLabel.setEnabled(true);
    limit.setEnabled(true);
    intervalLabel.setEnabled(true);
    interval.setEnabled(true);
    simulators.setEnabled(true);
    simulatorsLabel.setEnabled(true);
    explanation.setEnabled(true);
    description.setEnabled(true);
    reactionAbstraction.setEnabled(true);
    stateAbstraction.setEnabled(true);
    simulators.removeAllItems();
    simulators.addItem("SSA-Direct (Dynamic)");
    simulators.addItem("SSA-CR (Dynamic)");
    simulators.addItem("SSA-Direct (Hierarchical)");
    simulators.addItem("SSA-Direct (Flatten)");
    simulators.addItem("Hybrid-Hierarchical");

    simulators.addItem("Mixed-Hierarchical");
    simulators.setSelectedItem("SSA-Direct (Dynamic)");
    if (Gui.isReb2sacFound())
    {
      simulators.addItem("gillespie");
      simulators.addItem("iSSA");
      simulators.addItem("interactive");
      simulators.addItem("emc-sim");
      simulators.addItem("bunker");
      simulators.addItem("nmc");
      simulators.setSelectedItem("gillespie");
    }
    if (!stateAbstraction.isSelected())
    {
      ArrayList<String> getLists = new ArrayList<String>();
      getLists.add("distribute-transformer");
      getLists.add("reversible-to-irreversible-transformer");
      getLists.add("kinetic-law-constants-simplifier");
      Object[] objects = getLists.toArray();
      postAbs.setListData(objects);
    }
    append.setEnabled(true);
    concentrations.setEnabled(true);
    genRuns.setEnabled(true);
    genStats.setEnabled(true);
    report.setEnabled(true);
    setupToAppendRuns(false);
    disableiSSASimulatorOptions();
  }

  /**
   * This method enables and disables the required fields for ODE simulation.
   */
  private void enableODE()
  {
    seed.setEnabled(true);
    seedLabel.setEnabled(true);
    runs.setEnabled(true);
    runsLabel.setEnabled(true);
    fileStem.setEnabled(true);
    fileStemLabel.setEnabled(true);
    minStepLabel.setEnabled(true);
    minStep.setEnabled(true);
    stepLabel.setEnabled(true);
    step.setEnabled(true);
    errorLabel.setEnabled(false);
    absErr.setEnabled(false);
    relErrorLabel.setEnabled(false);
    relErr.setEnabled(false);
    initialTimeLabel.setEnabled(true);
    initialTimeField.setEnabled(true);
    outputStartTimeLabel.setEnabled(true);
    outputStartTimeField.setEnabled(true);
    limitLabel.setEnabled(true);
    limit.setEnabled(true);
    intervalLabel.setEnabled(true);
    interval.setEnabled(true);
    simulators.setEnabled(true);
    simulatorsLabel.setEnabled(true);
    explanation.setEnabled(true);
    description.setEnabled(true);
    reactionAbstraction.setEnabled(true);
    stateAbstraction.setEnabled(true);
    simulators.removeAllItems();
    simulators.addItem("Runge-Kutta-Fehlberg (Dynamic)");
    simulators.addItem("Runge-Kutta-Fehlberg (Flatten)");
    simulators.addItem("Runge-Kutta-Fehlberg (Hierarchical)");
    simulators.setSelectedItem("Runge-Kutta-Fehlberg (Dynamic)");
    if (Gui.isReb2sacFound())
    {
      simulators.addItem("euler");
      simulators.addItem("gear1");
      simulators.addItem("gear2");
      simulators.addItem("rk4imp");
      simulators.addItem("rk8pd");
      simulators.addItem("rkf45");
      simulators.setSelectedItem("rkf45");
    }
    ArrayList<String> getLists = new ArrayList<String>();
    getLists.add("kinetic-law-constants-simplifier");
    Object[] objects = getLists.toArray();
    postAbs.setListData(objects);
    append.setEnabled(true);
    concentrations.setEnabled(true);
    genRuns.setEnabled(true);
    genStats.setEnabled(true);
    report.setEnabled(true);
    disableiSSASimulatorOptions();
  }

  /**
   * This method enables and disables the required fields for N-ary analysis.
   */
  private void enableStateAbstraction()
  {
    ODE.setEnabled(false);
    fba.setEnabled(false);
    monteCarlo.setEnabled(true);
    markov.setEnabled(true);
    // reactionAbstraction.setEnabled(true);
    // stateAbstraction.setEnabled(true);
    enableAbstractionOptions(true, false);
    if (ODE.isSelected())
    {
      monteCarlo.setSelected(true);
      enableMonteCarlo();
    }
    ArrayList<String> getLists = new ArrayList<String>();
    getLists.add("complex-formation-and-sequestering-abstraction");
    getLists.add("operator-site-reduction-abstraction");
    Object[] objects = getLists.toArray();
    preAbs.setListData(objects);
    getLists = new ArrayList<String>();
    objects = getLists.toArray();
    loopAbs.setListData(objects);
    getLists = new ArrayList<String>();
    objects = getLists.toArray();
    postAbs.setListData(objects);
    if (!sbml.isSelected() && !xhtml.isSelected() && !dot.isSelected() && !fba.isSelected())
    {
      append.setEnabled(true);
      concentrations.setEnabled(true);
      genRuns.setEnabled(true);
      genStats.setEnabled(true);
      report.setEnabled(true);
      setupToAppendRuns(false);
    }
  }

  public String getRootPath()
  {
    return root;
  }

  /**
   * Invoked when the mouse is double clicked in the interesting species
   * JLists or termination conditions JLists. Adds or removes the selected
   * interesting species or termination conditions.
   */
  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

  /**
   * This method currently does nothing.
   */
  @Override
  public void mousePressed(MouseEvent e)
  {
  }

  /**
   * This method currently does nothing.
   */
  @Override
  public void mouseReleased(MouseEvent e)
  {
  }

  /**
   * This method currently does nothing.
   */
  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  /**
   * This method currently does nothing.
   */
  @Override
  public void mouseExited(MouseEvent e)
  {
  }
}
