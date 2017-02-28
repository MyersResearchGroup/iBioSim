/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package backend.analysis.dynamicsim.hierarchical.util.setup;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Model;

import backend.analysis.dynamicsim.hierarchical.math.HierarchicalNode;
import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;
import backend.analysis.dynamicsim.hierarchical.util.interpreter.MathInterpreter;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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
