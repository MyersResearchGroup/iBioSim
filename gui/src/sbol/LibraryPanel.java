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
	private HashMap<String, DnaComponent> compMap;
	private HashMap<String, SequenceFeature> featMap;
	private JTextArea viewArea;
	private DnaComponentPanel compPanel;
	private SequenceFeaturePanel featPanel;
	private JList libList = new JList();
	private String filter;
	
	public LibraryPanel(HashMap<String, Library> libMap, HashMap<String, DnaComponent> compMap, 
			HashMap<String, SequenceFeature> featMap, JTextArea viewArea, DnaComponentPanel compPanel, 
			SequenceFeaturePanel featPanel, String filter) {
		super(new BorderLayout());
		this.libMap = libMap;
		this.compMap = compMap;
		this.featMap = featMap;
		this.viewArea = viewArea;
		this.compPanel = compPanel;
		this.featPanel = featPanel;
		this.filter = filter;
		
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
			Library lib = libMap.get(selected[0].toString());
			viewArea.append("Name:  " + lib.getName() + "\n");
			viewArea.append("Description:  " + lib.getDescription() + "\n\n");

			String[] compIdArray = new String[lib.getComponents().size()];
			int n = 0;
			for (DnaComponent dnac : lib.getComponents()) {
				compIdArray[n] = dnac.getDisplayId();
				n++;
				compMap.put(dnac.getDisplayId(), dnac);
			}
			LinkedHashSet<String> compIds = lexoSort(compIdArray, n);
			compPanel.setComponents(compIds);

			String[] featIdArray = new String[lib.getFeatures().size()];
			n = 0;
			for (SequenceFeature sf : lib.getFeatures()) {
				if (filterFeature(sf, filter)) {
					featIdArray[n] = sf.getDisplayId();
					n++;
					featMap.put(sf.getDisplayId(), sf);
				}
			}
			LinkedHashSet<String> featIds = lexoSort(featIdArray, n);
			featPanel.setFeatures(featIds);
		}
	}
	//Sorts first m entries of string array lexographically
	private LinkedHashSet<String> lexoSort(String[] sortingArray, int m) {
		for (int j = 1; j < m; j++) {
			String key = sortingArray[j];
			int i = j - 1;
			while (i >= 0 && sortingArray[i].compareTo(key) > 0) {
				sortingArray[i + 1] = sortingArray[i];
				i = i - 1;
			}
			sortingArray[i + 1] = key;
		}
		LinkedHashSet<String> sortedSet = new LinkedHashSet<String>();
		for (int n = 0; n < m; n++)
			sortedSet.add(sortingArray[n]);
		return sortedSet;
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
