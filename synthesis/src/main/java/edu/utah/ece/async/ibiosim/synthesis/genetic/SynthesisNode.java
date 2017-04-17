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
package edu.utah.ece.async.ibiosim.synthesis.genetic;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.jsbml.SBase;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SequenceOntology;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.annotation.AnnotationUtility;
import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.SBOLException;
import edu.utah.ece.async.ibiosim.synthesis.sbol.util.SBOLFileManager;
import edu.utah.ece.async.ibiosim.synthesis.sbol.util.SBOLUtility;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SynthesisNode {
	private String id;
	private String type;
	private int nucleotideCount;
	private String signal;
	private List<URI> compURIs;
	private String coverConstraint;
	private List<SynthesisGraph> matches;
	private List<Integer> matchBounds;
	private int coverIndex;
	private List<SynthesisNode> uncoveredNodes;
	private int uncoveredBound;
	
	public SynthesisNode(String type, SBase sbmlElement, SBOLFileManager fileManager) throws SBOLException {
		id = SBMLutilities.getId(sbmlElement);
		this.type = type;
		compURIs = new LinkedList<URI>();
		processDNAComponents(sbmlElement, fileManager);
		coverIndex = -1;
		uncoveredBound = 0; 
	}
	
	public SynthesisNode(String type) {
		this.type = type;
		compURIs = new LinkedList<URI>();
		nucleotideCount = 0;
		coverIndex = -1;
		uncoveredBound = 0;
	}
	
	private void processDNAComponents(SBase sbmlElement, SBOLFileManager fileManager) throws SBOLException {
		//NOTE: Get all DnaComponent
		AnnotationUtility.parseSBOLAnnotation(sbmlElement, compURIs);
		List<ComponentDefinition> dnaComps = fileManager.resolveURIs(compURIs);
		nucleotideCount = SBOLUtility.countNucleotides(dnaComps);
		Set<URI> soFilterTypes = new HashSet<URI>();
		soFilterTypes.add(SequenceOntology.CDS);
		soFilterTypes.add(SequenceOntology.PROMOTER);
		//NOTE: get dnaComps with the specified SO types of CDS and PROMOTER.
		List<ComponentDefinition> signalComps = SBOLUtility.filterDNAComponents(dnaComps, soFilterTypes);
		//TODO: Why only get the first DnaComponent signal? Assume that signalComps always return 1 or 0?
		if (signalComps.size() > 0)
			signal = signalComps.get(0).getIdentity().toString(); //TODO: signal will store the uri of the 1st DnaComponent?
		else 
			signal = "";
	}
	
//	public void setID(String id) {
//		this.id = id;
//	}
	
	public String getID() {
		if (id == null)
			id = "";
		return id;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public int getNucleotideCount() {
		return nucleotideCount;
	}
	
	public void setMatchBounds(List<Integer> matchBounds) {
		this.matchBounds = matchBounds;
	}
	
	public List<Integer> getMatchBounds() {
		if (matchBounds == null)
			matchBounds = new LinkedList<Integer>();
		return matchBounds;
	}
	
	public String getSignal() {
		if (signal == null)
			signal = "";
		return signal;
	}
	
	public List<URI> getCompURIs() {
		return compURIs;
	}
	
	public void setUncoveredNodes(List<SynthesisNode> uncoveredNodes) {
		this.uncoveredNodes = uncoveredNodes;
	}
	
	public List<SynthesisNode> getUncoveredNodes() {
		if (uncoveredNodes == null)
			uncoveredNodes = new LinkedList<SynthesisNode>();
		return uncoveredNodes;
	}
	
	public void setUncoveredBound(int uncoveredBound) {
		this.uncoveredBound = uncoveredBound;
	}
	
	public int getUncoveredBound() {
		return uncoveredBound;
	}
	
	public void setCoverConstraint(String coverConstraint) {
		this.coverConstraint = coverConstraint;
	}
	
	public String getCoverConstraint() {
		if (coverConstraint == null)
			coverConstraint = "";
		return coverConstraint;
	}
	
	public void setMatches(List<SynthesisGraph> matches) {
		this.matches = matches;
	}
	
	public List<SynthesisGraph> getMatches() {
		if (matches == null)
			matches = new LinkedList<SynthesisGraph>();
		return matches;
	}
	
	public int getCoverIndex() {
		return coverIndex;
	}
	
	public SynthesisGraph getCover(int coverIndex) {
		if (coverIndex >= 0 && matches != null && coverIndex < matches.size())
			return matches.get(coverIndex);
		return null;
	}
	
	public int getCoverBound(int coverIndex) {
		if (coverIndex >= 0 && matchBounds != null && coverIndex < matchBounds.size())
			return matchBounds.get(coverIndex);
		return 0;
	}
	
	public SynthesisGraph getCover() {
		if (matches != null && matches.size() > 0) {
			if (coverIndex < 0)
				return matches.get(0);
			else if (coverIndex < matches.size())
				return matches.get(coverIndex);
			else
				return null;
		}
		return null;
	}
	
	public int getCoverBound() {
		if (matchBounds != null && matchBounds.size() > 0) {
			if (coverIndex < 0)
				return matchBounds.get(0);
			else if (coverIndex < matchBounds.size())
				return matchBounds.get(coverIndex);
			else
				return 0;
		}
		return 0;
	}
	
	public void terminateBranch() {
		coverIndex = -1;
	}
	
	public boolean branch() {
		if (matches != null) {
			coverIndex++;
			if (coverIndex < matches.size()) {
				return true;
			}
			coverIndex = -1;
			return false;
		}
		return false;
	}
	
	public void sortMatches() {
		if (matches != null && matchBounds != null && matches.size() == matchBounds.size())
			quickSortMatches(0, matches.size() - 1);
	}
	
	private void quickSortMatches(int left, int right) {
		if (left < right) {
			int pivot = left + (right - left)/2;
			pivot = partitionMatches(left, right, pivot);
			quickSortMatches(left, pivot - 1);
			quickSortMatches(pivot + 1, right);
		}
	}
	
	private int partitionMatches(int left, int right, int pivot) {
		int pivotValue = matchBounds.get(pivot);
		swapMatches(pivot, right);
		int store = left;
		for (int i = left; i < right; i++)
			if (matchBounds.get(i) < pivotValue) {
				swapMatches(store, i);
				store++;
			}
		swapMatches(store, right);
		return store;
	}
	
	private void swapMatches(int first, int second) {
		if (first != second) {
			matches.add(second - 1, matches.remove(first));
			matches.add(first, matches.remove(second));
			matchBounds.add(second - 1, matchBounds.remove(first));
			matchBounds.add(first, matchBounds.remove(second));
		}
	}
	
	@Override
	public String toString() {
		return id;
	}
}
