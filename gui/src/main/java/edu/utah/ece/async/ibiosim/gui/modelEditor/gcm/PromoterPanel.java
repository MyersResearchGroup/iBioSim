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
package edu.utah.ece.async.ibiosim.gui.modelEditor.gcm;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.SBOLAnnotation;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbmlcore.MySpecies;
import edu.utah.ece.async.ibiosim.gui.modelEditor.sbol.SBOLField2;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.Utils;
import edu.utah.ece.async.ibiosim.gui.modelEditor.util.PropertyField;
import edu.utah.ece.async.ibiosim.gui.modelEditor.util.PropertyList;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PromoterPanel extends JPanel implements ActionListener {


	private static final long serialVersionUID = 5873800942710657929L;
	private String[] options = { "Ok", "Cancel" };
	private HashMap<String, PropertyField> fields = null;
	private SBOLField2 sbolField;
	private String selected = "";
	private BioModel bioModel = null;
	private boolean paramsOnly;
	private ModelEditor modelEditor = null;
	private Species promoter = null;
	private Reaction production = null;
	private PropertyList speciesList;
	private JComboBox compartBox = null;
	private JTextField iIndex = null;

	public PromoterPanel(String selected, BioModel bioModel, PropertyList speciesList, boolean paramsOnly, BioModel refGCM, 
			ModelEditor modelEditor) {
		super(new GridLayout(paramsOnly?7:13, 1));
		this.selected = selected;
		this.bioModel = bioModel;
		this.paramsOnly = paramsOnly;
		this.modelEditor = modelEditor;
		this.speciesList = speciesList;

		fields = new HashMap<String, PropertyField>();
		

		Model model = bioModel.getSBMLDocument().getModel();
		promoter = model.getSpecies(selected);

		PropertyField field  = null;
		// ID field
		String dimInID = SBMLutilities.getDimensionString(promoter);
		field = new PropertyField(GlobalConstants.ID, promoter.getId() + dimInID, null, null, Utility.IDDimString, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		if (!paramsOnly) add(field);		
		// Name field
		field = new PropertyField(GlobalConstants.NAME, promoter.getName(), null, null, Utility.NAMEstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.NAME, field);
		if (!paramsOnly) add(field);	
			
		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel(GlobalConstants.PORTTYPE);
		typeBox = new JComboBox(types);
		//typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		if (!paramsOnly) add(tempPanel);
		if (bioModel.isInput(promoter.getId())) {
			typeBox.setSelectedItem(GlobalConstants.INPUT);
		} else if (bioModel.isOutput(promoter.getId())) {
			typeBox.setSelectedItem(GlobalConstants.OUTPUT);
		} else {
			typeBox.setSelectedItem(GlobalConstants.INTERNAL);
		}
		production = bioModel.getProductionReaction(selected);
		
		// compartment field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Compartment");
		compartBox = MySpecies.createCompartmentChoices(bioModel);		
		compartBox.setSelectedItem(promoter.getCompartment());
		compartBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(compartBox);
		
		if (!paramsOnly) add(tempPanel);
		
		// indices field
		tempPanel = new JPanel(new GridLayout(1, 2));
		iIndex = new JTextField(20);
		String freshIndex = SBMLutilities.getIndicesString(promoter, "compartment");
		iIndex.setText(freshIndex);
		tempPanel.add(new JLabel("Compartment Indices"));
		tempPanel.add(iIndex);
			
		if (!paramsOnly) add(tempPanel);
		
		// promoter count
		String origString = "default";
		String defaultValue = bioModel.getParameter(GlobalConstants.PROMOTER_COUNT_STRING);
		String formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (refGCM.getSBMLDocument().getModel().getSpecies(promoter.getId()).getInitialAmount() != 
					model.getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue()) {
				defaultValue = ""+refGCM.getSBMLDocument().getModel().getSpecies(promoter.getId()).getInitialAmount();
				origString = "custom";
			}
			formatString = Utility.SWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.PROMOTER_COUNT_STRING, 
				bioModel.getParameter(GlobalConstants.PROMOTER_COUNT_STRING), origString, 
				defaultValue, formatString, paramsOnly, origString, false);
		String sweep = AnnotationUtility.parseSweepAnnotation(promoter);
		if (sweep != null) {		
			field.setValue(sweep);
			field.setCustom();
		} else if (!defaultValue.equals(""+promoter.getInitialAmount())) {
			field.setValue(""+promoter.getInitialAmount());
			field.setCustom();
		}
		fields.put(GlobalConstants.PROMOTER_COUNT_STRING, field);
		add(field);	

		// RNAP binding
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING) + "/" + 
				bioModel.getParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			/*
			defaultValue = refGCM.getParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING)+"/"+
					refGCM.getParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING); */
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter ko_f = refProd.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
				LocalParameter ko_r = refProd.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
				if (ko_f != null && ko_r != null) {
					defaultValue = ko_f.getValue()+"/"+ko_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.RNAP_BINDING_STRING, defaultValue, origString, defaultValue, formatString, paramsOnly,
				origString, false);
		if (production != null) {
			LocalParameter ko_f = production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			LocalParameter ko_r = production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			sweep = AnnotationUtility.parseSweepAnnotation(ko_f);
			if (sweep != null) {		
				field.setValue(sweep);
				field.setCustom();
			} else if (ko_f != null && ko_r != null && !defaultValue.equals(ko_f.getValue()+"/"+ko_r.getValue())) {
				field.setValue(ko_f.getValue()+"/"+ko_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.RNAP_BINDING_STRING, field);
		add(field);
		
		// Activated RNAP binding
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING) + "/" + 
				bioModel.getParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			/*
			defaultValue = refGCM.getParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING) + "/" + 
					refGCM.getParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING); */
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter kao_f = refProd.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
				LocalParameter kao_r = refProd.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
				if (kao_f != null && kao_r != null) {
					defaultValue =  kao_f.getValue()+"/"+kao_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, defaultValue, origString, defaultValue, 
				formatString, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter kao_f = production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
			LocalParameter kao_r = production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
			sweep = AnnotationUtility.parseSweepAnnotation(kao_f);
			if (sweep != null) {		
				field.setValue(sweep);
				field.setCustom();
			} else if (kao_f != null && kao_r != null && !defaultValue.equals(kao_f.getValue()+"/"+kao_r.getValue())) {
				field.setValue(kao_f.getValue()+"/"+kao_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, field);
		add(field);
		
		// kocr
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.OCR_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			//defaultValue = refGCM.getParameter(GlobalConstants.OCR_STRING);
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter ko = refProd.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
				if (ko != null) {
					defaultValue = ko.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.OCR_STRING, bioModel.getParameter(GlobalConstants.OCR_STRING),
				origString, defaultValue, formatString, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter ko = production.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
			sweep = AnnotationUtility.parseSweepAnnotation(ko);
			if (sweep != null) {		
				field.setValue(sweep);
				field.setCustom();
			} else if (ko != null && !defaultValue.equals(ko.getValue()+"")) {
				field.setValue(ko.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.OCR_STRING, field);
		add(field);
		
		// kbasal
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.KBASAL_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter kb = refProd.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING);
				if (kb != null) {
					defaultValue = kb.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.KBASAL_STRING, bioModel.getParameter(GlobalConstants.KBASAL_STRING),
				origString, defaultValue, Utility.SWEEPstring, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter kb = production.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING);
			sweep = AnnotationUtility.parseSweepAnnotation(kb);
			if (sweep != null) {		
				field.setValue(sweep);
				field.setCustom();
			} else if (kb != null && !defaultValue.equals(kb.getValue()+"")) {
				field.setValue(kb.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.KBASAL_STRING, field);
		add(field);
		
		// kactived production
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.ACTIVATED_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter ka = refProd.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING);
				if (ka != null) {
					defaultValue = ka.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.ACTIVATED_STRING, bioModel.getParameter(GlobalConstants.ACTIVATED_STRING),
				origString, defaultValue, formatString, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter ka = production.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING);
			sweep = AnnotationUtility.parseSweepAnnotation(ka);
			if (sweep != null) {		
				field.setValue(sweep);
				field.setCustom();
			} else if (ka != null && !defaultValue.equals(ka.getValue()+"")) {
				field.setValue(ka.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.ACTIVATED_STRING, field);
		add(field);
		
		// stoichiometry
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.STOICHIOMETRY_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter np = refProd.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
				if (np != null) {
					defaultValue = np.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, bioModel.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
				origString, defaultValue, formatString, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter np = production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
			sweep = AnnotationUtility.parseSweepAnnotation(np);
			if (sweep != null) {		
				field.setValue(sweep);
				field.setCustom();
			} else if (np != null && !defaultValue.equals(np.getValue()+"")) {
				field.setValue(np.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.STOICHIOMETRY_STRING, field);
		add(field);		
		
		// Parse out SBOL annotations and add to SBOL field
		if (!paramsOnly) {
			
			// Field for annotating promoter with SBOL DNA components
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(production, sbolURIs);
			
			if (sbolURIs.size()>0 && sbolField.getSBOLObjSBOTerm().equals(GlobalConstants.SBO_DNA_SEGMENT)) 
			{
				SBOLAnnotation sbolAnnot = new SBOLAnnotation(selected, sbolURIs, sbolStrand);
				sbolAnnot.createSBOLElementsDescription(GlobalConstants.SBOL_COMPONENTDEFINITION, 
						sbolField.getSBOLURIs().iterator().next()); 
				if(!AnnotationUtility.setSBOLAnnotation(promoter, sbolAnnot))
				{
					JOptionPane.showMessageDialog(Gui.frame, 
							"Invalid XML in SBML file", 
							"Error occurred while annotating SBML element "  + SBMLutilities.getId(promoter) + " with SBOL.", 
							JOptionPane.ERROR_MESSAGE); 
				}
				else
				{
					AnnotationUtility.removeSBOLAnnotation(production);
				}
			} 
			else 
			{
				sbolStrand = AnnotationUtility.parseSBOLAnnotation(promoter, sbolURIs);
			}
			sbolField = new SBOLField2(sbolURIs, sbolStrand, GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 
					3, false);
			
			add(sbolField);
		}

		String oldName = null;
		oldName = selected;

		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}
	
	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this,
				"Promoter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			boolean valueCheck = checkValues();
			if (!valueCheck) {
			  JOptionPane.showMessageDialog(Gui.frame, "Illegal values entered.", "Error", JOptionPane.ERROR_MESSAGE); 
				return false;
			}
			String[] idDims = new String[]{""};
			String[] dimensionIds = new String[]{""};
			String[] dex = new String[]{""};
			idDims = Utils.checkSizeParameters(bioModel.getSBMLDocument(), 
					fields.get(GlobalConstants.ID).getValue(), false);
			if(idDims==null)return false;
			dimensionIds = SBMLutilities.getDimensionIds("",idDims.length-1);
			String id = selected;
			
			if (!paramsOnly) {
				if (oldName == null) {
					if (bioModel.isSIdInUse(idDims[0])) {
					  JOptionPane.showMessageDialog(Gui.frame, "Id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
						return false;
					}
				}
				else if (!oldName.equals(idDims[0])) {
					if (bioModel.isSIdInUse(idDims[0])) {
					  JOptionPane.showMessageDialog(Gui.frame, "Id already exists.", "Error", JOptionPane.ERROR_MESSAGE); 
            
						return false;
					}
				}
				
				id = idDims[0];
				promoter.setName(fields.get(GlobalConstants.NAME).getValue());
				SBMLutilities.createDimensions(promoter, dimensionIds, idDims);
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)compartBox.getSelectedItem());
				dex = Utils.checkIndices(iIndex.getText(), variable, bioModel.getSBMLDocument(), dimensionIds, "compartment", idDims, null, null);
				if(dex==null)return false;
				SBMLutilities.addIndices(promoter, "compartment", dex, 1);
				SBMLutilities.addIndices(production, "compartment", dex, 1);
			}
			promoter.setCompartment((String)compartBox.getSelectedItem());
			production.setCompartment((String)compartBox.getSelectedItem());
			String speciesType = typeBox.getSelectedItem().toString();
			boolean onPort = (speciesType.equals(GlobalConstants.INPUT)||speciesType.equals(GlobalConstants.OUTPUT));
			
			promoter.setSBOTerm(GlobalConstants.SBO_PROMOTER_BINDING_REGION);
			PropertyField f = fields.get(GlobalConstants.PROMOTER_COUNT_STRING);
			if (f.getValue().startsWith("(")) {
				promoter.setInitialAmount(1.0);
				if(!AnnotationUtility.setSweepAnnotation(promoter, f.getValue()))
	      {
	        JOptionPane.showMessageDialog(Gui.frame, "Invalid XML Operation", "Error occurred while annotating SBML element " 
	            + SBMLutilities.getId(promoter), JOptionPane.ERROR_MESSAGE); 
	        
	      }
			} else {
				promoter.setInitialAmount(Double.parseDouble(f.getValue()));
			}
			String kaStr = null;
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.STOICHIOMETRY_STRING);
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.OCR_STRING);
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.KBASAL_STRING);
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.ACTIVATED_STRING);
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
			production.getKineticLaw().getListOfLocalParameters().remove(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
			f = fields.get(GlobalConstants.ACTIVATED_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				kaStr = f.getValue();
			}
			String npStr = null;
			f = fields.get(GlobalConstants.STOICHIOMETRY_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				npStr = f.getValue();
			}
			String koStr = null;
			f = fields.get(GlobalConstants.OCR_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				koStr = f.getValue();
			}
			String kbStr = null;
			f = fields.get(GlobalConstants.KBASAL_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				kbStr = f.getValue();
			}
			String KoStr = null;
			f = fields.get(GlobalConstants.RNAP_BINDING_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				KoStr = f.getValue();
			}
			String KaoStr = null;
			f = fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				KaoStr = f.getValue();
			}
			bioModel.createProductionReaction(selected,kaStr,npStr,koStr,kbStr,KoStr,KaoStr,onPort,idDims);

			if (!paramsOnly) {
				// Add SBOL annotation to promoter
				if (sbolField.getSBOLURIs().size() > 0) 
				{
					if(sbolField.isSBOLSBOSet() && !sbolField.getSBOLObjSBOTerm().equals(GlobalConstants.SBO_DNA_SEGMENT))
					{
						JOptionPane.showMessageDialog(Gui.frame, 
								"You are not allowed to annotate a promoter that does not have SBOL component type DNA.", 
								"Prohibited SBOL Annotation Type",
								JOptionPane.WARNING_MESSAGE); 
					}
					else
					{
						if (!production.isSetMetaId() || production.getMetaId().equals(""))
							SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), production, 
									bioModel.getMetaIDIndex());
						try 
						{
							bioModel.changePromoterId(id, SBMLutilities.getUniqueSBMLId(sbolField.getSBOLObjID(), bioModel));
							setSBOLAnnotation(promoter);
						} 
						catch (BioSimException e) 
						{
							JOptionPane.showMessageDialog(Gui.frame, e.getTitle(), e.getMessage(), 
									JOptionPane.ERROR_MESSAGE);
						}
					}

				} else
					AnnotationUtility.removeSBOLAnnotation(promoter);

				// rename all the influences that use this promoter if name was changed
				if (selected != null && oldName != null && !oldName.equals(id)) {
					try {
						bioModel.changePromoterId(oldName, id);
					} catch (BioSimException e) {
						JOptionPane.showMessageDialog(Gui.frame, e.getTitle(), e.getMessage(), 
								JOptionPane.ERROR_MESSAGE);
					}
					this.secondToLastUsedPromoter = oldName;
					promoterNameChange = true;
				}
				bioModel.createDirPort(promoter.getId(),speciesType);
				this.lastUsedPromoter = id;
			}
			speciesList.removeItem(selected);
			speciesList.removeItem(selected + " Modified");
			speciesList.addItem(id);
			speciesList.setSelectedValue(id, true);
	
			modelEditor.refresh();
			modelEditor.setDirty(true);
		} else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}
	
	/**
	 * Perform annotation on the SBML element with the SBOL object that was selected from SBOL association.
	 * @param sbmlElement - The SBML element to set the SBOL annotation on.
	 */
	private void setSBOLAnnotation(SBase sbmlElement)
	{
		SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlElement.getMetaId(), 
				sbolField.getSBOLURIs(), sbolField.getSBOLStrand());
		sbolAnnot.createSBOLElementsDescription(GlobalConstants.SBOL_COMPONENTDEFINITION, 
				sbolField.getSBOLURIs().iterator().next()); 
		if(!AnnotationUtility.setSBOLAnnotation(sbmlElement, sbolAnnot))
		{
			JOptionPane.showMessageDialog(Gui.frame,  
					"Error occurred while annotating SBML element "  + sbmlElement.getId() + " with SBOL.", 
					"Invalid XML in SBML file",
					JOptionPane.ERROR_MESSAGE); 
		}
		
		if(sbolField.isSBOLNameSet())
		{
			sbmlElement.setName(sbolField.getSBOLObjName());
		}
		if(sbolField.isSBOLSBOSet())
		{
			sbmlElement.setSBOTerm(sbolField.getSBOLObjSBOTerm());
		}
	}
	
	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getState().equals(fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getStates()[1])) {
				updates += selected + "/" + GlobalConstants.PROMOTER_COUNT_STRING + " "
						+ fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getValue();
			}
			if (fields.get(GlobalConstants.RNAP_BINDING_STRING).getState()
					.equals(fields.get(GlobalConstants.RNAP_BINDING_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.RNAP_BINDING_STRING + " "
						+ fields.get(GlobalConstants.RNAP_BINDING_STRING).getValue();
			}
			if (fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getState()
					.equals(fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.ACTIVATED_RNAP_BINDING_STRING + " "
						+ fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getValue();
			}
			if (fields.get(GlobalConstants.OCR_STRING).getState().equals(fields.get(GlobalConstants.OCR_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.OCR_STRING + " "
						+ fields.get(GlobalConstants.OCR_STRING).getValue();
			}
			if (fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(fields.get(GlobalConstants.STOICHIOMETRY_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.STOICHIOMETRY_STRING + " "
						+ fields.get(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KBASAL_STRING).getState().equals(fields.get(GlobalConstants.KBASAL_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.KBASAL_STRING + " "
						+ fields.get(GlobalConstants.KBASAL_STRING).getValue();
			}
			if (fields.get(GlobalConstants.ACTIVATED_STRING).getState().equals(fields.get(GlobalConstants.ACTIVATED_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.ACTIVATED_STRING + " "
						+ fields.get(GlobalConstants.ACTIVATED_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += selected + "/";
			}
		}
		return updates;
	}
	
	// Provide a public way to query what the last used (or created) promoter was.
	private String lastUsedPromoter;
	public String getLastUsedPromoter(){return lastUsedPromoter;}
	
	private String secondToLastUsedPromoter;
	public String getSecondToLastUsedPromoter(){return secondToLastUsedPromoter;}
	
	private boolean promoterNameChange = false;
	public boolean wasPromoterNameChanged(){return promoterNameChange;}
	
	private static final String[] types = new String[] { GlobalConstants.INPUT, GlobalConstants.INTERNAL, 
		GlobalConstants.OUTPUT};

	private JComboBox typeBox = null;

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == compartBox){
			if (bioModel.isArray((String)compartBox.getSelectedItem())) {
				iIndex.setEnabled(true);
			} else {
				iIndex.setText("");
				iIndex.setEnabled(false);
			}
		}
	}
	
}