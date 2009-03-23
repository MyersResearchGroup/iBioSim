package lhpn2sbml.parser;

public class ExprParser {

	private String tokvalue = "";
	private int position = 0;
	private int token = 0;
	private double result = 0;
	
	public int intexpr_gettok(String expr) {
		char c;
		boolean readword;
		
		readword = false;
		while (position < expr.length()) {
			
		}
		
		if (!readword) {
			return WORD;
		}
		else {
			return END_OF_STRING;
		}
	}

	private static final int WORD = 1;
	
	private static final int END_OF_STRING = 2;

}