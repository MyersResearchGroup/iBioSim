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
package edu.utah.ece.async.gui.biomodel.gui.sbol;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
//import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import edu.utah.ece.async.dataModels.util.GlobalConstants;
import edu.utah.ece.async.gui.biomodel.gui.schematic.ModelEditor;
import edu.utah.ece.async.gui.main.Gui;
import edu.utah.ece.async.gui.main.util.EditPreferences;
import edu.utah.ece.async.synthesis.sbol.util.SBOLUtility2;

public class SBOLField2 extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sbolType;
	private int styleOption;
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private List<URI> sbolURIs = new LinkedList<URI>();
	private String sbolStrand;
	private JButton sbolButton = new JButton("Associate SBOL");
	private ModelEditor modelEditor;
	private boolean isModelPanelField;
	private URI removedBioSimURI;
	
	public SBOLField2(List<URI> sbolURIs, String sbolStrand, String sbolType, ModelEditor modelEditor, 
			int styleOption, boolean isModelPanelField) {
		super(new GridLayout(1, styleOption));
		this.sbolURIs.addAll(sbolURIs);
		this.sbolStrand = sbolStrand;
		constructField(sbolType, modelEditor, styleOption, isModelPanelField);
	}
	
	public SBOLField2(String sbolType, ModelEditor modelEditor, int styleOption, boolean isModelPanelField) {
		super(new GridLayout(1, styleOption));
		sbolStrand = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
		constructField(sbolType, modelEditor, styleOption, isModelPanelField);
	}
	
	public void constructField(String sbolType, ModelEditor modelEditor, int styleOption, boolean isModelPanelField) {
		this.sbolType = sbolType;
		this.styleOption = styleOption;
		if (styleOption == 2 || styleOption  == 3) {
			setLabel(sbolType);
			this.add(sbolLabel);
		}
		sbolButton.setActionCommand("associateSBOL");
		sbolButton.addActionListener(this);
		this.add(sbolButton);
		if (styleOption == 3)
			this.add(sbolText);
		sbolText.setVisible(false);
		
		this.modelEditor = modelEditor;
		this.isModelPanelField = isModelPanelField;
	}
	
	public List<URI> getSBOLURIs() {
		return sbolURIs;
	}
	
	public void setSBOLURIs(List<URI> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}
	
	
	public String getSBOLStrand() {
		return sbolStrand;
	}
	
	public void setSBOLStrand(String sbolStrand) {
		this.sbolStrand = sbolStrand;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("associateSBOL")) {
			HashSet<String> sbolFilePaths = modelEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION);
			SBOLAssociationPanel2  associationPanel;
			if (isModelPanelField) 
			{
				associationPanel = new SBOLAssociationPanel2(sbolFilePaths, sbolURIs, sbolStrand, 
						SBOLUtility2.soSynonyms(sbolType), modelEditor);
				removedBioSimURI = associationPanel.getRemovedBioSimURI();
			} else
				associationPanel = new SBOLAssociationPanel2(sbolFilePaths, sbolURIs, sbolStrand,
						SBOLUtility2.soSynonyms(sbolType));
			sbolURIs = associationPanel.getComponentURIs();
			sbolStrand = associationPanel.getComponentStrand();
		} 
	}
	
	private void setLabel(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_COMPONENTDEFINITION)) {
			if (styleOption == 3)
				sbolLabel = new JLabel("SBOL ComponentDefinition");
			else
				sbolLabel = new JLabel("SBOL ComponentDefinition: ");
		} else if (sbolType.equals(GlobalConstants.SBOL_CDS))
			sbolLabel = new JLabel("SBOL Coding Sequence");
		else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER))
			sbolLabel = new JLabel("SBOL Promoter");
		else if (sbolType.equals(GlobalConstants.SBOL_RBS))
			sbolLabel = new JLabel("SBOL Ribosome Binding Site");
		else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR))
			sbolLabel = new JLabel("SBOL Terminator");
	}
	
	// Deletes from local SBOL files any iBioSim composite component that had its URI removed from the SBOLAssociationPanel
	public void deleteRemovedBioSimComponent() throws SBOLValidationException {
		if (removedBioSimURI != null) {
			for (String filePath : modelEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION)) {
				SBOLDocument sbolDoc;
				try {
					sbolDoc = SBOLUtility2.loadSBOLFile(filePath, EditPreferences.getDefaultUriPrefix());
					SBOLUtility2.deleteDNAComponent(removedBioSimURI, sbolDoc);
					SBOLUtility2.writeSBOLDocument(filePath, sbolDoc);
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
