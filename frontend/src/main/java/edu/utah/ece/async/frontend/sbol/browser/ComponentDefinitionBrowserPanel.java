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
package edu.utah.ece.async.frontend.sbol.browser;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.core2.Component;

import edu.utah.ece.async.backend.sbol.util.SBOLUtility2;
import edu.utah.ece.async.dataModels.util.GlobalConstants;

//import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;
import org.sbolstandard.core2.*;

import java.net.URI;
import java.util.*;

/**
 * 
 *
 * @author Nicholas Roehner
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ComponentDefinitionBrowserPanel extends JPanel implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SBOLDocument SBOLDOC; 
	private LinkedList<URI>    compURIs;
	private LinkedList<String> compIDs;
	
	private JTextArea viewArea;
	private JList compDefList = new JList();
	
	public ComponentDefinitionBrowserPanel(SBOLDocument sbolDoc, JTextArea viewArea) {
		super(new BorderLayout());
		this.SBOLDOC = sbolDoc; 
		this.viewArea = viewArea;
		
		compDefList.addMouseListener(this);
		
		JLabel componentLabel = new JLabel("Component Defintions:");
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compDefList);
		
		this.add(componentLabel, "North");
		this.add(componentScroll, "Center");
	}
	
	public void setComponents(LinkedList<String> compIDs, LinkedList<URI> compURIs) {
		this.compURIs = compURIs;
		this.compIDs = compIDs;
		Object[] idObjects = compIDs.toArray();
		compDefList.setListData(idObjects);
	}
	
	public void filterComponentsByType(String filterRole) {
		viewArea.setText("");
		LinkedList<URI> filteredURIs = new LinkedList<URI>();
		LinkedList<String> filteredIDs = new LinkedList<String>();
		for (int i = 0; i < compURIs.size(); i++) {
			ComponentDefinition dnac = null;
			dnac = SBOLDOC.getComponentDefinition(compURIs.get(i));
			if (filterRole.equals("all") || (dnac.getTypes().size() > 0 &&
					SBOLUtility2.convertURIToTypeString(dnac.getTypes().iterator().next()).equals(filterRole))) 
			{
				filteredURIs.add(compURIs.get(i));
				filteredIDs.add(compIDs.get(i));
			}
		}
		setComponents(filteredIDs, filteredURIs);
	}
	
	public void filterComponentsByRole(String filterRole) {
		viewArea.setText("");
		LinkedList<URI> filteredURIs = new LinkedList<URI>();
		LinkedList<String> filteredIDs = new LinkedList<String>();
		for (int i = 0; i < compURIs.size(); i++) {
			ComponentDefinition dnac = null;
			dnac = SBOLDOC.getComponentDefinition(compURIs.get(i));
			if (filterRole.equals("all") || (dnac.getRoles().size() > 0 &&
					SBOLUtility2.convertURIToSOTerm(dnac.getRoles().iterator().next()).equals(filterRole))) 
			{
				filteredURIs.add(compURIs.get(i));
				filteredIDs.add(compIDs.get(i));
			}
		}
		setComponents(filteredIDs, filteredURIs);
	}
	
	public LinkedList<URI> getSelectedURIs() {
		LinkedList<URI> selectedURIs = new LinkedList<URI>();
		int[] selectedIndices = compDefList.getSelectedIndices();
		for (int i = 0; i < selectedIndices.length; i++)
			selectedURIs.add(compURIs.get(selectedIndices[i]));
		return selectedURIs;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) 
	{
		if (e.getSource() == compDefList) 
		{
			viewArea.setText("");
			LinkedList<URI> selectedURIs = getSelectedURIs();
			
			for (URI compURI : selectedURIs) 
			{
				ComponentDefinition dnac = null; 
				dnac = SBOLDOC.getComponentDefinition(compURI);
				if (dnac != null) 
				{
					viewArea.append("Identity:  " + dnac.getIdentity() + "\n");
					if (!dnac.getIdentity().equals(dnac.getPersistentIdentity())) {
						viewArea.append("Persistent Identity:  " + dnac.getPersistentIdentity() + "\n");
					}
					if (dnac.isSetDisplayId()) {
						viewArea.append("Display ID:  " + dnac.getDisplayId() + "\n");
					}
					if (dnac.isSetVersion()) {
						viewArea.append("Version:  " + dnac.getVersion() + "\n");
					}
					if (dnac.isSetWasDerivedFrom()) {
						viewArea.append("Version:  " + dnac.getWasDerivedFrom() + "\n");
					}
					if (dnac.isSetName()) {
						viewArea.append("Name:  " + dnac.getName() + "\n");
					}
					if (dnac.isSetDescription()) {
						viewArea.append("Description:  " + dnac.getDescription() + "\n");
					}
					String types = "";
					for (URI uri : dnac.getTypes()) {
						types = types + SBOLUtility2.convertURIToTypeString(uri) + ", ";
					}
					if (types.length() > 0) {
						viewArea.append("Types:  ");
						viewArea.append(types.substring(0, types.length() - 2) + "\n");
					}
					
					String roles = "";
					for (URI uri : dnac.getRoles()) 
						roles = roles + SBOLUtility2.convertURIToSOTerm(uri) + ", ";
					if (roles.length() > 0) {
						viewArea.append("Roles:  ");
						viewArea.append(roles.substring(0, roles.length() - 2) + "\n");
					}
					
					if (dnac.getComponents().size()>0) {
						try {
							viewArea.append("Components:  ");
							String sortedComponents = "";
							for (Component component : dnac.getSortedComponents()) {
								sortedComponents = sortedComponents + processComponent(dnac,component);
							}
							viewArea.append(sortedComponents + "\n");
						}
						catch (SBOLValidationException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					if (dnac.getSequences().size()>0) 
					{
						String seq = processSequences(dnac.getSequences());
						viewArea.append("DNA Sequence:  ");
						viewArea.append(seq + "\n");
					} 
				} 
			} 
		} 
	}
	
	private static String processComponent(ComponentDefinition dnac,Component component) {
		String componentStr = "";
		ComponentDefinition definition = component.getDefinition();
		if (definition!=null) {
			if (definition.isSetDisplayId()) {
				componentStr = definition.getDisplayId();
			} else {
				componentStr = definition.getIdentity().toString();
			}
		} else {
			if (component.isSetDisplayId()) {
				componentStr = component.getDisplayId();
			} else {
				componentStr = component.getIdentity().toString();
			}
		}
		SequenceAnnotation sequenceAnnotation = dnac.getSequenceAnnotation(component);
		if (sequenceAnnotation != null) {
			if (sequenceAnnotation.getLocations().size()>0) {
				componentStr += " (";
				boolean first = true;
				for (Location location : sequenceAnnotation.getLocations()) {
					String symbol = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
					if (location.isSetOrientation() && location.getOrientation().equals(OrientationType.REVERSECOMPLEMENT)) {
						symbol = GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND;
					}
					if (!first) {
						componentStr += "; ";
					} else {
						first = false;
					}
					if (location instanceof Range) {
						Range range = (Range)location;
						componentStr += symbol + range.getStart() + " to " + symbol + range.getEnd(); 
					} else if (location instanceof Cut) {
						Cut cut = (Cut)location;
						componentStr += symbol + cut.getAt();
					} else if (location instanceof GenericLocation) {
						componentStr += symbol;
					}
				}
				componentStr += ") ";
			}
		}
		return componentStr;
	}
	
	private static String processSequences(Set<Sequence> sequences)
	{
		//String sequence = "";
		for(Sequence s : sequences)
		{
			// TODO: need to figure out a better way for multiple sequences, for now just return first sequence
			return s.getElements();
		}
		return "NA";//sequence;
	}
	
	/*private static String processSequenceConstraints(Set<SequenceConstraint> sc)
	{
		// process restriction (URI)?
		String seqConstraint = "";
		for(SequenceConstraint s : sc)
		{
			if(s != null)
				seqConstraint = seqConstraint + s.getDisplayId() + " subject: " + s.getSubject().getDisplayId() + " object: " + s.getObject().getDisplayId()  + ", ";
			else
				seqConstraint = seqConstraint + "NA"; 
		}
//		seqConstraint = seqConstraint.substring(0, seqConstraint.length() - 2); 
		seqConstraint = seqConstraint.substring(0, seqConstraint.length()); 
		return seqConstraint;
	}
	
	private static String processComponents(Set<Component> components)
	{
		//process MapsTo? 
		String component = "";
		for(Component c : components)
		{
			if(c != null)
				component = component + c.getDisplayId() + " ";
			else
				component = component + "NA"; 
		}
//		component = component.substring(0, component.length() - 2); 
		component = component.substring(0, component.length() ); 
		return component;
	}
	*/
	
//	private static SequenceAnnotation[] sortAnnotations(Set<SequenceAnnotation> unsortedSA) {
//		
//	ArrayList<SequenceAnnotation> sortedSA = new ArrayList<SequenceAnnotation>(); 
//		Map<SequenceAnnotation, Range> saToRange = new HashMap<SequenceAnnotation, Range>();
//		
//		//Insert sort of annotations by starting position
//		// O(|SA| |L|)
//		for (SequenceAnnotation sa : unsortedSA)
//		{
//			Range range = null; 
//			for(Location location : sa.getLocations())
//			{
//				// TODO: Assuming each location has 1 range and each sequenceAnnotation is mapped to 
//				// 1 range for fast access
//				if(location instanceof Range)
//				{
//					range = (Range) location; 
//					saToRange.put(sa, range);
//				}
//			}
//			if(range != null)
//			{
//				sortedSA.add(sa);
//			}
//		}
//		// O(|SA| log |SA|)
//		sortSA(sortedSA, saToRange);
//		
//		return sortedSA.toArray(new SequenceAnnotation[sortedSA.size()]);
//		
//	}
	
//	private static void sortSA(ArrayList<SequenceAnnotation> listOfSA, final Map<SequenceAnnotation, Range> saToRange)
//	{
//		Collections.sort(listOfSA, new Comparator<SequenceAnnotation>()
//				{
//
//					@Override
//					public int compare(SequenceAnnotation o1,
//							SequenceAnnotation o2)
//					{
//						Range range1 = saToRange.get(o1);
//						Range range2 = saToRange.get(o2);
//						
//						if(range1.getStart() > range2.getStart())
//						{
//							return 1;
//						}
//						else if(range1.getStart() < range2.getStart())
//						{
//							return -1;
//						}
//						else if(range1.getEnd() > range2.getEnd())
//						{
//							return 1;
//						}
//						
//						return -1;
//					}
//			
//				});
//	}
//	
//	private String processAnnotations(SequenceAnnotation[] arraySA) 
//	{
//		String annotations = "";
//		for (int k = 0; k < arraySA.length; k++) 
//		{
//			ComponentDefinition subComponent = arraySA[k].getComponent().getDefinition();
//		
//			if (subComponent != null) 
//			{
////				ComponentDefinition resolvedSubComponent = null;
//				annotations = annotations + subComponent.getDisplayId(); 
//			} 
//			else
//			{
//				annotations = annotations + "NA"; 
//			}
//			
//			
//			String symbol;
//			Range range = null;
//			
//			for(Location location : arraySA[k].getLocations())
//			{
//				if(location instanceof Range)
//				{
//					range = (Range) location;
//				}
//			}
//			if (range != null)
//			{
//
//				if(!range.isSetOrientation() || range.getOrientation().toString().equals("inline"))
//				{
//					symbol = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
//				}
//				else
//				{
//					symbol = GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND;
//				}
//			}
//			else
//			{
//				symbol = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
//			}
//			
//			annotations = annotations + " " + symbol + range.getStart() + " to " + symbol + range.getEnd() + ", "; 
//			
//		}
//		if(annotations.isEmpty())
//			annotations = annotations + "NA";
//		annotations = annotations.substring(0, annotations.length() - 2); //removes the , at the end
//		return annotations;
//	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
