package frontend.biomodel.gui.util;

import javax.swing.JButton;


public abstract class AbstractRunnableNamedButton extends JButton implements NamedObject,
		Runnable {		
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractRunnableNamedButton(String name) {
		super(name);
		this.setName(name);
	}
	
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}
	
	//private String name = "";
}
