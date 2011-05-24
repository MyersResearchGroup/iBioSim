package sbol;

import java.awt.*;
import java.awt.event.*;

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
	private LibraryPanel libPanel;
	private DnaComponentPanel compPanel;
	private SequenceFeaturePanel featPanel;
	private TextArea viewer = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
	private String[] options = {"Ok"};
	
	public SbolBrowser(String filePath) {
		super(new BorderLayout());
		
		JPanel textPanel = new JPanel(new GridLayout(1,3));
		JLabel libraryLabel = new JLabel("Libraries:");
		textPanel.add(libraryLabel);
		JLabel componentLabel = new JLabel("DNA Components:");
		textPanel.add(componentLabel);
		JLabel featureLabel = new JLabel("Sequence Features:");
		textPanel.add(featureLabel);
		this.add(textPanel, "North");
		
		loadRDF(filePath);
		featPanel = new SequenceFeaturePanel(featMap, viewer);
		compPanel = new DnaComponentPanel(compMap, viewer, featPanel);
		libPanel = new LibraryPanel(libMap, viewer, compPanel, featPanel);
		libPanel.setLibraries(libMap.keySet());
		
		JPanel listPanel = new JPanel(new GridLayout(1,3));
		listPanel.add(libPanel);
		listPanel.add(compPanel);
		listPanel.add(featPanel);
		this.add(listPanel, "Center");
		
		viewer.setEditable(false);
		this.add(viewer, "South");
		
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
			ArrayList<String> libIds = new ArrayList<String>();
			boolean libFlag = false;
			while (scanIn.hasNext()) {
				String token = scanIn.next();
				if (libFlag) {
					String temp = token.split("<")[1];
					libIds.add(temp.substring(temp.indexOf(">") + 1, temp.length()));
					libFlag = false;
				} else if (token.equals("\t<rdf:type rdf:resource=\"http://sbols.org/sbol.owl#Library\"/>"))
					libFlag = true;
				rdfString = rdfString.concat(token) + "\n";
			}
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
