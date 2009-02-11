package lhpn2sbml.gui;

import lhpn2sbml.parser.*;

import gcm2sbml.gui.*;
//import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
//import java.util.Properties;

//import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ControlFlowPanel extends JPanel implements ActionListener {

	private String selected = "";

	private PropertyList flowList;

	private JComboBox fromBox, toBox;

	private String[] placeList = null, transitionList = null, flowStringList;

	private String[] options = { "Ok", "Cancel" };

	private LHPNFile lhpn;
	
	private boolean flag = false;

	private HashMap<String, PropertyField> fields = null;

	public ControlFlowPanel(String selected, PropertyList flowList, LHPNFile lhpn) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.flowList = flowList;
		this.lhpn = lhpn;

		placeList = lhpn.getPlaceList();
		//System.out.println(placeList.length);
		transitionList = lhpn.getTransitionList();
		flowStringList = new String[0];
		if (!placeList.equals(null) && !transitionList.equals(null)) {
			flowStringList = new String[placeList.length + transitionList.length];
			// JOptionPane.showMessageDialog(this, flowStringList.length);
		}
		// placeList = lhpn.getPlaceList();
		// transitionList = lhpn.getTransitionList();
		if (!placeList.equals(null) && !transitionList.equals(null)) {
			System.arraycopy(placeList, 0, flowStringList, 0, placeList.length);
			System.arraycopy(transitionList, 0, flowStringList, placeList.length,
					transitionList.length);
			// JOptionPane.showMessageDialog(this, flowStringList.length);
		}
		else if (placeList.length > 0) {
			System.arraycopy(placeList, 0, flowStringList, 0, placeList.length);
		}
		else if (transitionList.length > 0) {
			System.arraycopy(transitionList, 0, flowStringList, 0, transitionList.length);
		}
		// JOptionPane.showMessageDialog(this, flowStringList.length);

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.NAMEstring);
		fields.put(GlobalConstants.ID, field);
		// add(field);

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
			// toBox.setSelectedItem(flowStringList[0]);
			toBox.addActionListener(this);
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(toBox);
			add(tempPanel);
		}

		String oldName = null;
		if (selected != null) {
			// JOptionPane.showMessageDialog(this, "here");
			oldName = selected;
			// fields.get(GlobalConstants.ID).setValue(selected);
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
					// JOptionPane.showMessageDialog(this, to);
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
		}
		fields.get(GlobalConstants.ID).setValue(
				fromBox.getSelectedItem().toString() + " " + toBox.getSelectedItem().toString());
	}

	// private boolean checkValues() {
	// for (PropertyField f : fields.values()) {
	// if (!f.isValid()) {
	// return false;
	// }
	// }
	// return true;
	// }

	private boolean openGui(String oldName) {
		String id = (String) fromBox.getSelectedItem() + " " + (String) toBox.getSelectedItem();
		// JOptionPane.showMessageDialog(this, oldName);
		String[] oldFlow = new String[2];
		if (oldName != null) {
			oldFlow = oldName.split("\\s");
		}
		String[] newFlow = { (String) fromBox.getSelectedItem(), (String) toBox.getSelectedItem() };
		int value = JOptionPane.showOptionDialog(new JFrame(), this, "Control Flow Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			id = (String) fromBox.getSelectedItem() + " " + (String) toBox.getSelectedItem();
			newFlow[0] = (String) fromBox.getSelectedItem();
			newFlow[1] = (String) toBox.getSelectedItem();
			// if (!checkValues()) {
			// Utility.createErrorMessage("Error", "Illegal values entered.");
			// return false;
			// }
			if (oldName == null) {
				if (lhpn.containsFlow(newFlow[0], newFlow[1])) {
					Utility.createErrorMessage("Error", "Movement already exists.");
					return false;
				}
			}
			else if (!oldName.equals(id)) {
				if (lhpn.containsFlow(newFlow[0], newFlow[1])) {
					Utility.createErrorMessage("Error", "Movement already exists.");
					return false;
				}
			}

			// Check to see if we need to add or edit

			if (selected != null && !oldName.equals(id) && oldName != null) {
				lhpn.removeControlFlow(oldFlow[0], oldFlow[1]);
			}
			lhpn.addControlFlow(fromBox.getSelectedItem().toString(), toBox.getSelectedItem()
					.toString());
			flowList.removeItem(oldName);
			// JOptionPane.showMessageDialog(this, id);
			flowList.addItem(id);
			flowList.setSelectedValue(id, true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
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
				//System.out.println(toBox.getItemCount());
				// JOptionPane.showMessageDialog(this, toBox.getItemCount());
				for (int i = 0; i < transitionList.length; i++) {
					// JOptionPane.showMessageDialog(this, "transition " + i + "
					// " + transitionList[i]);
					//toBox.removeItem(transitionList[i]);
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
					// JOptionPane.showMessageDialog(this, "to: " + to);
				}
				toBox.removeAllItems();
				//toBox.removeAll();
				//System.out.println("count " + toBox.getItemCount());
				//System.out.println("starting for loop");
				for (int i = 0; i < placeList.length; ++i) {
					//System.out.println(placeList.length);
					//System.out.println(i + " : " + placeList[i]);
					//System.out.println(placeList.length);
					//toBox.removeItem(placeList[i]);
					toBox.addItem(placeList[i]);
					//System.out.println(i + " adsf " + toBox.getItemAt(i));
					//System.out.println(placeList[i]);
					//System.out.println("");
				}
				//System.out.println("leaving for loop");
				if (flag) {
					toBox.setSelectedItem(to);
				}
			}
			flag = false;
		}
		fields.get(GlobalConstants.ID).setValue(
				fromBox.getSelectedItem().toString() + " " + toBox.getSelectedItem().toString());
	}

	// private void loadProperties(Properties property) {
	// for (Object o : property.keySet()) {
	// if (fields.containsKey(o.toString())) {
	// fields.get(o.toString()).setValue(property.getProperty(o.toString()));
	// fields.get(o.toString()).setCustom();
	// }
	// }
	// }

}
