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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.Model;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.NumberNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.ReactionNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.SpeciesNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.VariableNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.AbstractHierarchicalNode.Type;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.HierarchicalUtilities;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.interpreter.MathInterpreter.InterpreterType;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.FunctionDefNode;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public final class MathInterpreter
{

  public static enum InterpreterType
  {
    RATE,
    ASSIGNMENT,
    OTHER
  };

  public static HierarchicalNode parseASTNode(ASTNode math, Map<String, VariableNode> variableToNodes, Map<String, VariableNode> dimensionNode, InterpreterType type)
  {
    return parseASTNode(math, null, variableToNodes, dimensionNode, null, type);
  }

  public static HierarchicalNode parseASTNode(ASTNode math, Map<String, VariableNode> variableToNodes, VariableNode parent, InterpreterType type)
  {
    return parseASTNode(math, null, variableToNodes, null, parent, type);
  }

  public static HierarchicalNode parseASTNode(ASTNode math, Map<String, VariableNode> variableToNodes, InterpreterType type)
  {
    return parseASTNode(math, null, variableToNodes, null, null, type);
  }

  public static HierarchicalNode parseASTNode(ASTNode math, Map<String, HierarchicalNode> args, Map<String, VariableNode> variableToNodes, Map<String, VariableNode> dimensionNodes, VariableNode parent, InterpreterType type)
  {

    HierarchicalNode node;

    switch (math.getType())
    {
    case CONSTANT_E:
      node = new HierarchicalNode(Type.CONSTANT_E);
      break;
    case CONSTANT_FALSE:
      node = new HierarchicalNode(Type.CONSTANT_FALSE);
      break;
    case CONSTANT_PI:
      node = new HierarchicalNode(Type.CONSTANT_PI);
      break;
    case CONSTRUCTOR_PIECE:
      node = new HierarchicalNode(Type.CONSTRUCTOR_PIECE);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case CONSTRUCTOR_OTHERWISE:
      node = new HierarchicalNode(Type.CONSTRUCTOR_OTHERWISE);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case CONSTANT_TRUE:
      node = new HierarchicalNode(Type.CONSTANT_TRUE);
      break;
    case DIVIDE:
      node = new HierarchicalNode(Type.DIVIDE);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case FUNCTION:
      if(HierarchicalUtilities.ibiosimFunctionDefinitions.contains(math.getName()))
      {
        node = new FunctionDefNode(math.getName());
        for (ASTNode child : math.getListOfNodes())
        {
          node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, null, type));
        }
      }
      else
      {
        FunctionDefinition func = (FunctionDefinition) math.getVariable();
        if(args == null)
        {
          args = new HashMap<String, HierarchicalNode>();
        }

        for(int i = 0; i < func.getNumArguments(); i++)
        {
          ASTNode arg = func.getArgument(i);
          ASTNode child = math.getChild(i);
          HierarchicalNode parseArg = parseASTNode(child,  new HashMap<String, HierarchicalNode>(), variableToNodes, dimensionNodes, null, type);

          args.put(arg.getName(), parseArg);
        }
        node = parseASTNode(func.getBody(), args, null, dimensionNodes, parent, type);
      }

      break;
    case FUNCTION_ABS:
      node = new HierarchicalNode(Type.FUNCTION_ABS);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCCOS:
      node = new HierarchicalNode(Type.FUNCTION_ARCCOS);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCCOSH:
      node = new HierarchicalNode(Type.FUNCTION_ARCCOSH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCCOT:
      node = new HierarchicalNode(Type.FUNCTION_ARCCOT);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCCOTH:
      node = new HierarchicalNode(Type.FUNCTION_ARCCOTH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCCSC:
      node = new HierarchicalNode(Type.FUNCTION_ARCCSC);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCCSCH:
      node = new HierarchicalNode(Type.FUNCTION_ARCCSCH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, null,parent, type));
      break;
    case FUNCTION_ARCSEC:
      node = new HierarchicalNode(Type.FUNCTION_ARCSEC);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCSECH:
      node = new HierarchicalNode(Type.FUNCTION_ARCSECH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCSIN:
      node = new HierarchicalNode(Type.FUNCTION_ARCSIN);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCSINH:
      node = new HierarchicalNode(Type.FUNCTION_ARCSINH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCTAN:
      node = new HierarchicalNode(Type.FUNCTION_ARCTAN);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_ARCTANH:
      node = new HierarchicalNode(Type.FUNCTION_ARCTANH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_CEILING:
      node = new HierarchicalNode(Type.FUNCTION_CEILING);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_COS:
      node = new HierarchicalNode(Type.FUNCTION_COS);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_COSH:
      node = new HierarchicalNode(Type.FUNCTION_COSH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_COT:
      node = new HierarchicalNode(Type.FUNCTION_COT);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_COTH:
      node = new HierarchicalNode(Type.FUNCTION_COTH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_CSC:
      node = new HierarchicalNode(Type.FUNCTION_CSC);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes,parent, type));
      break;
    case FUNCTION_CSCH:
      node = new HierarchicalNode(Type.FUNCTION_CSCH);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes,parent, type));
      break;
    case FUNCTION_DELAY:
      node = new HierarchicalNode(Type.FUNCTION_DELAY);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes,dimensionNodes, parent, type));
      break;
    case FUNCTION_EXP:
      node = new HierarchicalNode(Type.FUNCTION_EXP);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_FACTORIAL:
      node = new HierarchicalNode(Type.FUNCTION_FACTORIAL);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_FLOOR:
      node = new HierarchicalNode(Type.FUNCTION_FLOOR);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_LN:
      node = new HierarchicalNode(Type.FUNCTION_LN);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_LOG:
      node = new HierarchicalNode(Type.FUNCTION_LOG);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case FUNCTION_MAX:
      node = new HierarchicalNode(Type.FUNCTION_MAX);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case FUNCTION_MIN:
      node = new HierarchicalNode(Type.FUNCTION_MIN);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case FUNCTION_PIECEWISE:
      node = new HierarchicalNode(Type.FUNCTION_PIECEWISE);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case FUNCTION_POWER:
      node = new HierarchicalNode(Type.FUNCTION_POWER);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case	FUNCTION_QUOTIENT:
      node = new HierarchicalNode(Type.FUNCTION_QUOTIENT);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case	FUNCTION_RATE_OF:
      node = new HierarchicalNode(Type.FUNCTION_RATEOF);
      HierarchicalNode rateOf = parseASTNode(math.getChild(0), args,variableToNodes,dimensionNodes, parent, type); 
      if(rateOf.isName())
      {
        node.addChild(rateOf);
      }
      else
      {
        node.addChild(rateOf.getChild(0));
        rateOf.getChildren().set(0, node);
        node = rateOf;
      }
      break;
    case FUNCTION_REM:
      node = new HierarchicalNode(Type.FUNCTION_REM);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case FUNCTION_ROOT:
      node = new HierarchicalNode(Type.FUNCTION_ROOT);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case FUNCTION_SEC:
      node = new HierarchicalNode(Type.FUNCTION_SEC);
      node.addChild(parseASTNode(math.getChild(0), args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_SECH:
      node = new HierarchicalNode(Type.FUNCTION_SECH);
      node.addChild(parseASTNode(math.getChild(0), args,variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_SELECTOR:
      node = new HierarchicalNode(Type.FUNCTION_SELECTOR);
      node.addChild(parseASTNode(math.getChild(0),args, variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_SIN:
      node = new HierarchicalNode(Type.FUNCTION_SIN);
      node.addChild(parseASTNode(math.getChild(0), args,variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_SINH:
      node = new HierarchicalNode(Type.FUNCTION_SINH);
      node.addChild(parseASTNode(math.getChild(0), args,variableToNodes,  dimensionNodes, parent, type));
      break;
    case FUNCTION_TAN:
      node = new HierarchicalNode(Type.FUNCTION_TAN);
      node.addChild(parseASTNode(math.getChild(0), args,variableToNodes, dimensionNodes, parent, type));
      break;
    case FUNCTION_TANH:
      node = new HierarchicalNode(Type.FUNCTION_TANH);
      node.addChild(parseASTNode(math.getChild(0), args,variableToNodes, dimensionNodes, parent, type));
      break;
    case INTEGER:
      node = new HierarchicalNode(math.getInteger());
      break;
    case LAMBDA:
      node = new HierarchicalNode(Type.LAMBDA);
      node.addChild(parseASTNode(math.getChild(0), args,variableToNodes,dimensionNodes, parent, type));
      break;
    case LOGICAL_AND:
      node = new HierarchicalNode(Type.LOGICAL_AND);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case LOGICAL_IMPLIES:
      node = new HierarchicalNode(Type.LOGICAL_IMPLIES);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case LOGICAL_NOT:
      node = new HierarchicalNode(Type.LOGICAL_NOT);
      node.addChild(parseASTNode(math.getChild(0), args,variableToNodes, dimensionNodes, parent, type));
      break;
    case LOGICAL_OR:
      node = new HierarchicalNode(Type.LOGICAL_OR);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case LOGICAL_XOR:
      node = new HierarchicalNode(Type.LOGICAL_XOR);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case MINUS:
      node = new HierarchicalNode(Type.MINUS);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case NAME:
      String name = math.getName();
      if (dimensionNodes != null && dimensionNodes.containsKey(name))
      {
        node = dimensionNodes.get(name);
      }
      else if (args != null && args.containsKey(name))
      {
        node = args.get(name);
      }
      else
      {
        node = variableToNodes.get(name);

        if(node.isSpecies())
        {
          SpeciesNode species = (SpeciesNode) node;

          if(!species.hasOnlySubstance())
          {
            if(type != InterpreterType.ASSIGNMENT)
            {
              node = new HierarchicalNode(Type.DIVIDE);
              node.addChild(species);
              node.addChild(species.getCompartment());
            }
            else if(type == InterpreterType.ASSIGNMENT)
            {
              if(parent != null)
              {
                if(parent.isSpecies())
                {
                  if( parent.isSetInitialValue())
                  {
                    if(!species.hasAmountUnits() && parent.hasAmountUnits())
                    {
                      node = new HierarchicalNode(Type.TIMES);
                      node.addChild(species);
                      node.addChild(species.getCompartment());
                    }
                    else if(species.hasAmountUnits() && !parent.hasAmountUnits())
                    {
                      node = new HierarchicalNode(Type.DIVIDE);
                      node.addChild(species);
                      node.addChild(species.getCompartment());
                    }
                  }
                }
                else
                {
                  if(!species.hasOnlySubstance() && !species.isBoundaryCondition())
                  {
                      node = new HierarchicalNode(Type.DIVIDE);
                      node.addChild(species);
                      node.addChild(species.getCompartment());
                  }
                }
              }
            }
//            else if(type == InterpreterType.ASSIGNMENT && !species.hasAmountUnits())
//            {
//              if(parent != null && parent.isSetInitialValue())
//              {
//                node = new HierarchicalNode(Type.TIMES);
//                node.addChild(species);
//                node.addChild(species.getCompartment());
//              }
//            }
          }
        }
      }
      break;
    case NAME_AVOGADRO:
      node = new HierarchicalNode(Type.NAME_AVOGADRO);
      break;
    case NAME_TIME:
      node = variableToNodes.get("_time");
      break;
    case PLUS:
      node = new HierarchicalNode(Type.PLUS);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case POWER:
      node = new HierarchicalNode(Type.POWER);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case PRODUCT:
      node = new HierarchicalNode(Type.PRODUCT);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case QUALIFIER_BVAR:
      node = new HierarchicalNode(Type.QUALIFIER_BVAR);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case QUALIFIER_DEGREE:
      node = new HierarchicalNode(Type.QUALIFIER_DEGREE);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child,args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case QUALIFIER_LOGBASE:
      node = new HierarchicalNode(Type.QUALIFIER_LOGBASE);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case RATIONAL:
      node = new HierarchicalNode(1.0 * math.getNumerator() / math.getDenominator());
      break;
    case REAL:
      node = new HierarchicalNode(math.getReal());
      break;
    case REAL_E:
      double value = math.getMantissa() * Math.pow(10, math.getExponent());
      node = new HierarchicalNode(value);
      break;
    case RELATIONAL_EQ:
      node = new HierarchicalNode(Type.RELATIONAL_EQ);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case RELATIONAL_GEQ:
      node = new HierarchicalNode(Type.RELATIONAL_GEQ);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case RELATIONAL_GT:
      node = new HierarchicalNode(Type.RELATIONAL_GT);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case RELATIONAL_LEQ:
      node = new HierarchicalNode(Type.RELATIONAL_LEQ);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case RELATIONAL_LT:
      node = new HierarchicalNode(Type.RELATIONAL_LT);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case RELATIONAL_NEQ:
      node = new HierarchicalNode(Type.RELATIONAL_NEQ);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case SEMANTICS:
      //TODO:
      node = new HierarchicalNode(Type.SEMANTICS);
      break;
    case SUM:
      node = new HierarchicalNode(Type.SUM);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case TIMES:
      node = new HierarchicalNode(Type.TIMES);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args,variableToNodes, dimensionNodes, parent, type));
      }
      break;
    case UNKNOWN:
      node = new HierarchicalNode(Type.UNKNOWN);
      break;
    case VECTOR:
      node = new HierarchicalNode(Type.VECTOR);
      for (ASTNode child : math.getListOfNodes())
      {
        node.addChild(parseASTNode(child, args, variableToNodes, dimensionNodes, parent, type));
      }
      break;
    default:
      node = new HierarchicalNode(0);
    }

    return node;
  }
}
