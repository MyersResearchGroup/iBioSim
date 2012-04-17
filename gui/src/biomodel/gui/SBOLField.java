package biomodel.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbolstandard.core.*;

import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLAssociationPanel;
import sbol.SBOLUtility;

public class SBOLField extends JPanel implements ActionListener {
	
	private String sbolType;
	private int styleOption;
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private LinkedList<String> sbolURIs = new LinkedList<String>();
	private JButton sbolButton = new JButton("Associate SBOL");
	private ModelEditor gcmEditor;
	
	public SBOLField(String sbolType, ModelEditor gcmEditor, int styleOption) {
		super(new GridLayout(1, 2));
	
		this.sbolType = sbolType;
		this.styleOption = styleOption;
		setLabel(sbolType);
		sbolButton.setActionCommand("associateSBOL");
		sbolButton.addActionListener(this);
		this.add(sbolLabel);
		this.add(sbolButton);
		if (styleOption == 0)
			this.add(sbolText);
		sbolText.setVisible(false);
		
		this.gcmEditor = gcmEditor;
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
	
	public LinkedList<String> getSBOLURIs() {
		return sbolURIs;
	}
	
	public void setSBOLURIs(LinkedList<String> sbolURIs) {
		this.sbolURIs = sbolURIs;
	}
	
	public void addSBOLURI(String uri) {
		sbolURIs.add(uri);
	}
	
	public boolean isValidText() {
		if (sbolText.getText().equals(""))
			return true;
		else if (sbolType.equals(GlobalConstants.SBOL_DNA_COMPONENT))
			return true;
		else {
			String sourceCompURI = sbolText.getText();
			for (String filePath : gcmEditor.getSbolFiles()) {
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
				if (sbolDoc != null)
					for (DnaComponent dnac : SBOLUtility.loadDNAComponents(sbolDoc)) 
						if (sourceCompURI.equals(dnac.getURI().toString())) {
							for (URI uri : dnac.getTypes())
								if (SBOLUtility.soSynonyms(sbolType).contains(uri.toString()))
									return true;
							Utility.createErrorMessage("Invalid GCM to SBOL Association", "DNA component with URI " + sourceCompURI
									+ " is not a " + sbolLabel.getText() + ".");
							return false;
						}
			}
			Utility.createErrorMessage("DNA Component Not Found", "Component with URI " + sourceCompURI 
					+ " is not found in project SBOL files.");
			return false;
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("associateSBOL")) {
			HashSet<String> sbolFiles = gcmEditor.getSbolFiles();
			SBOLAssociationPanel associationPanel = new SBOLAssociationPanel(sbolFiles, sbolURIs, SBOLUtility.soSynonyms(sbolType));
			sbolURIs = associationPanel.getCompURIs();
			//			SBOLBrowser browser = new SBOLBrowser(sbolFiles, SBOLUtility.typeConverter(sbolType), sbolText.getText());
//			sbolText.setText(browser.getSelection());
		} 
	}
	
	private void setLabel(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_DNA_COMPONENT)) {
			if (styleOption == 0)
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
}
