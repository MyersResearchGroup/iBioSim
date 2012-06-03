package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.core.*;

import java.net.URI;
import java.util.*;

public class CollectionBrowserPanel extends JPanel implements MouseListener {

	private LinkedList<String> libURIs;
	private HashMap<String, org.sbolstandard.core.Collection> libMap;
	private HashMap<String, DnaComponent> compMap;
	private JTextArea viewArea;
	private DNAComponentBrowserPanel compPanel;
	private JList libList = new JList();
	private Set<String> filter;
	
	public CollectionBrowserPanel(HashMap<String, org.sbolstandard.core.Collection> libMap, HashMap<String, DnaComponent> compMap, 
			JTextArea viewArea, DNAComponentBrowserPanel compPanel, Set<String> filter) {
		super(new BorderLayout());
		this.libMap = libMap;
		this.compMap = compMap;
		this.viewArea = viewArea;
		this.compPanel = compPanel;
		this.filter = filter;
		
		libList.addMouseListener(this);
		
		JLabel libraryLabel = new JLabel("Collections:");
		
		JScrollPane libraryScroll = new JScrollPane();
		libraryScroll.setMinimumSize(new Dimension(260, 200));
		libraryScroll.setPreferredSize(new Dimension(276, 132));
//		libraryScroll.setMinimumSize(new Dimension(276, 50));
//		libraryScroll.setPreferredSize(new Dimension(276, 50));
		libraryScroll.setViewportView(libList);
		
		this.add(libraryLabel, "North");
		this.add(libraryScroll, "Center");
	}
	
	public void setLibraries(LinkedList<String> libIds, LinkedList<String> libURIs) {
		this.libURIs = libURIs;
		libIds.addFirst("all");
		Object[] idObjects = libIds.toArray();
		libList.setListData(idObjects);
		libList.setSelectedIndex(0);
		displaySelected();
	}
	
	private String[] getSelectedURIs() {
		int[] selectedIndices = libList.getSelectedIndices();
		String[] selectedURIs = new String[selectedIndices.length];
		for (int i = 0; i < selectedURIs.length; i++) {
			int index = selectedIndices[i];
			// Leave first entry in selectedURIs null if "all" was selected under Collections (see displaySelected())
			if (index != 0)
				selectedURIs[i] = libURIs.get(index - 1);
		}
		return selectedURIs;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == libList) {
			viewArea.setText("");
			displaySelected();
		}
	}
	
	private void displaySelected() {
		String[] selectedURIs = getSelectedURIs();
		LinkedList<String> compIdNames = new LinkedList<String>();
		LinkedList<String> compURIs = new LinkedList<String>();
		int n = 0;
		// Case when a specific collection(s) is selected
		if (selectedURIs[0] != null) {
			org.sbolstandard.core.Collection lib = libMap.get(selectedURIs[0]);
			if (lib.getName() != null)
				viewArea.append("Name:  " + lib.getName() + "\n");
			else
				viewArea.append("Name:  NA\n");
			if (lib.getDescription() != null)
				viewArea.append("Description:  " + lib.getDescription() + "\n\n");
			else
				viewArea.append("Description:  NA\n\n");

			for (DnaComponent dnac : lib.getComponents()) 
				if (!compURIs.contains(dnac.getURI().toString())) {
					dnac = compMap.get(dnac.getURI().toString());
					if (filter.size() == 0 || filterFeature(dnac, filter)) {
						if (dnac.getName() != null && !dnac.getName().equals(""))
							compIdNames.add(dnac.getDisplayId() + " : " + dnac.getName());
						else
							compIdNames.add(dnac.getDisplayId());
						compURIs.add(dnac.getURI().toString());
						n++;
					}
				}
		} else {  // Case when "all" is selected under Collections
//			for (String libURI : libURIs) 
				for (DnaComponent dnac : compMap.values()) 
					if (!compURIs.contains(dnac.getURI().toString())) {
						dnac = compMap.get(dnac.getURI().toString());
						if (filter.size() == 0 || filterFeature(dnac, filter)) {
							if (dnac.getName() != null && !dnac.getName().equals(""))
								compIdNames.add(dnac.getDisplayId() + " : " + dnac.getName());
							else
								compIdNames.add(dnac.getDisplayId());
							compURIs.add(dnac.getURI().toString());
							n++;
						}
					}
		}
		LinkedList<LinkedList<String>> sortedResult = lexoSort(compIdNames, compURIs, n);
		compPanel.setComponents(sortedResult.get(0), sortedResult.get(1));
	}
	
	//Sorts first m entries of string array lexographically
	private LinkedList<LinkedList<String>> lexoSort(LinkedList<String> sortingList, LinkedList<String> companionList, int m) {
		for (int j = 1; j < m; j++) {
			String key = sortingList.get(j);
			String companionKey = companionList.get(j);
			int i = j - 1;
			while (i >= 0 && sortingList.get(i).compareTo(key) > 0) {
				sortingList.set(i + 1, sortingList.get(i));
				companionList.set(i + 1, companionList.get(i));
				i = i - 1;
			}
			sortingList.set(i + 1, key);
			companionList.set(i + 1, companionKey);
		}
		LinkedList<LinkedList<String>> sortedResult = new LinkedList<LinkedList<String>>();
		sortedResult.add(sortingList);
		sortedResult.add(companionList);
		return sortedResult;
	}
	
	private boolean filterFeature(DnaComponent dnac, Set<String> filter) {
 		for (URI uri : dnac.getTypes()) 
			if (filter.contains(uri.toString()))
				return true;
 		return false;
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
