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
package edu.utah.ece.async.dataModels.verification.platu.common;

import java.io.Console;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Definitions of commonly used constants, and functions for the entire project.
 * 
 * @author ldtwo
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Common {
	public static final int INFINITY = Integer.MAX_VALUE;
	public static final  int LPN_INT_MAX = Integer.MAX_VALUE;
	public static final  int LPN_INT_MIN = 0;
	public static final DecimalFormat FLOAT=new DecimalFormat("00.000");
	public static final DecimalFormat LONG=new DecimalFormat("###,###,###,##0");
	
	enum Verif_mode { verif_flat, verif_compositional }{}
	enum Timing { timing_untimed, timing_timed }{}
	
	static public   void pr(Object o) {
		System.out.print(o);
    }

	static  public void prln(Object o) {
		System.out.println(o);
    }

    static public void pErr(Object o) {
    	System.err.println(o);
    }
    public static char getChar(){
        try {
            return (char) System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
            return ' ';
        }
    }
    
    public  static  boolean brk(Console con) {
    	char cmd = 'y';
        prln("Do you want to continue? y/n");
        while (true) {
            // prln("simulate " + iterations++ + " >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
            if (con == null) {
                break;
            }
			String line = con.readLine();
			if (line == null) {
			    pErr("line==null");
			}
			else if (line.length() > 0) {
			    cmd = line.charAt(0);
			    if (cmd == 'n' || cmd == 'y') {
			        break;
			    }
				prln("Wrong command. Try again");
			} else {
			    pErr("type 'y' OR 'n'");
			}
        }
        if (cmd == 'n') {
            return false;
        }
        return true;
    }
    
    public static  Collection<Integer> toList(int[] arr) {
        TreeSet<Integer> l = new TreeSet<Integer>();
        for (int i : arr) { 
            l.add(i);
        }
        return l;
    }
    
    public static<T>  Collection<T> toList(T[] arr) {
        HashSet<T> l = new HashSet<T>();
        for (T i : arr) {
            l.add(i);
        }
        return l;
    }

    public static  int[] toArray(Collection<Integer> set) {
        int[] arr = new int[set.size()];
        int idx = 0;
        for (int i : set) {
            arr[idx++] = i;
        }
        return arr;
    }
    
  static final public  void forceGarbageCollection() {
//      for (int i = 0; i < 5; i++) {
//            try {
//                Thread.sleep(15);
//        garbage(i*1023);
//            } catch (Exception ex) {
//                Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
//            }
//      }
          System.gc();
      for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(10);
            } catch (Exception ex) {
                Logger.getLogger(Common.class.getName()).log(Level.SEVERE, null, ex);
            }
      }

    }

    /**
     * Call this to create garbage for the GC to collect. It will be more likely to
     * free other garbage in the heap. THis will give more accurate initial memory
     * readings.
     * @return
     */
   static final   int garbage(int l) {
        int[] nums = {1, 2, 43, 4, 4, 5, 23, 5, 5, 1, 2, 3, 4, 21, 34, 23, 4};
        int tot = 0;
        String ss = "";
        for (int i : nums) {
            for (int j : nums) {
                for (int k : nums) {
                    String s = i + "" + j + "" + k+l;
                    ss += s;
                    s += s + s + i;
                    tot += s.length() + ss.length();
                }
            }
        }
        
        return tot;
    }
}
