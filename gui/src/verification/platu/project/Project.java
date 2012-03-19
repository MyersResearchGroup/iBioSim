package verification.platu.project;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import lpn.parser.LhpnFile;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import verification.platu.lpn.DualHashMap;
import verification.platu.lpn.io.Instance;
import verification.platu.lpn.io.PlatuGrammarLexer;
import verification.platu.lpn.io.PlatuInstLexer;
import verification.platu.main.Options;
import verification.platu.stategraph.*;
import verification.platu.TimingAnalysis.*;
import verification.platu.logicAnalysis.Analysis;
import verification.platu.logicAnalysis.CompositionalAnalysis;
import verification.platu.lpn.LPN;
import verification.platu.lpn.LPNTranRelation;
import verification.platu.lpn.io.PlatuGrammarParser;
import verification.platu.lpn.io.PlatuInstParser;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;
import verification.timed_state_exploration.zone.Analysis_Timed;
import verification.timed_state_exploration.zone.StateGraph_timed;

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
  	
	public Project() {
		this.label = "";
		this.designUnitSet = new ArrayList<StateGraph>(1);
		lpnTranRelation = new LPNTranRelation(this.designUnitSet);
	}
  	
	public Project(LhpnFile lpn) {
		this.label = "";
		this.designUnitSet = new ArrayList<StateGraph>(1);
		StateGraph stateGraph = new StateGraph(lpn);
		designUnitSet.add(stateGraph);
		//stateGraph.printStates();
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

	public Project(ArrayList<LhpnFile> lpns) {
		this.label = "";
		this.designUnitSet = new ArrayList<StateGraph>(lpns.size());
		for (int i=0; i<lpns.size(); i++) {
			LhpnFile lpn = lpns.get(i);
			StateGraph stateGraph = new StateGraph(lpn);
			designUnitSet.add(stateGraph);
		}		
	}

	/**
	 * Find the SG for the entire project where each project state is a tuple of
	 * local states
	 * 
	 */
	public StateGraph[] search() {	
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
	    
		long start = System.currentTimeMillis(); 
		int lpnCnt = designUnitSet.size();

		/* Prepare search by placing LPNs in an array in the order of their indices.*/
        StateGraph[] sgArray = new StateGraph[lpnCnt];
        int idx = 0;
		for (StateGraph du : designUnitSet) {
			LhpnFile lpn = du.getLpn();
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
			LhpnFile curLpn = sgArray[index].getLpn();
			StateGraph curSg = sgArray[index];
			initStateArray[index] = curSg.getInitState(); //curLpn.getInitState();
			int[] curStateVector = initStateArray[index].getVector();
			varValMap = curLpn.getAllVarsWithValues(curStateVector);
//			HashMap<String, String> vars = curLpn.getAllOutputs();//curLpn.getAllOutputs();
//			DualHashMap<String, Integer> VarIndexMap = curLpn.getVarIndexMap();
//			for(String var : vars.keySet()) {
//				varValMap.put(var, curStateVector[VarIndexMap.getValue(var)]);
//			}
			
		}

		// TODO: (future) Need to adjust the transition vector as well?
		// Adjust the value of the input variables in LPN in the initial state.
		// Add the initial states into their respective LPN.
		for (int index = 0; index < lpnCnt; index++) {
			StateGraph curSg = sgArray[index];
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
		StateGraph[] stateGraphArray = dfsStateExploration.search_dfs(sgArray, initStateArray);
		
		long elapsedTimeMillis = System.currentTimeMillis() - start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		System.out.println("---> total runtime: " + elapsedTimeSec + " sec\n");
		
		return stateGraphArray;
	}

	public Set<LPN> readLpn(final String src_file) {
		Set<LPN> lpnSet = null;

		try {
			if (!src_file.endsWith(".lpn")) {
				System.err.println("Invalid file extention");
				System.exit(1);
			}

			ANTLRFileStream input = new ANTLRFileStream(src_file);
			PlatuGrammarLexer lexer = new PlatuGrammarLexer(input);
			TokenStream tokenStream = new CommonTokenStream(lexer);
			PlatuGrammarParser parser = new PlatuGrammarParser(tokenStream);
			lpnSet = parser.lpn(this);
			
		} catch (Exception ex) {
			Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		return lpnSet;
	}
	
	/**
	 * Find the SG for the entire project where each project state is a tuple of
	 * local states. Use partial order reduction during dfs search.
	 * @param cycleClosingMthdIndex 
	 * @param outputDotFile 
	 * @return 
	 * 
	 */
	public StateGraph searchWithPOR(int cycleClosingMthdIndex) {	
		validateInputs();
//		
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
//	    
		long start = System.currentTimeMillis(); 
		int lpnCnt = designUnitSet.size();

		/* Prepare search by placing LPNs in an array in the order of their indices.*/
        StateGraph[] sgArray = new StateGraph[lpnCnt];
        int idx = 0;
		for (StateGraph du : designUnitSet) {
			LhpnFile lpn = du.getLpn();
			lpn.setLpnIndex(idx++);
			sgArray[lpn.getLpnIndex()] = du;
		}

		// Initialize the project state
		HashMap<String, Integer> varValMap = new HashMap<String, Integer>();
		State[] initStateArray = new State[lpnCnt];
		
		for (int index = 0; index < lpnCnt; index++) {
			LhpnFile curLpn = sgArray[index].getLpn();
			StateGraph curSg = sgArray[index];
			initStateArray[index] = curSg.getInitState(); //curLpn.getInitState();
			int[] curStateVector = initStateArray[index].getVector();
			varValMap = curLpn.getAllVarsWithValues(curStateVector);
//			DualHashMap<String, Integer> VarIndexMap = curLpn.getVarIndexMap();
//			HashMap<String, String> outVars = curLpn.getAllOutputs();		
//			for(String var : outVars.keySet()) {
//				varValMap.put(var, curStateVector[VarIndexMap.getValue(var)]);
//			}
		}

		// Adjust the value of the input variables in LPN in the initial state.
		// Add the initial states into their respective LPN.
		for (int index = 0; index < lpnCnt; index++) {
			StateGraph curLpn = sgArray[index];
			initStateArray[index].update(curLpn, varValMap, curLpn.getLpn().getVarIndexMap());
			initStateArray[index] = curLpn.addState(initStateArray[index]);
			
		}		
		
		StateGraph stateGraph;
		Analysis dfsStateExplorationWithPOR = new Analysis(sgArray);
		// cycleClosingMthdIndex: 0 = Use sticky transitions
		//						  1 = Use behavioral analysis
		//						  2 = Use behavioral analysis with state trace-back
		//                        3 = No cycle closing
		//						  4 = Peled's cycle condition
		if (cycleClosingMthdIndex == 0 || cycleClosingMthdIndex == 3) 
			stateGraph = dfsStateExplorationWithPOR.search_dfsPOR(sgArray, initStateArray, cycleClosingMthdIndex);
		else 
			stateGraph = dfsStateExplorationWithPOR.search_dfsPORrefinedCycleRule(sgArray, initStateArray, cycleClosingMthdIndex);
		
		long elapsedTimeMillis = System.currentTimeMillis() - start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		System.out.println("---> total runtime: " + elapsedTimeSec + " sec\n");
		return stateGraph;
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
			
			try {
				parser.parseLpnFile(this);
			} 
			catch (RecognitionException e) {
				System.err.println("error: error parsing " + srcFile);
				System.exit(1);
			}
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
			
			try {
				parser.parseLpnFile(this);
			} 
			catch (RecognitionException e) {
				System.err.println("error: error parsing " + srcFile);
				System.exit(1);
			}
		}
		
		verification.platu.lpn.LPN.nextID = 1;
		
		HashMap<String, LPN> instanceMap = new HashMap<String, LPN>();
		for(Instance inst : PlatuInstParser.InstanceList){
			LPN lpn = PlatuInstParser.LpnMap.get(inst.getLpnLabel());
			if(lpn == null){
				System.err.println("error: class " + inst.getLpnLabel() + " does not exist");
				System.exit(1);
			}
			
			LPN instLpn = lpn.instantiate(inst.getName());
			
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
			LPN dstLpn = instanceMap.get(inst.getName());
			if(dstLpn == null){
				System.err.println("error: instance " + inst.getName() + " does not exist");
				System.exit(1);
			}
			
			List<String> argumentList = dstLpn.getArgumentList();
			List<String> varList = inst.getVariableList();
			List<String> modList = inst.getModuleList();

			if(argumentList.size() != varList.size()){
				System.err.println("error: incompatible number of arguments for instance " + inst.getName());
				System.exit(1);
			}
			
			for(int i = 0; i < argumentList.size(); i++){
				LPN srcLpn = instanceMap.get(modList.get(i));
				if(srcLpn == null){
					System.err.println("error: instance " + modList.get(i) + " does not exist");
					System.exit(1);
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
	        		if(sg == sg2) continue;
	        		
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
}
