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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.CompatibilityFixer;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum ModelPreferencesTab implements PreferencesTab {
	INSTANCE;
	
	private JCheckBox Undeclared, Units;
	
	private JTextField ACTIVED_VALUE;
	private JTextField KACT_VALUE;
	private JTextField KBASAL_VALUE;
	private JTextField KDECAY_VALUE;
	private JTextField KECDECAY_VALUE;
	private JTextField COOPERATIVITY_VALUE;
	private JTextField RNAP_VALUE;
	private JTextField PROMOTER_COUNT_VALUE;
	private JTextField OCR_VALUE;
	private JTextField RNAP_BINDING_VALUE;
	private JTextField ACTIVATED_RNAP_BINDING_VALUE;
	private JTextField KREP_VALUE;
	private JTextField STOICHIOMETRY_VALUE;
	private JTextField KCOMPLEX_VALUE;
	private JTextField FORWARD_MEMDIFF_VALUE;
	private JTextField REVERSE_MEMDIFF_VALUE;
	private JTextField KECDIFF_VALUE;
	
	private boolean async = false;

	@Override
	public String getTitle() {
		return "Model";
	}

	@Override
	public String getDescription() {
		return "Default Model Parameters";
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.getImageIcon("dot.jpg");
	}

	@Override
	public Component getComponent() {
		// model preferences
		Undeclared = new JCheckBox("Check for undeclared units in SBML");
		if (IBioSimPreferences.INSTANCE.getModelPreference("biosim.check.undeclared").equals("false")) {
			Undeclared.setSelected(false);
		}
		else {
			Undeclared.setSelected(true);
		}
		Units = new JCheckBox("Check units in SBML");
		if (IBioSimPreferences.INSTANCE.getModelPreference("biosim.check.units").equals("false")) {
			Units.setSelected(false);
		}
		else {
			Units.setSelected(true);
		}

		ACTIVED_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.ACTIVED_VALUE"));
		KACT_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.KACT_VALUE"));
		KBASAL_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.KBASAL_VALUE"));
		KDECAY_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.KDECAY_VALUE"));
		KECDECAY_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.KECDECAY_VALUE"));
		COOPERATIVITY_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.COOPERATIVITY_VALUE"));
		RNAP_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.RNAP_VALUE"));
		PROMOTER_COUNT_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.PROMOTER_COUNT_VALUE"));
		OCR_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.OCR_VALUE"));
		RNAP_BINDING_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.RNAP_BINDING_VALUE"));
		ACTIVATED_RNAP_BINDING_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE"));
		KREP_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.KREP_VALUE"));
		STOICHIOMETRY_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.STOICHIOMETRY_VALUE"));
		KCOMPLEX_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.KCOMPLEX_VALUE"));
		FORWARD_MEMDIFF_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.FORWARD_MEMDIFF_VALUE"));
		REVERSE_MEMDIFF_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.REVERSE_MEMDIFF_VALUE"));
		KECDIFF_VALUE = new JTextField(IBioSimPreferences.INSTANCE.getModelPreference("biosim.gcm.KECDIFF_VALUE"));

		JPanel labels = new JPanel(new GridLayout(18, 1));
		labels.add(Undeclared);
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.ACTIVATED_STRING) + " (" + GlobalConstants.ACTIVATED_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KACT_STRING) + " (" + GlobalConstants.KACT_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KBASAL_STRING) + " (" + GlobalConstants.KBASAL_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KDECAY_STRING) + " (" + GlobalConstants.KDECAY_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KECDECAY_STRING) + " (" + GlobalConstants.KECDECAY_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.COOPERATIVITY_STRING) + " (" + GlobalConstants.COOPERATIVITY_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.RNAP_STRING) + " (" + GlobalConstants.RNAP_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.PROMOTER_COUNT_STRING) + " (" + GlobalConstants.PROMOTER_COUNT_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.OCR_STRING) + " (" + GlobalConstants.OCR_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.RNAP_BINDING_STRING) + " (" + GlobalConstants.RNAP_BINDING_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING) + " ("
				+ GlobalConstants.ACTIVATED_RNAP_BINDING_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KREP_STRING) + " (" + GlobalConstants.KREP_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.STOICHIOMETRY_STRING) + " (" + GlobalConstants.STOICHIOMETRY_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KCOMPLEX_STRING) + " (" + GlobalConstants.KCOMPLEX_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.FORWARD_MEMDIFF_STRING) + " (" + GlobalConstants.FORWARD_MEMDIFF_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.REVERSE_MEMDIFF_STRING) + " (" + GlobalConstants.REVERSE_MEMDIFF_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KECDIFF_STRING) + " (" + GlobalConstants.KECDIFF_STRING + "):"));

		JPanel fields = new JPanel(new GridLayout(18, 1));
		fields.add(Units);
		fields.add(ACTIVED_VALUE);
		fields.add(KACT_VALUE);
		fields.add(KBASAL_VALUE);
		fields.add(KDECAY_VALUE);
		fields.add(KECDECAY_VALUE);
		fields.add(COOPERATIVITY_VALUE);
		fields.add(RNAP_VALUE);
		fields.add(PROMOTER_COUNT_VALUE);
		fields.add(OCR_VALUE);
		fields.add(RNAP_BINDING_VALUE);
		fields.add(ACTIVATED_RNAP_BINDING_VALUE);
		fields.add(KREP_VALUE);
		fields.add(STOICHIOMETRY_VALUE);
		fields.add(KCOMPLEX_VALUE);
		fields.add(FORWARD_MEMDIFF_VALUE);
		fields.add(REVERSE_MEMDIFF_VALUE);
		fields.add(KECDIFF_VALUE);
		
		JButton restoreModel = new JButton("Restore Defaults");
		restoreModel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Undeclared.setSelected(true);
				Units.setSelected(true);
				KREP_VALUE.setText(".5");
				KACT_VALUE.setText(".0033");
				PROMOTER_COUNT_VALUE.setText("2");
				KBASAL_VALUE.setText(".0001");
				OCR_VALUE.setText(".05");
				KDECAY_VALUE.setText(".0075");
				KECDECAY_VALUE.setText(".005");
				RNAP_VALUE.setText("30");
				RNAP_BINDING_VALUE.setText(".033");
				ACTIVATED_RNAP_BINDING_VALUE.setText("1");
				STOICHIOMETRY_VALUE.setText("10");
				KCOMPLEX_VALUE.setText("0.05");
				COOPERATIVITY_VALUE.setText("2");
				ACTIVED_VALUE.setText(".25");
				FORWARD_MEMDIFF_VALUE.setText("1.0");
				REVERSE_MEMDIFF_VALUE.setText("0.01");
				KECDIFF_VALUE.setText("1.0");
			}
		});	
		// create model preferences panel
		JPanel modelPrefs = new JPanel(new GridLayout(1, 2));
		if (async) {
			modelPrefs.add(Undeclared);
			modelPrefs.add(Units);
		} else {
			modelPrefs.add(labels);
			modelPrefs.add(fields);
		}
		JPanel modelPrefsFinal = new JPanel(new BorderLayout());
		modelPrefsFinal.add(modelPrefs,"North");
		modelPrefsFinal.add(restoreModel,"South");
		
		FormBuilder builder = new FormBuilder();
		builder.add("", modelPrefsFinal);

		return builder.build();
	}

	@Override
	public boolean save() {
		if (Undeclared.isSelected()) {
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.check.undeclared", "true");
		}
		else {
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.check.undeclared", "false");
		}
		if (Units.isSelected()) {
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.check.units", "true");
		}
		else {
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.check.units", "false");
		}
		try {
			Double.parseDouble(KREP_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.KREP_VALUE", KREP_VALUE.getText().trim());
			Double.parseDouble(KACT_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.KACT_VALUE", KACT_VALUE.getText().trim());
			Double.parseDouble(PROMOTER_COUNT_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.PROMOTER_COUNT_VALUE", PROMOTER_COUNT_VALUE.getText().trim());
			Double.parseDouble(KBASAL_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.KBASAL_VALUE", KBASAL_VALUE.getText().trim());
			Double.parseDouble(OCR_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.OCR_VALUE", OCR_VALUE.getText().trim());
			Double.parseDouble(KDECAY_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.KDECAY_VALUE", KDECAY_VALUE.getText().trim());
			Double.parseDouble(KECDECAY_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.KECDECAY_VALUE", KECDECAY_VALUE.getText().trim());
			Double.parseDouble(RNAP_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.RNAP_VALUE", RNAP_VALUE.getText().trim());
			Double.parseDouble(RNAP_BINDING_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.RNAP_BINDING_VALUE", RNAP_BINDING_VALUE.getText().trim());
			Double.parseDouble(ACTIVATED_RNAP_BINDING_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ACTIVATED_RNAP_BINDING_VALUE.getText().trim());
			Double.parseDouble(STOICHIOMETRY_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.STOICHIOMETRY_VALUE", STOICHIOMETRY_VALUE.getText().trim());
			Double.parseDouble(KCOMPLEX_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.KCOMPLEX_VALUE", KCOMPLEX_VALUE.getText().trim());
			Double.parseDouble(COOPERATIVITY_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.COOPERATIVITY_VALUE", COOPERATIVITY_VALUE.getText().trim());
			Double.parseDouble(ACTIVED_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.ACTIVED_VALUE", ACTIVED_VALUE.getText().trim());
			String[] fdrv = FORWARD_MEMDIFF_VALUE.getText().trim().split("/");
			// if the user specifies a forward and reverse rate
			if (fdrv.length == 2) {
				IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.FORWARD_MEMDIFF_VALUE", fdrv[0]);
			}
			else if (fdrv.length == 1) {
				Double.parseDouble(FORWARD_MEMDIFF_VALUE.getText().trim());
				IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.FORWARD_MEMDIFF_VALUE", FORWARD_MEMDIFF_VALUE.getText().trim());
			}
			fdrv = REVERSE_MEMDIFF_VALUE.getText().trim().split("/");
			// if the user specifies a forward and reverse rate
			if (fdrv.length == 2) {
				IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.MEMDIFF_VALUE", fdrv[1]);
			}
			else if (fdrv.length == 1) {
				Double.parseDouble(REVERSE_MEMDIFF_VALUE.getText().trim());
				IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.MEMDIFF_VALUE", REVERSE_MEMDIFF_VALUE.getText().trim());
			}
			Double.parseDouble(KECDIFF_VALUE.getText().trim());
			IBioSimPreferences.INSTANCE.setModelPreference("biosim.gcm.KECDIFF_VALUE", KECDIFF_VALUE.getText().trim());
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Numeric model preference given non-numeric value.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
