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
package backend.verification.platu.markovianAnalysis;

public class Property {
	private String label;

	private String property;

	public Property(String label, String property) {
		this.label = label;
		this.property = property;
	}

	String getLabel() {
		return label;
	}

	String getProperty() {
		return property;
	}

	/*
	private void setLabel(String label) {
		this.label = label;
	}

	private void setProperty(String property) {
		this.property = property;
	}
	*/
}