package backend.sbol.browser;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import org.sbolstandard.core2.*;

import java.net.URI;
import java.util.*;

public class CollectionBrowserPanel2 extends JPanel implements MouseListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SBOLDocument SBOLDOC; 
//	private static final long serialVersionUID = 1L;
	
	private LinkedList<URI> localLibURIs;
	private LinkedList<URI> localCompURIs;
	
//	private UseFirstFound<org.sbolstandard.core.Collection, URI> aggregateLibResolver;
//	private UseFirstFound<DnaComponent, URI> aggregateCompResolver;
	
	private JTextArea viewArea;
	private ComponentDefinitionBrowserPanel compPanel;
	
	private JList       libList = new JList();
	private Set<URI> filter  = new HashSet<URI>();
	private boolean isAssociationBrowser = false;
//	
	public CollectionBrowserPanel2(SBOLDocument sbolDoc, JTextArea viewArea, ComponentDefinitionBrowserPanel compDefPanel) {
		super(new BorderLayout());
		this.SBOLDOC = sbolDoc; 
		this.viewArea = viewArea;
		this.compPanel = compDefPanel;
		
		libList.addMouseListener(this);
		
		JLabel libraryLabel = new JLabel("Collections:");
		
		JScrollPane libraryScroll = new JScrollPane();
		libraryScroll.setMinimumSize(new Dimension(260, 200));
		libraryScroll.setPreferredSize(new Dimension(276, 132));
		libraryScroll.setViewportView(libList);
		
		this.add(libraryLabel, "North");
		this.add(libraryScroll, "Center");
	}
	
	public void setLocalLibsComps(LinkedList<String> localLibIds, LinkedList<URI> localLibURIs, LinkedList<URI> localCompURIs) {
		this.localLibURIs = localLibURIs;
		this.localCompURIs = localCompURIs;
		localLibIds.addFirst("all");
		Object[] idObjects = localLibIds.toArray();
		libList.setListData(idObjects);
		libList.setSelectedIndex(0);
		displaySelected();
	}
	
	public void setFilter(Set<URI> filter) {
		this.filter = filter;
	}
	
	public void setIsAssociationBrowser(boolean set) {
		isAssociationBrowser = set;
	}
	
	private URI[] getSelectedURIs() {
		int[] selectedIndices = libList.getSelectedIndices();
		URI[] selectedURIs = new URI[selectedIndices.length];
		for (int i = 0; i < selectedURIs.length; i++) {
			int index = selectedIndices[i];
			// Leave first entry in selectedURIs null if "all" was selected under Collections (see displaySelected())
			if (index != 0)
				selectedURIs[i] = localLibURIs.get(index - 1);
		}
		return selectedURIs;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == libList) {
			viewArea.setText("");
			displaySelected();
		}
	}
	
	public void displaySelected() {
		URI[] selectedURIs = getSelectedURIs();
		LinkedList<String> compIdNames = new LinkedList<String>();
		LinkedList<URI> compURIs = new LinkedList<URI>();
		int n = 0;
		// Case when a specific collection(s) is selected
		if (selectedURIs[0] != null) 
		{
			org.sbolstandard.core2.Collection lib = null;
			lib = SBOLDOC.getCollection(selectedURIs[0]);
			if (lib==null) return;
			if (lib.getName() != null)
				viewArea.append("Name:  " + lib.getName() + "\n");
			else
				viewArea.append("Name:  NA\n");
			if (lib.getDescription() != null)
				viewArea.append("Description:  " + lib.getDescription() + "\n\n");
			else
				viewArea.append("Description:  NA\n\n");
			
			for (TopLevel dnac : lib.getMembers()) 
			{
				if(dnac instanceof ComponentDefinition)
				{
					URI compURI = dnac.getIdentity();
					if (!isAssociationBrowser || !compURI.toString().endsWith("iBioSim")) 
						{
							ComponentDefinition resolvedDnac = null;
							resolvedDnac = SBOLDOC.getComponentDefinition(dnac.getIdentity());
							if ((resolvedDnac != null && processDNAComponent(resolvedDnac, compIdNames, compURIs)) 
									|| processDNAComponent( (ComponentDefinition)dnac, compIdNames, compURIs))
								n++;
						}
				}
			}
		} 
		else 
		{  // Case when "all" is selected under Collections
			
			for (URI compURI : localCompURIs) 
				if (!isAssociationBrowser || !compURI.toString().endsWith("iBioSim")) 
				{
					ComponentDefinition resolvedDnac = null;
					resolvedDnac = SBOLDOC.getComponentDefinition(compURI);
					if (resolvedDnac != null && processDNAComponent(resolvedDnac, compIdNames, compURIs)) 
						n++;
				}
			
		}
		lexoSort(compIdNames, compURIs, n); //order how the compDef list should be viewed
		compPanel.setComponents(compIdNames, compURIs);
	}
	
	private boolean processDNAComponent(ComponentDefinition dnac, LinkedList<String> compIdNames, LinkedList<URI> compURIs) {
		if (filterFeature(dnac)) 
		{
			if (dnac.getName() != null && !dnac.getName().equals(""))
				compIdNames.add(dnac.getDisplayId() + " : " + dnac.getName());
			else
				compIdNames.add(dnac.getDisplayId());
			compURIs.add(dnac.getIdentity());
			return true;
		}
		return false;
	}
	
	private boolean filterFeature(ComponentDefinition dnac) {
 		if (filter.size() == 0)
 			return true;
		for (URI uri : dnac.getRoles()) 
			if (filter.contains(uri.toString()))
				return true;
 		return false;
	}
	
	//Sorts first m entries of string array lexographically
	private static void lexoSort(LinkedList<String> sortingList, LinkedList<URI> companionList, int m) {
		for (int j = 1; j < m; j++) 
		{
			String key = sortingList.get(j);
			URI companionKey = companionList.get(j);
			int i = j - 1;
			while (i >= 0 && sortingList.get(i).compareTo(key) > 0) 
			{
				sortingList.set(i + 1, sortingList.get(i));
				companionList.set(i + 1, companionList.get(i));
				i = i - 1;
			}
			sortingList.set(i + 1, key);
			companionList.set(i + 1, companionKey);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
