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
package edu.utah.ece.async.ibiosim.synthesis.sbol.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.sbml.jsbml.Model;
import org.sbolstandard.core2.ComponentDefinition;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.SBOLAnnotation;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.parser.BioModel;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;

/**
 * 
 *
 * @author Nicholas Roehner
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLIdentityManager {

	private BioModel biomodel;
	private List<URI> modelURIs;
	private String modelStrand;
	private int indexOfBioSimURI = -1;
	private ComponentDefinition bioSimComp;
	private String saveFilePath;
	private String uriAuthority;

	public SBOLIdentityManager(BioModel biomodel, String uriAuthority) {
		this.biomodel = biomodel;
		this.uriAuthority = uriAuthority;
		modelURIs = new LinkedList<URI>();
		modelStrand = AnnotationUtility.parseSBOLAnnotation(biomodel.getSBMLDocument().getModel(), modelURIs);
		if (modelURIs.size() == 0) 
		{
			try 
			{
				modelURIs.add(new URI(uriAuthority + "#iBioSimPlaceHolder"));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			indexOfBioSimURI = 0;
		} 
		else 
		{
			int indexOfModelURI = -1;
			Iterator<URI> iterate = modelURIs.iterator();
			while (iterate.hasNext() && indexOfBioSimURI < 0) {
				indexOfModelURI++;
				URI modelURI = iterate.next();
				if (modelURI.toString().contains("_iBioSim") || modelURI.toString().endsWith("iBioSimPlaceHolder"))
					indexOfBioSimURI = indexOfModelURI;
			} 
		}
	}
	
	public BioModel getBioModel() {
		return biomodel;
	}
	
////	public List<URI> getModelURIs() {
////		return modelURIs;
////	}
////	
////	public List<DnaComponent> getModelComponents() {
////		return modelComps;
////	}
	
	public URI getBioSimURI() {
		return modelURIs.get(indexOfBioSimURI);
	}
	
	public ComponentDefinition getBioSimComponent() {
		return bioSimComp;
	}
	
	public String getSaveFilePath() {
		return saveFilePath;
	}
	
	public boolean containsBioSimURI() {
		return (indexOfBioSimURI >= 0 && 
				(modelURIs.get(indexOfBioSimURI).toString().contains("_iBioSim") || 
						modelURIs.get(indexOfBioSimURI).toString().endsWith("iBioSimPlaceHolder")));
	}
	
	public boolean containsPlaceHolderURI() {
		return (indexOfBioSimURI >= 0 && 
				modelURIs.get(indexOfBioSimURI).toString().endsWith("iBioSimPlaceHolder"));
	}
	
////	public boolean containsModelURIs() {
////		if (modelURIs.size() > 0 && !(modelURIs.size() == 1 && containsPlaceHolderURI()))
////			return true;
////		JOptionPane.showMessageDialog(Gui.frame, "There is no SBOL associated with the model itself.\n" +
////				"To associate SBOL with the model itself, you must associate SBOL\n" + 
////				"with its elements and save or associate SBOL directly via the model panel.",
////				"No SBOL Associated with Model", JOptionPane.ERROR_MESSAGE);
////		return false;
////	}
	
	public void removeBioSimURI() {
		modelURIs.remove(indexOfBioSimURI);
	}
	
	public void replaceBioSimURI (URI replacementURI) {
		modelURIs.remove(indexOfBioSimURI);
		modelURIs.add(indexOfBioSimURI, replacementURI);
	}
	
////	public void insertPlaceHolderURI() {
////		try {
////			modelURIs.add(indexOfBioSimURI, new URI("http://www.async.ece.utah.edu#iBioSimPlaceHolder"));
////		} catch (URISyntaxException e) {
////			e.printStackTrace();
////		}
////	}
	
	public boolean loadBioSimComponent(SBOLFileManager fileManager) throws SBOLException {
		bioSimComp = fileManager.resolveURI(modelURIs.get(indexOfBioSimURI));
		if (bioSimComp == null) {
			String[] options = new String[]{"Ok", "Cancel"};
			int choice = JOptionPane.showOptionDialog(null, 
					"Previously synthesized composite DNA component and its descriptors could not be loaded." +
							"  Would you like to overwrite?", "Warning", JOptionPane.DEFAULT_OPTION, 
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			return (choice == 0);
		}
		return true;
	}
	
	public boolean loadAndLocateBioSimComponent(SBOLFileManager fileManager) throws SBOLException {
		bioSimComp = fileManager.resolveAndLocateTopLevelURI(modelURIs.get(indexOfBioSimURI));
		if (bioSimComp == null) {
			String[] options = new String[]{"Ok", "Cancel"};
			int choice = JOptionPane.showOptionDialog(null, 
					"Previously synthesized composite DNA component and its descriptors could not be loaded." +
							"  Would you like to overwrite?", "Warning", JOptionPane.DEFAULT_OPTION, 
							JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			return (choice == 0);
		}
		saveFilePath = fileManager.getLocatedFilePath();
		return true;
	}
	
//	public boolean loadModelComponents(SBOLFileManager fileManager) {
//		List<URI> exportURIs = new LinkedList<URI>(modelURIs);
//		if (containsPlaceHolderURI())
//			exportURIs.remove(indexOfBioSimURI);
//		if (exportURIs.size() > 0) {
//			modelComps = fileManager.resolveURIs(exportURIs);
//			return modelComps.size() > 0;
//		}
//		return false;
//	}
	
	// Loads SBOL descriptors such as display ID, name, and description for newly synthesized iBioSim composite component 
	// from model or previously synthesized component
	// Also determines target file for saving newly synthesized component and checks for match with previously synthesized 
	// component (latter affects save of new component and the construction of its URIs)
	public String[] describeDNAComponent(ComponentDefinition dnaComp) 
	{
//		ComponentDefinition described_dnaComp = null;
		String[] described_dnaComp = new String[4]; 
		String[] descriptors = biomodel.getSBOLDescriptors();
		
		if (descriptors != null) 
		{
			described_dnaComp[0] = descriptors[0]; //displayId
			described_dnaComp[1] = descriptors[1]; //name
			described_dnaComp[2] = descriptors[2]; //description
		} 
		else if (bioSimComp != null) 
		{
			described_dnaComp[0] = bioSimComp.getDisplayId(); //displayId
			if (bioSimComp.getName() != null)
				described_dnaComp[1] = bioSimComp.getName(); //name
			if (bioSimComp.getDescription() != null)
				described_dnaComp[2] = bioSimComp.getDescription(); //description
			described_dnaComp[3] = modelURIs.get(indexOfBioSimURI).toString(); //identity
		} 
		else 
		{
			described_dnaComp[0] = biomodel.getSBMLDocument().getModel().getId(); //displayId
			
		}
		
		return described_dnaComp;
	}
	
	
//	// Constructs URIs for newly synthesized component, its DNA sequence, and sequence annotations
//	// Also replaces URI of previously synthesized component or placeholder URI among component URIs previously annotating model
//	public void identifyDNAComponent(ComponentDefinition dnaComp) 
//	{
//		boolean identical = false;
//		if (bioSimComp != null) {
//			Set<SequenceAnnotation> annos = dnaComp.getSequenceAnnotations();
//			Set<SequenceAnnotation> synthAnnos = bioSimComp.getSequenceAnnotations();
//			if (annos.size() == synthAnnos.size()) 
//			{
//				//TODO: Can't set identity for SequenceAnnot. and CompDef and Seq.
////				for (int i = 0; i < annos.size(); i++)
////				{
////					annos.get(i).setURI(synthAnnos.get(i).getURI());
////				}
////				dnaComp.setURI(modelURIs.get(indexOfBioSimURI));
////				if (bioSimComp.getDnaSequence() != null)
////					dnaComp.getDnaSequence().setURI(bioSimComp.getDnaSequence().getURI());
//				
////				identical = SBOLDeepEquality.isDeepEqual(dnaComp, bioSimComp);
//				identical = dnaComp.equals(bioSimComp);
//			} 
//		}
////		if (!identical) {
////			setTime(); 
////			// URI authority and time are set for creating new URIs
////			try {
////				dnaComp.setURI(new URI(uriAuthority + "#comp" + time + "_iBioSim"));
////			} catch (URISyntaxException e) {
////				e.printStackTrace();
////			}
////			try {
////				dnaComp.getDnaSequence().setURI(new URI(uriAuthority + "#seq" + time + "_iBioSim"));
////			} catch (URISyntaxException e) {
////				e.printStackTrace();
////			}
//////			List<SequenceAnnotation> synthAnnos = dnaComp.getAnnotations();
////			Set<SequenceAnnotation> synthAnnos = dnaComp.getSequenceAnnotations();
////			for (int i = 0; i < synthAnnos.size(); i++) {
////				try {
////					synthAnnos.get(i).setURI(new URI(uriAuthority + "#anno" + i + time + "_iBioSim"));
////				} catch (URISyntaxException e) {
////					e.printStackTrace();
////				}
////			}
////			
////		}
//	}
	
	
	public void annotateBioModel() {
		Model sbmlModel = biomodel.getSBMLDocument().getModel();
		if (modelURIs.size() > 0) {
			if (!sbmlModel.isSetMetaId() || sbmlModel.getMetaId().equals(""))
				SBMLutilities.setDefaultMetaID(biomodel.getSBMLDocument(), sbmlModel, 
						biomodel.getMetaIDIndex());
			SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlModel.getMetaId(), modelURIs, modelStrand);
			AnnotationUtility.setSBOLAnnotation(sbmlModel, sbolAnnot);
		} else
			AnnotationUtility.removeSBOLAnnotation(sbmlModel);
	}

	
//	private void setTime() {
//		Calendar now = Calendar.getInstance();
//		time = "_" + now.get(Calendar.MONTH) + "_" 
//				+ now.get(Calendar.DATE) + "_" + now.get(Calendar.YEAR) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" 
//				+ now.get(Calendar.MINUTE) + "_" + now.get(Calendar.SECOND) + "_" + now.get(Calendar.MILLISECOND);
//	}
}
