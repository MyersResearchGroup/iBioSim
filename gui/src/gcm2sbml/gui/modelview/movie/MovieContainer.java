package gcm2sbml.gui.modelview.movie;

import gcm2sbml.gui.GCM2SBMLEditor;
import gcm2sbml.gui.modelview.ModelView;
import gcm2sbml.gui.modelview.Utils;
import gcm2sbml.parser.GCMFile;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import reb2sac.Reb2Sac;

import biomodelsim.BioSim;

public class MovieContainer extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private ModelView modelView;
	private Reb2Sac reb2sac;
	
	
	public MovieContainer(Reb2Sac reb2sac_, GCMFile gcm, BioSim biosim, GCM2SBMLEditor gcm2sbml){
		super(new BorderLayout());
		modelView = new ModelView(gcm, biosim, gcm2sbml, false);
		this.add(modelView, BorderLayout.CENTER);
		
		this.reb2sac = reb2sac_;
	}
	
	private boolean isUIInitialized;
	public void display(){
		modelView.display();

		if(isUIInitialized == false){
			chooseTSDFile();
			
			this.addUI();
			
			isUIInitialized = true;
		}
	}
	
	private String chooseTSDFile(){
		Vector<String> filenames = new Vector<String>();
		for (String s : new File(reb2sac.getSimPath()).list()) {
			if (s.endsWith(".tsd")) {
				filenames.add(s);
			}
		}
		return filenames.firstElement();
	}
	
	JButton playPauseButton;
	JButton rewindButton;
	JSlider slider;
	private void addUI(){
		JToolBar mt = new JToolBar();
		
		rewindButton = Utils.makeToolButton("", "rewind", "Rewind", this);
		mt.add(rewindButton);

		playPauseButton = Utils.makeToolButton("", "playpause", "Play", this);
		mt.add(playPauseButton);
		
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		mt.add(slider);

		this.add(mt, BorderLayout.SOUTH);
	}
	
	public void actionPerformed(ActionEvent event) {
		// TODO Auto-generated method stub
		String command = event.getActionCommand();
		
		if(command.equals("rewind")){
			
		}else if(command.equals("playpause")){
			
		}
	}

}
