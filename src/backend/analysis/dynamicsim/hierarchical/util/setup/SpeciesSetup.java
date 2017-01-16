package backend.analysis.dynamicsim.hierarchical.util.setup;

import java.io.IOException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;

import backend.analysis.dynamicsim.hierarchical.math.FunctionNode;
import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.math.SpeciesNode;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.math.AbstractHierarchicalNode.Type;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

public class SpeciesSetup
{
  /**
   * sets up a single species
   * 
   * @param species
   * @param speciesID
   */
  private static void setupSingleSpecies(HierarchicalModel modelstate, Species species, Model model, StateType type)
  {

    SpeciesNode node = createSpeciesNode(species, type, modelstate.getIndex());
    
    VariableNode compartment = modelstate.getNode(species.getCompartment());
    node.setCompartment(compartment);
    if (species.isSetInitialAmount())
    {
      node.setValue(modelstate.getIndex(), species.getInitialAmount());
    }
    else if (species.isSetInitialConcentration())
    {
      HierarchicalNode initConcentration = new HierarchicalNode(Type.TIMES);
      initConcentration.addChild(new HierarchicalNode(species.getInitialConcentration()));
      initConcentration.addChild(compartment);
      FunctionNode functionNode = new FunctionNode(node, initConcentration);
      modelstate.addInitAssignment(functionNode);
      functionNode.setIsInitAssignment(true);
    }
    if (species.getConstant())
    {
      modelstate.addMappingNode(species.getId(), node);
    }

    else
    {
      modelstate.addVariable(node);
    }

  }

  /**
   * puts species-related information into data structures
   * 
   * @throws IOException
   */
  public static void setupSpecies(HierarchicalModel modelstate,  StateType type, Model model)
  {
    for (Species species : model.getListOfSpecies())
    {
      if (modelstate.isDeletedBySId(species.getId()))
      {
        continue;
      }
      if (ArraysSetup.checkArray(species))
      {
        continue;
      }
      setupSingleSpecies(modelstate, species, model, type);
    }
  }


  private static SpeciesNode createSpeciesNode(Species species, StateType type, int index)
  {
    SpeciesNode node = new SpeciesNode(species.getId());
    node.createSpeciesTemplate(index);
    node.createState(type);
    node.setValue(index, 0);
    node.setBoundaryCondition(species.getBoundaryCondition(), index);
    node.setHasOnlySubstance(species.getHasOnlySubstanceUnits(), index);
    node.setIsVariableConstant(species.getConstant());
  
    return node;
  }

}
