package biomodel.gui.textualeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;
import main.util.MutableBoolean;
import main.util.Utility;

import org.sbml.libsbml.AlgebraicRule;
import org.sbml.libsbml.AssignmentRule;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.Layout;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Port;
import org.sbml.libsbml.RateRule;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.ModelEditor;
import biomodel.gui.SBOLField;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;


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

	private MutableBoolean dirty;

	private Gui biosim;

	private JComboBox ruleType, ruleVar;
	
	private ModelEditor modelEditor;
	
	private SBOLField sbolField;
	
	private JTextField ruleMath;

	/* Create rule panel */
	public Rules(Gui biosim, BioModel gcm, ModelEditor gcmEditor, MutableBoolean dirty) {
		super(new BorderLayout());
		this.bioModel = gcm;
		this.biosim = biosim;
		this.modelEditor = gcmEditor;
		this.dirty = dirty;

		/* Create rule panel */
		Model model = gcm.getSBMLDocument().getModel();
		addRule = new JButton("Add Rule");
		removeRule = new JButton("Remove Rule");
		editRule = new JButton("Edit Rule");
		rules = new JList();
		ListOf listOfRules = model.getListOfRules();
		String[] rul = new String[(int) model.getNumRules()];
		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = (Rule) listOfRules.get(i);
			if (rule.isAlgebraic()) {
				rul[i] = "0 = " + bioModel.removeBooleans(rule.getMath());
			}
			else if (rule.isAssignment()) {
				rul[i] = rule.getVariable() + " = " + bioModel.removeBooleans(rule.getMath());
			}
			else {
				rul[i] = "d( " + rule.getVariable() + " )/dt = " + bioModel.removeBooleans(rule.getMath());
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
		if (model.getNumRules() > 0) {
			String[] rul = new String[(int) model.getNumRules()];
			for (int i = 0; i < model.getNumRules(); i++) {
				Rule rule = (Rule) model.getListOfRules().get(i);
				if (rule.isAlgebraic()) {
					rul[i] = "0 = " + bioModel.removeBooleans(rule.getMath());
				}
				else if (rule.isAssignment()) {
					rul[i] = rule.getVariable() + " = " + bioModel.removeBooleans(rule.getMath());
				}
				else {
					rul[i] = "d( " + rule.getVariable() + " )/dt = " + bioModel.removeBooleans(rule.getMath());
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
		JPanel IDPanel = new JPanel();
		JPanel mathPanel = new JPanel();
		JLabel IDLabel = new JLabel("ID:");
		JLabel typeLabel = new JLabel("Type:");
		JLabel varLabel = new JLabel("Variable:");
		JLabel ruleLabel = new JLabel("Rule:");
		JLabel onPortLabel = new JLabel("Is Mapped to a Port:");
		String[] list = { "Algebraic", "Assignment", "Rate" };
		ruleType = new JComboBox(list);
		ruleVar = new JComboBox();
		JTextField id = new JTextField(12);
		ruleMath = new JTextField(30);
		ruleVar.setEnabled(false);
		JCheckBox onPort = new JCheckBox();
		if (option.equals("OK")) {
			ruleType.setEnabled(false);
			Rule rule = (Rule)bioModel.getSBMLDocument().getModel().getElementByMetaId(metaId);
			if (rule.getElementName().equals(GlobalConstants.ALGEBRAIC_RULE)) {
				ruleType.setSelectedItem("Algebraic");
				ruleVar.setEnabled(false);
				ruleMath.setText(bioModel.removeBooleans(rule.getMath()));
			}
			else if  (rule.getElementName().equals(GlobalConstants.RATE_RULE)) {
				ruleType.setSelectedItem("Rate");
				rateRuleVar(rule.getVariable());
				ruleVar.setEnabled(true);
				ruleVar.setSelectedItem(rule.getVariable());
				ruleMath.setText(bioModel.removeBooleans(rule.getMath()));
			}
			else {
				ruleType.setSelectedItem("Assignment");
				assignRuleVar(rule.getVariable());
				ruleVar.setEnabled(true);
				ruleVar.setSelectedItem(rule.getVariable());
				ruleMath.setText(bioModel.removeBooleans(rule.getMath()));
			}
			if (!modelEditor.isParamsOnly()) {
				//Parse out SBOL annotations and add to SBOL field
				LinkedList<URI> sbolURIs = AnnotationUtility.parseSBOLAnnotation(rule);
				// Field for annotating rules with SBOL DNA components
				sbolField = new SBOLField(sbolURIs, GlobalConstants.SBOL_DNA_COMPONENT, modelEditor, 2, false);
			}
			if (rule.isSetMetaId()) {
				id.setText(rule.getMetaId());
			} else {
				String ruleId = "rule0";
				int cn = 0;
				while (bioModel.getSBMLDocument().getElementByMetaId(ruleId)!=null) {
					cn++;
					ruleId = "rule" + cn;
				}
				id.setText(ruleId);
			}
			if (bioModel.getPortByMetaIdRef(rule.getMetaId())!=null) {
				onPort.setSelected(true);
			} else {
				onPort.setSelected(false);
			}
		}
		else {
			// Field for annotating rules with SBOL DNA components
			sbolField = new SBOLField(new LinkedList<URI>(), GlobalConstants.SBOL_DNA_COMPONENT, modelEditor, 2, false);
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
			while (bioModel.getSBMLDocument().getElementByMetaId(ruleId)!=null) {
				cn++;
				ruleId = "rule" + cn;
			}
			id.setText(ruleId);
		}
		ruleType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
			}
		});
		IDPanel.add(IDLabel);
		IDPanel.add(id);
		IDPanel.add(typeLabel);
		IDPanel.add(ruleType);
		IDPanel.add(varLabel);
		IDPanel.add(ruleVar);
		mathPanel.add(ruleLabel);
		mathPanel.add(ruleMath);
		mathPanel.add(onPortLabel);
		mathPanel.add(onPort);
		rulePanel.add(IDPanel,"North");
		rulePanel.add(mathPanel,"Center");
		if (!modelEditor.isParamsOnly())
			rulePanel.add(sbolField,"South");
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, rulePanel, "Rule Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			String addVar = "";
			addVar = (String) ruleVar.getSelectedItem();
			error = SBMLutilities.checkID(bioModel.getSBMLDocument(), id.getText().trim(), metaId, false, true);
			
			if (ruleMath.getText().trim().equals("")) {
				JOptionPane.showMessageDialog(Gui.frame, "Rule must have formula.", "Enter Rule Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else if (SBMLutilities.myParseFormula(ruleMath.getText().trim()) == null) {
				JOptionPane.showMessageDialog(Gui.frame, "Rule formula is not valid.", "Enter Valid Formula", JOptionPane.ERROR_MESSAGE);
				error = true;
			}
			else {
				ArrayList<String> invalidVars = SBMLutilities.getInvalidVariables(bioModel.getSBMLDocument(), ruleMath.getText().trim(), "", false);
				if (invalidVars.size() > 0) {
					String invalid = "";
					for (int i = 0; i < invalidVars.size(); i++) {
						if (i == invalidVars.size() - 1) {
							invalid += invalidVars.get(i);
						}
						else {
							invalid += invalidVars.get(i) + "\n";
						}
					}
					String message;
					message = "Rule contains unknown variables.\n\n" + "Unknown variables:\n" + invalid;
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setWrapStyleWord(true);
					messageArea.setEditable(false);
					JScrollPane scrolls = new JScrollPane();
					scrolls.setMinimumSize(new Dimension(300, 300));
					scrolls.setPreferredSize(new Dimension(300, 300));
					scrolls.setViewportView(messageArea);
					JOptionPane.showMessageDialog(Gui.frame, scrolls, "Unknown Variables", JOptionPane.ERROR_MESSAGE);
					error = true;
				}
				if (!error) {
					error = SBMLutilities.checkNumFunctionArguments(bioModel.getSBMLDocument(), bioModel.addBooleans(ruleMath.getText().trim()));
				}
				if (!error) {
					if (bioModel.addBooleans(ruleMath.getText().trim()).isBoolean()) {
						JOptionPane.showMessageDialog(Gui.frame, "Rule must evaluate to a number.", "Number Expected", JOptionPane.ERROR_MESSAGE);
						error = true;
					}
				}
			}
			if (!error) {
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
					Rule r = (Rule) (bioModel.getSBMLDocument().getModel().getElementByMetaId(metaId));
					String addStr;
					String oldVar = "";
					String oldMath = bioModel.removeBooleans(r.getMath());
					if (ruleType.getSelectedItem().equals("Algebraic")) {
						r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
						addStr = "0 = " + bioModel.removeBooleans(r.getMath());
						SBMLutilities.checkOverDetermined(bioModel.getSBMLDocument());
					}
					else if (ruleType.getSelectedItem().equals("Rate")) {
						oldVar = r.getVariable();
						r.setVariable(addVar);
						r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
						error = checkRateRuleUnits(r);
						addStr = "d( " + addVar + " )/dt = " + bioModel.removeBooleans(r.getMath());
					}
					else {
						oldVar = r.getVariable();
						r.setVariable(addVar);
						r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
						error = checkAssignmentRuleUnits(r);
						addStr = addVar + " = " + bioModel.removeBooleans(r.getMath());
					}
//					String oldVal = rul[index];
					rul[index] = addStr;
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
						// Handle SBOL data
						// Checks whether SBOL annotation on model needs to be deleted later when annotating rule with SBOL
//						boolean removeModelSBOLAnnotationFlag = false;
//						LinkedList<URI> sbolURIs = sbolField.getSBOLURIs();
//						if (sbolField.getSBOLURIs().size() > 0 && 
//								bioModel.getElementSBOLCount() == 0 && bioModel.getModelSBOLAnnotationFlag()) {
//							Object[] sbolOptions = { "OK", "Cancel" };
//							int choice = JOptionPane.showOptionDialog(null, 
//									"SBOL associated to model elements can't coexist with SBOL associated to model itself unless" +
//											" the latter was previously generated from the former.  Remove SBOL associated to model?", 
//											"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, sbolOptions, sbolOptions[0]);
//							if (choice == JOptionPane.OK_OPTION)
//								removeModelSBOLAnnotationFlag = true;
//							else
//								error = true;
//						}
						if (!error) {
							// Add SBOL annotation to rule
							LinkedList<URI> sbolURIs = sbolField.getSBOLURIs();
							if (sbolURIs.size() > 0) {
								SBOLAnnotation sbolAnnot = new SBOLAnnotation(r.getMetaId(), sbolURIs);
								AnnotationUtility.setSBOLAnnotation(r, sbolAnnot);
								if (sbolField.wasInitiallyBlank())
									bioModel.setElementSBOLCount(bioModel.getElementSBOLCount() + 1);
//								if (removeModelSBOLAnnotationFlag) {
//									AnnotationUtility.removeSBOLAnnotation(bioModel.getSBMLDocument().getModel());
//									bioModel.setModelSBOLAnnotationFlag(false);
//									modelEditor.getSchematic().getSBOLDescriptorsButton().setEnabled(true);
//								}
							} else {
								AnnotationUtility.removeSBOLAnnotation(r);
								if (!sbolField.wasInitiallyBlank())
									bioModel.setElementSBOLCount(bioModel.getElementSBOLCount() - 1);
							}

							Port port = bioModel.getPortByMetaIdRef(r.getMetaId());
							r.setMetaId(id.getText().trim());
							if (port!=null) {
								if (onPort.isSelected()) {
									port.setId(GlobalConstants.RULE+"__"+r.getMetaId());
									port.setMetaIdRef(r.getMetaId());
								} else {
									port.removeFromParentAndDelete();
								}
							} else {
								if (onPort.isSelected()) {
									port = bioModel.getSBMLCompModel().createPort();
									port.setId(GlobalConstants.RULE+"__"+r.getMetaId());
									port.setMetaIdRef(r.getMetaId());
								}
							}
						}
					}
					if (error) {
						if (!oldVar.equals("")) {
							r.setVariable(oldVar);
						}
						r.setMath(bioModel.addBooleans(oldMath));
						rul = oldRul;
//						rul[index] = oldVal;
					}
//					updateRules(rul);
					rules.setListData(rul);
					rules.setSelectedIndex(index);
				}
				else {
					String[] rul = new String[rules.getModel().getSize()];
					for (int i = 0; i < rules.getModel().getSize(); i++) {
						rul[i] = rules.getModel().getElementAt(i).toString();
					}
					JList add = new JList();
					int index = rules.getSelectedIndex();
					String addStr;
					if (ruleType.getSelectedItem().equals("Algebraic")) {
						addStr = "0 = " + SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(ruleMath.getText().trim()));
					}
					else if (ruleType.getSelectedItem().equals("Rate")) {
						addStr = "d( " + addVar + " )/dt = "
								+ SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(ruleMath.getText().trim()));
					}
					else {
						addStr = addVar + " = " + SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(ruleMath.getText().trim()));
					}
					Object[] adding = { addStr };
					add.setListData(adding);
					add.setSelectedIndex(0);
					rules.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					adding = Utility.add(rul, rules, add, null, null, null, null, null, Gui.frame);
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
					Rule rPointer = null;
					if (!error) {
						SBMLDocument sbmlDoc = bioModel.getSBMLDocument();
						if (ruleType.getSelectedItem().equals("Algebraic")) {
							AlgebraicRule r = sbmlDoc.getModel().createAlgebraicRule();
							r.setMetaId(id.getText().trim());
							r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
							SBMLutilities.checkOverDetermined(bioModel.getSBMLDocument());
							rPointer = r;
						}
						else if (ruleType.getSelectedItem().equals("Rate")) {
							RateRule r = sbmlDoc.getModel().createRateRule();
							r.setMetaId(id.getText().trim());
							r.setVariable(addVar);
							r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
							error = checkRateRuleUnits(r);
							rPointer = r;
						}
						else {
							AssignmentRule r = sbmlDoc.getModel().createAssignmentRule();
							r.setMetaId(id.getText().trim());
							r.setVariable(addVar);
							r.setMath(bioModel.addBooleans(ruleMath.getText().trim()));
							error = checkAssignmentRuleUnits(r);
							rPointer = r;
						}
					}
					if (!error && SBMLutilities.checkCycles(bioModel.getSBMLDocument())) {
						JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
								"Cycle Detected", JOptionPane.ERROR_MESSAGE);
						error = true;
//						rul = oldRul;
					}
					if (error) {
						rul = oldRul;
						removeTheRule(addStr);
					} else {
						if (onPort.isSelected()) {
							Port port = bioModel.getSBMLCompModel().createPort();
							port.setId(GlobalConstants.RULE+"__"+rPointer.getMetaId());
							port.setMetaIdRef(rPointer.getMetaId());
						}
						rPointer.setMetaId(id.getText().trim());
					}
//					updateRules(rul);
					rules.setListData(rul);
					rules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					if (bioModel.getSBMLDocument().getModel().getNumRules() == 1) {
						rules.setSelectedIndex(0);
					}
					else {
						rules.setSelectedIndex(index);
					}
				}
				dirty.setValue(true);
				bioModel.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, rulePanel, "Rule Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
						options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return metaId;
		}
		return id.getText().trim();
	}
	
	/**
	 * Remove a rule
	 */
	private void removeRule() {
		int index = rules.getSelectedIndex();
		if (index != -1) {
			String selected = ((String) rules.getSelectedValue());
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
			dirty.setValue(true);
			bioModel.makeUndoPoint();
		}
	}

	public void removeRuleByMetaId(String metaId) {
		SBase rule = bioModel.getSBMLDocument().getModel().getElementByMetaId(metaId);
		if (rule != null) {
			rule.removeFromParentAndDelete();
			for (long j = 0; j < bioModel.getSBMLCompModel().getNumPorts(); j++) {
				Port port = bioModel.getSBMLCompModel().getPort(j);
				if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(metaId)) {
					bioModel.getSBMLCompModel().removePort(j);
					break;
				}
			}
		}
	}
	
	/**
	 * Remove the rule
	 */
	private void removeTheRule(String selected) {
		// algebraic rule
		if ((selected.split(" ")[0]).equals("0")) {
			String tempMath = selected.substring(4);
			ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
				if ((((Rule) r.get(i)).isAlgebraic()) && bioModel.removeBooleans(((Rule) r.get(i)).getMath()).equals(tempMath)) {
					for (long j = 0; j < bioModel.getSBMLCompModel().getNumPorts(); j++) {
						Port port = bioModel.getSBMLCompModel().getPort(j);
						if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(r.get(i).getMetaId())) {
							bioModel.getSBMLCompModel().removePort(j);
							break;
						}
					}
					r.remove(i);
				}
			}
		}
		// rate rule
		else if ((selected.split(" ")[0]).equals("d(")) {
			String tempVar = selected.split(" ")[1];
			String tempMath = selected.substring(selected.indexOf('=') + 2);
			ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
				if ((((Rule) r.get(i)).isRate()) && bioModel.removeBooleans(((Rule) r.get(i)).getMath()).equals(tempMath)
						&& ((Rule) r.get(i)).getVariable().equals(tempVar)) {
					for (long j = 0; j < bioModel.getSBMLCompModel().getNumPorts(); j++) {
						Port port = bioModel.getSBMLCompModel().getPort(j);
						if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(r.get(i).getMetaId())) {
							bioModel.getSBMLCompModel().removePort(j);
							break;
						}
					}
					r.remove(i);
				}
			}
		}
		// assignment rule
		else {
			String tempVar = selected.split(" ")[0];
			String tempMath = selected.substring(selected.indexOf('=') + 2);
			ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
				if ((((Rule) r.get(i)).isAssignment()) && bioModel.removeBooleans(((Rule) r.get(i)).getMath()).equals(tempMath)
						&& ((Rule) r.get(i)).getVariable().equals(tempVar)) {
					for (long j = 0; j < bioModel.getSBMLCompModel().getNumPorts(); j++) {
						Port port = bioModel.getSBMLCompModel().getPort(j);
						if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(r.get(i).getMetaId())) {
							bioModel.getSBMLCompModel().removePort(j);
							break;
						}
					}
					r.remove(i);
				}
			}
		}
		if (bioModel.getSBMLLayout().getLayout("iBioSim") != null) {
			Layout layout = bioModel.getSBMLLayout().getLayout("iBioSim"); 
			if (layout.getAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+selected)!=null) {
				layout.removeAdditionalGraphicalObject(GlobalConstants.GLYPH+"__"+selected);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected) != null) {
				layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+selected);
			}
		}
	}

	/**
	 * Sort rules in order to be evaluated
	 */
	private String[] sortRules(String[] rules) {
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
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (!((Compartment) ids.get(i)).getConstant()) {
				if (keepVarAssignRule(bioModel, selected, id)) {
					ruleVar.addItem(((Compartment) ids.get(i)).getId());
					assignOK = true;
				}
			}
		}
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter p = model.getParameter(i);
			if (!(p.getConstant()) && !SBMLutilities.isPlace(p) && !SBMLutilities.isBoolean(p)) {
				if (keepVarAssignRule(bioModel, selected, p.getId())) {
					ruleVar.addItem(p.getId());
					assignOK = true;
				}
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (!((Species) ids.get(i)).getConstant()) {
				if (keepVarAssignRule(bioModel, selected, id))
					if (((Species) ids.get(i)).getBoundaryCondition() || !SBMLutilities.usedInReaction(bioModel.getSBMLDocument(), id)) {
						ruleVar.addItem(((Species) ids.get(i)).getId());
						assignOK = true;
					}
			}
		}
		ids = model.getListOfReactions();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) ids.get(i);
			ListOf ids2 = reaction.getListOfReactants();
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				SpeciesReference reactant = (SpeciesReference) ids2.get(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals("")) && !(reactant.getConstant())) {
					String id = reactant.getId();
					if (keepVarAssignRule(bioModel, selected, id)) {
						ruleVar.addItem(id);
						assignOK = true;
					}
				}
			}
			ids2 = reaction.getListOfProducts();
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				SpeciesReference product = (SpeciesReference) ids2.get(j);
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
		ListOf ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			String id = ((Compartment) ids.get(i)).getId();
			if (!((Compartment) ids.get(i)).getConstant()) {
				if (keepVarRateRule(bioModel, selected, id)) {
					ruleVar.addItem(((Compartment) ids.get(i)).getId());
					rateOK = true;
				}
			}
		}
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter p = model.getParameter(i);
			if (!(p.getConstant()) && !SBMLutilities.isPlace(p) && !SBMLutilities.isBoolean(p)) {
				if (keepVarRateRule(bioModel, selected, p.getId())) {
					ruleVar.addItem(p.getId());
					rateOK = true;
				}
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			String id = ((Species) ids.get(i)).getId();
			if (!((Species) ids.get(i)).getConstant()) {
				if (keepVarRateRule(bioModel, selected, id))
					if (((Species) ids.get(i)).getBoundaryCondition() || !SBMLutilities.usedInReaction(bioModel.getSBMLDocument(), id)) {
						ruleVar.addItem(((Species) ids.get(i)).getId());
						rateOK = true;
					}
			}
		}
		ids = model.getListOfReactions();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) ids.get(i);
			ListOf ids2 = reaction.getListOfReactants();
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				SpeciesReference reactant = (SpeciesReference) ids2.get(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals("")) && !(reactant.getConstant())) {
					String id = reactant.getId();
					if (keepVarRateRule(bioModel, selected, id)) {
						ruleVar.addItem(id);
						rateOK = true;
					}
				}
			}
			ids2 = reaction.getListOfProducts();
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				SpeciesReference product = (SpeciesReference) ids2.get(j);
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
		bioModel.getSBMLDocument().getModel().populateListFormulaUnitsData();
		if (rule.containsUndeclaredUnits()) {
			if (biosim.checkUndeclared) {
				JOptionPane.showMessageDialog(Gui.frame, "Rate rule contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (biosim.checkUnits) {
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
		bioModel.getSBMLDocument().getModel().populateListFormulaUnitsData();
		if (rule.containsUndeclaredUnits()) {
			if (biosim.checkUndeclared) {
				JOptionPane.showMessageDialog(Gui.frame, "Assignment rule contains literals numbers or parameters with undeclared units.\n"
						+ "Therefore, it is not possible to completely verify the consistency of the units.", "Contains Undeclared Units",
						JOptionPane.WARNING_MESSAGE);
			}
			return false;
		}
		else if (biosim.checkUnits) {
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
//		while (gcm.getSBMLDocument().getModel().getNumRules() > 0) {
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
	public static boolean keepVarAssignRule(BioModel gcm, String selected, String id) {
		if (!selected.equals(id)) {
			ListOf ia = gcm.getSBMLDocument().getModel().getListOfInitialAssignments();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumInitialAssignments(); i++) {
				InitialAssignment init = (InitialAssignment) ia.get(i);
				if (init.getSymbol().equals(id))
					return false;
			}
			ListOf e = gcm.getSBMLDocument().getModel().getListOfEvents();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumEvents(); i++) {
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) e.get(i);
				for (int j = 0; j < event.getNumEventAssignments(); j++) {
					if (id.equals(event.getEventAssignment(j).getVariable())) {
						return false;
					}
				}
			}
			ListOf r = gcm.getSBMLDocument().getModel().getListOfRules();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumRules(); i++) {
				Rule rule = (Rule) r.get(i);
				if (rule.isAssignment() && rule.getVariable().equals(id))
					return false;
				if (rule.isRate() && rule.getVariable().equals(id))
					return false;
			}
		}
		return true;
	}

	/**
	 * Determines if a variable is already in a rate rule
	 */
	public static boolean keepVarRateRule(BioModel gcm, String selected, String id) {
		if (!selected.equals(id)) {
			ListOf r = gcm.getSBMLDocument().getModel().getListOfRules();
			for (int i = 0; i < gcm.getSBMLDocument().getModel().getNumRules(); i++) {
				Rule rule = (Rule) r.get(i);
				if ((rule.isRate()||rule.isAssignment()) && rule.getVariable().equals(id))
					return false;
			}
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
		// if the add event button is clicked
		if (e.getSource() == addRule) {
			ruleEditor("Add","");
		}
		// if the edit event button is clicked
		else if (e.getSource() == editRule) {
			if (rules.getSelectedIndex() == -1) {
				JOptionPane.showMessageDialog(Gui.frame, "No rule selected.", "Must Select a Rule", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String metaId = "";
			String selected = ((String) rules.getSelectedValue());
			if ((selected.split(" ")[0]).equals("0")) {
				String math = selected.substring(4);
				ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
				for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
					if ((((Rule) r.get(i)).isAlgebraic())
							&& (bioModel.removeBooleans(((Rule) r.get(i)).getMath()).equals(math))) {
						metaId = r.get(i).getMetaId();
					}
				}
			}
			else if ((selected.split(" ")[0]).equals("d(")) {
				String var = selected.split(" ")[1];
				ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
				for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
					if ((((Rule) r.get(i)).isRate()) && ((Rule) r.get(i)).getVariable().equals(var)) {
						metaId = r.get(i).getMetaId();
					}
				}
			}
			else {
				String var = selected.split(" ")[0];
				ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
				for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
					if ((((Rule) r.get(i)).isAssignment()) && ((Rule) r.get(i)).getVariable().equals(var)) {
						metaId = r.get(i).getMetaId();
					}
				}
			}
			ruleEditor("OK",metaId);
		}
		// if the remove event button is clicked
		else if (e.getSource() == removeRule) {
			removeRule();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			if (e.getSource() == rules) {
				if (rules.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(Gui.frame, "No rule selected.", "Must Select a Rule", JOptionPane.ERROR_MESSAGE);
					return;
				}
				String metaId = "";
				String selected = ((String) rules.getSelectedValue());
				if ((selected.split(" ")[0]).equals("0")) {
					String math = selected.substring(4);
					ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
					for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isAlgebraic())
								&& (bioModel.removeBooleans(((Rule) r.get(i)).getMath()).equals(math))) {
							metaId = r.get(i).getMetaId();
						}
					}
				}
				else if ((selected.split(" ")[0]).equals("d(")) {
					String var = selected.split(" ")[1];
					ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
					for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isRate()) && ((Rule) r.get(i)).getVariable().equals(var)) {
							metaId = r.get(i).getMetaId();
						}
					}
				}
				else {
					String var = selected.split(" ")[0];
					ListOf r = bioModel.getSBMLDocument().getModel().getListOfRules();
					for (int i = 0; i < bioModel.getSBMLDocument().getModel().getNumRules(); i++) {
						if ((((Rule) r.get(i)).isAssignment()) && ((Rule) r.get(i)).getVariable().equals(var)) {
							metaId = r.get(i).getMetaId();
						}
					}
				}
				ruleEditor("OK",metaId);
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
