package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import VerilogConstructs.AbstractVerilogConstruct;
import VerilogConstructs.VerilogAlwaysBlock;
import VerilogConstructs.VerilogAssignment;
import VerilogConstructs.VerilogBlock;
import VerilogConstructs.VerilogConditional;
import VerilogConstructs.VerilogDelay;
import VerilogConstructs.VerilogInitialBlock;
import VerilogConstructs.VerilogModule;
import VerilogConstructs.VerilogModuleInstance;
import VerilogConstructs.VerilogWait;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001BaseListener;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Always_constructContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Binary_operatorContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Blocking_assignmentContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Conditional_statementContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Delay_controlContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.ExpressionContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Function_callContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Function_declarationContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Hierarchical_identifierContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Initial_constructContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Input_declarationContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.List_of_net_identifiersContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.List_of_port_connectionsContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Module_declarationContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Module_instanceContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Module_instantiationContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Named_port_connectionContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Net_assignmentContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Net_identifierContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.NumberContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Ordered_port_connectionContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Output_declarationContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Port_identifierContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Reg_declarationContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Seq_blockContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.System_function_callContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Unary_operatorContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Variable_typeContext;
import edu.utah.ece.async.ibiosim.synthesis.Verilog2001Parser.Wait_statementContext;



/**
 * Parses a Verilog module to a structured format.
 * 
 * @author Tramy Nguyen
 */
public class VerilogParser extends Verilog2001BaseListener{

	private VerilogModule verilogModule;  
	private Stack<AbstractVerilogConstruct> constructCtx;

	private LinkedList<String> operators; //name of variables for assignments or conditional expression
	private int operatorSize;
	
	
	public VerilogParser() {
		verilogModule = new VerilogModule();
		constructCtx = new Stack<>();
		operators = new LinkedList<>();
		operatorSize = 0;
	}

	/**
	 * A structured verilog that was parsed for the given verilog file and all information found within the module.
	 * @return The verilog module. 
	 */
	public VerilogModule getVerilogModule() {
		return this.verilogModule;
	}

	@Override
	public void enterModule_declaration(Module_declarationContext ctx) {

		String moduleId = ctx.module_identifier().getText();
		this.verilogModule.setId(moduleId);
	}

	public void enterModule_instantiation(Module_instantiationContext ctx){
		String moduleRefName = ctx.module_identifier().getText();
		for(Module_instanceContext moduleRef : ctx.module_instance()) {
			VerilogModuleInstance instance = verilogModule.createVerilogModuleInstance();

			String moduleInstanceName = moduleRef.name_of_instance().getText();

			instance.setSubModuleId(moduleInstanceName);
			instance.setModuleReference(moduleRefName);
			List_of_port_connectionsContext ports = moduleRef.list_of_port_connections();

			if(ports.ordered_port_connection() != null && ports.ordered_port_connection().size() >0) {
				for(Ordered_port_connectionContext portCtx : ports.ordered_port_connection()) {
					instance.addOrderedConnections(portCtx.getText());
				}
			} 
			else if(ports.named_port_connection() != null && ports.named_port_connection().size() >0) {
				for(Named_port_connectionContext portCtx : ports.named_port_connection()) {
					instance.addNamedConnection(portCtx.expression().getText(), portCtx.port_identifier().getText());
				}
			}
		}
	}
	
	public void exitModule_instantiation(Module_instantiationContext ctx){
		operators.clear();
	}

	
	@Override
	public void enterList_of_net_identifiers(List_of_net_identifiersContext ctx){ 

		for(Net_identifierContext wireName : ctx.net_identifier()) {
			this.verilogModule.addWire(wireName.getText());
		}
	}

	@Override
	public void enterInput_declaration(Input_declarationContext ctx) {
		List<Port_identifierContext> portNames = ctx.list_of_port_identifiers().port_identifier();

		for(Port_identifierContext portNameCtx : portNames) {
			String portName = portNameCtx.identifier().Simple_identifier().toString();
			this.verilogModule.addInputPort(portName);
		}
	}

	@Override
	public void enterOutput_declaration(Output_declarationContext ctx) {
		List<Port_identifierContext> portNames = ctx.list_of_port_identifiers().port_identifier();

		for(Port_identifierContext portNameCtx : portNames) {
			String portName = portNameCtx.identifier().Simple_identifier().toString();
			this.verilogModule.addOutputPort(portName);
		}
	}

	@Override
	public void enterReg_declaration(Reg_declarationContext ctx) {
		List<Variable_typeContext> typeContexts = ctx.list_of_variable_identifiers().variable_type();

		for(Variable_typeContext typeContext : typeContexts) {
			String variableName = typeContext.variable_identifier().identifier().Simple_identifier().toString();
			this.verilogModule.addRegister(variableName);
		}
	}

	@Override
	public void enterInitial_construct(Initial_constructContext ctx) {
		// It is possible to have multiple initial blocks in verilog so let's keep track of each initial block 
		// to know which assignments belong in which block.
		VerilogInitialBlock initialBlock = this.verilogModule.createInitialBlock();
		this.constructCtx.push(initialBlock);
	}

	@Override
	public void exitInitial_construct(Initial_constructContext ctx) {
		if(!this.constructCtx.isEmpty()) {
			this.constructCtx.pop();
		}
	}
	
	@Override
	public void enterNet_assignment(Net_assignmentContext ctx) {
		VerilogAssignment contAssign = new VerilogAssignment();
		constructCtx.push(contAssign);
		this.verilogModule.addContinousAssignment(contAssign);
	}
	
	@Override
	public void exitNet_assignment(Net_assignmentContext ctx){
		if(!this.constructCtx.isEmpty()) {
			AbstractVerilogConstruct construct = this.constructCtx.pop();
			assert(construct instanceof VerilogAssignment);
			VerilogAssignment assignment = (VerilogAssignment) construct;
			String variable = operators.removeFirst();
			String expression = infixToPrefix();
			assignment.setVariable(variable);
			assignment.setExpression(expression);
		}
	}

	@Override
	public void enterAlways_construct(Always_constructContext ctx) {
		// It is possible to have multiple always block in verilog 
		VerilogAlwaysBlock alwaysBlock = this.verilogModule.createAlwayBlock();
		this.constructCtx.push(alwaysBlock);
	}

	@Override
	public void exitAlways_construct(Always_constructContext ctx) {
		if(!this.constructCtx.isEmpty()) {
			this.constructCtx.pop();
		}
	}
	
	@Override
	public void enterHierarchical_identifier(Hierarchical_identifierContext ctx) { 
		operators.add(ctx.getText());
	}


	@Override
	public void enterUnary_operator(Unary_operatorContext ctx) { 
		operators.add(ctx.getText());
	}


	@Override
	public void enterBinary_operator(Binary_operatorContext ctx) {
		operators.add(ctx.getText());
	}
	
	@Override
	public void enterNumber(NumberContext ctx) { 
		if(ctx.Binary_number() != null) {
			String[] binaryRepresentation = ctx.Binary_number().getText().split("'b");
			int value = Integer.parseInt(binaryRepresentation[1], 2);
			operators.add(String.valueOf(value));
		}
		else if(ctx.Decimal_number() != null) {
			operators.add(ctx.getText());
		}
	}

	@Override
	public void enterBlocking_assignment(Blocking_assignmentContext ctx) {
		VerilogAssignment blockAssignment = new VerilogAssignment();
		if(!constructCtx.isEmpty()) {
			AbstractVerilogConstruct currentConstruct = constructCtx.peek();
			if(currentConstruct instanceof VerilogBlock) {
				VerilogBlock block = (VerilogBlock) currentConstruct;
				block.addConstruct(blockAssignment);
				constructCtx.push(blockAssignment);
			} else if(currentConstruct instanceof VerilogConditional) {
				VerilogConditional conditionalBlock = (VerilogConditional) currentConstruct;
				
				if(conditionalBlock.getIfCondition() == null) {
					String expression = infixToPrefix();
					conditionalBlock.setIfCondition(expression);
				}
				conditionalBlock.addConstruct(blockAssignment);
				constructCtx.push(blockAssignment);
			}
		}

	}

	@Override
	public void exitBlocking_assignment(Blocking_assignmentContext ctx) {
		if(!this.constructCtx.isEmpty()) {
			AbstractVerilogConstruct construct = this.constructCtx.pop();
			assert(construct instanceof VerilogAssignment);
			VerilogAssignment assignment = (VerilogAssignment) construct;
			String variable = operators.removeFirst();
			String expression = infixToPrefix();
			assignment.setVariable(variable);
			assignment.setExpression(expression);
		}
	}

	@Override
	public void enterExpression(ExpressionContext ctx){
		this.operators.add("(");
	}
	
	@Override
	public void exitExpression(ExpressionContext ctx){
		this.operators.add(")");
		
	}
	
	@Override 
	public void enterWait_statement(Wait_statementContext ctx) { 

		if(!constructCtx.isEmpty()) {
			AbstractVerilogConstruct currentConstruct = constructCtx.peek();
			
			if(currentConstruct instanceof VerilogConditional) {
				VerilogConditional conditionalBlock = (VerilogConditional) currentConstruct;
				
				if(conditionalBlock.getIfCondition() == null) {
					String expression = infixToPrefix();
					conditionalBlock.setIfCondition(expression);
				}
			}
			
			VerilogWait waitStatement = new VerilogWait();
			currentConstruct.addConstruct(waitStatement);
			constructCtx.push(waitStatement);

		}
	}

	@Override 
	public void exitWait_statement(Wait_statementContext ctx) { 
		if(!this.constructCtx.isEmpty()) {
			AbstractVerilogConstruct construct = this.constructCtx.pop();
			
			String expression = infixToPrefix();
			VerilogWait waitStatement = (VerilogWait) construct;
			waitStatement.setWaitExpression(expression);
		}
	}

	@Override
	public void enterDelay_control(Delay_controlContext ctx){ 
		if(!constructCtx.isEmpty()) {
			VerilogDelay delayConstruct = new VerilogDelay();
			delayConstruct.setDelayValue(Integer.parseInt(ctx.delay_value().getText()));
			
			AbstractVerilogConstruct currentConstruct = constructCtx.peek();
			currentConstruct.addConstruct(delayConstruct);
		}
	}

	@Override
	public void enterSeq_block(Seq_blockContext ctx){ 
		// enterSeq_block will return all nested assignments and statements within a begin and end block 
		// of a conditional statement. 
		VerilogBlock block = new VerilogBlock();
		if(!constructCtx.isEmpty()) {
			AbstractVerilogConstruct currentConstruct = constructCtx.peek();
			if(currentConstruct instanceof VerilogConditional) {
				VerilogConditional conditionalBlock = (VerilogConditional) currentConstruct;
				
				if(conditionalBlock.getIfCondition() == null) {
					String expression = infixToPrefix();
					conditionalBlock.setIfCondition(expression);
				}
			}
			currentConstruct.addConstruct(block);
			constructCtx.push(block);

		}
	}

	@Override
	public void exitSeq_block(Seq_blockContext ctx){ 
		if(!this.constructCtx.isEmpty()) {
			this.constructCtx.pop();
		}
	}

	@Override
	public void enterConditional_statement(Conditional_statementContext ctx){ 
		VerilogConditional conditional = new VerilogConditional();
		if(!constructCtx.isEmpty()) {
			// Conditional statements can be attached to any constructs so there is no need to check for
			// a specific construct to attach the conditional statements to.
			AbstractVerilogConstruct currentConstruct = constructCtx.peek();
			currentConstruct.addConstruct(conditional);
			constructCtx.add(conditional);
		}
	}

	@Override
	public void exitConditional_statement(Conditional_statementContext ctx){ 
		if(!this.constructCtx.isEmpty()) {
			this.constructCtx.pop();
		}

	}
	
	
	public void exitFunction_declaration(Function_declarationContext ctx) {
		System.out.println("141 " + ctx.getText());
		operators.clear();
	}

	@Override
	public void enterFunction_call(Function_callContext ctx) {
		System.out.println("461 " + ctx.getText()); 
		operatorSize = this.operators.size();
	}
	
	@Override
	public void exitFunction_call(Function_callContext ctx) {
		System.out.println(ctx.getText()); 
		while(this.operators.size() > this.operatorSize) {
			this.operators.removeLast();
		}
		this.operators.addLast(ctx.getText());
	}
	
	@Override 
	public void enterSystem_function_call(System_function_callContext ctx){ 
		String function = ctx.getText();
		switch(function) {
		case "$random":
			operators.add("uniform(0," + Integer.MAX_VALUE + ")");
			break;
		}

	}

	private int getPrecedence(String operator) {
		switch(operator) {
		case "!":
		case "~":
			return -1;
		case "&":
		case "^":
			return -2;
		case "+":
		case "-":
			return -3;
		case "*":
		case "\\":
		case "%":
			return -4;
		case "==":
		case "!=":
			return -5;
		case "|":
			return -6;
		case "&&":
			return -7;
		case "||":
			return -8;
		case "(":
		case ")":
			return Integer.MIN_VALUE;
		default:
			return 1;
		}
	}
	
	private LinkedList<String> infixToPostfix() {
		Stack<String> char_stack = new Stack<>();
		LinkedList<String> output = new LinkedList<>();

		while(!operators.isEmpty()) {
			String operand = operators.removeFirst();
			int precedence = getPrecedence(operand);

			if(precedence > 0) {
				convertToSBMLFunctions(operand, output);
			} 
			else if(operand.equals("(")) {
				char_stack.push(operand);
			} 
			else if(operand.equals(")")) {
				while (!char_stack.peek().equals("(")) {
					String op = char_stack.pop();
					convertToSBMLFunctions(op, output);
				}
				// Remove '(' from the stack
				char_stack.pop(); 
			} 
			else {
				if (getPrecedence(char_stack.peek()) <= -1) {
					while (char_stack.size() > 0 && getPrecedence(operand) <= getPrecedence(char_stack.peek())) {
						String op = char_stack.pop();
						convertToSBMLFunctions(op, output);
					}
					// Push current Operator on stack
					char_stack.push(operand);
				}

			}
		}
		return output;
	}
	
	private void convertToSBMLFunctions(String op, LinkedList<String> output) {
		String operand1, operand2;
		switch(op) {
		case "!":
		case "~":
			operand1 = output.removeLast();
			output.addLast("not" + "(" + operand1 + ")");
			break;
		case "&":
		case "&&":
			operand1 = output.removeLast();
			operand2 = output.removeLast();
			output.addLast("and" + "(" + operand1 + "," +operand2 + ")");
			break;
		case "|":
		case "||":
			operand1 = output.removeLast();
			operand2 = output.removeLast();
			output.addLast("or" + "(" + operand1 + "," +operand2 + ")");
			break;
		case "^":
			operand1 = output.removeLast();
			operand2 = output.removeLast();
			output.addLast("xor" + "(" + operand1 + "," +operand2 + ")");
			break;
		case "==":
			operand1 = output.removeLast();
			operand2 = output.removeLast();
			output.addLast("eq" + "(" + operand1 + "," +operand2 + ")");
			break;
		case "!=":
			operand1 = output.removeLast();
			operand2 = output.removeLast();
			output.addLast("neq" + "(" + operand1 + "," + operand2 + ")");
			break;
		case "%":
			operand1 = output.removeLast();
			operand2 = output.removeLast();
			//output.addLast("mod" + "(" + operand1 + "," +operand2 + ")");
			output.addLast("piecewise(1, uniform(0,1)  < 0.5, 0)");
			break;
		default:
			output.add(op);
		}
		
	}

	private String infixToPrefix()
	{
	    /* Reverse String
	     * Replace ( with ) and vice versa
	     * Get Postfix
	     * Reverse Postfix  */
	    int l = this.operators.size();
	 
	    Collections.reverse(operators);
	 
	    // Replace ( with ) and vice versa
	    for (int i = 0; i < l; i++) {
	 
	        if (operators.get(i).equals("(")) {
	        	operators.set(i,")");
	        }
	        else if (operators.get(i).equals(")")) {
	        	operators.set(i,"(");
	        }
	    }
	 
	    LinkedList<String> prefixMath = infixToPostfix();
	    
	    return prefixMath.pop();
	}
}