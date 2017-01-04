package analysis.incrementalsim;
import java.util.Comparator;

public class EventQueueComparator implements Comparator<EventQueueElement>{
	@Override
	public int compare(EventQueueElement e1, EventQueueElement e2) {
		if (e1.getScheduledTime() < e2.getScheduledTime()){
			return -1;
		}
		else if (e1.getScheduledTime() > e2.getScheduledTime()) {
			return 1;
		}
		else { // e1.getScheduledTime() == e2.getScheduledTime()
			// Compare priority
			if (e1.getPriorityVal() > e2.getPriorityVal()) {
				return -1;
			}
			if (e1.getPriorityVal() < e2.getPriorityVal()) {
				return 1;
			}
			return 0;
		}
	}
}
