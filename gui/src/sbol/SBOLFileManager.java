package sbol;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
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
	
	private Set<String> sbolFiles = new HashSet<String>();
	private Set<String> sbolFilePaths;
	private UseFirstFound<DnaComponent, URI> aggregateCompResolver = new AggregatingResolver.UseFirstFound<DnaComponent, URI>();
	
	public SBOLFileManager(Set<String> sbolFilePaths) {
		this.sbolFilePaths = sbolFilePaths;
	}
	
	public boolean loadSBOLFiles() {
		if (sbolFilePaths.size() == 0) {
			JOptionPane.showMessageDialog(Gui.frame, "No SBOL files are found in project.", 
					"File Not Found", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		LinkedList<Resolver<DnaComponent, URI>> compResolvers = new LinkedList<Resolver<DnaComponent, URI>>();
		for (String filePath : sbolFilePaths) {
			String sbolFile = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
			sbolFiles.add(sbolFile);
			SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
			if (sbolDoc != null) {
				SBOLDocumentImpl flattenedDoc = (SBOLDocumentImpl) SBOLUtility.flattenSBOLDocument(sbolDoc);
				Resolver<DnaComponent, URI> compResolver = flattenedDoc.getComponentUriResolver();
				compResolvers.add(compResolver);
			} else
				return false;
		}
		aggregateCompResolver.setResolvers(compResolvers);
		return true;
	}
	
	public DnaComponent resolveURI(URI uri) {
		return aggregateCompResolver.resolve(uri);
	}
	
	public Set<String> getSBOLFiles() {
		return sbolFiles;
	}
	
	public DnaComponent saveDNAComponent(BioModel biomodel, DnaComponent dnaComp, String saveDirectory, 
			SBOLIdentityManager identityManager) {
		String targetFile = biomodel.getSBOLSaveFile();
		if (biomodel.getSBOLSaveFile() != null)
			targetFile = biomodel.getSBOLSaveFile();
		else if (identityManager.getBioSimComp() != null)
			targetFile = "";
		else
			targetFile = sbolFiles.iterator().next();
		
		// Save component to local SBOL files
		for (String sbolFile : sbolFiles) {
			if (identityManager.getBioSimURI().toString().endsWith("iBioSim") || sbolFile.equals(targetFile)) {
				SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(saveDirectory + File.separator + sbolFile);
				if (identityManager.getBioSimURI().toString().endsWith("iBioSim"))
					SBOLUtility.mergeDNAComponent(identityManager.getBioSimURI(), dnaComp, sbolDoc);
				if (sbolFile.equals(targetFile))
					SBOLUtility.addDNAComponent(dnaComp, sbolDoc);
				SBOLUtility.writeSBOLDocument(saveDirectory + File.separator + sbolFile, sbolDoc);
			}
		}
		return dnaComp;
	} 
	
	
	
}
