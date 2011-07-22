package gcm.gui.modelview.movie;

import gcm.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

/**
 * this is the panel that pops up in the movie schematic
 * it allows you to change colors for things
 * 
 * @author jason (sarbruis@gmail.com)
 */
public class SchemeChooserPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	//CLASS VARIABLES
	
	public static boolean changed;
	
	private JPanel optionsPanel;
	
	private JTextField minChooser;
	private JTextField maxChooser;
	private JComboBox speciesChooser;
	private JComboBox colorChooser;
	private JComboBox applyToChooser;
	
	MovieContainer movieContainer;
	
	ArrayList<String> allSpecies;

	/**
	 * constructor
	 */
	public SchemeChooserPanel(MovieContainer movieContainer) {
		
		//call the JPanel constructor to make this a border layout panel
		super(new BorderLayout());
		
		this.movieContainer = movieContainer;
		this.allSpecies = movieContainer.getTSDParser().getSpecies();
		
		changed = buildPanel() == true ? true : false;
	}
	
	public static boolean showSchemeChooserPanel(MovieContainer movieContainer) {

		new SchemeChooserPanel(movieContainer);
		
		return changed;
	}
	
	/**
	 * builds the color scheme chooser panel
	 * 
	 * @return whether the user clicked ok or cancel
	 */
	private boolean buildPanel() {
		
		String[] colorsArray = {"green", "red", "blue"};
		
		String[] applyToOptions = {"this component only", "all components with this model"};
		
		optionsPanel = new JPanel(new GridLayout(5, 2));
		this.add(optionsPanel, BorderLayout.CENTER);
		
		optionsPanel.add(new JLabel("Species"));
		speciesChooser = new JComboBox(allSpecies.toArray());
		optionsPanel.add(speciesChooser);
		
		optionsPanel.add(new JLabel("Color"));
		colorChooser = new JComboBox(colorsArray);
		optionsPanel.add(colorChooser);
		
		optionsPanel.add(new JLabel("Min"));
		minChooser = new JTextField("0");
		optionsPanel.add(minChooser);
		
		optionsPanel.add(new JLabel("Max"));
		maxChooser = new JTextField("100");
		optionsPanel.add(maxChooser);
		
		optionsPanel.add(new JLabel("Apply to"));
		applyToChooser = new JComboBox(applyToOptions);
		optionsPanel.add(applyToChooser);
		
		String[] options = {GlobalConstants.OK, GlobalConstants.CANCEL};
		
		int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Select a Color Scheme",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		//if the user clicks "ok" on the panel
		if (okCancel == JOptionPane.OK_OPTION) {
			
			
		}
		
		return false;
	}
	
	public void actionPerformed(ActionEvent event) {
	}
	
	
}
