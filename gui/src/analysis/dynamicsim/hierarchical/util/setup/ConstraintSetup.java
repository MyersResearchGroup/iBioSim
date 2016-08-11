package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;

public class ConstraintSetup
{
	/**
	 * puts constraint-related information into data structures
	 */
	public static void setupConstraints(HierarchicalModel modelstate, Model model)
	{

		int count = 0;
		for (Constraint constraint : model.getListOfConstraints())
		{
			String id = null;
			if (constraint.isSetMetaId())
			{
				id = constraint.getMetaId();
			}
			else
			{
				id = "constraint " + count++;
			}

			if (modelstate.isDeletedByMetaId(constraint.getMetaId()))
			{
				continue;
			}

			setupSingleConstraint(modelstate, id, constraint.getMath(), model);
		}

	}

	public static void setupSingleConstraint(HierarchicalModel modelstate, String id, ASTNode math, Model model)
	{

		math = HierarchicalUtilities.inlineFormula(modelstate, math, model);

		HierarchicalNode constraintNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap());

		modelstate.addConstraint(id, constraintNode);
	}
}
