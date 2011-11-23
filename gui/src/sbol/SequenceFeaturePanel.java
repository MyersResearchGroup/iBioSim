package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.libSBOLj.*;
import org.sbolstandard.xml.*;

import java.net.URI;
import java.util.*;

public class SequenceFeaturePanel extends JPanel implements MouseListener {

	private HashMap<String, SequenceFeature> featMap;
	private JTextArea viewArea;
	private JList featList = new JList();
	
	public SequenceFeaturePanel(HashMap<String, SequenceFeature> featMap, JTextArea viewArea) {
		super(new BorderLayout());
		this.featMap = featMap;
		this.viewArea = viewArea;
		
		featList.addMouseListener(this);
		
		JLabel featureLabel = new JLabel("Sequence Features:");
		
		JScrollPane featureScroll = new JScrollPane();		
		featureScroll.setMinimumSize(new Dimension(260, 200));
		featureScroll.setPreferredSize(new Dimension(276, 132));
		featureScroll.setViewportView(featList);
		
		this.add(featureLabel, "North");
		this.add(featureScroll, "Center");
	}
	
	public void setFeatures(Set<String> featIds) {
		Object[] idObjects = featIds.toArray();
		featList.setListData(idObjects);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == featList) {
			viewArea.setText("");
			Object[] selected = featList.getSelectedValues();
			for (Object o : selected) {
				SequenceFeature sf = featMap.get(o.toString());
				viewArea.append("Name:  " + sf.getName() + "\n");
				viewArea.append("Description:  " + sf.getDescription() + "\n");
				viewArea.append("Types:  ");
				String types = "";
				for (URI uri : sf.getTypes()) {
					if (!uri.getFragment().equals("SequenceFeature"))
						types = types + uri.getFragment() + ", ";
				}
				viewArea.append(types.substring(0, types.length() - 2) + "\n");
				viewArea.append("DNA Sequence:  " + sf.getDnaSequence().getDnaSequence() + "\n\n");
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
