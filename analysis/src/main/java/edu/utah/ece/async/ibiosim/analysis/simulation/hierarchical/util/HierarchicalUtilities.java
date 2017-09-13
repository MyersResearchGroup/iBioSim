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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.RateSplitterInterpreter;


/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalUtilities
{

	public static final String		separator					= (File.separator.equals("\\")) ? "\\\\" : File.separator;

	public static final Set<String>	ibiosimFunctionDefinitions	= new HashSet<String>(Arrays.asList("uniform", "exponential", "gamma", "chisq", "lognormal", "laplace", "cauchy", "poisson", "binomial", "bernoulli", "normal"));

	public static void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList)
	{

		ASTNode child;
		long size = node.getChildCount();

		if (node.getChildCount() == 0)
		{
			nodeChildrenList.add(node);
		}

		for (int i = 0; i < size; i++)
		{
			// TODO:check this
			child = node.getChild(i);
			nodeChildrenList.add(child);
			getAllASTNodeChildren(child, nodeChildrenList);
		}
	}

	public static int getPercentage(int totalRuns, int currentRun, double currentTime, double timeLimit)
	{

		if (totalRuns == 1)
		{
			double timePerc = currentTime / timeLimit;
			return (int) (timePerc * 100);
		}
		else
		{
			double runPerc = 1.0 * currentRun / totalRuns;
			return (int) (runPerc * 100);
		}
	}
	
	public static ASTNode[] splitMath(ASTNode math)
	{
		ASTNode plus = new ASTNode(Type.PLUS);
		ASTNode minus = new ASTNode(Type.PLUS);
		ASTNode[] result = new ASTNode[] { plus, minus };
		List<ASTNode> nodes = RateSplitterInterpreter.parseASTNode(math);
		for (ASTNode node : nodes)
		{
			if (node.getType() == ASTNode.Type.MINUS)
			{
				minus.addChild(node.getChild(0));
			}
			else
			{
				plus.addChild(node);
			}
		}

		return plus.getChildCount() > 0 && minus.getChildCount() > 0 ? result : null;
	}

}
