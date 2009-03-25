package lhpn2sbml.parser;

public class ExprParser {

	public int intexpr_gettok(String expr, String tokvalue, int maxtok, int position) {
		char c;
		boolean readword;
		int toklen = 0;

		readword = false;
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
			case '+':
			case '*':
			case '/':
			case '%':
			case '=':
			case '<':
			case '>':
				if (!readword)
					return c;
				else {
					position--;
					return WORD;
				}
			case '-':
				if (!readword) {
					if (expr.charAt(position) == '>') {
						position++;
						return (IMPLIES);
					}
					else
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
			default:
				if (toklen < maxtok) {
					readword = true;
					tokvalue += c;
					toklen++;
				}
				break;
			}
		}
		if (!readword)
			return (END_OF_STRING);
		else
			return (WORD);
	}

	boolean intexpr_U(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  int i;
	  int temp;
	  exprsn newresult = null;

	  switch (token) {
	  case WORD:
	    if (!tokvalue.equals("and")){
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if ((token) != '(') {
		System.out.print("ERROR: Expected a (\n");
		return false;
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,result,signals,nsignals,
			     events,nevents,nplaces)) {
		return false;
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			     events,nevents,nplaces)) 
		return false;
	      if ((token) != ')') {
		System.out.printf("ERROR: Expected a )\n");
		return false;
	      }
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 'n';
		(result).lvalue = (int)(result).lvalue & (int)newresult.lvalue;	
		(result).uvalue = (result).lvalue;	
	      } else{
		(result) = new exprsn((result),newresult,"&",'w');    
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    } else if (!tokvalue.equals("or")){
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if ((token) != '(') {
		System.out.printf("ERROR: Expected a (\n");
		return false;
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,result,signals,nsignals,
			     events,nevents,nplaces)) 
		return false;
	      if ((token) != ',') {
		System.out.printf("ERROR: Expected a ,\n");
		return false;
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			     events,nevents,nplaces)) 
		return false;
	      if ((token) != ')') {
		System.out.printf("ERROR: Expected a )\n");
		return false;
	      }
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 'n';
		(result).lvalue = (int)(result).lvalue | (int)newresult.lvalue;	
		(result).uvalue = (result).lvalue;	
	      } else{
		(result) = new exprsn((result),newresult,"|",'w');    
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    } else if (!tokvalue.equals("exor")){
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if ((token) != '(') {
		System.out.printf("ERROR: Expected a (\n");
		return false;
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,result,signals,nsignals,
			     events,nevents,nplaces)) 
		return false;
	      if ((token) != ',') {
		System.out.printf("ERROR: Expected a ,\n");
		return false;
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			     events,nevents,nplaces)) 
		return false;
	      if ((token) != ')') {
		System.out.printf("ERROR: Expected a )\n");
		return false;
	      }
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 'n';
		(result).lvalue = (int)(result).lvalue ^ (int)newresult.lvalue;	
		(result).uvalue = (result).lvalue;	
	      } else{
		(result) = new exprsn((result),newresult,"^",'w');    
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    } else if (!tokvalue.equals("not")){
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if ((token) != '(') {
		System.out.printf("ERROR: Expected a (\n");
		return false;
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,result,signals,nsignals,
			     events,nevents,nplaces)) 
		return false;
	      if ((token) != ')') {
		System.out.printf("ERROR: Expected a )\n");
		return false;
	      }
	      //simplify if operands are static
	      if (((result).isit=='n')||((result).isit=='t')){
		(result).isit = 'n';
		(result).lvalue = ~(int)(result).lvalue;
		(result).uvalue = (result).lvalue;	
	      } else{
		(result) = new exprsn((result),null,"~",'w');    
	      }
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    } else if ((!tokvalue.equals("true"))||(!tokvalue.equals("t"))){
	      (result) = new exprsn('t',true,true,-1);
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    }else if ((!tokvalue.equals("false"))||(!tokvalue.equals("f"))){
	      (result) = new exprsn('t',false,false,-1);
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    }else{
	      //do boolean lookup here!!!
	      for (i=0;i<nsignals;i++){
		if (!signals[i].name.equals(tokvalue)){{
		  //printf("successful lookup of boolean variable %s\n",signals[i]);
		  (result) = new exprsn('b',0,1,i);
		  (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
		  return true;
		}
	      }
	      for (i=nevents;i<nevents+nplaces;i++){
		if (!events[i].event.equals(tokvalue)){
		  //printf("successful lookup of variable %s\n",events[i]->event);
		  if (events[i].type == VAR){
		    //printf("parsed discrete variable\n");
		    (result) = new exprsn('i',-INFIN,INFIN,i);
		  }
		  else{
		    //printf("parsed continuous variable\n");
		    (result) = new exprsn('c',-INFIN,INFIN,i);
		    //printf("isit = %c\n",(*result)->isit);
		  }
		  (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
		  return true;
		}
	      }
	      if ((int)(tokvalue.charAt(0))>('9')||((int)(tokvalue.charAt(0))<'0')){
		System.out.printf("U1:ERROR(%s): Expected a ID, Number, or a (\n",tokvalue);
		return false;
	      }
	      temp = Integer.parseInt(tokvalue);
	      (result) = new exprsn('n',temp,temp,-1);
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      //printf("resolved number %f\n",temp);
	    }
	    }
	  case '(':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_L(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    if ((token) != ')') {
	      System.out.printf("ERROR: Expected a )\n");
	      return false;
	    }
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    break;
	  default:
	    System.out.printf("U2:ERROR: Expected a ID, Number, or a (\n");
	    return false;
	  }
	  return true;
	}

	boolean intexpr_T(int token,String expr,String tokvalue,int position,
	      exprsn result, signalADT[] signals,int nsignals, 
	      eventADT[] events,int nevents, int nplaces)
	{
	  switch (token) {
	  case WORD:
	  case '(':
	    if (!intexpr_U(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	  case '-':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_U(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((result).isit=='n')||((result).isit=='t')){
	      (result).isit = 'n';
	      (result).lvalue = -((result).lvalue);
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),null,"U-",'a');    
	    }
	  default:
	    System.out.printf("T:ERROR: Expected a ID, Number, (, or -\n");
	    return false;
	  }
	}

	boolean intexpr_C(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  exprsn newresult = null;

	  switch (token) {
	  case '*':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_T(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 'n';
		(result).lvalue = (result).lvalue * newresult.lvalue;	
		(result).uvalue = (result).lvalue;	
	      } else{
		(result) = new exprsn((result),newresult,"*",'a');    
	      }
	    if (!intexpr_C(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  case '/':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_T(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 'n';
		(result).lvalue = (result).lvalue / newresult.lvalue;	
		(result).uvalue = (result).lvalue;	
	      } else{
		(result) = new exprsn((result),newresult,"/",'a');    
	      }
	    if (!intexpr_C(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  case '%':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_T(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 'n';
	      (result).lvalue = (int)(result).lvalue % (int)newresult.lvalue;	
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"%",'a');    
	    }
	    if (!intexpr_C(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
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
	    if (!intexpr_T(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 'n';
	      (result).lvalue = (result).lvalue * newresult.lvalue;	
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"*",'a');    
	    }
	    if (!intexpr_C(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;

	  default:
	    System.out.printf("ERROR: Expected a * or /\n");
	    return false;
	  }
	  return true;
	}

	boolean intexpr_B(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  exprsn newresult = null;

	  switch (token) {
	  case '+':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_S(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 'n';
	      (result).lvalue = (result).lvalue + newresult.lvalue;	
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"+",'a');    
	    }
	    if (!intexpr_B(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  case '-':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_S(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 'n';
	      (result).lvalue = (result).lvalue - newresult.lvalue;	
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"-",'a');    
	    }
	    if (!intexpr_B(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
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

	boolean intexpr_S(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  switch (token) {
	  case WORD:
	  case '(':
	  case '-':
	    if (!intexpr_T(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    if (!intexpr_C(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  default:
	    System.out.printf("S:ERROR: Expected a ID, Number, (, or -\n");
	    return false;
	  }
	  return true;
	}

	boolean intexpr_R(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  switch (token) {
	  case WORD:
	  case '(':
	  case '-':
	    if (!intexpr_S(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    if (!intexpr_B(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  default:
	    System.out.printf("R:ERROR: Expected a ID, Number, (, or -\n");
	    return false;
	  }
	  return true;
	}

	boolean intexpr_P(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  exprsn newresult = null;
	  int spos,i;
	  char[] ineq = new char[VAR];
	  String comp;
	  //  printf("P\n");
	  switch (token) {
	  case '=':
	    spos = position;
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 't';
	      if (result.lvalue == newresult.lvalue) {
	    	  result.lvalue = 1;
	      }
	      else {
	    	  result.lvalue = 0;
	      }
	      (result).uvalue = (result).lvalue;	
	    } else{
	      if ((result).isit=='c'){
		comp = events[(result).index].event;
		comp +="=";
		int paren=0;
		for (i=spos;i<position;i++) {
		  if (expr.charAt(i)=='(') paren++;
		  if (expr.charAt(i)==')') paren--;
		  ineq[i-spos]=expr.charAt(i);
		}
		ineq[i-spos+paren]=0;
		comp += ineq;
		//printf("looking for %s\n",comp.c_str());
		for (i=0;i<nsignals;i++){
		  if (!signals[i].name.equals(comp)){
		    //printf("successful lookup of boolean variable '%s'\n",signals[i]->name);
		    (result).isit = 'b';
		    (result).index = i;
		    (result).lvalue = 0;
		    (result).uvalue = 1;
		    return true;
		  }
		}
	      }else{
		(result) = new exprsn((result),newresult,"==",'r');    
	      }
	    }
	    break;
	  case '>':
	    spos = position;
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if ((token)=='='){
	      spos = position;
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			     events,nevents,nplaces)) 
		return false;
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 't';
			if ((result).lvalue >= newresult.lvalue) {
				result.lvalue = 1;
			}
			else {
				result.lvalue = 0;
			}
		(result).uvalue = (result).lvalue;	
	      } else{
		if ((result).isit=='c'){
		  comp = events[(result).index].event;
		  comp +=">=";
		  int paren=0;
		  for (i=spos;i<position;i++) {
		    if (expr.charAt(i)=='(') paren++;
		    if (expr.charAt(i)==')') paren--;
		    ineq[i-spos]=expr.charAt(i);
		  }
		  ineq[i-spos+paren]=0;
		  comp += ineq;
		  //printf("looking for %s\n",comp.c_str());
		  for (i=0;i<nsignals;i++){
		    if (!signals[i].name.equals(comp)){
		      //printf("successful lookup of boolean variable '%s'\n",signals[i]->name);
		      (result).isit = 'b';
		      (result).index = i;
		      (result).lvalue = 0;
		      (result).uvalue = 1;
		      return true;
		    }
		  }
		}else{
		  (result) = new exprsn((result),newresult,">=",'r');    
		}
	      }
	    }else{
	      if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
		return false;
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 't';
		if ((result).lvalue > newresult.lvalue) {
			result.lvalue = 1;
		}
		else {
			result.lvalue = 0;
		}
		(result).uvalue = (result).lvalue;	
	      } else{
		if ((result).isit=='c'){
		  comp = events[(result).index].event;
		  comp +=">";
		  int paren=0;
		  for (i=spos;i<position;i++) {
		    if (expr.charAt(i)=='(') paren++;
		    if (expr.charAt(i)==')') paren--;
		    ineq[i-spos]=expr.charAt(i);
		  }
		  ineq[i-spos+paren]=0;
		  comp += ineq;
		  //printf("looking for %s\n",comp.c_str());
		  for (i=0;i<nsignals;i++){
		    if (!signals[i].name.equals(comp)){
		      //  printf("successful lookup of boolean variable '%s'\n",signals[i]->name);
		      (result).isit = 'b';
		      (result).index = i;
		      (result).lvalue = 0;
		      (result).uvalue = 1;
		      return true;
		    }
		  }
		}else{
		  (result) = new exprsn((result),newresult,">",'r');    
		}
	      }
	    }
	    break;
	  case '<':
	    spos = position;
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if ((token)=='='){
	      spos = position;
	      (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	      if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
		return false;
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 't';
		if ((result).lvalue <= newresult.lvalue) {
			result.lvalue = 1;
		}
		else {
			result.lvalue = 0;
		}
		(result).uvalue = (result).lvalue;	
	      } else{
		if ((result).isit=='c'){
		  comp = events[(result).index].event;
		  comp +="<=";
		  int paren=0;
		  for (i=spos;i<position;i++) {
		    if (expr.charAt(i)=='(') paren++;
		    if (expr.charAt(i)==')') paren--;
		    ineq[i-spos]=expr.charAt(i);
		    }
		  ineq[i-spos+paren]=0;
		  comp += ineq;
		  //printf("looking for %s\n",comp.c_str());
		  for (i=0;i<nsignals;i++){
		    if (!signals[i].name.equals(comp)){
		      //printf("successful lookup of boolean variable '%s'\n",signals[i]->name);
		      (result).isit = 'b';
		      (result).index = i;
		      (result).lvalue = 0;
		      (result).uvalue = 1;
		      return true;
		    }
		  }
		}else{
		  (result) = new exprsn((result),newresult,"<=",'r');    
		}
	      }
	    }else{
	      if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
		return false;
	      //simplify if operands are static
	      if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		  (((result).isit=='n')||((result).isit=='t'))){
		(result).isit = 't';
		if ((result).lvalue < newresult.lvalue) {
			result.lvalue = 1;
		}
		else {
			result.lvalue = 0;
		}	
		(result).uvalue = (result).lvalue;	
	      } else{
		if ((result).isit=='c'){
		  comp = events[(result).index].event;
		  comp +="<";
		  int paren=0;
		  for (i=spos;i<position;i++) {
		    if (expr.charAt(i)=='(') paren++;
		    if (expr.charAt(i)==')') paren--;
		    ineq[i-spos]=expr.charAt(i);
		  }
		  ineq[i-spos+paren]=0;
		  comp += ineq;
		  System.out.printf("looking for %s\n",comp);
		  for (i=0;i<nsignals;i++){
		    if (!signals[i].name.equals(comp)){
		      System.out.printf("successful lookup of boolean variable '%s'\n",signals[i].name);
		      (result).isit = 'b';
		      (result).index = i;
		      (result).lvalue = 0;
		      (result).uvalue = 1;
		      return true;
		    }
		  }
		}else{
		  (result) = new exprsn((result),newresult,"<",'r');    
		}
	      }
	    }
	    break;
	  case '[':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_R(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    if ((token) != ']') {
	      System.out.printf("ERROR: Expected a ]\n");
	      return false;
	    }
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 't';
	      (result).lvalue =
		(((int)(result).lvalue)>>((int)newresult.lvalue))&1;	
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"[]",'w');    
	    }
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
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
	  //printf("/P\n");
	  return true;
	}


	boolean intexpr_O(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  //  printf("O\n");

	  switch (token) {
	  case WORD:
	  case '(':
	  case '-':
	    if (!intexpr_R(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    if (!intexpr_P(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  default:
	    System.out.printf("O:ERROR: Expected a ID, Number, or a (\n");
	    return false;
	  }
	  //  printf("/O\n");
	  return true;
	}

	boolean intexpr_N(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  //  printf("N\n");

	  switch (token) {
	  case WORD:
	  case '-':
	  case '(':
	    if (!intexpr_O(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  case '~':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_O(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((result).isit=='n')||((result).isit=='t')){
	      (result).isit = 't';
	      if (result.lvalue == 1) {
	    	  result.lvalue = 0;
	      }
	      else {
	    	  result.lvalue = 1;
	      }
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),null,"!",'l');    
	    }
	    break;
	  default:
	    System.out.printf("N:ERROR: Expected a ID, Number, (, or -\n");
	    return false;
	  }
	  return true;
	}

	boolean intexpr_E(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  exprsn newresult = null;
	  //printf("E\n");

	  switch (token) {
	  case '&':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_N(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 't';
	      if ((result.lvalue == 0) || (newresult.lvalue == 0)) {
	    	  result.lvalue = 0;
	      }
	      else {
	    	  result.lvalue = 1;
	      }
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"&&",'l');    
	    }
	    if (!intexpr_E(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  case '|':
	  case ')':
	  case IMPLIES:
	  case END_OF_STRING:
	  default:
	    System.out.printf("ERROR(%c): Expected an &\n",(token));
	    return false;
	  }
	  return true;
	}

	boolean intexpr_D(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  exprsn newresult = null;
	  //printf("D\n");

	  switch (token) {
	  case '|':
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_M(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 't';
	      if (result.lvalue != 0 || newresult.lvalue != 0) {
	    	  result.lvalue = 1;
	      }
	      else {
	    	  result.lvalue = 0;
	      }
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"||",'l');    
	    }
	    if (!intexpr_D(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  case ')':
	  case END_OF_STRING:
	  case IMPLIES:
	    (token)=intexpr_gettok(expr,tokvalue,MAXTOKEN,position);
	    if (!intexpr_M(token,expr,tokvalue,position,newresult,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    //simplify if operands are static
	    if (((newresult.isit=='n')||(newresult.isit=='t'))&&
		(((result).isit=='n')||((result).isit=='t'))){
	      (result).isit = 't';
	      if (result.lvalue != 0 || newresult.lvalue == 0) {
	    	  result.lvalue = 1;
	      }
	      else {
	    	  result.lvalue = 0;
	      }
	      (result).uvalue = (result).lvalue;	
	    } else{
	      (result) = new exprsn((result),newresult,"->",'l');    
	    }
	    if (!intexpr_D(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  default:
	    System.out.printf("ERROR: Expected an | or ->\n");
	    return false;
	  }
	  return true;
	}

	boolean intexpr_M(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  //printf("M\n");
	  switch (token) {
	  case WORD:
	  case '(':
	  case '~':
	  case '-':
	    if (!intexpr_N(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    if (!intexpr_E(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    break;
	  default:
	    System.out.printf("M: ERROR: Expected a ID, Number, (, or -\n");
	    return false;
	  }
	  return true;
	}

	boolean intexpr_L(int token,String expr,String tokvalue,int position,
		       exprsn result, signalADT[] signals,int nsignals, 
		       eventADT[] events,int nevents, int nplaces)
	{
	  //printf("L\n");
	  switch (token) {
	  case WORD:
	  case '(':
	  case '~':
	  case '-':
	    if (!intexpr_M(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces)) 
	      return false;
	    if (!intexpr_D(token,expr,tokvalue,position,result,signals,nsignals,
			   events,nevents,nplaces))
	      return false;
	    break;
	  default:
	    System.out.printf("L:ERROR: Expected a ID, Number, (, or -\n");
	    return false;
	  }
	  return true;
	}
	
	class exprsn {
		 String op;
		  char isit;  // b=Boolean, i=Integer, c=Continuous, n=Number, t=Truth value, 
		              //w=bitWise, a=Arithmetic, r=Relational, l=Logical
		  int lvalue,uvalue;
		  int index;
		  double real;
		  boolean logical;
		  exprsn r1, r2;
		  exprsn(char willbe, boolean lNV, boolean uNV, int ptr) {
		  }
		  exprsn(char willbe, int lNV, int uNV, int ptr) {
		  }
		  exprsn(exprsn nr1, exprsn nr2, String nop, char willbe) {
		  }
		  void op_set(String new_op) {
		  }
		}
	
	class eventADT {
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
		  exprsn EXP;
		  ineqADT inequalities;
	}
	
	class ineqADT {
		  // 0 =  ">", 1 = ">=", 2 = "<", 3 = "<=", 4 = "="
		  // 5 = ":=", 6 = "'dot:=", 7 = ":=T/F"  *AMS* 
		  int type;
		  int uconstant; //for upper bound rate assignments
		  int constant;
		  String uexpr,lexpr;
		  exprsn utree, ltree;
		  int signal;
		}
	
	class signalADT {
		  String name;
		  boolean is_level;
		  int event;
		  int riselower;
		  int riseupper;
		  int falllower;
		  int fallupper;
		  int maxoccurrence;
		}
	
	class level_exp {
		  String product;
		}


	private static final int WORD = 1;

	private static final int IMPLIES = 7;

	private static final int END_OF_STRING = 2;
	
	private static final int MAXTOKEN = 2000;
	
	private static final int VAR = 262144;
	
	private static final int INFIN = 2147483647;

}