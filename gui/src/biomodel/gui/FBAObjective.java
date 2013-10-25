package biomodel.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import biomodel.parser.BioModel;
import main.util.Utility;

public class FBAObjective extends JPanel implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;

	private JList objectives; // JList of events

	private JList objectiveList; // JList of event assignments

	private BioModel bioModel;
	
	public FBAObjective(BioModel bioModel) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		
		JPanel bigPanel = new JPanel(new BorderLayout());
		
		String[] objectiveStringArray = new String[0];

		objectives = new JList();
		objectiveList = new JList();
		
		JPanel ObjectiveCreationPanel = new JPanel(new BorderLayout());
		JPanel buttons = new JPanel();
		JButton addObjective = new JButton("Add");
		JButton removeObjective = new JButton("Remove");
		JButton editObjective = new JButton("Edit");
		buttons.add(addObjective);
		buttons.add(removeObjective);
		buttons.add(editObjective);
		addObjective.addActionListener(this);
		removeObjective.addActionListener(this);
		editObjective.addActionListener(this);
		JLabel ObjectiveCreationLabel = new JLabel("List of Objectives:");
		objectiveList.removeAll();
		objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(objectiveList);
		
		Utility.sort(objectiveStringArray);
		objectiveList.setListData(objectiveStringArray);
		objectiveList.setSelectedIndex(0);
		objectiveList.addMouseListener(this);
		ObjectiveCreationPanel.add(ObjectiveCreationLabel, "North");
		ObjectiveCreationPanel.add(scroll, "Center");
		ObjectiveCreationPanel.add(buttons, "South");
		
		bigPanel.add(ObjectiveCreationPanel, "South");
		Object[] options = { "Ok", "Cancel" };
		String title = "Objectives Editor";
		int value = JOptionPane.showOptionDialog(Gui.frame, bigPanel, title, JOptionPane.YES_NO_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION && value != JOptionPane.YES_OPTION) {
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, bigPanel, title, JOptionPane.YES_NO_OPTION, 
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
	}
	
	public void objectiveEditor(String option){		
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
		objectiveList.removeAll();
		objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		
		Utility.sort(assign);
		objectiveList.setListData(assign);
		objectiveList.setSelectedIndex(0);
		objectiveList.addMouseListener(this);
		eventAssignPanel.add(eventAssignLabel, "North");
		eventAssignPanel.add(scroll, "Center");
		eventAssignPanel.add(addEventAssign, "South");
		
		eventPanel.add(eventAssignPanel, "South");
		Object[] options = { option, "Cancel" };
		String title = "Event Editor";
		int value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			
		}
	}
	
	private void removeAssignment(JList objectiveList) {
		int index = objectiveList.getSelectedIndex();
		if (index != -1) {
			objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			Utility.remove(objectiveList);
			objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < objectiveList.getModel().getSize()) {
				objectiveList.setSelectedIndex(index);
			}
			else {
				objectiveList.setSelectedIndex(index - 1);
			}
		}
	}

	/* TODO: NEED ALL THE FUNCTIONS BELOW */
	public void actionPerformed(ActionEvent e) {
		if (((JButton) e.getSource()).getText().equals("Add")) {
			objectiveEditor(bioModel, objectiveList, "Add");
		}
		// if the edit event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Edit")) {
			objectiveEditor(bioModel, objectiveList, "OK");
		}
		// if the remove event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Remove")) {
			removeAssignment(objectiveList);
		}
	}
	
	private void objectiveEditor(BioModel gcm, JList objectiveList, String option) {
		if (option.equals("OK") && objectiveList.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No objective selected.", "Must Select an Objective", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		/* TODO: BUILD YOUR OBJECTIVE PANEL HERE */
		JPanel obPanel = new JPanel(new GridLayout(4, 2));
		
		JLabel activeObjectiveLabel = new JLabel("Active Objective:");
		JLabel IDLabel = new JLabel("ID:");
		JLabel typeLabel = new JLabel("Type:");
		JLabel objectiveLabel = new JLabel("Objective:");
		
		JCheckBox activeObjective = new JCheckBox("");
		JTextField objectiveID = new JTextField(12);
		if (option.equals("OK")) {
			String selectAssign = ((String) objectiveList.getSelectedValue());
			objectiveID.setText(selectAssign.split("")[1].trim());
		}
		JComboBox type = new JComboBox(new String[] {"Maximize", "Minimize"});
		
		obPanel.add(activeObjectiveLabel);
		obPanel.add(activeObjective);
		obPanel.add(IDLabel);
		obPanel.add(objectiveID);
		obPanel.add(typeLabel);
		obPanel.add(type);
		obPanel.add(objectiveLabel);
		
		
		String[] assign = new String[objectiveList.getModel().getSize()];
		
		JTextField objective = new JTextField(12);
		if (option.equals("OK")) {
			String selectAssign = ((String) objectiveList.getSelectedValue());
			objective.setText(selectAssign.split("")[1].trim());
		}
		obPanel.add(objective);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, obPanel, "Objective Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if (!error) {
				if (option.equals("OK")) {
					int index = objectiveList.getSelectedIndex();
					objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					assign = Utility.getList(assign, objectiveList);
					objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					assign[index] = "(" + objectiveID.getText().trim() + ") = " + 
							objective.getText().trim();
					Utility.sort(assign);
					objectiveList.setListData(assign);
					objectiveList.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = objectiveList.getSelectedIndex();
					Object[] adding = {"(" + objectiveID.getText().trim() + ") = " + 
							objective.getText().trim() };
					add.setListData(adding);
					add.setSelectedIndex(0);
					objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(assign, objectiveList, add, null, null, null, null, null, Gui.frame);
					assign = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						assign[i] = (String) adding[i];
					}
					Utility.sort(assign);
					objectiveList.setListData(assign);
					objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (adding.length == 1) {
						objectiveList.setSelectedIndex(0);
					}
					else {
						objectiveList.setSelectedIndex(index);
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, obPanel, "Objective Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	
	}
		

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == objectives) {
				if (objectives.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No objective selected.", "Must Select an Objective", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) objectives.getSelectedValue());
				objectiveEditor("OK");
			}
			else if (e.getSource() == objectiveList) {
				objectiveEditor(bioModel, objectiveList, "OK");
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}
}
