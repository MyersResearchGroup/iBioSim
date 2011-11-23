package sbol;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.sbolstandard.core.*;

import java.net.URI;
import java.util.*;

public class LibraryPanel extends JPanel implements MouseListener {

	private HashMap<String, org.sbolstandard.core.Collection> libMap;
	private HashMap<String, DnaComponent> compMap;
	private JTextArea viewArea;
	private DnaComponentPanel compPanel;
	private JList libList = new JList();
	private String filter;
	
	public LibraryPanel(HashMap<String, org.sbolstandard.core.Collection> libMap, HashMap<String, DnaComponent> compMap, 
			JTextArea viewArea, DnaComponentPanel compPanel, String filter) {
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
	
	public void setLibraries(Set<String> libIds) {
		Object[] idObjects = libIds.toArray();
		libList.setListData(idObjects);
	}
	
	public String[] getSelectedIds() {
		Object[] selected = libList.getSelectedValues();
		String[] selectedIds = new String[selected.length];
		for (int i = 0; i < selectedIds.length; i++)
			selectedIds[i] = selected[i].toString();
		return selectedIds;
	}
	
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == libList) {
			viewArea.setText("");
			String[] selectedIds = getSelectedIds();
			org.sbolstandard.core.Collection lib = libMap.get(selectedIds[0]);
			if (lib.getName() != null)
				viewArea.append("Name:  " + lib.getName() + "\n");
			else
				viewArea.append("Name:  NA\n");
			if (lib.getDescription() != null)
				viewArea.append("Description:  " + lib.getDescription() + "\n\n");
			else
				viewArea.append("Description:  NA\n\n");

			String[] compIdArray = new String[lib.getComponents().size()];
			int n = 0;
			for (DnaComponent dnac : lib.getComponents()) {
				if (filter.equals("") || filterFeature(dnac, filter)) {
					compIdArray[n] = dnac.getDisplayId();
					compMap.put(dnac.getDisplayId(), dnac);
					n++;
				}
			}
			LinkedHashSet<String> compIds = lexoSort(compIdArray, n);
			compPanel.setComponents(compIds);

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
	
	private boolean filterFeature(DnaComponent dnac, String filter) {
		HashSet<String> types = new HashSet<String>();
 		for (URI uri : dnac.getTypes()) 
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
