/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.gui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author ldmtwo
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
    LinkedList keys = new LinkedList();
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
