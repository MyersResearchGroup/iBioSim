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
package edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.UnitDefinition;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.SBOLAnnotation;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.modelEditor.fba.FBAObjective;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbol.SBOLField2;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.Utils;

/**
 * Construct the Model Editor Panel.
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ModelPanel extends JButton implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JTextField modelID; 
	private JTextField modelName; 

	private JButton fbaoButton;

	private JComboBox<String> substanceUnits, timeUnits, volumeUnits, areaUnits, lengthUnits, extentUnits, conversionFactor;
	private JComboBox<Object> framework;
	
	private JTextField conviIndex;

	private SBOLField2 sbolField;
	
	private BioModel bioModel;
	
	private Model sbmlModel;
	
	private ModelEditor modelEditor;
	
	public ModelPanel(BioModel gcm, ModelEditor modelEditor) {
		super();
		this.bioModel = gcm;
		this.modelEditor = modelEditor;
		sbolField = new SBOLField2(GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 1, true);
		this.sbmlModel = gcm.getSBMLDocument().getModel();
		this.setText("Model");
		this.setToolTipText("Edit Model Attributes");
		this.addActionListener(this);
		this.addActionListener(sbolField);
		if (modelEditor.isParamsOnly()) {
			this.setEnabled(false);
		}
	}
	
	/**
	 * Add the given component to the Model Editor panel in a grid formatted fashion.
	 * @param modelEditorPanel - The panel where the component will be placed on.
	 * @param modelEditorField - the field or value to add to the model editor.
	 * @param rowIndex - The row's index value where the modelEditorField will be placed in. rowIndex must be greater than or equal to 0. The value will grow from top to bottom.
	 * @param colIndex - The col's index value where the modelEditorField will be placed in. colIndex must be greater than or equal to 0. The value will grow from left to right.
	 */
	private void addModelEditor_Field(JPanel modelEditorPanel, java.awt.Component modelEditorField, int rowIndex, int colIndex)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL; // fit the component base on the width of the JPanel
		c.gridx = colIndex;
		c.gridy = rowIndex;
		modelEditorPanel.add(modelEditorField, c);
	}
	
	/**
	 * Set up GUI design layout for Model Editor 
	 * @param option - The JButton label name that will appear as a selection button for the Model Editor panel
	 */
	private void loadModelEditor(String option)
	{
		// Get fields to load onto Model Editor panel
		JPanel modelEditorPanel = new JPanel(new GridBagLayout());
		int labelRow = 0, valueRow = 0, labelCol = 0, valueCol = 1;
		
		Model model = sbmlModel;
		
		JLabel modelIDLabel = new JLabel("Model ID:", JLabel.RIGHT);
		modelID = new JTextField(model.getId()); 
		modelID.setEditable(false);
		addModelEditor_Field(modelEditorPanel, modelIDLabel, labelRow++, labelCol);
		addModelEditor_Field(modelEditorPanel, modelID, valueRow++, valueCol);

		JLabel modelNameLabel = new JLabel("Model Name:", JLabel.RIGHT);
		modelName = new JTextField(model.getName()); 
		addModelEditor_Field(modelEditorPanel, modelNameLabel, labelRow++, labelCol);
		addModelEditor_Field(modelEditorPanel, modelName, valueRow++, valueCol);
		
		conviIndex = new JTextField(20);
		if (bioModel.getSBMLDocument().getLevel() > 2) 
		{
			/* ---------- Create Unit label and get unit values to add to unit combo box ---------- */
			JLabel substanceUnitsLabel = new JLabel("Substance Units:", JLabel.RIGHT);
			substanceUnits = new JComboBox<String>();
			substanceUnits.addItem("( none )");
			
			JLabel timeUnitsLabel = new JLabel("Time Units:", JLabel.RIGHT);
			timeUnits = new JComboBox<String>();
			timeUnits.addItem("( none )");
			
			JLabel volumeUnitsLabel = new JLabel("Volume Units:", JLabel.RIGHT);
			volumeUnits = new JComboBox<String>();
			volumeUnits.addItem("( none )");
			
			JLabel areaUnitsLabel = new JLabel("Area Units:", JLabel.RIGHT);
			areaUnits = new JComboBox<String>();
			areaUnits.addItem("( none )");
			
			JLabel lengthUnitsLabel = new JLabel("Length Units:", JLabel.RIGHT);
			lengthUnits = new JComboBox<String>();
			lengthUnits.addItem("( none )");
			
			JLabel extentUnitsLabel = new JLabel("Extent Units:", JLabel.RIGHT);
			extentUnits = new JComboBox<String>();
			extentUnits.addItem("( none )");
			
			JLabel conversionFactorLabel = new JLabel("Conversion Factor:", JLabel.RIGHT);
			conversionFactor = new JComboBox<String>();
			conversionFactor.addActionListener(this);
			conversionFactor.addItem("( none )");

			for (int i = 0; i < model.getUnitDefinitionCount(); i++) 
			{
				UnitDefinition unit = model.getUnitDefinition(i);
				if ((unit.getUnitCount() == 1)
						&& (unit.getUnit(0).isMole() || unit.getUnit(0).isItem() || unit.getUnit(0).isGram() || unit.getUnit(0).isKilogram())
						&& (unit.getUnit(0).getExponent() == 1)) 
				{
					substanceUnits.addItem(unit.getId());
					extentUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isSecond()) && (unit.getUnit(0).getExponent() == 1)) 
				{
					timeUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponent() == 1)
						|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 3)) 
				{
					volumeUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 2)) 
				{
					areaUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 1)) 
				{
					lengthUnits.addItem(unit.getId());
				}
			}
			
			substanceUnits.addItem("dimensionless");
			substanceUnits.addItem("gram");
			substanceUnits.addItem("item");
			substanceUnits.addItem("kilogram");
			substanceUnits.addItem("mole");
			
			timeUnits.addItem("dimensionless");
			timeUnits.addItem("second");
			
			volumeUnits.addItem("dimensionless");
			volumeUnits.addItem("litre");
			
			areaUnits.addItem("dimensionless");
			
			lengthUnits.addItem("dimensionless");
			lengthUnits.addItem("metre");
			
			extentUnits.addItem("dimensionless");
			extentUnits.addItem("gram");
			extentUnits.addItem("item");
			extentUnits.addItem("kilogram");
			extentUnits.addItem("mole");
			
			/* ---------- SBOL Association Annotation ---------- */
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlModel, sbolURIs);
			sbolField.setSBOLURIs(sbolURIs);
			sbolField.setSBOLStrand(sbolStrand);
			
			for (int i = 0; i < model.getParameterCount(); i++) {
				Parameter param = model.getParameter(i);
				if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
					conversionFactor.addItem(param.getId());
				}
			}
			
			// Set the default selection for all of the combo box
			if (option.equals("OK")) {
				substanceUnits.setSelectedItem(model.getSubstanceUnits());
				timeUnits.setSelectedItem(model.getTimeUnits());
				volumeUnits.setSelectedItem(model.getVolumeUnits());
				areaUnits.setSelectedItem(model.getAreaUnits());
				lengthUnits.setSelectedItem(model.getLengthUnits());
				extentUnits.setSelectedItem(model.getExtentUnits());
				conversionFactor.setSelectedItem(model.getConversionFactor());
				
				String freshIndex = "";
				SBMLutilities.getIndicesString(model, "conversionFactor");
				conviIndex.setText(freshIndex);
			}
			
			fbaoButton = new JButton("Edit Objectives");
			fbaoButton.setActionCommand("fluxObjective");
			fbaoButton.addActionListener(this);
			
			framework = new JComboBox<Object>(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_FRAMEWORK));
			framework.addActionListener(this);
			if (model.isSetSBOTerm()) {
				framework.setSelectedItem(SBMLutilities.sbo.getName(model.getSBOTermID()));
			}
			
			// Insert all labels labels and its value into the Model Editor panel.
			addModelEditor_Field(modelEditorPanel, substanceUnitsLabel, labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, substanceUnits, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, timeUnitsLabel, labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, timeUnits, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, volumeUnitsLabel, labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, volumeUnits, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, areaUnitsLabel, labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, areaUnits, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, lengthUnitsLabel, labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, lengthUnits, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, extentUnitsLabel, labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, extentUnits, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, conversionFactorLabel, labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, conversionFactor, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, new JLabel("Conversion Factor Indices:", JLabel.RIGHT), labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, conviIndex, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, new JLabel("Flux Objective:", JLabel.RIGHT), labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, fbaoButton, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, new JLabel(GlobalConstants.SBOTERM, JLabel.RIGHT), labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, framework, valueRow++, valueCol);
			
			addModelEditor_Field(modelEditorPanel, new JLabel("SBOL ModuleDefinition:", JLabel.RIGHT), labelRow++, labelCol);
			addModelEditor_Field(modelEditorPanel, sbolField, valueRow++, valueCol);
		}
		
		// Display Model Editor panel
		Object[] options = { option, "Cancel" };
		
		int value = JOptionPane.showOptionDialog(Gui.frame, modelEditorPanel, "Model Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value != JOptionPane.YES_OPTION)
			sbolField.resetRemovedBioSimURI();
		String[] dex = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if(!error&&!conversionFactor.getSelectedItem().equals("( none )")){
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)conversionFactor.getSelectedItem());
				dex = Utils.checkIndices(conviIndex.getText(), variable, bioModel.getSBMLDocument(), null, "conversionFactor", 
						null, null, null);
				error = (dex==null);
			}
			// Add SBOL annotation to SBML model 
			if (!error) {
				if (sbolField.getSBOLURIs().size() > 0) {
					if (!sbmlModel.isSetMetaId() || sbmlModel.getMetaId().equals(""))
						SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), sbmlModel, 
								bioModel.getMetaIDIndex());
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlModel.getMetaId(), sbolField.getSBOLURIs(),
							sbolField.getSBOLStrand());
					
					if(!AnnotationUtility.setSBOLAnnotation(sbmlModel, sbolAnnot))
					{
					  JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file", "Error occurred while annotating SBML element "  + SBMLutilities.getId(sbmlModel) + " with SBOL.", JOptionPane.ERROR_MESSAGE); 
					}
					//Update iBioSim model editor id, name and SBO term from the annotated SBOL element
					sbmlModel.setId(sbolField.getSBOLObjID());
					modelName.setText(sbolField.getSBOLObjName());
					framework.setSelectedItem(SBMLutilities.sbo.getName(sbolField.getSBOLObjSBOTerm()));
					
				} else {
					AnnotationUtility.removeSBOLAnnotation(sbmlModel);
				}
				
			}
			if (!error) {
				if (substanceUnits.getSelectedItem().equals("( none )")) {
					model.unsetSubstanceUnits();
				}
				else {
					model.setSubstanceUnits((String) substanceUnits.getSelectedItem());
				}
				if (timeUnits.getSelectedItem().equals("( none )")) {
					model.unsetTimeUnits();
				}
				else {
					model.setTimeUnits((String) timeUnits.getSelectedItem());
				}
				if (volumeUnits.getSelectedItem().equals("( none )")) {
					model.unsetVolumeUnits();
				}
				else {
					model.setVolumeUnits((String) volumeUnits.getSelectedItem());
				}
				if (areaUnits.getSelectedItem().equals("( none )")) {
					model.unsetAreaUnits();
				}
				else {
					model.setAreaUnits((String) areaUnits.getSelectedItem());
				}
				if (lengthUnits.getSelectedItem().equals("( none )")) {
					model.unsetLengthUnits();
				}
				else {
					model.setLengthUnits((String) lengthUnits.getSelectedItem());
				}
				if (extentUnits.getSelectedItem().equals("( none )")) {
					model.unsetExtentUnits();
				}
				else {
					model.setExtentUnits((String) extentUnits.getSelectedItem());
				}
				if (conversionFactor.getSelectedItem().equals("( none )")) {
					model.unsetConversionFactor();
					SBMLutilities.addIndices(model, "conversionFactor", null, 1);
				}
				else {
					model.setConversionFactor((String) conversionFactor.getSelectedItem());
					SBMLutilities.addIndices(model, "conversionFactor", dex, 1);
				}
				if (framework.getSelectedItem().equals("(unspecified)")) {
					model.unsetSBOTerm();
				} else {
					model.setSBOTerm(SBMLutilities.sbo.getId((String)framework.getSelectedItem()));
				}
				model.setName(modelName.getText()); //for reloading purposes
				modelEditor.setDirty(true);
				modelEditor.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, modelEditorPanel, "Model Units Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this) {
			loadModelEditor("OK"); 
		} else if (e.getActionCommand().equals("editDescriptors")) {
			modelEditor.setDirty(true);
		}
		else if (e.getActionCommand().equals("fluxObjective")){
			FBAObjective fbaObjective = new FBAObjective(bioModel);
			fbaObjective.openGui();
			modelEditor.setDirty(true);
		}
		else if (e.getActionCommand().equals("comboBoxChanged")){
			if (conversionFactor.getSelectedItem().equals("( none )")) {
				conviIndex.setText("");
				conviIndex.setEnabled(false);
			} else {
				if (bioModel.isArray((String)conversionFactor.getSelectedItem())) {
					conviIndex.setEnabled(true);
				} else {
					conviIndex.setText("");
					conviIndex.setEnabled(false);
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	public SBOLField2 getSBOLField() {
		return sbolField;
	}
}
