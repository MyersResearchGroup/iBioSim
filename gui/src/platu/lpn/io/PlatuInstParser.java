// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g 2011-08-07 04:16:34

    package platu.lpn.io;
    
    import java.util.StringTokenizer;
    import java.io.File;
    import platu.lpn.io.Instance;
    import java.util.Map.Entry;
    import java.util.HashMap;
    import java.util.HashSet;
    import java.util.LinkedList;
    import java.util.Queue;
    import platu.lpn.LPN;
    import platu.lpn.VarSet;
    import platu.lpn.LpnTranList;
    import platu.lpn.LPNTran;
    import platu.lpn.DualHashMap;
    import platu.lpn.VarExpr;
    import platu.lpn.VarExprList;
    import platu.stategraph.StateGraph;
    import platu.project.Project;
    import platu.expression.*;
    import platu.Main;    


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class PlatuInstParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "PATH", "ID", "INT", "MEMBER", "LPAREN", "RPAREN", "TRUE", "FALSE", "QMARK", "COLON", "SEMICOLON", "PERIOD", "UNDERSCORE", "COMMA", "QUOTE", "MODULE", "NAME", "INPUT", "OUTPUT", "INTERNAL", "MARKING", "STATE_VECTOR", "TRANSITION", "LABEL", "PRESET", "POSTSET", "PLUS", "MINUS", "TIMES", "DIV", "MOD", "EQUALS", "GREATER", "LESS", "GREATER_EQUAL", "LESS_EQUAL", "EQUIV", "NOT_EQUIV", "NEGATION", "AND", "OR", "IMPLICATION", "BITWISE_NEGATION", "BITWISE_AND", "BITWISE_OR", "BITWISE_XOR", "BITWISE_LSHIFT", "BITWISE_RSHIFT", "LETTER", "DIGIT", "FILE", "WS", "COMMENT", "MULTILINECOMMENT", "XMLCOMMENT", "IGNORE", "'include'", "'/include'", "'main'", "'/mod'", "'class'", "'arg'", "'['", "']'", "'/class'", "'const'", "'/const'", "'/var'", "'{'", "'}'", "'/marking'", "'/transition'", "'assert'", "'condition'", "'delay'", "'inf'"
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
        

    public String[] getTokenNames() { return PlatuInstParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g"; }


    	// static variables
        static private int INFINITY = Integer.MAX_VALUE;
        static private boolean main = false;  // true if main module has been parsed
        static private ExpressionNode ZERO = new ConstNode("FALSE", 0);  // constant false node
        static private ExpressionNode ONE = new ConstNode("TRUE", 1);  // constant true node
        static private Expression TrueExpr = new Expression(ONE); // constant true expression
        static public HashMap<String, LPN> LpnMap = new HashMap<String, LPN>();  // all modules parsed, keyed by module name
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
    	private void error(String error){
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
    				element.setType(platu.lpn.VarType.GLOBAL);
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
    		newArray.setType(platu.lpn.VarType.GLOBAL);
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
    				element.setType(platu.lpn.VarType.INPUT);
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
    		newArray.setType(platu.lpn.VarType.INPUT);
    		VarNodeMap.put(var, newArray);
    //  		VarCountMap.put(var, 0);
    		Inputs.add(var);
    		argumentList.add(var);
    	}



    // $ANTLR start "parseLpnFile"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:202:1: parseLpnFile[Project prj] : ( include )? ( globalConstants )? ( globalVariables )? ( main[prj] )? ( moduleClass[prj] )* EOF ;
    public final void parseLpnFile(Project prj) throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:5: ( ( include )? ( globalConstants )? ( globalVariables )? ( main[prj] )? ( moduleClass[prj] )* EOF )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:7: ( include )? ( globalConstants )? ( globalVariables )? ( main[prj] )? ( moduleClass[prj] )* EOF
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:7: ( include )?
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
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:7: include
                    {
                    pushFollow(FOLLOW_include_in_parseLpnFile52);
                    include();

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:16: ( globalConstants )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==LESS) ) {
                int LA2_1 = input.LA(2);

                if ( (LA2_1==69) ) {
                    alt2=1;
                }
            }
            switch (alt2) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:16: globalConstants
                    {
                    pushFollow(FOLLOW_globalConstants_in_parseLpnFile55);
                    globalConstants();

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:33: ( globalVariables )?
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
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:203:33: globalVariables
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
                    	
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:211:8: ( main[prj] )?
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
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:211:8: main[prj]
                    {
                    pushFollow(FOLLOW_main_in_parseLpnFile82);
                    main(prj);

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:211:19: ( moduleClass[prj] )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==LESS) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:211:20: moduleClass[prj]
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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:214:1: include : '<' 'include' '>' ( PATH ';' )+ '<' '/include' '>' ;
    public final void include() throws RecognitionException {
        Token PATH1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:215:2: ( '<' 'include' '>' ( PATH ';' )+ '<' '/include' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:215:4: '<' 'include' '>' ( PATH ';' )+ '<' '/include' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_include110); 
            match(input,60,FOLLOW_60_in_include112); 
            match(input,GREATER,FOLLOW_GREATER_in_include114); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:215:22: ( PATH ';' )+
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
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:215:23: PATH ';'
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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:223:1: main[Project prj] : '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' instantiation '<' '/mod' '>' ;
    public final void main(Project prj) throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:224:2: ( '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' instantiation '<' '/mod' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:224:4: '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' instantiation '<' '/mod' '>'
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
            	    			globalVarNode.setType(platu.lpn.VarType.GLOBAL);
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
            			
            pushFollow(FOLLOW_instantiation_in_main171);
            instantiation();

            state._fsp--;

            match(input,LESS,FOLLOW_LESS_in_main173); 
            match(input,63,FOLLOW_63_in_main175); 
            match(input,GREATER,FOLLOW_GREATER_in_main177); 

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


    // $ANTLR start "moduleClass"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:275:1: moduleClass[Project prj] returns [LPN lpn] : ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' ) ;
    public final LPN moduleClass(Project prj) throws RecognitionException {
        LPN lpn = null;

        Token modName=null;
        Token arrayArg2=null;
        Token arg2=null;
        Token arrayArg=null;
        Token arg=null;
        PlatuInstParser.expression_return arrayExpr2 = null;

        PlatuInstParser.expression_return arrayExpr = null;

        PlatuInstParser.logic_return logic2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:276:5: ( ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' ) )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:276:7: ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' )
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:276:7: ( '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:276:9: '<' 'class' 'name' '=' '\"' modName= ID '\"' 'arg' '=' '\"' ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )? '\"' '>' ( constants )? variables logic '<' '/class' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_moduleClass203); 
            match(input,64,FOLLOW_64_in_moduleClass205); 
            match(input,NAME,FOLLOW_NAME_in_moduleClass207); 
            match(input,EQUALS,FOLLOW_EQUALS_in_moduleClass209); 
            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass211); 
            modName=(Token)match(input,ID,FOLLOW_ID_in_moduleClass215); 

                			// module names must be unique
                			if(LpnMap.containsKey((modName!=null?modName.getText():null))){
                				System.err.println("error on line " + modName.getLine() + ": module " + (modName!=null?modName.getText():null) + " already exists");
                				System.exit(1);
                			}
                			
                			if((modName!=null?modName.getText():null).equals("main")){
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
            	    			globalVarNode.setType(platu.lpn.VarType.GLOBAL);
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
                		
            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass230); 
            match(input,65,FOLLOW_65_in_moduleClass232); 
            match(input,EQUALS,FOLLOW_EQUALS_in_moduleClass234); 
            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass236); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:326:24: ( ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )* )?
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0==ID) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:326:25: ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) ) ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )*
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:326:25: ( (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ ) | (arg2= ID ) )
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==ID) ) {
                        int LA8_1 = input.LA(2);

                        if ( (LA8_1==66) ) {
                            alt8=1;
                        }
                        else if ( ((LA8_1>=COMMA && LA8_1<=QUOTE)) ) {
                            alt8=2;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 8, 1, input);

                            throw nvae;
                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 0, input);

                        throw nvae;
                    }
                    switch (alt8) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:326:26: (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ )
                            {
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:326:26: (arrayArg2= ID ( '[' arrayExpr2= expression ']' )+ )
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:326:27: arrayArg2= ID ( '[' arrayExpr2= expression ']' )+
                            {
                            arrayArg2=(Token)match(input,ID,FOLLOW_ID_in_moduleClass243); 

                                			// check aginst globals and other inputs
                            	   			if(GlobalConstHashMap.containsKey((arrayArg2!=null?arrayArg2.getText():null))){
                            	   				System.err.println("error on line " + arrayArg2.getLine() + ": variable " + (arrayArg2!=null?arrayArg2.getText():null) + " is already defined as a global constant");
                            	   				System.exit(1);
                            	   			}
                            	   			else if(GlobalVarHashMap.containsKey((arrayArg2!=null?arrayArg2.getText():null))){
                            	   				System.err.println("error on line " + arrayArg2.getLine() + ": variable " + (arrayArg2!=null?arrayArg2.getText():null) + " is already defined as a global variable");
                            	   				System.exit(1);
                            	   			}
                            	   			else if(VarNodeMap.containsKey((arrayArg2!=null?arrayArg2.getText():null))){
                            	   				System.err.println("error on line " + arrayArg2.getLine() + ": variable " + (arrayArg2!=null?arrayArg2.getText():null) + " is already defined");
                            	   				System.exit(1);
                            	   			}
                            	   			
                            	   			List<Integer> dimensionList = new ArrayList<Integer>();
                                		
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:344:6: ( '[' arrayExpr2= expression ']' )+
                            int cnt7=0;
                            loop7:
                            do {
                                int alt7=2;
                                int LA7_0 = input.LA(1);

                                if ( (LA7_0==66) ) {
                                    alt7=1;
                                }


                                switch (alt7) {
                            	case 1 :
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:344:7: '[' arrayExpr2= expression ']'
                            	    {
                            	    match(input,66,FOLLOW_66_in_moduleClass260); 
                            	    pushFollow(FOLLOW_expression_in_moduleClass264);
                            	    arrayExpr2=expression();

                            	    state._fsp--;


                            	        			dimensionList.add((arrayExpr2!=null?arrayExpr2.value:0));
                            	        		
                            	    match(input,67,FOLLOW_67_in_moduleClass280); 

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt7 >= 1 ) break loop7;
                                        EarlyExitException eee =
                                            new EarlyExitException(7, input);
                                        throw eee;
                                }
                                cnt7++;
                            } while (true);


                                			createInputArray((arrayArg2!=null?arrayArg2.getText():null), dimensionList);
                                		

                            }


                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:352:10: (arg2= ID )
                            {
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:352:10: (arg2= ID )
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:352:11: arg2= ID
                            {
                            arg2=(Token)match(input,ID,FOLLOW_ID_in_moduleClass304); 

                                			// check against globals
                                			if(GlobalConstHashMap.containsKey((arg2!=null?arg2.getText():null))){
                                				System.err.println("error on line " + arg2.getLine() + ": variable " + (arg2!=null?arg2.getText():null) + " is already defined as a global constant");
                                				System.exit(1);
                                			}
                                			else if(GlobalVarHashMap.containsKey((arg2!=null?arg2.getText():null))){
                                				System.err.println("error on line " + arg2.getLine() + ": variable " + (arg2!=null?arg2.getText():null) + " is already defined as a global variable");
                                				System.exit(1);
                                			}
                                			
                                			// add variable and value to state vector
                            				StatevectorMap.put(arg2.getText(), 0);
                            				
                            				// generate variable index and create new var node  
                            				int index = VariableIndex++;
                               				VarIndexMap.insert(arg2.getText(), index);
                               				
                               				VarNode inputVarNode =  new VarNode(arg2.getText(), index);
                               				inputVarNode.setType(platu.lpn.VarType.INPUT);
                               				VarNodeMap.put(arg2.getText(), inputVarNode);
                            //    			VarCountMap.put(arg2.getText(), 0);
                                			
                                			argumentList.add(arg2.getText());
                            				Inputs.add(arg2.getText());
                                		

                            }


                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:379:9: ( ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ ) | ( ',' arg= ID ) )*
                    loop10:
                    do {
                        int alt10=3;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0==COMMA) ) {
                            int LA10_2 = input.LA(2);

                            if ( (LA10_2==ID) ) {
                                int LA10_3 = input.LA(3);

                                if ( (LA10_3==66) ) {
                                    alt10=1;
                                }
                                else if ( ((LA10_3>=COMMA && LA10_3<=QUOTE)) ) {
                                    alt10=2;
                                }


                            }


                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:379:10: ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:379:10: ( ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+ )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:379:11: ',' arrayArg= ID ( '[' arrayExpr= expression ']' )+
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_moduleClass324); 
                    	    arrayArg=(Token)match(input,ID,FOLLOW_ID_in_moduleClass328); 

                    	    	    		// check aginst globals and other inputs
                    	    	   			if(GlobalConstHashMap.containsKey((arrayArg!=null?arrayArg.getText():null))){
                    	    	   				System.err.println("error on line " + arrayArg.getLine() + ": variable " + (arrayArg!=null?arrayArg.getText():null) + " is already defined as a global constant");
                    	    	   				System.exit(1);
                    	    	   			}
                    	    	   			else if(GlobalVarHashMap.containsKey((arrayArg!=null?arrayArg.getText():null))){
                    	    	   				System.err.println("error on line " + arrayArg.getLine() + ": variable " + (arrayArg!=null?arrayArg.getText():null) + " is already defined as a global variable");
                    	    	   				System.exit(1);
                    	    	   			}
                    	    	   			else if(VarNodeMap.containsKey((arrayArg!=null?arrayArg.getText():null))){
                    	    	   				System.err.println("error on line " + arrayArg.getLine() + ": variable " + (arrayArg!=null?arrayArg.getText():null) + " is already defined");
                    	    	   				System.exit(1);
                    	    	   			}
                    	    	   			
                    	    	   			List<Integer> dimensionList = new ArrayList<Integer>();
                    	    	   		
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:397:6: ( '[' arrayExpr= expression ']' )+
                    	    int cnt9=0;
                    	    loop9:
                    	    do {
                    	        int alt9=2;
                    	        int LA9_0 = input.LA(1);

                    	        if ( (LA9_0==66) ) {
                    	            alt9=1;
                    	        }


                    	        switch (alt9) {
                    	    	case 1 :
                    	    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:397:7: '[' arrayExpr= expression ']'
                    	    	    {
                    	    	    match(input,66,FOLLOW_66_in_moduleClass344); 
                    	    	    pushFollow(FOLLOW_expression_in_moduleClass348);
                    	    	    arrayExpr=expression();

                    	    	    state._fsp--;


                    	    	        			dimensionList.add((arrayExpr!=null?arrayExpr.value:0));
                    	    	        		
                    	    	    match(input,67,FOLLOW_67_in_moduleClass364); 

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


                    	        			createInputArray((arrayArg!=null?arrayArg.getText():null), dimensionList);
                    	        		

                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:405:10: ( ',' arg= ID )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:405:10: ( ',' arg= ID )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:405:11: ',' arg= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_moduleClass387); 
                    	    arg=(Token)match(input,ID,FOLLOW_ID_in_moduleClass391); 

                    	        			// check aginst globals and other inputs
                    	        			if(GlobalConstHashMap.containsKey((arg!=null?arg.getText():null))){
                    	        				System.err.println("error on line " + arg.getLine() + ": variable " + (arg!=null?arg.getText():null) + " is already defined as a global constant");
                    	        				System.exit(1);
                    	        			}
                    	        			else if(GlobalVarHashMap.containsKey((arg!=null?arg.getText():null))){
                    	        				System.err.println("error on line " + arg.getLine() + ": variable " + (arg!=null?arg.getText():null) + " is already defined as a global variable");
                    	        				System.exit(1);
                    	        			}
                    	        			else if(VarNodeMap.containsKey((arg!=null?arg.getText():null))){
                    	        				System.err.println("error on line " + arg.getLine() + ": variable " + (arg!=null?arg.getText():null) + " is already defined");
                    	        				System.exit(1);
                    	        			}
                    	        			
                    	        			// add variable and value to state vector
                    	    				StatevectorMap.put(arg.getText(), 0);
                    	    				
                    	    				// generate variable index and create new var node  
                    	    				int index = VariableIndex++;
                    	       				VarIndexMap.insert(arg.getText(), index);
                    	       				
                    	       				VarNode inputVarNode = new VarNode(arg.getText(), index);
                    	       				inputVarNode.setType(platu.lpn.VarType.INPUT);
                    	       				VarNodeMap.put(arg.getText(), inputVarNode);
                    	    //    			VarCountMap.put(arg.getText(), 0);
                    	        			
                    	        			argumentList.add(arg.getText());
                    	    				Inputs.add(arg.getText());
                    	        		

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);


                    }
                    break;

            }

            match(input,QUOTE,FOLLOW_QUOTE_in_moduleClass414); 
            match(input,GREATER,FOLLOW_GREATER_in_moduleClass416); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:436:21: ( constants )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==LESS) ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1==69) ) {
                    alt12=1;
                }
            }
            switch (alt12) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:436:21: constants
                    {
                    pushFollow(FOLLOW_constants_in_moduleClass418);
                    constants();

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_variables_in_moduleClass421);
            variables();

            state._fsp--;

            pushFollow(FOLLOW_logic_in_moduleClass423);
            logic2=logic();

            state._fsp--;

            match(input,LESS,FOLLOW_LESS_in_moduleClass425); 
            match(input,68,FOLLOW_68_in_moduleClass427); 
            match(input,GREATER,FOLLOW_GREATER_in_moduleClass429); 

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
            	            
            				lpn = new StateGraph(prj, (modName!=null?modName.getText():null), Inputs, Outputs, Internals, VarNodeMap, (logic2!=null?logic2.lpnTranSet:null), 
            	         			StatevectorMap, initialMarking, null);
            				
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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:477:1: constants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' ;
    public final void constants() throws RecognitionException {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:478:2: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:478:4: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_constants454); 
            match(input,69,FOLLOW_69_in_constants456); 
            match(input,GREATER,FOLLOW_GREATER_in_constants458); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:478:20: (const1= ID '=' val1= INT ';' )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==ID) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:478:21: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_constants463); 
            	    match(input,EQUALS,FOLLOW_EQUALS_in_constants465); 
            	    val1=(Token)match(input,INT,FOLLOW_INT_in_constants469); 

            	    				// make sure constant is not defined as something else
            	    				if(VarNodeMap.containsKey((const1!=null?const1.getText():null))){
            	    					System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " already exists as a variable"); 
            	    					System.exit(1);
            	    				}
            	    				else if(GlobalConstHashMap.containsKey((const1!=null?const1.getText():null))){
            	    				    System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " already exists as a global constant");
            	    				    System.exit(1);
            	    				}
            	    				else if(GlobalVarHashMap.containsKey((const1!=null?const1.getText():null))){
            	                		System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " is already defined as a global variable");
            	                		System.exit(1);
            	                	}
            	    				
            	    				// put will override previous value
            	    				Integer result = ConstHashMap.put((const1!=null?const1.getText():null), Integer.parseInt((val1!=null?val1.getText():null)));
            	    				if(result != null){
            	    					System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " has already been defined");
            	    					System.exit(1);
            	    				}
            	    			
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_constants480); 

            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_constants484); 
            match(input,70,FOLLOW_70_in_constants486); 
            match(input,GREATER,FOLLOW_GREATER_in_constants488); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:504:1: globalConstants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' ;
    public final void globalConstants() throws RecognitionException {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:505:5: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:505:9: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalConstants505); 
            match(input,69,FOLLOW_69_in_globalConstants507); 
            match(input,GREATER,FOLLOW_GREATER_in_globalConstants509); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:505:25: (const1= ID '=' val1= INT ';' )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==ID) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:505:26: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_globalConstants514); 
            	    match(input,EQUALS,FOLLOW_EQUALS_in_globalConstants516); 
            	    val1=(Token)match(input,INT,FOLLOW_INT_in_globalConstants520); 

            	                	// make sure constant has not been defined already
            	                	if(GlobalVarHashMap.containsKey((const1!=null?const1.getText():null))){
            	                		System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " is already defined as a global variable");
            	                		System.exit(1);
            	                	}
            	                	
            	                	// put will override previous value
            	                    Integer result = GlobalConstHashMap.put((const1!=null?const1.getText():null), Integer.parseInt((val1!=null?val1.getText():null)));
            	                    if(result != null){
            	                        System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " has already been defined");
            	                        System.exit(1);
            	                    }
            	                
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalConstants546); 

            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_globalConstants550); 
            match(input,70,FOLLOW_70_in_globalConstants552); 
            match(input,GREATER,FOLLOW_GREATER_in_globalConstants554); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:523:1: globalVariables : '<' 'var' '>' ( globalVarDecl | globalArrayDecl )+ '<' '/var' '>' ;
    public final void globalVariables() throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:524:2: ( '<' 'var' '>' ( globalVarDecl | globalArrayDecl )+ '<' '/var' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:524:4: '<' 'var' '>' ( globalVarDecl | globalArrayDecl )+ '<' '/var' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalVariables568); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_globalVariables570); 
            match(input,GREATER,FOLLOW_GREATER_in_globalVariables572); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:524:18: ( globalVarDecl | globalArrayDecl )+
            int cnt15=0;
            loop15:
            do {
                int alt15=3;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==ID) ) {
                    int LA15_2 = input.LA(2);

                    if ( (LA15_2==EQUALS) ) {
                        alt15=1;
                    }
                    else if ( (LA15_2==66) ) {
                        alt15=2;
                    }


                }


                switch (alt15) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:524:19: globalVarDecl
            	    {
            	    pushFollow(FOLLOW_globalVarDecl_in_globalVariables575);
            	    globalVarDecl();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:524:35: globalArrayDecl
            	    {
            	    pushFollow(FOLLOW_globalArrayDecl_in_globalVariables579);
            	    globalArrayDecl();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt15 >= 1 ) break loop15;
                        EarlyExitException eee =
                            new EarlyExitException(15, input);
                        throw eee;
                }
                cnt15++;
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_globalVariables583); 
            match(input,71,FOLLOW_71_in_globalVariables585); 
            match(input,GREATER,FOLLOW_GREATER_in_globalVariables587); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:527:1: globalVarDecl : var= ID '=' (val= INT | var2= ID ) ';' ;
    public final void globalVarDecl() throws RecognitionException {
        Token var=null;
        Token val=null;
        Token var2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:528:2: (var= ID '=' (val= INT | var2= ID ) ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:528:4: var= ID '=' (val= INT | var2= ID ) ';'
            {
            var=(Token)match(input,ID,FOLLOW_ID_in_globalVarDecl600); 

            				// make sure global variables are consistently defined in each lpn file
            				if(GlobalSize == 0){
            					if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
            						System.err.println("error on line" + var.getLine() + ": " + (var!=null?var.getText():null) + "already exists as a constant"); 
            	                    System.exit(1);
            					}
            					else if(GlobalVarHashMap.containsKey((var!=null?var.getText():null))){
            						System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " has already been defined");
            						System.exit(1);
            					}
            				}
            				else{
            					if(!GlobalVarHashMap.containsKey((var!=null?var.getText():null))){
            						System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is inconsistently defined");
            						System.exit(1);
            					}
            				}
            				
            				GlobalCount++;
            			
            match(input,EQUALS,FOLLOW_EQUALS_in_globalVarDecl610); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:550:7: (val= INT | var2= ID )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==INT) ) {
                alt16=1;
            }
            else if ( (LA16_0==ID) ) {
                alt16=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:550:8: val= INT
                    {
                    val=(Token)match(input,INT,FOLLOW_INT_in_globalVarDecl615); 

                    				// make sure global variables are consistently initialized
                    				int value = Integer.parseInt((val!=null?val.getText():null));
                    				if(GlobalSize == 0){
                    					GlobalVarHashMap.put((var!=null?var.getText():null), value);
                    				}
                    				else{
                    					int globalVal = GlobalVarHashMap.get((var!=null?var.getText():null));
                    					if(globalVal != value){
                    						System.err.println("error on line " + val.getLine() + ": " + (var!=null?var.getText():null) + " is inconsistently assigned");
                    						System.exit(1);
                    					}
                    				}
                    			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:565:5: var2= ID
                    {
                    var2=(Token)match(input,ID,FOLLOW_ID_in_globalVarDecl629); 

                    				// get value of variable
                    				Integer value = null;
                    				if(GlobalConstHashMap.containsKey((var2!=null?var2.getText():null))){
                    					value = GlobalConstHashMap.get((var2!=null?var2.getText():null));
                    				}
                    				else if(GlobalVarHashMap.containsKey((var2!=null?var2.getText():null))){
                    					System.err.println("error on line " + var2.getLine() + ": global variable " + (var2!=null?var2.getText():null) + " cannot be assigned to global variable " + (var!=null?var.getText():null)); 
                        				System.exit(1);
                    				}
                    				else{
                    					System.err.println("error on line " + var2.getLine() + ": " + (var2!=null?var2.getText():null) + " is not defined"); 
                        				System.exit(1);
                    				}
                    				
                    				// make sure global variable is consitently initialized
                    				if(GlobalSize == 0){
                    					GlobalVarHashMap.put((var!=null?var.getText():null), value);
                    				}
                    				else{
                    					int globalVal = GlobalVarHashMap.get((var!=null?var.getText():null));
                    					if(globalVal != value){
                    						System.err.println("error on line " + var2.getLine() + ": " + (var!=null?var.getText():null) + " is inconsistently assigned");
                    						System.exit(1);
                    					}
                    				}
                    			

                    }
                    break;

            }

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalVarDecl640); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:596:1: globalArrayDecl : arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ ';' ;
    public final void globalArrayDecl() throws RecognitionException {
        Token arrayVar=null;
        PlatuInstParser.expression_return arrayExpr = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:597:2: (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:597:4: arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ ';'
            {
            arrayVar=(Token)match(input,ID,FOLLOW_ID_in_globalArrayDecl654); 

            				List<Integer> dimensionList = new ArrayList<Integer>();
            				
            				// make sure global variables are consistently defined in each lpn file
            				if(GlobalSize == 0){
            					if(GlobalConstHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
            						System.err.println("error on line" + arrayVar.getLine() + ": " + (arrayVar!=null?arrayVar.getText():null) + "already exists as a constant"); 
            	                    System.exit(1);
            					}
            					else if(GlobalVarHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
            						System.err.println("error on line " + arrayVar.getLine() + ": " + (arrayVar!=null?arrayVar.getText():null) + " has already been defined");
            						System.exit(1);
            					}
            				}
            				else{
            					if(!GlobalVarHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
            						System.err.println("error on line " + arrayVar.getLine() + ": " + (arrayVar!=null?arrayVar.getText():null) + " is inconsistently defined");
            						System.exit(1);
            					}
            				}
            				
            				GlobalCount++;
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:621:3: ( '[' (arrayExpr= expression ) ']' )+
            int cnt17=0;
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==66) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:621:4: '[' (arrayExpr= expression ) ']'
            	    {
            	    match(input,66,FOLLOW_66_in_globalArrayDecl664); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:621:8: (arrayExpr= expression )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:621:9: arrayExpr= expression
            	    {
            	    pushFollow(FOLLOW_expression_in_globalArrayDecl669);
            	    arrayExpr=expression();

            	    state._fsp--;


            	    				dimensionList.add((arrayExpr!=null?arrayExpr.value:0));
            	    			

            	    }

            	    match(input,67,FOLLOW_67_in_globalArrayDecl681); 

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

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalArrayDecl685); 

            				// make sure global variables are consistently initialized
            				if(GlobalSize == 0){
            					GlobalVarHashMap.put((arrayVar!=null?arrayVar.getText():null), 0);
            					GlobalVarNodeMap.put((arrayVar!=null?arrayVar.getText():null), new ArrayNode((arrayVar!=null?arrayVar.getText():null), null, dimensionList.size(), dimensionList, null));
            				}
            				else{
            					ArrayNode node = (ArrayNode) GlobalVarNodeMap.get((arrayVar!=null?arrayVar.getText():null));
            					if(node.getDimensions() != dimensionList.size()){
            						error("error on line " + arrayVar.getLine() + ": " + (arrayVar!=null?arrayVar.getText():null) + " is inconsistently assigned");
            					}
            					
            					List<Integer> dimList = node.getDimensionList();
            					for(int i = 0; i < dimensionList.size(); i++){
            						if(dimList.get(i) != dimensionList.get(i)){
            							error("error on line " + arrayVar.getLine() + ": " + (arrayVar!=null?arrayVar.getText():null) + " is inconsistently assigned");
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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:647:1: variables : '<' 'var' '>' ( varDecl | arrayDecl )+ '<' '/var' '>' ;
    public final void variables() throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:648:2: ( '<' 'var' '>' ( varDecl | arrayDecl )+ '<' '/var' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:648:4: '<' 'var' '>' ( varDecl | arrayDecl )+ '<' '/var' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_variables701); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_variables703); 
            match(input,GREATER,FOLLOW_GREATER_in_variables705); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:648:18: ( varDecl | arrayDecl )+
            int cnt18=0;
            loop18:
            do {
                int alt18=3;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==ID) ) {
                    int LA18_2 = input.LA(2);

                    if ( (LA18_2==EQUALS) ) {
                        alt18=1;
                    }
                    else if ( (LA18_2==66) ) {
                        alt18=2;
                    }


                }


                switch (alt18) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:648:20: varDecl
            	    {
            	    pushFollow(FOLLOW_varDecl_in_variables709);
            	    varDecl();

            	    state._fsp--;


            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:648:30: arrayDecl
            	    {
            	    pushFollow(FOLLOW_arrayDecl_in_variables713);
            	    arrayDecl();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt18 >= 1 ) break loop18;
                        EarlyExitException eee =
                            new EarlyExitException(18, input);
                        throw eee;
                }
                cnt18++;
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_variables717); 
            match(input,71,FOLLOW_71_in_variables719); 
            match(input,GREATER,FOLLOW_GREATER_in_variables721); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:651:1: varDecl : var= ID '=' (val= INT | var2= ID ) ';' ;
    public final void varDecl() throws RecognitionException {
        Token var=null;
        Token val=null;
        Token var2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:652:2: (var= ID '=' (val= INT | var2= ID ) ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:652:4: var= ID '=' (val= INT | var2= ID ) ';'
            {
            Integer value = null; Token varNode = null;
            var=(Token)match(input,ID,FOLLOW_ID_in_varDecl738); 

            				// check variable is unique in scope
            				if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
            					System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is a global constant"); 
                				System.exit(1);
            				}
            				else if(GlobalVarHashMap.containsKey((var!=null?var.getText():null))){
            					System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is a global variable"); 
                				System.exit(1);
            				}
            				else if(VarNodeMap.containsKey((var!=null?var.getText():null))){
            					System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " has already been defined");
            					System.exit(1);
            				}
            				
            				varNode = var;
            			
            match(input,EQUALS,FOLLOW_EQUALS_in_varDecl748); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:671:7: (val= INT | var2= ID )
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==INT) ) {
                alt19=1;
            }
            else if ( (LA19_0==ID) ) {
                alt19=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:671:8: val= INT
                    {
                    val=(Token)match(input,INT,FOLLOW_INT_in_varDecl753); 

                    				// get variable initial value
                    				value = Integer.parseInt((val!=null?val.getText():null));
                    			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:676:5: var2= ID
                    {
                    var2=(Token)match(input,ID,FOLLOW_ID_in_varDecl767); 

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
                    					System.err.println("error on line " + var2.getLine() + ": " + (var2!=null?var2.getText():null) + " is not defined or is not compatible"); 
                        				System.exit(1);
                    				}
                    				
                    				varNode = var2;
                    			

                    }
                    break;

            }


            				// add variable and value to state vector
            				StatevectorMap.put(varNode.getText(), value);
            				
            				// generate variable index and create new var node  
            				int index = VariableIndex++;
               				VarIndexMap.insert(varNode.getText(), index);
               				
               				VarNode internalVar = new VarNode(varNode.getText(), index);
               				internalVar.setType(platu.lpn.VarType.INTERNAL);
               				VarNodeMap.put(varNode.getText(), internalVar);
                			VarCountMap.put(varNode.getText(), 0);
                			
            				Internals.add(varNode.getText());
            			
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_varDecl786); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:717:1: arrayDecl : var= ID ( '[' (arrayExpr= expression ) ']' )+ ( '=' '{' (val2= INT | var2= ID )+ '}' )? ';' ;
    public final void arrayDecl() throws RecognitionException {
        Token var=null;
        Token val2=null;
        Token var2=null;
        PlatuInstParser.expression_return arrayExpr = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:718:2: (var= ID ( '[' (arrayExpr= expression ) ']' )+ ( '=' '{' (val2= INT | var2= ID )+ '}' )? ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:718:4: var= ID ( '[' (arrayExpr= expression ) ']' )+ ( '=' '{' (val2= INT | var2= ID )+ '}' )? ';'
            {
            var=(Token)match(input,ID,FOLLOW_ID_in_arrayDecl800); 

            				List<Integer> dimensionList = new ArrayList<Integer>();
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:722:3: ( '[' (arrayExpr= expression ) ']' )+
            int cnt20=0;
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);

                if ( (LA20_0==66) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:722:4: '[' (arrayExpr= expression ) ']'
            	    {
            	    match(input,66,FOLLOW_66_in_arrayDecl811); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:722:8: (arrayExpr= expression )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:722:9: arrayExpr= expression
            	    {
            	    pushFollow(FOLLOW_expression_in_arrayDecl816);
            	    arrayExpr=expression();

            	    state._fsp--;


            	    				dimensionList.add((arrayExpr!=null?arrayExpr.value:0));
            	    			

            	    }

            	    match(input,67,FOLLOW_67_in_arrayDecl827); 

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
            						element.setType(platu.lpn.VarType.INTERNAL);
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
            				newArray.setType(platu.lpn.VarType.INTERNAL);
            				VarNodeMap.put((var!=null?var.getText():null), newArray);
                			VarCountMap.put((var!=null?var.getText():null), 0);
            				Internals.add((var!=null?var.getText():null));
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:785:3: ( '=' '{' (val2= INT | var2= ID )+ '}' )?
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==EQUALS) ) {
                alt22=1;
            }
            switch (alt22) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:785:4: '=' '{' (val2= INT | var2= ID )+ '}'
                    {
                    match(input,EQUALS,FOLLOW_EQUALS_in_arrayDecl839); 

                    				List<Integer> valueList = new ArrayList<Integer>();
                    			
                    match(input,72,FOLLOW_72_in_arrayDecl849); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:789:7: (val2= INT | var2= ID )+
                    int cnt21=0;
                    loop21:
                    do {
                        int alt21=3;
                        int LA21_0 = input.LA(1);

                        if ( (LA21_0==INT) ) {
                            alt21=1;
                        }
                        else if ( (LA21_0==ID) ) {
                            alt21=2;
                        }


                        switch (alt21) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:789:8: val2= INT
                    	    {
                    	    val2=(Token)match(input,INT,FOLLOW_INT_in_arrayDecl854); 

                    	    				Integer dimVal = Integer.parseInt((val2!=null?val2.getText():null));
                    	    				if(dimVal < 1){
                    	    					error("error on line " + val2.getLine() + ": invalid dimension");
                    	    				}
                    	    				
                    	    				valueList.add(dimVal);
                    	    			

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:798:5: var2= ID
                    	    {
                    	    var2=(Token)match(input,ID,FOLLOW_ID_in_arrayDecl868); 

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
                    	    					System.err.println("error on line " + var2.getLine() + ": " + (var2!=null?var2.getText():null) + " is not defined"); 
                    	        				System.exit(1);
                    	    				}
                    	    				
                    	    				if(initVal < 1){
                    	    					error("error on line " + var2.getLine() + ": invalid dimension");
                    	    				}
                    	    				
                    	    				valueList.add(initVal);
                    	    			

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt21 >= 1 ) break loop21;
                                EarlyExitException eee =
                                    new EarlyExitException(21, input);
                                throw eee;
                        }
                        cnt21++;
                    } while (true);

                    match(input,73,FOLLOW_73_in_arrayDecl880); 

                    				//TODO: initialize array
                    				int dimensions = dimensionList.size();
                    			

                    }
                    break;

            }

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_arrayDecl892); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:834:1: instantiation : (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+ ;
    public final void instantiation() throws RecognitionException {
        Token modName=null;
        Token instName=null;
        Token var=null;
        Token var2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:835:5: ( (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+ )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:835:7: (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:835:7: (modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';' )+
            int cnt25=0;
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);

                if ( (LA25_0==ID) ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:835:8: modName= ID instName= ID '(' ( (var= MEMBER ',' )* var2= MEMBER )? ')' ';'
            	    {
            	    modName=(Token)match(input,ID,FOLLOW_ID_in_instantiation910); 
            	    instName=(Token)match(input,ID,FOLLOW_ID_in_instantiation914); 

            	        			List<String> argList = new ArrayList<String>();
            	        			List<String> modList = new ArrayList<String>();
            	        		
            	    match(input,LPAREN,FOLLOW_LPAREN_in_instantiation930); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:840:9: ( (var= MEMBER ',' )* var2= MEMBER )?
            	    int alt24=2;
            	    int LA24_0 = input.LA(1);

            	    if ( (LA24_0==MEMBER) ) {
            	        alt24=1;
            	    }
            	    switch (alt24) {
            	        case 1 :
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:840:10: (var= MEMBER ',' )* var2= MEMBER
            	            {
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:840:10: (var= MEMBER ',' )*
            	            loop23:
            	            do {
            	                int alt23=2;
            	                int LA23_0 = input.LA(1);

            	                if ( (LA23_0==MEMBER) ) {
            	                    int LA23_1 = input.LA(2);

            	                    if ( (LA23_1==COMMA) ) {
            	                        alt23=1;
            	                    }


            	                }


            	                switch (alt23) {
            	            	case 1 :
            	            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:840:11: var= MEMBER ','
            	            	    {
            	            	    var=(Token)match(input,MEMBER,FOLLOW_MEMBER_in_instantiation935); 
            	            	    match(input,COMMA,FOLLOW_COMMA_in_instantiation937); 

            	            	        			String buffer = (var!=null?var.getText():null);
            	            	            		StringTokenizer tk = new StringTokenizer(buffer, ".");
            	            	            		
            	            	        			String module = tk.nextToken();
            	            	        			String variable = tk.nextToken();
            	            	        			
            	            	        			modList.add(module);
            	            	        			argList.add(module + "." + variable);
            	            	        		

            	            	    }
            	            	    break;

            	            	default :
            	            	    break loop23;
            	                }
            	            } while (true);

            	            var2=(Token)match(input,MEMBER,FOLLOW_MEMBER_in_instantiation957); 

            	                			String buffer = (var2!=null?var2.getText():null);
            	                    		StringTokenizer tk = new StringTokenizer(buffer, ".");
            	                    		
            	                			String module = tk.nextToken();
            	                			String variable = tk.nextToken();
            	                			
            	                			modList.add(module);
            	                			argList.add(module + "." + variable);	
            	                		

            	            }
            	            break;

            	    }

            	    match(input,RPAREN,FOLLOW_RPAREN_in_instantiation975); 
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_instantiation977); 

            	        			Instance inst = new Instance((modName!=null?modName.getText():null), (instName!=null?instName.getText():null), argList, modList);
            	        			InstanceList.add(inst);
            	        		

            	    }
            	    break;

            	default :
            	    if ( cnt25 >= 1 ) break loop25;
                        EarlyExitException eee =
                            new EarlyExitException(25, input);
                        throw eee;
                }
                cnt25++;
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
    };

    // $ANTLR start "logic"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:870:1: logic returns [List<Integer> initMarking, LPNTranSet lpnTranSet] : marking ( transition )+ ;
    public final PlatuInstParser.logic_return logic() throws RecognitionException {
        PlatuInstParser.logic_return retval = new PlatuInstParser.logic_return();
        retval.start = input.LT(1);

        LPNTran transition3 = null;

        List<Integer> marking4 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:871:5: ( marking ( transition )+ )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:871:9: marking ( transition )+
            {
            retval.lpnTranSet = new LpnTranList();
            pushFollow(FOLLOW_marking_in_logic1023);
            marking4=marking();

            state._fsp--;

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:872:14: ( transition )+
            int cnt26=0;
            loop26:
            do {
                int alt26=2;
                int LA26_0 = input.LA(1);

                if ( (LA26_0==LESS) ) {
                    int LA26_1 = input.LA(2);

                    if ( (LA26_1==TRANSITION) ) {
                        alt26=1;
                    }


                }


                switch (alt26) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:872:15: transition
            	    {
            	    pushFollow(FOLLOW_transition_in_logic1026);
            	    transition3=transition();

            	    state._fsp--;

            	    retval.lpnTranSet.add(transition3);

            	    }
            	    break;

            	default :
            	    if ( cnt26 >= 1 ) break loop26;
                        EarlyExitException eee =
                            new EarlyExitException(26, input);
                        throw eee;
                }
                cnt26++;
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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:878:1: marking returns [List<Integer> mark] : ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )? ;
    public final List<Integer> marking() throws RecognitionException {
        List<Integer> mark = null;

        Token m1=null;
        Token c1=null;
        Token m2=null;
        Token c2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:879:5: ( ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )? )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:879:9: ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )?
            {
            mark = new LinkedList<Integer>(); Integer result;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:880:9: ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>' )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==LESS) ) {
                int LA31_1 = input.LA(2);

                if ( (LA31_1==MARKING) ) {
                    alt31=1;
                }
            }
            switch (alt31) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:880:10: '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/marking' '>'
                    {
                    match(input,LESS,FOLLOW_LESS_in_marking1079); 
                    match(input,MARKING,FOLLOW_MARKING_in_marking1081); 
                    match(input,GREATER,FOLLOW_GREATER_in_marking1083); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:880:28: ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )?
                    int alt30=2;
                    int LA30_0 = input.LA(1);

                    if ( ((LA30_0>=ID && LA30_0<=INT)) ) {
                        alt30=1;
                    }
                    switch (alt30) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:880:29: (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )*
                            {
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:880:29: (m1= INT | c1= ID )
                            int alt27=2;
                            int LA27_0 = input.LA(1);

                            if ( (LA27_0==INT) ) {
                                alt27=1;
                            }
                            else if ( (LA27_0==ID) ) {
                                alt27=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 27, 0, input);

                                throw nvae;
                            }
                            switch (alt27) {
                                case 1 :
                                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:880:30: m1= INT
                                    {
                                    m1=(Token)match(input,INT,FOLLOW_INT_in_marking1089); 

                                            		mark.add(Integer.parseInt((m1!=null?m1.getText():null)));
                                            	

                                    }
                                    break;
                                case 2 :
                                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:884:11: c1= ID
                                    {
                                    c1=(Token)match(input,ID,FOLLOW_ID_in_marking1115); 

                                            		result = ConstHashMap.get((c1!=null?c1.getText():null));
                                            		if(result == null){
                                            			System.err.println("error on line " + c1.getLine() + ": " + (c1!=null?c1.getText():null) + " is not a valid constant");
                                            			System.exit(1);
                                            		}
                                            		
                                            		mark.add(result);
                                            	

                                    }
                                    break;

                            }

                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:894:11: ( ',' (m2= INT | c2= ID ) )*
                            loop29:
                            do {
                                int alt29=2;
                                int LA29_0 = input.LA(1);

                                if ( (LA29_0==COMMA) ) {
                                    alt29=1;
                                }


                                switch (alt29) {
                            	case 1 :
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:894:12: ',' (m2= INT | c2= ID )
                            	    {
                            	    match(input,COMMA,FOLLOW_COMMA_in_marking1139); 
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:894:16: (m2= INT | c2= ID )
                            	    int alt28=2;
                            	    int LA28_0 = input.LA(1);

                            	    if ( (LA28_0==INT) ) {
                            	        alt28=1;
                            	    }
                            	    else if ( (LA28_0==ID) ) {
                            	        alt28=2;
                            	    }
                            	    else {
                            	        NoViableAltException nvae =
                            	            new NoViableAltException("", 28, 0, input);

                            	        throw nvae;
                            	    }
                            	    switch (alt28) {
                            	        case 1 :
                            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:894:17: m2= INT
                            	            {
                            	            m2=(Token)match(input,INT,FOLLOW_INT_in_marking1144); 

                            	                    		mark.add(Integer.parseInt((m2!=null?m2.getText():null)));
                            	                    	

                            	            }
                            	            break;
                            	        case 2 :
                            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:898:11: c2= ID
                            	            {
                            	            c2=(Token)match(input,ID,FOLLOW_ID_in_marking1170); 

                            	                    		result = ConstHashMap.get((c2!=null?c2.getText():null));
                            	                    		if(result == null){
                            	                    			System.err.println("error on line " + c2.getLine() + ": " + (c2!=null?c2.getText():null) + " is not a valid constant");
                            	                    			System.exit(1);
                            	                    		}
                            	                    		
                            	                    		mark.add(result);
                            	                    	

                            	            }
                            	            break;

                            	    }


                            	    }
                            	    break;

                            	default :
                            	    break loop29;
                                }
                            } while (true);


                            }
                            break;

                    }

                    match(input,LESS,FOLLOW_LESS_in_marking1197); 
                    match(input,74,FOLLOW_74_in_marking1199); 
                    match(input,GREATER,FOLLOW_GREATER_in_marking1201); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:911:1: transition returns [LPNTran lpnTran] : '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/transition' '>' ;
    public final LPNTran transition() throws RecognitionException {
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
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:912:5: ( '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/transition' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:912:10: '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/transition' '>'
            {

            	    		Integer result = null;
            	    		ArrayList presetList = new ArrayList();  
            	    		ArrayList postsetList = new ArrayList(); 
            	    		VarExprList assignmentList = new VarExprList();
            	    		ArrayList<Expression> assertionList = new ArrayList<Expression>();
            	    		Expression guardExpr = TrueExpr; 
            	    		int delayLB = 0; 
            	    		int delayUB = INFINITY;
            	    		boolean local = true;
            	    	
            match(input,LESS,FOLLOW_LESS_in_transition1235); 
            match(input,TRANSITION,FOLLOW_TRANSITION_in_transition1237); 
            match(input,LABEL,FOLLOW_LABEL_in_transition1239); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1241); 
            match(input,QUOTE,FOLLOW_QUOTE_in_transition1243); 
            lbl=(Token)input.LT(1);
            if ( (input.LA(1)>=ID && input.LA(1)<=INT) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            match(input,QUOTE,FOLLOW_QUOTE_in_transition1253); 
            match(input,PRESET,FOLLOW_PRESET_in_transition1255); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1257); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:923:69: ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) )
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==QUOTE) ) {
                int LA34_1 = input.LA(2);

                if ( (LA34_1==QUOTE) ) {
                    alt34=1;
                }
                else if ( ((LA34_1>=ID && LA34_1<=INT)) ) {
                    alt34=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 34, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:923:70: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1260); 
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1262); 

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:923:80: ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:923:80: ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:923:81: '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1267); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:923:85: (pre= INT | pre1= ID )
                    int alt32=2;
                    int LA32_0 = input.LA(1);

                    if ( (LA32_0==INT) ) {
                        alt32=1;
                    }
                    else if ( (LA32_0==ID) ) {
                        alt32=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 32, 0, input);

                        throw nvae;
                    }
                    switch (alt32) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:923:86: pre= INT
                            {
                            pre=(Token)match(input,INT,FOLLOW_INT_in_transition1272); 

                                			presetList.add(Integer.parseInt((pre!=null?pre.getText():null)));
                               			

                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:927:7: pre1= ID
                            {
                            pre1=(Token)match(input,ID,FOLLOW_ID_in_transition1292); 

                              				result = ConstHashMap.get((pre1!=null?pre1.getText():null)); 
                              				if(result == null){
                              					System.err.println("error on line " + pre1.getLine() + ": " + (pre1!=null?pre1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				
                              				presetList.add(result);
                              			

                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:937:6: ( ',' pre2= INT | ',' pre3= ID )*
                    loop33:
                    do {
                        int alt33=3;
                        int LA33_0 = input.LA(1);

                        if ( (LA33_0==COMMA) ) {
                            int LA33_2 = input.LA(2);

                            if ( (LA33_2==INT) ) {
                                alt33=1;
                            }
                            else if ( (LA33_2==ID) ) {
                                alt33=2;
                            }


                        }


                        switch (alt33) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:937:8: ',' pre2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1308); 
                    	    pre2=(Token)match(input,INT,FOLLOW_INT_in_transition1312); 

                    	        			presetList.add(Integer.parseInt((pre2!=null?pre2.getText():null)));
                    	       			

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:941:7: ',' pre3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1330); 
                    	    pre3=(Token)match(input,ID,FOLLOW_ID_in_transition1334); 

                    	      				result = ConstHashMap.get((pre3!=null?pre3.getText():null)); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + pre3.getLine() + ": " + (pre3!=null?pre3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}
                    	      				
                    	      				presetList.add(result);
                    	      			

                    	    }
                    	    break;

                    	default :
                    	    break loop33;
                        }
                    } while (true);

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1349); 

                    }


                    }
                    break;

            }

            match(input,POSTSET,FOLLOW_POSTSET_in_transition1353); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1355); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:951:27: ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==QUOTE) ) {
                int LA37_1 = input.LA(2);

                if ( (LA37_1==QUOTE) ) {
                    alt37=1;
                }
                else if ( ((LA37_1>=ID && LA37_1<=INT)) ) {
                    alt37=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 37, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:951:29: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1359); 
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1361); 

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:951:39: ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:951:39: ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:951:40: '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1366); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:951:44: (post= INT | post1= ID )
                    int alt35=2;
                    int LA35_0 = input.LA(1);

                    if ( (LA35_0==INT) ) {
                        alt35=1;
                    }
                    else if ( (LA35_0==ID) ) {
                        alt35=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 35, 0, input);

                        throw nvae;
                    }
                    switch (alt35) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:951:46: post= INT
                            {
                            post=(Token)match(input,INT,FOLLOW_INT_in_transition1372); 

                                			postsetList.add(Integer.parseInt((post!=null?post.getText():null)));
                                		

                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:955:8: post1= ID
                            {
                            post1=(Token)match(input,ID,FOLLOW_ID_in_transition1392); 

                                			result = ConstHashMap.get((post1!=null?post1.getText():null)); 
                              				if(result == null){
                              					System.err.println("error on line " + post1.getLine() + ": " + (post1!=null?post1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				
                              				postsetList.add(result);
                                		

                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:966:6: ( ( ',' post2= INT ) | ( ',' post3= ID ) )*
                    loop36:
                    do {
                        int alt36=3;
                        int LA36_0 = input.LA(1);

                        if ( (LA36_0==COMMA) ) {
                            int LA36_2 = input.LA(2);

                            if ( (LA36_2==INT) ) {
                                alt36=1;
                            }
                            else if ( (LA36_2==ID) ) {
                                alt36=2;
                            }


                        }


                        switch (alt36) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:966:8: ( ',' post2= INT )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:966:8: ( ',' post2= INT )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:966:9: ',' post2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1417); 
                    	    post2=(Token)match(input,INT,FOLLOW_INT_in_transition1421); 

                    	        			postsetList.add(Integer.parseInt((post2!=null?post2.getText():null)));
                    	        		

                    	    }


                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:970:10: ( ',' post3= ID )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:970:10: ( ',' post3= ID )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:970:11: ',' post3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1442); 
                    	    post3=(Token)match(input,ID,FOLLOW_ID_in_transition1445); 

                    	        			result = ConstHashMap.get((post3!=null?post3.getText():null)); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + post3.getLine() + ": " + (post3!=null?post3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}
                    	      				
                    	      				postsetList.add(result);
                    	        		

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop36;
                        }
                    } while (true);

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1464); 

                    }


                    }
                    break;

            }

            match(input,GREATER,FOLLOW_GREATER_in_transition1469); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:980:21: ( guard )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==77) ) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:980:22: guard
                    {
                    pushFollow(FOLLOW_guard_in_transition1472);
                    guard5=guard();

                    state._fsp--;


                        			guardExpr = guard5;
                        		

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:984:9: ( delay )?
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==78) ) {
                alt39=1;
            }
            switch (alt39) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:984:10: delay
                    {
                    pushFollow(FOLLOW_delay_in_transition1492);
                    delay6=delay();

                    state._fsp--;


                        			delayLB = (delay6!=null?delay6.delayLB:0); 
                        			delayUB = (delay6!=null?delay6.delayUB:0);
                        		

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:989:9: ( ( assertion ) | ( assignment ) )*
            loop40:
            do {
                int alt40=3;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==76) ) {
                    alt40=1;
                }
                else if ( (LA40_0==ID) ) {
                    alt40=2;
                }


                switch (alt40) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:989:10: ( assertion )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:989:10: ( assertion )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:989:11: assertion
            	    {
            	    pushFollow(FOLLOW_assertion_in_transition1513);
            	    assertion7=assertion();

            	    state._fsp--;


            	        			if(assertion7 != null){		
            	      					assertionList.add(assertion7);
            	      				}
            	        		

            	    }


            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:995:10: ( assignment )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:995:10: ( assignment )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:995:11: assignment
            	    {
            	    pushFollow(FOLLOW_assignment_in_transition1533);
            	    assignment8=assignment();

            	    state._fsp--;


            	        			assignmentList.add(assignment8);
            	        		

            	    }


            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_transition1553); 
            match(input,75,FOLLOW_75_in_transition1555); 
            match(input,GREATER,FOLLOW_GREATER_in_transition1557); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1009:1: assertion returns [Expression booleanExpr] : 'assert' '(' expression ')' ';' ;
    public final Expression assertion() throws RecognitionException {
        Expression booleanExpr = null;

        PlatuInstParser.expression_return expression9 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1010:2: ( 'assert' '(' expression ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1010:4: 'assert' '(' expression ')' ';'
            {
            booleanExpr = null;
            match(input,76,FOLLOW_76_in_assertion1591); 
            match(input,LPAREN,FOLLOW_LPAREN_in_assertion1593); 
            pushFollow(FOLLOW_expression_in_assertion1595);
            expression9=expression();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_assertion1597); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assertion1599); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1017:1: guard returns [Expression expr] : 'condition' '(' expression ')' ';' ;
    public final Expression guard() throws RecognitionException {
        Expression expr = null;

        PlatuInstParser.expression_return expression10 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1018:5: ( 'condition' '(' expression ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1018:8: 'condition' '(' expression ')' ';'
            {
            match(input,77,FOLLOW_77_in_guard1624); 
            match(input,LPAREN,FOLLOW_LPAREN_in_guard1626); 
            pushFollow(FOLLOW_expression_in_guard1628);
            expression10=expression();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_guard1630); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_guard1632); 

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
    };

    // $ANTLR start "delay"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1024:1: delay returns [int delayLB, int delayUB] : 'delay' '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' ;
    public final PlatuInstParser.delay_return delay() throws RecognitionException {
        PlatuInstParser.delay_return retval = new PlatuInstParser.delay_return();
        retval.start = input.LT(1);

        Token lb=null;
        Token ub=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1025:5: ( 'delay' '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1025:8: 'delay' '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';'
            {
            match(input,78,FOLLOW_78_in_delay1666); 
            match(input,LPAREN,FOLLOW_LPAREN_in_delay1668); 
            lb=(Token)match(input,INT,FOLLOW_INT_in_delay1672); 

                			retval.delayLB = Integer.parseInt((lb!=null?lb.getText():null));
               			
            match(input,COMMA,FOLLOW_COMMA_in_delay1686); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1029:8: (ub= INT | 'inf' )
            int alt41=2;
            int LA41_0 = input.LA(1);

            if ( (LA41_0==INT) ) {
                alt41=1;
            }
            else if ( (LA41_0==79) ) {
                alt41=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 41, 0, input);

                throw nvae;
            }
            switch (alt41) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1029:9: ub= INT
                    {
                    ub=(Token)match(input,INT,FOLLOW_INT_in_delay1691); 

                     				retval.delayUB = Integer.parseInt((ub!=null?ub.getText():null));
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
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1046:6: 'inf'
                    {
                    match(input,79,FOLLOW_79_in_delay1706); 

                     				retval.delayUB = INFINITY;
                    			

                    }
                    break;

            }

            match(input,RPAREN,FOLLOW_RPAREN_in_delay1719); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_delay1721); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1053:1: assignment returns [VarExpr assign] : ( (var1= ID '=' var2= ID ) | (var= ID '=' varExpr= expression ';' ) | (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' ) );
    public final VarExpr assignment() throws RecognitionException {
        VarExpr assign = null;

        Token var1=null;
        Token var2=null;
        Token var=null;
        Token arrayVar=null;
        PlatuInstParser.expression_return varExpr = null;

        PlatuInstParser.expression_return arrayExpr = null;

        PlatuInstParser.expression_return assignExpr = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1054:5: ( (var1= ID '=' var2= ID ) | (var= ID '=' varExpr= expression ';' ) | (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' ) )
            int alt43=3;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==ID) ) {
                int LA43_1 = input.LA(2);

                if ( (LA43_1==EQUALS) ) {
                    int LA43_2 = input.LA(3);

                    if ( (LA43_2==ID) ) {
                        switch ( input.LA(4) ) {
                        case LESS:
                            {
                            int LA43_6 = input.LA(5);

                            if ( (LA43_6==75) ) {
                                alt43=1;
                            }
                            else if ( ((LA43_6>=ID && LA43_6<=INT)||LA43_6==LPAREN||(LA43_6>=TRUE && LA43_6<=FALSE)||(LA43_6>=PLUS && LA43_6<=MINUS)||LA43_6==NEGATION||LA43_6==BITWISE_NEGATION) ) {
                                alt43=2;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 43, 6, input);

                                throw nvae;
                            }
                            }
                            break;
                        case ID:
                        case 76:
                            {
                            alt43=1;
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
                        case 66:
                            {
                            alt43=2;
                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 43, 4, input);

                            throw nvae;
                        }

                    }
                    else if ( (LA43_2==INT||LA43_2==LPAREN||(LA43_2>=TRUE && LA43_2<=FALSE)||(LA43_2>=PLUS && LA43_2<=MINUS)||LA43_2==NEGATION||LA43_2==BITWISE_NEGATION) ) {
                        alt43=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 43, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA43_1==66) ) {
                    alt43=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 43, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1054:9: (var1= ID '=' var2= ID )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1054:9: (var1= ID '=' var2= ID )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1054:10: var1= ID '=' var2= ID
                    {
                    var1=(Token)match(input,ID,FOLLOW_ID_in_assignment1747); 
                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1749); 
                    var2=(Token)match(input,ID,FOLLOW_ID_in_assignment1753); 

                    }


                        			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey((var1!=null?var1.getText():null))){
                        				System.err.println("error on line " + var1.getLine() + ": global constant " + (var1!=null?var1.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey((var1!=null?var1.getText():null))){
                        				System.err.println("error on line " + var1.getLine() + ": constant " + (var1!=null?var1.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!VarNodeMap.containsKey((var1!=null?var1.getText():null))){
                        				System.err.println("error on line " + var1.getLine() + ": variable " + (var1!=null?var1.getText():null) + " was not declared");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains((var1!=null?var1.getText():null)) && !Internals.contains((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var1.getLine() + ": input variable " + (var1!=null?var1.getText():null) + " cannot be assigned");
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
                        				System.err.println("error on line " + var2.getLine() + ": variable " + (var2!=null?var2.getText():null) + " was not declared");
                        				System.exit(1);
                        			}
                        			else{
                        				node2 = VarNodeMap.get((var2!=null?var2.getText():null));
                        			}
                    	    		
                    	    		VarNode node1 = VarNodeMap.get((var1!=null?var1.getText():null));
                    	    		if(ArrayNode.class.isAssignableFrom(node1.getClass())){
                    	    			if(!ArrayNode.class.isAssignableFrom(node2.getClass())){
                    	   					System.err.println("error on line " + var.getLine() + ": variable " + (var!=null?var.getText():null) + " is an array");
                    	   					System.exit(1);
                       					}
                       					
                       					ArrayNode arrayNode1 = (ArrayNode) node1;
                       					ArrayNode arrayNode2 = (ArrayNode) node2;
                       					
                       					List<Integer> dimensionList1 = arrayNode1.getDimensionList();
                       					List<Integer> dimensionList2 = arrayNode2.getDimensionList();
                       					
                       					if(dimensionList1.size() != dimensionList2.size()){
                       						System.err.println("error on line " + var1.getLine() + ": incompatible array dimensions");
                       						System.exit(1);
                       					}
                       					
                       					for(int i = 0; i < dimensionList1.size(); i++){
                       						if(dimensionList1.get(i) != dimensionList2.get(i)){
                       							System.err.println("error on line " + var1.getLine() + ": incompatible array dimensions");
                       							System.exit(1);
                       						}
                       					}
                       					
                       					//TODO: array to array assignment
                       					
                       				}else if(ArrayNode.class.isAssignableFrom(node2.getClass())){
                       					System.err.println("error on line " + var2.getLine() + ": variable " + (var2!=null?var2.getText():null) + " is an array");
                       					System.exit(1);
                       				}
                       				else{
                       					// regular assignment
                       					Expression expr = new Expression(node2);
                    	    			assign = new VarExpr(node1, expr);
                       				}
                       				
                       				if(node1.getType() == platu.lpn.VarType.INTERNAL || node1.getType() == platu.lpn.VarType.OUTPUT){
                    	   				Integer varCount = VarCountMap.get((var1!=null?var1.getText():null));
                    		    		VarCountMap.put((var1!=null?var1.getText():null), ++varCount);
                    	    		}
                        		

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1134:8: (var= ID '=' varExpr= expression ';' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1134:8: (var= ID '=' varExpr= expression ';' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1134:9: var= ID '=' varExpr= expression ';'
                    {
                    var=(Token)match(input,ID,FOLLOW_ID_in_assignment1775); 
                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1777); 
                    	
                        			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var.getLine() + ": global constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var.getLine() + ": constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!VarNodeMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var.getLine() + ": variable " + (var!=null?var.getText():null) + " was not declared");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains((var!=null?var.getText():null)) && !Internals.contains((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var.getLine() + ": input variable " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        		
                    pushFollow(FOLLOW_expression_in_assignment1795);
                    varExpr=expression();

                    state._fsp--;


                    	    		Expression expr = new Expression((varExpr!=null?varExpr.expr:null));
                    	    		VarNode node = VarNodeMap.get((var!=null?var.getText():null));
                    	    		if(ArrayNode.class.isAssignableFrom(node.getClass())){
                       					System.err.println("error on line " + var.getLine() + ": variable " + (var!=null?var.getText():null) + " is an array");
                       					System.exit(1);
                       				}
                       				
                       				if(node.getType() == platu.lpn.VarType.INTERNAL || node.getType() == platu.lpn.VarType.OUTPUT){
                    	   				Integer varCount = VarCountMap.get((var!=null?var.getText():null));
                    		    		VarCountMap.put((var!=null?var.getText():null), ++varCount);
                    	    		}
                    	    		
                    	    		assign = new VarExpr(node, expr);
                    	   		
                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1810); 

                    }


                    }
                    break;
                case 3 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1170:13: (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1170:13: (arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1170:14: arrayVar= ID ( '[' (arrayExpr= expression ) ']' )+ '=' assignExpr= expression ';'
                    {
                    arrayVar=(Token)match(input,ID,FOLLOW_ID_in_assignment1818); 

                    	   			List<ExpressionNode> indexList = new ArrayList<ExpressionNode>();
                    	   			
                    	   			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + arrayVar.getLine() + ": global constant " + (arrayVar!=null?arrayVar.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + arrayVar.getLine() + ": constant " + (arrayVar!=null?arrayVar.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!VarNodeMap.containsKey((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + arrayVar.getLine() + ": variable " + (arrayVar!=null?arrayVar.getText():null) + " was not declared");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains((arrayVar!=null?arrayVar.getText():null)) && !Internals.contains((arrayVar!=null?arrayVar.getText():null))){
                        				System.err.println("error on line " + arrayVar.getLine() + ": input variable " + (arrayVar!=null?arrayVar.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                    	   		
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1192:6: ( '[' (arrayExpr= expression ) ']' )+
                    int cnt42=0;
                    loop42:
                    do {
                        int alt42=2;
                        int LA42_0 = input.LA(1);

                        if ( (LA42_0==66) ) {
                            alt42=1;
                        }


                        switch (alt42) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1192:7: '[' (arrayExpr= expression ) ']'
                    	    {
                    	    match(input,66,FOLLOW_66_in_assignment1835); 
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1192:11: (arrayExpr= expression )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1192:12: arrayExpr= expression
                    	    {
                    	    pushFollow(FOLLOW_expression_in_assignment1840);
                    	    arrayExpr=expression();

                    	    state._fsp--;


                    	        			ExpressionNode node = (arrayExpr!=null?arrayExpr.expr:null);
                    	    				indexList.add(node);
                    	    	   		

                    	    }

                    	    match(input,67,FOLLOW_67_in_assignment1857); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt42 >= 1 ) break loop42;
                                EarlyExitException eee =
                                    new EarlyExitException(42, input);
                                throw eee;
                        }
                        cnt42++;
                    } while (true);

                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1861); 
                    pushFollow(FOLLOW_expression_in_assignment1865);
                    assignExpr=expression();

                    state._fsp--;


                    	    		Expression expr = new Expression((assignExpr!=null?assignExpr.expr:null));
                    	    		VarNode arrayNode = VarNodeMap.get((arrayVar!=null?arrayVar.getText():null));
                    	    		if(ArrayNode.class.isAssignableFrom(arrayNode.getClass())){
                    	    			assign = new VarExpr(new ArrayElement((ArrayNode) arrayNode, indexList), expr);
                    	    		}
                    	    		else{
                    	    			System.err.println("error on line " + arrayVar.getLine() + ": " + (arrayVar!=null?arrayVar.getText():null) + " is not an array");
                    	    			System.exit(1);
                    	    		}
                    	    		
                    	    		if(arrayNode.getType() == platu.lpn.VarType.INTERNAL || arrayNode.getType() == platu.lpn.VarType.OUTPUT){
                    	   				Integer varCount = VarCountMap.get((arrayVar!=null?arrayVar.getText():null));
                    		    		VarCountMap.put((arrayVar!=null?arrayVar.getText():null), ++varCount);
                    	    		}
                    	   		
                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1880); 

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
    };

    // $ANTLR start "term"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1218:1: term returns [ExpressionNode expr, int value] : (var= ID | (array= ID ( '[' (arrayExpr= expression ) ']' )+ ) | LPAREN expression RPAREN | INT | TRUE | FALSE );
    public final PlatuInstParser.term_return term() throws RecognitionException {
        PlatuInstParser.term_return retval = new PlatuInstParser.term_return();
        retval.start = input.LT(1);

        Token var=null;
        Token array=null;
        Token INT12=null;
        PlatuInstParser.expression_return arrayExpr = null;

        PlatuInstParser.expression_return expression11 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1219:5: (var= ID | (array= ID ( '[' (arrayExpr= expression ) ']' )+ ) | LPAREN expression RPAREN | INT | TRUE | FALSE )
            int alt45=6;
            switch ( input.LA(1) ) {
            case ID:
                {
                int LA45_1 = input.LA(2);

                if ( (LA45_1==RPAREN||(LA45_1>=QMARK && LA45_1<=SEMICOLON)||(LA45_1>=PLUS && LA45_1<=MOD)||(LA45_1>=GREATER && LA45_1<=NOT_EQUIV)||(LA45_1>=AND && LA45_1<=IMPLICATION)||(LA45_1>=BITWISE_AND && LA45_1<=BITWISE_RSHIFT)||LA45_1==67) ) {
                    alt45=1;
                }
                else if ( (LA45_1==66) ) {
                    alt45=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 45, 1, input);

                    throw nvae;
                }
                }
                break;
            case LPAREN:
                {
                alt45=3;
                }
                break;
            case INT:
                {
                alt45=4;
                }
                break;
            case TRUE:
                {
                alt45=5;
                }
                break;
            case FALSE:
                {
                alt45=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }

            switch (alt45) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1219:9: var= ID
                    {
                    var=(Token)match(input,ID,FOLLOW_ID_in_term1911); 

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
                       					System.err.println("error on line " + var.getLine() + ": variable " + (var!=null?var.getText():null) + " is an array");
                       					System.exit(1);
                        			}
                        			else{
                    					System.err.println("error on line " + var.getLine() + ": variable " + (var!=null?var.getText():null) + " is not valid");
                    					System.exit(1);
                        			}
                       			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1246:6: (array= ID ( '[' (arrayExpr= expression ) ']' )+ )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1246:6: (array= ID ( '[' (arrayExpr= expression ) ']' )+ )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1246:7: array= ID ( '[' (arrayExpr= expression ) ']' )+
                    {
                    array=(Token)match(input,ID,FOLLOW_ID_in_term1930); 

                      				List<ExpressionNode> indexList = new ArrayList<ExpressionNode>();
                      				List<Integer> valueList = new ArrayList<Integer>();
                      				VarNode arrayNode = null;
                      				
                      				if(!VarNodeMap.containsKey((array!=null?array.getText():null))){
                      					System.err.println("error on line " + array.getLine() + ": " + (array!=null?array.getText():null) + " is not a valid array");
                       					System.exit(1);
                      				}
                      				
                      				arrayNode = VarNodeMap.get((array!=null?array.getText():null));
                      				if(!ArrayNode.class.isAssignableFrom(arrayNode.getClass())){
                       					System.err.println("error on line " + array.getLine() + ": " + (array!=null?array.getText():null) + " is not a valid array");
                       					System.exit(1);
                       				}
                       			
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1263:6: ( '[' (arrayExpr= expression ) ']' )+
                    int cnt44=0;
                    loop44:
                    do {
                        int alt44=2;
                        int LA44_0 = input.LA(1);

                        if ( (LA44_0==66) ) {
                            alt44=1;
                        }


                        switch (alt44) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1263:7: '[' (arrayExpr= expression ) ']'
                    	    {
                    	    match(input,66,FOLLOW_66_in_term1946); 
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1263:11: (arrayExpr= expression )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1263:12: arrayExpr= expression
                    	    {
                    	    pushFollow(FOLLOW_expression_in_term1951);
                    	    arrayExpr=expression();

                    	    state._fsp--;


                    	      				ExpressionNode node = (arrayExpr!=null?arrayExpr.expr:null);
                    	      				indexList.add(node);
                    	      				valueList.add((arrayExpr!=null?arrayExpr.value:0));
                    	      			

                    	    }

                    	    match(input,67,FOLLOW_67_in_term1966); 

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


                      				String name = ((ArrayNode) arrayNode).getElement(valueList).getName();
                      				retval.value = StatevectorMap.get(name);
                      				retval.expr = new ArrayElement((ArrayNode) arrayNode, indexList);
                      			

                    }


                    }
                    break;
                case 3 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1276:9: LPAREN expression RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_term1991); 
                    pushFollow(FOLLOW_expression_in_term1993);
                    expression11=expression();

                    state._fsp--;

                    match(input,RPAREN,FOLLOW_RPAREN_in_term1995); 
                    retval.expr = (expression11!=null?expression11.expr:null); retval.value = (expression11!=null?expression11.value:0);

                    }
                    break;
                case 4 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1277:9: INT
                    {
                    INT12=(Token)match(input,INT,FOLLOW_INT_in_term2007); 
                    retval.value = Integer.parseInt((INT12!=null?INT12.getText():null)); retval.expr = new ConstNode("name", retval.value);

                    }
                    break;
                case 5 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1278:9: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_term2019); 
                    retval.expr = ONE; retval.value = 1;

                    }
                    break;
                case 6 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1279:9: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_term2031); 
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
    };

    // $ANTLR start "unary"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1282:1: unary returns [ExpressionNode expr, int value] : ( '+' | ( '-' ) )* term ;
    public final PlatuInstParser.unary_return unary() throws RecognitionException {
        PlatuInstParser.unary_return retval = new PlatuInstParser.unary_return();
        retval.start = input.LT(1);

        PlatuInstParser.term_return term13 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1283:5: ( ( '+' | ( '-' ) )* term )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1283:9: ( '+' | ( '-' ) )* term
            {
            boolean positive = true;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1284:6: ( '+' | ( '-' ) )*
            loop46:
            do {
                int alt46=3;
                int LA46_0 = input.LA(1);

                if ( (LA46_0==PLUS) ) {
                    alt46=1;
                }
                else if ( (LA46_0==MINUS) ) {
                    alt46=2;
                }


                switch (alt46) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1284:7: '+'
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_unary2068); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1284:13: ( '-' )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1284:13: ( '-' )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1284:14: '-'
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_unary2073); 
            	    if(positive){ positive = false;} else {positive = true;}

            	    }


            	    }
            	    break;

            	default :
            	    break loop46;
                }
            } while (true);

            pushFollow(FOLLOW_term_in_unary2080);
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
    };

    // $ANTLR start "bitwiseNegation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1297:1: bitwiseNegation returns [ExpressionNode expr, int value] : ( '~' )* unary ;
    public final PlatuInstParser.bitwiseNegation_return bitwiseNegation() throws RecognitionException {
        PlatuInstParser.bitwiseNegation_return retval = new PlatuInstParser.bitwiseNegation_return();
        retval.start = input.LT(1);

        PlatuInstParser.unary_return unary14 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1298:2: ( ( '~' )* unary )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1298:5: ( '~' )* unary
            {
            boolean neg = false;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1299:3: ( '~' )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==BITWISE_NEGATION) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1299:4: '~'
            	    {
            	    match(input,BITWISE_NEGATION,FOLLOW_BITWISE_NEGATION_in_bitwiseNegation2111); 
            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);

            pushFollow(FOLLOW_unary_in_bitwiseNegation2117);
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
    };

    // $ANTLR start "negation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1312:1: negation returns [ExpressionNode expr, int value] : ( '!' )* bitwiseNegation ;
    public final PlatuInstParser.negation_return negation() throws RecognitionException {
        PlatuInstParser.negation_return retval = new PlatuInstParser.negation_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseNegation_return bitwiseNegation15 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1313:2: ( ( '!' )* bitwiseNegation )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1313:4: ( '!' )* bitwiseNegation
            {
            boolean neg = false;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1314:3: ( '!' )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==NEGATION) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1314:4: '!'
            	    {
            	    match(input,NEGATION,FOLLOW_NEGATION_in_negation2143); 
            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);

            pushFollow(FOLLOW_bitwiseNegation_in_negation2149);
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
    };

    // $ANTLR start "mult"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1327:1: mult returns [ExpressionNode expr, int value] : op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )* ;
    public final PlatuInstParser.mult_return mult() throws RecognitionException {
        PlatuInstParser.mult_return retval = new PlatuInstParser.mult_return();
        retval.start = input.LT(1);

        PlatuInstParser.negation_return op1 = null;

        PlatuInstParser.negation_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1328:5: (op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1328:9: op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )*
            {
            pushFollow(FOLLOW_negation_in_mult2177);
            op1=negation();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1329:6: ( '*' op2= negation | '/' op2= negation | '%' op2= negation )*
            loop49:
            do {
                int alt49=4;
                switch ( input.LA(1) ) {
                case TIMES:
                    {
                    alt49=1;
                    }
                    break;
                case DIV:
                    {
                    alt49=2;
                    }
                    break;
                case MOD:
                    {
                    alt49=3;
                    }
                    break;

                }

                switch (alt49) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1329:8: '*' op2= negation
            	    {
            	    match(input,TIMES,FOLLOW_TIMES_in_mult2189); 
            	    pushFollow(FOLLOW_negation_in_mult2193);
            	    op2=negation();

            	    state._fsp--;

            	    retval.expr = new MultNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value * (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1330:8: '/' op2= negation
            	    {
            	    match(input,DIV,FOLLOW_DIV_in_mult2204); 
            	    pushFollow(FOLLOW_negation_in_mult2208);
            	    op2=negation();

            	    state._fsp--;

            	    retval.expr = new DivNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value / (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 3 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1331:8: '%' op2= negation
            	    {
            	    match(input,MOD,FOLLOW_MOD_in_mult2219); 
            	    pushFollow(FOLLOW_negation_in_mult2223);
            	    op2=negation();

            	    state._fsp--;

            	    retval.expr = new ModNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value % (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop49;
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
    };

    // $ANTLR start "add"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1335:1: add returns [ExpressionNode expr, int value] : op1= mult ( '+' op2= mult | '-' op2= mult )* ;
    public final PlatuInstParser.add_return add() throws RecognitionException {
        PlatuInstParser.add_return retval = new PlatuInstParser.add_return();
        retval.start = input.LT(1);

        PlatuInstParser.mult_return op1 = null;

        PlatuInstParser.mult_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1336:5: (op1= mult ( '+' op2= mult | '-' op2= mult )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1336:9: op1= mult ( '+' op2= mult | '-' op2= mult )*
            {
            pushFollow(FOLLOW_mult_in_add2262);
            op1=mult();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1337:6: ( '+' op2= mult | '-' op2= mult )*
            loop50:
            do {
                int alt50=3;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==PLUS) ) {
                    alt50=1;
                }
                else if ( (LA50_0==MINUS) ) {
                    alt50=2;
                }


                switch (alt50) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1337:8: '+' op2= mult
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_add2273); 
            	    pushFollow(FOLLOW_mult_in_add2277);
            	    op2=mult();

            	    state._fsp--;

            	    retval.expr = new AddNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value + (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1338:9: '-' op2= mult
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_add2289); 
            	    pushFollow(FOLLOW_mult_in_add2293);
            	    op2=mult();

            	    state._fsp--;

            	    retval.expr = new SubNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value - (op2!=null?op2.value:0);

            	    }
            	    break;

            	default :
            	    break loop50;
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
    };

    // $ANTLR start "shift"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1342:1: shift returns [ExpressionNode expr, int value] : op1= add ( '<<' op2= add | '>>' op2= add )* ;
    public final PlatuInstParser.shift_return shift() throws RecognitionException {
        PlatuInstParser.shift_return retval = new PlatuInstParser.shift_return();
        retval.start = input.LT(1);

        PlatuInstParser.add_return op1 = null;

        PlatuInstParser.add_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1343:5: (op1= add ( '<<' op2= add | '>>' op2= add )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1343:9: op1= add ( '<<' op2= add | '>>' op2= add )*
            {
            pushFollow(FOLLOW_add_in_shift2332);
            op1=add();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1344:6: ( '<<' op2= add | '>>' op2= add )*
            loop51:
            do {
                int alt51=3;
                int LA51_0 = input.LA(1);

                if ( (LA51_0==BITWISE_LSHIFT) ) {
                    alt51=1;
                }
                else if ( (LA51_0==BITWISE_RSHIFT) ) {
                    alt51=2;
                }


                switch (alt51) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1344:8: '<<' op2= add
            	    {
            	    match(input,BITWISE_LSHIFT,FOLLOW_BITWISE_LSHIFT_in_shift2343); 
            	    pushFollow(FOLLOW_add_in_shift2347);
            	    op2=add();

            	    state._fsp--;

            	    retval.expr = new LeftShiftNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value << (op2!=null?op2.value:0);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1345:9: '>>' op2= add
            	    {
            	    match(input,BITWISE_RSHIFT,FOLLOW_BITWISE_RSHIFT_in_shift2359); 
            	    pushFollow(FOLLOW_add_in_shift2363);
            	    op2=add();

            	    state._fsp--;

            	    retval.expr = new RightShiftNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value >> (op2!=null?op2.value:0);

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
    // $ANTLR end "shift"

    public static class relation_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "relation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1349:1: relation returns [ExpressionNode expr, int value] : op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* ;
    public final PlatuInstParser.relation_return relation() throws RecognitionException {
        PlatuInstParser.relation_return retval = new PlatuInstParser.relation_return();
        retval.start = input.LT(1);

        PlatuInstParser.shift_return op1 = null;

        PlatuInstParser.shift_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1350:5: (op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1350:9: op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
            {
            pushFollow(FOLLOW_shift_in_relation2398);
            op1=shift();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1351:6: ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
            loop52:
            do {
                int alt52=5;
                switch ( input.LA(1) ) {
                case LESS:
                    {
                    alt52=1;
                    }
                    break;
                case LESS_EQUAL:
                    {
                    alt52=2;
                    }
                    break;
                case GREATER_EQUAL:
                    {
                    alt52=3;
                    }
                    break;
                case GREATER:
                    {
                    alt52=4;
                    }
                    break;

                }

                switch (alt52) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1351:8: '<' op2= shift
            	    {
            	    match(input,LESS,FOLLOW_LESS_in_relation2409); 
            	    pushFollow(FOLLOW_shift_in_relation2413);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new LessNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value < (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1352:9: '<=' op2= shift
            	    {
            	    match(input,LESS_EQUAL,FOLLOW_LESS_EQUAL_in_relation2425); 
            	    pushFollow(FOLLOW_shift_in_relation2429);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new LessEqualNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value <= (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 3 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1353:9: '>=' op2= shift
            	    {
            	    match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_relation2441); 
            	    pushFollow(FOLLOW_shift_in_relation2445);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new GreatEqualNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value >= (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 4 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1354:9: '>' op2= shift
            	    {
            	    match(input,GREATER,FOLLOW_GREATER_in_relation2457); 
            	    pushFollow(FOLLOW_shift_in_relation2461);
            	    op2=shift();

            	    state._fsp--;

            	    retval.expr = new GreatNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value > (op2!=null?op2.value:0)) ? 1 : 0;

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
    // $ANTLR end "relation"

    public static class equivalence_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "equivalence"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1358:1: equivalence returns [ExpressionNode expr, int value] : op1= relation ( '==' op2= relation | '!=' op2= relation )* ;
    public final PlatuInstParser.equivalence_return equivalence() throws RecognitionException {
        PlatuInstParser.equivalence_return retval = new PlatuInstParser.equivalence_return();
        retval.start = input.LT(1);

        PlatuInstParser.relation_return op1 = null;

        PlatuInstParser.relation_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1359:5: (op1= relation ( '==' op2= relation | '!=' op2= relation )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1359:9: op1= relation ( '==' op2= relation | '!=' op2= relation )*
            {
            pushFollow(FOLLOW_relation_in_equivalence2500);
            op1=relation();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1360:6: ( '==' op2= relation | '!=' op2= relation )*
            loop53:
            do {
                int alt53=3;
                int LA53_0 = input.LA(1);

                if ( (LA53_0==EQUIV) ) {
                    alt53=1;
                }
                else if ( (LA53_0==NOT_EQUIV) ) {
                    alt53=2;
                }


                switch (alt53) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1360:8: '==' op2= relation
            	    {
            	    match(input,EQUIV,FOLLOW_EQUIV_in_equivalence2511); 
            	    pushFollow(FOLLOW_relation_in_equivalence2515);
            	    op2=relation();

            	    state._fsp--;

            	    retval.expr = new EquivNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value == (op2!=null?op2.value:0)) ? 1 : 0;

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1361:8: '!=' op2= relation
            	    {
            	    match(input,NOT_EQUIV,FOLLOW_NOT_EQUIV_in_equivalence2526); 
            	    pushFollow(FOLLOW_relation_in_equivalence2530);
            	    op2=relation();

            	    state._fsp--;

            	    retval.expr = new NotEquivNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value != (op2!=null?op2.value:0)) ? 1 : 0;

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
    // $ANTLR end "equivalence"

    public static class bitwiseAnd_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "bitwiseAnd"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1365:1: bitwiseAnd returns [ExpressionNode expr, int value] : op1= equivalence ( '&' op2= equivalence )* ;
    public final PlatuInstParser.bitwiseAnd_return bitwiseAnd() throws RecognitionException {
        PlatuInstParser.bitwiseAnd_return retval = new PlatuInstParser.bitwiseAnd_return();
        retval.start = input.LT(1);

        PlatuInstParser.equivalence_return op1 = null;

        PlatuInstParser.equivalence_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1366:5: (op1= equivalence ( '&' op2= equivalence )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1366:9: op1= equivalence ( '&' op2= equivalence )*
            {
            pushFollow(FOLLOW_equivalence_in_bitwiseAnd2569);
            op1=equivalence();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1367:6: ( '&' op2= equivalence )*
            loop54:
            do {
                int alt54=2;
                int LA54_0 = input.LA(1);

                if ( (LA54_0==BITWISE_AND) ) {
                    alt54=1;
                }


                switch (alt54) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1367:8: '&' op2= equivalence
            	    {
            	    match(input,BITWISE_AND,FOLLOW_BITWISE_AND_in_bitwiseAnd2581); 
            	    pushFollow(FOLLOW_equivalence_in_bitwiseAnd2585);
            	    op2=equivalence();

            	    state._fsp--;

            	    retval.expr = new BitAndNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value & (op2!=null?op2.value:0);

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
    // $ANTLR end "bitwiseAnd"

    public static class bitwiseXor_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "bitwiseXor"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1371:1: bitwiseXor returns [ExpressionNode expr, int value] : op1= bitwiseAnd ( '^' op2= bitwiseAnd )* ;
    public final PlatuInstParser.bitwiseXor_return bitwiseXor() throws RecognitionException {
        PlatuInstParser.bitwiseXor_return retval = new PlatuInstParser.bitwiseXor_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseAnd_return op1 = null;

        PlatuInstParser.bitwiseAnd_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1372:5: (op1= bitwiseAnd ( '^' op2= bitwiseAnd )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1372:9: op1= bitwiseAnd ( '^' op2= bitwiseAnd )*
            {
            pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2624);
            op1=bitwiseAnd();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1373:6: ( '^' op2= bitwiseAnd )*
            loop55:
            do {
                int alt55=2;
                int LA55_0 = input.LA(1);

                if ( (LA55_0==BITWISE_XOR) ) {
                    alt55=1;
                }


                switch (alt55) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1373:8: '^' op2= bitwiseAnd
            	    {
            	    match(input,BITWISE_XOR,FOLLOW_BITWISE_XOR_in_bitwiseXor2635); 
            	    pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2639);
            	    op2=bitwiseAnd();

            	    state._fsp--;

            	    retval.expr = new BitXorNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value ^ (op2!=null?op2.value:0);

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
    // $ANTLR end "bitwiseXor"

    public static class bitwiseOr_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "bitwiseOr"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1377:1: bitwiseOr returns [ExpressionNode expr, int value] : op1= bitwiseXor ( '|' op2= bitwiseXor )* ;
    public final PlatuInstParser.bitwiseOr_return bitwiseOr() throws RecognitionException {
        PlatuInstParser.bitwiseOr_return retval = new PlatuInstParser.bitwiseOr_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseXor_return op1 = null;

        PlatuInstParser.bitwiseXor_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1378:5: (op1= bitwiseXor ( '|' op2= bitwiseXor )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1378:9: op1= bitwiseXor ( '|' op2= bitwiseXor )*
            {
            pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2678);
            op1=bitwiseXor();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1379:6: ( '|' op2= bitwiseXor )*
            loop56:
            do {
                int alt56=2;
                int LA56_0 = input.LA(1);

                if ( (LA56_0==BITWISE_OR) ) {
                    alt56=1;
                }


                switch (alt56) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1379:8: '|' op2= bitwiseXor
            	    {
            	    match(input,BITWISE_OR,FOLLOW_BITWISE_OR_in_bitwiseOr2689); 
            	    pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2693);
            	    op2=bitwiseXor();

            	    state._fsp--;

            	    retval.expr = new BitOrNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = retval.value | (op2!=null?op2.value:0);

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
    // $ANTLR end "bitwiseOr"

    public static class and_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "and"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1383:1: and returns [ExpressionNode expr, int value] : op1= bitwiseOr ( '&&' op2= bitwiseOr )* ;
    public final PlatuInstParser.and_return and() throws RecognitionException {
        PlatuInstParser.and_return retval = new PlatuInstParser.and_return();
        retval.start = input.LT(1);

        PlatuInstParser.bitwiseOr_return op1 = null;

        PlatuInstParser.bitwiseOr_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1384:5: (op1= bitwiseOr ( '&&' op2= bitwiseOr )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1384:9: op1= bitwiseOr ( '&&' op2= bitwiseOr )*
            {
            pushFollow(FOLLOW_bitwiseOr_in_and2732);
            op1=bitwiseOr();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1385:6: ( '&&' op2= bitwiseOr )*
            loop57:
            do {
                int alt57=2;
                int LA57_0 = input.LA(1);

                if ( (LA57_0==AND) ) {
                    alt57=1;
                }


                switch (alt57) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1385:8: '&&' op2= bitwiseOr
            	    {
            	    match(input,AND,FOLLOW_AND_in_and2743); 
            	    pushFollow(FOLLOW_bitwiseOr_in_and2747);
            	    op2=bitwiseOr();

            	    state._fsp--;

            	    retval.expr = new AndNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value != 0 && (op2!=null?op2.value:0) != 0) ? 1 : 0;

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
    // $ANTLR end "and"

    public static class or_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "or"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1389:1: or returns [ExpressionNode expr, int value] : op1= and ( '||' op2= and )* ;
    public final PlatuInstParser.or_return or() throws RecognitionException {
        PlatuInstParser.or_return retval = new PlatuInstParser.or_return();
        retval.start = input.LT(1);

        PlatuInstParser.and_return op1 = null;

        PlatuInstParser.and_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1390:5: (op1= and ( '||' op2= and )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1390:9: op1= and ( '||' op2= and )*
            {
            pushFollow(FOLLOW_and_in_or2786);
            op1=and();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1391:6: ( '||' op2= and )*
            loop58:
            do {
                int alt58=2;
                int LA58_0 = input.LA(1);

                if ( (LA58_0==OR) ) {
                    alt58=1;
                }


                switch (alt58) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1391:8: '||' op2= and
            	    {
            	    match(input,OR,FOLLOW_OR_in_or2797); 
            	    pushFollow(FOLLOW_and_in_or2801);
            	    op2=and();

            	    state._fsp--;

            	    retval.expr = new OrNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value != 0 || (op2!=null?op2.value:0) != 0) ? 1 : 0;

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
    // $ANTLR end "or"

    public static class implication_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "implication"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1395:1: implication returns [ExpressionNode expr, int value] : op1= or ( '->' op2= or )* ;
    public final PlatuInstParser.implication_return implication() throws RecognitionException {
        PlatuInstParser.implication_return retval = new PlatuInstParser.implication_return();
        retval.start = input.LT(1);

        PlatuInstParser.or_return op1 = null;

        PlatuInstParser.or_return op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1396:5: (op1= or ( '->' op2= or )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1396:7: op1= or ( '->' op2= or )*
            {
            pushFollow(FOLLOW_or_in_implication2838);
            op1=or();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1397:6: ( '->' op2= or )*
            loop59:
            do {
                int alt59=2;
                int LA59_0 = input.LA(1);

                if ( (LA59_0==IMPLICATION) ) {
                    alt59=1;
                }


                switch (alt59) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1397:8: '->' op2= or
            	    {
            	    match(input,IMPLICATION,FOLLOW_IMPLICATION_in_implication2849); 
            	    pushFollow(FOLLOW_or_in_implication2853);
            	    op2=or();

            	    state._fsp--;

            	    retval.expr = new ImplicationNode(retval.expr, (op2!=null?op2.expr:null)); retval.value = (retval.value == 0 || (op2!=null?op2.value:0) != 0) ? 1 : 0;

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
    // $ANTLR end "implication"

    public static class expression_return extends ParserRuleReturnScope {
        public ExpressionNode expr;
        public int value;
    };

    // $ANTLR start "expression"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1400:1: expression returns [ExpressionNode expr, int value] : op1= implication ( '?' op2= expression ':' op3= expression )? ;
    public final PlatuInstParser.expression_return expression() throws RecognitionException {
        PlatuInstParser.expression_return retval = new PlatuInstParser.expression_return();
        retval.start = input.LT(1);

        PlatuInstParser.implication_return op1 = null;

        PlatuInstParser.expression_return op2 = null;

        PlatuInstParser.expression_return op3 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1401:5: (op1= implication ( '?' op2= expression ':' op3= expression )? )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1401:9: op1= implication ( '?' op2= expression ':' op3= expression )?
            {
            pushFollow(FOLLOW_implication_in_expression2888);
            op1=implication();

            state._fsp--;

            retval.expr = (op1!=null?op1.expr:null); retval.value = (op1!=null?op1.value:0);
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1402:6: ( '?' op2= expression ':' op3= expression )?
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==QMARK) ) {
                alt60=1;
            }
            switch (alt60) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuInst.g:1402:7: '?' op2= expression ':' op3= expression
                    {
                    match(input,QMARK,FOLLOW_QMARK_in_expression2898); 
                    pushFollow(FOLLOW_expression_in_expression2902);
                    op2=expression();

                    state._fsp--;

                    match(input,COLON,FOLLOW_COLON_in_expression2904); 
                    pushFollow(FOLLOW_expression_in_expression2908);
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
    public static final BitSet FOLLOW_GREATER_in_main162 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_instantiation_in_main171 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LESS_in_main173 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_63_in_main175 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_main177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_moduleClass203 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000001L});
    public static final BitSet FOLLOW_64_in_moduleClass205 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_NAME_in_moduleClass207 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_moduleClass209 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass211 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_moduleClass215 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass230 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
    public static final BitSet FOLLOW_65_in_moduleClass232 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_moduleClass234 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass236 = new BitSet(new long[]{0x0000000000040020L});
    public static final BitSet FOLLOW_ID_in_moduleClass243 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_moduleClass260 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_moduleClass264 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_moduleClass280 = new BitSet(new long[]{0x0000000000060000L,0x0000000000000004L});
    public static final BitSet FOLLOW_ID_in_moduleClass304 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_moduleClass324 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_moduleClass328 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_moduleClass344 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_moduleClass348 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_moduleClass364 = new BitSet(new long[]{0x0000000000060000L,0x0000000000000004L});
    public static final BitSet FOLLOW_COMMA_in_moduleClass387 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_moduleClass391 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_QUOTE_in_moduleClass414 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_moduleClass416 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_constants_in_moduleClass418 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_variables_in_moduleClass421 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_logic_in_moduleClass423 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_LESS_in_moduleClass425 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000010L});
    public static final BitSet FOLLOW_68_in_moduleClass427 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_moduleClass429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_constants454 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_constants456 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_constants458 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_ID_in_constants463 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_constants465 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_constants469 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_constants480 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_constants484 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_70_in_constants486 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_constants488 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalConstants505 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_69_in_globalConstants507 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants509 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_ID_in_globalConstants514 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_globalConstants516 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_globalConstants520 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalConstants546 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_globalConstants550 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_70_in_globalConstants552 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants554 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalVariables568 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_INTERNAL_in_globalVariables570 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables572 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_globalVarDecl_in_globalVariables575 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_globalArrayDecl_in_globalVariables579 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_globalVariables583 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_71_in_globalVariables585 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables587 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_globalVarDecl600 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_globalVarDecl610 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_globalVarDecl615 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_ID_in_globalVarDecl629 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalVarDecl640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_globalArrayDecl654 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_globalArrayDecl664 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_globalArrayDecl669 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_globalArrayDecl681 = new BitSet(new long[]{0x0000000000004000L,0x0000000000000004L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalArrayDecl685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_variables701 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_INTERNAL_in_variables703 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_variables705 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_varDecl_in_variables709 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_arrayDecl_in_variables713 = new BitSet(new long[]{0x0000002000000020L});
    public static final BitSet FOLLOW_LESS_in_variables717 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_71_in_variables719 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_variables721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_varDecl738 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_varDecl748 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_varDecl753 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_ID_in_varDecl767 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_varDecl786 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_arrayDecl800 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_arrayDecl811 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_arrayDecl816 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_arrayDecl827 = new BitSet(new long[]{0x0000000800004000L,0x0000000000000004L});
    public static final BitSet FOLLOW_EQUALS_in_arrayDecl839 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_72_in_arrayDecl849 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_arrayDecl854 = new BitSet(new long[]{0x0000000000000060L,0x0000000000000200L});
    public static final BitSet FOLLOW_ID_in_arrayDecl868 = new BitSet(new long[]{0x0000000000000060L,0x0000000000000200L});
    public static final BitSet FOLLOW_73_in_arrayDecl880 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_arrayDecl892 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_instantiation910 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_instantiation914 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_instantiation930 = new BitSet(new long[]{0x0000000000000280L});
    public static final BitSet FOLLOW_MEMBER_in_instantiation935 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COMMA_in_instantiation937 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_MEMBER_in_instantiation957 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_instantiation975 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_instantiation977 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_marking_in_logic1023 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_transition_in_logic1026 = new BitSet(new long[]{0x0000002000000002L});
    public static final BitSet FOLLOW_LESS_in_marking1079 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_MARKING_in_marking1081 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_marking1083 = new BitSet(new long[]{0x0000002000000060L});
    public static final BitSet FOLLOW_INT_in_marking1089 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_ID_in_marking1115 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_COMMA_in_marking1139 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_marking1144 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_ID_in_marking1170 = new BitSet(new long[]{0x0000002000020000L});
    public static final BitSet FOLLOW_LESS_in_marking1197 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_74_in_marking1199 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_marking1201 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_transition1235 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_TRANSITION_in_transition1237 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LABEL_in_transition1239 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1241 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1243 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_set_in_transition1247 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1253 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_PRESET_in_transition1255 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1257 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1260 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1262 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1267 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_transition1272 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_ID_in_transition1292 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1308 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_transition1312 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1330 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_transition1334 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1349 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_POSTSET_in_transition1353 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1355 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1359 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1361 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1366 = new BitSet(new long[]{0x0000000000000060L});
    public static final BitSet FOLLOW_INT_in_transition1372 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_ID_in_transition1392 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1417 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_transition1421 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_COMMA_in_transition1442 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_transition1445 = new BitSet(new long[]{0x0000000000060000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1464 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_transition1469 = new BitSet(new long[]{0x0000002000000020L,0x0000000000007000L});
    public static final BitSet FOLLOW_guard_in_transition1472 = new BitSet(new long[]{0x0000002000000020L,0x0000000000005000L});
    public static final BitSet FOLLOW_delay_in_transition1492 = new BitSet(new long[]{0x0000002000000020L,0x0000000000001000L});
    public static final BitSet FOLLOW_assertion_in_transition1513 = new BitSet(new long[]{0x0000002000000020L,0x0000000000001000L});
    public static final BitSet FOLLOW_assignment_in_transition1533 = new BitSet(new long[]{0x0000002000000020L,0x0000000000001000L});
    public static final BitSet FOLLOW_LESS_in_transition1553 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_75_in_transition1555 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_GREATER_in_transition1557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_76_in_assertion1591 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_assertion1593 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assertion1595 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_assertion1597 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assertion1599 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_77_in_guard1624 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_guard1626 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_guard1628 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_guard1630 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_guard1632 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_78_in_delay1666 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LPAREN_in_delay1668 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_INT_in_delay1672 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_COMMA_in_delay1686 = new BitSet(new long[]{0x0000000000000040L,0x0000000000008000L});
    public static final BitSet FOLLOW_INT_in_delay1691 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_79_in_delay1706 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_delay1719 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_delay1721 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1747 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1749 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_ID_in_assignment1753 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1775 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1777 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assignment1795 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1818 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_assignment1835 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assignment1840 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_assignment1857 = new BitSet(new long[]{0x0000000800000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1861 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_assignment1865 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1880 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_term1911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_term1930 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
    public static final BitSet FOLLOW_66_in_term1946 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_term1951 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_67_in_term1966 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
    public static final BitSet FOLLOW_LPAREN_in_term1991 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_term1993 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_RPAREN_in_term1995 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_term2007 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_term2019 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_term2031 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_unary2068 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_MINUS_in_unary2073 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_term_in_unary2080 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BITWISE_NEGATION_in_bitwiseNegation2111 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_unary_in_bitwiseNegation2117 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEGATION_in_negation2143 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseNegation_in_negation2149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_negation_in_mult2177 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_TIMES_in_mult2189 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_negation_in_mult2193 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_DIV_in_mult2204 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_negation_in_mult2208 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_MOD_in_mult2219 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_negation_in_mult2223 = new BitSet(new long[]{0x0000000700000002L});
    public static final BitSet FOLLOW_mult_in_add2262 = new BitSet(new long[]{0x00000000C0000002L});
    public static final BitSet FOLLOW_PLUS_in_add2273 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_mult_in_add2277 = new BitSet(new long[]{0x00000000C0000002L});
    public static final BitSet FOLLOW_MINUS_in_add2289 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_mult_in_add2293 = new BitSet(new long[]{0x00000000C0000002L});
    public static final BitSet FOLLOW_add_in_shift2332 = new BitSet(new long[]{0x000C000000000002L});
    public static final BitSet FOLLOW_BITWISE_LSHIFT_in_shift2343 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_add_in_shift2347 = new BitSet(new long[]{0x000C000000000002L});
    public static final BitSet FOLLOW_BITWISE_RSHIFT_in_shift2359 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_add_in_shift2363 = new BitSet(new long[]{0x000C000000000002L});
    public static final BitSet FOLLOW_shift_in_relation2398 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_LESS_in_relation2409 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2413 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_LESS_EQUAL_in_relation2425 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2429 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_relation2441 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2445 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_GREATER_in_relation2457 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_shift_in_relation2461 = new BitSet(new long[]{0x000000F000000002L});
    public static final BitSet FOLLOW_relation_in_equivalence2500 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_EQUIV_in_equivalence2511 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_relation_in_equivalence2515 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_NOT_EQUIV_in_equivalence2526 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_relation_in_equivalence2530 = new BitSet(new long[]{0x0000030000000002L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2569 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_BITWISE_AND_in_bitwiseAnd2581 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2585 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2624 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_BITWISE_XOR_in_bitwiseXor2635 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2639 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2678 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_BITWISE_OR_in_bitwiseOr2689 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2693 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2732 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_AND_in_and2743 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2747 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_and_in_or2786 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_OR_in_or2797 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_and_in_or2801 = new BitSet(new long[]{0x0000100000000002L});
    public static final BitSet FOLLOW_or_in_implication2838 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_IMPLICATION_in_implication2849 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_or_in_implication2853 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_implication_in_expression2888 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_QMARK_in_expression2898 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_expression2902 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_COLON_in_expression2904 = new BitSet(new long[]{0x00004400C0000D60L});
    public static final BitSet FOLLOW_expression_in_expression2908 = new BitSet(new long[]{0x0000000000000002L});

}