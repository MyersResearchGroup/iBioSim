package lhpn2sbml.parser;

import java.util.ArrayList;
import java.util.HashMap;

//import java.util.Properties;

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

	private LhpnFile lhpn;

	public ExprTree(LhpnFile lhpn) {
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
	
	public ExprTree(OldLHPNFile lhpn) {
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
	
	public ExprTree(Abstraction abstraction) {
		// this.abstraction = abstraction;
		this.lhpn = abstraction;
		String[] bools = abstraction.getBooleanVars();
		String[] conts = abstraction.getContVars();
		String[] ints = abstraction.getIntVars();
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
	
	public ExprTree(Transition transition) {
		//this.lhpn = lhpn;
		//String[] bools = lhpn.getBooleanVars();
		//String[] conts = lhpn.getContVars();
		//String[] ints = lhpn.getIntVars();
		//signals = new ArrayList<String>();
		//for (int j = 0; j < bools.length; j++) {
		//	signals.add(bools[j]);
		//}
		//for (int j = 0; j < conts.length; j++) {
		//	signals.add(conts[j]);
		//}
		//for (int j = 0; j < ints.length; j++) {
		//	signals.add(ints[j]);
		//}
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
		// + " result = ");
		double temp;
		// ExprTree newresult = new ExprTree();

		switch (token) {
		case WORD:
			if (tokvalue.toLowerCase().equals("and")) {
				token = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.print("ERROR: Expected a (\n");
					return false;
				}
				token = intexpr_gettok(expr);
				if (!intexpr_R(expr)) {
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)){
					// System.out.println(newresult.toString());
					(this).isit = 'n';
					// System.out.println(this.lvalue);
					// System.out.println(newresult.lvalue);
					(this).lvalue = ((int) (this).lvalue) & ((int) newresult.lvalue);
					// System.out.println("After " + newresult.lvalue);
					(this).uvalue = (this).lvalue;
				}
				else {
					// (result) = new ExprTree((result), newresult, "&", 'w');
					setNodeValues((this), newresult, "&", 'w');
				}
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.toLowerCase().equals("or")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)) {
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
			else if (tokvalue.toLowerCase().equals("xor")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)) {
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
			else if (tokvalue.toLowerCase().equals("bit")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((newresult.isit == 'n') || (newresult.isit == 't'))
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)) {
					(this).isit = 'n';
					(this).lvalue = ((int) (this).lvalue >> (int) newresult.lvalue) & 1;
					(this).uvalue = (this).lvalue;
				}
				else {
					// (result) = new ExprTree((result), newresult, "^", 'w');
					setNodeValues((this), newresult, "[]", 'w');
				}
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.toLowerCase().equals("not")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((this).isit == 'n') || ((this).isit == 't')
					    && ((this).lvalue == (this).uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)) {
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
			else if (tokvalue.toLowerCase().equals("int")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((this).isit == 'n') || ((this).isit == 't')) {
					// DO NOTHING
				}
				else {
					// (result) = new ExprTree((result), null, "~", 'w');
					setNodeValues((this), null, "int", 'l');
				}
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("uniform")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "uniform", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("normal")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "normal", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("gamma")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "gamma", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("lognormal")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "lognormal", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("binomial")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a ,\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				if (!newresult.intexpr_R(expr))
					return false;
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "binomial", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("exponential")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "exponential", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("chisq")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "chisq", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("laplace")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "laplace", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("cauchy")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "cauchy", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("rayleigh")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "rayleigh", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("poisson")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "poisson", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("bernoulli")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "bernoulli", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("rate")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
					//		+ "\nU: Expected a (");
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "rate", 'a');
				(token) = intexpr_gettok(expr);
			}
			else if ((tokvalue.equals("true")) || tokvalue.equals("TRUE")) {
				// (result) = new ExprTree('t', 1, 1, null);
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("t") && !signals.contains(tokvalue)) {
				// (result) = new ExprTree('t', 1, 1, null);
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("T") && !signals.contains(tokvalue)) {
				// (result) = new ExprTree('t', 1, 1, null);
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if ((tokvalue.equals("false")) || tokvalue.equals("FALSE")) {
				// (result) = new ExprTree('t', 0, 0, null);
				setVarValues('t', 0, 0, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("f") && !signals.contains(tokvalue)) {
				// (result) = new ExprTree('t', 0, 0, null);
				setVarValues('t', 0, 0, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("F") && !signals.contains(tokvalue)) {
				// (result) = new ExprTree('t', 0, 0, null);
				setVarValues('t', 0, 0, null);
				(token) = intexpr_gettok(expr);
			}
			else if ((tokvalue.toLowerCase().equals("unknown"))) {
				// (result) = new ExprTree('t', 0, 0, null);
				setVarValues('t', 0, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.toLowerCase().equals("inf")) {
				setVarValues('n', INFIN, INFIN, null);
				token = intexpr_gettok(expr);
			}
			else {
				// do boolean lookup here!!!
				if (signals.contains(tokvalue)) {
					if (lhpn.isInput(tokvalue) || lhpn.isOutput(tokvalue)) {
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
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr + "\nU1:("
					//		+ tokvalue + "): Expected a ID, Number, or a (\n");
					System.out.printf("U1:ERROR(%s): Expected a ID, Number, or a (\n", tokvalue);
					return false;
				}
				else if ((int) (tokvalue.charAt(0)) > ('9') || ((int) (tokvalue.charAt(0)) < '0')) {
					//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr + "\nU1:("
					//		+ tokvalue + "): Expected a ID, Number, or a (\n");
					System.out.printf("U1:ERROR(%s): Expected a ID, Number, or a (\n", tokvalue);
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
				//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
				//		+ "\nU: Expected a (");
				System.out.printf("ERROR: Expected a )\n");
				return false;
			}
			(token) = intexpr_gettok(expr);
			break;
		default:
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nU2: Expected a ID, Number, or a (");
			System.out.printf("U2:ERROR: Expected a ID, Number, or a (\n");
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
			if ((((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)) {
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nT: Expected a ID, Number, (, or -\n");
			System.out.printf("T:ERROR: Expected a ID, Number, (, or -\n");
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = Math.pow(lvalue,newresult.lvalue);
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nC: Expected a * or /\n");
			System.out.printf("ERROR: Expected a * or /\n");
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nB: Expected a + or -\n");
			System.out.printf("ERROR: Expected a + or -\n");
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nS: Expected a ID, Number, (, or -\n");
			System.out.printf("S:ERROR: Expected a ID, Number, (, or -\n");
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nR: Expected a ID, Number, (, or -\n");
			System.out.printf("R:ERROR: Expected a ID, Number, (, or -\n");
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)) {
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
					// if ((this).isit == 'c') {
					// comp = variable;
					// comp += ">=";
					// int paren = 0;
					// ineq = new String();
					// for (i = spos; i < position; i++) {
					// if (expr.charAt(i) == '(')
					// paren++;
					// if (expr.charAt(i) == ')')
					// paren--;
					// ineq = ineq + expr.charAt(i);
					// }
					// // ineq[i - spos + paren] = 0;
					// comp += ineq;
					// // printf("looking for %s\n",comp.c_str());
					// // for (i = 0; i < nsignals; i++) {
					// if (signals.contains(comp)) {
					// // printf("successful lookup of boolean variable
					// // '%s'\n",signals[i]->name);
					// (this).isit = 'b';
					// this.variable = comp;
					// (this).lvalue = 0;
					// (this).uvalue = 1;
					// return true;
					// }
					// // }
					// else {
					// signals.add(comp);
					// this.isit = 'b';
					// this.variable = comp;
					// this.lvalue = 0;
					// this.uvalue = 1;
					// return true;
					// }
					// }
					// else {
					// (result) = new ExprTree((result), newresult, ">=",
					// 'r');
					setNodeValues((this), newresult, ">=", 'r');
					// }
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
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)) {
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
					// if ((this).isit == 'c') {
					// comp = variable;
					// comp += ">";
					// int paren = 0;
					// ineq = new String();
					// for (i = spos; i < position; i++) {
					// if (expr.charAt(i) == '(')
					// paren++;
					// if (expr.charAt(i) == ')')
					// paren--;
					// ineq = ineq + expr.charAt(i);
					// }
					// // ineq[i - spos + paren] = 0;
					// comp += ineq;
					// // printf("looking for %s\n",comp.c_str());
					// // for (i = 0; i < nsignals; i++) {
					// if (signals.contains(comp)) {
					// // printf("successful lookup of boolean variable
					// // '%s'\n",signals[i]->name);
					// (this).isit = 'b';
					// (this).variable = comp;
					// (this).lvalue = 0;
					// (this).uvalue = 1;
					// return true;
					// }
					// else {
					// signals.add(comp);
					// this.isit = 'b';
					// this.variable = comp;
					// this.lvalue = 0;
					// this.uvalue = 1;
					// return true;
					// }
					// // }
					// }
					// else {
					// (result) = new ExprTree((result), newresult, ">",
					// 'r');
					setNodeValues((this), newresult, ">", 'r');
					// }
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
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)) {
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
					// if ((this).isit == 'c') {
					// comp = variable;
					// comp += "<=";
					// int paren = 0;
					// ineq = new String();
					// for (i = spos; i < position; i++) {
					// / if (expr.charAt(i) == '(')
					// paren++;
					// if (expr.charAt(i) == ')')
					// paren--;
					// ineq = ineq + expr.charAt(i);
					// }
					// ineq[i - spos + paren] = 0;
					// comp += ineq;
					// printf("looking for %s\n",comp.c_str());
					// for (i = 0; i < nsignals; i++) {
					// if (signals.contains(comp)) {
					// printf("successful lookup of boolean variable
					// '%s'\n",signals[i]->name);
					// (this).isit = 'b';
					// (this).variable = comp;
					// (this).lvalue = 0;
					// (this).uvalue = 1;
					// return true;
					// }
					// else {
					// signals.add(comp);
					// this.isit = 'b';
					// this.variable = comp;
					// this.lvalue = 0;
					// this.uvalue = 1;
					// return true;
					// }
					// }
					// }
					// else {
					// (result) = new ExprTree((result), newresult, "<=",
					// 'r');
					setNodeValues((this), newresult, "<=", 'r');
					// }
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
						&& (((this).isit == 'n') || ((this).isit == 't'))
					    && ((this).lvalue == (this).uvalue)
					    && (newresult.lvalue == newresult.uvalue)
					    && ((this).lvalue != INFIN)
					    && ((this).lvalue != -INFIN)
					    && (newresult.lvalue != INFIN)
					    && (newresult.lvalue != -INFIN)) {
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
					// if ((this).isit == 'c') {
					// comp = variable;
					// comp += "<";
					// int paren = 0;
					// ineq = new String();
					// for (i = spos; i < position; i++) {
					// if (expr.charAt(i) == '(')
					// paren++;
					// if (expr.charAt(i) == ')')
					// paren--;
					// ineq = ineq + expr.charAt(i);
					// }
					// // ineq[i - spos + paren] = 0;
					// comp += ineq;
					// System.out.printf("looking for %s\n", comp);
					// // for (i = 0; i < nsignals; i++) {
					// if (signals.contains(comp)) {
					// // System.out.printf("successful lookup of
					// // boolean variable '%s'\n",
					// // signals[i]);
					// (this).isit = 'b';
					// (this).variable = comp;
					// (this).lvalue = 0;
					// (this).uvalue = 1;
					// return true;
					// }
					// else {
					// signals.add(comp);
					// this.isit = 'b';
					// this.variable = comp;
					// this.lvalue = 0;
					// this.uvalue = 1;
					// return true;
					// }
					// // }
					// }
					// else {
					// (result) = new ExprTree((result), newresult, "<",
					// 'r');
					setNodeValues((this), newresult, "<", 'r');
					// }
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
				//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
				//		+ "\nP: Expected a ]");
				System.out.printf("ERROR: Expected a ]\n");
				return false;
			}
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nP: Expected a [, =, <, or >");
			System.out.printf("ERROR: Expected a [, =, <, or >\n");
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nO: Expected a ID, Number, or a (");
			System.out.printf("O:ERROR: Expected a ID, Number, or a (\n");
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
			if ((((this).isit == 'n') || ((this).isit == 't'))
			    && ((this).lvalue == (this).uvalue)
			    && ((this).lvalue != INFIN)
			    && ((this).lvalue != -INFIN)) {
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nN: Expected a ID, Number, (, or -");
			System.out.printf("N:ERROR: Expected a ID, Number, (, or -\n");
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr + "\nE:(" + token
			//		+ "): Expected an &");
			System.out.printf("ERROR(%c): Expected an &\n", (token));
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
					&& (((this).isit == 'n') || ((this).isit == 't'))
				    && ((this).lvalue == (this).uvalue)
				    && (newresult.lvalue == newresult.uvalue)
				    && ((this).lvalue != INFIN)
				    && ((this).lvalue != -INFIN)
				    && (newresult.lvalue != INFIN)
				    && (newresult.lvalue != -INFIN)) {
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nD: Expected an | or ->");
			System.out.printf("ERROR: Expected an | or ->\n");
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nM: Expected a ID, Number, (, or -");
			System.out.printf("M: ERROR: Expected a ID, Number, (, or -\n");
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
			//Utility.createErrorMessage("ERROR", "Invalid expression: " + expr
			//		+ "\nL: Expected a ID, Number, (, or -");
			System.out.printf("L:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public String toString() {
		String result = "";
		result = getElement("LHPN");
		return result;
	}
	
	public String toString(String type) {
		String result = "";
		result = getElement(type);
		return result;
	}

	public String toString(String type, String lhpnSbml) {
		String result = "";
		result = getElement(lhpnSbml);
		if (type.equals("continuous") || type.equals("integer")) {
			if (isit == 't') {
				if (uvalue == 0) {
					result = "0";
				}
				else {
					result = "1";
				}
			}
		}
		else {
			if (isit == 'n') {
				if (uvalue == 0) {
					result = "FALSE";
				}
				else {
					result = "TRUE";
				}
			}
		}
		return result;
	}

	public boolean implies(ExprTree expr) {
		if (isEqual(expr)) {
			return true;
		}
		if (expr.isit == 'l' && expr.op.equals("||")) {
			if (implies(expr.r1) || implies(expr.r2)) {
				return true;
			}
		}
		else if (expr.isit == 'l' && expr.op.equals("&&")) {
			if (implies(expr.r1) && implies(expr.r2)) {
				return true;
			}
		}
		switch (isit) {
		case 't': // Truth value
			if (uvalue == 1 && lvalue == 1) {
				return false;
			}
			else if (uvalue == 0 && lvalue == 0) {
				return true;
			}
			else {
				return false;
			}
		case 'r': // Relational
			if (op.contains(">")) {
				if (expr.isit == 'r' && expr.op.contains(">")) {
					if (r2.lvalue > expr.r2.uvalue) {
						return true;
					}
					else if (r2.lvalue == expr.r2.uvalue && op.length() >= expr.op.length()) {
						return true;
					}
				}
			}
			else if (op.contains("<")) {
				if (expr.isit == 'r' && expr.op.contains("<")) {
					if (r2.lvalue < expr.r2.uvalue) {
						return true;
					}
					else if (r2.lvalue == expr.r2.uvalue && op.length() >= expr.op.length()) {
						return true;
					}
				}
			}
			return false;
		case 'l': // Logical
			if (op.equals("&&")) {
				if (expr.isit == 'b') {
					if (r1.implies(expr) || r2.implies(expr)) {
						return true;
					}
				}
			}
			else if (op.equals("||")) {
				if (expr.isit == 'b') {
					if (r1.implies(expr) && r2.implies(expr)) {
						return true;
					}
				}
			}
			return false;
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
		case 'n': // Number
		case 'w': // bitWise
		case 'a': // Arithmetic
		default:
			return false;
		}
	}

	public boolean containsVar(String var) {
		switch (isit) {
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
			if (variable.equals(var))
				return true;
			return false;
		case 'r': // Relational
		case 'l': // Logical
		case 'a': // Arithmetic
		case 'w': // bitWise
			if (r1 != null) {
				if (r1.containsVar(var)) {
					return true;
				}
			}
			if (r2 != null) {
				if (r2.containsVar(var)) {
					return true;
				}
			}
			return false;
		case 'n': // Number
		case 't': // Truth value
		default:
			return false;
		}
	}

	public ArrayList<String> getVars() {
		ArrayList<String> vars = new ArrayList<String>();
		switch (isit) {
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
			if (!vars.contains(variable))
				vars.add(variable);
			break;
		case 'r': // Relational
		case 'l': // Logical
		case 'a': // Arithmetic
		case 'w': // bitWise
			if (r1 != null)
				vars.addAll(r1.getVars());
			if (r2 != null)
				vars.addAll(r2.getVars());
			break;
		case 'n': // Number
		case 't': // Truth value
		default:
			break;
		}
		return vars;
	}

	public void scaleVals(Double scaleFactor) { // SB
		switch (isit) {
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
			// if (!vars.contains(variable))
			// vars.add(variable);
			break;
		case 'r': // Relational
		case 'l': // Logical
		case 'a': // Arithmetic
		case 'w': // bitWise
			if (r1 != null)
				r1.scaleVals(scaleFactor);
			if (r2 != null)
				r2.scaleVals(scaleFactor);
			break;
		case 'n': // Number
			variable = String.valueOf((int) (Double.parseDouble(variable) * scaleFactor));
			break;
		case 't': // Truth value
		default:
			break;
		}
		// return ;
	}

	public boolean containsCont() {
		switch (isit) {
		case 'b': // Boolean
		case 't': // Truth value
			return false;
		case 'i': // Integer
		case 'c': // Continuous
		case 'r': // Relational
		case 'a': // Arithmetic
		case 'n': // Number
			return true;
		case 'l': // Logical
		case 'w': // bitWise
			boolean r1cont = false,
			r2cont = false;
			if (r1 != null)
				r1cont = r1.containsCont();
			if (r2 != null)
				r2cont = r2.containsCont();
			return (r1cont || r2cont);
		}
		return false;
	}

	public void replace(String var, String type, ExprTree e) {
		if (this == e) {
			return;
		}
		boolean simplify = false;
		switch (isit) {
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
			if (variable.equals(var)) {
				if (e.isit == 'a' || e.isit == 'r' || e.isit == 'l' || e.isit == 'w') {
					setNodeValues(e.r1, e.r2, e.op, e.isit);
				}
				else {
					setVarValues(e.isit, e.lvalue, e.uvalue, e.variable);
				}
			}
			return;
		case 'w': // bitWise
		case 'l': // Logical
		case 'r': // Relational
		case 'a': // Arithmetic
			if (r1 != null)
				r1.replace(var, type, e);
			if (r2 != null)
				r2.replace(var, type, e);
			// simplify if operands are static
			if (op.equals("&&")) {
				if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
						simplify = true;
					}
					else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						}
						else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
						}
					}
				}
				else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
						simplify = true;
					}
					else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						}
						else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
						}
					}
				}
			}
			else if (op.equals("||")) {
				if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue == 1) {
						setVarValues('t', 1.0, 1.0, null);
						simplify = true;
					}
					else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						}
						else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
						}
					}
				}
				else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 1) {
						setVarValues('t', 1.0, 1.0, null);
						simplify = true;
					}
					else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						}
						else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
						}
					}
				}
				else if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue != 0 || r2.lvalue != 0) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("->")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue != 0 || r2.lvalue == 0) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("!")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 't';
					if (r1.lvalue == 1) {
						this.lvalue = 0;
					}
					else {
						this.lvalue = 1;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("==")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue == r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals(">=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue >= r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals(">")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue > r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("<=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue <= r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("<")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue < r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("&")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					// System.out.println(newresult.toString());
					(this).isit = 'n';
					// System.out.println(this.lvalue);
					// System.out.println(newresult.lvalue);
					(this).lvalue = ((int) (r1).lvalue) & ((int) r2.lvalue);
					// System.out.println("After " + newresult.lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("|")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue | (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("^")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue ^ (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("~")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = ~(int) (r1).lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("[]")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					(this).lvalue = (((int) (r1).lvalue) >> ((int) r2.lvalue)) & 1;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("U-")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = -((r1).lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("*")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue * r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("/")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue / r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("%")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue % r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			else if (op.equals("+")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue + r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue == 0 && r1.uvalue == 0) {
						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
					}
				}
				else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 0 && r2.uvalue == 0) {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					}
				}
			}
			else if (op.equals("-")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue - r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			}
			break;
		case 't': // Truth value
			if (lvalue != 0 && uvalue != 0) {
				lvalue = 1;
				uvalue = 1;
			}
			else if (lvalue != 0 || uvalue != 0) {
				lvalue = 0;
				uvalue = 1;
			}
			return;
		case 'n': // Number
			break;
		}
		if (simplify) {
			if (type.equals("integer") || type.equals("continuous")) {
				isit = 'n';
			}
			else {
				isit = 't';
				if (lvalue != 0 && uvalue != 0) {
					lvalue = 1;
					uvalue = 1;
				}
				else if (lvalue != 0 || uvalue != 0) {
					lvalue = 0;
					uvalue = 1;
				}
			}
		}
	}

	public void replaceVar(String var1, String var2) {
		switch (isit) {
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
			if (variable.equals(var1)) {
				variable = var2;
			}
			return;
		case 'w': // bitWise
		case 'l': // Logical
		case 'r': // Relational
		case 'a': // Arithmetic
			if (r1 != null)
				r1.replaceVar(var1, var2);
			if (r2 != null)
				r2.replaceVar(var1, var2);
		case 't': // Truth value
		case 'n': // Number
			break;
		}
	}

//	public void copy(ExprTree e, String type) {
//		if (e.op != null) {
//			op = e.op;
//		}
//		isit = e.isit;
//		lvalue = e.lvalue;
//		uvalue = e.uvalue;
//		if (e.variable != null) {
//			variable = e.variable;
//		}
//		real = e.real;
//		logical = e.logical;
//		r1 = new ExprTree(lhpn);
//		if (e.r1 != null) {
//			r1.copy(e.r1, type);
//		}
//		r2 = new ExprTree(lhpn);
//		if (e.r2 != null) {
//			r2.copy(e.r2, type);
//		}
//		// simplify if operands are static
//		boolean simplify = false;
//		if (isit == 'a' || isit == 'r' || isit == 'l' || isit == 'w') {
//			if (op.equals("&&")) {
//				if ((r1.isit == 'n') || (r1.isit == 't')) {
//					if (r1.lvalue == 0) {
//						setVarValues('t', 0.0, 0.0, null);
//						simplify = true;
//					}
//					else {
//						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
//							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
//						}
//						else {
//							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
//						}
//					}
//				}
//				else if (((r2).isit == 'n') || ((r2).isit == 't')) {
//					if (r2.lvalue == 0) {
//						setVarValues('t', 0.0, 0.0, null);
//						simplify = true;
//					}
//					else {
//						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
//							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
//						}
//						else {
//							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
//						}
//					}
//				}
//				else if (r1.equals(r2)) {
//					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
//						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
//					}
//					else {
//						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
//					}
//				}
//				else {
//					ExprTree notE = new ExprTree(this);
//					notE.setNodeValues((this), null, "!", 'l');
//					if (r1.equals(notE) || notE.equals(r1)) {
//						setVarValues('t', 0.0, 0.0, null);
//					}
//				}
//			}
//			else if (op.equals("||")) {
//				if ((r1.isit == 'n') || (r1.isit == 't')) {
//					if (r1.lvalue != 0) {
//						setVarValues('t', 1.0, 1.0, null);
//						simplify = true;
//					}
//					else {
//						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
//							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
//						}
//						else {
//							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
//						}
//					}
//				}
//				else if (((r2).isit == 'n') || ((r2).isit == 't')) {
//					if (r2.lvalue != 0) {
//						setVarValues('t', 1.0, 1.0, null);
//						simplify = true;
//					}
//					else {
//						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
//							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
//						}
//						else {
//							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
//						}
//					}
//				}
//				else if (r1.equals(r2)) {
//					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
//						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
//					}
//					else {
//						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
//					}
//				}
//				else {
//					ExprTree notE = new ExprTree(this);
//					notE.setNodeValues((this), null, "!", 'l');
//					if (r1.equals(notE) || notE.equals(r1)) {
//						setVarValues('t', 1.0, 1.0, null);
//					}
//				}
//				// if (((r1.isit == 'n') || (r1.isit == 't'))
//				// && (((r2).isit == 'n') || ((r2).isit == 't'))) {
//				// (this).isit = 't';
//				// if (r1.lvalue != 0 || r2.lvalue != 0) {
//				// this.lvalue = 1;
//				// }
//				// else {
//				// this.lvalue = 0;
//				// }
//				// (this).uvalue = (this).lvalue;
//				// simplify = true;
//				// }
//			}
//			else if (op.equals("->")) {
//				if (r1.isit == 'n' || r1.isit == 't') {
//					if (r1.lvalue != 0) {
//						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
//							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
//						}
//						else {
//							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
//						}
//					}
//					else if (r1.uvalue == 0) {
//						setVarValues('t', 1.0, 1.0, null);
//					}
//				}
//				else if (r2.isit == 't' || r2.isit == 'n') {
//					if (r2.lvalue != 0) {
//						setVarValues('t', 1.0, 1.0, null);
//					}
//					else if (r2.uvalue == 0) {
//						ExprTree notE = new ExprTree(r2);
//						notE.setNodeValues((this), null, "!", 'l');
//						setNodeValues(notE.r1, notE.r2, notE.op, notE.isit);
//					}
//				}
//				// else if (((r1.isit == 'n') || (r1.isit == 't'))
//				// && (((r2).isit == 'n') || ((r2).isit == 't'))) {
//				// (this).isit = 't';
//				// if (r1.lvalue != 0 || r2.lvalue == 0) {
//				// this.lvalue = 1;
//				// }
//				// else {
//				// this.lvalue = 0;
//				// }
//				// (this).uvalue = (this).lvalue;
//				// simplify = true;
//				// }
//			}
//			else if (op.equals("!")) {
//				if (((r1).isit == 'n') || ((r1).isit == 't')) {
//					(this).isit = 't';
//					if (r1.lvalue == 1) {
//						this.lvalue = 0;
//					}
//					else {
//						this.lvalue = 1;
//					}
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("==")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 't';
//					if (r1.lvalue == r2.lvalue) {
//						this.lvalue = 1;
//					}
//					else {
//						this.lvalue = 0;
//					}
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals(">=")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 't';
//					if ((r1).lvalue >= r2.lvalue) {
//						this.lvalue = 1;
//					}
//					else {
//						this.lvalue = 0;
//					}
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals(">")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 't';
//					if ((r1).lvalue > r2.lvalue) {
//						this.lvalue = 1;
//					}
//					else {
//						this.lvalue = 0;
//					}
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("<=")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 't';
//					if ((r1).lvalue <= r2.lvalue) {
//						this.lvalue = 1;
//					}
//					else {
//						this.lvalue = 0;
//					}
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("<")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 't';
//					if ((r1).lvalue < r2.lvalue) {
//						this.lvalue = 1;
//					}
//					else {
//						this.lvalue = 0;
//					}
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("&")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					// System.out.println(newresult.toString());
//					(this).isit = 'n';
//					// System.out.println(this.lvalue);
//					// System.out.println(newresult.lvalue);
//					(this).lvalue = ((int) (r1).lvalue) & ((int) r2.lvalue);
//					// System.out.println("After " + newresult.lvalue);
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("|")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 'n';
//					(this).lvalue = (int) (r1).lvalue | (int) r2.lvalue;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (isit == 'w' && op.equals("^")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 'n';
//					(this).lvalue = (int) (r1).lvalue ^ (int) r2.lvalue;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("~")) {
//				if (((r1).isit == 'n') || ((r1).isit == 't')) {
//					(this).isit = 'n';
//					(this).lvalue = ~(int) (r1).lvalue;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("[]")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 't';
//					(this).lvalue = (((int) (r1).lvalue) >> ((int) r2.lvalue)) & 1;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("U-")) {
//				if (((r1).isit == 'n') || ((r1).isit == 't')) {
//					(this).isit = 'n';
//					(this).lvalue = -((r1).lvalue);
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//			}
//			else if (op.equals("*")) {
//				if (r1.isit == 'n' || r1.isit == 't') {
//					if (r1.lvalue == 0 && r1.uvalue == 0) {
//						setVarValues('t', 0.0, 0.0, null);
//					}
//					else if (r1.lvalue == 1 && r1.uvalue == 1) {
//						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
//							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
//						}
//						else {
//							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
//						}
//					}
//				}
//				else if (r2.isit == 'n' || r2.isit == 't') {
//					if (r2.lvalue == 0 && r2.uvalue == 0) {
//						setVarValues('t', 0.0, 0.0, null);
//					}
//					else if (r2.lvalue == 1 && r2.uvalue == 1) {
//						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
//							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
//						}
//						else {
//							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
//						}
//					}
//				}
//				// if (((r1.isit == 'n') || (r1.isit == 't'))
//				// && (((r2).isit == 'n') || ((r2).isit == 't'))) {
//				// (this).isit = 'n';
//				// (this).lvalue = (r1).lvalue * r2.lvalue;
//				// (this).uvalue = (this).lvalue;
//				// simplify = true;
//				// }
//			}
//			else if (op.equals("/")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 'n';
//					(this).lvalue = (r1).lvalue / r2.lvalue;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//				else if ((r1.isit == 'n' || r1.isit == 't') && r1.uvalue == 0 && r1.lvalue == 0) {
//					setVarValues('n', 0.0, 0.0, null);
//				}
//				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 1 && r2.uvalue == 1) {
//					if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
//						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
//					}
//					else {
//						setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
//					}
//				}
//			}
//			else if (op.equals("%")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 'n';
//					(this).lvalue = (r1).lvalue % r2.lvalue;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//				else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 1.0 && r1.uvalue == 1.0) {
//					setVarValues('n', 1.0, 1.0, null);
//				}
//				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 1.0 && r2.uvalue == 1.0) {
//					if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
//						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
//					}
//					else {
//						setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
//					}
//				}
//			}
//			else if (op.equals("+")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 'n';
//					(this).lvalue = (r1).lvalue + r2.lvalue;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//				else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 0 && r1.uvalue == 0) {
//					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
//						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
//					}
//					else {
//						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
//					}
//				}
//				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 0 && r2.uvalue == 0) {
//					if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
//						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
//					}
//					else {
//						setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
//					}
//				}
//			}
//			else if (op.equals("-")) {
//				if (((r1.isit == 'n') || (r1.isit == 't'))
//						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
//					(this).isit = 'n';
//					(this).lvalue = (r1).lvalue - r2.lvalue;
//					(this).uvalue = (this).lvalue;
//					simplify = true;
//				}
//				else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 0 && r1.uvalue == 0) {
//					setNodeValues(r2, null, "U-", 'a');
//				}
//				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 0 && r2.uvalue == 0) {
//					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
//						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
//					}
//					else {
//						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
//					}
//				}
//			}
//		}
//		if (simplify) {
//			if (type.equals("integer") || type.equals("continuous")) {
//				isit = 'n';
//			}
//			else {
//				isit = 't';
//				if (lvalue != 0 && uvalue != 0) {
//					lvalue = 1;
//					uvalue = 1;
//				}
//				else if (lvalue != 0 || uvalue != 0) {
//					lvalue = 0;
//					uvalue = 1;
//				}
//			}
//		}
//	}

	public char getChange(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (variables.containsKey(variable)) {
				if (variables.get(variable).toString().toLowerCase().equals("false"))
					return 'F';
				if (variables.get(variable).toString().toLowerCase().equals("true"))
					return 'T';
				else  {
					return 'X';
				}
			}
			else {
				return 'U';
			}
		case 't': // Truth value
			if (uvalue == 0)
				return 'T';
			else if (lvalue == 1)
				return 'F';
			return 'U';
		case 'l': // Logical
			if (op.equals("||")) {
				if (r1.getChange(variables) == 'T' || r2.getChange(variables) == 'T') {
					return 'T';
				}
				else if (r1.getChange(variables) == 'X' || r2.getChange(variables) == 'X') {
					return 'X';
				}
				else if (r1.getChange(variables) == 't') {
					if (r2.getChange(variables) == 'f') {
						return 'X';
					}
					else {
						return 't';
					}
				}
				else if (r2.getChange(variables) == 't') {
					if (r1.getChange(variables) == 'f') {
						return 'X';
					}
					else {
						return 't';
					}
				}
				else if (r1.getChange(variables) == 'f' || r2.getChange(variables) == 'f') {
					return 'f';
				}
				else if (r1.getChange(variables) == 'F') {
					if (r2.getChange(variables) == 'F') {
						return 'F';
					}
					else {
						return 'f';
					}
				}
				else if (r2.getChange(variables) == 'F') {
					return 'f';
				}
				return 'U';
			}
			else if (op.equals("&&")) {
				if (r1.getChange(variables) == 'F' || r2.getChange(variables) == 'F') {
					return 'F';
				}
				else if (r1.getChange(variables) == 'X' || r2.getChange(variables) == 'X') {
					return 'X';
				}
				else if (r1.getChange(variables) == 'f') {
					if (r2.getChange(variables) == 't') {
						return 'X';
					}
					else {
						return 'f';
					}
				}
				else if (r2.getChange(variables) == 'f') {
					if (r1.getChange(variables) == 't') {
						return 'X';
					}
					else {
						return 'f';
					}
				}
				else if (r1.getChange(variables) == 't' || r2.getChange(variables) == 't') {
					return 't';
				}
				else if (r1.getChange(variables) == 'T') {
					if (r2.getChange(variables) == 'T') {
						return 'T';
					}
					else {
						return 't';
					}
				}
				else if (r2.getChange(variables) == 'T') {
					return 't';
				}
				return 'U';
			}
			else if (op.equals("!")) {
				if (r1.getChange(variables) == 'T') {
					return 'F';
				}
				else if (r1.getChange(variables) == 'F') {
					return 'T';
				}
				else if (r1.getChange(variables) == 't') {
					return 'f';
				}
				else if (r1.getChange(variables) == 'f') {
					return 't';
				}
				return r1.getChange(variables);
			}
			else if (op.equals("->")) {
				if (r1.getChange(variables) == 'T' || r2.getChange(variables) == 'F') {
					return 'T';
				}
				else if (r1.getChange(variables) == 'X' || r2.getChange(variables) == 'X') {
					return 'X';
				}
				else if (r1.getChange(variables) == 't') {
					if (r2.getChange(variables) == 't') {
						return 'X';
					}
					else {
						return 't';
					}
				}
				else if (r2.getChange(variables) == 'f') {
					if (r1.getChange(variables) == 'f') {
						return 'X';
					}
					else {
						return 't';
					}
				}
				else if (r1.getChange(variables) == 'f') {
					return 'f';
				}
				else if (r2.getChange(variables) == 't') {
					return 'f';
				}
				else if (r1.getChange(variables) == 'F') {
					if (r2.getChange(variables) == 'T') {
						return 'F';
					}
					else {
						return 'f';
					}
				}
				else if (r2.getChange(variables) == 'T') {
					return 'f';
				}
				return 'U';
			}
			else if (op.equals("^")) {
				if (r1.getChange(variables) == 'T') {
					if (r2.getChange(variables) == 'T') {
						return 'F';
					}
					else if (r2.getChange(variables) == 'F') {
						return 'T';
					}
					else if (r2.getChange(variables) == 't') {
						return 'f';
					}
					else if (r2.getChange(variables) == 'f') {
						return 't';
					}
					return 'X';
				}
				else if (r1.getChange(variables) == 'F') {
					if (r2.getChange(variables) == 'U') {
						return 'X';
					}
					else {
						return r2.getChange(variables);
					}
				}
				else if (r1.getChange(variables) == 't') {
					if (r2.getChange(variables) == 't') {
						return 'f';
					}
					else if (r2.getChange(variables) == 'f') {
						return 't';
					}
					return 'X';
				}
				else if (r1.getChange(variables) == 'f') {
					if (r2.getChange(variables) == 'T') {
						return 't';
					}
					else if (r2.getChange(variables) == 'F') {
						return 'f';
					}
					return 'X';
				}
				else if (r1.getChange(variables) == 'U' && r2.getChange(variables) == 'U') {
					return 'U';
				}
				return 'X';
			}
		case 'a': // Arithmetic
		case 'w': // bitWise
			boolean flag = false;
			for (String var : getVars()) {
				if (variables.containsKey(var)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return 'U';
			}
			if (op.equals("&")) {
				if (evaluateExp(variables) == 0.0) {
					return 'F';
				}
				else if (new Double(evaluateExp(variables)).equals(Double.NaN)) {
					return 'X';
				}
				return 'T';
			}
		case 'r': // Relational
			if (r1.isit == 'i') {
				if (!variables.containsKey(r1.variable)) {
					return 'U';
				}
				if (op.equals("==")) {
					if (r1.evaluateExp(variables) == r2.evaluateExp(variables)) {
						return 'T';
					}
					else if (new Double(r2.evaluateExp(variables)).equals(Double.NaN)) {
						return 'X';
					}
					return 'F';
				}
				else if (op.equals(">=")) {
					if (r1.evaluateExp(variables) >= r2.evaluateExp(variables)) {
						return 'T';
					}
					else if (new Double(r2.evaluateExp(variables)).equals(Double.NaN) || new Double(r1.evaluateExp(variables)).equals(Double.NaN)) {
						return 'X';
					}
					return 'F';
				}
				else if (op.equals("<=")) {
					if (r1.evaluateExp(variables) <= r2.evaluateExp(variables)) {
						return 'T';
					}
					else if (new Double(r2.evaluateExp(variables)).equals(Double.NaN)) {
						return 'X';
					}
					return 'F';
				}
				else if (op.equals(">")) {
					if (r1.evaluateExp(variables) > r2.evaluateExp(variables)) {
						return 'T';
					}
					else if (new Double(r2.evaluateExp(variables)).equals(Double.NaN)) {
						return 'X';
					}
					return 'F';
				}
				else if (op.equals("<")) {
					if (r1.evaluateExp(variables) < r2.evaluateExp(variables)) {
						return 'T';
					}
					else if (new Double(r2.evaluateExp(variables)).equals(Double.NaN)) {
						return 'X';
					}
					return 'F';
				}
				return 'X';
			}
			else {
				return 'X';
			}
		case 'i': // Integer
			if (variables.containsKey(variable)) {
				if (Integer.parseInt(variables.get(variable)) == 0.0) {
					return 'F';
				}
				else {
					return 'T';
				}
			}
			else {
				return 'U';
			}
		case 'c': // Continuous
			return 'X';
		case 'n': // Number
			if (uvalue == 0.0 && lvalue == 0.0) {
				return 'F';
			}
			else {
				return 'T';
			}
		}
		return 'X';
	}

	public boolean becomesFalse(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (variables.containsKey(variable))
				if (variables.get(variable).toString().toLowerCase().equals("false"))
					return true;
			return false;
		case 't': // Truth value
			if (lvalue == 0)
				return true;
			return false;
		case 'l': // Logical
			if (op.equals("||")) {
				if (r1.becomesFalse(variables) && r2.becomesFalse(variables)) {
					return true;
				}
				return false;
			}
			else if (op.equals("&&")) {
				if ((r1.becomesFalse(variables) && !r2.becomesTrue(variables))
						|| (!r1.becomesTrue(variables) && r2.becomesFalse(variables)))
					return true;
				return false;
			}
			else if (op.equals("==")) {
				if (!(r1.isEqual(r2) || r1.evaluateExp(variables) == r2.evaluateExp(variables)))
					return true;
				return false;
			}
			else if (op.equals("!")) {
				if (r1.becomesTrue(variables))
					return true;
				return false;
			}
			else if (op.equals("->")) {
				if (r1.becomesFalse(variables) || r2.becomesTrue(variables)) {
					return true;
				}
				return false;
			}
		case 'w': // bitWise
			if (op.equals("&")) {
				if (!(evaluateExp(variables) == 0.0)) {
					return true;
				}
				return false;
			}
			else if (op.equals("|")) {
				if (!(evaluateExp(variables) == 0.0)) {
					return true;
				}
				return false;
			}
			else if (op.equals("^")) {
				if (!(evaluateExp(variables) == 0.0)) {
					return true;
				}
				return false;
			}
			else if (op.equals("~")) {
				if (!(evaluateExp(variables) == 0.0)) {
					return true;
				}
				return false;
			}
			else if (op.equals("[]")) {
				if (!(evaluateExp(variables) == 0.0)) {
					return true;
				}
				return false;
			}
		case 'r': // Relational
			if (r1.isit == 'i') {
				if (!variables.containsKey(r1.variable)) {
					return false;
				}
				if (op.equals("==")) {
					if (r1.evaluateExp(variables) == r2.evaluateExp(variables)) {
						return false;
					}
					return true;
				}
				else if (op.equals(">=")) {
					if (r1.evaluateExp(variables) >= r2.evaluateExp(variables)) {
						return false;
					}
					return true;
				}
				else if (op.equals("<=")) {
					if (r1.evaluateExp(variables) <= r2.evaluateExp(variables)) {
						return false;
					}
					return true;
				}
				else if (op.equals(">")) {
					if (r1.evaluateExp(variables) > r2.evaluateExp(variables)) {
						return false;
					}
					return true;
				}
				else if (op.equals("<")) {
					if (r1.evaluateExp(variables) < r2.evaluateExp(variables)) {
						return false;
					}
					return true;
				}
				return true;
			}
			else {
				return true;
			}
		case 'i': // Integer
			if (variables.containsKey(variable)) {
				if (Integer.parseInt(variables.get(variable)) == 0.0) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				return false;
			}
		case 'c': // Continuous
			return true;
		case 'a': // Arithmetic
			boolean contains = false;
			for (String s : getVars()) {
				if (variables.containsKey(s)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
			if (!(evaluateExp(variables) == 0.0)) {
				return false;
			}
			else {
				return true;
			}
		case 'n': // Number
			if (uvalue == 0.0 && lvalue == 0.0) {
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}

	public boolean becomesTrue(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (variables.containsKey(variable)) {
				if (variables.get(variable).toString().matches("[\\d[\\.]]+")) {
					if (Double.parseDouble(variables.get(variable).toString()) != 0) {
						return true;
					}
				}
				if (variables.get(variable).toString().toLowerCase().equals("true"))
					return true;
			}
			return false;
		case 'i': // Integer
			if (variables.containsKey(variable)) {
				if (!variables.get(variable).equals("0.0")) {
					return true;
				}
			}
			return false;
		case 'c': // Continuous
			return true;
		case 'n': // Number
		case 't': // Truth value
			if (uvalue != 0)
				return true;
			return false;
		case 'l': // Logical
			if (op.equals("||")) {
				if (r1.becomesTrue(variables) || r2.becomesTrue(variables))
					return true;
				return false;
			}
			else if (op.equals("&&")) {
				if ((r1.becomesTrue(variables) && !r2.becomesFalse(variables))
						|| (!r1.becomesFalse(variables) && r2.becomesTrue(variables)))
					return true;
				return false;
			}
			else if (op.equals("==")) {
				if (r1.isEqual(r2, variables)
						|| r1.evaluateExp(variables) == r2.evaluateExp(variables))
					return true;
				return false;
			}
			else if (op.equals("!")) {
				if (r1.becomesFalse(variables))
					return true;
				return false;
			}
			else if (op.equals("->")) {
				if (r1.becomesTrue(variables) || r2.becomesFalse(variables)) {
					return true;
				}
				else {
					return false;
				}
			}
		case 'w': // bitWise
			if (op.equals("&")) {
				if (evaluateExp(variables) == 0.0) {
					return false;
				}
				return true;
			}
			else if (op.equals("|")) {
				if (evaluateExp(variables) == 0.0) {
					return false;
				}
				return true;
			}
			else if (op.equals("^")) {
				if (evaluateExp(variables) == 0.0) {
					return false;
				}
				return true;
			}
			else if (op.equals("~")) {
				if (evaluateExp(variables) == 0.0) {
					return false;
				}
				return true;
			}
			else if (op.equals("[]")) {
				if (evaluateExp(variables) == 0.0) {
					return false;
				}
				return true;
			}
		case 'r': // Relational
			if (r1.isit == 'i') {
				if (!variables.containsKey(r1.variable)) {
					return false;
				}
				if (op.equals("==")) {
					if (!(r1.evaluateExp(variables) == r2.evaluateExp(variables))) {
						return false;
					}
					return true;
				}
				else if (op.equals(">=")) {
					if (!(r1.evaluateExp(variables) >= r2.evaluateExp(variables))) {
						return false;
					}
					return true;
				}
				else if (op.equals("<=")) {
					if (!(r1.evaluateExp(variables) <= r2.evaluateExp(variables))) {
						return false;
					}
					return true;
				}
				else if (op.equals(">")) {
					if (!(r1.evaluateExp(variables) > r2.evaluateExp(variables))) {
						return false;
					}
					return true;
				}
				else if (op.equals("<")) {
					if (!(r1.evaluateExp(variables) < r2.evaluateExp(variables))) {
						return false;
					}
					return true;
				}
				return true;
			}
			else {
				return true;
			}
		case 'a': // Arithmetic
			boolean contains = false;
			for (String s : getVars()) {
				if (variables.containsKey(s)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				return false;
			}
			if (!(evaluateExp(variables) != 0.0)) {
				return false;
			}
			else {
				return true;
			}
		}
		return true;
	}

	public String getElement(String type) {
		boolean sbmlFlag;
		sbmlFlag = type.equals("SBML");
		Boolean verilog = type.equalsIgnoreCase("Verilog");
		String result = "";
		switch (isit) {
		case 'b': // Boolean
		case 'i': // Integer
		case 'c': // Continuous
			if (!sbmlFlag){
			// result = result + variable;
			result = variable;
			break;
			}
			else {
				if (isit == 'b') {
					result = "eq(" + variable + ",1)";
				}
				else {
					result = variable;
				}
				break;
				
			}
		case 'n': // Number
			// TODO: create a random initial value
			// Temporary: initialize all inf, -inf, [-inf, inf] to 0
			Double tempuval = uvalue;
			Double templval = lvalue;
			if ((uvalue == lvalue) || tempuval.toString().equals("")) {
				if (lvalue==INFIN) {
					result = "inf";
				} else if (lvalue==-INFIN) {
					result = "-inf";
				} else {
					if (tempuval%1 == 0) {
						int tempval = (int) (tempuval/1);
						result = new Integer(tempval).toString();
					}
					else {
						result = tempuval.toString();
					}
				}
				// result = tempuval.toString();
			}
			else {
				String lval;
				if (lvalue==INFIN) {
					lval = "inf";
				} else if (lvalue==-INFIN){
					lval = "-inf";
				} else {
					if (tempuval%1 == 0) {
						int tempval = (int) (templval/1);
						lval = new Integer(tempval).toString();
					}
					else {
						lval = templval.toString();
					}
				}
				String uval;
				if (uvalue==INFIN) {
					uval = "inf";	
				} else if (uvalue==-INFIN){
					uval = "-inf";
				} else {
					if (tempuval%1 == 0) {
						int tempval = (int) (tempuval/1);
						uval = new Integer(tempval).toString();
					}
					else {
						uval = tempuval.toString();
					}
				}
				if (verilog){
					result = lval + " + (($unsigned($random))%(" + uval + "-" + lval + "+1))";
				} else {
					result = "uniform(" + lval + "," + uval + ")";
				}
				// result = "[" + templval.toString() + "," +
				// tempuval.toString() + "]";

			}
			break;
		case 't': // Truth value
			if (uvalue == 0 && lvalue == 0) {
				if (verilog)
					result = "0";
				else 
					result = "FALSE";
				// result = "TRUE";
			}
			else if (uvalue == 1 && lvalue == 1) {
				if (verilog)
					result = "1";
				else
					result = "TRUE";
				// result = "FALSE";
			}
			else {
				if (sbmlFlag | verilog){
					result = "0";
				}
				result = "UNKNOWN";
				
			}
			// else {
			// System.out.println("WARNING: Unknown assignment to a boolean
			// variable");
			// result = result + "UNKNOWN";
			// }
			break;

		case 'w': // bitWise
			if (op.equals("&")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag)	 {
						result = "BITAND(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
					else if (verilog){
						result = r1.getElement(type) + "&" + r2.getElement(type);
					}
					else {
						result = "and(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
				}
			}
			else if (op.equals("|")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag) {
						result = "BITOR(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
					else if (verilog){
						result = r1.getElement(type) + "|" + r2.getElement(type);
					}
					else {
						result = "or(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
				}
			}
			
			else if (op.equals("[]")) {
				if (r1 != null && r2 != null) {
					result = "BIT(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
				}
			}
			else if (op.equals("!")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag){
						result = "BITNOT(" + r1.getElement(type) + ")";
					} 
					else if (verilog){
						result = "~" + r1.getElement(type);
					}
					else {
						result = "not(" + r1.getElement(type) +  ")";
					}
					
				}
			}
			else if (op.equals("^")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag){
						result = "XOR(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
					else if (verilog){
						result = r1.getElement(type) + "^" + r2.getElement(type);
					}
					else {
						result = "exor(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
					
				}
			}
			break;
		case 'a': // Arithmetic
		case 'r': // Relational
		case 'l': // Logical
			if (op.equals("!")) {
				if (r1 != null) {
					if (r1.isit == 'b' || r1.isit == 'i' || r1.isit == 'c' || r1.isit == 'n'
							|| r1.isit == 't') {
						if(sbmlFlag) {
							result = "not(" + r1.getElement(type) + ")";
						}
						else if (verilog){
							result = "!" + r1.getElement(type);
						}
						else {
							result = "~" + r1.getElement(type);
						}
					}
					else {
						if(sbmlFlag){
							result = "not(" + r1.getElement(type) + ")";
						}
						else if (verilog){
							result = "!" + "(" + r1.getElement(type) + ")";
						}
						else {
							result = "~" + "(" + r1.getElement(type) + ")";
						}
					}
				}
				break;
			}
			else {
				if (op.equals("&&")) {
					if (r1.isit == 'r' || (r1.isit == 'l' && r1.op.equals("||"))) {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "and(" + r1.getElement(type) + ",";
							}
							else if (verilog){
								result = "(" + r1.getElement(type) + ")&&";
							}
							else {
								result = "(" + r1.getElement(type) + ")";
							}
						}

					}
					else {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "and(" + r1.getElement(type) + ",";
							}
							else if (verilog){
								result = r1.getElement(type) + "&&";
							}
							else {
								result = r1.getElement(type);
							}
							
						}
					}
					
					if (!sbmlFlag && !verilog) {
						result = result + "&";
					}
					
					if (r2.isit == 'r' || (r2.isit == 'l' && r2.op.equals("||"))) {
						if (r2 != null) {
							if (sbmlFlag){
								result = result + r2.getElement(type) + ")";
							}
							else {
								result = result + "(" + r2.getElement(type) + ")";
							}
							
						}
					}
					else {
						if (r2 != null) {
							if (sbmlFlag) {
								result = result + r2.getElement(type) + ")";
							}
							else {
								result = result + r2.getElement(type);
							}
							
						}
					}
				}
				else if (op.equals("||")) {
					if (r1.isit == 'r') {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "or(" + r1.getElement(type) + ",";
							}
							else if (verilog){
								result = "(" + r1.getElement(type) + ")||";
							}
							else {
								result = "(" + r1.getElement(type) + ")";
							}
							
						}
					}
					else {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "or(" + r1.getElement(type) + ",";
							}
							else if (verilog){
								result = r1.getElement(type) + "||";
							}
							else {
								result = r1.getElement(type);
							}
						}
					}
					
					if (!sbmlFlag && !verilog){
						result = result + "|";
					}
					
					if (r2.isit == 'r') {
						if (r2 != null) {
							if (sbmlFlag) {
								result = result + r2.getElement(type) + ")";
							}
							else {
								result = result + "(" + r2.getElement(type) + ")";
							}
							
						}
					}
					else {
						if (r2 != null) {
							if (sbmlFlag) {
								result = result + r2.getElement(type) + ")";
							}
							else {
								result = result + r2.getElement(type);
							}
							
						}
					}
				}
				else if (op.equals("uniform")) {
					if (r1 != null && r2 != null) {
						if (verilog){
							result = r1.getElement(type) + " + (($unsigned($random))%(" + r2.getElement(type) + "-" + r1.getElement(type) + "+1))";
						} else {
							result = "uniform(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
						}
					}
				}	//TODO: Add verilog functions for other distributions
				else if (op.equals("normal")) {
					if (r1 != null && r2 != null) {
						result = "normal(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
				}
				else if (op.equals("gamma")) {
					if (r1 != null && r2 != null) {
						result = "gamma(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
				}
				else if (op.equals("lognormal")) {
					if (r1 != null && r2 != null) {
						result = "lognormal(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
				}
				else if (op.equals("binomial")) {
					if (r1 != null && r2 != null) {
						result = "binomial(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
					}
				}
				else if (op.equals("exponential")) {
					if (r1 != null) {
						result = "exponential(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("chisq")) {
					if (r1 != null) {
						result = "chisq(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("laplace")) {
					if (r1 != null) {
						result = "laplace(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("cauchy")) {
					if (r1 != null) {
						result = "cauchy(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("rayleigh")) {
					if (r1 != null) {
						result = "rayleigh(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("poisson")) {
					if (r1 != null) {
						result = "poisson(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("bernoulli")) {
					if (r1 != null) {
						result = "bernoulli(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("rate")) {
					if (r1 != null) {
						result = "rate(" + r1.getElement(type) + ")";
					}
				}
				else if (op.equals("INT")) {
					if (r1 != null) {
						if (sbmlFlag) {
							result = "piecewise(1," + r1.getElement(type) + "0 )"; 
						}
						else {
							result = "INT(" + r1.getElement(type) + ")";
						}
						
					}
				}
				else if (op.equals("==")) {
					if (r1 != null) {
						if (sbmlFlag) {
							result = "eq(" + r1.getElement(type) + ",";
						}
						else if (verilog){
							result = r1.getElement(type) + "==";
						}
						else {
							result = r1.getElement(type);
						}
						
					}
					if (!sbmlFlag && !verilog) {
						result = result + "=";
					}
					
					if (r2 != null) {
						if (sbmlFlag) {
							result = result + r2.getElement(type) + ")";
						}
						else {
							result = result + r2.getElement(type);
						}
						
					}
				}
				else if (op.equals("+")) {
					if (r1.isit == 'b'
							|| r1.isit == 'i'
							|| r1.isit == 'c'
							|| r1.isit == 'n'
							|| r1.isit == 't'
							|| (r1.isit == 'a' && (r1.op.equals("+") || r1.op.equals("-")
									|| r1.op.equals("*") || r1.op.equals("/") || r1.op.equals("^")))) {
						if (r1 != null) {
							result = r1.getElement(type);
						}
					}
					else {
						if (r1 != null) {
							result = "(" + r1.getElement(type) + ")";
						}
					}
					result = result + "+";
					if (r2.isit == 'b'
							|| r2.isit == 'i'
							|| r2.isit == 'c'
							|| r2.isit == 'n'
							|| r2.isit == 't'
							|| (r2.isit == 'a' && (r2.op.equals("+") || r2.op.equals("-")
									|| r2.op.equals("*") || r2.op.equals("/") || r2.op.equals("^")))) {
						if (r2 != null) {
							result = result + r2.getElement(type);
						}
					}
					else {
						if (r2 != null) {
							result = result + "(" + r2.getElement(type) + ")";
						}
					}
				}
				else if (op.equals("-")) {
					if (r1.isit == 'b'
							|| r1.isit == 'i'
							|| r1.isit == 'c'
							|| r1.isit == 'n'
							|| r1.isit == 't'
							|| (r1.isit == 'a' && (r1.op.equals("+") || r1.op.equals("-")
									|| r1.op.equals("*") || r1.op.equals("/") || r1.op.equals("^")))) {
						if (r1 != null) {
							result = r1.getElement(type);
						}
					}
					else {
						if (r1 != null) {
							result = "(" + r1.getElement(type) + ")";
						}
					}
					result = result + "-";
					if (r2.isit == 'b'
							|| r2.isit == 'i'
							|| r2.isit == 'c'
							|| r2.isit == 'n'
							|| r2.isit == 't'
							|| (r2.isit == 'a' && (r2.op.equals("-") || r2.op.equals("*")
									|| r2.op.equals("/") || r2.op.equals("^")))) {
						if (r2 != null) {
							result = result + r2.getElement(type);
						}
					}
					else {
						if (r2 != null) {
							result = result + "(" + r2.getElement(type) + ")";
						}
					}
				}
				else if (op.equals("*")) {
					if (r1.isit == 'b'
							|| r1.isit == 'i'
							|| r1.isit == 'c'
							|| r1.isit == 'n'
							|| r1.isit == 't'
							|| (r1.isit == 'a' && (r1.op.equals("*") || r1.op.equals("/") || r1.op
									.equals("^")))) {
						if (r1 != null) {
							result = r1.getElement(type);
						}
					}
					else {
						if (r1 != null) {
							result = "(" + r1.getElement(type) + ")";
						}
					}
					result = result + "*";
					if (r2.isit == 'b'
							|| r2.isit == 'i'
							|| r2.isit == 'c'
							|| r2.isit == 'n'
							|| r2.isit == 't'
							|| (r2.isit == 'a' && (r2.op.equals("*") || r2.op.equals("/") || r2.op
									.equals("^")))) {
						if (r2 != null) {
							result = result + r2.getElement(type);
						}
					}
					else {
						if (r2 != null) {
							result = result + "(" + r2.getElement(type) + ")";
						}
					}
				}
				else if (op.equals("/")) {
					if (r1.isit == 'b'
							|| r1.isit == 'i'
							|| r1.isit == 'c'
							|| r1.isit == 'n'
							|| r1.isit == 't'
							|| (r1.isit == 'a' && (r1.op.equals("*") || r1.op.equals("/") || r1.op
									.equals("^")))) {
						if (r1 != null) {
							result = r1.getElement(type);
						}
					}
					else {
						if (r1 != null) {
							result = "(" + r1.getElement(type) + ")";
						}
					}
					result = result + "/";
					if (r2.isit == 'b' || r2.isit == 'i' || r2.isit == 'c' || r2.isit == 'n'
							|| r2.isit == 't'
							|| (r2.isit == 'a' && (r2.op.equals("/") || r2.op.equals("^")))) {
						if (r2 != null) {
							result = result + r2.getElement(type);
						}
					}
					else {
						if (r2 != null) {
							result = result + "(" + r2.getElement(type) + ")";
						}
					}
					if (sbmlFlag) {
						result = "floor(" + result + ")";
					}
				}
				// relational ops: geq, leq, gt, lt
				// mod 
				else {  
					if (!sbmlFlag) {
						if (r1 != null) {
							if (r1.isit == 'b' || r1.isit == 'i' || r1.isit == 'c' || r1.isit == 'n'
									|| r1.isit == 't') {
								result = r1.getElement(type);
							}
							else {
								result = "(" + r1.getElement(type) + ")";
							}
						}
						result = result + op;
						if (r2 != null) {
							if (r2.isit == 'b' || r2.isit == 'i' || r2.isit == 'c' || r2.isit == 'n'
									|| r2.isit == 't') {
								result = result + r2.getElement(type);
							}
							else {
								result = result + "(" + r2.getElement(type) + ")";
							}
						}
					}
					
					if (sbmlFlag) {
						if (op.equals("<=")) {
							if (r1 != null && r2 != null) {
								result = "leq(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
							}
						}
						if (op.equals(">=")) {
							if (r1 != null && r2 != null) {								
								result = "geq(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
							}
						}
						if (op.equals(">")) {
							if (r1 != null && r2 != null) {								
								result = "gt(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
							}
						}
						if (op.equals("<")) {
							if (r1 != null && r2 != null) {								
								result = "lt(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
							}
						}
						if (op.equals("%")) {
							if (r1 != null && r2 != null) {								
								result = "mod(" + r1.getElement(type) + "," + r2.getElement(type) + ")";
							}
						}

					}
				}
			}
		}
		return result;
	}

	public boolean isEqual(ExprTree expr) {
		if (isit == expr.isit) {
			boolean same = false;
			switch (isit) {
			case 'b': // Boolean
			case 'i': // Integer
			case 'c': // Continuous
				if (variable.equals(expr.variable)) {
					same = true;
				}
				break;
			case 'n': // Number
			case 't': // Truth value
				if (uvalue == expr.uvalue && lvalue == expr.lvalue) {
					same = true;
				}
				break;
			case 'w': // bitWise
			case 'a': // Arithmetic
			case 'r': // Relational
			case 'l': // Logical
				if (op.equals(expr.op)) {
					same = true;
				}
			}
			if (same) {
				boolean r1Same = false, r2Same = false;
				if (r1 == null) {
					if (expr.r1 == null) {
						r1Same = true;
					}
				}
				else if (r1.isEqual(expr.r1)) {
					r1Same = true;
				}
				if (r2 == null) {
					if (expr.r2 == null) {
						r2Same = true;
					}
				}
				else if (r2.isEqual(expr.r2)) {
					r2Same = true;
				}
				if (r1Same && r2Same) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isEqual(ExprTree expr, HashMap<String, String> variables) {
		if (isit == expr.isit) {
			boolean same = false;
			switch (isit) {
			case 'b': // Boolean
			case 'i': // Integer
			case 'c': // Continuous
				if (variables.containsKey(variable)) {
					if (variables.containsKey(expr.variable)) {
						if (variables.get(variable).equals(variables.get(expr.variable)))
							same = true;
					}
				}
				else if (variable.equals(expr.variable)) {
					same = true;
				}
				break;
			case 'n': // Number
			case 't': // Truth value
				if (uvalue == expr.uvalue && lvalue == expr.lvalue) {
					same = true;
				}
				else if (variables.containsKey(expr.variable)) {
					if (uvalue == lvalue) {
						if (uvalue == 1.0
								&& variables.get(expr.variable).toLowerCase().equals("true"))
							same = true;
						else if (uvalue == 0.0
								&& variables.get(expr.variable).toLowerCase().equals("false"))
							same = true;
					}
				}
				break;
			case 'w': // bitWise
			case 'a': // Arithmetic
			case 'r': // Relational
			case 'l': // Logical
				if (op.equals(expr.op)) {
					same = true;
				}
			}
			if (same) {
				boolean r1Same = false, r2Same = false;
				if (r1 == null) {
					if (expr.r1 == null) {
						r1Same = true;
					}
				}
				else if (r1.isEqual(expr.r1)) {
					r1Same = true;
				}
				if (r2 == null) {
					if (expr.r2 == null) {
						r2Same = true;
					}
				}
				else if (r2.isEqual(expr.r2)) {
					r2Same = true;
				}
				if (r1Same && r2Same) {
					return true;
				}
			}
		}
		return false;
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

	public void setNodeValues(ExprTree nr1, ExprTree nr2, String nop, char willbe) {
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
		// simplify if operands are static
		if (isit == 'a' || isit == 'r' || isit == 'l' || isit == 'w') {
			if (op.equals("&&")) {
				if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
					}
					else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						}
						else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
						}
					}
				}
				else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
					}
					else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						}
						else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
						}
					}
				}
				else if (r1.equals(r2)) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					}
					else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				}
				else {
					ExprTree notE = new ExprTree(this);
					notE.setNodeValues((this), null, "!", 'l');
					if (r1.equals(notE) || notE.equals(r1)) {
						setVarValues('t', 0.0, 0.0, null);
					}
				}
			}
			else if (op.equals("||")) {
				if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue != 0) {
						setVarValues('t', 1.0, 1.0, null);
					}
					else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						}
						else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
						}
					}
				}
				else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue != 0) {
						setVarValues('t', 1.0, 1.0, null);
					}
					else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						}
						else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
						}
					}
				}
				else if (r1.equals(r2)) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					}
					else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				}
				else {
					ExprTree notE = new ExprTree(this);
					notE.setNodeValues((this), null, "!", 'l');
					if (r1.equals(notE) || notE.equals(r1)) {
						setVarValues('t', 1.0, 1.0, null);
					}
				}
			}
			else if (op.equals("->")) {
				if (r1.isit == 'n' || r1.isit == 't') {
					if (r1.lvalue != 0) {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						}
						else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
						}
					}
					else if (r1.uvalue == 0) {
						setVarValues('t', 1.0, 1.0, null);
					}
				}
				else if (r2.isit == 't' || r2.isit == 'n') {
					if (r2.lvalue != 0) {
						setVarValues('t', 1.0, 1.0, null);
					}
					else if (r2.uvalue == 0) {
						ExprTree notE = new ExprTree(r2);
						notE.setNodeValues((this), null, "!", 'l');
						setNodeValues(notE.r1, notE.r2, notE.op, notE.isit);
					}
				}
			}
			else if (op.equals("!")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 't';
					if (r1.lvalue == 1) {
						this.lvalue = 0;
					}
					else {
						this.lvalue = 1;
					}
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("==")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue == r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals(">=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue >= r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals(">")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue > r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("<=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue <= r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("<")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue < r2.lvalue) {
						this.lvalue = 1;
					}
					else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("&")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					// System.out.println(newresult.toString());
					(this).isit = 'n';
					// System.out.println(this.lvalue);
					// System.out.println(newresult.lvalue);
					(this).lvalue = ((int) (r1).lvalue) & ((int) r2.lvalue);
					// System.out.println("After " + newresult.lvalue);
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("|")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue | (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
			}
			else if (isit == 'w' && op.equals("^")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue ^ (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("~")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = ~(int) (r1).lvalue;
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("[]")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					(this).lvalue = (((int) (r1).lvalue) >> ((int) r2.lvalue)) & 1;
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("U-")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = -((r1).lvalue);
					(this).uvalue = (this).lvalue;
				}
			}
			else if (op.equals("*")) {
				if (r1.isit == 'n' || r1.isit == 't') {
					if (r1.lvalue == 0 && r1.uvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
					}
					else if (r1.lvalue == 1 && r1.uvalue == 1) {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						}
						else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
						}
					}
				}
				else if (r2.isit == 'n' || r2.isit == 't') {
					if (r2.lvalue == 0 && r2.uvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
					}
					else if (r2.lvalue == 1 && r2.uvalue == 1) {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						}
						else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
						}
					}
				}
			}
			else if (op.equals("/")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue / r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else if ((r1.isit == 'n' || r1.isit == 't') && r1.uvalue == 0 && r1.lvalue == 0) {
					setVarValues('n', 0.0, 0.0, null);
				}
				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 1 && r2.uvalue == 1) {
					if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
					}
					else {
						setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
					}
				}
			}
			else if (op.equals("%")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue % r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 1.0 && r1.uvalue == 1.0) {
					setVarValues('n', 1.0, 1.0, null);
				}
				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 1.0 && r2.uvalue == 1.0) {
					if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
					}
					else {
						setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
					}
				}
			}
			else if (op.equals("+")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue + r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 0 && r1.uvalue == 0) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					}
					else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				}
				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 0 && r2.uvalue == 0) {
					if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w' || r2.isit == 'r') {
						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
					}
					else {
						setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
					}
				}
			}
			else if (op.equals("-")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue - r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
				else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 0 && r1.uvalue == 0) {
					setNodeValues(r2, null, "U-", 'a');
				}
				else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 0 && r2.uvalue == 0) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w' || r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					}
					else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				}
			}
		}
	}

	public double evaluateExp(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (!variables.containsKey(variable)
					|| variables.get(variable).toLowerCase().equals("unknown"))
				return Double.NaN;
			if (variables.get(variable).toLowerCase().equals("true")) {
				return 1.0;
			}
			else {
				return 0.0;
			}
		case 'i': // Integer
		case 'c': // Continuous
			try {
				return Double.parseDouble(variables.get(variable));
			}
			catch (Exception e) {
				return Double.NaN;
			}
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
			else if (uvalue == 0 && lvalue == 0) {
				return 0.0;
			}
			else {
				return Double.NaN;
			}
		case 'w': // bitWise
		case 'a': // Arithmetic
		case 'r': // Relational
		case 'l': // Logical
			if (op.equals("!")) {
				if (r1 != null) {
					if (r1.evaluateExp(variables) == 1.0) {
						return 0.0;
					}
					else if (r1.evaluateExp(variables) == 0.0) {
						return 1.0;
					}
					else {
						return Double.NaN;
					}
				}
				else if (r2 != null) {
					if (r2.evaluateExp(variables) == 1.0) {
						return 0.0;
					}
					else if (r2.evaluateExp(variables) == 0.0) {
						return 1.0;
					}
					else {
						return Double.NaN;
					}
				}
				else {
					return Double.NaN;
				}
			}
			else {
				double left;
				double right;
				if (r1 != null) {
					left = r1.evaluateExp(variables);
				}
				else {
					left = Double.NaN;
				}
				if (r2 != null) {
					right = r2.evaluateExp(variables);
				}
				else {
					right = Double.NaN;
				}
				if (op.equals("&&")) {
					if (left == 1.0 && right == 1.0) {
						return 1.0;
					}
					else if (left == 0.0 || right == 0.0) {
						return 0.0;
					}
					else
						return Double.NaN;
				}
				else if (op.equals("||")) {
					if (left == 1.0 || right == 1.0) {
						return 1.0;
					}
					else if (left == 0.0 && right == 0.0) {
						return 0.0;
					}
					else
						return Double.NaN;
				}
				else if (op.equals("==")) {
					if (left == right) {
						return 1.0;
					}
					else if (left != right) {
						return 0.0;
					}
					else {
						return Double.NaN;
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
					else if (left >= right) {
						return 0.0;
					}
					else {
						return Double.NaN;
					}
				}
				else if (op.equals(">")) {
					if (left > right) {
						return 1.0;
					}
					else if (left <= right) {
						return 0.0;
					}
					else {
						return Double.NaN;
					}
				}
				else if (op.equals("<=")) {
					if (left <= right) {
						return 1.0;
					}
					else if (left > right) {
						return 0.0;
					}
					else {
						return Double.NaN;
					}
				}
				else if (op.equals(">=")) {
					if (left >= right) {
						return 1.0;
					}
					else if (left < right) {
						return 0.0;
					}
					else {
						return Double.NaN;
					}
				}
				else {
					return Double.NaN;
				}
			}
		}
		return Double.NaN;
	}

	private static final int WORD = 1;

	private static final int IMPLIES = 7;

	private static final int END_OF_STRING = 2;

	// private static final int MAXTOKEN = 2000;

	// private static final int VAR = 262144;

	private static final int INFIN = 2147483647;

}