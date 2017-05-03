/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.dataModels.sbol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLIdentityManager;
import edu.utah.ece.async.ibiosim.dataModels.sbol.SBOLUtility;

/**
 * 
 *
 * @author Nicholas Roehner
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLFileManager {
	
	private SBOLDocument SBOLDOC; 
	
	private HashMap<String, SBOLDocument> fileDocMap = new HashMap<String, SBOLDocument>();
	private boolean sbolFilesAreLoaded = true;
	private String locatedFilePath;
	
	public SBOLFileManager(Set<String> sbolFilePaths, String defaultURIPrefix) throws SBOLException, FileNotFoundException, SBOLValidationException, IOException, SBOLConversionException 
	{
		if (sbolFilePaths.size() == 0) 
		{
			sbolFilesAreLoaded = false;
			String message = "No SBOL files are found in project.";
			String messageTitle = "File Not Found";
			throw new SBOLException(message, messageTitle);
		} 
		else 
		{
			SBOLDOC = new SBOLDocument();
			SBOLDOC.setDefaultURIprefix(defaultURIPrefix); 
			Iterator<String> sbolFileIterator = sbolFilePaths.iterator();
			do //Go through each sbol file path and create an SBOLDocument
			{
				String sbolFilePath = sbolFileIterator.next();
				
				File f = new File(sbolFilePath);
				String fileName = f.getName().replace(".sbol", "");
				SBOLReader.setURIPrefix(defaultURIPrefix + "/" + fileName);
				SBOLDocument sbolDoc = SBOLReader.read(new FileInputStream(sbolFilePath));
				if (sbolDoc != null) 
				{
					//store each sbol document to its corresponding sbol file path to fileDocMap to keep track of which sbol 
					//document belong to which sbol file.
					fileDocMap.put(sbolFilePath, sbolDoc); 
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
		}
	}
	
	public boolean sbolFilesAreLoaded() {
		return sbolFilesAreLoaded;
	}
	
	public ComponentDefinition resolveURI(URI uri) throws SBOLException {
		ComponentDefinition resolvedComp = null;
		resolvedComp = SBOLDOC.getComponentDefinition(uri);
		if (resolvedComp == null){
			String message = "DNA component with URI " + uri.toString() +
					" could not be found in project SBOL files.";
			String messageTitle = "DNA Component Not Found";
			throw new SBOLException(message, messageTitle);
		}
		return resolvedComp;
	}
	
	public List<ComponentDefinition> resolveURIs(List<URI> uris) throws SBOLException {
		boolean error = false;
		List<ComponentDefinition> resolvedComps = new LinkedList<ComponentDefinition>();
		for (URI uri : uris) {
			ComponentDefinition resolvedComp = null;
			resolvedComp = SBOLDOC.getComponentDefinition(uri);
			if (resolvedComp == null) {
				error = true;
				resolvedComps.clear();
				String message = "DNA component with URI " + uri.toString() +
						" could not be found in project SBOL files.";
				String messageTitle = "DNA Component Not Found";
				throw new SBOLException(message, messageTitle);
			} 
			else if (!error)
				resolvedComps.add(resolvedComp);
		}
		return resolvedComps;
	}
	
	public ComponentDefinition resolveAndLocateTopLevelURI(URI uri) throws SBOLException {
		for (String sbolFilePath : fileDocMap.keySet()) {
			ComponentDefinition resolvedComp = fileDocMap.get(sbolFilePath).getComponentDefinition(uri);
			if (resolvedComp != null) {
				locatedFilePath = sbolFilePath;
				return resolvedComp;
			}
		}
		String message = "DNA component with URI " + uri.toString() +
				" could not be found in project SBOL files.";
		String messageTitle = "DNA Component Not Found";
		throw new SBOLException(message, messageTitle);

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
	
	public void saveDNAComponent(ComponentDefinition dnaComp, SBOLIdentityManager identityManager, SBOLDocument tempSbolDoc) throws SBOLValidationException, FileNotFoundException, SBOLConversionException {
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
			if (sbolFilePath.equals(targetFilePath))
			{
				for(Sequence s : tempSbolDoc.getSequences())
					SBOLUtility.addSequence(s, sbolDoc, false);
				for(ComponentDefinition c : tempSbolDoc.getComponentDefinitions())
					SBOLUtility.addDNAComponent(c, sbolDoc, false);
			}
			SBOLUtility.writeSBOLDocument(sbolFilePath, sbolDoc);
			System.out.println("Wrote sbolAnnot to this file: " + sbolFilePath);
		}
	}
	
	public static void saveDNAComponents(List<ComponentDefinition> dnaComps, String filePath) throws SBOLValidationException, FileNotFoundException, SBOLConversionException {
		SBOLDocument sbolDoc = new SBOLDocument();
		for (ComponentDefinition dnaComp : dnaComps)
			SBOLUtility.addDNAComponent(dnaComp, sbolDoc, false);
		SBOLUtility.writeSBOLDocument(filePath, sbolDoc);
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
