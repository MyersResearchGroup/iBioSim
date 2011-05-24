package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.libSBOLj.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import main.Gui;

public class LibraryPanel extends JPanel implements MouseListener {

	private HashMap<String, Library> libMap;
	private TextArea viewer;
	private DnaComponentPanel compPanel;
	private SequenceFeaturePanel featPanel;
	private JList libList = new JList();
	
	public LibraryPanel(HashMap<String, Library> libMap, TextArea viewer, DnaComponentPanel compPanel, SequenceFeaturePanel featPanel) {
		super();
		this.libMap = libMap;
		this.viewer = viewer;
		this.compPanel = compPanel;
		this.featPanel = featPanel;
		
		libList.addMouseListener(this);
		
		JScrollPane libraryScroll = new JScrollPane();
		libraryScroll.setMinimumSize(new Dimension(260, 200));
		libraryScroll.setPreferredSize(new Dimension(276, 132));
		libraryScroll.setViewportView(libList);
		this.add(libraryScroll);
		
	}
	
	public void setLibraries(Set<String> libIds) {
		Object[] idObjects = libIds.toArray();
		libList.setListData(idObjects);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == libList) {
			viewer.setText("");
			Object[] selected = libList.getSelectedValues();
			for (Object o : selected) {
				Library lib = libMap.get(o.toString());
				viewer.append("Name:  " + lib.getName() + "\n");
				viewer.append("Description:  " + lib.getDescription() + "\n\n");
				
				HashSet<String> compIds = new HashSet<String>();
				for (DnaComponent dnac : lib.getComponents())
					compIds.add(dnac.getDisplayId());
				compPanel.setComponents(compIds);
				
				HashSet<String> featIds = new HashSet<String>();
				for (SequenceFeature sf : lib.getFeatures()) 
					featIds.add(sf.getDisplayId());
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
