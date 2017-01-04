package backend.sbol.browser;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;

import backend.biomodel.util.GlobalConstants;
import backend.sbol.util.SBOLUtility;
import frontend.main.Gui;

import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.SBOLDocumentImpl;

import java.io.*;
import java.net.URI;

import java.util.*;

public class SBOLBrowser extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	private String[] options = {"Ok", "Cancel"};
	private JPanel selectionPanel = new JPanel(new GridLayout(1,2));
	private JPanel filterPanel = new JPanel(new GridLayout(1,3));
	private JComboBox filterBox;
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
	
	private Gui gui;
	private String browsePath;
	
	//Constructor when browsing a single RDF file from the main gui
	public SBOLBrowser(Gui gui, String browsePath) {
		super(new BorderLayout());
		this.gui = gui;
		this.browsePath = browsePath;
		
		browsePath = browsePath.replace("\\\\", "\\");
		
		loadSbolFiles(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION), browsePath);
		
		constructBrowser(new HashSet<String>());
	}
		
	public void open() {			
		JPanel browserPanel = new JPanel(new BorderLayout());
		browserPanel.add(filterPanel, BorderLayout.NORTH);
		browserPanel.add(selectionPanel, BorderLayout.CENTER);
		browserPanel.add(viewScroll, BorderLayout.SOUTH);

		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
		gui.addTab(browsePath.substring(browsePath.lastIndexOf(Gui.separator) + 1), this, "SBOL Browser");
	}
	
	public void reload(Gui gui,String browsePath) {
		this.removeAll();
		
		browsePath = browsePath.replace("\\\\", "\\");
		
		selectionPanel = new JPanel(new GridLayout(1,2));
		filterPanel = new JPanel(new GridLayout(1,3));
		viewArea = new JTextArea();
		viewScroll = new JScrollPane();
		
		aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
		aggregateAnnoResolver = new AggregatingResolver.UseFirstFound<SequenceAnnotation, URI>();
		aggregateSeqResolver = new AggregatingResolver.UseFirstFound<DnaSequence, URI>();
		aggregateLibResolver = new AggregatingResolver.UseFirstFound<org.sbolstandard.core.Collection, URI>();
		
		localLibURIs = new LinkedList<URI>();
		localLibIds = new LinkedList<String>();
		localCompURIs = new LinkedList<URI>();
		
		loadSbolFiles(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION), browsePath);
		
		constructBrowser(new HashSet<String>());
		
		JPanel browserPanel = new JPanel(new BorderLayout());
		browserPanel.add(filterPanel, BorderLayout.NORTH);
		browserPanel.add(selectionPanel, BorderLayout.CENTER);
		browserPanel.add(viewScroll, BorderLayout.SOUTH);
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL Browser", browserPanel);
		this.add(browserTab);
	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SBOLBrowser(HashSet<String> sbolFilePaths, Set<String> filter) {
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
		String browseFile = browsePath.substring(browsePath.lastIndexOf(Gui.separator) + 1);
		for (String filePath : sbolFilePaths) {
			String file = filePath.substring(filePath.lastIndexOf(Gui.separator) + 1);
			if (browsePath.equals("") || browseFile.equals(file)) {
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
				if (sbolDoc != null) {
					SBOLDocumentImpl flatDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
					compResolvers.add(flatDoc.getComponentUriResolver());
					annoResolvers.add(flatDoc.getAnnotationUriResolver());
					seqResolvers.add(flatDoc.getSequenceUriResolver());
					libResolvers.add(flatDoc.getCollectionUriResolver());
					for (SBOLRootObject sbolObj : flatDoc.getContents())
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
		Set<String> dnacTypes = new LinkedHashSet<String>();
		dnacTypes.add("all");
		for (int i = 0; i < localCompURIs.size(); i++) {
			DnaComponent localComp = null;
			try {
				localComp = aggregateCompResolver.resolve(localCompURIs.get(i));
			} catch (MergerException e) {
				e.printStackTrace();
				return;
			}
			if (localComp.getTypes().size() > 0) {	
				dnacTypes.add(SBOLUtility.convertURIToSOTerm(localComp.getTypes().iterator().next()));
			}
		}
		filterBox = new JComboBox(dnacTypes.toArray(new String[dnacTypes.size()]));
		
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
		
		filterBox.setActionCommand("filterSBOL");
		filterBox.addActionListener(this);
		JLabel filterLabel = new JLabel("Filter by Type:  ");
		filterLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		filterPanel.add(filterLabel);
		filterPanel.add(filterBox);
		filterPanel.add(new JLabel());
	}
	
	public LinkedList<URI> getSelection() {
		return selectedCompURIs;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("filterSBOL")) {
			libPanel.displaySelected();
			compPanel.filterComponents(filterBox.getSelectedItem().toString());
		} 
		
	}
}
