package gcm2sbml;


import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import gcm2sbml.gui.GCM2SBMLEditor;
import junit.framework.TestCase;

import org.junit.Before;

public class GCM2SBMLEditorTest extends TestCase {

	@Before
	public void setUp() throws Exception {
	}
	
	public void testList() {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel();
		DefaultListModel model = new DefaultListModel();
		model.addElement("foo");
		model.addElement("bar");
		JList list = new JList(model);
		panel.add(list);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		System.out.println();
		model.remove(1);
		System.out.println();
	}
	
	public void testView() {
		System.loadLibrary("sbmlj");
		GCM2SBMLEditor editor = new GCM2SBMLEditor("", "nand.dot", null);
		editor.setVisible(true);
		JFrame frame = new JFrame();
		frame.add(editor);
		frame.pack();
		frame.setVisible(true);
		System.out.println();
		System.out.println();
	}
	
	

}
