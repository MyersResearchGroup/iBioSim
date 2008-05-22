package gcm2sbml.gui;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class InfluencePanel extends JPanel implements ActionListener {
	public InfluencePanel(String selected, PropertyList list, GCMFile gcm) {
		super(new GridLayout(9, 1));
		this.selected = selected;
		this.list = list;
		this.gcm = gcm;

		fields = new HashMap<String, PropertyField>();

		// Name field
		PropertyField field = new PropertyField(GlobalConstants.NAME, "", null,
				null, "(.*)");
		field.setEnabled(false);
		fields.put(GlobalConstants.NAME, field);
		add(field);

		// Input field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel("Input");
		inputBox = new JComboBox(gcm.getSpeciesAsArray());
		// inputBox.setSelectedItem();
		inputBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(inputBox);
		add(tempPanel);

		// Output field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Output");
		outputBox = new JComboBox(gcm.getSpeciesAsArray());
		// outputBox.setSelectedItem(types[0]);
		outputBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(outputBox);
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
		add(tempPanel);

		// Biochemical field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Biochemical");
		bioBox = new JComboBox(bio);
		bioBox.setSelectedIndex(0);
		// bioBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(bioBox);
		add(tempPanel);

		// dimer
		field = new PropertyField(GlobalConstants.MAX_DIMER_STRING, gcm
				.getParameter(GlobalConstants.MAX_DIMER_STRING),
				PropertyField.states[0], gcm
						.getParameter(GlobalConstants.MAX_DIMER_STRING),
				Utility.NUMstring);
		fields.put(GlobalConstants.MAX_DIMER_STRING, field);
		add(field);

		// krep
		field = new PropertyField(GlobalConstants.KREP_STRING, gcm
				.getParameter(GlobalConstants.KREP_STRING),
				PropertyField.states[0], gcm
						.getParameter(GlobalConstants.KREP_STRING),
				Utility.NUMstring);
		fields.put(GlobalConstants.KREP_STRING, field);
		add(field);

		// kact
		field = new PropertyField(GlobalConstants.KACT_STRING, gcm
				.getParameter(GlobalConstants.KACT_STRING),
				PropertyField.states[0], gcm
						.getParameter(GlobalConstants.KACT_STRING),
				Utility.NUMstring);
		fields.put(GlobalConstants.KACT_STRING, field);
		field.setEnabled(false);
		add(field);
		
		setType(types[0]);
		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getInfluences().get(selected);
			fields.get(GlobalConstants.NAME).setValue(selected);
			inputBox.setSelectedItem(gcm.getInput(selected));
			outputBox.setSelectedItem(gcm.getOutput(selected));
			if (prop.get(GlobalConstants.TYPE).equals(
					GlobalConstants.ACTIVATION)) {
				typeBox.setSelectedItem(types[1]);
				setType(types[1]);
			} else {
				typeBox.setSelectedItem(types[0]);
				setType(types[0]);
			}
			if (prop.containsKey(GlobalConstants.BIO)) {
				bioBox.setSelectedItem(bio[1]);
			} else {
				bioBox.setSelectedItem(bio[0]);
			}
			if (prop.containsKey(GlobalConstants.PROMOTER)) {
				promoterBox.setSelectedItem(prop.get(GlobalConstants.PROMOTER));
			} else {
				promoterBox.setSelectedItem("default");
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
			if (!f.isValid()) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(new JFrame(), this,
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
						|| f.getState().equals(PropertyField.states[1])) {
					property.put(f.getKey(), f.getValue());
				}
			}
			property.put(GlobalConstants.TYPE, typeBox.getSelectedItem()
					.toString());
			String label = "";
			if (!promoterBox.getSelectedItem().equals("default")) {
				property.put(GlobalConstants.PROMOTER, promoterBox.getSelectedItem());
				label = promoterBox.getSelectedItem().toString();
			}
			if (bioBox.getSelectedItem().equals(bio[1])) {
				property.put(GlobalConstants.BIO, bio[1]);
				label = label + "+";
			}
			property.put("label", "\""+label+"\"");

			if (selected != null && !oldName.equals(id)) {
				list.removeItem(oldName);
				gcm.removeInfluence(oldName);
			}
			list.removeItem(id);
			list.addItem(id);			
			gcm.addInfluences(id, property);						
		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")
				&& e.getSource().equals(typeBox)) {
			setType(typeBox.getSelectedItem().toString());
		} else if (e.getActionCommand().equals("comboBoxChanged")
				&& (e.getSource().equals(inputBox) || e.getSource().equals(
						outputBox)) || e.getSource().equals(promoterBox)) {
			fields.get(GlobalConstants.NAME).setValue(
					inputBox.getSelectedItem() + " -> "
							+ outputBox.getSelectedItem() + ", Promoter " + promoterBox.getSelectedItem());
		}
	}

	private void setType(String type) {
		if (type.equals(types[0])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(true);
		} else if (type.equals(types[1])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(true);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
		} else {
			throw new IllegalStateException("Illegal state");
		}
	}

	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(
						property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}

	private String[] options = { "Ok", "Cancel" };
	private String[] types = { "repression", "activation" };
	private String[] bio = { "no", "yes" };
	private HashMap<String, PropertyField> fields = null;
	private GCMFile gcm = null;
	private String selected = "";
	private JComboBox inputBox = null;
	private JComboBox outputBox = null;
	private JComboBox promoterBox = null;
	private JComboBox bioBox = null;
	private JComboBox typeBox = null;
	private PropertyList list = null;
}