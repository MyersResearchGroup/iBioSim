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
package edu.utah.ece.async.lpn.parser.properties;

import javax.swing.DefaultListModel;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class AbstractionProperty {
  
  public String xform0 = "Merge Parallel Places - simplification",
      xform1 = "Remove Place in Self-Loop - simplification",
      xform3 = "Remove Transitions with Single Place in Postset - simplification",
      xform4 = "Remove Transitions with Single Place in Preset - simplification",
      xform5 = "Merge Transitions with Same Preset and Postset - simplification",
      xform6 = "Merge Transitions with Same Preset - simplification",
      xform7 = "Merge Transitions with Same Postset - simplification",
      xform8 = "Local Assignment Propagation - simplification",
      xform9 = "Remove Write Before Write - simplification",
      xform10 = "Simplify Expressions - simplification",
      xform11 = "Constant False Enabling Conditions - simplification",
      xform12 = "Abstract Assignments to the Same Variable - abstraction",
      xform13 = "Remove Unread Variables - abstraction",
      xform14 = "Remove Dead Places - simplification",
      xform15 = "Remove Dead Transitions - simplification",
      xform16 = "Constant True Enabling Conditions - simplification",
      xform17 = "Eliminate Dominated Transitions - simplification",
      xform18 = "Remove Unread Variables - simplification",
      xform19 = "Correlated Variables - simplification",
      xform20 = "Remove Arc after Failure Transitions - simplification",
      xform21 = "Timing Bound Normalization - abstraction",
      xform22 = "Remove Vacuous Transitions - simplification",
      xform23 = "Remove Vacuous Transitions - abstraction",
      xform24 = "Remove Pairwise Write Before Write - simplification",
      xform25 = "Propagate Constant Variable Values - simplifiction",
      xform26 = "Remove Dangling Transitions - simplification",
      xform27 = "Combine Parallel Transitions - simplification",
      xform28 = "Combine Parallel Transitions - abstraction",
      xform29 = "Remove Uninteresting Variables - simplification",
      xform30 = "Remove Uninteresting Transitions - simplification",
      xform31 = "Simplify Uniform Expressions - abstraction";

  public String[] transforms = { xform12, xform28, xform27, xform11, xform16,
      xform19, xform17, xform8, xform0, xform7, xform6, xform5, xform25, xform20,
      xform26, xform14, xform15, xform24, xform1, xform3, xform4,
      xform30, xform29, xform13, xform18, xform23, xform22, xform9,
      xform10, xform31, xform21 };
  
  public DefaultListModel listModel, absListModel, preAbsModel, loopAbsModel,
  postAbsModel;
  
  public String maxIter, factor, field; 
  
  boolean verification, simplify, abstractLhpn;
  
  public AbstractionProperty()
  {
    listModel = new DefaultListModel();
    // Creates Abstraction List
    preAbsModel = new DefaultListModel();
    loopAbsModel = new DefaultListModel();
    postAbsModel = new DefaultListModel();
    maxIter = ""; factor = ""; field = "";
    simplify = true;
    verification = true;
  }
  
  public String[] getIntVars() {
    String[] intVars = new String[listModel.getSize()];
    for (int i = 0; i < listModel.getSize(); i++) {
      if (listModel.elementAt(i) != null) {
        intVars[i] = listModel.elementAt(i).toString();
      }
    }
    return intVars;
  }

  public void viewCircuit() {
    String[] getFilename;
    if (field.trim().equals("")) {
    } else {
      getFilename = new String[1];
      getFilename[0] = field.trim();
    }
  }

  public boolean isSimplify() {
    if (verification) {
      return true;
    }
    if (simplify
        || abstractLhpn) {
      return true;
    }
    return false;
  }

  public boolean isAbstract() {
    if (verification) {
      return true;
    }
    return abstractLhpn;
  }

  public Integer getNormFactor() {
    Integer factor;
    try {
      factor = Integer.parseInt(this.factor);
    }
    catch (NumberFormatException e) {
      factor =  -1;
    }
    return factor;
  }
  
  public Integer maxIterations() {
    return Integer.parseInt(maxIter);
  }
}
