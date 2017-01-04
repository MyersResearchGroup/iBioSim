package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;

import analysis.dynamicsim.hierarchical.math.VariableNode;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel.ModelType;
import analysis.dynamicsim.hierarchical.states.HierarchicalState.StateType;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

public class ParameterSetup
{
	/**
	 * puts parameter-related information into data structures
	 */
	public static void setupParameters(HierarchicalModel modelstate, ModelType type, Model model)
	{
		for (Parameter parameter : model.getListOfParameters())
		{
			if (modelstate.isDeletedBySId(parameter.getId()))
			{
				continue;
			}
			else if (ArraysSetup.checkArray(parameter))
			{
				continue;
			}
			setupSingleParameter(modelstate, parameter, type);
		}
	}

	/**
	 * sets up a single (non-local) parameter
	 * 
	 * @param parameter
	 */
	private static void setupSingleParameter(HierarchicalModel modelstate, Parameter parameter, ModelType type)
	{
	
		VariableNode node = new VariableNode(parameter.getId());
		node.createState(StateType.SPARSE);
		node.setValue(modelstate.getIndex(), parameter.getValue());
		
		if (parameter.isConstant())
		{
			modelstate.addMappingNode(parameter.getId(), node);
		}
		else
		{
			modelstate.addVariable(node);
		}

	}

	/**
	 * sets up the local parameters in a single kinetic law
	 * 
	 * @param kineticLaw
	 * @param reactionID
	 */
	public static void setupLocalParameters(HierarchicalModel modelstate, KineticLaw kineticLaw, Reaction reaction)
	{

		String reactionID = reaction.getId();

		if (kineticLaw != null)
		{
			for (LocalParameter localParameter : kineticLaw.getListOfLocalParameters())
			{

				String id = localParameter.getId();

				if (modelstate.isDeletedBySId(id))
				{
					continue;
				}
				else if (localParameter.isSetMetaId() && modelstate.isDeletedByMetaId(localParameter.getMetaId()))
				{
					continue;
				}

				String parameterID = reactionID + "_" + id;

				VariableNode node = new VariableNode(parameterID, StateType.SCALAR);
				node.setValue(0, localParameter.getValue());
				modelstate.addMappingNode(parameterID, node);

				HierarchicalUtilities.alterLocalParameter(kineticLaw.getMath(), id, parameterID);
			}
		}
	}

}
