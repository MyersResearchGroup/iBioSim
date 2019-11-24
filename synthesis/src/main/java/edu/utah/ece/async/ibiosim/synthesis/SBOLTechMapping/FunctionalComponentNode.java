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
package edu.utah.ece.async.ibiosim.synthesis.SBOLTechMapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.Sequence;

/**
 * 
 *
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class FunctionalComponentNode
{
	private SBOLDocument sbolDoc; //SBOLDocument where the SBOL objects are referred to
	private Set<Sequence> sequences;
	private FunctionalComponent functionalComponent; //Represent each vertex node

	Map<ModuleDefinition, Context> moduleDefinitionContextMap;

	public FunctionalComponentNode(SBOLDocument sbolDoc, FunctionalComponent fc) {
		this.sequences = fc.getDefinition().getSequences();
		this.sbolDoc = sbolDoc;
		this.functionalComponent = fc;
		this.moduleDefinitionContextMap = new HashMap<>();

	}
	
	public void addContext(ModuleDefinition md) {
		moduleDefinitionContextMap.put(md, new Context());
	}

	public void addChild(ModuleDefinition md, FunctionalComponentNode node) {
		moduleDefinitionContextMap.get(md).children.add(node);
	}

	public void addParent(ModuleDefinition md, FunctionalComponentNode node) {
		moduleDefinitionContextMap.get(md).parents.add(node);
	}

	public void addRelationship(ModuleDefinition md, FunctionalComponentNode node, URI relation) {
		moduleDefinitionContextMap.get(md).relations.put(node, relation);
	}

	public URI getRelationship(ModuleDefinition md, FunctionalComponentNode node) {
		return moduleDefinitionContextMap.get(md).relations.get(node);
	}

	public List<FunctionalComponentNode> getChildren(ModuleDefinition md) {
		return moduleDefinitionContextMap.get(md).children;
	}


	public List<FunctionalComponentNode> getParents(ModuleDefinition md) {
		return moduleDefinitionContextMap.get(md).parents;
	}

	public URI getCompDefType() throws SBOLTechMapException {
		ComponentDefinition cd = this.functionalComponent.getDefinition();
		Set<URI> cdTypes = cd.getTypes();

		if(cdTypes.isEmpty()) {
			throw new SBOLTechMapException("There are no type attached to this ComponentDefinition " + cd.getIdentity());
		}
		else if(cdTypes.size() > 1) {
			//log that there are more than 1 cd types.
		}

		return cdTypes.iterator().next();
	}

	public void setDegree(ModuleDefinition md, int degree) {
		this.moduleDefinitionContextMap.get(md).degree = degree;
	}

	public ComponentDefinition getComponentDefinition() {
		return this.functionalComponent.getDefinition();
	}

	public Set<Sequence> getSequences(){
		return this.sequences;
	}

	public String getFlattenedSequence() {
		String completeSeq = "";
		for(Sequence s: sequences)
		{
			completeSeq = completeSeq + s.getElements();
		}
		return completeSeq;
	}

	public SBOLDocument getSBOLDocument() {
		return this.sbolDoc;
	}

	public FunctionalComponent getFunctionalComponent() {
		return this.functionalComponent;
	}

	public int getDegree(ModuleDefinition md) {
		return this.moduleDefinitionContextMap.get(md).degree; 
	}

	public double getScore(ModuleDefinition md) {
		return this.moduleDefinitionContextMap.get(md).score;
	}

	public void setScore(ModuleDefinition md, double value) {
		this.moduleDefinitionContextMap.get(md).score = value; 
	}

	@Override
	public String toString() {
		return this.functionalComponent.getDisplayId(); 
	}

	public boolean isLeaf(ModuleDefinition md) {
		return this.moduleDefinitionContextMap.get(md).children.size() == 0;
	}

	public boolean isRoot(ModuleDefinition md) {
		return this.moduleDefinitionContextMap.get(md).parents.size() == 0;
	}

	class Context {

		private List<FunctionalComponentNode> parents;
		private List<FunctionalComponentNode> children; 
		private Map<FunctionalComponentNode, URI> relations; 
		private double score; 
		private int degree; 

		public Context() {
			this.children = new ArrayList<FunctionalComponentNode>();
			this.parents = new ArrayList<FunctionalComponentNode>();
			this.relations = new HashMap<FunctionalComponentNode, URI>();
			this.degree = 0; 
		}
	}
}
