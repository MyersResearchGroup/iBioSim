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

package edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.ExternalModelDefinition;
import org.sbml.jsbml.ext.comp.Submodel;

import edu.utah.ece.async.util.GlobalConstants;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.math.VariableNode;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.methods.HierarchicalMixedSimulator;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.analysis.dynamicsim.hierarchical.util.comp.ReplacementHandler;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ModelSetup
{
  /**
   * Initializes the modelstate array
   * 
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void setupModels(HierarchicalSimulation sim, ModelType type) throws XMLStreamException, IOException
  {
    setupModels(sim, type, null);
  }
  
  /**
   * Initializes the modelstate array
   * 
   * @throws IOException
   * @throws XMLStreamException
   */
  public static void setupModels(HierarchicalSimulation sim, ModelType type, VectorWrapper wrapper) throws XMLStreamException, IOException
  {
    
    SBMLDocument document = sim.getDocument();
    Model model = document.getModel();
    String rootPath = sim.getRootDirectory();

    List<HierarchicalModel> listOfModules = new ArrayList<HierarchicalModel>();
    List<Model> listOfModels = new ArrayList<Model>();
    List<String> listOfPrefix = new ArrayList<String>();
    List<ReplacementHandler> listOfHandlers = new ArrayList<ReplacementHandler>();
    
    Map<String, Integer> mapOfModels = new HashMap<String, Integer>();
    
    sim.setListOfModules(listOfModules);

    

    CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document.getPlugin(CompConstants.namespaceURI);
    CompModelPlugin sbmlCompModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
    HierarchicalModel topmodel = new HierarchicalModel("topmodel");
    sim.setTopmodel(topmodel);
    mapOfModels.put("topmodel", 0);
    setModelType(topmodel, model);

    listOfPrefix.add("");
    listOfModules.add(topmodel);
    listOfModels.add(model);

    if (sbmlCompModel != null)
    {
      setupSubmodels(sim, rootPath, "", sbmlComp, sbmlCompModel, listOfModules, listOfModels, listOfPrefix, mapOfModels);
      ReplacementSetup.setupReplacements(listOfHandlers, listOfModules, listOfModels, listOfPrefix, mapOfModels);
    }

    initializeModelStates(sim, listOfHandlers, listOfModules, listOfModels, sim.getCurrentTime(), type, wrapper);

    if (sim instanceof HierarchicalMixedSimulator)
    {
      initializeHybridSimulation((HierarchicalMixedSimulator) sim, listOfModels, listOfModules);
    }
  }

  private static void setupSubmodels(HierarchicalSimulation sim, String path, String prefix, CompSBMLDocumentPlugin sbmlComp, CompModelPlugin sbmlCompModel, List<HierarchicalModel> listOfModules, List<Model> listOfModels, List<String> listOfPrefix, Map<String, Integer> mapOfModels)
      throws XMLStreamException, IOException
  {

    for (Submodel submodel : sbmlCompModel.getListOfSubmodels())
    {

      String newPrefix = prefix + submodel.getId() + "__";
      Model model = null;
      CompModelPlugin compModel = null;
      CompSBMLDocumentPlugin compDoc = sbmlComp;
      if (sbmlComp.getListOfExternalModelDefinitions() != null && sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
      {
        ExternalModelDefinition ext = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef());
        String source = ext.getSource();
        String extDef = path + HierarchicalUtilities.separator + source;
        SBMLDocument extDoc = SBMLReader.read(new File(extDef));
        model = extDoc.getModel();
        compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
        compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);

        while (ext.isSetModelRef())
        {
          if (compDoc.getExternalModelDefinition(ext.getModelRef()) != null)
          {
            ext = compDoc.getListOfExternalModelDefinitions().get(ext.getModelRef());
            source = ext.getSource().replace("file:", "");
            extDef = path + HierarchicalUtilities.separator + source;
            extDoc = SBMLReader.read(new File(extDef));
            model = extDoc.getModel();
            compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
            compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
          }
          else if (compDoc.getModelDefinition(ext.getModelRef()) != null)
          {
            model = compDoc.getModelDefinition(ext.getModelRef());
            compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
            break;
          }
          else
          {
            break;
          }
        }
      }
      else if (sbmlComp.getListOfModelDefinitions() != null && sbmlComp.getListOfModelDefinitions().get(submodel.getModelRef()) != null)
      {
        model = sbmlComp.getModelDefinition(submodel.getModelRef());
        compModel = (CompModelPlugin) model.getPlugin(CompConstants.namespaceURI);
      }

      if (model != null)
      {
        String id = prefix + submodel.getId();
        HierarchicalModel modelstate = new HierarchicalModel(id);
        sim.addSubmodel(id, modelstate);
        mapOfModels.put(id, mapOfModels.size());
        listOfPrefix.add(newPrefix);
        listOfModules.add(modelstate);
        listOfModels.add(model);
        setModelType(modelstate, model);
        setupSubmodels(sim, path, newPrefix, compDoc, compModel, listOfModules, listOfModels, listOfPrefix, mapOfModels);
      }
    }
  }

  private static void initializeModelStates(HierarchicalSimulation sim, List<ReplacementHandler> listOfHandlers, List<HierarchicalModel> listOfModules, List<Model> listOfModels, VariableNode time, ModelType modelType, VectorWrapper wrapper) throws IOException
  {
    StateType type = StateType.SPARSE;
    
    if(modelType == ModelType.HODE)
    {
      type = StateType.VECTOR;
    }
    
    for (int i = 0; i < listOfModules.size(); i++)
    {
      CoreSetup.initializeVariables(listOfModules.get(i), listOfModels.get(i), type, time, wrapper);
    }

    for (int i = listOfHandlers.size() - 1; i >= 0; i--)
    {
      listOfHandlers.get(i).copyNodeTo();
    }

    boolean isSSA = modelType == ModelType.HSSA;
    
    for (int i = 0; i < listOfModules.size(); i++)
    {
      CoreSetup.initializeModel(listOfModules.get(i), listOfModels.get(i), time, isSSA);
    }

    if (isSSA)
    {
      sim.linkPropensities();
    }
  }

  private static void initializeHybridSimulation(HierarchicalMixedSimulator sim, List<Model> listOfModels, List<HierarchicalModel> listOfModules) throws IOException
  {
    List<HierarchicalModel> listOfODEStates = new ArrayList<HierarchicalModel>();
    List<HierarchicalModel> listOfSSAStates = new ArrayList<HierarchicalModel>();
    List<HierarchicalModel> listOfFBAStates = new ArrayList<HierarchicalModel>();
    List<Model> listOfFBAModels = new ArrayList<Model>();

    for (int i = 0; i < listOfModels.size(); i++)
    {
      Model model = listOfModels.get(i);
      HierarchicalModel state = listOfModules.get(i);

      if (state.getModelType() == ModelType.HFBA)
      {
        listOfFBAStates.add(state);
        listOfFBAModels.add(model);
      }
      else if (state.getModelType() == ModelType.HSSA)
      {
        listOfSSAStates.add(state);
      }
      else
      {
        listOfODEStates.add(state);
      }
    }

    addSimulationMethod(sim, listOfODEStates, false);
    addSimulationMethod(sim, listOfSSAStates, true);
    addFBA(sim, listOfFBAModels, listOfFBAStates);
  }

  private static void addSimulationMethod(HierarchicalMixedSimulator sim, List<HierarchicalModel> listOfODEStates, boolean isSSA)
  {
    if (listOfODEStates.size() > 0)
    {
      HierarchicalModel topmodel = listOfODEStates.get(0);
      Map<String, HierarchicalModel> submodels = listOfODEStates.size() > 1 ? new HashMap<String, HierarchicalModel>() : null;

      for (int i = 1; i < listOfODEStates.size(); i++)
      {
        HierarchicalModel submodel = listOfODEStates.get(i);
        submodels.put(submodel.getID(), submodel);
      }

      if (isSSA)
      {
        sim.createODESim(topmodel, submodels);
      }
      else
      {
        sim.createODESim(topmodel, submodels);
      }
    }
  }

  // TODO: generalize this
  private static void addFBA(HierarchicalMixedSimulator sim, List<Model> listOfFBAModels, List<HierarchicalModel> listOfFBAStates)
  {
    if (listOfFBAModels.size() > 0)
    {
      HierarchicalModel state = listOfFBAStates.get(0);
      Model model = listOfFBAModels.get(0);
      sim.createFBASim(state, model);
    }
  }

  private static void setModelType(HierarchicalModel modelstate, Model model)
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
}