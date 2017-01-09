package backend.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.hierarchical.math.FunctionNode;
import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.math.SpeciesNode;
import backend.analysis.dynamicsim.hierarchical.math.VariableNode;
import backend.analysis.dynamicsim.hierarchical.math.AbstractHierarchicalNode.Type;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

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

				if (!node.hasOnlySubstance(modelstate.getIndex()))
				{
					HierarchicalNode amountNode = new HierarchicalNode(Type.TIMES);
					amountNode.addChild(initAssignNode);
					amountNode.addChild(node.getCompartment());
					initAssignNode = amountNode;
				}
			}

			FunctionNode node = new FunctionNode(variableNode, initAssignNode);
			node.setIsInitAssignment(true);
			modelstate.addInitAssignment(node);

		}

	}

}
