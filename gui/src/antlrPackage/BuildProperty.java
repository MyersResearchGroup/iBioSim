package antlrPackage;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import lpn.parser.LhpnFile;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.language.Expr;

import antlr.CommonAST;
import antlr.Parser;
/*import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream; */

import antlr.collections.AST;
import antlrPackage.PropertyLexer;
import antlrPackage.PropertyParser;
import antlr.Parser.*;

public class BuildProperty {
	//public static JFrame frame;
	//private static String root;
	//private static String separator;

	public void buildProperty() throws IOException, RecognitionException {
		
		//String propertyId = JOptionPane.showInputDialog(frame, "Enter the SVA property name:", "Model ID", JOptionPane.PLAIN_MESSAGE);
		//System.out.println(propertyId);
		//if (propertyId!=null){
			//String property = JOptionPane.showInputDialog(frame, "Enter the SVA property:", "Model", JOptionPane.PLAIN_MESSAGE);
		//CharStream charStream = new ANTLRStringStream(" wait(omega > 2.2, 20);\r\n" + 
				//"assert(abc, 20); ");
			BufferedReader input = new BufferedReader(new FileReader("C:\\Users\\Dhanashree\\research\\prop.txt"));
			
			String read = input.readLine();
			
			StringBuffer sb = new StringBuffer(read);
			
			while(read != null) {
			read=input.readLine();
			sb.append(read);
			}
			
			String  property = sb.toString();
			CharStream charStream = new ANTLRStringStream(property);
		PropertyLexer lexer = new PropertyLexer(charStream);
		TokenStream tokenStream =  new CommonTokenStream(lexer);
		PropertyParser parser = new PropertyParser(tokenStream);
		PropertyParser.program_return program = parser.program();
		System.out.println("tree: "+((Tree)program.tree).toStringTree()+"\n");
		
		CommonTree r0 = ((CommonTree)program.tree);
		//System.out.println("parent :"+program.start.getText());
		int number = r0.getChildCount();
		LhpnFile lpn = new LhpnFile();
		LhpnFile lpnFinal = new LhpnFile();
		printTree(r0, number);
		lpnFinal = generateLPN(r0, lpn);
		
		
	}
	//}
	public static void printTree(CommonTree t, int number) {
		
		if ( t != null ) {
			StringBuffer sb = new StringBuffer(number);
			for ( int i = 0; i < number; i++ )
				sb = sb.append("   ");
			for ( int i = 0; i < t.getChildCount(); i++ ) {
				System.out.println(sb.toString() + t.getChild(i).toString());
				//System.out.println(sb.toString() + t.getChild(i).getType() + ":" + t.getChild(i).toString());
				printTree((CommonTree)t.getChild(i), number+1);
			}
		}
	} 
	
	public static LhpnFile generateLPN(CommonTree t, LhpnFile lpn) throws IOException {
		String enablingCond="";
		String delay="";
		int numPlaces=0;
		int numTransitions=0;
		int numFailTransitions=0;
		int numFailPlaces=0;
		lpn= lpn;
		File lpnFile = new File("Property.lpn");
		lpnFile.createNewFile();
		
		
		
		if ( t != null ) {
			int childCount= t.getChildCount();
			for(int i=0;i<childCount;i++){
				System.out.println("child is : "+t.getChild(i));	
			}
			for(int i=0;i<childCount;i++){
				//System.out.println("child is : "+t.getChild(i));	
			//}
			int child=-1;
			
			switch(t.getChild(i).getType())
			{
			case antlrPackage.PropertyLexer.ASSERT :
				System.out.println("child is here assert, no of child: "+t.getChild(i)+t.getChildCount());
				enablingCond= generateExpression((CommonTree)t.getChild(i).getChild(0));
				delay= generateExpression((CommonTree)t.getChild(i).getChild(1));
				 if(numPlaces==0){
						lpn.addPlace("p"+numPlaces, true);
					}
					else{
						lpn.addPlace("p"+numPlaces, false);
					}
					numPlaces++;
					lpn.addTransition("t" + numTransitions);
					lpn.addEnabling("t" +numTransitions, enablingCond);
					lpn.changeDelay("t" +numTransitions, delay);
					numTransitions++;
					lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
					lpn.addTransition("tFail" + numFailTransitions);
					lpn.addEnabling("tFail" +numFailTransitions, "~"+enablingCond);
					
					numFailTransitions++;
					lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
					lpn.addPlace("pFail"+numFailPlaces, false);
					numFailPlaces++;
					lpn.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numPlaces-1));
					lpn.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpn.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
				break;
			case antlrPackage.PropertyLexer.ID : 
				
				break;
			case antlrPackage.PropertyLexer.INT :
				break;
			case antlrPackage.PropertyLexer.NOT :
				break;
			case antlrPackage.PropertyLexer.PLUS :
				break;
			case antlrPackage.PropertyLexer.MINUS :
				break;
			case antlrPackage.PropertyLexer.MULT :
				break;
			case antlrPackage.PropertyLexer.DIV :
				break;
			case antlrPackage.PropertyLexer.MOD :
				break;
			case antlrPackage.PropertyLexer.EQUAL :
				break;
			case antlrPackage.PropertyLexer.NOT_EQUAL :
				break;
			case antlrPackage.PropertyLexer.GET :
				break;
			case antlrPackage.PropertyLexer.LET :
				break;
			case antlrPackage.PropertyLexer.GETEQ :
				break;
			case antlrPackage.PropertyLexer.LETEQ :
				break;
			
			case antlrPackage.PropertyLexer.AND :
				break;
			case antlrPackage.PropertyLexer.OR :
				break;
			case antlrPackage.PropertyLexer.SAMEAS :
				break;
			case antlrPackage.PropertyLexer.WAIT :
				System.out.println("child is here: "+t.getChild(i));
				int count = t.getChild(i).getChildCount();
				
				System.out.println("count is: "+count);
				for(int j=0;j<count;j++){
					System.out.println("new child is : "+t.getChild(i).getChild(j));	
					//System.out.println("new child is hello : "+newChild.getChild(0).getChild(1));	
				}
				if (count==1){
					enablingCond= generateExpression((CommonTree)t.getChild(i).getChild(0));
				}
				else if(count==2){
					 enablingCond= generateExpression((CommonTree)t.getChild(i).getChild(0));
					 delay= generateExpression((CommonTree)t.getChild(i).getChild(1));
					 
					 if(numPlaces==0){
							lpn.addPlace("p"+numPlaces, true);
						}
						else{
							lpn.addPlace("p"+numPlaces, false);
						}
						numPlaces++;
						lpn.addTransition("t" + numTransitions);
						lpn.addEnabling("t" +numTransitions, enablingCond);
						numTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						lpn.addTransition("tFail" + numFailTransitions);
						lpn.addEnabling("tFail" +numFailTransitions, "~"+enablingCond);
						lpn.changeDelay("tFail" +numFailTransitions, delay);
						numFailTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
						lpn.addPlace("pFail"+numFailPlaces, false);
						numFailPlaces++;
						lpn.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numPlaces-1));
						lpn.addPlace("p"+numPlaces, false);
						numPlaces++;
						lpn.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
				} 
				
				
				break;
			
			case antlrPackage.PropertyLexer.IF :
				System.out.println("child in IF **********: "+t.getChild(i).getChildCount());
				for(int j=0;j<t.getChild(i).getChildCount();j++){
					//int type= t.getChild(i).getChild(j).getType();
					System.out.println("child is : "+t.getChild(i).getChild(j));	
					if(j==0){
						enablingCond=  generateExpression((CommonTree)t.getChild(i).getChild(0));
						//lpn.addPlace("pif0", true);
						if(numPlaces==0){
							lpn.addPlace("p"+numPlaces, true);
						}
						else{
							lpn.addPlace("p"+numPlaces, false);
						}
						numPlaces++;
						lpn.addTransition("t" + numTransitions);
						lpn.addEnabling("t" +numTransitions, enablingCond);
						numTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						lpn.addPlace("p"+numPlaces, false);
						numPlaces++;
						lpn.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));
					}
					else if(t.getChild(i).getChild(j).getType()==antlrPackage.PropertyLexer.ELSEIF){
						lpn=generateLPN((CommonTree)t.getChild(i).getChild(j), lpn);
						
					}
					else{
						lpn=generateLPN((CommonTree)t.getChild(i).getChild(j), lpn);
					}
					//System.out.println("new child is hello : "+newChild.getChild(0).getChild(1));	
					
				}
				 
				break;
			case antlrPackage.PropertyLexer.END :
				break;
			case antlrPackage.PropertyLexer.ELSEIF :
				for(int j=0;j<t.getChild(i).getChildCount();j++){
					//int type= t.getChild(i).getChild(j).getType();
					System.out.println("child is : "+t.getChild(i).getChild(j));	
					if(j==0){
						enablingCond=  generateExpression((CommonTree)t.getChild(i).getChild(0));
						//lpn.addPlace("pif0", true);
						if(numPlaces==0){
							lpn.addPlace("p"+numPlaces, true);
						}
						else{
							lpn.addPlace("p"+numPlaces, false);
						}
						numPlaces++;
						lpn.addTransition("t" + numTransitions);
						lpn.addEnabling("t" +numTransitions, enablingCond);
						numTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						lpn.addPlace("p"+numPlaces, false);
						numPlaces++;
						lpn.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));
					}
					else if(t.getChild(i).getChild(j).getType()==antlrPackage.PropertyLexer.ELSEIF){
						//lpn=generateLPN((CommonTree)t.getChild(i).getChild(j), lpn);
						lpn=generateLPN((CommonTree)t.getChild(i).getChild(j), lpn);
					}
					//System.out.println("new child is hello : "+newChild.getChild(0).getChild(1));	
				}
				break;
				default :
					break;
			}
			}
		
				}
		
		lpn.save("C:\\Users\\Dhanashree\\research\\prop."+lpnFile);	
		return lpn;
	}
	
	 public static String generateExpression(CommonTree newChild) {
		//int indent = 10;
		String result = "";
		String string1= "";
		String string2="";
		if ( newChild != null ) {
			//for ( int i = 0; i < newChild.getChildCount(); i++ ) {
				switch (newChild.getType()) {
				case antlrPackage.PropertyLexer.WAIT: 
					System.out.println("new child *****"+newChild);
					break;
				case antlrPackage.PropertyLexer.IF: 
					System.out.println("new child *****"+newChild);
					break;
				case antlrPackage.PropertyLexer.ID : 
					result= newChild.toString();
					System.out.println("result1"+result);
					break;
				case antlrPackage.PropertyLexer.FLOAT:
					result=newChild.toString();
					break;
				case antlrPackage.PropertyLexer.INT	:
					result=newChild.toString();
					System.out.println("result1"+result);
					break;
				case antlrPackage.PropertyLexer.STRING	:
					result=newChild.toString();
					break;
				case antlrPackage.PropertyLexer.GET :
					//String string2= ">";
					 string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					System.out.println("string1 :"+string1);
					//System.out.println("next child :"+newChild.getChild(1));
					System.out.println("string2 :"+string2);
					result= (string1 + ">" +string2);
					System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.AND :
					 string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "&" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.DIV :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "/" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.EQUAL :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "=" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.GETEQ :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + ">=" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.LET :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "<" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.LETEQ :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "<=" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.MINUS :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "-" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.MOD :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "%" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.MULT :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "*" +string2);
					 System.out.println("result2 :"+result);
					break;
				
				case antlrPackage.PropertyLexer.NOT :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 //string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= ("~" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.NOT_EQUAL :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "!=" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.OR :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "|" +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.PLUS :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + " + " +string2);
					 System.out.println("result2 :"+result);
					break;
				case antlrPackage.PropertyLexer.SAMEAS :
					string1= generateExpression((CommonTree)newChild.getChild(0));
					 string2= generateExpression((CommonTree)newChild.getChild(1));
					 result= (string1 + "==" +string2);
					 System.out.println("result2 :"+result);
					break;
				
					default :
						break;
				}
			
			}
		return result;
		} 

}
