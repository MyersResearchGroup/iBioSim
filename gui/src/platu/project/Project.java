package platu.project;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import lpn.parser.LhpnFile;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import platu.logicAnalysis.Analysis;
import platu.logicAnalysis.CompositionalAnalysis;
import platu.lpn.DualHashMap;
import platu.lpn.LPN;
import platu.lpn.LPNTranRelation;
import platu.lpn.io.Instance;
import platu.lpn.io.PlatuGrammarLexer;
import platu.lpn.io.PlatuGrammarParser;
import platu.lpn.io.PlatuInstLexer;
import platu.lpn.io.PlatuInstParser;
import platu.main.Options;
import platu.stategraph.*;
import platu.TimingAnalysis.*;

public class Project {

	protected String label;
	
	/* 1. Each design unit has an unique label index.
	 * 2. The indices of all design units are sequential starting from 0. 
	 * */
	protected List<StateGraph> designUnitSet;
	
	protected LPNTranRelation lpnTranRelation = null;
	
	protected CompositionalAnalysis analysis = null;
		      	
	public Project() {
		this.label = "";
		this.designUnitSet = new ArrayList<StateGraph>(1);
		lpnTranRelation = new LPNTranRelation(this.designUnitSet);
	}

	/**
	 * Find the SG for the entire project where each project state is a tuple of
	 * local states
	 * 
	 */
	public void search() {	
		validateInputs();
		
		if(Options.getSearchType().equals("compositional")){
    		this.analysis = new CompositionalAnalysis();
			
			if(Options.getParallelFlag()){
				this.analysis.parallelCompositionalFindSG(this.designUnitSet);
			}
			else{
				this.analysis.findReducedSG(this.designUnitSet);
			}
			
			return;
		}
	    
		long start = System.currentTimeMillis(); 
		int lpnCnt = designUnitSet.size();

		/* Prepare search by placing LPNs in an array in the order of their indices.*/
        StateGraph[] sgArray = new StateGraph[lpnCnt];
        int idx = 0;
		for (StateGraph du : designUnitSet) {
			LhpnFile lpn = du.getLpn();
			lpn.setIndex(idx++);
			sgArray[lpn.getIndex()] = du;
		}

		// Initialize the project state
		HashMap<String, Integer> varValMap = new HashMap<String, Integer>();
		State[] initStateArray = new State[lpnCnt];
		
		for (int index = 0; index < lpnCnt; index++) {
			LhpnFile curLpn = sgArray[index].getLpn();
			StateGraph curSg = sgArray[index];
			// TODO: (Done) get InitState should be created in StateGraph, not in LhpnFile.
			initStateArray[index] = curSg.getInitState(); //curLpn.getInitState();
			int[] curStateVector = initStateArray[index].getVector();
			HashMap<String, String> outVars = curLpn.getAllOutputs();
			DualHashMap<String, Integer> VarIndexMap = curLpn.getVarIndexMap();
			for(String var : outVars.keySet()) {
				varValMap.put(var, curStateVector[VarIndexMap.getValue(var)]);
			}
			
		}

		// Adjust the value of the input variables in LPN in the initial state.
		// Add the initial states into their respective LPN.
		for (int index = 0; index < lpnCnt; index++) {
			// TODO: (Done) need to change this to use our LPN 
			StateGraph curLpn = sgArray[index];
			initStateArray[index].update(varValMap, curLpn.getLpn().getVarIndexMap());
			initStateArray[index] = curLpn.addState(initStateArray[index]);
			
		}		
		

		if (Options.getTimingAnalysisFlag()) {
			new TimingAnalysis(sgArray); 
			return;
		}
		else if(!Options.getTimingAnalysisFlag()) {
			Analysis tmp = new Analysis(sgArray, initStateArray, lpnTranRelation, Options.getSearchType());
			// Analysis tmp = new Analysis(lpnList, curStateArray,
			// lpnTranRelation, "dfs_por");
			//Analysis tmp = new Analysis(modArray, initStateArray, lpnTranRelation, "dfs");
			//Analysis tmp = new Analysis(modArray, initStateArray, lpnTranRelation, "dfs_noDisabling");
		}
		else {
			System.out.println("---> Error: wrong value for option 'timingAnalysis'");
			return;
		}
		
		long elapsedTimeMillis = System.currentTimeMillis() - start; 
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		System.out.println("---> total runtime: " + elapsedTimeSec + " sec\n");
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
		
		platu.lpn.LPN.nextID = 1;
		
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
		
		// TODO: Is this really needed???
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
    private void validateInputs(){
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
