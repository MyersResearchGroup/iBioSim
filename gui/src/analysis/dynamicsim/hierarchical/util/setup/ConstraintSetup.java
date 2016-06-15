package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

public class ConstraintSetup
{
	/**
	 * puts constraint-related information into data structures
	 */
	public static void setupConstraints(ModelState modelstate, Model model)
	{

		if (model.getNumConstraints() > 0)
		{
			modelstate.setNoConstraintsFlag(false);
		}

		for (Constraint constraint : model.getListOfConstraints())
		{
			if (constraint.isSetMetaId() && modelstate.isDeletedByMetaId(constraint.getMetaId()))
			{
				continue;
			}
			setupSingleConstraint(modelstate, constraint.getMath(), model);
		}

	}

	public static void setupSingleConstraint(ModelState modelstate, ASTNode math, Model model)
	{

		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);

		for (ASTNode constraintNode : math.getListOfNodes())
		{

			if (constraintNode.isName())
			{

				// String nodeName = constraintNode.getName();
			}
		}

	}

}
