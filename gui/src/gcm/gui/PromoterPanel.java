package gcm.gui;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;
import gcm.util.Utility;

import java.awt.GridLayout;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;

import main.Gui;


public class PromoterPanel extends JPanel {
	
//	private JTextField sbolPromoterText = new JTextField(20);
//	private JButton sbolPromoterButton = new JButton("Associate SBOL");

	private static final long serialVersionUID = 5873800942710657929L;
	private String[] options = { "Ok", "Cancel" };
	private HashMap<String, PropertyField> fields = null;
	private HashMap<String, SbolField> sbolFields;
	private String selected = "";
	private GCMFile gcm = null;
	private boolean paramsOnly;
	private ModelEditor gcmEditor = null;
	private Species promoter = null;
	private Reaction production = null;
	
	public PromoterPanel(String selected, GCMFile gcm, boolean paramsOnly, GCMFile refGCM, 
			ModelEditor gcmEditor) {
		super(new GridLayout(paramsOnly?7:11, 1));
		this.selected = selected;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.gcmEditor = gcmEditor;

		fields = new HashMap<String, PropertyField>();
		sbolFields = new HashMap<String, SbolField>();

		Model model = gcm.getSBMLDocument().getModel();
		promoter = model.getSpecies(selected);

		PropertyField field  = null;
		if (!paramsOnly) {
			// ID field
			field = new PropertyField(GlobalConstants.ID, promoter.getId(), null, null, Utility.IDstring, paramsOnly, "default", false);
			fields.put(GlobalConstants.ID, field);
			add(field);		
			// Name field
			field = new PropertyField(GlobalConstants.NAME, promoter.getName(), null, null, Utility.NAMEstring, paramsOnly, "default", false);
			fields.put(GlobalConstants.NAME, field);
			add(field);		
		}
	
		production = model.getReaction("Production_"+selected);
		if (production != null) {
			if (!production.isSetAnnotation() || !production.getAnnotationString().contains("Production")) production = null;
		}
		
		// promoter count
		String origString = "default";
		String defaultValue = gcm.getParameter(GlobalConstants.PROMOTER_COUNT_STRING);
		String formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (refGCM.getSBMLDocument().getModel().getSpecies(promoter.getId()).getInitialAmount() != 
					model.getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue()) {
				defaultValue = ""+refGCM.getSBMLDocument().getModel().getSpecies(promoter.getId()).getInitialAmount();
				origString = "custom";
			}
			formatString = Utility.SWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.PROMOTER_COUNT_STRING, 
				gcm.getParameter(GlobalConstants.PROMOTER_COUNT_STRING), origString, 
				defaultValue, formatString, paramsOnly, origString, false);
		if (!defaultValue.equals(""+promoter.getInitialAmount())) {
			field.setValue(""+promoter.getInitialAmount());
			field.setCustom();
		}
		fields.put(GlobalConstants.PROMOTER_COUNT_STRING, field);
		add(field);	

		// RNAP binding
		origString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING) + "/" + 
				gcm.getParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			/*
			defaultValue = refGCM.getParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING)+"/"+
					refGCM.getParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING); */
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter ko_f = refProd.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
				LocalParameter ko_r = refProd.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
				if (ko_f != null && ko_r != null) {
					defaultValue = ko_f.getValue()+"/"+ko_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.RNAP_BINDING_STRING, defaultValue, origString, defaultValue, formatString, paramsOnly,
				origString, false);
		if (production != null) {
			LocalParameter ko_f = production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			LocalParameter ko_r = production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			if (ko_f != null && ko_r != null && !defaultValue.equals(ko_f.getValue()+"/"+ko_r.getValue())) {
				field.setValue(ko_f.getValue()+"/"+ko_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.RNAP_BINDING_STRING, field);
		add(field);
		
		// Activated RNAP binding
		origString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING) + "/" + 
				gcm.getParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			/*
			defaultValue = refGCM.getParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING) + "/" + 
					refGCM.getParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING); */
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter kao_f = refProd.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
				LocalParameter kao_r = refProd.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
				if (kao_f != null && kao_r != null) {
					defaultValue =  kao_f.getValue()+"/"+kao_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		} 
		field = new PropertyField(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, defaultValue, origString, defaultValue, 
				formatString, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter kao_f = production.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
			LocalParameter kao_r = production.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
			if (kao_f != null && kao_r != null && !defaultValue.equals(kao_f.getValue()+"/"+kao_r.getValue())) {
				field.setValue(kao_f.getValue()+"/"+kao_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, field);
		add(field);
		
		// kocr
		origString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.OCR_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			//defaultValue = refGCM.getParameter(GlobalConstants.OCR_STRING);
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter ko = refProd.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
				if (ko != null) {
					defaultValue = ko.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.OCR_STRING, gcm.getParameter(GlobalConstants.OCR_STRING),
				origString, defaultValue, Utility.NUMstring, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter ko = production.getKineticLaw().getLocalParameter(GlobalConstants.OCR_STRING);
			if (ko != null && !defaultValue.equals(ko.getValue()+"")) {
				field.setValue(ko.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.OCR_STRING, field);
		add(field);
		
		// kbasal
		origString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.KBASAL_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter kb = refProd.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING);
				if (kb != null) {
					defaultValue = kb.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.KBASAL_STRING, gcm.getParameter(GlobalConstants.KBASAL_STRING),
				origString, defaultValue, Utility.SWEEPstring, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter kb = production.getKineticLaw().getLocalParameter(GlobalConstants.KBASAL_STRING);
			if (kb != null && !defaultValue.equals(kb.getValue()+"")) {
				field.setValue(kb.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.KBASAL_STRING, field);
		add(field);
		
		// kactived production
		origString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.ACTIVATED_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter ka = refProd.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING);
				if (ka != null) {
					defaultValue = ka.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.ACTIVATED_STRING, gcm.getParameter(GlobalConstants.ACTIVATED_STRING),
				origString, defaultValue, formatString, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter ka = production.getKineticLaw().getLocalParameter(GlobalConstants.ACTIVATED_STRING);
			if (ka != null && !defaultValue.equals(ka.getValue()+"")) {
				field.setValue(ka.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.ACTIVATED_STRING, field);
		add(field);
		
		// stoichiometry
		origString = "default";
		defaultValue = gcm.getParameter(GlobalConstants.STOICHIOMETRY_STRING);
		formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (production != null) {
				Reaction refProd = refGCM.getSBMLDocument().getModel().getReaction(production.getId());
				LocalParameter np = refProd.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
				if (np != null) {
					defaultValue = np.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.STOICHIOMETRY_STRING, gcm.getParameter(GlobalConstants.STOICHIOMETRY_STRING),
				origString, defaultValue, formatString, paramsOnly, origString, false);
		if (production != null) {
			LocalParameter np = production.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
			if (np != null && !defaultValue.equals(np.getValue()+"")) {
				field.setValue(np.getValue()+"");
				field.setCustom();
			}	
		}
		fields.put(GlobalConstants.STOICHIOMETRY_STRING, field);
		add(field);		
		
		// Panel for associating SBOL promoter element
		if (!paramsOnly) {
			SbolField Prom_Field = new SbolField(GlobalConstants.SBOL_PROMOTER, gcmEditor);
			sbolFields.put(GlobalConstants.SBOL_PROMOTER, Prom_Field);
			add(Prom_Field);
		
			// Panel for associating SBOL terminator element
			SbolField Term_Field = new SbolField(GlobalConstants.SBOL_TERMINATOR, gcmEditor);
			sbolFields.put(GlobalConstants.SBOL_TERMINATOR, Term_Field);
			add(Term_Field);

			String annotation = promoter.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
			String [] annotations = annotation.split(",");
			for (int i=0;i<annotations.length;i++) {
				if (annotations[i].startsWith(GlobalConstants.SBOL_PROMOTER)) {
					String [] sbolRBS = annotations[i].split("=");
					Prom_Field.setText(sbolRBS[1]);
				}
				if (annotations[i].startsWith(GlobalConstants.SBOL_TERMINATOR)) {
					String [] sbolORF = annotations[i].split("=");
					Term_Field.setText(sbolORF[1]);
				}
			}
		}

		String oldName = null;
		oldName = selected;

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
			String id = selected;
			if (!paramsOnly) {
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
				id = fields.get(GlobalConstants.ID).getValue();
				promoter.setId(id);
				promoter.setName(fields.get(GlobalConstants.NAME).getValue());
			}
			promoter.setInitialAmount(Double.parseDouble(fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getValue()));
			String kaStr = null;
			production.getKineticLaw().removeLocalParameter(GlobalConstants.STOICHIOMETRY_STRING);
			production.getKineticLaw().removeLocalParameter(GlobalConstants.OCR_STRING);
			production.getKineticLaw().removeLocalParameter(GlobalConstants.KBASAL_STRING);
			production.getKineticLaw().removeLocalParameter(GlobalConstants.ACTIVATED_STRING);
			production.getKineticLaw().removeLocalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			production.getKineticLaw().removeLocalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			production.getKineticLaw().removeLocalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
			production.getKineticLaw().removeLocalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
			PropertyField f = fields.get(GlobalConstants.ACTIVATED_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				kaStr = f.getValue();
			}
			String npStr = null;
			f = fields.get(GlobalConstants.STOICHIOMETRY_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				npStr = f.getValue();
			}
			String koStr = null;
			f = fields.get(GlobalConstants.OCR_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				koStr = f.getValue();
			}
			String kbStr = null;
			f = fields.get(GlobalConstants.KBASAL_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				kbStr = f.getValue();
			}
			String KoStr = null;
			f = fields.get(GlobalConstants.RNAP_BINDING_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				KoStr = f.getValue();
			}
			String KaoStr = null;
			f = fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING);
			if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
				KaoStr = f.getValue();
			}
			gcm.createProductionReaction(selected,kaStr,npStr,koStr,kbStr,KoStr,KaoStr);

			if (!paramsOnly) {
				String annotation = "";
				// Add SBOL properties
				for (SbolField sf : sbolFields.values()) {
					if (!sf.getText().equals(""))
						annotation += "," + sf.getType() + "=" + sf.getText();
				}
				promoter.setAnnotation(GlobalConstants.TYPE + "=" + GlobalConstants.PROMOTER);
				if (!annotation.equals("")) {
					promoter.appendAnnotation(annotation);
				} 

				// rename all the influences that use this promoter if name was changed
				if (selected != null && !oldName.equals(id)) {
					while (gcm.getUsedIDs().contains(selected)) {
						gcm.getUsedIDs().remove(selected);
					}
					gcm.changePromoterName(oldName, id);
					this.secondToLastUsedPromoter = oldName;
					promoterNameChange = true;
				}
				if (!gcm.getUsedIDs().contains(id)) {
					gcm.getUsedIDs().add(id);
				}
				this.lastUsedPromoter = id;
			}
			gcmEditor.setDirty(true);
		} else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}
	
	public String updates() {
		String updates = "";
		if (paramsOnly) {
			if (fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getState().equals(fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getStates()[1])) {
				updates += selected + "/" + GlobalConstants.PROMOTER_COUNT_STRING + " "
						+ fields.get(GlobalConstants.PROMOTER_COUNT_STRING).getValue();
			}
			if (fields.get(GlobalConstants.RNAP_BINDING_STRING).getState()
					.equals(fields.get(GlobalConstants.RNAP_BINDING_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.RNAP_BINDING_STRING + " "
						+ fields.get(GlobalConstants.RNAP_BINDING_STRING).getValue();
			}
			if (fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getState()
					.equals(fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.ACTIVATED_RNAP_BINDING_STRING + " "
						+ fields.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING).getValue();
			}
			if (fields.get(GlobalConstants.OCR_STRING).getState().equals(fields.get(GlobalConstants.OCR_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.OCR_STRING + " "
						+ fields.get(GlobalConstants.OCR_STRING).getValue();
			}
			if (fields.get(GlobalConstants.STOICHIOMETRY_STRING).getState().equals(fields.get(GlobalConstants.STOICHIOMETRY_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.STOICHIOMETRY_STRING + " "
						+ fields.get(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KBASAL_STRING).getState().equals(fields.get(GlobalConstants.KBASAL_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.KBASAL_STRING + " "
						+ fields.get(GlobalConstants.KBASAL_STRING).getValue();
			}
			if (fields.get(GlobalConstants.ACTIVATED_STRING).getState().equals(fields.get(GlobalConstants.ACTIVATED_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += selected + "/" + GlobalConstants.ACTIVATED_STRING + " "
						+ fields.get(GlobalConstants.ACTIVATED_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += selected + "/";
			}
		}
		return updates;
	}
	
	// Provide a public way to query what the last used (or created) promoter was.
	private String lastUsedPromoter;
	public String getLastUsedPromoter(){return lastUsedPromoter;}
	
	private String secondToLastUsedPromoter;
	public String getSecondToLastUsedPromoter(){return secondToLastUsedPromoter;}
	
	private boolean promoterNameChange = false;
	public boolean wasPromoterNameChanged(){return promoterNameChange;}
	
}