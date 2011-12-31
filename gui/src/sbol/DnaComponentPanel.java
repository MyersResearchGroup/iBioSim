package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.core.*;

import java.net.URI;
import java.util.*;

public class DnaComponentPanel extends JPanel implements MouseListener {

	private LinkedList<String> compURIs;
	private HashMap<String, DnaComponent> compMap;
	private JTextArea viewArea;
	private JList compList = new JList();
	
	public DnaComponentPanel(HashMap<String, DnaComponent> compMap, JTextArea viewArea) {
		super(new BorderLayout());
		this.compMap = compMap;
		this.viewArea = viewArea;
		
		compList.addMouseListener(this);
		
		JLabel componentLabel = new JLabel("DNA Components:");
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
//		componentScroll.setMinimumSize(new Dimension(276, 50));
//		componentScroll.setPreferredSize(new Dimension(276, 50));
		componentScroll.setViewportView(compList);
		
		this.add(componentLabel, "North");
		this.add(componentScroll, "Center");
	}
	
	public void setComponents(LinkedList<String> compIds, LinkedList<String> compURIs) {
		this.compURIs = compURIs;
		Object[] idObjects = compIds.toArray();
		compList.setListData(idObjects);
	}
	
	public String[] getSelectedURIs() {
		int[] selectedIndices = compList.getSelectedIndices();
		String[] selectedURIs = new String[selectedIndices.length];
		for (int i = 0; i < selectedURIs.length; i++)
			selectedURIs[i] = compURIs.get(selectedIndices[i]).toString();
		return selectedURIs;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == compList) {
			viewArea.setText("");
			String[] selectedURIs = getSelectedURIs();
			for (String compURI : selectedURIs) {
				if (compMap.containsKey(compURI)) {
					DnaComponent dnac = compMap.get(compURI);
					
					if (dnac.getName() != null && !dnac.getName().equals(""))
						viewArea.append("Name:  " + dnac.getName() + "\n");
					else
						viewArea.append("Name:  NA\n");
					
					if (dnac.getDescription() != null && !dnac.getDescription().equals(""))
						viewArea.append("Description:  " + dnac.getDescription() + "\n");
					else 
						viewArea.append("Description:  NA\n");
					
					if (dnac.getAnnotations() != null && dnac.getAnnotations().size() > 0) {
						viewArea.append("Annotations:  ");
						SequenceAnnotation[] sortedSA = sortAnnotations(dnac.getAnnotations());
						String annotations = processAnnotations(sortedSA);
						viewArea.append(annotations + "\n");
					} else 
						viewArea.append("Annotations:  NA\n");
					viewArea.append("Types:  ");
					String types = "";
					for (URI uri : dnac.getTypes()) {
						if (!uri.getFragment().equals("SequenceFeature"))
							types = types + uri.getFragment() + ", ";
					}
					if (types.length() > 0)
						viewArea.append(types.substring(0, types.length() - 2) + "\n");
					else
						viewArea.append("NA\n");
					
					if (dnac.getDnaSequence() != null && dnac.getDnaSequence().getNucleotides() != null && !dnac.getDnaSequence().getNucleotides().equals(""))
						viewArea.append("DNA Sequence:  " + dnac.getDnaSequence().getNucleotides() + "\n");
					else
						viewArea.append("DNA Sequence:  NA\n");
					
					
				}
			}
		} 
	}
	
	private SequenceAnnotation[] sortAnnotations(java.util.Collection<SequenceAnnotation> unsortedSA) {
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
			if (subComponent != null) 
				annotations = annotations + compMap.get(subComponent.getURI().toString()).getDisplayId();
			else
				annotations = annotations + "NA";
			String sign = arraySA[k].getStrand();
			annotations = annotations + " " + sign + arraySA[k].getBioStart() + " to " + sign + arraySA[k].getBioEnd() + ", "; 
			
		}
		annotations = annotations.substring(0, annotations.length() - 2);
		return annotations;
	}
	
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
