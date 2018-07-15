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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;

/**
 * Sets up the replacement and deletions in hierarchical models.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
class ReplacementSetup {

  static void setupDeletion(ModelContainer container, List<NodeDeletion> listOfDeletions) {
    CompModelPlugin topCompModel = container.getCompModel();
    HierarchicalModel top = container.getHierarchicalModel();
    if (topCompModel.isSetListOfSubmodels()) {
      for (Submodel submodel : topCompModel.getListOfSubmodels()) {
        if (submodel.isSetListOfDeletions()) {
          for (Deletion deletion : submodel.getListOfDeletions()) {
            HierarchicalModel sub = top.getSubmodel(submodel.getId());
            if (deletion.isSetIdRef()) {
              if (sub.containsSubmodel(deletion.getIdRef())) {
                sub = sub.getSubmodel(deletion.getIdRef());
              }
              if (deletion.isSetSBaseRef()) {
                if (deletion.getSBaseRef().isSetIdRef()) {
                  String subId = deletion.getSBaseRef().getIdRef();
                  listOfDeletions.add(new NodeDeletion(sub, subId, null));
                } else if (deletion.getSBaseRef().isSetMetaIdRef()) {
                  String subId = deletion.getSBaseRef().getMetaIdRef();
                  listOfDeletions.add(new NodeDeletion(sub, null, subId));
                }
              } else {
                String subId = deletion.getIdRef();
                listOfDeletions.add(new NodeDeletion(sub, subId, null));
              }
            } else if (deletion.isSetMetaIdRef()) {
              String subId = deletion.getMetaIdRef();
              listOfDeletions.add(new NodeDeletion(sub, null, subId));
            } else if (deletion.isSetPortRef()) {
              CompModelPlugin subModel = container.getChild(submodel.getId()).getCompModel();
              Port port = subModel.getListOfPorts().get(deletion.getPortRef());
              if (port.isSetIdRef()) {
                String subId = port.getIdRef();
                listOfDeletions.add(new NodeDeletion(sub, subId, null));
              } else if (port.isSetMetaIdRef()) {
                String subId = port.getMetaIdRef();
                listOfDeletions.add(new NodeDeletion(sub, null, subId));
              } else if (port.isSetSBaseRef()) {
                SBaseRef ref = port.getSBaseRef();
                if (ref.isSetIdRef()) {
                  String subId = ref.getIdRef();
                  listOfDeletions.add(new NodeDeletion(sub, subId, null));
                } else if (ref.isSetMetaIdRef()) {
                  String subId = ref.getMetaIdRef();
                  listOfDeletions.add(new NodeDeletion(sub, null, subId));
                }
              }
            }
          }
        }
      }
    }
  }

  static void setupObjectReplacement(HierarchicalSimulation sim, SBase sbase, String id, String metaid, ModelContainer container, List<NodeDeletion> listOfDeletions) {
    CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);

    if (sbasePlugin != null) {
      if (sbasePlugin.isSetReplacedBy()) {
        ReplacedBy replacedBy = sbasePlugin.getReplacedBy();
        replaceElement(sim, id, metaid, replacedBy.getSubmodelRef(), replacedBy, container, true, listOfDeletions);
      }

      if (sbasePlugin.isSetListOfReplacedElements()) {
        for (ReplacedElement element : sbasePlugin.getListOfReplacedElements()) {
          replaceElement(sim, id, metaid, element.getSubmodelRef(), element, container, false, listOfDeletions);
        }
      }
    }

  }

  /**
   * Replacement of non-variables.
   */
  private static void replaceElement(HierarchicalSimulation sim, String id, String metaid, String subModelId, SBaseRef element, ModelContainer container, boolean isReplacedBy, List<NodeDeletion> listOfDeletions) {
    HierarchicalModel top = container.getHierarchicalModel();
    HierarchicalModel sub = top.getSubmodel(subModelId);
    CompModelPlugin compModel = container.getChild(subModelId).getCompModel();

    if (element.isSetIdRef()) {
      if (sub.containsSubmodel(element.getIdRef())) {
        sub = sub.getSubmodel(element.getMetaIdRef());
      }
      if (element.isSetSBaseRef()) {
        SBaseRef ref = element.getSBaseRef();
        while (ref.isSetSBaseRef()) {
          sub = sub.getSubmodel(ref.getMetaIdRef());
          ref = ref.getSBaseRef();
        }
        String subId = ref.getMetaIdRef();
        listOfDeletions.add(new NodeDeletion(sub, subId, null));
      } else {
        String subId = element.getIdRef();
        listOfDeletions.add(new NodeDeletion(sub, subId, null));
      }
    } else if (element.isSetMetaIdRef()) {
      if (isReplacedBy) {
        listOfDeletions.add(new NodeDeletion(top, id, metaid));
      } else {
        String subId = element.getMetaIdRef();
        listOfDeletions.add(new NodeDeletion(sub, null, subId));
      }
    } else if (element.isSetPortRef()) {
      Port port = compModel.getListOfPorts().get(element.getPortRef());
      if (isReplacedBy) {
        listOfDeletions.add(new NodeDeletion(top, id, metaid));
      }
      String subId = port.getMetaIdRef();
      listOfDeletions.add(new NodeDeletion(sub, null, subId));
    }
  }

  static void setupVariableReplacement(HierarchicalSimulation sim, AbstractNamedSBase sbase, VectorWrapper wrapper, ModelContainer container, List<NodeReplacement> listOfReplacements) {
    CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);
    String id = sbase.getId();

    if (sbasePlugin != null) {
      if (sbasePlugin.isSetReplacedBy()) {
        ReplacedBy replacedBy = sbasePlugin.getReplacedBy();
        String subModelId = replacedBy.getSubmodelRef();
        replaceVariable(sim, subModelId, replacedBy, id, container, wrapper, listOfReplacements, true);
      }

      if (sbasePlugin.isSetListOfReplacedElements()) {
        for (ReplacedElement element : sbasePlugin.getListOfReplacedElements()) {
          String subModelId = element.getSubmodelRef();
          replaceVariable(sim, subModelId, element, id, container, wrapper, listOfReplacements, false);
        }
      }
    }
  }

  private static void replaceVariable(HierarchicalSimulation sim, String subModelId, SBaseRef element, String id, ModelContainer container, VectorWrapper wrapper, List<NodeReplacement> listOfReplacements, boolean isReplacedBy) {
    HierarchicalModel top = container.getHierarchicalModel();
    HierarchicalModel sub = top.getSubmodel(subModelId);
    CompModelPlugin compModel = container.getChild(subModelId).getCompModel();
    String subId = null;
    if (element.isSetIdRef()) {
      if (sub.containsSubmodel(element.getIdRef())) {
        sub = sub.getSubmodel(element.getIdRef());
      }
      if (element.isSetSBaseRef()) {
        SBaseRef ref = element.getSBaseRef();
        while (ref.isSetSBaseRef()) {
          sub = sub.getSubmodel(ref.getIdRef());
          ref = ref.getSBaseRef();
        }
        subId = ref.getIdRef();
      } else {
        subId = element.getIdRef();

      }
    } else if (element.isSetPortRef()) {
      Port port = compModel.getListOfPorts().get(element.getPortRef());
      subId = port.getIdRef();
    }
    if (subId != null) {
      if (isReplacedBy) {
        listOfReplacements.add(new NodeReplacement(sub, top, subId, id));
      } else {
        listOfReplacements.add(new NodeReplacement(top, sub, id, subId));
      }
    }
  }

}
