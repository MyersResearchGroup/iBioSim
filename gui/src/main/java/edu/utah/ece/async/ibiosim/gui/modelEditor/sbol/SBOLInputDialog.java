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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.InputDialog;
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
	
	private JCheckBox onlyShowRootModDefs;
	private JCheckBox onlyShowRootCompDefs;
	

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
		return "There are multiple designs.  Select the design(s) you would to perform VPR Model Generation on.";
	}

	@Override
	public void initFormPanel(FormBuilder builder) 
	{
		//The initial design layout
		onlyShowRootModDefs = new JCheckBox("Show root ModuleDefinitions");
		onlyShowRootModDefs.setSelected(true);
		onlyShowRootModDefs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateTable();
			}
		});
		builder.add("", onlyShowRootModDefs);
		
		onlyShowRootCompDefs = new JCheckBox("Show root ComponentDefinitions");
		onlyShowRootCompDefs.setSelected(true);
		onlyShowRootCompDefs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateTable();
			}
		});
		builder.add("", onlyShowRootCompDefs);
		
		
	}

	@Override
	protected JPanel initMainPanel() 
	{
		//Get information for main design layout and load them up
		List<TopLevel> topLevelObjs = new ArrayList<TopLevel>();
		if(onlyShowRootCompDefs.isSelected())
		{
			topLevelObjs.addAll(doc.getRootComponentDefinitions());
		}
		if(onlyShowRootModDefs.isSelected())
		{
			topLevelObjs.addAll(doc.getRootModuleDefinitions());
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

	
	public SBOLDocument getSelection() {
		try 
		{
			int row = table.convertRowIndexToModel(table.getSelectedRow());
			TopLevel comp = ((TopLevelTableModel) table.getModel()).getElement(row);
			return doc.createRecursiveCopy(comp);
		} 
		catch (SBOLValidationException e) 
		{
			JOptionPane.showMessageDialog(null, "This TopLevel SBOL object cannot be imported: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private void updateTable() 
	{
		List<TopLevel> topLevelObjs = new ArrayList<TopLevel>();
		if (onlyShowRootCompDefs.isSelected()) 
		{
			topLevelObjs.addAll(doc.getRootComponentDefinitions());
		} 
		
		if (onlyShowRootModDefs.isSelected())  
		{
			topLevelObjs.addAll(doc.getRootModuleDefinitions());
		}
		
		((TopLevelTableModel) table.getModel()).setElements(topLevelObjs);
		tableLabel.setText("Select Design(s) (" + topLevelObjs.size() + ")");
	}

}
