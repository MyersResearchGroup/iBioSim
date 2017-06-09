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
import java.util.List;
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

import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.util.SEDMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
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
import edu.utah.ece.async.lema.verification.lpn.properties.AbstractionProperty;

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

  /*
   * Simulation Options
   */
  private JTextField modelFileField ;
  private JRadioButton		noAbstraction, expandReactions, reactionAbstraction, stateAbstraction;
  private JRadioButton		ODE, monteCarlo, markov, fba, sbml, dot, xhtml;

  private JTextField fileStem;
  private JLabel fileStemLabel;

  private JTextField			initialTimeField, outputStartTimeField, limit, minStep, step, relErr, absErr, seed, runs;
  private JLabel          initialTimeLabel, outputStartTimeLabel, limitLabel, minStepLabel, stepLabel, relErrorLabel, errorLabel, seedLabel, runsLabel;

  private JTextField interval;
  private JComboBox			intervalLabel;

  // Description of simulator method
  private JLabel				description, explanation;

  private JComboBox			simulators;																	
  private JLabel				simulatorsLabel;																		


  // Report options
  private JLabel        report;
  private JCheckBox     append, concentrations, genRuns, genStats;

  /*
   * Advanced methods
   */

  // iSSA
  private JRadioButton		mpde, meanPath, medianPath;
  private JRadioButton		adaptive, nonAdaptive;
  private JComboBox     bifurcation;
  private JLabel iSSATypeLabel, iSSAAdaptiveLabel, bifurcationLabel;

  // Abstraction
  private JPanel        advanced;
  private JList       preAbs, loopAbs, postAbs;
  private JLabel        preAbsLabel, loopAbsLabel, postAbsLabel;
  private JButton       addPreAbs, rmPreAbs, editPreAbs;
  private JButton       addLoopAbs, rmLoopAbs, editLoopAbs;
  private JButton       addPostAbs, rmPostAbs, editPostAbs;

  private JTextField      rapid1, rapid2, qssa, maxCon, diffStoichAmp;  
  private JLabel				rapidLabel1, rapidLabel2, qssaLabel, maxConLabel, diffStoichAmpLabel;																												// sbml

  private JComboBox     transientProperties, subTaskList;

  private final Gui			gui;																														// reference																											// simulation

  private final Log			log;																														// the

  private final JTabbedPane	simTab;																													// the

  private ModelEditor			modelEditor;																												// model


  private final Pattern		stemPat				= Pattern.compile("([a-zA-Z]|[0-9]|_)*");

  private final AnalysisProperties properties;
  private final Preferences biosimrc = Preferences.userRoot();

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
   * @param absProperty
   *            - the abstraction panel
   * @param root
   *            - the project root directory path
   * @param simName
   *            - the name of the analysis view
   * @param modelFile
   *            - the SBML model file
   */
  public AnalysisView(Gui gui, Log log, JTabbedPane simTab, AbstractionPanel  abstractionPanel, String root, String simName, String modelFile)
  {

    super(new BorderLayout());

    this.properties = new AnalysisProperties(simName, modelFile, root, abstractionPanel == null);

    this.gui = gui;
    this.log = log;
    this.simTab = simTab;
    createMainView(simName, modelFile);
  }

  private void createMainView(String simName, String modelFile)
  {
    JPanel modelFilePanel = new JPanel();
    fileStemLabel = new JLabel("Task ID:");
    JTextField taskId = new JTextField(simName);
    taskId.setEditable(false);
    fileStem = new JTextField("", 15);
    subTaskList = new JComboBox();
    subTaskList.addItem("(none)");


    //loadPropertiesFile(simName, modelFile.replace(".xml", ""));
    //loadSEDML("");


    JLabel modelFileLabel = new JLabel("Model File:");

    modelFileField = new JTextField(modelFile);
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
    subTaskList.addActionListener(this);
  }

  /* Creates the abstraction radio button options */
  private JPanel createAbstractionOptions()
  {
    String modelFile = properties.getFilename();
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
    if (!Executables.reb2sacFound)
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
        //selectedMarkovSim = biosimrc.get("biosim.sim.sim", "");
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
    Preferences biosimrc = Preferences.userRoot();
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
    if (properties.getFilename().contains(".lpn"))
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
    if (properties.getFilename().contains(".lpn"))
    {
      JLabel prop = new JLabel("Property:");
      String[] props = new String[] { "none" };
      LPN lpn = new LPN();
      try {
        lpn.load(properties.getRoot() + GlobalConstants.separator + properties.getFilename());
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

  private void createAdvancedOptionsTab()
  {
    Preferences biosimrc = Preferences.userRoot();
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


  @Override
  public void run() {

  }

  public void run(String direct, boolean refresh) {
//    try
//    {
      if (sbml.isSelected())
      {
        //      File f = new File(root + GlobalConstants.separator + sbmlName);
        //      if (f.exists())
        //      {
        //        Object[] options = { "Overwrite", "Cancel" };
        //        int value = JOptionPane.showOptionDialog(component, "File already exists." + "\nDo you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        //        if (value == JOptionPane.YES_OPTION)
        //        {
        //          File dir = new File(root + GlobalConstants.separator + sbmlName);
        //          if (dir.isDirectory())
        //          {
        //            gui.deleteDir(dir);
        //          }
        //          else
        //          {
        //            System.gc();
        //            dir.delete();
        //          }
        //        }
        //        else
        //        {
        //          new File(directory + GlobalConstants.separator + "running").delete();
        //          logFile.close();
        //          return 0;
        //        }
        //      }
//        if (sbmlName != null && !sbmlName.trim().equals(""))
//        {
//          if (!gui.updateOpenModelEditor(sbmlName))
//          {
//            try
//            {
//              ModelEditor gcm = new ModelEditor(root + GlobalConstants.separator, sbmlName, gui, log, false, null, null, null, false, false);
//              gui.addTab(sbmlName, gcm, "Model Editor");
//              gui.addToTree(sbmlName);
//            }
//            catch (Exception e)
//            {
//              e.printStackTrace();
//            }
//          }
//          else
//          {
//            gui.getTab().setSelectedIndex(gui.getTab(sbmlName));
//          }
//          gui.enableTabMenu(gui.getTab().getSelectedIndex());
//        }
      }
      else if (ODE.isSelected())
      {
        updateTSDGraph();
      }
      else if (monteCarlo.isSelected())
      {
        updateTSDGraph();
      }

      //      new File(directory + GlobalConstants.separator + "running").delete();
      //      logFile.close();
//    }
//    catch (InterruptedException e1)
//    {
//      JOptionPane.showMessageDialog(Gui.frame, "Error In Execution!", "Error In Execution", JOptionPane.ERROR_MESSAGE);
//      e1.printStackTrace();
//    }
//    catch (IOException e1)
//    {
//      JOptionPane.showMessageDialog(Gui.frame, "File I/O Error!", "File I/O Error", JOptionPane.ERROR_MESSAGE);
//      e1.printStackTrace();
//    }
//    catch (XMLStreamException e) {
//      JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error Checking File", JOptionPane.ERROR_MESSAGE);
//      e.printStackTrace();
//    }

  }

  public void run(ArrayList<AnalysisThread> threads, ArrayList<String> dirs, ArrayList<String> levelOne, String stem)
  {
    //    for (AnalysisThread thread : threads)
    //    {
    //      try
    //      {
    //        thread.join();
    //      }
    //      catch (InterruptedException e)
    //      {
    //      }
    //    }
    //    if (!dirs.isEmpty() && new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + stem + dirs.get(0) + GlobalConstants.separator + "sim-rep.txt").exists())
    //    {
    //      ArrayList<String> dataLabels = new ArrayList<String>();
    //      ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
    //      String spec = dirs.get(0).split("=")[0];
    //      dataLabels.add(spec);
    //      data.add(new ArrayList<Double>());
    //      for (String prefix : levelOne)
    //      {
    //        double val = Double.parseDouble(prefix.split("=")[1].split("_")[0]);
    //        data.get(0).add(val);
    //        for (String d : dirs)
    //        {
    //          if (d.startsWith(prefix))
    //          {
    //            String suffix = d.replace(prefix, "");
    //            ArrayList<String> vals = new ArrayList<String>();
    //            try
    //            {
    //              Scanner s = new Scanner(new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + stem + d + GlobalConstants.separator + "sim-rep.txt"));
    //              while (s.hasNextLine())
    //              {
    //                String[] ss = s.nextLine().split(" ");
    //                if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
    //                {
    //                }
    //                if (vals.size() == 0)
    //                {
    //                  for (String add : ss)
    //                  {
    //                    vals.add(add + suffix);
    //                  }
    //                }
    //                else
    //                {
    //                  for (int i = 0; i < ss.length; i++)
    //                  {
    //                    vals.set(i, vals.get(i) + " " + ss[i]);
    //                  }
    //                }
    //              }
    //              s.close();
    //            }
    //            catch (Exception e)
    //            {
    //              e.printStackTrace();
    //            }
    //            double total = 0;
    //            int i = 0;
    //            if (vals.get(0).split(" ")[0].startsWith("#total"))
    //            {
    //              total = Double.parseDouble(vals.get(0).split(" ")[1]);
    //              i = 1;
    //            }
    //            for (; i < vals.size(); i++)
    //            {
    //              int index;
    //              if (dataLabels.contains(vals.get(i).split(" ")[0]))
    //              {
    //                index = dataLabels.indexOf(vals.get(i).split(" ")[0]);
    //              }
    //              else
    //              {
    //                dataLabels.add(vals.get(i).split(" ")[0]);
    //                data.add(new ArrayList<Double>());
    //                index = dataLabels.size() - 1;
    //              }
    //              if (total == 0)
    //              {
    //                data.get(index).add(Double.parseDouble(vals.get(i).split(" ")[1]));
    //              }
    //              else
    //              {
    //                data.get(index).add(100 * ((Double.parseDouble(vals.get(i).split(" ")[1])) / total));
    //              }
    //            }
    //          }
    //        }
    //      }
    //      DataParser constData = new DataParser(dataLabels, data);
    //      constData.outputTSD(root + GlobalConstants.separator + simName + GlobalConstants.separator + "sim-rep.tsd");
    //      for (int i = 0; i < simTab.getComponentCount(); i++)
    //      {
    //        if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
    //        {
    //          if (simTab.getComponentAt(i) instanceof Graph)
    //          {
    //            ((Graph) simTab.getComponentAt(i)).refresh();
    //          }
    //        }
    //      }
    //    }
  }


  private void updateTSDGraph()
  {
    //    for (int i = 0; i < simTab.getComponentCount(); i++)
    //    {
    //      if (simTab.getComponentAt(i).getName().equals("TSD Graph"))
    //      {
    //        if (simTab.getComponentAt(i) instanceof Graph)
    //        {
    //          boolean outputM = true;
    //          boolean outputV = true;
    //          boolean outputS = true;
    //          boolean outputTerm = false;
    //          boolean warning = false;
    //          ArrayList<String> run = new ArrayList<String>();
    //          for (String f : work.list())
    //          {
    //            if (f.contains("mean"))
    //            {
    //              outputM = false;
    //            }
    //            else if (f.contains("variance"))
    //            {
    //              outputV = false;
    //            }
    //            else if (f.contains("standard_deviation"))
    //            {
    //              outputS = false;
    //            }
    //            if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
    //            {
    //              run.add(f);
    //            }
    //            else if (f.equals("term-time.txt"))
    //            {
    //              outputTerm = true;
    //            }
    //          }
    //          if (genStats && (outputM || outputV || outputS))
    //          {
    //            warning = ((Graph) simTab.getComponentAt(i)).getWarning();
    //            ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
    //          }
    //          new File(directory + GlobalConstants.separator + "running").delete();
    //          logFile.close();
    //          if (outputTerm)
    //          {
    //            ArrayList<String> dataLabels = new ArrayList<String>();
    //            dataLabels.add("time");
    //            ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
    //            if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
    //            {
    //              try
    //              {
    //                Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
    //                if (s.hasNextLine())
    //                {
    //                  String[] ss = s.nextLine().split(" ");
    //                  if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
    //                  {
    //                  }
    //                  else
    //                  {
    //                    for (String add : ss)
    //                    {
    //                      if (!add.equals("#total") && !add.equals("time-limit"))
    //                      {
    //                        dataLabels.add(add);
    //                        ArrayList<Double> times = new ArrayList<Double>();
    //                        terms.add(times);
    //                      }
    //                    }
    //                  }
    //                }
    //                s.close();
    //              }
    //              catch (Exception e)
    //              {
    //                e.printStackTrace();
    //              }
    //            }
    //            Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
    //            while (scan.hasNextLine())
    //            {
    //              String line = scan.nextLine();
    //              String[] term = line.split(" ");
    //              if (!dataLabels.contains(term[0]))
    //              {
    //                dataLabels.add(term[0]);
    //                ArrayList<Double> times = new ArrayList<Double>();
    //                times.add(Double.parseDouble(term[1]));
    //                terms.add(times);
    //              }
    //              else
    //              {
    //                terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
    //              }
    //            }
    //            scan.close();
    //            ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
    //            ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
    //            for (int j = 0; j < dataLabels.size(); j++)
    //            {
    //              ArrayList<Double> temp = new ArrayList<Double>();
    //              temp.add(0.0);
    //              data.add(temp);
    //              temp = new ArrayList<Double>();
    //              temp.add(0.0);
    //              percentData.add(temp);
    //            }
    //            for (double j = printInterval; j <= timeLimit; j += printInterval)
    //            {
    //              data.get(0).add(j);
    //              percentData.get(0).add(j);
    //              for (int k = 1; k < dataLabels.size(); k++)
    //              {
    //                data.get(k).add(data.get(k).get(data.get(k).size() - 1));
    //                percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
    //                for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
    //                {
    //                  if (terms.get(k - 1).get(l) < j)
    //                  {
    //                    data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
    //                    percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
    //                    terms.get(k - 1).remove(l);
    //                  }
    //                }
    //              }
    //            }
    //            DataParser probData = new DataParser(dataLabels, data);
    //            probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
    //            probData = new DataParser(dataLabels, percentData);
    //            probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
    //          }
    //          if (refresh)
    //          {
    //            ((Graph) simTab.getComponentAt(i)).refresh();
    //          }
    //        }
    //        else
    //        {
    //          simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, true, false));
    //          boolean outputM = true;
    //          boolean outputV = true;
    //          boolean outputS = true;
    //          boolean outputTerm = false;
    //          boolean warning = false;
    //          ArrayList<String> run = new ArrayList<String>();
    //          for (String f : work.list())
    //          {
    //            if (f.contains("mean"))
    //            {
    //              outputM = false;
    //            }
    //            else if (f.contains("variance"))
    //            {
    //              outputV = false;
    //            }
    //            else if (f.contains("standard_deviation"))
    //            {
    //              outputS = false;
    //            }
    //            if (f.contains("run-") && f.endsWith("." + printer_id.substring(0, printer_id.length() - 8)))
    //            {
    //              run.add(f);
    //            }
    //            else if (f.equals("term-time.txt"))
    //            {
    //              outputTerm = true;
    //            }
    //          }
    //          if (genStats && (outputM || outputV || outputS))
    //          {
    //            warning = ((Graph) simTab.getComponentAt(i)).getWarning();
    //            ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, direct, warning, true);
    //          }
    //          new File(directory + GlobalConstants.separator + "running").delete();
    //          logFile.close();
    //          if (outputTerm)
    //          {
    //            ArrayList<String> dataLabels = new ArrayList<String>();
    //            dataLabels.add("time");
    //            ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
    //            if (new File(directory + GlobalConstants.separator + "sim-rep.txt").exists())
    //            {
    //              try
    //              {
    //                Scanner s = new Scanner(new File(directory + GlobalConstants.separator + "sim-rep.txt"));
    //                if (s.hasNextLine())
    //                {
    //                  String[] ss = s.nextLine().split(" ");
    //                  if (ss[0].equals("The") && ss[1].equals("total") && ss[2].equals("termination") && ss[3].equals("count:") && ss[4].equals("0"))
    //                  {
    //                  }
    //                  else
    //                  {
    //                    for (String add : ss)
    //                    {
    //                      if (!add.equals("#total") && !add.equals("time-limit"))
    //                      {
    //                        dataLabels.add(add);
    //                        ArrayList<Double> times = new ArrayList<Double>();
    //                        terms.add(times);
    //                      }
    //                    }
    //                  }
    //                }
    //                s.close();
    //              }
    //              catch (Exception e)
    //              {
    //                e.printStackTrace();
    //              }
    //            }
    //            Scanner scan = new Scanner(new File(directory + GlobalConstants.separator + "term-time.txt"));
    //            while (scan.hasNextLine())
    //            {
    //              String line = scan.nextLine();
    //              String[] term = line.split(" ");
    //              if (!dataLabels.contains(term[0]))
    //              {
    //                dataLabels.add(term[0]);
    //                ArrayList<Double> times = new ArrayList<Double>();
    //                times.add(Double.parseDouble(term[1]));
    //                terms.add(times);
    //              }
    //              else
    //              {
    //                terms.get(dataLabels.indexOf(term[0]) - 1).add(Double.parseDouble(term[1]));
    //              }
    //            }
    //            scan.close();
    //            ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
    //            ArrayList<ArrayList<Double>> percentData = new ArrayList<ArrayList<Double>>();
    //            for (int j = 0; j < dataLabels.size(); j++)
    //            {
    //              ArrayList<Double> temp = new ArrayList<Double>();
    //              temp.add(0.0);
    //              data.add(temp);
    //              temp = new ArrayList<Double>();
    //              temp.add(0.0);
    //              percentData.add(temp);
    //            }
    //            for (double j = printInterval; j <= timeLimit; j += printInterval)
    //            {
    //              data.get(0).add(j);
    //              percentData.get(0).add(j);
    //              for (int k = 1; k < dataLabels.size(); k++)
    //              {
    //                data.get(k).add(data.get(k).get(data.get(k).size() - 1));
    //                percentData.get(k).add(percentData.get(k).get(percentData.get(k).size() - 1));
    //                for (int l = terms.get(k - 1).size() - 1; l >= 0; l--)
    //                {
    //                  if (terms.get(k - 1).get(l) < j)
    //                  {
    //                    data.get(k).set(data.get(k).size() - 1, data.get(k).get(data.get(k).size() - 1) + 1);
    //                    percentData.get(k).set(percentData.get(k).size() - 1, ((data.get(k).get(data.get(k).size() - 1)) * 100) / runs);
    //                    terms.get(k - 1).remove(l);
    //                  }
    //                }
    //              }
    //            }
    //            DataParser probData = new DataParser(dataLabels, data);
    //            probData.outputTSD(directory + GlobalConstants.separator + "term-time.tsd");
    //            probData = new DataParser(dataLabels, percentData);
    //            probData.outputTSD(directory + GlobalConstants.separator + "percent-term-time.tsd");
    //          }
    //          simTab.getComponentAt(i).setName("TSD Graph");
    //        }
    //      }
    //      if (refresh)
    //      {
    //        if (simTab.getComponentAt(i).getName().equals("Histogram"))
    //        {
    //          if (simTab.getComponentAt(i) instanceof Graph)
    //          {
    //            ((Graph) simTab.getComponentAt(i)).refresh();
    //          }
    //          else
    //          {
    //            if (new File(filename.substring(0, filename.length() - filename.split("/")[filename.split("/").length - 1].length()) + "sim-rep.txt").exists())
    //            {
    //              simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
    //              simTab.getComponentAt(i).setName("Histogram");
    //            }
    //          }
    //        }
    //      }
    //    }
  }
  /**
   * adds a string with the species ID and its threshold values to the
   * arraylist of interesting species
   * 
   * @param speciesAndThresholds
   */
  public void addInterestingSpecies(String speciesAndThresholds)
  {
    List<String> interestingSpecies = properties.getSimulationProperties().getIntSpecies();
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
    List<String> interestingSpecies = properties.getSimulationProperties().getIntSpecies();
    for (int i = 0; i < interestingSpecies.size(); i++)
    {
      if (interestingSpecies.get(i).split(" ")[0].equals(species))
      {
        interestingSpecies.remove(i);
        return;
      }
    }
  }

  public String getSimName()
  {
    return properties.getSim();
  }

  public List<String> getInterestingSpecies()
  {
    return properties.getSimulationProperties().getIntSpecies();
  }

  public String getRootPath()
  {
    return properties.getRoot();
  }

  public String getSimID()
  {
    return fileStem.getText().trim();
  }

  public String getSimPath()
  {
    String simName = properties.getId();
    String root = properties.getRoot();
    if (!fileStem.getText().trim().equals(""))
    {
      return root + GlobalConstants.separator + simName + GlobalConstants.separator + fileStem.getText().trim();
    }
    return root + GlobalConstants.separator + simName;
  }

  public boolean noExpand()
  {
    return noAbstraction.isSelected();
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

  public int getNumRuns() {
    try {
      return Integer.parseInt(runs.getText());
    } catch (Exception e) {
      return 1;
    }
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

  public int getStartIndex(String outDir) {
    String root = properties.getRoot();
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

  public void executeRun()
  {
    /*
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
     */
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
      if (Executables.reb2sacFound)
      {
        simulators.addItem("atacs");
        simulators.addItem("ctmc-transient");
      }
      //      if (selectedMarkovSim != null)
      //      {
      //        simulators.setSelectedItem(selectedMarkovSim);
      //      }
    }
    //change = false;
  }


  public Graph createGraph(String open, boolean regularGraph)
  {
    String root = properties.getRoot();
    String simName = properties.getId();

    String outDir = root + GlobalConstants.separator + simName;
    String printer_id;
    printer_id = "tsd.printer";
    String printer_track_quantity = "amount";
    if (concentrations.isSelected())
    {
      printer_track_quantity = "concentration";
    }
    return new Graph(this, printer_track_quantity, simName + " simulation results", printer_id, outDir, "time", gui, open, log, null, regularGraph, false);
  }

  /**
   * Saves the simulate options.
   */
  public boolean save(String direct)
  {

    String sim = properties.getSim();
    try
    {
      double initialTime = Double.parseDouble(initialTimeField.getText().trim());
      if (initialTime < 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Initial Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }

      properties.getSimulationProperties().setInitialTime(initialTime);

    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Initial Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }


    try
    {
      double outputStartTime = Double.parseDouble(outputStartTimeField.getText().trim());
      if (outputStartTime < 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Output Start Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      properties.getSimulationProperties().setOutputStartTime(outputStartTime);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Output Start Time Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }


    try
    {
      double timeLimit = Double.parseDouble(limit.getText().trim());
      if (timeLimit <= 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Time Limit Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      } 
      properties.getSimulationProperties().setTimeLimit(timeLimit);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Time Limit Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }


    try
    {
      if (((String) intervalLabel.getSelectedItem()).contains("Print Interval"))
      {
        double printInterval = Double.parseDouble(interval.getText().trim());
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
        properties.getSimulationProperties().setPrintInterval(printInterval);
      }
      else
      {
        int printInterval = Integer.parseInt(interval.getText().trim());
        if (printInterval <= 0)
        {
          JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Number of Steps Field.", "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        }
        properties.getSimulationProperties().setNumSteps(printInterval);
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
      double minTimeStep = Double.parseDouble(minStep.getText().trim());

      if (minTimeStep < 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Minimum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }

      properties.getSimulationProperties().setMinTimeStep(minTimeStep);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Real Number into the Minimum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if (step.getText().trim().equals("inf") && !sim.equals("euler"))
    {
      double timeStep = Double.MAX_VALUE;
      properties.getSimulationProperties().setTimeStep(timeStep);
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
        double timeStep = Double.parseDouble(step.getText().trim());
        if (timeStep <= 0)
        {
          JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Positive Number into the Maximum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
          return false;
        } 
      }
      catch (Exception e1)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Real Number into the Maximum Time Step Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }

    try
    {
      double absError = Double.parseDouble(absErr.getText().trim());

      if (absError < 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Absolute Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }

      properties.getSimulationProperties().setAbsError(absError);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Absolute Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try
    {
      double relError = Double.parseDouble(relErr.getText().trim());

      if (relError < 0)
      {
        JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Non-negative Number into the Relative Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      properties.getSimulationProperties().setRelError(relError);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The Relative Error Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }


    try
    {
      long rndSeed = Long.parseLong(seed.getText().trim());
      properties.getSimulationProperties().setRndSeed(rndSeed);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter an Integer into the Random Seed Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    try
    {
      int  run = Integer.parseInt(runs.getText().trim());
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
      properties.getSimulationProperties().setPrinter_id("null.printer");
    }
    else
    {
      properties.getSimulationProperties().setPrinter_id("tsd.printer");
    }
    if (concentrations.isSelected())
    {
      properties.getSimulationProperties().setPrinter_track_quantity("concentration");
    }
    else
    {
      properties.getSimulationProperties().setPrinter_track_quantity("amount");
    }
    if (genStats.isSelected())
    {
      properties.getSimulationProperties().setGenStats("true");
    }
    else
    {
      properties.getSimulationProperties().setGenStats("false");
    }

    try
    {
      double rap1 = Double.parseDouble(rapid1.getText().trim());
      properties.getAdvancedProperties().setRap1(rap1);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The" + " Rapid Equilibrium Condition 1 Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try
    {
      double rap2 = Double.parseDouble(rapid2.getText().trim());
      properties.getAdvancedProperties().setRap2(rap2);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The" + " Rapid Equilibrium Condition 2 Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try
    {
      double qss = Double.parseDouble(qssa.getText().trim());
      properties.getAdvancedProperties().setQss(qss);
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter A Real Number Into The QSSA Condition Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    try
    {
      int con = Integer.parseInt(maxCon.getText().trim());
      properties.getAdvancedProperties().setCon(con);
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
        double stoichAmp = Double.parseDouble(diffStoichAmp.getText().trim());
        properties.getAdvancedProperties().setStoichAmp(stoichAmp);
      }
      else
      {
        properties.getAdvancedProperties().setStoichAmp(1);
      }
    }
    catch (Exception e1)
    {
      JOptionPane.showMessageDialog(Gui.frame, "Must Enter a Double Into the Stoich." + " Amp. Field.", "Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    if (noAbstraction.isSelected() && ODE.isSelected())
    {
      properties.setNone(true);
    }
    else if (expandReactions.isSelected())
    {
      properties.setExpand(true);      
    }
    else if (reactionAbstraction.isSelected())
    {
      properties.setAbs(true);
    }
    else if (stateAbstraction.isSelected())
    {
      properties.setNary(true);
    }
    if(ODE.isSelected())
    {
      properties.setOde();
    }
    else if(monteCarlo.isSelected())
    {
      properties.setSsa();
    }
    else if(markov.isSelected())
    {
      properties.setMarkov();
    }
    else if(sbml.isSelected())
    {
      properties.setSbml();
    }
    else if(dot.isSelected())
    {
      properties.setDot();
    }
    else if(fba.isSelected())
    {
      properties.setFba();
    }
    else if(xhtml.isSelected())
    {
      properties.setXhtml();
    }
    // Run runProgram = new Run(this);
    //    int cut = 0;
    //    sbmlProp = sbmlProp.replace("\\", "/");
    //    String[] getFilename = sbmlProp.split("/");
    //    for (int i = 0; i < getFilename[getFilename.length - 1].length(); i++)
    //    {
    //      if (getFilename[getFilename.length - 1].charAt(i) == '.')
    //      {
    //        cut = i;
    //      }
    //    }
    //    boolean saveTopLevel = false;
    //    if (!direct.equals("."))
    //    {
    //      simProp = simProp.substring(0, simProp.length() - simProp.split("/")[simProp.split("/").length - 1].length()) + direct + GlobalConstants.separator + simProp.substring(simProp.length() - simProp.split("/")[simProp.split("/").length - 1].length());
    //      saveTopLevel = true;
    //    }
    //    String propName = simProp.substring(0, simProp.length() - getFilename[getFilename.length - 1].length()) + getFilename[getFilename.length - 1].substring(0, cut) + ".properties";
    //    log.addText("Creating properties file:\n" + propName + "\n");
    //    int numPaths = Integer.parseInt((String) (bifurcation.getSelectedItem()));
    //    Run.createProperties(initialTime, outputStartTime, timeLimit, ((String) (intervalLabel.getSelectedItem())), printInterval, minTimeStep, timeStep, absError, relError, ".", rndSeed,
    //      run, numPaths, intSpecies, printer_id, printer_track_quantity, generate_statistics, sbmlProp.split("/"), selectedButtons, this,
    //      sbmlProp, rap1, rap2, qss, con, stoichAmp, preAbs, loopAbs, postAbs, lpnAbstraction, mpde.isSelected(), meanPath.isSelected(),
    //      adaptive.isSelected());
    //    if (direct.equals("."))
    //    {
    //      outDir = simName;
    //    }
    //    else
    //    {
    //      outDir = simName + GlobalConstants.separator + direct;
    //    }
    //    if (!runs.isEnabled())
    //    {
    //      for (String runs : new File(root + GlobalConstants.separator + outDir).list())
    //      {
    //        if (runs.length() >= 4)
    //        {
    //          String end = "";
    //          for (int j = 1; j < 5; j++)
    //          {
    //            end = runs.charAt(runs.length() - j) + end;
    //          }
    //          if (end.equals(".tsd") || end.equals(".dat") || end.equals(".csv"))
    //          {
    //            if (runs.contains("run-"))
    //            {
    //              run = Math.max(run, Integer.parseInt(runs.substring(4, runs.length() - end.length())));
    //            }
    //          }
    //        }
    //      }
    //    }
    //    String topLevelProps = sbmlProp.substring(0, sbmlProp.length() - getFilename[getFilename.length - 1].length()) + getFilename[getFilename.length - 1].substring(0, cut) + ".properties";
    //    try
    //    {
    //      Properties getProps = new Properties();
    //      FileInputStream load = new FileInputStream(new File(topLevelProps));
    //      getProps.load(load);
    //      load.close();
    //      getProps.setProperty("selected.simulator", sim);
    //      if (transientProperties != null)
    //      {
    //        getProps.setProperty("selected.property", (String) transientProperties.getSelectedItem());
    //      }
    //      if (!fileStem.getText().trim().equals(""))
    //      {
    //        new File(root + GlobalConstants.separator + simName + GlobalConstants.separator + fileStem.getText().trim()).mkdir();
    //        getProps.setProperty("file.stem", fileStem.getText().trim());
    //      }
    //      if (monteCarlo.isSelected() || ODE.isSelected())
    //      {
    //        if (append.isSelected())
    //        {
    //          getProps.setProperty("monte.carlo.simulation.start.index", 
    //            getStartIndex(outDir) + "");
    //        }
    //        else
    //        {
    //          getProps.setProperty("monte.carlo.simulation.start.index", "1");
    //        }
    //      }
    //      FileOutputStream store = new FileOutputStream(new File(propName));
    //      getProps.store(store, getFilename[getFilename.length - 1].substring(0, cut) + " Properties");
    //      store.close();
    //      if (saveTopLevel)
    //      {
    //        store = new FileOutputStream(new File(topLevelProps));
    //        getProps.store(store, getFilename[getFilename.length - 1].substring(0, cut) + " Properties");
    //        store.close();
    //      }
    //      //saveSEDML();
    //    }
    //    catch (Exception e)
    //    {
    //      e.printStackTrace();
    //      JOptionPane.showMessageDialog(Gui.frame, "Unable to add properties to property file.", "Error", JOptionPane.ERROR_MESSAGE);
    //    }
    //    //change = false;
    return true;
  }

  public JPanel getAdvanced()
  {
    JPanel constructPanel = new JPanel(new BorderLayout());
    constructPanel.add(advanced, "Center");
    return constructPanel;
  }

  public void setSim(String newSimName)
  {
    //    sbmlFile = sbmlFile.replace("\\", "/");
    //    sbmlProp = root + GlobalConstants.separator + newSimName + GlobalConstants.separator + sbmlFile.split("/")[sbmlFile.split("/").length - 1];
    //    simName = newSimName;
  }

  public boolean hasChanged()
  {
    return false;
  }
  public void updateBackgroundFile(String updatedFile)
  {
    properties.setId(updatedFile);
    modelFileField.setText(updatedFile);
  }

  public String getBackgroundFile()
  {
    return properties.getId();
  }

  public void updateProperties()
  {
    //    if (transientProperties != null && modelFile.contains(".lpn"))
    //    {
    //      Object selected = transientProperties.getSelectedItem();
    //      String[] props = new String[] { "none" };
    //      LPN lpn = new LPN();
    //      try {
    //        lpn.load(root + GlobalConstants.separator + modelFile);
    //        String[] getProps = lpn.getProperties().toArray(new String[0]);
    //        props = new String[getProps.length + 1];
    //        props[0] = "none";
    //        for (int i = 0; i < getProps.length; i++)
    //        {
    //          props[i + 1] = getProps[i];
    //        }
    //        transientProperties.removeAllItems();
    //        for (String s : props)
    //        {
    //          transientProperties.addItem(s);
    //        }
    //        transientProperties.setSelectedItem(selected);
    //      } catch (BioSimException e) {
    //        JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), e.getTitle(), JOptionPane.ERROR_MESSAGE); 
    //        e.printStackTrace();
    //      }
    //      
    //    }
  }

  private void refreshHistogram()
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
          //TODO:simTab.setComponentAt(i, new Graph(analysisView, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
          simTab.getComponentAt(i).setName("Histogram");
        }
      }
    }
  }

  /**
   * This method enables and disables the required fields for none and
   * abstraction.
   */
  private void enableReactionAbstraction()
  {
    String modelFile = properties.getFilename();
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
    //    if (modelFile.contains(".lpn") || modelFile.contains(".s") || modelFile.contains(".inst"))
    //    {
    //      markov.setEnabled(true);
    //    }
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
   * This method performs different functions depending on what buttons are
   * pushed and what input fields contain data.
   */
  @Override
  public void actionPerformed(ActionEvent e)
  {
    //    change = true;
    if (e.getSource() == subTaskList) {
      String subTask = "";
      if (!((String)subTaskList.getSelectedItem()).equals("(none)")) {
        subTask = (String)subTaskList.getSelectedItem();
      }
      //saveSEDML();
      //loadSEDML(subTask);
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
      //      int cut = 0;
      //      sbmlProp = sbmlProp.replace("\\", "/");
      //      String[] getFilename = sbmlProp.split("/");
      //      for (int i = 0; i < getFilename[getFilename.length - 1].length(); i++)
      //      {
      //        if (getFilename[getFilename.length - 1].charAt(i) == '.')
      //        {
      //          cut = i;
      //        }
      //      }
      //      String propName = sbmlProp.substring(0, sbmlProp.length() - getFilename[getFilename.length - 1].length()) + getFilename[getFilename.length - 1].substring(0, cut) + ".properties";
      //      try
      //      {
      //        if (new File(propName).exists())
      //        {
      //          Properties getProps = new Properties();
      //          FileInputStream load = new FileInputStream(new File(propName));
      //          getProps.load(load);
      //          load.close();
      //          if (getProps.containsKey("monte.carlo.simulation.time.limit"))
      //          {
      //            initialTimeField.setText(getProps.getProperty("simulation.initial.time"));
      //            outputStartTimeField.setText(getProps.getProperty("simulation.output.start.time"));
      //            minStep.setText(getProps.getProperty("monte.carlo.simulation.min.time.step"));
      //            step.setText(getProps.getProperty("monte.carlo.simulation.time.step"));
      //            limit.setText(getProps.getProperty("monte.carlo.simulation.time.limit"));
      //            interval.setText(getProps.getProperty("monte.carlo.simulation.print.interval"));
      //          }
      //        }
      //      }
      //      catch (IOException e1)
      //      {
      //        JOptionPane.showMessageDialog(Gui.frame, "Unable to restore time limit and print interval.", "Error", JOptionPane.ERROR_MESSAGE);
      //      }
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
    String modelFile = properties.getFilename();
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
    if (Executables.reb2sacFound)
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
    if (Executables.reb2sacFound)
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
    if (Executables.reb2sacFound)
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
