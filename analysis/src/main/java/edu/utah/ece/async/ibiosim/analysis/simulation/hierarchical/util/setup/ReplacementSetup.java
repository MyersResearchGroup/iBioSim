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

import java.util.List;

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

  static void setupReplacement(HierarchicalSimulation sim, AbstractNamedSBase sbase, VectorWrapper wrapper, ModelContainer container, List<NodeReplacement> listOfReplacements)
  {
    CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);
    String id = sbase.getId();

    if (sbasePlugin != null)
    {
      if (sbasePlugin.isSetReplacedBy())
      {
        ReplacedBy replacedBy = sbasePlugin.getReplacedBy();
        String subModelId = replacedBy.getSubmodelRef();
        setupReplacedElement(sim, subModelId, replacedBy, id, container,   wrapper, listOfReplacements, true);
      }

      if (sbasePlugin.isSetListOfReplacedElements())
      {
        for (ReplacedElement element : sbasePlugin.getListOfReplacedElements())
        {
          String subModelId = element.getSubmodelRef();
          setupReplacedElement(sim, subModelId, element, id, container, wrapper, listOfReplacements, false);
        }
      }
    }

  }

  /**
   * Replacement of variables.
   *
   */
  private static void setupReplacedElement(HierarchicalSimulation sim, String subModelId, SBaseRef element, String id, ModelContainer container, VectorWrapper wrapper, List<NodeReplacement> listOfReplacements, boolean isReplacedBy)
  {
    HierarchicalModel top = container.getHierarchicalModel();
    HierarchicalModel sub = top.getSubmodel(subModelId);
    CompModelPlugin compModel = container.getChild(subModelId).getCompModel();
    String subId = null;
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

        subId = ref.getIdRef();

      }
      else
      {
        subId = element.getIdRef();

      }
    }
    else if (element.isSetPortRef())
    {
      Port port = compModel.getListOfPorts().get(element.getPortRef());
      subId = port.getIdRef();
    }

    if(subId != null)
    {
      if(isReplacedBy)
      {
        listOfReplacements.add(new NodeReplacement(sub, top, subId, id));
      }
      else
      {
        listOfReplacements.add(new NodeReplacement(top, sub, id, subId));
      }
    }
  }


  /**
   * Replacement of non-variables.
   *
   */
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

}
