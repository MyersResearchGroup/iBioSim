package frontend.biomodel.gui.schematic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


public class ListChooser extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private ListChooser(JFrame frame, Object[] list, String message){
		super(new BorderLayout());
		jlist = new JList(list);
		
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jlist.setLayoutOrientation(JList.VERTICAL);
		jlist.setVisibleRowCount(-1);
		jlist.setSelectedIndex(0);
		jlist.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					// TODO: Figure out how to 
					//throw new Error("BAH!");
				}
			}
		});
		
		
		JScrollPane listScroller = new JScrollPane(jlist);
		listScroller.setPreferredSize(new Dimension(250, 80));
		
		this.add(listScroller, BorderLayout.NORTH);
		int choice = JOptionPane.showOptionDialog(frame, this, message, JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, null, JOptionPane.OK_OPTION);
		if(choice == JOptionPane.CANCEL_OPTION){
			jlist.removeSelectionInterval(0, list.length);
		}
	}
	
	private JList jlist;
	public String getSelectedValue(){
		return (String)jlist.getSelectedValue();
	}


	/**
	 * given a list of strings, choose one. Throws EmptyListException if 
	 * the list is empty. Chooses the first item if there is only one.
	 * Otherwise, pops up a prompt to let the user choose one.
	 * 
	 * NOTE: biosim.frame() has a frame you can pass in.
	 * @param gcmFilename
	 * @param type
	 * @return
	 */
	public static class EmptyListException extends Exception{private static final long serialVersionUID = 1L;}
	public static String selectFromList(JFrame frame, Object[] list, String message) throws EmptyListException{
		if(list.length == 0)
			throw new EmptyListException();
		
		if(list.length == 1){
			return (String)list[0];
			
		}
		ListChooser pc = new ListChooser(frame, list, message);
		return pc.getSelectedValue();
	}
	
	
	
}
