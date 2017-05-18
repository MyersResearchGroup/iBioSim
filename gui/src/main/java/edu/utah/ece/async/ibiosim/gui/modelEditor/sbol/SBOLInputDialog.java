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
package edu.utah.ece.async.ibiosim.gui.modelEditor.sbol;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;

import com.google.common.collect.Lists;

import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types;
import edu.utah.ece.async.sboldesigner.sbol.editor.Part;
import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.InputDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PartCellRenderer;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

/**
 * 
 * 
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLInputDialog extends InputDialog<SBOLDocument> {
	private static final String TITLE = "Designs for VPR Model Generation";

	private JTable table;
	private JLabel tableLabel;

//	private JComboBox<Part> roleSelection;
//	private JComboBox<String> roleRefinement;
//	private JComboBox<Types> typeSelection;
	
	private JCheckBox onlyShowRootSBOLObjs;
	private JCheckBox onlyShowRootModDefs;
	private JCheckBox onlyShowRootCompDefs;
	
	private JButton deleteCD;
	private static final Part ALL_PARTS = new Part("All parts", "All");

	private SBOLDocument doc;

	/**
	 * this.getInput() returns an SBOLDocument with a single rootCD selected
	 * from the rootCDs in doc.
	 */
	public SBOLInputDialog(final Component parent, SBOLDocument doc) {
		super(parent, TITLE);

		this.doc = doc;
	}

	@Override
	protected String initMessage() {
		return "There are multiple designs.  Select the design(s) you would to perform VPR Model Generation on";
	}

	@Override
	public void initFormPanel(FormBuilder builder) 
	{
		//The initial design layout
		onlyShowRootModDefs = new JCheckBox("Only show root ModuleDefinitions");
		onlyShowRootModDefs.setSelected(true);
		onlyShowRootModDefs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateTable();
			}
		});
		builder.add("", onlyShowRootModDefs);
		
		onlyShowRootCompDefs = new JCheckBox("Only show root ComponentDefinitions");
		onlyShowRootCompDefs.setSelected(true);
		onlyShowRootCompDefs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateTable();
			}
		});
		builder.add("", onlyShowRootCompDefs);
		
		
		onlyShowRootSBOLObjs = new JCheckBox("Only show root ComponentDefinitions and ModuleDefinitions");
		onlyShowRootSBOLObjs.setSelected(true);
		onlyShowRootSBOLObjs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateTable();
			}
		});
		builder.add("", onlyShowRootSBOLObjs);
	}

	@Override
	protected JPanel initMainPanel() 
	{
		//Get information for main design layout and load them up
		List<TopLevel> topLevelObjs = new ArrayList<TopLevel>();
		if (onlyShowRootSBOLObjs.isSelected()) 
		{
			topLevelObjs.addAll(doc.getRootComponentDefinitions());
			topLevelObjs.addAll(doc.getRootModuleDefinitions());
		} 
		else 
		{
			topLevelObjs.addAll(doc.getComponentDefinitions());
			topLevelObjs.addAll(doc.getModuleDefinitions());
		}

		// Show an instance of list of designs user can choose from
		TopLevelTableModel tableModel = new TopLevelTableModel(topLevelObjs);
		JPanel panel = createTablePanel(tableModel, "Select Design(s) (" + tableModel.getRowCount() + ")");
		table = (JTable) panel.getClientProperty("table");
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableLabel = (JLabel) panel.getClientProperty("label");

		updateTable();

		return panel;
	}

	@Override
	protected SBOLDocument getSelection() {
		try {
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			TopLevel comp = ((TopLevelTableModel) table.getModel()).getElement(row);
			return doc.createRecursiveCopy(comp);
		} catch (SBOLValidationException e) {
			JOptionPane.showMessageDialog(null, "This ComponentDefinition cannot be imported: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

//	private void updateRoleRefinement() {
//		roleRefinement.removeAllItems();
//		for (String s : SBOLUtils.createRefinements((Part) roleSelection.getSelectedItem())) {
//			roleRefinement.addItem(s);
//		}
//	}

	private void updateTable() {
//		Part part;
//		String roleName = (String) roleRefinement.getSelectedItem();
//		if (roleName == null || roleName.equals("None")) {
//			part = isRoleSelection() ? (Part) roleSelection.getSelectedItem() : ALL_PARTS;
//		} else {
//			SequenceOntology so = new SequenceOntology();
//			URI role = so.getURIbyName(roleName);
//			part = new Part(role, null, null);
//		}
//
//		Set<ComponentDefinition> CDsToDisplay;
//		if (onlyShowRootCDs.isSelected()) {
//			CDsToDisplay = doc.getRootComponentDefinitions();
//		} else {
//			CDsToDisplay = doc.getComponentDefinitions();
//		}
//
//		List<ComponentDefinition> components = SBOLUtils.getCDOfRole(CDsToDisplay, part);
//		components = SBOLUtils.getCDOfType(components, (Types) typeSelection.getSelectedItem());
//		((TopLevelTableModel) table.getModel()).setElements(components);
//		tableLabel.setText("Matching parts (" + components.size() + ")");
	}

//	private boolean isRoleSelection() {
//		return roleSelection != null;
//	}

	private void updateFilter(String filterText) {
		filterText = "(?i)" + filterText;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		TableRowSorter<TopLevelTableModel> sorter = (TableRowSorter) table.getRowSorter();
		if (filterText.length() == 0) {
			sorter.setRowFilter(null);
		} else {
			try {
				RowFilter<TopLevelTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1);
				sorter.setRowFilter(rf);
			} catch (java.util.regex.PatternSyntaxException e) {
				sorter.setRowFilter(null);
			}
		}

		tableLabel.setText("Matching parts (" + sorter.getViewRowCount() + ")");
	}
}
