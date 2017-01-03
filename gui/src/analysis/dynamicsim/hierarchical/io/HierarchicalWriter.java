package analysis.dynamicsim.hierarchical.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analysis.dynamicsim.hierarchical.math.SpeciesNode;
import analysis.dynamicsim.hierarchical.math.VariableNode;
import analysis.dynamicsim.hierarchical.model.HierarchicalModel;

public class HierarchicalWriter
{

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



    if (topmodel.getNumOfVariables() > 0)
    {
      for (VariableNode node : topmodel.getVariables())
      {
        bufferedWriter.write(commaSpace + node.getValue(0));
      }
    }
    for (HierarchicalModel submodel : submodels.values())
    {
      if (submodel.getNumOfVariables() > 0)
      {
        for (VariableNode node : submodel.getVariables())
        {
          bufferedWriter.write(commaSpace + node.getValue(submodel.getIndex()));
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
      double value = node.getValue(ms.getIndex());
      if (printConcentrations.contains(s))
      {
        SpeciesNode species = (SpeciesNode) node;
        value = species.getConcentration(ms.getIndex());
      }
      bufferedWriter.write(commaSpace + value);
      commaSpace = ",";

    }

    bufferedWriter.write(")");
    bufferedWriter.flush();
  }
}
