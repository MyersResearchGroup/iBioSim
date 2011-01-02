package gcm2sbml.gui.modelview.movie.visualizations.component;

import gcm2sbml.gui.modelview.movie.MovieContainer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.ui.tabbedui.VerticalLayout;

import biomodelsim.BioSim;

public class ComponentSchemeChooser extends JPanel implements ActionListener {

	private final String ADD_ANOTHER = "add_another";
	
	private ComponentScheme componentScheme;
	private MovieContainer movieContainer;
	private String compName;
	
	public ComponentSchemeChooser(String compName, MovieContainer movieContainer) {
		super(new BorderLayout());

		this.compName = compName;
		this.componentScheme = movieContainer.getMoviePreferences().getComponentSchemeForComponent(compName);
		this.movieContainer = movieContainer;
		
		buildGUI();
		openGUI();
	}
	
	private JPanel schemeHolder;
	
	private void buildGUI(){
		
		schemeHolder = new JPanel(new VerticalLayout());
		this.add(schemeHolder, BorderLayout.CENTER);
		
		ListIterator<ComponentSchemePart> iter = componentScheme.getSchemes().listIterator();
		
		while(iter.hasNext()){
			schemeHolder.add(
					new ComponentSchemePartChooser(
							iter.next(),
							compName,
							movieContainer.getTSDParser()
							)
					);
		}
	
//		JButton moreButton = new JButton("Add Another Scheme");
//		moreButton.setActionCommand(ADD_ANOTHER);
//		moreButton.addActionListener(this);
//		this.add(moreButton, BorderLayout.SOUTH);
		
	}
	
	private void openGUI(){
		int value = JOptionPane.showOptionDialog(BioSim.frame, this, "Scheme for Component",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		
		if (value == JOptionPane.OK_OPTION) {
			for(Object o:schemeHolder.getComponents()){
				((ComponentSchemePartChooser)o).saveChanges();
			}
		}	
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(ADD_ANOTHER)) {
			if(componentScheme.getSchemes().size() < 3){
				//addSchemePart(componentScheme.getNewAtEnd());
			}
		}
	}
	
//	private void addSchemePart(ComponentSchemePart csp){
//		schemeHolder.add(
//				new ComponentSchemePartChooser(csp)
//				);
//
//		schemeHolder.updateUI();
//		this.updateUI();
//	}
}
