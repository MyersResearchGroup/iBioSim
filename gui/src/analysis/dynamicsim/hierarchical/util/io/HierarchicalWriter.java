package analysis.dynamicsim.hierarchical.util.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import analysis.dynamicsim.hierarchical.simulator.HierarchicalObjects.ModelState;

public class HierarchicalWriter
{

	public static void printAllToTSD(double printTime, BufferedWriter bufferedWriter, ModelState topmodel, Map<String, ModelState> submodels,
			Map<String, Double> replacements) throws IOException
	{
		String commaSpace = "";

		bufferedWriter.write("(");

		commaSpace = "";

		// print the current time
		bufferedWriter.write(printTime + ",");

		// loop through the speciesIDs and print their current value to the file
		for (String speciesID : topmodel.getSpeciesIDSet())
		{

			bufferedWriter.write(commaSpace + topmodel.getVariableToValue(replacements, speciesID));
			commaSpace = ",";

		}

		for (String noConstantParam : topmodel.getVariablesToPrint())
		{

			bufferedWriter.write(commaSpace + topmodel.getVariableToValue(replacements, noConstantParam));
			commaSpace = ",";

		}

		for (ModelState models : submodels.values())
		{
			for (String speciesID : models.getSpeciesIDSet())
			{
				if (!models.getIsHierarchical().contains(speciesID))
				{
					bufferedWriter.write(commaSpace + models.getVariableToValue(replacements, speciesID));
					commaSpace = ",";
				}
			}

			for (String noConstantParam : models.getVariablesToPrint())
			{
				if (!models.getIsHierarchical().contains(noConstantParam))
				{
					bufferedWriter.write(commaSpace + models.getVariableToValue(replacements, noConstantParam));
					commaSpace = ",";
				}
			}
		}

		bufferedWriter.write(")");
		bufferedWriter.flush();
	}

	public static void printInterestingToTSD(BufferedWriter bufferedWriter, double printTime, ModelState topmodel, Map<String, ModelState> submodels,
			Map<String, Double> replacements, String[] interesting, Set<String> printConcentrations) throws IOException
	{

		String commaSpace = "";

		bufferedWriter.write("(");

		commaSpace = "";

		// print the current time
		bufferedWriter.write(printTime + ",");

		double temp;
		// loop through the speciesIDs and print their current value to the file

		for (String s : interesting)
		{
			String element = s.replaceAll("(.+)__", "");
			String id = s.replace("__" + element, "");
			ModelState ms = (id.equals(s)) ? topmodel : submodels.get(id);
			if (printConcentrations.contains(s))
			{
				temp = ms.getVariableToValue(replacements, ms.getSpeciesToCompartmentNameMap().get(element));
				bufferedWriter.write(commaSpace + ms.getVariableToValue(replacements, element) / temp);
				commaSpace = ",";
			}
			else
			{
				bufferedWriter.write(commaSpace + ms.getVariableToValue(replacements, element));
				commaSpace = ",";
			}
		}

		bufferedWriter.write(")");
		bufferedWriter.flush();
	}

	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException
	 */
	public static void printToTSD(BufferedWriter bufferedWriter, ModelState topmodel, Map<String, ModelState> submodels,
			Map<String, Double> replacements, String[] interesting, Set<String> printConcentrations, double printTime) throws IOException
	{
		if (interesting.length == 0)
		{
			printAllToTSD(printTime, bufferedWriter, topmodel, submodels, replacements);
		}
		else
		{
			printInterestingToTSD(bufferedWriter, printTime, topmodel, submodels, replacements, interesting, printConcentrations);
		}

	}

	public static void setupVariableFromTSD(BufferedWriter bufferedWriter, ModelState topmodel, Map<String, ModelState> submodels,
			String[] interesting) throws IOException
	{
		bufferedWriter.write("(" + "\"" + "time" + "\"");

		if (interesting.length > 0)
		{
			for (String s : interesting)
			{

				bufferedWriter.write(",\"" + s + "\"");

			}

			bufferedWriter.write("),\n");

			return;
		}

		for (String speciesID : topmodel.getSpeciesIDSet())
		{
			bufferedWriter.write(",\"" + speciesID + "\"");
		}

		for (String noConstantParam : topmodel.getVariablesToPrint())
		{
			bufferedWriter.write(",\"" + noConstantParam + "\"");
		}
		/*
		 * for (String compartment : topmodel.compartmentIDSet) {
		 * bufferedTSDWriter.write(", \"" + compartment + "\""); }
		 */
		for (ModelState model : submodels.values())
		{
			for (String speciesID : model.getSpeciesIDSet())
			{
				if (!model.getIsHierarchical().contains(speciesID))
				{
					bufferedWriter.write(",\"" + model.getID() + "__" + speciesID + "\"");
				}
			}

			for (String noConstantParam : model.getVariablesToPrint())
			{
				if (!model.getIsHierarchical().contains(noConstantParam))
				{
					bufferedWriter.write(",\"" + model.getID() + "__" + noConstantParam + "\"");
				}
			}
		}

		bufferedWriter.write("),\n");

	}
}
