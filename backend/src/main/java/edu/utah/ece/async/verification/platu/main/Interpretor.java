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
package main.java.edu.utah.ece.async.verification.platu.main;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import dataModels.lpn.parser.LPN;
import dataModels.util.exceptions.BioSimException;
import main.java.edu.utah.ece.async.verification.platu.project.Project;
import main.java.edu.utah.ece.async.verification.platu.stategraph.StateGraph;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Interpretor {

    public static boolean OLD_LPN = false;
    private boolean readFlag = false;
    final int MAXHISTORY = 10;
    boolean FLATMODEL = false;
    String[] commandHistory = new String[MAXHISTORY];
    static final LinkedHashMap<enCMD, String> mapCMD = (new LinkedHashMap<enCMD, String>(20));
    static final LinkedHashMap<enCMD, String> mapDesc = (new LinkedHashMap<enCMD, String>(20));

    enum enCMD {
        compAnalysis, findallsg, skip, set, last, liststg, sglist, readcsp,
        setinterface, savesg, savelpn, join, compose, removedummypn,
        rdpn, findsg, findrsg, simulate, sim, getsg, addenv,
        flattenpns, mergebgpnall, getsgmaximal, getsgmaxenv, getsgflat,
        mergesg, sgabst, abstractsg, sgaf, autofailure,
        sgreduce, reduce, deletesg, help, quit, q, interactive,
        print, chkpie, sghide, abstStateTran, hidevar, readrsg,
        readlpn, chkpi, del, draw
    }
    
    static final String[] CMD = {"compAnalysis", "findallsg", "skip", "set", "last", "liststg", "sglist", "readcsp",
        "setinterface", "savesg", "savelpn", "join", "compose", "removedummypn",
        "rdpn", "findsg", "findrsg", "simulate", "sim", "getsg", "addenv",
        "flattenpns", "mergebgpnall", "getsgmaximal", "getsgmaxenv", "getsgflat",
        "mergesg", "sgabst", "abstractsg", "sgaf", "autofailure",
        "sgreduce", "reduce", "deletesg", "help", "quit", "q", "interactive",
        "print", "chkpie", "sghide", "abstStateTran", "hidevar", "readrsg",
        "readlpn", "chkpi", "del", "draw"
    };
    static final String[] CMDDesc = {
        "[compAnalysis]", "[findallsg]", "  [skip]", "  [set]", "re-enter last command", "  [liststg]", "  [sglist]", "  [readcsp]",
        "  [setinterface]", "+ [savesg]", "+ [savelpn]", "+ [join]", "+ [compose]", "  [removedummypn]",
        "  [rdpn]", "+ [findsg]", "+ [findrsg]", "+ [simulate]", "+ [sim]", "+ [getsg]", "  [addenv]",
        "  [flattenpns]", "+ [mergebgpnall]", "  [getsgmaximal]", "  [getsgmaxenv]", "  [getsgflat]",
        "  [mergesg]", "  [sgabst]", "  [abstractsg]", "  [sgaf]", "  [autofailure]",
        "  [sgreduce]", "  [reduce]", "  [deletesg]", "+  [help]", "+  [quit]", "+ [q]", "+ [interactive]",
        "+ [print]", "  [chkpie]", "  [sghide]", "  [abstStateTran]", "  [hidevar]", "  [readrsg]",
        "+ [readlpn]", "  [chkpi]", "  [del]", "  [draw]"
    };

    static {
        int x = 0;
        for (enCMD en : enCMD.values()) {
            mapCMD.put(en, CMD[x]);
            mapDesc.put(en, CMDDesc[x]);
            x++;
        }
    }

    public int interpretcommand(Project prj, final String commandline) throws BioSimException {
        if (prj == null) {
            new Exception("Main: interpretcommand: prj is NULL").printStackTrace();
            return 0;
        }

        //String systemcommand = commandline;
        //systemcommand += " ";
        LinkedList<String> arguments = new LinkedList<String>();
        String command;
        String argument1 = "";

        // parse commandline and place command and arguements into list
        String buffer = commandline;
        StringTokenizer tk = new StringTokenizer(buffer, " ");
        while (tk.hasMoreTokens()) {
            arguments.add(tk.nextToken());
        }

        // make sure there is a command to execute
        if (arguments.size() == 0) {
            return 0;
        }

        // remove command from argument list
        command = arguments.removeFirst();

        int argumentcount = arguments.size();
        if (arguments.size() > 0) {
            argument1 = arguments.peekFirst();
        }

        // mode changes
        if (command.charAt(0) == '/' && command.charAt(1) == '/') {
            return 0;
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.skip)) == 0) {
            return 0;
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.del)) == 0) {
            for (StateGraph sg : prj.getDesignUnitSet()) {
            	LPN lpn = sg.getLpn();
            	
                if (lpn.getLabel().compareTo(argument1) == 0) {
                    prj.getDesignUnitSet().remove(lpn);
                }
            }
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.last)) == 0) {
            int x = 1;
            for (; x < MAXHISTORY; x++) {
                System.out.printf("\t%d\t%s\n", x, commandHistory[x]);
            }

            System.out.printf("enter choice: ");
            try {
                x = System.in.read() - '0';
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            int ret = interpretcommand(prj, commandHistory[x]);//!!
            return (ret);
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.rdpn)) == 0
                || command.compareToIgnoreCase(mapCMD.get(enCMD.removedummypn)) == 0) {
            //if( argument1 != "" )
            //prj.removeDummyPN( argument1 );
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.findsg)) == 0) {
            if (argumentcount > 0) {
                prj.search();
            } else {
                prj.search();
            }
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.findallsg)) == 0) {
                prj.search();
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.findrsg)) == 0) {
            prj.search();
        }else if (command.compareToIgnoreCase(mapCMD.get(enCMD.compAnalysis)) == 0) {
    		prj.search();
        }else if (command.compareToIgnoreCase(mapCMD.get(enCMD.draw)) == 0){
        	if(argumentcount > 0){
        		for(StateGraph sg : prj.getDesignUnitSet()){
        			LPN lpn = sg.getLpn();
        			
        			if(argument1.equals(lpn.getLabel())){
        				System.out.print("drawing " + lpn.getLabel() + "...");
        				sg.draw();
        				System.out.println("Done");
        			}
        		}
        	}
        	else{
        		for(StateGraph sg : prj.getDesignUnitSet()){
        			LPN lpn = sg.getLpn();
        			System.out.print("drawing " + lpn.getLabel() + "...");
        			sg.draw();
        			System.out.println("Done");
        		}
        	}
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.help)) == 0) {
            System.out.printf("%15s   |   %s\n"
                    + "----------------------------------------------------\n", "Command", "Description");
            for (enCMD cmd : enCMD.values()) {//!!
                System.out.printf("%15s   |   %s\n", mapCMD.get(cmd), mapDesc.get(cmd));
                //prln( CMD[cmd] + "  |  " + CMDDESC[cmd] );
            }
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.q)) == 0
                || command.compareToIgnoreCase(mapCMD.get(enCMD.quit)) == 0) {
            return 1;
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.readlpn)) == 0) {
	        	if(!readFlag){
		        	readFlag = true;
		        	
		        	LinkedList<String> fileList = new LinkedList<String>();
		        	for(String arg : arguments){
		        		File f = new File(arg);
		        		fileList.add(f.getAbsolutePath());
		        	}
		        	
		        	prj.readLpn(fileList);
            }
            else{
            	File f = new File(argument1);
                Project.readLpn(f.getAbsolutePath());
            }
        }

        return 0;
    }

    static String mergeColumns(String a, String b) {
        String ret = "";
        StringTokenizer tk1 = new StringTokenizer(a, "\n");
        StringTokenizer tk2 = new StringTokenizer(b, "\n");
        while (tk1.hasMoreTokens() && tk2.hasMoreTokens()) {
            ret += tk1.nextToken() + "\t" + tk2.hasMoreTokens() + "\n";
        }
        while (tk1.hasMoreTokens()) {
            ret += tk1.nextToken() + "\n";
        }
        while (tk2.hasMoreTokens()) {
            ret += tk2.hasMoreTokens() + "\n";
        }
        return ret;
    }
}
