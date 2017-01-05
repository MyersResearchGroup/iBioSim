package backend.analysis.dynamicsim.hierarchical.util.comp;

import java.util.Comparator;

import backend.analysis.dynamicsim.hierarchical.math.EventNode;

// EVENT COMPARATOR INNER CLASS
/**
 * compares two events to see which one should be before the other in the
 * priority queue
 */
public class HierarchicalEventComparator implements Comparator<EventNode>
{
	@Override
	public int compare(EventNode event1, EventNode event2)
	{
		if (event1.getFireTime() > event2.getFireTime())
		{
			return 1;
		}
		else if (event1.getFireTime() < event2.getFireTime())
		{
			return -1;
		}
		else
		{

			if (event1.getPriority() > event2.getPriority())
			{
				return -1;
			}
			else if (event1.getPriority() < event2.getPriority())
			{
				return 1;
			}
			else
			{
				return Math.random() > 0.5 ? 1 : -1;
			}

		}
	}
}