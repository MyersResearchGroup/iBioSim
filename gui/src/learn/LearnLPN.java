package learn;

//import gcm2sbml.parser.GCMFile;
import lhpn2sbml.parser.LhpnFile;
//import lhpn2sbml.parser.ExprTree;
import lhpn2sbml.parser.Lpn2verilog;
import parser.*;
//import java.awt.*;
//import java.awt.List;
//import java.awt.event.*;
import java.io.*;
import java.util.*;
//import java.util.regex.*;
//import java.util.regex.Pattern;

import javax.swing.*;

//import org.sbml.libsbml.*;
import biomodelsim.*;

/**
* This class is meant for learning LPNs from simulation data. 
* It is mostly the same as LearnLHPN class, except that this is 
* not tied to GUI. This could have been a single method in LearnLHPN 
* class but since most of the variables are global, this is done as 
* a separate class.
* 
* @author Satish Batchu
*/
public class LearnLPN extends JPanel {

	private static final long serialVersionUID = -5806315070287184299L;

	private String directory;

//	private Log log;

	private String separator;

	private BioSim biosim;

	private String lhpnFile;

	private ArrayList<Variable> reqdVarsL;

	private ArrayList<Integer> reqdVarIndices;

	private ArrayList<ArrayList<Double>> data;

	private ArrayList<String> varNames;

	private int[][] bins;

	private HashMap<String, ArrayList<Double>> thresholds;

	private Double[][] rates;
	
	private double[][] values;

	private Double[] duration;

	private int pathLength ; //= 7 ;// intFixed 25 pd 7 integrator 15;

	private int rateSampling ; //= -1 ; //intFixed 250; 20; //-1;

	private LhpnFile g;

	private Integer numPlaces = 0, numTransitions = 0;

	private HashMap<String, Properties> placeInfo, transitionInfo;
	
	private HashMap<String, Properties> cvgInfo;

	private Double minDelayVal = 10.0, minRateVal = 10.0, minDivisionVal = 10.0;

	private Double delayScaleFactor, valScaleFactor;

	BufferedWriter out;

	File logFile;

	// Threshold parameters
	private double epsilon ;//= 0.1; // What is the +/- epsilon where signals are considered to be equivalent

	private Integer runLength ; //= 15; // the number of time points that a value must persist to be considered constant

	private Double runTime ; // = 5e-12; // 10e-6 for intFixed; 5e-6 for integrator. 5e-12 for pd;// the amount of time that must pass to be considered constant when using absoluteTime

	private double percent ; // = 0.8; // a decimal value representing the percent of the total trace that must be constant to qualify to become a DMVC var

	private String[] transEnablingsVHDL;
	
	private String[][] transIntAssignVHDL;
	
	private String[] transDelayAssignVHDL;
	
	private String[] transEnablingsVAMS;
	
	private String[] transConditionalsVAMS;
		
	private String[][] transIntAssignVAMS;
	
	private String[] transDelayAssignVAMS;
	
	private String failPropVHDL;

	private HashMap<String, Properties> transientNetPlaces;

	private HashMap<String, Properties> transientNetTransitions;

	private ArrayList<String> ratePlaces;

	private boolean vamsRandom = false;
	
	private HashMap<String,ArrayList<Double>> dmvcValuesUnique;
	
	// Pattern lParenR = Pattern.compile("\\(+"); //SB
	
	//Pattern floatingPointNum = Pattern.compile(">=(-*[0-9]+\\.*[0-9]*)"); //SB

	// Pattern falseR = Pattern.compile("false",Pattern.CASE_INSENSITIVE); //pass the I flag to be case insensitive

	/**
	 * This is the constructor for the Learn class. It initializes all the input
	 * fields, puts them on panels, adds the panels to the frame, and then
	 * displays the frame.
	 */
	public LhpnFile learnLPN(String directory, Log log, BioSim biosim, int moduleNumber, HashMap<String, ArrayList<Double>> thresholds, HashMap<String,Double> tPar, ArrayList<Variable> reqdVarsL, ArrayList<String> varNames, Double valScaleFactor, Double delayScaleFactor) {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}

		//this.log = log;
		this.directory = directory;
		this.biosim = biosim;
		this.reqdVarsL = reqdVarsL;
		this.thresholds = thresholds;
		this.valScaleFactor = valScaleFactor;
		this.delayScaleFactor = delayScaleFactor;
		this.varNames = varNames;
		
		String[] getFilename = directory.split(separator);
		lhpnFile = getFilename[getFilename.length - 1] + moduleNumber + ".lpn";

		epsilon = tPar.get("epsilon");
		pathLength = (int) tPar.get("pathLength").doubleValue();
		rateSampling = (int) tPar.get("rateSampling").doubleValue();
		percent = tPar.get("percent");
		if (tPar.containsKey("runTime")){
			runTime = tPar.get("runTime");
			runLength = null;
		} else{ 
			runLength = (int) tPar.get("runLength").doubleValue();
			runTime = null;
		}
		new File(directory + separator + lhpnFile).delete();
		try {
			logFile = new File(directory + separator + "run.log");
//			logFile = new File(directory + separator + "run" + moduleNumber + ".log");
//			logFile.createNewFile();
			out = new BufferedWriter(new FileWriter(logFile,true)); //appending
//				File lhpn = new File(directory + separator + lhpnFile);
//				lhpn.delete();
			/* Initializations being done in resetAll method added on Aug 12,2009. These 
			 * initializations ensure that place,transition numbers start from 0 
			 * everytime we click play button on LEMA though compiled only once. 
			 * Init values and rates being cleared for the same reason. 
			 */
			numPlaces = 0;
			numTransitions = 0;
			out.write("Running: dataToLHPN for module " + moduleNumber +  "\n");
			TSDParser tsd = new TSDParser(directory + separator + "run-1.tsd",
					biosim, false);
			varNames = tsd.getSpecies();
			File cvgFile = new File(directory + separator + "run.cvg");
			cvgFile.createNewFile();
			BufferedWriter coverage = new BufferedWriter(new FileWriter(cvgFile));
			int i = 1;

			g = new LhpnFile(); // The generated lhpn is stored in this object
			placeInfo = new HashMap<String, Properties>();
			transitionInfo = new HashMap<String, Properties>();
			cvgInfo = new HashMap<String, Properties>();
			transientNetPlaces = new HashMap<String, Properties>();
			transientNetTransitions = new HashMap<String, Properties>();
			ratePlaces = new ArrayList<String>();
			while (new File(directory + separator + "run-" + i + ".tsd").exists()) {
				Properties cProp = new Properties();
				cvgInfo.put(String.valueOf(i), cProp);
				cProp.setProperty("places", String.valueOf(0));
				cProp.setProperty("transitions", String.valueOf(0));
				cProp.setProperty("rates", String.valueOf(0));
				cProp.setProperty("delays", String.valueOf(0));
				tsd = new TSDParser(directory + separator + "run-" + i + ".tsd", biosim,false);
				data = tsd.getData();
//ORDER REVERSED this first then detectDMV				genBinsRates(divisionsL); // changes made here.. data being used was global before.
				findReqdVarIndices();
				genBinsRates(thresholds); // moved this above detectDMV on May 11,2010 assuming this order reversal won't affect things.
				detectDMV(data,false); // changes made here.. data being used was global before.
				updateGraph(bins, rates, cProp);
				//cProp.store(coverage,  "run-" + String.valueOf(i) + ".tsd");
				coverage.write("run-" + String.valueOf(i) + ".tsd\t");
				coverage.write("places : " + cProp.getProperty("places"));
				coverage.write("\ttransitions : " + cProp.getProperty("transitions") + "\n");
				i++;
			}
			coverage.close();
			for (String st1 : g.getTransitionList()) {
				out.write("\nTransition is " + st1);
				if (isTransientTransition(st1)){
					out.write(" Incoming place " + g.getPreset(st1)[0]);
					if (g.getPostset(st1).length != 0){
						out.write(" Outgoing place " + g.getPostset(st1)[0]);
					}
					continue;
				}
				String binEncoding = getPlaceInfoIndex(g.getPreset(st1)[0]);
				out.write(" Incoming place " + g.getPreset(st1)[0]
						+ " Bin encoding is " + binEncoding);
				if (g.getPostset(st1).length != 0){
					binEncoding = getPlaceInfoIndex(g.getPostset(st1)[0]);
					out.write(" Outgoing place " + g.getPostset(st1)[0]
						+ " Bin encoding is " + binEncoding);
				}
			}
			out.write("\nTotal no of transitions : " + numTransitions);
			out.write("\nTotal no of places : " + numPlaces);
			Properties initCond = new Properties();
			for (Variable v : reqdVarsL) {
				if (v.isDmvc()) {
					g.addInteger(v.getName(), v.getInitValue());
				} else {
					initCond.put("value", v.getInitValue());
					initCond.put("rate", v.getInitRate());
					g.addContinuous(v.getName(), initCond);
				}
			}
			//g.addOutput("fail", "false");
			HashMap<String, ArrayList<Double>> scaledThresholds; // scaledThresholds are scaleDiv
			scaledThresholds = normalize();
			initCond = new Properties(); 
			for (Variable v : reqdVarsL) {	// Updating with scaled initial values & rates
				if (v.isDmvc()) {
					g.changeIntegerInitCond(v.getName(), v.getInitValue());
				} else {
					initCond.put("value", v.getInitValue());
					initCond.put("rate", v.getInitRate());
					g.changeContInitCond(v.getName(), initCond);
				}
			}
			String[] transitionList = g.getTransitionList();
			transEnablingsVHDL = new String[transitionList.length];
			transDelayAssignVHDL = new String[transitionList.length];
			transIntAssignVHDL = new String[transitionList.length][reqdVarsL.size()];
			transEnablingsVAMS = new String[transitionList.length];
			transConditionalsVAMS = new String[transitionList.length];
			transIntAssignVAMS = new String[transitionList.length][reqdVarsL.size()];
			transDelayAssignVAMS = new String[transitionList.length];
			int transNum;
			for (String t : transitionList) {
				transNum = Integer.parseInt(t.split("t")[1]);
				if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
					if (!isTransientTransition(t)){
						if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
							// g.getPreset(t).length != 0 && g.getPostset(t).length != 0 ??
							ArrayList<Integer> diffL = diff(getPlaceInfoIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
							String condStr = "";
							transEnablingsVHDL[transNum] = "";
							transEnablingsVAMS[transNum] = "";
							String[] binIncoming = getPlaceInfoIndex(g.getPreset(t)[0]).split(",");
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split(",");
							Boolean firstInputBinChg = true;
							Boolean opChange = false, ipChange = false;
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									// the above condition means that if the bin change is on a non-input dmv variable, there won't be any enabling condition
									ipChange = true;
									if (Integer.parseInt(binIncoming[k]) < Integer.parseInt(binOutgoing[k])) {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binIncoming[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
											transEnablingsVHDL[transNum] += reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
											transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
											transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") >= " + ((int)val)/valScaleFactor +"))";
										}
										else{
											condStr += "&(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
											transEnablingsVHDL[transNum] += " and " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
											transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
											transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") >= " + ((int)val)/valScaleFactor +"))";
										}
									} else {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
											transEnablingsVHDL[transNum] += "not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
											transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
											transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") < " + ((int)val)/valScaleFactor +"))";
										}
										else{
											condStr += "&~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
											transEnablingsVHDL[transNum] += "and not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
											transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
											transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") < " + ((int)val)/valScaleFactor +"))";
										}
									}
									//if (diffL.get(diffL.size() - 1) != k) {
									//	condStr += "&";
									//	transEnablingsVHDL[transNum] += " and ";
										////COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									//}
									firstInputBinChg = false;
								}
								// Enablings Till above.. Below one is dmvc delay,assignment. Whenever a transition's preset and postset places differ in dmvc vars, then this transition gets the assignment of the dmvc value in the postset place and delay assignment of the preset place's duration range. This has to be changed after taking the causal relation input
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									opChange = true;
									String pPrev = g.getPreset(t)[0];
									String nextPlace = g.getPostset(t)[0];
									//if (!isTransientTransition(t)){
							//		int mind = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin")));
							//		int maxd = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax")));
									int mind = (int) Math.floor(Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin")));
									int maxd = (int) Math.ceil(Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax")));
									if (mind != maxd)
										g.changeDelay(t, "uniform(" + mind + "," + maxd + ")");
									else
										g.changeDelay(t, String.valueOf(mind));
									int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))); 
									if (minv != maxv)
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"uniform(" + minv  + ","+ maxv + ")");
									else
										g.addIntAssign(t,reqdVarsL.get(k).getName(), String.valueOf(minv));
									int dmvTnum =  Integer.parseInt(t.split("t")[1]);
									transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
									transDelayAssignVHDL[dmvTnum] = "delay(" + mind + "," + maxd + ")";
									if (!vamsRandom){
										transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = "+ ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor)+";\n";
										//transDelayAssignVAMS[dmvTnum] = "#" + (int)(((Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
										transDelayAssignVAMS[dmvTnum] = "#" + (int)((mind + maxd)/(2*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}
									else{
										transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = $dist_uniform(seed,"+ String.valueOf((int)Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))/valScaleFactor)) + "," + String.valueOf((int)Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))/valScaleFactor))+");\n";
										//transDelayAssignVAMS[dmvTnum] = "del = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMin")))/delayScaleFactor)*Math.pow(10, 12)) + "," +(int)Math.ceil((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(pPrev)).getProperty("dMax"))/delayScaleFactor)*Math.pow(10, 12)) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
										transDelayAssignVAMS[dmvTnum] = "del = $dist_uniform(seed," + (int) (mind/delayScaleFactor) + "," +(int) (maxd/delayScaleFactor) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
									}
									/*}
									else{
										g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + "]");
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ","+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + "]");
										int dmvTnum =  Integer.parseInt(t.split("t")[1]);
										transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
										transDelayAssignVHDL[dmvTnum] = "delay(" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + ")";
										transIntAssignVAMS[dmvTnum][k] = ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor);
										transDelayAssignVAMS[dmvTnum] =  (int)(((Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}*/
								}
							}
							if (ipChange & opChange) // Both ip and op changes on same transition. Then delay should be 0. Not the previous bin duration.
								g.changeDelay(t, "0");
							if (diffL.size() > 1){
								transEnablingsVHDL[transNum] = "(" + transEnablingsVHDL[transNum] + ")";
							}
							if ((transEnablingsVAMS[transNum] != "") && (transEnablingsVAMS[transNum] != null)){ 
								transEnablingsVAMS[transNum] += ")";
							}
							if (!condStr.equalsIgnoreCase("")){
								//condStr += enFailAnd;
								g.addEnabling(t, condStr);
							}
							else{
								//Nothing added to Enabling condition.
								//condStr = enFail;
								//g.addEnabling(t, condStr);
							}
							//transEnablingsVHDL[transNum] += enFailAnd;
						}
					}
					else{
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							ArrayList<Integer> diffL = diff(getTransientNetPlaceIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
							String condStr = "";
							transEnablingsVHDL[transNum] = "";
							transEnablingsVAMS[transNum] = "";
							String[] binIncoming = getTransientNetPlaceIndex(g.getPreset(t)[0]).split(",");
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split(",");
							Boolean firstInputBinChg = true;
							Boolean opChange = false, ipChange = false;
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									ipChange = true;
									if (Integer.parseInt(binIncoming[k]) < Integer.parseInt(binOutgoing[k])) {
										//	double val = divisionsL.get(k).get(Integer.parseInt(binIncoming[k])).doubleValue();
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binIncoming[k])).doubleValue();
										if (firstInputBinChg){
										condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
										transEnablingsVHDL[transNum] += reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
										transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
										}
										else{
											condStr += "&(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
											transEnablingsVHDL[transNum] += " and " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.floor(val)+".0)";	
											transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
										}
									} else {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])).doubleValue();
										if (firstInputBinChg){
										condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
										transEnablingsVHDL[transNum] += " and not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
										transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
										}
										else{
											condStr += "&~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.ceil(val) + ")";
											transEnablingsVHDL[transNum] += "not " + reqdVarsL.get(k).getName() + "'above(" + (int) Math.ceil(val)+".0)";
											transEnablingsVAMS[transNum] = " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
										}
									}
									//if (diffL.get(diffL.size() - 1) != k) {
									//	condStr += "&";
									//	transEnablingsVHDL[transNum] += " and ";
									//	//COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									//}
									firstInputBinChg = false;
								}
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									opChange = true;
									String pPrev = g.getPreset(t)[0];
									String nextPlace = g.getPostset(t)[0];
									//int mind = (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin")));
									//int maxd = (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax")));
									int mind = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin")));
									int maxd = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMax")));
									if (mind != maxd)
										g.changeDelay(t, "uniform(" + mind + "," + maxd + ")");
									else
										g.changeDelay(t, String.valueOf(mind));
									int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax")));
									if (minv != maxv)
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"uniform(" + minv + ","+ maxv + ")");
									else
										g.addIntAssign(t,reqdVarsL.get(k).getName(),String.valueOf(minv));
									int dmvTnum =  Integer.parseInt(t.split("t")[1]);
									transIntAssignVHDL[dmvTnum][k] = reqdVarsL.get(k).getName() +" => span(" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ".0,"+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + ".0)";
									transDelayAssignVHDL[dmvTnum] = "delay(" + mind + "," + maxd + ")";
									if (!vamsRandom){
										transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = "+ ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor)+";\n";;
										//											transDelayAssignVAMS[dmvTnum] =  "#" + (int)(((Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
										transDelayAssignVAMS[dmvTnum] =  "#" + (int)((mind + maxd)/(2*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}
									else{
										transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = $dist_uniform(seed,"+ String.valueOf(Math.floor((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))/valScaleFactor))) + "," + String.valueOf(Math.ceil((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))/valScaleFactor)))+");\n";
										//transDelayAssignVAMS[dmvTnum] =  "del = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin")))/delayScaleFactor)*Math.pow(10, 12)) + "," + (int)Math.ceil((Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))/delayScaleFactor)*Math.pow(10, 12)) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
										transDelayAssignVAMS[dmvTnum] =  "del = $dist_uniform(seed," + (int) (mind/delayScaleFactor) + "," + (int) (maxd/delayScaleFactor) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
									}
								}
							}
							if (ipChange & opChange) // Both ip and op changes on same transition. Then delay should be 0. Not the previous bin duration.
								g.changeDelay(t, "0");
							if (diffL.size() > 1){
								transEnablingsVHDL[transNum] = "(" + transEnablingsVHDL[transNum] + ")";
							}
							if ((transEnablingsVAMS[transNum] != "") && (transEnablingsVAMS[transNum] != null)){ 
								transEnablingsVAMS[transNum] += ")";
							}
							if (!condStr.equalsIgnoreCase("")){
								//condStr += enFailAnd;
								g.addEnabling(t, condStr);
							}
							else{
								//condStr = enFail;
								//g.addEnabling(t, condStr);
							}
							//transEnablingsVHDL[transNum] += enFailAnd;
							
						}
					}
				}
				//DIFFERENT FROM GUI
//				if ((g.getPreset(t) != null) && (!isTransientTransition(t)) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("PROP"))){
//					g.addEnabling(t, failProp);
//				}
			}
			ArrayList<String> placesWithoutPostsetTrans = new ArrayList<String>();
			for (String st1 : g.getPlaceList()) {
				if (g.getPostset(st1).length == 0){ // a place without a postset transition
					placesWithoutPostsetTrans.add(st1);
				} else if (g.getPostset(st1).length > 1){
					HashMap<String,Boolean> varsInEnabling = new HashMap<String,Boolean>();
					for (String st2 : g.getPostset(st1)){
						if (g.getEnablingTree(st2) != null){
							for (String st3 : g.getEnablingTree(st2).getVars()){
								varsInEnabling.put(st3, true);
							}
						}
					}
					for (String st2 : g.getPostset(st1)){
						if (varsInEnabling.keySet().size() >= 1){ // && (g.getEnablingTree(st2) != null)
							String[] binOutgoing = getPlaceInfoIndex(g.getPostset(st2)[0]).split(",");
							String condStr = "";
							for (String st : varsInEnabling.keySet()){
								int bin = Integer.valueOf(binOutgoing[findReqdVarslIndex(st)]);
								if (bin == 0){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "~(" + st + ">=" + (int) Math.ceil(scaledThresholds.get(st).get(bin).doubleValue()) + ")";
								} else if (bin == (scaledThresholds.get(st).size())){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st + ">="	+ (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")";
								} else{
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")&~(" + st + ">=" + (int) Math.ceil(scaledThresholds.get(st).get(bin).doubleValue()) + ")";
								}
							}
							out.write("Changed enabling of " + st2 + " to " + condStr + "\n");
							g.addEnabling(st2, condStr);
						}
					}
				/*	for (String st2 : g.getPostset(st1)){
						ExprTree enableTree = g.getEnablingTree(st2);
						if (enableTree != null){	// If enabling of a transition is null then it's obviously not mutually exclusive of any other parallel transitions from that place
							for (String st3 : varsInEnabling.keySet()){
								// TODO: CHECK THE BIN CHANGES HERE AND ADD ENABLING CONDITIONS
								if (!enableTree.containsVar(st3)){
								//	System.out.println("At place " + st1 + " for transition " + st2 + ",Get threshold of " + st3);
									visitedPlaces = new HashMap<String,Boolean>();
									String completeEn =traceBack(st1,st3);
									System.out.println("At place " + st1 + " for transition " + st2 + ",Get threshold of " + st3+ " from " + completeEn);
									Pattern enPatternParan = Pattern.compile(".*?(~?\\(" + st3+ ".*?\\)*)[a-zA-Z]*.*");
									Pattern enPattern;
									Matcher enMatcher;
									//Pattern enPatternNoParan = Pattern.compile(".*?(~?\\(?" + st3+ ".*?\\)*)[a-zA-Z]*.*"); 
									Matcher enMatcherParan = enPatternParan.matcher(completeEn);
									if (enMatcherParan.find()) {
										enPattern = Pattern.compile(".*?(~?\\(" + st3+ ".*?\\)).*?");
										System.out.println("Matching for pattern " + enPattern.toString());
										enMatcher = enPattern.matcher(completeEn);
										String enCond = enMatcher.group(1);
										System.out.println("Extracted " +enCond);
									} else {
										enPattern = Pattern.compile(".*?(" + st3+ ".*?)[a-zA-Z]*.*?");
										System.out.println("Matching for pattern " + enPattern.toString());
										enMatcher = enPattern.matcher(completeEn);
										String enCond = enMatcher.group(1);
										System.out.println("Extracted " +enCond);
									}
								}
							}
						}
					}*/
				}
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						if (g.getPreset(st1).length != 0){
							for (String t : g.getPreset(st1)) {
								for (int k = 0; k < reqdVarsL.size(); k++) {
									if (!reqdVarsL.get(k).isDmvc()) {
										int minr = getMinRate(p, reqdVarsL.get(k).getName());
										int maxr = getMaxRate(p, reqdVarsL.get(k).getName());
										if (minr != maxr)
											g.addRateAssign(t, reqdVarsL.get(k).getName(), "uniform("	+ minr	+ "," + maxr + ")");
										else
											g.addRateAssign(t, reqdVarsL.get(k).getName(), String.valueOf(minr));
									}
								}
							}
						}
					}
				}
			}
			for (String st1 : transientNetTransitions.keySet()){
				String s = g.getPostset("t" + transientNetTransitions.get(st1).getProperty("transitionNum"))[0];
				// check TYPE of preset ????
				String p = getPlaceInfoIndex(s);
				if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
					for (int k = 0; k < reqdVarsL.size(); k++) {
						if (!reqdVarsL.get(k).isDmvc()) {
							//	out.write("<" + t	+ "=["	+ reqdVarsL.get(k).getName() + ":=["	+ getMinRate(p, reqdVarsL.get(k).getName())	+ "," + getMaxRate(p, reqdVarsL.get(k).getName()) + "]]>");
							int minr = getMinRate(p, reqdVarsL.get(k).getName());
							int maxr = getMaxRate(p, reqdVarsL.get(k).getName());
							if (minr != maxr)
								g.addRateAssign("t" + transientNetTransitions.get(st1).getProperty("transitionNum"), reqdVarsL.get(k).getName(), "uniform("	+ minr	+ "," + maxr + ")");
							else
								g.addRateAssign("t" + transientNetTransitions.get(st1).getProperty("transitionNum"), reqdVarsL.get(k).getName(), String.valueOf(minr));
						}
					}
				}
			}
			for (String st1 : placesWithoutPostsetTrans){
				placeInfo.remove(getPlaceInfoIndex(st1));
				g.removePlace(st1);
			}
			out.close();
	//		addMetaBins();
	//		addMetaBinTransitions();
			g.save(directory + separator + lhpnFile);
		//	writeVHDLAMSFile(lhpnFile.replace(".lpn",".vhd"));
		//	writeVerilogAMSFile(lhpnFile.replace(".lpn",".vams"));
			new Lpn2verilog(directory + separator + lhpnFile); //writeSVFile(directory + separator + lhpnFile);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN file couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch (NullPointerException e4) {
			e4.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN file couldn't be created/written. Null exception",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch (ArrayIndexOutOfBoundsException e1) {	// comes from initMark = -1 of updateGraph()
			e1.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Unable to calculate rates.\nWindow size or pathlength must be reduced.\nLearning unsuccessful.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			try {
				out.write("ERROR! Unable to calculate rates.\nIf Window size = -1, pathlength must be reduced;\nElse, reduce windowsize\nLearning unsuccessful.");
				out.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		catch (java.lang.IllegalStateException e3){
			e3.printStackTrace();
			//System.out.println("LPN file couldn't be created/written ");
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN File not found for merging.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return (g);
	}
	
	private int findReqdVarslIndex(String s) {
		for (int i = 0; i < reqdVarsL.size() ; i++){
			if (s.equalsIgnoreCase(reqdVarsL.get(i).getName())){
				return i;
			}
		}
		return -1;
	}
	
	private boolean isTransientPlace(String st1) {
		for (String s : transientNetPlaces.keySet()){
			if (st1.equalsIgnoreCase("p" + transientNetPlaces.get(s).getProperty("placeNum"))){
				return true;
			}
		}
		return false;
	}

	private boolean isTransientTransition(String st1) {
		for (String s : transientNetTransitions.keySet()){
			if (st1.equalsIgnoreCase("t" + transientNetTransitions.get(s).getProperty("transitionNum"))){
				return true;
			}
		}
		return false;
	}

	
	public void findReqdVarIndices(){
		reqdVarIndices = new ArrayList<Integer>();
		for (int i = 0; i < reqdVarsL.size(); i++) {
			for (int j = 1; j < varNames.size(); j++) {
				if (reqdVarsL.get(i).getName().equalsIgnoreCase(varNames.get(j))) {
					reqdVarIndices.add(j);
				}
			}
		}
	}

	public void genBinsRates(HashMap<String, ArrayList<Double>> localThresholds) { // genBins
//		public void genBinsRates(String datFile,ArrayList<ArrayList<Double>> divisionsL) { // genBins
//			TSDParser tsd = new TSDParser(directory + separator + datFile, biosim,false);
			// genBins
//			data = tsd.getData();
		try{
			reqdVarIndices = new ArrayList<Integer>();
			bins = new int[reqdVarsL.size()][data.get(0).size()];
			for (int i = 0; i < reqdVarsL.size(); i++) {
				// System.out.println("Divisions " + divisionsL.get(i));
				for (int j = 1; j < varNames.size(); j++) {
					String currentVar = reqdVarsL.get(i).getName();
					if (currentVar.equalsIgnoreCase(varNames.get(j))) {
						// System.out.println(reqdVarsL.get(i) + " matched "+
						// varNames.get(j) + " i = " + i + " j = " + j);
						reqdVarIndices.add(j);
						for (int k = 0; k < data.get(j).size(); k++) {
							// System.out.print(data.get(j).get(k) + " ");
							for (int l = 0; l < localThresholds.get(currentVar).size(); l++) {
								if (data.get(j).get(k) <= localThresholds.get(currentVar).get(l)) {
									bins[i][k] = l;
									break;
								} else {
									bins[i][k] = l + 1; // indices of bins not same
									// as that of the variable.
									// i here. not j; if j wanted, then size of bins
									// array should be varNames not reqdVars
								}
							}
						}
						// System.out.print(" ");
					}
				}
			}
			/*
			 * System.out.println("array bins is :"); for (int i = 0; i <
			 * reqdVarsL.size(); i++) { System.out.print(reqdVarsL.get(i).getName() + "
			 * "); for (int k = 0; k < data.get(0).size(); k++) {
			 * System.out.print(bins[i][k] + " "); } System.out.print("\n"); }
			 */
			// genRates
			rates = new Double[reqdVarsL.size()][data.get(0).size()];
			values = new double[reqdVarsL.size()][data.get(0).size()];
			duration = new Double[data.get(0).size()];
			int mark, k, previous = 0; // indices of rates not same as that of the variable. if
			// wanted, then size of rates array should be varNames
			// not reqdVars
			if (rateSampling == -1) { // replacing inf with -1 since int
				mark = 0;
				for (int i = 0; i < data.get(0).size(); i++) {
					if (i < mark) {
						continue;
					}
					while ((mark < data.get(0).size()) && (compareBins(i, mark))) {
						for (int j = 0; j < reqdVarsL.size(); j++){
							k = reqdVarIndices.get(j);
							values[j][i] = (values[j][i]*(mark-i) + data.get(k).get(mark))/(mark-i+1);
						}
						mark++;
					}
					if ((data.get(0).get(mark - 1) != data.get(0).get(i)) && ((mark - i) >=  pathLength) && (mark != data.get(0).size())) { 	// && (mark != (data.get(0).size() - 1 condition added on nov 23.. to avoid the last region bcoz it's not complete. rechk
						if (!compareBins(previous,i)){
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
							}
							duration[i] = data.get(0).get(mark)	- data.get(0).get(i); // changed (mark - 1) to mark on may 28,2010
							previous = i;
						} else{	// There was a glitch and you returned to the same region
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][previous] = ((data.get(k).get(mark - 1) - data.get(k).get(previous)) / (data.get(0).get(mark - 1) - data.get(0).get(previous)));
							}
							duration[previous] = data.get(0).get(mark)	- data.get(0).get(previous); // changed (mark - 1) to mark on may 28,2010
						}
					} else if ((mark - i) <  pathLength)  { // account for the glitch duration //  
						duration[previous] += data.get(0).get(mark)	- data.get(0).get(i); 
					} else if (data.get(0).get(mark - 1) == data.get(0).get(i)){ // bin with only one point. Added this condition on June 9,2010
						//Rates are meaningless here since the bin has just one point.
						//But calculating the rate because if it is null, then places won't be created in genBinsRates for one point bins.
						//Calculating rate b/w start point of next bin and start point of this bin
						out.write("Bin with one point at time " + data.get(0).get(i) + "\n");
						for (int j = 0; j < reqdVarsL.size(); j++) {
							k = reqdVarIndices.get(j);
							rates[j][i] = ((data.get(k).get(mark) - data.get(k).get(i)) / (data.get(0).get(mark) - data.get(0).get(i)));
						}
						duration[i] = data.get(0).get(mark)	- data.get(0).get(i); // changed (mark - 1) to mark on may 28,2010
						previous = i;
					}
				}
			} else { //TODO: This may have bugs in duration calculation etc.
				boolean calcRate;
				boolean prevFail = true;
				int binStartPoint = 0, binEndPoint = 0;
				for (int i = 0; i < (data.get(0).size() - rateSampling); i++) {
					calcRate = true;
					for (int l = 0; l < rateSampling; l++) {
						if (!compareBins(i, i + l)) {
							if (!prevFail){
								binEndPoint = i -2 + rateSampling;
								duration[binStartPoint] = data.get(0).get(binEndPoint)	- data.get(0).get(binStartPoint);
							}
							calcRate = false;
							prevFail = true;
							break;
						}
					}
					if (calcRate && (data.get(0).get(i + rateSampling) != data.get(0).get(i))) {
						for (int j = 0; j < reqdVarsL.size(); j++) {
							k = reqdVarIndices.get(j);
							rates[j][i] = ((data.get(k).get(i + rateSampling) - data.get(k).get(i)) / (data.get(0).get(i + rateSampling) - data.get(0).get(i)));
						}
						if (prevFail){
							binStartPoint = i;
						}
						prevFail = false;
					}
				}
				// commented on nov 23. don't need this. should avoid rate calculation too for this region. but not avoiding now.
				/*	if (!prevFail){ // for the last genuine rate-calculating region of the trace; this may not be required if the trace is incomplete.trace data may not necessarily end at a region endpt
						duration[binStartPoint] = data.get(0).get(data.get(0).size()-1)	- data.get(0).get(binStartPoint);
					}*/ 
			}
		} catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Bins/Rates could not be generated. Please check thresholds.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);				
		} catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Log file couldn't be opened for writing genBinsRates messages.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		/*
			try {
				for (int i = 0; i < (data.get(0).size()); i++) {
					for (int j = 0; j < reqdVarsL.size(); j++) {
						k = reqdVarIndices.get(j);
						out.write(data.get(k).get(i) + " ");// + bins[j][i] + " " +
						// rates[j][i] + " ");
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						out.write(bins[j][i] + " ");
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						out.write(rates[j][i] + " ");
					}
					out.write(duration[i] + " ");
					out.write("\n");
				}
			} catch (IOException e) {
				System.out
						.println("Log file couldn't be opened for writing rates and bins ");
			}*/
	}


	public boolean compareBins(int j, int mark) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;
	}

	public void updateRateInfo(int[][] bins, Double[][] rates, Properties cvgProp) {
		String prevPlaceKey = ""; // "" or " " ; rechk
		String key = "";
		Double prevPlaceDuration = null;
		// boolean addNewPlace;
		// ArrayList<String> ratePlaces = new ArrayList<String>(); // ratePlaces can include non-input dmv places.
		// boolean newRate = false;
		Properties p0, p1 = null;
		try{
			for (int i = 0; i < (data.get(0).size() - 1); i++) {
				if (rates[0][i] != null) { // check if indices are ok. 0???? or 1???
					prevPlaceKey = key;
					key = "" + bins[0][i];
					for (int j = 1; j < reqdVarsL.size(); j++) {
//						key += "" + bins[j][i];
						key += "," + bins[j][i];
					}
					if (placeInfo.containsKey(key)) {
						p0 = placeInfo.get(key);
					} 
					else if ((transientNetPlaces.containsKey(key)) && (ratePlaces.size() == 1)){
						p0 = transientNetPlaces.get(key);
					}
					else {
						p0 = new Properties();
						if (ratePlaces.size() == 0){
							transientNetPlaces.put(key, p0);
							g.addPlace("p" + numPlaces, true);
						}
						else{
							placeInfo.put(key, p0);
							g.addPlace("p" + numPlaces, false);
						}
						p0.setProperty("placeNum", numPlaces.toString());
						p0.setProperty("type", "RATE");
						p0.setProperty("initiallyMarked", "false");
						p0.setProperty("metaType","false");  // REMOVE LATER?????
						ratePlaces.add("p" + numPlaces);
						numPlaces++;
						cvgProp.setProperty("places", String.valueOf(Integer.parseInt(cvgProp.getProperty("places"))+1));
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						// rechk if (reqdVarsL.get(j).isDmvc() && reqdVarsL.get(j).isInput()) {
						// continue;
						// }
						if (reqdVarsL.get(j).isDmvc()) { // && !reqdVarsL.get(j).isInput()){
							addValue(p0,reqdVarsL.get(j).getName(),dmvcValuesUnique.get(reqdVarsL.get(j).getName()).get(bins[j][i]));
							out.write("Add value : " + reqdVarsL.get(j).getName() + " -> " + dmvcValuesUnique.get(reqdVarsL.get(j).getName()).get(bins[j][i]) + " at place p" + p0.getProperty("placeNum") + "\n");
							continue;
						}
						addRate(p0, reqdVarsL.get(j).getName(), rates[j][i]);
						// newR, oldR, dmvc etc. left
					}
					if (!prevPlaceKey.equalsIgnoreCase(key)) {
						if (transitionInfo.containsKey(prevPlaceKey + key)) { // instead of tuple
							p1 = transitionInfo.get(prevPlaceKey + key);
						} else if (prevPlaceKey != "") {
							// transition = new Transition(reqdVarsL.size(),place,prevPlace);
							p1 = new Properties();
							p1.setProperty("transitionNum", numTransitions.toString());
							if (ratePlaces.size() == 2){
								transientNetTransitions.put(prevPlaceKey + key, p1);
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + transientNetPlaces.get(prevPlaceKey).getProperty("placeNum"), "t" + transientNetTransitions.get(prevPlaceKey + key).getProperty("transitionNum")); 
								g.addMovement("t" + transientNetTransitions.get(prevPlaceKey + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
							}
							else{
								transitionInfo.put(prevPlaceKey + key, p1);
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + placeInfo.get(prevPlaceKey).getProperty("placeNum"), "t" + transitionInfo.get(prevPlaceKey + key).getProperty("transitionNum")); 
								g.addMovement("t" + transitionInfo.get(prevPlaceKey + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
							}
							numTransitions++;
							cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
							// transition.setCore(true);
						}
						if (prevPlaceDuration != null){ //Delay on a transition is the duration spent at its preceding place
							addDuration(p1, prevPlaceDuration);
							out.write("Update delay at transition t" + p1.getProperty("transitionNum") + " with " + prevPlaceDuration + " at time " + data.get(0).get(i) + "\n");
						}
					}
					prevPlaceDuration = duration[i];
					/*if (duration[i] != null){	//STORING DELAYS AT TRANSITIONS NOW
					addDuration(p0, duration[i]);
				}*/
					//else if (duration[i] != null && transientNet){
					//	addTransientDuration(p0, duration[i]);
					//}
					if (p1 != null) {
						for (int j = 0; j < reqdVarsL.size(); j++) {
							if (reqdVarsL.get(j).isDmvc() && reqdVarsL.get(j).isInput()) {
								continue;
							}
							if (reqdVarsL.get(j).isDmvc()) {
								continue;
							}
							addRate(p1, reqdVarsL.get(j).getName(), rates[j][i]);
						}
					}
				}
			}
		}catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Log file couldn't be opened for writing UpdateRateInfo messages.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void updateGraph(int[][] bins, Double[][] rates, Properties cvgProp) {
		updateRateInfo(bins, rates, cvgProp);
		//updateTimeInfo(bins,cvgProp);
		int initMark = -1;
		int k;
		String key;
		for (int i = 0; i < reqdVarsL.size(); i++) {
			for (int j = 0; j < data.get(0).size(); j++) {
				if (rates[i][j] != null) {
					k = reqdVarIndices.get(i);
					// addInitValues(data.get(k).get(j), i); // k or i think ??
					// addInitRates(rates[i][j], i);// k or i think??
					reqdVarsL.get(i).addInitValues(data.get(k).get(j)); // Do the same for initvals too??
					//reqdVarsL.get(i).addInitRates(rates[i][j]);	// this just adds the first rate; not the rates of entire 1st region.
					initMark = j;
					break;
				}
			}
			key = "" + bins[0][initMark];
			for (int l = 1; l < reqdVarsL.size(); l++) {
//				key = key + "" + bins[l][initMark];
				key = key + "," + bins[l][initMark];
			}
			if (!reqdVarsL.get(i).isDmvc()){
				reqdVarsL.get(i).addInitRates((double)getMinRate(key, reqdVarsL.get(i).getName()));
				reqdVarsL.get(i).addInitRates((double)getMaxRate(key, reqdVarsL.get(i).getName()));
			}
/*
			if (placeInfo.get(key).getProperty("initiallyMarked").equalsIgnoreCase("false")) {

				placeInfo.get(key).setProperty("initiallyMarked", "true");
				g.changeInitialMarking("p" + placeInfo.get(key).getProperty("placeNum"), true);
			}
*/
		}
	}

	public HashMap<String, ArrayList<Double>> detectDMV(ArrayList<ArrayList<Double>> data, Boolean callFromAutogen) {
		int startPoint, endPoint, mark, numPoints;
		HashMap<String, ArrayList<Double>> dmvDivisions = new HashMap<String, ArrayList<Double>>();
		double absTime;
		dmvcValuesUnique = new HashMap<String, ArrayList<Double>>();
		for (int i = 0; i < reqdVarsL.size(); i++) {
			absTime = 0;
			mark = 0;
			DMVCrun runs = reqdVarsL.get(i).getRuns();
			runs.clearAll(); // flush all the runs from previous dat file.
			int lastRunPointsWithoutTransition = 0;
			Double lastRunTimeWithoutTransition = 0.0;
			if (!callFromAutogen) { // This flag is required because if the call is from autogenT, then data has just the reqdVarsL but otherwise, it has all other vars too. So reqdVarIndices not reqd when called from autogen
				for (int j = 0; j <= data.get(0).size(); j++) {
					if (j < mark) // not reqd??
						continue;
					if (((j+1) < data.get(reqdVarIndices.get(i)).size()) && 
							Math.abs(data.get(reqdVarIndices.get(i)).get(j) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= epsilon) {
						startPoint = j;
						runs.addValue(data.get(reqdVarIndices.get(i)).get(j)); // chk carefully reqdVarIndices.get(i)
						while (((j + 1) < data.get(0).size()) && (bins[i][startPoint] == bins[i][j+1]) && (Math.abs(data.get(reqdVarIndices.get(i)).get(startPoint) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= epsilon)) {       //checking of same bins[] condition added on May 11,2010.
							runs.addValue(data.get(reqdVarIndices.get(i)).get(j + 1)); // chk carefully
							// reqdVarIndices.get(i)
							j++;
						}
						endPoint = j;
						if (runTime == null) {
							if ((endPoint < (data.get(0).size() - 1)) && ((endPoint - startPoint) + 1) >= runLength) {
								runs.addStartPoint(startPoint);
								runs.addEndPoint(endPoint);
							} else if (((endPoint - startPoint) + 1) >= runLength) {
								lastRunPointsWithoutTransition = endPoint - startPoint + 1;
							} else {
								runs.removeValue();
							}
						} else {
							if ((endPoint < (data.get(0).size() - 1)) && (calcDelay(startPoint, endPoint) >= runTime)) {
								runs.addStartPoint(startPoint);
								runs.addEndPoint(endPoint);
								absTime += calcDelay(startPoint, endPoint);
							} else if (((endPoint - startPoint) + 1) >= runLength) {
								lastRunTimeWithoutTransition = calcDelay(startPoint, endPoint);
							} else {
								runs.removeValue();
							}
						}
						mark = endPoint;
					}
				}
			}
			else{
				for (int j = 0; j <= data.get(0).size(); j++) {
					if (j < mark) // not reqd??
						continue;
					if (((j+1) < data.get(i+1).size()) && 
							Math.abs(data.get(i+1).get(j) - data.get(i+1).get(j + 1)) <= epsilon) { //i+1 and not i bcoz 0th col is time
						startPoint = j;
						runs.addValue(data.get(i+1).get(j)); // chk carefully reqdVarIndices.get(i)
						while (((j + 1) < data.get(0).size()) && (Math.abs(data.get(i+1).get(startPoint) - data.get(i+1).get(j + 1)) <= epsilon)) {
							runs.addValue(data.get(i+1).get(j + 1)); // chk carefully
							// reqdVarIndices.get(i)
							j++;
						}
						endPoint = j;
						if (runTime == null) {
							if ((endPoint < (data.get(0).size() - 1)) && ((endPoint - startPoint) + 1) >= runLength) {
								runs.addStartPoint(startPoint);
								runs.addEndPoint(endPoint);
							} else if (((endPoint - startPoint) + 1) >= runLength) {
								lastRunPointsWithoutTransition = endPoint - startPoint + 1;
							} else {
								runs.removeValue();
							}
						} else {
							if ((endPoint < (data.get(0).size() - 1)) && (calcDelayWithData(startPoint, endPoint, data) >= runTime)) {
								runs.addStartPoint(startPoint);
								runs.addEndPoint(endPoint);
								absTime += calcDelayWithData(startPoint, endPoint, data);
							} else if (((endPoint - startPoint) + 1) >= runLength) {
								lastRunTimeWithoutTransition = calcDelayWithData(startPoint, endPoint, data);
							} else {
								runs.removeValue();
							}
						}
						mark = endPoint;
					}
				}
			}
			try {
				if (runTime == null) {
					numPoints = runs.getNumPoints();
					if (((numPoints + lastRunPointsWithoutTransition)/ (double) data.get(0).size()) < percent) {
						runs.clearAll();
						reqdVarsL.get(i).setDmvc(false);
						out.write(reqdVarsL.get(i).getName()
								+ " is not a dmvc \n");
					} else {
						reqdVarsL.get(i).setDmvc(true);
						Double[] dmvcValues = reqdVarsL.get(i).getRuns().getAvgVals();
						Arrays.sort(dmvcValues);
						//System.out.println("Sorted DMV values of " + reqdVarsL.get(i).getName() + " are ");
						//for (Double l : dmvcValues){
						//	System.out.print(l + " ");
						//}
						dmvcValuesUnique.put(reqdVarsL.get(i).getName(),new ArrayList<Double>());
						ArrayList<Double> dmvSplits = new ArrayList<Double>();
						out.write("Final DMV values of " + reqdVarsL.get(i).getName() + " are ");
						for (int j = 0; j < dmvcValues.length; j++){
							dmvcValuesUnique.get(reqdVarsL.get(i).getName()).add(dmvcValues[j]);
							out.write(dmvcValues[j] + " ");
							if (dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() > 1){
								dmvSplits.add((dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 1) + dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 2))/2);
							}
							for (int k = j+1; k < dmvcValues.length; k++){
								if (Math.abs((dmvcValues[j] - dmvcValues[k])) > epsilon){
									j = k-1;
									break;
								}
								else if (k >= (dmvcValues.length -1)){
									j = k;
								}
							}	
						}
						dmvDivisions.put(reqdVarsL.get(i).getName(), dmvSplits);
						out.write(reqdVarsL.get(i).getName() + " is  a dmvc \n");
					}
				} else {
					if (((absTime + lastRunTimeWithoutTransition)/ (data.get(0).get(data.get(0).size() - 1) - data
							.get(0).get(0))) < percent) {
						runs.clearAll();
						reqdVarsL.get(i).setDmvc(false);
						out.write(reqdVarsL.get(i).getName()
								+ " is not a dmvc \n");
					} else {
						reqdVarsL.get(i).setDmvc(true);
						Double[] dmvcValues = reqdVarsL.get(i).getRuns().getAvgVals();
						Arrays.sort(dmvcValues);
						dmvcValuesUnique.put(reqdVarsL.get(i).getName(),new ArrayList<Double>());
						ArrayList<Double> dmvSplits = new ArrayList<Double>();
						for (int j = 0; j < dmvcValues.length; j++){
							dmvcValuesUnique.get(reqdVarsL.get(i).getName()).add(dmvcValues[j]);
							out.write(dmvcValues[j].toString());
							if (dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() > 1){
								double d1 = (dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 1) + dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 2))/2.0;
								d1 = d1*10000;
								int d2 = (int) d1; 
								//System.out.println(d2);
								//System.out.println(((double)d2)/10000.0);
								dmvSplits.add(((double)d2)/10000.0); // truncating to 4 decimal places
							}
							for (int k = j+1; k < dmvcValues.length; k++){
								if (Math.abs((dmvcValues[j] - dmvcValues[k])) > epsilon){
									j = k-1;
									break;
								}
								else if (k >= (dmvcValues.length -1)){
									j = k;
								}
							}	
						}
						dmvDivisions.put(reqdVarsL.get(i).getName(), dmvSplits);
						out.write(reqdVarsL.get(i).getName()
										+ " is  a dmvc \n");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(biosim.frame(),
						"Log file couldn't be opened for writing rates and bins.",
						"ERROR!", JOptionPane.ERROR_MESSAGE);
			}
		}
		return(dmvDivisions);
	}

	public double calcDelay(int i, int j) {
		return (data.get(0).get(j) - data.get(0).get(i));
		// should add some next run logic later..?
	}
	
	public double calcDelayWithData(int i, int j, ArrayList<ArrayList<Double>> data) {
		return (data.get(0).get(j) - data.get(0).get(i));
	}

	public void addValue(Properties p, String name, Double v) { 
		Double vMin;
		Double vMax;
		if ((p.getProperty(name + "_vMin") == null)
				&& (p.getProperty(name + "_vMax") == null)) {
			p.setProperty(name + "_vMin", v.toString());
			p.setProperty(name + "_vMax", v.toString());
			return;
		} else {
			vMin = Double.parseDouble(p.getProperty(name + "_vMin"));
			vMax = Double.parseDouble(p.getProperty(name + "_vMax"));
			if (v < vMin) {
				vMin = v;
			} else if (v > vMax) {
				vMax = v;
			}
		}
		p.setProperty(name + "_vMin", vMin.toString());
		p.setProperty(name + "_vMax", vMax.toString());
	}

	public void addRate(Properties p, String name, Double r) { // latest
		// change.. above one working fine if this doesn't
		Double rMin;
		Double rMax;
		if ((p.getProperty(name + "_rMin") == null)
				&& (p.getProperty(name + "_rMax") == null)) {
			p.setProperty(name + "_rMin", r.toString());
			p.setProperty(name + "_rMax", r.toString());
			return;
		} else {
			rMin = Double.parseDouble(p.getProperty(name + "_rMin"));
			rMax = Double.parseDouble(p.getProperty(name + "_rMax"));
			if (r < rMin) {
				rMin = r;
			} else if (r > rMax) {
				rMax = r;
			}
		}
		p.setProperty(name + "_rMin", rMin.toString());
		p.setProperty(name + "_rMax", rMax.toString());
	}

	public HashMap<String,ArrayList<Double>> normalize() {
		HashMap<String,ArrayList<Double>> scaledThresholds = new HashMap<String,ArrayList<Double>>();
		Boolean contVarExists = false;
		for (Variable v : reqdVarsL){
			if (!v.isDmvc()){
				contVarExists = true;
			}
		}
		// deep copy of divisions
		for (String s : thresholds.keySet()){
			ArrayList<Double> o1 = thresholds.get(s);
			ArrayList<Double> tempDiv = new ArrayList<Double>();
			for (Double o2 : o1){
				tempDiv.add( o2.doubleValue()); // clone() not working here
			}
			scaledThresholds.put(s,tempDiv);
		}
		Double minDelay = getMinDelay();
		Double maxDelay = getMaxDelay();
		Double minDivision = getMinDiv(scaledThresholds);
		Double maxDivision = getMaxDiv(scaledThresholds);
		Double scaleFactor = 1.0;
		try {
			if ((valScaleFactor == -1.0) && (delayScaleFactor == -1.0)){
				out.write("\nAuto determining both value and delay scale factors\n");
				valScaleFactor = 1.0;
				delayScaleFactor = 1.0;
				out.write("minimum delay is " + minDelay + " before scaling time.\n");
				if ((minDelay != null) && (minDelay != 0)) {
					for (int i = 0; i < 18; i++) {
						if (scaleFactor > (minDelayVal / minDelay)) {
							break;
						}
						scaleFactor *= 10.0;
					}
					if ((maxDelay != null) && ((int) (maxDelay * scaleFactor) > Integer.MAX_VALUE)) {
						System.out.println("Delay Scaling has caused an overflow");
					}
					out.write("minimum delay is " + scaleFactor * minDelay + "after scaling by " + scaleFactor + "\n");
					delayScaleFactor = scaleFactor;
					scaleDelay(delayScaleFactor);
				}
				if (contVarExists){
					scaleFactor = 1.0;
					Double minRate = getMinRate(); // minRate should return minimum by magnitude alone?? or even by sign..
					Double maxRate = getMaxRate();
					out.write("minimum rate is " + minRate + " before scaling the variable.\n");
					if ((minRate != null) && (minRate != 0)) {
						for (int i = 0; i < 14; i++) {
							if (scaleFactor > Math.abs(minRateVal / minRate)) {
								break;
							}
							scaleFactor *= 10.0;
						}
						for (int i = 0; i < 14; i++) {
							if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) < Integer.MAX_VALUE)) {
								break;
							}
							scaleFactor /= 10.0;
						}
						if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) > Integer.MAX_VALUE)) {
							System.out.println("Rate Scaling has caused an overflow");
						}
						out.write("minimum rate is " + minRate * scaleFactor + " after scaling by " + scaleFactor + "\n");
						valScaleFactor = scaleFactor;
						scaledThresholds = scaleValue(scaleFactor,scaledThresholds);
					}
				}
				minDivision = getMinDiv(scaledThresholds);
				maxDivision = getMaxDiv(scaledThresholds);
				out.write("minimum division is " + minDivision + " before scaling for division.\n");
				scaleFactor = 1.0;
				if ((minDivision != null) && (minDivision != 0)) {
					for (int i = 0; i < 14; i++) {
						if (Math.abs(scaleFactor * minDivision) > minDivisionVal) {
							break;
						}
						scaleFactor *= 10;
					}
					if ((maxDivision != null) && (Math.abs((int) (maxDivision * scaleFactor)) > Integer.MAX_VALUE)) {
						System.out.println("Division Scaling has caused an overflow");
					}
					out.write("minimum division is " + minDivision * scaleFactor + " after scaling by " + scaleFactor + "\n");
					valScaleFactor *= scaleFactor;
					scaledThresholds = scaleValue(scaleFactor,scaledThresholds);
				}
			} else if (valScaleFactor == -1.0){ //force delayScaling; automatic varScaling
				out.write("\nAuto determining value scale factor; Forcing delay scale factor\n");
				valScaleFactor = 1.0;
				out.write("minimum delay is " + minDelay + " before scaling time.\n");
				/*if ((minDelay != null) && (minDelay != 0)) {
					for (int i = 0; i < 18; i++) {
						if (scaleFactor > (minDelayVal / minDelay)) {
							break;
						}
						scaleFactor *= 10.0;
					}
					if ((maxDelay != null) && ((int) (maxDelay * scaleFactor) > Integer.MAX_VALUE)) {
						System.out.println("Delay Scaling has caused an overflow");
					}
					if (scaleFactor > delayScaleFactor){
						out.write("WARNING: Minimum delay won't be an integer with the given scale factor. So using " + scaleFactor + "instead.");
						delayScaleFactor = scaleFactor;
					} //else delayScaleFactor = delayScaleFactor (user provided)
					scaleDelay(delayScaleFactor);
				} else {
					scaleDelay(delayScaleFactor);// Even if we can't calculate delayScaleFactor, use user provided one whatever it is
					out.write("min delay = 0. So, not checking whether the given delayScalefactor is correct\n");
				}*/
				out.write("minimum delay is " + delayScaleFactor * minDelay + "after scaling by " + delayScaleFactor + "\n");
				scaleDelay(delayScaleFactor);
				if (contVarExists){
					scaleFactor = 1.0;
					Double minRate = getMinRate(); // minRate should return minimum by
					// magnitude alone?? or even by sign..
					Double maxRate = getMaxRate();
					out.write("minimum rate is " + minRate + " before scaling the variable.\n");
					if ((minRate != null) && (minRate != 0)) {
						for (int i = 0; i < 14; i++) {
							if (scaleFactor > Math.abs(minRateVal / minRate)) {
								break;
							}
							scaleFactor *= 10.0;
						}
						for (int i = 0; i < 14; i++) {
							if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) < Integer.MAX_VALUE)) {
								break;
							}
							scaleFactor /= 10.0;
						}
						if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) > Integer.MAX_VALUE)) {
							System.out.println("Rate Scaling has caused an overflow");
						}
						out.write("minimum rate is " + minRate * scaleFactor + " after scaling by " + scaleFactor + "\n");
						valScaleFactor = scaleFactor;
						scaledThresholds = scaleValue(scaleFactor,scaledThresholds);
						// TEMPORARY
						/*		for (String p : g.getPlaceList()){
					if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
						String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
						System.out.println(s);
					}
					} */
						// end TEMPORARY
					}
				}
				minDivision = getMinDiv(scaledThresholds);
				maxDivision = getMaxDiv(scaledThresholds);
				out.write("minimum division is " + minDivision + " before scaling for division.\n");
				scaleFactor = 1.0;
				if ((minDivision != null) && (minDivision != 0)) {
					for (int i = 0; i < 14; i++) {
						if (Math.abs(scaleFactor * minDivision) > minDivisionVal) {
							break;
						}
						scaleFactor *= 10;
					}
					if ((maxDivision != null)
							&& (Math.abs((int) (maxDivision * scaleFactor)) > Integer.MAX_VALUE)) {
						System.out.println("Division Scaling has caused an overflow");
					}
					out.write("minimum division is " + minDivision * scaleFactor + " after scaling by " + scaleFactor + "\n");
					valScaleFactor *= scaleFactor;
					scaledThresholds = scaleValue(scaleFactor,scaledThresholds);
					// TEMPORARY
					/*		for (String p : g.getPlaceList()){
					if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
						String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
						System.out.println(s);
					}
					}*/
					// END TEMPORARY
				}
			} else if (delayScaleFactor == -1.0){ //force valueScaling; automatic delayScaling
				out.write("\nAuto determining delay scale factor; Forcing value scale factor\n");
				//May be wrong in some cases
				delayScaleFactor = 1.0;
				minDivision = getMinDiv(scaledThresholds);
				maxDivision = getMaxDiv(scaledThresholds);
				out.write("minimum division is " + minDivision + " before scaling for division.\n");
				/*scaleFactor = 1.0;
				if ((minDivision != null) && (minDivision != 0)) {
					for (int i = 0; i < 14; i++) {
						if (Math.abs(scaleFactor * minDivision) > minDivisionVal) {
							break;
						}
						scaleFactor *= 10;
					}
					if ((maxDivision != null) && (Math.abs((int) (maxDivision * scaleFactor)) > Integer.MAX_VALUE)) {
						out.write("ERROR: Division Scaling has caused an overflow");
					}
					if (scaleFactor > valScaleFactor){
						out.write("WARNING: Minimum threshold won't be an integer with the given scale factor. So using " + scaleFactor + "instead.");
						valScaleFactor = scaleFactor;
					}
					out.write("minimum division is " + minDivision * valScaleFactor	+ " after scaling by " + valScaleFactor + "\n");
					scaledThresholds = scaleValue(valScaleFactor,scaledThresholds);
					// TEMPORARY
					//		for (String p : g.getPlaceList()){
					//if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
					//	String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
					//	System.out.println(s);
					//}
					//}
					// END TEMPORARY
				} else {
					scaledThresholds = scaleValue(valScaleFactor,scaledThresholds);
					out.write("Min division is 0. So, not checking whether given value scale factor is correct");
				}*/
				scaledThresholds = scaleValue(valScaleFactor,scaledThresholds);
				out.write("minimum division is " + minDivision * valScaleFactor	+ " after scaling by " + valScaleFactor + "\n");
				if (contVarExists){
					scaleFactor = 1.0;
					Double minRate = getMinRate(); // minRate should return minimum by magnitude alone?? or even by sign..
					Double maxRate = getMaxRate();
					out.write("minimum rate is " + minRate + " before scaling the variable.\n");
					if ((minRate != null) && (minRate != 0)) {
						for (int i = 0; i < 14; i++) {
							if (scaleFactor > Math.abs(minRateVal / minRate)) {
								break;
							}
							scaleFactor *= 10.0;
						}
						for (int i = 0; i < 14; i++) {
							if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) < Integer.MAX_VALUE)) {
								break;
							}
							scaleFactor /= 10.0;
						}
						if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) > Integer.MAX_VALUE)) {
							out.write("Rate Scaling has caused an overflow\n");
						}
						out.write("minimum rate is " + minRate * scaleFactor + " after scaling delay by " + 1/scaleFactor + "\n");
						scaleDelay(1/scaleFactor);
						delayScaleFactor = 1/scaleFactor;
						// TEMPORARY
						/*		for (String p : g.getPlaceList()){
					if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
						String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
						System.out.println(s);
					}
					} */
						// end TEMPORARY
					}
				}
				scaleFactor = 1.0;
				minDelay = getMinDelay();
				maxDelay = getMaxDelay();
				out.write("minimum delay is " + minDelay + " before scaling time.\n");
				if ((minDelay != null) && (minDelay != 0)) {
					for (int i = 0; i < 18; i++) {
						if (scaleFactor > (minDelayVal / minDelay)) {
							break;
						}
						scaleFactor *= 10.0;
					}
					if ((maxDelay != null) && ((int) (maxDelay * scaleFactor) > Integer.MAX_VALUE)) {
						System.out.println("Delay Scaling has caused an overflow");
					}
					delayScaleFactor = scaleFactor;
					out.write("minimum delay is " + delayScaleFactor * minDelay
							+ "after scaling delay by " + delayScaleFactor + "\n");
					scaleDelay(delayScaleFactor);
				}
			} else { //User forces both delay and value scaling factors.
				out.write("\nForcing delay and value scale factors\n");
				minDivision = getMinDiv(scaledThresholds);
				out.write("minimum division is " + minDivision + " before scaling for division.\n");
				scaledThresholds = scaleValue(valScaleFactor, scaledThresholds);
				minDivision = getMinDiv(scaledThresholds);
				out.write("minimum division is " + minDivision + " after scaling for division.\n");
				minDelay = getMinDelay();
				out.write("minimum delay is " + minDivision + " before scaling for delay.\n");
				scaleDelay(delayScaleFactor);
				minDelay = getMinDelay();
				out.write("minimum delay is " + minDivision + " after scaling for delay.\n");
				/*
				 minDivision = getMinDiv(scaledThresholds);
				 maxDivision = getMaxDiv(scaledThresholds); 
				 scaleFactor = 1.0;
				 if ((minDivision != null) && (minDivision != 0)) {
					for (int i = 0; i < 14; i++) {
						if (Math.abs(scaleFactor * minDivision) > minDivisionVal) {
							break;
						}
						scaleFactor *= 10;
					}
					if ((maxDivision != null) && (Math.abs((int) (maxDivision * scaleFactor)) > Integer.MAX_VALUE)) {
						out.write("ERROR: Division Scaling has caused an overflow");
					}
					if (scaleFactor > valScaleFactor){
						out.write("WARNING: Minimum threshold won't be an integer with the given scale factor. So using " + scaleFactor + "instead.");
						valScaleFactor = scaleFactor;
					}
					out.write("minimum division is " + minDivision * valScaleFactor	+ " after scaling by " + valScaleFactor + "\n");
					scaledThresholds = scaleValue(valScaleFactor,scaledThresholds);
					// TEMPORARY
					//		for (String p : g.getPlaceList()){
					//if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
					//	String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
					//	System.out.println(s);
					//}
					//}
					// END TEMPORARY
				} else {
					scaledThresholds = scaleValue(valScaleFactor,scaledThresholds);
					out.write("Min division is 0. So, not checking whether given value scale factor is correct");
				}
				out.write("minimum delay is " + minDelay + " before scaling time.\n");
				scaleFactor = 1.0;
				minDelay = getMinDelay();
				maxDelay = getMaxDelay();
				if ((minDelay != null) && (minDelay != 0)) {
					for (int i = 0; i < 18; i++) {
						if (scaleFactor > (minDelayVal / minDelay)) {
							break;
						}
						scaleFactor *= 10.0;
					}
					if ((maxDelay != null) && ((int) (maxDelay * scaleFactor) > Integer.MAX_VALUE)) {
						System.out.println("Delay Scaling has caused an overflow");
					}
					if (scaleFactor > delayScaleFactor){
						out.write("WARNING: Minimum delay won't be an integer with the given scale factor. So using " + scaleFactor + "instead.");
						delayScaleFactor = scaleFactor;
					}
					out.write("minimum delay value is " + delayScaleFactor * minDelay
							+ "after scaling by " + delayScaleFactor + "\n");
					scaleDelay(delayScaleFactor);
				}
				if (contVarExists){
					scaleFactor = 1.0;
					Double minRate = getMinRate(); // minRate should return minimum by magnitude alone?? or even by sign..
					Double maxRate = getMaxRate();
					out.write("minimum rate is " + minRate + " before scaling the variable.\n");
					if ((minRate != null) && (minRate != 0)) {
						for (int i = 0; i < 14; i++) {
							if (scaleFactor > Math.abs(minRateVal / minRate)) {
								break;
							}
							scaleFactor *= 10.0;
						}
						for (int i = 0; i < 14; i++) {
							if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) < Integer.MAX_VALUE)) {
								break;
							}
							scaleFactor /= 10.0;
						}
						if ((maxRate != null) && (Math.abs((int) (maxRate * scaleFactor)) > Integer.MAX_VALUE)) {
							out.write("Rate Scaling has caused an overflow\n");
						}
						out.write("minimum rate is " + minRate * scaleFactor + " after scaling delay by " + scaleFactor + "\n");
						if (scaleFactor > 1)
							out.write("The given value scaling factor is insufficient for rates. So increasing it to "+ scaleFactor*valScaleFactor);
						scaledThresholds = scaleValue(scaleFactor,scaledThresholds);
						valScaleFactor *= scaleFactor;
						// TEMPORARY
						//		for (String p : g.getPlaceList()){
					//if ((placeInfo.get(getPlaceInfoIndex(p)).getProperty("type")).equals("PROP")){
					//	String s = g.scaleEnabling(g.getPostset(p)[0],scaleFactor);
					//	System.out.println(s);
					//}
					//}
						// end TEMPORARY
					}
				}*/
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"LPN file couldn't be created/written",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return(scaledThresholds);
	}

	public HashMap<String,ArrayList<Double>> scaleValue(Double scaleFactor, HashMap<String,ArrayList<Double>> localThresholds) {
		String place;
		Properties p;
		try{
			for (String st1 : g.getPlaceList()) {
				if (!isTransientPlace(st1)){
					place = getPlaceInfoIndex(st1);
					p = placeInfo.get(place);
				}
				else{
					place = getTransientNetPlaceIndex(st1);
					p = transientNetPlaces.get(place);
				}
				if (place != "failProp"){
					/* Related to the separate net for DMV input driver
					if (p.getProperty("type").equals("DMVC")) {
						p.setProperty("DMVCValue", Double.toString(Double.parseDouble(p.getProperty("DMVCValue"))* scaleFactor));
					} else {
					 */
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/delayScaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/delayScaleFactor)));
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMin"))* scaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v.getName()
											+ "_rMax"))* scaleFactor));
						} else {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/delayScaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/delayScaleFactor)));
							if (!v.isInput()) {
								p.setProperty(v.getName() + "_vMin", Double
										.toString(Double.parseDouble(p.getProperty(v.getName()
												+ "_vMin"))* scaleFactor));
								p.setProperty(v.getName() + "_vMax", Double
										.toString(Double.parseDouble(p.getProperty(v.getName()
												+ "_vMax")) * scaleFactor));
							}

						}
					}
					//}
				}
			}
			/*int i = 0;
			  for (Variable v : reqdVarsL) {
				v.scaleInitByVar(scaleFactor);
				for (int j = 0; j < divisions.get(i).size(); j++) {
					divisions.get(i).set(j,divisions.get(i).get(j) * scaleFactor);
				}
				i++;
			}*/
			//commented above for replacing divisionsL with thresholds
			for (Variable v : reqdVarsL) {
				v.scaleInitByVar(scaleFactor);
				for (int j = 0; j < localThresholds.get(v.getName()).size(); j++) {
					localThresholds.get(v.getName()).set(j,localThresholds.get(v.getName()).get(j) * scaleFactor);
				}
			}
		}
		catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Not all regions have values for all dmv variables",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return localThresholds;
	}

	public void scaleDelay(Double scaleFactor) {
		try{
			String place;
			Properties p;
			for (String t : g.getTransitionList()) {
				if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
					if (!isTransientTransition(t)){
						if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
							String pPrev = g.getPreset(t)[0];
							String nextPlace = g.getPostset(t)[0];
							Double mind = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
							Double maxd = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax"));
							transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).setProperty("dMin",Double.toString(mind*scaleFactor));
							transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).setProperty("dMax",Double.toString(maxd*scaleFactor));
						}
					} else {
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							String pPrev = g.getPreset(t)[0];
							String nextPlace = g.getPostset(t)[0];
							Double mind = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
							Double maxd = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMax"));
							transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).setProperty("dMin",Double.toString(mind*scaleFactor));
							transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).setProperty("dMax",Double.toString(maxd*scaleFactor));
						}
					}
				}
			}
			for (String st1 : g.getPlaceList()) {
				if (!isTransientPlace(st1)){
					place = getPlaceInfoIndex(st1);
					p = placeInfo.get(place);
				}
				else{
					place = getTransientNetPlaceIndex(st1);
					p = transientNetPlaces.get(place);
				}
				if (place != "failProp"){
					/* Related to the separate net for DMV input driver
					if (p.getProperty("type").equals("DMVC")) {
						String[] times = null;
						String name = p.getProperty("DMVCVariable");
						String s = p.getProperty("dmvcTime_" + name);
						String newS = null;
						if (s != null) {
							times = s.split(" ");
							for (int i = 0; i < times.length; i++) {
								if (newS == null) {
									// newS = Integer.toString((int)(Double.parseDouble(times[i])*scaleFactor));
									newS = Double.toString(Double.parseDouble(times[i])
					 * scaleFactor);
								} else {
									// newS = newS + Integer.toString((int)(Double.parseDouble(times[i])*scaleFactor));
									newS = newS + " " + Double.toString(Double
											.parseDouble(times[i]) * scaleFactor);
								}
							}
							p.setProperty("dmvcTime_" + name, newS);
						}
						p.setProperty("dMin", Double.toString(Double.parseDouble(p
								.getProperty("dMin")) * scaleFactor));
						p.setProperty("dMax", Double.toString(Double.parseDouble(p
								.getProperty("dMax")) * scaleFactor));
					} else{*/
					// p.setProperty("dMin",Integer.toString((int)(Double.parseDouble(p.getProperty("dMin"))*scaleFactor)));
					// p.setProperty("dMax",Integer.toString((int)(Double.parseDouble(p.getProperty("dMax"))*scaleFactor)));
				/* STORING DELAYS AT TRANSITIONS
				  	p.setProperty("dMin", Double.toString(Double.parseDouble(p
							.getProperty("dMin")) * scaleFactor));
					p.setProperty("dMax", Double.toString(Double.parseDouble(p
							.getProperty("dMax")) * scaleFactor));
				*/			
					for (Variable v : reqdVarsL) {
						if (!v.isDmvc()) {
							// p.setProperty(v.getName() +
							// "_rMin",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMin"))/scaleFactor)));
							// p.setProperty(v.getName() +
							// "_rMax",Integer.toString((int)(Double.parseDouble(p.getProperty(v.getName()
							// + "_rMax"))/scaleFactor)));
							p.setProperty(v.getName() + "_rMin", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMin"))	/ scaleFactor));
							p.setProperty(v.getName() + "_rMax", Double
									.toString(Double.parseDouble(p.getProperty(v
											.getName() + "_rMax"))	/ scaleFactor));
						}
					}
					//}
				}
			}
			for (Variable v : reqdVarsL) {
				// if (!v.isDmvc()){ this if maynot be required.. rates do exist for dmvc ones as well.. since calculated before detectDMV
				v.scaleInitByDelay(scaleFactor);
				// }
			}
			// SEE IF RATES IN TRANSITIONS HAVE TO BE ADJUSTED HERE
		}
		catch(NullPointerException e){
			System.out.println("Delay scaling error due to null. Check");
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e){
			System.out.println("Delay scaling error due to Array Index.");
		}
	}
/*
	public Double getMinDiv(ArrayList<ArrayList<Double>> divisions) {
		Double minDiv = divisions.get(0).get(0);
		for (int i = 0; i < divisions.size(); i++) {
			for (int j = 0; j < divisions.get(i).size(); j++) {
				if (divisions.get(i).get(j) < minDiv) {
					minDiv = divisions.get(i).get(j);
				}
			}
		}
		return minDiv;
	}

	public Double getMaxDiv(ArrayList<ArrayList<Double>> divisions) {
		Double maxDiv = divisions.get(0).get(0);
		for (int i = 0; i < divisions.size(); i++) {
			for (int j = 0; j < divisions.get(i).size(); j++) {
				if (divisions.get(i).get(j) > maxDiv) {
					maxDiv = divisions.get(i).get(j);
				}
			}
		}
		return maxDiv;
	}
	Commented above for replacing divisionsL with thresholds
*/
	public Double getMinDiv(HashMap<String, ArrayList<Double>> divisions) {
		Double minDiv = null;
		for (String s : divisions.keySet()) {
			if (minDiv == null){
				minDiv = divisions.get(s).get(0);
			}
			for (int j = 0; j < divisions.get(s).size(); j++) {
				if (divisions.get(s).get(j) < minDiv) {
					minDiv = divisions.get(s).get(j);
				}
			}
		}
		return minDiv;
	}

	public Double getMaxDiv(HashMap<String, ArrayList<Double>> divisions) {
		Double maxDiv = null;
		for (String s : divisions.keySet()) {
			if (maxDiv == null){
				maxDiv = divisions.get(s).get(0);
			}
			for (int j = 0; j < divisions.get(s).size(); j++) {
				if (divisions.get(s).get(j) > maxDiv) {
					maxDiv = divisions.get(s).get(j);
				}
			}
		}
		return maxDiv;
	}

	public Double getMinRate() { // minimum of entire lpn
		Double minRate = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("RATE")) {
				for (Variable v : reqdVarsL) {
					if (!v.isDmvc()){
						if ((minRate == null)
								&& (p.getProperty(v.getName() + "_rMin") != null)) {
							minRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMin"));
						} else if ((p.getProperty(v.getName() + "_rMin") != null)
								&& (Double.parseDouble(p.getProperty(v.getName()
										+ "_rMin")) < minRate)
										&& (Double.parseDouble(p.getProperty(v.getName()
												+ "_rMin")) != 0.0)) {
							minRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMin"));
						}
					}
				}
			}
		}
		return minRate;
	}

	public Double getMaxRate() {
		Double maxRate = null;
		for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if (p.getProperty("type").equals("RATE")) {
				for (Variable v : reqdVarsL) {
					if (!v.isDmvc()){
						if ((maxRate == null)
								&& (p.getProperty(v.getName() + "_rMax") != null)) {
							maxRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMax"));
						} else if ((p.getProperty(v.getName() + "_rMax") != null)
								&& (Double.parseDouble(p.getProperty(v.getName()
										+ "_rMax")) > maxRate)
										&& (Double.parseDouble(p.getProperty(v.getName()
												+ "_rMax")) != 0.0)) {
							maxRate = Double.parseDouble(p.getProperty(v.getName()
									+ "_rMax"));
						}
					}
				}
			}
		}
		return maxRate;
	}

	public Double getMinDelay() {
		Double minDelay = null;
		Double mind;
		for (String t : g.getTransitionList()) {
			if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
				if (!isTransientTransition(t)){
					if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
						String pPrev = g.getPreset(t)[0];
						String nextPlace = g.getPostset(t)[0];
						if (transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin") != null){
							 mind = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
							 if (minDelay == null)
								 minDelay = mind;
							 else if ((minDelay > mind) && (mind != 0))
								 minDelay = mind;
						}
					}
				} else {
					if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
						String pPrev = g.getPreset(t)[0];
						String nextPlace = g.getPostset(t)[0];
						if (transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin") != null){
							mind = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dMin"));
							if (minDelay == null)
								minDelay = mind;
							else if ((minDelay > mind) && (mind != 0))
								minDelay = mind;
						}
					}
				}
			}
		}
		/*for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if ((minDelay == null) && (p.getProperty("dMin") != null)
					&& (Double.parseDouble(p.getProperty("dMin")) != 0)) {
				minDelay = Double.parseDouble(p.getProperty("dMin"));
			} else if ((p.getProperty("dMin") != null)
					&& (Double.parseDouble(p.getProperty("dMin")) != 0)
					&& (Double.parseDouble(p.getProperty("dMin")) < minDelay)) {
				minDelay = Double.parseDouble(p.getProperty("dMin"));
			}
			//}
		}*/
		return minDelay;
	}

	public Double getMaxDelay() {
		Double maxDelay = null;
		Double maxd;
		for (String t : g.getTransitionList()) {
			if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
				if (!isTransientTransition(t)){
					if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
						String pPrev = g.getPreset(t)[0];
						String nextPlace = g.getPostset(t)[0];
						if (transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax") != null){
							 maxd = Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + getPlaceInfoIndex(nextPlace)).getProperty("dMax"));
							 if (maxDelay == null)
								 maxDelay = maxd;
							 else if ((maxDelay < maxd) && (maxd != 0))
								 maxDelay = maxd;
						}
					}
				} else {
					if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
						String pPrev = g.getPreset(t)[0];
						String nextPlace = g.getPostset(t)[0];
						if (transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dmax") != null){
							maxd = Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+getPlaceInfoIndex(nextPlace)).getProperty("dmax"));
							if (maxDelay == null)
								maxDelay = maxd;
							else if ((maxDelay < maxd) && (maxd != 0))
								maxDelay = maxd;
						}
					}
				}
			}
		}
		/*for (String place : placeInfo.keySet()) {
			Properties p = placeInfo.get(place);
			if ((maxDelay == null) && (p.getProperty("dMax") != null)
					&& (Double.parseDouble(p.getProperty("dMax")) != 0)) {
				maxDelay = Double.parseDouble(p.getProperty("dMax"));
			} else if ((p.getProperty("dMax") != null)
					&& (Double.parseDouble(p.getProperty("dMax")) != 0)
					&& (Double.parseDouble(p.getProperty("dMax")) > maxDelay)) {
				maxDelay = Double.parseDouble(p.getProperty("dMax"));
			}
			//}
		}*/
		return maxDelay;
	}

	public void addDuration(Properties p, Double d) {
		Double dMin;
		Double dMax;
		// d = d*(10^6);
		if ((p.getProperty("dMin") == null) && (p.getProperty("dMax") == null)) {
			// p.setProperty("dMin", Integer.toString((int)(Math.floor(d))));
			// p.setProperty("dMax", Integer.toString((int)(Math.floor(d))));
			p.setProperty("dMin", d.toString());
			p.setProperty("dMax", d.toString());
			return;
		} else {
			dMin = Double.parseDouble(p.getProperty("dMin"));
			dMax = Double.parseDouble(p.getProperty("dMax"));
			if (d < dMin) {
				dMin = d;
			} else if (d > dMax) {
				dMax = d;
			}
		}
		p.setProperty("dMin", dMin.toString());
		p.setProperty("dMax", dMax.toString());
		// p.setProperty("dMin", Integer.toString((int)(Math.floor(dMin))));
		// p.setProperty("dMax", Integer.toString((int)(Math.ceil(dMax))));
	}

	public String getPlaceInfoIndex(String s) {
		String index = null;
		for (String st2 : placeInfo.keySet()) {
			if (("p" + placeInfo.get(st2).getProperty("placeNum"))
					.equalsIgnoreCase(s)) {
				index = st2;
				break;
			}
		}
		return index;
	}
	
	public String getTransientNetPlaceIndex(String s) {
		String index = null;
		for (String st2 : transientNetPlaces.keySet()) {
			if (("p" + transientNetPlaces.get(st2).getProperty("placeNum"))
					.equalsIgnoreCase(s)) {
				index = st2;
				break;
			}
		}
		return index;
	}
	
	public ArrayList<Integer> diff(String pre_bin, String post_bin) {
		ArrayList<Integer> diffL = new ArrayList<Integer>();
		// String p_bin[] = p.getBinEncoding();
//		String[] preset_encoding = pre_bin.split("");
//		String[] postset_encoding = post_bin.split("");
		String[] preset_encoding = pre_bin.split(",");
		String[] postset_encoding = post_bin.split(",");
		for (int j = 0; j < preset_encoding.length; j++) { // to account for ""
			// being created in the array
			if (Integer.parseInt(preset_encoding[j]) != Integer
					.parseInt(postset_encoding[j])) {
				diffL.add(j);// to account for "" being created in the
				// array
			}
		}
		return (diffL);
	}

	public int getMinRate(String place, String name) {
		Properties p = placeInfo.get(place);
		return ((int) Math.floor(Double.parseDouble(p.getProperty(name
				+ "_rMin"))));
		// return(rMin[i]);
	}

	public int getMaxRate(String place, String name) {
		Properties p = placeInfo.get(place);
		return ((int) Math.floor(Double.parseDouble(p.getProperty(name
				+ "_rMax"))));
		// return(rMin[i]);
	}
	
	
	public boolean compareBins(int j, int mark,int[][] bins) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;
	}
	

	public void writeVHDLAMSFile(String vhdFile){
		try{
			ArrayList<String> ratePlaces = new ArrayList<String>();
			ArrayList<String> dmvcPlaces = new ArrayList<String>();
			File VHDLFile = new File(directory + separator + vhdFile);
			VHDLFile.createNewFile();
			BufferedWriter vhdlAms = new BufferedWriter(new FileWriter(VHDLFile));
			StringBuffer buffer = new StringBuffer();
			String pNum;
			vhdlAms.write("library IEEE;\n");
			vhdlAms.write("use IEEE.std_logic_1164.all;\n");
			vhdlAms.write("use work.handshake.all;\n");
			vhdlAms.write("use work.nondeterminism.all;\n\n");
			vhdlAms.write("entity amsDesign is\n");
			vhdlAms.write("end amsDesign;\n\n");
			vhdlAms.write("architecture "+vhdFile.split("\\.")[0]+" of amsDesign is\n");
			for (Variable v : reqdVarsL){
				vhdlAms.write("\tquantity "+v.getName()+":real;\n");
			}
			for (int i = 0; i < numPlaces; i++){
				if (!isTransientPlace("p"+i)){
					String p = getPlaceInfoIndex("p"+i);
					if (!placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				else {
					String p = getTransientNetPlaceIndex("p"+i);
					if (!transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				vhdlAms.write("\tshared variable place:integer:= "+i+";\n");
				break;
			}
			if (failPropVHDL != null){
				//TODO: Make this an assertion
				//vhdlAms.write("\tshared variable fail:boolean:= false;\n");
			}
			for (String st1 : g.getPlaceList()){
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						ratePlaces.add(st1); // w.r.t g here
					}
					/* Related to the separate net for DMV input driver
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("DMVC")) {
						dmvcPlaces.add(p); // w.r.t placeInfo here
					}*/
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}
					/* Related to the separate net for DMV input driver
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("DMVC")){
						dmvcPlaces.add(p); // w.r.t g here
					}*/
				}
			}
			/*for (String st:dmvcPlaces){
				System.out.println("p" + placeInfo.get(st).getProperty("placeNum") + "," +placeInfo.get(st).getProperty("DMVCVariable"));
			}*/ 
			/*
			Collections.sort(dmvcPlaces,new Comparator<String>(){
				public int compare(String a, String b){
					String v1 = placeInfo.get(a).getProperty("DMVCVariable");
					String v2 = placeInfo.get(b).getProperty("DMVCVariable");
					if (reqdVarsL.indexOf(v1) < reqdVarsL.indexOf(v2)){
						return -1;
					}
					else if (reqdVarsL.indexOf(v1) == reqdVarsL.indexOf(v2)){
						return 0;
					}
					else{
						return 1;
					}
				}
			});*/
			Collections.sort(dmvcPlaces,new Comparator<String>(){
				public int compare(String a, String b){
					if (Integer.parseInt(a.split("_")[1]) < Integer.parseInt(b.split("_")[1])){
						return -1;
					}
					else if (Integer.parseInt(a.split("_")[1]) == Integer.parseInt(b.split("_")[1])){
						return 0;
					}
					else{
						return 1;
					}
				}
			});
			Collections.sort(ratePlaces,new Comparator<String>(){
				String v1,v2;
				public int compare(String a, String b){
					if (!isTransientPlace(a) && !isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else if (!isTransientPlace(a) && isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
					else if (isTransientPlace(a) && !isTransientPlace(b)){
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else {
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
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
			// sending the initial place to the end of the list. since if statements begin with preset of each place
			//ratePlaces.add(ratePlaces.get(0)); 
			//ratePlaces.remove(0);
			
			/*System.out.println("after sorting:");
			for (String st:dmvcPlaces){
				System.out.println("p" + placeInfo.get(st).getProperty("placeNum") + "," +placeInfo.get(st).getProperty("DMVCVariable"));
			}*/
			vhdlAms.write("begin\n");
			//buffer.append("\nbegin\n");
			String[] vals;
			for (Variable v : reqdVarsL){ // taking the lower value from the initial value range. Ok?
				//vhdlAms.write("\tbreak "+v.getName()+" => "+((((v.getInitValue()).split("\\,"))[0]).split("\\["))[1]+".0;\n");
				vals = v.getInitValue().split("\\,");
				vhdlAms.write("\tbreak "+v.getName()+" => span("+((vals[0]).split("\\["))[1]+".0,"+ ((vals[1]).split("\\]"))[0] +".0);\n");
				//buffer.append("\tbreak "+v.getName()+" => span("+((vals[0]).split("\\["))[1]+".0,"+ ((vals[1]).split("\\]"))[0] +".0);\n");
			}
			vhdlAms.write("\n");
			//buffer.append("\n");
			for (Variable v : reqdVarsL){
				if (v.isDmvc()){
					vhdlAms.write("\t"+v.getName()+"'dot == 0.0;\n");
					//buffer.append("\t"+v.getName()+"'dot == 0.0;\n");
				}
			}
			vhdlAms.write("\n");
			//buffer.append("\n");
			ArrayList<ArrayList<String>> dmvcVarPlaces = new ArrayList<ArrayList<String>>();
			boolean contVarExists = false;
			for (Variable v: reqdVarsL){
				dmvcVarPlaces.add(new ArrayList<String>());
				if (v.isDmvc()){
					continue;
				}
				else{
					contVarExists = true;
				}
			}
			for (String st:dmvcPlaces){
				dmvcVarPlaces.get(Integer.parseInt(st.split("_")[1])).add(st);
			}
			if (contVarExists){
				if (ratePlaces.size() != 0){
					//vhdlAms.write("\tcase place use\n");
					buffer.append("\tcase place use\n");
				}
				//vhdlAms.write("\ntype rate_places is (");
				for (String p : ratePlaces){
					pNum = p.split("p")[1];
					//vhdlAms.write("\t\twhen "+p.split("p")[1] +" =>\n");
					buffer.append("\t\twhen "+ pNum +" =>\n");
					if (!isTransientPlace(p)){
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
								//vhdlAms.write("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0);\n");
								buffer.append("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0);\n");
							}
						}
					}
					else{
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
								//vhdlAms.write("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0);\n");
								buffer.append("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getTransientNetPlaceIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getTransientNetPlaceIndex(p), reqdVarsL.get(j).getName())+".0);\n");
							}
						}
					}
				}
				vhdlAms.write(buffer.toString());
				vhdlAms.write("\t\twhen others =>\n");
				for (int j = 0; j<reqdVarsL.size(); j++){
					if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
						//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
					vhdlAms.write("\t\t\t" + reqdVarsL.get(j).getName() + "'dot == 0.0;\n");
					}
				}
				vhdlAms.write("\tend case;\n");
			}
			vhdlAms.write("\tprocess\n");
			vhdlAms.write("\tbegin\n");
			vhdlAms.write("\tcase place is\n");
			buffer.delete(0, buffer.length());
			String[] transL;
			int transNum;
			for (String p : ratePlaces){
				pNum = p.split("p")[1];
				vhdlAms.write("\t\twhen "+pNum +" =>\n");
				vhdlAms.write("\t\t\twait until ");
				transL = g.getPostset(p);
				if (transL.length == 1){
					transNum = Integer.parseInt(transL[0].split("t")[1]);
					vhdlAms.write(transEnablingsVHDL[transNum] + ";\n");
					if (transDelayAssignVHDL[transNum] != null){
						vhdlAms.write("\t\t\twait for "+ transDelayAssignVHDL[transNum]+";\n");
						//vhdlAms.write("\t\t\tbreak "+ transIntAssignVHDL[transNum]+";\n");
						for (String s : transIntAssignVHDL[transNum]){
							if (s != null){
								vhdlAms.write("\t\t\tbreak "+ s +";\n");
							}
						}
					}
					if (g.getPostset(transL[0]).length != 0)
						vhdlAms.write("\t\t\tplace := " + g.getPostset(transL[0])[0].split("p")[1] + ";\n");
				}
				else{
					boolean firstTrans = true;
					buffer.delete(0, buffer.length());
					for (String t : transL){
						transNum = Integer.parseInt(t.split("t")[1]);
						if (firstTrans){
							firstTrans = false;
							vhdlAms.write("(" + transEnablingsVHDL[transNum]);
							buffer.append("\t\t\tif " + transEnablingsVHDL[transNum] + " then\n");
							if (transDelayAssignVHDL[transNum] != null){
								buffer.append("\t\t\t\twait for "+ transDelayAssignVHDL[transNum]+";\n");
								for (String s : transIntAssignVHDL[transNum]){
									if (s != null){
										buffer.append("\t\t\t\tbreak "+ s +";\n");
									}
								}
							}
							buffer.append("\t\t\t\tplace := " + g.getPostset(t)[0].split("p")[1] + ";\n"); 
						}
						else{
							vhdlAms.write(" or " +transEnablingsVHDL[transNum] );
							buffer.append("\t\t\telsif " + transEnablingsVHDL[transNum] + " then\n");
							if (transDelayAssignVHDL[transNum] != null){
								buffer.append("\t\t\t\twait for "+ transDelayAssignVHDL[transNum]+";\n");
								//buffer.append("\t\t\t\tbreak "+ transIntAssignVHDL[transNum]+";\n");
								for (String s : transIntAssignVHDL[transNum]){
									if (s != null){
										buffer.append("\t\t\t\tbreak "+ s +";\n");
									}
								}
							}
							buffer.append("\t\t\t\tplace := " + g.getPostset(t)[0].split("p")[1] + ";\n"); 
						}
					}
					vhdlAms.write(");\n");
					buffer.append("\t\t\tend if;\n");
					vhdlAms.write(buffer.toString());
				}
			}
			vhdlAms.write("\t\twhen others =>\n\t\t\twait for 0.0;\n\t\t\tplace := "+ ratePlaces.get(0).split("p")[1] + ";\n\tend case;\n\tend process;\n");
			for (int i = 0; i < dmvcVarPlaces.size(); i++){
				if (dmvcVarPlaces.get(i).size() != 0){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				for (String p : dmvcVarPlaces.get(i)){
					if (!transientNetPlaces.containsKey(p)){
						vhdlAms.write("\t\twait for delay("+ (int) Math.floor(Double.parseDouble(placeInfo.get(p).getProperty("dMin")))+","+(int)Math.ceil(Double.parseDouble(placeInfo.get(p).getProperty("dMax"))) +");\n");
						// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
						vhdlAms.write("\t\tbreak "+reqdVarsL.get(i).getName()+ " => "+ (int) Math.floor(Double.parseDouble(placeInfo.get(p).getProperty("DMVCValue"))) + ".0;\n");
					}
					else{
						vhdlAms.write("\t\twait for delay("+ (int) Math.floor(Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin")))+","+(int)Math.ceil(Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))) +");\n");
						// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
						vhdlAms.write("\t\tbreak "+reqdVarsL.get(i).getName()+ " => "+ (int) Math.floor(Double.parseDouble(transientNetPlaces.get(p).getProperty("DMVCValue"))) + ".0;\n");
					}
				}
				vhdlAms.write("\tend process;\n\n");
				}
			}
			if (failPropVHDL != null){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				vhdlAms.write("\t\twait until " + failPropVHDL + ";\n");
				//TODO: Change this to assertion
				//vhdlAms.write("\t\tfail := true;\n");
				vhdlAms.write("\tend process;\n\n");
			}
		//	vhdlAms.write("\tend process;\n\n");
			vhdlAms.write("end "+vhdFile.split("\\.")[0]+";\n");
			vhdlAms.close();
			
		}
		catch(IOException e){
			JOptionPane.showMessageDialog(biosim.frame(),
					"VHDL-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(biosim.frame(),
					"Error in VHDL-AMS model generation.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void writeVerilogAMSFile(String vamsFileName){
		try{
			ArrayList<String> ratePlaces = new ArrayList<String>();
			ArrayList<String> dmvcPlaces = new ArrayList<String>();
			File vamsFile = new File(directory + separator + vamsFileName);
			vamsFile.createNewFile();
			Double rateFactor = valScaleFactor/delayScaleFactor;
			BufferedWriter vams = new BufferedWriter(new FileWriter(vamsFile));
			StringBuffer buffer = new StringBuffer();
			StringBuffer buffer2 = new StringBuffer();
			StringBuffer buffer3 = new StringBuffer();
			StringBuffer buffer4 = new StringBuffer();
			StringBuffer initBuffer = new StringBuffer();
			vams.write("`include \"constants.vams\"\n");
			vams.write("`include \"disciplines.vams\"\n");
			vams.write("`timescale 1ps/1ps\n\n");
			vams.write("module "+vamsFileName.split("\\.")[0]+" (");
			buffer.append("\tparameter delay = 0, rtime = 1p, ftime = 1p;\n");
			Variable v;
			String[] vals;
			for (int i = 0; i < reqdVarsL.size(); i++){
				v = reqdVarsL.get(i);
				if ( i!= 0){
					vams.write(",");
				}
				vams.write(" "+v.getName());
				if (v.isInput()){
					buffer.append("\tinput "+v.getName()+";\n\telectrical "+v.getName()+";\n");
				} else{
					buffer.append("\tinout "+v.getName()+";\n\telectrical "+v.getName()+";\n");
					if (!v.isDmvc()){
						buffer2.append("\treal change_"+v.getName()+";\n");
						buffer2.append("\treal rate_"+v.getName()+";\n");
						vals = v.getInitValue().split("\\,");
						double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
						initBuffer.append("\t\tchange_"+v.getName()+" = "+ spanAvg+";\n");
						vals = v.getInitRate().split("\\,");
						spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*rateFactor);
						initBuffer.append("\t\trate_"+v.getName()+" = "+ (int)spanAvg+";\n");
					}
					else{
						buffer2.append("\treal "+v.getName()+"Val;\n");	// changed from real to int.. check??
						vals = reqdVarsL.get(i).getInitValue().split("\\,");
						double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
						initBuffer.append("\t\t"+reqdVarsL.get(i).getName()+"Val = "+ spanAvg+";\n");
					}
				}
			}
		//	if (failPropVHDL != null){
		//		buffer.append("\toutput reg fail;\n\tlogic fail;\n");
		//		vams.write(", fail");
		//	}
			vams.write(");\n" + buffer+"\n");
			if (buffer2.length() != 0){
				vams.write(buffer2.toString());
			}
			vams.write("\treal entryTime;\n");
			if (vamsRandom){
				vams.write("\tinteger seed;\n\tinteger del;\n");
			}
			vams.write("\tinteger place;\n\n\tinitial\n\tbegin\n");
			vams.write(initBuffer.toString());
			vams.write("\t\tentryTime = 0;\n");
			if (vamsRandom){
				vams.write("\t\tseed = 0;\n");
			}
			buffer.delete(0, buffer.length());
			buffer2.delete(0, buffer2.length());
			for (int i = 0; i < numPlaces; i++){
				String p;
				if (!isTransientPlace("p"+i)){
					p = getPlaceInfoIndex("p"+i);
					if (!placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				else{
					p = getTransientNetPlaceIndex("p"+i);
					if (!transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						continue;
					}
				}
				vams.write("\t\tplace = "+i+";\n");
				break;
			}
			//if (failPropVHDL != null){
			//	vams.write("\t\t\tfail = 1'b0;\n");
			//}
			vams.write("\tend\n\n");
			for (String st1 : g.getPlaceList()){
				if (!isTransientPlace(st1)){
					String p = getPlaceInfoIndex(st1);
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
						ratePlaces.add(st1); // w.r.t g here
					}
					/* Related to the separate net for DMV input driver
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("DMVC")) {
						dmvcPlaces.add(p); // w.r.t placeInfo here
					}*/
					if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("PROP")) {

					}
				}
				else{
					String p = getTransientNetPlaceIndex(st1);
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("RATE")){
						ratePlaces.add(st1); // w.r.t g here
					}
					/* Related to the separate net for DMV input driver
					if (transientNetPlaces.get(p).getProperty("type").equalsIgnoreCase("DMVC")){
						dmvcPlaces.add(p); // w.r.t  placeInfo here
					}*/
				}
			}
			/*for (String st:dmvcPlaces){
				System.out.println("p" + placeInfo.get(st).getProperty("placeNum") + "," +placeInfo.get(st).getProperty("DMVCVariable"));
			}*/ 
			Collections.sort(dmvcPlaces,new Comparator<String>(){
				public int compare(String a, String b){
					if (Integer.parseInt(a.split("_")[1]) < Integer.parseInt(b.split("_")[1])){
						return -1;
					}
					else if (Integer.parseInt(a.split("_")[1]) == Integer.parseInt(b.split("_")[1])){
						if (Integer.parseInt(a.split("_")[2]) < Integer.parseInt(b.split("_")[2])){
							return -1;
						}
						else if (Integer.parseInt(a.split("_")[2]) == Integer.parseInt(b.split("_")[2])){
							return 0;
						}
						else{
							return 1;
						}
					}
					else{
						return 1;
					}
				}
			});
			Collections.sort(ratePlaces,new Comparator<String>(){
				String v1,v2;
				public int compare(String a, String b){
					if (!isTransientPlace(a) && !isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else if (!isTransientPlace(a) && isTransientPlace(b)){
						v1 = placeInfo.get(getPlaceInfoIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
					else if (isTransientPlace(a) && !isTransientPlace(b)){
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = placeInfo.get(getPlaceInfoIndex(b)).getProperty("placeNum");
					}
					else {
						v1 = transientNetPlaces.get(getTransientNetPlaceIndex(a)).getProperty("placeNum");
						v2 = transientNetPlaces.get(getTransientNetPlaceIndex(b)).getProperty("placeNum");
					}
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
			ArrayList<String> transitions = new ArrayList<String>(); 
			for (String t : g.getTransitionList()){
				transitions.add(t);
			}
			Collections.sort(transitions,new Comparator<String>(){
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
			// sending the initial place to the end of the list. since if statements begin with preset of each place
			//ratePlaces.add(ratePlaces.get(0)); 
			//ratePlaces.remove(0);
			
			ArrayList<ArrayList<String>> dmvcVarPlaces = new ArrayList<ArrayList<String>>();
			boolean contVarExists = false;
			for (Variable var: reqdVarsL){
				dmvcVarPlaces.add(new ArrayList<String>());
				if (var.isDmvc()){
					continue;
				}
				else{
					contVarExists = true;
				}
			}
			for (String st:dmvcPlaces){
				dmvcVarPlaces.get(Integer.parseInt(st.split("_")[1])).add(st);
			}
			int transNum;
			buffer.delete(0, buffer.length());
			initBuffer.delete(0, initBuffer.length());
			String presetPlace = null,postsetPlace = null;
			StringBuffer[] transBuffer = new StringBuffer[transitions.size()];
			int cnt = 0;
			StringBuffer transAlwaysPlaceBuffer = new StringBuffer();
			int placeAlwaysBlockNum = -1;
			for (String t : transitions){
				presetPlace = g.getPreset(t)[0];
				//if (g.getPostset(t) != null){
				//	postsetPlace = g.getPostset(t)[0];
				//}
				transNum = Integer.parseInt(t.split("t")[1]);
				cnt = transNum;
				if (!isTransientTransition(t)){
					if (placeInfo.get(getPlaceInfoIndex(presetPlace)).getProperty("type").equals("RATE")){
						if (g.getPostset(t).length != 0)
							postsetPlace = g.getPostset(t)[0];
						for (int j = 0; j < transNum; j++){
							if ((transEnablingsVAMS[j] != null) && (transEnablingsVAMS[j].equalsIgnoreCase(transEnablingsVAMS[transNum]))){
								cnt = j;
								break;
							}
						}
						if ( cnt == transNum){
							transBuffer[cnt] = new StringBuffer();
							if (transEnablingsVAMS[transNum].equalsIgnoreCase("")){
								//transBuffer[cnt].append("\talways@(place)" + "\n\tbegin\n"); May 14, 2010
								placeAlwaysBlockNum = cnt;
							}
							else{
								transBuffer[cnt].append("\t"+transEnablingsVAMS[transNum] + "\n\tbegin\n");	
							    transAlwaysPlaceBuffer.append("\t\t" + transConditionalsVAMS[transNum] + "\n\t\tbegin\n\t\t\tentryTime = $abstime;\n");
							    if (g.getPostset(t).length != 0)
							    	transAlwaysPlaceBuffer.append("\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");	
							}
						}
						else{
							String s = transBuffer[cnt].toString();
							s = s.replaceAll("\t\tend\n\tend\n", "\t\tend\n");
							transBuffer[cnt].delete(0, transBuffer[cnt].length());
							transBuffer[cnt].append(s);
							if (!transEnablingsVAMS[transNum].equalsIgnoreCase("")){
								transAlwaysPlaceBuffer.append("\t\t" + transConditionalsVAMS[transNum] + "\n\t\tbegin\n\t\t\tentryTime = $abstime;\n" + "\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");
							}
						}
						transBuffer[cnt].append("\t\tif (place == "+ presetPlace.split("p")[1] +")\n\t\tbegin\n");
						if (transDelayAssignVAMS[transNum] != null){
							transBuffer[cnt].append("\t\t\t"+transDelayAssignVAMS[transNum]+";\n");
							for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
								if (transIntAssignVAMS[transNum][i] != null){
									transBuffer[cnt].append("\t\t\t"+ transIntAssignVAMS[transNum][i]);
								}
							}
						}
						transBuffer[cnt].append("\t\t\tentryTime = $abstime;\n");
						transBuffer[cnt].append("\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								transBuffer[cnt].append("\t\t\trate_"+reqdVarsL.get(j).getName()+ " = "+(int)((getMinRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName())+getMaxRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName()))/(2.0*rateFactor)) + ";\n");
								transBuffer[cnt].append("\t\t\tchange_" + reqdVarsL.get(j).getName()+ " = V("+ reqdVarsL.get(j).getName()+ ");\n");
								if (!transEnablingsVAMS[transNum].equalsIgnoreCase("")){
									transAlwaysPlaceBuffer.append("\t\t\trate_"+reqdVarsL.get(j).getName()+ " = "+(int)((getMinRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName())+getMaxRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName()))/(2.0*rateFactor)) + ";\n");
									transAlwaysPlaceBuffer.append("\t\t\tchange_" + reqdVarsL.get(j).getName()+ " = V("+ reqdVarsL.get(j).getName()+ ");\n");
								}
							}
						}
						transBuffer[cnt].append("\t\tend\n");
						//if ( cnt == transNum){
							transBuffer[cnt].append("\tend\n");
						//}
						if (!transEnablingsVAMS[transNum].equalsIgnoreCase("")){
							transAlwaysPlaceBuffer.append("\t\tend\n");
						}
					}
				}
				else{
					if (transientNetPlaces.get(getTransientNetPlaceIndex(presetPlace)).getProperty("type").equals("RATE")){
						if (g.getPostset(t).length != 0)
							postsetPlace = g.getPostset(t)[0];
						for (int j = 0; j < transNum; j++){
							if ((transEnablingsVAMS[j] != null) && (transEnablingsVAMS[j].equalsIgnoreCase(transEnablingsVAMS[transNum]))){
								cnt = j;
								break;
							}
						}
						if ( cnt == transNum){
							transBuffer[cnt] = new StringBuffer();
							transBuffer[cnt].append("\t"+transEnablingsVAMS[transNum] + "\n\tbegin\n");
						}
						else{
							String s = transBuffer[cnt].toString();
							s = s.replaceAll("\t\tend\n\tend\n", "\t\tend\n");
							transBuffer[cnt].delete(0, transBuffer[cnt].length());
							transBuffer[cnt].append(s);
						}
						transBuffer[cnt].append("\t\tif (place == "+ presetPlace.split("p")[1] +")\n\t\tbegin\n");
						if (transDelayAssignVAMS[transNum] != null){
							transBuffer[cnt].append("\t\t\t"+transDelayAssignVAMS[transNum]+";\n");
							for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
								if (transIntAssignVAMS[transNum][i] != null){
									transBuffer[cnt].append("\t\t\t"+ transIntAssignVAMS[transNum][i]);
								}
							}
						}
						transBuffer[cnt].append("\t\t\tentryTime = $abstime;\n");
						if (g.getPostset(t).length != 0)
							transBuffer[cnt].append("\t\t\tplace = " + postsetPlace.split("p")[1] + ";\n");
						for (int j = 0; j<reqdVarsL.size(); j++){
							if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								transBuffer[cnt].append("\t\t\trate_"+reqdVarsL.get(j).getName()+ " = "+(int)((getMinRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName())+getMaxRate(getPlaceInfoIndex(postsetPlace), reqdVarsL.get(j).getName()))/(2.0*rateFactor)) + ";\n");
								transBuffer[cnt].append("\t\t\tchange_" + reqdVarsL.get(j).getName()+ " = V("+ reqdVarsL.get(j).getName()+ ");\n");
							}
						}
						transBuffer[cnt].append("\t\tend\n");
						//if ( cnt == transNum){
							transBuffer[cnt].append("\tend\n");
						//}
					}
				}
				//if (transDelayAssignVAMS[transNum] != null){
				//	buffer.append("\t"+transEnablingsVAMS[transNum] + "\n\tbegin\n");
				//  buffer.append("\t\tif (place == "+ presetPlace.split("p")[1] +")\n\t\tbegin\n");
				//	buffer.append("\t\t\t#"+transDelayAssignVAMS[transNum]+";\n");
				//	for (int i = 0; i < transIntAssignVAMS[transNum].length; i++){
				//		if (transIntAssignVAMS[transNum][i] != null){
				//			buffer.append("\t\t\t"+reqdVarsL.get(i).getName()+"Val = "+ transIntAssignVAMS[transNum][i]+";\n");
				//		}
				//	}
				//	buffer.append("\t\tend\n\tend\n");
				//}
			}
			if (placeAlwaysBlockNum == -1){
				vams.write("\talways@(place)" + "\n\tbegin\n");
				vams.write(transAlwaysPlaceBuffer.toString());
				vams.write("\tend\n");
			}
			else{
				String s = transBuffer[placeAlwaysBlockNum].toString();
				s = s.replaceAll("\t\tend\n\tend\n", "\t\tend\n");
				transBuffer[placeAlwaysBlockNum].delete(0, transBuffer[placeAlwaysBlockNum].length());
				transBuffer[placeAlwaysBlockNum].append("\talways@(place)" + "\n\tbegin\n");
				transBuffer[placeAlwaysBlockNum].append(transAlwaysPlaceBuffer);
				transBuffer[placeAlwaysBlockNum].append(s);
				transBuffer[placeAlwaysBlockNum].append("\tend\n");
			}
			for (int j = 0; j < transitions.size(); j++){
				if (transBuffer[j] != null){
					vams.write(transBuffer[j].toString());
				}
			}
			vams.write("\tanalog\n\tbegin\n");
			for (int j = 0; j<reqdVarsL.size(); j++){
				if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){	
					vams.write("\t\tV("+reqdVarsL.get(j).getName()+") <+ transition(change_" + reqdVarsL.get(j).getName() + " + rate_" + reqdVarsL.get(j).getName()+"*($abstime-entryTime));\n");
				}
				if ((!reqdVarsL.get(j).isInput()) && (reqdVarsL.get(j).isDmvc())){
					vams.write("\t\tV("+reqdVarsL.get(j).getName()+") <+ transition(" + reqdVarsL.get(j).getName() + "Val,delay,rtime,ftime);\n");
					//vals = reqdVarsL.get(j).getInitValue().split("\\,");
					//double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
					//initBuffer.append("\t\t"+reqdVarsL.get(j).getName()+"Val = "+ spanAvg+";\n");
				}
			}
			vams.write("\tend\n");
		//	if (initBuffer.length() != 0){
		//		vams.write("\n\tinitial\n\tbegin\n"+initBuffer+"\tend\n");
		//	}
			//if (buffer.length() != 0){
			//	vams.write(buffer.toString());
			//}
			vams.write("endmodule\n\n");
			buffer.delete(0, buffer.length());
			buffer2.delete(0, buffer2.length());
			initBuffer.delete(0, initBuffer.length());
			int count = 0;
			for (int i = 0; i < dmvcVarPlaces.size(); i++){
				if (dmvcVarPlaces.get(i).size() != 0){
					if (count == 0){
						vams.write("module driver ( " + reqdVarsL.get(i).getName() + "drive ");
						count++;
					}
					else{
						vams.write(", " + reqdVarsL.get(i).getName() + "drive ");
						count++;
					}
					buffer.append("\n\toutput "+ reqdVarsL.get(i).getName() + "drive;\n");
					buffer.append("\telectrical "+ reqdVarsL.get(i).getName() + "drive;\n");
					buffer2.append("\treal " + reqdVarsL.get(i).getName() + "Val" + ";\n");
					vals = reqdVarsL.get(i).getInitValue().split("\\,");
					double spanAvg = (Double.parseDouble(((vals[0]).split("\\["))[1])+Double.parseDouble(((vals[1]).split("\\]"))[0]))/(2.0*valScaleFactor);
					initBuffer.append("\n\tinitial\n\tbegin\n"+"\t\t"+ reqdVarsL.get(i).getName() + "Val = "+ spanAvg+";\n");
					if ((count == 1) && (vamsRandom) ){
						initBuffer.append("\t\tseed = 0;\n");
					}
					//buffer3.append("\talways\n\tbegin\n");
					boolean transientDoneFirst = false;
					for (String p : dmvcVarPlaces.get(i)){
						if (!transientNetPlaces.containsKey(p)){	// since p is w.r.t placeInfo & not w.r.t g
						//	buffer3.append("\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
						//	buffer3.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + placeInfo.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n");
							if (transientDoneFirst){
								initBuffer.append("\t\tforever\n\t\tbegin\n");
								transientDoneFirst = false;
							}
							if (g.getPostset("p" + placeInfo.get(p).getProperty("placeNum")).length != 0){
								if (!vamsRandom){
									//initBuffer.append("\t\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
									initBuffer.append("\t\t\t#"+  (int)(((Double.parseDouble(placeInfo.get(p).getProperty("dMin"))+ Double.parseDouble(placeInfo.get(p).getProperty("dMax"))))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
								}
								else{
									//initBuffer.append("\t\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(placeInfo.get(p).getProperty("dMin")))/delayScaleFactor)*Math.pow(10, 12)) + "," +(int)Math.ceil((Double.parseDouble(placeInfo.get(p).getProperty("dMax"))/delayScaleFactor)*Math.pow(10, 12)) + ");\n\t\t\t#del ");	// converting seconds to ns using math.pow(10,9)
									initBuffer.append("\t\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(placeInfo.get(p).getProperty("dMin")))/delayScaleFactor)) + "," +(int)Math.ceil((Double.parseDouble(placeInfo.get(p).getProperty("dMax"))/delayScaleFactor)) + ");\n\t\t\t#del ");	// converting seconds to ns using math.pow(10,9)
								}
								initBuffer.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + placeInfo.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n");
							}
							else{

							}
						}
						else{
							/*buffer3.append("\tinitial\n\tbegin\n");
							buffer3.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
							buffer3.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + transientNetPlaces.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n");
							buffer3.append("\tend\n");*/
							transientDoneFirst = true;
							if (!vamsRandom){
								//initBuffer.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax")))*Math.pow(10, 12))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
								initBuffer.append("\t\t#"+  (int)(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin"))+ Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))))/(2.0*delayScaleFactor)) +" "); // converting seconds to nanosec. hence pow(10,9)
							}
							else{
								//initBuffer.append("\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin")))/delayScaleFactor)*Math.pow(10, 12)) + "," +(int)Math.ceil((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))/delayScaleFactor)*Math.pow(10, 12)) + ");\n\t\t#del ");	// converting seconds to ns using math.pow(10,9)
								initBuffer.append("\t\tdel = $dist_uniform(seed," + (int)Math.floor(((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMin")))/delayScaleFactor)) + "," +(int)Math.ceil((Double.parseDouble(transientNetPlaces.get(p).getProperty("dMax"))/delayScaleFactor)) + ");\n\t\t#del ");	// converting seconds to ns using math.pow(10,9)
							}
							initBuffer.append(reqdVarsL.get(i).getName()+ "Val = "+  ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(g.getPostset(g.getPostset("p" + transientNetPlaces.get(p).getProperty("placeNum"))[0])[0])).getProperty("DMVCValue"))))/valScaleFactor + ";\n" );
						//	initBuffer.append("\tend\n");
						}
					}
				//	buffer3.append("\tend\n");
					initBuffer.append("\t\tend\n\tend\n");
					buffer4.append("\t\tV("+reqdVarsL.get(i).getName() + "drive) <+ transition("+reqdVarsL.get(i).getName() + "Val,delay,rtime,ftime);\n");
				}
			}
			BufferedWriter topV = new BufferedWriter(new FileWriter(new File(directory + separator + "top.vams")));
			topV.write("`timescale 1ps/1ps\n\nmodule top();\n\n");
			if (count != 0){
				vams.write(");\n");
				vams.write("\tparameter delay = 0, rtime = 1p, ftime = 1p;\n");
				if (vamsRandom){
					vams.write("\tinteger del;\n\tinteger seed;\n");
				}
				vams.write(buffer+"\n"+buffer2+initBuffer+buffer3);
				vams.write("\tanalog\n\tbegin\n"+buffer4+"\tend\nendmodule");
				count = 0;
				for (int i = 0; i < dmvcVarPlaces.size(); i++){
					if (dmvcVarPlaces.get(i).size() != 0){
						if (count == 0){
							topV.write("\tdriver tb(\n\t\t." + reqdVarsL.get(i).getName() + "drive(" + reqdVarsL.get(i).getName() + ")");
							count++;
						}
						else{
							topV.write(",\n\t\t." + reqdVarsL.get(i).getName() + "drive(" + reqdVarsL.get(i).getName() + ")");
							count++;
						}
					}
				}
				topV.write("\n\t);\n\n");
			}
			for (int i = 0; i < reqdVarsL.size(); i++){
				v = reqdVarsL.get(i);
				if ( i== 0){
					topV.write("\t"+vamsFileName.split("\\.")[0]+" dut(\n\t\t."+ v.getName() + "(" + v.getName() + ")");
				}
				else{
					topV.write(",\n\t\t." + v.getName() + "(" + reqdVarsL.get(i).getName() + ")");
					count++;
				}
			}
			topV.write("\n\t);\n\nendmodule");
			topV.close();
			vams.close();
		/*if (failPropVHDL != null){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				vhdlAms.write("\t\twait until " + failPropVHDL + ";\n");
				vhdlAms.write("\t\tfail := true;\n");
				vhdlAms.write("\tend process;\n\n");
			}
		//	vhdlAms.write("\tend process;\n\n");
			vhdlAms.write("end "+vhdFile.split("\\.")[0]+";\n");*/
			
		}
		catch(IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Verilog-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(biosim.frame(),
					"Error in Verilog-AMS model generation.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	//T[] aux = (T[])a.clone();
	
	public LhpnFile mergeLhpns(LhpnFile l1,LhpnFile l2){//(LhpnFile l1, LhpnFile l2){
		String place1 = "p([-\\d]+)", place2 = "P([-\\d]+)";
		String transition1 = "t([-\\d]+)", transition2 = "T([-\\d]+)";
		int placeNum, transitionNum;
		int minPlace=0, maxPlace=0, minTransition = 0, maxTransition = 0;
		Boolean first = true;
		try{
			for (String st1: l1.getPlaceList()){
				if ((st1.matches(place1)) || (st1.matches(place2))){
					st1 = st1.replaceAll("p", "");
					st1 = st1.replaceAll("P", "");
					placeNum = Integer.valueOf(st1);
					if (placeNum > maxPlace){
						maxPlace = placeNum;
						if (first){
							first = false;
							minPlace = placeNum;
						}
					}
					if (placeNum < minPlace){
						minPlace = placeNum;
						if (first){
							first = false;
							maxPlace = placeNum;
						}
					}
				}
			}
			for (String st1: l2.getPlaceList()){
				if ((st1.matches(place1)) || (st1.matches(place2))){
					st1 = st1.replaceAll("p", "");
					st1 = st1.replaceAll("P", "");
					placeNum = Integer.valueOf(st1);
					if (placeNum > maxPlace)
						maxPlace = placeNum;
					if (placeNum < minPlace)
						minPlace = placeNum;
				}
			}
			//System.out.println("min place and max place in both lpns are : " + minPlace + "," + maxPlace);
			for (String st2: l2.getPlaceList()){
				for (String st1: l1.getPlaceList()){
					if (st1.equalsIgnoreCase(st2)){
						maxPlace++;
						l2.renamePlace(st2, "p" + maxPlace);//, l2.getPlace(st2).isMarked());
						break;
					}
				}
			}
			first = true;
			for (String st1: l1.getTransitionList()){
				if ((st1.matches(transition1)) || (st1.matches(transition2))){
					st1 = st1.replaceAll("t", "");
					st1 = st1.replaceAll("T", "");
					transitionNum = Integer.valueOf(st1);
					if (transitionNum > maxTransition){
						maxTransition = transitionNum;
						if (first){
							first = false;
							minTransition = transitionNum;
						}
					}
					if (transitionNum < minTransition){
						minTransition = transitionNum;
						if (first){
							first = false;
							maxTransition = transitionNum;
						}
					}
				}
			}
			for (String st1: l2.getTransitionList()){
				if ((st1.matches(transition1)) || (st1.matches(transition2))){
					st1 = st1.replaceAll("t", "");
					st1 = st1.replaceAll("T", "");
					transitionNum = Integer.valueOf(st1);
					if (transitionNum > maxTransition)
						maxTransition = transitionNum;
					if (transitionNum < minTransition)
						minTransition = transitionNum;
				}
			}
			//System.out.println("min transition and max transition in both lpns are : " + minTransition + "," + maxTransition);
			for (String st2: l2.getTransitionList()){
				for (String st1: l1.getTransitionList()){
					if (st1.equalsIgnoreCase(st2)){
						maxTransition++;
						l2.renameTransition(st2, "t" + maxTransition);
						break;
					}
				}
			}
			l2.save(directory + separator + "tmp.lpn");
			l1.load(directory + separator + "tmp.lpn");
			File tmp = new File(directory + separator + "tmp.lpn");
			tmp.delete();
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(biosim.frame(),
					"Problem while merging lpns",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		return l1;
	}
}