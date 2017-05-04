package edu.utah.ece.async.ibiosim.dataModels.util;

import java.io.IOException;
import java.util.Map;

public class Executables {

	public static String reb2sacExecutable = null;
	public static String[] envp  = null;
	public static String geneNetExecutable  = null;

	public static Boolean			libsbmlFound		= false;
	public static Boolean			reb2sacFound		= false;
	public static Boolean			geneNetFound		= false;

	private static boolean hasChecked = false;

	public static void checkExecutables()
	{
		if(!hasChecked)
		{
			int exitValue = 1;
			
			libsbmlFound		= true;
			reb2sacFound		= true;
			geneNetFound		= true;
			
			try {
				System.loadLibrary("sbmlj");
				// For extra safety, check that the jar file is in the classpath.
				Class.forName("org.sbml.libsbml.libsbml");
			} catch (UnsatisfiedLinkError e) {
				libsbmlFound = false;
			} catch (ClassNotFoundException e) {
				libsbmlFound = false;
			} catch (SecurityException e) {
				libsbmlFound = false;
			}
			
			try {
				if (System.getProperty("os.name").contentEquals("Linux")) {
					Executables.reb2sacExecutable = "reb2sac.linux64";
				} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					Executables.reb2sacExecutable = "reb2sac.mac64";
				} else {
					Executables.reb2sacExecutable = "reb2sac.exe";
				}
				ProcessBuilder ps = new ProcessBuilder(Executables.reb2sacExecutable, "");
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
				Process reb2sac = ps.start();
				if (reb2sac != null) {
					exitValue = reb2sac.waitFor();
				}
				if (exitValue != 255 && exitValue != -1) {
					Executables.reb2sacFound = false;
				}
			} catch (IOException e) {
				Executables.reb2sacFound = false;
			} catch (InterruptedException e) {
				Executables.reb2sacFound = false;
			}
			exitValue = 1;
			try {
				if (System.getProperty("os.name").contentEquals("Linux")) {
					Executables.geneNetExecutable = "GeneNet.linux64";
				} else if (System.getProperty("os.name").toLowerCase().startsWith("mac os")) {
					Executables.geneNetExecutable = "GeneNet.mac64";
				} else {
					Executables.geneNetExecutable = "GeneNet.exe";
				}
				ProcessBuilder ps = new ProcessBuilder(Executables.geneNetExecutable, "");
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
				ps.redirectErrorStream(true);
				Process geneNet = ps.start();
				if (geneNet != null) {
					exitValue = geneNet.waitFor();
				}
				if (exitValue != 255 && exitValue != 134 && exitValue != -1) {
					Executables.geneNetFound = false;
				}
			} catch (IOException e) {
				Executables.geneNetFound = false;
			} catch (InterruptedException e) {
				Executables.geneNetFound = false;
			}
		}
	}

}
