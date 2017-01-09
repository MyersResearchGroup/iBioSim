package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.math.VariableNode;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

public class CompartmentSetup
{
	public static void setupCompartments(HierarchicalModel modelstate,  StateType type, Model model)
	{
		for (Compartment compartment : model.getListOfCompartments())
		{
			if (modelstate.isDeletedBySId(compartment.getId()))
			{
				continue;
			}
			setupSingleCompartment(modelstate, compartment, type);
		}
	}

	private static void setupSingleCompartment(HierarchicalModel modelstate, Compartment compartment, StateType type)
	{

		String compartmentID = compartment.getId();
		VariableNode node = new VariableNode(compartmentID);
		node.createState(type);
		if (Double.isNaN(compartment.getSize()))
		{
			node.setValue(modelstate.getIndex(), 1);
		}
		else
		{
		  node.setValue(modelstate.getIndex(), compartment.getSize());
		}
		
		if (compartment.getConstant())
		{
			modelstate.addMappingNode(compartmentID, node);
		}
		else
		{
			modelstate.addVariable(node);
		}
	}

}
