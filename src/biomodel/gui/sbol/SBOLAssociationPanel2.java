package biomodel.gui.sbol;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import main.Gui;

import org.sbolstandard.core2.*;

import biomodel.gui.schematic.ModelEditor;
import biomodel.util.GlobalConstants;
import sbol.browser.SBOLBrowser2;
import sbol.util.SBOLFileManager2;
import sbol.util.SBOLIdentityManager2;
import sbol.util.SBOLUtility2;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import javax.swing.JCheckBox;

public class SBOLAssociationPanel2 extends JPanel implements ActionListener 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SBOLDocument SBOLDOC; 
//	private static final long serialVersionUID = 1L;
	private HashSet<String> sbolFilePaths;
	private List<URI> compURIs;
	private List<URI> defaultCompURIs;
	private boolean defaultMinusBoxState;
	private Set<URI> soTypes;
	private ModelEditor modelEditor; 
//	private UseFirstFound<DnaComponent, URI> aggregateCompResolver; 
	private JList compList 	   = new JList();
	private JCheckBox minusBox = new JCheckBox();
	private String[] options;
	private boolean iBioSimURIPresent;
	private URI removedBioSimURI;
	private JButton add    			= new JButton("Add");
	private JButton remove 			= new JButton("Remove");
	private JButton editDescriptors = new JButton("Edit/View");
	private JButton addMove 		= new JButton("Add/Move Composite");
	
	public SBOLAssociationPanel2(HashSet<String> sbolFilePaths, List<URI> defaultCompURIs, 
			String defaultCompStrand, Set<URI> soTypes, ModelEditor modelEditor) 
	{
		super(new BorderLayout());
		SBOLDOC = new SBOLDocument(); 
		this.sbolFilePaths = sbolFilePaths;
		this.modelEditor = modelEditor;
		if (defaultCompURIs.size() == 0) 
		{
			compURIs = new LinkedList<URI>();
			insertPlaceHolder();
		} 
		else 
		{
			compURIs = new LinkedList<URI>(defaultCompURIs);
		}
		this.defaultCompURIs = defaultCompURIs;
		minusBox.setSelected(defaultCompStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND));
		defaultMinusBoxState = minusBox.isSelected();
		this.soTypes = soTypes;
		
		options = new String[]{"Ok", "Cancel"};
		constructPanel();
	}
	
	public SBOLAssociationPanel2(HashSet<String> sbolFilePaths, List<URI> defaultCompURIs, 
			String defaultCompStrand, Set<URI> soTypes) 
	{
		super(new BorderLayout());
		SBOLDOC = new SBOLDocument(); 
		this.sbolFilePaths = sbolFilePaths;
			
		compURIs = new LinkedList<URI>(defaultCompURIs);
		this.defaultCompURIs = defaultCompURIs;
		minusBox.setSelected(defaultCompStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND));
		defaultMinusBoxState = minusBox.isSelected();
		this.soTypes = soTypes;
		
		options = new String[]{"Ok", "Cancel"};
		constructPanel();
	}
	
	public void constructPanel() 
	{
		JLabel      associationLabel = new JLabel("Associated DNA Components:"); 
		JScrollPane componentScroll  = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		this.add(associationLabel, "North");
		this.add(componentScroll, "Center");
		
		JPanel minusPanel = new JPanel();
		JLabel minusLabel = new JLabel("Reverse Complement");
		minusPanel.add(minusBox);
		minusPanel.add(minusLabel);
		JPanel metaPanel      = new JPanel(new GridLayout(2, 1));
		JPanel compositePanel = new JPanel(new GridLayout(1, 2));
		add.addActionListener(this);
		remove.addActionListener(this);
		addMove.addActionListener(this);
		editDescriptors.addActionListener(this);
		compositePanel.add(add);
		compositePanel.add(remove);
		if (modelEditor == null) 
		{
			//compositePanel.add(editDescriptors);
		} 
		else 
		{
			compositePanel.add(editDescriptors);
			compositePanel.add(addMove);
		}
		metaPanel.add(minusPanel);
		metaPanel.add(compositePanel);
		this.add(metaPanel, "South");
		
		if (loadSBOLFiles(sbolFilePaths)) {
			if (setComponentIDList())
				panelOpen();
		}
		
		//TODO: comment out for now
//			SBOLDOC = SBOLUtility2.loadSBOLFiles(sbolFilePaths);
//			
//			if (setComponentIDList())
//				panelOpen();
	}
	
	private boolean loadSBOLFiles(HashSet<String> sbolFilePaths) 
	{
//		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		
		for (String filePath : sbolFilePaths) 
		{
			SBOLDocument sbolDoc = SBOLUtility2.loadSBOLFile(filePath);
			if (sbolDoc != null) 
			{
				//Remove duplicate objects within an sbol document
//				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
//				compResolvers.add(flattenedDoc.getComponentUriResolver());
				for(ComponentDefinition c : sbolDoc.getComponentDefinitions())
				{
					if(SBOLDOC.getComponentDefinition(c.getIdentity()) == null) 
					{
						try {
							SBOLDOC.createCopy(c);
						}
						catch (SBOLValidationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			} 
			else
				return false;
		}
		
//		aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
//		aggregateCompResolver.setResolvers(compResolvers);
		
		if (sbolFilePaths.size() == 0) 
		{
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean setComponentIDList() 
	{
		LinkedList<String> compIdNames = new LinkedList<String>();
		LinkedList<Integer> dissociationIndices = new LinkedList<Integer>();
		iBioSimURIPresent = false;
		for (int i = compURIs.size() - 1; i >= 0; i--) 
		{
			URI uri = compURIs.get(i);
			String compIdName = "";
			if (uri.toString().endsWith("iBioSimPlaceHolder")) 
			{
				iBioSimURIPresent = true;
				String modelID = modelEditor.getBioModel().getSBMLDocument().getModel().getId();
				compIdName = modelID + " (Placeholder for iBioSim Composite DNA Component)";
			} 
			else 
			{
//				DnaComponent resolvedComp = null;
				ComponentDefinition resolvedComp = null; 
				resolvedComp = SBOLDOC.getComponentDefinition(uri);
				if (resolvedComp == null) {
					Set<TopLevel> wasDerivedFrom = SBOLDOC.getByWasDerivedFrom(uri);
					if (wasDerivedFrom.size() > 0) {
						resolvedComp = (ComponentDefinition)wasDerivedFrom.iterator().next();
						compURIs.set(i, resolvedComp.getIdentity());
					}
				}
				if (resolvedComp != null) 
				{
					compIdName = resolvedComp.getDisplayId();
					if (resolvedComp.getName() != null && !resolvedComp.getName().equals(""))
						compIdName = compIdName + " : " + resolvedComp.getName();
					if (uri.toString().endsWith("iBioSim")) {
						iBioSimURIPresent = true;
						compIdName = compIdName + " (iBioSim Composite DNA Component)";
					}
				} else 
				{
					Object[] options = { "OK", "Cancel" };
					int choice = JOptionPane.showOptionDialog(Gui.frame, 
							"Currently associated DNA component with URI " + uri.toString() +
							" is not found in project SBOL files and could not be loaded. " +
							"Would like to dissociate this component?", 
							"DNA Component Not Found", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
					if (choice == 0) 
						dissociationIndices.add(i);
					else
						return false;
				}
			}
			if (compIdName.length() > 0)
				compIdNames.addFirst(compIdName);
		}
		Object[] idObjects = compIdNames.toArray();
		compList.setListData(idObjects);
		for (int i : dissociationIndices) 
		{
			URI compURI = compURIs.remove(i);
			if (compURI.toString().endsWith("iBioSim"))
				insertPlaceHolder(new int[]{i});	
		}
		return true;
	}
	
	
	private void panelOpen() 
	{
		int choice = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Association", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (choice == 0) return;
		compURIs = defaultCompURIs;
		minusBox.setSelected(defaultMinusBoxState);
		removedBioSimURI = null;
	}
	
	private void moveIBioSimURI() 
	{
		int[] selectedIndices = compList.getSelectedIndices();
		int destinationIndex;
		if (selectedIndices.length == 0)
			destinationIndex = compURIs.size();
		else
			destinationIndex = selectedIndices[selectedIndices.length - 1];
		int originIndex = -1;
		do {
			originIndex++;
		} while (!compURIs.get(originIndex).toString().endsWith("iBioSim") 
				&& !compURIs.get(originIndex).toString().endsWith("iBioSimPlaceHolder"));
		URI compURI = compURIs.remove(originIndex);
		if (destinationIndex > originIndex)
			destinationIndex--;
		compURIs.add(destinationIndex, compURI);
		setComponentIDList();
	}
	
//	@SuppressWarnings("unused")
//	private LinkedList<URI> getSelectedURIs() {
//		LinkedList<URI> selectedURIs = new LinkedList<URI>();
//		int[] selectedIndices = compList.getSelectedIndices();
//		for (int i = 0; i < selectedIndices.length; i++)
//			selectedURIs.add(compURIs.get(selectedIndices[i]));
//		return selectedURIs;
//	}
	
	private void insertComponentURIs(LinkedList<URI> insertionURIs) 
	{
		int[] selectedIndices = compList.getSelectedIndices();
		insertComponentURIs(insertionURIs, selectedIndices);
	}
	
	private void insertComponentURIs(LinkedList<URI> insertionURIs, int[] insertionIndices) 
	{
		int insertionIndex;
		if (insertionIndices.length == 0)
			insertionIndex = compURIs.size();
		else
			insertionIndex = insertionIndices[insertionIndices.length - 1];
		compURIs.addAll(insertionIndex, insertionURIs);
		setComponentIDList();
	}
	
	private void insertPlaceHolder() 
	{
		insertComponentURIs(createPlaceHolder());
	}
	
	private void insertPlaceHolder(int[] insertionIndices) 
	{
		insertComponentURIs(createPlaceHolder(), insertionIndices);
	}
	
	private static LinkedList<URI> createPlaceHolder() 
	{
		LinkedList<URI> placeHolderList = new LinkedList<URI>();
		try 
		{
			placeHolderList.add(new URI("http://www.async.ece.utah.edu#iBioSimPlaceHolder"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return placeHolderList;
	}
	
	private void removeSelectedURIs() {
		int[] selectedIndices = compList.getSelectedIndices();
		if (selectedIndices.length == 0) {
			return;
			// TODO: Removed, not sure purpose of this
			//selectedIndices = new int[]{compURIs.size() - 1};
		}
		for (int i = selectedIndices.length; i > 0; i--) {
			URI compURI = compURIs.remove(selectedIndices[i - 1]);
			if (compURI.toString().endsWith("iBioSim"))	{
				removedBioSimURI = compURI;
				int[] insertionIndex = new int[]{selectedIndices[i - 1]};
				insertPlaceHolder(insertionIndex);
			}
		}
		setComponentIDList();
	}
	
	public List<URI> getComponentURIs() {
		return compURIs;
	}
	
	public URI getRemovedBioSimURI() {
		return removedBioSimURI;
	}
	
	public String getComponentStrand() {
		if (minusBox.isSelected())
			return GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND;
		return GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND;
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == addMove) 
		{
			if (iBioSimURIPresent)
				moveIBioSimURI();
			else 
				insertPlaceHolder();
		} 
		else if (e.getSource() == editDescriptors) 
		{
			Gui gui = modelEditor.getGui();
//			SBOLFileManager fileManager = new SBOLFileManager(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
			SBOLFileManager2 fileManager = new SBOLFileManager2(gui.getFilePaths(GlobalConstants.SBOL_FILE_EXTENSION));
			if (compList.getSelectedIndex()==-1) return;
			if (((String)compList.getSelectedValue()).contains("(Placeholder for iBioSim Composite DNA Component)")) 
			{
				if (fileManager.sbolFilesAreLoaded()) 
				{
//					SBOLIdentityManager identityManager = new SBOLIdentityManager(modelEditor.getBioModel());
					SBOLIdentityManager2 identityManager = new SBOLIdentityManager2(modelEditor.getBioModel());
//					SBOLDescriptorPanel descriptorPanel = new SBOLDescriptorPanel(identityManager, fileManager);
					SBOLDescriptorPanel2 descriptorPanel = new SBOLDescriptorPanel2(identityManager, fileManager);
					while (descriptorPanel.panelOpen(identityManager, fileManager));
				}
			} 
			else 
			{
//				DnaComponent dnaComponent = null;
				ComponentDefinition dnaComponent = null;
				String SBOLFileName = "";
				for (String s : fileManager.getSBOLFilePaths()) 
				{
//					dnaComponent = fileManager.resolveDisplayID((String)compList.getSelectedValue(), s);
					dnaComponent = SBOLDOC.getComponentDefinition(compURIs.get(compList.getSelectedIndex()));
					if (dnaComponent!= null) 
					{
						SBOLFileName = s;
						break;
					}
				}
				if (dnaComponent!=null) {
					SBOLDescriptorPanel2 descriptorPanel = new SBOLDescriptorPanel2(SBOLFileName.substring(SBOLFileName.lastIndexOf(Gui.separator)+1),
							dnaComponent.getDisplayId(),dnaComponent.getName(),dnaComponent.getDescription());
					descriptorPanel.openViewer();
				}
			}
		} 
		else if (e.getSource() == add) 
		{
//			SBOLBrowser browser = new SBOLBrowser(sbolFilePaths, soTypes);
			SBOLBrowser2 browser = new SBOLBrowser2(sbolFilePaths, soTypes);
			insertComponentURIs(browser.getSelection());
		} 
		else if (e.getSource() == remove) 
		{
			removeSelectedURIs();
		}
	}

}
