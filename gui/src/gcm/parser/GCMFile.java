package gcm.parser;

import gcm.gui.Grid;
import gcm.network.AbstractionEngine;
import gcm.network.GeneticNetwork;
import gcm.network.Promoter;
import gcm.network.SpeciesInterface;
import gcm.util.GlobalConstants;
import gcm.util.UndoManager;
import gcm.util.Utility;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.BoundingBox;
import org.sbml.libsbml.CompExtension;
import org.sbml.libsbml.CompModelPlugin;
import org.sbml.libsbml.CompSBMLDocumentPlugin;
import org.sbml.libsbml.CompSBasePlugin;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentGlyph;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Dimensions;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.ExternalModelDefinition;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.LayoutExtension;
import org.sbml.libsbml.LayoutModelPlugin;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.ReactionGlyph;
import org.sbml.libsbml.ReplacedElement;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesGlyph;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.Submodel;
import org.sbml.libsbml.TextGlyph;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.Layout;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.libsbml;

import sbmleditor.MySpecies;
import sbmleditor.Reactions;
import sbmleditor.SBMLutilities;
import util.MutableString;

import lpn.parser.LhpnFile;
import main.Gui;
import main.Log;


/**
 * This class describes a GCM file
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class GCMFile {

	private String separator;

	private String filename = null;

	private UndoManager undoManager;

	public GCMFile(String path) {
		//gcm2sbml = new GCM2SBML(this);
		undoManager = new UndoManager();
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.path = path;
		isWithinCompartment = false;
		enclosingCompartment = "";
		grid = new Grid();
		compartments = new HashMap<String, Properties>();
		//loadDefaultParameters();
	}
	
	public void createSBMLDocument(String modelId) {
		sbml = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		Model m = sbml.createModel();
		sbml.setModel(m);
		m.setId(modelId);
		Compartment c = m.createCompartment();
		c.setId("default");
		c.setSize(1);
		c.setSpatialDimensions(3);
		c.setConstant(true);
		SBMLutilities.addRandomFunctions(sbml);
		sbmlFile = modelId + ".xml";
		sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
		sbml.setPkgRequired("layout", false); 
		sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
		sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
		sbml.setPkgRequired("comp", true); 
		sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
		sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
	}

	public boolean getIsWithinCompartment() {
		return isWithinCompartment;
	}
	
	public void setIsWithinCompartment(boolean isWithinCompartment) {
		this.isWithinCompartment = isWithinCompartment;
	}
	
	public String getSBMLFile() {
		return sbmlFile;
	}

	public void setSBMLFile(String file) {
		sbmlFile = file;
	}

	public void setUsedIDs(ArrayList<String >usedIDs) {
		this.usedIDs = usedIDs;
	}

	public ArrayList<String> getUsedIDs() {
		return usedIDs;
	}

	public MySpecies getSpeciesPanel() {
		return speciesPanel;
	}

	public void setSpeciesPanel(MySpecies speciesPanel) {
		this.speciesPanel = speciesPanel;
	}

	public Reactions getReactionPanel() {
		return reactionPanel;
	}

	public void setReactionPanel(Reactions reactionPanel) {
		this.reactionPanel = reactionPanel;
	}

	public SBMLDocument getSBMLDocument() {
		return sbml;
	}

	public LayoutModelPlugin getSBMLLayout() {
		return sbmlLayout;
	}

	public CompSBMLDocumentPlugin getSBMLComp() {
		return sbmlComp;
	}

	public CompModelPlugin getSBMLCompModel() {
		return sbmlCompModel;
	}

	public void setSBMLDocument(SBMLDocument sbmlDoc) {
		sbml = sbmlDoc;
		usedIDs = SBMLutilities.CreateListOfUsedIDs(sbml);
	}

	public void setSBMLLayout(LayoutModelPlugin sbmlLayout) {
		this.sbmlLayout = sbmlLayout;
	}

	public void setSBMLComp(CompSBMLDocumentPlugin sbmlComp) {
		this.sbmlComp = sbmlComp;
	}

	public void setSBMLCompModel(CompModelPlugin sbmlCompModel) {
		this.sbmlCompModel = sbmlCompModel;
	}
	
	public void reloadSBMLFile() {
		if (sbml==null) {
			sbml = Gui.readSBML(path + separator + sbmlFile);
			sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
			sbml.setPkgRequired("layout", false); 
			sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
			sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
			sbml.setPkgRequired("comp", true); 
			sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
			sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		} else {
			sbml.setModel(Gui.readSBML(path + separator + sbmlFile).getModel());
			sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
			sbml.setPkgRequired("layout", false); 
			sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
			sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
			sbml.setPkgRequired("comp", true); 
			sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
			sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		}
	}

	public void createLogicalModel(final String filename, final Log log, final Gui biosim,
			final String lpnName) {
		try {
			new File(filename + ".temp").createNewFile();
		}
		catch (IOException e2) {
		}
		final JFrame naryFrame = new JFrame("Thresholds");
		WindowListener w = new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				naryFrame.dispose();
			}

			public void windowOpened(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}
		};
		naryFrame.addWindowListener(w);

		JTabbedPane naryTabs = new JTabbedPane();
		ArrayList<JPanel> specProps = new ArrayList<JPanel>();
		final ArrayList<JTextField> texts = new ArrayList<JTextField>();
		final ArrayList<JList> consLevel = new ArrayList<JList>();
		final ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
		final ArrayList<String> specs = new ArrayList<String>();
		for (String spec : getSpecies()) {
			specs.add(spec);
			JPanel newPanel1 = new JPanel(new GridLayout(1, 2));
			JPanel newPanel2 = new JPanel(new GridLayout(1, 2));
			JPanel otherLabel = new JPanel();
			otherLabel.add(new JLabel(spec + " Amount:"));
			newPanel2.add(otherLabel);
			consLevel.add(new JList());
			conLevel.add(new Object[0]);
			consLevel.get(consLevel.size() - 1).setListData(new Object[0]);
			conLevel.set(conLevel.size() - 1, new Object[0]);
			JScrollPane scroll = new JScrollPane();
			scroll.setPreferredSize(new Dimension(260, 100));
			scroll.setViewportView(consLevel.get(consLevel.size() - 1));
			JPanel area = new JPanel();
			area.add(scroll);
			newPanel2.add(area);
			JPanel addAndRemove = new JPanel();
			JTextField adding = new JTextField(15);
			texts.add(adding);
			JButton Add = new JButton("Add");
			JButton Remove = new JButton("Remove");
			Add.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int number = Integer.parseInt(e.getActionCommand().substring(3,
							e.getActionCommand().length()));
					try {
						int get = Integer.parseInt(texts.get(number).getText().trim());
						if (get <= 0) {
							JOptionPane.showMessageDialog(naryFrame,
									"Amounts Must Be Positive Integers.", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
						else {
							JList add = new JList();
							Object[] adding = { "" + get };
							add.setListData(adding);
							add.setSelectedIndex(0);
							Object[] sort = util.Utility.add(conLevel.get(number),
									consLevel.get(number), add, false, null, null, null, null,
									null, null, naryFrame);
							int in;
							for (int out = 1; out < sort.length; out++) {
								int temp = Integer.parseInt((String) sort[out]);
								in = out;
								while (in > 0 && Integer.parseInt((String) sort[in - 1]) >= temp) {
									sort[in] = sort[in - 1];
									--in;
								}
								sort[in] = temp + "";
							}
							conLevel.set(number, sort);
						}
					}
					catch (Exception e1) {
						JOptionPane.showMessageDialog(naryFrame,
								"Amounts Must Be Positive Integers.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			Add.setActionCommand("Add" + (consLevel.size() - 1));
			Remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int number = Integer.parseInt(e.getActionCommand().substring(6,
							e.getActionCommand().length()));
					conLevel.set(number, util.Utility
							.remove(consLevel.get(number), conLevel.get(number)));
				}
			});
			Remove.setActionCommand("Remove" + (consLevel.size() - 1));
			addAndRemove.add(adding);
			addAndRemove.add(Add);
			addAndRemove.add(Remove);
			JPanel newnewPanel = new JPanel(new BorderLayout());
			newnewPanel.add(newPanel1, "North");
			newnewPanel.add(newPanel2, "Center");
			newnewPanel.add(addAndRemove, "South");
			specProps.add(newnewPanel);
			naryTabs.addTab(spec + " Properties", specProps.get(specProps.size() - 1));
		}

		JButton naryRun = new JButton("Create");
		JButton naryClose = new JButton("Cancel");
		naryRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				flattenGCM();
				convertToLHPN(specs, conLevel, new MutableString()).save(filename);
				log.addText("Saving GCM file as LPN:\n" + path + separator + lpnName + "\n");
				biosim.addToTree(lpnName);
				naryFrame.dispose();
				new File(filename + ".temp").delete();
			}
		});
		naryClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				naryFrame.dispose();
				new File(filename + ".temp").delete();
			}
		});
		JPanel naryRunPanel = new JPanel();
		naryRunPanel.add(naryRun);
		naryRunPanel.add(naryClose);

		JPanel naryPanel = new JPanel(new BorderLayout());
		naryPanel.add(naryTabs, "Center");
		naryPanel.add(naryRunPanel, "South");

		naryFrame.setContentPane(naryPanel);
		naryFrame.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe) {
			screenSize = new Dimension(640, 480);
		}
		Dimension frameSize = naryFrame.getSize();

		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		int x = screenSize.width / 2 - frameSize.width / 2;
		int y = screenSize.height / 2 - frameSize.height / 2;
		naryFrame.setLocation(x, y);
		naryFrame.setResizable(false);
		naryFrame.setVisible(true);
	}

	private ArrayList<String> copyArray(ArrayList<String> original) {
		ArrayList<String> copy = new ArrayList<String>();
		for (String element : original) {
			copy.add(element);
		}
		return copy;
	}
	
	private double parseValue(String value) {
		if (value == null) return 0.0;
		if (value.contains("/")) {
			String[] parts = value.split("/");
			return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
		}
		else {
			return Double.parseDouble(value);
		}
	}

	public LhpnFile convertToLHPN(ArrayList<String> specs, ArrayList<Object[]> conLevel, MutableString lpnProperty) {
		GCMParser parser = new GCMParser(this, false);
		GeneticNetwork network = parser.buildNetwork();
		network.markAbstractable();
		AbstractionEngine abs = network.createAbstractionEngine();
		HashMap<String, ArrayList<String>> infl = new HashMap<String, ArrayList<String>>();
		// TODO: THIS NEEDS TO BE UPDATED
		/*
		for (String influence : influences.keySet()) {
			if (influences.get(influence).get(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
				String input = getInput(influence);
				String output = getOutput(influence);
				if (infl.containsKey(output)) {
					infl.get(output).add("act:" + input + ":" + influence);
				}
				else {
					ArrayList<String> out = new ArrayList<String>();
					out.add("act:" + input + ":" + influence);
					infl.put(output, out);
				}
			}
			else if (influences.get(influence).get(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION)) {
				String input = getInput(influence);
				String output = getOutput(influence);
				if (infl.containsKey(output)) {
					infl.get(output).add("rep:" + input + ":" + influence);
				}
				else {
					ArrayList<String> out = new ArrayList<String>();
					out.add("rep:" + input + ":" + influence);
					infl.put(output, out);
				}
			}
		}
		*/
		ArrayList<String> biochemical = getBiochemicalSpecies();
		LhpnFile LHPN = new LhpnFile();
		for (int i = 0; i < specs.size(); i++) {
			double initial = parseValue(getParameter(GlobalConstants.INITIAL_STRING));
			double selectedThreshold = 0;
			try {
				if (sbml.getModel().getSpecies(specs.get(i)).isSetInitialAmount()) {
					initial = sbml.getModel().getSpecies(specs.get(i)).getInitialAmount();
				} else {
					initial = sbml.getModel().getSpecies(specs.get(i)).getInitialConcentration();
				}
			}
			catch (Exception e) {
			}
			double difference = Math.abs(selectedThreshold - initial);
			for (Object threshold : conLevel.get(i)) {
				double thisThreshold = Double.parseDouble((String) threshold);
				double diff = Math.abs(thisThreshold - initial);
				if (diff < difference) {
					difference = diff;
					selectedThreshold = thisThreshold;
				}
			}
			LHPN.addInteger(specs.get(i), "" + ((int) selectedThreshold));
		}
		for (String input : getInputSpecies()) {
			double value;
			try {
				if (sbml.getModel().getSpecies(input).isSetInitialAmount()) {
					value = sbml.getModel().getSpecies(input).getInitialAmount();
				} else {
					value = sbml.getModel().getSpecies(input).getInitialConcentration();
				}
			}
			catch (Exception e) {
				value = 0;
			}
			LHPN.addInteger(input, "" + value);
		}
		for (int i = 0; i < specs.size(); i++) {
			if (!biochemical.contains(specs.get(i)) && !getInputSpecies().contains(specs.get(i))) {
				int placeNum = 0;
				int transNum = 0;
				String previousPlaceName = specs.get(i) + placeNum;
				placeNum++;
				if (LHPN.getIntegers().get(specs.get(i)).equals("0")) {
					LHPN.addPlace(previousPlaceName, true);
				}
				else {
					LHPN.addPlace(previousPlaceName, false);
				}
				String number = "0";
				for (Object threshold : conLevel.get(i)) {
					if (LHPN.getIntegers().get(specs.get(i)).equals("" + ((int) Double.parseDouble((String) threshold)))) {
						LHPN.addPlace(specs.get(i) + placeNum, true);
					}
					else {
						LHPN.addPlace(specs.get(i) + placeNum, false);
					}
					if (!(getSpeciesType(specs.get(i)).equals(GlobalConstants.INPUT))) {
						ArrayList<String> proms = new ArrayList<String>();
						Double global_np = parseValue(getParameter(GlobalConstants.STOICHIOMETRY_STRING));
						Double global_kd = parseValue(getParameter(GlobalConstants.KDECAY_STRING));
						Double np = global_np;
						Double kd = global_kd;
						for (Promoter p : network.getPromoters().values()) {
							for (SpeciesInterface s : p.getOutputs()) {
								if (s.getId().equals(specs.get(i))) {
									proms.add(p.getId());
								}
							}
							
						}
						String rate = "";
						for (String promoter : proms) {
							String promRate = abs.abstractOperatorSite(network.getPromoters().get(promoter));
							for (String species : getSpecies()) {
								if (promRate.contains(species) && !LHPN.getIntegers().keySet().contains(species)) {
									double value;
									try {
										if (sbml.getModel().getSpecies(species).isSetInitialAmount()) {
											value = sbml.getModel().getSpecies(species).getInitialAmount();
										} else {
											value = sbml.getModel().getSpecies(species).getInitialConcentration();
										}
									}
									catch (Exception e) {
										value = 0;
									}
									LHPN.addInteger(species, "" + value);
								}
							}
							if (rate.equals("")) {
								rate = "(" + promRate + ")";
							}
							else {
								rate += "+(" + promRate + ")";
							}
						}
						if (!rate.equals("")) {
							LHPN.addTransition(specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(previousPlaceName, specs.get(i) + "_trans" + transNum);
							LHPN.addMovement(specs.get(i) + "_trans" + transNum, specs.get(i) + placeNum);
							LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), (String) threshold);
							LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, np + "*((" + rate + ")/" + "(" + threshold + "-" + number
									+ "))");
							transNum++;
						}
						LHPN.addTransition(specs.get(i) + "_trans" + transNum);
						LHPN.addMovement(specs.get(i) + placeNum, specs.get(i) + "_trans" + transNum);
						LHPN.addMovement(specs.get(i) + "_trans" + transNum, previousPlaceName);
						LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), number);
						String specExpr = specs.get(i);
						if (network.getSpecies().get(specs.get(i)).isSequesterable()) {
							specExpr = abs.sequesterSpecies(specs.get(i), 0, false);
						}
						else if (network.getComplexMap().containsKey(specs.get(i))) {
							specExpr = abs.abstractComplex(specs.get(i), 0, false);
						}
						if (!specExpr.equals(specs.get(i))) {
							specExpr = "(" + specExpr + ")";
						}
						kd = global_kd;
						if (isSpeciesDegradable(specs.get(i))) {
							Reaction reaction = sbml.getModel().getReaction("Degradation_"+specs.get(i));
							LocalParameter param = reaction.getKineticLaw().getLocalParameter(GlobalConstants.KDECAY_STRING);
							if (param != null) {
								kd = param.getValue();
							}
						}
						LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "(" + specExpr + "*" + kd + ")/" + "(" + threshold + "-" + number
								+ ")");
						transNum++;
					}
					previousPlaceName = specs.get(i) + placeNum;
					placeNum++;
					number = (String) threshold;
				}
			}
		}
		if (lpnProperty.getString() != null) {
			ArrayList<String> sortedSpecies = new ArrayList<String>();
			for (String s : getSpecies()) {
				sortedSpecies.add(s);
			}
			int i, j;
			String index;
			for (i = 1; i < sortedSpecies.size(); i++) {
				index = sortedSpecies.get(i);
				j = i;
				while ((j > 0) && sortedSpecies.get(j - 1).length() < index.length()) {
					sortedSpecies.set(j, sortedSpecies.get(j - 1));
					j--;
				}
				sortedSpecies.set(j, index);
			}
			for (String s : sortedSpecies) {
				String replace = null;
				if (network.getSpecies().get(s).isSequesterable()) {
					replace = abs.sequesterSpecies(s, 0, false);
				}
				else if (network.getComplexMap().containsKey(s)) {
					replace = abs.abstractComplex(s, 0, false);
				}
				if (replace != null && lpnProperty.getString().contains(s)) {
					replace = "(" + replace + ")";
					lpnProperty.setString(lpnProperty.getString().replaceAll(s, replace));
				}
			}
			LHPN.addProperty(lpnProperty.getString());
		}
		return LHPN;
	}

	public void createCondition(String s,int condition) {
		for (int i = 0; i < sbml.getModel().getNumConstraints(); i++) {
			if (sbml.getModel().getConstraint(i).isSetMetaId() &&
				sbml.getModel().getConstraint(i).getMetaId().equals("Condition_"+condition)) return;
		}
		s = s.replace("<="," less than or equal ");
		s = s.replace(">="," greater than or equal ");
		s = s.replace("<"," less than ");
		s = s.replace(">"," greater than ");
		s = s.replace("&"," and ");
		s = s.replace("|"," or ");
		XMLNode xmlNode = XMLNode.convertStringToXMLNode("<message><p xmlns=\"http://www.w3.org/1999/xhtml\">"
				+ s.trim() + "</p></message>");
		Constraint c = sbml.getModel().createConstraint();
		c.setMetaId("Condition_"+condition);
		c.setMath(SBMLutilities.myParseFormula("true"));
		c.setMessage(xmlNode);
	}

	public void createGlobalParameter(String global,String value) {
		if (!global.equals("") && sbml.getModel().getParameter(global)==null) {
			Parameter parameter = sbml.getModel().createParameter();
			parameter.setId(global);
			if (CompatibilityFixer.getGuiName(global)!=null) 
				parameter.setName(CompatibilityFixer.getGuiName(global));
			parameter.setValue(Double.parseDouble(value));
			parameter.setConstant(true);
		}
	}
	
	public void createSpeciesFromGCM(String s,Properties property) {
		Species species = sbml.getModel().getSpecies(s);
		if (species==null) {
			species = sbml.getModel().createSpecies();
			species.setId(s);
			if (enclosingCompartment.equals("")) {
				species.setCompartment(sbml.getModel().getCompartment(0).getId());
			} else {
				species.setCompartment(enclosingCompartment);
			}
			species.setBoundaryCondition(false);
			species.setConstant(false);
			species.setInitialAmount(0);
			species.setHasOnlySubstanceUnits(true);
		}
		if (property.containsKey(GlobalConstants.INITIAL_STRING)) {
			String initialString = property.getProperty(GlobalConstants.INITIAL_STRING);
			if (Utility.isValid(initialString, Utility.NUMstring)) {
				species.setInitialAmount(Double.parseDouble(initialString));
			} 
			else if (Utility.isValid(initialString, Utility.CONCstring)) {
				species.setInitialConcentration(Double.parseDouble(initialString.substring(1,initialString.length()-1)));
			} 
		}
		if (property.containsKey(GlobalConstants.TYPE)) {
			species.setAnnotation(GlobalConstants.TYPE + "=" + property.getProperty(GlobalConstants.TYPE)
					.replace("diffusible","").replace("constitutive",""));
		} else {
			species.setAnnotation(GlobalConstants.TYPE + "=" + GlobalConstants.INTERNAL);
		}
		if (property.containsKey(GlobalConstants.SBOL_RBS) &&
				property.containsKey(GlobalConstants.SBOL_ORF)) {
			species.appendAnnotation("," + GlobalConstants.SBOL_RBS + "=" + 
				property.getProperty(GlobalConstants.SBOL_RBS) + "," + 
				GlobalConstants.SBOL_ORF + "=" + property.getProperty(GlobalConstants.SBOL_ORF));
		} else if (property.containsKey(GlobalConstants.SBOL_RBS)) {
			species.appendAnnotation("," + GlobalConstants.SBOL_RBS + "=" + 
					property.getProperty(GlobalConstants.SBOL_RBS));
		} else if (property.containsKey(GlobalConstants.SBOL_ORF)) {
			species.appendAnnotation("," + GlobalConstants.SBOL_ORF + "=" + 
					property.getProperty(GlobalConstants.SBOL_ORF));
		}
		double kd = -1;
		if (property.containsKey(GlobalConstants.KDECAY_STRING)) {
			kd = Double.parseDouble(property.getProperty(GlobalConstants.KDECAY_STRING));
		} 
		if (kd != 0) {
			createDegradationReaction(s,kd);
		} 
		if (property.containsKey(GlobalConstants.TYPE) && 
			property.getProperty(GlobalConstants.TYPE).contains("diffusible")) {
			createDiffusionReaction(s,property.getProperty(GlobalConstants.MEMDIFF_STRING));
		}
		if (property.containsKey(GlobalConstants.TYPE) && 
				property.getProperty(GlobalConstants.TYPE).contains("constitutive")) {
			createConstitutiveReaction(s);
		}
	}
	
	public void createPromoterFromGCM(String s,Properties property) {
		if (sbml.getModel().getSpecies(s)==null) {
			Species species = sbml.getModel().createSpecies();
			species.setId(s);
			if (property != null && property.containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
				species.setInitialAmount(Double.parseDouble(property.getProperty(GlobalConstants.PROMOTER_COUNT_STRING)));
			} else {
				species.setInitialAmount(sbml.getModel().getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue());
			} 
			if (enclosingCompartment.equals("")) {
				species.setCompartment(sbml.getModel().getCompartment(0).getId());
			} else {
				species.setCompartment(enclosingCompartment);
			}
			species.setBoundaryCondition(false);
			species.setConstant(false);
			species.setHasOnlySubstanceUnits(true);
			species.setAnnotation(GlobalConstants.TYPE + "=" + GlobalConstants.PROMOTER);
			if (property.containsKey(GlobalConstants.SBOL_PROMOTER) &&
					property.containsKey(GlobalConstants.SBOL_TERMINATOR)) {
				species.appendAnnotation(GlobalConstants.SBOL_PROMOTER + "=" + 
					property.getProperty(GlobalConstants.SBOL_PROMOTER) + "," +
					GlobalConstants.SBOL_TERMINATOR + "=" + property.getProperty(GlobalConstants.SBOL_TERMINATOR));
			} else if (property.containsKey(GlobalConstants.SBOL_PROMOTER)) {
				species.appendAnnotation(GlobalConstants.SBOL_PROMOTER + "=" + 
						property.getProperty(GlobalConstants.SBOL_PROMOTER));
			} else if (property.containsKey(GlobalConstants.SBOL_TERMINATOR)) {
				species.appendAnnotation(GlobalConstants.SBOL_TERMINATOR + "=" + 
						property.getProperty(GlobalConstants.SBOL_TERMINATOR));
			}
			createProductionReaction(s,property.getProperty(GlobalConstants.ACTIVATED_STRING),
					property.getProperty(GlobalConstants.STOICHIOMETRY_STRING),
					property.getProperty(GlobalConstants.OCR_STRING),
					property.getProperty(GlobalConstants.KBASAL_STRING),
					property.getProperty(GlobalConstants.RNAP_BINDING_STRING),
					property.getProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING));
		}
	} 
	
	public Layout createLayout() {
		if (sbmlLayout==null) {
			sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
			sbml.setPkgRequired("layout", false); 
			sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
		}
		Layout layout;
		if (sbmlLayout.getLayout("iBioSim")==null) {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		} else {
			layout = sbmlLayout.getLayout("iBioSim");
		}
		return layout;
	}
	
	public void placeSpecies(String s,Double x,Double y,Double h,Double w) {
		Layout layout = sbmlLayout.getLayout("iBioSim");
		SpeciesGlyph speciesGlyph;
		if (layout.getSpeciesGlyph(s)!=null) {
			speciesGlyph = layout.getSpeciesGlyph(s);
		} else {
			speciesGlyph = layout.createSpeciesGlyph();
			speciesGlyph.setId(s);
			speciesGlyph.setSpeciesId(s);
		}
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setX(x);
		boundingBox.setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		speciesGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(s)!=null) {
			textGlyph = layout.getTextGlyph(s);
		} else {
			textGlyph = layout.createTextGlyph();
		}
		textGlyph.setId(s);
		textGlyph.setGraphicalObjectId(s);
		textGlyph.setText(s);
		textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
	}

	public void placeReaction(String s,Double x,Double y,Double h,Double w) {
		Layout layout = sbmlLayout.getLayout("iBioSim");
		ReactionGlyph reactionGlyph;
		if (layout.getReactionGlyph(s)!=null) {
			reactionGlyph = layout.getReactionGlyph(s);
		} else {
			reactionGlyph = layout.createReactionGlyph();
		}
		reactionGlyph.setId(s);
		reactionGlyph.setReactionId(s);
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setX(x);
		boundingBox.setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		reactionGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(s)!=null) {
			textGlyph = layout.getTextGlyph(s);
		} else {
			textGlyph = layout.createTextGlyph();
		}
		textGlyph.setId(s);
		textGlyph.setGraphicalObjectId(s);
		textGlyph.setText(s);
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
	}

	public void placeCompartment(String s,Double x,Double y,Double h,Double w) {
		Layout layout = sbmlLayout.getLayout("iBioSim");
		CompartmentGlyph compartmentGlyph;
		if (layout.getCompartmentGlyph(s)!=null) {
			compartmentGlyph = layout.getCompartmentGlyph(s);
		} else {
			compartmentGlyph = layout.createCompartmentGlyph();
		}
		compartmentGlyph.setId(s);
		compartmentGlyph.setCompartmentId(s);
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setX(x);
		boundingBox.setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		compartmentGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(s)!=null) {
			textGlyph = layout.getTextGlyph(s);
		} else {
			textGlyph = layout.createTextGlyph();
		}
		textGlyph.setId(s);
		textGlyph.setGraphicalObjectId(s);
		textGlyph.setText(s);
		textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox());
	}
	
	public void updateLayoutDimensions() {
		Layout layout = sbmlLayout.getLayout("iBioSim");
		for (long i=0; i<layout.getNumCompartmentGlyphs(); i++ ) {
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(i);
			if (compartmentGlyph.getBoundingBox().getPosition().getXOffset() +
			    compartmentGlyph.getBoundingBox().getDimensions().getWidth() >
				layout.getDimensions().getWidth()) {
					layout.getDimensions().setWidth(compartmentGlyph.getBoundingBox().getPosition().getXOffset() +
							compartmentGlyph.getBoundingBox().getDimensions().getWidth());
			}
			if (compartmentGlyph.getBoundingBox().getPosition().getYOffset() +
				    compartmentGlyph.getBoundingBox().getDimensions().getHeight() >
					layout.getDimensions().getHeight()) {
						layout.getDimensions().setHeight(compartmentGlyph.getBoundingBox().getPosition().getYOffset() +
								compartmentGlyph.getBoundingBox().getDimensions().getHeight());
			}
		}
		for (long i=0; i<layout.getNumSpeciesGlyphs(); i++ ) {
			SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(i);
			if (speciesGlyph.getBoundingBox().getPosition().getXOffset() +
			    speciesGlyph.getBoundingBox().getDimensions().getWidth() >
				layout.getDimensions().getWidth()) {
					layout.getDimensions().setWidth(speciesGlyph.getBoundingBox().getPosition().getXOffset() +
							speciesGlyph.getBoundingBox().getDimensions().getWidth());
			}
			if (speciesGlyph.getBoundingBox().getPosition().getYOffset() +
				    speciesGlyph.getBoundingBox().getDimensions().getHeight() >
					layout.getDimensions().getHeight()) {
						layout.getDimensions().setHeight(speciesGlyph.getBoundingBox().getPosition().getYOffset() +
								speciesGlyph.getBoundingBox().getDimensions().getHeight());
			}
		}
		for (long i=0; i<layout.getNumReactionGlyphs(); i++ ) {
			ReactionGlyph reactionGlyph = layout.getReactionGlyph(i);
			if (reactionGlyph.getBoundingBox().getPosition().getXOffset() +
			    reactionGlyph.getBoundingBox().getDimensions().getWidth() >
				layout.getDimensions().getWidth()) {
					layout.getDimensions().setWidth(reactionGlyph.getBoundingBox().getPosition().getXOffset() +
							reactionGlyph.getBoundingBox().getDimensions().getWidth());
			}
			if (reactionGlyph.getBoundingBox().getPosition().getYOffset() +
				    reactionGlyph.getBoundingBox().getDimensions().getHeight() >
					layout.getDimensions().getHeight()) {
						layout.getDimensions().setHeight(reactionGlyph.getBoundingBox().getPosition().getYOffset() +
								reactionGlyph.getBoundingBox().getDimensions().getHeight());
			}
		}
	}
	
	public void setGridSize(int rows,int cols) {
		Layout layout = createLayout();
		if (rows >= 0 && cols >= 0) {
			layout.setAnnotation("grid=(" + rows + "," + cols + ")");
		} /*else {
			layout.unsetAnnotation();
		}*/
	}
	
	public void createComponentFromGCM(String s,Properties prop) {
		ExternalModelDefinition extModel = null;
		String extId = prop.getProperty("gcm").replace(".gcm","");
		if (sbmlComp.getExternalModelDefinition(extId)==null) { 	
			extModel = sbmlComp.createExternalModelDefinition();
		} else {
			extModel = sbmlComp.getExternalModelDefinition(extId);
		}
		extModel.setId(extId);
		extModel.setSource("file://" + prop.getProperty("gcm").replace(".gcm",".xml"));
		if (prop.getProperty("compartment").equals("true")) {
			extModel.setAnnotation("compartment");
		} else {
			extModel.unsetAnnotation();
		}
		Submodel subModel = null;
		if (sbmlCompModel.getSubmodel(s)==null) {
			subModel = sbmlCompModel.createSubmodel();
		} else {
			subModel = sbmlCompModel.getSubmodel(s);
		}
		subModel.setId(s);
		subModel.setModelRef(extId);
		if (prop.keySet().contains("row") && prop.keySet().contains("col")) {
			subModel.setAnnotation("grid=(" + prop.getProperty("row") + "," + prop.getProperty("col") + ")");
		} else {
			subModel.unsetAnnotation();
		}
		for (Object propName : prop.keySet()) {
			if (!propName.toString().equals("gcm")
					&& !propName.toString().equals(GlobalConstants.ID)
					&& prop.keySet().contains("type_" + propName)) {
				if (prop.getProperty("type_" + propName).equals("Output")) {
					String speciesId = prop.getProperty(propName.toString()).toString();
					CompSBasePlugin sbmlSBase = 
							(CompSBasePlugin)sbml.getModel().getSpecies(speciesId).getPlugin("comp");
					ReplacedElement replacement = null;
					boolean found = false;
					for (long i = 0; i < sbmlSBase.getNumReplacedElements(); i++) {
						replacement = sbmlSBase.getReplacedElement(i);
						if (replacement.getSubmodelRef().equals(s) && 
							replacement.getIdRef().equals(propName.toString())) {
							found = true;
							break;
						}
					}
					if (!found) replacement = sbmlSBase.createReplacedElement();
					replacement.setSubmodelRef(s);
					replacement.setIdRef(propName.toString());
					replacement.setAnnotation("Output");
				}
				else {
					String speciesId = prop.getProperty(propName.toString()).toString();
					CompSBasePlugin sbmlSBase = 
							(CompSBasePlugin)sbml.getModel().getSpecies(speciesId).getPlugin("comp");
					ReplacedElement replacement = null;
					boolean found = false;
					for (long i = 0; i < sbmlSBase.getNumReplacedElements(); i++) {
						replacement = sbmlSBase.getReplacedElement(i);
						if (replacement.getSubmodelRef().equals(s) && 
							replacement.getIdRef().equals(propName.toString())) {
							found = true;
							break;
						}
					}
					if (!found) replacement = sbmlSBase.createReplacedElement();
					replacement.setSubmodelRef(s);
					replacement.setIdRef(propName.toString());
					replacement.setAnnotation("Input");
				}
			}
		}
	}

	public void createCompPlugin() {
		if (sbmlComp==null) {
			sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
			sbml.setPkgRequired("comp", true); 
			sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
			sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		}
	}

	public Reaction createComplexReaction(String s,String KcStr) {
		Reaction r = sbml.getModel().getReaction("Complex_"+s);
		if (r==null) {
			r = sbml.getModel().createReaction();
			r.setId("Complex_"+s);
			r.setAnnotation("Complex");
			if (enclosingCompartment.equals("")) {
				r.setCompartment(sbml.getModel().getCompartment(0).getId());
			} else {
				r.setCompartment(enclosingCompartment);
			}
			r.setReversible(true);
			r.setFast(false);
			SpeciesReference product = r.createProduct();
			product.setSpecies(s);
			product.setStoichiometry(1);
			product.setConstant(true);		
		}
		r.createKineticLaw();
		double [] Kc = Utility.getEquilibrium(KcStr); 
		if (Kc[0] >= 0) { 	
			KineticLaw k = r.getKineticLaw();
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_KCOMPLEX_STRING);
			p.setValue(Kc[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_KCOMPLEX_STRING);
			p.setValue(Kc[1]);
		}
		createComplexKineticLaw(r);
		return r;
	}

	public Reaction addNoInfluenceToProductionReaction(String promoterId,String activatorId,String productId) {
		Reaction r = sbml.getModel().getReaction("Production_" + promoterId);
		ModifierSpeciesReference modifier = r.getModifier(activatorId);
		if (!activatorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(activatorId);
			modifier.setAnnotation(GlobalConstants.NOINFLUENCE);
		} else if (modifier != null) {
			return r;
		}
		if (!productId.equals("none") && r.getProduct(productId)==null) {
			SpeciesReference product = r.createProduct();
			product.setSpecies(productId);
			double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			product.setStoichiometry(np);
			product.setConstant(true);
			r.removeProduct(promoterId+"_mRNA");
			sbml.getModel().removeSpecies(promoterId+"_mRNA");
		}
		createProductionKineticLaw(r);
		return r;
	}
	
	public Reaction addActivatorToProductionReaction(String promoterId,String activatorId,String productId,String npStr,
			String ncStr,String KaStr) {
		Reaction r = sbml.getModel().getReaction("Production_" + promoterId);
		ModifierSpeciesReference modifier = r.getModifier(activatorId);
		if (!activatorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(activatorId);
			modifier.setAnnotation(GlobalConstants.ACTIVATION);
		} else if (modifier != null && modifier.getAnnotationString().contains(GlobalConstants.REPRESSION)) {
			modifier.setAnnotation(GlobalConstants.REGULATION);
		}
		if (!productId.equals("none") && r.getProduct(productId)==null) {
			SpeciesReference product = r.createProduct();
			product.setSpecies(productId);
			if (npStr != null) {
				double np = Double.parseDouble(npStr);
				product.setStoichiometry(np);
			} else {
				double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
				product.setStoichiometry(np);
			}
			product.setConstant(true);
			r.removeProduct(promoterId+"_mRNA");
			sbml.getModel().removeSpecies(promoterId+"_mRNA");
		}
		KineticLaw k = r.getKineticLaw();
		if (ncStr!=null && k.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+activatorId+"_a")==null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.COOPERATIVITY_STRING+"_"+activatorId+"_a");
			p.setValue(Double.parseDouble(ncStr));
		} 							
		if (KaStr != null && k.getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+activatorId+"_"))==null) {
			double [] Ka = Utility.getEquilibrium(KaStr);
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+activatorId+"_"));
			p.setValue(Ka[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_"+activatorId+"_"));
			p.setValue(Ka[1]);
		} 
		createProductionKineticLaw(r);
		return r;
	}
	
	public Reaction addRepressorToProductionReaction(String promoterId,String repressorId,String productId,String npStr,
			String ncStr,String KrStr) {
		Reaction r = sbml.getModel().getReaction("Production_" + promoterId);
		ModifierSpeciesReference modifier = r.getModifier(repressorId);
		if (!repressorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(repressorId);
			modifier.setAnnotation(GlobalConstants.REPRESSION);
		} else if (modifier != null && modifier.getAnnotationString().contains(GlobalConstants.ACTIVATION)) {
			modifier.setAnnotation(GlobalConstants.REGULATION);
		}
		if (!productId.equals("none") && r.getProduct(productId)==null) {
			SpeciesReference product = r.createProduct();
			product.setSpecies(productId);
			if (npStr != null) {
				double np = Double.parseDouble(npStr);
				product.setStoichiometry(np);
			} else {
				double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
				product.setStoichiometry(np);
			}
			product.setConstant(true);
			r.removeProduct(promoterId+"_mRNA");
			sbml.getModel().removeSpecies(promoterId+"_mRNA");
		}
		KineticLaw k = r.getKineticLaw();
		if (ncStr!=null && k.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+repressorId+"_r")==null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.COOPERATIVITY_STRING+"_"+repressorId+"_r");
			p.setValue(Double.parseDouble(ncStr));
		} 							
		if (KrStr != null && k.getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+repressorId+"_"))==null) {
			double [] Kr = Utility.getEquilibrium(KrStr);
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+repressorId+"_"));
			p.setValue(Kr[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_"+repressorId+"_"));
			p.setValue(Kr[1]);
		} 
		createProductionKineticLaw(r);
		return r;
	}

	public Reaction addReactantToComplexReaction(String reactantId,String productId,String KcStr,String CoopStr) {
		Reaction r = createComplexReaction(productId,KcStr);
		if (r.getReactant(reactantId)==null) {
			SpeciesReference reactant = r.createReactant();
			reactant.setSpecies(reactantId);
			reactant.setConstant(true);
			KineticLaw k = r.getKineticLaw();
			if (CoopStr != null && k.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId)==null) {
				LocalParameter p = k.createLocalParameter();
				p.setId(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId);
				double nc = Double.parseDouble(CoopStr);
				p.setValue(nc);
				reactant.setStoichiometry(nc);
			} else {
				Parameter p = sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING);
				reactant.setStoichiometry(p.getValue());
			}
		}
		createComplexKineticLaw(r);
		return r;
	}
	
	public Reaction createDiffusionReaction(String s,String kmdiffStr) {
		Reaction reaction = sbml.getModel().getReaction("Diffusion_"+s);
		if (reaction==null) {
			reaction = sbml.getModel().createReaction();
			reaction.setId("Diffusion_"+s);
			reaction.setAnnotation("Diffusion");
			if (enclosingCompartment.equals("")) {
				reaction.setCompartment(sbml.getModel().getCompartment(0).getId());
			} else {
				reaction.setCompartment(enclosingCompartment);
			}
			reaction.setReversible(true);
			reaction.setFast(false);
			SpeciesReference reactant = reaction.createReactant();
			reactant.setSpecies(s);
			reactant.setStoichiometry(1);
			reactant.setConstant(true);
		}
		KineticLaw k = reaction.createKineticLaw();
		double [] kmdiff = Utility.getEquilibrium(kmdiffStr);
		if (kmdiff[0] >= 0) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_MEMDIFF_STRING);
			p.setValue(kmdiff[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_MEMDIFF_STRING);
			p.setValue(kmdiff[1]);
		}
		k.setMath(SBMLutilities.myParseFormula(GlobalConstants.FORWARD_MEMDIFF_STRING+"*"+s+"-"+
				GlobalConstants.REVERSE_MEMDIFF_STRING));
		return reaction;
	}

	public Reaction createConstitutiveReaction(String s) {
		Reaction reaction = sbml.getModel().getReaction("Constitutive_"+s);
		if (reaction==null) {
			reaction = sbml.getModel().createReaction();
			reaction.setId("Constitutive_"+s);
			reaction.setAnnotation("Constitutive");
			if (enclosingCompartment.equals("")) {
				reaction.setCompartment(sbml.getModel().getCompartment(0).getId());
			} else {
				reaction.setCompartment(enclosingCompartment);
			}
			reaction.setReversible(false);
			reaction.setFast(false);
			SpeciesReference product = reaction.createProduct();
			product.setSpecies(s);
			double np = sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue();
			product.setStoichiometry(np);
			product.setConstant(true);
			KineticLaw k = reaction.createKineticLaw();
			k.setMath(SBMLutilities.myParseFormula(GlobalConstants.OCR_STRING));
		}
		return reaction;
	}
	
	public Reaction createDegradationReaction(String s,double kd) {
		Reaction reaction = sbml.getModel().getReaction("Degradation_"+s);
		if (reaction==null) {
			reaction = sbml.getModel().createReaction();
			reaction.setId("Degradation_"+s);
			reaction.setAnnotation("Degradation");
			if (enclosingCompartment.equals("")) {
				reaction.setCompartment(sbml.getModel().getCompartment(0).getId());
			} else {
				reaction.setCompartment(enclosingCompartment);
			}
			reaction.setReversible(false);
			reaction.setFast(false);
			SpeciesReference reactant = reaction.createReactant();
			reactant.setSpecies(s);
			reactant.setStoichiometry(1);
			reactant.setConstant(true);
		} 
		KineticLaw k = reaction.createKineticLaw();
		if (kd > 0) {
			LocalParameter p = k.createLocalParameter();
			p.setId("kd");
			p.setValue(kd);
		}
		k.setMath(SBMLutilities.myParseFormula("kd*"+s));

		return reaction;
	}

	public Reaction createProductionReaction(String s,String ka,String np,String ko,String kb, String KoStr, String KaoStr) {
		Reaction r = sbml.getModel().getReaction("Production_" + s);
		KineticLaw k = null;
		if (r == null) {
			r = sbml.getModel().createReaction();
			r.setId("Production_" + s);
			r.setAnnotation("Production");
			if (enclosingCompartment.equals("")) {
				r.setCompartment(sbml.getModel().getCompartment(0).getId());
			} else {
				r.setCompartment(enclosingCompartment);
			}
			r.setReversible(false);
			r.setFast(false);
			ModifierSpeciesReference modifier = r.createModifier();
			modifier.setSpecies(s);
			modifier.setAnnotation("promoter");
			k = r.createKineticLaw();
			Species mRNA = sbml.getModel().createSpecies();
			mRNA.setId(s+"_mRNA");
			mRNA.setInitialAmount(0.0);
			mRNA.setBoundaryCondition(false);
			mRNA.setConstant(false);
			mRNA.setHasOnlySubstanceUnits(true);
			mRNA.setAnnotation(GlobalConstants.TYPE + "=" + GlobalConstants.MRNA);
			SpeciesReference product = r.createProduct();
			product.setSpecies(mRNA.getId());
			product.setStoichiometry(1.0);
			product.setConstant(true);
		}
		k = r.getKineticLaw();
			if (ka != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.ACTIVATED_STRING);
			p.setValue(Double.parseDouble(ka));
		} 		
		// TODO: WHEN THIS IS UPDATED NEED TO UPDATE STOICHIOMETRY OF PRODUCT
		if (np != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.STOICHIOMETRY_STRING);
			p.setValue(Double.parseDouble(np));
		} 							
		if (ko != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.OCR_STRING);
			p.setValue(Double.parseDouble(ko));
		} 							
		if (kb != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.KBASAL_STRING);
			p.setValue(Double.parseDouble(kb));
		} 
		if (KoStr != null) {
			double [] Ko = Utility.getEquilibrium(KoStr);
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			p.setValue(Ko[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			p.setValue(Ko[1]);
		} 							
		if (KaoStr != null) {
			double [] Kao = Utility.getEquilibrium(KaoStr);
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING);
			p.setValue(Kao[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING);
			p.setValue(Kao[1]);
		} 
		createProductionKineticLaw(r);
		return r;
	}

	public void createComplexKineticLaw(Reaction reaction) {
		String kineticLaw;
		kineticLaw = GlobalConstants.FORWARD_KCOMPLEX_STRING;
		for (int i=0;i<reaction.getNumReactants();i++) {
			if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING + "_" + 
					reaction.getReactant(i).getSpecies())==null) {
				kineticLaw += "*pow(" + reaction.getReactant(i).getSpecies() + "," + GlobalConstants.COOPERATIVITY_STRING + ")";
			} else {
				kineticLaw += "*pow(" + reaction.getReactant(i).getSpecies() + "," + GlobalConstants.COOPERATIVITY_STRING + 
						"_" + reaction.getReactant(i).getSpecies() + ")";
			}
		}
		kineticLaw += "-" + GlobalConstants.REVERSE_KCOMPLEX_STRING + "*" + reaction.getProduct(0).getSpecies();
		reaction.getKineticLaw().setMath(SBMLutilities.myParseFormula(kineticLaw));
	}

	public void createProductionKineticLaw(Reaction reaction) {
		String kineticLaw;
		boolean activated = false;
		String promoter = "";
		for (int i=0;i<reaction.getNumModifiers();i++) {
			if (reaction.getModifier(i).getAnnotationString().contains(GlobalConstants.ACTIVATION)||
					reaction.getModifier(i).getAnnotationString().contains(GlobalConstants.REGULATION)) {
				activated = true;
			} else if (reaction.getModifier(i).getAnnotationString().contains("promoter")) {
				promoter = reaction.getModifier(i).getSpecies();
			}
		}
		if (activated) {
			kineticLaw = promoter + "*(" + GlobalConstants.KBASAL_STRING + "*" +
					"(" + GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" 
					+ GlobalConstants.RNAP_STRING;
			String actBottom = "";
			for (int i=0;i<reaction.getNumModifiers();i++) {
				if (reaction.getModifier(i).getAnnotationString().contains(GlobalConstants.ACTIVATION)||
						reaction.getModifier(i).getAnnotationString().contains(GlobalConstants.REGULATION)) {
					String activator = reaction.getModifier(i).getSpecies();
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KACT_STRING.replace("_","_"+activator+"_"))==null) {
						kineticLaw += "+" + GlobalConstants.ACTIVATED_STRING + "*" + 
								"(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING + "/" + GlobalConstants.REVERSE_KACT_STRING + ")*" 
								+ activator;
						actBottom += "+(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING + "/" + GlobalConstants.REVERSE_KACT_STRING + ")*" 
								+ activator;
					} else {
						kineticLaw += "+" + GlobalConstants.ACTIVATED_STRING + "*" + 
								"(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + activator + "_") + "/" + 
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + activator + "_") + ")*" + activator;
						actBottom += "+(" + GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING + "/" + 
								GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING +
								"*pow((" + GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + activator + "_") + "/" + 
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + activator +"_") + ")*" + activator;
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+activator+"_a")==null) {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
						actBottom += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
					} else {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + activator +"_a)";
						actBottom += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + activator +"_a)";
					}
				}
			}
			kineticLaw += ")/(1+(" + GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + 
					GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" + GlobalConstants.RNAP_STRING + actBottom;
			for (int i=0;i<reaction.getNumModifiers();i++) {
				if (reaction.getModifier(i).getAnnotationString().contains(GlobalConstants.REPRESSION) ||
					reaction.getModifier(i).getAnnotationString().contains(GlobalConstants.REGULATION)) {
					String repressor = reaction.getModifier(i).getSpecies();
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+repressor+"_"))==null) {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING + "/" + GlobalConstants.REVERSE_KREP_STRING + ")*" 
								+ repressor;
					} else {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + repressor + "_") + "/" + 
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + repressor + "_") + ")*" + repressor;
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+repressor+"_r")==null) {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
					} else {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + repressor +"_r)";
					}
				}
			}
			kineticLaw += ")";
		} else {
			kineticLaw = "(" + promoter + "*" + GlobalConstants.OCR_STRING + "*" +
					"(" + GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" 
					+ GlobalConstants.RNAP_STRING + ")/(1+(" + 
					GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" 
					+ GlobalConstants.RNAP_STRING;
			for (int i=0;i<reaction.getNumModifiers();i++) {
				if (reaction.getModifier(i).getAnnotationString().contains(GlobalConstants.REPRESSION)) {
					String repressor = reaction.getModifier(i).getSpecies();
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.FORWARD_KREP_STRING.replace("_","_"+repressor+"_"))==null) {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING + "/" + GlobalConstants.REVERSE_KREP_STRING + ")*" 
								+ repressor;
					} else {
						kineticLaw += "+pow((" + GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + repressor + "_")+"/" + 
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + repressor + "_") + ")*" + repressor;
					}
					if (reaction.getKineticLaw().getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+repressor+"_r")==null) {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + ")";
					} else {
						kineticLaw += "," + GlobalConstants.COOPERATIVITY_STRING + "_" + repressor +"_r)";
					}
				}
			}
			kineticLaw += ")";
		}
		//System.out.println(kineticLaw);
		reaction.getKineticLaw().setMath(SBMLutilities.myParseFormula(kineticLaw));
 	}
	
	/**
	 * Save the current object to file.
	 * 
	 * @param filename
	 */
	public void save(String filename) {
		//gcm2sbml.convertGCM2SBML(filename);
		setGridSize(grid.getNumRows(),grid.getNumCols());
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBML(sbml, filename.replace(".gcm",".xml"));
	}

	public StringBuffer saveToBuffer() {
		setGridSize(grid.getNumRows(),grid.getNumCols());
		SBMLWriter writer = new SBMLWriter();
		String SBMLstr = writer.writeSBMLToString(sbml);
		StringBuffer buffer = new StringBuffer(SBMLstr);
		return buffer;
	}
	
	public void load(String filename) {
		//gcm2sbml.load(filename);
		this.filename = filename;
		String[] splitPath = filename.split(separator);
		sbmlFile = splitPath[splitPath.length-1].replace(".gcm",".xml");
		loadSBMLFile(sbmlFile);
	}

	public void changePromoterName(String oldName, String newName) {
		if (sbml != null) {
			if (sbml.getModel() != null) {
				SBMLutilities.updateVarId(sbml, true, oldName, newName);
				if (sbml.getModel().getSpecies(oldName) != null) {
					sbml.getModel().getSpecies(oldName).setId(newName);
				}
			}
		}
		Reaction reaction = sbml.getModel().getReaction("Production_"+oldName);
		reaction.setId("Production_"+newName);
		SpeciesReference product = reaction.getProduct(oldName+"_mRNA");
		if (product!=null) {
			sbml.getModel().getSpecies(oldName+"_mRNA").setId(newName+"_mRNA");
			product.setSpecies(newName+"_mRNA");
		}

		if (sbmlLayout.getNumLayouts() != 0) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getSpeciesGlyph(oldName)!=null) {
				SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(oldName);
				speciesGlyph.setId(newName);
				speciesGlyph.setSpeciesId(newName);
			}
			if (layout.getTextGlyph(oldName)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(oldName);
				textGlyph.setId(newName);
				textGlyph.setGraphicalObjectId(newName);
				textGlyph.setText(newName);
			}
		}
	}

	public void changeSpeciesName(String oldName, String newName) {
		/*
		ArrayList<String> newConditions = new ArrayList<String>();
		for (String condition : conditions) {
			int index = condition.indexOf(oldName, 0);
			if (index != -1) {
				while (index <= condition.length() && index != -1) {
					if (index != 0 && !Character.isDigit(condition.charAt(index - 1))
							&& !Character.isLetter(condition.charAt(index - 1))
							&& condition.charAt(index - 1) != '_'
							&& index + oldName.length() != condition.length()
							&& !Character.isDigit(condition.charAt(index + oldName.length()))
							&& !Character.isLetter(condition.charAt(index + oldName.length()))
							&& condition.charAt(index + oldName.length()) != '_') {
						condition = condition.substring(0, index)
								+ condition.substring(index, condition.length()).replace(oldName,
										newName);
					}
					index++;
					index = condition.indexOf(oldName, index);
				}
			}
			newConditions.add(condition);
		}
		conditions = newConditions;
		*/
		if (sbml != null) {
			if (sbml.getModel() != null) {
				SBMLutilities.updateVarId(sbml, true, oldName, newName);
				if (sbml.getModel().getSpecies(oldName) != null) {
					sbml.getModel().getSpecies(oldName).setId(newName);
				}
			}
		}
		if (isSpeciesConstitutive(oldName)) {
			Reaction reaction = sbml.getModel().getReaction("Constitutive_"+oldName);
			reaction.setId("Constitutive_"+newName);
		}
		if (isSpeciesDiffusible(oldName)) {
			Reaction reaction = sbml.getModel().getReaction("Diffusion_"+oldName);
			reaction.setId("Diffusion_"+newName);
		}
		if (isSpeciesDegradable(oldName)) {
			Reaction reaction = sbml.getModel().getReaction("Degradation_"+oldName);
			reaction.setId("Degradation_"+newName);
		}
		if (isSpeciesComplex(oldName)) {
			Reaction reaction = sbml.getModel().getReaction("Complex_"+oldName);
			reaction.setId("Complex_"+newName);
		}
		for (int i=0;i<sbml.getModel().getNumReactions();i++) {
			Reaction reaction = sbml.getModel().getReaction(i);
			if (reaction.getAnnotationString().contains("Complex")) {
				KineticLaw k = reaction.getKineticLaw();
				for (int j=0;j<k.getNumLocalParameters();j++) {
					LocalParameter param = k.getLocalParameter(j);
					if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldName)) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newName);
					}
				}
				createComplexKineticLaw(reaction);
			} else if (reaction.getAnnotationString().contains("Production")) {
				KineticLaw k = reaction.getKineticLaw();
				for (int j=0;j<k.getNumLocalParameters();j++) {
					LocalParameter param = k.getLocalParameter(j);
					if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldName + "_r")) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newName + "_r");
					} else if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldName + "_a")) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newName + "_a");
					} else if (param.getId().equals(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + oldName + "_"))) {
						param.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + newName + "_"));
					} else if (param.getId().equals(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + oldName + "_"))) {
						param.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + newName + "_"));
					} else if (param.getId().equals(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + oldName + "_"))) {
						param.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + newName + "_"));
					} else if (param.getId().equals(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + oldName + "_"))) {
						param.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + newName + "_"));
					}
				}
			}
		}
		if (sbmlLayout.getNumLayouts() != 0) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getSpeciesGlyph(oldName)!=null) {
				SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(oldName);
				speciesGlyph.setId(newName);
				speciesGlyph.setSpeciesId(newName);
			}
			if (layout.getTextGlyph(oldName)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(oldName);
				textGlyph.setId(newName);
				textGlyph.setGraphicalObjectId(newName);
				textGlyph.setText(newName);
			}
		}
	}

	public void changeComponentName(String oldName, String newName) {
		// TODO: REMOVED, IS THIS REALLY NEEDED?
		/*
		String[] sArray = new String[influences.keySet().size()];
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String arrow = getArrow(s);
			String output = getOutput(s);
			boolean replaceInput = input.equals(oldName);
			boolean replaceOutput = output.equals(oldName);
			String newInfluenceName = "";
			if (replaceInput || replaceOutput) {
				if (replaceInput) {
					newInfluenceName = newInfluenceName + newName;
				}
				else {
					newInfluenceName = newInfluenceName + input;
				}
				if (replaceOutput) {
					newInfluenceName = newInfluenceName + " " + arrow + " " + newName;
				}
				else {
					newInfluenceName = newInfluenceName + " " + arrow + " " + output;
				}
				String promoterName = "default";
				if (influences.get(s).containsKey(GlobalConstants.PROMOTER)) {
					promoterName = influences.get(s).get(GlobalConstants.PROMOTER).toString();
				}
				newInfluenceName = newInfluenceName + ", Promoter " + promoterName;
				influences.put(newInfluenceName, influences.get(s));
				influences.remove(s);
			}
		}
		*/
		Submodel subModel = sbmlCompModel.getSubmodel(oldName);
		subModel.setId(newName);
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbml.getModel().getSpecies(i).getPlugin("comp");
			ReplacedElement replacement = null;
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
				replacement = sbmlSBase.getReplacedElement(j);
				if (replacement.getSubmodelRef().equals(oldName)) {
					replacement.setSubmodelRef(newName);
				}
			}
		}
		if (sbmlLayout.getNumLayouts() != 0) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getCompartmentGlyph(oldName)!=null) {
				CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(oldName);
				compartmentGlyph.setId(newName);
				compartmentGlyph.setCompartmentId(newName);
			}
			if (layout.getTextGlyph(oldName)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(oldName);
				textGlyph.setId(newName);
				textGlyph.setGraphicalObjectId(newName);
				textGlyph.setText(newName);
			}
		}
	}

	
	//ADD METHODS
	/*
	public void addSpecies(String name, Properties property) {
		species.put(name, property);
	}
	*/
	
	public void addReaction(String sourceID,String targetID,boolean isModifier) {
		Model m = sbml.getModel();
		JPanel reactionListPanel = new JPanel(new GridLayout(1, 1));
		ArrayList<String> choices = new ArrayList<String>();
		choices.add("Create a new reaction");
		for (int i=0; i < m.getNumReactions(); i++) {
			Reaction r = m.getReaction(i);
			if (!isModifier && r.getReactant(sourceID) != null) {
				choices.add("Add " + targetID + " as a product of reaction " + r.getId());
			}
			if (r.getProduct(targetID) != null) {
				if (isModifier) {
					choices.add("Add " + sourceID + " as a modifier of reaction " + r.getId());
				} else {
					choices.add("Add " + sourceID + " as a reactant of reaction " + r.getId());
				}
			}
		}
		
		Object[] options = { "OK", "Cancel" };
		JComboBox reactionList = new JComboBox(choices.toArray());
		if (choices.size()>1) {
			reactionListPanel.add(reactionList);
			int value = JOptionPane.showOptionDialog(Gui.frame, reactionListPanel, "Reaction Choice",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (value == JOptionPane.NO_OPTION) return;
		}
		if (((String)reactionList.getSelectedItem()).contains("reactant")) {
			String[] selection = ((String)reactionList.getSelectedItem()).split(" ");
			String reactionId = selection[selection.length-1];
			Reaction r = m.getReaction(reactionId);
			SpeciesReference s = r.createReactant();
			s.setSpecies(sourceID);
			s.setStoichiometry(1.0);
			s.setConstant(true);
		} else if (((String)reactionList.getSelectedItem()).contains("modifier")) {
			String[] selection = ((String)reactionList.getSelectedItem()).split(" ");
			String reactionId = selection[selection.length-1];
			Reaction r = m.getReaction(reactionId);
			ModifierSpeciesReference s = r.createModifier();
			s.setSpecies(sourceID);
		} else if (((String)reactionList.getSelectedItem()).contains("product")) {
			String[] selection = ((String)reactionList.getSelectedItem()).split(" ");
			String reactionId = selection[selection.length-1];
			Reaction r = m.getReaction(reactionId);
			SpeciesReference s = r.createProduct();
			s.setSpecies(targetID);
			s.setStoichiometry(1.0);
			s.setConstant(true);
		} else {
			Reaction r = m.createReaction();
			String reactionId = "r0";
			int i = 0;
			while (usedIDs.contains(reactionId)) {
				i++;
				reactionId = "r" + i;
			}
			usedIDs.add(reactionId);
			r.setId(reactionId);
			if (enclosingCompartment.equals("")) {
				r.setCompartment(m.getCompartment(0).getId());
			} else {
				r.setCompartment(enclosingCompartment);
			}
			r.setReversible(false);
			r.setFast(false);
			if (isModifier) { 
				ModifierSpeciesReference source = r.createModifier();
				source.setSpecies(sourceID);
			} else {
				SpeciesReference source = r.createReactant();
				source.setSpecies(sourceID);
				source.setConstant(true);
				source.setStoichiometry(1.0);
			}
			SpeciesReference target = r.createProduct();
			target.setSpecies(targetID);
			target.setConstant(true);
			target.setStoichiometry(1.0);
			KineticLaw k = r.createKineticLaw();
			LocalParameter p = k.createLocalParameter();
			p.setId("kf");
			p.setValue(0.1);
			p = k.createLocalParameter();
			p.setId("kr");
			p.setValue(1.0);
			k.setMath(SBMLutilities.myParseFormula("kf*"+sourceID));
		}
	}

	/**
	 * Add a component given the specified name and properties. If either is
	 * null then they will be created using (hopefully) sensible defaults.
	 * 
	 * @param name
	 * @param properties
	 * 
	 * @return: the id of the created component.
	 */
	public String addComponent(String id, String modelFile, boolean enclosed, int row, int col, double x, double y) {
		ExternalModelDefinition extModel = null;
		String extId = modelFile.replace(".gcm","");
		if (sbmlComp.getExternalModelDefinition(extId)==null) { 	
			extModel = sbmlComp.createExternalModelDefinition();
		} else {
			extModel = sbmlComp.getExternalModelDefinition(extId);
		}
		extModel.setId(extId);
		extModel.setSource("file://" + modelFile.replace(".gcm",".xml"));
		if (enclosed) {
			extModel.setAnnotation("compartment");
		} else {
			extModel.unsetAnnotation();
		}
		Submodel subModel = null;
		if (id == null) {
			int count = 1;
			id = "C" + count;
			while (sbmlCompModel.getSubmodel(id)!=null) {
				count++;
				id = "C" + count;
			}
			subModel = sbmlCompModel.createSubmodel();
		} else {
			if (sbmlCompModel.getSubmodel(id)==null) {
				subModel = sbmlCompModel.createSubmodel();
			} else {
				subModel = sbmlCompModel.getSubmodel(id);
			}
		}
		subModel.setId(id);
		subModel.setModelRef(extId);
		if (row >= 0 && col >= 0) {
			subModel.setAnnotation("grid=(" + row + "," + col + ")");
		} else {
			subModel.unsetAnnotation();
		}
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		CompartmentGlyph compartmentGlyph = null;
		if (layout.getCompartmentGlyph(id)!=null) {
			compartmentGlyph = layout.getCompartmentGlyph(id);
		} else {
			compartmentGlyph = layout.createCompartmentGlyph();
			compartmentGlyph.setId(id);
			compartmentGlyph.setCompartmentId(id);
		}
		compartmentGlyph.getBoundingBox().setX(x);
		compartmentGlyph.getBoundingBox().setY(y);
		compartmentGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_COMPONENT_WIDTH);
		compartmentGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_COMPONENT_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(id);
		textGlyph.setGraphicalObjectId(id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox());
		usedIDs.add(id);
		return id;
	}

	// used by createNewObjectName
	private HashMap<String, Integer> createdHighIds;

	/**
	 * builds a sensible default name for a new species, component, or promoter.
	 * 
	 * @param prefix
	 *            : a string which will begin the component name. ("S", "C", or
	 *            "P")
	 * @param hash
	 *            : the internal model of species or components or promoter
	 * @return: a string name for the new component.
	 */
	public String createNewObjectName(String prefix, HashMap<String, Properties> hash) {
		// make sure createdHighIds exists and is initialized.
		if (createdHighIds == null)
			createdHighIds = new HashMap<String, Integer>();
		if (createdHighIds.containsKey(prefix) == false)
			createdHighIds.put(prefix, 0);

		String name;
		do {
			createdHighIds.put(prefix, createdHighIds.get(prefix) + 1);
			name = prefix + String.valueOf(createdHighIds.get(prefix));
		}
		while (hash.containsKey(name) || usedIDs.contains(name));

		return name;
	}
	
	/**
	 * loads the grid size from the gcm file
	 * 
	 * @param data string data from a gcm file
	 */
	private void loadGridSize() {
		if (sbmlLayout != null) {
			if (sbmlLayout.getLayout("iBioSim")!=null && sbmlLayout.getLayout("iBioSim").isSetAnnotation()) {
				String annotation = sbmlLayout.getLayout("iBioSim").getAnnotation().toXMLString();
				int first = annotation.indexOf("(");
				int middle = annotation.indexOf(",");
				int last = annotation.indexOf(")");
				int row = Integer.valueOf(annotation.substring(first+1,middle));
				int col = Integer.valueOf(annotation.substring(middle+1,last));
				buildGrid(row, col);
				return;
			}
		}
	}

	public void removeSpecies(String id) {
		if (id != null) {
			sbml.getModel().removeSpecies(id);
			if (isSpeciesConstitutive(id)) {
				removeReaction("Constitutive_"+id);
			}
			if (isSpeciesDiffusible(id)) {
				removeReaction("Diffusion_"+id);
			}
			if (isSpeciesDegradable(id)) {
				removeReaction("Degradation_"+id);
			}
			if (sbmlLayout.getLayout("iBioSim") != null) {
				Layout layout = sbmlLayout.getLayout("iBioSim"); 
				if (layout.getSpeciesGlyph(id)!=null) {
					layout.removeSpeciesGlyph(id);
				}
				if (layout.getTextGlyph(id) != null) {
					layout.removeTextGlyph(id);
				}
			}
			speciesPanel.refreshSpeciesPanel(sbml);
		}
		while (usedIDs.contains(id)) {
			usedIDs.remove(id);
		}
	}

	public void removeReaction(String id) {
		Reaction tempReaction = sbml.getModel().getReaction(id);
		ListOf r = sbml.getModel().getListOfReactions();
		for (int i = 0; i < sbml.getModel().getNumReactions(); i++) {
			if (((Reaction) r.get(i)).getId().equals(tempReaction.getId())) {
				r.remove(i);
			}
		}
		if (sbmlLayout.getLayout("iBioSim") != null) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getReactionGlyph(id)!=null) {
				layout.removeReactionGlyph(id);
			}
			if (layout.getTextGlyph(id) != null) {
				layout.removeTextGlyph(id);
			}
		}
		usedIDs.remove(id);
	}

	public ArrayList<String> getSpecies() {
		ArrayList<String> speciesSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getNumSpecies(); i++) {
				Species species = sbml.getModel().getSpecies(i);
				if (!species.isSetAnnotation() || !species.getAnnotationString().contains(GlobalConstants.TYPE + "=" + GlobalConstants.PROMOTER)){
					speciesSet.add(species.getId());
				}
			}
		}
		return speciesSet;
	}
	public ArrayList<String> getPromoters() {
		ArrayList<String> promoterSet = new ArrayList<String>();
		for (int i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			if (species.isSetAnnotation() && species.getAnnotationString().contains(GlobalConstants.TYPE + "=" + GlobalConstants.PROMOTER)){
				promoterSet.add(species.getId());
			}
		}
		return promoterSet;
	}
	
	public ArrayList<String> getInputSpecies() {
		ArrayList<String> inputs = new ArrayList<String>();
		for (String spec : getSpecies()) {
			if (getSpeciesType(spec).equals(GlobalConstants.INPUT)) {
				inputs.add(spec);
			}
		}
		return inputs;
	}

	public ArrayList<String> getOutputSpecies() {
		ArrayList<String> outputs = new ArrayList<String>();
		for (String spec : getSpecies()) {
			if (getSpeciesType(spec).equals(GlobalConstants.OUTPUT)) {
				outputs.add(spec);
			}
		}
		return outputs;
	}
	
	public ArrayList<String> getBiochemicalSpecies() {
		ArrayList<String> inputs = getInputSpecies();
		ArrayList<String> genetic = new ArrayList<String>();
		ArrayList<String> biochemical = new ArrayList<String>();
		for (String spec : getSpecies()) {
			for (long i=0; i<sbml.getModel().getNumReactions(); i++) {
				Reaction r = sbml.getModel().getReaction(i);
				if (r.getProduct(spec)!=null) {
					genetic.add(spec);
				}
			}
			if (!genetic.contains(spec) && !inputs.contains(spec)) {
				biochemical.add(spec);
			}
		}
		return biochemical;
	}

	public String getModelFileName(String id) {
		Submodel submodel = sbmlCompModel.getSubmodel(id);
		ExternalModelDefinition extModel = sbmlComp.getExternalModelDefinition(submodel.getModelRef());
		return extModel.getSource().substring(7);
	}

	public boolean isCompartmentEnclosed(String id) {
		Submodel submodel = sbmlCompModel.getSubmodel(id);
		if (submodel != null) {
			ExternalModelDefinition extModel = sbmlComp.getExternalModelDefinition(submodel.getModelRef());
			if (extModel.getAnnotation()!=null) {
				return extModel.getAnnotation().toXMLString().contains("compartment");
			} 
		}
		return false;
	}

	public HashMap<String, String> getInputs(String id) {
		HashMap<String, String> inputs = new HashMap();
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbml.getModel().getSpecies(i).getPlugin("comp");
			ReplacedElement replacement = null;
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
				replacement = sbmlSBase.getReplacedElement(j);
				if (replacement.getSubmodelRef().equals(id) &&
				    replacement.getAnnotation().toXMLString().contains("Input")) {
					inputs.put(replacement.getIdRef(),sbml.getModel().getSpecies(i).getId());
				}
			}
		}
		return inputs;
	}

	public HashMap<String, String> getOutputs(String id) {
		HashMap<String, String> inputs = new HashMap();
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbml.getModel().getSpecies(i).getPlugin("comp");
			ReplacedElement replacement = null;
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
				replacement = sbmlSBase.getReplacedElement(j);
				if (replacement.getSubmodelRef().equals(id) &&
				    replacement.getAnnotation().toXMLString().contains("Output")) {
					inputs.put(replacement.getIdRef(),sbml.getModel().getSpecies(i).getId());
				}
			}
		}
		return inputs;
	}
/*	
	public HashMap<String, Properties> getComponents() {
		return components;
	}
	public HashMap<String, Properties> getCompartments() {
		return compartments;
	}
	*/
	
	public int getSubmodelRow(Submodel submodel) {
		String annotation = submodel.getAnnotation().toXMLString();
		int first = annotation.indexOf("(");
		int middle = annotation.indexOf(",");
		int row = Integer.valueOf(annotation.substring(first+1,middle));
		return row;
	}
	
	public int getSubmodelCol(Submodel submodel) {
		String annotation = submodel.getAnnotation().toXMLString();
		int middle = annotation.indexOf(",");
		int last = annotation.indexOf(")");
		int col = Integer.valueOf(annotation.substring(middle+1,last));
		return col;
	}
	
	public void setSubmodelRowCol(String compId,int row,int col) {
		Submodel submodel = sbmlCompModel.getSubmodel(compId);
		submodel.setAnnotation("grid=(" + row + "," + col + ")");
	}

	/**
	 * returns all the ports in the gcm file matching type, which must be either
	 * GlobalConstants.INPUT or GlobalConstants.OUTPUT.
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<String> getPorts(String type) {
		ArrayList<String> out = new ArrayList<String>();
		for (String speciesId : this.getSpecies()) {
			if (getSpeciesType(speciesId).equals(type)) {
				out.add(speciesId);
			}
		}
		return out;
	}
	
	public String getSpeciesType(String speciesId) {
		Species species = sbml.getModel().getSpecies(speciesId);
		String annotation = species.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
		String [] annotations = annotation.split(",");
		for (int i=0;i<annotations.length;i++) {
			if (annotations[i].startsWith(GlobalConstants.TYPE)) {
				String [] type = annotations[i].split("=");
				return type[1];
			}
		}
		return "";
	}
	
	public boolean isSpeciesDiffusible(String speciesId) {
		Reaction diffusion = sbml.getModel().getReaction("Diffusion_"+speciesId);
		if (diffusion != null) {
			if (diffusion.isSetAnnotation() && diffusion.getAnnotationString().contains("Diffusion")) return true;
		}
		return false;
	}
	
	public boolean isSpeciesConstitutive(String speciesId) {
		Reaction constitutive = sbml.getModel().getReaction("Constitutive_"+speciesId);
		if (constitutive != null) {
			if (constitutive.isSetAnnotation() && constitutive.getAnnotationString().contains("Constitutive")) return true;
		}
		return false;
	}
	
	public boolean isSpeciesDegradable(String speciesId) {
		Reaction degradation = sbml.getModel().getReaction("Degradation_"+speciesId);
		if (degradation != null) {
			if (degradation.isSetAnnotation() && degradation.getAnnotationString().contains("Degradation")) return true;
		}
		return false;
	}
	
	public boolean isSpeciesComplex(String speciesId) {
		Reaction complex = sbml.getModel().getReaction("Complex_"+speciesId);
		if (complex != null) {
			if (complex.isSetAnnotation() && complex.getAnnotationString().contains("Complex")) return true;
		}
		return false;
	}
	
	public String getSpeciesSBOLAnnotation(String speciesId,String SBOLelement) {
		String annotation = sbml.getModel().getSpecies(speciesId).getAnnotationString().replace("<annotation>","")
				.replace("</annotation>","");
		String [] annotations = annotation.split(",");
		for (int i=0;i<annotations.length;i++) {
			if (annotations[i].startsWith(SBOLelement)) {
				String [] type = annotations[i].split("=");
				return type[1];
			}
		}
		return "";
	}

	public void connectComponentAndSpecies(String compId, String port, String specId, String type) {
		CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbml.getModel().getSpecies(specId).getPlugin("comp");
		boolean found = false;
		ReplacedElement replacement = null;
		for (long i = 0; i < sbmlSBase.getNumReplacedElements(); i++) {
			replacement = sbmlSBase.getReplacedElement(i);
			if (replacement.getSubmodelRef().equals(compId) && 
				replacement.getIdRef().equals(port)) {
				found = true;
				break;
			}
		}
		if (!found) replacement = sbmlSBase.createReplacedElement();
		replacement.setSubmodelRef(compId);
		replacement.setIdRef(port);
		replacement.setAnnotation(type);
		return;
	}

	/**
	 * Given a component and the name of a species, return true if that species
	 * is connected to that component. Optionally disconnect them as well.
	 */
	/*
	public boolean checkDisconnectComponentAndSpecies(Properties comp, String speciesId, boolean disconnect) {
		// now figure out which port the species is connected to
		for (Object p : comp.keySet()) {
			String key = p.toString();
			String value = (String) comp.get(key);
			if (value.equals(speciesId) && comp.containsKey("type_" + key)) {
				if (disconnect) {
					comp.remove(key);
					comp.remove("type_" + key);
				}
				return true;
			}
		}
		return false;
	}
	*/

	public String getComponentPortMap(String s) {
		String portmap = "(";
		boolean first = true;
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
				ReplacedElement replacement = sbmlSBase.getReplacedElement(j);
				if (replacement.getSubmodelRef().equals(s)) {
					if (!first) portmap += ", ";
					portmap += replacement.getIdRef() + "->" + species.getId();
					first = false;
				}
			}
		}
		portmap += ")";
		return portmap;
	}
	
	/*
	public HashMap<String, Properties> getInfluences() {
		return influences;
	}
	*/
	
	/**
	 * Checks to see if removing influence is okay
	 * 
	 * @param name
	 *            influence to remove
	 * @return true, it is always okay to remove influence
	 */
	public boolean removeInfluenceCheck(String name) {
		return true;
	}

	/**
	 * looks everywhere to see if the given species is connected to anything.
	 * Returns true if it is. Also will delete the connections if remove
	 * parameter is true. NOTE: This function does not check outside GCM files.
	 * Use speciesUsedInOtherGCM() for that.
	 * 
	 * @param remove
	 * @return
	 */
	/*
	private boolean checkRemoveSpeciesAssociations(String name, boolean remove) {
		boolean ret = false;

		boolean changed;
		do {
			changed = false;
			for (String s : influences.keySet()) {
				if (getInput(s).equals(name) || getOutput(s).equals(name)) {
					ret = true;
					if (remove) {
						influences.remove(s);
						// start over because the keyset changed and the forloop
						// could be broken.
						changed = true;
						break;
					}
				}
			}
		}
		while (changed == true);
//		for (String c : getComponents().keySet()) {
//			if (checkDisconnectComponentAndSpecies(getComponents().get(c), name, remove))
//				ret = true;
//		}
		
		return ret;
	}
	*/

	/*
	public boolean speciesUsedInOtherGCM(String name) {
		if (species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.INPUT)
				|| species.get(name).getProperty(GlobalConstants.TYPE).equals(
						GlobalConstants.OUTPUT)) {
			for (String s : new File(path).list()) {
				if (s.endsWith(".gcm")) {
					GCMFile g = new GCMFile(path);
					g.load(path + separator + s);
					for (String comp : g.getComponents().keySet()) {
						String compGCM = g.getComponents().get(comp).getProperty("gcm");
						if (filename.endsWith(compGCM)
								&& g.getComponents().get(comp).containsKey(name)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
    */
	
	/**
	 * Checks to see if removing specie is okay
	 * 
	 * @param name
	 *            specie to remove
	 * @return true if specie is in no influences
	 */
	/*
	public boolean removeSpeciesCheck(String name) {
		return !checkRemoveSpeciesAssociations(name, false);
	}*/

	/*
	public boolean editSpeciesCheck(String name, String newType) {
		if ((species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.INPUT) && 
				newType.contains(GlobalConstants.INPUT)) || 
			(species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT) &&
				newType.contains(GlobalConstants.OUTPUT))) {
			return true;
		}
		else if (species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.INPUT)
				|| species.get(name).getProperty(GlobalConstants.TYPE).contains(GlobalConstants.OUTPUT)) {
			for (String s : new File(path).list()) {
				if (s.endsWith(".gcm")) {
					GCMFile g = new GCMFile(path);
					g.load(path + separator + s);
					for (String comp : g.getComponents().keySet()) {
						String compGCM = g.getComponents().get(comp).getProperty("gcm");
						if (filename.endsWith(compGCM)
								&& g.getComponents().get(comp).containsKey(name)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
    */

	public void removePromoter(String id) {
		sbml.getModel().removeSpecies(id);
		sbml.getModel().removeSpecies(id+"_mRNA");
		removeReaction("Production_"+id);
		if (sbmlLayout.getLayout("iBioSim") != null) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getSpeciesGlyph(id)!=null) {
				layout.removeSpeciesGlyph(id);
			}
			if (layout.getTextGlyph(id) != null) {
				layout.removeTextGlyph(id);
			}
		}
		while (usedIDs.contains(id)) {
			usedIDs.remove(id);
		}
	}

	public void removeComponent(String name) {
		for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
			if (sbmlCompModel.getSubmodel(i).getId().equals(name)) {
				sbmlCompModel.removeSubmodel(i);
			}
		}
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbml.getModel().getSpecies(i).getPlugin("comp");
			ReplacedElement replacement = null;
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
				replacement = sbmlSBase.getReplacedElement(j);
				if (replacement.getSubmodelRef().equals(name)) {
					sbmlSBase.removeReplacedElement(j);
				}
			}
		}
		if (sbmlLayout.getLayout("iBioSim") != null) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getCompartmentGlyph(name)!=null) {
				layout.removeCompartmentGlyph(name);
			}
			if (layout.getTextGlyph(name) != null) {
				layout.removeTextGlyph(name);
			}
		}
		while (usedIDs.contains(name)) {
			usedIDs.remove(name);
		}
	}

	public void removeInfluence(String name) {
		if (name.contains("+")) {
			Reaction reaction = sbml.getModel().getReaction("Complex_"+name.substring(name.indexOf(">")+1));
			reaction.removeReactant(name.substring(0,name.indexOf("+")));
			if (reaction.getNumReactants()==0) {
				sbml.getModel().removeReaction(reaction.getId());
			} else {
				createComplexKineticLaw(reaction);
			}
		} else if (name.contains(",")) {
			Reaction reaction = sbml.getModel().getReaction("Production_"+name.substring(name.indexOf(",")+1));
			ModifierSpeciesReference modifier = reaction.getModifier(name.substring(0,name.indexOf("-")));
			if (modifier.getAnnotationString().contains(GlobalConstants.REGULATION)) {
				if (name.contains("|")) {
					modifier.setAnnotation(GlobalConstants.ACTIVATION);
				} else {
					modifier.setAnnotation(GlobalConstants.REPRESSION);
				}
			} else {
				if (name.contains("x>")) {
					reaction.removeModifier(name.substring(0,name.indexOf(">")-1));
				} else {
					reaction.removeModifier(name.substring(0,name.indexOf("-")));
				}
			}
			if (reaction.getNumModifiers()==1) {
				sbml.getModel().removeSpecies(name.substring(name.indexOf(",")+1));
				sbml.getModel().removeReaction(reaction.getId());
			} else {
				createProductionKineticLaw(reaction);
			}
		} else if (name.contains("|")) {
			Reaction reaction = sbml.getModel().getReaction("Production_"+name.substring(name.indexOf("|")+1));
			ModifierSpeciesReference modifier = reaction.getModifier(name.substring(0,name.indexOf("-")));
			if (modifier.getAnnotationString().contains(GlobalConstants.REGULATION)) {
				modifier.setAnnotation(GlobalConstants.ACTIVATION);
			} else {
				if (name.contains("x>")) {
					reaction.removeModifier(name.substring(0,name.indexOf(">")-1));
				} else {
					reaction.removeModifier(name.substring(0,name.indexOf("-")));
				}
			}
			createProductionKineticLaw(reaction);
		} else if (name.contains(">")) {
			Reaction reaction = sbml.getModel().getReaction("Production_"+name.substring(name.indexOf(">")+1));
			if (reaction!=null) {
				ModifierSpeciesReference modifier = null;
				if (name.contains("x>")) {
					modifier = reaction.getModifier(name.substring(0,name.indexOf(">")-1));
				} else {
					modifier = reaction.getModifier(name.substring(0,name.indexOf("-")));
				}
				if (modifier.getAnnotationString().contains(GlobalConstants.REGULATION)) {
					modifier.setAnnotation(GlobalConstants.REPRESSION);
				} else {
					if (name.contains("x>")) {
						reaction.removeModifier(name.substring(0,name.indexOf(">")-1));
					} else {
						reaction.removeModifier(name.substring(0,name.indexOf("-")));
					}
				}
				createProductionKineticLaw(reaction);
			} else {
				String promoterId = name.substring(0,name.indexOf("-"));
				reaction = sbml.getModel().getReaction("Production_"+promoterId);
				reaction.removeProduct(name.substring(name.indexOf(">")+1));
				if (reaction.getNumProducts()==0) {
					Species mRNA = sbml.getModel().createSpecies();
					mRNA.setId(promoterId+"_mRNA");
					mRNA.setInitialAmount(0.0);
					mRNA.setBoundaryCondition(false);
					mRNA.setConstant(false);
					mRNA.setHasOnlySubstanceUnits(true);
					SpeciesReference product = reaction.createProduct();
					product.setSpecies(mRNA.getId());
					product.setStoichiometry(1.0);
					product.setConstant(true);
				}
			}
		}
	}

	/**
	 * returns null if the given influence is allowed and false otherwise.
	 * Reasons an influence wouldn't be allowed is if it shadows an existing
	 * influence, is a no-influence and an influence already exists there, or a
	 * no-influence already exists there. If the influence isn't allowed,
	 * returns a reason why.
	 */
	/*
	public String isInfluenceAllowed(String newInfluenceName) {

		String sourceID = GCMFile.getInput(newInfluenceName);
		String targetID = GCMFile.getOutput(newInfluenceName);

		// Only one influence with the exact same name is allowed.
		if (this.getInfluences().containsKey(newInfluenceName)) {
			return "there is already an influence between those species on that promoter.";
		}

		// now check for existing influences between the two given species.
		for (String infName : this.getInfluences().keySet()) {
			if (GCMFile.getInput(infName).equals(sourceID)
					&& GCMFile.getOutput(infName).equals(targetID)) {
				// the influence goes between the same set of species. Now do
				// some more checks.
				String arrow = getArrow(infName);
				String newArrow = getArrow(newInfluenceName);
				if (arrow.contains("x>")) {
					return "there is a no-influence between these species.";
				}

				if (newArrow.contains("x>")) {
					return "there is already at least one influence between these species.\nYou cannot add a no-influence until you remove the other influences.";
				}
			}
		}

		// nothing was wrong, the influence is allowed.
		return null;
	}
	*/

	/**
	 * creates and adds a new species.
	 * 
	 * @param id
	 *            : the new id. If null the id will be generated
	 * @param x
	 * @param y
	 */
	private int creatingSpeciesID = 0;
	private int creatingReactionID = 0;

	public void createSpecies(String id, float x, float y) {
		if (id == null) {
			do {
				creatingSpeciesID++;
				id = "S" + String.valueOf(creatingSpeciesID);
			}
			while (usedIDs.contains(id));
		}
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		SpeciesGlyph speciesGlyph = null;
		if (layout.getSpeciesGlyph(id)!=null) {
			speciesGlyph = layout.getSpeciesGlyph(id);
		} else {
			speciesGlyph = layout.createSpeciesGlyph();
			speciesGlyph.setId(id);
			speciesGlyph.setSpeciesId(id);
		}
		speciesGlyph.getBoundingBox().setX(x);
		speciesGlyph.getBoundingBox().setY(y);
		speciesGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_SPECIES_WIDTH);
		speciesGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_SPECIES_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(id);
		textGlyph.setGraphicalObjectId(id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
		if (sbml != null && sbml.getModel().getSpecies(id)==null) {
			Model m = sbml.getModel();
			Species s = m.createSpecies();
			s.setId(id);
			s.setAnnotation(GlobalConstants.TYPE+"="+GlobalConstants.INTERNAL);
			usedIDs.add(id);
			if (enclosingCompartment.equals("")) {
				s.setCompartment(m.getCompartment(0).getId());
			} else {
				s.setCompartment(enclosingCompartment);
			}
			s.setBoundaryCondition(false);
			s.setConstant(false);
			s.setInitialAmount(0);
			s.setHasOnlySubstanceUnits(true);
			if (speciesPanel!=null)
				speciesPanel.refreshSpeciesPanel(sbml);
		}
	}

	public void createReaction(String id, float x, float y) {
		if (id == null) {
			do {
				creatingReactionID++;
				id = "R" + String.valueOf(creatingReactionID);
			}
			while (usedIDs.contains(id));
		}
		usedIDs.add(id);
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		ReactionGlyph reactionGlyph = null;
		if (layout.getReactionGlyph(id)!=null) {
			reactionGlyph = layout.getReactionGlyph(id);
		} else {
			reactionGlyph = layout.createReactionGlyph();
			reactionGlyph.setId(id);
			reactionGlyph.setReactionId(id);
		}
		reactionGlyph.getBoundingBox().setX(x);
		reactionGlyph.getBoundingBox().setY(y);
		reactionGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_REACTION_WIDTH);
		reactionGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_REACTION_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(id);
		textGlyph.setGraphicalObjectId(id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
		Model m = sbml.getModel();
		Reaction r = m.createReaction();
		r.setId(id);
		if (enclosingCompartment.equals("")) {
			r.setCompartment(m.getCompartment(0).getId());
		} else {
			r.setCompartment(enclosingCompartment);
		}
		r.setReversible(false);
		r.setFast(false);
		KineticLaw k = r.createKineticLaw();
		LocalParameter p = k.createLocalParameter();
		p.setId("kf");
		p.setValue(0.1);
		p = k.createLocalParameter();
		p.setId("kr");
		p.setValue(1.0);
		k.setMath(SBMLutilities.myParseFormula("kf"));
	}

	private int creatingPromoterID = 0;

	public String createPromoter(String id, float x, float y, boolean is_explicit) {
		if (id == null) {
			do {
				creatingPromoterID++;
				id = "P" + String.valueOf(creatingPromoterID);
			}
			while ((usedIDs != null) && usedIDs.contains(id));
		}
		if (usedIDs != null) usedIDs.add(id);
		Species species = sbml.getModel().createSpecies();
		species.setId(id);
		species.setAnnotation(GlobalConstants.TYPE + "=" + GlobalConstants.PROMOTER);
		species.setInitialAmount(sbml.getModel().getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue());
		if (enclosingCompartment.equals("")) {
			species.setCompartment(sbml.getModel().getCompartment(0).getId());
		} else {
			species.setCompartment(enclosingCompartment);
		}
		species.setBoundaryCondition(false);
		species.setConstant(false);
		species.setHasOnlySubstanceUnits(true);
		createProductionReaction(id,null,null,null,null,null,null);
		if (is_explicit) {
			Layout layout = null;
			if (sbmlLayout.getLayout("iBioSim") != null) {
				layout = sbmlLayout.getLayout("iBioSim"); 
			} else {
				layout = sbmlLayout.createLayout();
				layout.setId("iBioSim");
			}
			SpeciesGlyph speciesGlyph = null;
			if (layout.getSpeciesGlyph(id)!=null) {
				speciesGlyph = layout.getSpeciesGlyph(id);
			} else {
				speciesGlyph = layout.createSpeciesGlyph();
				speciesGlyph.setId(id);
				speciesGlyph.setSpeciesId(id);
			}
			speciesGlyph.getBoundingBox().setX(x);
			speciesGlyph.getBoundingBox().setY(y);
			speciesGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_SPECIES_WIDTH);
			speciesGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_SPECIES_HEIGHT);
			//prop.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_SPECIES_WIDTH));
			//prop.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_SPECIES_HEIGHT));
			TextGlyph textGlyph = layout.createTextGlyph();
			textGlyph.setId(id);
			textGlyph.setGraphicalObjectId(id);
			textGlyph.setText(id);
			textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
			//prop.setProperty(GlobalConstants.EXPLICIT_PROMOTER, GlobalConstants.TRUE);
			//centerVertexOverPoint(prop, x, y);
		}

		return id;
	}

	/**
	 * Given a properties list (species or components) and some coords, center
	 * over that point.
	 */
	/*
	public void centerVertexOverPoint(Properties prop, double x, double y) {
		x -= Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH))) / 2.0;
		y -= Double.parseDouble(prop.getProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT))) / 2.0;
		prop.setProperty("graphx", String.valueOf(x));
		prop.setProperty("graphy", String.valueOf(y));
	}
	*/

	public boolean isPromoterExplicit(String promoterId) {
		if (promoterId != null && sbmlLayout.getLayout("iBioSim").getSpeciesGlyph(promoterId)!=null)
			return true;
		else 
			return false;
	}
	
	public String influenceHasExplicitPromoter(String infName) {
		String promoterName;
		if (infName.contains(">")) {
			promoterName = infName.substring(infName.indexOf(">")+1);
		} else {
			promoterName = infName.substring(infName.indexOf("|")+1);
		}
		if (getPromoters().contains(promoterName)) return promoterName;
		else return null;
	}

	/*
	public String[] getSpeciesAsArray() {
		String[] s = new String[species.size()];
		s = species.keySet().toArray(s);
		Arrays.sort(s);
		return s;
	}

	public String[] getPromotersAsArray() {
		String[] s = new String[promoters.size()];
		s = promoters.keySet().toArray(s);
		Arrays.sort(s);
		return s;
	}
	*/

	public String[] getImplicitPromotersAsArray() {
		String[] s = new String[getPromoters().size()];
		int index = 0;
		for (String prom : getPromoters()) {
			if (!isPromoterExplicit(prom)) {
				s[index] = prom;
				index++;
			}
		}
		String[] implicit = new String[index];
		for (int i = 0; i < index; i++)
			implicit[i] = s[i];
		Arrays.sort(implicit);
		return implicit;
	}

	/*
	public HashMap<String, Properties> getPromoters() {
		return promoters;
	}

	public HashMap<String, String> getGlobalParameters() {
		return globalParameters;
	}

	public HashMap<String, String> getDefaultParameters() {
		return defaultParameters;
	}

	public HashMap<String, String> getParameters() {
		return parameters;
	}
	*/
	
	/*
	public boolean globalParameterIsSet(String parameter) {
		return globalParameters.containsKey(parameter);
	}
	*/

	public String getParameter(String parameter) {
		if (sbml != null && sbml.getModel().getInitialAssignment(parameter)!=null) {
			return SBMLutilities.myFormulaToString(sbml.getModel().getInitialAssignment(parameter).getMath());
		} else if (sbml != null && sbml.getModel().getParameter(parameter)!=null){
			return ""+sbml.getModel().getParameter(parameter).getValue();
		} else {
			return "1";
		}
		/*
		if (globalParameters.containsKey(parameter)) {
			return globalParameters.get(parameter);
		}
		else {
			return defaultParameters.get(parameter);
		}
		*/
	}

	public void setParameter(String parameter, String value) {
		//globalParameters.put(parameter, value);
		if (sbml != null) { 
			sbml.getModel().getParameter(parameter).setValue(Double.parseDouble(value));
		} 
		//parameters.put(parameter, value);
	}

	/*
	public void setDefaultParameter(String parameter, String value) {
		defaultParameters.put(parameter, value);
	}
	*/
	
	/*
	public void removeParameter(String parameter) {
		globalParameters.remove(parameter);
	}
	*/

	private void loadSBMLFile(String sbmlFile) {
		if (!sbmlFile.equals("")) {
			if (new File(path + separator + sbmlFile).exists()) {
				if (sbml != null) {
					SBMLDocument document = Gui.readSBML(path + separator + sbmlFile);
					sbml.setModel(document.getModel());
					sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
					sbml.setPkgRequired("layout", false); 
					sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
					sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
					sbml.setPkgRequired("comp", true); 
					sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
					sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
				} else {
					sbml = Gui.readSBML(path + separator + sbmlFile);
					sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
					sbml.setPkgRequired("layout", false); 
					sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
					sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
					sbml.setPkgRequired("comp", true); 
					sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
					sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
				} 	
			} else {
				createSBMLDocument(sbmlFile.replace(".xml",""));
			}
		} 
		/*
		pattern = Pattern.compile(SBML);
		matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String sbmlData = matcher.group();
			sbmlData = sbmlData.replaceFirst("SBML=","");
			SBMLReader reader = new SBMLReader();
			SBMLDocument document = reader.readSBMLFromString(sbmlData);
			sbml.setModel(document.getModel());
			sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
			sbml.setPkgRequired("layout", false); 
			sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
			sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
			sbml.setPkgRequired("comp", true); 
			sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
			sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		}
		*/
		if (sbml != null) {
			for (long i = 0; i < sbml.getModel().getNumCompartments(); i++) {
				if (sbml.getModel().getCompartment(i).isSetAnnotation()) {
					if (sbml.getModel().getCompartment(i).getAnnotation().toXMLString().contains("EnclosingCompartment")) {
						enclosingCompartment = sbml.getModel().getCompartment(i).getId();
						isWithinCompartment = true;
					}
				}
			}
		}
		usedIDs = SBMLutilities.CreateListOfUsedIDs(sbml);
		if (sbmlCompModel != null) {
			for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
				usedIDs.add(sbmlCompModel.getSubmodel(i).getId());
			}
		}
		loadGridSize();
	}

	private void loadSBMLFromBuffer(StringBuffer buffer) {
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = reader.readSBMLFromString(buffer.toString());
		sbml.setModel(document.getModel());
		sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
		sbml.setPkgRequired("layout", false); 
		sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
		sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
		sbml.setPkgRequired("comp", true); 
		sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
		sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		if (sbml != null) {
			for (long i = 0; i < sbml.getModel().getNumCompartments(); i++) {
				if (sbml.getModel().getCompartment(i).isSetAnnotation()) {
					if (sbml.getModel().getCompartment(i).getAnnotation().toXMLString().contains("EnclosingCompartment")) {
						enclosingCompartment = sbml.getModel().getCompartment(i).getId();
						isWithinCompartment = true;
					}
				}
			}
		}
	}
	
	public SBMLDocument flattenGCM() {
		ArrayList<String> gcms = new ArrayList<String>();
		gcms.add(filename);
		save(filename + ".temp");
		ArrayList<String> comps = new ArrayList<String>();
		for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
			comps.add(sbmlCompModel.getSubmodel(i).getId());
		}
		GCMFile gcm = new GCMFile(path);
		gcm.load(filename + ".temp");

		// loop through the keyset of the components of the gcm
		for (String s : comps) {
			GCMFile file = new GCMFile(path);

			// load the component's gcm into a new GCMFile
			String extModel = sbmlComp.getExternalModelDefinition(sbmlCompModel.getSubmodel(s)
					.getModelRef()).getSource().substring(7).replace(".xml",".gcm");
			file.load(path + separator + extModel);
			ArrayList<String> copy = copyArray(gcms);
			if (copy.contains(file.getFilename())) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten GCM.\n" + "There is a loop in the components.");
				load(filename + ".temp");
				new File(filename + ".temp").delete();
				return null;
			}
			copy.add(file.getFilename());

			// recursively add this component's sbml (and its inside components'
			// sbml, etc.) to the overall sbml
			sbml = unionSBML(gcm, unionGCM(this, file, s, copy), s, 
					file.getIsWithinCompartment(),file.getParameter(GlobalConstants.RNAP_STRING),
					file.getEnclosingCompartment());
			if (sbml == null && copy.isEmpty()) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten GCM.\n" + "There is a loop in the components.");
				load(filename + ".temp");
				new File(filename + ".temp").delete();
				return null;
			}
			else if (sbml == null) {
				Utility.createErrorMessage("Cannot Merge SBMLs", "Unable to merge sbml files from components.");
				load(filename + ".temp");
				new File(filename + ".temp").delete();
				return null;
			}
		}
		sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", false);
		sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", false);
		//components = new HashMap<String, Properties>();
		if (sbml != null) {
			sbml.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
			sbml.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
			sbml.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
			sbml.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
			sbml.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
			sbml.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
			sbml.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
			long numErrors = sbml.checkConsistency();
			if (numErrors > 0) {
				Utility.createErrorMessage("Merged SBMLs Are Inconsistent", "The merged sbml files have inconsistencies.");
			}
		}
		new File(filename + ".temp").delete();
		return sbml;
	}

	private GCMFile unionGCM(GCMFile topLevel, GCMFile bottomLevel, String compName, ArrayList<String> gcms) {
		ArrayList<String> mod = new ArrayList<String>();
		for (long i = 0; i < bottomLevel.getSBMLCompModel().getNumSubmodels(); i++) {
			mod.add(bottomLevel.getSBMLCompModel().getSubmodel(i).getId());
		}
		for (String s : mod) {
			GCMFile file = new GCMFile(path);
			String extModel = bottomLevel.getSBMLComp().getExternalModelDefinition(bottomLevel.getSBMLCompModel().getSubmodel(s)
					.getModelRef()).getSource().substring(7).replace(".xml",".gcm");
			file.load(path + separator + extModel);
			ArrayList<String> copy = copyArray(gcms);
			if (copy.contains(file.getFilename())) {
				while (!gcms.isEmpty()) {
					gcms.remove(0);
				}
				return null;
			}
			copy.add(file.getFilename());
			bottomLevel.setSBMLDocument(unionSBML(bottomLevel, unionGCM(bottomLevel, file, s, copy), s, 
					file.getIsWithinCompartment(),file.getParameter(GlobalConstants.RNAP_STRING),
					file.getEnclosingCompartment()));
		}

		// change the names of the bottom-level stuff
		// prepend the component name to the species to preserve hierarchy
		/*
		mod = bottomLevel.getPromoters();
		for (String prom : mod) {
			bottomLevel.changePromoterName(prom, compName + "__" + prom);
		}
		mod = bottomLevel.getSpecies();
		for (String spec : mod) {
			bottomLevel.changeSpeciesName(spec, compName + "__" + spec);
			for (long i = 0; i < topLevel.getSBMLDocument().getModel().getNumSpecies(); i++) {
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)topLevel.getSBMLDocument().getModel().getSpecies(i).getPlugin("comp");
				ReplacedElement replacement = null;
				for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
					replacement = sbmlSBase.getReplacedElement(j);
					if (replacement.getSubmodelRef().equals(compName)) {
						if (spec.equals(compName + "__" + replacement.getIdRef())) {
							bottomLevel.changeSpeciesName(spec, topLevel.getSBMLDocument().getModel().getSpecies(i).getId());
						}
					}
				}
			}
		}
		*/
		return bottomLevel;
	}
	
	private SBMLDocument unionSBML(GCMFile mainGCM, GCMFile gcm, String compName, 
			boolean isWithinCompartment, String RNAPamount, String topComp) {
		SBMLDocument mainDoc = mainGCM.getSBMLDocument();
		SBMLDocument doc = gcm.getSBMLDocument();
		Model m = doc.getModel();
		for (int i = 0; i < m.getNumCompartmentTypes(); i++) {
			org.sbml.libsbml.CompartmentType c = m.getCompartmentType(i);
			String newName = compName + "__" + c.getId();
			updateVarId(false, c.getId(), newName, doc);
			c.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumCompartmentTypes(); j++) {
				if (mainDoc.getModel().getCompartmentType(j).getId().equals(c.getId())) {
					add = false;
					org.sbml.libsbml.CompartmentType comp = mainDoc.getModel().getCompartmentType(j);
					if (!c.getName().equals(comp.getName())) {
						return null;
					}
				}
			}
			if (add) {
				mainDoc.getModel().addCompartmentType(c);
			}
		}
		for (int i = 0; i < m.getNumCompartments(); i++) {
			org.sbml.libsbml.Compartment c = m.getCompartment(i);
			if (isWithinCompartment || c.getId().contains("__")) {
				updateVarId(false, c.getId(), compName + "__" + c.getId(), doc);
				compartments.remove(c.getId());
				c.setId(compName + "__" + c.getId());
			}
			else {
				if (topComp.equals("")) {
					topComp = mainDoc.getModel().getCompartment(0).getId();
				}
				updateVarId(false, c.getId(), topComp, doc);
				compartments.remove(c.getId());
				c.setId(topComp);
			}

			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumCompartments(); j++) {
				if (mainDoc.getModel().getCompartment(j).getId().equals(c.getId())) {
					add = false;
					/*
					 * org.sbml.libsbml.Compartment comp =
					 * mainDoc.getModel().getCompartment(j); if
					 * (!c.getName().equals(comp.getName())) { return null; } if
					 * (
					 * !c.getCompartmentType().equals(comp.getCompartmentType())
					 * ) { return null; } if (c.getConstant() !=
					 * comp.getConstant()) { return null; } if
					 * (!c.getOutside().equals(comp.getOutside())) { return
					 * null; } if (c.getVolume() != comp.getVolume()) { return
					 * null; } if (c.getSpatialDimensions() !=
					 * comp.getSpatialDimensions()) { return null; } if
					 * (c.getSize() != comp.getSize()) { return null; } if
					 * (!c.getUnits().equals(comp.getUnits())) { return null; }
					 */
				}
			}
			if (add) {
				mainDoc.getModel().addCompartment(c);
				if (!compartments.containsKey(c.getId())) {
					Properties prop = new Properties();
					prop.put(GlobalConstants.RNAP_STRING,RNAPamount);
					compartments.put(c.getId(),prop);
				}
			}
		}
		for (int i = 0; i < m.getNumSpeciesTypes(); i++) {
			org.sbml.libsbml.SpeciesType s = m.getSpeciesType(i);
			String newName = compName + "__" + s.getId();
			updateVarId(false, s.getId(), newName, doc);
			s.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumSpeciesTypes(); j++) {
				if (mainDoc.getModel().getSpeciesType(j).getId().equals(s.getId())) {
					add = false;
					org.sbml.libsbml.SpeciesType spec = mainDoc.getModel().getSpeciesType(j);
					if (!s.getName().equals(spec.getName())) {
						return null;
					}
				}
			}
			if (add) {
				mainDoc.getModel().addSpeciesType(s);
			}
		}
		for (long i = 0; i < m.getNumSpecies(); i++) {
			Species spec = m.getSpecies(i);
			String newName = compName + "__" + spec.getId();
			for (long j = 0; j < mainDoc.getModel().getNumSpecies(); j++) {
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)mainDoc.getModel().getSpecies(j).getPlugin("comp");
				ReplacedElement replacement = null;
				for (long k = 0; k < sbmlSBase.getNumReplacedElements(); k++) {
					replacement = sbmlSBase.getReplacedElement(k);
					if (replacement.getSubmodelRef().equals(compName)) {
						if (spec.getId().equals(replacement.getIdRef())) {
							newName = "_" + compName + "__" + mainDoc.getModel().getSpecies(j).getId();
						}
					}
				}
			}
			if (gcm.getPromoters().contains(spec.getId())) {
				gcm.changePromoterName(spec.getId(), newName);
			} else {
				gcm.changeSpeciesName(spec.getId(), newName);
			}
			//updateVarId(true, spec.getId(), newName, doc);
			//spec.setId(newName);
		}
		for (int i = 0; i < m.getNumSpecies(); i++) {
			Species spec = m.getSpecies(i);
			boolean add = true;
			if (spec.getId().startsWith("_" + compName + "__")) {
				if (gcm.getPromoters().contains(spec.getId())) {
					gcm.changePromoterName(spec.getId(), spec.getId().substring(3 + compName.length()));
				} else {
					gcm.changeSpeciesName(spec.getId(), spec.getId().substring(3 + compName.length()));
				}
				/*
				updateVarId(true, spec.getId(), spec.getId().substring(3 + compName.length()), doc);
				spec.setId(spec.getId().substring(3 + compName.length()));
				*/
			}
			else {
				for (int j = 0; j < mainDoc.getModel().getNumSpecies(); j++) {
					if (mainDoc.getModel().getSpecies(j).getId().equals(spec.getId())) {
						Species s = mainDoc.getModel().getSpecies(j);
						if (!s.getName().equals(spec.getName())) {
							return null;
						}
						if (!s.getCompartment().equals(spec.getCompartment())) {
							return null;
						}
						if (s.getConstant() != spec.getConstant()) {
							return null;
						}
						if (s.getBoundaryCondition() != spec.getBoundaryCondition()) {
							return null;
						}
						if (s.getHasOnlySubstanceUnits() != spec.getHasOnlySubstanceUnits()) {
							return null;
						}
						if (s.getCharge() != spec.getCharge()) {
							return null;
						}
						if (!s.getSpatialSizeUnits().equals(spec.getSpatialSizeUnits())) {
							return null;
						}
						if (s.getInitialAmount() != spec.getInitialAmount()) {
							return null;
						}
						if (s.getInitialConcentration() != spec.getInitialConcentration()) {
							return null;
						}
						if (!s.getSpeciesType().equals(spec.getSpeciesType())) {
							return null;
						}
						if (!s.getSubstanceUnits().equals(spec.getSubstanceUnits())) {
							return null;
						}
						if (!s.getUnits().equals(spec.getUnits())) {
							return null;
						}
						add = false;
					}
				}
			}
			if (add) {
				mainDoc.getModel().addSpecies(spec);
			}
		}
		for (int i = 0; i < m.getNumParameters(); i++) {
			Parameter p = m.getParameter(i);
			String newName = compName + "__" + p.getId();
			updateVarId(false, p.getId(), newName, doc);
			p.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumParameters(); j++) {
				if (mainDoc.getModel().getParameter(j).getId().equals(p.getId())) {
					add = false;
					Parameter param = mainDoc.getModel().getParameter(j);
					if (!p.getName().equals(param.getName())) {
						return null;
					}
					if (!p.getUnits().equals(param.getUnits())) {
						return null;
					}
					if (p.getConstant() != param.getConstant()) {
						return null;
					}
					if (p.getValue() != param.getValue()) {
						return null;
					}
				}
			}
			if (add) {
				mainDoc.getModel().addParameter(p);
			}
		}
		for (int i = 0; i < m.getNumReactions(); i++) {
			org.sbml.libsbml.Reaction r = m.getReaction(i);
			String newName = compName + "__" + r.getId();
			updateVarId(false, r.getId(), newName, doc);
			r.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumReactions(); j++) {
				if (mainDoc.getModel().getReaction(j).getId().equals(r.getId())) {
					add = false;
					org.sbml.libsbml.Reaction reac = mainDoc.getModel().getReaction(j);
					if (!r.getName().equals(reac.getName())) {
						return null;
					}
					if (r.getFast() != reac.getFast()) {
						return null;
					}
					if (r.getReversible() != reac.getReversible()) {
						return null;
					}
					if (!r.getKineticLaw().equals(reac.getKineticLaw())) {
						return null;
					}
					for (int k = 0; k < reac.getNumModifiers(); k++) {
						ModifierSpeciesReference mod = reac.getModifier(k);
						boolean found = false;
						for (int l = 0; l < r.getNumModifiers(); l++) {
							ModifierSpeciesReference mod2 = r.getModifier(l);
							if (mod.getId().equals(mod2.getId())) {
								found = true;
								if (!mod.getSpecies().equals(mod2.getSpecies())) {
									return null;
								}
							}
						}
						if (!found) {
							return null;
						}
					}
					for (int k = 0; k < r.getNumModifiers(); k++) {
						ModifierSpeciesReference mod = r.getModifier(k);
						boolean found = false;
						for (int l = 0; l < reac.getNumModifiers(); l++) {
							ModifierSpeciesReference mod2 = reac.getModifier(l);
							if (mod.getId().equals(mod2.getId())) {
								found = true;
								if (!mod.getSpecies().equals(mod2.getSpecies())) {
									return null;
								}
							}
						}
						if (!found) {
							return null;
						}
					}
					for (int k = 0; k < reac.getNumProducts(); k++) {
						SpeciesReference p = reac.getProduct(k);
						boolean found = false;
						for (int l = 0; l < r.getNumProducts(); l++) {
							SpeciesReference prod = r.getProduct(l);
							if (p.getId().equals(prod.getId())) {
								found = true;
								if (!p.getSpecies().equals(prod.getSpecies())) {
									return null;
								}
								if (!p.getStoichiometryMath().equals(prod.getStoichiometryMath())) {
									return null;
								}
								if (p.getStoichiometry() != prod.getStoichiometry()) {
									return null;
								}
								if (p.getDenominator() != prod.getDenominator()) {
									return null;
								}
							}
						}
						if (!found) {
							return null;
						}
					}
					for (int k = 0; k < r.getNumProducts(); k++) {
						SpeciesReference p = r.getProduct(k);
						boolean found = false;
						for (int l = 0; l < reac.getNumProducts(); l++) {
							SpeciesReference prod = reac.getProduct(l);
							if (p.getId().equals(prod.getId())) {
								found = true;
								if (!p.getSpecies().equals(prod.getSpecies())) {
									return null;
								}
								if (!p.getStoichiometryMath().equals(prod.getStoichiometryMath())) {
									return null;
								}
								if (p.getStoichiometry() != prod.getStoichiometry()) {
									return null;
								}
								if (p.getDenominator() != prod.getDenominator()) {
									return null;
								}
							}
						}
						if (!found) {
							return null;
						}
					}
					for (int k = 0; k < reac.getNumReactants(); k++) {
						SpeciesReference react = reac.getReactant(k);
						boolean found = false;
						for (int l = 0; l < r.getNumReactants(); l++) {
							SpeciesReference react2 = r.getReactant(l);
							if (react.getId().equals(react2.getId())) {
								found = true;
								if (!react.getSpecies().equals(react2.getSpecies())) {
									return null;
								}
								if (!react.getStoichiometryMath().equals(react2.getStoichiometryMath())) {
									return null;
								}
								if (react.getStoichiometry() != react2.getStoichiometry()) {
									return null;
								}
								if (react.getDenominator() != react2.getDenominator()) {
									return null;
								}
							}
						}
						if (!found) {
							return null;
						}
					}
					for (int k = 0; k < r.getNumReactants(); k++) {
						SpeciesReference react = r.getReactant(k);
						boolean found = false;
						for (int l = 0; l < reac.getNumReactants(); l++) {
							SpeciesReference react2 = reac.getReactant(l);
							if (react.getId().equals(react2.getId())) {
								found = true;
								if (!react.getSpecies().equals(react2.getSpecies())) {
									return null;
								}
								if (!react.getStoichiometryMath().equals(react2.getStoichiometryMath())) {
									return null;
								}
								if (react.getStoichiometry() != react2.getStoichiometry()) {
									return null;
								}
								if (react.getDenominator() != react2.getDenominator()) {
									return null;
								}
							}
						}
						if (!found) {
							return null;
						}
					}
				}
			}
			if (add) {
				mainDoc.getModel().addReaction(r);
			}
		}
		for (int i = 0; i < m.getNumInitialAssignments(); i++) {
			InitialAssignment init = (InitialAssignment) m.getListOfInitialAssignments().get(i);
			mainDoc.getModel().addInitialAssignment(init);
		}
		for (int i = 0; i < m.getNumRules(); i++) {
			org.sbml.libsbml.Rule r = m.getRule(i);
			mainDoc.getModel().addRule(r);
		}
		for (int i = 0; i < m.getNumConstraints(); i++) {
			Constraint constraint = (Constraint) m.getListOfConstraints().get(i);
			String newName = compName + "__" + constraint.getMetaId();
			constraint.setMetaId(newName);
			for (int j = 0; j < mainDoc.getModel().getNumConstraints(); j++) {
				if (mainDoc.getModel().getConstraint(j).getMetaId().equals(constraint.getMetaId())) {
					Constraint c = mainDoc.getModel().getConstraint(j);
					if (!c.getMessageString().equals(constraint.getMessageString())) {
						return null;
					}
					if (c.getMath() != constraint.getMath()) {
						return null;
					}
				}
			}
			mainDoc.getModel().addConstraint(constraint);
		}
		for (int i = 0; i < m.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) m.getListOfEvents().get(i);
			String newName = compName + "__" + event.getId();
			updateVarId(false, event.getId(), newName, doc);
			event.setId(newName);
			for (int j = 0; j < mainDoc.getModel().getNumEvents(); j++) {
				if (mainDoc.getModel().getEvent(j).getId().equals(event.getId())) {
					org.sbml.libsbml.Event e = mainDoc.getModel().getEvent(j);
					if (!e.getName().equals(event.getName())) {
						return null;
					}
					if (e.getUseValuesFromTriggerTime() != event.getUseValuesFromTriggerTime()) {
						return null;
					}
					if (!e.getDelay().equals(event.getDelay())) {
						return null;
					}
					if (!e.getTimeUnits().equals(event.getTimeUnits())) {
						return null;
					}
					if (!e.getTrigger().equals(event.getTrigger())) {
						return null;
					}
					for (int k = 0; k < e.getNumEventAssignments(); k++) {
						EventAssignment a = e.getEventAssignment(k);
						boolean found = false;
						for (int l = 0; l < event.getNumEventAssignments(); l++) {
							EventAssignment assign = event.getEventAssignment(l);
							if (a.getVariable().equals(assign.getVariable())) {
								found = true;
								if (!a.getMath().equals(assign.getMath())) {
									return null;
								}
							}
						}
						if (!found) {
							return null;
						}
					}
				}
			}
			mainDoc.getModel().addEvent(event);
		}
		for (int i = 0; i < m.getNumUnitDefinitions(); i++) {
			UnitDefinition u = m.getUnitDefinition(i);
			String newName = u.getId();
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumUnitDefinitions(); j++) {
				if (mainDoc.getModel().getUnitDefinition(j).getId().equals(u.getId())) {
					if (UnitDefinition.areIdentical(mainDoc.getModel().getUnitDefinition(j), u)) {
						add = false;
					}
					else {
						newName = compName + "__" + u.getId();
					}
				}
			}
			if (add) {
				u.setId(newName);
				mainDoc.getModel().addUnitDefinition(u);
			}
		}
		for (int i = 0; i < m.getNumFunctionDefinitions(); i++) {
			FunctionDefinition f = m.getFunctionDefinition(i);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumFunctionDefinitions(); j++) {
				if (mainDoc.getModel().getFunctionDefinition(j).getId().equals(f.getId())) {
					add = false;
				}
			}
			if (add) {
				mainDoc.getModel().addFunctionDefinition(f);
			}
		}
		return mainDoc;
	}

	private String myFormulaToString(ASTNode mathFormula) {
		String formula = libsbml.formulaToString(mathFormula);
		formula = formula.replaceAll("arccot", "acot");
		formula = formula.replaceAll("arccoth", "acoth");
		formula = formula.replaceAll("arccsc", "acsc");
		formula = formula.replaceAll("arccsch", "acsch");
		formula = formula.replaceAll("arcsec", "asec");
		formula = formula.replaceAll("arcsech", "asech");
		formula = formula.replaceAll("arccosh", "acosh");
		formula = formula.replaceAll("arcsinh", "asinh");
		formula = formula.replaceAll("arctanh", "atanh");
		String newformula = formula.replaceFirst("00e", "0e");
		while (!(newformula.equals(formula))) {
			formula = newformula;
			newformula = formula.replaceFirst("0e\\+", "e+");
			newformula = newformula.replaceFirst("0e-", "e-");
		}
		formula = formula.replaceFirst("\\.e\\+", ".0e+");
		formula = formula.replaceFirst("\\.e-", ".0e-");
		return formula;
	}

	private ASTNode myParseFormula(String formula) {
		ASTNode mathFormula = libsbml.parseFormula(formula);
		if (mathFormula == null)
			return null;
		setTimeAndTrigVar(mathFormula);
		return mathFormula;
	}

	private String updateFormulaVar(String s, String origVar, String newVar) {
		s = " " + s + " ";
		s = s.replace(" " + origVar + " ", " " + newVar + " ");
		s = s.replace(" " + origVar + "(", " " + newVar + "(");
		s = s.replace("(" + origVar + ")", "(" + newVar + ")");
		s = s.replace("(" + origVar + " ", "(" + newVar + " ");
		s = s.replace("(" + origVar + ",", "(" + newVar + ",");
		s = s.replace(" " + origVar + ")", " " + newVar + ")");
		s = s.replace(" " + origVar + "^", " " + newVar + "^");
		return s.trim();
	}

	private ASTNode updateMathVar(ASTNode math, String origVar, String newVar) {
		String s = updateFormulaVar(myFormulaToString(math), origVar, newVar);
		return myParseFormula(s);
	}

	private void updateVarId(boolean isSpecies, String origId, String newId, SBMLDocument document) {
		if (origId.equals(newId))
			return;
		Model model = document.getModel();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			org.sbml.libsbml.Species species = (org.sbml.libsbml.Species) model.getListOfSpecies()
					.get(i);
			if (species.getCompartment().equals(origId)) {
				species.setCompartment(newId);
			}
			if (species.getSpeciesType().equals(origId)) {
				species.setSpeciesType(newId);
			}
		}
		for (int i = 0; i < model.getNumCompartments(); i++) {
			org.sbml.libsbml.Compartment compartment = (org.sbml.libsbml.Compartment) model
					.getListOfCompartments().get(i);
			if (compartment.getCompartmentType().equals(origId)) {
				compartment.setCompartmentType(newId);
			}
		}
		for (int i = 0; i < model.getNumReactions(); i++) {
			org.sbml.libsbml.Reaction reaction = (org.sbml.libsbml.Reaction) model
					.getListOfReactions().get(i);
			if (!reaction.isSetCompartment() || reaction.getCompartment().equals(origId)) {
				reaction.setCompartment(newId);
			}
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						specRef.getStoichiometryMath().setMath(
								updateMathVar(specRef.getStoichiometryMath().getMath(), origId,
										newId));
					}
				}
			}
			if (isSpecies) {
				for (int j = 0; j < reaction.getNumModifiers(); j++) {
					if (reaction.getModifier(j).isSetSpecies()) {
						ModifierSpeciesReference specRef = reaction.getModifier(j);
						if (origId.equals(specRef.getSpecies())) {
							specRef.setSpecies(newId);
						}
					}
				}
			}
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getReactant(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						specRef.getStoichiometryMath().setMath(
								updateMathVar(specRef.getStoichiometryMath().getMath(), origId,
										newId));
					}
				}
			}
			reaction.getKineticLaw().setMath(
					updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
		}
		if (model.getNumInitialAssignments() > 0) {
			for (int i = 0; i < model.getNumInitialAssignments(); i++) {
				InitialAssignment init = (InitialAssignment) model.getListOfInitialAssignments()
						.get(i);
				if (origId.equals(init.getSymbol())) {
					init.setSymbol(newId);
				}
				init.setMath(updateMathVar(init.getMath(), origId, newId));
			}
		}
		if (model.getNumRules() > 0) {
			for (int i = 0; i < model.getNumRules(); i++) {
				Rule rule = (Rule) model.getListOfRules().get(i);
				if (rule.isSetVariable() && origId.equals(rule.getVariable())) {
					rule.setVariable(newId);
				}
				rule.setMath(updateMathVar(rule.getMath(), origId, newId));
			}
		}
		if (model.getNumConstraints() > 0) {
			for (int i = 0; i < model.getNumConstraints(); i++) {
				Constraint constraint = (Constraint) model.getListOfConstraints().get(i);
				constraint.setMath(updateMathVar(constraint.getMath(), origId, newId));
			}
		}
		if (model.getNumEvents() > 0) {
			for (int i = 0; i < model.getNumEvents(); i++) {
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) model.getListOfEvents()
						.get(i);
				if (event.isSetTrigger()) {
					event.getTrigger().setMath(
							updateMathVar(event.getTrigger().getMath(), origId, newId));
				}
				if (event.isSetDelay()) {
					event.getDelay().setMath(
							updateMathVar(event.getDelay().getMath(), origId, newId));
				}
				for (int j = 0; j < event.getNumEventAssignments(); j++) {
					EventAssignment ea = (EventAssignment) event.getListOfEventAssignments().get(j);
					if (ea.getVariable().equals(origId)) {
						ea.setVariable(newId);
					}
					if (ea.isSetMath()) {
						ea.setMath(updateMathVar(ea.getMath(), origId, newId));
					}
				}
			}
		}
	}

	private void setTimeAndTrigVar(ASTNode node) {
		if (node.getType() == libsbml.AST_NAME) {
			if (node.getName().equals("t")) {
				node.setType(libsbml.AST_NAME_TIME);
			}
			else if (node.getName().equals("time")) {
				node.setType(libsbml.AST_NAME_TIME);
			}
		}
		if (node.getType() == libsbml.AST_FUNCTION) {
			if (node.getName().equals("acot")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOT);
			}
			else if (node.getName().equals("acoth")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOTH);
			}
			else if (node.getName().equals("acsc")) {
				node.setType(libsbml.AST_FUNCTION_ARCCSC);
			}
			else if (node.getName().equals("acsch")) {
				node.setType(libsbml.AST_FUNCTION_ARCCSCH);
			}
			else if (node.getName().equals("asec")) {
				node.setType(libsbml.AST_FUNCTION_ARCSEC);
			}
			else if (node.getName().equals("asech")) {
				node.setType(libsbml.AST_FUNCTION_ARCSECH);
			}
			else if (node.getName().equals("acosh")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOSH);
			}
			else if (node.getName().equals("asinh")) {
				node.setType(libsbml.AST_FUNCTION_ARCSINH);
			}
			else if (node.getName().equals("atanh")) {
				node.setType(libsbml.AST_FUNCTION_ARCTANH);
			}
		}

		for (int c = 0; c < node.getNumChildren(); c++)
			setTimeAndTrigVar(node.getChild(c));
	}

	public String getFilename() {
		return filename;
	}

	public String getPath() {
		return path;
	}

	
	//UNDO-REDO METHODS
	
	public void makeUndoPoint() {
		StringBuffer up = saveToBuffer();
		undoManager.makeUndoPoint(up);
	}

	public void undo() {
		StringBuffer p = (StringBuffer) undoManager.undo();
		if (p != null)
			this.loadSBMLFromBuffer(p);
	}

	public void redo() {
		StringBuffer p = (StringBuffer) undoManager.redo();
		if (p != null)
			this.loadSBMLFromBuffer(p);
	}
	
	
	//GRID

	public Grid getGrid() {
		return grid;
	}
	
	/*
	public void setGrid(Grid g) {
		grid = g;
	}	
	*/
	
	public void buildGrid(int rows, int cols) {
		
		grid.createGrid(rows, cols, this, null);
	}
	
	/**
	 * reloads the grid from file
	 */
	public void reloadGrid() {
		// TODO: This should be done with SBML file
		/*
		StringBuffer data = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}
		
		parseGridSize(data);
		*/
	}
	
	public boolean getGridEnabledFromFile(String filename) {
		
		StringBuffer data = new StringBuffer();
		
		if (filename == null) return true;

		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String str;
			while ((str = in.readLine()) != null) {
				data.append(str + "\n");
			}
			in.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException("Error opening file");
		}		
		
		// TODO: THIS SHOULD BE GOTTEN FROM SBML FILE
		/*
		Pattern network = Pattern.compile(GRID);
		Matcher matcher = network.matcher(data.toString());
		
		if (!matcher.find()) return false;
		
		String info = matcher.group(1);
		
		if (info != null) return true;
		*/
		
		return false;
	}
	
	
	
	//ENCLOSING COMPARTMENT
	
	public String getEnclosingCompartment() {
		return enclosingCompartment;
	}

	public void setEnclosingCompartment(String enclosingCompartment) {
		if (!this.enclosingCompartment.equals("")) {
			Compartment compartment = sbml.getModel().getCompartment(this.enclosingCompartment);
			if (compartment != null) compartment.unsetAnnotation();
		}
		if (enclosingCompartment != null && !enclosingCompartment.equals("")) {
			Compartment compartment = sbml.getModel().getCompartment(enclosingCompartment);
			if (compartment != null) compartment.setAnnotation("EnclosingCompartment");
		}
		this.enclosingCompartment = enclosingCompartment;
	}
	
	private SBMLDocument sbml = null;
	
	private LayoutModelPlugin sbmlLayout = null;
	
	private CompSBMLDocumentPlugin sbmlComp = null;
	
	private CompModelPlugin sbmlCompModel = null;
	
	private MySpecies speciesPanel = null;
	
	private Reactions reactionPanel = null;
	
	private Grid grid = null;
	
	private boolean isWithinCompartment;
	
	private String enclosingCompartment;

	private ArrayList<String >usedIDs;
	
	private String path;

	private String sbmlFile = "";
	
	//private GCM2SBML gcm2sbml = null;

	private HashMap<String, Properties> compartments;
}
