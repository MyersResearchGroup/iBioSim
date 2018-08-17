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
 * Base class for observables in iBioSim. Observables are classes that can be observed by other class.
 * When the classes changes, it can notify the observing classes about the change.
 *
 * @author Leandro Watanabe
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version $Rev$
 * @version %I%
 */
public interface BioObservable {

	/**
	 * Observable requests types.
	 */
	public static enum RequestType {
		REQUEST_INTEGER, REQUEST_BOOLEAN, REQUEST_DOUBLE, REQUEST_STRING, REQUEST_PROGRESS, ADD_FILE
	};

	/**
	 * Adds an {@link BioObservable} object as an observable.
	 *
	 * @param bioObservable
	 *          - the observable object that is observing this instance.
	 */
	public abstract void addObservable(BioObservable bioObservable);

	/**
	 * Adds an {@link BioObserver} as an observer.
	 *
	 * @param bioObserver
	 *          - the observer object that is observing this instance.
	 */
	public abstract void addObserver(BioObserver bioObserver);

	/**
	 * Notify the objects that are observing this instance.
	 *
	 * @param message
	 *          - the message sent to the objects observing this instance.
	 */
	public abstract void notifyObservers(Message message);

	/**
	 * Makes a request to the observers.
	 *
	 * @param type
	 *          - the request type.
	 * @param message
	 *          - the message exchanged.
	 *
	 * @return true if the request was successful.
	 */
	public abstract boolean request(RequestType type, Message message);

	/**
	 * Sends a message to the observers.
	 *
	 * @param type
	 *          - the request type.
	 * @param message
	 *          - the message exchanged.
	 * @return true if the send was successful.
	 */
	public abstract boolean send(RequestType type, Message message);
}
