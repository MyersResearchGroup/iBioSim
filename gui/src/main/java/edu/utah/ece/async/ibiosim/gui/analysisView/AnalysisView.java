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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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

import edu.utah.ece.async.ibiosim.analysis.Run;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisProperties;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisPropertiesLoader;
import edu.utah.ece.async.ibiosim.analysis.properties.AnalysisPropertiesWriter;
import edu.utah.ece.async.ibiosim.analysis.properties.PropertiesUtil;
import edu.utah.ece.async.ibiosim.analysis.properties.SimulationProperties;
import edu.utah.ece.async.ibiosim.analysis.util.SEDMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.Executables;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.Message;
import edu.utah.ece.async.ibiosim.dataModels.util.dataparser.DataParser;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.PanelObservable;
import edu.utah.ece.async.ibiosim.dataModels.util.observe.BioObservable.RequestType;
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
public class AnalysisView extends PanelObservable implements ActionListener, Runnable, MouseListener
{

  private static final long serialVersionUID  = 3181014495993143825L;

  /*
   * Simulation Options
   */
  private JTextField modelFileField ;
  private JRadioButton    noAbstraction, expandReactions, reactionAbstraction, stateAbstraction;
  private JRadioButton    ODE, monteCarlo, markov, fba, sbml, dot, xhtml;

  private JTextField fileStem;
  private JLabel fileStemLabel;

  private JTextField      initialTimeField, outputStartTimeField, limit, minStep, step, relErr, absErr, seed, runs;
  private JLabel          initialTimeLabel, outputStartTimeLabel, limitLabel, minStepLabel, stepLabel, relErrorLabel, errorLabel, seedLabel, runsLabel;

  private JTextField interval;
  private JComboBox<String>     intervalLabel;

  // Description of simulator method
  private JLabel        description, explanation;

  private JComboBox<String>     simulators;                                 
  private JLabel        simulatorsLabel;                                    


  // Report options
  private JLabel        report;
  private JCheckBox     append, concentrations, genRuns, genStats;

  /*
   * Advanced methods
   */

  // iSSA
  private JRadioButton    mpde, meanPath, medianPath;
  private JRadioButton    adaptive, nonAdaptive;
  private JComboBox<String>     bifurcation;
  private JLabel iSSATypeLabel, iSSAAdaptiveLabel, bifurcationLabel;

  // Abstraction
  private JPanel        advanced;
  private JList<String>       preAbs, loopAbs, postAbs;
  private JLabel        preAbsLabel, loopAbsLabel, postAbsLabel;
  private JButton       addPreAbs, rmPreAbs, editPreAbs;
  private JButton       addLoopAbs, rmLoopAbs, editLoopAbs;
  private JButton       addPostAbs, rmPostAbs, editPostAbs;

  private JTextField      rapid1, rapid2, qssa, maxCon, diffStoichAmp;  
  private JLabel        rapidLabel1, rapidLabel2, qssaLabel, maxConLabel, diffStoichAmpLabel;                                                       // sbml

  // Progress Ba
  private JProgressBar progress;
  private JButton progressCancel;
  private JLabel progressLabel;
  
  private JComboBox<String>     transientProperties, subTaskList;

  private final Gui     gui;                                                            // reference                                                      // simulation

  private final Log     log;                                                            // the

  private final JTabbedPane simTab;                                                         // the

  private ModelEditor     modelEditor;                                                        // model


  private final Pattern   stemPat       = Pattern.compile("([a-zA-Z]|[0-9]|_)*");

  private final AnalysisProperties properties;
  private final Preferences biosimrc = Preferences.userRoot();

  private final SEDMLDocument SedMLDoc; 

  private boolean change;
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
  public AnalysisView(Gui gui, Log log, JTabbedPane simTab, AbstractionPanel  abstractionPanel, SEDMLDocument SedMLDoc, String root, String simName, String modelFile)
  {

    super(new BorderLayout());

    this.properties = new AnalysisProperties(simName, modelFile, root, abstractionPanel == null);

    this.gui = gui;
    this.log = log;
    this.simTab = simTab;
    this.SedMLDoc = SedMLDoc;

    String  sbmlFile = root + File.separator + modelFile;
    if(abstractionPanel != null)
    {
      this.properties.getVerificationProperties().setAbsProperty(abstractionPanel.getAbstractionProperty());  
    }
    

    if (modelFile.endsWith(".lpn"))
    {
      sbmlFile = root + File.separator + simName + File.separator + modelFile.replace(".lpn", ".xml");
    }

    //    String sbmlProp = root + File.separator + simName + File.separator + modelFile.replace(".lpn", ".xml");
    //    interestingSpecies = new ArrayList<String>();
    //    change = false;

    createMainView(simName, modelFile);
  }

  private void createMainView(String simName, String modelFile)
  {
    JPanel modelFilePanel = new JPanel();
    fileStemLabel = new JLabel("Task ID:");
    JTextField taskId = new JTextField(simName);
    taskId.setEditable(false);
    fileStem = new JTextField("", 15);
    subTaskList = new JComboBox<String>();
    subTaskList.addItem("(none)");




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
    

    try {
      AnalysisPropertiesLoader.loadPropertiesFile(properties);
      AnalysisPropertiesLoader.loadSEDML(SedMLDoc, "", properties);
      loadPropertiesFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /* Creates the abstraction radio button options */
  private JPanel createAbstractionOptions()
  {
    String modelFile = properties.getModelFile();
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
    }
    else if (biosimrc.get("biosim.sim.type", "").equals("Network"))
    {
      dot.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
    }
    else
    {
      xhtml.doClick();
      simulators.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
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
    simulators = new JComboBox<String>();
    // simulators.setSelectedItem("rkf45");
    simulators.addActionListener(this);
    initialTimeLabel = new JLabel("Initial Time:");
    initialTimeField = new JTextField(biosimrc.get("biosim.sim.initial.time", ""), 15);
    outputStartTimeLabel = new JLabel("Output Start Time:");
    outputStartTimeField = new JTextField(biosimrc.get("biosim.sim.output.start.time", ""), 15);
    limitLabel = new JLabel("Time Limit:");
    limit = new JTextField(biosimrc.get("biosim.sim.limit", ""), 15);
    String[] intervalChoices = { "Print Interval", "Minimum Print Interval", "Number Of Steps" };
    intervalLabel = new JComboBox<String>(intervalChoices);
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
    String modelFile = properties.getModelFile();
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
        lpn.load(properties.getRoot() + File.separator + properties.getModelFile());

        String[] getProps = lpn.getProperties().toArray(new String[0]);
        props = new String[getProps.length + 1];
        props[0] = "none";
        for (int i = 0; i < getProps.length; i++)
        {
          props[i + 1] = getProps[i];
        }
        transientProperties = new JComboBox<String>(props);
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
    preAbs = new JList<String>();
    loopAbs = new JList<String>();
    postAbs = new JList<String>();
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

    bifurcation = new JComboBox<String>(options);
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

  /**
   * If the run button is pressed, this method starts a new thread for the
   * simulation.
   * 
   * @param refresh
   * @throws BioSimException 
   * @throws InterruptedException 
   * @throws IOException 
   * @throws XMLStreamException
   * @throws NumberFormatException
   */
  public void run(boolean refresh)
  {
    if (!save())
    {
      return;
    }
    String root = properties.getRoot();
    String outDir = properties.getDirectory();
    String simName = properties.getSim();

    if (monteCarlo.isSelected() || ODE.isSelected())
    {
      if (append.isSelected())
      {
        String[] searchForRunFiles = new File(outDir).list();
        for (String s : searchForRunFiles)
        {
          if (s.length() > 3 && new File(outDir + File.separator + s).isFile() && (s.equals("mean.tsd") || s.equals("standard_deviation.tsd") || s.equals("variance.tsd")))
          {
            new File(root + File.separator + outDir + File.separator + s).delete();
          }
        }
      }
      else
      {
        String[] searchForRunFiles = new File(outDir).list();
        for (String s : searchForRunFiles)
        {
          if (s.length() > 3 && s.substring(0, 4).equals("run-") && new File(outDir + File.separator + s).isFile())
          {
            new File(outDir + File.separator + s).delete();
          }
        }
      }
    }
    progress = new JProgressBar(0, 100);
    progressCancel = new JButton("Cancel");
    progressLabel = new JLabel("Running " + simName);
    
    JFrame running = createProgressBar(progressLabel, progress, progressCancel);
    Run runProgram = new Run(properties);
    runProgram.addObservable(this);
    progressCancel.addActionListener(runProgram);
    gui.getExitButton().addActionListener(runProgram);
    if (monteCarlo.isSelected() || ODE.isSelected())
    {
      File[] files = new File(outDir).listFiles();
      for (File f : files)
      {
        if (f.getName().contains("mean.") || f.getName().contains("standard_deviation.") || f.getName().contains("variance."))
        {
          f.delete();
        }
      }
      updateTSDGraph(refresh);
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
    try {
      exit = runProgram.execute();
      if (stateAbstraction.isSelected() && modelEditor == null && !simName.contains("markov-chain-analysis") && exit == 0)
      {
        //      simProp = simProp.replace("\\", "/");
        //      Nary_Run nary_Run = new Nary_Run(this, simulators, simProp.split("/"), simProp, fba, sbml, dot, xhtml, stateAbstraction, ODE, monteCarlo,
        //        initialTime, outputStartTime, timeLimit, ((String) (intervalLabel.getSelectedItem())), printInterval, minTimeStep, timeStep, root + File.separator + simName,
        //        rndSeed, run, printer_id, printer_track_quantity, intSpecies, rap1, rap2, qss, con, log, gui, simTab, root, directory, modelFile,
        //        reactionAbstraction, lpnAbstraction, absError, relError);
        // nary_Run.open();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (XMLStreamException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (BioSimException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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
    
    for(int i = 0; i < simTab.getTabCount(); i++)
    {
      if (simTab.getComponentAt(i) instanceof Graph)
      {
        ((Graph) simTab.getComponentAt(i)).refresh();
      }
    }
  }

  public int getStartIndex(String outDir) {
    if (append.isSelected())
    {
      String[] searchForRunFiles = new File(outDir).list();
      int start = 1;
      for (String s : searchForRunFiles)
      {
        if (s.length() > 3 && s.substring(0, 4).equals("run-") && new File(outDir + File.separator + s).isFile())
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
  public boolean save()
  {

    String simName = (String) simulators.getSelectedItem();
    properties.setSim(simName);
    String propName = properties.getPropertiesName();
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

    if (step.getText().trim().equals("inf") && !simName.equals("euler"))
    {
      double timeStep = Double.MAX_VALUE;
      properties.getSimulationProperties().setMaxTimeStep(timeStep);
    }
    else if (step.getText().trim().equals("inf") && simName.equals("euler"))
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
        properties.getSimulationProperties().setMaxTimeStep(timeStep);
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
      properties.getSimulationProperties().setRun(run);
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
      properties.setNone();
    }
    else if (expandReactions.isSelected())
    {
      properties.setExpand();      
    }
    else if (reactionAbstraction.isSelected())
    {
      properties.setAbs();
    }
    else if (stateAbstraction.isSelected())
    {
      properties.setNary();
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

    log.addText("Creating properties file:\n" + propName + "\n");
    int numPaths = Integer.parseInt((String) (bifurcation.getSelectedItem()));
    properties.getIncrementalProperties().setNumPaths(numPaths);

    try {
      AnalysisPropertiesWriter.createProperties(properties);

      AnalysisPropertiesWriter.saveSEDML(SedMLDoc, properties);
    } catch (IOException e1) {
      e1.printStackTrace();
    }

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
//    try
//    {
//      Properties getProps = new Properties();
//      FileInputStream load = new FileInputStream(new File(propName));
//      getProps.load(load);
//      load.close();
//      getProps.setProperty("selected.simulator", simName);
//      if (transientProperties != null)
//      {
//        getProps.setProperty("selected.property", (String) transientProperties.getSelectedItem());
//      }
//      if (!fileStem.getText().trim().equals(""))
//      {
//        new File(root + File.separator + simName + File.separator + fileStem.getText().trim()).mkdir();
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
//      getProps.store(store, properties.getModelFile() + " Properties");
//      store.close();
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//      JOptionPane.showMessageDialog(Gui.frame, "Unable to add properties to property file.", "Error", JOptionPane.ERROR_MESSAGE);
//    }
    //change = false;
    return true;
  }



  public Graph createGraph(String open, boolean regularGraph)
  {
    String root = properties.getRoot();
    String simName = properties.getId();

    String outDir = root + File.separator + simName;
    String printer_id;
    printer_id = "tsd.printer";
    String printer_track_quantity = "amount";
    if (concentrations.isSelected())
    {
      printer_track_quantity = "concentration";
    }
    return new Graph(this, printer_track_quantity, simName + " simulation results", printer_id, outDir, "time", gui, open, log, null, regularGraph, false);
  }

  public void executeRun()
  {
    boolean ignoreSweep = false;
    String modelFile = properties.getModelFile();
    String root = properties.getRoot();
    String simName = properties.getSim();

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
      AnalysisPropertiesWriter.saveSEDML(SedMLDoc, properties);
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
          lhpnFile.load(root + File.separator + modelFile);
          Abstraction abst = new Abstraction(lhpnFile, properties.getVerificationProperties().getAbsProperty());
          abst.abstractSTG(false);
          abst.save(root + File.separator + simName + File.separator + modelFile);

          if (transientProperties != null && !((String) transientProperties.getSelectedItem()).equals("none"))
          {

            t1.convertLPN2SBML(root + File.separator + simName + File.separator + modelFile, ((String) transientProperties.getSelectedItem()));

          }
          else
          {
            t1.convertLPN2SBML(root + File.separator + simName + File.separator + modelFile, "");
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
            t1.convertLPN2SBML(root + File.separator + modelFile, ((String) transientProperties.getSelectedItem()));
          }
          else
          {
            t1.convertLPN2SBML(root + File.separator + modelFile, "");
          }
        } catch (BioSimException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      t1.setFilename(root + File.separator + simName + File.separator + stem + File.separator + modelFile.replace(".lpn", ".xml"));
      t1.outputSBML();
      new AnalysisThread(this).start(true);
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
    ListModel<String> preAbsList = preAbs.getModel();
    for (int i = 0; i < preAbsList.getSize(); i++)
    {
      String abstractionOption = preAbsList.getElementAt(i);
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
    ListModel<String> preAbsList = preAbs.getModel();
    for (int i = 0; i < preAbsList.getSize(); i++)
    {
      String abstractionOption = (String) preAbsList.getElementAt(i);
      if (!abstractionOption.equals("complex-formation-and-sequestering-abstraction") && !abstractionOption.equals("operator-site-reduction-abstraction"))
      {
        return true;
      }
    }
    ListModel<String> loopAbsList = loopAbs.getModel();
    if (loopAbsList.getSize() > 0)
    {
      return true;
    }
    ListModel<String> postAbsList = postAbs.getModel();
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
    change = false;
  }

  public void setSim(String newSimName)
  {
    properties.setSim(newSimName);
  }

  public boolean hasChanged()
  {
    return change;
  }

  public String[] getInterestingSpecies()
  {
    return properties.getSimulationProperties().getIntSpecies().toArray(new String[properties.getSimulationProperties().getIntSpecies().size()]);
  }

  public List<String> getInterestingSpeciesAsArrayList()
  {
    return properties.getSimulationProperties().getIntSpecies();
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
    List<String> interestingSpecies = properties.getSimulationProperties().getIntSpecies();
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

  public void run(ArrayList<AnalysisThread> threads, ArrayList<String> dirs, ArrayList<String> levelOne, String stem)
  {

    String root = properties.getRoot();
    String simName = properties.getSim();
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
    if (!dirs.isEmpty() && new File(root + File.separator + simName + File.separator + stem + dirs.get(0) + File.separator + "sim-rep.txt").exists())
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
              Scanner s = new Scanner(new File(root + File.separator + simName + File.separator + stem + d + File.separator + "sim-rep.txt"));
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
      constData.outputTSD(root + File.separator + simName + File.separator + "sim-rep.tsd");
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

  public String getRootPath()
  {
    return properties.getRoot();
  }
  
  public int getNumRuns() {
    try {
      return Integer.parseInt(runs.getText());
    } catch (Exception e) {
      return 1;
    }
  }
  
  public String getSimID()
  {
    return fileStem.getText().trim();
  }

  public String getSimPath()
  {
    String root = properties.getRoot();
    String simName = properties.getId();
    if (!fileStem.getText().trim().equals(""))
    {
      return root + File.separator + simName + File.separator + fileStem.getText().trim();
    }
    return root + File.separator + simName;
  }

  public String getSimName()
  {
    String simName = properties.getId();
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
  /**
   * Loads the simulate options.
   */
  private void loadPropertiesFile()
  {

    //      lpnAbstraction.preAbs.setListData(lpnAbstraction.getAbstractionProperty().preAbsModel.toArray());
    //      lpnAbstraction.loopAbs.setListData(lpnAbstraction.getAbstractionProperty().loopAbsModel.toArray());
    //      lpnAbstraction.postAbs.setListData(lpnAbstraction.getAbstractionProperty().postAbsModel.toArray());

    if (properties.isNone())
    {
      noAbstraction.setSelected(true);
      enableNoAbstraction();
    }
    else if (properties.isExpand())
    {
      expandReactions.setSelected(true);
      enableNoAbstraction();
    }
    else if (properties.isAbs())
    {
      reactionAbstraction.setSelected(true);
      enableReactionAbstraction();
    }
    else
    {
      stateAbstraction.setSelected(true);
      enableStateAbstraction();
    }


    // usingSSA.doClick();

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

    if(properties.getSimulationProperties().getPrinter_track_quantity() != null && properties.getSimulationProperties().getPrinter_track_quantity().equals("concentration"))
    {
      concentrations.doClick();
    }
    if (properties.getSimulationProperties().getPrinter_id() != null && properties.getSimulationProperties().getPrinter_id().equals("null.printer"))
    {
      genRuns.doClick();

    }
    
    if(properties.isPrintInterval())
    {
      intervalLabel.setSelectedItem("Print Interval");
      interval.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getPrintInterval()));
    }
    else if (properties.isMinPrintInterval())
    {
      intervalLabel.setSelectedItem("Minimum Print Interval");
      interval.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getMinTimeStep()));
    }
    else
    {
      intervalLabel.setSelectedItem("Number Of Steps");
      interval.setText(String.valueOf(properties.getSimulationProperties().getNumSteps()));
    }
    
    initialTimeField.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getInitialTime()));

    outputStartTimeField.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getOutputStartTime()));

    limit.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getTimeLimit()));

    absErr.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getAbsError()));

    relErr.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getRelError()));

    step.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getTimeStep()));

    minStep.setText(PropertiesUtil.parseDouble(properties.getSimulationProperties().getMinTimeStep()));
    
    if(properties.getSim() != null)
    {
      simulators.setSelectedItem(properties.getSim());
    }

    fileStem.setText(properties.getFileStem());
    
    seed.setText(String.valueOf(properties.getSimulationProperties().getRndSeed()));

    runs.setText(String.valueOf(properties.getSimulationProperties().getRun()));

    if (properties.isOde())
    {
      ODE.setSelected(true);
      enableODE();
      

    }
    else if (properties.isSsa())
    {
      monteCarlo.setSelected(true);
      append.setEnabled(true);
      enableMonteCarlo();
        String simId = properties.getSim();
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
      
       fileStem.setText(properties.getFileStem());
      
    }
    else if (properties.isMarkov())
    {
      markov.setSelected(true);
      enableMarkov();
      simulators.setSelectedItem(properties.getSim());
    }
    else if (properties.isFba())
    {
      fba.doClick();
      enableFBA();
      // absErr.setEnabled(false);
    }
    else if (properties.isSbml())
    {
      sbml.setSelected(true);
      enableSbmlDotAndXhtml();
    }
    else if (properties.isDot())
    {
      dot.setSelected(true);
      enableSbmlDotAndXhtml();
    }
    else if (properties.isXhtml())
    {
      xhtml.setSelected(true);
      enableSbmlDotAndXhtml();
    }
    else if (properties.isLhpn())
    {
      enableSbmlDotAndXhtml();
    }
    
//    if (load.containsKey("selected.property"))
//    {
//      if (transientProperties != null)
//      {
//        transientProperties.setSelectedItem(load.getProperty("selected.property"));
//      }
//    }
//    ArrayList<String> getLists = new ArrayList<String>();
//    int i = 1;
//    while (load.containsKey("simulation.run.termination.condition." + i))
//    {
//      getLists.add(load.getProperty("simulation.run.termination.condition." + i));
//      i++;
//    }
//    getLists = new ArrayList<String>();
//    i = 1;
//    while (load.containsKey("reb2sac.interesting.species." + i))
//    {
//      String species = load.getProperty("reb2sac.interesting.species." + i);
//      int j = 2;
//      String interesting = " ";
//      if (load.containsKey("reb2sac.concentration.level." + species + ".1"))
//      {
//        interesting += load.getProperty("reb2sac.concentration.level." + species + ".1");
//      }
//      while (load.containsKey("reb2sac.concentration.level." + species + "." + j))
//      {
//        interesting += "," + load.getProperty("reb2sac.concentration.level." + species + "." + j);
//        j++;
//      }
//      if (!interesting.equals(" "))
//      {
//        species += interesting;
//      }
//      getLists.add(species);
//      i++;
//    }
//    for (String s : getLists)
//    {
//      String[] split1 = s.split(" ");
//
//      // load the species and its thresholds into the list of
//      // interesting species
//      String speciesAndThresholds = split1[0];
//
//      if (split1.length > 1)
//      {
//        speciesAndThresholds += " " + split1[1];
//      }
//
//      interestingSpecies.add(speciesAndThresholds);
//    }
//
//    getLists = new ArrayList<String>();
//    i = 1;
//    while (load.containsKey("gcm.abstraction.method." + i))
//    {
//      getLists.add(load.getProperty("gcm.abstraction.method." + i));
//      i++;
//    }
//    i = 1;
//    while (load.containsKey("reb2sac.abstraction.method.1." + i))
//    {
//      getLists.add(load.getProperty("reb2sac.abstraction.method.1." + i));
//      i++;
//    }
//    preAbstractions = getLists.toArray();
//    preAbs.setListData(preAbstractions);
//
//    getLists = new ArrayList<String>();
//    i = 1;
//    while (load.containsKey("reb2sac.abstraction.method.2." + i))
//    {
//      getLists.add(load.getProperty("reb2sac.abstraction.method.2." + i));
//      i++;
//    }
//    loopAbstractions = getLists.toArray();
//    loopAbs.setListData(loopAbstractions);
//
//    getLists = new ArrayList<String>();
//    i = 1;
//    while (load.containsKey("reb2sac.abstraction.method.3." + i))
//    {
//      getLists.add(load.getProperty("reb2sac.abstraction.method.3." + i));
//      i++;
//    }
//    postAbstractions = getLists.toArray();
//    postAbs.setListData(postAbstractions);
//
//    if (load.containsKey("reb2sac.rapid.equilibrium.condition.1"))
//    {
//      rapid1.setText(load.getProperty("reb2sac.rapid.equilibrium.condition.1"));
//    }
//    if (load.containsKey("reb2sac.rapid.equilibrium.condition.2"))
//    {
//      rapid2.setText(load.getProperty("reb2sac.rapid.equilibrium.condition.2"));
//    }
//    if (load.containsKey("reb2sac.qssa.condition.1"))
//    {
//      qssa.setText(load.getProperty("reb2sac.qssa.condition.1"));
//    }
//    if (load.containsKey("reb2sac.operator.max.concentration.threshold"))
//    {
//      maxCon.setText(load.getProperty("reb2sac.operator.max.concentration.threshold"));
//    }
//    if (load.containsKey("reb2sac.diffusion.stoichiometry.amplification.value"))
//    {
//      diffStoichAmp.setText(load.getProperty("reb2sac.diffusion.stoichiometry.amplification.value"));
//    }
//    if (load.containsKey("reb2sac.iSSA.number.paths"))
//    {
//      bifurcation.setSelectedItem(load.getProperty("reb2sac.iSSA.number.paths"));
//    }
//    if (load.containsKey("reb2sac.iSSA.type"))
//    {
//      String type = load.getProperty("reb2sac.iSSA.type");
//      if (type.equals("mpde"))
//      {
//        mpde.doClick();
//      }
//      else if (type.equals("medianPath"))
//      {
//        medianPath.doClick();
//      }
//      else
//      {
//        meanPath.doClick();
//      }
//    }
//    if (load.containsKey("reb2sac.iSSA.adaptive"))
//    {
//      String type = load.getProperty("reb2sac.iSSA.adaptive");
//      if (type.equals("true"))
//      {
//        adaptive.doClick();
//      }
//      else
//      {
//        nonAdaptive.doClick();
//      }
//    }

    change = false;
  }
  public void updateProperties()
  {
    String modelFile = properties.getModelFile();
    String root = properties.getRoot();

    if (transientProperties != null && modelFile.contains(".lpn"))
    {
      Object selected = transientProperties.getSelectedItem();
      String[] props = new String[] { "none" };
      LPN lpn = new LPN();
      try {
        lpn.load(root + File.separator + modelFile);
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

  public JPanel getAdvanced()
  {
    JPanel constructPanel = new JPanel(new BorderLayout());
    constructPanel.add(advanced, "Center");
    return constructPanel;
  }
  
  /**
   * This method enables and disables the required fields for none and
   * abstraction.
   */
  private void enableNoAbstraction()
  {
    String modelFile = properties.getModelFile();
    ODE.setEnabled(true);
    monteCarlo.setEnabled(true);
    fba.setEnabled(true);
    markov.setEnabled(false);
    enableAbstractionOptions(false, false);
    ArrayList<String> getLists = new ArrayList<String>();
    String[] objects = getLists.toArray(new String[getLists.size()]);
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
    objects = getLists.toArray(new String[getLists.size()]);
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
    String modelFile = properties.getModelFile();
    ODE.setEnabled(true);
    monteCarlo.setEnabled(true);
    fba.setEnabled(true);
    markov.setEnabled(false);
    enableAbstractionOptions(true, true);
    ArrayList<String> getLists = new ArrayList<String>();
    getLists.add("complex-formation-and-sequestering-abstraction");
    getLists.add("operator-site-reduction-abstraction");
    String[] objects = getLists.toArray(new String[getLists.size()]);
    preAbs.setListData(objects);
    getLists = new ArrayList<String>();
    objects = getLists.toArray(new String[getLists.size()]);
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
    objects = getLists.toArray(new String[getLists.size()]);
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
    String[] objects = getLists.toArray(new String[getLists.size()]);
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
    genStats.setEnabled(true);
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
    String[] objects = getLists.toArray(new String[getLists.size()]);
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
    String[] objects = getLists.toArray(new String[getLists.size()]);
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
      String[] objects = getLists.toArray(new String[getLists.size()]);
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
    String[] objects = getLists.toArray(new String[getLists.size()]);
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
    String[] objects = getLists.toArray(new String[getLists.size()]);
    preAbs.setListData(objects);
    getLists = new ArrayList<String>();
    objects = getLists.toArray(new String[getLists.size()]);
    loopAbs.setListData(objects);
    getLists = new ArrayList<String>();
    objects = getLists.toArray(new String[getLists.size()]);
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
      String propName = properties.getFilename() + ".properties";
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

  /* Add an abstraction method */
  private void addAbstractionMethod(ActionEvent e)
  {
    JPanel addAbsPanel = new JPanel(new BorderLayout());
    JComboBox<String> absList = new JComboBox<String>();
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

  private void updateTSDGraph(boolean refresh)
  {
    SimulationProperties simulationProperties = properties.getSimulationProperties();
    
    String printer_id = simulationProperties.getPrinter_id();
    String directory = properties.getDirectory();
    boolean genStats = this.genStats.isSelected();
    double printInterval = simulationProperties.getPrintInterval();
    double timeLimit = simulationProperties.getTimeLimit();
    int runs = simulationProperties.getRun();
    String printer_track_quantity = simulationProperties.getPrinter_track_quantity();
    String outDir = directory;
    File work = new File(directory);
    
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
            ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, directory, warning, true);
          }
          new File(directory + File.separator + "running").delete();
          if (outputTerm)
          {
            ArrayList<String> dataLabels = new ArrayList<String>();
            dataLabels.add("time");
            ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
            if (new File(directory + File.separator + "sim-rep.txt").exists())
            {
              try
              {
                Scanner s = new Scanner(new File(directory + File.separator + "sim-rep.txt"));
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
            Scanner scan = null;
            try {
              scan = new Scanner(new File(directory + File.separator + "term-time.txt"));
            
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
            }
            catch (FileNotFoundException e) 
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            finally
            {
              if(scan != null)
              {
                scan.close();
              }
            }
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
            probData.outputTSD(directory + File.separator + "term-time.tsd");
            probData = new DataParser(dataLabels, percentData);
            probData.outputTSD(directory + File.separator + "percent-term-time.tsd");
          }
          if (refresh)
          {
            ((Graph) simTab.getComponentAt(i)).refresh();
          }
        }
        else
        {
          simTab.setComponentAt(i, new Graph(this, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, true, false));
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
            ((Graph) simTab.getComponentAt(i)).calculateAverageVarianceDeviation(run, 0, directory, warning, true);
          }
          new File(directory + File.separator + "running").delete();
          if (outputTerm)
          {
            ArrayList<String> dataLabels = new ArrayList<String>();
            dataLabels.add("time");
            ArrayList<ArrayList<Double>> terms = new ArrayList<ArrayList<Double>>();
            if (new File(directory + File.separator + "sim-rep.txt").exists())
            {
              try
              {
                Scanner s = new Scanner(new File(directory + File.separator + "sim-rep.txt"));
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
            Scanner scan = null;
            try {
              scan = new Scanner(new File(directory + File.separator + "term-time.txt"));
            
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
            } catch (FileNotFoundException e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            finally
            {
              if(scan != null)
              {

                scan.close();
              }
            }
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
            probData.outputTSD(directory + File.separator + "term-time.tsd");
            probData = new DataParser(dataLabels, percentData);
            probData.outputTSD(directory + File.separator + "percent-term-time.tsd");
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
            if (new File(directory + File.separator + "sim-rep.txt").exists())
            {
              simTab.setComponentAt(i, new Graph(this, printer_track_quantity, outDir.split("/")[outDir.split("/").length - 1] + " simulation results", printer_id, outDir, "time", gui, null, log, null, false, false));
              simTab.getComponentAt(i).setName("Histogram");
            }
          }
        }
      }
    }
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
      AnalysisPropertiesWriter.saveSEDML(SedMLDoc, properties);
      AnalysisPropertiesLoader.loadSEDML(SedMLDoc, subTask, properties);
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
  
  @Override
  public boolean request(RequestType type, Message message)
  {
    if(type == RequestType.REQUEST_STRING)
    {
      String sbmlName = JOptionPane.showInputDialog(this, "Enter Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
      message.setString(sbmlName);
      return true;
    }
    else if(type == RequestType.REQUEST_OVERWRITE)
    {
      final Object[] options = { "Overwrite", "Cancel" };
      int value = JOptionPane.showOptionDialog(this, "File already exists." + "\nDo you want to overwrite?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
      if (value == JOptionPane.YES_OPTION)
      {
        message.setBoolean(true);
      }
      else
      {
        message.setBoolean(false);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean send(RequestType type, Message message)
  {
    if(type == RequestType.ADD_FILE)
    {
      String sbmlName = message.getMessage();
      if (sbmlName != null && !sbmlName.trim().equals(""))
      {
        if (!gui.updateOpenModelEditor(sbmlName))
        {
          try
          {
            ModelEditor gcm = new ModelEditor(properties.getRoot() + File.separator, sbmlName, gui, log, false, null, null, null, false, false);
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
      
      return true;
    }
    
    if(type == RequestType.REQUEST_PROGRESS)
    {
      int progressValue = message.getValue();
      if(progress != null)
      {
        this.progress.setValue(progressValue);
        return true;
      }
    }
    
    return false;
  }
}
