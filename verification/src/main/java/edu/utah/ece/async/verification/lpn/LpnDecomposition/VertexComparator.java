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
package edu.utah.ece.async.verification.lpn.LpnDecomposition;

import java.util.Comparator;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VertexComparator implements Comparator<Vertex>{
	private int maxNumVarsInOneComp;
	
	public VertexComparator(Integer maxNumVarsInOneComp) {
		this.maxNumVarsInOneComp = maxNumVarsInOneComp;
	}

	@Override
	public int compare(Vertex v1, Vertex v2) {
		if (v1.calculateBestNetGain(maxNumVarsInOneComp) >= v2.calculateBestNetGain(maxNumVarsInOneComp)) 
			return -1;
		return 1;
//		else
//			return 0;
	}
}
