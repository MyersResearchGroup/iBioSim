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

package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.states;

import java.util.ArrayList;
import java.util.List;

/**
 * State variable for the representation of dense arrays.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class DenseState extends TreeState {
	private List<HierarchicalState> listOfStates;

	public DenseState () {
		this.listOfStates = new ArrayList<>(1);
	}

	public DenseState (int capacity) {
		this.listOfStates = new ArrayList<>(capacity);
	}

	@Override
	public HierarchicalState getState(int index) {
		return listOfStates.get(index);
	}

	@Override
	public void addState(int index, HierarchicalState state) {
		if (listOfStates == null) {
			listOfStates = new ArrayList<>();
		}

		while (index > listOfStates.size()) {
			listOfStates.add(state);
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (listOfStates != null) {
			for (int i = 0; i < listOfStates.size(); i++) {
				builder.append("{" + i + ":" + listOfStates.get(i) + "}");
			}
		}
		return builder.toString();
	}

	@Override
	protected boolean containsChild(int index) {
		if (listOfStates.size() <= index) { return false; }
		return true;
	}

	@Override
	public void replaceState(int index, HierarchicalState state) {
		if (listOfStates.size() <= index) {
			addState(index, state);
		} else {
			listOfStates.set(index, state);
		}

	}

	@Override
	public double getRateValue() {
		return 0;
	}

	@Override
	public void setRateValue(double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreInitialValue() {
		for (HierarchicalState state : listOfStates) {
			state.restoreInitialValue();
		}
	}

}
