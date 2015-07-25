package sbol.browser;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.MergerException;
import org.sbolstandard.core2.*;

import sbol.util.SBOLUtility;
//import sbol.util.SBOLUtility;
import sbol.util.SBOLUtility2;
import biomodel.util.GlobalConstants;

import java.io.*;
import java.net.URI;
import java.util.*;
import org.sbolstandard.core2.Collection;

import main.Gui;

public class SBOLBrowser2 extends JPanel implements ActionListener {
	
	private static SBOLDocument SBOLDOC; 
	
//	private static final long serialVersionUID = 1L;
	private String[] options = {"Ok", "Cancel"};
	private JPanel      selectionPanel = new JPanel(new GridLayout(1,2));
	private JPanel      filterPanel    = new JPanel(new GridLayout(1,3));
	private JComboBox   filterBox;

	private JTextArea   viewArea   = new JTextArea();
	private JScrollPane viewScroll = new JScrollPane();
	private CollectionBrowserPanel2          libPanel; 
	private ComponentDefinitionBrowserPanel compPanel; // control what to display for Component Definition(s)
	private LinkedList<URI> selectedCompURIs;
	
	private LinkedList<URI>    localLibURIs  = new LinkedList<URI>();    //store Collection Identity(s)
	private LinkedList<String> localLibIds   = new LinkedList<String>(); //store Collection displayId(s)
	private LinkedList<URI>    localCompURIs = new LinkedList<URI>();	 //store ComponentDef. Identity(s)
	private boolean isAssociationBrowser = false;

	private Gui gui;
	private String browsePath;
	
	private String DEFAULTPREFIX = "http://www.async.utah.edu";
	
	//Constructor when browsing a single RDF file from the main gui
	public SBOLBrowser2(Gui gui, String browsePath) {
		super(new BorderLayout());
		
		this.gui        = gui;
		this.browsePath = browsePath;
		this.SBOLDOC    = new SBOLDocument();
//		this.SBOLDOC.setDefaultURIprefix(DEFAULTPREFIX);
		
		browsePath = browsePath.replace("\\\\", "\\");
		
		//TODO: will only look for SBOL extension. extend to other formats (json, ttl, xml)
		loadSbolFiles(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION), browsePath);
		constructBrowser(new HashSet<String>());
	}
		
	public void open() {			
		JPanel browserPanel = new JPanel(new BorderLayout());
		
		browserPanel.add(filterPanel, BorderLayout.NORTH);
		browserPanel.add(selectionPanel, BorderLayout.CENTER);
		browserPanel.add(viewScroll, BorderLayout.SOUTH);
		
		JTabbedPane browserTab = new JTabbedPane();
		browserTab.add("SBOL2 Browser", browserPanel);
		this.add(browserTab);
		gui.addTab(browsePath.substring(browsePath.lastIndexOf(Gui.separator) + 1), this, "SBOL2 Browser");
	}
	
//	public void reload(Gui gui,String browsePath) {
//		this.removeAll();
//		
//		browsePath = browsePath.replace("\\\\", "\\");
//		
//		selectionPanel = new JPanel(new GridLayout(1,2));
//		filterPanel = new JPanel(new GridLayout(1,3));
//		viewArea = new JTextArea();
//		viewScroll = new JScrollPane();
//		
//		aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
//		aggregateAnnoResolver = new AggregatingResolver.UseFirstFound<SequenceAnnotation, URI>();
//		aggregateSeqResolver = new AggregatingResolver.UseFirstFound<DnaSequence, URI>();
//		aggregateLibResolver = new AggregatingResolver.UseFirstFound<org.sbolstandard.core.Collection, URI>();
//		
//		localLibURIs = new LinkedList<URI>();
//		localLibIds = new LinkedList<String>();
//		localCompURIs = new LinkedList<URI>();
//		
//		loadSbolFiles(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION), browsePath);
//		
//		constructBrowser(new HashSet<String>());
//		
//		JPanel browserPanel = new JPanel(new BorderLayout());
//		browserPanel.add(filterPanel, BorderLayout.NORTH);
//		browserPanel.add(selectionPanel, BorderLayout.CENTER);
//		browserPanel.add(viewScroll, BorderLayout.SOUTH);
//		
//		JTabbedPane browserTab = new JTabbedPane();
//		browserTab.add("SBOL Browser", browserPanel);
//		this.add(browserTab);
//	}
	
	//Constructor when browsing RDF file subsets for SBOL to GCM association
	public SBOLBrowser2(HashSet<String> sbolFilePaths, Set<String> filter) {
//		super(new GridLayout(3,1));
		super(new BorderLayout());
		isAssociationBrowser = true;
		selectedCompURIs = new LinkedList<URI>();
		
		loadSbolFiles(sbolFilePaths, "");
	
		constructBrowser(filter);

		this.add(filterPanel, BorderLayout.NORTH);
		this.add(selectionPanel, BorderLayout.CENTER);
		this.add(viewScroll, BorderLayout.SOUTH);

		boolean display = true;
		while (display)
			display = browserOpen();
	}
	
	private void loadSbolFiles(HashSet<String> sbolFilePaths, String browsePath) 
	{
		String browseFile = browsePath.substring(browsePath.lastIndexOf(Gui.separator) + 1);
		
		boolean first = true;
		for (String filePath : sbolFilePaths) 
		{
			String file = filePath.substring(filePath.lastIndexOf(Gui.separator) + 1);
			
			if (browsePath.equals("") || browseFile.equals(file)) 
			{
				SBOLDocument sbolDoc = null;
//				sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
				if (first) {
					SBOLDOC = SBOLUtility2.loadSBOLFile(filePath);
					for(ComponentDefinition c : SBOLDOC.getComponentDefinitions())
					{
						if ((!isAssociationBrowser || !c.getIdentity().toString().endsWith("iBioSim"))) 
							localCompURIs.add(c.getIdentity());
					}
					for(Collection col : SBOLDOC.getCollections())
					{
						if (col.getDisplayId() != null && !col.getDisplayId().equals("") && 
								!localLibURIs.contains(col.getIdentity().toString())) 
						{
							localLibURIs.add(col.getIdentity()); 
							localLibIds.add(col.getDisplayId());
						}
					} 
					first = false;
				} else {
					sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
				}
				if (sbolDoc != null) 
				{
					for(ComponentDefinition c : sbolDoc.getComponentDefinitions())
					{
						if(SBOLDOC.getComponentDefinition(c.getIdentity()) == null) {
//							System.out.println(c.getIdentity());
							SBOLDOC.createCopy(c);
						}
						if ((!isAssociationBrowser || !c.getIdentity().toString().endsWith("iBioSim"))) 
							localCompURIs.add(c.getIdentity());
					}
					for(Sequence s : sbolDoc.getSequences())
					{
						if(SBOLDOC.getSequence(s.getIdentity()) == null)
							SBOLDOC.createCopy(s);
					}
					for(Model m : sbolDoc.getModels())
					{
						if(SBOLDOC.getModel(m.getIdentity()) == null)
							SBOLDOC.createCopy(m);
					}
					for(ModuleDefinition modDef : sbolDoc.getModuleDefinitions())
					{
						if(SBOLDOC.getModuleDefinition(modDef.getIdentity()) == null)
							SBOLDOC.createCopy(modDef);
					}
					for(GenericTopLevel gtl : sbolDoc.getGenericTopLevels())
					{
						if(SBOLDOC.getGenericTopLevel(gtl.getIdentity()) == null)
							SBOLDOC.createCopy(gtl);
					}
					for(Collection col : sbolDoc.getCollections())
					{
						if(SBOLDOC.getCollection(col.getIdentity()) == null)
							SBOLDOC.createCopy(col);
						if (col.getDisplayId() != null && !col.getDisplayId().equals("") && 
								!localLibURIs.contains(col.getIdentity().toString())) 
						{
							localLibURIs.add(col.getIdentity()); 
							localLibIds.add(col.getDisplayId());
						}
					} // end of collection iteration
				} //end of sbol != null
			} //end of searching 1 file
		} // end of iterating through mult files
		
//		java.util.List<String> compDefRoles = new ArrayList<String>();
//		for(ComponentDefinition compDef : SBOLDOC.getComponentDefinitions())
//		{
//			//TODO: Why do you only want the first role of every compDef?
//			compDefRoles.add(SBOLUtility2.convertURIToSOTerm(compDef.getRoles().iterator().next()));
//		}
		
		//TODO: is this how you would use localCompURIs with global sbolDoc to get rid of resolve?
		Set<String> compDefRoles = new LinkedHashSet<String>();
		compDefRoles.add("all");
		for (int i = 0; i < localCompURIs.size(); i++) {
			ComponentDefinition localComp = null;
			try {
//				localComp = aggregateCompResolver.resolve(localCompURIs.get(i));
				localComp = SBOLDOC.getComponentDefinition(localCompURIs.get(i));
			} catch (SBOLValidationException e) {
				e.printStackTrace();
				return;
			}
			if (localComp.getRoles().size() > 0) {	
				compDefRoles.add(SBOLUtility2.convertURIToSOTerm(localComp.getRoles().iterator().next()));
			}
		}
		
		filterBox = new JComboBox(compDefRoles.toArray());
	}
	
	private boolean browserOpen() {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Browser2", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == JOptionPane.YES_OPTION) 
		{
			selectedCompURIs = compPanel.getSelectedURIs();
			if (selectedCompURIs.size() == 0) 
			{
				JOptionPane.showMessageDialog(Gui.frame, "No Component Definition is selected.",
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
		
		compPanel = new ComponentDefinitionBrowserPanel(SBOLDOC, viewArea);
		libPanel = new CollectionBrowserPanel2(SBOLDOC, viewArea, compPanel);
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
	
//	public LinkedList<URI> getSelection() {
//		return selectedCompURIs;
//	}
//
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getActionCommand().equals("filterSBOL")) 
		{
			libPanel.displaySelected();
			compPanel.filterComponents(filterBox.getSelectedItem().toString());
		} 
		
	}
}
