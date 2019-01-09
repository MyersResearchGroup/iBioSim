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
public class SynthesisNode
{
	private SBOLDocument sbolDoc; //SBOLDocument where the SBOL objects are referred to
	private ModuleDefinition moduleDefinition; //Container where information about the gates are stored in
	
	private ComponentDefinition componentDefinition; 
	private FunctionalComponent functionalComponent; //Represent each vertex node
	private URI compDefType; 
	
	private Set<Sequence> sequences;
	private String flattenedSequence;
	
	private List<SynthesisNode> parents;
	private List<SynthesisNode> children; 
	private Map<SynthesisNode, URI> relations; 
	
	private double score; 
	private int degree; 
	private boolean visited; 
	
	public SynthesisNode(SBOLDocument sbolDoc, ModuleDefinition md, FunctionalComponent fc) 
	{
		this.children = new ArrayList<SynthesisNode>();
		this.parents = new ArrayList<SynthesisNode>();
		
		this.functionalComponent = fc;
		
		ComponentDefinition compDef = fc.getDefinition();
		
		this.componentDefinition = compDef;
		this.compDefType = compDef.getTypes().iterator().next();
		this.sequences = compDef.getSequences();
		String completeSeq = "";
		for(Sequence s: sequences)
		{
			completeSeq = completeSeq + s.getElements();
		}
		this.flattenedSequence = completeSeq;
		this.moduleDefinition = md;
		this.sbolDoc = sbolDoc;
		
		this.relations = new HashMap<SynthesisNode, URI>();
		this.degree = 0; 
		this.visited = false; 
	}
	
	public void addChild(SynthesisNode node)
	{
		children.add(node);
	}
	
	public void addParent(SynthesisNode node)
	{
		parents.add(node);
	}
	
	public void addRelationship(SynthesisNode node, URI relation)
	{
		relations.put(node, relation);
	}
	
	public URI getRelationship(SynthesisNode node)
	{
		return relations.get(node);
	}
	
	public List<SynthesisNode> getChildren()
	{
		return children;
	}
	
	public boolean getVisited()
	{
		return this.visited; 
	}
	
	public List<SynthesisNode> getParents()
	{
		return parents;
	}
	
	public URI getCompDefType()
	{
		return this.compDefType;
	}
	
	public void setDegree(int degree)
	{
		this.degree = degree;
	}
	
	public ComponentDefinition getComponentDefinition()
	{
		return this.componentDefinition;
	}
	
	public Set<Sequence> getSequences(){
		return this.sequences;
	}
	
	public String getFlattenedSequence()
	{
		return this.flattenedSequence;
	}
	
	public ModuleDefinition getModuleDefinition()
	{
		return this.moduleDefinition;
	}
	
	public SBOLDocument getSBOLDocument()
	{
		return this.sbolDoc;
	}
	
	public FunctionalComponent getFunctionalComponent()
	{
		return this.functionalComponent;
	}
	
	public int getDegree()
	{
		return degree; 
	}
	
	public double getScore()
	{
		return this.score;
	}
	
	public void setScore(double value)
	{
		this.score = value; 
	}
	
	@Override
	public String toString()
	{
		return moduleDefinition.getDisplayId() + "_" + this.functionalComponent.getDisplayId(); 
	}
	
	public boolean isLeaf()
	{
		return children.size() == 0;
	}
	
	public boolean isRoot()
	{
		return parents.size() == 0;
	}
}
