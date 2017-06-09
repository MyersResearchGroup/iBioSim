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
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.TopLevel;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.gui.modelEditor.schematic.ModelEditor;
import edu.utah.ece.async.ibiosim.gui.util.preferences.EditPreferences;
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
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim
 *         Contributors </a>
 * @version %I%
 */
public class SBOLField2 extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private int styleOption;
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private List<URI> sbolURIs = new LinkedList<URI>();
	private String sbolStrand;
	private JButton edit_sbolButton, remove_sbolButton;
	private ModelEditor modelEditor; 
	private URI removedBioSimURI;
	
	private String associateObjID, associateObjName, associatedObjSBO;

	//TODO: not sure what the purpose of this field is for
	private boolean isModelPanelField;
	private String sbolType;
	
	/**
	 * 
	 * @param sbolURIs
	 * @param sbolStrand
	 * @param sbolType
	 * @param modelEditor
	 * @param styleOption
	 * @param isModelPanelField
	 */
	public SBOLField2(List<URI> sbolURIs, String sbolStrand, String sbolType, ModelEditor modelEditor, int styleOption,
			boolean isModelPanelField) {
		super(new GridLayout(1, styleOption));
		this.sbolURIs.addAll(sbolURIs);
		this.sbolStrand = sbolStrand;
		constructField(sbolType, modelEditor, styleOption, isModelPanelField);
	}

	/**
	 * 
	 * @param sbolType
	 * @param modelEditor
	 * @param styleOption
	 * @param isModelPanelField
	 */
	public SBOLField2(String sbolType, ModelEditor modelEditor, int styleOption, boolean isModelPanelField) {
		super(new GridLayout(1, styleOption));
		sbolStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
		constructField(sbolType, modelEditor, styleOption, isModelPanelField);
	}

	/**
	 * Create the following GUI layout for SBOL Association:
	 * 1. Add/Edit SBOL Association
	 * 2. Remove SBOL Association
	 * 
	 * @param sbolType - The SBOL object type. This is currently limited to SBOL ComponentDefinition or ModuleDefinition
	 * @param modelEditor - The SBML model editor that the sbol association will take place on.
	 * @param styleOption - specify 2 or 3 to set set SBOL association label to SBOL types.
	 * @param isModelPanelField - 
	 */
	public void constructField(String sbolType, ModelEditor modelEditor, int styleOption, boolean isModelPanelField) {
		this.sbolType = sbolType;
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
	public List<URI> getSBOLURIs() {
		return sbolURIs;
	}

	/**
	 * Set a list of SBOL URIs.
	 * @param sbolURIs - The list of SBOL URIs to set.
	 */
	public void setSBOLURIs(List<URI> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}

	/**
	 * Get the SBOL strand.
	 * @return The SBOL strand.
	 */
	public String getSBOLStrand() {
		return sbolStrand;
	}

	/**
	 * Set the SBOL strand.
	 * @param sbolStrand - The SBOL strand to set to.
	 */
	public void setSBOLStrand(String sbolStrand) {
		this.sbolStrand = sbolStrand;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("associateSBOL")) 
		{
			HashSet<String> sbolFilePaths = modelEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION);
			String filePath = sbolFilePaths.iterator().next();

			try {
				if (sbolURIs.size() > 0) {
					editSBOL(filePath);
				} else {
					associateSBOL(filePath);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		else if(e.getActionCommand().equals("removeAssociateSBOL"))
		{
			//TODO: remove the associated SBOL component off of the SBML element.
		}
	}
	
	/**
	 * Get the associated SBOL objects displayID.
	 * @return SBOL object displayID
	 */
	public String getSBOLObjID()
	{
		return associateObjID;
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
		associateObjID = sbolObj.getDisplayId();
		
		associateObjName = sbolObj.getName();
		
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
		if(sbolObj instanceof ModuleDefinition)
		{
			ModuleDefinition modDef = (ModuleDefinition) sbolObj;
			//TODO: determine the mapping for ModuleDefinition's SBO term.
		}
	}
	
	/**
	 * Get the equivalent SBML SBO term from the given SBOL SBO term
	 * @param sbolSBOTerm - The SBOL SBO term.
	 * @return The converted SBML SBO term.
	 */
	private String getSpeciesSBOTerm(URI sbolSBOTerm)
	{
		if(sbolSBOTerm.equals(ComponentDefinition.DNA))
		{
			return GlobalConstants.SBO_DNA_SEGMENT;
		}
		else if(sbolSBOTerm.equals(ComponentDefinition.RNA))
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
	 * associate a ComponentDefinition to this part
	 * @param filePath - the full path to the SBOL file.
	 * @throws Exception - SBOL data exception
	 */
	private void associateSBOL(String filePath) throws Exception {
		String[] options = {"Registry part", "Generic part", "Cancel"};
		int choice = JOptionPane.showOptionDialog(getParent(),
				"There is currently no associated SBOL part.  Would you like to associate one from a registry or associate a generic part?",
				"Associate SBOL Part", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
				options[0]);

		SBOLDocument workingDoc = SBOLReader.read(filePath);
		workingDoc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());

		switch (choice) {

		case 0: // Registry Part
			SBOLDocument selection = new RegistryInputDialog(null, RegistryInputDialog.ALL_PARTS,
					edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types.All_types, null, workingDoc).getInput();

			if (selection != null) 
			{
				SBOLUtils.insertTopLevels(selection, workingDoc);
				SBOLWriter.write(workingDoc, filePath);
				ComponentDefinition associated_compDef = selection.getRootComponentDefinitions().iterator().next();
				sbolURIs = Arrays.asList(associated_compDef.getIdentity());
				updateSBMLFieldsFromSBOL(associated_compDef);
			}
			break;

		case 1: // Generic Part
			ComponentDefinition cd = Parts.GENERIC.createComponentDefinition(workingDoc);

			SBOLWriter.write(workingDoc, filePath);
			sbolURIs = Arrays.asList(cd.getIdentity());
			break;

		case JOptionPane.CLOSED_OPTION:
		default:
		}
	}

	/**
	 * use PartEditDialog to edit/view the part
	 */
	private void editSBOL(String filePath) throws IOException, SBOLConversionException, SBOLValidationException {
		SBOLDocument workingDoc = SBOLReader.read(filePath);
		workingDoc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());

		URI compDefURI = sbolURIs.get(0);
		ComponentDefinition cd = workingDoc.getComponentDefinition(compDefURI);
		if (cd == null) {
			JOptionPane.showMessageDialog(getParent(), "Can't find" + compDefURI + " in " + filePath);
			return;
		}

		
		ComponentDefinition editedCD = PartEditDialog.editPart(getParent(), cd, true, true, workingDoc);

		if (editedCD == null) {
			// nothing was changed
			return;
		}

		sbolURIs = Arrays.asList(editedCD.getIdentity());
		updateSBMLFieldsFromSBOL(editedCD);
		
		SBOLWriter.write(workingDoc, filePath);
	}

	private void setLabel(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_COMPONENTDEFINITION)) {
			if (styleOption == 3)
				sbolLabel = new JLabel("SBOL ComponentDefinition");
			else
				sbolLabel = new JLabel("SBOL ComponentDefinition: ");
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

	// Deletes from local SBOL files any iBioSim composite component that had
	// its URI removed from the SBOLAssociationPanel
	public void deleteRemovedBioSimComponent() throws SBOLValidationException {
		if (removedBioSimURI != null) {
			for (String filePath : modelEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION)) {
				SBOLDocument sbolDoc;
				try {
					sbolDoc = SBOLUtility.loadSBOLFile(filePath, EditPreferences.getDefaultUriPrefix());
					SBOLUtility.deleteDNAComponent(removedBioSimURI, sbolDoc);
					SBOLUtility.writeSBOLDocument(filePath, sbolDoc);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SBOLConversionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			removedBioSimURI = null;
		}
	}

	public void resetRemovedBioSimURI() {
		removedBioSimURI = null;
	}
}
