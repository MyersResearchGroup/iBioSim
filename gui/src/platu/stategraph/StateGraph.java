package platu.stategraph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import lmoore.zone.Zone;
import platu.expression.VarNode;
import platu.logicAnalysis.Constraint;
import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.lpn.LpnTranList;
import platu.lpn.VarSet;
import platu.project.Project;
import platu.stategraph.state.State;
import platu.stategraph.state.StateTran;


@SuppressWarnings("StaticNonFinalUsedInInitialization")
// TODO: (Utah) remove "extends LPN" 
public class StateGraph extends LPN {

    private static int UPDATE_DURATION = 1000;//millisec

    /**
     * @return the UPDATE_DURATION
     */
    public static int getUPDATE_DURATION() {
        return UPDATE_DURATION;
    }

    /**
     * @param aUPDATE_DURATION the UPDATE_DURATION to set
     */
    public static void setUPDATE_DURATION(int aUPDATE_DURATION) {
        UPDATE_DURATION = aUPDATE_DURATION;
        //System.out.println("Printing updates every " + (double) UPDATE_DURATION / 1000f + " seconds");
    }
    
    public State sgInitState;
    // TODO: (Utah) remove deisgnPrj? It is only used in setStateGraph(). But setStateGraph() never gets called.
    private Project designPrj;
    // TODO: (Utah) add new object: LhpnFile lhpn;
    protected HashSet<String> inputs;
    protected HashSet<String> outputs;
    protected HashSet<String> internals;
    int iterations = 0;
    final static double hashSetSize = Math.pow(2f, 1f);
    long initialMemory = 0;

    static {
        //System.out.println("Hash set size is set to " + (int) hashSetSize);
        //System.out.println("Printing updates every " + (double) getUPDATE_DURATION() / 1000f + " seconds");
    }
    
    //final protected HashSet<State> allStates = new HashSet<State>((int) 1);
    //protected HashSet<StateTran> allTransitions = new HashSet<StateTran>((int) 1);
    protected State init = null;
    public HashMap<State, LinkedList<LPNTran>> lpnTransitionMap = new HashMap<State, LinkedList<LPNTran>>();
    protected List<State> stateSet = new LinkedList<State>();
    protected List<State> frontierStateSet = new LinkedList<State>();
    protected List<State> entryStateSet = new LinkedList<State>();

    // TODO: (Utah) Rewrite the two StateGraph() constructors below to adapt our LPN? 
    public StateGraph(Project prj, String label, VarSet inputs, VarSet outputs,
            VarSet internals, LpnTranList transitions, State initState) {
        super(prj, label, inputs, outputs, internals, transitions, initState);
        this.prj = prj;
        this.label = label;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internals = internals;
        this.transitions = transitions;
        this.initState = initState;
        if (prj == null || label == null || inputs == null ||//
                outputs == null || internals == null
                || transitions == null || initState == null) {
            new NullPointerException().printStackTrace();
        }
        if (ENABLE_PRINT) {
//            System.out.println("new LPN()1: \t" + description());
        }

        counts[0]++;
    }

    public StateGraph(Project prj, String label, VarSet inputs, VarSet outputs,
            VarSet internals, HashMap<String, VarNode> varNodeMap, LpnTranList transitions, 
            HashMap<String, Integer> initialVector, int[] initialMarkings, Zone initialZone) {
        super(prj, label, inputs, outputs, internals, varNodeMap, transitions, initialVector, initialMarkings, initialZone);
        this.prj = prj;
        this.label = label;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internals = internals;
        this.transitions = transitions;
//        this.initState = initState;
        if (prj == null || label == null || inputs == null ||//
                outputs == null || internals == null
                || transitions == null) {
            new NullPointerException().printStackTrace();
        }

        counts[0]++;
    }
    
    /**
     *
     * @param label
     * @param designPrj
     * @param inputs
     * @param outputs
     * @param internals
     */
    public void setStateGraph(String label, Project designPrj,
            HashSet<String> inputs, HashSet<String> outputs,
            HashSet<String> internals) {

        this.label = label;
        this.designPrj = designPrj;
        this.inputs = inputs;
        this.outputs = outputs;
        this.internals = internals;
    }

//    public void findSG() {
//        try {
//            System.gc();
//            Timer updateTimer = new Timer(getUPDATE_DURATION(), new ActionListener() {
//
//                long lastTime = System.currentTimeMillis();
//                final long startTime = lastTime;
//                int lastStates = 0;
//DecimalFormat df1=new DecimalFormat("00.000");
//DecimalFormat df2=new DecimalFormat("###,###,###,##0");
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    try {
//                        System.gc();
//                        int numStates = allStates.size();
//                        long now = System.currentTimeMillis();
//                        long totalMem, freeMem;
//                        totalMem = Runtime.getRuntime().totalMemory();
//                        freeMem = Runtime.getRuntime().freeMemory();
//                        long mem = (totalMem - freeMem - initialMemory) / 1024;
//                        int deltaStates = numStates - lastStates;
//                        long timeMS = now - startTime;
//                        double deltaTime = (getUPDATE_DURATION() / 1000f);
//                        long timeSec = (long) (timeMS / 1000f);
//                        long h = timeSec / 3600, m = (timeSec % 3600) / 60;
//                        double s = (timeMS / 1000f) - h * 3600 - m * 60;
//                        int statesPerSec = (int) (deltaStates / deltaTime);
//                        String timeStr = String.format("%d:%02d:%2s", h, m, df1.format(s));
//                        int bytesPerState =(int) (1024l * mem / numStates);
//                        if (out != System.out) {
//                            out.printf("Free:%-10s Used: %-10s Total: %-10s Stack: %-7s %s states/sec  %s B/state  %s\n",
//                                    Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb",
//                                    mem / 1024 + " mb",
//                                    Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb",
//                                    ptr + "",
//                                    String.format("%5s   %5s  ", label,
//                                    "|S| =" + numStates) + "  " + (statesPerSec > 0.9 * Integer.MAX_VALUE ? "--" : statesPerSec),
//                                    1024l * mem / numStates,
//                                    timeStr);
//                        }
//                        System.out.printf("Free:%-10s Used: %-10s Total: %-10s Stack: %-7s %s states/sec  %s B/state  %s\n",
//                                df2.format(freeMem / 1024 / 1024) + " mb",
//                               df2.format( mem / 1024)+ " mb",
//                                df2.format(totalMem / 1024 / 1024) + " mb ("+
//                                df2.format(totalMem/bytesPerState/1000)+" K states max)",
//                               df2.format( ptr) + "",
//                                String.format("%5s   %5s  ", label,
//                                "|S| =" + df2.format(numStates)) + "  " + df2.format(statesPerSec),
//                              df2.format( bytesPerState),
//                                timeStr);
//                        lastTime = System.currentTimeMillis();
//                        lastStates = numStates;
//                    } catch (Exception ex) {
//                    }
//                }
//            });
//
//            Timer gcTimer = new Timer(getUPDATE_DURATION(), new ActionListener() {
//
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    System.gc();
//                }
//            });
//
//            findDependency();
//            //gcTimer.start();
//            updateTimer.start();
//            label = getLabel();
//
//            System.out.println("running findsg_untimed on " + getLabel());
//
//            //================================== first recursion init
//            State currentState = getInitStateUntimed();
////            System.out.println("INIT STATE: " + currentState);
//
//            if (ENABLE_PRINT) {
//                prDbg(2, "INIT STATE: " + currentState);
//            }
//            allTrans = new LPNTranSet(getTransitions());
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
//            //LPNTran.getEnabledLpnTrans(allTrans, currentState, false);
//            int enabledcount = currentEnabledTransitions.size();
//            // terminate algorithm if there are no transitions initially enabled to fire
//            if (enabledcount == 0 && ENABLE_PRINT) {
//                System.out.println("StateGraph:\t" + label + ":\tVerification failed: deadlocked at initial state.");
//                return;
//            }
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
//
//            if (PRINT_LEVEL < 6) {
//                out.println(currentState);
//            }
//            sgInitState = currentState;
//            allStates.add((currentState));
//            long time = System.currentTimeMillis();
//            initialMemory = getRuntime().totalMemory() - getRuntime().freeMemory();
////            r_findSG_untimed_recursive( currentState.clone(), currentEnabledTransitions);
//            r_findSG_untimed_stack_array(currentState.clone(), currentEnabledTransitions);
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
//            //if (OPEN_STATE_EXPLORER) {
//            //    StateExplorer se = new StateExplorer(stateIncSet, stateOutSet);
//            //    se.setState(sgInitState);
//            //    // se.setState(stateIncSet.keySet().iterator().next());
//            //    se.pack();
//            //}
////            if (DRAW_JAVA_GRAPH) {
////                Graph graph = new Graph(new GraphTest(stateIncSet));
////            }
//        } catch (Exception ex) {
//            Logger.getLogger(TimedStateGraph.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    
//    int r_findSG(final State currentState, LPNTranSet currentEnabledTransitions) {
//        return r_findSG_untimed_stack_array(currentState, currentEnabledTransitions);
//    }
//    int ptr = 1;
//
//    public int r_findSG_untimed_stack_array(final State INITIAL_STATE, final LPNTranSet INITIAL_ENABLED_TRANSITIONS) {
//        int size = 260000;
//        State nextState, CURRENT_STATE;
//        LPNTranSet CURRENT_ENABLED_TRANSITIONS = null;
//        State[] stStack = new State[size];
//        LPNTranSet[] tranStack = new LPNTranSet[size];
//        initialMemory = getRuntime().totalMemory() - getRuntime().freeMemory();
//        System.out.println("INITIAL MEMORY: " + initialMemory / 1024 + " k");
//        stStack[0] = INITIAL_STATE;
//        tranStack[0] = INITIAL_ENABLED_TRANSITIONS;
//        while (true) {
//            ptr--;
//            CURRENT_ENABLED_TRANSITIONS = tranStack[ptr];
//            CURRENT_STATE = stStack[ptr];
//            for (LPNTran firedTran : CURRENT_ENABLED_TRANSITIONS) {  //foreach tran
//                nextState = firedTran.fire(CURRENT_STATE);
//
//                // If nextState has been traversed before, skip to the next enabled transition.
//
////System.out.println(new ByteArray(nextState.digest4()));
//                if (allStates.add(nextState) == false) {
////                    nextState = null;
//                    continue;
//                }
//                stStack[ptr] = nextState;
//                //NEXT_ENABLED_TRANSITIONS
//                tranStack[ptr] = getEnabledLpnTrans(
//                        nextState, firedTran, CURRENT_ENABLED_TRANSITIONS, false);
////                 if (tranStack[ptr].isEmpty() == true) {
////                    prDbg(10, "StateGraph:\t" + label + ":\tVerification failed: deadlocked at initial state.");
////                    return ERROR;
////                }
////
////                if (LPNTran.disablingError(CURRENT_ENABLED_TRANSITIONS, tranStack[ptr], firedTran)) {
////                    prDbg(10, "StateGraph:\t" + label + ":\tVerification failed: deadlocked at initial state.");
////                    return ERROR;
////                }
//                ptr++;
//            } //END foreach tran
//            if (ptr < 1) {
//                break;
//            }
//        }
//        return 0;
//    }

//    public int r_findSG_untimed_stack_linked(final State currentState, Collection<LPNTran> currentEnabledTransitions) {
//
//        Stack<State> stStack = new Stack<State>();
//        Stack<LPNTranSet> tranStack = new Stack<LPNTranSet>();
//        int ptr = 1;
//        //vars to be reused
//        stStack.push(currentState);
//        tranStack.push((LPNTranSet) currentEnabledTransitions);
//        while (true) {
//            ptr--;
//            LPNTranSet lpnt = tranStack.pop();
//            State st = stStack.pop();
//            for (LPNTran firedTran : lpnt) {  //foreach tran
//                State nextState = firedTran.fire(st);
//
//                if (allStates.add(nextState) == false) {
////                    nextState = null;
//                    continue;
//                } else {
//                    LPNTranSet nextEnabledTransitions = getEnabledLpnTrans(nextState, firedTran, lpnt, false);
//                    stStack.push(nextState);
//                    tranStack.push(nextEnabledTransitions);
//
//                    ptr++;
//                }
//            } //END foreach tran
//
//            if (ptr == 0) {
//                break;
//            }
//        }
//        return 0;
//    }

//    /**
//     *
//     * @return
//     */
//    @Override
//    public String toString() {
//    	LPN lpn = (LPN) this;
//        return String.format("%5s     %5s", lpn.getLabel() //+"  sgTranCount=" + sgTranCount
//                ,
//                //                "stateIncSet size=" + stateIncSet.size(),
//                //                "stateOutSet size=" + stateOutSet.size(),
//                "|States| =" + allStates.size());
//    }
//    
////    public void setLabel(String label){
//////    	this.label = label;
////    }
    // TODO: (Utah) Replace LPN with our LPN object.
    public void printStates(){
    	LPN lpn = (LPN) this;
    	System.out.println(String.format("%-8s    %5s", lpn.getLabel(), "|States| = " + reachSize()));
    }
//    // Find if '(current_state, lpn_tran, next_state)' exists in this SG. If so, return 'next_state';
//    // Otherwise, fire 'lpn_tran', add '(current_state, lpn_tran, next_state)' into this SG, and return 'next_state'.
//
//    public final State next(State current_state, LPNTran lpn_tran) {
//
//
//        return null;
//    }//!

//    /**
//     *
//     * @param state
//     * @return
//     */
//    public final State find(State state) {
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
    //boolean isNew(State ) ;

//    /**
//     *
//     * @param level
//     * @param o
//     */
//    public static void prDbg(int level, Object o) {
//        if (level >= PRINT_LEVEL) {
//            out.println(o);
//        }
//    }

//    /**
//     *
//     * @param o
//     */
//    public static void pErr(Object o) {
//        System.err.println(o);
//    }

//    public String disjunction() {
//        String expression = "false";
//        for (State s : allStates) {
//            expression += "||(" + s.conjunction(allTrans) + ")";
//        }
//        return expression;
//    }

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
//            Logger.getLogger(StateGraph.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    class DotThread extends Thread {

        @Override
        public void run() {
//            try {
//                String dot1 = OutputDOT.toString(sgInitState, stateIncSet,
//                        allStates);
//                String timed = TIMED_ANALYSIS ? "_TIMED" : "_UNTIMED";
//                String stateInfo = "_" + allStates.size() + "-states_" + sgTranCount + "-trans";
//                File f1 = new File(LPN_PATH + "\\" + Main.RESULT_FOLDER + "\\"
//                        + label + timed + stateInfo + "_" + Integer.toHexString(graphIdx++)
//                        + ".sg");
//                OutputDOT.string2File(f1, dot1);
//                OutputDOT.DOT2File("png", f1);
//                this.interrupt();
//            } catch (Exception e) {
//                e.printStackTrace(platu.Main.out);
//            }
        }
    }

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
    
//    public int size() {
//        return allStates.size();
//    }

    /**
     * Finds reachable states from the given state.
     * Also generates new constraints from the state transitions.
     * @param baseState State to start from
     * @return Number of new transitions.
     */
    public int constrFindSG(final State baseState){
    	boolean newStateFlag = false;
    	int ptr = 1;
        int newTransitions = 0;
        Stack<State> stStack = new Stack<State>();
        Stack<LpnTranList> tranStack = new Stack<LpnTranList>();
        LpnTranList currentEnabledTransitions = getEnabled(baseState);
        
        stStack.push(baseState);
        tranStack.push((LpnTranList) currentEnabledTransitions);
        
        while (true){
            ptr--;
            State currentState = stStack.pop();
            currentEnabledTransitions = tranStack.pop();

            this.lpnTransitionMap.get(currentState).addAll(currentEnabledTransitions);
            
            for (LPNTran firedTran : currentEnabledTransitions) {
                State st = firedTran.constrFire(currentState);
                State nextState = addReachable(st);

                newStateFlag = false;
            	if(nextState == null){
            		nextState = st;
            		newStateFlag = true;
            	}

//            	StateTran stTran = new StateTran(currentState, firedTran, state);
            	if(firedTran.addStateTran(currentState, nextState) == null){
            		newTransitions++;
            		// TODO: check that a variable was changed before creating a constraint
	            	if(!firedTran.local()){
	            		for(LPN lpn : firedTran.getLpnList()){
	                  		Constraint c = new Constraint(currentState, nextState, firedTran, lpn);
	                  		lpn.addConstraint(c);
	        			}
	            	}
        		}

            	if(!newStateFlag) continue;
            	
            	LpnTranList nextEnabledTransitions = getEnabled(nextState);
                if (nextEnabledTransitions.isEmpty()) continue;
                
//                currentEnabledTransitions = getEnabled(nexState);
                LPNTran disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
                if(disabledTran != null) {
                    System.out.println("Verification failed: " +disabledTran.getFullLabel() + " is disabled by " + 
                    			firedTran.getFullLabel());
                   
                    currentState.setFailure();
                    return -1;
                }
                
                stStack.push(nextState);
                tranStack.push(nextEnabledTransitions);
                ptr++;
            }

            if (ptr == 0) {
                break;
            }
        }

        return newTransitions;
    }
    
    /**
     * Finds reachable states from the given state.
     * Also generates new constraints from the state transitions.
     * @param baseState State to start from
     * @return Number of new transitions.
     */
    public int constrStickyFindSG(final State baseState, List<LPNTran> previousEnabledSet){
    	boolean newTranFlag = false;
    	int ptr = 1;
        int newTransitions = 0;
        Stack<State> stStack = new Stack<State>();
        Stack<LpnTranList> tranStack = new Stack<LpnTranList>();
        LpnTranList currentEnabledTransitions = getEnabled(baseState);

//        System.out.println(previousEnabledSet.size());
    	for(LPNTran stickyTran : previousEnabledSet){
    		if(!stickyTran.sticky()) continue;
    		
    		int[] preset = stickyTran.getPreSet();
    		int[] marking = baseState.getMarking();
    		
        	// check if sticky tran is marking enabled
        	boolean enabled = true;
    		for(int pre : preset){
    			boolean contained = false;
    			for(int m : marking){
    				if(pre == m){
    					contained = true;
    					break;
    				}
    			}
    			
    			if(!contained){
    				enabled = false;
    				break;
    			}
    		}
    		
    		if(enabled && !currentEnabledTransitions.contains(stickyTran)){
    			System.out.println("sticky1");
    			currentEnabledTransitions.add(stickyTran);
    		}
    	}
        
        stStack.push(baseState);
        tranStack.push((LpnTranList) currentEnabledTransitions);
        while (true){
            ptr--;
            State currentState = stStack.pop();
            currentEnabledTransitions = tranStack.pop();
            
            this.lpnTransitionMap.get(currentState).addAll(currentEnabledTransitions);

//            System.out.println("curEnabled.size = " + currentEnabledTransitions.size());
            
            for (LPNTran firedTran : currentEnabledTransitions) {
                State st = firedTran.constrFire(currentState);
                State nextState = addReachable(st);

                newTranFlag = false;

            	if(nextState == null){
            		nextState = st;
            	}

//            	StateTran stTran = new StateTran(currentState, firedTran, state);
            	if(firedTran.addStateTran(currentState, nextState) == null){
            		newTranFlag = true;
            		newTransitions++;
            		// TODO: check that a variable was changed before creating a constraint
	            	if(!firedTran.local()){
	            		for(LPN lpn : firedTran.getLpnList()){
	                  		Constraint c = new Constraint(currentState, nextState, firedTran, lpn);
	                  		lpn.addConstraint(c);
	        			}
	            	}
        		}
            	
            	LpnTranList nextEnabledTransitions = getEnabled(nextState);

//                System.out.println("\n firedTran : " + firedTran.getFullLabel());
            	int stickyCount = 0;
            	for(LPNTran stickyTran : currentEnabledTransitions){
            		if(stickyTran == firedTran || !stickyTran.sticky()) continue;
            		
//            		System.out.println(stickyTran.getFullLabel());
            		int[] nextStateMarking = nextState.getMarking();
            		int[] preset = stickyTran.getPreSet();
            		
            		boolean enabled = true;
            		for(int pre : preset){
            			boolean contained = false;
            			for(int m : nextStateMarking){
            				if(pre == m){
            					contained = true;
            					break;
            				}
            			}
            			
            			if(!contained){
            				enabled = false;
            				break;
            			}
            		}
            		
            		if(enabled){
            			if(!nextEnabledTransitions.contains(stickyTran)){
            				System.out.println("sticky");
            				nextEnabledTransitions.add(stickyTran);
            				stickyCount++;
            			}
            		}
            	}
                
            	if(!newTranFlag && stickyCount == 0)
            		continue;
            	
                if (nextEnabledTransitions.isEmpty()) continue;
                
//                currentEnabledTransitions = getEnabled(nexState);
//                LPNTran disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
//                if(disabledTran != null) {
//                    prDbg(10, "Verification failed: " +disabledTran.getFullLabel() + " is disabled by " + 
//                    			firedTran.getFullLabel());
//                   
//                    currentState.setFailure();
//                    return -1;
//                }
                
                stStack.push(nextState);
                tranStack.push(nextEnabledTransitions);
                ptr++;
            }

            if (ptr == 0) {
                break;
            }
        }

        return newTransitions;
    }
    
    /**
     * Finds reachable states from the given state.
     * Also generates new constraints from the state transitions.
     * Synchronized version of constrFindSG().  Method is not synchronized, but uses synchronized methods
     * @param baseState State to start from
     * @return Number of new transitions.
     */
    public int synchronizedConstrFindSG(final State baseState){
    	boolean newStateFlag = false;
    	int ptr = 1;
        int newTransitions = 0;
        Stack<State> stStack = new Stack<State>();
        Stack<LpnTranList> tranStack = new Stack<LpnTranList>();
        LpnTranList currentEnabledTransitions = getEnabled(baseState);

        stStack.push(baseState);
        tranStack.push((LpnTranList) currentEnabledTransitions);
        while (true){
            ptr--;
            State currentState = stStack.pop();
            currentEnabledTransitions = tranStack.pop();
            this.lpnTransitionMap.get(currentState).addAll(currentEnabledTransitions);

            for (LPNTran firedTran : currentEnabledTransitions) {
                State st = firedTran.constrFire(currentState);
                State nextState = addReachable(st);

                newStateFlag = false;
            	if(nextState == null){
            		newStateFlag = true;
            		nextState = st;
            	}
        		
//            	StateTran stTran = new StateTran(currentState, firedTran, state);
            	if(firedTran.synchronizedAddStateTran(currentState, nextState) == null){
	        		newTransitions++;
	        		
	            	if(!firedTran.local()){
	            		// TODO: check that a variable was changed before creating a constraint
	            		for(LPN lpn : firedTran.getLpnList()){
	                  		Constraint c = new Constraint(currentState, nextState, firedTran, lpn);
	                  		lpn.synchronizedAddConstraint(c);
	        			}
	  
	            	}
            	}

            	if(!newStateFlag)
            		continue;
            	
            	LpnTranList nextEnabledTransitions = getEnabled(nextState);
                if (nextEnabledTransitions == null || nextEnabledTransitions.isEmpty()) {
                    continue;
                }
                
//                currentEnabledTransitions = getEnabled(nexState);
//                LPNTran disabledTran = firedTran.disablingError(currentEnabledTransitions, nextEnabledTransitions);
//                if(disabledTran != null) {
//                    prDbg(10, "Verification failed: " +disabledTran.getFullLabel() + " is disabled by " + 
//                    			firedTran.getFullLabel());
//                   
//                    currentState.setFailure();
//                    return -1;
//                }
                
                stStack.push(nextState);
                tranStack.push(nextEnabledTransitions);
                ptr++;
            }

            if (ptr == 0) {
                break;
            }
        }

        return newTransitions;
    }
    
    public List<State> getFrontierStateSet(){
    	return this.frontierStateSet;
    }
    
    public List<State> getStateSet(){
    	return this.stateSet;
    }
    
    public void addFrontierState(State st){
    	this.entryStateSet.add(st);
//    	this.stateSet.add(st);
    }
    
//    public int numTransitions(){
//    	return this.allTransitions.size();
//    }
//    
//    public Set<StateTran> getStateTransitions(){
//    	return this.allTransitions;
//    }
//    
//    public boolean addTransition(StateTran tran){
//    	return this.allTransitions.add(tran);
//    }
    
    public void genFrontier(){
    	this.stateSet.addAll(this.frontierStateSet);
    	this.frontierStateSet.clear();
    	this.frontierStateSet.addAll(this.entryStateSet);
    	this.entryStateSet.clear();
    }
    
    public void setInitialState(State init){
    	this.init = init;
    }
    
    public State getInitialState(){
    	return this.init;
    }
    
    public void draw(){
    	String dotFile = this.label + ".dot";
    	PrintStream graph = null;
    	
		try {
			graph = new PrintStream(new FileOutputStream(dotFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
    	
    	graph.println("digraph SG{");
    	//graph.println("  fixedsize=true");
    	
    	int size = this.outputs.size() + this.inputs.size() + this.internals.size();
    	String[] variables = new String[size];
    	
    	int i;
    	for(i = 0; i < size; i++){
    		variables[i] = varIndexMap.getKey(i);
    	}
    	
    	//for(State state : this.reachableSet.keySet()){
    	for(int stateIdx = 0; stateIdx < this.reachSize(); stateIdx++) {
    		State state = this.getState(stateIdx);
    		String dotLabel = state.getIndex() + ": ";
    		int[] vector = state.getVector();

    		for(i = 0; i < size; i++){
    			dotLabel += variables[i];

        		if(vector[i] == 0) dotLabel += "'";
        		
        		if(i < size-1) dotLabel += " ";
    		}
    		
    		int[] mark = state.getMarking();
    		
    		dotLabel += "\\n";
    		for(i = 0; i < mark.length; i++){
    			if(i == 0) dotLabel += "[";
    			
    			dotLabel += mark[i];
    			
    			if(i < mark.length - 1)
    				dotLabel += ", ";
    			else
    				dotLabel += "]";
    		}

    		String attributes = "";
    		if(state.getIndex() == 1) attributes += " peripheries=2";
    		if(state.failure()) attributes += " style=filled fillcolor=\"red\"";
    		
    		graph.println("  " + state.getIndex() + "[shape=ellipse width=.3 height=.3 " +
					"label=\"" + dotLabel + "\"" + attributes + "]");
    		
    		
    		for(LPNTran lpnTran : this.lpnTransitionMap.get(state)){
    			State tailState = state;
    			State headState = lpnTran.getNextState(tailState);
    			
    			String edgeLabel = lpnTran.getLabel() + ": ";
        		int[] headVector = headState.getVector();
        		int[] tailVector = tailState.getVector();
        		
        		for(i = 0; i < size; i++){
            		if(headVector[i] != tailVector[i]){
            			if(headVector[i] == 0){
            				edgeLabel += variables[i];
            				edgeLabel += "-";
            			}
            			else{
            				edgeLabel += variables[i];
            				edgeLabel += "+";
            			}
            		}
        		}
        		
        		graph.println("  " + tailState.getIndex() + " -> " + headState.getIndex() + "[label=\"" + edgeLabel + "\"]");
    		}
    	}
    	
    	graph.println("}");
	    graph.close();
    }
}
