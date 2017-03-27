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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utah.ece.async.verification.platu.platuLpn;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author ldtwo
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class VarSet extends HashSet<String> {

   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public    VarSet(HashSet<String> in) {
        super(in);
    }

  public  VarSet(String[] in) {
        super(Arrays.asList(in));
    }

    public VarSet() {
        super(0);
    }

    @Override
    public VarSet clone() {
        VarSet set=new VarSet();
        set.addAll(this);
        return set;
    }
}
