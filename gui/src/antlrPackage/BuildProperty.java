package antlrPackage;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import lpn.parser.LhpnFile;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;


import antlrPackage.PropertyLexer;
import antlrPackage.PropertyParser;


public class BuildProperty {
	//public static JFrame frame;
	private String root;
	private String separator;
	static int numPlaces=0;
	static int numTransitions=0;
	static int numFailTransitions=0;
	static int numFailPlaces=0;
	static int numStartPlaces=0;
	static int numEndPlaces=0;
	static String pFirst = "p0";
	
	static List list = new List();
	public void buildProperty(String propFileName) throws IOException, RecognitionException {

		//String propertyId = JOptionPane.showInputDialog(frame, "Enter the SVA property name:", "Model ID", JOptionPane.PLAIN_MESSAGE);
		//System.out.println(propertyId);
		//if (propertyId!=null){
		//String property = JOptionPane.showInputDialog(frame, "Enter the SVA property:", "Model", JOptionPane.PLAIN_MESSAGE);
		//CharStream charStream = new ANTLRStringStream(" wait(omega > 2.2, 20);\r\n" + 
		//"assert(abc, 20); ");
		numPlaces=0;
		numTransitions=0;
		numFailTransitions=0;
		numFailPlaces=0;
		numStartPlaces=0;
		numEndPlaces=0;
		
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		}
		else {
			separator = File.separator;
		}
		LhpnFile lpn = new LhpnFile();
		lpn.load(propFileName);
		
		String lpnFileString= propFileName.substring(0, propFileName.length()-4);
		String lpnFileName = lpnFileString.concat("lpn");
		File lpnFile = new File(lpnFileName);
		lpnFile.createNewFile();
		String[] lpnPath = lpnFileName.split(separator);
		System.out.println("No of places : "+numPlaces);
		BufferedReader input = new BufferedReader(new FileReader(propFileName));

		String line = input.readLine();

		//StringBuffer sb2 = new StringBuffer(line);
		StringBuffer sb = new StringBuffer(line);
		//LhpnFile lpn = new LhpnFile();

		while(line!=null){
		
			line=input.readLine();
				sb.append(line);
				
		}

		String  property = sb.toString();
		System.out.println("property: "+property+"\n");
		CharStream charStream = new ANTLRStringStream(property);
		PropertyLexer lexer = new PropertyLexer(charStream);
		TokenStream tokenStream =  new CommonTokenStream(lexer);
		PropertyParser parser = new PropertyParser(tokenStream);
		PropertyParser.program_return program = parser.program();
		System.out.println("tree: "+((Tree)program.tree).toStringTree()+"\n");

		CommonTree r0 = ((CommonTree)program.tree);
		//System.out.println("parent :"+program.start.getText());
		int number = r0.getChildCount();
		System.out.println("NUMBER : "+number+"\n");
		printTree(r0, number);
		generateFile(r0, lpn,lpnFileName);
	}

	public void generateFile(CommonTree r0, LhpnFile lpn, String lpnFileName){
		LhpnFile lpnFinal = new LhpnFile();
		

		File lpnFile = new File(".lpn");
		try {
			lpnFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			lpnFinal = generateLPN(r0, lpn, false);
			lpnFinal.addTransition("t" + numTransitions);
			numTransitions++;
			lpnFinal.addMovement("p"+(numPlaces-1),"t" +(numTransitions-1));
			lpnFinal.addMovement("t" +(numTransitions-1), pFirst); 
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lpnFinal.save(lpnFileName);

	}
	public static void printTree(CommonTree t, int number) {

		if ( t != null ) {
			StringBuffer sb = new StringBuffer(number);
			for ( int i = 0; i < number; i++ )
				sb = sb.append("   ");
			for ( int i = 0; i < t.getChildCount(); i++ ) {
				System.out.println(sb.toString() + t.getChild(i).toString());
				printTree((CommonTree)t.getChild(i), number+1);
			}
		}
	} 

	public static LhpnFile generateLPN(CommonTree t, LhpnFile lpn2, boolean recursiveCall) throws IOException {
		String enablingCond="";
		String delay="";
		String varType = " ";
		String varName = "";

		LhpnFile lpn= lpn2;
		if ( t != null ) {
			int childCount=0;
			if(recursiveCall){
				childCount=1;
			}
			else{
				childCount= t.getChildCount();
			}

			System.out.println("child count is : "+t.getChildCount());
			for(int i=0;i<childCount;i++){
				System.out.println("child is : "+t.getChild(i));	
			} 
			for(int i=0;i<childCount;i++){

				CommonTree switchCaseTree= new CommonTree();

				if(recursiveCall){
					System.out.println("Start of switch statement in recursive call:"+t);
					switchCaseTree=t;
				}
				else {
					System.out.println("Start of switch statement not in recursive call:"+t.getChild(i));
					switchCaseTree=(CommonTree)t.getChild(i);
				}
				switch(switchCaseTree.getType())
				{
				case antlrPackage.PropertyLexer.BOOLEAN : 
					varType = "boolean";
					varName = generateExpression((CommonTree)switchCaseTree.getChild(0));
					lpn.addInput(varName, varType);
					break;
				case antlrPackage.PropertyLexer.REAL : 
					varType = "real";
					 varName = generateExpression((CommonTree)switchCaseTree.getChild(0));
					lpn.addInput(varName, varType);
					break;
				case antlrPackage.PropertyLexer.INTEGER : 
					 varType = "int";
					 varName = generateExpression((CommonTree)switchCaseTree.getChild(0));
					lpn.addInput(varName, varType);
					break;
				case antlrPackage.PropertyLexer.ASSERT :
					System.out.println("Assert statement ");
					enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));
					delay= generateExpression((CommonTree)switchCaseTree.getChild(1));
					if(numPlaces==0){
						lpn.addPlace("p"+numPlaces, true);
						numPlaces++;
					}
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
					lpn.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numFailPlaces-1));
					lpn.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpn.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					break;
					
				case antlrPackage.PropertyLexer.WAIT_STABLE :
					System.out.println("child is :"+(CommonTree)switchCaseTree.getChild(0));
					enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));
					delay= generateExpression((CommonTree)switchCaseTree.getChild(1));
					if(numPlaces==0){
						lpn.addPlace("p"+numPlaces, true);
						numPlaces++;
					}
					lpn.addTransition("t" + numTransitions);
					lpn.addEnabling("t" +numTransitions, enablingCond);
					lpn.changeDelay("t" +numTransitions, delay);
					numTransitions++;
					lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
					lpn.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpn.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					lpn.getTransition("t" +(numTransitions-1)).setPersistent(true);
					break;
				case antlrPackage.PropertyLexer.ASSERT_UNTIL :
					String string1 = generateExpression((CommonTree)switchCaseTree.getChild(0));
					String string2 = generateExpression((CommonTree)switchCaseTree.getChild(1));
					enablingCond= string2;
					//delay= generateExpression((CommonTree)switchCaseTree.getChild(1));
					if(numPlaces==0){
						lpn.addPlace("p"+numPlaces, true);
						numPlaces++;
					}
					lpn.addTransition("t" + numTransitions);
					lpn.addEnabling("t" +numTransitions, enablingCond);
					
					numTransitions++;
					lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
					lpn.addTransition("tFail" + numFailTransitions);
					numFailTransitions++;
					enablingCond = "~("+string1+") & "+"~("+string2+")";
					lpn.addEnabling("tFail" +(numFailTransitions-1), enablingCond);

					
					lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
					lpn.addPlace("pFail"+numFailPlaces, false);
					numFailPlaces++;
					lpn.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numFailPlaces-1));
					lpn.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpn.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					
				break;
				case antlrPackage.PropertyLexer.ID : 
					System.out.println("Property name ");

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
					System.out.println("wait statement: ");
					int count = switchCaseTree.getChildCount();
					if (count==1){
						enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));
						
						if(numPlaces==0){
							lpn.addPlace("p"+numPlaces, true);
							numPlaces++;
						}
						lpn.addTransition("t" + numTransitions);
						lpn.addEnabling("t" +numTransitions, enablingCond);
						numTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						
						lpn.addPlace("p"+numPlaces, false);
						numPlaces++;
						lpn.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					}
					else if(count==2){
						enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));
						delay= generateExpression((CommonTree)switchCaseTree.getChild(1));

						if(numPlaces==0){
							lpn.addPlace("p"+numPlaces, true);
							numPlaces++;
						}
						lpn.addTransition("t" + numTransitions);
						lpn.addEnabling("t" +numTransitions, enablingCond);
						numTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						lpn.addTransition("tFail" + numFailTransitions);
						lpn.addEnabling("tFail" +numFailTransitions, "~"+enablingCond);
						lpn.changeDelay("tFail" +numFailTransitions, delay);
						lpn.getTransition("tFail" +numFailTransitions).setPersistent(true);
						numFailTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
						lpn.addPlace("pFail"+numFailPlaces, false);
						numFailPlaces++;
						lpn.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numFailPlaces-1));
						lpn.addPlace("p"+numPlaces, false);
						numPlaces++;
						lpn.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					} 


					break;

				case antlrPackage.PropertyLexer.IF :
					System.out.println("IF statement");
					if(list.getItemCount()!=0){
						list.removeAll();

					}
					String condition=  generateExpression((CommonTree)switchCaseTree.getChild(0));
					list.add(condition);

					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(switchCaseTree.getChild(j).getType()==antlrPackage.PropertyLexer.ELSEIF){

							condition=  generateExpression((CommonTree)switchCaseTree.getChild(j).getChild(0));
							list.add(condition);


						}
					}
					if(numPlaces==0){

						lpn.addPlace("pStart"+numStartPlaces, true);
						pFirst= "pStart"+numStartPlaces;
						numStartPlaces++;
					
						lpn.addPlace("pEnd"+numEndPlaces, false);
						numEndPlaces++;

					}
					else{
						lpn.addTransition("t" + numTransitions);
						numTransitions++;
						lpn.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						lpn.addPlace("pStart"+numStartPlaces, false);
						numStartPlaces++;
						lpn.addMovement( "t" +(numTransitions-1),"pStart"+(numStartPlaces-1));
						lpn.addPlace("pEnd"+numEndPlaces, false);
						numEndPlaces++;
					}

					for(int x=0;x<list.getItemCount();x++){

						System.out.println("list is : "+list.getItem(x)+"\n");
					}
					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(j==0){
							enablingCond=  generateExpression((CommonTree)switchCaseTree.getChild(0));
							lpn.addTransition("t" + numTransitions);
							StringBuffer sb = new StringBuffer();
							String newEnablingCond1= "";

							for(int m=0;m<list.getItemCount();m++){

								if(list.getItem(m).matches(enablingCond)){
									if(m==(list.getItemCount()-1)){
										newEnablingCond1 = "("+list.getItem(m)+")";
									}
									else{
										newEnablingCond1 = "("+list.getItem(m)+")&";
									}
								}
								else{
									if(m==(list.getItemCount()-1)){

										newEnablingCond1 = "~("+list.getItem(m)+")";
									}
									else{
										newEnablingCond1 = "~("+list.getItem(m)+")&";
									}
								}

								sb.append(newEnablingCond1);

							}
							String newEnablingCond = sb.toString();
							System.out.println("newEnablinCondition : "+newEnablingCond+"\n");

							lpn.addEnabling("t" +numTransitions, enablingCond);
							numTransitions++;
							lpn.addMovement("pStart"+(numStartPlaces-1), "t" +(numTransitions-1));
							lpn.addPlace("p"+numPlaces, false);
							numPlaces++;
							lpn.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));

						}

						else if(!(switchCaseTree.getChild(j).getType()==antlrPackage.PropertyLexer.ELSEIF) & !(switchCaseTree.getChild(j).getType()==antlrPackage.PropertyLexer.ELSE)){
							lpn=generateLPN((CommonTree)switchCaseTree.getChild(j), lpn, true);


						}

					}
					lpn.addTransition("t" + numTransitions);
					numTransitions++;
					lpn.addMovement("p"+(numPlaces-1),"t" +(numTransitions-1));
					lpn.addMovement("t" +(numTransitions-1), "pEnd"+(numEndPlaces-1));
					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(switchCaseTree.getChild(j).getType()==antlrPackage.PropertyLexer.ELSEIF){
							lpn=generateLPN((CommonTree)switchCaseTree.getChild(j), lpn, true);
							}
					}
					
					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(switchCaseTree.getChild(j).getType()==antlrPackage.PropertyLexer.ELSE){
							lpn=generateLPN((CommonTree)switchCaseTree.getChild(j), lpn, true);
							}
					}
			
					String newEnablingCond1 = "";
					StringBuffer sb = new StringBuffer();
						for(int m=0;m<list.getItemCount();m++){

								if(m==(list.getItemCount()-1)){

									newEnablingCond1 = "~("+list.getItem(m)+")";
								}
								else{
									newEnablingCond1 = "~("+list.getItem(m)+")&";
								}
						sb.append(newEnablingCond1);

						}
						String newEnablingCond = sb.toString();
						
						lpn.addTransition("t" + numTransitions);
							lpn.addEnabling("t" +numTransitions, newEnablingCond);
					numTransitions++;
						lpn.addMovement("pStart"+(numStartPlaces-1),"t" +(numTransitions-1));
					lpn.addMovement("t" +(numTransitions-1),"pEnd"+(numEndPlaces-1)); 
					
					lpn.addTransition("t" + numTransitions);
					numTransitions++;
					lpn.addMovement("pEnd"+(numEndPlaces-1),"t" +(numTransitions-1));
					
					lpn.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpn.addMovement("t" +(numTransitions-1),"p"+(numPlaces-1));
					break;
				case antlrPackage.PropertyLexer.END :
					break;
				case antlrPackage.PropertyLexer.ELSEIF :
					System.out.println("ELSEIF ");	
					for(int j=0;j<switchCaseTree.getChildCount();j++){

						if(j==0){
							enablingCond=  generateExpression((CommonTree)switchCaseTree.getChild(0));
							if(numPlaces==0){
								lpn.addPlace("p"+numPlaces, true);
								numPlaces++;
							}
							lpn.addTransition("t" + numTransitions);
							StringBuffer sb2 = new StringBuffer();
							String newEnablingCondition1= "";

							for(int m=0;m<list.getItemCount();m++){

								if(list.getItem(m).matches(enablingCond)){
									if(m==(list.getItemCount()-1)){
										newEnablingCondition1 = "("+list.getItem(m)+")";
									}
									else{
										newEnablingCondition1 = "("+list.getItem(m)+")&";
									}
								}
								else{
									if(m==(list.getItemCount()-1)){

										newEnablingCondition1 = "~("+list.getItem(m)+")";
									}
									else{
										newEnablingCondition1 = "~("+list.getItem(m)+")&";
									}
								}

								sb2.append(newEnablingCondition1);

							}
							String newEnablingCondition = sb2.toString();
							System.out.println("newEnablinCondition in ELSEIF : "+newEnablingCondition+"\n");

							lpn.addEnabling("t" +numTransitions, newEnablingCondition);
							numTransitions++;
							lpn.addMovement("pStart"+(numStartPlaces-1), "t" +(numTransitions-1));
							lpn.addPlace("p"+numPlaces, false);
							numPlaces++;
							lpn.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));
						}
						else if(switchCaseTree.getChild(j).getType()==antlrPackage.PropertyLexer.IF){
							lpn=generateLPN((CommonTree)switchCaseTree.getChild(j), lpn, true);
						}
						else{

							lpn=generateLPN((CommonTree)switchCaseTree.getChild(j), lpn, true);
						}

					}
					lpn.addTransition("t" + numTransitions);
					numTransitions++;
					lpn.addMovement("p"+(numPlaces-1),"t" +(numTransitions-1));
					lpn.addMovement("t" +(numTransitions-1), "pEnd"+(numEndPlaces-1));
					break;
				case antlrPackage.PropertyLexer.ELSE :
					System.out.println("ELSE ");	
					
					StringBuffer sb3 = new StringBuffer();
					String newEnablingCondition2= "";

					for(int m=0;m<list.getItemCount();m++){

							if(m==(list.getItemCount()-1)){

								newEnablingCondition2 = "~("+list.getItem(m)+")";
							}
							else{
								newEnablingCondition2 = "~("+list.getItem(m)+")&";
							}

						sb3.append(newEnablingCondition2);

					}
					String newEnablingCond2 = sb3.toString();
					System.out.println("newEnablinCondition in ELSE : "+newEnablingCond2+"\n");
					lpn.addTransition("t" + numTransitions);
					lpn.addEnabling("t" +numTransitions, newEnablingCond2);
					numTransitions++;
					lpn.addMovement("pStart"+(numStartPlaces-1), "t" +(numTransitions-1));
					lpn.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpn.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));
					for(int j=0;j<switchCaseTree.getChildCount();j++){

						lpn=generateLPN((CommonTree)switchCaseTree.getChild(j), lpn, true);
							//enablingCond=  generateExpression((CommonTree)switchCaseTree.getChild(0));
							if(numPlaces==0){
								lpn.addPlace("p"+numPlaces, true);
								numPlaces++;
							}
						
					}
					lpn.addTransition("t" + numTransitions);
					numTransitions++;
					lpn.addMovement("p"+(numPlaces-1),"t" +(numTransitions-1));
					lpn.addMovement("t" +(numTransitions-1), "pEnd"+(numEndPlaces-1));
					break;
				default :
					break;
				}
			}

		}
		
		return lpn;
	}

	public static String generateExpression(CommonTree newChild) {

		String result = "";
		String string1= "";
		String string2="";
		if ( newChild != null ) {

			switch (newChild.getType()) {
			case antlrPackage.PropertyLexer.WAIT: 
				break;
			case antlrPackage.PropertyLexer.IF: 
				break;
			case antlrPackage.PropertyLexer.ID : 
				result= newChild.toString();
				System.out.println("String in ID : "+result);
				break;
			case antlrPackage.PropertyLexer.FLOAT:
				result=newChild.toString();
				break;
			case antlrPackage.PropertyLexer.INT	:
				result=newChild.toString();
				System.out.println("String in INT :"+result);
				break;
			case antlrPackage.PropertyLexer.STRING	:
				result=newChild.toString();
				break;
			case antlrPackage.PropertyLexer.GET :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + ">" +string2);
				System.out.println("String in GET :"+result);
				break;
			case antlrPackage.PropertyLexer.AND :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "&" +string2);
				System.out.println("String in AND :"+result);
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
				result= ("~" +string1);
				System.out.println("String in NOT :"+result);
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
				result= (string1 + "=" +string2);
				System.out.println("String in SAMEAS :"+result);
				break;

			default :
				break;
			}

		}
		return result;
	} 

}