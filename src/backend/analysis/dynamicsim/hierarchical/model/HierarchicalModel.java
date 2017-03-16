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
package backend.analysis.dynamicsim.hierarchical.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.hierarchical.math.ConstraintNode;
import backend.analysis.dynamicsim.hierarchical.math.EventNode;
import backend.analysis.dynamicsim.hierarchical.math.FunctionNode;
import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.math.ReactionNode;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import dataModels.util.GlobalConstants;

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
	private HierarchicalNode  propensity;
	private double      initPropensity;

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
			this.initPropensity = state.initPropensity;
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
	  if(variables == null)
	  {
	    variables = new ArrayList<VariableNode>();
	  }

    if (idToNode == null)
    {
      idToNode = new HashMap<String, VariableNode>();
    }
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
		if (reactions == null)
		{
			reactions = new ArrayList<ReactionNode>();
		}
		if (idToNode == null)
		{
			idToNode = new HashMap<String, VariableNode>();
		}
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

		if (events == null)
		{
			events = new LinkedList<EventNode>();
		}

		events.add(node);
	}

	public VariableNode addArray(VariableNode node)
	{

		if (arrays == null)
		{
			arrays = new ArrayList<VariableNode>();
		}

		if (idToNode == null)
		{
			idToNode = new HashMap<String, VariableNode>();
		}
		arrays.add(node);
		idToNode.put(node.getName(), node);
		return node;
	}

	public ConstraintNode addConstraint(String variable, HierarchicalNode node)
	{

		if (constraints == null)
		{
			constraints = new ArrayList<ConstraintNode>();
		}

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

	public int getNumOfReactions()
	{
		return reactions == null ? 0 : reactions.size();
	}

	public int getNumOfEvents()
	{
		return events == null ? 0 : events.size();
	}

	public int getNumOfConstraints()
	{
		return constraints == null ? 0 : constraints.size();
	}

	public int getNumOfArrays()
	{
		return arrays == null ? 0 : arrays.size();
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

	public double getPropensity(int index)
	{
		return propensity.getValue(index);
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

	public HierarchicalNode createPropensity()
	{
		this.propensity = new HierarchicalNode(0);
		return propensity;
	}

	public void setInitPropensity(int index)
	{
		if (propensity != null)
		{
			this.initPropensity = propensity.getValue(index);
		}
	}

	public void restoreInitPropensity(int index)
	{
		this.propensity.setValue(index, initPropensity);
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

}