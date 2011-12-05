package analysis.dynamicsim;

import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JProgressBar;


public class DynamicGillespie {
	
	//simulator type
	private String simulatorType;
	
	//the simulator object
	Simulator simulator = null;
	
	
	/**
	 * constructor; sets the simulator type
	 */
	public DynamicGillespie(String type) {
		
		simulatorType = type;
	}	
	
	public void simulate(String SBMLFileName, String outputDirectory, double timeLimit, 
			double maxTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			int runs, JLabel progressLabel) {
		
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
			
			progressLabel.setText(progressLabel.getText().replace(" (" + (run-1) + ")","") + " (" + run + ")");
	
			simulator.simulate();
			simulator.clear();
			
			if ((runs - run) >= 1)
				simulator.setupForNewRun(run + 1);
			
//			//garbage collect every twenty-five runs
//			if ((run % 25) == 0)
//				System.gc();
		}
		
		try {
			simulator.printStatisticsTSD();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * cancels the simulation on the next iteration
	 * called from outside the class when the user closes the progress bar dialog
	 */
	public void cancel() {
		
		if (simulator != null)
			simulator.cancel();
	}
}


/*
IMPLEMENTATION NOTES:
	

if the top node of a reversible reaction isn't a minus sign, then give an error and exit
	--and there's some special time variable and whatnot


EVALUATION NOTES:

apparently minus can be non-binary?  (need to check this)
	--maybe just unary and binary


CONSTRAINTS:

you need to display the message if it fails?

*/