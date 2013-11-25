package biomodel.gui.sbol;

import java.awt.*;

import javax.swing.*;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.SBOLDocumentImpl;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;

import biomodel.util.GlobalConstants;

import sbol.browser.SBOLBrowser;
import sbol.util.SBOLUtility;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import javax.swing.JCheckBox;

public class SBOLAssociationPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private HashSet<String> sbolFilePaths;
//	private HashMap<String, SBOLDocument> sbolFileDocMap = new HashMap<String, SBOLDocument>();
	private List<URI> compURIs;
	private List<URI> defaultCompURIs;
	private boolean defaultMinusBoxState;
	private Set<String> soTypes;
	private String modelID;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver;
	private JList compList = new JList();
	private JCheckBox minusBox = new JCheckBox();
	private String[] options;
	private boolean iBioSimURIPresent;
	private URI removedBioSimURI;
	
	public SBOLAssociationPanel(HashSet<String> sbolFilePaths, List<URI> defaultCompURIs, String defaultCompStrand, Set<String> soTypes, String modelID) {
		super(new BorderLayout());
		
		this.sbolFilePaths = sbolFilePaths;
		
		if (defaultCompURIs.size() == 0) {
			compURIs = new LinkedList<URI>();
			insertPlaceHolder();
		} else 
			compURIs = new LinkedList<URI>(defaultCompURIs);
		this.defaultCompURIs = defaultCompURIs;
		minusBox.setSelected(defaultCompStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND));
		defaultMinusBoxState = minusBox.isSelected();
		this.soTypes = soTypes;
		this.modelID = modelID;
		
		
		options = new String[]{"Add/Move Composite", "Add", "Remove", "Ok", "Cancel"};
		constructPanel();
	}
	
	public SBOLAssociationPanel(HashSet<String> sbolFilePaths, List<URI> defaultCompURIs, String defaultCompStrand, Set<String> soTypes) {
		super(new BorderLayout());
		
		this.sbolFilePaths = sbolFilePaths;
			
		compURIs = new LinkedList<URI>(defaultCompURIs);
		this.defaultCompURIs = defaultCompURIs;
		minusBox.setSelected(defaultCompStrand.equals(GlobalConstants.SBOL_ASSEMBLY_MINUS_STRAND));
		defaultMinusBoxState = minusBox.isSelected();
		this.soTypes = soTypes;
		
		options = new String[]{"Add", "Remove", "Ok", "Cancel"};
		constructPanel();
	}
	
	public void constructPanel() {
		JLabel associationLabel = new JLabel("Associated DNA Components:");
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		JPanel minusPanel = new JPanel();
		JLabel minusLabel = new JLabel("Minus Strand");
		minusPanel.add(minusBox);
		minusPanel.add(minusLabel);
		
		this.add(associationLabel, "North");
		this.add(componentScroll, "Center");
		this.add(minusPanel, "South");
		
		if (loadSBOLFiles(sbolFilePaths)) {
			boolean display = setComponentIDList();
			while (display)
				display = panelOpen();
		}
	}
	
	private boolean loadSBOLFiles(HashSet<String> sbolFilePaths) {
		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		for (String filePath : sbolFilePaths) {
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
//			sbolFileDocMap.put(filePath, sbolDoc);
			if (sbolDoc != null) {
				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
				compResolvers.add(flattenedDoc.getComponentUriResolver());
			} else
				return false;
		}
		aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
		aggregateCompResolver.setResolvers(compResolvers);
		if (sbolFilePaths.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private boolean setComponentIDList() {
		LinkedList<String> compIdNames = new LinkedList<String>();
		LinkedList<Integer> dissociationIndices = new LinkedList<Integer>();
		iBioSimURIPresent = false;
		for (int i = compURIs.size() - 1; i >= 0; i--) {
			URI uri = compURIs.get(i);
			String compIdName = "";
			if (uri.toString().endsWith("iBioSimPlaceHolder")) {
				iBioSimURIPresent = true;
				compIdName = modelID + " (Placeholder for iBioSim Composite DNA Component)";
			} else {
				DnaComponent resolvedComp = null;
				try {
					resolvedComp = aggregateCompResolver.resolve(uri);
				} catch (MergerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (resolvedComp != null) {
					compIdName = resolvedComp.getDisplayId();
					if (resolvedComp.getName() != null && !resolvedComp.getName().equals(""))
						compIdName = compIdName + " : " + resolvedComp.getName();
					if (uri.toString().endsWith("iBioSim")) {
						iBioSimURIPresent = true;
						compIdName = compIdName + " (iBioSim Composite DNA Component)";
					}
				} else {
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
		for (int i : dissociationIndices) {
			URI compURI = compURIs.remove(i);
			if (compURI.toString().endsWith("iBioSim"))
				insertPlaceHolder(new int[]{i});	
		}
		return true;
	}
	
	private boolean panelOpen() {
		int mode = 5 - options.length;
		int choice = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Association", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (choice >= 0)
			choice += mode;
		if (choice == 0) {
			if (iBioSimURIPresent) {
				moveIBioSimURI();
			} else 
				insertPlaceHolder();
			return true;
		} else if (choice == 1) {
			SBOLBrowser browser = new SBOLBrowser(sbolFilePaths, soTypes, getSelectedURIs());
			insertComponentURIs(browser.getSelection());
			return true;
		} else if (choice == 2) {
			removeSelectedURIs();
			return true;
		} else if (choice == 3) 
			return false;
		else {
			compURIs = defaultCompURIs;
			minusBox.setSelected(defaultMinusBoxState);
			removedBioSimURI = null;
			return false;
		}
	}
	
	private void moveIBioSimURI() {
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
	
	private LinkedList<URI> getSelectedURIs() {
		LinkedList<URI> selectedURIs = new LinkedList<URI>();
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = 0; i < selectedIndices.length; i++)
			selectedURIs.add(compURIs.get(selectedIndices[i]));
		return selectedURIs;
	}
	
	private void insertComponentURIs(LinkedList<URI> insertionURIs) {
		int[] selectedIndices = compList.getSelectedIndices();
		insertComponentURIs(insertionURIs, selectedIndices);
	}
	
	private void insertComponentURIs(LinkedList<URI> insertionURIs, int[] insertionIndices) {
		int insertionIndex;
		if (insertionIndices.length == 0)
			insertionIndex = compURIs.size();
		else
			insertionIndex = insertionIndices[insertionIndices.length - 1];
		compURIs.addAll(insertionIndex, insertionURIs);
		setComponentIDList();
	}
	
	private void insertPlaceHolder() {
		insertComponentURIs(createPlaceHolder());
	}
	
	private void insertPlaceHolder(int[] insertionIndices) {
		insertComponentURIs(createPlaceHolder(), insertionIndices);
	}
	
	private LinkedList<URI> createPlaceHolder() {
		LinkedList<URI> placeHolderList = new LinkedList<URI>();
		try {
			placeHolderList.add(new URI("http://www.async.ece.utah.edu#iBioSimPlaceHolder"));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return placeHolderList;
	}
	
	private void removeSelectedURIs() {
		int[] selectedIndices = compList.getSelectedIndices();
		if (selectedIndices.length == 0) {
			selectedIndices = new int[]{compURIs.size() - 1};
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

}
