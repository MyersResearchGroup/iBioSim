package frontend.synthesis.async;


import javax.swing.*;

import backend.biomodel.util.Utility;
import backend.util.GlobalConstants;
import frontend.biomodel.gui.util.PropertyList;
import frontend.main.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.ArrayList;
import java.util.prefs.Preferences;


/**
 * This class creates a GUI front end for the Synthesis tool. It provides the
 * necessary options to run an atacs simulation of the circuit and manage the
 * results from the ATACS GUI.
 * 
 * @author Kevin Jones
 */

public class Synthesis extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton save, run, viewCircuit, viewRules, viewTrace, viewLog, addComponent,
			removeComponent;

	private JLabel algorithm, timingMethod, technology, otherOptions, otherOptions2, maxSizeLabel,
			gateDelayLabel, compilation, bddSizeLabel, advTiming, synthesisOptions, pruning;

	private JRadioButton untimed, geometric, posets, bag, bap, baptdc, atomicGates, generalizedC,
			standardC, bmGc, bm2, singleCube, multicube, bdd, direct;

	private JCheckBox abst, partialOrder, dot, verbose, quiet, noinsert, shareGate, combo, exact,
			manual, inponly, notFirst, preserve, ordered, subset, unsafe, expensive, conflict,
			reachable, dumb, genrg, timsubset, superset, infopt, orbmatch, interleav, prune,
			disabling, nofail, keepgoing, explpn, nochecks, reduction, minins, newTab, postProc,
			redCheck, xForm2, expandRate, graph;

	private JTextField maxSize, gateDelay, bddSize, backgroundField, componentField;

	private ButtonGroup timingMethodGroup, technologyGroup, algorithmGroup;

	private String directory, separator, root, synthFile, synthesisFile, sourceFile,
			sourceFileNoPath;

	private String oldMax, oldDelay, oldBdd;

	private PropertyList componentList;

	private boolean change;

	private Log log;

	private Gui biosim;

	/**
	 * This is the constructor for the Synthesis class. It initializes all the
	 * input fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public Synthesis(String directory, String filename, Log log, Gui biosim) {
		separator = GlobalConstants.separator;

		this.biosim = biosim;
		this.directory = directory;
		this.log = log;
		this.sourceFile = filename;
		String[] getFilename = directory.split(separator);
		synthFile = getFilename[getFilename.length - 1] + ".syn";
		String[] tempDir = directory.split(separator);
		root = tempDir[0];
		for (int i = 1; i < tempDir.length - 1; i++) {
			root = root + separator + tempDir[i];
		}

		JPanel timingRadioPanel = new JPanel();
		timingRadioPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel technologyPanel = new JPanel();
		technologyPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel otherPanel = new JPanel();
		otherPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel valuePanel = new JPanel();
		valuePanel.setMaximumSize(new Dimension(1000, 35));
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel buttonPanel = new JPanel();
		JPanel compilationPanel = new JPanel();
		compilationPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel advancedPanel = new JPanel();
		advancedPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel bddPanel = new JPanel();
		bddPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel synthPanel = new JPanel();
		synthPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel pruningPanel = new JPanel();
		pruningPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel advTimingPanel = new JPanel();
		advTimingPanel.setMaximumSize(new Dimension(1000, 62));
		advTimingPanel.setPreferredSize(new Dimension(1000, 62));

		maxSize = new JTextField("4");
		gateDelay = new JTextField("0 inf");
		bddSize = new JTextField("");
		maxSize.setPreferredSize(new Dimension(30, 18));
		gateDelay.setPreferredSize(new Dimension(70, 18));
		bddSize.setPreferredSize(new Dimension(40, 18));
		oldMax = maxSize.getText();
		oldDelay = gateDelay.getText();
		oldBdd = bddSize.getText();
		componentList = new PropertyList("");
		componentField = new JTextField();

		algorithm = new JLabel("Synthesis Algorithm:");
		timingMethod = new JLabel("Timing Method:");
		technology = new JLabel("Technology:");
		otherOptions = new JLabel("Other Options:");
		otherOptions2 = new JLabel("Other Options:");
		maxSizeLabel = new JLabel("Max Size:");
		gateDelayLabel = new JLabel("Gate Delay:");
		compilation = new JLabel("Compilation Options:");
		bddSizeLabel = new JLabel("BDD Linkspace Size:");
		advTiming = new JLabel("Timing Options:");
		synthesisOptions = new JLabel("Synthesis Options:");
		pruning = new JLabel("Pruning Options:");

		// Initializes the radio buttons and check boxes
		// Timing Methods
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
		// Technology Options
		atomicGates = new JRadioButton("Atomic Gates");
		generalizedC = new JRadioButton("Generalized-C");
		standardC = new JRadioButton("Standard-C");
		bmGc = new JRadioButton("BM gC");
		bm2 = new JRadioButton("BM 2-level");
		atomicGates.addActionListener(this);
		generalizedC.addActionListener(this);
		standardC.addActionListener(this);
		bmGc.addActionListener(this);
		bm2.addActionListener(this);
		// Other Basic Options
		dot = new JCheckBox("Dot");
		verbose = new JCheckBox("Verbose");
		quiet = new JCheckBox("Quiet");
		noinsert = new JCheckBox("No Insert");
		graph = new JCheckBox("Show State Graph");
		dot.addActionListener(this);
		verbose.addActionListener(this);
		quiet.addActionListener(this);
		noinsert.addActionListener(this);
		graph.addActionListener(this);
		// Synthesis Algorithms
		singleCube = new JRadioButton("Single Cube");
		multicube = new JRadioButton("Multicube");
		bdd = new JRadioButton("BDD");
		direct = new JRadioButton("Direct");
		singleCube.addActionListener(this);
		multicube.addActionListener(this);
		bdd.addActionListener(this);
		direct.addActionListener(this);
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
		keepgoing = new JCheckBox("Keep going");
		explpn = new JCheckBox("Expand LPN");
		genrg.addActionListener(this);
		timsubset.addActionListener(this);
		superset.addActionListener(this);
		infopt.addActionListener(this);
		orbmatch.addActionListener(this);
		interleav.addActionListener(this);
		prune.addActionListener(this);
		disabling.addActionListener(this);
		nofail.addActionListener(this);
		keepgoing.addActionListener(this);
		explpn.addActionListener(this);
		// Advanced Synthesis Options
		shareGate = new JCheckBox("Share Gate");
		combo = new JCheckBox("Combo");
		exact = new JCheckBox("Exact");
		manual = new JCheckBox("Manual");
		inponly = new JCheckBox("Input Only");
		shareGate.addActionListener(this);
		combo.addActionListener(this);
		exact.addActionListener(this);
		manual.addActionListener(this);
		inponly.addActionListener(this);
		// Pruning Options
		notFirst = new JCheckBox("Not First");
		preserve = new JCheckBox("Preserve");
		ordered = new JCheckBox("Ordered");
		subset = new JCheckBox("Subset");
		unsafe = new JCheckBox("Unsafe");
		expensive = new JCheckBox("Expensive");
		conflict = new JCheckBox("Conflict");
		reachable = new JCheckBox("Reachable");
		dumb = new JCheckBox("Dumb");
		notFirst.addActionListener(this);
		preserve.addActionListener(this);
		ordered.addActionListener(this);
		subset.addActionListener(this);
		unsafe.addActionListener(this);
		expensive.addActionListener(this);
		conflict.addActionListener(this);
		reachable.addActionListener(this);
		dumb.addActionListener(this);
		// Other Advanced Options
		nochecks = new JCheckBox("No checks");
		reduction = new JCheckBox("Reduction");
		minins = new JCheckBox("Limit Transition Point Size");
		nochecks.addActionListener(this);
		reduction.addActionListener(this);
		minins.addActionListener(this);
		// Component List
		addComponent = new JButton("Add Component");
		removeComponent = new JButton("Remove Component");
		GridBagConstraints constraints = new GridBagConstraints();
		JPanel componentPanel = Utility.createPanel(this, "Components", componentList,
				addComponent, removeComponent, null);
		constraints.gridx = 0;
		constraints.gridy = 1;

		timingMethodGroup = new ButtonGroup();
		technologyGroup = new ButtonGroup();
		algorithmGroup = new ButtonGroup();

		untimed.setSelected(true);
		atomicGates.setSelected(true);
		singleCube.setSelected(true);

		// Groups the radio buttons
		timingMethodGroup.add(untimed);
		timingMethodGroup.add(geometric);
		timingMethodGroup.add(posets);
		timingMethodGroup.add(bag);
		timingMethodGroup.add(bap);
		timingMethodGroup.add(baptdc);
		technologyGroup.add(atomicGates);
		technologyGroup.add(generalizedC);
		technologyGroup.add(standardC);
		technologyGroup.add(bmGc);
		technologyGroup.add(bm2);
		algorithmGroup.add(singleCube);
		algorithmGroup.add(multicube);
		algorithmGroup.add(bdd);
		algorithmGroup.add(direct);

		JPanel basicOptions = new JPanel();
		JPanel advOptions = new JPanel();

		// Adds the buttons to their panels
		timingRadioPanel.add(timingMethod);
		timingRadioPanel.add(untimed);
		timingRadioPanel.add(geometric);
		timingRadioPanel.add(posets);
		timingRadioPanel.add(bag);
		timingRadioPanel.add(bap);
		timingRadioPanel.add(baptdc);

		technologyPanel.add(technology);
		technologyPanel.add(atomicGates);
		technologyPanel.add(generalizedC);
		technologyPanel.add(standardC);
		technologyPanel.add(bmGc);
		technologyPanel.add(bm2);

		otherPanel.add(otherOptions);
		otherPanel.add(verbose);
		otherPanel.add(quiet);
		otherPanel.add(noinsert);
		otherPanel.add(graph);

		valuePanel.add(maxSizeLabel);
		valuePanel.add(maxSize);
		valuePanel.add(gateDelayLabel);
		valuePanel.add(gateDelay);

		algorithmPanel.add(algorithm);
		algorithmPanel.add(singleCube);
		algorithmPanel.add(multicube);
		algorithmPanel.add(bdd);
		algorithmPanel.add(direct);

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
		advTimingPanel.add(keepgoing);
		advTimingPanel.add(explpn);

		synthPanel.add(synthesisOptions);
		synthPanel.add(shareGate);
		synthPanel.add(combo);
		synthPanel.add(exact);
		synthPanel.add(manual);
		synthPanel.add(inponly);

		pruningPanel.add(pruning);
		pruningPanel.add(notFirst);
		pruningPanel.add(preserve);
		pruningPanel.add(ordered);
		pruningPanel.add(subset);
		pruningPanel.add(unsafe);
		pruningPanel.add(expensive);
		pruningPanel.add(conflict);
		pruningPanel.add(reachable);
		pruningPanel.add(dumb);

		advancedPanel.add(otherOptions2);
		advancedPanel.add(nochecks);
		advancedPanel.add(reduction);
		advancedPanel.add(minins);
		bddPanel.add(bddSizeLabel);
		bddPanel.add(bddSize);

		// load parameters
		Properties load = new Properties();
		synthesisFile = "";
		try {
			FileInputStream in = new FileInputStream(new File(directory + separator + synthFile));
			load.load(in);
			in.close();
			if (load.containsKey("synthesis.file")) {
				String[] getProp = load.getProperty("synthesis.file").split(separator);
				synthesisFile = directory.substring(0, directory.length()
						- getFilename[getFilename.length - 1].length())
						+ getProp[getProp.length - 1];
			}
			if (load.containsKey("synthesis.source")) {
				sourceFile = load.getProperty("synthesis.source");
			}
			if (load.containsKey("synthesis.Max")) {
				maxSize.setText(load.getProperty("synthesis.Max"));
			}
			if (load.containsKey("synthesis.Delay")) {
				gateDelay.setText(load.getProperty("synthesis.Delay"));
			}
			if (load.containsKey("synthesis.bddSize")) {
				bddSize.setText(load.getProperty("synthesis.bddSize"));
			}
			if (load.containsKey("synthesis.Component") && load.getProperty("synthesis.Component") != null) {
				componentField.setText(load.getProperty("synthesis.Component"));
			}
			Integer i = 0;
			while (load.containsKey("synthesis.compList" + i.toString())) {
				componentList.addItem(load.getProperty("synthesis.compList" + i.toString()));
				i++;
			}
			if (load.containsKey("synthesis.timing.methods")) {
				if (load.getProperty("synthesis.timing.methods").equals("untimed")) {
					untimed.setSelected(true);
				}
				else if (load.getProperty("synthesis.timing.methods").equals("geometric")) {
					geometric.setSelected(true);
				}
				else if (load.getProperty("synthesis.timing.methods").equals("posets")) {
					posets.setSelected(true);
				}
				else if (load.getProperty("synthesis.timing.methods").equals("bag")) {
					bag.setSelected(true);
				}
				else if (load.getProperty("synthesis.timing.methods").equals("bap")) {
					bap.setSelected(true);
				}
				else {
					baptdc.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.abst")) {
				if (load.getProperty("synthesis.abst").equals("true")) {
					abst.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.partial.order")) {
				if (load.getProperty("synthesis.partial.order").equals("true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.technology")) {
				if (load.getProperty("synthesis.technology").equals("atomicGates")) {
					atomicGates.setSelected(true);
				}
				else if (load.getProperty("synthesis.technology").equals("generalizedC")) {
					generalizedC.setSelected(true);
				}
				else if (load.getProperty("synthesis.technology").equals("standardC")) {
					standardC.setSelected(true);
				}
				else if (load.getProperty("synthesis.technology").equals("bmGc")) {
					bmGc.setSelected(true);
				}
				else {
					bm2.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.Dot")) {
				if (load.getProperty("synthesis.Dot").equals("true")) {
					dot.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.Verb")) {
				if (load.getProperty("synthesis.Verb").equals("true")) {
					verbose.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.quiet")) {
				if (load.getProperty("synthesis.quiet").equals("true")) {
					quiet.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.noinsert")) {
				if (load.getProperty("synthesis.noinsert").equals("true")) {
					noinsert.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.graph")) {
				if (load.getProperty("synthesis.graph").equals("true")) {
					graph.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.partial.order")) {
				if (load.getProperty("synthesis.partial.order").equals("true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.partial.order")) {
				if (load.getProperty("synthesis.partial.order").equals("true")) {
					partialOrder.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.algorithm")) {
				if (load.getProperty("synthesis.algorithm").equals("singleCube")) {
					singleCube.setSelected(true);
				}
				else if (load.getProperty("synthesis.algorithm").equals("multicube")) {
					multicube.setSelected(true);
				}
				else if (load.getProperty("synthesis.algorithm").equals("bdd")) {
					bdd.setSelected(true);
				}
				else {
					direct.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.compilation.newTab")) {
				if (load.getProperty("synthesis.compilation.newTab").equals("true")) {
					newTab.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.compilation.postProc")) {
				if (load.getProperty("synthesis.compilation.postProc").equals("true")) {
					postProc.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.compilation.redCheck")) {
				if (load.getProperty("synthesis.compilation.redCheck").equals("true")) {
					redCheck.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.compilation.xForm2")) {
				if (load.getProperty("synthesis.compilation.xForm2").equals("true")) {
					xForm2.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.compilation.expandRate")) {
				if (load.getProperty("synthesis.compilation.expandRate").equals("true")) {
					expandRate.setSelected(true);
				}
			}

			if (load.containsKey("synthesis.timing.genrg")) {
				if (load.getProperty("synthesis.timing.genrg").equals("true")) {
					genrg.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.subset")) {
				if (load.getProperty("synthesis.timing.subset").equals("true")) {
					timsubset.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.superset")) {
				if (load.getProperty("synthesis.timing.superset").equals("true")) {
					superset.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.infopt")) {
				if (load.getProperty("synthesis.timing.infopt").equals("true")) {
					infopt.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.orbmatch")) {
				if (load.getProperty("synthesis.timing.orbmatch").equals("true")) {
					orbmatch.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.interleav")) {
				if (load.getProperty("synthesis.timing.interleav").equals("true")) {
					interleav.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.prune")) {
				if (load.getProperty("synthesis.timing.prune").equals("true")) {
					prune.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.disabling")) {
				if (load.getProperty("synthesis.timing.disabling").equals("true")) {
					disabling.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.nofail")) {
				if (load.getProperty("synthesis.timing.nofail").equals("true")) {
					nofail.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.timing.explpn")) {
				if (load.getProperty("synthesis.timing.explpn").equals("true")) {
					explpn.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.options.shareGate")) {
				if (load.getProperty("synthesis.options.shareGate").equals("true")) {
					shareGate.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.options.combo")) {
				if (load.getProperty("synthesis.options.combo").equals("true")) {
					combo.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.options.exact")) {
				if (load.getProperty("synthesis.options.exact").equals("true")) {
					exact.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.options.manual")) {
				if (load.getProperty("synthesis.options.manual").equals("true")) {
					manual.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.options.inponly")) {
				if (load.getProperty("synthesis.options.inponly").equals("true")) {
					inponly.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.notFirst")) {
				if (load.getProperty("synthesis.pruning.notFirst").equals("true")) {
					notFirst.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.preserve")) {
				if (load.getProperty("synthesis.pruning.preserve").equals("true")) {
					preserve.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.ordered")) {
				if (load.getProperty("synthesis.pruning.ordered").equals("true")) {
					ordered.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.subset")) {
				if (load.getProperty("synthesis.pruning.subset").equals("true")) {
					subset.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.unsafe")) {
				if (load.getProperty("synthesis.pruning.unsafe").equals("true")) {
					unsafe.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.expensive")) {
				if (load.getProperty("synthesis.pruning.expensive").equals("true")) {
					expensive.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.conflict")) {
				if (load.getProperty("synthesis.pruning.conflict").equals("true")) {
					conflict.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.reachable")) {
				if (load.getProperty("synthesis.pruning.reachable").equals("true")) {
					reachable.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.pruning.dumb")) {
				if (load.getProperty("synthesis.pruning.dumb").equals("true")) {
					dumb.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.nochecks")) {
				if (load.getProperty("synthesis.nochecks").equals("true")) {
					nochecks.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.reduction")) {
				if (load.getProperty("synthesis.reduction").equals("true")) {
					reduction.setSelected(true);
				}
			}
			if (load.containsKey("synthesis.minins")) {
				if (load.getProperty("synthesis.minins").equals("true")) {
					minins.setSelected(true);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		String[] tempArray = synthesisFile.split(separator);
		sourceFileNoPath = tempArray[tempArray.length - 1];
		backgroundField = new JTextField(sourceFileNoPath);
		
		/*TODO Test Assembly File compilation */
		if (sourceFile.endsWith(".s") || sourceFile.endsWith(".inst")) {
			biosim.copySFiles(sourceFile, directory);
			try {
				String preprocCmd;
				preprocCmd = System.getenv("BIOSIM") + "/bin/s2lpn " + sourceFile;
				File work = new File(directory);
				Runtime exec = Runtime.getRuntime();
				Process preproc = exec.exec(preprocCmd, null, work);
				log.addText("Executing:\n" + preprocCmd + "\n");
				preproc.waitFor();
				if (sourceFile.endsWith(".s")) {
					sourceFile.replace(".s", ".lpn");
				}
				else {
					sourceFile.replace(".inst", ".lpn");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame,
						"Error with preprocessing.", "Error",
						JOptionPane.ERROR_MESSAGE);
				//e.printStackTrace();
			}
		}

		getFilename = sourceFile.split(separator);
		getFilename = getFilename[getFilename.length - 1].split("\\.");
		File graphFile = new File(getFilename[0] + ".dot");
		File rulesFile = new File(getFilename[0] + ".prs");
		File traceFile = new File(getFilename[0] + ".trace");
		File logFile = new File(directory + "run.log");

		// Creates the run button
		run = new JButton("Save and Synthesize");
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
		buttonPanel.add(viewCircuit);
		if (!graphFile.exists()) {
			viewCircuit.setEnabled(false);
		}
		viewCircuit.setMnemonic(KeyEvent.VK_C);

		// Creates the view production rules button
		viewRules = new JButton("View Production Rules");
		viewRules.addActionListener(this);
		buttonPanel.add(viewRules);
		if (!rulesFile.exists()) {
			viewRules.setEnabled(false);
		}
		viewRules.setMnemonic(KeyEvent.VK_R);

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
		if (!logFile.exists()) {
			viewLog.setEnabled(false);
		}
		viewLog.setMnemonic(KeyEvent.VK_V);

		JPanel backgroundPanel = new JPanel();
		JLabel backgroundLabel = new JLabel("Model File:");
		backgroundField.setMaximumSize(new Dimension(200, 20));
		backgroundField.setEditable(false);
		JLabel componentLabel = new JLabel("Component:");
		componentField.setPreferredSize(new Dimension(200, 20));
		backgroundPanel.add(backgroundLabel);
		backgroundPanel.add(backgroundField);
		if (synthesisFile.endsWith(".vhd")) {
			backgroundPanel.add(componentLabel);
			backgroundPanel.add(componentField);
		}
		backgroundPanel.setMaximumSize(new Dimension(600, 30));
		basicOptions.add(backgroundPanel);
		basicOptions.add(timingRadioPanel);
		basicOptions.add(technologyPanel);
		basicOptions.add(otherPanel);
		basicOptions.add(valuePanel);
		basicOptions.add(algorithmPanel);
		if (synthesisFile.endsWith(".vhd")) {
			basicOptions.add(componentPanel);
		}
		basicOptions.setLayout(new BoxLayout(basicOptions, BoxLayout.Y_AXIS));

		advOptions.add(compilationPanel);
		advOptions.add(synthPanel);
		advOptions.add(pruningPanel);
		advOptions.add(advancedPanel);
		advOptions.add(bddPanel);
		advOptions.setLayout(new BoxLayout(advOptions, BoxLayout.Y_AXIS));

		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Basic Options", basicOptions);
		tab.addTab("Advanced Options", advOptions);
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
			save();
			new Thread(this).start();
		}
		else if (e.getSource() == save) {
			log.addText("Saving:\n" + directory + separator + synthFile + "\n");
			save();
		}
		else if (e.getSource() == viewCircuit) {
			viewCircuit();
		}
		else if (e.getSource() == viewRules) {
			viewRules();
		}
		else if (e.getSource() == viewTrace) {
			viewTrace();
		}
		else if (e.getSource() == viewLog) {
			viewLog();
		}
		else if (e.getSource() == addComponent) {
			String[] vhdlFiles = new File(root).list();
			ArrayList<String> tempFiles = new ArrayList<String>();
			for (int i = 0; i < vhdlFiles.length; i++) {
				if (vhdlFiles[i].endsWith(".vhd") && !vhdlFiles[i].equals(sourceFileNoPath)) {
					tempFiles.add(vhdlFiles[i]);
				}
			}
			vhdlFiles = new String[tempFiles.size()];
			for (int i = 0; i < vhdlFiles.length; i++) {
				vhdlFiles[i] = tempFiles.get(i);
			}
			String filename = (String) JOptionPane.showInputDialog(this, "", "Select Component",
					JOptionPane.PLAIN_MESSAGE, null, vhdlFiles, vhdlFiles[0]);
			if (filename != null) {
				String[] comps = componentList.getItems();
				boolean contains = false;
				for (int i=0; i<comps.length; i++) {
					if (comps[i].equals(filename)) {
						contains = true;
					}
				}
				if (!filename.endsWith(".vhd")) {
					JOptionPane.showMessageDialog(Gui.frame,
							"You must select a valid VHDL file.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				else if (new File(directory + separator + filename).exists()
						|| filename.equals(sourceFileNoPath) || contains) {
					JOptionPane.showMessageDialog(Gui.frame,
							"This component is already contained in this tool.", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				componentList.addItem(filename);
				return;
			}
		}
		else if (e.getSource() == removeComponent) {
			if (componentList.getSelectedValue() != null) {
				String selected = componentList.getSelectedValue().toString();
				componentList.removeItem(selected);
				new File(directory + separator + selected).delete();
			}
		}
	}

	@Override
	public void run() {
		String[] tempArray = synthesisFile.split(separator);
		String circuitFile = tempArray[tempArray.length - 1];
		tempArray = sourceFile.split("separator");
		tempArray = tempArray[tempArray.length - 1].split("\\.");
		String traceFilename = tempArray[0] + ".trace";
		File traceFile = new File(traceFilename);
		String rulesFilename, graphFilename;
		String pargFilename = tempArray[0] + ".grf";
		if (componentField.getText().trim().equals("")) {
			rulesFilename = tempArray[0] + ".prs";
			graphFilename = tempArray[0] + ".dot";
		}
		else {
			rulesFilename = directory + separator + componentField.getText().trim() + ".prs";
			graphFilename = directory + separator + componentField.getText().trim() + ".dot";
		}
		File rulesFile = new File(rulesFilename);
		File graphFile = new File(graphFilename);
		if (rulesFile.exists()) {
			rulesFile.delete();
		}
		if (graphFile.exists()) {
			graphFile.delete();
		}
		if (traceFile.exists()) {
			traceFile.delete();
		}
		for (String s : componentList.getItems()) {
			try {
				FileInputStream in = new FileInputStream(new File(root + separator + s));
				FileOutputStream out = new FileOutputStream(new File(directory + separator + s));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame, "Cannot update the file " + s + ".",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		String options = "";
		// Text field options
		options = options + "-M" + maxSize.getText() + " ";
		String[] temp = gateDelay.getText().split(" ");
		if (!temp[1].equals("inf")) {
			options = options + "-G" + temp[0] + "/" + temp[1] + " ";
		}
		if (!bddSize.getText().equals("") && !bddSize.getText().equals("0")) {
			options = options + "-L" + bddSize.getText() + " ";
		}
		// Timing method
		if (untimed.isSelected()) {
			options = options + "-tu";
		}
		else if (geometric.isSelected()) {
			options = options + "-tg";
		}
		else if (posets.isSelected()) {
			options = options + "-ts";
		}
		else if (bag.isSelected()) {
			options = options + "-tg";
		}
		else if (bap.isSelected()) {
			options = options + "-tp";
		}
		else {
			options = options + "-tt";
		}
		// Technology Options
		if (atomicGates.isSelected()) {
			options = options + "ot";
		}
		else if (generalizedC.isSelected()) {
			options = options + "og";
		}
		else if (standardC.isSelected()) {
			options = options + "os";
		}
		else if (bmGc.isSelected()) {
			options = options + "ob";
		}
		else {
			options = options + "ol";
		}
		// Other Options
		if (dot.isSelected()) {
			options = options + "od";
		}
		if (verbose.isSelected()) {
			options = options + "ov";
		}
		if (quiet.isSelected()) {
			options = options + "oq";
		}
		if (noinsert.isSelected()) {
			options = options + "oi";
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
		if (keepgoing.isSelected()) {
			options = options + "oK";
		}
		if (explpn.isSelected()) {
			options = options + "oL";
		}
		// Synthesis Options
		if (shareGate.isSelected()) {
			options = options + "oH";
		}
		if (combo.isSelected()) {
			options = options + "oC";
		}
		if (exact.isSelected()) {
			options = options + "oE";
		}
		if (manual.isSelected()) {
			options = options + "oM";
		}
		if (inponly.isSelected()) {
			options = options + "oN";
		}
		// Pruning Options
		if (notFirst.isSelected()) {
			options = options + "PN";
		}
		if (preserve.isSelected()) {
			options = options + "PP";
		}
		if (ordered.isSelected()) {
			options = options + "PO";
		}
		if (subset.isSelected()) {
			options = options + "PS";
		}
		if (unsafe.isSelected()) {
			options = options + "PU";
		}
		if (expensive.isSelected()) {
			options = options + "PE";
		}
		if (conflict.isSelected()) {
			options = options + "PC";
		}
		if (reachable.isSelected()) {
			options = options + "PR";
		}
		if (dumb.isSelected()) {
			options = options + "PD";
		}
		// Other Advanced Options
		if (nochecks.isSelected()) {
			options = options + "on";
		}
		if (reduction.isSelected()) {
			options = options + "oR";
		}
		if (minins.isSelected()) {
			options = options + "om";
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
		if (circuitFile.endsWith(".g")) {
			options = options + "lgsg";
		}
		// Synthesis Methods
		if (singleCube.isSelected()) {
			options = options + "ys";
		}
		else if (multicube.isSelected()) {
			options = options + "ym";
		}
		else if (bdd.isSelected()) {
			options = options + "yb";
		}
		else {
			options = options + "yd";
		}
		options = options + "pn";
		if (graph.isSelected()) {
			options = options + "ps";
		}
		String cmd = "atacs " + options;
		String[] components = componentList.getItems();
		for (String s : components) {
			cmd = cmd + " " + s;
		}
		cmd = cmd + " " + circuitFile;
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
		running.setContentPane(all);
		running.pack();
		Dimension screenSize;
		try {
			Toolkit tk = Toolkit.getDefaultToolkit();
			screenSize = tk.getScreenSize();
		}
		catch (AWTError awe) {
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
		File work = new File(directory);
		Runtime exec = Runtime.getRuntime();
		log.addText("Executing:\n" + cmd + "\n");
		try {
			final Process synth = exec.exec(cmd, null, work);
			cancel.setActionCommand("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					synth.destroy();
					running.setCursor(null);
					running.dispose();
				}
			});
			biosim.getExitButton().setActionCommand("Exit program");
			biosim.getExitButton().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					synth.destroy();
					running.setCursor(null);
					running.dispose();
				}
			});
			cancel.setActionCommand("Cancel");
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					synth.destroy();
					running.setCursor(null);
					running.dispose();
				}
			});
			biosim.getExitButton().setActionCommand("Exit program");
			biosim.getExitButton().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					synth.destroy();
					running.setCursor(null);
					running.dispose();
				}
			});
			String output = "";
			InputStream reb = synth.getInputStream();
			InputStreamReader isr = new InputStreamReader(reb);
			BufferedReader br = new BufferedReader(isr);
			FileWriter out = new FileWriter(new File(directory + separator + "run.log"));
			while ((output = br.readLine()) != null) {
				out.write(output);
				out.write("\n");
			}
			out.close();
			br.close();
			isr.close();
			reb.close();
			viewLog.setEnabled(true);
			int exitValue = synth.waitFor();
			running.setCursor(null);
			running.dispose();
			if (exitValue == 143) {
				JOptionPane
						.showMessageDialog(Gui.frame, "Synthesis was"
								+ " canceled by the user.", "Canceled Synthesis",
								JOptionPane.ERROR_MESSAGE);
			}
			else {
				if (rulesFile.exists()) {
					viewCircuit.setEnabled(true);
					viewRules.setEnabled(true);
					viewTrace.setEnabled(false);
					viewCircuit();
				}
				else {
					viewCircuit.setEnabled(false);
					viewRules.setEnabled(false);
					viewTrace.setEnabled(true);
					viewTrace();
				}
			}
			if (graph.isSelected()) {
				if (dot.isSelected()) {
					Preferences biosimrc = Preferences.userRoot();
					String command = biosimrc.get("biosim.general.graphviz", "") + " ";
					exec.exec(command + graphFilename);
					log.addText("Executing:\n" + command + graphFilename + "\n");
				}
				else {
					exec.exec("parg " + pargFilename);
					log.addText("Executing:\nparg " + pargFilename + "\n");
				}
			}
		}
		catch (Exception e) {
		}
	}

	public void saveAs() {
		String newName = JOptionPane.showInputDialog(Gui.frame, "Enter Synthesis name:",
				"Synthesis Name", JOptionPane.PLAIN_MESSAGE);
		if (newName == null) {
			return;
		}
		if (!newName.endsWith(".syn")) {
			newName = newName + ".syn";
		}
		save();
	}

	public void save() {
		try {
			Properties prop = new Properties();
			prop.setProperty("synthesis.file", synthesisFile);
			prop.setProperty("synthesis.source", sourceFile);
			prop.setProperty("synthesis.Max", this.maxSize.getText().trim());
			prop.setProperty("synthesis.Delay", this.gateDelay.getText().trim());
			if (!componentField.getText().trim().equals("")) {
				prop.setProperty("synthesis.Component", this.componentField.getText().trim());
			}
			String[] components = componentList.getItems();
			for (Integer i = 0; i < components.length; i++) {
				prop.setProperty("synthesis.compList" + i.toString(), components[i]);
			}
			if (!bddSize.equals("")) {
				prop.setProperty("synthesis.bddSize", this.bddSize.getText().trim());
			}
			if (untimed.isSelected()) {
				prop.setProperty("synthesis.timing.methods", "untimed");
			}
			else if (geometric.isSelected()) {
				prop.setProperty("synthesis.timing.methods", "geometric");
			}
			else if (posets.isSelected()) {
				prop.setProperty("synthesis.timing.methods", "posets");
			}
			else if (bag.isSelected()) {
				prop.setProperty("synthesis.timing.methods", "bag");
			}
			else if (bap.isSelected()) {
				prop.setProperty("synthesis.timing.methods", "bap");
			}
			else if (baptdc.isSelected()) {
				prop.setProperty("synthesis.timing.methods", "baptdc");
			}
			if (atomicGates.isSelected()) {
				prop.setProperty("synthesis.technology", "atomicGates");
			}
			else if (generalizedC.isSelected()) {
				prop.setProperty("synthesis.technology", "generalizedC");
			}
			else if (standardC.isSelected()) {
				prop.setProperty("synthesis.technology", "standardC");
			}
			else if (bmGc.isSelected()) {
				prop.setProperty("synthesis.technology", "bmGc");
			}
			else if (bm2.isSelected()) {
				prop.setProperty("synthesis.technology", "bm2");
			}
			if (dot.isSelected()) {
				prop.setProperty("synthesis.Dot", "true");
			}
			else {
				prop.setProperty("synthesis.Dot", "false");
			}
			if (verbose.isSelected()) {
				prop.setProperty("synthesis.Verb", "true");
			}
			else {
				prop.setProperty("synthesis.Verb", "false");
			}
			if (quiet.isSelected()) {
				prop.setProperty("synthesis.quiet", "true");
			}
			else {
				prop.setProperty("synthesis.quiet", "false");
			}
			if (noinsert.isSelected()) {
				prop.setProperty("synthesis.noinsert", "true");
			}
			else {
				prop.setProperty("synthesis.noinsert", "false");
			}
			if (graph.isSelected()) {
				prop.setProperty("synthesis.graph", "true");
			}
			else {
				prop.setProperty("synthesis.graph", "false");
			}
			if (singleCube.isSelected()) {
				prop.setProperty("synthesis.algorithm", "singleCube");
			}
			else if (multicube.isSelected()) {
				prop.setProperty("synthesis.algorithm", "multicube");
			}
			else if (bdd.isSelected()) {
				prop.setProperty("synthesis.algorithm", "bdd");
			}
			else {
				prop.setProperty("synthesis.algorithm", "direct");
			}
			if (newTab.isSelected()) {
				prop.setProperty("synthesis.compilation.newTab", "true");
			}
			else {
				prop.setProperty("synthesis.compilation.newTab", "false");
			}
			if (postProc.isSelected()) {
				prop.setProperty("synthesis.compilation.postProc", "true");
			}
			else {
				prop.setProperty("synthesis.compilation.postProc", "false");
			}
			if (redCheck.isSelected()) {
				prop.setProperty("synthesis.compilation.redCheck", "true");
			}
			else {
				prop.setProperty("synthesis.compilation.redCheck", "false");
			}
			if (xForm2.isSelected()) {
				prop.setProperty("synthesis.compilation.xForm2", "true");
			}
			else {
				prop.setProperty("synthesis.compilation.xForm2", "false");
			}
			if (expandRate.isSelected()) {
				prop.setProperty("synthesis.compilation.expandRate", "true");
			}
			else {
				prop.setProperty("synthesis.compilation.expandRate", "false");
			}

			if (genrg.isSelected()) {
				prop.setProperty("synthesis.timing.genrg", "true");
			}
			else {
				prop.setProperty("synthesis.timing.genrg", "false");
			}
			if (timsubset.isSelected()) {
				prop.setProperty("synthesis.timing.subset", "true");
			}
			else {
				prop.setProperty("synthesis.timing.subset", "false");
			}
			if (superset.isSelected()) {
				prop.setProperty("synthesis.timing.superset", "true");
			}
			else {
				prop.setProperty("synthesis.timing.superset", "false");
			}
			if (infopt.isSelected()) {
				prop.setProperty("synthesis.timing.infopt", "true");
			}
			else {
				prop.setProperty("synthesis.timing.infopt", "false");
			}
			if (orbmatch.isSelected()) {
				prop.setProperty("synthesis.timing.orbmatch", "true");
			}
			else {
				prop.setProperty("synthesis.timing.orbmatch", "false");
			}
			if (interleav.isSelected()) {
				prop.setProperty("synthesis.timing.interleav", "true");
			}
			else {
				prop.setProperty("synthesis.timing.interleav", "false");
			}
			if (prune.isSelected()) {
				prop.setProperty("synthesis.timing.prune", "true");
			}
			else {
				prop.setProperty("synthesis.timing.prune", "false");
			}
			if (disabling.isSelected()) {
				prop.setProperty("synthesis.timing.disabling", "true");
			}
			else {
				prop.setProperty("synthesis.timing.disabling", "false");
			}
			if (nofail.isSelected()) {
				prop.setProperty("synthesis.timing.nofail", "true");
			}
			else {
				prop.setProperty("synthesis.timing.nofail", "false");
			}
			if (keepgoing.isSelected()) {
				prop.setProperty("synthesis.timing.keepgoing", "true");
			}
			else {
				prop.setProperty("synthesis.timing.keepgoing", "false");
			}
			if (explpn.isSelected()) {
				prop.setProperty("synthesis.timing.explpn", "true");
			}
			else {
				prop.setProperty("synthesis.timing.explpn", "false");
			}
			if (shareGate.isSelected()) {
				prop.setProperty("synthesis.options.shareGate", "true");
			}
			else {
				prop.setProperty("synthesis.options.shareGate", "false");
			}
			if (combo.isSelected()) {
				prop.setProperty("synthesis.options.combo", "true");
			}
			else {
				prop.setProperty("synthesis.options.combo", "false");
			}
			if (exact.isSelected()) {
				prop.setProperty("synthesis.options.exact", "true");
			}
			else {
				prop.setProperty("synthesis.options.exact", "false");
			}
			if (manual.isSelected()) {
				prop.setProperty("synthesis.options.manual", "true");
			}
			else {
				prop.setProperty("synthesis.options.manual", "false");
			}
			if (inponly.isSelected()) {
				prop.setProperty("synthesis.options.inponly", "true");
			}
			else {
				prop.setProperty("synthesis.options.inponly", "false");
			}
			if (notFirst.isSelected()) {
				prop.setProperty("synthesis.pruning.notFirst", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.notFirst", "false");
			}
			if (preserve.isSelected()) {
				prop.setProperty("synthesis.pruning.preserve", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.preserve", "false");
			}
			if (ordered.isSelected()) {
				prop.setProperty("synthesis.pruning.ordered", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.ordered", "false");
			}
			if (subset.isSelected()) {
				prop.setProperty("synthesis.pruning.subset", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.subset", "false");
			}
			if (expensive.isSelected()) {
				prop.setProperty("synthesis.pruning.expensive", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.expensive", "false");
			}
			if (conflict.isSelected()) {
				prop.setProperty("synthesis.pruning.conflict", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.conflict", "false");
			}
			if (reachable.isSelected()) {
				prop.setProperty("synthesis.pruning.reachable", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.reachable", "false");
			}
			if (dumb.isSelected()) {
				prop.setProperty("synthesis.pruning.dumb", "true");
			}
			else {
				prop.setProperty("synthesis.pruning.dumb", "false");
			}
			if (nochecks.isSelected()) {
				prop.setProperty("synthesis.nochecks", "true");
			}
			else {
				prop.setProperty("synthesis.nochecks", "false");
			}
			if (reduction.isSelected()) {
				prop.setProperty("synthesis.reduction", "true");
			}
			else {
				prop.setProperty("synthesis.reduction", "false");
			}
			if (minins.isSelected()) {
				prop.setProperty("synthesis.minins", "true");
			}
			else {
				prop.setProperty("synthesis.minins", "false");
			}
			FileOutputStream out = new FileOutputStream(new File(directory + separator + synthFile));
			prop.store(out, synthesisFile);
			out.close();
			log.addText("Saving Parameter File:\n" + directory + separator + synthFile + "\n");
			change = false;
			oldMax = maxSize.getText();
			oldDelay = gateDelay.getText();
			oldBdd = bddSize.getText();
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to save parameter file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		for (String s : componentList.getItems()) {
			try {
				new File(directory + separator + s).createNewFile();
				FileInputStream in = new FileInputStream(new File(root + separator + s));
				FileOutputStream out = new FileOutputStream(new File(directory + separator + s));
				int read = in.read();
				while (read != -1) {
					out.write(read);
					read = in.read();
				}
				in.close();
				out.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame, "Cannot add the selected component.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void reload(String newname) {
		backgroundField.setText(newname);
	}

	public boolean getViewCircuitEnabled() {
		return viewCircuit.isEnabled();
	}

	public boolean getViewRulesEnabled() {
		return viewRules.isEnabled();
	}

	public boolean getViewLogEnabled() {
		return viewLog.isEnabled();
	}

	public boolean getViewTraceEnabled() {
		return viewTrace.isEnabled();
	}

	public void viewCircuit() {
		String[] getFilename = sourceFile.split(separator);
		String circuitFile = getFilename[getFilename.length - 1];
		String graphFile;
		if (componentField.getText().trim().equals("")) {
			getFilename = getFilename[getFilename.length - 1].split("\\.");
			graphFile = getFilename[0] + ".dot";
		}
		else {
			getFilename = componentField.getText().trim().split("\\.");
			graphFile = getFilename[0] + ".dot";
		}
		try {
			File work = new File(directory);
			Runtime exec = Runtime.getRuntime();
			File circuit = new File(directory + separator + graphFile);
			if (!circuit.exists()) {
				String cmd = "";
				if (circuitFile.endsWith(".g")) {
					cmd = "atacs -lgslllodpl " + circuitFile;
				}
				else if (circuitFile.endsWith(".lpn")) {
					cmd = "atacs -llodpl " + circuitFile;
				}
				else if (circuitFile.endsWith(".vhd")) {
					cmd = "atacs -lvslllodpl " + circuitFile;
				}
				else if (circuitFile.endsWith(".csp")) {
					cmd = "atacs -lcslllodpl " + circuitFile;
				}
				else if (circuitFile.endsWith(".hse")) {
					cmd = "atacs -lhslllodpl " + circuitFile;
				}
				else if (circuitFile.endsWith(".unc")) {
					cmd = "atacs -lxodps " + circuitFile;
				}
				else if (circuitFile.endsWith(".rsg")) {
					cmd = "atacs -lsodps " + circuitFile;
				}
				if (new File(directory + separator + graphFile).exists()) {
					exec.exec(cmd, null, work);
					log.addText("Executing:\n" + cmd);
				}
				else {
					File log = new File(directory + separator + "atacs.log");
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
					JOptionPane.showMessageDialog(Gui.frame, scrolls, "Log",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
			Preferences biosimrc = Preferences.userRoot();
			String command = biosimrc.get("biosim.general.graphviz", "");
			exec.exec(command + " " + graphFile, null, work);
			log.addText(command + " " + directory + separator + graphFile + "\n");
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to view circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewRules() {
		String[] getFilename = sourceFile.split("\\.");
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
				JOptionPane.showMessageDialog(Gui.frame, scrolls, "Circuit View",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(Gui.frame, "No production rules exist.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to view production rules.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewTrace() {
		String[] getFilename = sourceFile.split("\\.");
		String traceFilename = getFilename[0] + ".trace";
		try {
			if (new File(traceFilename).exists()) {
				File log = new File(traceFilename);
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
				JOptionPane.showMessageDialog(Gui.frame, scrolls, "Trace View",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(Gui.frame, "No trace file exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to view trace.", "Error",
					JOptionPane.ERROR_MESSAGE);
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
			}
			else {
				JOptionPane.showMessageDialog(Gui.frame, "No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean hasChanged() {
		if (!oldMax.equals(maxSize.getText())) {
			return true;
		}
		if (!oldDelay.equals(gateDelay.getText())) {
			return true;
		}
		if (!oldBdd.equals(bddSize.getText())) {
			return true;
		}
		return change;
	}
}