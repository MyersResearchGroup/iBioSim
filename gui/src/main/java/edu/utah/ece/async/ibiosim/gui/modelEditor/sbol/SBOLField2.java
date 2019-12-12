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
package edu.utah.ece.async.ibiosim.gui.modelEditor.sbol;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PartEditDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryInputDialog;


/**
 * SBOL Association Panel. This class will allow user to add/edit or remove SBOL association to SBML elements. 
 * 
 * @author Tramy Nguyen
 * @author Nicholas Roehner
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLField2 extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private int styleOption; //The number of columns to create for the UI components.
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private List<URI> sbolURIs = new LinkedList<URI>();
	private String sbolStrand;
	private JButton edit_sbolButton, remove_sbolButton;
	private ModelEditor modelEditor; 
	private URI removedBioSimURI;
	
	//SBOL object fields that are being associated
	private String associateObjName, associatedObjSBO;
	
	private String sbolType;
	boolean isComponentDefinition = false;
	boolean isModuleDefinition = false;

	//TODO: not sure what the purpose of this field is for
	private boolean isModelPanelField;
	
	/**
	 * Constructor to give access to SBOL association.
	 * @param sbolURIs - The URI of the SBOL object identity to be annotated
	 * @param sbolStrand - The SBOL sequence to be annotated to the SBOL object.
	 * @param sbolType - The SBOL object type that the user want to annotate the SBML element with. 
	 * @param modelEditor - To get access to SBOL library file.
	 * @param styleOption - Specify 2 or 3 to indicate how many columns the GUI will occupy to insert the SBOL Association add/edit and remove buttons.
	 * @param isModelPanelField - 
	 */
	public SBOLField2(List<URI> sbolURIs, String sbolStrand, String sbolType, ModelEditor modelEditor, int styleOption,
			boolean isModelPanelField) {
		super(new GridLayout(1, styleOption));
		this.sbolURIs.addAll(sbolURIs);
		this.sbolStrand = sbolStrand;
		this.sbolType = sbolType;
		setSBOLAssociate_Type(sbolType);
		constructField(modelEditor, styleOption, isModelPanelField);
	}
	
	/**
	 * Store what type of SBOL object is being annotated for SBOL association.
	 * @param sbolType -The SBOL object type that the user want to annotate the SBML element with. This sbolType is currently limited to ComponentDefinition or ModuleDefinition.
	 */
	private void setSBOLAssociate_Type(String sbolType) 
	{
		if(sbolType.equals(GlobalConstants.SBOL_COMPONENTDEFINITION))
		{
			isComponentDefinition = true;
			isModuleDefinition = false;
		}
		else if(sbolType.equals(GlobalConstants.SBOL_MODULEDEFINITION))
		{
			isComponentDefinition = false;
			isModuleDefinition = true;
		}
	}

	/**
	 * Creates an instance for SBOL Association. 
	 * @param sbolType - The SBOL object type that the user want to annotate the SBML element with. This sbolType is currently limited to ComponentDefinition or ModuleDefinition.
	 * @param modelEditor
	 * @param styleOption - The number of columns to create for the UI components.
	 * @param isModelPanelField
	 */
	public SBOLField2(String sbolType, ModelEditor modelEditor, int styleOption, boolean isModelPanelField){
		super(new GridLayout(1, styleOption));
		sbolStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
		this.sbolType = sbolType;
		constructField(modelEditor, styleOption, isModelPanelField);
		setSBOLAssociate_Type(sbolType);
	}

	/**
	 * Create the following GUI layout for SBOL Association:
	 * 1. Add/Edit SBOL Association
	 * 2. Remove SBOL Association
	 * 
	 * @param modelEditor - The SBML model editor that the sbol association will take place on.
	 * @param styleOption - specify 2 or 3 to set set SBOL association label to SBOL types.
	 * @param isModelPanelField - 
	 */
	public void constructField(ModelEditor modelEditor, int styleOption, boolean isModelPanelField) {
		this.styleOption = styleOption;
		if (styleOption == 2 || styleOption == 3) {
			setLabel(sbolType);
			this.add(sbolLabel);
		}
		
		edit_sbolButton = new JButton("Add/Edit SBOL Part");
		edit_sbolButton.setActionCommand("associateSBOL");
		edit_sbolButton.addActionListener(this);
		this.add(edit_sbolButton);
		if (styleOption == 3)
			this.add(sbolText);
		sbolText.setVisible(false);
		
		remove_sbolButton = new JButton("Remove SBOL Association");
		remove_sbolButton.setActionCommand("removeAssociateSBOL");
		remove_sbolButton.addActionListener(this);
		this.add(remove_sbolButton);

		this.modelEditor = modelEditor;
		this.isModelPanelField = isModelPanelField;
	}

	/**
	 * Get a list of SBOL URIs.
	 * @return A list of SBOL URIs.
	 */
	public List<URI> getSBOLURIs() 
	{
		return sbolURIs;
	}

	/**
	 * Set a list of SBOL URIs.
	 * @param sbolURIs - The list of SBOL URIs to set.
	 */
	public void setSBOLURIs(List<URI> sbolURIs) 
	{
		this.sbolURIs = sbolURIs;
	}

	/**
	 * Get the SBOL strand.
	 * @return The SBOL strand.
	 */
	public String getSBOLStrand() 
	{
		return sbolStrand;
	}

	/**
	 * Set the SBOL strand.
	 * @param sbolStrand - The SBOL strand to set to.
	 */
	public void setSBOLStrand(String sbolStrand) 
	{
		this.sbolStrand = sbolStrand;
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		HashSet<String> sbolFilePaths = modelEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION);
		String filePath = sbolFilePaths.iterator().next();

		SBOLDocument workingDoc = readSBOLFile(filePath);
		if (e.getActionCommand().equals("associateSBOL")) 
		{
			if (sbolURIs.size() > 0) 
			{
				editSBOL(filePath, workingDoc);
			}
			else 
			{
				associateSBOL(filePath, workingDoc);
			}
		}
		else if(e.getActionCommand().equals("removeAssociateSBOL"))
		{
			removeAssociatedSBOL(filePath, workingDoc);
		}
	}
	
	/**
	 * Check if the SBOL object's displayId is set.
	 * @return True if the SBOL object's displayId is set. False otherwise.
	 */
	public boolean isSBOLIDSet()
	{
		return associatedObjSBO != null;
	}
	
	/**
	 * Check if the SBOL object's name is set.
	 * @return True if the SBOL object's name is set. False otherwise.
	 */
	public boolean isSBOLNameSet()
	{
		return associateObjName != null;
	}
	
	/**
	 * Check if the SBOL object's SBO term is set. 
	 * @return True if the SBOL object's SBO term is set. False otherwise.
	 */
	public boolean isSBOLSBOSet()
	{
		return associatedObjSBO != null;
	}
	
	
	/**
	 * Get the associated SBOL object name.
	 * @return SBOL object name
	 */
	public String getSBOLObjName()
	{
		return associateObjName;
	}
	
	/**
	 * Get the associated SBOL object name.
	 * @return SBOL object SBO term
	 */
	public String getSBOLObjSBOTerm()
	{
		return associatedObjSBO;
	}
	
	
	/**
	 * Update the following SBML fields after SBOL association has been performed.
	 * @param sbolObj - The associated SBOL object use to update the SBML fields.
	 */
	private void updateSBMLFieldsFromSBOL(TopLevel sbolObj)
	{
		associateObjName = sbolObj.getName();
		
		/*
		 * There is no mapping from ModuleDefinition to the SBO term we use on the SBML Model.
		 * Map only ComponentDefinition SBO term to SBML.
		 */
		if(sbolObj instanceof ComponentDefinition)
		{
			ComponentDefinition compDef = (ComponentDefinition) sbolObj;
			Set<URI> compDef_types = compDef.getTypes();
			if(!compDef_types.isEmpty())
			{
				URI type = compDef_types.iterator().next();
				associatedObjSBO = getSpeciesSBOTerm(type);
			}
		}
	}
	
	/**
	 * Get the equivalent SBML SBO term from the given SBOL SBO term
	 * @param sbolSBOTerm - The SBOL SBO term.
	 * @return The converted SBML SBO term.
	 */
	private String getSpeciesSBOTerm(URI sbolSBOTerm)
	{
		if(sbolSBOTerm.equals(ComponentDefinition.DNA_REGION))
		{
			return GlobalConstants.SBO_DNA_SEGMENT;
		}
		else if(sbolSBOTerm.equals(ComponentDefinition.RNA_REGION))
		{
			return GlobalConstants.SBO_RNA_SEGMENT;
		}
		else if(sbolSBOTerm.equals(ComponentDefinition.PROTEIN))
		{
			return GlobalConstants.SBO_PROTEIN;
		}
		else if(sbolSBOTerm.equals(ComponentDefinition.COMPLEX))
		{
			return GlobalConstants.SBO_NONCOVALENT_COMPLEX;
		}
		else if(sbolSBOTerm.equals(ComponentDefinition.SMALL_MOLECULE))
		{
			return GlobalConstants.SBO_SIMPLE_CHEMICAL;
		}
		
		return GlobalConstants.SBO_PROTEIN; //default case if no SBOL term was given
	}
	

	/**
	 * Run dialog to ask user if they want to associate SBOL from parts registry or create a generic part.
	 * @param filePath - the full path to the SBOL file.
	 * @throws Exception - SBOL data exception
	 */
	private void associateSBOL(String filePath, SBOLDocument workingDoc)
	{
		//TODO: Add check to perform SBOL association for CompDef and ModDef
		String[] options = {"Registry part", "Generic part", "Cancel"};
		int choice = JOptionPane.showOptionDialog(getParent(),
				"There is currently no associated SBOL part.  Would you like to associate one from a registry or associate a generic part?",
				"Associate SBOL Part", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[0]);

		switch (choice) {

		case 0: // Registry Part
			SBOLDocument selection = new RegistryInputDialog(Gui.frame, RegistryInputDialog.ALL_PARTS,
					edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types.All_types, null, workingDoc).getInput();

			if (selection != null) 
			{
				try 
				{
					SBOLUtils.insertTopLevels(selection, workingDoc);
				} 
				catch (Exception e) 
				{
					JOptionPane.showMessageDialog(getParent(), "Unable associate SBOL with this object", 
							"Failed Associating SBOL",
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
				ComponentDefinition associated_compDef = selection.getRootComponentDefinitions().iterator().next();
				setAssociatedSBOL(filePath, workingDoc, associated_compDef);
			}
			break;

		case 1: // Generic Part
			ComponentDefinition cd = Parts.GENERIC.createComponentDefinition(workingDoc);
			setAssociatedSBOL(filePath, workingDoc, cd);
			editSBOL(filePath, workingDoc);
			break;

		case JOptionPane.CLOSED_OPTION:
		default:
		}
	}
	
	
	/**
	 * Return the associated SBOL object that is to be annotated on the given SBML element.
	 * If the SBOL element can't be found, null is returned.
	 * @param workingDoc - The SBOLDocument to get the SBOL element to be annotated on the SBML element.
	 * @return The SBOL object. Null is returned if no the SBOL object could not be found in the workingDoc.
	 */
	private TopLevel getAssociatedSBOL_Obj(String filePath, SBOLDocument workingDoc)
	{
		URI sbolObjURI = sbolURIs.get(0); //There can be only one SBOL object associated to an SBML element.
		if(isComponentDefinition)
		{
			ComponentDefinition cd = workingDoc.getComponentDefinition(sbolObjURI);
			if (cd == null) {
				JOptionPane.showMessageDialog(getParent(), "Can't find " + sbolObjURI + " in " + filePath);
				return null;
			}
			return cd;
		}
		else if(isModuleDefinition)
		{
			ModuleDefinition modDef = workingDoc.getModuleDefinition(sbolObjURI);
			if (modDef == null) 
			{
				JOptionPane.showMessageDialog(getParent(), "Can't find" + sbolObjURI + " in " + filePath);
				return null;
			}
			return modDef;
		}
		
		return null; 
	}

	/**
	 * use PartEditDialog to edit/view the part
	 * @param filePath - full path to SBOL file
	 * @param workingDoc - The SBOLDocument that contain all parts that the user can use to associate SBOL.
	 */
	private void editSBOL(String filePath, SBOLDocument workingDoc) 
	{
		TopLevel sbolObj = getAssociatedSBOL_Obj(filePath, workingDoc);
		TopLevel editedSBOLObj = null;
		if(isComponentDefinition)
		{
			ComponentDefinition cd = (ComponentDefinition) sbolObj;
			ComponentDefinition editedCD = PartEditDialog.editPart(getParent(), null, cd, true, true, workingDoc, false);
			//ComponentDefinition editedCD = PartEditDialog.editPart(getParent(), null, cd, true, true, workingDoc);
			if (editedCD == null) {
				// nothing was changed
				return;
			}
			editedSBOLObj = editedCD;
		}
		else if(isModuleDefinition)
		{
			ModuleDefinition md = (ModuleDefinition) sbolObj;
			//TODO: implement a dialog for getting ModuleDefinition parts.
		}
		
		setAssociatedSBOL(filePath, workingDoc, editedSBOLObj);
	}
	
	/**
	 * Get the annotated SBOL object and remove it from the 
	 * @param filePath - The full path on where the associated SBOL object is located
	 * @param workingDoc - The SBOLDocument that contains the associated SBOL object.
	 */
	private void removeAssociatedSBOL(String filePath, SBOLDocument workingDoc)
	{
		TopLevel removeSBOLObj = getAssociatedSBOL_Obj(filePath, workingDoc);
		sbolURIs.remove(removeSBOLObj.getIdentity());
		try 
		{
			JOptionPane.showMessageDialog(Gui.frame, "Warning! You are about to remove the following SBOL component from this SBML element: \n"
					+ "SBOL id: " + removeSBOLObj.getIdentity() + "\n"
					+ "SBOL displayId: " + removeSBOLObj.getDisplayId() + "\n"
					+ "SBOL name: " + removeSBOLObj.getName());
			
			deleteRemovedBioSimComponent();
		} 
		catch (SBOLValidationException e) 
		{
			JOptionPane.showMessageDialog(getParent(), "Unable to remove SBOL parts for SBOL Association: " + filePath, "SBOL Validation Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		writeSBOLDocument(filePath, workingDoc);
	}
	
	/**
	 * Set information for the Associated SBOL type.
	 * @param filePath - The full path on where the associated SBOL object is located
	 * @param workingDoc - The SBOLDocument that contains the associated SBOL object.
	 * @param sbolObj - The SBOL object that the user want to perform SBOL association to.
	 */
	private void setAssociatedSBOL(String filePath, SBOLDocument workingDoc, TopLevel sbolObj)
	{
		sbolURIs = Arrays.asList(sbolObj.getIdentity());
		updateSBMLFieldsFromSBOL(sbolObj);
		writeSBOLDocument(filePath, workingDoc);
	}
	
	/**
	 * Write the given SBOLDocument to the specified filePath
	 * @param filePath - The location to write the SBOLDocument to
	 * @param workingDoc - The SBOLDocument to write 
	 */
	private void writeSBOLDocument(String filePath, SBOLDocument workingDoc)
	{
		try 
		{
			SBOLUtility.getSBOLUtility().writeSBOLDocument(filePath, workingDoc);
		} 
		catch (FileNotFoundException e) 
		{
			JOptionPane.showMessageDialog(getParent(), "File cannot be found for SBOL Association: " + filePath, 
					"File Not Found",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) 
		{
			JOptionPane.showMessageDialog(getParent(), "Unable to perform SBOLConversion when reading or writing this file for SBOL Association: " + filePath, 
					"SBOL Conversion Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	/**
	 * Read the given SBOLDocument from the specified filePath.
	 * 
	 * @param filePath - The location to read the SBOLDocument.
	 * @return The SBOLDocument that was read in. 
	 */
	private SBOLDocument readSBOLFile(String filePath)
	{
		SBOLDocument doc = null;
		try 
		{
			doc =  SBOLUtility.getSBOLUtility().loadSBOLFile(filePath, SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
		} 
		catch (FileNotFoundException e) 
		{
			JOptionPane.showMessageDialog(getParent(), "File cannot be found for SBOL Association: " + filePath, 
					"File Not Found",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (SBOLValidationException e) 
		{
			JOptionPane.showMessageDialog(getParent(), "Invalid SBOL was encountered when parsing the SBOL library file" + filePath, 
					"Invalid SBOL file",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(getParent(), "Unable to read SBOL file for SBOL Association: " + filePath, 
					"I/O Exception",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} 
		catch (SBOLConversionException e) 
		{
			JOptionPane.showMessageDialog(getParent(), "Unable to perform SBOLConversion when reading or writing this file for SBOL Association: " + filePath, 
					"SBOL Conversion Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		catch (SBOLException e1) 
		{
			JOptionPane.showMessageDialog(getParent(), 
					e1.getMessage(), e1.getTitle(), 
					JOptionPane.ERROR_MESSAGE);
		}
		return doc;
	}

	private void setLabel(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_COMPONENTDEFINITION)) {
			if (styleOption == 3)
				sbolLabel = new JLabel("SBOL ComponentDefinition");
			else
				sbolLabel = new JLabel("SBOL ComponentDefinition:", JLabel.RIGHT);
		} 
		else if (sbolType.equals(GlobalConstants.SBOL_CDS))
			sbolLabel = new JLabel("SBOL Coding Sequence");
		else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER))
			sbolLabel = new JLabel("SBOL Promoter");
		else if (sbolType.equals(GlobalConstants.SBOL_RBS))
			sbolLabel = new JLabel("SBOL Ribosome Binding Site");
		else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR))
			sbolLabel = new JLabel("SBOL Terminator");
	}

	/**
	 * Deletes from local SBOL files any iBioSim composite component that had
	 * its URI removed from the SBOLAssociationPanel
	 * @throws SBOLValidationException
	 */
	public void deleteRemovedBioSimComponent() throws SBOLValidationException 
	{
		if (removedBioSimURI != null) 
		{
			for (String filePath : modelEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION)) 
			{
				SBOLDocument sbolDoc = readSBOLFile(filePath);
				SBOLUtility.getSBOLUtility().deleteDNAComponent(removedBioSimURI, sbolDoc);
				writeSBOLDocument(filePath, sbolDoc);
			}

			removedBioSimURI = null;
		}
	} 

	public void resetRemovedBioSimURI() {
		removedBioSimURI = null;
	}
}
