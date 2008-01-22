package gcm2sbml.gui;

import javax.swing.JButton;

public abstract class AbstractRunnableNamedButton extends JButton implements NamedObject,
		RunnableGui {		
	
	public AbstractRunnableNamedButton(String name) {
		super(name);
		this.setName(name);
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName();
	}
	
	private String name = "";
}
