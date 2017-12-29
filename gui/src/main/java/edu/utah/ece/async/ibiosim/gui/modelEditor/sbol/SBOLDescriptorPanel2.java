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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.Utility;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLFileManager;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLIdentityManager;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.sboldesigner.sbol.editor.SBOLEditorPreferences;

/**
 * 
 *
 * @author Nicholas Roehner
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLDescriptorPanel2 extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private String initialID;
	private JTextField idText, nameText, descriptionText;
	private JComboBox saveFileIDBox;
	private List<String> saveFilePaths;
	private String[] options = {"Ok", "Cancel"};
	private boolean removeBioSimURI = false;
	private boolean display;
	
	public SBOLDescriptorPanel2(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		
		super(new GridLayout(3, 2));
		constructPanel(fileManager.getSBOLFilePaths());
		display = (loadSBOLDescriptors(identityManager) || 
				!identityManager.containsBioSimURI() || identityManager.containsPlaceHolderURI() ||
				loadBioSimComponentDescriptors(identityManager, fileManager));
		
	}
	
	public SBOLDescriptorPanel2(String SBOLFileName, String displayId, String name, String description) {
		super(new GridLayout(4, 2));
		Set<String> SBOLFilePaths = new HashSet<String>();
		SBOLFilePaths.add(SBOLFileName);
		constructPanel(SBOLFilePaths);
		idText.setText(displayId);
		nameText.setText(name);
		descriptionText.setText(description);
		saveFileIDBox.setEnabled(false);
		idText.setEnabled(false);
		nameText.setEnabled(false);
		descriptionText.setEnabled(false);
	}
	
	public void openViewer() {
		JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Descriptors", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
	
	public void constructPanel(Set<String> sbolFilePaths) {
		
		idText = new JTextField("", 40);
		nameText = new JTextField("", 40);
		descriptionText = new JTextField("", 40);
		saveFilePaths = new LinkedList<String>(sbolFilePaths);
		saveFilePaths.add("Save to New File");
		saveFileIDBox = new JComboBox();
		for (String saveFilePath : saveFilePaths) {
			saveFileIDBox.addItem(GlobalConstants.getFilename(saveFilePath));
		}
		
		add(new JLabel("SBOL ComponentDefinition ID:"));
		add(idText);
		add(new JLabel("SBOL ComponentDefinition Name:"));
		add(nameText);
		add(new JLabel("SBOL ComponentDefinition Description:"));
		add(descriptionText);
	}
	
	private boolean loadSBOLDescriptors(SBOLIdentityManager identityManager) {
		String[] sbolDescriptors = identityManager.getBioModel().getSBOLDescriptors();
		if (sbolDescriptors == null) {
			initialID = identityManager.getBioModel().getSBMLDocument().getModel().getId();
			idText.setText(initialID);
			return false;
		}
		initialID = sbolDescriptors[0];
		idText.setText(initialID);
		nameText.setText(sbolDescriptors[1]);
		descriptionText.setText(sbolDescriptors[2]);
		saveFileIDBox.setSelectedIndex(saveFilePaths.indexOf(identityManager.getBioModel().getSBOLSaveFilePath()));
		return true;
	}
	
	private boolean loadBioSimComponentDescriptors(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		try {
			if (identityManager.loadAndLocateBioSimComponent(fileManager)) {
				ComponentDefinition bioSimComp = identityManager.getBioSimComponent();
				if (bioSimComp != null) {
					saveFileIDBox.setSelectedIndex(saveFilePaths.indexOf(identityManager.getSaveFilePath()));
					initialID = bioSimComp.getDisplayId();
					idText.setText(initialID);
					if (bioSimComp.getName() != null)
						nameText.setText(bioSimComp.getName());
					if (bioSimComp.getDescription() != null)
						descriptionText.setText(bioSimComp.getDescription());
				} else 
					removeBioSimURI = true;
				return true;
			}
		} catch (SBOLException e) {
			
			JOptionPane.showMessageDialog(Gui.frame, e.getMessage(), 
					e.getTitle(), JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean panelOpen(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		if (!display) return false;
		
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"Composite SBOL Descriptors", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == 0) 
		{
			if(saveFilePaths.get(saveFileIDBox.getSelectedIndex()).equals("Save to New File"))
			{
				//bring up dialog to create new file
				String newName = JOptionPane.showInputDialog(Gui.frame, "Enter file name:", "File Name", JOptionPane.PLAIN_MESSAGE); 
				if(newName.isEmpty())
					JOptionPane.showMessageDialog(Gui.frame, "No file name was provided.", "No file name", JOptionPane.ERROR_MESSAGE);
				if(!newName.endsWith(".rdf") && !newName.endsWith(".sbol"))
					newName = newName + ".sbol";
				else if(newName.endsWith(".rdf"))
					newName = newName.replace(".rdf", ".sbol");
				
				SBOLDocument newSBOLDoc = new SBOLDocument();
				newSBOLDoc.setDefaultURIprefix(SBOLEditorPreferences.INSTANCE.getUserInfo().getURI().toString());
				
				String filePath =  identityManager.getBioModel().getPath() + File.separator + newName;
				try
				{
					newSBOLDoc.write(filePath);
				}
				catch (SBOLConversionException e)
				{
					JOptionPane.showMessageDialog(Gui.frame, "Error writing SBOL file at " + filePath + ".", 
							"SBOL Conversion Error", JOptionPane.ERROR_MESSAGE);
				}
				catch (IOException e) {
					JOptionPane.showMessageDialog(Gui.frame, "Error writing SBOL file at " + filePath + ".", 
							"SBOL Write Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			//else if?
			if (isSourceIdentifierInvalid(idText.getText()) || 
					isSourceIdentifierDuplicate(idText.getText(), 
							saveFilePaths.get(saveFileIDBox.getSelectedIndex()), fileManager))
				return true;
			String[] sbolDescriptors = new String[3];
			sbolDescriptors[0] = idText.getText();
			sbolDescriptors[1] = nameText.getText();
			sbolDescriptors[2] = descriptionText.getText(); 
			identityManager.getBioModel().setSBOLDescriptors(sbolDescriptors);
			identityManager.getBioModel().setSBOLSaveFilePath(saveFilePaths.get(saveFileIDBox.getSelectedIndex()));
			if (removeBioSimURI) {
				try {
					identityManager.replaceBioSimURI(new URI("http://www.async.ece.utah.edu#iBioSimPlaceHolder"));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				identityManager.annotateBioModel();
			}
			return false;
		}
		return false;
	}
	
	private static boolean isSourceIdentifierInvalid(String sourceID) {
		if (sourceID.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is blank.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return true;
		} else if (!Utility.isValid(sourceID, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is not alphanumeric.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return true;
		} 
		return false;
	}
	
	private boolean isSourceIdentifierDuplicate(String sourceID, String saveFilePath, SBOLFileManager fileManager) {
		 if (!sourceID.equals(initialID) && fileManager.resolveDisplayID(sourceID, saveFilePath) != null) {
			 JOptionPane.showMessageDialog(Gui.frame, "Chosen SBOL file contains DNA component with chosen ID.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			 return true;
		 } 
		 return false;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		
		
	}
}
