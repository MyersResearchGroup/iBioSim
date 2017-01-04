grammar PlatuGrammar;

options {
  language = Java;
}

@header{
    package verification.platu.lpn.io;
    
    import java.util.Map.Entry;
    import java.util.HashMap;
    import java.util.LinkedList;
    import java.util.HashSet;
    import java.util.Set;
    import java.util.Arrays;
    import verification.platu.lpn.LPN;
    import verification.platu.lpn.VarSet;
    import verification.platu.lpn.LpnTranList;
    import verification.platu.lpn.LPNTran;
    import verification.platu.lpn.DualHashMap;
    import verification.platu.lpn.VarExpr;
    import verification.platu.lpn.VarExprList;
    import verification.platu.stategraph.StateGraph;
    import verification.platu.project.Project;
    import verification.platu.expression.*;
    
    import lpn.parser.LhpnFile;
    import lpn.parser.Place;
    import lpn.parser.Transition;
    import lpn.parser.Variable;
    import lpn.parser.ExprTree;
     
    
    
}

@members{
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
    package verification.platu.lpn.io;
}

//parseLpnFile[Project prj, boolean instance, HashMap<String, String> portMap] returns [Set<LPN> lpnSet]
//	:	
//	;
	
//lpn[Project prj] returns [Set<LPN> lpnSet]
  lpn returns [Set<LhpnFile> lpnSet]//
    :   {$lpnSet = new HashSet<LhpnFile>();}//{$lpnSet = new HashSet<LPN>();}
        ((globalConstants globalVariables) | (globalVariables globalConstants) | (globalVariables) | (globalConstants))?
        	{
        		// check that global constants are consistently defined in each lpn file
        		if(GlobalSize > 0 && GlobalCount != GlobalSize){
        			System.err.println("error: global variable definitions are inconsistent");
        			System.exit(1);
        		}
        	} 
      	(module //module[prj]
            {
            	$lpnSet.add($module.lpn);
            }
        | main //main[prj]
        	{
        		
        	}
        )+ EOF
    ;
    
main // [Project prj]
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
//module[Project prj] returns [LPN lpn]
module returns [LhpnFile lpn]
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
          System.out.println("mod = " + $ID.text);
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
	            
//	            int i = 0;
//	            int[] initialMarking = new int[$logic.initMarking.size()];
//	            for(Integer mark : $logic.initMarking){
//	            	initialMarking[i++] = mark;
//	            }

//				$lpn = new LPN(prj, $ID.text, Inputs, Outputs, Internals, VarNodeMap, $logic.lpnTranSet, 
//	         			StatevectorMap, initialMarking);
        $lpn = new LhpnFile();
        $lpn.setLabel($ID.text);
        System.out.println("---- LPN : " + $lpn.getLabel() + " ----");
        for (Transition t: $logic.lpnTranSet) {
          $lpn.addTransition(t);
          t.setLpn($lpn);
          System.out.println("transition(logic): " + t.getLabel());
          for (Place p : t.getPreset()) {
            if ($logic.initMarking.contains(p.getName())) 
              $lpn.addPlace(p.getName(), true);
            else
              $lpn.addPlace(p.getName(), false);
            $lpn.getPlace(p.getName()).addPostset(t);            
          }
          for (Place p : t.getPostset()) {
            if ($logic.initMarking.contains(p.getName())) 
              $lpn.addPlace(p.getName(), true);
            else
              $lpn.addPlace(p.getName(), false);
            $lpn.getPlace(p.getName()).addPreset(t);
          }   
        }
        
//				for(Transition tran : inputTranList){
//					tran.addDstLpn($lpn);
//				}
//				$lpn.addAllInputTrans(inputTranList);
//				$lpn.addAllOutputTrans(outputTranList);
//	      $lpn.setVarIndexMap(VarIndexMap);         
//	      $logic.lpnTranSet.setLPN($lpn);     
//	      prj.getDesignUnitSet().add($lpn.getStateGraph());
          
//          for (Transition t : inputTranList) {
//            System.out.println("transition(in): " + t.getLabel()); 
//            $lpn.addTransition(t);
//          }
          for (Transition t : outputTranList) {
            System.out.println("transition(out): " + t.getLabel());
            $lpn.addTransition(t);
          }
          
	        LpnMap.put($lpn.getLabel(), $lpn);
//	        for (String var : StatevectorMap.keySet()) {
//	          $lpn.addInteger(var, StatevectorMap.get(var)+"");
//	        }
	        
	        // TODO: Where to use these hashmaps?
          // map outputs to lpn object
          for(String output : Outputs){
            GlobalOutputMap.put(output, $lpn);
            $lpn.addOutput(output, "integer", StatevectorMap.get(output)+"");
            System.out.println("@1: Added output variable " + output + " to LPN " + $lpn.getLabel());
          }
          // map potential output to lpn object
          for(String internal : Internals){
            GlobalOutputMap.put(internal, $lpn);
            $lpn.addInternal(internal, "integer", StatevectorMap.get(internal)+"");
            System.out.println("@1: Added internal variable " + internal + " to LPN " + $lpn.getLabel() + "with initial value " + StatevectorMap.get(internal));
	        }
          // map input variable to lpn object
          for(String input : Inputs) {
            if(GlobalInputMap.containsKey(input)){
              GlobalInputMap.get(input).add($lpn);
              $lpn.addInput(input, "integer", StatevectorMap.get(input)+"");
              System.out.println("@1: Added input variable " + input + " to LPN " + $lpn.getLabel() + " with initial value " + StatevectorMap.get(input) + ".");
	          }
          	else{
          	  List<LhpnFile> lpnList = new ArrayList<LhpnFile>();//List<LPN> lpnList = new ArrayList<LPN>();
           	  lpnList.add($lpn);
           	  GlobalInputMap.put(input, lpnList);
           	  $lpn.addInput(input, "integer", StatevectorMap.get(input)+"");
           	  System.out.println("@2: Added input variable " + input + " to LPN " + $lpn.getLabel() + " with initial value " + StatevectorMap.get(input) + ".");
           	}
          }
          
			}
		)
    ;
    
constants
	:	'<' 'const' '>' (const1=ID '=' val1=INT 
			{
				// make sure constant is not defined as something else
//				String const1_tmp = $const1.text;
//				if ($const1.text.contains(".")) {
//				  const1_tmp.replaceAll(".", "_");
//				}
//				else {
//				  const1_tmp = $const1.text;
//				}

        String const1_tmp = $const1.text;
        if ($const1.text.contains(".")) {
          const1_tmp = const1_tmp.replace(".", "_");
        }
        else {
          const1_tmp = $const1.text;
        }
				if(StatevectorMap.containsKey(const1_tmp)){
					System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " already exists as a variable"); 
					System.exit(1);
				}
				else if(GlobalConstHashMap.containsKey(const1_tmp)){
				    System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " already exists as a global constant");
				    System.exit(1);
				}
				else if(GlobalVarHashMap.containsKey(const1_tmp)){
            		System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " is already defined as a global variable");
            		System.exit(1);
            	}
				// put will override previous value
				//Integer result = ConstHashMap.put($const1.text, Integer.parseInt($val1.text));
				Integer result = ConstHashMap.put(const1_tmp, Integer.parseInt($val1.text));			
				if(result != null){
					System.err.println("warning on line " + $const1.getLine() + ": " + $const1.text + " will be overwritten");
				}
			}
		 ';')* '<' '/' 'const' '>'
	;

globalConstants
    :   '<' 'const' '>' (const1=ID '=' val1=INT 
            {
              String const1_tmp = $const1.text;
              if ($const1.text.contains(".")) {
                const1_tmp = const1_tmp.replace(".", "_");
              }
              else {
                const1_tmp = $const1.text;
              }
            	// make sure constant has not been defined already
            	if(GlobalVarHashMap.containsKey(const1_tmp)){
            		System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " is already defined as a global variable");
            		System.exit(1);
            	}
            	
            	// put will override previous value
                Integer result = GlobalConstHashMap.put(const1_tmp, Integer.parseInt($val1.text));
                if(result != null){
                    System.err.println("warning on line " + $const1.getLine() + ": " + $const1.text + " will be overwritten");
                }
            }
         ';')* '<' '/' 'const' '>'
    ;

globalVariables
	:	'<' 'var' '>' (var=ID 
			{
				// make sure global variables are consistently defined in each lpn file
				String var_tmp = $var.text;
        if ($var.text.contains(".")) {
          var_tmp = var_tmp.replace(".", "_");
        }
        else {
          var_tmp = $var.text;
        }
				if(GlobalSize == 0){
					if(GlobalConstHashMap.containsKey(var_tmp)){
						System.err.println("error on line" + $var.getLine() + ": " + $var.text + "already exists as a constant"); 
	                    System.exit(1);
					}
					else if(GlobalVarHashMap.containsKey(var_tmp)){
						System.err.println("error on line " + $var.getLine() + ": " + $var.text + " has already been defined");
						System.exit(1);
					}
				}
				else{
					if(!GlobalVarHashMap.containsKey(var_tmp)){
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
					GlobalVarHashMap.put(var_tmp, value);
				}
				else{
					int globalVal = GlobalVarHashMap.get(var_tmp);
					if(globalVal != value){
						System.err.println("error on line " + $val.getLine() + ": " + $var.text + " is inconsistently assigned");
						System.exit(1);
					}
				}
			}
		| var2=ID
			{
			  String var2_tmp = $var2.text;
        if ($var2.text.contains(".")) {
          var2_tmp = var2_tmp.replace(".", "_");
        }
        else {
          var2_tmp = $var2.text;
        }
				// get value of variable
				Integer value = null;
				if(GlobalConstHashMap.containsKey(var2_tmp)){
					value = GlobalConstHashMap.get(var2_tmp);
				}
				else if(GlobalVarHashMap.containsKey(var2_tmp)){
					System.err.println("error on line " + $var2.getLine() + ": global variable " + $var2.text + " cannot be assigned to global variable " + $var.text); 
    				System.exit(1);
				}
				else{
					System.err.println("error on line " + $var2.getLine() + ": " + $var2.text + " is not defined"); 
    				System.exit(1);
				}
				// make sure global variable is consistently initialized
				if(GlobalSize == 0){
					GlobalVarHashMap.put(var2_tmp, value);
				}
				else{
					int globalVal = GlobalVarHashMap.get(var2_tmp);
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
	:	{Integer value = null; Token varNode = null; String var_tmp;}
		'<' 'var' '>' ((var=ID 
			{
				// check variable is unique in scope
				var_tmp = $var.text;
        if ($var.text.contains(".")) {
          var_tmp = var_tmp.replace(".", "_");
        }
        else {
          var_tmp = $var.text;
        }		
				if(GlobalConstHashMap.containsKey(var_tmp)){
					System.err.println("error on line " + $var.getLine() + ": " + $var.text + " is a global constant"); 
    				System.exit(1);
				}
				else if(GlobalVarHashMap.containsKey(var_tmp)){
					System.err.println("error on line " + $var.getLine() + ": " + $var.text + " is a global variable"); 
    				System.exit(1);
				}
				else if(StatevectorMap.containsKey(var_tmp)){
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
			  String var2_tmp = $var2.text;
        if ($var2.text.contains(".")) {
          var2_tmp = var2_tmp.replace(".", "_");
        }
        else {
          var2_tmp = $var2.text;
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
					System.err.println("error on line " + $var2.getLine() + ": " + $var2.text + " is not defined"); 
    				System.exit(1);
				}
				
				varNode = var2;
				var_tmp = var2_tmp;
			}
		)';')
			{
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
			// TODO: Need to support arrays in our LPN.
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
						//VarNode v = new VarNode(name, index);
						ExprTree v = new ExprTree(name);
						array.add(v);
						
						// add variable and value to state vector
						StatevectorMap.put(name, 0);
						
						// generate variable index and create new var node  
		   			VarIndexMap.insert(name, index);
					}
					// TODO: ArrayNodeMap? 
//					ArrayNodeMap.put($arrayName.text, new ArrayNode($arrayName.text, array, 1));
					VarCountMap.put($arrayName.text, 0);
				}
				else{
				}
			}
		))* '<' '/' 'var' '>'
			{
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
	;

// TODO: implement instantiation
instantiation
    :   {HashMap<String, String> portMap = new HashMap<String, String>();}
    	'<' 'inst' '>'
    	modName=ID instName=ID '('(mod=ID '.' var=ID)+ ')'
    	'<' '/' 'inst' '>'
    ;

logic returns [List<String> initMarking, LpnTranList lpnTranSet]//[List<Integer> initMarking, LpnTranList lpnTranSet]
    :   {$lpnTranSet = new LpnTranList();}
    	marking (transition {$lpnTranSet.add($transition.lpnTran);})+
        {
            $initMarking = $marking.mark;
        }
    ; 
    
marking returns [List<String> mark] //[List mark]
    :   {$mark = new LinkedList<String>(); Integer result;}
        ('<' 'marking' '>' ((m1=INT
        	{
//        		$mark.add(Integer.parseInt($m1.text));
//        		initMarkedPlaces.add(Integer.parseInt($m1.text));
        		$mark.add("p" + $m1.text);
            initMarkedPlaces.add("p" + $m1.text);     		
        	} 
        | c1=ID
        	{
        	  String c1_tmp = $c1.text;
        	  if ($c1.text.contains(".")) {
        	    c1_tmp = c1_tmp.replace(".", "_");
        	  }
        	  else {
        	    c1_tmp = $c1.text;
        	  }
//        		result = ConstHashMap.get(c1_tmp);
//        		if(result == null){
//        			System.err.println("error on line " + $c1.getLine() + ": " + $c1.text + " is not a valid constant");
//        			System.exit(1);
//        		}
//        		$mark.add(result);
//        		initMarkedPlaces.add(Integer.parseInt($m1.text));
              $mark.add(c1_tmp);
              initMarkedPlaces.add(c1_tmp);
        	}
        ) (',' (m2=INT 
        	{
//        		$mark.add(Integer.parseInt($m2.text));
//        	  initMarkedPlaces.add(Integer.parseInt($m2.text));
            $mark.add("p"+$m2.text);
            initMarkedPlaces.add("p" +$m2.text);
        		
        	}
        | c2=ID
        	{
        	  String c2_tmp = $c2.text;
        	  if ($c2.text.contains(".")) {
        	    c2_tmp = c2_tmp.replace(".", "_");
        	  }
        	  else {
        	    c2_tmp = $c2.text;
        	  }
//        		result = ConstHashMap.get(c2_tmp);
//        		if(result == null){
//        			System.err.println("error on line " + $c2.getLine() + ": " + $c2.text + " is not a valid constant");
//        			System.exit(1);
//        		}
//        		$mark.add(result);
//        		initMarkedPlaces.add(result);
              $mark.add(c2_tmp);
              initMarkedPlaces.add(c2_tmp);
        	}
       	))*)? '<' '/' 'marking' '>')?
    ;
 
transition returns [Transition lpnTran]// [LPNTran lpnTran]
    :   	{
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
	    		
	    	}
    	'<' 'transition' 'label' '=' '"' lbl=(ID|INT) '"' 'preset' '=' ('"' '"' | ('"' (pre=INT 
    		{
    			//presetList.add(Integer.parseInt($pre.text));
    			if ($pre!=null && initMarkedPlaces.contains(Integer.parseInt($pre.text))) 
    			  presetList.add(new Place("p"+$pre.text, true));    			
    			else 
    			  presetList.add(new Place("p"+$pre.text, false));
   			} 
  		| pre1=ID
  			{
  			  String pre1_tmp = $pre1.text;
  			  if ($pre1.text.contains(".")) {
  			    pre1_tmp = pre1_tmp.replace(".", "_");
  			  }
  			  else {
  			    pre1_tmp = $pre1.text;
  			  }
  				result = ConstHashMap.get(pre1_tmp); 
  				if(result == null){
  					System.err.println("error on line " + $pre1.getLine() + ": " + $pre1.text + " is not a constant");
  					System.exit(1);
  				}
  				//presetList.add(result);
  				if (initMarkedPlaces.contains(result)) 
            presetList.add(new Place("p"+result, true));
          else
            presetList.add(new Place("p"+result, false));
  			}
 		) ( ',' pre2=INT 
    		{
    			//presetList.add(Integer.parseInt($pre2.text));
    			if ($pre2!=null && initMarkedPlaces.contains(Integer.parseInt($pre2.text)))
            presetList.add(new Place("p"+$pre2.text, true));
          else
            presetList.add(new Place("p"+$pre2.text, false));
   			} 
  		| ',' pre3=ID
  			{
  			  String pre3_tmp = $pre3.text;
          if ($pre3.text.contains(".")) {
            pre3_tmp = pre3_tmp.replace(".", "_");
          }
          else {
            pre3_tmp = $pre3.text;
          }
  				result = ConstHashMap.get(pre3_tmp); 
  				if(result == null){
  					System.err.println("error on line " + $pre3.getLine() + ": " + $pre3.text + " is not a constant");
  					System.exit(1);
  				}
  				//presetList.add(result);
  				if ($pre3!=null && initMarkedPlaces.contains(Integer.parseInt($pre3.text)))
            presetList.add(new Place("p"+result, true));
          else
            presetList.add(new Place("p"+result, false));
  			}
 		)* '"')) 'postset' '=' ( '"' '"' | ('"' ( post=INT
    		{
    			//postsetList.add(Integer.parseInt($post.text));
    			if ($post!=null && initMarkedPlaces.contains(Integer.parseInt($post.text)))
            postsetList.add(new Place("p"+$post.text, true));
          else
            postsetList.add(new Place("p"+$post.text, false));
    		} 
    	| post1=ID
    		{
    		  String post1_tmp = $post1.text;
          if ($post1.text.contains(".")) {
            post1_tmp = post1_tmp.replace(".", "_");
          }
          else {
            post1_tmp = $post1.text;
          }
    			result = ConstHashMap.get(post1_tmp); 
  				if(result == null){
  					System.err.println("error on line " + $post1.getLine() + ": " + $post1.text + " is not a constant");
  					System.exit(1);
  				}
  				//postsetList.add(result);
  				if (initMarkedPlaces.contains(result))
            postsetList.add(new Place("p"+result, true));
          else
            postsetList.add(new Place("p"+result, false));
    		}
    	)
    	( (',' post2=INT)
    		{
    			//postsetList.add(Integer.parseInt($post2.text));
    			if ($post2!=null && initMarkedPlaces.contains(Integer.parseInt($post2.text)))
            postsetList.add(new Place("p"+$post2.text, true));
          else
            postsetList.add(new Place("p"+$post2.text, false));
    		} 
    	| (','post3=ID)
    		{
    		  String post3_tmp = $post3.text;
          if ($post3.text.contains(".")) {
            post3_tmp = post3_tmp.replace(".", "_");
          }
          else {
            post3_tmp = $post3.text;
          }
    			result = ConstHashMap.get(post3_tmp); 
  				if(result == null){
  					System.err.println("error on line " + $post3.getLine() + ": " + $post3.text + " is not a constant");
  					System.exit(1);
  				}		
//  				postsetList.add(result);
          if (initMarkedPlaces.contains(result))
            postsetList.add(new Place("p"+result, true));
          else
            postsetList.add(new Place("p"+result, false));
    		}
    	)* '"' )) '>' (guard 
    		{
    			guardExpr = $guard.expr;
    		}
    	)? (delay 
    		{
    		  // TODO: ignored delay for untimed models
    			delayLB = $delay.delayLB; 
    			delayUB = $delay.delayUB;
    		}
    	)? ((assertion
    		{
    		  // TODO: ignored assertions temporarily
    			if($assertion.booleanExpr != null){		
  					assertionList.add($assertion.booleanExpr);
  				}
    		}
    	) | (assignment
    		{
    			assignmentList.putAll($assignment.assign);
    		}
    	))* '<' '/' 'transition' '>'
        {
        	// create new lpn transitions and add assertions
        	// $lpnTran = new LPNTran($lbl.text, TransitionIndex++, presetList, postsetList, guardExpr, assignmentList, delayLB, delayUB, local);
        	$lpnTran = new Transition();
        	$lpnTran.setName("t" + $lbl.text);
        	$lpnTran.setIndex(TransitionIndex++);
        	for (Place p: presetList) {
        	  $lpnTran.addPreset(p);
        	}
        	for (Place p: postsetList) {
            $lpnTran.addPostset(p);
          }
          $lpnTran.addEnablingWithoutLPN(guardExpr);
          for (String var : assignmentList.keySet()) {
            $lpnTran.addIntAssign(var, assignmentList.get(var));
          }
          
        	// TODO: Add assertionList to the new lpn transition
//        	if(assertionList.size() > 0){
//        		$lpnTran.addAllAssertions(assertionList);
//        	}

          // add non-local transition to associated LPNs
          for(String var : assignmentList.keySet()){
            if(Outputs.contains(var)){
              // local is determined by isLocal() in Transition.java
              local = false;            
//              if(GlobalInputMap.containsKey(var)){
//                for(LhpnFile lpn : GlobalInputMap.get(var)){
//                  lpn.addInputTran($lpnTran);
//                  // dstLpn is added by setDstLpnList in Transition.java
//                  $lpnTran.addDstLpn(lpn);
//                }
//              }
            }
            
            // map lpn transition with output and potential outuput variables
            if(GlobalTranMap.containsKey(var)){
              GlobalTranMap.get(var).add($lpnTran);
              System.out.println("Add "+ $lpnTran.getLabel() + " to variable " + var);
            }
            else{
              List<Transition> tranList = new ArrayList<Transition>();
              tranList.add($lpnTran);
              GlobalTranMap.put(var, tranList);
              System.out.println("Create tranList for variable " + var + ", and add " + $lpnTran.getLabel() + " to it.");
              
            }
          }

//     		$lpnTran.setLocal(local);
       		if(local == false){
       			outputTranList.add($lpnTran);
       		}
        }
    ;
  
assertion returns [ExprTree booleanExpr]//[Expression booleanExpr]
	:	{booleanExpr = null;// TODO: assertion
	} 
		'assert' '(' expression ')' ';'
			{
				//$booleanExpr = new Expression($expression.expr);
				$booleanExpr = new ExprTree($expression.expr);
			}
	;
	
guard returns [ExprTree expr]//[Expression expr]
    :   expression ';'
    		{
   				//$expr = new Expression($expression.expr);
   				$expr = new ExprTree($expression.expr);
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

assignment returns [HashMap<String, ExprTree> assign]//[VarExpr assign] 
    :   (var=ID '=' 
    		{	
    		  String var_tmp = $var.text;
    		  if ($var.text.contains(".")) {
    		    var_tmp = var_tmp.replace(".", "_");
    		  }
    		  else {
    		    var_tmp = $var.text;
    		  }
    			// make sure only global, internal and output variables are assigned
    			if(GlobalConstHashMap.containsKey(var_tmp)){
    				System.err.println("error on line " + $var.getLine() + ": global constant " + $var.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(ConstHashMap.containsKey(var_tmp)){
    				System.err.println("error on line " + $var.getLine() + ": constant " + $var.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(!Outputs.contains(var_tmp) && !Internals.contains(var_tmp)){
    				System.err.println("error on line " + $var.getLine() + ": input variable " + $var.text + " cannot be assigned");
    				System.exit(1);
    			}
    		}
    	expression ';'
	    	{
	    		Integer varCount = VarCountMap.get(var_tmp);
	    		if(varCount != null){
	    			VarCountMap.put(var_tmp, ++varCount);
	    		}
	    		
//	    		Expression expr = new Expression($expression.expr);
//	    		$assign = new VarExpr(VarNodeMap.get($var.text), expr);

            ExprTree expr = new ExprTree($expression.expr);
            $assign = new HashMap<String, ExprTree>();
            $assign.put(var_tmp, expr);
	   		}
	   		//TODO: Need to support array assignment
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
term returns [ExprTree expr] //[ExpressionNode expr]
    :   ID
    		{
    		  String ID_tmp = $ID.text;
          if ($ID.text.contains(".")) {
            ID_tmp = ID_tmp.replace(".", "_");
          }
          else {
            ID_tmp = $ID.text;
          }
    			if(ConstHashMap.containsKey(ID_tmp)){
    				//$expr = new ConstNode($ID.text, ConstHashMap.get($ID.text));
    				$expr = new ExprTree(ConstHashMap.get(ID_tmp)+"");
    			}
    			else if(GlobalConstHashMap.containsKey(ID_tmp)){
    			  //$expr = new ConstNode($ID.text, GlobalConstHashMap.get($ID.text));
    			  $expr = new ExprTree(GlobalConstHashMap.get(ID_tmp)+"");
    			}
    			else if(StatevectorMap.containsKey(ID_tmp)){ 
    			  //$expr = VarNodeMap.get($ID.text);
    			  $expr = VarNodeMap.get(ID_tmp);
    			}
    			else{ // identify new input variable
//    				// create expression
//					$expr = new platu.lpn.io.expression.VarNode($ID.text);
					
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
          $expr = newVarNode.getExprTree();
          VarNodeMap.put(ID_tmp, newVarNode);
          
//					int index = VariableIndex++;
//	    			VarIndexMap.insert($ID.text, index);
//	    			VarNode newVarNode = new VarNode($ID.text, index);
//	    			VarNodeMap.put($ID.text, newVarNode);
//	    			$expr = newVarNode;
	    			
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
    |   LPAREN expression RPAREN {$expr = $expression.expr;}
    |   INT { //{$expr = new ConstNode("name", Integer.parseInt($INT.text));}
            ExprTree tree = new ExprTree(Integer.parseInt($INT.text)+"");
            $expr = tree.getExprTree();         
        } 
    |   TRUE {
          ExprTree tree = new ExprTree("true");
          $expr = tree.getExprTree();
        }
    |   FALSE {
          ExprTree tree = new ExprTree("false");
          $expr = tree.getExprTree();
        }
    ;
//    |   TRUE {$expr = ONE;}
//    |   FALSE {$expr = ZERO;}
//    ;

    
unary returns [ExprTree expr]//[ExpressionNode expr]
    :   {boolean positive = true;}
    	('+' | ('-' {if(positive){ positive = false;} else {positive = true;}}))* term
    	{
    		if(!positive){
    			//$expr = new MinNode($term.expr);
    			// TODO: Correct ExprTree for unary?
    			$expr = new ExprTree($term.expr, null, "U-", 'a');
    		}
    		else{
    			$expr = $term.expr;
   			}
    	}
    ;

bitwiseNegation returns [ExprTree expr] //[ExpressionNode expr]
	: 	{boolean neg = false;}
		('~' {if(neg){neg = false;} else{neg = true;}})* unary
			{
				if(neg){
					//$expr = new BitNegNode($unary.expr);
					$expr = new ExprTree(null, $unary.expr, "~", 'l');
				}
				else{
					$expr = $unary.expr;
				}
			}
	;
	
negation returns [ExprTree expr]//[ExpressionNode expr]
	:	{boolean neg = false;}
		('!' {if(neg){neg = false;} else{neg = true;}})* bitwiseNegation
			{
				if(neg){
					//$expr = new NegNode($bitwiseNegation.expr);
					// TODO: Correct translation of negation?
					$expr = new ExprTree(null, $bitwiseNegation.expr, "~", 'l');
				}
				else{
					$expr = $bitwiseNegation.expr;
				}
			}
	;
	
mult returns [ExprTree expr] //[ExpressionNode expr]
    :   op1=negation {$expr = $op1.expr;} 
    	(	'*' op2=negation {$expr = new ExprTree($expr, $op2.expr, "*", 'a');}//{$expr = new MultNode($expr, $op2.expr);}
    	|	'/' op2=negation {$expr = new ExprTree($expr, $op2.expr, "/", 'a');}//{$expr = new DivNode($expr, $op2.expr);}
    	// TODO: Line below fails to compile
    	// |	'%' op2=negation {$expr = new ExprTree($expr, $op2.expr, "%", 'a');}//{$expr = new ModNode($expr, $op2.expr);}
    	)*
    ;
    
add returns [ExprTree expr] //[ExpressionNode expr]
    :   op1=mult {$expr = $op1.expr;}
    	(	'+' op2=mult {$expr = new ExprTree($expr, $op2.expr, "+", 'a');}//{$expr = new AddNode($expr, $op2.expr);}
    	| 	'-' op2=mult {$expr = new ExprTree($expr, $op2.expr, "-", 'a');}//{$expr = new SubNode($expr, $op2.expr);}
    	)*
    ;
    
shift returns [ExprTree expr]//[ExpressionNode expr]
    :   op1=add {$expr = $op1.expr;}
      ( '<<' op2=add {$expr = new ExprTree(new ExprTree("int(" + $op1.text + ")"), new ExprTree(new ExprTree("2"), $op2.expr, "^", 'a'), "*", 'a');}
      |   '>>' op2=add {$expr = new ExprTree(new ExprTree("int(" + $op1.text + ")"), new ExprTree(new ExprTree("2"), $op2.expr, "^", 'a'), "/", 'a');}
      )*
//    	(	'<<' op2=add {$expr = new LeftShiftNode($expr, $op2.expr);}
//    	| 	'>>' op2=add {$expr = new RightShiftNode($expr, $op2.expr);}
//    	)*
    ;

relation returns [ExprTree expr]//[ExpressionNode expr]
    :   op1=shift {$expr = $op1.expr;}
    	(	'<' op2=shift {$expr = new ExprTree($expr, $op2.expr, "<", 'r');}//{$expr = new LessNode($expr, $op2.expr);}
    	| 	'<=' op2=shift {$expr = new ExprTree($expr, $op2.expr, "<=", 'r');}//{$expr = new LessEqualNode($expr, $op2.expr);}
    	| 	'>=' op2=shift {$expr = new ExprTree($expr, $op2.expr, ">=", 'r');}//{$expr = new GreatEqualNode($expr, $op2.expr);}
    	| 	'>' op2=shift {$expr = new ExprTree($expr, $op2.expr, ">", 'r');}//{$expr = new GreatNode($expr, $op2.expr);}
    	)*
    ;
    
equivalence returns [ExprTree expr] // [ExpressionNode expr]
    :   op1=relation {$expr = $op1.expr;}
    	(	'==' op2=relation {$expr = new ExprTree($expr, $op2.expr, "=", 'r');}//{$expr = new EquivNode($expr, $op2.expr);}
    	// TODO: Not equal to in our LPN?
    	|	'!=' op2=relation {$expr = new ExprTree(new ExprTree($expr, $op2.expr, "=", 'r'), null, "~", 'l');}//{$expr = new NotEquivNode($expr, $op2.expr);}
    	)*
    ;
    
bitwiseAnd returns [ExprTree expr]//[ExpressionNode expr]
    :   op1=equivalence {$expr = $op1.expr;} 
    	(	'&' op2=equivalence {$expr = new ExprTree($expr, $op2.expr, "&", 'w');}//{$expr = new BitAndNode($expr, $op2.expr);}
    	)*
    ;
    
bitwiseXor returns [ExprTree expr]//[ExpressionNode expr]
    :   op1=bitwiseAnd {$expr = $op1.expr;}
    	(	'^' op2=bitwiseAnd {$expr = new ExprTree($expr, $op2.expr, "X", 'w');}//{$expr = new BitXorNode($expr, $op2.expr);}
    	)*
    ;
    
bitwiseOr returns [ExprTree expr]//[ExpressionNode expr]
    :   op1=bitwiseXor {$expr = $op1.expr;}
    	(	'|' op2=bitwiseXor {$expr = new ExprTree($expr, $op2.expr, "|", 'w');}//{$expr = new BitOrNode($expr, $op2.expr);}
    	)*
    ;
    
and returns [ExprTree expr] //[ExpressionNode expr]
    :   op1=bitwiseOr {$expr = $op1.expr;}
    	(	'&&' op2=bitwiseOr {$expr = new ExprTree($expr, $op2.expr, "&&", 'l');}//{$expr = new AndNode($expr, $op2.expr);}
    	// TODO: Temporarily set the type to 'l'.
    	)*
    ;
    
or returns [ExprTree expr]//[ExpressionNode expr]
    :   op1=and {$expr = $op1.expr;}
    	(	'||' op2=and {$expr = new ExprTree($expr, $op2.expr, "||", 'l');}//{$expr = new OrNode($expr, $op2.expr);}
    	// TODO: Temporarily set the type to 'l'.
    	)*
    ;

implication returns [ExprTree expr] //[ExpressionNode expr]
    :	op1=or {$expr = $op1.expr;}//{$expr = $op1.expr;}
    	(	'->' op2=or  {$expr = new ExprTree($expr, $op2.expr, "->", 'l');}//{$expr = new ImplicationNode($expr, $op2.expr);}
    	)*
    ;
    
expression returns [ExprTree expr] // [ExpressionNode expr]
    :   op1=implication {$expr = $op1.expr;}
    	('?' op2=expression ':' op3=expression 
    		{
    			//$expr = new TernaryNode($expr, $op2.expr, $op3.expr);
    			// op1?op2:op3 == int(op1)*op2+int(~op1)*op3
    			//$expr = new ExprTree($expr);
    			$expr = new ExprTree(new ExprTree(new ExprTree("int("+$op1.text + ")"), $op2.expr, "*", 'a'), 
    			                     new ExprTree(new ExprTree("int(~("+$op1.text + "))"), $op3.expr, "*", 'a'), 
    			                     "+", 'a');
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

