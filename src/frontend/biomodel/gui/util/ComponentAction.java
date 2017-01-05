package frontend.biomodel.gui.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import frontend.main.Gui;

public class ComponentAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7810208239930810196L;

	private String id;

	private Gui gui;

	public ComponentAction(String name, String id, Gui gui) {
		super(name);
		this.id = id;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		gui.openModelEditor(id, false);
	}

}
