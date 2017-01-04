package verification.timed_state_exploration.zoneProject;

import java.util.HashMap;

public class ContinuousRecordSet extends HashMap<UpdateContinuous, UpdateContinuous> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7357798608686001995L;

	
	/**
	 * Adds an UpdateContinuous record.
	 * 
	 * @param record The UpdateContinuous object to add.
	 * @return The previous record for the same continuous variable if it exists; null
	 * otherwise.
	 */
	public UpdateContinuous add(UpdateContinuous record){
		return super.put(record, record);
	}
	
	/**
	 * Returns any record that has an underlying LPNTransitionPair. This is contained
	 * in the LPNContAndRate portion of the UpdateContinuous.
	 * @param ltpair
	 * @return
	 */
	public UpdateContinuous get(LPNContinuousPair lcpair){
		
		return super.get(new UpdateContinuous(lcpair));
	}
	
	public boolean contains(LPNContinuousPair lcpair){
		return super.containsKey(new UpdateContinuous(lcpair));
	}
}
