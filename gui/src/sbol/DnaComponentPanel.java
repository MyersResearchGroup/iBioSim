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
	private TextArea viewArea;
	private SequenceFeaturePanel featPanel;
	private JList compList = new JList();
	private String filter;
	
	public DnaComponentPanel(HashMap<String, DnaComponent> compMap, TextArea viewArea, 
			SequenceFeaturePanel featPanel, String filter) {
		super(new BorderLayout());
		this.compMap = compMap;
		this.viewArea = viewArea;
		this.featPanel = featPanel;
		this.filter = filter;
		
		compList.addMouseListener(this);
		
		JLabel componentLabel = new JLabel("DNA Components:");
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		this.add(componentLabel, "North");
		this.add(componentScroll, "Center");
	}
	
	public void setComponents(Set<String> compIds) {
		Object[] idObjects = compIds.toArray();
		compList.setListData(idObjects);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == compList) {
			viewArea.setText("");
			Object[] selected = compList.getSelectedValues();
			for (Object o : selected) {
				DnaComponent dnac = compMap.get(o.toString());
				viewArea.append("Name:  " + dnac.getName() + "\n");
				viewArea.append("Description:  " + dnac.getDescription() + "\n");
				viewArea.append("Annotations:  ");
				
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
				//Processing sorted annotations and associated sequence features for display
				String annotations = "";
				LinkedHashSet<String> featIds = new LinkedHashSet<String>();
				for (int k = 0; k < sortedSA.length; k++) {
					for (SequenceFeature sf : sortedSA[k].getFeatures()) {
						annotations = annotations + sf.getName() + " + ";
						if (filterFeature(sf, filter))
							featIds.add(sf.getDisplayId());
					}
					annotations = annotations.substring(0, annotations.length() - 2);
					String sign = sortedSA[k].getStrand();
					if (sign.equals("+"))
						sign = "";
					annotations = annotations + sign + sortedSA[k].getStart() + " to " + sign + sortedSA[k].getStop() + ", "; 
				}
				viewArea.append(annotations.substring(0, annotations.length() - 2) + "\n");
				viewArea.append("DNA Sequence:  " + dnac.getDnaSequence().getDnaSequence() + "\n\n");
				featPanel.setFeatures(featIds);
			}
		} 
	}

	private boolean filterFeature(SequenceFeature sf, String filter) {
		if (filter.equals(""))
			return true;
		HashSet<String> types = new HashSet<String>();
 		for (URI uri : sf.getTypes()) 
			types.add(uri.getFragment());
 		return types.contains(filter);
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
