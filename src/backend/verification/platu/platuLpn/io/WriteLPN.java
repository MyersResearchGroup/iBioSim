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

package backend.verification.platu.platuLpn.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import backend.verification.platu.platuLpn.PlatuLPN;
import backend.verification.platu.platuLpn.LPNTran;
import backend.verification.platu.platuLpn.VarSet;
import backend.verification.platu.platuLpn.VarVal;

/**
 * @author ldtwo
 */
public class WriteLPN {

    public static void write(String file, PlatuLPN[] lpnArr) throws Exception {
        if (file == null || lpnArr == null) {
            throw new NullPointerException();
        }
        write(new File(file), lpnArr);
    }

    public static void write(File file, PlatuLPN[] lpnArr) throws Exception {
        if (file == null || lpnArr == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        write(new FileOutputStream(file, true), lpnArr);

    }

    public static void write(FileOutputStream out, PlatuLPN[] lpnArr) throws Exception {
        if (out == null || lpnArr == null) {
            throw new NullPointerException();
        }
        for (PlatuLPN lpn : lpnArr) {
            write(out, lpn);
        }
        out.write((".end\n").getBytes());

    }

    public static void write(String file, PlatuLPN lpn) throws Exception {
        if (file == null || lpn == null) {
            throw new NullPointerException();
        }
        write(new File(file), lpn);
    }

    public static void write(File file, PlatuLPN lpn) throws Exception {
        if (file == null || lpn == null) {
            throw new NullPointerException();
        }
        write(new FileOutputStream(file, true) {

            @Override
            public void write(byte[] b) throws IOException {
                super.write(b);
                debugPrint(">>" + new String(b).replace("\n", "\\n"));
            }
        }, lpn);
    }
static final Comparator<VarVal> vvComparator=new Comparator<VarVal>() {

            @Override
			public int compare(VarVal o1, VarVal o2) {
               return o1.getVariable().compareTo(o2.getVariable());
            }
        };

    public static void write(FileOutputStream out, PlatuLPN lpn) throws Exception {
        if (out == null || lpn == null) {
            throw new NullPointerException();
        }
        //lines 1..4
        debugPrint("writting...");
        out.write((".module " + lpn.getLabel() + "\n").getBytes());
        String[] varLbl = {"INPUTS: ", "OUTPUTS: ", "INTERNALS: "};

        VarSet vvi = lpn.getInputs();
        debugPrint("vvi.size=" + vvi.size());
        VarSet vvo = lpn.getOutputs();
        debugPrint("vvo.size=" + vvi.size());
        VarSet vvn = lpn.getInternals();
        debugPrint("vvn.size=" + vvi.size());


        String[][] tmp;
        tmp = new String[][]{//inputs, outputs, internals
                    vvi.size() == 0 ? new String[0] : vvi.toArray(new String[]{}),
                    vvo.size() == 0 ? new String[0] : vvo.toArray(new String[]{}),
                    vvn.size() == 0 ? new String[0] : vvn.toArray(new String[]{})
                };
        Arrays.sort(tmp[0]);
        Arrays.sort(tmp[1]);
        Arrays.sort(tmp[2]);

        for (int y = 0; y < 3; y++) {
            out.write((varLbl[y]).getBytes());
            if (tmp[y] == null) {
                new NullPointerException().printStackTrace();
            }
            if (tmp[y].length > 0) {
                for (int x = 0; x < tmp[y].length - 1; x++) {
                    if (tmp[y][x] == null) {
                        new NullPointerException().printStackTrace();
                    }
                    out.write((tmp[y][x] + ", \n\t").getBytes());
                }
                out.write((tmp[y][tmp[y].length - 1] + ";\n").getBytes());
            } else {
                out.write(("\n").getBytes());
            }
        }
        //line 5
        out.write(("{").getBytes());
        
        List<Integer> tmp1 = new ArrayList<Integer>();
//        for(int i = 0; i < lpn.getInitStateTimed().getMarking().length; i++)
//        	tmp1.add(lpn.getInitStateTimed().getMarking()[i]);
        Iterator<Integer> it = tmp1.iterator();

        // debugPrint("MARK>>");
        while (it.hasNext()) {
            out.write((it.next() + "").getBytes());
            if (it.hasNext()) {
                out.write((", ").getBytes());
            }
        }
        out.write(("};\n{").getBytes());
        int i = 0;

        out.write(("};\n").getBytes());
        LPNTran[] transitions=lpn.getTransitions().toArray(new LPNTran[0]);
        Comparator<LPNTran> comp=new Comparator<LPNTran>() {

            @Override
			public int compare(LPNTran o1, LPNTran o2) {
                return o1.getIndex()>o2.getIndex()?1:-1;
            }
        };
        Arrays.sort(transitions, comp);
//        Iterator<LPNTran> it3 = lpn.getTransitions().iterator();
//        LPNTran tranTmp;
        //debugPrint("TRANS>>");
        i=0;
        String[] tranArray=new String[lpn.getTransitions().size()];
      for(LPNTran t:transitions) {
            tranArray[i++]=t.toString();
        }
//        Arrays.sort(tranArray);
        for(String tran:tranArray){
            out.write((tran).getBytes());
        }
         out.write("\n.end".getBytes());
    }

	private static void debugPrint(Object o) {
        // System.out.println(o);
    }
}
