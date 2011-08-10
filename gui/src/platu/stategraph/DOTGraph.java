/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.stategraph;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * @author ldmtwo
 */
public class DOTGraph extends OutputDOT {

    HashMap<String, DOTGraph> subgraph = new HashMap<String, DOTGraph>(1);
    HashSet<String> edges = new HashSet<String>(1);
    HashMap<String, String> nodes = new HashMap<String, String>(1);

    public int size() {
        return edges.size();
    }

    public void addNode(String labelWithoutSpaces) {
        if (!nodes.containsKey(labelWithoutSpaces)) {
            nodes.put(labelWithoutSpaces, " [shape=plaintext]");
        }
    }

    public void addNode(String labelWithoutSpaces, String color) {
        if (!nodes.containsKey(labelWithoutSpaces)) {
            nodes.put(labelWithoutSpaces, " [shape=plaintext, color=" + color + "]");
        }
    }

    public void addDeadlockNode(String labelWithoutSpaces) {
        nodes.put(labelWithoutSpaces, " [shape=ellipse,style=filled,fillcolor=\"#0055ffff\"]");
    }

    public void addDisableNode(String labelWithoutSpaces) {
        nodes.put(labelWithoutSpaces, " [shape=diamond,style=filled,fillcolor=\"#00ffff5f\"]");
    }

    public void addInitNode(String labelWithoutSpaces) {
        nodes.put(labelWithoutSpaces, " [shape=Mdiamond,color=red2]");
    }

    public void addEdge(String labelWithoutSpaces) {
        edges.add(labelWithoutSpaces);
    }

    public void addEdge(Object labelWithoutSpaces_A, Object labelWithoutSpaces_B, Object tranLabel) {
        edges.add(labelWithoutSpaces_A + " -> " + labelWithoutSpaces_B + " [label=\"" + tranLabel + "\"]");
    }

    public void addEdge(Object labelWithoutSpaces_A,
            Object labelWithoutSpaces_B, Object tranLabel, String color) {
        edges.add(labelWithoutSpaces_A + " -> " + labelWithoutSpaces_B
                + " [label=\"" + tranLabel + "\""
                + ", color=\"" + color
                + "\"]");
    }

    public void addEdgeWithColor(Object labelWithoutSpaces_A,
            Object labelWithoutSpaces_B, String color) {
        edges.add(labelWithoutSpaces_A + " -> " + labelWithoutSpaces_B
                + " [color=\"" + color
                + "\"]");
    }

    public void addEdge(Object A, Object B) {
        edges.add(A + " -> " + B);
    }
    String head = "digraph sg{\n"
            + "	graph [splines=true overlap=false truecolor]\n"
            //+ "	graph [splines=true overlap=false truecolor bgcolor=\"#ff00005f\"]\n"
            + "node [style=filled fillcolor=\"#00ff005f\""
            //        + " fixedsize=true,width=0.5"
            + "]\n" //	                + "pack=\"true\"\n"
            //                + "rankdir=LR\n"
            //               + "ratio=\"auto\"\n"
            //                + "ranksep=.75\n"
            //                + "overlap=false\n"
            ;
    String foot = "}";

    public void write(String file) {
        write(new File(file));
    }

    public void write(File f) {
        String data = head;
        for (String s : nodes.keySet()) {
            data += s + " " + nodes.get(s) + ";\n";
        }
        for (String t : edges) {
            data += t + ";\n";
        }
        data += foot;
        string2File(f, data);
        String format = "png";
//        DOT2Image(format, f);


        //LAYOUTS:  circo dot fdp neato osage patchwork sfdp twopi
//DOT2Image(format, f,"sfdp");
        if (nodes.size() < 200) {
            DOT2Image(format, f, "dot");
        }
        DOT2Image(format, f, "neato");
//DOT2Image(format, f,"twopi");
//DOT2Image(format, f,"circo");
//DOT2Image(format, f,"fdp");
//DOT2Image(format, f,"osage");
//DOT2Image(format, f,"patchwork");

    }
}
