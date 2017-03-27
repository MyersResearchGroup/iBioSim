/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.verification;

import javax.swing.*;

import edu.utah.ece.async.biomodel.gui.util.AbstractRunnableNamedButton;
import edu.utah.ece.async.biomodel.gui.util.PropertyList;
import edu.utah.ece.async.biomodel.gui.util.Runnable;
import edu.utah.ece.async.biomodel.util.Utility;
import edu.utah.ece.async.util.GlobalConstants;

import java.awt.*;
import java.awt.event.*;

/**
 * This class creates a GUI front end for the Verification tool. It provides the
 * necessary options to run an atacs simulation of the circuit and manage the
 * results from the BioSim GUI.
 * 
 * @author Kevin Jones
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ParamEditor extends JPanel implements ActionListener {

	private static final long serialVersionUID = -5806315070287184299L;

	//private JButton save, run, viewCircuit, viewTrace, viewLog, addComponent, removeComponent;

	private JLabel algorithm, timingMethod, timingOptions, otherOptions, otherOptions2,
			compilation, bddSizeLabel, advTiming, abstractLabel;

	private JRadioButton untimed, geometric, posets, bag, bap, baptdc, verify, vergate, orbits,
			search, trace, bdd, dbm, smt, lhpn, view, none, simplify, abstractLhpn;

	private JCheckBox abst, partialOrder, dot, verbose, graph, genrg, timsubset, superset, infopt,
			orbmatch, interleav, prune, disabling, nofail, keepgoing, explpn, nochecks, reduction,
			newTab, postProc, redCheck, xForm2, expandRate;

	private JTextField bddSize;

	//private JTextField backgroundField, componentField;

	private ButtonGroup timingMethodGroup, algorithmGroup, abstractionGroup;
	
	private PropertyList variables;

	private String separator, root;

	//private String directory, verFile, oldBdd, sourceFileNoPath;
	
	public String verifyFile;

	//private boolean change, atacs;
	
	//private JTabbedPane bigTab;

	//private PropertyList componentList;
	
	//private AbstPane abstPane;

	//private Log log;

	//private BioSim biosim;
	/**
	 * This is the constructor for the Verification class. It initializes all
	 * the input fields, puts them on panels, adds the panels to the frame, and
	 * then displays the frame.
	 */
	public ParamEditor(String directory, boolean lema, boolean atacs) {
		separator = GlobalConstants.separator;
		//this.atacs = atacs;
		//this.biosim = biosim;
		//this.log = log;
		//this.directory = directory;
		//this.bigTab = bigTab;
		// String[] getFilename = directory.split(separator);
		//String[] tempArray = filename.split("\\.");
		//String traceFilename = tempArray[0] + ".trace";
		//File traceFile = new File(traceFilename);
		String[] tempDir = directory.split(separator);
		root = tempDir[0];
		for (int i = 1; i < tempDir.length - 1; i++) {
			root = root + separator + tempDir[i];
		}

		JPanel abstractionPanel = new JPanel();
		abstractionPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel timingRadioPanel = new JPanel();
		timingRadioPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel timingCheckBoxPanel = new JPanel();
		timingCheckBoxPanel.setMaximumSize(new Dimension(1000, 30));
		JPanel otherPanel = new JPanel();
		otherPanel.setMaximumSize(new Dimension(1000, 35));
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setMaximumSize(new Dimension(1000, 35));
		//JPanel buttonPanel = new JPanel();
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
		//oldBdd = bddSize.getText();
		//componentField = new JTextField("");
		//componentList = new PropertyList("");

		abstractLabel = new JLabel("Abstraction:");
		algorithm = new JLabel("Verification Algorithm:");
		timingMethod = new JLabel("Timing Method:");
		timingOptions = new JLabel("Timing Options:");
		otherOptions = new JLabel("Other Options:");
		otherOptions2 = new JLabel("Other Options:");
		compilation = new JLabel("Compilation Options:");
		bddSizeLabel = new JLabel("BDD Linkspace Size:");
		advTiming = new JLabel("Timing Options:");

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
		}
		else {
			bdd = new JRadioButton("BDD");
			dbm = new JRadioButton("DBM");
			smt = new JRadioButton("SMT");
		}
		lhpn = new JRadioButton("LPN");
		view = new JRadioButton("View");
		// Basic Timing Options
		abst = new JCheckBox("Abstract");
		partialOrder = new JCheckBox("Partial Order");
		// Other Basic Options
		dot = new JCheckBox("Dot");
		verbose = new JCheckBox("Verbose");
		graph = new JCheckBox("Show State Graph");
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
		// Other Advanced Options
		nochecks = new JCheckBox("No checks");
		reduction = new JCheckBox("Reduction");
		nochecks.addActionListener(this);
		reduction.addActionListener(this);
		// Component List
		//addComponent = new JButton("Add Component");
		//removeComponent = new JButton("Remove Component");
		// addComponent.addActionListener(this);
		// removeComponent.addActionListener(this);
		GridBagConstraints constraints = new GridBagConstraints();
		variables = new PropertyList("Variable List");
		EditButton editVar = new EditButton("Edit Variable");
		JPanel varPanel = Utility.createPanel(this, "Variables", variables, null, null, editVar);
		constraints.gridx = 0;
		constraints.gridy = 1;

		abstractionGroup = new ButtonGroup();
		timingMethodGroup = new ButtonGroup();
		algorithmGroup = new ButtonGroup();

		abstractLhpn.setSelected(true);
		if (lema) {
			dbm.setSelected(true);
		}
		else {
			untimed.setSelected(true);
		}
		verify.setSelected(true);

		// Groups the radio buttons
		abstractionGroup.add(none);
		abstractionGroup.add(simplify);
		abstractionGroup.add(abstractLhpn);
		if (lema) {
			timingMethodGroup.add(bdd);
			timingMethodGroup.add(dbm);
			timingMethodGroup.add(smt);
		}
		else {
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

		// Adds the buttons to their panels
		abstractionPanel.add(abstractLabel);
		abstractionPanel.add(none);
		abstractionPanel.add(simplify);
		abstractionPanel.add(abstractLhpn);
		timingRadioPanel.add(timingMethod);
		if (lema) {
			timingRadioPanel.add(bdd);
			timingRadioPanel.add(dbm);
			timingRadioPanel.add(smt);
		}
		else {
			timingRadioPanel.add(untimed);
			timingRadioPanel.add(geometric);
			timingRadioPanel.add(posets);
			timingRadioPanel.add(bag);
			timingRadioPanel.add(bap);
			timingRadioPanel.add(baptdc);
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
		advTimingPanel.add(keepgoing);
		advTimingPanel.add(explpn);

		advancedPanel.add(otherOptions2);
		advancedPanel.add(nochecks);
		advancedPanel.add(reduction);

		bddPanel.add(bddSizeLabel);
		bddPanel.add(bddSize);

		basicOptions.add(varPanel);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	public class EditButton extends AbstractRunnableNamedButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public EditButton(String name) {
			super(name);
		}

		@Override
		public void run() {
			new EditCommand().run();
		}

	}
	
	private class EditCommand implements Runnable {
		public EditCommand() {
	//		this.name = name;
			//this.list = list;
		}

		@Override
		public void run() {
			
		}

		/*
		public String getName() {
			return name;
		}

		private String name = null;
*/
		//private PropertyList list = null;
	}
	
}