package lhpn2sbml.parser;

public class eventADT {
	String event;

	boolean dropped;

	boolean immediate;

	int color;

	int signal;

	int lower;

	int upper;

	int process;

	int type;

	String data;

	String hsl;

	String transRate;

	int rate;

	int lrate;

	int urate;

	int lrange;

	int urange;

	int linitrate;

	int uinitrate;

	level_exp SOP;

	ExprTree EXP;

	class level_exp {
		String product;
	}
}