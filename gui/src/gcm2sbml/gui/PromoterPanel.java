package gcm2sbml.gui;

import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class PromoterPanel extends JPanel {
	public PromoterPanel(String selected, PropertyList promoterList,
			PropertyList influencesList, GCMFile gcm, boolean paramsOnly, BioSim biosim, GCMFile refGCM) {
		super(new GridLayout(8, 1));
		this.selected = selected;
		this.promoterList = promoterList;
		this.influenceList = influencesList;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.biosim = biosim;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null,
				null, Utility.IDstring, paramsOnly);
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.ID, field);
		add(field);		

		// Name field
		field = new PropertyField(GlobalConstants.NAME, "", null,
				null, Utility.NAMEstring, paramsOnly);
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.NAME, field);
		add(field);		
		
//		fields.put("ID", field);
//		add(field);
		
		// promoter count
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.PROMOTER_COUNT_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.PROMOTER_COUNT_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.PROMOTER_COUNT_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.PROMOTER_COUNT_STRING);
			}
			field = new PropertyField(GlobalConstants.PROMOTER_COUNT_STRING, gcm
					.getParameter(GlobalConstants.PROMOTER_COUNT_STRING),
					PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.PROMOTER_COUNT_STRING, gcm
					.getParameter(GlobalConstants.PROMOTER_COUNT_STRING),
					PropertyField.states[0], gcm
					.getParameter(GlobalConstants.PROMOTER_COUNT_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.PROMOTER_COUNT_STRING, field);
		add(field);		
		
		// cooperativity
//		field = new PropertyField(GlobalConstants.COOPERATIVITY_STRING, gcm
//				.getParameter(GlobalConstants.COOPERATIVITY_STRING),
//				PropertyField.states[0], gcm
//				.getParameter(GlobalConstants.COOPERATIVITY_STRING), Utility.NUMstring);
//		fields.put(GlobalConstants.COOPERATIVITY_STRING, field);
//		add(field);		

		// RNAP binding
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.RNAP_BINDING_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.RNAP_BINDING_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.RNAP_BINDING_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.RNAP_BINDING_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.RNAP_BINDING_STRING);
			}
			field = new PropertyField(GlobalConstants.RNAP_BINDING_STRING, gcm
					.getParameter(GlobalConstants.RNAP_BINDING_STRING),
					PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.RNAP_BINDING_STRING, gcm
					.getParameter(GlobalConstants.RNAP_BINDING_STRING),
					PropertyField.states[0], gcm
					.getParameter(GlobalConstants.RNAP_BINDING_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.RNAP_BINDING_STRING, field);
		add(field);
		
		// kocr
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.OCR_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.OCR_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.OCR_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.OCR_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.OCR_STRING);
			}
			field = new PropertyField(GlobalConstants.OCR_STRING, gcm
					.getParameter(GlobalConstants.OCR_STRING),
					PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.OCR_STRING, gcm
					.getParameter(GlobalConstants.OCR_STRING),
					PropertyField.states[0], gcm
					.getParameter(GlobalConstants.OCR_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.OCR_STRING, field);
		add(field);
		
		// stoichiometry
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.STOICHIOMETRY_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.STOICHIOMETRY_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.STOICHIOMETRY_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.STOICHIOMETRY_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.STOICHIOMETRY_STRING);
			}
			field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, gcm
					.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
					PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, gcm
					.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
					PropertyField.states[0], gcm
					.getParameter(GlobalConstants.STOICHIOMETRY_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.STOICHIOMETRY_STRING, field);
		add(field);		
		
		// kbasal
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KBASAL_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.KBASAL_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.KBASAL_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KBASAL_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KBASAL_STRING);
			}
			field = new PropertyField(GlobalConstants.KBASAL_STRING, gcm
					.getParameter(GlobalConstants.KBASAL_STRING),
					PropertyField.paramStates[0], defaultValue,
					Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.KBASAL_STRING, gcm
					.getParameter(GlobalConstants.KBASAL_STRING),
					PropertyField.states[0], gcm
							.getParameter(GlobalConstants.KBASAL_STRING),
					Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.KBASAL_STRING, field);
		add(field);
		
		// kactived production
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.ACTIVED_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.ACTIVED_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.ACTIVED_STRING);
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.ACTIVED_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.ACTIVED_STRING);
			}
			field = new PropertyField(GlobalConstants.ACTIVED_STRING, gcm
					.getParameter(GlobalConstants.ACTIVED_STRING),
					PropertyField.paramStates[0], defaultValue, Utility.SWEEPstring, paramsOnly);
		} else {
			field = new PropertyField(GlobalConstants.ACTIVED_STRING, gcm
					.getParameter(GlobalConstants.ACTIVED_STRING),
					PropertyField.states[0], gcm
					.getParameter(GlobalConstants.ACTIVED_STRING), Utility.NUMstring, paramsOnly);
		}
		fields.put(GlobalConstants.ACTIVED_STRING, field);
		add(field);

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getPromoters().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
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
	
	// Provide a public way to query what the last used (or created) promoter was.
	private String lastUsedPromoter;
	public String getLastUsedPromoter(){return lastUsedPromoter;}
	
	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(biosim.frame(), this,
				"Promoter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getPromoters().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Promoter id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getPromoters().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error","Promoter id already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			for (PropertyField f : fields.values()) {
				if (f.getState() == null
						|| f.getState().equals(PropertyField.states[1])
						|| f.getState().equals(PropertyField.paramStates[1])) {
					property.put(f.getKey(), f.getValue());
//					if (f.getKey().equals("ID")) {
//						property.put(GlobalConstants.NAME, f.getValue());
//					}
				}
			}

			// rename all the influences that use this promoter
			if (selected != null && !oldName.equals(id)) {
				gcm.changePromoterName(oldName, id);
				((DefaultListModel) influenceList.getModel()).clear();
				influenceList.addAllItem(gcm.getInfluences().keySet());
			}
			gcm.addPromoter(id, property);
			this.lastUsedPromoter = id;
			
			if (paramsOnly) {
				if (fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.RNAP_BINDING_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.OCR_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.KBASAL_STRING).getState().equals(PropertyField.paramStates[1]) ||
						fields.get(GlobalConstants.ACTIVED_STRING).getState().equals(PropertyField.paramStates[1])) {
					id += " Modified";
				}
			}
			promoterList.removeItem(oldName);
			promoterList.removeItem(oldName + " Modified");
			promoterList.addItem(id);
			promoterList.setSelectedValue(id, true);

		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
	}
	
	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getState().equals(PropertyField.paramStates[1])) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ CompatibilityFixer.getSBMLName(GlobalConstants.PROMOTER_COUNT_STRING) + " "
						+ fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getValue();
			}
			if (fields.get(GlobalConstants.RNAP_BINDING_STRING).getState()
					.equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.RNAP_BINDING_STRING) + " "
				+ fields.get(GlobalConstants.RNAP_BINDING_STRING).getValue();
			}
			if (fields.get(GlobalConstants.OCR_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.OCR_STRING) + " "
				+ fields.get(GlobalConstants.OCR_STRING).getValue();
			}
			if (fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.STOICHIOMETRY_STRING) + " "
				+ fields.get(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KBASAL_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.KBASAL_STRING) + " "
				+ fields.get(GlobalConstants.KBASAL_STRING).getValue();
			}
			if (fields.get(GlobalConstants.ACTIVED_STRING).getState().equals(PropertyField.paramStates[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ CompatibilityFixer.getSBMLName(GlobalConstants.ACTIVED_STRING) + " "
				+ fields.get(GlobalConstants.ACTIVED_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/";
			}
		}
		return updates;
	}

	public void actionPerformed(ActionEvent e) {
	}
	
	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(
						property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
//			if (o.equals(GlobalConstants.NAME)) {
//				fields.get("ID").setValue(
//						property.getProperty(o.toString()));
//			}
		}
	}
	
	
	private String[] options = { "Ok", "Cancel" };
	private HashMap<String, PropertyField> fields = null;
	private String selected = "";
	private GCMFile gcm = null;
	private PropertyList promoterList = null;
	private PropertyList influenceList = null;
	private boolean paramsOnly;
	private BioSim biosim;
}
