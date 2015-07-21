package sbol.browser;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

//import org.sbolstandard.core.DnaComponent;
//import org.sbolstandard.core.DnaSequence;
//import org.sbolstandard.core.MergerException;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;
//import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;
import org.sbolstandard.core2.*;

import sbol.util.SBOLUtility2;
import biomodel.util.GlobalConstants;

import java.net.URI;
import java.util.*;

public class ComponentDefinitionBrowserPanel extends JPanel implements MouseListener {

	private static SBOLDocument SBOLDOC; 
//	private static final long serialVersionUID = 1L;
	private LinkedList<URI>    compURIs;
	private LinkedList<String> compIDs;
//	private UseFirstFound<DnaComponent, URI> aggregateCompResolver;
//	private UseFirstFound<SequenceAnnotation, URI> aggregateAnnoResolver;
//	private UseFirstFound<DnaSequence, URI> aggregateSeqResolver;
	
	private JTextArea viewArea;
	private JList compDefList = new JList();
	
	public ComponentDefinitionBrowserPanel(SBOLDocument sbolDoc, JTextArea viewArea) {
		super(new BorderLayout());
		this.SBOLDOC = sbolDoc; 
//		this.aggregateCompResolver = aggregateCompResolver;
//		this.aggregateAnnoResolver = aggregateAnnoResolver;
//		this.aggregateSeqResolver = aggregateSeqResolver;
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
	
	public void filterComponents(String filterType) {
		viewArea.setText("");
		LinkedList<URI> filteredURIs = new LinkedList<URI>();
		LinkedList<String> filteredIDs = new LinkedList<String>();
		for (int i = 0; i < compURIs.size(); i++) {
			ComponentDefinition dnac = null;
			try {
//				dnac = aggregateCompResolver.resolve(compURIs.get(i));
				dnac = SBOLDOC.getComponentDefinition(compURIs.get(i));
			} catch (SBOLValidationException e) {
				e.printStackTrace();
				return;
			}
			if (filterType.equals("all") || (dnac.getTypes().size() > 0 &&
					SBOLUtility2.convertURIToSOTerm(dnac.getTypes().iterator().next()).equals(filterType))) {
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
				try 
				{
					dnac = SBOLDOC.getComponentDefinition(compURI);
				} 
				catch (SBOLValidationException e1) 
				{
					e1.printStackTrace();
				}
				if (dnac != null) 
				{
					viewArea.append("Display ID:  " + dnac.getDisplayId() + "\n");
					
					if (dnac.getName() != null && !dnac.getName().equals(""))
						viewArea.append("Name:  " + dnac.getName() + "\n");
					else
						viewArea.append("Name:  NA\n");
					
					if (dnac.getDescription() != null && !dnac.getDescription().equals(""))
						viewArea.append("Description:  " + dnac.getDescription() + "\n");
					else 
						viewArea.append("Description:  NA\n");
					
					//TODO: if all sequence annotation are added into one global sboldoc, is there a need to resolve any conflicting URIs? 
//					LinkedList<SequenceAnnotation> unsortedSA = new LinkedList<SequenceAnnotation>();
//					if (dnac.getAnnotations() != null) {
//						for (SequenceAnnotation sa : dnac.getAnnotations()) {
//							SequenceAnnotation resolvedSA = null;
//							try {
//								resolvedSA = aggregateAnnoResolver.resolve(sa.getURI());
//							} catch (MergerException e1) {
//								e1.printStackTrace();
//							}
//							if (resolvedSA != null)
//								unsortedSA.add(resolvedSA);
//							else
//								unsortedSA.add(sa);
//						}
//					}
//					if (unsortedSA.size() > 0) {
//						SequenceAnnotation[] sortedSA = sortAnnotations(unsortedSA);
//						String annotations = processAnnotations(sortedSA);
//						viewArea.append("Annotations:  ");
//						viewArea.append(annotations + "\n");
//					} else 
//						viewArea.append("Annotations:  NA\n");
					
					if (dnac.getSequenceAnnotations().size() == 0) 
					{
						//TODO: how to sort different type of locations?
						//ascending order of range
//						SequenceAnnotation[] sortedSA = sortAnnotations(dnac.getSequenceAnnotations()); 
//						String annotations = processAnnotations(sortedSA); //append sa information
						viewArea.append("Sequence Annotations:  ");
//						viewArea.append(annotations + "\n");
					} 
					else 
						viewArea.append("Sequence Annotations:  NA\n");
					
					viewArea.append("Roles:  ");
					String roles = "";
					for (URI uri : dnac.getRoles()) 
						roles = roles + SBOLUtility2.convertURIToSOTerm(uri) + ", ";
					if (roles.length() > 0)
						viewArea.append(roles.substring(0, roles.length() - 2) + "\n");
					else
						viewArea.append("NA\n");
					
//					DnaSequence seq = dnac.getDnaSequence();
//					if (seq != null) {
//						DnaSequence resolvedSeq = null;
//						try {
//							resolvedSeq = aggregateSeqResolver.resolve(seq.getURI());
//						} catch (MergerException e1) {
//							e1.printStackTrace();
//						}
//						if (resolvedSeq != null)
//							viewArea.append("DNA Sequence:  " + resolvedSeq.getNucleotides() + "\n\n");
//						else 
//							viewArea.append("DNA Sequence:  " + seq.getNucleotides() + "\n\n");
//					} else
//						viewArea.append("DNA Sequence:  NA\n\n");
					
					//TODO: how do you treat multiple sequences when core had one sequence to display?
					if (dnac.getSequences() != null) 
					{
						String seq = processSequences(dnac.getSequences());
						viewArea.append("Sequences:  ");
						viewArea.append(seq + "\n");
					} 
					else
						viewArea.append("Sequence:  NA\n\n");
					
					if ( dnac.getSequenceConstraints() != null) 
					{
						String seqCon = processSequenceConstraints(dnac.getSequenceConstraints());
						viewArea.append("SequenceConstraints: ");
						viewArea.append(seqCon + "\n");
					}
					else
						viewArea.append("SequenceConstraint:  NA\n\n");
					
					if ( dnac.getComponents() != null) 
					{
						
						String comp = processComponents(dnac.getComponents());
						viewArea.append("Components: ");
						viewArea.append(comp + "\n");
					}
					else
						viewArea.append("Component:  NA\n\n");
				} // end of each compDef
			} // end of iterating through all selectedURIs
		} 
	}
	
	private static String processSequences(Set<Sequence> sequences)
	{
		String sequence = "";
		for(Sequence s : sequences)
		{
			//TODO: should we add this level of checking for each sequence == null?
			if(s != null)
				sequence = sequence + s.getDisplayId() + " " + s.getElements() + ", ";
			else
				sequence = sequence + "NA"; 
		}
		
		sequence = sequence.substring(0, sequence.length() - 2); //this will remove the extra "," at the end of the string
		return sequence;
	}
	
	private static String processSequenceConstraints(Set<SequenceConstraint> sc)
	{
		//TODO: Do we want to process restriction (URI)?
		String seqConstraint = "";
		for(SequenceConstraint s : sc)
		{
			if(s != null)
				seqConstraint = seqConstraint + s.getDisplayId() + " subject: " + s.getSubject().getDisplayId() + " object: " + s.getObject().getDisplayId()  + ", ";
			else
				seqConstraint = seqConstraint + "NA"; 
		}
		//TODO: what does this do seqConstraint.length() - 2?
//		seqConstraint = seqConstraint.substring(0, seqConstraint.length() - 2); 
		seqConstraint = seqConstraint.substring(0, seqConstraint.length()); 
		return seqConstraint;
	}
	
	private static String processComponents(Set<Component> components)
	{
		//TODO: Do we want to process MapsTo? 
		String component = "";
		for(Component c : components)
		{
			if(c != null)
				component = component + c.getDisplayId() + " ";
			else
				component = component + "NA"; 
		}
		//TODO: what does this do seqConstraint.length() - 2?
//		component = component.substring(0, component.length() - 2); 
		component = component.substring(0, component.length() ); 
		return component;
	}
	
	private static String sortLocations(Set<Location> locations)
	{
		String location = "";
		for(Location l : locations)
		{
			if(l instanceof Range)
			{
				Range r = (Range) l; 
			}
			else if(l instanceof Cut)
			{
				
			}
			else if(l instanceof GenericLocation)
			{
				
			}
			else
				location = location + "NA";
		}
		
		return location; 
	}
	
//	private static SequenceAnnotation[] sortAnnotations(Set<SequenceAnnotation> unsortedSA) {
//		SequenceAnnotation[] sortedSA = new SequenceAnnotation[unsortedSA.size()];
//		int n = 0;
//		for (SequenceAnnotation sa : unsortedSA) {
//			sortedSA[n] = sa;
//			n++;
//		}
//		//Insert sort of annotations by starting position
//		for (int j = 1; j < sortedSA.length; j++) {
//			SequenceAnnotation keyAnnotation = sortedSA[j];
//			int key = keyAnnotation.getBioStart();
//			int i = j - 1;
//			while (i >= 0 && sortedSA[i].getBioStart() > key) {
//				sortedSA[i + 1] = sortedSA[i];
//				i = i - 1;
//			}
//			sortedSA[i + 1] = keyAnnotation;
//		}
//		return sortedSA;
//		
//	}
//	
//	private String processAnnotations(SequenceAnnotation[] arraySA) {
//		String annotations = "";
//		for (int k = 0; k < arraySA.length; k++) {
//			DnaComponent subComponent = arraySA[k].getSubComponent();
//			if (subComponent != null) {
//				DnaComponent resolvedSubComponent = null;
//				try {
//					resolvedSubComponent = aggregateCompResolver.resolve(subComponent.getURI());
//				} catch (SBOLValidationException e) {
//					e.printStackTrace();
//				}
//				if (resolvedSubComponent != null)
//					annotations = annotations + resolvedSubComponent.getDisplayId();
//				else
//					annotations = annotations + subComponent.getDisplayId();
//			} else
//				annotations = annotations + "NA"; 
//			String symbol;
//			if (arraySA[k].getStrand() != null)
//				symbol = arraySA[k].getStrand().getSymbol();
//			else
//				symbol = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
//			annotations = annotations + " " + symbol + arraySA[k].getBioStart() + " to " + symbol + arraySA[k].getBioEnd() + ", "; 
//			
//		}
//		annotations = annotations.substring(0, annotations.length() - 2);
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
