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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.TopLevel;

import com.google.common.base.Throwables;

import edu.utah.ece.async.sboldesigner.sbol.editor.Registries;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.InputDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryPreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

/**
 * Design Selection Dialog for VPR Model Generation and SBOLDesigner.
 * 
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLInputDialog extends InputDialog<SBOLDocument> {
	private static final String TITLE = "Select Designs";
	private final ActionListener actionListener = new DialogActionListener();
	private JTable table;
	private JLabel tableLabel;
	
	private JCheckBox onlyShowRootModDefs, onlyShowRootCompDefs;
	
	private JButton openSBOLDesigner, openVPRGenerator, optionsButton;

	private SBOLDocument doc;
	
	private boolean sbolDesigner, vprGenerator;

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
		return "Select multiple designs by holding down alt/command on your keyboard.";
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
			SBOLDocument outputDoc = new SBOLDocument();
			
			for(int r : table.getSelectedRows())
			{
				int row = table.convertRowIndexToModel(r);
				TopLevel comp = ((TopLevelTableModel) table.getModel()).getElement(row); 
				outputDoc.createCopy(doc.createRecursiveCopy(comp));
			}
			
			return outputDoc;
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
	
	/**
	 * Returns SBOLDocument, containing whatever data is selected
	 */
	public SBOLDocument getInput() {
		initGUI();
		try {
			setVisible(true);
			if (canceled) {
				return null;
			}
			Registries.get().save();
			return getSelection();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(getParent(), Throwables.getRootCause(e).getMessage(), "ERROR",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}
	
	private void initGUI() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		if (registrySelection != null) {
			optionsButton = new JButton("Options");
			optionsButton.addActionListener(actionListener);
			buttonPanel.add(optionsButton);
		}

		buttonPanel.add(Box.createHorizontalStrut(200));
		buttonPanel.add(Box.createHorizontalGlue());

		openSBOLDesigner = new JButton("Open SBOLDesigner");
		openSBOLDesigner.addActionListener(actionListener);
		openSBOLDesigner.setEnabled(false);
		getRootPane().setDefaultButton(openSBOLDesigner);
		buttonPanel.add(openSBOLDesigner);
		
		openVPRGenerator = new JButton("Generate Model");
		openVPRGenerator.addActionListener(actionListener);
		openVPRGenerator.setEnabled(false);
		getRootPane().setDefaultButton(openVPRGenerator);
		buttonPanel.add(openVPRGenerator);
		
		initFormPanel(builder);

		JComponent formPanel = builder.build();
		formPanel.setAlignmentX(LEFT_ALIGNMENT);

		Box topPanel = Box.createVerticalBox();
		String message = initMessage();
		if (message != null) {
			JPanel messageArea = new JPanel();
			messageArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6),
					BorderFactory.createEtchedBorder()));
			messageArea.setAlignmentX(LEFT_ALIGNMENT);
			messageArea.add(new JLabel("<html>" + message.replace("\n", "<br>") + "</html>"));
			topPanel.add(messageArea);
		}
		topPanel.add(formPanel);

		JComponent mainPanel = initMainPanel();

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		contentPane.add(topPanel, BorderLayout.NORTH);
		if (mainPanel != null) {
			contentPane.add(mainPanel, BorderLayout.CENTER);
		}
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		setContentPane(contentPane);

		initFinished();

		if (registrySelection != null) {
			registryChanged();
		}

		pack();
		setLocationRelativeTo(getOwner());
	}
	
	private class DialogActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == registrySelection) {
				final Registry registry = (Registry) registrySelection.getSelectedItem();
				if (registry == null) {
					location = null;
					location = null;
				} else {
					int selectedIndex = registrySelection.getSelectedIndex();
					
					Registries.get().setVersionRegistryIndex(selectedIndex);
					
					location = registry.getLocation();
					uriPrefix = registry.getUriPrefix();
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						registryChanged();
					}
				});
			} else if (source == optionsButton) {
				PreferencesDialog.showPreferences(SBOLInputDialog.this, RegistryPreferencesTab.INSTANCE.getTitle());
				registrySelection.removeAllItems();
				for (Registry r : Registries.get()) {
					registrySelection.addItem(r);
				}
				registrySelection.setSelectedIndex(Registries.get().getPartRegistryIndex());
			} else if (source == openSBOLDesigner) {
				setVisible(false);
				canceled = false;
				sbolDesigner = true;
				vprGenerator = false;
			} else if (source == openVPRGenerator) {
				setVisible(false);
				canceled = false;
				sbolDesigner = false;
				vprGenerator = true;
			}
		}
	}
	
	@Override
	protected void setSelectAllowed(boolean allow) {

		openSBOLDesigner.setEnabled(allow);
		openVPRGenerator.setEnabled(allow);
		
		
	}
	@Override
	protected void handleTableSelection() {
		canceled = false;
		setVisible(false);
	}

	
	public boolean isVPRGenerator()
	{
		return vprGenerator;
	}
	
	public boolean isSBOLDesigner()
	{
		return sbolDesigner;
	}
	
	public boolean isCanceled()
	{
		return canceled;
	}
	
}
