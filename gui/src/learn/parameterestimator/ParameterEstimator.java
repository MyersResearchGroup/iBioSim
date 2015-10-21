package learn.parameterestimator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import learn.genenet.Experiments;
import learn.genenet.SpeciesCollection;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.util.Setup;
import analysis.dynamicsim.hierarchical.util.ode.ODEState;

public class ParameterEstimator
{

	public static SBMLDocument estimate(SBMLDocument document, List<String> parameterList, Experiments experiments, SpeciesCollection speciesCollection)
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
			String reactionID = reaction.getId();
			if (reaction.isReversible())
			{
				for (SpeciesReference reactant : reaction.getListOfReactants())
				{
					Setup.setupSingleRevReactant(estimateState, reactionID, reactant.getSpecies(), reactant, reaction.getListOfProducts().size(), null);
				}

				for (SpeciesReference product : reaction.getListOfProducts())
				{
					Setup.setupSingleRevProduct(estimateState, reactionID, product.getSpecies(), product, reaction.getListOfReactants().size(), null);
				}

				for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					Setup.setupSingleModifier(estimateState, reactionID + "_fd", modifier.getSpecies());
					Setup.setupSingleModifier(estimateState, reactionID + "_rv", modifier.getSpecies());
				}
			}
			else
			{
				for (SpeciesReference reactant : reaction.getListOfReactants())
				{
					Setup.setupSingleReactant(estimateState, reactionID, reactant.getSpecies(), reactant, null);
				}

				for (SpeciesReference product : reaction.getListOfProducts())
				{
					Setup.setupSingleProduct(estimateState, reactionID, product.getSpecies(), product, null);
				}

				for (ModifierSpeciesReference modifier : reaction.getListOfModifiers())
				{
					Setup.setupSingleModifier(estimateState, reactionID, modifier.getSpecies());
				}
			}

			Setup.setupSingleReaction(estimateState, reaction, reactionID, reaction.getKineticLaw().getMath(), reaction.getReversible(), models, null, null, 0.0);
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
