package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.MutableBoolean;
import main.util.Utility;

import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.Layout;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Port;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.UnitDefinition;

import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;


/**
 * This is a class for creating SBML parameters
 * 
 * @author Chris Myers
 * 
 */
public class Parameters extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JList parameters; // JList of parameters

	private JButton addParam, removeParam, editParam; // parameters buttons

	private JTextField paramID, paramName, paramValue;

	private JComboBox paramUnits;

	private JComboBox paramConst;
	
	private JCheckBox onPort;

	private BioModel gcm;

	private MutableBoolean dirty;

	private Boolean paramsOnly;

	private String file;

	private ArrayList<String> parameterChanges;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;

	private Gui biosim;
	
	private boolean constantsOnly;

	public Parameters(Gui biosim, BioModel gcm, MutableBoolean dirty, Boolean paramsOnly, ArrayList<String> getParams,
			String file, ArrayList<String> parameterChanges, boolean constantsOnly) {
		super(new BorderLayout());
		this.gcm = gcm;
		this.dirty = dirty;
		this.paramsOnly = paramsOnly;
		this.file = file;
		this.parameterChanges = parameterChanges;
		this.biosim = biosim;
		this.constantsOnly = constantsOnly;
		Model model = gcm.getSBMLDocument().getModel();
		JPanel addParams = new JPanel();
		if (constantsOnly) {
			addParam = new JButton("Add Constant");
			removeParam = new JButton("Remove Constant");
			editParam = new JButton("Edit Constant");
		} else {
			addParam = new JButton("Add Parameter");
			removeParam = new JButton("Remove Parameter");
			editParam = new JButton("Edit Parameter");
		}
		addParams.add(addParam);
		addParams.add(removeParam);
		addParams.add(editParam);
		addParam.addActionListener(this);
		removeParam.addActionListener(this);
		editParam.addActionListener(this);
		if (paramsOnly) {
			addParam.setEnabled(false);
			removeParam.setEnabled(false);
		}
		JLabel parametersLabel;
		if (constantsOnly) {
			parametersLabel = new JLabel("List of Global Parameters:");
		} else {
			parametersLabel = new JLabel("List of Global Parameters:");
		}
		parameters = new JList();
		parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll3 = new JScrollPane();
		scroll3.setViewportView(parameters);
		ListOf listOfParameters = model.getListOfParameters();
		String[] params = new String[(int) model.getNumParameters()];
		int notIncludedParametersCount = 0;
		
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			
			params[i] = parameter.getId(); 

			if (parameter.getId().contains("_locations"))
				++notIncludedParametersCount;
			if (constantsOnly && !parameter.getConstant()) {
				++notIncludedParametersCount;
				params[i] = params[i] + "__DELETE";
			}
			
			if (paramsOnly) {
				params[i] = parameter.getId() + " " + parameter.getValue();
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
			else {				
				params[j] = paramsCopy[i];
				j++;
			}
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
		this.gcm = gcm;
		Model model = gcm.getSBMLDocument().getModel();
		ListOf listOfParameters = model.getListOfParameters();
		
		int skip = 0;
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			if (constantsOnly && !parameter.getConstant()) skip++;
		}
		int k = 0;
		String[] params = new String[(int) model.getNumParameters()-skip];
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter parameter = (Parameter) listOfParameters.get(i);
			if (constantsOnly && !parameter.getConstant()) continue;
			params[k] = parameter.getId();
			if (paramsOnly) {
				params[k] += " " + parameter.getValue();
				for (int j = 0; j < parameterChanges.size(); j++) {
					if (parameterChanges.get(j).split(" ")[0].equals(params[k].split(" ")[0])) {
						parameterChanges.set(j,
								params[k] + " " + parameterChanges.get(j).split(" ")[2] + " " + parameterChanges.get(j).split(" ")[3]);
						params[k] = parameterChanges.get(j);
					}
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
	public String parametersEditor(String option,String selected) {
		JPanel parametersPanel;
		if (paramsOnly) {
			parametersPanel = new JPanel(new GridLayout(8, 2));
		}
		else {
			parametersPanel = new JPanel(new GridLayout(6, 2));
		}
		JLabel idLabel = new JLabel("ID:");
		JLabel nameLabel = new JLabel("Name:");
		JLabel valueLabel = new JLabel("Initial Value:");
		JLabel unitLabel = new JLabel("Units:");
		JLabel constLabel = new JLabel("Constant:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		paramID = new JTextField();
		paramName = new JTextField();
		paramValue = new JTextField();
		paramUnits = new JComboBox();
		paramUnits.addItem("( none )");
		onPort = new JCheckBox();
		Model model = gcm.getSBMLDocument().getModel();
		ListOf listOfUnits = model.getListOfUnitDefinitions();
		String[] units = new String[(int) model.getNumUnitDefinitions()];
		for (int i = 0; i < model.getNumUnitDefinitions(); i++) {
			UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
			units[i] = unit.getId();
		}
		for (int i = 0; i < units.length; i++) {
			if (gcm.getSBMLDocument().getLevel() > 2
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
		if (gcm.getSBMLDocument().getLevel() < 3) {
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
			public void actionPerformed(ActionEvent e) {
				Object[] options = { "Ok", "Close" };
				JPanel p = new JPanel(new GridLayout(4, 2));
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
			paramUnits.setEnabled(false);
			paramConst.setEnabled(false);
			onPort.setEnabled(false);
			sweep.setEnabled(false);
		}
		String selectedID = "";
		if (option.equals("OK")) {
			try {
				Parameter paramet = gcm.getSBMLDocument().getModel().getParameter(selected);
				paramID.setText(paramet.getId());
				selectedID = paramet.getId();
				paramName.setText(paramet.getName());
				if (paramet.getConstant()) {
					paramConst.setSelectedItem("true");
				}
				else {
					paramConst.setSelectedItem("false");
				}
				if (paramsOnly) {
					if (paramet.isSetValue()) {
						paramValue.setText("" + paramet.getValue());
					}
				} else {
					InitialAssignment init = gcm.getSBMLDocument().getModel().getInitialAssignment(selectedID);
					if (init!=null) {
						paramValue.setText(SBMLutilities.myFormulaToString(init.getMath()));
					} else if (paramet.isSetValue()) {
						paramValue.setText("" + paramet.getValue());
					}
				}
				if (paramet.isSetUnits()) {
					paramUnits.setSelectedItem(paramet.getUnits());
				}
				if (gcm.getPortByIdRef(paramet.getId())!=null) {
					onPort.setSelected(true);
				} else {
					onPort.setSelected(false);
				}
				if (paramsOnly && (((String) parameters.getSelectedValue()).contains("Modified"))
						|| (((String) parameters.getSelectedValue()).contains("Custom"))
						|| (((String) parameters.getSelectedValue()).contains("Sweep"))) {
					type.setSelectedItem("Modified");
					sweep.setEnabled(true);
					paramValue
							.setText(((String) parameters.getSelectedValue()).split(" ")[((String) parameters.getSelectedValue()).split(" ").length - 1]);
					paramValue.setEnabled(true);
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
						}
					}
				}
			}
			catch (Exception e) {
			}
		}
		parametersPanel.add(idLabel);
		parametersPanel.add(paramID);
		parametersPanel.add(nameLabel);
		parametersPanel.add(paramName);
		if (paramsOnly) {
			JLabel typeLabel = new JLabel("Type:");
			type.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!((String) type.getSelectedItem()).equals("Original")) {
						sweep.setEnabled(true);
						paramValue.setEnabled(true);
						paramUnits.setEnabled(false);
					}
					else {
						sweep.setEnabled(false);
						paramValue.setEnabled(false);
						paramUnits.setEnabled(false);
						SBMLDocument d = Gui.readSBML(file);
						if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).isSetValue()) {
							paramValue.setText(d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).getValue() + "");
						}
						else {
							paramValue.setText("");
						}
						if (d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).isSetUnits()) {
							paramUnits.setSelectedItem(d.getModel().getParameter(((String) parameters.getSelectedValue()).split(" ")[0]).getUnits()	+ "");
						}
					}
				}
			});
			parametersPanel.add(typeLabel);
			parametersPanel.add(type);
		}
		parametersPanel.add(valueLabel);
		parametersPanel.add(paramValue);
		if (paramsOnly) {
			parametersPanel.add(new JLabel());
			parametersPanel.add(sweep);
		}
		parametersPanel.add(unitLabel);
		parametersPanel.add(paramUnits);
		parametersPanel.add(constLabel);
		parametersPanel.add(paramConst);
		parametersPanel.add(onPortLabel);
		parametersPanel.add(onPort);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = SBMLutilities.checkID(gcm.getSBMLDocument(), paramID.getText().trim(), selectedID, false, false);
			if (!error) {
				double val = 0.0;
				if (paramValue.getText().trim().startsWith("(") && paramValue.getText().trim().endsWith(")")) {
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
					InitialAssignments.removeInitialAssignment(gcm, selectedID);
					try {
						val = Double.parseDouble(paramValue.getText().trim());
					}
					catch (Exception e1) {
						error = InitialAssignments.addInitialAssignment(biosim, gcm, paramID.getText().trim(), 
								paramValue.getText().trim());
						val = 0.0;
						/*
						JOptionPane.showMessageDialog(Gui.frame, 
								"The value must be a real number.", "Enter A Valid Value", JOptionPane.ERROR_MESSAGE);
						error = true;
						*/
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
							if (params[i].equals(selected)) index = i;
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
						param = paramID.getText().trim(); // + " " + val;
						if (paramsOnly)
							param += " " + val;
						/*
						 * if (!unit.equals("( none )")) { param =
						 * paramID.getText().trim() + " " + val + " " + unit; }
						 */
					}
					if (!error && option.equals("OK") && paramConst.getSelectedItem().equals("true")) {
						error = SBMLutilities.checkConstant(gcm.getSBMLDocument(), "Parameters", selected);
					}
					if (!error && option.equals("OK") && paramConst.getSelectedItem().equals("false")) {
						error = checkNotConstant(selected);
					}
					if (!error) {
						if (option.equals("OK")) {
							int index = -1;
							String[] params = new String[parameters.getModel().getSize()];
							for (int i = 0; i < parameters.getModel().getSize(); i++) {
								params[i] = parameters.getModel().getElementAt(i).toString();
								if (params[i].equals(selected)) index = i;
							}
							Parameter paramet = gcm.getSBMLDocument().getModel().getParameter(selected);
							paramet.setId(paramID.getText().trim());
							paramet.setName(paramName.getText().trim());
							if (paramConst.getSelectedItem().equals("true")) {
								paramet.setConstant(true);
							}
							else {
								paramet.setConstant(false);
							}
							Port port = gcm.getPortByIdRef(selected);
							if (port!=null) {
								if (onPort.isSelected()) {
									port.setId(GlobalConstants.PARAMETER+"__"+paramet.getId());
									port.setIdRef(paramet.getId());
								} else {
									port.removeFromParentAndDelete();
								}
							} else {
								if (onPort.isSelected()) {
									port = gcm.getSBMLCompModel().createPort();
									port.setId(GlobalConstants.PARAMETER+"__"+paramet.getId());
									port.setIdRef(paramet.getId());
								}
							}
							paramet.setValue(val);
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
									adding = Utility.add(params, parameters, add, null, null, null, null, null, Gui.frame);
									params = new String[adding.length];
									for (int i = 0; i < adding.length; i++) {
										params[i] = (String) adding[i];
									}
									Utility.sort(params);
									parameters.setListData(params);
									parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									if (gcm.getSBMLDocument().getModel().getNumParameters() == 1) {
										parameters.setSelectedIndex(0);
									}
									else {
										parameters.setSelectedIndex(index);
									}
								}
								if (paramet.getConstant()) {
									if (gcm.getSBMLLayout().getLayout("iBioSim") != null) {
										Layout layout = gcm.getSBMLLayout().getLayout("iBioSim"); 
										if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+selected)!=null) {
											layout.removeSpeciesGlyph(GlobalConstants.GLYPH+"__"+selected);
										}
										if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
											layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected);
										}
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
									if (parameterChanges.get(i).split(" ")[0].equals(paramID.getText().trim())) {
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
								SBMLutilities.updateVarId(gcm.getSBMLDocument(), false, selected, paramID.getText().trim());
							}
							if (paramet.getId().equals(GlobalConstants.STOICHIOMETRY_STRING)) {
								for (long i=0; i<model.getNumReactions(); i++) {
									Reaction r = model.getReaction(i);
									if (BioModel.isProductionReaction(r)) {
										if (r.getKineticLaw().getLocalParameter(GlobalConstants.STOICHIOMETRY_STRING)==null) {
											for (long j=0; j<r.getNumProducts(); j++) {
												r.getProduct(j).setStoichiometry(paramet.getValue());
											}
										}
									}
								}
							} else if (paramet.getId().equals(GlobalConstants.COOPERATIVITY_STRING)) {
								for (long i=0; i<model.getNumReactions(); i++) {
									Reaction r = model.getReaction(i);
									if (r.getAnnotationString().contains("Complex")) {
										for (long j=0; j<r.getNumReactants(); j++) {
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
							Parameter paramet = gcm.getSBMLDocument().getModel().createParameter();
							paramet.setId(paramID.getText().trim());
							paramet.setName(paramName.getText().trim());
							if (paramConst.getSelectedItem().equals("true")) {
								paramet.setConstant(true);
							}
							else {
								paramet.setConstant(false);
							}
							paramet.setValue(val);
							if (!unit.equals("( none )")) {
								paramet.setUnits(unit);
							}
							if (onPort.isSelected()) {
								Port port = gcm.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.PARAMETER+"__"+paramet.getId());
								port.setIdRef(paramet.getId());
							}
							if (!constantsOnly || paramet.getConstant()) {
								JList add = new JList();
								Object[] adding = { param };
								add.setListData(adding);
								add.setSelectedIndex(0);
								parameters.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
								adding = Utility.add(params, parameters, add, null, null, null, null, null, Gui.frame);
								params = new String[adding.length];
								for (int i = 0; i < adding.length; i++) {
									params[i] = (String) adding[i];
								}
								Utility.sort(params);
								parameters.setListData(params);
								parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								if (gcm.getSBMLDocument().getModel().getNumParameters() == 1) {
									parameters.setSelectedIndex(0);
								}
								else {
									parameters.setSelectedIndex(index);
								}
							}
						}
						dirty.setValue(true);
						gcm.makeUndoPoint();
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, parametersPanel, "Parameter Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return selected;
		}
		return paramID.getText().trim();
	}

	/**
	 * Parameter that is used in a conversion factor must be constant.
	 */
	private boolean checkNotConstant(String val) {
		for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumSpecies(); i++) {
			Species species = gcm.getSBMLDocument().getModel().getSpecies(i);
			if (species.getConversionFactor().equals(val)) {
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
		if (!SBMLutilities.variableInUse(gcm.getSBMLDocument(), selected, false, true, true)) {
			Parameter tempParameter = gcm.getSBMLDocument().getModel().getParameter(selected);
			ListOf p = gcm.getSBMLDocument().getModel().getListOfParameters();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumParameters(); i++) {
				if (((Parameter) p.get(i)).getId().equals(tempParameter.getId())) {
					p.remove(i);
				}
			}
			for (long i = 0; i < gcm.getSBMLCompModel().getNumPorts(); i++) {
				Port port = gcm.getSBMLCompModel().getPort(i);
				if (port.isSetIdRef() && port.getIdRef().equals(tempParameter.getId())) {
					gcm.getSBMLCompModel().removePort(i);
					break;
				}
			}
			if (gcm.getSBMLLayout().getLayout("iBioSim") != null) {
				Layout layout = gcm.getSBMLLayout().getLayout("iBioSim"); 
				if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+selected)!=null) {
					layout.removeSpeciesGlyph(GlobalConstants.GLYPH+"__"+selected);
				}
				if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
					layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected);
				}
			}
			dirty.setValue(true);
			gcm.makeUndoPoint();
			return true;
		}
		return false;
	}

	public void setPanels(InitialAssignments initialsPanel, Rules rulesPanel) {
		this.initialsPanel = initialsPanel;
		this.rulesPanel = rulesPanel;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add compartment type button is clicked
		// if the add species type button is clicked
		// if the add compartment button is clicked
		// if the add parameters button is clicked
		if (e.getSource() == addParam) {
			parametersEditor("Add","");
		}
		// if the edit parameters button is clicked
		else if (e.getSource() == editParam) {
			if (parameters.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No parameter selected.", "Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) parameters.getSelectedValue()).split(" ")[0];
			parametersEditor("OK",selected);
			initialsPanel.refreshInitialAssignmentPanel(gcm);
			rulesPanel.refreshRulesPanel();
		}
		// if the remove parameters button is clicked
		else if (e.getSource() == removeParam) {
			int index = parameters.getSelectedIndex();
			if (index != -1) {
				if (removeParameter(((String) parameters.getSelectedValue()).split(" ")[0])) {
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

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == parameters) {
				if (parameters.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No parameter selected.", "Must Select A Parameter", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) parameters.getSelectedValue()).split(" ")[0];
				parametersEditor("OK",selected);
				initialsPanel.refreshInitialAssignmentPanel(gcm);
				rulesPanel.refreshRulesPanel();
			}
		}
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}

}
