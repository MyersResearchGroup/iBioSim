package analysis;

import java.text.NumberFormat;

import main.Gui;

import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.ArrayRealVector;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.sbml.libsbml.FbcModelPlugin;
import org.sbml.libsbml.FluxBound;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.Species;
import org.sbml.libsbml.SpeciesReference;

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
		//arr=new double[(int) (2*(sbml.getModel().getNumSpecies() + sbml.getModel().getNumReactions()))];
	}
	
	/**
	 * @return
	 */
	public int PerformFluxBalanceAnalysis(){
		// Load the SBML file
		sbml = Gui.readSBML(sbmlFileName);
		fbc = (FbcModelPlugin)sbml.getModel().getPlugin("fbc");
		if (fbc != null) {
			for (long i = 0; i < fbc.getNumObjectives(); i++) {
				String objective = "";
				// Construct an double array size of number of reactions
				for (long j = 0; j < fbc.getObjective(i).getNumFluxObjectives(); j++) {
					objective += fbc.getObjective(i).getFluxObjective(j).getCoefficient() + "*" + fbc.getObjective(i).getFluxObjective(j).getReaction() + " + ";
					// insert the coefficient in the array in the appropriate location for the reaction in this flux objective
				}
				System.out.println(objective);
				// [ 0 0 ... 1.0 ]
				// LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(MY_FLUX_COEFFICIENT_ARRAY, 0);
				
				// Construct set of inequalities which is number of flux bounds (+ locations for equal bounds) + 2*number of species
				//inequalities (polyhedral feasible set G.X<H )
				//ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[4];
				for (long j = 0; j < fbc.getNumFluxBounds(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					System.out.println(bound.getReaction() + " " + bound.getOperation() + " " + bound.getValue());
					// Create a vector R size number of reactions where the entry is 1.0 for the reaction for this bound and 0 otherwise
					// rSign/hSign is determined by the operation, <= +/-, >= -/+
					// inequalities[j] = new LinearMultivariateRealFunction(rSign * R, hSign * value);
				}
				for (long j = 0; j < sbml.getModel().getNumSpecies(); j++) {
					Species species = sbml.getModel().getSpecies(j);
					System.out.println(species.getId());
					// Construct a stoch vector size of number of reactions, init with 0's
					for (long k = 0; k < sbml.getModel().getNumReactions(); k++) {
						Reaction r = sbml.getModel().getReaction(k);
						for (long l = 0; l < r.getNumReactants(); l++) {
							SpeciesReference sr = r.getReactant(l);
							if (sr.getSpecies().equals(species.getId())) {
								System.out.println(r.getId() + ":" + (-1)*sr.getStoichiometry());
							}
						}
						// Do same for products
						// Insert (product stoch - reactant stoch) into appropriate location in the vector 
					}
					// inequalities[2*j + fbc.getNumFluxBounds()] = new LinearMultivariateRealFunction(stoch, 0);
					// inequalities[2*j + 1 + fbc.getNumFluxBounds()] = new LinearMultivariateRealFunction(-1*stoch, 0);
				}
//				//optimization problem
//				OptimizationRequest or = new OptimizationRequest();
//				or.setF0(objectiveFunction);
//				or.setFi(inequalities);
//				//or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point, not mandatory
//				or.setToleranceFeas(1.E-9);
//				or.setTolerance(1.E-9);
//				
//				//optimization
//				jop = new JOptimizer();
//				jop.setOptimizationRequest(or);
//				try {
//					jop.optimize();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				double [] sol = jop.getOptimizationResponse().getSolution();
				for (long j = 0; j < sbml.getModel().getNumReactions(); j++) {
					System.out.println(sbml.getModel().getReaction(j)); // + " = " + sol[j]
				}
			}
			return 0;
		} else {
			System.out.println("No flux balance constraints");
			return 0;
		}
		
//		// Objective function (plane)
//		LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(new double[] { -1., -1. }, 4);
//
//		//inequalities (polyhedral feasible set G.X<H )
//		ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[4];
//		double[][] G = new double[][] {{4./3., -1}, {-1./2., 1.}, {-2., -1.}, {1./3., 1.}};
//		double[] H = new double[] {2., 1./2., 2., 1./2.};
//		inequalities[0] = new LinearMultivariateRealFunction(G[0], -H[0]);
//		inequalities[1] = new LinearMultivariateRealFunction(G[1], -H[1]);
//		inequalities[2] = new LinearMultivariateRealFunction(G[2], -H[2]);
//		inequalities[3] = new LinearMultivariateRealFunction(G[3], -H[3]);
//		
//		//optimization problem
//		OptimizationRequest or = new OptimizationRequest();
//		or.setF0(objectiveFunction);
//		or.setFi(inequalities);
//		//or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point, not mandatory
//		or.setToleranceFeas(1.E-9);
//		or.setTolerance(1.E-9);
//		
//		//optimization
//		jop = new JOptimizer();
//		jop.setOptimizationRequest(or);
//		try {
//			jop.optimize();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		double [] sol = jop.getOptimizationResponse().getSolution();
//		System.out.println(nf.format(sol[0]));
//		System.out.println(nf.format(sol[1]));
//		return (int) sol[0];
	}

}
