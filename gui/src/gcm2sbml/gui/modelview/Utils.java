package gcm2sbml.gui.modelview;

import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class Utils {

	
	
	public static JButton makeToolButton(String iconFilename, String actionCommand, String tooltip, ActionListener listener){
		JButton button = new JButton();
		
		button.setActionCommand(actionCommand);
		
		
		button.setToolTipText(tooltip);
		button.addActionListener(listener);
		
		
		if(iconFilename.equals("")){
			// No icon, just set the text to the tooltip
			button.setText(tooltip);
		}else{
			// Use an icon.
			String ENVVAR = System.getenv("BIOSIM");
			String separator;
			if (File.separator.equals("\\")) {
				separator = "\\\\";
			}
			else {
				separator = File.separator;
			}
			String path = ENVVAR + separator
			+ "gui" + separator + "icons" + separator + "modelview"
			+ iconFilename;
			
			button.setIcon(new ImageIcon(path));
		}
		return button;
	}
	
}
