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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.SBaseRef;
import org.sbml.jsbml.ext.comp.Submodel;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.VariableType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.ValueState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.ModelContainer;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ReplacementSetup
{

	static void setupDeletion(ModelContainer container)
	{
		CompModelPlugin topCompModel = container.getCompModel();
		HierarchicalModel top = container.getHierarchicalModel();
		if (topCompModel.isSetListOfSubmodels())
		{
			for (Submodel submodel : topCompModel.getListOfSubmodels())
			{
				if (submodel.isSetListOfDeletions())
				{
					for (Deletion deletion : submodel.getListOfDeletions())
					{
						HierarchicalModel sub = top.getSubmodel(submodel.getId());
						if (deletion.isSetIdRef())
						{
							if (sub.containsSubmodel(deletion.getIdRef()))
							{
								sub = sub.getSubmodel(deletion.getIdRef());
							}
							if (deletion.isSetSBaseRef())
							{
								if (deletion.getSBaseRef().isSetIdRef())
								{
									String subId = deletion.getSBaseRef().getIdRef();
									sub.addDeletedBySid(subId);
								}
								else if (deletion.getSBaseRef().isSetMetaIdRef())
								{
									String subId = deletion.getSBaseRef().getMetaIdRef();
									sub.addDeletedByMetaId(subId);
								}
							}
							else
							{
								String subId = deletion.getIdRef();
								sub.addDeletedBySid(subId);
							}
						}
						else if (deletion.isSetMetaIdRef())
						{
							String subId = deletion.getMetaIdRef();
							sub.addDeletedByMetaId(subId);
						}
						else if (deletion.isSetPortRef())
						{
							CompModelPlugin subModel = container.getChild(submodel.getId()).getCompModel();
							Port port = subModel.getListOfPorts().get(deletion.getPortRef());
							if (port.isSetIdRef())
							{
								String subId = port.getIdRef();
								sub.addDeletedBySid(subId);
							}
							else if (port.isSetMetaIdRef())
							{
								String subId = port.getMetaIdRef();
								sub.addDeletedByMetaId(subId);
							}
							else if (port.isSetSBaseRef())
							{
								SBaseRef ref = port.getSBaseRef();
								if (ref.isSetIdRef())
								{
									sub.addDeletedBySid(ref.getIdRef());
								}
								else if (ref.isSetMetaIdRef())
								{
									sub.addDeletedByMetaId(ref.getMetaIdRef());
								}
							}
						}
					}
				}
			}
		}
	}

  static void setupReplacement(HierarchicalSimulation sim, SBase sbase, String id, ModelContainer container)
  {
    CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);

    if (sbasePlugin != null)
    {
      if (sbasePlugin.isSetReplacedBy())
      {
        ReplacedBy replacedBy = sbasePlugin.getReplacedBy();
        setupReplacedElement(sim, id, replacedBy.getSubmodelRef(), replacedBy, container, false);
      }

      if (sbasePlugin.isSetListOfReplacedElements())
      {
        for (ReplacedElement element : sbasePlugin.getListOfReplacedElements())
        {
          setupReplacedElement(sim, id,element.getSubmodelRef(), element, container, false);
        }
      }
    }

  }
  
	static void setupReplacement(HierarchicalSimulation sim, AbstractNamedSBase sbase, VectorWrapper wrapper, VariableType varType , ModelContainer container)
	{
		CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);
		String id = sbase.getId();
		
		if (sbasePlugin != null)
		{
			if (sbasePlugin.isSetReplacedBy())
			{
			  ReplacedBy replacedBy = sbasePlugin.getReplacedBy();
			  String subModelId = replacedBy.getSubmodelRef();
			  setupReplacedElement(sim, subModelId, replacedBy, id, container,  wrapper, varType, true);
			}

			if (sbasePlugin.isSetListOfReplacedElements())
			{
				for (ReplacedElement element : sbasePlugin.getListOfReplacedElements())
				{
	        String subModelId = element.getSubmodelRef();
	        setupReplacedElement(sim, subModelId, element, id, container, wrapper, varType, false);
				}
			}
		}

	}

	private static void setupReplacedElement(HierarchicalSimulation sim, String subModelId, SBaseRef element, String id, ModelContainer container, VectorWrapper wrapper, VariableType varType, boolean isReplacedBy)
	{
		HierarchicalModel top = container.getHierarchicalModel();
		HierarchicalModel sub = top.getSubmodel(subModelId);
		CompModelPlugin compModel = container.getChild(subModelId).getCompModel();

		if (element.isSetIdRef())
		{
			if (sub.containsSubmodel(element.getIdRef()))
			{
				sub = sub.getSubmodel(element.getIdRef());
			}
			if (element.isSetSBaseRef())
			{
				SBaseRef ref = element.getSBaseRef();
				while (ref.isSetSBaseRef())
				{
					sub = sub.getSubmodel(ref.getIdRef());
					ref = ref.getSBaseRef();
				}

				String subId = ref.getIdRef();
				performReplacement(sim, id,  top,  subId,  sub,   wrapper, varType, isReplacedBy);
			}
			else
			{
				String subId = element.getIdRef();
				performReplacement(sim, id,  top,  subId,  sub,  wrapper, varType, isReplacedBy);
			}
		}
		else if (element.isSetPortRef())
		{
			Port port = compModel.getListOfPorts().get(element.getPortRef());
			String subId = port.getIdRef();
			performReplacement(sim, id,  top,  subId,  sub,   wrapper, varType, isReplacedBy);
		}
	}

	
	private static void setupReplacedElement(HierarchicalSimulation sim, String id, String subModelId, SBaseRef element, ModelContainer container, boolean isReplacedBy)
  {
    HierarchicalModel top = container.getHierarchicalModel();
    HierarchicalModel sub = top.getSubmodel(subModelId);
    CompModelPlugin compModel = container.getChild(subModelId).getCompModel();

    if (element.isSetIdRef())
    {
      if (sub.containsSubmodel(element.getIdRef()))
      {
        sub = sub.getSubmodel(element.getMetaIdRef());
      }
      if (element.isSetSBaseRef())
      {
        SBaseRef ref = element.getSBaseRef();
        while (ref.isSetSBaseRef())
        {
          sub = sub.getSubmodel(ref.getMetaIdRef());
          ref = ref.getSBaseRef();
        }

        String subId = ref.getMetaIdRef();
        sub.addDeletedBySid(subId);
      }
      else
      {
        String subId = element.getIdRef();
        sub.addDeletedBySid(subId);
      }
    }
    else if (element.isSetMetaIdRef())
    {
      if(isReplacedBy)
      {
        top.addDeletedByMetaId(id);
      }
      else
      {
        String subId = element.getMetaIdRef();
        sub.addDeletedByMetaId(subId);
      }
    }
    else if (element.isSetPortRef())
    {
      Port port = compModel.getListOfPorts().get(element.getPortRef());
      if(isReplacedBy)
      {
        top.addDeletedByMetaId(id);
      }
      String subId = port.getMetaIdRef();
      sub.addDeletedByMetaId(subId);
    }
  }
  
	private static void performReplacement(HierarchicalSimulation sim, String id, HierarchicalModel top, String subId, HierarchicalModel sub, VectorWrapper wrapper, VariableType varType, boolean isReplacedBy)
	{
	  boolean hasTopNode = top.containsNode(id);
	  boolean hasSubNode = sub.containsNode(subId);
	  
	  VariableNode node = null;
	  
	  if(!hasTopNode && !hasSubNode)
	  {
	    if(varType == VariableType.SPECIES)
	    {
	      node = new SpeciesNode(id);
	      ((SpeciesNode)node).createSpeciesTemplate();
	    }
	    else if(varType == VariableType.REACTION)
	    {
	      node = new ReactionNode(id);
	     ((ReactionNode) node).addReactionState(top.getIndex());
	     ((ReactionNode) node).copyReactionState(top.getIndex(), sub.getIndex());
	    }
	    else if(varType == VariableType.VARIABLE)
	    {
	      node = new VariableNode(id);
	    }

      top.addMappingNode(id, node);
      sub.addMappingNode(subId, node);
      
	    HierarchicalState state = node.createState(sim.getCollectionType(), wrapper);
      HierarchicalState child = null;
	    if(sim.getAtomicType() == StateType.VECTOR)
	    {
	      child = new VectorState(wrapper);
	    }
	    else
	    {
	      child = new ValueState();
	    }
	    state.addState(top.getIndex(), child);
	    state.addState(sub.getIndex(), child);
	  }
	  else if(!hasTopNode && hasSubNode)
    {
      node = sub.getNode(subId);
      top.addMappingNode(id, node);
      node.getState().copyState(sub.getIndex(), top.getIndex());
      if(varType == VariableType.REACTION)
      {
       ((ReactionNode) node).copyReactionState(sub.getIndex(), top.getIndex());
      }
    }
	  else
	  {
	    node = top.getNode(id);
      sub.addMappingNode(subId, node);
      node.getState().copyState(top.getIndex(), sub.getIndex());
      if(varType == VariableType.REACTION)
      {
       ((ReactionNode) node).copyReactionState(top.getIndex(), sub.getIndex());
      }
	  }
	  
    if(isReplacedBy)
    {
      top.addDeletedBySid(id);
    }
    else
    {
      sub.addDeletedBySid(subId);
    }
	}


}
