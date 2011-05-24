package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.libSBOLj.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import main.Gui;

public class SequenceFeaturePanel extends JPanel implements MouseListener {

	private HashMap<String, SequenceFeature> featMap;
	private TextArea viewer;
	private JList featList = new JList();
	
	public SequenceFeaturePanel(HashMap<String, SequenceFeature> featMap, TextArea viewer) {
		super();
		this.featMap = featMap;
		this.viewer = viewer;
		
		featList.addMouseListener(this);
		
		JScrollPane featureScroll = new JScrollPane();		
		featureScroll.setMinimumSize(new Dimension(260, 200));
		featureScroll.setPreferredSize(new Dimension(276, 132));
		featureScroll.setViewportView(featList);
		this.add(featureScroll);
	}
	
	public void setFeatures(Set<String> featIds) {
		Object[] idObjects = featIds.toArray();
		featList.setListData(idObjects);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == featList) {
			viewer.setText("");
			Object[] selected = featList.getSelectedValues();
			for (Object o : selected) {
				SequenceFeature sf = featMap.get(o.toString());
				viewer.append("Name:  " + sf.getName() + "\n");
				viewer.append("Description:  " + sf.getDescription() + "\n");
				viewer.append("Types:  ");
				String types = "";
				for (URI uri : sf.getTypes()) {
					if (!uri.getFragment().equals("SequenceFeature"))
						types = types + uri.getFragment() + ", ";
				}
				viewer.append(types.substring(0, types.length() - 2) + "\n");
				viewer.append("DNA Sequence:  " + sf.getDnaSequence().getDnaSequence() + "\n\n");
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
