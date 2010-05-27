package lhpn2sbml.parser;

import java.io.*;
import java.util.*;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JOptionPane;


public class Lpn2verilog {
	private String separator;
	
	public Lpn2verilog(String lpnFileName) {
		try{
			if (File.separator.equals("\\")) {
				separator = "\\\\";
			}
			else {
				separator = File.separator;
			}
			LhpnFile lpn = new LhpnFile();
			lpn.load(lpnFileName);
			String svFileName = lpnFileName.replaceAll(".lpn", ".sv");
			File svFile = new File(svFileName);
			svFile.createNewFile();
			String[] svPath = svFileName.split(separator);
			String svModuleName = svPath[svPath.length -1].split("\\.")[0];
			//System.out.println("\nModule name is " + svModuleName + "\n");
			BufferedWriter sv = new BufferedWriter(new FileWriter(svFile));
			StringBuffer initBuffer = new StringBuffer();
			sv.write("`timescale 1ps/1ps\n\n");		//TODO: THIS IS ASSUMPTION
			Boolean first = true;
			String[] varsList = lpn.getVariables();
			for (String v: varsList){
				if (lpn.isOutput(v)){
					if (first){
						if (!lpn.isBoolean(v)){
							sv.write("module "+svModuleName+" (output real " + v);	//TODO: Integer if dmv?
						} else{
							sv.write("module "+svModuleName+" (output reg " + v);
						}
						first = false;
					} else{
						if (!lpn.isBoolean(v)){
							sv.write(", output real " + v);
						} else{
							sv.write(", output reg " + v);
						}
					}
				}
				else if (lpn.isInput(v)){
					if (first){
						if (!lpn.isBoolean(v)){
							sv.write("module "+svFileName.split("\\.")[0]+" (input real " + v);
						} else{
							sv.write("module "+svFileName.split("\\.")[0]+" (input wire " + v);
						}
						first = false;
					} else{
						if (!lpn.isBoolean(v)){
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
			//Sorting works only if places are like p0,p1.. and transitions are like t0,t1..
			ArrayList<String> transArrayList = new ArrayList(Arrays.asList(transitionList));
			Collections.sort(transArrayList,new Comparator<String>(){
				public int compare(String a, String b){
					String v1 = a.split("t")[1];
					String v2 = b.split("t")[1];
					if (Integer.parseInt(v1) < Integer.parseInt(v2)){
						return -1;
					}
					else if (Integer.parseInt(v1) == Integer.parseInt(v2)){
						return 0;
					}
					else{
						return 1;
					}
				}
			});
			transArrayList.toArray(transitionList);
			String[] placeList = lpn.getPlaceList();
			ArrayList<String> placeArrayList = new ArrayList(Arrays.asList(placeList));
			Collections.sort(placeArrayList,new Comparator<String>(){
				public int compare(String a, String b){
					String v1 = a.split("p")[1];
					String v2 = b.split("p")[1];
					if (Integer.parseInt(v1) < Integer.parseInt(v2)){
						return -1;
					}
					else if (Integer.parseInt(v1) == Integer.parseInt(v2)){
						return 0;
					}
					else{
						return 1;
					}
				}
			});
			placeArrayList.toArray(placeList);
			/*for (String st: transitionList){
				System.out.print(st + " ");
			}
			System.out.println();
			for (String st: placeList){
				System.out.print(st + " ");
			}*/
			for (String st: transitionList){
				if (first){
					sv.write("\twire[31:0] " + st);
			//		initBuffer.append("\t\t" + st + " <= 0"); As transitions are wires, no initial values to them
					first = false;
				} else{
					sv.write(", " + st);
			//		initBuffer.append("; " + st + " <= 0");
				}
			}
			sv.write(";\n\tinteger tmax=-1");
			for (String v: varsList){
				if (!lpn.isInput(v) && !lpn.isOutput(v))
					sv.write("," + v);
			}
			sv.write(";\n");
			//initBuffer.append(";\n");
			first = true;
			for (String st: placeList){
				if (first){
					sv.write("\treg " + st);
					if (lpn.getPlace(st).isMarked())
						initBuffer.append("\t\t" + st + " <= 1");
					else
						initBuffer.append("\t\t" + st + " <= 0");
					first = false;
				} else{
					sv.write(", " + st);
					if (lpn.getPlace(st).isMarked())
						initBuffer.append("; " + st + " <= 1");
					else
						initBuffer.append("; " + st + " <= 0");
				}
			}
			sv.write(";\n");
			initBuffer.append(";\n");
			for (String v: varsList){
				if (v != null){		//Not Required ??
					String initVal = lpn.getInitialVal(v);
					if (lpn.isContinuous(v) || lpn.isInteger(v)){
						if (lpn.isContinuous(v)){
							// Generate Verilog-AMS and not System Verilog in this case
							double initRate = Double.parseDouble(lpn.getInitialRate(v));
							//TODO: rate stuff etc. here..
						}

						// Assign initial values to continuous, discrete and boolean variables
//						As per translator.java: Extract the lower and upper bounds and set the initial value to the mean. 
//						Anything that involves infinity, take either the lower or upper bound which is not infinity.  
//						If both are infinity, set to 0.	
						String initValue = lpn.getInitialVal(v);
						String tmp_initValue = initValue;
						String[] subString = initValue.split(",");
						String lowerBound = null;
						String upperBound = null;
						if (tmp_initValue.contains(",")){
							tmp_initValue = tmp_initValue.replaceFirst(",", "");
							for (int i = 0; i<subString.length; i ++)
							{
								if (subString[i].contains("[")){
									lowerBound = subString[i].replace("[", "");	
								}
								if (subString[i].contains("uniform(")){
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
								initBuffer.append("\t\t" + v + " <= " + initValue + ";\n");
							}
							else { // initial value is a range, not involving infinity
								if (!lowerBound.equalsIgnoreCase(upperBound))
									initBuffer.append("\t\t" + v + " <= " + lowerBound + " + (($unsigned($random))%(" + upperBound + "-" + lowerBound + "+1));\n");
								else
									initBuffer.append("\t\t" + v + " <= " + lowerBound + ";\n");
							}	
						} 
						else { // initial value is a single number
							initBuffer.append("\t\t" + v + " <= " + initValue + ";\n");
						}
					}
					else { // boolean variable 
						String initValue = lpn.getInitialVal(v);
						if (initValue.equals("true")){
							initBuffer.append("\t\t" + v + " <= 1'b1;\n");
						}
						else if (initValue.equals("false")){
							initBuffer.append("\t\t" + v + " <= 1'b0;\n");
						}
						else {
							System.out.println("WARNING: The initial value of Boolean variable " + v + " should be a boolean value.");
						}
					}
				}
			}
			sv.write("\tinitial begin\n");
			sv.write(initBuffer.toString());
			sv.write("\tend\n");
			for (String st : transitionList){
				sv.write("\tassign ");
				String delay = lpn.getTransition(st).getDelay();
				if (delay != null){
					System.out.println(st + " delay " + lpn.getTransition(st).getDelay());
					if (delay.contains(",")){
						delay = delay.replace("uniform(","");
						delay = delay.replace(")","");
						String[] delayBounds = delay.split(",");
						System.out.println("delay bounds are " + delayBounds[0] + "," + delayBounds[1]);
						if (delayBounds.length > 2){
							System.out.println("Error in delay assignments. Considering required part only");
						}
						if (delayBounds[0].equalsIgnoreCase(delayBounds[1])){
							sv.write("#(" + st + "==-1 ? " + delayBounds[0] + " : 0) " + st + " = ");
						} else{
							sv.write("#(" + st + "==-1 ? " + delayBounds[0] +" + (($unsigned($random))%(" + delayBounds[1] + "-" + delayBounds[0]+ "+1)) : 0) " + st + " = ");
						}
					} else{
						sv.write("#(" + st + "==-1 ? " + delay + " : 0) " + st + " = ");
					}
					
				} else{
					sv.write(st + " = ");
				}
				if (lpn.getPreset(st).length != 0){ //Assuming there's no transition without a preset.
					first = true;
					for (String st2 : lpn.getPreset(st)){
						if (first){
							sv.write("((" + st2);
							first = false;
						}
						else{
							sv.write(" && " + st2);
						}
					}
					if (lpn.getEnablingTree(st) != null){
						sv.write(" && (" + lpn.getEnablingTree(st).getElement("Verilog") + ")");
						System.out.println(st + " enabling " + lpn.getEnablingTree(st).getElement("Verilog"));
					}
					sv.write(")?$unsigned($random):-1)");
				}
				sv.write(" ;\n");
				sv.write("\talways @(" + st + ") begin\n\t\tif (" + st + " == tmax) begin\n" );
				for (String st2 : lpn.getPreset(st)){
					sv.write("\t\t\t" + st2 + " <= 0;\n");
				}
				for (String st2 : lpn.getPostset(st)){
					sv.write("\t\t\t" + st2 + " <= 1;\n");
				}
				HashMap<String,String> assignments = lpn.getTransition(st).getAssignments(); 
				if (assignments.size() != 0){
					for (String st2 : assignments.keySet()){
						System.out.println("Assignment " + st2 + " <= " + lpn.getTransition(st).getAssignTree(st2));
						String asgnmt = assignments.get(st2);
						if (asgnmt.contains(",")){
							asgnmt = asgnmt.replace("uniform(","");
							asgnmt = asgnmt.replace(")","");
							String[] asgnmtBounds = asgnmt.split(",");
							System.out.println("asgnmt bounds are " + asgnmtBounds[0] + "," + asgnmtBounds[1]);
							if (asgnmtBounds.length > 2){
								System.out.println("Error in value assignments. Considering required part only");
							}
							if (asgnmtBounds[0].equalsIgnoreCase(asgnmtBounds[1])){
								sv.write("\t\t\t" + st2 + " <= " + asgnmtBounds[0] + ";\n");
							} else{
								sv.write("\t\t\t" + st2 + " <= " + asgnmtBounds[0] +" + (($unsigned($random))%(" + asgnmtBounds[1] + "-" + asgnmtBounds[0]+ "+1));\n");
							}
						} else{
							sv.write("\t\t\t" + st2 + " <= " + asgnmt + ";\n");
						}
					}
				}
				sv.write("\t\tend\n\tend\n");
			}
			if (transitionList.length > 0){
				sv.write("\talways@(");
				first = true;
				for (String st : transitionList){
					if (first){
						sv.write(st);
						first = false;
					}
					else
						sv.write("," + st);
				}
				sv.write(") begin\n");
				for (String st : transitionList){
					if (first){
						first = false;
						sv.write("\t\ttmax = "+st+";");
					}
					else{
						sv.write("\t\tif(tmax < " + st + ")\n\t\t\ttmax = " + st + ";\n");
					}
				}
				sv.write("\tend\n");
			}
			sv.write("endmodule");
			sv.close();
		} catch (IOException e){
			e.printStackTrace();
			System.out.println("ERROR: Verilog file could not be created/written.");
		}
	}
}
