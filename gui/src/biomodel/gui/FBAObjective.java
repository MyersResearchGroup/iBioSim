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

import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FluxObjective;
import org.sbml.jsbml.ext.fbc.Objective;
import org.sbml.jsbml.ext.fbc.Objective.Type;

import main.Gui;
import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.parser.BioModel;
import main.util.Utility;

public class FBAObjective extends JPanel implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;

	private JList objectives; // JList of objectives

	private JList objectiveList; // JList of event assignments

	private BioModel bioModel;
	
	private FBCModelPlugin fbc;
	
	public FBAObjective(BioModel bioModel) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		fbc = bioModel.getSBMLFBC();
		
//		HashMap<String, Integer> reactionIndex = new HashMap<String, Integer>();
//		int kp = 0;
//		for(int l =0;l<fbc.getListOfFluxBounds().size();l++){
//			if(!reactionIndex.containsKey(fbc.getFluxBound(l).getReaction())){
//				reactionIndex.put(fbc.getFluxBound(l).getReaction(), kp);
//				kp++;
//			}
//		}
		
		JPanel bigPanel = new JPanel(new BorderLayout());
		// TODO: allocate size based on number of objectives
		String[] objectiveStringArray = new String[fbc.getListOfObjectives().size()];
		// TODO: get active id from list of objectives
		String activeObjective = fbc.getListOfObjectives().getActiveObjective();
			
		// TODO: Build entries to the objectiveStringArray
		for (int i = 0; i < fbc.getListOfObjectives().size(); i++) {
			String objective = "";
			// TODO: get its type
			Type type = fbc.getObjective(i).getType();
			// TODO: get its id
			String id = fbc.getObjective(i).getId();
			// TODO: compare id with active id
			if(activeObjective.equals(id)){
				objective = "*";
			}
			if (type.equals(Type.MINIMIZE)) {
				objective += "Min";
			}
			else {
				objective += "Max";
			}
			objective += "(" + id + ") = ";
			
			objective += fbc.getObjective(i).getListOfFluxObjectives().get(0).getCoefficient() + 
					" * " + fbc.getObjective(i).getListOfFluxObjectives().get(0).getReaction();
			for (int j = 1; j < fbc.getObjective(i).getListOfFluxObjectives().size(); j++) {
				objective += " + " + fbc.getObjective(i).getListOfFluxObjectives().get(j).getCoefficient() + 
						" * " + fbc.getObjective(i).getListOfFluxObjectives().get(j).getReaction();
			}
			objectiveStringArray[i] = objective;
//			objectiveStringArray[i] = fbc.getObjective(i).toString();
		}
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
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if (value == JOptionPane.YES_OPTION) {
				objectiveStringArray = new String[objectiveList.getModel().getSize()];
				for (int i = 0; i < objectiveList.getModel().getSize(); i++) {
					objectiveStringArray[i] = objectiveList.getModel().getElementAt(i).toString();
				}
				while (fbc.getListOfObjectives().size() > 0) {
					fbc.removeObjective(0);
				}
				for (int i = 0; i<objectiveStringArray.length;i++){
					Objective objective = fbc.createObjective();
					int m = objectiveStringArray[i].indexOf("M");
					if(objectiveStringArray[i].startsWith("Max", m)){
						objective.setType(Type.MAXIMIZE);
					}
					else{
						objective.setType(Type.MINIMIZE);
					}
					int leftParenthese = objectiveStringArray[i].indexOf("(");
					int rightParenthese = objectiveStringArray[i].indexOf(")");
					objective.setId(objectiveStringArray[i].substring(leftParenthese+1,	rightParenthese).trim());
					if(objectiveStringArray[i].startsWith("*")){
						fbc.setActiveObjective(objective.getId());
					}
					int eqsign = objectiveStringArray[i].indexOf("=");
					String equationString = objectiveStringArray[i].substring(eqsign + 1, 
							objectiveStringArray[i].length()).trim();
					System.out.println(equationString);
					
					// TODO: get this working when no coefficient is provided
					String[] equationTokens = equationString.split("\\+");
					for (int j = 0; j<equationTokens.length; j++){
						FluxObjective fluxObjective = objective.createFluxObjective();
						String [] coefficeintReaction = equationTokens[j].split("\\*");
						fluxObjective.setCoefficient(Double.parseDouble(coefficeintReaction[0].trim()));
						fluxObjective.setReaction(coefficeintReaction[1].trim());
					}

				}
			}
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
		JPanel obPanel = new JPanel(new GridLayout(4, 2));
		
		JLabel activeObjectiveLabel = new JLabel("Active Objective:");
		JLabel IDLabel = new JLabel("ID:");
		JLabel typeLabel = new JLabel("Type:");
		JLabel objectiveLabel = new JLabel("Objective:");
		
		JCheckBox activeObjective = new JCheckBox("");
		JTextField objectiveID = new JTextField(12);
		JComboBox type = new JComboBox(new String[] {"Maximize", "Minimize"});
		JTextField objective = new JTextField(12);
		String selectedID = "";
		if (option.equals("OK")) {
			String selectAssign = ((String) objectiveList.getSelectedValue());
			if(selectAssign.startsWith("*")){
				activeObjective.setSelected(true);
			}
			int m = selectAssign.indexOf("M");
			if(selectAssign.startsWith("Max", m)){
				type.setSelectedItem("Maximize");
			}
			else{
				type.setSelectedItem("Minimize");
			}
			int leftParenthese = selectAssign.indexOf("(");
			int rightParenthese = selectAssign.indexOf(")");
			selectedID = selectAssign.substring(leftParenthese+1, rightParenthese).trim();
			objectiveID.setText(selectAssign.substring(leftParenthese+1, rightParenthese).trim());
			int eqsign = selectAssign.indexOf("=");
			objective.setText(selectAssign.substring(eqsign+1).trim());
		}
		
		obPanel.add(activeObjectiveLabel);
		obPanel.add(activeObjective);
		obPanel.add(IDLabel);
		obPanel.add(objectiveID);
		obPanel.add(typeLabel);
		obPanel.add(type);
		obPanel.add(objectiveLabel);
		obPanel.add(objective);
		
		String[] assign = new String[objectiveList.getModel().getSize()];
		
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, obPanel, "Objective Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(bioModel.getSBMLDocument(), objectiveID.getText().trim(), selectedID, false, false);
			if (!error) {
				// TODO: error check the formula (##.##)? \\* ID (+ (##.##)? \\* ID)*
				// set error=true if violation
				// error message
			} 
			if (!error) {
				// TODO: check that all id's in the formula are valid reactions, namely, bioModel.getDocument().getReaction(ID) should not be null
				// if problem set error=true
				// error message
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = objectiveList.getSelectedIndex();
					String assignString = "";
					objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					assign = Utility.getList(assign, objectiveList);
					objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if(activeObjective.isSelected()){
						assignString = "*";
						// TODO: remove * from any other member of "assign" which has a "*"
					}
					if(type.getSelectedItem().equals("Maximize")){
						assignString += "Max";
					}
					else{
						assignString += "Min";
					}
					assignString += "(" + objectiveID.getText().trim() + ") = " + 
							objective.getText().trim();
					// TODO: change "assign" to be more meaningful
					assign[index] = assignString;
					Utility.sort(assign);
					objectiveList.setListData(assign);
					objectiveList.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = objectiveList.getSelectedIndex();
					String addingString = "";
					if(activeObjective.isSelected()){
						addingString = "*";
						// TODO: remove * from all other entries of "assign"
					}
					if(type.getSelectedItem().equals("Maximize")){
						addingString += "Max";
					}
					else{
						addingString += "Min";
					}
					addingString += "(" + objectiveID.getText().trim() + ") = " + 
							objective.getText().trim();
					Object[] adding = {addingString};
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
					JOptionPane.showMessageDialog(Gui.frame, "No objective selected.", 
							"Must Select an Objective", JOptionPane.ERROR_MESSAGE);
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
