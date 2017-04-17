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
package edu.utah.ece.async.biosim.synthesis.techMap;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.FunctionalComponent;
import org.sbolstandard.core2.ModuleDefinition;

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
	private List<SynthesisNode> children; 
	private FunctionalComponent functionalComponent; //Represent each vertex node
	private ComponentDefinition componentDefinition; //Use to store the type for each vertex node
	private ModuleDefinition moduleDefinition; //Represent each gate
	private URI compDefType; 
	private double score; 
	private List<SynthesisNode> parents;
	private Map<SynthesisNode, URI> relations; //TODO: MIGHT NOT NEED
	private int degree; 
	private boolean visited; 
	
	public SynthesisNode(ModuleDefinition md, FunctionalComponent fc, int uniqueId)
	{
		this.children = new ArrayList<SynthesisNode>();
		this.parents = new ArrayList<SynthesisNode>();
		this.functionalComponent = fc;
		this.componentDefinition = fc.getDefinition(); 
		this.moduleDefinition = md;
		this.compDefType = fc.getDefinition().getTypes().iterator().next();
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
	
	public ModuleDefinition getModuleDefinition()
	{
		return this.moduleDefinition;
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
		return moduleDefinition.getDisplayId() + "_" + functionalComponent.getDisplayId();
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
