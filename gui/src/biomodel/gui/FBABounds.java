package biomodel.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Port;
import org.sbml.libsbml.RateRule;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBaseList;
import org.sbml.libsbml.libsbml;
import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.SBOLDocumentImpl;

import biomodel.annotation.AnnotationUtility;
import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLAssociationPanel;
import sbol.SBOLUtility;

public class FBABounds extends JPanel implements ActionListener {
	
	private JTextField fbobText = new JTextField(20);
	private JButton fbabButton = new JButton("Edit Bounds");
	private ModelEditor gcmEditor;
	private boolean isModelPanelField;
	private String[] options;
	private JTextField fbabText;
	private JList compList;
	
	public FBABounds() {
		super(new GridLayout(1,1));
		fbabButton.setActionCommand("fluxBounds");
		fbabButton.addActionListener(this);
		this.add(fbabButton);
//		fbaoText = new JTextField(20);
//		this.add(fbaoText);
//		fbaoText.setVisible(false);
//		
//		
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("fluxBounds")) {
			createFBAO();
		}
		
	}
	public void createFBAO(){
		JPanel panel = new JPanel(new BorderLayout());
		JLabel associationLabel = new JLabel("Flux Bounds");
		options = new String[]{"Add", "Remove", "Edit", "Ok", "Cancel"};
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		panel.add(associationLabel, "North");
		panel.add(componentScroll, "Center");
		
		JOptionPane.showOptionDialog(Gui.frame, panel,
				"Flux Objective", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
}
