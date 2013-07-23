package analysis;

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
	
	public FluxBalanceAnalysis(String sbmlFileName) {
		this.sbmlFileName = sbmlFileName;
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
//		RealMatrix Pmatrix = new Array2DRowRealMatrix(new double[][] {
//				{ 1.68, 0.34, 0.38 },
//				{ 0.34, 3.09, -1.59 }, 
//				{ 0.38, -1.59, 1.54 } });
//		RealVector qVector = new ArrayRealVector(new double[] { 0.018, 0.025, 0.01 });
//
//	    // Objective function.
//		double theta = 0.01522;
//		RealMatrix P = Pmatrix.scalarMultiply(theta);
//		RealVector q = qVector.mapMultiply(-1);
//		PDQuadraticMultivariateRealFunction objectiveFunction = new PDQuadraticMultivariateRealFunction(P.getData(), q.toArray(), 0);
//		
//		OptimizationRequest or = new OptimizationRequest();
//		or.setF0(objectiveFunction);
//		or.setInitialPoint(new double[] {0.04, 0.50, 0.46}); 
//		
//	    //optimization
//		NewtonUnconstrained opt = new NewtonUnconstrained();
//		opt.setOptimizationRequest(or);
//		int returnCode = 0;
//		try {
//			returnCode = opt.optimize();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//		System.out.println(returnCode);
//		return returnCode;
		
		
		
		
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
		int returnCode = 0;
		try {
			returnCode = jop.optimize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		double [] sol = jop.getOptimizationResponse().getSolution();
		return (int) sol[0];
	}

}
