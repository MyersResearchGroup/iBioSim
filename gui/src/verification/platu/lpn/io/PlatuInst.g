grammar PlatuInst;

options {
  language = Java;
}

@header{
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
}

@members{
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

parseLpnFile[Project prj]
    :	include? globalConstants? globalVariables?  //((globalConstants globalVariables) | (globalVariables globalConstants) | (globalVariables) | (globalConstants))?
        	{
        		// check that global constants are consistently defined in each lpn file
        		if(GlobalSize > 0 && GlobalCount != GlobalSize){
        			System.err.println("error: global variable definitions are inconsistent");
        			System.exit(1);
        		}
        	} 
      	main[prj]? (moduleClass[prj])* EOF
    ;
    
include
	:	'<' 'include' '>' (PATH ';'
			{
				File f = new File($PATH.text);
        		includeSet.add(f.getAbsolutePath());
			}
		)+ '<' '/include' '>'
	;
	
main[Project prj]
	:	'<' 'mod' 'name' '=' '"' 'main' '"' '>'
			{
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
			}
		variables? constants? instantiation '<' '/mod' '>'
	;
    
process
	:	'<' 'process' 'name' '=' '"' processName=ID '"' '>' '<' '/process' '>'
	;
	
// TODO: don't enforce order
moduleClass[Project prj] returns [LPN lpn]
    :	( '<' 'class' 'name' '=' '"' modName=ID
    		{
    			// module names must be unique
    			if(LpnMap.containsKey($modName.text)){
    				System.err.println("error on line " + $modName.getLine() + ": module " + $modName.text + " already exists");
    				System.exit(1);
    			}
    			
    			if($modName.text.equals("main")){
    				error("error on line " + $modName.getLine() + ": main is reserved");
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
    		}
    	'"' 'arg' '=' '"' (((arrayArg2=ID 
    		{
    			// check aginst globals and other inputs
	   			if(GlobalConstHashMap.containsKey($arrayArg2.text)){
	   				System.err.println("error on line " + $arrayArg2.getLine() + ": variable " + $arrayArg2.text + " is already defined as a global constant");
	   				System.exit(1);
	   			}
	   			else if(GlobalVarHashMap.containsKey($arrayArg2.text)){
	   				System.err.println("error on line " + $arrayArg2.getLine() + ": variable " + $arrayArg2.text + " is already defined as a global variable");
	   				System.exit(1);
	   			}
	   			else if(VarNodeMap.containsKey($arrayArg2.text)){
	   				System.err.println("error on line " + $arrayArg2.getLine() + ": variable " + $arrayArg2.text + " is already defined");
	   				System.exit(1);
	   			}
	   			
	   			List<Integer> dimensionList = new ArrayList<Integer>();
    		}
    	('[' arrayExpr2=expression 
    		{
    			dimensionList.add($arrayExpr2.value);
    		}
    	']')+
    		{
    			createInputArray($arrayArg2.text, dimensionList);
    		}
    	) | (arg2=ID
    		{
    			// check against globals
    			if(GlobalConstHashMap.containsKey($arg2.text)){
    				System.err.println("error on line " + $arg2.getLine() + ": variable " + $arg2.text + " is already defined as a global constant");
    				System.exit(1);
    			}
    			else if(GlobalVarHashMap.containsKey($arg2.text)){
    				System.err.println("error on line " + $arg2.getLine() + ": variable " + $arg2.text + " is already defined as a global variable");
    				System.exit(1);
    			}
    			
    			// add variable and value to state vector
				StatevectorMap.put($arg2.getText(), 0);
				
				// generate variable index and create new var node  
				int index = VariableIndex++;
   				VarIndexMap.insert($arg2.getText(), index);
   				
   				VarNode inputVarNode =  new VarNode($arg2.getText(), index);
   				inputVarNode.setType(platu.lpn.VarType.INPUT);
   				VarNodeMap.put($arg2.getText(), inputVarNode);
//    			VarCountMap.put($arg2.getText(), 0);
    			
    			argumentList.add($arg2.getText());
				Inputs.add($arg2.getText());
    		}
    	)) ((',' arrayArg=ID
    		{
	    		// check aginst globals and other inputs
	   			if(GlobalConstHashMap.containsKey($arrayArg.text)){
	   				System.err.println("error on line " + $arrayArg.getLine() + ": variable " + $arrayArg.text + " is already defined as a global constant");
	   				System.exit(1);
	   			}
	   			else if(GlobalVarHashMap.containsKey($arrayArg.text)){
	   				System.err.println("error on line " + $arrayArg.getLine() + ": variable " + $arrayArg.text + " is already defined as a global variable");
	   				System.exit(1);
	   			}
	   			else if(VarNodeMap.containsKey($arrayArg.text)){
	   				System.err.println("error on line " + $arrayArg.getLine() + ": variable " + $arrayArg.text + " is already defined");
	   				System.exit(1);
	   			}
	   			
	   			List<Integer> dimensionList = new ArrayList<Integer>();
	   		}
    	('[' arrayExpr=expression 
    		{
    			dimensionList.add($arrayExpr.value);
    		}
    	']')+
    		{
    			createInputArray($arrayArg.text, dimensionList);
    		} 
    	) | (',' arg=ID 
    		{
    			// check aginst globals and other inputs
    			if(GlobalConstHashMap.containsKey($arg.text)){
    				System.err.println("error on line " + $arg.getLine() + ": variable " + $arg.text + " is already defined as a global constant");
    				System.exit(1);
    			}
    			else if(GlobalVarHashMap.containsKey($arg.text)){
    				System.err.println("error on line " + $arg.getLine() + ": variable " + $arg.text + " is already defined as a global variable");
    				System.exit(1);
    			}
    			else if(VarNodeMap.containsKey($arg.text)){
    				System.err.println("error on line " + $arg.getLine() + ": variable " + $arg.text + " is already defined");
    				System.exit(1);
    			}
    			
    			// add variable and value to state vector
				StatevectorMap.put($arg.getText(), 0);
				
				// generate variable index and create new var node  
				int index = VariableIndex++;
   				VarIndexMap.insert($arg.getText(), index);
   				
   				VarNode inputVarNode = new VarNode($arg.getText(), index);
   				inputVarNode.setType(platu.lpn.VarType.INPUT);
   				VarNodeMap.put($arg.getText(), inputVarNode);
//    			VarCountMap.put($arg.getText(), 0);
    			
    			argumentList.add($arg.getText());
				Inputs.add($arg.getText());
    		}
    	))* )? '"' '>' constants? variables logic '<' '/class' '>'
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
	            
	            int[] initialMarking = new int[$logic.initMarking.size()];
	            
	            int i = 0;
	            for(Integer mark : $logic.initMarking){
	            	initialMarking[i++] = mark;
	            }
	            
				$lpn = new LPN(prj, $modName.text, Inputs, Outputs, Internals, VarNodeMap, $logic.lpnTranSet, 
	         			StatevectorMap, initialMarking);
				
				$lpn.addAllInputTrans(inputTranList);
				$lpn.addAllOutputTrans(outputTranList);
	            $lpn.setVarIndexMap(VarIndexMap);         
	            $logic.lpnTranSet.setLPN($lpn);     

	            LpnMap.put($lpn.getLabel(), $lpn);
	            $lpn.setArgumentList(argumentList);
			}
		)
    ; 
	
constants
	:	'<' 'const' '>' (const1=ID '=' val1=INT 
			{
				// make sure constant is not defined as something else
				if(VarNodeMap.containsKey($const1.text)){
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
					System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " has already been defined");
					System.exit(1);
				}
			}
		 ';')* '<' '/const' '>'
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
                    System.err.println("error on line " + $const1.getLine() + ": " + $const1.text + " has already been defined");
                    System.exit(1);
                }
            }
         ';')* '<' '/const' '>'
    ;

globalVariables
	:	'<' 'var' '>' (globalVarDecl | globalArrayDecl)+ '<' '/var' '>'
	;

globalVarDecl
	:	var=ID 
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
				
				// make sure global variable is consitently initialized
				if(GlobalSize == 0){
					GlobalVarHashMap.put($var.text, value);
				}
				else{
					int globalVal = GlobalVarHashMap.get($var.text);
					if(globalVal != value){
						System.err.println("error on line " + $var2.getLine() + ": " + $var.text + " is inconsistently assigned");
						System.exit(1);
					}
				}
			}
		) ';'
	;
	
globalArrayDecl
	:	arrayVar=ID
			{
				List<Integer> dimensionList = new ArrayList<Integer>();
				
				// make sure global variables are consistently defined in each lpn file
				if(GlobalSize == 0){
					if(GlobalConstHashMap.containsKey($arrayVar.text)){
						System.err.println("error on line" + $arrayVar.getLine() + ": " + $arrayVar.text + "already exists as a constant"); 
	                    System.exit(1);
					}
					else if(GlobalVarHashMap.containsKey($arrayVar.text)){
						System.err.println("error on line " + $arrayVar.getLine() + ": " + $arrayVar.text + " has already been defined");
						System.exit(1);
					}
				}
				else{
					if(!GlobalVarHashMap.containsKey($arrayVar.text)){
						System.err.println("error on line " + $arrayVar.getLine() + ": " + $arrayVar.text + " is inconsistently defined");
						System.exit(1);
					}
				}
				
				GlobalCount++;
			}
		('[' (arrayExpr=expression 
			{
				dimensionList.add($arrayExpr.value);
			}
		) ']')+ ';'
			{
				// make sure global variables are consistently initialized
				if(GlobalSize == 0){
					GlobalVarHashMap.put($arrayVar.text, 0);
					GlobalVarNodeMap.put($arrayVar.text, new ArrayNode($arrayVar.text, null, dimensionList.size(), dimensionList, null));
				}
				else{
					ArrayNode node = (ArrayNode) GlobalVarNodeMap.get($arrayVar.text);
					if(node.getDimensions() != dimensionList.size()){
						error("error on line " + $arrayVar.getLine() + ": " + $arrayVar.text + " is inconsistently assigned");
					}
					
					List<Integer> dimList = node.getDimensionList();
					for(int i = 0; i < dimensionList.size(); i++){
						if(dimList.get(i) != dimensionList.get(i)){
							error("error on line " + $arrayVar.getLine() + ": " + $arrayVar.text + " is inconsistently assigned");
						}
					}
				}
			} 
	;
variables
	:	'<' 'var' '>' ( varDecl | arrayDecl)+ '<' '/var' '>'
	;

varDecl
	:	{Integer value = null; Token varNode = null;}
		var=ID 
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
				else if(VarNodeMap.containsKey($var.text)){
					System.err.println("error on line " + $var.getLine() + ": " + $var.text + " has already been defined");
					System.exit(1);
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
					value = StatevectorMap.get($var.text);
				}
				else{
					System.err.println("error on line " + $var2.getLine() + ": " + $var2.text + " is not defined or is not compatible"); 
    				System.exit(1);
				}
				
				varNode = var2;
			}
		) 
			{
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
			}
		';'
	;
	
arrayDecl
	:	var=ID 
			{
				List<Integer> dimensionList = new ArrayList<Integer>();
			}
		('[' (arrayExpr=expression
			{
				dimensionList.add($arrayExpr.value);
			}
		) ']')+
			{
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
						String name = $var.text + "." + varCount;
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

				ArrayNode newArray = new ArrayNode($var.text, topLevelArray, dimensionList.size(), dimensionList, varList);
				newArray.setType(platu.lpn.VarType.INTERNAL);
				VarNodeMap.put($var.text, newArray);
    			VarCountMap.put($var.text, 0);
				Internals.add($var.text);
			}
		('=' 
			{
				List<Integer> valueList = new ArrayList<Integer>();
			}
		'{' (val2=INT
			{
				Integer dimVal = Integer.parseInt($val2.text);
				if(dimVal < 1){
					error("error on line " + $val2.getLine() + ": invalid dimension");
				}
				
				valueList.add(dimVal);
			} 
		| var2=ID
			{
				Integer initVal = null;
				
				// get variable initial value
				if(GlobalConstHashMap.containsKey($var2.text)){
					initVal = GlobalConstHashMap.get($var2.text);
				}
				else if(GlobalVarHashMap.containsKey($var2.text)){
					initVal = GlobalVarHashMap.get($var2.text);
				}
				else if(ConstHashMap.containsKey($var2.text)){
					initVal = ConstHashMap.get($var2.text);
				}
				else if(StatevectorMap.containsKey($var2.text)){ // Should var be allowed to assign a var?
					initVal = StatevectorMap.get($var2.text);
				}
				else{
					System.err.println("error on line " + $var2.getLine() + ": " + $var2.text + " is not defined"); 
    				System.exit(1);
				}
				
				if(initVal < 1){
					error("error on line " + $var2.getLine() + ": invalid dimension");
				}
				
				valueList.add(initVal);
			}
		)+ '}'
			{
				//TODO: initialize array
				int dimensions = dimensionList.size();
			}
		)? ';'
	;
	
instantiation
    :	(modName=ID instName=ID 
    		{
    			List<String> argList = new ArrayList<String>();
    			List<String> modList = new ArrayList<String>();
    		}
    	'('((var=MEMBER ','
    		{
    			String buffer = $var.text;
        		StringTokenizer tk = new StringTokenizer(buffer, ".");
        		
    			String module = tk.nextToken();
    			String variable = tk.nextToken();
    			
    			modList.add(module);
    			argList.add(module + "." + variable);
    		}
    	)* var2=MEMBER
    		{
    			String buffer = $var2.text;
        		StringTokenizer tk = new StringTokenizer(buffer, ".");
        		
    			String module = tk.nextToken();
    			String variable = tk.nextToken();
    			
    			modList.add(module);
    			argList.add(module + "." + variable);	
    		} 
    	)?')' ';'
    		{
    			Instance inst = new Instance($modName.text, $instName.text, argList, modList);
    			InstanceList.add(inst);
    		}
    	)+
    ;

logic returns [List<Integer> initMarking, LpnTranList lpnTranSet]
    :   {$lpnTranSet = new LpnTranList();}
    	marking (transition {$lpnTranSet.add($transition.lpnTran);})+
        {
            $initMarking = $marking.mark;
        }
    ; 
    
marking returns [List<Integer> mark]
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
       	))*)? '<' '/marking' '>')?
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
    	( (',' post2=INT
    		{
    			postsetList.add(Integer.parseInt($post2.text));
    		} 
    	) | (','post3=ID
    		{
    			result = ConstHashMap.get($post3.text); 
  				if(result == null){
  					System.err.println("error on line " + $post3.getLine() + ": " + $post3.text + " is not a constant");
  					System.exit(1);
  				}
  				
  				postsetList.add(result);
    		}
    	))* '"' )) '>' (guard 
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
    	))* '<' '/transition' '>'
        {
        	// create new lpn transitions and add assertions
        	$lpnTran = new LPNTran($lbl.text, TransitionIndex++, presetList, postsetList, guardExpr, assignmentList, delayLB, delayUB, local);
        	if(assertionList.size() > 0){
        		$lpnTran.addAllAssertions(assertionList);
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
    :  'condition' '(' expression ')' ';'
    		{
   				$expr = new Expression($expression.expr);
    		}
    ;
    
delay returns [int delayLB, int delayUB]
    : 	'delay' '(' lb=INT
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
    :   (var1=ID '=' var2=ID) 
    		{
    			// make sure only global, internal and output variables are assigned
    			if(GlobalConstHashMap.containsKey($var1.text)){
    				System.err.println("error on line " + $var1.getLine() + ": global constant " + $var1.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(ConstHashMap.containsKey($var1.text)){
    				System.err.println("error on line " + $var1.getLine() + ": constant " + $var1.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(!VarNodeMap.containsKey($var1.text)){
    				System.err.println("error on line " + $var1.getLine() + ": variable " + $var1.text + " was not declared");
    				System.exit(1);
    			}
    			else if(!Outputs.contains($var1.text) && !Internals.contains($var.text)){
    				System.err.println("error on line " + $var1.getLine() + ": input variable " + $var1.text + " cannot be assigned");
    				System.exit(1);
    			}
    			
				ExpressionNode node2 = null;
    			if(GlobalConstHashMap.containsKey($var2.text)){
    				node2 = new ConstNode($var2.text, GlobalConstHashMap.get($var2.text));
    			}
    			else if(ConstHashMap.containsKey($var2.text)){
    				node2 = new ConstNode($var2.text, ConstHashMap.get($var2.text));
    			}
    			else if(GlobalVarHashMap.containsKey($var2.text)){
    				node2 = VarNodeMap.get($var2.text);
    			}
    			else if(!VarNodeMap.containsKey($var2.text)){
    				System.err.println("error on line " + $var2.getLine() + ": variable " + $var2.text + " was not declared");
    				System.exit(1);
    			}
    			else{
    				node2 = VarNodeMap.get($var2.text);
    			}
	    		
	    		VarNode node1 = VarNodeMap.get($var1.text);
	    		if(ArrayNode.class.isAssignableFrom(node1.getClass())){
	    			if(!ArrayNode.class.isAssignableFrom(node2.getClass())){
	   					System.err.println("error on line " + $var.getLine() + ": variable " + $var.text + " is an array");
	   					System.exit(1);
   					}
   					
   					ArrayNode arrayNode1 = (ArrayNode) node1;
   					ArrayNode arrayNode2 = (ArrayNode) node2;
   					
   					List<Integer> dimensionList1 = arrayNode1.getDimensionList();
   					List<Integer> dimensionList2 = arrayNode2.getDimensionList();
   					
   					if(dimensionList1.size() != dimensionList2.size()){
   						System.err.println("error on line " + $var1.getLine() + ": incompatible array dimensions");
   						System.exit(1);
   					}
   					
   					for(int i = 0; i < dimensionList1.size(); i++){
   						if(dimensionList1.get(i) != dimensionList2.get(i)){
   							System.err.println("error on line " + $var1.getLine() + ": incompatible array dimensions");
   							System.exit(1);
   						}
   					}
   					
   					//TODO: array to array assignment
   					
   				}else if(ArrayNode.class.isAssignableFrom(node2.getClass())){
   					System.err.println("error on line " + $var2.getLine() + ": variable " + $var2.text + " is an array");
   					System.exit(1);
   				}
   				else{
   					// regular assignment
   					Expression expr = new Expression(node2);
	    			$assign = new VarExpr(node1, expr);
   				}
   				
   				if(node1.getType() == platu.lpn.VarType.INTERNAL || node1.getType() == platu.lpn.VarType.OUTPUT){
	   				Integer varCount = VarCountMap.get($var1.text);
		    		VarCountMap.put($var1.text, ++varCount);
	    		}
    		}
    	| (var=ID '=' 
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
    			else if(!VarNodeMap.containsKey($var.text)){
    				System.err.println("error on line " + $var.getLine() + ": variable " + $var.text + " was not declared");
    				System.exit(1);
    			}
    			else if(!Outputs.contains($var.text) && !Internals.contains($var.text)){
    				System.err.println("error on line " + $var.getLine() + ": input variable " + $var.text + " cannot be assigned");
    				System.exit(1);
    			}
    		}
    	varExpr=expression
	    	{
	    		Expression expr = new Expression($varExpr.expr);
	    		VarNode node = VarNodeMap.get($var.text);
	    		if(ArrayNode.class.isAssignableFrom(node.getClass())){
   					System.err.println("error on line " + $var.getLine() + ": variable " + $var.text + " is an array");
   					System.exit(1);
   				}
   				
   				if(node.getType() == platu.lpn.VarType.INTERNAL || node.getType() == platu.lpn.VarType.OUTPUT){
	   				Integer varCount = VarCountMap.get($var.text);
		    		VarCountMap.put($var.text, ++varCount);
	    		}
	    		
	    		$assign = new VarExpr(node, expr);
	   		}
	   	';') | (arrayVar=ID 
	   		{
	   			List<ExpressionNode> indexList = new ArrayList<ExpressionNode>();
	   			
	   			// make sure only global, internal and output variables are assigned
    			if(GlobalConstHashMap.containsKey($arrayVar.text)){
    				System.err.println("error on line " + $arrayVar.getLine() + ": global constant " + $arrayVar.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(ConstHashMap.containsKey($arrayVar.text)){
    				System.err.println("error on line " + $arrayVar.getLine() + ": constant " + $arrayVar.text + " cannot be assigned");
    				System.exit(1);
    			}
    			else if(!VarNodeMap.containsKey($arrayVar.text)){
    				System.err.println("error on line " + $arrayVar.getLine() + ": variable " + $arrayVar.text + " was not declared");
    				System.exit(1);
    			}
    			else if(!Outputs.contains($arrayVar.text) && !Internals.contains($arrayVar.text)){
    				System.err.println("error on line " + $arrayVar.getLine() + ": input variable " + $arrayVar.text + " cannot be assigned");
    				System.exit(1);
    			}
	   		}
	   	('[' (arrayExpr=expression
	   		{
    			ExpressionNode node = $arrayExpr.expr;
				indexList.add(node);
	   		}
	   	) ']')+ '=' assignExpr=expression
	   		{
	    		Expression expr = new Expression($assignExpr.expr);
	    		VarNode arrayNode = VarNodeMap.get($arrayVar.text);
	    		if(ArrayNode.class.isAssignableFrom(arrayNode.getClass())){
	    			$assign = new VarExpr(new ArrayElement((ArrayNode) arrayNode, indexList), expr);
	    		}
	    		else{
	    			System.err.println("error on line " + $arrayVar.getLine() + ": " + $arrayVar.text + " is not an array");
	    			System.exit(1);
	    		}
	    		
	    		if(arrayNode.getType() == platu.lpn.VarType.INTERNAL || arrayNode.getType() == platu.lpn.VarType.OUTPUT){
	   				Integer varCount = VarCountMap.get($arrayVar.text);
		    		VarCountMap.put($arrayVar.text, ++varCount);
	    		}
	   		}
	   	';')
    ;
    
// Expressions
term returns [ExpressionNode expr, int value]
    :   var=ID 
    		{
    			if(ConstHashMap.containsKey($var.text)){
    				$value = ConstHashMap.get($var.text);
    				$expr = new ConstNode($var.text, $value);
    			}
    			else if(GlobalConstHashMap.containsKey($var.text)){
    				$value = GlobalConstHashMap.get($var.text);
    				$expr = new ConstNode($var.text, $value);
    			}
    			else if(StatevectorMap.containsKey($var.text)){ 
    				$value = StatevectorMap.get($var.text);
					$expr = VarNodeMap.get($var.text);
    			}
    			else if(GlobalVarHashMap.containsKey($var.text)){
    				$value = StatevectorMap.get($var.text);
    				$expr = VarNodeMap.get($var.text);
    			}
    			else if(VarNodeMap.containsKey($var.text)){
   					System.err.println("error on line " + $var.getLine() + ": variable " + $var.text + " is an array");
   					System.exit(1);
    			}
    			else{
					System.err.println("error on line " + $var.getLine() + ": variable " + $var.text + " is not valid");
					System.exit(1);
    			}
   			}
  	|	(array=ID 
  			{
  				List<ExpressionNode> indexList = new ArrayList<ExpressionNode>();
  				List<Integer> valueList = new ArrayList<Integer>();
  				VarNode arrayNode = null;
  				
  				if(!VarNodeMap.containsKey($array.text)){
  					System.err.println("error on line " + $array.getLine() + ": " + $array.text + " is not a valid array");
   					System.exit(1);
  				}
  				
  				arrayNode = VarNodeMap.get($array.text);
  				if(!ArrayNode.class.isAssignableFrom(arrayNode.getClass())){
   					System.err.println("error on line " + $array.getLine() + ": " + $array.text + " is not a valid array");
   					System.exit(1);
   				}
   			}
   		('[' (arrayExpr=expression
  			{
  				ExpressionNode node = $arrayExpr.expr;
  				indexList.add(node);
  				valueList.add($arrayExpr.value);
  			}
  		) ']')+
  			{
  				String name = ((ArrayNode) arrayNode).getElement(valueList).getName();
  				$value = StatevectorMap.get(name);
  				$expr = new ArrayElement((ArrayNode) arrayNode, indexList);
  			}
  		)
    |   LPAREN expression RPAREN {$expr = $expression.expr; $value = $expression.value;}
    |   INT {$value = Integer.parseInt($INT.text); $expr = new ConstNode("name", $value);}
    |   TRUE {$expr = ONE; $value = 1;}
    |   FALSE {$expr = ZERO; $value = 0;}
    ;
    
unary returns [ExpressionNode expr, int value]
    :   {boolean positive = true;}
    	('+' | ('-' {if(positive){ positive = false;} else {positive = true;}}))* term
    	{
    		if(!positive){
    			$expr = new MinNode($term.expr);
    			$value = -$term.value;
    		}
    		else{
    			$expr = $term.expr;
    			$value = $term.value;
   			}
    	}
    ;

bitwiseNegation returns [ExpressionNode expr, int value]
	: 	{boolean neg = false;}
		('~' {if(neg){neg = false;} else{neg = true;}})* unary
			{
				if(neg){
					$expr = new BitNegNode($unary.expr);
					$value = ~$unary.value;
				}
				else{
					$expr = $unary.expr;
					$value = $unary.value;
				}
			}
	;
	
negation returns [ExpressionNode expr, int value]
	:	{boolean neg = false;}
		('!' {if(neg){neg = false;} else{neg = true;}})* bitwiseNegation
			{
				if(neg){
					$expr = new NegNode($bitwiseNegation.expr);
					$value = $bitwiseNegation.value == 0 ? 1 : 0;
				}
				else{
					$expr = $bitwiseNegation.expr;
					$value = $bitwiseNegation.value;
				}
			}
	;
	
mult returns [ExpressionNode expr, int value]
    :   op1=negation {$expr = $op1.expr; $value = $op1.value;} 
    	(	'*' op2=negation {$expr = new MultNode($expr, $op2.expr); $value = $value * $op2.value;}
    	|	'/' op2=negation {$expr = new DivNode($expr, $op2.expr); $value = $value / $op2.value;}
    	|	'%' op2=negation {$expr = new ModNode($expr, $op2.expr); $value = $value \% $op2.value;}
    	)*
    ;
    
add returns [ExpressionNode expr, int value]
    :   op1=mult {$expr = $op1.expr; $value = $op1.value;}
    	(	'+' op2=mult {$expr = new AddNode($expr, $op2.expr); $value = $value + $op2.value;}
    	| 	'-' op2=mult {$expr = new SubNode($expr, $op2.expr); $value = $value - $op2.value;}
    	)*
    ;
    
shift returns [ExpressionNode expr, int value]
    :   op1=add {$expr = $op1.expr; $value = $op1.value;}
    	(	'<<' op2=add {$expr = new LeftShiftNode($expr, $op2.expr); $value = $value << $op2.value;}
    	| 	'>>' op2=add {$expr = new RightShiftNode($expr, $op2.expr); $value = $value >> $op2.value;}
    	)*
    ;

relation returns [ExpressionNode expr, int value]
    :   op1=shift {$expr = $op1.expr; $value = $op1.value;}
    	(	'<' op2=shift {$expr = new LessNode($expr, $op2.expr); $value = ($value < $op2.value) ? 1 : 0;}
    	| 	'<=' op2=shift {$expr = new LessEqualNode($expr, $op2.expr); $value = ($value <= $op2.value) ? 1 : 0;}
    	| 	'>=' op2=shift {$expr = new GreatEqualNode($expr, $op2.expr); $value = ($value >= $op2.value) ? 1 : 0;}
    	| 	'>' op2=shift {$expr = new GreatNode($expr, $op2.expr); $value = ($value > $op2.value) ? 1 : 0;}
    	)*
    ;
    
equivalence returns [ExpressionNode expr, int value]
    :   op1=relation {$expr = $op1.expr; $value = $op1.value;}
    	(	'==' op2=relation {$expr = new EquivNode($expr, $op2.expr); $value = ($value == $op2.value) ? 1 : 0;}
    	|	'!=' op2=relation {$expr = new NotEquivNode($expr, $op2.expr); $value = ($value != $op2.value) ? 1 : 0;}
    	)*
    ;
    
bitwiseAnd returns [ExpressionNode expr, int value]
    :   op1=equivalence {$expr = $op1.expr; $value = $op1.value;} 
    	(	'&' op2=equivalence {$expr = new BitAndNode($expr, $op2.expr); $value = $value & $op2.value;}
    	)*
    ;
    
bitwiseXor returns [ExpressionNode expr, int value]
    :   op1=bitwiseAnd {$expr = $op1.expr; $value = $op1.value;}
    	(	'^' op2=bitwiseAnd {$expr = new BitXorNode($expr, $op2.expr); $value = $value ^ $op2.value;}
    	)*
    ;
    
bitwiseOr returns [ExpressionNode expr, int value]
    :   op1=bitwiseXor {$expr = $op1.expr; $value = $op1.value;}
    	(	'|' op2=bitwiseXor {$expr = new BitOrNode($expr, $op2.expr); $value = $value | $op2.value;}
    	)*
    ;
    
and returns [ExpressionNode expr, int value]
    :   op1=bitwiseOr {$expr = $op1.expr; $value = $op1.value;}
    	(	'&&' op2=bitwiseOr {$expr = new AndNode($expr, $op2.expr); $value = ($value != 0 && $op2.value != 0) ? 1 : 0;}
    	)*
    ;
    
or returns [ExpressionNode expr, int value]
    :   op1=and {$expr = $op1.expr; $value = $op1.value;}
    	(	'||' op2=and {$expr = new OrNode($expr, $op2.expr); $value = ($value != 0 || $op2.value != 0) ? 1 : 0;}
    	)*
    ;
    
implication returns [ExpressionNode expr, int value]
    :	op1=or {$expr = $op1.expr; $value = $op1.value;}
    	(	'->' op2=or  {$expr = new ImplicationNode($expr, $op2.expr); $value = ($value == 0 || $op2.value != 0) ? 1 : 0;}
    	)*
    ;
    
expression returns [ExpressionNode expr, int value]
    :   op1=implication {$expr = $op1.expr; $value = $op1.value;}
    	('?' op2=expression ':' op3=expression 
    		{
    			$value = ($value != 0) ? $op2.value : $op3.value;
    			$expr = new TernaryNode($expr, $op2.expr, $op3.expr);
    		}
    	)?
    ;
    
//CTL formula
ctlTerm
	:	ID
	|	LPAREN ctl LPAREN
	;
	
ctl
	:
	;
	

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

//CLOSE_INCLUDE : '<' '/include' '>';
//CLOSE_CONST : '<' '/const' '>';
//CLOSE_VAR : '<' '/var' '>';
//CLOSE_MODULE : '<' '/mod' '>';
//CLOSE_CLASS : '<' '/class' '>';
//CLOSE_MARKING : '<' '/marking' '>';
//CLOSE_TRANSITION : '<' '/transition' '>';

fragment LETTER: ('a'..'z' | 'A'..'Z');
fragment DIGIT: '0'..'9';
fragment FILE: (LETTER | DIGIT) ('_'? (LETTER | DIGIT))*;
INT: '-'? DIGIT+;
ID: LETTER (UNDERSCORE? (LETTER | DIGIT))*;
PATH: ((LETTER ':') | '/')? (FILE ('/' | '\\'))* FILE '.lpn';
MEMBER: ID '.' ID;
WS: (' ' | '\t' | '\n' | '\r' | '\f')+ {$channel = HIDDEN;};
COMMENT: '//' .* ('\n' | '\r') {$channel = HIDDEN;};
MULTILINECOMMENT: '/*' .* '*/' {$channel = HIDDEN;};
XMLCOMMENT: ('<' '!' '-' '-') .* ('-' '-' '>') {$channel = HIDDEN;};
IGNORE: '<' '?' .* '?' '>' {$channel = HIDDEN;};

