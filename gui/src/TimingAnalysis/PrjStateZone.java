package platu.TimingAnalysis;

import java.util.Arrays;
import platu.stategraph.*;

public class PrjStateZone {
	State[] stateArray;
	Zone1 zone;
	
	public PrjStateZone(final State[] other, Zone1 zone) {
    	this.stateArray = other;
        this.zone = zone;
    }

    
    @Override
    public boolean equals(final Object other) {
    	PrjStateZone otherSt = (PrjStateZone)other;
    	for(int i = 0; i < this.stateArray.length; i++)
    		if(this.stateArray[i] != otherSt.stateArray[i])
    			return false;
        return this.zone==otherSt.zone;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.stateArray) ^ zone.hashCode();
    }

     /**
     * @return the zone
     */
    public Zone1 getZone() {
        return zone;
    }

    public State[]	getStateArray()	{
    	return stateArray;
    }

    @Override
    public String toString() {
        return Arrays.toString(stateArray)+zone.hashCode();
    }

}
