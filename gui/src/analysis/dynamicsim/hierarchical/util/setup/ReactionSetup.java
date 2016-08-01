package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;

public class ReactionSetup
{

	public static void setupReactions(ModelState modelstate, Model model)
	{
		Reaction reaction;
		for (int i = 0; i < model.getNumReactions(); i++)
		{
			reaction = model.getReaction(i);
			ParameterSetup.setupLocalParameters(modelstate, reaction.getKineticLaw(), reaction);
			if (modelstate.isDeletedBySId(reaction.getId()))
			{
				continue;
			}
			else if (ArraysSetup.checkArray(reaction))
			{
				continue;
			}
			modelstate.addReaction(reaction.getId());

		}

	}

	public static void setupSpeciesReferenceToReaction(ModelState modelstate, Model model, boolean split)
	{
		for (Reaction reaction : model.getListOfReactions())
		{
			if (modelstate.isDeletedBySId(reaction.getId()))
			{
				continue;
			}
			ReactionNode reactionNode = (ReactionNode) modelstate.getNode(reaction.getId());

			for (SpeciesReference reactant : reaction.getListOfReactants())
			{
				SpeciesReferenceSetup.setupSingleReactant(modelstate, reactionNode, reactant.getSpecies(), reactant);
			}
			for (SpeciesReference product : reaction.getListOfProducts())
			{
				SpeciesReferenceSetup.setupSingleProduct(modelstate, reactionNode, product.getSpecies(), product);
			}
			KineticLaw kineticLaw = reaction.getKineticLaw();
			if (kineticLaw != null)
			{
				ASTNode reactionFormula = kineticLaw.getMath();
				setupSingleReaction(modelstate, reaction, reactionNode, reactionFormula, reaction.getReversible(), split, model, 0);
			}
		}
	}

	/**
	 * calculates the initial propensity of a single reaction also does some
	 * initialization stuff
	 * 
	 * @param reactionID
	 * @param reactionFormula
	 * @param reversible
	 * @param reactantsList
	 * @param productsList
	 * @param modifiersList
	 */
	private static void setupSingleReaction(ModelState modelstate, Reaction reaction, ReactionNode forward, ASTNode reactionFormula, boolean reversible, boolean split, Model model, double currentTime)
	{
		reactionFormula = HierarchicalUtilities.inlineFormula(modelstate, reactionFormula, model);

		if (reversible && split)
		{
			setupSingleRevReaction(modelstate, forward, reactionFormula, currentTime);
		}
		else
		{
			setupSingleNonRevReaction(modelstate, forward, reactionFormula, model, currentTime);
		}

	}

	private static void setupSingleNonRevReaction(ModelState modelstate, ReactionNode reactionNode, ASTNode reactionFormula, Model model, double currentTime)
	{
		HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, modelstate.getVariableToNodeMap(), reactionNode);
		reactionNode.setForwardRate(math);
		reactionNode.computeNotEnoughEnoughMolecules();
	}

	private static void setupSingleRevReaction(ModelState modelstate, ReactionNode reactionNode, ASTNode reactionFormula, double currentTime)
	{
		ASTNode[] splitMath = HierarchicalUtilities.splitMath(reactionFormula);
		if (splitMath == null)
		{
			HierarchicalNode math = MathInterpreter.parseASTNode(reactionFormula, modelstate.getVariableToNodeMap(), reactionNode);
			reactionNode.setForwardRate(math);
			reactionNode.computeNotEnoughEnoughMolecules();
		}
		else
		{
			HierarchicalNode forwardRate = MathInterpreter.parseASTNode(splitMath[0], modelstate.getVariableToNodeMap(), reactionNode);
			HierarchicalNode reverseRate = MathInterpreter.parseASTNode(splitMath[1], modelstate.getVariableToNodeMap(), reactionNode);
			reactionNode.setForwardRate(forwardRate);
			reactionNode.setReverseRate(reverseRate);
			reactionNode.computeNotEnoughEnoughMolecules();
		}
	}

}
