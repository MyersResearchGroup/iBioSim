package analysis.dynamicsim;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import main.Gui;


public class DynamicSimulation {
	
	//simulator type
	private String simulatorType;
	
	//the simulator object
	private Simulator simulator = null;
	
	private boolean cancelFlag = false;	
	
	/**
	 * constructor; sets the simulator type
	 */
	public DynamicSimulation(String type) {
		
		simulatorType = type;
	}	
	
	public void simulate(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			int runs, JLabel progressLabel, JFrame running, double stoichAmpValue, String[] interestingSpecies) {
		
		String progressText = progressLabel.getText();
		
		try {
			
			progressLabel.setText("Generating Model . . .");
			running.setMinimumSize(new Dimension((progressLabel.getText().length() * 10) + 20, 
					(int) running.getSize().getHeight()));
			
			if (simulatorType.equals("cr"))
				simulator = new SimulatorSSACR(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies);
			else if (simulatorType.equals("direct"))
				simulator = new SimulatorSSADirect(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies);
			else if (simulatorType.equals("rk"))
				simulator = new SimulatorODERK(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		for (int run = 1; run <= runs; ++run) {
			
			if (cancelFlag == true)
				break;
			
			progressLabel.setText(progressText.replace(" (" + (run - 1) + ")","") + " (" + run + ")");
			running.setMinimumSize(new Dimension((progressLabel.getText().length() * 10) + 20, 
					(int) running.getSize().getHeight()));
	
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