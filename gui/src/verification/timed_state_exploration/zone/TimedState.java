package verification.timed_state_exploration.zone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import lpn.parser.LhpnFile;
import lpn.parser.Transition;
import verification.platu.lpn.DualHashMap;
import verification.platu.lpn.VarSet;
import verification.platu.stategraph.State;
import verification.platu.stategraph.StateGraph;

/**
 * Adds timing to a State.
 * 
 * @author Andrew N. Fisher
 *
 */
public class TimedState extends State{
	
	/* A Zone for keeping track timing information. */
	private Zone _zone;

	private State _state;
	
	@Override
	public void setLpn(LhpnFile thisLpn) {
		_state.setLpn(thisLpn);
	}

	@Override
	public LhpnFile getLpn() {
		return _state.getLpn();
	}

	@Override
	public void setLabel(String lbl) {
		_state.setLabel(lbl);
	}

	@Override
	public String getLabel() {
		return _state.getLabel();
	}

	@Override
	public boolean[] getTranVector() {
		return _state.getTranVector();
	}

	@Override
	public void setIndex(int newIndex) {
		_state.setIndex(newIndex);
	}

	@Override
	public int getIndex() {
		return _state.getIndex();
	}

	@Override
	public boolean hasNonLocalEnabled() {
		return _state.hasNonLocalEnabled();
	}

	@Override
	public void hasNonLocalEnabled(boolean nonLocalEnabled) {
		_state.hasNonLocalEnabled(nonLocalEnabled);
	}

	@Override
	public boolean isFailure() {
		return _state.isFailure();
	}

	@Override
	public TimedState clone() {
		// TODO: Ensure that the new TimedState contains its own copy of the zone.
		return new TimedState(_state, _zone);
	}

	@Override
	public String print() {
		// TODO Auto-generated method stub
		return _state.print();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return _state.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Check for completion.
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		TimedState other = (TimedState) obj;
		
		if(!_state.equals(other._state))
			return false;
		
		return _zone.equals(other._zone);
	}

	@Override
	public void print(DualHashMap<String, Integer> VarIndexMap) {
		_state.print(VarIndexMap);
	}

	@Override
	public int[] getMarking() {
		return _state.getMarking();
	}

	@Override
	public void setMarking(int[] newMarking) {
		_state.setMarking(newMarking);
	}

	@Override
	public int[] getVector() {
		return _state.getVector();
	}

	@Override
	public HashMap<String, Integer> getOutVector(VarSet outputs,
			DualHashMap<String, Integer> VarIndexMap) {
		return _state.getOutVector(outputs, VarIndexMap);
	}

	@Override
	public State getLocalState() {
		return _state.getLocalState();
	}


	@Override
	public String getEnabledSetString() {
		return _state.getEnabledSetString();
	}

	@Override
	public State update(StateGraph SG, HashMap<String, Integer> newVector,
			DualHashMap<String, Integer> VarIndexMap) {
		return _state.update(SG, newVector, VarIndexMap);
	}

	@Override
	public State update(HashMap<String, Integer> newVector,
			DualHashMap<String, Integer> VarIndexMap, boolean[] newTranVector) {
		return _state.update(newVector, VarIndexMap, newTranVector);
	}

	@Override
	public File serialize(String filename) throws FileNotFoundException,
			IOException {
		return _state.serialize(filename);
	}

	@Override
	public boolean failure() {
		return _state.failure();
	}

	@Override
	public void setFailure() {
		_state.setFailure();
	}

	@Override
	public void print(LhpnFile lpn) {
		_state.print(lpn);
	}

	public TimedState(LhpnFile lpn, int[] new_marking, int[] new_vector,
			boolean[] new_isTranEnabled) {
		super(lpn, new_marking, new_vector, new_isTranEnabled);
		// TODO Find a way to remove the super call. 
		
		_state = new State(lpn, new_marking, new_vector, new_isTranEnabled);
		
		_zone = new Zone(new State(lpn, new_marking, new_vector, new_isTranEnabled));
	}

	/**
	 * Creates a timed state by adding an initial zone.
	 * @param other
	 * 			The current state.
	 */
	public TimedState(State other) {
		super(other);
		// TODO Find a way to remove the super call.
		
		_state = other;
		_zone = new Zone(other);
	}
	
	public TimedState(State s, Zone z)
	{
		super(s);
		// TODO: Find a way to remove the super call.
		_state = s;
		_zone = z.clone();
	}
	
	public String toString()
	{
		return _state.toString() + "\n" + _zone;
	}
	
	public List<Transition> getEnabledTransitionByZone()
	{
		return _zone.getEnabledTransitions();
	}
	
	public Zone getZone()
	{
		return _zone;
	}

	public State getState()
	{
		return _state;
	}
	
}
