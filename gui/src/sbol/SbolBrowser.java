package sbol;

import gcm.util.Utility;

import java.awt.*;

import javax.swing.*;

import org.sbolstandard.libSBOLj.*;

import java.io.*;
import java.net.URI;
import java.util.*;

import main.Gui;

public class SbolBrowser extends JPanel {
	
	private String[] options = {"Ok", "Cancel"};
	private JPanel selectionPanel = new JPanel(new GridLayout(1,2));
	private JTextArea viewArea = new JTextArea();
	private JScrollPane viewScroll = new JScrollPane();
	private LibraryPanel libPanel;
	private DnaComponentPanel compPanel;
	private String selection = "";
	
	//Constructor when browsing a single RDF file from the main gui
	public SbolBrowser(String filePath) {
		super(new BorderLayout());
		
		HashMap<String, Library> libMap = new HashMap<String, Library>();
		Library lib = SbolUtility.loadRDF(filePath);
		String mySeparator = File.separator;
		if (mySeparator.equals("\\"))
			mySeparator = "\\\\";
		String[] splitPath = filePath.split(mySeparator);
		libMap.put(splitPath[splitPath.length - 1] + "/" + lib.getDisplayId(), lib);
		
		constructBrowser(libMap, "");
		
		JPanel browserPanel = new JPanel();
		browserPanel.add(selectionPanel, "North");
		browserPanel.add(viewScroll, "Center");
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
		
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SbolBrowser(HashSet<String> sbolFiles, String filter, String defaultSelection) {
		super(new GridLayout(2,1));
		
		HashMap<String, Library> libMap = new HashMap<String, Library>();
		for (String filePath : sbolFiles) {
			Library lib = SbolUtility.loadRDF(filePath);
			String mySeparator = File.separator;
			if (mySeparator.equals("\\"))
				mySeparator = "\\\\";
			String[] splitPath = filePath.split(mySeparator);
			libMap.put(splitPath[splitPath.length - 1] + "/" + lib.getDisplayId(), lib);
		}
		
		constructBrowser(libMap, filter);
		
		this.add(selectionPanel);
		this.add(viewScroll);
		
		boolean display = true;
		while (display)
			display = browserOpen(filter, defaultSelection);
	}
	
	private boolean browserOpen(String filter, String defaultSelection) {
		boolean selectionValid;
		do {
			selectionValid = true;
			int option = JOptionPane.showOptionDialog(Gui.frame, this,
					"SBOL Browser", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (option == JOptionPane.YES_OPTION) {
				String[] libIds = libPanel.getSelectedIds();
				String[] compIds = compPanel.getSelectedIds();
				if (libIds.length > 0)
					selection = libIds[0];
				else {
					selectionValid = false;
					JOptionPane.showMessageDialog(Gui.frame, "Blank ID.",
							"Invalid ID", JOptionPane.ERROR_MESSAGE);
				}
				if (compIds.length > 0)
					selection = selection + "/" + compIds[0];
				else if (!filter.equals("library") && libIds.length > 0) {
					selectionValid = false;
					JOptionPane.showMessageDialog(Gui.frame, "Component ID not selected.",
							"Invalid ID", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				selection = defaultSelection;
				selectionValid = true;
			}
		} while(!selectionValid);
		return false;
	}
	
	private void constructBrowser(HashMap<String, Library> libMap, String filter) {
		viewScroll.setMinimumSize(new Dimension(780, 400));
		viewScroll.setPreferredSize(new Dimension(828, 264));
		viewScroll.setViewportView(viewArea);
		viewArea.setLineWrap(true);
		viewArea.setEditable(false);
		
		HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
		HashMap<String, SequenceFeature> featMap = new HashMap<String, SequenceFeature>();
		
		compPanel = new DnaComponentPanel(compMap, featMap, viewArea);
		libPanel = new LibraryPanel(libMap, compMap, featMap, viewArea, compPanel, filter);
		libPanel.setLibraries(libMap.keySet());
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
	}
	
	public String getSelection() {
		return selection;
	}
}
