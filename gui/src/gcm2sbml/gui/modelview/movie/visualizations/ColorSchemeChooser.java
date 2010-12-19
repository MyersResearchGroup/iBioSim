package gcm2sbml.gui.modelview.movie.visualizations;

import gcm2sbml.gui.modelview.movie.GradientEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jfree.ui.tabbedui.VerticalLayout;

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
		
		//GradientEditor gs[] = {new GradientEditor(Color.BLACK, Color.RED), new GradientEditor(Color.BLACK, Color.GREEN), new GradientEditor(Color.BLACK, Color.BLUE)};
		JLabel l;
		JPanel p;
		JButton b;
		
		
		gradientEditor = new GradientEditor(colorScheme.getColorGradient());
		this.add(gradientEditor);
		
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
		
		// minmax presets
		//this.add(new JSeparator(JSeparator));
		p = new JPanel(new GridLayout(1,3));
		this.add(p);
		l = new JLabel("Min-Max Presets:");
		p.add(l);
		b = new JButton("Global Min-Max");
		b.setToolTipText("Set the min and max to the minimum and maximum values found in any species in this simulation");
		p.add(b);
		b = new JButton("Species Min-Max");
		b.setToolTipText("Set the min and max to the minimum and maximum values used by -this- species");
		p.add(b);
		
	}

	/**
	 * Call this to write any changs the user made back to the colorScheme that was
	 * passed in on initialization.
	 */
	public void saveChanges(){
		try{
			colorScheme.setMin(Integer.parseInt(minTextField.getText()));
		}catch(NumberFormatException e){
			// Leave the value unchanged, until we have a better idea for how to handle bad input.
		}
		
		try{
			colorScheme.setMax(Integer.parseInt(maxTextField.getText()));
		}catch(NumberFormatException e){
			
		}
		
		gradientEditor.saveChanges();
	}

	private JTextField minTextField;
	private JTextField maxTextField;
	private GradientEditor gradientEditor;
}
