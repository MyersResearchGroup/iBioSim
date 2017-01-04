package biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.Utility;

import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.UnitDefinition;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.sbol.SBOLField2;
//import biomodel.gui.sbol.SBOLField;
import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;


/**
 * This is a class for creating SBML parameters
 * 
 * @author Chris Myers
 * 
 */
public class Parameters extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JList parameters; // JList of parameters

	private JButton addParam, addBool, addPlace, removeParam, editParam; // parameters buttons

	private JTextField paramID, paramName, paramValue, rateValue;

	private JComboBox paramUnits;

	private JComboBox paramConst;

	private JComboBox placeMarking;
	
	private JComboBox portDir;
	
	private SBOLField2 sbolField;
	
	private BioModel bioModel;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;
	
	private ModelEditor modelEditor;
	
	private boolean constantsOnly;
	
	private JComboBox SBOTerms;
	
	public Parameters(BioModel bioModel, ModelEditor modelEditor, Boolean paramsOnly, ArrayList<String> getParams, String file,
			ArrayList<String> parameterChanges, boolean constantsOnly) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		this.modelEditor = modelEditor;
		this.constantsOnly = constantsOnly;
		Model model = bioModel.getSBMLDocument().getModel();
		JPanel addParams = new JPanel();
		if (constantsOnly) {
			addParam = new JButton("Add Real Constant");
			addBool = new JButton("Add Boolean Constant");
			removeParam = new JButton("Remove Constant");
			editParam = new JButton("Edit Constant");
		} else {
			addParam = new JButton("Add Real");
			addBool = new JButton("Add Boolean");
			addPlace = new JButton("Add Place");
			removeParam = new JButton("Remove Parameter");
			editParam = new JButton("Edit Parameter");
		}
		addParams.add(addParam);
		addParams.add(addBool);
		if (!constantsOnly) {
			addParams.add(addPlace);
			addPlace.addActionListener(this);
		}
		addParams.add(removeParam);
		addParams.add(editParam);
		addBool.addActionListener(this);
		addParam.addActionListener(this);
		removeParam.addActionListener(this);
		editParam.addActionListener(this);
		if (paramsOnly) {
			addBool.setEnabled(false);
			addParam.setEnabled(false);
			removeParam.setEnabled(false);
		}
		JLabel parametersLabel;
		parametersLabel = new JLabel("List of Global Parameters:");
		parameters = new JList();
		parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll3 = new JScrollPane();
		scroll3.setViewportView(parameters);
		ListOf<Parameter> listOfParameters = model.getListOfParameters();
		String[] params = new String[model.getParameterCount()];
		int notIncludedParametersCount = 0;
		
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter parameter = listOfParameters.get(i);
			
			params[i] = parameter.getId(); 
			params[i] += SBMLutilities.getDimensionString(parameter);

			if (parameter.getId().contains("_locations"))
				++notIncludedParametersCount;
			else if (constantsOnly && !parameter.getConstant()) {
				++notIncludedParametersCount;
				params[i] = params[i] + "__DELETE";
			}
			
			if (!params[i].endsWith("__DELETE")) {
				if (paramsOnly) {
					params[i] += " " + parameter.getValue();
					for (int j = 0; j < getParams.size(); j++) {
						if (getParams.get(j).split(" ")[0].equals(parameter.getId())) {
							parameterChanges.add(getParams.get(j));
							String[] splits = getParams.get(j).split(" ");
							if (splits[splits.length - 2].equals("Modified") || splits[splits.length - 2].equals("Custom")) {
								String value = splits[splits.length - 1];
								parameter.setValue(Double.parseDouble(value));
								params[i] += " Modified " + splits[splits.length - 1];
							}
							else if (splits[splits.length - 2].equals("Sweep")) {
								String value = splits[splits.length - 1];
								parameter.setValue(Double.parseDouble(value.split(",")[0].substring(1).trim()));
								params[i] += " " + splits[splits.length - 2] + " " + splits[splits.length - 1];
							}
						}
					}
				} else {
					if (SBMLutilities.isPlace(parameter)) {
						params[i] += " - place";
					} else if (SBMLutilities.isBoolean(parameter)) {
						params[i] += " - boolean";
					} else {
						params[i] += " - real";
					}
				}
			}
		}
		
		//take out the location parameters from the parameter list
		String[] paramsCopy = params;
		
		if (notIncludedParametersCount > 0)
			params = new String[paramsCopy.length - notIncludedParametersCount];
		
		int j=0;
		for (int i = 0; i < paramsCopy.length; ++i) {
			
			if (paramsCopy[i].contains("_locations")||paramsCopy[i].endsWith("__DELETE"))
				continue;
			params[j] = paramsCopy[i];
			j++;
		}
		
		Utility.sort(params);
		parameters.setListData(params);
		parameters.setSelectedIndex(0);
		parameters.addMouseListener(this);
		this.add(parametersLabel, "North");
		this.add(scroll3, "Center");
		this.add(addParams, "South");
	}
	
	/**
	 * Refresh parameter panel
	 */
	public void refreshParameterPanel(BioModel gcm) {
		String selectedParameter = "";
		if (!parameters.isSelectionEmpty()) {
			selectedParameter = ((String) parameters.getSelectedValue()).split(" ")[0];
		}
		this.bioModel = gcm;
		Model model = gcm.getSBMLDocument().getModel();
		ListOf<Parameter> listOfParameters = model.getListOfParameters();
		
		int skip = 0;
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter parameter = listOfParameters.get(i);
			if (constantsOnly && !parameter.getConstant()) skip++;
		}
		int k = 0;
		String[] params = new String[model.getParameterCount()-skip];
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter parameter = listOfParameters.get(i);
			if (constantsOnly && !parameter.getConstant()) continue;
			params[k] = parameter.getId() + SBMLutilities.getDimensionString(parameter);
			if (paramsOnly) {
				params[k] += " " + parameter.getValue();
				for (int j = 0; j < parameterChanges.size(); j++) {
					String[] splits = parameterChanges.get(j).split(" ");
					if (splits[0].equals(params[k].split(" ")[0])) {
						parameterChanges.set(j,	params[k] + " " + splits[splits.length-2] + " " + 
								splits[splits.length-1]);
						params[k] = parameterChanges.get(j);
					}
				}
			} else if (!params[k].endsWith("__DELETE")) {
				if (SBMLutilities.isPlace(parameter)) {
					params[k] += " - place";
				} else if (SBMLutilities.isBoolean(parameter)) {
					params[k] += " - boolean";
				} else {
					params[k] += " - real";
				}
			}
			k++;
		}
		Utility.sort(params);
		int selected = 0;
		for (int i = 0; i < params.length; i++) {
			if (params[i].split(" ")[0].equals(selectedParameter)) {
				selected = i;
			}
		}
		parameters.setListData(params);
		parameters.setSelectedIndex(selected);
	}

	/**
	 * Creates a frame used to edit parameters or create new ones.
	 */
	public String parametersEditor(String option,String selected,boolean isBoolean,boolean isPlace) {
		JPanel parametersPanel;
		if (paramsOnly) {
			if (isPlace) {
				parametersPanel = new JPanel(new GridLayout(8, 2));
			} else if (isBoolean){
				parametersPanel = new JPanel(new GridLayout(9, 2));
			} else {
				parametersPanel = new JPanel(new GridLayout(10, 2));
			}
		}
		else {
			if (isPlace) {
				parametersPanel = new JPanel(new GridLayout(5, 2));
			} else if (isBoolean){
				parametersPanel = new JPanel(new GridLayout(6, 2));
			} else {
				parametersPanel = new JPanel(new GridLayout(8, 2));
			}
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel onPortLabel = new JLabel("Port Type:");
		JLabel valueLabel = new JLabel("Initial Value:");
		JLabel rateLabel = new JLabel("Initial Rate:");
		JLabel unitLabel = new JLabel("Units:");
		JLabel constLabel = new JLabel("Constant:");
		JLabel sboTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_PARAMETER));
		if (isPlace) {
			SBOTerms.setSelectedItem("petri net place");
		} else if (isBoolean) {
			SBOTerms.setSelectedItem("logical parameter");
		}
		paramID = new JTextField();
		paramName = new JTextField();
		paramValue = new JTextField();
		paramValue.setText("0.0");
		rateValue = new JTextField();
		placeMarking = new JComboBox();
		placeMarking.addItem("false");
		placeMarking.addItem("true");
		paramUnits = new JComboBox();
		paramUnits.addItem("( none )");
		portDir = new JComboBox();
		portDir.addItem(GlobalConstants.INPUT);
		portDir.addItem(GlobalConstants.INTERNAL);
		portDir.addItem(GlobalConstants.OUTPUT);
		portDir.setSelectedItem(GlobalConstants.INTERNAL);
		Model model = bioModel.getSBMLDocument().getModel();
		ListOf<UnitDefinition> listOfUnits = model.getListOfUnitDefinitions();
		String[] units = new String[model.getUnitDefinitionCount()];
		for (int i = 0; i < model.getUnitDefinitionCount(); i++) {
			UnitDefinition unit = listOfUnits.get(i);
			units[i] = unit.getId();
		}
		for (int i = 0; i < units.length; i++) {
			if (bioModel.getSBMLDocument().getLevel() > 2
					|| (!units[i].equals("substance") && !units[i].equals("volume") && !units[i].equals("area") && !units[i].equals("length") && !units[i]
							.equals("time"))) {
				paramUnits.addItem(units[i]);
			}
		}
		String[] unitIdsL2V4 = { "substance", "volume", "area", "length", "time", "ampere", "becquerel", "candela", "celsius", "coulomb",
				"dimensionless", "farad", "gram", "gray", "henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux",
				"metre", "mole", "newton", "ohm", "pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
		String[] unitIdsL3V1 = { "ampere", "avogadro", "becquerel", "candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray",
				"henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm",
				"pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
		String[] unitIds;
		if (bioModel.getSBMLDocument().getLevel() < 3) {
			unitIds = unitIdsL2V4;
		}
		else {
			unitIds = unitIdsL3V1;
		}
		for (int i = 0; i < unitIds.length; i++) {
			paramUnits.addItem(unitIds[i]);
		}
		paramConst = new JComboBox();
		paramConst.addItem("true");
		paramConst.addItem("false");
		String[] list = { "Original", "Modified" };
		String[] list1 = { "1", "2" };
		final JComboBox type = new JComboBox(list);
		final JTextField start = new JTextField();
		final JTextField stop = new JTextField();
		final JTextField step = new JTextField();
		final JComboBox level = new JComboBox(list1);
		final JButton sweep = new JButton("Sweep");
		sweep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Ok", "Close" };
				JPanel p = new JPanel(new GridLayout(6, 2));
				JLabel startLabel = new JLabel("Start:");
				JLabel stopLabel = new JLabel("Stop:");
				JLabel stepLabel = new JLabel("Step:");
				JLabel levelLabel = new JLabel("Level:");
				p.add(startLabel);
				p.add(start);
				p.add(stopLabel);
				p.add(stop);
				p.add(stepLabel);
				p.add(step);
				p.add(levelLabel);
				p.add(level);
				int i = JOptionPane.showOptionDialog(Gui.frame, p, "Sweep", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
				if (i == JOptionPane.YES_OPTION) {
					double startVal = 0.0;
					double stopVal = 0.0;
					double stepVal = 0.0;
					try {
						startVal = Double.parseDouble(start.getText().trim());
						stopVal = Double.parseDouble(stop.getText().trim());
						stepVal = Double.parseDouble(step.getText().trim());
					}
					catch (Exception e1) {
					}
					paramValue.setText("(" + startVal + "," + stopVal + "," + stepVal + "," + level.getSelectedItem() + ")");
				}
			}
		});
		if (paramsOnly) {
			paramID.setEditable(false);
			paramName.setEditable(false);
			paramValue.setEnabled(false);
			rateValue.setEnabled(false);
			placeMarking.setEnabled(false);
			paramUnits.setEnabled(false);
			paramConst.setEnabled(false);
			portDir.setEnabled(false);
			SBOTerms.setEnabled(false);
			sweep.setEnabled(false);
		}
		String selectedID = "";
		Parameter rateParam = null;
		if (option.equals("OK")) {
			try {
				Parameter paramet = bioModel.getSBMLDocument().getModel().getParameter(selected);
				if (SBMLutilities.isPlace(paramet) || SBMLutilities.isBoolean(paramet)) {
					valueLabel.setText("Initial marking");
					if (SBMLutilities.isBoolean(paramet)) {
						valueLabel.setText("Initial value:");
						isBoolean = true;
					} else {
						isPlace = true;
					}
					if (paramsOnly) {
						if (isPlace) {
							parametersPanel.setLayout(new GridLayout(8, 2));
						} else {
							parametersPanel.setLayout(new GridLayout(9, 2));
						}
					} else {
						if (isPlace) {
							parametersPanel.setLayout(new GridLayout(5, 2));
						} else {
							parametersPanel.setLayout(new GridLayout(6, 2));
						}
					}
					if (paramet.getValue()==0) {
						placeMarking.setSelectedIndex(0);
					} else {
						placeMarking.setSelectedIndex(1);
					}
				} else {
					rateParam = bioModel.getSBMLDocument().getModel().getParameter(selected + "_" + GlobalConstants.RATE);
					if (rateParam!=null) {
						if (paramsOnly) {
							parametersPanel = new JPanel(new GridLayout(10, 2));
						} else {
							parametersPanel = new JPanel(new GridLayout(9, 2));
						}
						if (paramsOnly) {
							if (rateParam.isSetValue()) {
								rateValue.setText("" + rateParam.getValue());
							}
						} else {
							InitialAssignment init = bioModel.getSBMLDocument().getModel()
									.getInitialAssignment(paramet.getId()+"_"+GlobalConstants.RATE);
							if (init!=null) {
								rateValue.setText(bioModel.removeBooleans(init.getMath()));
							} else if (rateParam.isSetValue()) {
								rateValue.setText("" + rateParam.getValue());
							}
						}			
					}
				}
				String dimInID = SBMLutilities.getDimensionString(paramet);
				paramID.setText(paramet.getId() + dimInID);
				selectedID = paramet.getId();
				paramName.setText(paramet.getName());
				if (paramet.getConstant()) {
					paramConst.setSelectedItem("true");
				}
				else {
					paramConst.setSelectedItem("false");
				}
				if (!paramsOnly && !isPlace && !isBoolean) {
					List<URI> sbolURIs = new LinkedList<URI>();
					//Parse out SBOL annotations and add to SBOL field
					String sbolStrand = AnnotationUtility.parseSBOLAnnotation(paramet, sbolURIs);
					// Field for annotating rules with SBOL DNA components
					sbolField = new SBOLField2(sbolURIs, sbolStrand, GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 
							1, false);
				}
				if (paramsOnly) {
					if (paramet.isSetValue()) {
						paramValue.setText("" + paramet.getValue());
					}
				} else {
					InitialAssignment init = bioModel.getSBMLDocument().getModel().getInitialAssignment(selectedID);
					if (init!=null) {
						paramValue.setText(bioModel.removeBooleans(init.getMath()));
					} else if (paramet.isSetValue()) {
						paramValue.setText("" + paramet.getValue());
					}
				}
				if (paramet.isSetUnits()) {
					paramUnits.setSelectedItem(paramet.getUnits());
				}
				if (paramet.isSetSBOTerm()) {
					SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(paramet.getSBOTermID()));
				}
				if (bioModel.getPortByIdRef(paramet.getId())!=null) {
					Port port = bioModel.getPortByIdRef(paramet.getId());
					if (SBMLutilities.isInputPort(port)) {
						portDir.setSelectedItem(GlobalConstants.INPUT); 
					} else {
						portDir.setSelectedItem(GlobalConstants.OUTPUT); 
					}
				} else {
					portDir.setSelectedItem(GlobalConstants.INTERNAL);
				}
				
				if (paramsOnly && parameters.getSelectedValue()!=null) {
					if (((String) parameters.getSelectedValue()).contains("Modified")
						|| (((String) parameters.getSelectedValue()).contains("Custom"))
						|| (((String) parameters.getSelectedValue()).contains("Sweep"))) {
						type.setSelectedItem("Modified");
						sweep.setEnabled(true);
						paramValue
						.setText(((String) parameters.getSelectedValue()).split(" ")[((String) parameters.getSelectedValue()).split(" ").length - 1]);
						paramValue.setEnabled(true);
						placeMarking.setEnabled(true);
						paramUnits.setEnabled(false);
						if (paramValue.getText().trim().startsWith("(")) {
							try {
								start.setText((paramValue.getText().trim()).split(",")[0].substring(1).trim());
								stop.setText((paramValue.getText().trim()).split(",")[1].trim());
								step.setText((paramValue.getText().trim()).split(",")[2].trim());
								int lev = Integer.parseInt((paramValue.getText().trim()).split(",")[3].replace(")", "").trim());
								if (lev == 1) {
									level.setSelectedIndex(0);
								}
								else {
									level.setSelectedIndex(1);
								}
							}
							catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			List<URI> sbolURIs = new LinkedList<URI>();
			sbolField = new SBOLField2(sbolURIs, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND, 
					GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 1, false);
		}
		parametersPanel.add(idLabel);
		parametersPanel.add(paramID);
		parametersPanel.add(nameLabel);
		parametersPanel.add(paramName);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Value Type:");
			type.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						paramValue.setEnabled(true);
						placeMarking.setEnabled(true);
						paramUnits.setEnabled(false);
					}
					else {
						sweep.setEnabled(false);
						paramValue.setEnabled(false);
						placeMarking.setEnabled(false);
						paramUnits.setEnabled(false);
						SBMLDocument d = SBMLutilities.readSBML(file);
						if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).isSetValue()) {
							paramValue.setText(d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).getValue() + "");
						}
						else {
							paramValue.setText("");
						}
						if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).isSetUnits()) {
							paramUnits.setSelectedItem(d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).getUnits()	+ "");
						}
						if (paramValue.getText().equals(0)) {
							placeMarking.setSelectedIndex(0);
						} else {
							placeMarking.setSelectedIndex(1);
						}

					}
				}
			});
			parametersPanel.add(typeLabel);
			parametersPanel.add(type);
		}
		parametersPanel.add(onPortLabel);
		parametersPanel.add(portDir);
		parametersPanel.add(sboTermLabel);
		parametersPanel.add(SBOTerms);
		parametersPanel.add(valueLabel);
		if (isPlace || isBoolean) {
			parametersPanel.add(placeMarking);
		} else {
			parametersPanel.add(paramValue);
		}
		if (rateParam!=null) {
			parametersPanel.add(rateLabel);
			parametersPanel.add(rateValue);
		}
		if (paramsOnly) {
			parametersPanel.add(new JLabel());
			parametersPanel.add(sweep);
		}
		if (!isPlace && !isBoolean) {
			parametersPanel.add(unitLabel);
			parametersPanel.add(paramUnits);
		}
		if (!isPlace) {
			parametersPanel.add(constLabel);
			parametersPanel.add(paramConst);
		}
		if (!paramsOnly && !isPlace && !isBoolean) {
			parametersPanel.add(new JLabel("SBOL ComponentDefinition:  "));
			parametersPanel.add(sbolField);
		}
		Object[] options = { option, "Cancel" };
		String editorTitle = "Parameter Editor";
		if (isPlace) {
			editorTitle = "Place Editor";
		} else if (isBoolean) {
			editorTitle = "Boolean Editor";
		}
		int value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, editorTitle, JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		String[] dimID = new String[]{""};
		String[] dimensionIds = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			dimID = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), paramID.getText(), false);
			if(dimID!=null){
				dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
				error = SBMLutilities.checkID(bioModel.getSBMLDocument(), dimID[0].trim(), selectedID, false);
			} else {
				error = true;
			}
			if (!error) {
				if (isPlace | isBoolean) {
					if (placeMarking.getSelectedIndex()==0) {
						paramValue.setText("0.0");
					} else {
						paramValue.setText("1.0");
					}
				}
				double val = 0.0;
				double rateVal = 0.0;
				if (paramsOnly && paramValue.getText().trim().startsWith("(") && paramValue.getText().trim().endsWith(")")) {
					try {
						Double.parseDouble((paramValue.getText().trim()).split(",")[0].substring(1).trim());
						Double.parseDouble((paramValue.getText().trim()).split(",")[1].trim());
						Double.parseDouble((paramValue.getText().trim()).split(",")[2].trim());
						int lev = Integer.parseInt((paramValue.getText().trim()).split(",")[3].replace(")", "").trim());
						if (lev != 1 && lev != 2) {
							error = true;
							JOptionPane.showMessageDialog(Gui.frame, "The level can only be 1 or 2.", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
					catch (Exception e1) {
						error = true;
						JOptionPane.showMessageDialog(Gui.frame, "Invalid sweeping parameters.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				else {
					InitialAssignments.removeInitialAssignment(bioModel, selectedID);
					InitialAssignments.removeInitialAssignment(bioModel, selectedID + "_" + GlobalConstants.RATE);
					try {
						val = Double.parseDouble(paramValue.getText().trim());
					}
					catch (Exception e1) {
						error = InitialAssignments.addInitialAssignment(bioModel, paramValue.getText().trim(), dimID);
						val = 0.0;
					}
					if (rateParam!=null) {
						try {
							rateVal = Double.parseDouble(rateValue.getText().trim());
						}
						catch (Exception e1) {
							error = InitialAssignments.addInitialRateAssignment(bioModel, 
									rateValue.getText().trim(), dimID);
							rateVal = 0.0;
						}
					}
				}
				if (!error) {
					String unit = (String) paramUnits.getSelectedItem();
					String param = "";
					if (paramsOnly && !((String) type.getSelectedItem()).equals("Original")) {
						String[] params = new String[parameters.getModel().getSize()];
						int index = 0;
						for (int i = 0; i < parameters.getModel().getSize(); i++) {
							params[i] = parameters.getModel().getElementAt(i).toString();
							if (params[i].split(" ")[0].equals(selected)) index = i;
						}
						String[] splits = params[index].split(" ");
						for (int i = 0; i < splits.length - 2; i++) {
							param += splits[i] + " ";
						}
						if (!splits[splits.length - 2].equals("Modified") && !splits[splits.length - 2].equals("Custom")
								&& !splits[splits.length - 2].equals("Sweep")) {
							param += splits[splits.length - 2] + " " + splits[splits.length - 1] + " ";
						}
						if (paramValue.getText().trim().startsWith("(") && paramValue.getText().trim().endsWith(")")) {
							double startVal = Double.parseDouble((paramValue.getText().trim()).split(",")[0].substring(1).trim());
							double stopVal = Double.parseDouble((paramValue.getText().trim()).split(",")[1].trim());
							double stepVal = Double.parseDouble((paramValue.getText().trim()).split(",")[2].trim());
							int lev = Integer.parseInt((paramValue.getText().trim()).split(",")[3].replace(")", "").trim());
							param += "Sweep (" + startVal + "," + stopVal + "," + stepVal + "," + lev + ")";
						}
						else {
							param += "Modified " + val;
						}
					}
					else {
						if (dimID!=null) {
							param = dimID[0].trim(); // + " " + val;
							for (int i = 1; i < dimID.length; i++) {
								param += "[" + dimID[i] + "]";
							}
						}
						if (paramsOnly)
							param += " " + val;
						else {
							if (isPlace) {
								param += " - place";
							} else if (isBoolean) {
								param += " - boolean";
							} else {
								param += " - real";
							}
						}
						/*
						 * if (!unit.equals("( none )")) { param =
						 * dimID[0].trim() + " " + val + " " + unit; }
						 */
					}
					if (!error && option.equals("OK") && paramConst.getSelectedItem().equals("true")) {
						error = SBMLutilities.checkConstant(bioModel.getSBMLDocument(), "Parameters", selected);
					}
					if (!error && option.equals("OK") && paramConst.getSelectedItem().equals("false")) {
						error = checkNotConstant(selected);
					}
					if (!error) {
						Parameter paramet;
						if (option.equals("OK")) {
							int index = -1;
							String[] params = new String[parameters.getModel().getSize()];
							for (int i = 0; i < parameters.getModel().getSize(); i++) {
								params[i] = parameters.getModel().getElementAt(i).toString();
								if (params[i].split(" ")[0].equals(selected)) index = i;
							}
							paramet = bioModel.getSBMLDocument().getModel().getParameter(selected);

							if (dimID!=null) {
								paramet.setId(dimID[0].trim());
							}
							SBMLutilities.createDimensions(paramet, dimensionIds, dimID);
							paramet.setName(paramName.getText().trim());
							if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
								paramet.unsetSBOTerm();
							} else {
								paramet.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
							}
							if (paramConst.getSelectedItem().equals("true")) {
								paramet.setConstant(true);
							}
							else {
								paramet.setConstant(false);
							}
							bioModel.createDirPort(paramet.getId(),(String)portDir.getSelectedItem());
							paramet.setValue(val);
							if (rateParam!=null) {
								if (dimID!=null) {
									rateParam.setId(dimID[0].trim()+"_"+GlobalConstants.RATE);
								}
								rateParam.setValue(rateVal);
							}
							if (unit.equals("( none )")) {
								paramet.unsetUnits();
							}
							else {
								paramet.setUnits(unit);
							}
							if (!constantsOnly || paramet.getConstant()) {
								if (index >= 0) {
									parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									params = Utility.getList(params, parameters);
									parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									params[index] = param;
									Utility.sort(params);
									parameters.setListData(params);
									parameters.setSelectedIndex(index);
								} else {
									JList add = new JList();
									Object[] adding = { param };
									add.setListData(adding);
									add.setSelectedIndex(0);
									parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									adding = Utility.add(params, parameters, add);
									params = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										params[i] = (String) adding[i];
									}
									Utility.sort(params);
									parameters.setListData(params);
									parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									if (bioModel.getSBMLDocument().getModel().getParameterCount() == 1) {
										parameters.setSelectedIndex(0);
									}
									else {
										parameters.setSelectedIndex(index);
									}
								}
								if (paramet.getConstant()) {
									Layout layout = bioModel.getLayout();
									if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+selected)!=null) {
										layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+selected);
									}
									if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
										layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+selected);
									}
								} 
							} else if (constantsOnly) {
								if (index >= 0) { 
									parameters.setSelectedIndex(index);
									parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
									Utility.remove(parameters);
									parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								}
							}
							if (paramsOnly) {
								int remove = -1;
								for (int i = 0; i < parameterChanges.size(); i++) {
									if (dimID!=null && parameterChanges.get(i).split(" ")[0].equals(dimID[0].trim())) {
										remove = i;
									}
								}
								if (remove != -1) {
									parameterChanges.remove(remove);
								}
								if (!((String) type.getSelectedItem()).equals("Original")) {
									parameterChanges.add(param);
								}
							}
							else {
								if (dimID!=null) {
									SBMLutilities.updateVarId(bioModel.getSBMLDocument(), false, selected, dimID[0].trim());
								}
								if (rateParam!=null && dimID!=null) {
									SBMLutilities.updateVarId(bioModel.getSBMLDocument(), false, selected+"_"+GlobalConstants.RATE, 
											dimID[0].trim()+"_"+GlobalConstants.RATE);
								}
							}
							if (paramet.getId().equals(GlobalConstants.STOICHIOMETRY_STRING)) {
								for (int i=0; i<model.getReactionCount(); i++) {
									Reaction r = model.getReaction(i);
									if (BioModel.isProductionReaction(r)) {
										if (r.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING)==null) {
											for (int j=0; j<r.getProductCount(); j++) {
												r.getProduct(j).setStoichiometry(paramet.getValue());
											}
										}
									}
								}
							} else if (paramet.getId().equals(GlobalConstants.COOPERATIVITY_STRING)) {
								for (int i=0; i<model.getReactionCount(); i++) {
									Reaction r = model.getReaction(i);
									if (BioModel.isComplexReaction(r)) {
										for (int j=0; j<r.getReactantCount(); j++) {
											SpeciesReference reactant = r.getReactant(j);
											if (r.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + reactant.getSpecies())==null) {
												reactant.setStoichiometry(paramet.getValue());
											}
										}
									}
								}
							}
						}
						else {
							String[] params = new String[parameters.getModel().getSize()];
							int index = 0;
							for (int i = 0; i < parameters.getModel().getSize(); i++) {
								params[i] = parameters.getModel().getElementAt(i).toString();
								if (params[i].equals(selected)) index = i;
							}
							paramet = bioModel.getSBMLDocument().getModel().createParameter();
							if (dimID!=null)
								paramet.setId(dimID[0].trim());
							paramet.setName(paramName.getText().trim());
							if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
								paramet.unsetSBOTerm();
							} else {
								paramet.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
							}
							SBMLutilities.createDimensions(paramet, dimensionIds, dimID);
							if (paramConst.getSelectedItem().equals("true")) {
								paramet.setConstant(true);
							}
							else {
								paramet.setConstant(false);
							}
							if (isPlace) {
								paramet.setSBOTerm(GlobalConstants.SBO_PETRI_NET_PLACE);
								paramet.setConstant(false);
							} else if (isBoolean) {
								paramet.setSBOTerm(GlobalConstants.SBO_LOGICAL);
								//paramet.setConstant(false);
							}
							paramet.setValue(val);
							if (!unit.equals("( none )")) {
								paramet.setUnits(unit);
							}
							bioModel.createDirPort(paramet.getId(),(String)portDir.getSelectedItem());
							if (!constantsOnly || paramet.getConstant()) {
								JList add = new JList();
								Object[] adding = { param };
								add.setListData(adding);
								add.setSelectedIndex(0);
								parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Utility.add(params, parameters, add);
								params = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									params[i] = (String) adding[i];
								}
								Utility.sort(params);
								parameters.setListData(params);
								parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (bioModel.getSBMLDocument().getModel().getParameterCount() == 1) {
									parameters.setSelectedIndex(0);
								}
								else {
									parameters.setSelectedIndex(index);
								}
							}
						}
						if (!error && !paramsOnly & !isPlace & !isBoolean) {
							// Add SBOL annotation to species
							if (sbolField.getSBOLURIs().size() > 0) {
								if (!paramet.isSetMetaId() || paramet.getMetaId().equals(""))
									SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), paramet, 
											bioModel.getMetaIDIndex());
								SBOLAnnotation sbolAnnot = new SBOLAnnotation(paramet.getMetaId(), sbolField.getSBOLURIs(),
										sbolField.getSBOLStrand());
								AnnotationUtility.setSBOLAnnotation(paramet, sbolAnnot);
							} else 
								AnnotationUtility.removeSBOLAnnotation(paramet);
						}
						modelEditor.setDirty(true);
						bioModel.makeUndoPoint();
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, editorTitle, JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return selected;
		}
		return paramID.getText().split("\\[")[0].trim();
	}

	/**
	 * Parameter that is used in a conversion factor must be constant.
	 */
	private boolean checkNotConstant(String val) {
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getSpeciesCount(); i++) {
			Species species = bioModel.getSBMLDocument().getModel().getSpecies(i);
			if (species.isSetConversionFactor() && species.getConversionFactor().equals(val)) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Parameter must be constant because it is used as a conversion factor for " + species.getId() + ".",
						" Parameter Must Be Constant", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove a global parameter
	 */
	private boolean removeParameter(String selected) {
		if (!SBMLutilities.variableInUse(bioModel.getSBMLDocument(), selected, false, true, true)) {
			Parameter tempParameter = bioModel.getSBMLDocument().getModel().getParameter(selected);
			ListOf<Parameter> p = bioModel.getSBMLDocument().getModel().getListOfParameters();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
				if (p.get(i).getId().equals(tempParameter.getId())) {
					p.remove(i);
				}
			}
			for (int i = 0; i < bioModel.getSBMLCompModel().getListOfPorts().size(); i++) {
				Port port = bioModel.getSBMLCompModel().getListOfPorts().get(i);
				if (port.isSetIdRef() && port.getIdRef().equals(tempParameter.getId())) {
					bioModel.getSBMLCompModel().getListOfPorts().remove(i);
					break;
				}
			}
			Layout layout = bioModel.getLayout();
			if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+selected)!=null) {
				layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+selected);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
				layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+selected);
			}
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
			return true;
		}
		return false;
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add parameters button is clicked
		if (e.getSource() == addParam) {
			parametersEditor("Add","",false,false);
		} else if (e.getSource() == addBool) {
			parametersEditor("Add","",true,false);
		} else if (e.getSource() == addPlace) {
			parametersEditor("Add","",false,true);
		}
		// if the edit parameters button is clicked
		else if (e.getSource() == editParam) {
			if (parameters.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No parameter selected.", "Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) parameters.getSelectedValue()).split("\\[| ")[0];
			parametersEditor("OK",selected,false,false);
			initialsPanel.refreshInitialAssignmentPanel(bioModel);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove parameters button is clicked
		else if (e.getSource() == removeParam) {
			int index = parameters.getSelectedIndex();
			if (index != -1) {
				if (removeParameter(((String) parameters.getSelectedValue()).split("\\[| ")[0])) {
					parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					Utility.remove(parameters);
					parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (index < parameters.getModel().getSize()) {
						parameters.setSelectedIndex(index);
					}
					else {
						parameters.setSelectedIndex(index - 1);
					}
				}
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == parameters) {
				if (parameters.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No parameter selected.", "Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) parameters.getSelectedValue()).split("\\[| ")[0];
				parametersEditor("OK",selected,false,false);
				initialsPanel.refreshInitialAssignmentPanel(bioModel);
				rulesPanel.refreshRulesPanel();
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
