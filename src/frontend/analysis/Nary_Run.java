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
package frontend.analysis;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
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

import dataModels.util.GlobalConstants;
import frontend.main.Gui;
import frontend.main.Log;
import frontend.main.util.Utility;
import frontend.verification.AbstPane;

/**
 * This is the Nary_Run class. It creates a GUI for input in the nary
 * abstraction. It implements the ActionListener class. This allows the Nary_Run
 * GUI to perform actions when buttons are pressed.
 * 
 * @author Curtis Madsen
 */
public class Nary_Run implements ActionListener, Runnable
{

	private final JFrame				naryFrame;						// Frame
																		// for
																		// nary
																		// abstraction

	private final JButton				naryRun;						// The
																		// run
																		// button
																		// for
																		// nary
																		// abstraction

	private final JButton				naryClose;						// The
																		// close
																		// button
																		// for
																		// nary
																		// abstraction

	private final JTextField			stopRate;						// Text
																		// field
																		// for
																		// nary
																		// abstraction

	private final JList					finalState;					// List
																		// for
																		// final
																		// state

	private Object[]					finalStates	= new Object[0];	// List
																		// of
																		// final
																		// states

	private final JComboBox				stopEnabled;					// Combo
																		// box
																		// for
																		// Nary
																		// Abstraction

	private final ArrayList<JTextField>	inhib;							// Text
																		// fields
																		// for
																		// inhibition
																		// levels

	private final ArrayList<JList>		consLevel;						// Lists
																		// for
																		// concentration
																		// levels

	private final ArrayList<String>		getSpeciesProps;				// Species
																		// in
																		// properties
																		// file

	private final JButton				finalAdd, finalRemove;			// Buttons
																		// for
																		// altering
																		// final
																		// state

	/*
	 * Text fields for species properties
	 */
	private final ArrayList<JTextField>	texts;

	private final ArrayList<Object[]>	conLevel;						// Lists
																		// for
																		// concentration
																		// levels

	private final JComboBox				highLow, speci;				// Combo
																		// Boxes
																		// for
																		// final
																		// states

	/*
	 * Radio Buttons for termination conditions
	 */

	private final JComboBox				simulators;					// Combo
																		// Box
																		// for
																		// possible
																		// simulators

	private final String				filename;						// name
																		// of
																		// sbml
																		// file

	private final String[]				getFilename;					// array
																		// of
																		// filename

	private final JRadioButton			fba, sbml, dot, xhtml;			// Radio
																		// Buttons
																		// output
																		// option

	/*
	 * Radio Buttons that represent the different abstractions
	 */
	private final JRadioButton			nary, ODE, monteCarlo;

	/*
	 * Data used for monteCarlo abstraction
	 */
	private final double				initialTime, outputStartTime, timeLimit, printInterval, minTimeStep, timeStep;

	private final int					run;												// Data
																							// used
																							// for
																							// monteCarlo
																							// abstraction

	private final long					rndSeed;											// Data
																							// used
																							// for
																							// monteCarlo
																							// abstraction

	/*
	 * Data used for monteCarlo abstraction
	 */
	private final String				outDir, printer_id, printer_track_quantity;

	/*
	 * terminations and interesting species
	 */
	private String[]					termCond;

	private final String[]				intSpecies;

	private final double				rap1, rap2, qss;									// advanced
																							// options

	private final int					con;												// advanced
																							// options

	private final ArrayList<Integer>	counts;											// counts
																							// of
																							// con
																							// levels

	private final Log					log;												// the
																							// log

	private final Gui					biomodelsim;										// tstubd
																							// gui

	private final JTabbedPane			simTab;											// the
																							// simulation
																							// tab

	private final String				root;

	private String						separator;

	private final String				useInterval;

	private final String				direct;

	private final String				modelFile;

	private final JRadioButton			abstraction;

	private final AbstPane				abstPane;

	private final double				absError;

	private final double				relError;

	/**
	 * This constructs a new Nary_Run object. This object is a GUI that contains
	 * input fields for the nary abstraction. This constructor initializes the
	 * member variables and creates the nary frame.
	 */
	public Nary_Run(Component component, JComboBox simulators, String[] getFilename,
			String filename, JRadioButton fba, JRadioButton sbml, JRadioButton dot,
			JRadioButton xhtml, JRadioButton nary, JRadioButton ODE, JRadioButton monteCarlo,
			double initialTime, double outputStartTime, double timeLimit, String useInterval, double printInterval, double minTimeStep,
			double timeStep, String outDir, long rndSeed, int run, String printer_id,
			String printer_track_quantity, String[] intSpecies, double rap1, double rap2,
			double qss, int con, Log log, Gui biomodelsim, JTabbedPane simTab, String root,
			String direct, String modelFile, JRadioButton abstraction, AbstPane abstPane,
			double absError, double relError)
	{
		separator = GlobalConstants.separator;

		// intitializes the member variables
		this.absError = absError;
		this.relError = relError;
		this.root = root;
		this.rap1 = rap1;
		this.rap2 = rap2;
		this.qss = qss;
		this.con = con;
		this.intSpecies = intSpecies;
		this.initialTime = initialTime;
		this.outputStartTime = outputStartTime;
		this.timeLimit = timeLimit;
		this.printInterval = printInterval;
		this.minTimeStep = minTimeStep;
		this.timeStep = timeStep;
		this.outDir = outDir;
		this.rndSeed = rndSeed;
		this.run = run;
		this.printer_id = printer_id;
		this.printer_track_quantity = printer_track_quantity;
		this.simulators = simulators;
		this.getFilename = getFilename;
		this.filename = filename;
		this.dot = dot;
		this.sbml = sbml;
		this.fba = fba;
		this.xhtml = xhtml;
		this.nary = nary;
		this.monteCarlo = monteCarlo;
		this.ODE = ODE;
		this.log = log;
		this.biomodelsim = biomodelsim;
		this.simTab = simTab;
		this.useInterval = useInterval;
		this.direct = direct;
		this.modelFile = modelFile;
		this.abstPane = abstPane;
		this.abstraction = abstraction;

		// creates the nary frame and adds a window listener
		naryFrame = new JFrame("Nary Properties");
		WindowListener w = new WindowListener()
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				naryFrame.dispose();
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
		naryFrame.addWindowListener(w);

		// creates the input fields for the nary abstraction
		JPanel naryInput = new JPanel(new GridLayout(2, 2));
		JLabel stopEnabledLabel = new JLabel("Analysis Stop Enabled:");
		String choice[] = new String[2];
		choice[0] = "false";
		choice[1] = "true";
		stopEnabled = new JComboBox(choice);
		JLabel stopRateLabel = new JLabel("Analysis Stop Rate:");
		stopRate = new JTextField();
		stopRate.setText("0.0005");

		// creates the final state JList
		JLabel finalStateLabel = new JLabel("Final State:");
		finalState = new JList();
		JScrollPane finalScroll = new JScrollPane();
		finalScroll.setPreferredSize(new Dimension(260, 100));
		finalScroll.setViewportView(finalState);
		JPanel addRemove = new JPanel();
		Object[] high = { "high", "low" };
		highLow = new JComboBox(high);
		finalAdd = new JButton("Add");
		finalRemove = new JButton("Remove");
		finalAdd.addActionListener(this);
		finalRemove.addActionListener(this);

		// adds the nary input fields to a panel
		naryInput.add(stopEnabledLabel);
		naryInput.add(stopEnabled);
		naryInput.add(stopRateLabel);
		naryInput.add(stopRate);
		JPanel finalPanel = new JPanel();
		finalPanel.add(finalStateLabel);
		finalPanel.add(finalScroll);
		JPanel naryInputPanel = new JPanel(new BorderLayout());
		naryInputPanel.add(naryInput, "North");
		naryInputPanel.add(finalPanel, "Center");
		naryInputPanel.add(addRemove, "South");

		// reads in the species properties to determine which species to use
		Properties naryProps = new Properties();
		try
		{
			FileInputStream load = new FileInputStream(new File(outDir + separator
					+ "species.properties"));
			naryProps.load(load);
			load.close();
			FileOutputStream store = new FileOutputStream(new File(outDir + separator
					+ "species.properties"));
			naryProps.store(store, "");
			store.close();
			naryProps = new Properties();
			new File("species.properties").delete();
			load = new FileInputStream(new File(outDir + separator + "species.properties"));
			naryProps.load(load);
			load.close();
		}
		catch (Exception e1)
		{
			JOptionPane.showMessageDialog(component, "Properties File Not Found!",
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		}
		Iterator<Object> iter = naryProps.keySet().iterator();
		getSpeciesProps = new ArrayList<String>();
		while (iter.hasNext())
		{
			String next = (String) iter.next();
			if (next.contains("specification"))
			{
				String[] get = next.split("e");
				getSpeciesProps.add(get[get.length - 1].substring(1, get[get.length - 1].length()));
			}
		}

		// puts the species into the combo box for the final state
		speci = new JComboBox(getSpeciesProps.toArray());
		addRemove.add(highLow);
		addRemove.add(speci);
		addRemove.add(finalAdd);
		addRemove.add(finalRemove);

		// creates the species properties input fields
		ArrayList<JPanel> specProps = new ArrayList<JPanel>();
		texts = new ArrayList<JTextField>();
		inhib = new ArrayList<JTextField>();
		consLevel = new ArrayList<JList>();
		conLevel = new ArrayList<Object[]>();
		counts = new ArrayList<Integer>();
		for (int i = 0; i < getSpeciesProps.size(); i++)
		{
			JPanel newPanel1 = new JPanel(new GridLayout(1, 2));
			JPanel newPanel2 = new JPanel(new GridLayout(1, 2));
			JPanel label = new JPanel();
			label.add(new JLabel(getSpeciesProps.get(i) + " Absolute Inhibition Threshold:"));
			newPanel1.add(label);
			JPanel text = new JPanel();
			inhib.add(new JTextField());
			inhib.get(i).setPreferredSize(new Dimension(260, 20));
			inhib.get(i).setText("<<none>>");
			text.add(inhib.get(i));
			newPanel1.add(text);
			JPanel otherLabel = new JPanel();
			otherLabel.add(new JLabel(getSpeciesProps.get(i) + " Concentration Level:"));
			newPanel2.add(otherLabel);
			consLevel.add(new JList());
			conLevel.add(new Object[0]);
			iter = naryProps.keySet().iterator();
			ArrayList<String> get = new ArrayList<String>();
			int count = 0;
			while (iter.hasNext())
			{
				String next = (String) iter.next();
				if (next.contains("concentration.level." + getSpeciesProps.get(i) + "."))
				{
					get.add(naryProps.getProperty(next));
					count++;
				}
			}
			counts.add(count);
			int in;
			for (int out = 1; out < get.size(); out++)
			{
				if (!get.get(out).equals("<<none>>"))
				{
					double temp = Double.parseDouble(get.get(out));
					in = out;
					while (in > 0 && Double.parseDouble(get.get(in - 1)) >= temp)
					{
						get.set(in, get.get(in - 1));
						--in;
					}
					get.set(in, temp + "");
				}
			}
			consLevel.get(i).setListData(get.toArray());
			conLevel.set(i, get.toArray());
			JScrollPane scroll = new JScrollPane();
			scroll.setPreferredSize(new Dimension(260, 100));
			scroll.setViewportView(consLevel.get(i));
			JPanel area = new JPanel();
			area.add(scroll);
			newPanel2.add(area);
			JPanel addAndRemove = new JPanel();
			JTextField adding = new JTextField(15);
			texts.add(adding);
			JButton Add = new JButton("Add");
			JButton Remove = new JButton("Remove");
			Add.addActionListener(this);
			Add.setActionCommand("Add" + i);
			Remove.addActionListener(this);
			Remove.setActionCommand("Remove" + i);
			addAndRemove.add(adding);
			addAndRemove.add(Add);
			addAndRemove.add(Remove);
			JPanel newnewPanel = new JPanel(new BorderLayout());
			newnewPanel.add(newPanel1, "North");
			newnewPanel.add(newPanel2, "Center");
			newnewPanel.add(addAndRemove, "South");
			specProps.add(newnewPanel);
		}

		// creates the nary run and close buttons
		naryRun = new JButton("Run Nary");
		naryClose = new JButton("Cancel");
		naryRun.addActionListener(this);
		naryClose.addActionListener(this);
		JPanel naryRunPanel = new JPanel();
		naryRunPanel.add(naryRun);
		naryRunPanel.add(naryClose);

		// creates tabs for all the nary options and all the species
		JTabbedPane naryTabs = new JTabbedPane();
		naryTabs.addTab("Nary Properties", naryInputPanel);
		for (int i = 0; i < getSpeciesProps.size(); i++)
		{
			naryTabs.addTab(getSpeciesProps.get(i) + " Properties", specProps.get(i));
		}

		// adds the tabs and the run button to the main panel
		JPanel naryPanel = new JPanel(new BorderLayout());
		naryPanel.add(naryTabs, "Center");
		naryPanel.add(naryRunPanel, "South");

		// Packs the nary frame and displays it
		naryFrame.setContentPane(naryPanel);
		naryFrame.pack();
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
		Dimension frameSize = naryFrame.getSize();

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
		naryFrame.setLocation(x, y);
		naryFrame.setResizable(false);
		// naryFrame.setVisible(true);
	}

	public void open()
	{
		naryFrame.setVisible(true);
	}

	/**
	 * This method performs different functions depending on what buttons are
	 * pushed and what input fields contain data.
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// if the nary run button is clicked
		if (e.getSource() == naryRun)
		{
			new Thread(this).start();
		}
		// if the nary close button is clicked
		if (e.getSource() == naryClose)
		{
			naryFrame.dispose();
		}
		// if the add button for the final states is clicked
		else if (e.getSource() == finalAdd)
		{
			JList add = new JList();
			Object[] adding = { highLow.getSelectedItem() + "." + speci.getSelectedItem() };
			add.setListData(adding);
			add.setSelectedIndex(0);
			finalStates = Utility.add(finalStates, finalState, add);
		}
		// if the remove button for the final states is clicked
		else if (e.getSource() == finalRemove)
		{
			Utility.remove(finalState, finalStates);
		}
		// if the add or remove button for the species properties is clicked
		else
		{
			// if the add button for the species properties is clicked
			if (e.getActionCommand().contains("Add"))
			{
				int number = Integer.parseInt(e.getActionCommand().substring(3,
						e.getActionCommand().length()));
				try
				{
					double get = Double.parseDouble(texts.get(number).getText().trim());
					if (get < 0)
					{
						JOptionPane.showMessageDialog(naryFrame,
								"Concentration Levels Must Be Positive Real Numbers.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						JList add = new JList();
						Object[] adding = { "" + get };
						add.setListData(adding);
						add.setSelectedIndex(0);
						Object[] sort = Utility.add(conLevel.get(number), consLevel.get(number),
								add);
						int in;
						for (int out = 1; out < sort.length; out++)
						{
							double temp = Double.parseDouble((String) sort[out]);
							in = out;
							while (in > 0 && Double.parseDouble((String) sort[in - 1]) >= temp)
							{
								sort[in] = sort[in - 1];
								--in;
							}
							sort[in] = temp + "";
						}
						conLevel.set(number, sort);
					}
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(naryFrame,
							"Concentration Levels Must Be Positive Real Numbers.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}

			}
			// if the remove button for the species properties is clicked
			else if (e.getActionCommand().contains("Remove"))
			{
				int number = Integer.parseInt(e.getActionCommand().substring(6,
						e.getActionCommand().length()));
				Utility.remove(consLevel.get(number), conLevel.get(number));
			}
		}
	}

	/**
	 * If the nary run button is pressed, this method starts a new thread for
	 * the nary abstraction.
	 */
	@Override
	public void run()
	{
		naryFrame.dispose();
		final JButton naryCancel = new JButton("Cancel Nary");
		final JFrame running = new JFrame("Running...");
		WindowListener w = new WindowListener()
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				naryCancel.doClick();
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
		JLabel label = new JLabel("Progress");
		JProgressBar progress = new JProgressBar(0, run);
		progress.setStringPainted(true);
		// progress.setString("");
		// progress.setIndeterminate(true);
		progress.setValue(0);
		text.add(label);
		progBar.add(progress);
		button.add(naryCancel);
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
		String sim = (String) simulators.getSelectedItem();
		String stopE = (String) stopEnabled.getSelectedItem();
		double stopR = 0.0005;
		try
		{
			stopR = Double.parseDouble(stopRate.getText().trim());
		}
		catch (Exception e1)
		{
			JOptionPane.showMessageDialog(naryFrame,
					"Must Enter A Real Number Into The Analysis Stop Rate Field.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		String[] finalS = Utility.getList(finalStates, finalState);
		Run runProgram = new Run(null);
		naryCancel.addActionListener(runProgram);
		Nary_Run.createNaryProperties(initialTime, outputStartTime, timeLimit, useInterval, printInterval, minTimeStep, timeStep,
				outDir, rndSeed, run, 1, printer_id, printer_track_quantity, getFilename,
				naryFrame, filename, monteCarlo, stopE, stopR, finalS, inhib, consLevel,
				getSpeciesProps, conLevel, termCond, intSpecies, rap1, rap2, qss, con, counts,
				false, false, false);
		if (monteCarlo.isSelected())
		{
			File[] files = new File(outDir).listFiles();
			for (File f : files)
			{
				if (f.getName().contains("run-"))
				{
					f.delete();
				}
			}
		}
		runProgram.execute(filename, fba, sbml, dot, xhtml, naryFrame, ODE, monteCarlo, sim,
				printer_id, printer_track_quantity, outDir, nary, 2, intSpecies, log, biomodelsim,
				simTab, root, progress, "", null, direct, initialTime, outputStartTime, timeLimit, timeLimit * run, modelFile,
				abstPane, abstraction, null, null, absError, relError, timeStep, printInterval, run, rndSeed,
				true, label, running);
		running.setCursor(null);
		running.dispose();
		naryCancel.removeActionListener(runProgram);
	}

	/**
	 * This method is given what data is entered into the nary frame and creates
	 * the nary properties file from that information.
	 */
	public static void createNaryProperties(double initialTime, double outputStartTime, double timeLimit, String useInterval,
			double printInterval, double minTimeStep, double timeStep, String outDir, long rndSeed,
			int run, int numPaths, String printer_id, String printer_track_quantity,
			String[] getFilename, Component component, String filename, JRadioButton monteCarlo,
			String stopE, double stopR, String[] finalS, ArrayList<JTextField> inhib,
			ArrayList<JList> consLevel, ArrayList<String> getSpeciesProps,
			ArrayList<Object[]> conLevel, String[] termCond, String[] intSpecies, double rap1,
			double rap2, double qss, int con, ArrayList<Integer> counts, boolean mpde,
			boolean meanPath, boolean adaptive)
	{
		Properties nary = new Properties();
		try
		{
			FileInputStream load = new FileInputStream(new File(outDir + GlobalConstants.separator
					+ "species.properties"));
			nary.load(load);
			load.close();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(component, "Species Properties File Not Found!",
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		}
		nary.setProperty("reb2sac.abstraction.method.0.1", "enzyme-kinetic-qssa-1");
		nary.setProperty("reb2sac.abstraction.method.0.2", "reversible-to-irreversible-transformer");
		nary.setProperty("reb2sac.abstraction.method.0.3", "multiple-products-reaction-eliminator");
		nary.setProperty("reb2sac.abstraction.method.0.4", "multiple-reactants-reaction-eliminator");
		nary.setProperty("reb2sac.abstraction.method.0.5",
				"single-reactant-product-reaction-eliminator");
		nary.setProperty("reb2sac.abstraction.method.0.6", "dimer-to-monomer-substitutor");
		nary.setProperty("reb2sac.abstraction.method.0.7", "inducer-structure-transformer");
		nary.setProperty("reb2sac.abstraction.method.1.1", "modifier-structure-transformer");
		nary.setProperty("reb2sac.abstraction.method.1.2", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.2.1", "operator-site-forward-binding-remover");
		nary.setProperty("reb2sac.abstraction.method.2.3", "enzyme-kinetic-rapid-equilibrium-1");
		nary.setProperty("reb2sac.abstraction.method.2.4", "irrelevant-species-remover");
		nary.setProperty("reb2sac.abstraction.method.2.5", "inducer-structure-transformer");
		nary.setProperty("reb2sac.abstraction.method.2.6", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.2.7", "similar-reaction-combiner");
		nary.setProperty("reb2sac.abstraction.method.2.8", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.2.2", "dimerization-reduction");
		nary.setProperty("reb2sac.abstraction.method.3.1", "nary-order-unary-transformer");
		nary.setProperty("reb2sac.abstraction.method.3.2", "modifier-constant-propagation");
		nary.setProperty("reb2sac.abstraction.method.3.3", "absolute-inhibition-generator");
		nary.setProperty("reb2sac.abstraction.method.3.4", "final-state-generator");
		nary.setProperty("reb2sac.abstraction.method.3.5", "stop-flag-generator");
		nary.setProperty("reb2sac.nary.order.decider", "distinct");
		nary.setProperty("simulation.printer", printer_id);
		nary.setProperty("simulation.printer.tracking.quantity", printer_track_quantity);
		nary.setProperty("reb2sac.analysis.stop.enabled", stopE);
		nary.setProperty("reb2sac.analysis.stop.rate", "" + stopR);
		for (int i = 0; i < getSpeciesProps.size(); i++)
		{
			if (!(inhib.get(i).getText().trim() != "<<none>>"))
			{
				nary.setProperty("reb2sac.absolute.inhibition.threshold." + getSpeciesProps.get(i),
						inhib.get(i).getText().trim());
			}
			String[] consLevels = Utility.getList(conLevel.get(i), consLevel.get(i));
			for (int j = 0; j < counts.get(i); j++)
			{
				nary.remove("reb2sac.concentration.level." + getSpeciesProps.get(i) + "." + (j + 1));
			}
			for (int j = 0; j < consLevels.length; j++)
			{
				nary.setProperty("reb2sac.concentration.level." + getSpeciesProps.get(i) + "."
						+ (j + 1), consLevels[j]);
			}
		}
		if (monteCarlo.isSelected())
		{
			nary.setProperty("simulation.initial.time", "" + initialTime);
			nary.setProperty("simulation.output.start.time", "" + outputStartTime);
			nary.setProperty("monte.carlo.simulation.time.limit", "" + timeLimit);
			if (useInterval.equals("Print Interval"))
			{
				nary.setProperty("monte.carlo.simulation.print.interval", "" + printInterval);
			}
			else if (useInterval.equals("Minimum Print Interval"))
			{
				nary.setProperty("monte.carlo.simulation.minimum.print.interval", ""
						+ printInterval);
			}
			else
			{
				nary.setProperty("monte.carlo.simulation.number.steps", "" + ((int) printInterval));
			}
			if (timeStep == Double.MAX_VALUE)
			{
				nary.setProperty("monte.carlo.simulation.time.step", "inf");
			}
			else
			{
				nary.setProperty("monte.carlo.simulation.time.step", "" + timeStep);
			}
			nary.setProperty("monte.carlo.simulation.min.time.step", "" + minTimeStep);
			nary.setProperty("monte.carlo.simulation.random.seed", "" + rndSeed);
			nary.setProperty("monte.carlo.simulation.runs", "" + run);
			nary.setProperty("monte.carlo.simulation.out.dir", ".");
			nary.setProperty("reb2sac.iSSA.number.paths", "" + numPaths);
			if (mpde)
			{
				nary.setProperty("reb2sac.iSSA.type", "mpde");
			}
			else if (meanPath)
			{
				nary.setProperty("reb2sac.iSSA.type", "meanPath");
			}
			else
			{
				nary.setProperty("reb2sac.iSSA.type", "medianPath");
			}
			if (adaptive)
			{
				nary.setProperty("reb2sac.iSSA.adaptive", "true");
			}
			else
			{
				nary.setProperty("reb2sac.iSSA.adaptive", "false");
			}
		}
		for (int i = 0; i < finalS.length; i++)
		{
			if (finalS[i].trim() != "<<unknown>>")
			{
				nary.setProperty("reb2sac.final.state." + (i + 1), "" + finalS[i]);
			}
		}
		for (int i = 0; i < intSpecies.length; i++)
		{
			if (intSpecies[i] != "")
			{
				nary.setProperty("reb2sac.interesting.species." + (i + 1), "" + intSpecies[i]);
			}
		}
		nary.setProperty("reb2sac.rapid.equilibrium.condition.1", "" + rap1);
		nary.setProperty("reb2sac.rapid.equilibrium.condition.2", "" + rap2);
		nary.setProperty("reb2sac.qssa.condition.1", "" + qss);
		nary.setProperty("reb2sac.operator.max.concentration.threshold", "" + con);
		for (int i = 0; i < termCond.length; i++)
		{
			if (termCond[i] != "")
			{
				nary.setProperty("simulation.run.termination.condition." + (i + 1), ""
						+ termCond[i]);
			}
		}
		try
		{
			FileOutputStream store = new FileOutputStream(new File(filename.replace(".sbml", "")
					.replace(".xml", "") + ".properties"));
			nary.store(store,
					getFilename[getFilename.length - 1].replace(".sbml", "").replace(".xml", "")
							+ " Properties");
			store.close();
		}
		catch (Exception except)
		{
			JOptionPane.showMessageDialog(component, "Unable To Save Properties File!"
					+ "\nMake sure you select a model for simulation.", "Unable To Save File",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
