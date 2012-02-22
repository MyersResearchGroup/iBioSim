package analysis.dynamicsim;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import main.Gui;


public class DynamicGillespie {
	
	//simulator type
	private String simulatorType;
	
	//the simulator object
	private Simulator simulator = null;
	
	private boolean cancelFlag = false;
	
	
	/**
	 * constructor; sets the simulator type
	 */
	public DynamicGillespie(String type) {
		
		simulatorType = type;
	}	
	
	public void simulate(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			int runs, JLabel progressLabel, JFrame running) {
		
		try {
			
			if (simulatorType.equals("cr"))
				simulator = new SimulatorSSACR(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, randomSeed, progress, printInterval);
			else if (simulatorType.equals("direct"))
				simulator = new SimulatorSSADirect(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, randomSeed, progress, printInterval);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int run = 1; run <= runs; ++run) {
			
			if (cancelFlag == true)
				break;
			
			progressLabel.setText(progressLabel.getText().replace(" (" + (run - 1) + ")","") + " (" + run + ")");
			running.setMinimumSize(new Dimension((progressLabel.getText().length() * 10) + 30, (int) running.getSize().getHeight()));
	
			simulator.simulate();
			simulator.clear();
			
			if ((runs - run) >= 1)
				simulator.setupForNewRun(run + 1);
			
//			//garbage collect every twenty-five runs
//			if ((run % 25) == 0)
//				System.gc();
		}
		
		if (cancelFlag == false) {
			
			progressLabel.setText("Generating Statistics . . .");
			running.setMinimumSize(new Dimension(200,100));
			
			try {
				simulator.printStatisticsTSD();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * cancels the simulation on the next iteration
	 * called from outside the class when the user closes the progress bar dialog
	 */
	public void cancel() {
		
		if (simulator != null) {
			
			JOptionPane.showMessageDialog(Gui.frame, "Simulation Canceled",
					"Canceled", JOptionPane.ERROR_MESSAGE);
			
			simulator.cancel();
			
			cancelFlag = true;
		}
	}
}


/*
IMPLEMENTATION NOTES:
	

if the top node of a reversible reaction isn't a minus sign, then give an error and exit


CONSTRAINTS:

you need to display the message if it fails?

*/