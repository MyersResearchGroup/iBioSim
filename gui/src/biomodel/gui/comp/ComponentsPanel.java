package biomodel.gui.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.comp.Submodel;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.sbol.SBOLField2;
//import biomodel.gui.sbol.SBOLField;
import biomodel.gui.schematic.ModelEditor;
import biomodel.gui.util.PropertyField;
import biomodel.gui.util.PropertyList;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;
import biomodel.util.Utility;
import main.Gui;


public class ComponentsPanel extends JPanel implements ActionListener, MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String selected = "";

	private String[] options = { "Ok", "Cancel" };
	
	private ArrayList<String> portIds = null;
	
	private ArrayList<String> idRefs = null;
	
	private ArrayList<String> types = null;

	private ArrayList<JTextField> dimensionsField = null;

	private ArrayList<JTextField> portIndicesField = null;

	private ArrayList<JTextField> subModelIndicesField = null;

	private ArrayList<JComboBox> portmapBox = null;

	private ArrayList<JComboBox> directionBox = null;

	private ArrayList<JComboBox> convBox = null;

	private BioModel bioModel = null;

	private PropertyList componentsList = null;

	private HashMap<String, PropertyField> fields = null;
	
	private SBOLField2 sbolField;

	private String selectedComponent, oldPort;
	
	private ModelEditor modelEditor;
	
	private String subModelId;
	
	private JComboBox timeConvFactorBox;
	
	private JComboBox extentConvFactorBox;
	
	private boolean paramsOnly;
	
	private BioModel subBioModel;
	
	private JList replacementsDeletions;
	
	private String[] replDel;

	public ComponentsPanel(String selected, PropertyList componentsList, BioModel bioModel, BioModel subBioModel,
			ArrayList<String> ports, String selectedComponent, String oldPort, boolean paramsOnly, ModelEditor gcmEditor) {
		
		super(new BorderLayout());
		
		this.selected = selected;
		this.componentsList = componentsList;
		this.bioModel = bioModel;
		this.modelEditor = gcmEditor;
		this.selectedComponent = selectedComponent;
		this.oldPort = oldPort;
		this.paramsOnly = paramsOnly;
		this.subBioModel = subBioModel;
		//this.setPreferredSize(new Dimension(800, 600));

		fields = new HashMap<String, PropertyField>();
		portIds = new ArrayList<String>();
		idRefs = new ArrayList<String>();
		types = new ArrayList<String>();
		dimensionsField = new ArrayList<JTextField>();
		portIndicesField = new ArrayList<JTextField>();
		subModelIndicesField = new ArrayList<JTextField>();
		portmapBox = new ArrayList<JComboBox>();
		directionBox = new ArrayList<JComboBox>();
		convBox = new ArrayList<JComboBox>();

		String[] directions = new String[2];
		directions[0] = "<--";
		directions[1] = "-->";
		
		if (bioModel.isGridEnabled()) {
			subModelId = "GRID__" + selectedComponent.replace(".xml","");
		} else {
			subModelId = selected;
		}
		
		ArrayList <String> constParameterList = bioModel.getConstantUserParameters();
		Collections.sort(constParameterList);
		String[] parameters = new String[constParameterList.size()+1];
		parameters[0] = "(none)";
		for (int l = 1; l < parameters.length; l++) {
			parameters[l] = constParameterList.get(l-1);
		}
		timeConvFactorBox = new JComboBox(parameters);
		extentConvFactorBox = new JComboBox(parameters);

		createPortMapFields(bioModel.getCompartments(), ports, GlobalConstants.COMPARTMENT, parameters);
		createPortMapFields(bioModel.getParameters(), ports, GlobalConstants.PARAMETER, parameters);
		createPortMapFields(bioModel.getParameters(), ports, GlobalConstants.LOCALPARAMETER, parameters);
		createPortMapFields(bioModel.getBooleans(), ports, GlobalConstants.BOOLEAN, parameters);
		createPortMapFields(bioModel.getPlaces(), ports, GlobalConstants.PLACE, parameters);
		createPortMapFields(bioModel.getSpecies(), ports, GlobalConstants.SBMLSPECIES, parameters);
		createPortMapFields(bioModel.getPromoters(), ports, GlobalConstants.PROMOTER, parameters);
		createPortMapFields(bioModel.getReactions(), ports, GlobalConstants.SBMLREACTION, parameters);
		createPortMapFields(bioModel.getFunctions(), ports, GlobalConstants.FUNCTION, parameters);
		createPortMapFields(bioModel.getUnits(), ports, GlobalConstants.UNIT, parameters);
		createPortMapFields(bioModel.getAlgebraicRules(), ports, GlobalConstants.ALGEBRAIC_RULE, parameters);
		createPortMapFields(bioModel.getAssignmentRules(), ports, GlobalConstants.ASSIGNMENT_RULE, parameters);
		createPortMapFields(bioModel.getRateRules(), ports, GlobalConstants.RATE_RULE, parameters);
		createPortMapFields(bioModel.getConstraints(), ports, GlobalConstants.CONSTRAINT, parameters);
		createPortMapFields(bioModel.getEvents(), ports, GlobalConstants.EVENT, parameters);
		createPortMapFields(bioModel.getTransitions(), ports, GlobalConstants.TRANSITION, parameters);

		String[] Choices = new String[2];
		Choices[0] = "--include--";
		Choices[1] = "--delete--";
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (!type.equals(GlobalConstants.COMPARTMENT) && !type.equals(GlobalConstants.PARAMETER) &&
				!type.equals(GlobalConstants.LOCALPARAMETER) && !type.equals(GlobalConstants.EVENT) &&
				!type.equals(GlobalConstants.SBMLSPECIES) && !type.equals(GlobalConstants.SBMLREACTION) && 
				!type.equals(GlobalConstants.FUNCTION) && !type.equals(GlobalConstants.UNIT) &&
				!type.equals(GlobalConstants.ASSIGNMENT_RULE) && !type.equals(GlobalConstants.RATE_RULE) &&
				!type.equals(GlobalConstants.ALGEBRAIC_RULE) && !type.equals(GlobalConstants.CONSTRAINT)  && 
				!type.equals(GlobalConstants.PROMOTER) && !type.equals(GlobalConstants.BOOLEAN) && 
				!type.equals(GlobalConstants.PLACE) && !type.equals(GlobalConstants.TRANSITION)) {
				portIds.add(portId);
				idRefs.add(idRef.replace("init__",""));
				types.add(type);
				JComboBox port = new JComboBox(Choices);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				dirport.setEnabled(false);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
				JTextField dimension = new JTextField();
				dimensionsField.add(dimension);
				JTextField portIndices = new JTextField();
				portIndicesField.add(portIndices);
				JTextField subModelIndices = new JTextField();
				subModelIndicesField.add(subModelIndices);
			}
		}
		

		Submodel instance = bioModel.getSBMLCompModel().getListOfSubmodels().get(subModelId);
		// ID field
		String dimInID = SBMLutilities.getDimensionString(instance);
		PropertyField field = new PropertyField(GlobalConstants.ID, "", null, null,
				Utility.IDDimString, paramsOnly, "default", false);
		fields.put(GlobalConstants.ID, field);
		JPanel top = new JPanel(new GridLayout(6, 1));
		top.add(field);

		// Name field
		if (instance != null) {
			field = new PropertyField(GlobalConstants.NAME, instance.getName(), null, null, Utility.NAMEstring, paramsOnly, 
					"default", false);
		} else {
			field = new PropertyField(GlobalConstants.NAME, "", null, null, Utility.NAMEstring, paramsOnly, 
					"default", false);
		}
		fields.put(GlobalConstants.NAME, field);
		top.add(field);

		JLabel timeConvFactorLabel = new JLabel("Time Conversion Factor");
		JLabel extentConvFactorLabel = new JLabel("Extent Conversion Factor");
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new GridLayout(1, 2));
		JPanel extentPanel = new JPanel();
		extentPanel.setLayout(new GridLayout(1, 2));
		timePanel.add(timeConvFactorLabel);
		timePanel.add(timeConvFactorBox);
		extentPanel.add(extentConvFactorLabel);
		extentPanel.add(extentConvFactorBox);
		if (instance != null && instance.isSetTimeConversionFactor()) {
			timeConvFactorBox.setSelectedItem(instance.getTimeConversionFactor());
		}
		if (instance != null && instance.isSetExtentConversionFactor()) {
			extentConvFactorBox.setSelectedItem(instance.getExtentConversionFactor());
		}
		top.add(timePanel);
		top.add(extentPanel);
		// Parse out SBOL annotations and add to SBOL field
		if(!paramsOnly) {
			// Field for annotating submodel with SBOL DNA components
			List<URI> sbolURIs = new LinkedList<URI>(); 
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(instance, sbolURIs);
			sbolField = new SBOLField2(sbolURIs, sbolStrand, GlobalConstants.SBOL_COMPONENTDEFINITION, gcmEditor, 
					2, false);
			top.add(sbolField);
		}
		top.add(new JLabel("Port maps:"));

		replacementsDeletions = new JList();
		replacementsDeletions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(replacementsDeletions);
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(replacementsDeletions);
		replDel = new String[portIds.size()];
		for (int i = 0; i < portIds.size(); i++) {
			replDel[i] = idRefs.get(i) + " port is unchanged";
		}
		add(top,"North");
		add(scroll,"Center");

		// Port Map field
		/*
		JPanel headingPanel = new JPanel();
		JLabel typeLabel = new JLabel("Type");
		JLabel portLabel = new JLabel("Port");
		JLabel dirLabel = new JLabel("Direction");
		JLabel replLabel = new JLabel("Replacement");
		JLabel convLabel = new JLabel("Conversion");
		headingPanel.setLayout(new GridLayout(1, 5));
		headingPanel.add(typeLabel);
		headingPanel.add(portLabel);
		headingPanel.add(dirLabel);
		headingPanel.add(replLabel);
		headingPanel.add(convLabel);
		if (portIds.size()>0) {
			add(headingPanel);
		}
		//add(new JLabel("Ports"));
		for (int i = 0; i < portIds.size(); i++) {
			JPanel tempPanel = new JPanel();
			JLabel tempLabel = new JLabel(idRefs.get(i));
			JLabel tempLabel2 = new JLabel(types.get(i));
			tempPanel.setLayout(new GridLayout(1, 5));
			tempPanel.add(tempLabel2);
			tempPanel.add(tempLabel);
			tempPanel.add(directionBox.get(i));
			tempPanel.add(portmapBox.get(i));
			tempPanel.add(convBox.get(i));
			add(tempPanel);
			directionBox.get(i).addActionListener(this);
			portmapBox.get(i).addActionListener(this);
		}
		*/
		if (instance!=null) {
			for (int j = 0; j < instance.getListOfDeletions().size(); j++) {
				Deletion deletion = instance.getListOfDeletions().get(j);
				int l = portIds.indexOf(deletion.getPortRef());
				if (l >= 0) {
					portmapBox.get(l).setSelectedItem("--delete--");
					replDel[l] = idRefs.get(l) + " port is deleted";
					dimensionsField.get(l).setText(SBMLutilities.getDimensionString(deletion));
					portIndicesField.get(l).setText(SBMLutilities.getIndicesString(deletion,"comp:portRef"));
				}
			}
		}
		ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(bioModel.getSBMLDocument().getModel());
		for (int j = 0; j < elements.size(); j++) {
			SBase sbase = elements.get(j);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);
			if (sbmlSBase!=null) {
				if (sbase.getElementName().equals(GlobalConstants.ASSIGNMENT_RULE)||
						sbase.getElementName().equals(GlobalConstants.RATE_RULE)||
						sbase.getElementName().equals(GlobalConstants.ALGEBRAIC_RULE)||
						sbase.getElementName().equals(GlobalConstants.CONSTRAINT)) {
					getPortMap(sbmlSBase,sbase.getMetaId());
				} else {
					getPortMap(sbmlSBase,SBMLutilities.getId(sbase));
				}
			}
		}
		//String oldName = null;
		if (selected != null) {
			//oldName = selected;
			fields.get(GlobalConstants.ID).setValue(selected+dimInID);
		}
		updateComboBoxEnabling();
		//main.util.Utility.sort(replDel);
		replacementsDeletions.setListData(replDel);
		replacementsDeletions.setSelectedIndex(0);
		replacementsDeletions.addMouseListener(this);
		/*
		boolean display = false;
		while (!display) {
			display = openGui(oldName);
		}
		*/
	}
	
	private void createPortMapFields(ArrayList<String> replacementList,ArrayList<String> ports,String portType,
			String[] parameters) {
		Collections.sort(replacementList);
		String[] choices = new String[replacementList.size() + 2];
		choices[0] = "--none--";
		choices[1] = "--delete--";
		for (int l = 2; l < choices.length; l++) {
			choices[l] = replacementList.get(l - 2);
		}
		String[] directions = new String[2];
		directions[0] = "<--";
		directions[1] = "-->";
		for (int i = 0; i < ports.size(); i++) {
			String type = ports.get(i).split(":")[0];
			String portId = ports.get(i).split(":")[1];
			String idRef = ports.get(i).split(":")[2];
			if (type.equals(portType)) {
				portIds.add(portId);
				idRefs.add(idRef);
				types.add(type);
				JComboBox port = new JComboBox(choices);
				portmapBox.add(port);
				JComboBox dirport = new JComboBox(directions);
				directionBox.add(dirport);
				JComboBox convFactor = new JComboBox(parameters);
				convBox.add(convFactor);
				JTextField dimension = new JTextField();
				dimensionsField.add(dimension);
				JTextField portIndices = new JTextField();
				portIndicesField.add(portIndices);
				JTextField subModelIndices = new JTextField();
				subModelIndicesField.add(subModelIndices);
			}
		}
	}
	
	private void updateComboBoxEnabling() {
		for (int i = 0; i < portmapBox.size(); i++) {
			if (portmapBox.get(i).getSelectedIndex()<2 &&
				(directionBox.get(i).getSelectedIndex()!=0 ||
				 convBox.get(i).getSelectedIndex()!=0)) {
				directionBox.get(i).setSelectedIndex(0);
				directionBox.get(i).setEnabled(false);
				convBox.get(i).setSelectedIndex(0);
				convBox.get(i).setEnabled(false);
				dimensionsField.get(i).setEnabled(false);
				portIndicesField.get(i).setEnabled(false);
				subModelIndicesField.get(i).setEnabled(false);
			} else if (portmapBox.get(i).getSelectedIndex()<2) {
				directionBox.get(i).setEnabled(false);
				convBox.get(i).setEnabled(false);
				if (portmapBox.get(i).getSelectedIndex()==0) {
					dimensionsField.get(i).setEnabled(false);
					dimensionsField.get(i).setText("");
					portIndicesField.get(i).setEnabled(false);
					portIndicesField.get(i).setText("");
					subModelIndicesField.get(i).setEnabled(false);
					subModelIndicesField.get(i).setText("");
				} else {
					if (!idRefs.get(i).contains("[")) {
						dimensionsField.get(i).setEnabled(false);
						dimensionsField.get(i).setText("");
						portIndicesField.get(i).setEnabled(false);
						portIndicesField.get(i).setText("");
					} else {
						dimensionsField.get(i).setEnabled(true);
						portIndicesField.get(i).setEnabled(true);
					}
					subModelIndicesField.get(i).setEnabled(false);
					subModelIndicesField.get(i).setText("");
				}
			} else if (directionBox.get(i).getSelectedIndex()==1 &&
					convBox.get(i).getSelectedIndex()!=0) {
					directionBox.get(i).setEnabled(true);
					convBox.get(i).setSelectedIndex(0);
					convBox.get(i).setEnabled(false);
					dimensionsField.get(i).setEnabled(true);
					portIndicesField.get(i).setEnabled(true);
					subModelIndicesField.get(i).setEnabled(true);
			} else if (directionBox.get(i).getSelectedIndex()==1) {
					directionBox.get(i).setEnabled(true);
					convBox.get(i).setEnabled(false);
					if (!idRefs.get(i).contains("[")&&!fields.get(GlobalConstants.ID).getValue().contains("[")) {
						dimensionsField.get(i).setEnabled(false);
						dimensionsField.get(i).setText("");
					} else {
						dimensionsField.get(i).setEnabled(true);
					}
					if (!idRefs.get(i).contains("[")) {
						portIndicesField.get(i).setEnabled(false);
						portIndicesField.get(i).setText("");
					} else {
						portIndicesField.get(i).setEnabled(true);
					}
					if (!fields.get(GlobalConstants.ID).getValue().contains("[")) {
						subModelIndicesField.get(i).setEnabled(false);
						subModelIndicesField.get(i).setText("");
					} else {
						subModelIndicesField.get(i).setEnabled(true);
					}
			} else {
				directionBox.get(i).setEnabled(true);
				convBox.get(i).setEnabled(true);
				if (!idRefs.get(i).contains("[")&&!fields.get(GlobalConstants.ID).getValue().contains("[")) {
					dimensionsField.get(i).setEnabled(false);
					dimensionsField.get(i).setText("");
				} else {
					dimensionsField.get(i).setEnabled(true);
				}
				if (!idRefs.get(i).contains("[")) {
					portIndicesField.get(i).setEnabled(false);
					portIndicesField.get(i).setText("");
				} else {
					portIndicesField.get(i).setEnabled(true);
				}
				if (!fields.get(GlobalConstants.ID).getValue().contains("[")) {
					subModelIndicesField.get(i).setEnabled(false);
					subModelIndicesField.get(i).setText("");
				} else {
					subModelIndicesField.get(i).setEnabled(true);
				}
			}
		}
	}
	
	private void getPortMap(CompSBasePlugin sbmlSBase,String id) {
		for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++) {
			ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
			if (replacement.getSubmodelRef().equals(subModelId)) {
				if (replacement.isSetPortRef()) {
					int l = portIds.indexOf(replacement.getPortRef());
					if (l >= 0) {
						portmapBox.get(l).setSelectedItem(id);
						if (!portmapBox.get(l).getSelectedItem().equals(id)) {
							portmapBox.get(l).addItem(id);
							portmapBox.get(l).setSelectedItem(id);
						}
						directionBox.get(l).setSelectedIndex(0);
						convBox.get(l).setSelectedIndex(0);
						if (replacement.isSetConversionFactor()) {
							convBox.get(l).setSelectedItem(replacement.getConversionFactor());
						}
						dimensionsField.get(l).setText(SBMLutilities.getDimensionString(replacement));
						portIndicesField.get(l).setText(SBMLutilities.getIndicesString(replacement,"comp:portRef"));
						subModelIndicesField.get(l).setText(SBMLutilities.getIndicesString(replacement,"comp:submodelRef"));
						replDel[l] = idRefs.get(l) + " port is replaced by " + id;
					}
				}
				else if (replacement.isSetDeletion()) {
					Deletion deletion = bioModel.getSBMLCompModel().getListOfSubmodels().get(subModelId).getListOfDeletions().get(replacement.getDeletion());
					if (deletion!=null) {
						int l = portIds.indexOf(deletion.getPortRef());
						if (l >= 0) {
							portmapBox.get(l).setSelectedItem(id);
							if (!portmapBox.get(l).getSelectedItem().equals(id)) {
								portmapBox.get(l).addItem(id);
								portmapBox.get(l).setSelectedItem(id);
							}
							directionBox.get(l).setSelectedIndex(0);
							convBox.get(l).setSelectedIndex(0);
							dimensionsField.get(l).setText(SBMLutilities.getDimensionString(replacement));
							portIndicesField.get(l).setText(SBMLutilities.getIndicesString(replacement,"comp:portRef"));
							subModelIndicesField.get(l).setText(SBMLutilities.getIndicesString(replacement,"comp:submodelRef"));
							replDel[l] = idRefs.get(l) + " port is replaced by " + id;
						}
					}
				}
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId)) {
				if (replacement.isSetPortRef()) {
					int l = portIds.indexOf(replacement.getPortRef());
					if (l >= 0) {
						portmapBox.get(l).setSelectedItem(id);
						if (!portmapBox.get(l).getSelectedItem().equals(id)) {
							portmapBox.get(l).addItem(id);
							portmapBox.get(l).setSelectedItem(id);
						}
						directionBox.get(l).setSelectedIndex(1);
						convBox.get(l).setSelectedIndex(0);
						dimensionsField.get(l).setText(SBMLutilities.getDimensionString(replacement));
						portIndicesField.get(l).setText(SBMLutilities.getIndicesString(replacement,"comp:portRef"));
						subModelIndicesField.get(l).setText(SBMLutilities.getIndicesString(replacement,"comp:submodelRef"));
						replDel[l] = idRefs.get(l) + " port replaces " + id;
					}
				} 
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
	
	private boolean removePortMaps(CompSBasePlugin sbmlSBase) {
		boolean result = false;
		for (int j = sbmlSBase.getListOfReplacedElements().size(); j > 0; j--) {
			ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(j - 1);
			if (replacement.getSubmodelRef().equals(subModelId) && 
					(replacement.isSetPortRef()|| replacement.isSetDeletion())) { 
				//sbmlSBase.getListOfReplacedElements().remove(j - 1);
				sbmlSBase.removeReplacedElement(replacement);
				result = true;
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId) && (replacement.isSetPortRef())) {
				sbmlSBase.unsetReplacedBy();
				result = true;
			}
		}
		return result;
	}

	public boolean openGui(String oldName) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Module Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			
			if (!checkValues()) {
				Utility.createErrorMessage("Error", "Illegal values entered.");
				return false;
			}
			String[] dimensions = new String[]{""};
			String[] dimensionIds = new String[]{""};
			dimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
					fields.get(GlobalConstants.ID).getValue(), false);
			if(dimensions==null)return false;
			dimensionIds = SBMLutilities.getDimensionIds("",dimensions.length-1);
			String id = dimensions[0];
			if (oldName == null) {
				if (bioModel.isSIdInUse(id)) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			else if (!oldName.equals(id)) {
				if (bioModel.isSIdInUse(id)) {
					Utility.createErrorMessage("Error", "Id already exists.");
					return false;
				}
			}
			for (int i = 0; i < portIds.size(); i++) {
				String[] topDimensions = new String[]{""};
				String[] topDimensionIds = new String[]{""};
				String[] portDimensions = new String[]{""};
				String[] portIndices = new String[]{""};
				String[] subModelIndices = new String[]{""};
				String[] portDimensionIds = new String[]{""};
				String portmapId = (String)portmapBox.get(i).getSelectedItem();
				if (!portmapId.equals("--none--")&&!portmapId.equals("--include--")) {
					String portId = portIds.get(i);
					portDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), dimensionsField.get(i).getText(), true);
					if(portDimensions!=null){
						portDimensionIds = SBMLutilities.getDimensionIds("r",portDimensions.length-1);
					} else {
						return false;
					}
					SBase variable = subBioModel.getSBMLCompModel().getPort(portId);
					if (portmapId.equals("--delete--")) {
						topDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
								fields.get(GlobalConstants.ID).getValue(), false);
						if(topDimensions!=null) {
							topDimensionIds = SBMLutilities.getDimensionIds("",topDimensions.length-1);
						}
					} else {
						topDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
								SBMLutilities.getArrayId(bioModel.getSBMLDocument(), portmapId), false);
						if(topDimensions!=null) {
							topDimensionIds = SBMLutilities.getDimensionIds("",topDimensions.length-1);
						}
					}
					portIndices = SBMLutilities.checkIndices(portIndicesField.get(i).getText(), variable, bioModel.getSBMLDocument(), portDimensionIds, "comp:portRef", 
							portDimensions, topDimensionIds, topDimensions);
					if (portIndices==null) return false;
					if (!portmapId.equals("--delete--")) {
						variable = bioModel.getSBMLCompModel().getSubmodel(subModelId);
						variable = new Submodel();
						ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(variable);
						sBasePlugin.unsetListOfDimensions();
						for(int j = 0; j<dimensions.length-1; j++){
							org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[j]);
							dimX.setSize(dimensions[j+1].replace("]", "").trim());
							dimX.setArrayDimension(j);
						}
						subModelIndices = SBMLutilities.checkIndices(subModelIndicesField.get(i).getText(), variable, bioModel.getSBMLDocument(), portDimensionIds, "comp:submodelRef", 
								portDimensions, topDimensionIds, topDimensions);
						if (subModelIndices==null) return false;
					}
				}
			}						
			Submodel instance = bioModel.getSBMLCompModel().getListOfSubmodels().get(subModelId);
			if (instance != null) {
				instance.setName(fields.get(GlobalConstants.NAME).getValue());
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(instance);
				sBasePlugin.unsetListOfDimensions();
				for(int i = 0; i<dimensions.length-1; i++){
					org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[i]);
					dimX.setSize(dimensions[i+1].replace("]", "").trim());
					dimX.setArrayDimension(i);
				}
				while (instance.getListOfDeletions().size()>0) {
					Deletion deletion = instance.getListOfDeletions().get(0);
					instance.removeDeletion(deletion);
				}
				if (timeConvFactorBox.getSelectedItem().equals("(none)")) {
					instance.unsetTimeConversionFactor();
				} else {
					instance.setTimeConversionFactor((String)timeConvFactorBox.getSelectedItem());
				}
				if (extentConvFactorBox.getSelectedItem().equals("(none)")) {
					instance.unsetExtentConversionFactor();
				} else {
					instance.setExtentConversionFactor((String)extentConvFactorBox.getSelectedItem());
				}
			} else {
				Utility.createErrorMessage("Error", "Submodel is missing.");
				return false;
			}
			ArrayList<SBase> elements = SBMLutilities.getListOfAllElements(bioModel.getSBMLDocument().getModel());
			for (int j = 0; j < elements.size(); j++) {
				SBase sbase = elements.get(j);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbase.getExtension(CompConstants.namespaceURI);
				if (sbmlSBase!=null) {
					if (removePortMaps(sbmlSBase)) {
						elements = SBMLutilities.getListOfAllElements(bioModel.getSBMLDocument().getModel());
					}
				}
			}
			for (int i = 0; i < portIds.size(); i++) {
				String subId = id;
				if (subModelId.startsWith("GRID__")) subId = subModelId;
				String portId = portIds.get(i);
				//String type = types.get(i);
				String portmapId = (String)portmapBox.get(i).getSelectedItem();
				String[] topDimensions = new String[]{""};
				String[] topDimensionIds = new String[]{""};
				String[] portDimensions = new String[]{""};
				String[] portIndices = new String[]{""};
				String[] subModelIndices = new String[]{""};
				String[] portDimensionIds = new String[]{""};
				if (!portmapId.equals("--none--")&&!portmapId.equals("--include--")) {
					portDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), dimensionsField.get(i).getText(), true);
					if(portDimensions!=null){
						portDimensionIds = SBMLutilities.getDimensionIds("r",portDimensions.length-1);
					}
					SBase variable = subBioModel.getSBMLCompModel().getPort(portId);
					if (portmapId.equals("--delete--")) {
						topDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
								fields.get(GlobalConstants.ID).getValue(), false);
						if(topDimensions!=null) {
							topDimensionIds = SBMLutilities.getDimensionIds("",topDimensions.length-1);
						}
					} else {
						topDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
								SBMLutilities.getArrayId(bioModel.getSBMLDocument(), portmapId), false);
						if(topDimensions!=null) {
							topDimensionIds = SBMLutilities.getDimensionIds("",topDimensions.length-1);
						}
					}
					portIndices = SBMLutilities.checkIndices(portIndicesField.get(i).getText(), variable, bioModel.getSBMLDocument(), portDimensionIds, "comp:portRef", 
							portDimensions, topDimensionIds, topDimensions);
					if (!portmapId.equals("--delete--")) {
						variable = bioModel.getSBMLCompModel().getSubmodel(subModelId);
						subModelIndices = SBMLutilities.checkIndices(subModelIndicesField.get(i).getText(), variable, bioModel.getSBMLDocument(), portDimensionIds, "comp:submodelRef", 
								portDimensions, topDimensionIds, topDimensions);
					}
				}
				if (!portmapId.equals("--none--")&&!portmapId.equals("--delete--")&&!portmapId.equals("--include--")) {
					SBase sbase = SBMLutilities.getElementBySId(bioModel.getSBMLDocument().getModel(), portmapId);
					CompSBasePlugin sbmlSBase = null;
					if (sbase!=null) {
						sbmlSBase = SBMLutilities.getCompSBasePlugin(sbase);
						if (sbmlSBase != null) {
							if (directionBox.get(i).getSelectedIndex()==0) {
								boolean deletion = false;
								if (sbase instanceof Event) {
									deletion = true;
								}
								Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(subId);
								SBMLutilities.addReplacement(sbase, submodel, subId, portId, (String)convBox.get(i).getSelectedItem(),
										portDimensions, portDimensionIds, portIndices, subModelIndices, deletion);
								String subSpeciesId = portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "");
								SBMLutilities.addImplicitDeletions(submodel, subBioModel, subSpeciesId, portDimensions, 
										portDimensionIds,portIndices);
							} else {
								boolean skip = false;
								if (sbmlSBase.isSetReplacedBy()) {
									ReplacedBy replacement = sbmlSBase.getReplacedBy();
									if (!replacement.getSubmodelRef().equals(subId) ||
											!replacement.getPortRef().equals(portId)) {	
										Utility.createErrorMessage("Error", portmapId + " is already replaced by " +
											replacement.getPortRef().replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "") + 
											" from subModel " + replacement.getSubmodelRef() + "\nCannot also replace with " + 
											portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "") + 
											" from subModel " + subId);
										skip = true;
									}
								}
								if (!skip) {
									SBMLutilities.addReplacedBy(sbase, subId, portId, portDimensions, portDimensionIds, portIndices, subModelIndices);
									String subSpeciesId = portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "");
									SBMLutilities.addImplicitReplacedBys(subId, bioModel, subBioModel, SBMLutilities.getId(sbase), subSpeciesId, 
											portDimensions, portDimensionIds, portIndices, subModelIndices);
								}
							}
						}
					} else {
						sbase = SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument().getModel(), portmapId);
						sbmlSBase = SBMLutilities.getCompSBasePlugin(sbase);
						if (sbmlSBase != null) {
							if (directionBox.get(i).getSelectedIndex()==0) {
								Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(subId);
								SBMLutilities.addReplacement(sbase, submodel, subId, portId, (String)convBox.get(i).getSelectedItem(),
										portDimensions, portDimensionIds, portIndices, subModelIndices, true);
							} else {
								boolean skip = false;
								if (sbmlSBase.isSetReplacedBy()) {
									ReplacedBy replacement = sbmlSBase.getReplacedBy();
									if (!replacement.getSubmodelRef().equals(subId) ||
											!replacement.getPortRef().equals(portId)) {	
										Utility.createErrorMessage("Error", portmapId + " is already replaced by " +
											replacement.getPortRef().replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "") + 
											" from subModel " + replacement.getSubmodelRef() + "\nCannot also replace with " + 
											portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "") + 
											" from subModel " + subId);
										skip = true;
									}
								}
								if (!skip) {
									SBMLutilities.addReplacedBy(sbase, subId, portId, portDimensions, portDimensionIds, portIndices, subModelIndices);
								}
							}
						}
					}
				} else if (portmapId.equals("--delete--")) {
					Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(subId);
					SBMLutilities.addDeletion(submodel, portId, portDimensions, portDimensionIds,portIndices);
					String subSpeciesId = portId.replace(GlobalConstants.INPUT+"__", "").replace(GlobalConstants.OUTPUT+"__", "");
					SBMLutilities.addImplicitDeletions(submodel, subBioModel, subSpeciesId, portDimensions, 
							portDimensionIds,portIndices);
				}
			}
			if (selected != null && oldName != null && !oldName.equals(id)) {
				bioModel.changeComponentName(oldName, id);
			}
			String newPort = bioModel.getComponentPortMap(id);
			componentsList.removeItem(oldName + " " + selectedComponent.replace(".xml", "") + " " + oldPort);
			componentsList.addItem(id + " " + selectedComponent.replace(".xml", "") + " " + newPort);
			componentsList.setSelectedValue(id + " " + selectedComponent.replace(".xml", "") + " " + newPort, true);
			if (!paramsOnly) {
				// Add SBOL annotation to submodel
				if (sbolField.getSBOLURIs().size() > 0 || 
						sbolField.getSBOLStrand().equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND)) {
					if (!instance.isSetMetaId() || instance.getMetaId().equals(""))
							SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), instance, 
									bioModel.getMetaIDIndex());
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(instance.getMetaId(), sbolField.getSBOLURIs(),
							sbolField.getSBOLStrand());
					AnnotationUtility.setSBOLAnnotation(instance, sbolAnnot);

				} else
					AnnotationUtility.removeSBOLAnnotation(instance);
			}
			modelEditor.setDirty(true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}
	
	private void replDelEditor(String option,String selected) {
		JPanel replDelPanel = new JPanel(new GridLayout(8,2));
		int selectedIndex = -1;
		int originalDir = -1;
		int originalRepl = -1;
		int originalConv = -1;
		String originalDim = "";
		String originalPortIndices = "";
		String originalSubModelIndices = "";
		if (option.equals("OK")) {
			selectedIndex = idRefs.indexOf(selected);
			
			JLabel dimensionsLabel = new JLabel("Dimensions");
			replDelPanel.add(dimensionsLabel);
			replDelPanel.add(dimensionsField.get(selectedIndex));
			originalDim = dimensionsField.get(selectedIndex).getText();

			JLabel typeLabel = new JLabel("Type");
			replDelPanel.add(typeLabel);
			JLabel tempLabel2 = new JLabel(types.get(selectedIndex));
			replDelPanel.add(tempLabel2);
			
			JLabel portLabel = new JLabel("Port");
			replDelPanel.add(portLabel);
			JLabel tempLabel = new JLabel(idRefs.get(selectedIndex));
			replDelPanel.add(tempLabel);
			
			JLabel portIndexLabel = new JLabel("Port indices");
			replDelPanel.add(portIndexLabel);
			replDelPanel.add(portIndicesField.get(selectedIndex));
			originalPortIndices = portIndicesField.get(selectedIndex).getText();
			
			JLabel subModelIndexLabel = new JLabel("SubModel indices");
			replDelPanel.add(subModelIndexLabel);
			replDelPanel.add(subModelIndicesField.get(selectedIndex));
			originalSubModelIndices = subModelIndicesField.get(selectedIndex).getText();

			JLabel dirLabel = new JLabel("Direction");
			replDelPanel.add(dirLabel);
			replDelPanel.add(directionBox.get(selectedIndex));
			originalDir = directionBox.get(selectedIndex).getSelectedIndex();

			JLabel replLabel = new JLabel("Replacement");
			replDelPanel.add(replLabel);
			replDelPanel.add(portmapBox.get(selectedIndex));
			originalRepl = portmapBox.get(selectedIndex).getSelectedIndex();

			JLabel convLabel = new JLabel("Conversion");
			replDelPanel.add(convLabel);
			replDelPanel.add(convBox.get(selectedIndex));
			originalConv = convBox.get(selectedIndex).getSelectedIndex();
			directionBox.get(selectedIndex).addActionListener(this);
			portmapBox.get(selectedIndex).addActionListener(this);
			updateComboBoxEnabling();
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, replDelPanel, "Port Map Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String portId = portIds.get(selectedIndex);
			String[] topDimensions = new String[]{""};
			String[] topDimensionIds = new String[]{""};
			String[] portDimensions = new String[]{""};
			String[] portIndices = new String[]{""};
			String[] subModelIndices = new String[]{""};
			String[] portDimensionIds = new String[]{""};
			String portmapId = (String)portmapBox.get(selectedIndex).getSelectedItem();
			if (!portmapId.equals("--none--")&&!portmapId.equals("--include--")) {
				portDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), dimensionsField.get(selectedIndex).getText(), true);
				if(portDimensions!=null){
					portDimensionIds = SBMLutilities.getDimensionIds("r",portDimensions.length-1);
				}else{
					error = true;
				}
				if (portmapId.equals("--delete--")) {
					topDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
							fields.get(GlobalConstants.ID).getValue(), false);
					if(topDimensions!=null) {
						topDimensionIds = SBMLutilities.getDimensionIds("",topDimensions.length-1);
					}
				} else {
					topDimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
							SBMLutilities.getArrayId(bioModel.getSBMLDocument(), portmapId), false);
					if(topDimensions!=null) {
						topDimensionIds = SBMLutilities.getDimensionIds("",topDimensions.length-1);
					}
				}
				if (!error) {
					SBase variable = subBioModel.getSBMLCompModel().getPort(portId);
					if (variable==null) {
						error = true;
					} else {
						portIndices = SBMLutilities.checkIndices(portIndicesField.get(selectedIndex).getText(), variable, bioModel.getSBMLDocument(), portDimensionIds, "comp:portRef", 
								portDimensions, topDimensionIds, topDimensions);
						error = (portIndices==null);
					}
				}
				if (!error) {
					if (!portmapId.equals("--delete--")) {
						SBase variable = new Submodel();
						String[] dimensions = new String[]{""};
						String[] dimensionIds = new String[]{""};
						dimensions = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), 
								fields.get(GlobalConstants.ID).getValue(), false);
						if(dimensions!=null && dimensions.length>0) {
							dimensionIds = SBMLutilities.getDimensionIds("",dimensions.length-1);
							ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(variable);
							sBasePlugin.unsetListOfDimensions();
							for(int i = 0; i<dimensions.length-1; i++){
								org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[i]);
								dimX.setSize(dimensions[i+1].replace("]", "").trim());
								dimX.setArrayDimension(i);
							}
						}
						subModelIndices = SBMLutilities.checkIndices(subModelIndicesField.get(selectedIndex).getText(), variable, bioModel.getSBMLDocument(), portDimensionIds, "comp:submodelRef", 
								portDimensions, topDimensionIds, topDimensions);
						error = (subModelIndices==null);
					}
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, replDelPanel, "Port Map Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
			if (!error) {
				if (portmapBox.get(selectedIndex).getSelectedItem().equals("--none--")) {
					replDel[selectedIndex] = idRefs.get(selectedIndex) + " port is unchanged"; 
				} else if (portmapBox.get(selectedIndex).getSelectedItem().equals("--delete--")) {
					replDel[selectedIndex] = idRefs.get(selectedIndex) + " port is deleted"; 
				} else if (directionBox.get(selectedIndex).getSelectedItem().equals("<--")) {
					replDel[selectedIndex] = idRefs.get(selectedIndex) + " port is replaced by " + 
							portmapBox.get(selectedIndex).getSelectedItem(); 
				} else {
					replDel[selectedIndex] = idRefs.get(selectedIndex) + " port replaces " + 
							portmapBox.get(selectedIndex).getSelectedItem(); 
				}
				//main.util.Utility.sort(replDel);
				replacementsDeletions.setListData(replDel);
				replacementsDeletions.setSelectedIndex(selectedIndex);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			directionBox.get(selectedIndex).setSelectedIndex(originalDir);
			portmapBox.get(selectedIndex).setSelectedIndex(originalRepl);
			convBox.get(selectedIndex).setSelectedIndex(originalConv);
			dimensionsField.get(selectedIndex).setText(originalDim);
			portIndicesField.get(selectedIndex).setText(originalPortIndices);
			subModelIndicesField.get(selectedIndex).setText(originalSubModelIndices);
			return;
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("comboBoxChanged")) {
			updateComboBoxEnabling();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == replacementsDeletions) {
				String selected = ((String)replacementsDeletions.getSelectedValue()).split(" ")[0]; 
				replDelEditor("OK",selected);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}
