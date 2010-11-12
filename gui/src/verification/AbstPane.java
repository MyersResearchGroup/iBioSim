package verification;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader; //import java.io.FileWriter;
import java.io.IOException; //import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
import java.util.Properties;

import lhpn2sbml.parser.LhpnFile;

import gcm2sbml.gui.PropertyList; //import gcm2sbml.util.Utility;

import biomodelsim.*;
import buttons.Buttons;

/**
 * This class creates a GUI front end for the Verification tool. It provides the
 * necessary options to run an atacs simulation of the circuit and manage the
 * results from the BioSim GUI.
 * 
 * @author Kevin Jones
 */

public class AbstPane extends JPanel implements ActionListener, Runnable {

	private static final long serialVersionUID = -5806315070287184299L;

	private JButton addIntSpecies, removeIntSpecies, clearIntSpecies, addXform, removeXform,
			addAllXforms, clearXforms, addPreAbs, addLoopAbs, addPostAbs, editPreAbs, editLoopAbs, editPostAbs, rmPreAbs, rmLoopAbs, rmPostAbs, clearPreAbs, clearLoopAbs, clearPostAbs;

	public JList species, intSpecies, xforms, selectXforms, preAbs, loopAbs, postAbs;

	public DefaultListModel listModel, absListModel, preAbsModel, loopAbsModel, postAbsModel;

	private JTextField field;

	private String directory, separator, root, absFile, oldBdd;
	
	private JLabel preAbsLabel, loopAbsLabel, postAbsLabel;

	public String xform0 = "Merge Parallel Places - simplification",
			xform1 = "Remove Place in Self-Loop - simplification",
			xform3 = "Remove Transitions with Single Place in Postset - simplification",
			xform4 = "Remove Transitions with Single Place in Preset - simplification",
			xform5 = "Merge Transitions with Same Preset and Postset - simplification",
			xform6 = "Merge Transitions with Same Preset - simplification",
			xform7 = "Merge Transitions with Same Postset - simplification",
			xform8 = "Local Assignment Propagation - simplification",
			xform9 = "Remove Write Before Write - simplification",
			xform10 = "Simplify Expressions - simplification",
			xform11 = "Constant False Enabling Conditions - simplification",
			xform12 = "Abstract Assignments to the Same Variable - abstraction",
			xform13 = "Remove Unread Variables - abstraction",
			xform14 = "Remove Dead Places - simplification",
			xform15 = "Remove Dead Transitions - simplification",
			xform16 = "Constant True Enabling Conditions - simplification",
			xform17 = "Eliminate Dominated Transitions - simplification",
			xform18 = "Remove Unread Variables - simplification",
			xform19 = "Correlated Variables - simplification",
			xform20 = "Remove Arc after Failure Transitions - simplification",
			xform21 = "Timing Bound Normalization - abstraction",
			xform22 = "Remove Vacuous Transitions - simplification",
			xform23 = "Remove Vacuous Transitions - abstraction",
			xform24 = "Remove Pairwise Write Before Write - simplification",
			xform25 = "Propagate Constant Variable Values - simplifiction",
			xform26 = "Remove Dangling Transitions - simplification",
			xform27 = "Combine Parallel Transitions - simplification",
			xform28 = "Combine Parallel Transitions - abstraction",
			xform29 = "Remove Uninteresting Variables - simplification";

	public String[] transforms = { xform0, xform1, xform3, xform4, xform5, xform6, xform7, xform8,
			xform25, xform9, xform24, xform10, xform12, xform13, xform14, xform16, xform11,
			xform15, xform17, xform18, xform19, xform20, xform21, xform22, xform23, xform26, xform27, xform28, xform29 };

	public JTextField factorField, iterField;

	private boolean change;

	private PropertyList componentList;

	private Log log;

	private Verification verification;

	/**
	 * This is the constructor for the Verification class. It initializes all
	 * the input fields, puts them on panels, adds the panels to the frame, and
	 * then displays the frame.
	 */
	public AbstPane(String directory, Verification verification, Log log,
			boolean lema, boolean atacs) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.directory = directory;
		this.log = log;
		this.verification = verification;
		this.setLayout(new BorderLayout());
		absFile = verification.getVerName() + ".abs";
		verification.copyFile();
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(directory + separator + verification.verifyFile);

		// Creates the interesting species JList
		listModel = new DefaultListModel();
		intSpecies = new JList(lhpn.getVariables());
		species = new JList(listModel);
		JLabel spLabel = new JLabel("Available Variables:");
		JLabel speciesLabel = new JLabel("Interesting Variables:");
		JPanel speciesHolder = new JPanel(new BorderLayout());
		JPanel listOfSpeciesLabelHolder = new JPanel(new GridLayout(1, 2));
		JPanel listOfSpeciesHolder = new JPanel(new GridLayout(1, 2));
		JScrollPane scroll = new JScrollPane();
		JScrollPane scroll1 = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(species);
		scroll1.setMinimumSize(new Dimension(260, 200));
		scroll1.setPreferredSize(new Dimension(276, 132));
		scroll1.setViewportView(intSpecies);
		addIntSpecies = new JButton("Add Variable");
		removeIntSpecies = new JButton("Remove Variable");
		clearIntSpecies = new JButton("Clear Variable");
		addIntSpecies.addActionListener(this);
		removeIntSpecies.addActionListener(this);
		clearIntSpecies.addActionListener(this);
		listOfSpeciesLabelHolder.add(spLabel);
		listOfSpeciesHolder.add(scroll1);
		listOfSpeciesLabelHolder.add(speciesLabel);
		listOfSpeciesHolder.add(scroll);
		speciesHolder.add(listOfSpeciesLabelHolder, "North");
		speciesHolder.add(listOfSpeciesHolder, "Center");
		JPanel buttonHolder = new JPanel();
		buttonHolder.add(addIntSpecies);
		buttonHolder.add(removeIntSpecies);
		buttonHolder.add(clearIntSpecies);
		speciesHolder.add(buttonHolder, "South");
		this.add(speciesHolder, "North");

		JPanel factorPanel = new JPanel();
		JLabel factorLabel = new JLabel("Normalization Factor");
		factorField = new JTextField("5");
		factorField.setPreferredSize(new Dimension(40, 18));
		factorPanel.add(factorLabel);
		factorPanel.add(factorField);
		JLabel iterLabel = new JLabel("Maximum Number of Iterations");
		iterField = new JTextField("100");
		iterField.setPreferredSize(new Dimension(40, 18));
		factorPanel.add(iterLabel);
		factorPanel.add(iterField);
		this.add(factorPanel);

		// Creates the abstractions JList
		/*absListModel = new DefaultListModel();
		selectXforms = new JList(transforms);
		xforms = new JList(absListModel);
		for (String s : transforms) {
			absListModel.addElement(s);
		}
		JLabel absLabel = new JLabel("Available Transforms:");
		JLabel abstractLabel = new JLabel("Selected Transforms:");
		JPanel absHolder = new JPanel(new BorderLayout());
		JPanel listOfTransformsLabelHolder = new JPanel(new GridLayout(1, 2));
		JPanel listOfTransformsHolder = new JPanel(new GridLayout(1, 2));
		JScrollPane absScroll = new JScrollPane();
		JScrollPane absScroll1 = new JScrollPane();
		absScroll.setMinimumSize(new Dimension(260, 200));
		absScroll.setPreferredSize(new Dimension(276, 132));
		absScroll.setViewportView(xforms);
		absScroll1.setMinimumSize(new Dimension(260, 200));
		absScroll1.setPreferredSize(new Dimension(276, 132));
		absScroll1.setViewportView(selectXforms);
		addXform = new JButton("Add Transform");
		removeXform = new JButton("Remove Transform");
		addAllXforms = new JButton("Add All Transforms");
		clearXforms = new JButton("Clear Transforms");
		addXform.addActionListener(this);
		removeXform.addActionListener(this);
		addAllXforms.addActionListener(this);
		clearXforms.addActionListener(this);
		listOfTransformsLabelHolder.add(absLabel);
		listOfTransformsHolder.add(absScroll1);
		listOfTransformsLabelHolder.add(abstractLabel);
		listOfTransformsHolder.add(absScroll);
		absHolder.add(listOfTransformsLabelHolder, "North");
		absHolder.add(listOfTransformsHolder, "Center");
		JPanel absButtonHolder = new JPanel();
		absButtonHolder.add(addXform);
		absButtonHolder.add(removeXform);
		absButtonHolder.add(addAllXforms);
		absButtonHolder.add(clearXforms);
		absHolder.add(absButtonHolder, "South");
		this.add(absHolder, "South");
		*/
		preAbsModel = new DefaultListModel();
		loopAbsModel = new DefaultListModel();
		postAbsModel = new DefaultListModel();
		preAbs = new JList(preAbsModel);
		loopAbs = new JList(loopAbsModel);
		postAbs = new JList(postAbsModel);
		preAbsLabel = new JLabel("Preprocess abstraction methods:");
		loopAbsLabel = new JLabel("Main loop abstraction methods:");
		postAbsLabel = new JLabel("Postprocess abstraction methods:");
		JPanel absHolder = new JPanel(new BorderLayout());
		JPanel listOfAbsLabelHolder = new JPanel(new GridLayout(1, 3));
		JPanel listOfAbsHolder = new JPanel(new GridLayout(1, 3));
		JPanel listOfAbsButtonHolder = new JPanel(new GridLayout(1, 3));
		JScrollPane preAbsScroll = new JScrollPane();
		JScrollPane loopAbsScroll = new JScrollPane();
		JScrollPane postAbsScroll = new JScrollPane();
		preAbsScroll.setMinimumSize(new Dimension(260, 200));
		preAbsScroll.setPreferredSize(new Dimension(276, 132));
		preAbsScroll.setViewportView(preAbs);
		loopAbsScroll.setMinimumSize(new Dimension(260, 200));
		loopAbsScroll.setPreferredSize(new Dimension(276, 132));
		loopAbsScroll.setViewportView(loopAbs);
		postAbsScroll.setMinimumSize(new Dimension(260, 200));
		postAbsScroll.setPreferredSize(new Dimension(276, 132));
		postAbsScroll.setViewportView(postAbs);
		addPreAbs = new JButton("Add");
		rmPreAbs = new JButton("Remove");
		editPreAbs = new JButton("Edit");
		clearPreAbs = new JButton("Clear");
		JPanel preAbsButtonHolder = new JPanel();
		preAbsButtonHolder.add(addPreAbs);
		preAbsButtonHolder.add(rmPreAbs);
		preAbsButtonHolder.add(clearPreAbs);
		// preAbsButtonHolder.add(editPreAbs);
		addLoopAbs = new JButton("Add");
		rmLoopAbs = new JButton("Remove");
		editLoopAbs = new JButton("Edit");
		clearLoopAbs = new JButton("Clear");
		JPanel loopAbsButtonHolder = new JPanel();
		loopAbsButtonHolder.add(addLoopAbs);
		loopAbsButtonHolder.add(rmLoopAbs);
		loopAbsButtonHolder.add(clearLoopAbs);
		// loopAbsButtonHolder.add(editLoopAbs);
		addPostAbs = new JButton("Add");
		rmPostAbs = new JButton("Remove");
		editPostAbs = new JButton("Edit");
		clearPostAbs = new JButton("Clear");
		JPanel postAbsButtonHolder = new JPanel();
		postAbsButtonHolder.add(addPostAbs);
		postAbsButtonHolder.add(rmPostAbs);
		postAbsButtonHolder.add(clearPostAbs);
		// postAbsButtonHolder.add(editPostAbs);
		listOfAbsLabelHolder.add(preAbsLabel);
		listOfAbsHolder.add(preAbsScroll);
		listOfAbsLabelHolder.add(loopAbsLabel);
		listOfAbsHolder.add(loopAbsScroll);
		listOfAbsLabelHolder.add(postAbsLabel);
		listOfAbsHolder.add(postAbsScroll);
		listOfAbsButtonHolder.add(preAbsButtonHolder);
		listOfAbsButtonHolder.add(loopAbsButtonHolder);
		listOfAbsButtonHolder.add(postAbsButtonHolder);
		absHolder.add(listOfAbsLabelHolder, "North");
		absHolder.add(listOfAbsHolder, "Center");
		absHolder.add(listOfAbsButtonHolder, "South");
		preAbsModel.addElement(xform12);
		loopAbsModel.addElement(xform0);
		loopAbsModel.addElement(xform1);
		loopAbsModel.addElement(xform3);
		loopAbsModel.addElement(xform4);
		loopAbsModel.addElement(xform5);
		loopAbsModel.addElement(xform6);
		loopAbsModel.addElement(xform7);
		loopAbsModel.addElement(xform8);
		loopAbsModel.addElement(xform9);
		loopAbsModel.addElement(xform10);
		loopAbsModel.addElement(xform11);
		loopAbsModel.addElement(xform12);
		loopAbsModel.addElement(xform13);
		loopAbsModel.addElement(xform14);
		loopAbsModel.addElement(xform15);
		loopAbsModel.addElement(xform16);
		loopAbsModel.addElement(xform17);
		loopAbsModel.addElement(xform18);
		loopAbsModel.addElement(xform19);
		loopAbsModel.addElement(xform20);
		loopAbsModel.addElement(xform22);
		loopAbsModel.addElement(xform23);
		loopAbsModel.addElement(xform24);
		loopAbsModel.addElement(xform25);
		loopAbsModel.addElement(xform26);
		loopAbsModel.addElement(xform29);
		postAbsModel.addElement(xform21);
		preAbs.setListData(preAbsModel.toArray());
		loopAbs.setListData(loopAbsModel.toArray());
		postAbs.setListData(postAbsModel.toArray());
		//preAbs.setEnabled(false);
		//loopAbs.setEnabled(false);
		//postAbs.setEnabled(false);
		//preAbs.addMouseListener(this);
		//loopAbs.addMouseListener(this);
		//postAbs.addMouseListener(this);
		//preAbsLabel.setEnabled(false);
		//loopAbsLabel.setEnabled(false);
		//postAbsLabel.setEnabled(false);
		//addPreAbs.setEnabled(false);
		//rmPreAbs.setEnabled(false);
		//editPreAbs.setEnabled(false);
		addPreAbs.addActionListener(this);
		rmPreAbs.addActionListener(this);
		editPreAbs.addActionListener(this);
		clearPreAbs.addActionListener(this);
		//addLoopAbs.setEnabled(false);
		//rmLoopAbs.setEnabled(false);
		//editLoopAbs.setEnabled(false);
		addLoopAbs.addActionListener(this);
		rmLoopAbs.addActionListener(this);
		editLoopAbs.addActionListener(this);
		clearLoopAbs.addActionListener(this);
		//addPostAbs.setEnabled(false);
		//rmPostAbs.setEnabled(false);
		//editPostAbs.setEnabled(false);
		addPostAbs.addActionListener(this);
		rmPostAbs.addActionListener(this);
		editPostAbs.addActionListener(this);
		clearPostAbs.addActionListener(this);
		this.add(absHolder, "South");

		change = false;
	}
	
	public AbstPane(String directory, String lpnFile, Log log, BioSim biosim,
			boolean lema, boolean atacs) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		this.directory = directory;
		this.log = log;
		this.setLayout(new BorderLayout());
		LhpnFile lhpn = new LhpnFile();
		lhpn.load(directory + separator + lpnFile);

		// Creates the interesting species JList
		listModel = new DefaultListModel();
		intSpecies = new JList(lhpn.getVariables());
		species = new JList(listModel);
		JLabel spLabel = new JLabel("Available Variables:");
		JLabel speciesLabel = new JLabel("Interesting Variables:");
		JPanel speciesHolder = new JPanel(new BorderLayout());
		JPanel listOfSpeciesLabelHolder = new JPanel(new GridLayout(1, 2));
		JPanel listOfSpeciesHolder = new JPanel(new GridLayout(1, 2));
		JScrollPane scroll = new JScrollPane();
		JScrollPane scroll1 = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 200));
		scroll.setPreferredSize(new Dimension(276, 132));
		scroll.setViewportView(species);
		scroll1.setMinimumSize(new Dimension(260, 200));
		scroll1.setPreferredSize(new Dimension(276, 132));
		scroll1.setViewportView(intSpecies);
		addIntSpecies = new JButton("Add Variable");
		removeIntSpecies = new JButton("Remove Variable");
		clearIntSpecies = new JButton("Clear Variable");
		addIntSpecies.addActionListener(this);
		removeIntSpecies.addActionListener(this);
		clearIntSpecies.addActionListener(this);
		listOfSpeciesLabelHolder.add(spLabel);
		listOfSpeciesHolder.add(scroll1);
		listOfSpeciesLabelHolder.add(speciesLabel);
		listOfSpeciesHolder.add(scroll);
		speciesHolder.add(listOfSpeciesLabelHolder, "North");
		speciesHolder.add(listOfSpeciesHolder, "Center");
		JPanel buttonHolder = new JPanel();
		buttonHolder.add(addIntSpecies);
		buttonHolder.add(removeIntSpecies);
		buttonHolder.add(clearIntSpecies);
		speciesHolder.add(buttonHolder, "South");
		this.add(speciesHolder, "North");

		JPanel factorPanel = new JPanel();
		JLabel factorLabel = new JLabel("Normalization Factor");
		factorField = new JTextField("5");
		factorField.setPreferredSize(new Dimension(40, 18));
		factorPanel.add(factorLabel);
		factorPanel.add(factorField);
		JLabel iterLabel = new JLabel("Maximum Number of Iterations");
		iterField = new JTextField("100");
		iterField.setPreferredSize(new Dimension(40, 18));
		factorPanel.add(iterLabel);
		factorPanel.add(iterField);
		this.add(factorPanel);

		// Creates the abstractions JList
		//absListModel = new DefaultListModel();
		//selectXforms = new JList(transforms);
		//xforms = new JList(absListModel);
		//for (String s : transforms) {
		//	absListModel.addElement(s);
		//}
		JLabel absLabel = new JLabel("Available Transforms:");
		JLabel abstractLabel = new JLabel("Selected Transforms:");
		JPanel absHolder = new JPanel(new BorderLayout());
		JPanel listOfTransformsLabelHolder = new JPanel(new GridLayout(1, 2));
		JPanel listOfTransformsHolder = new JPanel(new GridLayout(1, 2));
		JScrollPane absScroll = new JScrollPane();
		JScrollPane absScroll1 = new JScrollPane();
		absScroll.setMinimumSize(new Dimension(260, 200));
		absScroll.setPreferredSize(new Dimension(276, 132));
		absScroll.setViewportView(xforms);
		absScroll1.setMinimumSize(new Dimension(260, 200));
		absScroll1.setPreferredSize(new Dimension(276, 132));
		absScroll1.setViewportView(selectXforms);
		addXform = new JButton("Add Transform");
		removeXform = new JButton("Remove Transform");
		addAllXforms = new JButton("Add All Transforms");
		clearXforms = new JButton("Clear Transforms");
		addXform.addActionListener(this);
		removeXform.addActionListener(this);
		addAllXforms.addActionListener(this);
		clearXforms.addActionListener(this);
		listOfTransformsLabelHolder.add(absLabel);
		listOfTransformsHolder.add(absScroll1);
		listOfTransformsLabelHolder.add(abstractLabel);
		listOfTransformsHolder.add(absScroll);
		absHolder.add(listOfTransformsLabelHolder, "North");
		absHolder.add(listOfTransformsHolder, "Center");
		JPanel absButtonHolder = new JPanel();
		absButtonHolder.add(addXform);
		absButtonHolder.add(removeXform);
		absButtonHolder.add(addAllXforms);
		absButtonHolder.add(clearXforms);
		absHolder.add(absButtonHolder, "South");
		//this.add(absHolder, "South");

		change = false;
	}

	/**
	 * This method performs different functions depending on what menu items or
	 * buttons are selected.
	 * 
	 * @throws
	 * @throws
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addIntSpecies) {
			if (!listModel.contains(intSpecies.getSelectedValue())) {
				listModel.addElement(intSpecies.getSelectedValue());
			}
		}
		if (e.getSource() == removeIntSpecies) {
			listModel.removeElement(species.getSelectedValue());
		}
		if (e.getSource() == clearIntSpecies) {
			listModel.removeAllElements();
		}
		if (e.getSource() == addXform) {
			if (!absListModel.contains(selectXforms.getSelectedValue())) {
				absListModel.addElement(selectXforms.getSelectedValue());
			}
		}
		if (e.getSource() == removeXform) {
			absListModel.removeElement(xforms.getSelectedValue());
		}
		if (e.getSource() == addPreAbs || e.getSource() == addLoopAbs || e.getSource() == addPostAbs) {
			JPanel addAbsPanel = new JPanel(new BorderLayout());
			JComboBox absList = new JComboBox();
			for (String s : transforms) {
				absList.addItem(s);
			}
			addAbsPanel.add(absList, "Center");
			String[] options = { "Add", "Cancel" };
			int value = JOptionPane.showOptionDialog(BioSim.frame, addAbsPanel,
					"Add abstraction method", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
					null, options, options[0]);
			if (value == JOptionPane.YES_OPTION) {
				if (e.getSource() == addPreAbs) {
					addPreXform(absList.getSelectedItem().toString());
				}
				else if (e.getSource() == addLoopAbs) {
					addLoopXform(absList.getSelectedItem().toString());
				}
				else {
					addPostXform(absList.getSelectedItem().toString());
				}
			}
		}
		if (e.getSource() == rmPreAbs) {
			preAbsModel.removeElement(preAbs.getSelectedValue());
			preAbs.setListData(preAbsModel.toArray());
		}
		if (e.getSource() == rmLoopAbs) {
			loopAbsModel.removeElement(loopAbs.getSelectedValue());
			loopAbs.setListData(loopAbsModel.toArray());
		}
		if (e.getSource() == rmPostAbs) {
			postAbsModel.removeElement(postAbs.getSelectedValue());
			postAbs.setListData(postAbsModel.toArray());
		}
		if (e.getSource() == clearPreAbs) {
			preAbsModel.removeAllElements();
			preAbs.setListData(preAbsModel.toArray());
		}
		if (e.getSource() == clearLoopAbs) {
			loopAbsModel.removeAllElements();
			loopAbs.setListData(loopAbsModel.toArray());
		}
		if (e.getSource() == clearPostAbs) {
			postAbsModel.removeAllElements();
			postAbs.setListData(postAbsModel.toArray());
		}
		if (e.getSource() == addAllXforms) {
			for (String s : transforms) {
				if (!absListModel.contains(s)) {
					absListModel.addElement(s);
				}
			}
		}
		if (e.getSource() == clearXforms) {
			absListModel.removeAllElements();
		}
	}

	public void addIntVar(String variable) {
		if (!listModel.contains(variable)) {
			listModel.addElement(variable);
		}
	}

	//public void addXform(String variable) {
	//	if (!absListModel.contains(variable)) {
	//		absListModel.addElement(variable);
	//	}
	//}
	
	public void addPreXform(String xform) {
		if (!preAbsModel.contains(xform)) {
			add(preAbs, preAbsModel, xform);
		}
	}
	
	public void addLoopXform(String xform) {
		if (!loopAbsModel.contains(xform)) {
			add(loopAbs, loopAbsModel, xform);
		}
	}
	
	public void addPostXform(String xform) {
		if (!postAbsModel.contains(xform)) {
			add(postAbs, postAbsModel, xform);
		}
	}
	
	public void add(JList currentList, DefaultListModel currentModel, Object newItem) {
		Object[] list = new Object[currentList.getModel().getSize() + 1];
		int addAfter = currentList.getSelectedIndex();
		DefaultListModel newModel = new DefaultListModel();
		for (int i = 0; i <= currentList.getModel().getSize(); i++) {
			if (i <= addAfter) {
				list[i] = currentList.getModel().getElementAt(i);
				newModel.addElement(currentList.getModel().getElementAt(i));
			}
			else if (i == (addAfter + 1)) {
				list[i] = newItem;
				newModel.addElement(newItem);
			}
			else {
				list[i] = currentList.getModel().getElementAt(i - 1);
				newModel.addElement(currentList.getModel().getElementAt(i - 1));
			}
		}
		currentList.setListData(list);
		if (currentModel.equals(preAbsModel)) {
			preAbsModel  = newModel;
		}
		else if (currentModel.equals(loopAbsModel)) {
			loopAbsModel = newModel;
		}
		else if (currentModel.equals(postAbsModel)) {
			postAbsModel = newModel;
		}
	}

	public void removeAllXform() {
		preAbsModel.removeAllElements();
		loopAbsModel.removeAllElements();
		postAbsModel.removeAllElements();
		//absListModel.removeAllElements();
	}

	public void run() {

	}

	public void saveAs() {
		String newName = JOptionPane.showInputDialog(BioSim.frame, "Enter Verification name:",
				"Verification Name", JOptionPane.PLAIN_MESSAGE);
		if (newName == null) {
			return;
		}
		if (!newName.endsWith(".ver")) {
			newName = newName + ".ver";
		}
		save(newName);
	}

	public void save() {
		save(absFile);
	}

	public void save(String filename) {
		// JOptionPane.showMessageDialog(this, verifyFile);
		try {
			// System.out.println(absFile);
			Properties prop = new Properties();
			// FileInputStream in = new FileInputStream(new File(directory +
			// separator + filename));
			// prop.load(in);
			// in.close();
			// prop.setProperty("verification.file", verifyFile);
			String intVars = "";
			for (int i = 0; i < listModel.getSize(); i++) {
				intVars = listModel.getElementAt(i) + " ";
			}
			if (!intVars.equals("")) {
				prop.setProperty("abstraction.interesting", intVars);
			}
			for (int i = 0; i < absListModel.getSize(); i++) {
				String s = absListModel.getElementAt(i).toString();
				if (preAbsModel.contains(s)) {
					prop.setProperty(s, "preloop");
				}
				else if (absListModel.contains(s)) {
					prop.setProperty(s, "mainloop");
				}
				else if (postAbsModel.contains(s)) {
					prop.setProperty(s, "postloop");
				}
			}
			if (!factorField.getText().equals("")) {
				prop.setProperty("abstraction.factor", factorField.getText());
			}
			if (!iterField.getText().equals("")) {
				prop.setProperty("abstraction.factor", iterField.getText());
			}
			FileOutputStream out = new FileOutputStream(new File(directory + separator + absFile));
			prop.store(out, absFile);
			out.close();
			log.addText("Saving Parameter File:\n" + directory + separator + absFile + "\n");
			change = false;
		}
		catch (Exception e1) {
			// e1.printStackTrace();
			JOptionPane.showMessageDialog(BioSim.frame, "Unable to save parameter file!",
					"Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
		if (componentList != null) {
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
					JOptionPane.showMessageDialog(BioSim.frame,
							"Cannot add the selected component.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	public void reload(String newname) {
		field.setText(newname);
	}

	public String[] getIntVars() {
		String[] intVars = new String[listModel.getSize()];
		for (int i = 0; i < listModel.getSize(); i++) {
			if (listModel.elementAt(i) != null) {
				intVars[i] = listModel.elementAt(i).toString();
			}
		}
		return intVars;
	}

	public void viewCircuit() {
		String[] getFilename;
		if (field.getText().trim().equals("")) {
			// getFilename = verifyFile.split("\\.");
		}
		else {
			getFilename = new String[1];
			getFilename[0] = field.getText().trim();
		}
	}

	public boolean isSimplify() {
		if (verification == null) {
			return true;
		}
		if (verification.simplify.isSelected() || verification.abstractLhpn.isSelected()) {
			return true;
		}
		return false;
	}

	public boolean isAbstract() {
		if (verification == null) {
			return true;
		}
		return verification.abstractLhpn.isSelected();
	}

	public Integer getNormFactor() {
		String factorString = factorField.getText();
		return Integer.parseInt(factorString);
	}

	public Integer maxIterations() {
		String iterString = iterField.getText();
		return Integer.parseInt(iterString);
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
				JOptionPane.showMessageDialog(BioSim.frame, scrolls, "Run Log",
						JOptionPane.INFORMATION_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(BioSim.frame, "No run log exists.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(BioSim.frame, "Unable to view run log.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public boolean hasChanged() {
		if (!oldBdd.equals(field.getText())) {
			return true;
		}
		return change;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			// if (e.getSource() == intSpecies) {
			// addInterstingSpecies();
			// }
			// else if (e.getSource() == species) {
			// editInterstingSpecies();
			// }
		}
	}

}
