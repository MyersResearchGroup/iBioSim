package biomodel.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;

import sbol.SBOLAssociationPanel;
import sbol.SBOLUtility;

public class FBABounds extends JPanel implements ActionListener, MouseListener {
	
	private JButton addEvent, removeEvent, editEvent;

	private JList events; // JList of events

	private JList eventAssign; // JList of event assignments
	
	private BioModel bioModel;
	
	public FBABounds(BioModel bioModel,String reactionId) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		
		JPanel eventPanel = new JPanel(new BorderLayout());
		
		String[] assign = new String[0];
		
		// TODO: populate the string array with flux bounds corresponding to this reactionId

		JList eventAssign = new JList();
		
		JPanel eventAssignPanel = new JPanel(new BorderLayout());
		JPanel addEventAssign = new JPanel();
		JButton addAssignment = new JButton("Add");
		JButton removeAssignment = new JButton("Remove");
		JButton editAssignment = new JButton("Edit");
		addEventAssign.add(addAssignment);
		addEventAssign.add(removeAssignment);
		addEventAssign.add(editAssignment);
		addAssignment.addActionListener(this);
		removeAssignment.addActionListener(this);
		editAssignment.addActionListener(this);
		JLabel eventAssignLabel = new JLabel("List of Flux Bounds:");
		eventAssign.removeAll();
		eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(eventAssign);
		
		//Utility.sort(assign);
		eventAssign.setListData(assign);
		eventAssign.setSelectedIndex(0);
		eventAssign.addMouseListener(this);
		eventAssignPanel.add(eventAssignLabel, "North");
		eventAssignPanel.add(scroll, "Center");
		eventAssignPanel.add(addEventAssign, "South");
		
		eventPanel.add(eventAssignPanel, "South");
		Object[] options = { "Ok", "Cancel" };
		String title = "Flux Bounds Editor";
		int value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION && value != JOptionPane.YES_OPTION) {
			error = false;
			if (value == JOptionPane.YES_OPTION) {
				// TODO: remove flux bounds for reactionId
				// TODO: use Utility.getList to get the new bounds
				// TODO: for each new bound, create a new flux bound and populate it accordingly
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, 
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
//		
	}
	
	public void eventEditor(String option){		
		String[] assign = new String[0];
		JPanel eventPanel = new JPanel(new BorderLayout());
		
		JPanel eventAssignPanel = new JPanel(new BorderLayout());
		JPanel addEventAssign = new JPanel();
		JButton addAssignment = new JButton("Add Assignment");
		JButton removeAssignment = new JButton("Remove Assignment");
		JButton editAssignment = new JButton("Edit Assignment");
		addEventAssign.add(addAssignment);
		addEventAssign.add(removeAssignment);
		addEventAssign.add(editAssignment);
		addAssignment.addActionListener(this);
		removeAssignment.addActionListener(this);
		editAssignment.addActionListener(this);
		JLabel eventAssignLabel = new JLabel("List of Assignments:");
		eventAssign.removeAll();
		eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		
		//Utility.sort(assign);
		eventAssign.setListData(assign);
		eventAssign.setSelectedIndex(0);
		eventAssign.addMouseListener(this);
		eventAssignPanel.add(eventAssignLabel, "North");
		eventAssignPanel.add(scroll, "Center");
		eventAssignPanel.add(addEventAssign, "South");
		
		eventPanel.add(eventAssignPanel, "South");
		Object[] options = { option, "Cancel" };
		String title = "Event Editor";
		int value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			
		}
	}
	
	private void removeAssignment(JList eventAssign) {
		int index = eventAssign.getSelectedIndex();
		if (index != -1) {
			eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			//Utility.remove(eventAssign);
			eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < eventAssign.getModel().getSize()) {
				eventAssign.setSelectedIndex(index);
			}
			else {
				eventAssign.setSelectedIndex(index - 1);
			}
		}
	}
	
	private void eventAssignEditor(JList eventAssign, String option) {
		if (option.equals("OK") && eventAssign.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No event assignment selected.", "Must Select an Event Assignment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		/* TODO: BUILD YOUR OBJECTIVE PANEL HERE */
		JPanel evPanel = new JPanel(new GridLayout(1, 2));
		
		JLabel objectiveLabel = new JLabel("Bound:");
		
		JTextField objective = new JTextField(12);
		
		evPanel.add(objectiveLabel);
		evPanel.add(objective);
		
		//EAPanel.add(eqn);
		Object[] options = { option, "Cancel" };
		String title = "Flux Bound Editor";
		int value = JOptionPane.showOptionDialog(Gui.frame, evPanel, title, JOptionPane.YES_NO_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION && value != JOptionPane.YES_OPTION) {
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, evPanel, title, JOptionPane.YES_NO_OPTION, 
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
	}
		


	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addEvent) {
			eventEditor("Add");
		}
		/* TODO: ONLY NEED THE PART BELOW HERE */
		// if the add event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Add")) {
			eventAssignEditor(eventAssign, "Add");
		}
		// if the edit event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Edit")) {
			eventAssignEditor(eventAssign, "OK");
		}
		// if the remove event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Remove")) {
			removeAssignment(eventAssign);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
