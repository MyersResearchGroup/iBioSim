package synthesis;

import javax.swing.*;

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

import biomodelsim.*;

/**
 * This class creates a GUI front end for the Synthesis tool. It provides the
 * necessary options to run an atacs simulation of the circuit and manage the
 * results from the BioSim GUI.
 * 
 * @author Kevin Jones
 */

public class Synthesis extends JPanel implements ActionListener {

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton save, run, viewCircuit, viewLog;

	private JLabel algorithm, timingMethod, timingOptions, technology, otherOptions, maxSizeLabel,
			gateDelayLabel, compilation, bddSizeLabel, synthesisOptions, pruning, advTiming;

	private JRadioButton untimed, geometric, posets, bag, bap, baptdc, atomicGates, generalizedC,
			standardC, bmGc, bm2, singleCube, multicube, bdd, direct, newTab, postProc, redCheck,
			xForm2, expandRate;

	private JCheckBox abst, partialOrder, dot, verbose, quiet, noinsert, shareGate, combo, exact,
			manual, inponly, notFirst, preserve, ordered, subset, unsafe, expensive, conflict,
			reachable, dumb, genrg, timsubset, superset, infopt, orbmatch, interleav, prune,
			disabling, nofail, keepgoing, explpn, nochecks, reduction, minins;

	private JTextField maxSize, gateDelay, bddSize;

	private ButtonGroup timingMethodGroup, technologyGroup, algorithmGroup, compilationGroup;

	private String directory, separator, synthFile, synthesisFile, sourceFile;

	private BioSim biosim;

	/**
	 * This is the constructor for the Synthesis class. It initializes all the
	 * input fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public Synthesis(String directory, String filename, BioSim biosim) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}

		this.biosim = biosim;
		this.directory = directory;
		this.sourceFile = filename;
		String[] getFilename = directory.split(separator);
		synthFile = getFilename[getFilename.length - 1] + ".syn";

		JPanel timingRadioPanel = new JPanel();
		JPanel timingCheckBoxPanel = new JPanel();
		JPanel technologyPanel = new JPanel();
		JPanel otherPanel = new JPanel();
		JPanel algorithmPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		JPanel compilationPanel = new JPanel();
		JPanel advancedPanel = new JPanel();
		JPanel displayPanel = new JPanel();
		JPanel printPanel = new JPanel();
		JPanel synthPanel = new JPanel();
		JPanel pruningPanel = new JPanel();
		JPanel advTimingPanel = new JPanel();

		maxSize = new JTextField("4");
		gateDelay = new JTextField("0 inf");
		bddSize = new JTextField("");
		maxSize.setPreferredSize(new Dimension(30, 18));
		gateDelay.setPreferredSize(new Dimension(70, 18));
		bddSize.setPreferredSize(new Dimension(40, 18));

		algorithm = new JLabel("Analysis Algorithm:");
		timingMethod = new JLabel("Timing Method:");
		timingOptions = new JLabel("Timing Options:");
		technology = new JLabel("Technology:");
		otherOptions = new JLabel("Other Options:");
		maxSizeLabel = new JLabel("Max Size:");
		gateDelayLabel = new JLabel("Gate Delay:");
		compilation = new JLabel("Compilation Options:");
		bddSizeLabel = new JLabel("BDD Linkspace Size:");
		synthesisOptions = new JLabel("Synthesis Options:");
		pruning = new JLabel("Pruning Options:");
		advTiming = new JLabel("Timing Options:");

		// Initializes the radio buttons and check boxes
		// Timing Methods
		untimed = new JRadioButton("Untimed");
		geometric = new JRadioButton("Geometric");
		posets = new JRadioButton("POSETs");
		bag = new JRadioButton("BAG");
		bap = new JRadioButton("BAP");
		baptdc = new JRadioButton("BAPTDC");
		// Basic Timing Options
		abst = new JCheckBox("Abstract");
		partialOrder = new JCheckBox("Partial Order");
		// Technology Options
		atomicGates = new JRadioButton("Atomic Gates");
		generalizedC = new JRadioButton("Generalized-C");
		standardC = new JRadioButton("Standard-C");
		bmGc = new JRadioButton("BM gC");
		bm2 = new JRadioButton("BM 2-level");
		// Other Basic Options
		dot = new JCheckBox("Dot");
		verbose = new JCheckBox("Verbose");
		quiet = new JCheckBox("Quiet");
		noinsert = new JCheckBox("NoInsert");
		// Synthesis Algorithms
		singleCube = new JRadioButton("Single Cube");
		multicube = new JRadioButton("Multicube");
		bdd = new JRadioButton("BDD");
		direct = new JRadioButton("Direct");
		// Compilations Options
		newTab = new JRadioButton("New Tab");
		postProc = new JRadioButton("Post Processing");
		redCheck = new JRadioButton("Red Check");
		xForm2 = new JRadioButton("xForm2");
		expandRate = new JRadioButton("Expand Rate");
		// Advanced Synthesis Options
		shareGate = new JCheckBox("Share Gate");
		combo = new JCheckBox("Combo");
		exact = new JCheckBox("Exact");
		manual = new JCheckBox("Manual");
		inponly = new JCheckBox("Inponly");
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
		// Advanced Timing Options
		genrg = new JCheckBox("Generate RG");
		timsubset = new JCheckBox("Subsets");
		superset = new JCheckBox("Supersets");
		infopt = new JCheckBox("Inf opt");
		orbmatch = new JCheckBox("Orb match");
		interleav = new JCheckBox("Interleav");
		prune = new JCheckBox("Prune");
		disabling = new JCheckBox("Disabling");
		nofail = new JCheckBox("No fail");
		keepgoing = new JCheckBox("Keep going");
		explpn = new JCheckBox("Exp LPN");
		// Other Advanced Options
		nochecks = new JCheckBox("No checks");
		reduction = new JCheckBox("Reduction");
		minins = new JCheckBox("Min ins");

		timingMethodGroup = new ButtonGroup();
		technologyGroup = new ButtonGroup();
		algorithmGroup = new ButtonGroup();
		compilationGroup = new ButtonGroup();

		untimed.setSelected(true);
		atomicGates.setSelected(true);
		singleCube.setSelected(true);
		newTab.setSelected(true);

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
		compilationGroup.add(newTab);
		compilationGroup.add(postProc);
		compilationGroup.add(redCheck);
		compilationGroup.add(xForm2);
		compilationGroup.add(expandRate);

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

		timingCheckBoxPanel.add(timingOptions);
		timingCheckBoxPanel.add(abst);
		timingCheckBoxPanel.add(partialOrder);

		technologyPanel.add(technology);
		technologyPanel.add(atomicGates);
		technologyPanel.add(generalizedC);
		technologyPanel.add(standardC);
		technologyPanel.add(bmGc);
		technologyPanel.add(bm2);

		otherPanel.add(otherOptions);
		otherPanel.add(dot);
		otherPanel.add(verbose);
		otherPanel.add(quiet);
		otherPanel.add(noinsert);
		otherPanel.add(maxSizeLabel);
		otherPanel.add(maxSize);
		otherPanel.add(gateDelayLabel);
		otherPanel.add(gateDelay);

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

		advancedPanel.add(otherOptions);
		advancedPanel.add(nochecks);
		advancedPanel.add(reduction);
		advancedPanel.add(minins);
		advancedPanel.add(bddSizeLabel);
		advancedPanel.add(bddSize);

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
						+ separator + getProp[getProp.length - 1];
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
			if (load.containsKey("synthesis.compilation")) {
				if (load.getProperty("synthesis.compilation").equals("newTab")) {
					newTab.setSelected(true);
				}
				else if (load.getProperty("synthesis.compilation").equals("postProc")) {
					postProc.setSelected(true);
				}
				else if (load.getProperty("synthesis.compilation").equals("redCheck")) {
					redCheck.setSelected(true);
				}
				else if (load.getProperty("synthesis.compilation").equals("xForm2")) {
					xForm2.setSelected(true);
				}
				else {
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
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to load properties file!",
					"Error Loading Properties", JOptionPane.ERROR_MESSAGE);
		}
		save();

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
		viewCircuit.setMnemonic(KeyEvent.VK_C);

		// Creates the view log button
		viewLog = new JButton("View Log");
		viewLog.addActionListener(this);
		buttonPanel.add(viewLog);
		viewLog.setMnemonic(KeyEvent.VK_V);

		basicOptions.add(timingRadioPanel);
		basicOptions.add(timingCheckBoxPanel);
		basicOptions.add(technologyPanel);
		basicOptions.add(otherPanel);
		basicOptions.add(algorithmPanel);
		basicOptions.setLayout(new BoxLayout(basicOptions, BoxLayout.Y_AXIS));

		advOptions.add(displayPanel);
		advOptions.add(printPanel);
		advOptions.add(compilationPanel);
		advOptions.add(advTimingPanel);
		advOptions.add(synthPanel);
		advOptions.add(pruningPanel);
		advOptions.add(advancedPanel);
		advOptions.setLayout(new BoxLayout(advOptions, BoxLayout.Y_AXIS));

		JTabbedPane tab = new JTabbedPane();
		tab.addTab("Basic Options", basicOptions);
		tab.addTab("Advanced Options", advOptions);
		tab.setPreferredSize(new Dimension(1000, 480));

		this.setLayout(new BorderLayout());
		this.add(tab, BorderLayout.PAGE_START);
		this.add(buttonPanel, BorderLayout.PAGE_END);
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 * 
	 * @throws
	 * @throws
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == run) {
			save();
			try {
				run();
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, "File cannot be read", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (e.getSource() == save) {
			save();
		}
		else if (e.getSource() == viewCircuit) {
			viewCircuit();
		}
		else if (e.getSource() == viewLog) {
			viewLog();
		}
	}

	public void run() throws IOException {
		// String command = "/home/shang/kjones/atacs/bin/atacs -";
		String options = "";
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
		// Text field options
		options = options + "M" + maxSize.getText();
		String[] temp = gateDelay.getText().split(" ");
		if (!temp[1].equals("inf")) {
			options = options + "G" + temp[0] + "/" + temp[1];
		}
		// Timing Options
		if (abst.isSelected()) {
			options = options + "oa";
		}
		if (partialOrder.isSelected()) {
			options = options + "op";
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
			options = options + "yd ";
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
			options = options + "cN ";
		}
		else if (postProc.isSelected()) {
			options = options + "cP ";
		}
		else if (redCheck.isSelected()) {
			options = options + "cR ";
		}
		else if (xForm2.isSelected()) {
			options = options + "cT ";
		}
		else {
			options = options + "cE ";
		}
		// String[] temp = sourceFile.split(separator);
		// String src = temp[temp.length - 1];
		String cmd = "atacs " + options + " " + sourceFile;
		// String[] cmd = {"emacs", "temp" };
		//JOptionPane.showMessageDialog(this, cmd);
		// Runtime exec = Runtime.getRuntime();
		File work = new File(directory);
		Runtime exec = Runtime.getRuntime();
		Process synth = exec.exec(cmd, null, work);
		try {
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
			synth.waitFor();
		}
		catch (Exception e) {
		}
	}

	public void save() {
		try {
			Properties prop = new Properties();
			FileInputStream in = new FileInputStream(new File(directory + separator + synthFile));
			prop.load(in);
			in.close();
			prop.setProperty("synthesis.file", synthesisFile);
			prop.setProperty("synthesis.source", sourceFile);
			prop.setProperty("synthesis.Max", this.maxSize.getText().trim());
			prop.setProperty("synthesis.Delay", this.gateDelay.getText().trim());
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
			if (abst.isSelected()) {
				prop.setProperty("synthesis.Abst", "true");
			}
			else {
				prop.setProperty("synthesis.Abst", "false");
			}
			if (partialOrder.isSelected()) {
				prop.setProperty("synthesis.partial.order", "true");
			}
			else {
				prop.setProperty("synthesis.partial.order", "false");
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
				prop.setProperty("synthesis.compilation", "newTab");
			}
			else if (postProc.isSelected()) {
				prop.setProperty("synthesis.compilation", "postProc");
			}
			else if (redCheck.isSelected()) {
				prop.setProperty("synthesis.compilation", "redCheck");
			}
			else if (xForm2.isSelected()) {
				prop.setProperty("synthesis.compilation", "xForm2");
			}
			else {
				prop.setProperty("synthesis.compilation", "expandRate");
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
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to save parameter file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewCircuit() {
		String[] getFilename = sourceFile.split("\\.");
		String circuitFile = getFilename[0] + ".prs";
		// JOptionPane.showMessageDialog(this, circuitFile);
		// JOptionPane.showMessageDialog(this, directory + separator +
		// circuitFile);
		try {
			// JOptionPane.showMessageDialog(this, directory + separator +
			// "run.log");
			// String[] getFilename = sourceFile.split(".");
			// String circuitFile = getFilename[0] + ".ps";
			// JOptionPane.showMessageDialog(this, directory + separator +
			// circuitFile);
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
				JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Circuit View",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(), "No circuit view exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to view circuit.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void viewLog() {
		try {
			// JOptionPane.showMessageDialog(this, directory + separator +
			// "run.log");
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
				JOptionPane.showMessageDialog(biosim.frame(), scrolls, "Run Log",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(biosim.frame(), "No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(biosim.frame(), "Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}