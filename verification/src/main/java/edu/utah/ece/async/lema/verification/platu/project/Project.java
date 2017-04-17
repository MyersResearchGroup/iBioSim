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
package edu.utah.ece.async.lema.verification.platu.project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JProgressBar;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;

import edu.utah.ece.async.ibiosim.dataModels.util.GlobalConstants;
import edu.utah.ece.async.ibiosim.dataModels.util.exceptions.BioSimException;
import edu.utah.ece.async.lema.verification.lpn.LPN;
import edu.utah.ece.async.lema.verification.lpn.Translator;
import edu.utah.ece.async.lema.verification.platu.logicAnalysis.Analysis;
import edu.utah.ece.async.lema.verification.platu.logicAnalysis.CompositionalAnalysis;
import edu.utah.ece.async.lema.verification.platu.main.Options;
import edu.utah.ece.async.lema.verification.platu.markovianAnalysis.MarkovianAnalysis;
import edu.utah.ece.async.lema.verification.platu.markovianAnalysis.PerfromTransientMarkovAnalysisThread;
import edu.utah.ece.async.lema.verification.platu.markovianAnalysis.ProbGlobalStateSet;
import edu.utah.ece.async.lema.verification.platu.markovianAnalysis.ProbLocalStateGraph;
import edu.utah.ece.async.lema.verification.platu.platuLpn.LPNTranRelation;
import edu.utah.ece.async.lema.verification.platu.platuLpn.PlatuLPN;
import edu.utah.ece.async.lema.verification.platu.platuLpn.io.Instance;
import edu.utah.ece.async.lema.verification.platu.platuLpn.io.PlatuInstLexer;
import edu.utah.ece.async.lema.verification.platu.platuLpn.io.PlatuInstParser;
import edu.utah.ece.async.lema.verification.platu.stategraph.State;
import edu.utah.ece.async.lema.verification.platu.stategraph.StateGraph;
import edu.utah.ece.async.lema.verification.timed_state_exploration.octagon.Equivalence;
import edu.utah.ece.async.lema.verification.timed_state_exploration.octagon.Octagon;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.ContinuousUtilities;
import edu.utah.ece.async.lema.verification.timed_state_exploration.zoneProject.Zone;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Project {

	protected String label;
	
	/* 1. Each design unit has an unique label index.
	 * 2. The indices of all design units are sequential starting from 0. 
	 * */
	protected List<StateGraph> designUnitSet;
	
	/* The list for timing analysis */
//	protected List<StateGraph_timed> designUnitTimedSet;
	
	protected LPNTranRelation lpnTranRelation = null;
	
	protected CompositionalAnalysis analysis = null;
	
	private Observer observer;
	
	private static String separator = GlobalConstants.separator;
  	
	public Project() {
		this.label = "";
		this.designUnitSet = new ArrayList<StateGraph>(1);
		lpnTranRelation = new LPNTranRelation(this.designUnitSet);
	}
  	
	public Project(LPN lpn) {
		this.label = "";
		this.designUnitSet = new ArrayList<StateGraph>(1);
		StateGraph stateGraph = new StateGraph(lpn);
		designUnitSet.add(stateGraph);
		
	}
	
	/**
	 * If the OptionsFlag is false, then this constructor is identical to
	 * Poject(LhpnFile lpn). If the OptionsFlag is true, this constructor uses
	 * StateGraph_timed objects.
	 * 
	 * @author Andrew N. Fisher
	 * 
	 * @param lpn
	 * 			The lpn under consideration.
	 * @param OptionsFlag
	 * 			True for timing analysis and false otherwise. The option should match
	 * 			Options.getTimingAnalysisFlag().
	 */
//	public Project(LhpnFile lpn, boolean OptionsFlag)
//	{
//		if(Options.getTimingAnalysisFlag())
//		{
//			this.label = "";
//			this.designUnitSet = new ArrayList<StateGraph>(0);
//			this.designUnitTimedSet = new ArrayList<StateGraph_timed>(1);
//			StateGraph_timed stategraph = new StateGraph_timed(lpn);
//			designUnitTimedSet.add(stategraph);
//		}
//		else
//		{
//			this.label = "";
//			this.designUnitSet = new ArrayList<StateGraph>(1);
//			StateGraph stateGraph = new StateGraph(lpn);
//			designUnitSet.add(stateGraph);
//		}
//	}

	public Project(ArrayList<LPN> lpns) {
		this.label = "";
		this.designUnitSet = new ArrayList<StateGraph>(lpns.size());
		if (!Options.getMarkovianModelFlag())
			for (int i=0; i<lpns.size(); i++) {
				LPN lpn = lpns.get(i);
				StateGraph stateGraph = new StateGraph(lpn);
				lpn.addStateGraph(stateGraph);
				designUnitSet.add(stateGraph);
			}
		else 
			for (int i=0; i<lpns.size(); i++) {
				LPN lpn = lpns.get(i);
				ProbLocalStateGraph stateGraph = new ProbLocalStateGraph(lpn);
				lpn.addStateGraph(stateGraph);
				designUnitSet.add(stateGraph);
			}
		lpnTranRelation = new LPNTranRelation(this.designUnitSet);
	}

	/**
	 * Find the SG for the entire project where each project state is a tuple of
	 * local states
	 * @throws BioSimException 
	 * 
	 */
	public void search() throws BioSimException {
		// TODO: temporarily set the input validation only to non-stochastic LPN models.
		if (!Options.getMarkovianModelFlag())
			validateInputs();
		
//		if(Options.getSearchType().equals("compositional")){
//    		this.analysis = new CompositionalAnalysis();
//			
//			if(Options.getParallelFlag()){
//				this.analysis.parallelCompositionalFindSG(this.designUnitSet);
//			}
//			else{
//				this.analysis.findReducedSG(this.designUnitSet);
//			}
//			
//			return;
//		}
	    
		long startReachability = System.currentTimeMillis(); 
		int lpnCnt = designUnitSet.size();

		/* Prepare search by placing LPNs in an array in the order of their indices.*/
        StateGraph[] sgArray = new StateGraph[lpnCnt];
        int idx = 0;
		for (StateGraph du : designUnitSet) {
			LPN lpn = du.getLpn();
			lpn.setLpnIndex(idx++);
			sgArray[lpn.getLpnIndex()] = du;
		}
		
		// If timing, then create the sgArray with StateGraph_timed objects.
//		if(Options.getTimingAnalysisFlag())
//		{
//			for(StateGraph_timed du : this.designUnitTimedSet)
//			{
//				LhpnFile lpn = du.getLpn();
//				lpn.setIndex(idx++);
//				sgArray[lpn.getIndex()] = du;
//			}
//		}
		
		// Initialize the project state
		HashMap<String, Integer> varValMap = new HashMap<String, Integer>();
		State[] initStateArray = new State[lpnCnt];
		for (int index = 0; index < lpnCnt; index++) {
			LPN curLpn = sgArray[index].getLpn();
			StateGraph curSg = sgArray[index];			
			initStateArray[index] = curSg.genInitialState();			
			int[] curVariableVector = initStateArray[index].getVariableVector();
			varValMap = curLpn.getAllVarsWithValuesAsInt(curVariableVector);
		}

		// Adjust the value of the input variables in LPN in the initial state.
		// Add the initial states into their respective state graphs.
		for (int index = 0; index < lpnCnt; index++) {
			StateGraph curSg = sgArray[index];
			// If this is a timing analysis, the boolean inequality variables
			// must be updated.
			if(Options.getTimingAnalysisFlag()){
				// First create a zone with the continuous variables.
				State[] ls = new State[1];
				ls[0] = initStateArray[index];
				
				Equivalence z = null;
				if(Options.getTimingAnalysisType().equals("zone")){
					z = new Zone(ls, true);
				}
				else{
					z = new Octagon(ls, true);
				}
				ContinuousUtilities.updateInitialInequalities(z, ls[0]);
				initStateArray[index] = curSg.genInitialState();
			}			
			initStateArray[index].update(curSg, varValMap, curSg.getLpn().getVarIndexMap());			
			initStateArray[index] = curSg.addState(initStateArray[index]);	
		}
		
		// Initialize the zones for the initStateArray, if timining is enabled.
//		if(Options.getTimingAnalysisFlag())
//		{
//			for(int index =0; index < lpnCnt; index++)
//			{
//				if(sgArray[index] instanceof StateGraph_timed)
//				{
//					
//				}
//			}
//		}
		
//		if (Options.getTimingAnalysisFlag()) {
//			new TimingAnalysis(sgArray); 
//			return;
//		}
//		else if(!Options.getTimingAnalysisFlag()) {
//			Analysis tmp = new Analysis(sgArray, initStateArray, lpnTranRelation, Options.getSearchType());
//			// Analysis tmp = new Analysis(lpnList, curStateArray,
//			// lpnTranRelation, "dfs_por");
//			//Analysis tmp = new Analysis(modArray, initStateArray, lpnTranRelation, "dfs");
//			//Analysis tmp = new Analysis(modArray, initStateArray, lpnTranRelation, "dfs_noDisabling");
//		}
//		else {
//			System.out.println("---> Error: wrong value for option 'timingAnalysis'");
//			return;
//		}
		
		/* Entry point for the timed analysis. */
//		if(Options.getTimingAnalysisFlag())
//		{
//			Analysis_Timed dfsTimedStateExploration = new Analysis_Timed(sgArray);
//			dfsTimedStateExploration.search_dfs_timed(sgArray, initStateArray);
//			return new StateGraph[0];
//		}

		Analysis dfsStateExploration = new Analysis(sgArray);
		dfsStateExploration.addObserver(observer);
		if (!Options.getMarkovianModelFlag()) {
			if (Options.getPOR().toLowerCase().equals("off")) {
				// DFS state exploration without any state reduction.
				dfsStateExploration.search_dfs(sgArray, initStateArray);
			}
			if (Options.getPOR().toLowerCase().equals("tb")) {
				dfsStateExploration.searchPOR_taceback(sgArray, initStateArray);
			}				
			else if (Options.getPOR().toLowerCase().equals("behavioral")) {
				//CompositionalAnalysis compAnalysis = new CompositionalAnalysis();
				CompositionalAnalysis.compositionalFindSG(sgArray);
				// TODO: temporarily commented out POR with behavioral analysis
				//dfsStateExploration.searchPOR_behavioral(sgArray, initStateArray, lpnTranRelation, "state");
			}			
			long elapsedTimeMillis = System.currentTimeMillis() - startReachability; 
			float elapsedTimeSec = elapsedTimeMillis/1000F;
			System.out.println("---> total runtime for reachability analysis: " + elapsedTimeSec + " sec\n");
			if (Options.getOutputLogFlag()) {
				if (Options.getPOR().toLowerCase().equals("off")) {
					outputRuntimeLog(false, elapsedTimeSec);
				}
				else
					outputRuntimeLog(true, elapsedTimeSec);
			}				
			if (Options.getOutputSgFlag()) {			
				for (int i=0; i<sgArray.length; i++) {								
					sgArray[i].drawLocalStateGraph();
				}
			}
		}
		else { // Probabilistic model
			// ====== TEMP ========
			ProbGlobalStateSet globalStateSet = null;
			if (Options.getPOR().toLowerCase().equals("off")) {
				globalStateSet = (ProbGlobalStateSet) dfsStateExploration.search_dfs(sgArray, initStateArray);
			}
			else if (Options.getPOR().toLowerCase().equals("tb")) {
				globalStateSet = (ProbGlobalStateSet) dfsStateExploration.searchPOR_taceback(sgArray, initStateArray);
			}
			else if (Options.getPOR().toLowerCase().equals("behavioral")) {
				//CompositionalAnalysis compAnalysis = new CompositionalAnalysis();
				CompositionalAnalysis.compositionalFindSG(sgArray);	
				// TODO: (temp) Temporarily disable POR behavioral analysis on prob. models.
				//globalStateSet = (ProbGlobalStateSet) dfsStateExploration.searchPOR_behavioral(sgArray, initStateArray, lpnTranRelation, "state");
			}
			long elapsedTimeMillisReachability = System.currentTimeMillis() - startReachability; 
			float elapsedTimeSecReachability = elapsedTimeMillisReachability/1000F;
			System.out.println("---> total runtime for reachability analysis: " + elapsedTimeSecReachability + " sec");			
			if (Options.getOutputLogFlag())
				outputRuntimeLog(false, elapsedTimeSecReachability);			
			System.gc();
////			// -------------------- Temp: steady-state analysis --------------
//			System.out.println("--------- Steady State Analysis ---------");
//			long startSteadyState = System.currentTimeMillis();
//			MarkovianAnalysis markovianAnalysis = new MarkovianAnalysis(globalStateSet);
//			double tolerance = 0.0000000001;
//			PrjState initialSt = ((ProbGlobalStateSet) globalStateSet).getInitialState();
//			markovianAnalysis.performSteadyStateMarkovianAnalysis(tolerance, null, initialSt, null);
//			dfsStateExploration.drawGlobalStateGraph(sgArray, initialSt, globalStateSet);
//			long elapsedTimeMillisSteadyState = System.currentTimeMillis() - startSteadyState; 
//			float elapsedTimeSecSteadyState = elapsedTimeMillisSteadyState/1000F;
//			System.out.println("---> total runtime for steady state analysis: " + elapsedTimeSecSteadyState + " sec");
//			// ------------------------------------------------------------
			
			// -------------------- Temp: transient analysis --------------
			System.out.println("--------- Transient Analysis ---------");
			long startTransientAnalysis = System.currentTimeMillis();
			MarkovianAnalysis markovianAnalysis = new MarkovianAnalysis(globalStateSet);
			

			// --- toggle_switch ---
			double timeLimit = 5000.0;
			double printInterval = 100.0;			
			double timeStep = 100.0;
			double absError = 1.0e-9;		
			String prop = "Pr=?{PF[<=5000]((LacI>40)&(TetR<20))}";
			//String prop = "Pr=?{PF[<=5000]((TetR>40)&(LacI<20))}";			
			// --- end of toggle_switch ---
		
			// === C-element circuits ===
//			double timeLimit = 2100.0;
//			double printInterval = 100.0;			
//			double timeStep = 100.0;
//			double absError = 1.0e-9;
//			// --- majority ---
//			String prop = "Pr=?{PF[<=2100]((E>40)&(C<20))}";
			// --- end of majority ---
////////			
////////			// --- speedInd ---
////////			//String prop = "Pr=?{PF[<=2100]((S2>80)&(S3<20))}";
////////			// --- end of speedInd ---
////////			
////////			// --- toggle ---
////////			//String prop = "Pr=?{PF[<=2100]((Z>80)&(Y<40))}";
////////			// --- end of toggle ---
////////			
////////			
//			// ========================================
			JProgressBar progress = new JProgressBar(0, 100);

			PerfromTransientMarkovAnalysisThread performMarkovAnalysisThread = new PerfromTransientMarkovAnalysisThread(
					markovianAnalysis, progress);			
			String[] condition = Translator.getProbpropParts(Translator.getProbpropExpression(prop));
			boolean globallyTrue = false;
			if (prop.contains("PF")) {
				condition[0] = "true";
			}
			else if (prop.contains("PG")) {
				condition[0] = "true";
				globallyTrue = true;
			}
			performMarkovAnalysisThread.start(timeLimit, timeStep, printInterval, absError, condition, globallyTrue);
			
			try {
				performMarkovAnalysisThread.join();
			} catch (InterruptedException e) {
				//JOptionPane.showMessageDialog(Gui.frame, "Error In Execution!", "Error In Execution", JOptionPane.ERROR_MESSAGE);			
				e.printStackTrace();
			}
			//dfsStateExploration.drawGlobalStateGraph(sgArray, globalStateSet.getInitialState(), globalStateSet, true);		
			//markovianAnalysis.printStateSetStatus(globalStateSet, "end of transient analysis");
			long elapsedTimeMillisTransient = System.currentTimeMillis() - startTransientAnalysis; 
			float elapsedTimeSecTransient = elapsedTimeMillisTransient/1000F;
			System.out.println("---> total runtime for transient analysis: " + elapsedTimeSecTransient + " sec");
			// ------------------------------------------------------------
//			
//			// -------------------- Temp: nested analysis --------------
//			// ------------------------------------------------------------
		}
//		if (Options.getOutputSgFlag())
//			for (int i=0; i<sgArray.length; i++) 
//				sgArray[i].drawLocalStateGraph();				
	}

	public static Set<PlatuLPN> readLpn(final String src_file) {
		Set<PlatuLPN> lpnSet = null;

		try {
			if (!src_file.endsWith(".lpn")) {
				System.err.println("Invalid file extention");
				System.exit(1);
			}

//			ANTLRFileStream input = new ANTLRFileStream(src_file);
//			PlatuGrammarLexer lexer = new PlatuGrammarLexer(input);
//			TokenStream tokenStream = new CommonTokenStream(lexer);
//			PlatuGrammarParser parser = new PlatuGrammarParser(tokenStream);
//			lpnSet = parser.lpn(this);
			
		} catch (Exception ex) {
			Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		return lpnSet;
	}
	
	private static void outputRuntimeLog(boolean isPOR, float runtime) {
		try {
			String fileName = null;
			if (isPOR) {
				fileName = Options.getPrjSgPath() + separator + Options.getLogName() + "_"
						+ Options.getPOR() + "_" + Options.getCycleClosingMthd() + "_" 
						+ Options.getCycleClosingStrongStubbornMethd() +  "_runtime.log";
			}
			else
				fileName = Options.getPrjSgPath() + Options.getLogName() + "_full_runtime.log";
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write("runtime(sec)\n");
			out.write(runtime + "\n");
			out.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error producing local state graph as dot file.");
		}	
	}
	
	public void readLpn(List<String> fileList) {		
		for(String srcFile : fileList){
			if (!srcFile.endsWith(".lpn")) {
				System.err.println("Invalid file extention");
				System.exit(1);
			}

			ANTLRFileStream input = null;
			try {
				input = new ANTLRFileStream(srcFile);
			} 
			catch (IOException e) {
				System.err.println("error: error reading " + srcFile);
				System.exit(1);
			}
			
			PlatuInstLexer lexer = new PlatuInstLexer(input);
			TokenStream tokenStream = new CommonTokenStream(lexer);
			PlatuInstParser parser = new PlatuInstParser(tokenStream);
			
			parser.parseLpnFile(this);
		}

		PlatuInstParser.includeSet.removeAll(fileList);
		for(String srcFile : PlatuInstParser.includeSet){
			if (!srcFile.endsWith(".lpn")) {
				System.err.println("Invalid file extention");
				System.exit(1);
			}

			ANTLRFileStream input = null;
			try {
				input = new ANTLRFileStream(srcFile);
			} 
			catch (IOException e) {
				System.err.println("error: error reading " + srcFile);
				System.exit(1);
			}
			
			PlatuInstLexer lexer = new PlatuInstLexer(input);
			TokenStream tokenStream = new CommonTokenStream(lexer);
			PlatuInstParser parser = new PlatuInstParser(tokenStream);
			
			parser.parseLpnFile(this);
		}
		
		edu.utah.ece.async.lema.verification.platu.platuLpn.PlatuLPN.nextID = 1;
		
		HashMap<String, PlatuLPN> instanceMap = new HashMap<String, PlatuLPN>();
		for(Instance inst : PlatuInstParser.InstanceList){
			PlatuLPN lpn = PlatuInstParser.LpnMap.get(inst.getLpnLabel());
			if(lpn == null){
				System.err.println("error: class " + inst.getLpnLabel() + " does not exist");
				return;
			}
			
			PlatuLPN instLpn = lpn.instantiate(inst.getName());
			
			instanceMap.put(instLpn.getLabel(), instLpn);
			this.designUnitSet.add(instLpn.getStateGraph());
		}
		
		// TODO: (irrelevant) Is this really needed???
		/*
		for(StateGraph sg : this.designUnitSet){
			sg.getLpn().setGlobals(this.designUnitSet);
		}
		*/
		
		for(Instance inst : PlatuInstParser.InstanceList){
			PlatuLPN dstLpn = instanceMap.get(inst.getName());
			if(dstLpn == null){
				System.err.println("error: instance " + inst.getName() + " does not exist");
				return;
			}
			
			List<String> argumentList = dstLpn.getArgumentList();
			List<String> varList = inst.getVariableList();
			List<String> modList = inst.getModuleList();

			if(argumentList.size() != varList.size()){
				System.err.println("error: incompatible number of arguments for instance " + inst.getName());
				return;
			}
			
			for(int i = 0; i < argumentList.size(); i++){
				PlatuLPN srcLpn = instanceMap.get(modList.get(i));
				if(srcLpn == null){
					System.err.println("error: instance " + modList.get(i) + " does not exist");
					return;
				}
				
				String outputVar = varList.get(i);
				String inputVar = argumentList.get(i);
				srcLpn.connect(outputVar, dstLpn, inputVar);
			}
		}
	}
	
	/**
	 * @return the designUnitSet
	 */
	public List<StateGraph> getDesignUnitSet() {
		return designUnitSet;
	}
    
    /**
     * Validates each lpn's input variables are driven by another lpn's output.
     */
    protected void validateInputs(){	// Changed protection level. ANF
    	boolean error = false;
    	for(StateGraph sg : designUnitSet){
	        for(String input : sg.getLpn().getAllInputs().keySet()){
	        	boolean connected = false;
	        	for(StateGraph sg2 : designUnitSet){
	        		if(sg == sg2) 
	        			continue;	        		
	        		if(sg2.getLpn().getAllOutputs().keySet().contains(input)){
	        			connected = true;
	        			break;
	        		}
	        	}	        	
	        	if(!connected){
	        		error = true;
	        		System.err.println("error in lpn " + sg.getLpn().getLabel() + ": input variable '" + input + "' is not dependent on an output");
	        	}
	        }
    	}
    	
        if(error){
        	System.exit(1);
        }
    }
    
    public void setObserver(Observer observer)
    {
      this.observer = observer;
    }
}
