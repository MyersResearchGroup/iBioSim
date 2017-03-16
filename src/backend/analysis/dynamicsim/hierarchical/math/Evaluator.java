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
package backend.analysis.dynamicsim.hierarchical.math;

import odk.lang.FastMath;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.sbml.jsbml.ASTNode;

import backend.analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import backend.analysis.dynamicsim.hierarchical.util.HierarchicalUtilities;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public final class Evaluator
{

  public static double evaluateExpressionRecursive(HierarchicalNode node, int index)
  {
    return evaluateExpressionRecursive(node, true, index);
  }

  /**
   * This is a math evaluator for HierarchicalNode objects in the context of
   * the hierarchical simulator.
   * 
   * @param checkSubstance
   *            TODO
   * @param index
   *            TODO
   * 
   */
  public static double evaluateExpressionRecursive(HierarchicalNode node, boolean checkSubstance, int index)
  {
    if (node.isBoolean())
    {
      return evaluateBoolean(node, checkSubstance, index);
    }
    else if (node.isConstant())
    {
      return evaluateConstant(node);
    }
    else if (node.isNumber())
    {
      return evaluateNumber(node, index);
    }
    else if (node.isName())
    {
      return evaluateName(node, checkSubstance, index);
    }
    else if (node.isOperator())
    {
      return evaluateOperator(node, checkSubstance, index);
    }
    else
    {
      return evaluateFunction(node, checkSubstance, index);
    }
  }

  /*
   * Evaluates boolean functions
   */
  private static double evaluateBoolean(HierarchicalNode node, boolean checkSubstance, int index)
  {
    switch (node.getType())
    {

    case CONSTANT_TRUE:
    {
      return 1.0;
    }
    case CONSTANT_FALSE:
    {
      return 0.0;
    }
    case LOGICAL_NOT:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return value < 1 ? 1 : 0;
    }
    case LOGICAL_AND:
    {
      for (int childIter = 0; childIter < node.getNumOfChild(); ++childIter)
      {
        if(evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index) == 0)
        {
          return 0;
        }
      }

      return 1;
    }
    case LOGICAL_IMPLIES:
    {
      double a = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double b = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
      return a < 1 || b > 0 ? 1 : 0;
    }
    case LOGICAL_OR:
    {
      for (int childIter = 0; childIter < node.getNumOfChild(); ++childIter)
      {
        if(evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index) == 1)
        {
          return 1;
        }
      }

      return 0;
    }

    case LOGICAL_XOR:
    {

      boolean xorResult = (node.getNumOfChild() == 0) ? false : evaluateExpressionRecursive(node.getChild(0), checkSubstance, index) > 0;

      for (int childIter = 1; childIter < node.getNumOfChild(); ++childIter)
      {
        xorResult = xorResult ^ evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index) > 0;
      }

      return xorResult ? 1 : 0;
    }

    case RELATIONAL_EQ:
    {
      double lhs, rhs;
      lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);

      for (int i = 1; i < node.getNumOfChild(); i++)
      {
        rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance, index);

        if (lhs != rhs)
        {
          return 0;
        }

        lhs = rhs;
      }

      return 1;
    }

    case RELATIONAL_NEQ:
    {
      double lhs, rhs;
      lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);

      for (int i = 1; i < node.getNumOfChild(); i++)
      {
        rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance, index);

        if (lhs == rhs)
        {
          return 0;
        }

        lhs = rhs;
      }

      return 1;
    }
    case RELATIONAL_GEQ:
    {
      double lhs, rhs;
      lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);

      for (int i = 1; i < node.getNumOfChild(); i++)
      {
        rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance, index);

        if (lhs < rhs)
        {
          return 0;
        }

        lhs = rhs;
      }

      return 1;
    }
    case RELATIONAL_LEQ:
    {
      double lhs, rhs;
      lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);

      for (int i = 1; i < node.getNumOfChild(); i++)
      {
        rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance, index);

        if (lhs > rhs)
        {
          return 0;
        }

        lhs = rhs;
      }

      return 1;
    }
    case RELATIONAL_GT:
    {
      double lhs, rhs;
      lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);

      for (int i = 1; i < node.getNumOfChild(); i++)
      {
        rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance, index);

        if (lhs <= rhs)
        {
          return 0;
        }

        lhs = rhs;
      }

      return 1;
    }
    case RELATIONAL_LT:
    {
      double lhs, rhs;
      lhs = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);

      for (int i = 1; i < node.getNumOfChild(); i++)
      {
        rhs = evaluateExpressionRecursive(node.getChild(i), checkSubstance, index);

        if (lhs >= rhs)
        {
          return 0;
        }

        lhs = rhs;
      }

      return 1;
    }

    default:
      return 0.0;

    }
  }

  /*
   * 
   */
  private static double evaluateConstant(HierarchicalNode node)
  {

    switch (node.getType())
    {

    case CONSTANT_E:
    {
      return Math.E;
    }
    case CONSTANT_PI:
    {
      return Math.PI;
    }
    default:
      return 0;
    }
  }

  private static double evaluateFunction(HierarchicalNode node, boolean checkSubstance, int index)
  {
    switch (node.getType())
    {
    case FUNCTION:
    {
      String nodeName = ((VariableNode) node).getName();

      if (nodeName.equals("uniform"))
      {
        double leftChildValue = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        double rightChildValue = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
        double lowerBound = Math.min(leftChildValue, rightChildValue);
        double upperBound = Math.max(leftChildValue, rightChildValue);
        UniformRealDistribution distrib = new UniformRealDistribution(lowerBound, upperBound);
        return distrib.sample();
      }
      else if (nodeName.equals("exponential"))
      {
        double mean = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        ExponentialDistribution distrib = new ExponentialDistribution(mean);
        return distrib.sample();
      }
      else if (nodeName.equals("gamma"))
      {
        double shape = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        double scale = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);

        GammaDistribution distrib = new GammaDistribution(shape, scale);
        return distrib.sample();
      }
      else if (nodeName.equals("chisq"))
      {
        double deg = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        ChiSquaredDistribution distrib = new ChiSquaredDistribution(deg);
        return distrib.sample();
      }
      else if (nodeName.equals("lognormal"))
      {
        double scale = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        double shape = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
        LogNormalDistribution distrib = new LogNormalDistribution(scale, shape);
        return distrib.sample();
      }
      else if (nodeName.equals("laplace"))
      {
        double mu = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        double beta = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
        LaplaceDistribution distrib = new LaplaceDistribution(mu, beta);
        return distrib.sample();
      }
      else if (nodeName.equals("cauchy"))
      {
        double median = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        double scale = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
        CauchyDistribution distrib = new CauchyDistribution(median, scale);
        return distrib.sample();
      }
      else if (nodeName.equals("poisson"))
      {
        double p = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        PoissonDistribution distrib = new PoissonDistribution(p);
        return distrib.sample();
      }
      else if (nodeName.equals("binomial"))
      {
        int p = (int) evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        double b = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
        BinomialDistribution distrib = new BinomialDistribution(p, b);
        return distrib.sample();
      }
      else if (nodeName.equals("bernoulli"))
      {
        // TODO:
        return 0;
      }
      else if (nodeName.equals("normal"))
      {
        double mean = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
        double std = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);

        NormalDistribution distrib = new NormalDistribution(mean, std);
        return distrib.sample();
      }
      else
      {
        return 0;
      }
    }

    case FUNCTION_ABS:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.abs(value);
    }
    case FUNCTION_ARCCOS:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.acos(value);
    }
    case FUNCTION_ARCSIN:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.asin(value);
    }
    case FUNCTION_ARCTAN:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.atan(value);
    }
    case FUNCTION_CEILING:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.ceil(value);
    }
    case FUNCTION_COS:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.cos(value);
    }
    case FUNCTION_COSH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.cosh(value);
    }
    case FUNCTION_EXP:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.exp(value);
    }
    case FUNCTION_FLOOR:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.floor(value);
    }
    case FUNCTION_LN:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.log(value);
    }
    case FUNCTION_LOG:
    {
      double base = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double var = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
      double value = Math.log(var) / Math.log(base);
      return value;
    }
    case FUNCTION_SIN:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.sin(value);
    }
    case FUNCTION_SINH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.sinh(value);
    }
    case FUNCTION_TAN:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.tan(value);
    }
    case FUNCTION_TANH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      return Math.tanh(value);
    }
    case FUNCTION_PIECEWISE:
    {

      for (int childIter = 0; childIter < node.getNumOfChild() - 1; childIter += 2)
      {
        boolean condition = evaluateExpressionRecursive(node.getChild(childIter + 1), checkSubstance, index) > 0;
        if (condition)
        {
          return evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index);
        }

      }
       return evaluateExpressionRecursive(node.getChild(node.getNumOfChild() - 1), checkSubstance, index);
    }
    case FUNCTION_REM:
    {
      double d0 = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double d1 = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
      int quo = (int) (d0/d1);
      return d0 - d1*quo;
    }
    case FUNCTION_ROOT:
    {
      return FastMath.pow(evaluateExpressionRecursive(node.getChild(1), checkSubstance, index), 1 / evaluateExpressionRecursive(node.getChild(0), checkSubstance, index));
    }
    case FUNCTION_QUOTIENT:
    {
      double d0 = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double d1 = evaluateExpressionRecursive(node.getChild(1), checkSubstance, index);
      return (int) (d0/d1);
    }
    case FUNCTION_SEC:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = 1 / Math.cos(value);
      return result;
    }
    case FUNCTION_SECH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = 1 / Math.cosh(value);
      return result;
    }
    case FUNCTION_FACTORIAL:
    {
      HierarchicalNode leftChild = node.getChild(0);
      int leftValue = (int) evaluateExpressionRecursive(leftChild, checkSubstance, index);
      double result = 1;
      while (leftValue > 0)
      {
        result = result * leftValue;
        leftValue--;
      }
      return result;

    }
    case FUNCTION_COT:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = 1 / Math.tan(value);
      return result;
    }
    case FUNCTION_COTH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.sinh(value) / Math.cosh(value);
      return result;
    }
    case FUNCTION_CSC:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = 1 / Math.sin(value);
      return result;
    }
    case FUNCTION_CSCH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = 1 / Math.sinh(value);
      return result;
    }
    case FUNCTION_DELAY:
    {
      // TODO:
      return 0;
    }
    case FUNCTION_ARCTANH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = 0.5 * (Math.log(value + 1) - Math.log(1 - value));
      return result;
    }
    case FUNCTION_ARCSINH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.log(value + Math.sqrt(value * value + 1));
      return result;
    }
    case FUNCTION_ARCCOSH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.log(value + Math.sqrt(value + 1) * Math.sqrt(value - 1));
      return result;
    }
    case FUNCTION_ARCCOT:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.atan(1 / value);
      return result;
    }
    case FUNCTION_ARCCOTH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = 0.5 * (Math.log(1 + 1 / value) - Math.log(1 - 1 / value));
      return result;
    }
    case FUNCTION_ARCCSC:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.asin(1 / value);
      return result;
    }

    case FUNCTION_ARCCSCH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.log(1 / value + Math.sqrt(1 / (value * value) + 1));
      return result;
    }
    case FUNCTION_ARCSEC:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.acos(1 / value);
      return result;
    }

    case FUNCTION_ARCSECH:
    {
      double value = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double result = Math.log(1 / value + Math.sqrt(1 / value + 1) * Math.sqrt(1 / value - 1));
      return result;
    }
    case FUNCTION_POWER:
    {
      HierarchicalNode leftChild = node.getChild(0);
      HierarchicalNode rightChild = node.getChild(1);
      double leftValue = evaluateExpressionRecursive(leftChild, checkSubstance, index);
      double rightValue = evaluateExpressionRecursive(rightChild, checkSubstance, index);
      double result = Math.pow(leftValue, rightValue);
      return result;

    }
    case FUNCTION_RATEOF:
    {
      HierarchicalNode value = node.getChild(0);
      
      return value.getRate();
    }
    case FUNCTION_MIN:
    {
      if(node.getNumOfChild() == 0)
      {
        return Double.NaN;
      }
      
      double min = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double tmp;
      for (int childIter = 1; childIter < node.getNumOfChild(); childIter++)
      {
        tmp = evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index);
        if(tmp < min)
        {
          min = tmp;
        }
      }
      return min;
    }
    case FUNCTION_MAX:
    {
      if(node.getNumOfChild() == 0)
      {
        return Double.NaN;
      }
      
      double max = evaluateExpressionRecursive(node.getChild(0), checkSubstance, index);
      double tmp;
      for (int childIter = 1; childIter < node.getNumOfChild(); childIter++)
      {
        tmp = evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index);
        if(tmp > max)
        {
          max = tmp;
        }
      }
      return max;
    }
    case FUNCTION_SELECTOR:
    {
      // HierarchicalNode array = node.getChild(0);
      //
      // for (int i = 1; i < node.getNumOfChild(); i++)
      // {
      // int index = (int) evaluateExpressionRecursive(node.getChild(i),
      // checkSubstance, index);
      // array = array.getChild(index);
      // }
      //
      // return ((VariableNode) array).getValue(index);
    }

    default:
      return 0.0;
    }
  }

  private static double evaluateNumber(HierarchicalNode node, int index)
  {
    return node.getValue();
  }

  private static double evaluateName(HierarchicalNode node, boolean checkSubstance, int index)
  {
    if (node.isSpecies() && checkSubstance)
    {
      SpeciesNode species = (SpeciesNode) node;

      double value = 0;

      if (!species.hasOnlySubstance())
      {
        value = species.getConcentration(index);
      }
      else
      {
        value = species.getValue(index);
      }

      return value;
    }
    else if (node.isReaction())
    {
      ReactionNode reaction = (ReactionNode) node;

      if (reaction.hasEnoughMolecules())
      {
        return reaction.getValue(index);
      }
    }
    return ((VariableNode) node).getValue(index);
  }

  private static double evaluateOperator(HierarchicalNode node, boolean checkSubstance, int index)
  {
    double result;
    switch (node.getType())
    {
    case PLUS:
    {

      result = 0.0;

      for (int childIter = 0; childIter < node.getNumOfChild(); childIter++)
      {
        result += evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index);
      }

      break;
    }

    case MINUS:
    {
      HierarchicalNode leftChild = node.getChild(0);

      result = evaluateExpressionRecursive(leftChild, checkSubstance, index);

      if (node.getNumOfChild() == 1)
      {
        result = -result;
      }
      else
      {
        for (int childIter = 1; childIter < node.getNumOfChild(); ++childIter)
        {
          result -= evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index);
        }
      }
      break;
    }

    case TIMES:
    {

      result = 1.0;

      for (int childIter = 0; childIter < node.getNumOfChild(); ++childIter)
      {
        result *= evaluateExpressionRecursive(node.getChild(childIter), checkSubstance, index);
      }

      break;
    }

    case DIVIDE:
    {
      HierarchicalNode leftChild = node.getChild(0);
      HierarchicalNode rightChild = node.getChild(1);
      double leftValue = evaluateExpressionRecursive(leftChild, checkSubstance, index);
      double rightValue = evaluateExpressionRecursive(rightChild, checkSubstance, index);
      result = leftValue / rightValue;
      break;
    }
    case POWER:
    {
      HierarchicalNode leftChild = node.getChild(0);
      HierarchicalNode rightChild = node.getChild(1);
      double leftValue = evaluateExpressionRecursive(leftChild, checkSubstance, index);
      double rightValue = evaluateExpressionRecursive(rightChild, checkSubstance, index);
      result = Math.pow(leftValue, rightValue);
      break;

    }
    default:
      result = 0.0;
    }

    return result;

  }

}
