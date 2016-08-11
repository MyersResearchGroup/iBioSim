package analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;
import analysis.dynamicsim.hierarchical.util.math.AbstractHierarchicalNode.Type;
import analysis.dynamicsim.hierarchical.util.math.HierarchicalNode;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class InitAssignmentSetup
{
	public static void setupInitialAssignments(HierarchicalModel modelstate, Model model)
	{

		for (InitialAssignment initAssignment : model.getListOfInitialAssignments())
		{
			if (initAssignment.isSetMetaId() && modelstate.isDeletedByMetaId(initAssignment.getMetaId()))
			{
				continue;
			}
			String variable = initAssignment.getVariable();
			VariableNode variableNode = modelstate.getNode(variable);

			ASTNode math = HierarchicalUtilities.inlineFormula(modelstate, initAssignment.getMath(), model);
			HierarchicalNode initAssignNode = MathInterpreter.parseASTNode(math, modelstate.getVariableToNodeMap());

			if (variableNode.isSpecies())
			{
				SpeciesNode node = (SpeciesNode) variableNode;

				if (!node.hasOnlySubstance())
				{
					HierarchicalNode amountNode = new HierarchicalNode(Type.TIMES);
					amountNode.addChild(initAssignNode);
					amountNode.addChild(node.getCompartment());
					initAssignNode = amountNode;
				}
			}

			variableNode.setInitialAssignment(initAssignNode);

		}

	}

}
