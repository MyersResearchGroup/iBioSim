package learn.parameterestimator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import learn.genenet.Experiments;
import learn.genenet.SpeciesCollection;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;

import analysis.dynamicsim.hierarchical.util.Setup;
import analysis.dynamicsim.hierarchical.util.ode.ODEState;

public class ParameterEstimator
{

	public static SBMLDocument estimate(SBMLDocument document, List<String> parameterList, Experiments experiments,
			SpeciesCollection speciesCollection)
	{
		final Model model = document.getModel();
		final String modelId = model.getId();
		final String stateId = "topmodel";
		final Map<String, Model> models = new HashMap<String, Model>(1)
		{
			{
				put(modelId, model);
			}
		};
		final EstimateState estimateState = new EstimateState(models, modelId, stateId);
		;
		final ODEState odeState = new ODEState();
		initialize(model, estimateState, odeState, models);
		// TODO: fill this out
		return null;
	}

	/**
	 * Initialize the objects to help determine the math of the variables.
	 * 
	 * @param model
	 * @param estimateState
	 * @param odeState
	 * @param models
	 */
	private static void initialize(final Model model, final EstimateState estimateState, final ODEState odeState, final Map<String, Model> models)
	{

		for (Species species : model.getListOfSpecies())
		{
			Setup.setupSingleSpecies(estimateState, species, species.getId(), models, null);
		}

		for (Reaction reaction : model.getListOfReactions())
		{
			Setup.setupSingleReaction(estimateState, reaction, reaction.getId(), reaction.getKineticLaw().getMath(), reaction.getReversible(),
					reaction.getListOfReactants(), reaction.getListOfProducts(), reaction.getListOfModifiers(), models, null, null, 0);
		}

		odeState.getDvariablesdtime().put(estimateState.getID(), new HashMap<String, ASTNode>());

		for (String reaction : estimateState.getReactionToFormulaMap().keySet())
		{
			odeState.addReaction(estimateState, reaction);
		}
	}

	/**
	 * Get the ODE for a particular species.
	 * 
	 * @param estimateState
	 * @param odeState
	 * @param variable
	 * @return
	 */
	private static ASTNode getMath(final EstimateState estimateState, final ODEState odeState, final String variable)
	{
		return odeState.getDvariablesdtime().get(estimateState.getID()).get(variable);
	}
}
