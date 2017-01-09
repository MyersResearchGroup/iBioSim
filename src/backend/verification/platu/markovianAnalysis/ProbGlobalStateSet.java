package backend.verification.platu.markovianAnalysis;

import java.util.HashMap;
import java.util.Iterator;

import backend.verification.platu.logicAnalysis.StateSetInterface;
import backend.verification.platu.project.PrjState;

public class ProbGlobalStateSet extends HashMap<PrjState, PrjState> implements StateSetInterface, Runnable {//extends HashSet<PrjState>{

	private static final long serialVersionUID = 1L;

	private PrjState initState;

	public ProbGlobalStateSet() {
		super();
	}

	/*
	 * Get the initial state.
	 */
	public PrjState getInitialState() {
		return initState;
	}

	/* 
     * Set the initial state.
     * @param initState
     * 		The initial state.
     */
	public void setInitState(PrjState initPrjState) {
		this.initState = initPrjState;	
	}

	@Override
	public boolean contains(PrjState s) {
		return this.keySet().contains(s);
	}

	@Override
	public boolean add(PrjState state) {
		super.put(state, state);
		return false;
	}

	@Override
	public Iterator<PrjState> iterator() {
		return super.keySet().iterator();
	}

	@Override
	public void run() {	
	}
	
	
}
