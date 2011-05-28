package sbol;

import java.awt.*;
import javax.swing.*;

import org.sbolstandard.libSBOLj.*;
import java.io.*;
import java.util.*;

import main.Gui;

public class SbolBrowser extends JPanel {
	
	private HashMap<String, Library> libMap = new HashMap<String, Library>();
	private HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
	private HashMap<String, SequenceFeature> featMap = new HashMap<String, SequenceFeature>();
	private LibraryPanel libPanel;
	private DnaComponentPanel compPanel;
	private SequenceFeaturePanel featPanel;
	private TextArea viewArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
	private String[] options = {"Ok"};
	
	public SbolBrowser(String filePath) {
		super(new BorderLayout());
		
		loadRDF(filePath);
		featPanel = new SequenceFeaturePanel(featMap, viewArea);
		compPanel = new DnaComponentPanel(compMap, viewArea, featPanel);
		libPanel = new LibraryPanel(libMap, viewArea, compPanel, featPanel);
		libPanel.setLibraries(libMap.keySet());
		JPanel selectionPanel = new JPanel(new GridLayout(1,3));
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
		selectionPanel.add(featPanel);
		
		viewArea.setEditable(false);
		
		JPanel browserPanel = new JPanel();
		browserPanel.add(selectionPanel, "North");
		browserPanel.add(viewArea, "Center");
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("Browser", browserPanel);
		
		this.add(browserTab);
	
		boolean display = true;
		while (display)
			display = browserOpen();
	}
	
	private boolean browserOpen() {
		JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Browser", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		return false;
	}
	
	private void loadRDF(String filePath) {
		try {
			FileInputStream in = new FileInputStream(filePath);
			Scanner scanIn = new Scanner(in).useDelimiter("\n");
			String rdfString = "";
			HashSet<String> libIds = new HashSet<String>();
			boolean libFlag = false;
			while (scanIn.hasNext()) {
				String token = scanIn.next();
				if (libFlag && token.startsWith("\t<displayId")) {
						int start = token.indexOf(">");
						int stop = token.indexOf("<", start);
						libIds.add(token.substring(start + 1, stop));
						libFlag = false;
				} else if (token.equals("\t<rdf:type rdf:resource=\"http://sbols.org/sbol.owl#Library\"/>"))
					libFlag = true;
				rdfString = rdfString.concat(token) + "\n";
			}
			scanIn.close();
			SBOLservice factory = SBOLutil.fromRDF(rdfString);
			for (String libId : libIds) {
				Library lib = factory.getLibrary(libId);
				libMap.put(libId, lib);
				for (DnaComponent dnac : lib.getComponents()) {
					compMap.put(dnac.getDisplayId(), dnac);
					for (SequenceAnnotation sa : dnac.getAnnotations()) {
						for (SequenceFeature sf : sa.getFeatures())
							featMap.put(sf.getDisplayId(), sf);
					}
				}
				for (SequenceFeature sf : lib.getFeatures()) {
					if (!featMap.containsKey(sf.getDisplayId()))
						featMap.put(sf.getDisplayId(), sf);
				}
			}
				
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(Gui.frame, "File not found.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
