package gcm.gui;

import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreeModel;

import sbol.SbolBrowser;

public class SbolField extends JPanel implements ActionListener {
	
	private String sbolType;
	private JLabel sbolLabel;
	private JTextField sbolText = new JTextField(20);
	private JButton sbolButton = new JButton("Associate SBOL");
	private GCM2SBMLEditor gcmEditor;
	
	public SbolField(String sbolType, GCM2SBMLEditor gcmEditor) {
		super(new GridLayout(1, 3));
	
		this.sbolType = sbolType;
		setLabel(sbolType);
//		sbolText.setEditable(false);
		sbolButton.setActionCommand(sbolType);
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
		return Utility.isValid(getText(), Utility.SBOLFIELDstring);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(GlobalConstants.SBOL_ORF)) {
			HashSet<String> filePaths = gcmEditor.getSbolFiles();
			SbolBrowser browser = new SbolBrowser(filePaths, "ORF", sbolText);
		} else if (e.getActionCommand().equals(GlobalConstants.SBOL_PROMOTER)) {
			HashSet<String> filePaths = gcmEditor.getSbolFiles();
			SbolBrowser browser = new SbolBrowser(filePaths, "promoter", sbolText);
		} else if (e.getActionCommand().equals(GlobalConstants.SBOL_RBS)) {
			HashSet<String> filePaths = gcmEditor.getSbolFiles();
			SbolBrowser browser = new SbolBrowser(filePaths, "RBS", sbolText);
		} else if (e.getActionCommand().equals(GlobalConstants.SBOL_TERMINATOR)) {
			HashSet<String> filePaths = gcmEditor.getSbolFiles();
			SbolBrowser browser = new SbolBrowser(filePaths, "terminator", sbolText);
		} 
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
