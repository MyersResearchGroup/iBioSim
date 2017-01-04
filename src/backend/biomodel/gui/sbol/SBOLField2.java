package backend.biomodel.gui.sbol;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
//import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;

import backend.biomodel.gui.schematic.ModelEditor;
import backend.biomodel.util.GlobalConstants;
import backend.sbol.util.SBOLUtility2;

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
	
//	public String getType() {
//		return sbolType;
//	}
//	
//	public String getText() {
//		return sbolText.getText();
//	}
//	
//	public void setText(String text) {
//		sbolText.setText(text);
//	}
	
	public List<URI> getSBOLURIs() {
		return sbolURIs;
	}
	
	public void setSBOLURIs(List<URI> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}
	
//	public void addSBOLURI(URI uri) {
//		sbolURIs.add(uri);
//	}
	
	public String getSBOLStrand() {
		return sbolStrand;
	}
	
	public void setSBOLStrand(String sbolStrand) {
		this.sbolStrand = sbolStrand;
	}
	
//	public boolean isValidText() {
//		if (sbolText.getText().equals(""))
//			return true;
//		else if (sbolType.equals(GlobalConstants.SBOL_DNA_COMPONENT))
//			return true;
//		else {
//			URI sourceCompURI = null;
//			try {
//				sourceCompURI = new URI(sbolText.getText());
//			} catch (URISyntaxException e) {
//				Utility.createErrorMessage("Invalid URI", "SBOL association text could not be parsed as URI.");
//				return false;
//			}
//			for (String filePath : modelEditor.getGui().getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION)) {
//				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
//				if (sbolDoc != null) {
//					SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
//					DnaComponent resolvedDnac = flattenedDoc.getComponentUriResolver().resolve(sourceCompURI);
//					if (resolvedDnac != null) {
//						for (URI uri : resolvedDnac.getTypes())
//							if (SBOLUtility.soSynonyms(sbolType).contains(uri.toString()))
//								return true;
//						Utility.createErrorMessage("Invalid GCM to SBOL Association", "DNA component with URI " + sourceCompURI
//								+ " is not a " + sbolLabel.getText() + ".");
//						return false;
//					}
//				}
//			}
//			Utility.createErrorMessage("DNA Component Not Found", "Component with URI " + sourceCompURI 
//					+ " is not found in project SBOL files.");
//			return false;
//		}
//	}
//	
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
				SBOLDocument sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
				SBOLUtility2.deleteDNAComponent(removedBioSimURI, sbolDoc);
				SBOLUtility2.writeSBOLDocument(filePath, sbolDoc);
			}
			removedBioSimURI = null;
		}
	}
	
	public void resetRemovedBioSimURI() {
		removedBioSimURI = null;
	}
}
