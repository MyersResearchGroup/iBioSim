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
package edu.utah.ece.async.sbol.browser;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.*;
import org.sbolstandard.core2.*;

import java.net.URI;
import java.util.*;

import org.sbolstandard.core2.Collection;

import edu.utah.ece.async.sbol.util.SBOLUtility2;
import edu.utah.ece.async.main.Gui;
import edu.utah.ece.async.util.GlobalConstants;

/**
 * 
 *
 * @author Nicholas Roehner
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLBrowser2 extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SBOLDocument SBOLDOC; 
	
//	private static final long serialVersionUID = 1L;
	private String[] options = {"Ok", "Cancel"};
	private JPanel      selectionPanel = new JPanel(new GridLayout(1,2));
	private JPanel      filterTypePanel    = new JPanel(new GridLayout(1,3));
	private JComboBox   filterTypeBox;
	private JPanel      filterRolePanel    = new JPanel(new GridLayout(1,3));
	private JComboBox   filterRoleBox;

	private JTextArea   viewArea   = new JTextArea();
	private JScrollPane viewScroll = new JScrollPane();
	private CollectionBrowserPanel2          libPanel; 
	private ComponentDefinitionBrowserPanel compPanel; // control what to display for Component Definition(s)
	
	private LinkedList<URI>    selectedCompURIs; 
	private LinkedList<URI>    localLibURIs  = new LinkedList<URI>();    //store Collection Identity(s)
	private LinkedList<String> localLibIds   = new LinkedList<String>(); //store Collection displayId(s)
	private LinkedList<URI>    localCompURIs = new LinkedList<URI>();	 //store ComponentDef. Identity(s)
	private boolean isAssociationBrowser = false;

	private Gui    gui;
	private String browsePath;
	
	//Constructor when browsing a single RDF file from the main gui
	public SBOLBrowser2(Gui gui, String browsePath) {
		super(new BorderLayout());
		
		this.gui        = gui;
		this.browsePath = browsePath;
		this.SBOLDOC    = new SBOLDocument();
		
		//browsePath = browsePath.replace("\\\\", "\\");
		loadSbolFiles(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION), browsePath);
		constructBrowser(new HashSet<URI>());
	}
		
	public void open() {			
		JPanel browserPanel = new JPanel(new BorderLayout());
		JPanel filterPanel = new JPanel();
		filterPanel.add(filterTypePanel);
		filterPanel.add(filterRolePanel);
		browserPanel.add(filterPanel, BorderLayout.NORTH);
		browserPanel.add(selectionPanel, BorderLayout.CENTER);
		browserPanel.add(viewScroll, BorderLayout.SOUTH);
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
		gui.addTab(browsePath.substring(browsePath.lastIndexOf(GlobalConstants.separator) + 1), this, "SBOL Browser");
	}
	
	public void reload(Gui gui,String browsePath) {
		this.removeAll();
		
		browsePath = browsePath.replace("\\\\", "\\");
		
		selectionPanel = new JPanel(new GridLayout(1,2));
		filterTypePanel = new JPanel(new GridLayout(1,2));
		filterRolePanel = new JPanel(new GridLayout(1,2));
		viewArea = new JTextArea();
		viewScroll = new JScrollPane();
		
		localLibURIs = new LinkedList<URI>();
		localLibIds = new LinkedList<String>();
		localCompURIs = new LinkedList<URI>();
		
		loadSbolFiles(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION), browsePath);
		
		constructBrowser(new HashSet<URI>());
		
		JPanel browserPanel = new JPanel(new BorderLayout());
		JPanel filterPanel = new JPanel();
		filterPanel.add(filterTypePanel);
		filterPanel.add(filterRolePanel);
		browserPanel.add(filterPanel, BorderLayout.NORTH);
		browserPanel.add(selectionPanel, BorderLayout.CENTER);
		browserPanel.add(viewScroll, BorderLayout.SOUTH);
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SBOLBrowser2(HashSet<String> sbolFilePaths, Set<URI> filter) 
	{
//		super(new GridLayout(3,1));
		super(new BorderLayout());
		isAssociationBrowser = true;
		selectedCompURIs = new LinkedList<URI>();
		this.SBOLDOC    = new SBOLDocument();
		
		loadSbolFiles(sbolFilePaths, "");
	
		constructBrowser(filter);

		JPanel filterPanel = new JPanel();
		filterPanel.add(filterTypePanel);
		filterPanel.add(filterRolePanel);
		this.add(filterPanel, BorderLayout.NORTH);
		this.add(selectionPanel, BorderLayout.CENTER);
		this.add(viewScroll, BorderLayout.SOUTH);

		boolean display = true;
		while (display)
			display = browserOpen();
	}
	
	private void loadSbolFiles(HashSet<String> sbolFilePaths, String browsePath) 
	{
		String browseFile = browsePath.substring(browsePath.lastIndexOf(GlobalConstants.separator) + 1);
		
//		boolean first = true;
		for (String filePath : sbolFilePaths) 
		{
			String file = filePath.substring(filePath.lastIndexOf(GlobalConstants.separator) + 1);
			
			if (browsePath.equals("") || browseFile.equals(file)) 
			{
				SBOLDocument sbolDoc = null;
				try {
					sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SBOLValidationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SBOLConversionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				if (first) {
//					SBOLDOC = SBOLUtility2.loadSBOLFile(filePath);
//					for(ComponentDefinition c : SBOLDOC.getComponentDefinitions())
//					{
//						if ((!isAssociationBrowser || !c.getIdentity().toString().endsWith("iBioSim"))) 
//							localCompURIs.add(c.getIdentity());
//					}
//					for(Collection col : SBOLDOC.getCollections())
//					{
//						if (col.getDisplayId() != null && !col.getDisplayId().equals("") && 
//								!localLibURIs.contains(col.getIdentity().toString())) 
//						{
//							localLibURIs.add(col.getIdentity()); 
//							localLibIds.add(col.getDisplayId());
//						}
//					} 
//					first = false;
//				} else {
//					sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
//				}
				if (sbolDoc != null) 
				{
					for(ComponentDefinition c : sbolDoc.getComponentDefinitions())
					{
						if(SBOLDOC.getComponentDefinition(c.getIdentity()) == null) 
						{
							try {
								SBOLDOC.createCopy(c);
							}
							catch (SBOLValidationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if ((!isAssociationBrowser || !c.getIdentity().toString().endsWith("iBioSim"))) 
								localCompURIs.add(c.getIdentity());
						}
					}
					for(Sequence s : sbolDoc.getSequences())
					{
						if(SBOLDOC.getSequence(s.getIdentity()) == null) {
							try {
								SBOLDOC.createCopy(s);
							}
							catch (SBOLValidationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					/*for(Model m : sbolDoc.getModels())
					{
						if(SBOLDOC.getModel(m.getIdentity()) == null)
							SBOLDOC.createCopy(m);
					}
					for(ModuleDefinition modDef : sbolDoc.getModuleDefinitions())
					{
						if(SBOLDOC.getModuleDefinition(modDef.getIdentity()) == null)
							SBOLDOC.createCopy(modDef);
					}
					for(GenericTopLevel gtl : sbolDoc.getGenericTopLevels())
					{
						if(SBOLDOC.getGenericTopLevel(gtl.getIdentity()) == null)
							SBOLDOC.createCopy(gtl);
					}
					*/
					for(Collection col : sbolDoc.getCollections())
					{
						if(SBOLDOC.getCollection(col.getIdentity()) == null) {
							try {
								SBOLDOC.createCopy(col);
							}
							catch (SBOLValidationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (col.getDisplayId() != null && !col.getDisplayId().equals("") && 
								!localLibURIs.contains(col.getIdentity().toString())) 
						{
							localLibURIs.add(col.getIdentity()); 
							localLibIds.add(col.getDisplayId());
						}
					} // end of collection iteration
				} //end of sbol != null
			} //end of searching 1 file
		} // end of iterating through mult files
		
		Set<String> compDefTypes = new LinkedHashSet<String>();
		compDefTypes.add("all");
		compDefTypes.add("DNA");
		compDefTypes.add("RNA");
		compDefTypes.add("protein");
		compDefTypes.add("complex");
		compDefTypes.add("small molecule");
		compDefTypes.add("effector");
		filterTypeBox = new JComboBox(compDefTypes.toArray());
		
		Set<String> compDefRoles = new LinkedHashSet<String>();
		compDefRoles.add("all");
		for (int i = 0; i < localCompURIs.size(); i++) {
			ComponentDefinition localComp = null;
			localComp = SBOLDOC.getComponentDefinition(localCompURIs.get(i));
			if (localComp.getRoles().size() > 0) 
			{	
				compDefRoles.add(SBOLUtility2.convertURIToSOTerm(localComp.getRoles().iterator().next()));
			}
		}
		filterRoleBox = new JComboBox(compDefRoles.toArray());
	}
	
	private boolean browserOpen() 
	{
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Browser", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == JOptionPane.YES_OPTION) 
		{
			selectedCompURIs = compPanel.getSelectedURIs();
			if (selectedCompURIs.size() == 0) 
			{
				JOptionPane.showMessageDialog(Gui.frame, "No Component Definition is selected.",
						"Invalid Selection", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}
	
	private void constructBrowser(Set<URI> filter) 
	{
		viewScroll.setMinimumSize(new Dimension(780, 400));
		viewScroll.setPreferredSize(new Dimension(828, 264));
		viewScroll.setViewportView(viewArea);
		viewArea.setLineWrap(true);
		viewArea.setEditable(false);
		
		compPanel = new ComponentDefinitionBrowserPanel(SBOLDOC, viewArea);
		libPanel = new CollectionBrowserPanel2(SBOLDOC, viewArea, compPanel);
		libPanel.setFilter(filter);
		libPanel.setLocalLibsComps(localLibIds, localLibURIs, localCompURIs);
		libPanel.setIsAssociationBrowser(isAssociationBrowser);
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
		
		filterTypeBox.setActionCommand("filterTypes");
		filterTypeBox.addActionListener(this);
		JLabel filterTypeLabel = new JLabel("Filter by Type:  ");
		filterTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		filterTypePanel.add(filterTypeLabel);
		filterTypePanel.add(filterTypeBox);

		filterRoleBox.setActionCommand("filterRoles");
		filterRoleBox.addActionListener(this);
		JLabel filterRoleLabel = new JLabel("Filter by Role:  ");
		filterRoleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		filterRolePanel.add(filterRoleLabel);
		filterRolePanel.add(filterRoleBox);
	}
	
	public LinkedList<URI> getSelection() 
	{
		return selectedCompURIs;
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getActionCommand().equals("filterRoles")) 
		{
			libPanel.displaySelected();
			if (!filterRoleBox.getSelectedItem().toString().equals("all")) {
				filterTypeBox.setSelectedItem("DNA");
			}
			compPanel.filterComponentsByRole(filterRoleBox.getSelectedItem().toString());
		} else if (e.getActionCommand().equals("filterTypes")) 
		{
			libPanel.displaySelected();
			if (!filterTypeBox.getSelectedItem().toString().equals("DNA")) {
				filterRoleBox.setSelectedItem("all");
			}
			compPanel.filterComponentsByType(filterTypeBox.getSelectedItem().toString());
		}
		
	}
}
