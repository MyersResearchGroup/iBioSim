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
package main.java.edu.utah.ece.async.biomodel.util;

import org.sbml.jsbml.SBMLDocument;

/**
 * 
 *
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Validation implements Runnable {

    private String file;
    private SBMLDocument doc;
    private boolean overdeterminedOnly;

    public Validation(String file, SBMLDocument doc, boolean overdeterminedOnly) {
    	this.file = file;
    	this.doc = doc;
    	this.overdeterminedOnly = overdeterminedOnly;
    }

    public void run() {
        //SBMLutilities.check(file, doc, overdeterminedOnly);
    }
}
