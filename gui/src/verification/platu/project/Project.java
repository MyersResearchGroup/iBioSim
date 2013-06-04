package verification.platu.project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lpn.parser.LhpnFile;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;

import verification.platu.logicAnalysis.Analysis;
import verification.platu.logicAnalysis.CompositionalAnalysis;
import verification.platu.lpn.LPN;
import verification.platu.lpn.LPNTranRelation;
import verification.platu.lpn.io.Instance;
import verification.platu.lpn.io.PlatuInstLexer;
import verification.platu.lpn.io.PlatuInstParser;
import verification.platu.main.Options;
import verification.platu.markovianAnalysis.ProbLocalStateGraph;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;
import verification.timed_state_exploration.zoneProject.ContinuousUtilities;
import verification.timed_state_exploration.zoneProject.Zone;

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
		if (!Options.getProbabilisticModelFlag())
			for (int i=0; i<lpns.size(); i++) {
				LhpnFile lpn = lpns.get(i);
				StateGraph stateGraph = new StateGraph(lpn);
				lpn.addStateGraph(stateGraph);
				designUnitSet.add(stateGraph);
			}
		else 
			for (int i=0; i<lpns.size(); i++) {
				LhpnFile lpn = lpns.get(i);
				ProbLocalStateGraph stateGraph = new ProbLocalStateGraph(lpn);
				lpn.addStateGraph(stateGraph);
				designUnitSet.add(stateGraph);
			}
		lpnTranRelation = new LPNTranRelation(this.designUnitSet);
	}

	/**
	 * Find the SG for the entire project where each project state is a tuple of
	 * local states
	 * 
	 */
	public void search() {
		// TODO: temporarily set the input validation only to non-stochastic LPN models.
		if (!Options.getProbabilisticModelFlag())
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
			initStateArray[index] = curSg.genInitialState();
			int[] curStateVector = initStateArray[index].getVector();
			varValMap = curLpn.getAllVarsWithValuesAsInt(curStateVector);
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
				Zone z = new Zone(ls, true);
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
		dfsStateExploration.search_dfs(sgArray, initStateArray);
		
		long elapsedTimeMillis = System.currentTimeMillis() - start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		System.out.println("---> total runtime: " + elapsedTimeSec + " sec\n");
		if (Options.getOutputLogFlag())
			outputRuntimeLog(false, elapsedTimeSec);
		if (Options.getOutputSgFlag())
			if (sgArray != null)
				for (int i=0; i<sgArray.length; i++) {					
					sgArray[i].drawLocalStateGraph();
				}		
	}

	public Set<LPN> readLpn(final String src_file) {
		Set<LPN> lpnSet = null;

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
	
	/**
	 * Find the SG for the entire project where each project state is a tuple of
	 * local states. Use partial order reduction with trace-back during the dfs search.
	 * @param globalSGpath 
	 * @return 
	 * 
	 */
	public void searchWithPOR() {
		// TODO: temporarily set the input validation only to non-stochastic LPN models.
		if (!Options.getProbabilisticModelFlag())
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
//	    
		long start = System.currentTimeMillis(); 
		int lpnCnt = designUnitSet.size();

		/* Prepare search by placing LPNs in an array in the order of their indices.*/
        StateGraph[] sgArray = new StateGraph[lpnCnt];
        int idx = 0;
		for (StateGraph sg : designUnitSet) {
			LhpnFile lpn = sg.getLpn();
			lpn.setLpnIndex(idx++);
			sgArray[lpn.getLpnIndex()] = sg;
		}

		// Initialize the project state
		HashMap<String, Integer> varValMap = new HashMap<String, Integer>();
		State[] initStateArray = new State[lpnCnt];
		
		for (int index = 0; index < lpnCnt; index++) {
			LhpnFile curLpn = sgArray[index].getLpn();
			StateGraph curSg = sgArray[index];
			initStateArray[index] = curSg.genInitialState(); //curLpn.getInitState();
			int[] curStateVector = initStateArray[index].getVector();
			varValMap = curLpn.getAllVarsWithValuesAsInt(curStateVector);
//			DualHashMap<String, Integer> VarIndexMap = curLpn.getVarIndexMap();
//			HashMap<String, String> outVars = curLpn.getAllOutputs();		
//			for(String var : outVars.keySet()) {
//				varValMap.put(var, curStateVector[VarIndexMap.getValue(var)]);
//			}
		}

		// Adjust the value of the input variables in LPN in the initial state.
		// Add the initial states into their respective LPN.
		for (int index = 0; index < lpnCnt; index++) {
			StateGraph curSg = sgArray[index];
			initStateArray[index].update(curSg, varValMap, curSg.getLpn().getVarIndexMap());
			initStateArray[index] = curSg.addState(initStateArray[index]);
		}		
		
		Analysis dfsPOR = new Analysis(sgArray);
		if (Options.getPOR().toLowerCase().equals("tb"))
			dfsPOR.searchPOR_taceback(sgArray, initStateArray);
		else if (Options.getPOR().toLowerCase().equals("behavioral")) {
			CompositionalAnalysis compAnalysis = new CompositionalAnalysis();
			compAnalysis.compositionalFindSG(sgArray);			
			dfsPOR.searchPOR_behavioral(sgArray, initStateArray, lpnTranRelation, "state");
		}			
		else {
			System.out.println("Need to provide a POR method.");			
		}
		long elapsedTimeMillis = System.currentTimeMillis() - start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;	
		System.out.println("---> total runtime: " + elapsedTimeSec + " sec\n");
		if (Options.getOutputLogFlag())
			outputRuntimeLog(true, elapsedTimeSec);		
		if (Options.getOutputSgFlag())
			if (sgArray != null)
				for (int i=0; i<sgArray.length; i++) {								
					sgArray[i].drawLocalStateGraph();
				}
	}
	
	private void outputRuntimeLog(boolean isPOR, float runtime) {
		try {
			String fileName = null;
			if (isPOR)
				fileName = Options.getPrjSgPath() + Options.getLogName() + "_"
						+ Options.getPOR() + "_" + Options.getCycleClosingMthd() + "_" 
						+ Options.getCycleClosingAmpleMethd() +  "_runtime.log";
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
}
