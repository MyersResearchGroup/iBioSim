package biomodel.util;

import java.awt.AWTError;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.TreeNode;
import javax.xml.stream.XMLStreamException;

import main.Gui;
import main.util.Utility;
import odk.lang.FastMath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ext.comp.CompModelPlugin;
import org.sbml.jsbml.ext.comp.CompSBMLDocumentPlugin;
import org.sbml.jsbml.ext.comp.CompConstant;
import org.sbml.jsbml.ext.comp.CompSBasePlugin;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FluxBound;
import org.sbml.jsbml.Compartment;
//CompartmentType not supported in Level 3
//import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.text.parser.FormulaParserLL3;
import org.sbml.jsbml.text.parser.IFormulaParser;
import org.sbml.jsbml.text.parser.ParseException;
import org.sbml.jsbml.validator.SBMLValidator;
import org.sbml.jsbml.xml.XMLNode;
import org.sbml.jsbml.AbstractNamedSBase;
import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.Annotation;
import org.sbml.jsbml.ExplicitRule;
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
import org.sbml.jsbml.JSBML;

import flanagan.math.Fmath;
import flanagan.math.PsRandom;
import biomodel.parser.BioModel;


public class SBMLutilities {

	/**
	 * Check that ID is valid and unique
	 */
	public static boolean checkID(SBMLDocument document, String ID, String selectedID, boolean isReacParam, boolean isMetaId) {
		Pattern IDpat = Pattern.compile("([a-zA-Z]|_)([a-zA-Z]|[0-9]|_)*");
		if (ID.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "An ID is required.", "Enter an ID", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (!(IDpat.matcher(ID).matches())) {
			JOptionPane.showMessageDialog(Gui.frame, "An ID can only contain letters, numbers, and underscores.", "Invalid ID",
					JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (ID.equals("t") || ID.equals("time") || ID.equals("true") || ID.equals("false") || ID.equals("notanumber") || ID.equals("pi")
				|| ID.equals("infinity") || ID.equals("exponentiale") || ID.equals("abs") || ID.equals("arccos") || ID.equals("arccosh")
				|| ID.equals("arcsin") || ID.equals("arcsinh") || ID.equals("arctan") || ID.equals("arctanh") || ID.equals("arccot")
				|| ID.equals("arccoth") || ID.equals("arccsc") || ID.equals("arccsch") || ID.equals("arcsec") || ID.equals("arcsech")
				|| ID.equals("acos") || ID.equals("acosh") || ID.equals("asin") || ID.equals("asinh") || ID.equals("atan") || ID.equals("atanh")
				|| ID.equals("acot") || ID.equals("acoth") || ID.equals("acsc") || ID.equals("acsch") || ID.equals("asec") || ID.equals("asech")
				|| ID.equals("cos") || ID.equals("cosh") || ID.equals("cot") || ID.equals("coth") || ID.equals("csc") || ID.equals("csch")
				|| ID.equals("ceil") || ID.equals("factorial") || ID.equals("exp") || ID.equals("floor") || ID.equals("ln") || ID.equals("log")
				|| ID.equals("sqr") || ID.equals("log10") || ID.equals("pow") || ID.equals("sqrt") || ID.equals("root") || ID.equals("piecewise")
				|| ID.equals("sec") || ID.equals("sech") || ID.equals("sin") || ID.equals("sinh") || ID.equals("tan") || ID.equals("tanh")
				|| ID.equals("and") || ID.equals("or") || ID.equals("xor") || ID.equals("not") || ID.equals("eq") || ID.equals("geq")
				|| ID.equals("leq") || ID.equals("gt") || ID.equals("neq") || ID.equals("lt") || ID.equals("delay")
				|| ((document.getLevel() > 2) && (ID.equals("avogadro")))) {
			JOptionPane.showMessageDialog(Gui.frame, "ID cannot be a reserved word.", "Illegal ID", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		if (!ID.equals(selectedID) && (getElementBySId(document, ID)!=null || getElementByMetaId(document, ID)!=null)) {	
			if (isReacParam) {
				JOptionPane.showMessageDialog(Gui.frame, "ID shadows a global ID.", "Not a Unique ID", JOptionPane.WARNING_MESSAGE);
			}
			else {
				JOptionPane.showMessageDialog(Gui.frame, "ID is not unique.", "Enter a Unique ID", JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Find invalid reaction variables in a formula
	 */
	public static ArrayList<String> getInvalidVariables(SBMLDocument document, String formula, String arguments, boolean isFunction) {
		ArrayList<String> validVars = new ArrayList<String>();
		ArrayList<String> invalidVars = new ArrayList<String>();
		Model model = document.getModel();
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
			validVars.add(model.getFunctionDefinition(i).getId());
		}
		if (!isFunction) {
			for (int i = 0; i < model.getSpeciesCount(); i++) {
				validVars.add(model.getSpecies(i).getId());
			}
		}
		if (isFunction) {
			String[] args = arguments.split(" |\\,");
			for (int i = 0; i < args.length; i++) {
				validVars.add(args[i]);
			}
		}
		else {
			for (int i = 0; i < model.getCompartmentCount(); i++) {
				if (document.getLevel() > 2 || model.getCompartment(i).getSpatialDimensions() != 0) {
					validVars.add(model.getCompartment(i).getId());
				}
			}
			for (int i = 0; i < model.getParameterCount(); i++) {
				validVars.add(model.getParameter(i).getId());
			}
			for (int i = 0; i < model.getReactionCount(); i++) {
				Reaction reaction = model.getReaction(i);
				validVars.add(reaction.getId());
				for (int j = 0; j < reaction.getReactantCount(); j++) {
					SpeciesReference reactant = reaction.getReactant(j);
					if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
						validVars.add(reactant.getId());
					}
				}
				for (int j = 0; j < reaction.getProductCount(); j++) {
					SpeciesReference product = reaction.getProduct(j);
					if ((product.isSetId()) && (!product.getId().equals(""))) {
						validVars.add(product.getId());
					}
				}
			}
			String[] kindsL3V1 = { "ampere", "avogadro", "becquerel", "candela", "celsius", "coulomb", "dimensionless", "farad", "gram", "gray", "henry",
					"hertz", "item", "joule", "katal", "kelvin", "kilogram", "litre", "lumen", "lux", "metre", "mole", "newton", "ohm", "pascal",
					"radian", "second", "siemens", "sievert", "steradian", "tesla", "volt", "watt", "weber" };
			for (int i = 0; i < kindsL3V1.length; i++) {
				validVars.add(kindsL3V1[i]);
			}
			for (int i = 0; i < model.getUnitDefinitionCount(); i++) {
				validVars.add(model.getListOfUnitDefinitions().get(i).getId());
			}
		}
		String[] splitLaw = formula.split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-|>|=|<|\\^|%|&|\\||!");
		for (int i = 0; i < splitLaw.length; i++) {
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
					|| ((document.getLevel() > 2) && (splitLaw[i].equals("avogadro")))) {
			}
			else {
				String temp = splitLaw[i];
				if (splitLaw[i].substring(splitLaw[i].length() - 1, splitLaw[i].length()).equals("e")) {
					temp = splitLaw[i].substring(0, splitLaw[i].length() - 1);
				}
				try {
					Double.parseDouble(temp);
				}
				catch (Exception e1) {
					if (!validVars.contains(splitLaw[i])) {
						if (splitLaw[i].equals("uniform")) {
							createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
						} else if (splitLaw[i].equals("normal")) {
							createFunction(model, "normal", "Normal distribution", "lambda(m,s,m)");
						} else if (splitLaw[i].equals("exponential")) {
							createFunction(model, "exponential", "Exponential distribution", "lambda(l,1/l)");
						} else if (splitLaw[i].equals("gamma")) {
							createFunction(model, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
						} else if (splitLaw[i].equals("lognormal")) {
							createFunction(model, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
						} else if (splitLaw[i].equals("chisq")) {
							createFunction(model, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
						} else if (splitLaw[i].equals("laplace")) {
							createFunction(model, "laplace", "Laplace distribution", "lambda(a,0)");
						} else if (splitLaw[i].equals("cauchy")) {
							createFunction(model, "cauchy", "Cauchy distribution", "lambda(a,a)");
						} else if (splitLaw[i].equals("rayleigh")) {
							createFunction(model, "rayleigh", "Rayleigh distribution","lambda(s,s*sqrt(pi/2))");
						} else if (splitLaw[i].equals("poisson")) {
							createFunction(model, "poisson", "Poisson distribution", "lambda(mu,mu)");
						} else if (splitLaw[i].equals("binomial")) {
							createFunction(model, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
						} else if (splitLaw[i].equals("bernoulli")) {
							createFunction(model, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
						} else if (splitLaw[i].equals("PSt")) {
							createFunction(model, "PSt", "Probabilistic Steady State Property",	"lambda(x,uniform(0,1))");
						} else if (splitLaw[i].equals("St")) {
							createFunction(model, "St", "Steady State Property", "lambda(x,not(not(x)))");
						} else if (splitLaw[i].equals("PG")) {
							createFunction(model, "PG", "Probabilistic Globally Property", "lambda(t,x,uniform(0,1))");
						} else if (splitLaw[i].equals("G")) {
							createFunction(model, "G", "Globally Property", "lambda(t,x,or(not(t),x))");
						} else if (splitLaw[i].equals("PF")) {
							createFunction(model, "PF", "Probabilistic Eventually Property", "lambda(t,x,uniform(0,1))");
						} else if (splitLaw[i].equals("F")) {
							createFunction(model, "F", "Eventually Property", "lambda(t,x,or(not(t),not(x)))");
						} else if (splitLaw[i].equals("PU")) {
							createFunction(model, "PU", "Probabilistic Until Property", "lambda(t,x,y,uniform(0,1))");
						} else if (splitLaw[i].equals("U")) {
							createFunction(model, "G", "Globally Property", "lambda(t,x,or(not(t),x))");
							createFunction(model, "F", "Eventually Property", "lambda(t,x,or(not(t),not(x)))");
							createFunction(model, "U", "Until Property", "lambda(t,x,y,and(G(t,x),F(t,y)))");
						} else if (splitLaw[i].equals("rate")) {
							createFunction(model, "rate", "Rate", "lambda(a,a)");
						} else if (splitLaw[i].equals("BIT")) {
							createFunction(model, "BIT", "bit selection", "lambda(a,b,a*b)");
						} else if (splitLaw[i].equals("BITAND")) {
							createFunction(model, "BITAND", "Bitwise AND", "lambda(a,b,a*b)");
						} else if (splitLaw[i].equals("BITOR")) {
							createFunction(model, "BITOR", "Bitwise OR", "lambda(a,b,a*b)");
						} else if (splitLaw[i].equals("BITNOT")) {
							createFunction(model, "BITNOT", "Bitwise NOT", "lambda(a,b,a*b)");
						} else if (splitLaw[i].equals("BITXOR")) {
							createFunction(model, "BITXOR", "Bitwise XOR", "lambda(a,b,a*b)");
						} else if (splitLaw[i].equals("mod")) {
							createFunction(model, "mod", "Modular", "lambda(a,b,a-floor(a/b)*b)");
						} else if (splitLaw[i].equals("neighborQuantityLeft")) {
							createFunction(model, "neighborQuantityLeft", "neighborQuantityLeft", "lambda(a,0)");
							createFunction(model, "neighborQuantityLeftFull", "neighborQuantityLeftFull", "lambda(a,b,c,0)");
						} else if (splitLaw[i].equals("neighborQuantityRight")) {
							createFunction(model, "neighborQuantityRight", "neighborQuantityRight", "lambda(a,0)");
							createFunction(model, "neighborQuantityRightFull", "neighborQuantityRightFull", "lambda(a,b,c,0)");
						} else if (splitLaw[i].equals("neighborQuantityAbove")) {
							createFunction(model, "neighborQuantityAbove", "neighborQuantityAbove", "lambda(a,0)");
							createFunction(model, "neighborQuantityAboveFull", "neighborQuantityAboveFull", "lambda(a,b,c,0)");
						} else if (splitLaw[i].equals("neighborQuantityBelow")) {
							createFunction(model, "neighborQuantityBelow", "neighborQuantityBelow", "lambda(a,0)");
							createFunction(model, "neighborQuantityBelowFull", "neighborQuantityBelowFull", "lambda(a,b,c,0)");
						}
						else {
							invalidVars.add(splitLaw[i]);
						}
						if (splitLaw[i].contains("neighborQuantity")) {
							createFunction(model, "getCompartmentLocationX", "getCompartmentLocationX", "lambda(a,0)");
							createFunction(model, "getCompartmentLocationY", "getCompartmentLocationY", "lambda(a,0)");
						}
					}
				}
			}
		}
		return invalidVars;
	}
	
	public static void pruneUnusedSpecialFunctions(SBMLDocument document) {
		if (document.getModel().getFunctionDefinition("uniform")!=null) {
			if (!variableInUse(document, "uniform", false, false, true)) {
				document.getModel().removeFunctionDefinition("uniform");
			}
		}
		if (document.getModel().getFunctionDefinition("normal")!=null) {
			if (!variableInUse(document, "normal", false, false, true)) {
				document.getModel().removeFunctionDefinition("normal");
			}
		}
		if (document.getModel().getFunctionDefinition("exponential")!=null) {
			if (!variableInUse(document, "exponential", false, false, true)) {
				document.getModel().removeFunctionDefinition("exponential");
			}
		}
		if (document.getModel().getFunctionDefinition("gamma")!=null) {
			if (!variableInUse(document, "gamma", false, false, true)) {
				document.getModel().removeFunctionDefinition("gamma");
			}
		}
		if (document.getModel().getFunctionDefinition("lognormal")!=null) {
			if (!variableInUse(document, "lognormal", false, false, true)) {
				document.getModel().removeFunctionDefinition("lognormal");
			}
		}
		if (document.getModel().getFunctionDefinition("chisq")!=null) {
			if (!variableInUse(document, "chisq", false, false, true)) {
				document.getModel().removeFunctionDefinition("chisq");
			}
		}
		if (document.getModel().getFunctionDefinition("laplace")!=null) {
			if (!variableInUse(document, "laplace", false, false, true)) {
				document.getModel().removeFunctionDefinition("laplace");
			}
		}
		if (document.getModel().getFunctionDefinition("cauchy")!=null) {
			if (!variableInUse(document, "cauchy", false, false, true)) {
				document.getModel().removeFunctionDefinition("cauchy");
			}
		}
		if (document.getModel().getFunctionDefinition("rayleigh")!=null) {
			if (!variableInUse(document, "rayleigh", false, false, true)) {
				document.getModel().removeFunctionDefinition("rayleigh");
			}
		}
		if (document.getModel().getFunctionDefinition("poisson")!=null) {
			if (!variableInUse(document, "poisson", false, false, true)) {
				document.getModel().removeFunctionDefinition("poisson");
			}
		}
		if (document.getModel().getFunctionDefinition("binomial")!=null) {
			if (!variableInUse(document, "binomial", false, false, true)) {
				document.getModel().removeFunctionDefinition("binomial");
			}
		}
		if (document.getModel().getFunctionDefinition("bernoulli")!=null) {
			if (!variableInUse(document, "bernoulli", false, false, true)) {
				document.getModel().removeFunctionDefinition("bernoulli");
			}
		}
		if (document.getModel().getFunctionDefinition("St")!=null) {
			if (!variableInUse(document, "St", false, false, true)) {
				document.getModel().removeFunctionDefinition("St");
			}
		}
		if (document.getModel().getFunctionDefinition("PSt")!=null) {
			if (!variableInUse(document, "PSt", false, false, true)) {
				document.getModel().removeFunctionDefinition("PSt");
			}
		}
		if (document.getModel().getFunctionDefinition("PG")!=null) {
			if (!variableInUse(document, "PG", false, false, true)) {
				document.getModel().removeFunctionDefinition("PG");
			}
		}
		if (document.getModel().getFunctionDefinition("PF")!=null) {
			if (!variableInUse(document, "PF", false, false, true)) {
				document.getModel().removeFunctionDefinition("PF");
			}
		}
		if (document.getModel().getFunctionDefinition("PU")!=null) {
			if (!variableInUse(document, "PU", false, false, true)) {
				document.getModel().removeFunctionDefinition("PU");
			}
		}
		if (document.getModel().getFunctionDefinition("G")!=null) {
			if (!variableInUse(document, "G", false, false, true)) {
				document.getModel().removeFunctionDefinition("G");
			}
		}
		if (document.getModel().getFunctionDefinition("F")!=null) {
			if (!variableInUse(document, "F", false, false, true)) {
				document.getModel().removeFunctionDefinition("F");
			}
		}
		if (document.getModel().getFunctionDefinition("U")!=null) {
			if (!variableInUse(document, "U", false, false, true)) {
				document.getModel().removeFunctionDefinition("U");
			}
		}
		if (document.getModel().getFunctionDefinition("rate")!=null) {
			if (!variableInUse(document, "rate", false, false, true)) {
				document.getModel().removeFunctionDefinition("rate");
			}
		}
		if (document.getModel().getFunctionDefinition("mod")!=null) {
			if (!variableInUse(document, "mod", false, false, true)) {
				document.getModel().removeFunctionDefinition("mod");
			}
		}
		if (document.getModel().getFunctionDefinition("BIT")!=null) {
			if (!variableInUse(document, "BIT", false, false, true)) {
				document.getModel().removeFunctionDefinition("BIT");
			}
		}
		if (document.getModel().getFunctionDefinition("BITOR")!=null) {
			if (!variableInUse(document, "BITOR", false, false, true)) {
				document.getModel().removeFunctionDefinition("BITOR");
			}
		}
		if (document.getModel().getFunctionDefinition("BITXOR")!=null) {
			if (!variableInUse(document, "BITXOR", false, false, true)) {
				document.getModel().removeFunctionDefinition("BITXOR");
			}
		}
		if (document.getModel().getFunctionDefinition("BITNOT")!=null) {
			if (!variableInUse(document, "BITNOT", false, false, true)) {
				document.getModel().removeFunctionDefinition("BITNOT");
			}
		}
		if (document.getModel().getFunctionDefinition("BITAND")!=null) {
			if (!variableInUse(document, "BITAND", false, false, true)) {
				document.getModel().removeFunctionDefinition("BITAND");
			}
		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityLeft") != null) {
//			if (!variableInUse(document, "neighborQuantityLeft", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityLeft");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityRight") != null) {
//			if (!variableInUse(document, "neighborQuantityRight", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityRight");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityAbove") != null) {
//			if (!variableInUse(document, "neighborQuantityAbove", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityAbove");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityBelow") != null) {
//			if (!variableInUse(document, "neighborQuantityBelow", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityBelow");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityLeftFull") != null) {
//			if (!variableInUse(document, "neighborQuantityLeftFull", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityLeftFull");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityRightFull") != null) {
//			if (!variableInUse(document, "neighborQuantityRightFull", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityRightFull");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityAboveFull") != null) {
//			if (!variableInUse(document, "neighborQuantityAboveFull", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityAboveFull");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("neighborQuantityBelowFull") != null) {
//			if (!variableInUse(document, "neighborQuantityBelowFull", false, false, true)) {
//				document.getModel().removeFunctionDefinition("neighborQuantityBelowFull");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("getCompartmentLocationX") != null) {
//			if (!variableInUse(document, "getCompartmentLocationX", false, false, true)) {
//				document.getModel().removeFunctionDefinition("getCompartmentLocationX");
//			}
//		}
//		if (document.getModel().getFunctionDefinition("getCompartmentLocationY") != null) {
//			if (!variableInUse(document, "getCompartmentLocationY", false, false, true)) {
//				document.getModel().removeFunctionDefinition("getCompartmentLocationY");
//			}
//		}
	
	}

	/**
	 * Convert ASTNodes into a string
	 */
	public static String myFormulaToString(ASTNode mathFormula) {
		if (mathFormula==null) return "";
		setTimeToT(mathFormula);
		String formula;
		Preferences biosimrc = Preferences.userRoot();
		if (biosimrc.get("biosim.general.infix", "").equals("prefix")) {
			formula = JSBML.formulaToString(mathFormula);
		} else {
			formula = myFormulaToStringInfix(mathFormula);
		}
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
		while (!(newformula.equals(formula))) {
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
	public static void setTimeToT(ASTNode node) {
		if (node==null) return;
		if (node.getType() == ASTNode.Type.NAME_TIME) {
			if (!node.getName().equals("t") || !node.getName().equals("time")) {
				node.setName("t");
			}
		}
		else if (node.getType() == ASTNode.Type.NAME_AVOGADRO) {
			node.setName("avogadro");
		}
		for (int c = 0; c < node.getChildCount(); c++)
			setTimeToT(node.getChild(c));
	}

	/**
	 * Convert String into ASTNodes
	 */
	public static ASTNode myParseFormula(String formula) {
		ASTNode mathFormula = null;
		Preferences biosimrc = Preferences.userRoot();
		try {
			IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
			if (biosimrc.get("biosim.general.infix", "").equals("prefix")) {
				mathFormula = ASTNode.parseFormula(formula, parser);
			}
			else {
				mathFormula = ASTNode.parseFormula(formula, parser);
				// mathFormula = libsbml.parseL3Formula(formula);
			}
		}
		catch (ParseException e) {
			return null;
		}
		catch (Exception e) {
			return null;
		}
		if (mathFormula == null)
			return null;
		setTimeAndTrigVar(mathFormula);
		return mathFormula;
	}

	/**
	 * Recursive function to set time and trig functions
	 */
	public static void setTimeAndTrigVar(ASTNode node) {
		if (node.getType() == ASTNode.Type.NAME) {
			if (node.getName().equals("t")) {
				node.setType(ASTNode.Type.NAME_TIME);
			}
			else if (node.getName().equals("time")) {
				node.setType(ASTNode.Type.NAME_TIME);
			}
			else if (node.getName().equals("avogadro")) {
				node.setType(ASTNode.Type.NAME_AVOGADRO);
			}
		}
		if (node.getType() == ASTNode.Type.FUNCTION) {
			if (node.getName().equals("acot")) {
				node.setType(ASTNode.Type.FUNCTION_ARCCOT);
			}
			else if (node.getName().equals("acoth")) {
				node.setType(ASTNode.Type.FUNCTION_ARCCOTH);
			}
			else if (node.getName().equals("acsc")) {
				node.setType(ASTNode.Type.FUNCTION_ARCCSC);
			}
			else if (node.getName().equals("acsch")) {
				node.setType(ASTNode.Type.FUNCTION_ARCCSCH);
			}
			else if (node.getName().equals("asec")) {
				node.setType(ASTNode.Type.FUNCTION_ARCSEC);
			}
			else if (node.getName().equals("asech")) {
				node.setType(ASTNode.Type.FUNCTION_ARCSECH);
			}
			else if (node.getName().equals("acosh")) {
				node.setType(ASTNode.Type.FUNCTION_ARCCOSH);
			}
			else if (node.getName().equals("asinh")) {
				node.setType(ASTNode.Type.FUNCTION_ARCSINH);
			}
			else if (node.getName().equals("atanh")) {
				node.setType(ASTNode.Type.FUNCTION_ARCTANH);
			}
		}

		for (int c = 0; c < node.getChildCount(); c++)
			setTimeAndTrigVar(node.getChild(c));
	}

	/**
	 * Check the number of arguments to a function
	 */
	public static boolean checkNumFunctionArguments(SBMLDocument document, ASTNode node) {
		ListOf<FunctionDefinition> sbml = document.getModel().getListOfFunctionDefinitions();
		switch (node.getType()) {
		case FUNCTION_ABS:
		case FUNCTION_ARCCOS:
		case FUNCTION_ARCCOSH:
		case FUNCTION_ARCSIN:
		case FUNCTION_ARCSINH:
		case FUNCTION_ARCTAN:
		case FUNCTION_ARCTANH:
		case FUNCTION_ARCCOT:
		case FUNCTION_ARCCOTH:
		case FUNCTION_ARCCSC:
		case FUNCTION_ARCCSCH:
		case FUNCTION_ARCSEC:
		case FUNCTION_ARCSECH:
		case FUNCTION_COS:
		case FUNCTION_COSH:
		case FUNCTION_SIN:
		case FUNCTION_SINH:
		case FUNCTION_TAN:
		case FUNCTION_TANH:
		case FUNCTION_COT:
		case FUNCTION_COTH:
		case FUNCTION_CSC:
		case FUNCTION_CSCH:
		case FUNCTION_SEC:
		case FUNCTION_SECH:
		case FUNCTION_CEILING:
		case FUNCTION_FACTORIAL:
		case FUNCTION_EXP:
		case FUNCTION_FLOOR:
		case FUNCTION_LN:
			if (node.getChildCount() != 1) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + node.getName() + " but found " + node.getChildCount() + ".",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument for " + node.getName() + " function must evaluate to a number.",
						"Number Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case LOGICAL_NOT:
			if (node.getChildCount() != 1) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + node.getName() + " but found " + node.getChildCount() + ".",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (!node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument for not function must be of type Boolean.", "Boolean Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case LOGICAL_AND:
		case LOGICAL_OR:
		case LOGICAL_XOR:
			for (int i = 0; i < node.getChildCount(); i++) {
				if (!node.getChild(i).isBoolean()) {
					JOptionPane.showMessageDialog(Gui.frame, "Argument " + i + " for " + node.getName() + " function is not of type Boolean.",
							"Boolean Expected", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			break;
		case PLUS:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for + operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for + operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case MINUS:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for - operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if ((node.getChildCount() > 1) && (node.getChild(1).isBoolean())) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for - operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case TIMES:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for * operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for * operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case DIVIDE:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for / operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for / operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case POWER:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for ^ operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for ^ operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case FUNCTION_DELAY:
		case FUNCTION_POWER:
		case FUNCTION_ROOT:
		case RELATIONAL_GEQ:
		case RELATIONAL_LEQ:
		case RELATIONAL_LT:
		case RELATIONAL_GT:
		case FUNCTION_LOG:
			if (node.getChildCount() != 2) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + node.getName() + " but found " + node.getChildCount() + ".",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for " + node.getName() + " function must evaluate to a number.",
						"Number Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(1).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for " + node.getName() + " function must evaluate to a number.",
						"Number Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case RELATIONAL_EQ:
		case RELATIONAL_NEQ:
			if (node.getChildCount() != 2) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + node.getName() + " but found " + node.getChildCount() + ".",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if ((node.getChild(0).isBoolean() && !node.getChild(1).isBoolean()) || (!node.getChild(0).isBoolean() && node.getChild(1).isBoolean())) {
				JOptionPane.showMessageDialog(Gui.frame, "Arguments for " + node.getName() + " function must either both be numbers or Booleans.",
						"Argument Mismatch", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case FUNCTION_PIECEWISE:
			if (node.getChildCount() < 1) {
				JOptionPane.showMessageDialog(Gui.frame, "Piecewise function requires at least 1 argument.", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			for (int i = 1; i < node.getChildCount(); i += 2) {
				if (!node.getChild(i).isBoolean()) {
					JOptionPane.showMessageDialog(Gui.frame, "Even arguments of piecewise function must be of type Boolean.", "Boolean Expected",
							JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			int pieceType = -1;
			for (int i = 0; i < node.getChildCount(); i += 2) {
				if (node.getChild(i).isBoolean()) {
					if (pieceType == 2) {
						JOptionPane.showMessageDialog(Gui.frame, "All odd arguments of a piecewise function must agree.", "Type Mismatch",
								JOptionPane.ERROR_MESSAGE);
						return true;
					}
					pieceType = 1;
				}
				else {
					if (pieceType == 1) {
						JOptionPane.showMessageDialog(Gui.frame, "All odd arguments of a piecewise function must agree.", "Type Mismatch",
								JOptionPane.ERROR_MESSAGE);
						return true;
					}
					pieceType = 2;
				}
			}
		case FUNCTION:
			for (int i = 0; i < document.getModel().getFunctionDefinitionCount(); i++) {
				if (((FunctionDefinition) sbml.get(i)).getId().equals(node.getName())) {
					long numArgs = ((FunctionDefinition) sbml.get(i)).getArgumentCount();
					if (numArgs != node.getChildCount()) {
						JOptionPane.showMessageDialog(Gui.frame,
								"Expected " + numArgs + " argument(s) for " + node.getName() + " but found " + node.getChildCount() + ".",
								"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
						return true;
					}
					break;
				}
			}
			break;
		case NAME:
			if (node.getName().equals("abs") || node.getName().equals("arccos") || node.getName().equals("arccosh")
					|| node.getName().equals("arcsin") || node.getName().equals("arcsinh") || node.getName().equals("arctan")
					|| node.getName().equals("arctanh") || node.getName().equals("arccot") || node.getName().equals("arccoth")
					|| node.getName().equals("arccsc") || node.getName().equals("arccsch") || node.getName().equals("arcsec")
					|| node.getName().equals("arcsech") || node.getName().equals("acos") || node.getName().equals("acosh")
					|| node.getName().equals("asin") || node.getName().equals("asinh") || node.getName().equals("atan")
					|| node.getName().equals("atanh") || node.getName().equals("acot") || node.getName().equals("acoth")
					|| node.getName().equals("acsc") || node.getName().equals("acsch") || node.getName().equals("asec")
					|| node.getName().equals("asech") || node.getName().equals("cos") || node.getName().equals("cosh")
					|| node.getName().equals("cot") || node.getName().equals("coth") || node.getName().equals("csc") || node.getName().equals("csch")
					|| node.getName().equals("ceil") || node.getName().equals("factorial") || node.getName().equals("exp")
					|| node.getName().equals("floor") || node.getName().equals("ln") || node.getName().equals("log") || node.getName().equals("sqr")
					|| node.getName().equals("log10") || node.getName().equals("sqrt") || node.getName().equals("sec")
					|| node.getName().equals("sech") || node.getName().equals("sin") || node.getName().equals("sinh") || node.getName().equals("tan")
					|| node.getName().equals("tanh") || node.getName().equals("not")) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + node.getName() + " but found 0.",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getName().equals("and") || node.getName().equals("or") || node.getName().equals("xor") || node.getName().equals("pow")
					|| node.getName().equals("eq") || node.getName().equals("geq") || node.getName().equals("leq") || node.getName().equals("gt")
					|| node.getName().equals("neq") || node.getName().equals("lt") || node.getName().equals("delay") || node.getName().equals("root")) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + node.getName() + " but found 0.",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getName().equals("piecewise")) {
				JOptionPane.showMessageDialog(Gui.frame, "Piecewise function requires at least 1 argument.", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			for (int i = 0; i < document.getModel().getFunctionDefinitionCount(); i++) {
				if (((FunctionDefinition) sbml.get(i)).getId().equals(node.getName())) {
					long numArgs = ((FunctionDefinition) sbml.get(i)).getArgumentCount();
					JOptionPane.showMessageDialog(Gui.frame, "Expected " + numArgs + " argument(s) for " + node.getName() + " but found 0.",
							"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			break;
		}
		for (int c = 0; c < node.getChildCount(); c++) {
			if (checkNumFunctionArguments(document, node.getChild(c))) {
				return true;
			}
		}
		return false;
	}
	
	public static Boolean isSpecialFunction(String functionId) {
		if (functionId.equals("uniform")) return true;
		else if (functionId.equals("normal")) return true;
		else if (functionId.equals("exponential")) return true;
		else if (functionId.equals("gamma")) return true;
		else if (functionId.equals("lognormal")) return true;
		else if (functionId.equals("chisq")) return true;
		else if (functionId.equals("laplace")) return true;
		else if (functionId.equals("cauchy")) return true;
		else if (functionId.equals("poisson")) return true;
		else if (functionId.equals("binomial")) return true;
		else if (functionId.equals("bernoulli")) return true;
		else if (functionId.equals("St")) return true;
		else if (functionId.equals("PSt")) return true;
		else if (functionId.equals("PG")) return true;
		else if (functionId.equals("PF")) return true;
		else if (functionId.equals("PU")) return true;
		else if (functionId.equals("G")) return true;
		else if (functionId.equals("F")) return true;
		else if (functionId.equals("U")) return true;
		return false;
	}

	public static void fillBlankMetaIDs (SBMLDocument document) {
		int metaIDIndex = 1;
		Model model = document.getModel();
		setDefaultMetaID(document, model, metaIDIndex);
		for (int i = 0; i < model.getParameterCount(); i++) 
			metaIDIndex = setDefaultMetaID(document, model.getParameter(i), metaIDIndex);
		for (int i = 0; i < model.getSpeciesCount(); i++) 
			metaIDIndex = setDefaultMetaID(document, model.getSpecies(i), metaIDIndex);
		for (int i = 0; i < model.getReactionCount(); i++) 
			metaIDIndex = setDefaultMetaID(document, model.getReaction(i), metaIDIndex);
		for (int i = 0; i < model.getRuleCount(); i++) 
			metaIDIndex = setDefaultMetaID(document, model.getRule(i), metaIDIndex);
		CompModelPlugin compModel = (CompModelPlugin) document.getModel().getExtension(CompConstant.namespaceURI);
		if (compModel != null && compModel.isSetListOfSubmodels()) {
			for (int i = 0; i < compModel.getListOfSubmodels().size(); i++)
				metaIDIndex = setDefaultMetaID(document, compModel.getListOfSubmodels().get(i), metaIDIndex);
		}
	}
	
	public static int setDefaultMetaID(SBMLDocument document, SBase sbmlObject, int metaIDIndex) {
		CompSBMLDocumentPlugin compDocument = (CompSBMLDocumentPlugin) document.getExtension(CompConstant.namespaceURI);
		String metaID = sbmlObject.getMetaId();
		if (metaID == null || metaID.equals("")) {
			metaID = "iBioSim" + metaIDIndex;
			while (getElementByMetaId(document, metaID) != null
					|| (compDocument != null && getElementByMetaId(compDocument, metaID) != null)) {
				metaIDIndex++;
				metaID = "iBioSim" + metaIDIndex;
			}
			setMetaId(sbmlObject, metaID);
			metaIDIndex++;
		}
		return metaIDIndex;
	}

	public static ArrayList<String> CreateListOfUsedIDs(SBMLDocument document) {
		ArrayList<String> usedIDs = new ArrayList<String>();
		if (document==null) return usedIDs;
		Model model = document.getModel();
		if (model.isSetId()) {
			usedIDs.add(model.getId());
		}
		for (int i = 0; i < model.getFunctionDefinitionCount(); i++) {
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
		for (int i = 0; i < model.getUnitDefinitionCount(); i++) {
			usedIDs.add(model.getUnitDefinition(i).getId());
		}
//		CompartmentType and SpeciesType not supported in Level 3
//		ids = model.getListOfCompartmentTypes();
//		for (int i = 0; i < model.getNumCompartmentTypes(); i++) {
//			usedIDs.add(((CompartmentType) ids.get(i)).getId());
//		}
//		ids = model.getListOfSpeciesTypes();
//		for (int i = 0; i < model.getSpeciesTypeCount(); i++) {
//			usedIDs.add(((SpeciesType) ids.get(i)).getId());
//		}
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			usedIDs.add(model.getCompartment(i).getId());
		}
		for (int i = 0; i < model.getParameterCount(); i++) {
			usedIDs.add(model.getParameter(i).getId());
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			usedIDs.add(reaction.getId());
			for (int j = 0; j < reaction.getReactantCount(); j++) {
				SpeciesReference reactant = reaction.getReactant(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
					usedIDs.add(reactant.getId());
				}
			}
			for (int j = 0; j < reaction.getProductCount(); j++) {
				SpeciesReference product = reaction.getProduct(j);
				if ((product.isSetId()) && (!product.getId().equals(""))) {
					usedIDs.add(product.getId());
				}
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			usedIDs.add(model.getSpecies(i).getId());
		}
		for (int i = 0; i < model.getConstraintCount(); i++) {
			Constraint constraint = model.getConstraint(i);
			if (constraint.isSetMetaId()) {
				usedIDs.add(constraint.getMetaId());
			}
		}
		for (int i = 0; i < model.getEventCount(); i++) {
			Event event = model.getEvent(i);
			if (event.isSetId()) {
				usedIDs.add(event.getId());
			}
		}
		return usedIDs;
	}

	/**
	 * Check for cycles in initialAssignments and assignmentRules
	 */
	public static boolean checkCycles(SBMLDocument document) {
		Model model = document.getModel();
		String[] rateLaws = new String[(int) model.getReactionCount()];
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			if (reaction.getKineticLaw()==null || reaction.getKineticLaw().getMath()==null) {
				rateLaws[i] = reaction.getId() + " = 0.0"; 
			} else {
				rateLaws[i] = reaction.getId() + " = " + myFormulaToString(reaction.getKineticLaw().getMath());
			}
		}
		String[] initRules = new String[(int) model.getInitialAssignmentCount()];
		for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
			InitialAssignment init = model.getInitialAssignment(i);
			initRules[i] = init.getVariable() + " = " + myFormulaToString(init.getMath());
		}
		String[] rules = new String[(int) model.getRuleCount()];
		for (int i = 0; i < model.getRuleCount(); i++) {
			Rule rule = model.getRule(i);
			if (rule.isAlgebraic()) {
				rules[i] = "0 = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else if (rule.isAssignment()) {
				rules[i] = getVariable(rule) + " = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else {
				rules[i] = "d( " + getVariable(rule) + " )/dt = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
		}
		String[] result = new String[rules.length + initRules.length + rateLaws.length];
		int j = 0;
		boolean[] used = new boolean[rules.length + initRules.length + rateLaws.length];
		for (int i = 0; i < rules.length + initRules.length + rateLaws.length; i++) {
			used[i] = false;
		}
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("0")) {
				result[j] = rules[i];
				used[i] = true;
				j++;
			}
		}
		boolean progress;
		do {
			progress = false;
			for (int i = 0; i < rules.length + initRules.length + rateLaws.length; i++) {
				String[] rule;
				if (i < rules.length) {
					if (used[i] || (rules[i].split(" ")[0].equals("0")) || (rules[i].split(" ")[0].equals("d(")))
						continue;
					rule = rules[i].split(" ");
				}
				else if (i < rules.length + initRules.length) {
					if (used[i])
						continue;
					rule = initRules[i - rules.length].split(" ");
				}
				else {
					if (used[i])
						continue;
					rule = rateLaws[i - (rules.length + initRules.length)].split(" ");
				}
				boolean insert = true;
				for (int k = 1; k < rule.length; k++) {
					for (int l = 0; l < rules.length + initRules.length + rateLaws.length; l++) {
						String rule2;
						if (l < rules.length) {
							if (used[l] || (rules[l].split(" ")[0].equals("0")) || (rules[l].split(" ")[0].equals("d(")))
								continue;
							rule2 = rules[l].split(" ")[0];
						}
						else if (l < rules.length + initRules.length) {
							if (used[l])
								continue;
							rule2 = initRules[l - rules.length].split(" ")[0];
						}
						else {
							if (used[l])
								continue;
							rule2 = rateLaws[l - (rules.length + initRules.length)].split(" ")[0];
						}
						if (rule[k].equals(rule2)) {
							insert = false;
							break;
						}
					}
					if (!insert)
						break;
				}
				if (insert) {
					if (i < rules.length) {
						result[j] = rules[i];
					}
					else if (i < rules.length + initRules.length) {
						result[j] = initRules[i - rules.length];
					}
					else {
						result[j] = rateLaws[i - (rules.length + initRules.length)];
					}
					j++;
					progress = true;
					used[i] = true;
				}
			}
		}
		while ((progress) && (j < rules.length + initRules.length + rateLaws.length));
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].split(" ")[0].equals("d(")) {
				result[j] = rules[i];
				j++;
			}
		}
		if (j != rules.length + initRules.length + rateLaws.length) {
			return true;
		}
		return false;
	}

	/**
	 * Checks consistency of the sbml file.
	 */
	public static void checkOverDetermined(SBMLDocument document) {
		if (Gui.isLibsbmlFound()) {
			try {
				org.sbml.libsbml.SBMLDocument doc = new org.sbml.libsbml.SBMLReader().readSBMLFromString(new SBMLWriter().writeSBMLToString(document));
				doc.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
				doc.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
				doc.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
				doc.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
				doc.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
				doc.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
				doc.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
				long numErrors = doc.checkConsistency();
				if (numErrors > 0) {
					JOptionPane.showMessageDialog(Gui.frame, "Algebraic rules make model overdetermined.", "Model is Overdetermined",
							JOptionPane.WARNING_MESSAGE);
				}
			} catch (SBMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {		
			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, false);
			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, false);
			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, false);
			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, false);
			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, false);
			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, false);
			document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
			long numErrors = document.checkConsistency();
			if (numErrors > 0) {
				JOptionPane.showMessageDialog(Gui.frame, "Algebraic rules make model overdetermined.", "Model is Overdetermined",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Create check if species used in reaction
	 */
	public static boolean usedInReaction(SBMLDocument document, String id) {
		for (int i = 0; i < document.getModel().getReactionCount(); i++) {
			for (int j = 0; j < document.getModel().getReaction(i).getReactantCount(); j++) {
				if (document.getModel().getReaction(i).getReactant(j).getSpecies().equals(id)) {
					return true;
				}
			}
			for (int j = 0; j < document.getModel().getReaction(i).getProductCount(); j++) {
				if (document.getModel().getReaction(i).getProduct(j).getSpecies().equals(id)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks if species is a reactant in a non-degradation reaction
	 */
	public static boolean usedInNonDegradationReaction(SBMLDocument document, String id) {
		for (int i = 0; i < document.getModel().getReactionCount(); i++) {
			for (int j = 0; j < document.getModel().getReaction(i).getReactantCount(); j++) {
				if (document.getModel().getReaction(i).getReactant(j).getSpecies().equals(id)
						&& (document.getModel().getReaction(i).getProductCount() > 0 || document.getModel().getReaction(i).getReactantCount() > 1)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Update variable in math formula using String
	 */
	public static String updateFormulaVar(String s, String origVar, String newVar) {
		s = " " + s + " ";
		s = s.replace(" " + origVar + " ", " " + newVar + " ");
		s = s.replace(" " + origVar + ",", " " + newVar + ",");
		s = s.replace(" " + origVar + "(", " " + newVar + "(");
		s = s.replace("(" + origVar + ")", "(" + newVar + ")");
		s = s.replace("(" + origVar + " ", "(" + newVar + " ");
		s = s.replace("(" + origVar + ",", "(" + newVar + ",");
		s = s.replace(" " + origVar + ")", " " + newVar + ")");
		s = s.replace(" " + origVar + "^", " " + newVar + "^");
		return s.trim();
	}

	/**
	 * Update variable in math formula using ASTNode
	 */
	public static ASTNode updateMathVar(ASTNode math, String origVar, String newVar) {
		String s = updateFormulaVar(myFormulaToString(math), origVar, newVar);
		return myParseFormula(s);
	}
	
	/**
	 * Check if compartment is in use.
	 */
	public static boolean compartmentInUse(SBMLDocument document, String compartmentId) {
		boolean remove = true;
		ArrayList<String> speciesUsing = new ArrayList<String>();
		for (int i = 0; i < document.getModel().getSpeciesCount(); i++) {
			Species species = (Species) document.getModel().getListOfSpecies().get(i);
			if (species.isSetCompartment()) {
				if (species.getCompartment().equals(compartmentId)) {
					remove = false;
					speciesUsing.add(species.getId());
				}
			}
		}
		ArrayList<String> reactionsUsing = new ArrayList<String>();
		for (int i = 0; i < document.getModel().getReactionCount(); i++) {
			Reaction reaction = document.getModel().getReaction(i);
			if (reaction.isSetCompartment()) {
				if (reaction.getCompartment().equals(compartmentId)){
					remove = false;
					reactionsUsing.add(reaction.getId());
				}
			}
		}
		ArrayList<String> outsideUsing = new ArrayList<String>();
		for (int i = 0; i < document.getModel().getCompartmentCount(); i++) {
			Compartment compartment = document.getModel().getCompartment(i);
			if (compartment.isSetOutside()) {
				if (compartment.getOutside().equals(compartmentId)) {
					remove = false;
					outsideUsing.add(compartment.getId());
				}
			}
		}
		if (!remove) {
			String message = "Unable to remove the selected compartment.";
			if (speciesUsing.size() != 0) {
				message += "\n\nIt contains the following species:\n";
				String[] vars = speciesUsing.toArray(new String[0]);
				Utility.sort(vars);
				for (int i = 0; i < vars.length; i++) {
					if (i == vars.length - 1) {
						message += vars[i];
					}
					else {
						message += vars[i] + "\n";
					}
				}
			}
			if (reactionsUsing.size() != 0) {
				message += "\n\nIt contains the following reactions:\n";
				String[] vars = reactionsUsing.toArray(new String[0]);
				Utility.sort(vars);
				for (int i = 0; i < vars.length; i++) {
					if (i == vars.length - 1) {
						message += vars[i];
					}
					else {
						message += vars[i] + "\n";
					}
				}
			}
			if (outsideUsing.size() != 0) {
				message += "\n\nIt outside the following compartments:\n";
				String[] vars = outsideUsing.toArray(new String[0]);
				Utility.sort(vars);
				for (int i = 0; i < vars.length; i++) {
					if (i == vars.length - 1) {
						message += vars[i];
					}
					else {
						message += vars[i] + "\n";
					}
				}
			}
			JTextArea messageArea = new JTextArea(message);
			messageArea.setEditable(false);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(300, 300));
			scroll.setPreferredSize(new Dimension(300, 300));
			scroll.setViewportView(messageArea);
			JOptionPane.showMessageDialog(Gui.frame, scroll, "Unable To Remove Compartment", JOptionPane.ERROR_MESSAGE);
		}
		return !remove;
	}
	
	/**
	 * Check if a variable is in use.
	 */
	public static boolean variableInUse(SBMLDocument document, String species, boolean zeroDim, boolean displayMessage, 
			boolean checkReactions) {
		Model model = document.getModel();
		boolean inUse = false;
		boolean isSpecies = (document.getModel().getSpecies(species) != null);
		if (species.equals("")) {
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
		if (document.getLevel() > 2) {
			if (model.isSetConversionFactor() && model.getConversionFactor().equals(species)) {
				inUse = true;
				usedInModelConversionFactor = true;
			}
			for (int i = 0; i < model.getSpeciesCount(); i++) {
				Species speciesConv = (Species) model.getListOfSpecies().get(i);
				if (speciesConv.isSetConversionFactor()) {
					if (species.equals(speciesConv.getConversionFactor())) {
						inUse = true;
						speciesUsing.add(speciesConv.getId());
					}
				}
			}
		}
		if (checkReactions) {
			for (int i = 0; i < model.getReactionCount(); i++) {
				Reaction reaction = (Reaction) model.getListOfReactions().get(i);
				if (isSpecies && (BioModel.isDegradationReaction(reaction) || BioModel.isDiffusionReaction(reaction) ||
						BioModel.isConstitutiveReaction(reaction))) continue;
				if (BioModel.isProductionReaction(reaction) && BioModel.IsDefaultProductionParameter(species)) {
					defaultParametersNeeded.add(reaction.getId());
					inUse = true;
				}
				for (int j = 0; j < reaction.getProductCount(); j++) {
					if (reaction.getProduct(j).isSetSpecies()) {
						String specRef = reaction.getProduct(j).getSpecies();
						if (species.equals(specRef)) {
							inUse = true;
							productsUsing.add(reaction.getId());
						}
					}
				}
				for (int j = 0; j < reaction.getReactantCount(); j++) {
					if (reaction.getReactant(j).isSetSpecies()) {
						String specRef = reaction.getReactant(j).getSpecies();
						if (species.equals(specRef)) {
							inUse = true;
							reactantsUsing.add(reaction.getId());
						}
					}
				}
				for (int j = 0; j < reaction.getModifierCount(); j++) {
					if (reaction.getModifier(j).isSetSpecies()) {
						String specRef = reaction.getModifier(j).getSpecies();
						if (species.equals(specRef)) {
							inUse = true;
							modifiersUsing.add(reaction.getId());
						}
					}
				}
				String[] vars = SBMLutilities.myFormulaToString(reaction.getKineticLaw().getMath()).split(" |\\(|\\)|\\,");
				for (int j = 0; j < vars.length; j++) {
					if (vars[j].equals(species)) {
						kineticLawsUsing.add(reaction.getId());
						inUse = true;
						break;
					}
				}
			}
		}
		for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
			InitialAssignment init = model.getInitialAssignment(i);
			String initStr = SBMLutilities.myFormulaToString(init.getMath());
			String[] vars = initStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					initsUsing.add(init.getVariable() + " = " + SBMLutilities.myFormulaToString(init.getMath()));
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < model.getRuleCount(); i++) {
			Rule rule = model.getRule(i);
			String initStr = SBMLutilities.myFormulaToString(rule.getMath());
			if (rule.isAssignment() || rule.isRate()) {
				initStr += " = " + getVariable(rule);
			}
			String[] vars = initStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					if (rule.isAssignment()) {
						rulesUsing.add(getVariable(rule) + " = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					else if (rule.isRate()) {
						rulesUsing.add("d(" + getVariable(rule) + ")/dt = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					else {
						rulesUsing.add("0 = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < model.getConstraintCount(); i++) {
			Constraint constraint = model.getConstraint(i);
			String consStr = SBMLutilities.myFormulaToString(constraint.getMath());
			String[] vars = consStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					constraintsUsing.add(consStr);
					inUse = true;
					break;
				}
			}
		}
		for (int i = 0; i < model.getEventCount(); i++) {
			org.sbml.jsbml.Event event = model.getEvent(i);
			String trigger = SBMLutilities.myFormulaToString(event.getTrigger().getMath());
			String eventStr = trigger;
			if (event.isSetDelay()) {
				eventStr += " " + SBMLutilities.myFormulaToString(event.getDelay().getMath());
			}
			for (int j = 0; j < event.getEventAssignmentCount(); j++) {
				eventStr += " " + (event.getListOfEventAssignments().get(j).getVariable()) + " = "
						+ SBMLutilities.myFormulaToString(event.getListOfEventAssignments().get(j).getMath());
			}
			String[] vars = eventStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					eventsUsing.add(event.getId());
					inUse = true;
					break;
				}
			}
		}
		if (inUse) {
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
			for (int i = 0; i < speciesConvFactors.length; i++) {
				if (i == speciesConvFactors.length - 1) {
					speciesConvFac += speciesConvFactors[i];
				}
				else {
					speciesConvFac += speciesConvFactors[i] + "\n";
				}
			}
			for (int i = 0; i < reacts.length; i++) {
				if (i == reacts.length - 1) {
					reactants += reacts[i];
				}
				else {
					reactants += reacts[i] + "\n";
				}
			}
			for (int i = 0; i < prods.length; i++) {
				if (i == prods.length - 1) {
					products += prods[i];
				}
				else {
					products += prods[i] + "\n";
				}
			}
			for (int i = 0; i < mods.length; i++) {
				if (i == mods.length - 1) {
					modifiers += mods[i];
				}
				else {
					modifiers += mods[i] + "\n";
				}
			}
			for (int i = 0; i < kls.length; i++) {
				if (i == kls.length - 1) {
					kineticLaws += kls[i];
				}
				else {
					kineticLaws += kls[i] + "\n";
				}
			}
			for (int i = 0; i < dps.length; i++) {
				if (i == dps.length - 1) {
					defaults += dps[i];
				}
				else {
					defaults += dps[i] + "\n";
				}
			}
			for (int i = 0; i < sm.length; i++) {
				if (i == sm.length - 1) {
					stoicMath += sm[i];
				}
				else {
					stoicMath += sm[i] + "\n";
				}
			}
			for (int i = 0; i < inAs.length; i++) {
				if (i == inAs.length - 1) {
					initAssigns += inAs[i];
				}
				else {
					initAssigns += inAs[i] + "\n";
				}
			}
			for (int i = 0; i < ruls.length; i++) {
				if (i == ruls.length - 1) {
					rules += ruls[i];
				}
				else {
					rules += ruls[i] + "\n";
				}
			}
			for (int i = 0; i < consts.length; i++) {
				if (i == consts.length - 1) {
					constraints += consts[i];
				}
				else {
					constraints += consts[i] + "\n";
				}
			}
			for (int i = 0; i < evs.length; i++) {
				if (i == evs.length - 1) {
					events += evs[i];
				}
				else {
					events += evs[i] + "\n";
				}
			}
			String message;
			if (zeroDim) {
				message = "Unable to change compartment to 0-dimensions.";
			}
			else {
				message = "Unable to remove the selected variable.";
			}
			if (usedInModelConversionFactor) {
				message += "\n\nIt is used as the model conversion factor.\n";
			}
			if (speciesUsing.size() != 0) {
				message += "\n\nIt is used as a conversion factor in the following species:\n" + speciesConvFac;
			}
			if (reactantsUsing.size() != 0) {
				message += "\n\nIt is used as a reactant in the following reactions:\n" + reactants;
			}
			if (productsUsing.size() != 0) {
				message += "\n\nIt is used as a product in the following reactions:\n" + products;
			}
			if (modifiersUsing.size() != 0) {
				message += "\n\nIt is used as a modifier in the following reactions:\n" + modifiers;
			}
			if (kineticLawsUsing.size() != 0) {
				message += "\n\nIt is used in the kinetic law in the following reactions:\n" + kineticLaws;
			}
			if (defaultParametersNeeded.size() != 0) {
				message += "\n\nDefault parameter is needed by the following reactions:\n" + defaults;
			}
			if (stoicMathUsing.size() != 0) {
				message += "\n\nIt is used in the stoichiometry math for the following reaction/species:\n" + stoicMath;
			}
			if (initsUsing.size() != 0) {
				message += "\n\nIt is used in the following initial assignments:\n" + initAssigns;
			}
			if (rulesUsing.size() != 0) {
				message += "\n\nIt is used in the following rules:\n" + rules;
			}
			if (constraintsUsing.size() != 0) {
				message += "\n\nIt is used in the following constraints:\n" + constraints;
			}
			if (eventsUsing.size() != 0) {
				message += "\n\nIt is used in the following events:\n" + events;
			}
			if (displayMessage) {
				JTextArea messageArea = new JTextArea(message);
				messageArea.setEditable(false);
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(400, 400));
				scroll.setPreferredSize(new Dimension(400, 400));
				scroll.setViewportView(messageArea);
				JOptionPane.showMessageDialog(Gui.frame, scroll, "Unable To Remove Variable", JOptionPane.ERROR_MESSAGE);
			}
		}
		return inUse;
	}

	/**
	 * Update variable Id
	 */
	public static void updateVarId(SBMLDocument document, boolean isSpecies, String origId, String newId) {
		if (origId.equals(newId))
			return;
		Model model = document.getModel();
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = (Reaction) model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getProductCount(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
				}
			}
			if (isSpecies) {
				for (int j = 0; j < reaction.getModifierCount(); j++) {
					if (reaction.getModifier(j).isSetSpecies()) {
						ModifierSpeciesReference specRef = reaction.getModifier(j);
						if (origId.equals(specRef.getSpecies())) {
							specRef.setSpecies(newId);
						}
					}
				}
			}
			for (int j = 0; j < reaction.getReactantCount(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getReactant(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
				}
			}
			if (reaction.isSetKineticLaw()) {
				reaction.getKineticLaw().setMath(SBMLutilities.updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
			}
		}
		if (document.getLevel() > 2) {
			if (model.isSetConversionFactor() && origId.equals(model.getConversionFactor())) {
				model.setConversionFactor(newId);
			}
			if (model.getSpeciesCount() > 0) {
				for (int i = 0; i < model.getSpeciesCount(); i++) {
					Species species = (Species) model.getListOfSpecies().get(i);
					if (species.isSetConversionFactor()) {
						if (origId.equals(species.getConversionFactor())) {
							species.setConversionFactor(newId);
						}
					}
				}
			}
		}
		if (model.getInitialAssignmentCount() > 0) {
			for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
				InitialAssignment init = (InitialAssignment) model.getListOfInitialAssignments().get(i);
				if (origId.equals(init.getVariable())) {
					init.setVariable(newId);
				}
				init.setMath(SBMLutilities.updateMathVar(init.getMath(), origId, newId));
			}
			try {
				if (SBMLutilities.checkCycles(document)) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
							"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (model.getRuleCount() > 0) {
			for (int i = 0; i < model.getRuleCount(); i++) {
				Rule rule = (Rule) model.getListOfRules().get(i);
				if (isSetVariable(rule) && origId.equals(getVariable(rule))) {
					setVariable(rule, newId);
				}
				rule.setMath(SBMLutilities.updateMathVar(rule.getMath(), origId, newId));
			}
			try {
				if (SBMLutilities.checkCycles(document)) {
					JOptionPane.showMessageDialog(Gui.frame, "Cycle detected within initial assignments, assignment rules, and rate laws.",
							"Cycle Detected", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(Gui.frame, "Cycle detected in assignments.", "Cycle Detected", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (model.getConstraintCount() > 0) {
			for (int i = 0; i < model.getConstraintCount(); i++) {
				Constraint constraint = (Constraint) model.getListOfConstraints().get(i);
				constraint.setMath(SBMLutilities.updateMathVar(constraint.getMath(), origId, newId));
			}
		}
		if (model.getEventCount() > 0) {
			for (int i = 0; i < model.getEventCount(); i++) {
				org.sbml.jsbml.Event event = (org.sbml.jsbml.Event) model.getListOfEvents().get(i);
				if (event.isSetTrigger()) {
					event.getTrigger().setMath(SBMLutilities.updateMathVar(event.getTrigger().getMath(), origId, newId));
				}
				if (event.isSetDelay()) {
					event.getDelay().setMath(SBMLutilities.updateMathVar(event.getDelay().getMath(), origId, newId));
				}
				for (int j = 0; j < event.getEventAssignmentCount(); j++) {
					EventAssignment ea = (EventAssignment) event.getListOfEventAssignments().get(j);
					if (ea.getVariable().equals(origId)) {
						ea.setVariable(newId);
					}
					if (ea.isSetMath()) {
						ea.setMath(SBMLutilities.updateMathVar(ea.getMath(), origId, newId));
					}
				}
			}
		}
	}

	/**
	 * Variable that is updated by a rule or event cannot be constant
	 */
	public static boolean checkConstant(SBMLDocument document, String varType, String val) {
		for (int i = 0; i < document.getModel().getRuleCount(); i++) {
			Rule rule = document.getModel().getRule(i);
			if (getVariable(rule)!=null && getVariable(rule).equals(val)) {
				JOptionPane.showMessageDialog(Gui.frame, varType + " cannot be constant if updated by a rule.", varType + " Cannot Be Constant",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		for (int i = 0; i < document.getModel().getEventCount(); i++) {
			org.sbml.jsbml.Event event = (org.sbml.jsbml.Event) document.getModel().getListOfEvents().get(i);
			for (int j = 0; j < event.getEventAssignmentCount(); j++) {
				EventAssignment ea = (EventAssignment) event.getListOfEventAssignments().get(j);
				if (ea.getVariable().equals(val)) {
					JOptionPane.showMessageDialog(Gui.frame, varType + " cannot be constant if updated by an event.",
							varType + " Cannot Be Constant", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks consistency of the sbml file.
	 */
	public static boolean check(String file,SBMLDocument doc,boolean warnings,boolean overdetermined) {
		String message = "";
		long numErrors = 0;
		if (Gui.isLibsbmlFound()) {
			org.sbml.libsbml.SBMLDocument document = null;
			if (doc == null) {
				document = new org.sbml.libsbml.SBMLReader().readSBML(file);
			} else {
				try {
					document = new org.sbml.libsbml.SBMLReader().readSBMLFromString(new SBMLWriter().writeSBMLToString(doc));
				}
				catch (SBMLException e) {
					JOptionPane.showMessageDialog(Gui.frame, "Invalid SBML file","Error Checking File", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				catch (XMLStreamException e) {
					JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file","Error Checking File", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			if (document==null) return false;
			if (overdetermined) {
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_INTERNAL_CONSISTENCY, false);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
			} else {
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_INTERNAL_CONSISTENCY, true);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
			}
			if (warnings) {
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, true);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, true);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_SBO_CONSISTENCY, true);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_MODELING_PRACTICE, true);
			} else {
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
				document.setConsistencyChecks(org.sbml.libsbml.libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
			}			
			numErrors = document.checkConsistency();
			for (int i = 0; i < numErrors; i++) {
				String error = document.getError(i).getMessage(); 
				message += i + ":" + error + "\n";
			}
		}
		else {
			SBMLDocument document = doc;
			if (document==null) {
				document = SBMLutilities.readSBML(file);
			} 
			if (document==null) return false;
			if (overdetermined) {
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, false);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, false);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
			} else {
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.OVERDETERMINED_MODEL, true);
			}
			if (warnings) {
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, true);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, true);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, true);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, true);
			} else {
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.UNITS_CONSISTENCY, false);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MATHML_CONSISTENCY, false);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.SBO_CONSISTENCY, false);
				document.setConsistencyChecks(SBMLValidator.CHECK_CATEGORY.MODELING_PRACTICE, false);
			}			
			numErrors = document.checkConsistency();
			for (int i = 0; i < numErrors; i++) {
				String error = document.getError(i).getMessage(); 
				message += i + ":" + error + "\n";
			}
		}
		if (numErrors > 0) {
			JTextArea messageArea = new JTextArea(message);
			messageArea.setLineWrap(true);
			messageArea.setEditable(false);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(600, 600));
			scroll.setPreferredSize(new Dimension(600, 600));
			scroll.setViewportView(messageArea);
			JOptionPane.showMessageDialog(Gui.frame, scroll, "SBML Errors and Warnings", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	public static boolean checkUnitsInAssignmentRule(SBMLDocument document,Rule rule) {
		UnitDefinition unitDef = rule.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(getVariable(rule));
		Compartment compartment = document.getModel().getCompartment(getVariable(rule));
		Parameter parameter = document.getModel().getParameter(getVariable(rule));
		if (species != null) {
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null) {
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else {
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
			return true;
		}
		return false;
	}
	
	public static boolean checkUnitsInRateRule(SBMLDocument document,Rule rule) {
		UnitDefinition unitDef = rule.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(getVariable(rule));
		Compartment compartment = document.getModel().getCompartment(getVariable(rule));
		Parameter parameter = document.getModel().getParameter(getVariable(rule));
		if (species != null) {
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null) {
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else {
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (document.getModel().getUnitDefinition("time") != null) {
			UnitDefinition timeUnitDef = document.getModel().getUnitDefinition("time");
			for (int i = 0; i < timeUnitDef.getUnitCount(); i++) {
				Unit timeUnit = timeUnitDef.getUnit(i);
				Unit recTimeUnit = unitDefVar.createUnit();
				recTimeUnit.setKind(timeUnit.getKind());
				if (document.getLevel() < 3) {
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				else {
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				recTimeUnit.setScale(timeUnit.getScale());
				recTimeUnit.setMultiplier(timeUnit.getMultiplier());
			}
		}
		else {
			Unit unit = unitDefVar.createUnit();
			unit.setKind(Unit.Kind.valueOf("second".toUpperCase()));
			unit.setExponent(-1.0);
			unit.setScale(0);
			unit.setMultiplier(1.0);
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
			return true;
		}
		return false;
	}
	
	public static boolean checkUnitsInInitialAssignment(SBMLDocument document,InitialAssignment init) {
		UnitDefinition unitDef = init.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(init.getVariable());
		Compartment compartment = document.getModel().getCompartment(init.getVariable());
		Parameter parameter = document.getModel().getParameter(init.getVariable());
		if (species != null) {
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null) {
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else {
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
			return true;
		}
		return false;
	}	
	
	public static boolean checkUnitsInKineticLaw(SBMLDocument document,KineticLaw law) {
		UnitDefinition unitDef = law.getDerivedUnitDefinition();
		UnitDefinition unitDefLaw = new UnitDefinition(document.getLevel(), document.getVersion());
		if (document.getModel().getUnitDefinition("substance") != null) {
			UnitDefinition subUnitDef = document.getModel().getUnitDefinition("substance");
			for (int i = 0; i < subUnitDef.getUnitCount(); i++) {
				Unit subUnit = subUnitDef.getUnit(i);
				unitDefLaw.addUnit(subUnit);
			}
		}
		else {
			Unit unit = unitDefLaw.createUnit();
			unit.setKind(Unit.Kind.valueOf("mole".toUpperCase()));
			unit.setExponent(1.0);
			unit.setScale(0);
			unit.setMultiplier(1.0);
		}
		if (document.getModel().getUnitDefinition("time") != null) {
			UnitDefinition timeUnitDef = document.getModel().getUnitDefinition("time");
			for (int i = 0; i < timeUnitDef.getUnitCount(); i++) {
				Unit timeUnit = timeUnitDef.getUnit(i);
				Unit recTimeUnit = unitDefLaw.createUnit();
				recTimeUnit.setKind(timeUnit.getKind());
				if (document.getLevel() < 3) {
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				else {
					recTimeUnit.setExponent(timeUnit.getExponent() * (-1));
				}
				recTimeUnit.setScale(timeUnit.getScale());
				recTimeUnit.setMultiplier(timeUnit.getMultiplier());
			}
		}
		else {
			Unit unit = unitDefLaw.createUnit();
			unit.setKind(Unit.Kind.valueOf("second".toUpperCase()));
			unit.setExponent(-1.0);
			unit.setScale(0);
			unit.setMultiplier(1.0);
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefLaw)) {
			return true;
		}
		return false;
	}

	public static boolean checkUnitsInEventDelay(SBMLDocument document,Delay delay) {
		UnitDefinition unitDef = delay.getDerivedUnitDefinition();
		if (unitDef != null && !(unitDef.isVariantOfTime())) {
			return true;
		}
		return false;
	}

	public static boolean checkUnitsInEventAssignment(SBMLDocument document,EventAssignment assign) {
		UnitDefinition unitDef = assign.getDerivedUnitDefinition();
		UnitDefinition unitDefVar;
		Species species = document.getModel().getSpecies(assign.getVariable());
		Compartment compartment = document.getModel().getCompartment(assign.getVariable());
		Parameter parameter = document.getModel().getParameter(assign.getVariable());
		if (species != null) {
			unitDefVar = species.getDerivedUnitDefinition();
		}
		else if (compartment != null) {
			unitDefVar = compartment.getDerivedUnitDefinition();
		}
		else {
			unitDefVar = parameter.getDerivedUnitDefinition();
		}
		if (!UnitDefinition.areEquivalent(unitDef, unitDefVar)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks consistency of the sbml file.
	 */
	public static boolean checkUnits(SBMLDocument document) {
		//TODO: Does this need to be ported over?
		//document.getModel().populateListFormulaUnitsData();
		long numErrors = 0;
		String message = "Change in unit definition causes unit errors in the following elements:\n";
		for (int i = 0; i < document.getModel().getReactionCount(); i++) {
			Reaction reaction = document.getModel().getReaction(i);
			KineticLaw law = reaction.getKineticLaw();
			if (law != null) {
				if (checkUnitsInKineticLaw(document,law)) {
					message += "Reaction: " + reaction.getId() + "\n";
					numErrors++;
				} 
			}
		}
		for (int i = 0; i < document.getModel().getInitialAssignmentCount(); i++) {
			InitialAssignment init = document.getModel().getInitialAssignment(i);
			if (checkUnitsInInitialAssignment(document,init)) {
				message += "Initial assignment on variable: " + init.getVariable() + "\n";
				numErrors++;
			} 
		}
		for (int i = 0; i < document.getModel().getRuleCount(); i++) {
			Rule rule = document.getModel().getRule(i);
			if (rule.isAssignment()) {
				if (checkUnitsInAssignmentRule(document,rule)) {
					message += "Assignment rule on variable: " + getVariable(rule) + "\n";
					numErrors++;
				}
			} else if (rule.isRate()) {
				if (checkUnitsInRateRule(document,rule)) {
					message += "Rate rule on variable: " + getVariable(rule) + "\n";
					numErrors++;
				}
			}
		}
		for (int i = 0; i < document.getModel().getEventCount(); i++) {
			Event event = document.getModel().getEvent(i);
			Delay delay = event.getDelay();
			if (delay != null) {
				if (checkUnitsInEventDelay(document,delay)) {
					message += "Delay on event: " + event.getId() + "\n";
					numErrors++;
				}
			}
			for (int j = 0; j < event.getEventAssignmentCount(); j++) {
				EventAssignment assign = event.getListOfEventAssignments().get(j);
				if (checkUnitsInEventAssignment(document,assign)) {
					message += "Event assignment for event " + event.getId() + " on variable: " + assign.getVariable() + "\n";
					numErrors++;
				}
			}
		}
		
		/*
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, false);
		long numErrorsWarnings = document.checkConsistency();
		for (long i = 0; i < numErrorsWarnings; i++) {
			if (!document.getError(i).isWarning()) {
				String error = document.getError(i).getMessage();
				message += i + ":" + error + "\n";
				numErrors++;
			}
		}
		*/
		if (numErrors > 0) {
			JTextArea messageArea = new JTextArea(message);
			messageArea.setLineWrap(true);
			messageArea.setEditable(false);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(600, 600));
			scroll.setPreferredSize(new Dimension(600, 600));
			scroll.setViewportView(messageArea);
			JOptionPane.showMessageDialog(Gui.frame, scroll, "Unit Errors in Model", JOptionPane.ERROR_MESSAGE);
			return true;
		}
		return false;
	}

	public static void addRandomFunctions(SBMLDocument document) {
		Model model = document.getModel();
		createFunction(model, "uniform", "Uniform distribution", "lambda(a,b,(a+b)/2)");
		createFunction(model, "normal", "Normal distribution", "lambda(m,s,m)");
		createFunction(model, "exponential", "Exponential distribution", "lambda(l,1/l)");
		createFunction(model, "gamma", "Gamma distribution", "lambda(a,b,a*b)");
		createFunction(model, "lognormal", "Lognormal distribution", "lambda(z,s,exp(z+s^2/2))");
		createFunction(model, "chisq", "Chi-squared distribution", "lambda(nu,nu)");
		createFunction(model, "laplace", "Laplace distribution", "lambda(a,0)");
		createFunction(model, "cauchy", "Cauchy distribution", "lambda(a,a)");
		createFunction(model, "rayleigh", "Rayleigh distribution","lambda(s,s*sqrt(pi/2))");
		createFunction(model, "poisson", "Poisson distribution", "lambda(mu,mu)");
		createFunction(model, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
		createFunction(model, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
	}

	/**
	 * Add a new function
	 */
	public static void createFunction(Model model, String id, String name, String formula) {
		if (model.getFunctionDefinition(id) == null) {
			FunctionDefinition f = model.createFunctionDefinition();
			f.setId(id);
			f.setName(name);
			try {
				IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
				f.setMath(ASTNode.parseFormula(formula, parser));
			}
			catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static boolean isBoolean(SBMLDocument document, ASTNode node) {
	  if (node == null) {
	    return false;
	  } else if ( node.isBoolean() ) {
	    return true;
	  } else if (node.getType() == ASTNode.Type.FUNCTION) {
	    FunctionDefinition fd = document.getModel().getFunctionDefinition( node.getName() );
	    if (fd != null && fd.isSetMath()) {
	      return isBoolean( document, fd.getMath().getRightChild() );
	    } else {
	      return false;
	    }
	  } else if (node.getType() == ASTNode.Type.FUNCTION_PIECEWISE) {
	    for (int c = 0; c < node.getChildCount(); c += 2) {
	      if ( !isBoolean( document, node.getChild(c) ) ) return false;
	    }
	    return true;
	  }
	  return false;
	}

	public static boolean isBoolean(Parameter parameter) {
		if (parameter.isSetSBOTerm()) {
			if (parameter.getSBOTerm()==GlobalConstants.SBO_BOOLEAN) {
				parameter.setSBOTerm(GlobalConstants.SBO_LOGICAL);
				return true;
			} else if (parameter.getSBOTerm()==GlobalConstants.SBO_LOGICAL) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPlace(Parameter parameter) {
		if (parameter.isSetSBOTerm()) {
			if (parameter.getSBOTerm()==GlobalConstants.SBO_PLACE) {
				parameter.setSBOTerm(GlobalConstants.SBO_PETRI_NET_PLACE);
				return true;
			} else if (parameter.getSBOTerm()==GlobalConstants.SBO_PETRI_NET_PLACE) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTransition(Event event) {
		if (event.isSetSBOTerm()) {
			if (event.getSBOTerm()==GlobalConstants.SBO_TRANSITION) {
				event.setSBOTerm(GlobalConstants.SBO_PETRI_NET_TRANSITION);
				return true;
			} else if (event.getSBOTerm()==GlobalConstants.SBO_PETRI_NET_TRANSITION) {
				return true;
			}
		}
		return false;
	}
	
	public static ASTNode addPreset(ASTNode math,String place) {
		return myParseFormula("and("+myFormulaToString(math)+",eq("+place+","+"1))");
	}
	
	public static ASTNode removePreset(ASTNode math,String place) {
		if (math.getType() == ASTNode.Type.LOGICAL_AND) {
			ASTNode rightChild = math.getRightChild();
			if (rightChild.getType() == ASTNode.Type.RELATIONAL_EQ && 
				rightChild.getLeftChild().getName().equals(place)) {
				return deepCopy(math.getLeftChild());
			}
		}
		for (int i = 0; i < math.getChildCount(); i++) {
			ASTNode child = removePreset(math.getChild(i),place);
			math.replaceChild(i, child);
		}
		return deepCopy(math);
	}
	
	public static String addBoolean(String formula,String boolVar) {
		formula = formula.replace(" "+boolVar+" ", " eq("+boolVar+",1) ");
		formula = formula.replace(","+boolVar+",",",eq("+boolVar+",1),");
		formula = formula.replace("("+boolVar+",", "(eq("+boolVar+",1),");
		formula = formula.replace(","+boolVar+")", ",eq("+boolVar+",1))");
		formula = formula.replace("("+boolVar+" ", "(eq("+boolVar+",1) ");
		formula = formula.replace(" "+boolVar+")", " eq("+boolVar+",1))");
		formula = formula.replace("("+boolVar+")", " eq("+boolVar+",1)");
		if (formula.startsWith(boolVar+" ")) {
			formula = formula.replaceFirst(boolVar+" ", "eq(" + boolVar + ",1)");
		}
		if (formula.endsWith(" " + boolVar)) {
			formula = formula.replaceFirst(" " + boolVar, "eq(" + boolVar + ",1)");
		}
		if (formula.equals(boolVar)) {
			formula = formula.replace(boolVar, "eq(" + boolVar + ",1)");
		}
		return formula;
	}
	
	public static ASTNode removeBoolean(ASTNode math,String boolVar) {
		if (math.getType() == ASTNode.Type.RELATIONAL_EQ) {
			if (math.getLeftChild().getName()!=null && math.getLeftChild().getName().equals(boolVar)) {
				return deepCopy(math.getLeftChild());
			}
		}
		for (int i = 0; i < math.getChildCount(); i++) {
			ASTNode child = removeBoolean(math.getChild(i),boolVar);
			math.replaceChild(i, child);
		}
		return deepCopy(math);
	}
	
	public static String myFormulaToStringInfix(ASTNode math) {
		if (math.getType() == ASTNode.Type.CONSTANT_E) {
			return "exponentiale";
		} else if (math.getType() == ASTNode.Type.CONSTANT_FALSE) {
			return "false";
		} else if (math.getType() == ASTNode.Type.CONSTANT_PI) {
			return "pi";
		} else if (math.getType() == ASTNode.Type.CONSTANT_TRUE) {
			return "true";
		} else if (math.getType() == ASTNode.Type.DIVIDE) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " / " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION) {
			String result = math.getName() + "(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i+1 < math.getChildCount()) {
					result += ",";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ABS) {
			return "abs(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOS) {
			return "acos(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOSH) {
			return "acosh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOT) {
			return "acot(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOTH) {
			return "acoth(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSC) {
			return "acsc(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSCH) {
			return "acsch(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSEC) {
			return "asec(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSECH) {
			return "asech(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSIN) {
			return "asin(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSINH) {
			return "asinh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCTAN) {
			return "atan(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCTANH) {
			return "atanh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_CEILING) {
			return "ceil(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_COS) {
			return "cos(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_COSH) {
			return "cosh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_COT) {
			return "cot(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_COTH) {
			return "coth(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_CSC) {
			return "csc(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_CSCH) {
			return "csch(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_DELAY) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "delay(" + leftStr + " , " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_EXP) {
			return "exp(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_FACTORIAL) {
			return "factorial(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_FLOOR) {
			return "floor(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_LN) {
			return "ln(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_LOG) {
			String result = "log(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i+1 < math.getChildCount()) {
					result += ",";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE) {
			String result = "piecewise(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i+1 < math.getChildCount()) {
					result += ",";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.FUNCTION_POWER) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "pow(" + leftStr + " , " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_ROOT) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "root(" + leftStr + " , " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_SEC) {
			return "sec(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_SECH) {
			return "sech(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_SIN) {
			return "sin(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_SINH) {
			return "sinh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_TAN) {
			return "tan(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.FUNCTION_TANH) {
			return "tanh(" + myFormulaToStringInfix(math.getChild(0)) + ")";
		} else if (math.getType() == ASTNode.Type.INTEGER) {
			if (math.hasUnits()) {
				return "" + math.getInteger() + " " + math.getUnits();
			} else {
				return "" + math.getInteger();
			}
		} else if (math.getType() == ASTNode.Type.LOGICAL_AND) {
			if (math.getChildCount()==0) return "";
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i+1 < math.getChildCount()) {
					result += " && ";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.LOGICAL_NOT) {
			if (math.getChildCount()==0) return "";
			String result = "!(";
			String child = myFormulaToStringInfix(math.getChild(0));
			result += child;
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.LOGICAL_OR) {
			if (math.getChildCount()==0) return "";
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i+1 < math.getChildCount()) {
					result += " || ";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.LOGICAL_XOR) {
			if (math.getChildCount()==0) return "";
			String result = "xor(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = myFormulaToStringInfix(math.getChild(i));
				result += child;
				if (i+1 < math.getChildCount()) {
					result += ",";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.MINUS) {
			if (math.getChildCount()==1) { 
				return "-" + myFormulaToStringInfix(math.getChild(0));
			} else {
				String leftStr = myFormulaToStringInfix(math.getLeftChild());
				String rightStr = myFormulaToStringInfix(math.getRightChild());
				return "(" + leftStr + " - " + rightStr + ")";
			}
		} else if (math.getType() == ASTNode.Type.NAME) {
 			return math.getName();
		} else if (math.getType() == ASTNode.Type.NAME_AVOGADRO) {
 			return "avogadro";
		} else if (math.getType() == ASTNode.Type.NAME_TIME) {
 			return "t";
		} else if (math.getType() == ASTNode.Type.PLUS) {
			String returnVal = "(";
			boolean first = true;
			for (int i=0; i < math.getChildCount(); i++) {
				if (first) {
					first = false;
				} else {
					returnVal += " + ";
				}
				returnVal += myFormulaToStringInfix(math.getChild(i));
			}
			returnVal += ")";
			return returnVal;
		} else if (math.getType() == ASTNode.Type.POWER) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " ^ " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RATIONAL) {
			if (math.hasUnits()) {
				return math.getNumerator() + "/" + math.getDenominator() + " " + math.getUnits();
			} else {
				return math.getNumerator() + "/" + math.getDenominator();
			}
		} else if (math.getType() == ASTNode.Type.REAL) {
			if (math.hasUnits()) {
				return "" + math.getReal() + " " + math.getUnits();
			} else {
				return "" + math.getReal();
			}
		} else if (math.getType() == ASTNode.Type.REAL_E) {
			if (math.hasUnits()) {
				return math.getMantissa() + "e" + math.getExponent() + " " + math.getUnits();
			} else {
				return math.getMantissa() + "e" + math.getExponent();
			}
		} else if (math.getType() == ASTNode.Type.RELATIONAL_EQ) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " == " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_GEQ) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " >= " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_GT) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " > " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_LEQ) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " <= " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_LT) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " < " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_NEQ) {
			String leftStr = myFormulaToStringInfix(math.getLeftChild());
			String rightStr = myFormulaToStringInfix(math.getRightChild());
			return "(" + leftStr + " != " + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.TIMES) {
			String returnVal = "(";
			boolean first = true;
			for (int i=0; i < math.getChildCount(); i++) {
				if (first) {
					first = false;
				} else {
					returnVal += " * ";
				}
				returnVal += myFormulaToStringInfix(math.getChild(i));
			}
			returnVal += ")";
			return returnVal;
		} else {
			if (math.isOperator()) {
				System.out.println("Operator " + math.getName() + " is not currently supported.");
			} else {
				System.out.println(math.getName() + " is not currently supported.");
			} 
		}
		return "";
	}
	
	public static boolean returnsBoolean(ASTNode math, Model model) {
		if (math.isBoolean()) {
			return true;
		} else if (math.getType() == ASTNode.Type.CONSTANT_E) {
			return false;
		} else if (math.getType() == ASTNode.Type.CONSTANT_FALSE) {
			return true;
		} else if (math.getType() == ASTNode.Type.CONSTANT_PI) {
			return false;
		} else if (math.getType() == ASTNode.Type.CONSTANT_TRUE) {
			return true;
		} else if (math.getType() == ASTNode.Type.DIVIDE) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION) {
			return returnsBoolean(math.getRightChild(), model);
		} else if (math.getType() == ASTNode.Type.FUNCTION_ABS) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOS) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOSH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOT) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCOTH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSC) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCCSCH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSEC) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSECH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSIN) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCSINH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCTAN) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ARCTANH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_CEILING) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_COS) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_COSH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_COT) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_COTH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_CSC) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_CSCH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_DELAY) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_EXP) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_FACTORIAL) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_FLOOR) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_LN) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_LOG) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE) {
			boolean result = true;
			for (int i = 0; i < math.getChildCount(); i++) {
				result = result && returnsBoolean(math.getChild(i), model);
			}
			return result;
		} else if (math.getType() == ASTNode.Type.FUNCTION_POWER) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_ROOT) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_SEC) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_SECH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_SIN) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_SINH) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_TAN) {
			return false;
		} else if (math.getType() == ASTNode.Type.FUNCTION_TANH) {
			return false;
		} else if (math.getType() == ASTNode.Type.INTEGER) {
			return false;
		} else if (math.getType() == ASTNode.Type.LOGICAL_AND) {
			return true;
		} else if (math.getType() == ASTNode.Type.LOGICAL_NOT) {
			return true;
		} else if (math.getType() == ASTNode.Type.LOGICAL_OR) {
			return true;
		} else if (math.getType() == ASTNode.Type.LOGICAL_XOR) {
			return true;
		} else if (math.getType() == ASTNode.Type.MINUS) {
			return false;
		} else if (math.getType() == ASTNode.Type.NAME) {
			return false;
		} else if (math.getType() == ASTNode.Type.NAME_AVOGADRO) {
			return false;
		} else if (math.getType() == ASTNode.Type.NAME_TIME) {
			return false;
		} else if (math.getType() == ASTNode.Type.PLUS) {
			return false;
		} else if (math.getType() == ASTNode.Type.POWER) {
			return false;
		} else if (math.getType() == ASTNode.Type.RATIONAL) {
			return false;
		} else if (math.getType() == ASTNode.Type.REAL) {
			return false;
		} else if (math.getType() == ASTNode.Type.REAL_E) {
			return false;
		} else if (math.getType() == ASTNode.Type.RELATIONAL_EQ) {
			return true;
		} else if (math.getType() == ASTNode.Type.RELATIONAL_GEQ) {
			return true;
		} else if (math.getType() == ASTNode.Type.RELATIONAL_GT) {
			return true;
		} else if (math.getType() == ASTNode.Type.RELATIONAL_LEQ) {
			return true;
		} else if (math.getType() == ASTNode.Type.RELATIONAL_LT) {
			return true;
		} else if (math.getType() == ASTNode.Type.RELATIONAL_NEQ) {
			return true;
		} else if (math.getType() == ASTNode.Type.TIMES) {
			return false;
		} else {
			if (math.isOperator()) {
				System.out.println("Operator " + math.getName() + " is not currently supported.");
			} else {
				System.out.println(math.getName() + " is not currently supported.");
			} 
		}
		return false;
	}
	
	public static String SBMLMathToBoolLPNString(ASTNode math,HashMap<String,Integer> constants,ArrayList<String> booleans) {
		if (math.getType() == ASTNode.Type.FUNCTION_PIECEWISE && math.getChildCount() > 1) {
			return SBMLMathToLPNString(math.getChild(1),constants,booleans);
		}
		return SBMLMathToLPNString(math,constants,booleans);
	}
	
	public static String SBMLMathToLPNString(ASTNode math,HashMap<String,Integer> constants,ArrayList<String> booleans) {
		if (math.getType() == ASTNode.Type.CONSTANT_FALSE) {
			return "false";
		} else if (math.getType() == ASTNode.Type.CONSTANT_TRUE) {
			return "true";
		} else if (math.getType() == ASTNode.Type.REAL) {
			return "" + math.getReal();
		} else if (math.getType() == ASTNode.Type.INTEGER) {
			return "" + math.getInteger();
		} else if (math.getType() == ASTNode.Type.NAME) {
			if (constants.containsKey(math.getName())) {
				return "" + constants.get(math.getName());
			} 
 			return math.getName();
		} else if (math.getType() == ASTNode.Type.FUNCTION) {
			String result = math.getName() + "(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = SBMLMathToLPNString(math.getChild(i),constants,booleans);
				result += child;
				if (i+1 < math.getChildCount()) {
					result += ",";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.PLUS) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "+" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.MINUS) {
			if (math.getChildCount()==1) {
				return "-" + SBMLMathToLPNString(math.getChild(0),constants,booleans);
			} 
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "-" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.TIMES) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "*" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.DIVIDE) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "/" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.POWER) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "^" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_EQ) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			if (booleans.contains(leftStr) && rightStr.equals("1")) {
				return leftStr;
			} else {
				return "(" + leftStr + "=" + rightStr + ")";
			}
		} else if (math.getType() == ASTNode.Type.RELATIONAL_GEQ) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + ">=" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_GT) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + ">" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_LEQ) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "<=" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_LT) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "<" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.RELATIONAL_NEQ) {
			String leftStr = SBMLMathToLPNString(math.getLeftChild(),constants,booleans);
			String rightStr = SBMLMathToLPNString(math.getRightChild(),constants,booleans);
			return "(" + leftStr + "!=" + rightStr + ")";
		} else if (math.getType() == ASTNode.Type.LOGICAL_NOT) {
			if (math.getChildCount()==0) return "";
			String result = "~(";
			String child = SBMLMathToLPNString(math.getChild(0),constants,booleans);
			result += child;
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.LOGICAL_AND) {
			if (math.getChildCount()==0) return "";
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = SBMLMathToLPNString(math.getChild(i),constants,booleans);
				result += child;
				if (i+1 < math.getChildCount()) {
					result += "&";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.LOGICAL_OR) {
			if (math.getChildCount()==0) return "";
			String result = "(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = SBMLMathToLPNString(math.getChild(i),constants,booleans);
				result += child;
				if (i+1 < math.getChildCount()) {
					result += "|";
				}
			}
			result += ")";
			return result;
		} else if (math.getType() == ASTNode.Type.LOGICAL_XOR) {
			if (math.getChildCount()==0) return "";
			String result = "exor(";
			for (int i = 0; i < math.getChildCount(); i++) {
				String child = SBMLMathToLPNString(math.getChild(i),constants,booleans);
				result += child;
				if (i+1 < math.getChildCount()) {
					result += ",";
				}
			}
			result += ")";
			return result;
		} else {
			if (math.isOperator()) {
				System.out.println("Operator " + math.getName() + " is not currently supported.");
			} else {
				System.out.println(math.getName() + " is not currently supported.");
			} 
		}
		return "";
	}

	public static ArrayList<String> getPreset(SBMLDocument doc,Event event) {
		ArrayList<String> preset = new ArrayList<String>();
		for (int i = 0; i < event.getEventAssignmentCount(); i++) {
			EventAssignment ea = event.getListOfEventAssignments().get(i);
			Parameter p = doc.getModel().getParameter(ea.getVariable());
			if (p != null && SBMLutilities.isPlace(p) && SBMLutilities.myFormulaToString(ea.getMath()).equals("0")) {
				preset.add(p.getId());
			}
		}
		return preset;
	}

	public static ArrayList<String> getPostset(SBMLDocument doc,Event event) {
		ArrayList<String> postset = new ArrayList<String>();
		for (int i = 0; i < event.getEventAssignmentCount(); i++) {
			EventAssignment ea = event.getListOfEventAssignments().get(i);
			Parameter p = doc.getModel().getParameter(ea.getVariable());
			if (p != null && SBMLutilities.isPlace(p) && SBMLutilities.myFormulaToString(ea.getMath()).equals("1")) {
				postset.add(p.getId());
			}
		}
		return postset;
	}
	
	public static void replaceArgument(ASTNode formula,String bvar, ASTNode arg) {
		int n = 0;
		for (int i = 0; i < formula.getChildCount(); i++) {
			ASTNode child = formula.getChild(i);
			if (child.getName() != null && child.getName().equals(bvar)) {
				formula.replaceChild(n, deepCopy(arg));
			} else if (child.getChildCount() > 0) {
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
	protected static void getAllASTNodeChildren(ASTNode node, ArrayList<ASTNode> nodeChildrenList) {
		
		for (int i = 0; i < node.getChildCount(); i++) {
			ASTNode child = node.getChild(i);
			if (child.getChildCount() == 0)
				nodeChildrenList.add(child);
			else {
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
	public static ASTNode inlineFormula(Model model, ASTNode formula) {
		
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
		
		if (formula.isFunction() == false /* || formula.isLeaf() == false*/) {
			
			for (int i = 0; i < formula.getChildCount(); ++i)
				formula.replaceChild(i, inlineFormula(model,formula.getChild(i)));//.clone()));

		} else if (formula.isFunction() && model.getFunctionDefinition(formula.getName()) != null) {
			
			if (ibiosimFunctionDefinitions.contains(formula.getName()))
				return formula;
			
			ASTNode inlinedFormula = deepCopy(model.getFunctionDefinition(formula.getName()).getBody());
			ASTNode oldFormula = deepCopy(formula);
			
			ArrayList<ASTNode> inlinedChildren = new ArrayList<ASTNode>();
			getAllASTNodeChildren(inlinedFormula, inlinedChildren);
			
			if (inlinedChildren.size() == 0)
				inlinedChildren.add(inlinedFormula);
			
			HashMap<String, Integer> inlinedChildToOldIndexMap = new HashMap<String, Integer>();
			
			for (int i = 0; i < model.getFunctionDefinition(formula.getName()).getArgumentCount(); ++i) {
				inlinedChildToOldIndexMap.put(model.getFunctionDefinition(formula.getName()).getArgument(i).getName(), i);
			}
			
			for (int i = 0; i < inlinedChildren.size(); ++i) {
				
				ASTNode child = inlinedChildren.get(i);
				
				if (child.getChildCount()==0 && child.isName()) {
					
					int index = inlinedChildToOldIndexMap.get(child.getName());
					replaceArgument(inlinedFormula,myFormulaToString(child), oldFormula.getChild(index));
					
					if (inlinedFormula.getChildCount() == 0)
						inlinedFormula = oldFormula.getChild(index);
				}
			}
			
			return inlinedFormula;
		}
		return formula;
	}
	
	public static void expandFunctionDefinitions(SBMLDocument doc) {
		Model model = doc.getModel();
		for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
			InitialAssignment ia = model.getListOfInitialAssignments().get(i);
			if (ia.isSetMath()) {
				ia.setMath(inlineFormula(model,ia.getMath()));
			}
		}
		for (int i = 0; i < model.getRuleCount(); i++) {
			Rule r = model.getRule(i);
			if (r.isSetMath()) {
				r.setMath(inlineFormula(model,r.getMath()));
			}
		}
		for (int i = 0; i < model.getConstraintCount(); i++) {
			Constraint c = model.getConstraint(i);
			if (c.isSetMath()) {
				c.setMath(inlineFormula(model,c.getMath()));
			}
		}
		for (int i = 0; i < model.getEventCount(); i++) {
			Event e = model.getEvent(i);
			if (e.getDelay()!=null && e.getDelay().isSetMath()) {
				e.getDelay().setMath(inlineFormula(model,e.getDelay().getMath()));
			}
			if (e.getTrigger()!=null && e.getTrigger().isSetMath()) {
				e.getTrigger().setMath(inlineFormula(model,e.getTrigger().getMath()));
			}
			if (e.getPriority()!=null && e.getPriority().isSetMath()) {
				e.getPriority().setMath(inlineFormula(model,e.getPriority().getMath()));
			}
			for (int j = 0; j < e.getEventAssignmentCount(); j++) {
				EventAssignment ea = e.getListOfEventAssignments().get(j);
				if (ea.isSetMath()) {
					ea.setMath(inlineFormula(model,ea.getMath()));
				}
			}
		}
	}
	
	public static void expandInitialAssignments(SBMLDocument document) {
		for (InitialAssignment ia : document.getModel().getListOfInitialAssignments()) {
			SBase sb = getElementBySId(document, ia.getVariable());
			if (sb instanceof QuantityWithUnit) {
				((QuantityWithUnit) sb).setValue(evaluateExpression(document.getModel(), ia.getMath()));
			}
		}
		for (int i = 0; i < document.getModel().getListOfInitialAssignments().size(); i ++) {
			document.getModel().getListOfInitialAssignments().remove(i);
		}
	}
	
	public static String getVariable(Rule r) {
		if (r instanceof ExplicitRule) {
			return ((ExplicitRule) r).getVariable();
		}
		return null;
	}
	
	public static void setVariable(Rule r, String variable) {
		if (r instanceof ExplicitRule) {
			((ExplicitRule) r).setVariable(variable);
		}
	}
	
	public static boolean isSetVariable(Rule r) {
		if (r instanceof ExplicitRule) {
			return ((ExplicitRule) r).isSetVariable();
		}
		return false;
	}
	
	public static ASTNode deepCopy(ASTNode original) {
		return new ASTNode(original);
	}
	
	public static void removeFromParentAndDelete(SBase element) {
		element.removeFromParent();
	}
	
	public static SBase getElementByMetaId(SBMLDocument document, String metaId) {
		return document.findSBase(metaId);
	}
	
	public static SBase getElementBySId(SBMLDocument document, String id) {
		return getElementBySId(document.getModel(), id);
	}
	
	public static SBase getElementByMetaId(Model m, String metaId) {
		return getElementByMetaId(m.getSBMLDocument(), metaId);
	}
	
	public static SBase getElementBySId(Model m, String id) {
		return m.findNamedSBase(id);
	}
	
	private static SBase getElementByMetaId(CompSBMLDocumentPlugin compDocument, String metaId) {
		for (SBase sb : getListOfAllElements(compDocument)) {
			if (sb.getMetaId().equals(metaId)) {
				return sb;
			}
		}
		return null;
	}
	
	public static ArrayList<SBase> getListOfAllElements(TreeNode node) {
		ArrayList<SBase> elements = new ArrayList<SBase>();
		if (node instanceof SBase) {
			elements.add((SBase) node);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			elements.addAll(getListOfAllElements(node.getChildAt(i)));
		}
		return elements;
	}
	
//	public static ListOf<SBase> getListOfAllElements(Model m) {
//		ListOf<SBase> elements = new ListOf<SBase>();
//		for (Compartment c : m.getListOfCompartments()) {
//			elements.add(c);
//		}
//		for (Constraint c : m.getListOfConstraints()) {
//			elements.add(c);
//		}
//		for (Event e : m.getListOfEvents()) {
//			elements.add(e);
//			for (EventAssignment ea : e.getListOfEventAssignments()) {
//				elements.add(ea);
//			}
//		}
//		for (FunctionDefinition fd : m.getListOfFunctionDefinitions()) {
//			elements.add(fd);
//		}
//		for (InitialAssignment ia : m.getListOfInitialAssignments()) {
//			elements.add(ia);
//		}
//		for (Parameter p : m.getListOfParameters()) {
//			elements.add(p);
//		}
//		for (UnitDefinition ud : m.getListOfPredefinedUnitDefinitions()) {
//			elements.add(ud);
//		}
//		for (Reaction r : m.getListOfReactions()) {
//			elements.add(r);
//			for (ModifierSpeciesReference msr : r.getListOfModifiers()) {
//				elements.add(msr);
//			}
//			for (SpeciesReference sr : r.getListOfProducts()) {
//				elements.add(sr);
//			}
//			for (SpeciesReference sr : r.getListOfReactants()) {
//				elements.add(sr);
//			}
//			for (LocalParameter lp : r.getKineticLaw().getListOfLocalParameters()) {
//				elements.add(lp);
//			}
//		}
//		for (Rule r : m.getListOfRules()) {
//			elements.add(r);
//		}
//		for (Species s : m.getListOfSpecies()) {
//			elements.add(s);
//		}
//		for (UnitDefinition ud : m.getListOfUnitDefinitions()) {
//			elements.add(ud);
//		}
//		return elements;
//	}
	
	public static String getId(SBase sb) {
		if (sb instanceof AbstractNamedSBase) {
			return ((AbstractNamedSBase) sb).getId();
		}
		else {
			return null;
		}
	}
	
	public static int appendAnnotation(SBase sbmlObject, String annotation) {
		sbmlObject.getAnnotation().appendNoRDFAnnotation(annotation);
		//sbmlObject.setAnnotation(new Annotation(sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim() + annotation));
		return JSBML.OPERATION_SUCCESS;
	}
	
	public static int appendAnnotation(SBase sbmlObject, XMLNode annotation) {
		sbmlObject.setAnnotation(new Annotation(sbmlObject.getAnnotationString().replace("<annotation>", "").replace("</annotation>", "").trim() + annotation.toXMLString()));
		return JSBML.OPERATION_SUCCESS;
	}
	
	public static FBCModelPlugin getFBCModelPlugin(Model model) {
		if (model.getExtension(FBCConstants.namespaceURI) != null) {
			return (FBCModelPlugin)model.getExtension(FBCConstants.namespaceURI);
		}
		else {
			FBCModelPlugin fbc = new FBCModelPlugin(model);
			model.addExtension(FBCConstants.namespaceURI, fbc);
			return fbc;
		}
	}
	
	public static LayoutModelPlugin getLayoutModelPlugin(Model model) {
		if (model.getExtension(LayoutConstants.namespaceURI) != null) {
			return (LayoutModelPlugin)model.getExtension(LayoutConstants.namespaceURI);
		}
		else {
			LayoutModelPlugin layout = new LayoutModelPlugin(model);
			model.addExtension(LayoutConstants.namespaceURI, layout);
			return layout;
		}
	}
	
	public static CompSBMLDocumentPlugin getCompSBMLDocumentPlugin(SBMLDocument document) {
		if (document.getExtension(CompConstant.namespaceURI) != null) {
			return (CompSBMLDocumentPlugin)document.getExtension(CompConstant.namespaceURI);
		}
		else {
			CompSBMLDocumentPlugin comp = new CompSBMLDocumentPlugin(document);
			document.addExtension(CompConstant.namespaceURI, comp);
			return comp;
		}
	}
	
	public static CompModelPlugin getCompModelPlugin(Model model) {
		if (model.getExtension(CompConstant.namespaceURI) != null) {
			return (CompModelPlugin)model.getExtension(CompConstant.namespaceURI);
		}
		else {
			CompModelPlugin comp = new CompModelPlugin(model);
			model.addExtension(CompConstant.namespaceURI, comp);
			return comp;
		}
	}
	
	public static CompSBasePlugin getCompSBasePlugin(SBase sb) {
		if (sb.getExtension(CompConstant.namespaceURI) != null) {
			return (CompSBasePlugin)sb.getExtension(CompConstant.namespaceURI);
		}
		else {
			CompSBasePlugin comp = new CompSBasePlugin(sb);
			sb.addExtension(CompConstant.namespaceURI, comp);
			return comp;
		}
	}

	public static void setNamespaces(SBMLDocument document, Map<String,String> namespaces) {
		document.getSBMLDocumentNamespaces().clear();
		for (String key : namespaces.keySet()) {
			String prefix = "";
			String shortName = key;
			if (key.contains(":")) {
				prefix = key.split(":")[0];
				shortName = key.split(":")[1];
			} 
			document.addNamespace(shortName, prefix, namespaces.get(key));
		}
		/*
		ArrayList<String> remove = new ArrayList<String>();
		for (String namespace : document.getNamespaces()) {
			remove.add(namespace);
		}
		for (String namespace : remove) {
			document.removeNamespace(namespace);
		}
		for (String namespace : namespaces) {
			document.addNamespace(namespace);
		}
		*/
	}
	
	public static boolean getBooleanFromDouble(double value) {

		if (value == 0.0) 
			return false;
		else 
			return true;
	}

	public static double getDoubleFromBoolean(boolean value) {

		if (value == true)
			return 1.0;
		else 
			return 0.0;
	}
	
	public static double evaluateExpression(Model model, ASTNode node) {
		PsRandom prng = new PsRandom();
		if (node.isBoolean()) {

			switch (node.getType()) {

			case CONSTANT_TRUE:
				return 1.0;

			case CONSTANT_FALSE:
				return 0.0;

			case  LOGICAL_NOT:
				return getDoubleFromBoolean(!(getBooleanFromDouble(evaluateExpression(model, node.getLeftChild()))));

			case LOGICAL_AND: {

				boolean andResult = true;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					andResult = andResult && getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter)));

				return getDoubleFromBoolean(andResult);
			}

			case LOGICAL_OR: {

				boolean orResult = false;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					orResult = orResult || getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter)));

				return getDoubleFromBoolean(orResult);				
			}

			case LOGICAL_XOR: {

				boolean xorResult = getBooleanFromDouble(evaluateExpression(model, node.getChild(0)));

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					xorResult = xorResult ^ getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter)));

				return getDoubleFromBoolean(xorResult);
			}

			case RELATIONAL_EQ:
				return getDoubleFromBoolean(
						evaluateExpression(model, node.getLeftChild()) == evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_NEQ:
				return getDoubleFromBoolean(
						evaluateExpression(model, node.getLeftChild()) != evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_GEQ:
			{
				//System.out.println("Node: " + libsbml.formulaToString(node.getRightChild()) + " " + evaluateExpressionRecursive(modelstate, node.getRightChild()));
				//System.out.println("Node: " + evaluateExpressionRecursive(modelstate, node.getLeftChild()) + " " + evaluateExpressionRecursive(modelstate, node.getRightChild()));

				return getDoubleFromBoolean(
						evaluateExpression(model, node.getLeftChild()) >= evaluateExpression(model, node.getRightChild()));
			}
			case RELATIONAL_LEQ:
				return getDoubleFromBoolean(
						evaluateExpression(model, node.getLeftChild()) <= evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_GT:
				return getDoubleFromBoolean(
						evaluateExpression(model, node.getLeftChild()) > evaluateExpression(model, node.getRightChild()));

			case RELATIONAL_LT:
			{
				return getDoubleFromBoolean(
						evaluateExpression(model, node.getLeftChild()) < evaluateExpression(model, node.getRightChild()));			
			}

			}
		}

		//if it's a mathematical constant
		else if (node.isConstant()) {

			switch (node.getType()) {

			case CONSTANT_E:
				return Math.E;

			case CONSTANT_PI:
				return Math.PI;
			}
		}
		else if (node.isInteger())
			return node.getInteger();

		//if it's a number
		else if (node.isReal())
			return node.getReal();

		//if it's a user-defined variable
		//eg, a species name or global/local parameter
		else if (node.isName()) {
			
			SBase sb = getElementBySId(model, node.getName());
			if (sb instanceof QuantityWithUnit) {
				return ((QuantityWithUnit) sb).getValue();
			}
		}

		//operators/functions with two children
		else {

			ASTNode leftChild = node.getLeftChild();
			ASTNode rightChild = node.getRightChild();

			switch (node.getType()) {

			case PLUS: {

				double sum = 0.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					sum += evaluateExpression(model, node.getChild(childIter));					

				return sum;
			}

			case MINUS: {

				double sum = evaluateExpression(model, leftChild);

				for (int childIter = 1; childIter < node.getChildCount(); ++childIter)
					sum -= evaluateExpression(model, node.getChild(childIter));					

				return sum;
			}

			case TIMES: {

				double product = 1.0;

				for (int childIter = 0; childIter < node.getChildCount(); ++childIter)
					product *= evaluateExpression(model, node.getChild(childIter));

				return product;
			}

			case DIVIDE:
				return (evaluateExpression(model, leftChild) / evaluateExpression(model, rightChild));

			case FUNCTION_POWER:
				return (FastMath.pow(evaluateExpression(model, leftChild), evaluateExpression(model, rightChild)));

			case FUNCTION: {
				//use node name to determine function
				//i'm not sure what to do with completely user-defined functions, though
				String nodeName = node.getName();

				//generates a uniform random number between the upper and lower bound
				if (nodeName.equals("uniform")) {

					double leftChildValue = evaluateExpression(model, node.getLeftChild());
					double rightChildValue = evaluateExpression(model, node.getRightChild());
					double lowerBound = FastMath.min(leftChildValue, rightChildValue);
					double upperBound = FastMath.max(leftChildValue, rightChildValue);

					return prng.nextDouble(lowerBound, upperBound);
				}
				else if (nodeName.equals("exponential")) {

					return prng.nextExponential(evaluateExpression(model, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("gamma")) {

					return prng.nextGamma(1, evaluateExpression(model, node.getLeftChild()), 
							evaluateExpression(model, node.getRightChild()));
				}
				else if (nodeName.equals("chisq")) {

					return prng.nextChiSquare((int) evaluateExpression(model, node.getLeftChild()));
				}
				else if (nodeName.equals("lognormal")) {

					return prng.nextLogNormal(evaluateExpression(model, node.getLeftChild()), 
							evaluateExpression(model, node.getRightChild()));
				}
				else if (nodeName.equals("laplace")) {

					//function doesn't exist in current libraries
					return 0;
				}
				else if (nodeName.equals("cauchy")) {

					return prng.nextLorentzian(0, evaluateExpression(model, node.getLeftChild()));
				}
				else if (nodeName.equals("poisson")) {

					return prng.nextPoissonian(evaluateExpression(model, node.getLeftChild()));
				}
				else if (nodeName.equals("binomial")) {

					return prng.nextBinomial(evaluateExpression(model, node.getLeftChild()),
							(int) evaluateExpression(model, node.getRightChild()));
				}
				else if (nodeName.equals("bernoulli")) {

					return prng.nextBinomial(evaluateExpression(model, node.getLeftChild()), 1);
				}
				else if (nodeName.equals("normal")) {

					return prng.nextGaussian(evaluateExpression(model, node.getLeftChild()),
							evaluateExpression(model, node.getRightChild()));	
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

			case FUNCTION_PIECEWISE: {

				//loop through child triples
				//if child 1 is true, return child 0, else return child 2				
				for (int childIter = 0; childIter < node.getChildCount(); childIter += 3) {

					if ((childIter + 1) < node.getChildCount() && 
							getBooleanFromDouble(evaluateExpression(model, node.getChild(childIter + 1)))) {
						return evaluateExpression(model, node.getChild(childIter));
					}
					else if ((childIter + 2) < node.getChildCount()) {
						return evaluateExpression(model, node.getChild(childIter + 2));
					}
				}

				return 0;
			}

			case FUNCTION_ROOT:
				return FastMath.pow(evaluateExpression(model, node.getRightChild()), 
						1 / evaluateExpression(model, node.getLeftChild()));

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
				//NOT PLANNING TO SUPPORT THIS
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

			} //end switch

		}
		return 0.0;
	}
	
	public static void setMetaId(AbstractSBase asb, String newId) {
		if (!asb.getMetaId().equals(newId)){
			asb.setMetaId(newId);
		}
	}
	
	public static void setMetaId(SBase asb, String newId) {
		if (!asb.getMetaId().equals(newId)){
			asb.setMetaId(newId);
		}
	}
	
	public static ModifierSpeciesReference removeModifier(Reaction r, String species) {
		if (r.getListOfModifiers() != null) {
			return r.removeModifier(species);
		}
		else {
			return null;
		}
	}
	
	public static void checkModelCompleteness(SBMLDocument document) {
		FBCModelPlugin fbc = SBMLutilities.getFBCModelPlugin(document.getModel());
		JTextArea messageArea = new JTextArea();
		messageArea.append("Model is incomplete.  Cannot be simulated until the following information is provided.\n");
		boolean display = false;
		org.sbml.jsbml.Model model = document.getModel();
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			Compartment compartment = model.getCompartment(i);
			if (!compartment.isSetSize()) {
				messageArea.append("--------------------------------------------------------------------------\n");
				messageArea.append("Compartment " + compartment.getId() + " needs a size.\n");
				display = true;
			}
		}
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);
			if (!(species.isSetInitialAmount()) && !(species.isSetInitialConcentration())) {
				messageArea.append("--------------------------------------------------------------------------\n");
				messageArea.append("Species " + species.getId() + " needs an initial amount or concentration.\n");
				display = true;
			}
		}
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter parameter = model.getParameter(i);
			if (!(parameter.isSetValue())) {
				messageArea.append("--------------------------------------------------------------------------\n");
				messageArea.append("Parameter " + parameter.getId() + " needs an initial value.\n");
				display = true;
			}
		}
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			if (fbc!=null) {
				boolean foundIt = false;
				for (int j = 0; j < fbc.getListOfFluxBounds().size(); j++) {
					FluxBound fb = fbc.getFluxBound(j);
					if (fb.getReaction().equals(reaction.getId())) {
						foundIt = true;
						break;
					}
				}
				if (foundIt) continue;
			}
			if (!(reaction.isSetKineticLaw())) {
				messageArea.append("--------------------------------------------------------------------------\n");
				messageArea.append("Reaction " + reaction.getId() + " needs a kinetic law.\n");
				display = true;
			}
			else {
				for (int j = 0; j < reaction.getKineticLaw().getLocalParameterCount(); j++) {
					LocalParameter param = reaction.getKineticLaw().getLocalParameter(j);
					if (!(param.isSetValue())) {
						messageArea.append("--------------------------------------------------------------------------\n");
						messageArea.append("Local parameter " + param.getId() + " for reaction " + reaction.getId() + " needs an initial value.\n");
						display = true;
					}
				}
			}
		}
		if (display) {
			final JFrame f = new JFrame("SBML Model Completeness Errors");
			messageArea.setLineWrap(true);
			messageArea.setEditable(false);
			messageArea.setSelectionStart(0);
			messageArea.setSelectionEnd(0);
			JScrollPane scroll = new JScrollPane();
			scroll.setMinimumSize(new Dimension(600, 600));
			scroll.setPreferredSize(new Dimension(600, 600));
			scroll.setViewportView(messageArea);
			JButton close = new JButton("Dismiss");
			close.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					f.dispose();
				}
			});
			JPanel consistencyPanel = new JPanel(new BorderLayout());
			consistencyPanel.add(scroll, "Center");
			consistencyPanel.add(close, "South");
			f.setContentPane(consistencyPanel);
			f.pack();
			Dimension screenSize;
			try {
				Toolkit tk = Toolkit.getDefaultToolkit();
				screenSize = tk.getScreenSize();
			}
			catch (AWTError awe) {
				screenSize = new Dimension(640, 480);
			}
			Dimension frameSize = f.getSize();
			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}
			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}
			int x = screenSize.width / 2 - frameSize.width / 2;
			int y = screenSize.height / 2 - frameSize.height / 2;
			f.setLocation(x, y);
			f.setVisible(true);
		}
	}

	public static SBMLDocument readSBML(String filename) {
		//SBMLReader reader = new SBMLReader();
		SBMLDocument document = null;
		try {
			document = SBMLReader.read(new File(filename));
			// TODO: This is a hack to remove duplicate units
			int i = 0; 
			while (i < document.getModel().getUnitDefinitionCount()) {
				UnitDefinition unitDef = document.getModel().getUnitDefinition(i);
				if (!unitDef.isSetId()) {
					document.getModel().removeUnitDefinition(i);
				} else {
					i++;
				}
			}
		} catch (XMLStreamException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "Invalid XML in SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame, "I/O error when opening SBML file","Error Opening File", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if (document.getModel().isSetId()) {
			document.getModel().setId(document.getModel().getId().replace(".","_"));
		}
		if (document.getLevel() < Gui.SBML_LEVEL || document.getVersion() < Gui.SBML_VERSION) {
			if (!Gui.libsbmlFound) {
				JOptionPane.showMessageDialog(Gui.frame, "Unable convert model to Level "+Gui.SBML_LEVEL+" Version " + Gui.SBML_VERSION,
						"Error Opening File", JOptionPane.ERROR_MESSAGE);
				return null;
			} 
			long numErrors = 0;
			org.sbml.libsbml.SBMLReader reader = new org.sbml.libsbml.SBMLReader();
			org.sbml.libsbml.SBMLDocument doc = reader.readSBML(filename);
			numErrors = doc.checkL3v1Compatibility();
			if (numErrors > 0) {
				JTextArea messageArea = new JTextArea();
				messageArea.append("Conversion to SBML level " + Gui.SBML_LEVEL + " version " + Gui.SBML_VERSION + " produced the errors listed below. ");
				messageArea.append("It is recommended that you fix them before using these models or you may get unexpected results.\n\n");
				messageArea.append("--------------------------------------------------------------------------------------\n");
				messageArea.append(filename);
				messageArea.append("\n--------------------------------------------------------------------------------------\n\n");
				for (int i = 0; i < numErrors; i++) {
					String error = doc.getError(i).getMessage();
					messageArea.append(i + ":" + error + "\n");
				}
				final JFrame f = new JFrame("SBML Conversion Errors and Warnings");
				messageArea.setLineWrap(true);
				messageArea.setEditable(false);
				messageArea.setSelectionStart(0);
				messageArea.setSelectionEnd(0);
				JScrollPane scroll = new JScrollPane();
				scroll.setMinimumSize(new Dimension(600, 600));
				scroll.setPreferredSize(new Dimension(600, 600));
				scroll.setViewportView(messageArea);
				JButton close = new JButton("Dismiss");
				close.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						f.dispose();
					}
				});
				JPanel consistencyPanel = new JPanel(new BorderLayout());
				consistencyPanel.add(scroll, "Center");
				consistencyPanel.add(close, "South");
				f.setContentPane(consistencyPanel);
				f.pack();
				Dimension screenSize;
				try {
					Toolkit tk = Toolkit.getDefaultToolkit();
					screenSize = tk.getScreenSize();
				}
				catch (AWTError awe) {
					screenSize = new Dimension(640, 480);
				}
				Dimension frameSize = f.getSize();
				if (frameSize.height > screenSize.height) {
					frameSize.height = screenSize.height;
				}
				if (frameSize.width > screenSize.width) {
					frameSize.width = screenSize.width;
				}
				int x = screenSize.width / 2 - frameSize.width / 2;
				int y = screenSize.height / 2 - frameSize.height / 2;
				f.setLocation(x, y);
				f.setVisible(true);
				Object[] options = { "Dismiss" };
				JOptionPane.showOptionDialog(Gui.frame, scroll, "SBML Conversion Errors and Warnings", JOptionPane.YES_OPTION, JOptionPane.PLAIN_MESSAGE, null, options,
						options[0]);
			}
			doc.setLevelAndVersion(Gui.SBML_LEVEL, Gui.SBML_VERSION,false);
			org.sbml.libsbml.SBMLWriter writer = new org.sbml.libsbml.SBMLWriter();
			try {
				writer.writeSBMLToFile(doc, filename);
			} catch (SBMLException e) {
				e.printStackTrace();
			} 
			try {
				document = SBMLReader.read(new File(filename));
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return document;
	}
}
