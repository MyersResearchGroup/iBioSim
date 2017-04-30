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
package edu.utah.ece.async.ibiosim.gui.util.preferences;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.metal.MetalButtonUI;

import com.mxgraph.util.mxConstants;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.CompatibilityFixer;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.util.FileTree;

/**
 * 
 *
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EditPreferences {

	private JCheckBox Undeclared, Units;

	//private JComboBox LevelVersion;

	private JLabel viewerLabel;
	
	private boolean checkUndeclared, checkUnits;
	
	private JCheckBox dialog;
	private JCheckBox icons;
	private JCheckBox delete;
	private JCheckBox libsbmlFlatten;
	private JCheckBox libsbmlValidate;
	private JCheckBox showWarnings;
	private JCheckBox infix;
	private JTextField xhtmlCmd;
	private JTextField dotCmd;
	private JTextField prismCmd;
	private JTextField verCmd;
	private JTextField viewerField;
	
	private JTextField ACTIVED_VALUE;
	private JTextField KACT_VALUE;
	private JTextField KBASAL_VALUE;
	private JTextField KDECAY_VALUE;
	private JTextField KECDECAY_VALUE;
	private JTextField COOPERATIVITY_VALUE;
	private JTextField RNAP_VALUE;
	private JTextField PROMOTER_COUNT_VALUE;
	private JTextField OCR_VALUE;
	private JTextField RNAP_BINDING_VALUE;
	private JTextField ACTIVATED_RNAP_BINDING_VALUE;
	private JTextField KREP_VALUE;
	private JTextField STOICHIOMETRY_VALUE;
	private JTextField KCOMPLEX_VALUE;
	private JTextField FORWARD_MEMDIFF_VALUE;
	private JTextField REVERSE_MEMDIFF_VALUE;
	private JTextField KECDIFF_VALUE;
	
	private JTextField uriField;
	private JTextField regexField;
	private JComboBox validationBox;
	private JComboBox assemblyBox;
	private JComboBox warningBox;
	
	private JTextField initialTime;
	private JTextField outputStartTime;
	private JTextField limit;
	private JTextField interval;
	private JTextField minStep;
	private JTextField step;
	private JTextField error;
	private JTextField relError;
	private JTextField seed;
	private JTextField runs;
	private JTextField rapid1;
	private JTextField rapid2;
	private JTextField qssa;
	private JTextField concentration;
	private JComboBox useInterval;
	private JTextField simCommand;
	private JComboBox sim;
	private JComboBox abs;
	private JComboBox type;
	
	private JTextField tn;
	private JTextField tj;
	private JTextField ti;
	private JComboBox bins;
	private JComboBox equaldata;
	private JComboBox autolevels;
	private JTextField ta;
	private JTextField tr;
	private JTextField tm;
	private JTextField tt;
	private JComboBox debug;
	private JComboBox succpred;
	private JComboBox findbaseprob;
	
	private boolean async;
	
	private FileTree tree;
	
	private JFrame frame;
	
	private class SchematicElement {
		JButton colorButton;
		JButton strokeButton;
		JButton fontButton;
		JComboBox shape;
		JTextField opacity;
		
		private SchematicElement(String shapeStr,Color color,boolean edge) {
			if (edge) {
				String[] choices = { mxConstants.NONE, mxConstants.ARROW_BLOCK, mxConstants.ARROW_CLASSIC, mxConstants.ARROW_DIAMOND,
						mxConstants.ARROW_OPEN, mxConstants.ARROW_OVAL };
				shape = new JComboBox(choices);
				strokeButton = createColorButton(color);
				opacity = new JTextField("100");
			} else {
				String[] choices = { mxConstants.NONE, mxConstants.SHAPE_ACTOR, mxConstants.SHAPE_CYLINDER, 
						mxConstants.SHAPE_DOUBLE_ELLIPSE, mxConstants.SHAPE_ELLIPSE, mxConstants.SHAPE_HEXAGON, 
						mxConstants.SHAPE_IMAGE, mxConstants.SHAPE_RECTANGLE, mxConstants.SHAPE_RECTANGLE + " (rounded)", 
						mxConstants.SHAPE_RHOMBUS, mxConstants.SHAPE_SWIMLANE, mxConstants.SHAPE_TRIANGLE};
				shape = new JComboBox(choices);
				strokeButton = createColorButton(new Color(0));
				opacity = new JTextField("50");
			}
			shape.setSelectedItem(shapeStr);
			colorButton = createColorButton(color);
			fontButton = createColorButton(new Color(0));
		}
	}
	
	private HashMap<String,SchematicElement> schematicElements = null;
	
	public EditPreferences(JFrame frame,boolean async, FileTree tree) {
		this.frame = frame;
		this.async = async;
		this.tree = tree;
		schematicElements = new HashMap<String, SchematicElement>();
	}
	
	private JPanel generalPreferences(Preferences biosimrc) {
		// general preferences
		dialog = new JCheckBox("Use File Dialog");
		icons = new JCheckBox("Use Plus/Minus For Expanding/Collapsing File Tree");
		delete = new JCheckBox("Must Confirm File Deletions");
		libsbmlFlatten = new JCheckBox("Use libsbml to Flatten Models");
		libsbmlValidate = new JCheckBox("Use libsbml to Validate Models");
		showWarnings = new JCheckBox("Report Validation Warnings");
		xhtmlCmd = new JTextField(biosimrc.get("biosim.general.browser", ""));
		JLabel xhtmlCmdLabel = new JLabel("Browser Viewer Command");
		if (xhtmlCmd.getText().equals("")) {
			if (System.getProperty("os.name").contentEquals("Linux")) {
				xhtmlCmd.setText("xdg-open");
			} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
				xhtmlCmd.setText("open");
			} else {
				xhtmlCmd.setText("cmd /c start");
			}
		}
		prismCmd = new JTextField(biosimrc.get("biosim.general.prism", ""));
		JLabel prismCmdLabel = new JLabel("PRISM Model Checking Command");
		if (prismCmd.getText().equals("")) {
			prismCmd.setText("prism");
		}
		dotCmd = new JTextField(biosimrc.get("biosim.general.graphviz", ""));
		JLabel dotCmdLabel = new JLabel("Graphviz Viewer Command");
		if (dotCmd.getText().equals("")) {
			if (System.getProperty("os.name").contentEquals("Linux")) {
				dotCmd.setText("xdg-open");
			} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
				dotCmd.setText("open");
			} else {
				dotCmd.setText("dotty");
			}
		}
		infix = new JCheckBox("Use Infix Expression Parser");
		if (biosimrc.get("biosim.general.file_browser", "").equals("FileDialog")) {
			dialog.setSelected(true);
		}
		else {
			dialog.setSelected(false);
		}
		if (biosimrc.get("biosim.general.tree_icons", "").equals("default")) {
			icons.setSelected(false);
		}
		else {
			icons.setSelected(true);
		}
		if (biosimrc.get("biosim.general.delete", "").equals("confirm")) {
			delete.setSelected(true);
		}
		else {
			delete.setSelected(false);
		}
		if (biosimrc.get("biosim.general.flatten", "").equals("libsbml")) {
			libsbmlFlatten.setSelected(true);
		}
		else {
			libsbmlFlatten.setSelected(false);
		}
		if (biosimrc.get("biosim.general.validate", "").equals("libsbml")) {
			libsbmlValidate.setSelected(true);
		}
		else {
			libsbmlValidate.setSelected(false);
		}
		if (biosimrc.get("biosim.general.warnings", "").equals("true")) {
			showWarnings.setSelected(true);
		}
		else {
			showWarnings.setSelected(false);
		}
		if (biosimrc.get("biosim.general.infix", "").equals("prefix")) {
			infix.setSelected(false);
		}
		else {
			infix.setSelected(true);
		}			
		JLabel verCmdLabel = new JLabel("Verification command:");
		verCmd = new JTextField(biosimrc.get("lema.verification.command", ""));
		viewerLabel = new JLabel("External Editor for non-LPN files:");
		viewerField = new JTextField(biosimrc.get("lema.general.viewer", ""));
		
		JButton restoreGen = new JButton("Restore Defaults");
		restoreGen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setSelected(false);
				icons.setSelected(false);
				delete.setSelected(true);
				libsbmlFlatten.setSelected(false);
				libsbmlValidate.setSelected(false);
				showWarnings.setSelected(false);
				if (System.getProperty("os.name").contentEquals("Linux")) {
					xhtmlCmd.setText("xdg-open");
					dotCmd.setText("xdg-open");
				} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					xhtmlCmd.setText("open");
					dotCmd.setText("open");
				} else {
					xhtmlCmd.setText("cmd /c start");
					dotCmd.setText("dotty");
				}
				prismCmd.setText("prism");
				infix.setSelected(true);
				verCmd.setText("");
				viewerField.setText("");
			}
		});	

		// create general preferences panel
		JPanel generalPrefsBordered;
		if (async) {
			generalPrefsBordered = new JPanel(new GridLayout(12,1));
		} else {
			generalPrefsBordered = new JPanel(new GridLayout(10,1));
		}
		generalPrefsBordered.add(dialog);
		generalPrefsBordered.add(icons);
		generalPrefsBordered.add(delete);
		generalPrefsBordered.add(libsbmlFlatten);
		generalPrefsBordered.add(libsbmlValidate);
		generalPrefsBordered.add(showWarnings);
		JPanel xhtmlCmdPanel = new JPanel(new GridLayout(1,2));
		xhtmlCmdPanel.add(xhtmlCmdLabel);
		xhtmlCmdPanel.add(xhtmlCmd);
		generalPrefsBordered.add(xhtmlCmdPanel);
		JPanel dotCmdPanel = new JPanel(new GridLayout(1,2));
		dotCmdPanel.add(dotCmdLabel);
		dotCmdPanel.add(dotCmd);
		generalPrefsBordered.add(dotCmdPanel);
		JPanel prismCmdPanel = new JPanel(new GridLayout(1,2));
		prismCmdPanel.add(prismCmdLabel);
		prismCmdPanel.add(prismCmd);
		generalPrefsBordered.add(prismCmdPanel);
		//generalPrefsBordered.add(infix);
		if (async) {
			JPanel verCmdPanel = new JPanel(new GridLayout(1,2));
			verCmdPanel.add(verCmdLabel);
			verCmdPanel.add(verCmd);
			generalPrefsBordered.add(verCmdPanel);
			JPanel viewerPanel = new JPanel(new GridLayout(1,2));
			viewerPanel.add(viewerLabel);
			viewerPanel.add(viewerField);
			generalPrefsBordered.add(viewerPanel);
		}
		JPanel generalPrefsFinal = new JPanel(new BorderLayout());
		generalPrefsFinal.add(generalPrefsBordered,"North");
		generalPrefsFinal.add(restoreGen,"South");
		return generalPrefsFinal;
	}
	
	private static JButton createColorButton(Color color) {
		JButton colorButton = new JButton();
		colorButton.setPreferredSize(new Dimension(30, 20));
		colorButton.setBorder(BorderFactory.createLineBorder(Color.darkGray));
		colorButton.setBackground(color);
		colorButton.setForeground(color);
		colorButton.setUI(new MetalButtonUI());
		//colorButton.setActionCommand("" + i);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//int i = Integer.parseInt(e.getActionCommand());
				Color newColor = JColorChooser.showDialog(Gui.frame, "Choose Color", ((JButton) e.getSource()).getBackground());
				if (newColor != null) {
					((JButton) e.getSource()).setBackground(newColor);
					((JButton) e.getSource()).setForeground(newColor);
				}
			}
		});
		return colorButton;
	}
	
	private static Color createColorFromString(String colorStr) {
		int red = Integer.valueOf(colorStr.substring(1,3),16);
		int green = Integer.valueOf(colorStr.substring(3,5),16);
		int blue = Integer.valueOf(colorStr.substring(5,7),16);
		return new Color(red,green,blue);
	}
	
	private void setDefaultSchematicPreferences() {
		SchematicElement schematicElement;

		if (!async) {
			schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE+" (rounded)",createColorFromString("#FFFFFF"),false);
			schematicElements.put("Compartment",schematicElement);

			schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE+" (rounded)",createColorFromString("#87F274"),false);
			schematicElements.put("subCompartment",schematicElement);

			schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE+" (rounded)",createColorFromString("#5CB4F2"),false);
			schematicElements.put("Species",schematicElement);

			schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#C7007B"),false);
			schematicElements.put("Reaction",schematicElement);

			schematicElement = new SchematicElement(mxConstants.SHAPE_RHOMBUS,createColorFromString("#F00E0E"),false);
			schematicElements.put("Promoter",schematicElement);

			schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#00FF00"),false);
			schematicElements.put("Event",schematicElement);
			
			schematicElement = new SchematicElement(mxConstants.ARROW_BLOCK,createColorFromString("#34BA04"),true);
			schematicElements.put("Activation_Edge",schematicElement);

			schematicElement = new SchematicElement(mxConstants.ARROW_OVAL,createColorFromString("#FA2A2A"),true);
			schematicElements.put("Repression_Edge",schematicElement);

			schematicElement = new SchematicElement(mxConstants.ARROW_DIAMOND,createColorFromString("#000000"),true);
			schematicElements.put("NoInfluence_Edge",schematicElement);

			schematicElement = new SchematicElement(mxConstants.ARROW_OPEN,createColorFromString("#4E5D9C"),true);
			schematicElements.put("Complex_Edge",schematicElement);

			schematicElement = new SchematicElement(mxConstants.ARROW_OPEN,createColorFromString("#34BA04"),true);
			schematicElements.put("Production_Edge",schematicElement);

			schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#FFFFFF"),false);
			schematicElements.put("Component",schematicElement);
		}
		
		schematicElement = new SchematicElement(mxConstants.SHAPE_SWIMLANE,createColorFromString("#FFFF00"),false);
		schematicElements.put("Rule",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_HEXAGON,createColorFromString("#FF0000"),false);
		schematicElements.put("Constraint",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#0000FF"),false);
		schematicElements.put("Variable",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#87F274"),false);
		schematicElements.put("subComponent",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#FFFFFF"),false);
		schematicElements.put("Transition",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#808080"),false);
		schematicElements.put("Boolean_True",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_RECTANGLE,createColorFromString("#FFFFFF"),false);
		schematicElements.put("Boolean_False",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_ELLIPSE,createColorFromString("#FFFFFF"),false);
		schematicElements.put("Place_NotMarked",schematicElement);

		schematicElement = new SchematicElement(mxConstants.SHAPE_DOUBLE_ELLIPSE,createColorFromString("#808080"),false);
		schematicElements.put("Place_Marked",schematicElement);

		schematicElement = new SchematicElement(mxConstants.ARROW_OPEN,createColorFromString("#000000"),true);
		schematicElements.put("Default_Edge",schematicElement);
	}
	
	private void restoreDefaultSchematicPreferences() {
		SchematicElement schematicElement;
		for (String element:schematicElements.keySet()) {
			schematicElement = schematicElements.get(element);
			schematicElement.shape.setSelectedItem(mxConstants.SHAPE_RECTANGLE);
			schematicElement.colorButton.setForeground(createColorFromString("#FFFFFF"));
			schematicElement.colorButton.setBackground(createColorFromString("#FFFFFF"));
			schematicElement.strokeButton.setForeground(new Color(0));
			schematicElement.strokeButton.setBackground(new Color(0));
			schematicElement.fontButton.setForeground(new Color(0));
			schematicElement.fontButton.setBackground(new Color(0));
			schematicElement.opacity.setText("50");
		}
		if (!async) {
			schematicElement = schematicElements.get("Compartment");
			schematicElement.shape.setSelectedItem(mxConstants.SHAPE_RECTANGLE+" (rounded)");
			
			schematicElement = schematicElements.get("subCompartment");
			schematicElement.colorButton.setForeground(createColorFromString("#87F274"));
			schematicElement.colorButton.setBackground(createColorFromString("#87F274"));
			schematicElement.shape.setSelectedItem(mxConstants.SHAPE_RECTANGLE+" (rounded)");

			schematicElement = schematicElements.get("Species");
			schematicElement.colorButton.setForeground(createColorFromString("#5CB4F2"));
			schematicElement.colorButton.setBackground(createColorFromString("#5CB4F2"));
			schematicElement.shape.setSelectedItem(mxConstants.SHAPE_RECTANGLE+" (rounded)");

			schematicElement = schematicElements.get("Reaction");
			schematicElement.colorButton.setForeground(createColorFromString("#C7007B"));
			schematicElement.colorButton.setBackground(createColorFromString("#C7007B"));
			schematicElement.shape.setSelectedItem(mxConstants.SHAPE_RECTANGLE);

			schematicElement = schematicElements.get("Event");
			schematicElement.colorButton.setForeground(createColorFromString("#00FF00"));
			schematicElement.colorButton.setBackground(createColorFromString("#00FF00"));

			schematicElement = schematicElements.get("Promoter");
			schematicElement.colorButton.setForeground(createColorFromString("#F00E0E"));
			schematicElement.colorButton.setBackground(createColorFromString("#F00E0E"));
			schematicElement.shape.setSelectedItem(mxConstants.SHAPE_RHOMBUS);
			
			schematicElement = schematicElements.get("Activation_Edge");
			schematicElement.strokeButton.setForeground(createColorFromString("#34BA04"));
			schematicElement.strokeButton.setBackground(createColorFromString("#34BA04"));
			schematicElement.shape.setSelectedItem(mxConstants.ARROW_BLOCK);
			schematicElement.opacity.setText("100");

			schematicElement = schematicElements.get("Repression_Edge");
			schematicElement.strokeButton.setForeground(createColorFromString("#FA2A2A"));
			schematicElement.strokeButton.setBackground(createColorFromString("#FA2A2A"));
			schematicElement.shape.setSelectedItem(mxConstants.ARROW_OVAL);
			schematicElement.opacity.setText("100");

			schematicElement = schematicElements.get("NoInfluence_Edge");
			schematicElement.strokeButton.setForeground(createColorFromString("#000000"));
			schematicElement.strokeButton.setBackground(createColorFromString("#000000"));
			schematicElement.shape.setSelectedItem(mxConstants.ARROW_DIAMOND);
			schematicElement.opacity.setText("100");

			schematicElement = schematicElements.get("Complex_Edge");
			schematicElement.strokeButton.setForeground(createColorFromString("#4E5D9C"));
			schematicElement.strokeButton.setBackground(createColorFromString("#4E5D9C"));
			schematicElement.shape.setSelectedItem(mxConstants.ARROW_OPEN);
			schematicElement.opacity.setText("100");

			schematicElement = schematicElements.get("Production_Edge");
			schematicElement.strokeButton.setForeground(createColorFromString("#34BA04"));
			schematicElement.strokeButton.setBackground(createColorFromString("#34BA04"));
			schematicElement.shape.setSelectedItem(mxConstants.ARROW_OPEN);
			schematicElement.opacity.setText("100");
		}
		
		schematicElement = schematicElements.get("Rule");
		schematicElement.colorButton.setForeground(createColorFromString("#FFFF00"));
		schematicElement.colorButton.setBackground(createColorFromString("#FFFF00"));
		schematicElement.shape.setSelectedItem(mxConstants.SHAPE_SWIMLANE);

		schematicElement = schematicElements.get("Constraint");
		schematicElement.colorButton.setForeground(createColorFromString("#FF0000"));
		schematicElement.colorButton.setBackground(createColorFromString("#FF0000"));
		schematicElement.shape.setSelectedItem(mxConstants.SHAPE_HEXAGON);

		schematicElement = schematicElements.get("Variable");
		schematicElement.colorButton.setForeground(createColorFromString("#0000FF"));
		schematicElement.colorButton.setBackground(createColorFromString("#0000FF"));

		//schematicElement = schematicElements.get("Component");

		schematicElement = schematicElements.get("subComponent");
		schematicElement.colorButton.setForeground(createColorFromString("#87F274"));
		schematicElement.colorButton.setBackground(createColorFromString("#87F274"));

		//schematicElement = schematicElements.get("Transition");

		schematicElement = schematicElements.get("Boolean_True");
		schematicElement.colorButton.setForeground(createColorFromString("#808080"));
		schematicElement.colorButton.setBackground(createColorFromString("#808080"));

		//schematicElement = schematicElements.get("Boolean_False");

		schematicElement = schematicElements.get("Place_NotMarked");
		schematicElement.shape.setSelectedItem(mxConstants.SHAPE_ELLIPSE);

		schematicElement = schematicElements.get("Place_Marked");
		schematicElement.colorButton.setForeground(createColorFromString("#808080"));
		schematicElement.colorButton.setBackground(createColorFromString("#808080"));
		schematicElement.shape.setSelectedItem(mxConstants.SHAPE_DOUBLE_ELLIPSE);

		schematicElement = schematicElements.get("Default_Edge");
		schematicElement.strokeButton.setForeground(createColorFromString("#000000"));
		schematicElement.strokeButton.setBackground(createColorFromString("#000000"));
		schematicElement.shape.setSelectedItem(mxConstants.ARROW_OPEN);
		schematicElement.opacity.setText("100");
	}
	
	private JPanel schematicPreferences(Preferences biosimrc) {
		setDefaultSchematicPreferences();
		JPanel labels;
		JPanel shapes;
		JPanel colors;
		if (async) {
			labels = new JPanel(new GridLayout(11, 1));
			shapes = new JPanel(new GridLayout(11, 1));
			colors = new JPanel(new GridLayout(11, 1));
		} else {
			labels = new JPanel(new GridLayout(23, 1));
			shapes = new JPanel(new GridLayout(23, 1));
			colors = new JPanel(new GridLayout(23, 1));
		}
		
		labels.add(new JLabel("Element"));
		shapes.add(new JLabel("Shape",SwingConstants.CENTER));
		JPanel colorButtons = new JPanel(new GridLayout(1, 4));
		colorButtons.add(new JLabel("Fill",SwingConstants.CENTER));
		colorButtons.add(new JLabel("Stroke",SwingConstants.CENTER));
		colorButtons.add(new JLabel("Font",SwingConstants.CENTER));
		colorButtons.add(new JLabel("Opacity",SwingConstants.CENTER));
		colors.add(colorButtons);
		Object[] keys = schematicElements.keySet().toArray();
		Arrays.sort(keys);
		for (Object element:keys) {
			colorButtons = new JPanel(new GridLayout(1, 4));
			labels.add(new JLabel((String) element));
			if (async) {
				if (!biosimrc.get("lema.schematic.shape."+element, "").equals("")) {
					String shape = biosimrc.get("lema.schematic.shape."+element, "");
					if (biosimrc.get("lema.schematic.rounded."+element, "").equals("true")) {
						shape += " (rounded)";
					}
					schematicElements.get(element).shape.setSelectedItem(shape);
				} 
				if (!biosimrc.get("lema.schematic.color."+element, "").equals("")) {
					schematicElements.get(element).colorButton = 
							createColorButton(createColorFromString(biosimrc.get("lema.schematic.color."+element, "")));
				} 
				if (!biosimrc.get("lema.schematic.strokeColor."+element, "").equals("")) {
					schematicElements.get(element).strokeButton = 
							createColorButton(createColorFromString(biosimrc.get("lema.schematic.strokeColor."+element, "")));
				} 
				if (!biosimrc.get("lema.schematic.fontColor."+element, "").equals("")) {
					schematicElements.get(element).fontButton = 
							createColorButton(createColorFromString(biosimrc.get("lema.schematic.fontColor."+element, "")));
				} 
				if (!biosimrc.get("lema.schematic.opacity."+element, "").equals("")) {
					schematicElements.get(element).opacity.setText(biosimrc.get("lema.schematic.opacity."+element, "50"));
				} 
			} else {
				if (!biosimrc.get("biosim.schematic.shape."+element, "").equals("")) {
					String shape = biosimrc.get("biosim.schematic.shape."+element, "");
					if (biosimrc.get("biosim.schematic.rounded."+element, "").equals("true")) {
						shape += " (rounded)";
					}
					schematicElements.get(element).shape.setSelectedItem(shape);
				} 
				if (!biosimrc.get("biosim.schematic.color."+element, "").equals("")) {
					schematicElements.get(element).colorButton = 
							createColorButton(createColorFromString(biosimrc.get("biosim.schematic.color."+element, "")));
				} 
				if (!biosimrc.get("biosim.schematic.strokeColor."+element, "").equals("")) {
					schematicElements.get(element).strokeButton = 
							createColorButton(createColorFromString(biosimrc.get("biosim.schematic.strokeColor."+element, "")));
				} 
				if (!biosimrc.get("biosim.schematic.fontColor."+element, "").equals("")) {
					schematicElements.get(element).fontButton = 
							createColorButton(createColorFromString(biosimrc.get("biosim.schematic.fontColor."+element, "")));
				} 
				if (!biosimrc.get("biosim.schematic.opacity."+element, "").equals("")) {
					schematicElements.get(element).opacity.setText(biosimrc.get("biosim.schematic.opacity."+element, "50"));
				} 
			}
			shapes.add(schematicElements.get(element).shape);
			colorButtons.add(schematicElements.get(element).colorButton);
			colorButtons.add(schematicElements.get(element).strokeButton);
			colorButtons.add(schematicElements.get(element).fontButton);
			colorButtons.add(schematicElements.get(element).opacity);
			colors.add(colorButtons);
		}

		JButton restoreSchematic = new JButton("Restore Defaults");
		restoreSchematic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				restoreDefaultSchematicPreferences();
			}
		});	
		// create model preferences panel
		JPanel schematicPrefs = new JPanel(new GridLayout(1, 3));
		/*
		if (async) {
			modelPrefs.add(Undeclared);
			modelPrefs.add(Units);
		} else {
		*/
		schematicPrefs.add(labels);
		schematicPrefs.add(shapes);
		schematicPrefs.add(colors);
			/*
		}
		*/
		JPanel schematicPrefsFinal = new JPanel(new BorderLayout());
		schematicPrefsFinal.add(schematicPrefs,"North");
		schematicPrefsFinal.add(restoreSchematic,"South");
		return schematicPrefsFinal;
	}

	private JPanel modelPreferences(Preferences biosimrc) {
		// model preferences
		Undeclared = new JCheckBox("Check for undeclared units in SBML");
		if (checkUndeclared) {
			Undeclared.setSelected(true);
		}
		else {
			Undeclared.setSelected(false);
		}
		Units = new JCheckBox("Check units in SBML");
		if (checkUnits) {
			Units.setSelected(true);
		}
		else {
			Units.setSelected(false);
		}
		ACTIVED_VALUE = new JTextField(biosimrc.get("biosim.gcm.ACTIVED_VALUE", ""));
		KACT_VALUE = new JTextField(biosimrc.get("biosim.gcm.KACT_VALUE", ""));
		KBASAL_VALUE = new JTextField(biosimrc.get("biosim.gcm.KBASAL_VALUE", ""));
		KDECAY_VALUE = new JTextField(biosimrc.get("biosim.gcm.KDECAY_VALUE", ""));
		KECDECAY_VALUE = new JTextField(biosimrc.get("biosim.gcm.KECDECAY_VALUE", ""));
		COOPERATIVITY_VALUE = new JTextField(biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", ""));
		RNAP_VALUE = new JTextField(biosimrc.get("biosim.gcm.RNAP_VALUE", ""));
		PROMOTER_COUNT_VALUE = new JTextField(biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", ""));
		OCR_VALUE = new JTextField(biosimrc.get("biosim.gcm.OCR_VALUE", ""));
		RNAP_BINDING_VALUE = new JTextField(biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", ""));
		ACTIVATED_RNAP_BINDING_VALUE = new JTextField(biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ""));
		KREP_VALUE = new JTextField(biosimrc.get("biosim.gcm.KREP_VALUE", ""));
		STOICHIOMETRY_VALUE = new JTextField(biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", ""));
		KCOMPLEX_VALUE = new JTextField(biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", ""));
		FORWARD_MEMDIFF_VALUE = new JTextField(biosimrc.get("biosim.gcm.FORWARD_MEMDIFF_VALUE", ""));
		REVERSE_MEMDIFF_VALUE = new JTextField(biosimrc.get("biosim.gcm.REVERSE_MEMDIFF_VALUE", ""));
		KECDIFF_VALUE = new JTextField(biosimrc.get("biosim.gcm.KECDIFF_VALUE", ""));

		JPanel labels = new JPanel(new GridLayout(18, 1));
		labels.add(Undeclared);
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.ACTIVATED_STRING) + " (" + GlobalConstants.ACTIVATED_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KACT_STRING) + " (" + GlobalConstants.KACT_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KBASAL_STRING) + " (" + GlobalConstants.KBASAL_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KDECAY_STRING) + " (" + GlobalConstants.KDECAY_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KECDECAY_STRING) + " (" + GlobalConstants.KECDECAY_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.COOPERATIVITY_STRING) + " (" + GlobalConstants.COOPERATIVITY_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.RNAP_STRING) + " (" + GlobalConstants.RNAP_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.PROMOTER_COUNT_STRING) + " (" + GlobalConstants.PROMOTER_COUNT_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.OCR_STRING) + " (" + GlobalConstants.OCR_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.RNAP_BINDING_STRING) + " (" + GlobalConstants.RNAP_BINDING_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.ACTIVATED_RNAP_BINDING_STRING) + " ("
				+ GlobalConstants.ACTIVATED_RNAP_BINDING_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KREP_STRING) + " (" + GlobalConstants.KREP_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.STOICHIOMETRY_STRING) + " (" + GlobalConstants.STOICHIOMETRY_STRING
				+ "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KCOMPLEX_STRING) + " (" + GlobalConstants.KCOMPLEX_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.FORWARD_MEMDIFF_STRING) + " (" + GlobalConstants.FORWARD_MEMDIFF_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.REVERSE_MEMDIFF_STRING) + " (" + GlobalConstants.REVERSE_MEMDIFF_STRING + "):"));
		labels.add(new JLabel(CompatibilityFixer.getGuiName(GlobalConstants.KECDIFF_STRING) + " (" + GlobalConstants.KECDIFF_STRING + "):"));

		JPanel fields = new JPanel(new GridLayout(18, 1));
		fields.add(Units);
		fields.add(ACTIVED_VALUE);
		fields.add(KACT_VALUE);
		fields.add(KBASAL_VALUE);
		fields.add(KDECAY_VALUE);
		fields.add(KECDECAY_VALUE);
		fields.add(COOPERATIVITY_VALUE);
		fields.add(RNAP_VALUE);
		fields.add(PROMOTER_COUNT_VALUE);
		fields.add(OCR_VALUE);
		fields.add(RNAP_BINDING_VALUE);
		fields.add(ACTIVATED_RNAP_BINDING_VALUE);
		fields.add(KREP_VALUE);
		fields.add(STOICHIOMETRY_VALUE);
		fields.add(KCOMPLEX_VALUE);
		fields.add(FORWARD_MEMDIFF_VALUE);
		fields.add(REVERSE_MEMDIFF_VALUE);
		fields.add(KECDIFF_VALUE);
		
		JButton restoreModel = new JButton("Restore Defaults");
		restoreModel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Undeclared.setSelected(true);
				Units.setSelected(true);
				KREP_VALUE.setText(".5");
				KACT_VALUE.setText(".0033");
				PROMOTER_COUNT_VALUE.setText("2");
				KBASAL_VALUE.setText(".0001");
				OCR_VALUE.setText(".05");
				KDECAY_VALUE.setText(".0075");
				KECDECAY_VALUE.setText(".005");
				RNAP_VALUE.setText("30");
				RNAP_BINDING_VALUE.setText(".033");
				ACTIVATED_RNAP_BINDING_VALUE.setText("1");
				STOICHIOMETRY_VALUE.setText("10");
				KCOMPLEX_VALUE.setText("0.05");
				COOPERATIVITY_VALUE.setText("2");
				ACTIVED_VALUE.setText(".25");
				FORWARD_MEMDIFF_VALUE.setText("1.0");
				REVERSE_MEMDIFF_VALUE.setText("0.01");
				KECDIFF_VALUE.setText("1.0");
			}
		});	
		// create model preferences panel
		JPanel modelPrefs = new JPanel(new GridLayout(1, 2));
		if (async) {
			modelPrefs.add(Undeclared);
			modelPrefs.add(Units);
		} else {
			modelPrefs.add(labels);
			modelPrefs.add(fields);
		}
		JPanel modelPrefsFinal = new JPanel(new BorderLayout());
		modelPrefsFinal.add(modelPrefs,"North");
		modelPrefsFinal.add(restoreModel,"South");
		return modelPrefsFinal;
	}
	
	public static String getDefaultUriPrefix() {
		Preferences biosimrc = Preferences.userRoot();
		return biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE, GlobalConstants.SBOL_AUTHORITY_DEFAULT);
	}
	
	private JPanel SBOLPreferences(Preferences biosimrc) {	
		// assembly preferences
		JPanel assemblyLabels = new JPanel(new GridLayout(13, 1));
		assemblyLabels.add(new JLabel("Namespace"));
		assemblyLabels.add(new JLabel("Assemble Complete Genetic Construct"));
		assemblyLabels.add(new JLabel("Regex for Complete Genetic Construct"));
		assemblyLabels.add(new JLabel("Validate Assembled Constructs"));
		assemblyLabels.add(new JLabel("Incomplete Construct Warning"));
		
		JPanel assemblyFields = new JPanel(new GridLayout(13 ,1));
		uriField = new JTextField(EditPreferences.getDefaultUriPrefix(),15);
		String regex = SBOLUtility.convertRegexSOTermsToNumbers(
				biosimrc.get(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, ""));
		regexField = new JTextField(regex, 15);
		assemblyBox = new JComboBox(new String[]{"True", "False"});
		assemblyBox.setSelectedItem(biosimrc.get(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, ""));
		assemblyBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (assemblyBox.getSelectedIndex() == 0) {
					regexField.setEnabled(true);
					validationBox.setSelectedIndex(0);
					validationBox.setEnabled(true);
					warningBox.setSelectedIndex(0);
					warningBox.setEnabled(true);
				} else {
					regexField.setEnabled(false);
					validationBox.setSelectedIndex(1);
					validationBox.setEnabled(false);
					warningBox.setSelectedIndex(1);
					warningBox.setEnabled(false);
				}
			}
		});
		validationBox = new JComboBox(new String[]{"True", "False"});
		validationBox.setSelectedItem(biosimrc.get(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, ""));
		validationBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (validationBox.getSelectedIndex() == 0) {
					warningBox.setSelectedIndex(0);
					warningBox.setEnabled(true);
					warningBox.setSelectedIndex(1);
				} else {
					warningBox.setSelectedIndex(1);
					warningBox.setSelectedIndex(1);
					warningBox.setEnabled(false);
				}
			}
		});
		warningBox = new JComboBox(new String[]{"True", "False"});
		warningBox.setSelectedItem(biosimrc.get(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, ""));
		assemblyFields.add(uriField);
		assemblyFields.add(assemblyBox);
		assemblyFields.add(regexField);
		assemblyFields.add(validationBox);
		assemblyFields.add(warningBox);
		
		JButton restoreDefaultsButton = new JButton("Restore Defaults");
		restoreDefaultsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				uriField.setText(GlobalConstants.SBOL_AUTHORITY_DEFAULT);
				regexField.setText(GlobalConstants.GENETIC_CONSTRUCT_REGEX_DEFAULT);
				assemblyBox.setSelectedItem(GlobalConstants.CONSTRUCT_ASSEMBLY_DEFAULT);
				validationBox.setSelectedItem(GlobalConstants.CONSTRUCT_VALIDATION_DEFAULT);
				warningBox.setSelectedItem(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_DEFAULT);
			}
		});	
		
		// create assembly preferences panel
		JPanel assemblyPrefsPane = new JPanel(new GridLayout(1, 2));
		assemblyPrefsPane.add(assemblyLabels);
		assemblyPrefsPane.add(assemblyFields);
		JPanel assemblyPrefsTop = new JPanel(new BorderLayout());
		assemblyPrefsTop.add(assemblyPrefsPane,"North");
		assemblyPrefsTop.add(restoreDefaultsButton,"South");
		return assemblyPrefsTop;
	}

	private JPanel analysisPreferences(Preferences biosimrc) {	
		// analysis preferences
		String[] choices = { "None", "Expand Reactions", "Reaction-based", "State-based" };
		simCommand = new JTextField(biosimrc.get("biosim.sim.command", ""));
		abs = new JComboBox(choices);
		abs.setSelectedItem(biosimrc.get("biosim.sim.abs", ""));

		if (!abs.getSelectedItem().equals("State-based")) {
			choices = new String[] { "ODE", "Monte Carlo", "SBML", "Network", "Browser" };
		}
		else {
			choices = new String[] { "Monte Carlo", "Markov", "SBML", "Network", "Browser", "LPN" };
		}

		type = new JComboBox(choices);
		type.setSelectedItem(biosimrc.get("biosim.sim.type", ""));

		if (type.getSelectedItem().equals("ODE")) {
			choices = new String[] { "euler", "gear1", "gear2", "rk4imp", "rk8pd", "rkf45", "Runge-Kutta-Fehlberg" };
		}
		else if (type.getSelectedItem().equals("Monte Carlo")) {
			choices = new String[] { "gillespie", "SSA-Hierarchical", "SSA-Direct", "SSA-CR", "iSSA", "interactive", "emc-sim", "bunker", "nmc"};
		}
		else if (type.getSelectedItem().equals("Markov")) {
			choices = new String[] { "steady-state-markov-chain-analysis", "transient-markov-chain-analysis", "reachability-analysis", "prism", "atacs",
					"ctmc-transient" };
		}
		else {
			choices = new String[] { "euler", "gear1", "gear2", "rk4imp", "rk8pd", "rkf45" };
		}

		sim = new JComboBox(choices);
		sim.setSelectedItem(biosimrc.get("biosim.sim.sim", ""));
		abs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!abs.getSelectedItem().equals("State-based")) {
					Object o = type.getSelectedItem();
					type.removeAllItems();
					type.addItem("ODE");
					type.addItem("Monte Carlo");
					type.addItem("Model");
					type.addItem("Network");
					type.addItem("Browser");
					type.setSelectedItem(o);
				}
				else {
					Object o = type.getSelectedItem();
					type.removeAllItems();
					type.addItem("Monte Carlo");
					type.addItem("Markov");
					type.addItem("Model");
					type.addItem("Network");
					type.addItem("Browser");
					type.addItem("LPN");
					type.setSelectedItem(o);
				}
			}
		});

		type.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (type.getSelectedItem() == null) {
				}
				else if (type.getSelectedItem().equals("ODE")) {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("euler");
					sim.addItem("gear1");
					sim.addItem("gear2");
					sim.addItem("rk4imp");
					sim.addItem("rk8pd");
					sim.addItem("rkf45");
					sim.addItem("Runge-Kutta-Fehlberg");
					sim.setSelectedIndex(5);
					sim.setSelectedItem(o);
				}
				else if (type.getSelectedItem().equals("Monte Carlo")) {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("gillespie");
					sim.addItem("SSA-Hierarchical");
					sim.addItem("SSA-Direct");
					sim.addItem("SSA-CR");
					sim.addItem("interactive");
					sim.addItem("iSSA");
					sim.addItem("emc-sim");
					sim.addItem("bunker");
					sim.addItem("nmc");
					sim.setSelectedItem(o);
				}
				else if (type.getSelectedItem().equals("Markov")) {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("steady-state-markov-chain-analysis");
					sim.addItem("transient-markov-chain-analysis");
					sim.addItem("reachability-analysis");
					sim.addItem("prism");
					sim.addItem("atacs");
					sim.addItem("ctmc-transient");
					sim.setSelectedItem(o);
				}
				else {
					Object o = sim.getSelectedItem();
					sim.removeAllItems();
					sim.addItem("euler");
					sim.addItem("gear1");
					sim.addItem("gear2");
					sim.addItem("rk4imp");
					sim.addItem("rk8pd");
					sim.addItem("rkf45");
					sim.setSelectedIndex(5);
					sim.setSelectedItem(o);
				}
			}
		});

		initialTime = new JTextField(biosimrc.get("biosim.sim.initial.time", ""));
		outputStartTime = new JTextField(biosimrc.get("biosim.sim.output.start.time", ""));
		limit = new JTextField(biosimrc.get("biosim.sim.limit", ""));
		interval = new JTextField(biosimrc.get("biosim.sim.interval", ""));
		minStep = new JTextField(biosimrc.get("biosim.sim.min.step", ""));
		step = new JTextField(biosimrc.get("biosim.sim.step", ""));
		error = new JTextField(biosimrc.get("biosim.sim.error", ""));
		relError = new JTextField(biosimrc.get("biosim.sim.relative.error", ""));
		seed = new JTextField(biosimrc.get("biosim.sim.seed", ""));
		runs = new JTextField(biosimrc.get("biosim.sim.runs", ""));
		rapid1 = new JTextField(biosimrc.get("biosim.sim.rapid1", ""));
		rapid2 = new JTextField(biosimrc.get("biosim.sim.rapid2", ""));
		qssa = new JTextField(biosimrc.get("biosim.sim.qssa", ""));
		concentration = new JTextField(biosimrc.get("biosim.sim.concentration", ""));
		
		choices = new String[] { "Print Interval", "Minimum Print Interval", "Number Of Steps" };
		useInterval = new JComboBox(choices);
		useInterval.setSelectedItem(biosimrc.get("biosim.sim.useInterval", ""));

		JPanel analysisLabels = new JPanel(new GridLayout(18, 1));
		analysisLabels.add(new JLabel("Simulation Command:"));
		analysisLabels.add(new JLabel("Abstraction:"));
		analysisLabels.add(new JLabel("Simulation Type:"));
		analysisLabels.add(new JLabel("Possible Simulators/Analyzers:"));
		analysisLabels.add(new JLabel("Initial Time:"));
		analysisLabels.add(new JLabel("Output Start Time:"));
		analysisLabels.add(new JLabel("Time Limit:"));
		analysisLabels.add(useInterval);
		analysisLabels.add(new JLabel("Minimum Time Step:"));
		analysisLabels.add(new JLabel("Maximum Time Step:"));
		analysisLabels.add(new JLabel("Absolute Error:"));
		analysisLabels.add(new JLabel("Relative Error:"));
		analysisLabels.add(new JLabel("Random Seed:"));
		analysisLabels.add(new JLabel("Runs:"));
		analysisLabels.add(new JLabel("Rapid Equilibrium Condition 1:"));
		analysisLabels.add(new JLabel("Rapid Equilibrium Condition 2:"));
		analysisLabels.add(new JLabel("QSSA Condition:"));
		analysisLabels.add(new JLabel("Max Concentration Threshold:"));

		JPanel analysisFields = new JPanel(new GridLayout(18, 1));
		analysisFields.add(simCommand);
		analysisFields.add(abs);
		analysisFields.add(type);
		analysisFields.add(sim);
		analysisFields.add(initialTime);
		analysisFields.add(outputStartTime);
		analysisFields.add(limit);
		analysisFields.add(interval);
		analysisFields.add(minStep);
		analysisFields.add(step);
		analysisFields.add(error);
		analysisFields.add(relError);
		analysisFields.add(seed);
		analysisFields.add(runs);
		analysisFields.add(rapid1);
		analysisFields.add(rapid2);
		analysisFields.add(qssa);
		analysisFields.add(concentration);
		
		JButton restoreAn = new JButton("Restore Defaults");
		restoreAn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				simCommand.setText("");
				abs.setSelectedItem("None");
				type.setSelectedItem("ODE");
				sim.setSelectedItem("rkf45");
				initialTime.setText("0.0");
				outputStartTime.setText("0.0");
				limit.setText("100.0");
				useInterval.setSelectedItem("Print Interval");
				interval.setText("1.0");
				step.setText("inf");
				minStep.setText("0");
				error.setText("1.0E-9");
				relError.setText("0.0");
				seed.setText("314159");
				runs.setText("1");
				rapid1.setText("0.1");
				rapid2.setText("0.1");
				qssa.setText("0.1");
				concentration.setText("15");
			}
		});	

		// create analysis preferences panel
		JPanel analysisPrefs = new JPanel(new GridLayout(1, 2));
		analysisPrefs.add(analysisLabels);
		analysisPrefs.add(analysisFields);
		JPanel analysisPrefsFinal = new JPanel(new BorderLayout());
		analysisPrefsFinal.add(analysisPrefs,"North");
		analysisPrefsFinal.add(restoreAn,"South");
		return analysisPrefsFinal;
	}
	
	private JPanel learnPreferences(Preferences biosimrc) {
		// learning preferences
		tn = new JTextField(biosimrc.get("biosim.learn.tn", ""));
		tj = new JTextField(biosimrc.get("biosim.learn.tj", ""));
		ti = new JTextField(biosimrc.get("biosim.learn.ti", ""));
		String[] choices = new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		bins = new JComboBox(choices);
		bins.setSelectedItem(biosimrc.get("biosim.learn.bins", ""));
		choices = new String[] { "Equal Data Per Bins", "Equal Spacing Of Bins" };
		equaldata = new JComboBox(choices);
		equaldata.setSelectedItem(biosimrc.get("biosim.learn.equaldata", ""));
		choices = new String[] { "Auto", "User" };
		autolevels = new JComboBox(choices);
		autolevels.setSelectedItem(biosimrc.get("biosim.learn.autolevels", ""));
		ta = new JTextField(biosimrc.get("biosim.learn.ta", ""));
		tr = new JTextField(biosimrc.get("biosim.learn.tr", ""));
		tm = new JTextField(biosimrc.get("biosim.learn.tm", ""));
		tt = new JTextField(biosimrc.get("biosim.learn.tt", ""));
		choices = new String[] { "0", "1", "2", "3" };
		debug = new JComboBox(choices);
		debug.setSelectedItem(biosimrc.get("biosim.learn.debug", ""));
		choices = new String[] { "Successors", "Predecessors", "Both" };
		succpred = new JComboBox(choices);
		succpred.setSelectedItem(biosimrc.get("biosim.learn.succpred", ""));
		choices = new String[] { "True", "False" };
		findbaseprob = new JComboBox(choices);
		findbaseprob.setSelectedItem(biosimrc.get("biosim.learn.findbaseprob", ""));

		JPanel learnLabels = new JPanel(new GridLayout(13, 1));
		learnLabels.add(new JLabel("Minimum Number Of Initial Vectors (Tn):"));
		learnLabels.add(new JLabel("Maximum Influence Vector Size (Tj):"));
		learnLabels.add(new JLabel("Score For Empty Influence Vector (Ti):"));
		learnLabels.add(new JLabel("Number Of Bins:"));
		learnLabels.add(new JLabel("Divide Bins:"));
		learnLabels.add(new JLabel("Generate Levels:"));
		learnLabels.add(new JLabel("Ratio For Activation (Ta):"));
		learnLabels.add(new JLabel("Ratio For Repression (Tr):"));
		learnLabels.add(new JLabel("Merge Influence Vectors Delta (Tm):"));
		learnLabels.add(new JLabel("Relax Thresholds Delta (Tt):"));
		learnLabels.add(new JLabel("Debug Level:"));
		learnLabels.add(new JLabel("Successors Or Predecessors:"));
		learnLabels.add(new JLabel("Basic FindBaseProb:"));

		JPanel learnFields = new JPanel(new GridLayout(13, 1));
		learnFields.add(tn);
		learnFields.add(tj);
		learnFields.add(ti);
		learnFields.add(bins);
		learnFields.add(equaldata);
		learnFields.add(autolevels);
		learnFields.add(ta);
		learnFields.add(tr);
		learnFields.add(tm);
		learnFields.add(tt);
		learnFields.add(debug);
		learnFields.add(succpred);
		learnFields.add(findbaseprob);
		
		JButton restoreLearn = new JButton("Restore Defaults");
		restoreLearn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tn.setText("2");
				tj.setText("2");
				ti.setText("0.5");
				bins.setSelectedItem("4");
				equaldata.setSelectedItem("Equal Data Per Bins");
				autolevels.setSelectedItem("Auto");
				ta.setText("1.15");
				tr.setText("0.75");
				tm.setText("0.0");
				tt.setText("0.025");
				debug.setSelectedItem("0");
				succpred.setSelectedItem("Successors");
				findbaseprob.setSelectedItem("False");
			}
		});	
		
		// create learning preferences panel
		JPanel learnPrefs = new JPanel(new GridLayout(1, 2));
		learnPrefs.add(learnLabels);
		learnPrefs.add(learnFields);
		JPanel learnPrefsFinal = new JPanel(new BorderLayout());
		learnPrefsFinal.add(learnPrefs,"North");
		learnPrefsFinal.add(restoreLearn,"South");
		return learnPrefsFinal;
	}
	
	private void saveGeneralPreferences (Preferences biosimrc) {
		if (dialog.isSelected()) {
			biosimrc.put("biosim.general.file_browser", "FileDialog");
		} else {
			biosimrc.put("biosim.general.file_browser", "JFileChooser");
		}
		if (icons.isSelected()) {
			biosimrc.put("biosim.general.tree_icons", "plus_minus");
			tree.setExpandibleIcons(false);
		} else {
			biosimrc.put("biosim.general.tree_icons", "default");
			tree.setExpandibleIcons(true);
		}
		if (delete.isSelected()) {
			biosimrc.put("biosim.general.delete", "confirm");
		} else {
			biosimrc.put("biosim.general.delete", "noconfirm");
		}
		if (libsbmlFlatten.isSelected()) {
			biosimrc.put("biosim.general.flatten", "libsbml");
		} else {
			biosimrc.put("biosim.general.flatten", "default");
		}
		if (libsbmlValidate.isSelected()) {
			biosimrc.put("biosim.general.validate", "libsbml");
		} else {
			biosimrc.put("biosim.general.validate", "default");
		}
		if (showWarnings.isSelected()) {
			biosimrc.put("biosim.general.warnings", "true");
		} else {
			biosimrc.put("biosim.general.warnings", "false");
		}
		if (infix.isSelected()) {
			biosimrc.put("biosim.general.infix", "infix");
		} else {
			biosimrc.put("biosim.general.infix", "prefix");
		}			
		//SBMLLevelVersion = "L3V1";
		//SBML_LEVEL = 3;
		//SBML_VERSION = 1;
		biosimrc.put("biosim.general.browser", xhtmlCmd.getText().trim());
		biosimrc.put("biosim.general.graphviz", dotCmd.getText().trim());
		biosimrc.put("biosim.general.prism", prismCmd.getText().trim());
		biosimrc.put("biosim.sbml.level_version", "L3V1");
		biosimrc.put("lema.verification.command", verCmd.getText().trim());
		biosimrc.put("lema.general.viewer", viewerField.getText().trim());
	}
	
	private static String colorToString(Color color) {
		String red = Integer.toString(color.getRed(),16);
		if (color.getRed() < 16) red = "0" + red;
		String green = Integer.toString(color.getGreen(),16);
		if (color.getGreen() < 16) green = "0" + green;
		String blue = Integer.toString(color.getBlue(),16);
		if (color.getBlue() < 16) blue = "0" + blue;
		return "#" + red + green + blue;
	}
	
	private boolean saveSchematicPreferences(Preferences biosimrc) {
		for (String element:schematicElements.keySet()) {
			SchematicElement schematicElement = schematicElements.get(element);
			String rounded = "false";
			String shape = (String)schematicElement.shape.getSelectedItem();
			if (((String)schematicElement.shape.getSelectedItem()).equals(mxConstants.SHAPE_RECTANGLE+" (rounded)")) {
				rounded = "true";
				shape = mxConstants.SHAPE_RECTANGLE;
			}
			if (async) {
				biosimrc.put("lema.schematic.color."+element, colorToString(schematicElement.colorButton.getForeground()));
				biosimrc.put("lema.schematic.strokeColor."+element, colorToString(schematicElement.strokeButton.getForeground()));
				biosimrc.put("lema.schematic.fontColor."+element, colorToString(schematicElement.fontButton.getForeground()));
				biosimrc.put("lema.schematic.opacity."+element, schematicElement.opacity.getText());
				biosimrc.put("lema.schematic.shape."+element, shape);
				biosimrc.put("lema.schematic.rounded."+element, rounded);
			} else {
				biosimrc.put("biosim.schematic.color."+element, colorToString(schematicElement.colorButton.getForeground()));
				biosimrc.put("biosim.schematic.strokeColor."+element, colorToString(schematicElement.strokeButton.getForeground()));
				biosimrc.put("biosim.schematic.fontColor."+element, colorToString(schematicElement.fontButton.getForeground()));
				biosimrc.put("biosim.schematic.opacity."+element, schematicElement.opacity.getText());
				biosimrc.put("biosim.schematic.shape."+element, shape);
				biosimrc.put("biosim.schematic.rounded."+element, rounded);
			}
		}	
		return false;
	}

	private boolean saveModelPreferences(Preferences biosimrc) {
		boolean problem = false;
		if (Undeclared.isSelected()) {
			checkUndeclared = true;
			biosimrc.put("biosim.check.undeclared", "true");
		}
		else {
			checkUndeclared = false;
			biosimrc.put("biosim.check.undeclared", "false");
		}
		if (Units.isSelected()) {
			checkUnits = true;
			biosimrc.put("biosim.check.units", "true");
		}
		else {
			checkUnits = false;
			biosimrc.put("biosim.check.units", "false");
		}
		try {
			Double.parseDouble(KREP_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.KREP_VALUE", KREP_VALUE.getText().trim());
			Double.parseDouble(KACT_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.KACT_VALUE", KACT_VALUE.getText().trim());
			Double.parseDouble(PROMOTER_COUNT_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.PROMOTER_COUNT_VALUE", PROMOTER_COUNT_VALUE.getText().trim());
			Double.parseDouble(KBASAL_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.KBASAL_VALUE", KBASAL_VALUE.getText().trim());
			Double.parseDouble(OCR_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.OCR_VALUE", OCR_VALUE.getText().trim());
			Double.parseDouble(KDECAY_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.KDECAY_VALUE", KDECAY_VALUE.getText().trim());
			Double.parseDouble(KECDECAY_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.KECDECAY_VALUE", KECDECAY_VALUE.getText().trim());
			Double.parseDouble(RNAP_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.RNAP_VALUE", RNAP_VALUE.getText().trim());
			Double.parseDouble(RNAP_BINDING_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.RNAP_BINDING_VALUE", RNAP_BINDING_VALUE.getText().trim());
			Double.parseDouble(ACTIVATED_RNAP_BINDING_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", ACTIVATED_RNAP_BINDING_VALUE.getText().trim());
			Double.parseDouble(STOICHIOMETRY_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.STOICHIOMETRY_VALUE", STOICHIOMETRY_VALUE.getText().trim());
			Double.parseDouble(KCOMPLEX_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.KCOMPLEX_VALUE", KCOMPLEX_VALUE.getText().trim());
			Double.parseDouble(COOPERATIVITY_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.COOPERATIVITY_VALUE", COOPERATIVITY_VALUE.getText().trim());
			Double.parseDouble(ACTIVED_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.ACTIVED_VALUE", ACTIVED_VALUE.getText().trim());
			String[] fdrv = FORWARD_MEMDIFF_VALUE.getText().trim().split("/");
			// if the user specifies a forward and reverse rate
			if (fdrv.length == 2) {
				biosimrc.put("biosim.gcm.FORWARD_MEMDIFF_VALUE", fdrv[0]);
			}
			else if (fdrv.length == 1) {
				Double.parseDouble(FORWARD_MEMDIFF_VALUE.getText().trim());
				biosimrc.put("biosim.gcm.FORWARD_MEMDIFF_VALUE", FORWARD_MEMDIFF_VALUE.getText().trim());
			}
			fdrv = REVERSE_MEMDIFF_VALUE.getText().trim().split("/");
			// if the user specifies a forward and reverse rate
			if (fdrv.length == 2) {
				biosimrc.put("biosim.gcm.MEMDIFF_VALUE", fdrv[1]);
			}
			else if (fdrv.length == 1) {
				Double.parseDouble(REVERSE_MEMDIFF_VALUE.getText().trim());
				biosimrc.put("biosim.gcm.MEMDIFF_VALUE", REVERSE_MEMDIFF_VALUE.getText().trim());
			}
			Double.parseDouble(KECDIFF_VALUE.getText().trim());
			biosimrc.put("biosim.gcm.KECDIFF_VALUE", KECDIFF_VALUE.getText().trim());
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Numeric model preference given non-numeric value.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
			problem = true;		
		}
		return problem;
	}
	
	private boolean saveAnalysisPreferences(Preferences biosimrc) {
		boolean problem = false;
		biosimrc.put("biosim.sim.command", simCommand.getText().trim());
		biosimrc.put("biosim.sim.useInterval", (String) useInterval.getSelectedItem());
		biosimrc.put("biosim.sim.abs", (String) abs.getSelectedItem());
		biosimrc.put("biosim.sim.type", (String) type.getSelectedItem());
		biosimrc.put("biosim.sim.sim", (String) sim.getSelectedItem());
		try {
			Double.parseDouble(initialTime.getText().trim());
			biosimrc.put("biosim.sim.initial.time", initialTime.getText().trim());
			Double.parseDouble(outputStartTime.getText().trim());
			biosimrc.put("biosim.sim.output.start.time", outputStartTime.getText().trim());
			Double.parseDouble(limit.getText().trim());
			biosimrc.put("biosim.sim.limit", limit.getText().trim());
			Double.parseDouble(interval.getText().trim());
			biosimrc.put("biosim.sim.interval", interval.getText().trim());
			if (step.getText().trim().equals("inf")) {
				biosimrc.put("biosim.sim.step", step.getText().trim());
			}
			else {
				Double.parseDouble(step.getText().trim());
				biosimrc.put("biosim.sim.step", step.getText().trim());
			}
			Double.parseDouble(minStep.getText().trim());
			biosimrc.put("biosim.min.sim.step", minStep.getText().trim());
			Double.parseDouble(error.getText().trim());
			biosimrc.put("biosim.sim.error", error.getText().trim());
			Double.parseDouble(relError.getText().trim());
			biosimrc.put("biosim.sim.relative.error", relError.getText().trim());
			Long.parseLong(seed.getText().trim());
			biosimrc.put("biosim.sim.seed", seed.getText().trim());
			Integer.parseInt(runs.getText().trim());
			biosimrc.put("biosim.sim.runs", runs.getText().trim());
			Double.parseDouble(rapid1.getText().trim());
			biosimrc.put("biosim.sim.rapid1", rapid1.getText().trim());
			Double.parseDouble(rapid2.getText().trim());
			biosimrc.put("biosim.sim.rapid2", rapid2.getText().trim());
			Double.parseDouble(qssa.getText().trim());
			biosimrc.put("biosim.sim.qssa", qssa.getText().trim());
			Integer.parseInt(concentration.getText().trim());
			biosimrc.put("biosim.sim.concentration", concentration.getText().trim());
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Numeric analysis preference given non-numeric value.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
			problem = true;		
		}
		return problem;
	}
	
	private boolean saveLearnPreferences(Preferences biosimrc) {
		boolean problem = false;
		biosimrc.put("biosim.learn.bins", (String) bins.getSelectedItem());
		biosimrc.put("biosim.learn.equaldata", (String) equaldata.getSelectedItem());
		biosimrc.put("biosim.learn.autolevels", (String) autolevels.getSelectedItem());
		biosimrc.put("biosim.learn.debug", (String) debug.getSelectedItem());
		biosimrc.put("biosim.learn.succpred", (String) succpred.getSelectedItem());
		biosimrc.put("biosim.learn.findbaseprob", (String) findbaseprob.getSelectedItem());
		try {
			Integer.parseInt(tn.getText().trim());
			biosimrc.put("biosim.learn.tn", tn.getText().trim());
			Integer.parseInt(tj.getText().trim());
			biosimrc.put("biosim.learn.tj", tj.getText().trim());
			Double.parseDouble(ti.getText().trim());
			biosimrc.put("biosim.learn.ti", ti.getText().trim());
			Double.parseDouble(ta.getText().trim());
			biosimrc.put("biosim.learn.ta", ta.getText().trim());
			Double.parseDouble(tr.getText().trim());
			biosimrc.put("biosim.learn.tr", tr.getText().trim());
			Double.parseDouble(tm.getText().trim());
			biosimrc.put("biosim.learn.tm", tm.getText().trim());
			Double.parseDouble(tt.getText().trim());
			biosimrc.put("biosim.learn.tt", tt.getText().trim());
		}
		catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Numeric learn preference given non-numeric value.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
			problem = true;		
		}
		return problem;
	}
	
	private boolean saveSBOLPreferences(Preferences biosimrc) {
		boolean problem = false;
		if (!uriField.getText().trim().equals("")) {
			biosimrc.put(GlobalConstants.SBOL_AUTHORITY_PREFERENCE, uriField.getText().trim());
		} else {
			JOptionPane.showMessageDialog(Gui.frame, "URI authority cannot be blank.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
			uriField.setText(biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE, ""));
			problem = true;
		}
		if (!regexField.getText().trim().equals(""))
			biosimrc.put(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, regexField.getText().trim());
		else {
			JOptionPane.showMessageDialog(Gui.frame, "Validation regex cannot be blank.", 
					"Invalid Preference", JOptionPane.ERROR_MESSAGE);
			regexField.setText(biosimrc.get(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, ""));
			problem = true;
		}
		if (assemblyBox.getSelectedItem().equals("True"))
			biosimrc.put(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, "True");
		else
			biosimrc.put(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, "False");
		if (validationBox.getSelectedItem().equals("True"))
			biosimrc.put(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "True");
		else
			biosimrc.put(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "False");
		if (warningBox.getSelectedItem().equals("True"))
			biosimrc.put(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, "True");
		else
			biosimrc.put(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, "False");
		return problem;
	}
	
	// builds the edit>preferences panel
	public void preferences() {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.check.undeclared", "").equals("false")) {
			checkUndeclared = false;
		}
		else {
			checkUndeclared = true;
		}
		if (biosimrc.get("biosim.check.units", "").equals("false")) {
			checkUnits = false;
		}
		else {
			checkUnits = true;
		}
		JPanel generalPrefs = generalPreferences(biosimrc);
		JPanel schematicPrefs = schematicPreferences(biosimrc);
		JPanel modelPrefs = modelPreferences(biosimrc);
		JPanel assemblyPrefs = SBOLPreferences(biosimrc);
		JPanel analysisPrefs = analysisPreferences(biosimrc);
		JPanel learnPrefs = learnPreferences(biosimrc);

		// create tabs
		JTabbedPane prefTabs = new JTabbedPane();
		prefTabs.addTab("General Preferences", generalPrefs);
		prefTabs.addTab("Schematic Preferences", schematicPrefs);
		prefTabs.addTab("Model Preferences", modelPrefs);
		prefTabs.addTab("Analysis Preferences", analysisPrefs);
		if (!async) prefTabs.addTab("SBOL Preferences", assemblyPrefs);
		if (!async) prefTabs.addTab("Learn Preferences", learnPrefs);

		boolean problem;
		int value;
		do {
			problem = false;
			Object[] options = { "Save", "Cancel" };
			value = JOptionPane.showOptionDialog(frame, prefTabs, "Preferences", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
					options, options[0]);

			// if user hits "save", store and/or check new data
			if (value == JOptionPane.YES_OPTION) {
				saveGeneralPreferences(biosimrc);
				problem = saveSchematicPreferences(biosimrc);
				if (!problem) problem = saveModelPreferences(biosimrc);
				if (!problem) problem = saveAnalysisPreferences(biosimrc);
				if (!problem) problem = saveLearnPreferences(biosimrc);
				if (!problem) problem = saveSBOLPreferences(biosimrc);
				try {
					biosimrc.sync();
				}
				catch (BackingStoreException e) {
					e.printStackTrace();
				}
			}
		} while (value == JOptionPane.YES_OPTION && problem);
	}
	
	public void setDefaultPreferences() {
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.file_browser", "").equals("")) {
			biosimrc.put("biosim.general.file_browser", "JFileChooser");
		}
		if (biosimrc.get("biosim.general.flatten", "").equals("")) {
			biosimrc.put("biosim.general.flatten", "default");
		}
		if (biosimrc.get("biosim.general.validate", "").equals("")) {
			biosimrc.put("biosim.general.validate", "default");
		}
		if (biosimrc.get("biosim.general.warnings", "").equals("")) {
			biosimrc.put("biosim.general.warnings", "false");
		}
		if (biosimrc.get("biosim.general.browser", "").equals("")) {
			if (System.getProperty("os.name").contentEquals("Linux")) {
				biosimrc.put("biosim.general.browser","xdg-open");
			} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
				biosimrc.put("biosim.general.browser","open");
			} else {
				biosimrc.put("biosim.general.browser","cmd /c start");
			}
		}
		if (biosimrc.get("biosim.general.graphviz", "").equals("")) {
			if (System.getProperty("os.name").contentEquals("Linux")) {
				biosimrc.put("biosim.general.graphviz","xdg-open");
			} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
				biosimrc.put("biosim.general.graphviz","open");
			} else {
				biosimrc.put("biosim.general.graphviz","dotty");
			}
		}
		if (biosimrc.get("biosim.general.prism", "").equals("")) {
			biosimrc.put("biosim.general.prism","prism");
		}
		if (biosimrc.get("biosim.general.infix", "").equals("")) {
			biosimrc.put("biosim.general.infix", "infix");
		}
		if (biosimrc.get("biosim.gcm.KREP_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KREP_VALUE", ".5");
		}
		if (biosimrc.get("biosim.gcm.KACT_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KACT_VALUE", ".0033");
		}
		if (biosimrc.get("biosim.gcm.PROMOTER_COUNT_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.PROMOTER_COUNT_VALUE", "2");
		}
		if (biosimrc.get("biosim.gcm.KBASAL_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KBASAL_VALUE", ".0001");
		}
		if (biosimrc.get("biosim.gcm.OCR_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.OCR_VALUE", ".05");
		}
		if (biosimrc.get("biosim.gcm.KDECAY_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KDECAY_VALUE", ".0075");
		}
		if (biosimrc.get("biosim.gcm.KECDECAY_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KECDECAY_VALUE", ".005");
		}
		if (biosimrc.get("biosim.gcm.RNAP_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.RNAP_VALUE", "30");
		}
		if (biosimrc.get("biosim.gcm.RNAP_BINDING_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.RNAP_BINDING_VALUE", ".033");
		}
		if (biosimrc.get("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.ACTIVATED_RNAP_BINDING_VALUE", "1");
		}
		if (biosimrc.get("biosim.gcm.STOICHIOMETRY_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.STOICHIOMETRY_VALUE", "10");
		}
		if (biosimrc.get("biosim.gcm.KCOMPLEX_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KCOMPLEX_VALUE", "0.05");
		}
		if (biosimrc.get("biosim.gcm.COOPERATIVITY_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.COOPERATIVITY_VALUE", "2");
		}
		if (biosimrc.get("biosim.gcm.ACTIVED_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.ACTIVED_VALUE", ".25");
		}
		if (biosimrc.get("biosim.gcm.FORWARD_MEMDIFF_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.FORWARD_MEMDIFF_VALUE", "1.0");
		}
		if (biosimrc.get("biosim.gcm.REVERSE_MEMDIFF_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.REVERSE_MEMDIFF_VALUE", "0.01");
		}
		if (biosimrc.get("biosim.gcm.KECDIFF_VALUE", "").equals("")) {
			biosimrc.put("biosim.gcm.KECDIFF_VALUE", "1.0");
		}
		if (biosimrc.get("biosim.sim.abs", "").equals("")) {
			biosimrc.put("biosim.sim.abs", "None");
		}
		if (biosimrc.get("biosim.sim.type", "").equals("")) {
			biosimrc.put("biosim.sim.type", "ODE");
		}
		if (biosimrc.get("biosim.sim.sim", "").equals("")) {
			biosimrc.put("biosim.sim.sim", "rkf45");
		}
		if (biosimrc.get("biosim.sim.initial.time", "").equals("")) {
			biosimrc.put("biosim.sim.initial.time", "0.0");
		}
		if (biosimrc.get("biosim.sim.output.start.time", "").equals("")) {
			biosimrc.put("biosim.sim.output.start.time", "0.0");
		}
		if (biosimrc.get("biosim.sim.limit", "").equals("")) {
			biosimrc.put("biosim.sim.limit", "100.0");
		}
		if (biosimrc.get("biosim.sim.useInterval", "").equals("")) {
			biosimrc.put("biosim.sim.useInterval", "Print Interval");
		}
		if (biosimrc.get("biosim.sim.interval", "").equals("")) {
			biosimrc.put("biosim.sim.interval", "1.0");
		}
		if (biosimrc.get("biosim.sim.step", "").equals("")) {
			biosimrc.put("biosim.sim.step", "inf");
		}
		if (biosimrc.get("biosim.sim.min.step", "").equals("")) {
			biosimrc.put("biosim.sim.min.step", "0");
		}
		if (biosimrc.get("biosim.sim.error", "").equals("")) {
			biosimrc.put("biosim.sim.error", "1.0E-9");
		}
		if (biosimrc.get("biosim.sim.relative.error", "").equals("")) {
			biosimrc.put("biosim.sim.relative.error", "0.0");
		}
		if (biosimrc.get("biosim.sim.seed", "").equals("")) {
			biosimrc.put("biosim.sim.seed", "314159");
		}
		if (biosimrc.get("biosim.sim.runs", "").equals("")) {
			biosimrc.put("biosim.sim.runs", "1");
		}
		if (biosimrc.get("biosim.sim.rapid1", "").equals("")) {
			biosimrc.put("biosim.sim.rapid1", "0.1");
		}
		if (biosimrc.get("biosim.sim.rapid2", "").equals("")) {
			biosimrc.put("biosim.sim.rapid2", "0.1");
		}
		if (biosimrc.get("biosim.sim.qssa", "").equals("")) {
			biosimrc.put("biosim.sim.qssa", "0.1");
		}
		if (biosimrc.get("biosim.sim.concentration", "").equals("")) {
			biosimrc.put("biosim.sim.concentration", "15");
		}
		if (biosimrc.get("biosim.learn.tn", "").equals("")) {
			biosimrc.put("biosim.learn.tn", "2");
		}
		if (biosimrc.get("biosim.learn.tj", "").equals("")) {
			biosimrc.put("biosim.learn.tj", "2");
		}
		if (biosimrc.get("biosim.learn.ti", "").equals("")) {
			biosimrc.put("biosim.learn.ti", "0.5");
		}
		if (biosimrc.get("biosim.learn.bins", "").equals("")) {
			biosimrc.put("biosim.learn.bins", "4");
		}
		if (biosimrc.get("biosim.learn.equaldata", "").equals("")) {
			biosimrc.put("biosim.learn.equaldata", "Equal Data Per Bins");
		}
		if (biosimrc.get("biosim.learn.autolevels", "").equals("")) {
			biosimrc.put("biosim.learn.autolevels", "Auto");
		}
		if (biosimrc.get("biosim.learn.ta", "").equals("")) {
			biosimrc.put("biosim.learn.ta", "1.15");
		}
		if (biosimrc.get("biosim.learn.tr", "").equals("")) {
			biosimrc.put("biosim.learn.tr", "0.75");
		}
		if (biosimrc.get("biosim.learn.tm", "").equals("")) {
			biosimrc.put("biosim.learn.tm", "0.0");
		}
		if (biosimrc.get("biosim.learn.tt", "").equals("")) {
			biosimrc.put("biosim.learn.tt", "0.025");
		}
		if (biosimrc.get("biosim.learn.debug", "").equals("")) {
			biosimrc.put("biosim.learn.debug", "0");
		}
		if (biosimrc.get("biosim.learn.succpred", "").equals("")) {
			biosimrc.put("biosim.learn.succpred", "Successors");
		}
		if (biosimrc.get("biosim.learn.findbaseprob", "").equals("")) {
			biosimrc.put("biosim.learn.findbaseprob", "False");
		}
		if (biosimrc.get(GlobalConstants.SBOL_AUTHORITY_PREFERENCE, "").equals("")) {
			biosimrc.put(GlobalConstants.SBOL_AUTHORITY_PREFERENCE, GlobalConstants.SBOL_AUTHORITY_DEFAULT);
		}
		if (biosimrc.get(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, "").equals("")) {
			biosimrc.put(GlobalConstants.GENETIC_CONSTRUCT_REGEX_PREFERENCE, GlobalConstants.GENETIC_CONSTRUCT_REGEX_DEFAULT);
		}
		if (biosimrc.get(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, "").equals("")) {
			biosimrc.put(GlobalConstants.CONSTRUCT_VALIDATION_PREFERENCE, GlobalConstants.CONSTRUCT_VALIDATION_DEFAULT);
		}
		if (biosimrc.get(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, "").equals("")) {
			biosimrc.put(GlobalConstants.CONSTRUCT_ASSEMBLY_PREFERENCE, GlobalConstants.CONSTRUCT_ASSEMBLY_DEFAULT);
		}
		if (biosimrc.get(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, "").equals("")) {
			biosimrc.put(GlobalConstants.CONSTRUCT_VALIDATION_WARNING_PREFERENCE, GlobalConstants.CONSTRUCT_VALIDATION_WARNING_DEFAULT);
		}
		if (biosimrc.get("biosim.general.tree_icons", "").equals("")) {
			biosimrc.put("biosim.general.tree_icons", "default");
		}
		else if (biosimrc.get("biosim.general.tree_icons", "").equals("plus_minus")) {
			tree.setExpandibleIcons(false);
		}
		if (biosimrc.get("biosim.general.delete", "").equals("")) {
			biosimrc.put("biosim.general.delete", "confirm");
		}
	}
}
