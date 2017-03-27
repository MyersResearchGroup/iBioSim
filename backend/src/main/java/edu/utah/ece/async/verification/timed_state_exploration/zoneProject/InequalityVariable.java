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
package main.java.edu.utah.ece.async.verification.timed_state_exploration.zoneProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import main.java.edu.utah.ece.async.lpn.parser.ExprTree;
import main.java.edu.utah.ece.async.lpn.parser.LPN;
import main.java.edu.utah.ece.async.lpn.parser.Transition;
import main.java.edu.utah.ece.async.lpn.parser.Variable;
import main.java.edu.utah.ece.async.verification.platu.platuLpn.DualHashMap;
import main.java.edu.utah.ece.async.verification.platu.stategraph.State;
import main.java.edu.utah.ece.async.verification.timed_state_exploration.octagon.Equivalence;


/**
 * An InequalityVariable is a Boolean variable for an inequality expression involving 
 * continuous variables. It extends lpn.parser.Variable. 
 * @author Andrew N. Fisher
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class InequalityVariable extends Variable {

	
	/*
	 * Representation Invariant :
	 * 		To create a canonical name for the variable, the name should be the string
	 * 		representation of the defining ExprTree pre-appended by a '$' to help
	 * 		avoid the possibility of name collision. Thus the variable for "x>6" is
	 * 		given the name '$x>6'. 
	 */
	
	/* Holds the defining expression for a boolean value derived from an inequality. */
	private ExprTree _inequalityExprTree;
	
	/* The continuous variable that this InequalityVariable depends on.*/
	//String _variable;
	//Variable _variable;
	private ArrayList<Variable> _variables;
	
	/* 
	 * Holds the Transitions that have this inequality in their enabling
	 * condition.
	 */
	private HashSet<Transition> _transitions;
	
	/* The LhpnFile object that this InequalityVariable belongs to. */
	private LPN _lpn;
	
	/* 
	 * The constant. Note: getConstant will set this variable. 
	 */
	private Integer _constant;
	
	/*
	 * True if constant is on the right; false otherwise.
	 */
	private boolean _isRight;
	
	// Cached hash code value.
	int _hashCode;
	
	/**
	 * Overrides the hashCode.
	 */
	@Override
	public int hashCode()
	{
		// Check if the hash code has been set.
		if(_hashCode <0)
		{
			_hashCode = createHashCode();
		}

		return _hashCode;
	}
	
	/**
	 * Creates a hash code for an EventSet object.
	 * @return
	 * 		The hash code.
	 */
	private int createHashCode()
	{
		int prime = 31;
		
		int newHashCode = prime;
		
		String name = toString();
		
		for(char c : name.toCharArray()){
			newHashCode *= (int) c;
		}
		
		return Math.abs(newHashCode);
	}
	
//	
//	Not needed anymore since the list of InequalityVariables is not dynamically
//	changing.
//	
//	/* 
//	 * Keeps track of the Transitions that currently reference this InequalityVariable.
//	 */
//	//HashSet<Transition> referencingTransitions;
//	int _referenceCount;
	
	/**
	 * Not supported.
	 * @param name
	 * @param type
	 * @param initCond
	 */
	public InequalityVariable(String name, String type, Properties initCond) {
		super(name, type, initCond);
		
		throw new UnsupportedOperationException("This constructor needs to be verified"
				+ "for correctness for inherited class InequalityVariable.");
	}

	/**
	 * Not supported.
	 * @param name
	 * @param type
	 * @param initValue
	 * @param port
	 */
	public InequalityVariable(String name, String type, String initValue,
			String port) {
		super(name, type, initValue, port);
		
		throw new UnsupportedOperationException("This constructor needs to be verified"
				+ "for correctness for inherited class InequalityVariable.");
	}

	/**
	 * Creates an InequalityVariable with a given name, initial value and defining
	 * expression tree.
	 * @param name
	 * 		The name (or ID) of the variable.
	 * @param initValue
	 * 		The initial value of the variable. Note : Inequality variables are
	 * 		a type of boolean variable. So this should be "true" or "false".
	 * @param ET
	 * 		An expression tree that defines the boolean value. This tree should
	 * 		represent a relational operator.
	 */
	public InequalityVariable(String name, String initValue, ExprTree ET, LPN lpn) {
		super(name, BOOLEAN, initValue);
		
		// Check if the name starts with a '$'. If not, yell.
		if(!name.startsWith("$")){
			throw new IllegalArgumentException("InequaltiyVariables' name"
					+ "must start with '$'");
		}
		
		// Declare the new boolean variable an internal signal.
		setPort(INTERNAL);
		
		// Set the defining expression.
		_inequalityExprTree = ET;
		
		// Set the containing LPN.
		_lpn = lpn;
		
		// Initialize the the variable list.
		_variables = new ArrayList<Variable>();
		
		// Initialize the transition list.
		_transitions = new HashSet<Transition>();
		// Extract the variable.
//		String contVariableName = "";
//		
//		if(ET.getLeftChild().containsCont()){
//			contVariableName = ET.getLeftChild().toString();
//		}
//		else{
//			contVariableName = ET.getRightChild().toString();
//		}
//		
//		_variable = lpn.getVariable(contVariableName);
		
		// Register this Inequality with the continuous variable.
//		
//		
//		Reference counts are not needed anymore since the set of 
//		Boolean variables is not dynamically changing.
//		
//		// When created, an expression refers to this variable.
//		_referenceCount = 1;
		
		// Populate the variables member field and register the 
		// this InequalityVariable with the continuous variables
		// it references.
		initializeContinuous();
		
	}
	
	

	/**
	 * @param name
	 * @param type
	 */
	public InequalityVariable(String name, String type) {
		super(name, type);
		
		throw new UnsupportedOperationException("This constructor needs to be verified"
				+ "for correctness for inherited class InequalityVariable.");
	}
	
	/**
	 * Overrides the toString method of Variable. Removes the pre-appended
	 * '$' of the InequalityVariable name.
	 */
	@Override
	public String toString(){
		//return "Inequality Variable : " + getName().substring(1);
		return getName().substring(1);
	}
	
	/**
	 * Override the equals method.
	 */
	@Override
	public boolean equals(Object var){
		/*
		 * Due to the fact that twoString returns the name without the '$' sign
		 * the equals method for variable will not work since it will end up 
		 * comparing the name without the dollar sign to the name with.
		 * So we need to correct the equals to use the full name. It should
		 * be considered whether the equals method the Variable class should
		 * just be changed to type checking and then use the name subfield.
		 */
		
		if(!(var instanceof InequalityVariable)){
			return false;
		}
		
		// Cast the variable
		InequalityVariable ineqvar = (InequalityVariable) var;
		return this.name.equals(ineqvar.name);
	}
	
//	
//	This is no longer needed since variables will not dynamically change.
//	
//	/**
//	 * Increase the count of how many expressions refer to this InequalityVariable.
//	 */
//	public void increaseCount(){
//		_referenceCount++;
//	}
//	
//	/**
//	 * Decreases the count of how many expressions refer to this InequalityVariable.
//	 */
//	public void decreaseCount(){
//		_referenceCount--;
//	}
//
//	/**
//	 * Returns the count of the number of expressions referring to this
//	 * InequalityVariable.
//	 * @return
//	 * 		The count recorded for how many expressions refer to this InequalityVariable.
//	 */
//	public int getCount(){
//		return _referenceCount;
//	}
	
	/**
	 * Returns false. InequalityVaribles are dealt with separately.
	 */
	@Override
	public boolean isInput() {
		return false;
	}
	
	/**
	 * Returns false. InequalityVaribles are dealt with separately.
	 */
	@Override
	public boolean isOutput() {
		return false;
	}

	/**
	 * Returns false. InequalityVaribles are dealt with separately.
	 */
	@Override
	public boolean isInternal() { 
		return false;
	}
	
	/**
	 * Evaluates the inequality according to the current state and zone.
	 * @param localState
	 * 		The current state.
	 * @param z
	 * 		The zone containing the value of the continuous variable.
	 */
	//public void update(Zone z){
//	public String evaluateInequality(State localState, Zone z){
//		
//		//This method ignores the case where the range of the continuous variable
//		// stradles the bound.
//		
//		//
//		String result = "";
//		
//		/*
//		 *  Extract the current values of the (Boolean and Integer) variables to be able
//		 *  to obtain the value of the expression side of the inequality. This
//		 *  may need to be changed when the bound evaluator is created and ranges are
//		 *  allowed for the Integer variables.
//		 */
//		HashMap<String, String> variableValues = _lpn.getAllVarsWithValuesAsString(localState.getVector());
//		
//		// Determine which side of the expression tree has the continuous variable.
//		if(_inequalityExprTree.getLeftChild().containsVar(_variable.getName())){
//			// Extract the value of the expression side of the inequality.
//			int expressionValue = (int) _inequalityExprTree.getRightChild().evaluateExpr(variableValues);
//			
//			// Determine which type of inequality.
//			String op = _inequalityExprTree.getOp();
//			
//			if(op.equals("<") || op.equals("<=")){
////				if(z.getUpperBoundbyContinuousVariable(_variable) <= expressionValue){
////					this.initValue = "true";
////				}
////				else
////				{
////					this.initValue = "false";
////				}
//
////				this.initValue = z.getUpperBoundbyContinuousVariable(_variable) <= expressionValue
////						? "true" : "false"; 
//				
//				result = z.getUpperBoundbyContinuousVariable(_variable.getName(), _lpn)
//						<= expressionValue ? "true" : "false";
//			}
//			else{
////				this.initValue = z.getLowerBoundbyContinuousVariable(_variable) >= expressionValue
////						? "true" : "false";
//				
//				result = z.getLowerBoundbyContinuousVariable(_variable.getName(), _lpn) 
//						>= expressionValue ? "true" : "false";
//			}
//			
//		}
//		else{
//			// Extract the value of the expression side of the inequality.
//			int expressionValue = (int) _inequalityExprTree.getLeftChild().evaluateExpr(variableValues);
//			
//			// Determine which type of inequality.
//
//			String op = _inequalityExprTree.getOp();
//			
//			if(op.equals("<") || op.equals("<=")){
////				if(expressionValue <= z.getLowerBoundbyContinuousVariable(_variable)){
////					this.initValue = "true";
////				}
////				else
////				{
////					this.initValue = "false";
////				}
//				
////				this.initValue = expressionValue <= z.getLowerBoundbyContinuousVariable(_variable)
////						? "true" : "false"; 
//				
//				result = expressionValue <= 
//						z.getLowerBoundbyContinuousVariable(_variable.getName(), _lpn)
//						? "true" : "false"; 
//			}
//			else{
////				this.initValue = expressionValue >= z.getUpperBoundbyContinuousVariable(_variable)
////						? "true" : "false";
//				
//				result = expressionValue >= 
//						z.getUpperBoundbyContinuousVariable(_variable.getName(), _lpn)
//						? "true" : "false";
//			}
//			
//		}
//		
//		return result;
//		
//	}
	
	/**
	 * Evaluates the inequality based on the current state and zone.
	 * @param localState
	 * 			The current (local) state.
	 * @param z
	 * 			The current zone.
	 * @return
	 * 			Zero if the inequality is false, a non-zero number if the
	 * 			inequality is true.
	 * @throws
	 * 			IllegalStateException if the inequality cannot be evaluated to
	 * 			true or false.
	 */
//	public int evaluate(State localState, Zone z){
	public int evaluate(State localState, Equivalence z){
	
//		// From the (local) state, extract the current values to use in the 
//		// evaluator.
//		HashMap<String, String> values =
//				_lpn.getAllVarsWithValuesAsString(localState.getVector());
//		
//		// Evaluate the defining expression tree.
//		IntervalPair range = _inequalityExprTree.evaluateExprBound(values, z);
//		
//		// Check that the upper and lower bounds agree (indicating that a single
//		// value was return instead of "don't know").
//		if(range.get_LowerBound() != range.get_UpperBound()){
//			// If a range of values (indicating a don't know condition) was
//			// returned, yell.
//			throw new IllegalStateException("When evaluating " + this +
//					 " on the local state " + localState +
//					 " with the zone " + z +
//					 " the result was \"don't know\", that is the " +
//					 " upper and lower bounds of the returned boolean " +
//					 " do not agree.");
//		}
//		
//		// If the upper and lower bounds agreed, then return that value.
//		
//		return range.get_LowerBound();
		
		
		// Due to the strange way that inequalities are evaluated
		// if the upper and lower bounds are equal to the
		// the constant, then the rate determines the value
		// of the inequality.
		
		// Get the variable
		Variable v = _variables.get(0);
		
		int lpnIndex = _lpn.getLpnIndex();
		
		int varIndex = _lpn.getContVarIndex(v.getName());
		
		LPNContinuousPair ltPair = new LPNContinuousPair(lpnIndex,
				varIndex);
		
		
		IntervalPair range = z.getContinuousBounds(v.getName(), _lpn);
		
		if(range.singleValue() &&
				range.get_LowerBound() == this.getConstant()){
			// The upper and lower bounds are equal to the constant
			// so the value of the inequality is determined by
			// the rate.
			
			int rate = z.getCurrentRate(ltPair);
			
			if( rate == 0){
				// Get the inequality variable index.
				int ineqIndex = _lpn.getVariableIndex(this.name);
				
				return localState.getVariableVector()[ineqIndex];
			}
			
			boolean isPositive = rate > 0;
			
			boolean result = isGreaterThan() == isPositive;
			
			return result ? 1 : 0;
		}
		
		return evaluate(localState.getVariableVector(), z, null);
	}
	
	/**
	 * Evaluates the inequality based on the current state and zone.
	 * @param vector
	 * 			The current value of the variables in the state.
	 * @param z
	 * 			The current zone.
	 * @return
	 * 			Zero if the inequality is false, a non-zero number if the
	 * 			inequality is true.
	 * @throws
	 * 			IllegalStateException if the inequality cannot be evaluated to
	 * 			true or false.
	 */
//	public int evaluate(int[] vector, Zone z, HashMap<LPNContAndRate, IntervalPair> continuousValues){
	public int evaluate(int[] vector, Equivalence z, HashMap<LPNContAndRate, IntervalPair> continuousValues){
		
		// Extract the continuous variable from the zone.
		
		// Get the variable for the inequality.
		//Variable v = _variables.get(0);
		
		//int lpnIndex = _lpn.getLpnIndex();
		
		//int varIndex = _lpn.getContVarIndex(v.toString());
		
		
		
		// From the (local) state, extract the current values to use in the 
		// evaluator.
		HashMap<String, String> values =
				_lpn.getAllVarsWithValuesAsString(vector);
		
		// Evaluate the defining expression tree.
		IntervalPair range = _inequalityExprTree.evaluateExprBound(values, z, continuousValues);
		
		// Check that the upper and lower bounds agree (indicating that a single
		// value was return instead of "don't know").
		if(range.get_LowerBound() != range.get_UpperBound()){
			// If a range of values (indicating a don't know condition) was
			// returned, yell.
			throw new IllegalStateException("When evaluating " + this +
					 " on the vector " + vector +
					 " with the zone " + z +
					 " the result was \"don't know\", that is the " +
					 " upper and lower bounds of the returned boolean " +
					 " do not agree.");
		}
		
		// If the upper and lower bounds agreed, then return that value.
		
		return range.get_LowerBound();
	}
	
	/**
	 * Get the type of inequality this variable represents.
	 * @return
	 * 		Strings "<", "<=", ">", ">=" depending on the type of inequality
	 * 		variable that this represents.
	 */
	public String get_op(){
		return _inequalityExprTree.getOp();
	}
	
	/**
	 * This method returns the constant of an inequality where one side
	 * evaluates to only a constant. It also populates the _isRigth
	 * field.
	 * @return
	 * 		The constant on one side of the inequality.
	 */
	public int getConstant(){
		
		if(_constant != null){
			return _constant;
		}
		
		// Find which side has the variable.
		if(_inequalityExprTree.getLeftChild().containsCont()){
			// Evaluate the other side. Note : since the assumption
			// is that one side evaluates to a constant, no
			// HashMap<String, String> should be needed for the 
			// evaluation, nor a zone.
			
			IntervalPair result = _inequalityExprTree.getRightChild()
					.evaluateExprBound(null, null, null);
			
			// Check that the bounds are the same.
			if(!result.singleValue()){
				// Scream! Something has gone wrong.
				throw new IllegalArgumentException("The InequalityVariable." +
						"getConstant() method evaluated to a non-trivial range.");
			}
			
			_constant = result.get_LowerBound();
			_isRight = true;
			
			// Either the lower or upper bound will be fine to return.
			return result.get_LowerBound();
		}
		else if(_inequalityExprTree.getRightChild().containsCont()){
			// Evaluate the other side. Note : since the assumption
			// is that one side evaluates to a constant, no
			// HashMap<String, String> should be needed for the 
			// evaluation.
			
			IntervalPair result = _inequalityExprTree.getLeftChild()
					.evaluateExprBound(null, null, null);
			
			// Check that the bounds are the same.
			if(!result.singleValue()){
				// Scream! Something has gone wrong.
				throw new IllegalArgumentException("The InequalityVariable." +
						"getConstant() method evaluated to a non-trivial range.");
			}
			
			_constant = result.get_UpperBound();
			_isRight = false;
			
			// Either the lower or upper bound will be fine to return.
			return result.get_UpperBound();
		}
		
//		System.err.println("Warning: the inequality " + this +
//				"does not have one side equal to a constant. For no good reason " +
//				"I'm assuming it is 0.");
		//return 0;
		
		throw new IllegalStateException("The inequality " + this + " does" +
				"not have one side equal to a constant.");
	}
	
//	/**
//	 * Finds which child node of the defining ExprTree that contains the
//	 * continuous variable.
//	 * @return
//	 * 		The ExrTree node of the defining ExprTree containing the continuous
//	 * 		variable.
//	 */
//	private ExprTree findContinuous(){
//		
//		
//		if(_inequalityExprTree.getLeftChild().containsVar(_variable.getName())){
//			
//		}
//			
//		return null;
//	}
	
	/**
	 * This method determines if the value exceeds the constant value.
	 * @param value
	 * 		Value to compare.
	 * @return
	 * 		If this inequality variable represents 'variable>constant' or
	 * 		'variable>=constant', then method returns truth value of
	 * 		'value>=constant'. Otherwise this method returns the truth value
	 * 		of 'variable<=constant'
	 */
	public boolean exceedConstant(int value){
		
		// There are four cases to consider. 'v>,>= a', 'v<,<= a', 'a>,>= v'
		// and 'a<,<= v'.
		String operation = get_op();
		
		int constant = getConstant();
		
		if(">=".contains(operation)){
			// Determine which side the constant lives on.
			if(_isRight){
				return value >= constant;
			}
			return value <= constant;
		}
		// Determine which side the constant lives on.
		if(_isRight){
			return value <= constant;
		}
		return value >= constant;
		
	}
	
	/**
	 * Adds the continuous variables that this InequalityVariable depends on
	 * and registers this InqualityVariable with these continuous variables.
	 */
	private void initializeContinuous(){
		
		// Get the continuous variables named in the inequality.
		ArrayList<String> variableNames = _inequalityExprTree.getContVars();
		
		for(String name : variableNames){
			// Get variable.
			Variable v = _lpn.getVariable(name);
			
			// Added each variable to the member variable.
			_variables.add(v);
			
			// Register this inequality variable with the continuous variable
			// if it is not already registered.
			if(v.getInequalities() == null || !v.getInequalities().contains(this)){
				v.addInequalityVariable(this);
			}
		}
		
	}
	
	/**
	 * Get the LPN that contains this variable.
	 * @return
	 * 		The LPN that contains this variable.
	 */
	public LPN get_lpn(){
		return _lpn;
	}
	
	/**
	 * Get the continuous variables that this inequality variable
	 * depends on.
	 * 
	 * @return
	 * 		The continuous variables that this inequality variable
	 * 		depends on.
	 */
	public ArrayList<Variable> getContVariables(){
		return _variables;
	}
	
	/**
	 * Gets the index of this Inequality variable as a Boolean variable
	 * in the associated LPN.
	 * @return
	 * 		The index in the LPN.
	 */
	public int get_index(){
		DualHashMap<String, Integer> indexMap = _lpn.getVarIndexMap();
		return indexMap.getValue(getName());
	}
	
	/**
	 * Registers a Transition with this IneqaualityVaraible.
	 * @param t
	 * 		A Transition that has this InequalityVariable in its enabling condition.
	 */
	public void addTransition(Transition t){
		_transitions.add(t);
	}
	
	/**
	 * Get the Transitions that have this Inequality variable in their enabling condition.
	 * @return
	 * 		The set of all Transitions that have this Inequality variable in their enabling condition.
	 */
	public HashSet<Transition> getTransitions(){
		return _transitions;
	}
	
	/**
	 * Determines if this is an inequality of the form
	 * v>=a, v>a, a<=v or a<v.
	 * @return
	 * 		True if this inequality variable is of the form
	 * 		v>=a, v>a, a<=v or a<v; false otherwise.
	 */
	public boolean isGreaterThan(){
		// Ensure the _isRight field is populated.
		getConstant();
		
		return (_isRight && getName().contains(">")) || (!_isRight && getName().contains("<")); 
	}
	
	/**
	 * Determines if this is an inequality of the form
	 * v<=a, v<a, a>=v, or a>v.
	 * @return
	 * 		True if this is an inequality of the form
	 * 		v<=a, v<a, a>=v, or a>v; false otherwise.
	 */
	public boolean isLessThan(){
		return !isGreaterThan();
	}
}
