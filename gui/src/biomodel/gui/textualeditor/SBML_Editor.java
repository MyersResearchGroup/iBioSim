package biomodel.gui.textualeditor;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import main.*;
import main.util.*;

import org.sbml.libsbml.*;

import analysis.*;
import biomodel.gui.textualeditor.Events;
import biomodel.gui.textualeditor.SBMLutilities;


/**
 * This is the SBML_Editor class. It takes in an sbml file and allows the user
 * to edit it by changing different fields displayed in a frame. It also
 * implements the ActionListener class, the MouseListener class, and the
 * KeyListener class which allows it to perform certain actions when buttons are
 * clicked on the frame, when one of the JList's items is double clicked, or
 * when text is entered into the model's ID.
 * 
 * @author Curtis Madsen
 */
public class SBML_Editor extends JPanel {

	private static final long serialVersionUID = 8236967001410906807L;

	private SBMLDocument document; // sbml document

	private String file; // SBML file

	private MutableBoolean dirty; // determines if any changes were made

	private AnalysisView reb2sac; // reb2sac options

	private Log log;

	private ArrayList<String> usedIDs;

	private Gui biosim;

	private String separator;

	private boolean paramsOnly;

	private String simDir;

	private String paramFile;

	private ArrayList<String> parameterChanges, elementChanges;

	private String refFile;

	private ModelPanel modelPanel;

	private InitialAssignments initialsPanel;

	private Rules rulesPanel;

	private Functions funcdefnPanel;

	/**
	 * Creates a new SBML_Editor and sets up the frame where the user can edit
	 * the SBML file given to this constructor.
	 */
	public SBML_Editor(String file, AnalysisView reb2sac, Log log, Gui biosim, String simDir, String paramFile) {
		this.reb2sac = reb2sac;
		paramsOnly = (reb2sac != null);
		if (paramsOnly) {
			parameterChanges = new ArrayList<String>();
			elementChanges = new ArrayList<String>();
		}
		this.log = log;
		this.biosim = biosim;
		this.simDir = simDir;
		this.paramFile = paramFile;
		if (paramFile != null) {
			try {
				Scanner scan = new Scanner(new File(paramFile));
				if (scan.hasNextLine()) {
					refFile = scan.nextLine();
				}
				scan.close();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to read parameter file.", "Error", JOptionPane.ERROR_MESSAGE);
				refFile = file;
			}
		}
		createSbmlFrame(file);
	}

	/**
	 * Private helper method that helps create the sbml frame.
	 */
	private void createSbmlFrame(String file) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		// intitializes the member variables
		if (!file.equals("")) {
			this.file = file;
		}
		else {
			this.file = null;
		}
		dirty = new MutableBoolean(false);

		// creates the sbml reader and reads the sbml file
		Model model;
		if (!file.equals("")) {
			document = Gui.readSBML(file);
			model = document.getModel();
			if (model.getId().equals("")) {
				String modelID = file.split(separator)[file.split(separator).length - 1];
				if (modelID.indexOf('.') >= 0) {
					modelID = modelID.substring(0, modelID.indexOf('.'));
				}
				model.setId(modelID);
				save(false, "", true, true);
			}
			//SBMLutilities.addRandomFunctions(document);
		}
		else {
			document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			model = document.createModel();
		}
		usedIDs = SBMLutilities.CreateListOfUsedIDs(document);
		ArrayList<String> getParams = new ArrayList<String>();
		if (paramsOnly) {
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
		}

		Compartments comp = new Compartments(biosim, document, usedIDs, dirty, paramsOnly, getParams, file, 
				parameterChanges, false, null);
		MySpecies spec = new MySpecies(biosim, document, usedIDs, dirty, paramsOnly, getParams, file, parameterChanges, false);
		Parameters param = new Parameters(biosim, document, usedIDs, dirty, paramsOnly, getParams, file, parameterChanges);
		Reactions reac = new Reactions(biosim, document, usedIDs, dirty, paramsOnly, getParams, file, parameterChanges);

		// adds the main panel to the frame and displays it
		JPanel mainPanelCenter = new JPanel(new GridLayout(2, 2));
		mainPanelCenter.add(comp);
		mainPanelCenter.add(spec);
		mainPanelCenter.add(reac);
		mainPanelCenter.add(param);

		modelPanel = new ModelPanel(document, dirty, paramsOnly);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(modelPanel, "North");
		mainPanel.add(mainPanelCenter, "Center");
		this.setLayout(new BorderLayout());

		JPanel defnPanel = createDefnFrame(model);
		JPanel rulesEventsPanel = createRuleFrame(model);
		comp.setPanels(initialsPanel, rulesPanel);
		funcdefnPanel.setPanels(initialsPanel, rulesPanel);
		spec.setPanels(initialsPanel, rulesPanel);
		param.setPanels(initialsPanel, rulesPanel);
		reac.setPanels(initialsPanel, rulesPanel);
		if (!paramsOnly) {
			JTabbedPane tab = new JTabbedPane();
			tab.addTab("Main Elements", mainPanel);
			tab.addTab("Definitions", defnPanel);
			// tab.addTab("Definitions/Types", defnPanel);
			tab.addTab("Initial Assignments/Rules/Constraints/Events", rulesEventsPanel);
			this.add(tab, "Center");
		}
		else {
			this.add(mainPanel, "Center");
		}
	}

	/**
	 * Private helper method to create definitions/types frame.
	 */
	private JPanel createDefnFrame(Model model) {
		funcdefnPanel = new Functions(document, usedIDs, dirty);
		Units unitdefnPanel = new Units(biosim, document, usedIDs, dirty);
		CompartmentTypes compTypePanel = new CompartmentTypes(biosim, document, usedIDs, dirty);
		SpeciesTypes specTypePanel = new SpeciesTypes(biosim, document, usedIDs, dirty);

		JPanel defnPanel = new JPanel(new GridLayout(2, 2));
		defnPanel.add(funcdefnPanel);
		defnPanel.add(unitdefnPanel);
		if (document.getLevel() < 3) {
			defnPanel.add(compTypePanel);
			defnPanel.add(specTypePanel);
		}
		return defnPanel;
	}

	/**
	 * Private helper method to create rules/events/constraints frame.
	 */
	private JPanel createRuleFrame(Model model) {
		initialsPanel = new InitialAssignments(biosim, document, dirty);
		rulesPanel = new Rules(biosim, document, dirty);
		Constraints constraintPanel = new Constraints(document, usedIDs, dirty);
		Events eventPanel = new Events(biosim, document, usedIDs, dirty, null);

		JPanel recPanel = new JPanel(new GridLayout(2, 2));
		recPanel.add(initialsPanel);
		recPanel.add(rulesPanel);
		recPanel.add(constraintPanel);
		recPanel.add(eventPanel);
		return recPanel;
	}

	public JScrollPane getElementsPanel() {
		if (paramsOnly) {
			Model m = document.getModel();
			ListOf e = m.getListOfConstraints();
			int consNum = (int) m.getNumConstraints();
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
					constraint.setMetaId(constraintId);
				}
				cons[i] = constraint.getMetaId();
			}
			e = m.getListOfRules();
			int rulNum = (int) m.getNumRules();
			String[] rul = new String[rulNum];
			for (int i = 0; i < rulNum; i++) {
				Rule rule = (Rule) e.get(i);
				if (rule.isAlgebraic()) {
					rul[i] = "0 = " + SBMLutilities.myFormulaToString(rule.getMath());
				}
				else if (rule.isAssignment()) {
					rul[i] = rule.getVariable() + " = " + SBMLutilities.myFormulaToString(rule.getMath());
				}
				else {
					rul[i] = "d( " + rule.getVariable() + " )/dt = " + SBMLutilities.myFormulaToString(rule.getMath());
				}
			}
			e = m.getListOfInitialAssignments();
			int initsNum = (int) m.getNumInitialAssignments();
			String[] inits = new String[initsNum];
			for (int i = 0; i < initsNum; i++) {
				inits[i] = ((InitialAssignment) e.get(i)).getSymbol() + " = "
						+ SBMLutilities.myFormulaToString(((InitialAssignment) e.get(i)).getMath());
			}
			e = m.getListOfEvents();
			int evNum = (int) m.getNumEvents();
			String[] ev = new String[evNum];
			for (int i = 0; i < evNum; i++) {
				if (((org.sbml.libsbml.Event) e.get(i)).isSetId()) {
					ev[i] = ((org.sbml.libsbml.Event) e.get(i)).getId();
				}
			}
			JPanel elements = new JPanel(new GridLayout(1, 4));
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
				elements.add(initial);
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
				elements.add(rules);
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
				elements.add(constaints);
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
				elements.add(events);
			}
			JPanel elementsPanel = new JPanel();
			((FlowLayout) elementsPanel.getLayout()).setAlignment(FlowLayout.LEFT);
			elementsPanel.add(elements);
			JScrollPane scroll = new JScrollPane();
			scroll.setViewportView(elements);
			return scroll;
		}
		else {
			return null;
		}
	}

	/**
	 * Save SBML file with a new name
	 */
	public void saveAs() {
		String simName = JOptionPane.showInputDialog(Gui.frame, "Enter Model ID:", "Model ID", JOptionPane.PLAIN_MESSAGE);
		if (simName != null && !simName.equals("")) {
			if (simName.length() > 4) {
				if (!simName.substring(simName.length() - 5).equals(".sbml") && !simName.substring(simName.length() - 4).equals(".xml")) {
					simName += ".xml";
				}
			}
			else {
				simName += ".xml";
			}
			String modelID = "";
			if (simName.length() > 4) {
				if (simName.substring(simName.length() - 5).equals(".sbml")) {
					modelID = simName.substring(0, simName.length() - 5);
				}
				else {
					modelID = simName.substring(0, simName.length() - 4);
				}
			}
			String oldId = document.getModel().getId();
			document.getModel().setId(modelID);
			document.getModel().setName(modelPanel.getModelName());
			String newFile = file;
			newFile = newFile.substring(0, newFile.length() - newFile.split(separator)[newFile.split(separator).length - 1].length()) + simName;
			try {
				log.addText("Saving SBML file as:\n" + newFile + "\n");
				SBMLWriter writer = new SBMLWriter();
				writer.writeSBML(document, newFile);
				JTabbedPane tab = biosim.getTab();
				for (int i = 0; i < tab.getTabCount(); i++) {
					if (tab.getTitleAt(i).equals(file.split(separator)[file.split(separator).length - 1])) {
						tab.setTitleAt(i, simName);
						tab.setComponentAt(i, new SBML_Editor(newFile, reb2sac, log, biosim, simDir, paramFile));
						tab.getComponentAt(i).setName("SBML Editor");
					}
				}
				biosim.addToTree(simName);
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to save sbml file.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
			}
			finally {
				document.getModel().setId(oldId);
			}
		}
	}

	public boolean isDirty() {
		return dirty.booleanValue();
	}

	public void setDirty(boolean dirty) {
		this.dirty.setValue(dirty);
	}

	public void setParamFileAndSimDir(String newParamFile, String newSimDir) {
		paramFile = newParamFile;
		simDir = newSimDir;
		file = newSimDir + separator + file.split(separator)[file.split(separator).length - 1];
	}

	/**
	 * Create the SBML file
	 */
	public void createSBML(String stem, String direct) {
		try {
			FileOutputStream out = new FileOutputStream(new File(paramFile));
			out.write((refFile + "\n").getBytes());
			for (String s : parameterChanges) {
				out.write((s + "\n").getBytes());
			}
			out.write(("\n").getBytes());
			for (String s : elementChanges) {
				out.write((s + "\n").getBytes());
			}
			out.close();
		}
		catch (IOException e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to save parameter file.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		try {
			if (!direct.equals(".")) {
				String[] d = direct.split("_");
				ArrayList<String> dd = new ArrayList<String>();
				for (int i = 0; i < d.length; i++) {
					if (!d[i].contains("=")) {
						String di = d[i];
						while (!d[i].contains("=")) {
							i++;
							di += "_" + d[i];
						}
						dd.add(di);
					}
					else {
						dd.add(d[i]);
					}
				}
				for (String di : dd) {
					if (di.contains("/")) {
						KineticLaw KL = document.getModel().getReaction(di.split("=")[0].split("/")[0]).getKineticLaw();
						ListOf p = KL.getListOfParameters();
						for (int i = 0; i < KL.getNumParameters(); i++) {
							Parameter param = ((Parameter) p.get(i));
							if (param.getId().equals(di.split("=")[0].split("/")[1])) {
								param.setValue(Double.parseDouble(di.split("=")[1]));
							}
						}
					}
					else {
						if (document.getModel().getParameter(di.split("=")[0]) != null) {
							document.getModel().getParameter(di.split("=")[0]).setValue(Double.parseDouble(di.split("=")[1]));
						}
						else {
							if (document.getModel().getSpecies(di.split("=")[0]).isSetInitialAmount()) {
								document.getModel().getSpecies(di.split("=")[0]).setInitialAmount(Double.parseDouble(di.split("=")[1]));
							}
							else {
								document.getModel().getSpecies(di.split("=")[0]).setInitialConcentration(Double.parseDouble(di.split("=")[1]));
							}
						}
					}
				}
			}
			direct = direct.replace("/", "-");
			if (direct.equals(".") && !stem.equals("")) {
				direct = "";
			}
			document.getModel().setName(modelPanel.getModelName());
			SBMLWriter writer = new SBMLWriter();
			writer.writeSBML(document, simDir + separator + stem + direct + separator + file.split(separator)[file.split(separator).length - 1]);
			SBMLDocument d = Gui.readSBML(simDir + separator + stem + direct + separator + file.split(separator)[file.split(separator).length - 1]);
			for (String s : elementChanges) {
				for (long i = d.getModel().getNumInitialAssignments() - 1; i >= 0; i--) {
					if (s.contains("=")) {
						String formula = SBMLutilities.myFormulaToString(((InitialAssignment) d.getModel().getListOfInitialAssignments().get(i))
								.getMath());
						String sFormula = s.substring(s.indexOf('=') + 1).trim();
						sFormula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(sFormula));
						sFormula = s.substring(0, s.indexOf('=') + 1) + " " + sFormula;
						if ((((InitialAssignment) d.getModel().getListOfInitialAssignments().get(i)).getSymbol() + " = " + formula).equals(sFormula)) {
							d.getModel().getListOfInitialAssignments().remove(i);
						}
					}
				}
				for (long i = d.getModel().getNumConstraints() - 1; i >= 0; i--) {
					if (d.getModel().getListOfConstraints().get(i).getMetaId().equals(s)) {
						d.getModel().getListOfConstraints().remove(i);
					}
				}
				for (long i = d.getModel().getNumEvents() - 1; i >= 0; i--) {
					if (d.getModel().getListOfEvents().get(i).getId().equals(s)) {
						d.getModel().getListOfEvents().remove(i);
					}
				}
				for (long i = d.getModel().getNumRules() - 1; i >= 0; i--) {
					if (s.contains("=")) {
						String formula = SBMLutilities.myFormulaToString(((Rule) d.getModel().getListOfRules().get(i)).getMath());
						String sFormula = s.substring(s.indexOf('=') + 1).trim();
						sFormula = SBMLutilities.myFormulaToString(SBMLutilities.myParseFormula(sFormula));
						sFormula = s.substring(0, s.indexOf('=') + 1) + " " + sFormula;
						Rule rule = (Rule) d.getModel().getListOfRules().get(i);
						String ruleFormula;
						if (rule.isAlgebraic()) {
							ruleFormula = "0 = " + formula;
						}
						else if (rule.isAssignment()) {
							ruleFormula = rule.getVariable() + " = " + formula;
						}
						else {
							ruleFormula = "d( " + rule.getVariable() + " )/dt = " + formula;
						}
						if (ruleFormula.equals(sFormula)) {
							d.getModel().getListOfRules().remove(i);
						}
					}
				}
			}
			writer = new SBMLWriter();
			writer.writeSBML(d, simDir + separator + stem + direct + separator + file.split(separator)[file.split(separator).length - 1]);
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to create sbml file.", "Error Creating File", JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	/**
	 * Saves the sbml file.
	 * 
	 * @param stem
	 */
	public void save(boolean run, String stem, boolean outputMessage, boolean ignoreSweep) {
		if (paramsOnly) {
			if (run) {
				ArrayList<String> sweepThese1 = new ArrayList<String>();
				ArrayList<ArrayList<Double>> sweep1 = new ArrayList<ArrayList<Double>>();
				ArrayList<String> sweepThese2 = new ArrayList<String>();
				ArrayList<ArrayList<Double>> sweep2 = new ArrayList<ArrayList<Double>>();
				for (String s : parameterChanges) {
					if (s.split(" ")[s.split(" ").length - 2].equals("Sweep")) {
						if ((s.split(" ")[s.split(" ").length - 1]).split(",")[3].replace(")", "").trim().equals("1")) {
							sweepThese1.add(s.split(" ")[0]);
							double start = Double.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[0].substring(1).trim());
							double stop = Double.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[1].trim());
							double step = Double.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[2].trim());
							ArrayList<Double> add = new ArrayList<Double>();
							for (double i = start; i <= stop; i += step) {
								add.add(i);
							}
							sweep1.add(add);
						}
						else {
							sweepThese2.add(s.split(" ")[0]);
							double start = Double.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[0].substring(1).trim());
							double stop = Double.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[1].trim());
							double step = Double.parseDouble((s.split(" ")[s.split(" ").length - 1]).split(",")[2].trim());
							ArrayList<Double> add = new ArrayList<Double>();
							for (double i = start; i <= stop; i += step) {
								add.add(i);
							}
							sweep2.add(add);
						}
					}
				}
				if (sweepThese1.size() == 0 && (sweepThese2.size() > 0)) {
					sweepThese1 = sweepThese2;
					sweepThese2 = new ArrayList<String>();
					sweep1 = sweep2;
					sweep2 = new ArrayList<ArrayList<Double>>();
				}
				if (sweepThese1.size() > 0 && !ignoreSweep) {
					ArrayList<AnalysisThread> threads = new ArrayList<AnalysisThread>();
					ArrayList<String> dirs = new ArrayList<String>();
					ArrayList<String> levelOne = new ArrayList<String>();
					int max = 0;
					for (ArrayList<Double> d : sweep1) {
						max = Math.max(max, d.size());
					}
					for (int j = 0; j < max; j++) {
						String sweep = "";
						for (int i = 0; i < sweepThese1.size(); i++) {
							int k = j;
							if (k >= sweep1.get(i).size()) {
								k = sweep1.get(i).size() - 1;
							}
							if (sweep.equals("")) {
								sweep += sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
							}
							else {
								sweep += "_" + sweepThese1.get(i) + "=" + sweep1.get(i).get(k);
							}
						}
						if (sweepThese2.size() > 0) {
							int max2 = 0;
							for (ArrayList<Double> d : sweep2) {
								max2 = Math.max(max2, d.size());
							}
							for (int l = 0; l < max2; l++) {
								String sweepTwo = sweep;
								for (int i = 0; i < sweepThese2.size(); i++) {
									int k = l;
									if (k >= sweep2.get(i).size()) {
										k = sweep2.get(i).size() - 1;
									}
									if (sweepTwo.equals("")) {
										sweepTwo += sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
									}
									else {
										sweepTwo += "_" + sweepThese2.get(i) + "=" + sweep2.get(i).get(k);
									}
								}
								new File(simDir + separator + stem + sweepTwo.replace("/", "-")).mkdir();
								createSBML(stem, sweepTwo);
								AnalysisThread thread = new AnalysisThread(reb2sac);
								thread.start(stem + sweepTwo.replace("/", "-"), false);
								threads.add(thread);
								dirs.add(sweepTwo.replace("/", "-"));
								reb2sac.emptyFrames();
							}
						}
						else {
							new File(simDir + separator + stem + sweep.replace("/", "-")).mkdir();
							createSBML(stem, sweep);
							AnalysisThread thread = new AnalysisThread(reb2sac);
							thread.start(stem + sweep.replace("/", "-"), false);
							threads.add(thread);
							dirs.add(sweep.replace("/", "-"));
							reb2sac.emptyFrames();
						}
						levelOne.add(sweep.replace("/", "-"));
					}
					new ConstraintTermThread(reb2sac).start(threads, dirs, levelOne, stem);
				}
				else {
					if (!stem.equals("")) {
						new File(simDir + separator + stem).mkdir();
					}
					createSBML(stem, ".");
					if (!stem.equals("")) {
						new AnalysisThread(reb2sac).start(stem, true);
					}
					else {
						new AnalysisThread(reb2sac).start(".", true);
					}
					reb2sac.emptyFrames();
				}
			}
			else {
				if (!stem.equals("")) {
					new File(simDir + separator + stem).mkdir();
				}
				createSBML(stem, ".");
			}
			dirty.setValue(false);
		}
		else {
			try {
				if (outputMessage) {
					log.addText("Saving SBML file:\n" + file + "\n");
				}
				document.getModel().setName(modelPanel.getModelName());
				/*
				 * for (int i=0;i<document.getModel().getNumEvents();i++) {
				 * System.out.println(i + " " +
				 * document.getModel().getEvent(i).getId()); }
				 */
				SBMLWriter writer = new SBMLWriter();
				if (!writer.writeSBML(document, file)) {
					JOptionPane.showMessageDialog(Gui.frame, "Unable to save sbml file.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
				}
				dirty.setValue(false);
				/*
				if (paramsOnly) {
					reb2sac.updateSpeciesList();
				}
				*/
			}
			catch (Exception e1) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable to save sbml file.", "Error Saving File", JOptionPane.ERROR_MESSAGE);
			}
			biosim.updateViews(file.split(separator)[file.split(separator).length - 1]);
		}
	}

	public void updateSBML(int tab, int tab2, String newFile) {
		SBML_Editor sbml = new SBML_Editor(newFile, reb2sac, log, biosim, simDir, paramFile);
		((JTabbedPane) (biosim.getTab().getComponentAt(tab))).setComponentAt(tab2, sbml);
		reb2sac.setSbml(sbml);
		//reb2sac.updateSpeciesList();
		for (int i = 0; i < ((JTabbedPane) biosim.getTab().getComponentAt(tab)).getTabCount(); i++) {
			Component c = ((JTabbedPane) (biosim.getTab().getComponentAt(tab))).getComponentAt(i);
			if (c instanceof SBML_Editor) {
				c.setName("SBML Editor");
			}
		}
	}

	public void updateSBML(int tab, int tab2) {
		updateSBML(tab, tab2, file);
	}

	/**
	 * Set the model ID
	 */
	public void setModelID(String modelID) {
		modelPanel.setModelID(modelID);
	}

	/**
	 * Set the file name
	 */
	public void setFile(String newFile) {
		file = newFile;
	}

	public String getRefFile() {
		return refFile;
	}

	public String getFile() {
		return file;
	}

	public void check() {
		SBMLutilities.check(file);
	}

	public ArrayList<String> getElementChanges() {
		return elementChanges;
	}
}
