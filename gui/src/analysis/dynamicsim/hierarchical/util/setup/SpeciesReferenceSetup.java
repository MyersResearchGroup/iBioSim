package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.SpeciesReference;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.util.math.ReactionNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesReferenceNode;

public class SpeciesReferenceSetup
{

	public static void setupSingleProduct(HierarchicalModel modelstate, ReactionNode reaction, String productID, SpeciesReference product)
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

		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode(stoichiometryValue);

		SpeciesNode species = (SpeciesNode) modelstate.getNode(productID);
		speciesReferenceNode.setSpecies(species);
		reaction.addProduct(speciesReferenceNode);

		if (product.isSetId() && product.getId().length() > 0)
		{
			speciesReferenceNode.setName(product.getId());

			if (!product.getConstant())
			{
				modelstate.addVariable(speciesReferenceNode);
				speciesReferenceNode.setIsVariableConstant(false);
			}
			else
			{
				modelstate.addConstant(speciesReferenceNode);
			}
		}
		species.addODERate(reaction, speciesReferenceNode);

	}

	public static void setupSingleReactant(HierarchicalModel modelstate, ReactionNode reaction, String reactantID, SpeciesReference reactant)
	{

		if (reactant.isSetId() && modelstate.isDeletedBySId(reactant.getId()))
		{
			return;
		}
		if (reactant.isSetMetaId() && modelstate.isDeletedByMetaId(reactant.getMetaId()))
		{
			return;
		}

		double stoichiometryValue = Double.isNaN(reactant.getStoichiometry()) ? 1 : reactant.getStoichiometry();
		SpeciesReferenceNode speciesReferenceNode = new SpeciesReferenceNode(stoichiometryValue);
		SpeciesNode species = (SpeciesNode) modelstate.getNode(reactantID);
		speciesReferenceNode.setSpecies(species);
		reaction.addReactant(speciesReferenceNode);

		if (reactant.isSetId() && reactant.getId().length() > 0)
		{
			speciesReferenceNode.setName(reactant.getId());

			if (!reactant.getConstant())
			{
				modelstate.addVariable(speciesReferenceNode);
				speciesReferenceNode.setIsVariableConstant(false);
			}
			else
			{
				modelstate.addConstant(speciesReferenceNode);
			}
		}
		species.subtractODERate(reaction, speciesReferenceNode);

	}

}
