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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBaseList;
import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.SBOLDocumentImpl;

import biomodel.annotation.AnnotationUtility;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLAssociationPanel;
import sbol.SBOLUtility;

public class FBAObjective extends JPanel implements ActionListener {
	
	private JLabel fbaoLabel = new JLabel("Flux Balance Objective: ");
	private JTextField fboaText = new JTextField(20);
	private JButton fbaoButton = new JButton("Edit Objective");
	private ModelEditor gcmEditor;
	private boolean isModelPanelField;
	private String[] options;
	private JTextField fbaoText;
	
	public FBAObjective() {
		this.add(fbaoLabel);
		fbaoButton.setActionCommand("Edit Objective");
		fbaoButton.addActionListener(this);
		this.add(fbaoButton);
		fbaoText = new JTextField(20);
		this.add(fbaoText);
		fbaoText.setVisible(false);
		
		options = new String[]{"Add", "Remove", "Edit", "Ok", "Cancel"};
		JOptionPane.showOptionDialog(Gui.frame, this,
				"Edit Objective", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
//	public void actionPerformed(ActionEvent e) {
//		if (e.getActionCommand().equals("Edit Objective")) {
//			SBOLAssociationPanel  associationPanel;
//			if (isModelPanelField) {
//				associationPanel = new SBOLAssociationPanel(sbolFilePaths, sbolURIs, 
//						SBOLUtility.soSynonyms(sbolType), gcmEditor.getBioModel().getSBMLDocument().getModel().getId());
//				removedBioSimURI = associationPanel.getRemovedBioSimURI();
//			} else
//				associationPanel = new SBOLAssociationPanel(sbolFilePaths, sbolURIs, SBOLUtility.soSynonyms(sbolType));
//			sbolURIs = associationPanel.getComponentURIs();
//		} 
//	}
}
