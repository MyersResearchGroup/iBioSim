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
package edu.utah.ece.async.ibiosim.analysis;

/**
 * Command line method for running the analysis jar file.  
 * <p>
 * Requirements:
 * <p>
 * inputfile
 * <p>
 * 
 * Options:
 * <p>
 *
 *
 * @author Leandro Watanabe
 * @author Tramy Nguyen
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Analysis {

	private static void usage() {
	  //TODO:
		System.err.println("Description:\n");
		System.err.println("Usage:\n");
		System.err.println("Required:\n");
		System.err.println("Options:\n");
		System.exit(1);
	}

	public static void main(String[] args) {
	
	  if(args.length  < 2)
	  {
	    usage();
	  }
	  
	  for(String flag : args)
	  {
	    if(flag.length() == 2 && flag.charAt(0) == '-')
	    {
	      switch(flag.charAt(1))
	      {
	        case 'h':
	          usage();
	          break;
	        default:
	          usage();
	          break;
	      }
	    }
	    else if(flag.endsWith(".xml"))
	    {
	      
	    }
	    else
	    {
	      usage();
	    }
	  }
	} 
}
