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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.sbolstandard.core2.*;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;

/**
 * This class is reserved for managing SBOL data.
 * 
 * @author Tramy Nguyen
 * @author Nicholas Roehner
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLUtility 
{
	private static SBOLDocument SBOLDOC; 

	public static SBOLDocument loadSBOLFiles(HashSet<String> sbolFilePaths, String defaultURIPrefix) {
		SBOLDocument sbolDoc = new SBOLDocument();
		Preferences biosimrc = Preferences.userRoot();

		for (String filePath : sbolFilePaths) 
		{
			File f = new File(filePath);
			String fileName = f.getName().replace(".sbol", "");
			sbolDoc.setDefaultURIprefix(defaultURIPrefix + "/" + fileName);
			SBOLReader.setDropObjectsWithDuplicateURIs(true);
			SBOLReader.setURIPrefix(defaultURIPrefix + "/" + fileName);
			try
			{
				String sbolRDF = filePath.replace(".sbol", ".rdf");
				SBOLReader.setKeepGoing(false);
				SBOLDocument newSbolDoc = SBOLReader.read(sbolRDF);
				for(ComponentDefinition c : newSbolDoc.getComponentDefinitions())
				{
					sbolDoc.createCopy(c);
				}
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

		}
		return sbolDoc;
	}


	/**
	 * Return the SBOLDocument parsed by the given file
	 * @param filePath
	 * @return
	 * @throws SBOLConversionException 
	 * @throws IOException 
	 * @throws SBOLValidationException 
	 * @throws FileNotFoundException 
	 */
	public static SBOLDocument loadSBOLFile(String filePath, String defaultURIPrefix) throws FileNotFoundException, SBOLValidationException, IOException, SBOLConversionException 
	{
		SBOLDocument sbolDoc = null; 

		File f = new File(filePath);
		String fileName = f.getName().replace(".sbol", "");
		Preferences biosimrc = Preferences.userRoot();
		SBOLReader.setURIPrefix(defaultURIPrefix + "/" + fileName);
		sbolDoc = SBOLReader.read(new FileInputStream(filePath));

		return sbolDoc; 
	}

	public static void writeSBOLDocument(String filePath, SBOLDocument sbolDoc) throws FileNotFoundException, SBOLConversionException 
	{
		SBOLWriter.write(sbolDoc, new FileOutputStream(filePath));
		
	}
	
	/**
	 * Copy all top level objects from the source SBOLDocument to the target SBOLDocument.
	 * @param sourceDoc - The SBOLDocument to copy the top level objects from
	 * @param targetDoc - The SBOLDocument to copy the top level objects into
	 * @throws SBOLValidationException - Unable to perform createCopy from libSBOLj
	 * @throws SBOLException - SBOL contents not equal when performing SBOL equals method was called.
	 */
	public static void copyAllTopLevels(SBOLDocument sourceDoc, SBOLDocument targetDoc) throws SBOLValidationException, SBOLException
	{
		for(TopLevel topLevObj : sourceDoc.getTopLevels())
		{
			if(topLevObj instanceof Sequence)
			{
				Sequence libSeq = targetDoc.getSequence(topLevObj.getIdentity());
				copySBOLObj(targetDoc, libSeq, topLevObj);
			}
			else if(topLevObj instanceof ComponentDefinition)
			{
				ComponentDefinition libCD = targetDoc.getComponentDefinition(topLevObj.getIdentity());
				copySBOLObj(targetDoc, libCD, topLevObj);
			}
			else if(topLevObj instanceof ModuleDefinition)
			{
				ModuleDefinition libMD = targetDoc.getModuleDefinition(topLevObj.getIdentity());
				copySBOLObj(targetDoc, libMD, topLevObj);
			}
			else if(topLevObj instanceof org.sbolstandard.core2.Model)
			{
				org.sbolstandard.core2.Model libModel = targetDoc.getModel(topLevObj.getIdentity());
				copySBOLObj(targetDoc, libModel, topLevObj);
			}
			else if(topLevObj instanceof GenericTopLevel)
			{
				GenericTopLevel libGTL = targetDoc.getGenericTopLevel(topLevObj.getIdentity());
				copySBOLObj(targetDoc, libGTL, topLevObj);
			}
			else if(topLevObj instanceof org.sbolstandard.core2.Collection)
			{
				org.sbolstandard.core2.Collection libColl = targetDoc.getCollection(topLevObj.getIdentity());
				copySBOLObj(targetDoc, libColl, topLevObj);
			}
		}
	}
	
	/**
	 * Copy the given SBOL object to the SBOL library document. If the given SBOL object already exist in the 
	 * SBOL library document but have differing content, then an error is thrown. If the given SBOL object doesn't
	 * exist within the SBOL library document, then it will be added.
	 * 
	 * @param targetSBOLDoc - The SBOL library document.
	 * @param targetTopLevelObj - The Top Level SBOL library object to compare to the TopLevel SBOL object to be added. 
	 * @param sourceTopLevObj - The given TopLevel SBOL object to be added to the SBOL library document.
	 * @throws SBOLValidationException - Unable to perform createCopy from libSBOLj
	 * @throws SBOLException - SBOL contents not equal when performing SBOL equals method was called.
	 */
	private static void copySBOLObj(SBOLDocument targetSBOLDoc, TopLevel targetTopLevelObj, TopLevel sourceTopLevObj) throws SBOLValidationException, SBOLException
	{
		if(targetTopLevelObj == null)
		{
			targetSBOLDoc.createCopy(sourceTopLevObj); 
		}
		else
		{
			//If the library SBOL obj isn't the same as the vpr SBOL object, report to the user 
			//that this model had SBOL objects with same SBOL id but diff. content. 
			if(!targetTopLevelObj.equals(sourceTopLevObj))
			{
				throw new SBOLException("The target SBOL file with this TopLevel Identity: " + targetTopLevelObj.getIdentity() +
						"does not match the content of the source SBOL TopLevel Identity: " + sourceTopLevObj.getIdentity(), 
						"SBOL Content Not Equal");
			}
		}
	}

	// Adds given DNA component and all its subcomponents to top level of SBOL Document if not already present
	// Subcomponents of the given component are replaced by components of matching URIs from the SBOL document to avoid having
	// multiple data structures of the same URI
	public static void addDNAComponent(ComponentDefinition dnac, SBOLDocument sbolDoc, boolean flatten) throws SBOLValidationException 
	{
		dnac = replaceDNAComponent(dnac, sbolDoc);
		Set<String> topURIs = new HashSet<String>();
		for (TopLevel sbolObj : sbolDoc.getTopLevels())
			if (sbolObj instanceof ComponentDefinition)
				topURIs.add(sbolObj.getIdentity().toString());

		if (flatten)
			flattenDNAComponent(dnac, sbolDoc, topURIs);
		else if (!topURIs.contains(dnac.getIdentity().toString()))
		{
			sbolDoc.createCopy(dnac); 
		}
	}

	public static void addSequence(Sequence seq, SBOLDocument sbolDoc, boolean flatten) throws SBOLValidationException 
	{
		Set<String> topURIs = new HashSet<String>();
		for (TopLevel sbolObj : sbolDoc.getTopLevels())
			if (sbolObj instanceof Sequence)
				topURIs.add(sbolObj.getIdentity().toString());

		if (!topURIs.contains(seq.getIdentity().toString()))
			sbolDoc.createCopy(seq); 
	}

	// Adds given DNA component and all its subcomponents to top level of SBOL Document if not already present
	private static void flattenDNAComponent(ComponentDefinition dnac, SBOLDocument sbolDoc, Set<String> topURIs) throws SBOLValidationException {
		if (!topURIs.contains(dnac.getIdentity())) {
			sbolDoc.createCopy(dnac); 
			topURIs.add(dnac.getIdentity().toString());
		}
		Set<SequenceAnnotation> annos = dnac.getSequenceAnnotations(); 
		if (annos != null && annos.size() > 0)
			flattenSubComponents(annos, sbolDoc, topURIs);
	}

	// Recursively adds annotation subcomponents to top level of SBOL document if not already present
	private static void flattenSubComponents(Set<SequenceAnnotation> annos, SBOLDocument sbolDoc, Set<String> topURIs) throws SBOLValidationException 
	{
		for (SequenceAnnotation anno : annos) {
			ComponentDefinition subDnac = anno.getComponent().getDefinition();
			if (subDnac != null) 
			{
				if (!topURIs.contains(subDnac.getIdentity())) {
					sbolDoc.createCopy(subDnac); 
					topURIs.add(subDnac.getIdentity().toString());
				}
				Set<SequenceAnnotation> subAnnos = subDnac.getSequenceAnnotations(); 
				if (subAnnos != null && subAnnos.size() > 0)
					flattenSubComponents(subAnnos, sbolDoc, topURIs);
			}
		}
	}

	// Replaces DNA component and its subcomponents with components from SBOL document if their URIs match
	// Used to avoid conflict of multiple data structures of same URI in a single SBOL document
	public static ComponentDefinition replaceDNAComponent(ComponentDefinition dnac, SBOLDocument sbolDoc) 
	{
		ComponentDefinition resolvedDnac = sbolDoc.getComponentDefinition(dnac.getIdentity());
		if (resolvedDnac != null)
			return resolvedDnac;
		Set<SequenceAnnotation> annos = dnac.getSequenceAnnotations(); 
		if (annos != null && annos.size() > 0)
		{
			replaceSubComponents(annos);
		}
		return dnac;
	}

	private static void replaceSubComponents(Set<SequenceAnnotation> annos) 
	{
		for (SequenceAnnotation anno : annos) 
		{
			ComponentDefinition subDnac = anno.getComponentDefinition();
			if (subDnac != null) 
			{
				ComponentDefinition resolvedSubDnac = SBOLDOC.getComponentDefinition(subDnac.getIdentity());
				if (resolvedSubDnac != null)
				{
					//					anno.setSubComponent(resolvedSubDnac);
				}
				else 
				{
					Set<SequenceAnnotation> subAnnos = subDnac.getSequenceAnnotations();
					if (subAnnos != null && subAnnos.size() > 0)
					{
						replaceSubComponents(subAnnos);
					}
				}
			}
		}
	}

	// Deletes all DNA components that have URIs matching the given URI and updates the annotations
	// and DNA sequences of composite components using the deleted components
	public static void deleteDNAComponent(URI deletingURI, SBOLDocument sbolDoc) throws SBOLValidationException {
		ComponentDefinition compDef = sbolDoc.getComponentDefinition(deletingURI);
		if (compDef!=null) {
			// TODO: should update any references to this compDef
			for (Sequence sequence : compDef.getSequences()) {
				sbolDoc.removeSequence(sequence);
			}
			sbolDoc.removeComponentDefinition(compDef);
		}

		List<ComponentDefinition> intersections = intersectDNAComponent(deletingURI, sbolDoc);
		for (ComponentDefinition intersectedDnac : intersections) {
			if (intersectedDnac.getIdentity().toString().equals(deletingURI.toString())) 
			{
				if(SBOLDOC.getComponentDefinition(intersectedDnac.getIdentity()) != null)
				{
					SBOLDOC.removeComponentDefinition(intersectedDnac);
					sbolDoc.removeComponentDefinition(intersectedDnac);
				}
			}
			else 
			{
				List<SequenceAnnotation> intersectedAnnos = new LinkedList<SequenceAnnotation>();
				for (SequenceAnnotation anno : intersectedDnac.getSequenceAnnotations()) {
					ComponentDefinition subDnac = anno.getComponent().getDefinition();
					if (subDnac != null && subDnac.getIdentity().toString().equals(deletingURI.toString())) 
						intersectedAnnos.add(anno);
				}
				for (SequenceAnnotation intersectedAnno : intersectedAnnos)
				{
					intersectedDnac.removeSequenceAnnotation(intersectedAnno);
				}
				//Note: Assume each compDef has 1 Sequence
				Sequence intersectedSeq = intersectedDnac.getSequences().iterator().next();
				String nucleotides = "";
				int position = 0;
				for (SequenceAnnotation anno : intersectedDnac.getSequenceAnnotations()) {
					String subNucleotides = anno.getComponent().getDefinition().getSequences().iterator().next().getElements();
					nucleotides = nucleotides + subNucleotides;
					int start = position + 1;
					position = position + subNucleotides.length();
					int end = position; 
					anno.addRange(intersectedSeq.getDisplayId()+"_range", start, end);
				}
				intersectedSeq.setElements(nucleotides);
			}
		}
	}


	// Returns all DNA components from SBOL document that have URIs matching the given URI
	// or that have subcomponents matching the given URI
	public static List<ComponentDefinition> intersectDNAComponent(URI intersectingURI, SBOLDocument sbolDoc) {
		
		List<ComponentDefinition> intersections = new LinkedList<ComponentDefinition>();
		for (TopLevel sbolObj : sbolDoc.getTopLevels()) // note getContents() only returns top-level SBOL objects
			if (sbolObj instanceof ComponentDefinition) 
				if (sbolObj.getIdentity().toString().equals(intersectingURI.toString()) ) 
					intersections.add((ComponentDefinition) sbolObj);
				else {
					Set<SequenceAnnotation> annos = ((ComponentDefinition) sbolObj).getSequenceAnnotations();
					if (annos != null && annos.size() > 0)
						intersections.addAll(intersectWithSubComponents(intersectingURI, (ComponentDefinition) sbolObj, annos));
				}
		return intersections;
	}


	private static List<ComponentDefinition> intersectWithSubComponents(URI intersectingURI, ComponentDefinition intersectedDnac, Set<SequenceAnnotation> intersectedAnnos) {
		
		List<ComponentDefinition> subIntersections = new LinkedList<ComponentDefinition>();
		Iterator<SequenceAnnotation> annoIterator = intersectedAnnos.iterator();
		while (subIntersections.size() == 0 && annoIterator.hasNext()) {
			ComponentDefinition subDnac = annoIterator.next().getComponent().getDefinition();
			if (subDnac != null && subDnac.getIdentity().toString().equals(intersectingURI.toString()))
				subIntersections.add(intersectedDnac);
		}
		if (subIntersections.size() == 0) {
			for (SequenceAnnotation intersectedAnno : intersectedAnnos) {
				ComponentDefinition subDnac = intersectedAnno.getComponent().getDefinition();
				if (subDnac != null) {
					Set<SequenceAnnotation> subAnnos = subDnac.getSequenceAnnotations();
					if (subAnnos != null && subAnnos.size() > 0)
						subIntersections.addAll(intersectWithSubComponents(intersectingURI, subDnac, subAnnos));
				}
			}
			if (subIntersections.size() > 0)
				subIntersections.add(intersectedDnac);
		}
		return subIntersections;
	}


	public static List<URI> loadDNAComponentURIs(List<ComponentDefinition> dnaComps) {
		List<URI> uris = new LinkedList<URI>();
		for (ComponentDefinition lowestComp : loadLowestSubComponents(dnaComps))
			uris.add(lowestComp.getIdentity());
		return uris;
	}


	public static List<ComponentDefinition> loadLowestSubComponents(List<ComponentDefinition> dnaComps) {
		List<ComponentDefinition> subComps = new LinkedList<ComponentDefinition>();
		List<ComponentDefinition> tempComps = new LinkedList<ComponentDefinition>(dnaComps);
		
		while (tempComps.size() > 0) 
		{
			if (tempComps.get(0).getSequenceAnnotations().size() > 0)
				for (SequenceAnnotation anno : tempComps.remove(0).getSequenceAnnotations())
					tempComps.add(anno.getComponentDefinition());
			else
				subComps.add(tempComps.remove(0));
		}
		return subComps;
	}

	
	public static List<ComponentDefinition> filterDNAComponents(List<ComponentDefinition> dnaComps, Set<URI> soFilterNums) {
		List<ComponentDefinition> filteredComps = new LinkedList<ComponentDefinition>();
		for (ComponentDefinition dnaComp : dnaComps) {
			for (URI role : soFilterNums) {
				if (dnaComp.getRoles().contains(role)) {
					filteredComps.add(dnaComp);
					break;
				}
			}
		}
		return filteredComps;
	}

	public static int countNucleotides(List<ComponentDefinition> dnaComps) {
		int nucleotideCount = 0;
		for (ComponentDefinition dnaComp : dnaComps) {
			Sequence sequence = dnaComp.getSequenceByEncoding(Sequence.IUPAC_DNA);
			if (sequence != null)
				nucleotideCount += sequence.getElements().length();
		} 
		return nucleotideCount;
	}


	public static String convertRegexSOTermsToNumbers(String regex) 
	{
		regex = regex.replaceAll("promoter", "SO:0000167");
		regex = regex.replaceAll("ribosome entry site", "SO:0000139");
		regex = regex.replaceAll("coding sequence", "SO:0000316");
		regex = regex.replaceAll("terminator", "SO:0000141");
		return regex;
	}

	public static String loadSONumber(ComponentDefinition dnaComp) {
		for (URI uri : dnaComp.getRoles()) {
			String authority = uri.getAuthority();
			if (authority != null && authority.equals(GlobalConstants.SO_AUTHORITY2)) {
				String path = uri.getPath();
				if (path != null && path.length() > 0) {
					String[] splitPath = path.split("/");
					if (splitPath[splitPath.length - 1].startsWith("SO"))
						return splitPath[splitPath.length - 1];
				}
			}
		}
		return null;
	}

	public static Properties loadSBOLSynthesisProperties(String synthFilePath, String separator, JFrame frame) {
		Properties synthProps = new Properties();
		for (String synthFileID : new File(synthFilePath).list())
			if (synthFileID.endsWith(GlobalConstants.SBOL_SYNTH_PROPERTIES_EXTENSION)) {
				try {
					FileInputStream propStreamIn = new FileInputStream(new File(synthFilePath + separator + synthFileID));
					synthProps.load(propStreamIn);
					propStreamIn.close();
					return synthProps;
				} catch (IOException e) {
					JOptionPane.showMessageDialog(frame, "Unable to load properties file!", "Error Loading Properties", JOptionPane.ERROR_MESSAGE);
				}
			}	
		return synthProps;
	}

}
