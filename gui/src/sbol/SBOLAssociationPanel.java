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
	
		this.soTypes = soTypes;
		
		JLabel associationLabel = new JLabel("Associated DNA Components:");
	
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		this.add(associationLabel, "North");
		this.add(componentScroll, "Center");
		
		loadComponentIDs(sbolFiles);
		setComponentIDList(defaultCompURIs);
		
		boolean display = true;
		while (display)
			display = panelOpen(defaultCompURIs);
	}
	
	private void loadComponentIDs(HashSet<String> sbolFiles) {
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
	
	private void setComponentIDList(LinkedList<String> compURIs) {
		LinkedList<String> compIDs = new LinkedList<String>();
		for (String uri : compURIs)
			compIDs.add(uriToID.get(uri));
		Object[] idObjects = compIDs.toArray();
		compList.setListData(idObjects);
	}
	
	private boolean panelOpen(LinkedList<String> defaultCompURIs) {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL ", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == 0) {
			SBOLBrowser browser = new SBOLBrowser(sbolFiles, soTypes, getSelectedURIs());
			insertComponents(browser.getSelection());
			return true;
		} else if (option == 1) {
			removeSelectedURIs();
			setComponentIDList(compURIs);
			return true;
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
	
	private void insertComponents(LinkedList<String> insertionURIs) {
		int[] selectedIndices = compList.getSelectedIndices();
		int insertionIndex;
		if (selectedIndices.length == 0)
			insertionIndex = compURIs.size();
		else
			insertionIndex = selectedIndices[selectedIndices.length - 1] + 1;
		compURIs.addAll(insertionIndex, insertionURIs);
		setComponentIDList(compURIs);
	}
	
	private void removeSelectedURIs() {
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = selectedIndices.length; i > 0; i--)
			compURIs.remove(selectedIndices[i - 1]);
		setComponentIDList(compURIs);
	}
	
	public LinkedList<String> getCompURIs() {
		return compURIs;
	}
}
