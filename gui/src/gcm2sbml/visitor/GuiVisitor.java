package gcm2sbml.visitor;

import gcm2sbml.gui.PropertyList;

import javax.swing.JButton;

public interface GuiVisitor {

	/**
	 * Visits a JButton
	 * @param button the button to visit
	 */
	public void visitButton(JButton button);
	
	/**
	 * Visits a PropertyList
	 * @param list the list to visit
	 */
	public void visitPropertyList(PropertyList list);
}
