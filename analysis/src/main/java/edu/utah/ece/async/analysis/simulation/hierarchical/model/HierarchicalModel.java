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
package edu.utah.ece.async.analysis.simulation.hierarchical.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.Model;

import edu.utah.ece.async.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.analysis.simulation.hierarchical.math.ConstraintNode;
import edu.utah.ece.async.analysis.simulation.hierarchical.math.EventNode;
import edu.utah.ece.async.analysis.simulation.hierarchical.math.FunctionNode;
import edu.utah.ece.async.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.dataModels.util.GlobalConstants;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public final class HierarchicalModel
{

	public static enum ModelType
	{
		HSSA, HODE, HFBA, NONE;
	}

	private ModelType					type;

	private Set<String>					deletedBySId;
	private Set<String>					deletedByMetaId;

	private List<EventNode>				events;
	private List<ReactionNode>			reactions;
	private List<VariableNode>			arrays;
	private List<ConstraintNode>		constraints;
	private List<FunctionNode> initAssignments;
	private List<FunctionNode> assignRules;
	private List<VariableNode> variables;

	private Map<String, VariableNode>	idToNode;


	private String        ID;
	protected int       index;
	private double        maxPropensity;
	private double        minPropensity;
	private FunctionNode  propensity;

	private Map<String, HierarchicalModel>	idToSubmodel;
	
	
	public HierarchicalModel(String submodelID)
	{
		this(submodelID, 0);
	}

	public HierarchicalModel(String submodelID, int index)
	{

		this.ID = submodelID;
		this.index = index;
		this.minPropensity = Double.MAX_VALUE / 10.0;
		this.maxPropensity = Double.MIN_VALUE / 10.0;
		
		this.idToNode = new HashMap<String, VariableNode>();
		this.variables = new ArrayList<VariableNode>();
		this.events = new LinkedList<EventNode>();
		this.constraints = new ArrayList<ConstraintNode>();
		this.reactions = new ArrayList<ReactionNode>();
		this.arrays = new ArrayList<VariableNode>();
		this.propensity = new FunctionNode(new VariableNode("propensity", StateType.SCALAR), new HierarchicalNode(Type.PLUS));
	}

	public HierarchicalModel(HierarchicalModel state)
	{
		this.type = state.type;
		// TODO: fix this
		this.deletedBySId = state.deletedBySId;
		this.deletedByMetaId = state.deletedByMetaId;
		this.events = state.events;
		this.reactions = state.reactions;
		this.arrays = state.arrays;
		this.constraints = state.constraints;
		this.idToNode = state.idToNode;

		this.ID = state.ID;
		this.minPropensity = state.minPropensity;
		this.maxPropensity = state.maxPropensity;
		this.index = state.index;

		if (state.propensity != null)
		{
			this.propensity = state.propensity;
		}
	}

	public void clear()
	{

	}

	@Override
	public HierarchicalModel clone()
	{
		return new HierarchicalModel(this);
	}
	
	public VariableNode addVariable(VariableNode node)
	{
	  variables.add(node);
	  idToNode.put(node.getName(), node);
	  return node;
	}

	public List<VariableNode> getListOfVariables()
	{
	  return variables;
	}
	
	public Map<String, VariableNode> getVariableToNodeMap()
	{
		return idToNode;
	}

	public ReactionNode addReaction(String variable)
	{
		ReactionNode node = new ReactionNode(variable);
		addReaction(node);
		return node;
	}

	public void addReaction(ReactionNode node)
	{
		reactions.add(node);
		idToNode.put(node.getName(), node);
	}

	public EventNode addEvent(HierarchicalNode triggerNode)
	{

		EventNode node = new EventNode(triggerNode);
		addEvent(node);
		return node;
	}

	public void addEvent(EventNode node)
	{
		events.add(node);
	}

	public VariableNode addArray(VariableNode node)
	{
		arrays.add(node);
		idToNode.put(node.getName(), node);
		return node;
	}

	public ConstraintNode addConstraint(String variable, HierarchicalNode node)
	{
		ConstraintNode constraintNode = new ConstraintNode(variable, node);

		constraints.add(constraintNode);

		return constraintNode;
	}

	public List<ConstraintNode> getListOfConstraints()
	{
		return constraints;
	}

	public void addMappingNode(String variable, VariableNode node)
	{
		idToNode.put(variable, node);
	}

	public VariableNode getNode(String variable)
	{
		return idToNode.get(variable);
	}

	public List<ConstraintNode> getConstraints()
	{
		return constraints;
	}

	public ConstraintNode getConstraint(int index)
	{
		return constraints.get(index);
	}

	public void addDeletedBySid(String id)
	{

		if (deletedBySId == null)
		{
			deletedBySId = new HashSet<String>();
		}

		deletedBySId.add(id);
	}

	public void addDeletedByMetaId(String metaid)
	{

		if (deletedByMetaId == null)
		{
			deletedByMetaId = new HashSet<String>();
		}

		deletedByMetaId.add(metaid);
	}

	public boolean isDeletedBySId(String sid)
	{

		if (deletedBySId == null)
		{
			return false;
		}

		return deletedBySId.contains(sid);
	}

	public boolean isDeletedByMetaId(String metaid)
	{

		if (deletedByMetaId == null)
		{
			return false;
		}

		return deletedByMetaId.contains(metaid);
	}

	public List<VariableNode> getArrays()
	{
		return arrays;
	}

	public List<ReactionNode> getReactions()
	{
		return reactions;
	}

	public ReactionNode getReaction(int index)
	{
		return reactions.get(index);
	}

	public List<EventNode> getEvents()
	{
		return events;
	}

	public EventNode getEvent(int index)
	{
		return events.get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ModelState [ID=" + getID() + "]";
	}

	public ModelType getModelType()
	{
		return type;
	}

	public void setModelType(ModelType type)
	{
		this.type = type;
	}

	public void setModelType(Model model)
	{
		if (model == null)
		{
			type = ModelType.NONE;
		}
		else if (model.getSBOTerm() == GlobalConstants.SBO_FLUX_BALANCE)
		{
			type = ModelType.HFBA;
		}
		else if (model.getSBOTerm() == GlobalConstants.SBO_NONSPATIAL_CONTINUOUS)
		{
			type = ModelType.HODE;
		}
		else
		{
			type = ModelType.HSSA;
		}
	}

	public String getID()
	{
		return ID;
	}

	public double getMaxPropensity()
	{
		return maxPropensity;
	}

	public double getMinPropensity()
	{
		return minPropensity;
	}

	public FunctionNode getPropensity()
	{
		return propensity;
	}

	public void setID(String iD)
	{
		ID = iD;
	}

	public void setMaxPropensity(double maxPropensity)
	{
		this.maxPropensity = maxPropensity;
	}

	public void setMinPropensity(double minPropensity)
	{
		this.minPropensity = minPropensity;
	}

	public int getIndex()
	{
		return index;
	}


	public void addInitAssignment(FunctionNode node)
	{
		if(initAssignments == null)
		{
			initAssignments = new ArrayList<FunctionNode>();
		}

		initAssignments.add(node);
	}

	public void addAssignRule(FunctionNode node)
	{
		if(assignRules == null)
		{
			assignRules = new ArrayList<FunctionNode>();
		}
		assignRules.add(node);
	}

	public List<FunctionNode> getInitAssignments() 
	{
		return initAssignments;
	}


	public void setInitAssignments(List<FunctionNode> initAssignments) 
	{
		this.initAssignments = initAssignments;
	}


	public List<FunctionNode> getAssignRules() 
	{
		return assignRules;
	}


	public void setAssignRules(List<FunctionNode> assignRules) 
	{
		this.assignRules = assignRules;
	}
	
	public void addSubmodel(HierarchicalModel submodel)
	{
		if(idToSubmodel == null)
		{
			idToSubmodel = new HashMap<String, HierarchicalModel>();
		}
		
		idToSubmodel.put(submodel.getID(), submodel);
	}
	
	public HierarchicalModel getSubmodel(String id)
	{
		HierarchicalModel submodel = null;
		if(idToSubmodel != null)
		{
			submodel = idToSubmodel.get(id);
		}
		return submodel;
	}
	
	public boolean containsSubmodel(String id)
	{
		
		if(idToSubmodel != null)
		{
			return idToSubmodel.containsKey(id);
		}
		return false;
	}
	
	public void computePropensities()
	{
	  for(ReactionNode node : reactions)
    {
      node.computePropensity(index);
    }
	}

	public void removeSubmodel(String id)
	{
	  idToSubmodel.remove(id);
	}
	
	public void insertPropensity(ReactionNode reaction)
	{
	    this.propensity.addChild(reaction);
	}
}