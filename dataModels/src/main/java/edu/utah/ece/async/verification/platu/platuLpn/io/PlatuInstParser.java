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
// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g 2011-09-05 15:36:30

    package edu.utah.ece.async.verification.platu.platuLpn.io;
    
    import java.util.StringTokenizer;
import java.io.File;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.antlr.runtime.*;

import edu.utah.ece.async.verification.platu.expression.*;
import edu.utah.ece.async.verification.platu.platuLpn.*;
import edu.utah.ece.async.verification.platu.platuLpn.io.Instance;
import edu.utah.ece.async.verification.platu.project.Project;

import java.util.List;
import java.util.ArrayList;

public class PlatuInstParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PATH", "ID", "INT", "MEMBER", "LPAREN", "RPAREN", "TRUE", "FALSE", "QMARK", "COLON", "SEMICOLON", "PERIOD", "UNDERSCORE", "COMMA", "QUOTE", "MODULE", "NAME", "INPUT", "OUTPUT", "INTERNAL", "MARKING", "STATE_VECTOR", "TRANSITION", "LABEL", "PRESET", "POSTSET", "PLUS", "MINUS", "TIMES", "DIV", "MOD", "EQUALS", "GREATER", "LESS", "GREATER_EQUAL", "LESS_EQUAL", "EQUIV", "NOT_EQUIV", "NEGATION", "AND", "OR", "IMPLICATION", "BITWISE_NEGATION", "BITWISE_AND", "BITWISE_OR", "BITWISE_XOR", "BITWISE_LSHIFT", "BITWISE_RSHIFT", "LETTER", "DIGIT", "FILE", "WS", "COMMENT", "MULTILINECOMMENT", "XMLCOMMENT", "IGNORE", "'include'", "'/include'", "'main'", "'/mod'", "'process'", "'/process'", "'class'", "'arg'", "'['", "']'", "'/class'", "'const'", "'/const'", "'/var'", "'{'", "'}'", "'/marking'", "'/transition'", "'assert'", "'condition'", "'delay'", "'inf'"
    };
    public static final int EOF=-1;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int T__73=73;
    public static final int T__74=74;
    public static final int T__75=75;
    public static final int T__76=76;
    public static final int T__77=77;
    public static final int T__78=78;
    public static final int T__79=79;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int PATH=4;
    public static final int ID=5;
    public static final int INT=6;
    public static final int MEMBER=7;
    public static final int LPAREN=8;
    public static final int RPAREN=9;
    public static final int TRUE=10;
    public static final int FALSE=11;
    public static final int QMARK=12;
    public static final int COLON=13;
    public static final int SEMICOLON=14;
    public static final int PERIOD=15;
    public static final int UNDERSCORE=16;
    public static final int COMMA=17;
    public static final int QUOTE=18;
    public static final int MODULE=19;
    public static final int NAME=20;
    public static final int INPUT=21;
    public static final int OUTPUT=22;
    public static final int INTERNAL=23;
    public static final int MARKING=24;
    public static final int STATE_VECTOR=25;
    public static final int TRANSITION=26;
    public static final int LABEL=27;
    public static final int PRESET=28;
    public static final int POSTSET=29;
    public static final int PLUS=30;
    public static final int MINUS=31;
    public static final int TIMES=32;
    public static final int DIV=33;
    public static final int MOD=34;
    public static final int EQUALS=35;
    public static final int GREATER=36;
    public static final int LESS=37;
    public static final int GREATER_EQUAL=38;
    public static final int LESS_EQUAL=39;
    public static final int EQUIV=40;
    public static final int NOT_EQUIV=41;
    public static final int NEGATION=42;
    public static final int AND=43;
    public static final int OR=44;
    public static final int IMPLICATION=45;
    public static final int BITWISE_NEGATION=46;
    public static final int BITWISE_AND=47;
    public static final int BITWISE_OR=48;
    public static final int BITWISE_XOR=49;
    public static final int BITWISE_LSHIFT=50;
    public static final int BITWISE_RSHIFT=51;
    public static final int LETTER=52;
    public static final int DIGIT=53;
    public static final int FILE=54;
    public static final int WS=55;
    public static final int COMMENT=56;
    public static final int MULTILINECOMMENT=57;
    public static final int XMLCOMMENT=58;
    public static final int IGNORE=59;

    // delegates
    // delegators


        public PlatuInstParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PlatuInstParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    @Override
	public String[] getTokenNames() { return PlatuInstParser.tokenNames; }
    @Override
	public String getGrammarFileName() { return "/Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g"; }


    	// static variables
        static private int INFINITY = Integer.MAX_VALUE;
        static private boolean main = false;  // true if main module has been parsed
        static private ExpressionNode ZERO = new ConstNode("FALSE", 0);  // constant false node
        static private ExpressionNode ONE = new ConstNode("TRUE", 1);  // constant true node
        static private Expression TrueExpr = new Expression(ONE); // constant true expression
        static public HashMap<String, PlatuLPN> LpnMap = new HashMap<String, PlatuLPN>();  // all modules parsed, keyed by module name
        static private HashMap<String, Integer> GlobalVarHashMap = new HashMap<String, Integer>();  // global variables and associated values
        static private HashMap<String, VarNode> GlobalVarNodeMap = new HashMap<String, VarNode>();
        static public List<Instance> InstanceList = new ArrayList<Instance>();
        static public HashSet<String> includeSet = new HashSet<String>();
        
        // non-static variables
        private HashMap<String, VarNode> VarNodeMap = new HashMap<String, VarNode>(); // maps variable name to variable object
    	private DualHashMap<String, Integer> VarIndexMap = new DualHashMap<String, Integer>();  // maps variables to an array index
        private HashMap<String, Integer> GlobalConstHashMap = new HashMap<String, Integer>();  // global constants within a single lpn file
        private HashMap<String, Integer> ConstHashMap = new HashMap<String, Integer>();  // constants within a single module
        private HashMap<String, Integer> StatevectorMap = new HashMap<String, Integer>();  // module variables mapped to initial values
        private HashMap<String, Integer> VarCountMap = new HashMap<String, Integer>(); // count of the references to each module variable
        private List<LPNTran> inputTranList = null;  // list of lpn transitions which affect a modules input
        private List<LPNTran> outputTranList = null; // list of lpn transitions which affect a modules output
        private List<String> argumentList = null; // list of class arguments
        private VarSet Inputs = null;  // module inputs
        private VarSet Internals = null; // module internal variables
        private VarSet Outputs = null;  // module outputs
        private int VariableIndex = 0;  // count of index assigned to module variables
        private int TransitionIndex = 0; // count of index assigned to lpn transitions
        private int GlobalCount = 0;  // number of global variables defined in this lpn file
        private int GlobalSize = 0;  // number of global variables defined
        
        // methods
    	private static void error(String error){
    		System.err.println(error);
    		System.exit(1);
    	}
    	
    	private void createGlobalArray(String var, List<Integer> dimensionList){
    		int iter = dimensionList.size() - 1;
    		int dIndex = 0;
    		int arraySize = dimensionList.get(dIndex++);
    		int lastSize = 0;
    		List<Object> topLevelArray = new ArrayList<Object>(arraySize);
    		
    		Queue<List<Object>> arrayQueue = new LinkedList<List<Object>>();
    		arrayQueue.offer(topLevelArray);
    				
    		while(iter > 0){
    			lastSize = arraySize;
    			arraySize = dimensionList.get(dIndex++);
    			int qSize = arrayQueue.size();
    			for(int i = 0; i < qSize; i++){
    				List<Object> array = arrayQueue.poll();
    				for(int j = 0 ; j < lastSize; j++){
    					List<Object> newArray = new ArrayList<Object>(arraySize);
    					array.add(j, newArray);
    					arrayQueue.offer(newArray);
    				}
    			}
    			
    			iter--;
    		}
    		
    		int varCount = 0;
    		dIndex--;
    		arraySize = dimensionList.get(dIndex);
    		
    		List<VarNode> varList = new ArrayList<VarNode>();
    		while(!arrayQueue.isEmpty()){
    			List<Object> array = arrayQueue.poll();
    			for(int i = 0; i < arraySize; i++){
    				String name = var + "." + varCount;
    				varCount++;
    				
    				int index = VariableIndex++;
    				VarNode element = new VarNode(name, index);
    				element.setType(VarType.GLOBAL);
    				array.add(i, element);
    				varList.add(element);
    				
    				// add variable and value to state vector
    				StatevectorMap.put(name, 0);
    		
    				// generate variable index and create new var node  
       				VarIndexMap.insert(name, index);
       				VarNodeMap.put(name, element);
    			}
    		}

    		ArrayNode newArray = new ArrayNode(var, topLevelArray, dimensionList.size(), dimensionList, varList);
    		newArray.setType(VarType.GLOBAL);
    		VarNodeMap.put(var, newArray);
    //  		VarCountMap.put(var, 0);
    //		Inputs.add(var);
    		Outputs.add(var);
    	}
    	
    	private void createInputArray(String var, List<Integer> dimensionList){
    		int iter = dimensionList.size() - 1;
    		int dIndex = 0;
    		int arraySize = dimensionList.get(dIndex++);
    		int lastSize = 0;
    		List<Object> topLevelArray = new ArrayList<Object>(arraySize);
    		
    		Queue<List<Object>> arrayQueue = new LinkedList<List<Object>>();
    		arrayQueue.offer(topLevelArray);
    				
    		while(iter > 0){
    			lastSize = arraySize;
    			arraySize = dimensionList.get(dIndex++);
    			int qSize = arrayQueue.size();
    			for(int i = 0; i < qSize; i++){
    				List<Object> array = arrayQueue.poll();
    				for(int j = 0 ; j < lastSize; j++){
    					List<Object> newArray = new ArrayList<Object>(arraySize);
    					array.add(j, newArray);
    					arrayQueue.offer(newArray);
    				}
    			}
    			
    			iter--;
    		}
    		
    		int varCount = 0;
    		dIndex--;
    		arraySize = dimensionList.get(dIndex);
    		
    		List<VarNode> varList = new ArrayList<VarNode>();
    		while(!arrayQueue.isEmpty()){
    			List<Object> array = arrayQueue.poll();
    			for(int i = 0; i < arraySize; i++){
    				String name = var + "." + varCount;
    				varCount++;
    				
    				int index = VariableIndex++;
    				VarNode element = new VarNode(name, index);
    				element.setType(VarType.INPUT);
    				array.add(i, element);
    				varList.add(element);
    				
    				// add variable and value to state vector
    				StatevectorMap.put(name, 0);
    		
    				// generate variable index and create new var node  
       				VarIndexMap.insert(name, index);
       				VarNodeMap.put(name, element);
    			}
    		}

    		ArrayNode newArray = new ArrayNode(var, topLevelArray, dimensionList.size(), dimensionList, varList);
    		newArray.setType(VarType.INPUT);
    		VarNodeMap.put(var, newArray);
    //  		VarCountMap.put(var, 0);
    		Inputs.add(var);
    		argumentList.add(var);
    	}



    // $ANTLR start "parseLpnFile"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:201:1: parseLpnFile[Project prj] : ( include )? ( globalConstants )? ( globalVariables )? ( main[prj] )? ( moduleClass[prj] )* EOF ;
    public final void parseLpnFile(Project prj) {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:5: ( ( include )? ( globalConstants )? ( globalVariables )? ( main[prj] )? ( moduleClass[prj] )* EOF )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:7: ( include )? ( globalConstants )? ( globalVariables )? ( main[prj] )? ( moduleClass[prj] )* EOF
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:7: ( include )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==LESS) ) {
                int LA1_1 = input.LA(2);

                if ( (LA1_1==60) ) {
                    alt1=1;
                }
            }
            switch (alt1) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:7: include
                    {
                    pushFollow(FOLLOW_include_in_parseLpnFile52);
                    include();

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:16: ( globalConstants )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==LESS) ) {
                int LA2_1 = input.LA(2);

                if ( (LA2_1==71) ) {
                    alt2=1;
                }
            }
            switch (alt2) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:16: globalConstants
                    {
                    pushFollow(FOLLOW_globalConstants_in_parseLpnFile55);
                    globalConstants();

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:33: ( globalVariables )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==LESS) ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==INTERNAL) ) {
                    alt3=1;
                }
            }
            switch (alt3) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:33: globalVariables
                    {
                    pushFollow(FOLLOW_globalVariables_in_parseLpnFile58);
                    globalVariables();

                    state._fsp--;


                    }
                    break;

            }


                    		// check that global constants are consistently defined in each lpn file
                    		if(GlobalSize > 0 && GlobalCount != GlobalSize){
                    			System.err.println("error: global variable definitions are inconsistent");
                    			System.exit(1);
                    		}
                    	
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:210:8: ( main[prj] )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==LESS) ) {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==MODULE) ) {
                    alt4=1;
                }
            }
            switch (alt4) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:210:8: main[prj]
                    {
                    pushFollow(FOLLOW_main_in_parseLpnFile82);
                    main();

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:210:19: ( moduleClass[prj] )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==LESS) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:210:20: moduleClass[prj]
            	    {
            	    pushFollow(FOLLOW_moduleClass_in_parseLpnFile87);
            	    moduleClass(prj);

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_parseLpnFile92); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "parseLpnFile"


    // $ANTLR start "include"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:213:1: include : '<' 'include' '>' ( PATH ';' )+ '<' '/include' '>' ;
    public final void include() {
        Token PATH1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:214:2: ( '<' 'include' '>' ( PATH ';' )+ '<' '/include' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:214:4: '<' 'include' '>' ( PATH ';' )+ '<' '/include' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_include110); 
            match(input,60,FOLLOW_60_in_include112); 
            match(input,GREATER,FOLLOW_GREATER_in_include114); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:214:22: ( PATH ';' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==PATH) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:214:23: PATH ';'
            	    {
            	    PATH1=(Token)match(input,PATH,FOLLOW_PATH_in_include117); 
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_include119); 

            	    				File f = new File((PATH1!=null?PATH1.getText():null));
            	            		includeSet.add(f.getAbsolutePath());
            	    			

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_include131); 
            match(input,61,FOLLOW_61_in_include133); 
            match(input,GREATER,FOLLOW_GREATER_in_include135); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "include"


    // $ANTLR start "main"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:222:1: main[Project prj] : '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( variables )? ( constants )? instantiation '<' '/mod' '>' ;
    public final void main() {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:223:2: ( '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( variables )? ( constants )? instantiation '<' '/mod' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:223:4: '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( variables )? ( constants )? instantiation '<' '/mod' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_main148); 
            match(input,MODULE,FOLLOW_MODULE_in_main150); 
            match(input,NAME,FOLLOW_NAME_in_main152); 
            match(input,EQUALS,FOLLOW_EQUALS_in_main154); 
            match(input,QUOTE,FOLLOW_QUOTE_in_main156); 
            match(input,62,FOLLOW_62_in_main158); 
            match(input,QUOTE,FOLLOW_QUOTE_in_main160); 
            match(input,GREATER,FOLLOW_GREATER_in_main162); 

            				if(main == true){
            					System.err.println("error: multiple main modules");
            					System.exit(1);
            				}
            				
            				main = true;

                			// initialize non static variables for new module
                    	    VarIndexMap = new DualHashMap<String, Integer>();
            			    ConstHashMap = new HashMap<String, Integer>();
            			    VarNodeMap = new HashMap<String, VarNode>();
            			    VarCountMap = new HashMap<String, Integer>();
            			    Inputs = new VarSet();
            			    Internals = new VarSet();
            			    Outputs = new VarSet();
            			    inputTranList = new LinkedList<LPNTran>();
            			    outputTranList = new LinkedList<LPNTran>();
            			    argumentList = new ArrayList<String>();
            			    StatevectorMap = new HashMap<String, Integer>();
            			    VariableIndex = 0;
            			    
            			    // add global variables to initial state vector and label as an input & output
            				for(Entry<String, Integer> e : GlobalVarHashMap.entrySet()){
            					String globalVar = e.getKey();
            					if(GlobalVarNodeMap.containsKey(globalVar)) continue;
            					
            					StatevectorMap.put(globalVar, e.getValue());
            					int index = VariableIndex++;
            	    			VarIndexMap.insert(globalVar, index);
            	    			
            	    			VarNode globalVarNode = new VarNode(globalVar, index);
            	    			globalVarNode.setType(VarType.GLOBAL);
            	    			VarNodeMap.put(globalVar, globalVarNode);
            //	    			Inputs.add(globalVar);
            	    			Outputs.add(globalVar);
            	    		}
            	    		
            	    		// add global arrays
            	    		for(VarNode node : GlobalVarNodeMap.values()){
            	    			ArrayNode arrayNode = (ArrayNode) node;

            	    			// construct array
            					createGlobalArray(arrayNode.getName(), arrayNode.getDimensionList());
            	    		}
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:270:3: ( variables )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==LESS) ) {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==INTERNAL) ) {
                    alt7=1;
                }
            }
            switch (alt7) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:270:3: variables
                    {
                    pushFollow(FOLLOW_variables_in_main171);
                    variables();

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:270:14: ( constants )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==LESS) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:270:14: constants
                    {
                    pushFollow(FOLLOW_constants_in_main174);
                    constants();

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_instantiation_in_main177);
            instantiation();

            state._fsp--;

            match(input,LESS,FOLLOW_LESS_in_main179); 
            match(input,63,FOLLOW_63_in_main181); 
            match(input,GREATER,FOLLOW_GREATER_in_main183); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "main"


    // $ANTLR start "process"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:273:1: process : '<' 'process' 'name' '=' '\"' processName= ID '\"' '>' '<' '/process' '>' ;
    public final void process() {
        //Token processName=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:274:2: ( '<' 'process' 'name' '=' '\"' processName= ID '\"' '>' '<' '/process' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:274:4: '<' 'process' 'name' '=' '\"' processName= ID '\"' '>' '<' '/process' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_process198); 
            match(input,64,FOLLOW_64_in_process200); 
            match(input,NAME,FOLLOW_NAME_in_process202); 
            match(input,EQUALS,FOLLOW_EQUALS_in_process204); 
            match(input,QUOTE,FOLLOW_QUOTE_in_process206); 
            //processName=(Token)
            match(input,ID,FOLLOW_ID_in_process210); 
            match(input,QUOTE,FOLLOW_QUOTE_in_process212); 
            match(input,GREATER,FOLLOW_GREATER_in_process214); 
            match(input,LESS,FOLLOW_LESS_in_process216); 
            match(input,65,FOLLOW_65_in_process218); 
            match(input,GREATER,FOLLOW_GREATER_in_process220); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "process"


    // $ANTLR start "moduleClass"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:278:1: moduleClass[Project prj] returns [LPN lpn] : ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' ) ;
    public final PlatuLPN moduleClass(Project prj) {
        PlatuLPN lpn = null;

        Token modName=null;
        Token arrayArg2=null;
        Token arg2=null;
        Token arrayArg=null;
        Token arg=null;
        PlatuInstParser.expression_return arrayExpr2 = null;

        PlatuInstParser.expression_return arrayExpr = null;

        PlatuInstParser.logic_return logic2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:279:5: ( ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' ) )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:279:7: ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' )
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:279:7: ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:279:9: '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_moduleClass243); 
            match(input,66,FOLLOW_66_in_moduleClass245); 
            match(input,NAME,FOLLOW_NAME_in_moduleClass247); 
            match(input,EQUALS,FOLLOW_EQUALS_in_moduleClass249); 
            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass251); 
            modName=(Token)match(input,ID,FOLLOW_ID_in_moduleClass255); 

                			// module names must be unique
                			if(LpnMap.containsKey((modName!=null?modName.getText():null))){
                				System.err.println("error on line " + (modName!=null?modName.getLine():null) + ": module " + (modName!=null?modName.getText():null) + " already exists");
                				System.exit(1);
                			}
                			
                			if(modName!=null && modName.getText().equals("main")){
                				error("error on line " + modName.getLine() + ": main is reserved");
                			}
                			
                			// initialize non static variables for new module
                    	    VarIndexMap = new DualHashMap<String, Integer>();
            			    ConstHashMap = new HashMap<String, Integer>();
            			    VarNodeMap = new HashMap<String, VarNode>();
            			    VarCountMap = new HashMap<String, Integer>();
            			    Inputs = new VarSet();
            			    Internals = new VarSet();
            			    Outputs = new VarSet();
            			    inputTranList = new LinkedList<LPNTran>();
            			    outputTranList = new LinkedList<LPNTran>();
            			    argumentList = new ArrayList<String>();
            			    StatevectorMap = new HashMap<String, Integer>();
            			    VariableIndex = 0;
            			    
            			    // add global variables to initial state vector and label as an input & output
            				for(Entry<String, Integer> e : GlobalVarHashMap.entrySet()){
            					String globalVar = e.getKey();
            					if(GlobalVarNodeMap.containsKey(globalVar)) continue;
            					
            					StatevectorMap.put(globalVar, e.getValue());
            					int index = VariableIndex++;
            	    			VarIndexMap.insert(globalVar, index);
            	    			
            	    			VarNode globalVarNode = new VarNode(globalVar, index);
            	    			globalVarNode.setType(VarType.GLOBAL);
            	    			VarNodeMap.put(globalVar, globalVarNode);
            //	    			Inputs.add(globalVar);
            	    			Outputs.add(globalVar);
            	    		}
            	    		
            	    		// add global arrays
            	    		for(VarNode node : GlobalVarNodeMap.values()){
            	    			ArrayNode arrayNode = (ArrayNode) node;

            	    			// construct array
            					createGlobalArray(arrayNode.getName(), arrayNode.getDimensionList());
            	    		}
                		
            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass270); 
            match(input,67,FOLLOW_67_in_moduleClass272); 
            match(input,EQUALS,FOLLOW_EQUALS_in_moduleClass274); 
            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass276); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:329:24: ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==ID) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:329:25: ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )*
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:329:25: ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) )
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==ID) ) {
                        int LA10_1 = input.LA(2);

                        if ( (LA10_1==68) ) {
                            alt10=1;
                        }
                        else if ( ((LA10_1>=COMMA && LA10_1<=QUOTE)) ) {
                            alt10=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 10, 1, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 0, input);

                        throw nvae;
                    }
                    switch (alt10) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:329:26: (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ )
                            {
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:329:26: (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ )
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:329:27: arrayArg2= ID ( '[' arrayExpr2= expression ']' )+
                            {
                            arrayArg2=(Token)match(input,ID,FOLLOW_ID_in_moduleClass283); 

                                			// check aginst globals and other inputs
                            	   			if(GlobalConstHashMap.containsKey((arrayArg2!=null?arrayArg2.getText():null))){
                            	   				System.err.println("error on line " + (arrayArg2!=null?arrayArg2.getLine():null) + ": variable " + (arrayArg2!=null?arrayArg2.getText():null) + " is already defined as a global constant");
                            	   				System.exit(1);
                            	   			}
                            	   			else if(GlobalVarHashMap.containsKey((arrayArg2!=null?arrayArg2.getText():null))){
                            	   				System.err.println("error on line " + (arrayArg2!=null?arrayArg2.getLine():null) + ": variable " + (arrayArg2!=null?arrayArg2.getText():null) + " is already defined as a global variable");
                            	   				System.exit(1);
                            	   			}
                            	   			else if(VarNodeMap.containsKey((arrayArg2!=null?arrayArg2.getText():null))){
                            	   				System.err.println("error on line " + (arrayArg2!=null?arrayArg2.getLine():null) + ": variable " + (arrayArg2!=null?arrayArg2.getText():null) + " is already defined");
                            	   				System.exit(1);
                            	   			}
                            	   			
                            	   			List<Integer> dimensionList = new ArrayList<Integer>();
                                		
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:347:6: ( '[' arrayExpr2= expression ']' )+
                            int cnt9=0;
                            loop9:
                            do {
                                int alt9=2;
                                int LA9_0 = input.LA(1);

                                if ( (LA9_0==68) ) {
                                    alt9=1;
                                }


                                switch (alt9) {
                            	case 1 :
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:347:7: '[' arrayExpr2= expression ']'
                            	    {
                            	    match(input,68,FOLLOW_68_in_moduleClass300); 
                            	    pushFollow(FOLLOW_expression_in_moduleClass304);
                            	    arrayExpr2=expression();

                            	    state._fsp--;


                            	        			dimensionList.add((arrayExpr2!=null?arrayExpr2.value:0));
                            	        		
                            	    match(input,69,FOLLOW_69_in_moduleClass320); 

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt9 >= 1 ) break loop9;
                                        EarlyExitException eee =
                                            new EarlyExitException(9, input);
                                        throw eee;
                                }
                                cnt9++;
                            } while (true);


                                			createInputArray((arrayArg2!=null?arrayArg2.getText():null), dimensionList);
                                		

                            }


                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:355:10: (arg2= ID )
                            {
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:355:10: (arg2= ID )
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:355:11: arg2= ID
                            {
                            arg2=(Token)match(input,ID,FOLLOW_ID_in_moduleClass344); 
                            if (arg2==null) {
                            	return null;
                            }
                            // check against globals
                            else if(GlobalConstHashMap.containsKey(arg2.getText())){
                            	System.err.println("error on line " + arg2.getLine() + ": variable " + arg2.getText() + " is already defined as a global constant");
                            	return null;
                            }
                            else if(GlobalVarHashMap.containsKey(arg2.getText())){
                            	System.err.println("error on line " + arg2.getLine() + ": variable " + arg2.getText() + " is already defined as a global variable");
                            	return null;
                            }

                            // add variable and value to state vector
                            StatevectorMap.put(arg2.getText(), 0);

                            // generate variable index and create new var node  
                            int index = VariableIndex++;
                            VarIndexMap.insert(arg2.getText(), index);

                            VarNode inputVarNode =  new VarNode(arg2.getText(), index);
                            inputVarNode.setType(VarType.INPUT);
                            VarNodeMap.put(arg2.getText(), inputVarNode);
                            //    			VarCountMap.put(arg2.getText(), 0);

                            argumentList.add(arg2.getText());
                            Inputs.add(arg2.getText());


                            }


                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:382:9: ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )*
                    loop12:
                    do {
                        int alt12=3;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==COMMA) ) {
                            int LA12_2 = input.LA(2);

                            if ( (LA12_2==ID) ) {
                                int LA12_3 = input.LA(3);

                                if ( (LA12_3==68) ) {
                                    alt12=1;
                                }
                                else if ( ((LA12_3>=COMMA && LA12_3<=QUOTE)) ) {
                                    alt12=2;
                                }


                            }


                        }


                        switch (alt12) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:382:10: ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:382:10: ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:382:11: ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_moduleClass364); 
                    	    arrayArg=(Token)match(input,ID,FOLLOW_ID_in_moduleClass368); 

                    	    	    		// check aginst globals and other inputs
                    	    	   			if(GlobalConstHashMap.containsKey((arrayArg!=null?arrayArg.getText():null))){
                    	    	   				System.err.println("error on line " + (arrayArg!=null?arrayArg.getLine():null) + ": variable " + (arrayArg!=null?arrayArg.getText():null) + " is already defined as a global constant");
                    	    	   				System.exit(1);
                    	    	   			}
                    	    	   			else if(GlobalVarHashMap.containsKey((arrayArg!=null?arrayArg.getText():null))){
                    	    	   				System.err.println("error on line " + (arrayArg!=null?arrayArg.getLine():null) + ": variable " + (arrayArg!=null?arrayArg.getText():null) + " is already defined as a global variable");
                    	    	   				System.exit(1);
                    	    	   			}
                    	    	   			else if(VarNodeMap.containsKey((arrayArg!=null?arrayArg.getText():null))){
                    	    	   				System.err.println("error on line " + (arrayArg!=null?arrayArg.getLine():null) + ": variable " + (arrayArg!=null?arrayArg.getText():null) + " is already defined");
                    	    	   				System.exit(1);
                    	    	   			}
                    	    	   			
                    	    	   			List<Integer> dimensionList = new ArrayList<Integer>();
                    	    	   		
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:400:6: ( '[' arrayExpr= expression ']' )+
                    	    int cnt11=0;
                    	    loop11:
                    	    do {
                    	        int alt11=2;
                    	        int LA11_0 = input.LA(1);

                    	        if ( (LA11_0==68) ) {
                    	            alt11=1;
                    	        }


                    	        switch (alt11) {
                    	    	case 1 :
                    	    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:400:7: '[' arrayExpr= expression ']'
                    	    	    {
                    	    	    match(input,68,FOLLOW_68_in_moduleClass384); 
                    	    	    pushFollow(FOLLOW_expression_in_moduleClass388);
                    	    	    arrayExpr=expression();

                    	    	    state._fsp--;


                    	    	        			dimensionList.add((arrayExpr!=null?arrayExpr.value:0));
                    	    	        		
                    	    	    match(input,69,FOLLOW_69_in_moduleClass404); 

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    if ( cnt11 >= 1 ) break loop11;
                    	                EarlyExitException eee =
                    	                    new EarlyExitException(11, input);
                    	                throw eee;
                    	        }
                    	        cnt11++;
                    	    } while (true);


                    	        			createInputArray((arrayArg!=null?arrayArg.getText():null), dimensionList);
                    	        		

                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:408:10: ( ',' arg= ID )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:408:10: ( ',' arg= ID )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:408:11: ',' arg= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_moduleClass427); 
                    	    arg=(Token)match(input,ID,FOLLOW_ID_in_moduleClass431); 
                    	    if (arg==null) return null;

                    	        			// check aginst globals and other inputs
                    	        			if(GlobalConstHashMap.containsKey(arg.getText())){
                    	        				System.err.println("error on line " + arg.getLine() + ": variable " + arg.getText() + " is already defined as a global constant");
                    	        				System.exit(1);
                    	        			}
                    	        			else if(GlobalVarHashMap.containsKey(arg.getText())){
                    	        				System.err.println("error on line " + arg.getLine() + ": variable " + arg.getText() + " is already defined as a global variable");
                    	        				System.exit(1);
                    	        			}
                    	        			else if(VarNodeMap.containsKey(arg.getText())){
                    	        				System.err.println("error on line " + arg.getLine() + ": variable " + arg.getText() + " is already defined");
                    	        				System.exit(1);
                    	        			}
                    	        			
                    	        			// add variable and value to state vector
                    	    				StatevectorMap.put(arg.getText(), 0);
                    	    				
                    	    				// generate variable index and create new var node  
                    	    				int index = VariableIndex++;
                    	       				VarIndexMap.insert(arg.getText(), index);
                    	       				
                    	       				VarNode inputVarNode = new VarNode(arg.getText(), index);
                    	       				inputVarNode.setType(VarType.INPUT);
                    	       				VarNodeMap.put(arg.getText(), inputVarNode);
                    	    //    			VarCountMap.put(arg.getText(), 0);
                    	        			
                    	        			argumentList.add(arg.getText());
                    	    				Inputs.add(arg.getText());
                    	        		

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);


                    }
                    break;

            }

            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass454); 
            match(input,GREATER,FOLLOW_GREATER_in_moduleClass456); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:439:21: ( constants )?
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==LESS) ) {
                int LA14_1 = input.LA(2);

                if ( (LA14_1==71) ) {
                    alt14=1;
                }
            }
            switch (alt14) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:439:21: constants
                    {
                    pushFollow(FOLLOW_constants_in_moduleClass458);
                    constants();

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_variables_in_moduleClass461);
            variables();

            state._fsp--;

            pushFollow(FOLLOW_logic_in_moduleClass463);
            logic2=logic();

            state._fsp--;

            match(input,LESS,FOLLOW_LESS_in_moduleClass465); 
            match(input,70,FOLLOW_70_in_moduleClass467); 
            match(input,GREATER,FOLLOW_GREATER_in_moduleClass469); 

            				for(Entry<String, Integer> e : VarCountMap.entrySet()){
            					if(e.getValue() == 0){
            						System.out.println("warning: variable '" + e.getKey() + "' is never assigned");
            					}
            				}
            				
            				// create new lpn
            //				Zone zone;
            //	            if (Main.ZONE_VERSION == 2) {
            //	                zone = new HashedHashedMapZoneImpl();
            //	            } 
            //	            else if (Main.ZONE_VERSION == 3) {
            //	                zone = new HashedHashedMapZoneImpl();
            //	            } 
            //	            else {
            //	                zone = new HashedHashedMapZoneImpl();
            //	           	}
            	            
            	            int[] initialMarking = new int[(logic2!=null?logic2.initMarking:null).size()];
            	            
            	            int i = 0;
            	            for(Integer mark : (logic2!=null?logic2.initMarking:null)){
            	            	initialMarking[i++] = mark;
            	            }
            	            
            				lpn = new PlatuLPN(prj, (modName!=null?modName.getText():null), Inputs, Outputs, Internals, VarNodeMap, (logic2!=null?logic2.lpnTranSet:null), 
            	         			StatevectorMap, initialMarking);
            				
            				lpn.addAllInputTrans(inputTranList);
            				lpn.addAllOutputTrans(outputTranList);
            	            lpn.setVarIndexMap(VarIndexMap);         
            	            (logic2!=null?logic2.lpnTranSet:null).setLPN(lpn);     

            	            LpnMap.put(lpn.getLabel(), lpn);
            	            lpn.setArgumentList(argumentList);
            			

            }


            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return lpn;
    }
    // $ANTLR end "moduleClass"


    // $ANTLR start "constants"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:480:1: constants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' ;
    public final void constants() {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:481:2: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:481:4: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_constants494); 
            match(input,71,FOLLOW_71_in_constants496); 
            match(input,GREATER,FOLLOW_GREATER_in_constants498); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:481:20: (const1= ID '=' val1= INT ';' )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==ID) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:481:21: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_constants503); 
            	    match(input,EQUALS,FOLLOW_EQUALS_in_constants505); 
            	    val1=(Token)match(input,INT,FOLLOW_INT_in_constants509); 

            	    				// make sure constant is not defined as something else
            	    				if(VarNodeMap.containsKey((const1!=null?const1.getText():null))){
            	    					System.err.println("error on line " + (const1!=null?const1.getLine():null) + ": " + (const1!=null?const1.getText():null) + " already exists as a variable"); 
            	    					System.exit(1);
            	    				}
            	    				else if(GlobalConstHashMap.containsKey((const1!=null?const1.getText():null))){
            	    				    System.err.println("error on line " + (const1!=null?const1.getLine():null) + ": " + (const1!=null?const1.getText():null) + " already exists as a global constant");
            	    				    System.exit(1);
            	    				}
            	    				else if(GlobalVarHashMap.containsKey((const1!=null?const1.getText():null))){
            	                		System.err.println("error on line " + (const1!=null?const1.getLine():null) + ": " + (const1!=null?const1.getText():null) + " is already defined as a global variable");
            	                		System.exit(1);
            	                	}
            	    				
            	    				// put will override previous value
            	    				Integer result = ConstHashMap.put((const1!=null?const1.getText():null), Integer.parseInt((val1!=null?val1.getText():null)));
            	    				if(result != null){
            	    					System.err.println("error on line " + (const1!=null?const1.getLine():null) + ": " + (const1!=null?const1.getText():null) + " has already been defined");
            	    					System.exit(1);
            	    				}
            	    			
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_constants520); 

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_constants524); 
            match(input,72,FOLLOW_72_in_constants526); 
            match(input,GREATER,FOLLOW_GREATER_in_constants528); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "constants"


    // $ANTLR start "globalConstants"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:507:1: globalConstants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' ;
    public final void globalConstants() {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:508:5: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:508:9: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalConstants545); 
            match(input,71,FOLLOW_71_in_globalConstants547); 
            match(input,GREATER,FOLLOW_GREATER_in_globalConstants549); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:508:25: (const1= ID '=' val1= INT ';' )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==ID) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:508:26: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_globalConstants554); 
            	    match(input,EQUALS,FOLLOW_EQUALS_in_globalConstants556); 
            	    val1=(Token)match(input,INT,FOLLOW_INT_in_globalConstants560); 

            	                	// make sure constant has not been defined already
            	                	if(GlobalVarHashMap.containsKey((const1!=null?const1.getText():null))){
            	                		System.err.println("error on line " + (const1!=null?const1.getLine():null) + ": " + (const1!=null?const1.getText():null) + " is already defined as a global variable");
            	                		System.exit(1);
            	                	}
            	                	
            	                	// put will override previous value
            	                    Integer result = GlobalConstHashMap.put((const1!=null?const1.getText():null), Integer.parseInt((val1!=null?val1.getText():null)));
            	                    if(result != null){
            	                        System.err.println("error on line " + (const1!=null?const1.getLine():null) + ": " + (const1!=null?const1.getText():null) + " has already been defined");
            	                        System.exit(1);
            	                    }
            	                
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalConstants586); 

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_globalConstants590); 
            match(input,72,FOLLOW_72_in_globalConstants592); 
            match(input,GREATER,FOLLOW_GREATER_in_globalConstants594); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "globalConstants"


    // $ANTLR start "globalVariables"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:526:1: globalVariables : '<' 'var' '>' ( globalVarDecl | globalArrayDecl )+ '<' '/var' '>' ;
    public final void globalVariables() {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:527:2: ( '<' 'var' '>' ( globalVarDecl | globalArrayDecl )+ '<' '/var' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:527:4: '<' 'var' '>' ( globalVarDecl | globalArrayDecl )+ '<' '/var' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalVariables608); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_globalVariables610); 
            match(input,GREATER,FOLLOW_GREATER_in_globalVariables612); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:527:18: ( globalVarDecl | globalArrayDecl )+
            int cnt17=0;
            loop17:
            do {
                int alt17=3;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==ID) ) {
                    int LA17_2 = input.LA(2);

                    if ( (LA17_2==EQUALS) ) {
                        alt17=1;
                    }
                    else if ( (LA17_2==68) ) {
                        alt17=2;
                    }


                }


                switch (alt17) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:527:19: globalVarDecl
            	    {
            	    pushFollow(FOLLOW_globalVarDecl_in_globalVariables615);
            	    globalVarDecl();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:527:35: globalArrayDecl
            	    {
            	    pushFollow(FOLLOW_globalArrayDecl_in_globalVariables619);
            	    globalArrayDecl();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt17 >= 1 ) break loop17;
                        EarlyExitException eee =
                            new EarlyExitException(17, input);
                        throw eee;
                }
                cnt17++;
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_globalVariables623); 
            match(input,73,FOLLOW_73_in_globalVariables625); 
            match(input,GREATER,FOLLOW_GREATER_in_globalVariables627); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "globalVariables"


    // $ANTLR start "globalVarDecl"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:530:1: globalVarDecl : var= ID '=' (val= INT | var2= ID ) ';' ;
    public final void globalVarDecl() {
        Token var=null;
        Token val=null;
        Token var2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:531:2: (var= ID '=' (val= INT | var2= ID ) ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:531:4: var= ID '=' (val= INT | var2= ID ) ';'
            {
            var=(Token)match(input,ID,FOLLOW_ID_in_globalVarDecl640); 
            if (var==null) return;

            				// make sure global variables are consistently defined in each lpn file
            				if(GlobalSize == 0){
            					if(GlobalConstHashMap.containsKey(var.getText())){
            						System.err.println("error on line" + var.getLine() + ": " + var.getText() + "already exists as a constant"); 
            	                    System.exit(1);
            					}
            					else if(GlobalVarHashMap.containsKey(var.getText())){
            						System.err.println("error on line " + var.getLine() + ": " + var.getText() + " has already been defined");
            						System.exit(1);
            					}
            				}
            				else{
            					if(!GlobalVarHashMap.containsKey(var.getText())){
            						System.err.println("error on line " + var.getLine() + ": " + var.getText() + " is inconsistently defined");
            						System.exit(1);
            					}
            				}
            				
            				GlobalCount++;
            			
            match(input,EQUALS,FOLLOW_EQUALS_in_globalVarDecl650); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:553:7: (val= INT | var2= ID )
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==INT) ) {
                alt18=1;
            }
            else if ( (LA18_0==ID) ) {
                alt18=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:553:8: val= INT
                    {
                    val=(Token)match(input,INT,FOLLOW_INT_in_globalVarDecl655); 
                    if (val==null) return;

                    				// make sure global variables are consistently initialized
                    				int value = Integer.parseInt(val.getText());
                    				if(GlobalSize == 0){
                    					GlobalVarHashMap.put(var.getText(), value);
                    				}
                    				else{
                    					int globalVal = GlobalVarHashMap.get(var.getText());
                    					if(globalVal != value){
                    						System.err.println("error on line " + val.getLine() + ": " + var.getText() + " is inconsistently assigned");
                    						System.exit(1);
                    					}
                    				}
                    			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:568:5: var2= ID
                    {
                    var2=(Token)match(input,ID,FOLLOW_ID_in_globalVarDecl669); 

                    				// get value of variable
                    				Integer value = null;
                    				if(GlobalConstHashMap.containsKey((var2!=null?var2.getText():null))){
                    					value = GlobalConstHashMap.get((var2!=null?var2.getText():null));
                    				}
                    				else if(GlobalVarHashMap.containsKey((var2!=null?var2.getText():null))){
                    					System.err.println("error on line " + (var2!=null?var2.getLine():null) + ": global variable " + (var2!=null?var2.getText():null) + " cannot be assigned to global variable " + var.getText()); 
                        				return;
                    				}
                    				else{
                    					System.err.println("error on line " + (var2!=null?var2.getLine():null) + ": " + (var2!=null?var2.getText():null) + " is not defined"); 
                        				return;
                    				}
                    				
                    				// make sure global variable is consitently initialized
                    				if(GlobalSize == 0){
                    					GlobalVarHashMap.put(var.getText(), value);
                    				}
                    				else{
                    					int globalVal = GlobalVarHashMap.get(var.getText());
                    					if(globalVal != value){
                    						System.err.println("error on line " + (var2!=null?var2.getLine():null) + ": " + var.getText() + " is inconsistently assigned");
                    						System.exit(1);
                    					}
                    				}
                    			

                    }
                    break;

            }

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalVarDecl680); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "globalVarDecl"


    // $ANTLR start "globalArrayDecl"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:599:1: globalArrayDecl : arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ ';' ;
    public final void globalArrayDecl() {
        Token arrayVar=null;
        PlatuInstParser.expression_return arrayExpr = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:600:2: (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:600:4: arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ ';'
            {
            arrayVar=(Token)match(input,ID,FOLLOW_ID_in_globalArrayDecl694); 

            				List<Integer> dimensionList = new ArrayList<Integer>();
            				
            				// make sure global variables are consistently defined in each lpn file
            				if(GlobalSize == 0){
            					if(GlobalConstHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
            						System.err.println("error on line" + (arrayVar!=null?arrayVar.getLine():null) + ": " + (arrayVar!=null?arrayVar.getText():null) + "already exists as a constant"); 
            	                    System.exit(1);
            					}
            					else if(GlobalVarHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
            						System.err.println("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": " + (arrayVar!=null?arrayVar.getText():null) + " has already been defined");
            						System.exit(1);
            					}
            				}
            				else{
            					if(!GlobalVarHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
            						System.err.println("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": " + (arrayVar!=null?arrayVar.getText():null) + " is inconsistently defined");
            						System.exit(1);
            					}
            				}
            				
            				GlobalCount++;
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:624:3: ( '[' (arrayExpr= expression ) ']' )+
            int cnt19=0;
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);

                if ( (LA19_0==68) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:624:4: '[' (arrayExpr= expression ) ']'
            	    {
            	    match(input,68,FOLLOW_68_in_globalArrayDecl704); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:624:8: (arrayExpr= expression )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:624:9: arrayExpr= expression
            	    {
            	    pushFollow(FOLLOW_expression_in_globalArrayDecl709);
            	    arrayExpr=expression();

            	    state._fsp--;


            	    				dimensionList.add((arrayExpr!=null?arrayExpr.value:0));
            	    			

            	    }

            	    match(input,69,FOLLOW_69_in_globalArrayDecl721); 

            	    }
            	    break;

            	default :
            	    if ( cnt19 >= 1 ) break loop19;
                        EarlyExitException eee =
                            new EarlyExitException(19, input);
                        throw eee;
                }
                cnt19++;
            } while (true);

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalArrayDecl725); 

            				// make sure global variables are consistently initialized
            				if(GlobalSize == 0){
            					GlobalVarHashMap.put((arrayVar!=null?arrayVar.getText():null), 0);
            					GlobalVarNodeMap.put((arrayVar!=null?arrayVar.getText():null), new ArrayNode((arrayVar!=null?arrayVar.getText():null), null, dimensionList.size(), dimensionList, null));
            				}
            				else{
            					ArrayNode node = (ArrayNode) GlobalVarNodeMap.get((arrayVar!=null?arrayVar.getText():null));
            					if(node.getDimensions() != dimensionList.size()){
            						error("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": " + (arrayVar!=null?arrayVar.getText():null) + " is inconsistently assigned");
            					}
            					
            					List<Integer> dimList = node.getDimensionList();
            					for(int i = 0; i < dimensionList.size(); i++){
            						if(dimList.get(i) != dimensionList.get(i)){
            							error("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": " + (arrayVar!=null?arrayVar.getText():null) + " is inconsistently assigned");
            						}
            					}
            				}
            			

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "globalArrayDecl"


    // $ANTLR start "variables"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:650:1: variables : '<' 'var' '>' ( varDecl | arrayDecl )+ '<' '/var' '>' ;
    public final void variables() {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:651:2: ( '<' 'var' '>' ( varDecl | arrayDecl )+ '<' '/var' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:651:4: '<' 'var' '>' ( varDecl | arrayDecl )+ '<' '/var' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_variables741); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_variables743); 
            match(input,GREATER,FOLLOW_GREATER_in_variables745); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:651:18: ( varDecl | arrayDecl )+
            int cnt20=0;
            loop20:
            do {
                int alt20=3;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==ID) ) {
                    int LA20_2 = input.LA(2);

                    if ( (LA20_2==EQUALS) ) {
                        alt20=1;
                    }
                    else if ( (LA20_2==68) ) {
                        alt20=2;
                    }


                }


                switch (alt20) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:651:20: varDecl
            	    {
            	    pushFollow(FOLLOW_varDecl_in_variables749);
            	    varDecl();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:651:30: arrayDecl
            	    {
            	    pushFollow(FOLLOW_arrayDecl_in_variables753);
            	    arrayDecl();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt20 >= 1 ) break loop20;
                        EarlyExitException eee =
                            new EarlyExitException(20, input);
                        throw eee;
                }
                cnt20++;
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_variables757); 
            match(input,73,FOLLOW_73_in_variables759); 
            match(input,GREATER,FOLLOW_GREATER_in_variables761); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "variables"


    // $ANTLR start "varDecl"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:654:1: varDecl : var= ID '=' (val= INT | var2= ID ) ';' ;
    public final void varDecl() {
        Token var=null;
        Token val=null;
        Token var2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:655:2: (var= ID '=' (val= INT | var2= ID ) ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:655:4: var= ID '=' (val= INT | var2= ID ) ';'
            {
            Integer value = null; Token varNode = null;
            var=(Token)match(input,ID,FOLLOW_ID_in_varDecl778); 

            				// check variable is unique in scope
            				if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
            					System.err.println("error on line " + (var!=null?var.getLine():null) + ": " + (var!=null?var.getText():null) + " is a global constant"); 
                				System.exit(1);
            				}
            				else if(GlobalVarHashMap.containsKey((var!=null?var.getText():null))){
            					System.err.println("error on line " + (var!=null?var.getLine():null) + ": " + (var!=null?var.getText():null) + " is a global variable"); 
                				System.exit(1);
            				}
            				else if(VarNodeMap.containsKey((var!=null?var.getText():null))){
            					System.err.println("error on line " + (var!=null?var.getLine():null) + ": " + (var!=null?var.getText():null) + " has already been defined");
            					System.exit(1);
            				}
            				
            				varNode = var;
            			
            match(input,EQUALS,FOLLOW_EQUALS_in_varDecl788); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:674:7: (val= INT | var2= ID )
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==INT) ) {
                alt21=1;
            }
            else if ( (LA21_0==ID) ) {
                alt21=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:674:8: val= INT
                    {
                    val=(Token)match(input,INT,FOLLOW_INT_in_varDecl793); 

                    				// get variable initial value
                    				value = Integer.parseInt((val!=null?val.getText():null));
                    			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:679:5: var2= ID
                    {
                    var2=(Token)match(input,ID,FOLLOW_ID_in_varDecl807); 

                    				// get variable initial value
                    				if(GlobalConstHashMap.containsKey((var2!=null?var2.getText():null))){
                    					value = GlobalConstHashMap.get((var2!=null?var2.getText():null));
                    				}
                    				else if(GlobalVarHashMap.containsKey((var2!=null?var2.getText():null))){
                    					value = GlobalVarHashMap.get((var2!=null?var2.getText():null));
                    				}
                    				else if(ConstHashMap.containsKey((var2!=null?var2.getText():null))){
                    					value = ConstHashMap.get((var2!=null?var2.getText():null));
                    				}
                    				else if(StatevectorMap.containsKey((var2!=null?var2.getText():null))){ // Should var be allowed to assign a var?
                    					value = StatevectorMap.get((var!=null?var.getText():null));
                    				}
                    				else{
                    					System.err.println("error on line " + (var2!=null?var2.getLine():null) + ": " + (var2!=null?var2.getText():null) + " is not defined or is not compatible"); 
                        				System.exit(1);
                    				}
                    				
                    				varNode = var2;
                    			

                    }
                    break;

            }

            if (varNode != null) {
            	// add variable and value to state vector
            	StatevectorMap.put(varNode.getText(), value);

            	// generate variable index and create new var node  
            	int index = VariableIndex++;
            	VarIndexMap.insert(varNode.getText(), index);

            	VarNode internalVar = new VarNode(varNode.getText(), index);
            	internalVar.setType(VarType.INTERNAL);
            	VarNodeMap.put(varNode.getText(), internalVar);
            	VarCountMap.put(varNode.getText(), 0);

            	Internals.add(varNode.getText());
            }
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_varDecl826); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "varDecl"


    // $ANTLR start "arrayDecl"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:720:1: arrayDecl : var= ID ( '[' (arrayExpr= expression ) ']' )+ ( '=' '{' (val2= INT | var2= ID )+ '}' )? ';' ;
    public final void arrayDecl() {
        Token var=null;
        Token val2=null;
        Token var2=null;
        PlatuInstParser.expression_return arrayExpr = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:721:2: (var= ID ( '[' (arrayExpr= expression ) ']' )+ ( '=' '{' (val2= INT | var2= ID )+ '}' )? ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:721:4: var= ID ( '[' (arrayExpr= expression ) ']' )+ ( '=' '{' (val2= INT | var2= ID )+ '}' )? ';'
            {
            var=(Token)match(input,ID,FOLLOW_ID_in_arrayDecl840); 

            				List<Integer> dimensionList = new ArrayList<Integer>();
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:725:3: ( '[' (arrayExpr= expression ) ']' )+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==68) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:725:4: '[' (arrayExpr= expression ) ']'
            	    {
            	    match(input,68,FOLLOW_68_in_arrayDecl851); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:725:8: (arrayExpr= expression )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:725:9: arrayExpr= expression
            	    {
            	    pushFollow(FOLLOW_expression_in_arrayDecl856);
            	    arrayExpr=expression();

            	    state._fsp--;


            	    				dimensionList.add((arrayExpr!=null?arrayExpr.value:0));
            	    			

            	    }

            	    match(input,69,FOLLOW_69_in_arrayDecl867); 

            	    }
            	    break;

            	default :
            	    if ( cnt22 >= 1 ) break loop22;
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);


            				int iter = dimensionList.size() - 1;
            				int dIndex = 0;
            				int arraySize = dimensionList.get(dIndex++);
            				int lastSize = 0;
            				List<Object> topLevelArray = new ArrayList<Object>(arraySize);
            				
            				Queue<List<Object>> arrayQueue = new LinkedList<List<Object>>();
            				arrayQueue.offer(topLevelArray);
            				
            				while(iter > 0){
            					lastSize = arraySize;
            					arraySize = dimensionList.get(dIndex++);
            					int qSize = arrayQueue.size();
            					for(int i = 0; i < qSize; i++){
            						List<Object> array = arrayQueue.poll();
            						for(int j = 0 ; j < lastSize; j++){
            							List<Object> newArray = new ArrayList<Object>(arraySize);
            							array.add(j, newArray);
            							arrayQueue.offer(newArray);
            						}
            					}
            					
            					iter--;
            				}
            				
            				int varCount = 0;
            				dIndex--;
            				arraySize = dimensionList.get(dIndex);
            				
            				List<VarNode> varList = new ArrayList<VarNode>();
            				while(!arrayQueue.isEmpty()){
            					List<Object> array = arrayQueue.poll();
            					for(int i = 0; i < arraySize; i++){
            						String name = (var!=null?var.getText():null) + "." + varCount;
            						varCount++;
            						
            						int index = VariableIndex++;
            						VarNode element = new VarNode(name, index);
            						element.setType(VarType.INTERNAL);
            						array.add(i, element);
            						varList.add(element);
            						
            						// add variable and value to state vector
            						StatevectorMap.put(name, 0);
            				
            						// generate variable index and create new var node  
            		   				VarIndexMap.insert(name, index);
            		   				VarNodeMap.put(name, element);
            					}
            				}

            				ArrayNode newArray = new ArrayNode((var!=null?var.getText():null), topLevelArray, dimensionList.size(), dimensionList, varList);
            				newArray.setType(VarType.INTERNAL);
            				VarNodeMap.put((var!=null?var.getText():null), newArray);
                			VarCountMap.put((var!=null?var.getText():null), 0);
            				Internals.add((var!=null?var.getText():null));
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:788:3: ( '=' '{' (val2= INT | var2= ID )+ '}' )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==EQUALS) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:788:4: '=' '{' (val2= INT | var2= ID )+ '}'
                    {
                    match(input,EQUALS,FOLLOW_EQUALS_in_arrayDecl879); 

                    				List<Integer> valueList = new ArrayList<Integer>();
                    			
                    match(input,74,FOLLOW_74_in_arrayDecl889); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:792:7: (val2= INT | var2= ID )+
                    int cnt23=0;
                    loop23:
                    do {
                        int alt23=3;
                        int LA23_0 = input.LA(1);

                        if ( (LA23_0==INT) ) {
                            alt23=1;
                        }
                        else if ( (LA23_0==ID) ) {
                            alt23=2;
                        }


                        switch (alt23) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:792:8: val2= INT
                    	    {
                    	    val2=(Token)match(input,INT,FOLLOW_INT_in_arrayDecl894); 
                    	    if (val2==null) return;

                    	    				Integer dimVal = Integer.parseInt(val2.getText());
                    	    				if(dimVal < 1){
                    	    					error("error on line " + val2.getLine() + ": invalid dimension");
                    	    				}
                    	    				
                    	    				valueList.add(dimVal);
                    	    			

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:801:5: var2= ID
                    	    {
                    	    var2=(Token)match(input,ID,FOLLOW_ID_in_arrayDecl908); 

                    	    				Integer initVal = null;
                    	    				
                    	    				// get variable initial value
                    	    				if(GlobalConstHashMap.containsKey((var2!=null?var2.getText():null))){
                    	    					initVal = GlobalConstHashMap.get((var2!=null?var2.getText():null));
                    	    				}
                    	    				else if(GlobalVarHashMap.containsKey((var2!=null?var2.getText():null))){
                    	    					initVal = GlobalVarHashMap.get((var2!=null?var2.getText():null));
                    	    				}
                    	    				else if(ConstHashMap.containsKey((var2!=null?var2.getText():null))){
                    	    					initVal = ConstHashMap.get((var2!=null?var2.getText():null));
                    	    				}
                    	    				else if(StatevectorMap.containsKey((var2!=null?var2.getText():null))){ // Should var be allowed to assign a var?
                    	    					initVal = StatevectorMap.get((var2!=null?var2.getText():null));
                    	    				}
                    	    				else{
                    	    					System.err.println("error on line " + (var2!=null?var2.getLine():null) + ": " + (var2!=null?var2.getText():null) + " is not defined"); 
                    	        				return;
                    	    				}
                    	    				
                    	    				if(initVal < 1){
                    	    					error("error on line " + (var2!=null?var2.getLine():null) + ": invalid dimension");
                    	    				}
                    	    				
                    	    				valueList.add(initVal);
                    	    			

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt23 >= 1 ) break loop23;
                                EarlyExitException eee =
                                    new EarlyExitException(23, input);
                                throw eee;
                        }
                        cnt23++;
                    } while (true);

                    match(input,75,FOLLOW_75_in_arrayDecl920); 

                    				//TODO: (original) initialize array
                    				//int dimensions = dimensionList.size();
                    			

                    }
                    break;

            }

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_arrayDecl932); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "arrayDecl"


    // $ANTLR start "instantiation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:837:1: instantiation : (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+ ;
    public final void instantiation() {
        Token modName=null;
        Token instName=null;
        Token var=null;
        Token var2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:838:5: ( (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+ )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:838:7: (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:838:7: (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+
            int cnt27=0;
            loop27:
            do {
                int alt27=2;
                int LA27_0 = input.LA(1);

                if ( (LA27_0==ID) ) {
                    alt27=1;
                }


                switch (alt27) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:838:8: modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';'
            	    {
            	    modName=(Token)match(input,ID,FOLLOW_ID_in_instantiation950); 
            	    instName=(Token)match(input,ID,FOLLOW_ID_in_instantiation954); 

            	        			List<String> argList = new ArrayList<String>();
            	        			List<String> modList = new ArrayList<String>();
            	        		
            	    match(input,LPAREN,FOLLOW_LPAREN_in_instantiation970); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:843:9: ( (var= MEMBER ',' )* var2= MEMBER )?
            	    int alt26=2;
            	    int LA26_0 = input.LA(1);

            	    if ( (LA26_0==MEMBER) ) {
            	        alt26=1;
            	    }
            	    switch (alt26) {
            	        case 1 :
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:843:10: (var= MEMBER ',' )* var2= MEMBER
            	            {
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:843:10: (var= MEMBER ',' )*
            	            loop25:
            	            do {
            	                int alt25=2;
            	                int LA25_0 = input.LA(1);

            	                if ( (LA25_0==MEMBER) ) {
            	                    int LA25_1 = input.LA(2);

            	                    if ( (LA25_1==COMMA) ) {
            	                        alt25=1;
            	                    }


            	                }


            	                switch (alt25) {
            	            	case 1 :
            	            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:843:11: var= MEMBER ','
            	            	    {
            	            	    var=(Token)match(input,MEMBER,FOLLOW_MEMBER_in_instantiation975); 
            	            	    match(input,COMMA,FOLLOW_COMMA_in_instantiation977); 

            	            	        			String buffer = (var!=null?var.getText():null);
            	            	            		StringTokenizer tk = new StringTokenizer(buffer, ".");
            	            	            		
            	            	        			String module = tk.nextToken();
            	            	        			String variable = tk.nextToken();
            	            	        			
            	            	        			modList.add(module);
            	            	        			argList.add(module + "." + variable);
            	            	        		

            	            	    }
            	            	    break;

            	            	default :
            	            	    break loop25;
            	                }
            	            } while (true);

            	            var2=(Token)match(input,MEMBER,FOLLOW_MEMBER_in_instantiation997); 

            	                			String buffer = (var2!=null?var2.getText():null);
            	                    		StringTokenizer tk = new StringTokenizer(buffer, ".");
            	                    		
            	                			String module = tk.nextToken();
            	                			String variable = tk.nextToken();
            	                			
            	                			modList.add(module);
            	                			argList.add(module + "." + variable);	
            	                		

            	            }
            	            break;

            	    }

            	    match(input,RPAREN,FOLLOW_RPAREN_in_instantiation1015); 
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_instantiation1017); 

            	        			Instance inst = new Instance((modName!=null?modName.getText():null), (instName!=null?instName.getText():null), argList, modList);
            	        			InstanceList.add(inst);
            	        		

            	    }
            	    break;

            	default :
            	    if ( cnt27 >= 1 ) break loop27;
                        EarlyExitException eee =
                            new EarlyExitException(27, input);
                        throw eee;
                }
                cnt27++;
            } while (true);


            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "instantiation"

    public static class logic_return extends ParserRuleReturnScope {
        public List<Integer> initMarking;
        public LpnTranList lpnTranSet;
    }

    // $ANTLR start "logic"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:873:1: logic returns [List<Integer> initMarking, LpnTranList lpnTranSet] : marking ( transition )+ ;
    public final PlatuInstParser.logic_return logic() {
        PlatuInstParser.logic_return retval = new PlatuInstParser.logic_return();
        retval.start = input.LT(1);

        LPNTran transition3 = null;

        List<Integer> marking4 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:874:5: ( marking ( transition )+ )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:874:9: marking ( transition )+
            {
            retval.lpnTranSet = new LpnTranList();
            pushFollow(FOLLOW_marking_in_logic1063);
            marking4=marking();

            state._fsp--;

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:875:14: ( transition )+
            int cnt28=0;
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==LESS) ) {
                    int LA28_1 = input.LA(2);

                    if ( (LA28_1==TRANSITION) ) {
                        alt28=1;
                    }


                }


                switch (alt28) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:875:15: transition
            	    {
            	    pushFollow(FOLLOW_transition_in_logic1066);
            	    transition3=transition();

            	    state._fsp--;

            	    retval.lpnTranSet.add(transition3);

            	    }
            	    break;

            	default :
            	    if ( cnt28 >= 1 ) break loop28;
                        EarlyExitException eee =
                            new EarlyExitException(28, input);
                        throw eee;
                }
                cnt28++;
            } while (true);


                        retval.initMarking = marking4;
                    

            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "logic"


    // $ANTLR start "marking"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:881:1: marking returns [List<Integer> mark] : ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )? ;
    public final List<Integer> marking() {
        List<Integer> mark = null;

        Token m1=null;
        Token c1=null;
        Token m2=null;
        Token c2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:882:5: ( ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )? )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:882:9: ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )?
            {
            mark = new LinkedList<Integer>(); Integer result;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:883:9: ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==LESS) ) {
                int LA33_1 = input.LA(2);

                if ( (LA33_1==MARKING) ) {
                    alt33=1;
                }
            }
            switch (alt33) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:883:10: '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>'
                    {
                    match(input,LESS,FOLLOW_LESS_in_marking1119); 
                    match(input,MARKING,FOLLOW_MARKING_in_marking1121); 
                    match(input,GREATER,FOLLOW_GREATER_in_marking1123); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:883:28: ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )?
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( ((LA32_0>=ID && LA32_0<=INT)) ) {
                        alt32=1;
                    }
                    switch (alt32) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:883:29: (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )*
                            {
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:883:29: (m1= INT | c1= ID )
                            int alt29=2;
                            int LA29_0 = input.LA(1);

                            if ( (LA29_0==INT) ) {
                                alt29=1;
                            }
                            else if ( (LA29_0==ID) ) {
                                alt29=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 29, 0, input);

                                throw nvae;
                            }
                            switch (alt29) {
                                case 1 :
                                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:883:30: m1= INT
                                    {
                                    m1=(Token)match(input,INT,FOLLOW_INT_in_marking1129); 

                                            		mark.add(Integer.parseInt((m1!=null?m1.getText():null)));
                                            	

                                    }
                                    break;
                                case 2 :
                                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:887:11: c1= ID
                                    {
                                    c1=(Token)match(input,ID,FOLLOW_ID_in_marking1155); 

                                            		result = ConstHashMap.get((c1!=null?c1.getText():null));
                                            		if(result == null){
                                            			System.err.println("error on line " + (c1!=null?c1.getLine():null) + ": " + (c1!=null?c1.getText():null) + " is not a valid constant");
                                            			System.exit(1);
                                            		}
                                            		
                                            		mark.add(result);
                                            	

                                    }
                                    break;

                            }

                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:897:11: ( ',' (m2= INT | c2= ID ) )*
                            loop31:
                            do {
                                int alt31=2;
                                int LA31_0 = input.LA(1);

                                if ( (LA31_0==COMMA) ) {
                                    alt31=1;
                                }


                                switch (alt31) {
                            	case 1 :
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:897:12: ',' (m2= INT | c2= ID )
                            	    {
                            	    match(input,COMMA,FOLLOW_COMMA_in_marking1179); 
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:897:16: (m2= INT | c2= ID )
                            	    int alt30=2;
                            	    int LA30_0 = input.LA(1);

                            	    if ( (LA30_0==INT) ) {
                            	        alt30=1;
                            	    }
                            	    else if ( (LA30_0==ID) ) {
                            	        alt30=2;
                            	    }
                            	    else {
                            	        NoViableAltException nvae =
                            	            new NoViableAltException("", 30, 0, input);

                            	        throw nvae;
                            	    }
                            	    switch (alt30) {
                            	        case 1 :
                            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:897:17: m2= INT
                            	            {
                            	            m2=(Token)match(input,INT,FOLLOW_INT_in_marking1184); 

                            	                    		mark.add(Integer.parseInt((m2!=null?m2.getText():null)));
                            	                    	

                            	            }
                            	            break;
                            	        case 2 :
                            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:901:11: c2= ID
                            	            {
                            	            c2=(Token)match(input,ID,FOLLOW_ID_in_marking1210); 

                            	                    		result = ConstHashMap.get((c2!=null?c2.getText():null));
                            	                    		if(result == null){
                            	                    			System.err.println("error on line " + (c2!=null?c2.getLine():null) + ": " + (c2!=null?c2.getText():null) + " is not a valid constant");
                            	                    			System.exit(1);
                            	                    		}
                            	                    		
                            	                    		mark.add(result);
                            	                    	

                            	            }
                            	            break;

                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop31;
                                }
                            } while (true);


                            }
                            break;

                    }

                    match(input,LESS,FOLLOW_LESS_in_marking1237); 
                    match(input,76,FOLLOW_76_in_marking1239); 
                    match(input,GREATER,FOLLOW_GREATER_in_marking1241); 

                    }
                    break;

            }


            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return mark;
    }
    // $ANTLR end "marking"


    // $ANTLR start "transition"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:914:1: transition returns [LPNTran lpnTran] : '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/transition' '>' ;
    public final LPNTran transition() {
        LPNTran lpnTran = null;

        Token lbl=null;
        Token pre=null;
        Token pre1=null;
        Token pre2=null;
        Token pre3=null;
        Token post=null;
        Token post1=null;
        Token post2=null;
        Token post3=null;
        Expression guard5 = null;

        PlatuInstParser.delay_return delay6 = null;

        Expression assertion7 = null;

        VarExpr assignment8 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:915:5: ( '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/transition' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:915:10: '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/transition' '>'
            {

            	    		Integer result = null;
            	    		ArrayList<Integer> presetList = new ArrayList<Integer>();  
            	    		ArrayList<Integer> postsetList = new ArrayList<Integer>(); 
            	    		VarExprList assignmentList = new VarExprList();
            	    		ArrayList<Expression> assertionList = new ArrayList<Expression>();
            	    		Expression guardExpr = TrueExpr; 
            	    		int delayLB = 0; 
            	    		int delayUB = INFINITY;
            	    		boolean local = true;
            	    	
            match(input,LESS,FOLLOW_LESS_in_transition1275); 
            match(input,TRANSITION,FOLLOW_TRANSITION_in_transition1277); 
            match(input,LABEL,FOLLOW_LABEL_in_transition1279); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1281); 
            match(input,QUOTE,FOLLOW_QUOTE_in_transition1283); 
            lbl=input.LT(1);
            if ( (input.LA(1)>=ID && input.LA(1)<=INT) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            match(input,QUOTE,FOLLOW_QUOTE_in_transition1293); 
            match(input,PRESET,FOLLOW_PRESET_in_transition1295); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1297); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:926:69: ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==QUOTE) ) {
                int LA36_1 = input.LA(2);

                if ( (LA36_1==QUOTE) ) {
                    alt36=1;
                }
                else if ( ((LA36_1>=ID && LA36_1<=INT)) ) {
                    alt36=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 36, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:926:70: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1300); 
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1302); 

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:926:80: ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:926:80: ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:926:81: '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1307); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:926:85: (pre= INT | pre1= ID )
                    int alt34=2;
                    int LA34_0 = input.LA(1);

                    if ( (LA34_0==INT) ) {
                        alt34=1;
                    }
                    else if ( (LA34_0==ID) ) {
                        alt34=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 34, 0, input);

                        throw nvae;
                    }
                    switch (alt34) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:926:86: pre= INT
                            {
                            pre=(Token)match(input,INT,FOLLOW_INT_in_transition1312); 

                                			presetList.add(Integer.parseInt((pre!=null?pre.getText():null)));
                               			

                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:930:7: pre1= ID
                            {
                            pre1=(Token)match(input,ID,FOLLOW_ID_in_transition1332); 

                              				result = ConstHashMap.get((pre1!=null?pre1.getText():null)); 
                              				if(result == null){
                              					System.err.println("error on line " + (pre1!=null?pre1.getLine():null) + ": " + (pre1!=null?pre1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				
                              				presetList.add(result);
                              			

                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:940:6: ( ',' pre2= INT | ',' pre3= ID )*
                    loop35:
                    do {
                        int alt35=3;
                        int LA35_0 = input.LA(1);

                        if ( (LA35_0==COMMA) ) {
                            int LA35_2 = input.LA(2);

                            if ( (LA35_2==INT) ) {
                                alt35=1;
                            }
                            else if ( (LA35_2==ID) ) {
                                alt35=2;
                            }


                        }


                        switch (alt35) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:940:8: ',' pre2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1348); 
                    	    pre2=(Token)match(input,INT,FOLLOW_INT_in_transition1352); 

                    	        			presetList.add(Integer.parseInt((pre2!=null?pre2.getText():null)));
                    	       			

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:944:7: ',' pre3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1370); 
                    	    pre3=(Token)match(input,ID,FOLLOW_ID_in_transition1374); 

                    	      				result = ConstHashMap.get((pre3!=null?pre3.getText():null)); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + (pre3!=null?pre3.getLine():null) + ": " + (pre3!=null?pre3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}
                    	      				
                    	      				presetList.add(result);
                    	      			

                    	    }
                    	    break;

                    	default :
                    	    break loop35;
                        }
                    } while (true);

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1389); 

                    }


                    }
                    break;

            }

            match(input,POSTSET,FOLLOW_POSTSET_in_transition1393); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1395); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:954:27: ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==QUOTE) ) {
                int LA39_1 = input.LA(2);

                if ( (LA39_1==QUOTE) ) {
                    alt39=1;
                }
                else if ( ((LA39_1>=ID && LA39_1<=INT)) ) {
                    alt39=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 39, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:954:29: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1399); 
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1401); 

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:954:39: ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:954:39: ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:954:40: '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1406); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:954:44: (post= INT | post1= ID )
                    int alt37=2;
                    int LA37_0 = input.LA(1);

                    if ( (LA37_0==INT) ) {
                        alt37=1;
                    }
                    else if ( (LA37_0==ID) ) {
                        alt37=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 37, 0, input);

                        throw nvae;
                    }
                    switch (alt37) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:954:46: post= INT
                            {
                            post=(Token)match(input,INT,FOLLOW_INT_in_transition1412); 

                                			postsetList.add(Integer.parseInt((post!=null?post.getText():null)));
                                		

                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:958:8: post1= ID
                            {
                            post1=(Token)match(input,ID,FOLLOW_ID_in_transition1432); 

                                			result = ConstHashMap.get((post1!=null?post1.getText():null)); 
                              				if(result == null){
                              					System.err.println("error on line " + (post1!=null?post1.getLine():null) + ": " + (post1!=null?post1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				
                              				postsetList.add(result);
                                		

                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:969:6: ( ( ',' post2= INT ) | ( ',' post3= ID ) )*
                    loop38:
                    do {
                        int alt38=3;
                        int LA38_0 = input.LA(1);

                        if ( (LA38_0==COMMA) ) {
                            int LA38_2 = input.LA(2);

                            if ( (LA38_2==INT) ) {
                                alt38=1;
                            }
                            else if ( (LA38_2==ID) ) {
                                alt38=2;
                            }


                        }


                        switch (alt38) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:969:8: ( ',' post2= INT )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:969:8: ( ',' post2= INT )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:969:9: ',' post2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1457); 
                    	    post2=(Token)match(input,INT,FOLLOW_INT_in_transition1461); 

                    	        			postsetList.add(Integer.parseInt((post2!=null?post2.getText():null)));
                    	        		

                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:973:10: ( ',' post3= ID )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:973:10: ( ',' post3= ID )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:973:11: ',' post3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1482); 
                    	    post3=(Token)match(input,ID,FOLLOW_ID_in_transition1485); 

                    	        			result = ConstHashMap.get((post3!=null?post3.getText():null)); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + (post3!=null?post3.getLine():null) + ": " + (post3!=null?post3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}
                    	      				
                    	      				postsetList.add(result);
                    	        		

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop38;
                        }
                    } while (true);

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1504); 

                    }


                    }
                    break;

            }

            match(input,GREATER,FOLLOW_GREATER_in_transition1509); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:983:21: ( guard )?
            int alt40=2;
            int LA40_0 = input.LA(1);

            if ( (LA40_0==79) ) {
                alt40=1;
            }
            switch (alt40) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:983:22: guard
                    {
                    pushFollow(FOLLOW_guard_in_transition1512);
                    guard5=guard();

                    state._fsp--;


                        			guardExpr = guard5;
                        		

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:987:9: ( delay )?
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==80) ) {
                alt41=1;
            }
            switch (alt41) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:987:10: delay
                    {
                    pushFollow(FOLLOW_delay_in_transition1532);
                    delay6=delay();

                    state._fsp--;


                        			delayLB = (delay6!=null?delay6.delayLB:0); 
                        			delayUB = (delay6!=null?delay6.delayUB:0);
                        		

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:992:9: ( ( assertion ) | ( assignment ) )*
            loop42:
            do {
                int alt42=3;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==78) ) {
                    alt42=1;
                }
                else if ( (LA42_0==ID) ) {
                    alt42=2;
                }


                switch (alt42) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:992:10: ( assertion )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:992:10: ( assertion )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:992:11: assertion
            	    {
            	    pushFollow(FOLLOW_assertion_in_transition1553);
            	    assertion7=assertion();

            	    state._fsp--;


            	        			if(assertion7 != null){		
            	      					assertionList.add(assertion7);
            	      				}
            	        		

            	    }


            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:998:10: ( assignment )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:998:10: ( assignment )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:998:11: assignment
            	    {
            	    pushFollow(FOLLOW_assignment_in_transition1573);
            	    assignment8=assignment();

            	    state._fsp--;


            	        			assignmentList.add(assignment8);
            	        		

            	    }


            	    }
            	    break;

            	default :
            	    break loop42;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_transition1593); 
            match(input,77,FOLLOW_77_in_transition1595); 
            match(input,GREATER,FOLLOW_GREATER_in_transition1597); 

                    	// create new lpn transitions and add assertions
                    	lpnTran = new LPNTran((lbl!=null?lbl.getText():null), TransitionIndex++, presetList, postsetList, guardExpr, assignmentList, delayLB, delayUB, local);
                    	if(assertionList.size() > 0){
                    		lpnTran.addAllAssertions(assertionList);
                    	}
                    

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return lpnTran;
    }
    // $ANTLR end "transition"


    // $ANTLR start "assertion"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1012:1: assertion returns [Expression booleanExpr] : 'assert' '(' expression ')' ';' ;
    public final Expression assertion() {
        Expression booleanExpr = null;

        PlatuInstParser.expression_return expression9 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1013:2: ( 'assert' '(' expression ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1013:4: 'assert' '(' expression ')' ';'
            {
            match(input,78,FOLLOW_78_in_assertion1631); 
            match(input,LPAREN,FOLLOW_LPAREN_in_assertion1633); 
            pushFollow(FOLLOW_expression_in_assertion1635);
            expression9=expression();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_assertion1637); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assertion1639); 

            				booleanExpr = new Expression((expression9!=null?expression9.expr:null));
            			

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return booleanExpr;
    }
    // $ANTLR end "assertion"


    // $ANTLR start "guard"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1020:1: guard returns [Expression expr] : 'condition' '(' expression ')' ';' ;
    public final Expression guard() {
        Expression expr = null;

        PlatuInstParser.expression_return expression10 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1021:5: ( 'condition' '(' expression ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1021:8: 'condition' '(' expression ')' ';'
            {
            match(input,79,FOLLOW_79_in_guard1664); 
            match(input,LPAREN,FOLLOW_LPAREN_in_guard1666); 
            pushFollow(FOLLOW_expression_in_guard1668);
            expression10=expression();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_guard1670); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_guard1672); 

               				expr = new Expression((expression10!=null?expression10.expr:null));
                		

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return expr;
    }
    // $ANTLR end "guard"

    public static class delay_return extends ParserRuleReturnScope {
        public int delayLB;
        public int delayUB;
    }

    // $ANTLR start "delay"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1027:1: delay returns [int delayLB, int delayUB] : 'delay' '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' ;
    public final PlatuInstParser.delay_return delay() {
        PlatuInstParser.delay_return retval = new PlatuInstParser.delay_return();
        retval.start = input.LT(1);

        Token lb=null;
        Token ub=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1028:5: ( 'delay' '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1028:8: 'delay' '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';'
            {
            match(input,80,FOLLOW_80_in_delay1706); 
            match(input,LPAREN,FOLLOW_LPAREN_in_delay1708); 
            lb=(Token)match(input,INT,FOLLOW_INT_in_delay1712); 
            if (lb==null) return null;

                			retval.delayLB = Integer.parseInt(lb.getText());
               			
            match(input,COMMA,FOLLOW_COMMA_in_delay1726); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1032:8: (ub= INT | 'inf' )
            int alt43=2;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==INT) ) {
                alt43=1;
            }
            else if ( (LA43_0==81) ) {
                alt43=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1032:9: ub= INT
                    {
                    ub=(Token)match(input,INT,FOLLOW_INT_in_delay1731); 
                    if (ub==null) return null;

                     				retval.delayUB = Integer.parseInt(ub.getText());
                     				// make sure delays are >= 0 and upper bound is >= lower bound
                     				if(retval.delayLB < 0){
                     					System.err.println("error on line " + lb.getLine() + ": lower bound " + retval.delayLB + " must be >= 0");
                      					System.exit(1);
                     				}
                     				else if(retval.delayLB == INFINITY){
                     					System.err.println("error on line " + ub.getLine() + ": lower bound " + retval.delayUB + " must be a non-negative finite number");
                      					System.exit(1);
                     				}
                     				else if(retval.delayUB < retval.delayLB){
                     					System.err.println("error on line " + ub.getLine() + ": upper bound " + retval.delayUB + " < lower bound " + retval.delayLB);
                      					System.exit(1);
                     				} 
                     			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1049:6: 'inf'
                    {
                    match(input,81,FOLLOW_81_in_delay1746); 

                     				retval.delayUB = INFINITY;
                    			

                    }
                    break;

            }

            match(input,RPAREN,FOLLOW_RPAREN_in_delay1759); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_delay1761); 

            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "delay"


    // $ANTLR start "assignment"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1056:1: assignment returns [VarExpr assign] : ( (var1= ID '=' var2= ID ) | (var= ID '=' varExpr= expression ';' ) | (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' ) );
    public final VarExpr assignment() {
        VarExpr assign = null;

        Token var1=null;
        Token var2=null;
        Token var=null;
        Token arrayVar=null;
        PlatuInstParser.expression_return varExpr = null;

        PlatuInstParser.expression_return arrayExpr = null;

        PlatuInstParser.expression_return assignExpr = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1057:5: ( (var1= ID '=' var2= ID ) | (var= ID '=' varExpr= expression ';' ) | (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' ) )
            int alt45=3;
            int LA45_0 = input.LA(1);

            if ( (LA45_0==ID) ) {
                int LA45_1 = input.LA(2);

                if ( (LA45_1==EQUALS) ) {
                    int LA45_2 = input.LA(3);

                    if ( (LA45_2==ID) ) {
                        switch ( input.LA(4) ) {
                        case LESS:
                            {
                            int LA45_6 = input.LA(5);

                            if ( (LA45_6==77) ) {
                                alt45=1;
                            }
                            else if ( ((LA45_6>=ID && LA45_6<=INT)||LA45_6==LPAREN||(LA45_6>=TRUE && LA45_6<=FALSE)||(LA45_6>=PLUS && LA45_6<=MINUS)||LA45_6==NEGATION||LA45_6==BITWISE_NEGATION) ) {
                                alt45=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 45, 6, input);

                                throw nvae;
                            }
                            }
                            break;
                        case ID:
                        case 78:
                            {
                            alt45=1;
                            }
                            break;
                        case QMARK:
                        case SEMICOLON:
                        case PLUS:
                        case MINUS:
                        case TIMES:
                        case DIV:
                        case MOD:
                        case GREATER:
                        case GREATER_EQUAL:
                        case LESS_EQUAL:
                        case EQUIV:
                        case NOT_EQUIV:
                        case AND:
                        case OR:
                        case IMPLICATION:
                        case BITWISE_AND:
                        case BITWISE_OR:
                        case BITWISE_XOR:
                        case BITWISE_LSHIFT:
                        case BITWISE_RSHIFT:
                        case 68:
                            {
                            alt45=2;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 45, 4, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA45_2==INT||LA45_2==LPAREN||(LA45_2>=TRUE && LA45_2<=FALSE)||(LA45_2>=PLUS && LA45_2<=MINUS)||LA45_2==NEGATION||LA45_2==BITWISE_NEGATION) ) {
                        alt45=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 45, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA45_1==68) ) {
                    alt45=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 45, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }
            switch (alt45) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1057:9: (var1= ID '=' var2= ID )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1057:9: (var1= ID '=' var2= ID )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1057:10: var1= ID '=' var2= ID
                    {
                    var1=(Token)match(input,ID,FOLLOW_ID_in_assignment1787); 
                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1789); 
                    var2=(Token)match(input,ID,FOLLOW_ID_in_assignment1793); 

                    }


                        			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey((var1!=null?var1.getText():null))){
                        				System.err.println("error on line " + (var1!=null?var1.getLine():null) + ": global constant " + (var1!=null?var1.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey((var1!=null?var1.getText():null))){
                        				System.err.println("error on line " + (var1!=null?var1.getLine():null) + ": constant " + (var1!=null?var1.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!VarNodeMap.containsKey((var1!=null?var1.getText():null))){
                        				System.err.println("error on line " + (var1!=null?var1.getLine():null) + ": variable " + (var1!=null?var1.getText():null) + " was not declared");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains((var1!=null?var1.getText():null)) && !Internals.contains((var1!=null?var1.getText():null))){
                        				System.err.println("error on line " + (var1!=null?var1.getLine():null) + ": input variable " + (var1!=null?var1.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			
                    				ExpressionNode node2 = null;
                        			if(GlobalConstHashMap.containsKey((var2!=null?var2.getText():null))){
                        				node2 = new ConstNode((var2!=null?var2.getText():null), GlobalConstHashMap.get((var2!=null?var2.getText():null)));
                        			}
                        			else if(ConstHashMap.containsKey((var2!=null?var2.getText():null))){
                        				node2 = new ConstNode((var2!=null?var2.getText():null), ConstHashMap.get((var2!=null?var2.getText():null)));
                        			}
                        			else if(GlobalVarHashMap.containsKey((var2!=null?var2.getText():null))){
                        				node2 = VarNodeMap.get((var2!=null?var2.getText():null));
                        			}
                        			else if(!VarNodeMap.containsKey((var2!=null?var2.getText():null))){
                        				System.err.println("error on line " + (var2!=null?var2.getLine():null) + ": variable " + (var2!=null?var2.getText():null) + " was not declared");
                        				return null;
                        			}
                        			else{
                        				node2 = VarNodeMap.get((var2!=null?var2.getText():null));
                        			}
                    	    		
                    	    		VarNode node1 = VarNodeMap.get((var1!=null?var1.getText():null));
                    	    		if(ArrayNode.class.isAssignableFrom(node1.getClass())){
                    	    			if(!ArrayNode.class.isAssignableFrom(node2.getClass())){
                    	   					System.err.println("error on line " + (var1!=null?var1.getLine():null) + ": variable " + (var1!=null?var1.getText():null) + " is an array");
                    	   					System.exit(1);
                       					}
                       					
                       					ArrayNode arrayNode1 = (ArrayNode) node1;
                       					ArrayNode arrayNode2 = (ArrayNode) node2;
                       					
                       					List<Integer> dimensionList1 = arrayNode1.getDimensionList();
                       					List<Integer> dimensionList2 = arrayNode2.getDimensionList();
                       					
                       					if(dimensionList1.size() != dimensionList2.size()){
                       						System.err.println("error on line " + (var1!=null?var1.getLine():null) + ": incompatible array dimensions");
                       						System.exit(1);
                       					}
                       					
                       					for(int i = 0; i < dimensionList1.size(); i++){
                       						if(dimensionList1.get(i) != dimensionList2.get(i)){
                       							System.err.println("error on line " + (var1!=null?var1.getLine():null) + ": incompatible array dimensions");
                       							System.exit(1);
                       						}
                       					}
                       					
                       					//TODO: (original) array to array assignment
                       					
                       				}else if(ArrayNode.class.isAssignableFrom(node2.getClass())){
                       					System.err.println("error on line " + (var2!=null?var2.getLine():null) + ": variable " + (var2!=null?var2.getText():null) + " is an array");
                       					System.exit(1);
                       				}
                       				else{
                       					// regular assignment
                       					Expression expr = new Expression(node2);
                    	    			assign = new VarExpr(node1, expr);
                       				}
                       				
                       				if(node1.getType() == VarType.INTERNAL || node1.getType() == VarType.OUTPUT){
                    	   				Integer varCount = VarCountMap.get((var1!=null?var1.getText():null));
                    		    		VarCountMap.put((var1!=null?var1.getText():null), ++varCount);
                    	    		}
                        		

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1137:8: (var= ID '=' varExpr= expression ';' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1137:8: (var= ID '=' varExpr= expression ';' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1137:9: var= ID '=' varExpr= expression ';'
                    {
                    var=(Token)match(input,ID,FOLLOW_ID_in_assignment1815); 
                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1817); 
                    	
                        			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + (var!=null?var.getLine():null) + ": global constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + (var!=null?var.getLine():null) + ": constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!VarNodeMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + (var!=null?var.getLine():null) + ": variable " + (var!=null?var.getText():null) + " was not declared");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains((var!=null?var.getText():null)) && !Internals.contains((var!=null?var.getText():null))){
                        				System.err.println("error on line " + (var!=null?var.getLine():null) + ": input variable " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        		
                    pushFollow(FOLLOW_expression_in_assignment1835);
                    varExpr=expression();

                    state._fsp--;


                    	    		Expression expr = new Expression((varExpr!=null?varExpr.expr:null));
                    	    		VarNode node = VarNodeMap.get((var!=null?var.getText():null));
                    	    		if(ArrayNode.class.isAssignableFrom(node.getClass())){
                       					System.err.println("error on line " + (var!=null?var.getLine():null) + ": variable " + (var!=null?var.getText():null) + " is an array");
                       					System.exit(1);
                       				}
                       				
                       				if(node.getType() == VarType.INTERNAL || node.getType() == VarType.OUTPUT){
                    	   				Integer varCount = VarCountMap.get((var!=null?var.getText():null));
                    		    		VarCountMap.put((var!=null?var.getText():null), ++varCount);
                    	    		}
                    	    		
                    	    		assign = new VarExpr(node, expr);
                    	   		
                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1850); 

                    }


                    }
                    break;
                case 3 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1173:13: (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1173:13: (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1173:14: arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';'
                    {
                    arrayVar=(Token)match(input,ID,FOLLOW_ID_in_assignment1858); 

                    	   			List<ExpressionNode> indexList = new ArrayList<ExpressionNode>();
                    	   			
                    	   			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": global constant " + (arrayVar!=null?arrayVar.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": constant " + (arrayVar!=null?arrayVar.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!VarNodeMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": variable " + (arrayVar!=null?arrayVar.getText():null) + " was not declared");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains((arrayVar!=null?arrayVar.getText():null)) && !Internals.contains((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": input variable " + (arrayVar!=null?arrayVar.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                    	   		
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1195:6: ( '[' (arrayExpr= expression ) ']' )+
                    int cnt44=0;
                    loop44:
                    do {
                        int alt44=2;
                        int LA44_0 = input.LA(1);

                        if ( (LA44_0==68) ) {
                            alt44=1;
                        }


                        switch (alt44) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1195:7: '[' (arrayExpr= expression ) ']'
                    	    {
                    	    match(input,68,FOLLOW_68_in_assignment1875); 
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1195:11: (arrayExpr= expression )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1195:12: arrayExpr= expression
                    	    {
                    	    pushFollow(FOLLOW_expression_in_assignment1880);
                    	    arrayExpr=expression();

                    	    state._fsp--;


                    	        			ExpressionNode node = (arrayExpr!=null?arrayExpr.expr:null);
                    	    				indexList.add(node);
                    	    	   		

                    	    }

                    	    match(input,69,FOLLOW_69_in_assignment1897); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt44 >= 1 ) break loop44;
                                EarlyExitException eee =
                                    new EarlyExitException(44, input);
                                throw eee;
                        }
                        cnt44++;
                    } while (true);

                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1901); 
                    pushFollow(FOLLOW_expression_in_assignment1905);
                    assignExpr=expression();

                    state._fsp--;


                    	    		Expression expr = new Expression((assignExpr!=null?assignExpr.expr:null));
                    	    		VarNode arrayNode = VarNodeMap.get((arrayVar!=null?arrayVar.getText():null));
                    	    		if(ArrayNode.class.isAssignableFrom(arrayNode.getClass())){
                    	    			assign = new VarExpr(new ArrayElement((ArrayNode) arrayNode, indexList), expr);
                    	    		}
                    	    		else{
                    	    			System.err.println("error on line " + (arrayVar!=null?arrayVar.getLine():null) + ": " + (arrayVar!=null?arrayVar.getText():null) + " is not an array");
                    	    			System.exit(1);
                    	    		}
                    	    		
                    	    		if(arrayNode.getType() == VarType.INTERNAL || arrayNode.getType() == VarType.OUTPUT){
                    	   				Integer varCount = VarCountMap.get((arrayVar!=null?arrayVar.getText():null));
                    		    		VarCountMap.put((arrayVar!=null?arrayVar.getText():null), ++varCount);
                    	    		}
                    	   		
                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1920); 

                    }


                    }
                    break;

            }
        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return assign;
    }
    // $ANTLR end "assignment"

    public static class term_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "term"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1221:1: term returns [ExpressionNode expr, int value] : (var= ID | (array= ID ( '[' (arrayExpr= expression ) ']' )+ ) | LPAREN expression RPAREN | INT | TRUE | FALSE );
    public final PlatuInstParser.term_return term() {
        PlatuInstParser.term_return retval = new PlatuInstParser.term_return();
        retval.start = input.LT(1);

        Token var=null;
        Token array=null;
        Token INT12=null;
        PlatuInstParser.expression_return arrayExpr = null;

        PlatuInstParser.expression_return expression11 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1222:5: (var= ID | (array= ID ( '[' (arrayExpr= expression ) ']' )+ ) | LPAREN expression RPAREN | INT | TRUE | FALSE )
            int alt47=6;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA47_1 = input.LA(2);

                if ( (LA47_1==RPAREN||(LA47_1>=QMARK && LA47_1<=SEMICOLON)||(LA47_1>=PLUS && LA47_1<=MOD)||(LA47_1>=GREATER && LA47_1<=NOT_EQUIV)||(LA47_1>=AND && LA47_1<=IMPLICATION)||(LA47_1>=BITWISE_AND && LA47_1<=BITWISE_RSHIFT)||LA47_1==69) ) {
                    alt47=1;
                }
                else if ( (LA47_1==68) ) {
                    alt47=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 47, 1, input);

                    throw nvae;
                }
                }
                break;
            case LPAREN:
                {
                alt47=3;
                }
                break;
            case INT:
                {
                alt47=4;
                }
                break;
            case TRUE:
                {
                alt47=5;
                }
                break;
            case FALSE:
                {
                alt47=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 47, 0, input);

                throw nvae;
            }

            switch (alt47) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1222:9: var= ID
                    {
                    var=(Token)match(input,ID,FOLLOW_ID_in_term1951); 

                        			if(ConstHashMap.containsKey((var!=null?var.getText():null))){
                        				retval.value = ConstHashMap.get((var!=null?var.getText():null));
                        				retval.expr = new ConstNode((var!=null?var.getText():null), retval.value);
                        			}
                        			else if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
                        				retval.value = GlobalConstHashMap.get((var!=null?var.getText():null));
                        				retval.expr = new ConstNode((var!=null?var.getText():null), retval.value);
                        			}
                        			else if(StatevectorMap.containsKey((var!=null?var.getText():null))){ 
                        				retval.value = StatevectorMap.get((var!=null?var.getText():null));
                    					retval.expr = VarNodeMap.get((var!=null?var.getText():null));
                        			}
                        			else if(GlobalVarHashMap.containsKey((var!=null?var.getText():null))){
                        				retval.value = StatevectorMap.get((var!=null?var.getText():null));
                        				retval.expr = VarNodeMap.get((var!=null?var.getText():null));
                        			}
                        			else if(VarNodeMap.containsKey((var!=null?var.getText():null))){
                       					System.err.println("error on line " + (var!=null?var.getLine():null) + ": variable " + (var!=null?var.getText():null) + " is an array");
                       					System.exit(1);
                        			}
                        			else{
                    					System.err.println("error on line " + (var!=null?var.getLine():null) + ": variable " + (var!=null?var.getText():null) + " is not valid");
                    					System.exit(1);
                        			}
                       			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1249:6: (array= ID ( '[' (arrayExpr= expression ) ']' )+ )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1249:6: (array= ID ( '[' (arrayExpr= expression ) ']' )+ )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1249:7: array= ID ( '[' (arrayExpr= expression ) ']' )+
                    {
                    array=(Token)match(input,ID,FOLLOW_ID_in_term1970); 

                      				List<ExpressionNode> indexList = new ArrayList<ExpressionNode>();
                      				List<Integer> valueList = new ArrayList<Integer>();
                      				VarNode arrayNode = null;
                      				
                      				if(!VarNodeMap.containsKey((array!=null?array.getText():null))){
                      					System.err.println("error on line " + (array!=null?array.getLine():null) + ": " + (array!=null?array.getText():null) + " is not a valid array");
                       					System.exit(1);
                      				}
                      				
                      				arrayNode = VarNodeMap.get((array!=null?array.getText():null));
                      				if(!ArrayNode.class.isAssignableFrom(arrayNode.getClass())){
                       					System.err.println("error on line " + (array!=null?array.getLine():null) + ": " + (array!=null?array.getText():null) + " is not a valid array");
                       					System.exit(1);
                       				}
                       			
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1266:6: ( '[' (arrayExpr= expression ) ']' )+
                    int cnt46=0;
                    loop46:
                    do {
                        int alt46=2;
                        int LA46_0 = input.LA(1);

                        if ( (LA46_0==68) ) {
                            alt46=1;
                        }


                        switch (alt46) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1266:7: '[' (arrayExpr= expression ) ']'
                    	    {
                    	    match(input,68,FOLLOW_68_in_term1986); 
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1266:11: (arrayExpr= expression )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1266:12: arrayExpr= expression
                    	    {
                    	    pushFollow(FOLLOW_expression_in_term1991);
                    	    arrayExpr=expression();

                    	    state._fsp--;


                    	      				ExpressionNode node = (arrayExpr!=null?arrayExpr.expr:null);
                    	      				indexList.add(node);
                    	      				valueList.add((arrayExpr!=null?arrayExpr.value:0));
                    	      			

                    	    }

                    	    match(input,69,FOLLOW_69_in_term2006); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt46 >= 1 ) break loop46;
                                EarlyExitException eee =
                                    new EarlyExitException(46, input);
                                throw eee;
                        }
                        cnt46++;
                    } while (true);


                      				String name = ((ArrayNode) arrayNode).getElement(valueList).getName();
                      				retval.value = StatevectorMap.get(name);
                      				retval.expr = new ArrayElement((ArrayNode) arrayNode, indexList);
                      			

                    }


                    }
                    break;
                case 3 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1279:9: LPAREN expression RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_term2031); 
                    pushFollow(FOLLOW_expression_in_term2033);
                    expression11=expression();

                    state._fsp--;

                    match(input,RPAREN,FOLLOW_RPAREN_in_term2035); 
                    retval.expr = (expression11!=null?expression11.expr:null); retval.value = (expression11!=null?expression11.value:0);

                    }
                    break;
                case 4 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1280:9: INT
                    {
                    INT12=(Token)match(input,INT,FOLLOW_INT_in_term2047); 
                    retval.value = Integer.parseInt((INT12!=null?INT12.getText():null)); retval.expr = new ConstNode("name", retval.value);

                    }
                    break;
                case 5 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1281:9: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_term2059); 
                    retval.expr = ONE; retval.value = 1;

                    }
                    break;
                case 6 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1282:9: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_term2071); 
                    retval.expr = ZERO; retval.value = 0;

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "term"

    public static class unary_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "unary"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1285:1: unary returns [ExpressionNode expr, int value] : ( '+' | ( '-' ) )* term ;
    public final PlatuInstParser.unary_return unary() {
        PlatuInstParser.unary_return retval = new PlatuInstParser.unary_return();
        retval.start = input.LT(1);

        PlatuInstParser.term_return term13 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1286:5: ( ( '+' | ( '-' ) )* term )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1286:9: ( '+' | ( '-' ) )* term
            {
            boolean positive = true;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1287:6: ( '+' | ( '-' ) )*
            loop48:
            do {
                int alt48=3;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==PLUS) ) {
                    alt48=1;
                }
                else if ( (LA48_0==MINUS) ) {
                    alt48=2;
                }


                switch (alt48) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1287:7: '+'
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_unary2108); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1287:13: ( '-' )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1287:13: ( '-' )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1287:14: '-'
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_unary2113); 
            	    if(positive){ positive = false;} else {positive = true;}

            	    }


            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);

            pushFollow(FOLLOW_term_in_unary2120);
            term13=term();

            state._fsp--;


                		if(!positive){
                			retval.expr = new MinNode((term13!=null?term13.expr:null));
                			retval.value = -(term13!=null?term13.value:0);
                		}
                		else{
                			retval.expr = (term13!=null?term13.expr:null);
                			retval.value = (term13!=null?term13.value:0);
               			}
                	

            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "unary"

    public static class bitwiseNegation_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "bitwiseNegation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1300:1: bitwiseNegation returns [ExpressionNode expr, int value] : ( '~' )* unary ;
    public final PlatuInstParser.bitwiseNegation_return bitwiseNegation() {
        PlatuInstParser.bitwiseNegation_return retval = new PlatuInstParser.bitwiseNegation_return();
        retval.start = input.LT(1);

        PlatuInstParser.unary_return unary14 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1301:2: ( ( '~' )* unary )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1301:5: ( '~' )* unary
            {
            boolean neg = false;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1302:3: ( '~' )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==BITWISE_NEGATION) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1302:4: '~'
            	    {
            	    match(input,BITWISE_NEGATION,FOLLOW_BITWISE_NEGATION_in_bitwiseNegation2151); 
            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop49;
                }
            } while (true);

            pushFollow(FOLLOW_unary_in_bitwiseNegation2157);
            unary14=unary();

            state._fsp--;


            				if(neg){
            					retval.expr = new BitNegNode((unary14!=null?unary14.expr:null));
            					retval.value = ~(unary14!=null?unary14.value:0);
            				}
            				else{
            					retval.expr = (unary14!=null?unary14.expr:null);
            					retval.value = (unary14!=null?unary14.value:0);
            				}
            			

            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitwiseNegation"

    public static class negation_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "negation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1315:1: negation returns [ExpressionNode expr, int value] : ( '!' )* bitwiseNegation ;
    public final PlatuInstParser.negation_return negation() {
        PlatuInstParser.negation_return retval = new PlatuInstParser.negation_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseNegation_return bitwiseNegation15 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1316:2: ( ( '!' )* bitwiseNegation )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1316:4: ( '!' )* bitwiseNegation
            {
            boolean neg = false;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1317:3: ( '!' )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==NEGATION) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1317:4: '!'
            	    {
            	    match(input,NEGATION,FOLLOW_NEGATION_in_negation2183); 
            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);

            pushFollow(FOLLOW_bitwiseNegation_in_negation2189);
            bitwiseNegation15=bitwiseNegation();

            state._fsp--;


            				if(neg){
            					retval.expr = new NegNode((bitwiseNegation15!=null?bitwiseNegation15.expr:null));
            					retval.value = (bitwiseNegation15!=null?bitwiseNegation15.value:0) == 0 ? 1 : 0;
            				}
            				else{
            					retval.expr = (bitwiseNegation15!=null?bitwiseNegation15.expr:null);
            					retval.value = (bitwiseNegation15!=null?bitwiseNegation15.value:0);
            				}
            			

            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "negation"

    public static class mult_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "mult"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1330:1: mult returns [ExpressionNode expr, int value] : op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )* ;
    public final PlatuInstParser.mult_return mult() {
        PlatuInstParser.mult_return retval = new PlatuInstParser.mult_return();
        retval.start = input.LT(1);

        PlatuInstParser.negation_return op1 = null;

        PlatuInstParser.negation_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1331:5: (op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1331:9: op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )*
            {
            pushFollow(FOLLOW_negation_in_mult2217);
            op1=negation();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1332:6: ( '*' op2= negation | '/' op2= negation | '%' op2= negation )*
            loop51:
            do {
                int alt51=4;
                switch ( input.LA(1) ) {
                case TIMES:
                    {
                    alt51=1;
                    }
                    break;
                case DIV:
                    {
                    alt51=2;
                    }
                    break;
                case MOD:
                    {
                    alt51=3;
                    }
                    break;

                }

                switch (alt51) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1332:8: '*' op2= negation
            	    {
            	    match(input,TIMES,FOLLOW_TIMES_in_mult2229); 
            	    pushFollow(FOLLOW_negation_in_mult2233);
            	    op2=negation();

            	    state._fsp--;

            	    retval.expr = new MultNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value * (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1333:8: '/' op2= negation
            	    {
            	    match(input,DIV,FOLLOW_DIV_in_mult2244); 
            	    pushFollow(FOLLOW_negation_in_mult2248);
            	    op2=negation();

            	    state._fsp--;

            	    retval.expr = new DivNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value / (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 3 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1334:8: '%' op2= negation
            	    {
            	    match(input,MOD,FOLLOW_MOD_in_mult2259); 
            	    pushFollow(FOLLOW_negation_in_mult2263);
            	    op2=negation();

            	    state._fsp--;

            	    retval.expr = new ModNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value % (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop51;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "mult"

    public static class add_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "add"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1338:1: add returns [ExpressionNode expr, int value] : op1= mult ( '+' op2= mult | '-' op2= mult )* ;
    public final PlatuInstParser.add_return add() {
        PlatuInstParser.add_return retval = new PlatuInstParser.add_return();
        retval.start = input.LT(1);

        PlatuInstParser.mult_return op1 = null;

        PlatuInstParser.mult_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1339:5: (op1= mult ( '+' op2= mult | '-' op2= mult )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1339:9: op1= mult ( '+' op2= mult | '-' op2= mult )*
            {
            pushFollow(FOLLOW_mult_in_add2302);
            op1=mult();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1340:6: ( '+' op2= mult | '-' op2= mult )*
            loop52:
            do {
                int alt52=3;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==PLUS) ) {
                    alt52=1;
                }
                else if ( (LA52_0==MINUS) ) {
                    alt52=2;
                }


                switch (alt52) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1340:8: '+' op2= mult
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_add2313); 
            	    pushFollow(FOLLOW_mult_in_add2317);
            	    op2=mult();

            	    state._fsp--;

            	    retval.expr = new AddNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value + (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1341:9: '-' op2= mult
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_add2329); 
            	    pushFollow(FOLLOW_mult_in_add2333);
            	    op2=mult();

            	    state._fsp--;

            	    retval.expr = new SubNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value - (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "add"

    public static class shift_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "shift"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1345:1: shift returns [ExpressionNode expr, int value] : op1= add ( '<<' op2= add | '>>' op2= add )* ;
    public final PlatuInstParser.shift_return shift() {
        PlatuInstParser.shift_return retval = new PlatuInstParser.shift_return();
        retval.start = input.LT(1);

        PlatuInstParser.add_return op1 = null;

        PlatuInstParser.add_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1346:5: (op1= add ( '<<' op2= add | '>>' op2= add )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1346:9: op1= add ( '<<' op2= add | '>>' op2= add )*
            {
            pushFollow(FOLLOW_add_in_shift2372);
            op1=add();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1347:6: ( '<<' op2= add | '>>' op2= add )*
            loop53:
            do {
                int alt53=3;
                int LA53_0 = input.LA(1);

                if ( (LA53_0==BITWISE_LSHIFT) ) {
                    alt53=1;
                }
                else if ( (LA53_0==BITWISE_RSHIFT) ) {
                    alt53=2;
                }


                switch (alt53) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1347:8: '<<' op2= add
            	    {
            	    match(input,BITWISE_LSHIFT,FOLLOW_BITWISE_LSHIFT_in_shift2383); 
            	    pushFollow(FOLLOW_add_in_shift2387);
            	    op2=add();

            	    state._fsp--;

            	    retval.expr = new LeftShiftNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value << (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1348:9: '>>' op2= add
            	    {
            	    match(input,BITWISE_RSHIFT,FOLLOW_BITWISE_RSHIFT_in_shift2399); 
            	    pushFollow(FOLLOW_add_in_shift2403);
            	    op2=add();

            	    state._fsp--;

            	    retval.expr = new RightShiftNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value >> (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop53;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "shift"

    public static class relation_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "relation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1352:1: relation returns [ExpressionNode expr, int value] : op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* ;
    public final PlatuInstParser.relation_return relation() {
        PlatuInstParser.relation_return retval = new PlatuInstParser.relation_return();
        retval.start = input.LT(1);

        PlatuInstParser.shift_return op1 = null;

        PlatuInstParser.shift_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1353:5: (op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1353:9: op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
            {
            pushFollow(FOLLOW_shift_in_relation2438);
            op1=shift();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1354:6: ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
            loop54:
            do {
                int alt54=5;
                switch ( input.LA(1) ) {
                case LESS:
                    {
                    alt54=1;
                    }
                    break;
                case LESS_EQUAL:
                    {
                    alt54=2;
                    }
                    break;
                case GREATER_EQUAL:
                    {
                    alt54=3;
                    }
                    break;
                case GREATER:
                    {
                    alt54=4;
                    }
                    break;

                }

                switch (alt54) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1354:8: '<' op2= shift
            	    {
            	    match(input,LESS,FOLLOW_LESS_in_relation2449); 
            	    pushFollow(FOLLOW_shift_in_relation2453);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new LessNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value < (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1355:9: '<=' op2= shift
            	    {
            	    match(input,LESS_EQUAL,FOLLOW_LESS_EQUAL_in_relation2465); 
            	    pushFollow(FOLLOW_shift_in_relation2469);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new LessEqualNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value <= (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 3 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1356:9: '>=' op2= shift
            	    {
            	    match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_relation2481); 
            	    pushFollow(FOLLOW_shift_in_relation2485);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new GreatEqualNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value >= (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 4 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1357:9: '>' op2= shift
            	    {
            	    match(input,GREATER,FOLLOW_GREATER_in_relation2497); 
            	    pushFollow(FOLLOW_shift_in_relation2501);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new GreatNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value > (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;

            	default :
            	    break loop54;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "relation"

    public static class equivalence_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "equivalence"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1361:1: equivalence returns [ExpressionNode expr, int value] : op1= relation ( '==' op2= relation | '!=' op2= relation )* ;
    public final PlatuInstParser.equivalence_return equivalence() {
        PlatuInstParser.equivalence_return retval = new PlatuInstParser.equivalence_return();
        retval.start = input.LT(1);

        PlatuInstParser.relation_return op1 = null;

        PlatuInstParser.relation_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1362:5: (op1= relation ( '==' op2= relation | '!=' op2= relation )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1362:9: op1= relation ( '==' op2= relation | '!=' op2= relation )*
            {
            pushFollow(FOLLOW_relation_in_equivalence2540);
            op1=relation();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1363:6: ( '==' op2= relation | '!=' op2= relation )*
            loop55:
            do {
                int alt55=3;
                int LA55_0 = input.LA(1);

                if ( (LA55_0==EQUIV) ) {
                    alt55=1;
                }
                else if ( (LA55_0==NOT_EQUIV) ) {
                    alt55=2;
                }


                switch (alt55) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1363:8: '==' op2= relation
            	    {
            	    match(input,EQUIV,FOLLOW_EQUIV_in_equivalence2551); 
            	    pushFollow(FOLLOW_relation_in_equivalence2555);
            	    op2=relation();

            	    state._fsp--;

            	    retval.expr = new EquivNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value == (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1364:8: '!=' op2= relation
            	    {
            	    match(input,NOT_EQUIV,FOLLOW_NOT_EQUIV_in_equivalence2566); 
            	    pushFollow(FOLLOW_relation_in_equivalence2570);
            	    op2=relation();

            	    state._fsp--;

            	    retval.expr = new NotEquivNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value != (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;

            	default :
            	    break loop55;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "equivalence"

    public static class bitwiseAnd_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "bitwiseAnd"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1368:1: bitwiseAnd returns [ExpressionNode expr, int value] : op1= equivalence ( '&' op2= equivalence )* ;
    public final PlatuInstParser.bitwiseAnd_return bitwiseAnd() {
        PlatuInstParser.bitwiseAnd_return retval = new PlatuInstParser.bitwiseAnd_return();
        retval.start = input.LT(1);

        PlatuInstParser.equivalence_return op1 = null;

        PlatuInstParser.equivalence_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1369:5: (op1= equivalence ( '&' op2= equivalence )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1369:9: op1= equivalence ( '&' op2= equivalence )*
            {
            pushFollow(FOLLOW_equivalence_in_bitwiseAnd2609);
            op1=equivalence();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1370:6: ( '&' op2= equivalence )*
            loop56:
            do {
                int alt56=2;
                int LA56_0 = input.LA(1);

                if ( (LA56_0==BITWISE_AND) ) {
                    alt56=1;
                }


                switch (alt56) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1370:8: '&' op2= equivalence
            	    {
            	    match(input,BITWISE_AND,FOLLOW_BITWISE_AND_in_bitwiseAnd2621); 
            	    pushFollow(FOLLOW_equivalence_in_bitwiseAnd2625);
            	    op2=equivalence();

            	    state._fsp--;

            	    retval.expr = new BitAndNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value & (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop56;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitwiseAnd"

    public static class bitwiseXor_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "bitwiseXor"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1374:1: bitwiseXor returns [ExpressionNode expr, int value] : op1= bitwiseAnd ( '^' op2= bitwiseAnd )* ;
    public final PlatuInstParser.bitwiseXor_return bitwiseXor() {
        PlatuInstParser.bitwiseXor_return retval = new PlatuInstParser.bitwiseXor_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseAnd_return op1 = null;

        PlatuInstParser.bitwiseAnd_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1375:5: (op1= bitwiseAnd ( '^' op2= bitwiseAnd )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1375:9: op1= bitwiseAnd ( '^' op2= bitwiseAnd )*
            {
            pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2664);
            op1=bitwiseAnd();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1376:6: ( '^' op2= bitwiseAnd )*
            loop57:
            do {
                int alt57=2;
                int LA57_0 = input.LA(1);

                if ( (LA57_0==BITWISE_XOR) ) {
                    alt57=1;
                }


                switch (alt57) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1376:8: '^' op2= bitwiseAnd
            	    {
            	    match(input,BITWISE_XOR,FOLLOW_BITWISE_XOR_in_bitwiseXor2675); 
            	    pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2679);
            	    op2=bitwiseAnd();

            	    state._fsp--;

            	    retval.expr = new BitXorNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value ^ (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop57;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitwiseXor"

    public static class bitwiseOr_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "bitwiseOr"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1380:1: bitwiseOr returns [ExpressionNode expr, int value] : op1= bitwiseXor ( '|' op2= bitwiseXor )* ;
    public final PlatuInstParser.bitwiseOr_return bitwiseOr() {
        PlatuInstParser.bitwiseOr_return retval = new PlatuInstParser.bitwiseOr_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseXor_return op1 = null;

        PlatuInstParser.bitwiseXor_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1381:5: (op1= bitwiseXor ( '|' op2= bitwiseXor )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1381:9: op1= bitwiseXor ( '|' op2= bitwiseXor )*
            {
            pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2718);
            op1=bitwiseXor();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1382:6: ( '|' op2= bitwiseXor )*
            loop58:
            do {
                int alt58=2;
                int LA58_0 = input.LA(1);

                if ( (LA58_0==BITWISE_OR) ) {
                    alt58=1;
                }


                switch (alt58) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1382:8: '|' op2= bitwiseXor
            	    {
            	    match(input,BITWISE_OR,FOLLOW_BITWISE_OR_in_bitwiseOr2729); 
            	    pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2733);
            	    op2=bitwiseXor();

            	    state._fsp--;

            	    retval.expr = new BitOrNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value | (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "bitwiseOr"

    public static class and_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "and"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1386:1: and returns [ExpressionNode expr, int value] : op1= bitwiseOr ( '&&' op2= bitwiseOr )* ;
    public final PlatuInstParser.and_return and() {
        PlatuInstParser.and_return retval = new PlatuInstParser.and_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseOr_return op1 = null;

        PlatuInstParser.bitwiseOr_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1387:5: (op1= bitwiseOr ( '&&' op2= bitwiseOr )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1387:9: op1= bitwiseOr ( '&&' op2= bitwiseOr )*
            {
            pushFollow(FOLLOW_bitwiseOr_in_and2772);
            op1=bitwiseOr();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1388:6: ( '&&' op2= bitwiseOr )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==AND) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1388:8: '&&' op2= bitwiseOr
            	    {
            	    match(input,AND,FOLLOW_AND_in_and2783); 
            	    pushFollow(FOLLOW_bitwiseOr_in_and2787);
            	    op2=bitwiseOr();

            	    state._fsp--;

            	    retval.expr = new AndNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value != 0 && (op2!=null?op2.value:0) != 0) ? 1 : 0;

            	    }
            	    break;

            	default :
            	    break loop59;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "and"

    public static class or_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "or"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1392:1: or returns [ExpressionNode expr, int value] : op1= and ( '||' op2= and )* ;
    public final PlatuInstParser.or_return or() {
        PlatuInstParser.or_return retval = new PlatuInstParser.or_return();
        retval.start = input.LT(1);

        PlatuInstParser.and_return op1 = null;

        PlatuInstParser.and_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1393:5: (op1= and ( '||' op2= and )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1393:9: op1= and ( '||' op2= and )*
            {
            pushFollow(FOLLOW_and_in_or2826);
            op1=and();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1394:6: ( '||' op2= and )*
            loop60:
            do {
                int alt60=2;
                int LA60_0 = input.LA(1);

                if ( (LA60_0==OR) ) {
                    alt60=1;
                }


                switch (alt60) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1394:8: '||' op2= and
            	    {
            	    match(input,OR,FOLLOW_OR_in_or2837); 
            	    pushFollow(FOLLOW_and_in_or2841);
            	    op2=and();

            	    state._fsp--;

            	    retval.expr = new OrNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value != 0 || (op2!=null?op2.value:0) != 0) ? 1 : 0;

            	    }
            	    break;

            	default :
            	    break loop60;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "or"

    public static class implication_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "implication"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1398:1: implication returns [ExpressionNode expr, int value] : op1= or ( '->' op2= or )* ;
    public final PlatuInstParser.implication_return implication() {
        PlatuInstParser.implication_return retval = new PlatuInstParser.implication_return();
        retval.start = input.LT(1);

        PlatuInstParser.or_return op1 = null;

        PlatuInstParser.or_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1399:5: (op1= or ( '->' op2= or )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1399:7: op1= or ( '->' op2= or )*
            {
            pushFollow(FOLLOW_or_in_implication2878);
            op1=or();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1400:6: ( '->' op2= or )*
            loop61:
            do {
                int alt61=2;
                int LA61_0 = input.LA(1);

                if ( (LA61_0==IMPLICATION) ) {
                    alt61=1;
                }


                switch (alt61) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1400:8: '->' op2= or
            	    {
            	    match(input,IMPLICATION,FOLLOW_IMPLICATION_in_implication2889); 
            	    pushFollow(FOLLOW_or_in_implication2893);
            	    op2=or();

            	    state._fsp--;

            	    retval.expr = new ImplicationNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value == 0 || (op2!=null?op2.value:0) != 0) ? 1 : 0;

            	    }
            	    break;

            	default :
            	    break loop61;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "implication"

    public static class expression_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    }

    // $ANTLR start "expression"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1404:1: expression returns [ExpressionNode expr, int value] : op1= implication ( '?' op2= expression ':' op3= expression )? ;
    public final PlatuInstParser.expression_return expression() {
        PlatuInstParser.expression_return retval = new PlatuInstParser.expression_return();
        retval.start = input.LT(1);

        PlatuInstParser.implication_return op1 = null;

        PlatuInstParser.expression_return op2 = null;

        PlatuInstParser.expression_return op3 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1405:5: (op1= implication ( '?' op2= expression ':' op3= expression )? )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1405:9: op1= implication ( '?' op2= expression ':' op3= expression )?
            {
            pushFollow(FOLLOW_implication_in_expression2933);
            op1=implication();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1406:6: ( '?' op2= expression ':' op3= expression )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==QMARK) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1406:7: '?' op2= expression ':' op3= expression
                    {
                    match(input,QMARK,FOLLOW_QMARK_in_expression2943); 
                    pushFollow(FOLLOW_expression_in_expression2947);
                    op2=expression();

                    state._fsp--;

                    match(input,COLON,FOLLOW_COLON_in_expression2949); 
                    pushFollow(FOLLOW_expression_in_expression2953);
                    op3=expression();

                    state._fsp--;


                        			retval.value = (retval.value != 0) ? (op2!=null?op2.value:0) : (op3!=null?op3.value:0);
                        			retval.expr = new TernaryNode(retval.expr, (op2!=null?op2.expr:null), (op3!=null?op3.expr:null));
                        		

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return retval;
    }
    // $ANTLR end "expression"


    // $ANTLR start "ctlTerm"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1415:1: ctlTerm : ( ID | LPAREN ctl LPAREN );
    public final void ctlTerm() {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1416:2: ( ID | LPAREN ctl LPAREN )
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==ID) ) {
                alt63=1;
            }
            else if ( (LA63_0==LPAREN) ) {
                alt63=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 63, 0, input);

                throw nvae;
            }
            switch (alt63) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1416:4: ID
                    {
                    match(input,ID,FOLLOW_ID_in_ctlTerm2989); 

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1417:4: LPAREN ctl LPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_ctlTerm2994); 
                    pushFollow(FOLLOW_ctl_in_ctlTerm2996);
                    ctl();

                    state._fsp--;

                    match(input,LPAREN,FOLLOW_LPAREN_in_ctlTerm2998); 

                    }
                    break;

            }
        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }
        finally {
        }
        return ;
    }
    // $ANTLR end "ctlTerm"


    // $ANTLR start "ctl"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1420:1: ctl : ;
    public final static void ctl() {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1421:2: ()
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1422:2: 
            {
            }

        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ctl"

    // Delegated rules


 

    public static final BitSet FOLLOW_include_in_parseLpnFile52 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_globalConstants_in_parseLpnFile55 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_globalVariables_in_parseLpnFile58 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_main_in_parseLpnFile82 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_moduleClass_in_parseLpnFile87 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_EOF_in_parseLpnFile92 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_include110 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_60_in_include112 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_include114 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_PATH_in_include117 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_include119 = new BitSet(new long[]{0x0000002000000010L});
    public static final BitSet FOLLOW_LESS_in_include131 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_61_in_include133 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_include135 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_main148 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_MODULE_in_main150 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_NAME_in_main152 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_main154 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_main156 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_62_in_main158 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_main160 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_main162 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_variables_in_main171 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_constants_in_main174 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_instantiation_in_main177 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LESS_in_main179 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_63_in_main181 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_main183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_process198 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_64_in_process200 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_NAME_in_process202 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_process204 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_process206 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_process210 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_process212 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_process214 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LESS_in_process216 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_process218 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_process220 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_moduleClass243 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_moduleClass245 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_NAME_in_moduleClass247 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_moduleClass249 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass251 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_moduleClass255 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass270 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_moduleClass272 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_moduleClass274 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass276 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_ID_in_moduleClass283 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_68_in_moduleClass300 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_moduleClass304 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_moduleClass320 = new BitSet(new long[]{0x0000000000060000L,0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_moduleClass344 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_moduleClass364 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_moduleClass368 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_68_in_moduleClass384 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_moduleClass388 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_moduleClass404 = new BitSet(new long[]{0x0000000000060000L,0x0000000000000010L});
    public static final BitSet FOLLOW_COMMA_in_moduleClass427 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_moduleClass431 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass454 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_moduleClass456 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_constants_in_moduleClass458 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_variables_in_moduleClass461 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_logic_in_moduleClass463 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LESS_in_moduleClass465 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_70_in_moduleClass467 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_moduleClass469 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_constants494 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_71_in_constants496 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_constants498 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_ID_in_constants503 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_constants505 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_constants509 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_constants520 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_constants524 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_72_in_constants526 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_constants528 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalConstants545 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_71_in_globalConstants547 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants549 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_ID_in_globalConstants554 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_globalConstants556 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_globalConstants560 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalConstants586 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_globalConstants590 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_72_in_globalConstants592 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants594 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalVariables608 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_INTERNAL_in_globalVariables610 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables612 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_globalVarDecl_in_globalVariables615 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_globalArrayDecl_in_globalVariables619 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_globalVariables623 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_73_in_globalVariables625 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables627 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_globalVarDecl640 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_globalVarDecl650 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_globalVarDecl655 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_ID_in_globalVarDecl669 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalVarDecl680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_globalArrayDecl694 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_68_in_globalArrayDecl704 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_globalArrayDecl709 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_globalArrayDecl721 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalArrayDecl725 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_variables741 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_INTERNAL_in_variables743 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_variables745 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_varDecl_in_variables749 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_arrayDecl_in_variables753 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_variables757 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_73_in_variables759 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_variables761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl778 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl788 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_varDecl793 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_ID_in_varDecl807 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_varDecl826 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_arrayDecl840 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_68_in_arrayDecl851 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_arrayDecl856 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_arrayDecl867 = new BitSet(new long[]{0x0000000800004000L,0x0000000000000010L});
    public static final BitSet FOLLOW_EQUALS_in_arrayDecl879 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_74_in_arrayDecl889 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_arrayDecl894 = new BitSet(new long[]{0x0000000000000060L,0x0000000000000800L});
    public static final BitSet FOLLOW_ID_in_arrayDecl908 = new BitSet(new long[]{0x0000000000000060L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_arrayDecl920 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_arrayDecl932 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_instantiation950 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_instantiation954 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_instantiation970 = new BitSet(new long[]{0x0000000000000280L});
    public static final BitSet FOLLOW_MEMBER_in_instantiation975 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COMMA_in_instantiation977 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_MEMBER_in_instantiation997 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_instantiation1015 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_instantiation1017 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_marking_in_logic1063 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_transition_in_logic1066 = new BitSet(new long[]{0x0000002000000002L});
    public static final BitSet FOLLOW_LESS_in_marking1119 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_MARKING_in_marking1121 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_marking1123 = new BitSet(new long[]{0x0000002000000060L});
    public static final BitSet FOLLOW_INT_in_marking1129 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_ID_in_marking1155 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_COMMA_in_marking1179 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_marking1184 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_ID_in_marking1210 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_LESS_in_marking1237 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_76_in_marking1239 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_marking1241 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_transition1275 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_TRANSITION_in_transition1277 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LABEL_in_transition1279 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1281 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1283 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_set_in_transition1287 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1293 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_PRESET_in_transition1295 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1297 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1300 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1302 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1307 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_transition1312 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_ID_in_transition1332 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1348 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_transition1352 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1370 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_transition1374 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1389 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_POSTSET_in_transition1393 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1395 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1399 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1401 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1406 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_transition1412 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_ID_in_transition1432 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1457 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_transition1461 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1482 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_transition1485 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1504 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_transition1509 = new BitSet(new long[]{0x0000002000000020L,0x000000000001C000L});
    public static final BitSet FOLLOW_guard_in_transition1512 = new BitSet(new long[]{0x0000002000000020L,0x0000000000014000L});
    public static final BitSet FOLLOW_delay_in_transition1532 = new BitSet(new long[]{0x0000002000000020L,0x0000000000004000L});
    public static final BitSet FOLLOW_assertion_in_transition1553 = new BitSet(new long[]{0x0000002000000020L,0x0000000000004000L});
    public static final BitSet FOLLOW_assignment_in_transition1573 = new BitSet(new long[]{0x0000002000000020L,0x0000000000004000L});
    public static final BitSet FOLLOW_LESS_in_transition1593 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_77_in_transition1595 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_transition1597 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_78_in_assertion1631 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_assertion1633 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assertion1635 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_assertion1637 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assertion1639 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_79_in_guard1664 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_guard1666 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_guard1668 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_guard1670 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_guard1672 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_80_in_delay1706 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_delay1708 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_delay1712 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COMMA_in_delay1726 = new BitSet(new long[]{0x0000000000000040L,0x0000000000020000L});
    public static final BitSet FOLLOW_INT_in_delay1731 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_81_in_delay1746 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_delay1759 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_delay1761 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1787 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1789 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_assignment1793 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1815 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1817 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assignment1835 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1850 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1858 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_68_in_assignment1875 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assignment1880 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_assignment1897 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1901 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assignment1905 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1920 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_term1951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_term1970 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_68_in_term1986 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_term1991 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_term2006 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_LPAREN_in_term2031 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_term2033 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_term2035 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_term2047 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_term2059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_term2071 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_unary2108 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_MINUS_in_unary2113 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_term_in_unary2120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BITWISE_NEGATION_in_bitwiseNegation2151 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_unary_in_bitwiseNegation2157 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEGATION_in_negation2183 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseNegation_in_negation2189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_negation_in_mult2217 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_TIMES_in_mult2229 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_negation_in_mult2233 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_DIV_in_mult2244 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_negation_in_mult2248 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_MOD_in_mult2259 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_negation_in_mult2263 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_mult_in_add2302 = new BitSet(new long[]{0x00000000C0000002L});
    public static final BitSet FOLLOW_PLUS_in_add2313 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_mult_in_add2317 = new BitSet(new long[]{0x00000000C0000002L});
    public static final BitSet FOLLOW_MINUS_in_add2329 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_mult_in_add2333 = new BitSet(new long[]{0x00000000C0000002L});
    public static final BitSet FOLLOW_add_in_shift2372 = new BitSet(new long[]{0x000C000000000002L});
    public static final BitSet FOLLOW_BITWISE_LSHIFT_in_shift2383 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_add_in_shift2387 = new BitSet(new long[]{0x000C000000000002L});
    public static final BitSet FOLLOW_BITWISE_RSHIFT_in_shift2399 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_add_in_shift2403 = new BitSet(new long[]{0x000C000000000002L});
    public static final BitSet FOLLOW_shift_in_relation2438 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_LESS_in_relation2449 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2453 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_LESS_EQUAL_in_relation2465 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2469 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_relation2481 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2485 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_GREATER_in_relation2497 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2501 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_relation_in_equivalence2540 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_EQUIV_in_equivalence2551 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_relation_in_equivalence2555 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_NOT_EQUIV_in_equivalence2566 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_relation_in_equivalence2570 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2609 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_BITWISE_AND_in_bitwiseAnd2621 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2625 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2664 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_BITWISE_XOR_in_bitwiseXor2675 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2679 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2718 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_BITWISE_OR_in_bitwiseOr2729 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2733 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2772 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_AND_in_and2783 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2787 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_and_in_or2826 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_OR_in_or2837 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_and_in_or2841 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_or_in_implication2878 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_IMPLICATION_in_implication2889 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_or_in_implication2893 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_implication_in_expression2933 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_QMARK_in_expression2943 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_expression2947 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_COLON_in_expression2949 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_expression2953 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_ctlTerm2989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_ctlTerm2994 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_ctl_in_ctlTerm2996 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_ctlTerm2998 = new BitSet(new long[]{0x0000000000000002L});

}