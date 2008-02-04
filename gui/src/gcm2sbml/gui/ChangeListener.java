package gcm2sbml.gui;

import java.util.Properties;

/**
 * This interface keeps track of things that change and 
 * update its own state based on the changes.
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public interface ChangeListener {
	/**
	 * Updates the state of the object based on a change event
	 */
	public void updateState(Properties property);
}
