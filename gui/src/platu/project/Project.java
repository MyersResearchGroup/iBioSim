package platu.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import platu.Options;
import platu.logicAnalysis.Analysis;
import platu.logicAnalysis.CompositionalAnalysis;
import platu.lpn.DualHashMap;
import platu.lpn.LPN;
import platu.lpn.LPNTranRelation;
import platu.lpn.LpnTranList;
import platu.lpn.VarValSet;
import platu.lpn.io.Instance;
import platu.lpn.io.PlatuGrammarLexer;
import platu.lpn.io.PlatuGrammarParser;
import platu.lpn.io.PlatuInstLexer;
import platu.lpn.io.PlatuInstParser;
import platu.stategraph.StateGraph;
import platu.stategraph.state.State;
import platu.TimingAnalysis.*;

public class Project {

    enum Mode{TIMED_ITERATIVE, UNTIMED_ITERATIVE, TIMED_MDD,UNTIMED_MDD,UNKNOWN};
    public Mode mode=Mode.UNKNOWN;
	protected String label;
	protected Set<String> inputs;
	protected Set<String> outputs;
	protected Set<String> internals;
	protected List<StateGraph> designUnitSet;
	protected LPNTranRelation lpnTranRelation = null;
	protected CompositionalAnalysis analysis = null;
	protected HashMap<Integer, Integer> IntObjTable;
	protected IDGenerator<Object> prjStateSet = new IDGenerator<Object>(1);
      	
	public Project() {
		this.inputs = new HashSet<String>(1);
		this.label = "";
		this.outputs = new HashSet<String>(1);
		this.internals = new HashSet<String>(1);
		this.designUnitSet = new ArrayList<StateGraph>(1);
		IntObjTable = new HashMap<Integer, Integer>(1);
		lpnTranRelation = new LPNTranRelation(this.designUnitSet);
	}
	
	public Project(Set<String> inputs, String Label, Set<String> outputs,//
			Set<String> internals, VarValSet PropSet, LpnTranList LpnTranSet, //
			List<StateGraph> designUnit_set, //
			int max_states, int max_trans) {
		this.inputs = inputs;
		this.label = Label;
		this.outputs = outputs;
		this.internals = internals;
		this.designUnitSet = designUnit_set;
		int i = 0;
		for(LPN du : designUnitSet) {
			du.setIndex(i++);
		}
		
		lpnTranRelation = new LPNTranRelation(this.designUnitSet);
	}

	/**
	 * Find the SG for the entire project where each project state is a tuple of
	 * local states
	 * 
	 */
	public void search() {	
		validateInputs();
		
		long start = System.currentTimeMillis(); 
		int lpnCnt = designUnitSet.size();

		// Initialize the memory for storing local states.
        StateGraph[] modArray = new StateGraph[lpnCnt];

		int i = 0;
		for (StateGraph du : designUnitSet) {
			du.setIndex(i);
			modArray[i] = du;
			i++;
		}

		// Initialize the project state
		HashMap<String, Integer> varValMap = new HashMap<String, Integer>();
		State[] initStateArray = new State[lpnCnt];
		
		for (int index = 0; index < lpnCnt; index++) {
			LPN curLpn = modArray[index];
			initStateArray[index] = curLpn.getInitStateUntimed();
			int[] curStateVector = initStateArray[index].getVector();
			HashSet<String> outVars = curLpn.getOutputs();
			DualHashMap<String, Integer> VarIndexMap = curLpn.getVarIndexMap();
			for(String var : outVars) {
				varValMap.put(var, curStateVector[VarIndexMap.getValue(var)]);
			}
		}

		// Adjust the value of the input variables in LPN in the initial state.
		// Add the initial states into their respective LPN.
		for (int index = 0; index < lpnCnt; index++) {
			StateGraph curLpn = modArray[index];
			initStateArray[index].update(varValMap, curLpn.getVarIndexMap());
			initStateArray[index] = curLpn.addState(initStateArray[index]);
		}		
		

		if (Options.getTimingAnalysisFlag()) {
			new TimingAnalysis(modArray); 
			return;
		}
		else if(!Options.getTimingAnalysisFlag()) {
			// Analysis tmp = new Analysis(lpnList, curStateArray,
			// lpnTranRelation, "dfs_mdd_2");
			// Analysis tmp = new Analysis(lpnList, curStateArray,
			// lpnTranRelation, "dfs_por");
			// Analysis tmp = new Analysis(lpnList, curStateArray,
			// lpnTranRelation, "dfs");
			Analysis tmp = new Analysis(modArray, initStateArray, lpnTranRelation, "dfs_noDisabling");
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
			this.designUnitSet.add((StateGraph) instLpn);
		}
		
		for(StateGraph sg : this.designUnitSet){
			sg.setGlobals(this.designUnitSet);
		}
		
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
     * @param level
     * @param o
     */
    public static void prDbg(int level, Object o) {
//        if (level >= AbstractStateGraph.PRINT_LEVEL) {
//            AbstractStateGraph.out.println(o);
//        }
    }
    
    public void findLocalSG(){
    	if(this.analysis == null){
    		this.analysis = new CompositionalAnalysis(this.designUnitSet);
    	}
    	
		validateInputs();

		if(Options.getParallelFlag()){
			this.analysis.parallelCompositionalFindSG(this.designUnitSet);
		}
		else{
			this.analysis.compositionalFindSG(this.designUnitSet);
		}
		
    	
//    	this.lpnTranRelation.findCompositionalDependencies();
////    	System.out.println(this.lpnTranRelation.case2);
////    	
//    	for(Entry<LPNTran, Set<LPNTran>> en : this.lpnTranRelation.getDependentTrans()){
//    		LPNTran lpnTran = en.getKey();
//    		System.out.print(lpnTran.getFullLabel() + ": ");
//    		for(LPNTran tran : en.getValue()){
//    			System.out.print(tran.getFullLabel() + ", ");
//    		}
//    		System.out.println();
//    	}
    }
    
    public void compositionalAnalysis(){
    	if(this.analysis == null){
    		this.analysis = new CompositionalAnalysis(this.designUnitSet);
    	}
    	
		validateInputs();
    	this.analysis.compositionalAnalsysis(designUnitSet);
    }
    
    /**
     * Validates each lpn's input variables are driven by another lpn's output.
     */
    private void validateInputs(){
    	boolean error = false;
    	for(LPN lpn : designUnitSet){
	        for(String input : lpn.getInputs()){
	        	boolean connected = false;
	        	for(LPN lpn2 : designUnitSet){
	        		if(lpn == lpn2) continue;
	        		
	        		if(lpn2.getOutputs().contains(input)){
	        			connected = true;
	        			break;
	        		}
	        	}
	        	
	        	if(!connected){
	        		error = true;
	        		System.err.println("error in lpn " + lpn.getLabel() + ": input variable '" + input + "' is not dependent on an output");
	        	}
	        }
    	}
    	
        if(error){
        	System.exit(1);
        }
    }
}
