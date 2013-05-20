package learn;

//import gcm2sbml.parser.GCMFile;
import lpn.parser.LhpnFile;
import lpn.parser.Lpn2verilog;
import main.*;
import main.util.dataparser.*;

import java.io.*;
import java.util.*;

//import javax.jws.soap.InitParam;
import javax.swing.*;



//import org.sbml.libsbml.*;

/**
 * This class generates an LHPN model from the simulation traces provided
 * in the learn view. The generated LHPN is stored in an object of type
 * lhpn2sbml.parser.LHPNfile . It is then saved in *.lpn file using the
 * save() method of the above class.
 * 
 * Rev. 1 - Kevin Jones
 * Rev. 2 - Scott Little (data2lhpn.py) 
 * Rev. 3 - Satish Batchu ( dataToLHPN() )
 */


public class LearnModel { // added ItemListener SB

	private static final long serialVersionUID = -5806315070287184299L;

	private String directory, lrnFile;

	private Log log;

	private String separator;

	private String learnFile, lhpnFile;

	private ArrayList<Variable> reqdVarsL;
	
	//private ArrayList<String> careVars;

	private ArrayList<Integer> reqdVarIndices;

	private ArrayList<ArrayList<Double>> data;

	private ArrayList<String> varNames;

	//private HashMap<String, int[]> careBins; 
	
	private int[][] bins;

	private ArrayList<ArrayList<Double>> divisionsL;
	
	private HashMap<String, ArrayList<Double>> thresholds;

	private Double[][] rates;
	
	private double[][] values;

	private Double[] duration;

	private int pathLengthBin ; //= 7 ;// intFixed 25 pd 7 integrator 15;

	private int rateSampling ; //= -1 ; //intFixed 250; 20; //-1;

	private LhpnFile g;

	private Integer numPlaces = 0;

	private Integer numTransitions = 0;

	private HashMap<String, Properties> placeInfo;

	private HashMap<String, Properties> transitionInfo;
	
	private HashMap<String, Properties> cvgInfo;

	private Double minDelayVal = 10.0;

	private Double minRateVal = 10.0;

	private Double minDivisionVal = 10.0;

	private Double delayScaleFactor = 1.0;

	private Double valScaleFactor = 1.0;

	BufferedWriter out;

	File logFile;

	// Threshold parameters
//	private double epsilon ;//= 0.1; // What is the +/- epsilon where signals are considered to be equivalent

	private Integer runLength ; //= 15; // the number of time points that a value must persist to be considered constant

	private Double runTime ; // = 5e-12; // 10e-6 for intFixed; 5e-6 for integrator. 5e-12 for pd;// the amount of time that must pass to be considered constant when using absoluteTime

	private boolean absoluteTime ; // = true; // true for intfixed //false; true for pd; false for integrator// when False time points are used to determine DMVC and when true absolutime time is used to determine DMVC

	private double percent ; // = 0.8; // a decimal value representing the percent of the total trace that must be constant to qualify to become a DMVC var
	
	private Double[] lowerLimit;
	
	private Double[] upperLimit; 
	
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

	private ArrayList<String> propPlaces;
	
	private boolean vamsRandom = false;
	
	private HashMap<String,Properties> dmvcValuesUnique;
	
	private Double dsFactor, vsFactor;
	
	private String currentPlace;
	
	private String[] currPlaceBin;
	
	private LhpnFile lpnWithPseudo;
	
	private int pseudoTransNum = 0;
	
	private HashMap<String,Boolean> pseudoVars;
	
	private ArrayList<HashMap<String, String>> constVal;
	
	private boolean dmvDetectDone = false;

	private boolean dmvStatusLoaded = false;
	
	private int pathLengthVar = 40;
	
	private double unstableTime;

	private boolean binError;

	private HashMap<String, ArrayList<String>> destabMap;

	private double stableTolerance;
	
	private ArrayList<String> startPlaces;
	
	// Pattern lParenR = Pattern.compile("\\(+"); 
	
	//Pattern floatingPointNum = Pattern.compile(">=(-*[0-9]+\\.*[0-9]*)"); 

	// Pattern falseR = Pattern.compile("false",Pattern.CASE_INSENSITIVE); //pass the I flag to be case insensitive

	/**
	 * This is a constructor for learning LPN models from simulation data. This could
	 * have been just a method in LearnLHPN class but because it started with 
	 * a lot of global variables/fields, it ended up being a constructor in a 
	 * separate class.
	 * 
	 *  Version 1 : Kevin Jones (Perl)
	 *  Version 2 : Scott Little (data2lhpn.py)
	 *  Version 3 : Satish Batchu (LearnModel.java)
	 * @throws IOException 
	 */
	
	public LhpnFile learnModel(String directory, Log log, Gui biosim, int moduleNumber, HashMap<String, 
			ArrayList<Double>> thresh, HashMap<String,Double> tPar, ArrayList<Variable> rVarsL, 
			HashMap<String, ArrayList<String>> dstab, Boolean netForStable, boolean pseudoEnable, 
			boolean transientPlaceReqd, Double vScaleFactor, Double dScaleFactor, String failProp) throws IOException {
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}
		// Assign the parameters received from the call to the fields of this class
		this.log = log;
		this.directory = directory;
		this.reqdVarsL = rVarsL;
		this.thresholds = thresh;
		this.valScaleFactor = vScaleFactor;
		this.delayScaleFactor = dScaleFactor;
		this.destabMap = dstab;
		String[] getFilename = directory.split(separator);
		if (moduleNumber == 0)
			lhpnFile = getFilename[getFilename.length - 1] + ".lpn";
		else
			lhpnFile = getFilename[getFilename.length - 1] + moduleNumber + ".lpn";
		//	epsilon = tPar.get("epsilon");
		pathLengthBin = (int) tPar.get("pathLengthBin").doubleValue();
		pathLengthVar = (int) tPar.get("pathLengthVar").doubleValue();
		rateSampling = (int) tPar.get("rateSampling").doubleValue();
		percent = tPar.get("percent");
		if (tPar.containsKey("runTime")){ //only runTime or runLength is required based on gui selection
			runTime = tPar.get("runTime");
			runLength = null;
		} else{ 
			runLength = (int) tPar.get("runLength").doubleValue();
			runTime = null;
		}
		//unstableTime = tPar.get("unstableTime").doubleValue();
		stableTolerance = tPar.get("stableTolerance").doubleValue();
		
		new File(directory + separator + lhpnFile).delete();
		try {

			logFile = new File(directory + separator + "run.log");
			if (moduleNumber == 0)
				logFile.createNewFile(); //create new file first time
			out = new BufferedWriter(new FileWriter(logFile,true)); //appending
			
			//	resetAll();
			numPlaces = 0;
			numTransitions = 0;
			
			out.write("Running: dataToLHPN for module " + moduleNumber +  "\n");
			TSDParser tsd = new TSDParser(directory + separator + "run-1.tsd",
					false);
			varNames = tsd.getSpecies();
			//String[] learnDir = lrnFile.split("\\.");
			//File cvgFile = new File(directory + separator + learnDir[0] + ".cvg");
			File cvgFile = new File(directory + separator + "run.cvg");
			//cvgFile.createNewFile();
			BufferedWriter coverage = new BufferedWriter(new FileWriter(cvgFile));
			g = new LhpnFile(); // The generated lhpn is stored in this object
			placeInfo = new HashMap<String, Properties>();
			transitionInfo = new HashMap<String, Properties>();
			cvgInfo = new HashMap<String, Properties>();
			transientNetPlaces = new HashMap<String, Properties>();
			transientNetTransitions = new HashMap<String, Properties>();
			startPlaces = new ArrayList<String>();
			if ((failProp != null)){ //Construct a property net with single place and a fail trans
				propPlaces = new ArrayList<String>();
				Properties p0 = new Properties();
				placeInfo.put("failProp", p0);
				p0.setProperty("placeNum", numPlaces.toString());
				p0.setProperty("type", "PROP");
				p0.setProperty("initiallyMarked", "true");
				g.addPlace("p" + numPlaces, true);
				propPlaces.add("p" + numPlaces);
				numPlaces++;
				Properties p1 = new Properties();
				transitionInfo.put("failProp", p1);
				p1.setProperty("transitionNum", numTransitions.toString());
				g.addTransition("t" + numTransitions); // prevTranKey+key);
				g.addMovement("p" + placeInfo.get("failProp").getProperty("placeNum"), "t" + transitionInfo.get("failProp").getProperty("transitionNum")); 
				g.getTransition("t" + numTransitions).setFail(true);
				numTransitions++;
			}
			out.write("epsilon = " + "; ratesampling = " + rateSampling + "; pathLengthBin = " + pathLengthBin + "; percent = " + percent + "; runlength = " + runLength + "; runtime = " + runTime + "; absoluteTime = " + absoluteTime + "; delayscalefactor = " + delayScaleFactor + "; valuescalefactor = " + valScaleFactor + "\n");
			dsFactor = this.delayScaleFactor;
			vsFactor = this.valScaleFactor;
			dmvcValuesUnique = new HashMap<String, Properties>();
			constVal = new ArrayList<HashMap<String, String>>();
			int tsdFileNum = 1;
			while (new File(directory + separator + "run-" + tsdFileNum + ".tsd").exists()) {
				Properties cProp = new Properties();
				cvgInfo.put(String.valueOf(tsdFileNum), cProp);
				cProp.setProperty("places", String.valueOf(0));
				cProp.setProperty("transitions", String.valueOf(0));
				cProp.setProperty("rates", String.valueOf(0));
				cProp.setProperty("delays", String.valueOf(0));
				if (!netForStable)
					tsd = new TSDParser(directory + separator + "run-" + tsdFileNum + ".tsd", false);
				else 
					tsd = new TSDParser(directory + separator + "runWithStables-" + tsdFileNum + ".tsd", false);
				data = tsd.getData();
				varNames = tsd.getSpecies();
				if (((destabMap != null) && (destabMap.size() != 0)) ){ //remove stable if it was added
					for (int l = 0; l < varNames.size(); l++)
						if (varNames.get(l).equals("stable"))
							varNames.remove(l); // should remove stable from this list
					for (int l = 0; l < reqdVarsL.size(); l++)
						if (reqdVarsL.get(l).getName().equals("stable"))
							reqdVarsL.remove(l); // should remove stable from this list
					if (thresholds.containsKey("stable"))
						thresholds.remove("stable"); // remove for stable.
				}
				findReqdVarIndices();   
				genBinsRates(thresholds); 
				if ((destabMap != null) && (destabMap.size() != 0)){
					out.write("Generating data for stables \n");
					//addStablesToData(thresholds, destabMap);
					addStablesToData2(bins,duration,thresholds,destabMap, reqdVarsL);
					for (String s : varNames)
						out.write(s + " ");
					out.write("\n");
					tsd.setData(data);
					tsd.setSpecies(varNames);
					tsd.outputTSD(directory + separator + "runWithStables-" + tsdFileNum + ".tsd");
				}
				findReqdVarIndices();
				genBinsRates(thresholds); 
				detectDMV(data,false); 
				updateGraph(bins, rates, tsdFileNum, transientPlaceReqd, cProp);
				addLastTransitionInfo(g, data.get(0).get(data.get(0).size() - 1) - data.get(0).get(0));
				//cProp.store(coverage,  "run-" + String.valueOf(i) + ".tsd");
				coverage.write("run-" + String.valueOf(tsdFileNum) + ".tsd\t");
				coverage.write("places : " + cProp.getProperty("places"));
				coverage.write("\ttransitions : " + cProp.getProperty("transitions") + "\n");
				tsdFileNum++;
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
					if (v.getInitValue() != null)
						g.addInteger(v.getName(), v.getInitValue());
				} else {
					if (v.getInitValue() != null) {
						initCond.put("value", v.getInitValue());
						initCond.put("rate", v.getInitRate());
						g.addContinuous(v.getName(), initCond);
					}
				}
			}
			HashMap<String, ArrayList<Double>> scaledThresholds; 
			scaledThresholds = normalize();
			//	globalValueScaling.setText(Double.toString(valScaleFactor));
			//	globalDelayScaling.setText(Double.toString(delayScaleFactor));
			initCond = new Properties(); 
			for (Variable v : reqdVarsL) {	// Updating with scaled initial values & rates
				if (v.isDmvc()) {
					if (v.getInitValue() != null)
						g.changeIntegerInitCond(v.getName(), v.getInitValue());
				} else {
					if (v.getInitValue() != null) {
						initCond.put("value", v.getInitValue());
						initCond.put("rate", v.getInitRate());
						g.changeContInitCond(v.getName(), initCond);
					}
				}
			}
			String[] transitionList = g.getTransitionList();
		/*	transEnablingsVAMS = new String[transitionList.length];
			transConditionalsVAMS = new String[transitionList.length];
			transIntAssignVAMS = new String[transitionList.length][reqdVarsL.size()];
			transDelayAssignVAMS = new String[transitionList.length];
		*/
			int transNum;
			for (String t : transitionList) {
				transNum = Integer.parseInt(t.split("t")[1]);
				if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
					if (!isTransientTransition(t)){
						if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
							// g.getPreset(t).length != 0 && g.getPostset(t).length != 0 ??
							String tKey = getTransitionInfoIndex(t);
							if (tKey==null) continue;
							String prevPlaceFullKey = getPresetPlaceFullKey(tKey);
							String nextPlaceFullKey = getPostsetPlaceFullKey(tKey);
							
							//ArrayList<Integer> diffL = diff(getPlaceInfoIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
							ArrayList<Integer> diffL = diff(prevPlaceFullKey, nextPlaceFullKey);
							String condStr = "";
						//	transEnablingsVAMS[transNum] = "";
							//String[] binIncoming = getPlaceInfoIndex(g.getPreset(t)[0]).split(",");
							//String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split(",");
							String[] binIncoming = prevPlaceFullKey.split(",");
							String[] binOutgoing = nextPlaceFullKey.split(",");
							Boolean firstInputBinChg = true;
							Boolean firstOutputBinChg = true;
							Boolean careOpChange = false, careIpChange = false;
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									// the above condition means that if the bin change is not on a non-input dmv variable, there won't be any enabling condition
									if (reqdVarsL.get(k).isCare())
										careIpChange = true;
									
									if (Integer.parseInt(binIncoming[k]) < Integer.parseInt(binOutgoing[k])) {
										//double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binIncoming[k])).doubleValue();
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])-1).doubleValue(); // changed on July 20, 2010
										if (firstInputBinChg){
											condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
									//		transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") >= " + ((int)val)/valScaleFactor +"))";
										}
										else{
											condStr += "&(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
									//		transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") >= " + ((int)val)/valScaleFactor +"))";
										}
									} else {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.floor(val) + ")"; //changed ceil to floor on aug 7,2010
									//		transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") < " + ((int)val)/valScaleFactor +"))";
										}
										else{
											condStr += "&~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.floor(val) + ")"; //changed ceil to floor on aug 7,2010
									//		transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
									//		transConditionalsVAMS[transNum] = "if ((place == " + g.getPreset(t)[0].split("p")[1] + ") && (V(" + reqdVarsL.get(k).getName() + ") < " + ((int)val)/valScaleFactor +"))";
										}
									}
									//if (diffL.get(diffL.size() - 1) != k) {
									//	condStr += "&";
										////COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									//}
									firstInputBinChg = false;
								}
								// Enablings Till above.. Below one is dmvc delay,assignment. Whenever a transition's preset and postset places differ in dmvc vars, then this transition gets the assignment of the dmvc value in the postset place and delay assignment of the preset place's duration range. This has to be changed after taking the causal relation input
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									if (reqdVarsL.get(k).isCare())
										careOpChange = true;
								//	String pPrev = g.getPreset(t)[0];
								//	String nextPlace = g.getPostset(t)[0];
									String nextPlaceKey = getPlaceInfoIndex(g.getPostset(t)[0]);
									//if (!isTransientTransition(t)){
								//	int mind = (int) Math.floor(Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + "," + getPlaceInfoIndex(nextPlace)).getProperty("dMin")));
								//	int maxd = (int) Math.ceil(Double.parseDouble(transitionInfo.get(getPlaceInfoIndex(pPrev) + "," + getPlaceInfoIndex(nextPlace)).getProperty("dMax")));
									int mind = 0;
									int maxd = 0;
									if (transitionInfo.get(tKey).getProperty("dMin") != null) {
										mind = (int) Math.floor(Double.parseDouble(transitionInfo.get(tKey).getProperty("dMin")));
									}
									if (transitionInfo.get(tKey).getProperty("dMax") != null) {
										maxd = (int) Math.ceil(Double.parseDouble(transitionInfo.get(tKey).getProperty("dMax")));
									}
									if (mind != maxd)
										g.changeDelay(t, "uniform(" + mind + "," + maxd + ")");
									else
										g.changeDelay(t, String.valueOf(mind));
									//int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									//int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax")));
									int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(nextPlaceKey).getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(nextPlaceKey).getProperty(reqdVarsL.get(k).getName() + "_vMax")));
									if (minv != maxv)
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"uniform(" + minv  + ","+ maxv + ")");
									else
										g.addIntAssign(t,reqdVarsL.get(k).getName(), String.valueOf(minv));
									int dmvTnum =  Integer.parseInt(t.split("t")[1]);
									if (!vamsRandom){
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = "+ ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor)+";\n";
								//		transDelayAssignVAMS[dmvTnum] = "#" + (int)((mind + maxd)/(2*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}
									else{
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = $dist_uniform(seed,"+ String.valueOf((int)Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))/valScaleFactor)) + "," + String.valueOf((int)Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))/valScaleFactor))+");\n";
								//		transDelayAssignVAMS[dmvTnum] = "del = $dist_uniform(seed," + (int) (mind/delayScaleFactor) + "," +(int) (maxd/delayScaleFactor) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
									}
									/*}
									else{
										g.changeDelay(t, "[" + (int) Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + "," + (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))) + "]");
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"[" + (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))) + ","+ (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))) + "]");
										int dmvTnum =  Integer.parseInt(t.split("t")[1]);
										transIntAssignVAMS[dmvTnum][k] = ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor);
										transDelayAssignVAMS[dmvTnum] =  (int)(((Math.floor(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMin"))) + Math.ceil(Double.parseDouble(transientNetPlaces.get(getTransientNetPlaceIndex(pPrev)).getProperty("dMax"))))*Math.pow(10, 12))/(2.0*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}*/
								}
							}
							if (careIpChange & careOpChange){ // Both ip and op changes on same transition. Then delay should be 0. Not the previous bin duration.
								if ((getPlaceInfoIndex(g.getPreset(t)[0]) != null) && (getPlaceInfoIndex(g.getPostset(t)[0]) != null)) 
									//if (!(transitionInfo.get(getPlaceInfoIndex(g.getPreset(t)[0]) + "," + getPlaceInfoIndex(g.getPostset(t)[0])).containsKey("ioChangeDelay")))
									if (!(transitionInfo.get(tKey).containsKey("ioChangeDelay"))){
										
										g.changeDelay(t, "0");
									}
							}
						//	if ((transEnablingsVAMS[transNum] != "") && (transEnablingsVAMS[transNum] != null)){ 
						//		transEnablingsVAMS[transNum] += ")";
						//	}
							if (!condStr.equalsIgnoreCase("")){
								//condStr += enFailAnd;
								g.addEnabling(t, condStr);
							}
							else{
								//Nothing added to Enabling condition.
								//condStr = enFail;
								//g.addEnabling(t, condStr);
							}
						}
					} else {
						Properties postsetPlace = null; // added for the sake of non-care outputs
						if (getTransientNetPlaceIndex(g.getPostset(t)[0]) != null) {
							postsetPlace = transientNetPlaces.get(getTransientNetPlaceIndex(g.getPostset(t)[0]));
						} else {
							postsetPlace = placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0]));
						}
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (postsetPlace.getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							String tKey = getTransientNetTransitionIndex(t);
							String prevPlaceFullKey = getPresetPlaceFullKey(tKey);
							String nextPlaceFullKey = getPostsetPlaceFullKey(tKey);
							//ArrayList<Integer> diffL = diff(getTransientNetPlaceIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));							
							ArrayList<Integer> diffL = diff(prevPlaceFullKey, nextPlaceFullKey);
							
							String condStr = "";
						//	transEnablingsVAMS[transNum] = "";
							//String[] binIncoming = getTransientNetPlaceIndex(g.getPreset(t)[0]).split(",");
							//String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split(",");
							String[] binIncoming = prevPlaceFullKey.split(",");
							String[] binOutgoing = nextPlaceFullKey.split(",");
							Properties nextPlace;
							String nextPlaceKey;
							if (getTransientNetPlaceIndex(g.getPostset(t)[0]) != null) {
								nextPlaceKey = getTransientNetPlaceIndex(g.getPostset(t)[0]);
								nextPlace = transientNetPlaces.get(nextPlaceKey);
							} else {
								nextPlaceKey = getPlaceInfoIndex(g.getPostset(t)[0]);
								nextPlace = placeInfo.get(nextPlaceKey);
								
							}
							Boolean firstInputBinChg = true;
							Boolean careOpChange = false, careIpChange = false;
							for (int k : diffL) {
								if (!((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput()))) {
									if (reqdVarsL.get(k).isCare())
										careIpChange = true;
									if (Integer.parseInt(binIncoming[k]) < Integer.parseInt(binOutgoing[k])) {
										//double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binIncoming[k])).doubleValue();
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])-1).doubleValue(); // changed on July 20, 2010
										if (firstInputBinChg){
											condStr += "(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
								//			transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
										}
										else{
											condStr += "&(" + reqdVarsL.get(k).getName() + ">=" + (int) Math.floor(val) + ")";
								//			transEnablingsVAMS[transNum] += " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),+1)";	// += temporary
										}
									} else {
										double val = scaledThresholds.get(reqdVarsL.get(k).getName()).get(Integer.parseInt(binOutgoing[k])).doubleValue();
										if (firstInputBinChg){
											condStr += "~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.floor(val) + ")";//changed ceil to floor on aug 7,2010
									//		transEnablingsVAMS[transNum] = "always@(cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
										} else {
											condStr += "&~(" + reqdVarsL.get(k).getName() + ">="	+ (int) Math.floor(val) + ")";//changed ceil to floor on aug 7,2010
									//		transEnablingsVAMS[transNum] = " and cross((V(" + reqdVarsL.get(k).getName() + ") - " + ((int)val)/valScaleFactor +"),-1)";	// +=; temporary
										}
									}
									//if (diffL.get(diffL.size() - 1) != k) {
									//	condStr += "&";
									//	//COME BACK???	temporary			transEnablingsVAMS[transNum] += 
									//}
									firstInputBinChg = false;
								}
								if ((reqdVarsL.get(k).isDmvc()) && (!reqdVarsL.get(k).isInput())) { // require few more changes here.should check for those variables that are constant over these regions and make them as causal????? thesis
									if (reqdVarsL.get(k).isCare())
										careOpChange = true;
									//String pPrev = g.getPreset(t)[0];
									//String nextPlace = g.getPostset(t)[0];
								//	String nextPlaceKey = getPlaceInfoIndex(g.getPostset(t)[0]);
									//int mind = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+ "," +getPlaceInfoIndex(nextPlace)).getProperty("dMin")));
									//int maxd = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(getTransientNetPlaceIndex(pPrev)+ "," +getPlaceInfoIndex(nextPlace)).getProperty("dMax")));
									int mind = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(tKey).getProperty("dMin")));
									int maxd = (int) Math.floor(Double.parseDouble(transientNetTransitions.get(tKey).getProperty("dMax")));
									if (mind != maxd)
										g.changeDelay(t, "uniform(" + mind + "," + maxd + ")");
									else
										g.changeDelay(t, String.valueOf(mind));
									//int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									//int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax")));
									int minv = (int) Math.floor(Double.parseDouble(nextPlace.getProperty(reqdVarsL.get(k).getName() + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(nextPlace.getProperty(reqdVarsL.get(k).getName() + "_vMax")));
									if (minv != maxv)
										g.addIntAssign(t,reqdVarsL.get(k).getName(),"uniform(" + minv + ","+ maxv + ")");
									else
										g.addIntAssign(t,reqdVarsL.get(k).getName(),String.valueOf(minv));
									int dmvTnum =  Integer.parseInt(t.split("t")[1]);
									if (!vamsRandom){
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = "+ ((int)(Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin")) + Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))))/(2.0*valScaleFactor)+";\n";;
								//		transDelayAssignVAMS[dmvTnum] =  "#" + (int)((mind + maxd)/(2*delayScaleFactor));	// converting seconds to ns using math.pow(10,9)
									}
									else{
								//		transIntAssignVAMS[dmvTnum][k] = reqdVarsL.get(k).getName()+"Val = $dist_uniform(seed,"+ String.valueOf(Math.floor((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMin"))/valScaleFactor))) + "," + String.valueOf(Math.ceil((Double.parseDouble(placeInfo.get(getPlaceInfoIndex(nextPlace)).getProperty(reqdVarsL.get(k).getName() + "_vMax"))/valScaleFactor)))+");\n";
								//		transDelayAssignVAMS[dmvTnum] =  "del = $dist_uniform(seed," + (int) (mind/delayScaleFactor) + "," + (int) (maxd/delayScaleFactor) + ");\n\t\t\t#del";	// converting seconds to ns using math.pow(10,9)
									}
								}
							}
							if (careIpChange & careOpChange){ // Both ip and op changes on same transition. Then delay should be 0. Not the previous bin duration.
								if ((getTransientNetPlaceIndex(g.getPreset(t)[0]) != null) && (nextPlaceKey != null)) 
									//if (!(transientNetTransitions.get(getTransientNetPlaceIndex(g.getPreset(t)[0]) + "," + getPlaceInfoIndex(g.getPostset(t)[0])).containsKey("ioChangeDelay")))
									if (!(transientNetTransitions.get(tKey).containsKey("ioChangeDelay")))
										g.changeDelay(t, "0");
							}
							if (diffL.size() > 1){
						//		transEnablingsVHDL[transNum] = "(" + transEnablingsVHDL[transNum] + ")";
							}
						//	if ((transEnablingsVAMS[transNum] != "") && (transEnablingsVAMS[transNum] != null)){ 
						//		transEnablingsVAMS[transNum] += ")";
						//	}
							if (!condStr.equalsIgnoreCase("")){
								g.addEnabling(t, condStr);
							}
							else{
								//g.addEnabling(t, condStr);
							}
						}
					}
				}
				if (failProp != null){
					if ((g.getPreset(t) != null) && (!isTransientTransition(t)) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("PROP"))){
						g.addEnabling(t, failProp);
					}
				}
			}
			ArrayList<String> placesWithoutPostsetTrans = new ArrayList<String>();
			ArrayList<String> dcVars = new ArrayList<String>();
			for (Variable v : reqdVarsL){
				if (!v.isCare())
					dcVars.add(v.getName());
			}
			for (String st1 : g.getPlaceList()) {
				if (g.getPostset(st1).length == 0){ // a place without a postset trans
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
						if ((varsInEnabling.keySet().size() >= 1) || (dcVars.size() > 0)){ // && (g.getEnablingTree(st2) != null))
							//String[] binOutgoing = getPlaceInfoIndex(g.getPostset(st2)[0]).split(",");
							String transKey;
							if (!isTransientTransition(st2))
								 transKey = getTransitionInfoIndex(st2);
							else
								 transKey = getTransientNetTransitionIndex(st2);
							if (transKey==null) continue;
							String[] binOutgoing = getPostsetPlaceFullKey(transKey).split(",");
							String condStr = "";
							for (String st : varsInEnabling.keySet()){
								int bin = Integer.valueOf(binOutgoing[findReqdVarslIndex(st)]);
								if (bin == 0){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "~(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
								} else if (bin == (scaledThresholds.get(st).size())){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st + ">="	+ (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")";
								} else{
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")&~(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin).doubleValue()) + ")";//changed ceil to floor on Aug7,2010
								}
							}
							for (String st : dcVars){
								if(!varsInEnabling.containsKey(st)){
									int bin = Integer.valueOf(binOutgoing[findReqdVarslIndex(st)]);
									if (bin == 0){
										if (!condStr.equalsIgnoreCase(""))
											condStr += "&";
										condStr += "~(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
									} else if (bin == (scaledThresholds.get(st).size())){
										if (!condStr.equalsIgnoreCase(""))
											condStr += "&";
										condStr += "(" + st + ">="	+ (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")";
									} else{
										if (!condStr.equalsIgnoreCase(""))
											condStr += "&";
										condStr += "(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")&~(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
									}
								}
							}
							out.write("Changed enabling of " + st2 + " to " + condStr + "\n");
							g.addEnabling(st2, condStr);
						}
					}
					/*for (String st2 : g.getPostset(st1)){
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
				String p;
				Properties placeP;
				if (getTransientNetPlaceIndex(s) != null){
					p = getTransientNetPlaceIndex(s);
					placeP = transientNetPlaces.get(p);
				} else {
					p = getPlaceInfoIndex(s);
					placeP = placeInfo.get(p);
				}
				if (placeP.getProperty("type").equalsIgnoreCase("RATE")) {
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
			//Reversed the order of adding initPlace and removing placeswithout postset transitions.
			//This makes sure that signals which are constant throughout a trace are not completely lost.
			addInitPlace(scaledThresholds);
			for (String st1 : placesWithoutPostsetTrans){
				//placeInfo.remove(getPlaceInfoIndex(st1));
				//g.removePlace(st1);
				int initPlaceNum = numPlaces - 1;
				g.addTransition("t" + numTransitions);
				g.addMovement(st1, "t" + numTransitions);
				g.addMovement("t" + numTransitions,"p" + initPlaceNum);
				out.write("Added transition t" + numTransitions + " b/w terminating place " + st1 + " and initPlace p" + initPlaceNum + "\n");
				String lastTransDel = null;
				if (getPlaceInfoIndex(st1) != null)
					lastTransDel = placeInfo.get(getPlaceInfoIndex(st1)).getProperty("lastTransitionDelay");
				else if (getTransientNetPlaceIndex(st1) != null)
					lastTransDel = transientNetPlaces.get(getTransientNetPlaceIndex(st1)).getProperty("lastTransitionDelay");
				if (lastTransDel != null) {
					g.changeDelay("t" + numTransitions, lastTransDel);
				}
				numTransitions++;
			}
			lpnWithPseudo = new LhpnFile();
			lpnWithPseudo = mergeLhpns(lpnWithPseudo,g);
			//if (pseudoEnable & ((moduleNumber == 0) || (moduleNumber == 1000))){
			if (pseudoEnable) {
				out.write("Adding pseudo transitions now. It'll be saved in " + directory + separator + "pseudo" + lhpnFile + "\n");
				addPseudo(scaledThresholds);
				//lpnWithPseudo.save(directory + separator + "pseudo" + lhpnFile);
			}
	//		addMetaBins();
	//		addMetaBinTransitions();
			if ((destabMap != null) || (destabMap.size() != 0)){
				HashMap<String, ArrayList<String>> dMap = new HashMap<String, ArrayList<String>>();
				int mNum = 1000;
				for (String destabOp : destabMap.keySet()){
					out.write("Generating stable signals with reqdVarsL as ");
					ArrayList <Variable> varsT = new ArrayList <Variable>();
					for (String d : destabMap.get(destabOp)){
						Variable input = new Variable("");
						input.copy(reqdVarsL.get(findReqdVarslIndex(d)));
						input.setCare(true);
						varsT.add(input);
						out.write(input.getName() + " ");
					}	
					Variable output = new Variable("");
					output.copy(reqdVarsL.get(findReqdVarslIndex("stable")));
					output.setInput(false);
					output.setOutput(true);
					//output.setCare(true);
					output.setCare(false);
					varsT.add(output);
					out.write(output.getName() + "\n");
					LearnModel l = new LearnModel();
					LhpnFile moduleLPN = l.learnModel(directory, log, biosim, mNum, thresholds, tPar, varsT, dMap, true, true, false, valScaleFactor, delayScaleFactor, null);
					//true parameter above indicates that the net being generated is for assigning stable
					// new Lpn2verilog(directory + separator + lhpnFile); //writeSVFile(directory + separator + lhpnFile);
			//		g = mergeLhpns(moduleLPN,g); // If pseudoTrans never required
					lpnWithPseudo = mergeLhpns(moduleLPN,lpnWithPseudo);
					mNum++;
				}
			}
			out.write("learning module done. Saving stuff and learning other modules.\n");
			lpnWithPseudo.save(directory + separator + lhpnFile);
			//new Lpn2verilog(directory + separator + lhpnFile); //writeSVFile(directory + separator + lhpnFile);
			out.write("Returning " + directory + separator + lhpnFile + "\n");
			out.close();
			
			//writeVerilogAMSFile(lhpnFile.replace(".lpn",".vams"));
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"LPN file couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch (NullPointerException e4) {
			out.close();
			e4.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"LPN file couldn't be created/written. Null exception",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch (ArrayIndexOutOfBoundsException e1) {	// comes from initMark = -1 of updateGraph()
			e1.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Unable to calculate rates.\nWindow size or pathLengthBin must be reduced.\nLearning unsuccessful.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
			try {
				out.write("ERROR! Unable to calculate rates.\nIf Window size = -1, pathLengthBin must be reduced;\nElse, reduce windowsize\nLearning unsuccessful.");
				out.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		} catch (java.lang.IllegalStateException e3){
			e3.printStackTrace();
			//System.out.println("LPN file couldn't be created/written ");
			JOptionPane.showMessageDialog(Gui.frame,
					"LPN File not found for merging.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		//return (g); 
		return (lpnWithPseudo);
	}

	private void addLastTransitionInfo(LhpnFile lpn, Double traceLength) {
		for (String st1 : lpn.getPlaceList()) {
			if (lpn.getPostset(st1).length == 0){ // a place without a postset transition
				if (getPlaceInfoIndex(st1) != null)
					placeInfo.get(getPlaceInfoIndex(st1)).setProperty("lastTransitionDelay", String.valueOf((int) (traceLength*delayScaleFactor)));
				else if (getTransientNetPlaceIndex(st1) != null)
					transientNetPlaces.get(getTransientNetPlaceIndex(st1)).setProperty("lastTransitionDelay", String.valueOf((int) (traceLength*delayScaleFactor)));
			}
		}
	}

	public ArrayList modeBinsCalculate(HashMap<String, ArrayList<String>> useMap, int i)
	{
		ArrayList currentBinArray = new ArrayList();
		ArrayList<ArrayList> modeBinArray = new ArrayList<ArrayList>();
		Set<String> useMapKeySet = useMap.keySet();
		int index  =0;
		int bin=0;
		for (int j=0; j<reqdVarsL.size(); j++){ 
			Variable v = reqdVarsL.get(j);
			String vName = v.getName();
			
			for(int k=0; k<useMapKeySet.size(); k++){
				int length =0;
				if(useMapKeySet.contains(vName)){
					
				}
				else{
					bin =bins[j][i];
					currentBinArray.add(index, bin);
					//System.out.println("bin final ^^^^^^^^ : "+bin);

					index++;
				}
			}
		}
		return currentBinArray;
	}


	public ArrayList varsBinsCalculate(HashMap<String, ArrayList<String>> useMap, int i)
	{
		ArrayList currentBinArray = new ArrayList();
		ArrayList<ArrayList> varsBinArray = new ArrayList<ArrayList>();
		Set<String> useMapKeySet = useMap.keySet();
		int index  =0;
		int bin=0;
		for (int j=0; j<reqdVarsL.size(); j++){ 
			Variable v = reqdVarsL.get(j);
			String vName = v.getName();
			if(!useMapKeySet.contains(vName)){
				
			}
			else{
				
				bin =bins[j][i];
				currentBinArray.add(index, bin);
				index++;
			}
			
		}
		return currentBinArray;
	}



	public ArrayList returnBin(ArrayList<Object> allInfo, int i ){

		ArrayList binInfo2 = (ArrayList) allInfo.get(i);
		ArrayList varsBinAtEnd2 = (ArrayList)binInfo2.get(1);
		return varsBinAtEnd2;
	}

	public int returnTime(ArrayList allInfo, int i ){

		ArrayList binInfo2 = (ArrayList) allInfo.get(i);
		int time = (Integer) binInfo2.get(0);
		return time;
	}

	public double returnDuration(ArrayList allInfo, int i ){

		ArrayList binInfo2 = (ArrayList) allInfo.get(i);
		double duration = (Double) binInfo2.get(2);
		return duration;
	}



	public void addStablesToData2(int[][]bins, Double[] duration, HashMap<String, ArrayList<Double>> localThresholds, HashMap<String, ArrayList<String>> useMap, ArrayList<Variable> reqdVarsL){

		
		ArrayList<Integer> modeBinAtStart;
		ArrayList<Integer> modeBinAtEnd;
		ArrayList<Integer> varsBinCurrent;
		ArrayList<Integer> varsBinAtEnd;
		int start=0;
		int end =0;
		int startMode=0;
		int endMode =0;
		Set allVarsBins = new HashSet();

		
		ArrayList<Object> allInfo = new ArrayList<Object>();
		ArrayList<Double> dataForStable = new ArrayList<Double>();
		//int index=0;
		int count1=0;
		ArrayList compareBin = new ArrayList();
		
		while (end<(data.get(0).size()-2) && endMode<(data.get(0).size()-2))
		{// System.out.println("end Mode is : "+endMode);
			modeBinAtStart = modeBinsCalculate(useMap,startMode);
			//System.out.println("modeBinAtStart%%%%%%% : "+modeBinAtStart+" at time : "+startMode);
			modeBinAtEnd = modeBinsCalculate(useMap, endMode+1);
			
			while((modeBinAtStart.equals(modeBinAtEnd))&& endMode<(data.get(0).size()-2)){ 
				endMode++; end++;
				varsBinCurrent=varsBinsCalculate(useMap,start);

				varsBinAtEnd=varsBinsCalculate(useMap,end);


				if((!varsBinCurrent.equals(varsBinAtEnd)) && (duration[end]!=null)){ 
					ArrayList<Object> binInfo = new ArrayList<Object>();
					binInfo.add(new Integer(end));
					binInfo.add(varsBinAtEnd);
					binInfo.add(new Double(duration[end]));
					allVarsBins.add(varsBinAtEnd);
					allInfo.add(count1, binInfo);
					//allInfo.add(binInfo);
					count1++;
					//System.out.println("allInfo : "+allInfo);
					start=end;
				}
				//startMode=endMode;
				modeBinAtStart = modeBinsCalculate(useMap,startMode);
				
				modeBinAtEnd = modeBinsCalculate(useMap, endMode+1);
				
			}

			//}
			int stable = startMode;
			
			Iterator itr = allVarsBins.iterator();
			while (itr.hasNext()){//System.out.println("compareBin : "+itr.next());
				compareBin=(ArrayList)itr.next();

				

				for(int i=(allInfo.size()-1);i>=0; --i){
					//System.out.println("compareBin : "+compareBin);
					ArrayList varsBinAtEnd2 = returnBin(allInfo, i);
					
					if(compareBin.equals(varsBinAtEnd2)){
						for(int m=i-1; m>=0;m--){
							//int time;
							int time= returnTime(allInfo,m+1);
							//System.out.println("time : "+time);
							if (time<stable)
								break;
							ArrayList bin2 = returnBin(allInfo, m);
							
							if (bin2.equals(varsBinAtEnd2)){
								double duration1= returnDuration(allInfo,i);
								//System.out.println("duration1 : "+duration1);
								double duration2= returnDuration(allInfo,m);
								//System.out.println("duration2 : "+duration2);
								if(Math.abs(duration1-duration2)/duration1>stableTolerance){ //0.02 default
									stable = time;
									//System.out.println("stable changed here to : "+stable);

								}
							}
						}
						break;  // need to chk
					}
				}
			}
			for(int i=startMode; i<=endMode; i++){
				if(i<stable){
					dataForStable.add(0.0);
				}
				else {
					dataForStable.add(1.0);
				}
				//dataForStable.add((data.get(0).size()-1), 1.0);
			}
			//System.out.println("startMode = " + startMode + " end = " + end + " endMode = " + endMode);	
			//System.out.println("data for stables: "+dataForStable.size());	

			startMode=endMode+1;
			endMode=startMode;
		}
		dataForStable.add((data.get(0).size()-1), 1.0);
		//System.out.println("data for stables: "+dataForStable.size());	
		int dataSize= data.size();
		data.add(dataSize, dataForStable);	
		//System.out.println("size of stable array is " + dataForStable.size());
		String s= "stable";
		varNames.add(s);

		
		Variable vStable = new Variable("stable");
		vStable.setCare(true); 
		vStable.setDmvc(true);
		vStable.forceDmvc(true);
		vStable.setEpsilon(0.1); // since stable is always 0 or 1 and threshold is 0.5. even epsilon of 0.3 is fine
		vStable.setInput(true);
		vStable.setOutput(false);
		//			varsWithStables.add(vStable);
		reqdVarsL.add(vStable);
		//	findReqdVarIndices();
		ArrayList<Double> tStable = new ArrayList<Double>();
		tStable.add(0.5);
		thresholds.put(s, tStable);

		
	}
	
	public void addStablesToData(HashMap<String, ArrayList<Double>> localThresholds, HashMap<String, ArrayList<String>> useMap){
		boolean sameBin = true;
		ArrayList<String> destabIps;
		HashMap<String, Integer> dataIndices;
		int point;
		for (String s : useMap.keySet()){
		//	destabIps = new ArrayList<String>();
		//	destabIps.add("ctl");
			destabIps = useMap.get(s);
			dataIndices = new HashMap<String, Integer>();
			for (int i = 0; i < destabIps.size(); i++) {
				String currentVar = destabIps.get(i);//reqdVarsL.get(i).getName();
				for (int j = 1; j < varNames.size(); j++) {
					if (currentVar.equalsIgnoreCase(varNames.get(j))) {
						dataIndices.put(currentVar,Integer.valueOf(j));
					}
				}
			}
			//int[] oldBin = new int[destabIps.size()];
			//int[] newBin = new int[destabIps.size()];
			int[] oldBin = new int[dataIndices.size()];
			int[] newBin = new int[dataIndices.size()];
			//double unstableTime = 5960.0;
			ArrayList<Double> dataForStable = new ArrayList<Double>();
			point = 0;
			dataForStable.add(0.0);	//Assume that always it starts unstable
			for (int j = 0; j < destabIps.size(); j++){
				String d = destabIps.get(j);
				if (dataIndices.containsKey(d))
					oldBin[j] = getRegion(data.get(dataIndices.get(d)).get(point),localThresholds.get(d));
			}
			for (int i = point+1; i < data.get(0).size(); i++){
				sameBin = true;
				for (int j = 0; j < destabIps.size(); j++){// check if all the responsible destabilizing variables are in the same bin at i as they were at point
					String d = destabIps.get(j);
					if (dataIndices.containsKey(d)){
						newBin[j] = getRegion(data.get(dataIndices.get(d)).get(i),localThresholds.get(d));
						if (oldBin[j] != newBin[j]){
							sameBin = false;
							break;
						}
					}
				}
				if ((sameBin) && (dataIndices.size() != 0)){
					if ((data.get(0).get(i) - data.get(0).get(point)) >= unstableTime){
						//dataForStable.set(i, 1.0);
						dataForStable.add(1.0);
					} else {
						//dataForStable.set(i, 0.0);
						dataForStable.add(0.0);
					}
				} else {
					//dataForStable.set(i, 0.0);
					dataForStable.add(0.0);
					for (int j = 0; j < destabIps.size(); j++){
						String d = destabIps.get(j);
						if (dataIndices.containsKey(d))
							oldBin[j] = getRegion(data.get(dataIndices.get(d)).get(point),localThresholds.get(d));
					}
					point = i;
				}
			}
			if (dataIndices.size() != 0){
				data.add(dataForStable);
				varNames.add("stable_" + s);
			}
		}
	}

	public Double getDelayScaleFactor(){
		return delayScaleFactor;
	}
	
	public Double getValueScaleFactor(){
		return valScaleFactor;
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


	public void resetAll(){
		// dmvcCnt = 0;
		numPlaces = 0;
		numTransitions = 0;
		delayScaleFactor = 1.0;
		valScaleFactor = 1.0;
		for (Variable v: reqdVarsL){
			v.reset();
		}
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
	
	public int getCareIndex(int reqdVarsIndex){
		int count = -1;
		for (int j = 0; j < reqdVarsL.size(); j++){ 
			if (reqdVarsL.get(j).isCare()){
				count++;
			}
			if (j == reqdVarsIndex)
				return count;
		}
		return -1;
	}
	
	public void addInitPlace(HashMap<String,ArrayList<Double>> scaledThresholds){
		//Properties initPlace = new Properties();
		//placeInfo.put("initMarked", p0);
		//initPlace.setProperty("placeNum", numPlaces.toString());
		//initPlace.setProperty("type", "INIT");
		//initPlace.setProperty("initiallyMarked", "true");
		int initPlaceNum = numPlaces;
		g.addPlace("p" + numPlaces, true);
		//propPlaces.add("p" + numPlaces);
		numPlaces++;
		try{
			try{
				out.write("Transient places are : ");
				for (String st : transientNetPlaces.keySet()){
					out.write(st + ";");
				}
				out.write("\n");
				for (String st : transientNetPlaces.keySet()){
					//Properties p1 = new Properties();
					//p1.setProperty("transitionNum", numTransitions.toString());
					//initTransitions.put(key, p1); //from initPlaceNum to key
					g.addTransition("t" + numTransitions); // prevTranKey+key);
					g.addMovement("p" + initPlaceNum, "t" + numTransitions);
					g.addMovement("t" + numTransitions, "p" + transientNetPlaces.get(st).getProperty("placeNum"));
					g.changeInitialMarking("p" + transientNetPlaces.get(st).getProperty("placeNum"), false);
					out.write("Added transition t" + numTransitions + " b/w initPlace and transient place p" + transientNetPlaces.get(st).getProperty("placeNum") + "\n");
					String[] binOutgoing = st.split(",");
					String condStr = "";
					for (int j = 0; j < reqdVarsL.size(); j++){ // preserving the order by having reqdVarsL as outer loop rather than care
						String st2 = reqdVarsL.get(j).getName();
						//	if (reqdVarsL.get(j).isCare()){
							if (reqdVarsL.get(j).isInput()){
								if (reqdVarsL.get(j).isCare()) {
									int bin = Integer.valueOf(binOutgoing[getCareIndex(j)]);
									if (bin == 0){
										if (!condStr.equalsIgnoreCase(""))
											condStr += "&";
										condStr += "~(" + st2 + ">=" + (int) Math.floor(scaledThresholds.get(st2).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
									} else if (bin == (scaledThresholds.get(st2).size())){
										if (!condStr.equalsIgnoreCase(""))
											condStr += "&";
										condStr += "(" + st2 + ">="	+ (int) Math.floor(scaledThresholds.get(st2).get(bin-1).doubleValue()) + ")";
									} else{
										if (!condStr.equalsIgnoreCase(""))
											condStr += "&";
										condStr += "(" + st2 + ">=" + (int) Math.floor(scaledThresholds.get(st2).get(bin-1).doubleValue()) + ")&~(" + st2 + ">=" + (int) Math.floor(scaledThresholds.get(st2).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
									}
								}
							} else {
								if (reqdVarsL.get(j).isDmvc()){
									int minv = (int) Math.floor(Double.parseDouble(transientNetPlaces.get(st).getProperty(st2 + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(transientNetPlaces.get(st).getProperty(st2 + "_vMax"))); 
									if (minv != maxv)
										g.addIntAssign("t" + numTransitions,st2,"uniform(" + minv  + ","+ maxv + ")");
									else
										g.addIntAssign("t" + numTransitions,st2,String.valueOf(minv));
									out.write("Added assignment to " + st2 + " at transition t" + numTransitions + "\n");
								}
								// deal with rates for continuous here
							}
					//	} else {
					//		out.write(st2 + " was don't care " + "\n");
					//	}
					}
					out.write("Changed enabling of t" + numTransitions + " to " + condStr + "\n");
					g.addEnabling("t" + numTransitions, condStr);
					numTransitions++;
				}
				for (String st : startPlaces){ //No transients case
					//Properties p1 = new Properties();
					//p1.setProperty("transitionNum", numTransitions.toString());
					//initTransitions.put(key, p1); //from initPlaceNum to key
					g.addTransition("t" + numTransitions); // prevTranKey+key);
					g.addMovement("p" + initPlaceNum, "t" + numTransitions);
					g.addMovement("t" + numTransitions, "p" + placeInfo.get(st).getProperty("placeNum"));
					g.changeInitialMarking("p" + placeInfo.get(st).getProperty("placeNum"), false);
					out.write("Added transition t" + numTransitions + " b/w initPlace and transient place p" + placeInfo.get(st).getProperty("placeNum") + "\n");
					String[] binOutgoing = st.split(",");
					String condStr = "";
					for (int j = 0; j < reqdVarsL.size(); j++){ // preserving the order by having reqdVarsL as outer loop rather than care
						String st2 = reqdVarsL.get(j).getName();
					//	if (reqdVarsL.get(j).isCare()){
							if (reqdVarsL.get(j).isInput()){
								int bin = Integer.valueOf(binOutgoing[getCareIndex(j)]);
								if (bin == 0){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "~(" + st2 + ">=" + (int) Math.floor(scaledThresholds.get(st2).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
								} else if (bin == (scaledThresholds.get(st2).size())){
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st2 + ">="	+ (int) Math.floor(scaledThresholds.get(st2).get(bin-1).doubleValue()) + ")";
								} else{
									if (!condStr.equalsIgnoreCase(""))
										condStr += "&";
									condStr += "(" + st2 + ">=" + (int) Math.floor(scaledThresholds.get(st2).get(bin-1).doubleValue()) + ")&~(" + st2 + ">=" + (int) Math.floor(scaledThresholds.get(st2).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
								}
							} else {
								if (reqdVarsL.get(j).isDmvc()){
									if (placeInfo.get(st).getProperty(st2 + "_vMin")!=null){
									int minv = (int) Math.floor(Double.parseDouble(placeInfo.get(st).getProperty(st2 + "_vMin")));
									int maxv = (int) Math.ceil(Double.parseDouble(placeInfo.get(st).getProperty(st2 + "_vMax"))); 
									if (minv != maxv)
										g.addIntAssign("t" + numTransitions,st2,"uniform(" + minv  + ","+ maxv + ")");
									else
										g.addIntAssign("t" + numTransitions,st2,String.valueOf(minv));
									out.write("Added assignment to " + st2 + " at transition t" + numTransitions + "\n");
								}}
								// deal with rates for continuous here
							}
					//	} else {
						///	out.write(st2 + " was don't care " + "\n");
						//}
					}
					out.write("Changed enabling of t" + numTransitions + " to " + condStr + "\n");
					g.addEnabling("t" + numTransitions, condStr);
					numTransitions++;
				}
				for (HashMap<String,String> st1 : constVal){	// for output signals that are constant throughout the trace.
					if (st1.size() != 0){
						g.addTransition("t" + numTransitions); // prevTranKey+key);
						g.addMovement("p" + initPlaceNum, "t" + numTransitions);
						for (String st2: st1.keySet()){
							g.addIntAssign("t" + numTransitions,st2,st1.get(st2));
						}
						numTransitions++;
					}
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(Gui.frame,
						"Null exception in addInitPlace.",
						"ERROR!", JOptionPane.ERROR_MESSAGE);
				out.close();
			}
		}catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened in addInitPlace.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void genBinsRates(HashMap<String, ArrayList<Double>> localThresholds) { 
//			TSDParser tsd = new TSDParser(directory + separator + datFile, biosim,false);
			// genBins data = tsd.getData();
		try{
			reqdVarIndices = new ArrayList<Integer>();
			//careBins = new HashMap<String, int[]>();//new int[reqdVarsL.size()][data.get(0).size()];
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
							ArrayList<Double> thresh = localThresholds.get(currentVar);
							bins[i][k] = getRegion(data.get(j).get(k),thresh);
							if ((k != 0) && (bins[i][k] != bins[i][k-1])){
								int length = 0;
								for (int m = k; m < data.get(j).size(); m++) {
									if (getRegion(data.get(j).get(m),thresh) == bins[i][k])
										length++;
									else 
										break;
								}
								if (length < pathLengthVar){
									out.write("Short bin for variable " + currentVar + " at " + data.get(0).get(k) + " until " + data.get(0).get(k+length-1) + " due to min pathLengthVar. Using " + bins[i][k-1] + " instead of " + bins[i][k] + " \n");
									for (int m = k; m < k+length; m++) {
										bins[i][m] = bins[i][k-1];
									}
								} else {
									for (int m = k; m < k+length; m++) {
										bins[i][m] = bins[i][k];
									}
								}
								k = k+length-1;
							}
						}
						if (binError)
							JOptionPane.showMessageDialog(Gui.frame,
									"Bin couldn't be retrieved for a point. Please check thresholds.",
									"ERROR!", JOptionPane.ERROR_MESSAGE);	

						// System.out.print(" ");
						break;
					} else {
								out.write("WARNING: A variable in reqdVarsL wasn't found in the complete list of names.\n");
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
			int mark, k, previous = 0, markFullPrev; // indices of rates not same as that of the variable. if
			// wanted, then size of rates array should be varNames
			// not reqdVars
			if (rateSampling == -1) { // replacing inf with -1 since int
				mark = 0;
				markFullPrev = 0;
				for (int i = 0; i < data.get(0).size(); i++) {
					if (i < mark) {
						continue;
					}
					markFullPrev = mark;
					//while ((mark < data.get(0).size()) && (compareBins(i, mark))) {
					while (mark < data.get(0).size()){
						if (compareBins(i, mark)){
						    if (compareFullBinInputs(i, mark) && !compareFullBins(i,mark)){ // new case to deal with non-care output change
								break;
							}
							for (int j = 0; j < reqdVarsL.size(); j++){
								k = reqdVarIndices.get(j);
								values[j][i] = (values[j][i]*(mark-i) + data.get(k).get(mark))/(mark-i+1);
							}
							mark++;
						} else 
							break;
						//Assume that only inputs can be don't care
						if (!compareFullBins(markFullPrev, mark-1)){ //mark-1 because of mark++ above
							markFullPrev = mark-1;
						}
					}
					if ((data.get(0).get(mark - 1) != data.get(0).get(i)) && ((mark - i) >=  pathLengthBin) && (mark != data.get(0).size())) { 	// && (mark != (data.get(0).size() - 1 condition added on nov 23.. to avoid the last region bcoz it's not complete. rechk
						if ((!compareBins(previous,i)) || (compareFullBinInputs(previous, i) && !compareFullBins(previous, i))){ // second case is non-care output change
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
							}
							//duration[i] = data.get(0).get(mark)	- data.get(0).get(i); // changed (mark - 1) to mark on may 28,2010
							duration[i] = data.get(0).get(mark)	- data.get(0).get(markFullPrev);
							previous = i;
							if (markFullPrev != i)
								out.write("Some don't care input change in b/w a bin. So referencing off " + markFullPrev + " instead of " + i + "\n");
						} else{	// There was a glitch and you returned to the same region
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][previous] = ((data.get(k).get(mark - 1) - data.get(k).get(previous)) / (data.get(0).get(mark - 1) - data.get(0).get(previous)));
							}
							if (mark < data.get(0).size()) //This if condition added to fix bug on sept 25 2010
								duration[previous] = data.get(0).get(mark)	- data.get(0).get(previous); // changed (mark - 1) to mark on may 28,2010
						}
					} else if ((mark - i) <  pathLengthBin)  { // account for the glitch duration //
						out.write("Short bin at " + data.get(0).get(i) + " until " + data.get(0).get(mark) + " due to min pathLengthBin. This delay being added to " + previous + " \n");
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
					} else if (mark == data.get(0).size()){ // long enough bin towards the end of trace.
						if ((!compareBins(previous,i)) || (compareFullBinInputs(previous, i) && !compareFullBins(previous, i))){ // second case is non-care output change
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][i] = ((data.get(k).get(mark - 1) - data.get(k).get(i)) / (data.get(0).get(mark - 1) - data.get(0).get(i)));
							}
							//duration[i] = data.get(0).get(mark)	- data.get(0).get(i); // changed (mark - 1) to mark on may 28,2010
							previous = i;
						} else{	
							// There was a glitch and you returned to the same region
							//Or signal is constant through out the trace
							for (int j = 0; j < reqdVarsL.size(); j++) {
								k = reqdVarIndices.get(j);
								rates[j][previous] = ((data.get(k).get(mark - 1) - data.get(k).get(previous)) / (data.get(0).get(mark - 1) - data.get(0).get(previous)));
							}
							//This duration shouldn't make any sense because there's no following transition
							duration[previous] = data.get(0).get(mark - 1)	- data.get(0).get(previous); // changed (mark - 1) to mark on may 28,2010
						}
						out.write("Bin at end of trace starts at " + previous + "\n");
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
			JOptionPane.showMessageDialog(Gui.frame,
					"Bins/Rates could not be generated. Please check thresholds.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);				
		} catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened for writing genBinsRates messages.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		
			/*try {
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

	public int getRegion(double value, ArrayList<Double> varThresholds){
		int bin = 0;
		try{
			for (int l = 0; l < varThresholds.size(); l++) {
				if (value <= varThresholds.get(l)) {
					bin = l;
					break;
				} else {
					bin = l + 1; 
				}
			}
		} catch (NullPointerException e){
			e.printStackTrace();
			binError = true;
		}
		return bin;
	}
	
	public boolean compareBins(int j, int mark) {
		/*for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;*/
		/*for (String st : careVars) { // this way only when order of cares not important. since this is a hashmap
			if (bins[findReqdVarslIndex(st)][j]!= bins[findReqdVarslIndex(st)][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;*/
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (reqdVarsL.get(i).isCare()){
				if (bins[i][j] != bins[i][mark]) {
					return false;
				} else {
					continue;
				}
			}
		}
		return true;
	}

	public boolean compareFullBinInputs(int j, int mark) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (reqdVarsL.get(i).isInput() && (bins[i][j] != bins[i][mark])) {
				return false;
			} else {
				continue;
			}
		}
		return true;
	}

	public boolean compareFullBins(int j, int mark) {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (bins[i][j] != bins[i][mark]) {
				return false;
			} else {
				continue;
			}
		}
		return true;
	}

	public void updateRateInfo(int[][] bins, Double[][] rates, int traceNum, Boolean transientPlaceReqd, Properties cvgProp) {
		String prevPlaceKey = "", prevPlaceFullKey = ""; // "" or " " ; rechk
		String key = "", fullKey = "";
		Double prevPlaceDuration = null;
		String transId = null;
		Boolean prevIpChange = false, prevOpChange = false, ipChange = false, opChange = false, careIpChange = false;
		// boolean addNewPlace;
		ArrayList<String> ratePlaces = new ArrayList<String>(); // ratePlaces can include non-input dmv places.
		// boolean newRate = false;
		//System.out.println("Duration " + duration);
		try{
			Properties p0, p1 = null;
			out.write("In UpdateRateInfo\n");
			for (int i = 0; i < (data.get(0).size() - 1); i++) {
				if (rates[0][i] != null) { // check if indices are ok. 0???? or 1???
					prevPlaceKey = key; prevPlaceFullKey = fullKey;
					key = ""; fullKey = "";
					//Full keys are for transitions; Just Keys are for Places
					for (int j = 0; j < reqdVarsL.size(); j++) {
						if (reqdVarsL.get(j).isCare()){
							if (key.equalsIgnoreCase(""))
								key += bins[j][i];
							else
								key += "," + bins[j][i];		
						}
						if (fullKey.equalsIgnoreCase(""))
							fullKey += bins[j][i];
						else
							fullKey += "," + bins[j][i];
					}
					out.write("Rates not null at " + i + "; key is " + key + "; full key is " + fullKey + ";\n");
					if (placeInfo.containsKey(key) && (!transientPlaceReqd | (ratePlaces.size() != 0))) {
						p0 = placeInfo.get(key);
						out.write("Came back to existing place p" + p0.getProperty("placeNum") + " at time " + data.get(0).get(i) + " bins " + key + "\n");
						if (traceNum > 1)
							ratePlaces.add("p" + p0.getProperty("placeNum"));
					} 
					else if (transientPlaceReqd && ((transientNetPlaces.containsKey(key)) && (((ratePlaces.size() == 1) && (traceNum == 1)) || ((ratePlaces.size() == 0) && (traceNum != 1))))){ // same transient in new trace => ratePlaces.size() < 1; same transient in same trace => ratePlaces.size() = 1
						p0 = transientNetPlaces.get(key);
						out.write("Came back to existing transient place p" + p0.getProperty("placeNum") + " at time " + data.get(0).get(i) + " bins " + key + "\n");
						if (ratePlaces.size() == 0){ // new trace
							ratePlaces.add("p" + p0.getProperty("placeNum"));
						}
					} else {
						p0 = new Properties();
						if (ratePlaces.size() == 0){
							if (!transientPlaceReqd) {
								placeInfo.put(key, p0);
								g.addPlace("p" + numPlaces, false);
								startPlaces.add(key);
							} else {
								transientNetPlaces.put(key, p0);
								g.addPlace("p" + numPlaces, true);
							}
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
						out.write("New place p" + numPlaces + " at time " + data.get(0).get(i) + " bins " + key + "\n");
						numPlaces++;
						cvgProp.setProperty("places", String.valueOf(Integer.parseInt(cvgProp.getProperty("places"))+1));
					}
					for (int j = 0; j < reqdVarsL.size(); j++) {
						// rechk if (reqdVarsL.get(j).isDmvc() && reqdVarsL.get(j).isInput()) {
						// continue;
						// }
						if (reqdVarsL.get(j).isDmvc()) { // && !reqdVarsL.get(j).isInput()){

							if(dmvcValuesUnique.get(reqdVarsL.get(j).getName()).getProperty(String.valueOf(bins[j][i]))!=null){// System.out.println("Here it is : "+dmvcValuesUnique.get(reqdVarsL.get(j).getName()).getProperty(String.valueOf(bins[j][i])));
							//System.out.println("DMVC " + dmvcValuesUnique);

							//System.out.println("DMVC " + dmvcValuesUnique);

							//System.out.println(reqdVarsL.get(j).getName() + " " + bins[j][i] + " "  + dmvcValuesUnique.get(reqdVarsL.get(j).getName()).getProperty(String.valueOf(bins[j][i]))"\n");
							out.write("Add value : " + reqdVarsL.get(j).getName() + " -> corresponding to bin " + bins[j][i] + " at place p" + p0.getProperty("placeNum") + "\n");
							out.write("Add value : " + reqdVarsL.get(j).getName() + " -> " + Double.valueOf(dmvcValuesUnique.get(reqdVarsL.get(j).getName()).getProperty(String.valueOf(bins[j][i]))) + " at place p" + p0.getProperty("placeNum") + "\n");
							addValue(p0,reqdVarsL.get(j).getName(),Double.valueOf(dmvcValuesUnique.get(reqdVarsL.get(j).getName()).getProperty(String.valueOf(bins[j][i]))));
							//out.write("Add value : " + reqdVarsL.get(j).getName() + " -> " + dmvcValuesUnique.get(reqdVarsL.get(j).getName()).get(bins[j][i]) + " at place p" + p0.getProperty("placeNum") + "\n");
							continue;
						}}
						addRate(p0, reqdVarsL.get(j).getName(), rates[j][i]);
					}
//					boolean transientNet = false;
					if (!prevPlaceFullKey.equalsIgnoreCase(fullKey)) {
						if (!prevPlaceFullKey.equals("")){
							ArrayList<Integer> diffL = diff(prevPlaceFullKey,fullKey);
							opChange = false; 
							ipChange = false;
							careIpChange = false;
							for (int k : diffL) {
								if (reqdVarsL.get(k).isInput()) {
									ipChange = true;
									if (reqdVarsL.get(k).isCare())
										careIpChange = true;
								} else {
									opChange = true;
								}
							}
						} else {
							ipChange = false;
							opChange = false;
							careIpChange = false;
						}
						opChange=true;
						if ((traceNum > 1) && transientNetTransitions.containsKey(prevPlaceFullKey + "," + fullKey) && (ratePlaces.size() == 2)) { // same transient transition as that from some previous trace
							p1 = transientNetTransitions.get(prevPlaceFullKey + "," + fullKey);
							transId = prevPlaceFullKey + "," + fullKey;
							out.write("Came back to existing transient transition t" + p1.getProperty("transitionNum") + " at time " + data.get(0).get(i) + " " + prevPlaceFullKey + " -> " + fullKey);
						} else if (transitionInfo.containsKey(prevPlaceFullKey + "," + fullKey) /*&& (ratePlaces.size() > 2)*/) { // instead of tuple
							p1 = transitionInfo.get(prevPlaceFullKey + "," + fullKey);
							transId = prevPlaceFullKey + "," + fullKey;
							out.write("Came back to existing transition t" + p1.getProperty("transitionNum") + " at time " + data.get(0).get(i) + " " + prevPlaceFullKey + " -> " + fullKey);
						} else if (!prevPlaceFullKey.equalsIgnoreCase("")) {
							// transition = new Transition(reqdVarsL.size(),place,prevPlace);
							p1 = new Properties();
							p1.setProperty("transitionNum", numTransitions.toString());
							if (transientPlaceReqd && (ratePlaces.size() <= 2)){ // changed == to <= for the sake of non-care outputs
								transientNetTransitions.put(prevPlaceFullKey + "," + fullKey, p1);
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + transientNetPlaces.get(prevPlaceKey).getProperty("placeNum"), "t" + transientNetTransitions.get(prevPlaceFullKey + "," + fullKey).getProperty("transitionNum")); 
								if (ratePlaces.size() == 2)
									g.addMovement("t" + transientNetTransitions.get(prevPlaceFullKey + "," + fullKey).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								else if (ratePlaces.size() == 1)
									g.addMovement("t" + transientNetTransitions.get(prevPlaceFullKey + "," + fullKey).getProperty("transitionNum"), "p" + transientNetPlaces.get(key).getProperty("placeNum"));
//								transientNet = true;
								transId = prevPlaceFullKey + "," + fullKey;
								out.write("New transition t" + numTransitions + " at time " + data.get(0).get(i) + " " + prevPlaceFullKey + " -> " + fullKey);
								numTransitions++;
								cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
							} else {
								if (prevIpChange & !prevOpChange & opChange & !ipChange){
									out.write("Should delete the transition" + transId+ " from ");
									if (transitionInfo.containsKey(transId)){
										out.write("transitionInfo\n");
										int removeTrans = Integer.valueOf(transitionInfo.get(transId).getProperty("transitionNum"));
										String lastLastPlace = g.getTransition("t" + removeTrans).getPreset()[0].getName();
										//String lastLastPlaceKey = getPlaceInfoIndex(lastLastPlace);
										String lastLastPlaceFullKey = getPresetPlaceFullKey(transId);
										if (transitionInfo.containsKey(lastLastPlaceFullKey + "," + fullKey)){
											p1 = transitionInfo.get(lastLastPlaceFullKey + "," + fullKey);
											p1.put("ioChangeDelay", "yes");
											int removeTransNum =  Integer.valueOf(transitionInfo.get(lastLastPlaceFullKey + "," +  prevPlaceFullKey).getProperty("transitionNum"));
											if (removeTransNum == numTransitions -1){
												out.write("\n\n\nFound an output change follwing a previous input change; remove transition t" + removeTransNum + " between " + lastLastPlace + " and p" + placeInfo.get(prevPlaceKey).getProperty("placeNum") + "; merged transition already exists in the form of t" + p1.getProperty("transitionNum") + " b/w " + lastLastPlace + " and p" + placeInfo.get(key).getProperty("placeNum") + "\n");
												transitionInfo.remove(lastLastPlaceFullKey + "," + prevPlaceFullKey);
												//g.removeTransition("t" + (numTransitions -1));
												g.removeTransition("t" + removeTransNum);
											}
											transId = lastLastPlaceFullKey + "," + fullKey;
										} else {
											transitionInfo.put(lastLastPlaceFullKey + "," + fullKey, p1);
											p1.put("ioChangeDelay", "yes");
											int removeTransNum =  Integer.valueOf(transitionInfo.get(lastLastPlaceFullKey + "," +  prevPlaceFullKey).getProperty("transitionNum"));
											g.addTransition("t" + numTransitions); // prevTranKey+key);
											g.addMovement(lastLastPlace, "t" + transitionInfo.get(lastLastPlaceFullKey + "," + fullKey).getProperty("transitionNum")); 
											g.addMovement("t" + transitionInfo.get(lastLastPlaceFullKey + "," + fullKey).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
											if (removeTransNum == numTransitions -1){
												out.write("\n\n\nFound an output change follwing a previous input change; remove transition t" + removeTransNum + " between " + lastLastPlace + " and " + placeInfo.get(prevPlaceKey).getProperty("placeNum") + "; add transition t" + numTransitions + " b/w " + lastLastPlace + " and " + placeInfo.get(key).getProperty("placeNum") + "\n");
												transitionInfo.remove(lastLastPlaceFullKey + "," + prevPlaceFullKey);
												//g.removeTransition("t" + (numTransitions -1));
												g.removeTransition("t" + removeTransNum);
											}
											transId = lastLastPlaceFullKey + "," + fullKey;
											out.write("New transition t" + numTransitions + " at time " + data.get(0).get(i) + " " + prevPlaceFullKey + " -> " + fullKey);
											numTransitions++;
											cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
										}
								} else if (transientPlaceReqd && transientNetTransitions.containsKey(transId)){
										out.write("transientNetTransitions\n");
										int removeTrans = Integer.valueOf(transientNetTransitions.get(transId).getProperty("transitionNum"));
										String lastLastPlace = g.getTransition("t" + removeTrans).getPreset()[0].getName();
										//String lastLastPlaceKey = getTransientNetPlaceIndex(lastLastPlace);
										String lastLastPlaceFullKey = getPresetPlaceFullKey(transId);
										if (transientNetTransitions.containsKey(lastLastPlaceFullKey + "," + fullKey)){
											p1 = transientNetTransitions.get(lastLastPlaceFullKey + "," + fullKey);
											p1.put("ioChangeDelay", "yes");
											int removeTransNum =  Integer.valueOf(transientNetTransitions.get(lastLastPlaceFullKey + "," + prevPlaceFullKey).getProperty("transitionNum"));
											if (removeTransNum == numTransitions -1){
												out.write("\n\n\nTRANSIENT:Found an output change followed by a previous input change; remove transition t" + removeTransNum + " between " + lastLastPlace + " and " + placeInfo.get(prevPlaceKey).getProperty("placeNum") + "; merged transition already exists in the form of t" + p1.getProperty("transitionNum") + " b/w " + lastLastPlace + " and " + placeInfo.get(key).getProperty("placeNum") + "\n");
												transientNetTransitions.remove(lastLastPlaceFullKey + "," + prevPlaceFullKey);
												//g.removeTransition("t" + (numTransitions -1));
												g.removeTransition("t" + removeTransNum);
											}
											transId = lastLastPlaceFullKey + "," + fullKey;
										} else {
											transientNetTransitions.put(lastLastPlaceFullKey + "," + fullKey, p1);
											p1.put("ioChangeDelay", "yes");
											int removeTransNum =  Integer.valueOf(transientNetTransitions.get(lastLastPlaceFullKey + "," + prevPlaceFullKey).getProperty("transitionNum"));
											g.addTransition("t" + numTransitions); // prevTranKey+key);
											g.addMovement(lastLastPlace, "t" + transientNetTransitions.get(lastLastPlaceFullKey + "," + fullKey).getProperty("transitionNum")); 
											g.addMovement("t" + transientNetTransitions.get(lastLastPlaceFullKey + "," + fullKey).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
											if (removeTransNum == numTransitions -1){
												out.write("\n\n\nTRANSIENT:Found an output change followed by a previous input change; remove transition t" + removeTransNum + " between " + lastLastPlace + " and " + placeInfo.get(prevPlaceKey).getProperty("placeNum") + "; add transition t" + numTransitions + " b/w " + lastLastPlace + " and " + placeInfo.get(key).getProperty("placeNum") + "\n");
												transientNetTransitions.remove(lastLastPlaceFullKey + "," + prevPlaceFullKey);
												//g.removeTransition("t" + (numTransitions -1));
												g.removeTransition("t" + removeTransNum);
											}
											transId = lastLastPlaceFullKey + "," + fullKey;
											out.write("TRANSIENT:New transition t" + numTransitions + " at time " + data.get(0).get(i) + " " + prevPlaceFullKey + " -> " + fullKey);
											numTransitions++;
											cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
										}
									} else {
										out.write("transition " + transId+ " not present to delete\n");
										System.out.println("transition " + transId+ " not present to delete\n");
									}
								} else {
									transitionInfo.put(prevPlaceFullKey + "," + fullKey, p1);
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(prevPlaceKey).getProperty("placeNum"), "t" + transitionInfo.get(prevPlaceFullKey + "," + fullKey).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(prevPlaceFullKey + "," + fullKey).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
									transId = prevPlaceFullKey + "," + fullKey;
									out.write("New transition t" + numTransitions + " at time " + data.get(0).get(i) + " " + prevPlaceFullKey + " -> " + fullKey);
									numTransitions++;
									cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
								}
							}
							// transition.setCore(true);
						}
						prevIpChange = ipChange;
						prevOpChange = opChange;
						//else if (prevPlaceKey == "") {
						//	p1 = new Properties();
						//	p1.setProperty("transitionNum", numTransitions.toString());
						//	initTransitions.put(key, p1); //from initPlaceNum to key
						//	g.addTransition("t" + numTransitions); // prevTranKey+key);
						//	g.addMovement("p" + transientNetPlaces.get(prevPlaceKey).getProperty("placeNum"), "t" + transientNetTransitions.get(prevPlaceKey + key).getProperty("transitionNum")); 
						//	g.addMovement("t" + transientNetTransitions.get(prevPlaceKey + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
						//	numTransitions++;
						//}
						if (prevPlaceDuration != null){ //Delay on a transition is the duration spent at its preceding place
							/*if (!(ipChange & opChange)){
								addDuration(p1, prevPlaceDuration);
								out.write(" Added duration "  + prevPlaceDuration + " to transition t" + p1.getProperty("transitionNum") + "\n");
							}
							else{
								addDuration(p1, 0.0);
								out.write(" Added duration 0 to transition t" + p1.getProperty("transitionNum") + "\n");
							}*/
							if (!opChange){ //Commented above and changed to this on Aug 5,2010. When there's no o/p bin change, then thrs no need for delay.
								addDuration(p1, 0.0);
								out.write(" Added duration 0 to transition t" + p1.getProperty("transitionNum") + "\n");
							} else if (careIpChange){ // Both o/p and a care i/p changed on the same transition. So, they changed at the same time.
								addDuration(p1, 0.0);
								out.write(" Added duration 0 to transition t" + p1.getProperty("transitionNum") + "\n");
							} else { //o/p change alone or (o/p change and don't care i/p change)
								addDuration(p1, prevPlaceDuration);
								out.write(" Added duration "  + prevPlaceDuration + " to transition t" + p1.getProperty("transitionNum") + "\n");
							}
						} else {
							out.write(" Not adding duration here. CHECK\n");
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
		}
		catch (IOException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened for writing UpdateRateInfo messages.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
	/*	}catch (NullPointerException e4) {
			e4.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Null exception during model generation in updateRate",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}*/
	}catch (NullPointerException e4) {
		e4.printStackTrace();
		JOptionPane.showMessageDialog(Gui.frame,
				"The number of thresholds generated do not match with the dmvc values.    Use 'Auto' option.",
				"ERROR!", JOptionPane.ERROR_MESSAGE);
	}
	}

	public String getPresetPlaceFullKey(String transition){
		//out.write("preset key for " + transition + " is ");
		String fullKey = "";
		if (transition != null){
			String[] b = transition.split(",");
			for (int j = 0; j < reqdVarsL.size(); j++) {
				if (fullKey.equalsIgnoreCase(""))
					fullKey += b[j];
				else
					fullKey += "," + b[j];
			}
			//out.write(fullKey + "\n");
			return(fullKey);
		} else 
			return null;
	}
	
	public String getPostsetPlaceFullKey(String transition){
		String fullKey = "";
		if (transition != null){
			String[] b = transition.split(",");
			for (int j = 0; j < reqdVarsL.size(); j++) {
				if (fullKey.equalsIgnoreCase(""))
					fullKey += b[j + reqdVarsL.size()];
				else
					fullKey += "," + b[j + reqdVarsL.size()];
			}
			return(fullKey);
		} else 
			return null;
	}
	
	public void updateGraph(int[][] bins, Double[][] rates, int traceNum, Boolean transientPlaceReqd, Properties cvgProp) {
		updateRateInfo(bins, rates, traceNum, transientPlaceReqd, cvgProp);
		//updateTimeInfo(bins,cvgProp);
		int initMark = -1;
		int k;
		String key;
		constVal.add(new HashMap<String, String>());
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
			if (initMark == -1){//constant throughout the trace?
				initMark = 0;
				k = reqdVarIndices.get(i);
				constVal.get(constVal.size() - 1).put(reqdVarsL.get(i).getName(),data.get(k).get(0).toString());
				reqdVarsL.get(i).addInitValues(data.get(k).get(0));
			}
			key = "" + bins[0][initMark];
			for (int l = 1; l < reqdVarsL.size(); l++) {
//				key = key + "" + bins[l][initMark];
				key = key + "," + bins[l][initMark];
			}
			if (!reqdVarsL.get(i).isDmvc()){
//				System.out.println(key);
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
		//dmvcValuesUnique = new HashMap<String, ArrayList<Double>>();
		try{
			for (int i = 0; i < reqdVarsL.size(); i++) {
				if (reqdVarsL.get(i).isForcedCont()){
					reqdVarsL.get(i).setDmvc(false);
					out.write(reqdVarsL.get(i).getName() + " is forced to be continuous \n");
				} else {
					absTime = 0;
					mark = 0;
					DMVCrun runs = reqdVarsL.get(i).getRuns();
					runs.clearAll(); // flush all the runs from previous dat file.
					int lastRunPointsWithoutTransition = 0;
					Double lastRunTimeWithoutTransition = 0.0;
					Double lastRunValueWithoutTransition = null;
					out.write("Epsilon for " + reqdVarsL.get(i).getName() + " is " + reqdVarsL.get(i).getEpsilon());
					if (!callFromAutogen) { // This flag is required because if the call is from autogenT, then data has just the reqdVarsL but otherwise, it has all other vars too. So reqdVarIndices not reqd when called from autogen
						for (int j = 0; j <= data.get(reqdVarIndices.get(i)).size(); j++) { // changed from data.get(0).size() to data.get(reqdVarIndices.get(i)).size() on july 31,2010.
							if (j < mark) // not reqd??
								continue;
							if (((j+1) < data.get(reqdVarIndices.get(i)).size()) && 
									Math.abs(data.get(reqdVarIndices.get(i)).get(j) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= reqdVarsL.get(i).getEpsilon()) {
								startPoint = j;
								runs.addValue(data.get(reqdVarIndices.get(i)).get(j)); // chk carefully reqdVarIndices.get(i)
								while (((j + 1) < data.get(0).size()) && (bins[i][startPoint] == bins[i][j+1]) && (Math.abs(data.get(reqdVarIndices.get(i)).get(startPoint) - data.get(reqdVarIndices.get(i)).get(j + 1)) <= reqdVarsL.get(i).getEpsilon())) {       //checking of same bins[] condition added on May 11,2010.
									runs.addValue(data.get(reqdVarIndices.get(i)).get(j + 1)); // chk carefully
									// reqdVarIndices.get(i)
									j++;
								}
								endPoint = j;
								if (!absoluteTime) {
									if ((endPoint < (data.get(0).size() - 1)) && ((endPoint - startPoint) + 1) >= runLength) {
										runs.addStartPoint(startPoint);
										runs.addEndPoint(endPoint);
									} else if (((endPoint - startPoint) + 1) >= runLength) {
										lastRunPointsWithoutTransition = endPoint - startPoint + 1;
										lastRunValueWithoutTransition = runs.getLastValue();
									} else {
										runs.removeValue();
									}
								} else {
									if ((endPoint < (data.get(0).size() - 1)) && (calcDelay(startPoint, endPoint) >= runTime)) {
										runs.addStartPoint(startPoint);
										runs.addEndPoint(endPoint);
										absTime += calcDelay(startPoint, endPoint);
									} else if (calcDelay(startPoint, endPoint) >= runTime) {
										lastRunTimeWithoutTransition = calcDelay(startPoint, endPoint);
										lastRunValueWithoutTransition = runs.getLastValue();
									} else {
										runs.removeValue();
									}
								}
								mark = endPoint;
							}
						}
						numPoints = runs.getNumPoints();
						if (!reqdVarsL.get(i).isForcedDmv() && ((absoluteTime && (((absTime + lastRunTimeWithoutTransition)/ (data.get(0).get(data.get(0).size() - 1) - data
								.get(0).get(0))) < percent)) || (!absoluteTime && (((numPoints + lastRunPointsWithoutTransition)/ (double) data.get(0).size()) < percent)))){ // isForced condition added on may 28
							runs.clearAll();
							reqdVarsL.get(i).setDmvc(false);
							out.write(reqdVarsL.get(i).getName() + " is not a dmvc \n");
						} else {
							reqdVarsL.get(i).setDmvc(true);
							Double[] dmvcValues;
							if (lastRunValueWithoutTransition != null ){
								Double[] dVals = reqdVarsL.get(i).getRuns().getAvgVals();
								dmvcValues = new Double[dVals.length + 1];
								for (int j = 0; j < dVals.length; j++){
									dmvcValues[j] = dVals[j];
								}
								dmvcValues[dVals.length] = lastRunValueWithoutTransition;
							} else
								dmvcValues = reqdVarsL.get(i).getRuns().getAvgVals();
							Arrays.sort(dmvcValues);
							//System.out.println("Sorted DMV values of " + reqdVarsL.get(i).getName() + " are ");
							//for (Double l : dmvcValues){
							//	System.out.print(l + " ");
							//}
							//System.out.println();
							if (!dmvcValuesUnique.containsKey(reqdVarsL.get(i).getName()))
								dmvcValuesUnique.put(reqdVarsL.get(i).getName(),new Properties());
							out.write("DMV values of " + reqdVarsL.get(i).getName() + " are : ");
							if (dmvcValues.length == 0){// constant value throughout the trace
								dmvcValues = new Double[1];
								dmvcValues[0]= reqdVarsL.get(i).getRuns().getConstVal();
							}
							for (int j = 0; j < dmvcValues.length; j++){
								if (dmvcValues[j] < thresholds.get(reqdVarsL.get(i).getName()).get(0)){
									dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put("0",dmvcValues[j].toString());
									//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin 0 is " + dmvcValues[j] + "\n");
									out.write("bin 0 -> " + dmvcValues[j] + "; ");
								}
								else if (dmvcValues[j] >= thresholds.get(reqdVarsL.get(i).getName()).get(thresholds.get(reqdVarsL.get(i).getName()).size() - 1)){
									dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(thresholds.get(reqdVarsL.get(i).getName()).size()),dmvcValues[j].toString());
									//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin " + thresholds.get(reqdVarsL.get(i).getName()).size() + " is " + dmvcValues[j] + "\n");
									out.write("bin " + thresholds.get(reqdVarsL.get(i).getName()).size() + " -> " + dmvcValues[j] + "; ");
								} else {
									for (int k = 0; k < thresholds.get(reqdVarsL.get(i).getName()).size()-1; k++){
										if ((dmvcValues[j] >= thresholds.get(reqdVarsL.get(i).getName()).get(k)) && (dmvcValues[j] < thresholds.get(reqdVarsL.get(i).getName()).get(k+1))){
											dmvcValuesUnique.get(reqdVarsL.get(i).getName()).put(String.valueOf(k+1),dmvcValues[j].toString());
											//System.out.println("For variable " + reqdVarsL.get(i).getName() + " value for bin " + String.valueOf(k+1) + " is " + dmvcValues[j] + "\n");
											out.write("bin " + (k+1) + " -> " + dmvcValues[j] + "; ");
											break;
										}
									}
								}
								//out.write(dmvcValues[j] + " ");
								//following not needed for calls from data2lhpn
								//	dmvSplits.add((dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 1) + dmvcValuesUnique.get(reqdVarsL.get(i).getName()).get(dmvcValuesUnique.get(reqdVarsL.get(i).getName()).size() - 2))/2);
								//}
								//for (int k = j+1; k < dmvcValues.length; k++){
								//	if (Math.abs((dmvcValues[j] - dmvcValues[k])) > epsilon){
								//		j = k-1;
								//		break;
								//	}
								//	else if (k >= (dmvcValues.length -1)){
								//		j = k;
								//	}
								//}	
							}
							//dmvDivisions.put(reqdVarsL.get(i).getName(), dmvSplits);
							out.write("\n" + reqdVarsL.get(i).getName() + " is  a dmvc \n");
						}
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
			//System.out.println("LPN file couldn't be created/written ");
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened for writing rates and bins.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		dmvDetectDone = true;
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

	public void addRate(Properties p, String name, Double r) { 
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

	public void deleteInvalidDmvcTime(Properties p, Double t) {
		String[] times = null;
		String name = p.getProperty("DMVCVariable");
		String s = p.getProperty("dmvcTime_" + name);
		String newS = null;
		Double dMin = null, dMax = null;
		if (s != null) {
			times = s.split(" ");
			for (int i = 0; i < times.length; i++) {
				if (Double.parseDouble(times[i]) != t) {
					if (newS == null){
						newS = times[i];
						dMin = Double.parseDouble(times[i]);
						dMax = Double.parseDouble(times[i]);
					}
					else{
						newS += " " + times[i];
						dMin = (Double.parseDouble(times[i]) < dMin) ? Double.parseDouble(times[i]) : dMin;
						dMax = (Double.parseDouble(times[i]) > dMax) ? Double.parseDouble(times[i]) : dMax;
					}
				}
			}
			p.setProperty("dmvcTime_" + name, newS);
		}
		if (dMin != null){
			p.setProperty("dMin", dMin.toString());
			p.setProperty("dMax", dMax.toString());
		}
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
					if ((maxDivision != null) && (Math.abs((int) (maxDivision * scaleFactor)) > Integer.MAX_VALUE)) {
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
				out.write("\nForcing delay and value scale factors by " + delayScaleFactor + " and " + valScaleFactor + " respectively \n");
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
			//System.out.println("LPN file couldn't be created/written ");
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be opened for writing rates and bins.",
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
								if(p.getProperty(v.getName()+ "_vMin")!=null){
								p.setProperty(v.getName() + "_vMin", Double.toString(Double.parseDouble(p.getProperty(v.getName()+ "_vMin"))* scaleFactor));
								p.setProperty(v.getName() + "_vMax", Double.toString(Double.parseDouble(p.getProperty(v.getName()+ "_vMax")) * scaleFactor));
							}}

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
				//System.out.println("After scaling init Val of " + v.getName() + " by factor " + scaleFactor + ", it is " + v.getInitValue() + "\n");
				for (int j = 0; j < localThresholds.get(v.getName()).size(); j++) {
					localThresholds.get(v.getName()).set(j,localThresholds.get(v.getName()).get(j) * scaleFactor);
				}
			}
			for (HashMap<String,String> st1 : constVal){	// for output signals that are constant throughout the trace.
				for (String st2: st1.keySet()){
					st1.put(st2,String.valueOf(Double.valueOf(st1.get(st2))*scaleFactor));
				}
			}
		}
		catch (NullPointerException e){
			e.printStackTrace();
			//System.out.println("LPN file couldn't be created/written ");
			JOptionPane.showMessageDialog(Gui.frame,
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
							String tKey = getTransitionInfoIndex(t);
							if (tKey != null){
								String pPrev = getPresetPlaceFullKey(tKey);
								String nextPlace = getPostsetPlaceFullKey(tKey);
								if ((transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMin")!=null) &&
									(transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMax")!=null)) {
									Double mind = Double.parseDouble(transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMin"));
									Double maxd = Double.parseDouble(transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMax"));
									transitionInfo.get(pPrev + "," + nextPlace).setProperty("dMin",Double.toString(mind*scaleFactor));
									transitionInfo.get(pPrev + "," + nextPlace).setProperty("dMax",Double.toString(maxd*scaleFactor));
								}
							} else {
								System.out.println("Transition " + t + " has no index in transitionInfo. CHECK");
							}
						}
					} else {
						Properties postsetPlaceProp = null; // added for the sake of non-care outputs
						if (getTransientNetPlaceIndex(g.getPostset(t)[0]) != null) {
							postsetPlaceProp = transientNetPlaces.get(getTransientNetPlaceIndex(g.getPostset(t)[0]));
						} else {
							postsetPlaceProp = placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0]));
						}
						if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
								&& (postsetPlaceProp.getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
							String tKey = getTransientNetTransitionIndex(t);
							if (tKey != null){
								String pPrev = getPresetPlaceFullKey(tKey);
								String nextPlace = getPostsetPlaceFullKey(tKey);
								Double mind = Double.parseDouble(transientNetTransitions.get(pPrev+ "," +nextPlace).getProperty("dMin"));
								Double maxd = Double.parseDouble(transientNetTransitions.get(pPrev+ "," +nextPlace).getProperty("dMax"));
								transientNetTransitions.get(pPrev+ "," +nextPlace).setProperty("dMin",Double.toString(mind*scaleFactor));
								transientNetTransitions.get(pPrev+ "," +nextPlace).setProperty("dMax",Double.toString(maxd*scaleFactor));
							} else {
								System.out.println("Transient transition " + t + " has no index in transientNetTransitions. CHECK");
							}
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
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Delay scaling error due to null. Check",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Delay scaling error due to Array Index.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}

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
		try{
		for (String t : g.getTransitionList()) {
			if ((g.getPreset(t) != null) && (g.getPostset(t) != null)){
				if (!isTransientTransition(t)){
					if ((placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
						String tKey = getTransitionInfoIndex(t);
						if (tKey != null){
							String pPrev = getPresetPlaceFullKey(tKey);
							String nextPlace = getPostsetPlaceFullKey(tKey);
							out.write("Transition " + g.getTransition(t).getLabel() + " b/w " + pPrev + " and " + nextPlace + " : finding delay \n");
							if (transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMin") != null){
								mind = Double.parseDouble(transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMin"));
								if (minDelay == null)
									minDelay = mind;
								else if ((minDelay > mind) && (mind != 0))
									minDelay = mind;
							}
						} else {
							System.out.println("ERROR: Transition " + t + " has no index in transitionInfo.");
							out.write("ERROR: Transition " + t + " has no index in transitionInfo.");
						}
					}
				} else {
					Properties postsetPlaceProp = null; // added for the sake of non-care outputs
					if (getTransientNetPlaceIndex(g.getPostset(t)[0]) != null) {
						postsetPlaceProp = transientNetPlaces.get(getTransientNetPlaceIndex(g.getPostset(t)[0]));
					} else {
						postsetPlaceProp = placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0]));
					}
					if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (postsetPlaceProp.getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
						String tKey = getTransientNetTransitionIndex(t);
						if (tKey != null){
							String pPrev = getPresetPlaceFullKey(tKey);
							String nextPlace = getPostsetPlaceFullKey(tKey);
							if (transientNetTransitions.get(pPrev+ "," +nextPlace).getProperty("dMin") != null){
								mind = Double.parseDouble(transientNetTransitions.get(pPrev+ "," +nextPlace).getProperty("dMin"));
								if (minDelay == null)
									minDelay = mind;
								else if ((minDelay > mind) && (mind != 0))
									minDelay = mind;
							}
						} else {
							System.out.println("Transient transition " + t + " has no index in transientNetTransitions. CHECK");
							out.write("ERROR: Transient transition " + t + " has no index in transientNetTransitions.");
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
		} catch (IOException e){
			e.printStackTrace();
		}
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
						String tKey = getTransitionInfoIndex(t);
						if (tKey != null){
							String pPrev = getPresetPlaceFullKey(tKey);
							String nextPlace = getPostsetPlaceFullKey(tKey);
							if (transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMax") != null){
								maxd = Double.parseDouble(transitionInfo.get(pPrev + "," + nextPlace).getProperty("dMax"));
								if (maxDelay == null)
									maxDelay = maxd;
								else if ((maxDelay < maxd) && (maxd != 0))
									maxDelay = maxd;
							}
						} else {
							System.out.println("Transition " + t + " has no index in transitionInfo. CHECK");
						}
					}
				} else {
					Properties postsetPlaceProp = null; // added for the sake of non-care outputs
					if (getTransientNetPlaceIndex(g.getPostset(t)[0]) != null) {
						postsetPlaceProp = transientNetPlaces.get(getTransientNetPlaceIndex(g.getPostset(t)[0]));
					} else {
						postsetPlaceProp = placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0]));
					}
					if ((transientNetPlaces.get(getTransientNetPlaceIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (postsetPlaceProp.getProperty("type").equalsIgnoreCase("RATE"))){		// transient non-dmv transition
						String tKey = getTransientNetTransitionIndex(t);
						if (tKey != null){
							String pPrev = getPresetPlaceFullKey(tKey);
							String nextPlace = getPostsetPlaceFullKey(tKey);
							if (transientNetTransitions.get(pPrev+ "," +nextPlace).getProperty("dmax") != null){
								maxd = Double.parseDouble(transientNetTransitions.get(pPrev+ "," +nextPlace).getProperty("dmax"));
								if (maxDelay == null)
									maxDelay = maxd;
								else if ((maxDelay < maxd) && (maxd != 0))
									maxDelay = maxd;
							}
						} else {
							System.out.println("Transient transition " + t + " has no index in transientNetTransitions. CHECK");
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
		//System.out.println("Adding duration of " + d);
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
	
	public String getTransitionInfoIndex(String s) {
		String index = null;
		for (String st2 : transitionInfo.keySet()) {
			if (("t" + transitionInfo.get(st2).getProperty("transitionNum"))
					.equalsIgnoreCase(s)) {
				index = st2;
				break;
			}
		}
		return index;
	}
	
	public String getTransientNetTransitionIndex(String s) {
		String index = null;
		for (String st2 : transientNetTransitions.keySet()) {
			if (("t" + transientNetTransitions.get(st2).getProperty("transitionNum"))
					.equalsIgnoreCase(s)) {
				index = st2;
				break;
			}
		}
		return index;
	}
	
	public ArrayList<Integer> diff(String pre_bin, String post_bin) {
		//Parameters are bins formed from reqdVarsL (not just cares).
		ArrayList<Integer> diffL = new ArrayList<Integer>();
		if ((pre_bin != null) && (post_bin != null)){
			String[] preset_encoding = pre_bin.split(",");
			String[] postset_encoding = post_bin.split(",");
			for (int j = 0; j < preset_encoding.length; j++) { // to account for "" being created in the array
				if (Integer.parseInt(preset_encoding[j]) != Integer.parseInt(postset_encoding[j])) {
					diffL.add(j);// to account for "" being created in the array
				}
			}
		}
		return (diffL);
	}
	
	public Integer getMinRate(String place, String name) {
		Properties p = placeInfo.get(place);
		if (p.getProperty(name + "_rMin") != null)
			return ((int) Math.floor(Double.parseDouble(p.getProperty(name
					+ "_rMin"))));
		else 
			return null;
		// return(rMin[i]);
	}

	public Integer getMaxRate(String place, String name) {
		Properties p = placeInfo.get(place);
		if (p.getProperty(name + "_rMax") != null)
			return ((int) Math.floor(Double.parseDouble(p.getProperty(name
					+ "_rMax"))));
		else 
			return null;
		// return(rMin[i]);
	}
	
/*	public Double[][] getDataExtrema(ArrayList<ArrayList<Double>> data){
		Double[][] extrema = new Double[reqdVarsL.size()][2];
		for (int i=0; i<reqdVarsL.size(); i++){
			//Object obj = Collections.min(data.get(reqdVarIndices.get(i)));
			Object obj = Collections.min(data.get(i+1));
			extrema[i][0] = Double.parseDouble(obj.toString());
			//obj = Collections.max(data.get(reqdVarIndices.get(i)));
			obj = Collections.max(data.get(i+1));
			extrema[i][1] = Double.parseDouble(obj.toString());
		}
	    return extrema;
	}
	*/
	public HashMap <String, Double[]> getDataExtrema(ArrayList<ArrayList<Double>> data){
		HashMap <String, Double[]> extrema = new HashMap <String, Double[]>();
		for (int i=0; i<reqdVarsL.size(); i++){
			//Object obj = Collections.min(data.get(reqdVarIndices.get(i)));
			Object obj = Collections.min(data.get(i+1));
			extrema.put(reqdVarsL.get(i).getName(),new Double[2]);
			extrema.get(reqdVarsL.get(i).getName())[0] = Double.parseDouble(obj.toString());
			//obj = Collections.max(data.get(reqdVarIndices.get(i)));
			obj = Collections.max(data.get(i+1));
			extrema.get(reqdVarsL.get(i).getName())[1] = Double.parseDouble(obj.toString());
		}
	    return extrema;
	}
	
	public Double[] getMinMaxRates(Double[] rateList){
		ArrayList<Double> cmpL = new ArrayList<Double>();
		Double[] minMax = {null,null};// new Double[2];
		for (Double r : rateList){
			if (r != null){
				cmpL.add(r);
			}
		}
		if (cmpL.size() > 0){
			Object obj = Collections.min(cmpL);
			minMax[0] = Double.parseDouble(obj.toString());
			obj = Collections.max(cmpL);
			minMax[1] = Double.parseDouble(obj.toString());
		}
		return minMax;
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
	
	public void addPseudo(HashMap<String,ArrayList<Double>> scaledThresholds){
		try{
			pseudoVars = new HashMap<String,Boolean>();
			//pseudoVars.put("ctl",true);
			for (int i = 0; i < reqdVarsL.size(); i++) {
				if ((reqdVarsL.get(i).isInput()) && (reqdVarsL.get(i).isCare())){
					pseudoVars.put(reqdVarsL.get(i).getName(),true);
				} 
			}
			if ((pseudoVars != null) && (pseudoVars.size() != 0)){
				for (String st : g.getPlaceList()){
					currentPlace = st;
					//TODO: do this only if not prop type place
					if (getPlaceInfoIndex(st) != null) {
						currPlaceBin = getPlaceInfoIndex(st).split(",");
						traverse(scaledThresholds);
					}
					else if (getTransientNetPlaceIndex(st) != null) {
						//currPlaceBin = getTransientNetPlaceIndex(st).split(",");
						//traverse(scaledThresholds);
					} else 
						out.write("WARNING: " + st + " is not in the placelist. It should be initPlace, otherwise something wrong\n");
					//			visitedPlaces = new HashMap<String,Boolean>();
					//			visitedPlaces.put(currentPlace, true);
					
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void traverse(HashMap<String,ArrayList<Double>> scaledThresholds){
		for (String nextPlace : g.getPlaceList()){
			if ((!nextPlace.equalsIgnoreCase(currentPlace)) && (getPlaceInfoIndex(nextPlace) != null)){
				if ((getPlaceInfoIndex(currentPlace) != null) && (!transitionInfo.containsKey(getPlaceInfoIndex(currentPlace) + "," + getPlaceInfoIndex(nextPlace)))){
					addPseudoTrans(nextPlace, scaledThresholds);
				} else if ((getTransientNetPlaceIndex(currentPlace) != null) && (!transientNetTransitions.containsKey(getTransientNetPlaceIndex(currentPlace) + "," + getPlaceInfoIndex(nextPlace)))){
					addPseudoTrans(nextPlace, scaledThresholds);
				}
			}
		}
	}
	
	private void addPseudoTrans(String nextPlace, HashMap<String, ArrayList<Double>> scaledThresholds){ // Adds pseudo transition b/w currentPlace and nextPlace
		String[] nextPlaceBin = getPlaceInfoIndex(nextPlace).split(",");
		String enabling = "";
		int bin;
		String st; 
		Boolean pseudo = false;
		int binIndex = -1;
		try{
			for (int i = 0; i < reqdVarsL.size(); i++) {
				if ((reqdVarsL.get(i).isInput()) && (reqdVarsL.get(i).isCare())){
					binIndex++;
					if (Integer.valueOf(currPlaceBin[binIndex]) == Integer.valueOf(nextPlaceBin[binIndex])){
						continue;
					} else {
						if (!pseudoVars.containsKey(reqdVarsL.get(i).getName())){
						// If the 2 places differ in the bins of a non-pseudovar, then u can't add pseudotrans there coz other variables belong to diff bins in these 2 places
							pseudo = false;
							break;
						}
						//if (Math.abs(Integer.valueOf(currPlaceBin[binIndex]) - Integer.valueOf(nextPlaceBin[binIndex])) > 1){
						// If the 2 places differ in the bins of a pseudovar but are not adjacent, then u can't add pseudotrans there
						//	pseudo = false;
						//	break;
						//}
						pseudo = true;
						bin = Integer.valueOf(nextPlaceBin[binIndex]);
						st = reqdVarsL.get(i).getName();
						if (bin == 0){
							if (!enabling.equalsIgnoreCase(""))
								enabling += "&";
							enabling += "~(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
						} else if (bin == (scaledThresholds.get(st).size())){
							if (!enabling.equalsIgnoreCase(""))
								enabling += "&";
							enabling += "(" + st + ">="	+ (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")";
						} else{
							if (!enabling.equalsIgnoreCase(""))
								enabling += "&";
							enabling += "(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin-1).doubleValue()) + ")&~(" + st + ">=" + (int) Math.floor(scaledThresholds.get(st).get(bin).doubleValue()) + ")";//changed ceil to floor on aug 7,2010
						}
					}
				} else if ((!reqdVarsL.get(i).isInput()) && (reqdVarsL.get(i).isCare())){
					binIndex++;
					if ( (int) Integer.valueOf(currPlaceBin[binIndex]) != (int) Integer.valueOf(nextPlaceBin[binIndex])){
						pseudo = false;
						break;
					}
				}
			}
			if (pseudo){
				out.write("Adding pseudo-transition pt"  + pseudoTransNum + " between " + currentPlace + " and " + nextPlace + " with enabling " + enabling + "\n");
				lpnWithPseudo.addTransition("pt" + pseudoTransNum);
				lpnWithPseudo.addMovement(currentPlace, "pt" + pseudoTransNum); 
				lpnWithPseudo.addMovement("pt" + pseudoTransNum, nextPlace);
				lpnWithPseudo.addEnabling("pt" + pseudoTransNum, enabling);
				pseudoTransNum++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Log file couldn't be written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void addMetaBins(){ // TODO: DIDN'T REPLACE divisionsL by thresholds IN THIS METHOD
		boolean foundBin = false;
		for (String st1 : g.getPlaceList()){
			String p = getPlaceInfoIndex(st1);
			String[] binEncoding ;
			ArrayList<Integer> syncBinEncoding;
			String o1;
			// st1 w.r.t g
			// p w.r.t placeInfo
			if (placeInfo.get(p).getProperty("type").equalsIgnoreCase("RATE")) {
//				String [] bE = p.split("");
				String [] bE = p.split(",");
				binEncoding = new String[bE.length - 1];
				for (int i = 0; i < bE.length; i++){
					binEncoding[i] = bE[i];    // since p.split("") gives ,0,1 if p was 01
				}
				for (int i = 0; i < binEncoding.length ; i++){
					if (!reqdVarsL.get(i).isDmvc()){
					if ((lowerLimit[i] != null) && (getMinRate(p,reqdVarsL.get(i).getName()) < 0)){
						syncBinEncoding = new ArrayList<Integer>();
						// deep copy of bin encoding
						for (int n = 0; n < binEncoding.length; n++){
							o1 = binEncoding[n];
							if (o1 == "A"){
								syncBinEncoding.add(-1);
							}
							else if (o1 == "Z"){
								syncBinEncoding.add(divisionsL.get(n).size());
							}
							else{
								syncBinEncoding.add( Integer.parseInt(o1)); // clone() not working here
							}
						}
						foundBin = false;
						while (!foundBin){
							syncBinEncoding.set(i,syncBinEncoding.get(i) - 1);
							String key = "";
							for (int m = 0; m < syncBinEncoding.size(); m++){
								if (syncBinEncoding.get(m) != -1){
									key += syncBinEncoding.get(m).toString();
								}
								else{
									key += "A"; // ***Encoding -1 as A
								}
							}
							if ((syncBinEncoding.get(i) == -1) && (!placeInfo.containsKey(key))){ 
								foundBin = true;
								Properties p0 = new Properties();
								placeInfo.put(key, p0);
								p0.setProperty("placeNum", numPlaces.toString());
								p0.setProperty("type", "RATE");
								p0.setProperty("initiallyMarked", "false");
								p0.setProperty("metaType","true");
								p0.setProperty("metaVar", String.valueOf(i));
								g.addPlace("p" + numPlaces, false);
								//ratePlaces.add("p"+numPlaces);
								numPlaces++;
								if (getMaxRate(p,reqdVarsL.get(i).getName()) > 0){ // minrate is 0; maxrate remains the same if positive
									addRate(p0, reqdVarsL.get(i).getName(), 0.0);
									addRate(p0, reqdVarsL.get(i).getName(), (double)getMaxRate(p,reqdVarsL.get(i).getName()));
							/*
							 * This transition should be added only in this case but
							 * since dotty cribs if there's no place on a transition's postset,
							 * moving this one down so that this transition is created even if the 
							 * min,max rate is 0 in which case it wouldn't make sense to have this
							 * transition
							 * 		Properties p2 = new Properties();
									transitionInfo.put(key + p, p2);
									p2.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addControlFlow("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")" + "&~fail");
									numTransitions++; */
								}
								else{
									addRate(p0, reqdVarsL.get(i).getName(), 0.0); // if the maximum rate was negative, then make the min & max rates both as zero
								}
								Properties p1 = new Properties();
								transitionInfo.put(p + "," + key, p1);
								p1.setProperty("transitionNum", numTransitions.toString());
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + "," + key).getProperty("transitionNum")); 
								g.addMovement("t" + transitionInfo.get(p + "," + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
							//	g.addEnabling("t" + numTransitions, "~fail");
								g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")");
								int minr = getMinRate(key, reqdVarsL.get(i).getName());
								int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
								if (minr != maxr)
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr + "," + maxr + ")");
								else
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
								numTransitions++;
								Properties p2 = new Properties();
								transitionInfo.put(key + "," + p, p2);
								p2.setProperty("transitionNum", numTransitions.toString());
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + "," + p).getProperty("transitionNum")); 
								g.addMovement("t" + transitionInfo.get(key + "," + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
								g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")");
								minr = getMinRate(p, reqdVarsL.get(i).getName());
								maxr = getMaxRate(p, reqdVarsL.get(i).getName());
								if (minr != maxr)
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
								else
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
								numTransitions++;
							}
							else if (placeInfo.containsKey(key)){
								foundBin = true;
								//Properties syncP = placeInfo.get(key);
								Properties p1;
								if (!transitionInfo.containsKey(p + "," + key)) { // instead of tuple
									p1 = new Properties();
									transitionInfo.put(p + "," + key, p1);
									p1.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + "," + key).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(p + "," + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								//	g.addEnabling("t" + numTransitions, "~fail");
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")");
									int minr = getMinRate(key, reqdVarsL.get(i).getName());
									int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									numTransitions++;
								}
							}
						}
					}
					if ((upperLimit[i] != null) && getMaxRate(p,reqdVarsL.get(i).getName()) > 0){
						syncBinEncoding = new ArrayList<Integer>();
						// deep copy of bin encoding
						for (int n = 0; n < binEncoding.length; n++){
							o1 = binEncoding[n];
							if (o1 == "A"){
								syncBinEncoding.add(-1);
							}
							else if (o1 == "Z"){
								syncBinEncoding.add(divisionsL.get(n).size()+1);
							}
							else{
								syncBinEncoding.add( Integer.parseInt(o1)); // clone() not working here
							}
						}
						foundBin = false;
						while (!foundBin){
							syncBinEncoding.set(i,syncBinEncoding.get(i) + 1);
							String key = "";
							for (int m = 0; m < syncBinEncoding.size(); m++){
								if (syncBinEncoding.get(m) < divisionsL.get(i).size()+1){
									key += syncBinEncoding.get(m).toString();
								}
								else{
									key += "Z"; // ***Encoding highest bin +1 as Z
									// encoding not required here.. but may be useful to distinguish the pseudobins from normal bins in future
								}
							}
							if ((syncBinEncoding.get(i) == divisionsL.get(i).size() + 1) && (!placeInfo.containsKey(key))){ 
								// divisionsL.get(i).size() + 1 or  divisionsL.get(i).size()???
								foundBin = true;
								Properties p0 = new Properties();
								placeInfo.put(key, p0);
								p0.setProperty("placeNum", numPlaces.toString());
								p0.setProperty("type", "RATE");
								p0.setProperty("initiallyMarked", "false");
								p0.setProperty("metaType","true");
								p0.setProperty("metaVar", String.valueOf(i));
								//ratePlaces.add("p"+numPlaces);
								g.addPlace("p" + numPlaces, false);
								numPlaces++;
								if (getMinRate(p,reqdVarsL.get(i).getName()) < 0){ // maxrate is 0; minrate remains the same if negative
									addRate(p0, reqdVarsL.get(i).getName(), 0.0);
									addRate(p0, reqdVarsL.get(i).getName(), (double)getMinRate(p,reqdVarsL.get(i).getName()));
									/*
									 * This transition should be added only in this case but
									 * since dotty cribs if there's no place on a transition's postset,
									 * moving this one down so that this transition is created even if the 
									 * min,max rate is 0 in which case it wouldn't make sense to have this
									 * transition
									 * 				
									Properties p2 = new Properties();
									transitionInfo.put(key + p, p2);
									p2.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addControlFlow("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + p).getProperty("transitionNum")); 
									g.addControlFlow("t" + transitionInfo.get(key + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")" + "&~fail");
									numTransitions++; 
									*/
								}
								else{
									addRate(p0, reqdVarsL.get(i).getName(), 0.0); // if the minimum rate was positive, then make the min & max rates both as zero
								}
								
								Properties p1 = new Properties();
								transitionInfo.put(p + "," + key, p1);
								p1.setProperty("transitionNum", numTransitions.toString());
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + "," + key).getProperty("transitionNum")); 
								g.addMovement("t" + transitionInfo.get(p + "," + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
							//	g.addEnabling("t" + numTransitions, "~fail");
								int minr = getMinRate(key, reqdVarsL.get(i).getName());
								int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
								if (minr != maxr)
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
								else
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
								g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")");
								numTransitions++;
								Properties p2 = new Properties();
								transitionInfo.put(key + "," + p, p2);
								p2.setProperty("transitionNum", numTransitions.toString());
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + placeInfo.get(key).getProperty("placeNum"), "t" + transitionInfo.get(key + "," + p).getProperty("transitionNum")); 
								g.addMovement("t" + transitionInfo.get(key + "," + p).getProperty("transitionNum"), "p" + placeInfo.get(p).getProperty("placeNum"));
								g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")");
								minr = getMinRate(p, reqdVarsL.get(i).getName());
								maxr = getMaxRate(p, reqdVarsL.get(i).getName());
								if (minr != maxr)
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
								else
									g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
								numTransitions++; 
							}
							else if (placeInfo.containsKey(key)){
								foundBin = true;
								//Properties syncP = placeInfo.get(key);
								Properties p1;
								if (!transitionInfo.containsKey(p + "," + key)) { // instead of tuple
									p1 = new Properties();
									transitionInfo.put(p + "," + key, p1);
									p1.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + "," + key).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(p + "," + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
									//	g.addEnabling("t" + numTransitions, "~fail");
									int minr = getMinRate(key, reqdVarsL.get(i).getName());
									int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")");
									numTransitions++;
								}
							}
						}
					}
					}
				}
			}
		}
	}
	public void addMetaBinTransitions(){ 
		// Adds transitions b/w existing metaBins. 
		// Doesn't add any new metabins. Doesn't add transitions b/w metabins & normal bins
		boolean foundBin = false;
		for (String st1 : g.getPlaceList()){
			String p = getPlaceInfoIndex(st1);
			Properties placeP = placeInfo.get(p);
			String[] binEncoding ;
			ArrayList<Integer> syncBinEncoding;
			String o1;
			// st1 w.r.t g
			// p w.r.t placeInfo
			if (placeP.getProperty("type").equalsIgnoreCase("RATE") && placeP.getProperty("metaType").equalsIgnoreCase("true")) {
//				String [] bE = p.split("");
				String [] bE = p.split(",");
				binEncoding = new String[bE.length - 1];
				for (int i = 0; i < bE.length; i++){
					binEncoding[i] = bE[i];    // since p.split("") gives ,0,1 if p was 01
				}
				for (int i = 0; i < binEncoding.length ; i++){
					if ((!reqdVarsL.get(i).isDmvc()) && (Integer.parseInt(placeP.getProperty("metaVar")) != i)){
					if ((getMinRate(p,reqdVarsL.get(i).getName()) < 0)){
						syncBinEncoding = new ArrayList<Integer>();
						// deep copy of bin encoding
						for (int n = 0; n < binEncoding.length; n++){
							o1 = binEncoding[n];
							if (o1 == "A"){
								syncBinEncoding.add(-1);
							}
							else if (o1 == "Z"){
								syncBinEncoding.add(divisionsL.get(n).size());
							}
							else{
								syncBinEncoding.add( Integer.parseInt(o1)); // clone() not working here
							}
						}
						foundBin = false;
						while (!foundBin){
							syncBinEncoding.set(i,syncBinEncoding.get(i) - 1);
							String key = "";
							for (int m = 0; m < syncBinEncoding.size(); m++){
								if (syncBinEncoding.get(m) != -1){
									key += syncBinEncoding.get(m).toString();
								}
								else{
									key += "A"; // ***Encoding -1 as A
								}
							}
							if (placeInfo.containsKey(key)){
								foundBin = true;
								//Properties syncP = placeInfo.get(key);
								Properties p1;
								if (!transitionInfo.containsKey(p + "," + key)) { // instead of tuple
									p1 = new Properties();
									transitionInfo.put(p + "," + key, p1);
									p1.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + "," + key).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(p + "," + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								//	g.addEnabling("t" + numTransitions, "~fail");
									g.addEnabling("t" + numTransitions, "~(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.floor(lowerLimit[i]) + ")");
									int minr = getMinRate(key, reqdVarsL.get(i).getName());
									int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									numTransitions++;
								}
							}
						}
					}
					if (getMaxRate(p,reqdVarsL.get(i).getName()) > 0){
						syncBinEncoding = new ArrayList<Integer>();
						// deep copy of bin encoding
						for (int n = 0; n < binEncoding.length; n++){
							o1 = binEncoding[n];
							if (o1 == "A"){
								syncBinEncoding.add(-1);
							}
							else if (o1 == "Z"){
								syncBinEncoding.add(divisionsL.get(n).size()+1);
							}
							else{
								syncBinEncoding.add( Integer.parseInt(o1)); // clone() not working here
							}
						}
						foundBin = false;
						while (!foundBin){
							syncBinEncoding.set(i,syncBinEncoding.get(i) + 1);
							String key = "";
							for (int m = 0; m < syncBinEncoding.size(); m++){
								if (syncBinEncoding.get(m) < divisionsL.get(i).size()+1){
									key += syncBinEncoding.get(m).toString();
								}
								else{
									key += "Z"; // ***Encoding highest bin +1 as Z
									// encoding not required here.. but may be useful to distinguish the pseudobins from normal bins in future
								}
							}
							if (placeInfo.containsKey(key)){
								foundBin = true;
								//Properties syncP = placeInfo.get(key);
								Properties p1;
								if (!transitionInfo.containsKey(p + "," + key)) { // instead of tuple
									p1 = new Properties();
									transitionInfo.put(p + "," + key, p1);
									p1.setProperty("transitionNum", numTransitions.toString());
									g.addTransition("t" + numTransitions); // prevTranKey+key);
									g.addMovement("p" + placeInfo.get(p).getProperty("placeNum"), "t" + transitionInfo.get(p + "," + key).getProperty("transitionNum")); 
									g.addMovement("t" + transitionInfo.get(p + "," + key).getProperty("transitionNum"), "p" + placeInfo.get(key).getProperty("placeNum"));
								//	g.addEnabling("t" + numTransitions, "~fail");
									int minr = getMinRate(key, reqdVarsL.get(i).getName());
									int maxr = getMaxRate(key, reqdVarsL.get(i).getName());
									if (minr != maxr)
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), "uniform("	+ minr	+ "," + maxr + ")");
									else
										g.addRateAssign("t" + numTransitions, reqdVarsL.get(i).getName(), String.valueOf(minr));
									g.addEnabling("t" + numTransitions, "(" + reqdVarsL.get(i).getName() + ">=" + (int) Math.ceil(upperLimit[i]) + ")");
									numTransitions++;
								}
							}
						}
					}
					}
				}
			}
		}
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
			JOptionPane.showMessageDialog(Gui.frame,
					"VHDL-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(Gui.frame,
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
			JOptionPane.showMessageDialog(Gui.frame,
					"Verilog-AMS model couldn't be created/written.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(Gui.frame,
					"Error in Verilog-AMS model generation.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	//T[] aux = (T[])a.clone();
	
	public LhpnFile mergeLhpns(LhpnFile l1,LhpnFile l2){//(LhpnFile l1, LhpnFile l2){
		l1.save(directory + separator + "l1.lpn"); //since there's no deep copy method
		l2.save(directory + separator + "l2.lpn");
		String place1 = "p([-\\d]+)", place2 = "P([-\\d]+)";
		String transition1 = "t([-\\d]+)", transition2 = "T([-\\d]+)";
		String pt1 = "pt([-\\d]+)", pt2 = "pt([-\\d]+)";
		int placeNum, transitionNum, ptNum;
		int minPlace=0, maxPlace=0, minTransition = 0, maxTransition = 0, minPT=0, maxPT=0;
		Boolean first = true;
		LhpnFile l3 = new LhpnFile();
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
			if ((st1.matches(pt1)) || (st1.matches(pt2))){
					st1 = st1.replaceAll("pt", "");
					st1 = st1.replaceAll("PT", "");
					ptNum = Integer.valueOf(st1);
					if (ptNum > maxPT){
						maxPT = ptNum;
						if (first){
							first = false;
							minPT = ptNum;
						}
					}
					if (ptNum < minPT){
						minPT = ptNum;
						if (first){
							first = false;
							maxPT = ptNum;
						}
					}
				}
			}
			for (String st1: l2.getTransitionList()){
				if ((st1.matches(pt1)) || (st1.matches(pt2))){
					st1 = st1.replaceAll("pt", "");
					st1 = st1.replaceAll("PT", "");
					ptNum = Integer.valueOf(st1);
					if (ptNum > maxPT)
						maxPT = ptNum;
					if (ptNum < minPT)
						minPT = ptNum;
				}
			}
			//System.out.println("min transition and max transition in both lpns are : " + minTransition + "," + maxTransition);
			for (String st2: l2.getTransitionList()){
				for (String st1: l1.getTransitionList()){
					if ((st1.equalsIgnoreCase(st2)) && ((st1.matches(pt1)) || (st1.matches(pt2))) && ((st2.matches(pt1)) || (st2.matches(pt2)))){
						maxPT++;
						l2.renameTransition(st2, "pt" + maxPT);
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
			l3 = new LhpnFile();
			l3.load(directory + separator + "l1.lpn");
			l3.load(directory + separator + "tmp.lpn");
			l2 = new LhpnFile();
			l2.load(directory + separator + "l2.lpn");
			File l1File = new File(directory + separator + "l1.lpn");
			File l2File = new File(directory + separator + "l2.lpn");
			l1File.delete(); l2File.delete();
			//l2.save(directory + separator + "tmp.lpn");
			//l1.load(directory + separator + "tmp.lpn");
			File tmp = new File(directory + separator + "tmp.lpn");
			tmp.delete();
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(Gui.frame,
					"Problem while merging lpns",
					"ERROR!", JOptionPane.ERROR_MESSAGE);
		}
		//return l1;
		return l3;
	}
}

/* 
	private String traceBack(String place, String var){
		String enabling = null;
		try{
			visitedPlaces.put(place,true);
			for (String presetTrans : g.getPreset(place)){
				ExprTree enableTree = g.getEnablingTree(presetTrans);
				if ((enableTree != null) && (enableTree.containsVar(var))){
					enabling = enableTree.toString();
					return enabling;
				}
			}
			for (String presetTrans : g.getPreset(place)){
				for (String presetPlace : g.getPreset(presetTrans)){
					if (!visitedPlaces.containsKey(presetPlace)){
						enabling = traceBack(presetPlace,var);
						if (enabling != null)
							return enabling;
					}
				}
			}
		} catch (NullPointerException e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(BioSim.frame,
					"Exception while tracing back for making the enabling conditions mutually exclusive.",
					"ERROR!", JOptionPane.ERROR_MESSAGE);

		}
		return enabling;
	}
		
This method is used for creating separate nets for each DMV input variable driver.
public void updateTimeInfo(int[][] bins, Properties cvgProp) {
	String prevPlace = null;
	String currPlace = null;
	Properties p3 = null;
	//ArrayList<String> dmvcPlaceL = new ArrayList<String>(); // only dmvc inputs
	boolean exists;
	// int dmvcCnt = 0; making this global .. rechk
	String[] places;
	try {
		for (int i = 0; i < reqdVarsL.size(); i++) {
			if (reqdVarsL.get(i).isDmvc() && reqdVarsL.get(i).isInput()) {
				out.write(reqdVarsL.get(i).getName() + " is a dmvc input variable \n");
				// dmvcCnt = 0; in case of multiple tsd files, this may be a problem. may create a new distinct place with an existing key.??
				prevPlace = null;
				currPlace = null;
				p3 = null;
				Properties p2 = null;
				String k;
				DMVCrun runs = reqdVarsL.get(i).getRuns();
				Double[] avgVals = runs.getAvgVals();
				out.write("variable " + reqdVarsL.get(i).getName() + " Number of runs = " + avgVals.length + "Avg Values are : " + avgVals.toString() + "\n");
				for (int j = 0; j < avgVals.length; j++) { // this gives number of runs/startpoints/endpoints
					exists = false;
					places = g.getPlaceList();
					if (places.length > 1) {
						for (String st : places) {
							k = getPlaceInfoIndex(st);
							if (!isTransientPlace(st) && (placeInfo.get(k).getProperty("type").equalsIgnoreCase("DMVC"))) {
								if ((Math.abs(Double.parseDouble(placeInfo.get(k).getProperty("DMVCValue")) - avgVals[j]) < epsilon)
										&& (placeInfo.get(k).getProperty("DMVCVariable").equalsIgnoreCase(reqdVarsL.get(i).getName()))) {
				//					out.write("Place with key " + k + "already exists. so adding dmvcTime to it\n");
									addDmvcTime(placeInfo.get(k), reqdVarsL.get(i).getName(), calcDelay(runs.getStartPoint(j), runs.getEndPoint(j)));
									addDuration(placeInfo.get(k),calcDelay(runs.getStartPoint(j), runs.getEndPoint(j)));
									exists = true;
									prevPlace = currPlace;
									currPlace = getPlaceInfoIndex(st);// k;
									p2 = placeInfo.get(currPlace);
	//next few lines commented to remove multiple dmv input places of same variable from being marked initially.
									//	if (j == 0) { // adding the place corresponding to the first dmv run to initial marking. 
									//	placeInfo.get(k).setProperty("initiallyMarked", "true");
									//	g.changeInitialMarking("p"	+ placeInfo.get(k).getProperty("placeNum"),true);
									//} 
									// break ; here?
								}
							}
						}
					}
					if (!exists) {
						prevPlace = currPlace;
						currPlace = "d_" + i + "_" + dmvcCnt;
						p2 = new Properties();
						p2.setProperty("placeNum", numPlaces.toString());
						p2.setProperty("type", "DMVC");
						p2.setProperty("DMVCVariable", reqdVarsL.get(i).getName());
						p2.setProperty("DMVCValue", avgVals[j].toString());
						p2.setProperty("initiallyMarked", "false");
						addDmvcTime(p2, reqdVarsL.get(i).getName(),calcDelay(runs.getStartPoint(j), runs.getEndPoint(j)));
						//placeInfo.put("d_" + i + "_" + dmvcCnt, p2);
						if (j == 0) {
							transientNetPlaces.put("d_" + i + "_" + dmvcCnt, p2);
							g.addPlace("p" + numPlaces, true);
							p2.setProperty("initiallyMarked", "true");
						}
						else{
							placeInfo.put("d_" + i + "_" + dmvcCnt, p2);
							g.addPlace("p" + numPlaces, false);
						}
						dmvcInputPlaces.add("p" + numPlaces);
						if (j == 0) { // adding the place corresponding to the first dmv run to initial marking
							p2.setProperty("initiallyMarked", "true");
							g.changeInitialMarking("p" + p2.getProperty("placeNum"), true);
						}
						numPlaces++;
						out.write("Created new place with key " + "d_" + i + "_" + dmvcCnt + "\n");
						dmvcCnt++;
						//dmvcPlaceL.add(currPlace);
						cvgProp.setProperty("places", String.valueOf(Integer.parseInt(cvgProp.getProperty("places"))+1));
					}
					Double d = calcDelay(runs.getStartPoint(j), runs.getEndPoint(j));// data.get(0).get(runs.getEndPoint(j)) - data.get(0).get(runs.getStartPoint(j));
						// data.get(0).get(reqdVarsL.get(prevPlace.getDmvcVar()).getRuns().getEndPoint(j-1));
					// TEMPORARY FIX
					//Double minTime = getMinDmvcTime(p2);
					//if ((d > minTime*Math.pow(10.0, 4.0))){ // && (getMinDmvcTime(p2) == getMaxDmvcTime(p2))){
					//	deleteInvalidDmvcTime(p2, getMinDmvcTime(p2));	// updates dmin,dmax too
					//}
					//END TEMPORARY FIX
		// For dmv input nets, transition's delay assignment is
		// it's preset's duration; value assgnmt is value in postset.
					addDuration(p2,d);	
					//boolean transientNet = false;
					//	out.write("Delay in place p"+ p2.getProperty("placeNum")+ " after updating " + d + " is ["+ p2.getProperty("dMin") + ","+ p2.getProperty("dMax") + "]\n");
					if (prevPlace != null) {
						if (transitionInfo.containsKey(prevPlace + currPlace)) {
							p3 = transitionInfo.get(prevPlace + currPlace);
						} else {
							p3 = new Properties();
							p3.setProperty("transitionNum", numTransitions.toString());
							if (transientNetPlaces.containsKey(prevPlace)){
								transientNetTransitions.put(prevPlace + currPlace, p3);
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p" + transientNetPlaces.get(prevPlace).getProperty("placeNum"), "t" + transientNetTransitions.get(prevPlace + currPlace).getProperty("transitionNum")); 
								g.addMovement("t" + transientNetTransitions.get(prevPlace + currPlace).getProperty("transitionNum"), "p" + placeInfo.get(currPlace).getProperty("placeNum"));
							//	transientNet = true;
							}
							else{
								transitionInfo.put(prevPlace + currPlace, p3);
								g.addTransition("t" + numTransitions); // prevTranKey+key);
								g.addMovement("p"+ placeInfo.get(prevPlace).getProperty("placeNum"), "t"+ transitionInfo.get(prevPlace + currPlace).getProperty("transitionNum")); 
								g.addMovement("t"+ transitionInfo.get(prevPlace+ currPlace).getProperty("transitionNum"),"p"+ placeInfo.get(currPlace).getProperty("placeNum"));
							}
							numTransitions++;
							cvgProp.setProperty("transitions", String.valueOf(Integer.parseInt(cvgProp.getProperty("transitions"))+1));
						}
					}
					//if (!transientNet){	// assuming postset duration
					//	addDuration(p2, d);
					//}
					//else {
					//	addTransientDuration(p2, d);
					//}
					
				}
			} else if (reqdVarsL.get(i).isDmvc()) { // non-input dmvc

			}
		}
	} catch (IOException e) {
		e.printStackTrace();
		JOptionPane.showMessageDialog(BioSim.frame,
				"Log file couldn't be opened for writing rates and bins.",
				"ERROR!", JOptionPane.ERROR_MESSAGE);
	}
}
*/

/*
			ArrayList<ArrayList<String>> ifL = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> ifRateL = new ArrayList<ArrayList<String>>();
			for (Variable v: reqdVarsL){
				ifL.add(new ArrayList<String>());
				ifRateL.add(new ArrayList<String>());
			}
			String[] tL;
			for (String p : ratePlaces){
				tL = g.getPreset(p);
				for (String t : tL){
					if ((g.getPreset(t).length != 0) && (g.getPostset(t).length != 0) && (placeInfo.get(getPlaceInfoIndex(g.getPreset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))
							&& (placeInfo.get(getPlaceInfoIndex(g.getPostset(t)[0])).getProperty("type").equalsIgnoreCase("RATE"))) {
						ArrayList<Integer> diffL = diff(getPlaceInfoIndex(g.getPreset(t)[0]), getPlaceInfoIndex(g.getPostset(t)[0]));
						String ifStr = "";
						String[] binIncoming = getPlaceInfoIndex(g.getPreset(t)[0]).split("");
						String[] binOutgoing = getPlaceInfoIndex(g.getPostset(t)[0]).split("");
						boolean above;
						double val;
						for (int k : diffL) {
							if (Integer.parseInt(binIncoming[k + 1]) < Integer.parseInt(binOutgoing[k + 1])) {
								val = divisionsL.get(k).get(Integer.parseInt(binIncoming[k + 1])).doubleValue();
								above = true;
							} else {
								val = divisionsL.get(k).get(Integer.parseInt(binOutgoing[k + 1])).doubleValue();
								above = false;
							}
							if (above) {
								ifStr = reqdVarsL.get(k).getName()+"'above("+val+")";
							}
							else{
								ifStr = "not "+ reqdVarsL.get(k).getName()+"'above("+val+")";
							}
							for (int j = 0; j<reqdVarsL.size(); j++){
								String rateStr = "";
								if ((!reqdVarsL.get(j).isInput()) && (!reqdVarsL.get(j).isDmvc())){
								//if (!(reqdVarsL.get(j).isInput() && reqdVarsL.get(j).isDmvc())){
									rateStr = reqdVarsL.get(j).getName() + "'dot == span(" + getMinRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0,"+getMaxRate(getPlaceInfoIndex(p), reqdVarsL.get(j).getName())+".0)";
									ifL.get(j).add(ifStr);
									ifRateL.get(j).add(rateStr);
								}
							}
						}
					}
				}
			}
			for (int i = 0; i<reqdVarsL.size(); i++){
				for (int j = 0; j<ifL.get(i).size(); j++){
					if (j==0){
						vhdlAms.write("\tif "+ifL.get(i).get(j)+" use\n");
					}
					else{
						vhdlAms.write("\telsif "+ifL.get(i).get(j)+" use\n");
					}
					vhdlAms.write("\t\t"+ ifRateL.get(i).get(j)+";\n");
				}
				if (ifL.get(i).size() != 0){
					vhdlAms.write("\tend use;\n\n");
				}
			}
		//	vhdlAms.write("\tprocess\n");
		//	vhdlAms.write("\tbegin\n");
			for (int i = 0; i < dmvcVarPlaces.size(); i++){
				if (dmvcVarPlaces.get(i).size() != 0){
				vhdlAms.write("\tprocess\n");
				vhdlAms.write("\tbegin\n");
				for (String p : dmvcVarPlaces.get(i)){
					vhdlAms.write("\t\twait for delay("+placeInfo.get(p).getProperty("dMax")+","+placeInfo.get(p).getProperty("dMin") +");\n");
		// recheck above line.. truncating double to int.. becomes 0 in most unscaled cases?/
					vhdlAms.write("\t\tbreak "+reqdVarsL.get(i).getName()+ " => "+ placeInfo.get(p).getProperty("DMVCValue") + ";\n");
				}
				vhdlAms.write("\tend process;\n\n");
				}
			}
		//	vhdlAms.write("\tend process;\n\n");
			vhdlAms.write("end "+vhdFile.split("\\.")[0]+";\n");
			vhdlAms.close();
			
		}
		catch(IOException e){
			
		}
	}
	//T[] aux = (T[])a.clone();
}
*/
/*  OBSOLETE METHODS
public ArrayList<ArrayList<Double>> parseBinFile() {
	reqdVarsL = new ArrayList<Variable>();
	ArrayList<String> linesBinFileL = null;
	int h = 0;
	//ArrayList<ArrayList<Double>> divisionsL = new ArrayList<ArrayList<Double>>();
	
	try {
		Scanner f1 = new Scanner(new File(directory + separator + binFile));
		// log.addText(directory + separator + binFile);
		linesBinFileL = new ArrayList<String>();
		linesBinFileL.add(f1.nextLine());
		while (f1.hasNextLine()) {
			linesBinFileL.add(f1.nextLine());
		}
		out.write("Required variables and their levels are :");
		for (String st : linesBinFileL) {
			divisionsL.add(new ArrayList<Double>());
			String[] wordsBinFileL = st.split("\\s");
			for (int i = 0; i < wordsBinFileL.length; i++) {
				if (i == 0) {
					reqdVarsL.add(new Variable(wordsBinFileL[i]));
					out.write("\n"	+ reqdVarsL.get(reqdVarsL.size() - 1).getName());
				} else {
					divisionsL.get(h).add(Double.parseDouble(wordsBinFileL[i]));

				}
			}
			out.write(" " + divisionsL.get(h));
			h++;
			// max = Math.max(max, wordsBinFileL.length + 1);
		}
		f1.close();

	} catch (Exception e1) {
	}
	return divisionsL;
}

 public String cleanRow (String row){ 
  	String rowNS,rowTS = null; 
  	try{ 
  		rowNS =lParenR.matcher(row).replaceAll(""); 
  		rowTS = rParenR.matcher(rowNS).replaceAll(""); 
  		return rowTS; 
  	}
  	catch(PatternSyntaxException pse){ 
 	    System.out.format("There is a problem withthe regular expression!%n"); 
	    System.out.format("The pattern in question is:%s%n",pse.getPattern()); 
	    System.out.format("The description is:%s%n",pse.getDescription()); 
  	    System.out.format("The message is:%s%n",pse.getMessage()); 
 	    System.out.format("The index is:%s%n",pse.getIndex()); 
	    System.exit(0); 
	    return rowTS; 
	} 
}
 */

