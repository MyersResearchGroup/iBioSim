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

import java.io.*;
import java.util.*;

import main.java.edu.utah.ece.async.util.GlobalConstants;
import main.java.edu.utah.ece.async.util.exceptions.BioSimException;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Lpn2verilog {
	//String place;

	public static void convert(String lpnFileName) throws BioSimException {
		HashMap<String,Boolean> visitedPlaces;
		String enable = "";
		String separator = GlobalConstants.separator;
		try{
			LPN lpn = new LPN();
			lpn.load(lpnFileName);
			String svFileName = lpnFileName.replaceAll(".lpn", ".sv");
			File svFile = new File(svFileName);
			svFile.createNewFile();
			String[] svPath = svFileName.split(separator);
			//for (int i=0; i < svPath.length; i++){
			//System.out.println("\nModule string is " + svPath[i] + "\n");}
			String svModuleName = svPath[svPath.length -1].split("\\.")[0];
			//System.out.println("\nModule name is " + svModuleName + "\n");
			BufferedWriter sv = new BufferedWriter(new FileWriter(svFile));
			StringBuffer initBuffer = new StringBuffer();
			StringBuffer markedPlaceBuffer = new StringBuffer();
			StringBuffer assertionBuffer = new StringBuffer();
			sv.write("`timescale 1ps/1fs\n\n");		//TODO: THIS IS ASSUMPTION
			Boolean first = true;
			String[] varsList = lpn.getVariables();
			sv.write("module "+svModuleName + " (");
			for (String v: varsList){
				if (lpn.isOutput(v)){
					if (first){
						if (lpn.isInteger(v) || lpn.isContinuous(v)){
							sv.write("output real " + v);	//TODO: Integer if dmv?
						} else{
							sv.write("output logic " + v);
						}
						first = false;
					} else{
						if (lpn.isInteger(v) || lpn.isContinuous(v)){
							sv.write(", output real " + v);
						} else{
							sv.write(", output logic " + v);
						}
					}
				}
				else if (lpn.isInput(v)){
					if (first){
						if (lpn.isInteger(v) || lpn.isContinuous(v)){
							sv.write("input real " + v);//svFileName.split("\\.")[0]+
						} else{
							sv.write("input wire " + v);//svFileName.split("\\.")[0]+
						}
						first = false;
					} else{
						if (lpn.isInteger(v) || lpn.isContinuous(v)){
							sv.write(", input real " + v);
						} else{
							sv.write(", input wire " + v);
						}
					}
				}
				//else deal with inout and internal
			}
			sv.write(");\n");
			first = true;
			String[] transitionList = lpn.getTransitionList();
			
			ArrayList<String> transArrayList = new ArrayList<String>(Arrays.asList(transitionList));
			//System.out.println("\ntransArray list is  " + transArrayList + "\n");
			Collections.sort(transArrayList,new Comparator<String>(){
				@Override
				public int compare(String a, String b){
					return(a.compareToIgnoreCase(b));
				}
			});
			transArrayList.toArray(transitionList);
			String[] placeList = lpn.getPlaceList();
			ArrayList<String> placeArrayList = new ArrayList<String>(Arrays.asList(placeList));
			Collections.sort(placeArrayList,new Comparator<String>(){
				@Override
				public int compare(String a, String b){
					return(a.compareToIgnoreCase(b));
				}
			});
			placeArrayList.toArray(placeList);
			
			initBuffer.append("\t\t$dumpfile(\"" + svModuleName + ".vcd\");\n");
			initBuffer.append("\t\t$dumpvars(0," + svModuleName + ");\n");
			for (String st: transitionList){
				if (lpn.getTransition(st).isPersistent()){ 	
						if (first){
							sv.write("\treg " + st+"p");
							first = false;
						} else{
							sv.write(", " + st);
						}

					//}
					if (!first){
						sv.write(";\n");
					}
				//}
				first = true;
				}
			}

			first = true;
			for (String st: transitionList){
				//if (!lpn.getTransition(st).isPersistent() || lpn.getTransition(st).hasConflictSet()){ 


					if (first){//System.out.println("This is a non- persistent Transition :"+st);
					sv.write("\twire " + st);
					first = false;
					} else{
						sv.write(", " + st);
					}
				//}
			}
			if (!first){
				sv.write(";\n");
			}
			first = true;
			for (String v: varsList){
				if (!lpn.isInput(v) && !lpn.isOutput(v) && !lpn.isContinuous(v)){
					if (first){
						first = false;
						sv.write("\treal " + v);
					}
					else
						sv.write("," + v);
				}
				
				else
					if (lpn.isContinuous(v)){
					if(first){
						first = false;
						sv.write("\treal  "+v+", rate_"+v); 
						}
					else sv.write(","+v+", rate_"+v);
					}
				//else {}
					//sv.write("," + v);
			}
			if (!first)
				sv.write(";\n");
			//initBuffer.append(";\n");
			HashMap<String,Integer> tag = new HashMap<String,Integer>();
			visitedPlaces = new HashMap<String,Boolean>();
			int netCount = 0;
			first = true;
			for (String v2: varsList){boolean contiCounter = false;
				if(lpn.isContinuous(v2)){
					if (!contiCounter){
					initBuffer.append("\t\t reset = 1; \n"); contiCounter= true;
					}
					String initRate = lpn.getInitialRate(v2);
					initRate = initRate.replace("[","uniform(");
					initRate = initRate.replace("]",")");
					//System.out.println("initRate = "+initRate);
					initBuffer.append("\t\t rate_"+v2+" = "+initRate+";\n");
					String initValue = lpn.getInitialVal(v2);
					initValue = initValue.replace("[","uniform(");
					initValue = initValue.replace("]",")");
					initBuffer.append("\t\t"+v2+" ="+initValue+";\n");
					}
				else {
					String initValue = lpn.getInitialVal(v2);
					initValue = initValue.replace("[","uniform(");
					initValue = initValue.replace("]",")");
					initBuffer.append("\t\t"+v2+" ="+initValue+";\n");
				}
				
					
			}
			for (String st: placeList){
				if (first){
					sv.write("\tlogic " + st);
					if (lpn.getPlace(st).isMarked()){
						initBuffer.append("\t\t" + st + " = 0");   
						
						if (markedPlaceBuffer.length() == 0)
							markedPlaceBuffer.append("\t\t#1;\n");
						markedPlaceBuffer.append("\t\t" + st + " = 1; //Initially Marked\n");
						tag = tagNet(lpn,st,netCount,tag,visitedPlaces);
						netCount++;
					}
					else
						initBuffer.append("\t\t" + st + " = 0");
					first = false;
				} else{
					sv.write(", " + st);
					if (lpn.getPlace(st).isMarked()){
						initBuffer.append("; " + st + " = 0");
						if (markedPlaceBuffer.length() == 0)
							markedPlaceBuffer.append("\t\t#1;\n");
						markedPlaceBuffer.append("\t\t" + st + " = 1; //Initially Marked\n");
						tagNet(lpn,st,netCount,tag,visitedPlaces);
						netCount++;
					}
					else
						initBuffer.append("; " + st + " = 0");
				} 
				if (lpn.getPreset(st) == null){
					//TODO: traverse all the non-repeating transitions from here and assign a tag to them   
				}
			}
			for (String v1: varsList){
				if(lpn.isContinuous(v1)){
					sv.write(", fastClk,reset");
					break;
					}
				} //new code
			if (!first)
				sv.write(";\n");

			if (!first){
				initBuffer.append(";\n");
				initBuffer.append(markedPlaceBuffer);
			}
			boolean firstCont = true;
			for (String v: varsList){
				if ((v != null) && (lpn.isOutput(v))){		//Initialize only outputs. Inputs will be initialized in their driver modules. Null condition Not Required ??
					//String initVal = lpn.getInitialVal(v);
					if (lpn.isContinuous(v) || lpn.isInteger(v)){
						if (lpn.isContinuous(v)){
							// TODO: Call Verilog-AMS generation from here. No System Verilog in this case
							//double initRate = Double.parseDouble(lpn.getInitialRate(v));
							if (firstCont){
								firstCont = false;
							//	initBuffer.append("\t\tentryTime<=$time;\n");
							//	sv.write("\treal entryTime;\n");
							}
							//sv.write("\treal rate_" + v + ", change_" + v + ";\n");
							if ((lpn.getInitialRate(v) != null) && (!lpn.getInitialRate(v).equalsIgnoreCase("unknown"))){
								//String initBufferString = getInitBufferString(v, lpn.getInitialRate(v));
								//initBuffer.append("\t\trate_" + initBufferString);
							}
							if ((lpn.getInitialVal(v) != null)  && (!lpn.getInitialVal(v).equalsIgnoreCase("unknown"))){
								//String initBufferString = getInitBufferString(v, lpn.getInitialVal(v));
							//	initBuffer.append("\t\tchange_" + initBufferString);
							}
						} else {
							if ((lpn.getInitialVal(v) != null) && (!lpn.getInitialVal(v).equalsIgnoreCase("unknown"))){
								String initBufferString = getInitBufferString(v, lpn.getInitialVal(v));
								initBuffer.append("\t\t" + initBufferString);
							}
						}
					}
					else { // boolean variable 
						String initValue = lpn.getInitialVal(v);
						if (initValue.equals("true")){
							initBuffer.append("\t\t" + v + " = 1'b1;\n");
						}
						else if (initValue.equals("false")){
							initBuffer.append("\t\t" + v + " = 1'b0;\n");
						}
						else {
							//System.out.println("WARNING: The initial value of Boolean variable " + v + " should be a boolean value.");
						}
					}
				}
			}
			sv.write("\tinitial begin\n");
			sv.write(initBuffer.toString());
			for (String v1: varsList){
				if(lpn.isContinuous(v1)){
					sv.write("\t\treset = 0;\n");
					sv.write("\tend\n\n");
					sv.write("\talways #1 fastClk = (~fastClk)&(~reset); \n\n");
					break;
				}
			}
			for (String v1: varsList){
				if(lpn.isContinuous(v1)){
					sv.write("\talways @(fastClk) begin \n");
					sv.write("\t"+v1+" <="+v1+"+rate_"+v1+";\n");
					sv.write("\tend \n\n");
					//break;
				}
			} //new code
			//sv.write("\tend\n");
			int contiCounter =0 ;
			for(String v1 :varsList){
				if (lpn.isContinuous(v1)){
					contiCounter++;
				}
			}
			if (contiCounter ==0) sv.write("\t\t end \n\n");
			
			Boolean[] firstTransition = new Boolean[netCount];
			//StringBuffer[] alwaysBuffer = new StringBuffer[netCount];
			//	StringBuffer[] prioritiesBuffer = new StringBuffer[netCount];
			StringBuffer[] assignmentsBuffer = new StringBuffer[netCount];
			for (int j = 0; j < netCount; j++){ //System.out.println("Netcount is :"+netCount);
			firstTransition[j] = true;
			//	alwaysBuffer[j] = new StringBuffer(); dec 4,2010
			//	prioritiesBuffer[j] = new StringBuffer();
			assignmentsBuffer[j] = new StringBuffer(); 
			}
			for (String st : transitionList){
				if (!lpn.getTransition(st).isPersistent()){ // System.out.println("This is a Non-Peristent transition : "+st);
					//if (transitionList.)
					sv.write("\tassign ");
					ExprTree delayTree = lpn.getTransition(st).getDelayTree();
					//System.out.println("Delay Tree :"+delayTree+"for transition :"+st);
					if (delayTree != null){
						String delay = delayTree.getElement("Verilog");
						//System.out.println("Delay "+delay);
						if (delay.contains("uniform")){ //range
							delay = delay.replaceFirst("uniform\\(", "");
							delay = delay.substring(0, delay.length()-1); //delay = delay.replace("\\)", "");
						} else {
							delay = delay + "," + delay;
						}
						sv.write("#(delay(~" + st + "," + delay + ")) " + st + " = ");

					} else{
						sv.write(st + " = ");
					}
					if (lpn.getPreset(st).length != 0){ //Assuming there's no transition without a preset.
						first = true;
						for (String st2 : lpn.getPreset(st)){ //System.out.println("getPreset :"+st2);
						if (first){
							sv.write(st2);
							first = false;
						}
						else{
							sv.write(" && " + st2);
						}

						}
						if (lpn.getEnablingTree(st) != null){
							//System.out.println("enabling");
							sv.write(" && (" + lpn.getEnablingTree(st).getElement("Verilog") + ")");
							//System.out.println(st +" enabling " + lpn.getEnablingTree(st).getElement("Verilog"));
						}
					}
					sv.write(";\n");
					if (lpn.getTransition(st).isFail()){
						assertionBuffer.append("\talways @(" + st + ") begin\n");
						assertionBuffer.append("\t\tassert(!" + st + ")\n");
						assertionBuffer.append("\t\telse\n");
						assertionBuffer.append("\t\t\t$error(\"Error! Assertion " + st + " failed at time %t\",$time);\n\tend\n");
					} else {

						assignmentsBuffer[tag.get(st)].append("\talways @(posedge " + st + ") begin\n"); //dec 4, 2010
						for (String st2 : lpn.getPreset(st)){//System.out.println(" tag.get(st) :"+tag.get(st));
						assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= 0;\n");
						}
						for (String st2 : lpn.getPostset(st)){
							assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= 1;\n");
						}
						HashMap<String,ExprTree> assignmentTrees = lpn.getTransition(st).getAssignTrees(); 
						HashMap<String,ExprTree> rateAssignmentTrees = lpn.getTransition(st).getRateAssignTrees(); 
						HashMap<String,ExprTree> valueAssignmentTrees = lpn.getTransition(st).getIntAssignTrees();
						HashMap<String,ExprTree> contAssignmentTrees = lpn.getTransition(st).getContAssignTrees(); //System.out.println("assignmentTrees.size() :"+assignmentTrees);
						if (assignmentTrees.size() != 0){
							for (String st2 : valueAssignmentTrees.keySet()){
								//System.out.println("Assignment " + st2 + " <= " + lpn.getTransition(st).getAssignTree(st2));
								String asgnmt = valueAssignmentTrees.get(st2).getElement("Verilog");
								//System.out.println("asgnmt :"+asgnmt);
								if ((asgnmt != null) && (asgnmt != ""))
									assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= " + asgnmt + ";\n");
							}
							for (String st2 : contAssignmentTrees.keySet()){
								//System.out.println("Assignment " + st2 + " <= " + lpn.getTransition(st).getAssignTree(st2));
								String asgnmt = contAssignmentTrees.get(st2).getElement("Verilog");
								if ((asgnmt != null) && (asgnmt != ""))
									assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= " + asgnmt + ";\n");
							}
							for (String st2 : rateAssignmentTrees.keySet()){//System.out.println("st2 :"+st2);
							//System.out.println("Assignment " + st2 + " <= " + lpn.getTransition(st).getAssignTree(st2));
							String asgnmt = rateAssignmentTrees.get(st2).getElement("Verilog");// System.out.println("asgnmt :"+asgnmt);
							if (asgnmt != null){
								//assignmentsBuffer[tag.get(st)].append("\t\tentryTime <= $time;\n");
								assignmentsBuffer[tag.get(st)].append("\t\trate_" + st2 + " <= " + asgnmt + ";\n");
								//assignmentsBuffer[tag.get(st)].append("\t\tchange_" + st2 + " <= " + st2 + ";\n");
							}
							}
						}
						assignmentsBuffer[tag.get(st)].append("\tend\n");
					}
				}
				else{

					//if(	lpn.getTransition(st).hasConflictSet()){ 
						//int size = 	transitionList.length;
						//System.out.println("st in conflict:"+st);
						//Transition[] trans = new Transition[5];
						//Transition[] conflictSet = new Transition[size];
						//conflictSet = lpn.getTransition(st).getConflictSet();
						//System.out.println("This is the conflict set size :"+size);
						
						
						sv.write("\tassign ");
						ExprTree delayTree = lpn.getTransition(st).getDelayTree();
						//System.out.println("Delay Tree :"+delayTree+"for transition :"+st);
						if (delayTree != null){
							String delay = delayTree.getElement("Verilog");
							//System.out.println("Delay "+delay);
							if (delay.contains("uniform")){ //range
								delay = delay.replaceFirst("uniform\\(", "");
								delay = delay.substring(0, delay.length()-1); //delay = delay.replace("\\)", "");
							} else {
								delay = delay + "," + delay;
							}
							sv.write("#(delay(~" + st + "," + delay + ")) " + st + " = ");

						} else{
							sv.write(st + " = ");
						}
						if (lpn.getPreset(st).length != 0){ //Assuming there's no transition without a preset.
							first = true;
							for (String st2 : lpn.getPreset(st)){// System.out.println("getPreset :"+st2);
							if (first){
								sv.write("("+st2);
								first = false;
							}
							else{
								sv.write(" && " + st2);
							}

							}
							sv.write(" && " +st+"p)");
							//if (lpn.getEnablingTree(st) != null){
							//System.out.println("enabling");
							//sv.write(" && (" + lpn.getEnablingTree(st).getElement("Verilog") + ")");
							//	System.out.println(st +" enabling " + lpn.getEnablingTree(st).getElement("Verilog"));
							//}
						}
						sv.write(";\n");// conflict box ends here

						HashMap<String,ExprTree> assignmentTrees = lpn.getTransition(st).getAssignTrees(); 
						HashMap<String,ExprTree> rateAssignmentTrees = lpn.getTransition(st).getRateAssignTrees(); 
						HashMap<String,ExprTree> valueAssignmentTrees = lpn.getTransition(st).getIntAssignTrees();
						HashMap<String,ExprTree> contAssignmentTrees = lpn.getTransition(st).getContAssignTrees(); //System.out.println("assignmentTrees.size() :"+assignmentTrees);


						if (lpn.getEnablingTree(st) != null){
							enable = lpn.getEnablingTree(st).getElement("Verilog");
							//System.out.println("enabling");
							//sv.write(" && (" + lpn.getEnablingTree(st).getElement("Verilog") + ")");
							//System.out.println(st +" enabling " + enable);
						}

						for (String place : lpn.getPreset(st)){ 
							//System.out.println("getPreset :"+place);
							if (enable!=null){
							assignmentsBuffer[tag.get(st)].append("\talways @(posedge ("+ enable + ") && (" +place+")) begin\n");} //dec 4, 2010//dec 4, 2010
							else assignmentsBuffer[tag.get(st)].append("\talways @(posedge " +place+") begin\n");
						}

						//ExprTree delay = lpn.getDelayTree(st);
						//System.out.println(" delay....delay :"+delay);
						for (String st2 : lpn.getPreset(st)){
						if (lpn.getEnablingTree(st) != null){
							enable = lpn.getEnablingTree(st).getElement("Verilog");
							
								
								//System.out.println(" tag.get(st) :"+tag.get(st));
								assignmentsBuffer[tag.get(st)].append("\t\t" + st + "p <=  (("+enable+") && "+st2+");\n");
								//else assignmentsBuffer[tag.get(st)].append("\t\t" + st + "p <=   "+st2+";\n");
							}
						else assignmentsBuffer[tag.get(st)].append("\t\t" + st + "p <=   "+st2+";\n");
						}
						
						assignmentsBuffer[tag.get(st)].append("\tend\n");
						
						// add 1 more normal always block here for persistent transition.
						
						if (lpn.getTransition(st).isFail()){
							assertionBuffer.append("\talways @(" + st + ") begin\n");
							assertionBuffer.append("\t\tassert(!" + st + ")\n");
							assertionBuffer.append("\t\telse\n");
							assertionBuffer.append("\t\t\t$error(\"Error! Assertion " + st + " failed at time %t\",$time);\n\tend\n");
						} else {

							assignmentsBuffer[tag.get(st)].append("\talways @(posedge " + st + ") begin\n"); //dec 4, 2010
							for (String st2 : lpn.getPreset(st)){//System.out.println(" tag.get(st) :"+tag.get(st));
							assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= 0;\n");
							}
							for (String st2 : lpn.getPostset(st)){
								assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= 1;\n");
							}
							//HashMap<String,ExprTree> assignmentTrees = lpn.getTransition(st).getAssignTrees(); 
							//HashMap<String,ExprTree> rateAssignmentTrees = lpn.getTransition(st).getRateAssignTrees(); 
							//HashMap<String,ExprTree> valueAssignmentTrees = lpn.getTransition(st).getIntAssignTrees();
							//HashMap<String,ExprTree> contAssignmentTrees = lpn.getTransition(st).getContAssignTrees(); System.out.println("assignmentTrees.size() :"+assignmentTrees);
							if (assignmentTrees.size() != 0){
								for (String st2 : valueAssignmentTrees.keySet()){
									//System.out.println("Assignment " + st2 + " <= " + lpn.getTransition(st).getAssignTree(st2));
									String asgnmt = valueAssignmentTrees.get(st2).getElement("Verilog");
									//System.out.println("asgnmt :"+asgnmt);
									if ((asgnmt != null) && (asgnmt != ""))
										assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= " + asgnmt + ";\n");
								}
								for (String st2 : contAssignmentTrees.keySet()){
									//System.out.println("Assignment " + st2 + " <= " + lpn.getTransition(st).getAssignTree(st2));
									String asgnmt = contAssignmentTrees.get(st2).getElement("Verilog");
									if ((asgnmt != null) && (asgnmt != ""))
										assignmentsBuffer[tag.get(st)].append("\t\t" + st2 + " <= " + asgnmt + ";\n");
								}
								for (String st2 : rateAssignmentTrees.keySet()){//System.out.println("st2 :"+st2);
								//System.out.println("Assignment " + st2 + " <= " + lpn.getTransition(st).getAssignTree(st2));
								String asgnmt = rateAssignmentTrees.get(st2).getElement("Verilog"); //System.out.println("asgnmt :"+asgnmt);
								if (asgnmt != null){
									assignmentsBuffer[tag.get(st)].append("\t\tentryTime <= $time;\n");
									assignmentsBuffer[tag.get(st)].append("\t\trate_" + st2 + " <= " + asgnmt + ";\n");
									assignmentsBuffer[tag.get(st)].append("\t\tchange_" + st2 + " <= " + st2 + ";\n");
								}
								}
							}
							assignmentsBuffer[tag.get(st)].append("\tend\n");
						}
					//}

				}
			}
			if (transitionList.length > 0){
				if (assertionBuffer.length()!= 0){
					sv.write(assertionBuffer.toString());
				}
				for (int j = 0; j < netCount; j++){
					//	if ((alwaysBuffer[j] != null) && (alwaysBuffer[j].length() != 0)){ //dec 4,2010
					if ((assignmentsBuffer[j] != null) && (assignmentsBuffer[j].length() != 0)){ //dec 4,2010	
						//		alwaysBuffer[j].append(") begin\n"); // dec 4,2010
						//		sv.write(alwaysBuffer[j].toString()); // dec 4,2010
						//		sv.write(prioritiesBuffer[j].toString());
						//		sv.write("\t\tprMax" + j + " = pr" + j + ".max[0];\n");
						//		sv.write("\t\tif (prMax" + j + " == 0)\n\t\t\tprMax" + j + "=1;\n");
						sv.write(assignmentsBuffer[j].toString());
						//		sv.write("\tend\n"); //dec 4,2010
					}
				}
			}
			sv.write("\tfunction real uniform(int a, int b);\n");
			sv.write("\t\treal c;\n");
			sv.write("\t\tif (a==b)\n");
			sv.write("\t\t\treturn a;\n");
			sv.write("\t\tif ((a>0)&&(b>0))\n");
			sv.write("\t\t\tc = $urandom_range(a*1000,b*1000)/1000.0;\n");
			sv.write("\t\telse\n");
			sv.write("\t\t\tc = a+$urandom_range(b*1000-a*1000)/1000.0;\n");
			sv.write("\t\treturn c;\n");
			sv.write("\tendfunction\n");
			sv.write("\tfunction real delay(bit tb, int l, int u);\n");
			sv.write("\t\tif (~tb)\n\t\t\treturn 0.0;\n");
			sv.write("\t\telse if (l == u)\n\t\t\treturn (u + 0.001*$urandom_range(1,100));\n");
			sv.write("\t\telse return(uniform(l,u) + 0.001*$urandom_range(1,100));\n");
			sv.write("\tendfunction\n");
			sv.write("endmodule");
			sv.close();
			//}
		} catch (IOException e){
			e.printStackTrace();
			//System.out.println("ERROR: Verilog file could not be created/written.");
		}
	}

	private static String getInitBufferString(String v, String initValue) {
		// Assign initial values/rates to continuous, discrete and boolean variables
		//		As per translator.java: Extract the lower and upper bounds and set the initial value to the mean. 
		//		Anything that involves infinity, take either the lower or upper bound which is not infinity.  
		//		If both are infinity, set to 0.

		String initBufferString = null;
		String tmp_initValue = initValue;
		String[] subString = initValue.split(",");
		String lowerBound = "0";
		String upperBound = "inf";
		if (tmp_initValue.contains(",")){
			tmp_initValue = tmp_initValue.replaceFirst(",", "");
			for (int i = 0; i<subString.length; i ++)
			{
				if (subString[i].contains("[")){
					lowerBound = subString[i].replace("[", "");	
				} 
				else if (subString[i].contains("uniform(")){
					lowerBound = subString[i].replace("uniform(", "");	
				}
				else if(subString[i].contains("]")){
					upperBound = subString[i].replace("]", "");
				}
				else if(subString[i].contains(")")){
					upperBound = subString[i].replace(")", "");
				}
			}
			// initial value involves infinity
			if (lowerBound.contains("inf") || upperBound.contains("inf")){
				if (lowerBound.contains("-inf") && upperBound.contains("inf")){
					initValue = "0" ; // if [-inf,inf], initValue = 0
				}
				else if (lowerBound.contains("-inf") && !upperBound.contains("inf")){
					initValue = upperBound; // if [-inf,a], initValue = a
				}
				else if (!lowerBound.contains("-inf") && upperBound.contains("inf")){
					initValue = lowerBound; // if [a,inf], initValue = a
				}
				initBufferString = v + " = " + initValue + ";\n";
			}
			else { // initial value is a range, not involving infinity
				if (!lowerBound.equalsIgnoreCase(upperBound)){
					if (Double.valueOf(lowerBound) > 0)
						initBufferString = v + " = " + lowerBound + " + $signed((($unsigned($random))%(" + upperBound + "-" + lowerBound + "+1)));\n";
					else if (Double.valueOf(lowerBound) < 0)
						initBufferString = v + " = " + lowerBound + " + $signed((($unsigned($random))%(" + upperBound + "+" + Math.abs(Integer.valueOf(lowerBound)) + "+1)));\n";
					else
						initBufferString = v + " = " + lowerBound + " + $signed((($unsigned($random))%(" + upperBound + "+1)));\n";
				}
				else
					initBufferString = v + " = " + lowerBound + ";\n";
			}	
		} 
		else { // initial rate is a single number
			initBufferString = v + " = " + initValue + ";\n";
		}
		return(initBufferString);
	}

	private static HashMap<String,Integer> tagNet(LPN g, String place, int id, HashMap<String,Integer> tag,
			HashMap<String,Boolean> visitedPlaces){
		//System.out.println("Place is :"+place);
		if (!visitedPlaces.containsKey(place)){
			visitedPlaces.put(place,true);
			for (String postsetTrans : g.getPostset(place)){
				//System.out.println("postset : "+postsetTrans);
				tag.put(postsetTrans, id);
				//System.out.println("Tagged transition " + postsetTrans + " with " + id);
				for (String postsetPlace : g.getPostset(postsetTrans)){// System.out.println("postsetPlace :"+postsetPlace);
				tag = tagNet(g, postsetPlace, id, tag, visitedPlaces);
				}
			}
		}
		//System.out.println("Hello");
		return tag;
	}
}