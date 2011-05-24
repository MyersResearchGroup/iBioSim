package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.libSBOLj.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import main.Gui;

public class DnaComponentPanel extends JPanel implements MouseListener {

	private HashMap<String, DnaComponent> compMap;
	private TextArea viewer;
	private SequenceFeaturePanel featPanel;
	private JList compList = new JList();
	
	public DnaComponentPanel(HashMap<String, DnaComponent> compMap, TextArea viewer, SequenceFeaturePanel featPanel) {
		super();
		this.compMap = compMap;
		this.viewer = viewer;
		this.featPanel = featPanel;
		
		compList.addMouseListener(this);
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		this.add(componentScroll);
		
	}
	
	public void setComponents(Set<String> compIds) {
		Object[] idObjects = compIds.toArray();
		compList.setListData(idObjects);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == compList) {
			viewer.setText("");
			Object[] selected = compList.getSelectedValues();
			for (Object o : selected) {
				DnaComponent dnac = compMap.get(o.toString());
				viewer.append("Name:  " + dnac.getName() + "\n");
				viewer.append("Description:  " + dnac.getDescription() + "\n");
				viewer.append("Annotations:  ");
				
				//Creation of to-be-sorted annotation array
				SequenceAnnotation[] sortedSA = new SequenceAnnotation[dnac.getAnnotations().size()];
				int n = 0;
				for (SequenceAnnotation sa : dnac.getAnnotations()) {
					sortedSA[n] = sa;
					n++;
				}
				//Insert sort of annotations by starting position
				for (int j = 1; j < sortedSA.length; j++) {
					SequenceAnnotation keyAnnotation = sortedSA[j];
					int key = keyAnnotation.getStart();
					int i = j - 1;
					while (i >= 0 && sortedSA[i].getStart() > key) {
						sortedSA[i + 1] = sortedSA[i];
						i = i - 1;
					}
					sortedSA[i + 1] = keyAnnotation;
				}
				//Processing sorted annotations for display
				String annotations = "";
				LinkedHashSet<String> featIds = new LinkedHashSet<String>();
				for (int k = 0; k < sortedSA.length; k++) {
					for (SequenceFeature sf : sortedSA[k].getFeatures()) {
						annotations = annotations + sf.getName() + " + ";
						featIds.add(sf.getDisplayId());
					}
					annotations = annotations.substring(0, annotations.length() - 2);
					String sign = sortedSA[k].getStrand();
					if (sign.equals("+"))
						sign = "";
					annotations = annotations + sign + sortedSA[k].getStart() + " to " + sign + sortedSA[k].getStop() + ", "; 
				}
				viewer.append(annotations.substring(0, annotations.length() - 2) + "\n");
				viewer.append("DNA Sequence:  " + dnac.getDnaSequence().getDnaSequence() + "\n\n");
				featPanel.setFeatures(featIds);
			}
		} 
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
