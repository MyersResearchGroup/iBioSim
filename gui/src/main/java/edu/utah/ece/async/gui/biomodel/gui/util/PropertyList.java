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
package edu.utah.ece.async.gui.biomodel.gui.util;

import java.util.Collection;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PropertyList extends JList implements EnableElement, NamedObject,
		Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PropertyList(String name) {
		super();
		model = new DefaultListModel();
		setModel(model);
		this.name = name;
	}
	
	public String[] getItems() {
		Object[] tempArray = model.toArray();
		String[] stringArray = new String[tempArray.length];
		for (int i=0; i<tempArray.length; i++) {
			if (tempArray[i] instanceof String) {
				stringArray[i] = (String) tempArray[i];
			}
		}
		return stringArray;
	}

	public void removeItem(String item) {
		model.removeElement(item);
	}
	
	public void removeAllItem() {
		model.removeAllElements();
	}

	public void addItem(String item) {
		if (model.size() == 0) {
			model.addElement(item);
		} else {
			model.ensureCapacity(model.getSize() + 1);
			for (int i = 0; i < model.size(); i++) {
				if (item.compareTo(model.get(i).toString()) < 0) {
					model.add(i, item);
					return;
				}
			}
			model.addElement(item);
		}
	}
	
	public void addAllItem(String[] items) {
		for (String s : items) {
			addItem(s);
		}
	}

	public void addAllItem(Collection<String> items) {
		for (String s : items) {
			addItem(s);
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void run() {
	}

	public void updateState(Properties property) {
		if (property.containsKey("deletion")) {
			removeItem(property.getProperty("deletion"));
		} else if (property.containsKey("insertion")) {
			addItem(property.getProperty("insertion"));
		}
	}

	private String name = "";

	private DefaultListModel model = null;
}
