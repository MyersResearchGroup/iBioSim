package gcm2sbml.visitor;

import javax.swing.JButton;

public interface GuiVisitor {

	/**
	 * Visits a JButton
	 * @param button the button to visit
	 */
	public void visitButton(JButton button);
}
