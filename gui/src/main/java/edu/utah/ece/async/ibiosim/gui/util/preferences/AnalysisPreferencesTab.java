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
package edu.utah.ece.async.ibiosim.gui.util.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum AnalysisPreferencesTab implements PreferencesTab {
	INSTANCE;

	// askUser is 0, overwrite is 1, and keep is 2
	private JTextField initialTime;
	private JTextField outputStartTime;
	private JTextField limit;
	private JTextField interval;
	private JTextField minStep;
	private JTextField step;
	private JTextField error;
	private JTextField relError;
	private JTextField seed;
	private JTextField runs;
	private JTextField rapid1;
	private JTextField rapid2;
	private JTextField qssa;
	private JTextField concentration;
	private JTextField amplification;
	private JComboBox useInterval;
	private JTextField simCommand;
	private JComboBox sim;
	private JComboBox abs;
	private JComboBox type;

	@Override
	public String getTitle() {
		return "Analysis";
	}

	@Override
	public String getDescription() {
		return "Default Analysis Settings";
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.getImageIcon("simulation.jpg");
	}

	@Override
	public Component getComponent() {
		// analysis preferences
		String[] choices = { "None", "Expand Reactions", "Reaction-based", "State-based" };
		simCommand = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.command"));
		abs = new JComboBox(choices);
		abs.setSelectedItem(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.abs"));

		if (!abs.getSelectedItem().equals("State-based")) {
			choices = new String[] { "ODE", "Monte Carlo", "SBML", "Network", "Browser" };
		}
		else {
			choices = new String[] { "Monte Carlo", "Markov", "SBML", "Network", "Browser", "LPN" };
		}

		type = new JComboBox(choices);
		type.setSelectedItem(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.type"));

		if (type.getSelectedItem().equals("ODE")) {
			choices = new String[] { "euler", "gear1", "gear2", "rk4imp", "rk8pd", "rkf45", "Runge-Kutta-Fehlberg" };
		}
		else if (type.getSelectedItem().equals("Monte Carlo")) {
			choices = new String[] { "gillespie", "SSA-Hierarchical", "SSA-Direct", "SSA-CR", "iSSA", "interactive", "emc-sim", "bunker", "nmc"};
		}
		else if (type.getSelectedItem().equals("Markov")) {
			choices = new String[] { "steady-state-markov-chain-analysis", "transient-markov-chain-analysis", "reachability-analysis", "prism", "atacs",
					"ctmc-transient" };
		}
		else {
			choices = new String[] { "euler", "gear1", "gear2", "rk4imp", "rk8pd", "rkf45" };
		}

		sim = new JComboBox(choices);
		sim.setSelectedItem(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.sim"));
		abs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!abs.getSelectedItem().equals("State-based")) {
					Object o = type.getSelectedItem();
					type.removeAllItems();
					type.addItem("ODE");
					type.addItem("Monte Carlo");
					type.addItem("Model");
					type.addItem("Network");
					type.addItem("Browser");
					type.setSelectedItem(o);
				}
				else {
					Object o = type.getSelectedItem();
					type.removeAllItems();
					type.addItem("Monte Carlo");
					type.addItem("Markov");
					type.addItem("Model");
					type.addItem("Network");
					type.addItem("Browser");
					type.addItem("LPN");
					type.setSelectedItem(o);
				}
			}
		});

		type.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (type.getSelectedItem() == null) {
				}
				else if (type.getSelectedItem().equals("ODE")) {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("euler");
					sim.addItem("gear1");
					sim.addItem("gear2");
					sim.addItem("rk4imp");
					sim.addItem("rk8pd");
					sim.addItem("rkf45");
					sim.addItem("Runge-Kutta-Fehlberg");
					sim.setSelectedIndex(5);
					sim.setSelectedItem(o);
				}
				else if (type.getSelectedItem().equals("Monte Carlo")) {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("gillespie");
					sim.addItem("SSA-Hierarchical");
					sim.addItem("SSA-Direct");
					sim.addItem("SSA-CR");
					sim.addItem("interactive");
					sim.addItem("iSSA");
					sim.addItem("emc-sim");
					sim.addItem("bunker");
					sim.addItem("nmc");
					sim.setSelectedItem(o);
				}
				else if (type.getSelectedItem().equals("Markov")) {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("steady-state-markov-chain-analysis");
					sim.addItem("transient-markov-chain-analysis");
					sim.addItem("reachability-analysis");
					sim.addItem("prism");
					sim.addItem("atacs");
					sim.addItem("ctmc-transient");
					sim.setSelectedItem(o);
				}
				else {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("euler");
					sim.addItem("gear1");
					sim.addItem("gear2");
					sim.addItem("rk4imp");
					sim.addItem("rk8pd");
					sim.addItem("rkf45");
					sim.setSelectedIndex(5);
					sim.setSelectedItem(o);
				}
			}
		});

		initialTime = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.initial.time"));
		outputStartTime = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.output.start.time"));
		limit = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.limit"));
		interval = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.interval"));
		minStep = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.min.step"));
		step = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.step"));
		error = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.error"));
		relError = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.relative.error"));
		seed = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.seed"));
		runs = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.runs"));
		rapid1 = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.rapid1"));
		rapid2 = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.rapid2"));
		qssa = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.qssa"));
		concentration = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.concentration"));
		amplification = new JTextField(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.amplification"));
		
		choices = new String[] { "Print Interval", "Minimum Print Interval", "Number Of Steps" };
		useInterval = new JComboBox(choices);
		useInterval.setSelectedItem(IBioSimPreferences.INSTANCE.getAnalysisPreference("biosim.sim.useInterval"));

		JPanel analysisLabels = new JPanel(new GridLayout(19, 1));
		analysisLabels.add(new JLabel("Simulation Command:"));
		analysisLabels.add(new JLabel("Abstraction:"));
		analysisLabels.add(new JLabel("Simulation Type:"));
		analysisLabels.add(new JLabel("Possible Simulators/Analyzers:"));
		analysisLabels.add(new JLabel("Initial Time:"));
		analysisLabels.add(new JLabel("Output Start Time:"));
		analysisLabels.add(new JLabel("Time Limit:"));
		analysisLabels.add(useInterval);
		analysisLabels.add(new JLabel("Minimum Time Step:"));
		analysisLabels.add(new JLabel("Maximum Time Step:"));
		analysisLabels.add(new JLabel("Absolute Error:"));
		analysisLabels.add(new JLabel("Relative Error:"));
		analysisLabels.add(new JLabel("Random Seed:"));
		analysisLabels.add(new JLabel("Runs:"));
		analysisLabels.add(new JLabel("Rapid Equilibrium Condition 1:"));
		analysisLabels.add(new JLabel("Rapid Equilibrium Condition 2:"));
		analysisLabels.add(new JLabel("QSSA Condition:"));
		analysisLabels.add(new JLabel("Max Concentration Threshold:"));
		analysisLabels.add(new JLabel("Grid Diffusion Stoichiometry Amplification:"));

		JPanel analysisFields = new JPanel(new GridLayout(19, 1));
		analysisFields.add(simCommand);
		analysisFields.add(abs);
		analysisFields.add(type);
		analysisFields.add(sim);
		analysisFields.add(initialTime);
		analysisFields.add(outputStartTime);
		analysisFields.add(limit);
		analysisFields.add(interval);
		analysisFields.add(minStep);
		analysisFields.add(step);
		analysisFields.add(error);
		analysisFields.add(relError);
		analysisFields.add(seed);
		analysisFields.add(runs);
		analysisFields.add(rapid1);
		analysisFields.add(rapid2);
		analysisFields.add(qssa);
		analysisFields.add(concentration);
		analysisFields.add(amplification);
		
		JButton restoreAn = new JButton("Restore Defaults");
		restoreAn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simCommand.setText("");
				abs.setSelectedItem("None");
				type.setSelectedItem("ODE");
				sim.setSelectedItem("rkf45");
				initialTime.setText("0.0");
				outputStartTime.setText("0.0");
				limit.setText("100.0");
				useInterval.setSelectedItem("Print Interval");
				interval.setText("1.0");
				step.setText("inf");
				minStep.setText("0");
				error.setText("1.0E-9");
				relError.setText("0.0");
				seed.setText("314159");
				runs.setText("1");
				rapid1.setText("0.1");
				rapid2.setText("0.1");
				qssa.setText("0.1");
				concentration.setText("15");
				amplification.setText("1.0");
			}
		});	

		// create analysis preferences panel
		JPanel analysisPrefs = new JPanel(new GridLayout(1, 2));
		analysisPrefs.add(analysisLabels);
		analysisPrefs.add(analysisFields);
		JPanel analysisPrefsFinal = new JPanel(new BorderLayout());
		analysisPrefsFinal.add(analysisPrefs,"North");
		analysisPrefsFinal.add(restoreAn,"South");
		
		FormBuilder builder = new FormBuilder();
		builder.add("", analysisPrefsFinal);

		return builder.build();
	}

	@Override
	public void save() {
		IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.command", simCommand.getText().trim());
		IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.useInterval", (String) useInterval.getSelectedItem());
		IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.abs", (String) abs.getSelectedItem());
		IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.type", (String) type.getSelectedItem());
		IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.sim", (String) sim.getSelectedItem());
		try {
			Double.parseDouble(initialTime.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.initial.time", initialTime.getText().trim());
			Double.parseDouble(outputStartTime.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.output.start.time", outputStartTime.getText().trim());
			Double.parseDouble(limit.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.limit", limit.getText().trim());
			Double.parseDouble(interval.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.interval", interval.getText().trim());
			if (step.getText().trim().equals("inf")) {
				IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.step", step.getText().trim());
			}
			else {
				Double.parseDouble(step.getText().trim());
				IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.step", step.getText().trim());
			}
			Double.parseDouble(minStep.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.min.sim.step", minStep.getText().trim());
			Double.parseDouble(error.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.error", error.getText().trim());
			Double.parseDouble(relError.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.relative.error", relError.getText().trim());
			Long.parseLong(seed.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.seed", seed.getText().trim());
			Integer.parseInt(runs.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.runs", runs.getText().trim());
			Double.parseDouble(rapid1.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.rapid1", rapid1.getText().trim());
			Double.parseDouble(rapid2.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.rapid2", rapid2.getText().trim());
			Double.parseDouble(qssa.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.qssa", qssa.getText().trim());
			Integer.parseInt(concentration.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.concentration", concentration.getText().trim());
			Double.parseDouble(amplification.getText().trim());
			IBioSimPreferences.INSTANCE.setAnalysisPreference("biosim.sim.amplification", amplification.getText().trim());
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Numeric analysis preference given non-numeric value.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
