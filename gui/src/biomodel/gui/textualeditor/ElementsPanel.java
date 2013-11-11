package biomodel.gui.textualeditor;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;

public class ElementsPanel extends JPanel { 

	private static final long serialVersionUID = 1L;
	private ArrayList<String> elementChanges;
	
	public ElementsPanel(SBMLDocument document,String paramFile) {
		super(new GridLayout(1, 4));
		elementChanges = new ArrayList<String>();
		ArrayList<String> usedIDs = SBMLutilities.CreateListOfUsedIDs(document);
		ArrayList<String> getParams = new ArrayList<String>();
		try {
			Scanner scan = new Scanner(new File(paramFile));
			if (scan.hasNextLine()) {
				scan.nextLine();
			}
			while (scan.hasNextLine()) {
				String s = scan.nextLine();
				if (s.trim().equals("")) {
					break;
				}
				getParams.add(s);
			}
			while (scan.hasNextLine()) {
				elementChanges.add(scan.nextLine());
			}
			scan.close();
		}
		catch (Exception e) {
		}
		Model m = document.getModel();
		ListOf e = m.getListOfConstraints();
		int consNum = (int) m.getConstraintCount();
		String[] cons = new String[(int) consNum];
		for (int i = 0; i < consNum; i++) {
			Constraint constraint = (Constraint) e.get(i);
			if (!constraint.isSetMetaId()) {
				String constraintId = "constraint0";
				int cn = 0;
				while (usedIDs.contains(constraintId)) {
					cn++;
					constraintId = "constraint" + cn;
				}
				usedIDs.add(constraintId);
				SBMLutilities.setMetaId(constraint, constraintId);
			}
			cons[i] = constraint.getMetaId();
		}
		e = m.getListOfRules();
		int rulNum = (int) m.getRuleCount();
		String[] rul = new String[rulNum];
		for (int i = 0; i < rulNum; i++) {
			Rule rule = (Rule) e.get(i);
			if (rule.isAlgebraic()) {
				rul[i] = "0 = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else if (rule.isAssignment()) {
				rul[i] = SBMLutilities.getVariable(rule) + " = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else {
				rul[i] = "d( " + SBMLutilities.getVariable(rule) + " )/dt = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			if (elementChanges.contains(rul[i])) {
				elementChanges.remove(rul[i]);
				elementChanges.add(rule.getMetaId());
			}
		}
		e = m.getListOfInitialAssignments();
		int initsNum = (int) m.getInitialAssignmentCount();
		String[] inits = new String[initsNum];
		for (int i = 0; i < initsNum; i++) {
			inits[i] = ((InitialAssignment) e.get(i)).getSymbol() + " = "
					+ SBMLutilities.myFormulaToString(((InitialAssignment) e.get(i)).getMath());
		}
		e = m.getListOfEvents();
		int evNum = (int) m.getEventCount();
		String[] ev = new String[evNum];
		for (int i = 0; i < evNum; i++) {
			if (((org.sbml.jsbml.Event) e.get(i)).isSetId()) {
				ev[i] = ((org.sbml.jsbml.Event) e.get(i)).getId();
			}
		}
		if (initsNum > 0) {
			JPanel initsPanel = new JPanel(new GridLayout(initsNum + 1, 1));
			initsPanel.add(new JLabel("Initial Assignments:"));
			for (int i = 0; i < inits.length; i++) {
				JCheckBox temp = new JCheckBox(inits[i]);
				if (!elementChanges.contains(inits[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				initsPanel.add(temp);
			}
			JPanel initial = new JPanel();
			((FlowLayout) initial.getLayout()).setAlignment(FlowLayout.LEFT);
			initial.add(initsPanel);
			this.add(initial);
		}
		if (rulNum > 0) {
			JPanel rulPanel = new JPanel(new GridLayout(rulNum + 1, 1));
			rulPanel.add(new JLabel("Rules:"));
			for (int i = 0; i < rul.length; i++) {
				JCheckBox temp = new JCheckBox(rul[i]);
				if (!elementChanges.contains(rul[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				rulPanel.add(temp);
			}
			JPanel rules = new JPanel();
			((FlowLayout) rules.getLayout()).setAlignment(FlowLayout.LEFT);
			rules.add(rulPanel);
			this.add(rules);
		}
		if (consNum > 0) {
			JPanel consPanel = new JPanel(new GridLayout(consNum + 1, 1));
			consPanel.add(new JLabel("Constaints:"));
			for (int i = 0; i < cons.length; i++) {
				JCheckBox temp = new JCheckBox(cons[i]);
				if (!elementChanges.contains(cons[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				consPanel.add(temp);
			}
			JPanel constaints = new JPanel();
			((FlowLayout) constaints.getLayout()).setAlignment(FlowLayout.LEFT);
			constaints.add(consPanel);
			this.add(constaints);
		}
		if (evNum > 0) {
			JPanel evPanel = new JPanel(new GridLayout(evNum + 1, 1));
			evPanel.add(new JLabel("Events:"));
			for (int i = 0; i < ev.length; i++) {
				JCheckBox temp = new JCheckBox(ev[i]);
				if (!elementChanges.contains(ev[i])) {
					temp.setSelected(true);
				}
				temp.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JCheckBox clicked = ((JCheckBox) e.getSource());
						if (clicked.isSelected()) {
							elementChanges.remove(clicked.getText());
						}
						else {
							elementChanges.add(clicked.getText());
						}
					}
				});
				evPanel.add(temp);
			}
			JPanel events = new JPanel();
			((FlowLayout) events.getLayout()).setAlignment(FlowLayout.LEFT);
			events.add(evPanel);
			this.add(events);
		}
	}

	public ArrayList<String> getElementChanges() {
		return elementChanges;
	}

}
