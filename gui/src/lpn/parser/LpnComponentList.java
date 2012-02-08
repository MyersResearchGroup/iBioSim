package lpn.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LpnComponentList extends LhpnFile{
	private ArrayList<Component> compList; 
	private Integer maxNumProcInOneComp;
	
	public LpnComponentList(Integer maxNumProcInOneComp, HashMap<Integer, LpnProcess> processMap) {
		compList = new ArrayList<Component>(processMap.size());
		this.maxNumProcInOneComp = maxNumProcInOneComp;
	}

	public ArrayList<Component> buildComponents(HashMap<Integer, LpnProcess> processMap, String directory, String lpnFileName) {
		if (maxNumProcInOneComp == 1) {
			// Each process is a component
			for (Iterator<LpnProcess> processMapIter = processMap.values().iterator(); processMapIter.hasNext();) {
				LpnProcess curProc = processMapIter.next();
				Component curComp = new Component(curProc);
				compList.add(curComp);
			}
		}
		else if (maxNumProcInOneComp > 1 && maxNumProcInOneComp < processMap.keySet().size()) {
			// Find shared variables between any two processes. 
			// For each process, the input and output vars are shared by one or more processes. 
			HashMap<Variable, ArrayList<LpnProcess>> sharedVarMap = new HashMap<Variable, ArrayList<LpnProcess>>();
			Object[] allProcesses = processMap.values().toArray();
			for (int i=0; i<allProcesses.length; i++) {
				LpnProcess curProcess = (LpnProcess) allProcesses[i];
				for (int j=i+1; j<allProcesses.length; j++) {
					LpnProcess nextProcess = (LpnProcess) allProcesses[j];
					for (int k=0; k < curProcess.getProcessInput().size(); k++) {
						Variable curInput = curProcess.getProcessInput().get(k);
						if (nextProcess.getProcessOutput().contains(curInput)) {
							if (sharedVarMap.get(curInput) == null || sharedVarMap.get(curInput).isEmpty()) {
								ArrayList<LpnProcess> procArray = new ArrayList<LpnProcess>(2);
								procArray.add(curProcess);
								procArray.add(nextProcess);
								sharedVarMap.put(curInput, procArray);
							}
							else {
								if (!sharedVarMap.get(curInput).contains(curProcess)) 
									sharedVarMap.get(curInput).add(curProcess);
								if (!sharedVarMap.get(curInput).contains(nextProcess))
									sharedVarMap.get(curInput).add(nextProcess);
							}
						}
					}
					for (int k=0; k<curProcess.getProcessOutput().size(); k++) {
						Variable curOutput = curProcess.getProcessOutput().get(k);
						if (nextProcess.getProcessInput().contains(curOutput)) {
							if (sharedVarMap.get(curOutput) == null || sharedVarMap.get(curOutput).isEmpty()) {
								ArrayList<LpnProcess> procArray = new ArrayList<LpnProcess>(2);
								procArray.add(curProcess);
								procArray.add(nextProcess);
								sharedVarMap.put(curOutput, procArray);
							}
							else {
								if (!sharedVarMap.get(curOutput).contains(curProcess))
									sharedVarMap.get(curOutput).add(curProcess);
								if (!sharedVarMap.get(curOutput).contains(nextProcess))
									sharedVarMap.get(curOutput).add(nextProcess);
							}
						}
					}
				}
			}
			printSharedVarMap(sharedVarMap);
			LpnProcessGraph processGraph = new LpnProcessGraph(sharedVarMap, maxNumProcInOneComp);
			String graphFileName = lpnFileName + "_processGraph.dot";
			processGraph.outputDotFile(directory + separator + graphFileName);
			compList = processGraph.coalesceProcesses();
			
		}
		else { // maxNumProcInOneComp > processMap.keySet().size()
			System.out.println("The maximal number of processes in a component can only be " + processMap.keySet().size());
			return null;
		}
		return null;
		
	}

	private void printSharedVarMap(
			HashMap<Variable, ArrayList<LpnProcess>> sharedVarMap) {
		System.out.println("~~~~~~ shared variables map ~~~~~~~~~~~");
		for (Iterator<Variable> sharedVarMapIter = sharedVarMap.keySet().iterator(); sharedVarMapIter.hasNext();) {
			Variable curSharedVarMap = sharedVarMapIter.next();
			System.out.print(curSharedVarMap + "\t");
			ArrayList<LpnProcess> map = sharedVarMap.get(curSharedVarMap);
			for (int i=0; i<map.size(); i++) {
				System.out.print(map.get(i).getProcessId() + ", ");
			}
			System.out.print("\n");
		}
	}

}
