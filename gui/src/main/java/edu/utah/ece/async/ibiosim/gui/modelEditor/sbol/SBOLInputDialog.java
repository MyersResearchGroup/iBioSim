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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.gui.Gui;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types;
import edu.utah.ece.async.sboldesigner.sbol.editor.Part;
import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registries;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.InputDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PartCellRenderer;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryPreferencesTab;
import edu.utah.ece.async.sboldesigner.swing.AbstractListTableModel;
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
	
	private final Gui gui;
	private final String filePath;
	private final String fileName;
	
	private final ActionListener actionListener = new DialogActionListener();
	private JTable table;
	private JLabel tableLabel;
	
	private JComboBox<Part> roleSelection;
	private JComboBox<String> roleRefinement;
	private JComboBox<Types> typeSelection;
	private JButton deleteTopLevelObjs;
	private static final Part ALL_PARTS = new Part("All parts", "All");
	
	private JCheckBox showRootDefs;
	private JRadioButton showModDefs, showCompDefs;
	
	private JButton openSBOLDesigner, openVPRGenerator, optionsButton, cancelButton;

	private SBOLDocument sbolDesigns;
	
	private boolean sbolDesigner, vprGenerator;

	/**
	 * An instance of the SBOL Design/Part selection dialog that will allow the user to open their selected design
	 * or part in SBOLDesigner or perform VPR Model Generation.
	 * 
	 * @param parent - The component the user wants this SBOL input dialog to be called on. In this case, this is the main view of iBioSim.
	 * @param gui - The gui that will allow SBOLInputDialog to pop up in.
	 * @param filePath - Path to where the designs will be loaded from
	 * @param fileName - Name of the file where the designs are loaded from.
	 * @param doc -The SBOLDocument the user wants to select their design or parts from. 
	 * This means that ComponentDefinition and ModuleDefinition contained within this given SBOLDocumen will be loaded into the SBOL Input Dialog as the designs the users are limited to selecting from.
	 */
	public SBOLInputDialog(final Component parent, final Gui gui, String filePath, String fileName, SBOLDocument doc) {
		super(parent, TITLE);
		this.gui = gui;
		this.filePath = filePath;
		this.fileName = fileName;
		this.sbolDesigns = doc;
	}
	

	@Override
	protected String initMessage() {
		return "Select multiple designs by holding down alt/command on your keyboard.";
	}

	/**
	 * Set up the initial design layout for the SBOL Design/Part dialog
	 */
	@Override
	public void initFormPanel(FormBuilder builder) 
	{
		// The initial design layout
		final JTextField filterSelection = new JTextField();
		filterSelection.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent paramDocumentEvent) {
				updateFilter(filterSelection.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent paramDocumentEvent) {
				updateFilter(filterSelection.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent paramDocumentEvent) {
				updateFilter(filterSelection.getText());
			}
		});

		
		builder.add("Filter parts", filterSelection);
		
		// I want checkboxes to be aligned so a JPanel was created with a grid of 2x2 to limit 2 checkboxes per row 
		JPanel filteredDesignPanel = new JPanel();
		int checkBoxRows = 2, checkBoxCols = 2;
		GridLayout designPanel = new GridLayout(checkBoxRows, checkBoxCols);
		filteredDesignPanel.setLayout(designPanel);
		
		showCompDefs = new JRadioButton("Show ComponentDefinitions");
		showCompDefs.setSelected(true);
		showCompDefs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) 
			{
				enableCompRoleType();
				updateTable();
			}
		});
		filteredDesignPanel.add(showCompDefs);
		
		showModDefs = new JRadioButton("Show ModuleDefinitions");
		showModDefs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) 
			{
				enableCompRoleType();
				updateTable();
			}
		});
		filteredDesignPanel.add(showModDefs);
		
		// Adding buttons to ButtonGroup will limit one button to be selected at a time.
		ButtonGroup enableSingleSelection = new ButtonGroup();
		enableSingleSelection.add(showCompDefs);
		enableSingleSelection.add(showModDefs);
		
		showRootDefs = new JCheckBox("Show root Definitions only");
		showRootDefs.setSelected(true);
		showRootDefs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				enableCompRoleType();
				updateTable();
			}
		});
		filteredDesignPanel.add(showRootDefs);
		builder.add("", filteredDesignPanel);
		
		typeSelection = new JComboBox<Types>(Types.values());
		typeSelection.setSelectedItem(Types.DNA);
		typeSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTable();
			}
		});
		builder.add("Part type", typeSelection);

		List<Part> parts = Lists.newArrayList(Parts.sorted());
		parts.add(0, ALL_PARTS);
		roleSelection = new JComboBox<Part>(parts.toArray(new Part[0]));
		roleSelection.setRenderer(new PartCellRenderer());
		roleSelection.setSelectedItem(ALL_PARTS);
		roleSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateRoleRefinement();
				updateTable();
			}
		});
		builder.add("Part role", roleSelection);

		// set up the JComboBox for role refinement
		roleRefinement = new JComboBox<String>();
		updateRoleRefinement();
		roleRefinement.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				updateTable();
			}
		});
		builder.add("Role refinement", roleRefinement);
		
		deleteTopLevelObjs = new JButton("Delete selected part(s). (This will resave the file)");
		deleteTopLevelObjs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int[] rows = table.getSelectedRows();
					for (int row : rows) {
						row = table.convertRowIndexToModel(row);
						TopLevel comp = ((TopLevelTableModel) table.getModel()).getElement(row);
						sbolDesigns.removeTopLevel(comp);
					}
					File file = SBOLUtils.setupFile();
					SBOLWriter.write(sbolDesigns, new FileOutputStream(file));
					updateTable();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(rootPane, "Failed to delete selected part: " + e1.getMessage());
					e1.printStackTrace();
				}
			}
		});
		builder.add("", deleteTopLevelObjs);
	}
	
	/**
	 * Enable or disable roleRefinement, roleSelection, and typeSelection ComboBox if ModuleDefinition or root ModuleDefinition is selected.. 
	 */
	private void enableCompRoleType()
	{
		boolean isEnabled = !(showModDefs.isSelected() && showRootDefs.isSelected() && !showCompDefs.isSelected()) &&
				!(showModDefs.isSelected() && !showRootDefs.isSelected() && !showCompDefs.isSelected());
		
		roleRefinement.setEnabled(isEnabled);
		roleSelection.setEnabled(isEnabled);
		typeSelection.setEnabled(isEnabled);
	}

	@Override
	protected JPanel initMainPanel() 
	{
		//Get information for main design layout and load them up
		List<TopLevel> topLevelObjs = new ArrayList<TopLevel>();
		if(showRootDefs.isSelected())
		{
			topLevelObjs.addAll(sbolDesigns.getRootComponentDefinitions());
		}
		if(showRootDefs.isSelected())
		{
			topLevelObjs.addAll(sbolDesigns.getRootModuleDefinitions());
		}

		//Show an a list of designs user can choose from
		TopLevelTableModel tableModel = new TopLevelTableModel(topLevelObjs);
		
		JPanel panel = createTablePanel(tableModel, "Select Design(s) (" + tableModel.getRowCount() + ")");
		
		table = (JTable) panel.getClientProperty("table");
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableLabel = (JLabel) panel.getClientProperty("label");
		
		updateTable();

		return panel;
	}
	
	@Override
	protected JPanel createTablePanel(AbstractListTableModel<?> tableModel, String title) 
	{
		/* Set up Design Table */
		final JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				setSelectAllowed(table.getSelectedRow() >= 0);
			}
		});

		setWidthAsPercentages(table, tableModel.getWidths());

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);

		JScrollPane tableScroller = new JScrollPane(table);
		tableScroller.setPreferredSize(new Dimension(450, 200));
		tableScroller.setAlignmentX(LEFT_ALIGNMENT);

		JLabel tableLabel = new JLabel(title);
		tableLabel.setLabelFor(table);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
		tablePane.add(tableLabel);
		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePane.add(tableScroller);

		tablePane.putClientProperty("table", table);
		tablePane.putClientProperty("scroller", tableScroller);
		tablePane.putClientProperty("label", tableLabel);

		//If the user decide to double click on a design, open the design in SBOLDesigner.
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && table.getSelectedRow() >= 0 && !showModDefs.isSelected())
				{
					SBOLDocument chosenDesign = getSelection(); 
					gui.openSBOLDesigner(filePath, fileName, chosenDesign.getRootComponentDefinitions(), chosenDesign.getDefaultURIprefix());
					setVisible(false);
				}
			}
		});
		
		return tablePane;
	}
	
	/**
	 * Get the SBOL part design that the user has selected from the Design/Parts table and return it part selected
	 * in a new SBOLDocument.
	 */
	public SBOLDocument getSelection()
	{
		SBOLDocument outputDoc = new SBOLDocument();

		for(int r : table.getSelectedRows())
		{
			int row = table.convertRowIndexToModel(r);
			TopLevel comp = ((TopLevelTableModel) table.getModel()).getElement(row); 
			outputDoc.setDefaultURIprefix(comp.getDocument().getDefaultURIprefix());
			try 
			{
				outputDoc.createCopy(sbolDesigns.createRecursiveCopy(comp));
			} 
			catch (SBOLValidationException e) 
			{
				JOptionPane.showMessageDialog(null, "This TopLevel SBOL object cannot be imported: " + e.getMessage());
				e.printStackTrace();

			}
		}

		return outputDoc;
	}
	
	
	private void updateRoleRefinement() 
	{
		roleRefinement.removeAllItems();
		for (String s : SBOLUtils.createRefinements((Part) roleSelection.getSelectedItem())) {
			roleRefinement.addItem(s);
		}
	}

	/**
	 * Update all design parts assigned to this table.
	 * These design parts include updating designs related to ComponentDefinitions and ModuleDefinitions.
	 */
	private void updateTable() 
	{
		List<TopLevel> topLevelObjs = new ArrayList<TopLevel>();
		
		topLevelObjs.addAll(getFilteredModDef());
		topLevelObjs.addAll(getFilteredCompDef());
		
		((TopLevelTableModel) table.getModel()).setElements(topLevelObjs);
		tableLabel.setText("Select Design(s) (" + topLevelObjs.size() + ")");
	}
	
	/**
	 * Filter Designs/parts with ComponentDefinitions if user want designs containing:
	 * - roleRefinement
	 * - role
	 * - root ComponentDefinitions
	 * - all ComponentDefinitions
	 * @return All ComponentDefinitions base on the user's filtered selections
	 */
	private List<ComponentDefinition> getFilteredCompDef()
	{
		Part part;
		String roleName = (String) roleRefinement.getSelectedItem();
		if (roleName == null || roleName.equals("None")) 
		{
			part = isRoleSelection() ? (Part) roleSelection.getSelectedItem() : ALL_PARTS;
		} 
		else 
		{
			SequenceOntology so = new SequenceOntology();
			URI role = so.getURIbyName(roleName);
			part = new Part(role, null, null);
		}
		
		Set<ComponentDefinition> CDsToDisplay;
		if (showRootDefs.isSelected() && showCompDefs.isSelected()) 
		{
			CDsToDisplay = sbolDesigns.getRootComponentDefinitions();
		} 
		else if (!showRootDefs.isSelected() && showCompDefs.isSelected())
		{
			CDsToDisplay = sbolDesigns.getComponentDefinitions();
		}
		else
		{
			CDsToDisplay = new HashSet<ComponentDefinition>(0);
		}
		
		List<ComponentDefinition> components = SBOLUtils.getCDOfRole(CDsToDisplay, part);
		components = SBOLUtils.getCDOfType(components, (Types) typeSelection.getSelectedItem());
		return components;
	}
	
	/**
	 * Filter Designs/parts with ModuleDefinitions if user want designs containing:
	 * - root ModuleDefinitions
	 * - all ModuleDefinitions
	 * @return All ModuleDefinitions base on the user's filtered selections
	 */
	private List<TopLevel> getFilteredModDef()
	{
		List<TopLevel> topLevelObjs = new ArrayList<TopLevel>();
		if (showRootDefs.isSelected() && showModDefs.isSelected())  
		{
			topLevelObjs.addAll(sbolDesigns.getRootModuleDefinitions());
		}
		if (!showRootDefs.isSelected() && showModDefs.isSelected())  
		{
			topLevelObjs.addAll(sbolDesigns.getModuleDefinitions());
		}
		
		return topLevelObjs;
	}
	
	private boolean isRoleSelection() {
		return roleSelection != null;
	}
	
	private void updateFilter(String filterText) 
	{
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
	
	private void initGUI() 
	{
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
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(actionListener);
		cancelButton.registerKeyboardAction(actionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		buttonPanel.add(cancelButton);
		
		openVPRGenerator = new JButton("Generate Model");
		openVPRGenerator.addActionListener(actionListener);
		openVPRGenerator.setEnabled(true);
		getRootPane().setDefaultButton(openVPRGenerator);
		buttonPanel.add(openVPRGenerator);
		
		openSBOLDesigner = new JButton("Open SBOLDesigner");
		openSBOLDesigner.addActionListener(actionListener);
		openSBOLDesigner.setEnabled(true);
		getRootPane().setDefaultButton(openSBOLDesigner);
		buttonPanel.add(openSBOLDesigner);
		
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
	
	private class DialogActionListener implements ActionListener 
	{
		public void actionPerformed(ActionEvent e) 
		{
			Object source = e.getSource();
			if (source == registrySelection) 
			{
				final Registry registry = (Registry) registrySelection.getSelectedItem();
				if (registry == null) 
				{
					location = null;
					location = null;
				} 
				else 
				{
					int selectedIndex = registrySelection.getSelectedIndex();
					
					Registries.get().setVersionRegistryIndex(selectedIndex);
					
					location = registry.getLocation();
					uriPrefix = registry.getUriPrefix();
				}
				SwingUtilities.invokeLater(new Runnable() 
				{
					@Override
					public void run() 
					{
						registryChanged();
					}
				});
			} 
			else if (source == optionsButton) 
			{
				PreferencesDialog.showPreferences(SBOLInputDialog.this, RegistryPreferencesTab.INSTANCE.getTitle());
				registrySelection.removeAllItems();
				for (Registry r : Registries.get()) 
				{
					registrySelection.addItem(r);
				}
				registrySelection.setSelectedIndex(Registries.get().getPartRegistryIndex());
			} 
			else if (source == cancelButton) 
			{
				canceled = true;
				setVisible(false);
			} else if (source == openSBOLDesigner) 
			{
				setVisible(false);
				canceled = false;
				sbolDesigner = true;
				vprGenerator = false;
			} 
			else if (source == openVPRGenerator) 
			{
				setVisible(false);
				canceled = false;
				sbolDesigner = false;
				vprGenerator = true;
			}
		}
	}
	
	@Override
	protected void setSelectAllowed(boolean allow) 
	{
		boolean setSBOLDesigner = (!showModDefs.isSelected() && showRootDefs.isSelected() && showCompDefs.isSelected()) ||
				(!showModDefs.isSelected() && !showRootDefs.isSelected() && showCompDefs.isSelected()) ||
				(showModDefs.isSelected() && showRootDefs.isSelected() && showCompDefs.isSelected());
		openSBOLDesigner.setEnabled(setSBOLDesigner && allow);
		openVPRGenerator.setEnabled(allow);
	}
	
	@Override
	protected void handleTableSelection() 
	{
		canceled = false;
		setVisible(false);
	}

	/**
	 * Check if VPR Model Generation button was selected
	 * @return True if the user clicked VPR Model Generation. False otherwise.
	 */
	public boolean isVPRGenerator()
	{
		return vprGenerator;
	}
	
	/**
	 * Check if SBOLDesigner button was selected
	 * @return True if the user clicked open design in SBOLDesigner. False otherwise.
	 */
	public boolean isSBOLDesigner()
	{
		return sbolDesigner;
	}

	/**
	 * Check if cancel button was selected
	 * @return True if the user clicked Cancel. False otherwise.
	 */
	public boolean isCanceled()
	{
		return canceled;
	}
	
}
