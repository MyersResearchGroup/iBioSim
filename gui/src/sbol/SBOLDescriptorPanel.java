package sbol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
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

import biomodel.parser.BioModel;
import biomodel.util.Utility;

public class SBOLDescriptorPanel extends JPanel {
	private String filePath;
	private BioModel bioModel;
//	private UseFirstFound<DnaComponent, URI> aggregateCompResolver;
	private Resolver<DnaComponent, URI> compResolver;
	private String initialID;
	private JTextField sbolID, sbolName, sbolDescription;
	private JComboBox sbolSaveFile;
	private String[] options = {"Ok", "Cancel"};
	
	public SBOLDescriptorPanel(URI sbolURI, String filePath, HashSet<String> sbolFiles, BioModel bioModel) {
		super(new GridLayout(4, 2));
		this.filePath = filePath;
		this.bioModel = bioModel;
		
		constructPanel(sbolFiles);
		
		if (loadDnaComponentDescriptors(sbolURI, sbolFiles)) {
			boolean display = true;
			while (display)
				display = panelOpen();
		}
	}
	
	public SBOLDescriptorPanel(String filePath, HashSet<String> sbolFiles, BioModel bioModel) {
		super(new GridLayout(4, 2));
		this.filePath = filePath;
		this.bioModel = bioModel;
		
		constructPanel(sbolFiles);
		loadSBOLDescriptors();
		boolean display = true;
		while (display)
			display = panelOpen();
	}
	
	public void constructPanel(HashSet<String> sbolFiles) {
		sbolID = new JTextField("", 40);
		sbolName = new JTextField("", 40);
		sbolDescription = new JTextField("", 40);
		sbolSaveFile = new JComboBox(sbolFiles.toArray());
		add(new JLabel("Save SBOL DNA Component to File:"));
		add(sbolSaveFile);
		add(new JLabel("SBOL DNA Component ID:"));
		add(sbolID);
		add(new JLabel("SBOL DNA Component Name:"));
		add(sbolName);
		add(new JLabel("SBOL DNA Component Description:"));
		add(sbolDescription);
	}
	
	private boolean loadDnaComponentDescriptors(URI sbolURI, HashSet<String> sbolFiles) {
		for (String targetFile : sbolFiles) {
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath + File.separator + targetFile);
			if (sbolDoc != null) {
				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
				compResolver = flattenedDoc.getComponentUriResolver();
				DnaComponent dnac = compResolver.resolve(sbolURI);
				if (dnac != null) {
					sbolSaveFile.setSelectedItem(targetFile);
					initialID = dnac.getDisplayId();
					sbolID.setText(initialID);
					if (dnac.getName() != null)
						sbolName.setText(dnac.getName());
					if (dnac.getDescription() != null)
						sbolDescription.setText(dnac.getDescription());
					return true;
				}
			}
		}
//		aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
//		aggregateCompResolver.setResolvers(compResolvers);
		if (sbolFiles.size() == 0) 
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		else 
			JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + sbolURI.toString() +
					" that is currently annotating your model could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return false;
	}
	
	private void loadSBOLDescriptors() {
		String[] sbolDescriptors = bioModel.getSBOLDescriptors();
		if (sbolDescriptors == null) {
			initialID = bioModel.getSBMLDocument().getModel().getId();
			sbolID.setText(initialID);
		} else {
			initialID = sbolDescriptors[0];
			sbolID.setText(initialID);
			sbolName.setText(sbolDescriptors[1]);
			sbolDescription.setText(sbolDescriptors[2]);
			sbolSaveFile.setSelectedItem(sbolDescriptors[3]);
		}
	}
	
	
	private boolean panelOpen() {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"Composite SBOL Descriptors", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == 0) {
			
			if (!isSourceIdValid(sbolID.getText(), sbolSaveFile.getSelectedItem().toString()))
				return true;
			String[] sbolDescriptors = new String[3];
			sbolDescriptors[0] = sbolID.getText();
			sbolDescriptors[1] = sbolName.getText();
			sbolDescriptors[2] = sbolDescription.getText(); 
			bioModel.setSBOLDescriptors(sbolDescriptors);
			bioModel.setSBOLSaveFile(sbolSaveFile.getSelectedItem().toString());
			return false;
		} else
			return false;
	}
	
	private boolean isSourceIdValid(String sourceID, String targetFile) {
		if (sourceID.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is blank.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (!Utility.isValid(sourceID, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is not alphanumeric.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return false;
		} else if (!sourceID.equals(initialID)) {
			SBOLDocument targetDoc = SBOLUtility.loadSBOLFile(filePath + File.separator + targetFile);
			if (targetDoc != null) {
				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(targetDoc);
				if (flattenedDoc.getComponentDisplayIdResolver().resolve(sourceID) != null) {
					JOptionPane.showMessageDialog(Gui.frame, "Chosen SBOL file contains DNA component with chosen ID.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		return true;
	}
}
