package frontend.verification;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import backend.verification.platu.main.Options;
import backend.verification.platu.project.Project;
import backend.verification.timed_state_exploration.zoneProject.Zone;
import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.Utility;
import dataModels.lpn.parser.Abstraction;
import dataModels.lpn.parser.LPN;
import dataModels.lpn.parser.Place;
import dataModels.lpn.parser.Transition;
import dataModels.lpn.parser.Variable;
import dataModels.lpn.parser.LpnDecomposition.Component;
import dataModels.lpn.parser.LpnDecomposition.LpnComponentList;
import dataModels.lpn.parser.LpnDecomposition.LpnProcess;
import dataModels.util.GlobalConstants;
import frontend.biomodel.gui.util.PropertyList;
import frontend.main.Gui;
import frontend.main.Log;




/**
 * This class creates a GUI front end for the Verification tool. It provides the
 * necessary options to run an atacs simulation of the circuit and manage the
 * results from the BioSim GUI.
 * 
 * @author Kevin Jones
 */

public class Verification extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton save, run, viewCircuit, viewTrace, viewLog, addComponent,
	removeComponent, addSFile, addLPN, removeLPN;

	private JLabel algorithm, timingMethod, timingOptions, otherOptions,
	otherOptions2, compilation, bddSizeLabel, advTiming, abstractLabel,
	listLabel;

	public JRadioButton untimed, geometric, posets, bag, bap, baptdc, verify,
	vergate, orbits, search, trace, bdd, dbm, smt, untimedStateSearch, lhpn, view, none,
	simplify, abstractLhpn, octagon, zone;
	//dbm2

	private JCheckBox abst, partialOrder, dot, verbose, graph, decomposeLPN, multipleLPNs, genrg,
	timsubset, superset, infopt, orbmatch, interleav, prune, disabling,
	nofail, noproj, keepgoing, explpn, nochecks, reduction, newTab,
	postProc, redCheck, xForm2, expandRate, useGraphs, resetOnce, noDisplayResult,
	rateOptimization, displayDBM;

	private JTextField bddSize, backgroundField, componentField;

	private JList sList;

	private DefaultListModel sListModel;

	private ButtonGroup timingMethodGroup, algorithmGroup, abstractionGroup;

	private String directory, separator, root, verFile, oldBdd,
	sourceFileNoPath;

	public String verifyFile;

	private boolean change, atacs, lema;

	private PropertyList componentList, lpnList;

	private AbstPane abstPane;

	private Log log;

	private Gui biosim;

	/**
	 * This is the constructor for the Verification class. It initializes all
	 * the input fields, puts them on panels, adds the panels to the frame, and
	 * then displays the frame.
	 */
	public Verification(String directory, String verName, String filename,
			Log log, Gui biosim, boolean lema, boolean atacs) {
		separator = GlobalConstants.separator;
		this.atacs = atacs;
		this.lema = lema;
		this.biosim = biosim;
		this.log = log;
		this.directory = directory;
		verFile = verName + ".ver";
		String[] tempArray = filename.split("\\.");
		String traceFilename = tempArray[0] + ".trace";
		File traceFile = new File(traceFilename);
		String[] tempDir = directory.split(separator);
		root = tempDir[0];
		for (int i = 1; i < tempDir.length - 1; i++) {
			root = root + separator + tempDir[i];
		}
		this.setMaximumSize(new Dimension(300,300));
		this.setMinimumSize(new Dimension(300,300));

		JPanel abstractionPanel = new JPanel();
		abstractionPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel timingRadioPanel = new JPanel();
		timingRadioPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel timingCheckBoxPanel = new JPanel();
		timingCheckBoxPanel.setMaximumSize(new Dimension(1000, 30));
		JPanel otherPanel = new JPanel();
		otherPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel preprocPanel = new JPanel();
		preprocPanel.setMaximumSize(new Dimension(1000, 700));
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel buttonPanel = new JPanel();
		JPanel compilationPanel = new JPanel();
		compilationPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel advancedPanel = new JPanel();
		advancedPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel bddPanel = new JPanel();
		bddPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel pruningPanel = new JPanel();
		pruningPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel advTimingPanel = new JPanel();
		advTimingPanel.setMaximumSize(new Dimension(1000, 62));
		advTimingPanel.setPreferredSize(new Dimension(1000, 62));

		bddSize = new JTextField("");
		bddSize.setPreferredSize(new Dimension(40, 18));
		oldBdd = bddSize.getText();
		componentField = new JTextField("");
		componentList = new PropertyList("");
		lpnList = new PropertyList("");

		abstractLabel = new JLabel("Abstraction:");
		algorithm = new JLabel("Verification Algorithm:");
		timingMethod = new JLabel("Timing Method:");
		timingOptions = new JLabel("Timing Options:");
		otherOptions = new JLabel("Other Options:");
		otherOptions2 = new JLabel("Other Options:");
		compilation = new JLabel("Compilation Options:");
		bddSizeLabel = new JLabel("BDD Linkspace Size:");
		advTiming = new JLabel("Timing Options:");
		//preprocLabel = new JLabel("Preprocess Command:");
		listLabel = new JLabel("Assembly Files:");
		JPanel labelPane = new JPanel();
		labelPane.add(listLabel);
		sListModel = new DefaultListModel();
		sList = new JList(sListModel);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(sList);
		JPanel scrollPane = new JPanel();
		scrollPane.add(scroll);
		addSFile = new JButton("Add File");
		addSFile.addActionListener(this);
		JPanel buttonPane = new JPanel();
		buttonPane.add(addSFile);
		//preprocStr = new JTextField();
		//preprocStr.setPreferredSize(new Dimension(500, 18));

		// Initializes the radio buttons and check boxes
		// Abstraction Options
		none = new JRadioButton("None");
		simplify = new JRadioButton("Simplification");
		abstractLhpn = new JRadioButton("Abstraction");
		// Timing Methods
		if (atacs) {
			untimed = new JRadioButton("Untimed");
			geometric = new JRadioButton("Geometric");
			posets = new JRadioButton("POSETs");
			bag = new JRadioButton("BAG");
			bap = new JRadioButton("BAP");
			baptdc = new JRadioButton("BAPTDC");
			untimed.addActionListener(this);
			geometric.addActionListener(this);
			posets.addActionListener(this);
			bag.addActionListener(this);
			bap.addActionListener(this);
			baptdc.addActionListener(this);
		} else {
			untimedStateSearch = new JRadioButton("Untimed");
			bdd = new JRadioButton("BDD");
			dbm = new JRadioButton("Zone (C)");
			smt = new JRadioButton("SMT");
			//dbm2 = new JRadioButton("DBM2");
			octagon = new JRadioButton("Octagon");
			zone = new JRadioButton("Zone (Java)");
			bdd.addActionListener(this);
			dbm.addActionListener(this);
			smt.addActionListener(this);
			//dbm2.addActionListener(this);
			octagon.addActionListener(this);
			zone.addActionListener(this);
		}
		lhpn = new JRadioButton("LPN");
		view = new JRadioButton("View");
		lhpn.addActionListener(this);
		view.addActionListener(this);
		// Basic Timing Options
		abst = new JCheckBox("Abstract");
		partialOrder = new JCheckBox("Partial Order");
		abst.addActionListener(this);
		partialOrder.addActionListener(this);
		// Other Basic Options
		dot = new JCheckBox("Dot");
		verbose = new JCheckBox("Verbose");
		graph = new JCheckBox("Show State Graph");
		//untimedPOR = new JCheckBox("Use Partial Orders");
		decomposeLPN = new JCheckBox("Decompose LPN into components");
		multipleLPNs = new JCheckBox("Multiple LPNs");
		dot.addActionListener(this);
		verbose.addActionListener(this);
		graph.addActionListener(this);
		//untimedPOR.addActionListener(this);
		decomposeLPN.addActionListener(this);
		multipleLPNs.addActionListener(this);
		// Verification Algorithms
		verify = new JRadioButton("Verify");
		vergate = new JRadioButton("Verify Gates");
		orbits = new JRadioButton("Orbits");
		search = new JRadioButton("Search");
		trace = new JRadioButton("Trace");
		verify.addActionListener(this);
		vergate.addActionListener(this);
		orbits.addActionListener(this);
		search.addActionListener(this);
		trace.addActionListener(this);
		// Compilations Options
		newTab = new JCheckBox("New Tab");
		postProc = new JCheckBox("Post Processing");
		redCheck = new JCheckBox("Redundancy Check");
		xForm2 = new JCheckBox("Don't Use Transform 2");
		expandRate = new JCheckBox("Expand Rate");
		newTab.addActionListener(this);
		postProc.addActionListener(this);
		redCheck.addActionListener(this);
		xForm2.addActionListener(this);
		expandRate.addActionListener(this);
		// Advanced Timing Options
		genrg = new JCheckBox("Generate RG");
		timsubset = new JCheckBox("Subsets");
		superset = new JCheckBox("Supersets");
		infopt = new JCheckBox("Infinity Optimization");
		orbmatch = new JCheckBox("Orbits Match");
		interleav = new JCheckBox("Interleave");
		prune = new JCheckBox("Prune");
		disabling = new JCheckBox("Disabling");
		nofail = new JCheckBox("No fail");
		noproj = new JCheckBox("No project");
		keepgoing = new JCheckBox("Keep going");
		explpn = new JCheckBox("Expand LPN");
		useGraphs = new JCheckBox("Use Graph Storage");
		genrg.addActionListener(this);
		timsubset.addActionListener(this);
		superset.addActionListener(this);
		infopt.addActionListener(this);
		orbmatch.addActionListener(this);
		interleav.addActionListener(this);
		prune.addActionListener(this);
		disabling.addActionListener(this);
		nofail.addActionListener(this);
		noproj.addActionListener(this);
		keepgoing.addActionListener(this);
		explpn.addActionListener(this);
		useGraphs.addActionListener(this);
		// Other Advanced Options
		nochecks = new JCheckBox("No checks");
		reduction = new JCheckBox("Reduction");
		nochecks.addActionListener(this);
		reduction.addActionListener(this);
		resetOnce = new JCheckBox("Reset Once");
		resetOnce.addActionListener(this);
		noDisplayResult = new JCheckBox("Silence Verification Window");
		noDisplayResult.addActionListener(this);
		displayDBM = new JCheckBox("Show DBM in State Graph");
		displayDBM.addActionListener(this);
		rateOptimization = new JCheckBox("Rate Optimization On");
		rateOptimization.addActionListener(this);
		// Component List
		addComponent = new JButton("Add Component");
		removeComponent = new JButton("Remove Component");
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel componentPanel = Utility.createPanel(this, "Components",
				componentList, addComponent, removeComponent, null);
		constraints.gridx = 0;
		constraints.gridy = 1;

		// LPN List
		addLPN = new JButton("Add LPN");
		removeLPN = new JButton("Remove LPN");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		JPanel LPNPanel = Utility.createPanel(this, "LPNs",
				lpnList, addLPN, removeLPN, null);
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;

		abstractionGroup = new ButtonGroup();
		timingMethodGroup = new ButtonGroup();
		algorithmGroup = new ButtonGroup();

		none.setSelected(true);
		if (lema) {
			dbm.setSelected(true);
		} else if (atacs) {
			untimed.setSelected(true);
		}
		verify.setSelected(true);

		// Groups the radio buttons
		abstractionGroup.add(none);
		abstractionGroup.add(simplify);
		abstractionGroup.add(abstractLhpn);
		if (lema) {
			timingMethodGroup.add(untimedStateSearch);
			timingMethodGroup.add(zone);
			timingMethodGroup.add(bdd);
			timingMethodGroup.add(dbm);
			timingMethodGroup.add(smt);
			//timingMethodGroup.add(dbm2);
			timingMethodGroup.add(octagon);
		} else {
			timingMethodGroup.add(untimed);
			timingMethodGroup.add(geometric);
			timingMethodGroup.add(posets);
			timingMethodGroup.add(bag);
			timingMethodGroup.add(bap);
			timingMethodGroup.add(baptdc);
		}
		timingMethodGroup.add(lhpn);
		timingMethodGroup.add(view);
		algorithmGroup.add(verify);
		algorithmGroup.add(vergate);
		algorithmGroup.add(orbits);
		algorithmGroup.add(search);
		algorithmGroup.add(trace);

		JPanel basicOptions = new JPanel();
		JPanel advOptions = new JPanel();

		// Adds the buttons to their panels
		abstractionPanel.add(abstractLabel);
		abstractionPanel.add(none);
		abstractionPanel.add(simplify);
		abstractionPanel.add(abstractLhpn);
		timingRadioPanel.add(timingMethod);
		if (atacs) {
			timingRadioPanel.add(untimed);
			timingRadioPanel.add(geometric);
			timingRadioPanel.add(posets);
			timingRadioPanel.add(bag);
			timingRadioPanel.add(bap);
			timingRadioPanel.add(baptdc);
		} else {
			timingRadioPanel.add(untimedStateSearch);
			timingRadioPanel.add(bdd);
			timingRadioPanel.add(smt);
			timingRadioPanel.add(dbm);
			timingRadioPanel.add(zone);
			//timingRadioPanel.add(dbm2);
			timingRadioPanel.add(octagon);
		}
		timingRadioPanel.add(lhpn);
		timingRadioPanel.add(view);

		timingCheckBoxPanel.add(timingOptions);
		timingCheckBoxPanel.add(abst);
		timingCheckBoxPanel.add(partialOrder);

		otherPanel.add(otherOptions);
		otherPanel.add(dot);
		otherPanel.add(verbose);
		otherPanel.add(graph);
		//otherPanel.add(untimedPOR);
		otherPanel.add(decomposeLPN);
		otherPanel.add(multipleLPNs);

		preprocPanel.add(labelPane);
		preprocPanel.add(scrollPane);
		preprocPanel.add(buttonPane);

		algorithmPanel.add(algorithm);
		algorithmPanel.add(verify);
		algorithmPanel.add(vergate);
		algorithmPanel.add(orbits);
		algorithmPanel.add(search);
		algorithmPanel.add(trace);

		compilationPanel.add(compilation);
		compilationPanel.add(newTab);
		compilationPanel.add(postProc);
		compilationPanel.add(redCheck);
		compilationPanel.add(xForm2);
		compilationPanel.add(expandRate);

		advTimingPanel.add(advTiming);
		advTimingPanel.add(genrg);
		advTimingPanel.add(timsubset);
		advTimingPanel.add(superset);
		advTimingPanel.add(infopt);
		advTimingPanel.add(orbmatch);
		advTimingPanel.add(interleav);
		advTimingPanel.add(prune);
		advTimingPanel.add(disabling);
		advTimingPanel.add(nofail);
		advTimingPanel.add(noproj);
		advTimingPanel.add(keepgoing);
		advTimingPanel.add(explpn);
		advTimingPanel.add(useGraphs);

		advancedPanel.add(otherOptions2);
		advancedPanel.add(nochecks);
		advancedPanel.add(reduction);
		advancedPanel.add(resetOnce);
		advancedPanel.add(noDisplayResult);
		advancedPanel.add(displayDBM);
		advancedPanel.add(rateOptimization);
		
		bddPanel.add(bddSizeLabel);
		bddPanel.add(bddSize);

		// load parameters
		Properties load = new Properties();
		verifyFile = "";
		try {
			FileInputStream in = new FileInputStream(new File(directory
					+ separator + verFile));
			load.load(in);
			in.close();
			if (load.containsKey("verification.file")) {
				verifyFile = load.getProperty("verification.file");
			}
			if (load.containsKey("verification.bddSize")) {
				bddSize.setText(load.getProperty("verification .bddSize"));
			}
			if (load.containsKey("verification.component")) {
				componentField.setText(load
						.getProperty("verification.component"));
			}
			Integer i = 0;
			while (load.containsKey("verification.compList" + i.toString())) {
				componentList.addItem(load.getProperty("verification.compList"
						+ i.toString()));
				i++;
			}
			Integer k = 0;
			while (load.containsKey("verification.lpnList" + k.toString())) {
				lpnList.addItem(load.getProperty("verification.lpnList" + k.toString()));
				k++;
			}
			if (load.containsKey("verification.abstraction")) {
				if (load.getProperty("verification.abstraction").equals("none")) {
					none.setSelected(true);
				} else if (load.getProperty("verification.abstraction").equals(
						"simplify")) {
					simplify.setSelected(true);
				} else {
					abstractLhpn.setSelected(true);
				}
			}
			abstPane = new AbstPane(root + separator + verName, this, log);
			if (load.containsKey("verification.timing.methods")) {
				if (atacs) {
					if (load.getProperty("verification.timing.methods").equals("untimed")) {
						untimed.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("geometric")) {
						geometric.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("posets")) {
						posets.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("bag")) {
						bag.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("bap")) {
						bap.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("baptdc")) {
						baptdc.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("lhpn")) {
						lhpn.setSelected(true);
					} else {
						view.setSelected(true);
					}
				} else {
					if (load.getProperty("verification.timing.methods").equals("bdd")) {
						bdd.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("dbm")) {
						dbm.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("smt")) {
						smt.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("untimedStateSearch")) {
						untimedStateSearch.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("lhpn")) {
						lhpn.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("zone")) {
						zone.setSelected(true);
					} else if (load.getProperty("verification.timing.methods").equals("octagon")) {
						octagon.setSelected(true);
					} else {
						view.setSelected(true);
					}
				}
			}
			if (load.containsKey("verification.Abst")) {
				if (load.getProperty("verification.Abst").equals("true")) {
					abst.setSelected(true);
				}
			}
			if (load.containsKey("verification.partial.order")) {
				if (load.getProperty("verification.partial.order").equals(
						"true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("verification.Dot")) {
				if (load.getProperty("verification.Dot").equals("true")) {
					dot.setSelected(true);
				}
			}
			if (load.containsKey("verification.Verb")) {
				if (load.getProperty("verification.Verb").equals("true")) {
					verbose.setSelected(true);
				}
			}
			if (load.containsKey("verification.Graph")) {
				if (load.getProperty("verification.Graph").equals("true")) {
					graph.setSelected(true);
				}
			}
//			if (load.containsKey("verification.UntimedPOR")) {
//				if (load.getProperty("verification.UntimedPOR").equals("true")) {
//					untimedPOR.setSelected(true);
//				}
//			}
			if (load.containsKey("verification.DecomposeLPN")) {
				if (load.getProperty("verification.DecomposeLPN").equals("true")) {
					decomposeLPN.setSelected(true);
				}
			}
			if (load.containsKey("verification.MultipleLPNs")) {
				if (load.getProperty("verification.MultipleLPNs").equals("true")) {
					multipleLPNs.setSelected(true);
				}
			}
			if (load.containsKey("verification.partial.order")) {
				if (load.getProperty("verification.partial.order").equals(
						"true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("verification.partial.order")) {
				if (load.getProperty("verification.partial.order").equals(
						"true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("verification.algorithm")) {
				if (load.getProperty("verification.algorithm").equals("verify")) {
					verify.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"vergate")) {
					vergate.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"orbits")) {
					orbits.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"search")) {
					search.setSelected(true);
				} else if (load.getProperty("verification.algorithm").equals(
						"trace")) {
					trace.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.newTab")) {
				if (load.getProperty("verification.compilation.newTab").equals(
						"true")) {
					newTab.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.postProc")) {
				if (load.getProperty("verification.compilation.postProc")
						.equals("true")) {
					postProc.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.redCheck")) {
				if (load.getProperty("verification.compilation.redCheck")
						.equals("true")) {
					redCheck.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.xForm2")) {
				if (load.getProperty("verification.compilation.xForm2").equals(
						"true")) {
					xForm2.setSelected(true);
				}
			}
			if (load.containsKey("verification.compilation.expandRate")) {
				if (load.getProperty("verification.compilation.expandRate")
						.equals("true")) {
					expandRate.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.genrg")) {
				if (load.getProperty("verification.timing.genrg")
						.equals("true")) {
					genrg.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.subset")) {
				if (load.getProperty("verification.timing.subset").equals(
						"true")) {
					timsubset.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.superset")) {
				if (load.getProperty("verification.timing.superset").equals(
						"true")) {
					superset.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.infopt")) {
				if (load.getProperty("verification.timing.infopt").equals(
						"true")) {
					infopt.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.orbmatch")) {
				if (load.getProperty("verification.timing.orbmatch").equals(
						"true")) {
					orbmatch.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.interleav")) {
				if (load.getProperty("verification.timing.interleav").equals(
						"true")) {
					interleav.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.prune")) {
				if (load.getProperty("verification.timing.prune")
						.equals("true")) {
					prune.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.disabling")) {
				if (load.getProperty("verification.timing.disabling").equals(
						"true")) {
					disabling.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.nofail")) {
				if (load.getProperty("verification.timing.nofail").equals(
						"true")) {
					nofail.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.noproj")) {
				if (load.getProperty("verification.timing.noproj").equals(
						"true")) {
					nofail.setSelected(true);
				}
			}
			if (load.containsKey("verification.timing.explpn")) {
				if (load.getProperty("verification.timing.explpn").equals(
						"true")) {
					explpn.setSelected(true);
				}
			}
			if (load.containsKey("verification.other.nochecks")) {
				if (load.getProperty("verification.other.nochecks").equals("true")) {
					nochecks.setSelected(true);
				}
			}
			if (load.containsKey("verification.other.reduction")) {
				if (load.getProperty("verification.other.reduction").equals("true")) {
					reduction.setSelected(true);
				}
			}
			if (load.containsKey("verification.other.resetOnce")) {
				if (load.getProperty("verification.other.resetOnce").equals("true")) {
					resetOnce.setSelected(true);
				}
			}
			if (load.containsKey("verification.other.noDisplayResult")) {
				if (load.getProperty("verification.other.noDisplayResult").equals("true")) {
					noDisplayResult.setSelected(true);
				}
			}
			if (load.containsKey("verification.other.displayDBM")) {
				if (load.getProperty("verification.other.displayDBM").equals("true")) {
					displayDBM.setSelected(true);
				}
			}
			if (load.containsKey("verification.other.rateOptimization")) {
				if (load.getProperty("verification.other.rateOptimization").equals("true")) {
					rateOptimization.setSelected(true);
				}
			}
			if (verifyFile.endsWith(".s")) {
				sListModel.addElement(verifyFile);
			}
			if (load.containsKey("verification.sList")) {
				String concatList = load.getProperty("verification.sList");
				String[] list = concatList.split("\\s");
				for (String s : list) {
					sListModel.addElement(s);
				}
			}
			if (load.containsKey("abstraction.interesting")) {
				String intVars = load.getProperty("abstraction.interesting");
				String[] array = intVars.split(" ");
				for (String s : array) {
					if (!s.equals("")) {
						abstPane.addIntVar(s);
					}
				}
			}
			HashMap<Integer, String> preOrder = new HashMap<Integer, String>();
			HashMap<Integer, String> loopOrder = new HashMap<Integer, String>();
			HashMap<Integer, String> postOrder = new HashMap<Integer, String>();
			boolean containsAbstractions = false;
			for (String s : abstPane.transforms) {
				if (load.containsKey(s)) {
					containsAbstractions = true;
				}
			}
			for (String s : abstPane.transforms) {
				if (load.containsKey("abstraction.transform." + s)) {
					if (load.getProperty("abstraction.transform." + s).contains("preloop")) {
						Pattern prePattern = Pattern.compile("preloop(\\d+)");
						Matcher intMatch = prePattern.matcher(load
								.getProperty("abstraction.transform." + s));
						if (intMatch.find()) {
							Integer index = Integer.parseInt(intMatch.group(1));
							preOrder.put(index, s);
						} else {
							abstPane.addPreXform(s);
						}
					}
					else {
						abstPane.preAbsModel.removeElement(s);
					}
					if (load.getProperty("abstraction.transform." + s).contains("mainloop")) {
						Pattern loopPattern = Pattern
								.compile("mainloop(\\d+)");
						Matcher intMatch = loopPattern.matcher(load
								.getProperty("abstraction.transform." + s));
						if (intMatch.find()) {
							Integer index = Integer.parseInt(intMatch.group(1));
							loopOrder.put(index, s);
						} else {
							abstPane.addLoopXform(s);
						}
					}
					else {
						abstPane.loopAbsModel.removeElement(s);
					}
					if (load.getProperty("abstraction.transform." + s).contains("postloop")) {
						Pattern postPattern = Pattern
								.compile("postloop(\\d+)");
						Matcher intMatch = postPattern.matcher(load
								.getProperty("abstraction.transform." + s));
						if (intMatch.find()) {
							Integer index = Integer.parseInt(intMatch.group(1));
							postOrder.put(index, s);
						} else {
							abstPane.addPostXform(s);
						}
					}
					else {
						abstPane.postAbsModel.removeElement(s);
					}
				}
				else if (containsAbstractions) {
					abstPane.preAbsModel.removeElement(s);
					abstPane.loopAbsModel.removeElement(s);
					abstPane.postAbsModel.removeElement(s);
				}
			}
			if (preOrder.size() > 0) {
				abstPane.preAbsModel.removeAllElements();
			}
			for (Integer j = 0; j < preOrder.size(); j++) {
				abstPane.preAbsModel.addElement(preOrder.get(j));
			}
			if (loopOrder.size() > 0) {
				abstPane.loopAbsModel.removeAllElements();
			}
			for (Integer j = 0; j < loopOrder.size(); j++) {
				abstPane.loopAbsModel.addElement(loopOrder.get(j));
			}
			if (postOrder.size() > 0) {
				abstPane.postAbsModel.removeAllElements();
			}
			for (Integer j = 0; j < postOrder.size(); j++) {
				abstPane.postAbsModel.addElement(postOrder.get(j));
			}
			abstPane.preAbs.setListData(abstPane.preAbsModel.toArray());
			abstPane.loopAbs.setListData(abstPane.loopAbsModel.toArray());
			abstPane.postAbs.setListData(abstPane.postAbsModel.toArray());
			if (load.containsKey("abstraction.transforms")) {
				String xforms = load.getProperty("abstraction.transforms");
				String[] array = xforms.split(", ");
				for (String s : array) {
					if (!s.equals("")) {
						abstPane.addLoopXform(s.replace(",", ""));
					}
				}
			}
			if (load.containsKey("abstraction.factor")) {
				abstPane.factorField.setText(load
						.getProperty("abstraction.factor"));
			}
			if (load.containsKey("abstraction.iterations")) {
				abstPane.iterField.setText(load
						.getProperty("abstraction.iterations"));
			}
			tempArray = verifyFile.split(separator);
			sourceFileNoPath = tempArray[tempArray.length - 1];
			backgroundField = new JTextField(sourceFileNoPath);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}

		// Creates the run button
		run = new JButton("Save and Verify");
		run.addActionListener(this);
		buttonPanel.add(run);
		run.setMnemonic(KeyEvent.VK_S);

		// Creates the save button
		save = new JButton("Save Parameters");
		save.addActionListener(this);
		buttonPanel.add(save);
		save.setMnemonic(KeyEvent.VK_P);

		// Creates the view circuit button
		viewCircuit = new JButton("View Circuit");
		viewCircuit.addActionListener(this);
		viewCircuit.setMnemonic(KeyEvent.VK_C);

		// Creates the view trace button
		viewTrace = new JButton("View Trace");
		viewTrace.addActionListener(this);
		buttonPanel.add(viewTrace);
		if (!traceFile.exists()) {
			viewTrace.setEnabled(false);
		}
		viewTrace.setMnemonic(KeyEvent.VK_T);

		// Creates the view log button
		viewLog = new JButton("View Log");
		viewLog.addActionListener(this);
		buttonPanel.add(viewLog);
		viewLog.setMnemonic(KeyEvent.VK_V);
		viewLog.setEnabled(false);

		JPanel backgroundPanel = new JPanel();
		JLabel backgroundLabel = new JLabel("Model File:");
		tempArray = verifyFile.split(separator);
		JLabel componentLabel = new JLabel("Component:");
		componentField.setPreferredSize(new Dimension(200, 20));
		String sourceFile = tempArray[tempArray.length - 1];
		backgroundField = new JTextField(sourceFile);
		backgroundField.setMaximumSize(new Dimension(200, 20));
		backgroundField.setEditable(false);
		backgroundPanel.add(backgroundLabel);
		backgroundPanel.add(backgroundField);
		if (verifyFile.endsWith(".vhd")) {
			backgroundPanel.add(componentLabel);
			backgroundPanel.add(componentField);
		}
		backgroundPanel.setMaximumSize(new Dimension(500, 30));
		basicOptions.add(backgroundPanel);
		basicOptions.add(abstractionPanel);
		basicOptions.add(timingRadioPanel);
		if (!lema) {
			basicOptions.add(timingCheckBoxPanel);
		}
		basicOptions.add(otherPanel);
		//if (lema) {
		//	basicOptions.add(preprocPanel);
		//}
		if (!lema) {
			basicOptions.add(algorithmPanel);
		}
		if (verifyFile.endsWith(".vhd")) {
			basicOptions.add(componentPanel);
		}
		if (verifyFile.endsWith(".lpn")) {
			basicOptions.add(LPNPanel);
		}
		basicOptions.setLayout(new BoxLayout(basicOptions, BoxLayout.Y_AXIS));
		basicOptions.add(Box.createVerticalGlue());

		advOptions.add(compilationPanel);
		advOptions.add(advTimingPanel);
		advOptions.add(advancedPanel);
		advOptions.add(bddPanel);
		advOptions.setLayout(new BoxLayout(advOptions, BoxLayout.Y_AXIS));
		advOptions.add(Box.createVerticalGlue());

		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Basic Options", basicOptions);
		tab.addTab("Advanced Options", advOptions);
		tab.addTab("Abstraction Options", abstPane);
		tab.setPreferredSize(new Dimension(1000, 480));

		this.setLayout(new BorderLayout());
		this.add(tab, BorderLayout.PAGE_START);
		change = false;
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 * 
	 * @throws
	 * @throws
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		change = true;
		if (e.getSource() == run) {
			save(verFile);
			new Thread(this).start();
		} else if (e.getSource() == save) {
			log.addText("Saving:\n" + directory + separator + verFile + "\n");
			save(verFile);
		} else if (e.getSource() == viewCircuit) {
			viewCircuit();
		} else if (e.getSource() == viewTrace) {
			viewTrace();
		} else if (e.getSource() == viewLog) {
			viewLog();
		} else if (e.getSource() == addComponent) {
			String[] vhdlFiles = new File(root).list();
			ArrayList<String> tempFiles = new ArrayList<String>();
			for (int i = 0; i < vhdlFiles.length; i++) {
				if (vhdlFiles[i].endsWith(".vhd")
						&& !vhdlFiles[i].equals(sourceFileNoPath)) {
					tempFiles.add(vhdlFiles[i]);
				}
			}
			vhdlFiles = new String[tempFiles.size()];
			for (int i = 0; i < vhdlFiles.length; i++) {
				vhdlFiles[i] = tempFiles.get(i);
			}
			String filename = (String) JOptionPane.showInputDialog(this, "",
					"Select Component", JOptionPane.PLAIN_MESSAGE, null,
					vhdlFiles, vhdlFiles[0]);
			if (filename != null) {
				String[] comps = componentList.getItems();
				boolean contains = false;
				for (int i = 0; i < comps.length; i++) {
					if (comps[i].equals(filename)) {
						contains = true;
					}
				}
				if (!filename.endsWith(".vhd")) {
					JOptionPane.showMessageDialog(Gui.frame,
							"You must select a valid VHDL file.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				} else if (new File(directory + separator + filename).exists()
						|| filename.equals(sourceFileNoPath) || contains) {
					JOptionPane
					.showMessageDialog(
							Gui.frame,
							"This component is already contained in this tool.",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				componentList.addItem(filename);
				return;
			}
		} else if (e.getSource() == removeComponent) {
			if (componentList.getSelectedValue() != null) {
				String selected = componentList.getSelectedValue().toString();
				componentList.removeItem(selected);
				new File(directory + separator + selected).delete();
			}
		} else if (e.getSource() == addSFile) {
			String sFile = JOptionPane.showInputDialog(this, "Enter Assembly File Name:",
					"Assembly File Name", JOptionPane.PLAIN_MESSAGE);
			if ((!sFile.endsWith(".s") && !sFile.endsWith(".inst")) || !(new File(sFile).exists())) {
				JOptionPane.showMessageDialog(this, "Invalid filename entered.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == addLPN) {
			String[] lpnFiles = new File(root).list();
			ArrayList<String> tempFiles = new ArrayList<String>();
			for (int i = 0; i < lpnFiles.length; i++) {
				if (lpnFiles[i].endsWith(".lpn")
						&& !lpnFiles[i].equals(sourceFileNoPath)) {
					tempFiles.add(lpnFiles[i]);
				}
			}
			Object[] tempFilesArray = tempFiles.toArray();
			Arrays.sort(tempFilesArray);
			lpnFiles = new String[tempFilesArray.length];
			for (int i = 0; i < lpnFiles.length; i++) {
				lpnFiles[i] = (String) tempFilesArray[i];
			}
			JList lpnListInCurDir = new JList(lpnFiles);
			JScrollPane scroll = new JScrollPane(lpnListInCurDir);
			String[] options = new String[]{"OK", "Cancel"};
			int selection = JOptionPane.showOptionDialog(this, scroll,
					"Select LPN", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			if (selection == JOptionPane.YES_OPTION) {
				for (Object obj : lpnListInCurDir.getSelectedValues()) {
					String filename = (String) obj;
					if (filename != null) {
						String[] lpns = lpnList.getItems();
						boolean contains = false;
						for (int i = 0; i < lpns.length; i++) {
							if (lpns[i].equals(filename)) {
								contains = true;
							}
						}
						if (!filename.endsWith(".lpn")) {
							JOptionPane.showMessageDialog(Gui.frame,
									"You must select a valid LPN file.", "Error",
									JOptionPane.ERROR_MESSAGE);
							return;
						} else if (new File(directory + separator + filename).exists()
								|| filename.equals(sourceFileNoPath) || contains) {
							JOptionPane
							.showMessageDialog(
									Gui.frame,
									"This lpn is already contained in this tool.",
									"Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						lpnList.addItem(filename);
					}
				}
			}
			return;
		}
		else if (e.getSource() == removeLPN) {
			if (lpnList.getSelectedValue() != null) {
				String selected = lpnList.getSelectedValue().toString();
				lpnList.removeItem(selected);
				new File(directory + separator + selected).delete();
			}
		}
	}

	//	private void printAllProcesses(HashMap<Transition, Integer> allProcessTrans) {		
	//		System.out.println("~~~~~~~~~~Begin~~~~~~~~~");
	//		Set<Transition> allTransitions = allProcessTrans.keySet();
	//		for (Iterator<Transition> allTransIter = allTransitions.iterator(); allTransIter.hasNext();) {
	//			Transition curTran = allTransIter.next();			
	//			System.out.println(curTran.getName() + "\t" + allProcessTrans.get(curTran));
	//		}
	//		System.out.println("~~~~~~~~~~End~~~~~~~~~");
	//	}

	@Override
	public void run() {
		copyFile();
		String[] array = directory.split(separator);
		String tempDir = "";
		String lpnFileName = "";
		if (!verifyFile.endsWith("lpn")) {
			String[] temp = verifyFile.split("\\.");
			lpnFileName = temp[0] + ".lpn";
		} else {
			lpnFileName = verifyFile;
		}
		if (untimedStateSearch.isSelected()) {
			LPN lpn = new LPN();
			lpn.load(directory + separator + lpnFileName);
			Options.setLogName(lpn.getLabel());
			boolean canPerformMarkovianAnalysisTemp = true;
			if (!canPerformMarkovianAnalysis(lpn))
				canPerformMarkovianAnalysisTemp = false;
			if (!decomposeLPN.isSelected()) {
				ArrayList<LPN> selectedLPNs = new ArrayList<LPN>();
				selectedLPNs.add(lpn);
				for (int i=0; i < lpnList.getSelectedValues().length; i++) {
					String curLPNname = (String) lpnList.getSelectedValues()[i];
					LPN curLPN = new LPN();
					curLPN.load(directory + separator + curLPNname);
					selectedLPNs.add(curLPN);
					if (!canPerformMarkovianAnalysis(curLPN))
						canPerformMarkovianAnalysisTemp = false;
				}
				if (canPerformMarkovianAnalysisTemp)
					Options.setMarkovianModelFlag();
				//				System.out.println("====== LPN loading order (default) ========");
				//				for (int i=0; i<selectedLPNs.size(); i++) {
				//					System.out.println(selectedLPNs.get(i).getLabel());
				//				}

				////=============== Code to produce a GUI for rearranging the order of loading LPNs into the Project constructor ================			
				//				System.out.println("====== LPN loading order (manipulated) ========");
				//				ArrayList<LhpnFile> selectedLPNsManipulated = new ArrayList<LhpnFile>(selectedLPNs.size());
				//				System.out.println("size = " + selectedLPNsManipulated.size());
				//				String[] LPNlabels = new String[selectedLPNs.size()];
				//				for (int i=0; i<selectedLPNs.size(); i++) {
				//					LPNlabels[i] = selectedLPNs.get(i).getLabel();
				//					System.out.println(i + " " + LPNlabels[i]);
				//				}
				//				System.out.println("selected index");
				//				for (int i=0; i<selectedLPNs.size(); i++) {
				//					JList LpnLoadingOrderList = new JList(LPNlabels);
				//					LpnLoadingOrderList.setVisibleRowCount(10);				
				//					JScrollPane ampleMethdsPane = new JScrollPane(LpnLoadingOrderList);
				//					JPanel mainPanel0 = new JPanel(new BorderLayout());
				//					mainPanel0.add("North", new JLabel("Select a LPN:"));
				//					mainPanel0.add("Center", ampleMethdsPane);							
				//					Object[] options0 = {"Select", "Cancel"};
				//					int optionRtVal0 = JOptionPane.showOptionDialog(Gui.frame, mainPanel0, "LPN order manipulation", 
				//							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options0, options0[0]);
				//					if (optionRtVal0 == 1)  // Cancel					
				//						return;
				//					System.out.println(LpnLoadingOrderList.getSelectedIndex() + " " + selectedLPNs.get(LpnLoadingOrderList.getSelectedIndex()).getLabel());
				//					selectedLPNsManipulated.add(selectedLPNs.get(LpnLoadingOrderList.getSelectedIndex()));
				//				}			
				//				//Project untimed_dfs = new Project(selectedLPNsManipulated);
				Project untimed_dfs = new Project(selectedLPNs);				

				// ------- Debugging Messages Settings ------------
				 //Options for printing out intermediate results during POR
				//Options.setDebugMode(true);
				Options.setDebugMode(false);
				if (Options.getDebugMode())
					System.out.println("Debug mode is ON.");				
				//----------- POR and Cycle Closing Methods (FULL)--------------
				//				if (untimedPOR.isSelected()) {
				//					// Options for using trace-back in ample calculation
				//					String[] ampleMethds = {"Use trace-back for ample computation", "No trace-back for ample computation"};
				//					JList ampleMethdsList = new JList(ampleMethds);
				//					ampleMethdsList.setVisibleRowCount(2);
				//					//cycleClosingList.addListSelectionListener(new ValueReporter());
				//					JScrollPane ampleMethdsPane = new JScrollPane(ampleMethdsList);
				//					JPanel mainPanel0 = new JPanel(new BorderLayout());
				//					mainPanel0.add("North", new JLabel("Select an ample set computation method:"));
				//					mainPanel0.add("Center", ampleMethdsPane);							
				//					Object[] options0 = {"Run", "Cancel"};
				//					int optionRtVal0 = JOptionPane.showOptionDialog(Gui.frame, mainPanel0, "Ample set computation methods selection", 
				//								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options0, options0[0]);
				//					if (optionRtVal0 == 1) {
				//						// Cancel
				//						return;
				//					}
				//					int ampleMethdsIndex = ampleMethdsList.getSelectedIndex();
				//					if (ampleMethdsIndex == 0) 
				//						Options.setPOR("tb");
				//					if (ampleMethdsIndex == 1)
				//						Options.setPOR("tboff");					
				//					// GUI for different cycle closing methods.
				//					String[] entries = {"Use behavioral analysis",
				//										"Use behavioral analysis and state trace-back",
				//										"No cycle closing",
				//										"Strong cycle condition"};
				//					JList cycleClosingList = new JList(entries);
				//					cycleClosingList.setVisibleRowCount(4);
				//					//cycleClosingList.addListSelectionListener(new ValueReporter());
				//					JScrollPane cycleClosingPane = new JScrollPane(cycleClosingList);
				//					JPanel mainPanel = new JPanel(new BorderLayout());
				//					mainPanel.add("North", new JLabel("Select a cycle closing method:"));
				//					mainPanel.add("Center", cycleClosingPane);							
				//					Object[] options = {"Run", "Cancel"};
				//					int optionRtVal = JOptionPane.showOptionDialog(Gui.frame, mainPanel, "Cycle closing methods selection", 
				//								JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				//					if (optionRtVal == 1) {
				//						// Cancel
				//						return;
				//					}
				//					int cycleClosingMthdIndex = cycleClosingList.getSelectedIndex();
				//					if (cycleClosingMthdIndex == 0) {
				//						Options.setCycleClosingMthd("behavioral");
				//						if (Options.getPOR().equals("tb")) {
				//							String[] cycleClosingAmpleMethds = {"Use trace-back", "No trace-back"};
				//							JList cycleClosingAmpleList = new JList(cycleClosingAmpleMethds);
				//							cycleClosingAmpleList.setVisibleRowCount(2);
				//							JScrollPane cycleClosingAmpleMethdsPane = new JScrollPane(cycleClosingAmpleList);
				//							JPanel mainPanel1 = new JPanel(new BorderLayout());
				//							mainPanel1.add("North", new JLabel("Select a cycle closing ample computation method:"));
				//							mainPanel1.add("Center", cycleClosingAmpleMethdsPane);							
				//							Object[] options1 = {"Run", "Cancel"};
				//							int optionRtVal1 = JOptionPane.showOptionDialog(Gui.frame, mainPanel1, "Cycle closing ample computation method selection", 
				//										JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
				//							if (optionRtVal1 == 1) {
				//								// Cancel
				//								return;
				//							}
				//							int cycleClosingAmpleMethdIndex = cycleClosingAmpleList.getSelectedIndex();
				//							if (cycleClosingAmpleMethdIndex == 0) 
				//								Options.setCycleClosingAmpleMethd("cctb");
				//							if (cycleClosingAmpleMethdIndex == 1)
				//								Options.setCycleClosingAmpleMethd("cctboff");
				//						}
				//						else if (Options.getPOR().equals("tboff")) {
				//							Options.setCycleClosingAmpleMethd("cctboff");
				//						}
				//					}						
				//					else if (cycleClosingMthdIndex == 1) 
				//						Options.setCycleClosingMthd("state_search");
				//					else if (cycleClosingMthdIndex == 2)
				//						Options.setCycleClosingMthd("no_cycleclosing");
				//					else if (cycleClosingMthdIndex == 3)
				//						Options.setCycleClosingMthd("strong");
				//					if (dot.isSelected()) {
				//						Options.setOutputSgFlag(true);
				//					}
				//					Options.setPrjSgPath(directory + separator);
				//					// Options for printing the final numbers from search_dfs or search_dfsPOR. 
				//					Options.setOutputLogFlag(true);
				////					Options.setPrintLogToFile(false);
				//					StateGraph[] stateGraphArray = untimed_dfs.searchPOR();
				//					if (dot.isSelected()) {
				//						for (int i=0; i<stateGraphArray.length; i++) {
				//							String graphFileName = stateGraphArray[i].getLpn().getLabel() + "POR_"+ Options.getCycleClosingMthd() + "_local_sg.dot";
				//							stateGraphArray[i].outputLocalStateGraph(directory + separator + graphFileName);
				//						}
				//						// Code for producing global state graph is in search_dfsPOR in the Analysis class.						
				//					}
				//				}
				// -------------------------------------				
				//----------- POR and Cycle Closing Methods (Simplified)--------------
				//if (untimedPOR.isSelected()) {
					// Options for using trace-back in ample calculation					
					String[] ampleMethds = {"No POR", "Trace-back","No trace-back for ample computation", "Behavioral Analysis"};
					JList ampleMethdsList = new JList(ampleMethds);
					ampleMethdsList.setVisibleRowCount(4);
					//cycleClosingList.addListSelectionListener(new ValueReporter());
					JScrollPane ampleMethdsPane = new JScrollPane(ampleMethdsList);
					JPanel mainPanel0 = new JPanel(new BorderLayout());
					mainPanel0.add("North", new JLabel("Select an ample set computation method:"));
					mainPanel0.add("Center", ampleMethdsPane);							
					Object[] options0 = {"Run", "Cancel"};
					int optionRtVal0 = JOptionPane.showOptionDialog(Gui.frame, mainPanel0, "Ample set computation methods selection", 
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options0, options0[0]);
					if (optionRtVal0 == 1) { // Cancel
						return;
					}
					int ampleMethdsIndex = ampleMethdsList.getSelectedIndex();
					if (ampleMethdsIndex == 0) { 
						Options.setPOR("off");						
					}
					if (ampleMethdsIndex == 1) { 
						Options.setPOR("tb");
						Options.setCycleClosingMthd("behavioral");
						Options.setCycleClosingStrongStubbornMethd("cctb");
						if (Options.getMarkovianModelFlag()) {
							String[] tranRateDepSpectrum = {"Full dependency relations", "Fastest average transition rates",
									"Ignore dependency relations within a rate change tolerance", "Ignore dependency relations"};
							JList tranRateDepSpectrumList = new JList(tranRateDepSpectrum);
							tranRateDepSpectrumList.setVisibleRowCount(4);							
							JScrollPane tranRateDepPane = new JScrollPane(tranRateDepSpectrumList);
							JPanel mainPanel1 = new JPanel(new BorderLayout());
							mainPanel1.add("North", new JLabel("Select a dependency relation for transition rates:"));
							mainPanel1.add("Center", tranRateDepPane);							
							Object[] options1 = {"Run", "Cancel"};
							int optionRtVal1 = JOptionPane.showOptionDialog(Gui.frame, mainPanel1, "Transition rates depedency relation for partial-order reduction", 
									JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
							if (optionRtVal1 == 1) { // Cancel
								return;
							}
							int tranRateDepSpectrumListIndex = tranRateDepSpectrumList.getSelectedIndex();
							if (tranRateDepSpectrumListIndex == 0) {
								Options.setTranRatePorDef("full");
							}
							else if (tranRateDepSpectrumListIndex == 1){
								Options.setTranRatePorDef("avrg");
								System.out.println("To be implemented.");
								System.exit(1);
							}
							else if (tranRateDepSpectrumListIndex == 2) {
								Options.setTranRatePorDef("tolr");
								System.out.println("To be implemented.");
								System.exit(1);
							}
							else if (tranRateDepSpectrumListIndex == 3) {
								Options.setTranRatePorDef("none");
							}
							
						}					
					}		
					if (ampleMethdsIndex == 2) {
						Options.setPOR("tboff");
						Options.setCycleClosingMthd("behavioral");
						Options.setCycleClosingStrongStubbornMethd("cctboff");
					}
					if (ampleMethdsIndex == 3) {
						Options.setPOR("behavioral");
						Options.setCycleClosingMthd("behavioral");
						Options.setCycleClosingStrongStubbornMethd("cctboff");
					}
					// TODO: Choose different cycle closing methods
					//					int cycleClosingMthdIndex = cycleClosingList.getSelectedIndex();
					//					if (cycleClosingMthdIndex == 0) {
					//						Options.setCycleClosingMthd("behavioral");
					//						if (Options.getPOR().equals("tb")) {
					//							String[] cycleClosingAmpleMethds = {"Use trace-back", "No trace-back"};
					//							JList cycleClosingAmpleList = new JList(cycleClosingAmpleMethds);
					//							cycleClosingAmpleList.setVisibleRowCount(2);
					//							JScrollPane cycleClosingAmpleMethdsPane = new JScrollPane(cycleClosingAmpleList);
					//							JPanel mainPanel1 = new JPanel(new BorderLayout());
					//							mainPanel1.add("North", new JLabel("Select a cycle closing ample computation method:"));
					//							mainPanel1.add("Center", cycleClosingAmpleMethdsPane);							
					//							Object[] options1 = {"Run", "Cancel"};
					//							int optionRtVal1 = JOptionPane.showOptionDialog(Gui.frame, mainPanel1, "Cycle closing ample computation method selection", 
					//										JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
					//							if (optionRtVal1 == 1) {
					//								// Cancel
					//								return;
					//							}
					//							int cycleClosingAmpleMethdIndex = cycleClosingAmpleList.getSelectedIndex();
					//							if (cycleClosingAmpleMethdIndex == 0) 
					//								Options.setCycleClosingAmpleMethd("cctb");
					//							if (cycleClosingAmpleMethdIndex == 1)
					//								Options.setCycleClosingAmpleMethd("cctboff");
					//						}
					//						else if (Options.getPOR().equals("tboff")) {
					//							Options.setCycleClosingAmpleMethd("cctboff");
					//						}
					//					}						
					//					else if (cycleClosingMthdIndex == 1) 
					//						Options.setCycleClosingMthd("state_search");
					//					else if (cycleClosingMthdIndex == 2)
					//						Options.setCycleClosingMthd("no_cycleclosing");
					//					else if (cycleClosingMthdIndex == 3)
					//						Options.setCycleClosingMthd("strong");

					if (dot.isSelected()) {
						Options.setOutputSgFlag(true);
					}
					//Options.setPrjSgPath(directory + separator);
					System.out.println("directory = "+ directory);
					Options.setPrjSgPath(directory);
					// Options for printing the final numbers from search_dfs.
					Options.setOutputLogFlag(true);
					untimed_dfs.search();
				//}				
				//else { // No POR
//					Options.setPrjSgPath(directory + separator);
//					// Options for printing the final numbers from search_dfs or search_dfsPOR. 
//					Options.setOutputLogFlag(true);
//					//					Options.setPrintLogToFile(false);
//					if (dot.isSelected()) 
//						Options.setOutputSgFlag(true);
//					untimed_dfs.search();
				//}
				return;
			}
			else if (decomposeLPN.isSelected() && lpnList.getSelectedValue() == null) {
				HashMap<Transition, Integer> allProcessTrans = new HashMap<Transition, Integer>();
				// create an Abstraction object to get all processes in one LPN
				Abstraction abs = lpn.abstractLhpn(this);
				abs.decomposeLpnIntoProcesses();				 
				allProcessTrans.putAll(abs.getTransWithProcIDs());
				HashMap<Integer, LpnProcess> processMap = new HashMap<Integer, LpnProcess>();
				for (Transition curTran: allProcessTrans.keySet()) {
					Integer procId = allProcessTrans.get(curTran);
					if (!processMap.containsKey(procId)) {
						LpnProcess newProcess = new LpnProcess(procId);
						newProcess.addTranToProcess(curTran);
						if (curTran.getPreset() != null) {							
							for (Place p : curTran.getPreset()) {
								newProcess.addPlaceToProcess(p);
							}
						}
						processMap.put(procId, newProcess);
					}
					else {
						LpnProcess curProcess = processMap.get(procId);
						curProcess.addTranToProcess(curTran);
						if (curTran.getPreset() != null) {
							for (Place p : curTran.getPreset()) {
								curProcess.addPlaceToProcess(p);
							}
						}			
					}
				}
				HashMap<String, ArrayList<Integer>> allProcessRead = abs.getProcessRead(); 
				HashMap<String, ArrayList<Integer>> allProcessWrite = abs.getProcessWrite();
				for (String curRead : allProcessRead.keySet()) {
					if (allProcessRead.get(curRead).size() == 1 
							&& allProcessWrite.get(curRead).size() == 1) {
						if (allProcessRead.get(curRead).equals(allProcessWrite.get(curRead))) {
							// variable is read and written only by one single process
							Integer curProcessID = allProcessRead.get(curRead).get(0); 
							ArrayList<Variable> processInternal = processMap.get(curProcessID).getProcessInternal();
							if (!processInternal.contains(abs.getVariable(curRead)))
								processInternal.add(abs.getVariable(curRead));
						}
						else {
							// variable is read in one process and written by another
							Integer curProcessID = allProcessRead.get(curRead).get(0); 
							ArrayList<Variable> processInput = processMap.get(curProcessID).getProcessInput();
							if (!processInput.contains(abs.getVariable(curRead)))
								processInput.add(abs.getVariable(curRead));
							curProcessID = allProcessWrite.get(curRead).get(0);
							ArrayList<Variable> processOutput = processMap.get(curProcessID).getProcessOutput();
							if (!processOutput.contains(abs.getVariable(curRead)))
								processOutput.add(abs.getVariable(curRead));
						}

					}
					else if (allProcessRead.get(curRead).size() > 1 || allProcessWrite.get(curRead).size() > 1) {
						ArrayList<Integer> readList = allProcessRead.get(curRead);
						ArrayList<Integer> writeList = allProcessWrite.get(curRead);
						ArrayList<Integer> alreadyAssignedAsOutput = new ArrayList<Integer>();
						for (int i=0; i<writeList.size(); i++) {
							Integer curProcessID = writeList.get(i);
							ArrayList<Variable> processOutput = processMap.get(curProcessID).getProcessOutput();
							if (!processOutput.contains(abs.getVariable(curRead)))
								processOutput.add(abs.getVariable(curRead));
							if (!alreadyAssignedAsOutput.contains(curProcessID))
								alreadyAssignedAsOutput.add(curProcessID);
						}
						readList.removeAll(alreadyAssignedAsOutput);
						for (int i=0; i<readList.size(); i++) {
							Integer curProcessID = readList.get(i);
							ArrayList<Variable> processInput = processMap.get(curProcessID).getProcessInput();
							if (!processInput.contains(abs.getVariable(curRead)))
								processInput.add(abs.getVariable(curRead));
						}
					}
					else if (allProcessWrite.get(curRead).size() == 0) {
						// variable is only read, but never written
						if (allProcessRead.get(curRead).size() == 1) {
							// variable is read locally
							Integer curProcessID = allProcessRead.get(curRead).get(0); 
							ArrayList<Variable> processInternal = processMap.get(curProcessID).getProcessInternal();
							if (!processInternal.contains(abs.getVariable(curRead)))
								processInternal.add(abs.getVariable(curRead)); 			
						}
						else if (allProcessRead.get(curRead).size() > 1) {
							// variable is read globally by multiple processes
							for (int i=0; i < allProcessRead.get(curRead).size(); i++) {
								Integer curProcessID = allProcessRead.get(curRead).get(i);
								ArrayList<Variable> processInput = processMap.get(curProcessID).getProcessInput();
								if (!processInput.contains(abs.getVariable(curRead)))
									processInput.add(abs.getVariable(curRead));
							}
						}
					}
					else if (allProcessRead.get(curRead).size() == 0) {
						// variable is only written, but never read
						if (allProcessWrite.get(curRead).size() == 1) {
							// variable is written locally
							Integer curProcessID = allProcessWrite.get(curRead).get(0); 
							ArrayList<Variable> processInternal = processMap.get(curProcessID).getProcessInternal();
							if (!processInternal.contains(abs.getVariable(curRead)))
								processInternal.add(abs.getVariable(curRead)); 	
						}
						if (allProcessWrite.get(curRead).size() > 1) {
							// variable is written globally by multiple processes
							for (int i=0; i < allProcessWrite.get(curRead).size(); i++) {
								Integer curProcessID = allProcessWrite.get(curRead).get(i);
								ArrayList<Variable> processInput = processMap.get(curProcessID).getProcessInput();
								if (!processInput.contains(abs.getVariable(curRead)))
									processInput.add(abs.getVariable(curRead));
							}
						}
					}
				}
				// Options to get all possible decompositions or just one decomposition
				String[] decomposition = {"Get ALL decompositions", "Get ONE decomposition"};
				JList decompositionList = new JList(decomposition);
				decompositionList.setVisibleRowCount(2);
				JScrollPane decompMethdsPane = new JScrollPane(decompositionList);
				JPanel mainPanel0 = new JPanel(new BorderLayout());
				mainPanel0.add("North", new JLabel("Select a LPN decomposition method:"));
				mainPanel0.add("Center", decompMethdsPane);							
				Object[] options0 = {"Run", "Cancel"};
				int optionRtVal0 = JOptionPane.showOptionDialog(Gui.frame, mainPanel0, "LPN decomposition methods selection", 
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options0, options0[0]);
				if (optionRtVal0 == 1) {
					// Cancel
					return;
				}
				int decompMethdIndex = decompositionList.getSelectedIndex();
				if (decompMethdIndex == 0) { // Automatically find all possible LPN partitions.
					// Find the process with least number of variables
					int leastNumVarsInOneProcess = lpn.getVariables().length;
					for (Integer curProcId : processMap.keySet()) {
						LpnProcess curProcess = processMap.get(curProcId);
						if (curProcess.getProcessVarSize() < leastNumVarsInOneProcess) {
							leastNumVarsInOneProcess = curProcess.getProcessVarSize();
						}		
					}					
					Integer maxNumVarsInOneComp = leastNumVarsInOneProcess;
					Integer tryNumDecomps = processMap.size(); // The maximal number of decomposed LPNs is the number of all processes in the LPN.
					HashSet<Integer> possibleDecomps = new HashSet<Integer>();
					//System.out.println("lpn.getVariables().length = " + lpn.getVariables().length);
					while(tryNumDecomps > 1) {
						LpnComponentList componentList = new LpnComponentList(maxNumVarsInOneComp);					
						componentList.buildComponents(processMap, directory, lpn.getLabel());
						HashMap<Integer, Component> compMap = componentList.getComponentMap();
						tryNumDecomps = compMap.size();						
						if (tryNumDecomps == 1)
							break;
						if (!possibleDecomps.contains(compMap.size())) {
							possibleDecomps.add(compMap.size());
							for (Component comp : compMap.values()) {
								LPN lpnComp = new LPN();
								lpnComp = comp.buildLPN(lpnComp);
								lpnComp.save(root + separator + lpn.getLabel() + "_decomp" + maxNumVarsInOneComp + "vars" + comp.getComponentId() + ".lpn");		
							}
						}
						maxNumVarsInOneComp++;
					}
					System.out.println("possible decompositions for " + lpn.getLabel() + ".lpn: ");
					for (Integer i : possibleDecomps) {
						System.out.print(i + ", ");
					}
					System.out.println();
					return;
				}
				if (decompMethdIndex ==1) { // User decides one LPN partition.
					// Find the process with the least number of variables
					int leastNumVarsInOneProcess = lpn.getVariables().length;
					for (Integer curProcId : processMap.keySet()) {
						LpnProcess curProcess = processMap.get(curProcId);
						if (curProcess.getProcessVarSize() < leastNumVarsInOneProcess) {
							leastNumVarsInOneProcess = curProcess.getProcessVarSize();
						}		
					}
					// Coalesce an array of processes into one LPN component.
					JPanel mainPanel = new JPanel(new BorderLayout());
					JPanel maxVarsPanel = new JPanel();
					JTextField maxVarsText = new JTextField(3);
					maxVarsText.setText("" + lpn.getVariables().length);
					maxVarsPanel.add(new JLabel("Enter the maximal number of variables allowed in one component:"));
					maxVarsPanel.add(maxVarsText);
					mainPanel.add("North", new JLabel("number of variables in this LPN: " + lpn.getVariables().length));
					mainPanel.add("Center", new JLabel("number of variables in the smallest process: " + leastNumVarsInOneProcess));
					mainPanel.add("South", maxVarsPanel);

					Object[] options = {"Run", "Cancel"};
					int optionRtVal = JOptionPane.showOptionDialog(Gui.frame, mainPanel, "Assign the maximal number of variables in one component", 
							JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
					if (optionRtVal == 1) {
						// Cancel
						return;
					}
					Integer maxNumVarsInOneComp = Integer.parseInt(maxVarsText.getText().trim());
					if (leastNumVarsInOneProcess >= maxNumVarsInOneComp) {
						// The original LPN is decomposed into processes.
						// Store each process as individual LPN.
						for (Integer curProcId : processMap.keySet()) {						
							LpnProcess curProcess = processMap.get(curProcId);	
							LPN lpnProc = new LPN();
							lpnProc = curProcess.buildLPN(lpnProc);
							lpnProc.save(root + separator + lpn.getLabel() + "_decomp" + maxNumVarsInOneComp + "vars" + curProcId + ".lpn");
							//lpnProc.save(directory + separator + lpn.getLabel() + curProcId + ".lpn");
						}
						JOptionPane.showMessageDialog(
								Gui.frame,
								"The entered maximal number of variables in one component is too small. The LPN was decomposed into processes.",
								"Warning", JOptionPane.WARNING_MESSAGE);
						return;
					}
					LpnComponentList componentList = new LpnComponentList(maxNumVarsInOneComp);					
					componentList.buildComponents(processMap, directory, lpn.getLabel());
					HashMap<Integer, Component> compMap = componentList.getComponentMap();
					for (Component comp : compMap.values()) {
						LPN lpnComp = new LPN();
						lpnComp = comp.buildLPN(lpnComp);
						// --- TEMP ----
						//checkDecomposition(lpn, lpnComp);
						// -------------
						lpnComp.save(root + separator + lpn.getLabel() + "_decomp" + maxNumVarsInOneComp + "vars" + comp.getComponentId() + ".lpn");		
					}				
				}
			}
			else if (multipleLPNs.isSelected() && lpnList.getSelectedValues().length < 1) {
				JOptionPane.showMessageDialog(
						Gui.frame,
						"Please select at least 1 more LPN.",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		long time1 = System.nanoTime();
		File work = new File(directory);
		/*
		if (!preprocStr.getText().equals("")) {
			try {
				String preprocCmd = preprocStr.getText();
				Runtime exec = Runtime.getRuntime();
				Process preproc = exec.exec(preprocCmd, null, work);
				log.addText("Executing:\n" + preprocCmd + "\n");
				preproc.waitFor();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Error with preprocessing.", "Error",
						JOptionPane.ERROR_MESSAGE);
				//e.printStackTrace();

			}
		}
		 */
		try {
			if (verifyFile.endsWith(".lpn")) {
				Runtime.getRuntime().exec("atacs -llsl " + verifyFile, null,
						work);
			} else if (verifyFile.endsWith(".vhd")) {
				Runtime.getRuntime().exec("atacs -lvsl " + verifyFile, null,
						work);
			} else if (verifyFile.endsWith(".g")) {
				Runtime.getRuntime().exec("atacs -lgsl " + verifyFile, null,
						work);
			} else if (verifyFile.endsWith(".hse")) {
				Runtime.getRuntime().exec("atacs -lhsl " + verifyFile, null,
						work);			}
		} catch (Exception e) {
		}
		for (int i = 0; i < array.length - 1; i++) {
			tempDir = tempDir + array[i] + separator;
		}
		LPN lhpnFile = new LPN();
		lhpnFile.load(directory + separator + lpnFileName);
		Abstraction abstraction = lhpnFile.abstractLhpn(this);
		String abstFilename;
		//if(dbm2.isSelected())
		//{
			//try {
				//verification.timed_state_exploration.dbm2.StateExploration.findStateGraph(lhpnFile, directory+separator, lpnFileName);
			//} catch (FileNotFoundException e) {
				//e.printStackTrace();
			//}

			//return;
		//}

		/**
		 * If the splitZone option (labeled "Split Zone") is selected,
		 * run the timed analysis.
		 */
		if(zone.isSelected() || octagon.isSelected())
		{

			// Version prior to adding zones to the project states.
			// Uses the timed_state_exploration.zone infrastructure.
			//			if(multipleLPNs.isSelected())
			//			{
			//				
			//			}
			//			else
			//			{
			//				LhpnFile lpn = new LhpnFile();
			//				lpn.load(directory + separator + lpnFileName);
			//
			//				// The full state graph is created for only one LPN.
			//
			//				/**
			//				 * This is what selects the timing analysis.
			//				 * The method setTimingAnalysisType sets a static variable
			//				 * in the Options class that is queried by the search method.
			//				 */
			//				Options.setTimingAnalsysisType("zone");
			//
			//				ZoneType.setSubsetFlag(!timsubset.isSelected());
			//				ZoneType.setSupersetFlag(!superset.isSelected());
			//
			//				Project_Timed timedStateSearch = new Project_Timed(lpn, 
			//						Options.getTimingAnalysisFlag(), false);
			//				if(useGraphs.isSelected()){
			//					timedStateSearch = new Project_Timed(lpn, 
			//							Options.getTimingAnalysisFlag(), true);
			//				}
			//				StateGraph_timed[] stateGraphArray = timedStateSearch.search();
			//				String graphFileName = verifyFile.replace(".lpn", "") + "_sg.dot";
			//				if (stateGraphArray.length > 1) {
			//					JOptionPane.showMessageDialog(
			//							Gui.frame,
			//							"Mutiple state graphs should not be produced.",
			//							"Error", JOptionPane.ERROR_MESSAGE);		
			//
			//				}
			//				else {
			//					if (dot.isSelected()) {
			//						stateGraphArray[0].outputLocalStateGraph(directory + separator + graphFileName);  
			//					}
			//					if(graph.isSelected()){
			//						showGraph(directory + separator + graphFileName);
			//					}
			//				}
			//			
			//				Options.setTimingAnalsysisType("off");
			//				Zone.clearLexicon();
			//			}
			//			return;


			// This is the code before the revision for allowing multiple LPNs
			//			// Uses the timed_state_exploration.zoneProject infrastructure.
			//			LhpnFile lpn = new LhpnFile();
			//			lpn.load(directory + separator + lpnFileName);
			//
			//			// The full state graph is created for only one LPN.
			//			
			//			/**
			//			 * This is what selects the timing analysis.
			//			 * The method setTimingAnalysisType sets a static variable
			//			 * in the Options class that is queried by the search method.
			//			 */
			//			Options.setTimingAnalsysisType("zone");
			//
			//			ZoneType.setSubsetFlag(!timsubset.isSelected());
			//			ZoneType.setSupersetFlag(!superset.isSelected());
			//			
			//			Project timedStateSearch = new Project(lpn);
			//			
			//			StateGraph[] stateGraphArray = timedStateSearch.search();
			//			String graphFileName = verifyFile.replace(".lpn", "") + "_sg.dot";
			//			
			//			if (dot.isSelected()) {
			//				stateGraphArray[0].outputLocalStateGraph(directory + separator + graphFileName);  
			//			}
			//			
			//			if(graph.isSelected()){
			//				showGraph(directory + separator + graphFileName);
			//			}
			//		
			//			Options.setTimingAnalsysisType("off");
			//			Zone.clearLexicon();
			//			
			//			return;

			// Uses the timed_state_exploration.zoneProject infrastructure.
			LPN lpn = new LPN();
			lpn.load(directory + separator + lpnFileName);			
			Options.set_TimingLogFile(directory + separator
					+ lpnFileName + ".tlog");

			// Load any other listed LPNs.
			ArrayList<LPN> selectedLPNs = new ArrayList<LPN>();
			// Add the current LPN to the list.
			selectedLPNs.add(lpn);
			//			for (int i=0; i < lpnList.getSelectedValues().length; i++) {
			//				 String curLPNname = (String) lpnList.getSelectedValues()[i];
			//				 LhpnFile curLPN = new LhpnFile();
			//				 curLPN.load(directory + separator + curLPNname);
			//				 selectedLPNs.add(curLPN);
			//			}

			String[] guiLPNList = lpnList.getItems();
			for (int i=0; i < guiLPNList.length; i++) {
				String curLPNname = guiLPNList[i];
				LPN curLPN = new LPN();
				curLPN.load(directory + separator + curLPNname);
				selectedLPNs.add(curLPN);
			}


			// Extract boolean variables from continuous variable inequalities.
			for(int i=0; i<selectedLPNs.size(); i++){
				selectedLPNs.get(i).parseBooleanInequalities();
			}

			/**
			 * This is what selects the timing analysis.
			 * The method setTimingAnalysisType sets a static variable
			 * in the Options class that is queried by the search method.
			 */
			if(zone.isSelected()){
				Options.setTimingAnalsysisType("zone");
			}
			else if(octagon.isSelected()){
				Options.setTimingAnalsysisType("octagon");
			}
			else{
				throw new IllegalStateException("Called the timing code"
						+ " without having selected zones or octagons.");
			}
			Options.set_resetOnce(resetOnce.isSelected());
			Options.set_displayResults(!noDisplayResult.isSelected());
			Options.set_displayDBM(displayDBM.isSelected());
			// Checking the rate optimization flag turns the optimization on.
			Options.set_rateOptimization(rateOptimization.isSelected());
			//Options.setDebugMode(true);
			
			Zone.setSubsetFlag(!timsubset.isSelected());
			Zone.setSupersetFlag(!superset.isSelected());

			Project timedStateSearch = new Project(selectedLPNs);
			if (dot.isSelected()) {
				Options.setOutputSgFlag(true);
				Options.setPrjSgPath(directory + separator);				
			}
			timedStateSearch.search();
//			String graphFileName = verifyFile.replace(".lpn", "") + "_sg.dot";
			String graphFileName = "full_sg.dot";

//			if (dot.isSelected()) {
//				stateGraphArray[0].outputLocalStateGraph(directory + separator + graphFileName);  
//			}

			if(graph.isSelected()){
				showGraph(directory + separator + graphFileName);
			}

			Options.setTimingAnalsysisType("off");

			BufferedWriter logFile = Zone.get_writeLogFile();
			
			if(logFile != null){
				try {
					logFile.close();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
			
			Zone.reset_writeLogFile();
			
			return;

		}
		if (lhpn.isSelected()) {
			abstFilename = JOptionPane.showInputDialog(this,
					"Please enter the file name for the abstracted LPN.",
					"Enter Filename", JOptionPane.PLAIN_MESSAGE);
			if (abstFilename != null) {
				if (!abstFilename.endsWith(".lpn")) {
					while (abstFilename.contains("\\.")) {
						abstFilename = JOptionPane.showInputDialog(this,
								"Please enter a valid file name for the abstracted LPN.",
								"Invalid Filename", JOptionPane.PLAIN_MESSAGE);
					}
					abstFilename = abstFilename + ".lpn";
				}
			} else {
				abstFilename = lpnFileName.replace(".lpn", "_abs.lpn");
			}
		} else {
			abstFilename = lpnFileName.replace(".lpn", "_abs.lpn");
		}
		String sourceFile;
		if (simplify.isSelected() || abstractLhpn.isSelected()) {
			String[] boolVars = lhpnFile.getBooleanVars();
			String[] contVars = lhpnFile.getContVars();
			String[] intVars = lhpnFile.getIntVars();
			String[] variables = new String[boolVars.length + contVars.length
			                                + intVars.length];
			int k = 0;
			for (int j = 0; j < contVars.length; j++) {
				variables[k] = contVars[j];
				k++;
			}
			for (int j = 0; j < intVars.length; j++) {
				variables[k] = intVars[j];
				k++;
			}
			for (int j = 0; j < boolVars.length; j++) {
				variables[k] = boolVars[j];
				k++;
			}
			if (!abstFilename.endsWith(".lpn"))
				abstFilename = abstFilename + ".lpn";
			if (abstPane.loopAbsModel.contains("Remove Variables")) {
				abstraction.abstractVars(abstPane.getIntVars());
			}
			abstraction.abstractSTG(true);
			if (!lhpn.isSelected() && !view.isSelected()) {
				abstraction.save(directory + separator + abstFilename);
			}
			sourceFile = abstFilename;
		} else {
			String[] tempArray = verifyFile.split(separator);
			sourceFile = tempArray[tempArray.length - 1];
		}
		if (!lhpn.isSelected() && !view.isSelected()) {
			abstraction.save(directory + separator + abstFilename);
		}
		if (!lhpn.isSelected() && !view.isSelected()) {
			String[] tempArray = verifyFile.split("\\.");
			String traceFilename = tempArray[0] + ".trace";
			File traceFile = new File(traceFilename);
			String pargName = "";
			String dotName = "";
			if (componentField.getText().trim().equals("")) {
				if (verifyFile.endsWith(".g")) {
					pargName = directory + separator
							+ sourceFile.replace(".g", ".prg");
					dotName = directory + separator
							+ sourceFile.replace(".g", ".dot");
				} else if (verifyFile.endsWith(".lpn")) {
					pargName = directory + separator
							+ sourceFile.replace(".lpn", ".prg");
					dotName = directory + separator
							+ sourceFile.replace(".lpn", ".dot");
				} else if (verifyFile.endsWith(".vhd")) {
					pargName = directory + separator
							+ sourceFile.replace(".vhd", ".prg");
					dotName = directory + separator
							+ sourceFile.replace(".vhd", ".dot");
				}
			} else {
				pargName = directory + separator
						+ componentField.getText().trim() + ".prg";
				dotName = directory + separator
						+ componentField.getText().trim() + ".dot";
			}
			File pargFile = new File(pargName);
			File dotFile = new File(dotName);
			if (traceFile.exists()) {
				traceFile.delete();
			}
			if (pargFile.exists()) {
				pargFile.delete();
			}
			if (dotFile.exists()) {
				dotFile.delete();
			}
			for (String s : componentList.getItems()) {
				try {
					FileInputStream in = new FileInputStream(new File(root
							+ separator + s));
					FileOutputStream out = new FileOutputStream(new File(
							directory + separator + s));
					int read = in.read();
					while (read != -1) {
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(Gui.frame,
							"Cannot update the file " + s + ".", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
			for (String s : lpnList.getItems()) {
				try {
					FileInputStream in = new FileInputStream(new File(root
							+ separator + s));
					FileOutputStream out = new FileOutputStream(new File(
							directory + separator + s));
					int read = in.read();
					while (read != -1) {
						out.write(read);
						read = in.read();
					}
					in.close();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(Gui.frame,
							"Cannot update the file " + s + ".", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}

			String options = "";
			// BDD Linkspace Size
			if (!bddSize.getText().equals("") && !bddSize.getText().equals("0")) {
				options = options + "-L" + bddSize.getText() + " ";
			}
			options = options + "-oq";
			// Timing method
			if (atacs) {
				if (untimed.isSelected()) {
					options = options + "tu";
				} else if (geometric.isSelected()) {
					options = options + "tg";
				} else if (posets.isSelected()) {
					options = options + "ts";
				} else if (bag.isSelected()) {
					options = options + "tg";
				} else if (bap.isSelected()) {
					options = options + "tp";
				} else {
					options = options + "tt";
				}
			} else {
				if (bdd.isSelected()) {
					options = options + "tB";
				} else if (dbm.isSelected()) {
					options = options + "tL";
				} else if (smt.isSelected()) {
					options = options + "tM";
				}
			}
			// Timing Options
			if (abst.isSelected()) {
				options = options + "oa";
			}
			if (partialOrder.isSelected()) {
				options = options + "op";
			}
			// Other Options
			if (dot.isSelected()) {
				options = options + "od";
			}
			if (verbose.isSelected()) {
				options = options + "ov";
			}
			// Advanced Timing Options
			if (genrg.isSelected()) {
				options = options + "oG";
			}
			if (timsubset.isSelected()) {
				options = options + "oS";
			}
			if (superset.isSelected()) {
				options = options + "oU";
			}
			if (infopt.isSelected()) {
				options = options + "oF";
			}
			if (orbmatch.isSelected()) {
				options = options + "oO";
			}
			if (interleav.isSelected()) {
				options = options + "oI";
			}
			if (prune.isSelected()) {
				options = options + "oP";
			}
			if (disabling.isSelected()) {
				options = options + "oD";
			}
			if (nofail.isSelected()) {
				options = options + "of";
			}
			if (noproj.isSelected()) {
				options = options + "oj";
			}
			if (keepgoing.isSelected()) {
				options = options + "oK";
			}
			if (explpn.isSelected()) {
				options = options + "oL";
			}
			// Other Advanced Options
			if (nochecks.isSelected()) {
				options = options + "on";
			}
			if (reduction.isSelected()) {
				options = options + "oR";
			}
			// Compilation Options
			if (newTab.isSelected()) {
				options = options + "cN";
			}
			if (postProc.isSelected()) {
				options = options + "cP";
			}
			if (redCheck.isSelected()) {
				options = options + "cR";
			}
			if (xForm2.isSelected()) {
				options = options + "cT";
			}
			if (expandRate.isSelected()) {
				options = options + "cE";
			}
			// Load file type
			if (verifyFile.endsWith(".g")) {
				options = options + "lg";
			} else if (verifyFile.endsWith(".lpn")) {
				options = options + "ll";
			} else if (verifyFile.endsWith(".vhd")
					|| verifyFile.endsWith(".vhdl")) {
				options = options + "lvslll";
			}
			// Verification Algorithms
			if (verify.isSelected()) {
				options = options + "va";
			} else if (vergate.isSelected()) {
				options = options + "vg";
			} else if (orbits.isSelected()) {
				options = options + "vo";
			} else if (search.isSelected()) {
				options = options + "vs";
			} else if (trace.isSelected()) {
				options = options + "vt";
			}
			if (graph.isSelected()) {
				options = options + "ps";
			}
			String cmd = "atacs " + options;
			String[] components = componentList.getItems();
			for (String s : components) {
				cmd = cmd + " " + s;
			}
			cmd = cmd + " " + sourceFile;
			if (!componentField.getText().trim().equals("")) {
				cmd = cmd + " " + componentField.getText().trim();
			}
			final JButton cancel = new JButton("Cancel");
			final JFrame running = new JFrame("Progress");
			WindowListener w = new WindowListener() {
				@Override
				public void windowClosing(WindowEvent arg0) {
					cancel.doClick();
					running.dispose();
				}

				@Override
				public void windowOpened(WindowEvent arg0) {
				}

				@Override
				public void windowClosed(WindowEvent arg0) {
				}

				@Override
				public void windowIconified(WindowEvent arg0) {
				}

				@Override
				public void windowDeiconified(WindowEvent arg0) {
				}

				@Override
				public void windowActivated(WindowEvent arg0) {
				}

				@Override
				public void windowDeactivated(WindowEvent arg0) {
				}
			};
			running.addWindowListener(w);
			JPanel text = new JPanel();
			JPanel progBar = new JPanel();
			JPanel button = new JPanel();
			JPanel all = new JPanel(new BorderLayout());
			JLabel label = new JLabel("Running...");
			JProgressBar progress = new JProgressBar();
			progress.setIndeterminate(true);
			text.add(label);
			progBar.add(progress);
			button.add(cancel);
			all.add(text, "North");
			all.add(progBar, "Center");
			all.add(button, "South");
			all.setOpaque(true);
			running.setContentPane(all);
			running.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			} catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = running.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			running.setLocation(x, y);
			running.setVisible(true);
			running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			work = new File(directory);
			Runtime exec = Runtime.getRuntime();
			try {
				Preferences biosimrc = Preferences.userRoot();
				String output = "";
				int exitValue = 0;
				if (biosimrc.get("biosim.verification.command", "").equals("")) {
					final Process ver = exec.exec(cmd, null, work);
					cancel.setActionCommand("Cancel");
					cancel.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							ver.destroy();
							running.setCursor(null);
							running.dispose();
						}
					});
					biosim.getExitButton().setActionCommand("Exit program");
					biosim.getExitButton().addActionListener(
							new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									ver.destroy();
									running.setCursor(null);
									running.dispose();
								}
							});
					log.addText("Executing:\n" + cmd + "\n");
					InputStream reb = ver.getInputStream();
					InputStreamReader isr = new InputStreamReader(reb);
					BufferedReader br = new BufferedReader(isr);
					FileWriter out = new FileWriter(new File(directory
							+ separator + "run.log"));
					while ((output = br.readLine()) != null) {
						out.write(output);
						out.write("\n");
					}
					out.close();
					br.close();
					isr.close();
					reb.close();
					viewLog.setEnabled(true);
					exitValue = ver.waitFor();
				} else {
					cmd = biosimrc.get("biosim.verification.command", "") + " "
							+ options + " " + sourceFile.replaceAll(".lpn", "");
					final Process ver = exec.exec(cmd, null, work);
					cancel.setActionCommand("Cancel");
					cancel.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							ver.destroy();
							running.setCursor(null);
							running.dispose();
						}
					});
					biosim.getExitButton().setActionCommand("Exit program");
					biosim.getExitButton().addActionListener(
							new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									ver.destroy();
									running.setCursor(null);
									running.dispose();
								}
							});
					log.addText("Executing:\n" + cmd + "\n");
					InputStream reb = ver.getInputStream();
					InputStreamReader isr = new InputStreamReader(reb);
					BufferedReader br = new BufferedReader(isr);
					FileWriter out = new FileWriter(new File(directory
							+ separator + "run.log"));
					while ((output = br.readLine()) != null) {
						out.write(output);
						out.write("\n");
					}
					out.close();
					br.close();
					isr.close();
					reb.close();
					viewLog.setEnabled(true);
					exitValue = ver.waitFor();
				}
				long time2 = System.nanoTime();
				long minutes;
				long hours;
				long days;
				double secs = ((time2 - time1) / 1000000000.0);
				long seconds = ((time2 - time1) / 1000000000);
				secs = secs - seconds;
				minutes = seconds / 60;
				secs = seconds % 60 + secs;
				hours = minutes / 60;
				minutes = minutes % 60;
				days = hours / 24;
				hours = hours % 60;
				String time;
				String dayLabel;
				String hourLabel;
				String minuteLabel;
				String secondLabel;
				if (days == 1) {
					dayLabel = " day ";
				} else {
					dayLabel = " days ";
				}
				if (hours == 1) {
					hourLabel = " hour ";
				} else {
					hourLabel = " hours ";
				}
				if (minutes == 1) {
					minuteLabel = " minute ";
				} else {
					minuteLabel = " minutes ";
				}
				if (seconds == 1) {
					secondLabel = " second";
				} else {
					secondLabel = " seconds";
				}
				if (days != 0) {
					time = days + dayLabel + hours + hourLabel + minutes
							+ minuteLabel + secs + secondLabel;
				} else if (hours != 0) {
					time = hours + hourLabel + minutes + minuteLabel + secs
							+ secondLabel;
				} else if (minutes != 0) {
					time = minutes + minuteLabel + secs + secondLabel;
				} else {
					time = secs + secondLabel;
				}
				log.addText("Total Verification Time: " + time + "\n\n");
				running.setCursor(null);
				running.dispose();
				FileInputStream atacsLog = new FileInputStream(new File(
						directory + separator + "atacs.log"));
				InputStreamReader atacsReader = new InputStreamReader(atacsLog);
				BufferedReader atacsBuffer = new BufferedReader(atacsReader);
				boolean success = false;
				while ((output = atacsBuffer.readLine()) != null) {
					if (output.contains("Verification succeeded.")) {
						JOptionPane.showMessageDialog(Gui.frame,
								"Verification succeeded!", "Success",
								JOptionPane.INFORMATION_MESSAGE);
						success = true;
						break;
					}
				}
				atacsBuffer.close();
				if (exitValue == 143) {
					JOptionPane.showMessageDialog(Gui.frame,
							"Verification was" + " canceled by the user.",
							"Canceled Verification", JOptionPane.ERROR_MESSAGE);
				}
				if (!success) {
					if (new File(pargName).exists()) {
						Process parg = exec.exec("parg " + pargName);
						log.addText("parg " + pargName + "\n");
						parg.waitFor();
					} else if (new File(dotName).exists()) {
						String command = biosimrc.get("biosim.general.graphviz", "") + " ";
						Process dot = exec.exec("open " + dotName);
						log.addText(command + dotName + "\n");
						dot.waitFor();
					} else {
						viewLog();
					}
				}
				if (graph.isSelected()) {
					if (dot.isSelected()) {
						String command = biosimrc.get("biosim.general.graphviz", "") + " ";
						exec.exec(command + dotName);
						log.addText("Executing:\n" + command + dotName + "\n");
					} else {
						exec.exec("parg " + pargName);
						log.addText("Executing:\nparg " + pargName + "\n");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame,
						"Unable to verify model.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			if (lhpn.isSelected()) {
				abstraction.save(tempDir + separator + abstFilename);
				biosim.addToTree(abstFilename);
			} else if (view.isSelected()) {
				abstraction.save(directory + separator + abstFilename);
				work = new File(directory + separator);
				try {
					String dotName = abstFilename.replace(".lpn", ".dot");
					new File(directory + separator + dotName).delete();
					Runtime exec = Runtime.getRuntime();
					abstraction.printDot(directory + separator + dotName);
					if (new File(directory + separator + dotName).exists()) {
						Preferences biosimrc = Preferences.userRoot();
						String command = biosimrc.get("biosim.general.graphviz", "") + " ";
						Process dot = exec.exec(command + dotName, null, work);
						log.addText(command + dotName + "\n");
						dot.waitFor();
					} else {
						JOptionPane.showMessageDialog(Gui.frame,
								"Unable to view LHPN.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* TODO: Not sure why this is not used
	private void checkDecomposition(LhpnFile lpn, LhpnFile lpnComp) {
		// Places
		for (String placeName : lpnComp.getPlaceList()) {
			if (lpn.getPlace(placeName) == null)
				System.out.println("Place " + placeName + "of component "+ lpnComp.getLabel() +" does not exist in LPN" + lpn.getLabel());
			if (lpnComp.getInitialMarking(placeName) != lpn.getInitialMarking(placeName))
				System.out.println("Initial marking for place " + placeName + "of component "+ lpnComp.getLabel() +" is not equal to that of " + lpn.getLabel());
		}
		// Transitions
		for (Transition tran : lpnComp.getAllTransitions()) {
			if (lpn.getTransition(tran.getLabel()) == null)
				System.out.println("Transition " + tran.getFullLabel() + "does not exist in LPN" + lpn.getLabel());
			else {
				Transition tranInLpn = lpn.getTransition(tran.getLabel());
				if (!tran.equals(tranInLpn)) {
					System.out.println("Transition " + tran.getFullLabel() + "is not equal to " + tranInLpn.getFullLabel());
				}				
			}
		}		
		// Variables
		for (String varName : lpnComp.getVariables()) {
			if (lpn.getVariable(varName) == null)
				System.out.println("Variable " + varName + "of component "+ lpnComp.getLabel() +" does not exist in LPN" + lpn.getLabel());
			else {
				if (!lpnComp.getVariable(varName).equals(lpn.getVariable(varName)))
					System.out.println("Variable " + varName + "of component "+ lpnComp.getLabel() +" is not equal to that in LPN " + lpn.getLabel());
			}
		}
	}
	*/

	public void saveAs() {
		String newName = JOptionPane.showInputDialog(Gui.frame,
				"Enter Verification name:", "Verification Name",
				JOptionPane.PLAIN_MESSAGE);
		if (newName == null) {
			return;
		}
		if (!newName.endsWith(".ver")) {
			newName = newName + ".ver";
		}
		save(newName);
	}

	public void save() {
		save(verFile);
	}

	public void save(String filename) {
		try {
			Properties prop = new Properties();
//			FileInputStream in = new FileInputStream(new File(directory
//					+ separator + filename));
//			prop.load(in);
//			in.close();
			prop.setProperty("verification.file", verifyFile);
			if (!bddSize.equals("")) {
				prop.setProperty("verification.bddSize", this.bddSize.getText()
						.trim());
			}
			if (!componentField.getText().trim().equals("")) {
				prop.setProperty("verification.component", componentField
						.getText().trim());
			} else {
				prop.remove("verification.component");
			}
			String[] components = componentList.getItems();
			if (components.length == 0) {
				for (Object s : prop.keySet()) {
					if (s.toString().startsWith("verification.compList")) {
						prop.remove(s);
					}
				}
			} else {
				for (Integer i = 0; i < components.length; i++) {
					prop.setProperty("verification.compList" + i.toString(),
							components[i]);
				}
			}
			String[] lpns = lpnList.getItems();
			if (lpns.length == 0) {
				for (Object s : prop.keySet()) {
					if (s.toString().startsWith("verification.lpnList")) {
						prop.remove(s);
					}
				}
			} else {
				for (Integer i = 0; i < lpns.length; i++) {
					prop.setProperty("verification.lpnList" + i.toString(),
							lpns[i]);
				}
			}
			if (none.isSelected()) {
				prop.setProperty("verification.abstraction", "none");
			} else if (simplify.isSelected()) {
				prop.setProperty("verification.abstraction", "simplify");
			} else {
				prop.setProperty("verification.abstraction", "abstract");
			}
			if (atacs) {
				if (untimed.isSelected()) {
					prop.setProperty("verification.timing.methods", "untimed");
				} else if (geometric.isSelected()) {
					prop.setProperty("verification.timing.methods", "geometric");
				} else if (posets.isSelected()) {
					prop.setProperty("verification.timing.methods", "posets");
				} else if (bag.isSelected()) {
					prop.setProperty("verification.timing.methods", "bag");
				} else if (bap.isSelected()) {
					prop.setProperty("verification.timing.methods", "bap");
				} else if (baptdc.isSelected()) {
					prop.setProperty("verification.timing.methods", "baptdc");
				} else if (lhpn.isSelected()) {
					prop.setProperty("verification.timing.methods", "lhpn");
				} else {
					prop.setProperty("verification.timing.methods", "view");
				}
			} else {
				if (bdd.isSelected()) {
					prop.setProperty("verification.timing.methods", "bdd");
				} else if (dbm.isSelected()) {
					prop.setProperty("verification.timing.methods", "dbm");
				} else if (smt.isSelected()) {
					prop.setProperty("verification.timing.methods", "smt");
				} else if (untimedStateSearch.isSelected()) {
					prop.setProperty("verification.timing.methods", "untimedStateSearch");
				} else if (lhpn.isSelected()) {
					prop.setProperty("verification.timing.methods", "lhpn");
				} else if (zone.isSelected()) {
					prop.setProperty("verification.timing.methods", "zone");
				} else if (octagon.isSelected()) {
					prop.setProperty("verification.timing.methods", "octagon");
				} else {
					prop.setProperty("verification.timing.methods", "view");
				}
			}
			if (abst.isSelected()) {
				prop.setProperty("verification.Abst", "true");
			} else {
				prop.setProperty("verification.Abst", "false");
			}
			if (partialOrder.isSelected()) {
				prop.setProperty("verification.partial.order", "true");
			} else {
				prop.setProperty("verification.partial.order", "false");
			}
			if (dot.isSelected()) {
				prop.setProperty("verification.Dot", "true");
			} else {
				prop.setProperty("verification.Dot", "false");
			}
			if (verbose.isSelected()) {
				prop.setProperty("verification.Verb", "true");
			} else {
				prop.setProperty("verification.Verb", "false");
			}
			if (graph.isSelected()) {
				prop.setProperty("verification.Graph", "true");
			} else {
				prop.setProperty("verification.Graph", "false");
			}
//			if (untimedPOR.isSelected()) {
//				prop.setProperty("verification.UntimedPOR", "true");
//			} else {
//				prop.setProperty("verification.UntimedPOR", "false");
//			}
			if (decomposeLPN.isSelected()) {
				prop.setProperty("verification.DecomposeLPN", "true");
			} else {
				prop.setProperty("verification.DecomposeLPN", "false");
			}
			if (multipleLPNs.isSelected()) {
				prop.setProperty("verification.MultipleLPNs", "true");
			} else {
				prop.setProperty("verification.MultipleLPNs", "false");
			}
			if (verify.isSelected()) {
				prop.setProperty("verification.algorithm", "verify");
			} else if (vergate.isSelected()) {
				prop.setProperty("verification.algorithm", "vergate");
			} else if (orbits.isSelected()) {
				prop.setProperty("verification.algorithm", "orbits");
			} else if (search.isSelected()) {
				prop.setProperty("verification.algorithm", "search");
			} else if (trace.isSelected()) {
				prop.setProperty("verification.algorithm", "trace");
			}
			if (newTab.isSelected()) {
				prop.setProperty("verification.compilation.newTab", "true");
			} else {
				prop.setProperty("verification.compilation.newTab", "false");
			}
			if (postProc.isSelected()) {
				prop.setProperty("verification.compilation.postProc", "true");
			} else {
				prop.setProperty("verification.compilation.postProc", "false");
			}
			if (redCheck.isSelected()) {
				prop.setProperty("verification.compilation.redCheck", "true");
			} else {
				prop.setProperty("verification.compilation.redCheck", "false");
			}
			if (xForm2.isSelected()) {
				prop.setProperty("verification.compilation.xForm2", "true");
			} else {
				prop.setProperty("verification.compilation.xForm2", "false");
			}
			if (expandRate.isSelected()) {
				prop.setProperty("verification.compilation.expandRate", "true");
			} else {
				prop
				.setProperty("verification.compilation.expandRate",
						"false");
			}
			if (genrg.isSelected()) {
				prop.setProperty("verification.timing.genrg", "true");
			} else {
				prop.setProperty("verification.timing.genrg", "false");
			}
			if (timsubset.isSelected()) {
				prop.setProperty("verification.timing.subset", "true");
			} else {
				prop.setProperty("verification.timing.subset", "false");
			}
			if (superset.isSelected()) {
				prop.setProperty("verification.timing.superset", "true");
			} else {
				prop.setProperty("verification.timing.superset", "false");
			}
			if (infopt.isSelected()) {
				prop.setProperty("verification.timing.infopt", "true");
			} else {
				prop.setProperty("verification.timing.infopt", "false");
			}
			if (orbmatch.isSelected()) {
				prop.setProperty("verification.timing.orbmatch", "true");
			} else {
				prop.setProperty("verification.timing.orbmatch", "false");
			}
			if (interleav.isSelected()) {
				prop.setProperty("verification.timing.interleav", "true");
			} else {
				prop.setProperty("verification.timing.interleav", "false");
			}
			if (prune.isSelected()) {
				prop.setProperty("verification.timing.prune", "true");
			} else {
				prop.setProperty("verification.timing.prune", "false");
			}
			if (disabling.isSelected()) {
				prop.setProperty("verification.timing.disabling", "true");
			} else {
				prop.setProperty("verification.timing.disabling", "false");
			}
			if (nofail.isSelected()) {
				prop.setProperty("verification.timing.nofail", "true");
			} else {
				prop.setProperty("verification.timing.nofail", "false");
			}
			if (nofail.isSelected()) {
				prop.setProperty("verification.timing.noproj", "true");
			} else {
				prop.setProperty("verification.timing.noproj", "false");
			}
			if (keepgoing.isSelected()) {
				prop.setProperty("verification.timing.keepgoing", "true");
			} else {
				prop.setProperty("verification.timing.keepgoing", "false");
			}
			if (explpn.isSelected()) {
				prop.setProperty("verification.timing.explpn", "true");
			} else {
				prop.setProperty("verification.timing.explpn", "false");
			}
			if (nochecks.isSelected()) {
				prop.setProperty("verification.other.nochecks", "true");
			} else {
				prop.setProperty("verification.other.nochecks", "false");
			}
			if (reduction.isSelected()) {
				prop.setProperty("verification.other.reduction", "true");
			} else {
				prop.setProperty("verification.other.reduction", "false");
			}
			if (resetOnce.isSelected()) {
				prop.setProperty("verification.other.resetOnce", "true");
			} else {
				prop.setProperty("verification.other.resetOnce", "false");
			}
			if (rateOptimization.isSelected()) {
				prop.setProperty("verification.other.rateOptimization", "true");
			} else {
				prop.setProperty("verification.other.rateOptimization", "false");
			}
			if (noDisplayResult.isSelected()) {
				prop.setProperty("verification.other.noDisplayResult", "true");
			} else {
				prop.setProperty("verification.other.noDisplayResult", "false");
			}
			if (displayDBM.isSelected()) {
				prop.setProperty("verification.other.displayDBM", "true");
			} else {
				prop.setProperty("verification.other.displayDBM", "false");
			}
			String intVars = "";
			for (int i = 0; i < abstPane.listModel.getSize(); i++) {
				if (abstPane.listModel.getElementAt(i) != null) {
					intVars = intVars + abstPane.listModel.getElementAt(i)
							+ " ";
				}
			}
			if (sListModel.size() > 0) {
				String list = "";
				for (Object o : sListModel.toArray()) {
					list = list + o + " ";
				}
				list.trim();
				prop.put("verification.sList", list);
			} else {
				prop.remove("verification.sList");
			}
			if (!intVars.equals("")) {
				prop.setProperty("abstraction.interesting", intVars.trim());
			} else {
				prop.remove("abstraction.interesting");
			}
			for (Integer i=0; i<abstPane.preAbsModel.size(); i++) {
				prop.setProperty("abstraction.transform." + abstPane.preAbsModel.getElementAt(i).toString(), "preloop" + i.toString());
			}
			for (Integer i=0; i<abstPane.loopAbsModel.size(); i++) {
				if (abstPane.loopAbsModel.getElementAt(i)==null) continue;
				if (abstPane.preAbsModel.contains(abstPane.loopAbsModel.getElementAt(i))) {
					String value = prop.getProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString());
					value = value + "mainloop" + i.toString();
					prop.setProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString(), value);
				}
				else {
					prop.setProperty("abstraction.transform." + abstPane.loopAbsModel.getElementAt(i).toString(), "mainloop" + i.toString());
				}
			}
			for (Integer i=0; i<abstPane.postAbsModel.size(); i++) {
				if (abstPane.preAbsModel.contains(abstPane.postAbsModel.getElementAt(i)) || abstPane.preAbsModel.contains(abstPane.postAbsModel.get(i))) {
					String value = prop.getProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString());
					value = value + "postloop" + i.toString();
					prop.setProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString(), value);
				}
				else {
					prop.setProperty("abstraction.transform." + abstPane.postAbsModel.getElementAt(i).toString(), "postloop" + i.toString());
				}
			}
			for (String s : abstPane.transforms) {
				if (!abstPane.preAbsModel.contains(s)
						&& !abstPane.loopAbsModel.contains(s)
						&& !abstPane.postAbsModel.contains(s)) {
					prop.remove(s);
				}
			}
			if (!abstPane.factorField.getText().equals("")) {
				prop.setProperty("abstraction.factor", abstPane.factorField
						.getText());
			}
			if (!abstPane.iterField.getText().equals("")) {
				prop.setProperty("abstraction.iterations", abstPane.iterField
						.getText());
			}
			FileOutputStream out = new FileOutputStream(new File(directory
					+ separator + verFile));
			prop.store(out, verifyFile);
			out.close();
			log.addText("Saving Parameter File:\n" + directory + separator
					+ verFile + "\n");
			change = false;
			oldBdd = bddSize.getText();
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to save parameter file!", "Error Saving File",
					JOptionPane.ERROR_MESSAGE);
		}
		for (String s : componentList.getItems()) {
			try {
				new File(directory + separator + s).createNewFile();
				FileInputStream in = new FileInputStream(new File(root
						+ separator + s));
				FileOutputStream out = new FileOutputStream(new File(directory
						+ separator + s));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame,
						"Cannot add the selected component.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		for (String s : lpnList.getItems()) {
			try {
				new File(directory + separator + s).createNewFile();
				FileInputStream in = new FileInputStream(new File(root
						+ separator + s));
				FileOutputStream out = new FileOutputStream(new File(directory
						+ separator + s));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame,
						"Cannot add the selected LPN.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void reload() {
	}

	public void viewCircuit() {
		String[] getFilename;
		if (componentField.getText().trim().equals("")) {
			getFilename = verifyFile.split("\\.");
		} else {
			getFilename = new String[1];
			getFilename[0] = componentField.getText().trim();
		}
		String circuitFile = getFilename[0] + ".prs";
		try {
			if (new File(circuitFile).exists()) {
				File log = new File(circuitFile);
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"Circuit View", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No circuit view exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean getViewTraceEnabled() {
		return viewTrace.isEnabled();
	}

	public boolean getViewLogEnabled() {
		return viewLog.isEnabled();
	}

	public void viewTrace() {
		String[] getFilename = verifyFile.split("\\.");
		String traceFilename = getFilename[0] + ".trace";
		try {
			if (new File(directory + separator + traceFilename).exists()) {
				File log = new File(directory + separator + traceFilename);
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls,
						"Trace View", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No trace file exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane
			.showMessageDialog(Gui.frame, "Unable to view trace.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLog() {
		try {
			if (new File(directory + separator + "run.log").exists()) {
				File log = new File(directory + separator + "run.log");
				BufferedReader input = new BufferedReader(new FileReader(log));
				String line = null;
				JTextArea messageArea = new JTextArea();
				while ((line = input.readLine()) != null) {
					messageArea.append(line);
					messageArea.append(System.getProperty("line.separator"));
				}
				input.close();
				messageArea.setLineWrap(true);
				messageArea.setWrapStyleWord(true);
				messageArea.setEditable(false);
				JScrollPane scrolls = new JScrollPane();
				scrolls.setMinimumSize(new Dimension(500, 500));
				scrolls.setPreferredSize(new Dimension(500, 500));
				scrolls.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scrolls, "Run Log",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean hasChanged() {
		if (!oldBdd.equals(bddSize.getText())) {
			return true;
		}
		return change;
	}

	public boolean isSimplify() {
		if (simplify.isSelected())
			return true;
		return false;
	}

	public void copyFile() {
		String[] tempArray = verifyFile.split(separator);
		String sourceFile = tempArray[tempArray.length - 1];
		String[] workArray = directory.split(separator);
		String workDir = "";
		for (int i = 0; i < (workArray.length - 1); i++) {
			workDir = workDir + workArray[i] + separator;
		}
		try {
			File newFile = new File(directory + separator + sourceFile);
			newFile.createNewFile();
			FileOutputStream copyin = new FileOutputStream(newFile);
			FileInputStream copyout = new FileInputStream(new File(workDir
					+ separator + sourceFile));
			int read = copyout.read();
			while (read != -1) {
				copyin.write(read);
				read = copyout.read();
			}
			copyin.close();
			copyout.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Gui.frame, "Cannot copy file "
					+ sourceFile, "Copy Error", JOptionPane.ERROR_MESSAGE);
		}
		/* TODO Test Assembly File compilation */
		if (sourceFile.endsWith(".s") || sourceFile.endsWith(".inst")) {
			biosim.copySFiles(verifyFile, directory);
			try {
				String preprocCmd;
				if (lema) {
					preprocCmd = System.getenv("LEMA") + "/bin/s2lpn " + verifyFile;
				}
				else if (atacs) {
					preprocCmd = System.getenv("ATACS") + "/bin/s2lpn " + verifyFile;
				}
				else {
					preprocCmd = System.getenv("BIOSIM") + "/bin/s2lpn " + verifyFile;
				}
				//for (Object o : sListModel.toArray()) {
				//	preprocCmd = preprocCmd + " " + o.toString();
				//}
				File work = new File(directory);
				Runtime exec = Runtime.getRuntime();
				Process preproc = exec.exec(preprocCmd, null, work);
				log.addText("Executing:\n" + preprocCmd + "\n");
				preproc.waitFor();
				if (verifyFile.endsWith(".s")) {
					verifyFile.replace(".s", ".lpn");
				}
				else {
					verifyFile.replace(".inst", ".lpn");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Error with preprocessing.", "Error",
						JOptionPane.ERROR_MESSAGE);
				//e.printStackTrace();
			}
		} else if (sourceFile.endsWith(".xml")) {
			BioModel bioModel = new BioModel(workDir);
			bioModel.load(workDir + separator + sourceFile);
			bioModel.saveAsLPN(directory + separator + sourceFile.replace(".xml", ".lpn"));
		}
	}

	public String getVerName() {
		String verName = verFile.replace(".ver", "");
		return verName;
	}

	public AbstPane getAbstPane() {
		return abstPane;
	}

	/**
	 * Calls the appropriate dot program to show the graph.
	 * @param fileName The absolute file name.
	 */
	public void showGraph(String fileName)
	{
		File file = new File(fileName);

		File work = file.getParentFile();
		try {
			Runtime exec = Runtime.getRuntime();
			if (new File(fileName).exists()) {

				long kB = 1024;	// number of bytes in a kilobyte.

				long fileSize = file.length()/kB; // Size of file in megabytes.

				// If the file is larger than a given amount of megabytes,
				// then give the user the chance to cancel the operation.

				int thresholdSize = 100;	// Specifies the threshold for giving the
				// user the option to not attempt to open the file.
				if(fileSize > thresholdSize)
				{
					int answer = JOptionPane.showConfirmDialog(Gui.frame,
							"The size of the file exceeds " + thresholdSize + " kB."
									+ "The file may not open. Do you want to continue?", 
									"Do you want to continue?", JOptionPane.YES_NO_OPTION);

					if(answer == JOptionPane.NO_OPTION)
					{
						return;
					}
				}
				Preferences biosimrc = Preferences.userRoot();
				String command = biosimrc.get("biosim.general.graphviz", "") + " ";
				Process dot = exec.exec(command + fileName, null, work);
				log.addText(command + fileName + "\n");
				dot.waitFor();
			} else {
				JOptionPane.showMessageDialog(Gui.frame,
						"Unable to view dot file.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static boolean canPerformMarkovianAnalysis(LPN lpn) {
		for (String trans : lpn.getTransitionList()) {
			if (!lpn.isExpTransitionRateTree(trans)) {
//				JOptionPane.showMessageDialog(Gui.frame, "LPN has transitions without exponential delay.",
//						"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			for (String var : lpn.getVariables()) {
				if (lpn.isRandomBoolAssignTree(trans, var)) {
//					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
//							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (lpn.isRandomContAssignTree(trans, var)) {
//					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
//							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				if (lpn.isRandomIntAssignTree(trans, var)) {
//					JOptionPane.showMessageDialog(Gui.frame, "LPN has assignments containing random functions.",
//							"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		if (lpn.getContVars().length > 0) {
//			JOptionPane.showMessageDialog(Gui.frame, "LPN contains continuous variables.",
//					"Unable to Perform Markov Chain Analysis", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;		
	}
}