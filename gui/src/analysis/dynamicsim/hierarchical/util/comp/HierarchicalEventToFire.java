package analysis.dynamicsim.hierarchical.util.comp;

import java.util.Map;

public class HierarchicalEventToFire
{

	private Map<String, Double>	eventAssignmentSet	= null;
	private String				eventID				= "";
	private double				fireTime			= 0.0;
	private String				modelID;

	public HierarchicalEventToFire(String modelID, String eventID, Map<String, Double> eventAssignmentSet, double fireTime)
	{

		this.eventID = eventID;
		this.eventAssignmentSet = eventAssignmentSet;
		this.fireTime = fireTime;
		this.modelID = modelID;
	}

	/**
	 * @return the eventAssignmentSet
	 */
	public Map<String, Double> getEventAssignmentSet()
	{
		return eventAssignmentSet;
	}

	/**
	 * @return the eventID
	 */
	public String getEventID()
	{
		return eventID;
	}

	/**
	 * @return the fireTime
	 */
	public double getFireTime()
	{
		return fireTime;
	}

	/**
	 * @return the modelID
	 */
	public String getModelID()
	{
		return modelID;
	}

	/**
	 * @param eventAssignmentSet
	 *            the eventAssignmentSet to set
	 */
	public void setEventAssignmentSet(Map<String, Double> eventAssignmentSet)
	{
		this.eventAssignmentSet = eventAssignmentSet;
	}

	/**
	 * @param eventID
	 *            the eventID to set
	 */
	public void setEventID(String eventID)
	{
		this.eventID = eventID;
	}

	/**
	 * @param fireTime
	 *            the fireTime to set
	 */
	public void setFireTime(double fireTime)
	{
		this.fireTime = fireTime;
	}

	/**
	 * @param modelID
	 *            the modelID to set
	 */
	public void setModelID(String modelID)
	{
		this.modelID = modelID;
	}
}