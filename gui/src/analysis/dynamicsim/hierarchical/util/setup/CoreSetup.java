package analysis.dynamicsim.hierarchical.util.setup;

import java.io.IOException;

import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class CoreSetup implements Setup
{

	public static void initializeVariables(HierarchicalModel modelstate, Model model, VariableNode time) throws IOException
	{
		modelstate.createVariableToNodeMap();
		modelstate.addMappingNode(time.getName(), time);
		ParameterSetup.setupParameters(modelstate, model);
		CompartmentSetup.setupCompartments(modelstate, model);
		SpeciesSetup.setupSpecies(modelstate, model);
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
