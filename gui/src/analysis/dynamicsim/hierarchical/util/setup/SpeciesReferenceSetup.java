package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesReferenceNode;
import analysis.dynamicsim.hierarchical.util.math.ValueNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class SpeciesReferenceSetup
{

	public static void setupSingleModifier(ModelState modelstate, ReactionNode reactionNode, String modifierID)
	{
		// VariableNode node = modelstate.getNode(modifierID);
		// if (node != null)
		// {
		// reactionNode.addModifier(node);
		// }
	}

	public static void setupSingleProduct(ModelState modelstate, ReactionNode reaction, String productID, SpeciesReference product)
	{

		if (product.isSetId() && modelstate.isDeletedBySId(product.getId()))
		{
			return;
		}
		if (product.isSetMetaId() && modelstate.isDeletedByMetaId(product.getMetaId()))
		{
			return;
		}

		double stoichiometryValue = Double.isNaN(product.getStoichiometry()) ? 1 : product.getStoichiometry();

		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode(new ValueNode(stoichiometryValue));

		SpeciesNode species = (SpeciesNode) modelstate.getNode(productID);
		speciesReferenceNode.setSpecies(species);
		reaction.addProduct(speciesReferenceNode);

		if (!product.getConstant())
		{

			if (product.getId().length() > 0)
			{
				VariableNode stoichiometry = modelstate.addVariable(product.getId(), product.getStoichiometry());
				stoichiometry.setIsVariableConstant(false);
				speciesReferenceNode.setStoichiometry(stoichiometry);
			}
		}
		species.addODERate(reaction, speciesReferenceNode);

	}

	public static void setupSingleReactant(ModelState modelstate, ReactionNode reaction, String reactantID, SpeciesReference reactant)
	{

		double stoichiometryValue = Double.isNaN(reactant.getStoichiometry()) ? 1 : reactant.getStoichiometry();
		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode(new ValueNode(stoichiometryValue));
		SpeciesNode species = (SpeciesNode) modelstate.getNode(reactantID);
		speciesReferenceNode.setSpecies(species);
		reaction.addReactant(speciesReferenceNode);

		if (!reactant.getConstant())
		{
			if (reactant.getId().length() > 0)
			{
				VariableNode stoichiometry = modelstate.addVariable(reactant.getId(), reactant.getStoichiometry());
				stoichiometry.setIsVariableConstant(false);
				speciesReferenceNode.setStoichiometry(stoichiometry);
			}
		}
		species.subtractODERate(reaction, speciesReferenceNode);

	}

}
