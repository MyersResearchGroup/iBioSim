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
package main.java.edu.utah.ece.async.biomodel.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import odk.lang.FastMath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.Compartment;
//CompartmentType not supported in Level 3
//import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.JSBML;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.QuantityWithUnit;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
//SpeciesType not supported in Level 3
//import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.comp.Deletion;
import org.sbml.jsbml.ext.comp.Port;
import org.sbml.jsbml.ext.comp.ReplacedBy;
import org.sbml.jsbml.ext.comp.ReplacedElement;
import org.sbml.jsbml.ext.comp.SBaseRef;
import org.sbml.jsbml.ext.comp.Submodel;
import org.sbml.jsbml.ext.distrib.DistribConstants;
import org.sbml.jsbml.ext.distrib.DistribFunctionDefinitionPlugin;
import org.sbml.jsbml.ext.distrib.DistribInput;
import org.sbml.jsbml.ext.distrib.DrawFromDistribution;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;
import org.sbml.jsbml.ext.fbc.FluxObjective;
import org.sbml.jsbml.ext.fbc.Objective;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.text.parser.FormulaParserLL3;
import org.sbml.jsbml.text.parser.IFormulaParser;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.xml.XMLAttributes;
import org.sbml.jsbml.xml.XMLNamespaces;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.xml.XMLTriple;
import org.sbml.libsbml.ConversionProperties;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBasePlugin;
import org.sbml.libsbml.libsbml;
import org.sbolstandard.core2.SystemsBiologyOntology;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;
import main.java.edu.utah.ece.async.biomodel.annotation.AnnotationUtility;
import main.java.edu.utah.ece.async.biomodel.parser.BioModel;
import main.java.edu.utah.ece.async.util.GlobalConstants;
import main.java.edu.utah.ece.async.util.Message;
import main.java.edu.utah.ece.async.util.exceptions.BioSimException;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class SBMLutilities
{

	public static final SystemsBiologyOntology sbo = new SystemsBiologyOntology();
	public static final Message message = new Message();
	
	public static final Object[] getSortedListOfSBOTerms(String parent) {
		Set<String> SBOTerms = SBMLutilities.sbo.getDescendantNamesOf(parent);
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(SBOTerms);
		java.util.Collections.sort(list);
		list.add(0,"(unspecified)");
		return list.toArray();
	}

	
	
	public static int getModelSize(SBMLDocument doc) {
		int size = 0;
		Model model = doc.getModel();
		size += model.getNumParameters();
		size += model.getNumCompartments();
		size += model.getNumSpecies();
		size += model.getNumReactions();
		size += model.getNumEvents();
		size += model.getNumConstraints();
		size += model.getNumRules();
		return size;
	}
	
	public static void updatePortDimensionsIndices(Submodel submodel, Port port, SBaseRef sbaseRef, Port subPort,
			SBMLDocument subDocument,SBMLDocument document)
	{
		ArraysSBasePlugin sBasePlugin = getArraysSBasePlugin(submodel);
		ArraysSBasePlugin sBasePluginPort = getArraysSBasePlugin(port);
		ArraysSBasePlugin sBasePluginSBaseRef = getArraysSBasePlugin(sbaseRef);
		ArraysSBasePlugin sBasePluginSubPort = getArraysSBasePlugin(subPort);
		for (Dimension dim : sBasePluginSubPort.getListOfDimensions()) {
			Dimension dimClone = dim.clone();
			dimClone.setArrayDimension(dim.getArrayDimension()+sBasePlugin.getListOfDimensions().size());
			dimClone.setId("d"+dimClone.getArrayDimension());
			sBasePluginPort.addDimension(dimClone);
			Parameter p = subDocument.getModel().getParameter(dim.getSize()).clone();
			p.setId(submodel.getId()+"__"+p.getId());
			p.setMetaId(submodel.getId()+"__"+p.getMetaId());
			if (document.getModel().getParameter(p.getId())==null) {
				document.getModel().addParameter(p);
			}
			dimClone.setSize(p.getId());
			Index index = sBasePluginSBaseRef.createIndex();
			index.setArrayDimension(dim.getArrayDimension());
			index.setReferencedAttribute("comp:portRef");
			index.setMath(SBMLutilities.myParseFormula(dimClone.getId()));
		}
	}
	
	public static boolean hasDimensions(SBase sBase)
	{
		return (getArraysSBasePlugin(sBase).getDimensionCount()>0);
	}

	public static String getDimensionString(SBase sBase)
	{
		String dimStr = "";
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sBase);
		for (int j = sBasePlugin.getDimensionCount() - 1; j >= 0; j--)
		{
			org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.getDimensionByArrayDimension(j);
			dimStr += "[" + dimX.getSize() + "]";
		}
		return dimStr;
	}

	public static String getIndicesString(SBase sBase, String attribute)
	{
		String indicesStr = "";
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sBase);
		for (int i = sBasePlugin.getIndexCount() - 1; i >= 0; i--)
		{
			Index index = sBasePlugin.getIndex(i, attribute);
			if (index != null)
			{
				indicesStr += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
			}
		}
		return indicesStr;
	}

	public static String[] getDimensionIds(String prefix, int count)
	{
		String[] dimensionIds = new String[count];
		for (int i = 0; i < count; i++)
		{
			dimensionIds[i] = prefix + "d" + i;
		}
		return dimensionIds;
	}
	
	public static String[] getDimensionIds(SBase sBase) 
	{
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sBase);
		String[] dimensionIds = new String[sBasePlugin.getDimensionCount()+1];
		for(int j = sBasePlugin.getDimensionCount()-1; j>=0; j--){
			Dimension dimX = sBasePlugin.getDimensionByArrayDimension(j);
			dimensionIds[j] = dimX.getId(); 
		}
		return dimensionIds;
	}
	
	public static String[] getDimensionSizes(SBase sBase) 
	{
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sBase);
		String[] dimensionSizes = new String[sBasePlugin.getDimensionCount()+1];
		for(int j = sBasePlugin.getDimensionCount()-1; j>=0; j--){
			Dimension dimX = sBasePlugin.getDimensionByArrayDimension(j);
			dimensionSizes[j+1] = dimX.getSize();
		}
		return dimensionSizes;
	}

	public static Map<String, Double> getDimensionSize(Model model, Map<String, String> dimNSize)
	{
		Map<String, Double> dimensionSizes = new HashMap<String, Double>();
		for (String dimId : dimNSize.keySet())
		{
			String parameterId = dimNSize.get(dimId);
			Parameter param = model.getParameter(parameterId);
			if (param == null)
			{
				return null;
			}
			dimensionSizes.put(dimId, param.getValue());
		}
		return dimensionSizes;
	}
	
	public static void cloneDimensionAddIndex(SBase source,SBase target,String attribute) {
		ArraysSBasePlugin sBasePluginSource = SBMLutilities.getArraysSBasePlugin(source);
		ArraysSBasePlugin sBasePluginTarget = SBMLutilities.getArraysSBasePlugin(target);
		sBasePluginTarget.unsetListOfDimensions();
		sBasePluginTarget.unsetListOfIndices();
		for (Dimension dim : sBasePluginSource.getListOfDimensions()) {
			sBasePluginTarget.addDimension(dim.clone());
			Index index = sBasePluginTarget.createIndex();
			index.setReferencedAttribute(attribute);
			index.setArrayDimension(dim.getArrayDimension());
			index.setMath(SBMLutilities.myParseFormula(dim.getId()));
		}
	}
	
	public static void cloneDimensions(SBase source,SBase target) {
		ArraysSBasePlugin sBasePluginSource = SBMLutilities.getArraysSBasePlugin(source);
		ArraysSBasePlugin sBasePluginTarget = SBMLutilities.getArraysSBasePlugin(target);
		sBasePluginTarget.unsetListOfDimensions();
		for (Dimension dim : sBasePluginSource.getListOfDimensions()) {
			sBasePluginTarget.addDimension(dim.clone());
		}
	}
	
	public static void copyDimensionsToEdgeIndex(SBase source,SBase target,SBase edge,String attribute) {
		if (SBMLutilities.dimensionsMatch(source,target)) {
			ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(source);
			ArraysSBasePlugin sBasePluginEdge = SBMLutilities.getArraysSBasePlugin(edge);
			sBasePluginEdge.unsetListOfIndices();
			for (Dimension dim : sBasePlugin.getListOfDimensions()) {
				Index index = sBasePluginEdge.createIndex();
				index.setReferencedAttribute(attribute);
				index.setArrayDimension(dim.getArrayDimension());
				index.setMath(SBMLutilities.myParseFormula(dim.getId()));
			}
		}			
	}

	public static void addIndices(SBase sBase,String attribute,String[] indices,int offset)
	{
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sBase);
		for(int i = sBasePlugin.getIndexCount()-1; i>=0; i--){
	        Index indie = sBasePlugin.getIndex(i,attribute);
	        if(indie!=null)
	           sBasePlugin.removeIndex(indie);
		}
		if (indices!=null) {
			for(int i = 0; i<indices.length-offset; i++){
				Index indexRule = sBasePlugin.createIndex();
				indexRule.setArrayDimension(i);
				indexRule.setReferencedAttribute(attribute);
				ASTNode indexMath = SBMLutilities.myParseFormula(indices[i+offset]);
				indexRule.setMath(indexMath);
			}
		}
	}

	public static String[] getSupport(String mathExpression)
	{
		String[] support = mathExpression.split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-|>|=|<|\\^|%|&|\\||!|\\[|\\]|\\{|\\}");
		return support;
	}

	/**
	 * Find invalid reaction variables in a formula
	 */
	public static ArrayList<String> getInvalidVariables(SBMLDocument document, String[] dimensionIds, String formula, String arguments,
			boolean isFunction)
	{
		ArrayList<String> validVars = new ArrayList<String>();
		ArrayList<String> invalidVars = new ArrayList<String>();
		Model model = document.getModel();
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++)
		{
			validVars.add(model.getFunctionDefinition(i).getId());
		}
		if (isFunction)
		{
			String[] args = arguments.split(" |\\,");
			for (int i = 0; i < args.length; i++)
			{
				validVars.add(args[i]);
			}
		}
		else
		{
			for (int i = 0; i < model.getCompartmentCount(); i++)
			{
				if (document.getLevel() > 2 || model.getCompartment(i).getSpatialDimensions() != 0)
				{
					validVars.add(model.getCompartment(i).getId());
				}
			}
			for (int i = 0; i < model.getSpeciesCount(); i++)
			{
				validVars.add(model.getSpecies(i).getId());
			}
			for (int i = 0; i < model.getParameterCount(); i++)
			{
				validVars.add(model.getParameter(i).getId());
			}
			for (int i = 0; i < model.getReactionCount(); i++)
			{
				Reaction reaction = model.getReaction(i);
				validVars.add(reaction.getId());
				for (int j = 0; j < reaction.getReactantCount(); j++)
				{
					SpeciesReference reactant = reaction.getReactant(j);
					if ((reactant.isSetId()) && (!reactant.getId().equals("")))
					{
						validVars.add(reactant.getId());
					}
				}
				for (int j = 0; j < reaction.getProductCount(); j++)
				{
					SpeciesReference product = reaction.getProduct(j);
					if ((product.isSetId()) && (!product.getId().equals("")))
					{
						validVars.add(product.getId());
					}
				}
			}
			String[] kindsL3V1 = { "ampere", "avogadro", "becquerel", "candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray",
					"henry", "hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm",
					"pascal", "radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
			for (int i = 0; i < kindsL3V1.length; i++)
			{
				validVars.add(kindsL3V1[i]);
			}
			for (int i = 0; i < model.getUnitDefinitionCount(); i++)
			{
				validVars.add(model.getListOfUnitDefinitions().get(i).getId());
			}
			if (dimensionIds != null)
			{
				for (int i = 0; i < dimensionIds.length; i++)
				{
					validVars.add(dimensionIds[i]);
				}
			}
		}
		String[] splitLaw = getSupport(formula);
		for (int i = 0; i < splitLaw.length; i++)
		{
			if (splitLaw[i].equals("abs") || splitLaw[i].equals("arccos") || splitLaw[i].equals("arccosh") || splitLaw[i].equals("arcsin")
					|| splitLaw[i].equals("arcsinh") || splitLaw[i].equals("arctan") || splitLaw[i].equals("arctanh") || splitLaw[i].equals("arccot")
					|| splitLaw[i].equals("arccoth") || splitLaw[i].equals("arccsc") || splitLaw[i].equals("arccsch") || splitLaw[i].equals("arcsec")
					|| splitLaw[i].equals("arcsech") || splitLaw[i].equals("acos") || splitLaw[i].equals("acosh") || splitLaw[i].equals("asin")
					|| splitLaw[i].equals("asinh") || splitLaw[i].equals("atan") || splitLaw[i].equals("atanh") || splitLaw[i].equals("acot")
					|| splitLaw[i].equals("acoth") || splitLaw[i].equals("acsc") || splitLaw[i].equals("acsch") || splitLaw[i].equals("asec")
					|| splitLaw[i].equals("asech") || splitLaw[i].equals("cos") || splitLaw[i].equals("cosh") || splitLaw[i].equals("cot")
					|| splitLaw[i].equals("coth") || splitLaw[i].equals("csc") || splitLaw[i].equals("csch") || splitLaw[i].equals("ceil")
					|| splitLaw[i].equals("factorial") || splitLaw[i].equals("exp") || splitLaw[i].equals("floor") || splitLaw[i].equals("ln")
					|| splitLaw[i].equals("log") || splitLaw[i].equals("sqr") || splitLaw[i].equals("log10") || splitLaw[i].equals("pow")
					|| splitLaw[i].equals("sqrt") || splitLaw[i].equals("root") || splitLaw[i].equals("piecewise") || splitLaw[i].equals("sec")
					|| splitLaw[i].equals("sech") || splitLaw[i].equals("sin") || splitLaw[i].equals("sinh") || splitLaw[i].equals("tan")
					|| splitLaw[i].equals("tanh") || splitLaw[i].equals("") || splitLaw[i].equals("and") || splitLaw[i].equals("or")
					|| splitLaw[i].equals("xor") || splitLaw[i].equals("not") || splitLaw[i].equals("eq") || splitLaw[i].equals("geq")
					|| splitLaw[i].equals("leq") || splitLaw[i].equals("gt") || splitLaw[i].equals("neq") || splitLaw[i].equals("lt")
					|| splitLaw[i].equals("delay") || splitLaw[i].equals("t") || splitLaw[i].equals("time") || splitLaw[i].equals("true")
					|| splitLaw[i].equals("false") || splitLaw[i].equals("pi") || splitLaw[i].equals("exponentiale")
					|| splitLaw[i].equals("infinity") || splitLaw[i].equals("notanumber")
					|| ((document.getLevel() > 2) && (splitLaw[i].equals("avogadro"))))
			{
			}
			else
			{
				String temp = splitLaw[i];
				if (splitLaw[i].substring(splitLaw[i].length() - 1, splitLaw[i].length()).equals("e"))
				{
					temp = splitLaw[i].substring(0, splitLaw[i].length() - 1);
				}
				try
				{
					Double.parseDouble(temp);
				}
				catch (Exception e1)
				{
					if (!validVars.contains(splitLaw[i]))
					{
						if (splitLaw[i].equals("uniform"))
						{
							createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
						}
						else if (splitLaw[i].equals("normal"))
						{
							createFunction(model, "normal", "Normal distribution", "lambda(m,s,m)");
						}
						else if (splitLaw[i].equals("exponential"))
						{
							createFunction(model, "exponential", "Exponential distribution", "lambda(l,1/l)");
						}
						else if (splitLaw[i].equals("gamma"))
						{
							createFunction(model, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
						}
						else if (splitLaw[i].equals("lognormal"))
						{
							createFunction(model, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
						}
						else if (splitLaw[i].equals("chisq"))
						{
							createFunction(model, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
						}
						else if (splitLaw[i].equals("laplace"))
						{
							createFunction(model, "laplace", "Laplace distribution", "lambda(a,0)");
						}
						else if (splitLaw[i].equals("cauchy"))
						{
							createFunction(model, "cauchy", "Cauchy distribution", "lambda(a,a)");
						}
						else if (splitLaw[i].equals("rayleigh"))
						{
							createFunction(model, "rayleigh", "Rayleigh distribution", "lambda(s,s*sqrt(pi/2))");
						}
						else if (splitLaw[i].equals("poisson"))
						{
							createFunction(model, "poisson", "Poisson distribution", "lambda(mu,mu)");
						}
						else if (splitLaw[i].equals("binomial"))
						{
							createFunction(model, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
						}
						else if (splitLaw[i].equals("bernoulli"))
						{
							createFunction(model, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
						}
						else if (splitLaw[i].equals("PSt"))
						{
							createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
							createFunction(model, "PSt", "Probabilistic Steady State Property", "lambda(x,uniform(0,1))");
						}
						else if (splitLaw[i].equals("St"))
						{
							createFunction(model, "St", "Steady State Property", "lambda(x,not(not(x)))");
						}
						else if (splitLaw[i].equals("PG"))
						{
							createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
							createFunction(model, "PG", "Probabilistic Globally Property", "lambda(t,x,uniform(0,1))");
						}
						else if (splitLaw[i].equals("G"))
						{
							createFunction(model, "G", "Globally Property", "lambda(t,x,or(not(t),x))");
						}
						else if (splitLaw[i].equals("PF"))
						{
							createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
							createFunction(model, "PF", "Probabilistic Eventually Property", "lambda(t,x,uniform(0,1))");
						}
						else if (splitLaw[i].equals("F"))
						{
							createFunction(model, "F", "Eventually Property", "lambda(t,x,or(not(t),not(x)))");
						}
						else if (splitLaw[i].equals("PU"))
						{
							createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
							createFunction(model, "PU", "Probabilistic Until Property", "lambda(t,x,y,uniform(0,1))");
						}
						else if (splitLaw[i].equals("U"))
						{
							createFunction(model, "G", "Globally Property", "lambda(t,x,or(not(t),x))");
							createFunction(model, "F", "Eventually Property", "lambda(t,x,or(not(t),not(x)))");
							createFunction(model, "U", "Until Property", "lambda(t,x,y,and(G(t,x),F(t,y)))");
						}
						else if (splitLaw[i].equals("rate"))
						{
							createFunction(model, "rate", "Rate", "lambda(a,a)");
						}
						else if (splitLaw[i].equals("BIT"))
						{
							createFunction(model, "BIT", "bit selection", "lambda(a,b,a*b)");
						}
						else if (splitLaw[i].equals("BITAND"))
						{
							createFunction(model, "BITAND", "Bitwise AND", "lambda(a,b,a*b)");
						}
						else if (splitLaw[i].equals("BITOR"))
						{
							createFunction(model, "BITOR", "Bitwise OR", "lambda(a,b,a*b)");
						}
						else if (splitLaw[i].equals("BITNOT"))
						{
							createFunction(model, "BITNOT", "Bitwise NOT", "lambda(a,b,a*b)");
						}
						else if (splitLaw[i].equals("BITXOR"))
						{
							createFunction(model, "BITXOR", "Bitwise XOR", "lambda(a,b,a*b)");
						}
						else if (splitLaw[i].equals("mod"))
						{
							createFunction(model, "mod", "Modular", "lambda(a,b,a-floor(a/b)*b)");
						}
						else if (splitLaw[i].equals("neighborQuantityLeft"))
						{
							createFunction(model, "neighborQuantityLeft", "neighborQuantityLeft", "lambda(a,0)");
							createFunction(model, "neighborQuantityLeftFull", "neighborQuantityLeftFull", "lambda(a,b,c,0)");
						}
						else if (splitLaw[i].equals("neighborQuantityRight"))
						{
							createFunction(model, "neighborQuantityRight", "neighborQuantityRight", "lambda(a,0)");
							createFunction(model, "neighborQuantityRightFull", "neighborQuantityRightFull", "lambda(a,b,c,0)");
						}
						else if (splitLaw[i].equals("neighborQuantityAbove"))
						{
							createFunction(model, "neighborQuantityAbove", "neighborQuantityAbove", "lambda(a,0)");
							createFunction(model, "neighborQuantityAboveFull", "neighborQuantityAboveFull", "lambda(a,b,c,0)");
						}
						else if (splitLaw[i].equals("neighborQuantityBelow"))
						{
							createFunction(model, "neighborQuantityBelow", "neighborQuantityBelow", "lambda(a,0)");
							createFunction(model, "neighborQuantityBelowFull", "neighborQuantityBelowFull", "lambda(a,b,c,0)");
						}
						else
						{
							invalidVars.add(splitLaw[i]);
						}
						if (splitLaw[i].contains("neighborQuantity"))
						{
							createFunction(model, "getCompartmentLocationX", "getCompartmentLocationX", "lambda(a,0)");
							createFunction(model, "getCompartmentLocationY", "getCompartmentLocationY", "lambda(a,0)");
						}
					}
				}
			}
		}
		return invalidVars;
	}

	public static void pruneUnusedSpecialFunctions(SBMLDocument document)
	{
		if (document.getModel().getFunctionDefinition("uniform") != null)
		{
			if (!functionInUse(document, "uniform", false, false, true))
			{
				document.getModel().removeFunctionDefinition("uniform");
			}
		}
		if (document.getModel().getFunctionDefinition("normal") != null)
		{
			if (!functionInUse(document, "normal", false, false, true))
			{
				document.getModel().removeFunctionDefinition("normal");
			}
		}
		if (document.getModel().getFunctionDefinition("exponential") != null)
		{
			if (!functionInUse(document, "exponential", false, false, true))
			{
				document.getModel().removeFunctionDefinition("exponential");
			}
		}
		if (document.getModel().getFunctionDefinition("gamma") != null)
		{
			if (!functionInUse(document, "gamma", false, false, true))
			{
				document.getModel().removeFunctionDefinition("gamma");
			}
		}
		if (document.getModel().getFunctionDefinition("lognormal") != null)
		{
			if (!functionInUse(document, "lognormal", false, false, true))
			{
				document.getModel().removeFunctionDefinition("lognormal");
			}
		}
		if (document.getModel().getFunctionDefinition("chisq") != null)
		{
			if (!functionInUse(document, "chisq", false, false, true))
			{
				document.getModel().removeFunctionDefinition("chisq");
			}
		}
		if (document.getModel().getFunctionDefinition("laplace") != null)
		{
			if (!functionInUse(document, "laplace", false, false, true))
			{
				document.getModel().removeFunctionDefinition("laplace");
			}
		}
		if (document.getModel().getFunctionDefinition("cauchy") != null)
		{
			if (!functionInUse(document, "cauchy", false, false, true))
			{
				document.getModel().removeFunctionDefinition("cauchy");
			}
		}
		if (document.getModel().getFunctionDefinition("rayleigh") != null)
		{
			if (!functionInUse(document, "rayleigh", false, false, true))
			{
				document.getModel().removeFunctionDefinition("rayleigh");
			}
		}
		if (document.getModel().getFunctionDefinition("poisson") != null)
		{
			if (!functionInUse(document, "poisson", false, false, true))
			{
				document.getModel().removeFunctionDefinition("poisson");
			}
		}
		if (document.getModel().getFunctionDefinition("binomial") != null)
		{
			if (!functionInUse(document, "binomial", false, false, true))
			{
				document.getModel().removeFunctionDefinition("binomial");
			}
		}
		if (document.getModel().getFunctionDefinition("bernoulli") != null)
		{
			if (!functionInUse(document, "bernoulli", false, false, true))
			{
				document.getModel().removeFunctionDefinition("bernoulli");
			}
		}
		if (document.getModel().getFunctionDefinition("St") != null)
		{
			if (!functionInUse(document, "St", false, false, true))
			{
				document.getModel().removeFunctionDefinition("St");
			}
		}
		if (document.getModel().getFunctionDefinition("PSt") != null)
		{
			if (!functionInUse(document, "PSt", false, false, true))
			{
				document.getModel().removeFunctionDefinition("PSt");
			}
		}
		if (document.getModel().getFunctionDefinition("PG") != null)
		{
			if (!functionInUse(document, "PG", false, false, true))
			{
				document.getModel().removeFunctionDefinition("PG");
			}
		}
		if (document.getModel().getFunctionDefinition("PF") != null)
		{
			if (!functionInUse(document, "PF", false, false, true))
			{
				document.getModel().removeFunctionDefinition("PF");
			}
		}
		if (document.getModel().getFunctionDefinition("PU") != null)
		{
			if (!functionInUse(document, "PU", false, false, true))
			{
				document.getModel().removeFunctionDefinition("PU");
			}
		}
		if (document.getModel().getFunctionDefinition("G") != null)
		{
			if (!functionInUse(document, "G", false, false, true))
			{
				document.getModel().removeFunctionDefinition("G");
			}
		}
		if (document.getModel().getFunctionDefinition("F") != null)
		{
			if (!functionInUse(document, "F", false, false, true))
			{
				document.getModel().removeFunctionDefinition("F");
			}
		}
		if (document.getModel().getFunctionDefinition("U") != null)
		{
			if (!functionInUse(document, "U", false, false, true))
			{
				document.getModel().removeFunctionDefinition("U");
			}
		}
		if (document.getModel().getFunctionDefinition("rate") != null)
		{
			if (!functionInUse(document, "rate", false, false, true))
			{
				document.getModel().removeFunctionDefinition("rate");
			}
		}
		if (document.getModel().getFunctionDefinition("mod") != null)
		{
			if (!functionInUse(document, "mod", false, false, true))
			{
				document.getModel().removeFunctionDefinition("mod");
			}
		}
		if (document.getModel().getFunctionDefinition("BIT") != null)
		{
			if (!functionInUse(document, "BIT", false, false, true))
			{
				document.getModel().removeFunctionDefinition("BIT");
			}
		}
		if (document.getModel().getFunctionDefinition("BITOR") != null)
		{
			if (!functionInUse(document, "BITOR", false, false, true))
			{
				document.getModel().removeFunctionDefinition("BITOR");
			}
		}
		if (document.getModel().getFunctionDefinition("BITXOR") != null)
		{
			if (!functionInUse(document, "BITXOR", false, false, true))
			{
				document.getModel().removeFunctionDefinition("BITXOR");
			}
		}
		if (document.getModel().getFunctionDefinition("BITNOT") != null)
		{
			if (!functionInUse(document, "BITNOT", false, false, true))
			{
				document.getModel().removeFunctionDefinition("BITNOT");
			}
		}
		if (document.getModel().getFunctionDefinition("BITAND") != null)
		{
			if (!functionInUse(document, "BITAND", false, false, true))
			{
				document.getModel().removeFunctionDefinition("BITAND");
			}
		}
		// if (document.getModel().getFunctionDefinition("neighborQuantityLeft")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityLeft", false, false,
		// true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityLeft");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("neighborQuantityRight")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityRight", false, false,
		// true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityRight");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("neighborQuantityAbove")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityAbove", false, false,
		// true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityAbove");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("neighborQuantityBelow")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityBelow", false, false,
		// true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityBelow");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("neighborQuantityLeftFull")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityLeftFull", false,
		// false, true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityLeftFull");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("neighborQuantityRightFull")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityRightFull", false,
		// false, true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityRightFull");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("neighborQuantityAboveFull")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityAboveFull", false,
		// false, true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityAboveFull");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("neighborQuantityBelowFull")
		// != null) {
		// if (!functionInUse(document, "neighborQuantityBelowFull", false,
		// false, true)) {
		// document.getModel().removeFunctionDefinition("neighborQuantityBelowFull");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("getCompartmentLocationX")
		// != null) {
		// if (!functionInUse(document, "getCompartmentLocationX", false, false,
		// true)) {
		// document.getModel().removeFunctionDefinition("getCompartmentLocationX");
		// }
		// }
		// if
		// (document.getModel().getFunctionDefinition("getCompartmentLocationY")
		// != null) {
		// if (!functionInUse(document, "getCompartmentLocationY", false, false,
		// true)) {
		// document.getModel().removeFunctionDefinition("getCompartmentLocationY");
		// }
		// }

	}

	/**
	 * Convert ASTNodes into a string
	 */
	public static String myFormulaToString(ASTNode mathFormula)
	{
		if (mathFormula == null)
		{
			return "";
		}
		setTimeToT(mathFormula);
		String formula;
		Preferences.userRoot();
		formula = JSBML.formulaToString(mathFormula);
		// FormulaCompilerLibSBML compiler = new FormulaCompilerLibSBML();
		// formula = ASTNode.formulaToString(mathFormula, compiler);
		// formula = myFormulaToStringInfix(mathFormula);
		formula = formula.replaceAll("arccot", "acot");
		formula = formula.replaceAll("arccoth", "acoth");
		formula = formula.replaceAll("arccsc", "acsc");
		formula = formula.replaceAll("arccsch", "acsch");
		formula = formula.replaceAll("arcsec", "asec");
		formula = formula.replaceAll("arcsech", "asech");
		formula = formula.replaceAll("arccosh", "acosh");
		formula = formula.replaceAll("arcsinh", "asinh");
		formula = formula.replaceAll("arctanh", "atanh");
		String newformula = formula.replaceFirst("00e", "0e");
		while (!(newformula.equals(formula)))
		{
			formula = newformula;
			newformula = formula.replaceFirst("0e\\+", "e+");
			newformula = newformula.replaceFirst("0e-", "e-");
		}
		formula = formula.replaceFirst("\\.e\\+", ".0e+");
		formula = formula.replaceFirst("\\.e-", ".0e-");
		return formula;
	}

	/**
	 * Recursive function to change time variable to t
	 */
	public static void setTimeToT(ASTNode node)
	{
		if (node == null)
		{
			return;
		}
		if (node.getType() == ASTNode.Type.NAME_TIME)
		{
			if (node.getName()==null || !node.getName().equals("t") || !node.getName().equals("time"))
			{
				node.setName("t");
			}
		}
		else if (node.getType() == ASTNode.Type.NAME_AVOGADRO)
		{
			node.setName("avogadro");
		}
		for (int c = 0; c < node.getChildCount(); c++)
		{
			setTimeToT(node.getChild(c));
		}
	}

	/**
	 * Convert String into ASTNodes
	 */
	public static ASTNode myParseFormula(String formula)
	{
		ASTNode mathFormula = null;
		Preferences.userRoot();
		try
		{
			IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
			mathFormula = ASTNode.parseFormula(formula, parser);
		}
		catch (ParseException e)
		{
			return null;
		}
		catch (Exception e)
		{
			return null;
		}
		if (mathFormula == null)
		{
			return null;
		}
		setTimeAndTrigVar(mathFormula);
		return mathFormula;
	}

	/**
	 * Recursive function to set time and trig functions
	 */
	public static void setTimeAndTrigVar(ASTNode node)
	{
		if (node.getType() == ASTNode.Type.NAME)
		{
			if (node.getName().equals("t"))
			{
				node.setType(ASTNode.Type.NAME_TIME);
				node.setName("t");
			}
			else if (node.getName().equals("time"))
			{
				node.setType(ASTNode.Type.NAME_TIME);
			}
			else if (node.getName().equals("avogadro"))
			{
				node.setType(ASTNode.Type.NAME_AVOGADRO);
			}
		}
		if (node.getType() == ASTNode.Type.FUNCTION)
		{
			if (node.getName().equals("acot"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCCOT);
			}
			else if (node.getName().equals("acoth"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCCOTH);
			}
			else if (node.getName().equals("acsc"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCCSC);
			}
			else if (node.getName().equals("acsch"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCCSCH);
			}
			else if (node.getName().equals("asec"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCSEC);
			}
			else if (node.getName().equals("asech"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCSECH);
			}
			else if (node.getName().equals("acosh"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCCOSH);
			}
			else if (node.getName().equals("asinh"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCSINH);
			}
			else if (node.getName().equals("atanh"))
			{
				node.setType(ASTNode.Type.FUNCTION_ARCTANH);
			}
		}

		for (int c = 0; c < node.getChildCount(); c++)
		{
			setTimeAndTrigVar(node.getChild(c));
		}
	}

	public static String fixSId(String sId) {
		sId = sId.replaceAll("[^a-zA-Z0-9_]", "_");
		sId = sId.replace(" ", "_");
		if (Character.isDigit(sId.charAt(0))) {
			sId = "_" + sId;
		}
		return sId;
	}

	public static void fillBlankMetaIDs(SBMLDocument document)
	{
		int metaIDIndex = 1;
		Model model = document.getModel();
		setDefaultMetaID(document, model, metaIDIndex);
		for (int i = 0; i < model.getParameterCount(); i++)
		{
			Parameter p = model.getParameter(i);
			if (!p.isSetMetaId() || p.getMetaId().equals(""))
			{
				metaIDIndex = setDefaultMetaID(document, p, metaIDIndex);
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++)
		{
			Species s = model.getSpecies(i);
			if (!s.isSetMetaId() || s.getMetaId().equals(""))
			{
				metaIDIndex = setDefaultMetaID(document, s, metaIDIndex);
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++)
		{
			Reaction r = model.getReaction(i);
			if (!r.isSetMetaId() || r.getMetaId().equals(""))
			{
				metaIDIndex = setDefaultMetaID(document, r, metaIDIndex);
			}
		}
		for (int i = 0; i < model.getRuleCount(); i++)
		{
			Rule r = model.getRule(i);
			if (!r.isSetMetaId() || r.getMetaId().equals(""))
			{
				metaIDIndex = setDefaultMetaID(document, r, metaIDIndex);
			} else {
				r.setMetaId(fixSId(r.getMetaId()));
			}
		}
		for (int i = 0; i < model.getEventCount(); i++)
		{
			Event e = model.getEvent(i);
			if (!e.isSetMetaId() || e.getMetaId().equals(""))
			{
				metaIDIndex = setDefaultMetaID(document, e, metaIDIndex);
			}
		}
		for (int i = 0; i < model.getConstraintCount(); i++)
		{
			Constraint c = model.getConstraint(i);
			if (!c.isSetMetaId() || c.getMetaId().equals(""))
			{
				metaIDIndex = setDefaultMetaID(document, c, metaIDIndex);
			} else {
				c.setMetaId(fixSId(c.getMetaId()));
			}
		}
		CompModelPlugin compModel = (CompModelPlugin) document.getModel().getExtension(CompConstants.namespaceURI);
		if (compModel != null && compModel.isSetListOfSubmodels())
		{
			for (int i = 0; i < compModel.getListOfSubmodels().size(); i++)
			{
				Submodel s = compModel.getListOfSubmodels().get(i);
				if (s.isSetMetaId() || s.getMetaId().equals(""))
				{
					metaIDIndex = setDefaultMetaID(document, s, metaIDIndex);
				}
			}
		}
	}

	public static int setDefaultMetaID(SBMLDocument document, SBase sbmlObject, int metaIDIndex)
	{
		CompSBMLDocumentPlugin compDocument = (CompSBMLDocumentPlugin) document.getExtension(CompConstants.namespaceURI);
		String metaID = "iBioSim" + metaIDIndex;
		while (getElementByMetaId(document, metaID) != null || (compDocument != null && getElementByMetaId(compDocument, metaID) != null))
		{
			metaIDIndex++;
			metaID = "iBioSim" + metaIDIndex;
		}
		setMetaId(sbmlObject, metaID);
		metaIDIndex++;
		return metaIDIndex;
	}

	public static ArrayList<String> CreateListOfUsedIDs(SBMLDocument document)
	{
		ArrayList<String> usedIDs = new ArrayList<String>();
		if (document == null)
		{
			return usedIDs;
		}
		Model model = document.getModel();
		if (model.isSetId())
		{
			usedIDs.add(model.getId());
		}
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++)
		{
			usedIDs.add(model.getFunctionDefinition(i).getId());
		}
		usedIDs.add("uniform");
		usedIDs.add("normal");
		usedIDs.add("exponential");
		usedIDs.add("gamma");
		usedIDs.add("lognormal");
		usedIDs.add("chisq");
		usedIDs.add("laplace");
		usedIDs.add("cauchy");
		usedIDs.add("poisson");
		usedIDs.add("binomial");
		usedIDs.add("bernoulli");
		usedIDs.add("St");
		usedIDs.add("PSt");
		usedIDs.add("PG");
		usedIDs.add("PF");
		usedIDs.add("PU");
		usedIDs.add("G");
		usedIDs.add("F");
		usedIDs.add("U");
		for (int i = 0; i < model.getUnitDefinitionCount(); i++)
		{
			usedIDs.add(model.getUnitDefinition(i).getId());
		}
		// CompartmentType and SpeciesType not supported in Level 3
		// ids = model.getListOfCompartmentTypes();
		// for (int i = 0; i < model.getNumCompartmentTypes(); i++) {
		// usedIDs.add(((CompartmentType) ids.get(i)).getId());
		// }
		// ids = model.getListOfSpeciesTypes();
		// for (int i = 0; i < model.getSpeciesTypeCount(); i++) {
		// usedIDs.add(((SpeciesType) ids.get(i)).getId());
		// }
		for (int i = 0; i < model.getCompartmentCount(); i++)
		{
			usedIDs.add(model.getCompartment(i).getId());
		}
		for (int i = 0; i < model.getParameterCount(); i++)
		{
			usedIDs.add(model.getParameter(i).getId());
		}
		for (int i = 0; i < model.getReactionCount(); i++)
		{
			Reaction reaction = model.getReaction(i);
			usedIDs.add(reaction.getId());
			for (int j = 0; j < reaction.getReactantCount(); j++)
			{
				SpeciesReference reactant = reaction.getReactant(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals("")))
				{
					usedIDs.add(reactant.getId());
				}
			}
			for (int j = 0; j < reaction.getProductCount(); j++)
			{
				SpeciesReference product = reaction.getProduct(j);
				if ((product.isSetId()) && (!product.getId().equals("")))
				{
					usedIDs.add(product.getId());
				}
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++)
		{
			usedIDs.add(model.getSpecies(i).getId());
		}
		for (int i = 0; i < model.getConstraintCount(); i++)
		{
			Constraint constraint = model.getConstraint(i);
			if (constraint.isSetMetaId())
			{
				usedIDs.add(constraint.getMetaId());
			}
		}
		for (int i = 0; i < model.getEventCount(); i++)
		{
			Event event = model.getEvent(i);
			if (event.isSetId())
			{
				usedIDs.add(event.getId());
			}
		}
		return usedIDs;
	}

	/**
	 * Check for cycles in initialAssignments and assignmentRules
	 */
	public static boolean checkCycles(SBMLDocument document)
	{
		Model model = document.getModel();
		String[] rateLaws = new String[model.getReactionCount()];
		for (int i = 0; i < model.getReactionCount(); i++)
		{
			Reaction reaction = model.getReaction(i);
			if (reaction.getKineticLaw() == null || reaction.getKineticLaw().getMath() == null)
			{
				rateLaws[i] = reaction.getId() + " = 0.0";
			}
			else
			{
				rateLaws[i] = reaction.getId() + " = " + myFormulaToString(reaction.getKineticLaw().getMath());
			}
		}
		String[] initRules = new String[model.getInitialAssignmentCount()];
		for (int i = 0; i < model.getInitialAssignmentCount(); i++)
		{
			InitialAssignment init = model.getInitialAssignment(i);
			initRules[i] = init.getVariable() + " = " + myFormulaToString(init.getMath());
		}
		String[] rules = new String[model.getRuleCount()];
		for (int i = 0; i < model.getRuleCount(); i++)
		{
			Rule rule = model.getRule(i);
			if (rule.isAlgebraic())
			{
				rules[i] = "0 = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else if (rule.isAssignment())
			{
				rules[i] = getVariable(rule) + " = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else
			{
				rules[i] = "d( " + getVariable(rule) + " )/dt = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
		}
		String[] result = new String[rules.length + initRules.length + rateLaws.length];
		int j = 0;
		boolean[] used = new boolean[rules.length + initRules.length + rateLaws.length];
		for (int i = 0; i < rules.length + initRules.length + rateLaws.length; i++)
		{
			used[i] = false;
		}
		for (int i = 0; i < rules.length; i++)
		{
			if (rules[i].split(" ")[0].equals("0"))
			{
				result[j] = rules[i];
				used[i] = true;
				j++;
			}
		}
		boolean progress;
		do
		{
			progress = false;
			for (int i = 0; i < rules.length + initRules.length + rateLaws.length; i++)
			{
				String[] rule;
				if (i < rules.length)
				{
					if (used[i] || (rules[i].split(" ")[0].equals("0")) || (rules[i].split(" ")[0].equals("d(")))
					{
						continue;
					}
					rule = rules[i].split(" ");
				}
				else if (i < rules.length + initRules.length)
				{
					if (used[i])
					{
						continue;
					}
					rule = initRules[i - rules.length].split(" ");
				}
				else
				{
					if (used[i])
					{
						continue;
					}
					rule = rateLaws[i - (rules.length + initRules.length)].split(" ");
				}
				boolean insert = true;
				for (int k = 1; k < rule.length; k++)
				{
					for (int l = 0; l < rules.length + initRules.length + rateLaws.length; l++)
					{
						String rule2;
						if (l < rules.length)
						{
							if (used[l] || (rules[l].split(" ")[0].equals("0")) || (rules[l].split(" ")[0].equals("d(")))
							{
								continue;
							}
							rule2 = rules[l].split(" ")[0];
						}
						else if (l < rules.length + initRules.length)
						{
							if (used[l])
							{
								continue;
							}
							rule2 = initRules[l - rules.length].split(" ")[0];
						}
						else
						{
							if (used[l])
							{
								continue;
							}
							rule2 = rateLaws[l - (rules.length + initRules.length)].split(" ")[0];
						}
						if (rule[k].equals(rule2))
						{
							insert = false;
							break;
						}
					}
					if (!insert)
					{
						break;
					}
				}
				if (insert)
				{
					if (i < rules.length)
					{
						result[j] = rules[i];
					}
					else if (i < rules.length + initRules.length)
					{
						result[j] = initRules[i - rules.length];
					}
					else
					{
						result[j] = rateLaws[i - (rules.length + initRules.length)];
					}
					j++;
					progress = true;
					used[i] = true;
				}
			}
		}
		while ((progress) && (j < rules.length + initRules.length + rateLaws.length));
		for (int i = 0; i < rules.length; i++)
		{
			if (rules[i].split(" ")[0].equals("d("))
			{
				result[j] = rules[i];
				j++;
			}
		}
		if (j != rules.length + initRules.length + rateLaws.length)
		{
			return true;
		}
		return false;
	}

	/**
	 * Checks if species is a reactant in a non-degradation reaction
	 */
	public static boolean usedInNonDegradationReaction(SBMLDocument document, String id)
	{
		for (int i = 0; i < document.getModel().getReactionCount(); i++)
		{
			for (int j = 0; j < document.getModel().getReaction(i).getReactantCount(); j++)
			{
				if (document.getModel().getReaction(i).getReactant(j).getSpecies().equals(id)
						&& (document.getModel().getReaction(i).getProductCount() > 0 || document.getModel().getReaction(i).getReactantCount() > 1))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Update variable in math formula using String
	 */
	public static String updateFormulaVar(String s, String origVar, String newVar)
	{
		String result = s.replaceAll("\\b" + origVar + "\\b", newVar);
		return result.trim();
	}

	/**
	 * Update variable in math formula using ASTNode
	 */
	public static ASTNode updateMathVar(ASTNode math, String origVar, String newVar)
	{
		String s = updateFormulaVar(myFormulaToString(math), origVar, newVar);
		return myParseFormula(s);
	}

	public static String convertMath2PrismProperty(ASTNode math) {
		if (math.getType() == ASTNode.Type.CONSTANT_E)
		{
			return "exponentiale";
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_FALSE)
		{
			return "false";
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_PI)
		{
			return "pi";
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_TRUE)
		{
			return "true";
		}
		else if (math.getType() == ASTNode.Type.DIVIDE)
		{
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "(" + leftStr + " / " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION)
		{
			String result = "";
			if (math.getName().equals("St")) {
				result = "S=?[";
				if (math.getChildCount()==1) {
					result += convertMath2PrismProperty(math.getChild(0));
				}
				result += "]";
			} else if ((math.getName().equals("PF"))||(math.getName().equals("PG"))||(math.getName().equals("PU"))||
					((math.getName().equals("F"))||(math.getName().equals("G"))||(math.getName().equals("U")))) {
				if (math.getName().startsWith("P")) {
					result = "[";
				} else {
					result = "P=? [";
				}
				if (math.getChildCount()>=2) {
					if (math.getChild(0).isRelational() && math.getChild(0).getChildCount()==2) { 
						String bound = convertMath2PrismProperty(math.getChild(0).getChild(1));
						String first = convertMath2PrismProperty(math.getChild(1));
						String relation = "";
						if (math.getChild(0).getType() == ASTNode.Type.RELATIONAL_LEQ) 
							relation = "<=";
						else if (math.getChild(0).getType() == ASTNode.Type.RELATIONAL_GEQ) 
							relation = ">=";
						else if (math.getChild(0).getType() == ASTNode.Type.RELATIONAL_GT) 
							relation = ">";
						else if (math.getChild(0).getType() == ASTNode.Type.RELATIONAL_LT) 
							relation = "<";
						else if (math.getChild(0).getType() == ASTNode.Type.RELATIONAL_EQ) 
							relation = "=";
						else if (math.getChild(0).getType() == ASTNode.Type.RELATIONAL_NEQ) 
							relation = "!=";
						if (math.getName().equals("PF")) {
							result += "F" + relation + bound + " " + first;
						} else if (math.getName().equals("PG")) {
							result += "G" + relation + bound + " " + first;
						} else {
							result += first + " " + "U" + relation + bound + " " +
									convertMath2PrismProperty(math.getChild(2));
						}
					}
				}
				result += "]";
			} else {
				result = math.getName() + "(";
				for (int i = 0; i < math.getChildCount(); i++)
				{
					String child = convertMath2PrismProperty(math.getChild(i));
					result += child;
					if (i + 1 < math.getChildCount())
					{
						result += ",";
					}
				}
				result += ")";
			}
			return result;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ABS)
		{
			return "abs(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOS)
		{
			return "acos(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOSH)
		{
			return "acosh(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOT)
		{
			return "acot(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOTH)
		{
			return "acoth(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSC)
		{
			return "acsc(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSCH)
		{
			return "acsch(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSEC)
		{
			return "asec(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSECH)
		{
			return "asech(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSIN)
		{
			return "asin(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSINH)
		{
			return "asinh(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCTAN)
		{
			return "atan(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCTANH)
		{
			return "atanh(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CEILING)
		{
			return "ceil(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COS)
		{
			return "cos(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COSH)
		{
			return "cosh(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COT)
		{
			return "cot(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COTH)
		{
			return "coth(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CSC)
		{
			return "csc(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CSCH)
		{
			return "csch(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_DELAY)
		{
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "delay(" + leftStr + " , " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_EXP)
		{
			return "exp(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_FACTORIAL)
		{
			return "factorial(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_FLOOR)
		{
			return "floor(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_LN)
		{
			return "ln(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_LOG)
		{
			String result = "log(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = convertMath2PrismProperty(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE)
		{
			String result = "piecewise(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = convertMath2PrismProperty(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_POWER)
		{
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "pow(" + leftStr + " , " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ROOT)
		{
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "root(" + leftStr + " , " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SEC)
		{
			return "sec(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SECH)
		{
			return "sech(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SIN)
		{
			return "sin(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SINH)
		{
			return "sinh(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_TAN)
		{
			return "tan(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_TANH)
		{
			return "tanh(" + convertMath2PrismProperty(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.INTEGER)
		{
			if (math.hasUnits())
			{
				return "" + math.getInteger() + " " + math.getUnits();
			}
			return "" + math.getInteger();
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_AND)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = convertMath2PrismProperty(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += " & ";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_NOT)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "!(";
			String child = convertMath2PrismProperty(math.getChild(0));
			result += child;
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_OR)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = convertMath2PrismProperty(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += " | ";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_XOR)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "xor(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = convertMath2PrismProperty(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.MINUS)
		{
			if (math.getChildCount() == 1)
			{
				return "-" + convertMath2PrismProperty(math.getChild(0));
			}
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "(" + leftStr + " - " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.NAME)
		{
			return math.getName();
		}
		else if (math.getType() == ASTNode.Type.NAME_AVOGADRO)
		{
			return "avogadro";
		}
		else if (math.getType() == ASTNode.Type.NAME_TIME)
		{
			return "t";
		}
		else if (math.getType() == ASTNode.Type.PLUS)
		{
			String returnVal = "(";
			boolean first = true;
			for (int i = 0; i < math.getChildCount(); i++)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					returnVal += " + ";
				}
				returnVal += convertMath2PrismProperty(math.getChild(i));
			}
			returnVal += ")";
			return returnVal;
		}
		else if (math.getType() == ASTNode.Type.POWER)
		{
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "(" + leftStr + " ^ " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RATIONAL)
		{
			if (math.hasUnits())
			{
				return math.getNumerator() + "/" + math.getDenominator() + " " + math.getUnits();
			}
			return math.getNumerator() + "/" + math.getDenominator();
		}
		else if (math.getType() == ASTNode.Type.REAL)
		{
			if (math.hasUnits())
			{
				return "" + math.getReal() + " " + math.getUnits();
			}
			return "" + math.getReal();
		}
		else if (math.getType() == ASTNode.Type.REAL_E)
		{
			if (math.hasUnits())
			{
				return math.getMantissa() + "e" + math.getExponent() + " " + math.getUnits();
			}
			return math.getMantissa() + "e" + math.getExponent();
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_EQ)
		{
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "(" + leftStr + " == " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GEQ)
		{
			if (math.getChildCount()>=2 && math.getChild(0).getType() == ASTNode.Type.FUNCTION &&
					(math.getChild(0).getName().equals("PG") || math.getChild(0).getName().equals("PF")
							|| math.getChild(0).getName().equals("PU"))) {
				return "P>=" + convertMath2PrismProperty(math.getRightChild()) + " " +
						convertMath2PrismProperty(math.getLeftChild());
			} else {
				String leftStr = convertMath2PrismProperty(math.getLeftChild());
				String rightStr = convertMath2PrismProperty(math.getRightChild());
				return "(" + leftStr + " >= " + rightStr + ")";
			}
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GT)
		{
			if (math.getChildCount()>=2 && math.getChild(0).getType() == ASTNode.Type.FUNCTION &&
					(math.getChild(0).getName().equals("PG") || math.getChild(0).getName().equals("PF")
							|| math.getChild(0).getName().equals("PU"))) {
				return "P>" + convertMath2PrismProperty(math.getRightChild()) + " " +
						convertMath2PrismProperty(math.getLeftChild());
			} else {
				String leftStr = convertMath2PrismProperty(math.getLeftChild());
				String rightStr = convertMath2PrismProperty(math.getRightChild());
				return "(" + leftStr + " > " + rightStr + ")";
			}
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LEQ)
		{
			if (math.getChildCount()>=2 && math.getChild(0).getType() == ASTNode.Type.FUNCTION &&
					(math.getChild(0).getName().equals("PG") || math.getChild(0).getName().equals("PF")
							|| math.getChild(0).getName().equals("PU"))) {
				return "P<=" + convertMath2PrismProperty(math.getRightChild()) + " " +
						convertMath2PrismProperty(math.getLeftChild());
			} else {
				String leftStr = convertMath2PrismProperty(math.getLeftChild());
				String rightStr = convertMath2PrismProperty(math.getRightChild());
				return "(" + leftStr + " <= " + rightStr + ")";
			}
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LT)
		{
			if (math.getChildCount()>=2 && math.getChild(0).getType() == ASTNode.Type.FUNCTION &&
					(math.getChild(0).getName().equals("PG") || math.getChild(0).getName().equals("PF")
							|| math.getChild(0).getName().equals("PU"))) {
				return "P<" + convertMath2PrismProperty(math.getRightChild()) + " " +
						convertMath2PrismProperty(math.getLeftChild());
			} else {
				String leftStr = convertMath2PrismProperty(math.getLeftChild());
				String rightStr = convertMath2PrismProperty(math.getRightChild());
				return "(" + leftStr + " < " + rightStr + ")";
			}
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_NEQ)
		{
			String leftStr = convertMath2PrismProperty(math.getLeftChild());
			String rightStr = convertMath2PrismProperty(math.getRightChild());
			return "(" + leftStr + " != " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.TIMES)
		{
			String returnVal = "(";
			boolean first = true;
			for (int i = 0; i < math.getChildCount(); i++)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					returnVal += " * ";
				}
				returnVal += convertMath2PrismProperty(math.getChild(i));
			}
			returnVal += ")";
			return returnVal;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SELECTOR)
		{
			String returnVal = convertMath2PrismProperty(math.getChild(0));
			for (int i = 1; i < math.getChildCount(); i++)
			{
				returnVal += "[" + convertMath2PrismProperty(math.getChild(i)) + "]";
			}
			return returnVal;
		}
		else if (math.getType() == ASTNode.Type.VECTOR)
		{
			String returnVal = "{";
			boolean first = true;
			for (int i = 0; i < math.getChildCount(); i++)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					returnVal += ", ";
				}
				returnVal += convertMath2PrismProperty(math.getChild(i));
			}
			returnVal += "}";
			return returnVal;
		}
		else
		{
			if (math.isOperator())
			{
				System.out.println("Operator " + math.getName() + " is not currently supported.");
			}
			else
			{
				System.out.println(math.getName() + " is not currently supported.");
			}
		}
		return "";
	}

	/**
	 * Check if a function is in use.
	 * @param document
	 * @param id
	 * @param zeroDim
	 * @param displayMessage
	 * @param checkReactions
	 * @return
	 */
	
	public static boolean functionInUse(SBMLDocument document, String id, boolean zeroDim, boolean displayMessage, boolean checkReactions)
	{
		if (variableInUse(document,id,zeroDim,displayMessage,checkReactions)) {
			return true;
		}
		Model model = document.getModel();
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++)
		{
			FunctionDefinition funcDefn = model.getFunctionDefinition(i);
			String funcDefnStr = SBMLutilities.myFormulaToString(funcDefn.getMath());
			String[] vars = funcDefnStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++)
			{
				if (vars[j].equals(id))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Check if a variable is in use.
	 */
	public static boolean variableInUse(SBMLDocument document, String id, boolean zeroDim, boolean displayMessage, boolean checkReactions)
	{
		Model model = document.getModel();
		boolean inUse = false;
		boolean isSpecies = (document.getModel().getSpecies(id) != null);
		if (id.equals(""))
		{
			return inUse;
		}
		boolean usedInModelConversionFactor = false;

		ArrayList<String> stoicMathUsing = new ArrayList<String>();
		ArrayList<String> reactantsUsing = new ArrayList<String>();
		ArrayList<String> productsUsing = new ArrayList<String>();
		ArrayList<String> modifiersUsing = new ArrayList<String>();
		ArrayList<String> kineticLawsUsing = new ArrayList<String>();
		ArrayList<String> defaultParametersNeeded = new ArrayList<String>();
		ArrayList<String> initsUsing = new ArrayList<String>();
		ArrayList<String> rulesUsing = new ArrayList<String>();
		ArrayList<String> constraintsUsing = new ArrayList<String>();
		ArrayList<String> eventsUsing = new ArrayList<String>();
		ArrayList<String> speciesUsing = new ArrayList<String>();
		ArrayList<String> fluxObjUsing = new ArrayList<String>();
		if (document.getLevel() > 2)
		{
			if (model.isSetConversionFactor() && model.getConversionFactor().equals(id))
			{
				inUse = true;
				usedInModelConversionFactor = true;
			}
			for (int i = 0; i < model.getSpeciesCount(); i++)
			{
				Species speciesConv = model.getListOfSpecies().get(i);
				if (speciesConv.isSetConversionFactor())
				{
					if (id.equals(speciesConv.getConversionFactor()))
					{
						inUse = true;
						speciesUsing.add(speciesConv.getId());
					}
				}
			}
		}
		if (checkReactions)
		{
			for (int i = 0; i < model.getReactionCount(); i++)
			{
				Reaction reaction = model.getListOfReactions().get(i);
				if (isSpecies
						&& (BioModel.isDegradationReaction(reaction) || BioModel.isDiffusionReaction(reaction) || BioModel
								.isConstitutiveReaction(reaction)))
				{
					continue;
				}
				if (BioModel.isProductionReaction(reaction) && BioModel.IsDefaultProductionParameter(id))
				{
					defaultParametersNeeded.add(reaction.getId());
					inUse = true;
				}
				for (int j = 0; j < reaction.getProductCount(); j++)
				{
					if (reaction.getProduct(j).isSetSpecies())
					{
						String specRef = reaction.getProduct(j).getSpecies();
						if (id.equals(specRef))
						{
							inUse = true;
							productsUsing.add(reaction.getId());
						}
					}
				}
				for (int j = 0; j < reaction.getReactantCount(); j++)
				{
					if (reaction.getReactant(j).isSetSpecies())
					{
						String specRef = reaction.getReactant(j).getSpecies();
						if (id.equals(specRef))
						{
							inUse = true;
							reactantsUsing.add(reaction.getId());
						}
					}
				}
				for (int j = 0; j < reaction.getModifierCount(); j++)
				{
					if (reaction.getModifier(j).isSetSpecies())
					{
						String specRef = reaction.getModifier(j).getSpecies();
						if (id.equals(specRef))
						{
							inUse = true;
							modifiersUsing.add(reaction.getId());
						}
					}
				}
				if (reaction.isSetKineticLaw())
				{
					String[] vars = SBMLutilities.myFormulaToString(reaction.getKineticLaw().getMath()).split(" |\\(|\\)|\\,");
					for (int j = 0; j < vars.length; j++)
					{
						if (vars[j].equals(id))
						{
							kineticLawsUsing.add(reaction.getId());
							inUse = true;
							break;
						}
					}
				}
			}
		}
		for (int i = 0; i < model.getInitialAssignmentCount(); i++)
		{
			InitialAssignment init = model.getInitialAssignment(i);
			String initStr = SBMLutilities.myFormulaToString(init.getMath());
			String[] vars = initStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++)
			{
				if (vars[j].equals(id))
				{
					initsUsing.add(init.getVariable() + " = " + SBMLutilities.myFormulaToString(init.getMath()));
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < model.getRuleCount(); i++)
		{
			Rule rule = model.getRule(i);
			String initStr = SBMLutilities.myFormulaToString(rule.getMath());
			if (rule.isAssignment() || rule.isRate())
			{
				initStr += " = " + getVariable(rule);
			}
			String[] vars = initStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++)
			{
				if (vars[j].equals(id))
				{
					if (rule.isAssignment())
					{
						rulesUsing.add(getVariable(rule) + " = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					else if (rule.isRate())
					{
						rulesUsing.add("d(" + getVariable(rule) + ")/dt = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					else
					{
						rulesUsing.add("0 = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < model.getConstraintCount(); i++)
		{
			Constraint constraint = model.getConstraint(i);
			String consStr = SBMLutilities.myFormulaToString(constraint.getMath());
			String[] vars = consStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++)
			{
				if (vars[j].equals(id))
				{
					constraintsUsing.add(consStr);
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < model.getEventCount(); i++)
		{
			org.sbml.jsbml.Event event = model.getEvent(i);
			String trigger = SBMLutilities.myFormulaToString(event.getTrigger().getMath());
			String eventStr = trigger;
			if (event.isSetDelay())
			{
				eventStr += " " + SBMLutilities.myFormulaToString(event.getDelay().getMath());
			}
			for (int j = 0; j < event.getEventAssignmentCount(); j++)
			{
				eventStr += " " + (event.getListOfEventAssignments().get(j).getVariable()) + " = "
						+ SBMLutilities.myFormulaToString(event.getListOfEventAssignments().get(j).getMath());
			}
			String[] vars = eventStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++)
			{
				if (vars[j].equals(id))
				{
					eventsUsing.add(event.getId());
					inUse = true;
					break;
				}
			}
		}
		FBCModelPlugin fbc = getFBCModelPlugin(model,false);
		if (fbc!=null) {
			for (int i = 0; i < fbc.getNumObjective(); i++)
			{
				Objective obj = fbc.getObjective(i);
				for (int j = 0; j < obj.getListOfFluxObjectives().size(); j++)
				{
					FluxObjective fluxObj = obj.getListOfFluxObjectives().get(j);
					if (fluxObj.getReaction().equals(id))
					{
						fluxObjUsing.add(obj.getId());
						inUse = true;
					}
				}
			}
		}
		if (inUse)
		{
			String reactants = "";
			String products = "";
			String modifiers = "";
			String kineticLaws = "";
			String defaults = "";
			String stoicMath = "";
			String initAssigns = "";
			String rules = "";
			String constraints = "";
			String events = "";
			String speciesConvFac = "";
			String fluxObj = "";
			String[] fluxObjectives = fluxObjUsing.toArray(new String[0]);
			Utility.sort(fluxObjectives);
			String[] speciesConvFactors = speciesUsing.toArray(new String[0]);
			Utility.sort(speciesConvFactors);
			String[] reacts = reactantsUsing.toArray(new String[0]);
			Utility.sort(reacts);
			String[] prods = productsUsing.toArray(new String[0]);
			Utility.sort(prods);
			String[] mods = modifiersUsing.toArray(new String[0]);
			Utility.sort(mods);
			String[] kls = kineticLawsUsing.toArray(new String[0]);
			Utility.sort(kls);
			String[] dps = defaultParametersNeeded.toArray(new String[0]);
			Utility.sort(dps);
			String[] sm = stoicMathUsing.toArray(new String[0]);
			Utility.sort(sm);
			String[] inAs = initsUsing.toArray(new String[0]);
			Utility.sort(inAs);
			String[] ruls = rulesUsing.toArray(new String[0]);
			Utility.sort(ruls);
			String[] consts = constraintsUsing.toArray(new String[0]);
			Utility.sort(consts);
			String[] evs = eventsUsing.toArray(new String[0]);
			Utility.sort(evs);
			for (int i = 0; i < fluxObjectives.length; i++)
			{
				if (i == fluxObjectives.length - 1)
				{
					fluxObj += fluxObjectives[i];
				}
				else
				{
					fluxObj += fluxObjectives[i] + "\n";
				}
			}
			for (int i = 0; i < speciesConvFactors.length; i++)
			{
				if (i == speciesConvFactors.length - 1)
				{
					speciesConvFac += speciesConvFactors[i];
				}
				else
				{
					speciesConvFac += speciesConvFactors[i] + "\n";
				}
			}
			for (int i = 0; i < reacts.length; i++)
			{
				if (i == reacts.length - 1)
				{
					reactants += reacts[i];
				}
				else
				{
					reactants += reacts[i] + "\n";
				}
			}
			for (int i = 0; i < prods.length; i++)
			{
				if (i == prods.length - 1)
				{
					products += prods[i];
				}
				else
				{
					products += prods[i] + "\n";
				}
			}
			for (int i = 0; i < mods.length; i++)
			{
				if (i == mods.length - 1)
				{
					modifiers += mods[i];
				}
				else
				{
					modifiers += mods[i] + "\n";
				}
			}
			for (int i = 0; i < kls.length; i++)
			{
				if (i == kls.length - 1)
				{
					kineticLaws += kls[i];
				}
				else
				{
					kineticLaws += kls[i] + "\n";
				}
			}
			for (int i = 0; i < dps.length; i++)
			{
				if (i == dps.length - 1)
				{
					defaults += dps[i];
				}
				else
				{
					defaults += dps[i] + "\n";
				}
			}
			for (int i = 0; i < sm.length; i++)
			{
				if (i == sm.length - 1)
				{
					stoicMath += sm[i];
				}
				else
				{
					stoicMath += sm[i] + "\n";
				}
			}
			for (int i = 0; i < inAs.length; i++)
			{
				if (i == inAs.length - 1)
				{
					initAssigns += inAs[i];
				}
				else
				{
					initAssigns += inAs[i] + "\n";
				}
			}
			for (int i = 0; i < ruls.length; i++)
			{
				if (i == ruls.length - 1)
				{
					rules += ruls[i];
				}
				else
				{
					rules += ruls[i] + "\n";
				}
			}
			for (int i = 0; i < consts.length; i++)
			{
				if (i == consts.length - 1)
				{
					constraints += consts[i];
				}
				else
				{
					constraints += consts[i] + "\n";
				}
			}
			for (int i = 0; i < evs.length; i++)
			{
				if (i == evs.length - 1)
				{
					events += evs[i];
				}
				else
				{
					events += evs[i] + "\n";
				}
			}
			String message;
			if (zeroDim)
			{
				message = "Unable to change compartment to 0-dimensions.";
			}
			else
			{
				message = "Unable to remove the selected variable.";
			}
			if (usedInModelConversionFactor)
			{
				message += "\n\nIt is used as the model conversion factor.\n";
			}
			if (speciesUsing.size() != 0)
			{
				message += "\n\nIt is used as a conversion factor in the following species:\n" + speciesConvFac;
			}
			if (reactantsUsing.size() != 0)
			{
				message += "\n\nIt is used as a reactant in the following reactions:\n" + reactants;
			}
			if (productsUsing.size() != 0)
			{
				message += "\n\nIt is used as a product in the following reactions:\n" + products;
			}
			if (modifiersUsing.size() != 0)
			{
				message += "\n\nIt is used as a modifier in the following reactions:\n" + modifiers;
			}
			if (kineticLawsUsing.size() != 0)
			{
				message += "\n\nIt is used in the kinetic law in the following reactions:\n" + kineticLaws;
			}
			if (defaultParametersNeeded.size() != 0)
			{
				message += "\n\nDefault parameter is needed by the following reactions:\n" + defaults;
			}
			if (stoicMathUsing.size() != 0)
			{
				message += "\n\nIt is used in the stoichiometry math for the following reaction/species:\n" + stoicMath;
			}
			if (initsUsing.size() != 0)
			{
				message += "\n\nIt is used in the following initial assignments:\n" + initAssigns;
			}
			if (rulesUsing.size() != 0)
			{
				message += "\n\nIt is used in the following rules:\n" + rules;
			}
			if (constraintsUsing.size() != 0)
			{
				message += "\n\nIt is used in the following constraints:\n" + constraints;
			}
			if (eventsUsing.size() != 0)
			{
				message += "\n\nIt is used in the following events:\n" + events;
			}
			if (fluxObjUsing.size() != 0)
			{
				message += "\n\nIt is used in the following flux objectives:\n" + fluxObj;
			}
			
			SBMLutilities.message.setErrorDialog("Unable To Remove Variable", message);
			
		}
		return inUse;
	}

	/**
	 * Update variable Id
	 * @throws BioSimException 
	 */
	public static void updateVarId(SBMLDocument document, boolean isSpecies, String origId, String newId) throws BioSimException
	{
		if (origId==null || origId.equals("") || origId.equals(newId))
		{
			return;
		}
		Model model = document.getModel();
		for (int i = 0; i < model.getReactionCount(); i++)
		{
			Reaction reaction = model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getProductCount(); j++)
			{
				if (reaction.getProduct(j).isSetSpecies())
				{
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies()))
					{
						specRef.setSpecies(newId);
					}
				}
			}
			if (isSpecies)
			{
				for (int j = 0; j < reaction.getModifierCount(); j++)
				{
					if (reaction.getModifier(j).isSetSpecies())
					{
						ModifierSpeciesReference specRef = reaction.getModifier(j);
						if (origId.equals(specRef.getSpecies()))
						{
							specRef.setSpecies(newId);
						}
					}
				}
			}
			for (int j = 0; j < reaction.getReactantCount(); j++)
			{
				if (reaction.getReactant(j).isSetSpecies())
				{
					SpeciesReference specRef = reaction.getReactant(j);
					if (isSpecies && origId.equals(specRef.getSpecies()))
					{
						specRef.setSpecies(newId);
					}
				}
			}
			if (reaction.isSetKineticLaw())
			{
				reaction.getKineticLaw().setMath(SBMLutilities.updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
			}
		}
		if (document.getLevel() > 2)
		{
			if (model.isSetConversionFactor() && origId.equals(model.getConversionFactor()))
			{
				model.setConversionFactor(newId);
			}
			if (model.getSpeciesCount() > 0)
			{
				for (int i = 0; i < model.getSpeciesCount(); i++)
				{
					Species species = model.getListOfSpecies().get(i);
					if (species.isSetConversionFactor())
					{
						if (origId.equals(species.getConversionFactor()))
						{
							species.setConversionFactor(newId);
						}
					}
				}
			}
		}
		if (model.getInitialAssignmentCount() > 0)
		{
			for (int i = 0; i < model.getInitialAssignmentCount(); i++)
			{
				InitialAssignment init = model.getListOfInitialAssignments().get(i);
				if (origId.equals(init.getVariable()))
				{
					init.setVariable(newId);
				}
				init.setMath(SBMLutilities.updateMathVar(init.getMath(), origId, newId));
			}
				if (SBMLutilities.checkCycles(document))
				{
				  throw new BioSimException("Cycle detected within initial assignments, assignment rules, and rate laws.",
            "Cycle Detected");
				}
		}
		if (model.getRuleCount() > 0)
		{
			for (int i = 0; i < model.getRuleCount(); i++)
			{
				Rule rule = model.getListOfRules().get(i);
				if (isSetVariable(rule) && origId.equals(getVariable(rule)))
				{
					setVariable(rule, newId);
				}
				rule.setMath(SBMLutilities.updateMathVar(rule.getMath(), origId, newId));
			}
				if (SBMLutilities.checkCycles(document))
				{
				  throw new BioSimException("Cycle detected within initial assignments, assignment rules, and rate laws.",
	            "Cycle Detected");
				}
		}
		if (model.getConstraintCount() > 0)
		{
			for (int i = 0; i < model.getConstraintCount(); i++)
			{
				Constraint constraint = model.getListOfConstraints().get(i);
				constraint.setMath(SBMLutilities.updateMathVar(constraint.getMath(), origId, newId));
			}
		}
		if (model.getEventCount() > 0)
		{
			for (int i = 0; i < model.getEventCount(); i++)
			{
				org.sbml.jsbml.Event event = model.getListOfEvents().get(i);
				if (event.isSetTrigger())
				{
					event.getTrigger().setMath(SBMLutilities.updateMathVar(event.getTrigger().getMath(), origId, newId));
				}
				if (event.isSetDelay())
				{
					event.getDelay().setMath(SBMLutilities.updateMathVar(event.getDelay().getMath(), origId, newId));
				}
				for (int j = 0; j < event.getEventAssignmentCount(); j++)
				{
					EventAssignment ea = event.getListOfEventAssignments().get(j);
					if (ea.getVariable().equals(origId))
					{
						ea.setVariable(newId);
					}
					if (ea.isSetMath())
					{
						ea.setMath(SBMLutilities.updateMathVar(ea.getMath(), origId, newId));
					}
				}
			}
		}
	}
	
//	/**
//	 * Checks consistency of the sbml file.
//	 */
//	public static boolean checkThread(String file, SBMLDocument doc, boolean overdeterminedOnly)
//	{
//		Validation validation = new Validation(file,doc,overdeterminedOnly);
//        Thread t = new Thread(validation);
////		final JButton cancel = new JButton("Cancel");
////		final JFrame running = new JFrame("Progress");
////		WindowListener w = new WindowListener()
////		{
////			@Override
////			public void windowClosing(WindowEvent arg0)
////			{
////				cancel.doClick();
////				running.dispose();
////			}
////
////			@Override
////			public void windowOpened(WindowEvent arg0)
////			{
////			}
////
////			@Override
////			public void windowClosed(WindowEvent arg0)
////			{
////			}
////
////			@Override
////			public void windowIconified(WindowEvent arg0)
////			{
////			}
////
////			@Override
////			public void windowDeiconified(WindowEvent arg0)
////			{
////			}
////
////			@Override
////			public void windowActivated(WindowEvent arg0)
////			{
////			}
////
////			@Override
////			public void windowDeactivated(WindowEvent arg0)
////			{
////			}
////		};
////		running.addWindowListener(w);
////		JPanel text = new JPanel();
////		JPanel progBar = new JPanel();
////		JPanel button = new JPanel();
////		JPanel all = new JPanel(new BorderLayout());
////		JLabel label = new JLabel("Running...");
////		JProgressBar progress = new JProgressBar(0, species.size());
////		progress.setStringPainted(true);
////		// progress.setString("");
////		progress.setValue(0);
////		text.add(label);
////		progBar.add(progress);
////		button.add(cancel);
////		all.add(text, "North");
////		all.add(progBar, "Center");
////		all.add(button, "South");
////		running.setContentPane(all);
////		running.pack();
////		Dimension screenSize;
////		//running.setLocation(x, y);
////		running.setVisible(true);
////		running.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
////		final Process learn;
////		int exitValue = 0;
////		try
////		{
////	        t.start();
////			cancel.setActionCommand("Cancel");
////			cancel.addActionListener(new ActionListener()
////			{
////				@Override
////				public void actionPerformed(ActionEvent e)
////				{
////					learn.destroy();
////					running.setCursor(null);
////					running.dispose();
////				}
////			});
////			//biosim.getExitButton().setActionCommand("Exit program");
////			biosim.getExitButton().addActionListener(new ActionListener()
////			{
////				@Override
////				public void actionPerformed(ActionEvent e)
////				{
////					learn.destroy();
////					running.setCursor(null);
////					running.dispose();
////				}
////			});
////			String output = "";
////			InputStream reb = learn.getInputStream();
////			InputStreamReader isr = new InputStreamReader(reb);
////			BufferedReader br = new BufferedReader(isr);
////			FileWriter out = new FileWriter(new File(directory + separator + "run.log"));
////			int count = 0;
////			while ((output = br.readLine()) != null)
////			{
////				if (output.startsWith("Gene = ", 0))
////				{
////					// log.addText(output);
////					count++;
////					progress.setValue(count);
////				}
////				out.write(output);
////				out.write("\n");
////			}
////			br.close();
////			isr.close();
////			reb.close();
////			out.close();
////			viewLog.setEnabled(true);
////			exitValue = learn.waitFor();
////		}
//        //t.wait();
//		return true;
//	}

	public static boolean checkUnitsInAssignmentRule(SBMLDocument document, Rule rule)
	{
		UnitDefinition unitDef = rule.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(getVariable(rule));
		Compartment compartment = document.getModel().getCompartment(getVariable(rule));
		Parameter parameter = document.getModel().getParameter(getVariable(rule));
		if (species != null)
		{
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null)
		{
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else
		{
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefVar))
		{
			return true;
		}
		return false;
	}

	public static boolean checkUnitsInRateRule(SBMLDocument document, Rule rule)
	{
		UnitDefinition unitDef = rule.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(getVariable(rule));
		Compartment compartment = document.getModel().getCompartment(getVariable(rule));
		Parameter parameter = document.getModel().getParameter(getVariable(rule));
		if (species != null)
		{
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null)
		{
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else
		{
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (document.getModel().getUnitDefinition("time") != null)
		{
			UnitDefinition timeUnitDef = document.getModel().getUnitDefinition("time");
			for (int i = 0; i < timeUnitDef.getUnitCount(); i++)
			{
				Unit timeUnit = timeUnitDef.getUnit(i);
				Unit recTimeUnit = unitDefVar.createUnit();
				recTimeUnit.setKind(timeUnit.getKind());
				if (document.getLevel() < 3)
				{
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				else
				{
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				recTimeUnit.setScale(timeUnit.getScale());
				recTimeUnit.setMultiplier(timeUnit.getMultiplier());
			}
		}
		else
		{
			Unit unit = unitDefVar.createUnit();
			unit.setKind(Unit.Kind.valueOf("second".toUpperCase()));
			unit.setExponent(-1.0);
			unit.setScale(0);
			unit.setMultiplier(1.0);
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefVar))
		{
			return true;
		}
		return false;
	}

	public static boolean checkUnitsInInitialAssignment(SBMLDocument document, InitialAssignment init)
	{
		UnitDefinition unitDef = init.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(init.getVariable());
		Compartment compartment = document.getModel().getCompartment(init.getVariable());
		Parameter parameter = document.getModel().getParameter(init.getVariable());
		if (species != null)
		{
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null)
		{
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else
		{
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefVar))
		{
			return true;
		}
		return false;
	}

	public static boolean checkUnitsInKineticLaw(SBMLDocument document, KineticLaw law)
	{
		UnitDefinition unitDef = law.getDerivedUnitDefinition();
		UnitDefinition unitDefLaw = new UnitDefinition(document.getLevel(), document.getVersion());
		if (document.getModel().getUnitDefinition("substance") != null)
		{
			UnitDefinition subUnitDef = document.getModel().getUnitDefinition("substance");
			for (int i = 0; i < subUnitDef.getUnitCount(); i++)
			{
				Unit subUnit = subUnitDef.getUnit(i);
				unitDefLaw.addUnit(subUnit);
			}
		}
		else
		{
			Unit unit = unitDefLaw.createUnit();
			unit.setKind(Unit.Kind.valueOf("mole".toUpperCase()));
			unit.setExponent(1.0);
			unit.setScale(0);
			unit.setMultiplier(1.0);
		}
		if (document.getModel().getUnitDefinition("time") != null)
		{
			UnitDefinition timeUnitDef = document.getModel().getUnitDefinition("time");
			for (int i = 0; i < timeUnitDef.getUnitCount(); i++)
			{
				Unit timeUnit = timeUnitDef.getUnit(i);
				Unit recTimeUnit = unitDefLaw.createUnit();
				recTimeUnit.setKind(timeUnit.getKind());
				if (document.getLevel() < 3)
				{
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				else
				{
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				recTimeUnit.setScale(timeUnit.getScale());
				recTimeUnit.setMultiplier(timeUnit.getMultiplier());
			}
		}
		else
		{
			Unit unit = unitDefLaw.createUnit();
			unit.setKind(Unit.Kind.valueOf("second".toUpperCase()));
			unit.setExponent(-1.0);
			unit.setScale(0);
			unit.setMultiplier(1.0);
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefLaw))
		{
			return true;
		}
		return false;
	}

	public static boolean checkUnitsInEventDelay(Delay delay)
	{
		UnitDefinition unitDef = delay.getDerivedUnitDefinition();
		if (unitDef != null && !(unitDef.isVariantOfTime()))
		{
			return true;
		}
		return false;
	}

	public static boolean checkUnitsInEventAssignment(SBMLDocument document, EventAssignment assign)
	{
		UnitDefinition unitDef = assign.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(assign.getVariable());
		Compartment compartment = document.getModel().getCompartment(assign.getVariable());
		Parameter parameter = document.getModel().getParameter(assign.getVariable());
		if (species != null)
		{
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null)
		{
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else
		{
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (unitDef != null && unitDefVar != null && !UnitDefinition.areEquivalent(unitDef, unitDefVar))
		{
			return true;
		}
		return false;
	}

	public static void addRandomFunctions(SBMLDocument document)
	{
		Model model = document.getModel();
		createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
		createFunction(model, "normal", "Normal distribution", "lambda(m,s,m)");
		createFunction(model, "exponential", "Exponential distribution", "lambda(l,1/l)");
		createFunction(model, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
		createFunction(model, "poisson", "Poisson distribution", "lambda(mu,mu)");
		createFunction(model, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
		createFunction(model, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
		createFunction(model, "laplace", "Laplace distribution", "lambda(a,0)");
		createFunction(model, "cauchy", "Cauchy distribution", "lambda(a,a)");
		createFunction(model, "rayleigh", "Rayleigh distribution", "lambda(s,s*sqrt(pi/2))");
		createFunction(model, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
		createFunction(model, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
	}

	public static void createDistribution(FunctionDefinition f, String[] inputTypes, String[] inputs, String distribution)
	{
		DistribFunctionDefinitionPlugin distrib = SBMLutilities.getDistribFunctionDefinitionPlugin(f);
		DrawFromDistribution draw = distrib.createDrawFromDistribution();
		for (int i = 0; i < inputs.length; i++)
		{
			DistribInput input = draw.createDistribInput();
			input.setId(inputs[i]);
			input.setIndex(i);
		}
		// UncertML element
		XMLNode xmlNode = new XMLNode(new XMLTriple("UncertML"), new XMLAttributes(), new XMLNamespaces());
		xmlNode.addNamespace("http://www.uncertml.org/3.0");

		// NormalDistribution element
		XMLNode distNode = new XMLNode(new XMLTriple(distribution), new XMLAttributes(), new XMLNamespaces());
		distNode.addAttr("definition", "http://www.uncertml.org/distributions");
		xmlNode.addChild(distNode);

		for (int i = 0; i < inputs.length; i++)
		{
			XMLNode inputNode = new XMLNode(new XMLTriple(inputTypes[i]), new XMLAttributes(), new XMLNamespaces());
			distNode.addChild(inputNode);
			XMLNode varNode = new XMLNode(new XMLTriple("var"), new XMLAttributes(), new XMLNamespaces());
			varNode.addAttr("varId", inputs[i]);
			inputNode.addChild(varNode);
		}
		draw.setUncertML(xmlNode);
	}

	/**
	 * Add a new function
	 */
	public static void createFunction(Model model, String id, String name, String formula)
	{
		if (model.getFunctionDefinition(id) == null)
		{
			FunctionDefinition f = model.createFunctionDefinition();
			f.setId(id);
			f.setName(name);
			try
			{
				IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
				f.setMath(ASTNode.parseFormula(formula, parser));
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			if (id.equals("uniform"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Uniform_distribution_(continuous)");
				SBMLutilities.createDistribution(f, new String[] { "minimum", "maximum" }, new String[] { "a", "b" }, "UniformDistribution");
			}
			else if (id.equals("normal"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Normal_distribution");
				SBMLutilities.createDistribution(f, new String[] { "mean", "stddev" }, new String[] { "m", "s" }, "NormalDistribution");
			}
			else if (id.equals("exponential"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Exponential_distribution");
				SBMLutilities.createDistribution(f, new String[] { "rate" }, new String[] { "l" }, "ExponentialDistribution");
			}
			else if (id.equals("gamma"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Gamma_distribution");
				SBMLutilities.createDistribution(f, new String[] { "shape", "scale" }, new String[] { "a", "b" }, "GammaDistribution");
			}
			else if (id.equals("poisson"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Poisson_distribution");
				SBMLutilities.createDistribution(f, new String[] { "rate" }, new String[] { "mu" }, "PoissonDistribution");
			}
			else if (id.equals("lognormal"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Log-normal_distribution");
				// TODO: check order
				SBMLutilities.createDistribution(f, new String[] { "shape", "logScale" }, new String[] { "sh", "lsc" }, "GammaDistribution");
			}
			else if (id.equals("chisq"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Chi-squared_distribution");
				SBMLutilities.createDistribution(f, new String[] { "degreeOfFreedom" }, new String[] { "nu" }, "ChiSquareDistribution");
			}
			else if (id.equals("laplace"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Laplace_distribution");
				// TODO: mine only has one param?
				SBMLutilities.createDistribution(f, new String[] { "location", "scale" }, new String[] { "l", "s" }, "LaplaceDistribution");
			}
			else if (id.equals("cauchy"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Cauchy_distribution");
				// TODO: mine only has one param?
				SBMLutilities.createDistribution(f, new String[] { "location", "scale" }, new String[] { "l", "s" }, "CauchyDistribution");
			}
			else if (id.equals("rayleigh"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Rayleigh_distribution");
				// TODO: Missing?
			}
			else if (id.equals("binomial"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Binomial_distribution");
				SBMLutilities.createDistribution(f, new String[] { "probabilityOfSuccess", "numberOfTrials" }, new String[] { "p", "n" },
						"BinomialDistribution");
			}
			else if (id.equals("bernoulli"))
			{
				AnnotationUtility.setDistributionAnnotation(f, "http://en.wikipedia.org/wiki/Bernoulli_distribution");
				SBMLutilities.createDistribution(f, new String[] { "categoryProb" }, new String[] { "p" }, "BernoulliDistribution");
			}
		}
	}

	public static void createDirPort(SBMLDocument document, String SId, String dir)
	{
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(document.getModel());
		Port port = null;
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++)
		{
			Port curPort = sbmlCompModel.getListOfPorts().get(i);
			if (curPort.isSetIdRef() && curPort.getIdRef().equals(SId))
			{
				port = curPort;
			}
		}
		SBase variable = SBMLutilities.getElementBySId(document, SId);
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(variable);
		if (dir.equals(GlobalConstants.INPUT))
		{
			if (port == null)
			{
				port = sbmlCompModel.createPort();
			}
			port.setId(dir + "__" + SId);
			port.setIdRef(SId);
			port.setSBOTerm(GlobalConstants.SBO_INPUT_PORT);
			ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
			sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());
			sBasePluginPort.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++)
			{
				org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
				Index portIndex = sBasePluginPort.createIndex();
				portIndex.setReferencedAttribute("comp:idRef");
				portIndex.setArrayDimension(i);
				portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
			}
		}
		else if (dir.equals(GlobalConstants.OUTPUT))
		{
			if (port == null)
			{
				port = sbmlCompModel.createPort();
			}
			port.setId(dir + "__" + SId);
			port.setIdRef(SId);
			port.setSBOTerm(GlobalConstants.SBO_OUTPUT_PORT);
			ArraysSBasePlugin sBasePluginPort = SBMLutilities.getArraysSBasePlugin(port);
			sBasePluginPort.setListOfDimensions(sBasePlugin.getListOfDimensions().clone());
			sBasePluginPort.unsetListOfIndices();
			for (int i = 0; i < sBasePlugin.getListOfDimensions().size(); i++)
			{
				org.sbml.jsbml.ext.arrays.Dimension dimen = sBasePlugin.getDimensionByArrayDimension(i);
				Index portIndex = sBasePluginPort.createIndex();
				portIndex.setReferencedAttribute("comp:idRef");
				portIndex.setArrayDimension(i);
				portIndex.setMath(SBMLutilities.myParseFormula(dimen.getId()));
			}
		}
		else if (port != null)
		{
			sbmlCompModel.removePort(port);
		}
	}

	public static boolean isBoolean(SBMLDocument document, ASTNode node)
	{
		if (node == null)
		{
			return false;
		}
		else if (node.isBoolean())
		{
			return true;
		}
		else if (node.getType() == ASTNode.Type.FUNCTION)
		{
			FunctionDefinition fd = document.getModel().getFunctionDefinition(node.getName());
			if (fd != null && fd.isSetMath())
			{
				return isBoolean(document, fd.getMath().getRightChild());
			}
			return false;
		}
		else if (node.getType() == ASTNode.Type.FUNCTION_PIECEWISE)
		{
			for (int c = 0; c < node.getChildCount(); c += 2)
			{
				if (!isBoolean(document, node.getChild(c)))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isBoolean(SBase parameter)
	{
		if (parameter.isSetSBOTerm())
		{
			if (parameter.getSBOTerm() == GlobalConstants.SBO_BOOLEAN)
			{
				parameter.setSBOTerm(GlobalConstants.SBO_LOGICAL);
				return true;
			}
			else if (parameter.getSBOTerm() == GlobalConstants.SBO_LOGICAL)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isPlace(SBase parameter)
	{
		if (parameter.isSetSBOTerm())
		{
			if (parameter.getSBOTerm() == GlobalConstants.SBO_PLACE)
			{
				parameter.setSBOTerm(GlobalConstants.SBO_PETRI_NET_PLACE);
				return true;
			}
			else if (parameter.getSBOTerm() == GlobalConstants.SBO_PETRI_NET_PLACE)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isTransition(Event event)
	{
		if (event.isSetSBOTerm())
		{
			if (event.getSBOTerm() == GlobalConstants.SBO_TRANSITION)
			{
				event.setSBOTerm(GlobalConstants.SBO_PETRI_NET_TRANSITION);
				return true;
			}
			else if (event.getSBOTerm() == GlobalConstants.SBO_PETRI_NET_TRANSITION)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isPersistentTransition(SBMLDocument document, Event event)
	{
		if (event.isSetSBOTerm())
		{
			if (event.getSBOTerm() == GlobalConstants.SBO_TRANSITION)
			{
				event.setSBOTerm(GlobalConstants.SBO_PETRI_NET_TRANSITION);
				Rule r = document.getModel().getRule(GlobalConstants.TRIGGER + "_" + event.getId());
				if (r != null)
				{
					return true;
				}
			}
			else if (event.getSBOTerm() == GlobalConstants.SBO_PETRI_NET_TRANSITION)
			{
				Rule r = document.getModel().getRule(GlobalConstants.TRIGGER + "_" + event.getId());
				if (r != null)
				{
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isFailTransition(Event event)
	{
		for (int j = 0; j < event.getEventAssignmentCount(); j++)
		{
			EventAssignment ea = event.getListOfEventAssignments().get(j);
			if (ea.getVariable().equals(GlobalConstants.FAIL))
			{
				return true;
			}
		}
		return false;
	}

	public static String getIdWithDimension(SBase sbase, String id)
	{
		String result = id;
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sbase);
		for (int i = sBasePlugin.getListOfDimensions().size() - 1; i >= 0; i--)
		{
			Dimension dim = sBasePlugin.getDimensionByArrayDimension(i);
			result += "[" + dim.getId() + "]";
		}
		return result;
	}

	public static ASTNode addPreset(ASTNode math, String place)
	{
		return myParseFormula("and(" + myFormulaToString(math) + ",eq(" + place + "," + "1))");
	}

	public static ASTNode removePreset(ASTNode math, String place)
	{
		if (math.getType() == ASTNode.Type.LOGICAL_AND)
		{
			ASTNode rightChild = math.getRightChild();
			if (rightChild.getType() == ASTNode.Type.RELATIONAL_EQ && rightChild.getLeftChild().toFormula().equals(place))
			{
				return deepCopy(math.getLeftChild());
			}
		}
		for (int i = 0; i < math.getChildCount(); i++)
		{
			ASTNode child = removePreset(math.getChild(i), place);
			math.replaceChild(i, child);
		}
		return deepCopy(math);
	}

	public static String addBoolean(String formula, String boolVar)
	{
		formula = formula.replace(" " + boolVar + " ", " (eq(" + boolVar + ",1)) ");
		formula = formula.replace("," + boolVar + ",", ",(eq(" + boolVar + ",1)),");
		formula = formula.replace("(" + boolVar + ",", "((eq(" + boolVar + ",1)),");
		formula = formula.replace("," + boolVar + ")", ",(eq(" + boolVar + ",1)))");
		formula = formula.replace("(" + boolVar + " ", "((eq(" + boolVar + ",1)) ");
		formula = formula.replace(" " + boolVar + ")", " (eq(" + boolVar + ",1)))");
		formula = formula.replace("(" + boolVar + ")", " (eq(" + boolVar + ",1))");
		if (formula.startsWith(boolVar + " "))
		{
			formula = formula.replaceFirst(boolVar + " ", "(eq(" + boolVar + ",1))");
		}
		if (formula.endsWith(" " + boolVar))
		{
			formula = formula.replaceFirst(" " + boolVar, "(eq(" + boolVar + ",1))");
		}
		if (formula.equals(boolVar))
		{
			formula = formula.replace(boolVar, "(eq(" + boolVar + ",1))");
		}
		return formula;
	}

	public static ASTNode removeBoolean(ASTNode math, String boolVar)
	{
		if (math == null)
		{
			return null;
		}
		if (math.getType() == ASTNode.Type.RELATIONAL_EQ)
		{
			if (math.getLeftChild().isSetName() && math.getLeftChild().getName().equals(boolVar))
			{
				return deepCopy(math.getLeftChild());
			}
		}
		for (int i = 0; i < math.getChildCount(); i++)
		{
			ASTNode child = removeBoolean(math.getChild(i), boolVar);
			math.replaceChild(i, child);
		}
		return deepCopy(math);
	}

	public static String myFormulaToStringInfix(ASTNode math)
	{
		if (math.getType() == ASTNode.Type.CONSTANT_E)
		{
			return "exponentiale";
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_FALSE)
		{
			return "false";
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_PI)
		{
			return "pi";
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_TRUE)
		{
			return "true";
		}
		else if (math.getType() == ASTNode.Type.DIVIDE)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " / " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION)
		{
			String result = math.getName() + "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ABS)
		{
			return "abs(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOS)
		{
			return "acos(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOSH)
		{
			return "acosh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOT)
		{
			return "acot(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOTH)
		{
			return "acoth(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSC)
		{
			return "acsc(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSCH)
		{
			return "acsch(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSEC)
		{
			return "asec(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSECH)
		{
			return "asech(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSIN)
		{
			return "asin(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSINH)
		{
			return "asinh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCTAN)
		{
			return "atan(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCTANH)
		{
			return "atanh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CEILING)
		{
			return "ceil(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COS)
		{
			return "cos(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COSH)
		{
			return "cosh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COT)
		{
			return "cot(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COTH)
		{
			return "coth(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CSC)
		{
			return "csc(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CSCH)
		{
			return "csch(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_DELAY)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "delay(" + leftStr + " , " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_EXP)
		{
			return "exp(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_FACTORIAL)
		{
			return "factorial(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_FLOOR)
		{
			return "floor(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_LN)
		{
			return "ln(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_LOG)
		{
			String result = "log(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE)
		{
			String result = "piecewise(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_POWER)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "pow(" + leftStr + " , " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ROOT)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "root(" + leftStr + " , " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SEC)
		{
			return "sec(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SECH)
		{
			return "sech(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SIN)
		{
			return "sin(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SINH)
		{
			return "sinh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_TAN)
		{
			return "tan(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_TANH)
		{
			return "tanh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		}
		else if (math.getType() == ASTNode.Type.INTEGER)
		{
			if (math.hasUnits())
			{
				return "" + math.getInteger() + " " + math.getUnits();
			}
			return "" + math.getInteger();
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_AND)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += " && ";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_NOT)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "!(";
			String child = myFormulaToStringInfix(math.getChild(0));
			result += child;
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_OR)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += " || ";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_XOR)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "xor(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.MINUS)
		{
			if (math.getChildCount() == 1)
			{
				return "-" + myFormulaToStringInfix(math.getChild(0));
			}
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " - " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.NAME)
		{
			return math.getName();
		}
		else if (math.getType() == ASTNode.Type.NAME_AVOGADRO)
		{
			return "avogadro";
		}
		else if (math.getType() == ASTNode.Type.NAME_TIME)
		{
			return "t";
		}
		else if (math.getType() == ASTNode.Type.PLUS)
		{
			String returnVal = "(";
			boolean first = true;
			for (int i = 0; i < math.getChildCount(); i++)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					returnVal += " + ";
				}
				returnVal += myFormulaToStringInfix(math.getChild(i));
			}
			returnVal += ")";
			return returnVal;
		}
		else if (math.getType() == ASTNode.Type.POWER)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " ^ " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RATIONAL)
		{
			if (math.hasUnits())
			{
				return math.getNumerator() + "/" + math.getDenominator() + " " + math.getUnits();
			}
			return math.getNumerator() + "/" + math.getDenominator();
		}
		else if (math.getType() == ASTNode.Type.REAL)
		{
			if (math.hasUnits())
			{
				return "" + math.getReal() + " " + math.getUnits();
			}
			return "" + math.getReal();
		}
		else if (math.getType() == ASTNode.Type.REAL_E)
		{
			if (math.hasUnits())
			{
				return math.getMantissa() + "e" + math.getExponent() + " " + math.getUnits();
			}
			return math.getMantissa() + "e" + math.getExponent();
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_EQ)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " == " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GEQ)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " >= " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GT)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " > " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LEQ)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " <= " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LT)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " < " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_NEQ)
		{
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " != " + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.TIMES)
		{
			String returnVal = "(";
			boolean first = true;
			for (int i = 0; i < math.getChildCount(); i++)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					returnVal += " * ";
				}
				returnVal += myFormulaToStringInfix(math.getChild(i));
			}
			returnVal += ")";
			return returnVal;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SELECTOR)
		{
			String returnVal = myFormulaToStringInfix(math.getChild(0));
			for (int i = 1; i < math.getChildCount(); i++)
			{
				returnVal += "[" + myFormulaToStringInfix(math.getChild(i)) + "]";
			}
			return returnVal;
		}
		else if (math.getType() == ASTNode.Type.VECTOR)
		{
			String returnVal = "{";
			boolean first = true;
			for (int i = 0; i < math.getChildCount(); i++)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					returnVal += ", ";
				}
				returnVal += myFormulaToStringInfix(math.getChild(i));
			}
			returnVal += "}";
			return returnVal;
		}
		else
		{
			if (math.isOperator())
			{
				System.out.println("Operator " + math.getName() + " is not currently supported.");
			}
			else
			{
				System.out.println(math.getName() + " is not currently supported.");
			}
		}
		return "";
	}

	public static boolean returnsBoolean(ASTNode math, Model model)
	{
		if (math.isBoolean())
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_E)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_FALSE)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_PI)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_TRUE)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.DIVIDE)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION)
		{
			return returnsBoolean(math.getRightChild(), model);
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ABS)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOS)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOSH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOT)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOTH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSC)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSCH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSEC)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSECH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSIN)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCSINH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCTAN)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ARCTANH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CEILING)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COS)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COSH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COT)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_COTH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CSC)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_CSCH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_DELAY)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_EXP)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_FACTORIAL)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_FLOOR)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_LN)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_LOG)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE)
		{
			boolean result = true;
			for (int i = 0; i < math.getChildCount(); i++)
			{
				result = result && returnsBoolean(math.getChild(i), model);
			}
			return result;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_POWER)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_ROOT)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SEC)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SECH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SIN)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SINH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_TAN)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_TANH)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.INTEGER)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_AND)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_NOT)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_OR)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_XOR)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.MINUS)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.NAME)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.NAME_AVOGADRO)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.NAME_TIME)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.PLUS)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.POWER)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.RATIONAL)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.REAL)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.REAL_E)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_EQ)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GEQ)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GT)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LEQ)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LT)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_NEQ)
		{
			return true;
		}
		else if (math.getType() == ASTNode.Type.TIMES)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.FUNCTION_SELECTOR)
		{
			return false;
		}
		else if (math.getType() == ASTNode.Type.VECTOR)
		{
			return false;
		}
		else
		{
			if (math.isOperator())
			{
				System.out.println("Operator " + math.getName() + " is not currently supported.");
			}
			else
			{
				System.out.println(math.getName() + " is not currently supported.");
			}
		}
		return false;
	}

	public static String SBMLMathToBoolLPNString(ASTNode math, HashMap<String, Integer> constants, ArrayList<String> booleans)
	{
		if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE && math.getChildCount() > 1)
		{
			return SBMLMathToLPNString(math.getChild(1), constants, booleans);
		}
		return SBMLMathToLPNString(math, constants, booleans);
	}

	public static String SBMLMathToLPNString(ASTNode math, HashMap<String, Integer> constants, ArrayList<String> booleans)
	{
		if (math.getType() == ASTNode.Type.CONSTANT_FALSE)
		{
			return "false";
		}
		else if (math.getType() == ASTNode.Type.CONSTANT_TRUE)
		{
			return "true";
		}
		else if (math.getType() == ASTNode.Type.REAL)
		{
			return "" + math.getReal();
		}
		else if (math.getType() == ASTNode.Type.INTEGER)
		{
			return "" + math.getInteger();
		}
		else if (math.getType() == ASTNode.Type.NAME)
		{
			if (constants.containsKey(math.getName()))
			{
				return "" + constants.get(math.getName());
			}
			return math.getName();
		}
		else if (math.getType() == ASTNode.Type.FUNCTION)
		{
			String result = math.getName() + "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = SBMLMathToLPNString(math.getChild(i), constants, booleans);
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.PLUS)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + "+" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.MINUS)
		{
			if (math.getChildCount() == 1)
			{
				return "-" + SBMLMathToLPNString(math.getChild(0), constants, booleans);
			}
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + "-" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.TIMES)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + "*" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.DIVIDE)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + "/" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.POWER)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + "^" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_EQ)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			if (booleans.contains(leftStr) && rightStr.equals("1"))
			{
				return leftStr;
			}
			return "(" + leftStr + "=" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GEQ)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + ">=" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_GT)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + ">" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LEQ)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + "<=" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_LT)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "(" + leftStr + "<" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.RELATIONAL_NEQ)
		{
			String leftStr = SBMLMathToLPNString(math.getLeftChild(), constants, booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(), constants, booleans);
			return "~(" + leftStr + "=" + rightStr + ")";
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_NOT)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "~(";
			String child = SBMLMathToLPNString(math.getChild(0), constants, booleans);
			result += child;
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_AND)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = SBMLMathToLPNString(math.getChild(i), constants, booleans);
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += "&";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_OR)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = SBMLMathToLPNString(math.getChild(i), constants, booleans);
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += "|";
				}
			}
			result += ")";
			return result;
		}
		else if (math.getType() == ASTNode.Type.LOGICAL_XOR)
		{
			if (math.getChildCount() == 0)
			{
				return "";
			}
			String result = "exor(";
			for (int i = 0; i < math.getChildCount(); i++)
			{
				String child = SBMLMathToLPNString(math.getChild(i), constants, booleans);
				result += child;
				if (i + 1 < math.getChildCount())
				{
					result += ",";
				}
			}
			result += ")";
			return result;
		}
		else
		{
			if (math.isOperator())
			{
				System.out.println("Operator " + math.getName() + " is not currently supported.");
			}
			else
			{
				System.out.println(math.getName() + " is not currently supported.");
			}
		}
		return "";
	}

	public static ArrayList<String> getPreset(SBMLDocument doc, Event event)
	{
		ArrayList<String> preset = new ArrayList<String>();
		for (int i = 0; i < event.getEventAssignmentCount(); i++)
		{
			EventAssignment ea = event.getListOfEventAssignments().get(i);
			Parameter p = doc.getModel().getParameter(ea.getVariable());
			if (p != null && SBMLutilities.isPlace(p) && SBMLutilities.myFormulaToString(ea.getMath()).equals("0"))
			{
				preset.add(p.getId());
			}
		}
		return preset;
	}

	public static ArrayList<String> getPostset(SBMLDocument doc, Event event)
	{
		ArrayList<String> postset = new ArrayList<String>();
		for (int i = 0; i < event.getEventAssignmentCount(); i++)
		{
			EventAssignment ea = event.getListOfEventAssignments().get(i);
			Parameter p = doc.getModel().getParameter(ea.getVariable());
			if (p != null && SBMLutilities.isPlace(p) && SBMLutilities.myFormulaToString(ea.getMath()).equals("1"))
			{
				postset.add(p.getId());
			}
		}
		return postset;
	}

	public static void replaceArgument(ASTNode formula, String bvar, ASTNode arg)
	{
		int n = 0;
		for (int i = 0; i < formula.getChildCount(); i++)
		{
			ASTNode child = formula.getChild(i);
			if (child.isSetName() && child.getName().equals(bvar))
			{
				formula.replaceChild(n, deepCopy(arg));
			}
			else if (child.getChildCount() > 0)
			{
				replaceArgument(child, bvar, arg);
			}
			n++;
		}
	}

	/**
	 * recursively puts every astnode child into the arraylist passed in
	 * 
	 * @param node
	 * @param nodeChildrenList
	 */
	protected static void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList)
	{

		for (int i = 0; i < node.getChildCount(); i++)
		{
			ASTNode child = node.getChild(i);
			if (child.getChildCount() == 0)
			{
				nodeChildrenList.add(child);
			}
			else
			{
				nodeChildrenList.add(child);
				getAllASTNodeChildren(child, nodeChildrenList);
			}
		}
	}

	/**
	 * inlines a formula with function definitions
	 * 
	 * @param formula
	 * @return
	 */
	public static ASTNode inlineFormula(Model model, ASTNode formula)
	{

		HashSet<String> ibiosimFunctionDefinitions = new HashSet<String>();

		ibiosimFunctionDefinitions.add("uniform");
		ibiosimFunctionDefinitions.add("exponential");
		ibiosimFunctionDefinitions.add("gamma");
		ibiosimFunctionDefinitions.add("chisq");
		ibiosimFunctionDefinitions.add("lognormal");
		ibiosimFunctionDefinitions.add("laplace");
		ibiosimFunctionDefinitions.add("cauchy");
		ibiosimFunctionDefinitions.add("poisson");
		ibiosimFunctionDefinitions.add("binomial");
		ibiosimFunctionDefinitions.add("bernoulli");
		ibiosimFunctionDefinitions.add("normal");
		ibiosimFunctionDefinitions.add("rate");
		ibiosimFunctionDefinitions.add("BIT");
		ibiosimFunctionDefinitions.add("BITNOT");
		ibiosimFunctionDefinitions.add("BITAND");
		ibiosimFunctionDefinitions.add("BITOR");
		ibiosimFunctionDefinitions.add("BITXOR");
		ibiosimFunctionDefinitions.add("G");
		ibiosimFunctionDefinitions.add("PG");
		ibiosimFunctionDefinitions.add("F");
		ibiosimFunctionDefinitions.add("PF");
		ibiosimFunctionDefinitions.add("U");
		ibiosimFunctionDefinitions.add("PU");

		if (formula.isFunction() == false || formula.isOperator()/* || formula.isLeaf() == false */)
		{

			for (int i = 0; i < formula.getChildCount(); ++i)
			{
				formula.replaceChild(i, inlineFormula(model, formula.getChild(i)));// .clone()));
			}

		}
		else if (formula.isFunction() && model.getFunctionDefinition(formula.getName()) != null)
		{

			if (ibiosimFunctionDefinitions.contains(formula.getName()))
			{
				return formula;
			}

			ASTNode inlinedFormula = deepCopy(model.getFunctionDefinition(formula.getName()).getBody());
			ASTNode oldFormula = deepCopy(formula);

			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			getAllASTNodeChildren(inlinedFormula, inlinedChildren);

			if (inlinedChildren.size() == 0)
			{
				inlinedChildren.add(inlinedFormula);
			}

			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();

			for (int i = 0; i < model.getFunctionDefinition(formula.getName()).getArgumentCount(); ++i)
			{
				inlinedChildToOldIndexMap.put(model.getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}

			for (int i = 0; i < inlinedChildren.size(); ++i)
			{

				ASTNode child = inlinedChildren.get(i);

				if (child.getChildCount() == 0 && child.isName())
				{

					int index = inlinedChildToOldIndexMap.get(child.getName());
					replaceArgument(inlinedFormula, myFormulaToString(child), inlineFormula(model,oldFormula.getChild(index)));

					if (inlinedFormula.getChildCount() == 0)
					{
						inlinedFormula = oldFormula.getChild(index);
					}
				}
			}

			return inlinedFormula;
		}
		return formula;
	}

	public static void expandFunctionDefinitions(SBMLDocument doc)
	{
		Model model = doc.getModel();
		for (int i = 0; i < model.getInitialAssignmentCount(); i++)
		{
			InitialAssignment ia = model.getListOfInitialAssignments().get(i);
			if (ia.isSetMath())
			{
				ia.setMath(inlineFormula(model, ia.getMath()));
			}
		}
		for (int i = 0; i < model.getRuleCount(); i++)
		{
			Rule r = model.getRule(i);
			if (r.isSetMath())
			{
				r.setMath(inlineFormula(model, r.getMath()));
			}
		}
		for (int i = 0; i < model.getConstraintCount(); i++)
		{
			Constraint c = model.getConstraint(i);
			if (c.isSetMath())
			{
				c.setMath(inlineFormula(model, c.getMath()));
			}
		}
		for (int i = 0; i < model.getEventCount(); i++)
		{
			Event e = model.getEvent(i);
			if (e.getDelay() != null && e.getDelay().isSetMath())
			{
				e.getDelay().setMath(inlineFormula(model, e.getDelay().getMath()));
			}
			if (e.getTrigger() != null && e.getTrigger().isSetMath())
			{
				e.getTrigger().setMath(inlineFormula(model, e.getTrigger().getMath()));
			}
			if (e.getPriority() != null && e.getPriority().isSetMath())
			{
				e.getPriority().setMath(inlineFormula(model, e.getPriority().getMath()));
			}
			for (int j = 0; j < e.getEventAssignmentCount(); j++)
			{
				EventAssignment ea = e.getListOfEventAssignments().get(j);
				if (ea.isSetMath())
				{
					ea.setMath(inlineFormula(model, ea.getMath()));
				}
			}
		}
	}

	public static void expandInitialAssignments(SBMLDocument document)
	{
		for (InitialAssignment ia : document.getModel().getListOfInitialAssignments())
		{
			SBase sb = getElementBySId(document, ia.getVariable());
			if (sb instanceof QuantityWithUnit)
			{
				((QuantityWithUnit) sb).setValue(evaluateExpression(document.getModel(), ia.getMath()));
			}
		}
		for (int i = 0; i < document.getModel().getListOfInitialAssignments().size(); i++)
		{
			document.getModel().getListOfInitialAssignments().remove(i);
		}
	}

	public static String getPromoterId(Reaction productionReaction)
	{
		for (int i = 0; i < productionReaction.getModifierCount(); i++)
		{
			ModifierSpeciesReference modifier = productionReaction.getModifier(i);
			if (BioModel.isPromoter(modifier))
			{
				return modifier.getSpecies();
			}
		}
		return null;
	}

	public static String getVariable(Rule r)
	{
		if (r instanceof ExplicitRule)
		{
			return ((ExplicitRule) r).getVariable();
		}
		return null;
	}

	public static void setVariable(Rule r, String variable)
	{
		if (r instanceof ExplicitRule)
		{
			((ExplicitRule) r).setVariable(variable);
		}
	}

	public static boolean isSetVariable(Rule r)
	{
		if (r instanceof ExplicitRule)
		{
			return ((ExplicitRule) r).isSetVariable();
		}
		return false;
	}

	public static ASTNode deepCopy(ASTNode original)
	{
		return new ASTNode(original);
	}

	public static SBase getElementByMetaId(SBMLDocument document, String metaId)
	{
		return document.findSBase(metaId);
	}

	public static SBase getElementBySId(SBMLDocument document, String id)
	{
		return getElementBySId(document.getModel(), id);
	}

	public static SBase getElementByMetaId(Model m, String metaId)
	{
		return getElementByMetaId(m.getSBMLDocument(), metaId);
	}

	public static SBase getElementBySId(Model m, String id)
	{
		return m.findNamedSBase(id);
	}

	public static SBase getElementByMetaId(CompSBMLDocumentPlugin compDocument, String metaId)
	{
		for (SBase sb : getListOfAllElements(compDocument))
		{
			if (sb.getMetaId().equals(metaId))
			{
				return sb;
			}
		}
		return null;
	}

	public static ArrayList<SBase> getListOfAllElements(TreeNode node)
	{
		ArrayList<SBase> elements = new ArrayList<SBase>();
		if (node instanceof SBase)
		{
			elements.add((SBase) node);
		}
		for (int i = 0; i < node.getChildCount(); i++)
		{
			elements.addAll(getListOfAllElements(node.getChildAt(i)));
		}
		return elements;
	}

	public static EventAssignment getEventAssignmentByVariable(Event event, String variable)
	{
		for (int i = 0; i < event.getEventAssignmentCount(); i++)
		{
			EventAssignment ea = event.getListOfEventAssignments().get(i);
			if (ea.getVariable().equals(variable))
			{
				return ea;
			}
		}
		return null;
	}

	// public static ListOf<SBase> getListOfAllElements(Model m) {
	// ListOf<SBase> elements = new ListOf<SBase>();
	// for (Compartment c : m.getListOfCompartments()) {
	// elements.add(c);
	// }
	// for (Constraint c : m.getListOfConstraints()) {
	// elements.add(c);
	// }
	// for (Event e : m.getListOfEvents()) {
	// elements.add(e);
	// for (EventAssignment ea : e.getListOfEventAssignments()) {
	// elements.add(ea);
	// }
	// }
	// for (FunctionDefinition fd : m.getListOfFunctionDefinitions()) {
	// elements.add(fd);
	// }
	// for (InitialAssignment ia : m.getListOfInitialAssignments()) {
	// elements.add(ia);
	// }
	// for (Parameter p : m.getListOfParameters()) {
	// elements.add(p);
	// }
	// for (UnitDefinition ud : m.getListOfPredefinedUnitDefinitions()) {
	// elements.add(ud);
	// }
	// for (Reaction r : m.getListOfReactions()) {
	// elements.add(r);
	// for (ModifierSpeciesReference msr : r.getListOfModifiers()) {
	// elements.add(msr);
	// }
	// for (SpeciesReference sr : r.getListOfProducts()) {
	// elements.add(sr);
	// }
	// for (SpeciesReference sr : r.getListOfReactants()) {
	// elements.add(sr);
	// }
	// for (LocalParameter lp : r.getKineticLaw().getListOfLocalParameters()) {
	// elements.add(lp);
	// }
	// }
	// for (Rule r : m.getListOfRules()) {
	// elements.add(r);
	// }
	// for (Species s : m.getListOfSpecies()) {
	// elements.add(s);
	// }
	// for (UnitDefinition ud : m.getListOfUnitDefinitions()) {
	// elements.add(ud);
	// }
	// return elements;
	// }

	public static String getId(SBase sb)
	{
		if (sb instanceof AbstractNamedSBase)
		{
			return ((AbstractNamedSBase) sb).getId();
		}
		return null;
	}

	public static int appendAnnotation(SBase sbmlObject, String annotation)
	{
		try
		{
			sbmlObject.getAnnotation().appendNonRDFAnnotation(annotation);
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
		return JSBML.OPERATION_SUCCESS;
	}

	public static int appendAnnotation(SBase sbmlObject, XMLNode annotation)
	{

		try
		{
			sbmlObject.getAnnotation().appendNonRDFAnnotation(annotation.toXMLString());
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
		return JSBML.OPERATION_SUCCESS;
	}

	public static FBCModelPlugin getFBCModelPlugin(Model model,boolean create)
	{
		if (model.getExtension(FBCConstants.namespaceURI) != null)
		{
			FBCModelPlugin fbc = (FBCModelPlugin) model.getExtension(FBCConstants.namespaceURI);
			fbc.setStrict(false);
			return fbc;
		}
		if (create) {
			FBCModelPlugin fbc = new FBCModelPlugin(model);
			fbc.setStrict(false);
			model.addExtension(FBCConstants.namespaceURI, fbc);
			return fbc;
		}
		return null;
	}

	public static DistribFunctionDefinitionPlugin getDistribFunctionDefinitionPlugin(FunctionDefinition function)
	{
		if (function.getExtension(DistribConstants.namespaceURI) != null)
		{
			return (DistribFunctionDefinitionPlugin) function.getExtension(DistribConstants.namespaceURI);
		}
		DistribFunctionDefinitionPlugin distrib = new DistribFunctionDefinitionPlugin(function);
		function.addExtension(DistribConstants.namespaceURI, distrib);
		return distrib;
	}

	public static LayoutModelPlugin getLayoutModelPlugin(Model model)
	{
		if (model.getExtension(LayoutConstants.namespaceURI) != null)
		{
			return (LayoutModelPlugin) model.getExtension(LayoutConstants.namespaceURI);
		}
		LayoutModelPlugin layout = new LayoutModelPlugin(model);
		model.addExtension(LayoutConstants.namespaceURI, layout);
		return layout;
	}

	public static CompSBMLDocumentPlugin getCompSBMLDocumentPlugin(SBMLDocument document)
	{
		if (document.getExtension(CompConstants.namespaceURI) != null)
		{
			return (CompSBMLDocumentPlugin) document.getExtension(CompConstants.namespaceURI);
		}
		CompSBMLDocumentPlugin comp = new CompSBMLDocumentPlugin(document);
		document.addExtension(CompConstants.namespaceURI, comp);
		return comp;
	}

	public static CompModelPlugin getCompModelPlugin(Model model)
	{
		if (model.getExtension(CompConstants.namespaceURI) != null)
		{
			return (CompModelPlugin) model.getExtension(CompConstants.namespaceURI);
		}
		CompModelPlugin comp = new CompModelPlugin(model);
		model.addExtension(CompConstants.namespaceURI, comp);
		return comp;
	}

	public static CompSBasePlugin getCompSBasePlugin(SBase sb)
	{
		if (sb.getExtension(CompConstants.namespaceURI) != null)
		{
			return (CompSBasePlugin) sb.getExtension(CompConstants.namespaceURI);
		}
		CompSBasePlugin comp = new CompSBasePlugin(sb);
		sb.addExtension(CompConstants.namespaceURI, comp);
		return comp;
	}

	public static FBCReactionPlugin getFBCReactionPlugin(Reaction r)
	{
		if (r.getExtension(FBCConstants.namespaceURI) != null)
		{
			return (FBCReactionPlugin) r.getExtension(FBCConstants.namespaceURI);
		}
		FBCReactionPlugin reac = new FBCReactionPlugin(r);
		r.addExtension(FBCConstants.namespaceURI, reac);
		return reac;
	}

	public static ArraysSBasePlugin getArraysSBasePlugin(SBase sb)
	{
		if (sb.getExtension(ArraysConstants.namespaceURI) != null)
		{
			return (ArraysSBasePlugin) sb.getExtension(ArraysConstants.namespaceURI);
		}
		ArraysSBasePlugin arrays = new ArraysSBasePlugin(sb);
		sb.addExtension(ArraysConstants.namespaceURI, arrays);
		return arrays;
	}

	public static boolean dimensionsMatch(SBase sbase1, SBase sbase2)
	{
		ArraysSBasePlugin sbasePlugin1 = SBMLutilities.getArraysSBasePlugin(sbase1);
		ArraysSBasePlugin sbasePlugin2 = SBMLutilities.getArraysSBasePlugin(sbase2);
		if (sbasePlugin1.getDimensionCount() != sbasePlugin2.getDimensionCount())
		{
			return false;
		}
		for (int i = 0; i < sbasePlugin1.getDimensionCount(); i++)
		{
			Dimension dim1 = sbasePlugin1.getDimensionByArrayDimension(i);
			Dimension dim2 = sbasePlugin1.getDimensionByArrayDimension(i);
			if (dim1 == null || dim2 == null)
			{
				return false;
			}
			if (dim1.getId() != dim2.getId())
			{
				return false;
			}
			if (dim1.getSize() != dim2.getSize())
			{
				return false;
			}
		}
		return true;
	}

	public static void setNamespaces(SBMLDocument document, Map<String, String> namespaces)
	{
		document.getDeclaredNamespaces().clear();
		for (String key : namespaces.keySet())
		{
			String prefix = "";
			// String shortName = key;
			if (key.contains(":"))
			{
				prefix = key.split(":")[0];
				// shortName = key.split(":")[1];
			}
			document.addDeclaredNamespace(prefix, namespaces.get(key));
		}
	}

	public static boolean getBooleanFromDouble(double value)
	{

		if (value == 0.0)
		{
			return false;
		}
		return true;
	}

	public static double getDoubleFromBoolean(boolean value)
	{

		if (value == true)
		{
			return 1.0;
		}
		return 0.0;
	}

	public static double evaluateExpression(Model model, ASTNode node)
	{
		PsRandom prng = new PsRandom();
		if (node.isBoolean())
		{

			switch (node.getType())
			{

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpression(model, node.getLeftChild()))));

			case LOGICAL_AND:
			{

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					andResult = andResult && getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter)));
				}

				return getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR:
			{

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					orResult = orResult || getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter)));
				}

				return getDoubleFromBoolean(orResult);
			}

			case LOGICAL_XOR:
			{

				boolean xorResult = getBooleanFromDouble(evaluateExpression(model, node.getChild(0)));

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
				{
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter)));
				}

				return getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return getDoubleFromBoolean(evaluateExpression(model, node.getLeftChild()) == evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_NEQ:
				return getDoubleFromBoolean(evaluateExpression(model, node.getLeftChild()) != evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_GEQ:
			{
				// System.out.println("Node: " +
				// libsbml.formulaToString(node.getRightChild()) + " " +
				// evaluateExpressionRecursive(modelstate,
				// node.getRightChild()));
				// System.out.println("Node: " +
				// evaluateExpressionRecursive(modelstate, node.getLeftChild())
				// + " " + evaluateExpressionRecursive(modelstate,
				// node.getRightChild()));

				return getDoubleFromBoolean(evaluateExpression(model, node.getLeftChild()) >= evaluateExpression(model, node.getRightChild()));
			}
			case RELATIONAL_LEQ:
				return getDoubleFromBoolean(evaluateExpression(model, node.getLeftChild()) <= evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_GT:
				return getDoubleFromBoolean(evaluateExpression(model, node.getLeftChild()) > evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_LT:
			{
				return getDoubleFromBoolean(evaluateExpression(model, node.getLeftChild()) < evaluateExpression(model, node.getRightChild()));
			}
			default:
			}
		}

		// if it's a mathematical constant
		else if (node.isConstant())
		{

			switch (node.getType())
			{

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
				return Math.PI;

			default:
			}
		}
		else if (node.isInteger())
		{
			return node.getInteger();
		}
		else if (node.isReal())
		{
			return node.getReal();
		}
		else if (node.isName())
		{

			SBase sb = getElementBySId(model, node.getName());
			if (sb instanceof QuantityWithUnit)
			{
				return ((QuantityWithUnit) sb).getValue();
			}
		}

		// operators/functions with two children
		else
		{

			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			switch (node.getType())
			{

			case PLUS:
			{

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					sum += evaluateExpression(model, node.getChild(childIter));
				}

				return sum;
			}

			case MINUS:
			{

				double sum = evaluateExpression(model, leftChild);

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
				{
					sum -= evaluateExpression(model, node.getChild(childIter));
				}

				return sum;
			}

			case TIMES:
			{

				double product = 1.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
				{
					product *= evaluateExpression(model, node.getChild(childIter));
				}

				return product;
			}

			case DIVIDE:
				return (evaluateExpression(model, leftChild) / evaluateExpression(model, rightChild));

			case FUNCTION_POWER:
				return (FastMath.pow(evaluateExpression(model, leftChild), evaluateExpression(model, rightChild)));

			case FUNCTION:
			{
				// use node name to determine function
				// i'm not sure what to do with completely user-defined
				// functions, though
				String nodeName = node.getName();

				// generates a uniform random number between the upper and lower
				// bound
				if (nodeName.equals("uniform"))
				{

					double leftChildValue = evaluateExpression(model, node.getLeftChild());
					double rightChildValue = evaluateExpression(model, node.getRightChild());
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);

					return prng.nextDouble(lowerBound, upperBound);
				}
				else if (nodeName.equals("exponential"))
				{

					return prng.nextExponential(evaluateExpression(model, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("gamma"))
				{

					return prng.nextGamma(1, evaluateExpression(model, node.getLeftChild()), evaluateExpression(model, node.getRightChild()));
				}
				else if (nodeName.equals("chisq"))
				{

					return prng.nextChiSquare((int) evaluateExpression(model, node.getLeftChild()));
				}
				else if (nodeName.equals("lognormal"))
				{

					return prng.nextLogNormal(evaluateExpression(model, node.getLeftChild()), evaluateExpression(model, node.getRightChild()));
				}
				else if (nodeName.equals("laplace"))
				{

					// function doesn't exist in current libraries
					return 0;
				}
				else if (nodeName.equals("cauchy"))
				{

					return prng.nextLorentzian(0, evaluateExpression(model, node.getLeftChild()));
				}
				else if (nodeName.equals("poisson"))
				{

					return prng.nextPoissonian(evaluateExpression(model, node.getLeftChild()));
				}
				else if (nodeName.equals("binomial"))
				{

					return prng.nextBinomial(evaluateExpression(model, node.getLeftChild()), (int) evaluateExpression(model, node.getRightChild()));
				}
				else if (nodeName.equals("bernoulli"))
				{

					return prng.nextBinomial(evaluateExpression(model, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("normal"))
				{

					return prng.nextGaussian(evaluateExpression(model, node.getLeftChild()), evaluateExpression(model, node.getRightChild()));
				}

				break;
			}

			case FUNCTION_ABS:
				return FastMath.abs(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCCOS:
				return FastMath.acos(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCSIN:
				return FastMath.asin(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCTAN:
				return FastMath.atan(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_CEILING:
				return FastMath.ceil(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_COS:
				return FastMath.cos(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_COSH:
				return FastMath.cosh(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_EXP:
				return FastMath.exp(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_FLOOR:
				return FastMath.floor(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_LN:
				return FastMath.log(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_LOG:
				return FastMath.log10(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_SIN:
				return FastMath.sin(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_SINH:
				return FastMath.sinh(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_TAN:
				return FastMath.tan(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_TANH:
				return FastMath.tanh(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_PIECEWISE:
			{

				// loop through child triples
				// if child 1 is true, return child 0, else return child 2
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 3)
				{

					if ((childIter + 1) < node.getChildCount() && getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter + 1))))
					{
						return evaluateExpression(model, node.getChild(childIter));
					}
					else if ((childIter + 2) < node.getChildCount())
					{
						return evaluateExpression(model, node.getChild(childIter + 2));
					}
				}

				return 0;
			}

			case FUNCTION_ROOT:
				return FastMath.pow(evaluateExpression(model, node.getRightChild()), 1 / evaluateExpression(model, node.getLeftChild()));

			case FUNCTION_SEC:
				return Fmath.sec(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_SECH:
				return Fmath.sech(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_FACTORIAL:
				return Fmath.factorial(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_COT:
				return Fmath.cot(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_COTH:
				return Fmath.coth(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_CSC:
				return Fmath.csc(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_CSCH:
				return Fmath.csch(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_DELAY:
				// NOT PLANNING TO SUPPORT THIS
				return 0;

			case FUNCTION_ARCTANH:
				return Fmath.atanh(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCSINH:
				return Fmath.asinh(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCCOSH:
				return Fmath.acosh(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCCOT:
				return Fmath.acot(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCCOTH:
				return Fmath.acoth(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCCSC:
				return Fmath.acsc(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCCSCH:
				return Fmath.acsch(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCSEC:
				return Fmath.asec(evaluateExpression(model, node.getChild(0)));

			case FUNCTION_ARCSECH:
				return Fmath.asech(evaluateExpression(model, node.getChild(0)));

			default:
			} // end switch

		}
		return 0.0;
	}

	public static void setMetaId(AbstractSBase asb, String newId)
	{
		if (!asb.getMetaId().equals(newId))
		{
			asb.setMetaId(newId);
		}
	}

	public static void setMetaId(SBase asb, String newId)
	{
		if (!asb.getMetaId().equals(newId))
		{
			asb.setMetaId(newId);
		}
	}

	/*
	 * public static LocalParameter getLocalParameter(KineticLaw k,String id) {
	 * for (int i = 0; i < k.getLocalParameterCount(); i++) { if
	 * (k.getLocalParameter(i).getId().equals(id)) { return
	 * k.getLocalParameter(i); } } return null; }
	 */

	// on-screen schematic display
	public static String getArrayId(SBMLDocument document, String id)
	{
		String arrayId = id;
		SBase sBase = getElementBySId(document, id);
		if (sBase == null)
		{
			sBase = getElementByMetaId(document, id);
			if (sBase == null)
			{
				return arrayId;
			}
		}
		String dimInID = SBMLutilities.getDimensionString(sBase);
		return arrayId + dimInID;
	}
	
	public static String getSpeciesReferenceIndexedId(SBase sBase)
	{
		String indexStr = "";
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sBase);
		for(int j = sBasePlugin.getIndexCount()-1; j>=0; j--){
			Index index = sBasePlugin.getIndex(j,"species");
			if (index!=null)
				indexStr += "[" + SBMLutilities.myFormulaToString(index.getMath()) + "]";
		}
		return indexStr;
	}

	public static ModifierSpeciesReference removeModifier(Reaction r, String species)
	{
		if (r.getListOfModifiers() != null)
		{
			return r.removeModifier(species);
		}
		return null;
	}

	public static void checkModelCompleteness(SBMLDocument document,boolean addDefaults)
	{
		JTextArea messageArea = new JTextArea();
		if (addDefaults) {
			messageArea.append("Model is incomplete.  Default values being added.\n");
		} else {
			messageArea.append("Model is incomplete.  Cannot be simulated until the following information is provided.\n");
		}
		boolean display = false;
		org.sbml.jsbml.Model model = document.getModel();
		for (int i = 0; i < model.getCompartmentCount(); i++)
		{
			Compartment compartment = model.getCompartment(i);
			if (!compartment.isSetSize() && model.getInitialAssignment(compartment.getId())==null)
			{
				messageArea.append("--------------------------------------------------------------------------\n");
				if (addDefaults) {
					messageArea.append("Compartment " + compartment.getId() + " needs a size, setting to 1.0.\n");
					compartment.setSize(1.0);
				} else {
					messageArea.append("Compartment " + compartment.getId() + " needs a size.\n");
				}
				display = true;
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++)
		{
			Species species = model.getSpecies(i);
			if (!(species.isSetInitialAmount()) && !(species.isSetInitialConcentration())
					 && model.getInitialAssignment(species.getId())==null)
			{
				messageArea.append("--------------------------------------------------------------------------\n");
				if (addDefaults) {
					messageArea.append("Species " + species.getId() + " needs an initial amount, setting to 0.\n");
					species.setInitialAmount(0.0);
				} else {
					messageArea.append("Species " + species.getId() + " needs an initial amount or concentration.\n");
				}
				display = true;
			}
		}
		for (int i = 0; i < model.getParameterCount(); i++)
		{
			Parameter parameter = model.getParameter(i);
			if (!(parameter.isSetValue()) && model.getInitialAssignment(parameter.getId())==null)
			{
				messageArea.append("--------------------------------------------------------------------------\n");
				if (addDefaults) {
					messageArea.append("Parameter " + parameter.getId() + " needs an initial value, setting to 0.\n");
					parameter.setValue(0.0);
				} else {
					messageArea.append("Parameter " + parameter.getId() + " needs an initial value.\n");
				}
				display = true;
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++)
		{
			Reaction reaction = model.getReaction(i);
			for (SpeciesReference reactant : reaction.getListOfReactants()) {
				if (!reactant.isSetStoichiometry() || reactant.getStoichiometry()==Double.NaN) {
					messageArea.append("--------------------------------------------------------------------------\n");
					if (addDefaults) {
						messageArea.append("Reaction " + reaction.getId() + " reactant " + reactant.getSpecies() + " needs a stoichiometry, setting to 1.0.\n");
						reactant.setStoichiometry(1.0);
					} else {
						messageArea.append("Reaction " + reaction.getId() + " reactant " + reactant.getSpecies() + " needs a stoichiometry.\n");
					}
					display = true;
				}
			}
			for (SpeciesReference product : reaction.getListOfProducts()) {
				if (!product.isSetStoichiometry() || product.getStoichiometry()==Double.NaN) {
					messageArea.append("--------------------------------------------------------------------------\n");
					if (addDefaults) {
						messageArea.append("Reaction " + reaction.getId() + " product " + product.getSpecies() + " needs a stoichiometry, setting to 1.0.\n");
						product.setStoichiometry(1.0);
					} else {
						messageArea.append("Reaction " + reaction.getId() + " product " + product.getSpecies() + " needs a stoichiometry.\n");
					}
					display = true;
				}
			}
			FBCReactionPlugin rBounds = SBMLutilities.getFBCReactionPlugin(reaction);
			if (rBounds.isSetLowerFluxBound() || rBounds.isSetUpperFluxBound()) {
				continue;
			}
			if (!(reaction.isSetKineticLaw()))
			{
				messageArea.append("--------------------------------------------------------------------------\n");
				if (addDefaults) {
					messageArea.append("Reaction " + reaction.getId() + " needs a kinetic law, creating one.\n");
					KineticLaw kl = reaction.createKineticLaw();
					kl.setMath(SBMLutilities.myParseFormula("NaN"));
				} else {
					messageArea.append("Reaction " + reaction.getId() + " needs a kinetic law.\n");
				}
				display = true;
			}
			else
			{
				for (int j = 0; j < reaction.getKineticLaw().getLocalParameterCount(); j++)
				{
					LocalParameter param = reaction.getKineticLaw().getLocalParameter(j);
					if (!(param.isSetValue()))
					{
						messageArea.append("--------------------------------------------------------------------------\n");
						if (addDefaults) {
							messageArea.append("Local parameter " + param.getId() + " for reaction " + reaction.getId() + " needs an initial value, setting to 0.\n");
							param.setValue(0.0);
						} else {
							messageArea.append("Local parameter " + param.getId() + " for reaction " + reaction.getId() + " needs an initial value.\n");
						}
						display = true;
					}
				}
			}
		}
//		if (display)
//		{
//			messageArea.setLineWrap(true);
//			messageArea.setEditable(false);
//			messageArea.setSelectionStart(0);
//			messageArea.setSelectionEnd(0);
//			JScrollPane scroll = new JScrollPane();
//			scroll.setMinimumSize(new java.awt.Dimension(600, 600));
//			scroll.setPreferredSize(new java.awt.Dimension(600, 600));
//			scroll.setViewportView(messageArea);
//			JOptionPane.showMessageDialog(Gui.frame, scroll, "SBML Model Completeness Errors", JOptionPane.ERROR_MESSAGE);
//
//		}
	}

	// TODO: should weave in better with existing conversion
	private static void convertToL3(org.sbml.libsbml.SBMLDocument doc)
	{
		if (doc == null || doc.getModel() == null)
		{
			return;
		}

		String layoutNsUri = "http://www.sbml.org/sbml/level3/version1/layout/version1";
		// String renderNsUri =
		// "http://www.sbml.org/sbml/level3/version1/render/version1";

		org.sbml.libsbml.LayoutModelPlugin plugin = (org.sbml.libsbml.LayoutModelPlugin) doc.getModel().getPlugin("layout");

		// bail if we don't have layout
		if (plugin == null)
		{
			return;
		}

		// convert document
		ConversionProperties prop = new ConversionProperties(new SBMLNamespaces(3, 1));
		prop.addOption("strict", false);
		prop.addOption("setLevelAndVersion", true);
		prop.addOption("ignorePackages", true);

		if (doc.convert(prop) != libsbml.LIBSBML_OPERATION_SUCCESS)
		{
			System.err.println("Conversion failed!");
			doc.printErrors();
			// System.exit(2);
		}

		// add new layout namespace and set required flag
		SBasePlugin docPlugin = doc.getPlugin("layout");

		// if we don't have layout there isnothing to do
		if (docPlugin == null)
		{
			return;
		}

		docPlugin.setElementNamespace(layoutNsUri);

		doc.getSBMLNamespaces().addPackageNamespace("layout", 1);
		doc.setPackageRequired("layout", false);

		// TODO: restore when we have render support
		// add enable render if needed
		/*
		 * SBasePlugin rdocPlugin = doc.getPlugin("render"); if (rdocPlugin !=
		 * null) { doc.getSBMLNamespaces().addPackageNamespace("render", 1); }
		 * else { doc.enablePackage(renderNsUri, "render", true); }
		 * doc.setPackageRequired("render", false);
		 */

	}
	
	private static SBMLDocument convertToFBCVersion2(String filename,SBMLDocument document) throws XMLStreamException, IOException 
	{
		if (document.getDeclaredNamespaces().get("xmlns:"+FBCConstants.shortLabel)!=null &&
				document.getDeclaredNamespaces().get("xmlns:"+FBCConstants.shortLabel).endsWith("1")) 
		{
			if (!SBMLutilities.libsbmlFound)
			{
			  SBMLutilities.message.setErrorDialog("Error Opening File", "Unable convert FBC model from Version 1 to Version 2.");
				return null;
			}
			//long numErrors = 0;
			org.sbml.libsbml.SBMLReader reader = new org.sbml.libsbml.SBMLReader();
			org.sbml.libsbml.SBMLDocument doc = reader.readSBML(filename);
			/* create a new conversion properties structure */
			ConversionProperties props = new ConversionProperties(new SBMLNamespaces(3, 1));

			/* add an option that we want to strip a given package */
			boolean strict = false;
			props.addOption("convert fbc v1 to fbc v2", true, "convert fbc v1 to fbc v2");
			props.addOption("strict", strict, "should the model be a strict one (i.e.: all non-specified bounds will be filled)");

			/* perform the conversion */
			if (doc.convert(props) != libsbml.LIBSBML_OPERATION_SUCCESS)
			{
				System.err.println("Conversion failed!");
				doc.printErrors();
				// System.exit(2);
			}
			org.sbml.libsbml.SBMLWriter writer = new org.sbml.libsbml.SBMLWriter();
			writer.writeSBMLToFile(doc, filename);
			document = SBMLReader.read(new File(filename));
		} else {
			SBMLutilities.getFBCModelPlugin(document.getModel(),true);
		}
		return document;
	}

	public static SBMLDocument readSBML(String filename) throws XMLStreamException, IOException
	{
		SBMLDocument document = null;
			document = SBMLReader.read(new File(filename));
		if (document.getModel().isSetId())
		{
			document.getModel().setId(document.getModel().getId().replace(".", "_"));
		}

		if (document.getLevel() < GlobalConstants.SBML_LEVEL || document.getVersion() < GlobalConstants.SBML_VERSION)
		{
			if (!SBMLutilities.libsbmlFound)
			{
				document.setLevelAndVersion(GlobalConstants.SBML_LEVEL, GlobalConstants.SBML_VERSION, false);
				SBMLWriter Xwriter = new SBMLWriter();
				Xwriter.writeSBMLToFile(document, filename);
				return document;
				
			}
			long numErrors = 0;
			org.sbml.libsbml.SBMLReader reader = new org.sbml.libsbml.SBMLReader();
			org.sbml.libsbml.SBMLDocument doc = reader.readSBML(filename);
			numErrors = doc.checkL3v1Compatibility();
			if (numErrors > 0)
			{
				String message = "Conversion to SBML level " + GlobalConstants.SBML_LEVEL + " version " + GlobalConstants.SBML_VERSION
						+ " produced the errors listed below.";
				message += "It is recommended that you fix them before using these models or you may get unexpected results.\n\n";
				message += "--------------------------------------------------------------------------------------\n";
				message += filename;
				message += "\n--------------------------------------------------------------------------------------\n\n";
				for (int i = 0; i < numErrors; i++)
				{
					String error = doc.getError(i).getMessage();
					message += i + ":" + error + "\n";
				}
				SBMLutilities.message.setConsole(message);
			}
			convertToL3(doc);
			doc.setLevelAndVersion(GlobalConstants.SBML_LEVEL, GlobalConstants.SBML_VERSION, false);
			/*
			 * for (int i = 0; i < doc.getNumErrors(); i++) {
			 * System.out.println(doc.getError(i).getMessage()); }
			 */
			org.sbml.libsbml.SBMLWriter writer = new org.sbml.libsbml.SBMLWriter();
			writer.writeSBMLToFile(doc, filename);
			document = SBMLReader.read(new File(filename));
		}
		document = convertToFBCVersion2(filename,document);
		return document;
	}

	// Note that the ID for a port that refers to a reaction can be the same as
	// the reaction's ID
	public static void addDeletion(Submodel submodel, String subPortId, String[] dimensions, String[] dimensionIds, String[] indices)
	{
		Deletion deletion = SBMLutilities.getDeletionByPortRef(submodel, subPortId);
		if (deletion == null)
		{
			deletion = submodel.createDeletion();
			deletion.setId("delete_" + subPortId);
			deletion.setPortRef(subPortId);
		}
		SBMLutilities.createDimensions(deletion, dimensionIds, dimensions);
		SBMLutilities.addIndices(deletion, "comp:portRef", indices, 1);
	}

	public static void createDimensions(SBase sBase, String[] dimensionIds, String[] dimensionSizes) {
		ArraysSBasePlugin sBasePlugin = SBMLutilities.getArraysSBasePlugin(sBase);
		sBasePlugin.unsetListOfDimensions();
		for(int i = 0; dimensionSizes!=null && i<dimensionSizes.length-1; i++){
			org.sbml.jsbml.ext.arrays.Dimension dimX = sBasePlugin.createDimension(dimensionIds[i]);
			dimX.setSize(dimensionSizes[i+1]);
			dimX.setArrayDimension(i);
		}
	}

	public static void addImplicitDeletions(Submodel submodel, BioModel subBiomodel, String subSpeciesId, String[] dimensions, String[] dimensionIds,
			String[] indices)
	{
		ListOf<Port> subPorts = subBiomodel.getSBMLCompModel().getListOfPorts();
		Reaction subDegradation = subBiomodel.getDegradationReaction(subSpeciesId);
		if (subDegradation != null && subPorts.get(subDegradation.getId()) != null)
		{
			addDeletion(submodel, subDegradation.getId(), dimensions, dimensionIds, indices);
		}
		Reaction subDiffusion = subBiomodel.getDiffusionReaction(subSpeciesId);
		if (subDiffusion != null && subPorts.get(subDiffusion.getId()) != null)
		{
			addDeletion(submodel, subDiffusion.getId(), dimensions, dimensionIds, indices);
		}
		Reaction subConstitutive = subBiomodel.getConstitutiveReaction(subSpeciesId);
		if (subConstitutive != null && subPorts.get(subConstitutive.getId()) != null)
		{
			addDeletion(submodel, subConstitutive.getId(), dimensions, dimensionIds, indices);
		}
		Reaction subComplexation = subBiomodel.getComplexReaction(subSpeciesId);
		if (subComplexation != null && subPorts.get(subComplexation.getId()) != null)
		{
			addDeletion(submodel, subComplexation.getId(), dimensions, dimensionIds, indices);
		}
		Reaction subProduction = subBiomodel.getProductionReaction(subSpeciesId);
		if (subProduction != null && subPorts.get(subProduction.getId()) != null)
		{
			addDeletion(submodel, subProduction.getId(), dimensions, dimensionIds, indices);
		}
	}

	public static Deletion getDeletionByPortRef(Submodel submodel, String portRef)
	{
		for (int i = 0; i < submodel.getListOfDeletions().size(); i++)
		{
			Deletion d = submodel.getListOfDeletions().get(i);
			if (d.getPortRef().equals(portRef))
			{
				return d;
			}
		}
		return null;
	}

	public static void addReplacement(SBase sbmlElement, Submodel submodel, String subModelId, String subPortId, String convFactor,
			String[] dimensions, String[] dimensionIds, String[] portIndices, String[] subModelIndices, boolean deletion)
	{
		CompSBasePlugin compElement = getCompSBasePlugin(sbmlElement);
		ReplacedElement replacement = compElement.createReplacedElement();
		replacement.setSubmodelRef(subModelId);
		if (!deletion)
		{
			replacement.setPortRef(subPortId);
		}
		else
		{
			addDeletion(submodel, subPortId, dimensions, dimensionIds, portIndices);
			replacement.setDeletion("delete_" + subPortId);
		}
		if (!convFactor.equals("(none)"))
		{
			replacement.setConversionFactor(convFactor);
		}
		SBMLutilities.createDimensions(replacement, dimensionIds, dimensions);
		ArraysSBasePlugin sBasePlugin = getArraysSBasePlugin(replacement);
		sBasePlugin.unsetListOfIndices();
		for (int i = 0; portIndices != null && i < portIndices.length - 1; i++)
		{
			Index indexRule = sBasePlugin.createIndex();
			indexRule.setArrayDimension(i);
			indexRule.setReferencedAttribute("comp:portRef");
			ASTNode indexMath = myParseFormula(portIndices[i + 1]);
			indexRule.setMath(indexMath);
		}
		for (int i = 0; subModelIndices != null && i < subModelIndices.length - 1; i++)
		{
			Index indexRule = sBasePlugin.createIndex();
			indexRule.setArrayDimension(i);
			indexRule.setReferencedAttribute("comp:submodelRef");
			ASTNode indexMath = myParseFormula(subModelIndices[i + 1]);
			indexRule.setMath(indexMath);
		}
	}

	public static void addReplacedBy(SBase sbmlElement, String subModelId, String subPortId, String[] dimensions, String[] dimensionIds,
			String[] portIndices, String[] subModelIndices)
	{
		CompSBasePlugin compElement = getCompSBasePlugin(sbmlElement);
		ReplacedBy replacement = compElement.createReplacedBy();
		replacement.setSubmodelRef(subModelId);
		replacement.setPortRef(subPortId);
		SBMLutilities.createDimensions(replacement, dimensionIds, dimensions);
		ArraysSBasePlugin sBasePlugin = getArraysSBasePlugin(replacement);
		sBasePlugin.unsetListOfIndices();
		for (int i = 0; portIndices != null && i < portIndices.length - 1; i++)
		{
			Index indexRule = sBasePlugin.createIndex();
			indexRule.setArrayDimension(i);
			indexRule.setReferencedAttribute("comp:portRef");
			ASTNode indexMath = myParseFormula(portIndices[i + 1]);
			indexRule.setMath(indexMath);
		}
		for (int i = 0; subModelIndices != null && i < subModelIndices.length - 1; i++)
		{
			Index indexRule = sBasePlugin.createIndex();
			indexRule.setArrayDimension(i);
			indexRule.setReferencedAttribute("comp:submodelRef");
			ASTNode indexMath = myParseFormula(subModelIndices[i + 1]);
			indexRule.setMath(indexMath);
		}
	}

	public static void addImplicitReplacedBys(String submodelId, BioModel bioModel, BioModel subBiomodel, String speciesId, String subSpeciesId,
			String[] dimensions, String[] dimensionIds, String[] portIndices, String[] subModelIndices)
	{
		ListOf<Port> subPorts = subBiomodel.getSBMLCompModel().getListOfPorts();
		Reaction degradation = bioModel.getDegradationReaction(speciesId);
		if (degradation != null)
		{
			Reaction subDegradation = subBiomodel.getDegradationReaction(subSpeciesId);
			if (subDegradation != null && subPorts.get(subDegradation.getId()) != null)
			{
				addReplacedBy(degradation, submodelId, subDegradation.getId(), dimensions, dimensionIds, portIndices, subModelIndices);
			}
		}
		Reaction diffusion = bioModel.getDiffusionReaction(speciesId);
		if (diffusion != null)
		{
			Reaction subDiffusion = subBiomodel.getDiffusionReaction(subSpeciesId);
			if (subDiffusion != null && subPorts.get(subDiffusion.getId()) != null)
			{
				addReplacedBy(diffusion, submodelId, subDiffusion.getId(), dimensions, dimensionIds, portIndices, subModelIndices);
			}
		}
		Reaction constitutive = bioModel.getConstitutiveReaction(speciesId);
		if (constitutive != null)
		{
			Reaction subConstitutive = subBiomodel.getConstitutiveReaction(subSpeciesId);
			if (subConstitutive != null && subPorts.get(subConstitutive.getId()) != null)
			{
				addReplacedBy(constitutive, submodelId, subConstitutive.getId(), dimensions, dimensionIds, portIndices, subModelIndices);
			}
		}
		Reaction complexation = bioModel.getComplexReaction(speciesId);
		if (complexation != null)
		{
			Reaction subComplexation = subBiomodel.getComplexReaction(subSpeciesId);
			if (subComplexation != null && subPorts.get(subComplexation.getId()) != null)
			{
				addReplacedBy(complexation, submodelId, subComplexation.getId(), dimensions, dimensionIds, portIndices, subModelIndices);
			}
		}
		Reaction production = bioModel.getProductionReaction(speciesId);
		if (production != null)
		{
			Reaction subProduction = subBiomodel.getProductionReaction(subSpeciesId);
			if (subProduction != null && subPorts.get(subProduction.getId()) != null)
			{
				addReplacedBy(production, submodelId, subProduction.getId(), dimensions, dimensionIds, portIndices, subModelIndices);
			}
		}
	}

	public static void addImplicitReplacementsDeletions(Submodel submodel, BioModel bioModel, BioModel subBioModel)
	{
		for (int i = 0; i < submodel.getListOfDeletions().size(); i++)
		{
			Deletion deletion = submodel.getListOfDeletions().get(i);
			if (deletion.isSetPortRef())
			{
				String subSpeciesId = deletion.getPortRef().replace(GlobalConstants.INPUT + "__", "").replace(GlobalConstants.OUTPUT + "__", "");
				// TODO: need to fix nulls
				addImplicitDeletions(submodel, subBioModel, subSpeciesId, null, null, null);
			}
		}
		ListOf<Species> sbmlSpecies = bioModel.getSBMLDocument().getModel().getListOfSpecies();
		for (int i = 0; i < sbmlSpecies.size(); i++)
		{
			CompSBasePlugin compElement = (CompSBasePlugin) sbmlSpecies.get(i).getExtension(CompConstants.namespaceURI);
			if (compElement != null)
			{
				for (int j = 0; j < compElement.getListOfReplacedElements().size(); j++)
				{
					ReplacedElement remoteReplacement = compElement.getListOfReplacedElements().get(j);
					if (remoteReplacement.getSubmodelRef().equals(submodel.getId()) && remoteReplacement.isSetPortRef())
					{
						String subSpeciesId = remoteReplacement.getPortRef().replace(GlobalConstants.INPUT + "__", "")
								.replace(GlobalConstants.OUTPUT + "__", "");
						// TODO: need to fix nulls
						addImplicitDeletions(submodel, subBioModel, subSpeciesId, null, null, null);
					}
				}
				if (compElement.isSetReplacedBy())
				{
					ReplacedBy localReplacement = compElement.getReplacedBy();
					if (localReplacement.getSubmodelRef().equals(submodel.getId()) && localReplacement.isSetPortRef())
					{
						String subSpeciesId = localReplacement.getPortRef().replace(GlobalConstants.INPUT + "__", "")
								.replace(GlobalConstants.OUTPUT + "__", "");
						// TODO: need to fix nulls
						addImplicitReplacedBys(submodel.getId(), bioModel, subBioModel, sbmlSpecies.get(i).getId(), subSpeciesId, null, null, null,
								null);
						// sbmlElements =
						// SBMLutilities.getListOfAllElements(sbml.getModel());
					}
				}
			}
		}
	}

	public static void removeStaleReplacementsDeletions(Submodel submodel, BioModel bioModel, BioModel subBiomodel)
	{
		ListOf<Port> subPorts = subBiomodel.getSBMLCompModel().getListOfPorts();
		for (int i = 0; i < submodel.getListOfDeletions().size(); i++)
		{
			Deletion deletion = submodel.getListOfDeletions().get(i);
			if (deletion.isSetPortRef() && subPorts.get(deletion.getPortRef()) == null)
			{
				submodel.removeDeletion(deletion);
				i--;
			}
		}
		ArrayList<SBase> sbmlElements = getListOfAllElements(bioModel.getSBMLDocument().getModel());
		for (int i = 0; i < sbmlElements.size(); i++)
		{
			CompSBasePlugin compElement = (CompSBasePlugin) sbmlElements.get(i).getExtension(CompConstants.namespaceURI);
			if (compElement != null)
			{
				for (int j = 0; j < compElement.getListOfReplacedElements().size(); j++)
				{
					ReplacedElement remoteReplacement = compElement.getListOfReplacedElements().get(j);
					if (remoteReplacement.getSubmodelRef().equals(submodel.getId()) && remoteReplacement.isSetPortRef()
							&& subPorts.get(remoteReplacement.getPortRef()) == null)
					{
						compElement.removeReplacedElement(remoteReplacement);
						sbmlElements = getListOfAllElements(bioModel.getSBMLDocument().getModel());
						i--;
					}
				}
				if (compElement.isSetReplacedBy())
				{
					ReplacedBy localReplacement = compElement.getReplacedBy();
					if (localReplacement.getSubmodelRef().equals(submodel.getId()) && localReplacement.isSetPortRef()
							&& subPorts.get(localReplacement.getPortRef()) == null)
					{
						compElement.unsetReplacedBy();
						sbmlElements = getListOfAllElements(bioModel.getSBMLDocument().getModel());
						i--;
					}
				}
			}
		}
	}

	public static String changeIdToPortRef(String root, SBaseRef sbaseRef, BioModel bioModel) throws XMLStreamException, IOException
	{
		String id = "";
		if (sbaseRef.isSetSBaseRef())
		{
			BioModel subModel = new BioModel(root);
			Submodel submodel = bioModel.getSBMLCompModel().getListOfSubmodels().get(sbaseRef.getIdRef());
			String extModel = bioModel.getSBMLComp().getListOfExternalModelDefinitions().get(submodel.getModelRef()).getSource()
					.replace("file://", "").replace("file:", "").replace(".gcm", ".xml");
			subModel.load(root + GlobalConstants.separator + extModel);
			id += changeIdToPortRef(root, sbaseRef.getSBaseRef(), subModel);
			subModel.save(root + GlobalConstants.separator + extModel);
		}
		if (sbaseRef.isSetIdRef())
		{
			Port port = bioModel.getPortBySBaseRef(sbaseRef);
			SBase sbase = getElementBySId(bioModel.getSBMLDocument(), sbaseRef.getIdRef());
			if (sbase != null)
			{
				if (id.equals(""))
				{
					id = sbase.getElementName() + "__" + sbaseRef.getIdRef();
				}
				else
				{
					id = id + "__" + sbaseRef.getIdRef();
				}
				if (port == null)
				{
					port = bioModel.getSBMLCompModel().getPort(id);
					if (port == null)
					{
						port = bioModel.getSBMLCompModel().createPort();
					}
					port.setId(id);
					port.setIdRef(sbaseRef.getIdRef());
					if (sbaseRef.isSetSBaseRef())
					{
						port.setSBaseRef(sbaseRef.getSBaseRef().clone());
					}
				}
				sbaseRef.unsetIdRef();
				sbaseRef.unsetSBaseRef();
				sbaseRef.setPortRef(port.getId());
				return id;
			}
			return "";
		}
		if (sbaseRef.isSetMetaIdRef())
		{
			Port port = bioModel.getPortBySBaseRef(sbaseRef);
			SBase sbase = getElementByMetaId(bioModel.getSBMLDocument(), sbaseRef.getMetaIdRef());
			if (id.equals(""))
			{
				id = sbase.getElementName() + "__" + sbaseRef.getMetaIdRef();
			}
			else
			{
				id = id + "__" + sbaseRef.getMetaIdRef();
			}
			if (sbase != null)
			{
				if (port == null)
				{
					port = bioModel.getSBMLCompModel().createPort();
					port.setId(id);
					port.setMetaIdRef(sbaseRef.getMetaIdRef());
					port.setSBaseRef(sbaseRef.getSBaseRef());
				}
				sbaseRef.unsetMetaIdRef();
				sbaseRef.unsetSBaseRef();
				sbaseRef.setPortRef(port.getId());
				return id;
			}
		}
		return "";
	}

	public static boolean updatePortMap(String root, CompSBasePlugin sbmlSBase, BioModel subModel, String subModelId) throws XMLStreamException, IOException
	{
		boolean updated = false;
		if (sbmlSBase.isSetListOfReplacedElements())
		{
			for (int k = 0; k < sbmlSBase.getListOfReplacedElements().size(); k++)
			{
				ReplacedElement replacement = sbmlSBase.getListOfReplacedElements().get(k);
				if (replacement.getSubmodelRef().equals(subModelId))
				{
					changeIdToPortRef(root, replacement, subModel);
					updated = true;
				}
			}
		}
		if (sbmlSBase.isSetReplacedBy())
		{
			ReplacedBy replacement = sbmlSBase.getReplacedBy();
			if (replacement.getSubmodelRef().equals(subModelId))
			{
				changeIdToPortRef(root, replacement, subModel);
				updated = true;
			}
		}
		return updated;
	}

	public static boolean updateReplacementsDeletions(String root, SBMLDocument document, CompSBMLDocumentPlugin sbmlComp,
			CompModelPlugin sbmlCompModel) throws XMLStreamException, IOException
	{
		for (int i = 0; i < sbmlCompModel.getListOfSubmodels().size(); i++)
		{
			BioModel subModel = new BioModel(root);
			Submodel submodel = sbmlCompModel.getListOfSubmodels().get(i);
			String extModel = sbmlComp.getListOfExternalModelDefinitions().get(submodel.getModelRef()).getSource().replace("file://", "")
					.replace("file:", "").replace(".gcm", ".xml");
			subModel.load(root + GlobalConstants.separator + extModel);
			ArrayList<SBase> elements = getListOfAllElements(document.getModel());
			for (int j = 0; j < elements.size(); j++)
			{
				SBase sbase = elements.get(j);
				CompSBasePlugin sbmlSBase = (CompSBasePlugin) sbase.getExtension(CompConstants.namespaceURI);
				if (sbmlSBase != null)
				{
					if (updatePortMap(root, sbmlSBase, subModel, submodel.getId()))
					{
						elements = getListOfAllElements(document.getModel());
					}
				}
			}
			for (int j = 0; j < submodel.getListOfDeletions().size(); j++)
			{
				Deletion deletion = submodel.getListOfDeletions().get(j);
				changeIdToPortRef(root, deletion, subModel);
			}
			subModel.save(root + GlobalConstants.separator + extModel);
		}

		return true;
	}
	
	public static boolean isInput(Model model,String id) {
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(model);
		Port port = getPortByIdRef(sbmlCompModel,id);
		if (port != null) {
			return SBMLutilities.isInputPort(port);
		}
		return false;
	}
		
	public static boolean isOutput(Model model,String id) {
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(model);
		Port port = getPortByIdRef(sbmlCompModel,id);
		if (port != null) {
			return SBMLutilities.isOutputPort(port);
		}
		return false;
	}
	
	
	public static boolean isOnPort(Model model,String id) {
		CompModelPlugin sbmlCompModel = SBMLutilities.getCompModelPlugin(model);
		Port port = getPortByIdRef(sbmlCompModel,id);
		if (port != null) {
			return true;
		}
		return false;
	}	
	
	public static Port getPortByIdRef(CompModelPlugin sbmlCompModel,String idRef) {
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			if (port.isSetIdRef() && port.getIdRef().equals(idRef)) {
				return port;
			}
		}
		return null;
	}
	
	public static Port getPortByMetaIdRef(CompModelPlugin sbmlCompModel,String metaIdRef) {
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			if (port.isSetMetaIdRef() && port.getMetaIdRef().equals(metaIdRef)) {
				return port;
			}
		}
		return null;
	}
	
	public static Port getPortByUnitRef(CompModelPlugin sbmlCompModel,String unitIdRef) {
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			if (port.isSetUnitRef() && port.getUnitRef().equals(unitIdRef)) {
				return port;
			}
		}
		return null;
	}
	
	public static Port getPortBySBaseRef(CompModelPlugin sbmlCompModel,SBaseRef sbaseRef) {
		for (int i = 0; i < sbmlCompModel.getListOfPorts().size(); i++) {
			Port port = sbmlCompModel.getListOfPorts().get(i);
			if (port.isSetIdRef() && port.getIdRef().equals(sbaseRef.getIdRef()) &&
				((!port.isSetSBaseRef() && !sbaseRef.isSetSBaseRef()) ||
				(port.getSBaseRef().isSetPortRef() && sbaseRef.getSBaseRef().isSetPortRef() &&
					port.getSBaseRef().getPortRef().equals(sbaseRef.getSBaseRef().getPortRef())))) {
				return port;
			}
		}
		return null;
	}

	public static boolean isInputPort(Port port) {
		if (port.isSetSBOTerm() && port.getSBOTerm()==GlobalConstants.SBO_INPUT_PORT) {
			return true;
		} else if (port.isSetSBOTerm() && port.getSBOTerm()==GlobalConstants.SBO_OUTPUT_PORT) {
			return false;
		} else if (port.getId().startsWith(GlobalConstants.INPUT + "__")){
			port.setSBOTerm(GlobalConstants.SBO_INPUT_PORT);
			return true;
		}
		return false;
	}


	public static boolean isOutputPort(Port port) {
		if (port.isSetSBOTerm() && port.getSBOTerm()==GlobalConstants.SBO_OUTPUT_PORT) {
			return true;
		} else if (port.isSetSBOTerm() && port.getSBOTerm()==GlobalConstants.SBO_INPUT_PORT) {
			return false;
		} else if (port.getId().startsWith(GlobalConstants.INPUT + "__")){
			return false;
		}
		port.setSBOTerm(GlobalConstants.SBO_OUTPUT_PORT);
		return true;
	}


  public static Boolean			libsbmlFound		= true;
  public static Boolean			reb2sacFound		= true;
  public static Boolean			geneNetFound		= true;
}
