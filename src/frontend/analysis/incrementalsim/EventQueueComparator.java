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
package frontend.analysis.incrementalsim;
import java.util.Comparator;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EventQueueComparator implements Comparator<EventQueueElement>{
	@Override
	public int compare(EventQueueElement e1, EventQueueElement e2) {
		if (e1.getScheduledTime() < e2.getScheduledTime()){
			return -1;
		}
		else if (e1.getScheduledTime() > e2.getScheduledTime()) {
			return 1;
		}
		else { // e1.getScheduledTime() == e2.getScheduledTime()
			// Compare priority
			if (e1.getPriorityVal() > e2.getPriorityVal()) {
				return -1;
			}
			if (e1.getPriorityVal() < e2.getPriorityVal()) {
				return 1;
			}
			return 0;
		}
	}
}
