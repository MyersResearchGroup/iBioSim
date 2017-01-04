package backend.sbol.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

import backend.biomodel.parser.BioModel;
import backend.biomodel.util.GlobalConstants;
import backend.sbol.util.SBOLIdentityManager2;
import backend.sbol.util.SBOLUtility2;
import frontend.main.Gui;

public class SBOLFileManager2 {
	
	private SBOLDocument SBOLDOC; 
	
	private HashMap<String, SBOLDocument> fileDocMap = new HashMap<String, SBOLDocument>();
	private boolean sbolFilesAreLoaded = true;
	private String locatedFilePath;
//	private UseFirstFound<DnaComponent, URI> aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
	
	public SBOLFileManager2(Set<String> sbolFilePaths) 
	{
		if (sbolFilePaths.size() == 0) 
		{
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			sbolFilesAreLoaded = false;
		} 
		else 
		{
//			LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
			SBOLDOC = new SBOLDocument();
			SBOLDOC.setDefaultURIprefix(GlobalConstants.SBOL_AUTHORITY_DEFAULT); 
//			SBOLDOC.setDefaultURIprefix(GlobalConstants.SO_AUTHORITY);
			Iterator<String> sbolFileIterator = sbolFilePaths.iterator();
			do //Go through each sbol file path and create an SBOLDocument
			{
				String sbolFilePath = sbolFileIterator.next();
//				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(sbolFilePath);
				SBOLDocument sbolDoc = SBOLUtility2.loadSBOLFile(sbolFilePath);
				if (sbolDoc != null) 
				{
					//store each sbol document to its corresponding sbol file path to fileDocMap to keep track of which sbol 
					//document belong to which sbol file.
					fileDocMap.put(sbolFilePath, sbolDoc); 
//					SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
//					Resolver<DnaComponent, URI> compResolver = flattenedDoc.getComponentUriResolver();
//					compResolvers.add(compResolver);
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
					for(Sequence s : sbolDoc.getSequences())
					{
						if(SBOLDOC.getSequence(s.getIdentity()) == null) 
						{
							try {
								SBOLDOC.createCopy(s);
							}
							catch (SBOLValidationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} 
				else
					sbolFilesAreLoaded = false;
			} while(sbolFileIterator.hasNext() && sbolFilesAreLoaded);
//			aggregateCompResolver.setResolvers(compResolvers);
		}
	}
	
	public boolean sbolFilesAreLoaded() {
		return sbolFilesAreLoaded;
	}
	
	public ComponentDefinition resolveURI(URI uri) {
		ComponentDefinition resolvedComp = null;
		resolvedComp = SBOLDOC.getComponentDefinition(uri);
		if (resolvedComp == null)
			JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + uri.toString() +
					" could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return resolvedComp;
	}
	
	public List<ComponentDefinition> resolveURIs(List<URI> uris) {
		boolean error = false;
		List<ComponentDefinition> resolvedComps = new LinkedList<ComponentDefinition>();
		for (URI uri : uris) {
			ComponentDefinition resolvedComp = null;
			resolvedComp = SBOLDOC.getComponentDefinition(uri);
			if (resolvedComp == null) {
				JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + uri.toString() +
						" could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
				error = true;
				resolvedComps.clear();
			} else if (!error)
				resolvedComps.add(resolvedComp);
		}
		return resolvedComps;
	}
	
	public ComponentDefinition resolveAndLocateTopLevelURI(URI uri) {
		for (String sbolFilePath : fileDocMap.keySet()) {
//			DnaComponent resolvedComp = ((SBOLDocumentImpl) fileDocMap.get(sbolFilePath))
			ComponentDefinition resolvedComp = fileDocMap.get(sbolFilePath).getComponentDefinition(uri);
			if (resolvedComp != null) {
				locatedFilePath = sbolFilePath;
				return resolvedComp;
			}
		}
		JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + uri.toString() +
				" could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return null;
	}
	
	
	public ComponentDefinition resolveDisplayID(String displayID, String sbolFilePath) {
		SBOLDocument sbolDoc = fileDocMap.get(sbolFilePath);
		if (sbolDoc != null) 
		{
//			return ((SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc))
//					.getComponentDisplayIdResolver().resolve(displayID);
		}
		return null;
	
	}
	
	public ComponentDefinition getComponentDefinition(String displayId, String version)
	{
		return SBOLDOC.getComponentDefinition(displayId, version);
	}
	
	public void saveDNAComponent(ComponentDefinition dnaComp, SBOLIdentityManager2 identityManager, SBOLDocument tempSbolDoc) throws SBOLValidationException {
		BioModel biomodel = identityManager.getBioModel();
		String targetFilePath = biomodel.getSBOLSaveFilePath();
		if (biomodel.getSBOLSaveFilePath() != null)
			targetFilePath = biomodel.getSBOLSaveFilePath();
		else if (identityManager.getBioSimComponent() != null)
			targetFilePath = "";
		else 
			targetFilePath = fileDocMap.keySet().iterator().next();
		
		// Save component to local SBOL files
		for (String sbolFilePath : fileDocMap.keySet()) 
		{
			SBOLDocument sbolDoc = fileDocMap.get(sbolFilePath);
//			SBOLUtility2.mergeDNAComponent(identityManager.getBioSimURI(), dnaComp, sbolDoc);
			if (sbolFilePath.equals(targetFilePath))
			{
				for(Sequence s : tempSbolDoc.getSequences())
					SBOLUtility2.addSequence(s, sbolDoc, false);
				for(ComponentDefinition c : tempSbolDoc.getComponentDefinitions())
					SBOLUtility2.addDNAComponent(c, sbolDoc, false);
			}
			SBOLUtility2.writeSBOLDocument(sbolFilePath, sbolDoc);
			System.out.println("Wrote sbolAnnot to this file: " + sbolFilePath);
		}
	}
	
	public static void saveDNAComponents(List<ComponentDefinition> dnaComps, String filePath) throws SBOLValidationException {
		SBOLDocument sbolDoc = new SBOLDocument();
		for (ComponentDefinition dnaComp : dnaComps)
			SBOLUtility2.addDNAComponent(dnaComp, sbolDoc, false);
		SBOLUtility2.writeSBOLDocument(filePath, sbolDoc);
	}
	
//	public static void exportDNAComponents(List<DnaComponent> dnaComps, String exportFilePath) {
//		SBOLDocument sbolDoc;
//		File exportFile = new File(exportFilePath);
//		if (exportFile.exists()) {
//			sbolDoc = SBOLUtility.loadSBOLFile(exportFilePath);
//			if (sbolDoc != null) {
//				for (DnaComponent dnaComp : dnaComps)
//					SBOLUtility.addDNAComponent(dnaComp, sbolDoc, false);
//				SBOLUtility.writeSBOLDocument(exportFilePath, sbolDoc);
//			}
//		} else {
//			sbolDoc = SBOLFactory.createDocument();
//			for (DnaComponent dnaComp : dnaComps)
//				SBOLUtility.addDNAComponent(dnaComp, sbolDoc, false);
//			try {
//				exportFile.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			SBOLUtility.writeSBOLDocument(exportFilePath, sbolDoc);
//		}
//	}
//	
//	public void deleteDNAComponent(URI deletingURI) {
//		for (String sbolFilePath : fileDocMap.keySet()) {
//			SBOLDocument sbolDoc = fileDocMap.get(sbolFilePath);
//			SBOLUtility.deleteDNAComponent(deletingURI, sbolDoc);
//			SBOLUtility.writeSBOLDocument(sbolFilePath, sbolDoc);
//		}
//	}
	
	public Set<String> getSBOLFilePaths() {
		return fileDocMap.keySet();
	}
	
	public String getLocatedFilePath() {
		return locatedFilePath;
	}
	
}
