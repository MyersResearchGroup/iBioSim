package biomodel.gui;


import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodel.parser.BioModel;

import main.Gui;


public class ConditionsPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String[] options = { "Ok", "Cancel" };

	private PropertyField field;

	private ModelEditor gcmEditor = null;

	public ConditionsPanel(String selected, PropertyList conditionList, BioModel gcm,
			boolean paramsOnly,ModelEditor gcmEditor) {
		super(new GridLayout(1, 1));
		this.gcmEditor = gcmEditor;

		// Condition field
		field = new PropertyField("Property", "", null, null, "Property", paramsOnly, "default", true);
		add(field);

		String oldProperty = null;
		if (selected != null) {
			oldProperty = selected;
			// Properties prop = lhpn.getVariables().get(selected);
			field.setValue(selected);
		}

		boolean display = false;
		while (!display) {
			display = openGui(oldProperty);
		}
	}

//	private boolean checkValues() {
//		boolean goodProperty = false;
//		String propertyTemp = field.getValue();
//		if (propertyTemp != null && !propertyTemp.equals("")) {
//			// check the balance of parentheses and square brackets
//			Parser p = new Parser(propertyTemp);
//			goodProperty = p.parseProperty();
//			if (!goodProperty) {
//				JOptionPane.showMessageDialog(Gui.frame,
//						"Invalid property. See terminal for detailed information.",
//						"Error in Property", JOptionPane.ERROR_MESSAGE);
//			}
//			return goodProperty;
//
//		}
//		else {
//			goodProperty = true;
//			return goodProperty;
//		}
//
//	}

	private boolean openGui(String oldProperty) {
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Property Editor",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value == JOptionPane.YES_OPTION) {
			gcmEditor.setDirty(true);
		}
		else if (value == JOptionPane.NO_OPTION) {
			return true;
		}
		return true;
	}

	public void actionPerformed(ActionEvent e) {
	}
}
