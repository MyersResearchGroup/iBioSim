package biomodel.parser;


import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.BoundingBox;
import org.sbml.libsbml.CompExtension;
import org.sbml.libsbml.CompModelPlugin;
import org.sbml.libsbml.CompSBMLDocumentPlugin;
import org.sbml.libsbml.CompSBasePlugin;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentGlyph;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.Deletion;
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
import org.sbml.libsbml.ModelDefinition;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Port;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.ReactionGlyph;
import org.sbml.libsbml.ReplacedElement;
import org.sbml.libsbml.Replacing;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.SBaseList;
import org.sbml.libsbml.SBaseRef;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesGlyph;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SpeciesType;
import org.sbml.libsbml.Submodel;
import org.sbml.libsbml.TextGlyph;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.Layout;
import org.sbml.libsbml.XMLAttributes;
import org.sbml.libsbml.XMLNode;
import org.sbml.libsbml.XMLTriple;
import org.sbml.libsbml.libsbml;

import biomodel.gui.Grid;
import biomodel.gui.textualeditor.Constraints;
import biomodel.gui.textualeditor.Events;
import biomodel.gui.textualeditor.MySpecies;
import biomodel.gui.textualeditor.Parameters;
import biomodel.gui.textualeditor.Reactions;
import biomodel.gui.textualeditor.Rules;
import biomodel.gui.textualeditor.SBMLutilities;
import biomodel.network.AbstractionEngine;
import biomodel.network.GeneticNetwork;
import biomodel.network.Promoter;
import biomodel.network.SpeciesInterface;
import biomodel.util.GlobalConstants;
import biomodel.util.UndoManager;
import biomodel.util.Utility;


import lpn.parser.LhpnFile;
import main.Gui;
import main.Log;
import main.util.MutableString;


/**
 * This class describes a GCM file
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class BioModel {

	private String separator;

	private String filename = null;

	private UndoManager undoManager;

	public BioModel(String path) {
		//gcm2sbml = new GCM2SBML(this);
		undoManager = new UndoManager();
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.path = path;
		grid = new Grid();
		compartments = new HashMap<String, Properties>();
	}
	
	public void createSBMLDocument(String modelId,boolean grid) {
		sbml = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		Model m = sbml.createModel();
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, m, metaIDIndex); 
		sbml.setModel(m);
		m.setId(modelId);
		Compartment c = m.createCompartment();
		if (grid) {
			c.setId("Grid");
		} else {
			c.setId("Cell");
		}
		c.setSize(1);
		c.setSpatialDimensions(3);
		c.setConstant(true);
		//SBMLutilities.addRandomFunctions(sbml);
		loadDefaultParameters();
		sbmlFile = modelId + ".xml";
		sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
		sbml.setPackageRequired("layout", false); 
		sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
		sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
		sbml.setPackageRequired("comp", true);
		((CompSBMLDocumentPlugin)sbml.getPlugin("comp")).setRequired(true);
		sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
		sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
	}

	private void loadDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();

		createGlobalParameter(GlobalConstants.FORWARD_KREP_STRING, biosimrc.get("biosim.gcm.KREP_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KREP_STRING, "1");
		
		createGlobalParameter(GlobalConstants.FORWARD_KACT_STRING, biosimrc.get("biosim.gcm.KACT_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KACT_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_KCOMPLEX_STRING, biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_KCOMPLEX_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_RNAP_BINDING_STRING, biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_RNAP_BINDING_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING, 
				biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_ACTIVATED_RNAP_BINDING_STRING, "1");

		createGlobalParameter(GlobalConstants.FORWARD_MEMDIFF_STRING, biosimrc.get("biosim.gcm.FORWARD_MEMDIFF_VALUE", ""));
		createGlobalParameter(GlobalConstants.REVERSE_MEMDIFF_STRING, biosimrc.get("biosim.gcm.REVERSE_MEMDIFF_VALUE", ""));

		createGlobalParameter(GlobalConstants.KDECAY_STRING, biosimrc.get("biosim.gcm.KDECAY_VALUE", ""));
		createGlobalParameter(GlobalConstants.KECDECAY_STRING, biosimrc.get("biosim.gcm.KECDECAY_VALUE", ""));
		createGlobalParameter(GlobalConstants.COOPERATIVITY_STRING, biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", ""));
		createGlobalParameter(GlobalConstants.RNAP_STRING, biosimrc.get("biosim.gcm.RNAP_VALUE", ""));
		createGlobalParameter(GlobalConstants.OCR_STRING, biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		createGlobalParameter(GlobalConstants.KBASAL_STRING, biosimrc.get("biosim.gcm.KBASAL_VALUE", ""));
		createGlobalParameter(GlobalConstants.PROMOTER_COUNT_STRING, biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		createGlobalParameter(GlobalConstants.STOICHIOMETRY_STRING, biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", ""));
		createGlobalParameter(GlobalConstants.ACTIVATED_STRING, biosimrc.get("biosim.gcm.ACTIVED_VALUE", ""));
		createGlobalParameter(GlobalConstants.KECDIFF_STRING, biosimrc.get("biosim.gcm.KECDIFF_VALUE", ""));
	}
	
	public boolean IsWithinCompartment() {
		for (long i = 0; i < sbml.getModel().getNumCompartments(); i++) {
			Compartment compartment = sbml.getModel().getCompartment(i);
			if (sbmlCompModel.getPort(GlobalConstants.COMPARTMENT + "__" + compartment.getId()) != null) return false;
		}
		return true;
	}
	
	/*
	public void setIsWithinCompartment(boolean isWithinCompartment) {
		if (isWithinCompartment && !this.isWithinCompartment) {
			sbmlCompModel.getPort(GlobalConstants.DEFAULT_COMPARTMENT).setId(GlobalConstants.ENCLOSING_COMPARTMENT);
		} else if  (!isWithinCompartment && this.isWithinCompartment) {
			sbmlCompModel.getPort(GlobalConstants.ENCLOSING_COMPARTMENT).setId(GlobalConstants.DEFAULT_COMPARTMENT);
		}
		this.isWithinCompartment = isWithinCompartment;
	}
	*/
	
	public String getSBMLFile() {
		return sbmlFile;
	}

	public void setSBMLFile(String file) {
		sbmlFile = file;
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

	public Rules getRulePanel() {
		return rulePanel;
	}

	public void setRulePanel(Rules rulePanel) {
		this.rulePanel = rulePanel;
	}

	public Constraints getConstraintPanel() {
		return constraintPanel;
	}

	public void setConstraintPanel(Constraints constraintPanel) {
		this.constraintPanel = constraintPanel;
	}

	public Events getEventPanel() {
		return eventPanel;
	}

	public void setEventPanel(Events eventPanel) {
		this.eventPanel = eventPanel;
	}

	public Parameters getParameterPanel() {
		return parameterPanel;
	}

	public void setParameterPanel(Parameters parameterPanel) {
		this.parameterPanel = parameterPanel;
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
		SBMLutilities.fillBlankMetaIDs(sbml);
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
		sbml = Gui.readSBML(path + separator + sbmlFile);
		sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
		sbml.setPackageRequired("layout", false); 
		sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
		sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
		sbml.setPackageRequired("comp", true); 
		((CompSBMLDocumentPlugin)sbml.getPlugin("comp")).setRequired(true);
		sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
		sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
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
							Object[] sort = main.util.Utility.add(conLevel.get(number),
									consLevel.get(number), add, null, null, null, null, null, naryFrame);
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
					conLevel.set(number, main.util.Utility
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
				flattenModel();
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
		if (network == null) return null;
		network.markAbstractable();
		AbstractionEngine abs = network.createAbstractionEngine();
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
					if (!(getSpeciesType(sbml,specs.get(i)).equals(GlobalConstants.INPUT))) {
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
						Reaction reaction = getDegradationReaction(specs.get(i));
						if (reaction != null) {
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
	
	public String getDefaultCompartment() {
		return sbml.getModel().getCompartment(0).getId();
	}
	
	// Note this doesn't appear to be used anywhere
	public void createSpeciesFromGCM(String s,Properties property) {
		Species species = sbml.getModel().getSpecies(s);
		if (species==null) {
			species = sbml.getModel().createSpecies();
			species.setId(s);
			species.setCompartment(getDefaultCompartment());
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
			/*
			species.setAnnotation(GlobalConstants.TYPE + "=" + property.getProperty(GlobalConstants.TYPE)
					.replace("diffusible","").replace("constitutive",""));
					*/
			String type = property.getProperty(GlobalConstants.TYPE).replace("diffusible","").replace("constitutive","");
			Port port = sbmlCompModel.createPort();
			port.setId(type+"__"+species.getId());
			port.setIdRef(species.getId());
		} /*else {
			species.setAnnotation(GlobalConstants.TYPE + "=" + GlobalConstants.INTERNAL);
		}*/
		/*
		if (property.containsKey(GlobalConstants.SBOL_RBS) &&
				property.containsKey(GlobalConstants.SBOL_CDS)) {
			species.appendAnnotation("," + GlobalConstants.SBOL_RBS + "=" + 
				property.getProperty(GlobalConstants.SBOL_RBS) + "," + 
				GlobalConstants.SBOL_CDS + "=" + property.getProperty(GlobalConstants.SBOL_CDS));
		} else if (property.containsKey(GlobalConstants.SBOL_RBS)) {
			species.appendAnnotation("," + GlobalConstants.SBOL_RBS + "=" + 
					property.getProperty(GlobalConstants.SBOL_RBS));
		} else if (property.containsKey(GlobalConstants.SBOL_CDS)) {
			species.appendAnnotation("," + GlobalConstants.SBOL_CDS + "=" + 
					property.getProperty(GlobalConstants.SBOL_CDS));
		}
		*/
		double kd = -1;
		if (property.containsKey(GlobalConstants.KDECAY_STRING)) {
			kd = Double.parseDouble(property.getProperty(GlobalConstants.KDECAY_STRING));
		} 
		if (kd != 0) {
			createDegradationReaction(s,kd,null);
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
	
	// Note this doesn't appear to be used anywhere
	public void createPromoterFromGCM(String s,Properties property) {
		Species promoter = sbml.getModel().getSpecies(s);
		if (promoter==null) {
			promoter = sbml.getModel().createSpecies();
			promoter.setId(s);
		}
		if (property != null && property.containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
			promoter.setInitialAmount(Double.parseDouble(property.getProperty(GlobalConstants.PROMOTER_COUNT_STRING)));
		} else {
			promoter.setInitialAmount(sbml.getModel().getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue());
		} 
		promoter.setCompartment(getDefaultCompartment());
		promoter.setBoundaryCondition(false);
		promoter.setConstant(false);
		promoter.setHasOnlySubstanceUnits(true);
		promoter.setSBOTerm(GlobalConstants.SBO_PROMOTER_SPECIES);
		/*
		if (property.containsKey(GlobalConstants.SBOL_PROMOTER) &&
				property.containsKey(GlobalConstants.SBOL_TERMINATOR)) {
			promoter.appendAnnotation(GlobalConstants.SBOL_PROMOTER + "=" + 
					property.getProperty(GlobalConstants.SBOL_PROMOTER) + "," +
					GlobalConstants.SBOL_TERMINATOR + "=" + property.getProperty(GlobalConstants.SBOL_TERMINATOR));
		} else if (property.containsKey(GlobalConstants.SBOL_PROMOTER)) {
			promoter.appendAnnotation(GlobalConstants.SBOL_PROMOTER + "=" + 
					property.getProperty(GlobalConstants.SBOL_PROMOTER));
		} else if (property.containsKey(GlobalConstants.SBOL_TERMINATOR)) {
			promoter.appendAnnotation(GlobalConstants.SBOL_TERMINATOR + "=" + 
					property.getProperty(GlobalConstants.SBOL_TERMINATOR));
		}
		*/
		createProductionReaction(s,property.getProperty(GlobalConstants.ACTIVATED_STRING),
				property.getProperty(GlobalConstants.STOICHIOMETRY_STRING),
				property.getProperty(GlobalConstants.OCR_STRING),
				property.getProperty(GlobalConstants.KBASAL_STRING),
				property.getProperty(GlobalConstants.RNAP_BINDING_STRING),
				property.getProperty(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING));
	}
	
	public Layout createLayout() {
		if (sbmlLayout==null) {
			sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
			sbml.setPackageRequired("layout", false); 
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
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s)!=null) {
			speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s);
		} else {
			speciesGlyph = layout.createSpeciesGlyph();
			speciesGlyph.setId(GlobalConstants.GLYPH+"__"+s);
			speciesGlyph.setSpeciesId(s);
		}
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setX(x);
		boundingBox.setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		if (layout.getDimensions().getWidth() < x+w) {
			layout.getDimensions().setWidth(x+w);
		}
		if (layout.getDimensions().getHeight() < y+h) {
			layout.getDimensions().setHeight(y+h);
		}
		speciesGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s)!=null) {
			textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
		} else {
			textGlyph = layout.createTextGlyph();
		}
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+s);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+s);
		textGlyph.setText(s);
		textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
	}

	public void placeReaction(String s,Double x,Double y,Double h,Double w) {
		Layout layout = sbmlLayout.getLayout("iBioSim");
		ReactionGlyph reactionGlyph;
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+s)!=null) {
			reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+s);
		} else {
			reactionGlyph = layout.createReactionGlyph();
		}
		reactionGlyph.setId(GlobalConstants.GLYPH+"__"+s);
		reactionGlyph.setReactionId(s);
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setX(x);
		boundingBox.setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		if (layout.getDimensions().getWidth() < x+w) {
			layout.getDimensions().setWidth(x+w);
		}
		if (layout.getDimensions().getHeight() < y+h) {
			layout.getDimensions().setHeight(y+h);
		}
		reactionGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s)!=null) {
			textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
		} else {
			textGlyph = layout.createTextGlyph();
		}
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+s);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+s);
		textGlyph.setText(s);
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
	}

	public void placeCompartment(String s,Double x,Double y,Double h,Double w) {
		Layout layout = sbmlLayout.getLayout("iBioSim");
		CompartmentGlyph compartmentGlyph;
		if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+s)!=null) {
			compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+s);
		} else {
			compartmentGlyph = layout.createCompartmentGlyph();
		}
		compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+s);
		compartmentGlyph.setCompartmentId(s);
		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setX(x);
		boundingBox.setY(y);
		Dimensions dim = new Dimensions();
		dim.setHeight(h);
		dim.setWidth(w);
		boundingBox.setDimensions(dim);
		if (layout.getDimensions().getWidth() < x+w) {
			layout.getDimensions().setWidth(x+w);
		}
		if (layout.getDimensions().getHeight() < y+h) {
			layout.getDimensions().setHeight(y+h);
		}
		compartmentGlyph.setBoundingBox(boundingBox);
		TextGlyph textGlyph = null;
		if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s)!=null) {
			textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+s);
		} else {
			textGlyph = layout.createTextGlyph();
		}
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+s);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+s);
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
	
	public void setGridSize(int rows, int cols) {
		
		Layout layout = createLayout();
		
		if (rows > 0 && cols > 0) {
			
			XMLAttributes attr = new XMLAttributes();
			attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
			attr.add("ibiosim:grid", "(" + rows + "," + cols + ")");
			XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
			
			layout.setAnnotation(node);
		} 
		else {
			layout.unsetAnnotation();
		}
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
		extModel.setSource("file:" + prop.getProperty("gcm").replace(".gcm",".xml"));

		String submodelID = s;
		
		//if a gridded/arrayed submodel exists, it'll have this ID
		String gridSubmodelID = "GRID__" + extId;
		int row = 0;
		int col = 0;
		
		//if the submodel is not on a grid
		if (!(prop.keySet().contains("row") && prop.keySet().contains("col"))) {
			
			gridSubmodelID = submodelID + "__" + extId;
		}
		else {
			
			row = Integer.parseInt(prop.getProperty("row"));
			col = Integer.parseInt(prop.getProperty("col"));
		}
		
		Submodel potentialGridSubmodel = sbmlCompModel.getSubmodel(gridSubmodelID);
		
		if (potentialGridSubmodel != null) {
			
			//if the annotation string already exists, then one of these existed before
			//so update its count
			if (potentialGridSubmodel.getAnnotationString().length() > 0 && prop.keySet().contains("row") &&
					prop.keySet().contains("col")) {
				
				int size = Integer.parseInt(potentialGridSubmodel.getAnnotationString()
						.replace("\"","").split("=")[2].replace("/>","").replace("</annotation>","").trim());
				
				XMLAttributes attr = new XMLAttributes();			
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:count", String.valueOf(++size));
				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				potentialGridSubmodel.setAnnotation(node);
			}
			else {
				
				XMLAttributes attr = new XMLAttributes();
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:count", "1");
				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				potentialGridSubmodel.setAnnotation(node);
			}
		}
		else {
			potentialGridSubmodel = sbmlCompModel.createSubmodel();
			potentialGridSubmodel.setId(gridSubmodelID);
			
			XMLAttributes attr = new XMLAttributes();			
			attr.add("xmlns:array", "http://www.fakeuri.com");
			attr.add("array:count", "1");
			XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
			
			potentialGridSubmodel.setAnnotation(node);
		}
		
		potentialGridSubmodel.setModelRef(extId);
		
		//add an entry to the location parameter for the external model			
		String locationParameterID = extModel.getId() + "__locations";
		
		if (!(prop.keySet().contains("row") && prop.keySet().contains("col")))
			locationParameterID = gridSubmodelID + "__locations";
		
		Parameter locationParameter = sbml.getModel().getParameter(locationParameterID);
		
		if (locationParameter == null) {
			
			locationParameter = sbml.getModel().createParameter();
			locationParameter.setId(locationParameterID);
			locationParameter.setConstant(false);
		}
		
		if (locationParameter.getAnnotation() != null)
			locationParameter.getAnnotation().getChild(0).addAttr("array:" + submodelID, "(" + row + "," + col + ")");
		else {
			
			XMLAttributes attr = new XMLAttributes();
			attr.add("xmlns:array", "http://www.fakeuri.com");
			attr.add("array:" + submodelID, "(" + row + "," + col + ")");
			
			XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
			
			locationParameter.setAnnotation(node);
		}
		
		if (prop.keySet().contains("row") && prop.keySet().contains("col")
				&& prop.getProperty("compartment").equals("true"))
			createGridSpecies(gridSubmodelID);
		
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
							replacement.getPortRef().equals(GlobalConstants.OUTPUT+"__"+propName.toString())) {
							found = true;
							break;
						}
					}
					if (!found) replacement = sbmlSBase.createReplacedElement();
					replacement.setSubmodelRef(s);
					replacement.setPortRef(GlobalConstants.OUTPUT+"__"+propName.toString());
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
							replacement.getPortRef().equals(GlobalConstants.INPUT+"__"+propName.toString())) {
							found = true;
							break;
						}
					}
					if (!found) replacement = sbmlSBase.createReplacedElement();
					replacement.setSubmodelRef(s);
					replacement.setPortRef(GlobalConstants.INPUT+"__"+propName.toString());
				}
			}
		}
	}

	public void createCompPlugin() {
		if (sbmlComp==null) {
			sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
			sbml.setPackageRequired("comp", true); 
			((CompSBMLDocumentPlugin)sbml.getPlugin("comp")).setRequired(true);
			sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
			sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		}
	}

	public Reaction createComplexReaction(String s,String KcStr) {
		Reaction r = getComplexReaction(s);
		if (r==null) {
			r = sbml.getModel().createReaction();
			r.setId("Complex_"+s);
			r.setSBOTerm(GlobalConstants.SBO_COMPLEX);
			r.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			r.setReversible(true);
			r.setFast(false);
			SpeciesReference product = r.createProduct();
			product.setSpecies(s);
			product.setStoichiometry(1);
			product.setConstant(true);		
			r.createKineticLaw();
		}
		if (KcStr != null && KcStr.startsWith("(")) {
			KineticLaw k = r.getKineticLaw();
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_KCOMPLEX_STRING);
			p.setValue(1.0);
			p.setAnnotation(GlobalConstants.FORWARD_KCOMPLEX_STRING+"="+KcStr);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_KCOMPLEX_STRING);
			p.setValue(1.0);
		} else {
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
		}
		createComplexKineticLaw(r);
		return r;
	}

	public Reaction addNoInfluenceToProductionReaction(String promoterId,String activatorId,String productId) {
		Reaction r = getProductionReaction(promoterId);
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
	
	public static boolean isComplexReaction(Reaction reaction) {
		if (reaction.isSetSBOTerm()) {
			if (reaction.getSBOTerm()==GlobalConstants.SBO_COMPLEX) return true;
		} else if (reaction.isSetAnnotation()) {
			if (reaction.getAnnotationString().contains("Complex")) {
				reaction.setSBOTerm(GlobalConstants.SBO_COMPLEX);
				reaction.unsetAnnotation();
				return true;
			}
		}
		return false;
	}
	
	public static boolean isConstitutiveReaction(Reaction reaction) {
		if (reaction.isSetSBOTerm()) {
			if (reaction.getSBOTerm()==GlobalConstants.SBO_CONSTITUTIVE) return true;
		} else if (reaction.isSetAnnotation()) {
			if (reaction.getAnnotationString().contains("Constitutive")) {
				reaction.setSBOTerm(GlobalConstants.SBO_CONSTITUTIVE);
				reaction.unsetAnnotation();
				return true;
			}
		}
		return false;
	}
	
	public static boolean isDegradationReaction(Reaction reaction) {
		if (reaction.isSetSBOTerm()) {
			if (reaction.getSBOTerm()==GlobalConstants.SBO_DEGRADATION) return true;
		} else if (reaction.isSetAnnotation()) {
			if (reaction.getAnnotationString().contains("Degradation")) {
				reaction.setSBOTerm(GlobalConstants.SBO_DEGRADATION);
				reaction.unsetAnnotation();
				return true;
			}
		}
		return false;
	}
	
	public static boolean isDiffusionReaction(Reaction reaction) {
		if (reaction.isSetSBOTerm()) {
			if (reaction.getSBOTerm()==GlobalConstants.SBO_DIFFUSION) return true;
		} else if (reaction.isSetAnnotation()) {
			if (reaction.getAnnotationString().contains("Diffusion")) {
				reaction.setSBOTerm(GlobalConstants.SBO_DIFFUSION);
				reaction.unsetAnnotation();
				return true;
			}
		}
		return false;
	}
	
	public static boolean isProductionReaction(Reaction reaction) {
		if (reaction.isSetSBOTerm()) {
			if (reaction.getSBOTerm()==GlobalConstants.SBO_PRODUCTION) return true;
		} else if (reaction.isSetAnnotation()) {
			if (reaction.getAnnotationString().contains("Production")) {
				reaction.setSBOTerm(GlobalConstants.SBO_PRODUCTION);
				reaction.unsetAnnotation();
				return true;
			}
		}
		return false;
	}
	
	public static boolean isMRNASpecies(Species species) {
		if (species.isSetAnnotation()) {
			if (species.getAnnotationString().contains(GlobalConstants.TYPE+"="+GlobalConstants.MRNA)) {
				species.setSBOTerm(GlobalConstants.SBO_MRNA);
				species.unsetAnnotation();
				return true;
			}
		}
		if (species.isSetSBOTerm()) {
			if (species.getSBOTerm()==GlobalConstants.SBO_MRNA || species.getSBOTerm()==GlobalConstants.SBO_MRNA_OLD) {
				species.setSBOTerm(GlobalConstants.SBO_MRNA);
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPromoterSpecies(Species species) {
		if (species.isSetAnnotation()) {
			if (species.getAnnotationString().contains(GlobalConstants.TYPE+"="+GlobalConstants.PROMOTER)) {
				species.setSBOTerm(GlobalConstants.SBO_PROMOTER_SPECIES);
				species.unsetAnnotation();
				return true;
			}
		}
		if (species.isSetSBOTerm()) {
			if (species.getSBOTerm()==GlobalConstants.SBO_OLD_PROMOTER_SPECIES) {
				species.setSBOTerm(GlobalConstants.SBO_PROMOTER_SPECIES);
				return true;
			}
			if (species.getSBOTerm()==GlobalConstants.SBO_PROMOTER_SPECIES) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPromoter(ModifierSpeciesReference modifier) {
		if (modifier.isSetAnnotation()) {
			if (modifier.getAnnotationString().contains("promoter")) {
				modifier.setSBOTerm(GlobalConstants.SBO_PROMOTER);
				modifier.unsetAnnotation();
				return true;
			}
		}
		if (modifier.isSetSBOTerm()) {
			if ((modifier.getSBOTerm()!=GlobalConstants.SBO_ACTIVATION) &&
				(modifier.getSBOTerm()!=GlobalConstants.SBO_REPRESSION) &&
				(modifier.getSBOTerm()!=GlobalConstants.SBO_REGULATION)) { 
				return true;
			}
		}
		return false;
	}
	
	public static boolean isActivator(ModifierSpeciesReference modifier) {
		if (modifier.isSetAnnotation()) {
			if (modifier.getAnnotationString().contains(GlobalConstants.ACTIVATION)) {
				modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
				modifier.unsetAnnotation();
				return true;
			}
		}
		if (modifier.isSetSBOTerm()) {
			if (modifier.getSBOTerm()==GlobalConstants.SBO_ACTIVATION) return true;
		}
		return false;
	}
	
	public static boolean isRepressor(ModifierSpeciesReference modifier) {
		if (modifier.isSetAnnotation()) {
			if (modifier.getAnnotationString().contains(GlobalConstants.REPRESSION)) {
				modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
				modifier.unsetAnnotation();
				return true;
			}
		}
		if (modifier.isSetSBOTerm()) {
			if (modifier.getSBOTerm()==GlobalConstants.SBO_REPRESSION) return true;
		}
		return false;
	}
	
	public static boolean isRegulator(ModifierSpeciesReference modifier) {
		if (modifier!=null) {
			if (modifier.isSetAnnotation()) {
				if (modifier.getAnnotationString().contains(GlobalConstants.REGULATION)) {
					modifier.setSBOTerm(GlobalConstants.SBO_REGULATION);
					modifier.unsetAnnotation();
					return true;
				} else if (modifier.getAnnotationString().contains("promoter")) {
					modifier.unsetAnnotation();
					return false;
				}
			}
			if (modifier.isSetSBOTerm()) {
				if (modifier.getSBOTerm()==GlobalConstants.SBO_REGULATION) return true;
			}
		}
		return false;
	}
	
	public Reaction addActivatorToProductionReaction(String promoterId,String activatorId,String productId,String npStr,
			String ncStr,String KaStr) {
		Reaction r = getProductionReaction(promoterId);
		ModifierSpeciesReference modifier = r.getModifier(activatorId);
		if (!activatorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(activatorId);
			modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
		} else if (modifier != null && isRepressor(modifier)) {
			modifier.setSBOTerm(GlobalConstants.SBO_REGULATION);
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
		Reaction r = getProductionReaction(promoterId);
		ModifierSpeciesReference modifier = r.getModifier(repressorId);
		if (!repressorId.equals("none") && modifier==null) {
			modifier = r.createModifier();
			modifier.setSpecies(repressorId);
			modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
		} else if (modifier != null && isActivator(modifier)) {
			modifier.setSBOTerm(GlobalConstants.SBO_REGULATION);
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
		SpeciesReference reactant = r.getReactant(reactantId);
		if (reactant==null) {
			reactant = r.createReactant();
			reactant.setSpecies(reactantId);
			reactant.setConstant(true);
		}
		KineticLaw k = r.getKineticLaw();
		LocalParameter p = k.getLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId);
		if (CoopStr != null) {
			if (p==null) {
				p = k.createLocalParameter();
				p.setId(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId);
			} 
			double nc = Double.parseDouble(CoopStr);
			p.setValue(nc);
			reactant.setStoichiometry(nc);
		} else {
			if (p != null) {
				k.removeLocalParameter(GlobalConstants.COOPERATIVITY_STRING+"_"+reactantId);
			}
			Parameter gp = sbml.getModel().getParameter(GlobalConstants.COOPERATIVITY_STRING);
			reactant.setStoichiometry(gp.getValue());
		}
		createComplexKineticLaw(r);
		return r;
	}
	
	public Reaction createDiffusionReaction(String s,String kmdiffStr) {
		Reaction reaction = sbml.getModel().getReaction("MembraneDiffusion_"+s);		
		if (reaction==null) {
			reaction = sbml.getModel().createReaction();
			reaction.setId("MembraneDiffusion_"+s);
			reaction.setSBOTerm(GlobalConstants.SBO_DIFFUSION);
			reaction.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			reaction.setReversible(true);
			reaction.setFast(false);
			SpeciesReference reactant = reaction.createReactant();
			reactant.setSpecies(s);
			reactant.setStoichiometry(1);
			reactant.setConstant(true);
		}
		KineticLaw k = reaction.createKineticLaw();
		if (kmdiffStr != null && kmdiffStr.startsWith("(")) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.FORWARD_MEMDIFF_STRING);
			p.setValue(1.0);
			p.setAnnotation(GlobalConstants.FORWARD_MEMDIFF_STRING+"="+kmdiffStr);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_MEMDIFF_STRING);
			p.setValue(1.0);
		} else {
			double [] kmdiff = Utility.getEquilibrium(kmdiffStr);
			if (kmdiff[0] >= 0) {
				LocalParameter p = k.createLocalParameter();
				p.setId(GlobalConstants.FORWARD_MEMDIFF_STRING);
				p.setValue(kmdiff[0]);
				p = k.createLocalParameter();
				p.setId(GlobalConstants.REVERSE_MEMDIFF_STRING);
				p.setValue(kmdiff[1]);
			}
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
			reaction.setSBOTerm(GlobalConstants.SBO_CONSTITUTIVE);
			reaction.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
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
	
	public Reaction createDegradationReaction(String s,double kd,String sweep) {
		Reaction reaction = sbml.getModel().getReaction("Degradation_"+s);
		if (reaction==null) {
			reaction = sbml.getModel().createReaction();
			reaction.setId("Degradation_"+s);
			reaction.setSBOTerm(GlobalConstants.SBO_DEGRADATION);
			reaction.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			reaction.setReversible(false);
			reaction.setFast(false);
			SpeciesReference reactant = reaction.createReactant();
			reactant.setSpecies(s);
			reactant.setStoichiometry(1);
			reactant.setConstant(true);
		} 
		KineticLaw k = reaction.createKineticLaw();
		if (kd > 0 || sweep != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId("kd");
			p.setValue(kd);
			if (sweep != null) {
				p.setAnnotation(GlobalConstants.KDECAY_STRING+"="+sweep);
			} 
		}
		k.setMath(SBMLutilities.myParseFormula("kd*"+s));

		return reaction;
	}

	public Reaction createProductionReaction(String s,String ka,String np,String ko,String kb, String KoStr, String KaoStr) {
		Reaction r = getProductionReaction(s);
		KineticLaw k = null;
		if (r == null) {
			r = sbml.getModel().createReaction();
			r.setId("Production_" + s);
			r.setSBOTerm(GlobalConstants.SBO_PRODUCTION);
			r.setCompartment(sbml.getModel().getSpecies(s).getCompartment());
			r.setReversible(false);
			r.setFast(false);
			ModifierSpeciesReference modifier = r.createModifier();
			modifier.setSpecies(s);
			modifier.setSBOTerm(GlobalConstants.SBO_PROMOTER);
			Species mRNA = sbml.getModel().createSpecies();
			mRNA.setId(s+"_mRNA");
			mRNA.setCompartment(r.getCompartment());
			mRNA.setInitialAmount(0.0);
			mRNA.setBoundaryCondition(false);
			mRNA.setConstant(false);
			mRNA.setHasOnlySubstanceUnits(true);
			mRNA.setSBOTerm(GlobalConstants.SBO_MRNA);
			SpeciesReference product = r.createProduct();
			product.setSpecies(mRNA.getId());
			product.setStoichiometry(1.0);
			product.setConstant(true);
		} 
		k = r.createKineticLaw();
		if (np != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.STOICHIOMETRY_STRING);
			double npVal = 1.0;
			if (np.startsWith("(")) {
				p.setAnnotation(GlobalConstants.STOICHIOMETRY_STRING+"="+np);
			} else {
				npVal = Double.parseDouble(np);
			}
			p.setValue(npVal);
			for (long i = 0; i < r.getNumProducts(); i++) {
				r.getProduct(i).setStoichiometry(npVal);
			}
		} else {
			for (long i = 0; i < r.getNumProducts(); i++) {
				r.getProduct(i).setStoichiometry(sbml.getModel().getParameter(GlobalConstants.STOICHIOMETRY_STRING).getValue());
			}
		}
		if (ko != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.OCR_STRING);
			if (ko.startsWith("(")) {
				p.setAnnotation(GlobalConstants.OCR_STRING+"="+ko);
				p.setValue(1.0);
			} else {
				p.setValue(Double.parseDouble(ko));
			}
		} 							
		if (kb != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.KBASAL_STRING);
			if (kb.startsWith("(")) {
				p.setAnnotation(GlobalConstants.KBASAL_STRING+"="+kb);
				p.setValue(1.0);
			} else {
				p.setValue(Double.parseDouble(kb));
			}
		} 
		if (ka != null) {
			LocalParameter p = k.createLocalParameter();
			p.setId(GlobalConstants.ACTIVATED_STRING);
			if (ka.startsWith("(")) {
				p.setAnnotation(GlobalConstants.ACTIVATED_STRING+"="+ka);
				p.setValue(1.0);
			} else {
				p.setValue(Double.parseDouble(ka));
			}
		} 		
		if (KoStr != null) {
			double [] Ko;
			LocalParameter p = k.createLocalParameter();
			if (KoStr.startsWith("(")) {
				p.setAnnotation(GlobalConstants.FORWARD_RNAP_BINDING_STRING+"="+KoStr);
				p.setValue(1.0);
				Ko = Utility.getEquilibrium("1.0/1.0");
			} else {
				Ko = Utility.getEquilibrium(KoStr);
			}
			p.setId(GlobalConstants.FORWARD_RNAP_BINDING_STRING);
			p.setValue(Ko[0]);
			p = k.createLocalParameter();
			p.setId(GlobalConstants.REVERSE_RNAP_BINDING_STRING);
			p.setValue(Ko[1]);
		} 							
		if (KaoStr != null) {
			double [] Kao;
			LocalParameter p = k.createLocalParameter();
			if (KaoStr.startsWith("(")) {
				p.setAnnotation(GlobalConstants.FORWARD_ACTIVATED_RNAP_BINDING_STRING+"="+KaoStr);
				p.setValue(1.0);
				Kao = Utility.getEquilibrium("1.0/1.0");
			} else {
				Kao = Utility.getEquilibrium(KoStr);
			}
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
			if (isActivator(reaction.getModifier(i))||isRegulator(reaction.getModifier(i))) {
				activated = true;
			} else if (isPromoter(reaction.getModifier(i))) {
				promoter = reaction.getModifier(i).getSpecies();
			}
		}
		if (activated) {
			kineticLaw = promoter + "*(" + GlobalConstants.KBASAL_STRING + "*" +
					"(" + GlobalConstants.FORWARD_RNAP_BINDING_STRING + "/" + GlobalConstants.REVERSE_RNAP_BINDING_STRING + ")*" 
					+ GlobalConstants.RNAP_STRING;
			String actBottom = "";
			for (int i=0;i<reaction.getNumModifiers();i++) {
				if (isActivator(reaction.getModifier(i)) || isRegulator(reaction.getModifier(i))) {
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
				if (isRepressor(reaction.getModifier(i)) || isRegulator(reaction.getModifier(i))) {
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
				if (isRepressor(reaction.getModifier(i))) {
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
		reaction.getKineticLaw().setMath(SBMLutilities.myParseFormula(kineticLaw));
 	}
	
	/**
	 * Save the current object to file.
	 * 
	 * @param filename
	 */
	public void save(String filename) {
		//gcm2sbml.convertGCM2SBML(filename);
		//updateCompartmentReplacements();
		setGridSize(grid.getNumRows(),grid.getNumCols());
		SBMLutilities.pruneUnusedSpecialFunctions(sbml);
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

	public void changePromoterId(String oldId, String newId) {
		if (sbml != null) {
			if (sbml.getModel() != null) {
				SBMLutilities.updateVarId(sbml, true, oldId, newId);
				Species promoter = sbml.getModel().getSpecies(oldId); 
				if (promoter != null) 
					promoter.setId(newId);
			}
		}
		Reaction reaction = getProductionReaction(oldId);
		if (newId.contains("__")) {
			reaction.setId("Production_"+newId.substring(newId.lastIndexOf("__")+2));
		} else {
			reaction.setId("Production_"+newId);
		}
		SpeciesReference product = reaction.getProduct(oldId+"_mRNA");
		if (product!=null) {
			sbml.getModel().getSpecies(oldId+"_mRNA").setId(newId+"_mRNA");
			product.setSpecies(newId+"_mRNA");
		}

		if (sbmlLayout.getNumLayouts() != 0) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId)!=null) {
				SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId);
				speciesGlyph.setId(GlobalConstants.GLYPH+"__"+newId);
				speciesGlyph.setSpeciesId(newId);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId);
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+newId);
				textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+newId);
				textGlyph.setText(newId);
			}
		}
	}

	public void changeSpeciesId(String oldId, String newId) {
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
				SBMLutilities.updateVarId(sbml, true, oldId, newId);
				if (sbml.getModel().getSpecies(oldId) != null) {
					sbml.getModel().getSpecies(oldId).setId(newId);
				}
			}
		}
		String baseId = newId;
		if (newId.contains("__")) {
			baseId = newId.substring(newId.lastIndexOf("__")+2);
		}
		if (isSpeciesConstitutive(oldId)) {
			Reaction reaction = sbml.getModel().getReaction("Constitutive_"+oldId);
			reaction.setId("Constitutive_"+baseId);
		}
		Reaction diffusion = getDiffusionReaction(oldId);
		if (diffusion != null) {
			diffusion.setId("MembraneDiffusion_"+baseId);
		}
		Reaction degradation = getDegradationReaction(oldId);
		if (degradation != null) {
			degradation.setId("Degradation_"+baseId);
		}
		if (isSpeciesComplex(oldId)) {
			Reaction reaction = getComplexReaction(oldId);
			reaction.setId("Complex_"+baseId);
		}
		for (int i=0;i<sbml.getModel().getNumReactions();i++) {
			Reaction reaction = sbml.getModel().getReaction(i);
			if (isComplexReaction(reaction)) {
				KineticLaw k = reaction.getKineticLaw();
				for (int j=0;j<k.getNumLocalParameters();j++) {
					LocalParameter param = k.getLocalParameter(j);
					if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldId)) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newId);
					}
				}
				createComplexKineticLaw(reaction);
			} else if (isProductionReaction(reaction)) {
				KineticLaw k = reaction.getKineticLaw();
				for (int j=0;j<k.getNumLocalParameters();j++) {
					LocalParameter param = k.getLocalParameter(j);
					if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_r")) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_r");
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_r",
								GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_r"));
					} else if (param.getId().equals(GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_a")) {
						param.setId(GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_a");
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.COOPERATIVITY_STRING + "_" + oldId + "_a",
								GlobalConstants.COOPERATIVITY_STRING + "_" + newId + "_a"));
					} else if (param.getId().equals(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.FORWARD_KREP_STRING.replace("_","_" + newId + "_")));
					} else if (param.getId().equals(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.REVERSE_KREP_STRING.replace("_","_" + newId + "_")));
					} else if (param.getId().equals(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.FORWARD_KACT_STRING.replace("_","_" + newId + "_")));
					} else if (param.getId().equals(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + oldId + "_"))) {
						param.setId(GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + newId + "_"));
						k.setMath(SBMLutilities.updateMathVar(k.getMath(), 
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + oldId + "_"),
								GlobalConstants.REVERSE_KACT_STRING.replace("_","_" + newId + "_")));
					}
				}
			}
		}
		if (sbmlLayout.getNumLayouts() != 0) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId)!=null) {
				SpeciesGlyph speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+oldId);
				speciesGlyph.setId(GlobalConstants.GLYPH+"__"+newId);
				speciesGlyph.setSpeciesId(newId);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldId);
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+newId);
				textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+newId);
				textGlyph.setText(newId);
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
		
		String submodelID = oldName;
		
		//look through locations parameters for find the submodel
		for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
			
			Parameter param = sbml.getModel().getParameter(i);
			
			if (param.getId().contains("__locations"))
				if (param.getAnnotationString().contains("array:" + oldName + "="))
					submodelID = "GRID__" + param.getId().replace("__locations", "");
		}
		
		Submodel subModel = sbmlCompModel.getSubmodel(submodelID);
		
		if (subModel == null) {
			submodelID = submodelID.replace("GRID__", "");
			subModel = sbmlCompModel.getSubmodel(submodelID);
		}
		
		if (sbml.getModel().getParameter(subModel.getId().replace("GRID__","") + "__locations") != null) {
			
			Parameter param = sbml.getModel().getParameter(subModel.getId().replace("GRID__","") + "__locations");
			
			String value = param.getAnnotation().getChild(0).getAttrValue("array:" + oldName);			
			param.getAnnotation().getChild(0).removeAttr("array:" + oldName);
			param.getAnnotation().getChild(0).addAttr("array:" + newName, value);
			
			if (param.getId().contains(oldName))
				param.setId(param.getId().replace(oldName, newName));
		}
		else
			subModel.setId(newName);
		
		subModel.setId(submodelID.replace(oldName, newName));
		
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
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+oldName)!=null) {
				CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+oldName);
				compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+newName);
				compartmentGlyph.setCompartmentId(newName);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldName)!=null) {
				TextGlyph textGlyph = layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+oldName);
				textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+newName);
				textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+newName);
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
			if (isDegradationReaction(r)) continue;
			if (isDiffusionReaction(r)) continue;
			if (isProductionReaction(r)) continue;
			if (isComplexReaction(r)) continue;
			if (isConstitutiveReaction(r)) continue;
			if (r.getAnnotationString().contains("grid")) continue;
			
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
			while (sbml.getElementBySId(reactionId)!=null) {
				i++;
				reactionId = "r" + i;
			}
			r.setId(reactionId);
			r.setReversible(false);
			r.setFast(false);
			ArrayList<String> CompChoices = new ArrayList<String>();
			if (isModifier) { 
				ModifierSpeciesReference source = r.createModifier();
				source.setSpecies(sourceID);
				CompChoices.add(sbml.getModel().getSpecies(sourceID).getCompartment());
				r.setCompartment(sbml.getModel().getSpecies(sourceID).getCompartment());
			} else {
				SpeciesReference source = r.createReactant();
				source.setSpecies(sourceID);
				source.setConstant(true);
				source.setStoichiometry(1.0);
				CompChoices.add(sbml.getModel().getSpecies(sourceID).getCompartment());
				r.setCompartment(sbml.getModel().getSpecies(sourceID).getCompartment());
			}
			SpeciesReference target = r.createProduct();
			target.setSpecies(targetID);
			target.setConstant(true);
			target.setStoichiometry(1.0);
			if (!r.getCompartment().equals(sbml.getModel().getSpecies(targetID).getCompartment())) {
				CompChoices.add(sbml.getModel().getSpecies(targetID).getCompartment());
				JComboBox compartmentList = new JComboBox(CompChoices.toArray());
				JPanel compartmentListPanel = new JPanel(new GridLayout(1, 1));
				compartmentListPanel.add(compartmentList);
				JOptionPane.showOptionDialog(Gui.frame, compartmentListPanel, "Compartment Choice",
						JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				r.setCompartment((String)compartmentList.getSelectedItem());
			}
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
	public String addComponent(String submodelID, String modelFile, 
			boolean enclosed, ArrayList<String> compartmentPorts, int row, int col, double x, double y) {
		
		ExternalModelDefinition extModel = null;
		String extId = modelFile.replace(".gcm","").replace(".xml","");
		
		if (sbmlComp.getExternalModelDefinition(extId) == null)
			extModel = sbmlComp.createExternalModelDefinition();
		else
			extModel = sbmlComp.getExternalModelDefinition(extId);
		
		extModel.setId(extId);
		extModel.setSource("file:" + modelFile.replace(".gcm",".xml"));
		
//		if (enclosed && extModel.getAnnotationString().contains("compartment") == false) {	
//			
//			XMLAttributes attr = new XMLAttributes();			
//			attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
//			attr.add("ibiosim:type", "compartment");
//			XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
//			
//			extModel.setAnnotation(node);
//		}
//		else
//			extModel.getAnnotation().getChild(0).removeAttr("ibiosim:type");
		
		//figure out what the submodel's ID should be if it's not provided
		if (submodelID == null) {
			
			int count = 1;
			submodelID = "C" + count;
			
			for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
				
				boolean escape = false;
				
				while (sbml.getModel().getParameter(i).getId().contains(extId + "__locations") == true) {
					
					++count;
					submodelID = "C" + count;
					
					if (sbml.getModel().getParameter(extId + "__locations") != null &&
							sbml.getModel().getParameter(extId + "__locations")
							.getAnnotationString().contains("array:" + submodelID + "=") == false) {
						
						escape = true;
						break;
					}
					else if (sbml.getModel().getParameter(extId + "__locations") == null &&
							sbml.getModel().getParameter(submodelID + "__"+ extId + "__locations") == null) {
						escape = true;
						break;
					}
				}
				
				if (escape == true)
					break;
			}
			
			while (this.getSBMLCompModel().getSubmodel(submodelID) != null) {
				
				++count;
				submodelID = "C" + count;
			}			
		}
		
		Compartment compartment = sbml.getModel().getCompartment(getDefaultCompartment());
		if (!isGridEnabled()) {
			sbml.getModel().getCompartment(getCompartmentByLocation((float)x,(float)y,(float)GlobalConstants.DEFAULT_COMPONENT_WIDTH,
					(float)GlobalConstants.DEFAULT_COMPONENT_HEIGHT));
		}
		if (compartment!=null) {
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
			for (String compartmentPort : compartmentPorts) {
				ReplacedElement replacement = sbmlSBase.createReplacedElement();
				replacement.setSubmodelRef(submodelID);
				replacement.setPortRef(GlobalConstants.COMPARTMENT+"__"+compartmentPort);
			}
		}
			
		int numRows = grid.getNumRows();
		int numCols = grid.getNumCols();
		
		//if a gridded/arrayed submodel exists, it'll have this ID
		String gridSubmodelID = "GRID__" + extId;
		
		Submodel potentialGridSubmodel = sbmlCompModel.getSubmodel(gridSubmodelID);
		
		if (potentialGridSubmodel != null) {
				
			//if the annotation string already exists, then one of these existed before
			//so update its count
			if (potentialGridSubmodel.getAnnotationString().length() > 0 && !(numRows == 0 && numCols == 0)) {
				
				int size = Integer.parseInt(potentialGridSubmodel.getAnnotationString()
						.replace("\"","").split("=")[2].replace("/>","").replace("</annotation>","").trim());
				
				XMLAttributes attr = new XMLAttributes();			
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:count", String.valueOf(++size));
				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				potentialGridSubmodel.setAnnotation(node);
			}
			else {
				
				XMLAttributes attr = new XMLAttributes();
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:count", "1");
				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				potentialGridSubmodel.setAnnotation(node);
			}
			
			potentialGridSubmodel.setModelRef(extId);
		}
		else {
			
			if (!(numRows == 0 && numCols == 0)) {
			
				potentialGridSubmodel = sbmlCompModel.createSubmodel();
				potentialGridSubmodel.setId(gridSubmodelID);
				
				XMLAttributes attr = new XMLAttributes();			
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:count", "1");
				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				potentialGridSubmodel.setAnnotation(node);				
				potentialGridSubmodel.setModelRef(extId);
			}
			else {
				
				Submodel submodel = null;
				
				if (sbmlCompModel.getSubmodel(submodelID) == null)
					submodel = sbmlCompModel.createSubmodel();
				else
					submodel = sbmlCompModel.getSubmodel(submodelID);
				
				submodel.setId(submodelID);
				submodel.setModelRef(extId);				
			}
		}
		
		if (!(numRows == 0 && numCols == 0)) {
			
			//add an entry to the location parameter for the external model			
			String locationParameterID = extModel.getId() + "__locations";
			
			Parameter locationParameter = sbml.getModel().getParameter(locationParameterID);
			
			if (locationParameter == null) {
				
				locationParameter = sbml.getModel().createParameter();
				locationParameter.setId(locationParameterID);
				locationParameter.setConstant(false);
			}
			
			if (locationParameter.getAnnotation() != null)
				locationParameter.getAnnotation().getChild(0).addAttr("array:" + submodelID, "(" + row + "," + col + ")");
			else {
				
				XMLAttributes attr = new XMLAttributes();
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:" + submodelID, "(" + row + "," + col + ")");
				
				XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				locationParameter.setAnnotation(node);
			}
			
			if (enclosed)
				createGridSpecies(gridSubmodelID);
		}
		
		//set layout information
		
		Layout layout = null;
		
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		
		CompartmentGlyph compartmentGlyph = null;
		
		if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+submodelID)!=null) {
			compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+submodelID);
		} else {
			compartmentGlyph = layout.createCompartmentGlyph();
			compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+submodelID);
			compartmentGlyph.setCompartmentId(submodelID);
		}
		
		compartmentGlyph.getBoundingBox().setX(x);
		compartmentGlyph.getBoundingBox().setY(y);
		compartmentGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_COMPONENT_WIDTH);
		compartmentGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_COMPONENT_HEIGHT);
		
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+submodelID);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+submodelID);
		textGlyph.setText(submodelID);
		textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox());
		
		return submodelID;
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
		while (hash.containsKey(name) || sbml.getElementBySId(name)!=null);

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
			if (getDiffusionReaction(id)!=null) {
				removeReaction("MembraneDiffusion_"+id);
			}
			if (getDegradationReaction(id)!=null) {
				removeReaction("Degradation_"+id);
			}
			if (sbmlLayout.getLayout("iBioSim") != null) {
				Layout layout = sbmlLayout.getLayout("iBioSim"); 
				if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
					layout.removeSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
				}
				if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
					layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
				}
			}
			for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
				Port port = sbmlCompModel.getPort(i);
				if (port.isSetIdRef() && port.getIdRef().equals(id)) {
					port.removeFromParentAndDelete();
				}
			}
			speciesPanel.refreshSpeciesPanel(this);
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
		for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
			Port port = sbmlCompModel.getPort(i);
			if (port.isSetIdRef() && port.getIdRef().equals(id)) {
				sbmlCompModel.removePort(i);
				break;
			}
		}
		if (sbmlLayout.getLayout("iBioSim") != null) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+id);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
				layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
			}
		}
	}
	
	public void removeByMetaId(String metaId) {
		SBase sbase = sbml.getModel().getElementByMetaId(metaId);
		if (sbase != null) {
			sbase.removeFromParentAndDelete();
			for (long j = 0; j < sbmlCompModel.getNumPorts(); j++) {
				Port port = sbmlCompModel.getPort(j);
				if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(metaId)) {
					sbmlCompModel.removePort(j);
					break;
				}
			}
		}
		if (sbmlLayout.getLayout("iBioSim") != null) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+metaId)!=null) {
				layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+metaId);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+metaId) != null) {
				layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+metaId);
			}
		}
	}
	
	public void removeById(String id) {
		SBase sbase = sbml.getModel().getElementBySId(id);
		if (sbase != null) {
			sbase.removeFromParentAndDelete();
			for (long j = 0; j < sbmlCompModel.getNumPorts(); j++) {
				Port port = sbmlCompModel.getPort(j);
				if (port.isSetIdRef() && port.getIdRef().equals(id)) {
					sbmlCompModel.removePort(j);
					break;
				}
			}
		}
		if (sbmlLayout.getLayout("iBioSim") != null) {
			Layout layout = sbmlLayout.getLayout("iBioSim"); 
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				layout.removeCompartmentGlyph(GlobalConstants.GLYPH+"__"+id);
			}
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				layout.removeSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
			}
			if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				layout.removeReactionGlyph(GlobalConstants.GLYPH+"__"+id);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
				layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
			}
		}
	}

	public boolean isSIdInUse(String id) {
		return (sbml.getElementBySId(id)!=null);
	}

	public ArrayList<String> getCompartments() {
		ArrayList<String> compartmentSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getNumCompartments(); i++) {
				Compartment compartment = sbml.getModel().getCompartment(i);
				compartmentSet.add(compartment.getId());
			}
		}
		return compartmentSet;
	}

	// TODO: remove special functions
	public ArrayList<String> getFunctions() {
		ArrayList<String> functionSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getNumFunctionDefinitions(); i++) {
				FunctionDefinition function = sbml.getModel().getFunctionDefinition(i);
				functionSet.add(function.getId());
			}
		}
		return functionSet;
	}

	public ArrayList<String> getUnits() {
		ArrayList<String> unitSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getNumUnitDefinitions(); i++) {
				UnitDefinition unit = sbml.getModel().getUnitDefinition(i);
				unitSet.add(unit.getId());
			}
		}
		return unitSet;
	}

	// TODO: remove special reactions
	public ArrayList<String> getReactions() {
		ArrayList<String> reactionSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getNumReactions(); i++) {
				Reaction reaction = sbml.getModel().getReaction(i);
				reactionSet.add(reaction.getId());
			}
		}
		return reactionSet;
	}

	public ArrayList<String> getParameters() {
		ArrayList<String> parameterSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getNumParameters(); i++) {
				Parameter parameter = sbml.getModel().getParameter(i);
				parameterSet.add(parameter.getId());
			}
		}
		return parameterSet;
	}

	public ArrayList<String> getSpecies() {
		ArrayList<String> speciesSet = new ArrayList<String>();
		if (sbml!=null) {
			for (int i = 0; i < sbml.getModel().getNumSpecies(); i++) {
				Species species = sbml.getModel().getSpecies(i);
				if (!isPromoterSpecies(species)) {
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
			if (isPromoterSpecies(species)) {
				promoterSet.add(species.getId());
			}
		}
		return promoterSet;
	}

	/*
	public String getSpeciesType(Species species) {
		String type = GlobalConstants.INTERNAL;
		if (species.isSetAnnotation()) {
			String annotation = species.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
			String [] annotations = annotation.split(",");
			for (int i=0;i<annotations.length;i++) {
				if (annotations[i].startsWith(GlobalConstants.TYPE)) {
					String [] splits = annotations[i].split("=");
					type = splits[1];
					break;
				}
			}
		}
		return type;
	}
	*/
	
	public ArrayList<String> getCompartmentPorts() {
		ArrayList<String> compartments = new ArrayList<String>();
		for (long i = 0; i < sbml.getModel().getNumCompartments(); i++) {
			Compartment compartment = sbml.getModel().getCompartment(i);
			if (sbmlCompModel.getPort(GlobalConstants.COMPARTMENT+"__"+compartment.getId())!=null) {
				compartments.add(compartment.getId());
			}
		}
		return compartments;
	}
	
	public Port getPortByIdRef(String idRef) {
		for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
			Port port = sbmlCompModel.getPort(i);
			if (port.isSetIdRef() && port.getIdRef().equals(idRef)) {
				return port;
			}
		}
		return null;
	}
	
	public Port getPortByMetaIdRef(String metaIdRef) {
		for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
			Port port = sbmlCompModel.getPort(i);
			if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(metaIdRef)) {
				return port;
			}
		}
		return null;
	}
	
	public Port getPortByUnitRef(String unitIdRef) {
		for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
			Port port = sbmlCompModel.getPort(i);
			if (port.isSetUnitRef() && port.getUnitRef().equals(unitIdRef)) {
				return port;
			}
		}
		return null;
	}
	
	public Port getPortBySBaseRef(SBaseRef sbaseRef) {
		for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
			Port port = sbmlCompModel.getPort(i);
			if (port.isSetIdRef() && port.getIdRef().equals(sbaseRef.getIdRef()) &&
				((!port.isSetSBaseRef() && !sbaseRef.isSetSBaseRef()) ||
				(port.getSBaseRef().isSetPortRef() && sbaseRef.getSBaseRef().isSetPortRef() &&
					port.getSBaseRef().getPortRef().equals(sbaseRef.getSBaseRef().getPortRef())))) {
				return port;
			}
		}
		return null;
	}
	
	public static Port getPortByIdRef(CompModelPlugin sbmlCompModel, String idRef) {
		for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
			Port port = sbmlCompModel.getPort(i);
			if (port.isSetIdRef() && port.getIdRef().equals(idRef)) {
				return port;
			}
		}
		return null;
	}
	
	public ArrayList<String> getPorts() {
		ArrayList<String> ports = new ArrayList<String>();
		for (long i = 0; i < sbmlCompModel.getNumPorts(); i++) {
			Port port = sbmlCompModel.getPort(i);
			String id = port.getId();
			if (port.isSetIdRef()) {
				String idRef = port.getIdRef();
				SBase sbase = sbml.getElementBySId(idRef);
				if (sbase!=null) {
					String type = sbml.getElementBySId(idRef).getElementName();
					ports.add(type + ":" + id + ":" + idRef);
				}
			} else if (port.isSetMetaIdRef()) {
				String idRef = port.getMetaIdRef();
				SBase sbase = sbml.getElementByMetaId(idRef);
				if (sbase!=null) {
					String type = sbml.getElementByMetaId(idRef).getElementName();
					ports.add(type + ":" + id + ":" + idRef);
				}
			} else if (port.isSetUnitRef()) {
				String idRef = port.getUnitRef();
				SBase sbase = sbml.getModel().getUnitDefinition(idRef);
				if (sbase!=null) {
					String type = sbml.getModel().getUnitDefinition(idRef).getElementName();
					ports.add(type + ":" + id + ":" + idRef);
				}
			}
		}
		Collections.sort(ports);
		return ports;
	}
	
	public ArrayList<String> getInputSpecies() {
		ArrayList<String> inputs = new ArrayList<String>();
		for (String spec : getSpecies()) {
			if (getSpeciesType(sbml,spec).equals(GlobalConstants.INPUT)) {
				inputs.add(spec);
			}
		}
		return inputs;
	}

	public ArrayList<String> getOutputSpecies() {
		ArrayList<String> outputs = new ArrayList<String>();
		for (String spec : getSpecies()) {
			if (getSpeciesType(sbml,spec).equals(GlobalConstants.OUTPUT)) {
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
		
		String componentModelRef = "";
		
		if (sbmlCompModel.getSubmodel(id) != null) {
			
			componentModelRef = sbmlCompModel.getSubmodel(id).getModelRef();
		}
		else {
			
			//search through the parameter location arrays to find the correct one
			for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter
				if (parameter.getId().contains("__locations")) {
					
					if (parameter.getAnnotationString().contains("array:" + id + "=") ||
							(parameter.getAnnotationString().contains("[[" + id + "]]"))) {
						
						componentModelRef = parameter.getId().replace("__locations","");
						
						if (componentModelRef.split("__").length > 1)
							componentModelRef = componentModelRef.split("__")[1];
						
						break;
					}		
				}				
			}
		}
		ExternalModelDefinition extModel = sbmlComp.getExternalModelDefinition(componentModelRef);
		if (extModel==null) {
			return "";
		}		
		return extModel.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
	}

	public HashMap<String, String> getInputs(String id) {
		HashMap<String, String> inputs = new HashMap<String, String>();
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbml.getModel().getSpecies(i).getPlugin("comp");
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
				ReplacedElement replacement = sbmlSBase.getReplacedElement(j);
				if (replacement.getSubmodelRef().equals(id) && replacement.isSetAnnotation() &&
					replacement.getAnnotation().toXMLString().contains("Input")) {
					String IdRef = replacement.getIdRef();
					replacement.unsetIdRef();
					replacement.setPortRef(GlobalConstants.INPUT+"__"+IdRef);
					replacement.unsetAnnotation();
					inputs.put(replacement.getPortRef().replace(GlobalConstants.INPUT+"__",""),
							sbml.getModel().getSpecies(i).getId());
				} else if  (replacement.getSubmodelRef().equals(id) && replacement.isSetPortRef() &&
				    replacement.getPortRef().startsWith(GlobalConstants.INPUT+"__")) {
					inputs.put(replacement.getPortRef().replace(GlobalConstants.INPUT+"__",""),
							sbml.getModel().getSpecies(i).getId());
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				Replacing replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(id) && (replacement.isSetPortRef()) &&
				   (replacement.getPortRef().startsWith(GlobalConstants.INPUT+"__"))) {
					inputs.put(replacement.getPortRef().replace(GlobalConstants.INPUT+"__",""),
							sbml.getModel().getSpecies(i).getId());
				}
			}
		}
		return inputs;
	}

	public HashMap<String, String> getOutputs(String id) {
		HashMap<String, String> outputs = new HashMap<String, String>();
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			CompSBasePlugin sbmlSBase = (CompSBasePlugin)sbml.getModel().getSpecies(i).getPlugin("comp");
			for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
				ReplacedElement replacement = sbmlSBase.getReplacedElement(j);
				if (replacement.getSubmodelRef().equals(id) && replacement.isSetAnnotation() &&
				    replacement.getAnnotation().toXMLString().contains("Output")) {
					String IdRef = replacement.getIdRef();
					replacement.unsetIdRef();
					replacement.setPortRef(GlobalConstants.OUTPUT+"__"+IdRef);
					replacement.unsetAnnotation();
					outputs.put(replacement.getPortRef().replace(GlobalConstants.OUTPUT+"__",""),
							sbml.getModel().getSpecies(i).getId());
				} else if  (replacement.getSubmodelRef().equals(id) && replacement.isSetPortRef() &&
				    replacement.getPortRef().startsWith(GlobalConstants.OUTPUT+"__")) {
					outputs.put(replacement.getPortRef().replace(GlobalConstants.OUTPUT+"__",""),
							sbml.getModel().getSpecies(i).getId());
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				Replacing replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(id) && (replacement.isSetPortRef()) &&
				   (replacement.getPortRef().startsWith(GlobalConstants.OUTPUT+"__"))) {
					outputs.put(replacement.getPortRef().replace(GlobalConstants.OUTPUT+"__",""),
							sbml.getModel().getSpecies(i).getId());
				}
			}
		}
		return outputs;
	}
/*	
	public HashMap<String, Properties> getComponents() {
		return components;
	}
	public HashMap<String, Properties> getCompartments() {
		return compartments;
	}
	*/
	
	/**
	 * returns the submodel's row from the location annotation
	 */
	public int getSubmodelRow(String submodelID) {
		
		String componentModelRefID = "";
		
		//search through the parameter location arrays to find the correct one
		for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
			
			Parameter parameter = sbml.getModel().getParameter(i);
			
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
				
				if (parameter.getAnnotationString().contains("array:" + submodelID + "=")) {
					
					componentModelRefID = parameter.getId().replace("__locations","");
					break;
				}		
			}				
		}
		
		String locationParameterString = componentModelRefID + "__locations";
		
		//get rid of the component from the location-lookup array and the modelref array
		String locationAnnotationString = sbml.getModel().getParameter(locationParameterString).getAnnotationString();		
		int submodelStringIndex = locationAnnotationString.indexOf("array:" + submodelID, 0);		
		String leftPart = locationAnnotationString.substring(submodelStringIndex, locationAnnotationString.indexOf(")", submodelStringIndex));
		String rowCol = leftPart.split("=")[1];
		String row = rowCol.split(",")[0].replace("\"(","");
		
		return Integer.parseInt(row);
	}
	
	/**
	 * returns the submodel's col from the location annotation
	 */
	public int getSubmodelCol(String submodelID) {
		
		String componentModelRefID = "";
		
		//search through the parameter location arrays to find the correct one
		for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
			
			Parameter parameter = sbml.getModel().getParameter(i);
			
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
				
				if (parameter.getAnnotationString().contains("array:" + submodelID + "=")) {
					
					componentModelRefID = parameter.getId().replace("__locations","");
					break;
				}		
			}				
		}
		
		String locationParameterString = componentModelRefID + "__locations";
		
		//get rid of the component from the location-lookup array and the modelref array
		String locationAnnotationString = sbml.getModel().getParameter(locationParameterString).getAnnotationString();
		int submodelStringIndex = locationAnnotationString.indexOf("array:" + submodelID, 0);		
		String leftPart = locationAnnotationString.substring(submodelStringIndex, locationAnnotationString.indexOf(")", submodelStringIndex));
		String rowCol = leftPart.split("=")[1];
		String col = rowCol.split(",")[1];
		
		return Integer.parseInt(col);
	}
	
	/**
	 * changes the row/col of a specified submodel
	 * 
	 * @param submodelID
	 * @param row
	 * @param col
	 */
	public void setSubmodelRowCol(String submodelID, int row, int col) {
		
		String componentModelRefID = "";
		
		//search through the parameter location arrays to find the correct one
		for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
			
			Parameter parameter = sbml.getModel().getParameter(i);
			
			//if it's a location parameter
			if (parameter.getId().contains("__locations")) {
				
				if (parameter.getAnnotationString().contains("array:" + submodelID + "=")) {
					
					componentModelRefID = parameter.getId().replace("__locations","");
					break;
				}		
			}				
		}
		
		String locationParameterString = componentModelRefID + "__locations";
		
		sbml.getModel().getParameter(locationParameterString).getAnnotation().getChild(0).removeAttr(
				sbml.getModel().getParameter(locationParameterString).getAnnotation().getChild(0).getAttrIndex("array:" + submodelID));
		sbml.getModel().getParameter(locationParameterString).getAnnotation().getChild(0)
		.addAttr("array:" + submodelID, "(" + row + "," + col + ")");
	}

	/**
	 * returns all the ports in the gcm file matching type, which must be either
	 * GlobalConstants.INPUT or GlobalConstants.OUTPUT.
	 * 
	 * @param type
	 * @return
	 */
	public ArrayList<String> getSpeciesPorts(String type) {
		ArrayList<String> out = new ArrayList<String>();
		for (String speciesId : this.getSpecies()) {
			if (getSpeciesType(sbml,speciesId).equals(type)) {
				out.add(speciesId);
			}
		}
		return out;
	}
	
	public void setSpeciesType(String speciesId,String type) {
		Port port = getPortByIdRef(speciesId);
		if (type.equals(GlobalConstants.INPUT)) {
			if (port==null) {
				port = sbmlCompModel.createPort();
			}
			port.setId(GlobalConstants.INPUT+"__"+speciesId);
			port.setIdRef(speciesId);
		} else if (type.equals(GlobalConstants.OUTPUT)) {
			if (port==null) {
				port = sbmlCompModel.createPort();
			}
			if (!port.isSetId() || port.getId().equals(GlobalConstants.INPUT+"__"+speciesId)) {
				port.setId(GlobalConstants.OUTPUT+"__"+speciesId);
			}
			port.setIdRef(speciesId);
		} else if (port != null) {
			port.removeFromParentAndDelete();
		}
	}
	
	public static String getSpeciesType(SBMLDocument sbml,String speciesId) {
		Species species = sbml.getModel().getSpecies(speciesId);
		CompModelPlugin sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		if (sbmlCompModel!=null) {
			if (sbmlCompModel.getPort(GlobalConstants.INPUT+"__"+speciesId)!=null) return GlobalConstants.INPUT;
			if (getPortByIdRef(sbmlCompModel,speciesId)!=null) return GlobalConstants.OUTPUT;
		}
		if (species.isSetAnnotation()) {
			String annotation = species.getAnnotationString().replace("<annotation>","").replace("</annotation>","");
			String [] annotations = annotation.split(",");
			for (int i=0;i<annotations.length;i++) {
				if (annotations[i].startsWith(GlobalConstants.TYPE)) {
					String [] type = annotations[i].split("=");
					if (sbmlCompModel!=null) {
						if (type.length < 2) {
							species.unsetAnnotation();
							return GlobalConstants.INTERNAL;
						} else if (type[1].equals(GlobalConstants.INPUT)) {
							Port port = sbmlCompModel.createPort();
							port.setId(GlobalConstants.INPUT+"__"+speciesId);
							port.setIdRef(speciesId);
							species.unsetAnnotation();
							return type[1];
						} else if (type[1].equals(GlobalConstants.OUTPUT)) {
							Port port = sbmlCompModel.createPort();
							port.setId(GlobalConstants.OUTPUT+"__"+speciesId);
							port.setIdRef(speciesId);
							species.unsetAnnotation();
							return type[1];
						} else if (type[1].equals(GlobalConstants.INTERNAL)) {
							species.unsetAnnotation();
							return type[1];
						}
					}
				}
			}
		} 
		if (isMRNASpecies(species)) {
			return GlobalConstants.MRNA; 
		}
		if (isPromoterSpecies(species)) {
			return GlobalConstants.PROMOTER; 
		}
		return GlobalConstants.INTERNAL;
	}

	public boolean isSpeciesConstitutive(String speciesId) {
		Reaction constitutive = sbml.getModel().getReaction("Constitutive_"+speciesId);
		if (constitutive != null) {
			if (isConstitutiveReaction(constitutive)) return true;
		}
		return false;
	}
	
	public Reaction getDegradationReaction(String speciesId) {
		Reaction degradation = sbml.getModel().getReaction("Degradation_"+speciesId);
		if (degradation != null) {
			if (degradation.isSetSBOTerm()) {
				if (degradation.getSBOTerm()==GlobalConstants.SBO_DEGRADATION) return degradation;
			} else if (degradation.isSetAnnotation()) {
				if (degradation.getAnnotationString().contains("Degradation")) {
					degradation.setSBOTerm(GlobalConstants.SBO_DEGRADATION);
					degradation.unsetAnnotation();
					return degradation;
				}
			}
		}
		return null;
	}
	
	public Reaction getDegradationReaction(String speciesId, Model model) {
		Reaction degradation = model.getReaction("Degradation_"+speciesId);
		if (degradation != null) {
			if (degradation.isSetSBOTerm()) {
				if (degradation.getSBOTerm()==GlobalConstants.SBO_DEGRADATION) return degradation;
			} else if (degradation.isSetAnnotation()) {
				if (degradation.getAnnotationString().contains("Degradation")) {
					degradation.setSBOTerm(GlobalConstants.SBO_DEGRADATION);
					degradation.unsetAnnotation();
					return degradation;
				}
			}
		}
		return null;
	}
	
	public Reaction getDiffusionReaction(String speciesId) {
		Reaction diffusion = sbml.getModel().getReaction("MembraneDiffusion_"+speciesId);
		if (diffusion == null) {
			diffusion = sbml.getModel().getReaction("Diffusion_"+speciesId);
			if (diffusion != null) {
				diffusion.setId("MembraneDiffusion_"+speciesId);
			}
		}
		if (diffusion != null) {
			if (diffusion.isSetSBOTerm()) {
				if (diffusion.getSBOTerm()==GlobalConstants.SBO_DIFFUSION) return diffusion;
			} else if (diffusion.isSetAnnotation()) {
				if (diffusion.getAnnotationString().contains("Diffusion")) {
					diffusion.setSBOTerm(GlobalConstants.SBO_DIFFUSION);
					diffusion.unsetAnnotation();
					return diffusion;
				}
			}
		}
		return null;
	}
	
	public Reaction getProductionReaction(String speciesId) {
		String component = "";
		if (speciesId.contains("__")) {
			component = speciesId.substring(0,speciesId.lastIndexOf("__")+2);
			speciesId = speciesId.substring(speciesId.lastIndexOf("__")+2);
		}
		Reaction production = sbml.getModel().getReaction(component+"Production_"+speciesId);
		if (production != null) {
			if (production.isSetSBOTerm()) {
				if (production.getSBOTerm()==GlobalConstants.SBO_PRODUCTION) return production;
			} else if (production.isSetAnnotation()) {
				if (production.getAnnotationString().contains("Production")) {
					production.setSBOTerm(GlobalConstants.SBO_PRODUCTION);
					production.unsetAnnotation();
					return production;
				}
			}
		}
		return null;
	}
	
	public Reaction getConstitutiveReaction(String speciesId) {
		Reaction constitutive = sbml.getModel().getReaction("Constitutive_"+speciesId);
		if (constitutive != null) {
			if (BioModel.isConstitutiveReaction(constitutive)) return constitutive;
		}
		return null;
	}
	
	public Reaction getComplexReaction(String speciesId) {
		String component = "";
		if (speciesId.contains("__")) {
			component = speciesId.substring(0,speciesId.lastIndexOf("__")+2);
			speciesId = speciesId.substring(speciesId.lastIndexOf("__")+2);
		}
		Reaction complex = sbml.getModel().getReaction(component+"Complex_"+speciesId);
		if (complex != null) {
			if (isComplexReaction(complex)) return complex;
		}
		return null;
	}

	public boolean isSpeciesComplex(String speciesId) {
		Reaction complex = getComplexReaction(speciesId);
		if (complex != null) {
			return true;
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
				replacement.getPortRef().equals(type+"__"+port)) {
				found = true;
				break;
			}
		}
		if (!found) replacement = sbmlSBase.createReplacedElement();
		replacement.setSubmodelRef(compId);
		replacement.setPortRef(type+"__"+port);
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
					portmap += replacement.getPortRef().replace(GlobalConstants.INPUT+"__","").replace(GlobalConstants.OUTPUT+"__","") 
							+ "->" + species.getId();
					first = false;
				}
			}
			if (sbmlSBase.isSetReplacedBy()) {
				Replacing replacement = sbmlSBase.getReplacedBy();
				if (replacement.getSubmodelRef().equals(s)) {
					if (!first) portmap += ", ";
					portmap += replacement.getPortRef().replace(GlobalConstants.INPUT+"__","").replace(GlobalConstants.OUTPUT+"__","") 
							+ "<-" + species.getId();
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
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				layout.removeSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id) != null) {
				layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+id);
			}
		}
	}

	/**
	 * removes a component from the model
	 * 
	 * @param name
	 */
	public void removeComponent(String name) {
		
		String componentModelRef = "";
		
		if (sbmlCompModel.getSubmodel(name) != null) {
			
			componentModelRef = sbmlCompModel.getSubmodel(name).getModelRef();
		}
		else {
			
			//look through the location parameter arrays to find the correct model ref
			for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter
				if (parameter.getId().contains("__locations")) {
					
					if (parameter.getAnnotationString().contains("array:" + name + "=")) {
						
						componentModelRef = parameter.getId().replace("__locations","");						
						break;
					}					
				}				
			}
		}
		
		String locationParameterString = componentModelRef + "__locations";
		
		if (sbml.getModel().getParameter(locationParameterString) != null) {
		
			//get rid of the component from the location-lookup array and the modelref array
			sbml.getModel().getParameter(locationParameterString).getAnnotation().getChild(0).removeAttr(
					sbml.getModel().getParameter(locationParameterString).getAnnotation().getChild(0).getAttrIndex("array:" + name));
			
			if (sbml.getModel().getParameter(locationParameterString).getAnnotationString().length() < 1)
				sbml.getModel().removeParameter(locationParameterString);
		}
		
		//if a gridded/arrayed submodel exists, it'll have this ID	
		String gridSubmodelID = "GRID__" + componentModelRef;
		
		Submodel potentialGridSubmodel = sbmlCompModel.getSubmodel(gridSubmodelID);
		
		if (potentialGridSubmodel == null)
			gridSubmodelID = componentModelRef;
		
		potentialGridSubmodel = sbmlCompModel.getSubmodel(gridSubmodelID);
		
		if (potentialGridSubmodel != null) {
			
			//if the annotation string already exists, then one of these existed before
			//so update its count
			if (potentialGridSubmodel.getAnnotationString().length() > 0) {
				
				int size = Integer.parseInt(potentialGridSubmodel.getAnnotationString()
						.replace("\"","").split("=")[2].replace("/>","").replace("</annotation>","").trim());
				
				//if we're getting rid of the last submodel of its kind
				//then delete its grid species (if they exist) and the GRID__ submodel
				if (size == 1) {
					
					//find the right submodel index to delete it
					for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
						
						if (sbmlCompModel.getSubmodel(i).getId().equals(gridSubmodelID))
							sbmlCompModel.removeSubmodel(i);
					}
					
					//remove the grid species this submodel had and its locations parameter
					sbml.getModel().removeParameter(locationParameterString);
					
					if (grid.getNumCols() > 0 && grid.getNumRows() > 0)
						removeGridSpecies(componentModelRef);
					
					this.getSBMLComp().removeExternalModelDefinition(componentModelRef);
				}
				else {					
					
					XMLAttributes attr = new XMLAttributes();			
					attr.add("xmlns:array", "http://www.fakeuri.com");
					attr.add("array:count", String.valueOf(--size));
					XMLNode node = new XMLNode(new XMLTriple("array","","array"), attr);
					
					potentialGridSubmodel.setAnnotation(node);
				}
			}
		}
		else {
			
			for (int i = 0; i < sbmlCompModel.getNumSubmodels(); ++i) {
				
				if (sbmlCompModel.getSubmodel(i).getId().equals(name))
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
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+name)!=null) {
				layout.removeCompartmentGlyph(GlobalConstants.GLYPH+"__"+name);
			}
			if (layout.getTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+name) != null) {
				layout.removeTextGlyph(GlobalConstants.TEXT_GLYPH+"__"+name);
			}
		}
	}
	
	public void removeComponentConnection(String speciesId,String componentId,String portId) {
		Species species = sbml.getModel().getSpecies(speciesId);
		CompSBasePlugin sbmlSBase = (CompSBasePlugin)species.getPlugin("comp");
		ReplacedElement replacement = null;
		for (long j = 0; j < sbmlSBase.getNumReplacedElements(); j++) {
			replacement = sbmlSBase.getReplacedElement(j);
			if (replacement.getSubmodelRef().equals(componentId) && replacement.getPortRef().equals(portId)) {
				sbmlSBase.removeReplacedElement(j);
			}
		}
	}		

	public void removeInfluence(String name) {
		if (name.contains("+")) {
			Reaction reaction = getComplexReaction(name.substring(name.indexOf(">")+1));
			reaction.removeReactant(name.substring(0,name.indexOf("+")));
			if (reaction.getNumReactants()==0) {
				sbml.getModel().removeReaction(reaction.getId());
			} else {
				createComplexKineticLaw(reaction);
			}
		} else if (name.contains(",")) {
			Reaction reaction = getProductionReaction(name.substring(name.indexOf(",")+1));
			if (reaction!=null) {
				ModifierSpeciesReference modifier = reaction.getModifier(name.substring(0,name.indexOf("-")));
				if (isRegulator(modifier)) {
					if (name.contains("|")) {
						modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
					} else {
						modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
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
			}
		} else if (name.contains("|")) {
			Reaction reaction = getProductionReaction(name.substring(name.indexOf("|")+1));
			ModifierSpeciesReference modifier = reaction.getModifier(name.substring(0,name.indexOf("-")));
			if (isRegulator(modifier)) {
				modifier.setSBOTerm(GlobalConstants.SBO_ACTIVATION);
			} else {
				if (name.contains("x>")) {
					reaction.removeModifier(name.substring(0,name.indexOf(">")-1));
				} else {
					reaction.removeModifier(name.substring(0,name.indexOf("-")));
				}
			}
			createProductionKineticLaw(reaction);
		} else if (name.contains(">")) {
			Reaction reaction = getProductionReaction(name.substring(name.indexOf(">")+1));
			if (reaction!=null) {
				ModifierSpeciesReference modifier = null;
				if (name.contains("x>")) {
					modifier = reaction.getModifier(name.substring(0,name.indexOf(">")-1));
				} else {
					modifier = reaction.getModifier(name.substring(0,name.indexOf("-")));
				}
				if (isRegulator(modifier)) {
					modifier.setSBOTerm(GlobalConstants.SBO_REPRESSION);
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
				reaction = getProductionReaction(promoterId);
				reaction.removeProduct(name.substring(name.indexOf(">")+1));
				if (reaction.getNumProducts()==0) {
					Species mRNA = sbml.getModel().createSpecies();
					mRNA.setId(promoterId+"_mRNA");
					mRNA.setCompartment(reaction.getCompartment());
					mRNA.setSBOTerm(GlobalConstants.SBO_MRNA);
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
	 * creates/removes grid species if a file is updated
	 * 
	 * @param filename
	 */
	public void updateGridSpecies(String componentModelRef) {
		
		componentModelRef = componentModelRef.replace(".xml", "");
		
		//loop through all components of this model ref
		if (sbml.getModel().getParameter(componentModelRef + "__locations") != null) {
				
			removeGridSpecies(componentModelRef);
			createGridSpecies("GRID__" + componentModelRef);
		}
	}
	
	/**
	 * removes the diffusible grid species from the model (but checks to make sure other submodels
	 * don't have them first)
	 * 
	 * @param componentID
	 */
	public void removeGridSpecies(String componentModelRef) {
		
		//find the sbml file for the component
		String externalModelID = 
			this.getSBMLComp().getExternalModelDefinition(componentModelRef).getSource().replace("file://","").replace("file:","");
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = null;
		
		String path = this.getPath();
		
		if (this.getPath().charAt(this.getPath().length() - 1) != '/')
			path += '/';
		
		//load the sbml file
		document = reader.readSBML(path + externalModelID);
		Model componentModel = document.getModel();
		
		ArrayList<String> speciesToRemove = new ArrayList<String>();
		
		//check all species in the component for diffusibility
		//if they're diffusible, they're candidates for being removed from the model
		//also, if they're not diffusible and on the grid species list, they're candidates for removal
		for (int speciesIndex = 0; speciesIndex < componentModel.getNumSpecies(); ++speciesIndex) {
			
			String speciesID = componentModel.getListOfSpecies().get(speciesIndex).getId();			
			Reaction diffusionReaction = componentModel.getReaction("MembraneDiffusion_" + speciesID);
			
			if (diffusionReaction != null && isDiffusionReaction(diffusionReaction)
					|| sbml.getModel().getSpecies(speciesID) != null) {
				
				speciesToRemove.add(speciesID);
				
				//see if the species is degradable.  if this status changes, then the degradation reactions
				//need to be updated.
				Reaction degradationReaction = getDegradationReaction(speciesID, componentModel);
				
				if (degradationReaction == null) {
					sbml.getModel().removeReaction("Degradation_" + speciesID);
				}
			}
		}
		
		//check all other submodels to make sure the species really should be removed
		//(ie, they don't exist anywhere else)
		for (int submodelIndex = 0; submodelIndex < sbmlCompModel.getNumSubmodels(); ++submodelIndex) {
			
			componentModelRef = sbmlCompModel.getSubmodel(submodelIndex).getModelRef();
			
			//find the sbml file for the component
			externalModelID = 
				this.getSBMLComp().getExternalModelDefinition(componentModelRef).getSource().replace("file://","").replace("file:","");
			
			//load the sbml file
			document = reader.readSBML(path + externalModelID);
			componentModel = document.getModel();
			
			//check all species in the component for diffusibility
			//if they're diffusible and they're in the removal list, they shouldn't be removed as grid species
			for (int speciesIndex = 0; speciesIndex < componentModel.getNumSpecies(); ++speciesIndex) {
				
				String speciesID = componentModel.getListOfSpecies().get(speciesIndex).getId();			
				Reaction diffusionReaction = componentModel.getReaction("MembraneDiffusion_" + speciesID);
				
				//if this is true, then this species shouldn't be removed because it exists elsewhere
				if (diffusionReaction != null && isDiffusionReaction(diffusionReaction) &&
						speciesToRemove.contains(speciesID)) {
					
					speciesToRemove.remove(speciesID);
				}
			}			
		}
		
		//remove the grid species from the model
		for (String specToRemove : speciesToRemove) {
			sbml.getModel().removeSpecies(specToRemove);
		}
		
		if (speciesToRemove.size() > 0)
			removeGridSpeciesReactions(speciesToRemove);
	}
	
	/**
	 * searches a component for diffusible species and adds them to the grid level
	 * 
	 * @param componentID
	 */
	public void createGridSpecies(String submodelID) {
		
		String componentModelRef = sbmlCompModel.getSubmodel(submodelID).getModelRef();
		
		//find the sbml file for the component
		String externalModelID = 
			this.getSBMLComp().getExternalModelDefinition(componentModelRef).getSource().replace("file://","").replace("file:","");
		
		SBMLReader reader = new SBMLReader();
		SBMLDocument document = null;
		
		if (this.getPath().charAt(this.getPath().length() - 1) != '/')
			this.path += '/';
		
		//load the sbml file
		document = reader.readSBML(this.getPath() + externalModelID);
		Model componentModel = document.getModel();
		
		ArrayList<Species> speciesToAdd = new ArrayList<Species>();
		
		System.err.println("creating grid species for " + submodelID);
		
		//check all species in the component for diffusibility
		//if they're diffusible, they're candidates for being added as a grid species
		for (int speciesIndex = 0; speciesIndex < componentModel.getNumSpecies(); ++speciesIndex) {
			
			String speciesID = componentModel.getListOfSpecies().get(speciesIndex).getId();			
			Reaction diffusionReaction = componentModel.getReaction("MembraneDiffusion_" + speciesID);
			
			if (diffusionReaction != null && isDiffusionReaction(diffusionReaction)) {
				
				speciesToAdd.add(componentModel.getListOfSpecies().get(speciesIndex));
				
				//if it's degradable and there's no degradation reaction, add one
				if (getDegradationReaction(speciesID) == null &&
						getDegradationReaction(speciesID, componentModel) != null) {
					createGridDegradationReaction(componentModel.getListOfSpecies().get(speciesIndex), sbml.getModel());
				}				
			}
		}
		
		//add diffusible species as grid species if they don't already exist
		for (Species specToAdd : speciesToAdd) {
			
			if (sbml.getModel().getSpecies(specToAdd.getId()) == null) {
				
				XMLAttributes attr = new XMLAttributes();
				attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
				attr.add("ibiosim:type", "grid");
				XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
			
				Species newSpecies = this.getSBMLDocument().getModel().createSpecies();
				newSpecies.setId(specToAdd.getId());
				newSpecies.setAnnotation(node);
				newSpecies.setInitialAmount(0.0);
				newSpecies.setBoundaryCondition(specToAdd.getBoundaryCondition());
				newSpecies.setConstant(specToAdd.getConstant());
				newSpecies.setHasOnlySubstanceUnits(specToAdd.getHasOnlySubstanceUnits());
				newSpecies.setCompartment(getDefaultCompartment());
			}
		}
		
		//if new grid species were added, create diffusion/degradation reactions for them
		if (speciesToAdd.size() > 0)
			createGridSpeciesReactions(speciesToAdd, componentModel);
	}
	
	public void createGridDegradationReaction(Species species, Model compModel) {
		
		String speciesID = species.getId();
		Boolean speciesDegrades = false;			
		Reaction degradation = this.getDegradationReaction(speciesID, compModel);
		
		//fix the sbo term/annotation stuff if it's not correct
		if (degradation != null)
			speciesDegrades = true;
		
		//only make grid degradation reactions if the species is degradable
		if (speciesDegrades) {
		
			//create array of grid degradation reactions
			String decayString = GlobalConstants.KECDECAY_STRING;
			double decayRate = sbml.getModel().getParameter("kecd").getValue();
				
			String decayUnitString = "u_1_second_n1";
			
			if (sbml.getModel().getUnitDefinition(decayUnitString) == null) {
				
				UnitDefinition ud = sbml.getModel().createUnitDefinition();
				Unit unit = ud.createUnit();
				unit.setExponent(-1);
				unit.setKind(libsbml.UnitKind_forName("second"));
				unit.setScale(0);
				unit.setMultiplier(1);
				ud.setId(decayUnitString);
				sbml.getModel().addUnitDefinition(ud);
			}
			
			Reaction r = sbml.getModel().createReaction();
			r.setId("Degradation_" + speciesID);
			r.setMetaId(r.getId());
			r.setCompartment(sbml.getModel().getCompartment(0).getId());
			r.setReversible(false);
			r.setFast(false);
			
			XMLAttributes attr = new XMLAttributes();
			attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
			attr.add("ibiosim:type", "grid");
			XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
			
			r.setAnnotation(node);
			
			KineticLaw kl = r.createKineticLaw();
			
			if (decayRate > 0) {
				
				//this is the mathematical expression for the decay
				String decayExpression = decayString + "* get2DArrayElement(" + speciesID + ", i, j)";				
				
				SpeciesReference reactant = r.createReactant();
				reactant.setSpecies(speciesID);
				reactant.setStoichiometry(1);
				reactant.setConstant(false);
				
				LocalParameter i = kl.createLocalParameter();
				LocalParameter j = kl.createLocalParameter();
				
				attr = new XMLAttributes();
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:min", "0");
				attr.add("array:max", String.valueOf(this.getGrid().getNumRows() - 1));
				node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				i.setAnnotation(node);
				
				attr = new XMLAttributes();
				attr.add("xmlns:array", "http://www.fakeuri.com");
				attr.add("array:min", "0");
				attr.add("array:max", String.valueOf(this.getGrid().getNumCols() - 1));
				node = new XMLNode(new XMLTriple("array","","array"), attr);
				
				j.setAnnotation(node);
				
				i.setId("i");
				j.setId("j");
				
				//parameter: id="kecd" value=(usually 0.005) units="u_1_second_n1" (inverse seconds)
				kl.addParameter(Utility.Parameter(decayString, decayRate, decayUnitString));
				
				//formula: kecd * species
				kl.setFormula(decayExpression);
				
				Utility.addReaction(sbml, r);
			}
		}		
	}
	
	/**
	 * create grid species reactions for the new grid species
	 * 
	 * @param newGridSpecies
	 */
	public void createGridSpeciesReactions(ArrayList<Species> newGridSpecies, Model compModel) {
		
		//create functions for getting an array element
		SBMLutilities.createFunction(
				sbml.getModel(), "get2DArrayElement", "get2DArrayElement", "lambda(a,b,c,a)");
		
		for (Species newSpecies : newGridSpecies) {
			
			createGridDegradationReaction(newSpecies, compModel);
			
			String speciesID = newSpecies.getId();
			
			//create array of grid diffusion reactions
			//NOTE: does not do diffusion with component species
			//loop though each of the four directions and add a diffusion reaction
			//implicitly, these will be arrays of reactions
			
			String diffusionUnitString = "u_1_second_n1";
			String diffusionString = GlobalConstants.KECDIFF_STRING;
			String diffComp = sbml.getModel().getCompartment(0).getId();
			double kecdiff = sbml.getModel().getParameter("kecdiff").getValue();
			
			for (int index = 0; index < 4; ++index) {
				
				String direction = "";
				String neighborRowIndexOffset = "0";
				String neighborColIndexOffset = "0";
				
				switch (index) {
				
					case 0: {direction = "Above"; neighborRowIndexOffset = "- 1"; neighborColIndexOffset = "+ 0"; break;}						
					case 1: {direction = "Below"; neighborRowIndexOffset = "+ 1"; neighborColIndexOffset = "+ 0"; break;}						
					case 2: {direction = "Left"; neighborRowIndexOffset = "+ 0"; neighborColIndexOffset = "- 1"; break;}						
					case 3: {direction = "Right"; neighborRowIndexOffset = "+ 0"; neighborColIndexOffset = "+ 1"; break;}			
				}
				
				//reversible between neighboring "outer" species
				//this is the diffusion across the "medium" if you will
				Reaction r = sbml.getModel().createReaction();
				r.setId("Diffusion_" + speciesID + "_" + direction);
				r.setMetaId(r.getId());
				r.setCompartment(diffComp);
				r.setReversible(true);
				r.setFast(false);
				
				XMLAttributes attr = new XMLAttributes();
				attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
				attr.add("ibiosim:type", "grid");
				XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
				
				r.setAnnotation(node);
				KineticLaw kl = r.createKineticLaw();
				
				if (kecdiff > 0) {
				
					//this is the rate times the current species minus the rate times the neighbor species
					String diffusionExpression = 
						diffusionString + " * " + "get2DArrayElement(" + speciesID + ", i, j)" + "-"
						+ diffusionString + " * " + "get2DArrayElement(" + speciesID + ", i " 
						+ String.valueOf(neighborRowIndexOffset) + ", j " + String.valueOf(neighborColIndexOffset) + ")";

					//reactant is current outer species					
					SpeciesReference reactant = r.createReactant();
					reactant.setSpecies(speciesID);
					reactant.setStoichiometry(1);
					reactant.setConstant(false);
					
					//product is neighboring species
					SpeciesReference product = r.createProduct();
					product.setSpecies(speciesID);
					product.setStoichiometry(1);
					product.setConstant(false);
					
					LocalParameter i = kl.createLocalParameter();
					LocalParameter j = kl.createLocalParameter();
					
					attr = new XMLAttributes();
					attr.add("xmlns:array", "http://www.fakeuri.com");
					attr.add("array:min", "0");
					attr.add("array:max", String.valueOf(this.getGrid().getNumRows() - 1));
					node = new XMLNode(new XMLTriple("array","","array"), attr);
					
					i.setAnnotation(node);
					
					attr = new XMLAttributes();
					attr.add("xmlns:array", "http://www.fakeuri.com");
					attr.add("array:min", "0");
					attr.add("array:max", String.valueOf(this.getGrid().getNumCols() - 1));
					node = new XMLNode(new XMLTriple("array","","array"), attr);
					
					j.setAnnotation(node);
					
					i.setId("i");
					j.setId("j");
					
					//parameters: id="kecdiff" value=kecdiff units="u_1_second_n1" (inverse seconds)
					kl.addParameter(Utility.Parameter(diffusionString, kecdiff, diffusionUnitString));					
					kl.setFormula(diffusionExpression);
					
					Utility.addReaction(sbml, r);
				}
			}		
			
			//create array of membrane diffusion reactions
			
			String membraneDiffusionComp = sbml.getModel().getCompartment(0).getId();
			
			Reaction r = sbml.getModel().createReaction();
			r.setId("MembraneDiffusion_" + speciesID);
			r.setMetaId(r.getId());
			r.setCompartment(membraneDiffusionComp);
			r.setReversible(true);
			r.setFast(false);
			
			XMLAttributes attr = new XMLAttributes();
			attr.add("xmlns:ibiosim", "http://www.fakeuri.com");
			attr.add("ibiosim:type", "grid");
			XMLNode node = new XMLNode(new XMLTriple("ibiosim","","ibiosim"), attr);
			
			r.setAnnotation(node);
			
			KineticLaw kl = r.createKineticLaw();
			
			//this is the rate times the inner species minus the rate times the outer species
			String membraneDiffusionExpression = "get2DArrayElement(kmdiff_f, i, j) * get2DArrayElement(" 
				+ speciesID + ", i, j" + ") - get2DArrayElement(kmdiff_r, i, j) * get2DArrayElement(" 
				+ speciesID + ", i, j" + ")";

			//reactant is inner/submodel species					
			SpeciesReference reactant = r.createReactant();
			reactant.setSpecies(speciesID);
			reactant.setStoichiometry(1);
			reactant.setConstant(false);
			
			//product is outer species
			SpeciesReference product = r.createProduct();
			product.setSpecies(speciesID);
			product.setStoichiometry(1);
			product.setConstant(false);
			
			LocalParameter i = kl.createLocalParameter();
			LocalParameter j = kl.createLocalParameter();
			
			attr = new XMLAttributes();
			attr.add("xmlns:array", "http://www.fakeuri.com");
			attr.add("array:min", "0");
			attr.add("array:max", String.valueOf(this.getGrid().getNumRows() - 1));
			node = new XMLNode(new XMLTriple("array","","array"), attr);
			
			i.setAnnotation(node);
			
			attr = new XMLAttributes();
			attr.add("xmlns:array", "http://www.fakeuri.com");
			attr.add("array:min", "0");
			attr.add("array:max", String.valueOf(this.getGrid().getNumCols() - 1));
			node = new XMLNode(new XMLTriple("array","","array"), attr);
			
			j.setAnnotation(node);
			
			i.setId("i");
			j.setId("j");
			
			kl.setFormula(membraneDiffusionExpression);
			Utility.addReaction(sbml, r);			
		}
	}
	
	/**
	 * removes grid species reactions for the old species that no longer exist
	 * @param oldSpecies
	 */
	public void removeGridSpeciesReactions(ArrayList<String> oldGridSpecies) {
		
		for (String oldSpeciesID : oldGridSpecies) {
			
			//remove degredation reaction
			String reactionID = "Degradation_" + oldSpeciesID;
			sbml.getModel().removeReaction(reactionID);
			
			//remove membrane diffusion reaction
			reactionID = "MembraneDiffusion_" + oldSpeciesID;
			sbml.getModel().removeReaction(reactionID);
			
			//remove diffusion reactions
			for (int i = 0; i < 4; ++i) {
				
				String direction = "";
				
				switch (i) {
				
					case 0: {direction = "Above"; break;}						
					case 1: {direction = "Below"; break;}						
					case 2: {direction = "Left"; break;}						
					case 3: {direction = "Right"; break;}			
				}
				
				sbml.getModel().removeReaction("Diffusion_" + oldSpeciesID + "_" + direction);				
			}
		}
	}
	
	public boolean checkCompartmentLocation(String id,double x, double y, double w, double h) {
		for (long i = 0; i < sbml.getModel().getNumCompartments(); i++) {
			Compartment c = sbml.getModel().getCompartment(i);
			if (c.getId().equals(id)) continue;
			Layout layout = null;
			if (sbmlLayout.getLayout("iBioSim") != null) {
				layout = sbmlLayout.getLayout("iBioSim"); 
			} else {
				layout = sbmlLayout.createLayout();
				layout.setId("iBioSim");
			}
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+c.getId());
			double cx = compartmentGlyph.getBoundingBox().x();
			double cy = compartmentGlyph.getBoundingBox().y();
			double cw = compartmentGlyph.getBoundingBox().width();
			double ch = compartmentGlyph.getBoundingBox().height();
			if (x >= cx && y >= cy && x+w <= cx+cw && y+h <= cy+ch) continue;
			if (x <= cx && y <= cy && x+w >= cx+cw && y+h >= cy+ch) continue;
			if (x+w <= cx) continue;
			if (x >= cx+cw) continue;
			if (y+h <= cy) continue;
			if (y >= cy+ch) continue;
			return false;
		}
		return true;
	}
	
	public boolean updateCompartmentsByLocation(boolean checkOnly) {
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
			for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
				Species s = sbml.getModel().getSpecies(i);
				SpeciesGlyph speciesGlyph = null;
				if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s.getId())!=null) {
					speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+s.getId());
					String compartment = getCompartmentByLocation((float)speciesGlyph.getBoundingBox().x(),
							(float)speciesGlyph.getBoundingBox().y(),(float)speciesGlyph.getBoundingBox().width(),
							(float)speciesGlyph.getBoundingBox().height());
					if (compartment.equals("")) {
						if (sbml.getModel().getNumCompartments()>1) {
							return false;
						} else {
							CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+s.getCompartment());
							double x = speciesGlyph.getBoundingBox().x();
							double y = speciesGlyph.getBoundingBox().y();
							double w = speciesGlyph.getBoundingBox().width();
							double h = speciesGlyph.getBoundingBox().height();
							double cx = compartmentGlyph.getBoundingBox().x();
							double cy = compartmentGlyph.getBoundingBox().y();
							double cw = compartmentGlyph.getBoundingBox().width();
							double ch = compartmentGlyph.getBoundingBox().height();
							if (x < cx) {
								compartmentGlyph.getBoundingBox().setX(x);
								compartmentGlyph.getBoundingBox().setWidth(cw + cx - x);
							}
							if (y < cy) {
								compartmentGlyph.getBoundingBox().setY(y);
								compartmentGlyph.getBoundingBox().setHeight(ch + cy - y);
							}
							if (x + w > cx + cw) {
								compartmentGlyph.getBoundingBox().setWidth(x + w - cx);
							}
							if (y + h > cy + ch) {
								compartmentGlyph.getBoundingBox().setHeight(y + h - cy);
							}
						}
					}
					if (!checkOnly)	s.setCompartment(compartment);
				}
			}
			for (long i = 0; i < sbml.getModel().getNumReactions(); i++) {
				Reaction r = sbml.getModel().getReaction(i);
				ReactionGlyph reactionGlyph = null;
				if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId())!=null) {
					reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+r.getId());
					String compartment = getCompartmentByLocation((float)reactionGlyph.getBoundingBox().x(),
						(float)reactionGlyph.getBoundingBox().y(),(float)reactionGlyph.getBoundingBox().width(),
						(float)reactionGlyph.getBoundingBox().height());
					if (compartment.equals("")) {
						if (sbml.getModel().getNumCompartments()>1) {
							return false;
						} else {
							CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+r.getCompartment());
							double x = reactionGlyph.getBoundingBox().x();
							double y = reactionGlyph.getBoundingBox().y();
							double w = reactionGlyph.getBoundingBox().width();
							double h = reactionGlyph.getBoundingBox().height();
							double cx = compartmentGlyph.getBoundingBox().x();
							double cy = compartmentGlyph.getBoundingBox().y();
							double cw = compartmentGlyph.getBoundingBox().width();
							double ch = compartmentGlyph.getBoundingBox().height();
							if (x < cx) {
								compartmentGlyph.getBoundingBox().setX(x);
								compartmentGlyph.getBoundingBox().setWidth(cw + cx - x);
							}
							if (y < cy) {
								compartmentGlyph.getBoundingBox().setY(y);
								compartmentGlyph.getBoundingBox().setHeight(ch + cy - y);
							}
							if (x + w > cx + cw) {
								compartmentGlyph.getBoundingBox().setWidth(x + w - cx);
							}
							if (y + h > cy + ch) {
								compartmentGlyph.getBoundingBox().setHeight(y + h - cy);
							}
						}
					}
					if (!checkOnly) r.setCompartment(compartment);
				}
			}
		}
		return true;
	}
	
	public String getCompartmentByLocation(float x, float y, float w, float h) {
		String compartment = "";
		double distance = -1;
		for (long i = 0; i < sbml.getModel().getNumCompartments(); i++) {
			Compartment c = sbml.getModel().getCompartment(i);
			Layout layout = null;
			if (sbmlLayout.getLayout("iBioSim") != null) {
				layout = sbmlLayout.getLayout("iBioSim"); 
			} else {
				layout = sbmlLayout.createLayout();
				layout.setId("iBioSim");
			}
			CompartmentGlyph compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+c.getId());
			double cx = compartmentGlyph.getBoundingBox().x();
			double cy = compartmentGlyph.getBoundingBox().y();
			double cw = compartmentGlyph.getBoundingBox().width();
			double ch = compartmentGlyph.getBoundingBox().height();
			if (x >= cx && y >= cy && x + w <= cx+cw && y + h <= cy+ch) {
				double calcDist = (x - cx) + (y - cy);
				if (distance==-1 || distance > calcDist) {
					compartment = compartmentGlyph.getCompartmentId();
				}
			}
		}
		return compartment;
	}

	public void createSpecies(String id, float x, float y) {
		String compartment = getCompartmentByLocation(x,y, GlobalConstants.DEFAULT_SPECIES_WIDTH, 
				GlobalConstants.DEFAULT_SPECIES_HEIGHT);
		if (compartment.equals("")) {
			Utility.createErrorMessage("Compartment Required", "Species must be placed within a compartment.");
			return;
		}
		if (id == null) {
			do {
				creatingSpeciesID++;
				id = "S" + String.valueOf(creatingSpeciesID);
			} while (sbml.getElementBySId(id)!=null);
		}
		
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		SpeciesGlyph speciesGlyph = null;
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
		} else {
			speciesGlyph = layout.createSpeciesGlyph();
			speciesGlyph.setId(GlobalConstants.GLYPH+"__"+id);
			speciesGlyph.setSpeciesId(id);
		}
		speciesGlyph.getBoundingBox().setX(x);
		speciesGlyph.getBoundingBox().setY(y);
		speciesGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_SPECIES_WIDTH);
		speciesGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_SPECIES_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
		if (sbml != null && sbml.getModel().getSpecies(id)==null) {
			Model m = sbml.getModel();
			Species species = m.createSpecies();
			species.setId(id);
			
			// Set default species metaID
			metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, species, metaIDIndex); 
			
			species.setCompartment(compartment);
			species.setBoundaryCondition(false);
			species.setConstant(false);
			species.setInitialAmount(0);
			species.setHasOnlySubstanceUnits(true);
			if (speciesPanel!=null)
				speciesPanel.refreshSpeciesPanel(this);
		}
	}

	public void createReaction(String id, float x, float y) {
		String compartment = getCompartmentByLocation(x,y, GlobalConstants.DEFAULT_REACTION_WIDTH, 
				GlobalConstants.DEFAULT_REACTION_HEIGHT);
		if (compartment.equals("")) {
			Utility.createErrorMessage("Compartment Required", "Reactions must be placed within a compartment.");
			return;
		}
		if (id == null) {
			do {
				creatingReactionID++;
				id = "R" + String.valueOf(creatingReactionID);
			}
			while (sbml.getElementBySId(id)!=null);
		}
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		ReactionGlyph reactionGlyph = null;
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id);
		} else {
			reactionGlyph = layout.createReactionGlyph();
			reactionGlyph.setId(GlobalConstants.GLYPH+"__"+id);
			reactionGlyph.setReactionId(id);
		}
		reactionGlyph.getBoundingBox().setX(x);
		reactionGlyph.getBoundingBox().setY(y);
		reactionGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_REACTION_WIDTH);
		reactionGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_REACTION_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
		Model m = sbml.getModel();
		Reaction r = m.createReaction();
		r.setId(id);
		// Set default reaction metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, r, metaIDIndex); 
		r.setCompartment(compartment);
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

	public void createRule(String id, float x, float y) {
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		ReactionGlyph reactionGlyph = null;
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id);
		} else {
			reactionGlyph = layout.createReactionGlyph();
			reactionGlyph.setId(GlobalConstants.GLYPH+"__"+id);
			reactionGlyph.setReactionId(GlobalConstants.GLYPH+"__"+id);
		}
		reactionGlyph.getBoundingBox().setX(x);
		reactionGlyph.getBoundingBox().setY(y);
		reactionGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_RULE_WIDTH);
		reactionGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_RULE_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
	}

	public void createConstraint(String id, float x, float y) {
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		ReactionGlyph reactionGlyph = null;
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id);
		} else {
			reactionGlyph = layout.createReactionGlyph();
			reactionGlyph.setId(GlobalConstants.GLYPH+"__"+id);
			reactionGlyph.setReactionId(id);
		}
		reactionGlyph.getBoundingBox().setX(x);
		reactionGlyph.getBoundingBox().setY(y);
		reactionGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_CONSTRAINT_WIDTH);
		reactionGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_CONSTRAINT_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
	}

	public void createEvent(String id, float x, float y) {
		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		ReactionGlyph reactionGlyph = null;
		if (layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			reactionGlyph = layout.getReactionGlyph(GlobalConstants.GLYPH+"__"+id);
		} else {
			reactionGlyph = layout.createReactionGlyph();
			reactionGlyph.setId(GlobalConstants.GLYPH+"__"+id);
			reactionGlyph.setReactionId(id);
		}
		reactionGlyph.getBoundingBox().setX(x);
		reactionGlyph.getBoundingBox().setY(y);
		reactionGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_EVENT_WIDTH);
		reactionGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_EVENT_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(reactionGlyph.getBoundingBox());
	}
	
	public String createPromoter(String id, float x, float y, boolean is_explicit) {
		String compartment;
		compartment = getCompartmentByLocation(x,y,GlobalConstants.DEFAULT_SPECIES_WIDTH,GlobalConstants.DEFAULT_SPECIES_HEIGHT);
		if (compartment.equals("")) {
			Utility.createErrorMessage("Compartement Required", "Promoter must be placed within a compartment.");
			return "";
		}
		Species promoter = sbml.getModel().createSpecies();
		// Set default species ID
		if (id == null) {
			do {
				creatingPromoterID++;
				id = "P" + String.valueOf(creatingPromoterID);
			}
			while (sbml.getElementBySId(id)!=null);
		}
		promoter.setId(id);
		// Set default promoter metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, promoter, metaIDIndex); 
		
		promoter.setSBOTerm(GlobalConstants.SBO_PROMOTER_SPECIES);
		promoter.setInitialAmount(sbml.getModel().getParameter(GlobalConstants.PROMOTER_COUNT_STRING).getValue());

		promoter.setCompartment(compartment);
		promoter.setBoundaryCondition(false);
		promoter.setConstant(false);
		promoter.setHasOnlySubstanceUnits(true);
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
			if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
			} else {
				speciesGlyph = layout.createSpeciesGlyph();
				speciesGlyph.setId(GlobalConstants.GLYPH+"__"+id);
				speciesGlyph.setSpeciesId(id);
			}
			speciesGlyph.getBoundingBox().setX(x);
			speciesGlyph.getBoundingBox().setY(y);
			speciesGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_SPECIES_WIDTH);
			speciesGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_SPECIES_HEIGHT);
			TextGlyph textGlyph = layout.createTextGlyph();
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
			textGlyph.setText(id);
			textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());
		}

		return id;
	}
	
	public String createVariable(String id, float x, float y) {
		Parameter parameter = sbml.getModel().createParameter();
		// Set default species ID
		if (id == null) {
			do {
				creatingVariableID++;
				id = "V" + String.valueOf(creatingVariableID);
			}
			while ((sbml.getElementBySId(id)!=null)||(sbml.getElementByMetaId(id)!=null));
		}
		parameter.setId(id);
		// Set default promoter metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, parameter, metaIDIndex); 
		parameter.setConstant(false);
		parameter.setValue(0.0);

		Layout layout = null;
		if (sbmlLayout.getLayout("iBioSim") != null) {
			layout = sbmlLayout.getLayout("iBioSim"); 
		} else {
			layout = sbmlLayout.createLayout();
			layout.setId("iBioSim");
		}
		SpeciesGlyph speciesGlyph = null;
		if (layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
			speciesGlyph = layout.getSpeciesGlyph(GlobalConstants.GLYPH+"__"+id);
		} else {
			speciesGlyph = layout.createSpeciesGlyph();
			speciesGlyph.setId(GlobalConstants.GLYPH+"__"+id);
			speciesGlyph.setSpeciesId(id);
		}
		speciesGlyph.getBoundingBox().setX(x);
		speciesGlyph.getBoundingBox().setY(y);
		speciesGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_VARIABLE_WIDTH);
		speciesGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_VARIABLE_HEIGHT);
		TextGlyph textGlyph = layout.createTextGlyph();
		textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
		textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
		textGlyph.setText(id);
		textGlyph.setBoundingBox(speciesGlyph.getBoundingBox());

		return id;
	}
	
	public String createCompartment(String id, float x, float y) {
		if (!checkCompartmentLocation(id,(double)x,(double)y,
				(double)GlobalConstants.DEFAULT_COMPARTMENT_WIDTH,(double)GlobalConstants.DEFAULT_COMPARTMENT_HEIGHT)) {
			Utility.createErrorMessage("Compartment Overlap", "Compartments must not overlap.");
			return "";
		}
		Compartment compartment = sbml.getModel().createCompartment();
		if (id == null) {
			do {
				creatingCompartmentID++;
				id = "Comp" + String.valueOf(creatingCompartmentID);
			}
			while ((sbml.getElementBySId(id)!=null)||(sbml.getElementByMetaId(id)!=null));
		}
		compartment.setId(id);
		// Set default promoter metaID
		metaIDIndex = SBMLutilities.setDefaultMetaID(sbml, compartment, metaIDIndex); 
		compartment.setConstant(true);
		compartment.setSize(1);
		compartment.setSpatialDimensions(3);

		if (!isGridEnabled()) {
			Layout layout = null;
			if (sbmlLayout.getLayout("iBioSim") != null) {
				layout = sbmlLayout.getLayout("iBioSim"); 
			} else {
				layout = sbmlLayout.createLayout();
				layout.setId("iBioSim");
			}
			CompartmentGlyph compartmentGlyph = null;
			if (layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+id)!=null) {
				compartmentGlyph = layout.getCompartmentGlyph(GlobalConstants.GLYPH+"__"+id);
			} else {
				compartmentGlyph = layout.createCompartmentGlyph();
				compartmentGlyph.setId(GlobalConstants.GLYPH+"__"+id);
				compartmentGlyph.setCompartmentId(id);
			}
			compartmentGlyph.getBoundingBox().setX(x);
			compartmentGlyph.getBoundingBox().setY(y);
			compartmentGlyph.getBoundingBox().setWidth(GlobalConstants.DEFAULT_COMPARTMENT_WIDTH);
			compartmentGlyph.getBoundingBox().setHeight(GlobalConstants.DEFAULT_COMPARTMENT_HEIGHT);
			TextGlyph textGlyph = layout.createTextGlyph();
			textGlyph.setId(GlobalConstants.TEXT_GLYPH+"__"+id);
			textGlyph.setGraphicalObjectId(GlobalConstants.GLYPH+"__"+id);
			textGlyph.setText(id);
			textGlyph.setBoundingBox(compartmentGlyph.getBoundingBox());
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
		if (promoterId != null && sbmlLayout.getLayout("iBioSim").getSpeciesGlyph(GlobalConstants.GLYPH+"__"+promoterId)!=null)
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

	public void setParameter(String parameter, String value, String sweep) {
		//globalParameters.put(parameter, value);
		if (sbml != null) { 
			if (value.startsWith("(")) {
				sbml.getModel().getParameter(parameter).setAnnotation(value);
			} else {
				sbml.getModel().getParameter(parameter).setValue(Double.parseDouble(value));
			}
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

	private void loadDefaultEnclosingCompartment() {
		if (sbml != null) {
			if (sbml.getModel().getNumCompartments()==0) {
				Compartment c = sbml.getModel().createCompartment();
				c.setId("Cell");
				c.setSize(1);
				c.setSpatialDimensions(3);
				c.setConstant(true);
				return;
			}
			for (long i = 0; i < sbml.getModel().getNumCompartments(); i++) {
				Compartment compartment = sbml.getModel().getCompartment(i);
				if (compartment.isSetAnnotation() &&
					(compartment.getAnnotation().toXMLString().contains("EnclosingCompartment"))) {
					compartment.unsetAnnotation();
					return;
				} 
			}
			Port port = sbmlCompModel.getPort(GlobalConstants.ENCLOSING_COMPARTMENT);
			if (port!=null) {
				port.removeFromParentAndDelete();
				return;
			} 
			port = sbmlCompModel.getPort(GlobalConstants.DEFAULT_COMPARTMENT);
			if (port!=null) {
				port.setId(GlobalConstants.COMPARTMENT + "__" + port.getIdRef());
				return;
			} 
		}
	}

	/*
	private void updateCompartmentReplacements() {
		Compartment compartment = sbml.getModel().getCompartment(defaultCompartment); //port.getIdRef());
		CompSBasePlugin sbmlSBase = (CompSBasePlugin)compartment.getPlugin("comp");
		long l = 0;
		while (l < sbmlSBase.getNumReplacedElements()) {
			ReplacedElement replacement = sbmlSBase.getReplacedElement(l);
			if (replacement.isSetPortRef() && 
					(replacement.getPortRef().equals(GlobalConstants.ENCLOSING_COMPARTMENT) ||
					replacement.getPortRef().equals(GlobalConstants.DEFAULT_COMPARTMENT) ||
					replacement.getPortRef().startsWith(GlobalConstants.COMPARTMENT+"__"))) {
				sbmlSBase.removeReplacedElement(l);
			} else {
				l++;
			}
		}
		for (long i=0; i<sbmlCompModel.getNumSubmodels(); i++) {
			Submodel submodel = sbmlCompModel.getSubmodel(i);
			ExternalModelDefinition extModelDefn = sbmlComp.getExternalModelDefinition(submodel.getModelRef());
			String extModelFile = extModelDefn.getSource().replace("file:","");
			//SBMLDocument subDocument = Gui.readSBML(path + separator + extModelFile);
			BioModel subBioModel = new BioModel(path);
			subBioModel.load(extModelFile);
			SBMLDocument subDocument = subBioModel.getSBMLDocument();
			if (subDocument != null) {
				CompModelPlugin subSbmlCompModel = subBioModel.getSBMLCompModel();
				if (subSbmlCompModel.getPort(GlobalConstants.ENCLOSING_COMPARTMENT)==null) {
					for (long j = 0; j < subSbmlCompModel.getNumPorts(); j++) {
						Port compPort = subSbmlCompModel.getPort(j);
						if (compPort.getId().startsWith(GlobalConstants.COMPARTMENT+"__")||
								compPort.getId().startsWith(GlobalConstants.ENCLOSING_COMPARTMENT)||
								compPort.getId().startsWith(GlobalConstants.DEFAULT_COMPARTMENT)) {
							ReplacedElement replacement = sbmlSBase.createReplacedElement();
							replacement.setSubmodelRef(submodel.getId());
							replacement.setPortRef(compPort.getId());
						}
					}
				}
			}
		}
	}
	*/
	
	private void updatePorts() {
		int j = 0;
		while (j < sbmlCompModel.getNumPorts()) {
			Port port = sbmlCompModel.getPort(j);
			if (port.isSetSBaseRef()) {
				port.removeFromParentAndDelete();
			} else {
				j++;
			}
		}
		if (!this.isGridEnabled()) {
			for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
				Submodel submodel = sbmlCompModel.getSubmodel(i);
				BioModel subBioModel = new BioModel(path);		
				String extModelFile = sbmlComp.getExternalModelDefinition(submodel.getModelRef())
						.getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
				subBioModel.load(path + separator + extModelFile);
				for (j = 0; j < subBioModel.getSBMLCompModel().getNumPorts(); j++) {
					Port subPort = subBioModel.getSBMLCompModel().getPort(j);
					Port port = sbmlCompModel.createPort();
					port.setId(subPort.getId()+"__"+submodel.getId());
					port.setIdRef(submodel.getId());
					SBaseRef sbaseRef = port.createSBaseRef();
					sbaseRef.setPortRef(subPort.getId());
				}
			}
		}
	}

	private void loadSBMLFile(String sbmlFile) {
		if (!sbmlFile.equals("")) {
			if (new File(path + separator + sbmlFile).exists()) {
				sbml = Gui.readSBML(path + separator + sbmlFile);
				sbml.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
				sbml.setPackageRequired("layout", false); 
				sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
				sbml.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
				sbml.setPackageRequired("comp", true); 
				((CompSBMLDocumentPlugin)sbml.getPlugin("comp")).setRequired(true);
				sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
				sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
			} else {
				createSBMLDocument(sbmlFile.replace(".xml",""),false);
			}
		} 
		loadDefaultParameters();
		loadDefaultEnclosingCompartment();
		//updateCompartmentReplacements();
		SBMLutilities.fillBlankMetaIDs(sbml);
		loadGridSize();
		updatePorts();
		
		for (int i = 0; i < sbml.getModel().getNumParameters(); ++i)
			if (sbml.getModel().getParameter(i).getId().contains("__locations")) {
				updateGridSpecies(sbml.getModel().getParameter(i).getId().replace("__locations",""));
			}
	}

	private void loadSBMLFromBuffer(StringBuffer buffer) {	
		SBMLReader reader = new SBMLReader();
		sbml = reader.readSBMLFromString(buffer.toString());
		sbmlLayout = (LayoutModelPlugin)sbml.getModel().getPlugin("layout");
		sbmlComp = (CompSBMLDocumentPlugin)sbml.getPlugin("comp");
		sbmlCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		loadDefaultEnclosingCompartment();
		loadGridSize();
	}
	
	public void recurseExportSingleFile(ArrayList<String> comps,CompModelPlugin subCompModel,CompSBMLDocumentPlugin subComp,
				CompSBMLDocumentPlugin documentComp) {
		for (long i = 0; i < subCompModel.getNumSubmodels(); i++) {
			String subModelId = subCompModel.getSubmodel(i).getId();
			String extModel = subComp.getExternalModelDefinition(subCompModel.getSubmodel(subModelId)
					.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			if (!comps.contains((String)extModel)) {
				comps.add(extModel);
				SBMLDocument subDocument = Gui.readSBML(path + separator + extModel);
				ModelDefinition md = new ModelDefinition(subDocument.getModel());
				String id = subDocument.getModel().getId();
				SBaseList elements = subDocument.getListOfAllElements();
				for (long j = 0; j < elements.getSize(); j++) {
					SBase sbase = elements.get(j);
					sbase.setMetaId(id+"__"+sbase.getMetaId());
				}
				documentComp.addModelDefinition(md);
				recurseExportSingleFile(comps,(CompModelPlugin)subDocument.getModel().getPlugin("comp"),
						(CompSBMLDocumentPlugin)subDocument.getPlugin("comp"),documentComp);
			}
		}
	}
	
	public void exportSingleFile(String exportFile) {
		ArrayList<String> comps = new ArrayList<String>();
		SBMLDocument document = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		document.setModel(sbml.getModel());
		document.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
		document.setPackageRequired("layout", false); 
		//LayoutModelPlugin documentLayout = (LayoutModelPlugin)document.getModel().getPlugin("layout");
		document.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
		document.setPackageRequired("comp", true);
		((CompSBMLDocumentPlugin)document.getPlugin("comp")).setRequired(true);
		CompSBMLDocumentPlugin documentComp = (CompSBMLDocumentPlugin)document.getPlugin("comp");
		//CompModelPlugin documentCompModel = (CompModelPlugin)document.getModel().getPlugin("comp");
		for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
			String subModelId = sbmlCompModel.getSubmodel(i).getId();
			String extModel = sbmlComp.getExternalModelDefinition(sbmlCompModel.getSubmodel(subModelId)
					.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			if (!comps.contains((String)extModel)) {
				comps.add(extModel);
				SBMLDocument subDocument = Gui.readSBML(path + separator + extModel);
				String id = subDocument.getModel().getId();
				SBaseList elements = subDocument.getListOfAllElements();
				for (long j = 0; j < elements.getSize(); j++) {
					SBase sbase = elements.get(j);
					sbase.setMetaId(id+"__"+sbase.getMetaId());
				}
				ModelDefinition md = new ModelDefinition(subDocument.getModel());
				documentComp.addModelDefinition(md);
				recurseExportSingleFile(comps,(CompModelPlugin)subDocument.getModel().getPlugin("comp"),
						(CompSBMLDocumentPlugin)subDocument.getPlugin("comp"),documentComp);
			}
		}
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBML(document, exportFile);
	}
	
	private ArrayList<String> getListOfSubmodels() {
		ArrayList<String> comps = new ArrayList<String>();

		if (this.getGridEnabledFromFile(filename.replace(".gcm",".xml"))) {
			
			//look through the location parameter arrays
			for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter, loop through the annotation and collect submodel IDs
				if (parameter.getId().contains("__locations")) {
					
					String[] splitAnnotation = parameter.getAnnotationString().replace("<annotation>","")
					.replace("</annotation>","").replace("\"","").replace("http://www.fakeuri.com","").replace("/>","").split("array:");
					
					//find all components in the annotation
					for (int j = 2; j < splitAnnotation.length; ++j) {
							
						comps.add(splitAnnotation[j].split("=")[0].trim());
					}
				}	
			}
		}
		else {
			
			for (long i = 0; i < sbmlCompModel.getNumSubmodels(); i++) {
				comps.add(sbmlCompModel.getSubmodel(i).getId());
			}
		}
		return comps;
	}
	
	private String getExtModelFileName(String s) {
		
		String extModel;
		String componentModelRef = "";
		
		if (this.getGridEnabledFromFile(filename.replace(".gcm",".xml"))) {
		
			//look through the location parameter arrays to find the correct model ref
			for (int i = 0; i < sbml.getModel().getNumParameters(); ++i) {
				
				Parameter parameter = sbml.getModel().getParameter(i);
				
				//if it's a location parameter
				if (parameter.getId().contains("__locations")) {
					
					if (parameter.getAnnotationString().contains("array:" + s + "=")) {
						
						componentModelRef = parameter.getId().replace("__locations","");
						break;
					}					
				}				
			}
			
			extModel = componentModelRef + ".xml";
		}
		else {

			// load the component's gcm into a new GCMFile
			extModel = sbmlComp.getExternalModelDefinition(sbmlCompModel.getSubmodel(s)
					.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
		}	
		
		return extModel;
	}
	
	private boolean checkModelConsistency(SBMLDocument document) {
		if (document != null) {
			document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
			document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
			document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
			document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
			document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
			document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
			document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
			long numErrors = document.checkConsistency();
			if (numErrors > 0) {
				Utility.createErrorMessage("Merged SBMLs Are Inconsistent", "The merged sbml files have inconsistencies.");
				String message = "";
				for (long i = 0; i < numErrors; i++) {
					String error = document.getError(i).getMessage(); // .replace(". ",
					// ".\n");
					message += i + ":" + error + "\n";
				}
				if (numErrors > 0) {
					JTextArea messageArea = new JTextArea(message);
					messageArea.setLineWrap(true);
					messageArea.setEditable(false);
					JScrollPane scroll = new JScrollPane();
					scroll.setMinimumSize(new Dimension(600, 600));
					scroll.setPreferredSize(new Dimension(600, 600));
					scroll.setViewportView(messageArea);
					JOptionPane.showMessageDialog(Gui.frame, scroll, "SBML Errors and Warnings", JOptionPane.ERROR_MESSAGE);
				}
				return false;
			}
		}
		return true;
	}
	
	public SBMLDocument newFlattenModel() {
		SBMLDocument document = new SBMLDocument();
		document.setModel(sbmlCompModel.flattenModel());
		document.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", true);
		document.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", true);
		//CompModelPlugin documentCompModel = (CompModelPlugin)sbml.getModel().getPlugin("comp");
		//while (documentCompModel.getNumPorts()>0) {
		//	documentCompModel.getPort(0).removeFromParentAndDelete();
		//}
		document.enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", false);
		document.enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", false);
		SBMLWriter writer = new SBMLWriter();
		writer.writeSBML(document, path + separator + "_temp.xml");
		return document;
	}
	
	public SBMLDocument flattenModel() {
		ArrayList<String> modelList = new ArrayList<String>();
		modelList.add(filename);
		String tempFile = filename.replace(".gcm","").replace(".xml","")+"_temp.xml";
		save(tempFile);
		ArrayList<String> comps = getListOfSubmodels();
		
		BioModel model = new BioModel(path);
		model.load(tempFile);

		// loop through the list of submodels
		for (String subModelId : comps) {
			BioModel subModel = new BioModel(path);		
			String extModelFile = getExtModelFileName(subModelId);
			subModel.load(path + separator + extModelFile);
			ArrayList<String> modelListCopy = copyArray(modelList);
			if (modelListCopy.contains(subModel.getFilename())) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				load(tempFile);
				new File(tempFile).delete();
				return null;
			}
			modelListCopy.add(subModel.getFilename());

			// recursively add this component's sbml (and its inside components'
			// sbml, etc.) to the overall sbml
			unionSBML(model, flattenModelRecurse(subModel, subModelId, modelListCopy), subModelId, 
					subModel.getParameter(GlobalConstants.RNAP_STRING));
			if (model.getSBMLDocument() == null && modelListCopy.isEmpty()) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				load(tempFile);
				new File(tempFile).delete();
				return null;
			}
			else if (model.getSBMLDocument() == null) {
				Utility.createErrorMessage("Cannot Flatten Model", "Unable to flatten sbml files from components.");
				load(tempFile);
				new File(tempFile).delete();
				return null;
			}
		}
		model.getSBMLDocument().enablePackage(LayoutExtension.getXmlnsL3V1V1(), "layout", false);
		model.getSBMLDocument().enablePackage(CompExtension.getXmlnsL3V1V1(), "comp", false);
		checkModelConsistency(model.getSBMLDocument());
		new File(tempFile).delete();
		return model.getSBMLDocument();
	}
	
	
	public SBMLDocument flattenBioModel() {
		ArrayList<String> modelList = new ArrayList<String>();
		modelList.add(filename);
		ArrayList<String> comps = getListOfSubmodels();

		// loop through the list of submodels
		for (String subModelId : comps) {
			BioModel subModel = new BioModel(path);		
			String extModelFile = getExtModelFileName(subModelId);
			subModel.load(path + separator + extModelFile);
			ArrayList<String> modelListCopy = copyArray(modelList);
			if (modelListCopy.contains(subModel.getFilename())) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				return null;
			}
			modelListCopy.add(subModel.getFilename());

			// recursively add this component's sbml (and its inside components'
			// sbml, etc.) to the overall sbml
			unionSBML(this, flattenModelRecurse(subModel, subModelId, modelListCopy), subModelId, 
					subModel.getParameter(GlobalConstants.RNAP_STRING));
			if (this.getSBMLDocument() == null && modelListCopy.isEmpty()) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten model.\n" + "There is a loop in the components.");
				return null;
			}
			else if (this.getSBMLDocument() == null) {
				Utility.createErrorMessage("Cannot Flatten Model", "Unable to flatten sbml files from components.");
				return null;
			}
		}
		checkModelConsistency(this.getSBMLDocument());
		return this.getSBMLDocument();
	}
	
	private BioModel flattenModelRecurse(BioModel model, String modelId, ArrayList<String> modelList) {
		ArrayList<String> comps = new ArrayList<String>();
		
		for (long i = 0; i < model.getSBMLCompModel().getNumSubmodels(); i++) {
			comps.add(model.getSBMLCompModel().getSubmodel(i).getId());
		}
		
		for (String s : comps) {
			BioModel subModel = new BioModel(path);
			String extModel = model.getSBMLComp().getExternalModelDefinition(model.getSBMLCompModel().getSubmodel(s)
					.getModelRef()).getSource().replace("file://","").replace("file:","").replace(".gcm",".xml");
			subModel.load(path + separator + extModel);
			ArrayList<String> modelListCopy = copyArray(modelList);
			if (modelListCopy.contains(subModel.getFilename())) {
				while (!modelList.isEmpty()) {
					modelList.remove(0);
				}
				return null;
			}
			modelListCopy.add(subModel.getFilename());
			unionSBML(model, flattenModelRecurse(subModel, s, modelListCopy), s, subModel.getParameter(GlobalConstants.RNAP_STRING));
		}
		return model;
	}

	private String buildReplacementId(SBaseRef replacement) {
		String replacementId = replacement.getIdRef();
		for(SBaseRef sBaseRef = replacement.getSBaseRef(); sBaseRef != null;
				sBaseRef = sBaseRef.getSBaseRef()) {
			replacementId = replacementId + "__" + sBaseRef.getIdRef();
		}
		return replacementId;
	}
	
	private void performDeletions(BioModel bioModel, BioModel subBioModel, String subModelId) {

		SBMLDocument subDocument = subBioModel.getSBMLDocument();
		Model subModel = subDocument.getModel();
		
		Submodel instance = bioModel.getSBMLCompModel().getSubmodel(subModelId);			
		
		if (instance == null)
			return;
		
		for (long i = 0; i < instance.getNumDeletions(); i++) {
			Deletion deletion = instance.getDeletion(i);
			if (deletion.isSetPortRef()) {
				Port port = subBioModel.getSBMLCompModel().getPort(deletion.getPortRef());
				if (port!=null) {
					if (port.isSetIdRef()) {
						if (subModel.getElementBySId(port.getIdRef())!=null) {
							SBase sbase =  subModel.getElementBySId(port.getIdRef());
							sbase.removeFromParentAndDelete();
						}
					} else if (port.isSetMetaIdRef()) {
						if (subModel.getElementByMetaId(port.getMetaIdRef())!=null) {
							SBase sbase =  subModel.getElementByMetaId(port.getMetaIdRef());
							sbase.removeFromParentAndDelete();
						}
					} else if (port.isSetUnitRef()) {
						if (subModel.getUnitDefinition(port.getUnitRef())!=null) {
							SBase sbase = subModel.getUnitDefinition(port.getUnitRef());
							sbase.removeFromParentAndDelete();
						}
					}
				}
			} else if (deletion.isSetIdRef()) {
				if (subModel.getElementBySId(deletion.getIdRef())!=null) {
					SBase sbase =  subModel.getElementBySId(deletion.getIdRef());
					sbase.removeFromParentAndDelete();
				}
			} else if (deletion.isSetMetaIdRef()) {
				if (subModel.getElementByMetaId(deletion.getMetaIdRef())!=null) {
					SBase sbase =  subModel.getElementByMetaId(deletion.getMetaIdRef());
					sbase.removeFromParentAndDelete();
				}
			} else if (deletion.isSetUnitRef()) {
				if (subModel.getUnitDefinition(deletion.getUnitRef())!=null) {
					SBase sbase = subModel.getUnitDefinition(deletion.getUnitRef());
					sbase.removeFromParentAndDelete();
				}
			}
		}
	}
	
	private String prepareReplacement(String newName,BioModel subBioModel,String subModelId,String replacementModelId,
			CompSBasePlugin sbmlSBase,String subId,String id) {

		for (long k = 0; k < sbmlSBase.getNumReplacedElements(); k++) {
			ReplacedElement replacement = sbmlSBase.getReplacedElement(k);
			if (replacement.getSubmodelRef().equals(replacementModelId)) {
				if (replacement.isSetPortRef()) {
					Port port = subBioModel.getSBMLCompModel().getPort(replacement.getPortRef());
					if (port != null && subId.equals(port.getIdRef())) {
						newName = "_" + subModelId + "__" + id;
					}
				} else if (replacement.isSetIdRef()) {
					String replacementId = buildReplacementId(replacement);
					if (subId.equals(replacementId)) {
						newName = "_" + subModelId + "__" + id;
					}
				}
			}
		}
		if (sbmlSBase.isSetReplacedBy()) {
			Replacing replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(replacementModelId)) {
				if (replacement.isSetPortRef()) {
					Port port = subBioModel.getSBMLCompModel().getPort(replacement.getPortRef());
					if (port != null && subId.equals(port.getIdRef())) {
						newName = "__" + subModelId + "__" + id;
					}
				} else if (replacement.isSetIdRef()) {
					String replacementId = buildReplacementId(replacement);
					if (subId.equals(replacementId)) {
						newName = "__" + subModelId + "__" + id;
					}
				}
			}
		}
		return newName;
	}
	
	private BioModel unionSBML(BioModel bioModel, BioModel subBioModel, String subModelId, String RNAPamount) {
		
		SBMLDocument document = bioModel.getSBMLDocument();
		SBMLDocument subDocument = subBioModel.getSBMLDocument();
		subDocument.setNamespaces(document.getNamespaces());

		Model model = document.getModel();
		Model subModel = subDocument.getModel();
		
		String replacementModelId = subModelId;
		if (bioModel.isGridEnabled()) {
			replacementModelId = "GRID__" + subModel.getId();
		} 	

		for (int i = 0; i < bioModel.getSBMLCompModel().getNumPorts(); i++) {
			Port p = bioModel.getSBMLCompModel().getPort(i);
			if (p.isSetIdRef() && p.getIdRef().equals(subModelId) && 
					p.isSetSBaseRef() && p.getSBaseRef().isSetPortRef()) {
				p.unsetIdRef();
				Port subPort = subBioModel.getSBMLCompModel().getPort(p.getSBaseRef().getPortRef());
				if (subPort.isSetIdRef()) {
					p.setIdRef(subModelId + "__" + subPort.getIdRef());
				} else if (subPort.isSetMetaIdRef()) {
					p.setMetaIdRef(subModelId + "__" + subPort.getMetaIdRef());
				}
				p.unsetSBaseRef();
			} 
		}
		
		performDeletions(bioModel,subBioModel,replacementModelId);
		
		// Rename compartment types
		for (int i = 0; i < subModel.getNumCompartmentTypes(); i++) {
			CompartmentType c = subModel.getCompartmentType(i);
			String newName = subModelId + "__" + c.getId();
			updateVarId(false, c.getId(), newName, subBioModel);
			c.setId(newName);
			if (c.isSetMetaId()) c.setMetaId(subModelId + "__" + c.getMetaId());
			if (model.getCompartmentType(c.getId())==null) {
				model.addCompartmentType(c);
			} else {
				// TODO: compartment type not unique
			}
		}
		// Rename species types
		for (int i = 0; i < subModel.getNumSpeciesTypes(); i++) {
			SpeciesType s = subModel.getSpeciesType(i);
			String newName = subModelId + "__" + s.getId();
			updateVarId(false, s.getId(), newName, subBioModel);
			s.setId(newName);
			if (s.isSetMetaId()) s.setMetaId(subModelId + "__" + s.getMetaId());
			if (model.getSpeciesType(s.getId())==null) {
				model.addSpeciesType(s);
			} else {
				// TODO: species type not unique
			}
		}
		// Rename compartments
		for (long i = 0; i < subModel.getNumCompartments(); i++) {
			Compartment c = subModel.getCompartment(i);
			String newName = subModelId + "__" + c.getId();
			for (long j = 0; j < model.getNumCompartments(); j++) {
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)model.getCompartment(j).getPlugin("comp");
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,c.getId(),
						model.getCompartment(j).getId());
			}
			updateVarId(false, c.getId(), newName, subBioModel);
			compartments.remove(c.getId());
			c.setId(newName);
			if (c.isSetMetaId()) c.setMetaId(subModelId + "__" + c.getMetaId());
		}
		for (int i = 0; i < subModel.getNumCompartments(); i++) {
			Compartment c = subModel.getCompartment(i);
			if (c.getId().startsWith("_" + subModelId + "__")) {
				updateVarId(false, c.getId(), c.getId().substring(3 + subModelId.length()), subBioModel);
				compartments.remove(c.getId());
				c.setId(c.getId().substring(3 + subModelId.length()));
			} else if (c.getId().startsWith("__" + subModelId + "__")) {
				String topId = c.getId().substring(4 + subModelId.length());
				updateVarId(false, c.getId(), topId, subBioModel);
				compartments.remove(c.getId());
				c.setId(topId);
				CompSBasePlugin SbmlSBase = (CompSBasePlugin)model.getCompartment(topId).getPlugin("comp");
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (long j = 0; j < SbmlSBase.getNumReplacedElements(); j++) {
					replacements.add(SbmlSBase.getReplacedElement(j));
				}
				model.removeCompartment(topId);
				model.addCompartment(c);
				SbmlSBase = (CompSBasePlugin)model.getSpecies(c.getId()).getPlugin("comp");
				for (ReplacedElement r : replacements) {
					SbmlSBase.addReplacedElement(r);
				}
			} else {
				if (model.getCompartment(c.getId())==null) {
					model.addCompartment(c);
					if (!compartments.containsKey(c.getId())) {
						Properties prop = new Properties();
						prop.put(GlobalConstants.RNAP_STRING,RNAPamount);
						compartments.put(c.getId(),prop);
					}
				} else {
					// TOOD: species not unique
				}
			}
		}
		// Rename species 
		for (long i = 0; i < subModel.getNumSpecies(); i++) {
			Species spec = subModel.getSpecies(i);
			String newName = subModelId + "__" + spec.getId();
			for (long j = 0; j < model.getNumSpecies(); j++) {
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)model.getSpecies(j).getPlugin("comp");
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,spec.getId(),
						model.getSpecies(j).getId());
			}
			if (subBioModel.getPromoters().contains(spec.getId())) {
				subBioModel.changePromoterId(spec.getId(), newName);
			} else {
				subBioModel.changeSpeciesId(spec.getId(), newName);
			}
			if (spec.isSetMetaId()) spec.setMetaId(subModelId + "__" + spec.getMetaId());
		}
		for (int i = 0; i < subModel.getNumSpecies(); i++) {
			Species spec = subModel.getSpecies(i);
			if (spec.getId().startsWith("_" + subModelId + "__")) {
				if (subBioModel.getPromoters().contains(spec.getId())) {
					subBioModel.changePromoterId(spec.getId(), spec.getId().substring(3 + subModelId.length()));
				} else {
					subBioModel.changeSpeciesId(spec.getId(), spec.getId().substring(3 + subModelId.length()));
				}
			} else if (spec.getId().startsWith("__" + subModelId + "__")) {
				String topId = spec.getId().substring(4 + subModelId.length());
				if (subBioModel.getPromoters().contains(spec.getId())) {
					subBioModel.changePromoterId(spec.getId(), topId);
				} else {
					subBioModel.changeSpeciesId(spec.getId(), topId);
				}
				CompSBasePlugin SbmlSBase = (CompSBasePlugin)model.getSpecies(topId).getPlugin("comp");
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (long j = 0; j < SbmlSBase.getNumReplacedElements(); j++) {
					replacements.add(SbmlSBase.getReplacedElement(j));
				}
				model.removeSpecies(topId);
				model.addSpecies(spec);
				SbmlSBase = (CompSBasePlugin)model.getSpecies(spec.getId()).getPlugin("comp");
				for (ReplacedElement r : replacements) {
					SbmlSBase.addReplacedElement(r);
				}
			} else {
				if (model.getSpecies(spec.getId())==null) {
					model.addSpecies(spec);
				} else {
					// TOOD: species not unique
				}
			}
		}
		// Rename parameters
		for (long i = 0; i < subModel.getNumParameters(); i++) {
			Parameter p = subModel.getParameter(i);
			String newName = subModelId + "__" + p.getId();
			for (long j = 0; j < model.getNumParameters(); j++) {
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)model.getParameter(j).getPlugin("comp");
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,p.getId(),
						model.getParameter(j).getId());
			}
			updateVarId(false, p.getId(), newName, subBioModel);
			p.setId(newName);
			if (p.isSetMetaId()) p.setMetaId(subModelId + "__" + p.getMetaId());
		}
		for (int i = 0; i < subModel.getNumParameters(); i++) {
			Parameter p = subModel.getParameter(i);
			if (p.getId().startsWith("_" + subModelId + "__")) {
				updateVarId(false, p.getId(), p.getId().substring(3 + subModelId.length()), subBioModel);
				p.setId(p.getId().substring(3 + subModelId.length()));
			} else if (p.getId().startsWith("__" + subModelId + "__")) {
				String topId = p.getId().substring(4 + subModelId.length());
				updateVarId(false, p.getId(), topId, subBioModel);
				p.setId(topId);
				CompSBasePlugin SbmlSBase = (CompSBasePlugin)model.getParameter(topId).getPlugin("comp");
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (long j = 0; j < SbmlSBase.getNumReplacedElements(); j++) {
					replacements.add(SbmlSBase.getReplacedElement(j));
				}
				model.removeParameter(topId);
				model.addParameter(p);
				SbmlSBase = (CompSBasePlugin)model.getParameter(p.getId()).getPlugin("comp");
				for (ReplacedElement r : replacements) {
					SbmlSBase.addReplacedElement(r);
				}
			} else {
				if (model.getParameter(p.getId())==null) {
					model.addParameter(p);
				} else {
					// TOOD: species not unique
				}
			}
		}

		for (int i = 0; i < subModel.getNumReactions(); i++) {
			Reaction r = subModel.getReaction(i);
			if (r.getId().contains("MembraneDiffusion")) continue;
			String newName = subModelId + "__" + r.getId();
			for (long j = 0; j < model.getNumReactions(); j++) {
				CompSBasePlugin sbmlSBase = (CompSBasePlugin)model.getReaction(j).getPlugin("comp");
				newName = prepareReplacement(newName,subBioModel,subModelId,replacementModelId,sbmlSBase,r.getId(),
						model.getReaction(j).getId());
			}
			updateVarId(false, r.getId(), newName, subBioModel);
			r.setId(newName);
			if (r.isSetMetaId()) r.setMetaId(subModelId + "__" + r.getMetaId());
		}
		for (int i = 0; i < subModel.getNumReactions(); i++) {
			Reaction r = subModel.getReaction(i);
			if (r.getId().startsWith("_" + subModelId + "__")) {
				updateVarId(false, r.getId(), r.getId().substring(3 + subModelId.length()), subBioModel);
				r.setId(r.getId().substring(3 + subModelId.length()));
			} else if (r.getId().startsWith("__" + subModelId + "__")) {
				String topId = r.getId().substring(3 + subModelId.length());
				updateVarId(false, r.getId(), topId, subBioModel);
				r.setId(topId);
				CompSBasePlugin SbmlSBase = (CompSBasePlugin)model.getReaction(topId).getPlugin("comp");
				ArrayList<ReplacedElement> replacements = new ArrayList<ReplacedElement>();
				for (long j = 0; j < SbmlSBase.getNumReplacedElements(); j++) {
					replacements.add(SbmlSBase.getReplacedElement(j));
				}
				model.removeReaction(topId);
				model.addReaction(r);
				SbmlSBase = (CompSBasePlugin)model.getParameter(r.getId()).getPlugin("comp");
				for (ReplacedElement repl : replacements) {
					SbmlSBase.addReplacedElement(repl);
				}
			} else {
				if (model.getReaction(r.getId())==null) {
					model.addReaction(r);
				} else {
					// TOOD: reaction not unique
				}
			}
		}
		for (int i = 0; i < subModel.getNumInitialAssignments(); i++) {
			InitialAssignment init = (InitialAssignment) subModel.getListOfInitialAssignments().get(i);
			if (init.isSetMetaId()) {
				init.setMetaId(subModelId + "__" + init.getMetaId());
			}
			model.addInitialAssignment(init);
		}
		for (int i = 0; i < subModel.getNumRules(); i++) {
			org.sbml.libsbml.Rule r = subModel.getRule(i);
			if (r.isSetMetaId()) {
				r.setMetaId(subModelId + "__" + r.getMetaId());
			}
			model.addRule(r);
		}
		for (int i = 0; i < subModel.getNumConstraints(); i++) {
			Constraint constraint = (Constraint) subModel.getListOfConstraints().get(i);
			String newName = subModelId + "__" + constraint.getMetaId();
			constraint.setMetaId(newName);
			for (int j = 0; j < model.getNumConstraints(); j++) {
				if (model.getConstraint(j).getMetaId().equals(constraint.getMetaId())) {
					Constraint c = model.getConstraint(j);
					if (!c.getMessageString().equals(constraint.getMessageString())) {
						return null;
					}
					if (c.getMath() != constraint.getMath()) {
						return null;
					}
				}
			}
			model.addConstraint(constraint);
		}
		
		for (int i = 0; i < subModel.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) subModel.getListOfEvents().get(i);
			
			if (event.getAnnotationString().length() > 0 && (
					event.getAnnotationString().contains("Division") ||
					event.getAnnotationString().contains("Death") ||
					event.getAnnotationString().contains("Move")))
				continue;
			
			String newName = subModelId + "__" + event.getId();
			updateVarId(false, event.getId(), newName, subBioModel);
			event.setId(newName);
			if (event.isSetMetaId()) {
				event.setMetaId(subModelId + "__" + event.getMetaId());
			}
			for (int j = 0; j < model.getNumEvents(); j++) {
				if (model.getEvent(j).getId().equals(event.getId())) {
					org.sbml.libsbml.Event e = model.getEvent(j);
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
			model.addEvent(event);
		}
		
		for (int i = 0; i < subModel.getNumUnitDefinitions(); i++) {
			UnitDefinition u = subModel.getUnitDefinition(i);
			String newName = u.getId();
			boolean add = true;
			for (int j = 0; j < model.getNumUnitDefinitions(); j++) {
				if (model.getUnitDefinition(j).getId().equals(u.getId())) {
					if (UnitDefinition.areIdentical(model.getUnitDefinition(j), u)) {
						add = false;
					}
					else {
						newName = subModelId + "__" + u.getId();
					}
				}
			}
			if (add) {
				u.setId(newName);
				if (u.isSetMetaId()) {
					u.setMetaId(subModelId + "__" + u.getMetaId());
				}
				model.addUnitDefinition(u);
			}
		}
		

		for (int i = 0; i < subModel.getNumFunctionDefinitions(); i++) {
			FunctionDefinition f = subModel.getFunctionDefinition(i);
			boolean add = true;
			for (int j = 0; j < model.getNumFunctionDefinitions(); j++) {
				if (model.getFunctionDefinition(j).getId().equals(f.getId())) {
					add = false;
				}
			}
			if (add) {
				if (f.isSetMetaId()) {
					f.setMetaId(subModelId + "__" + f.getMetaId());
				}
				model.addFunctionDefinition(f);
			}
		}
		
		return bioModel;
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

	private void updateVarId(boolean isSpecies, String origId, String newId, BioModel bioModel) {
		SBMLDocument document = bioModel.getSBMLDocument();
		if (origId.equals(newId))
			return;
		Model model = document.getModel();
		for (long i = 0; i < bioModel.getSBMLCompModel().getNumPorts(); i++) {
			Port port = bioModel.getSBMLCompModel().getPort(i);
			if (port.isSetIdRef() && port.getIdRef().equals(origId)) {
				port.setIdRef(newId);
			}
		}
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = model.getSpecies(i);
			if (species.getCompartment().equals(origId)) {
				species.setCompartment(newId);
			}
			if (species.getSpeciesType().equals(origId)) {
				species.setSpeciesType(newId);
			}
		}
		for (int i = 0; i < model.getNumCompartments(); i++) {
			Compartment compartment = (Compartment) model.getListOfCompartments().get(i);
			if (compartment.getCompartmentType().equals(origId)) {
				compartment.setCompartmentType(newId);
			}
		}
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) model.getListOfReactions().get(i);
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
			if (reaction.isSetKineticLaw()) {
				reaction.getKineticLaw().setMath(
						updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
			}
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
	
	public boolean isGridEnabled() {
		if ((getGrid().getNumRows() > 0) && (getGrid().getNumCols() > 0)) return true;
		return false;
	}
	
	/**
	 * looks in the file to see if it is a gridded file
	 * 
	 * @param filename
	 * @return
	 */
	public boolean getGridEnabledFromFile(String filename) {
		
		BioModel subModel = new BioModel(path);
		subModel.load(filename);
		if ((subModel.getGrid().getNumRows() > 0) || (subModel.getGrid().getNumCols() > 0)) return true;
		return false;

		/*
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
		
		//grid=(0,0) means there's no grid
		if (data.toString().contains("grid=(0,0)") == false)
			return true;
		else
			return false;
			*/
	}
	
	private SBMLDocument sbml = null;
	
	private LayoutModelPlugin sbmlLayout = null;
	
	private CompSBMLDocumentPlugin sbmlComp = null;
	
	private CompModelPlugin sbmlCompModel = null;
	
	private MySpecies speciesPanel = null;
	
	private Reactions reactionPanel = null;
	
	private Rules rulePanel = null;
	
	private Constraints constraintPanel = null;
	
	private Events eventPanel = null;
	
	private Parameters parameterPanel = null;
	
	private Grid grid = null;
	
	private int creatingCompartmentID = 0;
	private int creatingVariableID = 0;
	private int creatingPromoterID = 0;
	private int creatingSpeciesID = 0;
	private int metaIDIndex = 1;
	private int creatingReactionID = 0;
	
	private String path;

	private String sbmlFile = "";
	
	//private GCM2SBML gcm2sbml = null;

	private HashMap<String, Properties> compartments;
}
