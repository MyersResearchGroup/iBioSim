package analysis.dynamicsim;
import graph.Graph;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.xml.stream.XMLStreamException;

import analysis.dynamicsim.hierarchical.HierarchicalSimulation;
import analysis.dynamicsim.hierarchical.methods.HierarchicalODERKSimulator;
import analysis.dynamicsim.hierarchical.methods.HierarchicalSSADirectSimulator;
import main.Gui;
import main.Log;


public class DynamicSimulation {
	
	//simulator type
	private String simulatorType;
	
	//the simulator object
	private Simulator simulator;
	private HierarchicalSimulation hSimulator;
	private boolean cancelFlag;	
	private boolean statisticsFlag;
	
	/**
	 * constructor; sets the simulator type
	 */
	public DynamicSimulation(String type) {
		
		simulatorType = type;
	}	
	
	public void simulate(String SBMLFileName, String rootDirectory, String outputDirectory, double timeLimit, 
			double maxTimeStep, double minTimeStep, long randomSeed, JProgressBar progress, double printInterval, 
			int runs, JLabel progressLabel, JFrame running, double stoichAmpValue, 
			String[] interestingSpecies, int numSteps, double relError, double absError, 
			String quantityType, Boolean genStats, JTabbedPane simTab, Log log) {
		
		String progressText = progressLabel.getText();
		statisticsFlag = genStats;
		
		try {
			
			progressLabel.setText("Generating Model . . .");
			running.setMinimumSize(new Dimension((progressLabel.getText().length() * 10) + 20, 
					(int) running.getSize().getHeight()));
			
			if (simulatorType.equals("cr"))
				simulator = new SimulatorSSACR(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies, quantityType);
			else if (simulatorType.equals("direct"))
				simulator = new SimulatorSSADirect(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies, quantityType);
			else if (simulatorType.equals("rk"))
				simulator = new SimulatorODERK(SBMLFileName, outputDirectory, timeLimit, 
						maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies, numSteps, relError, absError, quantityType);
			else if (simulatorType.equals("hierarchical-direct"))
				hSimulator = new HierarchicalSSADirectSimulator(SBMLFileName, rootDirectory, outputDirectory, timeLimit, 
						maxTimeStep, minTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies, quantityType);
			else if (simulatorType.equals("hierarchical-hybrid"))
			{
				
			}
			else if (simulatorType.equals("hierarchical-rk"))
				hSimulator = new HierarchicalODERKSimulator(SBMLFileName, rootDirectory, outputDirectory, timeLimit, 
						maxTimeStep, randomSeed, progress, printInterval, stoichAmpValue, running, 
						interestingSpecies, numSteps, relError, absError, quantityType);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
			return;
		}
		finally
		{
//			if(hSimulator != null)
//			{
//				hSimulator.deleteFiles();
//			}
		}
		double val1 = System.currentTimeMillis();
		for (int run = 1; run <= runs; ++run) {
			
			if (cancelFlag == true)
				break;
			
			progressLabel.setText(progressText.replace(" (" + (run - 1) + ")","") + " (" + run + ")");
			running.setMinimumSize(new Dimension((progressLabel.getText().length() * 10) + 20, 
					(int) running.getSize().getHeight()));
			if(simulator != null)
			{
				simulator.simulate();
				simulator.clear();
				hSimulator = null;
				if ((runs - run) >= 1)
					simulator.setupForNewRun(run + 1);
			}
			else if(hSimulator != null)
			{
				hSimulator.simulate();
				hSimulator.clear();
				simulator = null;
				if ((runs - run) >= 1)
					hSimulator.setupForNewRun(run + 1);
			}
//			//garbage collect every twenty-five runs
//			if ((run % 25) == 0)
//			{
//				System.gc();
//				System.runFinalization();
//			}
		}
		System.gc();
		System.runFinalization();
		
		double val2 = System.currentTimeMillis();
		if (log==null) {
			System.out.println("Simulation Time: " + (val2-val1)/1000);
		} else {
			log.addText("Simulation Time: " + (val2-val1)/1000);
		}
		if (cancelFlag == false && statisticsFlag == true) {
			
			progressLabel.setText("Generating Statistics . . .");
			running.setMinimumSize(new Dimension(200,100));
			
			try {
				if(simulator != null)
					simulator.printStatisticsTSD();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		if (simTab!=null) {
			for (int i = 0; i < simTab.getComponentCount(); i++) {
				if (simTab.getComponentAt(i).getName().equals("TSD Graph")) {
					if (simTab.getComponentAt(i) instanceof Graph) {
						((Graph) simTab.getComponentAt(i)).refresh();
					}
				}
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