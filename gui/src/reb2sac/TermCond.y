%{
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.sbml.libsbml.*;
import biomodelsim.core.gui.*;
%}

/* %union	{ */
/*     char *string;	 */
/*     double value; */
/* } */

/* %type <value> SAD_CONSTANT          */
/* %type <string> SAD_TIME_VAR          */
/* %type <string> SAD_STRING          */
/* %type <string> WORD */
/* %type <string> SAD_SPECIES */
/* %type <string> SAD_REACTION */

%token WORD NUM
        
%left '&' '|'
%left '<' '>' '='
%left '+' '-'
%left '*' '/'
%left UMINUS '!'
%left '#' '%'
%left '('

%%

program : bool_exp
;            
        
bool_exp :
    logical_exp
    | comp_exp                        
;            
        
logical_exp :
      bool_exp '&' '&' bool_exp
    | bool_exp '|' '|' bool_exp
    | '!' bool_exp
    | '(' logical_exp ')'
;        
    
comp_exp :
      num_exp '<' '=' num_exp
    | num_exp '<' num_exp
    | num_exp '>' '='  num_exp
    | num_exp '>' num_exp
    | num_exp '=' num_exp
    | '(' comp_exp ')'
;        

num_exp : 
    num_exp '+' num_exp
    | num_exp '-' num_exp
    | num_exp '*' num_exp
    | num_exp '/' num_exp
    | '-' num_exp %prec UMINUS
    | '(' num_exp ')'
    | WORD '(' num_exp ')'
    {
      if (!($1.sval.equals("exp")) &&
	  !($1.sval.equals("log"))) {
	JOptionPane.showMessageDialog(biosim.frame(), "Expected exp or log!",
				"Error", JOptionPane.ERROR_MESSAGE);
	return 2;
      }
    }
    | WORD '(' num_exp ',' num_exp ')'
    {
      if (!($1.sval.equals("pow"))) {
	JOptionPane.showMessageDialog(biosim.frame(), "Expected pow!",
				"Error", JOptionPane.ERROR_MESSAGE);
	return 3;
      }
    }
    | NUM
    | WORD
    {
      if (!($1.sval.equals("t"))) {
	boolean found = false;
	for (int i=0;i<listOfSpecies.size();i++) {
	  if ($1.sval.equals(listOfSpecies.get(i))) {
	    reb2sac.addToIntSpecies(listOfSpecies.get(i));
	    found = true;
	    break;
	  }
	}
	if (!found) {
	  JOptionPane.showMessageDialog(biosim.frame(), $1.sval + " is not a valid species!",
					"Error", JOptionPane.ERROR_MESSAGE);
	  return 4;
	}
      }
    }
    | '%' WORD
    {
      boolean found = false;
      for (int i=0;i<listOfSpecies.size();i++) {
	if ($2.sval.equals(listOfSpecies.get(i))) {
	  reb2sac.addToIntSpecies(listOfSpecies.get(i));
	  found = true;
	  break;
	}
      }
      if (!found) {
	JOptionPane.showMessageDialog(biosim.frame(), $2.sval + " is not a valid species!",
				      "Error", JOptionPane.ERROR_MESSAGE);
	return 5;
      }
    }
    | '#' WORD
    {
      boolean found = false;
      for (int i=0;i<listOfSpecies.size();i++) {
	if ($2.sval.equals(listOfSpecies.get(i))) {
	  reb2sac.addToIntSpecies(listOfSpecies.get(i));
	  found = true;
	  break;
	}
      }
      if (!found) {
	JOptionPane.showMessageDialog(biosim.frame(), $2.sval + " is not a valid species!",
				      "Error", JOptionPane.ERROR_MESSAGE);
	return 6;
      }
    }
    | '@' WORD
    {
      boolean found = false;
      for (int i=0;i<listOfReactions.size();i++) {
	if ($2.sval.equals(listOfReactions.get(i))) {
	  found = true;
	  break;
	}
      }
      if (!found) {
	JOptionPane.showMessageDialog(biosim.frame(), $2.sval + " is not a valid reaction!",
				      "Error", JOptionPane.ERROR_MESSAGE);
	return 6;
      }
    }
;        
    
%%

private String expr;
private BioSim biosim;
private Reb2Sac reb2sac;
private ArrayList<String> listOfSpecies;
private ArrayList<String> listOfReactions;

void yyerror(String s)
{
  //JOptionPane.showMessageDialog(biosim.frame(), s,
  //			"Error", JOptionPane.ERROR_MESSAGE);
  // System.out.println("error: "+s);
}

private static final int END_OF_STRING=0;

private String tokvalue = "";
private int position=0;
private int token=0;

int yylex()
{
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
    c=expr.charAt(position);
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
      if ((!readword)&&(!readnum)&&(!readsci)) {
	return(c);
      }
      else if (readword) {
	position--;
	yylval = new TermCondVal(tokvalue);
	return(WORD);
      } else {
	position--;
	yylval = new TermCondVal(tokvalue);
	return(NUM);
      }
    case '+':
    case '-':
      if ((readsci) && (!readnum) && (readsign)) {
	tokvalue+=c;
	readsign=false;
	break;
      } if ((readsci) && (!readnum) && (!readsign)) {
	return -1;
      } else if ((!readword)&&(!readnum)&&(!readsci)) {
	return(c);
      }
      else if (readword) {
	position--;
	yylval = new TermCondVal(tokvalue);
	return(WORD);
      } else {
	position--;
	yylval = new TermCondVal(tokvalue);
	return(NUM);
      }
    case ' ':
      if (readword) {
	yylval = new TermCondVal(tokvalue);
	return (WORD); 
      } else if ((readnum)||(readsci)) {
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
      tokvalue+=c;
      break;
    case '.':
      if (readsci) {
	return -1;
      } else if (!readword) {
	readnum = true;
      }
      tokvalue+=c;
      break; 
    case 'e':
      if (readsci) {
	return -1;
      } else if (readnum) {
	readsci = true;
	readnum = false;
	readsign = true;
	tokvalue+=c;
	break;
      }
    default:
      if ((readnum)||(readsci)) {
	return -1;
      }
      readword=true;
      tokvalue+=c;
      break;
    }
  }
  if ((!readword)&&(!readnum)) {
    return(END_OF_STRING);
  } else if (readword) {
    yylval = new TermCondVal(tokvalue);
    return(WORD);
  } else if (readnum) {
    yylval = new TermCondVal(tokvalue);
    return(NUM);
  }
  return -1;
}

public int ParseTermCond(BioSim biosim, Reb2Sac reb2sac, ArrayList<String>listOfSpecies, 
			 ArrayList<String>listOfReactions, String termCond)
{ 
  (this).biosim = biosim;
  (this).reb2sac = reb2sac;
  (this).listOfSpecies = listOfSpecies;
  (this).listOfReactions = listOfReactions;
  expr = termCond;
  position = 0;
  return yyparse();
}

