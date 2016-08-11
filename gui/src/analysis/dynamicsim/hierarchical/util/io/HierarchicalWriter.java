package analysis.dynamicsim.hierarchical.util.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import analysis.dynamicsim.hierarchical.model.HierarchicalModel;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class HierarchicalWriter
{

	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException
	 */
	public static void printToTSD(BufferedWriter bufferedWriter, HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels, String[] interesting, Set<String> printConcentrations, double printTime) throws IOException
	{
		if (interesting == null || interesting.length == 0)
		{
			printAllToTSD(printTime, bufferedWriter, topmodel, submodels);
		}
		else
		{
			printInterestingToTSD(bufferedWriter, printTime, topmodel, submodels, interesting, printConcentrations);
		}

	}

	public static void setupVariableFromTSD(BufferedWriter bufferedWriter, HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels, String[] interesting) throws IOException
	{
		bufferedWriter.write("(" + "\"" + "time" + "\"");

		if (interesting != null && interesting.length > 0)
		{
			for (String s : interesting)
			{
				bufferedWriter.write(",\"" + s + "\"");
			}

			bufferedWriter.write("),\n");

			return;
		}

		if (topmodel.getNumOfVariables() > 0)
		{
			for (VariableNode node : topmodel.getVariables())
			{
				bufferedWriter.write(",\"" + node.getName() + "\"");
			}
		}
		for (HierarchicalModel submodel : submodels.values())
		{
			if (submodel.getNumOfVariables() > 0)
			{
				for (VariableNode node : submodel.getVariables())
				{
					bufferedWriter.write(",\"" + submodel.getID() + " " + node.getName() + "\"");
				}
			}
		}

		bufferedWriter.write("),\n");

	}

	private static void printAllToTSD(double printTime, BufferedWriter bufferedWriter, HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels) throws IOException
	{
		String commaSpace = ",";

		bufferedWriter.write("(");

		// print the current time
		bufferedWriter.write(printTime + "");

		// loop through the speciesIDs and print their current value to the
		// file
		for (VariableNode node : topmodel.getVariables())
		{
			bufferedWriter.write(commaSpace + node.getValue());
		}
		for (HierarchicalModel submodel : submodels.values())
		{
			if (submodel.getVariables() != null)
			{
				for (VariableNode node : submodel.getVariables())
				{
					bufferedWriter.write(commaSpace + node.getValue());
				}
			}
		}
		bufferedWriter.write(")");
		bufferedWriter.flush();
	}

	private static void printInterestingToTSD(BufferedWriter bufferedWriter, double printTime, HierarchicalModel topmodel, Map<String, HierarchicalModel> submodels, String[] interesting, Set<String> printConcentrations) throws IOException
	{

		String commaSpace = "";

		bufferedWriter.write("(");

		commaSpace = "";

		// print the current time
		bufferedWriter.write(printTime + ",");

		for (String s : interesting)
		{
			String element = s.replaceAll("(.+)__", "");
			String id = s.replace("__" + element, "");
			HierarchicalModel ms = (id.equals(s)) ? topmodel : submodels.get(id);
			VariableNode node = ms.getNode(element);
			double value = node.getValue();
			if (printConcentrations.contains(s))
			{
				SpeciesNode species = (SpeciesNode) node;
				value = species.getConcentration();
			}
			bufferedWriter.write(commaSpace + value);
			commaSpace = ",";

		}

		bufferedWriter.write(")");
		bufferedWriter.flush();
	}
}
