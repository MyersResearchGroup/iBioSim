package sbol;


import java.awt.*;

import javax.swing.*;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;
import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.DnaComponentImpl;
import org.sbolstandard.core.impl.SBOLDocumentImpl;

import biomodel.util.Utility;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

import main.Gui;

public class SBOLBrowser extends JPanel {
	
	private String[] options = {"Ok", "Cancel"};
	private JPanel selectionPanel = new JPanel(new GridLayout(1,2));
	private JTextArea viewArea = new JTextArea();
	private JScrollPane viewScroll = new JScrollPane();
	private CollectionBrowserPanel libPanel;
	private DNAComponentBrowserPanel compPanel;
	private LinkedList<URI> selectedCompURIs;
	
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
	private UseFirstFound<SequenceAnnotation, URI> aggregateAnnoResolver = new AggregatingResolver.UseFirstFound<SequenceAnnotation, URI>();
	private UseFirstFound<DnaSequence, URI> aggregateSeqResolver = new AggregatingResolver.UseFirstFound<DnaSequence, URI>();
	private UseFirstFound<org.sbolstandard.core.Collection, URI> aggregateLibResolver = new AggregatingResolver.UseFirstFound<org.sbolstandard.core.Collection, URI>();
	
	private LinkedList<URI> localLibURIs = new LinkedList<URI>();
	private LinkedList<String> localLibIds = new LinkedList<String>();
	private LinkedList<URI> localCompURIs = new LinkedList<URI>();
	
	//Constructor when browsing a single RDF file from the main gui
	public SBOLBrowser(Gui gui, String filePath) {
		super(new BorderLayout());
		
//		HashMap<String, org.sbolstandard.core.Collection> libMap = new HashMap<String, org.sbolstandard.core.Collection>();
//		HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
//		HashMap<String, SequenceAnnotation> annoMap = new HashMap<String, SequenceAnnotation>();
//		HashMap<String, DnaSequence> seqMap = new HashMap<String, DnaSequence>();
		
		filePath = filePath.replace("\\\\", "\\");
		
		loadSbolFiles(gui.getSbolFiles(), filePath);
		
		constructBrowser(new HashSet<String>());
			
		JPanel browserPanel = new JPanel();
		browserPanel.add(selectionPanel, "North");
		browserPanel.add(viewScroll, "Center");

		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
		gui.addTab(filePath.substring(filePath.lastIndexOf(File.separator) + 1), this, null);
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SBOLBrowser(HashSet<String> sbolFiles, Set<String> filter, LinkedList<URI> defaultSelectedCompURIs) {
		super(new GridLayout(2,1));
		
		selectedCompURIs = new LinkedList<URI>();
		
//		HashMap<String, org.sbolstandard.core.Collection> libMap = new HashMap<String, org.sbolstandard.core.Collection>();
//		HashMap<String, DnaComponent> compMap = new HashMap<String, DnaComponent>();
//		HashMap<String, SequenceAnnotation> annoMap = new HashMap<String, SequenceAnnotation>();
//		HashMap<String, DnaSequence> seqMap = new HashMap<String, DnaSequence>();
		
		loadSbolFiles(sbolFiles, "");
		
//		if (compMap.size() > 0) {
			constructBrowser(filter);

			this.add(selectionPanel);
			this.add(viewScroll);

			boolean display = true;
			while (display)
				display = browserOpen();
//		} else {
//			JOptionPane.showMessageDialog(Gui.frame, "No SBOL DNA components are found in project.", 
//					"DNA Components Not Found", JOptionPane.ERROR_MESSAGE);
//		}
	}
	
	private void loadSbolFiles(HashSet<String> sbolFiles, String browsePath) {
		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		LinkedList<Resolver<SequenceAnnotation, URI>> annoResolvers = new LinkedList<Resolver<SequenceAnnotation, URI>>();
		LinkedList<Resolver<DnaSequence, URI>> seqResolvers = new LinkedList<Resolver<DnaSequence, URI>>();
		LinkedList<Resolver<org.sbolstandard.core.Collection, URI>> libResolvers = new LinkedList<Resolver<org.sbolstandard.core.Collection, URI>>();
		for (String filePath : sbolFiles) {
			if (browsePath.equals("") || browsePath.equals(filePath)) {
				SBOLDocumentImpl sbolDoc = (SBOLDocumentImpl) SBOLUtility.loadSBOLFile(filePath);
				if (sbolDoc != null) {
					SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenDocument(sbolDoc);
					compResolvers.add(flattenedDoc.getComponentUriResolver());
					annoResolvers.add(flattenedDoc.getAnnotationUriResolver());
					seqResolvers.add(flattenedDoc.getSequenceUriResolver());
					libResolvers.add(flattenedDoc.getCollectionUriResolver());
					for (SBOLRootObject sbolObj : flattenedDoc.getContents()) {
						if (sbolObj instanceof org.sbolstandard.core.Collection) {
							org.sbolstandard.core.Collection lib = (org.sbolstandard.core.Collection) sbolObj;
							if (lib.getDisplayId() != null && !lib.getDisplayId().equals("") && 
									!localLibURIs.contains(lib.getURI().toString())) {
								localLibURIs.add(lib.getURI());
								localLibIds.add(lib.getDisplayId());
							}
						} else if (sbolObj instanceof DnaComponent) 
							localCompURIs.add(((DnaComponent) sbolObj).getURI());
					}
				}
			}
		}
		aggregateCompResolver.setResolvers(compResolvers);
		aggregateAnnoResolver.setResolvers(annoResolvers);
		aggregateSeqResolver.setResolvers(seqResolvers);
		aggregateLibResolver.setResolvers(libResolvers);
		
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
	
	private void constructBrowser(Set<String> filter) {
		viewScroll.setMinimumSize(new Dimension(780, 400));
		viewScroll.setPreferredSize(new Dimension(828, 264));
		viewScroll.setViewportView(viewArea);
		viewArea.setLineWrap(true);
		viewArea.setEditable(false);
		
		compPanel = new DNAComponentBrowserPanel(aggregateCompResolver, aggregateAnnoResolver, aggregateSeqResolver, viewArea);
		libPanel = new CollectionBrowserPanel(aggregateLibResolver, aggregateCompResolver, viewArea, compPanel, filter);
		libPanel.setLocalLibsComps(localLibIds, localLibURIs, localCompURIs);
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
	}
	
	public LinkedList<URI> getSelection() {
		return selectedCompURIs;
	}
}
