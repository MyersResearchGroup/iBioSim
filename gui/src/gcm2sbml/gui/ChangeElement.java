package gcm2sbml.gui;

/**
 * Objects that implement this interface changes state,
 * which affects objects that monitor this element.
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public interface ChangeElement {
	/**
	 * Adds an object that listens to this element
	 */
	public void addChangeListener(ChangeListener listener);
}
