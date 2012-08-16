package verification.timed_state_exploration.zoneProject;

import java.util.Properties;

import lpn.parser.ExprTree;
import lpn.parser.Variable;


/**
 * An InequalityVariable is a Boolean variable for an inequality expression involving 
 * continuous variables. It extends lpn.parser.Variable. 
 * @author Andrew N. Fisher
 *
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
	
	/* 
	 * Keeps track of the Transitions that currently reference this InequalityVariable.
	 */
	//HashSet<Transition> referencingTransitions;
	int _referenceCount;
	
	/**
	 * 
	 * @param name
	 * @param type
	 * @param initCond
	 */
	public InequalityVariable(String name, String type, Properties initCond) {
		super(name, type, initCond);
		// TODO Auto-generated constructor stub
		throw new UnsupportedOperationException("This constructor needs to be verified"
				+ "for correctness for inherited class InequalityVariable.");
	}

	/**
	 * @param name
	 * @param type
	 * @param initValue
	 * @param port
	 */
	public InequalityVariable(String name, String type, String initValue,
			String port) {
		super(name, type, initValue, port);
		// TODO Auto-generated constructor stub
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
	public InequalityVariable(String name, String initValue, ExprTree ET) {
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
		
		// When created, an expression refers to this variable.
		_referenceCount = 1;
	}
	
	

	/**
	 * @param name
	 * @param type
	 */
	public InequalityVariable(String name, String type) {
		super(name, type);
		// TODO Auto-generated constructor stub
		throw new UnsupportedOperationException("This constructor needs to be verified"
				+ "for correctness for inherited class InequalityVariable.");
	}
	
	/**
	 * Overrides the toString method of Variable. Removes the pre-appended
	 * '$' of the InequalityVariable name.
	 */
	public String toString(){
		return "Inequality Variable : " + getName().substring(1);
	}
	
	/**
	 * Increase the count of how many expressions refer to this InequalityVariable.
	 */
	public void increaseCount(){
		_referenceCount++;
	}
	
	/**
	 * Decreases the count of how many expressions refer to this InequalityVariable.
	 */
	public void decreaseCount(){
		_referenceCount--;
	}

	/**
	 * Returns the count of the number of expressions referring to this
	 * InequalityVariable.
	 * @return
	 * 		The count recorded for how many expressions refer to this InequalityVariable.
	 */
	public int getCount(){
		return _referenceCount;
	}
	
	/**
	 * Returns false. InequalityVaribles are dealt with separately.
	 */
	public boolean isInput() {
		return false;
	}
	
	/**
	 * Returns false. InequalityVaribles are dealt with separately.
	 */
	public boolean isOutput() {
		return false;
	}

	/**
	 * Returns false. InequalityVaribles are dealt with separately.
	 */
	public boolean isInternal() { 
		return false;
	}
}
