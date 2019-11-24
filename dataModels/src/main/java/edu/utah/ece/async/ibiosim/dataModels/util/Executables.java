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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class Executables {

	public static String reb2sacExecutable = "reb2sac";
	public static String[] envp  = null;
	public static String geneNetExecutable  = "GeneNet";
	public static String yosysExecutable  = "Yosys";

	public static Boolean			libsbmlFound		= false;
	public static Boolean			reb2sacFound		= false;
	public static Boolean			geneNetFound		= false;
	public static Boolean			yosysFound		= false;

	private static boolean hasChecked = false;

	private static ArrayList<String> errors;
	
	public static void checkExecutables()
	{
		errors = new ArrayList<String>();
		if(!hasChecked)
		{
			
			libsbmlFound		= true;
			reb2sacFound		= true;
			geneNetFound		= true;
			yosysFound = true;
			
			checkLibsbml();
		
      
      reb2sacFound = checkProcess(Executables.reb2sacExecutable);
      geneNetFound = checkProcess(Executables.geneNetExecutable);
      yosysFound = checkProcess(Executables.yosysExecutable);
			
		}
	}
	
	private static void checkLibsbml()
	{
	  try {
      System.loadLibrary("sbmlj");
      // For extra safety, check that the jar file is in the classpath.
      Class.forName("org.sbml.libsbml.libsbml");
    } catch (UnsatisfiedLinkError e) {
      errors.add("libsbml not found (UnsatisfiedLinkError)");
      //e.printStackTrace();
      libsbmlFound = false;
    } catch (ClassNotFoundException e) {
      errors.add("libsbml not found (ClassNotFoundException)");
      libsbmlFound = false;
    } catch (SecurityException e) {
      errors.add("libsbml not found (SecurityException)");
      libsbmlFound = false;
    }
	}
	
	private static boolean checkProcess(String processName)
	{
	  int exitValue = 1;
	  boolean canRun = true;
	  
	  try 
	  {
      ProcessBuilder ps = new ProcessBuilder(processName, "");
      Map<String, String> env = ps.environment();
      if (System.getenv("BIOSIM") != null) {
        env.put("BIOSIM", System.getenv("BIOSIM"));
      }
      if (System.getenv("LEMA") != null) {
        env.put("LEMA", System.getenv("LEMA"));
      }
      if (System.getenv("ATACSGUI") != null) {
        env.put("ATACSGUI", System.getenv("ATACSGUI"));
      }
      if (System.getenv("LD_LIBRARY_PATH") != null) {
        env.put("LD_LIBRARY_PATH", System.getenv("LD_LIBRARY_PATH"));
      }
      if (System.getenv("DDLD_LIBRARY_PATH") != null) {
        env.put("DYLD_LIBRARY_PATH", System.getenv("DDLD_LIBRARY_PATH"));
      }
      if (System.getenv("PATH") != null) {
        env.put("PATH", System.getenv("PATH"));
      }
      Executables.envp = new String[env.size()];
      int i = 0;
      for (String envVar : env.keySet()) {
        Executables.envp[i] = envVar + "=" + env.get(envVar);
        i++;
      }
      ps.redirectErrorStream(true);
      Process process = ps.start();
      if (process != null) {
        exitValue = process.waitFor();
      }
      if(processName == Executables.geneNetExecutable)
      {
        if (exitValue != 255 &&  exitValue != -1 &&  exitValue != 0 &&  exitValue != 134) {
          canRun = false;
          errors.add(processName + " not functional" + " (" + exitValue + ").");
        }
      }
      else if(processName == Executables.reb2sacExecutable)
      {
        if (exitValue != 255 &&  exitValue != -1) {
          canRun = false;
          errors.add(processName + " not functional" + " (" + exitValue + ").");
        }
      }
      else
      {
        if (exitValue != 255) {
          canRun = false;
          errors.add(processName + " not functional" + " (" + exitValue + ").");
        }
      }
      
    } catch (IOException e) {
      canRun= false;
      errors.add(processName + " not found.");
    } catch (InterruptedException e) {
      canRun = false;
      errors.add(processName + " throws exception.");
    }
	  
	  return canRun;
	}

	/**
	 * @return the errors
	 */
	public static ArrayList<String> getErrors() {
		return errors;
	}
}
