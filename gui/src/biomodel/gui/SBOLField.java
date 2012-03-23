package biomodel.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbolstandard.core.*;

import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLBrowser;
import sbol.SBOLUtility;

public class SBOLField extends JPanel implements ActionListener {
	
	private String sbolType;
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private JButton sbolButton = new JButton("Associate SBOL");
	private ModelEditor gcmEditor;
	
	public SBOLField(String sbolType, ModelEditor gcmEditor) {
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
		else {
			String sourceCompURI = sbolText.getText();
			for (String filePath : gcmEditor.getSbolFiles()) {
				Collection lib = SBOLUtility.loadXML(filePath);
				for (DnaComponent dnac : lib.getComponents())
					if (sourceCompURI.equals(dnac.getURI().toString())) {
						for (URI uri : dnac.getTypes())
							if (sbolType.equals(GlobalConstants.SBOL_DNA_COMPONENT) || 
									SBOLUtility.typeConverter(sbolType).contains(uri.getFragment()))
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
			SBOLBrowser browser = new SBOLBrowser(sbolFiles, SBOLUtility.typeConverter(sbolType), sbolText.getText());
			sbolText.setText(browser.getSelection());
		} 
	}
	
	private void setLabel(String sbolType) {
		if (sbolType.equals(GlobalConstants.SBOL_DNA_COMPONENT))
			sbolLabel = new JLabel("SBOL DNA Component");
		else if (sbolType.equals(GlobalConstants.SBOL_ORF))
			sbolLabel = new JLabel("SBOL Coding Sequence");
		else if (sbolType.equals(GlobalConstants.SBOL_PROMOTER))
			sbolLabel = new JLabel("SBOL Promoter");
		else if (sbolType.equals(GlobalConstants.SBOL_RBS))
			sbolLabel = new JLabel("SBOL Ribosome Binding Site");
		else if (sbolType.equals(GlobalConstants.SBOL_TERMINATOR))
			sbolLabel = new JLabel("SBOL Terminator");
	}
}
