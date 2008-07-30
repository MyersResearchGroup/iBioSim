package gcm2sbml.gui;

import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PromoterPanel extends JPanel {
	public PromoterPanel(String selected, PropertyList promoterList,
			PropertyList influencesList, GCMFile gcm) {
		super(new GridLayout(8, 1));
		this.selected = selected;
		this.promoterList = promoterList;
		this.influenceList = influencesList;
		this.gcm = gcm;

		fields = new HashMap<String, PropertyField>();

		// Name field
		PropertyField field = new PropertyField(GlobalConstants.NAME, "", null,
				null, Utility.IDstring);
		fields.put(GlobalConstants.NAME, field);
		add(field);		
		field = new PropertyField("ID", "", null,
				null, Utility.IDstring);
		
//		fields.put("ID", field);
//		add(field);
		
		// promoter count
		field = new PropertyField(GlobalConstants.PROMOTER_COUNT_STRING, gcm
				.getParameter(GlobalConstants.PROMOTER_COUNT_STRING),
				PropertyField.states[0], gcm
				.getParameter(GlobalConstants.PROMOTER_COUNT_STRING), Utility.NUMstring);
		fields.put(GlobalConstants.PROMOTER_COUNT_STRING, field);
		add(field);		
		
		// cooperativity
		field = new PropertyField(GlobalConstants.COOPERATIVITY_STRING, gcm
				.getParameter(GlobalConstants.COOPERATIVITY_STRING),
				PropertyField.states[0], gcm
				.getParameter(GlobalConstants.COOPERATIVITY_STRING), Utility.NUMstring);
		fields.put(GlobalConstants.COOPERATIVITY_STRING, field);
		add(field);		

		// RNAP binding
		field = new PropertyField(GlobalConstants.RNAP_BINDING_STRING, gcm
				.getParameter(GlobalConstants.RNAP_BINDING_STRING),
				PropertyField.states[0], gcm
				.getParameter(GlobalConstants.RNAP_BINDING_STRING), Utility.NUMstring);
		fields.put(GlobalConstants.RNAP_BINDING_STRING, field);
		add(field);
		
		// kocr
		field = new PropertyField(GlobalConstants.OCR_STRING, gcm
				.getParameter(GlobalConstants.OCR_STRING),
				PropertyField.states[0], gcm
				.getParameter(GlobalConstants.OCR_STRING), Utility.NUMstring);
		fields.put(GlobalConstants.OCR_STRING, field);
		add(field);
		
		// stoichiometry 
		field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, gcm
				.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
				PropertyField.states[0], gcm
				.getParameter(GlobalConstants.STOICHIOMETRY_STRING), Utility.NUMstring);
		fields.put(GlobalConstants.STOICHIOMETRY_STRING, field);
		add(field);		
		
		// kbasal
		field = new PropertyField(GlobalConstants.KBASAL_STRING, gcm
				.getParameter(GlobalConstants.KBASAL_STRING),
				PropertyField.states[0], gcm
						.getParameter(GlobalConstants.KBASAL_STRING),
				Utility.NUMstring);
		fields.put(GlobalConstants.KBASAL_STRING, field);
		add(field);
		
		// kactived production
		field = new PropertyField(GlobalConstants.ACTIVED_STRING, gcm
				.getParameter(GlobalConstants.ACTIVED_STRING),
				PropertyField.states[0], gcm
				.getParameter(GlobalConstants.ACTIVED_STRING), Utility.NUMstring);
		fields.put(GlobalConstants.ACTIVED_STRING, field);
		add(field);

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getPromoters().get(selected);
			fields.get(GlobalConstants.NAME).setValue(selected);
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
				"Promoter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getPromoters().containsKey(fields.get(GlobalConstants.NAME).getValue())) {
					Utility.createErrorMessage("Error", "Promoter name already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.NAME).getValue())) {
				if (gcm.getPromoters().containsKey(fields.get(GlobalConstants.NAME).getValue())) {
					Utility.createErrorMessage("Error","Promoter name already exists.");
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
//					if (f.getKey().equals("ID")) {
//						property.put(GlobalConstants.NAME, f.getValue());
//					}
				}
			}

			if (selected != null && !oldName.equals(id)) {
				gcm.changePromoterName(oldName, id);
				((DefaultListModel) influenceList.getModel()).clear();
				influenceList.addAllItem(gcm.getInfluences().keySet());
			}
			gcm.addPromoter(id, property);
			promoterList.removeItem(oldName);
			promoterList.addItem(id);
			promoterList.setSelectedValue(id, true);

		} else if (value == JOptionPane.NO_OPTION) {
			// System.out.println();
			return true;
		}
		return true;
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
}
