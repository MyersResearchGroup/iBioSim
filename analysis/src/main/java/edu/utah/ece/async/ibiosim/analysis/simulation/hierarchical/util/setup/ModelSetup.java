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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.HierarchicalSimulation;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.methods.HierarchicalMixedSimulator;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel.ModelType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.VectorWrapper;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states.HierarchicalState.StateType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.comp.ModelContainer;

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
    HierarchicalModel hierarchicalModel = new HierarchicalModel("topmodel");
    sim.setTopmodel(hierarchicalModel);
    
    CompSBMLDocumentPlugin sbmlComp = (CompSBMLDocumentPlugin) document.getPlugin(CompConstants.namespaceURI);
    
    LinkedList<ModelContainer> unproc = new LinkedList<ModelContainer>();
    List<ModelContainer> listOfContainers = new ArrayList<ModelContainer>();
    //TODO: Map<String, ModelContainer> templateFromSource;
    
    unproc.push(new ModelContainer(model, hierarchicalModel, null));
    
    while(!unproc.isEmpty())
    {
      ModelContainer container = unproc.pop();
      listOfContainers.add(container);
      sim.addModelState(container.getHierarchicalModel());
      
      if (container.getCompModel() != null)
      {
        for (Submodel submodel : container.getCompModel().getListOfSubmodels())
        {
          model = null;
          CompSBMLDocumentPlugin compDoc = sbmlComp;
          if (sbmlComp.getListOfExternalModelDefinitions() != null && sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()) != null)
          {
            ExternalModelDefinition ext = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef());
            String source = ext.getSource();
            String extDef = rootPath + HierarchicalUtilities.separator + source;
            SBMLDocument extDoc = SBMLReader.read(new File(extDef));
            model = extDoc.getModel();
            compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);

            while (ext.isSetModelRef())
            {
              if (compDoc.getExternalModelDefinition(ext.getModelRef()) != null)
              {
                ext = compDoc.getListOfExternalModelDefinitions().get(ext.getModelRef());
                source = ext.getSource().replace("file:", "");
                extDef = rootPath + HierarchicalUtilities.separator + source;
                extDoc = SBMLReader.read(new File(extDef));
                model = extDoc.getModel();
                compDoc = (CompSBMLDocumentPlugin) extDoc.getPlugin(CompConstants.namespaceURI);
              }
              else if (compDoc.getModelDefinition(ext.getModelRef()) != null)
              {
                model = compDoc.getModelDefinition(ext.getModelRef());
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
          }

          if (model != null)
          {
            hierarchicalModel = new HierarchicalModel(submodel.getId());
            unproc.push(new ModelContainer(model, hierarchicalModel, container));
          }
        }
      }
    }
    initializeModelStates(sim, listOfContainers, sim.getCurrentTime(), type, wrapper);
    
    if (sim instanceof HierarchicalMixedSimulator)
    {
      initializeHybridSimulation((HierarchicalMixedSimulator) sim, listOfContainers);
    }
  }

  private static void initializeModelStates(HierarchicalSimulation sim, List<ModelContainer> listOfContainers, VariableNode time, ModelType modelType, VectorWrapper wrapper) throws IOException
  {
    StateType type = StateType.SCALAR;

    boolean isSSA = modelType == ModelType.HSSA;
    
    if(modelType == ModelType.HODE)
    {
      type = StateType.VECTOR;
    }
    
    
    for(ModelContainer container : listOfContainers)
    {
      ReplacementSetup.setupDeletion(container);
      CoreSetup.initializeModel(sim, container, type, time, wrapper, isSSA);
    }
    
    if(wrapper != null)
    {
      wrapper.initStateValues();
    }
  }

  private static void initializeHybridSimulation(HierarchicalMixedSimulator sim, List<ModelContainer> listOfContainers) throws IOException, XMLStreamException
  {

    List<HierarchicalModel> listOfODEModels = new ArrayList<HierarchicalModel>();
    for (ModelContainer container : listOfContainers)
    {
      HierarchicalModel state = container.getHierarchicalModel();

      if (state.getModelType() == ModelType.HFBA)
      {
        sim.createFBASim(state, container.getModel());
      }
      else if(state.getModelType() == ModelType.HODE)
      {
        listOfODEModels.add(state);
      }
    }
    
    sim.createODESim(listOfContainers.get(0).getHierarchicalModel(), listOfODEModels);
  }
 
}