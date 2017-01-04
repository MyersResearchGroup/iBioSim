package backend.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import backend.analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;

public class CompartmentSetup
{
	public static void setupCompartments(HierarchicalModel modelstate,  ModelType type, Model model)
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

	private static void setupSingleCompartment(HierarchicalModel modelstate, Compartment compartment, ModelType type)
	{

		String compartmentID = compartment.getId();
		VariableNode node = new VariableNode(compartmentID);
		node.createState(StateType.SPARSE);
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
