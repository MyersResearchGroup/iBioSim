package lhpn2sbml.parser;

import gcm2sbml.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class ExprTree {

	String op;

	char isit; // b=Boolean, i=Integer, c=Continuous, n=Number, t=Truth value,

	// w=bitWise, a=Arithmetic, r=Relational, l=Logical
	double lvalue, uvalue;

	String variable;

	double real;

	boolean logical;

	ExprTree r1, r2;

	private String tokvalue = "";

	private int position = 0;

	public int token = 0;

	private ExprTree newresult;

	private ArrayList<String> signals;

	// private String result;

	// private int nsignals;// nevents, nplaces;

	private LHPNFile lhpn;

	public ExprTree(LHPNFile lhpn) {
		this.lhpn = lhpn;
		String[] bools = lhpn.getBooleanVars();
		String[] conts = lhpn.getContVars();
		String[] ints = lhpn.getIntVars();
		signals = new ArrayList<String>();
		for (int j = 0; j < bools.length; j++) {
			signals.add(bools[j]);
		}
		for (int j = 0; j < conts.length; j++) {
			signals.add(conts[j]);
		}
		for (int j = 0; j < ints.length; j++) {
			signals.add(ints[j]);
		}
	}

	ExprTree(char willbe, int lNV, int uNV, String var) {
		op = "";
		r1 = null;
		r2 = null;
		isit = willbe;
		if ((isit == 'b') || (isit == 't'))
			logical = true;
		else
			logical = false;
		uvalue = uNV;
		lvalue = lNV;
		variable = var;
		real = 0;
	}

	ExprTree(ExprTree nr1, ExprTree nr2, String nop, char willbe) {
		op = nop;
		r1 = nr1;
		r2 = nr2;
		isit = willbe;
		if ((isit == 'r') || (isit == 'l')) {
			logical = true;
			uvalue = 1;
			lvalue = 0;
		}
		else {
			logical = false;
			uvalue = INFIN;
			lvalue = -INFIN;
		}
		variable = null;
	}

	ExprTree(ExprTree source) {
		if (source.op != null) {
			op = source.op;
		}
		isit = source.isit;
		lvalue = source.lvalue;
		uvalue = source.uvalue;
		if (source.variable != null) {
			variable = source.variable;
		}
		real = source.real;
		logical = source.logical;
		if (source.r1 != null) {
			r1 = source.r1;
		}
		if (source.r2 != null) {
			r2 = source.r2;
		}
		if (source.tokvalue != null) {
			tokvalue = source.tokvalue;
		}
		position = source.position;
		token = source.token;
		// if (source.result != null) {
		// result = source.result;
		// }
		if (source.newresult != null) {
			newresult = source.newresult;
		}
		if (source.signals != null) {
			signals = source.signals;
		}
		if (source.lhpn != null) {
			lhpn = source.lhpn;
		}
	}

	// public int intexpr_gettok(String expr) {
	// char c;
	// boolean readword;
	// tokvalue = "";
	// readword = false;
	// while (position < expr.length()) {
	// c = expr.charAt(position);
	// position++;
	// switch (c) {
	// case '(':
	// case ')':
	// case '[':
	// case ']':
	// case ',':
	// case '~':
	// case '|':
	// case '&':
	// case '+':
	// case '*':
	// case '^':
	// case '/':
	// case '%':
	// case '=':
	// case '<':
	// case '>':
	// if (!readword)
	// return c;
	// else {
	// position--;
	// return WORD;
	// }
	// case '-':
	// if (tokvalue.endsWith("E") || tokvalue.endsWith("e")) {
	// readword = true;
	// tokvalue += c;
	// break;
	// }
	// else if (!readword) {
	// if (expr.charAt(position) == '>') {
	// position++;
	// return (IMPLIES);
	// }
	// else
	// return (c);
	// }
	// else {
	// position--;
	// return (WORD);
	// }
	// case ' ':
	// if (readword) {
	// return (WORD);
	// }
	// break;
	// default:
	// readword = true;
	// tokvalue += c;
	// break;
	// }
	// }
	// if (!readword)
	// return (END_OF_STRING);
	// else
	// return (WORD);
	// }

	public int intexpr_gettok(String expr) {
		char c;
		boolean readword;
		boolean readnum;
		boolean readsci;
		boolean readsign;

		readword = false;
		readnum = false;
		readsci = false;
		readsign = false;
		tokvalue = "";
		while (position < expr.length()) {
			c = expr.charAt(position);
			position++;
			switch (c) {
			case '(':
			case ')':
			case '[':
			case ']':
			case ',':
			case '~':
			case '|':
			case '&':
			case '*':
			case '^':
			case '/':
			case '%':
			case '=':
			case '<':
			case '>':
				if ((!readword) && (!readnum) && (!readsci)) {
					return (c);
				}
				else {
					position--;
					return (WORD);
				}
			case '+':
			case '-':
				if ((readsci) && (!readnum) && (readsign)) {
					tokvalue += c;
					readsign = false;
					break;
				}
				if ((readsci) && (!readnum) && (!readsign)) {
					return -1;
				}
				else if ((!readword) && (!readnum) && (!readsci)) {
					return (c);
				}
				else {
					position--;
					return (WORD);
				}
			case ' ':
				if (readword) {
					return (WORD);
				}
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				if (!readword) {
					readnum = true;
				}
				tokvalue += c;
				break;
			case '.':
				if (readsci) {
					return -1;
				}
				else if (!readword) {
					readnum = true;
				}
				tokvalue += c;
				break;
			case 'E':
			case 'e':
				if (readsci) {
					return -1;
				}
				else if (readnum) {
					readsci = true;
					readnum = false;
					readsign = true;
					tokvalue += c;
					break;
				}
			default:
				if ((readnum) || (readsci)) {
					return -1;
				}
				readword = true;
				tokvalue += c;
				break;
			}
		}
		if ((!readword) && (!readnum)) {
			return (END_OF_STRING);
		}
		else if (readword || readnum) {
			return (WORD);
		}
		return -1;
	}

	public boolean intexpr_U(String expr) {
		// System.out.println("U: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		double temp;
		ExprTree newresult = null;

		switch (token) {
		case WORD:
			if (tokvalue.equals("and")) {
				token = intexpr_gettok(expr);
				if ((token) != '(') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.print("ERROR: Expected a (\n");
					return false;
				}
				token = intexpr_gettok(expr);
				if (!intexpr_R(expr)) {
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (this).lvalue & (int) newresult.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else {
					// (result) = new ExprTree((result), newresult, "&", 'w');
					setNodeValues((this), newresult, "&", 'w');
				}
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("or")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (this).lvalue | (int) newresult.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else {
					// (result) = new ExprTree((result), newresult, "|", 'w');
					setNodeValues((this), newresult, "|", 'w');
				}
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("exor")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (this).lvalue ^ (int) newresult.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else {
					// (result) = new ExprTree((result), newresult, "^", 'w');
					setNodeValues((this), newresult, "^", 'w');
				}
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("not")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
							+ "\nU: Expected a (");
					// System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((this).isit == 'n') || ((this).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = ~(int) (this).lvalue;
					(this).uvalue = (this).lvalue;
				}
				else {
					// (result) = new ExprTree((result), null, "~", 'w');
					setNodeValues((this), null, "~", 'w');
				}
				(token) = intexpr_gettok(expr);
			}
			else if ((tokvalue.equals("true")) || (tokvalue.equals("t")) || tokvalue.equals("TRUE")
					|| tokvalue.equals("T")) {
				// (result) = new ExprTree('t', 1, 1, null);
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if ((tokvalue.equals("false")) || (tokvalue.equals("f"))
					|| tokvalue.equals("FALSE") || tokvalue.equals("F")) {
				// (result) = new ExprTree('t', 0, 0, null);
				setVarValues('t', 0, 0, null);
				(token) = intexpr_gettok(expr);
			}
			else {
				// do boolean lookup here!!!
				// for (i = 0; i < nsignals; i++) {
				// System.out.println("Signal " + signals[i]);
				if (signals.contains(tokvalue)) {
					if (lhpn.isInput(tokvalue) || lhpn.isOutput(tokvalue)) {
						// printf("successful lookup of boolean variable
						// %s\n",signals[i]);
						// (result) = new ExprTree('b', 0, 1, signals[i]);
						setVarValues('b', 0, 1, tokvalue);
						(token) = intexpr_gettok(expr);
						return true;
					}
					else if (lhpn.isInteger(tokvalue)) {
						setVarValues('i', -INFIN, INFIN, tokvalue);
						(token) = intexpr_gettok(expr);
						return true;
					}
					else {
						setVarValues('c', -INFIN, INFIN, tokvalue);
						(token) = intexpr_gettok(expr);
						return true;
					}
				}
				// }
				if (tokvalue.equals("")) {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr + "\nU1:("
							+ tokvalue + "): Expected a ID, Number, or a (\n");
					// System.out.printf("U1:ERROR(%s): Expected a ID, Number,
					// or a (\n", tokvalue);
					return false;
				}
				else if ((int) (tokvalue.charAt(0)) > ('9') || ((int) (tokvalue.charAt(0)) < '0')) {
					Utility.createErrorMessage("ERROR", "Invalid expression: " + expr + "\nU1:("
							+ tokvalue + "): Expected a ID, Number, or a (\n");
					// System.out.printf("U1:ERROR(%s): Expected a ID, Number,
					// or a (\n", tokvalue);
					return false;
				}
				temp = Double.parseDouble(tokvalue);
				// result = new ExprTree('n', temp, temp, null);
				setVarValues('n', temp, temp, null);
				token = intexpr_gettok(expr);
				// printf("resolved number %f\n",temp);
			}
			break;
		case '(':
			(token) = intexpr_gettok(expr);
			if (!intexpr_L(expr))
				return false;
			if ((token) != ')') {
				Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
						+ "\nU: Expected a (");
				// System.out.printf("ERROR: Expected a )\n");
				return false;
			}
			(token) = intexpr_gettok(expr);
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nU2: Expected a ID, Number, or a (");
			// System.out.printf("U2:ERROR: Expected a ID, Number, or a (\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_T(String expr) {
		// System.out.println("T: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		switch (token) {
		case WORD:
		case '(':
			if (!intexpr_U(expr))
				return false;
			break;
		case '-':
			(token) = intexpr_gettok(expr);
			if (!intexpr_U(expr))
				return false;
			// simplify if operands are static
			if (((this).isit == 'n') || ((this).isit == 't')) {
				(this).isit = 'n';
				(this).lvalue = -((this).lvalue);
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), null, "U-", 'a');
				setNodeValues((this), null, "U-", 'a');
			}
			break;
		default:
			// System.out.println(token);
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nT: Expected a ID, Number, (, or -\n");
			// System.out.printf("T:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_C(String expr) {
		// System.out.println("C: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		// ExprTree newresult = null;
		newresult = new ExprTree(this);
		switch (token) {
		case '*':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_T(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue * newresult.lvalue;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "*", 'a');
				setNodeValues((this), newresult, "*", 'a');
			}
			if (!intexpr_C(expr))
				return false;
			break;
		case '^':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_T(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 'n';
				(this).lvalue = lvalue * newresult.lvalue;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "*", 'a');
				setNodeValues((this), newresult, "^", 'a');
			}
			if (!intexpr_C(expr))
				return false;
			break;
		case '/':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_T(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue / newresult.lvalue;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "/", 'a');
				setNodeValues((this), newresult, "/", 'a');
			}
			if (!intexpr_C(expr))
				return false;
			break;
		case '%':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_T(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue % newresult.lvalue;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "%", 'a');
				setNodeValues((this), newresult, "%", 'a');
			}
			if (!intexpr_C(expr))
				return false;
			break;
		case '+':
		case '-':
		case ')':
		case '[':
		case ']':
		case '|':
		case '&':
		case '=':
		case '<':
		case '>':
		case ',':
		case IMPLIES:
		case END_OF_STRING:
			break;
		case '(':
		case WORD:
			if (!newresult.intexpr_T(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue * newresult.lvalue;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "*", 'a');
				setNodeValues((this), newresult, "*", 'a');
			}
			if (!intexpr_C(expr))
				return false;
			break;

		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nC: Expected a * or /\n");
			// System.out.printf("ERROR: Expected a * or /\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_B(String expr) {
		// System.out.println("B: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		// ExprTree newresult = null;
		newresult = new ExprTree(this);
		switch (token) {
		case '+':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_S(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue + newresult.lvalue;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "+", 'a');
				setNodeValues((this), newresult, "+", 'a');
			}
			if (!intexpr_B(expr))
				return false;
			break;
		case '-':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_S(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue - newresult.lvalue;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "-", 'a');
				setNodeValues((this), newresult, "-", 'a');
			}
			if (!intexpr_B(expr))
				return false;
			break;
		case ')':
		case '[':
		case ']':
		case '|':
		case '&':
		case '=':
		case '<':
		case '>':
		case ',':
		case IMPLIES:
		case END_OF_STRING:
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nB: Expected a + or -\n");
			// System.out.printf("ERROR: Expected a + or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_S(String expr) {
		// System.out.println("S: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		switch (token) {
		case WORD:
		case '(':
		case '-':
			if (!intexpr_T(expr))
				return false;
			if (!intexpr_C(expr))
				return false;
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nS: Expected a ID, Number, (, or -\n");
			// System.out.printf("S:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_R(String expr) {
		// System.out.println("R: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		switch (token) {
		case WORD:
		case '(':
		case '-':
			if (!intexpr_S(expr))
				return false;
			if (!intexpr_B(expr))
				return false;
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nR: Expected a ID, Number, (, or -\n");
			// System.out.printf("R:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_P(String expr) {
		// System.out.println("P: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		// ExprTree newresult = null;
		newresult = new ExprTree(this);
		int spos, i;
		String ineq = "";
		String comp;
		// printf("P\n");
		switch (token) {
		case '=':
			spos = position;
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_R(expr))
				return false;
			token = newresult.token;
			tokvalue = newresult.tokvalue;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 't';
				if (this.lvalue == newresult.lvalue) {
					this.lvalue = 1;
				}
				else {
					this.lvalue = 0;
				}
				(this).uvalue = (this).lvalue;
			}
			else {
				if ((this).isit == 'c') {
					comp = variable;
					comp += "=";
					int paren = 0;
					for (i = spos; i < position; i++) {
						if (expr.charAt(i) == '(')
							paren++;
						if (expr.charAt(i) == ')')
							paren--;
						ineq = ineq + expr.charAt(i);
					}
					// ineq[i - spos + paren] = 0;
					comp += ineq;
					// printf("looking for %s\n",comp.c_str());
					// for (i = 0; i < nsignals; i++) {
					if (signals.contains(comp)) {
						// printf("successful lookup of boolean variable
						// '%s'\n",signals[i]->name);
						this.isit = 'b';
						this.variable = comp;
						this.lvalue = 0;
						this.uvalue = 1;
						return true;
					}
					else {
						signals.add(comp);
						this.isit = 'b';
						this.variable = comp;
						this.lvalue = 0;
						this.uvalue = 1;
						return true;
					}
					// }
				}
				else {
					// (result) = new ExprTree((result), newresult, "==", 'r');
					setNodeValues((this), newresult, "==", 'r');
				}
			}
			break;
		case '>':
			spos = position;
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			if ((token) == '=') {
				spos = position;
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				tokvalue = newresult.tokvalue;
				position = newresult.position;
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))) {
					(this).isit = 't';
					if ((this).lvalue >= newresult.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
				else {
					if ((this).isit == 'c') {
						comp = variable;
						comp += ">=";
						int paren = 0;
						ineq = new String();
						for (i = spos; i < position; i++) {
							if (expr.charAt(i) == '(')
								paren++;
							if (expr.charAt(i) == ')')
								paren--;
							ineq = ineq + expr.charAt(i);
						}
						// ineq[i - spos + paren] = 0;
						comp += ineq;
						// printf("looking for %s\n",comp.c_str());
						// for (i = 0; i < nsignals; i++) {
						if (signals.contains(comp)) {
							// printf("successful lookup of boolean variable
							// '%s'\n",signals[i]->name);
							(this).isit = 'b';
							this.variable = comp;
							(this).lvalue = 0;
							(this).uvalue = 1;
							return true;
						}
						// }
						else {
							signals.add(comp);
							this.isit = 'b';
							this.variable = comp;
							this.lvalue = 0;
							this.uvalue = 1;
							return true;
						}
					}
					else {
						// (result) = new ExprTree((result), newresult, ">=",
						// 'r');
						setNodeValues((this), newresult, ">=", 'r');
					}
				}
			}
			else {
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!intexpr_R(expr))
					return false;
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))) {
					(this).isit = 't';
					if ((this).lvalue > newresult.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
				else {
					if ((this).isit == 'c') {
						comp = variable;
						comp += ">";
						int paren = 0;
						ineq = new String();
						for (i = spos; i < position; i++) {
							if (expr.charAt(i) == '(')
								paren++;
							if (expr.charAt(i) == ')')
								paren--;
							ineq = ineq + expr.charAt(i);
						}
						// ineq[i - spos + paren] = 0;
						comp += ineq;
						// printf("looking for %s\n",comp.c_str());
						// for (i = 0; i < nsignals; i++) {
						if (signals.contains(comp)) {
							// printf("successful lookup of boolean variable
							// '%s'\n",signals[i]->name);
							(this).isit = 'b';
							(this).variable = comp;
							(this).lvalue = 0;
							(this).uvalue = 1;
							return true;
						}
						else {
							signals.add(comp);
							this.isit = 'b';
							this.variable = comp;
							this.lvalue = 0;
							this.uvalue = 1;
							return true;
						}
						// }
					}
					else {
						// (result) = new ExprTree((result), newresult, ">",
						// 'r');
						setNodeValues((this), newresult, ">", 'r');
					}
				}
			}
			break;
		case '<':
			spos = position;
			(token) = intexpr_gettok(expr);
			if ((token) == '=') {
				spos = position;
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				tokvalue = newresult.tokvalue;
				position = newresult.position;
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))) {
					(this).isit = 't';
					if ((this).lvalue <= newresult.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
				else {
					if ((this).isit == 'c') {
						comp = variable;
						comp += "<=";
						int paren = 0;
						ineq = new String();
						for (i = spos; i < position; i++) {
							if (expr.charAt(i) == '(')
								paren++;
							if (expr.charAt(i) == ')')
								paren--;
							ineq = ineq + expr.charAt(i);
						}
						// ineq[i - spos + paren] = 0;
						comp += ineq;
						// printf("looking for %s\n",comp.c_str());
						// for (i = 0; i < nsignals; i++) {
						if (signals.contains(comp)) {
							// printf("successful lookup of boolean variable
							// '%s'\n",signals[i]->name);
							(this).isit = 'b';
							(this).variable = comp;
							(this).lvalue = 0;
							(this).uvalue = 1;
							return true;
						}
						else {
							signals.add(comp);
							this.isit = 'b';
							this.variable = comp;
							this.lvalue = 0;
							this.uvalue = 1;
							return true;
						}
						// }
					}
					else {
						// (result) = new ExprTree((result), newresult, "<=",
						// 'r');
						setNodeValues((this), newresult, "<=", 'r');
					}
				}
			}
			else {
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				tokvalue = newresult.tokvalue;
				position = newresult.position;
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))) {
					(this).isit = 't';
					if ((this).lvalue < newresult.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
				else {
					if ((this).isit == 'c') {
						comp = variable;
						comp += "<";
						int paren = 0;
						ineq = new String();
						for (i = spos; i < position; i++) {
							if (expr.charAt(i) == '(')
								paren++;
							if (expr.charAt(i) == ')')
								paren--;
							ineq = ineq + expr.charAt(i);
						}
						// ineq[i - spos + paren] = 0;
						comp += ineq;
						System.out.printf("looking for %s\n", comp);
						// for (i = 0; i < nsignals; i++) {
						if (signals.contains(comp)) {
							// System.out.printf("successful lookup of
							// boolean variable '%s'\n",
							// signals[i]);
							(this).isit = 'b';
							(this).variable = comp;
							(this).lvalue = 0;
							(this).uvalue = 1;
							return true;
						}
						else {
							signals.add(comp);
							this.isit = 'b';
							this.variable = comp;
							this.lvalue = 0;
							this.uvalue = 1;
							return true;
						}
						// }
					}
					else {
						// (result) = new ExprTree((result), newresult, "<",
						// 'r');
						setNodeValues((this), newresult, "<", 'r');
					}
				}
			}
			break;
		case '[':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_R(expr))
				return false;
			token = newresult.token;
			tokvalue = newresult.tokvalue;
			position = newresult.position;
			if ((token) != ']') {
				Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
						+ "\nP: Expected a ]");
				// System.out.printf("ERROR: Expected a ]\n");
				return false;
			}
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 't';
				(this).lvalue = (((int) (this).lvalue) >> ((int) newresult.lvalue)) & 1;
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "[]", 'w');
				setNodeValues((this), newresult, "[]", 'w');
			}
			(token) = intexpr_gettok(expr);
			break;
		case '&':
		case '|':
		case ')':
		case IMPLIES:
		case END_OF_STRING:
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nP: Expected a [, =, <, or >");
			// System.out.printf("ERROR: Expected a [, =, <, or >\n");
			return false;
		}
		// printf("/P\n");
		return true;
	}

	public boolean intexpr_O(String expr) {
		// System.out.println("O: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		switch (token) {
		case WORD:
		case '(':
		case '-':
			if (!intexpr_R(expr))
				return false;
			if (!intexpr_P(expr))
				return false;
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nO: Expected a ID, Number, or a (");
			// System.out.printf("O:ERROR: Expected a ID, Number, or a (\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_N(String expr) {
		// System.out.println("N: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		switch (token) {
		case WORD:
		case '-':
		case '(':
			if (!intexpr_O(expr))
				return false;
			break;
		case '~':
			(token) = intexpr_gettok(expr);
			if (!intexpr_O(expr))
				return false;
			// simplify if operands are static
			if (((this).isit == 'n') || ((this).isit == 't')) {
				(this).isit = 't';
				if (this.lvalue == 1) {
					this.lvalue = 0;
				}
				else {
					this.lvalue = 1;
				}
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), null, "!", 'l');
				setNodeValues((this), null, "!", 'l');
			}
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nN: Expected a ID, Number, (, or -");
			// System.out.printf("N:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_E(String expr) {
		newresult = new ExprTree(this);
		// System.out.println("E: token = " + token + " tokvalue = " + tokvalue
		// + " result = ");
		switch (token) {
		case '&':
			token = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_N(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			// System.out.println(newresult);
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 't';
				if ((this.lvalue == 0) || (newresult.lvalue == 0)) {
					this.lvalue = 0;
				}
				else {
					this.lvalue = 1;
				}
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "&&", 'l');
				setNodeValues((this), newresult, "&&", 'l');
			}
			if (!intexpr_E(expr))
				return false;
			break;
		case '|':
		case ')':
		case IMPLIES:
		case END_OF_STRING:
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr + "\nE:(" + token
					+ "): Expected an &");
			// System.out.printf("ERROR(%c): Expected an &\n", (token));
			return false;
		}
		return true;
	}

	public boolean intexpr_D(String expr) {
		// System.out.println("D: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		newresult = new ExprTree(this);
		switch (token) {
		case '|':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			if (!newresult.intexpr_M(expr))
				return false;
			token = newresult.token;
			position = newresult.position;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 't';
				if (this.lvalue != 0 || newresult.lvalue != 0) {
					this.lvalue = 1;
				}
				else {
					this.lvalue = 0;
				}
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "||", 'l');
				setNodeValues((this), newresult, "||", 'l');
			}
			if (!intexpr_D(expr))
				return false;
			break;
		case ')':
		case END_OF_STRING:
			break;
		case IMPLIES:
			(token) = intexpr_gettok(expr);
			if (!intexpr_M(expr))
				return false;
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))) {
				(this).isit = 't';
				if (this.lvalue != 0 || newresult.lvalue == 0) {
					this.lvalue = 1;
				}
				else {
					this.lvalue = 0;
				}
				(this).uvalue = (this).lvalue;
			}
			else {
				// (result) = new ExprTree((result), newresult, "->", 'l');
				setNodeValues(this, newresult, "->", 'l');
			}
			if (!intexpr_D(expr))
				return false;
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nD: Expected an | or ->");
			// System.out.printf("ERROR: Expected an | or ->\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_M(String expr) {
		// System.out.println("M: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		switch (token) {
		case WORD:
		case '(':
		case '~':
		case '-':
			if (!intexpr_N(expr))
				return false;
			if (!intexpr_E(expr))
				return false;
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nM: Expected a ID, Number, (, or -");
			// System.out.printf("M: ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_L(String expr) {
		// System.out.println(expr);
		// System.out.println("L: token = " + token + " tokvalue = " + tokvalue
		// + " result = " + result);
		switch (token) {
		case WORD:
		case '(':
		case '~':
		case '-':
			if (!intexpr_M(expr))
				return false;
			if (!intexpr_D(expr))
				return false;
			break;
		default:
			Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					+ "\nL: Expected a ID, Number, (, or -");
			// System.out.printf("L:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public String toString() {
		String result = "";
		result = getElement(result);
		return result;
	}

	private String getElement(String result) {
		switch (isit) {
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
			result = result + variable;
			break;
		case 'n': // Number
			if (uvalue == lvalue) {
				Double tempVal = uvalue;
				result = result + tempVal.toString();
			}
			else {
				Double tempuval = uvalue;
				Double templval = lvalue;
				result = result + "[" + templval.toString() + "," + tempuval.toString() + "]";
			}
			break;
		case 't': // Truth value
			if (uvalue == 1 && lvalue == 1) {
				result = result + "TRUE";
			}
			else if (uvalue == 0 && lvalue == 0) {
				result = result + "FALSE";
			}
			// else {
			// System.out.println("WARNING: Unknown assignment to a boolean
			// variable");
			// result = result + "UNKNOWN";
			// }
			break;
		case 'w': // bitWise
		case 'a': // Arithmetic
		case 'r': // Relational
		case 'l': // Logical
			if (op.equals("!")) {
				result = result + "~";
				if (r1 != null) {
					result = r1.getElement(result);
				}
			}
			else {
				if (r1 != null) {
					result = r1.getElement(result);
				}
				if (op.equals("&&")) {
					result = result + "&";
				}
				else if (op.equals("||")) {
					result = result + "|";
				}
				else if (op.equals("==")) {
					result = result + "=";
				}
				else {
					result = result + op;
				}
			}
		}
		if (r2 != null) {
			result = r2.getElement(result);
		}
		return result;
	}

	private void setVarValues(char willbe, double lNV, double uNV, String var) {
		op = "";
		r1 = null;
		r2 = null;
		isit = willbe;
		if ((isit == 'b') || (isit == 't'))
			logical = true;
		else
			logical = false;
		uvalue = uNV;
		lvalue = lNV;
		variable = var;
		real = 0;
	}

	private void setNodeValues(ExprTree nr1, ExprTree nr2, String nop, char willbe) {
		if (nr1 != null) {
			r1 = new ExprTree(nr1);
		}
		if (nr2 != null) {
			r2 = new ExprTree(nr2);
		}
		op = nop;
		isit = willbe;
		if ((isit == 'r') || (isit == 'l')) {
			logical = true;
			uvalue = 1;
			lvalue = 0;
		}
		else {
			logical = false;
			uvalue = INFIN;
			lvalue = -INFIN;
		}
		variable = null;
	}

	public double evaluateExp(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (variables.get(variable).toLowerCase().equals("true")) {
				return 1.0;
			}
			else {
				return 0.0;
			}
		case 'i': // Integer
		case 'c': // Continuous
			return Double.parseDouble(variables.get(variable));
		case 'n': // Number
			if (uvalue == lvalue) {
				return uvalue;
			}
			else {
				return ((uvalue - lvalue) * new java.util.Random().nextDouble()) + lvalue;
			}
		case 't': // Truth value
			if (uvalue == 1 && lvalue == 1) {
				return 1.0;
			}
			else {
				return 0.0;
			}
		case 'w': // bitWise
		case 'a': // Arithmetic
		case 'r': // Relational
		case 'l': // Logical
			if (op.equals("!")) {
				if (r1 != null) {
					if (r1.evaluateExp(variables) != 0.0) {
						return 0.0;
					}
					else {
						return 1.0;
					}
				}
				else if (r2 != null) {
					if (r2.evaluateExp(variables) != 0.0) {
						return 0.0;
					}
					else {
						return 1.0;
					}
				}
				else {
					return 0.0;
				}
			}
			else {
				double left = 0.0;
				double right = 0.0;
				if (r1 != null) {
					left = r1.evaluateExp(variables);
				}
				if (r2 != null) {
					right = r2.evaluateExp(variables);
				}
				if (op.equals("&&")) {
					if (left == 1.0 && right == 1.0) {
						return 1.0;
					}
					else {
						return 0.0;
					}
				}
				else if (op.equals("||")) {
					if (left == 1.0 || right == 1.0) {
						return 1.0;
					}
					else {
						return 0.0;
					}
				}
				else if (op.equals("==")) {
					if (left == right) {
						return 1.0;
					}
					else {
						return 0.0;
					}
				}
				else if (op.equals("+")) {
					return left + right;
				}
				else if (op.equals("*")) {
					return left * right;
				}
				else if (op.equals("/")) {
					return left / right;
				}
				else if (op.equals("%")) {
					return left % right;
				}
				else if (op.equals("^")) {
					return Math.pow(left, right);
				}
				else if (op.equals("<")) {
					if (left < right) {
						return 1.0;
					}
					else {
						return 0.0;
					}
				}
				else if (op.equals(">")) {
					if (left > right) {
						return 1.0;
					}
					else {
						return 0.0;
					}
				}
				else {
					return 0.0;
				}
			}
		}
		return 0.0;
	}

	private static final int WORD = 1;

	private static final int IMPLIES = 7;

	private static final int END_OF_STRING = 2;

	// private static final int MAXTOKEN = 2000;

	// private static final int VAR = 262144;

	private static final int INFIN = 2147483647;

}