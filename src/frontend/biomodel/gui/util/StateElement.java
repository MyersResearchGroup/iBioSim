package frontend.biomodel.gui.util;

import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * Objects that implement this interface changes state,
 * which affects objects that monitor this element.
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public interface StateElement {
	/**
	 * Adds a set of enable/disable elements
	 */
	public void addStateElements(ArrayList<JComponent> elements);
		
}
