package sbol;

import java.awt.*;

import javax.swing.*;

import main.Gui;

import org.sbolstandard.core.*;
import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.SBOLDocumentImpl;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;

import sbol.SBOLUtility;

import java.net.URI;
import java.util.*;

public class SBOLAssociationPanel extends JPanel {

	private HashSet<String> sbolFiles;
	private LinkedList<URI> compURIs;
	private LinkedList<URI> defaultCompURIs;
	private Set<String> soTypes;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver;
	private JList compList = new JList();
	private String[] options = {"Add", "Remove", "Ok", "Cancel"};
	
	public SBOLAssociationPanel(HashSet<String> sbolFiles, LinkedList<URI> defaultCompURIs, Set<String> soTypes) {
		super(new BorderLayout());
		
		this.sbolFiles = sbolFiles;
		compURIs = new LinkedList<URI>(defaultCompURIs);
		this.defaultCompURIs = defaultCompURIs;
	
		this.soTypes = soTypes;
		
		JLabel associationLabel = new JLabel("Associated DNA Components:");
	
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(compList);
		
		this.add(associationLabel, "North");
		this.add(componentScroll, "Center");
		
		if (loadSBOLFiles(sbolFiles)) {
			setComponentIDList();
			boolean display = true;
			while (display)
				display = panelOpen();
		}
	}
	
	private boolean loadSBOLFiles(HashSet<String> sbolFiles) {
		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		for (String filePath : sbolFiles) {
			SBOLDocumentImpl sbolDoc = (SBOLDocumentImpl) SBOLUtility.loadSBOLFile(filePath);
			if (sbolDoc != null) {
				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenDocument(sbolDoc);
				compResolvers.add(flattenedDoc.getComponentUriResolver());
			} else
				return false;
		}
		aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
		aggregateCompResolver.setResolvers(compResolvers);
		if (sbolFiles.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	private void setComponentIDList() {
		LinkedList<String> compIdNames = new LinkedList<String>();
		for (int i = 0; i < compURIs.size(); i++) {
			URI uri = compURIs.get(i);
			DnaComponent resolvedComp = aggregateCompResolver.resolve(uri);
			if (resolvedComp != null) {
				if (resolvedComp.getName() != null && !resolvedComp.getName().equals(""))
					compIdNames.add(resolvedComp.getDisplayId() + " : " + resolvedComp.getName());
				else
					compIdNames.add(resolvedComp.getDisplayId());
			} else 
				JOptionPane.showMessageDialog(Gui.frame, "Currently associated component with URI " + uri.toString() +
						" is not found in project SBOL files and could not be loaded.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		}
		Object[] idObjects = compIdNames.toArray();
		compList.setListData(idObjects);
	}
	
	private boolean panelOpen() {
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL ", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == 0) {
			SBOLBrowser browser = new SBOLBrowser(sbolFiles, soTypes, getSelectedURIs());
			insertComponents(browser.getSelection());
			return true;
		} else if (option == 1) {
			removeSelectedURIs();
			setComponentIDList();
			return true;
		} else if (option == 2) 
			return false;
		else {
			compURIs = defaultCompURIs;
			return false;
		}
	}
	
	private LinkedList<URI> getSelectedURIs() {
		LinkedList<URI> selectedURIs = new LinkedList<URI>();
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = 0; i < selectedIndices.length; i++)
			selectedURIs.add(compURIs.get(selectedIndices[i]));
		return selectedURIs;
	}
	
	private void insertComponents(LinkedList<URI> insertionURIs) {
		int[] selectedIndices = compList.getSelectedIndices();
		int insertionIndex;
		if (selectedIndices.length == 0)
			insertionIndex = compURIs.size();
		else
			insertionIndex = selectedIndices[selectedIndices.length - 1] + 1;
		compURIs.addAll(insertionIndex, insertionURIs);
		setComponentIDList();
	}
	
	private void removeSelectedURIs() {
		int[] selectedIndices = compList.getSelectedIndices();
		for (int i = selectedIndices.length; i > 0; i--)
			compURIs.remove(selectedIndices[i - 1]);
		setComponentIDList();
	}
	
	public LinkedList<URI> getCompURIs() {
		return compURIs;
	}
}
