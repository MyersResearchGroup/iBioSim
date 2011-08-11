package gcm.gui;

import gcm.parser.CompatibilityFixer;
import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreeModel;

import sbol.SbolBrowser;

import main.Gui;


public class PromoterPanel extends JPanel {
	
//	private JTextField sbolPromoterText = new JTextField(20);
//	private JButton sbolPromoterButton = new JButton("Associate SBOL");
	
	private String[] options = { "Ok", "Cancel" };
	private HashMap<String, PropertyField> fields = null;
	private HashMap<String, SbolField> sbolFields;
	private String selected = "";
	private GCMFile gcm = null;
	private PropertyList promoterList = null;
	private PropertyList influenceList = null;
	private boolean paramsOnly;
	private GCM2SBMLEditor gcmEditor = null;
	
	public PromoterPanel(String selected, PropertyList promoterList,
			PropertyList influencesList, GCMFile gcm, boolean paramsOnly, GCMFile refGCM, GCM2SBMLEditor gcmEditor) {
		super(new GridLayout(11, 1));
		this.selected = selected;
		this.promoterList = promoterList;
		this.influenceList = influencesList;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.gcmEditor = gcmEditor;

		fields = new HashMap<String, PropertyField>();
		sbolFields = new HashMap<String, SbolField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null,
				null, Utility.IDstring, paramsOnly, "default");
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.ID, field);
		add(field);		

		// Name field
		field = new PropertyField(GlobalConstants.NAME, "", null,
				null, Utility.NAMEstring, paramsOnly, "default");
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.NAME, field);
		add(field);		
		
//		fields.put("ID", field);
//		add(field);
		
		// promoter count
		String origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.PROMOTER_COUNT_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.PROMOTER_COUNT_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.PROMOTER_COUNT_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.PROMOTER_COUNT_STRING);
			}
			field = new PropertyField(GlobalConstants.PROMOTER_COUNT_STRING, gcm
					.getParameter(GlobalConstants.PROMOTER_COUNT_STRING),
					origString, defaultValue, Utility.SWEEPstring, paramsOnly, origString);
		} else {
			field = new PropertyField(GlobalConstants.PROMOTER_COUNT_STRING, gcm
					.getParameter(GlobalConstants.PROMOTER_COUNT_STRING),
					origString, gcm
					.getParameter(GlobalConstants.PROMOTER_COUNT_STRING), Utility.NUMstring, paramsOnly, origString);
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
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.RNAP_BINDING_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.RNAP_BINDING_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.RNAP_BINDING_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.RNAP_BINDING_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.RNAP_BINDING_STRING);
			}
			field = new PropertyField(GlobalConstants.RNAP_BINDING_STRING, gcm
					.getParameter(GlobalConstants.RNAP_BINDING_STRING),
					origString, defaultValue, Utility.SLASHSWEEPstring, paramsOnly, origString);
		} else {
			field = new PropertyField(GlobalConstants.RNAP_BINDING_STRING, gcm
					.getParameter(GlobalConstants.RNAP_BINDING_STRING),
					origString, gcm
					.getParameter(GlobalConstants.RNAP_BINDING_STRING), Utility.SLASHstring, paramsOnly, origString);
		}
		fields.put(GlobalConstants.RNAP_BINDING_STRING, field);
		add(field);
		
		// Activated RNAP binding
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING);
			}
			field = new PropertyField(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, gcm
					.getParameter(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING),
					origString, defaultValue, Utility.SLASHSWEEPstring, paramsOnly, origString);
		} else {
			field = new PropertyField(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, gcm
					.getParameter(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING),
					origString, gcm
					.getParameter(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING), Utility.SLASHstring, paramsOnly, origString);
		}
		fields.put(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, field);
		add(field);
		
		// kocr
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.OCR_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.OCR_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.OCR_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.OCR_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.OCR_STRING);
			}
			field = new PropertyField(GlobalConstants.OCR_STRING, gcm
					.getParameter(GlobalConstants.OCR_STRING),
					origString, defaultValue, Utility.SWEEPstring, paramsOnly, origString);
		} else {
			field = new PropertyField(GlobalConstants.OCR_STRING, gcm
					.getParameter(GlobalConstants.OCR_STRING),
					origString, gcm
					.getParameter(GlobalConstants.OCR_STRING), Utility.NUMstring, paramsOnly, origString);
		}
		fields.put(GlobalConstants.OCR_STRING, field);
		add(field);
		
		// stoichiometry
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.STOICHIOMETRY_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.STOICHIOMETRY_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.STOICHIOMETRY_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.STOICHIOMETRY_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.STOICHIOMETRY_STRING);
			}
			field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, gcm
					.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
					origString, defaultValue, Utility.SWEEPstring, paramsOnly, origString);
		} else {
			field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, gcm
					.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
					origString, gcm
					.getParameter(GlobalConstants.STOICHIOMETRY_STRING), Utility.NUMstring, paramsOnly, origString);
		}
		fields.put(GlobalConstants.STOICHIOMETRY_STRING, field);
		add(field);		
		
		// kbasal
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KBASAL_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.KBASAL_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.KBASAL_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KBASAL_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KBASAL_STRING);
			}
			field = new PropertyField(GlobalConstants.KBASAL_STRING, gcm
					.getParameter(GlobalConstants.KBASAL_STRING),
					origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString);
		} else {
			field = new PropertyField(GlobalConstants.KBASAL_STRING, gcm
					.getParameter(GlobalConstants.KBASAL_STRING),
					origString, gcm
							.getParameter(GlobalConstants.KBASAL_STRING),
					Utility.NUMstring, paramsOnly, origString);
		}
		fields.put(GlobalConstants.KBASAL_STRING, field);
		add(field);
		
		// kactived production
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.ACTIVED_STRING);
			if (refGCM.getPromoters().get(selected).containsKey(GlobalConstants.ACTIVED_STRING)) {
				defaultValue = refGCM.getPromoters().get(selected).getProperty(GlobalConstants.ACTIVED_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.ACTIVED_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.ACTIVED_STRING);
			}
			field = new PropertyField(GlobalConstants.ACTIVED_STRING, gcm
					.getParameter(GlobalConstants.ACTIVED_STRING),
					origString, defaultValue, Utility.SWEEPstring, paramsOnly, origString);
		} else {
			field = new PropertyField(GlobalConstants.ACTIVED_STRING, gcm
					.getParameter(GlobalConstants.ACTIVED_STRING),
					origString, gcm
					.getParameter(GlobalConstants.ACTIVED_STRING), Utility.NUMstring, paramsOnly, origString);
		}
		fields.put(GlobalConstants.ACTIVED_STRING, field);
		add(field);
		
		// Panel for associating SBOL promoter element
		SbolField sField = new SbolField(GlobalConstants.SBOL_PROMOTER, gcmEditor);
		sbolFields.put(GlobalConstants.SBOL_PROMOTER, sField);
		add(sField);
		
		// Panel for associating SBOL terminator element
		sField = new SbolField(GlobalConstants.SBOL_TERMINATOR, gcmEditor);
		sbolFields.put(GlobalConstants.SBOL_TERMINATOR, sField);
		add(sField);

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
			if (!f.isValidValue()/* || f.getValue().equals("RNAP") || 
					f.getValue().endsWith("_RNAP") || f.getValue().endsWith("_bound")*/) {
				return false;
			}
		}
		return true;
	}
	
	private boolean checkSbolValues() {
		for (SbolField sf : sbolFields.values()) {
			if (!sf.isValidText())
				return false;
		}
		return true;
	}

	private boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this,
				"Promoter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			boolean sbolValueCheck = checkSbolValues();
			boolean valueCheck = checkValues();
			if (!valueCheck || !sbolValueCheck) {
				if (!valueCheck)
					Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getUsedIDs().contains((String)fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getUsedIDs().contains((String)fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error","Id already exists.");
					return false;
				}
			}
			String id = fields.get(GlobalConstants.ID).getValue();

			// Check to see if we need to add or edit
			Properties property = new Properties();
			
			// preserve positioning info
			if (oldName != null) {
				for (Object p : gcm.getPromoters().get(oldName).keySet()) {
					String k = p.toString();
					if (k.equals("graphwidth") || k.equals("graphheight") || k.equals("graphy") || k.equals("graphx")
							|| k.equals("drawn_promoter")) {
						String v = (gcm.getPromoters().get(oldName).getProperty(k)).toString();
						property.put(k, v);
					}
				}
			}
			
			
			for (PropertyField f : fields.values()) {
				if (f.getState() == null
						|| f.getState().equals(f.getStates()[1])) {
					property.put(f.getKey(), f.getValue());
//					if (f.getKey().equals("ID")) {
//						property.put(GlobalConstants.NAME, f.getValue());
//					}
				}
			}
			
			// Add SBOL properties
			for (SbolField sf : sbolFields.values()) {
				if (!sf.getText().equals(""))
					property.put(sf.getType(), sf.getText());
			}

			// rename all the influences that use this promoter if name was changed
			if (selected != null && !oldName.equals(id)) {
				while (gcm.getUsedIDs().contains(selected)) {
					gcm.getUsedIDs().remove(selected);
				}
				gcm.changePromoterName(oldName, id);
				((DefaultListModel) influenceList.getModel()).clear();
				influenceList.addAllItem(gcm.getInfluences().keySet());
				this.secondToLastUsedPromoter = oldName;
				promoterNameChange = true;
			}
			if (!gcm.getUsedIDs().contains(id)) {
				gcm.getUsedIDs().add(id);
			}
			gcm.addPromoter(id, property);
			this.lastUsedPromoter = id;
			
			
			if (paramsOnly) {
				if (fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getState().equals(fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.RNAP_BINDING_STRING).getState().equals(fields.get(GlobalConstants.RNAP_BINDING_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getState().equals(fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.OCR_STRING).getState().equals(fields.get(GlobalConstants.OCR_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(fields.get(GlobalConstants.STOICHIOMETRY_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.KBASAL_STRING).getState().equals(fields.get(GlobalConstants.KBASAL_STRING).getStates()[1]) ||
						fields.get(GlobalConstants.ACTIVED_STRING).getState().equals(fields.get(GlobalConstants.ACTIVED_STRING).getStates()[1])) {
					id += " Modified";
				}
			}
			promoterList.removeItem(oldName);
			promoterList.removeItem(oldName + " Modified");
			promoterList.addItem(id);
			promoterList.setSelectedValue(id, true);
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
			if (fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getState().equals(fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getStates()[1])) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.PROMOTER_COUNT_STRING + " "
						+ fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getValue();
			}
			if (fields.get(GlobalConstants.RNAP_BINDING_STRING).getState()
					.equals(fields.get(GlobalConstants.RNAP_BINDING_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ GlobalConstants.RNAP_BINDING_STRING + " "
				+ fields.get(GlobalConstants.RNAP_BINDING_STRING).getValue();
			}
			if (fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getState()
					.equals(fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ GlobalConstants.ACTIVATED_RNAP_BINDING_STRING + " "
				+ fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getValue();
			}
			if (fields.get(GlobalConstants.OCR_STRING).getState().equals(fields.get(GlobalConstants.OCR_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ GlobalConstants.OCR_STRING + " "
				+ fields.get(GlobalConstants.OCR_STRING).getValue();
			}
			if (fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(fields.get(GlobalConstants.STOICHIOMETRY_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ GlobalConstants.STOICHIOMETRY_STRING + " "
				+ fields.get(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KBASAL_STRING).getState().equals(fields.get(GlobalConstants.KBASAL_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ GlobalConstants.KBASAL_STRING + " "
				+ fields.get(GlobalConstants.KBASAL_STRING).getValue();
			}
			if (fields.get(GlobalConstants.ACTIVED_STRING).getState().equals(fields.get(GlobalConstants.ACTIVED_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
				+ GlobalConstants.ACTIVED_STRING + " "
				+ fields.get(GlobalConstants.ACTIVED_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/";
			}
		}
		return updates;
	}
	
	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(
						property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			} else if (sbolFields.containsKey(o.toString())) {
				sbolFields.get(o.toString()).setText(property.getProperty(o.toString()));
			}
//			if (o.equals(GlobalConstants.NAME)) {
//				fields.get("ID").setValue(
//						property.getProperty(o.toString()));
//			}
		}
	}
	
	// Provide a public way to query what the last used (or created) promoter was.
	private String lastUsedPromoter;
	public String getLastUsedPromoter(){return lastUsedPromoter;}
	
	private String secondToLastUsedPromoter;
	public String getSecondToLastUsedPromoter(){return secondToLastUsedPromoter;}
	
	private boolean promoterNameChange = false;
	public boolean wasPromoterNameChanged(){return promoterNameChange;}
	
}