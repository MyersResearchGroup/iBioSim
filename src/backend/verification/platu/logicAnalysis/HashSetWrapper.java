package backend.verification.platu.logicAnalysis;

import java.util.HashSet;

import backend.verification.platu.project.PrjState;

public class HashSetWrapper extends HashSet<PrjState> implements StateSetInterface {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean contains(PrjState state) {
		return super.contains(state);
	}

	@Override
	public boolean add(PrjState state) {
		return super.add(state);
	}
	
	/**
	 * This method takes a PrjState instance otherPrjState, iterates through this hash set of PrjState instances, 
	 * and grabs the one from this set that "equals" to otherPrjState. If no match is found, this method returns null.
	 * @param otherPrjState
	 * @return
	 */
	public PrjState get(PrjState otherPrjState) {		
		for (PrjState prjSt : this) {
			if (this.contains(otherPrjState))
				return prjSt;
		}
		return null;
	}
}
