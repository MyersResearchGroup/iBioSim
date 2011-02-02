package gcm.gui.modelview.movie.visualizations;

import gcm.gui.modelview.movie.visualizations.gradient.GradientDisplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import main.Gui;

import org.jfree.ui.tabbedui.VerticalLayout;

import com.google.gson.Gson;


public class ColorSchemeChooser extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private ColorScheme colorScheme;

	public ColorSchemeChooser(ColorScheme colorScheme) {
		this.setLayout(new VerticalLayout());
		
		if(colorScheme == null)
			throw new Error("colorScheme can't be null!");
		this.colorScheme = colorScheme;
		
		buildGUI();
		
	}

	private void buildGUI(){
		
		//GradientDisplayer gs[] = {new GradientDisplayer(Color.BLACK, Color.RED), new GradientDisplayer(Color.BLACK, Color.GREEN), new GradientDisplayer(Color.BLACK, Color.BLUE)};
		JLabel l;
		JPanel p;
		JButton b;
		
		
		// Add the gradientDisplayer
		gradientDisplayer = new GradientDisplayer(colorScheme.getColorGradient());
		this.add(gradientDisplayer);
		
		JPanel minmax_panel = new JPanel(new BorderLayout());
		p = new JPanel(new VerticalLayout());
		l = new JLabel("Min");
		p.add(l);
		minTextField = new JTextField(String.valueOf(colorScheme.getMin()));
		minTextField.setColumns(5);
		p.add(minTextField);
		minmax_panel.add(p, BorderLayout.WEST);
		
		p = new JPanel(new VerticalLayout());
		l = new JLabel("Max");
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(l);
		maxTextField = new JTextField(String.valueOf(colorScheme.getMax()));
		maxTextField.setColumns(5);
		p.add(maxTextField);
		minmax_panel.add(p, BorderLayout.EAST);
		
		this.add(minmax_panel);
		
		
		// TODO: Implement this functionality because it is cool :)
//		// minmax presets
//		p = new JPanel(new GridLayout(1,3));
//		this.add(p);
//		l = new JLabel("Min-Max Presets:");
//		p.add(l);
//		b = new JButton("Global Min-Max");
//		b.setToolTipText("Set the min and max to the minimum and maximum values found in any species in this simulation");
//		p.add(b);
//		b = new JButton("Species Min-Max");
//		b.setToolTipText("Set the min and max to the minimum and maximum values used by -this- species");
//		p.add(b);
		
	}

	/**
	 * Call this to write any changs the user made back to the colorScheme that was
	 * passed in on initialization.
	 */
	public void saveChanges(){
		try{
			colorScheme.setMin(Float.parseFloat(minTextField.getText()));
		}catch(NumberFormatException e){
			// Leave the value unchanged, until we have a better idea for how to handle bad input.
			JOptionPane.showMessageDialog(Gui.frame, "Sorry, the value entered for 'Min' was " + minTextField.getText() + " and does not look like a valid number. It was ignored.");
		}
		
		try{
			colorScheme.setMax(Float.parseFloat(maxTextField.getText()));
		}catch(NumberFormatException e){
			JOptionPane.showMessageDialog(Gui.frame, "Sorry, the value entered for 'Max' was " + maxTextField.getText() + " and does not look like a valid number. It was ignored.");
		}
		
		gradientDisplayer.saveChanges();
	}

	private JTextField minTextField;
	private JTextField maxTextField;
	private GradientDisplayer gradientDisplayer;
}
