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
package edu.utah.ece.async.ibiosim.dataModels.util.observe;

import edu.utah.ece.async.ibiosim.dataModels.util.Message;

/**
 * Base class for observers in iBioSim. They are classes that observe observable classes.
 * Observer classes cannot be observed. Observer classes are generally GUI or classes with main functions.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public interface BioObserver {

	/**
	 * Updates the application according to the message passed by an observable class.
	 * This happens whenever the observer class gets notified by an observable class.
	 *
	 * @param message
	 *          - the message transmitted when the observer gets notified.
	 */
	public abstract void update(Message message);

}
