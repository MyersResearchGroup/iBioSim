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
package edu.utah.ece.async.dataModels.lpn.parser.properties;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

import edu.utah.ece.async.dataModels.lpn.parser.LPN;
import edu.utah.ece.async.dataModels.lpn.parser.Translator;
import edu.utah.ece.async.dataModels.lpn.parser.Variable;
import edu.utah.ece.async.dataModels.util.exceptions.BioSimException;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class BuildProperty {
	//public static JFrame frame;
	static int numPlaces=0;
	static int numTransitions=0;
	static int numFailTransitions=0;
	static int numFailPlaces=0;
	static int numStartPlaces=0;
	static int numEndPlaces=0;
	static String pFirst = "p0";
	static String pLast ="";
	static boolean loop = false;

	static List list = new List();
	public static void buildProperty(String propFileName) throws IOException, RecognitionException, BioSimException {

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



		LPN lpn = new LPN();
		//lpn.load(propFileName);

		String lpnFileString= propFileName.substring(0, propFileName.length()-4);
		String lpnFileName = lpnFileString.concat("lpn");
		File lpnFile = new File(lpnFileName);
		lpnFile.createNewFile();

		//String[] lpnPath = lpnFileName.split(separator);
		//System.out.println("No of places : "+numPlaces);

		//System.out.println("No of places : "+numPlaces);

		BufferedReader input = new BufferedReader(new FileReader(propFileName));

		String line = input.readLine();

		//StringBuffer sb2 = new StringBuffer(line);
		StringBuffer sb = new StringBuffer(line);
		//LhpnFile lpn = new LhpnFile();

		while(line!=null){

			line=input.readLine();
			sb.append(line);

		}
		input.close();

		String  property = sb.toString();
		//System.out.println("property: "+property+"\n");
		CharStream charStream = new ANTLRStringStream(property);
		PropertyLexer lexer = new PropertyLexer(charStream);
		TokenStream tokenStream =  new CommonTokenStream(lexer);
		PropertyParser parser = new PropertyParser(tokenStream);
		PropertyParser.program_return program;
		try {
			program = parser.program();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//JOptionPane.showMessageDialog(Gui.frame, "Error parsing property file, check console.", "Parse Error",  JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (parser.getNumberOfSyntaxErrors()>0) {
		  throw new BioSimException("Error parsing property file, check console.", "Parse Error");
		}
		//System.out.println("tree: "+((Tree)program.tree).toStringTree()+"\n");

		CommonTree r0 = program.tree;
		//System.out.println("parent :"+program.start.getText());
		//int number = r0.getChildCount();
		//System.out.println("NUMBER : "+number+"\n");
		//printTree(r0, number);
		generateFile(r0, lpn,lpnFileName);
	}

	public static void generateFile(CommonTree r0, LPN lpn, String lpnFileName) throws BioSimException{
		LPN lpnFinal = new LPN();
		File lpnFile = new File(".lpn");
		try {
			lpnFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			lpnFinal = generateLPN(r0, lpn, false, null);
			if(loop){
				lpnFinal.addTransition("t" + numTransitions);
				lpnFinal.changeDelay("t" + numTransitions, "0");
				numTransitions++;
				lpnFinal.addMovement(pLast,"t" +(numTransitions-1));
				lpnFinal.addMovement("t" +(numTransitions-1), pFirst); 
				loop=false;
			} 

		} catch (IOException e) {
			e.printStackTrace();
		}
		lpnFinal.save(lpnFileName);
		Translator t1 = new Translator();
		t1.convertLPN2SBML(lpnFileName, "");
		t1.setFilename(lpnFileName.replace(".lpn", ".xml"));
		t1.outputSBML();
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

	public static LPN generateLPN(CommonTree t, LPN lpn2, boolean recursiveCall, String stayConditions) throws IOException {

		String enablingCond="";
		String delay="";
		String varType = " ";
		String varName = "";

		LPN lpnObj= lpn2;
		if ( t != null ) {
			int childCount=0;
			if(recursiveCall){
				childCount=1;
			}
			else{
				childCount= t.getChildCount();
			}

			//System.out.println("child count is : "+t.getChildCount());
			for(int i=0;i<childCount;i++){
				//System.out.println("child is : "+t.getChild(i));	
			} 
			for(int i=0;i<childCount;i++){

				CommonTree switchCaseTree= new CommonTree();

				if(recursiveCall){
					//System.out.println("Start of switch statement in recursive call:"+t);
					switchCaseTree=t;
				}
				else {
					//System.out.println("Start of switch statement not in recursive call:"+t.getChild(i));
					switchCaseTree=(CommonTree)t.getChild(i);
				}
				switch(switchCaseTree.getType())
				{

				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.SENALWAYS :


					int senalways_pFirst;
					if (numPlaces == 0) {
						senalways_pFirst = 0;
					}
					else{
						senalways_pFirst = (numPlaces-1);
					}

					// Create a variable for the sensitivity list.
					String[] sensitivityList = null;
					String[] sensitivityVariables = null;

					// Check if senalways has any children.
					if(switchCaseTree.getChildCount() > 0){

						// Check if the zero-th child is a sensitivity list.
						CommonTree theList = (CommonTree) switchCaseTree.getChild(0);
						if(theList.getType() ==
								edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.LPARA){

							sensitivityList = new String[theList.getChildCount()];
							sensitivityVariables = new String[theList.getChildCount()];

							// Loop through the list gathering the elements.
							int elementCount = 0;
							for(Object obj : theList.getChildren()){

								CommonTree listElement = (CommonTree) obj;

								sensitivityList[elementCount] = 
										generateExpression(listElement);

								String storedVariableType = lpnObj.getVariable(
										sensitivityList[elementCount]).getType();

								String storedVariable = "_" + sensitivityList[elementCount];

								// Add the stay condition.
								if(stayConditions != null & elementCount > 0){
									stayConditions += "&";
								}else{
									stayConditions = "";
								}
								stayConditions += "(" + sensitivityList[elementCount] + "="
										+ storedVariable + ")";

								sensitivityVariables[elementCount] = storedVariable;

								if(storedVariableType == "boolean"){
									//									lpnObj.addBoolean(sensitivityVariables[elementCount], "0");
									lpnObj.addInput(sensitivityVariables[elementCount], storedVariableType, "0");
								}
								else if (storedVariableType == "integer"){
									lpnObj.addInteger(sensitivityVariables[elementCount], "0");
								}
								else if (storedVariableType == "discrete"){
									lpnObj.addInteger(sensitivityVariables[elementCount], "0");
								}
								else if (storedVariableType == "continuous"){
									Properties initCond = new Properties();
									initCond.put("value", "0");
									initCond.put("rate", "0");
									lpnObj.addContinuousInput(sensitivityVariables[elementCount], initCond);
								}

								elementCount++;
							}

							// Remove the sensitivity list.
							switchCaseTree.deleteChild(0);

							if(numPlaces == 0){
								lpnObj.addPlace("p" + 0, true);
								numPlaces++;
							}

							// Create the transition that stores the variables in the the list.
							lpnObj.addTransition("t" + numTransitions);
							lpnObj.changeDelay("t" + numTransitions, "0");
							numTransitions++;
							lpnObj.addPlace("p" + numPlaces, false);
							numPlaces++;
							lpnObj.addMovement("p" + senalways_pFirst, "t" + (numTransitions-1));
							lpnObj.addMovement("t" + (numTransitions-1), "p" + (numPlaces-1));

							for(int k=0; k<sensitivityVariables.length; k++){
								lpnObj.addIntAssign("t" + (numTransitions-1),
										sensitivityVariables[k], sensitivityList[k]);
							}
							senalways_pFirst = (numPlaces-1);
						}
					}


					//					System.out.println("senalways child count is: " + senalways_count);

					lpnObj = generateLPN(switchCaseTree, lpnObj, false, stayConditions);

					//					String senalways_pLast = "p" + (numPlaces-1);
					int senalways_pLast = (numPlaces -1);

					//					loop=true;

					//					if(loop){
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					if(stayConditions != null){
						lpnObj.addEnabling("t" + numTransitions, stayConditions);
					}
					numTransitions++;
					//						lpnObj.addMovement(pLast,"t" +(numTransitions-1));
					lpnObj.addMovement("p" + senalways_pLast,"t" +(numTransitions-1));
					//						lpnObj.addMovement("t" +(numTransitions-1), pFirst); 
					lpnObj.addMovement("t" +(numTransitions-1), "p" + senalways_pFirst); 
					//					    loop=false;
					//						}


					pLast = "p" + (numPlaces);
					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;

					if(sensitivityList != null && sensitivityVariables != null){

						// Create the enabling condition.
						enablingCond = "~(" + sensitivityVariables[0]
								+ " = " + sensitivityList[0] + ")";

						for(int k=1; k<sensitivityVariables.length; k++){
							enablingCond += "& ~(" + sensitivityVariables[k]
									+ " = " + sensitivityList[k] + ")";
						}

						for(int k=0; k <= senalways_pLast - senalways_pFirst; k++){
							lpnObj.addTransition("t" + numTransitions);
							//							lpnObj.addEnabling("t" + numTransitions,
							//									"~("  + sensitivityVariables[0] +
							//									"=" + sensitivityList[0] + ")");
							//							enablingCond = "~( " + sensitivityVariables[0] + " = " + sensitivityList[0] + " )";
							//							enablingCond = "senalways2a1";
							//							enablingCond = sensitivityVariables[0];
							lpnObj.addEnabling("t" + numTransitions, enablingCond);
							lpnObj.changeDelay("t" + numTransitions, "0");

							lpnObj.addMovement("p" + (senalways_pFirst+k), "t" + numTransitions);
							lpnObj.addMovement("t" + numTransitions, pLast);

							numTransitions++;
						}
					}
					break;

				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ALWAYS : 
					//System.out.println("child count of always : "+switchCaseTree.getChildCount());
					//					if(numPlaces==0) pFirst="p0";
					//					else pFirst = "p"+(numPlaces-1);

					String always_pFirst;
					if (numPlaces == 0){
						always_pFirst = "p0";
					}
					else{
						always_pFirst = "p"+(numPlaces-1);
					}

					//System.out.println("pFirst is :"+pFirst);
					//for(int q=0; q<switchCaseTree.getChildCount();q++){
					lpnObj = generateLPN(switchCaseTree, lpnObj, false, stayConditions);
					//	}

					String always_pLast = "p"+(numPlaces-1);

					//					pLast="p"+(numPlaces-1);
					loop=true;

					if(loop){
						lpnObj.addTransition("t" + numTransitions);
						lpnObj.changeDelay("t" + numTransitions, "0");
						numTransitions++;
						//						lpnObj.addMovement(pLast,"t" +(numTransitions-1));
						lpnObj.addMovement(always_pLast,"t" +(numTransitions-1));
						//						lpnObj.addMovement("t" +(numTransitions-1), pFirst); 
						lpnObj.addMovement("t" +(numTransitions-1), always_pFirst); 
						loop=false;
					}


					pLast = "p" + (numPlaces);
					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;

					break; 
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.RCURL :
					pLast= "p"+(numPlaces-1);
					//loop=false;
					//System.out.println("pLast is :"+pLast);
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.BOOLEAN : 
					varType = "boolean";
					varName = generateExpression((CommonTree)switchCaseTree.getChild(0));
					lpnObj.addInput(varName, varType, "0");
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.REAL : 
					varType = "continuous";
					varName = generateExpression((CommonTree)switchCaseTree.getChild(0));
					//					lpnObj.addInput(varName, varType);
					lpnObj.addInput(varName, varType, "0");
					Variable newVar = lpnObj.getVariable(varName);
					newVar.addInitRate("0");
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.INTEGER : 
					varType = "integer";
					varName = generateExpression((CommonTree)switchCaseTree.getChild(0));
					lpnObj.addInput(varName, varType, "0");
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ASSERT :
					//System.out.println("Assert statement ");
					enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));

					//System.out.println("Assert statement enabling cond :"+enablingCond);
					delay= generateExpression((CommonTree)switchCaseTree.getChild(1));
					if(numPlaces==0){
						lpnObj.addPlace("p"+numPlaces, true);
						numPlaces++;
					}
					lpnObj.addTransition("t" + numTransitions);
					if(stayConditions != null){
						lpnObj.addEnabling("t" +numTransitions, 
								"(" + enablingCond + ")&" + stayConditions);
					}else{
						lpnObj.addEnabling("t" +numTransitions, enablingCond);
					}
					lpnObj.changeDelay("t" +numTransitions, delay);
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
					lpnObj.addTransition("tFail" + numFailTransitions);
					lpnObj.changeDelay("tFail" + numFailTransitions, "0");
					lpnObj.getTransition("tFail" + numFailTransitions).setFail(true);
					if(stayConditions != null){
						lpnObj.addEnabling("tFail" +numFailTransitions, 
								"~("+enablingCond+")&" + stayConditions);
					}else{
						lpnObj.addEnabling("tFail" +numFailTransitions, "~("+enablingCond+")");
					}

					numFailTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
					lpnObj.addPlace("pFail"+numFailPlaces, false);
					numFailPlaces++;
					lpnObj.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numFailPlaces-1));
					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ASSERT_STABLE :

					break;	
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.WAIT_STABLE :
					//System.out.println("child is :"+(CommonTree)switchCaseTree.getChild(0));
					enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));
					delay= generateExpression((CommonTree)switchCaseTree.getChild(1));
					if(numPlaces==0){
						lpnObj.addPlace("p"+numPlaces, true);
						numPlaces++;
					}
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.addEnabling("t" +numTransitions, enablingCond);
					lpnObj.changeDelay("t" +numTransitions, delay);
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					lpnObj.getTransition("t" +(numTransitions-1)).setPersistent(true);
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ASSERT_UNTIL :
					String string1 = generateExpression((CommonTree)switchCaseTree.getChild(0));
					String string2 = generateExpression((CommonTree)switchCaseTree.getChild(1));
					enablingCond= string2;
					if(numPlaces==0){
						lpnObj.addPlace("p"+numPlaces, true);
						numPlaces++;
					}
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.addEnabling("t" +numTransitions, enablingCond);
					lpnObj.changeDelay("t" + numTransitions, "0");

					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
					lpnObj.addTransition("tFail" + numFailTransitions);
					lpnObj.changeDelay("tFail" + numFailTransitions, "0");
					lpnObj.getTransition("tFail" + numFailTransitions).setFail(true);
					numFailTransitions++;
					enablingCond = "~("+string1+") & "+"~("+string2+")";
					lpnObj.addEnabling("tFail" +(numFailTransitions-1), enablingCond);


					lpnObj.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
					lpnObj.addPlace("pFail"+numFailPlaces, false);
					numFailPlaces++;
					lpnObj.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numFailPlaces-1));
					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));

					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ID : 
					//System.out.println("Property name ");
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.INT :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.NOT :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.PLUS :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.MINUS :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.MULT :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.DIV :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.MOD :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.EQUAL :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.NOT_EQUAL :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.GET :

					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.LET :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.GETEQ :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.LETEQ :
					break;

				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.AND :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.OR :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.SAMEAS :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.WAIT :
					//System.out.println("wait statement: ");
					int count = switchCaseTree.getChildCount();
					if (count==1){


						enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));

						if(numPlaces==0){
							lpnObj.addPlace("p"+numPlaces, true);
							numPlaces++;
						}
						lpnObj.addTransition("t" + numTransitions);
						lpnObj.changeDelay("t" + numTransitions, "0");;
						if(stayConditions != null){
							lpnObj.addEnabling("t" +numTransitions,
									"(" + enablingCond + ")&" + stayConditions);
						}else{
							lpnObj.addEnabling("t" +numTransitions, enablingCond);
						}
						numTransitions++;
						lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));

						lpnObj.addPlace("p"+numPlaces, false);
						numPlaces++;
						lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));


					}
					else if(count==2){
						enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));
						delay= generateExpression((CommonTree)switchCaseTree.getChild(1));

						if(numPlaces==0){
							lpnObj.addPlace("p"+numPlaces, true);
							numPlaces++;
						}
						lpnObj.addTransition("t" + numTransitions);
						lpnObj.changeDelay("t" + numTransitions, "0");
						if(stayConditions != null){
							lpnObj.addEnabling("t" +numTransitions,
									"(" + enablingCond + ")&" + stayConditions);
						}else{
							lpnObj.addEnabling("t" +numTransitions, enablingCond);
						}
						numTransitions++;
						lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						lpnObj.addTransition("tFail" + numFailTransitions);
						lpnObj.getTransition("tFail" + numFailTransitions).setFail(true);
						if(stayConditions != null){
							lpnObj.addEnabling("tFail" +numFailTransitions,
									"~("+enablingCond+")&" + stayConditions);
						}else{
							lpnObj.addEnabling("tFail" +numFailTransitions, "~("+enablingCond+")");
						}
						lpnObj.changeDelay("tFail" +numFailTransitions, delay);

						numFailTransitions++;
						lpnObj.addMovement("p"+(numPlaces-1), "tFail" +(numFailTransitions-1));
						lpnObj.addPlace("pFail"+numFailPlaces, false);
						numFailPlaces++;
						lpnObj.addMovement( "tFail" +(numFailTransitions-1),"pFail"+(numFailPlaces-1));
						lpnObj.addPlace("p"+numPlaces, false);
						numPlaces++;
						lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					} 


					break;

				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.WAIT_DELAY:
					enablingCond = "t";
					delay = generateExpression((CommonTree)switchCaseTree.getChild(0));

					if(numPlaces==0){
						lpnObj.addPlace("p"+numPlaces, true);
						numPlaces++;
					}

					lpnObj.addTransition("t" + numTransitions);
					lpnObj.addEnabling("t" +numTransitions, enablingCond);
					lpnObj.changeDelay("t" +numTransitions, delay);
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));

					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));

					break;

				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.IF :
					boolean elsePartExists = false;
					//System.out.println("IF statement");
					if(list.getItemCount()!=0){
						list.removeAll();

					}
					String condition=  generateExpression((CommonTree)switchCaseTree.getChild(0));
					list.add(condition);

					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(switchCaseTree.getChild(j).getType()==edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ELSEIF){

							condition=  generateExpression((CommonTree)switchCaseTree.getChild(j).getChild(0));
							list.add(condition);


						}
					}
					if(numPlaces==0){

						lpnObj.addPlace("pStart"+numStartPlaces, true);
						pFirst= "pStart"+numStartPlaces;
						numStartPlaces++;

						lpnObj.addPlace("pEnd"+numEndPlaces, false);
						numEndPlaces++;

					}
					else{
						pFirst= "p0";
						lpnObj.addTransition("t" + numTransitions);
						lpnObj.changeDelay("t" + numTransitions, "0");
						numTransitions++;
						lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));
						lpnObj.addPlace("pStart"+numStartPlaces, false);
						numStartPlaces++;
						lpnObj.addMovement( "t" +(numTransitions-1),"pStart"+(numStartPlaces-1));
						lpnObj.addPlace("pEnd"+numEndPlaces, false);
						numEndPlaces++;
					}

					for(int x=0;x<list.getItemCount();x++){

						//System.out.println("list is : "+list.getItem(x)+"\n");
					}
					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(j==0){
							enablingCond=  generateExpression((CommonTree)switchCaseTree.getChild(0));
							lpnObj.addTransition("t" + numTransitions);
							lpnObj.changeDelay("t" + numTransitions, "0");
							StringBuffer sb = new StringBuffer();
							String newEnablingCond1= "";

							for(int m=0;m<list.getItemCount();m++){

								if(list.getItem(m).toString().equalsIgnoreCase(enablingCond)){
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
							//String newEnablingCond = sb.toString();
							//System.out.println("newEnablinCondition : "+newEnablingCond+"\n");

							lpnObj.addEnabling("t" +numTransitions, enablingCond);
							numTransitions++;
							lpnObj.addMovement("pStart"+(numStartPlaces-1), "t" +(numTransitions-1));
							lpnObj.addPlace("p"+numPlaces, false);
							numPlaces++;
							lpnObj.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));

						}

						else if(!(switchCaseTree.getChild(j).getType()==edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ELSEIF) & !(switchCaseTree.getChild(j).getType()==edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ELSE)){
							lpnObj=generateLPN((CommonTree)switchCaseTree.getChild(j), lpnObj, true, stayConditions);


						}

					}
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1),"t" +(numTransitions-1));
					lpnObj.addMovement("t" +(numTransitions-1), "pEnd"+(numEndPlaces-1));
					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(switchCaseTree.getChild(j).getType()==edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ELSEIF){
							lpnObj=generateLPN((CommonTree)switchCaseTree.getChild(j), lpnObj, true, stayConditions);
						}
					}

					for(int j=0;j<switchCaseTree.getChildCount();j++){
						if(switchCaseTree.getChild(j).getType()==edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ELSE){
							elsePartExists = true;
							lpnObj=generateLPN((CommonTree)switchCaseTree.getChild(j), lpnObj, true, stayConditions);
						}
					}

					if(!elsePartExists){
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
						//System.out.println(" condition is :"+newEnablingCond);
						lpnObj.addTransition("t" + numTransitions);
						lpnObj.addEnabling("t" +numTransitions, newEnablingCond);
						numTransitions++;
						lpnObj.addMovement("pStart"+(numStartPlaces-1),"t" +(numTransitions-1));
						lpnObj.addMovement("t" +(numTransitions-1),"pEnd"+(numEndPlaces-1)); 
					}
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					numTransitions++;
					lpnObj.addMovement("pEnd"+(numEndPlaces-1),"t" +(numTransitions-1));

					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement("t" +(numTransitions-1),"p"+(numPlaces-1));
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.END :
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ELSEIF :
					//System.out.println("ELSEIF ");	
					for(int j=0;j<switchCaseTree.getChildCount();j++){

						if(j==0){
							enablingCond=  generateExpression((CommonTree)switchCaseTree.getChild(0));
							//System.out.println("enabling condition :"+enablingCond);
							if(numPlaces==0){
								lpnObj.addPlace("p"+numPlaces, true);
								numPlaces++;
							}
							lpnObj.addTransition("t" + numTransitions);
							lpnObj.changeDelay("t" + numTransitions, "0");
							StringBuffer sb2 = new StringBuffer();
							String newEnablingCondition1= "";
							int counter=-1;
							for(int m=0;m<list.getItemCount();m++){
								//System.out.println("item :"+list.getItem(m).toString());

								if(list.getItem(m).toString().equalsIgnoreCase(enablingCond)){
									counter=m;

								}
							}
							//System.out.println("counter is : "+counter);

							for(int m=0;m<=counter;m++){
								//System.out.println("item :"+list.getItem(m).toString());

								if(list.getItem(m).toString().equalsIgnoreCase(enablingCond)){

									newEnablingCondition1 = "("+list.getItem(m)+")";

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
							//System.out.println("newEnablinCondition in ELSEIF : "+newEnablingCondition+"\n");

							lpnObj.addEnabling("t" +numTransitions, newEnablingCondition);
							numTransitions++;
							lpnObj.addMovement("pStart"+(numStartPlaces-1), "t" +(numTransitions-1));
							lpnObj.addPlace("p"+numPlaces, false);
							numPlaces++;
							lpnObj.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));
						}
						else if(switchCaseTree.getChild(j).getType()==edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.IF){
							lpnObj=generateLPN((CommonTree)switchCaseTree.getChild(j), lpnObj, true, stayConditions);
						}
						else{

							lpnObj=generateLPN((CommonTree)switchCaseTree.getChild(j), lpnObj, true, stayConditions);
						}

					}
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1),"t" +(numTransitions-1));
					lpnObj.addMovement("t" +(numTransitions-1), "pEnd"+(numEndPlaces-1));
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ELSE :
					//System.out.println("ELSE ");	

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
					//System.out.println("newEnablinCondition in ELSE : "+newEnablingCond2+"\n");
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					lpnObj.addEnabling("t" +numTransitions, newEnablingCond2);
					numTransitions++;
					lpnObj.addMovement("pStart"+(numStartPlaces-1), "t" +(numTransitions-1));
					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement("t" +(numTransitions-1), "p"+(numPlaces-1));
					for(int j=0;j<switchCaseTree.getChildCount();j++){

						lpnObj=generateLPN((CommonTree)switchCaseTree.getChild(j), lpnObj, true, stayConditions);
						//enablingCond=  generateExpression((CommonTree)switchCaseTree.getChild(0));
						if(numPlaces==0){
							lpnObj.addPlace("p"+numPlaces, true);
							numPlaces++;
						}

					}
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1),"t" +(numTransitions-1));
					lpnObj.addMovement("t" +(numTransitions-1), "pEnd"+(numEndPlaces-1));
					break;
				case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.WAIT_POSEDGE :
					enablingCond=  generateExpression((CommonTree)switchCaseTree.getChild(0));
					//System.out.println("");

					enablingCond= generateExpression((CommonTree)switchCaseTree.getChild(0));

					if(numPlaces==0){
						lpnObj.addPlace("p"+numPlaces, true);
						numPlaces++;
					}
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					if (stayConditions!=null) {
						lpnObj.addEnabling("t" +numTransitions, "~("+enablingCond+")" + "&" + stayConditions);
					} else {
						lpnObj.addEnabling("t" +numTransitions, "~("+enablingCond+")");
					}
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));

					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					lpnObj.addTransition("t" + numTransitions);
					lpnObj.changeDelay("t" + numTransitions, "0");
					if (stayConditions!=null) {
						lpnObj.addEnabling("t" +numTransitions, "(" + enablingCond + ")" + "&" + stayConditions);
					} else {
						lpnObj.addEnabling("t" +numTransitions, "(" + enablingCond + ")");
					}
					numTransitions++;
					lpnObj.addMovement("p"+(numPlaces-1), "t" +(numTransitions-1));

					lpnObj.addPlace("p"+numPlaces, false);
					numPlaces++;
					lpnObj.addMovement( "t" +(numTransitions-1),"p"+(numPlaces-1));
					break;
				default :
					break;
				}
			}

		}

		return lpnObj;
	}

	public static String generateExpression(CommonTree newChild) {

		String result = "";
		String string1= "";
		String string2="";
		if ( newChild != null ) {

			switch (newChild.getType()) {
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.WAIT: 
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.IF: 
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.ID : 
				result= newChild.toString();
				//System.out.println("String in ID : "+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.FLOAT:
				result=newChild.toString();
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.INT	:
				result=newChild.toString();
				//System.out.println("String in INT :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.STRING	:
				result=newChild.toString();
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.WAIT_POSEDGE	:
				result=generateExpression((CommonTree)newChild.getChild(0));
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.GET :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + ">" +string2);
				//System.out.println("String in GET :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.AND :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= ("("+string1 + ")&(" +string2+")");
				//result= (string1 + "&" +string2);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.DIV :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "/" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.EQUAL :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "=" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.GETEQ :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + ">=" +string2);

				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.LET :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "<" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.LETEQ :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "<=" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.MINUS :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "-" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.MOD :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "%" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.MULT :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "*" +string2);
				//System.out.println("result2 :"+result);
				break;

			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.NOT :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				result= ("~(" +string1+")");
				//result= "~"+string1;
				//System.out.println("String in NOT :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.NOT_EQUAL :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "!=" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.OR :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "|" +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.PLUS :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + " + " +string2);
				//System.out.println("result2 :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.SAMEAS :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= (string1 + "=" +string2);
				//System.out.println("String in SAMEAS :"+result);
				break;

			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.LPARA :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= "(";
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.RPARA :
				string1= generateExpression((CommonTree)newChild.getChild(0));
				string2= generateExpression((CommonTree)newChild.getChild(1));
				result= ")";
				//System.out.println("String in SAMEAS :"+result);
				break;
			case edu.utah.ece.async.dataModels.lpn.parser.properties.PropertyLexer.UNIFORM :
				result= newChild.toString();
				//System.out.println("String in UNIFORM :"+result);
				break;
			default :
				break;
			}

		}
		return result;
	} 

}