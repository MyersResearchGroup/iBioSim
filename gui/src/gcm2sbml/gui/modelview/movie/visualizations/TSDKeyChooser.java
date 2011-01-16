package gcm2sbml.gui.modelview.movie.visualizations;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.mxgraph.model.mxCell;

import biomodelsim.BioSim;

import parser.TSDParser;


/**
 * Allows the user to choose a sub-species inside a .tsd file that is part of a component.
 * @author Tyler
 *
 */
public class TSDKeyChooser extends JPanel {

	// anything matching the first string here will be displayed with the second.
	private static final String TSD_HIDE_STRING = "__";
	private static final String TSD_SHOW_STRING = ".";
	
	private String compName;
	private TSDParser tsdParser;
	
	private JList list;
	
	public TSDKeyChooser(String compName, TSDParser tsdParser, String preselectedValue) {
		super();
		
		if(tsdParser == null)
			throw new Error("tsdParser was null!");
		
		this.compName = compName;
		this.tsdParser = tsdParser;
		
		buildGUI(preselectedValue);
	}
	
	private void buildGUI(String preselectedValue){
		this.setLayout(new BorderLayout());
		
		tsdParser.getSpecies().toString();
		
		//JOptionPane.showMessageDialog(BioSim.frame, tsdParser.getSpecies().toString());

		int selectedIndex = 0;
		int index = 0;
		Vector<String> items = new Vector<String>();
		items.add("- None -");
		for(String s:tsdParser.getSpecies()){
			if(s.startsWith(compName + TSD_HIDE_STRING)){
				items.add(s.replace(TSD_HIDE_STRING, TSD_SHOW_STRING));
				index++;
				if(s.equals(preselectedValue))
					selectedIndex = index;
			}
		}
		
		list = new JList(items);
		list.setSelectedIndex(selectedIndex);
		list.setPreferredSize(new Dimension(300, 50));
		//list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		JScrollPane listScroller = new JScrollPane(list);
		this.add(listScroller, BorderLayout.CENTER);
	}
	
	/**
	 * returns the value of the currently selected TSD key or "" if none is selected.
	 * @return
	 */
	public String getSelectedKey(){
		if(list.getSelectedIndex() == 0)
			return null;
		String val = (String)list.getSelectedValue();
		return val.replace(TSD_SHOW_STRING, TSD_HIDE_STRING);
	}

}
