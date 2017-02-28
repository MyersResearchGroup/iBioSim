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
package frontend.biomodel.util;

import java.util.LinkedList;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class UndoManager {
	private LinkedList<Object> undoStack;
	private LinkedList<Object> redoStack;
	private Object currentState;
	
	private final int MAX_UNDO_STACK_SIZE = 100;
	
	public UndoManager(){
		undoStack = new LinkedList<Object>();
		redoStack = new LinkedList<Object>();
		
		currentState = null;
	}
	
	public void makeUndoPoint(Object state){
		
		// if the undo point is identical to the existing point, don't do anything.
		if(currentState != null && state.toString() == currentState.toString())
			return;
		redoStack.clear();

		// store the last state onto the undo list
		if(currentState != null)
			undoStack.addFirst(currentState);
		currentState = state;
		
		// make sure the stack doesn't get too big
		if(undoStack.size() > MAX_UNDO_STACK_SIZE){
			undoStack.removeLast();
		}
	}
	
	/**
	 * Pops an undo operation off the undo stack and returns it.
	 * Put currentState onto the redo stack.
	 */
	public Object undo(){
		// put the current state onto the redo stack
		if(currentState != null && undoStack.size() > 0){
			redoStack.addFirst(currentState);
		}
		
		if(undoStack.size() > 0){
			currentState = undoStack.removeFirst();
		}

		return currentState;
	}
	
	public Object redo(){
		if(currentState != null && redoStack.size() > 0){
			undoStack.addFirst(currentState);
		}
		
		if(redoStack.size() > 0){
			currentState = redoStack.removeFirst();
		}

		return currentState;
	}
}
