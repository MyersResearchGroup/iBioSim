package analysis.dynamicsim.hierarchical.util.setup;

import java.io.IOException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.math.AbstractHierarchicalNode.Type;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.ValueNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class SpeciesSetup
{
	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private static void setupSingleSpecies(ModelState modelstate, Species species, Model model)
	{

		double initValue = 0;
		String id = species.getId();

		SpeciesNode node = new SpeciesNode(id, initValue);
		node.setBoundaryCondition(species.getBoundaryCondition());
		node.setHasOnlySubstance(species.getHasOnlySubstanceUnits());
		if (species.getConstant())
		{
			modelstate.addConstant(node);
		}
		else
		{
			modelstate.addVariable(node);
		}
		// ArraysSetup.setupArrays(modelstate, species, node);

	}

	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	public static void setupSpecies(ModelState modelstate, Model model)
	{
		for (Species species : model.getListOfSpecies())
		{
			if (modelstate.isDeletedBySId(species.getId()))
			{
				continue;
			}

			setupSingleSpecies(modelstate, species, model);
		}
	}

	public static void setupCompartmentToSpecies(ModelState modelstate, Model model)
	{

		for (Species species : model.getListOfSpecies())
		{
			if (modelstate.isDeletedBySId(species.getId()))
			{
				continue;
			}
			SpeciesNode node = (SpeciesNode) modelstate.getNode(species.getId());
			VariableNode compartment = modelstate.getNode(species.getCompartment());
			node.setCompartment(compartment);
			if (species.isSetInitialAmount())
			{
				node.setValue(species.getInitialAmount());
			}

			else if (species.isSetInitialConcentration())
			{
				HierarchicalNode initConcentration = new HierarchicalNode(Type.TIMES);
				initConcentration.addChild(new ValueNode(species.getInitialConcentration()));
				initConcentration.addChild(compartment);
				node.setInitialAssignment(initConcentration);

			}

		}
	}

}
