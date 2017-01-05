// $ANTLR 3.4 /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g 2013-06-26 17:00:34

    package backend.verification.platu.lpn.io;
    
    import java.util.Map.Entry;
    import java.util.HashMap;
    import java.util.LinkedList;
    import java.util.HashSet;
    import java.util.Set;
    import java.util.Arrays;

import org.antlr.runtime.*;

import backend.lpn.parser.ExprTree;
import backend.lpn.parser.LhpnFile;
import backend.lpn.parser.Place;
import backend.lpn.parser.Transition;
import backend.lpn.parser.Variable;
import backend.verification.platu.expression.*;
import backend.verification.platu.lpn.DualHashMap;
import backend.verification.platu.lpn.LPN;
import backend.verification.platu.lpn.LPNTran;
import backend.verification.platu.lpn.LpnTranList;
import backend.verification.platu.lpn.VarExpr;
import backend.verification.platu.lpn.VarExprList;
import backend.verification.platu.lpn.VarSet;
import backend.verification.platu.project.Project;
import backend.verification.platu.stategraph.StateGraph;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class PlatuGrammarParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "AND", "BITWISE_AND", "BITWISE_LSHIFT", "BITWISE_NEGATION", "BITWISE_OR", "BITWISE_RSHIFT", "BITWISE_XOR", "COLON", "COMMA", "COMMENT", "DIGIT", "DIV", "EQUALS", "EQUIV", "FALSE", "GREATER", "GREATER_EQUAL", "ID", "IGNORE", "IMPLICATION", "INPUT", "INT", "INTERNAL", "LABEL", "LESS", "LESS_EQUAL", "LETTER", "LPAREN", "MARKING", "MINUS", "MOD", "MODULE", "MULTILINECOMMENT", "NAME", "NEGATION", "NOT_EQUIV", "OR", "OUTPUT", "PERIOD", "PLUS", "POSTSET", "PRESET", "QMARK", "QUOTE", "RPAREN", "SEMICOLON", "STATE_VECTOR", "TIMES", "TRANSITION", "TRUE", "UNDERSCORE", "WS", "XMLCOMMENT", "'['", "']'", "'assert'", "'const'", "'inf'", "'inst'", "'main'"
    };

    public static final int EOF=-1;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__59=59;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int AND=4;
    public static final int BITWISE_AND=5;
    public static final int BITWISE_LSHIFT=6;
    public static final int BITWISE_NEGATION=7;
    public static final int BITWISE_OR=8;
    public static final int BITWISE_RSHIFT=9;
    public static final int BITWISE_XOR=10;
    public static final int COLON=11;
    public static final int COMMA=12;
    public static final int COMMENT=13;
    public static final int DIGIT=14;
    public static final int DIV=15;
    public static final int EQUALS=16;
    public static final int EQUIV=17;
    public static final int FALSE=18;
    public static final int GREATER=19;
    public static final int GREATER_EQUAL=20;
    public static final int ID=21;
    public static final int IGNORE=22;
    public static final int IMPLICATION=23;
    public static final int INPUT=24;
    public static final int INT=25;
    public static final int INTERNAL=26;
    public static final int LABEL=27;
    public static final int LESS=28;
    public static final int LESS_EQUAL=29;
    public static final int LETTER=30;
    public static final int LPAREN=31;
    public static final int MARKING=32;
    public static final int MINUS=33;
    public static final int MOD=34;
    public static final int MODULE=35;
    public static final int MULTILINECOMMENT=36;
    public static final int NAME=37;
    public static final int NEGATION=38;
    public static final int NOT_EQUIV=39;
    public static final int OR=40;
    public static final int OUTPUT=41;
    public static final int PERIOD=42;
    public static final int PLUS=43;
    public static final int POSTSET=44;
    public static final int PRESET=45;
    public static final int QMARK=46;
    public static final int QUOTE=47;
    public static final int RPAREN=48;
    public static final int SEMICOLON=49;
    public static final int STATE_VECTOR=50;
    public static final int TIMES=51;
    public static final int TRANSITION=52;
    public static final int TRUE=53;
    public static final int UNDERSCORE=54;
    public static final int WS=55;
    public static final int XMLCOMMENT=56;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public PlatuGrammarParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public PlatuGrammarParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return PlatuGrammarParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g"; }


    	// static variables
        static private int INFINITY = Integer.MAX_VALUE;
        static private boolean main = false;
    //    static private ExprTree ZERO = new ExprTree("false"); // constant false node
    //    static private ExprTree ONE = new ExprTree("true");  // constant true node    
        static private HashMap<String, LhpnFile> LpnMap = new HashMap<String, LhpnFile>();  // all modules parsed, keyed by module name
        static private HashMap<String, Integer> GlobalVarHashMap = new HashMap<String, Integer>();  // global variables and associated values
        static private HashMap<String, LhpnFile> GlobalOutputMap = new HashMap<String, LhpnFile>();  // maps potential output variables to associated lpn
        static private HashMap<String, Integer> GlobalInterfaceMap = new HashMap<String, Integer>();  // maps variables to initial values, input have null value until associated output is found
        static private HashMap<String, List<LhpnFile>> GlobalInputMap = new HashMap<String, List<LhpnFile>>(); // maps input variables to associated lpn
        static private HashMap<String, List<Transition>> GlobalTranMap = new HashMap<String, List<Transition>>();  // maps potential output variables to lpn transitions which affect it
        static private HashMap<String, ExprTree> GlobalVarNodeMap = new HashMap<String, ExprTree>(); // maps global variable name to variable object
        //static private ExpressionNode ZERO = new ConstNode("FALSE", 0);  
        //static private ExpressionNode ONE = new ConstNode("TRUE", 1);  // constant true node
        //static private Expression TrueExpr = new Expression(ONE); // constant true expression
    //    static private HashMap<String, LPN> LpnMap = new HashMap<String, LPN>();  // all modules parsed, keyed by module name
    //    static private HashMap<String, Integer> GlobalVarHashMap = new HashMap<String, Integer>();  // global variables and associated values
    //    static private HashMap<String, LPN> GlobalOutputMap = new HashMap<String, LPN>();  // maps potential output variables to associated lpn
    //    static private HashMap<String, Integer> GlobalInterfaceMap = new HashMap<String, Integer>();  // maps variables to initial values, input have null value until associated output is found
    //    static private HashMap<String, List<LPN>> GlobalInputMap = new HashMap<String, List<LPN>>(); // maps input variables to associated lpn
    //    static private HashMap<String, List<LPNTran>> GlobalTranMap = new HashMap<String, List<LPNTran>>();  // maps potential output variables to lpn transitions which affect it
    //    static private HashMap<String, VarNode> GlobalVarNodeMap = new HashMap<String, VarNode>(); // maps global variable name to variable object
        
        static private HashSet<String> initMarkedPlaces = new HashSet<String>();
        //static private HashSet<Integer> initMarkedPlaces = new HashSet<Integer>();
        
        // non-static variables
        private boolean Instance = false;
        private HashMap<String, ExprTree> VarNodeMap = null; // maps variable name to variable object
        private HashMap<String, ArrayNode> ArrayNodeMap = null; // maps array variable name to variable object
    	  private DualHashMap<String, Integer> VarIndexMap = null;  // maps variables to an array index
        private HashMap<String, Integer> GlobalConstHashMap = new HashMap<String, Integer>();  // global constants within a single lpn file
        private HashMap<String, Integer> ConstHashMap = null;  // constants within a single module
        private HashMap<String, Integer> StatevectorMap = null;  // module variables mapped to initial values
        private HashMap<String, Integer> VarCountMap = null; // count of the references to each module variable
        private List<Transition> inputTranList = null;  // list of lpn transitions which affect a modules input
        private List<Transition> outputTranList = null; // list of lpn transitions which affect a modules output
        private VarSet Inputs = null;  // module inputs
        private VarSet Internals = null; // module internal variables
        private VarSet Outputs = null;  // module outputs
        private int VariableIndex = 0;  // count of index assigned to module variables
        private int TransitionIndex = 0;
        private int GlobalCount = 0;  // number of global variables defined in this lpn file
        private int GlobalSize = 0;  // number of global variables defined
        private String curLpn = "";
    //    private boolean Instance = false;
    //    private HashMap<String, VarNode> VarNodeMap = null; // maps variable name to variable object
    //    private HashMap<String, ArrayNode> ArrayNodeMap = null; // maps array variable name to variable object
    //    private DualHashMap<String, Integer> VarIndexMap = null;  // maps variables to an array index
    //    private HashMap<String, Integer> GlobalConstHashMap = new HashMap<String, Integer>();  // global constants within a single lpn file
    //    private HashMap<String, Integer> ConstHashMap = null;  // constants within a single module
    //    private HashMap<String, Integer> StatevectorMap = null;  // module variables mapped to initial values
    //    private HashMap<String, Integer> VarCountMap = null; // count of the references to each module variable
    //    private List<LPNTran> inputTranList = null;  // list of lpn transitions which affect a modules input
    //    private List<LPNTran> outputTranList = null; // list of lpn transitions which affect a modules output
    //    private VarSet Inputs = null;  // module inputs
    //    private VarSet Internals = null; // module internal variables
    //    private VarSet Outputs = null;  // module outputs
        
        public enum VarType {
        	INPUT, OUTPUT, INTERNAL, GLOBAL	
        }



    // $ANTLR start "lpn"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:120:3: lpn returns [Set<LhpnFile> lpnSet] : ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )? ( module | main )+ EOF ;
    public final Set<LhpnFile> lpn() throws RecognitionException {
        Set<LhpnFile> lpnSet = null;


        LhpnFile module1 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:121:5: ( ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )? ( module | main )+ EOF )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:121:9: ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )? ( module | main )+ EOF
            {
            lpnSet = new HashSet<LhpnFile>();

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:9: ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )?
            int alt1=5;
            alt1 = dfa1.predict(input);
            switch (alt1) {
                case 1 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:10: ( globalConstants globalVariables )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:10: ( globalConstants globalVariables )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:11: globalConstants globalVariables
                    {
                    pushFollow(FOLLOW_globalConstants_in_lpn77);
                    globalConstants();

                    state._fsp--;


                    pushFollow(FOLLOW_globalVariables_in_lpn79);
                    globalVariables();

                    state._fsp--;


                    }


                    }
                    break;
                case 2 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:46: ( globalVariables globalConstants )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:46: ( globalVariables globalConstants )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:47: globalVariables globalConstants
                    {
                    pushFollow(FOLLOW_globalVariables_in_lpn85);
                    globalVariables();

                    state._fsp--;


                    pushFollow(FOLLOW_globalConstants_in_lpn87);
                    globalConstants();

                    state._fsp--;


                    }


                    }
                    break;
                case 3 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:82: ( globalVariables )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:82: ( globalVariables )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:83: globalVariables
                    {
                    pushFollow(FOLLOW_globalVariables_in_lpn93);
                    globalVariables();

                    state._fsp--;


                    }


                    }
                    break;
                case 4 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:102: ( globalConstants )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:102: ( globalConstants )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:122:103: globalConstants
                    {
                    pushFollow(FOLLOW_globalConstants_in_lpn99);
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
                    	

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:130:8: ( module | main )+
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
                                    else if ( (LA2_6==63) ) {
                                        alt2=2;
                                    }


                                }


                            }


                        }


                    }


                }


                switch (alt2) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:130:9: module
            	    {
            	    pushFollow(FOLLOW_module_in_lpn124);
            	    module1=module();

            	    state._fsp--;



            	                	lpnSet.add(module1);
            	                

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:134:11: main
            	    {
            	    pushFollow(FOLLOW_main_in_lpn151);
            	    main();

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


            match(input,EOF,FOLLOW_EOF_in_lpn176); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return lpnSet;
    }
    // $ANTLR end "lpn"



    // $ANTLR start "main"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:141:1: main : '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( instantiation )+ '<' '/' 'mod' '>' ;
    public final void main() throws RecognitionException {
        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:142:2: ( '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( instantiation )+ '<' '/' 'mod' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:142:4: '<' 'mod' 'name' '=' '\"' 'main' '\"' '>' ( instantiation )+ '<' '/' 'mod' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_main195); 

            match(input,MODULE,FOLLOW_MODULE_in_main197); 

            match(input,NAME,FOLLOW_NAME_in_main199); 

            match(input,EQUALS,FOLLOW_EQUALS_in_main201); 

            match(input,QUOTE,FOLLOW_QUOTE_in_main203); 

            match(input,63,FOLLOW_63_in_main205); 

            match(input,QUOTE,FOLLOW_QUOTE_in_main207); 

            match(input,GREATER,FOLLOW_GREATER_in_main209); 


            				if(main == true){
            					System.err.println("error");
            					System.exit(1);
            				}
            				
            				main = true;
            			

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:151:3: ( instantiation )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==LESS) ) {
                    int LA3_1 = input.LA(2);

                    if ( (LA3_1==62) ) {
                        alt3=1;
                    }


                }


                switch (alt3) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:151:3: instantiation
            	    {
            	    pushFollow(FOLLOW_instantiation_in_main218);
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


            match(input,LESS,FOLLOW_LESS_in_main221); 

            match(input,DIV,FOLLOW_DIV_in_main223); 

            match(input,MODULE,FOLLOW_MODULE_in_main225); 

            match(input,GREATER,FOLLOW_GREATER_in_main227); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "main"



    // $ANTLR start "module"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:156:1: module returns [LhpnFile lpn] : ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' ) ;
    public final LhpnFile module() throws RecognitionException {
        LhpnFile lpn = null;


        Token ID2=null;
        PlatuGrammarParser.logic_return logic3 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:157:5: ( ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' ) )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:157:7: ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' )
            {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:157:7: ( '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:157:9: '<' 'mod' 'name' '=' '\"' ID '\"' '>' ( constants )? variables ( instantiation )? ( logic )? '<' '/' 'mod' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_module253); 

            match(input,MODULE,FOLLOW_MODULE_in_module255); 

            match(input,NAME,FOLLOW_NAME_in_module257); 

            match(input,EQUALS,FOLLOW_EQUALS_in_module259); 

            match(input,QUOTE,FOLLOW_QUOTE_in_module261); 

            ID2=(Token)match(input,ID,FOLLOW_ID_in_module263); 


                			// module names must be unique
                			if(LpnMap.containsKey((ID2!=null?ID2.getText():null))){
                				System.err.println("error on line " + ID2.getLine() + ": module " + (ID2!=null?ID2.getText():null) + " already exists");
                				System.exit(1);
                			}

                      // initialize non static variables for new module
                      VarIndexMap = new DualHashMap<String, Integer>();
                      ConstHashMap = new HashMap<String, Integer>();
                      VarNodeMap = new HashMap<String, ExprTree>();
                      // TODO: Array Nodes
                      ArrayNodeMap = new HashMap<String, ArrayNode>();
                      VarCountMap = new HashMap<String, Integer>();
                      Inputs = new VarSet();
                      Internals = new VarSet();
                      Outputs = new VarSet();
                      inputTranList = new ArrayList<Transition>();//inputTranList = new ArrayList<LPNTran>();
                      outputTranList = new ArrayList<Transition>();//outputTranList = new ArrayList<LPNTran>();
                      StatevectorMap = new HashMap<String, Integer>();
                      VariableIndex = 0;
                      System.out.println("-------------------------");
                      System.out.println("mod = " + (ID2!=null?ID2.getText():null));
                		

            match(input,QUOTE,FOLLOW_QUOTE_in_module279); 

            match(input,GREATER,FOLLOW_GREATER_in_module281); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:182:14: ( constants )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==LESS) ) {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==60) ) {
                    alt4=1;
                }
            }
            switch (alt4) {
                case 1 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:182:14: constants
                    {
                    pushFollow(FOLLOW_constants_in_module283);
                    constants();

                    state._fsp--;


                    }
                    break;

            }


            pushFollow(FOLLOW_variables_in_module286);
            variables();

            state._fsp--;


            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:182:35: ( instantiation )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==LESS) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==62) ) {
                    alt5=1;
                }
            }
            switch (alt5) {
                case 1 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:182:35: instantiation
                    {
                    pushFollow(FOLLOW_instantiation_in_module288);
                    instantiation();

                    state._fsp--;


                    }
                    break;

            }


            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:182:50: ( logic )?
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
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:182:50: logic
                    {
                    pushFollow(FOLLOW_logic_in_module291);
                    logic3=logic();

                    state._fsp--;


                    }
                    break;

            }


            match(input,LESS,FOLLOW_LESS_in_module294); 

            match(input,DIV,FOLLOW_DIV_in_module296); 

            match(input,MODULE,FOLLOW_MODULE_in_module298); 

            match(input,GREATER,FOLLOW_GREATER_in_module300); 


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
            	            
            //	            int i = 0;
            //	            int[] initialMarking = new int[(logic3!=null?logic3.initMarking:null).size()];
            //	            for(Integer mark : (logic3!=null?logic3.initMarking:null)){
            //	            	initialMarking[i++] = mark;
            //	            }

            //				lpn = new LPN(prj, (ID2!=null?ID2.getText():null), Inputs, Outputs, Internals, VarNodeMap, (logic3!=null?logic3.lpnTranSet:null), 
            //	         			StatevectorMap, initialMarking);
                    lpn = new LhpnFile();
                    lpn.setLabel((ID2!=null?ID2.getText():null));
                    System.out.println("---- LPN : " + lpn.getLabel() + " ----");
                    for (Transition t: (logic3!=null?logic3.lpnTranSet:null)) {
                      lpn.addTransition(t);
                      t.setLpn(lpn);
                      System.out.println("transition(logic): " + t.getLabel());
                      for (Place p : t.getPreset()) {
                        if ((logic3!=null?logic3.initMarking:null).contains(p.getName())) 
                          lpn.addPlace(p.getName(), true);
                        else
                          lpn.addPlace(p.getName(), false);
                        lpn.getPlace(p.getName()).addPostset(t);            
                      }
                      for (Place p : t.getPostset()) {
                        if ((logic3!=null?logic3.initMarking:null).contains(p.getName())) 
                          lpn.addPlace(p.getName(), true);
                        else
                          lpn.addPlace(p.getName(), false);
                        lpn.getPlace(p.getName()).addPreset(t);
                      }   
                    }
                    
            //				for(Transition tran : inputTranList){
            //					tran.addDstLpn(lpn);
            //				}
            //				lpn.addAllInputTrans(inputTranList);
            //				lpn.addAllOutputTrans(outputTranList);
            //	      lpn.setVarIndexMap(VarIndexMap);         
            //	      (logic3!=null?logic3.lpnTranSet:null).setLPN(lpn);     
            //	      prj.getDesignUnitSet().add(lpn.getStateGraph());
                      
            //          for (Transition t : inputTranList) {
            //            System.out.println("transition(in): " + t.getLabel()); 
            //            lpn.addTransition(t);
            //          }
                      for (Transition t : outputTranList) {
                        System.out.println("transition(out): " + t.getLabel());
                        lpn.addTransition(t);
                      }
                      
            	        LpnMap.put(lpn.getLabel(), lpn);
            //	        for (String var : StatevectorMap.keySet()) {
            //	          lpn.addInteger(var, StatevectorMap.get(var)+"");
            //	        }
            	        
            	        // TODO: Where to use these hashmaps?
                      // map outputs to lpn object
                      for(String output : Outputs){
                        GlobalOutputMap.put(output, lpn);
                        lpn.addOutput(output, "integer", StatevectorMap.get(output)+"");
                        System.out.println("@1: Added output variable " + output + " to LPN " + lpn.getLabel());
                      }
                      // map potential output to lpn object
                      for(String internal : Internals){
                        GlobalOutputMap.put(internal, lpn);
                        lpn.addInternal(internal, "integer", StatevectorMap.get(internal)+"");
                        System.out.println("@1: Added internal variable " + internal + " to LPN " + lpn.getLabel() + "with initial value " + StatevectorMap.get(internal));
            	        }
                      // map input variable to lpn object
                      for(String input : Inputs) {
                        if(GlobalInputMap.containsKey(input)){
                          GlobalInputMap.get(input).add(lpn);
                          lpn.addInput(input, "integer", StatevectorMap.get(input)+"");
                          System.out.println("@1: Added input variable " + input + " to LPN " + lpn.getLabel() + " with initial value " + StatevectorMap.get(input) + ".");
            	          }
                      	else{
                      	  List<LhpnFile> lpnList = new ArrayList<LhpnFile>();//List<LPN> lpnList = new ArrayList<LPN>();
                       	  lpnList.add(lpn);
                       	  GlobalInputMap.put(input, lpnList);
                       	  lpn.addInput(input, "integer", StatevectorMap.get(input)+"");
                       	  System.out.println("@2: Added input variable " + input + " to LPN " + lpn.getLabel() + " with initial value " + StatevectorMap.get(input) + ".");
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
        	// do for sure before leaving
        }
        return lpn;
    }
    // $ANTLR end "module"



    // $ANTLR start "constants"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:289:1: constants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' ;
    public final void constants() throws RecognitionException {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:290:2: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:290:4: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_constants327); 

            match(input,60,FOLLOW_60_in_constants329); 

            match(input,GREATER,FOLLOW_GREATER_in_constants331); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:290:20: (const1= ID '=' val1= INT ';' )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==ID) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:290:21: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_constants336); 

            	    match(input,EQUALS,FOLLOW_EQUALS_in_constants338); 

            	    val1=(Token)match(input,INT,FOLLOW_INT_in_constants342); 


            	    				// make sure constant is not defined as something else
            	    //				String const1_tmp = (const1!=null?const1.getText():null);
            	    //				if ((const1!=null?const1.getText():null).contains(".")) {
            	    //				  const1_tmp.replaceAll(".", "_");
            	    //				}
            	    //				else {
            	    //				  const1_tmp = (const1!=null?const1.getText():null);
            	    //				}

            	            String const1_tmp = (const1!=null?const1.getText():null);
            	            if ((const1!=null?const1.getText():null).contains(".")) {
            	              const1_tmp = const1_tmp.replace(".", "_");
            	            }
            	            else {
            	              const1_tmp = (const1!=null?const1.getText():null);
            	            }
            	    				if(StatevectorMap.containsKey(const1_tmp)){
            	    					System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " already exists as a variable"); 
            	    					System.exit(1);
            	    				}
            	    				else if(GlobalConstHashMap.containsKey(const1_tmp)){
            	    				    System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " already exists as a global constant");
            	    				    System.exit(1);
            	    				}
            	    				else if(GlobalVarHashMap.containsKey(const1_tmp)){
            	                		System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " is already defined as a global variable");
            	                		System.exit(1);
            	                	}
            	    				// put will override previous value
            	    				//Integer result = ConstHashMap.put((const1!=null?const1.getText():null), Integer.parseInt((val1!=null?val1.getText():null)));
            	    				Integer result = ConstHashMap.put(const1_tmp, Integer.parseInt((val1!=null?val1.getText():null)));			
            	    				if(result != null){
            	    					System.err.println("warning on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " will be overwritten");
            	    				}
            	    			

            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_constants353); 

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            match(input,LESS,FOLLOW_LESS_in_constants357); 

            match(input,DIV,FOLLOW_DIV_in_constants359); 

            match(input,60,FOLLOW_60_in_constants361); 

            match(input,GREATER,FOLLOW_GREATER_in_constants363); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "constants"



    // $ANTLR start "globalConstants"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:330:1: globalConstants : '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' ;
    public final void globalConstants() throws RecognitionException {
        Token const1=null;
        Token val1=null;

        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:331:5: ( '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:331:9: '<' 'const' '>' (const1= ID '=' val1= INT ';' )* '<' '/' 'const' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalConstants379); 

            match(input,60,FOLLOW_60_in_globalConstants381); 

            match(input,GREATER,FOLLOW_GREATER_in_globalConstants383); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:331:25: (const1= ID '=' val1= INT ';' )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==ID) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:331:26: const1= ID '=' val1= INT ';'
            	    {
            	    const1=(Token)match(input,ID,FOLLOW_ID_in_globalConstants388); 

            	    match(input,EQUALS,FOLLOW_EQUALS_in_globalConstants390); 

            	    val1=(Token)match(input,INT,FOLLOW_INT_in_globalConstants394); 


            	                  String const1_tmp = (const1!=null?const1.getText():null);
            	                  if ((const1!=null?const1.getText():null).contains(".")) {
            	                    const1_tmp = const1_tmp.replace(".", "_");
            	                  }
            	                  else {
            	                    const1_tmp = (const1!=null?const1.getText():null);
            	                  }
            	                	// make sure constant has not been defined already
            	                	if(GlobalVarHashMap.containsKey(const1_tmp)){
            	                		System.err.println("error on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " is already defined as a global variable");
            	                		System.exit(1);
            	                	}
            	                	
            	                	// put will override previous value
            	                    Integer result = GlobalConstHashMap.put(const1_tmp, Integer.parseInt((val1!=null?val1.getText():null)));
            	                    if(result != null){
            	                        System.err.println("warning on line " + const1.getLine() + ": " + (const1!=null?const1.getText():null) + " will be overwritten");
            	                    }
            	                

            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalConstants420); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            match(input,LESS,FOLLOW_LESS_in_globalConstants424); 

            match(input,DIV,FOLLOW_DIV_in_globalConstants426); 

            match(input,60,FOLLOW_60_in_globalConstants428); 

            match(input,GREATER,FOLLOW_GREATER_in_globalConstants430); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "globalConstants"



    // $ANTLR start "globalVariables"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:355:1: globalVariables : '<' 'var' '>' (var= ID '=' (val= INT |var2= ID ) ';' )* '<' '/' 'var' '>' ;
    public final void globalVariables() throws RecognitionException {
        Token var=null;
        Token val=null;
        Token var2=null;

        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:356:2: ( '<' 'var' '>' (var= ID '=' (val= INT |var2= ID ) ';' )* '<' '/' 'var' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:356:4: '<' 'var' '>' (var= ID '=' (val= INT |var2= ID ) ';' )* '<' '/' 'var' '>'
            {
            match(input,LESS,FOLLOW_LESS_in_globalVariables444); 

            match(input,INTERNAL,FOLLOW_INTERNAL_in_globalVariables446); 

            match(input,GREATER,FOLLOW_GREATER_in_globalVariables448); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:356:18: (var= ID '=' (val= INT |var2= ID ) ';' )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==ID) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:356:19: var= ID '=' (val= INT |var2= ID ) ';'
            	    {
            	    var=(Token)match(input,ID,FOLLOW_ID_in_globalVariables453); 


            	    				// make sure global variables are consistently defined in each lpn file
            	    				String var_tmp = (var!=null?var.getText():null);
            	            if ((var!=null?var.getText():null).contains(".")) {
            	              var_tmp = var_tmp.replace(".", "_");
            	            }
            	            else {
            	              var_tmp = (var!=null?var.getText():null);
            	            }
            	    				if(GlobalSize == 0){
            	    					if(GlobalConstHashMap.containsKey(var_tmp)){
            	    						System.err.println("error on line" + var.getLine() + ": " + (var!=null?var.getText():null) + "already exists as a constant"); 
            	    	                    System.exit(1);
            	    					}
            	    					else if(GlobalVarHashMap.containsKey(var_tmp)){
            	    						System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " has already been defined");
            	    						System.exit(1);
            	    					}
            	    				}
            	    				else{
            	    					if(!GlobalVarHashMap.containsKey(var_tmp)){
            	    						System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is inconsistently defined");
            	    						System.exit(1);
            	    					}
            	    				}
            	    				
            	    				GlobalCount++;
            	    			

            	    match(input,EQUALS,FOLLOW_EQUALS_in_globalVariables463); 

            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:385:7: (val= INT |var2= ID )
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
            	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:385:8: val= INT
            	            {
            	            val=(Token)match(input,INT,FOLLOW_INT_in_globalVariables468); 


            	            				// make sure global variables are consistently initialized
            	            				int value = Integer.parseInt((val!=null?val.getText():null));
            	            				if(GlobalSize == 0){
            	            					GlobalVarHashMap.put(var_tmp, value);
            	            				}
            	            				else{
            	            					int globalVal = GlobalVarHashMap.get(var_tmp);
            	            					if(globalVal != value){
            	            						System.err.println("error on line " + val.getLine() + ": " + (var!=null?var.getText():null) + " is inconsistently assigned");
            	            						System.exit(1);
            	            					}
            	            				}
            	            			

            	            }
            	            break;
            	        case 2 :
            	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:400:5: var2= ID
            	            {
            	            var2=(Token)match(input,ID,FOLLOW_ID_in_globalVariables482); 


            	            			  String var2_tmp = (var2!=null?var2.getText():null);
            	                    if ((var2!=null?var2.getText():null).contains(".")) {
            	                      var2_tmp = var2_tmp.replace(".", "_");
            	                    }
            	                    else {
            	                      var2_tmp = (var2!=null?var2.getText():null);
            	                    }
            	            				// get value of variable
            	            				Integer value = null;
            	            				if(GlobalConstHashMap.containsKey(var2_tmp)){
            	            					value = GlobalConstHashMap.get(var2_tmp);
            	            				}
            	            				else if(GlobalVarHashMap.containsKey(var2_tmp)){
            	            					System.err.println("error on line " + var2.getLine() + ": global variable " + (var2!=null?var2.getText():null) + " cannot be assigned to global variable " + (var!=null?var.getText():null)); 
            	                				System.exit(1);
            	            				}
            	            				else{
            	            					System.err.println("error on line " + var2.getLine() + ": " + (var2!=null?var2.getText():null) + " is not defined"); 
            	                				System.exit(1);
            	            				}
            	            				// make sure global variable is consistently initialized
            	            				if(GlobalSize == 0){
            	            					GlobalVarHashMap.put(var2_tmp, value);
            	            				}
            	            				else{
            	            					int globalVal = GlobalVarHashMap.get(var2_tmp);
            	            					if(globalVal != value){
            	            						System.err.println("error on line " + val.getLine() + ": " + (var!=null?var.getText():null) + " is inconsistently assigned");
            	            						System.exit(1);
            	            					}
            	            				}			
            	            			

            	            }
            	            break;

            	    }


            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_globalVariables492); 

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            match(input,LESS,FOLLOW_LESS_in_globalVariables496); 

            match(input,DIV,FOLLOW_DIV_in_globalVariables498); 

            match(input,INTERNAL,FOLLOW_INTERNAL_in_globalVariables500); 

            match(input,GREATER,FOLLOW_GREATER_in_globalVariables502); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "globalVariables"



    // $ANTLR start "variables"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:438:1: variables : '<' 'var' '>' ( (var= ID '=' (val= INT |var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID |val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )* '<' '/' 'var' '>' ;
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
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:439:2: ( '<' 'var' '>' ( (var= ID '=' (val= INT |var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID |val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )* '<' '/' 'var' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:439:4: '<' 'var' '>' ( (var= ID '=' (val= INT |var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID |val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )* '<' '/' 'var' '>'
            {
            Integer value = null; Token varNode = null; String var_tmp;

            match(input,LESS,FOLLOW_LESS_in_variables518); 

            match(input,INTERNAL,FOLLOW_INTERNAL_in_variables520); 

            match(input,GREATER,FOLLOW_GREATER_in_variables522); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:440:17: ( (var= ID '=' (val= INT |var2= ID ) ';' ) | (arrayName= ID ( '[' (dim= ID |val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' ) )*
            loop16:
            do {
                int alt16=3;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==ID) ) {
                    int LA16_2 = input.LA(2);

                    if ( (LA16_2==EQUALS) ) {
                        alt16=1;
                    }
                    else if ( (LA16_2==57) ) {
                        alt16=2;
                    }


                }


                switch (alt16) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:440:18: (var= ID '=' (val= INT |var2= ID ) ';' )
            	    {
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:440:18: (var= ID '=' (val= INT |var2= ID ) ';' )
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:440:19: var= ID '=' (val= INT |var2= ID ) ';'
            	    {
            	    var=(Token)match(input,ID,FOLLOW_ID_in_variables528); 


            	    				// check variable is unique in scope
            	    				var_tmp = (var!=null?var.getText():null);
            	            if ((var!=null?var.getText():null).contains(".")) {
            	              var_tmp = var_tmp.replace(".", "_");
            	            }
            	            else {
            	              var_tmp = (var!=null?var.getText():null);
            	            }		
            	    				if(GlobalConstHashMap.containsKey(var_tmp)){
            	    					System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is a global constant"); 
            	        				System.exit(1);
            	    				}
            	    				else if(GlobalVarHashMap.containsKey(var_tmp)){
            	    					System.err.println("error on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " is a global variable"); 
            	        				System.exit(1);
            	    				}
            	    				else if(StatevectorMap.containsKey(var_tmp)){
            	    					System.err.println("warning on line " + var.getLine() + ": " + (var!=null?var.getText():null) + " will be overwritten");
            	    				}		
            	    				varNode = var;
            	    			

            	    match(input,EQUALS,FOLLOW_EQUALS_in_variables538); 

            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:463:7: (val= INT |var2= ID )
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
            	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:463:8: val= INT
            	            {
            	            val=(Token)match(input,INT,FOLLOW_INT_in_variables543); 


            	            				// get variable initial value
            	            				value = Integer.parseInt((val!=null?val.getText():null));
            	            			

            	            }
            	            break;
            	        case 2 :
            	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:468:5: var2= ID
            	            {
            	            var2=(Token)match(input,ID,FOLLOW_ID_in_variables557); 


            	            			  String var2_tmp = (var2!=null?var2.getText():null);
            	                    if ((var2!=null?var2.getText():null).contains(".")) {
            	                      var2_tmp = var2_tmp.replace(".", "_");
            	                    }
            	                    else {
            	                      var2_tmp = (var2!=null?var2.getText():null);
            	                    }
            	            				// get variable initial value
            	            				if(GlobalConstHashMap.containsKey(var2_tmp)){
            	            					value = GlobalConstHashMap.get(var2_tmp);
            	            				}
            	            				else if(GlobalVarHashMap.containsKey(var2_tmp)){
            	            					value = GlobalVarHashMap.get(var2_tmp);
            	            				}
            	            				else if(ConstHashMap.containsKey(var2_tmp)){
            	            					value = ConstHashMap.get(var2_tmp);
            	            				}
            	            				else if(StatevectorMap.containsKey(var2_tmp)){ // Should var be allowed to assign a var?
            	            					value = StatevectorMap.get(var2_tmp);
            	            				}
            	            				else{
            	            					System.err.println("error on line " + var2.getLine() + ": " + (var2!=null?var2.getText():null) + " is not defined"); 
            	                				System.exit(1);
            	            				}
            	            				
            	            				varNode = var2;
            	            				var_tmp = var2_tmp;
            	            			

            	            }
            	            break;

            	    }


            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_variables567); 

            	    }



            	    				// add variable and value to state vector
            	    				//StatevectorMap.put(varNode.getText(), value);
            	    				StatevectorMap.put(var_tmp, value);
            	    				
            	    				// generate variable index and create new var node  
            	    				int index = VariableIndex++;
            	       			//VarIndexMap.insert(varNode.getText(), index);
            	       			VarIndexMap.insert(var_tmp, index);
            	       			ExprTree newVarNode = new ExprTree(var_tmp); 
            	            newVarNode.setIntegerSignals(StatevectorMap.keySet());      
            	            newVarNode.getExprTree();
            	            VarNodeMap.put(var_tmp, newVarNode);
            	       				
            	       				//VarNodeMap.put(varNode.getText(), new ExprTree(varNode.getText()));
            	       				//VarNodeMap.put(varNode.getText(), new VarNode(varNode.getText(), index));
            	        			
            	        			// if associated input variable has been defined, label as output, else label as internal
            	    				if(!GlobalInterfaceMap.containsKey(var_tmp)){
            	    					Internals.add(var_tmp);					
            	    				}
            	    				else{
            	    					if(GlobalInterfaceMap.get(varNode.getText()) != null){
            	    						System.err.println("error on line " + varNode.getLine() + ": variable '" + varNode.getText() + "' has already been declared in another module");
            	    						System.exit(1);
            	    					}
            	    					Outputs.add(var_tmp);
            	    					// TODO: Is it needed for our LPN?
            	    					// initialize associated input variables with output value
            	    					List<LhpnFile> lpnList = GlobalInputMap.get(var_tmp);
            	    					if(lpnList != null){
            	    						for(LhpnFile lpn : lpnList){
            	    							//lpn.getInitVector().put(varNode.getText(), value);
            	    							//(delete) lpn.addInput(var_tmp, "integer", value+"");
            	    						}
            	    					}
            	    				}			
            	    				GlobalInterfaceMap.put(var_tmp, value);
            	    				VarCountMap.put(var_tmp, 0);
            	    			

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:540:5: (arrayName= ID ( '[' (dim= ID |val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' )
            	    {
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:540:5: (arrayName= ID ( '[' (dim= ID |val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';' )
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:540:6: arrayName= ID ( '[' (dim= ID |val2= INT ) ']' )+ '=' ( '(' (val3= INT ',' )* val4= INT ')' )+ ';'
            	    {
            	    arrayName=(Token)match(input,ID,FOLLOW_ID_in_variables586); 


            	    				List<Integer> dimensionsList = new ArrayList<Integer>();
            	    			

            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:544:3: ( '[' (dim= ID |val2= INT ) ']' )+
            	    int cnt13=0;
            	    loop13:
            	    do {
            	        int alt13=2;
            	        int LA13_0 = input.LA(1);

            	        if ( (LA13_0==57) ) {
            	            alt13=1;
            	        }


            	        switch (alt13) {
            	    	case 1 :
            	    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:544:4: '[' (dim= ID |val2= INT ) ']'
            	    	    {
            	    	    match(input,57,FOLLOW_57_in_variables596); 

            	    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:544:8: (dim= ID |val2= INT )
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
            	    	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:544:9: dim= ID
            	    	            {
            	    	            dim=(Token)match(input,ID,FOLLOW_ID_in_variables601); 


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
            	    	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:566:5: val2= INT
            	    	            {
            	    	            val2=(Token)match(input,INT,FOLLOW_INT_in_variables615); 


            	    	            				dimensionsList.add(Integer.parseInt((val2!=null?val2.getText():null)));
            	    	            			

            	    	            }
            	    	            break;

            	    	    }


            	    	    match(input,58,FOLLOW_58_in_variables626); 

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


            	    match(input,EQUALS,FOLLOW_EQUALS_in_variables631); 


            	    				List<Integer> valueList = new ArrayList<Integer>();
            	    			

            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:574:3: ( '(' (val3= INT ',' )* val4= INT ')' )+
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
            	    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:574:4: '(' (val3= INT ',' )* val4= INT ')'
            	    	    {
            	    	    match(input,LPAREN,FOLLOW_LPAREN_in_variables642); 

            	    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:574:8: (val3= INT ',' )*
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
            	    	    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:574:9: val3= INT ','
            	    	    	    {
            	    	    	    val3=(Token)match(input,INT,FOLLOW_INT_in_variables647); 

            	    	    	    match(input,COMMA,FOLLOW_COMMA_in_variables649); 


            	    	    	    				valueList.add(Integer.parseInt((val3!=null?val3.getText():null)));
            	    	    	    			

            	    	    	    }
            	    	    	    break;

            	    	    	default :
            	    	    	    break loop14;
            	    	        }
            	    	    } while (true);


            	    	    val4=(Token)match(input,INT,FOLLOW_INT_in_variables663); 


            	    	    				valueList.add(Integer.parseInt((val4!=null?val4.getText():null)));
            	    	    			

            	    	    match(input,RPAREN,FOLLOW_RPAREN_in_variables673); 

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


            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_variables680); 


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
            	    						//VarNode v = new VarNode(name, index);
            	    						ExprTree v = new ExprTree(name);
            	    						array.add(v);
            	    						
            	    						// add variable and value to state vector
            	    						StatevectorMap.put(name, 0);
            	    						
            	    						// generate variable index and create new var node  
            	    		   			VarIndexMap.insert(name, index);
            	    					}
            	    					// TODO: ArrayNodeMap? 
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


            match(input,LESS,FOLLOW_LESS_in_variables694); 

            match(input,DIV,FOLLOW_DIV_in_variables696); 

            match(input,INTERNAL,FOLLOW_INTERNAL_in_variables698); 

            match(input,GREATER,FOLLOW_GREATER_in_variables700); 


            				// add global variables to initial state vector and label as an input & output
            				System.out.println("GlobalVarHashMap size = " + GlobalVarHashMap.size());
            				for(Entry<String, Integer> e : GlobalVarHashMap.entrySet()){
            					String globalVar = e.getKey();
            					StatevectorMap.put(globalVar, e.getValue());			
            					Integer index =  VariableIndex++;
            	    		VarIndexMap.insert(globalVar, index);
            	    		//VarNodeMap.put(globalVar, new VarNode(globalVar, index));
            	    		VarNodeMap.put(globalVar, new ExprTree(globalVar));	    		
            	    		Inputs.add(globalVar);
            	    		System.out.println("Added globalVar (" + globalVar + ") to Inputs.");
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
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "variables"



    // $ANTLR start "instantiation"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:632:1: instantiation : '<' 'inst' '>' modName= ID instName= ID '(' (mod= ID '.' var= ID )+ ')' '<' '/' 'inst' '>' ;
    public final void instantiation() throws RecognitionException {
        Token modName=null;
        Token instName=null;
        Token mod=null;
        Token var=null;

        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:633:5: ( '<' 'inst' '>' modName= ID instName= ID '(' (mod= ID '.' var= ID )+ ')' '<' '/' 'inst' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:633:9: '<' 'inst' '>' modName= ID instName= ID '(' (mod= ID '.' var= ID )+ ')' '<' '/' 'inst' '>'
            {
            HashMap<String, String> portMap = new HashMap<String, String>();

            match(input,LESS,FOLLOW_LESS_in_instantiation729); 

            match(input,62,FOLLOW_62_in_instantiation731); 

            match(input,GREATER,FOLLOW_GREATER_in_instantiation733); 

            modName=(Token)match(input,ID,FOLLOW_ID_in_instantiation742); 

            instName=(Token)match(input,ID,FOLLOW_ID_in_instantiation746); 

            match(input,LPAREN,FOLLOW_LPAREN_in_instantiation748); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:635:32: (mod= ID '.' var= ID )+
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
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:635:33: mod= ID '.' var= ID
            	    {
            	    mod=(Token)match(input,ID,FOLLOW_ID_in_instantiation752); 

            	    match(input,PERIOD,FOLLOW_PERIOD_in_instantiation754); 

            	    var=(Token)match(input,ID,FOLLOW_ID_in_instantiation758); 

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


            match(input,RPAREN,FOLLOW_RPAREN_in_instantiation762); 

            match(input,LESS,FOLLOW_LESS_in_instantiation769); 

            match(input,DIV,FOLLOW_DIV_in_instantiation771); 

            match(input,62,FOLLOW_62_in_instantiation773); 

            match(input,GREATER,FOLLOW_GREATER_in_instantiation775); 

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return ;
    }
    // $ANTLR end "instantiation"


    public static class logic_return extends ParserRuleReturnScope {
        public List<String> initMarking;
        public LpnTranList lpnTranSet;
    };


    // $ANTLR start "logic"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:639:1: logic returns [List<String> initMarking, LpnTranList lpnTranSet] : marking ( transition )+ ;
    public final PlatuGrammarParser.logic_return logic() throws RecognitionException {
        PlatuGrammarParser.logic_return retval = new PlatuGrammarParser.logic_return();
        retval.start = input.LT(1);


        Transition transition4 =null;

        List<String> marking5 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:640:5: ( marking ( transition )+ )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:640:9: marking ( transition )+
            {
            retval.lpnTranSet = new LpnTranList();

            pushFollow(FOLLOW_marking_in_logic805);
            marking5=marking();

            state._fsp--;


            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:641:14: ( transition )+
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
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:641:15: transition
            	    {
            	    pushFollow(FOLLOW_transition_in_logic808);
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
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "logic"



    // $ANTLR start "marking"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:647:1: marking returns [List<String> mark] : ( '<' 'marking' '>' ( (m1= INT |c1= ID ) ( ',' (m2= INT |c2= ID ) )* )? '<' '/' 'marking' '>' )? ;
    public final List<String> marking() throws RecognitionException {
        List<String> mark = null;


        Token m1=null;
        Token c1=null;
        Token m2=null;
        Token c2=null;

        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:648:5: ( ( '<' 'marking' '>' ( (m1= INT |c1= ID ) ( ',' (m2= INT |c2= ID ) )* )? '<' '/' 'marking' '>' )? )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:648:9: ( '<' 'marking' '>' ( (m1= INT |c1= ID ) ( ',' (m2= INT |c2= ID ) )* )? '<' '/' 'marking' '>' )?
            {
            mark = new LinkedList<String>(); Integer result;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:649:9: ( '<' 'marking' '>' ( (m1= INT |c1= ID ) ( ',' (m2= INT |c2= ID ) )* )? '<' '/' 'marking' '>' )?
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
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:649:10: '<' 'marking' '>' ( (m1= INT |c1= ID ) ( ',' (m2= INT |c2= ID ) )* )? '<' '/' 'marking' '>'
                    {
                    match(input,LESS,FOLLOW_LESS_in_marking862); 

                    match(input,MARKING,FOLLOW_MARKING_in_marking864); 

                    match(input,GREATER,FOLLOW_GREATER_in_marking866); 

                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:649:28: ( (m1= INT |c1= ID ) ( ',' (m2= INT |c2= ID ) )* )?
                    int alt22=2;
                    int LA22_0 = input.LA(1);

                    if ( (LA22_0==ID||LA22_0==INT) ) {
                        alt22=1;
                    }
                    switch (alt22) {
                        case 1 :
                            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:649:29: (m1= INT |c1= ID ) ( ',' (m2= INT |c2= ID ) )*
                            {
                            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:649:29: (m1= INT |c1= ID )
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
                                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:649:30: m1= INT
                                    {
                                    m1=(Token)match(input,INT,FOLLOW_INT_in_marking872); 


                                    //        		mark.add(Integer.parseInt((m1!=null?m1.getText():null)));
                                    //        		initMarkedPlaces.add(Integer.parseInt((m1!=null?m1.getText():null)));
                                            		mark.add("p" + (m1!=null?m1.getText():null));
                                                initMarkedPlaces.add("p" + (m1!=null?m1.getText():null));     		
                                            	

                                    }
                                    break;
                                case 2 :
                                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:656:11: c1= ID
                                    {
                                    c1=(Token)match(input,ID,FOLLOW_ID_in_marking898); 


                                            	  String c1_tmp = (c1!=null?c1.getText():null);
                                            	  if ((c1!=null?c1.getText():null).contains(".")) {
                                            	    c1_tmp = c1_tmp.replace(".", "_");
                                            	  }
                                            	  else {
                                            	    c1_tmp = (c1!=null?c1.getText():null);
                                            	  }
                                    //        		result = ConstHashMap.get(c1_tmp);
                                    //        		if(result == null){
                                    //        			System.err.println("error on line " + c1.getLine() + ": " + (c1!=null?c1.getText():null) + " is not a valid constant");
                                    //        			System.exit(1);
                                    //        		}
                                    //        		mark.add(result);
                                    //        		initMarkedPlaces.add(Integer.parseInt((m1!=null?m1.getText():null)));
                                                  mark.add(c1_tmp);
                                                  initMarkedPlaces.add(c1_tmp);
                                            	

                                    }
                                    break;

                            }


                            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:675:11: ( ',' (m2= INT |c2= ID ) )*
                            loop21:
                            do {
                                int alt21=2;
                                int LA21_0 = input.LA(1);

                                if ( (LA21_0==COMMA) ) {
                                    alt21=1;
                                }


                                switch (alt21) {
                            	case 1 :
                            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:675:12: ',' (m2= INT |c2= ID )
                            	    {
                            	    match(input,COMMA,FOLLOW_COMMA_in_marking922); 

                            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:675:16: (m2= INT |c2= ID )
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
                            	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:675:17: m2= INT
                            	            {
                            	            m2=(Token)match(input,INT,FOLLOW_INT_in_marking927); 


                            	            //        		mark.add(Integer.parseInt((m2!=null?m2.getText():null)));
                            	            //        	  initMarkedPlaces.add(Integer.parseInt((m2!=null?m2.getText():null)));
                            	                        mark.add("p"+(m2!=null?m2.getText():null));
                            	                        initMarkedPlaces.add("p" +(m2!=null?m2.getText():null));
                            	                    		
                            	                    	

                            	            }
                            	            break;
                            	        case 2 :
                            	            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:683:11: c2= ID
                            	            {
                            	            c2=(Token)match(input,ID,FOLLOW_ID_in_marking953); 


                            	                    	  String c2_tmp = (c2!=null?c2.getText():null);
                            	                    	  if ((c2!=null?c2.getText():null).contains(".")) {
                            	                    	    c2_tmp = c2_tmp.replace(".", "_");
                            	                    	  }
                            	                    	  else {
                            	                    	    c2_tmp = (c2!=null?c2.getText():null);
                            	                    	  }
                            	            //        		result = ConstHashMap.get(c2_tmp);
                            	            //        		if(result == null){
                            	            //        			System.err.println("error on line " + c2.getLine() + ": " + (c2!=null?c2.getText():null) + " is not a valid constant");
                            	            //        			System.exit(1);
                            	            //        		}
                            	            //        		mark.add(result);
                            	            //        		initMarkedPlaces.add(result);
                            	                          mark.add(c2_tmp);
                            	                          initMarkedPlaces.add(c2_tmp);
                            	                    	

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


                    match(input,LESS,FOLLOW_LESS_in_marking980); 

                    match(input,DIV,FOLLOW_DIV_in_marking982); 

                    match(input,MARKING,FOLLOW_MARKING_in_marking984); 

                    match(input,GREATER,FOLLOW_GREATER_in_marking986); 

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
        	// do for sure before leaving
        }
        return mark;
    }
    // $ANTLR end "marking"



    // $ANTLR start "transition"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:705:1: transition returns [Transition lpnTran] : '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT |pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT |post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/' 'transition' '>' ;
    public final Transition transition() throws RecognitionException {
        Transition lpnTran = null;


        Token lbl=null;
        Token pre=null;
        Token pre1=null;
        Token pre2=null;
        Token pre3=null;
        Token post=null;
        Token post1=null;
        Token post2=null;
        Token post3=null;
        ExprTree guard6 =null;

        PlatuGrammarParser.delay_return delay7 =null;

        ExprTree assertion8 =null;

        HashMap<String, ExprTree> assignment9 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:706:5: ( '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT |pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT |post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/' 'transition' '>' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:706:10: '<' 'transition' 'label' '=' '\"' lbl= ( ID | INT ) '\"' 'preset' '=' ( '\"' '\"' | ( '\"' (pre= INT |pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) ) 'postset' '=' ( '\"' '\"' | ( '\"' (post= INT |post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) ) '>' ( guard )? ( delay )? ( ( assertion ) | ( assignment ) )* '<' '/' 'transition' '>'
            {

            	    		Integer result = null;
            	    		//ArrayList presetList = new ArrayList(); 
            //	    	  VarExprList assignmentList = new VarExprList();
            //          ArrayList<Expression> assertionList = new ArrayList<Expression>();
            //          Expression guardExpr = TrueExpr; 
            //          int delayLB = 0; 
            //          int delayUB = INFINITY;
            //          boolean local = true;

                      ArrayList<Place> presetList = new ArrayList<Place>();  
                      ArrayList<Place> postsetList = new ArrayList<Place>();
                      HashMap<String, ExprTree> assignmentList = new HashMap<String, ExprTree>();
                      // TODO: Need to deal with assertionList
                      //ArrayList<Expression> assertionList = new ArrayList<Expression>();
                      ArrayList<ExprTree> assertionList = new ArrayList<ExprTree>();
                      ExprTree guardExpr = null;
                      int delayLB = 0; 
                      int delayUB = INFINITY;
                      boolean local = true;
            	    		
            	    	

            match(input,LESS,FOLLOW_LESS_in_transition1020); 

            match(input,TRANSITION,FOLLOW_TRANSITION_in_transition1022); 

            match(input,LABEL,FOLLOW_LABEL_in_transition1024); 

            match(input,EQUALS,FOLLOW_EQUALS_in_transition1026); 

            match(input,QUOTE,FOLLOW_QUOTE_in_transition1028); 

            lbl=(Token)input.LT(1);

            if ( input.LA(1)==ID||input.LA(1)==INT ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            match(input,QUOTE,FOLLOW_QUOTE_in_transition1038); 

            match(input,PRESET,FOLLOW_PRESET_in_transition1040); 

            match(input,EQUALS,FOLLOW_EQUALS_in_transition1042); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:728:69: ( '\"' '\"' | ( '\"' (pre= INT |pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' ) )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==QUOTE) ) {
                int LA26_1 = input.LA(2);

                if ( (LA26_1==QUOTE) ) {
                    alt26=1;
                }
                else if ( (LA26_1==ID||LA26_1==INT) ) {
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
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:728:70: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1045); 

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1047); 

                    }
                    break;
                case 2 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:728:80: ( '\"' (pre= INT |pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:728:80: ( '\"' (pre= INT |pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"' )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:728:81: '\"' (pre= INT |pre1= ID ) ( ',' pre2= INT | ',' pre3= ID )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1052); 

                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:728:85: (pre= INT |pre1= ID )
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
                            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:728:86: pre= INT
                            {
                            pre=(Token)match(input,INT,FOLLOW_INT_in_transition1057); 


                                			//presetList.add(Integer.parseInt((pre!=null?pre.getText():null)));
                                			if (pre!=null && initMarkedPlaces.contains(Integer.parseInt((pre!=null?pre.getText():null)))) 
                                			  presetList.add(new Place("p"+(pre!=null?pre.getText():null), true));    			
                                			else 
                                			  presetList.add(new Place("p"+(pre!=null?pre.getText():null), false));
                               			

                            }
                            break;
                        case 2 :
                            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:736:7: pre1= ID
                            {
                            pre1=(Token)match(input,ID,FOLLOW_ID_in_transition1077); 


                              			  String pre1_tmp = (pre1!=null?pre1.getText():null);
                              			  if ((pre1!=null?pre1.getText():null).contains(".")) {
                              			    pre1_tmp = pre1_tmp.replace(".", "_");
                              			  }
                              			  else {
                              			    pre1_tmp = (pre1!=null?pre1.getText():null);
                              			  }
                              				result = ConstHashMap.get(pre1_tmp); 
                              				if(result == null){
                              					System.err.println("error on line " + pre1.getLine() + ": " + (pre1!=null?pre1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				//presetList.add(result);
                              				if (initMarkedPlaces.contains(result)) 
                                        presetList.add(new Place("p"+result, true));
                                      else
                                        presetList.add(new Place("p"+result, false));
                              			

                            }
                            break;

                    }


                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:756:6: ( ',' pre2= INT | ',' pre3= ID )*
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
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:756:8: ',' pre2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1093); 

                    	    pre2=(Token)match(input,INT,FOLLOW_INT_in_transition1097); 


                    	        			//presetList.add(Integer.parseInt((pre2!=null?pre2.getText():null)));
                    	        			if (pre2!=null && initMarkedPlaces.contains(Integer.parseInt((pre2!=null?pre2.getText():null))))
                    	                presetList.add(new Place("p"+(pre2!=null?pre2.getText():null), true));
                    	              else
                    	                presetList.add(new Place("p"+(pre2!=null?pre2.getText():null), false));
                    	       			

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:764:7: ',' pre3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1115); 

                    	    pre3=(Token)match(input,ID,FOLLOW_ID_in_transition1119); 


                    	      			  String pre3_tmp = (pre3!=null?pre3.getText():null);
                    	              if ((pre3!=null?pre3.getText():null).contains(".")) {
                    	                pre3_tmp = pre3_tmp.replace(".", "_");
                    	              }
                    	              else {
                    	                pre3_tmp = (pre3!=null?pre3.getText():null);
                    	              }
                    	      				result = ConstHashMap.get(pre3_tmp); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + pre3.getLine() + ": " + (pre3!=null?pre3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}
                    	      				//presetList.add(result);
                    	      				if (pre3!=null && initMarkedPlaces.contains(Integer.parseInt((pre3!=null?pre3.getText():null))))
                    	                presetList.add(new Place("p"+result, true));
                    	              else
                    	                presetList.add(new Place("p"+result, false));
                    	      			

                    	    }
                    	    break;

                    	default :
                    	    break loop25;
                        }
                    } while (true);


                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1134); 

                    }


                    }
                    break;

            }


            match(input,POSTSET,FOLLOW_POSTSET_in_transition1138); 

            match(input,EQUALS,FOLLOW_EQUALS_in_transition1140); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:784:27: ( '\"' '\"' | ( '\"' (post= INT |post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' ) )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==QUOTE) ) {
                int LA29_1 = input.LA(2);

                if ( (LA29_1==QUOTE) ) {
                    alt29=1;
                }
                else if ( (LA29_1==ID||LA29_1==INT) ) {
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
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:784:29: '\"' '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1144); 

                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1146); 

                    }
                    break;
                case 2 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:784:39: ( '\"' (post= INT |post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:784:39: ( '\"' (post= INT |post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"' )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:784:40: '\"' (post= INT |post1= ID ) ( ( ',' post2= INT ) | ( ',' post3= ID ) )* '\"'
                    {
                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1151); 

                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:784:44: (post= INT |post1= ID )
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
                            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:784:46: post= INT
                            {
                            post=(Token)match(input,INT,FOLLOW_INT_in_transition1157); 


                                			//postsetList.add(Integer.parseInt((post!=null?post.getText():null)));
                                			if (post!=null && initMarkedPlaces.contains(Integer.parseInt((post!=null?post.getText():null))))
                                        postsetList.add(new Place("p"+(post!=null?post.getText():null), true));
                                      else
                                        postsetList.add(new Place("p"+(post!=null?post.getText():null), false));
                                		

                            }
                            break;
                        case 2 :
                            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:792:8: post1= ID
                            {
                            post1=(Token)match(input,ID,FOLLOW_ID_in_transition1177); 


                                		  String post1_tmp = (post1!=null?post1.getText():null);
                                      if ((post1!=null?post1.getText():null).contains(".")) {
                                        post1_tmp = post1_tmp.replace(".", "_");
                                      }
                                      else {
                                        post1_tmp = (post1!=null?post1.getText():null);
                                      }
                                			result = ConstHashMap.get(post1_tmp); 
                              				if(result == null){
                              					System.err.println("error on line " + post1.getLine() + ": " + (post1!=null?post1.getText():null) + " is not a constant");
                              					System.exit(1);
                              				}
                              				//postsetList.add(result);
                              				if (initMarkedPlaces.contains(result))
                                        postsetList.add(new Place("p"+result, true));
                                      else
                                        postsetList.add(new Place("p"+result, false));
                                		

                            }
                            break;

                    }


                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:813:6: ( ( ',' post2= INT ) | ( ',' post3= ID ) )*
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
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:813:8: ( ',' post2= INT )
                    	    {
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:813:8: ( ',' post2= INT )
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:813:9: ',' post2= INT
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1202); 

                    	    post2=(Token)match(input,INT,FOLLOW_INT_in_transition1206); 

                    	    }



                    	        			//postsetList.add(Integer.parseInt((post2!=null?post2.getText():null)));
                    	        			if (post2!=null && initMarkedPlaces.contains(Integer.parseInt((post2!=null?post2.getText():null))))
                    	                postsetList.add(new Place("p"+(post2!=null?post2.getText():null), true));
                    	              else
                    	                postsetList.add(new Place("p"+(post2!=null?post2.getText():null), false));
                    	        		

                    	    }
                    	    break;
                    	case 2 :
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:821:8: ( ',' post3= ID )
                    	    {
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:821:8: ( ',' post3= ID )
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:821:9: ',' post3= ID
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_transition1226); 

                    	    post3=(Token)match(input,ID,FOLLOW_ID_in_transition1229); 

                    	    }



                    	        		  String post3_tmp = (post3!=null?post3.getText():null);
                    	              if ((post3!=null?post3.getText():null).contains(".")) {
                    	                post3_tmp = post3_tmp.replace(".", "_");
                    	              }
                    	              else {
                    	                post3_tmp = (post3!=null?post3.getText():null);
                    	              }
                    	        			result = ConstHashMap.get(post3_tmp); 
                    	      				if(result == null){
                    	      					System.err.println("error on line " + post3.getLine() + ": " + (post3!=null?post3.getText():null) + " is not a constant");
                    	      					System.exit(1);
                    	      				}		
                    	    //  				postsetList.add(result);
                    	              if (initMarkedPlaces.contains(result))
                    	                postsetList.add(new Place("p"+result, true));
                    	              else
                    	                postsetList.add(new Place("p"+result, false));
                    	        		

                    	    }
                    	    break;

                    	default :
                    	    break loop28;
                        }
                    } while (true);


                    match(input,QUOTE,FOLLOW_QUOTE_in_transition1248); 

                    }


                    }
                    break;

            }


            match(input,GREATER,FOLLOW_GREATER_in_transition1253); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:841:20: ( guard )?
            int alt30=2;
            switch ( input.LA(1) ) {
                case BITWISE_NEGATION:
                case FALSE:
                case INT:
                case MINUS:
                case NEGATION:
                case PLUS:
                case TRUE:
                    {
                    alt30=1;
                    }
                    break;
                case ID:
                    {
                    int LA30_2 = input.LA(2);

                    if ( ((LA30_2 >= AND && LA30_2 <= BITWISE_LSHIFT)||(LA30_2 >= BITWISE_OR && LA30_2 <= BITWISE_XOR)||LA30_2==DIV||LA30_2==EQUIV||(LA30_2 >= GREATER && LA30_2 <= GREATER_EQUAL)||LA30_2==IMPLICATION||(LA30_2 >= LESS && LA30_2 <= LESS_EQUAL)||LA30_2==MINUS||(LA30_2 >= NOT_EQUIV && LA30_2 <= OR)||LA30_2==PLUS||LA30_2==QMARK||LA30_2==SEMICOLON||LA30_2==TIMES) ) {
                        alt30=1;
                    }
                    }
                    break;
                case LPAREN:
                    {
                    int LA30_3 = input.LA(2);

                    if ( (LA30_3==INT) ) {
                        int LA30_5 = input.LA(3);

                        if ( ((LA30_5 >= AND && LA30_5 <= BITWISE_LSHIFT)||(LA30_5 >= BITWISE_OR && LA30_5 <= BITWISE_XOR)||LA30_5==DIV||LA30_5==EQUIV||(LA30_5 >= GREATER && LA30_5 <= GREATER_EQUAL)||LA30_5==IMPLICATION||(LA30_5 >= LESS && LA30_5 <= LESS_EQUAL)||LA30_5==MINUS||(LA30_5 >= NOT_EQUIV && LA30_5 <= OR)||LA30_5==PLUS||LA30_5==QMARK||LA30_5==RPAREN||LA30_5==TIMES) ) {
                            alt30=1;
                        }
                    }
                    else if ( (LA30_3==BITWISE_NEGATION||LA30_3==FALSE||LA30_3==ID||LA30_3==LPAREN||LA30_3==MINUS||LA30_3==NEGATION||LA30_3==PLUS||LA30_3==TRUE) ) {
                        alt30=1;
                    }
                    }
                    break;
            }

            switch (alt30) {
                case 1 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:841:21: guard
                    {
                    pushFollow(FOLLOW_guard_in_transition1256);
                    guard6=guard();

                    state._fsp--;



                        			guardExpr = guard6;
                        		

                    }
                    break;

            }


            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:845:9: ( delay )?
            int alt31=2;
            int LA31_0 = input.LA(1);

            if ( (LA31_0==LPAREN) ) {
                alt31=1;
            }
            switch (alt31) {
                case 1 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:845:10: delay
                    {
                    pushFollow(FOLLOW_delay_in_transition1276);
                    delay7=delay();

                    state._fsp--;



                        		  // TODO: ignored delay for untimed models
                        			delayLB = (delay7!=null?delay7.delayLB:0); 
                        			delayUB = (delay7!=null?delay7.delayUB:0);
                        		

                    }
                    break;

            }


            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:851:9: ( ( assertion ) | ( assignment ) )*
            loop32:
            do {
                int alt32=3;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==59) ) {
                    alt32=1;
                }
                else if ( (LA32_0==ID) ) {
                    alt32=2;
                }


                switch (alt32) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:851:10: ( assertion )
            	    {
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:851:10: ( assertion )
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:851:11: assertion
            	    {
            	    pushFollow(FOLLOW_assertion_in_transition1297);
            	    assertion8=assertion();

            	    state._fsp--;



            	        		  // TODO: ignored assertions temporarily
            	        			if(assertion8 != null){		
            	      					assertionList.add(assertion8);
            	      				}
            	        		

            	    }


            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:858:10: ( assignment )
            	    {
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:858:10: ( assignment )
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:858:11: assignment
            	    {
            	    pushFollow(FOLLOW_assignment_in_transition1317);
            	    assignment9=assignment();

            	    state._fsp--;



            	        			assignmentList.putAll(assignment9);
            	        		

            	    }


            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            match(input,LESS,FOLLOW_LESS_in_transition1336); 

            match(input,DIV,FOLLOW_DIV_in_transition1338); 

            match(input,TRANSITION,FOLLOW_TRANSITION_in_transition1340); 

            match(input,GREATER,FOLLOW_GREATER_in_transition1342); 


                    	// create new lpn transitions and add assertions
                    	// lpnTran = new LPNTran((lbl!=null?lbl.getText():null), TransitionIndex++, presetList, postsetList, guardExpr, assignmentList, delayLB, delayUB, local);
                    	lpnTran = new Transition();
                    	lpnTran.setName("t" + (lbl!=null?lbl.getText():null));
                    	lpnTran.setIndex(TransitionIndex++);
                    	for (Place p: presetList) {
                    	  lpnTran.addPreset(p);
                    	}
                    	for (Place p: postsetList) {
                        lpnTran.addPostset(p);
                      }
                      lpnTran.addEnablingWithoutLPN(guardExpr);
                      for (String var : assignmentList.keySet()) {
                        lpnTran.addIntAssign(var, assignmentList.get(var));
                      }
                      
                    	// TODO: Add assertionList to the new lpn transition
            //        	if(assertionList.size() > 0){
            //        		lpnTran.addAllAssertions(assertionList);
            //        	}

                      // add non-local transition to associated LPNs
                      for(String var : assignmentList.keySet()){
                        if(Outputs.contains(var)){
                          // local is determined by isLocal() in Transition.java
                          local = false;            
            //              if(GlobalInputMap.containsKey(var)){
            //                for(LhpnFile lpn : GlobalInputMap.get(var)){
            //                  lpn.addInputTran(lpnTran);
            //                  // dstLpn is added by setDstLpnList in Transition.java
            //                  lpnTran.addDstLpn(lpn);
            //                }
            //              }
                        }
                        
                        // map lpn transition with output and potential outuput variables
                        if(GlobalTranMap.containsKey(var)){
                          GlobalTranMap.get(var).add(lpnTran);
                          System.out.println("Add "+ lpnTran.getLabel() + " to variable " + var);
                        }
                        else{
                          List<Transition> tranList = new ArrayList<Transition>();
                          tranList.add(lpnTran);
                          GlobalTranMap.put(var, tranList);
                          System.out.println("Create tranList for variable " + var + ", and add " + lpnTran.getLabel() + " to it.");
                          
                        }
                      }

            //     		lpnTran.setLocal(local);
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
        	// do for sure before leaving
        }
        return lpnTran;
    }
    // $ANTLR end "transition"



    // $ANTLR start "assertion"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:920:1: assertion returns [ExprTree booleanExpr] : 'assert' '(' expression ')' ';' ;
    public final ExprTree assertion() throws RecognitionException {
        ExprTree booleanExpr = null;


        ExprTree expression10 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:921:2: ( 'assert' '(' expression ')' ';' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:921:4: 'assert' '(' expression ')' ';'
            {
            booleanExpr = null;// TODO: assertion
            	

            match(input,59,FOLLOW_59_in_assertion1377); 

            match(input,LPAREN,FOLLOW_LPAREN_in_assertion1379); 

            pushFollow(FOLLOW_expression_in_assertion1381);
            expression10=expression();

            state._fsp--;


            match(input,RPAREN,FOLLOW_RPAREN_in_assertion1383); 

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assertion1385); 


            				//booleanExpr = new Expression(expression10);
            				booleanExpr = new ExprTree(expression10);
            			

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return booleanExpr;
    }
    // $ANTLR end "assertion"



    // $ANTLR start "guard"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:930:1: guard returns [ExprTree expr] : expression ';' ;
    public final ExprTree guard() throws RecognitionException {
        ExprTree expr = null;


        ExprTree expression11 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:931:5: ( expression ';' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:931:9: expression ';'
            {
            pushFollow(FOLLOW_expression_in_guard1411);
            expression11=expression();

            state._fsp--;


            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_guard1413); 


               				//expr = new Expression(expression11);
               				expr = new ExprTree(expression11);
                		

            }

        }

            catch (RecognitionException e){
            	System.err.println(e.input.getSourceName() + ":");
            	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
            	System.err.println();
            	System.exit(1);
            }

        finally {
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "guard"


    public static class delay_return extends ParserRuleReturnScope {
        public int delayLB;
        public int delayUB;
    };


    // $ANTLR start "delay"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:938:1: delay returns [int delayLB, int delayUB] : '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' ;
    public final PlatuGrammarParser.delay_return delay() throws RecognitionException {
        PlatuGrammarParser.delay_return retval = new PlatuGrammarParser.delay_return();
        retval.start = input.LT(1);


        Token lb=null;
        Token ub=null;

        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:939:5: ( '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';' )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:939:8: '(' lb= INT ',' (ub= INT | 'inf' ) ')' ';'
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_delay1447); 

            lb=(Token)match(input,INT,FOLLOW_INT_in_delay1451); 


                			retval.delayLB = Integer.parseInt((lb!=null?lb.getText():null));
               			

            match(input,COMMA,FOLLOW_COMMA_in_delay1465); 

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:943:8: (ub= INT | 'inf' )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==INT) ) {
                alt33=1;
            }
            else if ( (LA33_0==61) ) {
                alt33=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;

            }
            switch (alt33) {
                case 1 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:943:9: ub= INT
                    {
                    ub=(Token)match(input,INT,FOLLOW_INT_in_delay1470); 


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
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:960:6: 'inf'
                    {
                    match(input,61,FOLLOW_61_in_delay1485); 


                     				retval.delayUB = INFINITY;
                    			

                    }
                    break;

            }


            match(input,RPAREN,FOLLOW_RPAREN_in_delay1498); 

            match(input,SEMICOLON,FOLLOW_SEMICOLON_in_delay1500); 

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
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "delay"



    // $ANTLR start "assignment"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:967:1: assignment returns [HashMap<String, ExprTree> assign] : ( (var= ID '=' expression ';' ) | (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' ) );
    public final HashMap<String, ExprTree> assignment() throws RecognitionException {
        HashMap<String, ExprTree> assign = null;


        Token var=null;
        Token var2=null;
        ExprTree expression12 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:968:5: ( (var= ID '=' expression ';' ) | (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' ) )
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==ID) ) {
                int LA35_1 = input.LA(2);

                if ( (LA35_1==EQUALS) ) {
                    alt35=1;
                }
                else if ( (LA35_1==57) ) {
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
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:968:9: (var= ID '=' expression ';' )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:968:9: (var= ID '=' expression ';' )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:968:10: var= ID '=' expression ';'
                    {
                    var=(Token)match(input,ID,FOLLOW_ID_in_assignment1526); 

                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1528); 

                    	
                        		  String var_tmp = (var!=null?var.getText():null);
                        		  if ((var!=null?var.getText():null).contains(".")) {
                        		    var_tmp = var_tmp.replace(".", "_");
                        		  }
                        		  else {
                        		    var_tmp = (var!=null?var.getText():null);
                        		  }
                        			// make sure only global, internal and output variables are assigned
                        			if(GlobalConstHashMap.containsKey(var_tmp)){
                        				System.err.println("error on line " + var.getLine() + ": global constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(ConstHashMap.containsKey(var_tmp)){
                        				System.err.println("error on line " + var.getLine() + ": constant " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        			else if(!Outputs.contains(var_tmp) && !Internals.contains(var_tmp)){
                        				System.err.println("error on line " + var.getLine() + ": input variable " + (var!=null?var.getText():null) + " cannot be assigned");
                        				System.exit(1);
                        			}
                        		

                    pushFollow(FOLLOW_expression_in_assignment1544);
                    expression12=expression();

                    state._fsp--;


                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1546); 


                    	    		Integer varCount = VarCountMap.get(var_tmp);
                    	    		if(varCount != null){
                    	    			VarCountMap.put(var_tmp, ++varCount);
                    	    		}
                    	    		
                    //	    		Expression expr = new Expression(expression12);
                    //	    		assign = new VarExpr(VarNodeMap.get((var!=null?var.getText():null)), expr);

                                ExprTree expr = new ExprTree(expression12);
                                assign = new HashMap<String, ExprTree>();
                                assign.put(var_tmp, expr);
                    	   		

                    }


                    }
                    break;
                case 2 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1006:10: (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' )
                    {
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1006:10: (var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';' )
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1006:11: var2= ID ( '[' ( INT | ID ) ']' )+ '=' expression ';'
                    {
                    var2=(Token)match(input,ID,FOLLOW_ID_in_assignment1575); 


                    	   			List<Integer> indexList = new ArrayList<Integer>();
                    	   			
                    	   		

                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1011:6: ( '[' ( INT | ID ) ']' )+
                    int cnt34=0;
                    loop34:
                    do {
                        int alt34=2;
                        int LA34_0 = input.LA(1);

                        if ( (LA34_0==57) ) {
                            alt34=1;
                        }


                        switch (alt34) {
                    	case 1 :
                    	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1011:7: '[' ( INT | ID ) ']'
                    	    {
                    	    match(input,57,FOLLOW_57_in_assignment1592); 

                    	    if ( input.LA(1)==ID||input.LA(1)==INT ) {
                    	        input.consume();
                    	        state.errorRecovery=false;
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        throw mse;
                    	    }


                    	    match(input,58,FOLLOW_58_in_assignment1602); 

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


                    match(input,EQUALS,FOLLOW_EQUALS_in_assignment1606); 

                    pushFollow(FOLLOW_expression_in_assignment1608);
                    expression();

                    state._fsp--;


                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_assignment1610); 

                       		  
                    	   		

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
        	// do for sure before leaving
        }
        return assign;
    }
    // $ANTLR end "assignment"



    // $ANTLR start "term"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1018:1: term returns [ExprTree expr] : ( ID | LPAREN expression RPAREN | INT | TRUE | FALSE );
    public final ExprTree term() throws RecognitionException {
        ExprTree expr = null;


        Token ID13=null;
        Token INT15=null;
        ExprTree expression14 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1019:5: ( ID | LPAREN expression RPAREN | INT | TRUE | FALSE )
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
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1019:9: ID
                    {
                    ID13=(Token)match(input,ID,FOLLOW_ID_in_term1654); 


                        		  String ID_tmp = (ID13!=null?ID13.getText():null);
                              if ((ID13!=null?ID13.getText():null).contains(".")) {
                                ID_tmp = ID_tmp.replace(".", "_");
                              }
                              else {
                                ID_tmp = (ID13!=null?ID13.getText():null);
                              }
                        			if(ConstHashMap.containsKey(ID_tmp)){
                        				//expr = new ConstNode((ID13!=null?ID13.getText():null), ConstHashMap.get((ID13!=null?ID13.getText():null)));
                        				expr = new ExprTree(ConstHashMap.get(ID_tmp)+"");
                        			}
                        			else if(GlobalConstHashMap.containsKey(ID_tmp)){
                        			  //expr = new ConstNode((ID13!=null?ID13.getText():null), GlobalConstHashMap.get((ID13!=null?ID13.getText():null)));
                        			  expr = new ExprTree(GlobalConstHashMap.get(ID_tmp)+"");
                        			}
                        			else if(StatevectorMap.containsKey(ID_tmp)){ 
                        			  //expr = VarNodeMap.get((ID13!=null?ID13.getText():null));
                        			  expr = VarNodeMap.get(ID_tmp);
                        			}
                        			else{ // identify new input variable
                    //    				// create expression
                    //					expr = new platu.lpn.io.expression.VarNode((ID13!=null?ID13.getText():null));
                    					
                    					// label as input and initialize to 0
                    					StatevectorMap.put(ID_tmp, 0);
                    					System.out.println("label (" + ID_tmp + ") as input and initialize to 0. Added to Inputs.");
                    					Inputs.add(ID_tmp);
                    					for (String input : Inputs) {
                    					  System.out.println("@3: input = " + input);
                    					}
                    					
                    					 // identify new input variable
                              // create expression
                    	        ExprTree newVarNode = new ExprTree(ID_tmp); 
                              newVarNode.setIntegerSignals(StatevectorMap.keySet());      
                              expr = newVarNode.getExprTree();
                              VarNodeMap.put(ID_tmp, newVarNode);
                              
                    //					int index = VariableIndex++;
                    //	    			VarIndexMap.insert((ID13!=null?ID13.getText():null), index);
                    //	    			VarNode newVarNode = new VarNode((ID13!=null?ID13.getText():null), index);
                    //	    			VarNodeMap.put((ID13!=null?ID13.getText():null), newVarNode);
                    //	    			expr = newVarNode;
                    	    			
                    	    			// if associated output variable has not been defined insert with null value,
                    	    			// otherwise get output variable and relabel from internal to output, 
                    	    			// get output value and initialize input statevector, label lpn transitions associated with output as non-local
                    	    			// and add to current lpn's inputTranList
                       			if(!GlobalInterfaceMap.containsKey(ID_tmp)){
                       				GlobalInterfaceMap.put(ID_tmp, null);
                       				System.out.println("@ term, Added entry (" + ID_tmp + "null) to GlobalInterfaceMap");
                       			}
                       			else{
                       				Integer value = GlobalInterfaceMap.get(ID_tmp);
                       				if(value != null){
                       					StatevectorMap.put(ID_tmp, value);		
                       					LhpnFile outputLPN = GlobalOutputMap.get(ID_tmp);
                       					if(outputLPN.getAllInternals().keySet().contains(ID_tmp)){
                       						outputLPN.getAllInternals().remove(ID_tmp);
                    //   						for (String varID : outputLPN.getAllInternals().keySet()) {
                    //   						 System.out.println("@term-outputLPN: internal var " + varID + " is in LPN " + outputLPN.getLabel());
                    //   						}
                       						outputLPN.addOutput(ID_tmp, "integer", value+"");
                       						//System.out.println("@term : Removed internal variable " + ID_tmp);
                       						//System.out.println("@term : Added output variable " + ID_tmp + " to LPN " + outputLPN.getLabel());
                       					} 					
                       					for(Transition tran : GlobalTranMap.get(ID_tmp)){
                       					  //tran.setLocalFlag(false);
                       						//outputLPN.addOutputTran(tran);
                       						outputLPN.addTransition(tran);
                       						System.out.println("@term : Added transition " + tran.getLabel() + " to LPN " + outputLPN.getLabel());
                       						inputTranList.add(tran);
                       						System.out.println("@term : Added transition " + tran.getLabel() + " to inputTranList.");
                       						System.out.println("inputTranList : ");
                       						for (Transition t : inputTranList) {
                       						   System.out.println("Transition: " + t.getLabel());
                       						}
                       						System.out.println("~~~~~~~~~~~~~");
                       					}
                       				}
                       			}
                     			}
                    		

                    }
                    break;
                case 2 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1104:9: LPAREN expression RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_term1672); 

                    pushFollow(FOLLOW_expression_in_term1674);
                    expression14=expression();

                    state._fsp--;


                    match(input,RPAREN,FOLLOW_RPAREN_in_term1676); 

                    expr = expression14;

                    }
                    break;
                case 3 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1105:9: INT
                    {
                    INT15=(Token)match(input,INT,FOLLOW_INT_in_term1688); 

                     //{expr = new ConstNode("name", Integer.parseInt((INT15!=null?INT15.getText():null)));}
                                ExprTree tree = new ExprTree(Integer.parseInt((INT15!=null?INT15.getText():null))+"");
                                expr = tree.getExprTree();         
                            

                    }
                    break;
                case 4 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1109:9: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_term1701); 


                              ExprTree tree = new ExprTree("true");
                              expr = tree.getExprTree();
                            

                    }
                    break;
                case 5 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1113:9: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_term1713); 


                              ExprTree tree = new ExprTree("false");
                              expr = tree.getExprTree();
                            

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "term"



    // $ANTLR start "unary"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1123:1: unary returns [ExprTree expr] : ( '+' | ( '-' ) )* term ;
    public final ExprTree unary() throws RecognitionException {
        ExprTree expr = null;


        ExprTree term16 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1124:5: ( ( '+' | ( '-' ) )* term )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1124:9: ( '+' | ( '-' ) )* term
            {
            boolean positive = true;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1125:6: ( '+' | ( '-' ) )*
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
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1125:7: '+'
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_unary1754); 

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1125:13: ( '-' )
            	    {
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1125:13: ( '-' )
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1125:14: '-'
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_unary1759); 

            	    if(positive){ positive = false;} else {positive = true;}

            	    }


            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);


            pushFollow(FOLLOW_term_in_unary1766);
            term16=term();

            state._fsp--;



                		if(!positive){
                			//expr = new MinNode(term16);
                			// TODO: Correct ExprTree for unary?
                			expr = new ExprTree(term16, null, "U-", 'a');
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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "unary"



    // $ANTLR start "bitwiseNegation"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1138:1: bitwiseNegation returns [ExprTree expr] : ( '~' )* unary ;
    public final ExprTree bitwiseNegation() throws RecognitionException {
        ExprTree expr = null;


        ExprTree unary17 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1139:2: ( ( '~' )* unary )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1139:5: ( '~' )* unary
            {
            boolean neg = false;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1140:3: ( '~' )*
            loop38:
            do {
                int alt38=2;
                int LA38_0 = input.LA(1);

                if ( (LA38_0==BITWISE_NEGATION) ) {
                    alt38=1;
                }


                switch (alt38) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1140:4: '~'
            	    {
            	    match(input,BITWISE_NEGATION,FOLLOW_BITWISE_NEGATION_in_bitwiseNegation1798); 

            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop38;
                }
            } while (true);


            pushFollow(FOLLOW_unary_in_bitwiseNegation1804);
            unary17=unary();

            state._fsp--;



            				if(neg){
            					//expr = new BitNegNode(unary17);
            					expr = new ExprTree(null, unary17, "~", 'l');
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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "bitwiseNegation"



    // $ANTLR start "negation"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1152:1: negation returns [ExprTree expr] : ( '!' )* bitwiseNegation ;
    public final ExprTree negation() throws RecognitionException {
        ExprTree expr = null;


        ExprTree bitwiseNegation18 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1153:2: ( ( '!' )* bitwiseNegation )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1153:4: ( '!' )* bitwiseNegation
            {
            boolean neg = false;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1154:3: ( '!' )*
            loop39:
            do {
                int alt39=2;
                int LA39_0 = input.LA(1);

                if ( (LA39_0==NEGATION) ) {
                    alt39=1;
                }


                switch (alt39) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1154:4: '!'
            	    {
            	    match(input,NEGATION,FOLLOW_NEGATION_in_negation1830); 

            	    if(neg){neg = false;} else{neg = true;}

            	    }
            	    break;

            	default :
            	    break loop39;
                }
            } while (true);


            pushFollow(FOLLOW_bitwiseNegation_in_negation1836);
            bitwiseNegation18=bitwiseNegation();

            state._fsp--;



            				if(neg){
            					//expr = new NegNode(bitwiseNegation18);
            					// TODO: Correct translation of negation?
            					expr = new ExprTree(null, bitwiseNegation18, "~", 'l');
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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "negation"



    // $ANTLR start "mult"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1167:1: mult returns [ExprTree expr] : op1= negation ( '*' op2= negation | '/' op2= negation )* ;
    public final ExprTree mult() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1168:5: (op1= negation ( '*' op2= negation | '/' op2= negation )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1168:9: op1= negation ( '*' op2= negation | '/' op2= negation )*
            {
            pushFollow(FOLLOW_negation_in_mult1865);
            op1=negation();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1169:6: ( '*' op2= negation | '/' op2= negation )*
            loop40:
            do {
                int alt40=3;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==TIMES) ) {
                    alt40=1;
                }
                else if ( (LA40_0==DIV) ) {
                    alt40=2;
                }


                switch (alt40) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1169:8: '*' op2= negation
            	    {
            	    match(input,TIMES,FOLLOW_TIMES_in_mult1877); 

            	    pushFollow(FOLLOW_negation_in_mult1881);
            	    op2=negation();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "*", 'a');

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1170:8: '/' op2= negation
            	    {
            	    match(input,DIV,FOLLOW_DIV_in_mult1892); 

            	    pushFollow(FOLLOW_negation_in_mult1896);
            	    op2=negation();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "/", 'a');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "mult"


    public static class add_return extends ParserRuleReturnScope {
        public ExprTree expr;
    };


    // $ANTLR start "add"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1176:1: add returns [ExprTree expr] : op1= mult ( '+' op2= mult | '-' op2= mult )* ;
    public final PlatuGrammarParser.add_return add() throws RecognitionException {
        PlatuGrammarParser.add_return retval = new PlatuGrammarParser.add_return();
        retval.start = input.LT(1);


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1177:5: (op1= mult ( '+' op2= mult | '-' op2= mult )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1177:9: op1= mult ( '+' op2= mult | '-' op2= mult )*
            {
            pushFollow(FOLLOW_mult_in_add1948);
            op1=mult();

            state._fsp--;


            retval.expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1178:6: ( '+' op2= mult | '-' op2= mult )*
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
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1178:8: '+' op2= mult
            	    {
            	    match(input,PLUS,FOLLOW_PLUS_in_add1959); 

            	    pushFollow(FOLLOW_mult_in_add1963);
            	    op2=mult();

            	    state._fsp--;


            	    retval.expr = new ExprTree(retval.expr, op2, "+", 'a');

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1179:9: '-' op2= mult
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_add1975); 

            	    pushFollow(FOLLOW_mult_in_add1979);
            	    op2=mult();

            	    state._fsp--;


            	    retval.expr = new ExprTree(retval.expr, op2, "-", 'a');

            	    }
            	    break;

            	default :
            	    break loop41;
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
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "add"



    // $ANTLR start "shift"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1183:1: shift returns [ExprTree expr] : op1= add ( '<<' op2= add | '>>' op2= add )* ;
    public final ExprTree shift() throws RecognitionException {
        ExprTree expr = null;


        PlatuGrammarParser.add_return op1 =null;

        PlatuGrammarParser.add_return op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1184:5: (op1= add ( '<<' op2= add | '>>' op2= add )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1184:9: op1= add ( '<<' op2= add | '>>' op2= add )*
            {
            pushFollow(FOLLOW_add_in_shift2018);
            op1=add();

            state._fsp--;


            expr = (op1!=null?op1.expr:null);

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1185:7: ( '<<' op2= add | '>>' op2= add )*
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
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1185:9: '<<' op2= add
            	    {
            	    match(input,BITWISE_LSHIFT,FOLLOW_BITWISE_LSHIFT_in_shift2030); 

            	    pushFollow(FOLLOW_add_in_shift2034);
            	    op2=add();

            	    state._fsp--;


            	    expr = new ExprTree(new ExprTree("int(" + (op1!=null?input.toString(op1.start,op1.stop):null) + ")"), new ExprTree(new ExprTree("2"), (op2!=null?op2.expr:null), "^", 'a'), "*", 'a');

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1186:11: '>>' op2= add
            	    {
            	    match(input,BITWISE_RSHIFT,FOLLOW_BITWISE_RSHIFT_in_shift2048); 

            	    pushFollow(FOLLOW_add_in_shift2052);
            	    op2=add();

            	    state._fsp--;


            	    expr = new ExprTree(new ExprTree("int(" + (op1!=null?input.toString(op1.start,op1.stop):null) + ")"), new ExprTree(new ExprTree("2"), (op2!=null?op2.expr:null), "^", 'a'), "/", 'a');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "shift"



    // $ANTLR start "relation"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1193:1: relation returns [ExprTree expr] : op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* ;
    public final ExprTree relation() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1194:5: (op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1194:9: op1= shift ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
            {
            pushFollow(FOLLOW_shift_in_relation2091);
            op1=shift();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1195:6: ( '<' op2= shift | '<=' op2= shift | '>=' op2= shift | '>' op2= shift )*
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
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1195:8: '<' op2= shift
            	    {
            	    match(input,LESS,FOLLOW_LESS_in_relation2102); 

            	    pushFollow(FOLLOW_shift_in_relation2106);
            	    op2=shift();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "<", 'r');

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1196:9: '<=' op2= shift
            	    {
            	    match(input,LESS_EQUAL,FOLLOW_LESS_EQUAL_in_relation2118); 

            	    pushFollow(FOLLOW_shift_in_relation2122);
            	    op2=shift();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "<=", 'r');

            	    }
            	    break;
            	case 3 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1197:9: '>=' op2= shift
            	    {
            	    match(input,GREATER_EQUAL,FOLLOW_GREATER_EQUAL_in_relation2134); 

            	    pushFollow(FOLLOW_shift_in_relation2138);
            	    op2=shift();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, ">=", 'r');

            	    }
            	    break;
            	case 4 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1198:9: '>' op2= shift
            	    {
            	    match(input,GREATER,FOLLOW_GREATER_in_relation2150); 

            	    pushFollow(FOLLOW_shift_in_relation2154);
            	    op2=shift();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, ">", 'r');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "relation"



    // $ANTLR start "equivalence"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1202:1: equivalence returns [ExprTree expr] : op1= relation ( '==' op2= relation | '!=' op2= relation )* ;
    public final ExprTree equivalence() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1203:5: (op1= relation ( '==' op2= relation | '!=' op2= relation )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1203:9: op1= relation ( '==' op2= relation | '!=' op2= relation )*
            {
            pushFollow(FOLLOW_relation_in_equivalence2194);
            op1=relation();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1204:6: ( '==' op2= relation | '!=' op2= relation )*
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
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1204:8: '==' op2= relation
            	    {
            	    match(input,EQUIV,FOLLOW_EQUIV_in_equivalence2205); 

            	    pushFollow(FOLLOW_relation_in_equivalence2209);
            	    op2=relation();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "=", 'r');

            	    }
            	    break;
            	case 2 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1206:8: '!=' op2= relation
            	    {
            	    match(input,NOT_EQUIV,FOLLOW_NOT_EQUIV_in_equivalence2226); 

            	    pushFollow(FOLLOW_relation_in_equivalence2230);
            	    op2=relation();

            	    state._fsp--;


            	    expr = new ExprTree(new ExprTree(expr, op2, "=", 'r'), null, "~", 'l');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "equivalence"



    // $ANTLR start "bitwiseAnd"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1210:1: bitwiseAnd returns [ExprTree expr] : op1= equivalence ( '&' op2= equivalence )* ;
    public final ExprTree bitwiseAnd() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1211:5: (op1= equivalence ( '&' op2= equivalence )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1211:9: op1= equivalence ( '&' op2= equivalence )*
            {
            pushFollow(FOLLOW_equivalence_in_bitwiseAnd2269);
            op1=equivalence();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1212:6: ( '&' op2= equivalence )*
            loop45:
            do {
                int alt45=2;
                int LA45_0 = input.LA(1);

                if ( (LA45_0==BITWISE_AND) ) {
                    alt45=1;
                }


                switch (alt45) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1212:8: '&' op2= equivalence
            	    {
            	    match(input,BITWISE_AND,FOLLOW_BITWISE_AND_in_bitwiseAnd2281); 

            	    pushFollow(FOLLOW_equivalence_in_bitwiseAnd2285);
            	    op2=equivalence();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "&", 'w');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "bitwiseAnd"



    // $ANTLR start "bitwiseXor"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1216:1: bitwiseXor returns [ExprTree expr] : op1= bitwiseAnd ( '^' op2= bitwiseAnd )* ;
    public final ExprTree bitwiseXor() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1217:5: (op1= bitwiseAnd ( '^' op2= bitwiseAnd )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1217:9: op1= bitwiseAnd ( '^' op2= bitwiseAnd )*
            {
            pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2324);
            op1=bitwiseAnd();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1218:6: ( '^' op2= bitwiseAnd )*
            loop46:
            do {
                int alt46=2;
                int LA46_0 = input.LA(1);

                if ( (LA46_0==BITWISE_XOR) ) {
                    alt46=1;
                }


                switch (alt46) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1218:8: '^' op2= bitwiseAnd
            	    {
            	    match(input,BITWISE_XOR,FOLLOW_BITWISE_XOR_in_bitwiseXor2335); 

            	    pushFollow(FOLLOW_bitwiseAnd_in_bitwiseXor2339);
            	    op2=bitwiseAnd();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "X", 'w');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "bitwiseXor"



    // $ANTLR start "bitwiseOr"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1222:1: bitwiseOr returns [ExprTree expr] : op1= bitwiseXor ( '|' op2= bitwiseXor )* ;
    public final ExprTree bitwiseOr() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1223:5: (op1= bitwiseXor ( '|' op2= bitwiseXor )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1223:9: op1= bitwiseXor ( '|' op2= bitwiseXor )*
            {
            pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2378);
            op1=bitwiseXor();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1224:6: ( '|' op2= bitwiseXor )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==BITWISE_OR) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1224:8: '|' op2= bitwiseXor
            	    {
            	    match(input,BITWISE_OR,FOLLOW_BITWISE_OR_in_bitwiseOr2389); 

            	    pushFollow(FOLLOW_bitwiseXor_in_bitwiseOr2393);
            	    op2=bitwiseXor();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "|", 'w');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "bitwiseOr"



    // $ANTLR start "and"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1228:1: and returns [ExprTree expr] : op1= bitwiseOr ( '&&' op2= bitwiseOr )* ;
    public final ExprTree and() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1229:5: (op1= bitwiseOr ( '&&' op2= bitwiseOr )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1229:9: op1= bitwiseOr ( '&&' op2= bitwiseOr )*
            {
            pushFollow(FOLLOW_bitwiseOr_in_and2433);
            op1=bitwiseOr();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1230:6: ( '&&' op2= bitwiseOr )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==AND) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1230:8: '&&' op2= bitwiseOr
            	    {
            	    match(input,AND,FOLLOW_AND_in_and2444); 

            	    pushFollow(FOLLOW_bitwiseOr_in_and2448);
            	    op2=bitwiseOr();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "&&", 'l');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "and"



    // $ANTLR start "or"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1235:1: or returns [ExprTree expr] : op1= and ( '||' op2= and )* ;
    public final ExprTree or() throws RecognitionException {
        ExprTree expr = null;


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1236:5: (op1= and ( '||' op2= and )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1236:9: op1= and ( '||' op2= and )*
            {
            pushFollow(FOLLOW_and_in_or2493);
            op1=and();

            state._fsp--;


            expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1237:6: ( '||' op2= and )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==OR) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1237:8: '||' op2= and
            	    {
            	    match(input,OR,FOLLOW_OR_in_or2504); 

            	    pushFollow(FOLLOW_and_in_or2508);
            	    op2=and();

            	    state._fsp--;


            	    expr = new ExprTree(expr, op2, "||", 'l');

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
        	// do for sure before leaving
        }
        return expr;
    }
    // $ANTLR end "or"


    public static class implication_return extends ParserRuleReturnScope {
        public ExprTree expr;
    };


    // $ANTLR start "implication"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1242:1: implication returns [ExprTree expr] : op1= or ( '->' op2= or )* ;
    public final PlatuGrammarParser.implication_return implication() throws RecognitionException {
        PlatuGrammarParser.implication_return retval = new PlatuGrammarParser.implication_return();
        retval.start = input.LT(1);


        ExprTree op1 =null;

        ExprTree op2 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1243:5: (op1= or ( '->' op2= or )* )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1243:7: op1= or ( '->' op2= or )*
            {
            pushFollow(FOLLOW_or_in_implication2548);
            op1=or();

            state._fsp--;


            retval.expr = op1;

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1244:6: ( '->' op2= or )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==IMPLICATION) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1244:8: '->' op2= or
            	    {
            	    match(input,IMPLICATION,FOLLOW_IMPLICATION_in_implication2559); 

            	    pushFollow(FOLLOW_or_in_implication2563);
            	    op2=or();

            	    state._fsp--;


            	    retval.expr = new ExprTree(retval.expr, op2, "->", 'l');

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
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "implication"



    // $ANTLR start "expression"
    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1248:1: expression returns [ExprTree expr] : op1= implication ( '?' op2= expression ':' op3= expression )? ;
    public final ExprTree expression() throws RecognitionException {
        ExprTree expr = null;


        PlatuGrammarParser.implication_return op1 =null;

        ExprTree op2 =null;

        ExprTree op3 =null;


        try {
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1249:5: (op1= implication ( '?' op2= expression ':' op3= expression )? )
            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1249:9: op1= implication ( '?' op2= expression ':' op3= expression )?
            {
            pushFollow(FOLLOW_implication_in_expression2604);
            op1=implication();

            state._fsp--;


            expr = (op1!=null?op1.expr:null);

            // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1250:6: ( '?' op2= expression ':' op3= expression )?
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==QMARK) ) {
                alt51=1;
            }
            switch (alt51) {
                case 1 :
                    // /Users/zhangz/myBioSim/BioSim/gui/src/verification/platu/lpn/io/PlatuGrammar.g:1250:7: '?' op2= expression ':' op3= expression
                    {
                    match(input,QMARK,FOLLOW_QMARK_in_expression2614); 

                    pushFollow(FOLLOW_expression_in_expression2618);
                    op2=expression();

                    state._fsp--;


                    match(input,COLON,FOLLOW_COLON_in_expression2620); 

                    pushFollow(FOLLOW_expression_in_expression2624);
                    op3=expression();

                    state._fsp--;



                        			//expr = new TernaryNode(expr, op2, op3);
                        			// op1?op2:op3 == int(op1)*op2+int(~op1)*op3
                        			//expr = new ExprTree(expr);
                        			expr = new ExprTree(new ExprTree(new ExprTree("int("+(op1!=null?input.toString(op1.start,op1.stop):null) + ")"), op2, "*", 'a'), 
                        			                     new ExprTree(new ExprTree("int(~("+(op1!=null?input.toString(op1.start,op1.stop):null) + "))"), op3, "*", 'a'), 
                        			                     "+", 'a');
                        		

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
        	// do for sure before leaving
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
        "\1\34\1\32\2\23\1\uffff\2\25\1\20\1\17\1\20\1\17\1\31\1\74\1\25"+
        "\1\32\1\61\1\23\2\61\1\23\1\25\1\34\1\25\1\34\1\32\1\43\4\uffff";
    static final String DFA1_maxS =
        "\1\34\1\74\2\23\1\uffff\2\34\1\20\1\17\1\20\1\17\1\31\1\74\1\31"+
        "\1\32\1\61\1\23\2\61\1\23\4\34\1\43\1\74\4\uffff";
    static final String DFA1_acceptS =
        "\4\uffff\1\5\25\uffff\1\1\1\4\1\2\1\3";
    static final String DFA1_specialS =
        "\36\uffff}>";
    static final String[] DFA1_transitionS = {
            "\1\1",
            "\1\3\10\uffff\1\4\30\uffff\1\2",
            "\1\5",
            "\1\6",
            "",
            "\1\7\6\uffff\1\10",
            "\1\11\6\uffff\1\12",
            "\1\13",
            "\1\14",
            "\1\15",
            "\1\16",
            "\1\17",
            "\1\20",
            "\1\22\3\uffff\1\21",
            "\1\23",
            "\1\24",
            "\1\25",
            "\1\26",
            "\1\26",
            "\1\27",
            "\1\7\6\uffff\1\10",
            "\1\30",
            "\1\11\6\uffff\1\12",
            "\1\31",
            "\1\32\10\uffff\1\33",
            "\1\35\30\uffff\1\34",
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
            return "122:9: ( ( globalConstants globalVariables ) | ( globalVariables globalConstants ) | ( globalVariables ) | ( globalConstants ) )?";
        }
    }
 

    public static final BitSet FOLLOW_globalConstants_in_lpn77 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_globalVariables_in_lpn79 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_globalVariables_in_lpn85 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_globalConstants_in_lpn87 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_globalVariables_in_lpn93 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_globalConstants_in_lpn99 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_module_in_lpn124 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_main_in_lpn151 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_EOF_in_lpn176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_main195 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_MODULE_in_main197 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_NAME_in_main199 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_main201 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_main203 = new BitSet(new long[]{0x8000000000000000L});
    public static final BitSet FOLLOW_63_in_main205 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_main207 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_main209 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_instantiation_in_main218 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_LESS_in_main221 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_main223 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_MODULE_in_main225 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_main227 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_module253 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_MODULE_in_module255 = new BitSet(new long[]{0x0000002000000000L});
    public static final BitSet FOLLOW_NAME_in_module257 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_module259 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_module261 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ID_in_module263 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_module279 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_module281 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_constants_in_module283 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_variables_in_module286 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_instantiation_in_module288 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_logic_in_module291 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_LESS_in_module294 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_module296 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_MODULE_in_module298 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_module300 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_constants327 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_60_in_constants329 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_constants331 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_ID_in_constants336 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_constants338 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_INT_in_constants342 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_constants353 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_LESS_in_constants357 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_constants359 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_60_in_constants361 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_constants363 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalConstants379 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_60_in_globalConstants381 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants383 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_ID_in_globalConstants388 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_globalConstants390 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_INT_in_globalConstants394 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalConstants420 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_LESS_in_globalConstants424 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_globalConstants426 = new BitSet(new long[]{0x1000000000000000L});
    public static final BitSet FOLLOW_60_in_globalConstants428 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_globalConstants430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_globalVariables444 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_INTERNAL_in_globalVariables446 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables448 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_ID_in_globalVariables453 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_globalVariables463 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_INT_in_globalVariables468 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_globalVariables482 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_globalVariables492 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_LESS_in_globalVariables496 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_globalVariables498 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_INTERNAL_in_globalVariables500 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_globalVariables502 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_variables518 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_INTERNAL_in_variables520 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_variables522 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_ID_in_variables528 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_variables538 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_INT_in_variables543 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_ID_in_variables557 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_variables567 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_ID_in_variables586 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_57_in_variables596 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_ID_in_variables601 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_INT_in_variables615 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_58_in_variables626 = new BitSet(new long[]{0x0200000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_variables631 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_LPAREN_in_variables642 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_INT_in_variables647 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_COMMA_in_variables649 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_INT_in_variables663 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_variables673 = new BitSet(new long[]{0x0002000080000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_variables680 = new BitSet(new long[]{0x0000000010200000L});
    public static final BitSet FOLLOW_LESS_in_variables694 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_variables696 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_INTERNAL_in_variables698 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_variables700 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_instantiation729 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_62_in_instantiation731 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_instantiation733 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ID_in_instantiation742 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ID_in_instantiation746 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_LPAREN_in_instantiation748 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ID_in_instantiation752 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_PERIOD_in_instantiation754 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ID_in_instantiation758 = new BitSet(new long[]{0x0001000000200000L});
    public static final BitSet FOLLOW_RPAREN_in_instantiation762 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_LESS_in_instantiation769 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_instantiation771 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_62_in_instantiation773 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_instantiation775 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_marking_in_logic805 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_transition_in_logic808 = new BitSet(new long[]{0x0000000010000002L});
    public static final BitSet FOLLOW_LESS_in_marking862 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_MARKING_in_marking864 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_marking866 = new BitSet(new long[]{0x0000000012200000L});
    public static final BitSet FOLLOW_INT_in_marking872 = new BitSet(new long[]{0x0000000010001000L});
    public static final BitSet FOLLOW_ID_in_marking898 = new BitSet(new long[]{0x0000000010001000L});
    public static final BitSet FOLLOW_COMMA_in_marking922 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_INT_in_marking927 = new BitSet(new long[]{0x0000000010001000L});
    public static final BitSet FOLLOW_ID_in_marking953 = new BitSet(new long[]{0x0000000010001000L});
    public static final BitSet FOLLOW_LESS_in_marking980 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_marking982 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_MARKING_in_marking984 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_marking986 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESS_in_transition1020 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_TRANSITION_in_transition1022 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LABEL_in_transition1024 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1026 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1028 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_set_in_transition1032 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1038 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_PRESET_in_transition1040 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1042 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1045 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1047 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1052 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_INT_in_transition1057 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_ID_in_transition1077 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_COMMA_in_transition1093 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_INT_in_transition1097 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_COMMA_in_transition1115 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ID_in_transition1119 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1134 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_POSTSET_in_transition1138 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_transition1140 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1144 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1146 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1151 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_INT_in_transition1157 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_ID_in_transition1177 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_COMMA_in_transition1202 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_INT_in_transition1206 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_COMMA_in_transition1226 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_ID_in_transition1229 = new BitSet(new long[]{0x0000800000001000L});
    public static final BitSet FOLLOW_QUOTE_in_transition1248 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_transition1253 = new BitSet(new long[]{0x0820084292240080L});
    public static final BitSet FOLLOW_guard_in_transition1256 = new BitSet(new long[]{0x0800000090200000L});
    public static final BitSet FOLLOW_delay_in_transition1276 = new BitSet(new long[]{0x0800000010200000L});
    public static final BitSet FOLLOW_assertion_in_transition1297 = new BitSet(new long[]{0x0800000010200000L});
    public static final BitSet FOLLOW_assignment_in_transition1317 = new BitSet(new long[]{0x0800000010200000L});
    public static final BitSet FOLLOW_LESS_in_transition1336 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_DIV_in_transition1338 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_TRANSITION_in_transition1340 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_GREATER_in_transition1342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_59_in_assertion1377 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_LPAREN_in_assertion1379 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_expression_in_assertion1381 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_assertion1383 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assertion1385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_expression_in_guard1411 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_guard1413 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_delay1447 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_INT_in_delay1451 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_COMMA_in_delay1465 = new BitSet(new long[]{0x2000000002000000L});
    public static final BitSet FOLLOW_INT_in_delay1470 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_61_in_delay1485 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_delay1498 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_delay1500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1526 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1528 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_expression_in_assignment1544 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_assignment1575 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_57_in_assignment1592 = new BitSet(new long[]{0x0000000002200000L});
    public static final BitSet FOLLOW_set_in_assignment1594 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_58_in_assignment1602 = new BitSet(new long[]{0x0200000000010000L});
    public static final BitSet FOLLOW_EQUALS_in_assignment1606 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_expression_in_assignment1608 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_SEMICOLON_in_assignment1610 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_term1654 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_term1672 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_expression_in_term1674 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_RPAREN_in_term1676 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_term1688 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_term1701 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_term1713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_unary1754 = new BitSet(new long[]{0x0020080282240000L});
    public static final BitSet FOLLOW_MINUS_in_unary1759 = new BitSet(new long[]{0x0020080282240000L});
    public static final BitSet FOLLOW_term_in_unary1766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BITWISE_NEGATION_in_bitwiseNegation1798 = new BitSet(new long[]{0x0020080282240080L});
    public static final BitSet FOLLOW_unary_in_bitwiseNegation1804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEGATION_in_negation1830 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_bitwiseNegation_in_negation1836 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_negation_in_mult1865 = new BitSet(new long[]{0x0008000000008002L});
    public static final BitSet FOLLOW_TIMES_in_mult1877 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_negation_in_mult1881 = new BitSet(new long[]{0x0008000000008002L});
    public static final BitSet FOLLOW_DIV_in_mult1892 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_negation_in_mult1896 = new BitSet(new long[]{0x0008000000008002L});
    public static final BitSet FOLLOW_mult_in_add1948 = new BitSet(new long[]{0x0000080200000002L});
    public static final BitSet FOLLOW_PLUS_in_add1959 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_mult_in_add1963 = new BitSet(new long[]{0x0000080200000002L});
    public static final BitSet FOLLOW_MINUS_in_add1975 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_mult_in_add1979 = new BitSet(new long[]{0x0000080200000002L});
    public static final BitSet FOLLOW_add_in_shift2018 = new BitSet(new long[]{0x0000000000000242L});
    public static final BitSet FOLLOW_BITWISE_LSHIFT_in_shift2030 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_add_in_shift2034 = new BitSet(new long[]{0x0000000000000242L});
    public static final BitSet FOLLOW_BITWISE_RSHIFT_in_shift2048 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_add_in_shift2052 = new BitSet(new long[]{0x0000000000000242L});
    public static final BitSet FOLLOW_shift_in_relation2091 = new BitSet(new long[]{0x0000000030180002L});
    public static final BitSet FOLLOW_LESS_in_relation2102 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_shift_in_relation2106 = new BitSet(new long[]{0x0000000030180002L});
    public static final BitSet FOLLOW_LESS_EQUAL_in_relation2118 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_shift_in_relation2122 = new BitSet(new long[]{0x0000000030180002L});
    public static final BitSet FOLLOW_GREATER_EQUAL_in_relation2134 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_shift_in_relation2138 = new BitSet(new long[]{0x0000000030180002L});
    public static final BitSet FOLLOW_GREATER_in_relation2150 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_shift_in_relation2154 = new BitSet(new long[]{0x0000000030180002L});
    public static final BitSet FOLLOW_relation_in_equivalence2194 = new BitSet(new long[]{0x0000008000020002L});
    public static final BitSet FOLLOW_EQUIV_in_equivalence2205 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_relation_in_equivalence2209 = new BitSet(new long[]{0x0000008000020002L});
    public static final BitSet FOLLOW_NOT_EQUIV_in_equivalence2226 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_relation_in_equivalence2230 = new BitSet(new long[]{0x0000008000020002L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2269 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_BITWISE_AND_in_bitwiseAnd2281 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_equivalence_in_bitwiseAnd2285 = new BitSet(new long[]{0x0000000000000022L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2324 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_BITWISE_XOR_in_bitwiseXor2335 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_bitwiseAnd_in_bitwiseXor2339 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2378 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_BITWISE_OR_in_bitwiseOr2389 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_bitwiseXor_in_bitwiseOr2393 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2433 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_AND_in_and2444 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_bitwiseOr_in_and2448 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_and_in_or2493 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_OR_in_or2504 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_and_in_or2508 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_or_in_implication2548 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_IMPLICATION_in_implication2559 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_or_in_implication2563 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_implication_in_expression2604 = new BitSet(new long[]{0x0000400000000002L});
    public static final BitSet FOLLOW_QMARK_in_expression2614 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_expression_in_expression2618 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_COLON_in_expression2620 = new BitSet(new long[]{0x0020084282240080L});
    public static final BitSet FOLLOW_expression_in_expression2624 = new BitSet(new long[]{0x0000000000000002L});

}