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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.DeletionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.ReplacementNode;

/**
 * Sets up the replacement and deletions in hierarchical models.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
class ReplacementSetup {

  static void setupDeletion(ModelContainer container) {
    CompModelPlugin topCompModel = container.getCompModel();
    HierarchicalModel top = container.getHierarchicalModel();
    if (topCompModel.isSetListOfSubmodels()) {
      for (Submodel submodel : topCompModel.getListOfSubmodels()) {
        if (submodel.isSetListOfDeletions()) {
          for (Deletion deletion : submodel.getListOfDeletions()) {
            HierarchicalModel sub = top.getSubmodel(submodel.getId());
            ArrayList<String> submodels = new ArrayList<>();
            submodels.add(submodel.getId());
            DeletionNode nodeDeletion = new DeletionNode(submodels);
            top.addDeletion(nodeDeletion);
            if (deletion.isSetIdRef()) {
              if (sub.containsSubmodel(deletion.getIdRef())) {
                sub = sub.getSubmodel(deletion.getIdRef());
                submodels.add(deletion.getIdRef());
              }
              if (deletion.isSetSBaseRef()) {
                if (deletion.getSBaseRef().isSetIdRef()) {
                  String subId = deletion.getSBaseRef().getIdRef();
                  nodeDeletion.setSubIdRef(subId);
                } else if (deletion.getSBaseRef().isSetMetaIdRef()) {
                  String subId = deletion.getSBaseRef().getMetaIdRef();
                  nodeDeletion.setSubMetaIdRef(subId);
                }
              } else {
                String subId = deletion.getIdRef();
                nodeDeletion.setSubIdRef(subId);
              }
            } else if (deletion.isSetMetaIdRef()) {
              String subId = deletion.getMetaIdRef();
              nodeDeletion.setSubMetaIdRef(subId);
            } else if (deletion.isSetPortRef()) {
              CompModelPlugin subModel = container.getChild(submodel.getId()).getCompModel();
              Port port = subModel.getListOfPorts().get(deletion.getPortRef());
              if (port.isSetIdRef()) {
                String subId = port.getIdRef();
                nodeDeletion.setSubIdRef(subId);
              } else if (port.isSetMetaIdRef()) {
                String subId = port.getMetaIdRef();
                nodeDeletion.setSubMetaIdRef(subId);
              } else if (port.isSetSBaseRef()) {
                SBaseRef ref = port.getSBaseRef();
                if (ref.isSetIdRef()) {
                  String subId = ref.getIdRef();
                  nodeDeletion.setSubIdRef(subId);
                } else if (ref.isSetMetaIdRef()) {
                  String subId = ref.getMetaIdRef();
                  nodeDeletion.setSubMetaIdRef(subId);
                }
              }
            }
          }
        }
      }
    }
  }

  static void setupObjectReplacement(HierarchicalSimulation sim, SBase sbase, String id, String metaid, ModelContainer container) {
    CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);

    if (sbasePlugin != null) {

      if (sbasePlugin.isSetListOfReplacedElements()) {
        for (ReplacedElement element : sbasePlugin.getListOfReplacedElements()) {
          replaceElement(sim, id, metaid, element.getSubmodelRef(), element, container, false);
        }
      }

      if (sbasePlugin.isSetReplacedBy()) {
        ReplacedBy replacedBy = sbasePlugin.getReplacedBy();
        replaceElement(sim, id, metaid, replacedBy.getSubmodelRef(), replacedBy, container, true);
      }
    }

  }

  /**
   * Replacement of non-variables.
   */
  private static void replaceElement(HierarchicalSimulation sim, String id, String metaid, String subModelId, SBaseRef element, ModelContainer container, boolean isReplacedBy) {
    HierarchicalModel top = container.getHierarchicalModel();
    HierarchicalModel sub = top.getSubmodel(subModelId);
    CompModelPlugin compModel = container.getChild(subModelId).getCompModel();

    ArrayList<String> submodels = new ArrayList<>();
    ReplacementNode replacement = new ReplacementNode(submodels);
    submodels.add(subModelId);

    top.addReplacement(replacement);
    replacement.setJustDelete(true);

    replacement.setTopIdRef(id);
    replacement.setTopMetaIdRef(metaid);
    replacement.setReplacedBy(isReplacedBy);

    if (element.isSetIdRef()) {
      if (sub.containsSubmodel(element.getIdRef())) {
        sub = sub.getSubmodel(element.getMetaIdRef());
      }
      if (element.isSetSBaseRef()) {
        SBaseRef ref = element.getSBaseRef();
        while (ref.isSetSBaseRef()) {
          submodels.add(ref.getMetaIdRef());
          sub = sub.getSubmodel(ref.getMetaIdRef());
          ref = ref.getSBaseRef();
        }
        String subId = ref.getMetaIdRef();
        replacement.setSubMetaIdRef(subId);
      } else {
        String subId = element.getIdRef();
        replacement.setSubIdRef(subId);
      }
    } else if (element.isSetMetaIdRef()) {
      String subId = element.getMetaIdRef();
      replacement.setSubMetaIdRef(subId);
    } else if (element.isSetPortRef()) {
      Port port = compModel.getListOfPorts().get(element.getPortRef());
      String subId = port.getMetaIdRef();
      replacement.setSubMetaIdRef(subId);
    }
  }

  static void setupVariableReplacement(HierarchicalSimulation sim, AbstractNamedSBase sbase, VectorWrapper wrapper, ModelContainer container) {
    CompSBasePlugin sbasePlugin = (CompSBasePlugin) sbase.getExtension(CompConstants.shortLabel);
    String id = sbase.getId();

    if (sbasePlugin != null) {
      if (sbasePlugin.isSetListOfReplacedElements()) {
        for (ReplacedElement element : sbasePlugin.getListOfReplacedElements()) {
          String subModelId = element.getSubmodelRef();
          replaceVariable(sim, subModelId, element, id, container, wrapper, false);
        }
      }
      if (sbasePlugin.isSetReplacedBy()) {
        ReplacedBy replacedBy = sbasePlugin.getReplacedBy();
        String subModelId = replacedBy.getSubmodelRef();
        replaceVariable(sim, subModelId, replacedBy, id, container, wrapper, true);
      }
    }
  }

  private static void replaceVariable(HierarchicalSimulation sim, String subModelId, SBaseRef element, String id, ModelContainer container, VectorWrapper wrapper, boolean isReplacedBy) {
    HierarchicalModel top = container.getHierarchicalModel();
    HierarchicalModel sub = top.getSubmodel(subModelId);
    CompModelPlugin compModel = container.getChild(subModelId).getCompModel();
    ArrayList<String> submodels = new ArrayList<>();

    ReplacementNode replacement = new ReplacementNode(submodels);
    submodels.add(subModelId);
    top.addReplacement(replacement);

    replacement.setTopIdRef(id);
    replacement.setReplacedBy(isReplacedBy);

    if (element.isSetIdRef()) {
      if (sub.containsSubmodel(element.getIdRef())) {
        sub = sub.getSubmodel(element.getIdRef());
        submodels.add(element.getIdRef());
      }
      if (element.isSetSBaseRef()) {
        SBaseRef ref = element.getSBaseRef();
        while (ref.isSetSBaseRef()) {
          String idRef = ref.getIdRef();
          sub = sub.getSubmodel(idRef);
          ref = ref.getSBaseRef();
          submodels.add(idRef);
        }
        replacement.setSubIdRef(ref.getIdRef());
      } else {
        replacement.setSubIdRef(element.getIdRef());
      }
    } else if (element.isSetPortRef()) {
      Port port = compModel.getListOfPorts().get(element.getPortRef());
      replacement.setSubIdRef(port.getIdRef());
    } else if (element.isSetMetaIdRef()) {
      replacement.setSubMetaIdRef(element.getMetaIdRef());
    }
  }

  static void initializeComp(List<ModelContainer> listOfContainers) {
    Map<HierarchicalState, List<ReplacementDependency>> stateDependencies = new HashMap<>();
    for (int i = listOfContainers.size() - 1; i >= 0; i--) {
      ModelContainer container = listOfContainers.get(i);
      HierarchicalModel topModel = container.getHierarchicalModel();
      performDeletions(topModel);
      performReplacements(topModel, stateDependencies);
    }
  }

  private static void performDeletions(HierarchicalModel topmodel) {
    for (DeletionNode deletion : topmodel.getListOfDeletions()) {
      HierarchicalModel subModel = deletion.getSubmodel(topmodel);
      if (deletion.getSubIdRef() != null) {
        subModel.getNode(deletion.getSubIdRef()).deleteElement(subModel.getIndex());
      } else if (deletion.getSubMetaIdRef() != null) {
        subModel.getNodeByMetaId(deletion.getSubMetaIdRef()).deleteElement(subModel.getIndex());
      }
    }
  }

  private static void performReplacements(HierarchicalModel topModel, Map<HierarchicalState, List<ReplacementDependency>> stateDependencies) {
    for (ReplacementNode replacement : topModel.getListOfReplacements()) {
      performReplacement(topModel, replacement, stateDependencies);
    }
  }

  private static void performReplacement(HierarchicalModel topModel, ReplacementNode replacement, Map<HierarchicalState, List<ReplacementDependency>> stateDependencies) {

    HierarchicalNode topNode, subNode;
    HierarchicalModel subModel = replacement.getSubmodel(topModel);

    if (replacement.getTopIdRef() != null) {
      topNode = topModel.getNode(replacement.getTopIdRef());
    } else if (replacement.getTopMetaIdRef() != null) {
      topNode = topModel.getNodeByMetaId(replacement.getTopMetaIdRef());
    } else {
      return;
    }

    if (replacement.getSubIdRef() != null) {
      subNode = subModel.getNode(replacement.getSubIdRef());
    } else if (replacement.getSubMetaIdRef() != null) {
      subNode = subModel.getNodeByMetaId(replacement.getSubMetaIdRef());
    } else {
      return;
    }

    if (replacement.isJustDelete()) {
      if (replacement.isReplacedBy()) {
        topNode.deleteElement(topModel.getIndex());
      } else {
        subNode.deleteElement(subModel.getIndex());
      }
      return;
    }

    if (replacement.isReplacedBy()) {
      replace(subModel, subNode, topModel, topNode, replacement, stateDependencies);
    } else {
      replace(topModel, topNode, subModel, subNode, replacement, stateDependencies);
    }
  }

  private static void replace(HierarchicalModel replacingModel, HierarchicalNode replacingNode, HierarchicalModel replacedModel, HierarchicalNode replacedNode, ReplacementNode replacement, Map<HierarchicalState, List<ReplacementDependency>> stateDependencies) {
    int replacingIndex = replacingModel.getIndex();
    int replacedIndex = replacedModel.getIndex();

    HierarchicalState replacingState = replacingNode.getState().getChild(replacingIndex);
    HierarchicalState replacedState = replacedNode.getState().getChild(replacedIndex);

    replacingState.setHasInitRule(replacingState.hasInitRule() || replacedState.hasInitRule());
    replacingState.setHasRate(replacingState.hasRate() || replacedState.hasRate());
    replacingState.setHasRule(replacingState.hasRule() || replacedState.hasRule());

    List<ReplacementDependency> dependencies;
    if (stateDependencies.containsKey(replacedState)) {
      dependencies = stateDependencies.remove(replacedState);
    } else {
      dependencies = new ArrayList<>();
      dependencies.add(new ReplacementDependency(replacedNode, replacedIndex));
    }

    if (replacedNode.isReaction()) {
      replacedNode.deleteElement(replacedIndex);
    }

    replacedState.setReplaced(true);

    for (ReplacementDependency dependency : dependencies) {
      dependency.replaceNode(replacingState);
    }
    dependencies.add(new ReplacementDependency(replacingNode, replacingIndex));
    stateDependencies.put(replacingState, dependencies);
  }

  private static class ReplacementDependency {
    private final HierarchicalNode subNode;
    private final int index;

    ReplacementDependency(HierarchicalNode subNode, int index) {
      this.subNode = subNode;
      this.index = index;
    }

    void replaceNode(HierarchicalState state) {
      subNode.getState().replaceState(index, state);
    }

  }

}
