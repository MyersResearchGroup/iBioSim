package analysis.dynamicsim.hierarchical.util.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import analysis.dynamicsim.hierarchical.states.ModelState;
import analysis.dynamicsim.hierarchical.util.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.util.math.VariableNode;

public class HierarchicalWriter
{

	/**
	 * appends the current species states to the TSD file
	 * 
	 * @throws IOException
	 */
	public static void printToTSD(BufferedWriter bufferedWriter, ModelState topmodel, Map<String, ModelState> submodels, String[] interesting, Set<String> printConcentrations, double printTime) throws IOException
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

	public static void setupVariableFromTSD(BufferedWriter bufferedWriter, ModelState topmodel, Map<String, ModelState> submodels, String[] interesting) throws IOException
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

		for (VariableNode node : topmodel.getVariables())
		{
			bufferedWriter.write(",\"" + node.getName() + "\"");
		}

		bufferedWriter.write("),\n");

	}

	private static void printAllToTSD(double printTime, BufferedWriter bufferedWriter, ModelState topmodel, Map<String, ModelState> submodels) throws IOException
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

		bufferedWriter.write(")");
		bufferedWriter.flush();
	}

	private static void printInterestingToTSD(BufferedWriter bufferedWriter, double printTime, ModelState topmodel, Map<String, ModelState> submodels, String[] interesting, Set<String> printConcentrations) throws IOException
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
			ModelState ms = (id.equals(s)) ? topmodel : submodels.get(id);
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
