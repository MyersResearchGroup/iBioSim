grammar PlatuGrammar;

options {
  language = Java;
}

@header{
    package platu.lpn.io;
    
    import java.util.Map.Entry;
    import java.util.HashMap;
    import java.util.LinkedList;
    import java.util.HashSet;
    import java.util.Set;
    import java.util.Arrays;
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
}

@members{
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
}

@rulecatch{
    catch (RecognitionException e){
    	System.err.println(e.input.getSourceName() + ":");
    	System.err.println("---> Mismatched token '" + e.token.getText() + "' on line " + e.line);
    	System.err.println();
    	System.exit(1);
    }
}

@lexer::header{
    package platu.lpn.io;
}

parseLpnFile[Project prj, boolean instance, HashMap<String, String> portMap] returns [Set<LPN> lpnSet]
	:	
	;
	
lpn[Project prj] returns [Set<LPN> lpnSet]
    :   {$lpnSet = new HashSet<LPN>();}
        ((globalConstants globalVariables) | (globalVariables globalConstants) | (globalVariables) | (globalConstants))?
        	{
        		// check that global constants are consistently defined in each lpn file
        		if(GlobalSize > 0 && GlobalCount != GlobalSize){
        			System.err.println("error: global variable definitions are inconsistent");
        			System.exit(1);
        		}
        	} 
      	(module[prj]
            {
            	$lpnSet.add($module.lpn);
            }
        | main[prj]
        	{
        		
        	}
        )+ EOF
    ;
    
main[Project prj]
	:	'<' 'mod' 'name' '=' '"' 'main' '"' '>'
			{
				if(main == true){
					System.err.println("error");
					System.exit(1);
				}
				
				main = true;
			}
		instantiation+ '<' '/' 'mod' '>'
	;
    
// TODO: don't enforce order
module[Project prj] returns [LPN lpn]
    :	( '<' 'mod' 'name' '=' '"' ID
    		{
    			// module names must be unique
    			if(LpnMap.containsKey($ID.text)){
    				System.err.println("error on line " + $ID.getLine() + ": module " + $ID.text + " already exists");
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
    		} 
    	'"' '>' constants? variables instantiation? logic? '<' '/' 'mod' '>'
			{
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
	            int[] initialMarking = new int[$logic.initMarking.size()];
	            for(Integer mark : $logic.initMarking){
	            	initialMarking[i++] = mark;
	            }

				$lpn = new LPN(prj, $ID.text, Inputs, Outputs, Internals, VarNodeMap, $logic.lpnTranSet, 
	         			StatevectorMap, initialMarking);
				
				for(LPNTran tran : inputTranList){
					tran.addDstLpn($lpn);
				}
				
				$lpn.addAllInputTrans(inputTranList);
				$lpn.addAllOutputTrans(outputTranList);
	            $lpn.setVarIndexMap(VarIndexMap);         
	            $logic.lpnTranSet.setLPN($lpn);     
	            prj.getDesignUnitSet().add($lpn.getStateGraph());
	            
	            LpnMap.put($lpn.getLabel(), $lpn);
	            
	            // map outputs to lpn object
	            for(String output : Outputs){
	            	GlobalOutputMap.put(output, $lpn);
	            }
	            
	            // map potential output to lpn object
	            for(String internal : Internals){
	            	GlobalOutputMap.put(internal, $lpn);
	            }
	            
	            // map input variable to lpn object
	            for(String input : Inputs){
	            	if(GlobalInputMap.containsKey(input)){
	    				GlobalInputMap.get(input).add($lpn);
	    			}
	    			else{
	    				List<LPN> lpnList = new ArrayList<LPN>();
	    				lpnList.add($lpn);
	    				GlobalInputMap.put(input, lpnList);
	    			}
	            }
			}
		)
    ;
    
constants
	:	'<' 'const' '>' (const1=ID '=' val1=INT 
			{
				// make sure constant is not defined as something else
				if(StatevectorMap.containsKey($const1.text)){
					System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " already exists as a variable"); 
					System.exit(1);
				}
				else if(GlobalConstHashMap.containsKey($const1.text)){
				    System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " already exists as a global constant");
				    System.exit(1);
				}
				else if(GlobalVarHashMap.containsKey($const1.text)){
            		System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " is already defined as a global variable");
            		System.exit(1);
            	}
				
				// put will override previous value
				Integer result = ConstHashMap.put($const1.text, Integer.parseInt($val1.text));
				if(result != null){
					System.err.println("warning on line " + $const1.getLine() + ": " + $const1.text + " will be overwritten");
				}
			}
		 ';')* '<' '/' 'const' '>'
	;
	
globalConstants
    :   '<' 'const' '>' (const1=ID '=' val1=INT 
            {
            	// make sure constant has not been defined already
            	if(GlobalVarHashMap.containsKey($const1.text)){
            		System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " is already defined as a global variable");
            		System.exit(1);
            	}
            	
            	// put will override previous value
                Integer result = GlobalConstHashMap.put($const1.text, Integer.parseInt($val1.text));
                if(result != null){
                    System.err.println("warning on line " + $const1.getLine() + ": " + $const1.text + " will be overwritten");
                }
            }
         ';')* '<' '/' 'const' '>'
    ;

//TODO: add global variables to state vector
globalVariables
	:	'<' 'var' '>' (var=ID 
			{
				// make sure global variables are consistently defined in each lpn file
				if(GlobalSize == 0){
					if(GlobalConstHashMap.containsKey($var.text)){
						System.err.println("error on line" + $var.getLine() + ": " + $var.text + "already exists as a constant"); 
	                    System.exit(1);
					}
					else if(GlobalVarHashMap.containsKey($var.text)){
						System.err.println("error on line " + $var.getLine() + ": " + $var.text + " has already been defined");
						System.exit(1);
					}
				}
				else{
					if(!GlobalVarHashMap.containsKey($var.text)){
						System.err.println("error on line " + $var.getLine() + ": " + $var.text + " is inconsistently defined");
						System.exit(1);
					}
				}
				
				GlobalCount++;
			}
		'=' (val=INT 
			{
				// make sure global variables are consistently initialized
				int value = Integer.parseInt($val.text);
				if(GlobalSize == 0){
					GlobalVarHashMap.put($var.text, value);
				}
				else{
					int globalVal = GlobalVarHashMap.get($var.text);
					if(globalVal != value){
						System.err.println("error on line " + $val.getLine() + ": " + $var.text + " is inconsistently assigned");
						System.exit(1);
					}
				}
			}
		| var2=ID
			{
				// get value of variable
				Integer value = null;
				if(GlobalConstHashMap.containsKey($var2.text)){
					value = GlobalConstHashMap.get($var2.text);
				}
				else if(GlobalVarHashMap.containsKey($var2.text)){
					System.err.println("error on line " + $var2.getLine() + ": global variable " + $var2.text + " cannot be assigned to global variable " + $var.text); 
    				System.exit(1);
				}
				else{
					System.err.println("error on line " + $var2.getLine() + ": " + $var2.text + " is not defined"); 
    				System.exit(1);
				}
				
				// make sure global variable is consistently initialized
				if(GlobalSize == 0){
					GlobalVarHashMap.put($var.text, value);
				}
				else{
					int globalVal = GlobalVarHashMap.get($var.text);
					if(globalVal != value){
						System.err.println("error on line " + $val.getLine() + ": " + $var.text + " is inconsistently assigned");
						System.exit(1);
					}
				}
				
				
			}
		)';')* '<' '/' 'var' '>'
	;

//TODO: add globals
variables
	:	{Integer value = null; Token varNode = null;}
		'<' 'var' '>' ((var=ID 
			{
				// check variable is unique in scope
				if(GlobalConstHashMap.containsKey($var.text)){
					System.err.println("error on line " + $var.getLine() + ": " + $var.text + " is a global constant"); 
    				System.exit(1);
				}
				else if(GlobalVarHashMap.containsKey($var.text)){
					System.err.println("error on line " + $var.getLine() + ": " + $var.text + " is a global variable"); 
    				System.exit(1);
				}
				else if(StatevectorMap.containsKey($var.text)){
					System.err.println("warning on line " + $var.getLine() + ": " + $var.text + " will be overwritten");
				}
				
				varNode = var;
			}
		'=' (val=INT 
			{
				// get variable initial value
				value = Integer.parseInt($val.text);
			}
		| var2=ID
			{
				// get variable initial value
				if(GlobalConstHashMap.containsKey($var2.text)){
					value = GlobalConstHashMap.get($var2.text);
				}
				else if(GlobalVarHashMap.containsKey($var2.text)){
					value = GlobalVarHashMap.get($var2.text);
				}
				else if(ConstHashMap.containsKey($var2.text)){
					value = ConstHashMap.get($var2.text);
				}
				else if(StatevectorMap.containsKey($var2.text)){ // Should var be allowed to assign a var?
					value = StatevectorMap.get($var2.text);
				}
				else{
					System.err.println("error on line " + $var2.getLine() + ": " + $var2.text + " is not defined"); 
    				System.exit(1);
				}
				
				varNode = var2;
			}
		)';')
			{
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
		| (arrayName=ID
			{
				List<Integer> dimensionsList = new ArrayList<Integer>();
			}
		('[' (dim=ID
			{
				// get variable value
				if(GlobalConstHashMap.containsKey($dim.text)){
					value = GlobalConstHashMap.get($dim.text);
				}
				else if(GlobalVarHashMap.containsKey($dim.text)){
					value = GlobalVarHashMap.get($dim.text);
				}
				else if(ConstHashMap.containsKey($dim.text)){
					value = ConstHashMap.get($dim.text);
				}
				else if(StatevectorMap.containsKey($dim.text)){ // Should var be allowed to assign a var?
					value = StatevectorMap.get($dim.text);
				}
				else{
					System.err.println("error on line " + $dim.getLine() + ": " + $dim.text + " is not defined"); 
    				System.exit(1);
				}
				
				dimensionsList.add(value);
			} 
		| val2=INT
			{
				dimensionsList.add(Integer.parseInt($val2.text));
			}
		) ']' )+ '=' 
			{
				List<Integer> valueList = new ArrayList<Integer>();
			}
		('(' (val3=INT ','
			{
				valueList.add(Integer.parseInt($val3.text));
			}
		)* val4=INT 
			{
				valueList.add(Integer.parseInt($val4.text));
			}
		')' )+   ';' 
			{
				if(valueList.size() != dimensionsList.get(0)){
					System.err.println("error: incompatible number of elements in " + $arrayName.text);
					System.exit(1);
				}
				
				if(dimensionsList.size() == 1){
					int varCount = 0;
					int arraySize = dimensionsList.get(0);
					List<Object> array = new ArrayList<Object>(arraySize);
					for(int i = 0; i < arraySize; i++){
						String name = $arrayName.text + varCount++;
						int index = VariableIndex++;
						VarNode v = new VarNode(name, index);
						array.add(v);
						
						// add variable and value to state vector
						StatevectorMap.put(name, 0);
						
						// generate variable index and create new var node  
		   				VarIndexMap.insert(name, index);
					}
					
//					ArrayNodeMap.put($arrayName.text, new ArrayNode($arrayName.text, array, 1));
					VarCountMap.put($arrayName.text, 0);
				}
				else{
				}
			}
		))* '<' '/' 'var' '>'
			{
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
	;

// TODO: implement instantiation
instantiation
    :   {HashMap<String, String> portMap = new HashMap<String, String>();}
    	'<' 'inst' '>'
    	modName=ID instName=ID '('(mod=ID '.' var=ID)+ ')'
    	'<' '/' 'inst' '>'
    ;

logic returns [List<Integer> initMarking, LpnTranList lpnTranSet]
    :   {$lpnTranSet = new LpnTranList();}
    	marking (transition {$lpnTranSet.add($transition.lpnTran);})+
        {
            $initMarking = $marking.mark;
        }
    ; 
    
marking returns [List mark]
    :   {$mark = new LinkedList<Integer>(); Integer result;}
        ('<' 'marking' '>' ((m1=INT
        	{
        		$mark.add(Integer.parseInt($m1.text));
        	} 
        | c1=ID
        	{
        		result = ConstHashMap.get($c1.text);
        		if(result == null){
        			System.err.println("error on line " + $c1.getLine() + ": " + $c1.text + " is not a valid constant");
        			System.exit(1);
        		}
        		
        		$mark.add(result);
        	}
        ) (',' (m2=INT 
        	{
        		$mark.add(Integer.parseInt($m2.text));
        	}
        | c2=ID
        	{
        		result = ConstHashMap.get($c2.text);
        		if(result == null){
        			System.err.println("error on line " + $c2.getLine() + ": " + $c2.text + " is not a valid constant");
        			System.exit(1);
        		}
        		
        		$mark.add(result);
        	}
       	))*)? '<' '/' 'marking' '>')?
    ;
 
transition returns [LPNTran lpnTran]
    :   	{
	    		Integer result = null;
	    		ArrayList presetList = new ArrayList();  
	    		ArrayList postsetList = new ArrayList(); 
	    		VarExprList assignmentList = new VarExprList();
	    		ArrayList<Expression> assertionList = new ArrayList<Expression>();
	    		Expression guardExpr = TrueExpr; 
	    		int delayLB = 0; 
	    		int delayUB = INFINITY;
	    		boolean local = true;
	    	}
    	'<' 'transition' 'label' '=' '"' lbl=(ID|INT) '"' 'preset' '=' ('"' '"' | ('"' (pre=INT 
    		{
    			presetList.add(Integer.parseInt($pre.text));
   			} 
  		| pre1=ID
  			{
  				result = ConstHashMap.get($pre1.text); 
  				if(result == null){
  					System.err.println("error on line " + $pre1.getLine() + ": " + $pre1.text + " is not a constant");
  					System.exit(1);
  				}
  				
  				presetList.add(result);
  			}
 		) ( ',' pre2=INT 
    		{
    			presetList.add(Integer.parseInt($pre2.text));
   			} 
  		| ',' pre3=ID
  			{
  				result = ConstHashMap.get($pre3.text); 
  				if(result == null){
  					System.err.println("error on line " + $pre3.getLine() + ": " + $pre3.text + " is not a constant");
  					System.exit(1);
  				}
  				
  				presetList.add(result);
  			}
 		)* '"')) 'postset' '=' ( '"' '"' | ('"' ( post=INT
    		{
    			postsetList.add(Integer.parseInt($post.text));
    		} 
    	| post1=ID
    		{
    			result = ConstHashMap.get($post1.text); 
  				if(result == null){
  					System.err.println("error on line " + $post1.getLine() + ": " + $post1.text + " is not a constant");
  					System.exit(1);
  				}
  				
  				postsetList.add(result);
    		}
    	)
    	( (',' post2=INT)
    		{
    			postsetList.add(Integer.parseInt($post2.text));
    		} 
    	| (','post3=ID)
    		{
    			result = ConstHashMap.get($post3.text); 
  				if(result == null){
  					System.err.println("error on line " + $post3.getLine() + ": " + $post3.text + " is not a constant");
  					System.exit(1);
  				}
  				
  				postsetList.add(result);
    		}
    	)* '"' )) '>' (guard 
    		{
    			guardExpr = $guard.expr;
    		}
    	)? (delay 
    		{
    			delayLB = $delay.delayLB; 
    			delayUB = $delay.delayUB;
    		}
    	)? ((assertion
    		{
    			if($assertion.booleanExpr != null){		
  					assertionList.add($assertion.booleanExpr);
  				}
    		}
    	) | (assignment
    		{
    			assignmentList.add($assignment.assign);
    		}
    	))* '<' '/' 'transition' '>'
        {
        	// create new lpn transitions and add assertions
        	$lpnTran = new LPNTran($lbl.text, TransitionIndex++, presetList, postsetList, guardExpr, assignmentList, delayLB, delayUB, local);
        	if(assertionList.size() > 0){
        		$lpnTran.addAllAssertions(assertionList);
        	}
        	
        	// add non-local transition to associated LPNs
        	for(VarExpr e : assignmentList){
        		VarNode var = e.getVar();
        		if(Outputs.contains(var.getName())){
        			local = false;
        			
        			if(GlobalInputMap.containsKey(var.getName())){
	        			for(LPN lpn : GlobalInputMap.get(var.getName())){
	        				lpn.addInputTran($lpnTran);
	        				$lpnTran.addDstLpn(lpn);
	        			}
        			}
        		}
        		
        		// map lpn transition with output and potential outuput variables
        		if(GlobalTranMap.containsKey(var.getName())){
       				GlobalTranMap.get(var.getName()).add($lpnTran);
       			}
       			else{
       				List<LPNTran> tranList = new ArrayList<LPNTran>();
       				tranList.add($lpnTran);
       				GlobalTranMap.put(var.getName(), tranList);
       			}
        	}
        	
       		$lpnTran.setLocalFlag(local);
       		if(local == false){
       			outputTranList.add($lpnTran);
       		}
        }
    ;
  
assertion returns [Expression booleanExpr]
	:	{booleanExpr = null;}
		'assert' '(' expression ')' ';'
			{
				$booleanExpr = new Expression($expression.expr);
			}
	;
	
guard returns [Expression expr]
    :   expression ';'
    		{
   				$expr = new Expression($expression.expr);
    		}
    ;
    
delay returns [int delayLB, int delayUB]
    : 	'(' lb=INT
    		{
    			$delayLB = Integer.parseInt($lb.text);
   			} 
 		',' (ub=INT 
 			{
 				$delayUB = Integer.parseInt($ub.text);
 				// make sure delays are >= 0 and upper bound is >= lower bound
 				if($delayLB < 0){
 					System.err.println("error on line " + $lb.getLine() + ": lower bound " + $delayLB + " must be >= 0");
  					System.exit(1);
 				}
 				else if($delayLB == INFINITY){
 					System.err.println("error on line " + $ub.getLine() + ": lower bound " + $delayUB + " must be a non-negative finite number");
  					System.exit(1);
 				}
 				else if($delayUB < $delayLB){
 					System.err.println("error on line " + $ub.getLine() + ": upper bound " + $delayUB + " < lower bound " + $delayLB);
  					System.exit(1);
 				} 
 			} 
 		| 'inf' 
 			{
 				$delayUB = INFINITY;
			}
		) ')' ';'
    ;

assignment returns [VarExpr assign]
    :   (var=ID '=' 
    		{	
    			// make sure only global, internal and output variables are assigned
    			if(GlobalConstHashMap.containsKey($var.text)){
    				System.err.println("error on line " + $var.getLine() + ": global constant " + $var.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(ConstHashMap.containsKey($var.text)){
    				System.err.println("error on line " + $var.getLine() + ": constant " + $var.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(!Outputs.contains($var.text) && !Internals.contains($var.text)){
    				System.err.println("error on line " + $var.getLine() + ": input variable " + $var.text + " cannot be assigned");
    				System.exit(1);
    			}
    		}
    	expression ';'
	    	{
	    		Integer varCount = VarCountMap.get($var.text);
	    		if(varCount != null){
	    			VarCountMap.put($var.text, ++varCount);
	    		}
	    		
	    		Expression expr = new Expression($expression.expr);
	    		$assign = new VarExpr(VarNodeMap.get($var.text), expr);
	   		}
	   	) | (var2=ID 
	   		{
	   			List<Integer> indexList = new ArrayList<Integer>();
	   			
	   		}
	   	('[' (INT | ID) ']')+ '=' expression ';'
	   		{
	   		
	   		}
	   	)
    ;
    
// Expressions
term returns [ExpressionNode expr]
    :   ID
    		{
    			if(ConstHashMap.containsKey($ID.text)){
    				$expr = new ConstNode($ID.text, ConstHashMap.get($ID.text));
    			}
    			else if(GlobalConstHashMap.containsKey($ID.text)){
    			 $expr = new ConstNode($ID.text, GlobalConstHashMap.get($ID.text));
    			}
    			else if(StatevectorMap.containsKey($ID.text)){ 
//    				$expr = new platu.lpn.io.expression.VarNode($ID.text);
					$expr = VarNodeMap.get($ID.text);
    			}
    			else{ // identify new input variable
//    				// create expression
//					$expr = new platu.lpn.io.expression.VarNode($ID.text);
					
					// label as input and initialize to 0
					StatevectorMap.put($ID.text, 0);
					Inputs.add($ID.text);
					
					// generate a varaible index and create new variable object
					int index = VariableIndex++;
	    			VarIndexMap.insert($ID.text, index);
	    			VarNode newVarNode = new VarNode($ID.text, index);
	    			VarNodeMap.put($ID.text, newVarNode);
	    			$expr = newVarNode;
	    			
	    			// if associated output variable has not been defined insert with null value,
	    			// otherwise get output variable and relabel from internal to output, 
	    			// get output value and initialize input statevector, label lpn transitions associated with output as non-local
	    			// and add to current lpn's inputTranList
	    			if(!GlobalInterfaceMap.containsKey($ID.text)){
	    				GlobalInterfaceMap.put($ID.text, null);
	    			}
	    			else{
	    				Integer value = GlobalInterfaceMap.get($ID.text);
	    				if(value != null){
	    					StatevectorMap.put($ID.text, value);
	    					
	    					LPN outputLPN = GlobalOutputMap.get($ID.text);
	    					
	    					VarSet internals = outputLPN.getInternals();
	    					if(internals.contains($ID.text)){
	    						internals.remove($ID.text);
	    						outputLPN.getOutputs().add($ID.text);
	    					}
	    					
	    					
	    					for(LPNTran tran : GlobalTranMap.get($ID.text)){
	    						tran.setLocalFlag(false);
	    						outputLPN.addOutputTran(tran);
	    						
	    						inputTranList.add(tran);
	    					}
	    				}
	    			}
    			}
   			}
    |   LPAREN expression RPAREN {$expr = $expression.expr;}
    |   INT {$expr = new ConstNode("name", Integer.parseInt($INT.text));}
    |   TRUE {$expr = ONE;}
    |   FALSE {$expr = ZERO;}
    ;
    
unary returns [ExpressionNode expr]
    :   {boolean positive = true;}
    	('+' | ('-' {if(positive){ positive = false;} else {positive = true;}}))* term
    	{
    		if(!positive){
    			$expr = new MinNode($term.expr);
    		}
    		else{
    			$expr = $term.expr;
   			}
    	}
    ;

bitwiseNegation returns [ExpressionNode expr]
	: 	{boolean neg = false;}
		('~' {if(neg){neg = false;} else{neg = true;}})* unary
			{
				if(neg){
					$expr = new BitNegNode($unary.expr);
				}
				else{
					$expr = $unary.expr;
				}
			}
	;
	
negation returns [ExpressionNode expr]
	:	{boolean neg = false;}
		('!' {if(neg){neg = false;} else{neg = true;}})* bitwiseNegation
			{
				if(neg){
					$expr = new NegNode($bitwiseNegation.expr);
				}
				else{
					$expr = $bitwiseNegation.expr;
				}
			}
	;
	
mult returns [ExpressionNode expr]
    :   op1=negation {$expr = $op1.expr;} 
    	(	'*' op2=negation {$expr = new MultNode($expr, $op2.expr);}
    	|	'/' op2=negation {$expr = new DivNode($expr, $op2.expr);}
    	|	'%' op2=negation {$expr = new ModNode($expr, $op2.expr);}
    	)*
    ;
    
add returns [ExpressionNode expr]
    :   op1=mult {$expr = $op1.expr;}
    	(	'+' op2=mult {$expr = new AddNode($expr, $op2.expr);}
    	| 	'-' op2=mult {$expr = new SubNode($expr, $op2.expr);}
    	)*
    ;
    
shift returns [ExpressionNode expr]
    :   op1=add {$expr = $op1.expr;}
    	(	'<<' op2=add {$expr = new LeftShiftNode($expr, $op2.expr);}
    	| 	'>>' op2=add {$expr = new RightShiftNode($expr, $op2.expr);}
    	)*
    ;

relation returns [ExpressionNode expr]
    :   op1=shift {$expr = $op1.expr;}
    	(	'<' op2=shift {$expr = new LessNode($expr, $op2.expr);}
    	| 	'<=' op2=shift {$expr = new LessEqualNode($expr, $op2.expr);}
    	| 	'>=' op2=shift {$expr = new GreatEqualNode($expr, $op2.expr);}
    	| 	'>' op2=shift {$expr = new GreatNode($expr, $op2.expr);}
    	)*
    ;
    
equivalence returns [ExpressionNode expr]
    :   op1=relation {$expr = $op1.expr;}
    	(	'==' op2=relation {$expr = new EquivNode($expr, $op2.expr);}
    	|	'!=' op2=relation {$expr = new NotEquivNode($expr, $op2.expr);}
    	)*
    ;
    
bitwiseAnd returns [ExpressionNode expr]
    :   op1=equivalence {$expr = $op1.expr;} 
    	(	'&' op2=equivalence {$expr = new BitAndNode($expr, $op2.expr);}
    	)*
    ;
    
bitwiseXor returns [ExpressionNode expr]
    :   op1=bitwiseAnd {$expr = $op1.expr;}
    	(	'^' op2=bitwiseAnd {$expr = new BitXorNode($expr, $op2.expr);}
    	)*
    ;
    
bitwiseOr returns [ExpressionNode expr]
    :   op1=bitwiseXor {$expr = $op1.expr;}
    	(	'|' op2=bitwiseXor {$expr = new BitOrNode($expr, $op2.expr);}
    	)*
    ;
    
and returns [ExpressionNode expr]
    :   op1=bitwiseOr {$expr = $op1.expr;}
    	(	'&&' op2=bitwiseOr {$expr = new AndNode($expr, $op2.expr);}
    	)*
    ;
    
or returns [ExpressionNode expr]
    :   op1=and {$expr = $op1.expr;}
    	(	'||' op2=and {$expr = new OrNode($expr, $op2.expr);}
    	)*
    ;

implication returns [ExpressionNode expr]
    :	op1=or {$expr = $op1.expr;}
    	(	'->' op2=or  {$expr = new ImplicationNode($expr, $op2.expr);}
    	)*
    ;
    
expression returns [ExpressionNode expr]
    :   op1=implication {$expr = $op1.expr;}
    	('?' op2=expression ':' op3=expression 
    		{
    			$expr = new TernaryNode($expr, $op2.expr, $op3.expr);
    		}
    	)?
    ;
    
// Boolean only expression
//boolTerm returns [ExpressionNode expr]
//    :   ID 
//    		{
//    			if(ConstHashMap.containsKey($ID.text)){
//    				$expr = new ConstNode($ID.text, ConstHashMap.get($ID.text));
//    			}
//    			else if(VarHashSet.contains($ID.text)){
//    				$expr = new platu.lpn.io.expression.VarNode($ID.text);
//    			}
//    			else{
//    				System.err.println("error on line " + $ID.getLine() + ": " + $ID.text + " has not been declared");
//    				System.exit(1);
//    			}
//   			}
//    |   LPAREN boolExpr RPAREN {$expr = $boolExpr.expr;}
//    |   INT {$expr = new ConstNode("name", Integer.parseInt($INT.text));}
//    |   TRUE {$expr = ONE;}
//    |   FALSE {$expr = ZERO;}
//    ;
//    
//boolNeg returns [ExpressionNode expr]
//	:	{Boolean neg = false;}
//		('!' 
//			{
//				if(neg){
//					neg = false;
//				} 
//				else{
//					neg = true;
//				}
//			}
//		)* boolTerm
//			{
//				if(neg){
//					$expr = new NegNode($boolTerm.expr);
//				}
//				else{
//					$expr = $boolTerm.expr;
//				}
//			}
//	;
//	
//boolRel returns [ExpressionNode expr]
//    :   op1=boolNeg {$expr = $op1.expr;}
//    	(	'<' op2=boolNeg {$expr = new LessNode($expr, $op2.expr);}
//    	| 	'<=' op2=boolNeg {$expr = new LessEqualNode($expr, $op2.expr);}
//    	| 	'>=' op2=boolNeg {$expr = new GreatEqualNode($expr, $op2.expr);}
//    	| 	'>' op2=boolNeg {$expr = new GreatNode($expr, $op2.expr);}
//    	)*
//    ;
//    
//boolEquiv returns [ExpressionNode expr]
//    :   op1=boolRel {$expr = $op1.expr;}
//    	(	'==' op2=boolRel {$expr = new EquivNode($expr, $op2.expr);}
//    	|	'!=' op2=boolRel {$expr = new NotEquivNode($expr, $op2.expr);}
//    	)*
//    ;
//    
//boolAnd returns [ExpressionNode expr]
//    :   op1=boolEquiv {$expr = $op1.expr;}
//    	(	'&&' op2=boolEquiv {$expr = new AndNode($expr, $op2.expr);}
//    	)*
//    ;
//    
//boolOr returns [ExpressionNode expr]
//    :   op1=boolAnd {$expr = $op1.expr;}
//    	( '||' op2=boolAnd {$expr = new OrNode($expr, $op2.expr);}
//    	)*
//    ;
//    
//boolExpr returns [ExpressionNode expr]
//    :   op1=boolOr {$expr = $op1.expr;}
//    	('?' op2=boolExpr ':' op3=boolExpr 
//    		{
//    			$expr = new TernaryNode($expr, $op2.expr, $op3.expr);
//    		}
//    	)?
//    ;

// Tokens
LPAREN: '(';
RPAREN: ')';
QMARK: '?';
COLON: ':';
SEMICOLON: ';';
PERIOD: '.';
UNDERSCORE: '_';
COMMA: ',';
QUOTE: '"';

// Reserved Words
MODULE: 'mod';
NAME: 'name';
INPUT: 'input';
OUTPUT: 'output';
INTERNAL: 'var';
MARKING: 'marking';
STATE_VECTOR: 'statevector';
TRANSITION: 'transition';
LABEL: 'label';
PRESET: 'preset';
POSTSET: 'postset';
TRUE: 'true';
FALSE: 'false';

// Arithmetic Operators
PLUS: '+';
MINUS: '-';
TIMES: '*';
DIV: '/';
MOD: '%';
EQUALS: '=';

// Comparison Operators
GREATER: '>';
LESS: '<';
GREATER_EQUAL: '>=';
LESS_EQUAL: '<=';
EQUIV: '==';
NOT_EQUIV: '!=';

// Logical Operators
NEGATION: '!';
AND: '&&';
OR: '||';
IMPLICATION: '->';

// Bitwise Operators
BITWISE_NEGATION: '~';
BITWISE_AND: '&';
BITWISE_OR: '|';
BITWISE_XOR: '^';
BITWISE_LSHIFT: '<<';
BITWISE_RSHIFT: '>>';

fragment LETTER: ('a'..'z' | 'A'..'Z');
fragment DIGIT: '0'..'9';
INT: '-'? DIGIT+;
ID: LETTER ((UNDERSCORE | PERIOD)? (LETTER | DIGIT))*;
WS: (' ' | '\t' | '\n' | '\r' | '\f')+ {$channel = HIDDEN;};
COMMENT: '//' .* ('\n' | '\r') {$channel = HIDDEN;};
MULTILINECOMMENT: '/*' .* '*/' {$channel = HIDDEN;};
XMLCOMMENT: ('<' '!' '-' '-') .* ('-' '-' '>') {$channel = HIDDEN;};
IGNORE: '<' '?' .* '?' '>' {$channel = HIDDEN;};

