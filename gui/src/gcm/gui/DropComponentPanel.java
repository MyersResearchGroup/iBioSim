package gcm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import gcm.parser.GCMFile;
import gcm.util.GlobalConstants;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import main.Gui;

public class DropComponentPanel extends JPanel implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String TOPLEFT = "Top Left";
	private static final String DROP_ON_MOUSE_CLICK = "Drop on Mouse Click";
	private static final String[] ORIENTATIONS = {TOPLEFT, DROP_ON_MOUSE_CLICK};
	
	private JComboBox componentCombo;
	private JRadioButton doTiling;
	private JPanel tilePanel;
	private JTextField rowsChooser;
	private JTextField columnsChooser;
	private JTextField paddingChooser;
	private JComboBox orientationsCombo;
	
	private boolean droppedComponent;
	
	private GCM2SBMLEditor gcm2sbml;
	private GCMFile gcm;
	
	// the public interface to this class.
	// returns true if at least 1 component was dropped.
	private static DropComponentPanel panel;
	public static boolean dropComponent(GCM2SBMLEditor gcm2sbml, GCMFile gcm, float mouseX, float mouseY){
		//if(panel == null)
		panel = new DropComponentPanel(gcm2sbml, gcm);

		panel.openGUI(mouseX, mouseY);
		return panel.droppedComponent;
	}
	
	private DropComponentPanel(GCM2SBMLEditor gcm2sbml, GCMFile gcm){
		super(new BorderLayout());
		
		this.gcm2sbml = gcm2sbml;
		this.gcm = gcm;
		
		JPanel tilingPanel = new JPanel(new GridLayout(2, 1));
		
		// radio button to enable tiling
		doTiling = new JRadioButton("Tile Component", false);
		doTiling.addActionListener(this);				
		JLabel note = new JLabel("Note: tiling does not create a grid and no diffusion will occur.");
		
		tilingPanel.add(doTiling);
		tilingPanel.add(note);
		
		this.add(tilingPanel, BorderLayout.CENTER);
		
		// panel that contains tiling options
		tilePanel = new JPanel(new GridLayout(4, 2));
		this.add(tilePanel, BorderLayout.SOUTH);
		
		JLabel l;
		
		// options that go in the tiling panel		
		l = new JLabel("Columns"); tilePanel.add(l);
		columnsChooser = new JTextField("6");
		tilePanel.add(columnsChooser);
		
		l = new JLabel("Rows"); tilePanel.add(l);
		rowsChooser = new JTextField("6");
		tilePanel.add(rowsChooser);
		
		l = new JLabel("Padding"); tilePanel.add(l);
		paddingChooser = new JTextField("20");
		tilePanel.add(paddingChooser);
		
		l = new JLabel("Orientation"); tilePanel.add(l);
		orientationsCombo = new JComboBox(ORIENTATIONS);
		orientationsCombo.setSelectedIndex(0);
		tilePanel.add(orientationsCombo);
		
		updateTilingEnabled();
	}

	
	private void openGUI(float mouseX, float mouseY){
		
		ArrayList<String> gcmList = gcm2sbml.getComponentsList();
		if(gcmList.size() == 0){
			JOptionPane.showMessageDialog(Gui.frame,
					"There aren't any other gcms to use as components."
							+ "\nCreate a new gcm or import a gcm into the project first.",
					"Add Another GCM To The Project", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Object selItem = componentCombo != null ? componentCombo.getSelectedItem() : null;
		if (componentCombo != null)	this.remove(componentCombo);
		componentCombo = new JComboBox(gcmList.toArray());
		componentCombo.setSelectedItem(selItem);
		this.add(componentCombo, BorderLayout.NORTH);
		
		String[] options = { GlobalConstants.OK, GlobalConstants.CANCEL };
		int value = JOptionPane.showOptionDialog(Gui.frame, this, "Add Component(s)",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		// user presses escape
		if(value == JOptionPane.OK_OPTION)
			applyComponents(mouseX, mouseY);
		else
			this.droppedComponent = false;
	}
	
	/**
	 * Adds the components to the GCM file. Does -NOT- update the
	 * biograph, make an undo point, or mark anything dirty.
	 */
	private void applyComponents(float mouseX, float mouseY){
		int rowCount, colCount, padding;
		String orientation;
		if(doTiling.isSelected()){
			orientation = (String)orientationsCombo.getSelectedItem();
			try{
				rowCount = Integer.parseInt(rowsChooser.getText());
				colCount = Integer.parseInt(columnsChooser.getText());
				padding =  Integer.parseInt(paddingChooser.getText());
			}catch(NumberFormatException e){
				JOptionPane.showMessageDialog(Gui.frame,
						"A number you entered could not be parsed.",
						"Invalid number format",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}else{
			rowCount = colCount = 1;
			padding = 0;
			orientation = DROP_ON_MOUSE_CLICK;
		}

		String comp = (String)componentCombo.getSelectedItem();
				
		float separationX = GlobalConstants.DEFAULT_COMPONENT_WIDTH + padding;
		float separationY = GlobalConstants.DEFAULT_COMPONENT_HEIGHT + padding;
		
		float topleftX=0;
		float topleftY=0;
		if(orientation.equals(TOPLEFT)){
			topleftX = padding;
			topleftY = padding;
		}else if(orientation.equals(DROP_ON_MOUSE_CLICK)){
			topleftX = mouseX; // - separationX * colCount/2 + padding/2;
			topleftY = mouseY; // - separationY * rowCount/2 + padding/2;
		}
		
		//sets location(s) for all of the tiled component(s)
		for(int row=0; row<rowCount; row++){
			for(int col=0; col<colCount; col++){
				Properties properties = new Properties();
				properties.put("gcm", comp); //comp is the name of the gcm that the component contains
				properties.setProperty("graphwidth", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_WIDTH));
				properties.setProperty("graphheight", String.valueOf(GlobalConstants.DEFAULT_COMPONENT_HEIGHT));
				properties.setProperty("graphx", String.valueOf(col * separationX + topleftX));
				properties.setProperty("graphy", String.valueOf(row * separationY + topleftY));
				
				GCMFile compGCM = new GCMFile(gcm.getPath());
				compGCM.load(gcm.getPath() + File.separator + comp);
				if (compGCM.getIsWithinCompartment()) {
					properties.setProperty("compartment","true");
				} else {
					properties.setProperty("compartment","false");
				}
				gcm.addComponent(null, properties);
			}
		}
		
		this.droppedComponent = true;
	}
	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		updateTilingEnabled();
	}
	
	/**
	 * updates the enabled property of all the components that are enabled and disabled with the tiling options.
	 */
	private void updateTilingEnabled(){
		boolean enabled = doTiling.isSelected();
				
		rowsChooser.setEnabled(enabled);
		columnsChooser.setEnabled(enabled);
		paddingChooser.setEnabled(enabled);
		orientationsCombo.setEnabled(enabled);
	}
	
}
