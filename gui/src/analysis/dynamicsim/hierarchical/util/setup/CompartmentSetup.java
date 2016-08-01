package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class CompartmentSetup
{
	public static void setupCompartments(ModelState modelstate, Model model)
	{
		for (Compartment compartment : model.getListOfCompartments())
		{
			if (modelstate.isDeletedBySId(compartment.getId()))
			{
				continue;
			}
			setupSingleCompartment(modelstate, compartment);
		}

	}

	private static void setupSingleCompartment(ModelState modelstate, Compartment compartment)
	{

		String compartmentID = compartment.getId();

		VariableNode node = new VariableNode(compartmentID, compartment.getSize());

		if (Double.isNaN(compartment.getSize()))
		{
			node.setValue(1);
		}

		if (compartment.getConstant())
		{
			modelstate.addConstant(node);
		}
		else
		{
			modelstate.addVariable(node);
		}
	}

}
