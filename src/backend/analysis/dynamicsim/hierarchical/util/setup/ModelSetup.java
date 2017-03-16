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

package backend.analysis.dynamicsim.hierarchical.util.setup;

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

import backend.analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.methods.HierarchicalMixedSimulator;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import backend.analysis.dynamicsim.hierarchical.states.VectorWrapper;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.comp.ModelContainer;
import dataModels.util.GlobalConstants;

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
  }

  private static void initializeModelStates(HierarchicalSimulation sim, List<ModelContainer> listOfContainers, VariableNode time, ModelType modelType, VectorWrapper wrapper) throws IOException
  {
    StateType type = StateType.SPARSE;

    boolean isSSA = modelType == ModelType.HSSA;
    
    if(modelType == ModelType.HODE)
    {
      type = StateType.VECTOR;
    }
    
    for (int i = listOfContainers.size() - 1; i >= 0; i--)
    {
      ModelContainer container = listOfContainers.get(i);
      CoreSetup.initializeModel(sim, container, type, time, wrapper, isSSA);
    }

    if (isSSA)
    {
      sim.linkPropensities();
    }
    
    if(wrapper != null)
    {
      wrapper.initStateValues();
    }
  }

 
}