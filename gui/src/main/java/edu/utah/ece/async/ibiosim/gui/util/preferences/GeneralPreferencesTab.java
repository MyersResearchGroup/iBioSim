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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum GeneralPreferencesTab implements PreferencesTab {
	INSTANCE;

	private JCheckBox dialog = new JCheckBox("Use File Dialog",IBioSimPreferences.INSTANCE.isFileDialogEnabled());
	private JCheckBox icons = new JCheckBox("Use Plus/Minus For Expanding/Collapsing File Tree",
			IBioSimPreferences.INSTANCE.isPlusMinusIconsEnabled());
	private JCheckBox delete = new JCheckBox("Do Not Confirm File Deletions",
			IBioSimPreferences.INSTANCE.isNoConfirmEnabled());
	private JCheckBox libsbmlFlatten = new JCheckBox("Use libsbml to Flatten Models",
			IBioSimPreferences.INSTANCE.isLibSBMLFlattenEnabled());
	private JCheckBox showWarnings = new JCheckBox("Report Validation Warnings",
			IBioSimPreferences.INSTANCE.isWarningsEnabled());	
	

	private JRadioButton libsbmlValidate = new JRadioButton("Use libsbml to Validate Models",
			IBioSimPreferences.INSTANCE.isLibSBMLValidateEnabled());
	private JRadioButton jsbmlValidate = new JRadioButton("Use JSBML to Validate Models",
			IBioSimPreferences.INSTANCE.isJSBMLValidateEnabled());
	private JRadioButton webValidate = new JRadioButton("Use Webservice to Validate Models",
	  !libsbmlValidate.isSelected() && !jsbmlValidate.isSelected());
  
	private JLabel xhtmlCmdLabel = new JLabel("Browser Viewer Command");
	private JTextField xhtmlCmd = new JTextField(IBioSimPreferences.INSTANCE.getXhtmlCmd());
	private JLabel prismCmdLabel = new JLabel("PRISM Model Checking Command");
	private JTextField prismCmd = new JTextField(IBioSimPreferences.INSTANCE.getPrismCmd());
	private JLabel dotCmdLabel = new JLabel("Graphviz Viewer Command");
	private JTextField dotCmd = new JTextField(IBioSimPreferences.INSTANCE.getGraphvizCmd());

	public void groupValidationTypes()
	{
	  ButtonGroup validationGroup = new ButtonGroup();
	  validationGroup.add(libsbmlValidate);
	  validationGroup.add(jsbmlValidate);
	  validationGroup.add(webValidate);
	}
	
	@Override
	public String getTitle() {
		return "General";
	}

	@Override
	public String getDescription() {
		return "General Preferences";
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.getImageIcon("general.png");
	}

	@Override
	public Component getComponent() {
	  groupValidationTypes();
		FormBuilder builder = new FormBuilder();
		builder.add("", dialog);
		builder.add("", icons);
		builder.add("", delete);
		builder.add("", libsbmlFlatten);
		builder.add("", libsbmlValidate);
		builder.add("", jsbmlValidate);
    builder.add("", webValidate);
		builder.add("", showWarnings);
		builder.add("", xhtmlCmdLabel);
		builder.add("", xhtmlCmd);
		builder.add("", dotCmdLabel);
		builder.add("", dotCmd);
		builder.add("", prismCmdLabel);
		builder.add("", prismCmd);

		return builder.build();
	}

	@Override
	public void save() {
		IBioSimPreferences.INSTANCE.setFileDialogEnabled(dialog.isSelected());
		IBioSimPreferences.INSTANCE.setPlusMinusIconsEnabled(icons.isSelected()); 
		IBioSimPreferences.INSTANCE.setNoConfirmEnabled(delete.isSelected());
		IBioSimPreferences.INSTANCE.setLibSBMLFlattenEnabled(libsbmlFlatten.isSelected());
		IBioSimPreferences.INSTANCE.setValidateEnabled(libsbmlValidate.isSelected(), jsbmlValidate.isSelected());
		IBioSimPreferences.INSTANCE.setWarningsEnabled(showWarnings.isSelected());
		IBioSimPreferences.INSTANCE.setXhtmlCmd(xhtmlCmd.getText());
		IBioSimPreferences.INSTANCE.setGraphvizCmd(dotCmd.getText());
		IBioSimPreferences.INSTANCE.setPrismCmd(prismCmd.getText());
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
