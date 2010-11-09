package gcm2sbml.gui.modelview.movie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.LayoutManager;
import java.util.HashMap;

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

	private JTextField minTextField;
	private JTextField maxTextField;
	
	
	public ColorSchemeChooser() {
		this.setLayout(new VerticalLayout());
		
		buildGUI();
		
	}
	
	HashMap<String, GradientViewer> gradientHashMap = new HashMap<String, GradientViewer>();
	
	private void buildGUI(){
		
		//GradientViewer gs[] = {new GradientViewer(Color.BLACK, Color.RED), new GradientViewer(Color.BLACK, Color.GREEN), new GradientViewer(Color.BLACK, Color.BLUE)};
		JLabel l;
		JPanel p;
		JButton b;
		
		
		GradientViewer g = new GradientViewer(Color.BLACK, new Color(0, 255, 50), "GFP");
		this.add(g);
		
		JPanel minmax_panel = new JPanel(new BorderLayout());
		p = new JPanel(new VerticalLayout());
		l = new JLabel("Min");
		p.add(l);
		minTextField = new JTextField("0");
		minTextField.setColumns(5);
		p.add(minTextField);
		minmax_panel.add(p, BorderLayout.WEST);
		
		p = new JPanel(new VerticalLayout());
		l = new JLabel("Max");
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(l);
		maxTextField = new JTextField("10");
		maxTextField.setColumns(5);
		p.add(maxTextField);
		minmax_panel.add(p, BorderLayout.EAST);
		
		this.add(minmax_panel);
		
		// minmax presets
		//this.add(new JSeparator(JSeparator));
		l = new JLabel("Min-Max Presets");
		this.add(l);
		b = new JButton("Global Min-Max");
		b.setToolTipText("Set the min and max to the minimum and maximum values found in any species in this simulation");
		this.add(b);
		b = new JButton("Species Min-Max");
		b.setToolTipText("Set the min and max to the minimum and maximum values used by -this- species");
		this.add(b);
		
	}


}
