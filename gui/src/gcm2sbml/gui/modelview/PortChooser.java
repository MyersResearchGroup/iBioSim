package gcm2sbml.gui.modelview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import biomodelsim.BioSim;

import gcm2sbml.parser.GCMFile;

public class PortChooser extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private PortChooser(BioSim biosim, HashMap<String, Properties> ports, String type){
		super(new BorderLayout());
		list = new JList(ports.keySet().toArray());
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setSelectedIndex(0);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		
		this.add(listScroller, BorderLayout.NORTH);
		int choice = JOptionPane.showOptionDialog(biosim.frame(), this, "Please Choose an " + type.toUpperCase() + " Port", JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, null, JOptionPane.OK_OPTION);
		if(choice == JOptionPane.CANCEL_OPTION){
			list.removeSelectionInterval(0, ports.size());
		}
	}
	
	private JList list;
	public String getSelectedValue(){
		return (String)list.getSelectedValue();
	}
	
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * given a gcm and either GlobalConstants.INPUT or GlobalConstants.OUTPUT,
	 * allow the user to select a port.
	 * @param gcmFilename
	 * @param type
	 * @return
	 */
	public static class NoPortException extends Exception{private static final long serialVersionUID = 1L;}
	public static String selectGCMPort(BioSim biosim, GCMFile gcm, Properties comp, String type) throws NoPortException{
		String fullPath = gcm.getPath() + File.separator + comp.getProperty("gcm");
		GCMFile compGCM = new GCMFile(gcm.getPath());
		compGCM.load(fullPath);
		HashMap<String, Properties> ports = compGCM.getPorts(type);
		if(ports.size() == 0)
			throw new NoPortException();
		PortChooser pc = new PortChooser(biosim, ports, type);
		return pc.getSelectedValue();
	}
	
	
	
}
