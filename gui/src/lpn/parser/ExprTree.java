package lpn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.lang.Math;

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

	private ArrayList<String> booleanSignals, integerSignals, continuousSignals;

	private LhpnFile lhpn;
	
	public String expression;

	public ExprTree() {

	}
	/**
	 * This constructor is used in PlatuGrammar.g to convert LPNs from USF to LhpnFile.
	 * All LPNs from USF use integer variables only. So only integer signals are dealt with here.
	 * @param expression
	 */
	public ExprTree(String expression) {
		this.expression = expression;
		booleanSignals = new ArrayList<String>();
		integerSignals = new ArrayList<String>();
		continuousSignals = new ArrayList<String>();
//		intexpr_gettok(expression);
//		intexpr_L(expression);
	}

	public ExprTree(LhpnFile lhpn) {
		this.lhpn = lhpn;
		String[] bools = lhpn.getBooleanVars();
		String[] conts = lhpn.getContVars();
		String[] ints = lhpn.getIntVars();
		booleanSignals = new ArrayList<String>();
		integerSignals = new ArrayList<String>();
		continuousSignals = new ArrayList<String>();
		for (int j = 0; j < bools.length; j++) {
			booleanSignals.add(bools[j]);
		}
		for (int j = 0; j < conts.length; j++) {
			continuousSignals.add(conts[j]);
		}
		for (int j = 0; j < ints.length; j++) {
			integerSignals.add(ints[j]);
		}
	}

	public ExprTree(Abstraction abstraction) {
		this.lhpn = abstraction;
		String[] bools = abstraction.getBooleanVars();
		String[] conts = abstraction.getContVars();
		String[] ints = abstraction.getIntVars();
		booleanSignals = new ArrayList<String>();
		integerSignals = new ArrayList<String>();
		continuousSignals = new ArrayList<String>();
		for (int j = 0; j < bools.length; j++) {
			booleanSignals.add(bools[j]);
		}
		for (int j = 0; j < conts.length; j++) {
			continuousSignals.add(conts[j]);
		}
		for (int j = 0; j < ints.length; j++) {
			integerSignals.add(ints[j]);
		}
	}

	public ExprTree(Transition transition) {
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

	public ExprTree(ExprTree nr1, ExprTree nr2, String nop, char willbe) {
		op = nop;
		r1 = nr1;
		r2 = nr2;
		isit = willbe;
		if ((isit == 'r') || (isit == 'l')) {
			logical = true;
			uvalue = 1;
			lvalue = 0;
		} else {
			logical = false;
			uvalue = INFIN;
			lvalue = -INFIN;
		}
		variable = null;
	}

	public ExprTree(ExprTree source) {
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
		if (source.newresult != null) {
			newresult = source.newresult;
		}
		if (source.booleanSignals != null) {
			booleanSignals = source.booleanSignals;
		}
		if (source.integerSignals != null) {
			integerSignals = source.integerSignals;
		}
		if (source.continuousSignals != null) {
			continuousSignals = source.continuousSignals;
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
				} else {
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
				} else if ((!readword) && (!readnum) && (!readsci)) {
					return (c);
				} else {
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
				} else if (!readword) {
					readnum = true;
				}
				tokvalue += c;
				break;
			case 'E':
			case 'e':
				if (readsci) {
					return -1;
				} else if (readnum) {
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
		} else if (readword || readnum) {
			return (WORD);
		}
		return -1;
	}

	public boolean intexpr_U(String expr) {
		double temp;

		switch (token) {
		case WORD:
			if (tokvalue.toLowerCase().equals("and")) {
				token = intexpr_gettok(expr);
				if ((token) != '(') {
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
					(this).lvalue = ((int) (this).lvalue)
							& ((int) newresult.lvalue);
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "&", 'w');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("or")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					(this).lvalue = (int) (this).lvalue
							| (int) newresult.lvalue;
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "|", 'w');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("xor")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					(this).lvalue = (int) (this).lvalue
							^ (int) newresult.lvalue;
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "X", 'w');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("min")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					(this).lvalue = Math.min((this).lvalue, newresult.lvalue);
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "m", 'a');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("max")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					(this).lvalue = Math.max((this).lvalue, newresult.lvalue);
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "M", 'a');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("idiv")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					(this).lvalue = Math
							.floor((this).lvalue / newresult.lvalue);
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "i", 'a');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("bit")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					(this).isit = 't';
					(this).lvalue = ((int) (this).lvalue >> (int) newresult.lvalue) & 1;
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "[]", 'l');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("floor")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((this).isit == 'n') || ((this).isit == 't')
						&& ((this).lvalue == (this).uvalue)
						&& ((this).lvalue != INFIN)
						&& ((this).lvalue != -INFIN)) {
					(this).isit = 'n';
					(this).lvalue = Math.floor((this).lvalue);
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), null, "f", 'a');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("ceil")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((this).isit == 'n') || ((this).isit == 't')
						&& ((this).lvalue == (this).uvalue)
						&& ((this).lvalue != INFIN)
						&& ((this).lvalue != -INFIN)) {
					(this).isit = 'n';
					(this).lvalue = Math.ceil((this).lvalue);
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), null, "c", 'a');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("not")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
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
				} else {
					setNodeValues((this), null, "~", 'w');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("int")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_L(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				// simplify if operands are static
				if (((this).isit == 'n') || ((this).isit == 't')) {
					// DO NOTHING
				} else {
					setNodeValues((this), null, "INT", 'l');
				}
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("uniform")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "uniform", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("normal")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "normal", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("gamma")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "gamma", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("lognormal")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "lognormal", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("binomial")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ',') {
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
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), newresult, "binomial", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("exponential")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "exponential", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("chisq")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "chisq", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("laplace")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "laplace", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("cauchy")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "cauchy", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("rayleigh")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "rayleigh", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("poisson")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "poisson", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("bernoulli")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "bernoulli", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("rate")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					System.out.printf("ERROR: Expected a (\n");
					return false;
				}
				(token) = intexpr_gettok(expr);
				if (!intexpr_R(expr))
					return false;
				if ((token) != ')') {
					System.out.printf("ERROR: Expected a )\n");
					return false;
				}
				setNodeValues((this), null, "rate", 'a');
				(token) = intexpr_gettok(expr);
			} else if ((tokvalue.equals("true")) || tokvalue.equals("TRUE")) {
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("t") && !booleanSignals.contains(tokvalue) && !integerSignals.contains(tokvalue)
					&& !continuousSignals.contains(tokvalue)) {
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("T") && !booleanSignals.contains(tokvalue) && !integerSignals.contains(tokvalue)
					&& !continuousSignals.contains(tokvalue)) {
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			}
			else if ((tokvalue.equals("false")) || tokvalue.equals("FALSE")) {
				setVarValues('t', 0, 0, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("f") && !booleanSignals.contains(tokvalue) && !integerSignals.contains(tokvalue)
					&& !continuousSignals.contains(tokvalue)) {
				setVarValues('t', 0, 0, null);
				(token) = intexpr_gettok(expr);
			}
			else if (tokvalue.equals("F") && !booleanSignals.contains(tokvalue) && !integerSignals.contains(tokvalue)
					&& !continuousSignals.contains(tokvalue)) {
				setVarValues('t', 0, 0, null);
				(token) = intexpr_gettok(expr);
			} else if ((tokvalue.toLowerCase().equals("unknown"))) {
				setVarValues('t', 0, 1, null);
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.toLowerCase().equals("inf")) {
				setVarValues('n', INFIN, INFIN, null);
				token = intexpr_gettok(expr);
			} else {
				// do boolean lookup here!!!
				if (booleanSignals.contains(tokvalue)) {
					setVarValues('b', 0, 1, tokvalue);
					(token) = intexpr_gettok(expr);
					return true;
				}
				else if (integerSignals.contains(tokvalue)) {
					setVarValues('i', -INFIN, INFIN, tokvalue);
					(token) = intexpr_gettok(expr);
					return true;
				}
				else if (continuousSignals.contains(tokvalue)) {
					setVarValues('c', -INFIN, INFIN, tokvalue);
					(token) = intexpr_gettok(expr);
					return true;
				}
				if (tokvalue.equals("")) {
					System.out.printf(
							"U1:ERROR(%s): Expected a ID, Number, or a (\n",
							tokvalue);
					return false;
				} else if ((int) (tokvalue.charAt(0)) > ('9')
						|| ((int) (tokvalue.charAt(0)) < '0')) {
					System.out.printf(
							"U1:ERROR(%s): Expected a ID, Number, or a (\n",
							tokvalue);
					return false;
				}
				temp = Double.parseDouble(tokvalue);
				setVarValues('n', temp, temp, null);
				token = intexpr_gettok(expr);
			}
			break;
		case '(':
			(token) = intexpr_gettok(expr);
			if (!intexpr_L(expr))
				return false;
			if ((token) != ')') {
				System.out.printf("ERROR: Expected a )\n");
				return false;
			}
			(token) = intexpr_gettok(expr);
			break;
		default:
			System.out.printf("U2:ERROR: Expected a ID, Number, or a (\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_T(String expr) {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = -((this).lvalue);
				(this).uvalue = (this).lvalue;
			} else {
				setNodeValues((this), null, "U-", 'a');
			}
			break;
		default:
			System.out.printf("T:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_C(String expr) {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue * newresult.lvalue;
				(this).uvalue = (this).lvalue;
			} else {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = Math.pow(lvalue, newresult.lvalue);
				(this).uvalue = (this).lvalue;
			} else {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue / newresult.lvalue;
				(this).uvalue = (this).lvalue;
			} else {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue % newresult.lvalue;
				(this).uvalue = (this).lvalue;
			} else {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue * newresult.lvalue;
				(this).uvalue = (this).lvalue;
			} else {
				setNodeValues((this), newresult, "*", 'a');
			}
			if (!intexpr_C(expr))
				return false;
			break;

		default:
			System.out.printf("ERROR: Expected a * or /\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_B(String expr) {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue + newresult.lvalue;
				(this).uvalue = (this).lvalue;
			} else {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 'n';
				(this).lvalue = (this).lvalue - newresult.lvalue;
				(this).uvalue = (this).lvalue;
			} else {
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
			System.out.printf("ERROR: Expected a + or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_S(String expr) {
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
			System.out.printf("S:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_R(String expr) {
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
			System.out.printf("R:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_P(String expr) {
		newresult = new ExprTree(this);
		int spos, i;
		String ineq = "";
		String comp;
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 't';
				if (this.lvalue == newresult.lvalue) {
					this.lvalue = 1;
				} else {
					this.lvalue = 0;
				}
				(this).uvalue = (this).lvalue;
			} else {
				if ((this).isit == 'c') {
					comp = variable;
					comp += "=";
//					int paren = 0;
//					for (i = spos; i < position; i++) {
//						if (expr.charAt(i) == '(')
//							paren++;
//						if (expr.charAt(i) == ')')
//							paren--;
//						ineq = ineq + expr.charAt(i);
//					}
					comp += ineq;
					if (booleanSignals.contains(comp)) {
						this.isit = 'b';
						this.variable = comp;
						this.lvalue = 0;
						this.uvalue = 1;
						return true;
					} else {
						booleanSignals.add(comp);
						this.isit = 'b';
						this.variable = comp;
						this.lvalue = 0;
						this.uvalue = 1;
						return true;
					}
				} else {
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
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, ">=", 'r');
				}
			} else {
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
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, ">", 'r');
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
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "<=", 'r');
				}
			} else {
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
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				} else {
					setNodeValues((this), newresult, "<", 'r');
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
				System.out.printf("ERROR: Expected a ]\n");
				return false;
			}
			// simplify if operands are static
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))
					&& ((this).lvalue == (this).uvalue)
					&& (newresult.lvalue == newresult.uvalue)
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 't';
				(this).lvalue = (((int) (this).lvalue) >> ((int) newresult.lvalue)) & 1;
				(this).uvalue = (this).lvalue;
			} else {
				setNodeValues((this), newresult, "[]", 'l');
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
			System.out.printf("ERROR: Expected a [, =, <, or >\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_O(String expr) {
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
			System.out.printf("O:ERROR: Expected a ID, Number, or a (\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_N(String expr) {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)) {
				(this).isit = 't';
				if (this.lvalue == 1) {
					this.lvalue = 0;
				} else {
					this.lvalue = 1;
				}
				(this).uvalue = (this).lvalue;
			} else {
				setNodeValues((this), null, "!", 'l');
			}
			break;
		default:
			System.out.printf("N:ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_E(String expr) {
		newresult = new ExprTree(this);
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
			if (((newresult.isit == 'n') || (newresult.isit == 't'))
					&& (((this).isit == 'n') || ((this).isit == 't'))
					&& ((this).lvalue == (this).uvalue)
					&& (newresult.lvalue == newresult.uvalue)
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 't';
				if ((this.lvalue == 0) || (newresult.lvalue == 0)) {
					this.lvalue = 0;
				} else {
					this.lvalue = 1;
				}
				(this).uvalue = (this).lvalue;
			} else {
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
			System.out.printf("ERROR(%c): Expected an &\n", (token));
			return false;
		}
		return true;
	}

	public boolean intexpr_D(String expr) {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 't';
				if (this.lvalue != 0 || newresult.lvalue != 0) {
					this.lvalue = 1;
				} else {
					this.lvalue = 0;
				}
				(this).uvalue = (this).lvalue;
			} else {
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
					&& ((this).lvalue != INFIN) && ((this).lvalue != -INFIN)
					&& (newresult.lvalue != INFIN)
					&& (newresult.lvalue != -INFIN)) {
				(this).isit = 't';
				if (this.lvalue != 0 || newresult.lvalue == 0) {
					this.lvalue = 1;
				} else {
					this.lvalue = 0;
				}
				(this).uvalue = (this).lvalue;
			} else {
				setNodeValues(this, newresult, "->", 'l');
			}
			if (!intexpr_D(expr))
				return false;
			break;
		default:
			System.out.printf("ERROR: Expected an | or ->\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_M(String expr) {
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
			System.out.printf("M: ERROR: Expected a ID, Number, (, or -\n");
			return false;
		}
		return true;
	}

	public boolean intexpr_L(String expr) {
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
				} else {
					result = "1";
				}
			}
		} else {
			if (isit == 'n') {
				if (uvalue == 0) {
					result = "FALSE";
				} else {
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
		} else if (expr.isit == 'l' && expr.op.equals("&&")) {
			if (implies(expr.r1) && implies(expr.r2)) {
				return true;
			}
		}
		switch (isit) {
		case 't': // Truth value
			if (uvalue == 1 && lvalue == 1) {
				return false;
			} else if (uvalue == 0 && lvalue == 0) {
				return true;
			} else {
				return false;
			}
		case 'r': // Relational
			if (op.contains(">")) {
				if (expr.isit == 'r' && expr.op.contains(">")) {
					if (r2.lvalue > expr.r2.uvalue) {
						return true;
					} else if (r2.lvalue == expr.r2.uvalue
							&& op.length() >= expr.op.length()) {
						return true;
					}
				}
			} else if (op.contains("<")) {
				if (expr.isit == 'r' && expr.op.contains("<")) {
					if (r2.lvalue < expr.r2.uvalue) {
						return true;
					} else if (r2.lvalue == expr.r2.uvalue
							&& op.length() >= expr.op.length()) {
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
			} else if (op.equals("||")) {
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
			variable = String
					.valueOf((int) (Double.parseDouble(variable) * scaleFactor));
			break;
		case 't': // Truth value
		default:
			break;
		}
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
				if (e.isit == 'a' || e.isit == 'r' || e.isit == 'l'
						|| e.isit == 'w') {
					setNodeValues(e.r1, e.r2, e.op, e.isit);
				} else {
					setVarValues(e.isit, e.lvalue, e.uvalue, e.variable);
				}
			}
			return;
		case 'w': // bitWise
		case 'l': // Logical
		case 'r': // Relational
		case 'a': // Arithmetic
			if (r1 != null || r2 != null) {
				if (r1 != null)
					r1.replace(var, type, e);
				if (r2 != null)
					r2.replace(var, type, e);
				break;
			}
			// simplify if operands are static
			if (op.equals("&&")) {
				if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
						simplify = true;
					} else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w'
								|| r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						} else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue,
									r2.variable);
						}
					}
				} else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
						simplify = true;
					} else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
								|| r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						} else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue,
									r1.variable);
						}
					}
				}
			} else if (op.equals("||")) {
				if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue == 1) {
						setVarValues('t', 1.0, 1.0, null);
						simplify = true;
					} else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w'
								|| r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						} else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue,
									r2.variable);
						}
					}
				} else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 1) {
						setVarValues('t', 1.0, 1.0, null);
						simplify = true;
					} else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
								|| r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						} else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue,
									r1.variable);
						}
					}
				} else if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue != 0 || r2.lvalue != 0) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("->")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue != 0 || r2.lvalue == 0) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("!")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 't';
					if (r1.lvalue == 1) {
						this.lvalue = 0;
					} else {
						this.lvalue = 1;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("==")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue == r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals(">=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue >= r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals(">")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue > r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("<=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue <= r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("<")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue < r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("&")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = ((int) (r1).lvalue) & ((int) r2.lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("|")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue | (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("X")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue ^ (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("m")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = Math.min((r1).lvalue, r2.lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("M")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = Math.max((r1).lvalue, r2.lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("i")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = Math.floor((r1).lvalue / (r2).lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("f")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = Math.floor((r1).lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("c")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = Math.ceil((r1).lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("~")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = ~(int) (r1).lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("[]")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					(this).lvalue = (((int) (r1).lvalue) >> ((int) r2.lvalue)) & 1;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("U-")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = -((r1).lvalue);
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("*")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue * r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("/")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue / r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("%")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue % r2.lvalue;
					(this).uvalue = (this).lvalue;
					simplify = true;
				}
			} else if (op.equals("+")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue + r2.lvalue;
					(this).uvalue = (this).lvalue;
				} else if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue == 0 && r1.uvalue == 0) {
						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
					}
				} else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 0 && r2.uvalue == 0) {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					}
				}
			} else if (op.equals("-")) {
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
			} else if (lvalue != 0 || uvalue != 0) {
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
			} else {
				isit = 't';
				if (lvalue != 0 && uvalue != 0) {
					lvalue = 1;
					uvalue = 1;
				} else if (lvalue != 0 || uvalue != 0) {
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

	public char getChange(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (variables.containsKey(variable)) {
				if (variables.get(variable).toString().toLowerCase().equals(
						"false"))
					return 'F';
				if (variables.get(variable).toString().toLowerCase().equals(
						"true"))
					return 'T';
				else {
					return 'X';
				}
			} else {
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
				if (r1.getChange(variables) == 'T'
						|| r2.getChange(variables) == 'T') {
					return 'T';
				} else if (r1.getChange(variables) == 'X'
						|| r2.getChange(variables) == 'X') {
					return 'X';
				} else if (r1.getChange(variables) == 't') {
					if (r2.getChange(variables) == 'f') {
						return 'X';
					} else {
						return 't';
					}
				} else if (r2.getChange(variables) == 't') {
					if (r1.getChange(variables) == 'f') {
						return 'X';
					} else {
						return 't';
					}
				} else if (r1.getChange(variables) == 'f'
						|| r2.getChange(variables) == 'f') {
					return 'f';
				} else if (r1.getChange(variables) == 'F') {
					if (r2.getChange(variables) == 'F') {
						return 'F';
					} else {
						return 'f';
					}
				} else if (r2.getChange(variables) == 'F') {
					return 'f';
				}
				return 'U';
			} else if (op.equals("&&")) {
				if (r1.getChange(variables) == 'F'
						|| r2.getChange(variables) == 'F') {
					return 'F';
				} else if (r1.getChange(variables) == 'X'
						|| r2.getChange(variables) == 'X') {
					return 'X';
				} else if (r1.getChange(variables) == 'f') {
					if (r2.getChange(variables) == 't') {
						return 'X';
					} else {
						return 'f';
					}
				} else if (r2.getChange(variables) == 'f') {
					if (r1.getChange(variables) == 't') {
						return 'X';
					} else {
						return 'f';
					}
				} else if (r1.getChange(variables) == 't'
						|| r2.getChange(variables) == 't') {
					return 't';
				} else if (r1.getChange(variables) == 'T') {
					if (r2.getChange(variables) == 'T') {
						return 'T';
					} else {
						return 't';
					}
				} else if (r2.getChange(variables) == 'T') {
					return 't';
				}
				return 'U';
			} else if (op.equals("!")) {
				if (r1.getChange(variables) == 'T') {
					return 'F';
				} else if (r1.getChange(variables) == 'F') {
					return 'T';
				} else if (r1.getChange(variables) == 't') {
					return 'f';
				} else if (r1.getChange(variables) == 'f') {
					return 't';
				}
				return r1.getChange(variables);
			} else if (op.equals("->")) {
				if (r1.getChange(variables) == 'T'
						|| r2.getChange(variables) == 'F') {
					return 'T';
				} else if (r1.getChange(variables) == 'X'
						|| r2.getChange(variables) == 'X') {
					return 'X';
				} else if (r1.getChange(variables) == 't') {
					if (r2.getChange(variables) == 't') {
						return 'X';
					} else {
						return 't';
					}
				} else if (r2.getChange(variables) == 'f') {
					if (r1.getChange(variables) == 'f') {
						return 'X';
					} else {
						return 't';
					}
				} else if (r1.getChange(variables) == 'f') {
					return 'f';
				} else if (r2.getChange(variables) == 't') {
					return 'f';
				} else if (r1.getChange(variables) == 'F') {
					if (r2.getChange(variables) == 'T') {
						return 'F';
					} else {
						return 'f';
					}
				} else if (r2.getChange(variables) == 'T') {
					return 'f';
				}
				return 'U';
			}
		case 'r': // Relational
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
			if (op.equals("==")) {
				if (r1.evaluateExpr(variables) == r2.evaluateExpr(variables)) {
					return 'T';
				} else if (new Double(r1.evaluateExpr(variables))
						.equals(Double.NaN)
						|| new Double(r2.evaluateExpr(variables))
								.equals(Double.NaN)) {
					return 'X';
				}
				return 'F';
			} else if (op.equals(">=")) {
				if (r1.evaluateExpr(variables) >= r2.evaluateExpr(variables)) {
					return 'T';
				} else if (new Double(r2.evaluateExpr(variables))
						.equals(Double.NaN)
						|| new Double(r1.evaluateExpr(variables))
								.equals(Double.NaN)) {
					return 'X';
				}
				return 'F';
			} else if (op.equals("<=")) {
				if (r1.evaluateExpr(variables) <= r2.evaluateExpr(variables)) {
					return 'T';
				} else if (new Double(r1.evaluateExpr(variables))
						.equals(Double.NaN)
						|| new Double(r2.evaluateExpr(variables))
								.equals(Double.NaN)) {
					return 'X';
				}
				return 'F';
			} else if (op.equals(">")) {
				if (r1.evaluateExpr(variables) > r2.evaluateExpr(variables)) {
					return 'T';
				} else if (new Double(r1.evaluateExpr(variables))
						.equals(Double.NaN)
						|| new Double(r2.evaluateExpr(variables))
								.equals(Double.NaN)) {
					return 'X';
				}
				return 'F';
			} else if (op.equals("<")) {
				if (r1.evaluateExpr(variables) < r2.evaluateExpr(variables)) {
					return 'T';
				} else if (new Double(r1.evaluateExpr(variables))
						.equals(Double.NaN)
						|| new Double(r2.evaluateExpr(variables))
								.equals(Double.NaN)) {
					return 'X';
				}
				return 'F';
			}
			return 'X';
		case 'i': // Integer
			if (variables.containsKey(variable)) {
				if (Integer.parseInt(variables.get(variable)) == 0.0) {
					return 'F';
				} else {
					return 'T';
				}
			} else {
				return 'U';
			}
		case 'c': // Continuous
			return 'X';
		case 'n': // Number
			if (uvalue == 0.0 && lvalue == 0.0) {
				return 'F';
			} else {
				return 'T';
			}
		}
		return 'X';
	}

	public boolean becomesFalse(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (variables.containsKey(variable))
				if (variables.get(variable).toString().toLowerCase().equals(
						"false"))
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
			} else if (op.equals("&&")) {
				if ((r1.becomesFalse(variables) && !r2.becomesTrue(variables))
						|| (!r1.becomesTrue(variables) && r2
								.becomesFalse(variables)))
					return true;
				return false;
			} else if (op.equals("==")) {
				if (!(r1.isEqual(r2) || r1.evaluateExpr(variables) == r2
						.evaluateExpr(variables)))
					return true;
				return false;
			} else if (op.equals("!")) {
				if (r1.becomesTrue(variables))
					return true;
				return false;
			} else if (op.equals("->")) {
				if (r1.becomesFalse(variables) || r2.becomesTrue(variables)) {
					return true;
				}
				return false;
			} else if (op.equals("[]")) {
				if (!(evaluateExpr(variables) == 0.0)) {
					return true;
				}
				return false;
			}
		case 'w': // bitWise
			if (op.equals("&")) {
				if (!(evaluateExpr(variables) == 0.0)) {
					return true;
				}
				return false;
			} else if (op.equals("|")) {
				if (!(evaluateExpr(variables) == 0.0)) {
					return true;
				}
				return false;
			} else if (op.equals("X")) {
				if (!(evaluateExpr(variables) == 0.0)) {
					return true;
				}
				return false;
			} else if (op.equals("~")) {
				if (!(evaluateExpr(variables) == 0.0)) {
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
					if (r1.evaluateExpr(variables) == r2
							.evaluateExpr(variables)) {
						return false;
					}
					return true;
				} else if (op.equals(">=")) {
					if (r1.evaluateExpr(variables) >= r2
							.evaluateExpr(variables)) {
						return false;
					}
					return true;
				} else if (op.equals("<=")) {
					if (r1.evaluateExpr(variables) <= r2
							.evaluateExpr(variables)) {
						return false;
					}
					return true;
				} else if (op.equals(">")) {
					if (r1.evaluateExpr(variables) > r2.evaluateExpr(variables)) {
						return false;
					}
					return true;
				} else if (op.equals("<")) {
					if (r1.evaluateExpr(variables) < r2.evaluateExpr(variables)) {
						return false;
					}
					return true;
				}
				return true;
			} else {
				return true;
			}
		case 'i': // Integer
			if (variables.containsKey(variable)) {
				if (Integer.parseInt(variables.get(variable)) == 0.0) {
					return true;
				} else {
					return false;
				}
			} else {
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
			if (!(evaluateExpr(variables) == 0.0)) {
				return false;
			} else {
				return true;
			}
		case 'n': // Number
			if (uvalue == 0.0 && lvalue == 0.0) {
				return true;
			} else {
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
				if (variables.get(variable).toString().toLowerCase().equals(
						"true"))
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
			} else if (op.equals("&&")) {
				if ((r1.becomesTrue(variables) && !r2.becomesFalse(variables))
						|| (!r1.becomesFalse(variables) && r2
								.becomesTrue(variables)))
					return true;
				return false;
			} else if (op.equals("==")) {
				if (r1.isEqual(r2, variables)
						|| r1.evaluateExpr(variables) == r2
								.evaluateExpr(variables))
					return true;
				return false;
			} else if (op.equals("!")) {
				if (r1.becomesFalse(variables))
					return true;
				return false;
			} else if (op.equals("->")) {
				if (r1.becomesTrue(variables) || r2.becomesFalse(variables)) {
					return true;
				} else {
					return false;
				}
			}
		case 'w': // bitWise
			if (op.equals("&")) {
				if (evaluateExpr(variables) == 0.0) {
					return false;
				}
				return true;
			} else if (op.equals("|")) {
				if (evaluateExpr(variables) == 0.0) {
					return false;
				}
				return true;
			} else if (op.equals("X")) {
				if (evaluateExpr(variables) == 0.0) {
					return false;
				}
				return true;
			} else if (op.equals("~")) {
				if (evaluateExpr(variables) == 0.0) {
					return false;
				}
				return true;
			} else if (op.equals("[]")) {
				if (evaluateExpr(variables) == 0.0) {
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
					if (!(r1.evaluateExpr(variables) == r2
							.evaluateExpr(variables))) {
						return false;
					}
					return true;
				} else if (op.equals(">=")) {
					if (!(r1.evaluateExpr(variables) >= r2
							.evaluateExpr(variables))) {
						return false;
					}
					return true;
				} else if (op.equals("<=")) {
					if (!(r1.evaluateExpr(variables) <= r2
							.evaluateExpr(variables))) {
						return false;
					}
					return true;
				} else if (op.equals(">")) {
					if (!(r1.evaluateExpr(variables) > r2
							.evaluateExpr(variables))) {
						return false;
					}
					return true;
				} else if (op.equals("<")) {
					if (!(r1.evaluateExpr(variables) < r2
							.evaluateExpr(variables))) {
						return false;
					}
					return true;
				}
				return true;
			} else {
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
			if (!(evaluateExpr(variables) != 0.0)) {
				return false;
			} else {
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
				if (!sbmlFlag) {
					result = variable;
				} else {
					if (isit == 'b') {
						result = "eq(" + variable + ",1)";
					} else {
						result = variable;
					}
				}
			break;
		case 'n': // Number
			// long term solution: create initial assignment
			// short term solution: initialize all inf, -inf, [-inf, inf] to 0
			// initialize [l,u] to (l+u)/2
			Double tempuval = uvalue;
			Double templval = lvalue;
			if ((uvalue == lvalue) || tempuval.toString().equals("")) {
				if (lvalue == INFIN) {
					result = "inf";
				} else if (lvalue == -INFIN) {
					result = "-inf";
				} else {
					if (tempuval % 1 == 0) {
						int tempval = (int) (tempuval / 1);
						result = new Integer(tempval).toString();
					} else {
						result = tempuval.toString();
					}
				}
			} else {
				String lval;
				if (lvalue == INFIN) {
					lval = "inf";
				} else if (lvalue == -INFIN) {
					lval = "-inf";
				} else {
					if (tempuval % 1 == 0) {
						int tempval = (int) (templval / 1);
						lval = new Integer(tempval).toString();
					} else {
						lval = templval.toString();
					}
				}
				String uval;
				if (uvalue == INFIN) {
					uval = "inf";
				} else if (uvalue == -INFIN) {
					uval = "-inf";
				} else {
					if (tempuval % 1 == 0) {
						int tempval = (int) (tempuval / 1);
						uval = new Integer(tempval).toString();
					} else {
						uval = tempuval.toString();
					}
				}
				if (verilog) {
					result = "uniform(" + lval + "," + uval + ")";
				} else {
					result = "uniform(" + lval + "," + uval + ")";
				}
			}
			break;
		case 't': // Truth value
			if (uvalue == 0 && lvalue == 0) {
				if (verilog)
					result = "0";
				else
					result = "FALSE";
			} else if (uvalue == 1 && lvalue == 1) {
				if (verilog)
					result = "1";
				else
					result = "TRUE";
			} else {
				if (sbmlFlag | verilog) {
					result = "0";
				}
				result = "UNKNOWN";

			}
			break;

		case 'w': // bitWise
			if (op.equals("&")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag) {
						result = "BITAND(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					} else if (verilog) {
						result = r1.getElement(type) + "&"
								+ r2.getElement(type);
					} else {
						result = "and(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}
				}
			} else if (op.equals("|")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag) {
						result = "BITOR(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					} else if (verilog) {
						result = r1.getElement(type) + "|"
								+ r2.getElement(type);
					} else {
						result = "or(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}
				}
			} else if (op.equals("!")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag) {
						result = "BITNOT(" + r1.getElement(type) + ")";
					} else if (verilog) {
						result = "~" + r1.getElement(type);
					} else {
						result = "not(" + r1.getElement(type) + ")";
					}

				}
			} else if (op.equals("X")) {
				if (r1 != null && r2 != null) {
					if (sbmlFlag) {
						result = "XOR(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					} else if (verilog) {
						result = r1.getElement(type) + "^"
								+ r2.getElement(type);
					} else {
						result = "exor(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}

				}
			}
			break;
		case 'a': // Arithmetic
		case 'r': // Relational
		case 'l': // Logical
			if (op.equals("!")) {
				if (r1 != null) {
					if (r1.isit == 'b' || r1.isit == 'i' || r1.isit == 'c'
							|| r1.isit == 'n' || r1.isit == 't') {
						if (sbmlFlag) {
							result = "not(" + r1.getElement(type) + ")";
						} else if (verilog) {
							result = "!" + r1.getElement(type);
						} else {
							result = "~" + r1.getElement(type);
						}
					} else {
						if (sbmlFlag) {
							result = "not(" + r1.getElement(type) + ")";
						} else if (verilog) {
							result = "!" + "(" + r1.getElement(type) + ")";
						} else {
							result = "~" + "(" + r1.getElement(type) + ")";
						}
					}
				}
				break;
			} else {
				if (op.equals("&&")) {
					if (r1.isit == 'r'
							|| (r1.isit == 'l' && r1.op.equals("||"))) {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "and(" + r1.getElement(type) + ",";
							} else if (verilog) {
								result = "(" + r1.getElement(type) + ")&&";
							} else {
								result = "(" + r1.getElement(type) + ")";
							}
						}

					} else {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "and(" + r1.getElement(type) + ",";
							} else if (verilog) {
								result = r1.getElement(type) + "&&";
							} else {
								result = r1.getElement(type);
							}

						}
					}

					if (!sbmlFlag && !verilog) {
						result = result + "&";
					}

					if (r2.isit == 'r'
							|| (r2.isit == 'l' && r2.op.equals("||"))) {
						if (r2 != null) {
							if (sbmlFlag) {
								result = result + r2.getElement(type) + ")";
							} else {
								result = result + "(" + r2.getElement(type)
										+ ")";
							}

						}
					} else {
						if (r2 != null) {
							if (sbmlFlag) {
								result = result + r2.getElement(type) + ")";
							} else {
								result = result + r2.getElement(type);
							}

						}
					}
				} else if (op.equals("||")) {
					if (r1.isit == 'r') {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "or(" + r1.getElement(type) + ",";
							} else if (verilog) {
								result = "(" + r1.getElement(type) + ")||";
							} else {
								result = "(" + r1.getElement(type) + ")";
							}

						}
					} else {
						if (r1 != null) {
							if (sbmlFlag) {
								result = "or(" + r1.getElement(type) + ",";
							} else if (verilog) {
								result = r1.getElement(type) + "||";
							} else {
								result = r1.getElement(type);
							}
						}
					}

					if (!sbmlFlag && !verilog) {
						result = result + "|";
					}

					if (r2.isit == 'r') {
						if (r2 != null) {
							if (sbmlFlag) {
								result = result + r2.getElement(type) + ")";
							} else {
								result = result + "(" + r2.getElement(type)
										+ ")";
							}

						}
					} else {
						if (r2 != null) {
							if (sbmlFlag) {
								result = result + r2.getElement(type) + ")";
							} else {
								result = result + r2.getElement(type);
							}

						}
					}
				} else if (op.equals("f")) {
					if (r1 != null) {
						if (r1.isit == 'n') {
							result = new Integer((int) Math.floor(r1.lvalue))
									.toString();
						} else {
							if (sbmlFlag) {
								result = "floor(" + r1.getElement(type) + ")";
							} else if (verilog) {
								result = "$floor(" + r1.getElement(type) + ")";
							} else {
								result = "floor(" + r1.getElement(type) + ")";
							}
						}
					}
				} else if (op.equals("c")) {
					if (r1 != null) {
						if (r1.isit == 'n') {
							result = new Integer((int) Math.ceil(r1.lvalue))
									.toString();
						} else {
							if (sbmlFlag) {
								result = "ceil(" + r1.getElement(type) + ")";
							} else if (verilog) {
								result = "$ceil(" + r1.getElement(type) + ")";
							} else {
								result = "ceil(" + r1.getElement(type) + ")";
							}
						}
					}
				} else if (op.equals("m")) {
					if (r1 != null && r2 != null) {
						if (r1.isit == 'n' && r2.isit == 'n') {
							if (r1.lvalue < r2.lvalue) {
								result = r1.getElement(type);
							} else {
								result = r2.getElement(type);
							}
						} else {
							if (sbmlFlag) {
								result = "piecewise(" + r1.getElement(type)
										+ ",leq(" + r1.getElement(type) + ","
										+ r2.getElement(type) + "),"
										+ r2.getElement(type) + ")";
							//} else if (verilog) {
								//result = "min(" + r1.getElement(type) + ","
									//	+ r2.getElement(type) + ")";
								} else if (verilog) {
								result = "("+r1.getElement(type) +"<"+r2.getElement(type) +"?"+r1.getElement(type) +":"+r2.getElement(type) +")";
							} else {
								result = "min(" + r1.getElement(type) + ","
										+ r2.getElement(type) + ")";
							}
						}

					}
				} else if (op.equals("M")) {
					if (r1 != null && r2 != null) {
						if (r1.isit == 'n' && r2.isit == 'n') {
							if (r1.lvalue > r2.lvalue) {
								result = r1.getElement(type);
							} else {
								result = r2.getElement(type);
							}
						} else {
							if (sbmlFlag) {
								result = "piecewise(" + r1.getElement(type)
										+ ",geq(" + r1.getElement(type) + ","
										+ r2.getElement(type) + "),"
										+ r2.getElement(type) + ")";
							//} else if (verilog) {
								//result = "max(" + r1.getElement(type) + ","
										//+ r2.getElement(type) + ")";
							} else if (verilog) {
								result = "("+r1.getElement(type) +">"+r2.getElement(type) +"?"+r1.getElement(type) +":"+r2.getElement(type) +")";
							} else {
								result = "max(" + r1.getElement(type) + ","
										+ r2.getElement(type) + ")";
							}
						}
					}
				} else if (op.equals("i")) {
					if (r1 != null && r2 != null) {
						if (sbmlFlag) {
							result = "floor(" + r1.getElement(type) + "/"
									+ r2.getElement(type) + ")";
						} else if (verilog) {
							result = "floor(" + r1.getElement(type) + "/"
									+ r2.getElement(type) + ")";
						} else {
							result = "idiv(" + r1.getElement(type) + ","
									+ r2.getElement(type) + ")";
						}

					}
				} else if (op.equals("uniform")) {
					if (r1 != null && r2 != null) {
						if (verilog) {
							result = "uniform(" + r1.getElement(type) + ","
									+ r2.getElement(type) + ")";
						} else {
							result = "uniform(" + r1.getElement(type) + ","
									+ r2.getElement(type) + ")";
						}
					}
				} // TODO: Add verilog functions for other distributions
				else if (op.equals("[]")) {
					if (r1 != null && r2 != null) {
						result = "BIT(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}
				} else if (op.equals("normal")) {
					if (r1 != null && r2 != null) {
						result = "normal(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}
				} else if (op.equals("gamma")) {
					if (r1 != null && r2 != null) {
						result = "gamma(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}
				} else if (op.equals("lognormal")) {
					if (r1 != null && r2 != null) {
						result = "lognormal(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}
				} else if (op.equals("binomial")) {
					if (r1 != null && r2 != null) {
						result = "binomial(" + r1.getElement(type) + ","
								+ r2.getElement(type) + ")";
					}
				} else if (op.equals("exponential")) {
					if (r1 != null) {
						result = "exponential(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("chisq")) {
					if (r1 != null) {
						result = "chisq(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("laplace")) {
					if (r1 != null) {
						result = "laplace(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("cauchy")) {
					if (r1 != null) {
						result = "cauchy(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("rayleigh")) {
					if (r1 != null) {
						result = "rayleigh(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("poisson")) {
					if (r1 != null) {
						result = "poisson(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("bernoulli")) {
					if (r1 != null) {
						result = "bernoulli(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("rate")) {
					if (r1 != null) {
						result = "rate(" + r1.getElement(type) + ")";
					}
				} else if (op.equals("INT")) {
					if (r1 != null) {
						if (sbmlFlag) {
							result = "piecewise(1," + r1.getElement(type)
									+ ",0 )";
						} else {
							result = "INT(" + r1.getElement(type) + ")";
						}

					}
				} else if (op.equals("==")) {
					if (r1 != null) {
						if (sbmlFlag) {
							result = "eq(" + r1.getElement(type) + ",";
						} else if (verilog) {
							result = r1.getElement(type) + "==";
						} else {
							result = r1.getElement(type);
						}

					}
					if (!sbmlFlag && !verilog) {
						result = result + "=";
					}

					if (r2 != null) {
						if (sbmlFlag) {
							result = result + r2.getElement(type) + ")";
						} else {
							result = result + r2.getElement(type);
						}

					}
				} else if (op.equals("+")) {
					if (r1.isit == 'n' && r1.lvalue >= 0 && r2.isit == 'a'
							&& r2.op.equals("uniform")) {
						ExprTree tempUniform = new ExprTree(r2);
						r1.setNodeValues(r1, tempUniform.r1, "+", 'a');
						r2.setNodeValues(r1, tempUniform.r2, "+", 'a');
						isit = 'a';
						op = "uniform";
					} else if (r1.isit == 'a' && r1.op.equals("uniform")
							&& r2.isit == 'n' && r2.lvalue >= 0) {
						ExprTree tempUniform = new ExprTree(r1);
						r1.setNodeValues(r2, tempUniform.r1, "+", 'a');
						r2.setNodeValues(r2, tempUniform.r2, "+", 'a');
						isit = 'a';
						op = "uniform";
					} else {
						try {
							String r1String = r1.getElement(type);
							String r2String = r2.getElement(type);
							result = new Float(Float.parseFloat(r1String)
									+ Float.parseFloat(r2String)).toString();
						} catch (NumberFormatException e) {
							if (r1.isit == 'b'
									|| r1.isit == 'i'
									|| r1.isit == 'c'
									|| r1.isit == 'n'
									|| r1.isit == 't'
									|| (r1.isit == 'a' && (r1.op.equals("+")
											|| r1.op.equals("-")
											|| r1.op.equals("*")
											|| r1.op.equals("/") || r1.op
											.equals("^")))) {
								if (r1 != null) {
									result = r1.getElement(type);
								}
							} else {
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
									|| (r2.isit == 'a' && (r2.op.equals("+")
											|| r2.op.equals("-")
											|| r2.op.equals("*")
											|| r2.op.equals("/") || r2.op
											.equals("^")))) {
								if (r2 != null) {
									result = result + r2.getElement(type);
								}
							} else {
								if (r2 != null) {
									result = result + "(" + r2.getElement(type)
											+ ")";
								}
							}
						}
					}
				} else if (op.equals("-")) {
					if (r1.isit == 'a' && r1.op.equals("uniform")
							&& r2.isit == 'n' && r2.lvalue >= 0) {
						ExprTree tempUniform = new ExprTree(r1);
						r1.setNodeValues(tempUniform.r1, r2, "-", 'a');
						r2.setNodeValues(tempUniform.r2, r2, "-", 'a');
						isit = 'a';
						op = "uniform";
					} else {
						try {
							String r1String = r1.getElement(type);
							String r2String = r2.getElement(type);
							result = new Float(Float.parseFloat(r1String)
									- Float.parseFloat(r2String)).toString();
						} catch (NumberFormatException e) {
							if (r1.isit == 'b'
									|| r1.isit == 'i'
									|| r1.isit == 'c'
									|| r1.isit == 'n'
									|| r1.isit == 't'
									|| (r1.isit == 'a' && (r1.op.equals("+")
											|| r1.op.equals("-")
											|| r1.op.equals("*")
											|| r1.op.equals("/") || r1.op
											.equals("^")))) {
								if (r1 != null) {
									result = r1.getElement(type);
								}
							} else {
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
									|| (r2.isit == 'a' && (r2.op.equals("-")
											|| r2.op.equals("*")
											|| r2.op.equals("/") || r2.op
											.equals("^")))) {
								if (r2 != null) {
									result = result + r2.getElement(type);
								}
							} else {
								if (r2 != null) {
									result = result + "(" + r2.getElement(type)
											+ ")";
								}
							}
						}
					}
				} else if (op.equals("*")) {
					if (r1.isit == 'n' && r1.lvalue >= 0 && r2.isit == 'a'
							&& r2.op.equals("uniform")) {
						ExprTree tempUniform = new ExprTree(r2);
						r1.setNodeValues(r1, tempUniform.r1, "*", 'a');
						r2.setNodeValues(r1, tempUniform.r2, "*", 'a');
						isit = 'a';
						op = "uniform";
					} else if (r1.isit == 'a' && r1.op.equals("uniform")
							&& r2.isit == 'n' && r2.lvalue >= 0) {
						ExprTree tempUniform = new ExprTree(r1);
						r1.setNodeValues(r2, tempUniform.r1, "*", 'a');
						r2.setNodeValues(r2, tempUniform.r2, "*", 'a');
						isit = 'a';
						op = "uniform";
					} else {
						try {
							String r1String = r1.getElement(type);
							String r2String = r2.getElement(type);
							result = new Float(Float.parseFloat(r1String)
									* Float.parseFloat(r2String)).toString();
						} catch (NumberFormatException e) {
							if (r1.isit == 'b'
									|| r1.isit == 'i'
									|| r1.isit == 'c'
									|| r1.isit == 'n'
									|| r1.isit == 't'
									|| (r1.isit == 'a' && (r1.op.equals("*")
											|| r1.op.equals("/") || r1.op
											.equals("^")))) {
								if (r1 != null) {
									result = r1.getElement(type);
								}
							} else {
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
									|| (r2.isit == 'a' && (r2.op.equals("*")
											|| r2.op.equals("/") || r2.op
											.equals("^")))) {
								if (r2 != null) {
									result = result + r2.getElement(type);
								}
							} else {
								if (r2 != null) {
									result = result + "(" + r2.getElement(type)
											+ ")";
								}
							}
						}
					}
				} else if (op.equals("/")) {
					if (r1.isit == 'a' && r1.op.equals("uniform")
							&& r2.isit == 'n' && r2.lvalue >= 0) {
						ExprTree tempUniform = new ExprTree(r1);
						r1.setNodeValues(tempUniform.r1, r2, "/", 'a');
						r2.setNodeValues(tempUniform.r2, r2, "/", 'a');
						isit = 'a';
						op = "uniform";
					} else {
						try {
							String r1String = r1.getElement(type);
							String r2String = r2.getElement(type);
							result = new Float(Float.parseFloat(r1String)
									/ Float.parseFloat(r2String)).toString();
						} catch (NumberFormatException e) {
							if (r1.isit == 'b'
									|| r1.isit == 'i'
									|| r1.isit == 'c'
									|| r1.isit == 'n'
									|| r1.isit == 't'
									|| (r1.isit == 'a' && (r1.op.equals("*")
											|| r1.op.equals("/") || r1.op
											.equals("^")))) {
								if (r1 != null) {
									result = r1.getElement(type);
								}
							} else {
								if (r1 != null) {
									result = "(" + r1.getElement(type) + ")";
								}
							}
							result = result + "/";
							if (r2.isit == 'b'
									|| r2.isit == 'i'
									|| r2.isit == 'c'
									|| r2.isit == 'n'
									|| r2.isit == 't'
									|| (r2.isit == 'a' && (r2.op.equals("/") || r2.op
											.equals("^")))) {
								if (r2 != null) {
									result = result + r2.getElement(type);
								}
							} else {
								if (r2 != null) {
									result = result + "(" + r2.getElement(type)
											+ ")";
								}
							}
						}
					}
				} else if (op.equals("^")) {
					try {
						String r1String = r1.getElement(type);
						String r2String = r2.getElement(type);
						result = new Integer(Integer.parseInt(r1String)
								^ Integer.parseInt(r2String)).toString();
					} catch (NumberFormatException e) {
						if (r1.isit == 'b'
								|| r1.isit == 'i'
								|| r1.isit == 'c'
								|| r1.isit == 'n'
								|| r1.isit == 't'
								|| (r1.isit == 'a' && (r1.op.equals("*")
										|| r1.op.equals("/") || r1.op
										.equals("^")))) {
							if (r1 != null) {
								result = "(" + r1.getElement(type) + ")";
							}
						} else {
							if (r1 != null) {
								result = "(" + r1.getElement(type) + ")";
							}
						}
						result = result + "^";
						if (r2.isit == 'b'
								|| r2.isit == 'i'
								|| r2.isit == 'c'
								|| r2.isit == 'n'
								|| r2.isit == 't'
								|| (r2.isit == 'a' && (r2.op.equals("/") || r2.op
										.equals("^")))) {
							if (r2 != null) {
								result = result + "(" + r2.getElement(type)
										+ ")";
							}
						} else {
							if (r2 != null) {
								result = result + "(" + r2.getElement(type)
										+ ")";
							}
						}
					}
				}
				// relational ops: geq, leq, gt, lt
				// mod
				else {
					if (!sbmlFlag) {
						if (r1 != null) {
							if (r1.isit == 'b' || r1.isit == 'i'
									|| r1.isit == 'c' || r1.isit == 'n'
									|| r1.isit == 't') {
								result = r1.getElement(type);
							} else {
								result = "(" + r1.getElement(type) + ")";
							}
						}
						result = result + op;
						if (r2 != null) {
							if (r2.isit == 'b' || r2.isit == 'i'
									|| r2.isit == 'c' || r2.isit == 'n'
									|| r2.isit == 't') {
								result = result + r2.getElement(type);
							} else {
								result = result + "(" + r2.getElement(type)
										+ ")";
							}
						}
					}

					if (sbmlFlag) {
						if (op.equals("<=")) {
							if (r1 != null && r2 != null) {
								result = "leq(" + r1.getElement(type) + ","
										+ r2.getElement(type) + ")";
							}
						}
						if (op.equals(">=")) {
							if (r1 != null && r2 != null) {
								result = "geq(" + r1.getElement(type) + ","
										+ r2.getElement(type) + ")";
							}
						}
						if (op.equals(">")) {
							if (r1 != null && r2 != null) {
								result = "gt(" + r1.getElement(type) + ","
										+ r2.getElement(type) + ")";
							}
						}
						if (op.equals("<")) {
							if (r1 != null && r2 != null) {
								result = "lt(" + r1.getElement(type) + ","
										+ r2.getElement(type) + ")";
							}
						}
						if (op.equals("%")) {
							if (r1 != null && r2 != null) {
								result = "mod(" + r1.getElement(type) + ","
										+ r2.getElement(type) + ")";
							}
						}

					}
				}
			}
		}
		return result;
	}

	public ExprTree minimizeUniforms() {
		if (r1 != null) {
			r1.minimizeUniforms();
		}
		if (r2 != null) {
			r2.minimizeUniforms();
		}
		if (isit == 'a' && op.equals("m")) {
			if (r1.isit == 'n' && r2.isit == 'n') {
				isit = 'n';
				if (r1.lvalue < r2.lvalue) {
					lvalue = r1.lvalue;
				} else {
					lvalue = r2.lvalue;
				}
				r1 = null;
				r2 = null;
			} else if (r1.isit == 'a' && r1.op.equals("uniform")
					&& r2.isit == 'a' && r2.op.equals("uniform")) {
				ExprTree l1 = r1.r1;
				ExprTree l2 = r2.r1;
				ExprTree u1 = r1.r2;
				ExprTree u2 = r2.r2;
				op = "uniform";
				r1.op = "m";
				r2.op = "m";
				r1.r1 = l1;
				r1.r2 = l2;
				r2.r1 = u1;
				r2.r2 = u2;
			}
		}
		if (isit == 'a' && op.equals("M")) {
			if (r1.isit == 'n' && r2.isit == 'n') {
				isit = 'n';
				if (r1.lvalue < r2.lvalue) {
					lvalue = r2.lvalue;
				} else {
					lvalue = r1.lvalue;
				}
				r1 = null;
				r2 = null;
			} else if (r1.isit == 'a' && r1.op.equals("uniform")
					&& r2.isit == 'a' && r2.op.equals("uniform")) {
				ExprTree l1 = r1.r1;
				ExprTree l2 = r2.r1;
				ExprTree u1 = r1.r2;
				ExprTree u2 = r2.r2;
				op = "uniform";
				r1.op = "M";
				r2.op = "M";
				r1.r1 = l1;
				r1.r2 = l2;
				r2.r1 = u1;
				r2.r2 = u2;
			}
		}
		if (isit == 'a' && op.equals("+")) {
			if (r1.isit == 'a' && r1.op.equals("uniform") && r2.isit == 'a'
					&& r2.op.equals("uniform")) {
				ExprTree l1 = r1.r1;
				ExprTree l2 = r2.r1;
				ExprTree u1 = r1.r2;
				ExprTree u2 = r2.r2;
				op = "uniform";
				r1.op = "+";
				r2.op = "+";
				r1.r1 = l1;
				r1.r2 = l2;
				r2.r1 = u1;
				r2.r2 = u2;
			}
		}
		if (isit == 'a' && op.equals("-")) {
			if (r1.isit == 'a' && r1.op.equals("uniform") && r2.isit == 'a'
					&& r2.op.equals("uniform")) {
				ExprTree l1 = r1.r1;
				ExprTree l2 = r2.r1;
				ExprTree u1 = r1.r2;
				ExprTree u2 = r2.r2;
				op = "uniform";
				r1.op = "+";
				r2.op = "+";
				r1.r1 = l1;
				r1.r2 = u2;
				r2.r1 = u1;
				r2.r2 = l2;
			}
		}
		if (isit == 'a' && op.equals("c")) {
			if (r1.isit == 'a' && r1.op.equals("uniform")) {
				ExprTree l1 = r1.r1;
				ExprTree u1 = r1.r2;
				op = "uniform";
				r1 = new ExprTree(l1, null, "c", 'a');
				r2 = new ExprTree(u1, null, "c", 'a');
			}
		}
		if (isit == 'a' && op.equals("f")) {
			if (r1.isit == 'a' && r1.op.equals("uniform")) {
				ExprTree l1 = r1.r1;
				ExprTree u1 = r1.r2;
				op = "uniform";
				r1 = new ExprTree(l1, null, "f", 'a');
				r2 = new ExprTree(u1, null, "f", 'a');
			}
		}
		if (isit == 'a' && op.equals("uniform")) {
			if (r1.isit == 'a' && r1.op.equals("uniform")) {
				r1 = r1.r1;
			}
			if (r2.isit == 'a' && r2.op.equals("uniform")) {
				r2 = r2.r2;
			}
		}
		return this;
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
				} else if (r1.isEqual(expr.r1)) {
					r1Same = true;
				}
				if (r2 == null) {
					if (expr.r2 == null) {
						r2Same = true;
					}
				} else if (r2.isEqual(expr.r2)) {
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
						if (variables.get(variable).equals(
								variables.get(expr.variable)))
							same = true;
					}
				} else if (variable.equals(expr.variable)) {
					same = true;
				}
				break;
			case 'n': // Number
			case 't': // Truth value
				if (uvalue == expr.uvalue && lvalue == expr.lvalue) {
					same = true;
				} else if (variables.containsKey(expr.variable)) {
					if (uvalue == lvalue) {
						if (uvalue == 1.0
								&& variables.get(expr.variable).toLowerCase()
										.equals("true"))
							same = true;
						else if (uvalue == 0.0
								&& variables.get(expr.variable).toLowerCase()
										.equals("false"))
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
				} else if (r1.isEqual(expr.r1)) {
					r1Same = true;
				}
				if (r2 == null) {
					if (expr.r2 == null) {
						r2Same = true;
					}
				} else if (r2.isEqual(expr.r2)) {
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

	public void setNodeValues(ExprTree nr1, ExprTree nr2, String nop,
			char willbe) {
		ExprTree r1temp = null, r2temp = null;
		if (nr1 != null) {
			r1temp = new ExprTree(nr1);
		}
		if (nr2 != null) {
			r2temp = new ExprTree(nr2);
		}
		r1 = r1temp;
		r2 = r2temp;
		op = nop;
		isit = willbe;
		if ((isit == 'r') || (isit == 'l')) {
			logical = true;
			uvalue = 1;
			lvalue = 0;
		} else {
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
					} else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w'
								|| r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						} else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue,
									r2.variable);
						}
					}
				} else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
					} else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
								|| r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						} else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue,
									r1.variable);
						}
					}
				} else if (r1.equals(r2)) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
							|| r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					} else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				} else {
					ExprTree notE = new ExprTree(this);
					notE.setNodeValues((this), null, "!", 'l');
					if (r1.equals(notE) || notE.equals(r1)) {
						setVarValues('t', 0.0, 0.0, null);
					}
				}
			} else if (op.equals("||")) {
				if ((r1.isit == 'n') || (r1.isit == 't')) {
					if (r1.lvalue != 0) {
						setVarValues('t', 1.0, 1.0, null);
					} else {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w'
								|| r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						} else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue,
									r2.variable);
						}
					}
				} else if (((r2).isit == 'n') || ((r2).isit == 't')) {
					if (r2.lvalue != 0) {
						setVarValues('t', 1.0, 1.0, null);
					} else {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
								|| r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						} else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue,
									r1.variable);
						}
					}
				} else if (r1.equals(r2)) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
							|| r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					} else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				} else {
					ExprTree notE = new ExprTree(this);
					notE.setNodeValues((this), null, "!", 'l');
					if (r1.equals(notE) || notE.equals(r1)) {
						setVarValues('t', 1.0, 1.0, null);
					}
				}
			} else if (op.equals("->")) {
				if (r1.isit == 'n' || r1.isit == 't') {
					if (r1.lvalue != 0) {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w'
								|| r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						} else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue,
									r2.variable);
						}
					} else if (r1.uvalue == 0) {
						setVarValues('t', 1.0, 1.0, null);
					}
				} else if (r2.isit == 't' || r2.isit == 'n') {
					if (r2.lvalue != 0) {
						setVarValues('t', 1.0, 1.0, null);
					} else if (r2.uvalue == 0) {
						ExprTree notE = new ExprTree(r2);
						notE.setNodeValues((this), null, "!", 'l');
						setNodeValues(notE.r1, notE.r2, notE.op, notE.isit);
					}
				}
			} else if (op.equals("!")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 't';
					if (r1.lvalue == 1) {
						this.lvalue = 0;
					} else {
						this.lvalue = 1;
					}
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("==")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if (r1.lvalue == r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals(">=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue >= r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals(">")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue > r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("<=")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue <= r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("<")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					if ((r1).lvalue < r2.lvalue) {
						this.lvalue = 1;
					} else {
						this.lvalue = 0;
					}
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("&")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = ((int) (r1).lvalue) & ((int) r2.lvalue);
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("|")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue | (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
			} else if (isit == 'w' && op.equals("X")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (int) (r1).lvalue ^ (int) r2.lvalue;
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("m")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = Math.min((r1).lvalue, r2.lvalue);
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("M")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = Math.max((r1).lvalue, r2.lvalue);
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("i")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = Math.floor((r1).lvalue / r2.lvalue);
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("f")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = Math.floor((r1).lvalue);
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("c")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = Math.ceil((r1).lvalue);
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("~")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = ~(int) (r1).lvalue;
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("[]")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 't';
					(this).lvalue = (((int) (r1).lvalue) >> ((int) r2.lvalue)) & 1;
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("U-")) {
				if (((r1).isit == 'n') || ((r1).isit == 't')) {
					(this).isit = 'n';
					(this).lvalue = -((r1).lvalue);
					(this).uvalue = (this).lvalue;
				}
			} else if (op.equals("*")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue * r2.lvalue;
					(this).uvalue = (this).lvalue;
				} else if (r1.isit == 'n' || r1.isit == 't') {
					if (r1.lvalue == 0 && r1.uvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
					} else if (r1.lvalue == 1 && r1.uvalue == 1) {
						if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w'
								|| r2.isit == 'r') {
							setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
						} else {
							setVarValues(r2.isit, r2.lvalue, r2.uvalue,
									r2.variable);
						}
					}
				} else if (r2.isit == 'n' || r2.isit == 't') {
					if (r2.lvalue == 0 && r2.uvalue == 0) {
						setVarValues('t', 0.0, 0.0, null);
					} else if (r2.lvalue == 1 && r2.uvalue == 1) {
						if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
								|| r1.isit == 'r') {
							setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
						} else {
							setVarValues(r1.isit, r1.lvalue, r1.uvalue,
									r1.variable);
						}
					}
				}
			} else if (op.equals("/")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue / r2.lvalue;
					(this).uvalue = (this).lvalue;
				} else if ((r1.isit == 'n' || r1.isit == 't') && r1.uvalue == 0
						&& r1.lvalue == 0) {
					setVarValues('n', 0.0, 0.0, null);
				} else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 1
						&& r2.uvalue == 1) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
							|| r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					} else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				}
			} else if (op.equals("%")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue % r2.lvalue;
					(this).uvalue = (this).lvalue;
				} else if ((r2.isit == 'n' || r2.isit == 't')
						&& r2.lvalue == 1.0 && r2.uvalue == 1.0) {
					setVarValues('n', 0.0, 0.0, null);
				} else if ((r1.isit == 'n' || r1.isit == 't')
						&& r1.lvalue == 1.0 && r1.uvalue == 1.0) {
					setVarValues('n', 1.0, 1.0, null);
				}
			} else if (op.equals("+")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue + r2.lvalue;
					(this).uvalue = (this).lvalue;
				} else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 0
						&& r1.uvalue == 0) {
					if (r2.isit == 'l' || r2.isit == 'a' || r2.isit == 'w'
							|| r2.isit == 'r') {
						setNodeValues(r2.r1, r2.r2, r2.op, r2.isit);
					} else {
						setVarValues(r2.isit, r2.lvalue, r2.uvalue, r2.variable);
					}
				} else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 0
						&& r2.uvalue == 0) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
							|| r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					} else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				}
			} else if (op.equals("-")) {
				if (((r1.isit == 'n') || (r1.isit == 't'))
						&& (((r2).isit == 'n') || ((r2).isit == 't'))) {
					(this).isit = 'n';
					(this).lvalue = (r1).lvalue - r2.lvalue;
					(this).uvalue = (this).lvalue;
				} else if ((r1.isit == 'n' || r1.isit == 't') && r1.lvalue == 0
						&& r1.uvalue == 0) {
					setNodeValues(r2, null, "U-", 'a');
				} else if ((r2.isit == 'n' || r2.isit == 't') && r2.lvalue == 0
						&& r2.uvalue == 0) {
					if (r1.isit == 'l' || r1.isit == 'a' || r1.isit == 'w'
							|| r1.isit == 'r') {
						setNodeValues(r1.r1, r1.r2, r1.op, r1.isit);
					} else {
						setVarValues(r1.isit, r1.lvalue, r1.uvalue, r1.variable);
					}
				}
			}
		}
	}

	public double evaluateExpr(HashMap<String, String> variables) {
		double left;
		double right;
		switch (isit) {
		case 'b': // Boolean
			if (variables != null) {
				if (!variables.containsKey(variable)
						|| variables.get(variable).toLowerCase().equals(
								"unknown"))
					return Double.NaN;
				if (variables.get(variable).toLowerCase().equals("true") ||
					variables.get(variable).equals("1")) {
					return 1.0;
				} else {
					return 0.0;
				}
			} else {
				return Double.NaN;
			}
		case 'c': // Continuous
			return Double.NaN;
		case 'i': // Integer
			if (variables != null) {
				try {
					return Double.parseDouble(variables.get(variable));
				} catch (Exception e) {
					return Double.NaN;
				}
			} else {
				return Double.NaN;
			}
		case 'n': // Number
			if (uvalue == lvalue) {
				return uvalue;
			} else {
				return ((uvalue - lvalue) * new java.util.Random().nextDouble())
						+ lvalue;
			}
		case 't': // Truth value
			if (uvalue == 1 && lvalue == 1) {
				return 1.0;
			} else if (uvalue == 0 && lvalue == 0) {
				return 0.0;
			} else {
				return Double.NaN;
			}
		case 'w': // bitWise
			if (r1 != null) {
				left = r1.evaluateExpr(variables);
			} else {
				left = Double.NaN;
			}
			if (r2 != null) {
				right = r2.evaluateExpr(variables);
			} else {
				right = Double.NaN;
			}
			if (op.equals("&")) {
				return ((int) left) & ((int) right);
			} else if (op.equals("|")) {
				return ((int) left) | ((int) right);
			} else if (op.equals("!")) {
				return ~((int) left);
			} else if (op.equals("X")) {
				return ((int) left) ^ ((int) right);
			}
		case 'a': // Arithmetic
		case 'r': // Relational
		case 'l': // Logical
			if (op.equals("!")) {
				if (r1 != null) {
					if (r1.evaluateExpr(variables) == 1.0) {
						return 0.0;
					} else if (r1.evaluateExpr(variables) == 0.0) {
						return 1.0;
					} else {
						return Double.NaN;
					}
				} else if (r2 != null) {
					if (r2.evaluateExpr(variables) == 1.0) {
						return 0.0;
					} else if (r2.evaluateExpr(variables) == 0.0) {
						return 1.0;
					} else {
						return Double.NaN;
					}
				} else {
					return Double.NaN;
				}
			} else {
				if (r1 != null) {
					left = r1.evaluateExpr(variables);
				} else {
					left = Double.NaN;
				}
				if (r2 != null) {
					right = r2.evaluateExpr(variables);
				} else {
					right = Double.NaN;
				}
				if (op.equals("&&")) {
					if (left == 1.0 && right == 1.0) {
						return 1.0;
					} else if (left == 0.0 || right == 0.0) {
						return 0.0;
					} else
						return Double.NaN;
				} else if (op.equals("||")) {
					if (left == 1.0 || right == 1.0) {
						return 1.0;
					} else if (left == 0.0 && right == 0.0) {
						return 0.0;
					} else
						return Double.NaN;
				} else if (op.equals("==")) {
					if (left == Double.NaN || right == Double.NaN) {
						return Double.NaN;
					} else if (left == right) {
						return 1.0;
					} else if (left != right) {
						return 0.0;
					} else {
						return Double.NaN;
					}
				} else if (op.equals("->")) {
					if (left == 0.0 && (right == 1.0 || right == 0.0)) {
						return 1.0;
					} else if (left == 1.0 && right == 1.0) {
						return 1.0;
					} else if (left == 1.0 && right == 0.0) {
						return 0.0;
					} else {
						return Double.NaN;
					}
				} else if (op.equals("+")) {
					return left + right;
				} else if (op.equals("*")) {
					return left * right;
				} else if (op.equals("/")) {
					return left / right;
				} else if (op.equals("%")) {
					return left % right;
				} else if (op.equals("^")) {
					return Math.pow(left, right);
				} else if (op.equals("[]")) {
					return (((int) left) >> ((int) right)) & 1;
				} else if (op.equals("f")) {
					return Math.floor(left);
				} else if (op.equals("c")) {
					return Math.ceil(left);
				} else if (op.equals("m")) {
					return Math.min(left, right);
				} else if (op.equals("M")) {
					return Math.max(left, right);
				} else if (op.equals("i")) {
					return ((int) left) / ((int) right);
				} else if (op.equals("uniform")) {
					return Double.NaN;
				} else if (op.equals("normal")) {
					return Double.NaN;
				} else if (op.equals("gamma")) {
					return Double.NaN;
				} else if (op.equals("lognormal")) {
					return Double.NaN;
				} else if (op.equals("binomial")) {
					return Double.NaN;
				} else if (op.equals("exponential")) {
					return Double.NaN;
				} else if (op.equals("chisq")) {
					return Double.NaN;
				} else if (op.equals("laplace")) {
					return Double.NaN;
				} else if (op.equals("cauchy")) {
					return Double.NaN;
				} else if (op.equals("rayleigh")) {
					return Double.NaN;
				} else if (op.equals("poisson")) {
					return Double.NaN;
				} else if (op.equals("bernoulli")) {
					return Double.NaN;
				} else if (op.equals("rate")) {
					return Double.NaN;
				} else if (op.equals("INT")) {
					return ((int) left);
				} else if (op.equals("<")) {
					if (left < right) {
						return 1.0;
					} else if (left >= right) {
						return 0.0;
					} else {
						return Double.NaN;
					}
				} else if (op.equals(">")) {
					if (left > right) {
						return 1.0;
					} else if (left <= right) {
						return 0.0;
					} else {
						return Double.NaN;
					}
				} else if (op.equals("<=")) {
					if (left <= right) {
						return 1.0;
					} else if (left > right) {
						return 0.0;
					} else {
						return Double.NaN;
					}
				} else if (op.equals(">=")) {
					if (left >= right) {
						return 1.0;
					} else if (left < right) {
						return 0.0;
					} else {
						return Double.NaN;
					}
				} else {
					return Double.NaN;
				}
			}
		}
		return Double.NaN;
	}

	private static final int WORD = 1;

	private static final int IMPLIES = 7;

	private static final int END_OF_STRING = 2;

	private static final int INFIN = 2147483647;
	
	public String getOp()
	{
		return op;
	}
	
	public ExprTree getLeftChild()
	{
		return r1;
	}
	
	public ExprTree getRightChild()
	{
		return r2;
	}

	public ExprTree getExprTree() {
		token = this.intexpr_gettok(expression);
		this.intexpr_L(expression);
		return this;
	}
	public void setIntegerSignals(Set<String> signalSet) {
		for (String s : signalSet) {
			integerSignals.add(s);
		}	
	}

}