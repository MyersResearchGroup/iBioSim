package analysis;

import java.text.NumberFormat;
import java.util.HashMap;

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
	
	
	public FluxBalanceAnalysis(String sbmlFileName) {
		// Load the SBML file
		this.sbmlFileName = sbmlFileName;
		sbml = Gui.readSBML(sbmlFileName);
		fbc = (FbcModelPlugin)sbml.getModel().getPlugin("fbc");
		
		//Set number of digits after decimal point
		nf = NumberFormat.getNumberInstance();		
		nf.setMaximumFractionDigits(4);
		nf.setGroupingUsed(false);
	}
	
	/**
	 * @return
	 */
	public int PerformFluxBalanceAnalysis(){
		// Fill the objective array
		if (fbc != null) {
			for (long i = 0; i < fbc.getNumObjectives(); i++) {
				HashMap<String, Integer> reactionIndex = new HashMap<String, Integer>();
				int kp = 0;
				for(int l =0;l<fbc.getNumFluxBounds();l++){
					if(!reactionIndex.containsKey(fbc.getFluxBound(l).getReaction())){
						reactionIndex.put(fbc.getFluxBound(l).getReaction(), kp);
						kp++;
					}
				}
				System.out.println(reactionIndex);
				
				//Set the array to be the max it will ever be, Min & max for each + possible reversible reactions
				double [][] arr = new double [(int) sbml.getModel().getNumSpecies()][(int) (2*(sbml.getModel().getNumSpecies() + 2*(sbml.getModel().getNumReactions())))];
				double [] objective = new double[(int) (4*(sbml.getModel().getNumSpecies() + (sbml.getModel().getNumReactions())))];
				for (int j = 0; j < fbc.getObjective(i).getNumFluxObjectives(); j++) {
					objective [(int) fbc.getObjective(i).getFluxObjective(j).getColumn()] = fbc.getObjective(i).getFluxObjective(j).getCoefficient();
					System.out.println(fbc.getObjective(i).getFluxObjective(j).getReaction() + " " + fbc.getObjective(i).getFluxObjective(j).getColumn());
					// Insert the coefficient in the array in the appropriate location for the reaction in this flux objective
				}
				String printTest = "";
				for (int j = 0; j < objective.length; j++) {
					printTest +=objective[j];
				}				
				System.out.println(printTest);
				// [ 0 0 ... 1.0 ]
				LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(objective, 0);
				
				// Construct set of inequalities which is number of flux bounds (+ locations for equal bounds) + 2*number of species
				//inequalities (polyhedral feasible set G.X<H )
				ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[(int) (2*(sbml.getModel().getNumSpecies() + 2*(sbml.getModel().getNumReactions())))];
				System.out.println(fbc.getNumFluxBounds());
				for (long j = 0; j < fbc.getNumFluxBounds(); j++) {
					FluxBound bound = fbc.getFluxBound(j);
					double R [] = new double [reactionIndex.size()];
					if(reactionIndex.containsKey(bound.getReaction())){
						R[reactionIndex.get(bound.getReaction())]=1;
					}
					
					String secondTest = "";
					for (int k = 0; k < R.length; k++) {
						secondTest +=R[k];
					}				
					System.out.println(secondTest);
					
					if(bound.getOperation().equals("greaterEqual")){
						for(int k = 0; k<R.length;k++){
							R[k] = -1*R[k];
						}
						inequalities[(int) j] = new LinearMultivariateRealFunction(R, bound.getValue());
					}
					
					else if(bound.getOperation().equals("lessEqual")){
						inequalities[(int) j] = new LinearMultivariateRealFunction(R, -1* bound.getValue());
					}
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
						for (long l = 0; l < r.getNumProducts(); l++) {
							SpeciesReference sr = r.getProduct(l);
							if (sr.getSpecies().equals(species.getId())) {
								System.out.println(r.getId() + ":" + sr.getStoichiometry());
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