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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;

public  class ModelContainer
{
  private Model model;
  private HierarchicalModel hierarchicalModel;
  private CompModelPlugin compModel;
  private ModelContainer parent;
  private Map<String, ModelContainer> children;
  private String prefix;

  public ModelContainer(Model model, HierarchicalModel hierarchicalModel, ModelContainer parent)
  {
    this.model = model;
    this.hierarchicalModel = hierarchicalModel;
    this.compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
    this.parent = parent;
    setPrefix();
    addChild();
    setModelType(hierarchicalModel, model);
  }

  public Model getModel() {
    return model;
  }

  public HierarchicalModel getHierarchicalModel() {
    return hierarchicalModel;
  }

  public ModelContainer getChild(String id)
  {
    if(children != null)
    {
      return children.get(id);
    }
    return null;
  }

  public CompModelPlugin getCompModel() {
    return compModel;
  }

  public ModelContainer getParent() {
    return parent;
  }

  public String getPrefix()
  {
    return prefix;
  }

  private void addChild()
  {
    if(parent != null)
    {
      if(parent.children == null)
      {
        parent.children = new HashMap<String, ModelContainer>();
      }
      parent.children.put(hierarchicalModel.getID(), this);
      parent.hierarchicalModel.addSubmodel(hierarchicalModel);
    }
  }

  private void setModelType(HierarchicalModel modelstate, Model model)
  {
    int sboTerm = model.isSetSBOTerm() ? model.getSBOTerm() : -1;
    if (sboTerm == GlobalConstants.SBO_FLUX_BALANCE)
    {
      modelstate.setModelType(ModelType.HFBA);
    }
    else if (sboTerm == GlobalConstants.SBO_NONSPATIAL_DISCRETE)
    {
      modelstate.setModelType(ModelType.HSSA);
    }
    else if (sboTerm == GlobalConstants.SBO_NONSPATIAL_CONTINUOUS)
    {
      modelstate.setModelType(ModelType.HODE);
    }
    else
    {
      modelstate.setModelType(ModelType.NONE);
    }

  }

  private void setPrefix()
  {
    if(parent != null)
    {
      this.prefix =  parent.prefix + hierarchicalModel.getID() + "__";
    }
    else
    {
      this.prefix = "";
    }
  }

}