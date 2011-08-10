/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu;

import java.io.File;
import java.io.FileNotFoundException;
import platu.project.Project;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import platu.lpn.LPN;
import platu.lpn.io.WriteLPN;
import platu.stategraph.StateGraph;

public class Interpretor {

    public static boolean OLD_LPN = false;
    private boolean readFlag = false;
    final int MAXHISTORY = 10;
    boolean FLATMODEL = false;
    String[] commandHistory = new String[MAXHISTORY];
    static final LinkedHashMap<enCMD, String> mapCMD = (new LinkedHashMap<enCMD, String>(20));
    static final LinkedHashMap<enCMD, String> mapDesc = (new LinkedHashMap<enCMD, String>(20));

    enum enCMD {
        compAnalysis, findallsg, loadall, skip, set, last, liststg, sglist, readcsp,
        setinterface, savesg, savelpn, join, compose, removedummypn,
        rdpn, findsg, findrsg, simulate, sim, getrsgflatabs, getsg, addenv,
        flattenpns, mergebgpnall, getsgmaximal, getsgmaxenv, getsgflat,
        mergesg, sgabst, abstractsg, sgaf, autofailure,
        sgreduce, reduce, deletesg, help, quit, q, interactive,
        print, chkpie, sghide, abstStateTran, hidevar, readrsg,
        readlpn, chkpi, del, draw
    };
    
    static final String[] CMD = {"compAnalysis", "findallsg", "loadall", "skip", "set", "last", "liststg", "sglist", "readcsp",
        "setinterface", "savesg", "savelpn", "join", "compose", "removedummypn",
        "rdpn", "findsg", "findrsg", "simulate", "sim", "getrsgflatabs", "getsg", "addenv",
        "flattenpns", "mergebgpnall", "getsgmaximal", "getsgmaxenv", "getsgflat",
        "mergesg", "sgabst", "abstractsg", "sgaf", "autofailure",
        "sgreduce", "reduce", "deletesg", "help", "quit", "q", "interactive",
        "print", "chkpie", "sghide", "abstStateTran", "hidevar", "readrsg",
        "readlpn", "chkpi", "del", "draw"
    };
    static final String[] CMDDesc = {
        "[compAnalysis]", "[findallsg]", "[loadall]", "  [skip]", "  [set]", "re-enter last command", "  [liststg]", "  [sglist]", "  [readcsp]",
        "  [setinterface]", "+ [savesg]", "+ [savelpn]", "+ [join]", "+ [compose]", "  [removedummypn]",
        "  [rdpn]", "+ [findsg]", "+ [findrsg]", "+ [simulate]", "+ [sim]", "  [getrsgflatabs]", "+ [getsg]", "  [addenv]",
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
            // prln(x + ": " + CMD[x] + "\t" + CMDDesc[x]);
            x++;
        }
    }

    public int interpretcommand(Project prj, final String commandline) {
        if (prj == null) {
            new Exception("Main: interpretcommand: prj is NULL").printStackTrace();
        }

        String systemcommand = commandline;
        systemcommand += " ";
        LinkedList<String> arguments = new LinkedList<String>();
        String command;
        String argument1 = "";
        String argument2 = "";
        String argument3 = "";

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
        command = arguments.removeFirst();//front();

        int argumentcount = 0;
        if (arguments.size() > 0) {
            argument1 = arguments.peekFirst();//front();
            argumentcount++;
        }
        if (arguments.size() > 1) {
            argument2 = arguments.get(1);
            argumentcount++;
        }
        if (arguments.size() > 2) {
            argument3 = arguments.get(2);
            argumentcount++;
            argumentcount++;
        }

        // mode changes
        if (command.charAt(0) == '/' && command.charAt(1) == '/') {
            return 0;
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.skip)) == 0) {
            return 0;
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.del)) == 0) {
//            DesignUnit[] duList = (new LinkedList<LPN>(
//                    prj.getDesignUnitSet())).toArray(new DesignUnit[0]);
            for (LPN lpn : prj.getDesignUnitSet()) {
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
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.getrsgflatabs)) == 0) {
            //project.findFlatRSGabs( String("flatrsgabs") );
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.loadall)) == 0) {
//            try {
//                File[] files = new File(argument1).listFiles();
//                if (files == null) {
//                    new FileNotFoundException("Invalid directory: " + argument1).printStackTrace();
//                }
//                for (File f : files) {
//                    String s = f.getName().toLowerCase();
//                    if (s.endsWith(".lpn") || s.endsWith(".xml") || s.endsWith(".xlpn")) {
//                        try {
//                          
//                                Set<LPN> lpnSet = prj.readLpn(f.getAbsolutePath());
//                                for (LPN lpn : lpnSet) {
//                                    WriteLPN.write(TimedStateGraph.LPN_PATH + "\\"
//                                            + Main.RESULT_FOLDER + "\\" + f.getName() + "_" + lpn.getLabel(), lpn);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.findallsg)) == 0) {
                prj.findLocalSG();
        } else if (command.compareToIgnoreCase(mapCMD.get(enCMD.findrsg)) == 0) {
            prj.compositionalAnalysis();
        }else if (command.compareToIgnoreCase(mapCMD.get(enCMD.compAnalysis)) == 0) {
    		prj.compositionalAnalysis();
        }else if (command.compareToIgnoreCase(mapCMD.get(enCMD.draw)) == 0){
        	if(argumentcount > 0){
        		for(LPN lpn : prj.getDesignUnitSet()){
        			if(argument1.equals(lpn.getLabel())){
        				StateGraph sg = (StateGraph) lpn;
        				System.out.print("drawing " + lpn.getLabel() + "...");
        				sg.draw();
        				System.out.println("Done");
        			}
        		}
        	}
        	else{
        		for(LPN lpn : prj.getDesignUnitSet()){
        			StateGraph sg = (StateGraph) lpn;
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
            if(Options.getNewParser()){
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
		        	System.err.println("error: only one readLpn command is allowed");
		        	System.exit(1);
		        }
            }
            else{
            	File f = new File(argument1);
                prj.readLpn(f.getAbsolutePath());
            }
        }

        return 0;
    }

    String mergeColumns(String a, String b) {
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
