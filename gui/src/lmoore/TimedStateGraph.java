//package lmoore;
//
//import platu.ui.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import platu.Main;
//import static platu.Main.MAX_MEM;
//import static platu.Main.mergeColumns;
//import static java.lang.Runtime.getRuntime;
//import java.util.*;
//import javax.swing.Timer;
//import lmoore.zone.Zone;
//import platu.expression.VarNode;
//import platu.lpn.LPN;
//import platu.lpn.LPNTran;
//import platu.lpn.LPNTranSet;
//import platu.lpn.VarSet;
//import platu.project.DesignUnit;
//import platu.project.Project;
//import platu.stategraph.StateGraph;
//import platu.stategraph.state.State;
//
//public class TimedStateGraph extends StateGraph {
//
//    private static int graphIdx = 0;
//    private Project designPrj;
//    private HashMap<TimedState, LPNTranSet> firedTranSet = new HashMap<TimedState, LPNTranSet>();
//
//    public TimedStateGraph(Project prj, String label, VarSet inputs, VarSet outputs,
//            VarSet internals, LPNTranSet transitions, State initState) {
//        super(prj, label, inputs, outputs, internals, transitions, initState);
//        if (prj == null || label == null || inputs == null ||//
//                outputs == null || internals == null
//                || transitions == null || initState == null) {
//            new NullPointerException().printStackTrace();
//        }
//        if (ENABLE_PRINT) {
//            System.out.println("new LPN()1: \t" + description());
//        }
//
//        counts[0]++;
//    }
//    
//    public TimedStateGraph(Project prj, String label, VarSet inputs, VarSet outputs,
//            VarSet internals, HashMap<String, VarNode> varNodeMap, LPNTranSet transitions,  
//            HashMap<String, Integer> initialVector, int[] initialMarkings, Zone initialZone) {
//        super(prj, label, inputs, outputs, internals, varNodeMap, transitions, initialVector, initialMarkings, initialZone);
//        if (prj == null || label == null || inputs == null ||//
//                outputs == null || internals == null
//                || transitions == null) {
//            new NullPointerException().printStackTrace();
//        }
//        if (ENABLE_PRINT) {
//            System.out.println("new LPN()1: \t" + description());
//        }
//
//        counts[0]++;
//    }
//
//    /**
//     *
//     * @param label
//     * @param designPrj
//     * @param inputs
//     * @param outputs
//     * @param internals
//     */
//    public void setTimedStateGraph(String label, Project designPrj,
//            HashSet<String> inputs, HashSet<String> outputs,
//            HashSet<String> internals) {
//        this.label = label;
//        this.designPrj = designPrj;
//        this.inputs = inputs;
//        this.outputs = outputs;
//        this.internals = internals;
//    }
//
//    /**
//     *
//     * @return
//     */
//    @Override
//    public String toString() {
//        return String.format("%5s     %5s", label //+"  sgTranCount=" + sgTranCount
//                ,
//                //                "stateIncSet size=" + stateIncSet.size(),
//                //                "stateOutSet size=" + stateOutSet.size(),
//                "|States| =" + allStates.size());
//    }
//    static int STATE_NUM = Integer.MAX_VALUE;
//
//    // Find if '(current_state, lpn_tran, next_state)' exists in this SG. If so, return 'next_state';
//    // Otherwise, fire 'lpn_tran', add '(current_state, lpn_tran, next_state)' into this SG, and return 'next_state'.
//    public final TimedState next(TimedState current_state, LPNTran lpn_tran) {
//
//
//        return null;
//    }//!
//
//    /**
//     *
//     * @param state
//     * @return
//     */
//    public final TimedState find(TimedState state) {
////        counts[1]++;
//        boolean b = allStates.contains(state);
//        if (b) {
//            return null;
//        }
//        allStates.add(state);
////     try {
////            state.serialize(Main.RESULT_FOLDER + "\\ST." + state.hashCode()+".ser");
////        } catch (FileNotFoundException ex) {
////            Logger.getLogger(StateGraph.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (IOException ex) {
////            Logger.getLogger(StateGraph.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        return state;
//    }//!
//    //boolean isNew(State ) ;
//
//    /**
//     *
//     * @param level
//     * @param o
//     */
//    public static void prDbg(int level, Object o) {
////        if (level >= PRINT_LEVEL) {
////            out.println(o);
////        }
//    }
//
//    /**
//     *
//     * @param o
//     */
//    public static void pErr(Object o) {
//        System.err.println(o);
//    }
//
////    public String disjunction() {
////        String expression = "false";
////        for (State s : allStates) {
////            expression += "||(" + s.conjunction(allTrans) + ")";
////        }
////        return expression;
////    }
//
//    public void update() {
////        SystemState states = new SystemState(allStates);
////        for (State st : states.keySet()) {
////            r_findSG(st, allTrans);
////        }
//    }
//
//    public void writeImage() {
//        try {
//            Thread dotThread = new DotThread();
//            dotThread.start();
//            dotThread.join();
//        } catch (InterruptedException ex) {
//            Logger.getLogger(TimedStateGraph.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    class DotThread extends Thread {
//
//        @Override
//        public void run() {
////            try {
////                String dot1 = OutputDOT.toString(sgInitState, stateIncSet,
////                        allStates);
////                String timed = TIMED_ANALYSIS ? "_TIMED" : "_UNTIMED";
////                String stateInfo = "_" + allStates.size() + "-states_" + sgTranCount + "-trans";
////                File f1 = new File(LPN_PATH + "\\" + Main.RESULT_FOLDER + "\\"
////                        + label + timed + stateInfo + "_" + Integer.toHexString(graphIdx++)
////                        + ".sg");
////                OutputDOT.string2File(f1, dot1);
////                OutputDOT.DOT2File("png", f1);
////                this.interrupt();
////            } catch (Exception e) {
////                e.printStackTrace(platu.Main.out);
////            }
//        }
//    }
//
//    /**
//     *
//     * @param z
//     * @param initEnSet
//     */
//    void initZone(Zone z, LPNTranSet initEnSet) {
//        if (ENABLE_PRINT) {
//            prDbg(1, z);
//        }
//        //extend DMB with new timers
//        for (LPNTran t : initEnSet) {
//            if (!z.containsKey(t.getLabel())) {
//                z.allocate(t.getLabel());
//            }
//        }
//        for (LPNTran i : initEnSet) {
//            //Mi0 = M0i = 0;
//            z.put(i.getLabel(), 0, 0);
//            z.put(0, i.getLabel(), 0);
//            for (LPNTran k : initEnSet) {
//                //Mik = Mki = 0;
//                z.put(i.getLabel(), k.getLabel(), 0);
//                z.put(k.getLabel(), i.getLabel(), 0);
//            }
//            //            for (LPNTran k : setB) {
//            //                z.put(i.getLabel(), k.getLabel(), z.get(0, k.getLabel()));
//            //                z.put(k.getLabel(), i.getLabel(), z.get(k.getLabel(), 0));
//            //            }
//        }
//
//        if (ENABLE_PRINT) {
//            prDbg(1, z);
//        }
//        //advance time
//        for (LPNTran t : initEnSet) {
//            boolean b = z.put(0, t.getLabel(), t.getDelayUB());
//            if (!b) {
//                new Exception("zone.put==false: lpnt=" + t + "\n\tzone=" + z).printStackTrace(platu.Main.out);
//                getChar();
//            }
//        }
//
//        if (ENABLE_PRINT) {
//            prDbg(1, z);
//        }
//        //tighten loose bounds
//        z.recanonicalize();
//        //normalize?
//        //z.normalize(tran.getDelayUB(), fired);
//        if (ENABLE_PRINT) {
//            prDbg(1, z);
//        }
//        if (!z.isValid()) {
//            new Exception("zone.isValid==false: lpnt=INIT_TRAN\n\tzone=" + z).printStackTrace(platu.Main.out);
//
//            getChar();
//        }
//    }
//
//    /**
//     *
//     * @param zone
//     * @param fired
//     * @param firedTran
//     * @param nextSet
//     * @param currentSet
//     */
//    void updateZone(Zone zone, int fired, LPNTran firedTran,
//            LPNTranSet nextSet, LPNTranSet currentSet, POSETMatrix pm) {
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        zone.validate();
//        //restrict
//        //if (Main.ZONE_OPTIMIZE_INF)
//        {
//            infTimer = !((zone).contains(fired));
//            //           out.println("timer: "+fired+"\tinf?="+infTimer+"\tinZone?="+
//            //                   (((Zone3) zone).contains(fired)));
//        }
//        if (!infTimer) {
//            int val = zone.get(fired, 0);
//            if (val == Integer.MIN_VALUE + 1) {
//                new Exception("zone.get==-1: tran=" + firedTran
//                        + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
//                getChar();
//            }
//            if (val > -firedTran.getDelayLB()) {
//                zone.put(fired, 0, -firedTran.getDelayLB());
//            }
//            zone.restrict(fired, firedTran.getDelayLB());
//            if (ENABLE_PRINT) {
//                prDbg(1, zone);
//            }
//
//            //tighten loose bounds
//            zone.recanonicalize();
//            if (ENABLE_PRINT) {
//                prDbg(1, zone);
//            }
//            //project out fired rule
//            zone.project(fired);
//        }
//
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        //extend DMB with new timers
//        //newEnSet = next - current
//        LPNTranSet newEnSet = new LPNTranSet(nextSet);
//        newEnSet.removeAll(currentSet);
//        //setB = next - new
//        LPNTranSet nextMinusNew = new LPNTranSet(nextSet);
//        nextMinusNew.removeAll(newEnSet);
//        //allocate the space to be modified later
//        for (LPNTran t2 : newEnSet) {
//            if (!zone.containsKey(t2.getLabel())) {
//                zone.allocate(t2.getLabel());
//            }
//        }
//        for (LPNTran i : newEnSet) {
//            lblI = i.getLabel();
//            infTimer = !((zone).contains(lblI));
//            //out.println(lblK+"\t(newEnSet)"+(infTimer?"tran is inf":"not inf "));
//            if (infTimer) {
//                // continue;
//            }
//
//            //Mi0 = M0i = 0;
//            zone.put(lblI, 0, 0);
//            zone.put(0, lblI, 0);
//            for (LPNTran k : newEnSet) {
//                lblK = k.getLabel();
//                //if (Main.ZONE_OPTIMIZE_INF)
//                {
//                    infTimer = !((zone).contains(lblK));
//                    //out.println(lblK+"\t(newEnSet)"+(infTimer?"tran is inf":"not inf "));
//                    if (infTimer) {
//                        //continue;
//                    }
//                }
//                //Mik = Mki = 0;
//                zone.put(lblI, lblK, 0);
//                zone.put(lblK, lblI, 0);
//            }
//            for (LPNTran k : nextMinusNew) {
//                lblK = k.getLabel();
//                //if (Main.ZONE_OPTIMIZE_INF)
//                {
//                    infTimer = !((zone).contains(lblK));
//                    if (infTimer) {
//                        continue;
//                    }
//                }
//                //M(ik)=M(0k)
//                zone.put(lblI, lblK, zone.get(0, lblK));
//                zone.put(lblK, lblI, zone.get(lblK, 0));
//            }
//        }
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        //advance time
//        for (LPNTran t : nextSet) {
//            boolean b = zone.put(0, t.getLabel(), t.getDelayUB());
//            if (!b) {
//                new Exception("zone.put==false: lpnt=" + t + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
//                getChar();
//            }
//        }
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        //tighten loose bounds
//        zone.recanonicalize();
//        //normalize?
//        //z.normalize(tran.getDelayUB(),tran.getDelayLB(), fired);
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        if (!zone.isValid()) {
//            new Exception("zone.isValid==false: lpnt=" + firedTran + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
//            getChar();
//        }
//        //z.validate();
//        (zone).remove(new Integer(fired));
//        if (USING_POSET) {
//            updatePoset(pm, zone, firedTran, newEnSet);
//        }
//    }
//
//    /**
//     *POSET
//     * @param pmatrix
//     * @param zone
//     * @param fired
//     * @param newLpntSet
//     */
//    void updatePoset(POSETMatrix pmatrix, Zone zone, LPNTran fired, LPNTranSet newLpntSet) {
////        int firedLbl = fired.getLabel();
////        //ALGORITHM: update POSET matrix; page 282
////        //foreach ei in P (for each event in the POSET matrix
////        //DEF: ei is casual to fj if this event is then enabling event for
////        //the last rule that fires before fj fires
////        //    if(ei is casual to fj){
////        //        pji = -l;
////        //        pij = u;
////        //    }else if(ei directly proceeds ej){
////        //        pji = -l;
////        //        pij = inf;
////        //    }else{
////        //        pji = inf;
////        //        pij = inf;
////        //    }
////        TreeSet<Integer> poset = pmatrix.treeKeySet();
////        for (LPNTran tran : newLpntSet) {
////            //if(i < firedLbl)
////            {
////                pmatrix.put(firedLbl, tran.getLabel(), -fired.getDelayLB());
////                pmatrix.put(tran.getLabel(), firedLbl, fired.getDelayUB());
////                //                }else if(i>fired){
////                //                    pmatrix.put(fired,i,-tran.getDelayLB());
////                //                    pmatrix.put(i,fired,INF);
////            }
////            //                else{
////            //                    pmatrix.put(firedLbl,i,INF);
////            //                    pmatrix.put(i,firedLbl,INF);
////            //                }
////        }
////        //recan and project
////        pmatrix.recanonicalize();
////        pmatrix.project(firedLbl);
////        //foreach ri in Ren
////        //    Mi0 = 0;
////        //    M0i = ui;
////        //    foreach rj in Ren
////        //        mij = pij;
////        for (LPNTran i : newLpntSet) {
////            zone.put(i.getLabel(), 0, 0);
////            zone.put(0, i.getLabel(), i.getDelayUB());
////            for (LPNTran j : newLpntSet) {
////                zone.put(i.getLabel(), j.getLabel(), pmatrix.get(i.getLabel(), j.getLabel()));
////            }
////        }
////        zone.recanonicalize();
////        //normalize?
//    }
//
//    /**
//     *
//     * @param lpn
//     */
//    public void findSG() {
//        try {
//            Timer gcTimer = new Timer(10000, new ActionListener() {
//
//                long lastTime = System.currentTimeMillis();
//                final long startTime = lastTime;
//                int lastStates = allStates.size();
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    try {
//                        int numStates = allStates.size();
//                        long diff1 = (System.currentTimeMillis() - lastTime) / 1000;
//                        System.gc();
////                        System.gc();
////                        System.gc();
////                        System.gc();
////                        System.gc();
////                        System.gc();
////                        System.gc();
////                        System.gc();
//                        long mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
//                        int diff2 = numStates - lastStates;
//                        long time = System.currentTimeMillis() - startTime;
//                        long time2 = time / 1000;
//                        long h = time2 / 3600, m = (time2 % 3600) / 60;
//                        double s = time / 1000f - h * 3600 - m * 60;
//                        String timeStr = String.format("%d:%02d:%6.6s", h, m, (s));
//
//                        out.printf("F:%-10s U: %-10s T: %-10s M: %-10s %s states/sec  %s B/state  %s\n",
//                                Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb",
//                                mem / 1024 + " mb",
//                                Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb",
//                                Runtime.getRuntime().maxMemory() / 1024 / 1024 + " mb",
//                                String.format("  %5s   %5s  ", label,
//                                "|States| =" + numStates) + "  " + diff2 / diff1,
//                                1024 * mem / numStates,
//                                timeStr);
//                        System.out.printf("F:%-10s U: %-10s T: %-10s M: %-10s %s states/sec  %s B/state  %s\n",
//                                Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb",
//                                mem / 1024 + " mb",
//                                Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb",
//                                Runtime.getRuntime().maxMemory() / 1024 / 1024 + " mb",
//                                String.format("%5s   %5s", label,
//                                "|States| =" + numStates) + "  " + diff2 / diff1,
//                                1024 * mem / numStates,
//                                timeStr);
//                        lastTime = System.currentTimeMillis();
//                        lastStates = numStates;
//
////                    out.println("thread interupted:" + Thread.interrupted()
////                            + "\n" + Thread.getAllStackTraces().entrySet().toString().replace("Thread[", "\n\tThread["));
////                    for (Map.Entry<Thread, StackTraceElement[]> ent : Thread.getAllStackTraces().entrySet()) {
////                        out.printf("%s; %s; %s; %s; %s; \n",
////                                ent.getKey().getName(),
////                                ent.getKey().getPriority(),
////                                ent.getKey().getState(),
////                                ent.getKey().isDaemon(),
////                                ent.getKey().isInterrupted());
////                        if (ent.getKey().getName().toLowerCase().contains("thread")) {
////                            for (StackTraceElement ste : ent.getValue()) {
////                                out.println("\t" + ste);
////                            }
////                        }
////                    }
//                    } catch (Exception ex) {
//                    }
//                }
//            });
//            gcTimer.start();
//            label = getLabel();
//            Main.ZONE_OPTIMIZE_INF = Main.ZONE_OPTIMIZE_INF && TIMED_ANALYSIS;
//            OPTIMIZE_INF = Main.ZONE_OPTIMIZE_INF;
//            out.println("running findsg on " + getLabel());
//            //================================== first recursion init
//            TimedState currentState = getInitStateTimed();
//
//            if (ENABLE_PRINT) {
//                prDbg(2, "INIT STATE: " + currentState);
//            }
//            allTrans = new LPNTranSet(getTransitions());
////            for (LPNTran t1 : allTrans) {
////                for (LPNTran t2 : allTrans) {
////                    t1.mSet.addAll(LPNTran.getMarkedLpnTrans(allTrans, t2.getPostSet()));
//////                    for (int i : t2.getPostSet()) {
//////                        if (t1.getPreSet().contains(i)) {
//////                            t1.mSet.add(t2);
//////                        }
//////                    }
////                }
////            }
//            if (ENABLE_PRINT) {
//                prDbg(4, "LPNT: " + getInputs());
//            }
//            if (PRINT_LEVEL > 6) {
//                LPNTran.ENABLE_PRINT = false;
//            }
//            if (PRINT_LEVEL > 6) {
//                Main.ZONE_ENABLE_PRINT = false;
//            }
//            if (PRINT_LEVEL > 9) {
//                ENABLE_PRINT = false;
//            }
//            LPNTranSet currentEnabledTransitions = getEnabledLpnTrans(currentState, null, null, false);
//
////            currentEnabledTransitions.addAll(allTrans.subset(currentState.getEnabledSet()));
//
//            int enabledcount = currentEnabledTransitions.size();
//            // terminate algorithm if there are no transitions initially enabled to fire
//            if (enabledcount == 0 && ENABLE_PRINT) {
//                prDbg(10, "StateGraph:\t" + label + ":\tVerification failed: deadlocked at initial state.");
//                return;
//            } //updateZone2A(currentEnabledTransitions, null, currentState);
//            //=================================== 2...4) update zone ==================================
//            MemoryChart mem = null;
//            StateChart st = null;
//            if (DRAW_MEMORY_GRAPH) {
//                mem = new MemoryChart(this);
//                mem.start();
//            }
//            if (DRAW_STATE_GRAPH) {
//                st = new StateChart(this);
//                st.start();
//            }
//            long time = System.currentTimeMillis();
//            if (TIMED_ANALYSIS) {
//                //initZone(currentState.getZone(), currentEnabledTransitions);
//            }
//
//
//            if (PRINT_LEVEL < 6) {
//                out.println(currentState);
//            }
//            sgInitState = currentState;
//            allStates.add(currentState);
//            if (OPTIMIZE_INF) {
////                LPNTran.getTimeEnabledLpnTrans(allTrans, currentState);
//            }
//            r_findSG(currentState.clone(), currentEnabledTransitions);
////            if (USING_POSET) {
////                POSETMatrix pm = new POSETMatrix();
//////                r_findSG(currentState.clone(), currentEnabledTransitions, -1, pm);
////            } else {
//////                LPNTranSet readySet = LPNTran.getMarkedLpnTrans(allTrans,
//////                        LPNTran.toList(currentState.getMarking().toArray()));
//////                        //LPNTran.getMarkedLpnTrans(allTrans, currentState);
//////
//////                System.out.println("READY:   " + readySet);
//////                System.out.println("ENABLED: " + currentEnabledTransitions);
//////                r_findSG(currentState.clone(), currentEnabledTransitions, readySet);
////            }
//            time = System.currentTimeMillis() - time;
//            System.gc();
//            System.gc();
//            System.gc();
//            long memUse = getRuntime().totalMemory() - getRuntime().freeMemory();
//            MAX_MEM = MAX_MEM > memUse ? MAX_MEM : memUse;
//
//            if (SHOW_STATE_INC_TREE) {
//                //StateTree tree = new StateTree(stateIncSet);
//                //getChar();
//            }
//            if (DRAW_MEMORY_GRAPH) {
//                mem.running = false;
//            }
//            if (DRAW_STATE_GRAPH) {
//                st.running = false;
//            }
//            sgTranCount = allStates.size();
//
//            long time2 = time / 1000;
//            long h = time2 / 3600, m = (time2 % 3600) / 60;
//            double s = time / 1000f - h * 3600 - m * 60;
//            String timeStr = String.format("%d:%02d:%6.6s", h, m, (s));
//            prDbg(10, this + "\tTIME: " + timeStr + "\t" + memUse / 1024 + " Kb ");
//            System.out.println(this + "\tTIME: " + timeStr + "\t" + memUse / 1024 + " Kb ");
//
//            if (OUTPUT_DOT) {
//                Thread dotThread = new DotThread();
//                dotThread.start();
//            }
//
//
//            //dotThread.join();
//            //String debugOptions=
//            //String.format("%22s: %-10s\n", "LPN_PATH",LPN_PATH)+
//            //String.format("%22s: %-10s\n", "DOT_PATH",DOT_PATH)+
//            //String.format("%22s: %-10s\t", "STOP_ON_ERROR",STOP_ON_ERROR)+
//            //String.format("%22s: %-10s\t", "USING_POSET",USING_POSET)+
//            //String.format("%22s: %-10s\n", "STOP_ON_FAILURE",STOP_ON_FAILURE)+
//            //String.format("%22s: %-10s\t", "OPTIMIZE_INF",OPTIMIZE_INF)+
//            //String.format("%22s: %-10s\t", "TIMED_ANALYSIS",TIMED_ANALYSIS)+
//            //String.format("%22s: %-10s\n", "INTERACTIVE_MODE",INTERACTIVE_MODE)+
//            //String.format("%22s: %-10s\t", "OPEN_STATE_EXPLORER",OPEN_STATE_EXPLORER)+
//            //String.format("%22s: %-10s\t", "DRAW_JAVA_GRAPH",DRAW_JAVA_GRAPH)+
//            //String.format("%22s: %-10s\n", "DRAW_STATE_GRAPH",DRAW_STATE_GRAPH)+
//            //String.format("%22s: %-10s\t", "DRAW_MEMORY_GRAPH",DRAW_MEMORY_GRAPH)+
//            //String.format("%22s: %-10s\t", "OUTPUT_DOT",OUTPUT_DOT)+
//            //String.format("%22s: %-10s\n", "PRINT_LEVEL",PRINT_LEVEL)+
//            //String.format("%22s: %-10s\n", "ENABLE_PRINT",ENABLE_PRINT);
//            //out.print(debugOptions);
//        } catch (Exception ex) {
//            Logger.getLogger(TimedStateGraph.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    static int counter = 0;
//
//    /**
//     * Recursive part
//     * @param CURRENT_STATE
//     * @param CURRENT_ENABLED_TRANSITIONS
//     * @param CURRENT_FIRED
//     * @return
//     */
//    public int r_findSG(final TimedState CURRENT_STATE, final LPNTranSet CURRENT_ENABLED_TRANSITIONS) {
//        counts[2]++;
//        if (error && STOP_ON_ERROR) {
//            return ERROR;
//        }
//        if (failed && STOP_ON_FAILURE) {
//            return VERIFICATION_FAILED;
//        }
//        if (stackHeight > Main.MAX_STACK_HEIGHT) {
//            error = true;
//            return 32;
//        }
//        if (INTERACTIVE_MODE) {
//            getChar();
//        }
//
//        //vars to be reused
//        int RETURN_STATUS = 0;
//        LPNTran firedTran = null;
//        Iterator<LPNTran> cetIter;
//        int firedLabel = -1;
//
//        //==================================== 5) select en tran (timed) =====================================
//
//        if (OPTIMIZE_INF) {
//            Main.ZONE_OPTIMIZE_INF = false;
//        }
//        LPNTranSet currentEnabledTimedTransitions;
//        if (TIMED_ANALYSIS) {
//            currentEnabledTimedTransitions = getEnabledLpnTrans(CURRENT_STATE, firedTran, CURRENT_ENABLED_TRANSITIONS, false);
//
//
//        } else {
//            currentEnabledTimedTransitions = CURRENT_ENABLED_TRANSITIONS;
//        }
//        cetIter = currentEnabledTimedTransitions.iterator();
//        if (ENABLE_PRINT) {
//            prDbg(9, "ENABLED TRANS: " + currentEnabledTimedTransitions.size() + "\t" + currentEnabledTimedTransitions.toString());
//        }
//        if (OPTIMIZE_INF) {
//            Main.ZONE_OPTIMIZE_INF = true;
//        }
//        LPNTran selectedTransition = null;
////::::::::::::::::::::::::::::::::::::::::::::: SIMULATE code
//        if (SIMULATE) {
//            LPNTran tmpLPNTran;
//            Iterator<LPNTran> iter = currentEnabledTimedTransitions.iterator();
//            while (iter.hasNext()) {
//                tmpLPNTran = iter.next();
//                if (tmpLPNTran == null) {
//                    new NullPointerException("simulate: tmpTran==null").printStackTrace();
//                }
//                lpnMap.put(tmpLPNTran.getLabel(), tmpLPNTran);
//            }
//            while (true) {
//                int tmpIntLbl = -1;
//                prln("Select from " + currentEnabledTimedTransitions + "");//timed
//                {
//                    boolean valid = false;
//                    do {
//                        try {
//                            tmpIntLbl = new Integer(con.readLine().trim());
//                            valid = true;
//                        } catch (NumberFormatException numberFormatException) {
//                            if (!brk(con)) {
//                                break;
//                            }
//                        }
//                    } while (!valid);
//                }
//                selectedTransition = lpnMap.get(tmpIntLbl);
//                prln("transel=" + selectedTransition);
//                if (selectedTransition == null) {
//                    prln("   Wrong selection. Try again.");
//                } else {
//                    break;
//                }
//            }
//        }//END if simulate
//        //:::::::::::::::::::::::::::::::::::::::::::::END  SIMULATE code
//        TimedState nextState;
//        while (cetIter.hasNext()) {  //foreach tran
//            try {
//                if (INTERACTIVE_MODE) {
//                    getChar();
//                }
//                nextState = null;
//
//                firedTran = cetIter.next();
//                if (SIMULATE) {
//                    if (firedTran != selectedTransition) {
//                        continue;
//                    }
//                }
//                firedLabel = firedTran.getLabel();
//                //==================================== 6) fire =====================================
//                // fire transition to get next state
//                nextState = (TimedState) firedTran.fire(CURRENT_STATE);
//
//
//                //==================================== 7) project =====================================
//                LPNTranSet nextEnabledTransitions;//=new LPNTranSet(allTrans);
//                //    nextEnabledTransitions.removeAll(CURRENT_ENABLED_TRANSITIONS);
//                nextEnabledTransitions = getEnabledLpnTrans(
//                        CURRENT_STATE, firedTran, CURRENT_ENABLED_TRANSITIONS, false);
//
//
//                if (TIMED_ANALYSIS) {
////                   ; updateZone(nextState.getZone(), firedTran,
////                            nextEnabledTransitions, CURRENT_ENABLED_TRANSITIONS);
//
//                    //==================================== 9)  =====================================
//                    //differenceSet = currentEnabledTransitions - nextEnabledTransitions
//                    //remove un-needed timers from zone
//                    //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
//                    LPNTranSet diffEnabledTransitions = new LPNTranSet(CURRENT_ENABLED_TRANSITIONS);
//                    diffEnabledTransitions.removeAll(nextEnabledTransitions);
//                    diffEnabledTransitions.remove(firedTran);
//                    for (LPNTran t : diffEnabledTransitions) {
//                        if (ENABLE_PRINT) {
//                            prDbg(9, "projecting timer: " + t.getLabel() + " disabled by " + firedLabel);
//                        }
//
//                        //nextState.getZone().project(t.getLabel());
//                    }
//                    diffEnabledTransitions = null;
//                }
//                //=================================== 2...4) update zone ==================================
//                if (ENABLE_PRINT) {
//                    prDbg(9, "\n\ninserting...");
//                    prDbg(9, mergeColumns("CURRENT..." + CURRENT_STATE, "NEXT..." + nextState, "TRAN..." + firedTran, 30, 30, 30));
//                }
//                boolean stateExists = allStates.contains(nextState);
//
//                //==================================== 8) Verify =====================================
//
//                if (nextEnabledTransitions.isEmpty()) {
//                    if (ENABLE_PRINT) {
//                        prDbg(9, ("Verification failed: deadlock after firing ") + firedTran.getLabel());
//                    }
//                    RETURN_STATUS |= VERIFICATION_FAILED;
////                    nextState.setType(StateTypeEnum.DEADLOCK);
//                    failed = true;
//                    continue;
//                }
//                // Check if firing 'fired_transition' causes a disabling error.
//                if (false) {
//                    LPNTran disabledTran = LPNTran.disablingError(CURRENT_ENABLED_TRANSITIONS, nextEnabledTransitions, firedTran);
//                    if(disabledTran != null) {
//                        if (ENABLE_PRINT) {
//                            prDbg(9, ("Verification failed: disabling error after ") + firedTran.getLabel());
//                        }
//                        RETURN_STATUS |= VERIFICATION_FAILED;
////                        nextState.setType(StateTypeEnum.DISABLING);
//                        failed = true;
//                        continue;
//                    }
//                }
//
//                if (stateExists) {
//                    continue;
//                } //ELSE does not exist
//                allStates.add(nextState);
//                if (ENABLE_PRINT) {
//                    prDbg(7, "LVL " + stackHeight + "\tfind(nextState)==nextState (does not exist already) \n\t"
//                            + CURRENT_STATE.digest());
//                    //prDbg(7, "LVL " + stackHeight + "\tinserted ==> " + inserted);
//                    prDbg(4, "r_fingSG - LVL " + stackHeight);
//                    prDbg(7, " - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
//                    prDbg(7, " - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - ");
//                }
//                stackHeight++;
//                RETURN_STATUS |= r_findSG(nextState, nextEnabledTransitions);
//                stackHeight--;
//                if (ENABLE_PRINT) {
//                    prDbg(6, "END r_fingSG - LVL " + stackHeight);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.err.println(firedTran);
//            }
//        } //END foreach tran
//        return RETURN_STATUS | PATH_TAKEN;
//    }
//    int iterations = 0;
//
//    /**
//     *
//     * @param zone
//     * @param fired
//     * @param firedTran
//     * @param nextSet
//     * @param currentSet
//     */
//    void updateZone(Zone zone, LPNTran firedTran,
//            LPNTranSet nextSet, LPNTranSet currentSet) {
//        counts[3]++;
//        int fired = firedTran.getLabel();
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        //zone.validate();
//        //restrict
//        //if (Main.ZONE_OPTIMIZE_INF)
//        {
//            infTimer = !((zone).contains(fired));
//            // out.println("timer: "+fired+"\tinf?="+infTimer+"\tinZone?="+
//            // (((Zone) zone).contains(fired)));
//        }
//        if (!infTimer) {
//            int val = zone.get(fired, 0);
//            if (val == Integer.MIN_VALUE + 1) {
//                new Exception("zone.get==-1: tran=" + firedTran
//                        + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
//                getChar();
//            }
//            if (val > -firedTran.getDelayLB()) {
//                zone.put(fired, 0, -firedTran.getDelayLB());
//            }
//            zone.restrict(fired, firedTran.getDelayLB());
//            if (ENABLE_PRINT) {
//                prDbg(1, zone);
//            }
//
//            //tighten loose bounds
//            zone.recanonicalize();
//            if (ENABLE_PRINT) {
//                prDbg(1, zone);
//            }
//            //project out fired rule
//            zone.project(fired);
//        }
//
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        //extend DMB with new timers
//        //newEnSet = next - current
//        LPNTranSet newEnSet = new LPNTranSet(nextSet);
//        newEnSet.removeAll(currentSet);
//        //setB = next - new
//        LPNTranSet nextMinusNew = new LPNTranSet(nextSet);
//        nextMinusNew.removeAll(newEnSet);
//        //allocate the space to be modified later
//        for (LPNTran t2 : newEnSet) {
//            if (!zone.containsKey(t2.getLabel())) {
//                zone.allocate(t2.getLabel());
//            }
//        }
//        for (LPNTran i : newEnSet) {
//            lblI = i.getLabel();
//            infTimer = !((zone).contains(lblI));
//            //out.println(lblK+"\t(newEnSet)"+(infTimer?"tran is inf":"not inf "));
//            if (infTimer) {
//                // continue;
//            }
//
//            //Mi0 = M0i = 0;
//            zone.put(lblI, 0, 0);
//            zone.put(0, lblI, 0);
//            for (LPNTran k : newEnSet) {
//                lblK = k.getLabel();
//                //if (Main.ZONE_OPTIMIZE_INF)
//                //{
//                //infTimer = !((zone).contains(lblK));
//                ////out.println(lblK+"\t(newEnSet)"+(infTimer?"tran is inf":"not inf "));
//                //if (infTimer) {
//                ////continue;
//                //}
//                //}
//                //Mik = Mki = 0;
//                zone.put(lblI, lblK, 0);
//                zone.put(lblK, lblI, 0);
//            }
//            for (LPNTran k : nextMinusNew) {
//                lblK = k.getLabel();
//                //if (Main.ZONE_OPTIMIZE_INF)
//                {
//                    infTimer = !((zone).contains(lblK));
//                    if (infTimer) {
//                        continue;
//                    }
//                }
//                //M(ik)=M(0k)
//                zone.put(lblI, lblK, zone.get(0, lblK));
//                zone.put(lblK, lblI, zone.get(lblK, 0));
//            }
//        }
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        //advance time
//        for (LPNTran t : nextSet) {
//            boolean b = zone.put(0, t.getLabel(), t.getDelayUB());
//            if (!b) {
//                new Exception("zone.put==false: lpnt=" + t + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
//                getChar();
//            }
//        }
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        //tighten loose bounds
//        zone.recanonicalize();
//        //normalize?
//        //z.normalize(tran.getDelayUB(),tran.getDelayLB(), fired);
//        if (ENABLE_PRINT) {
//            prDbg(1, zone);
//        }
//        if (!zone.isValid()) {
//            new Exception("zone.isValid==false: lpnt=" + firedTran + "\n\tzone=" + zone).printStackTrace(platu.Main.out);
//            getChar();
//        }
//        //z.validate();
//        (zone).remove((fired));
//    }
//
//    static public void printUsageStats() {
//        System.out.printf("%-20s %11s\n", "StateGraph", counts[0]);
//        System.out.printf("\t%-20s %11s\n", "find(state)", counts[1]);
//        System.out.printf("\t%-20s %11s\n", "r_findSG", counts[2]);
//        System.out.printf("\t%-20s %11s\n", "updateZone", counts[3]);
//        // System.out.printf("\t%-20s %11s\n",   "",  counts[4]);
//        // System.out.printf("\t%-20s %11s\n",   "",  counts[5]);
//        // System.out.printf("\t%-20s %11s\n",   "",  counts[6]);
//        // System.out.printf("\t%-20s %11s\n",   "",  counts[7]);
//        // System.out.printf("\t%-20s %11s\n",   "",  counts[8]);
//        // System.out.printf("\t%-20s %11s\n",   "",  counts[9]);
//    }
//
////    final static public void compositionalFindSG(Project prj) {
////        try {
////            for (DesignUnit du_in : prj.getDesignUnitSet()) {
////                Thread.sleep(10);
////                LPN lpn_in = du_in.getLPNModel();
////                TimedStateGraph sg_in = du_in.getSGModel();
////                VarSet inputs1 = lpn_in.getInputs();
////                for (DesignUnit du_out : prj.getDesignUnitSet()) {
////                    Thread.sleep(10);
////                    LPN lpn_out = du_out.getLPNModel();
////                    TimedStateGraph sg_out = du_out.getSGModel();
////                    VarSet outputs2 = lpn_out.getOutputs();
////                    if (sg_in == sg_out) {
////                        continue;
////                    }
////                    //out.println("\n\n#####################################"
////                    //+ "###############################################"
////                    //+ "\nSG: " + sg_in.label + "\tvs\t" + sg_out.label);
////                    VarSet ioIntersection = new VarSet(inputs1);
////                    //ioIntersection.addAll(outputs2);
////                    VarSet dif = new VarSet(inputs1);
////                    dif.removeAll(outputs2);
////                    ioIntersection.removeAll(dif);
////                    if (compose(sg_in, ioIntersection, (TimedStateGraph) sg_out, lpn_in, lpn_out)) {
////                        continue;
////                    }
////                }
////
////
////            }//END for SGj in SGset
////            for (DesignUnit du_in : prj.getDesignUnitSet()) {
////                Thread.sleep(10);
////                LPN lpn_in = du_in.getLPNModel();
////                AbstractStateGraph sg_in = du_in.getSGModel();
////                VarSet inputs1 = lpn_in.getInputs();
////                for (DesignUnit du_out : prj.getDesignUnitSet()) {
////                    Thread.sleep(10);
////                    LPN lpn_out = du_out.getLPNModel();
////                    TimedStateGraph sg_out = du_out.getSGModel();
////                    VarSet outputs2 = lpn_out.getOutputs();
////                    if (sg_in == sg_out) {
////                        continue;
////                    }
////                    //out.println("\n\n#####################################"
////                    //+ "###############################################"
////                    //+ "\nSG: " + sg_in.label + "\tvs\t" + sg_out.label);
////                    VarSet ioIntersection = new VarSet(inputs1);
////                    //ioIntersection.addAll(outputs2);
////                    VarSet dif = new VarSet(inputs1);
////                    dif.removeAll(outputs2);
////                    ioIntersection.removeAll(dif);
////                    if (compose(sg_out, ioIntersection, (TimedStateGraph) sg_in, lpn_out, lpn_in)) {
////                        continue;
////                    }
////                }
////
////
////            }//END for SGj in SGset
////        } catch (InterruptedException interruptedException) {
////        }
////    }
//
//    private static boolean compose(TimedStateGraph sg_in, VarSet ioIntersection,
//            TimedStateGraph sg_out, LPN lpn_in, LPN lpn_out)
//            throws InterruptedException {
////        //dif=new VarSet(outputs2);
////        //dif.removeAll(inputs1);
////        //ioIntersection.removeAll(dif);
////        //out.println("INERTSECTION: " + ioIntersection);
////        //for Si in SGj
////        //out.println(lpn_in.description2());
////        //out.println(lpn_out.description2());
////        State[] stList_in = (new LinkedList<State>(sg_in.allStates)).toArray(new State[0]);
////        if (ioIntersection.size() <= 0) {
////            return true;
////        }
////        for (State st_in : stList_in) {
////            Thread.sleep(10);
////            // StateAbstraction sa1 = StateAbstraction.abstractState(si, lpn);
////            //for Sk in GlobalSG
////           State[] stList_out = (new LinkedList<State>(sg_out.allStates)).toArray(new State[0]);
////            for (State st_out : stList_out) {
////                Thread.sleep(10);
////                //update all states in Si
////                if (st_in != st_out) {
////                    //out.println("\n\nSTATE: " + st_in.ID + "\tvs\t" + st_out.ID);
////                    LPNTranSet transet_out = LPNTran.getEnabledLpnTrans(
////                            sg_out.allTrans, st_out, false);
////                    LPNTranSet transet_in = LPNTran.getEnabledLpnTrans(
////                            sg_in.allTrans, st_in, false);
////                    if (transet_out.size() < 1) {
////                        continue;
////                    }
////                    //out.println(" - - - - - -  BEFORE - - - - - - ");
////                    //out.println("[" + transet_out.getIDArrayString() + "]\t\t[" + transet_in.getIDArrayString() + "]");
////                    //out.println("TranSet size = " + transet_out.size() + ", " + transet_in.size());
////                    st_out.merge(st_in, ioIntersection, sg_in.allTrans, sg_out.allTrans, lpn_in, lpn_out);
////                    transet_out = LPNTran.getEnabledLpnTrans(
////                            sg_out.allTrans, st_out, false);
////                    transet_in = LPNTran.getEnabledLpnTrans(
////                            sg_in.allTrans, st_in, false);
////                    //out.println("\n - - - - - -  AFTER - - - - - - ");
////                    //out.println("[" + transet_out.getIDArrayString() + "]\t\t[" + transet_in.getIDArrayString() + "]");
////                    //out.println("TranSet size = " + transet_out.size() + ", " + transet_in.size());
////                    int states = sg_out.allStates.size();
////                    sg_out.r_findSG((TimedState) st_out,transet_out);
////                    //out.println("STATE size " + states + " --> " + sg_out.allStates.size());
////                }
////            }
////        }
//        return false;
//    }
//
//    @Override
//    public int size() {
//        return allStates.size();
//    }
//    private static final Logger LOG = Logger.getLogger(TimedStateGraph.class.getName());
//}
