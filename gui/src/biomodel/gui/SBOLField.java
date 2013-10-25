package biomodel.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.jsbml.Model;
import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.SBOLDocumentImpl;

import biomodel.annotation.AnnotationUtility;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLAssociationPanel;
import sbol.SBOLUtility;

public class SBOLField extends JPanel implements ActionListener {
	
	private String sbolType;
	private int styleOption;
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private List<URI> sbolURIs = new LinkedList<URI>();
	private List<URI> initialURIs;
	private boolean initiallyBlank;
	private JButton sbolButton = new JButton("Associate SBOL");
	private ModelEditor gcmEditor;
	private boolean isModelPanelField;
	private URI removedBioSimURI;
	
	public SBOLField(List<URI> sbolURIs, String sbolType, ModelEditor gcmEditor, int styleOption, boolean isModelPanelField) {
		super(new GridLayout(1, styleOption));
		this.sbolURIs.addAll(sbolURIs);
		constructField(sbolType, gcmEditor, styleOption, isModelPanelField);
	}
	
	public SBOLField(String sbolType, ModelEditor gcmEditor, int styleOption, boolean isModelPanelField) {
		super(new GridLayout(1, styleOption));
		constructField(sbolType, gcmEditor, styleOption, isModelPanelField);
	}
	
	public void constructField(String sbolType, ModelEditor gcmEditor, int styleOption, boolean isModelPanelField) {
		this.initialURIs = new LinkedList<URI>(sbolURIs);
		initiallyBlank = sbolURIs.size() == 0;
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
		
		this.gcmEditor = gcmEditor;
		this.isModelPanelField = isModelPanelField;
	}
	
	public boolean wasInitiallyBlank() {
		return initiallyBlank;
	}
	
	public List<URI> getInitialURIs() {
		return initialURIs;
	}
	
	public String getType() {
		return sbolType;
	}
	
	public String getText() {
		return sbolText.getText();
	}
	
	public void setText(String text) {
		sbolText.setText(text);
	}
	
	public List<URI> getSBOLURIs() {
		return sbolURIs;
	}
	
	public void setSBOLURIs(List<URI> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}
	
	public void addSBOLURI(URI uri) {
		sbolURIs.add(uri);
	}
	
	public boolean isValidText() {
		if (sbolText.getText().equals(""))
			return true;
		else if (sbolType.equals(GlobalConstants.SBOL_DNA_COMPONENT))
			return true;
		else {
			URI sourceCompURI = null;
			try {
				sourceCompURI = new URI(sbolText.getText());
			} catch (URISyntaxException e) {
				Utility.createErrorMessage("Invalid URI", "SBOL association text could not be parsed as URI.");
				return false;
			}
			for (String filePath : gcmEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION)) {
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
				if (sbolDoc != null) {
					SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
					DnaComponent resolvedDnac = flattenedDoc.getComponentUriResolver().resolve(sourceCompURI);
					if (resolvedDnac != null) {
						for (URI uri : resolvedDnac.getTypes())
							if (SBOLUtility.soSynonyms(sbolType).contains(uri.toString()))
								return true;
						Utility.createErrorMessage("Invalid GCM to SBOL Association", "DNA component with URI " + sourceCompURI
								+ " is not a " + sbolLabel.getText() + ".");
						return false;
					}
				}
			}
			Utility.createErrorMessage("DNA Component Not Found", "Component with URI " + sourceCompURI 
					+ " is not found in project SBOL files.");
			return false;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("associateSBOL")) {
			HashSet<String> sbolFilePaths = gcmEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION);
			SBOLAssociationPanel  associationPanel;
			if (isModelPanelField) {
				associationPanel = new SBOLAssociationPanel(sbolFilePaths, sbolURIs, 
						SBOLUtility.soSynonyms(sbolType), gcmEditor.getBioModel().getSBMLDocument().getModel().getId());
				removedBioSimURI = associationPanel.getRemovedBioSimURI();
			} else
				associationPanel = new SBOLAssociationPanel(sbolFilePaths, sbolURIs, SBOLUtility.soSynonyms(sbolType));
			sbolURIs = associationPanel.getComponentURIs();
		} 
	}
	
	private void setLabel(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_DNA_COMPONENT)) {
			if (styleOption == 3)
				sbolLabel = new JLabel("SBOL DNA Component");
			else
				sbolLabel = new JLabel("SBOL DNA Component: ");
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
	public void deleteRemovedBioSimComponent() {
		if (removedBioSimURI != null) {
			for (String filePath : gcmEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION)) {
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
				SBOLUtility.deleteDNAComponent(removedBioSimURI, sbolDoc);
				SBOLUtility.writeSBOLDocument(filePath, sbolDoc);
			}
			removedBioSimURI = null;
		}
	}
	
	public void resetRemovedBioSimURI() {
		removedBioSimURI = null;
	}
}
