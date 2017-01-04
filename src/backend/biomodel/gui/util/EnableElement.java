package backend.biomodel.gui.util;


/**
 * This interface keeps track of when things
 * are enabled or disabled
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public interface EnableElement {
	/**
	 * Updates the state of the object based on an event
	 */
	public void setEnabled(boolean state);
}
