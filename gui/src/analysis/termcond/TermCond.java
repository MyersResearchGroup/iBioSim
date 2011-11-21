//### This file created by BYACC 1.8(/Java extension  1.14)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";

package analysis.termcond;

//#line 2 "TermCond.y"
import java.util.*;
import javax.swing.*;

import main.*;

//#line 25 "TermCond.java"

public class TermCond {

	boolean yydebug; // do I want debug output?

	int yynerrs; // number of errors so far

	int yyerrflag; // was there an error?

	int yychar; // the current working character

	// ########## MESSAGES ##########
	// ###############################################################
	// method: debug
	// ###############################################################
	void debug(String msg) {
		if (yydebug)
			System.out.println(msg);
	}

	// ########## STATE STACK ##########
	final static int YYSTACKSIZE = 500; // maximum stack size

	int statestk[] = new int[YYSTACKSIZE]; // state stack

	int stateptr;

	int stateptrmax; // highest index of stackptr

	int statemax; // state when highest index reached

	// ###############################################################
	// methods: state stack push,pop,drop,peek
	// ###############################################################

	final void state_push(int state) {
		try {
			stateptr++;
			statestk[stateptr] = state;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			int oldsize = statestk.length;
			int newsize = oldsize * 2;
			int[] newstack = new int[newsize];
			System.arraycopy(statestk, 0, newstack, 0, oldsize);
			statestk = newstack;
			statestk[stateptr] = state;
		}
	}

	final int state_pop() {
		return statestk[stateptr--];
	}

	final void state_drop(int cnt) {
		stateptr -= cnt;
	}

	final int state_peek(int relative) {
		return statestk[stateptr - relative];
	}

	// ###############################################################
	// method: init_stacks : allocate and prepare stacks
	// ###############################################################
	final boolean init_stacks() {
		stateptr = -1;
		val_init();
		return true;
	}

	// ###############################################################
	// method: dump_stacks : show n levels of the stacks
	// ###############################################################
	void dump_stacks(int count) {
		int i;
		System.out.println("=index==state====value=     s:" + stateptr + "  v:" + valptr);
		for (i = 0; i < count; i++)
			System.out.println(" " + i + "    " + statestk[i] + "      " + valstk[i]);
		System.out.println("======================");
	}

	// ########## SEMANTIC VALUES ##########
	// public class TermCondVal is defined in TermCondVal.java

	String yytext;// user variable to return contextual strings

	TermCondVal yyval; // used to return semantic vals from action routines

	TermCondVal yylval;// the 'lval' (result) I got from yylex()

	TermCondVal valstk[];

	int valptr;

	// ###############################################################
	// methods: value stack push,pop,drop,peek.
	// ###############################################################
	void val_init() {
		valstk = new TermCondVal[YYSTACKSIZE];
		yyval = new TermCondVal();
		yylval = new TermCondVal();
		valptr = -1;
	}

	void val_push(TermCondVal val) {
		if (valptr >= YYSTACKSIZE)
			return;
		valstk[++valptr] = val;
	}

	TermCondVal val_pop() {
		if (valptr < 0)
			return new TermCondVal();
		return valstk[valptr--];
	}

	void val_drop(int cnt) {
		int ptr;
		ptr = valptr - cnt;
		if (ptr < 0)
			return;
		valptr = ptr;
	}

	TermCondVal val_peek(int relative) {
		int ptr;
		ptr = valptr - relative;
		if (ptr < 0)
			return new TermCondVal();
		return valstk[ptr];
	}

	// #### end semantic value section ####
	public final static short WORD = 257;

	public final static short NUM = 258;

	public final static short UMINUS = 259;

	public final static short YYERRCODE = 256;

	final static short yylhs[] = { -1, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4,
			4, 4, 4, 4, 4, 4, };

	final static short yylen[] = { 2, 1, 1, 1, 4, 4, 2, 3, 4, 3, 4, 3, 3, 3, 3, 3, 3, 3, 2, 3, 4,
			6, 1, 1, 2, 2, 2, };

	final static short yydefred[] = { 0, 0, 22, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 0, 0, 18, 6, 25,
			24, 0, 0, 0, 0, 26, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 13, 19, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 16, 17, 20, 0, 4, 5, 0, 0, 0, 21, };

	final static short yydgoto[] = { 9, 10, 11, 12, 13, };

	final static short yysindex[] = { -33, -39, 0, -18, -33, -231, -229, -33, -228, 0, -24, 0, 0,
			114, -18, -18, 0, 0, 0, 0, -24, -11, -8, 87, 0, -2, -87, -29, -22, -18, -18, -18, -18,
			-18, 121, 72, 0, 0, 0, -33, -33, -18, 46, -18, 46, 46, -37, -37, 0, 0, 0, -18, 0, 0,
			46, 46, 136, 0, };

	final static short yyrindex[] = { 0, 9, 0, 0, 0, 0, 0, 0, 0, 0, 38, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, -14, -13, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 21, 45, 20,
			34, 0, 0, 0, 0, 0, 0, 49, 60, 0, 0, };

	final static short yygindex[] = { 0, 69, 33, 36, 109, };

	final static int YYTABLESIZE = 240;

	static short yytable[];
	static {
		yytable();
	}

	static void yytable() {
		yytable = new short[] { 4, 14, 5, 9, 6, 32, 5, 7, 6, 23, 33, 15, 3, 5, 25, 6, 3, 5, 15, 6,
				14, 11, 15, 3, 2, 3, 18, 3, 19, 24, 36, 8, 41, 37, 15, 8, 39, 40, 1, 43, 21, 9, 8,
				22, 9, 12, 8, 23, 0, 8, 23, 23, 23, 23, 23, 0, 23, 0, 14, 11, 10, 14, 11, 14, 14,
				14, 0, 0, 0, 23, 23, 23, 15, 17, 0, 15, 20, 15, 15, 15, 14, 14, 14, 12, 0, 0, 12,
				8, 32, 30, 8, 31, 0, 33, 15, 15, 15, 0, 10, 0, 26, 10, 0, 0, 0, 0, 0, 0, 52, 53, 2,
				3, 16, 38, 32, 30, 23, 31, 0, 33, 0, 0, 0, 34, 35, 0, 0, 9, 38, 32, 30, 0, 31, 23,
				33, 0, 42, 44, 45, 46, 47, 48, 49, 0, 14, 11, 0, 27, 29, 28, 54, 0, 55, 0, 0, 0,
				32, 30, 15, 31, 56, 33, 50, 32, 30, 51, 31, 0, 33, 12, 0, 0, 0, 8, 27, 29, 28, 57,
				32, 30, 0, 31, 0, 33, 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 1, 2, 0, 0,
				0, 0, 0, 1, 2, 0, 0, 1, 2, };
	}

	static short yycheck[];
	static {
		yycheck();
	}

	static void yycheck() {
		yycheck = new short[] { 33, 40, 35, 0, 37, 42, 35, 40, 37, 0, 47, 40, 45, 35, 38, 37, 45,
				35, 40, 37, 0, 0, 40, 45, 38, 38, 257, 45, 257, 257, 41, 64, 61, 41, 0, 64, 38,
				124, 0, 61, 7, 38, 64, 7, 41, 0, 64, 38, -1, 0, 41, 42, 43, 44, 45, -1, 47, -1, 38,
				38, 0, 41, 41, 43, 44, 45, -1, -1, -1, 60, 61, 62, 38, 4, -1, 41, 7, 43, 44, 45,
				60, 61, 62, 38, -1, -1, 41, 38, 42, 43, 41, 45, -1, 47, 60, 61, 62, -1, 38, -1,
				124, 41, -1, -1, -1, -1, -1, -1, 39, 40, 124, 124, 3, 41, 42, 43, 7, 45, -1, 47,
				-1, -1, -1, 14, 15, -1, -1, 124, 41, 42, 43, -1, 45, 124, 47, -1, 27, 28, 29, 30,
				31, 32, 33, -1, 124, 124, -1, 60, 61, 62, 41, -1, 43, -1, -1, -1, 42, 43, 124, 45,
				51, 47, 41, 42, 43, 44, 45, -1, 47, 124, -1, -1, -1, 124, 60, 61, 62, 41, 42, 43,
				-1, 45, -1, 47, 124, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				-1, -1, -1, 257, 258, -1, -1, 257, 258, -1, -1, -1, -1, -1, 257, 258, -1, -1, 257,
				258, };
	}

	final static short YYFINAL = 9;

	final static short YYMAXTOKEN = 259;

	final static String yyname[] = { "end-of-file", null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, "'!'", null, "'#'", null,
			"'%'", "'&'", null, "'('", "')'", "'*'", "'+'", "','", "'-'", null, "'/'", null, null,
			null, null, null, null, null, null, null, null, null, null, "'<'", "'='", "'>'", null,
			"'@'", null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, "'|'", null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, null, null, null, null, null, "WORD", "NUM",
			"UMINUS", };

	final static String yyrule[] = { "$accept : program", "program : bool_exp",
			"bool_exp : logical_exp", "bool_exp : comp_exp",
			"logical_exp : bool_exp '&' '&' bool_exp", "logical_exp : bool_exp '|' '|' bool_exp",
			"logical_exp : '!' bool_exp", "logical_exp : '(' logical_exp ')'",
			"comp_exp : num_exp '<' '=' num_exp", "comp_exp : num_exp '<' num_exp",
			"comp_exp : num_exp '>' '=' num_exp", "comp_exp : num_exp '>' num_exp",
			"comp_exp : num_exp '=' num_exp", "comp_exp : '(' comp_exp ')'",
			"num_exp : num_exp '+' num_exp", "num_exp : num_exp '-' num_exp",
			"num_exp : num_exp '*' num_exp", "num_exp : num_exp '/' num_exp",
			"num_exp : '-' num_exp", "num_exp : '(' num_exp ')'", "num_exp : WORD '(' num_exp ')'",
			"num_exp : WORD '(' num_exp ',' num_exp ')'", "num_exp : NUM", "num_exp : WORD",
			"num_exp : '%' WORD", "num_exp : '#' WORD", "num_exp : '@' WORD", };

	// #line 152 "TermCond.y"

	private String expr;

	private ArrayList<String> listOfSpecies;

	private ArrayList<String> listOfReactions;

	void yyerror(String s) {
		// JOptionPane.showMessageDialog(BioSim.frame, s,
		// "Error", JOptionPane.ERROR_MESSAGE);
		// System.out.println("error: "+s);
	}

	private static final int END_OF_STRING = 0;

	private String tokvalue = "";

	private int position = 0;

	int yylex() {
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
			case '%':
			case '#':
			case '@':
			case '(':
			case '<':
			case '>':
			case '=':
			case '&':
			case '|':
			case '*':
			case '/':
			case '!':
			case ')':
			case ',':
				if ((!readword) && (!readnum) && (!readsci)) {
					return (c);
				}
				else if (readword) {
					position--;
					yylval = new TermCondVal(tokvalue);
					return (WORD);
				}
				else {
					position--;
					yylval = new TermCondVal(tokvalue);
					return (NUM);
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
				else if (readword) {
					position--;
					yylval = new TermCondVal(tokvalue);
					return (WORD);
				}
				else {
					position--;
					yylval = new TermCondVal(tokvalue);
					return (NUM);
				}
			case ' ':
				if (readword) {
					yylval = new TermCondVal(tokvalue);
					return (WORD);
				}
				else if ((readnum) || (readsci)) {
					yylval = new TermCondVal(tokvalue);
					return (NUM);
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
		else if (readword) {
			yylval = new TermCondVal(tokvalue);
			return (WORD);
		}
		else if (readnum) {
			yylval = new TermCondVal(tokvalue);
			return (NUM);
		}
		return -1;
	}

	public int ParseTermCond(ArrayList<String> listOfSpecies,
			ArrayList<String> listOfReactions, String termCond) {
		(this).listOfSpecies = listOfSpecies;
		(this).listOfReactions = listOfReactions;
		expr = termCond;
		position = 0;
		return yyparse();
	}

	// #line 409 "TermCond.java"
	// ###############################################################
	// method: yylexdebug : check lexer state
	// ###############################################################
	void yylexdebug(int state, int ch) {
		String s = null;
		if (ch < 0)
			ch = 0;
		if (ch <= YYMAXTOKEN) // check index bounds
			s = yyname[ch]; // now get it
		if (s == null)
			s = "illegal-symbol";
		debug("state " + state + ", reading " + ch + " (" + s + ")");
	}

	// The following are now global, to aid in error reporting
	int yyn; // next next thing to do

	int yym; //

	int yystate; // current parsing state from state table

	String yys; // current token string

	// ###############################################################
	// method: yyparse : parse input and execute indicated items
	// ###############################################################
	int yyparse() {
		boolean doaction;
		init_stacks();
		yynerrs = 0;
		yyerrflag = 0;
		yychar = -1; // impossible char forces a read
		yystate = 0; // initial state
		state_push(yystate); // save it
		val_push(yylval); // save empty value
		while (true) // until parsing is done, either correctly, or w/error
		{
			doaction = true;
			// if (yydebug) debug("loop");
			// #### NEXT ACTION (from reduction table)
			for (yyn = yydefred[yystate]; yyn == 0; yyn = yydefred[yystate]) {
				// if (yydebug)
				// debug("yyn:"+yyn+" state:"+yystate+" yychar:"+yychar);
				if (yychar < 0) // we want a char?
				{
					yychar = yylex(); // get next token
					// if (yydebug) debug(" next yychar:"+yychar);
					// #### ERROR CHECK ####
					if (yychar < 0) // it it didn't work/error
					{
						yychar = 0; // change it to default string (no -1!)
						// if (yydebug)
						// yylexdebug(yystate,yychar);
					}
				}// yychar<0
				yyn = yysindex[yystate]; // get amount to shift by (shift index)
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yychar) {
					// if (yydebug)
					// debug("state "+yystate+", shifting to state "+yytable[yyn]);
					// #### NEXT STATE ####
					yystate = yytable[yyn];// we are in a new state
					state_push(yystate); // save it
					val_push(yylval); // push our lval as the input for next
										// rule
					yychar = -1; // since we have 'eaten' a token, say we need
									// another
					if (yyerrflag > 0) // have we recovered an error?
						--yyerrflag; // give ourselves credit
					doaction = false; // but don't process yet
					break; // quit the yyn=0 loop
				}

				yyn = yyrindex[yystate]; // reduce
				if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yychar) { // we
					// reduced!
					// if (yydebug) debug("reduce");
					yyn = yytable[yyn];
					doaction = true; // get ready to execute
					break; // drop down to actions
				}
				else // ERROR RECOVERY
				{
					if (yyerrflag == 0) {
						yyerror("syntax error");
						yynerrs++;
					}
					if (yyerrflag < 3) // low error count?
					{
						yyerrflag = 3;
						while (true) // do until break
						{
							if (stateptr < 0) // check for under & overflow here
							{
								yyerror("stack underflow. aborting..."); // note
																			// lower
																			// case
																			// 's'
								return 1;
							}
							yyn = yysindex[state_peek(0)];
							if ((yyn != 0) && (yyn += YYERRCODE) >= 0 && yyn <= YYTABLESIZE
									&& yycheck[yyn] == YYERRCODE) {
								// if (yydebug)
								// debug("state "+state_peek(0)+", error
								// recovery shifting to
								// state "+yytable[yyn]+" ");
								yystate = yytable[yyn];
								state_push(yystate);
								val_push(yylval);
								doaction = false;
								break;
							}
							else {
								// if (yydebug)
								// debug("error recovery discarding state "+state_peek(0)+" ");
								if (stateptr < 0) // check for under & overflow
													// here
								{
									yyerror("Stack underflow. aborting..."); // capital
																				// 'S'
									return 1;
								}
								state_pop();
								val_pop();
							}
						}
					}
					else // discard this token
					{
						if (yychar == 0)
							return 1; // yyabort
						// if (yydebug)
						// {
						// yys = null;
						// if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
						// if (yys == null) yys = "illegal-symbol";
						// debug("state "+yystate+", error recovery discards
						// token
						// "+yychar+" ("+yys+")");
						// }
						yychar = -1; // read another
					}
				}// end error recovery
			}// yyn=0 loop
			if (!doaction) // any reason not to proceed?
				continue; // skip action
			yym = yylen[yyn]; // get count of terminals on rhs
			// if (yydebug)
			// debug("state "+yystate+", reducing "+yym+" by rule "+yyn+"
			// ("+yyrule[yyn]+")");
			if (yym > 0) // if count of rhs not 'nil'
				yyval = val_peek(yym - 1); // get current semantic value
			switch (yyn) {
			// ########## USER-SUPPLIED ACTIONS ##########
			case 20:
				// #line 67 "TermCond.y"
			{
				if (!(val_peek(3).sval.equals("exp")) && !(val_peek(3).sval.equals("log"))) {
					JOptionPane.showMessageDialog(Gui.frame, "Expected exp or log!", "Error",
							JOptionPane.ERROR_MESSAGE);
					return 2;
				}
			}
				break;
			case 21:
				// #line 76 "TermCond.y"
			{
				if (!(val_peek(5).sval.equals("pow"))) {
					JOptionPane.showMessageDialog(Gui.frame, "Expected pow!", "Error",
							JOptionPane.ERROR_MESSAGE);
					return 3;
				}
			}
				break;
			case 23:
				// #line 85 "TermCond.y"
			{
				if (!(val_peek(0).sval.equals("t"))) {
					boolean found = false;
					for (int i = 0; i < listOfSpecies.size(); i++) {
						if (val_peek(0).sval.equals(listOfSpecies.get(i))) {
							// reb2sac.addToIntSpecies(listOfSpecies.get(i));
							found = true;
							break;
						}
					}
					if (!found) {
						JOptionPane.showMessageDialog(Gui.frame, val_peek(0).sval
								+ " is not a valid species!", "Error", JOptionPane.ERROR_MESSAGE);
						return 4;
					}
				}
			}
				break;
			case 24:
				// #line 103 "TermCond.y"
			{
				boolean found = false;
				for (int i = 0; i < listOfSpecies.size(); i++) {
					if (val_peek(0).sval.equals(listOfSpecies.get(i))) {
						// reb2sac.addToIntSpecies(listOfSpecies.get(i));
						found = true;
						break;
					}
				}
				if (!found) {
					JOptionPane.showMessageDialog(Gui.frame, val_peek(0).sval
							+ " is not a valid species!", "Error", JOptionPane.ERROR_MESSAGE);
					return 5;
				}
			}
				break;
			case 25:
				// #line 119 "TermCond.y"
			{
				boolean found = false;
				for (int i = 0; i < listOfSpecies.size(); i++) {
					if (val_peek(0).sval.equals(listOfSpecies.get(i))) {
						// reb2sac.addToIntSpecies(listOfSpecies.get(i));
						found = true;
						break;
					}
				}
				if (!found) {
					JOptionPane.showMessageDialog(Gui.frame, val_peek(0).sval
							+ " is not a valid species!", "Error", JOptionPane.ERROR_MESSAGE);
					return 6;
				}
			}
				break;
			case 26:
				// #line 135 "TermCond.y"
			{
				boolean found = false;
				for (int i = 0; i < listOfReactions.size(); i++) {
					if (val_peek(0).sval.equals(listOfReactions.get(i))) {
						found = true;
						break;
					}
				}
				if (!found) {
					JOptionPane.showMessageDialog(Gui.frame, val_peek(0).sval
							+ " is not a valid reaction!", "Error", JOptionPane.ERROR_MESSAGE);
					return 6;
				}
			}
				break;
			// #line 651 "TermCond.java"
			// ########## END OF USER-SUPPLIED ACTIONS ##########
			}// switch
			// #### Now let's reduce... ####
			// if (yydebug) debug("reduce");
			state_drop(yym); // we just reduced yylen states
			yystate = state_peek(0); // get new state
			val_drop(yym); // corresponding value drop
			yym = yylhs[yyn]; // select next TERMINAL(on lhs)
			if (yystate == 0 && yym == 0)// done? 'rest' state and at first
											// TERMINAL
			{
				// if (yydebug) debug("After reduction, shifting from state 0 to
				// state
				// "+YYFINAL+"");
				yystate = YYFINAL; // explicitly say we're done
				state_push(YYFINAL); // and save it
				val_push(yyval); // also save the semantic value of parsing
				if (yychar < 0) // we want another character?
				{
					yychar = yylex(); // get next character
					if (yychar < 0)
						yychar = 0; // clean, if necessary
					// if (yydebug)
					// yylexdebug(yystate,yychar);
				}
				if (yychar == 0) // Good exit (if lex returns 0 ;-)
					break; // quit the loop--all DONE
			}// if yystate
			else // else not done yet
			{ // get next state and push, for next yydefred[]
				yyn = yygindex[yym]; // find out where to go
				if ((yyn != 0) && (yyn += yystate) >= 0 && yyn <= YYTABLESIZE
						&& yycheck[yyn] == yystate)
					yystate = yytable[yyn]; // get new state
				else
					yystate = yydgoto[yym]; // else go to new defred
				// if (yydebug) debug("after reduction, shifting from state
				// "+state_peek(0)+" to state "+yystate+"");
				state_push(yystate); // going again, so push state & val...
				val_push(yyval); // for next action
			}
		}// main loop
		return 0;// yyaccept!!
	}

	// ## end of method parse() ######################################

	// ## run() --- for Thread #######################################
	/**
	 * A default run method, used for operating this parser object in the
	 * background. It is intended for extending Thread or implementing Runnable.
	 * Turn off with -Jnorun .
	 */
	public void run() {
		yyparse();
	}

	// ## end of method run() ########################################

	// ## Constructors ###############################################
	/**
	 * Default constructor. Turn off with -Jnoconstruct .
	 * 
	 */
	public TermCond() {
		// nothing to do
	}

	/**
	 * Create a parser, setting the debug to true or false.
	 * 
	 * @param debugMe
	 *            true for debugging, false for no debug.
	 */
	public TermCond(boolean debugMe) {
		yydebug = debugMe;
	}
	// ###############################################################

}
// ################### END OF CLASS ##############################
