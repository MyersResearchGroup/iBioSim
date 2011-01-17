package gcm2sbml.gui;

import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class InfluencePanel extends JPanel implements ActionListener {
	public InfluencePanel(String selected, PropertyList list, GCMFile gcm, boolean paramsOnly, GCMFile refGCM, GCM2SBMLEditor gcmEditor) {
		super(new GridLayout(6, 1));
		this.selected = selected;
		this.list = list;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.gcmEditor = gcmEditor;

		fields = new HashMap<String, PropertyField>();

		// Name field
		PropertyField field = new PropertyField(GlobalConstants.NAME, "", null,
				null, "(.*)", paramsOnly, "default");
		field.setEnabled(false);
		fields.put(GlobalConstants.NAME, field);
		add(field);

		

		// Promoter field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel("Promoter");
		promoterBox = new JComboBox(gcm.getPromotersAsArray());
		((DefaultComboBoxModel) (promoterBox.getModel())).addElement("default");
		promoterBox.setSelectedItem("default");String origString = "default";
		promoterBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(promoterBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			promoterBox.setEnabled(false);
		}
		add(tempPanel);

		// Type field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Type");
		typeBox = new JComboBox(types);
		typeBox.setSelectedIndex(0);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			typeBox.setEnabled(false);
		}
		add(tempPanel);
		
		// coop
		String defString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.COOPERATIVITY_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.COOPERATIVITY_STRING);
				defString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.COOPERATIVITY_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.COOPERATIVITY_STRING);
			}
			field = new PropertyField(GlobalConstants.COOPERATIVITY_STRING, gcm
					.getParameter(GlobalConstants.COOPERATIVITY_STRING),
					defString, defaultValue,
					Utility.SWEEPstring, paramsOnly, defString);
		} else {
			field = new PropertyField(GlobalConstants.COOPERATIVITY_STRING, gcm
					.getParameter(GlobalConstants.COOPERATIVITY_STRING),
					defString, gcm
							.getParameter(GlobalConstants.COOPERATIVITY_STRING),
					Utility.NUMstring, paramsOnly, defString);
		}
		fields.put(GlobalConstants.COOPERATIVITY_STRING, field);
		add(field);

		// krep
		defString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KREP_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.KREP_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.KREP_STRING);
				defString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KREP_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KREP_STRING);
			}
			field = new PropertyField(GlobalConstants.KREP_STRING, gcm
					.getParameter(GlobalConstants.KREP_STRING),
					defString, defaultValue,
					Utility.SWEEPstring, paramsOnly, defString);
		} else {
			field = new PropertyField(GlobalConstants.KREP_STRING, gcm
					.getParameter(GlobalConstants.KREP_STRING),
					defString, gcm
							.getParameter(GlobalConstants.KREP_STRING),
					Utility.SLASHstring, paramsOnly, defString);
		}
		fields.put(GlobalConstants.KREP_STRING, field);
		add(field);

		// kact
		defString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KACT_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.KACT_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.KACT_STRING);
				defString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KACT_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KACT_STRING);
			}
			field = new PropertyField(GlobalConstants.KACT_STRING, gcm
					.getParameter(GlobalConstants.KACT_STRING),
					defString, defaultValue,
					Utility.SWEEPstring, paramsOnly, defString);
		} else {
			field = new PropertyField(GlobalConstants.KACT_STRING, gcm
					.getParameter(GlobalConstants.KACT_STRING),
					defString, gcm
							.getParameter(GlobalConstants.KACT_STRING),
					Utility.SLASHstring, paramsOnly, defString);
		}
		fields.put(GlobalConstants.KACT_STRING, field);
		field.setEnabled(false);
		add(field);

		setType(types[0]);
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getInfluences().get(selected);
			fields.get(GlobalConstants.NAME).setValue(selected);
			if (prop.containsKey(GlobalConstants.PROMOTER)) {
				promoterBox.setSelectedItem(prop.get(GlobalConstants.PROMOTER));
			} else {
				promoterBox.setSelectedItem("default");
			}
			if (prop.get(GlobalConstants.TYPE).equals(
					GlobalConstants.ACTIVATION)) {
				typeBox.setSelectedItem(types[1]);
				setType(types[1]);
			} else if (prop.get(GlobalConstants.TYPE).equals(
						GlobalConstants.REPRESSION)) {
				typeBox.setSelectedItem(types[0]);
				setType(types[0]);
				
			} else if (prop.get(GlobalConstants.TYPE).equals(
						GlobalConstants.COMPLEX)) {
				typeBox.setSelectedItem(types[3]);
				setType(types[3]);
			}else {
				typeBox.setSelectedItem(types[2]);
				setType(types[2]);
			}
			loadProperties(prop);
		}

		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(BioSim.frame, this,
				"Influence Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getInfluences().containsKey(
						fields.get(GlobalConstants.NAME).getValue())) {
					Utility.createErrorMessage("Error",
							"Influence already exists.");
					return false;
				}
			} else if (!oldName.equals(fields.get(GlobalConstants.NAME)
					.getValue())) {
				if (gcm.getInfluences().containsKey(
						fields.get(GlobalConstants.NAME).getValue())) {
					Utility.createErrorMessage("Error",
							"Influence already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.NAME).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null
						|| f.getState().equals(f.getStates()[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			property.put(GlobalConstants.TYPE, typeBox.getSelectedItem()
					.toString());
			String label = "";
			if (!promoterBox.getSelectedItem().equals("default")) {
				property.put(GlobalConstants.PROMOTER, promoterBox
						.getSelectedItem());
				label = promoterBox.getSelectedItem().toString();
			}
			property.put("label", "\"" + label + "\"");

			if (selected != null && !oldName.equals(id)) {
				list.removeItem(oldName);
				list.removeItem(oldName + " Modified");
				gcm.removeInfluence(oldName);
			}
			list.removeItem(id);
			list.removeItem(id + " Modified");
			gcm.addInfluences(id, property);
			if (paramsOnly) {
				if (fields.get(GlobalConstants.COOPERATIVITY_STRING).getState().equals(fields.get(GlobalConstants.COOPERATIVITY_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.KREP_STRING).getState().equals(fields.get(GlobalConstants.KREP_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.KACT_STRING).getState().equals(fields.get(GlobalConstants.KACT_STRING).getStates()[1])) {
					id += " Modified";
				}
			}
			list.addItem(id);
			list.setSelectedValue(id, true);
			gcmEditor.setDirty(true);
		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}
	
	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.COOPERATIVITY_STRING).getState().equals(fields.get(GlobalConstants.COOPERATIVITY_STRING).getStates()[1])) {
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
						+ CompatibilityFixer.getSBMLName(GlobalConstants.COOPERATIVITY_STRING) + " "
						+ fields.get(GlobalConstants.COOPERATIVITY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KREP_STRING).getState().equals(fields.get(GlobalConstants.KREP_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KREP_STRING) + " "
				+ fields.get(GlobalConstants.KREP_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KACT_STRING).getState().equals(fields.get(GlobalConstants.KACT_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KACT_STRING) + " "
				+ fields.get(GlobalConstants.KACT_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/";
			}
		}
		return updates;
	}

	/**
	 * Builds the influence name
	 * @param sourceId
	 * @param targetId
	 * @param type
	 * @param isBio: has to be "yes" or "no"
	 * @param promoter
	 * @return
	 */
	public static String buildName(String sourceId, String targetId, String type, String promoter){
		String arrow = " -| ";
		if (type==types[1]) {
			arrow = " -> ";
		} else if (type==types[2]) {
			arrow = " x> ";
		} else if (type==types[3]) {
			arrow = " +> ";
		} 
		
		String out = sourceId + arrow + targetId + ", Promoter " + promoter;
		
		return out;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")
				&& e.getSource().equals(typeBox)) {
			String type = typeBox.getSelectedItem().toString();
			setType(type);
			String arrow = "-|";
			if (type==types[1]) {
				arrow = "->";
			} else if (type==types[2]) {
				arrow = "x>";
			} else if (type==types[3]) {
				arrow = "+>";
			} 
			String newName = fields.get(GlobalConstants.NAME).getValue().replaceAll(".[>|]", arrow);
			fields.get(GlobalConstants.NAME).setValue(newName);

		} else if (e.getActionCommand().equals("comboBoxChanged")
				&& e.getSource().equals(promoterBox)) {
			String newName = fields.get(GlobalConstants.NAME).getValue().split("Promoter")[0] + "Promoter " + 
				promoterBox.getSelectedItem().toString();
			fields.get(GlobalConstants.NAME).setValue(newName);
		}
	}

	private void setType(String type) {
		if (type.equals(types[0])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(true);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			promoterBox.setEnabled(true);
		} else if (type.equals(types[1])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(true);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			promoterBox.setEnabled(true);
		} else if (type.equals(types[2])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(false);
			promoterBox.setSelectedItem("default");
			promoterBox.setEnabled(false);
		} else if (type.equals(types[3])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			promoterBox.setSelectedItem("default");
			promoterBox.setEnabled(false);
		} else {
			throw new IllegalStateException("Illegal state");
		}
	}

	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (o.equals(GlobalConstants.NAME)) {
				//do nothing
			}
			else if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(
						property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}

	private String[] options = { "Ok", "Cancel" };
	public static String[] types = { GlobalConstants.REPRESSION, GlobalConstants.ACTIVATION, 
		GlobalConstants.NOINFLUENCE, GlobalConstants.COMPLEX };
	public static String[] bio = { "no", "yes" };
	private HashMap<String, PropertyField> fields = null;
	private GCMFile gcm = null;
	private String selected = "";
	private JComboBox promoterBox = null;
	private JComboBox typeBox = null;
	private PropertyList list = null;
	private boolean paramsOnly;
	private GCM2SBMLEditor gcmEditor = null;
}