package biomodel.gui.fba;

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

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FluxObjective;
import org.sbml.jsbml.ext.fbc.Objective;
import org.sbml.jsbml.ext.fbc.Objective.Type;

import main.Gui;
import biomodel.parser.BioModel;
import biomodel.util.SBMLutilities;
import main.util.Utility;

public class FBAObjective extends JPanel implements ActionListener, MouseListener {
	
	private static final long serialVersionUID = 1L;

	private JList objectives; // JList of objectives

	private JList objectiveList; // JList of event assignments

	private BioModel bioModel;
	
	private FBCModelPlugin fbc;
	
	private JPanel bigPanel = null;

	private String[] objectiveStringArray = null;
	
	private String activeObjective = null;
	
	public FBAObjective(BioModel bioModel) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		fbc = bioModel.getSBMLFBC();
		
		bigPanel = new JPanel(new BorderLayout());
		objectiveStringArray = new String[fbc.getListOfObjectives().size()];
		activeObjective = fbc.getListOfObjectives().getActiveObjective();
			
		for (int i = 0; i < fbc.getListOfObjectives().size(); i++) {
			String objective = "";
			Type type = fbc.getObjective(i).getType();
			String id = fbc.getObjective(i).getId();
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
			boolean first = true;
			for (int j = 0; j < fbc.getObjective(i).getListOfFluxObjectives().size(); j++) {
				FluxObjective fluxObjective = fbc.getObjective(i).getListOfFluxObjectives().get(j);
				String indexStr = SBMLutilities.getIndicesString(fluxObjective, "fbc:reaction");
				if (!first) objective += " + "; else first = false;
				objective += fluxObjective.getCoefficient() + " * " + fluxObjective.getReaction() + indexStr;
			}
			objectiveStringArray[i] = objective;
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
	}
	
	public void openGui() {
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
				fbc.setActiveObjective("");
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
					
					String[] equationTokens = equationString.split("\\+");
					for (int j = 0; j<equationTokens.length; j++){
						FluxObjective fluxObjective = objective.createFluxObjective();
						String [] coefficientReaction = equationTokens[j].split("\\*");
						String reactionIdIndex;
						if(coefficientReaction.length>1){
							fluxObjective.setCoefficient(Double.parseDouble(coefficientReaction[0].trim()));
							reactionIdIndex = coefficientReaction[1].trim();
						}
						else{
							fluxObjective.setCoefficient(1.0);
							reactionIdIndex = coefficientReaction[0].trim();
						}
						String reactionId = reactionIdIndex;
						String indexStr = "";
						if (reactionIdIndex.contains("[")) {
							reactionId = reactionIdIndex.substring(0,reactionIdIndex.indexOf("["));
							indexStr = reactionIdIndex.substring(reactionIdIndex.indexOf("["));
						} 						
						SBase reaction = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), reactionId);
						String[] dex = SBMLutilities.checkIndices(indexStr, reaction, bioModel.getSBMLDocument(), null, "fbc:reaction", null, null, null);
						fluxObjective.setReaction(reactionId);
						SBMLutilities.addIndices(fluxObjective, "fbc:reaction", dex, 1);
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, bigPanel, title, JOptionPane.YES_NO_OPTION, 
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
	}
	
	private static void removeObjective(JList objectiveList) {
		// find where the selected objective is on the list
		int index = objectiveList.getSelectedIndex();
		if (index != -1) {
			objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			// remove it
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add objective button is clicked
		if (((JButton) e.getSource()).getText().equals("Add")) {
			objectiveEditor(objectiveList, "Add");
		}
		// if the edit objective button is clicked
		else if (((JButton) e.getSource()).getText().equals("Edit")) {
			objectiveEditor(objectiveList, "OK");
		}
		// if the remove objective button is clicked
		else if (((JButton) e.getSource()).getText().equals("Remove")) {
			removeObjective(objectiveList);
		}
	}
	
	private void objectiveEditor(JList objectiveList, String option) {
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
			String objectiveInput = ((String) objectiveList.getSelectedValue());
			if(objectiveInput.startsWith("*")){
				activeObjective.setSelected(true);
			}
			int m = objectiveInput.indexOf("M");
			if(objectiveInput.startsWith("Max", m)){
				type.setSelectedItem("Maximize");
			}
			else{
				type.setSelectedItem("Minimize");
			}
			int leftParenthese = objectiveInput.indexOf("(");
			int rightParenthese = objectiveInput.indexOf(")");
			selectedID = objectiveInput.substring(leftParenthese+1, rightParenthese).trim();
			objectiveID.setText(objectiveInput.substring(leftParenthese+1, rightParenthese).trim());
			int eqsign = objectiveInput.indexOf("=");
			objective.setText(objectiveInput.substring(eqsign+1).trim());
		}
		
		obPanel.add(activeObjectiveLabel);
		obPanel.add(activeObjective);
		obPanel.add(IDLabel);
		obPanel.add(objectiveID);
		obPanel.add(typeLabel);
		obPanel.add(type);
		obPanel.add(objectiveLabel);
		obPanel.add(objective);
		
		String[] listOfObjectives = new String[objectiveList.getModel().getSize()];
		
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, obPanel, "Objective Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(bioModel.getSBMLDocument(), objectiveID.getText().trim(), selectedID, false);
			if (!error) {
				String tester = objective.getText().replaceAll("\\s", "");
				if(!tester.matches("((\\d*\\.\\d*\\*)?[\\_\\da-zA-Z]+(\\[\\d+\\])?\\+?)+")){
					error = true;
					JOptionPane.showMessageDialog(Gui.frame, "Input does not match acceptable formula format.",
						"Invalid formula!", JOptionPane.ERROR_MESSAGE);
				}
				
			} 
			if (!error) {
				int eqsign = objective.getText().indexOf("=");
				String editEquationString = objective.getText().substring(eqsign + 1, 
						objective.getText().length()).trim();
				String[] editEquationTokens = editEquationString.split("\\+");
				for (int j = 0; j<editEquationTokens.length; j++){
					String [] coefficeintReaction = editEquationTokens[j].split("\\*");
					String reactionIdIndex = coefficeintReaction[0].trim();
					if(coefficeintReaction.length>1){
						reactionIdIndex = coefficeintReaction[1].trim();
						try{
							Double.parseDouble(coefficeintReaction[0]);
						}
						catch(Exception e){
							error = true;
							JOptionPane.showMessageDialog(Gui.frame, coefficeintReaction[0].trim() + " is not a double!", 
									"Must input vaild reaction IDs", JOptionPane.ERROR_MESSAGE);
						}
					}
					String reactionId = reactionIdIndex;
					if (reactionIdIndex.contains("[")) {
						reactionId = reactionIdIndex.substring(0,reactionIdIndex.indexOf("["));
					}
					if(!bioModel.getReactions().contains(reactionId)){
						error = true;
						JOptionPane.showMessageDialog(Gui.frame, "Reaction " + reactionId + " is not a valid reaction!", 
								"Must input vaild reaction IDs", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			if (!error) {
				if (option.equals("OK")) {
					int index = objectiveList.getSelectedIndex();
					String editedObjectiveString = "";
					objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					listOfObjectives = Utility.getList(listOfObjectives, objectiveList);
					objectiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if(activeObjective.isSelected()){
						editedObjectiveString = "*";
						for(int i = 0; i < listOfObjectives.length;i++){
							if(listOfObjectives[i].startsWith("*")){
								listOfObjectives[i] = listOfObjectives[i].substring(1, listOfObjectives[i].length());
							}
						}
					}
					if(type.getSelectedItem().equals("Maximize")){
						editedObjectiveString += "Max";
					}
					else{
						editedObjectiveString += "Min";
					}
					editedObjectiveString += "(" + objectiveID.getText().trim() + ") = " + 
							objective.getText().trim();
					listOfObjectives[index] = editedObjectiveString;
					Utility.sort(listOfObjectives);
					objectiveList.setListData(listOfObjectives);
					objectiveList.setSelectedIndex(index);
				}
				else {
					JList add = new JList();
					int index = objectiveList.getSelectedIndex();
					String newObjectiveString = "";
					if(activeObjective.isSelected()){
						newObjectiveString = "*";
					}
					if(type.getSelectedItem().equals("Maximize")){
						newObjectiveString += "Max";
					}
					else{
						newObjectiveString += "Min";
					}
					newObjectiveString += "(" + objectiveID.getText().trim() + ") = " + 
							objective.getText().trim();
					Object[] adding = {newObjectiveString};
					add.setListData(adding);
					add.setSelectedIndex(0);
					objectiveList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(listOfObjectives, objectiveList, add);
					listOfObjectives = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						listOfObjectives[i] = (String) adding[i];
						if (listOfObjectives[i].startsWith("*")&& !listOfObjectives[i].equals(newObjectiveString)) {
							listOfObjectives[i] = listOfObjectives[i].substring(1, listOfObjectives[i].length());
						}
					}
					Utility.sort(listOfObjectives);
					objectiveList.setListData(listOfObjectives);
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
		

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == objectives) {
				if (objectives.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No objective selected.", 
							"Must Select an Objective", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//String selected = ((String) objectives.getSelectedValue());
			}
			else if (e.getSource() == objectiveList) {
				objectiveEditor(objectiveList, "OK");
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
