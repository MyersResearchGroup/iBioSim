package sbol.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import main.Gui;

import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.MergerException;
import org.sbolstandard.core.Resolver;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SBOLFactory;
import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.SBOLDocumentImpl;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;

import biomodel.parser.BioModel;

public class SBOLFileManager {
	
	private HashMap<String, SBOLDocument> fileDocMap = new HashMap<String, SBOLDocument>();
	private boolean sbolFilesAreLoaded = true;
	private String locatedFilePath;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
	
	public SBOLFileManager(Set<String> sbolFilePaths) 
	{
		if (sbolFilePaths.size() == 0) 
		{
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			sbolFilesAreLoaded = false;
		} 
		else 
		{
			LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
			Iterator<String> sbolFileIterator = sbolFilePaths.iterator();
			do 
			{
				String sbolFilePath = sbolFileIterator.next();
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(sbolFilePath);
				if (sbolDoc != null) 
				{
					fileDocMap.put(sbolFilePath, sbolDoc); 
					SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
					Resolver<DnaComponent, URI> compResolver = flattenedDoc.getComponentUriResolver();
					compResolvers.add(compResolver);
				} 
				else
					sbolFilesAreLoaded = false;
			} while(sbolFileIterator.hasNext() && sbolFilesAreLoaded);
			aggregateCompResolver.setResolvers(compResolvers);
		}
	}
	
	public boolean sbolFilesAreLoaded() {
		return sbolFilesAreLoaded;
	}
	
	public DnaComponent resolveURI(URI uri) {
		DnaComponent resolvedComp = null;
		try {
			resolvedComp = aggregateCompResolver.resolve(uri);
		} catch (MergerException e) {
			e.printStackTrace();
		}
		if (resolvedComp == null)
			JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + uri.toString() +
					" could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return resolvedComp;
	}
	
	public List<DnaComponent> resolveURIs(List<URI> uris) {
		boolean error = false;
		List<DnaComponent> resolvedComps = new LinkedList<DnaComponent>();
		for (URI uri : uris) {
			DnaComponent resolvedComp = null;
			try {
				resolvedComp = aggregateCompResolver.resolve(uri);
			} catch (MergerException e) {
				e.printStackTrace();
			}
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
	
	public DnaComponent resolveAndLocateTopLevelURI(URI uri) {
		for (String sbolFilePath : fileDocMap.keySet()) {
			DnaComponent resolvedComp = ((SBOLDocumentImpl) fileDocMap.get(sbolFilePath))
					.getComponentUriResolver().resolve(uri);
			if (resolvedComp != null) {
				locatedFilePath = sbolFilePath;
				return resolvedComp;
			}
		}
		JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + uri.toString() +
				" could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return null;
	}
	
	public DnaComponent resolveDisplayID(String displayID, String sbolFilePath) {
		SBOLDocument sbolDoc = fileDocMap.get(sbolFilePath);
		if (sbolDoc != null) 
			return ((SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc))
					.getComponentDisplayIdResolver().resolve(displayID);
		return null;
	
	}
	
	public void saveDNAComponent(DnaComponent dnaComp, SBOLIdentityManager identityManager) {
		BioModel biomodel = identityManager.getBioModel();
		String targetFilePath = biomodel.getSBOLSaveFilePath();
		if (biomodel.getSBOLSaveFilePath() != null)
			targetFilePath = biomodel.getSBOLSaveFilePath();
		else if (identityManager.getBioSimComponent() != null)
			targetFilePath = "";
		else 
			targetFilePath = fileDocMap.keySet().iterator().next();
		
		// Save component to local SBOL files
		for (String sbolFilePath : fileDocMap.keySet()) {
//			if (identityManager.getSynthURI().toString().endsWith("iBioSim") || sbolFileID.equals(targetFileID)) {
			SBOLDocument sbolDoc = fileDocMap.get(sbolFilePath);
//				if (identityManager.getSynthURI().toString().endsWith("iBioSim"))
			SBOLUtility.mergeDNAComponent(identityManager.getBioSimURI(), dnaComp, sbolDoc);
			if (sbolFilePath.equals(targetFilePath))
				SBOLUtility.addDNAComponent(dnaComp, sbolDoc, false);
//			SBOLDocument sbolDoc2 = new SBOLDocumentImpl();
//			Set<String> uris = new HashSet<String>();
//			for (SBOLRootObject obj : sbolDoc.getContents()) {
//				if (!uris.contains(obj.getURI().toString())) {
//					sbolDoc2.addContent(obj);
//					uris.add(obj.getURI().toString());
//				}
//			}
			SBOLUtility.writeSBOLDocument(sbolFilePath, sbolDoc);
//			}
		}
	}
	
	public static void saveDNAComponents(List<DnaComponent> dnaComps, String filePath) {
		SBOLDocument sbolDoc = new SBOLDocumentImpl();
		for (DnaComponent dnaComp : dnaComps)
			SBOLUtility.addDNAComponent(dnaComp, sbolDoc, false);
		SBOLUtility.writeSBOLDocument(filePath, sbolDoc);
	}
	
	public static void exportDNAComponents(List<DnaComponent> dnaComps, String exportFilePath) {
		SBOLDocument sbolDoc;
		File exportFile = new File(exportFilePath);
		if (exportFile.exists()) {
			sbolDoc = SBOLUtility.loadSBOLFile(exportFilePath);
			if (sbolDoc != null) {
				for (DnaComponent dnaComp : dnaComps)
					SBOLUtility.addDNAComponent(dnaComp, sbolDoc, false);
				SBOLUtility.writeSBOLDocument(exportFilePath, sbolDoc);
			}
		} else {
			sbolDoc = SBOLFactory.createDocument();
			for (DnaComponent dnaComp : dnaComps)
				SBOLUtility.addDNAComponent(dnaComp, sbolDoc, false);
			try {
				exportFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			SBOLUtility.writeSBOLDocument(exportFilePath, sbolDoc);
		}
	}
	
	public void deleteDNAComponent(URI deletingURI) {
		for (String sbolFilePath : fileDocMap.keySet()) {
			SBOLDocument sbolDoc = fileDocMap.get(sbolFilePath);
			SBOLUtility.deleteDNAComponent(deletingURI, sbolDoc);
			SBOLUtility.writeSBOLDocument(sbolFilePath, sbolDoc);
		}
	}
	
	public Set<String> getSBOLFilePaths() {
		return fileDocMap.keySet();
	}
	
	public String getLocatedFilePath() {
		return locatedFilePath;
	}
	
}
