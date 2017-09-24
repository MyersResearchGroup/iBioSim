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
import edu.utah.ece.async.ibiosim.gui.util.preferences.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum LearnPreferencesTab implements PreferencesTab {
	INSTANCE;

	private JTextField tn;
	private JTextField tj;
	private JTextField ti;
	private JComboBox bins;
	private JComboBox equaldata;
	private JComboBox autolevels;
	private JTextField ta;
	private JTextField tr;
	private JTextField tm;
	private JTextField tt;
	private JComboBox debug;
	private JComboBox succpred;
	private JComboBox findbaseprob;

	@Override
	public String getTitle() {
		return "Learn";
	}

	@Override
	public String getDescription() {
		return "Default Learn Settings";
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.getImageIcon("learn.jpg");
	}

	@Override
	public Component getComponent() {
		// learning preferences
		tn = new JTextField(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.tn"));
		tj = new JTextField(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.tj"));
		ti = new JTextField(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.ti"));
		String[] choices = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		bins = new JComboBox(choices);
		bins.setSelectedItem(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.bins"));
		choices = new String[] { "Equal Data Per Bins", "Equal Spacing Of Bins" };
		equaldata = new JComboBox(choices);
		equaldata.setSelectedItem(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.equaldata"));
		choices = new String[] { "Auto", "User" };
		autolevels = new JComboBox(choices);
		autolevels.setSelectedItem(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.autolevels"));
		ta = new JTextField(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.ta"));
		tr = new JTextField(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.tr"));
		tm = new JTextField(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.tm"));
		tt = new JTextField(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.tt"));
		choices = new String[] { "0", "1", "2", "3" };
		debug = new JComboBox(choices);
		debug.setSelectedItem(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.debug"));
		choices = new String[] { "Successors", "Predecessors", "Both" };
		succpred = new JComboBox(choices);
		succpred.setSelectedItem(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.succpred"));
		choices = new String[] { "True", "False" };
		findbaseprob = new JComboBox(choices);
		findbaseprob.setSelectedItem(IBioSimPreferences.INSTANCE.getLearnPreference("biosim.learn.findbaseprob"));

		JPanel learnLabels = new JPanel(new GridLayout(13, 1));
		learnLabels.add(new JLabel("Minimum Number Of Initial Vectors (Tn):"));
		learnLabels.add(new JLabel("Maximum Influence Vector Size (Tj):"));
		learnLabels.add(new JLabel("Score For Empty Influence Vector (Ti):"));
		learnLabels.add(new JLabel("Number Of Bins:"));
		learnLabels.add(new JLabel("Divide Bins:"));
		learnLabels.add(new JLabel("Generate Levels:"));
		learnLabels.add(new JLabel("Ratio For Activation (Ta):"));
		learnLabels.add(new JLabel("Ratio For Repression (Tr):"));
		learnLabels.add(new JLabel("Merge Influence Vectors Delta (Tm):"));
		learnLabels.add(new JLabel("Relax Thresholds Delta (Tt):"));
		learnLabels.add(new JLabel("Debug Level:"));
		learnLabels.add(new JLabel("Successors Or Predecessors:"));
		learnLabels.add(new JLabel("Basic FindBaseProb:"));

		JPanel learnFields = new JPanel(new GridLayout(13, 1));
		learnFields.add(tn);
		learnFields.add(tj);
		learnFields.add(ti);
		learnFields.add(bins);
		learnFields.add(equaldata);
		learnFields.add(autolevels);
		learnFields.add(ta);
		learnFields.add(tr);
		learnFields.add(tm);
		learnFields.add(tt);
		learnFields.add(debug);
		learnFields.add(succpred);
		learnFields.add(findbaseprob);
		
		JButton restoreLearn = new JButton("Restore Defaults");
		restoreLearn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tn.setText("2");
				tj.setText("2");
				ti.setText("0.5");
				bins.setSelectedItem("4");
				equaldata.setSelectedItem("Equal Data Per Bins");
				autolevels.setSelectedItem("Auto");
				ta.setText("1.15");
				tr.setText("0.75");
				tm.setText("0.0");
				tt.setText("0.025");
				debug.setSelectedItem("0");
				succpred.setSelectedItem("Successors");
				findbaseprob.setSelectedItem("False");
			}
		});	
		
		// create learning preferences panel
		JPanel learnPrefs = new JPanel(new GridLayout(1, 2));
		learnPrefs.add(learnLabels);
		learnPrefs.add(learnFields);
		JPanel learnPrefsFinal = new JPanel(new BorderLayout());
		learnPrefsFinal.add(learnPrefs,"North");
		learnPrefsFinal.add(restoreLearn,"South");

		FormBuilder builder = new FormBuilder();
		builder.add("", learnPrefsFinal);

		return builder.build();
	}

	@Override
	public void save() {
		IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.bins", (String) bins.getSelectedItem());
		IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.equaldata", (String) equaldata.getSelectedItem());
		IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.autolevels", (String) autolevels.getSelectedItem());
		IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.debug", (String) debug.getSelectedItem());
		IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.succpred", (String) succpred.getSelectedItem());
		IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.findbaseprob", (String) findbaseprob.getSelectedItem());
		try {
			Integer.parseInt(tn.getText().trim());
			IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.tn", tn.getText().trim());
			Integer.parseInt(tj.getText().trim());
			IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.tj", tj.getText().trim());
			Double.parseDouble(ti.getText().trim());
			IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.ti", ti.getText().trim());
			Double.parseDouble(ta.getText().trim());
			IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.ta", ta.getText().trim());
			Double.parseDouble(tr.getText().trim());
			IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.tr", tr.getText().trim());
			Double.parseDouble(tm.getText().trim());
			IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.tm", tm.getText().trim());
			Double.parseDouble(tt.getText().trim());
			IBioSimPreferences.INSTANCE.setLearnPreference("biosim.learn.tt", tt.getText().trim());
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Numeric learn preference given non-numeric value.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
