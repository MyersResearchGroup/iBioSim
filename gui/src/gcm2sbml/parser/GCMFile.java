package gcm2sbml.parser;

import gcm2sbml.util.GlobalConstants;
import gcm2sbml.util.Utility;

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
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxCompactTreeLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.view.mxEdgeStyle;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxLayoutManager;

import lhpn2sbml.parser.ExprTree;
import lhpn2sbml.parser.LhpnFile;

import biomodelsim.BioSim;
import biomodelsim.Log;
import buttons.Buttons;

/**
 * This class describes a GCM file
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class GCMFile {
	
	private String separator;
	
	private String filename;

	public GCMFile(String path) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.path = path;
		species = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		loadDefaultParameters();
	}

	public String getSBMLFile() {
		return sbmlFile;
	}

	public void setSBMLFile(String file) {
		sbmlFile = file;
	}

	public boolean getDimAbs() {
		return dimAbs;
	}

	public void setDimAbs(boolean dimAbs) {
		this.dimAbs = dimAbs;
	}

	public boolean getBioAbs() {
		return bioAbs;
	}

	public void setBioAbs(boolean bioAbs) {
		this.bioAbs = bioAbs;
	}

	public void createLogicalModel(final String filename, final Log log, final BioSim biosim,
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
		for (String spec : species.keySet()) {
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
							Object[] sort = Buttons.add(conLevel.get(number),
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
					conLevel.set(number, Buttons
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
				flattenGCM(false);
				convertToLHPN(specs, conLevel).save(filename);
				log.addText("Saving GCM file as LPN:\n" + path + separator + lpnName + "\n");
				biosim.refreshTree();
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

	public SBMLDocument flattenGCM(boolean includeSBML) {
		save(filename + ".temp");
		ArrayList<String> comps = setToArrayList(components.keySet());
		SBMLDocument sbml = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		Model m = sbml.createModel();
		sbml.setModel(m);
		Utility.addCompartments(sbml, "default");
		sbml.getModel().getCompartment("default").setSize(1);
		if (!sbmlFile.equals("") && includeSBML) {
			sbml = BioSim.readSBML(path + separator + sbmlFile);
		}
		else if (!sbmlFile.equals("") && !includeSBML) {
			Utility.createErrorMessage("SBMLs Included",
					"There are sbml files associated with the gcm file and its components.");
			load(filename + ".temp");
			return null;
		}
		for (String s : comps) {
			GCMFile file = new GCMFile(path);
			file.load(path + separator + components.get(s).getProperty("gcm"));
			for (String p : globalParameters.keySet()) {
				if (!file.globalParameters.containsKey(p)) {
					file.setParameter(p, globalParameters.get(p));
				}
			}
			sbml = unionSBML(sbml, unionGCM(this, file, s, includeSBML), s);
			if (sbml == null && includeSBML) {
				Utility.createErrorMessage("Cannot Merge SBMLs", "Unable to merge sbml files from components.");
				load(filename + ".temp");
				return null;
			}
			else if (sbml == null && !includeSBML) {
				Utility.createErrorMessage("SBMLs Included",
						"There are sbml files associated with the gcm file and its components.");
				load(filename + ".temp");
				return null;
			}
		}
		components = new HashMap<String, Properties>();
		if (sbml != null) {
			long numErrors = sbml.checkConsistency();
			if (numErrors > 0) {
				Utility.createErrorMessage("Merged SBMLs Are Inconsistent", "The merged sbml files have inconsistencies.");
			}
		}
		return sbml;
	}

	private SBMLDocument unionGCM(GCMFile topLevel, GCMFile bottomLevel, String compName, boolean includeSBML) {
		ArrayList<String> mod = setToArrayList(bottomLevel.components.keySet());
		SBMLDocument sbml = new SBMLDocument(BioSim.SBML_LEVEL, BioSim.SBML_VERSION);
		Model m = sbml.createModel();
		sbml.setModel(m);
		if (!bottomLevel.sbmlFile.equals("") && includeSBML) {
			sbml = BioSim.readSBML(path + separator + bottomLevel.sbmlFile);
		}
		else if (!bottomLevel.sbmlFile.equals("") && !includeSBML) {
			return null;
		}
		for (String s : mod) {
			GCMFile file = new GCMFile(path);
			file.load(path + separator + bottomLevel.components.get(s).getProperty("gcm"));
			for (String p : bottomLevel.globalParameters.keySet()) {
				if (!file.globalParameters.containsKey(p)) {
					file.setParameter(p, bottomLevel.globalParameters.get(p));
				}
			}
			sbml = unionSBML(sbml, unionGCM(bottomLevel, file, s, includeSBML), s);
			if (sbml == null) {
				return null;
			}
		}
		mod = setToArrayList(bottomLevel.promoters.keySet());
		for (String prom : mod) {
			bottomLevel.promoters.get(prom).put(GlobalConstants.ID, compName + "_" + prom);
			bottomLevel.changePromoterName(prom, compName + "_" + prom);
		}
		mod = setToArrayList(bottomLevel.species.keySet());
		for (String spec : mod) {
			bottomLevel.species.get(spec).put(GlobalConstants.ID, compName + "_" + spec);
			bottomLevel.changeSpeciesName(spec, compName + "_" + spec);
		}
		mod = setToArrayList(bottomLevel.species.keySet());
		for (String spec : mod) {
			for (Object port : topLevel.components.get(compName).keySet()) {
				if (spec.equals(compName + "_" + port)) {
					bottomLevel.species.get(spec).put(GlobalConstants.ID,
							topLevel.components.get(compName).getProperty((String) port));
					bottomLevel.changeSpeciesName(spec, topLevel.components.get(compName).getProperty((String) port));
				}
			}
		}
		for (String param : bottomLevel.globalParameters.keySet()) {
			if (param.equals(GlobalConstants.KDECAY_STRING)) {
				mod = setToArrayList(bottomLevel.species.keySet());
				for (String spec : mod) {
					if (!bottomLevel.species.get(spec).containsKey(GlobalConstants.KDECAY_STRING)) {
						bottomLevel.species.get(spec).put(GlobalConstants.KDECAY_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KDECAY_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.KASSOCIATION_STRING)) {
				mod = setToArrayList(bottomLevel.influences.keySet());
				for (String infl : mod) {
					if (!bottomLevel.influences.get(infl).containsKey(GlobalConstants.KASSOCIATION_STRING)) {
						bottomLevel.influences.get(infl).put(GlobalConstants.KASSOCIATION_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KASSOCIATION_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.KBIO_STRING)) {
				mod = setToArrayList(bottomLevel.influences.keySet());
				for (String infl : mod) {
					if (!bottomLevel.influences.get(infl).containsKey(GlobalConstants.KBIO_STRING)) {
						bottomLevel.influences.get(infl).put(GlobalConstants.KBIO_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KBIO_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.COOPERATIVITY_STRING)) {
				mod = setToArrayList(bottomLevel.influences.keySet());
				for (String infl : mod) {
					if (!bottomLevel.influences.get(infl).containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
						bottomLevel.influences.get(infl).put(GlobalConstants.COOPERATIVITY_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.COOPERATIVITY_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.KREP_STRING)) {
				mod = setToArrayList(bottomLevel.influences.keySet());
				for (String infl : mod) {
					if (!bottomLevel.influences.get(infl).containsKey(GlobalConstants.KREP_STRING)) {
						bottomLevel.influences.get(infl).put(GlobalConstants.KREP_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KREP_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.KACT_STRING)) {
				mod = setToArrayList(bottomLevel.influences.keySet());
				for (String infl : mod) {
					if (!bottomLevel.influences.get(infl).containsKey(GlobalConstants.KACT_STRING)) {
						bottomLevel.influences.get(infl).put(GlobalConstants.KACT_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KACT_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.RNAP_BINDING_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.RNAP_BINDING_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.RNAP_BINDING_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.RNAP_BINDING_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.OCR_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.OCR_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.OCR_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.OCR_STRING));
					}
				}

			}
			else if (param.equals(GlobalConstants.KBASAL_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.KBASAL_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.KBASAL_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KBASAL_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.PROMOTER_COUNT_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.PROMOTER_COUNT_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.PROMOTER_COUNT_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.STOICHIOMETRY_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.STOICHIOMETRY_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.STOICHIOMETRY_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.STOICHIOMETRY_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.ACTIVED_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.ACTIVED_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.ACTIVED_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.ACTIVED_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.MAX_DIMER_STRING)) {
				mod = setToArrayList(bottomLevel.species.keySet());
				for (String spec : mod) {
					if (!bottomLevel.species.get(spec).containsKey(GlobalConstants.MAX_DIMER_STRING)) {
						bottomLevel.species.get(spec).put(GlobalConstants.MAX_DIMER_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.MAX_DIMER_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.INITIAL_STRING)) {
				mod = setToArrayList(bottomLevel.species.keySet());
				for (String spec : mod) {
					if (!bottomLevel.species.get(spec).containsKey(GlobalConstants.INITIAL_STRING)) {
						bottomLevel.species.get(spec).put(GlobalConstants.INITIAL_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.INITIAL_STRING));
					}
				}
			}
		}
		for (String prom : bottomLevel.promoters.keySet()) {
			topLevel.addPromoter(prom, bottomLevel.promoters.get(prom));
		}
		for (String spec : bottomLevel.species.keySet()) {
			if (!topLevel.species.keySet().contains(spec)) {
				topLevel.addSpecies(spec, bottomLevel.species.get(spec));
			}
		}
		for (String infl : bottomLevel.influences.keySet()) {
			topLevel.addInfluences(infl, bottomLevel.influences.get(infl));
		}
		for (String cond : bottomLevel.conditions) {
			topLevel.addCondition(cond);
		}
		return sbml;
	}

	public LhpnFile convertToLHPN(ArrayList<String> specs, ArrayList<Object[]> conLevel) {
		HashMap<String, ArrayList<String>> infl = new HashMap<String, ArrayList<String>>();
		for (String influence : influences.keySet()) {
			if (influences.get(influence).get(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
				String input = getInput(influence);
				String output = getOutput(influence);
				if (influences.get(influence).containsKey(GlobalConstants.BIO)
						&& influences.get(influence).get(GlobalConstants.BIO).equals("yes")) {
					if (infl.containsKey(output)) {
						infl.get(output).add("bioAct:" + input + ":" + influence);
					}
					else {
						ArrayList<String> out = new ArrayList<String>();
						out.add("bioAct:" + input + ":" + influence);
						infl.put(output, out);
					}
				}
				else {
					if (infl.containsKey(output)) {
						infl.get(output).add("act:" + input + ":" + influence);
					}
					else {
						ArrayList<String> out = new ArrayList<String>();
						out.add("act:" + input + ":" + influence);
						infl.put(output, out);
					}
				}
			}
			else if (influences.get(influence).get(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION)) {
				String input = getInput(influence);
				String output = getOutput(influence);
				if (influences.get(influence).containsKey(GlobalConstants.BIO)
						&& influences.get(influence).get(GlobalConstants.BIO).equals("yes")) {
					if (infl.containsKey(output)) {
						infl.get(output).add("bioRep:" + input + ":" + influence);
					}
					else {
						ArrayList<String> out = new ArrayList<String>();
						out.add("bioRep:" + input + ":" + influence);
						infl.put(output, out);
					}
				}
				else {
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
		}
		LhpnFile LHPN = new LhpnFile();
		for (int i = 0; i < specs.size(); i++) {
			LHPN.addInteger(specs.get(i), "0");
		}
		for (int i = 0; i < specs.size(); i++) {
			int placeNum = 0;
			int transNum = 0;
			String previousPlaceName = specs.get(i) + placeNum;
			placeNum++;
			LHPN.addPlace(previousPlaceName, true);
			String number = "0";
			for (Object threshold : conLevel.get(i)) {
				LHPN.addPlace(specs.get(i) + placeNum, false);
				LHPN.addTransition(specs.get(i) + "_trans" + transNum);
				LHPN.addMovement(previousPlaceName, specs.get(i) + "_trans" + transNum);
				LHPN.addMovement(specs.get(i) + "_trans" + transNum, specs.get(i) + placeNum);
				LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), (String) threshold);
				ArrayList<String> activators = new ArrayList<String>();
				ArrayList<String> repressors = new ArrayList<String>();
				ArrayList<String> bioActivators = new ArrayList<String>();
				ArrayList<String> bioRepressors = new ArrayList<String>();
				ArrayList<String> proms = new ArrayList<String>();
				Double global_np = Double.parseDouble(parameters.get(GlobalConstants.STOICHIOMETRY_STRING));
				Double global_ng = Double.parseDouble(parameters.get(GlobalConstants.PROMOTER_COUNT_STRING));
				Double global_kb = Double.parseDouble(parameters.get(GlobalConstants.KBASAL_STRING));
				Double global_Kb = Double.parseDouble(parameters.get(GlobalConstants.KBIO_STRING));
				Double global_Ko = Double.parseDouble(parameters.get(GlobalConstants.RNAP_BINDING_STRING));
				Double global_ka = Double.parseDouble(parameters.get(GlobalConstants.ACTIVED_STRING));
				Double global_Ka = Double.parseDouble(parameters.get(GlobalConstants.KACT_STRING));
				Double global_ko = Double.parseDouble(parameters.get(GlobalConstants.OCR_STRING));
				Double global_Kr = Double.parseDouble(parameters.get(GlobalConstants.KREP_STRING));
				Double global_kd = Double.parseDouble(parameters.get(GlobalConstants.KDECAY_STRING));
				Double global_nc = Double.parseDouble(parameters.get(GlobalConstants.COOPERATIVITY_STRING));
				Double RNAP = Double.parseDouble(parameters.get(GlobalConstants.RNAP_STRING));
				Double np = global_np;
				Double ng = global_ng;
				Double kb = global_kb;
				Double Kb = global_Kb;
				Double Ko = global_Ko;
				Double ka = global_ka;
				Double Ka = global_Ka;
				Double ko = global_ko;
				Double Kr = global_Kr;
				Double kd = global_kd;
				Double nc = global_nc;
				if (infl.containsKey(specs.get(i))) {
					for (String in : infl.get(specs.get(i))) {
						String[] parse = in.split(":");
						String species = parse[1];
						String influence = parse[2];
						String promoter = influence.split(" ")[influence.split(" ").length - 1];
						if (!proms.contains(promoter)) {
							proms.add(promoter);
						}
						if (parse[0].equals("act")) {
							activators.add(promoter + ":" + species + ":" + influence);
						}
						else if (parse[0].equals("rep")) {
							repressors.add(promoter + ":" + species + ":" + influence);
						}
						else if (parse[0].equals("bioAct")) {
							bioActivators.add(promoter + ":" + species + ":" + influence);
						}
						else if (parse[0].equals("bioRep")) {
							bioRepressors.add(promoter + ":" + species + ":" + influence);
						}
					}
				}
				String rate = "";
				for (String promoter : proms) {
					ng = global_ng;
					Ko = global_Ko;
					ko = global_ko;
					np = global_np;
					kb = global_kb;
					ka = global_ka;
					if (promoters.containsKey(promoter)) {
						Properties p = promoters.get(promoter);
						if (p.containsKey(GlobalConstants.PROMOTER_COUNT_STRING)) {
							ng = Double.parseDouble((String) p.get(GlobalConstants.PROMOTER_COUNT_STRING));
						}
						if (p.containsKey(GlobalConstants.RNAP_BINDING_STRING)) {
							Ko = Double.parseDouble((String) p.get(GlobalConstants.RNAP_BINDING_STRING));
						}
						if (p.containsKey(GlobalConstants.OCR_STRING)) {
							ko = Double.parseDouble((String) p.get(GlobalConstants.OCR_STRING));
						}
						if (p.containsKey(GlobalConstants.STOICHIOMETRY_STRING)) {
							np = Double.parseDouble((String) p.get(GlobalConstants.STOICHIOMETRY_STRING));
						}
						if (p.containsKey(GlobalConstants.KBASAL_STRING)) {
							kb = Double.parseDouble((String) p.get(GlobalConstants.KBASAL_STRING));
						}
						if (p.containsKey(GlobalConstants.ACTIVED_STRING)) {
							ka = Double.parseDouble((String) p.get(GlobalConstants.ACTIVED_STRING));
						}
					}
					String addBio = "";
					String influence = "";
					for (String bioAct : bioActivators) {
						String split[] = bioAct.split(":");
						if (split[0].equals(promoter)) {
							influence = split[2];
							Properties p = influences.get(influence);
							Kb = global_Kb;
							if (p.containsKey(GlobalConstants.KBIO_STRING)) {
								Kb = Double.parseDouble((String) p.get(GlobalConstants.KBIO_STRING));
							}
							if (addBio.equals("")) {
								addBio = Kb + "*" + split[1];
							}
							else {
								addBio += "*" + split[1];
							}
						}
					}
					if (!addBio.equals("")) {
						activators.add(promoter + ":" + addBio + ":" + influence);
					}
					addBio = "";
					influence = "";
					for (String bioRep : bioRepressors) {
						String split[] = bioRep.split(":");
						if (split[0].equals(promoter)) {
							influence = split[2];
							Properties p = influences.get(influence);
							Kb = global_Kb;
							if (p.containsKey(GlobalConstants.KBIO_STRING)) {
								Kb = Double.parseDouble((String) p.get(GlobalConstants.KBIO_STRING));
							}
							if (addBio.equals("")) {
								addBio = Kb + "*" + split[1];
							}
							else {
								addBio += "*" + split[1];
							}
						}
					}
					if (!addBio.equals("")) {
						repressors.add(promoter + ":" + addBio + ":" + influence);
					}
					String promRate = "";
					ArrayList<String> promActivators = new ArrayList<String>();
					ArrayList<String> promRepressors = new ArrayList<String>();
					for (String act : activators) {
						String split[] = act.split(":");
						if (split[0].equals(promoter)) {
							promActivators.add(split[1] + ":" + split[2]);
						}
					}
					for (String rep : repressors) {
						String split[] = rep.split(":");
						if (split[0].equals(promoter)) {
							promRepressors.add(split[1] + ":" + split[2]);
						}
					}					
					if (promActivators.size() != 0) {
						promRate += "(" + np + "*" + ng + ")*((" + kb + "*" + Ko + "*" + RNAP + ")";
						for (String act : promActivators) {
							String split[] = act.split(":");
							Properties p = influences.get(split[1]);
							nc = global_nc;
							Kr = global_Kr;
							Ka = global_Ka;
							if (p.containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
								nc = Double.parseDouble((String) p.get(GlobalConstants.COOPERATIVITY_STRING));
							}
							if (p.containsKey(GlobalConstants.KREP_STRING)) {
								Kr = Double.parseDouble((String) p.get(GlobalConstants.KREP_STRING));
							}
							if (p.containsKey(GlobalConstants.KACT_STRING)) {
								Ka = Double.parseDouble((String) p.get(GlobalConstants.KACT_STRING));
							}
							promRate += "+(" + ka + "*((" + Ka + "*" + RNAP + "*" + split[0] + ")^" + nc + "))";
						}
						promRate += ")/((1+(" + Ko + "*" + RNAP + "))";
						for (String act : promActivators) {
							String split[] = act.split(":");
							Properties p = influences.get(split[1]);
							nc = global_nc;
							Kr = global_Kr;
							Ka = global_Ka;
							if (p.containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
								nc = Double.parseDouble((String) p.get(GlobalConstants.COOPERATIVITY_STRING));
							}
							if (p.containsKey(GlobalConstants.KREP_STRING)) {
								Kr = Double.parseDouble((String) p.get(GlobalConstants.KREP_STRING));
							}
							if (p.containsKey(GlobalConstants.KACT_STRING)) {
								Ka = Double.parseDouble((String) p.get(GlobalConstants.KACT_STRING));
							}
							promRate += "+((" + Ka + "*" + RNAP + "*" + split[0] + ")^" + nc + ")";
						}
						if (promRepressors.size() != 0) {
							for (String rep : promRepressors) {
								String split[] = rep.split(":");
								Properties p = influences.get(split[1]);
								nc = global_nc;
								Kr = global_Kr;
								Ka = global_Ka;
								if (p.containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
									nc = Double.parseDouble((String) p.get(GlobalConstants.COOPERATIVITY_STRING));
								}
								if (p.containsKey(GlobalConstants.KREP_STRING)) {
									Kr = Double.parseDouble((String) p.get(GlobalConstants.KREP_STRING));
								}
								if (p.containsKey(GlobalConstants.KACT_STRING)) {
									Ka = Double.parseDouble((String) p.get(GlobalConstants.KACT_STRING));
								}
								promRate += "+((" + Kr + "*" + split[0] + ")^" + nc + ")";
							}
						}
						promRate += ")";
					}
					else {
						if (promRepressors.size() != 0) {
							promRate += "(" + np + "*" + ko + "*" + ng + ")*((" + Ko + "*" + RNAP + "))/((1+(" + Ko
									+ "*" + RNAP + "))";
							for (String rep : promRepressors) {
								String split[] = rep.split(":");
								Properties p = influences.get(split[1]);
								nc = global_nc;
								Kr = global_Kr;
								Ka = global_Ka;
								if (p.containsKey(GlobalConstants.COOPERATIVITY_STRING)) {
									nc = Double.parseDouble((String) p.get(GlobalConstants.COOPERATIVITY_STRING));
								}
								if (p.containsKey(GlobalConstants.KREP_STRING)) {
									Kr = Double.parseDouble((String) p.get(GlobalConstants.KREP_STRING));
								}
								if (p.containsKey(GlobalConstants.KACT_STRING)) {
									Ka = Double.parseDouble((String) p.get(GlobalConstants.KACT_STRING));
								}
								promRate += "+((" + Kr + "*" + split[0] + ")^" + nc + ")";
							}
							promRate += ")";
						}
					}
					if (!promRate.equals("")) {
						if (rate.equals("")) {
							rate = "(" + promRate + ")";
						}
						else {
							rate += "+(" + promRate + ")";
						}
					}
				}
				if (rate.equals("")) {
					rate = "0.0";
				}
				LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "(" + rate + ")/" + "(" + threshold + "-"
						+ number + ")");
				transNum++;
				LHPN.addTransition(specs.get(i) + "_trans" + transNum);
				LHPN.addMovement(specs.get(i) + placeNum, specs.get(i) + "_trans" + transNum);
				LHPN.addMovement(specs.get(i) + "_trans" + transNum, previousPlaceName);
				LHPN.addIntAssign(specs.get(i) + "_trans" + transNum, specs.get(i), number);
				Properties p = this.species.get(specs.get(i));
				kd = global_kd;
				if (p.containsKey(GlobalConstants.KDECAY_STRING)) {
					kd = Double.parseDouble((String) p.get(GlobalConstants.KDECAY_STRING));
				}
				LHPN.addTransitionRate(specs.get(i) + "_trans" + transNum, "(" + specs.get(i) + "*" + kd + ")/" + "("
						+ threshold + "-" + number + ")");
				transNum++;
				previousPlaceName = specs.get(i) + placeNum;
				placeNum++;
				number = (String) threshold;
			}
		}
		return LHPN;
	}

	
	/**
	 * Save the contents to a StringBuffer. Can later be written to a file or other stream.
	 * @return
	 */
	public StringBuffer saveToBuffer(Boolean includeGlobals){
		StringBuffer buffer = new StringBuffer("digraph G {\n");
		for (String s : species.keySet()) {
			buffer.append(s + " [");
			Properties prop = species.get(s);
			for (Object propName : prop.keySet()) {
				if ((propName.toString().equals(GlobalConstants.NAME))
						|| (propName.toString().equals("label"))) {
					buffer.append(checkCompabilitySave(propName.toString()) + "=" + "\""
							+ prop.getProperty(propName.toString()).toString() + "\"" + ",");
				}
				else {
					buffer.append(checkCompabilitySave(propName.toString()) + "=" + "\""
							+ prop.getProperty(propName.toString()).toString() + "\"" + ",");
				}
			}
			if (!prop.containsKey("shape")) {
				buffer.append("shape=ellipse,");
			}
			if (!prop.containsKey("label")) {
				buffer.append("label=\"" + s + "\"");
			}
			else {
				buffer.deleteCharAt(buffer.lastIndexOf(","));
			}
			// buffer.deleteCharAt(buffer.length() - 1);
			buffer.append("]\n");
		}
		for (String s : components.keySet()) {
			buffer.append(s + " [");
			Properties prop = components.get(s);
			for (Object propName : prop.keySet()) {
				if (propName.toString().equals("gcm")) {
					buffer.append(checkCompabilitySave(propName.toString()) + "=\""
							+ prop.getProperty(propName.toString()).toString() + "\"");
					// add sizes and positions of they are present.
					if(	prop.get("graphx")!=null && prop.get("graphy")!=null &&
							prop.getProperty("graphwidth")!=null && prop.getProperty("graphheight")!=null){
							buffer.append(	",graphx="+prop.get("graphx") + 
											",graphy="+prop.get("graphy") +
											",graphwidth="+prop.getProperty("graphwidth")+
											",graphheight="+prop.getProperty("graphheight"));
					}
				}
			}
			buffer.append("]\n");
		}
		for (String s : influences.keySet()) {
			buffer.append(getInput(s) + " -> "// + getArrow(s) + " "
					+ getOutput(s) + " [");
			Properties prop = influences.get(s);
			String promo = "default";
			if (prop.containsKey(GlobalConstants.PROMOTER)) {
				promo = prop.getProperty(GlobalConstants.PROMOTER);
			}
			prop.setProperty(GlobalConstants.NAME, "\"" + getInput(s) + " " + getArrow(s) + " "
					+ getOutput(s) + ", Promoter " + promo + "\"");
			for (Object propName : prop.keySet()) {
				if (propName.toString().equals("label") &&
						prop.getProperty(propName.toString()).toString().equals("")) {
					buffer.append("label=\"\",");
				}	
				else {
					buffer.append(checkCompabilitySave(propName.toString()) + "="
							+ prop.getProperty(propName.toString()).toString() + ",");
				}
			}

			String type = "";
			if (!prop.containsKey("arrowhead")) {
				if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION)) {
					type = "vee";
				}
				else if (prop.getProperty(GlobalConstants.TYPE).equals(
						GlobalConstants.REPRESSION)) {
					type = "tee";
				}
				else {
					type = "dot";
				}
				buffer.append("arrowhead=" + type + "");
			}
			if (buffer.charAt(buffer.length() - 1) == ',') {
				buffer.deleteCharAt(buffer.length() - 1);
			}
			buffer.append("]\n");
		}
		for (String s : components.keySet()) {
			Properties prop = components.get(s);
			for (Object propName : prop.keySet()) {
				if (!propName.toString().equals("gcm")
						&& !propName.toString().equals(GlobalConstants.ID)
						&& prop.keySet().contains("type_" + propName)) {
					if (prop.getProperty("type_" + propName).equals("Output")) {
						buffer.append(s + " -> " + prop.getProperty(propName.toString()).toString()
								+ " [port=" + propName.toString() + ", type=Output");
						buffer.append(", arrowhead=normal");
					}
					else {
						buffer.append(prop.getProperty(propName.toString()).toString() + " -> " + s
								+ " [port=" + propName.toString() + ", type=Input");
						buffer.append(", arrowhead=normal");
					}
					buffer.append("]\n");
				}
			}
		}
		buffer.append("}\n");
		
		// For saving .gcm file before sending to dotty, omit the rest of this.
		if(includeGlobals){
			buffer.append("Global {\n");
			for (String s : defaultParameters.keySet()) {
				if (globalParameters.containsKey(s)) {
					String value = globalParameters.get(s);
					buffer.append(s + "=" + value + "\n");
				}
			}
			buffer.append("}\nPromoters {\n");
			for (String s : promoters.keySet()) {
				buffer.append(s + " [");
				Properties prop = promoters.get(s);
				for (Object propName : prop.keySet()) {
					if (propName.toString().equals(GlobalConstants.NAME)) {
						buffer.append(checkCompabilitySave(propName.toString()) + "=" + "\""
								+ prop.getProperty(propName.toString()).toString() + "\"" + ",");
					}
					else {
						buffer.append(checkCompabilitySave(propName.toString()) + "="
								+ prop.getProperty(propName.toString()).toString() + ",");
					}
				}
				if (buffer.charAt(buffer.length() - 1) == ',') {
					buffer.deleteCharAt(buffer.length() - 1);
				}
				buffer.append("]\n");
			}
			buffer.append("}\nConditions {\n");
			for (String s : conditions) {
				buffer.append(s + "\n");
			}
			/*
			 * buffer.append("}\nComponents {\n"); for (String s :
			 * components.keySet()) { buffer.append(s + " ["); Properties prop =
			 * components.get(s); for (Object propName : prop.keySet()) { if
			 * (propName.toString().equals(GlobalConstants.ID)) { } else {
			 * buffer.append(checkCompabilitySave(propName.toString()) + "=" +
			 * prop.getProperty(propName.toString()).toString() + ","); } } if
			 * (buffer.charAt(buffer.length() - 1) == ',') {
			 * buffer.deleteCharAt(buffer.length() - 1); } buffer.append("]\n");
			 * }
			 */
			buffer.append("}\n");
			buffer.append(GlobalConstants.SBMLFILE + "=\"" + sbmlFile + "\"\n");
			if (bioAbs) {
				buffer.append(GlobalConstants.BIOABS + "=true\n");
			}
			if (dimAbs) {
				buffer.append(GlobalConstants.DIMABS + "=true\n");
			}
		}
		return buffer;
	}

	/**
	 * Save the current object to file.
	 * @param filename
	 */
	public void save(String filename) {
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));
                                                 
			StringBuffer buffer = saveToBuffer(true);
			                                     
			p.print(buffer);                     
			p.close();                           
		}                                        
		catch (FileNotFoundException e) {        
			e.printStackTrace();                 
		}                                        
	}                                            
                                                 
	public void load(String filename) {          
		this.filename = filename;                
		species = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();    
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		StringBuffer data = new StringBuffer();
		loadDefaultParameters();
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
		try {
			parseStates(data);
			parseInfluences(data);
			parseGlobal(data);
			parsePromoters(data);
			parseSBMLFile(data);
			parseBioAbs(data);
			parseDimAbs(data);
			parseConditions(data);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to parse GCM" + e.toString());
			// JOptionPane.showMessageDialog(null,
			// "Unable to parse model, creating a blank model.", "Error",
			// JOptionPane.ERROR_MESSAGE);
			// species = new HashMap<String, Properties>();
			// influences = new HashMap<String, Properties>();
			// promoters = new HashMap<String, Properties>();
			// globalParameters = new HashMap<String, String>();
		}
	}

	public void changePromoterName(String oldName, String newName) {
		String[] sArray = new String[influences.keySet().size()];
		sArray = influences.keySet().toArray(sArray);
		for (int i = 0; i < sArray.length; i++) {
			String s = sArray[i];
			String input = getInput(s);
			String arrow = getArrow(s);
			String output = getOutput(s);
			String newInfluenceName = "";
			if (influences.get(s).containsKey(GlobalConstants.PROMOTER)
					&& influences.get(s).get(GlobalConstants.PROMOTER).equals(oldName)) {
				newInfluenceName = input + " " + arrow + " " + output + ", Promoter " + newName;
				influences.put(newInfluenceName, influences.get(s));
				influences.remove(s);
				// If you don't remove the promoter, it will end up in the properties list twice, one with the old value and one with the new.
				influences.get(newInfluenceName).remove(GlobalConstants.PROMOTER);
				influences.get(newInfluenceName).setProperty(GlobalConstants.PROMOTER, newName);
				influences.get(newInfluenceName).setProperty(GlobalConstants.NAME, newInfluenceName);
			}
		}

		promoters.put(newName, promoters.get(oldName));
		promoters.remove(oldName);
	}

	public void changeSpeciesName(String oldName, String newName) {
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
						condition = condition.substring(0, index) + condition.substring(index, condition.length()).replace(oldName, newName);
					}
					index ++;
					index = condition.indexOf(oldName, index);
				}
			}
			newConditions.add(condition);
		}
		conditions = newConditions;
		species.put(newName, species.get(oldName));
		species.remove(oldName);
	}
	
	public void changeComponentName(String oldName, String newName) {
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
		components.put(newName, components.get(oldName));
		components.remove(oldName);
	}

	public void addSpecies(String name, Properties property) {
		species.put(name, property);
	}

	public void addPromoter(String name, Properties properties) {
		promoters.put(name.replace("\"", ""), properties);
	}

	/**
	 * Add a component given the specified name and properties. If
	 * either is null then they will be created using (hopefully) sensible
	 * defaults.
	 *  
	 * @param name
	 * @param properties
	 * 
	 * @return: the id of the created component.
	 */
	public String addComponent(String name, Properties properties) {
		
		if(name == null){
			name = createNewObjectName("C", components);
		}
		
		components.put(name, properties);
		return name;
	}

	// used by createNewObjectName
	private HashMap<String, Integer> createdHighIds;
	/**
	 * builds a sensible default name for a new species, component, or promoter.
	 * @param prefix: a string which will begin the component name. ("S", "C", or "P")
	 * @param hash: the internal model of species or components or promoter
	 * @return: a string name for the new component.
	 */
	public String createNewObjectName(String prefix, HashMap<String, Properties> hash){
		// make sure createdHighIds exists and is initialized.
		if(createdHighIds == null)
			createdHighIds = new HashMap<String, Integer>();
		if(createdHighIds.containsKey(prefix) == false)
			createdHighIds.put(prefix, 0);
		
		String name;
		do{
			createdHighIds.put(prefix, createdHighIds.get(prefix)+1);
			name = prefix + String.valueOf(createdHighIds.get(prefix));
		}while(hash.containsKey(name));		

		return name;
	}
	
	public String addCondition(String condition) {
		boolean retval = true;
		String finalCond = "";
		ArrayList<String> split = new ArrayList<String>();
		String splitting = condition;
		while (splitting.contains("||")) {
			split.add(splitting.substring(0, splitting.indexOf("||")));
			splitting = splitting.substring(splitting.indexOf("||") + 2);
		}
		split.add(splitting);
		for (String cond : split) {
			finalCond += "(";
			if (cond.split("->").length > 2) {
				return null;
			}
			String[] split2 = cond.split("->");
			int countLeft1 = 0;
			int countRight1 = 0;
			int countLeft2 = 0;
			int countRight2 = 0;
			int index = 0;
			while (index <= split2[0].length()) {
				index = split2[0].indexOf('(', index);
				if (index == -1) {
					break;
				}
				countLeft1 ++;
				index ++;
			}
			index = 0;
			while (index <= split2[0].length()) {
				index = split2[0].indexOf(')', index);
				if (index == -1) {
					break;
				}
				countRight1 ++;
				index ++;
			}
			if (split2.length > 1) {
				index = 0;
				while (index <= split2[0].length()) {
					index = split2[1].indexOf('(', index);
					if (index == -1) {
						break;
					}
					countLeft2 ++;
					index ++;
				}
				index = 0;
				while (index <= split2[0].length()) {
					index = split2[1].indexOf(')', index);
					if (index == -1) {
						break;
					}
					countRight2 ++;
					index ++;
				}
			}
			if ((countLeft1 - countRight1) == (countRight2 - countLeft2)) {
				for (int i = 0; i  < countLeft1 - countRight1; i ++) {
					split2[0] += ")";
					split2[1] = "(" + split2[1];
				}
			}
			ArrayList<String> specs = new ArrayList<String>();
			ArrayList<Object[]> conLevel = new ArrayList<Object[]>();
			for (String spec : species.keySet()) {
				specs.add(spec);
				ArrayList<String> level = new ArrayList<String>();
				level.add("0");
				conLevel.add(level.toArray());
			}
			ExprTree expr = new ExprTree(convertToLHPN(specs, conLevel));
			expr.token = expr.intexpr_gettok(split2[0]);
			if (!split2[0].equals("")) {
				retval = (expr.intexpr_L(split2[0]) && retval);
			}
			else {
				expr = null;
				retval = false;
			}
			if (split2.length > 1) {
				if (retval) {
					finalCond += "(" + expr.toString() + ")->";
				}
				expr = new ExprTree(convertToLHPN(specs, conLevel));
				expr.token = expr.intexpr_gettok(split2[0]);
				if (!split2[0].equals("")) {
					retval = (expr.intexpr_L(split2[0]) && retval);
				}
				else {
					expr = null;
					retval = false;
				}
				if (retval) {
					finalCond += "(" + expr.toString() + ")";
				}
			}
			else if (retval) {
				finalCond += expr.toString();
			}
			finalCond += ")||";
		}
		finalCond = finalCond.substring(0, finalCond.length() - 2);
		if (retval) {
			conditions.add(finalCond);
		}
		if (retval) {
			return finalCond;
		}
		else {
			return null;
		}
	}
	
	public void removeCondition(String condition) {
		conditions.remove(condition);
	}
	
	public ArrayList<String> getConditions() {
		return conditions;
	}

	public void addInfluences(String name, Properties property) {
		influences.put(name, property);
		// Now check to see if a promoter exists in the property
		if (property.containsKey("promoter")) {
			promoters.put(property.getProperty("promoter").replaceAll("\"", ""), new Properties());
		}

	}

	public void removeSpecies(String name) {
		if (name != null && species.containsKey(name)) {
			species.remove(name);
		}
	}

	public HashMap<String, Properties> getSpecies() {
		return species;
	}
	
	public ArrayList<String> getInputSpecies() {
		ArrayList<String> inputs = new ArrayList<String>();
		for (String spec : species.keySet()) {
			if (species.get(spec).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.INPUT)) {
				inputs.add(spec);
			}
		}
		return inputs;
	}
	
	public ArrayList<String> getOutputSpecies() {
		ArrayList<String> outputs = new ArrayList<String>();
		for (String spec : species.keySet()) {
			if (species.get(spec).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)) {
				outputs.add(spec);
			}
		}
		return outputs;
	}

	public HashMap<String, Properties> getComponents() {
		return components;
	}
	
	/**
	 * returns all the ports in the gcm file matching type,
	 * which must be either GlobalConstants.INPUT or 
	 * GlobalConstants.OUTPUT.
	 * @param type
	 * @return
	 */
	public HashMap<String, Properties> getPorts(String type){
		HashMap<String, Properties> out = new HashMap<String, Properties>();
		for(Object ko:this.getSpecies().keySet()){
			String key = (String)ko;
			Properties prop = this.getSpecies().get(ko);
			if(prop.getProperty(GlobalConstants.TYPE).equals(type)){
				out.put(key, prop);
			} 
		}
		return out;
	}
	
	public void connectComponentAndSpecies(Properties comp, String port, String specID, String type){
		
		comp.put(port, specID);
		comp.put("type_" + port, type);
		return;
	}
	
	public String getComponentPortMap(String s) {
		String portmap = "(";
		Properties c = components.get(s);
		ArrayList<String> ports = new ArrayList<String>();
		for (Object key : c.keySet()) {
			if (!key.equals("gcm") && c.containsKey("type_" + key)) {
				ports.add((String) key);
			}
		}
		int i, j;
		String index;
		for (i = 1; i < ports.size(); i++) {
			index = ports.get(i);
			j = i;
			while ((j > 0) && ports.get(j - 1).compareToIgnoreCase(index) > 0) {
				ports.set(j, ports.get(j - 1));
				j = j - 1;
			}
			ports.set(j, index);
		}
		if (ports.size() > 0) {
			portmap += ports.get(0) + "->" + c.getProperty(ports.get(0));
		}
		for (int k = 1; k < ports.size(); k++) {
			portmap += ", " + ports.get(k) + "->" + c.getProperty(ports.get(k));
		}
		portmap += ")";
		return portmap;
	}

	public HashMap<String, Properties> getInfluences() {
		return influences;
	}

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
	 * Checks to see if removing specie is okay
	 * 
	 * @param name
	 *            specie to remove
	 * @return true if specie is in no influences
	 */
	public boolean removeSpeciesCheck(String name) {
		for (String s : influences.keySet()) {
			if (s.contains(name)) {
				return false;
			}
		}
		return true;
	}

	public boolean removeComponentCheck(String name) {
		for (String s : influences.keySet()) {
			if ((" " + s + " ").contains(" " + name + " ")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks to see if removing promoter is okay
	 * 
	 * @param name
	 *            promoter to remove
	 * @return true if promoter is in no influences
	 */
	public boolean removePromoterCheck(String name) {
		for (Properties p : influences.values()) {
			if (p.containsKey(GlobalConstants.PROMOTER)
					&& p.getProperty(GlobalConstants.PROMOTER).equals(name)) {
				return false;
			}
		}
		return true;
	}

	public void removePromoter(String name) {
		if (name != null && promoters.containsKey(name)) {
			promoters.remove(name);
		}
	}

	public void removeComponent(String name) {
		if (name != null && components.containsKey(name)) {
			components.remove(name);
		}
	}

	public void removeInfluence(String name) {
		if (name != null && influences.containsKey(name)) {
			influences.remove(name);
		}
	}

	public void changeInfluencePromoter(String oldInfluence, String oldPromoter, String newPromoter){
		if(oldPromoter == null)
			oldPromoter = "default";
		String pattern = oldPromoter + "$";
		String newInfluence = oldInfluence.replaceFirst(pattern, newPromoter);
		Properties prop = influences.get(oldInfluence);
		
		prop.remove(GlobalConstants.PROMOTER);
		prop.setProperty(GlobalConstants.PROMOTER, newPromoter);
		if(prop.get(GlobalConstants.NAME) == oldInfluence){
			prop.remove(GlobalConstants.NAME);
			prop.put(GlobalConstants.NAME, newPromoter);
		}
		
		removeInfluence(oldInfluence);
		
		influences.put(newInfluence, prop);
	}
	
	public static String getInput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(2);
	}

	public String getArrow(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(3) + matcher.group(4);
	}

	public static String getOutput(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(5);
	}

	public String getPromoter(String name) {
		Pattern pattern = Pattern.compile(PARSE);
		Matcher matcher = pattern.matcher(name);
		matcher.find();
		return matcher.group(6);
	}

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

	public String getParameter(String parameter) {
		if (globalParameters.containsKey(parameter)) {
			return globalParameters.get(parameter);
		}
		else {
			return defaultParameters.get(parameter);
		}
	}

	public void setParameter(String parameter, String value) {
		globalParameters.put(parameter, value);
		parameters.put(parameter, value);
	}

	public void removeParameter(String parameter) {
		globalParameters.remove(parameter);
	}

	private void parseStates(StringBuffer data) {
		Pattern network = Pattern.compile(NETWORK);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		matcher.find();
		matcher = pattern.matcher(matcher.group(1));
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3) != null) {
					properties.put(propMatcher.group(1), propMatcher.group(3));
				}
				else {
					properties.put(propMatcher.group(1), propMatcher.group(4));
				}
			}
			// for backwards compatibility
			if (properties.containsKey("const")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.INPUT);
			}
			else if (!properties.containsKey(GlobalConstants.TYPE)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.OUTPUT);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals("constant")) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.INPUT);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.CONSTANT)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.INPUT);
			}
			if (properties.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.NORMAL)) {
				properties.setProperty(GlobalConstants.TYPE, GlobalConstants.OUTPUT);
			}

			// for backwards compatibility
			if (properties.containsKey("label")) {
				properties.put(GlobalConstants.ID, properties.getProperty("label")
						.replace("\"", ""));
			}
			if (properties.containsKey("gcm")) {
				if (properties.containsKey(GlobalConstants.TYPE)) {
					properties.remove(GlobalConstants.TYPE);
				}
				properties.put("gcm", properties.getProperty("gcm").replace("\"", ""));
				components.put(name, properties);
			}
			else {
				species.put(name, properties);
			}
		}
	}

	private void parseSBMLFile(StringBuffer data) {
		Pattern pattern = Pattern.compile(SBMLFILE);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			sbmlFile = matcher.group(1);
		}
	}

	private void parseDimAbs(StringBuffer data) {
		Pattern pattern = Pattern.compile(DIMABS);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			if (matcher.group(1).equals("true")) {
				dimAbs = true;
			}
		}
	}

	private void parseBioAbs(StringBuffer data) {
		Pattern pattern = Pattern.compile(BIOABS);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			if (matcher.group(1).equals("true")) {
				bioAbs = true;
			}
		}
	}

	private void parseGlobal(StringBuffer data) {
		Pattern pattern = Pattern.compile(GLOBAL);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String s = matcher.group(1);
			matcher = propPattern.matcher(s);
			while (matcher.find()) {
				if (matcher.group(3) != null) {
					globalParameters.put(matcher.group(1), matcher.group(3));
					parameters.put(matcher.group(1), matcher.group(3));
				}
				else {
					globalParameters.put(matcher.group(1), matcher.group(4));
					parameters.put(matcher.group(1), matcher.group(4));
				}
			}
		}
	}
	
	private void parseConditions(StringBuffer data) {
		Pattern pattern = Pattern.compile(CONDITION);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String s = matcher.group(1).trim();
			for (String cond : s.split("\n")) {
				addCondition(cond.trim());
			}
		}
	}

	private void parsePromoters(StringBuffer data) {
		Pattern network = Pattern.compile(PROMOTERS_LIST);
		Matcher matcher = network.matcher(data.toString());
		Pattern pattern = Pattern.compile(STATE);
		Pattern propPattern = Pattern.compile(PROPERTY);
		if (!matcher.find()) {
			return;
		}
		matcher = pattern.matcher(matcher.group(1));
		while (matcher.find()) {
			String name = matcher.group(2);
			Matcher propMatcher = propPattern.matcher(matcher.group(3));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3) != null) {
					properties.put(propMatcher.group(1), propMatcher.group(3));
				}
				else {
					properties.put(propMatcher.group(1), propMatcher.group(4));
				}
			}
			promoters.put(name, properties);
		}
	}

	/*
	 * private void parseComponents(StringBuffer data) { Pattern network =
	 * Pattern.compile(COMPONENTS_LIST); Matcher matcher =
	 * network.matcher(data.toString()); Pattern pattern =
	 * Pattern.compile(STATE); Pattern propPattern = Pattern.compile(PROPERTY);
	 * if (!matcher.find()) { return; } matcher =
	 * pattern.matcher(matcher.group(1)); while (matcher.find()) { String name =
	 * matcher.group(2); Matcher propMatcher =
	 * propPattern.matcher(matcher.group(3)); Properties properties = new
	 * Properties(); while (propMatcher.find()) { if
	 * (propMatcher.group(3)!=null) { properties.put(propMatcher.group(1),
	 * propMatcher.group(3)); } else { properties.put(propMatcher.group(1),
	 * propMatcher.group(4)); } } components.put(name, properties); } }
	 */

	private void parseInfluences(StringBuffer data) {
		Pattern pattern = Pattern.compile(REACTION);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		while (matcher.find()) {
			Matcher propMatcher = propPattern.matcher(matcher.group(6));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				if (propMatcher.group(3) != null) {
					properties
							.put(checkCompabilityLoad(propMatcher.group(1)), propMatcher.group(3));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(3))) {
						promoters.put(propMatcher.group(3).replaceAll("\"", ""), new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(3)
								.replace("\"", "")); // for backwards
														// compatibility
					}
				}
				else {
					properties
							.put(checkCompabilityLoad(propMatcher.group(1)), propMatcher.group(4));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(4))) {
						promoters.put(propMatcher.group(4).replaceAll("\"", ""), new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(4)
								.replace("\"", "")); // for backwards
														// compatibility
					}
				}
			}

			if (properties.containsKey("port")) {
				if (components.containsKey(matcher.group(2))) {
					components.get(matcher.group(2)).put(properties.get("port"), matcher.group(5));
					if (properties.containsKey("type")) {
						components.get(matcher.group(2)).put("type_" + properties.get("port"), properties.get("type"));
					}
					else {
						GCMFile file = new GCMFile(path);
						file.load(path + separator + components.get(matcher.group(2)).getProperty("gcm"));
						if (file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE).equals(GlobalConstants.INPUT)) {
							components.get(matcher.group(2)).put("type_" + properties.get("port"), "Input");
						}
						else if (file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)) {
							components.get(matcher.group(2)).put("type_" + properties.get("port"), "Output");
						}
					}
				}
				else {
					components.get(matcher.group(5)).put(properties.get("port"), matcher.group(2));
					if (properties.containsKey("type")) {
						components.get(matcher.group(5)).put("type_" + properties.get("port"), properties.get("type"));
					}
					else {
						GCMFile file = new GCMFile(path);
						file.load(path + separator + components.get(matcher.group(5)).getProperty("gcm"));
						if (file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE).equals(GlobalConstants.INPUT)) {
							components.get(matcher.group(5)).put("type_" + properties.get("port"), "Input");
						}
						else if (file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)) {
							components.get(matcher.group(5)).put("type_" + properties.get("port"), "Output");
						}
					}
				}
			}
			else {
				String name = "";
				if (properties.containsKey("arrowhead")) {
					if (properties.getProperty("arrowhead").indexOf("vee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.ACTIVATION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							name = matcher.group(2) + " +> " + matcher.group(5);
						}
						else {
							name = matcher.group(2) + " -> " + matcher.group(5);
						}
					}
					else if (properties.getProperty("arrowhead").indexOf("tee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.REPRESSION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							name = matcher.group(2) + " +| " + matcher.group(5);
						}
						else {
							name = matcher.group(2) + " -| " + matcher.group(5);
						}
					}
					else {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.NOINFLUENCE);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							name = matcher.group(2) + " x> " + matcher.group(5);
						}
						else {
							name = matcher.group(2) + " x> " + matcher.group(5);
						}
					}
				}
				if (properties.getProperty(GlobalConstants.PROMOTER) != null) {
					name = name + ", Promoter " + properties.getProperty(GlobalConstants.PROMOTER);
				}
				else {
					name = name + ", Promoter " + "default";
				}
				if (!properties.containsKey("label")) {
					String label = properties.getProperty(GlobalConstants.PROMOTER);
					if (label == null) {
						label = "";
					}
					if (properties.containsKey(GlobalConstants.BIO)
							&& properties.get(GlobalConstants.BIO).equals("yes")) {
						label = label + "+";
					}
					properties.put("label", "\"" + label + "\"");
				}
				properties.put(GlobalConstants.NAME, name);
				influences.put(name, properties);
			}
		}
	}

	public void loadDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();
		defaultParameters = new HashMap<String, String>();
		defaultParameters.put(GlobalConstants.KDECAY_STRING, biosimrc.get(
				"biosim.gcm.KDECAY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KASSOCIATION_STRING, biosimrc.get(
				"biosim.gcm.KASSOCIATION_VALUE", ""));
		defaultParameters.put(GlobalConstants.KBIO_STRING, biosimrc
				.get("biosim.gcm.KBIO_VALUE", ""));
		defaultParameters.put(GlobalConstants.COOPERATIVITY_STRING, biosimrc.get(
				"biosim.gcm.COOPERATIVITY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KREP_STRING, biosimrc
				.get("biosim.gcm.KREP_VALUE", ""));
		defaultParameters.put(GlobalConstants.KACT_STRING, biosimrc
				.get("biosim.gcm.KACT_VALUE", ""));
		defaultParameters.put(GlobalConstants.RNAP_BINDING_STRING, biosimrc.get(
				"biosim.gcm.RNAP_BINDING_VALUE", ""));
		defaultParameters.put(GlobalConstants.RNAP_STRING, biosimrc
				.get("biosim.gcm.RNAP_VALUE", ""));
		defaultParameters.put(GlobalConstants.OCR_STRING, biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		defaultParameters.put(GlobalConstants.KBASAL_STRING, biosimrc.get(
				"biosim.gcm.KBASAL_VALUE", ""));
		defaultParameters.put(GlobalConstants.PROMOTER_COUNT_STRING, biosimrc.get(
				"biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		defaultParameters.put(GlobalConstants.STOICHIOMETRY_STRING, biosimrc.get(
				"biosim.gcm.STOICHIOMETRY_VALUE", ""));
		defaultParameters.put(GlobalConstants.ACTIVED_STRING, biosimrc.get(
				"biosim.gcm.ACTIVED_VALUE", ""));
		defaultParameters.put(GlobalConstants.MAX_DIMER_STRING, biosimrc.get(
				"biosim.gcm.MAX_DIMER_VALUE", ""));
		defaultParameters.put(GlobalConstants.INITIAL_STRING, biosimrc.get(
				"biosim.gcm.INITIAL_VALUE", ""));

		for (String s : defaultParameters.keySet()) {
			parameters.put(s, defaultParameters.get(s));
		}

	}

	public void setParameters(HashMap<String, String> parameters) {
		for (String s : parameters.keySet()) {
			defaultParameters.put(s, parameters.get(s));
			this.parameters.put(s, parameters.get(s));
		}
	}

	private String checkCompabilitySave(String key) {
		if (key.equals(GlobalConstants.MAX_DIMER_STRING)) {
			return "maxDimer";
		}
		return key;
	}

	private String checkCompabilityLoad(String key) {
		if (key.equals("maxDimer")) {
			return GlobalConstants.MAX_DIMER_STRING;
		}
		return key;
	}
	
	private ArrayList<String> setToArrayList(Set<String> set) {
		ArrayList<String> array = new ArrayList<String>();
		for (String s : set) {
			array.add(s);
		}
		return array;
	}

	private SBMLDocument unionSBML(SBMLDocument mainDoc, SBMLDocument doc, String compName) {
		Model m = doc.getModel();
		for (int i = 0; i < m.getNumCompartmentTypes(); i++) {
			org.sbml.libsbml.CompartmentType c = m.getCompartmentType(i);
			String newName = compName + "_" + c.getId();
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
			String newName = compName + "_" + c.getId();
			updateVarId(false, c.getId(), newName, doc);
			c.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumCompartments(); j++) {
				if (mainDoc.getModel().getCompartment(j).getId().equals(c.getId())) {
					add = false;
					org.sbml.libsbml.Compartment comp = mainDoc.getModel().getCompartment(j);
					if (!c.getName().equals(comp.getName())) {
						return null;
					}
					if (!c.getCompartmentType().equals(comp.getCompartmentType())) {
						return null;
					}
					if (c.getConstant() != comp.getConstant()) {
						return null;
					}
					if (!c.getOutside().equals(comp.getOutside())) {
						return null;
					}
					if (c.getVolume() != comp.getVolume()) {
						return null;
					}
					if (c.getSpatialDimensions() != comp.getSpatialDimensions()) {
						return null;
					}
					if (c.getSize() != comp.getSize()) {
						return null;
					}
					if (!c.getUnits().equals(comp.getUnits())) {
						return null;
					}
				}
			}
			if (add) {
				mainDoc.getModel().addCompartment(c);
			}
		}
		for (int i = 0; i < m.getNumSpeciesTypes(); i++) {
			org.sbml.libsbml.SpeciesType s = m.getSpeciesType(i);
			String newName = compName + "_" + s.getId();
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
		for (int i = 0; i < m.getNumSpecies(); i++) {
			Species spec = m.getSpecies(i);
			String newName = compName + "_" + spec.getId();
			for (Object port : getComponents().get(compName).keySet()) {
				if (spec.getId().equals((String) port)) {
					newName = "_" + compName + "_" + getComponents().get(compName).getProperty((String) port);
				}
			}
			updateVarId(true, spec.getId(), newName, doc);
			spec.setId(newName);
		}
		for (int i = 0; i < m.getNumSpecies(); i++) {
			Species spec = m.getSpecies(i);
			boolean add = true;
			if (spec.getId().startsWith("_" + compName + "_")) {
				updateVarId(true, spec.getId(), spec.getId().substring(2 + compName.length()), doc);
				spec.setId(spec.getId().substring(2 + compName.length()));
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
			org.sbml.libsbml.Parameter p = m.getParameter(i);
			String newName = compName + "_" + p.getId();
			updateVarId(false, p.getId(), newName, doc);
			p.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumParameters(); j++) {
				if (mainDoc.getModel().getParameter(j).getId().equals(p.getId())) {
					add = false;
					org.sbml.libsbml.Parameter param = mainDoc.getModel().getParameter(j);
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
			String newName = compName + "_" + r.getId();
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
			mainDoc.getModel().addConstraint(constraint);
		}
		for (int i = 0; i < m.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) m.getListOfEvents().get(i);
			String newName = compName + "_" + event.getId();
			updateVarId(false, event.getId(), newName, doc);
			event.setId(newName);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumEvents(); j++) {
				if (mainDoc.getModel().getEvent(j).getId().equals(event.getId())) {
					add = false;
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
					for (int k = 0; k < event.getNumEventAssignments(); k++) {
						EventAssignment a = event.getEventAssignment(k);
						boolean found = false;
						for (int l = 0; l < e.getNumEventAssignments(); l++) {
							EventAssignment assign = e.getEventAssignment(l);
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
			if (add) {
				mainDoc.getModel().addEvent(event);
			}
		}
		for (int i = 0; i < m.getNumUnitDefinitions(); i++) {
			UnitDefinition u = m.getUnitDefinition(i);
			boolean add = true;
			for (int j = 0; j < mainDoc.getModel().getNumUnitDefinitions(); j++) {
				if (mainDoc.getModel().getUnitDefinition(j).getId().equals(u.getId())) {
					add = false;
				}
			}
			if (add) {
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
			org.sbml.libsbml.Species species = (org.sbml.libsbml.Species) model.getListOfSpecies().get(i);
			if (species.getCompartment().equals(origId)) {
				species.setCompartment(newId);
			}
			if (species.getSpeciesType().equals(origId)) {
				species.setSpeciesType(newId);
			}
		}
		for (int i = 0; i < model.getNumCompartments(); i++) {
			org.sbml.libsbml.Compartment compartment = (org.sbml.libsbml.Compartment) model.getListOfCompartments().get(i);
			if (compartment.getCompartmentType().equals(origId)) {
				compartment.setCompartmentType(newId);
			}
		}
		for (int i = 0; i < model.getNumReactions(); i++) {
			org.sbml.libsbml.Reaction reaction = (org.sbml.libsbml.Reaction) model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						specRef.getStoichiometryMath().setMath(updateMathVar(specRef
								.getStoichiometryMath().getMath(), origId, newId));
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
						specRef.getStoichiometryMath().setMath(updateMathVar(specRef
								.getStoichiometryMath().getMath(), origId, newId));
					}
				}
			}
			reaction.getKineticLaw().setMath(
					updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
		}
		if (model.getNumInitialAssignments() > 0) {
			for (int i = 0; i < model.getNumInitialAssignments(); i++) {
				InitialAssignment init = (InitialAssignment) model.getListOfInitialAssignments().get(i);
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
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) model.getListOfEvents().get(i);
				if (event.isSetTrigger()) {
					event.getTrigger().setMath(updateMathVar(event.getTrigger().getMath(), origId, newId));
				}
				if (event.isSetDelay()) {
					event.getDelay().setMath(updateMathVar(event.getDelay().getMath(), origId, newId));
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
	
	public String getPath(){
		return path;
	}

	private static final String NETWORK = "digraph\\sG\\s\\{([^}]*)\\s\\}";

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) (\\-|\\+|x)(\\>|\\|) *([^ \n]*) *\\[([^\\]]*)]";

	// private static final String PARSE = "(^|\\n) *([^ \\n,]*) *\\-\\> *([^
	// \n,]*)";

	private static final String PARSE = "(^|\\n) *([^ \\n,]*) (\\-|\\+|x)(\\>|\\|) *([^ \n,]*), Promoter ([a-zA-Z\\d_]+)";

	private static final String PROPERTY = "([a-zA-Z\\ \\-]+)=(\"([^\"]*)\"|([^\\s,]+))";

	private static final String GLOBAL = "Global\\s\\{([^}]*)\\s\\}";
	
	private static final String CONDITION = "Conditions\\s\\{([^}]*)\\s\\}";

	private static final String SBMLFILE = GlobalConstants.SBMLFILE + "=\"([^\"]*)\"";

	private static final String DIMABS = GlobalConstants.DIMABS + "=(true|false)";

	private static final String BIOABS = GlobalConstants.BIOABS + "=(true|false)";

	private static final String PROMOTERS_LIST = "Promoters\\s\\{([^}]*)\\s\\}";

	// private static final String COMPONENTS_LIST =
	// "Components\\s\\{([^}]*)\\s\\}";

	private String sbmlFile = "";

	private boolean dimAbs = false;

	private boolean bioAbs = false;

	private HashMap<String, Properties> species;

	private HashMap<String, Properties> influences;

	private HashMap<String, Properties> promoters;

	private HashMap<String, Properties> components;
	
	private ArrayList<String> conditions;

	private HashMap<String, String> parameters;

	private HashMap<String, String> defaultParameters;

	private HashMap<String, String> globalParameters;
	
	private String path;
}
