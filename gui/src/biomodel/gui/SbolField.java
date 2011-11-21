package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreeModel;


import org.sbolstandard.libSBOLj.Library;
import org.sbolstandard.libSBOLj.SequenceFeature;

import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SbolBrowser;
import sbol.SbolUtility;

public class SbolField extends JPanel implements ActionListener {
	
	private String sbolType;
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private JButton sbolButton = new JButton("Associate SBOL");
	private ModelEditor gcmEditor;
	
	public SbolField(String sbolType, ModelEditor gcmEditor) {
		super(new GridLayout(1, 3));
	
		this.sbolType = sbolType;
		setLabel(sbolType);
		sbolButton.setActionCommand("associateSBOL");
		sbolButton.addActionListener(this);
		this.add(sbolLabel);
		this.add(sbolButton);
		this.add(sbolText);
		
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
	
	public boolean isValidText() {
		if (sbolText.getText().equals(""))
			return true;
		else if (Utility.isValid(sbolText.getText(), Utility.SBOLFIELDstring)) {
			String fileId = sbolText.getText().split("/")[0];
			String libId = sbolText.getText().split("/")[1];
			String featId = sbolText.getText().split("/")[2];
			Library lib = SbolUtility.loadRDF(gcmEditor.getPath() + File.separator + fileId);
			boolean libMatch = false;
			boolean featMatch = false;
			if (lib != null && lib.getDisplayId().equals(libId)) {
				libMatch = true;
				for (SequenceFeature feat : lib.getFeatures()) {
					if (feat.getDisplayId().equals(featId)) {
						featMatch = true;
						for (URI uri : feat.getTypes()) {
							if (uri.getFragment().equals(typeConverter(sbolType)))
								return true;
						}
					}
				}
			}
			if (lib != null) {
				if (!libMatch)
					Utility.createErrorMessage("Invalid GCM to SBOL Association", "Collection " + libId + " is not found in file " + fileId + ".");
				else if (!featMatch)
					Utility.createErrorMessage("Invalid GCM to SBOL Association", "DNA component " + featId + " is not found in collection " +
							libId + " from file " + fileId + ".");
				else
					Utility.createErrorMessage("Invalid GCM to SBOL Association", "DNA component " + featId + " is not a " + sbolLabel.getText() + ".");
			}
			return false;
		} else
			Utility.createErrorMessage("Invalid GCM to SBOL Association", "Associations must follow pattern file ID/collection ID/DNA component ID.");
			return false;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("associateSBOL")) {
			HashSet<String> sbolFiles = gcmEditor.getSbolFiles();
			SbolBrowser browser = new SbolBrowser(sbolFiles, typeConverter(sbolType), sbolText.getText());
			sbolText.setText(browser.getSelection());
		} 
	}
	
	private String typeConverter(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_ORF))
			return "ORF";
		else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER))
			return "promoter";
		else if (sbolType.equals(GlobalConstants.SBOL_RBS))
			return "RBS";
		else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR))
			return "terminator";
		return "";
	}
	
	private void setLabel(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_ORF))
			sbolLabel = new JLabel("SBOL Open Reading Frame");
		else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER))
			sbolLabel = new JLabel("SBOL Promoter");
		else if (sbolType.equals(GlobalConstants.SBOL_RBS))
			sbolLabel = new JLabel("SBOL Ribosome Binding Site");
		else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR))
			sbolLabel = new JLabel("SBOL Terminator");
	}
}
