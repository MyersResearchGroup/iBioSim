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
	public InfluencePanel(String selected, PropertyList list, GCMFile gcm, boolean paramsOnly, BioSim biosim, GCMFile refGCM) {
		super(new GridLayout(11, 1));
		this.selected = selected;
		this.list = list;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.biosim = biosim;

		fields = new HashMap<String, PropertyField>();

		// Name field
		PropertyField field = new PropertyField(GlobalConstants.NAME, "", null,
				null, "(.*)", paramsOnly);
		field.setEnabled(false);
		fields.put(GlobalConstants.NAME, field);
		add(field);

		// Input field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel("Input");
		inputBox = new JComboBox(gcm.getSpeciesAsArray());
		inputBox.addItem("none");
		// inputBox.setSelectedItem();
		inputBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(inputBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			inputBox.setEnabled(false);
		}
		add(tempPanel);

		// Output field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Output");
		outputBox = new JComboBox(gcm.getSpeciesAsArray());
		outputBox.addItem("none");
		// outputBox.setSelectedItem(types[0]);
		outputBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(outputBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			outputBox.setEnabled(false);
		}
		add(tempPanel);

		// Promoter field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Promoter");
		promoterBox = new JComboBox(gcm.getPromotersAsArray());
		((DefaultComboBoxModel) (promoterBox.getModel())).addElement("default");
		promoterBox.setSelectedItem("default");
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

		// Biochemical field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Biochemical");
		bioBox = new JComboBox(bio);
		bioBox.setSelectedIndex(0);
		bioBox.addActionListener(this);
		// bioBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(bioBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			bioBox.setEnabled(false);
		}
		add(tempPanel);

		fields.get(GlobalConstants.NAME).setValue(inputBox.getSelectedItem() + " -| "
				+ outputBox.getSelectedItem() + ", Promoter "
				+ promoterBox.getSelectedItem());
		
		// coop
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.COOPERATIVITY_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.COOPERATIVITY_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.COOPERATIVITY_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.COOPERATIVITY_STRING);
			}
			field = new PropertyField(GlobalConstants.COOPERATIVITY_STRING, gcm
					.getParameter(GlobalConstants.COOPERATIVITY_STRING),
					PropertyField.paramStates[0], defaultValue,
					Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.COOPERATIVITY_STRING, gcm
					.getParameter(GlobalConstants.COOPERATIVITY_STRING),
					PropertyField.states[0], gcm
							.getParameter(GlobalConstants.COOPERATIVITY_STRING),
					Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.COOPERATIVITY_STRING, field);
		add(field);

		// dimer
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.MAX_DIMER_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.MAX_DIMER_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.MAX_DIMER_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.MAX_DIMER_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.MAX_DIMER_STRING);
			}
			field = new PropertyField(GlobalConstants.MAX_DIMER_STRING, gcm
					.getParameter(GlobalConstants.MAX_DIMER_STRING),
					PropertyField.paramStates[0], defaultValue,
					Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.MAX_DIMER_STRING, gcm
					.getParameter(GlobalConstants.MAX_DIMER_STRING),
					PropertyField.states[0], gcm
							.getParameter(GlobalConstants.MAX_DIMER_STRING),
					Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.MAX_DIMER_STRING, field);
		add(field);

		// krep
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KREP_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.KREP_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.KREP_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KREP_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KREP_STRING);
			}
			field = new PropertyField(GlobalConstants.KREP_STRING, gcm
					.getParameter(GlobalConstants.KREP_STRING),
					PropertyField.paramStates[0], defaultValue,
					Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.KREP_STRING, gcm
					.getParameter(GlobalConstants.KREP_STRING),
					PropertyField.states[0], gcm
							.getParameter(GlobalConstants.KREP_STRING),
					Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.KREP_STRING, field);
		add(field);

		// kact
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KACT_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.KACT_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.KACT_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KACT_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KACT_STRING);
			}
			field = new PropertyField(GlobalConstants.KACT_STRING, gcm
					.getParameter(GlobalConstants.KACT_STRING),
					PropertyField.paramStates[0], defaultValue,
					Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.KACT_STRING, gcm
					.getParameter(GlobalConstants.KACT_STRING),
					PropertyField.states[0], gcm
							.getParameter(GlobalConstants.KACT_STRING),
					Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.KACT_STRING, field);
		field.setEnabled(false);
		add(field);

		// kbio
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KBIO_STRING);
			if (refGCM.getInfluences().get(selected).containsKey(GlobalConstants.KBIO_STRING)) {
				defaultValue = refGCM.getInfluences().get(selected).getProperty(GlobalConstants.KBIO_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KBIO_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KBIO_STRING);
			}
			field = new PropertyField(GlobalConstants.KBIO_STRING, gcm
					.getParameter(GlobalConstants.KBIO_STRING),
					PropertyField.paramStates[0], defaultValue,
					Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.KBIO_STRING, gcm
					.getParameter(GlobalConstants.KBIO_STRING),
					PropertyField.states[0], gcm
							.getParameter(GlobalConstants.KBIO_STRING),
					Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.KBIO_STRING, field);
		field.setEnabled(false);
		add(field);

		setType(types[0]);
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getInfluences().get(selected);
			fields.get(GlobalConstants.NAME).setValue(selected);
			inputBox.setSelectedItem(GCMFile.getInput(selected));
			outputBox.setSelectedItem(GCMFile.getOutput(selected));
			if (prop.containsKey(GlobalConstants.BIO)) {
				bioBox.setSelectedItem(bio[1]);
				fields.get(GlobalConstants.KBIO_STRING).setEnabled(true);
			} else {
				bioBox.setSelectedItem(bio[0]);
				fields.get(GlobalConstants.KBIO_STRING).setEnabled(false);
			}
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
			} else {
				typeBox.setSelectedItem(types[2]);
				setType(types[2]);
				promoterBox.setSelectedItem("default");
				promoterBox.setEnabled(false);
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
		int value = JOptionPane.showOptionDialog(biosim.frame(), this,
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
						|| f.getState().equals(PropertyField.states[1])
						|| f.getState().equals(PropertyField.paramStates[1])) {
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
			if (bioBox.getSelectedItem().equals(bio[1])) {
				property.put(GlobalConstants.BIO, bio[1]);
				label = label + "+";
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
				if (fields.get(GlobalConstants.COOPERATIVITY_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.MAX_DIMER_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.KREP_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.KACT_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.KBIO_STRING).getState().equals(PropertyField.paramStates[1])) {
					id += " Modified";
				}
			}
			list.addItem(id);
			list.setSelectedValue(id, true);
		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}
	
	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.COOPERATIVITY_STRING).getState().equals(PropertyField.paramStates[1])) {
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
						+ CompatibilityFixer.getSBMLName(GlobalConstants.COOPERATIVITY_STRING) + " "
						+ fields.get(GlobalConstants.COOPERATIVITY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.MAX_DIMER_STRING).getState()
					.equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.MAX_DIMER_STRING) + " "
				+ fields.get(GlobalConstants.MAX_DIMER_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KREP_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KREP_STRING) + " "
				+ fields.get(GlobalConstants.KREP_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KACT_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KACT_STRING) + " "
				+ fields.get(GlobalConstants.KACT_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KBIO_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KBIO_STRING) + " "
				+ fields.get(GlobalConstants.KBIO_STRING).getValue();
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
	public static String buildName(String sourceId, String targetId, String type, String isBio, String promoter){
		String arrow = " -> ";
		if ((type==types[0]) && (isBio.equals(bio[0]))) {
			arrow = " -| ";
		} else if ((type==types[0]) && (isBio.equals(bio[1]))) {
			arrow = " +| ";
		} else if ((type==types[1]) && (isBio.equals(bio[1]))) {
			arrow = " +> ";
		} else if ((type==types[2])) {
			arrow = " x> ";
		}
		
		String out = sourceId + arrow + targetId + ", Promoter " + promoter;
		
		return out;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")
				&& e.getSource().equals(typeBox)) {
			setType(typeBox.getSelectedItem().toString());

			fields.get(GlobalConstants.NAME).setValue(buildName(
					inputBox.getSelectedItem().toString(), 
					outputBox.getSelectedItem().toString(),
					typeBox.getSelectedItem().toString(),
					bioBox.getSelectedItem().toString(),
					promoterBox.getSelectedItem().toString()
					));
			
			if (typeBox.getSelectedItem().equals("no influence")) {
				promoterBox.setSelectedItem("default");
				promoterBox.setEnabled(false);
			} else {
				promoterBox.setEnabled(true);
			}

		} else if (e.getActionCommand().equals("comboBoxChanged")
				&& (e.getSource().equals(inputBox) || e.getSource().equals(
						outputBox)) || e.getSource().equals(promoterBox)) {
			
			fields.get(GlobalConstants.NAME).setValue(buildName(
					inputBox.getSelectedItem().toString(), 
					outputBox.getSelectedItem().toString(),
					typeBox.getSelectedItem().toString(),
					bioBox.getSelectedItem().toString(),
					promoterBox.getSelectedItem().toString()
					));
		}
		if (e.getActionCommand().equals("comboBoxChanged")
				&& (e.getSource().equals(bioBox))) {
			boolean state = false;
			if (bioBox.getSelectedItem().equals(bio[0])) {
				state = false;
			} else {
				state = true;
			}
			fields.get(GlobalConstants.KBIO_STRING).setEnabled(state);
			
			fields.get(GlobalConstants.NAME).setValue(buildName(
					inputBox.getSelectedItem().toString(), 
					outputBox.getSelectedItem().toString(),
					typeBox.getSelectedItem().toString(),
					bioBox.getSelectedItem().toString(),
					promoterBox.getSelectedItem().toString()
					));
		}
	}

	private void setType(String type) {
		if (type.equals(types[0])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(true);
		} else if (type.equals(types[1])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(true);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
		} else if (type.equals(types[2])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
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
	public static String[] types = { "repression", "activation", "no influence" };
	private static String[] bio = { "no", "yes" };
	private HashMap<String, PropertyField> fields = null;
	private GCMFile gcm = null;
	private String selected = "";
	private JComboBox inputBox = null;
	private JComboBox outputBox = null;
	private JComboBox promoterBox = null;
	private JComboBox bioBox = null;
	private JComboBox typeBox = null;
	private PropertyList list = null;
	private boolean paramsOnly;
	private BioSim biosim;
}