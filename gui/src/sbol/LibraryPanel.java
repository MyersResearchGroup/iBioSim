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
	private TextArea viewArea;
	private DnaComponentPanel compPanel;
	private SequenceFeaturePanel featPanel;
	private JList libList = new JList();
	
	public LibraryPanel(HashMap<String, Library> libMap, TextArea viewArea, DnaComponentPanel compPanel, SequenceFeaturePanel featPanel) {
		super(new BorderLayout());
		this.libMap = libMap;
		this.viewArea = viewArea;
		this.compPanel = compPanel;
		this.featPanel = featPanel;
		
		libList.addMouseListener(this);
		
		JLabel libraryLabel = new JLabel("Libraries:");
		
		
		JScrollPane libraryScroll = new JScrollPane();
		libraryScroll.setMinimumSize(new Dimension(260, 200));
		libraryScroll.setPreferredSize(new Dimension(276, 132));
		libraryScroll.setViewportView(libList);
		
		this.add(libraryLabel, "North");
		this.add(libraryScroll, "Center");
	}
	
	public void setLibraries(Set<String> libIds) {
		Object[] idObjects = libIds.toArray();
		libList.setListData(idObjects);
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == libList) {
			viewArea.setText("");
			Object[] selected = libList.getSelectedValues();
			for (Object o : selected) {
				Library lib = libMap.get(o.toString());
				viewArea.append("Name:  " + lib.getName() + "\n");
				viewArea.append("Description:  " + lib.getDescription() + "\n\n");
				
				String[] compIdArray = new String[lib.getComponents().size()];
				int n = 0;
				for (DnaComponent dnac : lib.getComponents()) {
					compIdArray[n] = dnac.getDisplayId();
					n++;
				}
				LinkedHashSet<String> compIds = lexoSort(compIdArray);
				compPanel.setComponents(compIds);
				
				String[] featIdArray = new String[lib.getFeatures().size()];
				n = 0;
				for (SequenceFeature sf : lib.getFeatures()) {
					featIdArray[n] = sf.getDisplayId();
					n++;
				}
				LinkedHashSet<String> featIds = lexoSort(featIdArray);
				featPanel.setFeatures(featIds);
			}
		}
	}
	//Sorts string array lexographically
	private LinkedHashSet<String> lexoSort(String[] sortingArray) {
		for (int j = 1; j < sortingArray.length; j++) {
			String key = sortingArray[j];
			int i = j - 1;
			while (i >= 0 && sortingArray[i].compareTo(key) > 0) {
				sortingArray[i + 1] = sortingArray[i];
				i = i - 1;
			}
			sortingArray[i + 1] = key;
		}
		LinkedHashSet<String> sortedSet = new LinkedHashSet<String>();
		for (int n = 0; n < sortingArray.length; n++)
			sortedSet.add(sortingArray[n]);
		return sortedSet;
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
