package verification.platu.project;

import java.util.Arrays;

import verification.platu.TimingAnalysis.Zone1;
import verification.platu.stategraph.State;

public class prjStateTimed extends PrjState {

    private Object zone;

    public prjStateTimed(final State[] other, Object zone) {
    	this.stateArray = other;
        this.zone = zone;
    }

    public prjStateTimed(final State[] other) {
        stateArray = other;
    }
    
    @Override
    public boolean equals(final Object other) {
        return super.equals(other) && zone.equals(((prjStateTimed) other).zone);
    }

    @Override
    public int hashCode() {
        return super.hashCode()*31+zone.hashCode();
    }

     /**
     * @return the zone
     */
    public Zone1 getZone() {
        return (Zone1) zone;
    }

    /**
     * @param zone the zone to set
     */
    public void setZone(Object zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return Arrays.toString(stateArray)+zone.hashCode();
    }

}
