package verification.platu.logicAnalysis;

import java.util.HashSet;

import verification.platu.project.PrjState;

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
}
