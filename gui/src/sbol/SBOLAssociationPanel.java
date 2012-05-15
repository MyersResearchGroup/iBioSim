package sbol;

import java.awt.*;

import javax.swing.*;

import main.Gui;

import org.sbolstandard.core.*;

import sbol.SBOLUtility;

import java.util.*;

public class SBOLAssociationPanel extends JPanel {

	private HashSet<String> sbolFiles;
	private LinkedList<String> compURIs;
	private LinkedList<String> defaultCompURIs;
	private Set<String> soTypes;
	HashMap<String, String> uriToID;
	private JList compList = new JList();
	private String[] options = {"Add", "Remove", "Ok", "Cancel"};
	
	public SBOLAssociationPanel(HashSet<String> sbolFiles, LinkedList<String> defaultCompURIs, Set<String> soTypes) {
		super(new BorderLayout());
		
		this.sbolFiles = sbolFiles;
		compURIs = new LinkedList<String>(defaultCompURIs);
		this.defaultCompURIs = defaultCompURIs;
	
		this.soTypes = soTypes;
		
		JLabel associationLabel = new JLabel("Associated DNA Components:");
	
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		this.add(associationLabel, "North");
		this.add(componentScroll, "Center");
		
		loadSBOLFiles(sbolFiles);
		boolean set = setComponentIDList();
		
		if (set) {
			boolean display = true;
			while (display)
				display = panelOpen();
		}
	}
	
	private void loadSBOLFiles(HashSet<String> sbolFiles) {
		uriToID = new HashMap<String, String>();
		for (String filePath : sbolFiles) {
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
			if (sbolDoc != null)
				for (DnaComponent dnac : SBOLUtility.loadDNAComponents(sbolDoc).values()) 
					if (dnac.getDisplayId() != null && !dnac.getDisplayId().equals(""))
						uriToID.put(dnac.getURI().toString(), dnac.getDisplayId());
					else
						uriToID.put(dnac.getURI().toString(), "unidentified");	
		}
	}
	
	private boolean setComponentIDList() {
		LinkedList<String> compIDs = new LinkedList<String>();
		for (int i = 0; i < compURIs.size(); i++) {
			String uri = compURIs.get(i);
			if (uriToID.containsKey(uri))
				compIDs.add(uriToID.get(uri));
			else {
				JOptionPane.showMessageDialog(Gui.frame, "Currently associated component with URI " + uri +
						" is not found in project SBOL files and could not be loaded.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
				compURIs = defaultCompURIs;
				return false;
			}
		}
		Object[] idObjects = compIDs.toArray();
		compList.setListData(idObjects);
		return true;
	}
	
	private boolean panelOpen() {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL ", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == 0) {
			SBOLBrowser browser = new SBOLBrowser(sbolFiles, soTypes, getSelectedURIs());
			return insertComponents(browser.getSelection());
		} else if (option == 1) {
			removeSelectedURIs();
			return setComponentIDList();
		} else if (option > 2) 
			compURIs = defaultCompURIs;
		return false;
	}
	
	private LinkedList<String> getSelectedURIs() {
		LinkedList<String> selectedURIs = new LinkedList<String>();
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = 0; i < selectedIndices.length; i++)
			selectedURIs.add(compURIs.get(selectedIndices[i]));
		return selectedURIs;
	}
	
	private boolean insertComponents(LinkedList<String> insertionURIs) {
		int[] selectedIndices = compList.getSelectedIndices();
		int insertionIndex;
		if (selectedIndices.length == 0)
			insertionIndex = compURIs.size();
		else
			insertionIndex = selectedIndices[selectedIndices.length - 1] + 1;
		compURIs.addAll(insertionIndex, insertionURIs);
		return setComponentIDList();
	}
	
	private void removeSelectedURIs() {
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = selectedIndices.length; i > 0; i--)
			compURIs.remove(selectedIndices[i - 1]);
		setComponentIDList();
	}
	
	public LinkedList<String> getCompURIs() {
		return compURIs;
	}
}
