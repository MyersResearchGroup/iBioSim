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
package dataModels.biomodel.annotation;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import dataModels.util.GlobalConstants;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBOLAnnotation {
	
	private AnnotationElement modelToSbol;
	
	public SBOLAnnotation(String sbmlMetaID, URI dnaCompIdentity) {
		this(sbmlMetaID, dnaCompIdentity, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
	}
	
	public SBOLAnnotation(String sbmlMetaID, URI dnaCompIdentity, String strand) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		List<URI> identityHolder = new LinkedList<URI>();
		identityHolder.add(dnaCompIdentity);
		description.addChild(buildDNAComponentsDescription(description, identityHolder));
		if (strand.length() > 0)
			description.addChild(buildStrandDescription(description, strand));
	}
	
	public SBOLAnnotation(String sbmlMetaID, List<URI> dnaCompIdentities) {
		this(sbmlMetaID, dnaCompIdentities, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
	}
	
	public SBOLAnnotation(String sbmlMetaID, List<URI> dnaCompIdentities, String strand) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		if (dnaCompIdentities.size() > 0)
			description.addChild(buildDNAComponentsDescription(description, dnaCompIdentities));
		if (strand.length() > 0)
			description.addChild(buildStrandDescription(description, strand));
	}
	
	public SBOLAnnotation(String sbmlMetaID, String strand) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		if (strand.length() > 0)
			description.addChild(buildStrandDescription(description, strand));
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, URI sbolIdentity) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		List<URI> identityHolder = new LinkedList<URI>();
		identityHolder.add(sbolIdentity);
		description.addChild(buildSBOLElementsDescription(description, sbolClass, identityHolder));
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, List<URI> sbolElementIdentities) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		if (sbolElementIdentities.size() > 0)
			description.addChild(buildSBOLElementsDescription(description, sbolClass, sbolElementIdentities));
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, URI sbolElementIdentity, String strand) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		List<URI> identityHolder = new LinkedList<URI>();
		identityHolder.add(sbolElementIdentity);
		description.addChild(buildSBOLElementsDescription(description, sbolClass, identityHolder));
		if (strand.length() > 0)
			description.addChild(buildStrandDescription(description, strand));
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, List<URI> sbolElementIdentities, String strand) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		if (sbolElementIdentities.size() > 0)
			description.addChild(buildSBOLElementsDescription(description, sbolClass, sbolElementIdentities));
		if (strand.length() > 0)
			description.addChild(buildStrandDescription(description, strand));
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, URI sbolElementIdentity, URI dnaCompIdentity) {
		this(sbmlMetaID, sbolClass, sbolElementIdentity, dnaCompIdentity, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, URI sbolElementIdentity, URI dnaCompIdentity, String strand) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		List<URI> identityHolder = new LinkedList<URI>();
		identityHolder.add(sbolElementIdentity);
		description.addChild(buildSBOLElementsDescription(description, sbolClass, identityHolder));
		identityHolder.clear();
		identityHolder.add(dnaCompIdentity);
		description.addChild(buildDNAComponentsDescription(description, identityHolder));
		if (strand.length() > 0)
			description.addChild(buildStrandDescription(description, strand));
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, List<URI> sbolElementIdentities, List<URI> dnaCompIdentities) {
		this(sbmlMetaID, sbolClass, sbolElementIdentities, dnaCompIdentities, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
	}
	
	public SBOLAnnotation(String sbmlMetaID, String sbolClass, List<URI> sbolElementIdentities, List<URI> dnaCompIdentities, String strand) {
		AnnotationElement description = buildDescription(sbmlMetaID);
		if (sbolElementIdentities.size() > 0)
			description.addChild(buildSBOLElementsDescription(description, sbolClass, sbolElementIdentities));
		if (dnaCompIdentities.size() > 0)
			description.addChild(buildDNAComponentsDescription(description, dnaCompIdentities));
		if (strand.length() > 0)
			description.addChild(buildStrandDescription(description, strand));
	}
	
	private AnnotationElement buildDescription(String sbmlMetaId) {
		modelToSbol = new AnnotationElement("ModelToSBOL");
		modelToSbol.addNamespace(new AnnotationNamespace("http://sbolstandard.org/modeltosbol/1.0#"));
		
		AnnotationElement rdf = new AnnotationElement("rdf", "RDF");
		rdf.addNamespace(new AnnotationNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		rdf.addNamespace(new AnnotationNamespace("mts", "http://sbolstandard.org/modeltosbol/1.0#"));
		modelToSbol.addChild(rdf);
		
		AnnotationElement description = new AnnotationElement("rdf", "Description");
		description.addAttribute(new AnnotationAttribute("rdf", "about", "#" + sbmlMetaId));
		rdf.addChild(description);
		return description;
	}
	
	private AnnotationElement buildDNAComponentsDescription(AnnotationElement description, List<URI> sbolCompIdentities) {
		AnnotationElement dnaComponentsDescription = new AnnotationElement("mts", "DNAComponents");
		
		AnnotationElement seq = new AnnotationElement("rdf", "Seq");
		dnaComponentsDescription.addChild(seq);
		for (URI compURI : sbolCompIdentities) {
			AnnotationElement li = new AnnotationElement("rdf", "li");
			li.addAttribute(new AnnotationAttribute("rdf", "resource", compURI.toString()));
			seq.addChild(li);
		}	
		
		return dnaComponentsDescription;
	}
	
	private AnnotationElement buildStrandDescription(AnnotationElement description, String sbolStrand) {
		return new AnnotationElement("mts", "Strand", sbolStrand);
	}
	
	private AnnotationElement buildSBOLElementsDescription(AnnotationElement description, String sbolClass, List<URI> sbolIdentities) {
		AnnotationElement sbolElementsDescription = new AnnotationElement("mts", sbolClass + "s");
	
		AnnotationElement bag = new AnnotationElement("rdf", "Bag");
		sbolElementsDescription.addChild(bag);
		for (URI sbolURI : sbolIdentities) {
			AnnotationElement li = new AnnotationElement("rdf", "li");
			li.addAttribute(new AnnotationAttribute("rdf", "resource", sbolURI.toString()));
			bag.addChild(li);
		}
		
		return sbolElementsDescription;
	}
	
	public void createSBOLElementsDescription(String sbolClass, URI sbolIdentity) {
		List<URI> identityHolder = new LinkedList<URI>();
		identityHolder.add(sbolIdentity);
		createSBOLElementsDescription(sbolClass, identityHolder);
	}
	
	public void createSBOLElementsDescription(String sbolClass, List<URI> sbolIdentities) {
		AnnotationElement description = modelToSbol.getChild(0).getChild(0);
		description.addChild(0, buildSBOLElementsDescription(description, sbolClass, sbolIdentities));
	}
	
	public String toXMLString() {
		return modelToSbol.toXMLString();
	}
}
