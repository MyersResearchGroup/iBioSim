package gcm.gui.modelview.movie;

import gcm.gui.modelview.movie.MovieScheme.Scheme;
import gcm.util.GlobalConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.Paint;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

/**
 * this is the panel that pops up in the movie schematic
 * it allows you to change color gradients for things
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
	private JComboBox sizeChooser;
	private JComboBox opacityChooser;
	private JComboBox applyToChooser;
	
	//these color arrays need to remain synchronized
	//because you can't get the color name from the RGB data	
	private Color[] colorsArray;
	private String[] colorNamesArray;
	private String[] sizeOptions = {"none", "enabled"};
	private String[] opacityOptions = {"none", "enabled"};
	private String[] applyToComponentOptions = {"this component only", "all components with this model"};
	private String[] applyToGridOptions = {"this location only", "all grid locations"};
	private String cellType;
	private Color startColor; //the end of the gradient opposite the actual color
	
	private MovieContainer movieContainer;
	private MovieScheme movieScheme;
	
	//has every species within the TSD
	private ArrayList<String> allSpecies;
	
	//has every species within the particular cell (eg, component or grid location)
	private ArrayList<String> cellSpecies;
	
	//ID of the cell that was clicked on
	private String cellID;

	
	//CLASS METHODS

	/**
	 * constructor
	 */
	public SchemeChooserPanel(String cellID, MovieContainer movieContainer) {
		
		//call the JPanel constructor to make this a border layout panel
		super(new BorderLayout());
		
		cellSpecies = new ArrayList<String>();
		
		this.cellType = GlobalConstants.COMPONENT;
		this.cellID = cellID;
		this.movieContainer = movieContainer;
		this.movieScheme = movieContainer.getMovieScheme();
		this.allSpecies = movieContainer.getTSDParser().getSpecies();
		this.startColor = Color.black;
		
		this.colorsArray = new Color[] {this.getBackground(), Color.green, Color.red, Color.blue, Color.cyan, 
			Color.magenta, Color.pink, Color.yellow, Color.orange};
		
		this.colorNamesArray = new String[] {"none", "green", "red", "blue", "cyan", 
			"magenta", "pink", "yellow", "orange"};
		
		determineCellType(cellID);
		
		//if this is a species, open the panel for species
		if (cellType.equals(GlobalConstants.SPECIES)) {
			
		}
		//this is a component or grid location
		else {
		
			//this strips off species prefixes (ie, component or grid location)
			//for species within the cellID cell
			//(eg, C4__S1 will become S1 if cellID is C4)
			for (String species : allSpecies) {
				
				String[] speciesParts = species.split("__");
				
				//if there's a component or grid location prefix, strip it
				if (speciesParts != null && speciesParts.length > 1) {
					
					String speciesIDNoPrefix = species.replace(new String(speciesParts[0] + "__"), "");
					
					if (speciesParts[0].contains(cellID))
						cellSpecies.add(speciesIDNoPrefix);
				}
			}
			
			//if there aren't any species to change, tell the user
			//and don't build the panel
			if (cellSpecies.size() == 0) {
				
				JOptionPane.showMessageDialog(Gui.frame,
						"There aren't any species in this " + 
							cellType.toLowerCase() + " to adjust the appearance of.\n",
						"No Species Present", JOptionPane.ERROR_MESSAGE);
				
				changed = false;
			}
			else
				changed = buildPanel() == true ? true : false;
		}
	}
	
	/**
	 * access point and static call to create a scheme chooser panel
	 * 
	 * @param cellID the cell that was clicked on
	 * @param movieContainer
	 * @return
	 */
	public static boolean showSchemeChooserPanel(String cellID, MovieContainer movieContainer) {

		new SchemeChooserPanel(cellID, movieContainer);
		
		return changed;
	}
	
	/**
	 * builds the color scheme chooser panel
	 * 
	 * @return whether the user clicked ok or cancel
	 */
	private boolean buildPanel() {
		
		JPanel infoPanel = new JPanel(new GridLayout(1, 1));
		infoPanel.add(new JLabel("<html>Selecting a color scheme for a species will associate a color gradient<br>" +
				"in proportion to the number of molecules present with time.<br><br></html>"));
		this.add(infoPanel, BorderLayout.NORTH);
		
		optionsPanel = new JPanel(new GridLayout(7, 2));
		this.add(optionsPanel, BorderLayout.CENTER);
		
		optionsPanel.add(new JLabel("Species"));
		speciesChooser = new JComboBox(cellSpecies.toArray());
		speciesChooser.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				
				updatePanelValues();
			}
		});
		optionsPanel.add(speciesChooser);
		
		optionsPanel.add(new JLabel("Color Gradient"));
		colorChooser = new JComboBox(colorsArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		colorChooser.setRenderer(renderer);
		optionsPanel.add(colorChooser);
		
		optionsPanel.add(new JLabel("Size Gradient"));
		sizeChooser = new JComboBox(sizeOptions);
		sizeChooser.setEnabled(false);
		optionsPanel.add(sizeChooser);
		
		optionsPanel.add(new JLabel("Opacity Gradient"));
		opacityChooser = new JComboBox(opacityOptions);
		opacityChooser.setEnabled(false);
		optionsPanel.add(opacityChooser);
		
		optionsPanel.add(new JLabel("Min. number of molecules visible"));
		minChooser = new JTextField("0");
		optionsPanel.add(minChooser);
		
		optionsPanel.add(new JLabel("Saturating number of molecules"));
		maxChooser = new JTextField("20");
		optionsPanel.add(maxChooser);
		
		//note: if it's a species, then there's no applyToChooser
		if (cellType.equals(GlobalConstants.GRID_RECTANGLE)) {
		
			optionsPanel.add(new JLabel("Apply to"));
			applyToChooser = new JComboBox(applyToGridOptions);
			optionsPanel.add(applyToChooser);
		}
		else if (cellType.equals(GlobalConstants.COMPONENT)) {
		
			optionsPanel.add(new JLabel("Apply to"));
			applyToChooser = new JComboBox(applyToComponentOptions);
			optionsPanel.add(applyToChooser);
		}
		
		//populate the panel with store values if they exist
		updatePanelValues();
		
		String[] options = {"Save Changes", GlobalConstants.CANCEL};
		
		int okCancel = JOptionPane.showOptionDialog(Gui.frame, this, "Select inner species color scheme",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		//if the user clicks "save changes" on the panel
		if (okCancel == JOptionPane.OK_OPTION) {
			
			updateMovieScheme();
			
		}
		
		return false;
	}
	
	/**
	 * updates the movie scheme with the selected information for this cell
	 * this happens when the user hits "okay"
	 */
	private void updateMovieScheme() {
		
		//get the data that the user selected/input
		Color selectedColor = (Color)colorChooser.getSelectedItem();
		String speciesID = cellID + "__" + speciesChooser.getSelectedItem().toString();
		String applyTo = applyToChooser.getSelectedItem().toString();
		int min = Integer.parseInt(minChooser.getText());
		int max = Integer.parseInt(maxChooser.getText());
		
    	GradientPaint selectedGradient = 
    		new GradientPaint(min, 0, startColor, max, 0, selectedColor, false);
    	
    	//if a color gradient was selected
    	if (!selectedColor.equals(colorsArray[0])) {
    	
    		//the false indicates that this is an addition to the scheme map
			movieContainer.getMovieScheme().addSpeciesScheme(
					speciesID, selectedGradient, min, max, applyTo, movieContainer.getGCM(), cellType);
    	}
    	//if a color gradient wasn't selected (ie, "none" was selected)
    	else {
    		
    		movieContainer.getMovieScheme().removeSpeciesScheme(speciesID, cellType, applyTo);
    	}
	}
	
	/**
	 * determines whether the selected cell is a grid location, component, or species
	 * @param cellID
	 */
	private void determineCellType(String cellID) {
		
		if (allSpecies.contains(cellID))
			cellType = GlobalConstants.SPECIES;
		else if (cellID.contains("ROW"))
			cellType = GlobalConstants.GRID_RECTANGLE;
		else 
			cellType = GlobalConstants.COMPONENT;
	}
	
	/**
	 * updates the gradient, min, max, and apply to values
	 * based on which species is currently selected
	 * and which values have been saved
	 */
	private void updatePanelValues() {
		
		String speciesID = cellID + "__" + speciesChooser.getSelectedItem().toString();
		
		Scheme speciesScheme = movieScheme.getSpeciesScheme(speciesID);
		
		if (speciesScheme != null) {
		
			//get the scheme data for the species that was selected
			GradientPaint colorGradient = speciesScheme.getColorGradient();
			int min = speciesScheme.getMin();
			int max = speciesScheme.getMax();
			
			if (colorGradient != null)
				colorChooser.setSelectedItem(colorGradient.getColor2());
			else
				colorChooser.setSelectedItem(colorsArray[0]);
			
			minChooser.setText(Integer.toString(min));
			maxChooser.setText(Integer.toString(max));
		}
		else
			colorChooser.setSelectedItem(colorsArray[0]);
		
		//re-render the color combobox so that it shows the stored item
		colorChooser.setRenderer(new ComboBoxRenderer());
	}
	
	/**
	 * not currently used
	 */
	public void actionPerformed(ActionEvent event) {
	}
	
	
	//COMBOBOXRENDERER CLASS
	
	/**
	 * this is used to override the default cell renderer for
	 * comboboxes so that colors can be shown in the drop-down list
	 * 
	 * this is for the gradient combobox only at the moment
	 * 
	 * @author jason
	 */
	class ComboBoxRenderer extends DefaultListCellRenderer {
	
		private static final long serialVersionUID = 1L;
		
		Color gradientColor, selectedGradientColor;
		String name, selectedName;

		/**
		 * constructor
		 */
		public ComboBoxRenderer() {
			super();
			
			gradientColor = (Color)colorChooser.getSelectedItem();
			selectedGradientColor = (Color)colorChooser.getSelectedItem();
			
			name = colorNamesArray[colorChooser.getSelectedIndex()];
			selectedName = colorNamesArray[colorChooser.getSelectedIndex()];
		}
	
		/**
		 * used to assign a gradient color based on which component
		 * the user has hovered over or clicked on
		 * this is then painted (using the paintComponent function below) when it's passed back
		 */
		public Component getListCellRendererComponent(
		                    JList list,
		                    Object value,
		                    int index,
		                    boolean isSelected,
		                    boolean cellHasFocus) {
		    
		    //create the colors for the drop-down list
		    if (index <= colorsArray.length && index >= 0) {
		    	
		    	gradientColor = colorsArray[index];
		    	name = colorNamesArray[index];
		    }
		    else {
		    	gradientColor = selectedGradientColor;
		    	name = selectedName;
		    }
		    
		    //make sure the selected color only changes on a mouse click
		    if (isSelected && index <= colorsArray.length && index >= 0) {
		    	
		    	selectedGradientColor = colorsArray[index];
		    	selectedName = colorNamesArray[index];
		    }
		    
			setText(name);
			
		    return this;
		}
		
		/**
		 * draw the gradient for whichever item in the list
		 */
		public void paintComponent(Graphics g) {
			
			Color endColor;
			
			if (name.equals("none"))
				endColor = colorsArray[0];
			else 
				endColor = startColor;
			
			setOpaque(false);
	        Graphics2D g2d = (Graphics2D) g;
	        Paint oldPaint = g2d.getPaint();
	        Paint newPaint = new GradientPaint(0, 0, gradientColor, getWidth(), 0, endColor, false);
	        g2d.setPaint(newPaint);
	        g2d.fillRect(0,0,getWidth(),getHeight());
	        g2d.setPaint(oldPaint);
	        super.paintComponent(g);
		}
	}
}
