package backend.sbol.browser;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;

import backend.biomodel.util.GlobalConstants;
import backend.sbol.util.SBOLUtility;

import java.net.URI;
import java.util.*;

public class DNAComponentBrowserPanel extends JPanel implements MouseListener {

	private static final long serialVersionUID = 1L;
	private LinkedList<URI> compURIs;
	private LinkedList<String> compIDs;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver;
	private UseFirstFound<SequenceAnnotation, URI> aggregateAnnoResolver;
	private UseFirstFound<DnaSequence, URI> aggregateSeqResolver;
	
	private JTextArea viewArea;
	private JList compList = new JList();
	
	public DNAComponentBrowserPanel(UseFirstFound<DnaComponent, URI> aggregateCompResolver, UseFirstFound<SequenceAnnotation, URI> aggregateAnnoResolver, 
			UseFirstFound<DnaSequence, URI> aggregateSeqResolver, JTextArea viewArea) {
		super(new BorderLayout());
		this.aggregateCompResolver = aggregateCompResolver;
		this.aggregateAnnoResolver = aggregateAnnoResolver;
		this.aggregateSeqResolver = aggregateSeqResolver;
		this.viewArea = viewArea;
		
		compList.addMouseListener(this);
		
		JLabel componentLabel = new JLabel("DNA Components:");
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		this.add(componentLabel, "North");
		this.add(componentScroll, "Center");
	}
	
	public void setComponents(LinkedList<String> compIDs, LinkedList<URI> compURIs) {
		this.compURIs = compURIs;
		this.compIDs = compIDs;
		Object[] idObjects = compIDs.toArray();
		compList.setListData(idObjects);
	}
	
	public void filterComponents(String filterType) {
		viewArea.setText("");
		LinkedList<URI> filteredURIs = new LinkedList<URI>();
		LinkedList<String> filteredIDs = new LinkedList<String>();
		for (int i = 0; i < compURIs.size(); i++) {
			DnaComponent dnac = null;
			try {
				dnac = aggregateCompResolver.resolve(compURIs.get(i));
			} catch (MergerException e) {
				e.printStackTrace();
				return;
			}
			if (filterType.equals("all") || (dnac.getTypes().size() > 0 &&
					SBOLUtility.convertURIToSOTerm(dnac.getTypes().iterator().next()).equals(filterType))) {
				filteredURIs.add(compURIs.get(i));
				filteredIDs.add(compIDs.get(i));
			}
		}
		setComponents(filteredIDs, filteredURIs);
	}
	
	public LinkedList<URI> getSelectedURIs() {
		LinkedList<URI> selectedURIs = new LinkedList<URI>();
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = 0; i < selectedIndices.length; i++)
			selectedURIs.add(compURIs.get(selectedIndices[i]));
		return selectedURIs;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == compList) {
			viewArea.setText("");
			LinkedList<URI> selectedURIs = getSelectedURIs();
			for (URI compURI : selectedURIs) {
				DnaComponent dnac = null;
				try {
					dnac = aggregateCompResolver.resolve(compURI);
				} catch (MergerException e1) {
					e1.printStackTrace();
				}
				if (dnac != null) {
					
					viewArea.append("Display ID:  " + dnac.getDisplayId() + "\n");
					
					if (dnac.getName() != null && !dnac.getName().equals(""))
						viewArea.append("Name:  " + dnac.getName() + "\n");
					else
						viewArea.append("Name:  NA\n");
					
					if (dnac.getDescription() != null && !dnac.getDescription().equals(""))
						viewArea.append("Description:  " + dnac.getDescription() + "\n");
					else 
						viewArea.append("Description:  NA\n");
					
					LinkedList<SequenceAnnotation> unsortedSA = new LinkedList<SequenceAnnotation>();
					if (dnac.getAnnotations() != null) {
						for (SequenceAnnotation sa : dnac.getAnnotations()) {
							SequenceAnnotation resolvedSA = null;
							try {
								resolvedSA = aggregateAnnoResolver.resolve(sa.getURI());
							} catch (MergerException e1) {
								e1.printStackTrace();
							}
							if (resolvedSA != null)
								unsortedSA.add(resolvedSA);
							else
								unsortedSA.add(sa);
						}
					}
					if (unsortedSA.size() > 0) {
						SequenceAnnotation[] sortedSA = sortAnnotations(unsortedSA);
						String annotations = processAnnotations(sortedSA);
						viewArea.append("Annotations:  ");
						viewArea.append(annotations + "\n");
					} else 
						viewArea.append("Annotations:  NA\n");
					
					viewArea.append("Types:  ");
					String types = "";
					for (URI uri : dnac.getTypes()) 
						types = types + SBOLUtility.convertURIToSOTerm(uri) + ", ";
					if (types.length() > 0)
						viewArea.append(types.substring(0, types.length() - 2) + "\n");
					else
						viewArea.append("NA\n");
					DnaSequence seq = dnac.getDnaSequence();
					if (seq != null) {
						DnaSequence resolvedSeq = null;
						try {
							resolvedSeq = aggregateSeqResolver.resolve(seq.getURI());
						} catch (MergerException e1) {
							e1.printStackTrace();
						}
						if (resolvedSeq != null)
							viewArea.append("DNA Sequence:  " + resolvedSeq.getNucleotides() + "\n\n");
						else 
							viewArea.append("DNA Sequence:  " + seq.getNucleotides() + "\n\n");
					} else
						viewArea.append("DNA Sequence:  NA\n\n");
				}
			}
		} 
	}
	
	private static SequenceAnnotation[] sortAnnotations(LinkedList<SequenceAnnotation> unsortedSA) {
		SequenceAnnotation[] sortedSA = new SequenceAnnotation[unsortedSA.size()];
		int n = 0;
		for (SequenceAnnotation sa : unsortedSA) {
			sortedSA[n] = sa;
			n++;
		}
		//Insert sort of annotations by starting position
		for (int j = 1; j < sortedSA.length; j++) {
			SequenceAnnotation keyAnnotation = sortedSA[j];
			int key = keyAnnotation.getBioStart();
			int i = j - 1;
			while (i >= 0 && sortedSA[i].getBioStart() > key) {
				sortedSA[i + 1] = sortedSA[i];
				i = i - 1;
			}
			sortedSA[i + 1] = keyAnnotation;
		}
		return sortedSA;
	}
	
	private String processAnnotations(SequenceAnnotation[] arraySA) {
		String annotations = "";
		for (int k = 0; k < arraySA.length; k++) {
			DnaComponent subComponent = arraySA[k].getSubComponent();
			if (subComponent != null) {
				DnaComponent resolvedSubComponent = null;
				try {
					resolvedSubComponent = aggregateCompResolver.resolve(subComponent.getURI());
				} catch (MergerException e) {
					e.printStackTrace();
				}
				if (resolvedSubComponent != null)
					annotations = annotations + resolvedSubComponent.getDisplayId();
				else
					annotations = annotations + subComponent.getDisplayId();
			} else
				annotations = annotations + "NA"; 
			String symbol;
			if (arraySA[k].getStrand() != null)
				symbol = arraySA[k].getStrand().getSymbol();
			else
				symbol = GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
			annotations = annotations + " " + symbol + arraySA[k].getBioStart() + " to " + symbol + arraySA[k].getBioEnd() + ", "; 
			
		}
		annotations = annotations.substring(0, annotations.length() - 2);
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
