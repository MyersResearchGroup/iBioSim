package frontend.analysis.incrementalsim;


public class EventQueueElement {
	private double time;
	private double delayVal;
	private String eventID;
	private double priorityVal;
	
	public EventQueueElement(double time, String eventID, double delayVal, double priorityVal ) {
		this.time = time;
		this.delayVal = delayVal;
		this.eventID = eventID;
		this.priorityVal = priorityVal;
	}
		
	public double getScheduledTime(){
		return time+delayVal;
	}
	
	public String getEventId(){
		return eventID;
	}
	
	public double getPriorityVal() {
		return priorityVal;
	}

}
