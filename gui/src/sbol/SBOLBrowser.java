package sbol;


import java.awt.*;

import javax.swing.*;

import org.sbolstandard.core.*;

import biomodel.util.Utility;

import java.io.*;
import java.net.URI;

import java.util.*;

import main.Gui;

public class SBOLBrowser extends JPanel {
	
	private String[] options = {"Ok", "Cancel"};
	private JPanel selectionPanel = new JPanel(new GridLayout(1,2));
	private JTextArea viewArea = new JTextArea();
	private JScrollPane viewScroll = new JScrollPane();
	private CollectionBrowserPanel libPanel;
	private DNAComponentBrowserPanel compPanel;
	private LinkedList<String> selectedCompURIs;
	
	//Constructor when browsing a single RDF file from the main gui
	public SBOLBrowser(Gui gui, String filePath) {
		super(new BorderLayout());
		
//		SequenceTypeValidator validator = new SequenceTypeValidator("((a+b)|(cd*))*e");
		
		HashMap<String, org.sbolstandard.core.Collection> libMap = new HashMap<String, org.sbolstandard.core.Collection>();
		LinkedList<String> libURIs = new LinkedList<String>();
		LinkedList<String> libIds = new LinkedList<String>();
		HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
		HashMap<String, SequenceAnnotation> annoMap = new HashMap<String, SequenceAnnotation>();
		HashMap<String, DnaSequence> seqMap = new HashMap<String, DnaSequence>();
		
		filePath = filePath.replace("\\\\", "\\");
		
		loadSbolFiles(gui.getSbolFiles(), libURIs, libIds, libMap, compMap, annoMap, seqMap, filePath);
		
		constructBrowser(libURIs, libIds, libMap, compMap, annoMap, seqMap, new HashSet<String>());
			
		JPanel browserPanel = new JPanel();
		browserPanel.add(selectionPanel, "North");
		browserPanel.add(viewScroll, "Center");

		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
		gui.addTab(filePath.substring(filePath.lastIndexOf(File.separator) + 1), this, null);
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SBOLBrowser(HashSet<String> sbolFiles, Set<String> filter, LinkedList<String> defaultSelectedCompURIs) {
		super(new GridLayout(2,1));
		
		selectedCompURIs = new LinkedList<String>();
		
		HashMap<String, org.sbolstandard.core.Collection> libMap = new HashMap<String, org.sbolstandard.core.Collection>();
		LinkedList<String> libURIs = new LinkedList<String>();
		LinkedList<String> libIds = new LinkedList<String>();
		HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
		HashMap<String, SequenceAnnotation> annoMap = new HashMap<String, SequenceAnnotation>();
		HashMap<String, DnaSequence> seqMap = new HashMap<String, DnaSequence>();
		
		loadSbolFiles(sbolFiles, libURIs, libIds, libMap, compMap, annoMap, seqMap, "");
		
		if (compMap.size() > 0) {
			constructBrowser(libURIs, libIds, libMap, compMap, annoMap, seqMap, filter);

			this.add(selectionPanel);
			this.add(viewScroll);

			boolean display = true;
			while (display)
				display = browserOpen();
		} else {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL DNA components are found in project.", 
					"DNA Components Not Found", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void loadSbolFiles(HashSet<String> sbolFiles, LinkedList<String> libURIs, LinkedList<String> libIds, 
			HashMap<String, org.sbolstandard.core.Collection> libMap, HashMap<String, DnaComponent> compMap, 
			HashMap<String, SequenceAnnotation> annoMap, HashMap<String, DnaSequence> seqMap, String browsePath) {
		for (String filePath : sbolFiles) {
			if (browsePath.equals("") || browsePath.equals(filePath)) {
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
				if (sbolDoc != null) {
					for (org.sbolstandard.core.Collection lib : SBOLUtility.loadCollections(sbolDoc))
						if (lib.getDisplayId() != null && !lib.getDisplayId().equals("")) {
							if (!libURIs.contains(lib.getURI().toString())) {
								libURIs.add(lib.getURI().toString());
								libIds.add(lib.getDisplayId());
							}
							if (!libMap.containsKey(lib.getURI().toString()))
								libMap.put(lib.getURI().toString(), lib);
						}
					for (DnaComponent dnac : SBOLUtility.loadDNAComponents(sbolDoc).values()) {
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
	}
	
	private boolean browserOpen() {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Browser", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == JOptionPane.YES_OPTION) {
			selectedCompURIs = compPanel.getSelectedURIs();
			if (selectedCompURIs.size() == 0) {
				JOptionPane.showMessageDialog(Gui.frame, "No DNA component is selected.",
						"Invalid Selection", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
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
		
		compPanel = new DNAComponentBrowserPanel(compMap, annoMap, seqMap, viewArea);
		libPanel = new CollectionBrowserPanel(libMap, compMap, viewArea, compPanel, filter);
		libPanel.setLibraries(libIds, libURIs);
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
	}
	
	public LinkedList<String> getSelection() {
		return selectedCompURIs;
	}
}
