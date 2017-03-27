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
package main.java.edu.utah.ece.async.lpn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import main.java.edu.utah.ece.async.verification.timed_state_exploration.octagon.Equivalence;
import main.java.edu.utah.ece.async.verification.timed_state_exploration.zoneProject.IntervalPair;
import main.java.edu.utah.ece.async.verification.timed_state_exploration.zoneProject.LPNContAndRate;
import main.java.edu.utah.ece.async.verification.timed_state_exploration.zoneProject.LPNContinuousPair;
import main.java.edu.utah.ece.async.util.GlobalConstants;

import java.lang.Math;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
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

	ExprTree newresult;

	private ArrayList<String> booleanSignals, integerSignals, continuousSignals;

	private LPN lhpn;
	
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

	public ExprTree(LPN lhpn) {
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

//	public ExprTree(Transition transition) {
//	}

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
	
	/**
	 * This constructor takes a list of variables names. 
	 * Each variable name is either an actual LPN discrete integer variable name, or
	 * a variable that is created during the Markovian analysis of nested properties. 
	 * @param varNameList
	 */
	public ExprTree(ArrayList<String> varNameList) {
		booleanSignals = new ArrayList<String>();
		continuousSignals = new ArrayList<String>();
		integerSignals = new ArrayList<String>();
		for (int j = 0; j < varNameList.size(); j++) {
			integerSignals.add(varNameList.get(j));
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
				position--;
				return (WORD);
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
				} /*else if (!readword){  // TODO: had to remove to make exponential parse but does scientific notation still work?
					return -1;
				}*/
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

	public void intexpr_U(String expr) {
		double temp;

		switch (token) {
		case WORD:
			if (tokvalue.toLowerCase().equals("and")) {
				token = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				token = intexpr_gettok(expr);
				intexpr_R(expr);
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_L(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
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
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), newresult, "uniform", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("normal")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), newresult, "normal", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("gamma")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), newresult, "gamma", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("lognormal")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), newresult, "lognormal", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("binomial")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ',') {
					throw new IllegalArgumentException("ERROR: Expected a ,\n");
				}
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
				token = newresult.token;
				position = newresult.position;
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), newresult, "binomial", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("exponential")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "exponential", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("chisq")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "chisq", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("laplace")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "laplace", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("cauchy")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "cauchy", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("rayleigh")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "rayleigh", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("poisson")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "poisson", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("bernoulli")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "bernoulli", 'a');
				(token) = intexpr_gettok(expr);
			} else if (tokvalue.equals("rate")) {
				(token) = intexpr_gettok(expr);
				if ((token) != '(') {
					throw new IllegalArgumentException("ERROR: Expected a (\n");
				}
				(token) = intexpr_gettok(expr);
				intexpr_R(expr);
				if ((token) != ')') {
					throw new IllegalArgumentException("ERROR: Expected a )\n");
				}
				setNodeValues((this), null, "rate", 'a');
				(token) = intexpr_gettok(expr);
			} else if ((tokvalue.equals("true")) || tokvalue.equals("TRUE")) {
				setVarValues('t', 1, 1, null);
				(token) = intexpr_gettok(expr);
			} else if ((tokvalue.equals("maybe")) || tokvalue.equals("MAYBE")) {
				setVarValues('t', 0, 1, null);
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
					return;
				}
				else if (integerSignals.contains(tokvalue)) {
					setVarValues('i', -INFIN, INFIN, tokvalue);
					(token) = intexpr_gettok(expr);
					return;
				}
				else if (continuousSignals.contains(tokvalue)) {
					setVarValues('c', -INFIN, INFIN, tokvalue);
					(token) = intexpr_gettok(expr);
					return;
				}
				if (tokvalue.equals("")) {
					throw new IllegalArgumentException(String.format(
							"U1:ERROR(%s): Expected a ID, Number, or a (\n",
							tokvalue));
				} else if ((tokvalue.charAt(0)) > ('9')
						|| ((tokvalue.charAt(0)) < '0')) {
					throw new IllegalArgumentException(String.format(
							"U1:ERROR(%s): Expected a ID, Number, or a (\n",
							tokvalue));
				}
				temp = Double.parseDouble(tokvalue);
				setVarValues('n', temp, temp, null);
				token = intexpr_gettok(expr);
			}
			break;
		case '(':
			(token) = intexpr_gettok(expr);
			intexpr_L(expr);
			if ((token) != ')') {
				throw new IllegalArgumentException("ERROR: Expected a )\n");
			}
			(token) = intexpr_gettok(expr);
			break;
		default:
			throw new IllegalArgumentException("U2:ERROR: Expected a ID, Number, or a (\n");
		}
	}

	public void intexpr_T(String expr) {
		switch (token) {
		case WORD:
		case '(':
			intexpr_U(expr);
			break;
		case '-':
			(token) = intexpr_gettok(expr);
			intexpr_U(expr);
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
			throw new IllegalArgumentException("T:ERROR: Expected a ID, Number, (, or -\n");
		}
	}

	public void intexpr_C(String expr) {
		newresult = new ExprTree(this);
		switch (token) {
		case '*':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_T(expr);
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
			intexpr_C(expr);
			break;
		case '^':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_T(expr);
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
			intexpr_C(expr);
			break;
		case '/':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_T(expr);
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
			intexpr_C(expr);
			break;
		case '%':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_T(expr);
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
			intexpr_C(expr);
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
			newresult.intexpr_T(expr);
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
			intexpr_C(expr);
			break;

		default:
			throw new IllegalArgumentException("ERROR: Expected a * or /\n");
		}
	}

	public void intexpr_B(String expr) {
		newresult = new ExprTree(this);
		switch (token) {
		case '+':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_S(expr);
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
			intexpr_B(expr);
			break;
		case '-':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_S(expr);
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
			intexpr_B(expr);
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
			throw new IllegalArgumentException("ERROR: Expected a + or -\n");
		}
	}

	public void intexpr_S(String expr) {
		switch (token) {
		case WORD:
		case '(':
		case '-':
			intexpr_T(expr);
			intexpr_C(expr);
			break;
		default:
			throw new IllegalArgumentException("S:ERROR: Expected a ID, Number, (, or -\n");
		}
	}

	public void intexpr_R(String expr) {
		switch (token) {
		case WORD:
		case '(':
		case '-':
			intexpr_S(expr);
			intexpr_B(expr);
			break;
		default:
			throw new IllegalArgumentException("R:ERROR: Expected a ID, Number, (, or -\n");
		}
	}

	public void intexpr_P(String expr) {
		newresult = new ExprTree(this);
		//int spos, i;
		String ineq = "";
		String comp;
		switch (token) {
		case '=':
			//spos = position;
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_R(expr);
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
//					comp += "=";
////					int paren = 0;
////					for (i = spos; i < position; i++) {
////						if (expr.charAt(i) == '(')
////							paren++;
////						if (expr.charAt(i) == ')')
////							paren--;
////						ineq = ineq + expr.charAt(i);
////					}
//					comp += ineq;
					if (booleanSignals.contains(comp)) {
						this.isit = 'b';
						this.variable = comp;
						this.lvalue = 0;
						this.uvalue = 1;
						return;
					}
					booleanSignals.add(comp);
					this.isit = 'b';
					this.variable = comp;
					this.lvalue = 0;
					this.uvalue = 1;
					return;
				}
				setNodeValues((this), newresult, "==", 'r');
			}
			break;
		case '>':
			//spos = position;
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			if ((token) == '=') {
				//spos = position;
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
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
				newresult.intexpr_R(expr);
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
			//spos = position;
			(token) = intexpr_gettok(expr);
			if ((token) == '=') {
				//spos = position;
				(token) = intexpr_gettok(expr);
				newresult.token = token;
				newresult.tokvalue = tokvalue;
				newresult.position = position;
				newresult.intexpr_R(expr);
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
				newresult.intexpr_R(expr);
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
			newresult.intexpr_R(expr);
			token = newresult.token;
			tokvalue = newresult.tokvalue;
			position = newresult.position;
			if ((token) != ']') {
				throw new IllegalArgumentException("ERROR: Expected a ]\n");
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
			throw new IllegalArgumentException("ERROR: Expected a [, =, <, or >\n");
		}
	}

	public void intexpr_O(String expr) {
		switch (token) {
		case WORD:
		case '(':
		case '-':
			intexpr_R(expr);
			intexpr_P(expr);
			break;
		default:
			throw new IllegalArgumentException("O:ERROR: Expected a ID, Number, or a (\n");
		}
	}

	public void intexpr_N(String expr) {
		switch (token) {
		case WORD:
		case '-':
		case '(':
			intexpr_O(expr);
			break;
		case '~':
			(token) = intexpr_gettok(expr);
			intexpr_O(expr);
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
			throw new IllegalArgumentException("N:ERROR: Expected a ID, Number, (, or -\n");
		}
	}

	public void intexpr_E(String expr) {
		newresult = new ExprTree(this);
		switch (token) {
		case '&':
			token = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_N(expr);
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
			intexpr_E(expr);
			break;
		case '|':
		case ')':
		case IMPLIES:
		case END_OF_STRING:
			break;
		default:
			throw new IllegalArgumentException(String.format("ERROR(%c): Expected an &\n", (token)));
		}
	}

	public void intexpr_D(String expr) {
		newresult = new ExprTree(this);
		switch (token) {
		case '|':
			(token) = intexpr_gettok(expr);
			newresult.token = token;
			newresult.tokvalue = tokvalue;
			newresult.position = position;
			newresult.intexpr_M(expr);
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
			intexpr_D(expr);
			break;
		case ')':
		case END_OF_STRING:
			break;
		case IMPLIES:
			(token) = intexpr_gettok(expr);
			intexpr_M(expr);
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
			intexpr_D(expr);
			break;
		default:
			throw new IllegalArgumentException("ERROR: Expected an | or ->\n");
		}
	}

	public void intexpr_M(String expr) {
		switch (token) {
		case WORD:
		case '(':
		case '~':
		case '-':
			intexpr_N(expr);
			intexpr_E(expr);
			break;
		default:
			throw new IllegalArgumentException("M: ERROR: Expected a ID, Number, (, or -\n");
		}
	}

	public void intexpr_L(String expr) {
		switch (token) {
		case WORD:
		case '(':
		case '~':
		case '-':
			intexpr_M(expr);
			intexpr_D(expr);
			break;
		default:
			throw new IllegalArgumentException("L:ERROR: Expected a ID, Number, (, or -\n");
		}
	}

	@Override
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
	
	/**
	 * Returns a list of the continuous variable's names that are
	 * contained in this ExprTree.
	 * @return
	 * 		The list of name of the continuous variables in this
	 * 		ExprTree.
	 */
	public ArrayList<String> getContVars() {
		ArrayList<String> vars = new ArrayList<String>();
		switch (isit) {
		//case 'b': // Boolean
		//case 'i': // Integer
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

	/**
	 * This method will return true if the 
	 * expression tree contains a continuous variable.
	 * This is difference from containsCont() which
	 * will return true if there is an integer,
	 * relational, arithmetic or number.
	 * @return
	 * 	True if this ExprTree continuous a 
	 * 	continuous variable.
	 */
	public boolean containsExactlyCont() {
		switch (isit) {
		// These are leaf nodes that we are not looking for.
		case 'b': // Boolean
		case 't': // Truth value
		case 'i': // Integer
		case 'n': // Number
			return false;
		// This is what we are looking for.	
		case 'c': // Continuous
			return true;		
		// The subexpression may contain a continuous variable
		// so need to check further.
		case 'a': // Arithmetic
		case 'r': // Relational
		case 'l': // Logical
		case 'w': // bitWise
			boolean r1cont = false,
			r2cont = false;
			if (r1 != null)
				r1cont = r1.containsExactlyCont();
			if (r2 != null)
				r2cont = r2.containsExactlyCont();
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
			break;
		case 't': // Truth value
		case 'n': // Number
			break;
		}
	}

	public char getChange(HashMap<String, String> variables) {
		switch (isit) {
		case 'b': // Boolean
			if (variables.containsKey(variable)) {
				if (variables.get(variable).toString().toLowerCase().equals("false"))
					return 'F';
				if (variables.get(variable).toString().toLowerCase().equals("true"))
					return 'T';
				return 'X';
			}
			return 'U';
		case 't': // Truth value
			/*
			if (uvalue == 0)
				return 'F';
			else if (lvalue == 1)
				return 'T';
				*/
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
					}
					return 't';
				} else if (r2.getChange(variables) == 't') {
					if (r1.getChange(variables) == 'f') {
						return 'X';
					}
					return 't';
				} else if (r1.getChange(variables) == 'f'
						|| r2.getChange(variables) == 'f') {
					return 'f';
				} else if (r1.getChange(variables) == 'F') {
					if (r2.getChange(variables) == 'F') {
						return 'F';
					}
					return 'f';
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
					}
					return 'f';
				} else if (r2.getChange(variables) == 'f') {
					if (r1.getChange(variables) == 't') {
						return 'X';
					}
					return 'f';
				} else if (r1.getChange(variables) == 't'
						|| r2.getChange(variables) == 't') {
					return 't';
				} else if (r1.getChange(variables) == 'T') {
					if (r2.getChange(variables) == 'T') {
						return 'T';
					}
					return 't';
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
					}
					return 't';
				} else if (r2.getChange(variables) == 'f') {
					if (r1.getChange(variables) == 'f') {
						return 'X';
					}
					return 't';
				} else if (r1.getChange(variables) == 'f') {
					return 'f';
				} else if (r2.getChange(variables) == 't') {
					return 'f';
				} else if (r1.getChange(variables) == 'F') {
					if (r2.getChange(variables) == 'T') {
						return 'F';
					}
					return 'f';
				} else if (r2.getChange(variables) == 'T') {
					return 'f';
				}
				return 'U';
			}
			break;
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
				try {
					if (Integer.parseInt(variables.get(variable)) == 0.0) {
						return 'F';
					}
					return 'T';
				} catch (Exception e) {
					return 'X';
				}
			}
			return 'U';
		case 'c': // Continuous
			return 'X';
		case 'n': // Number
			if (uvalue == 0.0 && lvalue == 0.0) {
				return 'F';
			}
			return 'T';
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
			break;
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
			break;
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
			}
			return true;
		case 'i': // Integer
			if (variables.containsKey(variable)) {
				if (Integer.parseInt(variables.get(variable)) == 0.0) {
					return true;
				}
				return false;
			}
			return false;
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
			}
			return true;
		case 'n': // Number
			if (uvalue == 0.0 && lvalue == 0.0) {
				return true;
			}
			return false;
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
				}
				return false;
			}
			break;
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
			break;
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
			}
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
			if (!(evaluateExpr(variables) != 0.0)) {
				return false;
			}
			return true;
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
				if (sbmlFlag)
					result = "TRUE";
				else
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
			}
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
					if (type.equals("prism")) result = "pow(" + result + ",";
					else result = result + "^";
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
					if (type.equals("prism")) result = result + ")";
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
				}
				return 0.0;
			}
			return Double.NaN;
		case 'c': // Continuous
			return Double.NaN;
		case 'i': // Integer
			if (variables != null) {
				try {
					return Double.parseDouble(variables.get(variable));
				} catch (Exception e) {
					return Double.NaN;
				}
			}
			return Double.NaN;
		case 'n': // Number
			if (uvalue == lvalue) {
				return uvalue;
			}
			return ((uvalue - lvalue) * new java.util.Random().nextDouble())
					+ lvalue;
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
			break;
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
			}
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
				return Double.parseDouble(variables.get(r1.variable+"_" + GlobalConstants.RATE));
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
	
	@Override
	public ExprTree clone(){
		
		ExprTree ET = new ExprTree(); // ET phone home.
		
		ET.op = op;

		ET.isit = isit;

		ET.lvalue = lvalue;
		
		ET.uvalue = uvalue;
		
		ET.variable = variable;

		ET.real = real;

		ET.logical = logical;

		ET.r1 = r1 != null ? r1.clone() : null;
		
		ET.r2 = r2 != null ? r2.clone() : null;
		
		ET.tokvalue = tokvalue;

		ET.position = position;

		ET.token = token;

		ET.newresult = newresult != null ? newresult.clone() : null;

		//private ArrayList<String> booleanSignals, integerSignals, continuousSignals;

		ET.booleanSignals = (ArrayList<String>) booleanSignals.clone();
		
		ET.integerSignals = (ArrayList<String>) integerSignals.clone();
		
		ET.continuousSignals = continuousSignals;
		
		ET.lhpn = lhpn;
		
		ET.expression = expression;
		
		return ET;
	}

	/*
	 * Performs the same operation as this.clone except it does not copy the parsing information.
	 */
	public ExprTree shallowclone(){
		
		ExprTree ET = new ExprTree(); // ET phone home.
		
		ET.op = op;

		ET.isit = isit;

		ET.lvalue = lvalue;
		
		ET.uvalue = uvalue;
		
		ET.variable = variable;

		ET.real = real;

		ET.logical = logical;

		ET.r1 = r1 != null ? r1.shallowclone() : null;
		
		ET.r2 = r2 != null ? r2.shallowclone() : null;
		
		//private ArrayList<String> booleanSignals, integerSignals, continuousSignals;

		ET.booleanSignals = (ArrayList<String>) booleanSignals.clone();
		
		ET.integerSignals = (ArrayList<String>) integerSignals.clone();
		
		ET.continuousSignals = continuousSignals;
		
		ET.lhpn = lhpn;
		
		ET.expression = expression;
		
		return ET;
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
	
	/**
	 * Evaluates an expression tree involving ranges.
	 * Note: if no continuous variables are present, both z and continuousValues can be null.
	 * If continuous variables are present, then at least one must be non-null. Finally, if
	 * z is non-null, then the values will be taken from z and continuousValues will not be consulted.
	 * @param variables
	 * 			The values of the variables.
	 * @param z
	 * 			The zone containing the continuous variables.
	 * @param continuousValues
	 * 			The continuous variables along with their values.
	 * @return
	 * 			The range of values for the expression tree.
	 */
	public IntervalPair evaluateExprBound(HashMap<String, String> variables, Equivalence z, 
//			HashMap<LPNContinuousPair, IntervalPair> continuousValues){
			HashMap<LPNContAndRate, IntervalPair> continuousValues){
		
		
		/*
		 * The code for this method was modified from atacs/src/lhpnrsg.c.
		 */
		
//		void exprsn::eval(lhpnStateADT cur_state,int nevents){
//			  char log_val;
//			  int tl1,tl2,tu1,tu2,i,j,k;
//			  int preciser = 1;
//

		int lBound, uBound;
		
		// If lBound and uBound are never set, then return "don't know".
		lBound = -INFIN;
		uBound = INFIN;
		
		IntervalPair r1Range,r2Range = null;
		
		if(!op.equals("")){
//			  if (op!=""){
//			    //printf("%s, eval left child\n",op.c_str());
//			    r1->eval(cur_state,nevents);
//			    if ((r1->lvalue == -INFIN)||(r1->uvalue == INFIN)){
//			      lvalue = -INFIN;
//			      uvalue = INFIN;
//			      return;
//			    }
			r1Range = r1.evaluateExprBound(variables, z, continuousValues);
			if ((r1Range.get_LowerBound() == -INFIN) || (r1Range.get_UpperBound() == INFIN)){
				
				return new IntervalPair(-INFIN, INFIN);
			}
			
//			    if (r2){
//			      //printf("eval right child\n");
//			      r2->eval(cur_state,nevents);
//			      if ((r2->lvalue == -INFIN)||(r2->uvalue == INFIN)){
//			      lvalue = -INFIN;
//			      uvalue = INFIN;
//			      return;
//			      }
//		        }
			if(r2 != null){
				r2Range = r2.evaluateExprBound(variables, z, continuousValues);
				if ((r2Range.get_LowerBound() == - INFIN) || (r1Range.get_UpperBound() == INFIN)){
					return new IntervalPair(-INFIN, INFIN);
				}
			} else {
				r2Range = new IntervalPair(-INFIN, INFIN);
			}
			
//			    if (op=="||"){
//			      // logical OR
//			      if (r1->logical){
//				tl1 = r1->lvalue;
//				tu1 = r1->uvalue;
//			      }
			
			if( op.equals("||")){
				Boolean tl1, tu1, tl2, tu2;
				// logical OR
				if(r1.logical){
					tl1 = r1Range.get_LowerBound() != 0; // false if value is zero and true otherwise
					tu1 = r1Range.get_UpperBound() != 0; // false if value is zero and true otherwise
				}
			
				else{ // convert numeric r1 to boolean
//			      else{//convert numeric r1 to boolean
//				if ((r1->lvalue == 0)&&(r1->uvalue == 0)){//false
//				  tl1 = tu1 = 0;
//				}
				
					if((r1Range.get_LowerBound() == 0) && (r1Range.get_UpperBound() == 0)){ // false
						tl1 = tu1 = false;
					}
				
//				else if ((r1->lvalue < 0)&&(r1->uvalue < 0)||
//					 (r1->lvalue > 0)&&(r1->uvalue > 0)){//true
//				  tl1 = tu1 = 1;
//				}
					else if (((r1Range.get_LowerBound() < 0) && (r1Range.get_UpperBound() < 0)) ||
							((r1Range.get_LowerBound() > 0) && (r1Range.get_UpperBound() > 0))){ // true
						tl1 = tu1 = true;
					}
				
//				else{
//				  tl1 = 0;
//				  tu1 = 1;
//				}
//			      }
				
					else{
						tl1 = false;
						tu1 = true;
					}
				}
			
//			      if (r2->logical){
//				tl2 = r2->lvalue;
//				tu2 = r2->uvalue;
//			      }
			
				if(r2.logical){ // Note : r2Range can only be set if r2 was non-null.
					tl2 = r2Range.get_LowerBound() != 0; // False if value is zero and true otherwise.
					tu2 = r2Range.get_UpperBound() != 0; // False if value is zero and true otherwise.
				}
				else{// convert numeric r2 to boolean
//			      else{//convert numeric r2 to boolean
//				if ((r2->lvalue == 0)&&(r2->uvalue == 0)){//false
//				  tl2 = tu2 = 0;
//				}
					if((r2Range.get_LowerBound() == 0) && (r2Range.get_UpperBound() == 0)){// false
						tl2 = tu2 = false;
					}
					
//				else if ((r2->lvalue < 0)&&(r2->uvalue < 0)||
//					 (r2->lvalue > 0)&&(r2->uvalue > 0)){//true
//				  tl2 = tu2 = 1;
//				}
					else if (((r2Range.get_LowerBound() < 0) && (r2Range.get_UpperBound() < 0)) ||
							((r2Range.get_LowerBound() > 0) && (r2Range.get_UpperBound() > 0))){ // true
						tl2 = tu2 = true;
					}
//				else{
//				  tl2 = 0;
//				  tu2 = 1;
//				}
//			      }
					else{
						tl2 = false;
						tu2 = true;
					}
				}
//			      lvalue = tl1 || tl2;
//			      uvalue = tu1 || tu2;
				
//				lBound = tl1 || lt2;
//				uBound = tu1 || tu2;
				
				lBound = (tl1 || tl2) ? 1 : 0; // Poor man casting from boolean to int.
				uBound = (tu1 || tu2) ? 1 : 0;
				
			}
//			    }else if(op=="&&"){
//			      // logical AND
			
			else if (op.equals("&&")){ // logical AND
				Boolean tl1, tu1, tl2, tu2;
//			      if (r1->logical){
//				tl1 = r1->lvalue;
//				tu1 = r1->uvalue;
//			      }
				if(r1.logical){
					tl1 = r1Range.get_LowerBound() != 0; // false if value is zero and true otherwise
					tu1 = r1Range.get_UpperBound() != 0; // false if value is zero and true otherwise
				}
				
//			      else{//convert numeric r1 to boolean
//				if ((r1->lvalue == 0)&&(r1->uvalue == 0)){//false
//				  tl1 = tu1 = 0;
//				}
				
				else{ // convert number r1 to boolean
					if((r1Range.get_LowerBound() == 0) && (r1Range.get_UpperBound() == 0)){ // false
						tl1 = tu1 = false;
					}
				
				
//				else if ((r1->lvalue < 0)&&(r1->uvalue < 0)||
//					 (r1->lvalue > 0)&&(r1->uvalue > 0)){//true
//				  tl1 = tu1 = 1;
//				}
				
					else if (((r1Range.get_LowerBound() < 0) && (r1Range.get_UpperBound() < 0)) ||
							((r1Range.get_LowerBound() > 0) && (r1Range.get_UpperBound() > 0))){ // true
						tl1 = tu1 = true;
					}
				
//				else{
//				  tl1 = 0;
//				  tu1 = 1;
//				}
//			      }
				
					else{
						tl1 = false;
						tu1 = true;
					}
				}
//			      if (r2->logical){
//				tl2 = r2->lvalue;
//				tu2 = r2->uvalue;
//			      }
//			      else{//convert numeric r2 to boolean
				if(r2.logical){ // Note : r2Range can only be set if r2 was non-null.
					tl2 = r2Range.get_LowerBound() != 0; // False if value is zero and true otherwise.
					tu2 = r2Range.get_UpperBound() != 0; // False if value is zero and true otherwise.
				}
				else{// convert numeric r2 to boolean
				
//				if ((r2->lvalue == 0)&&(r2->uvalue == 0)){//false
//				  tl2 = tu2 = 0;
//				}
					
					if((r2Range.get_LowerBound() == 0) && (r2Range.get_UpperBound() == 0)){// false
						tl2 = tu2 = false;
					}
//				else if ((r2->lvalue < 0)&&(r2->uvalue < 0)||
//					 (r2->lvalue > 0)&&(r2->uvalue > 0)){//true
//				  tl2 = tu2 = 1;
//				}
					
					else if (((r2Range.get_LowerBound() < 0) && (r2Range.get_UpperBound() < 0)) ||
							((r2Range.get_LowerBound() > 0) && (r2Range.get_UpperBound() > 0))){ // true
						tl2 = tu2 = true;
					}
					
//				else{
//				  tl2 = 0;
//				  tu2 = 1;
//				}
//			      }
					else{
						tl2 = false;
						tu2 = true;
					}
				}
//			      lvalue = tl1 && tl2;
//			      uvalue = tu1 && tu2;
				
				lBound = (tl1 && tl2) ? 1 : 0; // Poor man casting from boolean to int.
				uBound = (tu1 && tu2) ? 1 : 0; // Or clever way; depends on how you look at it.
//			#ifdef __LHPN_EVAL__
//			      printf("and: [%d,%d](%c)&[%d,%d](%c) = [%d,%d]\n",r1->lvalue,
//				     r1->uvalue,r1->isit,r2->lvalue,r2->uvalue,r2->isit,lvalue,uvalue);
//			#endif
				
			}
//			    }else if(op=="->"){
//			      // implication operator
			else if(op.equals("->")){ // Implication operator.
				Boolean tl1, tu1, tl2, tu2;
//			      if (r1->logical){
//				tl1 = r1->lvalue;
//				tu1 = r1->uvalue;
//			      }
//			      else{//convert numeric r1 to boolean
//				if ((r1->lvalue == 0)&&(r1->uvalue == 0)){//false
//				  tl1 = tu1 = 0;
//				}
//				else if ((r1->lvalue < 0)&&(r1->uvalue < 0)||
//					 (r1->lvalue > 0)&&(r1->uvalue > 0)){//true
//				  tl1 = tu1 = 1;
//				}
//				else{
//				  tl1 = 0;
//				  tu1 = 1;
//				}
//			      }
				
				BooleanPair lowerBounds = logicalConversion(r1, r1Range);
				tl1 = lowerBounds.get_lower();
				tu1 = lowerBounds.get_upper();
//			      if (r2->logical){
//				tl2 = r2->lvalue;
//				tu2 = r2->uvalue;
//			      }
//			      else{//convert numeric r2 to boolean
//				if ((r2->lvalue == 0)&&(r2->uvalue == 0)){//false
//				  tl2 = tu2 = 0;
//				}
//				else if ((r2->lvalue < 0)&&(r2->uvalue < 0)||
//					 (r2->lvalue > 0)&&(r2->uvalue > 0)){//true
//				  tl2 = tu2 = 1;
//				}
//				else{
//				  tl2 = 0;
//				  tu2 = 1;
//				}
//			      }
//			      lvalue = tl1 || !tl2;
//			      uvalue = tu1 || !tu2;
				BooleanPair upperBounds = logicalConversion(r2, r2Range);
				tl2 = upperBounds.get_lower();
				tu2 = upperBounds.get_upper();
				

				lBound = (tl1 || !tl2) ? 1 : 0; // Poor man casting from boolean to int.
				uBound = (tu1 || !tu2) ? 1 : 0; // Or clever way; depends on how you look at it.
			}
//			    }else if(op=="!"){
//			      // logical NOT
			else if(op.equals("!")){
				Boolean tl1, tu1;
				
				BooleanPair bounds = logicalConversion(r1, r1Range);
				tl1 = bounds.get_lower();
				tu1 = bounds.get_upper();
//			      if (r1->logical){
//				tl1 = r1->lvalue;
//				tu1 = r1->uvalue;
//			      }
//			      else{//convert numeric r1 to boolean
//				if ((r1->lvalue == 0)&&(r1->uvalue == 0)){//false
//				  tl1 = tu1 = 0;
//				}
//				else if ((r1->lvalue < 0)&&(r1->uvalue < 0)||
//					 (r1->lvalue > 0)&&(r1->uvalue > 0)){//true
//				  tl1 = tu1 = 1;
//				}
//				else{
//				  tl1 = 0;
//				  tu1 = 1;
//				}
//			      }
//			      if (tl1 == tu1){
//				lvalue = 1- tl1;
//				uvalue = 1- tl1;
//			      }
				if(tl1 == tu1){
					lBound = !tl1 ? 1 : 0;
					uBound = !tl1 ? 1 : 0;
				}
				
//			#ifdef __LHPN_EVAL__
//			      printf("not: [%d,%d](%c) = [%d,%d]\n",r1->lvalue,
//				     r1->uvalue,r1->isit,lvalue,uvalue);
//			#endif
//			      //printf("negation: ~[%d,%d] = [%d,%d]\n",r1->lvalue,r1->uvalue,
//			      // lvalue,uvalue);
				
			}
//			    }else if(op=="=="){
//			      // "equality" operator
			else if (op.equals("==")){ //"equality" operator.
//			      // true if same point value
//			      if ((r1->lvalue == r1->uvalue) && (r2->lvalue == r2->uvalue) &&
//				  (r1->lvalue == r2->uvalue))
//				lvalue = uvalue = 1;
				
				// true if same point value.
				if((r1Range.get_LowerBound() == r1Range.get_UpperBound()) &&
						(r2Range.get_LowerBound() == r2Range.get_UpperBound()) && 
						(r1Range.get_LowerBound() == r2Range.get_UpperBound())){
					lBound = uBound = 1;
				}
				
//			      // false if no overlap
//			      else if ((r1->lvalue > r2->uvalue)||(r2->lvalue > r1->uvalue))
//				lvalue = uvalue = 0;
				
				// false if no overlap
				else if ((r1Range.get_LowerBound() > r2Range.get_UpperBound()) ||
						(r2Range.get_LowerBound() > r1Range.get_UpperBound())){
					lBound = uBound = 0;
				}
				
//			      // maybe if overlap
//			      else{
//				lvalue = 0;
//				uvalue = 1;
//			      }
				
				// maybe if overlap
				else{
					lBound = 0;
					uBound = 1;
				}
				
//			#ifdef __LHPN_EVAL__
//			      printf("[%d,%d]==[%d,%d]=[%d,%d]\n",r1->lvalue,r1->uvalue ,r2->lvalue,r2->uvalue,lvalue,uvalue);  
//			#endif   
			}
			else if(op.equals(">")){// "greater than" operator
//			    }else if(op==">"){
//			      // "greater than" operator
//			      //true if lower1 > upper2
//			      if (r1->lvalue > r2->uvalue)
//				lvalue = uvalue = 1;
				
				// true if lower1 > upper2
				if( r1Range.get_LowerBound() > r2Range.get_UpperBound()){
					lBound = uBound = 1;
				}
				
//			      //false if lower2 >= upper1
//			      else if (r2->lvalue >= r1->uvalue)
//				lvalue = uvalue = 0;
				
				// false if lower 2 >= upper1
				else if (r2Range.get_LowerBound() >= r1Range.get_UpperBound()){
					lBound = uBound = 0;
				}
				
//			      // maybe if overlap
//			      else{
//				lvalue = 0;
//				uvalue = 1;
//			      }
				
				// maybe, if overlap
				else {
					lBound = 0;
					uBound = 1;
				}
			}
//			    }else if(op==">="){
//			      // "greater than or equal" operator
			else if (op.equals(">=")){
//			      //true if lower1 >= upper2
//			      if (r1->lvalue >= r2->uvalue)
//				lvalue = uvalue = 1;
					
					// true if lower1 >= upper2
				if(r1Range.get_LowerBound() >= r2Range.get_UpperBound()){
					lBound = uBound = 1;
				}
					
//			      //false if lower2 > upper1
//			      else if (r2->lvalue > r1->uvalue)
//				lvalue = uvalue = 0;

				// false if lower2 > upper1
				else if (r2Range.get_LowerBound() > r1Range.get_UpperBound()){
					lBound = uBound = 0;
				}
					
//			      // maybe if overlap
//			      else{
//				lvalue = 0;
//				uvalue = 1;
//			      }
					
					// maybe if overlap
				else {
					lBound = 0;
					uBound = 1;
				}
			}
//			    }else if(op=="<"){
//			      // "less than" operator

			else if (op.equals("<")){// "less than" operator.
//			      //true if lower2 > upper1
//			      if (r2->lvalue > r1->uvalue)
//				lvalue = uvalue = 1;
				
				// true if lower2 > upper1
				if(r2Range.get_LowerBound() > r1Range.get_UpperBound()){
					lBound = uBound = 1;
				}
//			      //false if lower1 >= upper2
//			      else if (r1->lvalue >= r2->uvalue)
//				lvalue = uvalue = 0;
				
				// false if lower1 >= upper2
				else if (r1Range.get_LowerBound() >= r2Range.get_UpperBound()){
					lBound = uBound = 0;
				}
				
//			      // maybe if overlap
//			      else{
//				lvalue = 0;
//				uvalue = 1;
//			      }
				
				// maybe if overlap
				else{
					lBound = 0;
					uBound = 1;
				}
			}
//			    }else if(op=="<="){
//			      // "less than or equal" operator
				
			else if (op.equals("<=")){// "less than or equal" operator
//			      //true if lower2 >= upper1
//			      if (r2->lvalue >= r1->uvalue)
//				lvalue = uvalue = 1;
				
				// true if lower2 >= upper1
				if(r2Range.get_LowerBound() >= r1Range.get_UpperBound()){
					lBound = uBound = 1;
				}
				
//			      //false if lower1 > upper2
//			      else if (r1->lvalue > r2->uvalue)
//				lvalue = uvalue = 0;
				
				// false if lower1 > upper2
				else if (r1Range.get_LowerBound() > r2Range.get_UpperBound()){
					lBound = uBound =0;
				}
				
//			      // maybe if overlap
//			      else{
//				lvalue = 0;
//				uvalue = 1;
//			      }
				
				// maybe if overlap
				else {
					lBound = 0;
					uBound = 1;
				}
				
//			#ifdef __LHPN_EVAL__
//			      printf("[%d,%d]<=[%d,%d]=[%d,%d]\n",r1->lvalue,r1->uvalue ,r2->lvalue,r2->uvalue,lvalue,uvalue);  
//			#endif   
			}
//			    }else if(op=="[]"){//NEEDS WORK
//			      // bit extraction operator

			else if (op.equals("[]")){ // Apparently needs work.
//			      // Only extract if both are point values.  
//			      if ((r1->lvalue == r1->uvalue)&&(r2->lvalue == r2->uvalue)){
//				lvalue = uvalue = (r1->lvalue >> r2->uvalue) & 1;
//			      }
				if( (r1Range.get_LowerBound() == r1Range.get_UpperBound()) && 
						(r2Range.get_LowerBound() == r2Range.get_UpperBound())){
					lBound = uBound = 
							(r1Range.get_LowerBound() >> r2Range.get_UpperBound()) & 1;
				}
//			      else {
//				if (!preciser)
//				  {
//				    lvalue = 0;
//				    uvalue = 1;
//				  }
//				else {
//				  uvalue = 0;
//				  lvalue = 1;
//				  for (i = r1->lvalue;i<=r1->uvalue;i++)
//				    for (j = r2->lvalue;j<=r2->uvalue;j++){
//				      k = (i >> j) & 1;
//				      lvalue &= k;
//				      uvalue |= k;
//				      if (lvalue < uvalue)
//					return;
//				    }
//				}
//			      }
				
				else{
					// Not doing the !preciser part.
					uBound = 0;
					lBound = 1;
					for (int i = r1Range.get_LowerBound(); i<r1Range.get_UpperBound();
							i++){
						for (int j = r2Range.get_LowerBound();
								j<r2Range.get_UpperBound(); j++){
							int k = (i >> j) & 1;
							lBound &= k;
							uBound |= k;
							if(lBound < uBound){
								return new IntervalPair(lBound, uBound);
							}
						}
					}
						
				}
				
			}
//			    }else if(op=="+"){
//			      lvalue = r1->lvalue + r2->lvalue;
//			      uvalue = r1->uvalue + r2->uvalue;
			
			else if (op.equals("+")){
				lBound = r1Range.get_LowerBound() + r2Range.get_LowerBound();
				uBound = r1Range.get_UpperBound() + r2Range.get_UpperBound();
			}
			
//			    }else if(op=="-"){
//			      lvalue = r1->lvalue - r2->uvalue;
//			      uvalue = r1->uvalue - r2->lvalue;
			
			else if (op.equals("-")){
				lBound = r1Range.get_LowerBound() - r2Range.get_LowerBound();
				uBound = r1Range.get_UpperBound() - r2Range.get_UpperBound();
			}
			
//			    }else if(op=="*"){
//			      tl1 = r1->lvalue * r2->lvalue;
//			      tl2 = r1->uvalue * r2->uvalue;
//			      tu1 = r1->lvalue * r2->uvalue;
//			      tu2 = r1->uvalue * r2->lvalue;
//			      lvalue = min(min(min(tl1,tl2),tu1),tu2);
//			      uvalue = max(max(max(tl1,tl2),tu1),tu2);
			
			else if (op.equals("*")){
				int tl1, tl2, tu1, tu2;
				tl1 = r1Range.get_LowerBound() * r2Range.get_LowerBound();
				tl2 = r1Range.get_UpperBound() * r2Range.get_UpperBound();
				tu1 = r1Range.get_LowerBound() * r2Range.get_UpperBound();
				tu2 = r1Range.get_UpperBound() * r2Range.get_LowerBound();
				lBound = Math.min(Math.min(Math.min(tl1, tl2), tu1), tu2);
				uBound = Math.max(Math.max(Math.max(tl1, tl2), tu1), tu2);
			}
			
//			    }else if(op=="^"){
//			      tl1 = pow((double)r1->lvalue,(double)r2->lvalue);
//			      tl2 = pow((double)r1->uvalue,(double)r2->uvalue);
//			      tu1 = pow((double)r1->lvalue,(double)r2->uvalue);
//			      tu2 = pow((double)r1->uvalue,(double)r2->lvalue);
//			      lvalue = min(min(min(tl1,tl2),tu1),tu2);
//			      uvalue = max(max(max(tl1,tl2),tu1),tu2);
			
			else if (op.equals("^")){
				double tl1, tl2, tu1, tu2;
				tl1 = Math.pow(r1Range.get_LowerBound(), r2Range.get_LowerBound());
				tl2 = Math.pow(r1Range.get_UpperBound(), r2Range.get_UpperBound());
				tu1 = Math.pow(r1Range.get_LowerBound(), r2Range.get_UpperBound());
				tu2 = Math.pow(r1Range.get_UpperBound(), r2Range.get_LowerBound());
				lBound = (int) Math.min(Math.min(Math.min(tl1, tl2), tu1), tu2);
				uBound = (int) Math.max(Math.max(Math.max(tl1, tl2), tu1), tu2);
			}
			
//			    }else if(op=="u"){
//			      lvalue = r1->lvalue;
//			      uvalue = r2->uvalue;

			else if (op.equals("uniform")){
				lBound = r1Range.get_LowerBound();
				uBound = r2Range.get_UpperBound();
			}
			else if (op.equals("rate")){
				LPNContinuousPair lcPair = new LPNContinuousPair(r1.lhpn.getLpnIndex(),
						lhpn.getContVarIndex(r1.variable));
				lBound = z.getCurrentRate(lcPair);
				uBound = z.getCurrentRate(lcPair);
			}
//		    }else if(op=="/"){			
//			      //ropughly integer division.  
//			      //DON"T KNOW WHAT FLOATING POINT PART IS!!!!!
//			      tl1 = floor(r1->lvalue / r2->lvalue);
//			      tl2 = floor(r1->uvalue / r2->uvalue);
//			      tu1 = floor(r1->lvalue / r2->uvalue);
//			      tu2 = floor(r1->uvalue / r2->lvalue);
//			      lvalue = min(min(min(tl1,tl2),tu1),tu2);
//			      tl1 = ceil(r1->lvalue / r2->lvalue);
//			      tl2 = ceil(r1->uvalue / r2->uvalue);
//			      tu1 = ceil(r1->lvalue / r2->uvalue);
//			      tu2 = ceil(r1->uvalue / r2->lvalue);
//			      uvalue = max(max(max(tl1,tl2),tu1),tu2);
			
			else if (op.equals("/")){ // roughly integer division.
				// STILL DON'T KNOW WHAT FLOATING POINT PART IS !!!! :) !!!!
				double tl1, tl2, tu1, tu2;
				tl1 = Math.floor(((double)r1Range.get_LowerBound()) / r2Range.get_LowerBound());
				tl2 = Math.floor(((double)r1Range.get_UpperBound()) / r2Range.get_UpperBound());
				tu1 = Math.floor(((double)r1Range.get_LowerBound()) / r2Range.get_UpperBound());
				tu2 = Math.floor(((double)r1Range.get_UpperBound()) / r2Range.get_LowerBound());
				lBound = (int) Math.min(Math.min(Math.min(tl1, tl2), tu1), tu2);
				tl1 = Math.ceil(((double)r1Range.get_LowerBound()) / r2Range.get_LowerBound());
				tl2 = Math.ceil(((double)r1Range.get_UpperBound()) / r2Range.get_UpperBound());
				tu1 = Math.ceil(((double)r1Range.get_LowerBound()) / r2Range.get_UpperBound());
				tu2 = Math.ceil(((double)r1Range.get_UpperBound()) / r2Range.get_LowerBound());
				uBound = (int) Math.max(Math.max(Math.max(tl1, tl2), tu1), tu2);
			}
			
//			    }else if(op=="%"){//NEEDS WORK
			
			else if (op.equals("%")){// STILL NEEDS WORK.
			
//			      if (!preciser){
//				// Only calculate if both are point values.  
//				if ((r1->lvalue == r1->uvalue)&&(r2->lvalue == r2->uvalue)){
//				  lvalue = uvalue = r1->lvalue % r2->uvalue;
//				}
//				else{
//				  lvalue = min(0,max(-(max(abs(r2->lvalue),abs(r2->lvalue))-1),r1->lvalue));
//				  uvalue = max(0,min(max(abs(r2->lvalue),abs(r2->uvalue))-1,r1->uvalue));
//				}
//			      }
//			      else{
//				uvalue = -INFIN;
//				lvalue = INFIN;
//				for (i = r1->lvalue;i<=r1->uvalue;i++)
//				  for (j = r2->lvalue;j<=r2->uvalue;j++){
//				    k = i%j;
//				    if (k < lvalue)
//				      lvalue = k;
//				    if (k > uvalue)
//				      uvalue = k;
//				  }
//			      }
			
				// Not doing the !precier part.
				
				lBound = -INFIN;
				uBound = INFIN;
				for (int i = r1Range.get_LowerBound(); i <= r1Range.get_UpperBound(); i++){
					for ( int j = r2Range.get_LowerBound(); j <= r2Range.get_UpperBound(); j++){
						int k = i%j;
						if(k < lBound){
							lBound = k;
						}
						if( k > uBound){
							uBound = k;
						}
					}
				}
				
			}
			
//			    }else if(op=="U-"){
//			      lvalue = -(r1->uvalue);
//			      uvalue = -(r1->lvalue);
			
			else if (op.equals("U-")){
				lBound = -1 * r1Range.get_UpperBound();
				uBound = -1 * (r1Range.get_LowerBound());
			}
			
//			    }else if(op=="INT"){
//			      lvalue = r1->lvalue;
//			      uvalue = r1->uvalue;
			
			else if (op.equals("INT")){
				lBound = r1Range.get_LowerBound();
				uBound = r1Range.get_UpperBound();
			}
			
//			    }else if(op=="BOOL"){
//			      if ((r1->lvalue == 0)&& (r1->uvalue == 0))
//				lvalue = uvalue = 0;
//			      else if (((r1->lvalue > 0) && (r1->uvalue > 0))||
//				       ((r1->lvalue < 0) && (r1->uvalue < 0)))
//				lvalue = uvalue = 1;
//			      else {
//				lvalue = 0;
//				uvalue = 1;
//			      }
			
			else if(op.equals("BOOL")){
				if( (r1Range.get_LowerBound() == 0) && (r1Range.get_UpperBound() == 0)){
					lBound = uBound =0;
				}
				else if ((r1Range.get_LowerBound() > 0) && (r1Range.get_UpperBound() > 0) ||
						(r1Range.get_LowerBound() < 0) && (r1Range.get_UpperBound() < 0)){
					lBound = uBound =1 ;
				}
				else{
					lBound = 0;
					uBound = 1;
				}
			}
			
//			    }else if(op=="&"){
			
			else if(op.equals("&")){
//			      if ((r1->lvalue!=r1->uvalue)||(r2->lvalue!=r2->uvalue)) {
				
				if((r1Range.get_LowerBound() != r1Range.get_UpperBound()) ||
						(r2Range.get_LowerBound() != r2Range.get_UpperBound())){
				
//				if (!preciser){
//				  lvalue = min(r1->lvalue+r2->lvalue,0);
//				  uvalue = max((r1->uvalue),(r2->uvalue));
//				}
//				else{
//				  uvalue = -INFIN;
//				  lvalue = INFIN;
//				  for (i = r1->lvalue;i<=r1->uvalue;i++)
//				    for (j = r2->lvalue;j<=r2->uvalue;j++){
//				      k = i&j;
//				      if (k < lvalue)
//					lvalue = k;
//				      if (k > uvalue)
//					uvalue = k;
//				    }
//				}
//			      }
				
					// Not doing the !preciser part.
					uBound = -INFIN;
					lBound = INFIN;
					for( int i=r1Range.get_LowerBound(); i<=r1Range.get_UpperBound(); i++){
						for(int j=r2Range.get_LowerBound(); j<=r2Range.get_UpperBound(); j++){
							int k = i&j;
							if (k < lBound){
								lBound =k;
							}
							if( k > uBound){
								uBound = k;
							}
						}
					}
					
					
				}
//			      else {
//				lvalue = (r1->lvalue & r2->lvalue);
//				uvalue = (r1->lvalue & r2->lvalue);
//			      }
				
				else {
					lBound = (r1Range.get_LowerBound() & r2Range.get_LowerBound());
					uBound = (r1Range.get_LowerBound() & r2Range.get_LowerBound());
				}
//			#ifdef __LHPN_EVAL__
//			      printf("BITWISE AND: [%d,%d](%c)&[%d,%d](%c) = [%d,%d]\n",r1->lvalue,
//				     r1->uvalue,r1->isit,r2->lvalue,r2->uvalue,r2->isit,lvalue,uvalue);
//			#endif
			}
			
//			    }else if(op=="|"){
			else if (op.equals("|")){
//			      if ((r1->lvalue!=r1->uvalue)||(r2->lvalue!=r2->uvalue)) {
//				lvalue = min(r1->lvalue,r2->lvalue);
//				uvalue = max(r1->uvalue + r2->uvalue,-1);
//			      } else {
//				lvalue = (r1->lvalue | r2->lvalue);
//				uvalue = (r1->lvalue | r2->lvalue);
//			      }
				
				if((r1Range.get_LowerBound() != r1Range.get_UpperBound()) ||
						(r2Range.get_LowerBound() != r2Range.get_UpperBound())){
					
					lBound = Math.min(r1Range.get_LowerBound(), r2Range.get_LowerBound());
					uBound = Math.max(r1Range.get_UpperBound() + r2Range.get_UpperBound(), -1);
				}
				else {
					lBound = (r1Range.get_LowerBound() | r2Range.get_LowerBound());
					uBound = (r1Range.get_LowerBound() | r2Range.get_LowerBound());
				}
				
			}
//			    }else if(op=="m"){
//			      lvalue = min(r1->lvalue,r2->lvalue);
//			      uvalue = min(r1->uvalue,r2->uvalue);
			else if(op.equals("m")){
				lBound = Math.min(r1Range.get_LowerBound(), r2Range.get_LowerBound());
				uBound = Math.min(r1Range.get_UpperBound(), r2Range.get_UpperBound());
			}
			
//			    }else if(op=="M"){
//			      lvalue = max(r1->lvalue,r2->lvalue);
//			      uvalue = max(r1->uvalue,r2->uvalue);
			else if (op.equals("M")){
				lBound = Math.max(r1Range.get_LowerBound(), r2Range.get_LowerBound());
				uBound = Math.max(r1Range.get_UpperBound(), r2Range.get_UpperBound());
				
			}
			
//			    }else if(op=="i"){
//			      tl1 = r1->lvalue / r2->lvalue;
//			      tl2 = r1->uvalue / r2->uvalue;
//			      tu1 = r1->lvalue / r2->uvalue;
//			      tu2 = r1->uvalue / r2->lvalue;
//			      lvalue = min(min(min(tl1,tl2),tu1),tu2);
//			      uvalue = max(max(max(tl1,tl2),tu1),tu2);
			
			else if (op.equals("i")){
				
				int tl1, tl2, tu1, tu2;
				tl1 = r1Range.get_LowerBound() / r2Range.get_LowerBound();
				tl2 = r1Range.get_UpperBound() / r2Range.get_UpperBound();
				tu1 = r1Range.get_LowerBound() / r2Range.get_UpperBound();
				tu2 = r1Range.get_UpperBound() / r2Range.get_LowerBound();
				lBound = Math.min(Math.min(Math.min(tl1, tl2), tu1), tu2);
				uBound = Math.max(Math.max(Math.max(tl1, tl2), tu1), tu2);
			}
			
//			    }else if(op=="X"){
//			      lvalue = min(min(r1->lvalue-r2->uvalue,r2->lvalue-r1->uvalue),0);
//			      uvalue = max(max(r1->uvalue + r2->uvalue,-(r1->lvalue + r2->lvalue)),-1);
			else if(op.equals("X")){
				lBound = Math.min(Math.min(r1Range.get_LowerBound()-r2Range.get_UpperBound(), 
						r2Range.get_LowerBound()-r1Range.get_UpperBound()), 0);
				uBound = Math.max(Math.max(r1Range.get_UpperBound()+r2Range.get_UpperBound(), 
						r2Range.get_LowerBound()+r1Range.get_LowerBound()), -1);
			}
////			     }else if(op=="floor"){
////			       lvalue = floor(r1->lvalue);
////			       uvalue = floor(r1->uvalue);
////			     }else if(op=="round"){
////			       lvalue = round(r1->lvalue);
////			       uvalue = round(r1->uvalue);
////			     }else if(op=="ceil"){
////			       lvalue = ceil(r1->lvalue);
////			       uvalue = ceil(r1->uvalue);
			
			
//			    }else if(op=="~"){
//			      //bitwise negation operator (1's complement)
//			      lvalue = -((r1->uvalue)+1);
//			      uvalue = -((r1->lvalue)+1);
//			     }
			
			else if (op.equals("~")){
				// bitwise negation operator (1's complement)
				lBound = -1*(r1Range.get_LowerBound());
				uBound = -1*(r1Range.get_UpperBound());
				
			}
//			  }else if(isit == 'd'){
//			      for (i = 1;i<cur_state->z->size;i++){
//				if (cur_state->z->curClocks[i].enabled == index){
//				  lvalue = cur_state->r->bound[cur_state->z->curClocks[i].enabled-nevents].lower;
//				  uvalue = cur_state->r->bound[cur_state->z->curClocks[i].enabled-nevents].upper;
//			#ifdef __LHPN_EVAL__
//				  printf("lv=%d,uv=%d,index=%d,i=%d\n",lvalue, uvalue,index,i);
//			#endif
//				  break;
//				}
//			      }
			
			// Not present in the current implementation. These are 'i'.
//			else if (isit == 'd'){
//				//Check what this is before doing it.
//			}
			
//			  }else{
		}
			else{
//			    if ((isit == 'i')||(isit == 'c')){
				
				if(isit == 'i'){
					
//			      for (i = 1;i<cur_state->z->size;i++){
//				if (cur_state->z->curClocks[i].enabled == index){
//				  if (i>=cur_state->z->dbmEnd){
//				    lvalue = -1*(cur_state->z->matrix[0][i]);
//				    uvalue = cur_state->z->matrix[i][0];
//				  }
					
					
					// Get the value of the variable from the passed HashMap and convert it as appropriate.
					String sV = variables.get(variable); // sV for "string value"

					if(sV != null){
						
						int tmp = Integer.parseInt(sV);

						// Currently the platu.state does not support integer ranges.
						lBound = uBound = tmp;
					}
					else{
						lBound = -INFIN;
						uBound = INFIN;
					}

					
//				  else{// uses lower rate bound for both????
//				    lvalue = -1*(cur_state->z->matrix[0][i])*
//				      cur_state->r->bound[cur_state->z->curClocks[i].enabled-nevents].current;
//				    uvalue = cur_state->z->matrix[i][0]*
//				      cur_state->r->bound[cur_state->z->curClocks[i].enabled-nevents].current;
//				  }
//			#ifdef __LHPN_EVAL__
//				  printf("lv=%d,uv=%d,index=%d,i=%d\n",lvalue, uvalue,index,i);
//			#endif
//				  break;
//				}
//			      }
				}
			
				else if(isit == 'c'){
//					if(z != null){
//						return z.getContinuousBounds(variable, lhpn);
//					}
//					else{
//						return continuousValues.get(new LPNContinuousPair(lhpn.getLpnIndex(),
//								lhpn.getContVarIndex(variable)));
//					}
					LPNContinuousPair lcPair = new LPNContinuousPair(lhpn.getLpnIndex(),
							lhpn.getContVarIndex(variable));
					
					IntervalPair result = null;
					if(continuousValues != null){
						result = continuousValues.get(new LPNContAndRate(lcPair));
					}
					if(result != null){
						return result;
					}
					return z.getContinuousBounds(variable, lhpn);
					
				}
				
				else if (isit == 'b'){
//			    }else if (isit == 'b'){
//			      log_val = cur_state->m->state[index];
//			      if (log_val == '1'){
//				lvalue = 1;
//				uvalue = 1;
//			      } else if (log_val == 'X'){
//				lvalue = 0;
//				uvalue = 1;
//			      } else if (log_val == '0'){
//				lvalue = 0;
//				uvalue = 0;
//			      }
//			#ifdef __LHPN_EVAL__
//			      printf("successful lookup of boolean %d,%c[%d,%d]\n",index,
//				     cur_state->m->state[index],lvalue,uvalue);
//			#endif
				
					// Get the value of the variable from the passed HashMap and convert it as appropriate.
					String sV = variables.get(variable); // sV for "string value"

					if(sV != null){
						int tmp = (sV.toLowerCase().equals("true") || sV.equals("1")) ? 1 : 0; 

						// Currently the platu.state does not support boolean ranges.
						lBound = uBound = tmp;
					}
					else{
						lBound = 0;
						uBound = 1;
					}

				}
			
				else if(isit == 'n'){
			
//			    }else if ((isit == 'n')||(isit == 't')){
//			      // values already stored, no need to evaluate!
//			    }
//			  }
					
					lBound = uBound = (int) lvalue;
					
					
//					if (uvalue == lvalue) {
//						return uvalue;
//					} else {
//						return ((uvalue - lvalue) * new java.util.Random().nextDouble())
//								+ lvalue;
//					}
					
				}
				
				else if ((isit == 't')){
					
					// Should be fine to use the lvalue and uvalue.

					lBound = (int) lvalue;
					uBound = (int) uvalue;
					
					
//					// Get the value of the variable from the passed HashMap and convert it as appropriate.
//					String sV = variables.get(variable); // sV for "string value"
//					
//					if(sV != null){
//						lBound = uBound = 1;
//					}
//					else{
//						lBound = 0;
//						uBound = 1;
//					}
				}
				
//			};
			}
		

		// TODO : need to return an appropriate value when the operation is "".
		return new IntervalPair(lBound, uBound);
	}
	
	/**
	 * Converts an integer range for an expression tree representing a logical into
	 * a boolean pair.
	 * @param expr
	 * 			An expression tree.
	 * @param range
	 * 			The integer range.
	 * @return
	 * 		The range converted to a boolean pair if expr.logical is true and range is non-null.
	 */
	private BooleanPair logicalConversion(ExprTree expr, IntervalPair range){
		Boolean lower, upper;

		if( range != null && expr.logical){ // Note : range can only be set if range is non-null.
			lower = range.get_LowerBound() != 0; // False if value is zero and true otherwise.
			upper = range.get_UpperBound() != 0; // False if value is zero and true otherwise.
		}
		else{
		
			if (range!=null) {
				if((range.get_LowerBound() == 0) && (range.get_UpperBound() == 0)){// false
					lower = upper = false;
				}	
				else if (((range.get_LowerBound() < 0) && (range.get_UpperBound() < 0)) ||
						((range.get_LowerBound() > 0) && (range.get_UpperBound() > 0))){ // true
					lower = upper = true;
				}

				else{
					lower = false;
					upper = true;
				}
			} else {
				lower = false;
				upper = true;
			}
		}
		return new BooleanPair(lower, upper);
	}
	
//------------------------------- Inner Class -------------------------------------	
	private class BooleanPair {

		private Boolean _lower;
		private Boolean _upper;
		
		public BooleanPair(Boolean lower, Boolean upper) {
			super();
			this._lower = lower;
			this._upper = upper;
		}

		public Boolean get_lower() {
			return _lower;
		}

//		public void set_lower(Boolean lower) {
//			this._lower = lower;
//		}

		public Boolean get_upper() {
			return _upper;
		}

//		public void set_upper(Boolean upper) {
//			this._upper = upper;
//		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_lower == null) ? 0 : _lower.hashCode());
			result = prime * result + ((_upper == null) ? 0 : _upper.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof BooleanPair))
				return false;
			BooleanPair other = (BooleanPair) obj;
			if (_lower == null) {
				if (other._lower != null)
					return false;
			} else if (!_lower.equals(other._lower))
				return false;
			if (_upper == null) {
				if (other._upper != null)
					return false;
			} else if (!_upper.equals(other._upper))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "BooleanPair [lower=" + _lower + ", upper=" + _upper + "]";
		}
		
	}
}