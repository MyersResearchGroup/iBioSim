package gcm2sbml.gui.modelview.movie;

import gcm2sbml.gui.PropertyList;
import gcm2sbml.parser.GCMFile;

import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import biomodelsim.BioSim;

public class ComponentColorChooser extends JPanel {

	public ComponentColorChooser(String selected, PropertyList speciesList, PropertyList influencesList,
			PropertyList conditionsList, PropertyList componentsList, GCMFile gcm, boolean paramsOnly, GCMFile refGCM) {
		super(new GridLayout(7, 1));
		
		this.add(new JLabel("This is a test"));
		
	}
	
	private void openGUI(){
//		int value = JOptionPane.showOptionDialog(BioSim.frame, this, "Species Editor",
//				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
}
