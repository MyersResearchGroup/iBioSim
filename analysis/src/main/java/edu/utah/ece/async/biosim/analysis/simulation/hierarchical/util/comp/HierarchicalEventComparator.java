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
package edu.utah.ece.async.biosim.analysis.simulation.hierarchical.util.comp;

import java.util.Comparator;

import edu.utah.ece.async.biosim.analysis.simulation.hierarchical.states.EventState;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class HierarchicalEventComparator implements Comparator<EventState>
{
	@Override
	public int compare(EventState event1, EventState event2)
	{
		if (event1.getFireTime() > event2.getFireTime())
		{
			return 1;
		}
		else if (event1.getFireTime() < event2.getFireTime())
		{
			return -1;
		}
		else
		{

			if (event1.getPriority() > event2.getPriority())
			{
				return -1;
			}
			else if (event1.getPriority() < event2.getPriority())
			{
				return 1;
			}
			else
			{
				return Math.random() > 0.5 ? 1 : -1;
			}

		}
	}
}