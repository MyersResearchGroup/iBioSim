package biomodel.gui.textualeditor;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.Gui;
import main.util.Utility;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentType;
import org.sbml.libsbml.Constraint;
import org.sbml.libsbml.EventAssignment;
import org.sbml.libsbml.FunctionDefinition;
import org.sbml.libsbml.InitialAssignment;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.ModifierSpeciesReference;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.Rule;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;
import org.sbml.libsbml.SpeciesType;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbml;


public class SBMLutilities {

	/**
	 * Check that ID is valid and unique
	 */
	public static boolean checkID(SBMLDocument document, ArrayList<String> usedIDs, String ID, String selectedID, boolean isReacParam) {
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
		if (usedIDs.contains(ID) && !ID.equals(selectedID)) {
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
		ListOf sbml = model.getListOfFunctionDefinitions();
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			validVars.add(((FunctionDefinition) sbml.get(i)).getId());
		}
		if (!isFunction) {
			sbml = model.getListOfSpecies();
			for (int i = 0; i < model.getNumSpecies(); i++) {
				validVars.add(((Species) sbml.get(i)).getId());
			}
		}
		if (isFunction) {
			String[] args = arguments.split(" |\\,");
			for (int i = 0; i < args.length; i++) {
				validVars.add(args[i]);
			}
		}
		else {
			sbml = model.getListOfCompartments();
			for (int i = 0; i < model.getNumCompartments(); i++) {
				if (document.getLevel() > 2 || ((Compartment) sbml.get(i)).getSpatialDimensions() != 0) {
					validVars.add(((Compartment) sbml.get(i)).getId());
				}
			}
			sbml = model.getListOfParameters();
			for (int i = 0; i < model.getNumParameters(); i++) {
				validVars.add(((Parameter) sbml.get(i)).getId());
			}
			sbml = model.getListOfReactions();
			for (int i = 0; i < model.getNumReactions(); i++) {
				Reaction reaction = (Reaction) sbml.get(i);
				validVars.add(reaction.getId());
				ListOf sbml2 = reaction.getListOfReactants();
				for (int j = 0; j < reaction.getNumReactants(); j++) {
					SpeciesReference reactant = (SpeciesReference) sbml2.get(j);
					if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
						validVars.add(reactant.getId());
					}
				}
				sbml2 = reaction.getListOfProducts();
				for (int j = 0; j < reaction.getNumProducts(); j++) {
					SpeciesReference product = (SpeciesReference) sbml2.get(j);
					if ((product.isSetId()) && (!product.getId().equals(""))) {
						validVars.add(product.getId());
					}
				}
			}
		}
		String[] splitLaw = formula.split(" |\\(|\\)|\\,|\\*|\\+|\\/|\\-");
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
						} else if (splitLaw[i].equals("uniform")) {
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
						} else if (splitLaw[i].equals("poisson")) {
							createFunction(model, "poisson", "Poisson distribution", "lambda(mu,mu)");
						} else if (splitLaw[i].equals("binomial")) {
							createFunction(model, "binomial", "Binomial distribution", "lambda(p,n,p*n)");
						} else if (splitLaw[i].equals("bernoulli")) {
							createFunction(model, "bernoulli", "Bernoulli distribution", "lambda(p,p)");
						} else if (splitLaw[i].equals("PG")) {
							createFunction(model, "PG", "Globally Property", "lambda(t,x,or(not(t),x))");
						} else if (splitLaw[i].equals("PF")) {
							createFunction(model, "PF", "Eventually Property", "lambda(t,x,or(not(t),not(x)))");
						} else if (splitLaw[i].equals("PU")) {
							createFunction(model, "PG", "Globally Property", "lambda(t,x,or(not(t),x))");
							createFunction(model, "PF", "Eventually Property", "lambda(t,x,or(not(t),not(x)))");
							createFunction(model, "PU", "Until Property", "lambda(t,x,y,or(PG(t,x),PF(t,y)))");
						} else {
							invalidVars.add(splitLaw[i]);
						}
					}
				}
			}
		}
		return invalidVars;
	}

	/**
	 * Convert ASTNodes into a string
	 */
	public static String myFormulaToString(ASTNode mathFormula) {
		setTimeToT(mathFormula);
		String formula = libsbml.formulaToString(mathFormula);
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
		if (node.getType() == libsbml.AST_NAME_TIME) {
			if (!node.getName().equals("t") || !node.getName().equals("time")) {
				node.setName("t");
			}
		}
		else if (node.getType() == libsbml.AST_NAME_AVOGADRO) {
			node.setName("avogadro");
		}
		for (int c = 0; c < node.getNumChildren(); c++)
			setTimeToT(node.getChild(c));
	}

	/**
	 * Convert String into ASTNodes
	 */
	public static ASTNode myParseFormula(String formula) {
		ASTNode mathFormula = libsbml.parseFormula(formula);
		if (mathFormula == null)
			return null;
		setTimeAndTrigVar(mathFormula);
		return mathFormula;
	}

	/**
	 * Recursive function to set time and trig functions
	 */
	public static void setTimeAndTrigVar(ASTNode node) {
		if (node.getType() == libsbml.AST_NAME) {
			if (node.getName().equals("t")) {
				node.setType(libsbml.AST_NAME_TIME);
			}
			else if (node.getName().equals("time")) {
				node.setType(libsbml.AST_NAME_TIME);
			}
			else if (node.getName().equals("avogadro")) {
				node.setType(libsbml.AST_NAME_AVOGADRO);
			}
		}
		if (node.getType() == libsbml.AST_FUNCTION) {
			if (node.getName().equals("acot")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOT);
			}
			else if (node.getName().equals("acoth")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOTH);
			}
			else if (node.getName().equals("acsc")) {
				node.setType(libsbml.AST_FUNCTION_ARCCSC);
			}
			else if (node.getName().equals("acsch")) {
				node.setType(libsbml.AST_FUNCTION_ARCCSCH);
			}
			else if (node.getName().equals("asec")) {
				node.setType(libsbml.AST_FUNCTION_ARCSEC);
			}
			else if (node.getName().equals("asech")) {
				node.setType(libsbml.AST_FUNCTION_ARCSECH);
			}
			else if (node.getName().equals("acosh")) {
				node.setType(libsbml.AST_FUNCTION_ARCCOSH);
			}
			else if (node.getName().equals("asinh")) {
				node.setType(libsbml.AST_FUNCTION_ARCSINH);
			}
			else if (node.getName().equals("atanh")) {
				node.setType(libsbml.AST_FUNCTION_ARCTANH);
			}
		}

		for (int c = 0; c < node.getNumChildren(); c++)
			setTimeAndTrigVar(node.getChild(c));
	}

	/**
	 * Check the number of arguments to a function
	 */
	public static boolean checkNumFunctionArguments(SBMLDocument document, ASTNode node) {
		ListOf sbml = document.getModel().getListOfFunctionDefinitions();
		switch (node.getType()) {
		case libsbml.AST_FUNCTION_ABS:
		case libsbml.AST_FUNCTION_ARCCOS:
		case libsbml.AST_FUNCTION_ARCCOSH:
		case libsbml.AST_FUNCTION_ARCSIN:
		case libsbml.AST_FUNCTION_ARCSINH:
		case libsbml.AST_FUNCTION_ARCTAN:
		case libsbml.AST_FUNCTION_ARCTANH:
		case libsbml.AST_FUNCTION_ARCCOT:
		case libsbml.AST_FUNCTION_ARCCOTH:
		case libsbml.AST_FUNCTION_ARCCSC:
		case libsbml.AST_FUNCTION_ARCCSCH:
		case libsbml.AST_FUNCTION_ARCSEC:
		case libsbml.AST_FUNCTION_ARCSECH:
		case libsbml.AST_FUNCTION_COS:
		case libsbml.AST_FUNCTION_COSH:
		case libsbml.AST_FUNCTION_SIN:
		case libsbml.AST_FUNCTION_SINH:
		case libsbml.AST_FUNCTION_TAN:
		case libsbml.AST_FUNCTION_TANH:
		case libsbml.AST_FUNCTION_COT:
		case libsbml.AST_FUNCTION_COTH:
		case libsbml.AST_FUNCTION_CSC:
		case libsbml.AST_FUNCTION_CSCH:
		case libsbml.AST_FUNCTION_SEC:
		case libsbml.AST_FUNCTION_SECH:
		case libsbml.AST_FUNCTION_CEILING:
		case libsbml.AST_FUNCTION_FACTORIAL:
		case libsbml.AST_FUNCTION_EXP:
		case libsbml.AST_FUNCTION_FLOOR:
		case libsbml.AST_FUNCTION_LN:
			if (node.getNumChildren() != 1) {
				System.out.println(node.getNumChildren());
				JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + node.getName() + " but found " + node.getNumChildren() + ".",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument for " + node.getName() + " function must evaluate to a number.",
						"Number Expected", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_LOGICAL_NOT:
			if (node.getNumChildren() != 1) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 1 argument for " + node.getName() + " but found " + node.getNumChildren() + ".",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if (!node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument for not function must be of type Boolean.", "Boolean Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_LOGICAL_AND:
		case libsbml.AST_LOGICAL_OR:
		case libsbml.AST_LOGICAL_XOR:
			for (int i = 0; i < node.getNumChildren(); i++) {
				if (!node.getChild(i).isBoolean()) {
					JOptionPane.showMessageDialog(Gui.frame, "Argument " + i + " for " + node.getName() + " function is not of type Boolean.",
							"Boolean Expected", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			break;
		case libsbml.AST_PLUS:
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
		case libsbml.AST_MINUS:
			if (node.getChild(0).isBoolean()) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 1 for - operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if ((node.getNumChildren() > 1) && (node.getChild(1).isBoolean())) {
				JOptionPane.showMessageDialog(Gui.frame, "Argument 2 for - operator must evaluate to a number.", "Number Expected",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_TIMES:
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
		case libsbml.AST_DIVIDE:
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
		case libsbml.AST_POWER:
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
		case libsbml.AST_FUNCTION_DELAY:
		case libsbml.AST_FUNCTION_POWER:
		case libsbml.AST_FUNCTION_ROOT:
		case libsbml.AST_RELATIONAL_GEQ:
		case libsbml.AST_RELATIONAL_LEQ:
		case libsbml.AST_RELATIONAL_LT:
		case libsbml.AST_RELATIONAL_GT:
		case libsbml.AST_FUNCTION_LOG:
			if (node.getNumChildren() != 2) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + node.getName() + " but found " + node.getNumChildren() + ".",
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
		case libsbml.AST_RELATIONAL_EQ:
		case libsbml.AST_RELATIONAL_NEQ:
			if (node.getNumChildren() != 2) {
				JOptionPane.showMessageDialog(Gui.frame, "Expected 2 arguments for " + node.getName() + " but found " + node.getNumChildren() + ".",
						"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			if ((node.getChild(0).isBoolean() && !node.getChild(1).isBoolean()) || (!node.getChild(0).isBoolean() && node.getChild(1).isBoolean())) {
				JOptionPane.showMessageDialog(Gui.frame, "Arguments for " + node.getName() + " function must either both be numbers or Booleans.",
						"Argument Mismatch", JOptionPane.ERROR_MESSAGE);
				return true;
			}
			break;
		case libsbml.AST_FUNCTION_PIECEWISE:
			if (node.getNumChildren() < 1) {
				JOptionPane.showMessageDialog(Gui.frame, "Piecewise function requires at least 1 argument.", "Number of Arguments Incorrect",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
			for (int i = 1; i < node.getNumChildren(); i += 2) {
				if (!node.getChild(i).isBoolean()) {
					JOptionPane.showMessageDialog(Gui.frame, "Even arguments of piecewise function must be of type Boolean.", "Boolean Expected",
							JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			int pieceType = -1;
			for (int i = 0; i < node.getNumChildren(); i += 2) {
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
		case libsbml.AST_FUNCTION:
			for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
				if (((FunctionDefinition) sbml.get(i)).getId().equals(node.getName())) {
					long numArgs = ((FunctionDefinition) sbml.get(i)).getNumArguments();
					if (numArgs != node.getNumChildren()) {
						JOptionPane.showMessageDialog(Gui.frame,
								"Expected " + numArgs + " argument(s) for " + node.getName() + " but found " + node.getNumChildren() + ".",
								"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
						return true;
					}
					break;
				}
			}
			break;
		case libsbml.AST_NAME:
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
			for (int i = 0; i < document.getModel().getNumFunctionDefinitions(); i++) {
				if (((FunctionDefinition) sbml.get(i)).getId().equals(node.getName())) {
					long numArgs = ((FunctionDefinition) sbml.get(i)).getNumArguments();
					JOptionPane.showMessageDialog(Gui.frame, "Expected " + numArgs + " argument(s) for " + node.getName() + " but found 0.",
							"Number of Arguments Incorrect", JOptionPane.ERROR_MESSAGE);
					return true;
				}
			}
			break;
		}
		for (int c = 0; c < node.getNumChildren(); c++) {
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
		else if (functionId.equals("PG")) return true;
		else if (functionId.equals("PF")) return true;
		else if (functionId.equals("PU")) return true;
		return false;
	}

	public static ArrayList<String> CreateListOfUsedIDs(SBMLDocument document) {
		ArrayList<String> usedIDs = new ArrayList<String>();
		if (document==null) return usedIDs;
		Model model = document.getModel();
		if (model.isSetId()) {
			usedIDs.add(model.getId());
		}
		ListOf ids = model.getListOfFunctionDefinitions();
		for (int i = 0; i < model.getNumFunctionDefinitions(); i++) {
			usedIDs.add(((FunctionDefinition) ids.get(i)).getId());
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
		usedIDs.add("PG");
		usedIDs.add("PF");
		usedIDs.add("PU");
		ids = model.getListOfUnitDefinitions();
		for (int i = 0; i < model.getNumUnitDefinitions(); i++) {
			usedIDs.add(((UnitDefinition) ids.get(i)).getId());
		}
		ids = model.getListOfCompartmentTypes();
		for (int i = 0; i < model.getNumCompartmentTypes(); i++) {
			usedIDs.add(((CompartmentType) ids.get(i)).getId());
		}
		ids = model.getListOfSpeciesTypes();
		for (int i = 0; i < model.getNumSpeciesTypes(); i++) {
			usedIDs.add(((SpeciesType) ids.get(i)).getId());
		}
		ids = model.getListOfCompartments();
		for (int i = 0; i < model.getNumCompartments(); i++) {
			usedIDs.add(((Compartment) ids.get(i)).getId());
		}
		ids = model.getListOfParameters();
		for (int i = 0; i < model.getNumParameters(); i++) {
			usedIDs.add(((Parameter) ids.get(i)).getId());
		}
		ids = model.getListOfReactions();
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) ids.get(i);
			usedIDs.add(reaction.getId());
			ListOf ids2 = reaction.getListOfReactants();
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				SpeciesReference reactant = (SpeciesReference) ids2.get(j);
				if ((reactant.isSetId()) && (!reactant.getId().equals(""))) {
					usedIDs.add(reactant.getId());
				}
			}
			ids2 = reaction.getListOfProducts();
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				SpeciesReference product = (SpeciesReference) ids2.get(j);
				if ((product.isSetId()) && (!product.getId().equals(""))) {
					usedIDs.add(product.getId());
				}
			}
		}
		ids = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) {
			usedIDs.add(((Species) ids.get(i)).getId());
		}
		ids = model.getListOfConstraints();
		for (int i = 0; i < model.getNumConstraints(); i++) {
			if (((Constraint) ids.get(i)).isSetMetaId()) {
				usedIDs.add(((Constraint) ids.get(i)).getMetaId());
			}
		}
		ids = model.getListOfEvents();
		for (int i = 0; i < model.getNumEvents(); i++) {
			if (((org.sbml.libsbml.Event) ids.get(i)).isSetId()) {
				usedIDs.add(((org.sbml.libsbml.Event) ids.get(i)).getId());
			}
		}
		return usedIDs;
	}

	/**
	 * Check for cycles in initialAssignments and assignmentRules
	 */
	public static boolean checkCycles(SBMLDocument document) {
		Model model = document.getModel();
		ListOf listOfReactions = model.getListOfReactions();
		String[] rateLaws = new String[(int) model.getNumReactions()];
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) listOfReactions.get(i);
			if (reaction.getKineticLaw()==null || reaction.getKineticLaw().getMath()==null) {
				rateLaws[i] = reaction.getId() + " = 0.0"; 
			} else {
				rateLaws[i] = reaction.getId() + " = " + myFormulaToString(reaction.getKineticLaw().getMath());
			}
		}
		ListOf listOfInitials = model.getListOfInitialAssignments();
		String[] initRules = new String[(int) model.getNumInitialAssignments()];
		for (int i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment init = (InitialAssignment) listOfInitials.get(i);
			initRules[i] = init.getSymbol() + " = " + myFormulaToString(init.getMath());
		}
		ListOf listOfRules = model.getListOfRules();
		String[] rules = new String[(int) model.getNumRules()];
		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = (Rule) listOfRules.get(i);
			if (rule.isAlgebraic()) {
				rules[i] = "0 = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else if (rule.isAssignment()) {
				rules[i] = rule.getVariable() + " = " + SBMLutilities.myFormulaToString(rule.getMath());
			}
			else {
				rules[i] = "d( " + rule.getVariable() + " )/dt = " + SBMLutilities.myFormulaToString(rule.getMath());
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
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
		long numErrors = document.checkConsistency();
		/*
		String message = "";
		for (long i = 0; i < numErrors; i++) {
			String error = document.getError(i).getMessage(); // .replace(". ",
			// ".\n");
			message += i + ":" + error + "\n";
		}
		*/
		if (numErrors > 0) {
			JOptionPane.showMessageDialog(Gui.frame, "Algebraic rules make model overdetermined.", "Model is Overdetermined",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Create check if species used in reaction
	 */
	public static boolean usedInReaction(SBMLDocument document, String id) {
		for (int i = 0; i < document.getModel().getNumReactions(); i++) {
			for (int j = 0; j < document.getModel().getReaction(i).getNumReactants(); j++) {
				if (document.getModel().getReaction(i).getReactant(j).getSpecies().equals(id)) {
					return true;
				}
			}
			for (int j = 0; j < document.getModel().getReaction(i).getNumProducts(); j++) {
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
		for (int i = 0; i < document.getModel().getNumReactions(); i++) {
			for (int j = 0; j < document.getModel().getReaction(i).getNumReactants(); j++) {
				if (document.getModel().getReaction(i).getReactant(j).getSpecies().equals(id)
						&& (document.getModel().getReaction(i).getNumProducts() > 0 || document.getModel().getReaction(i).getNumReactants() > 1)) {
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
	 * Check if a variable is in use.
	 */
	public static boolean variableInUse(SBMLDocument document, String species, boolean zeroDim, boolean displayMessage, 
			boolean checkReactions) {
		Model model = document.getModel();
		boolean inUse = false;
		if (species.equals("")) {
			return inUse;
		}
		boolean usedInModelConversionFactor = false;

		ArrayList<String> stoicMathUsing = new ArrayList<String>();
		ArrayList<String> reactantsUsing = new ArrayList<String>();
		ArrayList<String> productsUsing = new ArrayList<String>();
		ArrayList<String> modifiersUsing = new ArrayList<String>();
		ArrayList<String> kineticLawsUsing = new ArrayList<String>();
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
			for (int i = 0; i < model.getNumSpecies(); i++) {
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
			for (int i = 0; i < model.getNumReactions(); i++) {
				Reaction reaction = (Reaction) model.getListOfReactions().get(i);
				for (int j = 0; j < reaction.getNumProducts(); j++) {
					if (reaction.getProduct(j).isSetSpecies()) {
						String specRef = reaction.getProduct(j).getSpecies();
						if (species.equals(specRef)) {
							inUse = true;
							productsUsing.add(reaction.getId());
						}
						else if (reaction.getProduct(j).isSetStoichiometryMath()) {
							String[] vars = SBMLutilities.myFormulaToString(reaction.getProduct(j).getStoichiometryMath().getMath()).split(
									" |\\(|\\)|\\,");
							for (int k = 0; k < vars.length; k++) {
								if (vars[k].equals(species)) {
									stoicMathUsing.add(reaction.getId() + "/" + specRef);
									inUse = true;
									break;
								}
							}
						}
					}
				}
				for (int j = 0; j < reaction.getNumReactants(); j++) {
					if (reaction.getReactant(j).isSetSpecies()) {
						String specRef = reaction.getReactant(j).getSpecies();
						if (species.equals(specRef)) {
							inUse = true;
							reactantsUsing.add(reaction.getId());
						}
						else if (reaction.getReactant(j).isSetStoichiometryMath()) {
							String[] vars = SBMLutilities.myFormulaToString(reaction.getReactant(j).getStoichiometryMath().getMath()).split(
									" |\\(|\\)|\\,");
							for (int k = 0; k < vars.length; k++) {
								if (vars[k].equals(species)) {
									stoicMathUsing.add(reaction.getId() + "/" + specRef);
									inUse = true;
									break;
								}
							}
						}
					}
				}
				for (int j = 0; j < reaction.getNumModifiers(); j++) {
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
		ListOf ia = document.getModel().getListOfInitialAssignments();
		for (int i = 0; i < document.getModel().getNumInitialAssignments(); i++) {
			InitialAssignment init = (InitialAssignment) ia.get(i);
			String initStr = SBMLutilities.myFormulaToString(init.getMath());
			String[] vars = initStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					initsUsing.add(init.getSymbol() + " = " + SBMLutilities.myFormulaToString(init.getMath()));
					inUse = true;
					break;
				}
			}
		}
		ListOf r = document.getModel().getListOfRules();
		for (int i = 0; i < document.getModel().getNumRules(); i++) {
			Rule rule = (Rule) r.get(i);
			String initStr = SBMLutilities.myFormulaToString(rule.getMath());
			if (rule.isAssignment() || rule.isRate()) {
				initStr += " = " + rule.getVariable();
			}
			String[] vars = initStr.split(" |\\(|\\)|\\,");
			for (int j = 0; j < vars.length; j++) {
				if (vars[j].equals(species)) {
					if (rule.isAssignment()) {
						rulesUsing.add(rule.getVariable() + " = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					else if (rule.isRate()) {
						rulesUsing.add("d(" + rule.getVariable() + ")/dt = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					else {
						rulesUsing.add("0 = " + SBMLutilities.myFormulaToString(rule.getMath()));
					}
					inUse = true;
					break;
				}
			}
		}
		ListOf c = document.getModel().getListOfConstraints();
		for (int i = 0; i < document.getModel().getNumConstraints(); i++) {
			Constraint constraint = (Constraint) c.get(i);
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
		ListOf e = model.getListOfEvents();
		for (int i = 0; i < model.getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) e.get(i);
			String trigger = SBMLutilities.myFormulaToString(event.getTrigger().getMath());
			String eventStr = trigger;
			if (event.isSetDelay()) {
				eventStr += " " + SBMLutilities.myFormulaToString(event.getDelay().getMath());
			}
			for (int j = 0; j < event.getNumEventAssignments(); j++) {
				eventStr += " " + (event.getEventAssignment(j).getVariable()) + " = "
						+ SBMLutilities.myFormulaToString(event.getEventAssignment(j).getMath());
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
		for (int i = 0; i < model.getNumReactions(); i++) {
			Reaction reaction = (Reaction) model.getListOfReactions().get(i);
			for (int j = 0; j < reaction.getNumProducts(); j++) {
				if (reaction.getProduct(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getProduct(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						specRef.getStoichiometryMath().setMath(SBMLutilities.updateMathVar(specRef.getStoichiometryMath().getMath(), origId, newId));
					}
				}
			}
			if (isSpecies) {
				for (int j = 0; j < reaction.getNumModifiers(); j++) {
					if (reaction.getModifier(j).isSetSpecies()) {
						ModifierSpeciesReference specRef = reaction.getModifier(j);
						if (origId.equals(specRef.getSpecies())) {
							specRef.setSpecies(newId);
						}
					}
				}
			}
			for (int j = 0; j < reaction.getNumReactants(); j++) {
				if (reaction.getReactant(j).isSetSpecies()) {
					SpeciesReference specRef = reaction.getReactant(j);
					if (isSpecies && origId.equals(specRef.getSpecies())) {
						specRef.setSpecies(newId);
					}
					if (specRef.isSetStoichiometryMath()) {
						specRef.getStoichiometryMath().setMath(SBMLutilities.updateMathVar(specRef.getStoichiometryMath().getMath(), origId, newId));
					}
				}
			}
			reaction.getKineticLaw().setMath(SBMLutilities.updateMathVar(reaction.getKineticLaw().getMath(), origId, newId));
		}
		if (document.getLevel() > 2) {
			if (model.isSetConversionFactor() && origId.equals(model.getConversionFactor())) {
				model.setConversionFactor(newId);
			}
			if (model.getNumSpecies() > 0) {
				for (int i = 0; i < model.getNumSpecies(); i++) {
					Species species = (Species) model.getListOfSpecies().get(i);
					if (species.isSetConversionFactor()) {
						if (origId.equals(species.getConversionFactor())) {
							species.setConversionFactor(newId);
						}
					}
				}
			}
		}
		if (model.getNumInitialAssignments() > 0) {
			for (int i = 0; i < model.getNumInitialAssignments(); i++) {
				InitialAssignment init = (InitialAssignment) model.getListOfInitialAssignments().get(i);
				if (origId.equals(init.getSymbol())) {
					init.setSymbol(newId);
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
		if (model.getNumRules() > 0) {
			for (int i = 0; i < model.getNumRules(); i++) {
				Rule rule = (Rule) model.getListOfRules().get(i);
				if (rule.isSetVariable() && origId.equals(rule.getVariable())) {
					rule.setVariable(newId);
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
		if (model.getNumConstraints() > 0) {
			for (int i = 0; i < model.getNumConstraints(); i++) {
				Constraint constraint = (Constraint) model.getListOfConstraints().get(i);
				constraint.setMath(SBMLutilities.updateMathVar(constraint.getMath(), origId, newId));
			}
		}
		if (model.getNumEvents() > 0) {
			for (int i = 0; i < model.getNumEvents(); i++) {
				org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) model.getListOfEvents().get(i);
				if (event.isSetTrigger()) {
					event.getTrigger().setMath(SBMLutilities.updateMathVar(event.getTrigger().getMath(), origId, newId));
				}
				if (event.isSetDelay()) {
					event.getDelay().setMath(SBMLutilities.updateMathVar(event.getDelay().getMath(), origId, newId));
				}
				for (int j = 0; j < event.getNumEventAssignments(); j++) {
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
		for (int i = 0; i < document.getModel().getNumRules(); i++) {
			Rule rule = document.getModel().getRule(i);
			if (rule.getVariable().equals(val)) {
				JOptionPane.showMessageDialog(Gui.frame, varType + " cannot be constant if updated by a rule.", varType + " Cannot Be Constant",
						JOptionPane.ERROR_MESSAGE);
				return true;
			}
		}
		for (int i = 0; i < document.getModel().getNumEvents(); i++) {
			org.sbml.libsbml.Event event = (org.sbml.libsbml.Event) document.getModel().getListOfEvents().get(i);
			for (int j = 0; j < event.getNumEventAssignments(); j++) {
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
	public static void check(String file) {
		// Hack to avoid weird bug.
		// By reloading the file before consistency checks, it seems to avoid a
		// crash when attempting to save a newly added parameter with no units
		SBMLDocument document = Gui.readSBML(file);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, true);
		long numErrors = document.checkConsistency();
		String message = "";
		for (long i = 0; i < numErrors; i++) {
			String error = document.getError(i).getMessage(); // .replace(". ",
			// ".\n");
			message += i + ":" + error + "\n";
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
		}
	}

	/**
	 * Checks consistency of the sbml file.
	 */
	public static boolean checkUnits(SBMLDocument document) {
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_GENERAL_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_IDENTIFIER_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, true);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MATHML_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_SBO_CONSISTENCY, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_MODELING_PRACTICE, false);
		document.setConsistencyChecks(libsbml.LIBSBML_CAT_OVERDETERMINED_MODEL, false);
		long numErrorsWarnings = document.checkConsistency();
		long numErrors = 0;
		String message = "Change in unit definition causes the following unit errors:\n";
		for (long i = 0; i < numErrorsWarnings; i++) {
			// if (document.getError(i).isWarning()) {
			String error = document.getError(i).getMessage();
			message += i + ":" + error + "\n";
			numErrors++;
			// }
		}
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
		// createFunction(model, "rayleigh", "Rayleigh distribution",
		// "lambda(s,s*sqrt(pi/2))");
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
			f.setMath(libsbml.parseFormula(formula));
		}
	}
	

	public static boolean isBoolean(SBMLDocument document, ASTNode node) {
	  if (node == null) {
	    return false;
	  } else if ( node.isBoolean() ) {
	    return true;
	  } else if (node.getType() == libsbml.AST_FUNCTION) {
	    FunctionDefinition fd = document.getModel().getFunctionDefinition( node.getName() );
	    if (fd != null && fd.isSetMath()) {
	      return isBoolean( document, fd.getMath().getRightChild() );
	    } else {
	      return false;
	    }
	  } else if (node.getType() == libsbml.AST_FUNCTION_PIECEWISE) {
	    for (int c = 0; c < node.getNumChildren(); c += 2) {
	      if ( !isBoolean( document, node.getChild(c) ) ) return false;
	    }
	    return true;
	  }
	  return false;
	}

}
