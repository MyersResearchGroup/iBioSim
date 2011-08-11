package gcm.parser;

import gcm.gui.Grid;
import gcm.network.AbstractionEngine;
import gcm.network.GeneticNetwork;
import gcm.util.GlobalConstants;
import gcm.util.UndoManager;
import gcm.util.Utility;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
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
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.LocalParameter;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;

import com.sun.mail.handlers.multipart_mixed;

import sbmleditor.MySpecies;
import sbmleditor.Reactions;
import sbmleditor.SBMLutilities;

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
		undoManager = new UndoManager();
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.path = path;
		species = new HashMap<String, Properties>();
		reactions = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		compartments = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		isWithinCompartment = false;
		grid = new Grid();
		loadDefaultParameters();
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

	public void setSBMLDocument(SBMLDocument sbmlDoc) {
		sbml = sbmlDoc;
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
				flattenGCM(false);
				convertToLHPN(specs, conLevel, null).save(filename);
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

	public SBMLDocument flattenGCM(boolean includeSBML) {
		ArrayList<String> gcms = new ArrayList<String>();
		gcms.add(filename);
		save(filename + ".temp");
		ArrayList<String> comps = setToArrayList(components.keySet());
		SBMLDocument sbml = null;
		if (!sbmlFile.equals("") && includeSBML) {
			if (this.sbml != null) {
				sbml = this.sbml;
			}
			else {
				sbml = Gui.readSBML(path + separator + sbmlFile);
			}
		}
		else if (!sbmlFile.equals("") && !includeSBML) {
			// TODO: This should likely be removed.
			Utility.createErrorMessage("SBMLs Included", "There are sbml files associated with the gcm file and its components.");
			load(filename + ".temp");
			new File(filename + ".temp").delete();
			return null;
		}
		else {
			sbml = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
			Model m = sbml.createModel();
			sbml.setModel(m);
			Utility.addCompartments(sbml, "default");
			sbml.getModel().getCompartment("default").setSize(1);
			m.setVolumeUnits("litre");
		}
		/*
		 * for (String compName : comps) { // Checks if component is a
		 * compartment Object testCompartment =
		 * components.get(compName).get("compartment"); boolean isCompartment =
		 * false; if (testCompartment != null) isCompartment =
		 * Boolean.parseBoolean(testCompartment.toString()); else
		 * components.get(compName).put("compartment", "false"); if
		 * (isCompartment) { Utility.addCompartments(sbml, compName);
		 * sbml.getModel().getCompartment(compName).setSize(1);
		 * sbml.getModel().setVolumeUnits("litre"); compartments.add(compName);
		 * } }
		 */

		// loop through the keyset of the components of the gcm
		for (String s : comps) {
			GCMFile file = new GCMFile(path);

			// load the component's gcm into a new GCMFile
			file.load(path + separator + components.get(s).getProperty("gcm"));
			/*
			 * if (file.getIsWithinCompartment()) {
			 * 
			 * //load the sbml associated with the component/compartment //and
			 * add it to the overall sbml SBMLDocument compSBML =
			 * Gui.readSBML(path + separator + file.sbmlFile);
			 * Utility.addCompartments(sbml, s + "__" +
			 * compSBML.getModel().getCompartment(0).getId());
			 * sbml.getModel().getCompartment(s + "__" +
			 * compSBML.getModel().getCompartment(0).getId()).setSize(1);
			 * sbml.getModel().setVolumeUnits("litre");
			 * 
			 * if (!compartments.contains(s + "__" +
			 * compSBML.getModel().getCompartment(0).getId()))
			 * compartments.add(s + "__" +
			 * compSBML.getModel().getCompartment(0).getId()); }
			 */
			for (String p : globalParameters.keySet()) {
				if (!file.globalParameters.containsKey(p)) {
					file.setParameter(p, globalParameters.get(p));
				}
			}
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
			sbml = unionSBML(sbml, unionGCM(this, file, s, includeSBML, copy), s, this.components, 
					file.getIsWithinCompartment(),file.getParameter(GlobalConstants.RNAP_STRING));
			if (sbml == null && copy.isEmpty()) {
				Utility.createErrorMessage("Loop Detected", "Cannot flatten GCM.\n" + "There is a loop in the components.");
				load(filename + ".temp");
				new File(filename + ".temp").delete();
				return null;
			}
			else if (sbml == null && includeSBML) {
				Utility.createErrorMessage("Cannot Merge SBMLs", "Unable to merge sbml files from components.");
				load(filename + ".temp");
				new File(filename + ".temp").delete();
				return null;
			}
			else if (sbml == null && !includeSBML) {
				Utility.createErrorMessage("SBMLs Included", "There are sbml files associated with the gcm file and its components.");
				load(filename + ".temp");
				new File(filename + ".temp").delete();
				return null;
			}
		}
		components = new HashMap<String, Properties>();
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

	private SBMLDocument unionGCM(GCMFile topLevel, GCMFile bottomLevel, String compName, boolean includeSBML, ArrayList<String> gcms) {
		ArrayList<String> mod = setToArrayList(bottomLevel.components.keySet());
		SBMLDocument sbml = new SBMLDocument(Gui.SBML_LEVEL, Gui.SBML_VERSION);
		Model m = sbml.createModel();
		sbml.setModel(m);
		if (!bottomLevel.sbmlFile.equals("") && includeSBML) {
			sbml = Gui.readSBML(path + separator + bottomLevel.sbmlFile);
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
			ArrayList<String> copy = copyArray(gcms);
			if (copy.contains(file.getFilename())) {
				while (!gcms.isEmpty()) {
					gcms.remove(0);
				}
				return null;
			}
			copy.add(file.getFilename());
			sbml = unionSBML(sbml, unionGCM(bottomLevel, file, s, includeSBML, copy), s, bottomLevel.components, 
					file.getIsWithinCompartment(),file.getParameter(GlobalConstants.RNAP_STRING));
			if (sbml == null) {
				return null;
			}
		}

		// change the names of the bottom-level stuff
		// prepend the component name to the species to preserve hierarchy

		mod = setToArrayList(bottomLevel.promoters.keySet());
		for (String prom : mod) {
			bottomLevel.promoters.get(prom).put(GlobalConstants.ID, compName + "__" + prom);
			bottomLevel.changePromoterName(prom, compName + "__" + prom);
		}

		mod = setToArrayList(bottomLevel.species.keySet());
		for (String spec : mod) {
			bottomLevel.species.get(spec).put(GlobalConstants.ID, compName + "__" + spec);
			bottomLevel.changeSpeciesName(spec, compName + "__" + spec);
		}

		mod = setToArrayList(bottomLevel.species.keySet());
		for (String spec : mod) {
			for (Object port : topLevel.components.get(compName).keySet()) {
				if (spec.equals(compName + "__" + port)) {
//						&& !bottomLevel.species.get(spec).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.INPUT)) {
					bottomLevel.species.get(spec).put(GlobalConstants.ID, topLevel.components.get(compName).getProperty((String) port));
					bottomLevel.changeSpeciesName(spec, topLevel.components.get(compName).getProperty((String) port));
				}
			}
		}

		// go through all of the global params of the bottom level gcm
		// if the bottom level species don't have the param as a property, add
		// it
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
			else if (param.equals(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.OCR_STRING)) {
				mod = setToArrayList(bottomLevel.promoters.keySet());
				for (String prom : mod) {
					if (!bottomLevel.promoters.get(prom).containsKey(GlobalConstants.OCR_STRING)) {
						bottomLevel.promoters.get(prom).put(GlobalConstants.OCR_STRING, bottomLevel.globalParameters.get(GlobalConstants.OCR_STRING));
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

			// this is probably never going to be called, as kmdiff won't ever
			// be a global parameter, as far as i know
			else if (param.equals(GlobalConstants.MEMDIFF_STRING)) {
				mod = setToArrayList(bottomLevel.species.keySet());
				for (String spec : mod) {
					if (!bottomLevel.species.get(spec).containsKey(GlobalConstants.MEMDIFF_STRING)) {
						bottomLevel.species.get(spec).put(GlobalConstants.MEMDIFF_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.MEMDIFF_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.KECDIFF_STRING)) {
				mod = setToArrayList(bottomLevel.species.keySet());
				for (String spec : mod) {
					if (!bottomLevel.species.get(spec).containsKey(GlobalConstants.KECDIFF_STRING)) {
						bottomLevel.species.get(spec).put(GlobalConstants.KECDIFF_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KECDIFF_STRING));
					}
				}
			}
			else if (param.equals(GlobalConstants.KECDECAY_STRING)) {
				mod = setToArrayList(bottomLevel.species.keySet());
				for (String spec : mod) {
					if (!bottomLevel.species.get(spec).containsKey(GlobalConstants.KECDECAY_STRING)) {
						bottomLevel.species.get(spec).put(GlobalConstants.KECDECAY_STRING,
								bottomLevel.globalParameters.get(GlobalConstants.KECDECAY_STRING));
					}
				}
			}
		}

		// now that the necessary name changes have happened,
		// put all of the bottom-level stuff into the top level

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
	
	private double parseValue(String value) {
		if (value.contains("/")) {
			String[] parts = value.split("/");
			return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
		}
		else {
			return Double.parseDouble(value);
		}
	}

	public LhpnFile convertToLHPN(ArrayList<String> specs, ArrayList<Object[]> conLevel, String lpnProperty) {
		GCMParser parser = new GCMParser(this, false);
		GeneticNetwork network = parser.buildNetwork();
		network.markAbstractable();
		AbstractionEngine abs = network.createAbstractionEngine();
		HashMap<String, ArrayList<String>> infl = new HashMap<String, ArrayList<String>>();
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
		ArrayList<String> biochemical = getBiochemicalSpecies();
		LhpnFile LHPN = new LhpnFile();
		for (int i = 0; i < specs.size(); i++) {
			double initial = parseValue(parameters.get(GlobalConstants.INITIAL_STRING));
			double selectedThreshold = 0;
			try {
				initial = parseValue(species.get(specs.get(i)).getProperty(GlobalConstants.INITIAL_STRING));
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
				value = parseValue(this.species.get(input).getProperty(GlobalConstants.INITIAL_STRING));
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
					Properties speciesProps = this.species.get(specs.get(i));
					if (!speciesProps.get(GlobalConstants.TYPE).equals(GlobalConstants.INPUT)) {
						ArrayList<String> activators = new ArrayList<String>();
						ArrayList<String> repressors = new ArrayList<String>();
						ArrayList<String> proms = new ArrayList<String>();
						Double global_np = parseValue(parameters.get(GlobalConstants.STOICHIOMETRY_STRING));
						Double global_kd = parseValue(parameters.get(GlobalConstants.KDECAY_STRING));
						Double np = global_np;
						Double kd = global_kd;
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
							}
						}
						String rate = "";
						for (String promoter : proms) {
							String promRate = abs.abstractOperatorSite(network.getPromoters().get(promoter));
							for (String species : this.species.keySet()) {
								if (promRate.contains(species) && !LHPN.getIntegers().keySet().contains(species)) {
									double value;
									try {
										value = parseValue(this.species.get(species).getProperty(GlobalConstants.INITIAL_STRING));
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
						if (speciesProps.containsKey(GlobalConstants.KDECAY_STRING)) {
							kd = parseValue((String) speciesProps.get(GlobalConstants.KDECAY_STRING));
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
		if (lpnProperty != null) {
			ArrayList<String> sortedSpecies = new ArrayList<String>();
			for (String s : species.keySet()) {
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
				if (replace != null && lpnProperty.contains(s)) {
					replace = "(" + replace + ")";
					lpnProperty = lpnProperty.replace(s, replace);
				}
			}
			LHPN.addProperty(lpnProperty);
		}
		return LHPN;
	}

	/**
	 * Save the contents to a StringBuffer. Can later be written to a file or
	 * other stream.
	 * 
	 * @return
	 */
	public StringBuffer saveToBuffer(Boolean includeGlobals, Boolean collectGarbage, boolean appendSBML) {
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
		for (String s : reactions.keySet()) {
			buffer.append(s + " [");
			Properties prop = reactions.get(s);
			boolean first = true;
			for (Object propName : prop.keySet()) {
				if (!first) buffer.append(","); else first=false;
				buffer.append(checkCompabilitySave(propName.toString()) + "=" + "\""
						+ prop.getProperty(propName.toString()).toString() + "\"");
			}
			if (!prop.containsKey("shape")) {
				if (!first) buffer.append(","); else first=false;
				buffer.append("shape=circle");
			}
			buffer.append("]\n");
		}
		for (String s : components.keySet()) {
			buffer.append(s + " [");
			Properties prop = components.get(s);
			for (Object propName : prop.keySet()) {
				buffer.append(checkCompabilitySave(propName.toString()) + "="
					+ prop.getProperty(propName.toString()).toString() + ",");
			}
			if (buffer.charAt(buffer.length() - 1) == ',') {
				buffer.deleteCharAt(buffer.length() - 1);
			}
			/*				
				if (propName.toString().equals("gcm")) {
					buffer.append(checkCompabilitySave(propName.toString()) + "=\""
							+ prop.getProperty(propName.toString()).toString() + "\"");
					// add sizes and positions of they are present.
					if (prop.get("graphx") != null && prop.get("graphy") != null
							&& prop.getProperty("graphwidth") != null
							&& prop.getProperty("graphheight") != null) {
						buffer.append(",graphx=" + prop.get("graphx") + ",graphy="
								+ prop.get("graphy") + ",graphwidth="
								+ prop.getProperty("graphwidth") + ",graphheight="
								+ prop.getProperty("graphheight"));
					}
					if (prop.get("compartment") != null)
						buffer.append(",compartment=" + prop.get("compartment"));
					else
						buffer.append(",compartment=false");
				}
			}
			*/
			buffer.append("]\n");
		}
		//List later facilitates garbage collecting of promoters that don't belong to an influence  
		ArrayList<String> promotersWithInfluences = new ArrayList<String>();
		for (String s : influences.keySet()) {
			buffer.append(getInput(s) + " -> " + getOutput(s) + " [");
			Properties prop = influences.get(s);
			String promo = "none";
			if (prop.containsKey(GlobalConstants.PROMOTER)) {
				promo = prop.getProperty(GlobalConstants.PROMOTER);
				if (!promotersWithInfluences.contains(promo))
					promotersWithInfluences.add(promo);
			}
			prop.setProperty(GlobalConstants.NAME, "\"" + getInput(s) + " " + getArrow(s) + " "
					+ getOutput(s) + ", Promoter " + promo + "\"");
			for (Object propName : prop.keySet()) {
				if (propName.toString().equals("label")
						&& prop.getProperty(propName.toString()).toString().equals("")) {
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
				else if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.REPRESSION)) {
					type = "tee";
				}
				else if (prop.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.COMPLEX)) {
					type = "plus";
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
		
		//append the grid size if there is one
		if (getGrid().isEnabled()) {
			
			buffer.append("}\nGrid {\n");			
			buffer.append("rows=" + getGrid().getNumRows() + "\n");
			buffer.append("cols=" + getGrid().getNumCols() + "\n");	
			buffer.append("spatial=" + Boolean.toString(getGrid().getGridSpatial()) + "\n");	
			buffer.append("}\n");
		}

		// For saving .gcm file before sending to dotty, omit the rest of this.
		if (includeGlobals) {
			buffer.append("Global {\n");
			for (String s : defaultParameters.keySet()) {
				if (globalParameters.containsKey(s)) {
					String value = globalParameters.get(s);
					buffer.append(s + "=" + value + "\n");
				}
			}
			buffer.append("compartment="+isWithinCompartment+"\n");
			buffer.append("}\nPromoters {\n");
			for (String s : promoters.keySet()) {
				if (collectGarbage) {
					//Only saves promoters belonging to influences
					Properties prop = promoters.get(s); 
					if (promotersWithInfluences.contains(s) ||
							(prop.containsKey("ExplicitPromoter") && prop.getProperty("ExplicitPromoter").equals("true"))) {
						buffer.append(s + " [");
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
				} else {
					//Saves all promoters
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
			}
			buffer.append("}\nConditions {\n");
			for (String s : conditions) {
				buffer.append(s + "\n");
			}
			buffer.append("}\n");

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
			if (appendSBML) {
				SBMLWriter writer = new SBMLWriter();
				String SBMLstr = writer.writeSBMLToString(sbml);
				buffer.append(GlobalConstants.SBMLFILE + "=\"" + sbmlFile + "\"\n");
				buffer.append("SBML="+SBMLstr);
			} else {
				buffer.append(GlobalConstants.SBMLFILE + "=\"" + sbmlFile + "\"\n");
			}
		}
		return buffer;
	}

	/**
	 * Save the current object to file.
	 * 
	 * @param filename
	 */
	public void save(String filename) {
		try {
			PrintStream p = new PrintStream(new FileOutputStream(filename));

			StringBuffer buffer = saveToBuffer(true, true, false);

			p.print(buffer);
			p.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * load the GCM file from a buffer.
	 */
	private void loadFromBuffer(StringBuffer data) {
		
		species = new HashMap<String, Properties>();
		reactions = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		grid = new Grid();
		
		loadDefaultParameters();
		
		try {
			parseStates(data);
			boolean complexConversion = parseInfluences(data);
			parseGlobal(data);
			parsePromoters(data);
			parseSBMLFile(data);
			parseConditions(data);
			parseGridSize(data);
			
			if (complexConversion) {
				save(this.filename);
				load(this.filename);
			}
			usedIDs = SBMLutilities.CreateListOfUsedIDs(sbml);
			usedIDs.addAll(components.keySet());
			usedIDs.addAll(promoters.keySet());
			usedIDs.addAll(parameters.keySet());
			//usedIDs.addAll(gcm.getSpecies().keySet());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void load(String filename) {
		while (filename.endsWith(".temp")) {
			filename = filename.substring(0, filename.length() - 5);
		}
		this.filename = filename;

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
		loadFromBuffer(data);

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
				// If you don't remove the promoter, it will end up in the
				// properties list twice, one with the old value and one with
				// the new.
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
		for (String c : components.keySet()) {
			for (Object key : components.get(c).keySet()) {
				if (components.get(c).getProperty((String) key).equals(oldName)) {
					components.get(c).put(key, newName);
				}
			}
		}
		conditions = newConditions;
		species.put(newName, species.get(oldName));
		species.remove(oldName);
		if (sbml != null) {
			if (sbml.getModel() != null) {
				SBMLutilities.updateVarId(sbml, true, oldName, newName);
				if (sbml.getModel().getSpecies(oldName) != null) {
					sbml.getModel().getSpecies(oldName).setId(newName);
				}
			}
		}
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

	
	//ADD METHODS
	
	public void addSpecies(String name, Properties property) {
		species.put(name, property);
	}
	
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
			r.setCompartment(m.getCompartment(0).getId());
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

	public void addPromoter(String name, Properties properties) {
		promoters.put(name.replace("\"", ""), properties);
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
	public String addComponent(String name, Properties properties) {

		if (name == null) {
			name = createNewObjectName("C", components);
		}

		components.put(name, properties);
		usedIDs.add(name);
		return name;
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

	public String addCondition(String condition) {
		conditions.add(condition);
		return condition;
		/*
		 * boolean retval = true; String finalCond = ""; ArrayList<String> split
		 * = new ArrayList<String>(); String splitting = condition; while
		 * (splitting.contains("||")) { split.add(splitting.substring(0,
		 * splitting.indexOf("||"))); splitting =
		 * splitting.substring(splitting.indexOf("||") + 2); }
		 * split.add(splitting); for (String cond : split) { finalCond += "(";
		 * if (cond.split("->").length > 2) { return null; } String[] split2 =
		 * cond.split("->"); int countLeft1 = 0; int countRight1 = 0; int
		 * countLeft2 = 0; int countRight2 = 0; int index = 0; while (index <=
		 * split2[0].length()) { index = split2[0].indexOf('(', index); if
		 * (index == -1) { break; } countLeft1++; index++; } index = 0; while
		 * (index <= split2[0].length()) { index = split2[0].indexOf(')',
		 * index); if (index == -1) { break; } countRight1++; index++; } if
		 * (split2.length > 1) { index = 0; while (index <= split2[0].length())
		 * { index = split2[1].indexOf('(', index); if (index == -1) { break; }
		 * countLeft2++; index++; } index = 0; while (index <=
		 * split2[0].length()) { index = split2[1].indexOf(')', index); if
		 * (index == -1) { break; } countRight2++; index++; } } if ((countLeft1
		 * - countRight1) == (countRight2 - countLeft2)) { for (int i = 0; i <
		 * countLeft1 - countRight1; i++) { split2[0] += ")"; split2[1] = "(" +
		 * split2[1]; } } ArrayList<String> specs = new ArrayList<String>();
		 * ArrayList<Object[]> conLevel = new ArrayList<Object[]>(); for (String
		 * spec : species.keySet()) { specs.add(spec); ArrayList<String> level =
		 * new ArrayList<String>(); level.add("0");
		 * conLevel.add(level.toArray()); } ExprTree expr = new
		 * ExprTree(convertToLHPN(specs, conLevel)); expr.token =
		 * expr.intexpr_gettok(split2[0]); if (!split2[0].equals("")) { retval =
		 * (expr.intexpr_L(split2[0]) && retval); } else { expr = null; retval =
		 * false; } if (split2.length > 1) { if (retval) { finalCond += "(" +
		 * expr.toString() + ")->"; } expr = new ExprTree(convertToLHPN(specs,
		 * conLevel)); expr.token = expr.intexpr_gettok(split2[0]); if
		 * (!split2[0].equals("")) { retval = (expr.intexpr_L(split2[0]) &&
		 * retval); } else { expr = null; retval = false; } if (retval) {
		 * finalCond += "(" + expr.toString() + ")"; } } else if (retval) {
		 * finalCond += expr.toString(); } finalCond += ")||"; } finalCond =
		 * finalCond.substring(0, finalCond.length() - 2); if (retval) {
		 * conditions.add(finalCond); } if (retval) { return finalCond; } else {
		 * return null; }
		 */
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

	
	//REMOVAL METHODS
	
	/**
	 * erases everything in the model but doesn't touch anything file-related
	 */
	public void clear() {
		
		species = new HashMap<String, Properties>();
		reactions = new HashMap<String, Properties>();
		influences = new HashMap<String, Properties>();
		promoters = new HashMap<String, Properties>();
		components = new HashMap<String, Properties>();
		compartments = new HashMap<String, Properties>();
		conditions = new ArrayList<String>();
		globalParameters = new HashMap<String, String>();
		parameters = new HashMap<String, String>();
		isWithinCompartment = false;
		grid = new Grid();
		loadDefaultParameters();
	}
	
	public void removeSpecies(String name) {
		if (name != null && species.containsKey(name)) {
			species.remove(name);
			sbml.getModel().removeSpecies(name);
			speciesPanel.refreshSpeciesPanel(sbml);
		}
		while (usedIDs.contains(name)) {
			usedIDs.remove(name);
		}
	}

	public void removeReaction(String name) {
		if (name != null && reactions.containsKey(name)) {
			reactions.remove(name);
		}
	}

	public void removeSpeciesAndAssociations(String name) {
		checkRemoveSpeciesAssociations(name, true);
		removeSpecies(name);
	}

	
	//GET METHODS
	
	public HashMap<String, Properties> getSpecies() {
		return species;
	}

	public HashMap<String, Properties> getReactions() {
		return reactions;
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
	
	public ArrayList<String> getGeneticSpecies() {
		ArrayList<String> genetic = new ArrayList<String>();
		for (String spec : species.keySet()) {
			for (String influ : influences.keySet()) {
				if (influ.startsWith(spec + " ->") || influ.contains("-> " + spec + ",")
						|| influ.startsWith(spec + " -|") || influ.contains("-| " + spec + ",")
						|| influ.startsWith(spec + " x>") || influ.contains("x> " + spec + ",")) {
					genetic.add(spec);
					break;
				}
			}
		}
		return genetic;
	}
	
	public ArrayList<String> getBiochemicalSpecies() {
		ArrayList<String> inputs = getInputSpecies();
		ArrayList<String> genetic = new ArrayList<String>();
		ArrayList<String> biochemical = new ArrayList<String>();
		for (String spec : species.keySet()) {
			for (String influ : influences.keySet()) {
				if (influ.startsWith(spec + " ->") || influ.contains("-> " + spec + ",")
						|| influ.startsWith(spec + " -|") || influ.contains("-| " + spec + ",")
						|| influ.startsWith(spec + " x>") || influ.contains("x> " + spec + ",")) {
					genetic.add(spec);
					break;
				}
			}
			if (!genetic.contains(spec) && !inputs.contains(spec)) {
				biochemical.add(spec);
			}
		}
		return biochemical;
	}

	public HashMap<String, Properties> getComponents() {
		return components;
	}

	public HashMap<String, Properties> getCompartments() {
		return compartments;
	}

	/**
	 * returns all the ports in the gcm file matching type, which must be either
	 * GlobalConstants.INPUT or GlobalConstants.OUTPUT.
	 * 
	 * @param type
	 * @return
	 */
	public HashMap<String, Properties> getPorts(String type) {
		HashMap<String, Properties> out = new HashMap<String, Properties>();
		for (Object ko : this.getSpecies().keySet()) {
			String key = (String) ko;
			Properties prop = this.getSpecies().get(ko);
			if (prop.getProperty(GlobalConstants.TYPE).equals(type)) {
				out.put(key, prop);
			}
		}
		return out;
	}

	public void connectComponentAndSpecies(Properties comp, String port, String specID, String type) {
		comp.put(port, specID);
		comp.put("type_" + port, type);
		return;
	}

	/**
	 * Given a component and the name of a species, return true if that species
	 * is connected to that component. Optionally disconnect them as well.
	 */
	public boolean checkDisconnectComponentAndSpecies(Properties comp, String speciesId,
			boolean disconnect) {
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
	 * looks everywhere to see if the given species is connected to anything.
	 * Returns true if it is. Also will delete the connections if remove
	 * parameter is true. NOTE: This function does not check outside GCM files.
	 * Use speciesUsedInOtherGCM() for that.
	 * 
	 * @param remove
	 * @return
	 */
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
		for (String c : getComponents().keySet()) {
			if (checkDisconnectComponentAndSpecies(getComponents().get(c), name, remove))
				ret = true;
		}
		// TODO: also make sure speciesUsedInOtherGCM() gets called when needed.
		return ret;
	}

	public boolean speciesUsedInOtherGCM(String name) {
		if (species.get(name).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.INPUT)
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

	/**
	 * Checks to see if removing specie is okay
	 * 
	 * @param name
	 *            specie to remove
	 * @return true if specie is in no influences
	 */
	public boolean removeSpeciesCheck(String name) {
		return !checkRemoveSpeciesAssociations(name, false);
	}

	public boolean editSpeciesCheck(String name, String newType) {
		if (species.get(name).getProperty(GlobalConstants.TYPE).equals(newType)) {
			return true;
		}
		else if (species.get(name).getProperty(GlobalConstants.TYPE).equals(GlobalConstants.INPUT)
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
							return false;
						}
					}
				}
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
		while (usedIDs.contains(name)) {
			usedIDs.remove(name);
		}
	}

	public void removeComponent(String name) {
		if (name != null && components.containsKey(name)) {
			components.remove(name);
		}
		while (usedIDs.contains(name)) {
			usedIDs.remove(name);
		}
	}

	public void removeInfluence(String name) {
		if (name != null && influences.containsKey(name)) {
			influences.remove(name);
		}
	}

	public void changeInfluencePromoter(String oldInfluence, String oldPromoter, String newPromoter) {
		if (oldPromoter == null)
			oldPromoter = "default";
		String pattern = oldPromoter + "$";
		String newInfluence = oldInfluence.replaceFirst(pattern, newPromoter);
		Properties prop = influences.get(oldInfluence);

		prop.remove(GlobalConstants.PROMOTER);
		prop.setProperty(GlobalConstants.PROMOTER, newPromoter);
		if (prop.get(GlobalConstants.NAME) == oldInfluence) {
			prop.remove(GlobalConstants.NAME);
			prop.put(GlobalConstants.NAME, newPromoter);
		}

		removeInfluence(oldInfluence);

		influences.put(newInfluence, prop);
	}

	/**
	 * returns null if the given influence is allowed and false otherwise.
	 * Reasons an influence wouldn't be allowed is if it shadows an existing
	 * influence, is a no-influence and an influence already exists there, or a
	 * no-influence already exists there. If the influence isn't allowed,
	 * returns a reason why.
	 */
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
			while (getSpecies().containsKey(id) || usedIDs.contains(id));
		}
		Properties prop = new Properties();
		prop.setProperty(GlobalConstants.NAME, "");
		prop.setProperty("label", id);
		prop.setProperty("ID", id);
		prop.setProperty("Type", "internal");
		prop.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_SPECIES_WIDTH));
		prop.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_SPECIES_HEIGHT));
		centerVertexOverPoint(prop, x, y);
		getSpecies().put(id, prop);
		if (sbml != null) {
			Model m = sbml.getModel();
			Species s = m.createSpecies();
			s.setId(id);
			usedIDs.add(id);
			s.setCompartment(m.getCompartment(0).getId());
			s.setBoundaryCondition(false);
			s.setConstant(false);
			s.setInitialAmount(0);
			s.setHasOnlySubstanceUnits(true);
			speciesPanel.refreshSpeciesPanel(sbml);
		}
	}

	public void createReaction(String id, float x, float y) {
		if (id == null) {
			do {
				creatingReactionID++;
				id = "R" + String.valueOf(creatingReactionID);
			}
			while (getReactions().containsKey(id) || usedIDs.contains(id));
		}
		Properties prop = new Properties();
		prop.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_REACTION_WIDTH));
		prop.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_REACTION_HEIGHT));
		centerVertexOverPoint(prop, x, y);
		getReactions().put(id, prop);
		if (sbml != null) {
			Model m = sbml.getModel();
			Reaction r = m.createReaction();
			r.setId(id);
			r.setCompartment(m.getCompartment(0).getId());
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
	}

	private int creatingPromoterID = 0;

	public String createPromoter(String id, float x, float y, boolean is_explicit) {
		if (id == null) {
			do {
				creatingPromoterID++;
				id = "P" + String.valueOf(creatingPromoterID);
			}
			while (getPromoters().containsKey(id) || usedIDs.contains(id));
		}
		Properties prop = new Properties();
		prop.setProperty("ID", id);
		usedIDs.add(id);
		prop.setProperty("name", "");
		if (is_explicit) {
			prop.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_SPECIES_WIDTH));
			prop.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_SPECIES_HEIGHT));
			prop.setProperty(GlobalConstants.EXPLICIT_PROMOTER, GlobalConstants.TRUE);
			centerVertexOverPoint(prop, x, y);
		}
		getPromoters().put(id, prop);

		return id;
	}

	/**
	 * Given a properties list (species or components) and some coords, center
	 * over that point.
	 */
	public void centerVertexOverPoint(Properties prop, double x, double y) {
		x -= Double.parseDouble(prop.getProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH))) / 2.0;
		y -= Double.parseDouble(prop.getProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT))) / 2.0;
		prop.setProperty("graphx", String.valueOf(x));
		prop.setProperty("graphy", String.valueOf(y));
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

	public boolean influenceHasExplicitPromoter(String infName) {
		String promoterName = getInfluences().get(infName).getProperty(GlobalConstants.PROMOTER);
		Properties promoter = getPromoters().get(promoterName);
		if (promoter != null
				&& promoter.getProperty(GlobalConstants.EXPLICIT_PROMOTER, "").equals(
						GlobalConstants.TRUE))
			return true;
		else
			return false;
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

	public String[] getImplicitPromotersAsArray() {
		String[] s = new String[promoters.size()];
		int index = 0;
		for (String prom : promoters.keySet()) {
			Properties promProp = promoters.get(prom);
			if (!promProp.containsKey(GlobalConstants.EXPLICIT_PROMOTER)
					|| !promProp.getProperty(GlobalConstants.EXPLICIT_PROMOTER).equals(
							GlobalConstants.TRUE)) {
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

	public boolean globalParameterIsSet(String parameter) {
		return globalParameters.containsKey(parameter);
	}

	public String getParameter(String parameter) {
		if (globalParameters.containsKey(parameter)) {
			return globalParameters.get(parameter);
		}
		else {
			return defaultParameters.get(parameter);
		}
	}

	private String getProp(Properties props, String prop) {
		if (props.containsKey(prop))
			return props.getProperty(prop);
		else
			return getParameter(prop);
	}

	public void setParameter(String parameter, String value) {
		globalParameters.put(parameter, value);
		parameters.put(parameter, value);
	}

	public void setDefaultParameter(String parameter, String value) {
		defaultParameters.put(parameter, value);
	}

	public void removeParameter(String parameter) {
		globalParameters.remove(parameter);
	}

	
	//PARSE METHODS
	
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
				String prop = CompatibilityFixer.convertOLDName(propMatcher.group(1));
				if (propMatcher.group(3) != null) {
					properties.put(prop, propMatcher.group(3));
				}
				else {
					properties.put(prop, propMatcher.group(4));
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
			if (!properties.containsKey("ID") && properties.containsKey("label")) {
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
			else if (properties.containsKey("shape") && properties.getProperty("shape").equals("circle")){
				if (properties.containsKey(GlobalConstants.TYPE)) {
					properties.remove(GlobalConstants.TYPE);
				}
				reactions.put(name, properties);
			} else {
				species.put(name, properties);
			}
		}
	}

	private void parseSBMLFile(StringBuffer data) {
		Pattern pattern = Pattern.compile(SBMLFILE);
		Matcher matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			sbmlFile = matcher.group(1);
			if (!sbmlFile.equals("")) {
				if (new File(path + separator + sbmlFile).exists()) {
					sbml = Gui.readSBML(path + separator + sbmlFile);
				} 	
			}
		} 
		pattern = Pattern.compile(SBML);
		matcher = pattern.matcher(data.toString());
		if (matcher.find()) {
			String sbmlData = matcher.group();
			sbmlData = sbmlData.replaceFirst("SBML=","");
			SBMLReader reader = new SBMLReader();
			SBMLDocument document = reader.readSBMLFromString(sbmlData);
			sbml.setModel(document.getModel());
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
				if (matcher.group(1).equals("compartment")) {
					if (matcher.group(2).equals("true")) {
						isWithinCompartment=true;
					} else {
						isWithinCompartment=false;
					}
					continue;
				}
				String prop = CompatibilityFixer.convertOLDName(matcher.group(1));
				if (matcher.group(3) != null) {
					globalParameters.put(prop, matcher.group(3));
					parameters.put(prop, matcher.group(3));
				}
				else {
					globalParameters.put(prop, matcher.group(4));
					parameters.put(prop, matcher.group(4));
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
				String prop = CompatibilityFixer.convertOLDName(propMatcher.group(1));
				if (propMatcher.group(3) != null) {
					properties.put(prop, propMatcher.group(3));
				}
				else {
					properties.put(prop, propMatcher.group(4));
				}
			}
			promoters.put(name, properties);
		}
	}

	/**
	 * loads the grid size from the gcm file
	 * 
	 * @param data string data from a gcm file
	 */
	private void parseGridSize(StringBuffer data) {
		
		Pattern network = Pattern.compile(GRID);
		Matcher matcher = network.matcher(data.toString());
		
		if (!matcher.find()) return;
		
		String info = matcher.group(1);
		
		if (info != null) {
			
			String[] rowcolInfo = info.split("\n");
			
			String[] rowInfo = rowcolInfo[1].split("=");
			String[] colInfo = rowcolInfo[2].split("=");
			String[] spatialInfo = rowcolInfo[3].split("=");
			
			String row = rowInfo[1];
			String col = colInfo[1];
			String gridSpatial = spatialInfo[1];
						
			buildGrid(Integer.parseInt(row), Integer.parseInt(col), Boolean.parseBoolean(gridSpatial));
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

	private boolean parseInfluences(StringBuffer data) {
		Pattern pattern = Pattern.compile(REACTION);
		Pattern propPattern = Pattern.compile(PROPERTY);
		Matcher matcher = pattern.matcher(data.toString());
		HashMap<String, ArrayList<String>> actBioMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, Properties> actBioPropMap = new HashMap<String, Properties>();
		HashMap<String, ArrayList<String>> repBioMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, Properties> repBioPropMap = new HashMap<String, Properties>();
		ArrayList<Properties> actDimerList = new ArrayList<Properties>();
		ArrayList<Properties> repDimerList = new ArrayList<Properties>();
		boolean complexConversion = false;
		while (matcher.find()) {
			Matcher propMatcher = propPattern.matcher(matcher.group(6));
			Properties properties = new Properties();
			while (propMatcher.find()) {
				String prop = CompatibilityFixer.convertOLDName(checkCompabilityLoad(propMatcher.group(1)));
				if (propMatcher.group(3) != null) {
					properties.setProperty(prop, propMatcher.group(3));
					if (propMatcher.group(1).equalsIgnoreCase(GlobalConstants.PROMOTER)
							&& !promoters.containsKey(propMatcher.group(3))) {
						promoters.put(propMatcher.group(3).replaceAll("\"", ""), new Properties());
						properties.setProperty(GlobalConstants.PROMOTER, propMatcher.group(3)
								.replace("\"", "")); // for backwards
						// compatibility
					}
				}
				else {
					properties.put(prop, propMatcher.group(4));
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
						components.get(matcher.group(2)).put("type_" + properties.get("port"),
								properties.get("type"));
					}
					else {
						GCMFile file = new GCMFile(path);
						file.load(path + separator
								+ components.get(matcher.group(2)).getProperty("gcm"));
						if (file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE)
								.equals(GlobalConstants.INPUT)) {
							components.get(matcher.group(2)).put("type_" + properties.get("port"),
									"Input");
						}
						else if (file.getSpecies().get(properties.get("port")).get(
								GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)) {
							components.get(matcher.group(2)).put("type_" + properties.get("port"),
									"Output");
						}
					}
				}
				else {
					components.get(matcher.group(5)).put(properties.get("port"), matcher.group(2));
					if (properties.containsKey("type")) {
						components.get(matcher.group(5)).put("type_" + properties.get("port"),
								properties.get("type"));
					}
					else {
						GCMFile file = new GCMFile(path);
						file.load(path + separator + components.get(matcher.group(5)).getProperty("gcm"));
						if (file.getSpecies().get(properties.get("port")) != null) {
							if (file.getSpecies().get(properties.get("port")).get(GlobalConstants.TYPE)
									.equals(GlobalConstants.INPUT)) {
								components.get(matcher.group(5)).put("type_" + properties.get("port"),"Input");
							}
							else if (file.getSpecies().get(properties.get("port")).get(
									GlobalConstants.TYPE).equals(GlobalConstants.OUTPUT)) {
								components.get(matcher.group(5)).put("type_" + properties.get("port"),"Output");
							}
						}
					}
				}
			}
			else {
				String name = "";
				int nDimer = Integer
						.parseInt(getProp(properties, GlobalConstants.MAX_DIMER_STRING));
				if (properties.containsKey("arrowhead")) {
					if (properties.getProperty("arrowhead").indexOf("vee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.ACTIVATION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							complexConversion = true;
							String promoter = properties.getProperty(GlobalConstants.PROMOTER);
							if (actBioMap.containsKey(promoter)) {
								ArrayList<String> parts = actBioMap.get(promoter);
								parts.add(matcher.group(2));
							}
							else {
								ArrayList<String> parts = new ArrayList<String>();
								parts.add(matcher.group(2));
								actBioMap.put(promoter, parts);
							}
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							actBioPropMap.put(promoter, properties);
						}
						else if (nDimer > 1) {
							complexConversion = true;
							properties.setProperty(GlobalConstants.TRANSCRIPTION_FACTOR, matcher.group(2));
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							actDimerList.add(properties);
						}
						else {
							name = matcher.group(2) + " -> " + matcher.group(5);
						}
					}
					else if (properties.getProperty("arrowhead").indexOf("tee") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.REPRESSION);
						if (properties.containsKey(GlobalConstants.BIO)
								&& properties.get(GlobalConstants.BIO).equals("yes")) {
							complexConversion = true;
							String promoter = properties.getProperty(GlobalConstants.PROMOTER);
							if (repBioMap.containsKey(promoter)) {
								ArrayList<String> parts = repBioMap.get(promoter);
								parts.add(matcher.group(2));
							}
							else {
								ArrayList<String> parts = new ArrayList<String>();
								parts.add(matcher.group(2));
								repBioMap.put(promoter, parts);
							}
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							repBioPropMap.put(promoter, properties);
						}
						else if (nDimer > 1) {
							complexConversion = true;
							properties.setProperty(GlobalConstants.TRANSCRIPTION_FACTOR, matcher.group(2));
							properties.setProperty(GlobalConstants.GENE_PRODUCT, matcher.group(5));
							repDimerList.add(properties);
						}
						else {
							name = matcher.group(2) + " -| " + matcher.group(5);
						}
					}
					else if (properties.getProperty("arrowhead").indexOf("plus") != -1) {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.COMPLEX);
						name = matcher.group(2) + " +> " + matcher.group(5);
					}
					else {
						properties.setProperty(GlobalConstants.TYPE, GlobalConstants.NOINFLUENCE);
						name = matcher.group(2) + " x> " + matcher.group(5);
					}
				}
				if (!complexConversion) {
					if (properties.containsKey(GlobalConstants.PROMOTER)) {
						name = name + ", Promoter "
								+ properties.getProperty(GlobalConstants.PROMOTER);
					}
					else if (properties.getProperty(GlobalConstants.TYPE).equals("complex") || 
							properties.getProperty(GlobalConstants.TYPE).equals("no influence")){
						name = name + ", Promoter " + "none";
					}
					//Instantiates default promoter
					else {
						String defaultPromoterName = "Promoter_" + getOutput(name);
						name = name + ", Promoter " + defaultPromoterName;
						createPromoter(defaultPromoterName, 0, 0, false);
						properties.setProperty(GlobalConstants.PROMOTER, defaultPromoterName);
					}
					String label = properties.getProperty(GlobalConstants.PROMOTER);
					if (label == null) {
						label = "";
					}
					// if (properties.containsKey(GlobalConstants.BIO)
					// && properties.get(GlobalConstants.BIO).equals("yes")) {
					// label = label + "+";
					// }
					properties.setProperty("label", "\"" + label + "\"");
					properties.setProperty(GlobalConstants.NAME, name);
					influences.put(name, properties);
				}
			}
		}
		// Parses mapped biochemical activation influences and adds them to the
		// gcm as complex influences
		parseBioInfluences(repBioMap, repBioPropMap);
		parseBioInfluences(actBioMap, actBioPropMap);
		// Parses collected dimer activation influences and adds them to the gcm
		// as complex influences
		parseDimerInfluences(repDimerList);
		parseDimerInfluences(actDimerList);
		// Removes local and global instances of old bio/dimer parameters from
		// gcm file
		for (Properties inflProp : influences.values()) {
			inflProp.remove(GlobalConstants.BIO);
			inflProp.remove(GlobalConstants.KBIO_STRING);
			inflProp.remove(GlobalConstants.MAX_DIMER_STRING);
		}
		for (Properties specProp : species.values()) {
			specProp.remove(GlobalConstants.KASSOCIATION_STRING);
		}
		globalParameters.remove(GlobalConstants.KBIO_STRING);
		globalParameters.remove(GlobalConstants.MAX_DIMER_STRING);
		globalParameters.remove(GlobalConstants.KASSOCIATION_STRING);

		return complexConversion;
	}

	// Parses mapped biochemical repression influences and adds them to the gcm
	// as complex influences
	private void parseBioInfluences(HashMap<String, ArrayList<String>> bioMap,
			HashMap<String, Properties> bioPropMap) {
		for (String promoter : bioMap.keySet()) {
			// Adds activation or repression influence with complex species as
			// input
			String complex = "";
			ArrayList<String> parts = bioMap.get(promoter);
			for (String part : parts) {
				complex = complex + part;
			}
			Properties inflProp = bioPropMap.get(promoter);
			String influence = complex;
			if (inflProp.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION))
				influence = influence + " -> ";
			else
				influence = influence + " -| ";
			influence = influence + inflProp.getProperty(GlobalConstants.GENE_PRODUCT)
					+ ", Promoter ";
			if (inflProp.containsKey(GlobalConstants.PROMOTER)) {
				influence = influence + inflProp.getProperty(GlobalConstants.PROMOTER);
			} 
			//Instantiates default promoter
			else {
				String defaultPromoterName = "Promoter_" + inflProp.getProperty(GlobalConstants.GENE_PRODUCT);
				influence = influence + defaultPromoterName;
				createPromoter(defaultPromoterName, 0, 0, false);
				inflProp.setProperty(GlobalConstants.PROMOTER, defaultPromoterName);
			}
			String label = inflProp.getProperty(GlobalConstants.PROMOTER);
			label = "\"" + label + "\"";
			inflProp.setProperty("label", label);
			inflProp.setProperty(GlobalConstants.NAME, influence);
			influences.put(influence, inflProp);
			// Adds complex species
			Properties compProp = new Properties();
			compProp.setProperty(GlobalConstants.NAME, complex);
			compProp.setProperty(GlobalConstants.TYPE, GlobalConstants.INTERNAL);
			compProp.setProperty(GlobalConstants.KCOMPLEX_STRING, getProp(inflProp,
					GlobalConstants.KBIO_STRING));
			compProp.setProperty(GlobalConstants.INITIAL_STRING, "0");
			compProp.setProperty(GlobalConstants.KDECAY_STRING, "0");
			species.put(complex, compProp);
			// Adds complex formation influences with biochemical species as
			// inputs
			for (String part : parts) {
				String compInfluence = part + " +> " + complex;
				Properties compFormProp = new Properties();
				compFormProp.setProperty("label", "\"\"");
				compFormProp.setProperty(GlobalConstants.NAME, compInfluence);
				compFormProp.setProperty(GlobalConstants.TYPE, GlobalConstants.COMPLEX);
				compFormProp.setProperty(GlobalConstants.COOPERATIVITY_STRING, "1");
				influences.put(compInfluence, compFormProp);
			}
		}
	}

	private void parseDimerInfluences(ArrayList<Properties> dimerList) {
		for (Properties inflProp : dimerList) {
			// Adds activation or repression influence with complex species as
			// input
			String monomer = getProp(inflProp, GlobalConstants.TRANSCRIPTION_FACTOR);
			String nDimer = getProp(inflProp, GlobalConstants.MAX_DIMER_STRING);
			String complex = monomer + "_" + nDimer;
			String influence = complex;
			if (inflProp.getProperty(GlobalConstants.TYPE).equals(GlobalConstants.ACTIVATION))
				influence = influence + " -> ";
			else
				influence = influence + " -| ";
			influence = influence + inflProp.getProperty(GlobalConstants.GENE_PRODUCT)
					+ ", Promoter ";
			if (inflProp.containsKey(GlobalConstants.PROMOTER)) {
				
				influence = influence + inflProp.getProperty(GlobalConstants.PROMOTER);
			} 
			//Instantiates default promoter
			else {
				String defaultPromoterName = "Promoter_" + inflProp.getProperty(GlobalConstants.GENE_PRODUCT);
				influence = influence + defaultPromoterName;
				createPromoter(defaultPromoterName, 0, 0, false);
				inflProp.setProperty(GlobalConstants.PROMOTER, defaultPromoterName);
			}
			String label = inflProp.getProperty(GlobalConstants.PROMOTER);
			label = "\"" + label + "\"";
			inflProp.put(GlobalConstants.NAME, influence);
			influences.put(influence, inflProp);
			// Adds complex species
			Properties compProp = new Properties();
			compProp.setProperty(GlobalConstants.NAME, complex);
			compProp.setProperty(GlobalConstants.TYPE, GlobalConstants.INTERNAL);
			compProp.setProperty(GlobalConstants.KCOMPLEX_STRING, getProp(species.get(monomer),
					GlobalConstants.KASSOCIATION_STRING));
			compProp.setProperty(GlobalConstants.INITIAL_STRING, "0");
			compProp.setProperty(GlobalConstants.KDECAY_STRING, "0");
			species.put(complex, compProp);
			// Adds complex formation influence with monomer as input
			String compInfluence = monomer + " +> " + complex;
			Properties compFormProp = new Properties();
			compFormProp.setProperty("label", "\"\"");
			compFormProp.setProperty(GlobalConstants.NAME, compInfluence);
			compFormProp.setProperty(GlobalConstants.TYPE, GlobalConstants.COMPLEX);
			compFormProp.setProperty(GlobalConstants.COOPERATIVITY_STRING, nDimer);
			influences.put(compInfluence, compFormProp);
		}
	}

	public void loadDefaultParameters() {
		Preferences biosimrc = Preferences.userRoot();
		defaultParameters = new HashMap<String, String>();
		
		defaultParameters.put(GlobalConstants.KDECAY_STRING, biosimrc.get(
				"biosim.gcm.KDECAY_VALUE", ""));
		defaultParameters.put(GlobalConstants.KECDECAY_STRING, biosimrc.get(
				"biosim.gcm.KECDECAY_VALUE", ""));
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
		defaultParameters.put(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING, biosimrc.get(
				"biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ""));
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
		defaultParameters.put(GlobalConstants.KCOMPLEX_STRING, biosimrc.get(
				"biosim.gcm.KCOMPLEX_VALUE", ""));
		defaultParameters.put(GlobalConstants.MEMDIFF_STRING, biosimrc.get(
				"biosim.gcm.MEMDIFF_VALUE", ""));
		defaultParameters.put(GlobalConstants.KECDIFF_STRING, biosimrc.get(
				"biosim.gcm.KECDIFF_VALUE", ""));

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

	private SBMLDocument unionSBML(SBMLDocument mainDoc, SBMLDocument doc, String compName, HashMap<String, Properties> components,
			boolean isWithinCompartment, String RNAPamount) {
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
				String topComp = mainDoc.getModel().getCompartment(0).getId();
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
		for (int i = 0; i < m.getNumSpecies(); i++) {
			Species spec = m.getSpecies(i);
			String newName = compName + "__" + spec.getId();
			for (Object port : components.get(compName).keySet()) {
				if (spec.getId().equals((String) port)) {
					newName = "_" + compName + "__" + components.get(compName).getProperty((String) port);
				}
			}
			updateVarId(true, spec.getId(), newName, doc);
			spec.setId(newName);
		}
		for (int i = 0; i < m.getNumSpecies(); i++) {
			Species spec = m.getSpecies(i);
			boolean add = true;
			if (spec.getId().startsWith("_" + compName + "__")) {
				updateVarId(true, spec.getId(), spec.getId().substring(3 + compName.length()), doc);
				spec.setId(spec.getId().substring(3 + compName.length()));
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
			String newName = compName + "__" + p.getId();
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
			mainDoc.getModel().addConstraint(constraint);
		}
		for (int i = 0; i < m.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) m.getListOfEvents().get(i);
			String newName = compName + "__" + event.getId();
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
				}
			}
			if (add) {
				mainDoc.getModel().addEvent(event);
			}
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
		StringBuffer up = this.saveToBuffer(true, false, true);
		undoManager.makeUndoPoint(up);
	}

	public void undo() {
		StringBuffer p = (StringBuffer) undoManager.undo();
		if (p != null)
			this.loadFromBuffer(p);
	}

	public void redo() {
		StringBuffer p = (StringBuffer) undoManager.redo();
		if (p != null)
			this.loadFromBuffer(p);
	}
	
	
	//GRID

	public Grid getGrid() {
		return grid;
	}
	
	public void setGrid(Grid g) {
		grid = g;
	}	
	
	public void buildGrid(int rows, int cols, boolean gridSpatial) {
		
		grid.createGrid(rows, cols, this, null, gridSpatial);
	}
	
	
	//CONSTANTS AND VARIABLES
	
	private static final String NETWORK = "digraph\\sG\\s\\{([^}]*)\\s\\}";

	private static final String STATE = "(^|\\n) *([^- \\n]*) *\\[(.*)\\]";

	private static final String REACTION = "(^|\\n) *([^ \\n]*) (\\-|\\+|x)(\\>|\\|) *([^ \n]*) *\\[([^\\]]*)]";

	// private static final String PARSE = "(^|\\n) *([^ \\n,]*) *\\-\\> *([^
	// \n,]*)";

	private static final String PARSE = "(^|\\n) *([^ \\n,]*) (\\-|\\+|x)(\\>|\\|) *([^ \n,]*)(, Promoter ([a-zA-Z\\d_]+))?";

	private static final String PROPERTY = "([a-zA-Z\\ \\-]+)=(\"([^\"]*)\"|([^\\s,]+))";

	private static final String GLOBAL = "Global\\s\\{([^}]*)\\s\\}";

	private static final String CONDITION = "Conditions\\s\\{([^@]*)\\s\\}";

	private static final String SBMLFILE = GlobalConstants.SBMLFILE + "=\"([^\"]*)\"";

	private static final String SBML = "SBML=((.*)\n)*";

	private static final String PROMOTERS_LIST = "Promoters\\s\\{([^}]*)\\s\\}";
	
	private static final String GRID = "Grid\\s\\{([^}]*)\\s\\}";

	// private static final String COMPONENTS_LIST =
	// "Components\\s\\{([^}]*)\\s\\}";

	private String sbmlFile = "";
	
	private SBMLDocument sbml = null;
	
	private MySpecies speciesPanel = null;
	
	private Reactions reactionPanel = null;
	
	private Grid grid = null;

	private HashMap<String, Properties> species;

	private HashMap<String, Properties> reactions;

	private HashMap<String, Properties> influences;

	private HashMap<String, Properties> promoters;

	private HashMap<String, Properties> components;

	private HashMap<String, Properties> compartments;

	private ArrayList<String> conditions;

	private HashMap<String, String> parameters;

	private HashMap<String, String> defaultParameters;

	private HashMap<String, String> globalParameters;
	
	private boolean isWithinCompartment;

	private ArrayList<String >usedIDs;
	
	private String path;

}
