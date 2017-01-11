package backend.analysis.dynamicsim.hierarchical.util.setup;

import java.io.IOException;

import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import backend.analysis.dynamicsim.hierarchical.states.VectorWrapper;

public class CoreSetup
{

  public static void initializeVariables(HierarchicalModel modelstate, Model model, StateType type, VariableNode time, VectorWrapper wrapper) throws IOException
  {
    modelstate.createVariableToNodeMap();
    modelstate.addMappingNode(time.getName(), time);
    ParameterSetup.setupParameters(modelstate, type, model, wrapper);
    CompartmentSetup.setupCompartments(modelstate, type,  model, wrapper);
    SpeciesSetup.setupSpecies(modelstate, type, model, wrapper);
    ReactionSetup.setupReactions(modelstate, model);
  }

  //TODO: might be able to merge these
  public static void initializeModel(HierarchicalModel modelstate, Model model, VariableNode time)
  {
    initializeModel(modelstate, model, time, false);
  }

  public static void initializeModel(HierarchicalModel modelstate, Model model, VariableNode time, boolean split)
  {
    ArraysSetup.linkDimensionSize(modelstate);
    ArraysSetup.expandArrays(modelstate);
    EventSetup.setupEvents(modelstate, model);
    ConstraintSetup.setupConstraints(modelstate, model);
    RuleSetup.setupRules(modelstate, model);
    InitAssignmentSetup.setupInitialAssignments(modelstate, model);
  }

}
