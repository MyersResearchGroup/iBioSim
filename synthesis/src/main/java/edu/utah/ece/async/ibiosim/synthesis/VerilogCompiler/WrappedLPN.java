package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import edu.utah.ece.async.lema.verification.lpn.LPN;

import java.util.HashSet;

/**
 * A utility class used to encode SBML using LPN model.
 * @author Tramy Nguyen
 *
 */
public class WrappedLPN {
    private int nextPlace;
    private int nextTransition;
    private String currentStart;

    public LPN lpn;
    public HashSet<String> last;

    public WrappedLPN(LPN lpn) {
        this.nextPlace = 0;
        this.nextTransition = 0;

        this.currentStart = null;
        this.lpn = lpn;
        this.last = new HashSet<>();
    }

    public void addTransition(String name) {
        this.lpn.addTransition(name);
        this.lpn.changeDelay(name, "uniform(0,5)");
    }

    public String nextPlaceName() {
        String nextPlaceName = "P" + Integer.toString(nextPlace);
        nextPlace++;

        return nextPlaceName;
    }

    public String nextTransitionName() {
        String nextTransitionName = "T" + Integer.toString(nextTransition);
        nextTransition++;

        return nextTransitionName;
    }

    public void closeNet() {
        String transitionName = nextTransitionName();

        this.lpn.addTransition(transitionName);

        for (String place : this.last) {
            this.lpn.addMovement(place, transitionName);
        }

        this.lpn.addMovement(transitionName, currentStart);
    }

    public void createNewNet() {
        String placeName = nextPlaceName();
        this.currentStart = placeName;
        this.last.clear();
        this.last.add(placeName);
        this.lpn.addPlace(placeName, true);
    }
}