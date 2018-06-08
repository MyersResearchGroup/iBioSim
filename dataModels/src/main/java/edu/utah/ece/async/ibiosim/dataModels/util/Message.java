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
package edu.utah.ece.async.ibiosim.dataModels.util;

/**
 * Stores information that are used to communicate between different classes.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Message {

	private boolean bool;
	private String message;
	private String title;
	private MessageType type;
	private int value;

	/**
	 * The type of the message.
	 */
	public static enum MessageType {
		CANCEL, CONSOLE, DIALOG, ERROR, LOG, NONE, SCROLLABLE_ERROR
	}

	/**
	 * Constructs a new instance.
	 */
	public Message() {
		this.type = MessageType.NONE;
	}

	/**
	 * Gets the boolean value of the message.
	 *
	 * @return the corresponding boolean value.
	 */
	public boolean getBoolean() {
		return bool;
	}

	/**
	 * Gets the message value.
	 *
	 * @return the message value.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the title value of the message.
	 *
	 * @return the corresponding title value.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the integer value of the message.
	 *
	 * @return the corresponding integer value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Checks if the message represents a cancel event.
	 *
	 * @return whether it is a cancel event or not.
	 */
	public boolean isCancel() {
		return this.type == MessageType.CANCEL;
	}

	/**
	 * Checks if the message represents console.
	 *
	 * @return whether it is a console or not.
	 */
	public boolean isConsole() {
		return this.type == MessageType.CONSOLE;
	}

	/**
	 * Checks if the message represents a dialog.
	 *
	 * @return whether it is a dialog or not.
	 */
	public boolean isDialog() {
		return this.type == MessageType.DIALOG;
	}

	/**
	 * Checks if the message represents a error dialog.
	 *
	 * @return whether it is an error dialog or not.
	 */
	public boolean isErrorDialog() {
		return this.type == MessageType.ERROR;
	}

	/**
	 * Checks if the message represents a log message.
	 *
	 * @return whether it is a log message or not.
	 */
	public boolean isLog() {
		return this.type == MessageType.LOG;
	}

	/**
	 * Checks if the message represents a scrollable error dialog.
	 *
	 * @return whether it is a scrollable error dialog or not.
	 */
	public boolean isScrollableErrorDialog() {
		return this.type == MessageType.SCROLLABLE_ERROR;
	}

	/**
	 * Sets the message as a boolean.
	 *
	 * @param bool
	 *          - the boolean value.
	 */
	public void setBoolean(boolean bool) {
		this.type = MessageType.NONE;
		this.bool = bool;
	}

	/**
	 * Sets the message as a cancel event.
	 */
	public void setCancel() {
		this.type = MessageType.CANCEL;
		this.message = null;
		this.title = null;
	}

	/**
	 * Sets the message as a console.
	 *
	 * @param message
	 *          - the message of the console.
	 */
	public void setConsole(String message) {
		this.type = MessageType.CONSOLE;
		this.message = message;
		this.title = null;
	}

	/**
	 * Sets the message as a dialog.
	 *
	 * @param title
	 *          - the title of the dialog.
	 * @param message
	 *          - the message of the dialog.
	 */
	public void setDialog(String title, String message) {
		this.type = MessageType.DIALOG;
		this.message = message;
		this.title = title;
	}

	/**
	 * Sets the message as an error dialog.
	 *
	 * @param title
	 *          - the title of the error dialog.
	 * @param message
	 *          - the message of the error dialog.
	 */
	public void setErrorDialog(String title, String message) {
		this.type = MessageType.ERROR;
		this.message = message;
		this.title = title;
	}

	/**
	 * Sets the message as an integer.
	 *
	 * @param value
	 *          - the integer value.
	 */
	public void setInteger(int value) {
		this.type = MessageType.NONE;
		this.value = value;
	}

	/**
	 * Sets the message as log.
	 *
	 * @param message
	 *          - the message of the log.
	 */
	public void setLog(String message) {
		this.type = MessageType.LOG;
		this.message = message;
		this.title = null;
	}

	/**
	 * Sets the message as a scrollable error dialog.
	 *
	 * @param title
	 *          - the title of the error dialog.
	 * @param message
	 *          - the message of the error dialog.
	 */
	public void setScrollableErrorDialog(String title, String message) {
		this.type = MessageType.SCROLLABLE_ERROR;
		this.message = message;
		this.title = title;
	}

	/**
	 * Sets the message .
	 *
	 * @param message
	 *          - arbitrary string.
	 */
	public void setString(String string) {
		this.type = MessageType.NONE;
		this.title = null;
		this.message = string;
	}

}
