package gcm2sbml;


import java.awt.GridLayout;

import gcm2sbml.gui.FieldPanel;
import gcm2sbml.util.Utility;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import junit.framework.TestCase;

import org.junit.Before;

public class FieldPanelTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}
	
	public void testPanel() {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(new GridLayout(1, 3));
		panel.add(new FieldPanel("ID", Utility.IDpat, true));
		panel.add(new FieldPanel("NUM", Utility.NUMpat, true));
		panel.add(new JButton("Button"));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);

		System.out.println();
	}

}
