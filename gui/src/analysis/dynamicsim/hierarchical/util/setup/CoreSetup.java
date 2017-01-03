package analysis.dynamicsim.hierarchical.util.setup;

import java.io.IOException;

import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.math.VariableNode;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;

public class CoreSetup
{

	public static void initializeVariables(HierarchicalModel modelstate, Model model, ModelType type, VariableNode time) throws IOException
	{
		modelstate.createVariableToNodeMap();
		modelstate.addMappingNode(time.getName(), time);
		ParameterSetup.setupParameters(modelstate, type, model);
		CompartmentSetup.setupCompartments(modelstate, type,  model);
		SpeciesSetup.setupSpecies(modelstate, type, model);
		ReactionSetup.setupReactions(modelstate, model);
	}

	public static void initializeModel(HierarchicalModel modelstate, Model model, VariableNode time)
	{
		initializeModel(modelstate, model, time, false);
	}

	public static void initializeModel(HierarchicalModel modelstate, Model model, VariableNode time, boolean split)
	{
		ArraysSetup.linkDimensionSize(modelstate);
		ArraysSetup.expandArrays(modelstate);

		initializeLinks(modelstate, model, time, split);
		EventSetup.setupEvents(modelstate, model);
		ConstraintSetup.setupConstraints(modelstate, model);
		RuleSetup.setupRules(modelstate, model);
		InitAssignmentSetup.setupInitialAssignments(modelstate, model);
	}

	public static void initializeLinks(HierarchicalModel modelstate, Model model, VariableNode time, boolean split)
	{

		SpeciesSetup.setupCompartmentToSpecies(modelstate, model);
		ReactionSetup.setupSpeciesReferenceToReaction(modelstate, model, split);

	}

}
