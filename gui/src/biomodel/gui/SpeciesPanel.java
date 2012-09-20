package biomodel.gui;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.Submodel;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.textualeditor.InitialAssignments;
import biomodel.gui.textualeditor.MySpecies;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.Utility;


import main.Gui;

public class SpeciesPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * calls constructor to construct the panel
	 * 
	 * @param selected
	 * @param speciesList
	 * @param influencesList
	 * @param conditionsList
	 * @param componentsList
	 * @param gcm
	 * @param paramsOnly
	 * @param refGCM
	 * @param modelEditor
	 */
	public SpeciesPanel(Gui biosim, String selected, PropertyList speciesList, 
			PropertyList componentsList, BioModel gcm, boolean paramsOnly,
			BioModel refGCM, ModelEditor modelEditor, boolean inTab){

		super(new BorderLayout());
		this.biosim = biosim;
		constructor(selected, speciesList, componentsList, gcm, paramsOnly, refGCM, modelEditor, inTab);
	}
	
	/**
	 * constructs the species panel
	 * 
	 * @param selected
	 * @param speciesList
	 * @param influencesList
	 * @param conditionsList
	 * @param componentsList
	 * @param bioModel
	 * @param paramsOnly
	 * @param refGCM
	 * @param gcmEditor
	 */
	private void constructor(String selected, PropertyList speciesList, 
			PropertyList componentsList, BioModel bioModel, boolean paramsOnly,
			BioModel refGCM,  ModelEditor modelEditor, boolean inTab) {

		JPanel grid;
		
		//if this is in analysis mode, only show the sweepable/changeable values
		if (paramsOnly)
			grid = new JPanel(new GridLayout(5,1));
		else {
			
			if (bioModel.getSBMLDocument().getLevel() > 2) {
				grid = new JPanel(new GridLayout(15,1));
			} 
			else {
				grid = new JPanel(new GridLayout(14,1));
			}
		}
		
		this.add(grid, BorderLayout.CENTER);		

		this.selected = selected;
		this.speciesList = speciesList;
		this.components = componentsList;
		this.bioModel = bioModel;
		this.refGCM = refGCM;
		this.paramsOnly = paramsOnly;

		this.modelEditor = modelEditor;


		fields = new HashMap<String, PropertyField>();

		Model model = bioModel.getSBMLDocument().getModel();
		species = model.getSpecies(selected);

		String origString = "default";
		PropertyField field = null;		
		
		// ID field
		field = new PropertyField(GlobalConstants.ID, species.getId(), null, null, Utility.IDstring, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		
		if (!paramsOnly) grid.add(field);
			
		// Name field
		field = new PropertyField(GlobalConstants.NAME, species.getName(), null, null, Utility.NAMEstring, paramsOnly, 
				"default", false);
		fields.put(GlobalConstants.NAME, field);
		
		if (!paramsOnly) grid.add(field);		

		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel(GlobalConstants.TYPE);
		typeBox = new JComboBox(types);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);

		if (!paramsOnly) grid.add(tempPanel);
		
		//diffusible/constitutive checkboxes		
		tempPanel = new JPanel(new GridLayout(1,2));
		tempPanel.add(new JLabel(""));

		diffusion = bioModel.getDiffusionReaction(selected);
		constitutive = bioModel.getConstitutiveReaction(selected);
		degradation = bioModel.getDegradationReaction(selected);
		complex = bioModel.getComplexReaction(selected);
		
		JPanel constDiff = new JPanel(new GridLayout(1,3));
		specDiffusible = new JCheckBox("diffusible");
		specDiffusible.setSelected(diffusion != null);
		specDiffusible.addActionListener(this);
		specDiffusible.setActionCommand("constdiffChanged");
		specConstitutive = new JCheckBox("constitutive");
		specConstitutive.setSelected(constitutive != null);
		specConstitutive.addActionListener(this);
		specConstitutive.setActionCommand("constdiffChanged");
		specDegradable = new JCheckBox("degrades");
		specDegradable.setSelected(degradation != null);
		specDegradable.addActionListener(this);
		specDegradable.setActionCommand("constdiffChanged");
		
		constDiff.add(specDiffusible);
		constDiff.add(specConstitutive);
		constDiff.add(specDegradable);

		tempPanel.add(constDiff);
		
		if (!paramsOnly) grid.add(tempPanel);
		
		// compartment field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Compartment");
		compartBox = MySpecies.createCompartmentChoices(bioModel);		
		compartBox.setSelectedItem(species.getCompartment());
		compartBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(compartBox);
			
		if (!paramsOnly) grid.add(tempPanel);
		
		String[] optionsTF = { "true", "false" };

		// Boundary condition field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Boundary Condition");
		specBoundary = new JComboBox(optionsTF);
		
		if (species.getBoundaryCondition()) {
			specBoundary.setSelectedItem("true");
		} 
		else {
			specBoundary.setSelectedItem("false");
		}
		
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(specBoundary);

		if (!paramsOnly) grid.add(tempPanel);

		// Constant field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Constant");
		specConstant = new JComboBox(optionsTF);
		
		if (species.getConstant()) {
			specConstant.setSelectedItem("true");
		} 
		else {
			specConstant.setSelectedItem("false");
		}
		
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(specConstant);

		if (!paramsOnly) grid.add(tempPanel);

		// Has only substance units field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Has Only Substance Units");
		specHasOnly = new JComboBox(optionsTF);
		
		if (species.getHasOnlySubstanceUnits()) {
			specHasOnly.setSelectedItem("true");
		} 
		else {
			specHasOnly.setSelectedItem("false");
		}
		
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(specHasOnly);

		if (!paramsOnly) grid.add(tempPanel);
		
		// Units field
		tempPanel = new JPanel();
		tempLabel = new JLabel("Units");
		unitsBox = MySpecies.createUnitsChoices(bioModel);
		
		if (species.isSetUnits()) {
			
			unitsBox.setSelectedItem(species.getUnits());
		}
		
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(unitsBox);

		if (!paramsOnly) grid.add(tempPanel);
		
		// Conversion factor field
		if (bioModel.getSBMLDocument().getLevel() > 2) {
			
			tempPanel = new JPanel();
			tempLabel = new JLabel("Conversion Factor");
			convBox = MySpecies.createConversionFactorChoices(bioModel);
			
			if (species.isSetConversionFactor()) {
				convBox.setSelectedItem(species.getConversionFactor());
			}
			
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(convBox);

			if (!paramsOnly) grid.add(tempPanel);
		}
		
		//mark as interesting field
		if (paramsOnly) {
			
			String thresholdText = "";
			boolean speciesMarked = false;
			
			ArrayList<String> interestingSpecies = modelEditor.getReb2Sac().getInterestingSpeciesAsArrayList();				
			
			//look for the selected species among the already-interesting
			//if it is interesting, populate the field with its data
			for (String speciesInfo : interestingSpecies) {
				if (speciesInfo.split(" ")[0].equals(selected)) {
					speciesMarked = true;
					thresholdText = speciesInfo.replace(selected, "").trim();
					break;
				}
			}			
			
			tempPanel = new JPanel(new GridLayout(1, 2));
			specInteresting = new JCheckBox("Mark as Interesting");
			specInteresting.addActionListener(this);
			specInteresting.setSelected(speciesMarked);
			tempPanel.add(specInteresting);
			thresholdTextField = new JTextField(thresholdText);
			
			if (!bioModel.isInput(selected) &&
					(modelEditor.getGCM().getBiochemicalSpecies() != null &&
					!modelEditor.getGCM().getBiochemicalSpecies().contains(selected))) {
				tempPanel.add(thresholdTextField);
				specInteresting.setText("Mark as Interesting (Enter comma-separated thresholds)");
			}
			
			grid.add(tempPanel);
		}		
			
		// Initial field
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.INITIAL_STRING);
			if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialAmount()) {
				defaultValue = "" + refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialAmount();
			} else if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialConcentration()) {
				defaultValue = "[" + refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialConcentration() + "]";
			}
			field = new PropertyField(GlobalConstants.INITIAL_STRING, 
					bioModel.getParameter(GlobalConstants.INITIAL_STRING), origString, defaultValue,
					Utility.SWEEPstring + "|" + Utility.CONCstring, paramsOnly, origString, false);
			fields.put(GlobalConstants.INITIAL_STRING, field);
			if (species.isSetAnnotation() && species.getAnnotationString().contains(GlobalConstants.INITIAL_STRING)) {
				String annotation = species.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
				String sweep = annotation.substring(annotation.indexOf(GlobalConstants.INITIAL_STRING)+3);
				field.setValue(sweep);
				field.setCustom();
			} else if (species.isSetInitialAmount() &&	!defaultValue.equals("" + species.getInitialAmount())) {
				field.setValue("" + species.getInitialAmount());
				field.setCustom();
			} else if (species.isSetInitialConcentration() && !defaultValue.equals("["+species.getInitialConcentration()+"]")) {
				field.setValue("[" + species.getInitialConcentration() + "]");
				field.setCustom();
			}
			grid.add(field);
		}
		else {
			tempPanel = new JPanel();
			tempLabel = new JLabel("Initial Amount/Concentration");
			initialField = new JTextField("");
			InitialAssignment init = bioModel.getSBMLDocument().getModel().getInitialAssignment(species.getId());
			if (init!=null) {
				initialField.setText(bioModel.removeBooleans(init.getMath()));
			} else if (species.isSetInitialAmount()) {
				initialField.setText("" + species.getInitialAmount());
			} else if (species.isSetInitialConcentration()) {
				initialField.setText("[" + species.getInitialConcentration() + "]");
			}
			tempPanel.setLayout(new GridLayout(1, 2));
			tempPanel.add(tempLabel);
			tempPanel.add(initialField);
			grid.add(tempPanel);
		}
		
		// Decay field
		origString = "default";
		String defaultValue = bioModel.getParameter(GlobalConstants.KDECAY_STRING);
		String formatString = Utility.NUMstring;
		if (paramsOnly) {
			if (degradation != null) {
				Reaction refDeg = refGCM.getSBMLDocument().getModel().getReaction(degradation.getId());
				LocalParameter kd = refDeg.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
				if (kd != null) {
					defaultValue = kd.getValue()+"";
					origString = "custom";
				}
			}
			formatString = Utility.SWEEPstring;
		}
		field = new PropertyField(GlobalConstants.KDECAY_STRING, 
				bioModel.getParameter(GlobalConstants.KDECAY_STRING), origString, defaultValue,
				formatString, paramsOnly, origString, false);
		if (degradation != null) {
			LocalParameter kd = degradation.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
			if (kd != null && kd.isSetAnnotation() && kd.getAnnotationString().contains(GlobalConstants.KDECAY_STRING)) {
				String sweep = kd.getAnnotationString().replace("<annotation>"+GlobalConstants.KDECAY_STRING+"=","")
						.replace("</annotation>","");
				field.setValue(sweep);
				field.setCustom();
			} else if (kd != null && !defaultValue.equals(""+kd.getValue())) {
				field.setValue(""+kd.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.KDECAY_STRING, field);
		grid.add(field);
		
		//Extracellular decay field
		/*
		origString = "default";
		if (paramsOnly) {
			
			String defaultValue = refGCM.getParameter(GlobalConstants.KECDECAY_STRING);
			
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KECDECAY_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KECDECAY_STRING);
				origString = "custom";
			}
			else { 
				defaultValue = gcm.getParameter(GlobalConstants.KECDECAY_STRING);
			}
			
			field = new PropertyField(GlobalConstants.KECDECAY_STRING, gcm
					.getParameter(GlobalConstants.KECDECAY_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString, false);
		}
		else {
			
			field = new PropertyField(GlobalConstants.KECDECAY_STRING, gcm
					.getParameter(GlobalConstants.KECDECAY_STRING), origString, gcm
					.getParameter(GlobalConstants.KECDECAY_STRING), Utility.NUMstring, paramsOnly,
					origString, false);
		}
		
		fields.put(GlobalConstants.KECDECAY_STRING, field);
		grid.add(field);
		*/
		
		// Complex Equilibrium Constant Field
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING) + "/" + 
				bioModel.getParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			if (complex != null) {
				Reaction refComp = refGCM.getSBMLDocument().getModel().getReaction(complex.getId());
				LocalParameter kc_f = refComp.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
				LocalParameter kc_r = refComp.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
				if (kc_f != null && kc_r != null) {
					defaultValue = kc_f.getValue()+"/"+kc_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		}
		field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, 
				bioModel.getParameter(GlobalConstants.KCOMPLEX_STRING), origString, defaultValue,
				formatString, paramsOnly, origString, false);
		if (complex != null) {
			LocalParameter kc_f = complex.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING);
			LocalParameter kc_r = complex.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING);
			if (kc_f != null && kc_f.isSetAnnotation() && 
					kc_f.getAnnotationString().contains(GlobalConstants.FORWARD_KCOMPLEX_STRING)) {
				String sweep = kc_f.getAnnotationString().replace("<annotation>"+GlobalConstants.FORWARD_KCOMPLEX_STRING+"=","")
						.replace("</annotation>","");
				field.setValue(sweep);
				field.setCustom();
			} else if (kc_f != null && kc_r != null && !defaultValue.equals(kc_f.getValue()+"/"+kc_r.getValue())) {
				field.setValue(kc_f.getValue()+"/"+kc_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.KCOMPLEX_STRING, field);
		grid.add(field);

		// Membrane Diffusible Field
		origString = "default";
		defaultValue = bioModel.getParameter(GlobalConstants.FORWARD_MEMDIFF_STRING) + "/" + 
				bioModel.getParameter(GlobalConstants.REVERSE_MEMDIFF_STRING); 
		formatString = Utility.SLASHstring;
		if (paramsOnly) {
			if (diffusion != null) {
				Reaction refDiff = refGCM.getSBMLDocument().getModel().getReaction(diffusion.getId());
				LocalParameter kmdiff_f = refDiff.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING);
				LocalParameter kmdiff_r = refDiff.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING);
				if (kmdiff_f != null && kmdiff_r != null) {
					defaultValue = kmdiff_f.getValue()+"/"+kmdiff_r.getValue();
					origString = "custom";
				}
			}
			formatString = Utility.SLASHSWEEPstring;
		}
		field = new PropertyField(GlobalConstants.MEMDIFF_STRING, bioModel
				.getParameter(GlobalConstants.MEMDIFF_STRING), origString, defaultValue,
				formatString, paramsOnly, origString, false);
		if (diffusion != null) {
			LocalParameter kmdiff_f = diffusion.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING);
			LocalParameter kmdiff_r = diffusion.getKineticLaw().getLocalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING);
			if (kmdiff_f != null && kmdiff_f.isSetAnnotation() && 
					kmdiff_f.getAnnotationString().contains(GlobalConstants.FORWARD_MEMDIFF_STRING)) {
				String sweep = kmdiff_f.getAnnotationString().replace("<annotation>"+GlobalConstants.FORWARD_MEMDIFF_STRING+"=","")
						.replace("</annotation>","");
				field.setValue(sweep);
				field.setCustom();
			} else if (kmdiff_f != null && kmdiff_r != null && !defaultValue.equals(kmdiff_f.getValue()+"/"+kmdiff_r.getValue())) {
				field.setValue(kmdiff_f.getValue()+"/"+kmdiff_r.getValue());
				field.setCustom();
			}
		}
		fields.put(GlobalConstants.MEMDIFF_STRING, field);
		grid.add(field);
		
		//extracellular diffusion field
		/*
		origString = "default";
		if (paramsOnly) {
			
			String defaultValue = refGCM.getParameter(GlobalConstants.KECDIFF_STRING);
			
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KECDIFF_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KECDIFF_STRING);
				origString = "custom";
			}
			else { 
				defaultValue = gcm.getParameter(GlobalConstants.KECDIFF_STRING);
			}
			
			field = new PropertyField(GlobalConstants.KECDIFF_STRING, gcm
					.getParameter(GlobalConstants.KECDIFF_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString, false);
		}
		else {
			
			field = new PropertyField(GlobalConstants.KECDIFF_STRING, gcm
					.getParameter(GlobalConstants.KECDIFF_STRING), origString, gcm
					.getParameter(GlobalConstants.KECDIFF_STRING), Utility.NUMstring, paramsOnly,
					origString, false);
		}
		
		fields.put(GlobalConstants.KECDIFF_STRING, field);
		grid.add(field);
		*/
		setFieldEnablings();
		
		// Parse out GCM and SBOL annotations and add to respective fields
		if (!paramsOnly) {
			// Field for annotating species with SBOL DNA components
			LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(species);
			sbolField = new SBOLField(sbolURIs, GlobalConstants.SBOL_DNA_COMPONENT, modelEditor, 3, false);

			grid.add(sbolField);

			if (bioModel.isInput(species.getId())) {
				typeBox.setSelectedItem(GlobalConstants.INPUT);
			} else if (bioModel.isOutput(species.getId())) {
				typeBox.setSelectedItem(GlobalConstants.OUTPUT);
			} else {
				typeBox.setSelectedItem(GlobalConstants.INTERNAL);
			}
			/*
			String annotation = species.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
			String [] annotations = annotation.split(",");
			for (int i=0;i<annotations.length;i++) 
				if (annotations[i].startsWith(GlobalConstants.TYPE)) {
					String [] type = annotations[i].split("=");
					typeBox.setSelectedItem(type[1]);
				}
				*/
		}
			
		/*
		if (selected != null) {
			
			Properties prop = gcm.getSpecies().get(selected);
			//This will generate the action command associated with changing the type combo box
			typeBox.setSelectedItem(((String)prop.getProperty(GlobalConstants.TYPE))
					.replace(GlobalConstants.SPASTIC, "").replace(GlobalConstants.DIFFUSIBLE, ""));
			//loadProperties(prop);
		}
		else {
			
			typeBox.setSelectedItem(types[1]);
		}
		*/
		
		boolean display = false;
		
		if (!inTab) {
			while (!display) {
				//show the panel; handle the data
				int value = JOptionPane.showOptionDialog(Gui.frame, this, "Species Editor",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				display = handlePanelData(value);
			}
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
	
//	private boolean checkSbolValues() {
//		for (SBOLField sf : sbolFields.values()) {
//			if (!sf.isValidText())
//				return false;
//		}
//		return true;
//	}

	/**
	 * adds interesting species to a list in Reb2Sac.java
	 * 
	 * @return whether the values were okay for adding or not
	 */
	private boolean addInterestingSpecies() {
		
		if (specInteresting.isSelected()) {
			
			String thresholdText = thresholdTextField.getText();
			ArrayList<Integer> thresholdValues = new ArrayList<Integer>();

			//if the threshold text is empty, don't do anything to it
			if (!thresholdText.isEmpty()) {
				
				try {
					// check the threshold values for validity
					for (String threshold : thresholdText.trim().split(",")) {
						thresholdValues.add(Integer.parseInt(threshold.trim()));
					}
				}
				catch (NumberFormatException e) {
	
					Utility.createErrorMessage("Error", "Threshold values must be comma-separated integers");
					return false;
				}
	
				Integer[] threshVals = thresholdValues.toArray(new Integer[0]);
				Arrays.sort(threshVals);
				thresholdText = "";
	
				for (Integer thresholdVal : threshVals)
					thresholdText += thresholdVal.toString() + ",";
	
				// take off the last ", "
				if (threshVals.length > 0)
					thresholdText = thresholdText.substring(0, thresholdText.length() - 1);
			}

			// everything is okay, so add the interesting species to the list
			modelEditor.getReb2Sac().addInterestingSpecies(selected + " " + thresholdText);
		}
		else {
			modelEditor.getReb2Sac().removeInterestingSpecies(selected);
		}
		return true;
	}
	
	/**
	 * handles the panel data
	 * 
	 * @return
	 */
	public boolean handlePanelData(int value) {	
		
		// the new id of the species. Will be filled in later.
		String newSpeciesID = null;
		
		// if the value is -1 (user hit escape) then set it equal to the cancel value
		if(value == -1) {
			
			for(int i=0; i<options.length; i++) {
				
				if(options[i] == options[1])
					value = i;
			}
		}
		
		// "OK"
		if (options[value].equals(options[0])) {
			
			boolean valueCheck = checkValues();
			
			if (!valueCheck) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			
			if (selected == null) {
				if (bioModel.isSIdInUse((String)fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "ID already exists.");
					return false;
				}
			}
			else if (!selected.equals(fields.get(GlobalConstants.ID).getValue())) {
				
				if (bioModel.isSIdInUse((String)fields.get(GlobalConstants.ID).getValue())) {
					
					Utility.createErrorMessage("Error", "ID already exists.");
					return false;
				}
			}
			
			// TOOD: FIX ME
			/*
			if (selected != null && (typeBox.getSelectedItem().toString().equals(types[0]) || 
					specConstitutive.isSelected())) {
				
				for (String infl : gcm.getInfluences().keySet()) {
					
					String geneProduct = GCMFile.getOutput(infl);
					
					if (selected.equals(geneProduct)) {
						
						Utility.createErrorMessage("Error", "There must be no connections to an input species " +
						"or constitutive species.");
						return false;
					}
				}
			}
			*/
			
			newSpeciesID = fields.get(GlobalConstants.ID).getValue();

			//Properties property = new Properties();
//			boolean removeModelSBOLAnnotationFlag = false;
			if (selected != null) {			
				
				//check and add interesting species information
				if (paramsOnly) {
					if (!addInterestingSpecies())
					return false;
				}
				
				// preserve positioning info
				/*
				for (Object s : gcm.getSpecies().get(selected).keySet()) {
					
					String k = s.toString();
					
					if (k.equals("graphwidth") || k.equals("graphheight") || k.equals("graphy") || k.equals("graphx")) {
						
						String v = (gcm.getSpecies().get(selected).getProperty(k)).toString();
						property.put(k, v);
					}
				}
				*/
				
				if (!paramsOnly) {
					
					InitialAssignments.removeInitialAssignment(bioModel, selected);
					if (Utility.isValid(initialField.getText(), Utility.NUMstring)) {
						species.setInitialAmount(Double.parseDouble(initialField.getText()));
					} 
					else if (Utility.isValid(initialField.getText(), Utility.CONCstring)) {
						//String conc = fields.get(GlobalConstants.INITIAL_STRING).getValue();
						species.setInitialConcentration(Double.parseDouble(initialField.getText().substring(1,initialField.getText().length()-1)));
					} else {
						boolean error = InitialAssignments.addInitialAssignment(biosim, bioModel, species.getId(), 
								initialField.getText().trim());
						if (error) return false;
						species.setInitialAmount(Double.parseDouble("0.0"));
					}
					
					// Checks whether SBOL annotation on model needs to be deleted later when annotating species with SBOL
//					if (sbolField.getSBOLURIs().size() > 0 && 
//							bioModel.getElementSBOLCount() == 0 && bioModel.getModelSBOLAnnotationFlag()) {
//						Object[] options = { "OK", "Cancel" };
//						int choice = JOptionPane.showOptionDialog(null, 
//								"SBOL associated to model elements can't coexist with SBOL associated to model itself unless" +
//								" the latter was previously generated from the former.  Remove SBOL associated to model?", 
//								"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
//						if (choice == JOptionPane.OK_OPTION)
//							removeModelSBOLAnnotationFlag = true;
//						else {
//							if (species.isSetInitialAmount())
//								species.unsetInitialAmount();
//							else if (species.isSetInitialConcentration())
//								species.unsetInitialConcentration();
//							return false;
//						}
//					}
					
					species.setName(fields.get(GlobalConstants.NAME).getValue());
					
					if (specBoundary.getSelectedItem().equals("true")) {
						species.setBoundaryCondition(true);
					}
					else {
						species.setBoundaryCondition(false);
					}
					
					if (specConstant.getSelectedItem().equals("true")) {
						species.setConstant(true);
					}
					else {
						species.setConstant(false);
					}
					
					species.setCompartment((String)compartBox.getSelectedItem());

					if (specHasOnly.getSelectedItem().equals("true")) {
						species.setHasOnlySubstanceUnits(true);
					}
					else {
						species.setHasOnlySubstanceUnits(false);
					}
					
					String unit = (String) unitsBox.getSelectedItem();
					
					if (unit.equals("( none )")) {
						species.unsetUnits();
					}
					else {
						species.setUnits(unit);
					}
					
					String convFactor = null;
					
					if (bioModel.getSBMLDocument().getLevel() > 2) {
						convFactor = (String) convBox.getSelectedItem();
						
						if (convFactor.equals("( none )")) {
							species.unsetConversionFactor();
						}
						else {
							species.setConversionFactor(convFactor);
						}
					}
				} else {
					PropertyField f = fields.get(GlobalConstants.INITIAL_STRING);
					if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
						if (Utility.isValid(f.getValue(), Utility.NUMstring)) {
							species.setInitialAmount(Double.parseDouble(f.getValue()));
						} 
						else if (Utility.isValid(f.getValue(), Utility.CONCstring)) {
							species.setInitialConcentration(Double.parseDouble(f.getValue().substring(1,f.getValue().length()-1)));
						} else {
							species.setAnnotation(GlobalConstants.INITIAL_STRING+"="+f.getValue());
						}
					} else {
						if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialAmount()) {
							species.setInitialAmount(refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialAmount());
						} else if (refGCM.getSBMLDocument().getModel().getSpecies(selected).isSetInitialConcentration()) {
							species.setInitialConcentration(refGCM.getSBMLDocument().getModel().getSpecies(selected).getInitialConcentration());
						}

					}
				}
			}
			String speciesType = typeBox.getSelectedItem().toString();
			bioModel.createDirPort(species.getId(),speciesType);
			boolean onPort = (speciesType.equals(GlobalConstants.INPUT)||speciesType.equals(GlobalConstants.OUTPUT));
			
			if (degradation != null && !specDegradable.isSelected()) {
				bioModel.removeReaction(degradation.getId());
			} else if (specDegradable.isSelected()) {
				PropertyField f = fields.get(GlobalConstants.KDECAY_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					if (f.getValue().startsWith("(")) {
						bioModel.createDegradationReaction(selected, 1.0, f.getValue(),onPort);		
					} else {
						bioModel.createDegradationReaction(selected, Double.parseDouble(f.getValue()), null,onPort);		
					}
				} else {
					bioModel.createDegradationReaction(selected, -1, null,onPort);		
				}
			} 
			if (diffusion != null && !specDiffusible.isSelected()) {
				bioModel.removeReaction(diffusion.getId());
			} else if (specDiffusible.isSelected()) {
				String kmdiffStr = null;
				PropertyField f = fields.get(GlobalConstants.MEMDIFF_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					kmdiffStr = f.getValue();
				}
				bioModel.createDiffusionReaction(selected, kmdiffStr,onPort);		
			} 
			if (constitutive != null && !specConstitutive.isSelected()) {
				bioModel.removeReaction(constitutive.getId());
			} else if (specConstitutive.isSelected()) {
				bioModel.createConstitutiveReaction(selected,onPort);		
			} 
			if (complex != null) {
				String KcStr = null;
				PropertyField f = fields.get(GlobalConstants.KCOMPLEX_STRING);
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					KcStr = f.getValue();
				}
				bioModel.createComplexReaction(selected, KcStr,onPort);
			}
			
			if (!paramsOnly) {
				// Add SBOL annotation to species
				LinkedList<URI> sbolURIs = sbolField.getSBOLURIs();
				if (sbolURIs.size() > 0) {
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(species.getMetaId(), sbolURIs);
					AnnotationUtility.setSBOLAnnotation(species, sbolAnnot);
					if (sbolField.wasInitiallyBlank())
						bioModel.setElementSBOLCount(bioModel.getElementSBOLCount() + 1);
//					if (removeModelSBOLAnnotationFlag) {
//						AnnotationUtility.removeSBOLAnnotation(bioModel.getSBMLDocument().getModel());
//						bioModel.setModelSBOLAnnotationFlag(false);
//						modelEditor.getSchematic().getSBOLDescriptorsButton().setEnabled(true);
//					}
				} else {
					AnnotationUtility.removeSBOLAnnotation(species);
					if (!sbolField.wasInitiallyBlank())
						bioModel.setElementSBOLCount(bioModel.getElementSBOLCount() - 1);
				}
			}
			
			if (selected != null && !selected.equals(newSpeciesID)) {
				bioModel.changeSpeciesId(selected, newSpeciesID);
				((DefaultListModel) components.getModel()).clear();

				for (long i = 0; i < bioModel.getSBMLCompModel().getNumSubmodels(); i++) {
					Submodel submodel = bioModel.getSBMLCompModel().getSubmodel(i);
					components.addItem(submodel.getId() + " " + submodel.getModelRef() + " " + bioModel.getComponentPortMap(submodel.getId()));
				}
			}

			//gcm.addSpecies(newSpeciesID, property);
			
			if (paramsOnly) {
				if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(
						fields.get(GlobalConstants.INITIAL_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
								fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
								fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])
						|| fields.get(GlobalConstants.MEMDIFF_STRING).getState().equals(
								fields.get(GlobalConstants.MEMDIFF_STRING).getStates()[1])) {
					newSpeciesID += " Modified";
				}
			}
			
			speciesList.removeItem(selected);
			speciesList.removeItem(selected + " Modified");
			speciesList.addItem(newSpeciesID);
			speciesList.setSelectedValue(newSpeciesID, true);
	
			modelEditor.refresh();
			modelEditor.setDirty(true);
		}

		// "Cancel"
		if(options[value].equals(options[1])) {
			return true;
		}
		return true;
	}

	public String updates() {
		
		String updates = "";
		
		if (paramsOnly) {

			if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(
					fields.get(GlobalConstants.INITIAL_STRING).getStates()[1])) {
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.INITIAL_STRING + " "
						+ fields.get(GlobalConstants.INITIAL_STRING).getValue();
			}

			if (fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
					fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])) {
				
				if (!updates.equals("")) {
					updates += "\n";
				}
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.KDECAY_STRING + " "
						+ fields.get(GlobalConstants.KDECAY_STRING).getValue();
			}
			
			if (fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
					fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])) {
				
				if (!updates.equals("")) {
					updates += "\n";
				}
				
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.KCOMPLEX_STRING + " "
						+ fields.get(GlobalConstants.KCOMPLEX_STRING).getValue();
			}
			
			if (fields.get(GlobalConstants.MEMDIFF_STRING).getState().equals(
					fields.get(GlobalConstants.MEMDIFF_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ GlobalConstants.MEMDIFF_STRING + " "
						+ fields.get(GlobalConstants.MEMDIFF_STRING).getValue();
			}
			
			if (updates.equals("")) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/";
			}
		}
		return updates;
	}

	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("comboBoxChanged") || 
				e.getActionCommand().equals("constdiffChanged")) {
			
			//disallow constant == true if diffusible or constitutive are selected
			if (specConstitutive.isSelected() || specDiffusible.isSelected()) {
				specConstant.setEnabled(false);
				specConstant.setSelectedItem("false");
			}
			else {
				specConstant.setEnabled(true);
			}
			setFieldEnablings();
		}

		if (paramsOnly)
			thresholdTextField.setEnabled(specInteresting.isSelected());
	}

	/**
	 * enables/disables field based on the species type
	 * @param type
	 */
	private void setFieldEnablings() {
		fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false);
		fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(false);
		fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(false);
		
		//diffusible
		if (specDiffusible.isSelected()) {
			fields.get(GlobalConstants.MEMDIFF_STRING).setEnabled(true);
		} 
		if (specDegradable.isSelected()) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
		}
		if (complex != null) {
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
		}
	}
	
	private String selected = "";

	private PropertyList speciesList = null;
	
	private PropertyList components = null;

	private String[] options = { "Ok", "Cancel" };

	private BioModel bioModel = null;
	private BioModel refGCM = null;

	private JComboBox typeBox = null;
	private JComboBox compartBox = null;
	private JComboBox convBox = null;
	private JComboBox unitsBox = null;
	private JComboBox specBoundary = null;
	private JComboBox specConstant = null;
	private JComboBox specHasOnly = null;
	private JTextField initialField = null;
	
	private JCheckBox specInteresting = null;
	private JCheckBox specDiffusible = null;
	private JCheckBox specConstitutive = null;
	private JCheckBox specDegradable = null;
	
	private Species species = null;
	private Reaction diffusion = null;
	private Reaction constitutive = null;
	private Reaction degradation = null;
	private Reaction complex = null;
	
	private JTextField thresholdTextField = null;
	
	private static final String[] types = new String[] { GlobalConstants.INPUT, GlobalConstants.INTERNAL, 
		GlobalConstants.OUTPUT};

	private HashMap<String, PropertyField> fields = null;
	
	private SBOLField sbolField;
	
	private boolean paramsOnly;
	
	private ModelEditor modelEditor;
	
	private Gui biosim;
}
