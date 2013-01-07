package sbol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import main.Gui;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.Resolver;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.SBOLDocumentImpl;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;

import biomodel.annotation.AnnotationUtility;
import biomodel.parser.BioModel;
import biomodel.util.Utility;

public class SBOLDescriptorPanel extends JPanel {
	private String initialID;
	private JTextField sbolID, sbolName, sbolDescription;
	private JComboBox sbolSaveFileID;
	private String[] options = {"Ok", "Cancel"};
	private boolean removeBioSimURI = false;
	
	public SBOLDescriptorPanel(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		super(new GridLayout(4, 2));
		constructPanel(fileManager.getSBOLFileIDs());
		boolean display = (loadSBOLDescriptors(identityManager) || 
				!identityManager.containsBioSimURI() || identityManager.containsPlaceHolderURI() ||
				loadBioSimComponentDescriptors(identityManager, fileManager));
		while (display)
			display = panelOpen(identityManager, fileManager);
	}
	
	public void constructPanel(Set<String> sbolFileIDs) {
		sbolID = new JTextField("", 40);
		sbolName = new JTextField("", 40);
		sbolDescription = new JTextField("", 40);
		sbolSaveFileID = new JComboBox(sbolFileIDs.toArray());
		add(new JLabel("Save SBOL DNA Component to File:"));
		add(sbolSaveFileID);
		add(new JLabel("SBOL DNA Component ID:"));
		add(sbolID);
		add(new JLabel("SBOL DNA Component Name:"));
		add(sbolName);
		add(new JLabel("SBOL DNA Component Description:"));
		add(sbolDescription);
	}
	
	private boolean loadSBOLDescriptors(SBOLIdentityManager identityManager) {
		String[] sbolDescriptors = identityManager.getBioModel().getSBOLDescriptors();
		if (sbolDescriptors == null) {
			initialID = identityManager.getBioModel().getSBMLDocument().getModel().getId();
			sbolID.setText(initialID);
			return false;
		} else {
			initialID = sbolDescriptors[0];
			sbolID.setText(initialID);
			sbolName.setText(sbolDescriptors[1]);
			sbolDescription.setText(sbolDescriptors[2]);
			sbolSaveFileID.setSelectedItem(identityManager.getBioModel().getSBOLSaveFileID());
			return true;
		}
	}
	
	private boolean loadBioSimComponentDescriptors(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		if (identityManager.loadAndLocateBioSimComponent(fileManager)) {
			DnaComponent bioSimComp = identityManager.getBioSimComponent();
			if (bioSimComp != null) {
				sbolSaveFileID.setSelectedItem(identityManager.getSaveFileID());
				initialID = bioSimComp.getDisplayId();
				sbolID.setText(initialID);
				if (bioSimComp.getName() != null)
					sbolName.setText(bioSimComp.getName());
				if (bioSimComp.getDescription() != null)
					sbolDescription.setText(bioSimComp.getDescription());
			} else 
				removeBioSimURI = true;
			return true;
		} else
			return false;
	}
	
	private boolean panelOpen(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"Composite SBOL Descriptors", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == 0) {
			if (isSourceIdentifierInvalid(sbolID.getText()) || 
					isSourceIdentifierDuplicate(sbolID.getText(), sbolSaveFileID.getSelectedItem().toString(), fileManager))
				return true;
			else {
				String[] sbolDescriptors = new String[3];
				sbolDescriptors[0] = sbolID.getText();
				sbolDescriptors[1] = sbolName.getText();
				sbolDescriptors[2] = sbolDescription.getText(); 
				identityManager.getBioModel().setSBOLDescriptors(sbolDescriptors);
				identityManager.getBioModel().setSBOLSaveFile(sbolSaveFileID.getSelectedItem().toString());
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
		} else 
			return false;
	}
	
	private boolean isSourceIdentifierInvalid(String sourceID) {
		if (sourceID.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is blank.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return true;
		} else if (!Utility.isValid(sourceID, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is not alphanumeric.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return true;
		} 
		return false;
	}
	
	private boolean isSourceIdentifierDuplicate(String sourceID, String saveFileID, SBOLFileManager fileManager) {
		 if (!sourceID.equals(initialID) && fileManager.resolveDisplayID(sourceID, saveFileID) != null) {
			 JOptionPane.showMessageDialog(Gui.frame, "Chosen SBOL file contains DNA component with chosen ID.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			 return true;
		 } 
		 return false;
	}
}
