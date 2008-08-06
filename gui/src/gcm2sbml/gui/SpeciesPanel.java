package gcm2sbml.gui;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SpeciesPanel extends JPanel implements ActionListener {
	public SpeciesPanel(String selected, PropertyList speciesList,
			PropertyList influencesList, GCMFile gcm) {
		super(new GridLayout(5, 1));
		this.selected = selected;
		this.speciesList = speciesList;
		this.influences = influencesList;
		this.gcm = gcm;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID,
				"", null, null, Utility.IDstring);
		fields.put(GlobalConstants.ID, field);
		add(field);
		
		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel(GlobalConstants.TYPE);
		typeBox = new JComboBox(types);
		typeBox.setSelectedItem(types[0]);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		add(tempPanel);

		// Initial field
		field = new PropertyField(GlobalConstants.INITIAL_STRING, gcm
				.getParameter(GlobalConstants.INITIAL_STRING),
				PropertyField.states[0], gcm
						.getParameter(GlobalConstants.INITIAL_STRING),
				Utility.NUMstring);
		fields.put(GlobalConstants.INITIAL_STRING, field);
		add(field);

		// Max dimer field
//		field = new PropertyField(GlobalConstants.MAX_DIMER_STRING, gcm
//				.getParameter(GlobalConstants.MAX_DIMER_STRING),
//			PropertyField.states[0], gcm
//						.getParameter(GlobalConstants.MAX_DIMER_STRING),
//				Utility.NUMstring);
//		fields.put(GlobalConstants.MAX_DIMER_STRING, field);
//		add(field);

		// Dimerization field
		field = new PropertyField(GlobalConstants.KASSOCIATION_STRING, gcm
				.getParameter(GlobalConstants.KASSOCIATION_STRING),
				PropertyField.states[0], gcm
						.getParameter(GlobalConstants.KASSOCIATION_STRING),
				Utility.NUMstring);
		fields.put(GlobalConstants.KASSOCIATION_STRING, field);
		add(field);

		// Decay field
		field = new PropertyField(GlobalConstants.KDECAY_STRING, gcm
				.getParameter(GlobalConstants.KDECAY_STRING),
				PropertyField.states[0], gcm
						.getParameter(GlobalConstants.KDECAY_STRING),
				Utility.NUMstring);
		fields.put(GlobalConstants.KDECAY_STRING, field);
		add(field);

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getSpecies().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			typeBox.setSelectedItem(prop.getProperty(GlobalConstants.TYPE));
			setType(prop.getProperty(GlobalConstants.TYPE));
			loadProperties(prop);
		}

		setType(types[0]);
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
				"Species Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Species id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error","Species id already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

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

			if (selected != null && !oldName.equals(id)) {
				gcm.changeSpeciesName(oldName, id);
				((DefaultListModel) influences.getModel()).clear();
				influences.addAllItem(gcm.getInfluences().keySet());
			}
			gcm.addSpecies(id, property);
			speciesList.removeItem(oldName);
			speciesList.addItem(id);
			speciesList.setSelectedValue(id, true);

		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			setType(typeBox.getSelectedItem().toString());
		}
	}

	private void setType(String type) {
		if (type.equals(types[0])) {
			//fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
		} else if (type.equals(types[1])) {
			//fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false);
		} else {
			//fields.get(GlobalConstants.MAX_DIMER_STRING).setEnabled(true);
			fields.get(GlobalConstants.KASSOCIATION_STRING).setEnabled(true);
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
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

	private String selected = "";

	private PropertyList speciesList = null;

	private PropertyList influences = null;

	private String[] options = { "Ok", "Cancel" };

	private GCMFile gcm = null;

	private JComboBox typeBox = null;

	private static final String[] types = new String[] { "normal", "boundary",
			"unconstrained" };

	private HashMap<String, PropertyField> fields = null;
}
