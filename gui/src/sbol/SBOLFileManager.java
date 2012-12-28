package sbol;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import main.Gui;

import org.sbml.libsbml.Model;
import org.sbolstandard.core.DnaComponent;
import org.sbolstandard.core.Resolver;
import org.sbolstandard.core.SBOLDocument;
import org.sbolstandard.core.SequenceAnnotation;
import org.sbolstandard.core.impl.AggregatingResolver;
import org.sbolstandard.core.impl.SBOLDocumentImpl;
import org.sbolstandard.core.impl.AggregatingResolver.UseFirstFound;
import org.sbolstandard.core.util.SBOLDeepEquality;
import org.sbolstandard.core.util.SequenceOntology;

import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.parser.BioModel;

public class SBOLFileManager {
	
	private String projectDirectory;
	private HashMap<String, SBOLDocument> fileDocMap = new HashMap<String, SBOLDocument>();
	private boolean sbolFilesAreLoaded = true;
	private String locatedFileID;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
	
	public SBOLFileManager(String projectDirectory, Set<String> sbolFileIDs) {
		if (sbolFileIDs.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			sbolFilesAreLoaded = false;
		} else {
			this.projectDirectory = projectDirectory;
			LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
			Iterator<String> sbolFileIterator = sbolFileIDs.iterator();
			do {
				String sbolFileID = sbolFileIterator.next();
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(projectDirectory + File.separator + sbolFileID);
				if (sbolDoc != null) {
					fileDocMap.put(sbolFileID, sbolDoc);
					SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
					Resolver<DnaComponent, URI> compResolver = flattenedDoc.getComponentUriResolver();
					compResolvers.add(compResolver);
				} else
					sbolFilesAreLoaded = false;
			} while(sbolFileIterator.hasNext() && sbolFilesAreLoaded);
			aggregateCompResolver.setResolvers(compResolvers);
		}
	}
	
	public boolean sbolFilesAreLoaded() {
		return sbolFilesAreLoaded;
	}
	
	public DnaComponent resolveURI(URI uri) {
		DnaComponent resolvedComp = aggregateCompResolver.resolve(uri);
		if (resolvedComp == null)
			JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + uri.toString() +
					" could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return resolvedComp;
	}
	
	public DnaComponent resolveAndLocateTopLevelURI(URI uri) {
		for (String sbolFileID : fileDocMap.keySet()) {
			DnaComponent resolvedComp = ((SBOLDocumentImpl) fileDocMap.get(sbolFileID))
					.getComponentUriResolver().resolve(uri);
			if (resolvedComp != null) {
				locatedFileID = sbolFileID;
				return resolvedComp;
			}
		}
		JOptionPane.showMessageDialog(Gui.frame, "DNA component with URI " + uri.toString() +
				" could not be found in project SBOL files.", "DNA Component Not Found", JOptionPane.ERROR_MESSAGE);
		return null;
	}
	
	public DnaComponent resolveDisplayID(String displayID, String sbolFileID) {
		SBOLDocument sbolDoc = fileDocMap.get(sbolFileID);
		if (sbolDoc != null) 
			return ((SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc))
					.getComponentDisplayIdResolver().resolve(displayID);
		else
			return null;
	
	}
	
	public DnaComponent saveDNAComponent(DnaComponent dnaComp, SBOLIdentityManager identityManager) {
		BioModel biomodel = identityManager.getBioModel();
		String targetFileID = biomodel.getSBOLSaveFileID();
		if (biomodel.getSBOLSaveFileID() != null)
			targetFileID = biomodel.getSBOLSaveFileID();
		else if (identityManager.getBioSimComponent() != null)
			targetFileID = "";
		else 
			targetFileID = fileDocMap.keySet().iterator().next();
		
		// Save component to local SBOL files
		for (String sbolFileID : fileDocMap.keySet()) {
//			if (identityManager.getSynthURI().toString().endsWith("iBioSim") || sbolFileID.equals(targetFileID)) {
			SBOLDocument sbolDoc = fileDocMap.get(sbolFileID);
//				if (identityManager.getSynthURI().toString().endsWith("iBioSim"))
			SBOLUtility.mergeDNAComponent(identityManager.getBioSimURI(), dnaComp, sbolDoc);
			if (sbolFileID.equals(targetFileID))
				SBOLUtility.addDNAComponent(dnaComp, sbolDoc);
			SBOLUtility.writeSBOLDocument(projectDirectory + File.separator + sbolFileID, sbolDoc);
//			}
		}
		return dnaComp;
	} 
	
	public void deleteDNAComponent(URI deletingURI) {
		for (String sbolFileID : fileDocMap.keySet()) {
			SBOLDocument sbolDoc = fileDocMap.get(sbolFileID);
			SBOLUtility.deleteDNAComponent(deletingURI, sbolDoc);
			SBOLUtility.writeSBOLDocument(projectDirectory + File.separator + sbolFileID, sbolDoc);
		}
	}
	
	public Set<String> getSBOLFileIDs() {
		return fileDocMap.keySet();
	}
	
	public String getLocatedFileID() {
		return locatedFileID;
	}
	
}
