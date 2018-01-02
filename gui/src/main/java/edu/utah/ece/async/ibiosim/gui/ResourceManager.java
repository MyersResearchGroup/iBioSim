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
package edu.utah.ece.async.ibiosim.gui;

import java.net.URL;

import javax.swing.ImageIcon;

public class ResourceManager {
  
  public static URL getResource(String fileName)
  {
    return ResourceManager.class.getResource(fileName);
  }
  
  public static ImageIcon getImageIcon(String fileName)
  {
    return new ImageIcon(ResourceManager.class.getResource("icons/" + fileName));
  }
  
}
