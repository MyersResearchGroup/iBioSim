package sbol;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class SBOLBrowser extends JPanel implements ActionListener {
	
	private String[] options = {"Ok", "Cancel"};
	private JPanel selectionPanel = new JPanel(new GridLayout(1,2));
	private JPanel filterPanel = new JPanel(new GridLayout(1,5));
	private JTextField filterText = new JTextField();
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
	private boolean isAssociationBrowser = false;
	
	//Constructor when browsing a single RDF file from the main gui
	public SBOLBrowser(Gui gui, String browsePath) {
		super(new BorderLayout());
		
		browsePath = browsePath.replace("\\\\", "\\");
		
		loadSbolFiles(gui.getFilePaths(".sbol"), browsePath);
		
		constructBrowser(new HashSet<String>());
			
		JPanel browserPanel = new JPanel(new BorderLayout());
		browserPanel.add(filterPanel, BorderLayout.NORTH);
		browserPanel.add(selectionPanel, BorderLayout.CENTER);
		browserPanel.add(viewScroll, BorderLayout.SOUTH);
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
		gui.addTab(browsePath.substring(browsePath.lastIndexOf(File.separator) + 1), this, "SBOL Browser");
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SBOLBrowser(HashSet<String> sbolFilePaths, Set<String> filter, LinkedList<URI> defaultSelectedCompURIs) {
//		super(new GridLayout(3,1));
		super(new BorderLayout());
		isAssociationBrowser = true;
		selectedCompURIs = new LinkedList<URI>();
		
		loadSbolFiles(sbolFilePaths, "");
	
		constructBrowser(filter);

		this.add(filterPanel, BorderLayout.NORTH);
		this.add(selectionPanel, BorderLayout.CENTER);
		this.add(viewScroll, BorderLayout.SOUTH);
//		this.add(selectionPanel);
//		this.add(viewScroll);
//		this.add(filterPanel);

		boolean display = true;
		while (display)
			display = browserOpen();
	}
	
	private void loadSbolFiles(HashSet<String> sbolFilePaths, String browsePath) {
		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		LinkedList<Resolver<SequenceAnnotation, URI>> annoResolvers = new LinkedList<Resolver<SequenceAnnotation, URI>>();
		LinkedList<Resolver<DnaSequence, URI>> seqResolvers = new LinkedList<Resolver<DnaSequence, URI>>();
		LinkedList<Resolver<org.sbolstandard.core.Collection, URI>> libResolvers = new LinkedList<Resolver<org.sbolstandard.core.Collection, URI>>();
		String browseFile = browsePath.substring(browsePath.lastIndexOf(File.separator) + 1);
		for (String filePath : sbolFilePaths) {
			String file = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
			if (browsePath.equals("") || browseFile.equals(file)) {
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
				if (sbolDoc != null) {
					SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
					compResolvers.add(flattenedDoc.getComponentUriResolver());
					annoResolvers.add(flattenedDoc.getAnnotationUriResolver());
					seqResolvers.add(flattenedDoc.getSequenceUriResolver());
					libResolvers.add(flattenedDoc.getCollectionUriResolver());
					for (SBOLRootObject sbolObj : flattenedDoc.getContents())
						if (sbolObj instanceof org.sbolstandard.core.Collection) {
							org.sbolstandard.core.Collection lib = (org.sbolstandard.core.Collection) sbolObj;
							if (lib.getDisplayId() != null && !lib.getDisplayId().equals("") && 
									!localLibURIs.contains(lib.getURI().toString())) {
								localLibURIs.add(lib.getURI());
								localLibIds.add(lib.getDisplayId());
							}
						} else if (sbolObj instanceof DnaComponent && 
								(!isAssociationBrowser || !sbolObj.getURI().toString().endsWith("iBioSim")))
							localCompURIs.add(sbolObj.getURI());
						
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
		libPanel = new CollectionBrowserPanel(aggregateLibResolver, aggregateCompResolver, viewArea, compPanel);
		libPanel.setFilter(filter);
		libPanel.setLocalLibsComps(localLibIds, localLibURIs, localCompURIs);
		libPanel.setIsAssociationBrowser(isAssociationBrowser);
		
		selectionPanel.add(libPanel);
		selectionPanel.add(compPanel);
		
		JButton filterButton = new JButton("Filter");
		filterButton.setActionCommand("filterSBOL");
		filterButton.addActionListener(this);
		JLabel filterLabel = new JLabel("Filter by Type:  ");
		filterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		filterPanel.add(filterLabel);
		filterPanel.add(filterText);
		filterPanel.add(filterButton);
		filterPanel.add(new JLabel());
	}
	
	public LinkedList<URI> getSelection() {
		return selectedCompURIs;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("filterSBOL")) {
			compPanel.filterComponents(filterText.getText());
		} 
		
	}
}
