package analysis.dynamicsim.hierarchical.states;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;

import analysis.dynamicsim.hierarchical.util.HierarchicalEventToFire;

public abstract class EventState extends ReactionState
{
	private HashMap<String, HashSet<String>>		eventToAffectedReactionSetMap;
	private HashMap<String, HashSet<Object>>		eventToAssignmentSetMap;
	private HashMap<String, ASTNode>				eventToDelayMap;
	private HashMap<String, Boolean>				eventToHasDelayMap;
	private HashMap<String, Boolean>				eventToPreviousTriggerValueMap;
	private HashMap<String, ASTNode>				eventToPriorityMap;
	private HashMap<String, Boolean>				eventToTriggerInitiallyTrueMap;
	private HashMap<String, ASTNode>				eventToTriggerMap;
	private HashMap<String, Boolean>				eventToTriggerPersistenceMap;
	private HashMap<String, Boolean>				eventToUseValuesFromTriggerTimeMap;
	private PriorityQueue<HierarchicalEventToFire>	triggeredEventQueue;
	private HashSet<String>							untriggeredEventSet;

	public EventState(HashMap<String, Model> models, String bioModel, String submodelID)
	{
		super(models, bioModel, submodelID);
		untriggeredEventSet = new HashSet<String>();
		if (getNumEvents() > 0)
		{
			setNoEventsFlag(false);
			untriggeredEventSet = new HashSet<String>((int) getNumEvents());
			eventToPriorityMap = new HashMap<String, ASTNode>((int) getNumEvents());
			eventToDelayMap = new HashMap<String, ASTNode>((int) getNumEvents());
			eventToHasDelayMap = new HashMap<String, Boolean>((int) getNumEvents());
			eventToTriggerMap = new HashMap<String, ASTNode>((int) getNumEvents());
			eventToTriggerInitiallyTrueMap = new HashMap<String, Boolean>((int) getNumEvents());
			eventToTriggerPersistenceMap = new HashMap<String, Boolean>((int) getNumEvents());
			eventToUseValuesFromTriggerTimeMap = new HashMap<String, Boolean>((int) getNumEvents());
			eventToAssignmentSetMap = new HashMap<String, HashSet<Object>>((int) getNumEvents());
			eventToAffectedReactionSetMap = new HashMap<String, HashSet<String>>(
					(int) getNumEvents());
			eventToPreviousTriggerValueMap = new HashMap<String, Boolean>((int) getNumEvents());
			setVariableToEventSetMap(new HashMap<String, HashSet<String>>((int) getNumEvents()));
		}
	}

	public EventState(EventState state)
	{
		super(state);
		eventToAffectedReactionSetMap = state.eventToAffectedReactionSetMap;
		eventToAssignmentSetMap = state.eventToAssignmentSetMap;
		eventToDelayMap = state.eventToDelayMap;
		eventToHasDelayMap = state.eventToHasDelayMap;
		eventToPreviousTriggerValueMap = state.eventToPreviousTriggerValueMap;
		eventToPriorityMap = state.eventToPriorityMap;
		eventToTriggerInitiallyTrueMap = state.eventToTriggerInitiallyTrueMap;
		eventToTriggerMap = state.eventToTriggerMap;
		eventToTriggerPersistenceMap = state.eventToTriggerPersistenceMap;
		eventToUseValuesFromTriggerTimeMap = state.eventToUseValuesFromTriggerTimeMap;
		triggeredEventQueue = state.triggeredEventQueue;
		untriggeredEventSet = state.untriggeredEventSet;
	}

	public HashMap<String, HashSet<String>> getEventToAffectedReactionSetMap()
	{
		return eventToAffectedReactionSetMap;
	}

	public void setEventToAffectedReactionSetMap(
			HashMap<String, HashSet<String>> eventToAffectedReactionSetMap)
	{
		this.eventToAffectedReactionSetMap = eventToAffectedReactionSetMap;
	}

	public HashMap<String, HashSet<Object>> getEventToAssignmentSetMap()
	{
		return eventToAssignmentSetMap;
	}

	public void setEventToAssignmentSetMap(HashMap<String, HashSet<Object>> eventToAssignmentSetMap)
	{
		this.eventToAssignmentSetMap = eventToAssignmentSetMap;
	}

	public HashMap<String, ASTNode> getEventToDelayMap()
	{
		return eventToDelayMap;
	}

	public void setEventToDelayMap(HashMap<String, ASTNode> eventToDelayMap)
	{
		this.eventToDelayMap = eventToDelayMap;
	}

	public HashMap<String, Boolean> getEventToHasDelayMap()
	{
		return eventToHasDelayMap;
	}

	public void setEventToHasDelayMap(HashMap<String, Boolean> eventToHasDelayMap)
	{
		this.eventToHasDelayMap = eventToHasDelayMap;
	}

	public HashMap<String, Boolean> getEventToPreviousTriggerValueMap()
	{
		return eventToPreviousTriggerValueMap;
	}

	public void setEventToPreviousTriggerValueMap(
			HashMap<String, Boolean> eventToPreviousTriggerValueMap)
	{
		this.eventToPreviousTriggerValueMap = eventToPreviousTriggerValueMap;
	}

	public HashMap<String, ASTNode> getEventToPriorityMap()
	{
		return eventToPriorityMap;
	}

	public void setEventToPriorityMap(HashMap<String, ASTNode> eventToPriorityMap)
	{
		this.eventToPriorityMap = eventToPriorityMap;
	}

	public HashMap<String, Boolean> getEventToTriggerInitiallyTrueMap()
	{
		return eventToTriggerInitiallyTrueMap;
	}

	public void setEventToTriggerInitiallyTrueMap(
			HashMap<String, Boolean> eventToTriggerInitiallyTrueMap)
	{
		this.eventToTriggerInitiallyTrueMap = eventToTriggerInitiallyTrueMap;
	}

	public HashMap<String, ASTNode> getEventToTriggerMap()
	{
		return eventToTriggerMap;
	}

	public void setEventToTriggerMap(HashMap<String, ASTNode> eventToTriggerMap)
	{
		this.eventToTriggerMap = eventToTriggerMap;
	}

	public HashMap<String, Boolean> getEventToTriggerPersistenceMap()
	{
		return eventToTriggerPersistenceMap;
	}

	public void setEventToTriggerPersistenceMap(
			HashMap<String, Boolean> eventToTriggerPersistenceMap)
	{
		this.eventToTriggerPersistenceMap = eventToTriggerPersistenceMap;
	}

	public HashMap<String, Boolean> getEventToUseValuesFromTriggerTimeMap()
	{
		return eventToUseValuesFromTriggerTimeMap;
	}

	public void setEventToUseValuesFromTriggerTimeMap(
			HashMap<String, Boolean> eventToUseValuesFromTriggerTimeMap)
	{
		this.eventToUseValuesFromTriggerTimeMap = eventToUseValuesFromTriggerTimeMap;
	}

	public HashSet<String> getUntriggeredEventSet()
	{
		return untriggeredEventSet;
	}

	public void setUntriggeredEventSet(HashSet<String> untriggeredEventSet)
	{
		this.untriggeredEventSet = untriggeredEventSet;
	}

	public PriorityQueue<HierarchicalEventToFire> getTriggeredEventQueue()
	{
		return triggeredEventQueue;
	}

	public void setTriggeredEventQueue(PriorityQueue<HierarchicalEventToFire> triggeredEventQueue)
	{
		this.triggeredEventQueue = triggeredEventQueue;
	}

}
