package analysis.dynamicsim.hierarchical.util.ode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sbml.jsbml.ASTNode;

import analysis.dynamicsim.hierarchical.states.ArraysState;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalSpeciesReference;
import analysis.dynamicsim.hierarchical.util.comp.HierarchicalStringPair;

public class ODEState
{
	private Map<String, Map<String, ASTNode>>	dvariablesdtime;

	public ODEState()
	{
		this.dvariablesdtime = new HashMap<String, Map<String, ASTNode>>();
	}

	public void addReaction(ArraysState modelstate, String reaction)
	{
		String modelstateID = modelstate.getID();

		ASTNode formula = modelstate.getReactionToFormulaMap().get(reaction);
		Set<HierarchicalSpeciesReference> reactantAndStoichiometrySet = modelstate.getReactionToReactantStoichiometrySetMap().get(reaction);
		Set<HierarchicalSpeciesReference> speciesAndStoichiometrySet = modelstate.getReactionToSpeciesAndStoichiometrySetMap().get(reaction);
		Set<HierarchicalStringPair> nonConstantStoichiometrySet = modelstate.getReactionToNonconstantStoichiometriesSetMap().get(reaction);

		if (reactantAndStoichiometrySet != null)
		{
			for (HierarchicalSpeciesReference reactantAndStoichiometry : reactantAndStoichiometrySet)
			{
				String reactant = reactantAndStoichiometry.getString();
				double stoichiometry = reactantAndStoichiometry.getDoub();
				ASTNode stoichNode = new ASTNode();
				stoichNode.setValue(-1 * stoichiometry);
				dvariablesdtime.get(modelstateID).put(reactant,
						ASTNode.sum(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
			}
		}

		if (speciesAndStoichiometrySet != null)
		{
			for (HierarchicalSpeciesReference speciesAndStoichiometry : speciesAndStoichiometrySet)
			{
				String species = speciesAndStoichiometry.getString();
				double stoichiometry = speciesAndStoichiometry.getDoub();

				if (stoichiometry > 0)
				{
					ASTNode stoichNode = new ASTNode();
					stoichNode.setValue(stoichiometry);
					dvariablesdtime.get(modelstateID).put(species,
							ASTNode.sum(dvariablesdtime.get(modelstateID).get(species), ASTNode.times(formula, stoichNode)));
				}
			}
		}

		if (nonConstantStoichiometrySet != null)
		{
			for (HierarchicalStringPair reactantAndStoichiometry : nonConstantStoichiometrySet)
			{
				String reactant = reactantAndStoichiometry.string1;
				String stoichiometry = reactantAndStoichiometry.string2;
				if (stoichiometry.startsWith("-"))
				{
					ASTNode stoichNode = new ASTNode(stoichiometry.substring(1, stoichiometry.length()));
					dvariablesdtime.get(modelstateID).put(reactant,
							ASTNode.diff(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
				}
				else
				{
					ASTNode stoichNode = new ASTNode(stoichiometry);
					dvariablesdtime.get(modelstateID).put(reactant,
							ASTNode.sum(dvariablesdtime.get(modelstateID).get(reactant), ASTNode.times(formula, stoichNode)));
				}
			}
		}
	}

	public Map<String, Map<String, ASTNode>> getDvariablesdtime()
	{
		return dvariablesdtime;
	}
}
