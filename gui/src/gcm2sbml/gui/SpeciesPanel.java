package gcm2sbml.gui;

import gcm2sbml.gui.modelview.movie.MovieContainer;
import gcm2sbml.gui.modelview.movie.visualizations.ColorScheme;
import gcm2sbml.gui.modelview.movie.visualizations.ColorSchemeChooser;
import gcm2sbml.parser.CompatibilityFixer;
import gcm2sbml.parser.GCMFile;
import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import parser.TSDParser;

import biomodelsim.BioSim;

public class SpeciesPanel extends JPanel implements ActionListener {

	public SpeciesPanel(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly,
			GCMFile refGCM, GCM2SBMLEditor gcmEditor){

		super(new BorderLayout());
		constructor(selected, speciesList, influencesList, conditionsList, componentsList, gcm, paramsOnly, refGCM, gcmEditor, null);
	}

	public SpeciesPanel(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly,
			GCMFile refGCM, GCM2SBMLEditor gcmEditor, MovieContainer movieContainer){

		super(new BorderLayout());
		constructor(selected, speciesList, influencesList, conditionsList, componentsList, gcm, paramsOnly, refGCM, gcmEditor, movieContainer);
	}
	
	private void constructor(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly,
			GCMFile refGCM,  GCM2SBMLEditor gcmEditor, MovieContainer movieContainer) {

		JPanel grid = new JPanel(new GridLayout(6,1));
		this.add(grid, BorderLayout.CENTER);
		

		this.selected = selected;
		this.speciesList = speciesList;
		this.influences = influencesList;
		this.conditions = conditionsList;
		this.components = componentsList;
		this.gcm = gcm;
		this.paramsOnly = paramsOnly;
		this.movieContainer = movieContainer;
		this.gcmEditor = gcmEditor;

		fields = new HashMap<String, PropertyField>();

		// ID field
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null, Utility.IDstring,
				paramsOnly, "default");
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.ID, field);
		grid.add(field);

		// Name field
		field = new PropertyField(GlobalConstants.NAME, "", null, null, Utility.NAMEstring,
				paramsOnly, "default");
		if (paramsOnly) {
			field.setEnabled(false);
		}
		fields.put(GlobalConstants.NAME, field);
		grid.add(field);

		// Type field
		JPanel tempPanel = new JPanel();
		JLabel tempLabel = new JLabel(GlobalConstants.TYPE);
		typeBox = new JComboBox(types);
		typeBox.setSelectedItem(types[1]);
		typeBox.addActionListener(this);
		tempPanel.setLayout(new GridLayout(1, 2));
		tempPanel.add(tempLabel);
		tempPanel.add(typeBox);
		if (paramsOnly) {
			tempLabel.setEnabled(false);
			typeBox.setEnabled(false);
		}
		grid.add(tempPanel);

		// Initial field
		String origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.INITIAL_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.INITIAL_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.INITIAL_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.INITIAL_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.INITIAL_STRING);
			}
			field = new PropertyField(GlobalConstants.INITIAL_STRING, gcm
					.getParameter(GlobalConstants.INITIAL_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString);
		}
		else {
			field = new PropertyField(GlobalConstants.INITIAL_STRING, gcm
					.getParameter(GlobalConstants.INITIAL_STRING), origString, gcm
					.getParameter(GlobalConstants.INITIAL_STRING), Utility.NUMstring, paramsOnly,
					origString);
		}
		fields.put(GlobalConstants.INITIAL_STRING, field);
		grid.add(field);

		// Decay field
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KDECAY_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KDECAY_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KDECAY_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KDECAY_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KDECAY_STRING);
			}
			field = new PropertyField(GlobalConstants.KDECAY_STRING, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString);
		}
		else {
			field = new PropertyField(GlobalConstants.KDECAY_STRING, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), origString, gcm
					.getParameter(GlobalConstants.KDECAY_STRING), Utility.NUMstring, paramsOnly,
					origString);
		}
		fields.put(GlobalConstants.KDECAY_STRING, field);
		grid.add(field);

		// add the color chooser for the movie component
		if(paramsOnly){
			ColorScheme colorScheme = movieContainer.getMoviePreferences().getOrCreateColorSchemeForSpecies(selected);
			colorSchemeChooser = new ColorSchemeChooser(colorScheme);
			this.add(colorSchemeChooser, BorderLayout.SOUTH);
		}
		
		
		// Complex Equilibrium Constant Field
		origString = "default";
		if (paramsOnly) {
			String defaultValue = refGCM.getParameter(GlobalConstants.KCOMPLEX_STRING);
			if (refGCM.getSpecies().get(selected).containsKey(GlobalConstants.KCOMPLEX_STRING)) {
				defaultValue = refGCM.getSpecies().get(selected).getProperty(
						GlobalConstants.KCOMPLEX_STRING);
				origString = "custom";
			}
			else if (gcm.globalParameterIsSet(GlobalConstants.KCOMPLEX_STRING)) {
				defaultValue = gcm.getParameter(GlobalConstants.KCOMPLEX_STRING);
			}
			field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, gcm
					.getParameter(GlobalConstants.KCOMPLEX_STRING), origString, defaultValue,
					Utility.SWEEPstring, paramsOnly, origString);
		}
		else {
			field = new PropertyField(GlobalConstants.KCOMPLEX_STRING, gcm
					.getParameter(GlobalConstants.KCOMPLEX_STRING), origString, gcm
					.getParameter(GlobalConstants.KCOMPLEX_STRING), Utility.SLASHstring, paramsOnly,
					origString);
		}
		fields.put(GlobalConstants.KCOMPLEX_STRING, field);
		grid.add(field);

		String oldName = null;
		if (selected != null) {
			oldName = selected;
			Properties prop = gcm.getSpecies().get(selected);
			fields.get(GlobalConstants.ID).setValue(selected);
			typeBox.setSelectedItem(prop.getProperty(GlobalConstants.TYPE));
			setType(prop.getProperty(GlobalConstants.TYPE));
			loadProperties(prop);
		}
		else {
			setType(types[1]);
		}
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
	}

	private boolean checkValues() {
		for (PropertyField f : fields.values()) {
			if (!f.isValidValue() || f.getValue().equals("RNAP") || f.getValue().endsWith("_RNAP")
					|| f.getValue().endsWith("_bound")) {
				return false;
			}
		}
		return true;
	}

	private boolean openGui(String oldName) {
		
		// figure out which set of buttons to use
		String[] buttonOptions = options;
		String defaultOption = options[0];
		if(paramsOnly)
			buttonOptions = withColorOptions;
		
		int value = JOptionPane.showOptionDialog(BioSim.frame, this, "Species Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, buttonOptions, defaultOption);
		
		// the new id of the species. Will be filled in later.
		String newSpeciesID = null;
		
		if (buttonOptions[value].equals(options[0]) || buttonOptions[value].equals(withColorOptions[0])) { // "OK" or "Ok and copy..."
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			if (oldName == null) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getParameters().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getPromoters().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(fields.get(GlobalConstants.ID).getValue())) {
				if (gcm.getComponents().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getSpecies().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getParameters().containsKey(fields.get(GlobalConstants.ID).getValue())
						|| gcm.getPromoters().containsKey(fields.get(GlobalConstants.ID).getValue())) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			if (oldName != null
					&& !gcm.editSpeciesCheck(oldName, typeBox.getSelectedItem().toString())) {
				Utility.createErrorMessage("Error", "Cannot change species type.  "
						+ "Species is used as a port in a component.");
				return false;
			}
			newSpeciesID = fields.get(GlobalConstants.ID).getValue();

			Properties property = new Properties();

			// copy the old values into the new property. Some will then be
			// overwritten ,
			// but others (such as positioning info) will not and need to be
			// preserved.
			if (oldName != null) {
				for (Object s : gcm.getSpecies().get(oldName).keySet()) {
					String k = s.toString();
					String v = (gcm.getSpecies().get(oldName).getProperty(k)).toString();
					if (!k.equals("label")) {
						property.put(k, v);
					}
				}
			}

			for (PropertyField f : fields.values()) {
				if (f.getState() == null || f.getState().equals(f.getStates()[1])) {
					property.put(f.getKey(), f.getValue());
				}
				else {
					property.remove(f.getKey());
				}
			}
			property.put(GlobalConstants.TYPE, typeBox.getSelectedItem().toString());

			if (selected != null && !oldName.equals(newSpeciesID)) {
				gcm.changeSpeciesName(oldName, newSpeciesID);
				((DefaultListModel) influences.getModel()).clear();
				influences.addAllItem(gcm.getInfluences().keySet());
				((DefaultListModel) conditions.getModel()).clear();
				conditions.addAllItem(gcm.getConditions());
				((DefaultListModel) components.getModel()).clear();
				for (String c : gcm.getComponents().keySet()) {
					components.addItem(c + " "
							+ gcm.getComponents().get(c).getProperty("gcm").replace(".gcm", "")
							+ " " + gcm.getComponentPortMap(c));
				}
			}
			gcm.addSpecies(newSpeciesID, property);
			if (paramsOnly) {
				if (fields.get(GlobalConstants.INITIAL_STRING).getState().equals(
						fields.get(GlobalConstants.INITIAL_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
								fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])
						|| fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
								fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])) {
					newSpeciesID += " Modified";
				}
			}
			speciesList.removeItem(oldName);
			speciesList.removeItem(oldName + " Modified");
			speciesList.addItem(newSpeciesID);
			speciesList.setSelectedValue(newSpeciesID, true);
			// ColorSchemeChooser
			if(colorSchemeChooser != null){
				//movieProperties.setProperty(id + MovieContainer.COLOR_PREPEND, colorSchemeChooser.)
			}

			if(paramsOnly)
				colorSchemeChooser.saveChanges();
			
			gcmEditor.setDirty(true);
		}
		if(buttonOptions[value].equals(withColorOptions[0])) { // "Ok, and copy..."
			movieContainer.copyMoviePreferencesSpecies(newSpeciesID);
		}
		if(buttonOptions[value].equals(options[1])) { // "Cancel"
			// System.out.println();
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
						+ CompatibilityFixer.getSBMLName(GlobalConstants.INITIAL_STRING) + " "
						+ fields.get(GlobalConstants.INITIAL_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KDECAY_STRING).getState().equals(
					fields.get(GlobalConstants.KDECAY_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ CompatibilityFixer.getSBMLName(GlobalConstants.KDECAY_STRING) + " "
						+ fields.get(GlobalConstants.KDECAY_STRING).getValue();
			}
			if (fields.get(GlobalConstants.KCOMPLEX_STRING).getState().equals(
					fields.get(GlobalConstants.KCOMPLEX_STRING).getStates()[1])) {
				if (!updates.equals("")) {
					updates += "\n";
				}
				updates += fields.get(GlobalConstants.ID).getValue() + "/"
						+ CompatibilityFixer.getSBMLName(GlobalConstants.KCOMPLEX_STRING) + " "
						+ fields.get(GlobalConstants.KCOMPLEX_STRING).getValue();
			}
			if (updates.equals("")) {
				updates += fields.get(GlobalConstants.ID).getValue() + "/";
			}
		}
		return updates;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			setType(typeBox.getSelectedItem().toString());
		}
	}

	private void setType(String type) {
		if (type.equals(types[0])) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(false);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(false);
		}
		else if (type.equals(types[1])) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
		}
		else if (type.equals(types[2])) {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(true);
		} else {
			fields.get(GlobalConstants.KDECAY_STRING).setEnabled(true);
			fields.get(GlobalConstants.KCOMPLEX_STRING).setEnabled(false);
		}
	}

	private void loadProperties(Properties property) {
		for (Object o : property.keySet()) {
			if (fields.containsKey(o.toString())) {
				fields.get(o.toString()).setValue(property.getProperty(o.toString()));
				fields.get(o.toString()).setCustom();
			}
		}
	}
	
	private String selected = "";

	private PropertyList speciesList = null;

	private PropertyList influences = null;
	
	private PropertyList conditions = null;
	
	private PropertyList components = null;

	private String[] options = { "Ok", "Cancel" };
	// make sure the Ok and Cancel options in both of these match exactly
	private String[] withColorOptions = {"Ok, and Copy to Other Species", "Ok", "Cancel"};

	private GCMFile gcm = null;

	private JComboBox typeBox = null;

	private static final String[] types = new String[] { "input", "internal", "output", "constitutive"};

	private HashMap<String, PropertyField> fields = null;

	private boolean paramsOnly;
	
	private MovieContainer movieContainer;

	private ColorSchemeChooser colorSchemeChooser;
	
	private GCM2SBMLEditor gcmEditor;
}
