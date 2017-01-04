package backend.biomodel.gui.util;

import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Describes a panel that contains a label and field with a value
 * 
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public class FieldPanel extends JPanel implements NamedObject, FocusListener {

	private static final long serialVersionUID = 1L;
	
	public FieldPanel(String name, Pattern pattern, boolean editable) {
		super(new GridLayout(1, 2));
		super.setName(name);
		//this.editable = editable;
		this.pattern = pattern;
		this.field = new JTextField(40);
		field.addFocusListener(this);		
		this.add(new JLabel(name));
		this.add(field);
		field.setEditable(editable);
		 
	}

	@Override
	public String getName() {
		return super.getName();
	}

	public String getValue() {
		return field.getText();
	}

	@Override
	public void focusGained(FocusEvent e) {
		//Do nothing
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		//Must check to see if input was valid
		Matcher matcher =  pattern.matcher(field.getText());
		if (!matcher.find()) {
			JOptionPane
			.showMessageDialog(
					this,
					"Invalid input.");
		}
		field.requestFocus();
	}

	private Pattern pattern = null;
	//private boolean editable = true;
	private JTextField field = null;	
}
