/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package edu.utah.ece.async.ibiosim.conversion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

import edu.utah.ece.async.ibiosim.dataModels.biomodel.util.SBMLutilities;

/**
 * Perform conversion from SBML to PRISM.
 *
 * @author Lukas Buecherl
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim
 *         Contributors </a>
 * @version %I%
 */

public class SBML2PRISM {

	/*
	 * Convert SBML to Prism. Input: SBMLDocument, File, bound. Output: void
	 * 
	 * Function takes in a SBML document and writes the prism conversion of the file
	 * in the same directory. The function also translates the constraint into a
	 * properties file that is written in the same directory in a separate file. The
	 * original filename is used for the prism and property file. The SBMML file
	 * ends in .xml, the prism file in .sm, and the properties file in .props.
	 * 
	 * To run the converter use the following command: java -jar
	 * conversion/target/iBioSim-conversion-3.1.0-SNAPSHOT-jar-with-dependencies.jar
	 * -l PRISM YOURSBMLFILE.xml
	 * 
	 * The function also allows the translation into a bound model by simply adding
	 * the flag -bound
	 */

	public static void convertSBML2PRISM(SBMLDocument sbmlDoc, String filename, boolean unbound) throws IOException {
		Model model = sbmlDoc.getModel();
		File file = new File(filename.replace(".xml", ".sm"));

		// Opening and writing preamble to the file
		FileWriter out = new FileWriter(file);
		out.write("// File generated by SBML-to-PRISM converter\n");
		out.write("// Original file: " + filename + "\n");
		out.write("// @GeneticLogicLab\n");
		out.write("\n");
		out.write("ctmc\n");
		out.write("\n");

		// Set bound limit if bound model is selected
		if (!unbound) {

			System.err.println("Under Development");
			/*
			 * double maxAmount = 0.0; for (int i = 0; i < model.getSpeciesCount(); i++) {
			 * Species species = model.getSpecies(i); if (species.getInitialAmount() >
			 * maxAmount) { maxAmount = species.getInitialAmount(); } }
			 * out.write(" const int MAX_AMOUNT = " + (int) maxAmount + ";\n");
			 */
		}

		// Declaration of new Compartment list
		ArrayList<Compartment> compartmentList = new ArrayList<Compartment>();

		// Iterating over Compartments
		for (int i = 0; i < model.getCompartmentCount(); i++) {
			Compartment compartment = model.getCompartment(i);
			compartmentList.add(compartment);
		}

		// System.err.println("Compartment List: " + compartmentList + "\n");

		// Identify compartments and their size
		if (!compartmentList.isEmpty()) {
			out.write("// Compartment size\n");
		}

		for (int i = 0; i < compartmentList.size(); i++) {
			String id = checkReservedKeywordPrism(compartmentList.get(i).getId());
			// System.err.println("Id: " + id);
			Double size = compartmentList.get(i).getSize();
			// System.err.println("Size: " + size);
			if (!Double.isNaN(size)) {
				out.write("const double " + id + " = " + size + ";\n");
			}
		}

		// Identify model parameters
		// Declaration of new parameter list
		ArrayList<Parameter> parameterList = new ArrayList<Parameter>();

		// Iterating over Compartments
		for (int i = 0; i < model.getParameterCount(); i++) {
			Parameter parameter = model.getParameter(i);
			parameterList.add(parameter);
		}

		// System.err.println("Parameter List: " + parameterList + "\n");

		if (!parameterList.isEmpty()) {
			out.write("\n");
			out.write("// Model parameters\n");
		}

		for (int i = 0; i < parameterList.size(); i++) {
			String id = checkReservedKeywordPrism(parameterList.get(i).getId());
			Double value = parameterList.get(i).getValue();
			String name = parameterList.get(i).getName();

			if (!Double.isNaN(value)) {
				out.write("const double " + id + " = " + value + "; // " + name + "\n"); // if not null name
			}
		}

		// Identify reactions
		// Declaration of new reaction list
		ArrayList<Reaction> reactionList = new ArrayList<Reaction>();
		// Iterating over reactions
		for (int i = 0; i < model.getReactionCount(); i++) {
			Reaction reaction = model.getReaction(i);
			reactionList.add(reaction);
		}

		// System.err.println("Reaction List: " + reactionList + "\n");

		// Identify local model parameters
		// Declaration of new local parameter list
		ArrayList<LocalParameter> localParameterList = new ArrayList<LocalParameter>();
		// Iterating over local parameters
		for (int i = 0; i < reactionList.size(); i++) {
			for (int j = 0; j < reactionList.get(i).getKineticLaw().getLocalParameterCount(); j++) {
				LocalParameter localparameter = reactionList.get(i).getKineticLaw().getLocalParameter(j);
				localParameterList.add(localparameter);
			}
		}

		// while (localParameterList.remove(null));
		// System.err.println("LocalParameterList: " + localParameterList + "\n");

		ArrayList<LocalParameter> UpdatedlocalParameterList = new ArrayList<LocalParameter>();
		for (int i = 0; i < reactionList.size(); i++) {
			for (int j = 0; j < reactionList.get(i).getKineticLaw().getLocalParameterCount(); j++) {
				LocalParameter localparameter = reactionList.get(i).getKineticLaw().getLocalParameter(j);
				// System.err.println("localparameter: " + localparameter);
				if (localparameter != null) {
					if (localParameterList.contains(localparameter)) {
						reactionList.get(i).getKineticLaw().getLocalParameter(j)
								.setId("local_" + localparameter.getId() + "_" + i);
						// System.err.println(
						// "localparameter: " + reactionList.get(i).getKineticLaw().getLocalParameter(j)
						// + "\n");
						UpdatedlocalParameterList.add(reactionList.get(i).getKineticLaw().getLocalParameter(j));
					}
				}
			}
		}

		for (int i = 0; i < reactionList.size(); i++) {
			for (int j = 0; j < reactionList.get(i).getKineticLaw().getLocalParameterCount(); j++) {
				// System.err.println("Reaction local parameter updated: "
				// + reactionList.get(i).getKineticLaw().getLocalParameter(j));
			}
		}

		if (!UpdatedlocalParameterList.isEmpty()) {
			out.write("\n");
			out.write("// Model local parameters\n");
		}

		for (int i = 0; i < UpdatedlocalParameterList.size(); i++) {
			LocalParameter localparameter = UpdatedlocalParameterList.get(i);
			String id = checkReservedKeywordPrism(localparameter.getId());
			Double value = localparameter.getValue();
			String name;
			if (localparameter.getName() != null) {
				name = localparameter.getName();
			} else {
				name = id;
			}
			out.write("const double " + id + " = " + value + "; // " + name + "\n"); // if not null name }

		}

		// System.err.println("Species Count: " + model.getSpeciesCount());
		// System.err.println("Reaction Count: " + model.getReactionCount());

		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < reactionList.size(); i++) {
			Reaction reaction = model.getReaction(i);
			String SumReactant = "";
			int reaStoch;
			// System.err.println("Reaction: " + reaction);
			// System.err.println("ReactantCount: " + reaction.getReactantCount());

			for (int j = 0; j < reaction.getReactantCount(); j++) {
				// System.err.println("Species: " + reaction.getReactant(j).getSpecies());
				reaStoch = (int) reaction.getReactant(j).getStoichiometry() - 1;
				// System.err.println("Stochiometry: " + reaStoch);
				if (reaStoch == 0) {
					SumReactant = SumReactant.concat(reaction.getReactant(j).getSpecies() + " >= " + reaStoch);
				} else {
					SumReactant = SumReactant.concat(reaction.getReactant(j).getSpecies() + " > " + reaStoch);
				}

				if (j < reaction.getReactantCount() - 1) {
					SumReactant = SumReactant.concat(" & ");
				}

				// System.err.println(SumReactant);

			}

			map.put(reaction.getId(), SumReactant);

			SumReactant = "";

		}

		// System.err.println(map);
		// System.err.println(map.get("R0"));

		// Identify model species
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			// Write out syntax
			// For function checkReservedKeywordPrism see below
			Species species = model.getSpecies(i);
			String id = checkReservedKeywordPrism(species.getId());
			int inAmount = (int) species.getInitialAmount();

			out.write("\n");
			out.write("// Species " + id + "\n");

			if (unbound) {
				out.write("module " + id + "\n");
				out.write("\n");
				out.write("  " + id + "  :  " + "int init " + inAmount + ";\n");
				out.write("\n");
			} else {
				System.err.println("Under Development");
				/*
				 * out.write("const int " + id + "_MAX = MAX_AMOUNT;\n"); out.write("module " +
				 * id + "\n"); out.write("\n"); out.write("  " + id + " : " + "[0.." + id +
				 * "_MAX] init " + inAmount + ";\n"); out.write("  // " + id + "  :  " +
				 * "int init " + inAmount + ";\n"); out.write("\n");
				 */
			}

			for (int j = 0; j < reactionList.size(); j++) {
				Reaction reaction = reactionList.get(j);
				// Identify reactants and products
				SpeciesReference reactant = reaction.getReactantForSpecies(species.getId());
				SpeciesReference product = reaction.getProductForSpecies(species.getId());
				String reactionId = checkReservedKeywordPrism(reaction.getId());
				String speciesId = checkReservedKeywordPrism(species.getId());
				int stochi;

				if (reactant != null && product != null) {

					int reaStoch = (int) reactant.getStoichiometry();
					int proStoch = (int) product.getStoichiometry();
					// System.err.println("Check 1");
					stochi = proStoch - reaStoch;

					out.write("		// " + reactionId + "\n");

					if (stochi >= 0) {
						out.write("		[" + reactionId + "] ");
						if (map.get(reactionId) != null) {
							out.write(map.get(reactionId));
						}
						out.write(" -> (" + speciesId + "\' = " + speciesId + " + " + stochi + ");\n");
					} else {
						out.write("		[" + reactionId + "] " + speciesId + " >  0 -> (" + speciesId + "\' = "
								+ speciesId + " - " + Math.abs(stochi) + ");\n");
					}

				} else if (reactant == null && product != null) {

					int proStoch = (int) product.getStoichiometry();
					int reaStoch = 0;
					// System.err.println("Check 2");
					stochi = proStoch - reaStoch;

					out.write("		// " + reactionId + "\n");
					out.write("		[" + reactionId + "] ");

					// System.err.println(reactionId);
					// System.err.println(map.get(reactionId));
					if (map.get(reactionId) != null && !"".equals(map.get(reactionId))) {
						// System.err.println(map.get(reactionId) );
						out.write(map.get(reactionId) + " -> (");
					} else if ("".equals(map.get(reactionId))) {
						out.write(speciesId + " >= 0 -> (");
					}
					out.write(speciesId + "\' = " + speciesId + " + " + stochi + ");\n");
				} else if (reactant != null && product == null) {
					int proStoch = 0;
					int reaStoch = (int) reactant.getStoichiometry();
					// System.err.println("Check 3");
					stochi = proStoch - reaStoch;
					out.write("		// " + reactionId + "\n");
					out.write("		[" + reactionId + "] ");
					if (map.get(reactionId) != null) {
						out.write(map.get(reactionId));
					}
					out.write(" -> (" + speciesId + "\' = " + speciesId + " - " + Math.abs(stochi) + ");\n");
				}
			}

			out.write("\n");
			out.write("endmodule\n");
		}

		// Identify reaction rate
		out.write("\n");
		out.write("// Reaction rates\n");
		out.write("module reaction_rates\n");
		out.write("\n");

		for (int i = 0; i < reactionList.size(); i++) {
			Reaction reaction = reactionList.get(i);
			String reactionId = checkReservedKeywordPrism(reaction.getId());

			// Write state transitions
			out.write("		// " + reactionId + ": ");

			for (int j = 0; j < reaction.getReactantCount(); j++) {
				if ((int) reaction.getReactant(j).getStoichiometry() > 1) {
					out.write((int) reaction.getReactant(j).getStoichiometry() + " ");
				}
				// System.err.println(checkReservedKeywordPrism(reaction.getReactant(j).getSpecies()));
				out.write(checkReservedKeywordPrism(reaction.getReactant(j).getSpecies()));
				if (j < reaction.getReactantCount() - 1) {
					out.write(" + ");
				}
			}
			out.write(" -> ");

			for (int j = 0; j < reaction.getProductCount(); j++) {
				if ((int) reaction.getProduct(j).getStoichiometry() > 1) {
					out.write((int) reaction.getProduct(j).getStoichiometry() + " ");
				}
				out.write(checkReservedKeywordPrism(reaction.getProduct(j).getSpecies()));
				if (j < reaction.getProductCount() - 1) {
					out.write(" + ");
				}

			}

			out.write("\n");

			String math = checkReserveKeywordMath(
					SBMLutilities.convertMath2PrismProperty(reaction.getKineticLaw().getMath()), model);

			// System.err.println(math);
			// System.err.println(localParameterList);

			for (int l = 0; l < reaction.getKineticLaw().getLocalParameterCount(); l++) {
				String locPara = reaction.getKineticLaw().getLocalParameter(l).getId();
				math = math.replace(locPara.replace("local_", "").replace("_" + i, ""), locPara);
			}

			// System.err.println(math);

			// Get the math for the reaction rate
			out.write("		[" + reactionId + "] " + math + " > 0 -> " + math + " : true;\n");
			out.write("\n");
		}

		out.write("endmodule\n");

		// Identify rewards
		out.write("\n");
		out.write("// Reward structures (one per species)");
		out.write("\n");

		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);
			String speciesId = checkReservedKeywordPrism(species.getId().replace(" ", ""));

			out.write("// Reward " + (i + 1) + ": " + speciesId + "\n");
			out.write("rewards " + "\"" + speciesId + "\" true : " + speciesId + "; endrewards\n");

		}

		out.close();

		// writePRISMProperty(filename, model);

	}

	/*
	 * Writes PRISM property Input: (String) filename, (Model) model Output: void
	 * 
	 * Function checks the constraints of a SBML model and translates it into the
	 * PRISM syntax. The properties are then written into a .props file.
	 */
	private static void writePRISMProperty(String filename, Model model) throws IOException {
		// Write Properties File
		File property = new File(filename.replace(".xml", ".props"));
		FileWriter property_out = new FileWriter(property);

		// Property preamble
		property_out.write("// File generated by SBML-to-PRISM converter\n");
		property_out.write("// Original file: " + filename + "\n");
		property_out.write("// @GeneticLogicLab\n");
		property_out.write("\n");

		for (int i = 0; i < model.getConstraintCount(); i++) {
			// Get and write translation of constraint
			property_out.write(checkReserveKeywordMath(
					SBMLutilities.convertMath2PrismProperty(model.getConstraint(i).getMath()), model));
		}

		property_out.close();
	}

	/*
	 * Check for reserved keywords as species names Input: (String) NameOfSpecies
	 * Output: (String) _NameOfSpecies
	 * 
	 * Function checks if the name of a species is also a reserved keyword in the
	 * prism language. If that is the case, the species name is replaced by the
	 * species name lead by an underscore.
	 */
	private static String checkReservedKeywordPrism(String speciesname) {
		// List of reserved keywords in the prism language
		List<String> keywords = Arrays.asList("A", "bool", "clock", "const", "ctmc", "C", "double", "dtmc", "E",
				"endinit", "endinvariant", "endmodule", "endobservables", "endrewards", "endsystem", "false", "formula",
				"filter", "func", "F", "global", "G", "init", "invariant", "I", "int", "label", "max", "mdp", "min",
				"module", "X", "nondeterministic", "observable", "observables", "of", "Pmax", "Pmin", "P", "pomdp",
				"popta", "probabilistic", "prob", "pta", "rate", "rewards", "Rmax", "Rmin", "R", "S", "stochastic",
				"system", "true", "U", "W");

		// Check if species name is in the list
		if (keywords.contains(speciesname)) {
			return "_" + speciesname;
		} else {
			return speciesname;
		}
	}

	/*
	 * Check for reserved keywords as species names in math properties Input:
	 * (String) NameOfSpecies, model sbmlDoc.getModel(); Output: (String)
	 * _NameOfSpecies
	 * 
	 * Function checks if the name of a species in a math function is also a
	 * reserved keyword in the prism language. If that is the case, the species name
	 * in the function is replaced by the species name lead by an underscore.
	 */
	private static String checkReserveKeywordMath(String math, Model model) {

		// List of reserved keywords in the prism language
		List<String> keywords = Arrays.asList("A", "bool", "clock", "const", "ctmc", "C", "double", "dtmc", "E",
				"endinit", "endinvariant", "endmodule", "endobservables", "endrewards", "endsystem", "false", "formula",
				"filter", "func", "F", "global", "G", "init", "invariant", "I", "int", "label", "max", "mdp", "min",
				"module", "X", "nondeterministic", "observable", "observables", "of", "Pmax", "Pmin", "P", "pomdp",
				"popta", "probabilistic", "prob", "pta", "rate", "rewards", "Rmax", "Rmin", "R", "S", "stochastic",
				"system", "true", "U", "W");

		// Declaration of new string list
		ArrayList<String> speciesString = new ArrayList<String>();

		// Iterating over species, if species name is a reserved keyword add it to list
		// speciesString
		for (int i = 0; i < model.getSpeciesCount(); i++) {
			Species species = model.getSpecies(i);

			if (keywords.contains(species.getId())) {
				speciesString.add(species.getId());
			}

		}

		// System.err.println(speciesString);

		// Iterate over list and replace species name
		for (int i = 0; i < keywords.size(); i++) {
			if (speciesString.contains(keywords.get(i))) {
				// Replace species in math equations
				// In equations species names are lead by a space
				// String Target = " " + keywords.get(i);
				// System.err.println("Target " + Target);
				// System.err.println(math);
				// math = math.replace("(?<!\\S)" + keywords.get(i) + "(?!\\S)", "_" +
				// keywords.get(i));
				math = math.replace(" " + keywords.get(i) + " ", " _" + keywords.get(i));
				math = math.replace(" " + keywords.get(i) + ")", " _" + keywords.get(i) + ")");
				math = math.replace("(" + keywords.get(i) + " ", "( _" + keywords.get(i));
				// System.err.println(math);

				// Replace species names in property files
				// In property files species names are lead by a (
				// String TargetProperty = "(" + keywords.get(i);
				// math = math.replace(TargetProperty, "(_" + keywords.get(i));
			}

		}

		return math;
	}

}
