package test.gcm2sbml;

import gcm2sbml.gui.FieldPanel;
import gcm2sbml.gui.PropertyField;
import gcm2sbml.util.Utility;

import java.awt.GridLayout;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import junit.framework.TestCase;

public class FieldPanelTest extends TestCase {

	//@Before
	public void setUp() throws Exception {
	}

	public void Panel() {
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

	public void testPropertyField() {
		JFrame frame = new JFrame();
		Properties p = new Properties();
		p.setProperty("decay", ".0075");
		PropertyField field = new PropertyField("decay", ".0075", PropertyField.states[0], ".0075");
		field.setRegExp(Utility.NUMstring);
		frame.add(field);
		frame.pack();
		frame.setVisible(true);

		System.out.println();
		field.setEnabled(false);
		field.setEnabled(true);
	}

}
