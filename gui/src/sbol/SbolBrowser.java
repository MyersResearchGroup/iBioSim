package sbol;


import java.awt.*;

import javax.swing.*;

import org.sbolstandard.core.*;

import biomodel.util.Utility;

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
	public SbolBrowser(Gui gui, String filePath) {
		super(new BorderLayout());
		
		HashMap<String, org.sbolstandard.core.Collection> libMap = new HashMap<String, org.sbolstandard.core.Collection>();
		LinkedList<String> libURIs = new LinkedList<String>();
		LinkedList<String> libIds = new LinkedList<String>();
		HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
		HashMap<String, SequenceAnnotation> annoMap = new HashMap<String, SequenceAnnotation>();
		HashMap<String, DnaSequence> seqMap = new HashMap<String, DnaSequence>();
		
		filePath = filePath.replace("\\\\", "\\");
		
		loadSbolFiles(gui.getSbolFiles(), libURIs, libIds, libMap, compMap, annoMap, seqMap, filePath);
		
		constructBrowser(libURIs, libIds, libMap, compMap, annoMap, seqMap, new HashSet<String>());
			
		if (libMap.size() > 0) {
			JPanel browserPanel = new JPanel();
			browserPanel.add(selectionPanel, "North");
			browserPanel.add(viewScroll, "Center");

			JTabbedPane browserTab = new JTabbedPane();
			browserTab.add("SBOL Browser", browserPanel);
			this.add(browserTab);
			gui.addTab(filePath.substring(filePath.lastIndexOf(File.separator) + 1), this, null);
		}
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SbolBrowser(HashSet<String> sbolFiles, Set<String> filter, String defaultSelection) {
		super(new GridLayout(2,1));
		
		HashMap<String, org.sbolstandard.core.Collection> libMap = new HashMap<String, org.sbolstandard.core.Collection>();
		LinkedList<String> libURIs = new LinkedList<String>();
		LinkedList<String> libIds = new LinkedList<String>();
		HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
		HashMap<String, SequenceAnnotation> annoMap = new HashMap<String, SequenceAnnotation>();
		HashMap<String, DnaSequence> seqMap = new HashMap<String, DnaSequence>();
		
		loadSbolFiles(sbolFiles, libURIs, libIds, libMap, compMap, annoMap, seqMap, "");
		
		if (libMap.size() > 0) {
			constructBrowser(libURIs, libIds, libMap, compMap, annoMap, seqMap, filter);

			this.add(selectionPanel);
			this.add(viewScroll);

			boolean display = true;
			while (display)
				display = browserOpen(defaultSelection);
		} else {
			selection = defaultSelection;
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadSbolFiles(HashSet<String> sbolFiles, LinkedList<String> libURIs, LinkedList<String> libIds, 
			HashMap<String, org.sbolstandard.core.Collection> libMap, HashMap<String, DnaComponent> compMap, 
			HashMap<String, SequenceAnnotation> annoMap, HashMap<String, DnaSequence> seqMap, String browsePath) {
		for (String filePath : sbolFiles) {
			org.sbolstandard.core.Collection lib = SbolUtility.loadXML(filePath);
			if (lib != null) {
				if (lib.getDisplayId() != null && !lib.getDisplayId().equals("")) {
					if ((browsePath.equals("") || browsePath.equals(filePath)) && !libURIs.contains(lib.getURI().toString())) {
						libURIs.add(lib.getURI().toString());
						libIds.add(lib.getDisplayId());
					}
					if (!libMap.containsKey(lib.getURI().toString()))
						libMap.put(lib.getURI().toString(), lib);
				}
				if (lib.getComponents() != null)
					for (DnaComponent dnac : lib.getComponents()) {
						if (dnac.getDisplayId() != null && !dnac.getDisplayId().equals("") 
								&& !compMap.containsKey(dnac.getURI().toString()))
							compMap.put(dnac.getURI().toString(), dnac);
						if (dnac.getAnnotations() != null)
							for (SequenceAnnotation sa : dnac.getAnnotations()) {
								Integer start = Integer.valueOf(sa.getBioStart());
								Integer end = Integer.valueOf(sa.getBioEnd());
								if (start != null && end != null && !annoMap.containsKey(sa.getURI().toString()))
									annoMap.put(sa.getURI().toString(), sa);
							}
						if (dnac.getDnaSequence() != null && dnac.getDnaSequence().getNucleotides() != null
								&& !dnac.getDnaSequence().getNucleotides().equals("")
								&& !seqMap.containsKey(dnac.getDnaSequence().getURI().toString()))
							seqMap.put(dnac.getDnaSequence().getURI().toString(), dnac.getDnaSequence());
					}
			}
		}
	}
	
	private boolean browserOpen(String defaultSelection) {
		boolean selectionValid;
		do {
			selectionValid = true;
			int option = JOptionPane.showOptionDialog(Gui.frame, this,
					"SBOL Browser", JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			if (option == JOptionPane.YES_OPTION) {
				String[] compIds = compPanel.getSelectedURIs();
				if (compIds.length > 0)
					selection = compIds[0];
				else {
					selectionValid = false;
					JOptionPane.showMessageDialog(Gui.frame, "No DNA component is selected.",
							"Invalid Selection", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				selection = defaultSelection;
				selectionValid = true;
			}
		} while(!selectionValid);
		return false;
	}
	
	private void constructBrowser(LinkedList<String> libURIs, LinkedList<String> libIds, 
			HashMap<String, org.sbolstandard.core.Collection> libMap, HashMap<String, DnaComponent> compMap, 
			HashMap<String, SequenceAnnotation> annoMap, HashMap<String, DnaSequence> seqMap, Set<String> filter) {
		viewScroll.setMinimumSize(new Dimension(780, 400));
		viewScroll.setPreferredSize(new Dimension(828, 264));
//		viewScroll.setMinimumSize(new Dimension(552, 80));
//		viewScroll.setPreferredSize(new Dimension(552, 80));
		viewScroll.setViewportView(viewArea);
		viewArea.setLineWrap(true);
		viewArea.setEditable(false);
		
		compPanel = new DnaComponentPanel(compMap, annoMap, seqMap, viewArea);
		libPanel = new LibraryPanel(libMap, compMap, viewArea, compPanel, filter);
		libPanel.setLibraries(libIds, libURIs);
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
	}
	
	public String getSelection() {
		return selection;
	}
}
