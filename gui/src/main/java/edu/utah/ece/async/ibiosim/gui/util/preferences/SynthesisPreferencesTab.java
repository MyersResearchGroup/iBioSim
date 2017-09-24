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

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.ibiosim.gui.util.preferences.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum SynthesisPreferencesTab implements PreferencesTab {
	INSTANCE;
	
	private JTextField regexField;
	private JComboBox validationBox;
	private JComboBox assemblyBox;
	private JComboBox warningBox;

	@Override
	public String getTitle() {
		return "Synthesis";
	}

	@Override
	public String getDescription() {
		return "Default Synthesis Settings";
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.getImageIcon("synth.png");
	}

	@Override
	public Component getComponent() {
		// assembly preferences
		JPanel assemblyLabels = new JPanel(new GridLayout(4, 1));
		assemblyLabels.add(new JLabel("Assemble Complete Genetic Construct"));
		assemblyLabels.add(new JLabel("Regex for Complete Genetic Construct"));
		assemblyLabels.add(new JLabel("Validate Assembled Constructs"));
		assemblyLabels.add(new JLabel("Incomplete Construct Warning"));
		
		JPanel assemblyFields = new JPanel(new GridLayout(4 ,1));
		String regex = SBOLUtility.convertRegexSOTermsToNumbers(
				IBioSimPreferences.INSTANCE.getSynthesisPreference(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE));
		regexField = new JTextField(regex, 15);
		assemblyBox = new JComboBox(new String[]{"True", "False"});
		assemblyBox.setSelectedItem(IBioSimPreferences.INSTANCE.getSynthesisPreference(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE));
		assemblyBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (assemblyBox.getSelectedIndex() == 0) {
					regexField.setEnabled(true);
					validationBox.setSelectedIndex(0);
					validationBox.setEnabled(true);
					warningBox.setSelectedIndex(0);
					warningBox.setEnabled(true);
				} else {
					regexField.setEnabled(false);
					validationBox.setSelectedIndex(1);
					validationBox.setEnabled(false);
					warningBox.setSelectedIndex(1);
					warningBox.setEnabled(false);
				}
			}
		});
		validationBox = new JComboBox(new String[]{"True", "False"});
		validationBox.setSelectedItem(IBioSimPreferences.INSTANCE.getSynthesisPreference(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE));
		validationBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (validationBox.getSelectedIndex() == 0) {
					warningBox.setSelectedIndex(0);
					warningBox.setEnabled(true);
					warningBox.setSelectedIndex(1);
				} else {
					warningBox.setSelectedIndex(1);
					warningBox.setSelectedIndex(1);
					warningBox.setEnabled(false);
				}
			}
		});
		warningBox = new JComboBox(new String[]{"True", "False"});
		warningBox.setSelectedItem(IBioSimPreferences.INSTANCE.getSynthesisPreference(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE));
		assemblyFields.add(assemblyBox);
		assemblyFields.add(regexField);
		assemblyFields.add(validationBox);
		assemblyFields.add(warningBox);
		
		JButton restoreDefaultsButton = new JButton("Restore Defaults");
		restoreDefaultsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				regexField.setText(GlobalConstants.GENETIC_CONSTRUCT_REGEX_DEFAULT);
				assemblyBox.setSelectedItem(GlobalConstants.CONSTRUCT_ASSEMBLY_DEFAULT);
				validationBox.setSelectedItem(GlobalConstants.CONSTRUCT_VALIDATION_DEFAULT);
				warningBox.setSelectedItem(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_DEFAULT);
			}
		});	
		
		// create assembly preferences panel
		JPanel assemblyPrefsPane = new JPanel(new GridLayout(1, 2));
		assemblyPrefsPane.add(assemblyLabels);
		assemblyPrefsPane.add(assemblyFields);
		JPanel assemblyPrefsTop = new JPanel(new BorderLayout());
		assemblyPrefsTop.add(assemblyPrefsPane,"North");
		assemblyPrefsTop.add(restoreDefaultsButton,"South");
		
		FormBuilder builder = new FormBuilder();
		builder.add("", assemblyPrefsTop);

		return builder.build();
	}

	@Override
	public void save() {
		if (!regexField.getText().trim().equals(""))
			IBioSimPreferences.INSTANCE.setSynthesisPreference(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, regexField.getText().trim());
		else {
			JOptionPane.showMessageDialog(Gui.frame, "Validation regex cannot be blank.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
			regexField.setText(IBioSimPreferences.INSTANCE.getSynthesisPreference(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE));
		}
		if (assemblyBox.getSelectedItem().equals("True"))
			IBioSimPreferences.INSTANCE.setSynthesisPreference(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, "True");
		else
			IBioSimPreferences.INSTANCE.setSynthesisPreference(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, "False");
		if (validationBox.getSelectedItem().equals("True"))
			IBioSimPreferences.INSTANCE.setSynthesisPreference(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "True");
		else
			IBioSimPreferences.INSTANCE.setSynthesisPreference(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "False");
		if (warningBox.getSelectedItem().equals("True"))
			IBioSimPreferences.INSTANCE.setSynthesisPreference(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, "True");
		else
			IBioSimPreferences.INSTANCE.setSynthesisPreference(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, "False");
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
