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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.metal.MetalButtonUI;

import com.mxgraph.util.mxConstants;

import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.ibiosim.gui.util.preferences.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum ModelEditorPreferencesTab implements PreferencesTab {
	INSTANCE;
	
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
	
	private HashMap<String,SchematicElement> schematicElements = new HashMap<String, SchematicElement>();

	@Override
	public String getTitle() {
		return "Model Editor";
	}

	@Override
	public String getDescription() {
		return "Preferences for Model Editor Schematic";
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.getImageIcon("sbml.jpg");
	}
	
	private static Color createColorFromString(String colorStr) {
		int red = Integer.valueOf(colorStr.substring(1,3),16);
		int green = Integer.valueOf(colorStr.substring(3,5),16);
		int blue = Integer.valueOf(colorStr.substring(5,7),16);
		return new Color(red,green,blue);
	}
	
	boolean async = false;
	
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

	@Override
	public Component getComponent() {
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
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.shape."+element).equals("")) {
					String shape = IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.shape."+element);
					if (IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.rounded."+element).equals("true")) {
						shape += " (rounded)";
					}
					schematicElements.get(element).shape.setSelectedItem(shape);
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.color."+element).equals("")) {
					schematicElements.get(element).colorButton = 
							createColorButton(createColorFromString(IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.color."+element)));
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.strokeColor."+element).equals("")) {
					schematicElements.get(element).strokeButton = 
							createColorButton(createColorFromString(IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.strokeColor."+element)));
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.fontColor."+element).equals("")) {
					schematicElements.get(element).fontButton = 
							createColorButton(createColorFromString(IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.fontColor."+element)));
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.opacity."+element).equals("")) {
					schematicElements.get(element).opacity.setText(IBioSimPreferences.INSTANCE.getSchematicPreference("lema.schematic.opacity."+element));
				} 
			} else {
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.shape."+element).equals("")) {
					String shape = IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.shape."+element);
					if (IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.rounded."+element).equals("true")) {
						shape += " (rounded)";
					}
					schematicElements.get(element).shape.setSelectedItem(shape);
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.color."+element).equals("")) {
					schematicElements.get(element).colorButton = 
							createColorButton(createColorFromString(IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.color."+element)));
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.strokeColor."+element).equals("")) {
					schematicElements.get(element).strokeButton = 
							createColorButton(createColorFromString(IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.strokeColor."+element)));
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.fontColor."+element).equals("")) {
					schematicElements.get(element).fontButton = 
							createColorButton(createColorFromString(IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.fontColor."+element)));
				} 
				if (!IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.opacity."+element).equals("")) {
					schematicElements.get(element).opacity.setText(IBioSimPreferences.INSTANCE.getSchematicPreference("biosim.schematic.opacity."+element));
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
	
		FormBuilder builder = new FormBuilder();
		builder.add("", schematicPrefsFinal);
	
		return builder.build();
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

	@Override
	public void save() {
		for (String element:schematicElements.keySet()) {
			SchematicElement schematicElement = schematicElements.get(element);
			String rounded = "false";
			String shape = (String)schematicElement.shape.getSelectedItem();
			if (((String)schematicElement.shape.getSelectedItem()).equals(mxConstants.SHAPE_RECTANGLE+" (rounded)")) {
				rounded = "true";
				shape = mxConstants.SHAPE_RECTANGLE;
			}
			if (async) {
				IBioSimPreferences.INSTANCE.setSchematicPreference("lema.schematic.color."+element, colorToString(schematicElement.colorButton.getForeground()));
				IBioSimPreferences.INSTANCE.setSchematicPreference("lema.schematic.strokeColor."+element, colorToString(schematicElement.strokeButton.getForeground()));
				IBioSimPreferences.INSTANCE.setSchematicPreference("lema.schematic.fontColor."+element, colorToString(schematicElement.fontButton.getForeground()));
				IBioSimPreferences.INSTANCE.setSchematicPreference("lema.schematic.opacity."+element, schematicElement.opacity.getText());
				IBioSimPreferences.INSTANCE.setSchematicPreference("lema.schematic.shape."+element, shape);
				IBioSimPreferences.INSTANCE.setSchematicPreference("lema.schematic.rounded."+element, rounded);
			} else {
				IBioSimPreferences.INSTANCE.setSchematicPreference("biosim.schematic.color."+element, colorToString(schematicElement.colorButton.getForeground()));
				IBioSimPreferences.INSTANCE.setSchematicPreference("biosim.schematic.strokeColor."+element, colorToString(schematicElement.strokeButton.getForeground()));
				IBioSimPreferences.INSTANCE.setSchematicPreference("biosim.schematic.fontColor."+element, colorToString(schematicElement.fontButton.getForeground()));
				IBioSimPreferences.INSTANCE.setSchematicPreference("biosim.schematic.opacity."+element, schematicElement.opacity.getText());
				IBioSimPreferences.INSTANCE.setSchematicPreference("biosim.schematic.shape."+element, shape);
				IBioSimPreferences.INSTANCE.setSchematicPreference("biosim.schematic.rounded."+element, rounded);
			}
		}	
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}
