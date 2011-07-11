package sbol;

import java.awt.*;

import javax.swing.*;

import org.sbolstandard.libSBOLj.*;
import java.io.*;
import java.net.URI;
import java.util.*;

import main.Gui;

public class SbolBrowser extends JPanel {
	
	private HashMap<String, Library> libMap = new HashMap<String, Library>();
	private HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
	private HashMap<String, SequenceFeature> featMap = new HashMap<String, SequenceFeature>();
	private String filter = "";
	private String[] options = {"Ok"};
	private JPanel selectionPanel = new JPanel(new GridLayout(1,2));
	private JTextArea viewArea = new JTextArea();
	private JScrollPane viewScroll = new JScrollPane();
	private LibraryPanel libPanel;
	private DnaComponentPanel compPanel;
	private JTextField sbolText;
	
	//Constructor when browsing a single RDF file from the main gui
	public SbolBrowser(String filePath) {
		super(new BorderLayout());
		
		loadRDF(filePath);
		
		constructBrowser();
		
		JPanel browserPanel = new JPanel();
		browserPanel.add(selectionPanel, "North");
		browserPanel.add(viewScroll, "Center");
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
		
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SbolBrowser(HashSet<String> filePaths, String filter, JTextField sbolText) {
		super(new GridLayout(2,1));
		this.filter = filter;
		this.sbolText = sbolText;
		
		for (String fp : filePaths)
			loadRDF(fp);
		
		constructBrowser();
		
		this.add(selectionPanel);
		this.add(viewScroll);
		
		boolean display = true;
		while (display)
			display = browserOpen();
	}
	
	private boolean browserOpen() {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Browser", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == JOptionPane.YES_OPTION) {
			sbolText.setText(libPanel.getSelectedIds()[0] + "/" + compPanel.getSelectedIds()[0]);
			return false;
		} else if (option == JOptionPane.NO_OPTION) {
			return false;
		}
		return false;
	}
	
	private void constructBrowser() {
		viewScroll.setMinimumSize(new Dimension(780, 400));
		viewScroll.setPreferredSize(new Dimension(828, 264));
		viewScroll.setViewportView(viewArea);
		viewArea.setLineWrap(true);
		viewArea.setEditable(false);
		
		compPanel = new DnaComponentPanel(compMap, featMap, viewArea);
		libPanel = new LibraryPanel(libMap, compMap, featMap, viewArea, compPanel, filter);
		libPanel.setLibraries(libMap.keySet());
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
	}
	
	private void loadRDF(String filePath) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			Scanner scanIn = new Scanner(in).useDelimiter("\n");
			String rdfString = "";
//			HashSet<String> libIds = new HashSet<String>();
//			boolean libFlag = false;
			while (scanIn.hasNext()) {
				String token = scanIn.next();
//				if (libFlag && token.startsWith("\t<displayId")) {
//						int start = token.indexOf(">");
//						int stop = token.indexOf("<", start);
//						libIds.add(token.substring(start + 1, stop));
//						libFlag = false;
//				} else if (token.equals("\t<rdf:type rdf:resource=\"http://sbols.org/sbol.owl#Library\"/>"))
//					libFlag = true;
				rdfString = rdfString.concat(token) + "\n";
			}
			scanIn.close();
			SbolService factory = IOTools.fromRdfXml(rdfString);
//			for (String libId : libIds) {
//				Library lib = factory.getLibrary(libId);
//				libMap.put(libId, lib);
//			}		
			Library lib = factory.getLibrary();
			String libId = lib.getDisplayId();
			libMap.put(libId, lib);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "Error opening SBOL file.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
