package verification.platu.logicAnalysis;

import verification.platu.project.PrjState;

public interface StateSetInterface extends Iterable<PrjState> {
	
	public boolean contains(PrjState state);
	
	public boolean add(PrjState state);
	
	public int size();

}
