// $ANTLR 3.3 Nov 30, 2010 12:50:56 /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g 2011-09-05 15:36:20

    package verification.platu.lpn.io;
    
    import java.util.Map.Entry;
    import java.util.HashMap;
    import java.util.LinkedList;
    import java.util.HashSet;
    import java.util.Set;
    import java.util.Arrays;

import verification.platu.expression.*;
import verification.platu.lpn.DualHashMap;
import verification.platu.lpn.LPN;
import verification.platu.lpn.LPNTran;
import verification.platu.lpn.LpnTranList;
import verification.platu.lpn.VarExpr;
import verification.platu.lpn.VarExprList;
import verification.platu.lpn.VarSet;
import verification.platu.project.Project;
import verification.platu.stategraph.StateGraph;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class PlatuGrammarParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ID", "INT", "LPAREN", "RPAREN", "TRUE", "FALSE", "QMARK", "COLON", "SEMICOLON", "PERIOD", "UNDERSCORE", "COMMA", "QUOTE", "MODULE", "NAME", "INPUT", "OUTPUT", "INTERNAL", "MARKING", "STATE_VECTOR", "TRANSITION", "LABEL", "PRESET", "POSTSET", "PLUS", "MINUS", "TIMES", "DIV", "MOD", "EQUALS", "GREATER", "LESS", "GREATER_EQUAL", "LESS_EQUAL", "EQUIV", "NOT_EQUIV", "NEGATION", "AND", "OR", "IMPLICATION", "BITWISE_NEGATION", "BITWISE_AND", "BITWISE_OR", "BITWISE_XOR", "BITWISE_LSHIFT", "BITWISE_RSHIFT", "LETTER", "DIGIT", "WS", "COMMENT", "MULTILINECOMMENT", "XMLCOMMENT", "IGNORE", "'main'", "'const'", "'['", "']'", "'inst'", "'assert'", "'inf'"
    };
    public static final int EOF=-1;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__59=59;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int ID=4;
    public static final int INT=5;
    public static final int LPAREN=6;
    public static final int RPAREN=7;
    public static final int TRUE=8;
    public static final int FALSE=9;
    public static final int QMARK=10;
    public static final int COLON=11;
    public static final int SEMICOLON=12;
    public static final int PERIOD=13;
    public static final int UNDERSCORE=14;
    public static final int COMMA=15;
    public static final int QUOTE=16;
    public static final int MODULE=17;
    public static final int NAME=18;
    public static final int INPUT=19;
    public static final int OUTPUT=20;
    public static final int INTERNAL=21;
    public static final int MARKING=22;
    public static final int STATE_VECTOR=23;
    public static final int TRANSITION=24;
    public static final int LABEL=25;
    public static final int PRESET=26;
    public static final int POSTSET=27;
    public static final int PLUS=28;
    public static final int MINUS=29;
    public static final int TIMES=30;
    public static final int DIV=31;
    public static final int MOD=32;
    public static final int EQUALS=33;
    public static final int GREATER=34;
    public static final int LESS=35;
    public static final int GREATER_EQUAL=36;
    public static final int LESS_EQUAL=37;
    public static final int EQUIV=38;
    public static final int NOT_EQUIV=39;
    public static final int NEGATION=40;
    public static final int AND=41;
    public static final int OR=42;
    public static final int IMPLICATION=43;
    public static final int BITWISE_NEGATION=44;
    public static final int BITWISE_AND=45;
    public static final int BITWISE_OR=46;
    public static final int BITWISE_XOR=47;
    public static final int BITWISE_LSHIFT=48;
    public static final int BITWISE_RSHIFT=49;
    public static final int LETTER=50;
    public static final int DIGIT=51;
    public static final int WS=52;
    public static final int COMMENT=53;
    public static final int MULTILINECOMMENT=54;
    public static final int XMLCOMMENT=55;
    public static final int IGNORE=56;

    // delegates
    // delegators


        public PlatuGrammarParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PlatuGrammarParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PlatuGrammarParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g"; }


    	// static variables
        static private int INFINITY = Integer.MAX_VALUE;
        static private boolean main = false;
        static private ExpressionNode ZERO = new ConstNode("FALSE", 0);  // constant false node
        static private ExpressionNode ONE = new ConstNode("TRUE", 1);  // constant true node
        static private Expression TrueExpr = new Expression(ONE); // constant true expression
        static private HashMap<String, LPN> LpnMap = new HashMap<String, LPN>();  // all modules parsed, keyed by module name
        static private HashMap<String, Integer> GlobalVarHashMap = new HashMap<String, Integer>();  // global variables and associated values
        static private HashMap<String, LPN> GlobalOutputMap = new HashMap<String, LPN>();  // maps potential output variables to associated lpn
        static private HashMap<String, Integer> GlobalInterfaceMap = new HashMap<String, Integer>();  // maps variables to initial values, input have null value until associated output is found
        static private HashMap<String, List<LPN>> GlobalInputMap = new HashMap<String, List<LPN>>(); // maps input variables to associated lpn
        static private HashMap<String, List<LPNTran>> GlobalTranMap = new HashMap<String, List<LPNTran>>();  // maps potential output variables to lpn transitions which affect it
        static private HashMap<String, VarNode> GlobalVarNodeMap = new HashMap<String, VarNode>(); // maps global variable name to variable object
        
        // non-static variables
        private boolean Instance = false;
        private HashMap<String, VarNode> VarNodeMap = null; // maps variable name to variable object
        private HashMap<String, ArrayNode> ArrayNodeMap = null; // maps array variable name to variable object
    	private DualHashMap<String, Integer> VarIndexMap = null;  // maps variables to an array index
        private HashMap<String, Integer> GlobalConstHashMap = new HashMap<String, Integer>();  // global constants within a single lpn file
        private HashMap<String, Integer> ConstHashMap = null;  // constants within a single module
        private HashMap<String, Integer> StatevectorMap = null;  // module variables mapped to initial values
        private HashMap<String, Integer> VarCountMap = null; // count of the references to each module variable
        private List<LPNTran> inputTranList = null;  // list of lpn transitions which affect a modules input
        private List<LPNTran> outputTranList = null; // list of lpn transitions which affect a modules output
        private VarSet Inputs = null;  // module inputs
        private VarSet Internals = null; // module internal variables
        private VarSet Outputs = null;  // module outputs
        private int VariableIndex = 0;  // count of index assigned to module variables
        private int TransitionIndex = 0;
        private int GlobalCount = 0;  // number of global variables defined in this lpn file
        private int GlobalSize = 0;  // number of global variables defined
        
        public enum VarType {
        	INPUT, OUTPUT, INTERNAL, GLOBAL	
    	}



    // $ANTLR start "parseLpnFile"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:80:1: parseLpnFile[Project prj, boolean instance, HashMap<String, String> portMap] returns [Set<LPN> lpnSet] : ;
    public final Set<LPN> parseLpnFile(Project prj, boolean instance, HashMap<String, String> portMap) throws RecognitionException {
        Set<LPN> lpnSet = null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:81:2: ()
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:82:2: 
            {
            }

        }
        finally {
        }
        return lpnSet;
    }
    // $ANTLR end "parseLpnFile"


    // $ANTLR start "lpn"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:84:1: lpn[Project prj] returns [Set<LPN> lpnSet] : ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )? ( module[prj] | main[prj] )+ EOF ;
    public final Set<LPN> lpn(Project prj) throws RecognitionException {
        Set<LPN> lpnSet = null;

        LPN module1 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:85:5: ( ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )? ( module[prj] | main[prj] )+ EOF )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:85:9: ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )? ( module[prj] | main[prj] )+ EOF
            {
            lpnSet = new HashSet<LPN>();
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:9: ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )?
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:10: ( globalConstants globalVariables )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:10: ( globalConstants globalVariables )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:11: globalConstants globalVariables
                    {
                    pushFollow(FOLLOW_globalConstants_in_lpn86);
                    globalConstants();

                    state._fsp--;

                    pushFollow(FOLLOW_globalVariables_in_lpn88);
                    globalVariables();

                    state._fsp--;


                    }


                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:46: ( globalVariables globalConstants )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:46: ( globalVariables globalConstants )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:47: globalVariables globalConstants
                    {
                    pushFollow(FOLLOW_globalVariables_in_lpn94);
                    globalVariables();

                    state._fsp--;

                    pushFollow(FOLLOW_globalConstants_in_lpn96);
                    globalConstants();

                    state._fsp--;


                    }


                    }
                    break;
                case 3 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:82: ( globalVariables )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:82: ( globalVariables )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:83: globalVariables
                    {
                    pushFollow(FOLLOW_globalVariables_in_lpn102);
                    globalVariables();

                    state._fsp--;


                    }


                    }
                    break;
                case 4 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:102: ( globalConstants )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:102: ( globalConstants )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:86:103: globalConstants
                    {
                    pushFollow(FOLLOW_globalConstants_in_lpn108);
                    globalConstants();

                    state._fsp--;


                    }


                    }
                    break;

            }


                    		// check that global constants are consistently defined in each lpn file
                    		if(GlobalSize > 0 && GlobalCount != GlobalSize){
                    			System.err.println("error: global variable definitions are inconsistent");
                    			System.exit(1);
                    		}
                    	
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:94:8: ( module[prj] | main[prj] )+
            int cnt2=0;
            loop2:
            do {
                int alt2=3;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==LESS) ) {
                    int LA2_2 = input.LA(2);

                    if ( (LA2_2==MODULE) ) {
                        int LA2_3 = input.LA(3);

                        if ( (LA2_3==NAME) ) {
                            int LA2_4 = input.LA(4);

                            if ( (LA2_4==EQUALS) ) {
                                int LA2_5 = input.LA(5);

                                if ( (LA2_5==QUOTE) ) {
                                    int LA2_6 = input.LA(6);

                                    if ( (LA2_6==ID) ) {
                                        alt2=1;
                                    }
                                    else if ( (LA2_6==57) ) {
                                        alt2=2;
                                    }


                                }


                            }


                        }


                    }


                }


                switch (alt2) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:94:9: module[prj]
            	    {
            	    pushFollow(FOLLOW_module_in_lpn133);
            	    module1=module(prj);

            	    state._fsp--;


            	                	lpnSet.add(module1);
            	                

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:98:11: main[prj]
            	    {
            	    pushFollow(FOLLOW_main_in_lpn160);
            	    main(prj);

            	    state._fsp--;


            	            		
            	            	

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_lpn185); 

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
        return lpnSet;
    }
    // $ANTLR end "lpn"


    // $ANTLR start "main"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:105:1: main[Project prj] : '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( instantiation )+ '<' '/' 'mod' '>' ;
    public final void main(Project prj) throws RecognitionException {
        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:106:2: ( '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( instantiation )+ '<' '/' 'mod' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:106:4: '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( instantiation )+ '<' '/' 'mod' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_main204); 
            match(input,MODULE,FOLLOW_MODULE_in_main206); 
            match(input,NAME,FOLLOW_NAME_in_main208); 
            match(input,EQUALS,FOLLOW_EQUALS_in_main210); 
            match(input,QUOTE,FOLLOW_QUOTE_in_main212); 
            match(input,57,FOLLOW_57_in_main214); 
            match(input,QUOTE,FOLLOW_QUOTE_in_main216); 
            match(input,GREATER,FOLLOW_GREATER_in_main218); 

            				if(main == true){
            					System.err.println("error");
            					System.exit(1);
            				}
            				
            				main = true;
            			
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:115:3: ( instantiation )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==LESS) ) {
                    int LA3_1 = input.LA(2);

                    if ( (LA3_1==61) ) {
                        alt3=1;
                    }


                }


                switch (alt3) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:115:3: instantiation
            	    {
            	    pushFollow(FOLLOW_instantiation_in_main227);
            	    instantiation();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_main230); 
            match(input,DIV,FOLLOW_DIV_in_main232); 
            match(input,MODULE,FOLLOW_MODULE_in_main234); 
            match(input,GREATER,FOLLOW_GREATER_in_main236); 

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


    // $ANTLR start "module"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:119:1: module[Project prj] returns [LPN lpn] : ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' ) ;
    public final LPN module(Project prj) throws RecognitionException {
        LPN lpn = null;

        Token ID2=null;
        PlatuGrammarParser.logic_return logic3 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:120:5: ( ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' ) )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:120:7: ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' )
            {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:120:7: ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:120:9: '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_module262); 
            match(input,MODULE,FOLLOW_MODULE_in_module264); 
            match(input,NAME,FOLLOW_NAME_in_module266); 
            match(input,EQUALS,FOLLOW_EQUALS_in_module268); 
            match(input,QUOTE,FOLLOW_QUOTE_in_module270); 
            ID2=(Token)match(input,ID,FOLLOW_ID_in_module272); 

                			// module names must be unique
                			if(LpnMap.containsKey((ID2!=null?ID2.getText():null))){
                				System.err.println("error on line " + ID2.getLine() + ": module " + (ID2!=null?ID2.getText():null) + " already exists");
                				System.exit(1);
                			}
                			
                			// initialize non static variables for new module
                    	    VarIndexMap = new DualHashMap<String, Integer>();
            			    ConstHashMap = new HashMap<String, Integer>();
            			    VarNodeMap = new HashMap<String, VarNode>();
            			    ArrayNodeMap = new HashMap<String, ArrayNode>();
            			    VarCountMap = new HashMap<String, Integer>();
            			    Inputs = new VarSet();
            			    Internals = new VarSet();
            			    Outputs = new VarSet();
            			    inputTranList = new ArrayList<LPNTran>();
            			    outputTranList = new ArrayList<LPNTran>();
            			    StatevectorMap = new HashMap<String, Integer>();
            			    VariableIndex = 0;
                		
            match(input,QUOTE,FOLLOW_QUOTE_in_module288); 
            match(input,GREATER,FOLLOW_GREATER_in_module290); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:142:14: ( constants )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==LESS) ) {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==58) ) {
                    alt4=1;
                }
            }
            switch (alt4) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:142:14: constants
                    {
                    pushFollow(FOLLOW_constants_in_module292);
                    constants();

                    state._fsp--;


                    }
                    break;

            }

            pushFollow(FOLLOW_variables_in_module295);
            variables();

            state._fsp--;

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:142:35: ( instantiation )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==LESS) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==61) ) {
                    alt5=1;
                }
            }
            switch (alt5) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:142:35: instantiation
                    {
                    pushFollow(FOLLOW_instantiation_in_module297);
                    instantiation();

                    state._fsp--;


                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:142:50: ( logic )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==LESS) ) {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==MARKING||LA6_1==TRANSITION) ) {
                    alt6=1;
                }
            }
            switch (alt6) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:142:50: logic
                    {
                    pushFollow(FOLLOW_logic_in_module300);
                    logic3=logic();

                    state._fsp--;


                    }
                    break;

            }

            match(input,LESS,FOLLOW_LESS_in_module303); 
            match(input,DIV,FOLLOW_DIV_in_module305); 
            match(input,MODULE,FOLLOW_MODULE_in_module307); 
            match(input,GREATER,FOLLOW_GREATER_in_module309); 

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
            	            
            	            int i = 0;
            	            int[] initialMarking = new int[(logic3!=null?logic3.initMarking:null).size()];
            	            for(Integer mark : (logic3!=null?logic3.initMarking:null)){
            	            	initialMarking[i++] = mark;
            	            }

            				lpn = new LPN(prj, (ID2!=null?ID2.getText():null), Inputs, Outputs, Internals, VarNodeMap, (logic3!=null?logic3.lpnTranSet:null), 
            	         			StatevectorMap, initialMarking);
            				
            				for(LPNTran tran : inputTranList){
            					tran.addDstLpn(lpn);
            				}
            				
            				lpn.addAllInputTrans(inputTranList);
            				lpn.addAllOutputTrans(outputTranList);
            	            lpn.setVarIndexMap(VarIndexMap);      
            	            // TODO: (temp) Hack here. May not use this in the future.
            	            (logic3!=null?logic3.lpnTranSet:null).setLPN(lpn);     
            	            prj.getDesignUnitSet().add(lpn.getStateGraph());
            	            
            	            LpnMap.put(lpn.getLabel(), lpn);
            	            
            	            // map outputs to lpn object
            	            for(String output : Outputs){
            	            	GlobalOutputMap.put(output, lpn);
            	            }
            	            
            	            // map potential output to lpn object
            	            for(String internal : Internals){
            	            	GlobalOutputMap.put(internal, lpn);
            	            }
            	            
            	            // map input variable to lpn object
            	            for(String input : Inputs){
            	            	if(GlobalInputMap.containsKey(input)){
            	    				GlobalInputMap.get(input).add(lpn);
            	    			}
            	    			else{
            	    				List<LPN> lpnList = new ArrayList<LPN>();
            	    				lpnList.add(lpn);
            	    				GlobalInputMap.put(input, lpnList);
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
        return lpn;
    }
    // $ANTLR end "module"


    // $ANTLR start "constants"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:208:1: constants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' ;
    public final void constants() throws RecognitionException {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:209:2: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:209:4: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_constants336); 
            match(input,58,FOLLOW_58_in_constants338); 
            match(input,GREATER,FOLLOW_GREATER_in_constants340); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:209:20: (const1= ID '=' val1= INT ';' )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==ID) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:209:21: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_constants345); 
            	    match(input,EQUALS,FOLLOW_EQUALS_in_constants347); 
            	    val1=(Token)match(input,INT,FOLLOW_INT_in_constants351); 

            	    				// make sure constant is not defined as something else
            	    				if(StatevectorMap.containsKey((const1!=null?const1.getText():null))){
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
            	    					System.err.println("warning on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " will be overwritten");
            	    				}
            	    			
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_constants362); 

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_constants366); 
            match(input,DIV,FOLLOW_DIV_in_constants368); 
            match(input,58,FOLLOW_58_in_constants370); 
            match(input,GREATER,FOLLOW_GREATER_in_constants372); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:234:1: globalConstants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' ;
    public final void globalConstants() throws RecognitionException {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:235:5: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:235:9: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalConstants389); 
            match(input,58,FOLLOW_58_in_globalConstants391); 
            match(input,GREATER,FOLLOW_GREATER_in_globalConstants393); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:235:25: (const1= ID '=' val1= INT ';' )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==ID) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:235:26: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_globalConstants398); 
            	    match(input,EQUALS,FOLLOW_EQUALS_in_globalConstants400); 
            	    val1=(Token)match(input,INT,FOLLOW_INT_in_globalConstants404); 

            	                	// make sure constant has not been defined already
            	                	if(GlobalVarHashMap.containsKey((const1!=null?const1.getText():null))){
            	                		System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " is already defined as a global variable");
            	                		System.exit(1);
            	                	}
            	                	
            	                	// put will override previous value
            	                    Integer result = GlobalConstHashMap.put((const1!=null?const1.getText():null), Integer.parseInt((val1!=null?val1.getText():null)));
            	                    if(result != null){
            	                        System.err.println("warning on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " will be overwritten");
            	                    }
            	                
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalConstants430); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_globalConstants434); 
            match(input,DIV,FOLLOW_DIV_in_globalConstants436); 
            match(input,58,FOLLOW_58_in_globalConstants438); 
            match(input,GREATER,FOLLOW_GREATER_in_globalConstants440); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:253:1: globalVariables : '<' 'var' '>' (var= ID '=' (val= INT | var2= ID ) ';' )* '<' '/' 'var' '>' ;
    public final void globalVariables() throws RecognitionException {
        Token var=null;
        Token val=null;
        Token var2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:254:2: ( '<' 'var' '>' (var= ID '=' (val= INT | var2= ID ) ';' )* '<' '/' 'var' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:254:4: '<' 'var' '>' (var= ID '=' (val= INT | var2= ID ) ';' )* '<' '/' 'var' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalVariables455); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_globalVariables457); 
            match(input,GREATER,FOLLOW_GREATER_in_globalVariables459); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:254:18: (var= ID '=' (val= INT | var2= ID ) ';' )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==ID) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:254:19: var= ID '=' (val= INT | var2= ID ) ';'
            	    {
            	    var=(Token)match(input,ID,FOLLOW_ID_in_globalVariables464); 

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
            	    			
            	    match(input,EQUALS,FOLLOW_EQUALS_in_globalVariables474); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:276:7: (val= INT | var2= ID )
            	    int alt9=2;
            	    int LA9_0 = input.LA(1);

            	    if ( (LA9_0==INT) ) {
            	        alt9=1;
            	    }
            	    else if ( (LA9_0==ID) ) {
            	        alt9=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 9, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt9) {
            	        case 1 :
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:276:8: val= INT
            	            {
            	            val=(Token)match(input,INT,FOLLOW_INT_in_globalVariables479); 

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
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:291:5: var2= ID
            	            {
            	            var2=(Token)match(input,ID,FOLLOW_ID_in_globalVariables493); 

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
            	            				
            	            				// make sure global variable is consistently initialized
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

            	    }

            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalVariables503); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_globalVariables507); 
            match(input,DIV,FOLLOW_DIV_in_globalVariables509); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_globalVariables511); 
            match(input,GREATER,FOLLOW_GREATER_in_globalVariables513); 

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


    // $ANTLR start "variables"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:325:1: variables : '<' 'var' '>' ( (var= ID '=' (val= INT | var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID | val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )* '<' '/' 'var' '>' ;
    public final void variables() throws RecognitionException {
        Token var=null;
        Token val=null;
        Token var2=null;
        Token arrayName=null;
        Token dim=null;
        Token val2=null;
        Token val3=null;
        Token val4=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:326:2: ( '<' 'var' '>' ( (var= ID '=' (val= INT | var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID | val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )* '<' '/' 'var' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:326:4: '<' 'var' '>' ( (var= ID '=' (val= INT | var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID | val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )* '<' '/' 'var' '>'
            {
            Integer value = null; Token varNode = null;
            match(input,LESS,FOLLOW_LESS_in_variables529); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_variables531); 
            match(input,GREATER,FOLLOW_GREATER_in_variables533); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:327:17: ( (var= ID '=' (val= INT | var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID | val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )*
            loop16:
            do {
                int alt16=3;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==ID) ) {
                    int LA16_2 = input.LA(2);

                    if ( (LA16_2==EQUALS) ) {
                        alt16=1;
                    }
                    else if ( (LA16_2==59) ) {
                        alt16=2;
                    }


                }


                switch (alt16) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:327:18: (var= ID '=' (val= INT | var2= ID ) ';' )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:327:18: (var= ID '=' (val= INT | var2= ID ) ';' )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:327:19: var= ID '=' (val= INT | var2= ID ) ';'
            	    {
            	    var=(Token)match(input,ID,FOLLOW_ID_in_variables539); 

            	    				// check variable is unique in scope
            	    				if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
            	    					System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is a global constant"); 
            	        				System.exit(1);
            	    				}
            	    				else if(GlobalVarHashMap.containsKey((var!=null?var.getText():null))){
            	    					System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is a global variable"); 
            	        				System.exit(1);
            	    				}
            	    				else if(StatevectorMap.containsKey((var!=null?var.getText():null))){
            	    					System.err.println("warning on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " will be overwritten");
            	    				}
            	    				
            	    				varNode = var;
            	    			
            	    match(input,EQUALS,FOLLOW_EQUALS_in_variables549); 
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:344:7: (val= INT | var2= ID )
            	    int alt11=2;
            	    int LA11_0 = input.LA(1);

            	    if ( (LA11_0==INT) ) {
            	        alt11=1;
            	    }
            	    else if ( (LA11_0==ID) ) {
            	        alt11=2;
            	    }
            	    else {
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 11, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt11) {
            	        case 1 :
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:344:8: val= INT
            	            {
            	            val=(Token)match(input,INT,FOLLOW_INT_in_variables554); 

            	            				// get variable initial value
            	            				value = Integer.parseInt((val!=null?val.getText():null));
            	            			

            	            }
            	            break;
            	        case 2 :
            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:349:5: var2= ID
            	            {
            	            var2=(Token)match(input,ID,FOLLOW_ID_in_variables568); 

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
            	            					value = StatevectorMap.get((var2!=null?var2.getText():null));
            	            				}
            	            				else{
            	            					System.err.println("error on line " + var2.getLine() + ": " + (var2!=null?var2.getText():null) + " is not defined"); 
            	                				System.exit(1);
            	            				}
            	            				
            	            				varNode = var2;
            	            			

            	            }
            	            break;

            	    }

            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_variables578); 

            	    }


            	    				// add variable and value to state vector
            	    				StatevectorMap.put(varNode.getText(), value);
            	    				
            	    				// generate variable index and create new var node  
            	    				int index = VariableIndex++;
            	       				VarIndexMap.insert(varNode.getText(), index);
            	       				VarNodeMap.put(varNode.getText(), new VarNode(varNode.getText(), index));
            	        			
            	        			// if associated input variable has been defined label as output, else label as internal
            	    				if(!GlobalInterfaceMap.containsKey(varNode.getText())){
            	    					Internals.add(varNode.getText());
            	    				}
            	    				else{
            	    					if(GlobalInterfaceMap.get(varNode.getText()) != null){
            	    						System.err.println("error on line " + varNode.getLine() + ": variable '" + varNode.getText() + "' has already been declared in another module");
            	    						System.exit(1);
            	    					}
            	    					Outputs.add(varNode.getText());
            	    					
            	    					// initialize associated input variables with output value
            	    					List<LPN> lpnList = GlobalInputMap.get(varNode.getText());
            	    					if(lpnList != null){
            	    						for(LPN lpn : lpnList){
            	    							lpn.getInitVector().put(varNode.getText(), value);
            	    						}
            	    					}
            	    				}
            	    				
            	    				GlobalInterfaceMap.put(varNode.getText(), value);
            	    				VarCountMap.put(varNode.getText(), 0);
            	    			

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:404:5: (arrayName= ID ( '[' (dim= ID | val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:404:5: (arrayName= ID ( '[' (dim= ID | val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:404:6: arrayName= ID ( '[' (dim= ID | val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';'
            	    {
            	    arrayName=(Token)match(input,ID,FOLLOW_ID_in_variables593); 

            	    				List<Integer> dimensionsList = new ArrayList<Integer>();
            	    			
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:408:3: ( '[' (dim= ID | val2= INT ) ']' )+
            	    int cnt13=0;
            	    loop13:
            	    do {
            	        int alt13=2;
            	        int LA13_0 = input.LA(1);

            	        if ( (LA13_0==59) ) {
            	            alt13=1;
            	        }


            	        switch (alt13) {
            	    	case 1 :
            	    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:408:4: '[' (dim= ID | val2= INT ) ']'
            	    	    {
            	    	    match(input,59,FOLLOW_59_in_variables603); 
            	    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:408:8: (dim= ID | val2= INT )
            	    	    int alt12=2;
            	    	    int LA12_0 = input.LA(1);

            	    	    if ( (LA12_0==ID) ) {
            	    	        alt12=1;
            	    	    }
            	    	    else if ( (LA12_0==INT) ) {
            	    	        alt12=2;
            	    	    }
            	    	    else {
            	    	        NoViableAltException nvae =
            	    	            new NoViableAltException("", 12, 0, input);

            	    	        throw nvae;
            	    	    }
            	    	    switch (alt12) {
            	    	        case 1 :
            	    	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:408:9: dim= ID
            	    	            {
            	    	            dim=(Token)match(input,ID,FOLLOW_ID_in_variables608); 

            	    	            				// get variable value
            	    	            				if(GlobalConstHashMap.containsKey((dim!=null?dim.getText():null))){
            	    	            					value = GlobalConstHashMap.get((dim!=null?dim.getText():null));
            	    	            				}
            	    	            				else if(GlobalVarHashMap.containsKey((dim!=null?dim.getText():null))){
            	    	            					value = GlobalVarHashMap.get((dim!=null?dim.getText():null));
            	    	            				}
            	    	            				else if(ConstHashMap.containsKey((dim!=null?dim.getText():null))){
            	    	            					value = ConstHashMap.get((dim!=null?dim.getText():null));
            	    	            				}
            	    	            				else if(StatevectorMap.containsKey((dim!=null?dim.getText():null))){ // Should var be allowed to assign a var?
            	    	            					value = StatevectorMap.get((dim!=null?dim.getText():null));
            	    	            				}
            	    	            				else{
            	    	            					System.err.println("error on line " + dim.getLine() + ": " + (dim!=null?dim.getText():null) + " is not defined"); 
            	    	                				System.exit(1);
            	    	            				}
            	    	            				
            	    	            				dimensionsList.add(value);
            	    	            			

            	    	            }
            	    	            break;
            	    	        case 2 :
            	    	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:430:5: val2= INT
            	    	            {
            	    	            val2=(Token)match(input,INT,FOLLOW_INT_in_variables622); 

            	    	            				dimensionsList.add(Integer.parseInt((val2!=null?val2.getText():null)));
            	    	            			

            	    	            }
            	    	            break;

            	    	    }

            	    	    match(input,60,FOLLOW_60_in_variables633); 

            	    	    }
            	    	    break;

            	    	default :
            	    	    if ( cnt13 >= 1 ) break loop13;
            	                EarlyExitException eee =
            	                    new EarlyExitException(13, input);
            	                throw eee;
            	        }
            	        cnt13++;
            	    } while (true);

            	    match(input,EQUALS,FOLLOW_EQUALS_in_variables638); 

            	    				List<Integer> valueList = new ArrayList<Integer>();
            	    			
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:438:3: ( '(' (val3= INT ',' )* val4= INT ')' )+
            	    int cnt15=0;
            	    loop15:
            	    do {
            	        int alt15=2;
            	        int LA15_0 = input.LA(1);

            	        if ( (LA15_0==LPAREN) ) {
            	            alt15=1;
            	        }


            	        switch (alt15) {
            	    	case 1 :
            	    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:438:4: '(' (val3= INT ',' )* val4= INT ')'
            	    	    {
            	    	    match(input,LPAREN,FOLLOW_LPAREN_in_variables649); 
            	    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:438:8: (val3= INT ',' )*
            	    	    loop14:
            	    	    do {
            	    	        int alt14=2;
            	    	        int LA14_0 = input.LA(1);

            	    	        if ( (LA14_0==INT) ) {
            	    	            int LA14_1 = input.LA(2);

            	    	            if ( (LA14_1==COMMA) ) {
            	    	                alt14=1;
            	    	            }


            	    	        }


            	    	        switch (alt14) {
            	    	    	case 1 :
            	    	    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:438:9: val3= INT ','
            	    	    	    {
            	    	    	    val3=(Token)match(input,INT,FOLLOW_INT_in_variables654); 
            	    	    	    match(input,COMMA,FOLLOW_COMMA_in_variables656); 

            	    	    	    				valueList.add(Integer.parseInt((val3!=null?val3.getText():null)));
            	    	    	    			

            	    	    	    }
            	    	    	    break;

            	    	    	default :
            	    	    	    break loop14;
            	    	        }
            	    	    } while (true);

            	    	    val4=(Token)match(input,INT,FOLLOW_INT_in_variables670); 

            	    	    				valueList.add(Integer.parseInt((val4!=null?val4.getText():null)));
            	    	    			
            	    	    match(input,RPAREN,FOLLOW_RPAREN_in_variables680); 

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

            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_variables687); 

            	    				if(valueList.size() != dimensionsList.get(0)){
            	    					System.err.println("error: incompatible number of elements in " + (arrayName!=null?arrayName.getText():null));
            	    					System.exit(1);
            	    				}
            	    				
            	    				if(dimensionsList.size() == 1){
            	    					int varCount = 0;
            	    					int arraySize = dimensionsList.get(0);
            	    					List<Object> array = new ArrayList<Object>(arraySize);
            	    					for(int i = 0; i < arraySize; i++){
            	    						String name = (arrayName!=null?arrayName.getText():null) + varCount++;
            	    						int index = VariableIndex++;
            	    						VarNode v = new VarNode(name, index);
            	    						array.add(v);
            	    						
            	    						// add variable and value to state vector
            	    						StatevectorMap.put(name, 0);
            	    						
            	    						// generate variable index and create new var node  
            	    		   				VarIndexMap.insert(name, index);
            	    					}
            	    					
            	    //					ArrayNodeMap.put((arrayName!=null?arrayName.getText():null), new ArrayNode((arrayName!=null?arrayName.getText():null), array, 1));
            	    					VarCountMap.put((arrayName!=null?arrayName.getText():null), 0);
            	    				}
            	    				else{
            	    				}
            	    			

            	    }


            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_variables701); 
            match(input,DIV,FOLLOW_DIV_in_variables703); 
            match(input,INTERNAL,FOLLOW_INTERNAL_in_variables705); 
            match(input,GREATER,FOLLOW_GREATER_in_variables707); 

            				// add global variables to initial state vector and label as an input & output
            				for(Entry<String, Integer> e : GlobalVarHashMap.entrySet()){
            					String globalVar = e.getKey();
            					StatevectorMap.put(globalVar, e.getValue());
            					
            					Integer index =  VariableIndex++;
            	    			VarIndexMap.insert(globalVar, index);
            	    			VarNodeMap.put(globalVar, new VarNode(globalVar, index));
            	    			
            	    			Inputs.add(globalVar);
            	    			Outputs.add(globalVar);
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
    // $ANTLR end "variables"


    // $ANTLR start "instantiation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:494:1: instantiation : '<' 'inst' '>' modName= ID instName= ID '(' (mod= ID '.' var= ID )+ ')' '<' '/' 'inst' '>' ;
    public final void instantiation() throws RecognitionException {
        Token modName=null;
        Token instName=null;
        Token mod=null;
        Token var=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:495:5: ( '<' 'inst' '>' modName= ID instName= ID '(' (mod= ID '.' var= ID )+ ')' '<' '/' 'inst' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:495:9: '<' 'inst' '>' modName= ID instName= ID '(' (mod= ID '.' var= ID )+ ')' '<' '/' 'inst' '>'
            {
            HashMap<String, String> portMap = new HashMap<String, String>();
            match(input,LESS,FOLLOW_LESS_in_instantiation736); 
            match(input,61,FOLLOW_61_in_instantiation738); 
            match(input,GREATER,FOLLOW_GREATER_in_instantiation740); 
            modName=(Token)match(input,ID,FOLLOW_ID_in_instantiation749); 
            instName=(Token)match(input,ID,FOLLOW_ID_in_instantiation753); 
            match(input,LPAREN,FOLLOW_LPAREN_in_instantiation755); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:497:32: (mod= ID '.' var= ID )+
            int cnt17=0;
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==ID) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:497:33: mod= ID '.' var= ID
            	    {
            	    mod=(Token)match(input,ID,FOLLOW_ID_in_instantiation759); 
            	    match(input,PERIOD,FOLLOW_PERIOD_in_instantiation761); 
            	    var=(Token)match(input,ID,FOLLOW_ID_in_instantiation765); 

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

            match(input,RPAREN,FOLLOW_RPAREN_in_instantiation769); 
            match(input,LESS,FOLLOW_LESS_in_instantiation776); 
            match(input,DIV,FOLLOW_DIV_in_instantiation778); 
            match(input,61,FOLLOW_61_in_instantiation780); 
            match(input,GREATER,FOLLOW_GREATER_in_instantiation782); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:501:1: logic returns [List<Integer> initMarking, LpnTranList lpnTranSet] : marking ( transition )+ ;
    public final PlatuGrammarParser.logic_return logic() throws RecognitionException {
        PlatuGrammarParser.logic_return retval = new PlatuGrammarParser.logic_return();
        retval.start = input.LT(1);

        LPNTran transition4 = null;

        List marking5 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:502:5: ( marking ( transition )+ )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:502:9: marking ( transition )+
            {
            retval.lpnTranSet = new LpnTranList();
            pushFollow(FOLLOW_marking_in_logic812);
            marking5=marking();

            state._fsp--;

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:503:14: ( transition )+
            int cnt18=0;
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==LESS) ) {
                    int LA18_1 = input.LA(2);

                    if ( (LA18_1==TRANSITION) ) {
                        alt18=1;
                    }


                }


                switch (alt18) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:503:15: transition
            	    {
            	    pushFollow(FOLLOW_transition_in_logic815);
            	    transition4=transition();

            	    state._fsp--;

            	    retval.lpnTranSet.add(transition4);

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


                        retval.initMarking = marking5;
                    

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:509:1: marking returns [List mark] : ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/' 'marking' '>' )? ;
    public final List marking() throws RecognitionException {
        List mark = null;

        Token m1=null;
        Token c1=null;
        Token m2=null;
        Token c2=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:510:5: ( ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/' 'marking' '>' )? )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:510:9: ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/' 'marking' '>' )?
            {
            mark = new LinkedList<Integer>(); Integer result;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:511:9: ( '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/' 'marking' '>' )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==LESS) ) {
                int LA23_1 = input.LA(2);

                if ( (LA23_1==MARKING) ) {
                    alt23=1;
                }
            }
            switch (alt23) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:511:10: '<' 'marking' '>' ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )? '<' '/' 'marking' '>'
                    {
                    match(input,LESS,FOLLOW_LESS_in_marking868); 
                    match(input,MARKING,FOLLOW_MARKING_in_marking870); 
                    match(input,GREATER,FOLLOW_GREATER_in_marking872); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:511:28: ( (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )* )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( ((LA22_0>=ID && LA22_0<=INT)) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:511:29: (m1= INT | c1= ID ) ( ',' (m2= INT | c2= ID ) )*
                            {
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:511:29: (m1= INT | c1= ID )
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
                                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:511:30: m1= INT
                                    {
                                    m1=(Token)match(input,INT,FOLLOW_INT_in_marking878); 

                                            		mark.add(Integer.parseInt((m1!=null?m1.getText():null)));
                                            	

                                    }
                                    break;
                                case 2 :
                                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:515:11: c1= ID
                                    {
                                    c1=(Token)match(input,ID,FOLLOW_ID_in_marking904); 

                                            		result = ConstHashMap.get((c1!=null?c1.getText():null));
                                            		if(result == null){
                                            			System.err.println("error on line " + c1.getLine() + ": " + (c1!=null?c1.getText():null) + " is not a valid constant");
                                            			System.exit(1);
                                            		}
                                            		
                                            		mark.add(result);
                                            	

                                    }
                                    break;

                            }

                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:525:11: ( ',' (m2= INT | c2= ID ) )*
                            loop21:
                            do {
                                int alt21=2;
                                int LA21_0 = input.LA(1);

                                if ( (LA21_0==COMMA) ) {
                                    alt21=1;
                                }


                                switch (alt21) {
                            	case 1 :
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:525:12: ',' (m2= INT | c2= ID )
                            	    {
                            	    match(input,COMMA,FOLLOW_COMMA_in_marking928); 
                            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:525:16: (m2= INT | c2= ID )
                            	    int alt20=2;
                            	    int LA20_0 = input.LA(1);

                            	    if ( (LA20_0==INT) ) {
                            	        alt20=1;
                            	    }
                            	    else if ( (LA20_0==ID) ) {
                            	        alt20=2;
                            	    }
                            	    else {
                            	        NoViableAltException nvae =
                            	            new NoViableAltException("", 20, 0, input);

                            	        throw nvae;
                            	    }
                            	    switch (alt20) {
                            	        case 1 :
                            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:525:17: m2= INT
                            	            {
                            	            m2=(Token)match(input,INT,FOLLOW_INT_in_marking933); 

                            	                    		mark.add(Integer.parseInt((m2!=null?m2.getText():null)));
                            	                    	

                            	            }
                            	            break;
                            	        case 2 :
                            	            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:529:11: c2= ID
                            	            {
                            	            c2=(Token)match(input,ID,FOLLOW_ID_in_marking959); 

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
                            	    break loop21;
                                }
                            } while (true);


                            }
                            break;

                    }

                    match(input,LESS,FOLLOW_LESS_in_marking986); 
                    match(input,DIV,FOLLOW_DIV_in_marking988); 
                    match(input,MARKING,FOLLOW_MARKING_in_marking990); 
                    match(input,GREATER,FOLLOW_GREATER_in_marking992); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:542:1: transition returns [LPNTran lpnTran] : '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/' 'transition' '>' ;
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
        Expression guard6 = null;

        PlatuGrammarParser.delay_return delay7 = null;

        Expression assertion8 = null;

        VarExpr assignment9 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:543:5: ( '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/' 'transition' '>' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:543:10: '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/' 'transition' '>'
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
            	    	
            match(input,LESS,FOLLOW_LESS_in_transition1026); 
            match(input,TRANSITION,FOLLOW_TRANSITION_in_transition1028); 
            match(input,LABEL,FOLLOW_LABEL_in_transition1030); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1032); 
            match(input,QUOTE,FOLLOW_QUOTE_in_transition1034); 
            lbl=(Token)input.LT(1);
            if ( (input.LA(1)>=ID && input.LA(1)<=INT) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            match(input,QUOTE,FOLLOW_QUOTE_in_transition1044); 
            match(input,PRESET,FOLLOW_PRESET_in_transition1046); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1048); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:554:69: ( '\"' '\"' | ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==QUOTE) ) {
                int LA26_1 = input.LA(2);

                if ( (LA26_1==QUOTE) ) {
                    alt26=1;
                }
                else if ( ((LA26_1>=ID && LA26_1<=INT)) ) {
                    alt26=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 26, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:554:70: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1051); 
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1053); 

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:554:80: ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:554:80: ( '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:554:81: '\"' (pre= INT | pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1058); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:554:85: (pre= INT | pre1= ID )
                    int alt24=2;
                    int LA24_0 = input.LA(1);

                    if ( (LA24_0==INT) ) {
                        alt24=1;
                    }
                    else if ( (LA24_0==ID) ) {
                        alt24=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 24, 0, input);

                        throw nvae;
                    }
                    switch (alt24) {
                        case 1 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:554:86: pre= INT
                            {
                            pre=(Token)match(input,INT,FOLLOW_INT_in_transition1063); 

                                			presetList.add(Integer.parseInt((pre!=null?pre.getText():null)));
                               			

                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:558:7: pre1= ID
                            {
                            pre1=(Token)match(input,ID,FOLLOW_ID_in_transition1083); 

                              				result = ConstHashMap.get((pre1!=null?pre1.getText():null)); 
                              				if(result == null){
                              					System.err.println("error on line " + pre1.getLine() + ": " + (pre1!=null?pre1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				
                              				presetList.add(result);
                              			

                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:568:6: ( ',' pre2= INT | ',' pre3= ID )*
                    loop25:
                    do {
                        int alt25=3;
                        int LA25_0 = input.LA(1);

                        if ( (LA25_0==COMMA) ) {
                            int LA25_2 = input.LA(2);

                            if ( (LA25_2==INT) ) {
                                alt25=1;
                            }
                            else if ( (LA25_2==ID) ) {
                                alt25=2;
                            }


                        }


                        switch (alt25) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:568:8: ',' pre2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1099); 
                    	    pre2=(Token)match(input,INT,FOLLOW_INT_in_transition1103); 

                    	        			presetList.add(Integer.parseInt((pre2!=null?pre2.getText():null)));
                    	       			

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:572:7: ',' pre3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1121); 
                    	    pre3=(Token)match(input,ID,FOLLOW_ID_in_transition1125); 

                    	      				result = ConstHashMap.get((pre3!=null?pre3.getText():null)); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + pre3.getLine() + ": " + (pre3!=null?pre3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}
                    	      				
                    	      				presetList.add(result);
                    	      			

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1140); 

                    }


                    }
                    break;

            }

            match(input,POSTSET,FOLLOW_POSTSET_in_transition1144); 
            match(input,EQUALS,FOLLOW_EQUALS_in_transition1146); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:582:27: ( '\"' '\"' | ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==QUOTE) ) {
                int LA29_1 = input.LA(2);

                if ( (LA29_1==QUOTE) ) {
                    alt29=1;
                }
                else if ( ((LA29_1>=ID && LA29_1<=INT)) ) {
                    alt29=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 29, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:582:29: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1150); 
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1152); 

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:582:39: ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:582:39: ( '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:582:40: '\"' (post= INT | post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1157); 
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:582:44: (post= INT | post1= ID )
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
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:582:46: post= INT
                            {
                            post=(Token)match(input,INT,FOLLOW_INT_in_transition1163); 

                                			postsetList.add(Integer.parseInt((post!=null?post.getText():null)));
                                		

                            }
                            break;
                        case 2 :
                            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:586:8: post1= ID
                            {
                            post1=(Token)match(input,ID,FOLLOW_ID_in_transition1183); 

                                			result = ConstHashMap.get((post1!=null?post1.getText():null)); 
                              				if(result == null){
                              					System.err.println("error on line " + post1.getLine() + ": " + (post1!=null?post1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				
                              				postsetList.add(result);
                                		

                            }
                            break;

                    }

                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:597:6: ( ( ',' post2= INT ) | ( ',' post3= ID ) )*
                    loop28:
                    do {
                        int alt28=3;
                        int LA28_0 = input.LA(1);

                        if ( (LA28_0==COMMA) ) {
                            int LA28_2 = input.LA(2);

                            if ( (LA28_2==INT) ) {
                                alt28=1;
                            }
                            else if ( (LA28_2==ID) ) {
                                alt28=2;
                            }


                        }


                        switch (alt28) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:597:8: ( ',' post2= INT )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:597:8: ( ',' post2= INT )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:597:9: ',' post2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1208); 
                    	    post2=(Token)match(input,INT,FOLLOW_INT_in_transition1212); 

                    	    }


                    	        			postsetList.add(Integer.parseInt((post2!=null?post2.getText():null)));
                    	        		

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:601:8: ( ',' post3= ID )
                    	    {
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:601:8: ( ',' post3= ID )
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:601:9: ',' post3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1232); 
                    	    post3=(Token)match(input,ID,FOLLOW_ID_in_transition1235); 

                    	    }


                    	        			result = ConstHashMap.get((post3!=null?post3.getText():null)); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + post3.getLine() + ": " + (post3!=null?post3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}
                    	      				
                    	      				postsetList.add(result);
                    	        		

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1254); 

                    }


                    }
                    break;

            }

            match(input,GREATER,FOLLOW_GREATER_in_transition1259); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:611:20: ( guard )?
            int alt30=2;
            switch ( input.LA(1) ) {
                case INT:
                case TRUE:
                case FALSE:
                case PLUS:
                case MINUS:
                case NEGATION:
                case BITWISE_NEGATION:
                    {
                    alt30=1;
                    }
                    break;
                case ID:
                    {
                    int LA30_2 = input.LA(2);

                    if ( (LA30_2==QMARK||LA30_2==SEMICOLON||(LA30_2>=PLUS && LA30_2<=MOD)||(LA30_2>=GREATER && LA30_2<=NOT_EQUIV)||(LA30_2>=AND && LA30_2<=IMPLICATION)||(LA30_2>=BITWISE_AND && LA30_2<=BITWISE_RSHIFT)) ) {
                        alt30=1;
                    }
                    }
                    break;
                case LPAREN:
                    {
                    int LA30_3 = input.LA(2);

                    if ( (LA30_3==INT) ) {
                        int LA30_5 = input.LA(3);

                        if ( (LA30_5==RPAREN||LA30_5==QMARK||(LA30_5>=PLUS && LA30_5<=MOD)||(LA30_5>=GREATER && LA30_5<=NOT_EQUIV)||(LA30_5>=AND && LA30_5<=IMPLICATION)||(LA30_5>=BITWISE_AND && LA30_5<=BITWISE_RSHIFT)) ) {
                            alt30=1;
                        }
                    }
                    else if ( (LA30_3==ID||LA30_3==LPAREN||(LA30_3>=TRUE && LA30_3<=FALSE)||(LA30_3>=PLUS && LA30_3<=MINUS)||LA30_3==NEGATION||LA30_3==BITWISE_NEGATION) ) {
                        alt30=1;
                    }
                    }
                    break;
            }

            switch (alt30) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:611:21: guard
                    {
                    pushFollow(FOLLOW_guard_in_transition1262);
                    guard6=guard();

                    state._fsp--;


                        			guardExpr = guard6;
                        		

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:615:9: ( delay )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==LPAREN) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:615:10: delay
                    {
                    pushFollow(FOLLOW_delay_in_transition1282);
                    delay7=delay();

                    state._fsp--;


                        			delayLB = (delay7!=null?delay7.delayLB:0); 
                        			delayUB = (delay7!=null?delay7.delayUB:0);
                        		

                    }
                    break;

            }

            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:620:9: ( ( assertion ) | ( assignment ) )*
            loop32:
            do {
                int alt32=3;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==62) ) {
                    alt32=1;
                }
                else if ( (LA32_0==ID) ) {
                    alt32=2;
                }


                switch (alt32) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:620:10: ( assertion )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:620:10: ( assertion )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:620:11: assertion
            	    {
            	    pushFollow(FOLLOW_assertion_in_transition1303);
            	    assertion8=assertion();

            	    state._fsp--;


            	        			if(assertion8 != null){		
            	      					assertionList.add(assertion8);
            	      				}
            	        		

            	    }


            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:626:10: ( assignment )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:626:10: ( assignment )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:626:11: assignment
            	    {
            	    pushFollow(FOLLOW_assignment_in_transition1323);
            	    assignment9=assignment();

            	    state._fsp--;


            	        			assignmentList.add(assignment9);
            	        		

            	    }


            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);

            match(input,LESS,FOLLOW_LESS_in_transition1342); 
            match(input,DIV,FOLLOW_DIV_in_transition1344); 
            match(input,TRANSITION,FOLLOW_TRANSITION_in_transition1346); 
            match(input,GREATER,FOLLOW_GREATER_in_transition1348); 

                    	// create new lpn transitions and add assertions
                    	lpnTran = new LPNTran((lbl!=null?lbl.getText():null), TransitionIndex++, presetList, postsetList, guardExpr, assignmentList, delayLB, delayUB, local);
                    	if(assertionList.size() > 0){
                    		lpnTran.addAllAssertions(assertionList);
                    	}
                    	
                    	// add non-local transition to associated LPNs
                    	for(VarExpr e : assignmentList){
                    		VarNode var = e.getVar();
                    		if(Outputs.contains(var.getName())){
                    			local = false;
                    			
                    			if(GlobalInputMap.containsKey(var.getName())){
            	        			for(LPN lpn : GlobalInputMap.get(var.getName())){
            	        				lpn.addInputTran(lpnTran);
            	        				lpnTran.addDstLpn(lpn);
            	        			}
                    			}
                    		}
                    		
                    		// map lpn transition with output and potential outuput variables
                    		if(GlobalTranMap.containsKey(var.getName())){
                   				GlobalTranMap.get(var.getName()).add(lpnTran);
                   			}
                   			else{
                   				List<LPNTran> tranList = new ArrayList<LPNTran>();
                   				tranList.add(lpnTran);
                   				GlobalTranMap.put(var.getName(), tranList);
                   			}
                    	}
                    	
                   		lpnTran.setLocalFlag(local);
                   		if(local == false){
                   			outputTranList.add(lpnTran);
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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:670:1: assertion returns [Expression booleanExpr] : 'assert' '(' expression ')' ';' ;
    public final Expression assertion() throws RecognitionException {
        Expression booleanExpr = null;

        ExpressionNode expression10 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:671:2: ( 'assert' '(' expression ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:671:4: 'assert' '(' expression ')' ';'
            {
            booleanExpr = null;
            match(input,62,FOLLOW_62_in_assertion1382); 
            match(input,LPAREN,FOLLOW_LPAREN_in_assertion1384); 
            pushFollow(FOLLOW_expression_in_assertion1386);
            expression10=expression();

            state._fsp--;

            match(input,RPAREN,FOLLOW_RPAREN_in_assertion1388); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assertion1390); 

            				booleanExpr = new Expression(expression10);
            			

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:678:1: guard returns [Expression expr] : expression ';' ;
    public final Expression guard() throws RecognitionException {
        Expression expr = null;

        ExpressionNode expression11 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:679:5: ( expression ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:679:9: expression ';'
            {
            pushFollow(FOLLOW_expression_in_guard1416);
            expression11=expression();

            state._fsp--;

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_guard1418); 

               				expr = new Expression(expression11);
                		

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:685:1: delay returns [int delayLB, int delayUB] : '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' ;
    public final PlatuGrammarParser.delay_return delay() throws RecognitionException {
        PlatuGrammarParser.delay_return retval = new PlatuGrammarParser.delay_return();
        retval.start = input.LT(1);

        Token lb=null;
        Token ub=null;

        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:686:5: ( '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:686:8: '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';'
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_delay1452); 
            lb=(Token)match(input,INT,FOLLOW_INT_in_delay1456); 

                			retval.delayLB = Integer.parseInt((lb!=null?lb.getText():null));
               			
            match(input,COMMA,FOLLOW_COMMA_in_delay1470); 
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:690:8: (ub= INT | 'inf' )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==INT) ) {
                alt33=1;
            }
            else if ( (LA33_0==63) ) {
                alt33=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:690:9: ub= INT
                    {
                    ub=(Token)match(input,INT,FOLLOW_INT_in_delay1475); 

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
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:707:6: 'inf'
                    {
                    match(input,63,FOLLOW_63_in_delay1490); 

                     				retval.delayUB = INFINITY;
                    			

                    }
                    break;

            }

            match(input,RPAREN,FOLLOW_RPAREN_in_delay1503); 
            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_delay1505); 

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
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:714:1: assignment returns [VarExpr assign] : ( (var= ID '=' expression ';' ) | (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' ) );
    public final VarExpr assignment() throws RecognitionException {
        VarExpr assign = null;

        Token var=null;
        Token var2=null;
        ExpressionNode expression12 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:715:5: ( (var= ID '=' expression ';' ) | (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' ) )
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==ID) ) {
                int LA35_1 = input.LA(2);

                if ( (LA35_1==EQUALS) ) {
                    alt35=1;
                }
                else if ( (LA35_1==59) ) {
                    alt35=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 35, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }
            switch (alt35) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:715:9: (var= ID '=' expression ';' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:715:9: (var= ID '=' expression ';' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:715:10: var= ID '=' expression ';'
                    {
                    var=(Token)match(input,ID,FOLLOW_ID_in_assignment1531); 
                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1533); 
                    	
                        			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var.getLine() + ": global constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var.getLine() + ": constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains((var!=null?var.getText():null)) && !Internals.contains((var!=null?var.getText():null))){
                        				System.err.println("error on line " + var.getLine() + ": input variable " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        		
                    pushFollow(FOLLOW_expression_in_assignment1549);
                    expression12=expression();

                    state._fsp--;

                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1551); 

                    	    		Integer varCount = VarCountMap.get((var!=null?var.getText():null));
                    	    		if(varCount != null){
                    	    			VarCountMap.put((var!=null?var.getText():null), ++varCount);
                    	    		}
                    	    		
                    	    		Expression expr = new Expression(expression12);
                    	    		assign = new VarExpr(VarNodeMap.get((var!=null?var.getText():null)), expr);
                    	   		

                    }


                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:741:10: (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' )
                    {
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:741:10: (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' )
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:741:11: var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';'
                    {
                    var2=(Token)match(input,ID,FOLLOW_ID_in_assignment1573); 

                    	   			List<Integer> indexList = new ArrayList<Integer>();
                    	   			
                    	   		
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:746:6: ( '[' ( INT | ID ) ']' )+
                    int cnt34=0;
                    loop34:
                    do {
                        int alt34=2;
                        int LA34_0 = input.LA(1);

                        if ( (LA34_0==59) ) {
                            alt34=1;
                        }


                        switch (alt34) {
                    	case 1 :
                    	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:746:7: '[' ( INT | ID ) ']'
                    	    {
                    	    match(input,59,FOLLOW_59_in_assignment1590); 
                    	    if ( (input.LA(1)>=ID && input.LA(1)<=INT) ) {
                    	        input.consume();
                    	        state.errorRecovery=false;
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        throw mse;
                    	    }

                    	    match(input,60,FOLLOW_60_in_assignment1600); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt34 >= 1 ) break loop34;
                                EarlyExitException eee =
                                    new EarlyExitException(34, input);
                                throw eee;
                        }
                        cnt34++;
                    } while (true);

                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1604); 
                    pushFollow(FOLLOW_expression_in_assignment1606);
                    expression();

                    state._fsp--;

                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1608); 

                    	   		
                    	   		

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


    // $ANTLR start "term"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:754:1: term returns [ExpressionNode expr] : ( ID | LPAREN expression RPAREN | INT | TRUE | FALSE );
    public final ExpressionNode term() throws RecognitionException {
        ExpressionNode expr = null;

        Token ID13=null;
        Token INT15=null;
        ExpressionNode expression14 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:755:5: ( ID | LPAREN expression RPAREN | INT | TRUE | FALSE )
            int alt36=5;
            switch ( input.LA(1) ) {
            case ID:
                {
                alt36=1;
                }
                break;
            case LPAREN:
                {
                alt36=2;
                }
                break;
            case INT:
                {
                alt36=3;
                }
                break;
            case TRUE:
                {
                alt36=4;
                }
                break;
            case FALSE:
                {
                alt36=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }

            switch (alt36) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:755:9: ID
                    {
                    ID13=(Token)match(input,ID,FOLLOW_ID_in_term1651); 

                        			if(ConstHashMap.containsKey((ID13!=null?ID13.getText():null))){
                        				expr = new ConstNode((ID13!=null?ID13.getText():null), ConstHashMap.get((ID13!=null?ID13.getText():null)));
                        			}
                        			else if(GlobalConstHashMap.containsKey((ID13!=null?ID13.getText():null))){
                        			 expr = new ConstNode((ID13!=null?ID13.getText():null), GlobalConstHashMap.get((ID13!=null?ID13.getText():null)));
                        			}
                        			else if(StatevectorMap.containsKey((ID13!=null?ID13.getText():null))){ 
                    //    				expr = new platu.lpn.io.expression.VarNode((ID13!=null?ID13.getText():null));
                    					expr = VarNodeMap.get((ID13!=null?ID13.getText():null));
                        			}
                        			else{ // identify new input variable
                    //    				// create expression
                    //					expr = new platu.lpn.io.expression.VarNode((ID13!=null?ID13.getText():null));
                    					
                    					// label as input and initialize to 0
                    					StatevectorMap.put((ID13!=null?ID13.getText():null), 0);
                    					Inputs.add((ID13!=null?ID13.getText():null));
                    					
                    					// generate a varaible index and create new variable object
                    					int index = VariableIndex++;
                    	    			VarIndexMap.insert((ID13!=null?ID13.getText():null), index);
                    	    			VarNode newVarNode = new VarNode((ID13!=null?ID13.getText():null), index);
                    	    			VarNodeMap.put((ID13!=null?ID13.getText():null), newVarNode);
                    	    			expr = newVarNode;
                    	    			
                    	    			// if associated output variable has not been defined insert with null value,
                    	    			// otherwise get output variable and relabel from internal to output, 
                    	    			// get output value and initialize input statevector, label lpn transitions associated with output as non-local
                    	    			// and add to current lpn's inputTranList
                    	    			if(!GlobalInterfaceMap.containsKey((ID13!=null?ID13.getText():null))){
                    	    				GlobalInterfaceMap.put((ID13!=null?ID13.getText():null), null);
                    	    			}
                    	    			else{
                    	    				Integer value = GlobalInterfaceMap.get((ID13!=null?ID13.getText():null));
                    	    				if(value != null){
                    	    					StatevectorMap.put((ID13!=null?ID13.getText():null), value);
                    	    					
                    	    					LPN outputLPN = GlobalOutputMap.get((ID13!=null?ID13.getText():null));
                    	    					
                    	    					VarSet internals = outputLPN.getInternals();
                    	    					if(internals.contains((ID13!=null?ID13.getText():null))){
                    	    						internals.remove((ID13!=null?ID13.getText():null));
                    	    						outputLPN.getOutputs().add((ID13!=null?ID13.getText():null));
                    	    					}
                    	    					
                    	    					
                    	    					for(LPNTran tran : GlobalTranMap.get((ID13!=null?ID13.getText():null))){
                    	    						tran.setLocalFlag(false);
                    	    						outputLPN.addOutputTran(tran);
                    	    						
                    	    						inputTranList.add(tran);
                    	    					}
                    	    				}
                    	    			}
                        			}
                       			

                    }
                    break;
                case 2 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:813:9: LPAREN expression RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_term1669); 
                    pushFollow(FOLLOW_expression_in_term1671);
                    expression14=expression();

                    state._fsp--;

                    match(input,RPAREN,FOLLOW_RPAREN_in_term1673); 
                    expr = expression14;

                    }
                    break;
                case 3 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:814:9: INT
                    {
                    INT15=(Token)match(input,INT,FOLLOW_INT_in_term1685); 
                    expr = new ConstNode("name", Integer.parseInt((INT15!=null?INT15.getText():null)));

                    }
                    break;
                case 4 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:815:9: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_term1697); 
                    expr = ONE;

                    }
                    break;
                case 5 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:816:9: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_term1709); 
                    expr = ZERO;

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
        return expr;
    }
    // $ANTLR end "term"


    // $ANTLR start "unary"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:819:1: unary returns [ExpressionNode expr] : ( '+' | ( '-' ) )* term ;
    public final ExpressionNode unary() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode term16 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:820:5: ( ( '+' | ( '-' ) )* term )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:820:9: ( '+' | ( '-' ) )* term
            {
            boolean positive = true;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:821:6: ( '+' | ( '-' ) )*
            loop37:
            do {
                int alt37=3;
                int LA37_0 = input.LA(1);

                if ( (LA37_0==PLUS) ) {
                    alt37=1;
                }
                else if ( (LA37_0==MINUS) ) {
                    alt37=2;
                }


                switch (alt37) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:821:7: '+'
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_unary1746); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:821:13: ( '-' )
            	    {
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:821:13: ( '-' )
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:821:14: '-'
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_unary1751); 
            	    if(positive){ positive = false;} else {positive = true;}

            	    }


            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);

            pushFollow(FOLLOW_term_in_unary1758);
            term16=term();

            state._fsp--;


                		if(!positive){
                			expr = new MinNode(term16);
                		}
                		else{
                			expr = term16;
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
        return expr;
    }
    // $ANTLR end "unary"


    // $ANTLR start "bitwiseNegation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:832:1: bitwiseNegation returns [ExpressionNode expr] : ( '~' )* unary ;
    public final ExpressionNode bitwiseNegation() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode unary17 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:833:2: ( ( '~' )* unary )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:833:5: ( '~' )* unary
            {
            boolean neg = false;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:834:3: ( '~' )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==BITWISE_NEGATION) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:834:4: '~'
            	    {
            	    match(input,BITWISE_NEGATION,FOLLOW_BITWISE_NEGATION_in_bitwiseNegation1789); 
            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);

            pushFollow(FOLLOW_unary_in_bitwiseNegation1795);
            unary17=unary();

            state._fsp--;


            				if(neg){
            					expr = new BitNegNode(unary17);
            				}
            				else{
            					expr = unary17;
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
        return expr;
    }
    // $ANTLR end "bitwiseNegation"


    // $ANTLR start "negation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:845:1: negation returns [ExpressionNode expr] : ( '!' )* bitwiseNegation ;
    public final ExpressionNode negation() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode bitwiseNegation18 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:846:2: ( ( '!' )* bitwiseNegation )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:846:4: ( '!' )* bitwiseNegation
            {
            boolean neg = false;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:847:3: ( '!' )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==NEGATION) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:847:4: '!'
            	    {
            	    match(input,NEGATION,FOLLOW_NEGATION_in_negation1821); 
            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);

            pushFollow(FOLLOW_bitwiseNegation_in_negation1827);
            bitwiseNegation18=bitwiseNegation();

            state._fsp--;


            				if(neg){
            					expr = new NegNode(bitwiseNegation18);
            				}
            				else{
            					expr = bitwiseNegation18;
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
        return expr;
    }
    // $ANTLR end "negation"


    // $ANTLR start "mult"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:858:1: mult returns [ExpressionNode expr] : op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )* ;
    public final ExpressionNode mult() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:859:5: (op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:859:9: op1= negation ( '*' op2= negation | '/' op2= negation | '%' op2= negation )*
            {
            pushFollow(FOLLOW_negation_in_mult1855);
            op1=negation();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:860:6: ( '*' op2= negation | '/' op2= negation | '%' op2= negation )*
            loop40:
            do {
                int alt40=4;
                switch ( input.LA(1) ) {
                case TIMES:
                    {
                    alt40=1;
                    }
                    break;
                case DIV:
                    {
                    alt40=2;
                    }
                    break;
                case MOD:
                    {
                    alt40=3;
                    }
                    break;

                }

                switch (alt40) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:860:8: '*' op2= negation
            	    {
            	    match(input,TIMES,FOLLOW_TIMES_in_mult1867); 
            	    pushFollow(FOLLOW_negation_in_mult1871);
            	    op2=negation();

            	    state._fsp--;

            	    expr = new MultNode(expr, op2);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:861:8: '/' op2= negation
            	    {
            	    match(input,DIV,FOLLOW_DIV_in_mult1882); 
            	    pushFollow(FOLLOW_negation_in_mult1886);
            	    op2=negation();

            	    state._fsp--;

            	    expr = new DivNode(expr, op2);

            	    }
            	    break;
            	case 3 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:862:8: '%' op2= negation
            	    {
            	    match(input,MOD,FOLLOW_MOD_in_mult1897); 
            	    pushFollow(FOLLOW_negation_in_mult1901);
            	    op2=negation();

            	    state._fsp--;

            	    expr = new ModNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop40;
                }
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
        return expr;
    }
    // $ANTLR end "mult"


    // $ANTLR start "add"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:866:1: add returns [ExpressionNode expr] : op1= mult ( '+' op2= mult | '-' op2= mult )* ;
    public final ExpressionNode add() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:867:5: (op1= mult ( '+' op2= mult | '-' op2= mult )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:867:9: op1= mult ( '+' op2= mult | '-' op2= mult )*
            {
            pushFollow(FOLLOW_mult_in_add1940);
            op1=mult();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:868:6: ( '+' op2= mult | '-' op2= mult )*
            loop41:
            do {
                int alt41=3;
                int LA41_0 = input.LA(1);

                if ( (LA41_0==PLUS) ) {
                    alt41=1;
                }
                else if ( (LA41_0==MINUS) ) {
                    alt41=2;
                }


                switch (alt41) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:868:8: '+' op2= mult
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_add1951); 
            	    pushFollow(FOLLOW_mult_in_add1955);
            	    op2=mult();

            	    state._fsp--;

            	    expr = new AddNode(expr, op2);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:869:9: '-' op2= mult
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_add1967); 
            	    pushFollow(FOLLOW_mult_in_add1971);
            	    op2=mult();

            	    state._fsp--;

            	    expr = new SubNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop41;
                }
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
        return expr;
    }
    // $ANTLR end "add"


    // $ANTLR start "shift"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:873:1: shift returns [ExpressionNode expr] : op1= add ( '<<' op2= add | '>>' op2= add )* ;
    public final ExpressionNode shift() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:874:5: (op1= add ( '<<' op2= add | '>>' op2= add )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:874:9: op1= add ( '<<' op2= add | '>>' op2= add )*
            {
            pushFollow(FOLLOW_add_in_shift2010);
            op1=add();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:875:6: ( '<<' op2= add | '>>' op2= add )*
            loop42:
            do {
                int alt42=3;
                int LA42_0 = input.LA(1);

                if ( (LA42_0==BITWISE_LSHIFT) ) {
                    alt42=1;
                }
                else if ( (LA42_0==BITWISE_RSHIFT) ) {
                    alt42=2;
                }


                switch (alt42) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:875:8: '<<' op2= add
            	    {
            	    match(input,BITWISE_LSHIFT,FOLLOW_BITWISE_LSHIFT_in_shift2021); 
            	    pushFollow(FOLLOW_add_in_shift2025);
            	    op2=add();

            	    state._fsp--;

            	    expr = new LeftShiftNode(expr, op2);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:876:9: '>>' op2= add
            	    {
            	    match(input,BITWISE_RSHIFT,FOLLOW_BITWISE_RSHIFT_in_shift2037); 
            	    pushFollow(FOLLOW_add_in_shift2041);
            	    op2=add();

            	    state._fsp--;

            	    expr = new RightShiftNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop42;
                }
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
        return expr;
    }
    // $ANTLR end "shift"


    // $ANTLR start "relation"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:880:1: relation returns [ExpressionNode expr] : op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* ;
    public final ExpressionNode relation() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:881:5: (op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:881:9: op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
            {
            pushFollow(FOLLOW_shift_in_relation2076);
            op1=shift();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:882:6: ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
            loop43:
            do {
                int alt43=5;
                switch ( input.LA(1) ) {
                case LESS:
                    {
                    alt43=1;
                    }
                    break;
                case LESS_EQUAL:
                    {
                    alt43=2;
                    }
                    break;
                case GREATER_EQUAL:
                    {
                    alt43=3;
                    }
                    break;
                case GREATER:
                    {
                    alt43=4;
                    }
                    break;

                }

                switch (alt43) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:882:8: '<' op2= shift
            	    {
            	    match(input,LESS,FOLLOW_LESS_in_relation2087); 
            	    pushFollow(FOLLOW_shift_in_relation2091);
            	    op2=shift();

            	    state._fsp--;

            	    expr = new LessNode(expr, op2);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:883:9: '<=' op2= shift
            	    {
            	    match(input,LESS_EQUAL,FOLLOW_LESS_EQUAL_in_relation2103); 
            	    pushFollow(FOLLOW_shift_in_relation2107);
            	    op2=shift();

            	    state._fsp--;

            	    expr = new LessEqualNode(expr, op2);

            	    }
            	    break;
            	case 3 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:884:9: '>=' op2= shift
            	    {
            	    match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_relation2119); 
            	    pushFollow(FOLLOW_shift_in_relation2123);
            	    op2=shift();

            	    state._fsp--;

            	    expr = new GreatEqualNode(expr, op2);

            	    }
            	    break;
            	case 4 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:885:9: '>' op2= shift
            	    {
            	    match(input,GREATER,FOLLOW_GREATER_in_relation2135); 
            	    pushFollow(FOLLOW_shift_in_relation2139);
            	    op2=shift();

            	    state._fsp--;

            	    expr = new GreatNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop43;
                }
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
        return expr;
    }
    // $ANTLR end "relation"


    // $ANTLR start "equivalence"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:889:1: equivalence returns [ExpressionNode expr] : op1= relation ( '==' op2= relation | '!=' op2= relation )* ;
    public final ExpressionNode equivalence() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:890:5: (op1= relation ( '==' op2= relation | '!=' op2= relation )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:890:9: op1= relation ( '==' op2= relation | '!=' op2= relation )*
            {
            pushFollow(FOLLOW_relation_in_equivalence2178);
            op1=relation();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:891:6: ( '==' op2= relation | '!=' op2= relation )*
            loop44:
            do {
                int alt44=3;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==EQUIV) ) {
                    alt44=1;
                }
                else if ( (LA44_0==NOT_EQUIV) ) {
                    alt44=2;
                }


                switch (alt44) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:891:8: '==' op2= relation
            	    {
            	    match(input,EQUIV,FOLLOW_EQUIV_in_equivalence2189); 
            	    pushFollow(FOLLOW_relation_in_equivalence2193);
            	    op2=relation();

            	    state._fsp--;

            	    expr = new EquivNode(expr, op2);

            	    }
            	    break;
            	case 2 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:892:8: '!=' op2= relation
            	    {
            	    match(input,NOT_EQUIV,FOLLOW_NOT_EQUIV_in_equivalence2204); 
            	    pushFollow(FOLLOW_relation_in_equivalence2208);
            	    op2=relation();

            	    state._fsp--;

            	    expr = new NotEquivNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop44;
                }
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
        return expr;
    }
    // $ANTLR end "equivalence"


    // $ANTLR start "bitwiseAnd"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:896:1: bitwiseAnd returns [ExpressionNode expr] : op1= equivalence ( '&' op2= equivalence )* ;
    public final ExpressionNode bitwiseAnd() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:897:5: (op1= equivalence ( '&' op2= equivalence )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:897:9: op1= equivalence ( '&' op2= equivalence )*
            {
            pushFollow(FOLLOW_equivalence_in_bitwiseAnd2247);
            op1=equivalence();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:898:6: ( '&' op2= equivalence )*
            loop45:
            do {
                int alt45=2;
                int LA45_0 = input.LA(1);

                if ( (LA45_0==BITWISE_AND) ) {
                    alt45=1;
                }


                switch (alt45) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:898:8: '&' op2= equivalence
            	    {
            	    match(input,BITWISE_AND,FOLLOW_BITWISE_AND_in_bitwiseAnd2259); 
            	    pushFollow(FOLLOW_equivalence_in_bitwiseAnd2263);
            	    op2=equivalence();

            	    state._fsp--;

            	    expr = new BitAndNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop45;
                }
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
        return expr;
    }
    // $ANTLR end "bitwiseAnd"


    // $ANTLR start "bitwiseXor"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:902:1: bitwiseXor returns [ExpressionNode expr] : op1= bitwiseAnd ( '^' op2= bitwiseAnd )* ;
    public final ExpressionNode bitwiseXor() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:903:5: (op1= bitwiseAnd ( '^' op2= bitwiseAnd )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:903:9: op1= bitwiseAnd ( '^' op2= bitwiseAnd )*
            {
            pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2302);
            op1=bitwiseAnd();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:904:6: ( '^' op2= bitwiseAnd )*
            loop46:
            do {
                int alt46=2;
                int LA46_0 = input.LA(1);

                if ( (LA46_0==BITWISE_XOR) ) {
                    alt46=1;
                }


                switch (alt46) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:904:8: '^' op2= bitwiseAnd
            	    {
            	    match(input,BITWISE_XOR,FOLLOW_BITWISE_XOR_in_bitwiseXor2313); 
            	    pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2317);
            	    op2=bitwiseAnd();

            	    state._fsp--;

            	    expr = new BitXorNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop46;
                }
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
        return expr;
    }
    // $ANTLR end "bitwiseXor"


    // $ANTLR start "bitwiseOr"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:908:1: bitwiseOr returns [ExpressionNode expr] : op1= bitwiseXor ( '|' op2= bitwiseXor )* ;
    public final ExpressionNode bitwiseOr() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:909:5: (op1= bitwiseXor ( '|' op2= bitwiseXor )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:909:9: op1= bitwiseXor ( '|' op2= bitwiseXor )*
            {
            pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2356);
            op1=bitwiseXor();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:910:6: ( '|' op2= bitwiseXor )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==BITWISE_OR) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:910:8: '|' op2= bitwiseXor
            	    {
            	    match(input,BITWISE_OR,FOLLOW_BITWISE_OR_in_bitwiseOr2367); 
            	    pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2371);
            	    op2=bitwiseXor();

            	    state._fsp--;

            	    expr = new BitOrNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop47;
                }
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
        return expr;
    }
    // $ANTLR end "bitwiseOr"


    // $ANTLR start "and"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:914:1: and returns [ExpressionNode expr] : op1= bitwiseOr ( '&&' op2= bitwiseOr )* ;
    public final ExpressionNode and() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:915:5: (op1= bitwiseOr ( '&&' op2= bitwiseOr )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:915:9: op1= bitwiseOr ( '&&' op2= bitwiseOr )*
            {
            pushFollow(FOLLOW_bitwiseOr_in_and2410);
            op1=bitwiseOr();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:916:6: ( '&&' op2= bitwiseOr )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==AND) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:916:8: '&&' op2= bitwiseOr
            	    {
            	    match(input,AND,FOLLOW_AND_in_and2421); 
            	    pushFollow(FOLLOW_bitwiseOr_in_and2425);
            	    op2=bitwiseOr();

            	    state._fsp--;

            	    expr = new AndNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop48;
                }
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
        return expr;
    }
    // $ANTLR end "and"


    // $ANTLR start "or"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:920:1: or returns [ExpressionNode expr] : op1= and ( '||' op2= and )* ;
    public final ExpressionNode or() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:921:5: (op1= and ( '||' op2= and )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:921:9: op1= and ( '||' op2= and )*
            {
            pushFollow(FOLLOW_and_in_or2464);
            op1=and();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:922:6: ( '||' op2= and )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==OR) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:922:8: '||' op2= and
            	    {
            	    match(input,OR,FOLLOW_OR_in_or2475); 
            	    pushFollow(FOLLOW_and_in_or2479);
            	    op2=and();

            	    state._fsp--;

            	    expr = new OrNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop49;
                }
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
        return expr;
    }
    // $ANTLR end "or"


    // $ANTLR start "implication"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:926:1: implication returns [ExpressionNode expr] : op1= or ( '->' op2= or )* ;
    public final ExpressionNode implication() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:927:5: (op1= or ( '->' op2= or )* )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:927:7: op1= or ( '->' op2= or )*
            {
            pushFollow(FOLLOW_or_in_implication2512);
            op1=or();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:928:6: ( '->' op2= or )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==IMPLICATION) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:928:8: '->' op2= or
            	    {
            	    match(input,IMPLICATION,FOLLOW_IMPLICATION_in_implication2523); 
            	    pushFollow(FOLLOW_or_in_implication2527);
            	    op2=or();

            	    state._fsp--;

            	    expr = new ImplicationNode(expr, op2);

            	    }
            	    break;

            	default :
            	    break loop50;
                }
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
        return expr;
    }
    // $ANTLR end "implication"


    // $ANTLR start "expression"
    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:932:1: expression returns [ExpressionNode expr] : op1= implication ( '?' op2= expression ':' op3= expression )? ;
    public final ExpressionNode expression() throws RecognitionException {
        ExpressionNode expr = null;

        ExpressionNode op1 = null;

        ExpressionNode op2 = null;

        ExpressionNode op3 = null;


        try {
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:933:5: (op1= implication ( '?' op2= expression ':' op3= expression )? )
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:933:9: op1= implication ( '?' op2= expression ':' op3= expression )?
            {
            pushFollow(FOLLOW_implication_in_expression2567);
            op1=implication();

            state._fsp--;

            expr = op1;
            // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:934:6: ( '?' op2= expression ':' op3= expression )?
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==QMARK) ) {
                alt51=1;
            }
            switch (alt51) {
                case 1 :
                    // /Users/erodrig9/workspace/platu/src/platu/lpn/io/PlatuGrammar.g:934:7: '?' op2= expression ':' op3= expression
                    {
                    match(input,QMARK,FOLLOW_QMARK_in_expression2577); 
                    pushFollow(FOLLOW_expression_in_expression2581);
                    op2=expression();

                    state._fsp--;

                    match(input,COLON,FOLLOW_COLON_in_expression2583); 
                    pushFollow(FOLLOW_expression_in_expression2587);
                    op3=expression();

                    state._fsp--;


                        			expr = new TernaryNode(expr, op2, op3);
                        		

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
        return expr;
    }
    // $ANTLR end "expression"

    // Delegated rules


    protected DFA1 dfa1 = new DFA1(this);
    static final String DFA1_eotS =
        "\36\uffff";
    static final String DFA1_eofS =
        "\36\uffff";
    static final String DFA1_minS =
        "\1\43\1\21\2\42\1\uffff\2\4\1\41\1\37\1\41\1\37\1\5\1\72\1\4\1\25"+
        "\1\14\1\42\2\14\1\42\1\4\1\43\1\4\1\43\2\21\4\uffff";
    static final String DFA1_maxS =
        "\1\43\1\72\2\42\1\uffff\2\43\1\41\1\37\1\41\1\37\1\5\1\72\1\5\1"+
        "\25\1\14\1\42\2\14\1\42\4\43\1\25\1\72\4\uffff";
    static final String DFA1_acceptS =
        "\4\uffff\1\5\25\uffff\1\1\1\4\1\2\1\3";
    static final String DFA1_specialS =
        "\36\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\1",
            "\1\4\3\uffff\1\3\44\uffff\1\2",
            "\1\5",
            "\1\6",
            "",
            "\1\7\36\uffff\1\10",
            "\1\11\36\uffff\1\12",
            "\1\13",
            "\1\14",
            "\1\15",
            "\1\16",
            "\1\17",
            "\1\20",
            "\1\22\1\21",
            "\1\23",
            "\1\24",
            "\1\25",
            "\1\26",
            "\1\26",
            "\1\27",
            "\1\7\36\uffff\1\10",
            "\1\30",
            "\1\11\36\uffff\1\12",
            "\1\31",
            "\1\33\3\uffff\1\32",
            "\1\35\50\uffff\1\34",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA1_eot = DFA.unpackEncodedString(DFA1_eotS);
    static final short[] DFA1_eof = DFA.unpackEncodedString(DFA1_eofS);
    static final char[] DFA1_min = DFA.unpackEncodedStringToUnsignedChars(DFA1_minS);
    static final char[] DFA1_max = DFA.unpackEncodedStringToUnsignedChars(DFA1_maxS);
    static final short[] DFA1_accept = DFA.unpackEncodedString(DFA1_acceptS);
    static final short[] DFA1_special = DFA.unpackEncodedString(DFA1_specialS);
    static final short[][] DFA1_transition;

    static {
        int numStates = DFA1_transitionS.length;
        DFA1_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA1_transition[i] = DFA.unpackEncodedString(DFA1_transitionS[i]);
        }
    }

    class DFA1 extends DFA {

        public DFA1(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 1;
            this.eot = DFA1_eot;
            this.eof = DFA1_eof;
            this.min = DFA1_min;
            this.max = DFA1_max;
            this.accept = DFA1_accept;
            this.special = DFA1_special;
            this.transition = DFA1_transition;
        }
        public String getDescription() {
            return "86:9: ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )?";
        }
    }
 

    public static final BitSet FOLLOW_globalConstants_in_lpn86 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_globalVariables_in_lpn88 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_globalVariables_in_lpn94 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_globalConstants_in_lpn96 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_globalVariables_in_lpn102 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_globalConstants_in_lpn108 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_module_in_lpn133 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_main_in_lpn160 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_EOF_in_lpn185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_main204 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_MODULE_in_main206 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_NAME_in_main208 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_main210 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_main212 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_57_in_main214 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_main216 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_main218 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_instantiation_in_main227 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LESS_in_main230 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_main232 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_MODULE_in_main234 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_main236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_module262 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_MODULE_in_module264 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_NAME_in_module266 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_module268 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_module270 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_module272 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_module288 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_module290 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_constants_in_module292 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_variables_in_module295 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_instantiation_in_module297 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_logic_in_module300 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LESS_in_module303 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_module305 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_MODULE_in_module307 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_module309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_constants336 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_58_in_constants338 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_constants340 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_ID_in_constants345 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_constants347 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_constants351 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_constants362 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_LESS_in_constants366 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_constants368 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_58_in_constants370 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_constants372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalConstants389 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_58_in_globalConstants391 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants393 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_ID_in_globalConstants398 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_globalConstants400 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_globalConstants404 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalConstants430 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_LESS_in_globalConstants434 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_globalConstants436 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_58_in_globalConstants438 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalVariables455 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_INTERNAL_in_globalVariables457 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables459 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_ID_in_globalVariables464 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_globalVariables474 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_INT_in_globalVariables479 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ID_in_globalVariables493 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalVariables503 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_LESS_in_globalVariables507 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_globalVariables509 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_INTERNAL_in_globalVariables511 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables513 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_variables529 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_INTERNAL_in_variables531 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_variables533 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_ID_in_variables539 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_variables549 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_INT_in_variables554 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ID_in_variables568 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_variables578 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_ID_in_variables593 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_59_in_variables603 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_ID_in_variables608 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_INT_in_variables622 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_60_in_variables633 = new BitSet(new long[]{0x0800000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_variables638 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_variables649 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_variables654 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_COMMA_in_variables656 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_variables670 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_variables680 = new BitSet(new long[]{0x0000000000001040L});
    public static final BitSet FOLLOW_SEMICOLON_in_variables687 = new BitSet(new long[]{0x0000000800000010L});
    public static final BitSet FOLLOW_LESS_in_variables701 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_variables703 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_INTERNAL_in_variables705 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_variables707 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_instantiation736 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_61_in_instantiation738 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_instantiation740 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_instantiation749 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_instantiation753 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_instantiation755 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_instantiation759 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_PERIOD_in_instantiation761 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_instantiation765 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_RPAREN_in_instantiation769 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_LESS_in_instantiation776 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_instantiation778 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_61_in_instantiation780 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_instantiation782 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_marking_in_logic812 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_transition_in_logic815 = new BitSet(new long[]{0x0000000800000002L});
    public static final BitSet FOLLOW_LESS_in_marking868 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_MARKING_in_marking870 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_marking872 = new BitSet(new long[]{0x0000000800000030L});
    public static final BitSet FOLLOW_INT_in_marking878 = new BitSet(new long[]{0x0000000800008000L});
    public static final BitSet FOLLOW_ID_in_marking904 = new BitSet(new long[]{0x0000000800008000L});
    public static final BitSet FOLLOW_COMMA_in_marking928 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_INT_in_marking933 = new BitSet(new long[]{0x0000000800008000L});
    public static final BitSet FOLLOW_ID_in_marking959 = new BitSet(new long[]{0x0000000800008000L});
    public static final BitSet FOLLOW_LESS_in_marking986 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_marking988 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_MARKING_in_marking990 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_marking992 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_transition1026 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_TRANSITION_in_transition1028 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_LABEL_in_transition1030 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1032 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1034 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_transition1038 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1044 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_PRESET_in_transition1046 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1048 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1051 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1053 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1058 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_INT_in_transition1063 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_ID_in_transition1083 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_COMMA_in_transition1099 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_transition1103 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_COMMA_in_transition1121 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_transition1125 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1140 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_POSTSET_in_transition1144 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1146 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1150 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1152 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1157 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_INT_in_transition1163 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_ID_in_transition1183 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_COMMA_in_transition1208 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_transition1212 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_COMMA_in_transition1232 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_ID_in_transition1235 = new BitSet(new long[]{0x0000000000018000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1254 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_transition1259 = new BitSet(new long[]{0x4000110830000370L});
    public static final BitSet FOLLOW_guard_in_transition1262 = new BitSet(new long[]{0x4000000800000050L});
    public static final BitSet FOLLOW_delay_in_transition1282 = new BitSet(new long[]{0x4000000800000010L});
    public static final BitSet FOLLOW_assertion_in_transition1303 = new BitSet(new long[]{0x4000000800000010L});
    public static final BitSet FOLLOW_assignment_in_transition1323 = new BitSet(new long[]{0x4000000800000010L});
    public static final BitSet FOLLOW_LESS_in_transition1342 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_DIV_in_transition1344 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_TRANSITION_in_transition1346 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_GREATER_in_transition1348 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_62_in_assertion1382 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_LPAREN_in_assertion1384 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_expression_in_assertion1386 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_assertion1388 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assertion1390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_guard1416 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_guard1418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_delay1452 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_delay1456 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_COMMA_in_delay1470 = new BitSet(new long[]{0x8000000000000020L});
    public static final BitSet FOLLOW_INT_in_delay1475 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_63_in_delay1490 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_delay1503 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_delay1505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1531 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1533 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_expression_in_assignment1549 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1551 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1573 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_59_in_assignment1590 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_set_in_assignment1592 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_60_in_assignment1600 = new BitSet(new long[]{0x0800000200000000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1604 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_expression_in_assignment1606 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_term1651 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_term1669 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_expression_in_term1671 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_RPAREN_in_term1673 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_term1685 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_term1697 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_term1709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_unary1746 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_MINUS_in_unary1751 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_term_in_unary1758 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BITWISE_NEGATION_in_bitwiseNegation1789 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_unary_in_bitwiseNegation1795 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEGATION_in_negation1821 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_bitwiseNegation_in_negation1827 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_negation_in_mult1855 = new BitSet(new long[]{0x00000001C0000002L});
    public static final BitSet FOLLOW_TIMES_in_mult1867 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_negation_in_mult1871 = new BitSet(new long[]{0x00000001C0000002L});
    public static final BitSet FOLLOW_DIV_in_mult1882 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_negation_in_mult1886 = new BitSet(new long[]{0x00000001C0000002L});
    public static final BitSet FOLLOW_MOD_in_mult1897 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_negation_in_mult1901 = new BitSet(new long[]{0x00000001C0000002L});
    public static final BitSet FOLLOW_mult_in_add1940 = new BitSet(new long[]{0x0000000030000002L});
    public static final BitSet FOLLOW_PLUS_in_add1951 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_mult_in_add1955 = new BitSet(new long[]{0x0000000030000002L});
    public static final BitSet FOLLOW_MINUS_in_add1967 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_mult_in_add1971 = new BitSet(new long[]{0x0000000030000002L});
    public static final BitSet FOLLOW_add_in_shift2010 = new BitSet(new long[]{0x0003000000000002L});
    public static final BitSet FOLLOW_BITWISE_LSHIFT_in_shift2021 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_add_in_shift2025 = new BitSet(new long[]{0x0003000000000002L});
    public static final BitSet FOLLOW_BITWISE_RSHIFT_in_shift2037 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_add_in_shift2041 = new BitSet(new long[]{0x0003000000000002L});
    public static final BitSet FOLLOW_shift_in_relation2076 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_LESS_in_relation2087 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_shift_in_relation2091 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_LESS_EQUAL_in_relation2103 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_shift_in_relation2107 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_relation2119 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_shift_in_relation2123 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_GREATER_in_relation2135 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_shift_in_relation2139 = new BitSet(new long[]{0x0000003C00000002L});
    public static final BitSet FOLLOW_relation_in_equivalence2178 = new BitSet(new long[]{0x000000C000000002L});
    public static final BitSet FOLLOW_EQUIV_in_equivalence2189 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_relation_in_equivalence2193 = new BitSet(new long[]{0x000000C000000002L});
    public static final BitSet FOLLOW_NOT_EQUIV_in_equivalence2204 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_relation_in_equivalence2208 = new BitSet(new long[]{0x000000C000000002L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2247 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_BITWISE_AND_in_bitwiseAnd2259 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2263 = new BitSet(new long[]{0x0000200000000002L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2302 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_BITWISE_XOR_in_bitwiseXor2313 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2317 = new BitSet(new long[]{0x0000800000000002L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2356 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_BITWISE_OR_in_bitwiseOr2367 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2371 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2410 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_AND_in_and2421 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2425 = new BitSet(new long[]{0x0000020000000002L});
    public static final BitSet FOLLOW_and_in_or2464 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_OR_in_or2475 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_and_in_or2479 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_or_in_implication2512 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_IMPLICATION_in_implication2523 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_or_in_implication2527 = new BitSet(new long[]{0x0000080000000002L});
    public static final BitSet FOLLOW_implication_in_expression2567 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_QMARK_in_expression2577 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_expression_in_expression2581 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COLON_in_expression2583 = new BitSet(new long[]{0x0000110030000370L});
    public static final BitSet FOLLOW_expression_in_expression2587 = new BitSet(new long[]{0x0000000000000002L});

}