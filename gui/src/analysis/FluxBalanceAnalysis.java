package analysis;

import java.text.NumberFormat;

import main.Gui;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.sbml.libsbml.FbcModelPlugin;
import org.sbml.libsbml.FluxBound;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import com.joptimizer.solvers.*;
import com.joptimizer.optimizers.*;
import com.joptimizer.functions.*;
import com.joptimizer.util.*;

public class FluxBalanceAnalysis {
	
	private JOptimizer jop;
	
	private String sbmlFileName;
	
	private SBMLDocument sbml;
	
	private FbcModelPlugin fbc;
	
	private NumberFormat nf;
	
	private double[] arr;
	
	public FluxBalanceAnalysis(String sbmlFileName) {
		this.sbmlFileName = sbmlFileName;
		nf = NumberFormat.getNumberInstance();
		
		//Set number of digits after decimal point
		nf.setMaximumFractionDigits(4);
		
		nf.setGroupingUsed(false);
		//Set the array to be the max it will ever be
		arr=new double[(int) (2*(sbml.getModel().getNumSpecies() + sbml.getModel().getNumReactions()))];
	}
	
	public int PerformFluxBalanceAnalysis(){
		// Load the SBML file
		sbml = Gui.readSBML(sbmlFileName);
		fbc = (FbcModelPlugin)sbml.getModel().getPlugin("fbc");
		for (long i = 0; i < sbml.getModel().getNumSpecies(); i++) {
			Species species = sbml.getModel().getSpecies(i);
			System.out.println(species.getId());
		}
		for (long i = 0; i < fbc.getNumFluxBounds(); i++) {
			FluxBound bound = fbc.getFluxBound(i);
			System.out.println(bound.getReaction());
		}
		
		
		
		// Objective function (plane)
		LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(new double[] { -1., -1. }, 4);

		//inequalities (polyhedral feasible set G.X<H )
		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[4];
		double[][] G = new double[][] {{4./3., -1}, {-1./2., 1.}, {-2., -1.}, {1./3., 1.}};
		double[] H = new double[] {2., 1./2., 2., 1./2.};
		inequalities[0] = new LinearMultivariateRealFunction(G[0], -H[0]);
		inequalities[1] = new LinearMultivariateRealFunction(G[1], -H[1]);
		inequalities[2] = new LinearMultivariateRealFunction(G[2], -H[2]);
		inequalities[3] = new LinearMultivariateRealFunction(G[3], -H[3]);
		
		//optimization problem
		OptimizationRequest or = new OptimizationRequest();
		or.setF0(objectiveFunction);
		or.setFi(inequalities);
		//or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point, not mandatory
		or.setToleranceFeas(1.E-9);
		or.setTolerance(1.E-9);
		
		//optimization
		jop = new JOptimizer();
		jop.setOptimizationRequest(or);
		try {
			jop.optimize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double [] sol = jop.getOptimizationResponse().getSolution();
		System.out.println(nf.format(sol[0]));
		System.out.println(nf.format(sol[1]));
		return (int) sol[0];
	}

}
