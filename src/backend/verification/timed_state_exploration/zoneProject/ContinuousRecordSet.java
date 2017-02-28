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
package backend.verification.timed_state_exploration.zoneProject;

import java.util.HashMap;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class ContinuousRecordSet extends HashMap<UpdateContinuous, UpdateContinuous> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7357798608686001995L;

	
	/**
	 * Adds an UpdateContinuous record.
	 * 
	 * @param record The UpdateContinuous object to add.
	 * @return The previous record for the same continuous variable if it exists; null
	 * otherwise.
	 */
	public UpdateContinuous add(UpdateContinuous record){
		return super.put(record, record);
	}
	
	/**
	 * Returns any record that has an underlying LPNTransitionPair. This is contained
	 * in the LPNContAndRate portion of the UpdateContinuous.
	 * @param ltpair
	 * @return
	 */
	public UpdateContinuous get(LPNContinuousPair lcpair){
		
		return super.get(new UpdateContinuous(lcpair));
	}
	
	public boolean contains(LPNContinuousPair lcpair){
		return super.containsKey(new UpdateContinuous(lcpair));
	}
}
