package biomodel.gui.fba;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import main.Gui;

import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FluxBound;
import biomodel.parser.BioModel;

public class FBABounds extends JPanel implements ActionListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JList events; // JList of events

	private JList eventAssign; // JList of event assignments
	
	private BioModel bioModel;
	
	private FBCModelPlugin fbc;
	
	public FBABounds(BioModel bioModel,String reactionId) {
		super(new BorderLayout());
		this.bioModel = bioModel;
		fbc = bioModel.getSBMLFBC();
		
		JPanel eventPanel = new JPanel(new BorderLayout());
		
		String[] assign = new String[0];
		
		FluxBound fluxBound = new FluxBound();
		for(int i = 0; i < fbc.getListOfFluxBounds().size(); i++){
			if(fbc.getListOfFluxBounds().get(i).getReaction().equals(reactionId)){
				fluxBound = fbc.getListOfFluxBounds().get(i);
				System.out.println(fluxBound.getValue());
				System.out.println(fluxBound.getOperation());
				System.out.println(fluxBound.getReaction());
				System.out.println(reactionId);
				
				if(assign[0].isEmpty()){
					assign[0]=reactionId + "";
				}
			}
		}
		
		
		// TODO: populate the string array with flux bounds corresponding to this reactionId

		JList eventAssign = new JList();
		
		JPanel biggerPanel = new JPanel(new BorderLayout());
		JPanel smallerPanel = new JPanel();
		JButton addBound = new JButton("Add");
		JButton removeBound = new JButton("Remove");
		JButton editBound = new JButton("Edit");
		smallerPanel.add(addBound);
		smallerPanel.add(removeBound);
		smallerPanel.add(editBound);
		addBound.addActionListener(this);
		removeBound.addActionListener(this);
		editBound.addActionListener(this);
		JLabel eventAssignLabel = new JLabel("List of Flux Bounds:");
		eventAssign.removeAll();
		eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane();
		scroll.setMinimumSize(new Dimension(260, 220));
		scroll.setPreferredSize(new Dimension(276, 152));
		scroll.setViewportView(eventAssign);
		
		//Utility.sort(assign);
		eventAssign.setListData(assign);
		eventAssign.setSelectedIndex(0);
		eventAssign.addMouseListener(this);
		biggerPanel.add(eventAssignLabel, "North");
		biggerPanel.add(scroll, "Center");
		biggerPanel.add(smallerPanel, "South");
		
		eventPanel.add(biggerPanel, "South");
		Object[] options = { "Ok", "Cancel" };
		String title = "Flux Bounds Editor";
		int value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION && value != JOptionPane.YES_OPTION) {
			error = false;
			if (value == JOptionPane.YES_OPTION) {
				// TODO: remove flux bounds for reactionId
				// TODO: use Utility.getList to get the new bounds
				// TODO: for each new bound, create a new flux bound and populate it accordingly
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, eventPanel, title, JOptionPane.YES_NO_OPTION, 
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
//		
	}
	
	private void removeFluxBound(JList eventAssign) {
		int index = eventAssign.getSelectedIndex();
		if (index != -1) {
			eventAssign.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			//Utility.remove(eventAssign);
			eventAssign.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			if (index < eventAssign.getModel().getSize()) {
				eventAssign.setSelectedIndex(index);
			}
			else {
				eventAssign.setSelectedIndex(index - 1);
			}
		}
	}
	
	private void fluxBoundEditor(JList eventAssign, String option) {
		if (option.equals("OK") && eventAssign.getSelectedIndex() == -1) {
			JOptionPane.showMessageDialog(Gui.frame, "No event assignment selected.", "Must Select an Event Assignment", JOptionPane.ERROR_MESSAGE);
			return;
		}
		/* TODO: BUILD YOUR OBJECTIVE PANEL HERE */
		JPanel evPanel = new JPanel(new GridLayout(1, 2));
		
		JLabel objectiveLabel = new JLabel("Bound:");
		
		JTextField objective = new JTextField(12);
		
		evPanel.add(objectiveLabel);
		evPanel.add(objective);
		
		//EAPanel.add(eqn);
		Object[] options = { option, "Cancel" };
		String title = "Flux Bound Editor";
		int value = JOptionPane.showOptionDialog(Gui.frame, evPanel, title, JOptionPane.YES_NO_OPTION, 
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION && value != JOptionPane.YES_OPTION) {
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, evPanel, title, JOptionPane.YES_NO_OPTION, 
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
	}
		


	public void actionPerformed(ActionEvent e) {
		if (((JButton) e.getSource()).getText().equals("Add")) {
			fluxBoundEditor(eventAssign, "Add");
		}
		// if the edit event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Edit")) {
			fluxBoundEditor(eventAssign, "OK");
		}
		// if the remove event assignment button is clicked
		else if (((JButton) e.getSource()).getText().equals("Remove")) {
			removeFluxBound(eventAssign);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
