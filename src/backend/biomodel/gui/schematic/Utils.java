package backend.biomodel.gui.schematic;

import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JRadioButton;

public class Utils {

	/**
	 * Sets up the button passed in.
	 */
	private static void setupButton(AbstractButton button, URL icon, URL selectedIcon, String actionCommand, String tooltip, ActionListener listener){
		button.setActionCommand(actionCommand);
		
		
		button.setToolTipText(tooltip);
		button.addActionListener(listener);
		
		
		if(icon == null){
			// No icon, just set the text to the tooltip
			button.setText(tooltip);
		}else{
			Utils.setIcon(button, icon);
			// set a selected icon if it exists
			//String selectedPath = path.replaceAll(".png", "_selected.png");
			//if(new File(selectedPath).exists())
			if(selectedIcon!=null)
				button.setSelectedIcon(new ImageIcon(selectedIcon));
		}
	}
	
	/**
	 * Sets the button's icon and returns the path to it.
	 * @param button
	 * @param icon
	 * @return
	 */
	public static void setIcon(AbstractButton button, URL icon){

		// set the icon
		button.setIcon(new ImageIcon(icon));
	}
	
	public static JRadioButton makeRadioToolButton(URL icon, URL selectedIcon, String actionCommand, String tooltip, ActionListener listener, final ButtonGroup buttonGroup){
		final JRadioButton button = new JRadioButton();
		buttonGroup.add(button);
		Utils.setupButton(button, icon, selectedIcon, actionCommand, tooltip, listener);
		button.setBorder(BorderFactory.createRaisedBevelBorder());
		button.setBorderPainted(true);
		
		return button;
	}
	
	
	public static JButton makeToolButton(URL icon, URL selectedIcon, String actionCommand, String tooltip, ActionListener listener){
		JButton button = new JButton();
		
		Utils.setupButton(button, icon, selectedIcon, actionCommand, tooltip, listener);
		return button;
	}
	
}
