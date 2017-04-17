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
package edu.utah.ece.async.verification.platu.main;

import java.util.concurrent.Executors;

import edu.utah.ece.async.verification.platu.main.Options;
import edu.utah.ece.async.verification.platu.project.Project;

import java.util.concurrent.ExecutorService;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.StringTokenizer;
import static java.lang.Runtime.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Main {

	// static variables
	public final static int PROCESSORS = Runtime.getRuntime().availableProcessors();
   	public final static int THREADS = PROCESSORS;
   	public final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
    public static final SimpleDateFormat dateAndTime = new SimpleDateFormat("yyyy-MMM-dd hh.mm.ss a");
    public static String workingDirectory = System.getProperty("user.dir");
    
//    static{
//    	System.out.println(System.getProperty("user.dir"));
//      System.out.println("CORES: " + PROCESSORS);
//    	System.out.println("isWindows: " + isWindows);
//    }
    
    public static ExecutorService exec = Executors.newFixedThreadPool(PROCESSORS);
    
    // options
    public static boolean ZONE_OPTIMIZE_INF = false;
    public static int ZONE_VERSION = 3;
    public static long GRAPH_KEEP_ALIVE_TIME = 3000;//millisec
    public static boolean ZONE_ENABLE_PRINT = false;
    public static boolean PRINT_USAGE_STATS = false;
    public static boolean REMOVE_DUMMY = false;
    public static boolean FIND_FAILURE = true;
    public static boolean SHOW_SIGNALS = false;
    public static boolean SHOW_FAILURES = true;
    public static boolean NO_DATE = true;
    public static boolean PRINT_FINAL_SUMMARY = false;
    public static long MAX_MEM = 0;
    public static long MAX_STACK_HEIGHT = Long.MAX_VALUE;
    public static boolean PRINT_MAIN = false;
    public static boolean ENABLE_LOGGING = false;

    private static void start(String cmdFile) {
        try {
            BufferedReader br = null;

            if (cmdFile != null) {
                br = new BufferedReader(new FileReader(cmdFile));
            } else {
                br = new BufferedReader(new InputStreamReader(System.in));
            }

            long memUse;
            Interpretor in = new Interpretor();
            Project prj = new Project();
            
            while (true) {
                String commandline = null;
                commandline = br.readLine();

                if (commandline == null) break;
                
                if(commandline.equalsIgnoreCase("reset")){
                    prj = new Project();
                }
                
                if (in.interpretcommand(prj, commandline) == 1) {
                    memUse = getRuntime().totalMemory() - getRuntime().freeMemory();
                    MAX_MEM = MAX_MEM > memUse ? MAX_MEM : memUse;
                    break;
                }
            }

            br.close();

        } catch (java.io.FileNotFoundException ex) {
            System.err.println("file not found: " + new File(cmdFile).getAbsoluteFile());
            ex.printStackTrace();
            System.exit(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            String optFileString = null, cmdFileString = null;
//            String switches = "";
            for (String arg : args) {
                if (arg.startsWith("/")) {
 //                   switches += arg + "\n";
                } 
                else if (arg.endsWith(".cmd") || arg.endsWith(".cmnd")) {
                    cmdFileString = arg;
                } 
                else if (arg.endsWith(".opt") || arg.endsWith(".cfg") || arg.endsWith(".conf")) {
                    optFileString = arg;
                }
            }
            
            String path = optFileString != null ? optFileString : cmdFileString;
            if (path != null) {
                File f = new File(path);
                path = f.getAbsoluteFile().getParentFile().getAbsolutePath();
            } 
            else {
                path = System.getProperty("user.dir");
            }
            
            File optFile = null;
            if (optFileString != null) {
                    optFile = new File(optFileString);
                    if (!optFile.exists()) {
                        System.err.println("file not found: " + optFile.getAbsolutePath());
                    }

                    // load options
                    Properties prop = new Properties();
                    prop.load(new FileInputStream(optFile));
                    setOptions(prop);
            }
            
            Main.start(cmdFileString);

//            if (PRINT_USAGE_STATS) {
//                VarVal.printUsageStats();
//                LPNTran.printUsageStats();
////                TimedStateGraph.printUsageStats();
////                if (AbstractStateGraph.TIMED_ANALYSIS) {
////                    TimedState.printUsageStats();
////                } else {
////                    State.printUsageStats();
////
////                }
//            }
        } 
        catch (Throwable e) {            
        	e.printStackTrace();
			System.exit(1);
        }
    }

    static void setOptions(Properties prop) throws Exception {
        String value = prop.getProperty("DOT_PATH");
        if(value != null) Options.setDotPath(value);
        
        value = prop.getProperty("VERBOSITY");
        if(value != null){
        	int verbosity = 0;
        	
        	try{
        		verbosity = Integer.parseInt(value);
        		Options.setVerbosity(verbosity);
        	}
        	catch(Exception e){
        		System.out.println("warning: verbosity option is not valid - default is 0");
        	}
        }
        
        value = prop.getProperty("TIMING_ANALYSIS");
        if(value != null){
    		Options.setTimingAnalsysisType(value);
        }
        
        value = prop.getProperty("COMPOSITIONAL_MINIMIZATION");
        if(value != null){
    		Options.setCompositionalMinimization(value);
        }
        
        value = prop.getProperty("POR");
        if(value != null) Options.setPOR(value);
        
        value = prop.getProperty("SEARCH");
        if(value != null) Options.setSearchType(value);
        
        value = prop.getProperty("STATE_FORMAT");
        if(value != null) Options.setStateFormat(value);
        
        value = prop.getProperty("NEW_PARSER_FLAG");
        if(value != null) if(value.equals("true") || value.equals("TRUE")) Options.setNewParser();
        
        value = prop.getProperty("STICKY_SEMANTICS");
        if(value != null) if(value.equals("true") || value.equals("TRUE")) Options.setStickySemantics();
        
        value = prop.getProperty("PARALLEL");
        if(value != null) if(value.equals("true") || value.equals("TRUE")) Options.setParallelFlag();
    }

//    static Properties storeOptions(File propFile) throws Exception {
//        Properties prop = new Properties();
////        prop.put("StateGraph.MAINTAIN_STATE_TRAN_LIST", "" + TimedStateGraph.MAINTAIN_STATE_TRAN_LIST);
////        prop.put("StateGraph.SHOW_STATE_INC_TREE", "" + TimedStateGraph.SHOW_STATE_INC_TREE);
////        prop.put("StateGraph.USING_POSET", "" + TimedStateGraph.USING_POSET);
//        prop.put("REMOVE_DUMMY", "" + REMOVE_DUMMY);
//        prop.put("FIND_FAILURE", "" + FIND_FAILURE);
//        prop.put("SHOW_SIGNALS", "" + SHOW_SIGNALS);
//        prop.put("SHOW_FAILURES", "" + SHOW_FAILURES);
////        prop.put("Expr.PRINT_DEBUG", "" + Expression.PRINT_DEBUG);
//        prop.put("LPNTran.ENABLE_PRINT", "" + LPNTran.ENABLE_PRINT);
//        prop.put("LPNTran.PRINT_LEVEL", "" + LPNTran.PRINT_LEVEL);
////        prop.put("Markings.INIT_SIZE", "" + Markings.INIT_SIZE);
//        prop.put("OutputDOT.SIMPLE_TRAN_LABELS", "" + OutputDOT.SIMPLE_TRAN_LABELS);
//        prop.put("OutputDOT.SIMPLE_STATES", "" + OutputDOT.SIMPLE_STATES);
////        prop.put("StateGraph.LPN_PATH", "" + TimedStateGraph.LPN_PATH);
////        prop.put("StateGraph.DOT_PATH", "" + TimedStateGraph.DOT_PATH);
////        prop.put("StateGraph.TIMED_ANALYSIS", "" + TimedStateGraph.TIMED_ANALYSIS);
////        prop.put("StateGraph.INTERACTIVE_MODE", "" + TimedStateGraph.INTERACTIVE_MODE);
////        prop.put("StateGraph.OPEN_STATE_EXPLORER", "" + TimedStateGraph.OPEN_STATE_EXPLORER);
////        prop.put("StateGraph.DRAW_JAVA_GRAPH", "" + TimedStateGraph.DRAW_JAVA_GRAPH);
////        prop.put("StateGraph.DRAW_MEMORY_GRAPH", "" + TimedStateGraph.DRAW_MEMORY_GRAPH);
////        prop.put("StateGraph.DRAW_STATE_GRAPH", "" + TimedStateGraph.DRAW_STATE_GRAPH);
////        prop.put("StateGraph.OUTPUT_DOT", "" + TimedStateGraph.OUTPUT_DOT);
////        prop.put("StateGraph.STOP_ON_ERROR", "" + TimedStateGraph.STOP_ON_ERROR);
////        prop.put("StateGraph.STOP_ON_FAILURE", "" + TimedStateGraph.STOP_ON_FAILURE);
////        prop.put("StateGraph.PRINT_LEVEL", "" + TimedStateGraph.PRINT_LEVEL);
//        prop.put("ZONE_ENABLE_PRINT", "" + ZONE_ENABLE_PRINT);
//        prop.put("LPN.ENABLE_PRINT", "" + LPN.ENABLE_PRINT);
//        prop.put("NO_DATE", "" + NO_DATE);
//        prop.put("PRINT_FINAL_SUMMARY", "" + PRINT_FINAL_SUMMARY);
//        prop.put("PRINT_MAIN", "" + PRINT_MAIN);
//        prop.put("GRAPH_KEEP_ALIVE_TIME", "" + GRAPH_KEEP_ALIVE_TIME);
//        prop.put("MAX_STACK_HEIGHT", "" + MAX_STACK_HEIGHT);
//        prop.put("ZONE_OPTIMIZE_INF", "" + ZONE_OPTIMIZE_INF);
//        prop.put("ZONE_VERSION", "" + ZONE_VERSION);
//        
//        try {
//            FileOutputStream fos = new FileOutputStream(propFile);
//            String str = "";
//            int idx = 0;
//            String[] entries = new String[prop.entrySet().size()];
//            for (Entry ent : prop.entrySet()) {
//                str = String.format("%-30s = %-1s\n", ent.getKey(), ent.getValue());
//                entries[idx++] = str.replace("\\", "\\\\");
//            }
//            
//            Arrays.sort(entries);
//            fos.write(("\n############ PRINT ##############\n").getBytes());
//            
//            for (String ent : entries) {
//                if (ent.toLowerCase().contains("print")) {
//                    fos.write((ent).getBytes());
//                }
//            }
//            
//            fos.write(("\n############ path ##############\n").getBytes());
//            
//            for (String ent : entries) {
//                if (ent.toLowerCase().contains("print")) continue;
//                
//                if (ent.toLowerCase().contains("path")) {
//                    fos.write((ent).getBytes());
//                }
//            }
//            
//            fos.write(("\n############# DOT ###############\n").getBytes());
//
//            for (String ent : entries) {
//                if (ent.toLowerCase().contains("print")) continue;
//                if (ent.toLowerCase().contains("path")) continue;
//                if (ent.toLowerCase().contains("dot")) {
//                    fos.write((ent).getBytes());
//                }
//            }
//            
//            fos.write(("\n############ GRAPHS #############\n").getBytes());
//
//            for (String ent : entries) {
//                if (ent.toLowerCase().contains("print")) {
//                    continue;
//                }
//                if (ent.toLowerCase().contains("path")) {
//                    continue;
//                }
//                if (ent.toLowerCase().contains("dot")) {
//                    continue;
//                }
//                if (ent.toLowerCase().contains("graph")) {
//                    fos.write((ent).getBytes());
//                }
//            }
//            
//            fos.write(("\n############ OTHER ##############\n").getBytes());
//
//            for (String ent : entries) {
//                if (ent.toLowerCase().contains("print")) {
//                    continue;
//                }
//                if (ent.toLowerCase().contains("path")) {
//                    continue;
//                }
//                if (ent.toLowerCase().contains("dot")) {
//                    continue;
//                }
//                if (ent.toLowerCase().contains("graph")) {
//                    continue;
//                }
//                fos.write((ent).getBytes());
//            }
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//        return prop;
//    }

    public static String mergeColumns(String a, String b, int w1, int w2) {
        String ret = "";
        StringTokenizer tk1 = new StringTokenizer(a, "\n");
        StringTokenizer tk2 = new StringTokenizer(b, "\n");
        while (tk1.hasMoreTokens() && tk2.hasMoreTokens()) {
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s |\n", tk1.nextToken(), tk2.nextToken());
        }
        while (tk2.hasMoreTokens()) {
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s |\n", "", tk2.nextToken());
        }
        while (tk1.hasMoreTokens()) {
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s |\n", tk1.nextToken(), "");
        }
        return ret;
    }

    public static String mergeColumns(String a, String b, String c, int w1, int w2, int w3) {
        String ret = "";
        StringTokenizer tk1 = new StringTokenizer(a, "\n");
        StringTokenizer tk2 = new StringTokenizer(b, "\n");
        StringTokenizer tk3 = new StringTokenizer(c, "\n");
        String s1 = null, s2 = null, s3 = null;
        while (tk1.hasMoreTokens() || tk2.hasMoreTokens() || tk3.hasMoreTokens()) {
            s1 = tk1.hasMoreTokens() ? tk1.nextToken() : "";
            s2 = tk2.hasMoreTokens() ? tk2.nextToken() : "";
            s3 = tk3.hasMoreTokens() ? tk3.nextToken() : "";
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s | %-" + w3 + "s |\n",
                    s1,// (""+s1).replace("\t", "...").replace(" ", "~"),
                    s2,// (""+s2).replace("\t", "...").replace(" ", "~"),
                    s3// (""+s3).replace("\t", "...").replace(" ", "~")
                    );
        }
        return ret;
    }
}
