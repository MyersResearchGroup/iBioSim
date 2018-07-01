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
package edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.util.setup;

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.ext.arrays.ArraysConstants;
import org.sbml.jsbml.ext.arrays.ArraysSBasePlugin;
import org.sbml.jsbml.ext.arrays.Dimension;
import org.sbml.jsbml.ext.arrays.Index;

import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.math.HierarchicalNode;
import edu.utah.ece.async.ibiosim.analysis.simulation.hierarchical.model.HierarchicalModel;

/**
 * Setups up arrays in the hierarchical simulator.
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
class ArraysSetup {

  static void setupArrays(HierarchicalModel modelstate, ModelContainer container, SBase sbase, HierarchicalNode node) {
    ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

    if (plugin == null) { return; }
    container.getHierarchicalModel();
    if (plugin.getNumDimensions() > 0) {
      for (Dimension dimension : plugin.getListOfDimensions()) {

      }
    }

  }

  static void setupIndices(HierarchicalModel modelstate, ModelContainer container, SBase sbase) {
    ArraysSBasePlugin plugin = (ArraysSBasePlugin) sbase.getExtension(ArraysConstants.shortLabel);

    if (plugin == null) { return; }

    if (plugin.getNumIndices() > 0) {
      for (Index index : plugin.getListOfIndices()) {

      }
    }
  }

}