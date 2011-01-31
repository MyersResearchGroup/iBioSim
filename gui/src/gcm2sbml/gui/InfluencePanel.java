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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class InfluencePanel extends JPanel implements ActionListener {
	public InfluencePanel(String origSelection, PropertyList list, GCMFile gcm, boolean paramsOnly, GCMFile refGCM, GCM2SBMLEditor gcmEditor) {
		super(new GridLayout(6, 1));
		this.origSelection = origSelection;
		this.list = list;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.gcmEditor = gcmEditor;
		this.promoterNameChange = false;
		this.promoterSetToNone = false;

		fields = new HashMap<String, PropertyField>();

		// Name field
		PropertyField field = new PropertyField(GlobalConstants.NAME, "", null,
				null, "(.*)", paramsOnly, "default");
		field.setEnabled(false);
		fields.put(GlobalConstants.NAME, field);
		add(field);

		// Variables used repeatedly for different parts of the UI.
		JPanel tempPanel;
		JLabel tempLabel;
		
		// Promoter field. Disabled if the influences is connected to an explicit promoter.
		tempPanel = new JPanel();
		tempLabel = new JLabel("Promoter");
		if (gcm.influenceHasExplicitPromoter(origSelection))
			promoterBox = new JComboBox(gcm.getPromotersAsArray());
		else
			promoterBox = new JComboBox(gcm.getImplicitPromotersAsArray());
		promoterBox.addActionListener(this);
		promoterButton = new JButton("Edit Promoter");
		promoterButton.setActionCommand("buttonPushed");
		promoterButton.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 3));
		tempPanel.add(tempLabel);
		tempPanel.add(promoterBox);
		tempPanel.add(promoterButton);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			promoterBox.setEnabled(false);
		}
		add(tempPanel);
		// disable the promoter stuff if the promoter is explicitly drawn.
		boolean explicitPromoter = false;
		if(gcm.influenceHasExplicitPromoter(origSelection)){	
			promoterBox.setEnabled(false);
			promoterButton.setEnabled(false);
			explicitPromoter = true;
		}


		// Type field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Type");
		if(explicitPromoter)
			typeBox = new JComboBox(explicitPromoterTypes);
		else
			typeBox = new JComboBox(types);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 3));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		tempPanel.add(new JLabel());
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			typeBox.setEnabled(false);
		}
		add(tempPanel);
		// Production is never a selectable type
		((DefaultComboBoxModel) (typeBox.getModel())).removeElement(types[4]);
		
		// coop
		String defString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.COOPERATIVITY_STRING);
			if (refGCM.getInfluences().get(origSelection).containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
				defaultValue = refGCM.getInfluences().get(origSelection).getProperty(GlobalConstants.COOPERATIVITY_STRING);
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
			if (refGCM.getInfluences().get(origSelection).containsKey(GlobalConstants.KREP_STRING)) {
				defaultValue = refGCM.getInfluences().get(origSelection).getProperty(GlobalConstants.KREP_STRING);
				defString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KREP_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KREP_STRING);
			}
			field = new PropertyField(GlobalConstants.KREP_STRING, gcm
					.getParameter(GlobalConstants.KREP_STRING),
					defString, defaultValue,
					Utility.SLASHSWEEPstring, paramsOnly, defString);
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
			if (refGCM.getInfluences().get(origSelection).containsKey(GlobalConstants.KACT_STRING)) {
				defaultValue = refGCM.getInfluences().get(origSelection).getProperty(GlobalConstants.KACT_STRING);
				defString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KACT_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KACT_STRING);
			}
			field = new PropertyField(GlobalConstants.KACT_STRING, gcm
					.getParameter(GlobalConstants.KACT_STRING),
					defString, defaultValue,
					Utility.SLASHSWEEPstring, paramsOnly, defString);
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

		if (origSelection != null) {
			Properties prop = gcm.getInfluences().get(origSelection);
			fields.get(GlobalConstants.NAME).setValue(origSelection);
			if (prop.containsKey(GlobalConstants.PROMOTER))
				promoterBox.setSelectedItem(prop.getProperty(GlobalConstants.PROMOTER));
			if (prop.get(GlobalConstants.TYPE).equals(
					GlobalConstants.ACTIVATION)) {
				//This will generate the action command associated with changing the type combo box
				typeBox.setSelectedItem(types[1]);
			} else if (prop.get(GlobalConstants.TYPE).equals(
						GlobalConstants.REPRESSION)) {
				typeBox.setSelectedItem(types[0]);
			} else if (prop.get(GlobalConstants.TYPE).equals(
						GlobalConstants.COMPLEX)) {
				typeBox.setSelectedItem(types[3]);
			}else {
				typeBox.setSelectedItem(types[2]);
			}
			loadProperties(prop);
		}

		boolean display = false;
		while (!display) {
			display = openGui();
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

	private boolean openGui() {
		int value = JOptionPane.showOptionDialog(BioSim.frame, this,
				"Influence Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (origSelection == null) {
				if (gcm.getInfluences().containsKey(
						fields.get(GlobalConstants.NAME).getValue())) {
					Utility.createErrorMessage("Error",
							"Influence already exists.");
					return false;
				}
			} else if (!origSelection.equals(fields.get(GlobalConstants.NAME)
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
			if (!promoterBox.getSelectedItem().equals("none")) {
				property.put(GlobalConstants.PROMOTER, promoterBox
						.getSelectedItem());
				label = promoterBox.getSelectedItem().toString();
			}
			property.put("label", "\"" + label + "\"");

			if (origSelection != null && !origSelection.equals(id)) {
				list.removeItem(origSelection);
				list.removeItem(origSelection + " Modified");
				gcm.removeInfluence(origSelection);
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
						+ GlobalConstants.COOPERATIVITY_STRING + " "
						+ fields.get(GlobalConstants.COOPERATIVITY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KREP_STRING).getState().equals(fields.get(GlobalConstants.KREP_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ GlobalConstants.KREP_STRING + " "
				+ fields.get(GlobalConstants.KREP_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KACT_STRING).getState().equals(fields.get(GlobalConstants.KACT_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += "\"" + fields.get(GlobalConstants.NAME).getValue() + "\"/"
				+ GlobalConstants.KACT_STRING + " "
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
			String oldName = fields.get(GlobalConstants.NAME).getValue();
			String newName = oldName.split("Promoter")[0] + "Promoter " + promoterBox.getSelectedItem().toString();
			fields.get(GlobalConstants.NAME).setValue(newName);
			if (promoterNameChange && oldName.equals(origSelection)) {
				origSelection = newName;
			}
			promoterNameChange = false;
		} else if (e.getActionCommand().equals("buttonPushed") && e.getSource().equals(promoterButton)) {
			PromoterPanel promPan = gcmEditor.launchPromoterPanel(promoterBox.getSelectedItem().toString());
			//Updates panel's selected promoter (and influence name) if promoter was renamed during user edit
			promoterNameChange = promPan.wasPromoterNameChanged();
			if (promoterNameChange) {
				String newPromoterName = promPan.getLastUsedPromoter();
				String oldPromoterName = promPan.getSecondToLastUsedPromoter();
				((DefaultComboBoxModel) (promoterBox.getModel())).addElement(newPromoterName);
				//This will generate the action command associated with changing the promoter combo box
				promoterBox.setSelectedItem(newPromoterName);
				((DefaultComboBoxModel) (promoterBox.getModel())).removeElement(oldPromoterName);
			}
		}
	}

	private void setType(String type) {
		if (type.equals(types[0])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(true);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			if (promoterSetToNone) {
				promoterBox.setSelectedIndex(0);
				((DefaultComboBoxModel) (promoterBox.getModel())).removeElement("none");
				promoterSetToNone = false;			
			}
			if (!gcm.influenceHasExplicitPromoter(origSelection)) {	
				promoterBox.setEnabled(true);
				promoterButton.setEnabled(true);
			}
		} else if (type.equals(types[1])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(true);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			if (promoterSetToNone) {
				promoterBox.setSelectedIndex(0);
				((DefaultComboBoxModel) (promoterBox.getModel())).removeElement("none");
				promoterSetToNone = false;			
			}
			if (!gcm.influenceHasExplicitPromoter(origSelection)) {	
				promoterBox.setEnabled(true);
				promoterButton.setEnabled(true);
			}
		} else if (type.equals(types[2])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(false);
			if (!promoterSetToNone) {
				((DefaultComboBoxModel) (promoterBox.getModel())).addElement("none");
				promoterBox.setSelectedItem("none");
				promoterSetToNone = true;
			}
			promoterBox.setEnabled(false);
			promoterButton.setEnabled(false);
		} else if (type.equals(types[3])) {
			fields.get(GlobalConstants.KACT_STRING).setEnabled(false);
			fields.get(GlobalConstants.KREP_STRING).setEnabled(false);
			fields.get(GlobalConstants.COOPERATIVITY_STRING).setEnabled(true);
			if (!promoterSetToNone) {
				((DefaultComboBoxModel) (promoterBox.getModel())).addElement("none");
				promoterBox.setSelectedItem("none");
				promoterSetToNone = true;
			}
			promoterBox.setEnabled(false);
			promoterButton.setEnabled(false);
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
		GlobalConstants.NOINFLUENCE, GlobalConstants.COMPLEX, GlobalConstants.PRODUCTION};
	 // options available for explicit promoters. Complex makes no sense, and no_influence breaks things.
	public static String[] explicitPromoterTypes = { GlobalConstants.REPRESSION, GlobalConstants.ACTIVATION};
	public static String[] bio = { "no", "yes" };
	private HashMap<String, PropertyField> fields = null;
	private GCMFile gcm = null;
	private String origSelection = "";
	private JComboBox promoterBox = null;
	private JComboBox typeBox = null;
	private JButton promoterButton;
	private PropertyList list = null;
	private boolean paramsOnly;
	private boolean promoterNameChange;
	private boolean promoterSetToNone;
	private GCM2SBMLEditor gcmEditor = null;
}