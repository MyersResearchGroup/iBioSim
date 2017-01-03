package analysis.dynamicsim.hierarchical.util.setup;

import java.io.IOException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;

import analysis.dynamicsim.hierarchical.math.FunctionNode;
import analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.math.VariableNode;
import analysis.dynamicsim.hierarchical.math.AbstractHierarchicalNode.Type;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

public class SpeciesSetup
{
	/**
	 * sets up a single species
	 * 
	 * @param species
	 * @param speciesID
	 */
	private static void setupSingleSpecies(HierarchicalModel modelstate, Species species, Model model, ModelType type)
	{

		SpeciesNode node = createSpeciesNode(species, type, modelstate.getIndex());
		if (species.getConstant())
		{
			modelstate.addMappingNode(species.getId(), node);
		}

		else
		{
			modelstate.addVariable(node);
		}

	}

	/**
	 * puts species-related information into data structures
	 * 
	 * @throws IOException
	 */
	public static void setupSpecies(HierarchicalModel modelstate,  ModelType type, Model model)
	{
		for (Species species : model.getListOfSpecies())
		{
			if (modelstate.isDeletedBySId(species.getId()))
			{
				continue;
			}
			if (ArraysSetup.checkArray(species))
			{
				continue;
			}
			setupSingleSpecies(modelstate, species, model, type);
		}
	}

	public static void setupCompartmentToSpecies(HierarchicalModel modelstate, Model model)
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
				node.setValue(modelstate.getIndex(), species.getInitialAmount());
			}
			else if (species.isSetInitialConcentration())
			{
				HierarchicalNode initConcentration = new HierarchicalNode(Type.TIMES);
				initConcentration.addChild(new HierarchicalNode(species.getInitialConcentration()));
				initConcentration.addChild(compartment);
				FunctionNode functionNode = new FunctionNode(node, initConcentration);
				modelstate.addInitAssignment(functionNode);
				functionNode.setIsInitAssignment(true);
			}

		}
	}

	private static SpeciesNode createSpeciesNode(Species species, ModelType type, int index)
	{
		SpeciesNode node = new SpeciesNode(species.getId());
		node.createSpeciesTemplate(index);
		node.createState(StateType.SPARSE);
		node.setValue(index, 0);
		node.setBoundaryCondition(species.getBoundaryCondition(), index);
		node.setHasOnlySubstance(species.getHasOnlySubstanceUnits(), index);
		node.setIsVariableConstant(species.getConstant());
		
		return node;
	}

}
