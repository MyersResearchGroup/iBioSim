package sbol.browser;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

//import org.sbolstandard.core.DnaComponent;
//import org.sbolstandard.core.DnaSequence;
//import org.sbolstandard.core.MergerException;
import org.sbolstandard.core2.SequenceAnnotation;
//import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;
import org.sbolstandard.core2.*;

import sbol.util.SBOLUtility2;
import biomodel.util.GlobalConstants;
import java.net.URI;
import java.util.*;

public class ComponentDefinitionBrowserPanel extends JPanel implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SBOLDocument SBOLDOC; 
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
			dnac = SBOLDOC.getComponentDefinition(compURIs.get(i));
			if (filterType.equals("all") || (dnac.getRoles().size() > 0 &&
					SBOLUtility2.convertURIToSOTerm(dnac.getRoles().iterator().next()).equals(filterType))) 
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
					viewArea.append("Display ID:  " + dnac.getDisplayId() + "\n");
					
					if (dnac.getName() != null && !dnac.getName().equals(""))
						viewArea.append("Name:  " + dnac.getName() + "\n");
					else
						viewArea.append("Name:  NA\n");
					
					if (dnac.getDescription() != null && !dnac.getDescription().equals(""))
						viewArea.append("Description:  " + dnac.getDescription() + "\n");
					else 
						viewArea.append("Description:  NA\n");
					
					
					if (dnac.getSequenceAnnotations().size() > 0) 
					{
						//ascending order of range
						SequenceAnnotation[] sortedSA = sortAnnotations(dnac.getSequenceAnnotations()); 
						String annotations = processAnnotations(sortedSA); //append sa information
						viewArea.append("Annotations:  ");
						viewArea.append(annotations + "\n");
					} 
					else 
						viewArea.append("Annotations:  NA\n");
					
					viewArea.append("Types:  ");
					String types = "";
					for (URI uri : dnac.getTypes()) {
						types = types + SBOLUtility2.convertURIToTypeString(uri) + ", ";
					}
					if (types.length() > 0)
						viewArea.append(types.substring(0, types.length() - 2) + "\n");
					else 
						viewArea.append("NA\n");
					
					viewArea.append("Roles:  ");
					String roles = "";
					for (URI uri : dnac.getRoles()) 
						roles = roles + SBOLUtility2.convertURIToSOTerm(uri) + ", ";
					if (roles.length() > 0)
						viewArea.append(roles.substring(0, roles.length() - 2) + "\n");
					else
						viewArea.append("NA\n");
					
					if (dnac.getSequences() != null) 
					{
//						Set<Sequence> sequences = new HashSet<Sequence>();
//						System.out.println("Found a sequence: " + dnac.getSequenceURIs());
//						for(URI s : dnac.getSequenceURIs())
//						{
//							sequences.add(SBOLDOC.getSequence(s));
//						}
						
						String seq = processSequences(dnac.getSequences());
//						String seq = processSequences(sequences);
						
						viewArea.append("DNA Sequence:  ");
						viewArea.append(seq + "\n");
					} 
					else
						viewArea.append("DNA Sequence:  NA\n\n");
					
					
					/*if ( dnac.getSequenceConstraints() != null) 
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
					 */
				} // end of each compDef
			} // end of iterating through all selectedURIs
		} 
	}
	
	private static String processSequences(Set<Sequence> sequences)
	{
		//String sequence = "";
		for(Sequence s : sequences)
		{
			// TODO: need to figure out a better way for multiple sequences, for now just return first sequence
			return s.getElements();
			/*
			if(s != null)
				sequence = sequence + s.getElements() + ", ";
			else
				sequence = sequence + "NA";
				*/ 
		}
		//sequence = sequence.substring(0, sequence.length() - 2); //this will remove the extra "," at the end of the string
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
	
	private static SequenceAnnotation[] sortAnnotations(Set<SequenceAnnotation> unsortedSA) {
		
	ArrayList<SequenceAnnotation> sortedSA = new ArrayList<SequenceAnnotation>(); 
		Map<SequenceAnnotation, Range> saToRange = new HashMap<SequenceAnnotation, Range>();
		
		//Insert sort of annotations by starting position
		// O(|SA| |L|)
		for (SequenceAnnotation sa : unsortedSA)
		{
			Range range = null; 
			for(Location location : sa.getLocations())
			{
				// TODO: Assuming each location has 1 range and each sequenceAnnotation is mapped to 
				// 1 range for fast access
				if(location instanceof Range)
				{
					range = (Range) location; 
					saToRange.put(sa, range);
				}
			}
			if(range != null)
			{
				sortedSA.add(sa);
			}
		}
		// O(|SA| log |SA|)
		sortSA(sortedSA, saToRange);
		
		return sortedSA.toArray(new SequenceAnnotation[sortedSA.size()]);
		
	}
	
	private static void sortSA(ArrayList<SequenceAnnotation> listOfSA, final Map<SequenceAnnotation, Range> saToRange)
	{
		Collections.sort(listOfSA, new Comparator<SequenceAnnotation>()
				{

					@Override
					public int compare(SequenceAnnotation o1,
							SequenceAnnotation o2)
					{
						Range range1 = saToRange.get(o1);
						Range range2 = saToRange.get(o2);
						
						if(range1.getStart() > range2.getStart())
						{
							return 1;
						}
						else if(range1.getStart() < range2.getStart())
						{
							return -1;
						}
						else if(range1.getEnd() > range2.getEnd())
						{
							return 1;
						}
						
						return -1;
					}
			
				});
	}
	
	private String processAnnotations(SequenceAnnotation[] arraySA) 
	{
		String annotations = "";
		for (int k = 0; k < arraySA.length; k++) 
		{
			ComponentDefinition subComponent = arraySA[k].getComponent().getDefinition();
		
			if (subComponent != null) 
			{
//				ComponentDefinition resolvedSubComponent = null;
				annotations = annotations + subComponent.getDisplayId(); 
			} 
			else
			{
				annotations = annotations + "NA"; 
			}
			
			
			String symbol;
			Range range = null;
			
			for(Location location : arraySA[k].getLocations())
			{
				if(location instanceof Range)
				{
					range = (Range) location;
				}
			}
			if (range != null)
			{

				if(!range.isSetOrientation() || range.getOrientation().toString().equals("inline"))
				{
					symbol = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
				}
				else
				{
					symbol = GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND;
				}
			}
			else
			{
				symbol = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
			}
			
			annotations = annotations + " " + symbol + range.getStart() + " to " + symbol + range.getEnd() + ", "; 
			
		}
		if(annotations.isEmpty())
			annotations = annotations + "NA";
		annotations = annotations.substring(0, annotations.length() - 2); //removes the , at the end
		return annotations;
	}
	
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
