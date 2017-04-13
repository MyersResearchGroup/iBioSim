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
package edu.utah.ece.async.gui.analysis.incrementalsim;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class EventQueueElement {
	private double time;
	private double delayVal;
	private String eventID;
	private double priorityVal;
	
	public EventQueueElement(double time, String eventID, double delayVal, double priorityVal ) {
		this.time = time;
		this.delayVal = delayVal;
		this.eventID = eventID;
		this.priorityVal = priorityVal;
	}
		
	public double getScheduledTime(){
		return time+delayVal;
	}
	
	public String getEventId(){
		return eventID;
	}
	
	public double getPriorityVal() {
		return priorityVal;
	}

}
