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
package main.java.edu.utah.ece.async.verification.platu.gui;

import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author ldmtwo
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class PrintTable {

    int width = 80;
    int col1Width = 40;

    public PrintTable() {
    }

    public PrintTable(int width) {
        this.width = width;
    }

    public PrintTable(int width, int col1Width) {
        this.width = width;
        this.col1Width = col1Width;
    }
    LinkedList<Object> keys = new LinkedList<Object>();
    HashMap<Object, Object> table = new HashMap<Object, Object>(100);

    public void put(Object left, Object right) {
        table.put((left + "").replace("\t", "     ").replace("\n", ""),
                (right + "").replace("\t", "     ").replace("\n", ""));
        keys.add(left);
    }

    public void print() {
        String line = "", line2 = "|*";
        String pr = "";
        for (int i = 0; i < width; i++) {
            line += "*";
        }
        for (int i = 0; i < width/2-1; i++) {
            line2 += " *";
        }
        System.out.printf("*%" + width + "s*\n", line);
        for (Object o : keys) {
            if ((o + "").trim().length() == 0) {
                pr += line2+" |\n";
            } else {
                pr += String.format("|%" + (col1Width) + "s | %-" + (width - col1Width - 3) + "s|\n",
                        o, table.get(o));
            }
        }
        System.out.print(pr);
        System.out.printf("*%" + width + "s*\n", line);
    }
}
