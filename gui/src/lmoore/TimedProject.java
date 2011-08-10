///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package lmoore;
//
//import java.text.DecimalFormat;
//import platu.stategraph.zone.*;
//import com.carrotsearch.hppc.LongArrayList;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import lmoore.zone.Cache;
//import lmoore.zone.Zone;
//import lmoore.zone.Cache.DCache;
//import lmoore.zone.Cache.IdentityCache;
//import lmoore.zone.impl.MatrixZoneVer3Impl;
//import lmoore.zone.impl.MatrixZoneVer3Impl.FinalizedCanonicalZone;
//import lmoore.zone.matrix.MatrixVer3;
//import platu.Common;
//import platu.Main;
//import platu.ui.PrintTable;
//import platu.TimingAnalysis.Zone1;
//import platu.lpn.DualHashMap;
//import platu.lpn.LPN;
//import platu.lpn.LPNTran;
//import platu.lpn.LPNTranSet;
//import platu.lpn.VarSet;
//import platu.project.IDGenerator;
//import platu.project.Project;
//import platu.project.prjState;
//import platu.project.prjStateTimed;
//import platu.project.Project.Mode;
//import platu.stategraph.DOTGraph;
//import platu.stategraph.StateGraph;
//import platu.stategraph.UpdateTimer;
//import platu.stategraph.state.State;
//import static lmoore.TimedProject.VERSION.*;
//import static lmoore.zone.Zone.*;
//
///**
// *
// * @author ldmtwo
// */
//public class TimedProject extends Common {
//
//    static {
////        new StackWindow().start();
//    }
//
//    enum VERSION {
//
//        RECURSIVE, ITERATIVE, ITERATIVE_MINIMAL,
//        ITERATIVE_DFS_CACHED, ITERATIVE_DFS_CACHED_MINIMAL,
//        ITERATIVE_BFS_CACHED, ITERATIVE_BFS_CACHED_MINIMAL,
//        ITERATIVE_BFS_CACHED_UT, MDD_VER1, MDD_VER2
//    }
//    static VERSION version = ITERATIVE;
//    static int ARRAY_INCREMENT = 1000;
//    static boolean PRINT_ENABLED = false;
//    static int FINDSG_VERSION = 1;
//    static private boolean deadlock = false, disabled = false;
//    private static LinkedList[] curEnabledList = new LinkedList[0];
//    private static LinkedList[] nextEnabledList = new LinkedList[0];
//    static PrintTable printer = new PrintTable(120, 40);
//    static Cache prjStateCache = Cache.getNewCache(100);
//    static long START_TIME = 0;
//    static DecimalFormat df1 = new DecimalFormat("00.000");
//    static DOTGraph graph = new DOTGraph();
//
//    /**
//     * Call this to create garbage for the GC to collect. It will be more likely to
//     * free other garbage in the heap. THis will give more accurate initial memory
//     * readings.
//     * @return
//     */
//    static int garbage() {
//        int[] nums = {1, 2, 43, 4, 4, 5, 23, 5, 5, 1, 2, 3, 4, 21, 34, 23, 4};
//        int tot = 0;
//        String ss = "";
//        for (int i : nums) {
//            for (int j : nums) {
//                for (int k : nums) {
//                    String s = i + "" + j + "" + k;
//                    ss += s;
//                    s += s + s + i;
//                    tot += s.length() + ss.length();
//                }
//            }
//        }
//        return tot;
//    }
//
//    /**
//     * Find the SG for the entire project where each project state is a tuple of
//     * local states
//     * @param prj 
//     */
//    static public void findsg(final Project prj) {
//        StateGraph.setUPDATE_DURATION(5000);
//
//// <editor-fold defaultstate="collapsed" desc="pre-findsg">
//        UpdateTimer updTimer = new UpdateTimer(StateGraph.getUPDATE_DURATION()) {
//
//            @Override
//            public int getNumStates() {
//                return (int) (prj.prjStateSet.size() + prjStateCache.size() + prj.mddMgr.numberOfStates(prj.reach));
//            }
//
//            @Override
//            public int getStackHeight() {
//                return prj.max_stack_depth;
//            }
//
//            @Override
//            public String getLabel() {
//                return prj.label;
//            }
//        };
//        long start = System.currentTimeMillis();
//        int lpnCnt = prj.designUnitSet.size();
//       // garbage();
//        //System.gc();
//        // Initialize the memory for storing local states.
//        // <editor-fold defaultstate="collapsed" desc="Initialize the memory for storing local states">
//        LPN[] lpnList = new LPN[lpnCnt];
//        prj.mddMgr = new Mdd(lpnCnt);
//        prj.reach = prj.mddMgr.newNode();
//
//        int i = 0;
//        for (LPN du : prj.designUnitSet) {
//        	du.setIndex(i);
//            lpnList[i] = du;
//            i++;
//        }
//
//        for (LPN p : lpnList) {
//            p.findDependency();
//            for (LPN p1 : lpnList) {
//                if (p == p1) {
//                    continue;
//                }
//                p.findDependency(p1.getOutputTrans());
//            }
//        }// </editor-fold>
//
//        // Computing the input sources for each LPN.
//        // <editor-fold defaultstate="collapsed" desc="Computing the input sources for each LPN">
//        for (int dest = 0; dest < lpnCnt; dest++) {
//            HashSet<String> dest_inputs = lpnList[dest].getInputs();
//            for (int src = 0; src < lpnCnt; src++) {
//                if (dest == src) {
//                    continue;
//                }
//                boolean isDriven = false;
//                HashSet<String> src_outputs = lpnList[src].getOutputs();
//                for (String srcOutput : src_outputs) {
//                    if (dest_inputs.contains(srcOutput) == true) {
//                        isDriven = true;
//                        break;
//                    }
//                }
//                if (isDriven == true) {
//                    HashSet<LPN> srcLpnSet = prj.lpnInputSrc.get(lpnList[dest]);
//                    if (srcLpnSet == null) {
//                        srcLpnSet = new HashSet<LPN>(1);
//                        prj.lpnInputSrc.put(lpnList[dest], srcLpnSet);
//                    }
//                    srcLpnSet.add(lpnList[src]);
//                }
//            }
//        }// </editor-fold>
//
//        // Initialize the project state
//        HashMap<String, Integer> varValMap = new HashMap<String, Integer>();
//        State[] curStateArray = new State[lpnCnt];
//        ArrayList<LinkedList<LPNTran>> enabledList = new ArrayList<LinkedList<LPNTran>>(1);
//        for (int index = 0; index < lpnCnt; index++) {
//            LPN curLpn = lpnList[index];
//            curStateArray[index] = curLpn.getInitStateUntimed();
//            int[] curStateVector = curStateArray[index].getVector();
//            HashSet<String> outVars = curLpn.getOutputs();
//            DualHashMap<String, Integer> VarIndexMap = curLpn.getVarIndexMap();
//            for (String var : outVars) {
//                varValMap.put(var, curStateVector[VarIndexMap.getValue(var)]);
//            }
//        }
//
//        // Adjust the value of the input variables in LPN in the initial state.
//        for (int index = 0; index < lpnCnt; index++) {
//            LPN curLpn = lpnList[index];
//            curStateArray[index].update(varValMap, curLpn.getVarIndexMap());
//            enabledList.add(index, curLpn.getEnabled(curStateArray[index]));
//        }
//
//
//        if (Project.deadLock(lpnList, curStateArray) == true) {
//            System.err.println("Verification failed: deadlock in the initial state.");
//            return;
//        }
//
//        // Add the initial project state into the prjStateSet, and invoke
//        // findsg_recursive().
//        HashSet<prjState> stateTrace = new HashSet<prjState>(1);
//        prjState curPrjState = new prjState(curStateArray);
////		prjStateSet.add(curPrjState);//!!This causes problems [lmoore]
//        stateTrace.add(curPrjState);
//
////		System.out.println("------------------");
////		curPrjState.print(lpnList);
//        //System.gc();
//        //updTimer.start();
//        // Start reachability analysis.
//        LinkedList<LPNTran> traceCex = new LinkedList<LPNTran>();
//        //findsg_recursive(lpnList, curStateArray, enabledList, stateTrace);
//        System.out.println("TIMED_ANALYSIS=" + StateGraph.TIMED_ANALYSIS);
//        START_TIME = System.currentTimeMillis();// </editor-fold>
//        try {
//            switch (version) {
//                case MDD_VER1:
//                    TimedProject.findsg_mdd_timed_ver_1(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case MDD_VER2:
//                    TimedProject.findsg_mdd_timed_ver_2(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case ITERATIVE_BFS_CACHED_UT:
//                    TimedProject.findsg_cached_iterative_untimed(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case ITERATIVE_BFS_CACHED:
//                    TimedProject.findsg_bfs_cached_iterative_timed(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case ITERATIVE_BFS_CACHED_MINIMAL:
//                    TimedProject.findsg_bfs_cached_iterative_timed(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case ITERATIVE_DFS_CACHED:
//                    TimedProject.findsg_dfs_cached_iterative_timed(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case ITERATIVE_DFS_CACHED_MINIMAL:
//                    TimedProject.findsg_dfs_cached_minimal_iterative_timed(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case ITERATIVE:
//                    TimedProject.search_dfs(lpnList, curStateArray);
//                    break;
//                case ITERATIVE_MINIMAL:
//                    TimedProject.findsg_minimal_iterative_timed(lpnList, curStateArray, enabledList, prj);
//                    break;
//                case RECURSIVE:
//                    TimedProject.findsg_dfs_cached_minimal_iterative_timed(lpnList, curStateArray, enabledList, prj);
//                    break;
//                default:
//                    TimedProject.search_dfs(lpnList, curStateArray);
//                    break;
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
//        }
////==================================
//        // <editor-fold defaultstate="collapsed" desc="post findsg">
//        //updTimer.print();
//        //updTimer.stop();
//        //findsg_iterative_decomposed(lpnList, curStateArray, enabledList);
//        //traceCex = findsg_iterative_mdd(lpnList, curStateArray, enabledList);
//
//        while (traceCex != null && traceCex.size() > 0) {
//            LPNTran lpnTran = traceCex.removeFirst();
//            System.out.println(lpnTran.getLpn().getLabel() + " : " + lpnTran.getLabel());
//        }
//
//        System.out.println("Modules' local states: ");
//        for (i = 0; i < lpnCnt; i++) {
//            System.out.println("module " + lpnList[i].getLabel() + ": "
//                    + lpnList[i].reachSize());
//        }
//
//        long elapsedTimeMillis = System.currentTimeMillis() - start;
//        float elapsedTimeSec = elapsedTimeMillis / 1000F;
//
//        System.out.println("---> total runtime: " + elapsedTimeSec + " sec\n");// </editor-fold>
//    }
//
//    static void search_dfs(LPN[] lpnList, State[] curLocalStateArray) {
//    	System.out.println("---> Calling timedProject.findsg_dfs");
//    	
//    	int zoneType = 0;
//    	
//        int arraySize = lpnList.length;
//        int max_stack_depth = 0;
//        long peakTotalMem = 0;
//        long peakUsedMem = 0;
//        boolean failure = false;
//        
//        HashMap<Zone, Zone> zoneTbl = new HashMap<Zone, Zone>();
//        HashSet<prjState> prjStateSet = new HashSet<prjState>();
//        
//        /*
//         * Compute the set of input transitions for each module 
//         */
//        HashSet[] inputTranSetArray = new HashSet[arraySize];
//        for(int i = 0; i<arraySize; i++) {
//        	LPN curLpn = lpnList[i];
//        	HashSet<LPNTran> curInputTranSet = new HashSet<LPNTran>();
//        	HashSet<String> inputVarSet = curLpn.getInputs();
//            for(int other_i = 0; other_i <arraySize; other_i++) {
//            	if(i == other_i) continue;
//            	LPNTranSet otherTranSet = lpnList[other_i].getTransitions();
//            	for(LPNTran other_tran : otherTranSet) {
//            		HashSet<String> assignedVarSet = other_tran.getAssignedVar();
//            		for(String assignedVar : assignedVarSet) 
//            			if(inputVarSet.contains(assignedVar)==true) {
//            				curInputTranSet.add(other_tran);
//            				break;
//            			}
//            	}
//            }        	
//        	inputTranSetArray[i] = curInputTranSet;
//        }
//        
//        for(int i = 0; i<arraySize; i++) {
//        	for(LPNTran tran : (HashSet<LPNTran>)inputTranSetArray[i]) {
//            	System.out.print(tran.getFullLabel() + "  ");
//            }        	
//        	System.out.println("---------------------");
//        }
//        
//                       
//        /*
//         * Compute the untimed enabled transition arrays in the initial state 
//         */
//        LPNTranSet[] initEnabledArray = new LPNTranSet[arraySize];
//        ArrayList<LinkedList<LPNTran>> enabledArrayList = new ArrayList<LinkedList<LPNTran>>();
//        for(int i = 0; i<arraySize; i++) {
//        	LPNTranSet tmp = lpnList[i].getEnabled(curLocalStateArray[i]);
//        	initEnabledArray[i] = tmp;
//        	enabledArrayList.add(i, tmp);
//        }
//        
//        /*
//         * Compute the initial zone
//         */
////        MatrixZoneVer3Impl initZone = Zone.newMatrixZoneVer3();
////        initZone.initialize(enabledArrayList);
////        if (initZone instanceof MatrixVer3) {
////            ((MatrixVer3) initZone).release();
////        }
//        
//        Zone1 initZone = new Zone1();
//        initZone.initialize(initEnabledArray);
//                        
//        /*
//         * Compute the timed enabled transitions in the initial state
//         */
//        LPNTranSet initTimedEnabled = new LPNTranSet();
//        for(int i = 0; i<arraySize; i++) {
//        	for(LPNTran tran : initEnabledArray[i]) 
//        		//if (initZone.isTimeEnabled(tran.getDelayLB(), tran.getID()))
//        		if (initZone.checkTiming(tran)==true)
//        			initTimedEnabled.addLast(tran);
//        }
//        
//        if(initTimedEnabled.size()==0) {
//			System.err.println("---> ERROR: Verification failed: deadlock in the initial state.");
//			failure = true;
//			return;
//		}
//        
//        /*
//         * Project the initial zone to local ones for each LPN.
//         */
//        Zone1[] localZones = initZone.split(initEnabledArray);
//       
//        /*
//         * Initializing the stacks needed for search.
//         */
//        Stack<State[]> stateStack = new Stack<State[]>();
//        Stack<Zone> zoneStack = new Stack<Zone>();
//        Stack<Zone1> zone1Stack = new Stack<Zone1>();
//        Stack<LPNTranSet> lpnTranStack = new Stack<LPNTranSet>();
////                
////        for(int i = 0; i<arraySize; i++) {
////        	curLocalStateArray[i].setZone(localZones[i]);
////        	curLocalStateArray[i] = lpnList[i].add(curLocalStateArray[i]);
////        }
//        
//        
//        stateStack.push(curLocalStateArray);
//        //zoneStack.push(initZone);
//        zone1Stack.push(initZone);
//        //zoneTbl.put(initZone, initZone);
//        Zone1.uniqueCache.put(initZone, initZone);
//        lpnTranStack.push(initTimedEnabled);	
//        //prjStateSet.add(new prjState(curLocalStateArray));
//        prjStateSet.add(new prjStateTimed(curLocalStateArray, initZone));
//
//        int tranFiringCnt = 0;
//
//        int iterations = 0;
//        
//        /*
//         * Main search loop.
//         */
//        main_while_loop:
//        while(failure==false && stateStack.empty() == false) {
//        	iterations++;
//        	
//    		long curTotalMem = Runtime.getRuntime().totalMemory();
//			long curUsedMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//			
//			if(curTotalMem > peakTotalMem)
//				peakTotalMem = curTotalMem;
//			
//			if(curUsedMem > peakUsedMem)
//				peakUsedMem = curUsedMem;
//			
//			//if(iterations == 100) System.exit(0);
//        	if(iterations % 2000 == 0) {
//        		System.out.println("---> #iteration " + iterations + "> # LPN transition firings: " + tranFiringCnt
//						+ ", # of prjStates: " + prjStateSet.size()
//						+ ", # of zones: " + Zone1.uniqueCache.size()
//						+ ", max_stack_depth: " + max_stack_depth
//						+ " used memory: " + (float)curUsedMem / 1000000
//						+ " free memory: " + (float)Runtime.getRuntime().freeMemory() / 1000000);
//        	}
//        	
//            if (stateStack.size() > max_stack_depth) {
//                max_stack_depth = stateStack.size();
//            }
//        	
//            State[] curStateArray = stateStack.peek();
//            //Zone curZone = zoneStack.peek();
//            Zone1 curZone = zone1Stack.peek();
//            LPNTranSet curTimedEnabled = lpnTranStack.peek();
//            
//            if(curTimedEnabled.size()==0) {
//            	stateStack.pop();
//            	//zoneStack.pop();
//            	zone1Stack.pop();
//            	lpnTranStack.pop();
//            	continue main_while_loop;
//            }
//            
//            
//            LPNTran firedTran = curTimedEnabled.removeLast();
//            
//            //System.out.println("firedTran " + firedTran.getFullLabel());
//            
//            int curIndex = firedTran.getLpn().getIndex();
//            State[] nextStateArray = firedTran.fire(lpnList, curStateArray, curIndex);
//            tranFiringCnt++;
//
//            boolean deadlock = true, disabled = false;
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//            LPNTranSet[] curEnabledArray = new LPNTranSet[arraySize];
//            LPNTranSet[] nextEnabledArray = new LPNTranSet[arraySize];
//            for (int i = 0; i < arraySize; i++) {
//                LPN lpn_tmp = lpnList[i];
//                LPNTranSet enabledList = null;
//                enabledList = lpn_tmp.getEnabled(curStateArray[i]);
//                curEnabledArray[i] = enabledList;
//                enabledList = lpn_tmp.getEnabled(nextStateArray[i]);
//                nextEnabledArray[i] = enabledList.clone();
//            }
//
////          System.out.println(curZone);
////	          System.out.println("\nfiredTran = " + firedTran.getFullLabel() + "\n");
//            
//          Zone1 nextZone = curZone.update(firedTran, nextEnabledArray);
//        System.out.println(nextZone + "\n=================================\n");
//
//            
////          System.out.println(nextZone + "\n=================================\n");
//            
//
////            Zone newNextZone = zoneTbl.get(nextZone);
////            if(newNextZone == null)
////            	zoneTbl.put(nextZone, nextZone);
////            else
////            	nextZone = newNextZone;
//            
//            boolean newzone = false;
//            Zone1 newNextZone1 = Zone1.uniqueCache.get(nextZone);
//            if(newNextZone1 == null) {
//            	Zone1.uniqueCache.put(nextZone, nextZone);
//            	newzone= true;
//            }
//            else
//            	nextZone = newNextZone1;
//                        
//            LPNTranSet nextTimedEnabled = new LPNTranSet();
//            for(int i = 0; i<arraySize; i++) {
//            	LPNTranSet tmp = nextEnabledArray[i];
//            	for(LPNTran tran : tmp) {
//            		//if (nextZone.isTimeEnabled(tran.getDelayLB(), tran.getID()))
//            		if (nextZone.checkTiming(tran)==true)
//            			nextTimedEnabled.addLast(tran);
//            	}
//            }
//            
//            LPNTran disabledTran = firedTran.disablingError(curTimedEnabled, nextTimedEnabled);
//			if(disabledTran != null) {
//				System.out.println("---> Disabling Error: " + disabledTran.getFullLabel() + " is disabled by " + firedTran.getFullLabel());
//
//				System.out.println("Current state:");
//				for(int ii = 0; ii < arraySize; ii++) {
//					System.out.println("module " + lpnList[ii].getLabel());
//					System.out.println(curStateArray[ii]);
//					System.out.println("Enabled set: " + curEnabledArray[ii]);
//				}
//				System.out.println("Timed enabled transitions: " + curTimedEnabled);
//				
//				System.out.println("======================\nNext state:");
//				for(int ii = 0; ii < arraySize; ii++) {
//					System.out.println("module " + lpnList[ii].getLabel());
//					System.out.println(nextStateArray[ii]);
//					System.out.println("Enabled set: " + nextEnabledArray[ii]);
//				}
//				System.out.println("Timed enabled transitions: " + nextTimedEnabled);
//
//				System.out.println();
//
//				failure = true;	
//				break main_while_loop;
//			}
//			
//            if(nextTimedEnabled.size()==0) {
//    			System.out.println("---> ERROR: Verification failed: deadlock.");
//    			for(int ii = 0; ii < arraySize; ii++) {
//					System.out.println("module " + lpnList[ii].getLabel());
//					System.out.println(nextStateArray[ii]);
//					System.out.println("Enabled set: " + nextEnabledArray[ii]);
//				}
//    			System.out.println("Zone: " + nextZone);
//    			failure = true;
//    			continue main_while_loop;
//    		}
//                        
//           
//            //localZones = Zone.split((MatrixZoneVer3Impl) nextZone, nextEnabledArray);
////            localZones = nextZone.split(nextEnabledArray);
////            for(int i = 0; i<arraySize; i++) {
////            	nextStateArray[i].setZone(localZones[i]);
////            	nextStateArray[i] = lpnList[i].add(nextStateArray[i]);
////            	//System.out.print(nextStateArray[i].getLabel()+ ", ");// + "\n" + nextStateArray[i] + "\n");// + localZones[i] + "\n");
////            }
//        	//System.out.println("\n");
//
//            
//            boolean isNew = prjStateSet.add(new prjStateTimed(nextStateArray, nextZone));
//            //boolean isNew = prjStateSet.add(new prjStateTimed(nextStateArray, nextZone));
//
//
//            if (isNew == true){// || newzone==true) {
//            	stateStack.push(nextStateArray);
//            	//zoneStack.push(nextZone);
//            	zone1Stack.push(nextZone);
//            	lpnTranStack.push(nextTimedEnabled);
//            }
//        }
//     
//        System.out.println("---> # of unique zones = " + Zone1.uniqueCache.size() + "\n" +
//         					"---> # of unique enabled arrays = " + Zone1.enabledArrayCache.size() + "\n"
//        		);
//
//
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prjStateSet.size()
//                + ", max_stack_depth: " + max_stack_depth);
//    }
//
//    static void findsg_minimal_iterative_timed(LPN[] lpnList, State[] curLocalStateArray,
//            ArrayList<LinkedList<LPNTran>> enabledArray,
//            final Project prj) {
//        int hasSingleTrans = 0;
//        int notSingleTrans = 0;
//        int nextEnabledListSize = 0;
//        String oldZone = null, newZone = null;
//
//// <editor-fold defaultstate="collapsed" desc="init">
//        prj.mode = Mode.TIMED_ITERATIVE;
//        MatrixZoneVer3Impl initZone = new MatrixZoneVer3Impl();
//        final int INITIAL_STACK_SIZE = 1000;
//        LinkedList[] enArray = enabledArray.toArray(new LinkedList[0]);
//
//        initZone.initialize(enabledArray);
//        if (initZone instanceof MatrixVer3) {
//            ((MatrixVer3) initZone).release();
//        }
//        prjStateTimed initState = new prjStateTimed(curLocalStateArray, initZone);
//        prj.prjStateSet.add(initState);
//
////        for(Entry<prjState,Integer> o:prjStateSet.entrySet()){
////            System.out.printf("### %s:\t%s\t%s\n",
////                    o.getValue(),
////                   o.getKey().toString(),
////                    o.getKey().getClass().getName());
////        }
//        Zone[] zoneStack = new Zone[INITIAL_STACK_SIZE];
//        zoneStack[0] = initZone;
//        LPNTran[] firedStack = new LPNTran[INITIAL_STACK_SIZE];
//        prjStateTimed[] stateStack = new prjStateTimed[INITIAL_STACK_SIZE];
//        stateStack[0] = initState;
//        int ptr_stateStack = 1;
//        LinkedList[][] lpnTranStack = new LinkedList[INITIAL_STACK_SIZE][];
//        lpnTranStack[0] = enArray;
//        int[] curIndexStack = new int[INITIAL_STACK_SIZE];
//        int ptr_curIndexStack = 1;
//        graph.addInitNode(0 + "");
//        int tranFiringCnt = 0;
////        int lastWrite = 0;
//        // </editor-fold>
//        while ((ptr_stateStack == 0) == false) {
//
////            if (prj.prjStateSet.size() > 1000000) {
////                break;
////            }
//            if (ptr_stateStack > prj.max_stack_depth) {
//                prj.max_stack_depth = ptr_stateStack;
//            }
//
////             if (prjStateSet.size()%29==0&&lastWrite!=prjStateSet.size()){
////                 lastWrite=prjStateSet.size();
////        graph.write(String.format("graph_%s_%s-tran_%s-state.gv",mode,tranFiringCnt,  prjStateSet.size()));
////            }
//            int curIndex = curIndexStack[--ptr_curIndexStack];
//            LinkedList[] curEnabledArray = lpnTranStack[ptr_stateStack - 1];
//            LinkedList<LPNTran> curEnabledSet = curEnabledArray[curIndex];
//
//            int arraySize = curEnabledArray.length;
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                curIndex++;
//                if (curIndex == arraySize) {
//                    if (stateStack.length == ptr_stateStack) {
//                        ptr_stateStack--;
//                    } else {
////                        zoneStack[ptr_stateStack] = null;
////                        firedStack[ptr_stateStack] = null;
////                        stateStack[ptr_stateStack] = null;
////                        lpnTranStack[ptr_stateStack] = null;
//                        ptr_stateStack--;
//                    }
//                    continue;
//                }
//            }
//
//            curIndexStack[ptr_curIndexStack++] = curIndex;
//            prjStateTimed curPrjState = stateStack[ptr_stateStack - 1];
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                continue;
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - select a time enabled tran
//// <editor-fold defaultstate="collapsed" desc="select a time enabled tran">
//            LPNTran firedTran = null;
//            int idx = 0;
//            for (LPNTran t : curEnabledSet) {
//                if (curPrjState.getZone().isTimeEnabled(t.getDelayLB(), t.getID())) {
//                    firedTran = t;
//                    break;
//                }
//                idx++;
//            }
//            if (firedTran == null) {
////                printTimeDeadlock(curEnabledSet, curPrjState);
////            zoneStack[ptr_stateStack] = null;
////            firedStack[ptr_stateStack] = null;
////            stateStack[ptr_stateStack] = null;
////            lpnTranStack[ptr_stateStack] = null;
//                ptr_stateStack--;
//                continue;
//            }// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            State[] nextStateArray = firedTran.fire(
//                    lpnList, curPrjState.toStateArray(), curIndex);
//            tranFiringCnt++;
//
//            // Add nextPrjState into prjStateSet
//            // If nextPrjState has been traversed before, skip to the next
//            // enabled transition.
//            Zone zone = curPrjState.getZone().clone();
//            prjStateTimed nextPrjState = new prjStateTimed(nextStateArray, zone);
//
////            boolean deadlock = true, disabled = false;
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//            LinkedList[] curEnabledList = new LinkedList[arraySize];
//            LinkedList[] nextEnabledList = new LinkedList[arraySize];
//            for (int i = 0; i < arraySize; i++) {
//                LPN lpn_tmp = lpnList[i];
//                LinkedList<LPNTran> enabledList;
//                enabledList = lpn_tmp.getEnabledLpnTrans(curPrjState.toStateArray()[i], null, null, false);
//                curEnabledList[i] = new LinkedList(enabledList);
//                enabledList = lpn_tmp.getEnabledLpnTrans(nextPrjState.toStateArray()[i], null, null, false);
//                nextEnabledList[i] = enabledList;
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
////  if(PRINT_ENABLED)oldZone=zone.toString();
////            if(zone instanceof FinalizedCanonicalZone)System.err.println("ERROR: Using FinalizedCanonicalZone");
//
//            update(zone, firedTran, nextEnabledList, curEnabledList);
//// if(PRINT_ENABLED) newZone=zone.toString();
//// - - - - - - - - - - - - - - - - - - - - - - - - - - project allowable disables
//// <editor-fold defaultstate="collapsed" desc="project allowable disables">
//            //differenceSet = currentEnabledTransitions - nextEnabledTransitions
//            //remove un-needed timers from zone
//            //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
//            LPNTranSet diffEnabledTransitions = new LPNTranSet();
//            for (LinkedList<LPNTran> tList : curEnabledArray) {
//                diffEnabledTransitions.addAll(tList);
//            }
//            for (LinkedList<LPNTran> tList : nextEnabledList) {
//                diffEnabledTransitions.removeAll(tList);
//            }
//            for (LPNTran t : diffEnabledTransitions) {
//                if (firedTran == t) {
//                    continue;
//                }
//                zone.project(t.getID());
//            }// </editor-fold>
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
////if(zone instanceof MatrixVer3)((MatrixVer3)zone).release();
////            nextPrjState.setZone(((MatrixZoneVer3Impl)zone).cloneFinal());
//            boolean isNew = prj.prjStateSet.add(nextPrjState);
//// <editor-fold defaultstate="collapsed" desc="check for deadlocks and disables">
////             if(PRINT_ENABLED) nextEnabledListSize = 0;
////            for (int i = 0; i < arraySize; i++) {
////                if(PRINT_ENABLED) nextEnabledListSize += nextEnabledList[i].size();
////                LPNTran disabledTran = LPNTran.disablingError(curEnabledArray[i],
////                        nextEnabledList[i], firedTran);
////                if (disabledTran != null) {
////                    System.out.println("StateGraph:\t" + disabledTran.getLabelString()
////                            + " by " + firedTran.getLabelString()
////                            + ":\tVerification failed: disabling error.");
////                    disabled = true;
////                }
////                if (nextEnabledList[i] != null && arraySize > 0) {
////                    deadlock = false;
////                }
////            }
////            if (disabled) {
////                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
////                return;
////            } else if (deadlock) {
////                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
////                return;
////            } else {
////            }                // </editor-fold>
//
//            curEnabledSet.remove(idx);//must remove the current tran after using it
//// <editor-fold defaultstate="collapsed" desc="print and graph stuff">
////            if(PRINT_ENABLED) if (nextEnabledListSize == 1) {
////                hasSingleTrans++;
////            } else {
////                notSingleTrans++;
////            }
////            if(PRINT_ENABLED){
////            printNoDeadlock(firedTran, Arrays.asList(nextEnabledList),
////                    prj.prjStateSet, curPrjState, nextPrjState, oldZone, newZone);
////            graph.addEdge(prj.prjStateSet.tryInsert(curPrjState),
////                    prj.prjStateSet.tryInsert(nextPrjState), firedTran.getLabelString());
////            addStateToGraph(disabled, graph, nextPrjState, deadlock,prj.prjStateSet);
////            }
////          // </editor-fold>
//            if (isNew == false) {
//                continue;
//            }
//            //range check
//            if (ptr_stateStack >= stateStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                zoneStack = Arrays.copyOf(zoneStack, newSize);
//                firedStack = Arrays.copyOf(firedStack, newSize);
//                stateStack = Arrays.copyOf(stateStack, newSize);
//                lpnTranStack = Arrays.copyOf(lpnTranStack, newSize);
//            }
////            zoneStack[ptr_stateStack] = zone;
////            firedStack[ptr_stateStack] = firedTran;
//            stateStack[ptr_stateStack] = nextPrjState;
//            lpnTranStack[ptr_stateStack++] = nextEnabledList;
//
//            if (ptr_curIndexStack >= curIndexStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                curIndexStack = Arrays.copyOf(curIndexStack, newSize);
//            }
//            curIndexStack[ptr_curIndexStack++] = 0;
//
//        }
////        for(Entry<prjState,Integer> o:prjStateSet.entrySet()){
////            System.out.printf("### %s:\t%s\t%s\n",
////                    o.getValue(),
////                   o.getKey().toString(),
////                    o.getKey().getClass().getName());
////        }
//
//
//        START_TIME = System.currentTimeMillis() - START_TIME;
//        garbage();
//        System.gc();
//
//        printer.put("TIME", formatTime(START_TIME));
//        printer.put("VERSION", version + "");
//        printer.put("TIMED_ANALYSIS", StateGraph.TIMED_ANALYSIS + "");
//        printer.put("      ", "");
//        printer.put("USED memory", "" + LONG.format(
//                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
////        printer.put("STATE CACHE SIZE", stateCache.size() + "");
////        printer.put("ZONE CACHE SIZE", zoneCache.size() + "");
//        printer.put("PRJ STATE CACHE SIZE", prjStateCache.size() + "");
//        printer.put("bytes/state", "" + LONG.format(
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
//                / (double) prjStateCache.size()));
//        printer.put(" ", "");
//        printer.put("STATES", "" + prj.prjStateSet.size());
//        printer.put("TRANSITIONS", "" + tranFiringCnt);
//        printer.print();
////        graph.write(String.format("graph_%s_%s-tran_%s-state.gv", prj.mode, tranFiringCnt, prj.prjStateSet.size()));
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prj.prjStateSet.size()
//                + ", max_stack_depth: " + prj.max_stack_depth);
//    }
//
//    static void findsg_bfs_cached_iterative_timed(LPN[] LPN_ARRAY, State[] INIT_STATE,
//            ArrayList<LinkedList<LPNTran>> INIT_ENABLED_TRANS,
//            final Project prj) {
//
//        Cache stateCache = Cache.getNewCache(100);
////        DCache zoneCache = new Cache.DCache(1024, 16);
//        Cache zoneCache = Cache.getNewCache(100);
//
//        final int INITIAL_STACK_SIZE = 1000;
//        MatrixZoneVer3Impl INIT_ZONE = new MatrixZoneVer3Impl();
//        int notSingleTrans = 0;
//        int hasSingleTrans = 0;
//        int ptr_stateStack = 1;
//        int tranFiringCnt = 0;
//        int nextEnabledListSize = 2;
//        String oldZone = "", newZone = "";
//        prj.mode = Mode.TIMED_ITERATIVE;
//        LPNTran[][] enArray = new LPNTran[INIT_ENABLED_TRANS.size()][];
//        for (int i = 0; i < enArray.length; i++) {
//            LinkedList<LPNTran> list = INIT_ENABLED_TRANS.get(i);
//            enArray[i] = list.toArray(new LPNTran[0]);
//        }
//
//        graph.addInitNode(0 + "");
//        INIT_ZONE.initialize(INIT_ENABLED_TRANS);
//        if (INIT_ZONE instanceof MatrixVer3) {
//            ((MatrixVer3) INIT_ZONE).release();
//        }
//
//        initStateArray = INIT_STATE;
//        initialZone = INIT_ZONE;
//        prj.prjStateSet.add(new prjStateTimed(INIT_STATE, INIT_ZONE));
//        insertState(INIT_STATE, stateCache, zoneCache, INIT_ZONE, prjStateCache);
//        Zone[] zoneStack = new Zone[INITIAL_STACK_SIZE];
//        zoneStack[0] = INIT_ZONE;
//
//        LPNTran[] firedStack = new LPNTran[INITIAL_STACK_SIZE];
//        State[][] stateStack = new State[INITIAL_STACK_SIZE][];
//        /*Stack per prjState
//         *      Set of Set of trans per LPN
//         *              set of LPNTrans per local state
//         */
//        LPNTran[][][] prjTranStack = new LPNTran[INITIAL_STACK_SIZE][][];
//        stateStack[0] = INIT_STATE;
//        prjTranStack[0] = enArray;
//        LPNTran[][] nextEnabledList;
//        while (ptr_stateStack != 0) {
//            ptr_stateStack--;
//
////                    System.out.println("STACK("+ptr_stateStack+")");
//            LPNTran[][] curModuleTranSet = prjTranStack[ptr_stateStack];
//            State[] curPrjState = stateStack[ptr_stateStack];
//            Zone ZONE = zoneStack[ptr_stateStack];
//            int idx = -1;
//            LPNTran[] curEnabledSet;
//            LPNTran firedLocalTran;
//            for (int i = 0; i < curModuleTranSet.length; i++) {
//                idx++;
////                    System.out.println("MOD("+idx+")");
//                curEnabledSet = curModuleTranSet[i];
//
//                for (int j = 0; j < curEnabledSet.length; j++) {
////                    System.out.println("LOCAL("+j+")");
//                    firedLocalTran = curEnabledSet[j];
//                    if (firedLocalTran == null
//                            || !ZONE.isTimeEnabled(
//                            firedLocalTran.getDelayLB(), firedLocalTran.getID())) {
//                        continue;
//                    }
//
//                    Zone zone2 = ZONE.clone();
////                    System.out.println();
////                    System.out.println();
////                    System.out.println(firedLocalTran.getLabelString());
////                    System.out.println(zone2);
////                    System.out.println("ENABLED? "+zone2.isTimeEnabled(
////                            firedLocalTran.getDelayLB(), firedLocalTran.getID())
////                            );
////                    if (!zone2.isTimeEnabled(firedLocalTran.getDelayLB(), firedLocalTran.getID())) {
////                        continue;
////                    }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - fire
//                    State[] nextStateArray = firedLocalTran.fire(LPN_ARRAY,
//                            curPrjState,  idx);
//                    tranFiringCnt++;
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//                    nextEnabledList = new LPNTran[curModuleTranSet.length][];//[module][local state tran]
//                    int sizeN = 0, sizeC = 0;
//                    LinkedList[] NEXT = new LinkedList[6];
//                    LinkedList[] CURRENT = new LinkedList[6];
//                    for (int k = 0; k < curModuleTranSet.length; k++) {
//                        //current
//                        nextEnabledList[k] = LPN_ARRAY[k].getEnabledLpnTrans2(
//                                curPrjState[k], null, null, false, null);
//                        CURRENT[k].addAll(Arrays.asList(nextEnabledList[k]));
//                        nextEnabledList[k] = LPN_ARRAY[k].getEnabledLpnTrans2(
//                                nextStateArray[k], null, null, false, null);
//                        NEXT[k].addAll(Arrays.asList(nextEnabledList[k]));
//                    }
//                    zone2.update(firedLocalTran, NEXT, CURRENT,
//                            nextEnabledList, curModuleTranSet);
//
////                    prjStateTimed nextPrjState = new prjStateTimed(nextStateArray, zone2);
//                    boolean isNew = //prj.prjStateSet.add(nextPrjState);
//                            //                    boolean b2 =
//                            insertState(nextStateArray, stateCache, zoneCache, zone2, prjStateCache);
////                    if (PRINT_ENABLED && b2 ^ isNew) {
////                        System.out.printf("NEW? IDGen: %s\tCACHE: %s\n", isNew, b2);
////                        System.out.printf("%s\n%s\n", nextPrjState, zone2);
////                    }
////                    if (PRINT_ENABLED) {
////                        if (nextEnabledListSize == 1) {
////                            hasSingleTrans++;
////                        } else {
////                            notSingleTrans++;
////                        }
////                    }
////                    if (PRINT_ENABLED) {
////                        prjStateTimed curState = new prjStateTimed(curPrjState, ZONE);
////                        printNoDeadlock(firedLocalTran, nextEnabledList,
////                                prj.prjStateSet, curState, nextPrjState, oldZone, newZone);
////                        graph.addEdge(prj.prjStateSet.tryInsert(curState),
////                                prj.prjStateSet.tryInsert(nextPrjState), firedLocalTran.getLabelString());
////                        addStateToGraph(disabled, graph, nextPrjState, deadlock, prj.prjStateSet);
////                    }
//                    if (isNew == false) {
//                        continue;
//                    }
//
//                    //range check
//                    if (ptr_stateStack >= stateStack.length) {
//                        int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                        zoneStack = Arrays.copyOf(zoneStack, newSize);
//                        firedStack = Arrays.copyOf(firedStack, newSize);
//                        stateStack = Arrays.copyOf(stateStack, newSize);
//                        prjTranStack = Arrays.copyOf(prjTranStack, newSize);
//                    }
//
//                    zoneStack[ptr_stateStack] = zone2;
//                    firedStack[ptr_stateStack] = firedLocalTran;
//                    stateStack[ptr_stateStack] = nextStateArray;
//                    prjTranStack[ptr_stateStack] = nextEnabledList;
//                    ptr_stateStack++;
//                }
//            }//END  for (LinkedList<LPNTran> curEnabledSet : curEnabledArray)
//        }//END while (ptr_stateStack != 0)
//
//
//        START_TIME = System.currentTimeMillis() - START_TIME;
//        garbage();
//        System.gc();
//        double size = ((double) MatrixZoneVer3Impl.sumOfSize / (double) MatrixZoneVer3Impl.clones);
//        double expectedMem = (size * size) * 4 + size * (1 + 2 * 4) + 38;
////        printer.put("sumOfSize", LONG.format(MatrixZoneVer3Impl.sumOfSize));
////        printer.put("average zone size", "" + FLOAT.format(size));
////        printer.put("texpectedMem", "" + FLOAT.format(expectedMem));
////        printer.put("estMemory", "" + String.format("%s bytes\t\t%s MB",
////                LONG.format((double) MatrixZoneVer3Impl.estMemory),
////                FLOAT.format((double) MatrixZoneVer3Impl.estMemory / 1024f / 1024f)));
////        printer.put("EM/clone", String.format("%s bytes\t\t%s KB\n",
////                FLOAT.format((double) MatrixZoneVer3Impl.estMemory / (double) MatrixZoneVer3Impl.clones),
////                FLOAT.format((double) MatrixZoneVer3Impl.estMemory / 1024f / (double) MatrixZoneVer3Impl.clones)));
////        printer.put("clones", String.format("%s\tnotSingleTrans=%s\tsingleTrans=%s\n",
////                LONG.format(MatrixZoneVer3Impl.clones),
////                LONG.format(notSingleTrans),
////                LONG.format(hasSingleTrans)));
////        printer.put("% saving", String.format("%s\tcost=%s bytes\tsavings=%s bytes\n",
////                FLOAT.format((1 - ((double) notSingleTrans
////                / ((double) notSingleTrans + hasSingleTrans))) * 100),
////                LONG.format(((double) notSingleTrans)
////                * MatrixZoneVer3Impl.estMemory / (double) MatrixZoneVer3Impl.clones),
////                FLOAT.format(
////                hasSingleTrans * MatrixZoneVer3Impl.estMemory / (double) MatrixZoneVer3Impl.clones)));
//
//        printer.put("TIME", formatTime(START_TIME));
//        printer.put("VERSION", version + "");
//        printer.put("TIMED_ANALYSIS", StateGraph.TIMED_ANALYSIS + "");
//        printer.put("      ", "");
//        printer.put("USED memory", "" + LONG.format(
//                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//        printer.put("STATE CACHE SIZE", stateCache.size() + "");
//        printer.put("ZONE CACHE SIZE", zoneCache.size() + "");
//        printer.put("PRJ STATE CACHE SIZE", prjStateCache.size() + "");
//        printer.put("bytes/state", "" + LONG.format(
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
//                / (double) prjStateCache.size()));
//        printer.put(" ", "");
//        printer.put("STATES", "" + prjStateCache.size());
//        printer.put("TRANSITIONS", "" + tranFiringCnt);
//        printer.print();
////        graph.write(String.format("graph_%s_%s-tran_%s-state.gv", prj.mode, tranFiringCnt, prj.prjStateSet.size()));
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prjStateCache.size()
//                + ", max_stack_depth: " + prj.max_stack_depth);
//    }
//    static State[] initStateArray;
//    static Zone initialZone;
//
//    static void findsg_dfs_cached_minimal_iterative_timed(LPN[] lpnList, State[] curLocalStateArray,
//            ArrayList<LinkedList<LPNTran>> enabledArray,
//            final Project prj) {
//        prj.mode = Mode.TIMED_ITERATIVE;
//        MatrixZoneVer3Impl initZone = new MatrixZoneVer3Impl();
//        DCache zoneCache = new Cache.DCache(1024, 16);
//        initStateArray = curLocalStateArray;
//        initialZone = initZone;
////        Cache zoneCache = Cache.getNewCache(1024);
//        Cache stateCache = Cache.getNewCache(1024);
//        final int INITIAL_STACK_SIZE = 1000;
//        LinkedList[] enArray = enabledArray.toArray(new LinkedList[0]);
//        initZone.initialize(enabledArray);
//
//        prjStateTimed initState = new prjStateTimed(curLocalStateArray, initZone);
//        insertState(curLocalStateArray, stateCache, zoneCache, initZone, prjStateCache);
//        Zone[] zoneStack = new Zone[INITIAL_STACK_SIZE];
//        LPNTran[] firedStack = new LPNTran[INITIAL_STACK_SIZE];
//        prjStateTimed[] stateStack = new prjStateTimed[INITIAL_STACK_SIZE];
//        LinkedList[][] lpnTranStack = new LinkedList[INITIAL_STACK_SIZE][];
//        int[] curIndexStack = new int[INITIAL_STACK_SIZE];
//        zoneStack[0] = initZone;
//        stateStack[0] = initState;
//        int ptr_stateStack = 1;
//        lpnTranStack[0] = enArray;
//        int ptr_curIndexStack = 1;
//        int tranFiringCnt = 0;
//        Zone zone = null;
//        LPNTran firedTran = null;
//        LinkedList[] curEnabledArray = null;
//        prjStateTimed nextPrjState = null;
//        int arraySize = 0;
//        LinkedList<LPNTran> curEnabledSet = null;
//        int idx = 0;
//        //=======================================================================
//        while ((ptr_stateStack == 0) == false) {
//            int curIndex = curIndexStack[--ptr_curIndexStack];
//            curEnabledArray = lpnTranStack[ptr_stateStack - 1];
//            curEnabledSet = curEnabledArray[curIndex];
//            arraySize = curEnabledArray.length;
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                curIndex++;
//                if (curIndex == arraySize) {
//                    if (stateStack.length == ptr_stateStack) {
//                        ptr_stateStack--;
//                    } else {
//                        freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                        ptr_stateStack--;
//                    }
//                    continue;
//                }
//            }
//
//            curIndexStack[ptr_curIndexStack++] = curIndex;
//            prjStateTimed curPrjState = stateStack[ptr_stateStack - 1];
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                continue;
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - select a time enabled tran
//            firedTran = null;
//            idx = 0;
//            for (LPNTran t : curEnabledSet) {
//                if (curPrjState.getZone().isTimeEnabled(t.getDelayLB(), t.getID())) {
//                    firedTran = t;
//                    break;
//                }
//                idx++;
//            }
//            if (firedTran == null) {
//                freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                ptr_stateStack--;
//                continue;
//            }// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            State[] nextStateArray = firedTran.fire(lpnList, curPrjState.toStateArray(), curIndex);
//            tranFiringCnt++;
//
//            // Add nextPrjState into prjStateSet
//            // If nextPrjState has been traversed before, skip to the next
//            // enabled transition.
//            nextPrjState = new prjStateTimed(nextStateArray);
//
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//            if (curEnabledList.length != arraySize) {
//                curEnabledList = new LinkedList[arraySize];
//            }
//            nextEnabledList = new LinkedList[arraySize];
//            for (int i = 0; i < arraySize; i++) {
//                final LPN lpn_tmp = lpnList[i];
//                curEnabledList[i] = (lpn_tmp.getEnabledLpnTrans(
//                        curPrjState.toStateArray()[i], null, null, false));
//                nextEnabledList[i] = lpn_tmp.getEnabledLpnTrans(
//                        nextStateArray[i], null, null, false);
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            zone = curPrjState.getZone().clone();
//            update(zone, firedTran, nextEnabledList, curEnabledList);
//// - - - - - - - - - - - - - - - - - - - - - - - - - - project allowable disables
//            //differenceSet = currentEnabledTransitions - nextEnabledTransitions
//            //remove un-needed timers from zone
//            //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
//            LPNTranSet diffEnabledTransitions = new LPNTranSet();
//            for (LinkedList<LPNTran> tList : curEnabledArray) {
//                diffEnabledTransitions.addAll(tList);
//            }
//            for (LinkedList<LPNTran> tList : nextEnabledList) {
//                diffEnabledTransitions.removeAll(tList);
//            }
//            for (LPNTran t : diffEnabledTransitions) {
//                if (firedTran == t) {
//                    continue;
//                }
//                zone.project(t.getID());
//            }
//            nextPrjState.setZone((zone));
//            boolean isNew = insertState(nextStateArray, stateCache, zoneCache, zone, prjStateCache);
//            if (isNew == false) {
//                curEnabledSet.remove(idx);//must remove the current tran after using it
//                continue;
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            for (int i = 0; i < arraySize; i++) {
//
//                LPNTran disabledTran = LPNTran.disablingError(curEnabledArray[i],
//                        nextEnabledList[i], firedTran);
//                if (disabledTran != null) {
//                    System.out.println("StateGraph:\t" + disabledTran.getLabelString()
//                            + " by " + firedTran.getLabelString()
//                            + ":\tVerification failed: disabling error.");
//                    disabled = true;
//                }
//                if (nextEnabledList[i] != null && arraySize > 0) {
//                    deadlock = false;
//                }
//            }
//            if (disabled) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            } else if (deadlock) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            }
//
//            curEnabledSet.remove(idx);//must remove the current tran after using it
//            if (isNew == false) {
//                continue;
//            }
//            //range check
//            if (ptr_stateStack >= stateStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                zoneStack = Arrays.copyOf(zoneStack, newSize);
//                firedStack = Arrays.copyOf(firedStack, newSize);
//                stateStack = Arrays.copyOf(stateStack, newSize);
//                lpnTranStack = Arrays.copyOf(lpnTranStack, newSize);
//            }
//            zoneStack[ptr_stateStack] = zone;
//            firedStack[ptr_stateStack] = firedTran;
//            stateStack[ptr_stateStack] = nextPrjState;
//            lpnTranStack[ptr_stateStack++] = nextEnabledList;
//
//            if (ptr_curIndexStack >= curIndexStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                curIndexStack = Arrays.copyOf(curIndexStack, newSize);
//            }
//            curIndexStack[ptr_curIndexStack++] = 0;
//        }
//
//        START_TIME = System.currentTimeMillis() - START_TIME;
//
//        garbage();
//        System.gc();
//        printer.put("TIME", formatTime(START_TIME));
//        printer.put("VERSION", version + "");
//        printer.put("TIMED_ANALYSIS", StateGraph.TIMED_ANALYSIS + "");
//        printer.put("      ", "");
//        printer.put("USED memory", "" + LONG.format(
//                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//        printer.put("STATE CACHE SIZE", stateCache.size() + "");
//        printer.put("ZONE CACHE SIZE", zoneCache.size() + "");
//        printer.put("PRJ STATE CACHE SIZE", prjStateCache.size() + "");
//        printer.put("bytes/state", "" + LONG.format(
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
//                / (double) prjStateCache.size()));
//        printer.put(" ", "");
//        printer.put("STATES", "" + prjStateCache.size());
//        printer.put("TRANSITIONS", "" + tranFiringCnt);
//        printer.print();
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prjStateCache.size()
//                + ", max_stack_depth: " + prj.max_stack_depth);
//    }
//
//    static void findsg_dfs_cached_iterative_timed(LPN[] lpnList, State[] curLocalStateArray,
//            ArrayList<LinkedList<LPNTran>> enabledArray,
//            final Project prj) {
//
//// <editor-fold defaultstate="collapsed" desc="init">
//        int hasSingleTrans = 0;
//        int notSingleTrans = 0;
//        int nextEnabledListSize = 0;
//        String oldZone = null, newZone = null;
//
//        IdentityCache zoneCache = (IdentityCache) Cache.getNewIdentityCache(1000);
//        Cache stateCache = Cache.getNewCache(1000);
//
//        prj.mode = Mode.TIMED_ITERATIVE;
//        Zone initZone = Zone.newZone();
//        final int INITIAL_STACK_SIZE = 1000;
//        LinkedList[] enArray = enabledArray.toArray(new LinkedList[0]);
//
//        initZone.initialize(enabledArray);
//        prjStateTimed initState = new prjStateTimed(curLocalStateArray, initZone);
//        insertState(curLocalStateArray, stateCache, zoneCache, initZone, prjStateCache);
//
//
//        Zone[] zoneStack = new Zone[INITIAL_STACK_SIZE];
//        zoneStack[0] = initZone;
//        LPNTran[] firedStack = new LPNTran[INITIAL_STACK_SIZE];
//        prjStateTimed[] stateStack = new prjStateTimed[INITIAL_STACK_SIZE];
//        stateStack[0] = initState;
//        int ptr_stateStack = 1;
//        LinkedList[][] lpnTranStack = new LinkedList[INITIAL_STACK_SIZE][];
//        lpnTranStack[0] = enArray;
//        int[] curIndexStack = new int[INITIAL_STACK_SIZE];
//        int ptr_curIndexStack = 1;
//        graph.addInitNode(0 + "");
//        int tranFiringCnt = 0;
//        // </editor-fold>
//        while ((ptr_stateStack == 0) == false) {
//
////            if (prj.prjStateSet.size() > 1000000) {
////                break;
////            }
//            if (ptr_stateStack > prj.max_stack_depth) {
//                prj.max_stack_depth = ptr_stateStack;
//            }
//
//            int curIndex = curIndexStack[--ptr_curIndexStack];
//            LinkedList[] curEnabledArray = lpnTranStack[ptr_stateStack - 1];
//            LinkedList<LPNTran> curEnabledSet = curEnabledArray[curIndex];
//
//            int arraySize = curEnabledArray.length;
//            //System.out.println("curEnabledSet=" + curEnabledSet + "\t" + Arrays.toString(curEnabledArray));
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                curIndex++;
//                if (curIndex == arraySize) {
//                    if (stateStack.length == ptr_stateStack) {
//                        ptr_stateStack--;
//                    } else {
//                        freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                        ptr_stateStack--;
//                    }
//                    continue;
//                }
//            }
//
//            curIndexStack[ptr_curIndexStack++] = curIndex;
//            prjStateTimed curPrjState = stateStack[ptr_stateStack - 1];
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                continue;
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - select a time enabled tran
//// <editor-fold defaultstate="collapsed" desc="select a time enabled tran">
//            LPNTran firedTran = null;
//            int idx = 0;
//            for (LPNTran t : curEnabledSet) {
//                if (curPrjState.getZone().isTimeEnabled(t.getDelayLB(), t.getID())) {
//                    firedTran = t;
//                    break;
//                }
//                idx++;
//            }
//            if (firedTran == null) {
//                freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                ptr_stateStack--;
//                continue;
//            }// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            State[] nextStateArray = firedTran.fire(
//                    lpnList, curPrjState.toStateArray(), curIndex);
//            tranFiringCnt++;
//
//            // Add nextPrjState into prjStateSet
//            // If nextPrjState has been traversed before, skip to the next
//            // enabled transition.
//            Zone zone = curPrjState.getZone().clone();
//            prjStateTimed nextPrjState = new prjStateTimed(nextStateArray, zone);
//
////            boolean deadlock = true, disabled = false;
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//            if (curEnabledList.length != arraySize) {
//                curEnabledList = new LinkedList[arraySize];
//            }
//            nextEnabledList = new LinkedList[arraySize];
//            for (int i = 0; i < arraySize; i++) {
//                final LPN lpn_tmp = lpnList[i];
//                curEnabledList[i] = (lpn_tmp.getEnabledLpnTrans(
//                        curPrjState.toStateArray()[i], null, null, false));
//                nextEnabledList[i] = lpn_tmp.getEnabledLpnTrans(
//                        nextPrjState.toStateArray()[i], null, null, false);
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            if (PRINT_ENABLED) {
//                oldZone = zone.toString();
//            }
//            if (zone instanceof FinalizedCanonicalZone) {
//                System.err.println("ERROR: Using FinalizedCanonicalZone");
//            }
//
//            update(zone, firedTran, nextEnabledList, curEnabledList);
//            if (PRINT_ENABLED) {
//                newZone = zone.toString();
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - project allowable disables
//// <editor-fold defaultstate="collapsed" desc="project allowable disables">
//            //differenceSet = currentEnabledTransitions - nextEnabledTransitions
//            //remove un-needed timers from zone
//            //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
//            LPNTranSet diffEnabledTransitions = new LPNTranSet();
//            for (LinkedList<LPNTran> tList : curEnabledArray) {
//                diffEnabledTransitions.addAll(tList);
//            }
//            for (LinkedList<LPNTran> tList : nextEnabledList) {
//                diffEnabledTransitions.removeAll(tList);
//            }
//            for (LPNTran t : diffEnabledTransitions) {
//                if (firedTran == t) {
//                    continue;
//                }
//                zone.project(t.getID());
//            }// </editor-fold>
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            boolean isNew = insertState(nextStateArray, stateCache, zoneCache, zone, prjStateCache);
//// <editor-fold defaultstate="collapsed" desc="check for deadlocks and disables">
//            if (PRINT_ENABLED) {
//                nextEnabledListSize = 0;
//            }
//            for (int i = 0; i < arraySize; i++) {
//                if (PRINT_ENABLED) {
//                    nextEnabledListSize += nextEnabledList[i].size();
//                }
//                LPNTran disabledTran = LPNTran.disablingError(curEnabledArray[i],
//                        nextEnabledList[i], firedTran);
//                if (disabledTran != null) {
//                    System.out.println("StateGraph:\t" + disabledTran.getLabelString()
//                            + " by " + firedTran.getLabelString()
//                            + ":\tVerification failed: disabling error.");
//                    disabled = true;
//                }
//                if (nextEnabledList[i] != null && arraySize > 0) {
//                    deadlock = false;
//                }
//            }
//            if (disabled) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            } else if (deadlock) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            } else {
//            }                // </editor-fold>
//
//            curEnabledSet.remove(idx);//must remove the current tran after using it
//// <editor-fold defaultstate="collapsed" desc="print and graph stuff">
//            if (PRINT_ENABLED) {
//                if (nextEnabledListSize == 1) {
//                    hasSingleTrans++;
//                } else {
//                    notSingleTrans++;
//                }
//            }
//            if (PRINT_ENABLED) {
//                printNoDeadlock(firedTran, Arrays.asList(nextEnabledList),
//                        prj.prjStateSet, curPrjState, nextPrjState, oldZone, newZone);
//                graph.addEdge(prj.prjStateSet.tryInsert(curPrjState),
//                        prj.prjStateSet.tryInsert(nextPrjState), firedTran.getLabelString());
//                addStateToGraph(disabled, graph, nextPrjState, deadlock, prj.prjStateSet);
//            }
////          // </editor-fold>
//            if (isNew == false) {
//                continue;
//            }
//            //range check
//            if (ptr_stateStack >= stateStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                zoneStack = Arrays.copyOf(zoneStack, newSize);
//                firedStack = Arrays.copyOf(firedStack, newSize);
//                stateStack = Arrays.copyOf(stateStack, newSize);
//                lpnTranStack = Arrays.copyOf(lpnTranStack, newSize);
//            }
//            zoneStack[ptr_stateStack] = zone;
//            firedStack[ptr_stateStack] = firedTran;
//            stateStack[ptr_stateStack] = nextPrjState;
//            lpnTranStack[ptr_stateStack++] = nextEnabledList;
//
//            if (ptr_curIndexStack >= curIndexStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                curIndexStack = Arrays.copyOf(curIndexStack, newSize);
//            }
//            curIndexStack[ptr_curIndexStack++] = 0;
//
//        }
//        START_TIME = System.currentTimeMillis() - START_TIME;
//        garbage();
//        System.gc();
//
//        printer.put("TIME", formatTime(START_TIME));
//        printer.put("VERSION", version + "");
//        printer.put("      ", "");
//        printer.put("USED memory", "" + LONG.format(
//                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//        printer.put("STATE CACHE SIZE", stateCache.size() + "");
//        printer.put("ZONE CACHE SIZE", zoneCache.size() + "");
//        printer.put("PRJ STATE CACHE SIZE", prjStateCache.size() + "");
//        printer.put("bytes/state", "" + LONG.format(
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
//                / (double) prjStateCache.size()));
//        printer.put(" ", "");
//        printer.put("STATES", "" + prjStateCache.size());
//        printer.put("TRANSITIONS", "" + tranFiringCnt);
//        printer.print();
////        graph.write(String.format("graph_%s_%s-tran_%s-state.gv", prj.mode, tranFiringCnt, prj.prjStateSet.size()));
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prj.prjStateSet.size()
//                + ", max_stack_depth: " + prj.max_stack_depth);
//    }
//
//    static void findsg_cached_iterative_untimed(LPN[] lpnList, State[] curLocalStateArray,
//            ArrayList<LinkedList<LPNTran>> enabledArray,
//            final Project prj) {
//
//        initStateArray = curLocalStateArray;
//        Cache stateCache = Cache.getNewCache(100000);
//
//        final int INITIAL_STACK_SIZE = 100000;
//        int ptr_stateStack = 1;
//        int tranFiringCnt = 0;
//        prj.mode = Mode.TIMED_ITERATIVE;
//        LPNTran[][] enArray = new LPNTran[enabledArray.size()][];
//        for (int i = 0; i < enArray.length; i++) {
//            LinkedList<LPNTran> list = enabledArray.get(i);
//            enArray[i] = list.toArray(new LPNTran[0]);
//        }
//
//        insertState(curLocalStateArray, stateCache, prjStateCache);
//
//        State[][] stateStack = new State[INITIAL_STACK_SIZE][];
//        LPNTran[][][] lpnTranStack = new LPNTran[INITIAL_STACK_SIZE][][];
//        stateStack[0] = curLocalStateArray;
//        lpnTranStack[0] = enArray;
//        LPNTran[][] nextEnabledList;
//        LPNTran[] curEnabledSet;
//        LPNTran firedTran;
//        LinkedList<LPNTran[][]> tranCache = new LinkedList<LPNTran[][]>();
//        while (ptr_stateStack != 0) {
//            ptr_stateStack--;
//            LPNTran[][] curEnabledArray = lpnTranStack[ptr_stateStack];
//            final State[] curPrjState = stateStack[ptr_stateStack];
//            int idx = -1;
//            for (int i = 0; i < curEnabledArray.length; i++) {
//                idx++;
//                curEnabledSet = curEnabledArray[i];
//
//                for (int j = 0; j < curEnabledSet.length; j++) {
//                    firedTran = curEnabledSet[j];
//                    if (firedTran == null) {
//                        continue;
//                    }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - fire
//                    State[] nextStateArray = firedTran.fire(lpnList, curPrjState, idx);
//                    tranFiringCnt++;
//                    boolean isNew = insertState(nextStateArray, stateCache, prjStateCache);
//
//                    if (isNew == false) {
//                        continue;
//                    }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//                    if (tranCache.size() > 0) {
//                        nextEnabledList = tranCache.pop();
//                    } else //                    if(nextEnabledList==null||nextEnabledList.length!=curEnabledArray.length)
//                    {
//                        nextEnabledList = new LPNTran[curEnabledArray.length][];
//                    }
//
//                    for (int k = 0; k < curEnabledArray.length; k++) {
//                        final LPN lpn_tmp = lpnList[k];
//                        nextEnabledList[k] = lpn_tmp.getEnabledLpnTrans2(
//                                nextStateArray[k], null, null, false, nextEnabledList[k]);
//                    }
//
//                    //range check
//                    if (ptr_stateStack >= stateStack.length) {
//                        int newSize = ptr_stateStack + 10000;
//                        stateStack = Arrays.copyOf(stateStack, newSize);
//                        lpnTranStack = Arrays.copyOf(lpnTranStack, newSize);
//                    }
//                    stateStack[ptr_stateStack] = nextStateArray;
//                    if (lpnTranStack[ptr_stateStack] != null) {
//                        tranCache.add(lpnTranStack[ptr_stateStack]);
//                    }
////                    while(tranCache.size()>5)tranCache.pop();
//                    lpnTranStack[ptr_stateStack] = nextEnabledList;
//                    ptr_stateStack++;
//                }//END for (LPNTran firedTran : curEnabledSet)
//            }//END  for (LinkedList<LPNTran> curEnabledSet : curEnabledArray)
//        }//END while (ptr_stateStack != 0)
//
//        START_TIME = System.currentTimeMillis() - START_TIME;
//        garbage();
//        System.gc();
//
//        printer.put("TIME", formatTime(START_TIME));
//        printer.put("VERSION", version + "");
//        printer.put("TIMED_ANALYSIS", StateGraph.TIMED_ANALYSIS + "");
//        printer.put("      ", "");
//        printer.put("USED memory", "" + LONG.format(
//                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//        printer.put("STATE CACHE SIZE", stateCache.size() + "");
////        printer.put("ZONE CACHE SIZE", zoneCache.size() + "");
//        printer.put("PRJ STATE CACHE SIZE", prjStateCache.size() + "");
//        printer.put("bytes/state", "" + LONG.format(
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
//                / (double) prjStateCache.size()));
//        printer.put(" ", "");
//        printer.put("STATES", "" + prjStateCache.size());
//        printer.put("TRANSITIONS", "" + tranFiringCnt);
//        printer.print();
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prjStateCache.size()
//                + ", max_stack_depth: " + prj.max_stack_depth);
//    }
//
//    static void findsg_mdd_timed_ver_1(LPN[] lpnList, State[] curLocalStateArray,
//            ArrayList<LinkedList<LPNTran>> enabledArray,
//            final Project prj) {
//        //array ={s1..sn, z}    new Mdd(n+1)
//
//
//
//// <editor-fold defaultstate="collapsed" desc="init">
//        int hasSingleTrans = 0;
//        int notSingleTrans = 0;
//        int nextEnabledListSize = 0;
//        String oldZone = null, newZone = null;
//
//        IdentityCache zoneCache = (IdentityCache) Cache.getNewIdentityCache(1000);
//        Cache stateCache = Cache.getNewCache(1000);
//
//        prj.mode = Mode.TIMED_ITERATIVE;
//        Zone initZone = Zone.newZone();
//        final int INITIAL_STACK_SIZE = 1000;
//        LinkedList[] enArray = enabledArray.toArray(new LinkedList[0]);
//
//        initZone.initialize(enabledArray);
//        prjStateTimed initState = new prjStateTimed(curLocalStateArray, initZone);
////        insertState(curLocalStateArray, stateCache, zoneCache, initZone, prjStateCache);
//
//        int[] prjState = toArray(curLocalStateArray, initZone);
//        Mdd mddMgr = new Mdd(prjState.length);
//        prj.mddMgr = mddMgr;
//        System.out.printf("prjState=%s\n", Arrays.toString(prjState));
////        mddMgr.add(reach, prjState);
//
//
//
//        Zone[] zoneStack = new Zone[INITIAL_STACK_SIZE];
//        zoneStack[0] = initZone;
//        LPNTran[] firedStack = new LPNTran[INITIAL_STACK_SIZE];
//        prjStateTimed[] stateStack = new prjStateTimed[INITIAL_STACK_SIZE];
//        stateStack[0] = initState;
//        int ptr_stateStack = 1;
//        LinkedList[][] lpnTranStack = new LinkedList[INITIAL_STACK_SIZE][];
//        lpnTranStack[0] = enArray;
//        int[] curIndexStack = new int[INITIAL_STACK_SIZE];
//        int ptr_curIndexStack = 1;
//        graph.addInitNode(0 + "");
//        int tranFiringCnt = 0;
//        // </editor-fold>
//        while ((ptr_stateStack == 0) == false) {
//
////            if (prj.prjStateSet.size() > 1000000) {
////                break;
////            }
//            if (ptr_stateStack > prj.max_stack_depth) {
//                prj.max_stack_depth = ptr_stateStack;
//            }
//
//            int curIndex = curIndexStack[--ptr_curIndexStack];
//            LinkedList[] curEnabledArray = lpnTranStack[ptr_stateStack - 1];
//            LinkedList<LPNTran> curEnabledSet = curEnabledArray[curIndex];
//
//            int arraySize = curEnabledArray.length;
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                curIndex++;
//                if (curIndex == arraySize) {
//                    if (stateStack.length == ptr_stateStack) {
//                        ptr_stateStack--;
//                    } else {
//                        freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                        ptr_stateStack--;
//                    }
//                    continue;
//                }
//            }
//
//            curIndexStack[ptr_curIndexStack++] = curIndex;
//            prjStateTimed curPrjState = stateStack[ptr_stateStack - 1];
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                continue;
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - select a time enabled tran
//// <editor-fold defaultstate="collapsed" desc="select a time enabled tran">
//            LPNTran firedTran = null;
//            int idx = 0;
//            for (LPNTran t : curEnabledSet) {
//                if (curPrjState.getZone().isTimeEnabled(t.getDelayLB(), t.getID())) {
//                    firedTran = t;
//                    break;
//                }
//                idx++;
//            }
//            if (firedTran == null) {
//                freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                ptr_stateStack--;
//                continue;
//            }// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            State[] nextStateArray = firedTran.fire(lpnList, curPrjState.toStateArray(), curIndex);
//            tranFiringCnt++;
//
//            // Add nextPrjState into prjStateSet
//            // If nextPrjState has been traversed before, skip to the next
//            // enabled transition.
//            Zone zone = curPrjState.getZone().clone();
//            prjStateTimed nextPrjState = new prjStateTimed(nextStateArray, zone);
//
////            boolean deadlock = true, disabled = false;
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//            if (curEnabledList.length != arraySize) {
//                curEnabledList = new LinkedList[arraySize];
//            }
//            nextEnabledList = new LinkedList[arraySize];
//            for (int i = 0; i < arraySize; i++) {
//                final LPN lpn_tmp = lpnList[i];
//                curEnabledList[i] = (lpn_tmp.getEnabledLpnTrans(
//                        curPrjState.toStateArray()[i], null, null, false));
//                nextEnabledList[i] = lpn_tmp.getEnabledLpnTrans(
//                        nextPrjState.toStateArray()[i], null, null, false);
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            if (PRINT_ENABLED) {
//                oldZone = zone.toString();
//            }
//            if (zone instanceof FinalizedCanonicalZone) {
//                System.err.println("ERROR: Using FinalizedCanonicalZone");
//            }
//
//            update(zone, firedTran, nextEnabledList, curEnabledList);
//            if (PRINT_ENABLED) {
//                newZone = zone.toString();
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - project allowable disables
//// <editor-fold defaultstate="collapsed" desc="project allowable disables">
//            //differenceSet = currentEnabledTransitions - nextEnabledTransitions
//            //remove un-needed timers from zone
//            //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
//            LPNTranSet diffEnabledTransitions = new LPNTranSet();
//            for (LinkedList<LPNTran> tList : curEnabledArray) {
//                diffEnabledTransitions.addAll(tList);
//            }
//            for (LinkedList<LPNTran> tList : nextEnabledList) {
//                diffEnabledTransitions.removeAll(tList);
//            }
//            for (LPNTran t : diffEnabledTransitions) {
//                if (firedTran == t) {
//                    continue;
//                }
//                zone.project(t.getID());
//            }// </editor-fold>
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
////            zone = (Zone) zoneCache.tryInsert(zone);
//            zone.ID = zoneCache.getID(zone);
//
//            nextPrjState.setZone(zone);
//            prjState = toArray(nextStateArray, zone);
//            boolean isNew =
//                    !mddMgr.contains(prj.reach, prjState);
//            if (isNew) {
////                Zone.NEXT_ID++;
//                zoneCache.addID(zone);
//                mddMgr.add(prj.reach, prjState);
//            }
//
//
//// <editor-fold defaultstate="collapsed" desc="check for deadlocks and disables">
//            if (PRINT_ENABLED) {
//                nextEnabledListSize = 0;
//            }
//            for (int i = 0; i < arraySize; i++) {
//                if (PRINT_ENABLED) {
//                    nextEnabledListSize += nextEnabledList[i].size();
//                }
//                LPNTran disabledTran = LPNTran.disablingError(curEnabledArray[i],
//                        nextEnabledList[i], firedTran);
//                if (disabledTran != null) {
//                    System.out.println("StateGraph:\t" + disabledTran.getLabelString()
//                            + " by " + firedTran.getLabelString()
//                            + ":\tVerification failed: disabling error.");
//                    disabled = true;
//                }
//                if (nextEnabledList[i] != null && arraySize > 0) {
//                    deadlock = false;
//                }
//            }
//            if (disabled) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            } else if (deadlock) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            } else {
//            }                // </editor-fold>
//
//            curEnabledSet.remove(idx);//must remove the current tran after using it
//// <editor-fold defaultstate="collapsed" desc="print and graph stuff">
//            if (PRINT_ENABLED) {
//                if (nextEnabledListSize == 1) {
//                    hasSingleTrans++;
//                } else {
//                    notSingleTrans++;
//                }
//            }
//            if (PRINT_ENABLED) {
//                printNoDeadlock(firedTran, Arrays.asList(nextEnabledList),
//                        prj.prjStateSet, curPrjState, nextPrjState, oldZone, newZone);
//                graph.addEdge(prj.prjStateSet.tryInsert(curPrjState),
//                        prj.prjStateSet.tryInsert(nextPrjState), firedTran.getLabelString());
//                addStateToGraph(disabled, graph, nextPrjState, deadlock, prj.prjStateSet);
//            }
////          // </editor-fold>
//            if (isNew == false) {
//                continue;
//            }
//            //range check
//            if (ptr_stateStack >= stateStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                zoneStack = Arrays.copyOf(zoneStack, newSize);
//                firedStack = Arrays.copyOf(firedStack, newSize);
//                stateStack = Arrays.copyOf(stateStack, newSize);
//                lpnTranStack = Arrays.copyOf(lpnTranStack, newSize);
//            }
//            zoneStack[ptr_stateStack] = zone;
//            firedStack[ptr_stateStack] = firedTran;
//            stateStack[ptr_stateStack] = nextPrjState;
//            lpnTranStack[ptr_stateStack++] = nextEnabledList;
//
//            if (ptr_curIndexStack >= curIndexStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                curIndexStack = Arrays.copyOf(curIndexStack, newSize);
//            }
//            curIndexStack[ptr_curIndexStack++] = 0;
//
//        }
//
//
//
//        START_TIME = System.currentTimeMillis() - START_TIME;
//
//        garbage();
//        System.gc();
//
//        printer.put("TIME", formatTime(START_TIME));
//        printer.put("VERSION", version + "");
//        printer.put("      ", "");
//        printer.put("USED memory", "" + LONG.format(
//                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//        printer.put("STATE CACHE SIZE", stateCache.size() + "");
//        printer.put("ZONE CACHE SIZE", zoneCache.size() + "");
//        printer.put("PRJ STATE CACHE SIZE", prjStateCache.size() + "");
//        printer.put("bytes/state", "" + LONG.format(
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
//                / (double) prjStateCache.size()));
//        printer.put(" ", "");
//        printer.put("STATES", "" + prjStateCache.size());
//        printer.put("TRANSITIONS", "" + tranFiringCnt);
//        printer.print();
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prjStateCache.size()
//                + ", max_stack_depth: " + prj.max_stack_depth);
//
//
//    }
//
//    static void findsg_mdd_timed_ver_2(LPN[] lpnList, State[] curLocalStateArray,
//            ArrayList<LinkedList<LPNTran>> enabledArray,
//            final Project prj) {
//        //array ={s1..sn, z}    nw Mdd(n+1)
//        //versus
//        //array = {t1...tn} //timed state  new Mdd(n)
//        //mdd.contains(node,int[])
//
//// <editor-fold defaultstate="collapsed" desc="init">
//        int hasSingleTrans = 0;
//        int notSingleTrans = 0;
//        int nextEnabledListSize = 0;
//        String oldZone = null, newZone = null;
//
//        IdentityCache zoneCache = (IdentityCache) Cache.getNewIdentityCache(1000);
//        Cache stateCache = Cache.getNewCache(1000);
//
//        prj.mode = Mode.TIMED_ITERATIVE;
//        Zone initZone = Zone.newMatrixZoneVer3();
//        final int INITIAL_STACK_SIZE = 1000;
//        LinkedList[] enArray = enabledArray.toArray(new LinkedList[0]);
//
//        initZone.initialize(enabledArray);
//        prjStateTimed initState = new prjStateTimed(curLocalStateArray, initZone);
////        insertState(curLocalStateArray, stateCache, zoneCache, initZone, prjStateCache);
//
//        int[] prjState = toArray(curLocalStateArray, initZone, enArray, zoneCache);
//        Mdd mddMgr = new Mdd(prjState.length);
//        prj.mddMgr = mddMgr;
//        System.out.printf("prjState=%s\n", Arrays.toString(prjState));
////        mddMgr.add(reach, prjState);
//
//
//
//        Zone[] zoneStack = new Zone[INITIAL_STACK_SIZE];
//        zoneStack[0] = initZone;
//        LPNTran[] firedStack = new LPNTran[INITIAL_STACK_SIZE];
//        prjStateTimed[] stateStack = new prjStateTimed[INITIAL_STACK_SIZE];
//        stateStack[0] = initState;
//        int ptr_stateStack = 1;
//        LinkedList[][] lpnTranStack = new LinkedList[INITIAL_STACK_SIZE][];
//        lpnTranStack[0] = enArray;
//        int[] curIndexStack = new int[INITIAL_STACK_SIZE];
//        int ptr_curIndexStack = 1;
//        graph.addInitNode(0 + "");
//        int tranFiringCnt = 0;
//        // </editor-fold>
//        while ((ptr_stateStack == 0) == false) {
//
////            if (prj.prjStateSet.size() > 1000000) {
////                break;
////            }
//            if (ptr_stateStack > prj.max_stack_depth) {
//                prj.max_stack_depth = ptr_stateStack;
//            }
//
//            int curIndex = curIndexStack[--ptr_curIndexStack];
//            LinkedList[] curEnabledArray = lpnTranStack[ptr_stateStack - 1];
//            LinkedList<LPNTran> curEnabledSet = curEnabledArray[curIndex];
//
//            int arraySize = curEnabledArray.length;
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                curIndex++;
//                if (curIndex == arraySize) {
//                    if (stateStack.length == ptr_stateStack) {
//                        ptr_stateStack--;
//                    } else {
//                        freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                        ptr_stateStack--;
//                    }
//                    continue;
//                }
//            }
//
//            curIndexStack[ptr_curIndexStack++] = curIndex;
//            prjStateTimed curPrjState = stateStack[ptr_stateStack - 1];
//
//            if (curEnabledSet == null || curEnabledSet.size() == 0) {
//                continue;
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - select a time enabled tran
//// <editor-fold defaultstate="collapsed" desc="select a time enabled tran">
//            LPNTran firedTran = null;
//            int idx = 0;
//            for (LPNTran t : curEnabledSet) {
//                if (curPrjState.getZone().isTimeEnabled(t.getDelayLB(), t.getID())) {
//                    firedTran = t;
//                    break;
//                }
//                idx++;
//            }
//            if (firedTran == null) {
//                freeStackElements(zoneStack, ptr_stateStack, firedStack, stateStack, lpnTranStack);
//                ptr_stateStack--;
//                continue;
//            }// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            State[] nextStateArray = firedTran.fire(lpnList, curPrjState.toStateArray(), curIndex);
//            tranFiringCnt++;
//
//            // Add nextPrjState into prjStateSet
//            // If nextPrjState has been traversed before, skip to the next
//            // enabled transition.
//            Zone zone = curPrjState.getZone().clone();
//            prjStateTimed nextPrjState = new prjStateTimed(nextStateArray, zone);
//
////            boolean deadlock = true, disabled = false;
//// - - - - - - - - - - - - - - - - - - - - - - - - - - determine next and current set
//            if (curEnabledList.length != arraySize) {
//                curEnabledList = new LinkedList[arraySize];
//            }
//            nextEnabledList = new LinkedList[arraySize];
//            for (int i = 0; i < arraySize; i++) {
//                final LPN lpn_tmp = lpnList[i];
//                curEnabledList[i] = (lpn_tmp.getEnabledLpnTrans(
//                        curPrjState.toStateArray()[i], null, null, false));
//                nextEnabledList[i] = lpn_tmp.getEnabledLpnTrans(
//                        nextStateArray[i], null, null, false);
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
//            if (PRINT_ENABLED) {
//                oldZone = zone.toString();
//            }
//            update(zone, firedTran, nextEnabledList, curEnabledList);
//            if (PRINT_ENABLED) {
//                newZone = zone.toString();
//            }
//// - - - - - - - - - - - - - - - - - - - - - - - - - - project allowable disables
//// <editor-fold defaultstate="collapsed" desc="project allowable disables">
//            //differenceSet = currentEnabledTransitions - nextEnabledTransitions
//            //remove un-needed timers from zone
//            //essentially: nextZone = currentZone  - (currentEnabledTransitions - nextEnabledTransitions)
//            LPNTranSet diffEnabledTransitions = new LPNTranSet();
//            for (LinkedList<LPNTran> tList : curEnabledArray) {
//                diffEnabledTransitions.addAll(tList);
//            }
//            for (LinkedList<LPNTran> tList : nextEnabledList) {
//                diffEnabledTransitions.removeAll(tList);
//            }
//            for (LPNTran t : diffEnabledTransitions) {
//                if (firedTran == t) {
//                    continue;
//                }
//                zone.project(t.getID());
//            }// </editor-fold>
//// - - - - - - - - - - - - - - - - - - - - - - - - - -
////            zone = (Zone) zoneCache.tryInsert(zone);
////            zone.ID = zoneCache.getID(zone);
//
//            nextPrjState.setZone(zone);
//            prjState = toArray(nextStateArray, zone, nextEnabledList, zoneCache);
//            boolean isNew = false == mddMgr.contains(prj.reach, prjState);
//            if (isNew) {
////                Zone.NEXT_ID++;
////                zoneCache.addID(zone);
//                mddMgr.add(prj.reach, prjState);
//            }
//
//
//// <editor-fold defaultstate="collapsed" desc="check for deadlocks and disables">
//            if (PRINT_ENABLED) {
//                nextEnabledListSize = 0;
//            }
//            for (int i = 0; i < arraySize; i++) {
//                if (PRINT_ENABLED) {
//                    nextEnabledListSize += nextEnabledList[i].size();
//                }
//                LPNTran disabledTran = LPNTran.disablingError(curEnabledArray[i],
//                        nextEnabledList[i], firedTran);
//                if (disabledTran != null) {
//                    System.out.println("StateGraph:\t" + disabledTran.getLabelString()
//                            + " by " + firedTran.getLabelString()
//                            + ":\tVerification failed: disabling error.");
//                    disabled = true;
//                }
//                if (nextEnabledList[i] != null && arraySize > 0) {
//                    deadlock = false;
//                }
//            }
//            if (disabled) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            } else if (deadlock) {
//                trace(ptr_stateStack, stateStack, firedStack, zoneStack);
//                return;
//            } else {
//            }                // </editor-fold>
//
//            curEnabledSet.remove(idx);//must remove the current tran after using it
//// <editor-fold defaultstate="collapsed" desc="print and graph stuff">
//            if (PRINT_ENABLED) {
//                if (nextEnabledListSize == 1) {
//                    hasSingleTrans++;
//                } else {
//                    notSingleTrans++;
//                }
//            }
//            if (PRINT_ENABLED) {
//                printNoDeadlock(firedTran, Arrays.asList(nextEnabledList),
//                        prj.prjStateSet, curPrjState, nextPrjState, oldZone, newZone);
//                graph.addEdge(prj.prjStateSet.tryInsert(curPrjState),
//                        prj.prjStateSet.tryInsert(nextPrjState), firedTran.getLabelString());
//                addStateToGraph(disabled, graph, nextPrjState, deadlock, prj.prjStateSet);
//            }
////          // </editor-fold>
//            if (isNew == false) {
//                continue;
//            }
//            //range check
//            if (ptr_stateStack >= stateStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                zoneStack = Arrays.copyOf(zoneStack, newSize);
//                firedStack = Arrays.copyOf(firedStack, newSize);
//                stateStack = Arrays.copyOf(stateStack, newSize);
//                lpnTranStack = Arrays.copyOf(lpnTranStack, newSize);
//            }
//            zoneStack[ptr_stateStack] = zone;
//            firedStack[ptr_stateStack] = firedTran;
//            stateStack[ptr_stateStack] = nextPrjState;
//            lpnTranStack[ptr_stateStack++] = nextEnabledList;
//
//            if (ptr_curIndexStack >= curIndexStack.length) {
//                int newSize = ptr_stateStack + ARRAY_INCREMENT;
//                curIndexStack = Arrays.copyOf(curIndexStack, newSize);
//            }
//            curIndexStack[ptr_curIndexStack++] = 0;
//
//        }
//
//
//
//        START_TIME = System.currentTimeMillis() - START_TIME;
//
//        garbage();
//        System.gc();
//
//        printer.put("TIME", formatTime(START_TIME));
//        printer.put("VERSION", version + "");
//        printer.put("      ", "");
//        printer.put("USED memory", "" + LONG.format(
//                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
//        printer.put("STATE CACHE SIZE", stateCache.size() + "");
//        printer.put("ZONE CACHE SIZE", zoneCache.size() + "");
//        printer.put("PRJ STATE CACHE SIZE", prjStateCache.size() + "");
//        printer.put("bytes/state", "" + LONG.format(
//                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
//                / (double) prjStateCache.size()));
//        printer.put(" ", "");
//        printer.put("STATES", "" + prjStateCache.size());
//        printer.put("TRANSITIONS", "" + tranFiringCnt);
//        printer.print();
//        System.out.println("SUMMARY: # LPN transition firings: " + tranFiringCnt
//                + ", # of prjStates found: " + prjStateCache.size()
//                + ", max_stack_depth: " + prj.max_stack_depth);
//
//
//    }
//
//    static private void trace(int ptr_lpnTranStack, prjStateTimed[] stateStack,
//            LPNTran[] firedStack, Zone[] zoneStack) {
//        //                    System.out.print(": TRACE: "+Arrays.deepToString(firedStack));
//        for (int i = 0; i < ptr_lpnTranStack; i++) {
////            System.out.println(i + ": TRACE: " + Arrays.asList(stateStack[i]));
//            String st = "";
//            for (State state : stateStack[i].stateArray) {
//                state.print();
//            }
//
//            if (firedStack[i] != null) {
//                System.out.println(i + ": TRACE: " + firedStack[i].getLabelString());
//            }
//            if (zoneStack[i] != null) {
//                System.out.println(zoneStack[i]);
//                //                    System.out.println("\t"+curIndexStack[i]);
//            }
//        }
//    }
//
//    static private boolean testFailure(int arraySize, final LPN[] lpnList, State[] nextStateArray,
//            ArrayList<LinkedList<LPNTran>> nextEnabledList,
//            ArrayList<LinkedList<LPNTran>> curEnabledArray,
//            LPNTran firedTran, IDGenerator idGen,
//            prjState curPrjState, prjState nextPrjState) throws InterruptedException {
//        boolean deadlock = true;
//        for (int i = 0; i < arraySize; i++) {
//            if (curPrjState.get(i) != nextStateArray[i]) {
//                LPN lpn_tmp = lpnList[i];
//                LinkedList<LPNTran> enabledList = lpn_tmp.getEnabledLpnTrans(nextStateArray[i], null, null, false);//firedTran, curEnabledArray.get(i), false);
//                nextEnabledList.add(i, enabledList);
////					if(enabledList != null)
////						System.out.println(lpn_tmp.getLabel() + ": << enabled size: " + enabledList.size());
////					else
////						System.out.println(lpn_tmp.getLabel() + ": << enabled size: null");
//            } else {
//                LinkedList<LPNTran> tmp = null;
//                if (curEnabledArray.get(i) != null) {
//                    tmp = (LinkedList<LPNTran>) (curEnabledArray.get(i)).clone();
//                }
//                nextEnabledList.add(i, tmp);
////					if(tmp != null)
////						System.out.println(lpnList[i].getLabel() + ": == enabled size: " + tmp.size());
////					else
////						System.out.println(lpnList[i].getLabel() + ": == enabled size: null");
//            }
//            LPNTran disabledTran = LPNTran.disablingError(curEnabledArray.get(i), nextEnabledList.get(i), firedTran);
//            if (disabledTran != null) {
//                System.out.println("StateGraph:\t" + disabledTran.getLabelString()
//                        + " by " + firedTran.getLabelString()
//                        + ":\tVerification failed: disabling error.");
//                return true;
//            }
//            if (nextEnabledList.get(i) != null && nextEnabledList.get(i).size() > 0) {
//                deadlock = false;
//            }
//        }
//        printIfDeadlock(deadlock, firedTran, nextEnabledList, idGen, curPrjState, nextPrjState);
//        return deadlock;
//    }
//
//    static private void printIfDeadlock(boolean deadlock, LPNTran firedTran,
//            ArrayList<LinkedList<LPNTran>> nextEnabledList, IDGenerator idGen,
//            prjState curPrjState, prjState nextPrjState) throws InterruptedException {
//        if (deadlock) {
//            Thread.sleep(15);
//            System.err.println("\t " + String.format("%15s", firedTran.getLabelString()) + "\t" + String.format("%20s", nextEnabledList.toString().substring(1, nextEnabledList.toString().length() - 1)));
//            System.err.println("State:  " + idGen.tryInsert(curPrjState) + " ----------------> ( " + idGen.tryInsert(nextPrjState) + " )  ---------------->  DEADLOCK");
//            Thread.sleep(10);
//        } else {
////            System.out.println("\t " + String.format("%15s", firedTran.getLabelString()) + "\t" + String.format("%20s", nextEnabledList.toString().substring(1, nextEnabledList.toString().length() - 1)));
////            System.out.println("State:  " + idGen.tryInsert(curPrjState) + " ----------------> ( " + idGen.tryInsert(nextPrjState) + " )  ---------------->  ???");
//        }
//    }
//
//    static private void printNoDeadlock(LPNTran firedTran, Collection<LinkedList> nextTransitionListArray,
//            IDGenerator idGen, prjState curPrjState, prjState nextPrjState,
//            String oldZone, String newZone) {
//        System.out.println(" " + String.format("%15s", firedTran.getLabelString()) + "\t"
//                + String.format("%20s", nextTransitionListArray.toString().substring(1, nextTransitionListArray.toString().length() - 1)));
//        System.out.println("State:  " + idGen.tryInsert(curPrjState) + " ----------------> ( " + idGen.tryInsert(nextPrjState) + " )  ---------------->  ???");
//        System.out.println("State:  " + (curPrjState).hashCode() + " ----------------> ( " + (nextPrjState).hashCode() + " )  ---------------->  ???");
//        System.out.println(curPrjState);
//        if (oldZone.length() + newZone.length() > 0) {
//            System.out.println(Main.mergeColumns(oldZone, newZone, 29, 30));
//        }
//    }
//
//    static private void printNoDeadlock(LPNTran firedTran, Object[] nextTransitionListArray,
//            IDGenerator idGen, prjState curPrjState, prjState nextPrjState,
//            String oldZone, String newZone) {
//        System.out.println(" " + String.format("%15s", firedTran.getLabelString()) + "\t"
//                + String.format("%20s", Arrays.deepToString(nextTransitionListArray)));
//        System.out.println("State:  " + idGen.tryInsert(curPrjState) + " ----------------> ( " + idGen.tryInsert(nextPrjState) + " )  ---------------->  ???");
//        System.out.println("State:  " + (curPrjState).hashCode() + " ----------------> ( " + (nextPrjState).hashCode() + " )  ---------------->  ???");
//        // System.out.println(curPrjState);
//        if (oldZone.length() + newZone.length() > 0) {
//            System.out.println(Main.mergeColumns(oldZone, newZone, 29, 30));
//        }
//    }
//
//    static private void printDeadlock(LPNTran firedTran, Collection<LinkedList<LPNTran>> nextTransitionListArray,
//            IDGenerator idGen, prjState curPrjState, prjState nextPrjState,
//            String oldZone, String newZone) {
//
//        System.err.println(" " + String.format("%15s", firedTran.getLabelString()) + "\t"
//                + String.format("%20s", nextTransitionListArray.toString().substring(1, nextTransitionListArray.toString().length() - 1)));
//        System.err.println("State:  " + idGen.tryInsert(curPrjState) + " ----------------> ( " + idGen.tryInsert(nextPrjState) + " )  ---------------->  DEADLOCK");
//        System.err.println(curPrjState);
//        if (oldZone.length() + newZone.length() > 0) {
//            System.err.println(Main.mergeColumns(oldZone, newZone, 29, 30));
//        }
//
//    }
//
//    static private void printDisable(LPNTran firedTran, ArrayList<LinkedList<LPNTran>> nextTransitionListArray,
//            IDGenerator idGen, prjState curPrjState, prjState nextPrjState,
//            String oldZone, String newZone) {
//
//        System.err.println(" " + String.format("%15s", firedTran.getLabelString()) + "\t"
//                + String.format("%20s", nextTransitionListArray.toString().substring(1, nextTransitionListArray.toString().length() - 1)));
//        System.err.println("State:  " + idGen.tryInsert(curPrjState) + " ----------------> ( " + idGen.tryInsert(nextPrjState) + " )  ---------------->  DISABLE");
//        System.err.println(curPrjState);
//        if (oldZone.length() + newZone.length() > 0) {
//            System.err.println(Main.mergeColumns(oldZone, newZone, 29, 30));
//        }
//
//    }
//
//    static private void addStateToGraph(boolean disabled, DOTGraph graph,
//            prjStateTimed nextPrjState, boolean deadlock, IDGenerator prjStateSet) {
//        if (disabled) {
//            graph.addDisableNode("" + prjStateSet.tryInsert(nextPrjState));
//        } else if (deadlock) {
//            graph.addDeadlockNode("" + prjStateSet.tryInsert(nextPrjState));
//        } else {
//            graph.addNode("" + prjStateSet.tryInsert(nextPrjState));
//        }
//    }
//
//    private static void freeStackElements(Zone[] zoneStack, int ptr_stateStack, LPNTran[] firedStack, prjStateTimed[] stateStack, LinkedList[][] lpnTranStack) {
//        zoneStack[ptr_stateStack] = null;
//        firedStack[ptr_stateStack] = null;
//        stateStack[ptr_stateStack] = null;
//        lpnTranStack[ptr_stateStack] = null;
//    }
//
//    public static byte[] int2byte(int[] src) {
//        int srcLength = src.length;
//        byte[] dst = new byte[srcLength << 2];
//
//        for (int i = 0; i < srcLength; i++) {
//            int x = src[i];
//            int j = i << 2;
//            dst[j++] = (byte) ((x) & 0xff);
//            dst[j++] = (byte) ((x >>> 8) & 0xff);
//            dst[j++] = (byte) ((x >>> 16) & 0xff);
//            dst[j++] = (byte) ((x >>> 24) & 0xff);
//        }
//        return dst;
//    }
//    final static int[] ZERO_INT_ARRAY = {~1};
//    static final long[] FLAG_BITS = new long[64];
//
//    static {
//        for (int i = 0; i < 64; i++) {
//            FLAG_BITS[i] = (1l << i);
//        }
////        System.out.println(Arrays.toString(FLAG_BITS));
//    }
//
//    static int differenceOfVectors(int[] vector, int stateIdx) {
//        int diff = 0;
//        for (int i = 0; i < vector.length; i++) {
//            if (initStateArray[stateIdx].getVector()[i] != vector[i]) {
//                diff |= FLAG_BITS[i];
//            }
//        }
//        return diff;
//    }
//
//    private static boolean insertState(
//            State[] curLocalStateArray, Cache stateCache,
//            DCache zoneCache, Zone zone,
//            Cache prjStateCache) {
//        ArrayList prjState = new ArrayList(curLocalStateArray.length + 1);
//        for (State st : curLocalStateArray) {
////            prjState.add(stateCache.tryInsert(st.getVector()));
////            prjState.add(stateCache.tryInsert(st.getMarking()));
//            prjState.add(stateCache.tryInsert(st));
//        }
//        prjState.add(zone);
//        return prjStateCache.add(prjState);
//    }
//
//    private static boolean insertState(
//            State[] curLocalStateArray, Cache stateCache,
//            Cache zoneCache, Zone zone,
//            Cache prjStateCache) {
//        ArrayList prjState = new ArrayList(curLocalStateArray.length + 1);
//        for (State st : curLocalStateArray) {
////            prjState.add(stateCache.tryInsert(st.getVector()));
////            prjState.add(stateCache.tryInsert(st.getMarking()));
//            prjState.add(stateCache.tryInsert(st));
//        }
//        if (zone instanceof MatrixZoneVer3Impl) {
//            final MatrixZoneVer3Impl z = (MatrixZoneVer3Impl) zone;
//            prjState.add(zoneCache.tryInsert((z.canonicalDBM())));
//        } else {
//            prjState.add(zoneCache.tryInsert(zone));
//        }
//        boolean ret = prjStateCache.add(prjState);
////        if(ret)
////        System.out.printf("prjStateCache size=%s\t\t%s\n",
////                prjStateCache.size(), Arrays.toString(curLocalStateArray));
//
//        return ret;
//    }
//
//    private static boolean insertState2(
//            State[] curLocalStateArray, Cache stateCache,
//            Cache zoneCache, Zone zone,
//            Cache prjStateCache) {
//        ArrayList prjState = new ArrayList(curLocalStateArray.length + 1);
//        for (State st : curLocalStateArray) {
////            prjState.add(stateCache.tryInsert(st.getVector()));
////            prjState.add(stateCache.tryInsert(st.getMarking()));
//            prjState.add(stateCache.tryInsert(st));
//        }
//        prjState.add(zoneCache.tryInsert((zone)));
//        return prjStateCache.add(prjState);
//    }
//
//    final private static boolean insertState(
//            State[] curLocalStateArray, Cache stateCache,
//            Cache prjStateCache) {
////        int i=0;
//        ArrayList prjState = new ArrayList(curLocalStateArray.length);
//        for (State st : curLocalStateArray) {
////            prjState.add(stateCache.tryInsert(st.getVector()));
////            prjState.add(stateCache.tryInsert(st.getMarking()));
//            prjState.add(stateCache.tryInsert(st));
//        }
//        return prjStateCache.add(prjState);
//    }
//
//    /**
//     * Try storing the difference of 2 state vectors to save memory.
//     * @param curLocalStateArray
//     * @param stateCache
//     * @param prjStateCache
//     * @return
//     */
//    final private static boolean insertState2(
//            State[] curLocalStateArray, Cache stateCache,
//            Cache prjStateCache) {
//        int i = 0;
////        int[] prj=new int[curLocalStateArray.length * 2 + 1];
//
////        IntArrayList prjState = new IntArrayList(curLocalStateArray.length);
//        LongArrayList prjState = new LongArrayList(curLocalStateArray.length);
////        ArrayList prjState = new ArrayList(curLocalStateArray.length);
//        for (int j = 0; i < curLocalStateArray.length; i++) {
//            long vDiff = differenceOfVectors(curLocalStateArray[i].getVector(), i);
////            prjState.add(vDiff);
////            prjState.add(Arrays.hashCode(curLocalStateArray[i].getMarking()));
//
//
////            prj[j++]=vDiff;
////            prj[j++]=Arrays.hashCode(curLocalStateArray[i].getMarking());
//            vDiff <<= 32;
////            int m = 0;
////            final int[] M = curLocalStateArray[i].getMarking();
////            for (j = 0; j < M.length; j++) {
////                m |= (1 << M[j])-1;
////            }
////            vDiff |= m;
//            vDiff |= Arrays.hashCode(curLocalStateArray[i].getMarking());
//            prjState.add(vDiff);
////            prjState.add(stateCache.tryInsert(curLocalStateArray[i].getVector()));
////            prjState.add(stateCache.tryInsert(curLocalStateArray[i].getMarking()));
////            prjState.add(stateCache.tryInsert(curLocalStateArray[i]));
//
//        }
////        prjState.trimToSize();
//        return prjStateCache.add(prjState);
//    }
//
//    public static void main(String[] arg) {
//
//        ArrayList a = new ArrayList(2);
//        ArrayList b = new ArrayList(2);
//        ArrayList c = new ArrayList(2);
//        ArrayList d = new ArrayList(2);
//        a.add(1);
//        b.add(1);
//        a.add("v");
//        b.add("v");
//        c.add(3);
//        c.add("v");
//        d.add("1");
//        d.add("v");
//        Cache set = Cache.getNewCache(4);
//        set.add(a);
//        set.add(b);
//        set.add(c);
//        set.add(d);
//        System.out.println("set=" + set);
//        System.out.println("set=" + set.size());
//    }
//
//    public static final int[] toArray(State[] stateArray, Zone zone) {
//        int[] idxArray = new int[stateArray.length + 1];
//        for (int i = 0; i < stateArray.length; i++) {
//            idxArray[i] = stateArray[i].getLabel();
//        }
//        idxArray[stateArray.length] = zone.getID();
//        return idxArray;
//    }
//
//    public static final int[] toArray(State[] stateArray) {
//        int[] idxArray = new int[stateArray.length];
//        for (int i = 0; i < stateArray.length; i++) {
//            idxArray[i] = stateArray[i].getLabel();
//        }
//        return idxArray;
//    }
//
//    private static int[] toArray(State[] stateArray, Zone zone, LinkedList[] nextEnabledList, IdentityCache cache) {
//        Zone[] zones = Zone.split((MatrixZoneVer3Impl) zone, nextEnabledList);
//        int[] idxArray = new int[stateArray.length + zones.length];
//        for (int i = 0; i < stateArray.length; i++) {
//            idxArray[i] = stateArray[i].getLabel();
//        }
//        for (int i = stateArray.length, j = 0; i < idxArray.length; i++, j++) {
//            zones[j].ID = cache.addID(zones[j]);
//            idxArray[i] = zones[j].getID();
//        }
//        return idxArray;
//    }
//
//    static String formatTime(long timeMS) {
//        long timeSec = (long) (timeMS / 1000f);
//        long h = timeSec / 3600, m = (timeSec % 3600) / 60;
//        double s = (timeMS / 1000f) - h * 3600 - m * 60;
//        String timeStr = String.format("%d:%02d:%2s", h, m, df1.format(s));
//        return timeStr;
//    }
//}
