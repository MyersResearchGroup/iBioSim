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
package backend.analysis.dynamicsim.hierarchical.util.comp;

import java.util.Comparator;

import backend.analysis.dynamicsim.hierarchical.math.EventNode;

// EVENT COMPARATOR INNER CLASS
/**
 * compares two events to see which one should be before the other in the
 * priority queue
 */
public class HierarchicalEventComparator implements Comparator<EventNode>
{
	@Override
	public int compare(EventNode event1, EventNode event2)
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