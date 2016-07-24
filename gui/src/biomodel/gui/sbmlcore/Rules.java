package biomodel.gui.sbmlcore;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

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
import javax.swing.SpringLayout;

import main.Gui;
import main.util.SpringUtilities;
import main.util.Utility;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Index;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
//import biomodel.gui.sbol.SBOLField;
import biomodel.gui.sbol.SBOLField2;
import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;


/**
 * This is a class for creating SBML rules
 * 
 * @author Chris Myers
 * 
 */
public class Rules extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JButton addRule, removeRule, editRule;

	private JList rules; // JList of initial assignments

	private BioModel bioModel;

	private JComboBox ruleType, ruleVar;
	
	private JTextField iIndex;
	
	private ModelEditor modelEditor;
	
	private SBOLField2 sbolField;
	
	private JTextField ruleMath;
	
	private JComboBox SBOTerms;
	
	/* Create rule panel */
	public Rules(BioModel gcm, ModelEditor modelEditor) {
		super(new BorderLayout());
		this.bioModel = gcm;
		this.modelEditor = modelEditor;

		/* Create rule panel */
		Model model = gcm.getSBMLDocument().getModel();
		addRule = new JButton("Add Rule");
		removeRule = new JButton("Remove Rule");
		editRule = new JButton("Edit Rule");
		rules = new JList();
		String[] rul = new String[model.getRuleCount()];
		for (int i = 0; i < model.getRuleCount(); i++) {
			Rule rule = model.getRule(i);
			if (!rule.isSetMetaId()) {
				String ruleId = "rule0";
				int cn = 0;
				while (SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument(), ruleId)!=null) {
					cn++;
					ruleId = "rule" + cn;
				}
				rule.setMetaId(ruleId);
			}
			rul[i] = rule.getMetaId() + SBMLutilities.getDimensionString(rule);
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(rule);
			rul[i] += " : ";
			if (rule.isAlgebraic()) {
				rul[i] += "0 = " + bioModel.removeBooleans(rule.getMath());
			}
			else if (rule.isAssignment()) {
				String index = "";
				for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
					Index indie = sBasePlugin.getIndex(j,"variable");
					if (indie!=null)
						index += "[" + SBMLutilities.myFormulaToString(indie.getMath()) + "]";
				}
				rul[i] += SBMLutilities.getVariable(rule) + index + " = " + bioModel.removeBooleans(rule.getMath());
			}
			else {
				String index = "";
				for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
					Index indie = sBasePlugin.getIndex(j,"variable");
					if (indie!=null) 
						index += "[" + SBMLutilities.myFormulaToString(indie.getMath()) + "]";
				}
				rul[i] += "d( " + SBMLutilities.getVariable(rule) + index + " )/dt = " + bioModel.removeBooleans(rule.getMath());
			}
		}
		String[] oldRul = rul;
		try {
			rul = sortRules(rul);
		}
		catch (Exception e) {
			// cycle = true;
			JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			rul = oldRul;
		}
		/*
		 * if (!cycle && SBMLutilities.checkCycles(gcm.getSBMLDocument())) {
		 * JOptionPane.showMessageDialog(Gui.frame,
		 * "Cycle detected within initial assignments, assignment rules, and rate laws."
		 * , "Cycle Detected", JOptionPane.ERROR_MESSAGE); }
		 */
		JPanel addRem = new JPanel();
		addRem.add(addRule);
		addRem.add(removeRule);
		addRem.add(editRule);
		addRule.addActionListener(this);
		removeRule.addActionListener(this);
		editRule.addActionListener(this);
		JLabel panelLabel = new JLabel("List of Rules:");
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(rules);
		Utility.sort(rul);
		rules.setListData(rul);
		rules.setSelectedIndex(0);
		rules.addMouseListener(this);
		this.add(panelLabel, "North");
		this.add(scroll, "Center");
		this.add(addRem, "South");
	}

	/**
	 * Refresh rules panel
	 */
	public void refreshRulesPanel() {
		Model model = bioModel.getSBMLDocument().getModel();
		if (model.getRuleCount() > 0) {
			String[] rul = new String[model.getRuleCount()];
			for (int i = 0; i < model.getRuleCount(); i++) {
				Rule rule = model.getListOfRules().get(i);
				rul[i] = rule.getMetaId() + SBMLutilities.getDimensionString(rule);
				rul[i] += " : ";
				ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(rule);
				if (rule.isAlgebraic()) {
					rul[i] = "0 = " + bioModel.removeBooleans(rule.getMath());
				}
				else if (rule.isAssignment()) {
					String index = "";
					for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
						Index indie = sBasePlugin.getIndex(j,"variable");
						if (indie!=null)
							index += "[" + SBMLutilities.myFormulaToString(indie.getMath()) + "]";
					}
					rul[i] = SBMLutilities.getVariable(rule) + index + " = " + bioModel.removeBooleans(rule.getMath());
				}
				else {
					String index = "";
					for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
						Index indie = sBasePlugin.getIndex(j,"variable");
						if (indie!=null)
							index += "[" + SBMLutilities.myFormulaToString(indie.getMath()) + "]";
					}
					rul[i] = "d( " + SBMLutilities.getVariable(rule) + index + " )/dt = " + bioModel.removeBooleans(rule.getMath());
				}
			}
			try {
				rul = sortRules(rul);
				if (SBMLutilities.checkCycles(bioModel.getSBMLDocument())) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
							"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			}
			rules.setListData(rul);
			rules.setSelectedIndex(0);
		}
	}

	/**
	 * Creates a frame used to edit rules or create new ones.
	 */
	public String ruleEditor(String option,String metaId) {
		JPanel rulePanel = new JPanel(new BorderLayout());
		JPanel firstLine = new JPanel();
//		JPanel secondLine = new JPanel();
//		JPanel thirdLine = new JPanel();
		JPanel topPanel = new JPanel(new GridLayout(1,1));
		JPanel mathPanel = new JPanel();
		JPanel SBOLPanel = new JPanel(new BorderLayout());
		JLabel IDLabel = new JLabel("ID:");
		JLabel typeLabel = new JLabel("Type:");
		JLabel varLabel = new JLabel("Variable:");
		JLabel ruleLabel = new JLabel("Rule:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		JLabel sboTermLabel = new JLabel(GlobalConstants.SBOTERM);
		SBOTerms = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_MATHEMATICAL_EXPRESSION));
		String[] list = { "Algebraic", "Assignment", "Rate" };
		ruleType = new JComboBox(list);
		ruleType.setSelectedItem("Assignment");
		ruleVar = new JComboBox();
		assignRuleVar("");
		JTextField id = new JTextField(12);
		ruleMath = new JTextField(85);
		ruleVar.setEnabled(true);
		ruleVar.addActionListener(this);
		JCheckBox onPort = new JCheckBox();
		iIndex = new JTextField(15);
		iIndex.setEnabled(true);
		
		if (option.equals("OK")) {
			ruleType.setEnabled(false);
			Rule rule = (Rule)SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument().getModel(), metaId);			
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(rule);
			String dimInID = SBMLutilities.getDimensionString(rule);
			if (rule.getElementName().equals(GlobalConstants.ALGEBRAIC_RULE)) {
				ruleType.setSelectedItem("Algebraic");
				ruleVar.setEnabled(false);
				ruleMath.setText(bioModel.removeBooleans(rule.getMath()));
			}
			else if  (rule.getElementName().equals(GlobalConstants.RATE_RULE)) {
				ruleType.setSelectedItem("Rate");
				rateRuleVar(SBMLutilities.getVariable(rule));
				ruleVar.setEnabled(true);
				ruleVar.setSelectedItem(SBMLutilities.getVariable(rule));
				ruleMath.setText(bioModel.removeBooleans(rule.getMath()));
			}
			else {
				ruleType.setSelectedItem("Assignment");
				assignRuleVar(SBMLutilities.getVariable(rule));
				ruleVar.setEnabled(true);
				ruleVar.setSelectedItem(SBMLutilities.getVariable(rule));
				if (bioModel.getSBMLDocument().getModel().getParameter(SBMLutilities.getVariable(rule))!=null &&
						SBMLutilities.isBoolean(bioModel.getSBMLDocument().getModel().getParameter(SBMLutilities.getVariable(rule)))) {
					ruleMath.setText(bioModel.removeBooleanAssign(rule.getMath()));
				} else {
					ruleMath.setText(bioModel.removeBooleans(rule.getMath()));
				}
			}
			if (!modelEditor.isParamsOnly()) {
				//Parse out SBOL annotations and add to SBOL field
				List<URI> sbolURIs = new LinkedList<URI>();
				String sbolStrand = AnnotationUtility.parseSBOLAnnotation(rule, sbolURIs);
				// Field for annotating rules with SBOL DNA components
				sbolField = new SBOLField2(sbolURIs, sbolStrand, GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 
						2, false);
			}
			if (rule.isSetMetaId()) {
				id.setText(rule.getMetaId() + dimInID);
			} else {
				String ruleId = "rule0";
				int cn = 0;
				while (SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument(), ruleId)!=null) {
					cn++;
					ruleId = "rule" + cn;
				}
				id.setText(ruleId + dimInID);
			}
			if (bioModel.getPortByMetaIdRef(rule.getMetaId())!=null) {
				onPort.setSelected(true);
			} else {
				onPort.setSelected(false);
			}
			if (rule.isSetSBOTerm()) {
				SBOTerms.setSelectedItem(SBMLutilities.sbo.getName(rule.getSBOTermID()));
			}
			String freshIndex = "";
			for(int i = sBasePlugin.getIndexCount()-1; i>=0; i--){
				Index indie = sBasePlugin.getIndex(i,"variable");
				if (indie!=null)
					freshIndex += "[" + SBMLutilities.myFormulaToString(indie.getMath()) + "]";
			}
			iIndex.setText(freshIndex);
		}
		else {
			// Field for annotating rules with SBOL DNA components
			sbolField = new SBOLField2(new LinkedList<URI>(), GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND, 
					GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 2, false);
			if (!assignRuleVar("") && !rateRuleVar("")) {
				ruleType.removeItem("Assignment");
				ruleType.removeItem("Rate");
			}
			else if (!assignRuleVar("")) {
				ruleType.removeItem("Assignment");
			}
			else if (!rateRuleVar("")) {
				ruleType.removeItem("Rate");
			}
			String ruleId = "rule0";
			int cn = 0;
			while (SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument(), ruleId)!=null) {
				cn++;
				ruleId = "rule" + cn;
			}
			id.setText(ruleId);
		}
		ruleType.addActionListener(this);
		firstLine.add(IDLabel);
		firstLine.add(id);
		firstLine.add(typeLabel);
		firstLine.add(ruleType);
		firstLine.add(onPortLabel);
		firstLine.add(onPort);
		firstLine.add(varLabel);
		firstLine.add(ruleVar);
		firstLine.add(new JLabel("Indices:"));
		firstLine.add(iIndex);
		topPanel.add(firstLine);
		//topPanel.add(thirdLine);
		mathPanel.add(ruleLabel);
		mathPanel.add(ruleMath);
		rulePanel.add(topPanel,"North");
		rulePanel.add(mathPanel,"Center");
		if (!modelEditor.isParamsOnly()) {
			JPanel sboField = new JPanel(new SpringLayout());
			sboField.add(sboTermLabel);
			sboField.add(SBOTerms);
			SpringUtilities.makeCompactGrid(sboField, 1, 2, 6, 6, 6, 6);
			SBOLPanel.add(sboField,"North");
			SBOLPanel.add(sbolField,"South");
		}
		rulePanel.add(SBOLPanel, "South");
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, rulePanel, "Rule Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, options[0]);
		String[] dimID = new String[]{""};
		String[] dex = new String[]{""};
		String[] dimensionIds = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			dimID = SBMLutilities.checkSizeParameters(bioModel.getSBMLDocument(), id.getText(), false);
			if(dimID!=null){
				dimensionIds = SBMLutilities.getDimensionIds("",dimID.length-1);
				error = SBMLutilities.checkID(bioModel.getSBMLDocument(), dimID[0].trim(), metaId, false);
			}
			else{
				error = true;
			}
			String addVar = "";
			addVar = (String) ruleVar.getSelectedItem();
			if(ruleVar.isEnabled() && !error && !ruleType.getSelectedItem().equals("Algebraic")){
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)ruleVar.getSelectedItem());
				dex = SBMLutilities.checkIndices(iIndex.getText(), variable, bioModel.getSBMLDocument(), dimensionIds, "variable", dimID, null, null);
				error = (dex==null);
			}
			if (!error) {
				if (ruleMath.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(Gui.frame, "Rule must have formula.", "Enter Rule Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else if (SBMLutilities.myParseFormula(ruleMath.getText().trim()) == null) {
					JOptionPane.showMessageDialog(Gui.frame, "Rule formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				else {
					error = SBMLutilities.displayinvalidVariables("Rule", bioModel.getSBMLDocument(), dimensionIds, ruleMath.getText().trim(), "", false);
					if (!error) {
						error = SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), SBMLutilities.myParseFormula(ruleMath.getText().trim()));
					}
					if (!error) {
						error = SBMLutilities.checkFunctionArgumentTypes(bioModel.getSBMLDocument(), bioModel.addBooleans(ruleMath.getText().trim()));
					}
					if (!error) {
						if (bioModel.getSBMLDocument().getModel().getParameter(addVar)!=null &&
								SBMLutilities.isBoolean(bioModel.getSBMLDocument().getModel().getParameter(addVar))) {
							if (!SBMLutilities.returnsBoolean(bioModel.addBooleans(ruleMath.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Rule must evaluate to a Boolean.", "Boolean Expected", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						} else {
							if (SBMLutilities.returnsBoolean(bioModel.addBooleans(ruleMath.getText().trim()), bioModel.getSBMLDocument().getModel())) {
								JOptionPane.showMessageDialog(Gui.frame, "Rule must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
								error = true;
							}
						}
					}
				}
			}
			if (!error) {
				Rule r = (Rule) (SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument().getModel(), metaId));
				if (option.equals("OK")) {
					String[] rul = new String[rules.getModel().getSize()];
					for (int i = 0; i < rules.getModel().getSize(); i++) {
						rul[i] = rules.getModel().getElementAt(i).toString();
					}
					int index = rules.getSelectedIndex();
					rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					rul = Utility.getList(rul, rules);
					String[] oldRul = rul.clone();
					rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					String addStr;
					String oldVar = "";
					String oldMath = bioModel.removeBooleans(r.getMath());
					if (ruleType.getSelectedItem().equals("Algebraic")) {
						r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
						addStr = "0 = " + bioModel.removeBooleans(r.getMath());
						error = !SBMLutilities.check("",bioModel.getSBMLDocument(),true);
					}
					else if (ruleType.getSelectedItem().equals("Rate")) {
						oldVar = SBMLutilities.getVariable(r);
						SBMLutilities.setVariable(r, addVar);
						r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
						error = checkRateRuleUnits(r);
						addStr = "d( " + addVar + iIndex.getText() + " )/dt = " + bioModel.removeBooleans(r.getMath());
					}
					else {
						oldVar = SBMLutilities.getVariable(r);
						SBMLutilities.setVariable(r, addVar);
						if (bioModel.getSBMLDocument().getModel().getParameter(addVar)!=null &&
								SBMLutilities.isBoolean(bioModel.getSBMLDocument().getModel().getParameter(addVar))) {
							r.setMath(bioModel.addBooleanAssign(ruleMath.getText().trim()));
						} else {
							r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
						}
						error = checkAssignmentRuleUnits(r);
						addStr = addVar + iIndex.getText() + " = " + bioModel.removeBooleans(r.getMath());
					}
					if(!error){
						error = (dimID==null || dex==null);
					}
					if (dimID!=null) {
						rul[index] = dimID[0].trim(); 
						if (dimID.length>1) {
							for (int i = 1; i < dimID.length; i++) {
								rul[index] += "[" + dimID[i] + "]";
							}
						}
						rul[index] += " : ";
					}
					rul[index] += addStr;
					if (!error) {
						try {
							rul = sortRules(rul);
						}
						catch (Exception e) {
							JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
							error = true;
						}
					}
					if (!error && SBMLutilities.checkCycles(bioModel.getSBMLDocument())) {
						JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
					if (!error && !modelEditor.isParamsOnly()) {
						if (dimID!=null) {
							SBMLutilities.setMetaId(r, dimID[0].trim());
						}
						Port port = bioModel.getPortByMetaIdRef(r.getMetaId());
						ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
						sBasePlugin.unsetListOfDimensions();
						for(int i = 0; dimID!=null && i<dimID.length-1; i++){
							org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[i]);
							dimX.setSize(dimID[i+1]);
							dimX.setArrayDimension(i);
						}
						sBasePlugin.unsetListOfIndices();
						for(int i = 0; dex!=null && i<dex.length-1; i++){
							Index indexRule = new Index();
							indexRule.setArrayDimension(i);
							indexRule.setReferencedAttribute("variable");
							ASTNode indexMath = SBMLutilities.myParseFormula(dex[i+1]);
							indexRule.setMath(indexMath);
							sBasePlugin.addIndex(indexRule);
						}
						if (port!=null) {
							if (onPort.isSelected()) {
								port.setId(GlobalConstants.RULE+"__"+r.getMetaId());
								port.setMetaIdRef(r.getMetaId());
								ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
								sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());	
								sBasePluginPort.unsetListOfIndices();
								for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
									org.sbml.jsbml.ext.arrays.Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
									Index portIndex = sBasePluginPort.createIndex();
									portIndex.setReferencedAttribute("comp:metaIdRef");
									portIndex.setArrayDimension(i);
									portIndex.setMath(SBMLutilities.myParseFormula(dim.getId()));
								}
							} else {
								bioModel.getSBMLCompModel().removePort(port);
							}
						} else {
							if (onPort.isSelected()) {
								port = bioModel.getSBMLCompModel().createPort();
								port.setId(GlobalConstants.RULE+"__"+r.getMetaId());
								port.setMetaIdRef(r.getMetaId());
								ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
								sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());
								sBasePluginPort.unsetListOfIndices();
								for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
									org.sbml.jsbml.ext.arrays.Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
									Index portIndex = sBasePluginPort.createIndex();
									portIndex.setReferencedAttribute("comp:metaIdRef");
									portIndex.setArrayDimension(i);
									portIndex.setMath(SBMLutilities.myParseFormula(dim.getId()));
								}
							}
						}
					}
					if (error) {
						if (!oldVar.equals("")) {
							SBMLutilities.setVariable(r, oldVar);
						}
						r.setMath(bioModel.addBooleans(oldMath));
						rul = oldRul;
//						rul[index] = oldVal;
					}
//					updateRules(rul);
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						r.unsetSBOTerm();
					} else {
						r.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					rules.setListData(rul);
					rules.setSelectedIndex(index);
					bioModel.makeUndoPoint();
				}
				else {
					String[] rul = new String[rules.getModel().getSize()];
					for (int i = 0; i < rules.getModel().getSize(); i++) {
						rul[i] = rules.getModel().getElementAt(i).toString();
					}
					JList add = new JList();
					int index = rules.getSelectedIndex();
					String addStr = "";
					if (dimID!=null) {
						addStr = dimID[0].trim(); 
						if (dimID.length>1) {
							for (int i = 1; i < dimID.length; i++) {
								addStr += "[" + dimID[i] + "]";
							}
						}
						addStr += " : ";
					}
					if (ruleType.getSelectedItem().equals("Algebraic")) {
						addStr += "0 = " + SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(ruleMath.getText().trim()));
					}
					else if (ruleType.getSelectedItem().equals("Rate")) {
						addStr += "d( " + addVar + iIndex.getText() + " )/dt = "
								+ SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(ruleMath.getText().trim()));
					}
					else {
						addStr += addVar + iIndex.getText() + " = " + SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(ruleMath.getText().trim()));
					}
					Object[] adding = { addStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(rul, rules, add);
					String[] oldRul = rul;
					rul = new String[adding.length];
					for (int i = 0; i < adding.length; i++) {
						rul[i] = (String) adding[i];
					}
					try {
						rul = sortRules(rul);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
//						rul = oldRul;
					}
					if (!error) {
						String ruleId = "";
						if (dimID!=null) ruleId = dimID[0].trim();
						SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
						if (ruleType.getSelectedItem().equals("Algebraic")) {
							r = sbmlDoc.getModel().createAlgebraicRule();
							SBMLutilities.setMetaId(r, ruleId);
							r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
							error = !SBMLutilities.check("",bioModel.getSBMLDocument(),true);
						}
						else if (ruleType.getSelectedItem().equals("Rate")) {
							r = sbmlDoc.getModel().createRateRule();
							SBMLutilities.setMetaId(r, ruleId);
							((RateRule) r).setVariable(addVar);
							r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
							error = checkRateRuleUnits(r);
						}
						else {
							r = sbmlDoc.getModel().createAssignmentRule();
							SBMLutilities.setMetaId(r, ruleId);
							((AssignmentRule) r).setVariable(addVar);
							if (bioModel.getSBMLDocument().getModel().getParameter(addVar)!=null &&
									SBMLutilities.isBoolean(bioModel.getSBMLDocument().getModel().getParameter(addVar))) {
								r.setMath(bioModel.addBooleanAssign(ruleMath.getText().trim()));
							} else {
								r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
							}
							error = checkAssignmentRuleUnits(r);
						}
						if (!error && SBMLutilities.checkCycles(bioModel.getSBMLDocument())) {
							JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
									"Cycle Detected", JOptionPane.ERROR_MESSAGE);
							error = true;
//							rul = oldRul;
						}
					}
					if (error) {
						rul = oldRul;
						removeTheRule(addStr);
					} else {
						ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(r);
						for(int i = 0; dimID!=null && i<dimID.length-1; i++){
							org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[i]);
							dimX.setSize(dimID[i+1]);
							dimX.setArrayDimension(i);
						}
						for(int i = 0; dex!=null && i<dex.length-1; i++){
							Index indexRule = new Index();
							indexRule.setArrayDimension(i);
							indexRule.setReferencedAttribute("variable");
							ASTNode indexMath = SBMLutilities.myParseFormula(dex[i+1]);
							indexRule.setMath(indexMath);
							sBasePlugin.addIndex(indexRule);
						}
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.RULE + "__" + r.getMetaId());
							port.setMetaIdRef(r.getMetaId());
							ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
							sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());		
							sBasePluginPort.unsetListOfIndices();
							for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++) {
								org.sbml.jsbml.ext.arrays.Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
								Index portIndex = sBasePluginPort.createIndex();
								portIndex.setReferencedAttribute("comp:metaIdRef");
								portIndex.setArrayDimension(i);
								portIndex.setMath(SBMLutilities.myParseFormula(dim.getId()));
							}
						}
					}
//					updateRules(rul);
					if (SBOTerms.getSelectedItem().equals("(unspecified)")) {
						r.unsetSBOTerm();
					} else {
						r.setSBOTerm(SBMLutilities.sbo.getId((String)SBOTerms.getSelectedItem()));
					}
					rules.setListData(rul);
					rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (bioModel.getSBMLDocument().getModel().getRuleCount() == 1) {
						rules.setSelectedIndex(0);
					}
					else {
						rules.setSelectedIndex(index);
					}
				}
				modelEditor.setDirty(true);
				if (!error && !modelEditor.isParamsOnly()) {
					// Add SBOL annotation to rule
					if (sbolField.getSBOLURIs().size() > 0) {
						if (!r.isSetMetaId() || r.getMetaId().equals(""))
							SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), r, 
									bioModel.getMetaIDIndex());
						SBOLAnnotation sbolAnnot = new SBOLAnnotation(r.getMetaId(), sbolField.getSBOLURIs(), 
								sbolField.getSBOLStrand());
						AnnotationUtility.setSBOLAnnotation(r, sbolAnnot);
					} else
						AnnotationUtility.removeSBOLAnnotation(r);
				}
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, rulePanel, "Rule Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return metaId;
		}
		return id.getText().split("\\[")[0].trim();
	}
	
	/**
	 * Remove a rule
	 */
	private void removeRule() {
		int index = rules.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) rules.getSelectedValue()).split("\\[| ")[0];
			removeTheRule(selected);
			rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			Utility.remove(rules);
			rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < rules.getModel().getSize()) {
				rules.setSelectedIndex(index);
			}
			else {
				rules.setSelectedIndex(index - 1);
			}
			modelEditor.setDirty(true);
			bioModel.makeUndoPoint();
		}
	}

	public void removeRuleByMetaId(String metaId) {
		Rule rule = (Rule)SBMLutilities.getElementByMetaId(bioModel.getSBMLDocument().getModel(), metaId);
		if (rule != null) {
			bioModel.getSBMLDocument().getModel().removeRule(rule);
			for (int j = 0; j < bioModel.getSBMLCompModel().getListOfPorts().size(); j++) {
				Port port = bioModel.getSBMLCompModel().getListOfPorts().get(j);
				if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(metaId)) {
					bioModel.getSBMLCompModel().getListOfPorts().remove(j);
					break;
				}
			}
		}
	}
	
	/**
	 * Remove the rule
	 */
	private void removeTheRule(String selected) {
		ListOf<Rule> r = bioModel.getSBMLDocument().getModel().getListOfRules();
		for (int i = 0; i < bioModel.getSBMLDocument().getModel().getRuleCount(); i++) {
			if ((r.get(i).getMetaId().equals(selected))) {
				for (int j = 0; j < bioModel.getSBMLCompModel().getListOfPorts().size(); j++) {
					Port port = bioModel.getSBMLCompModel().getListOfPorts().get(j);
					if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(r.get(i).getMetaId())) {
						bioModel.getSBMLCompModel().getListOfPorts().remove(j);
						break;
					}
				}
				r.remove(i);
			}
		}
		Layout layout = bioModel.getLayout();
		if (layout.getListOfAdditionalGraphicalObjects().get(GlobalConstants.GLYPH+"__"+selected)!=null) {
			layout.getListOfAdditionalGraphicalObjects().remove(GlobalConstants.GLYPH+"__"+selected);
		}
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
			layout.getListOfTextGlyphs().remove(GlobalConstants.TEXT_GLYPH+"__"+selected);
		}
	}

	/**
	 * Sort rules in order to be evaluated
	 */
	private static String[] sortRules(String[] rules) {
		String[] result = new String[rules.length];
		int j = 0;
		boolean[] used = new boolean[rules.length];
		for (int i = 0; i < rules.length; i++) {
			used[i] = false;
		}
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("0")) {
				result[j] = rules[i];
				used[i] = true;
				j++;
			}
		}
		boolean progress;
		do {
			progress = false;
			for (int i = 0; i < rules.length; i++) {
				if (used[i] || (rules[i].split(" ")[0].equals("0")) || (rules[i].split(" ")[0].equals("d(")))
					continue;
				String[] rule = rules[i].split(" ");
				boolean insert = true;
				for (int k = 1; k < rule.length; k++) {
					for (int l = 0; l < rules.length; l++) {
						if (used[l] || (rules[l].split(" ")[0].equals("0")) || (rules[l].split(" ")[0].equals("d(")))
							continue;
						String[] rule2 = rules[l].split(" ");
						if (rule[k].equals(rule2[0])) {
							insert = false;
							break;
						}
					}
					if (!insert)
						break;
				}
				if (insert) {
					result[j] = rules[i];
					j++;
					progress = true;
					used[i] = true;
				}
			}
		}
		while ((progress) && (j < rules.length));
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("d(")) {
				result[j] = rules[i];
				j++;
			}
		}
		if (j != rules.length) {
			throw new RuntimeException();
		}
		return result;
	}

	/**
	 * Create comboBox for assignments rules
	 */
	private boolean assignRuleVar(String selected) {
		boolean assignOK = false;
		ruleVar.removeAllItems();
		Model model = bioModel.getSBMLDocument().getModel();
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			Compartment compartment = model.getCompartment(i);
			String id = compartment.getId();
			if (!compartment.getConstant()) {
				if (keepVarAssignRule(bioModel, selected, id)) {
					ruleVar.addItem(compartment.getId());
					assignOK = true;
				}
			}
		}
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter p = model.getParameter(i);
			if (!(p.getConstant()) && !SBMLutilities.isPlace(p) /*&& !SBMLutilities.isBoolean(p)*/) {
				if (keepVarAssignRule(bioModel, selected, p.getId())) {
					ruleVar.addItem(p.getId());
					assignOK = true;
				}
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);
			String id = species.getId();
			if (!species.getConstant()) {
				if (keepVarAssignRule(bioModel, selected, id))
					if (species.getBoundaryCondition() || !SBMLutilities.usedInReaction(bioModel.getSBMLDocument(), id)) {
						ruleVar.addItem(species.getId());
						assignOK = true;
					}
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			for (int j = 0; j < reaction.getReactantCount(); j++) {
				SpeciesReference reactant = reaction.getReactant(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals("")) && !(reactant.getConstant())) {
					String id = reactant.getId();
					if (keepVarAssignRule(bioModel, selected, id)) {
						ruleVar.addItem(id);
						assignOK = true;
					}
				}
			}
			for (int j = 0; j < reaction.getProductCount(); j++) {
				SpeciesReference product = reaction.getProduct(j);
				if ((product.isSetId()) && (!product.getId().equals("")) && !(product.getConstant())) {
					String id = product.getId();
					if (keepVarAssignRule(bioModel, selected, id)) {
						ruleVar.addItem(id);
						assignOK = true;
					}
				}
			}
		}
		return assignOK;
	}

	/**
	 * Create comboBox for rate rules
	 */
	private boolean rateRuleVar(String selected) {
		boolean rateOK = false;
		ruleVar.removeAllItems();
		Model model = bioModel.getSBMLDocument().getModel();
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			Compartment compartment = model.getCompartment(i);
			String id = compartment.getId();
			if (!compartment.getConstant()) {
				if (keepVarRateRule(bioModel, selected, id)) {
					ruleVar.addItem(compartment.getId());
					rateOK = true;
				}
			}
		}
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter p = model.getParameter(i);
			if (!(p.getConstant()) && !SBMLutilities.isPlace(p) && !SBMLutilities.isBoolean(p)) {
				if (keepVarRateRule(bioModel, selected, p.getId())) {
					ruleVar.addItem(p.getId());
					rateOK = true;
				}
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);
			String id = species.getId();
			if (!species.getConstant()) {
				if (keepVarRateRule(bioModel, selected, id))
					if (species.getBoundaryCondition() || !SBMLutilities.usedInReaction(bioModel.getSBMLDocument(), id)) {
						ruleVar.addItem(species.getId());
						rateOK = true;
					}
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			for (int j = 0; j < reaction.getReactantCount(); j++) {
				SpeciesReference reactant = reaction.getReactant(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals("")) && !(reactant.getConstant())) {
					String id = reactant.getId();
					if (keepVarRateRule(bioModel, selected, id)) {
						ruleVar.addItem(id);
						rateOK = true;
					}
				}
			}
			for (int j = 0; j < reaction.getProductCount(); j++) {
				SpeciesReference product = reaction.getProduct(j);
				if ((product.isSetId()) && (!product.getId().equals("")) && !(product.getConstant())) {
					String id = product.getId();
					if (keepVarRateRule(bioModel, selected, id)) {
						ruleVar.addItem(id);
						rateOK = true;
					}
				}
			}
		}
		return rateOK;
	}

	/**
	 * Check the units of a rate rule
	 */
	public boolean checkRateRuleUnits(Rule rule) {
		if (Gui.getCheckUndeclared()) {
			if (rule.containsUndeclaredUnits()) {
				JOptionPane.showMessageDialog(Gui.frame, "Rate rule contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (Gui.getCheckUnits()) {
			if (SBMLutilities.checkUnitsInRateRule(bioModel.getSBMLDocument(), rule)) {
				JOptionPane.showMessageDialog(Gui.frame, "Units on the left and right-hand side of the rate rule do not agree.",
						"Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the units of an assignment rule
	 */
	public boolean checkAssignmentRuleUnits(Rule rule) {
		if (Gui.getCheckUndeclared()) {
			if (rule.containsUndeclaredUnits()) {
				JOptionPane.showMessageDialog(Gui.frame, "Assignment rule contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (Gui.getCheckUnits()) {
			if (SBMLutilities.checkUnitsInAssignmentRule(bioModel.getSBMLDocument(), rule)) {
				JOptionPane.showMessageDialog(Gui.frame, "Units on the left and right-hand side of the assignment rule do not agree.",
						"Units Do Not Match", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Update rules
	 */
//	private void updateRules(String[] rul) {
//		ListOf r = gcm.getSBMLDocument().getModel().getListOfRules();
//		while (gcm.getSBMLDocument().getModel().getRuleCount() > 0) {
//			SBase rule = r.remove(0);
//		}
//		for (int i = 0; i < rul.length; i++) {
//			if (rul[i].split(" ")[0].equals("0")) {
//				AlgebraicRule rule = gcm.getSBMLDocument().getModel().createAlgebraicRule();
//				rule.setMath(SBMLutilities.myParseFormula(rul[i].substring(rul[i].indexOf("=") + 1)));
//			}
//			else if (rul[i].split(" ")[0].equals("d(")) {
//				RateRule rule = gcm.getSBMLDocument().getModel().createRateRule();
//				rule.setVariable(rul[i].split(" ")[1]);
//				rule.setMath(SBMLutilities.myParseFormula(rul[i].substring(rul[i].indexOf("=") + 1)));
//			}
//			else {
//				AssignmentRule rule = gcm.getSBMLDocument().getModel().createAssignmentRule();
//				rule.setVariable(rul[i].split(" ")[0]);
//				rule.setMath(SBMLutilities.myParseFormula(rul[i].substring(rul[i].indexOf("=") + 1)));
//			}
//		}
//	}

	/**
	 * Determines if a variable is already in an initial assignment, assignment
	 * rule, or rate rule
	 */
	public static boolean keepVarAssignRule(BioModel bioModel, String selected, String id) {
		if (!selected.equals(id)) {
			Model model = bioModel.getSBMLDocument().getModel();
			for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
				InitialAssignment init = model.getInitialAssignment(i);
				if (init.getVariable().equals(id))
					return false;
			}
			for (int i = 0; i < model.getEventCount(); i++) {
				Event event = model.getEvent(i);
				for (int j = 0; j < event.getEventAssignmentCount(); j++) {
					if (id.equals(event.getListOfEventAssignments().get(j).getVariable())) {
						return false;
					}
				}
			}
			for (int i = 0; i < model.getRuleCount(); i++) {
				Rule rule = model.getRule(i);
				if (rule.isAssignment() && SBMLutilities.getVariable(rule).equals(id))
					return false;
				if (rule.isRate() && SBMLutilities.getVariable(rule).equals(id))
					return false;
			}
		}
		return true;
	}

	/**
	 * Determines if a variable is already in a rate rule
	 */
	public static boolean keepVarRateRule(BioModel bioModel, String selected, String id) {
		if (!selected.equals(id)) {
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getRuleCount(); i++) {
				Rule rule = bioModel.getSBMLDocument().getModel().getRule(i);
				if ((rule.isRate()||rule.isAssignment()) && SBMLutilities.getVariable(rule).equals(id))
					return false;
			}
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addRule) {
			ruleEditor("Add","");
			bioModel.makeUndoPoint();
		}
		// if the edit event button is clicked
		else if (e.getSource() == editRule) {
			if (rules.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No rule selected.", "Must Select a Rule", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String selected = ((String) rules.getSelectedValue()).split("\\[| ")[0];
			ruleEditor("OK",selected);
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeRule) {
			removeRule();
		}
		// if the variable is changed
		else if (e.getSource() == ruleType) {
			if (((String) ruleType.getSelectedItem()).equals("Assignment")) {
				assignRuleVar("");
				ruleVar.setEnabled(true);
			}
			else if (((String) ruleType.getSelectedItem()).equals("Rate")) {
				rateRuleVar("");
				ruleVar.setEnabled(true);
			}
			else {
				ruleVar.removeAllItems();
				ruleVar.setEnabled(false);
			}
			if(ruleVar.isEnabled()){
				iIndex.setEnabled(true);
			}
			else{
				iIndex.setEnabled(false);
			}
		}
		/*
		else if (e.getSource() == ruleVar) {
			if(ruleVar.isEnabled()){
				iIndex.setEnabled(true);
			}
			else{
				iIndex.setEnabled(false);
			}
		}
		*/
		else if (e.getSource() == ruleVar){
			if (bioModel.isArray((String)ruleVar.getSelectedItem())) {
				iIndex.setEnabled(true);
			} else {
				iIndex.setText("");
				iIndex.setEnabled(false);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == rules) {
				if (rules.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No rule selected.", "Must Select a Rule", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String selected = ((String) rules.getSelectedValue()).split("\\[| ")[0];
				ruleEditor("OK",selected);
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
