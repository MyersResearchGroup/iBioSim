package frontend.lpn.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dataModels.biomodel.util.Utility;
import dataModels.lpn.parser.*;
import dataModels.util.GlobalConstants;
import frontend.biomodel.gui.util.PropertyList;
import frontend.main.Gui;


public class ControlFlowPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "";

	private PropertyList flowList;

	private JComboBox fromBox, toBox;

	private String[] placeList = null, transitionList = null, flowStringList;

	private String[] options = { "Ok", "Cancel" };

	private LPN lhpn;
	
	private boolean flag = false;

	private HashMap<String, PropertyField> fields = null;

	public ControlFlowPanel(String selected, PropertyList flowList, LPN lhpn) {
		super(new GridLayout(2, 1));
		this.selected = selected;
		this.flowList = flowList;
		this.lhpn = lhpn;

		placeList = lhpn.getPlaceList();
		transitionList = lhpn.getTransitionList();
		flowStringList = new String[0];
		if (!placeList.equals(null) && !transitionList.equals(null)) {
			flowStringList = new String[placeList.length + transitionList.length];
		}
		if (!placeList.equals(null) && !transitionList.equals(null)) {
			System.arraycopy(placeList, 0, flowStringList, 0, placeList.length);
			System.arraycopy(transitionList, 0, flowStringList, placeList.length,
					transitionList.length);
		}
		else if (placeList.length > 0) {
			System.arraycopy(placeList, 0, flowStringList, 0, placeList.length);
		}
		else if (transitionList.length > 0) {
			System.arraycopy(transitionList, 0, flowStringList, 0, transitionList.length);
		}
		
		sort(placeList);
		sort(transitionList);
		sort(flowStringList);

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.NAMEstring);
		fields.put(GlobalConstants.ID, field);

		JPanel tempPanel;
		JLabel tempLabel;
		// From field
		if (placeList.length > 0 && transitionList.length > 0) {
			tempPanel = new JPanel();
			tempLabel = new JLabel("From");
			fromBox = new JComboBox(flowStringList);
			fromBox.setSelectedItem(flowStringList[0]);
			fromBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(fromBox);
			add(tempPanel);
		}

		// To field
		if (placeList.length > 0 && transitionList.length > 0) {
			tempPanel = new JPanel();
			tempLabel = new JLabel("To");
			if (isTransition(fromBox)) {
				toBox = new JComboBox(placeList);
			}
			else {
				toBox = new JComboBox(transitionList);
			}
			toBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(toBox);
			add(tempPanel);
		}

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			String[] flowArray = selected.split("\\s");
			fromBox.setSelectedItem(flowArray[0]);
			toBox.setSelectedItem(flowArray[1]);
			if (isPlace(fromBox)) {
				String to = "";
				Boolean flag = false;
				if (toBox.getSelectedItem() != null) {
					to = toBox.getSelectedItem().toString();
					flag = true;
				}
				toBox.removeAllItems();
				for (int i = 0; i < transitionList.length; i++) {
					toBox.removeItem(transitionList[i]);
					toBox.addItem(transitionList[i]);
				}

				if (flag) {
					toBox.setSelectedItem(to);
				}
			}
			else if (isTransition(fromBox)) {
				String to = "";
				Boolean flag = false;
				if (toBox.getSelectedItem() != null) {
					to = toBox.getSelectedItem().toString();
					flag = true;
				}
				toBox.removeAllItems();
				for (int i = 0; i < placeList.length; i++) {
					toBox.removeItem(placeList[i]);
					toBox.addItem(placeList[i]);
				}
				if (flag) {
					toBox.setSelectedItem(to);
				}
			}
		}

		if (placeList.length > 0 && transitionList.length > 0) {
			boolean display = false;
			while (!display) {
				display = openGui(oldName);
			}
		}
		else {
			JOptionPane.showMessageDialog(this,
					"There must be places and transitions to create control flow.",
					"Control Flow Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		fields.get(GlobalConstants.ID).setValue(
				fromBox.getSelectedItem().toString() + " " + toBox.getSelectedItem().toString());
	}

	private boolean openGui(String oldName) {
		String id = (String) fromBox.getSelectedItem() + " " + (String) toBox.getSelectedItem();
		String[] oldFlow = new String[2];
		if (oldName != null) {
			oldFlow = oldName.split("\\s");
		}
		String[] newFlow = { (String) fromBox.getSelectedItem(), (String) toBox.getSelectedItem() };
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Control Flow Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			id = (String) fromBox.getSelectedItem() + " " + (String) toBox.getSelectedItem();
			newFlow[0] = (String) fromBox.getSelectedItem();
			newFlow[1] = (String) toBox.getSelectedItem();
			if (oldName == null) {
				if (lhpn.containsMovement(newFlow[0], newFlow[1])) {
					Utility.createErrorMessage("Error", "Movement already exists.");
					return false;
				}
			}
			else if (!oldName.equals(id)) {
				if (lhpn.containsMovement(newFlow[0], newFlow[1])) {
					Utility.createErrorMessage("Error", "Movement already exists.");
					return false;
				}
			}

			// Check to see if we need to add or edit

			if (selected != null && oldName != null && !oldName.equals(id)) {
				lhpn.removeMovement(oldFlow[0], oldFlow[1]);
			}
			lhpn.addMovement(fromBox.getSelectedItem().toString(), toBox.getSelectedItem()
					.toString());
			flowList.removeItem(oldName);
			flowList.addItem(id);
			flowList.setSelectedValue(id, true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

	private boolean isPlace(JComboBox box) {
		if (box.getSelectedItem() != null) {
			String test = box.getSelectedItem().toString();
			for (int i = 0; i < placeList.length; i++) {
				if (placeList[i].equals(test)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isTransition(JComboBox box) {
		if (box.getSelectedItem() != null) {
			String test = box.getSelectedItem().toString();
			for (int i = 0; i < transitionList.length; i++) {
				if (transitionList[i].equals(test)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static void sort(String[] sort) {
		int i, j;
		String index;
		for (i = 1; i < sort.length; i++) {
			index = sort[i];
			j = i;
			while ((j > 0) && (sort[j - 1]).compareToIgnoreCase(index) > 0) {
				sort[j] = sort[j - 1];
				j = j - 1;
			}
			sort[j] = index;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (flag) {
			return;
		}
		if (e.getActionCommand().equals("comboBoxChanged")) {
			flag = true;
			if (isPlace(fromBox)) {
				String to = "";
				Boolean flag = false;
				if (toBox.getSelectedItem() != null) {
					to = toBox.getSelectedItem().toString();
					flag = true;
				}
				toBox.removeAllItems();
				for (int i = 0; i < transitionList.length; i++) {
					toBox.addItem(transitionList[i]);
				}
				if (flag) {
					toBox.setSelectedItem(to);
				}
			}
			else if (isTransition(fromBox)) {
				String to = "";
				Boolean flag = false;
				if (toBox.getSelectedItem() != null) {
					to = toBox.getSelectedItem().toString();
					flag = true;
				}
				toBox.removeAllItems();
				for (int i = 0; i < placeList.length; ++i) {
					toBox.addItem(placeList[i]);
				}
				if (flag) {
					toBox.setSelectedItem(to);
				}
			}
			flag = false;
		}
		fields.get(GlobalConstants.ID).setValue(
				fromBox.getSelectedItem().toString() + " " + toBox.getSelectedItem().toString());
	}

}
