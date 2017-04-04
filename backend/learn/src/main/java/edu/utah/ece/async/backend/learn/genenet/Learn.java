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
package edu.utah.ece.async.backend.learn.genenet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.utah.ece.async.dataModels.util.GlobalConstants;
import edu.utah.ece.async.dataModels.util.exceptions.BioSimException;

/**
 * 
 *
 * @author Leandro Watanabe
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class Learn
{

	private int	bins;

	public Learn(int bins)
	{
		this.bins = bins;
	}

	public void learnBaselineNetwork(SpeciesCollection S, Experiments E, NetCon C)
	{
		for (int i = 0; i < S.size(); i++)
		{
			String s_0 = S.getInterestingSpecies(i);

			for (int j = 0; j < S.size(); j++)
			{
				String s_1 = S.getInterestingSpecies(j);

				if (!s_0.equals(s_1))
				{
					addParent(s_0, s_1, E, S, C);
				}
			}
		}
	}

	private void addParent(String parent, String child, Experiments E, SpeciesCollection S, NetCon C)
	{
		int parent_col = S.getColumn(parent);
		int child_col = S.getColumn(child);
		int repression = 0;
		int activation = 0;
		int unknown = 0;
		double childPrevious, childCurr;
		double parentPrevious, parentCurr;
		double childRate, previousChildRate;
		double parentRate, previousParentRate;

		for (int i = 0; i < E.getNumOfExperiments(); i++)
		{
			previousChildRate = Double.NaN;
			previousParentRate = Double.NaN;
			for (int j = 1; j < E.getExperiments().get(i).size(); j++)
			{
				childPrevious = E.getDataPoint(i, j - 1, child_col);
				childCurr = E.getDataPoint(i, j, child_col);
				parentPrevious = E.getDataPoint(i, j - 1, parent_col);
				parentCurr = E.getDataPoint(i, j, parent_col);
				childRate = childCurr - childPrevious;
				parentRate = parentCurr - parentPrevious;

				if (j == 1)
				{
					previousChildRate = childRate;
					previousParentRate = parentRate;
					continue;
				}

				if (childCurr > 10 && parentCurr > 10)
				{
					if (childRate < previousChildRate)
					{
						if (parentRate > previousParentRate)
						{
							repression++;
						}
						else if (parentRate < previousParentRate)
						{
							activation++;
						}
						else
						{
							unknown++;
						}
					}
					else if (childRate > previousChildRate)
					{
						if (parentRate > previousParentRate)
						{
							activation++;
						}
						else if (parentRate < previousParentRate)
						{
							repression++;
						}
						else
						{
							unknown++;
						}
					}
				}

				previousParentRate = parentRate;
				previousChildRate = childRate;
			}
		}

		if (repression > activation && repression >= unknown)
		{
			C.addConnection(child, "repressor", repression, parent);
		}
		else if (activation > repression && repression >= unknown)
		{
			C.addConnection(child, "activator", repression, parent);
		}
	}

	public void learnNetwork(SpeciesCollection S, Experiments E, NetCon C, Thresholds T, Encodings L) throws BioSimException
	{
		EncodeExpts(S, E, C, T, L);

		for (int i = 0; i < S.size(); i++)
		{
			String s = S.getInterestingSpecies(i);

			if (!C.containEdge(s))
			{
				Experiments new_E = E.removeMutations(s);
				selectInitialParents(s, S, new_E, C, T, L);
				createMultipleParents(s, S, new_E, C, T, L);
				competeMultipleParents(s, S, new_E, C, T, L);
			}
		}
	}

	private void EncodeExpts(SpeciesCollection S, Experiments E, NetCon C, Thresholds T, Encodings L) throws BioSimException
	{
		for (int i = 0; i < S.size(); i++)
		{
		  String species = S.getInterestingSpecies(i);
		  if(!S.containsSpeciesData(species))
		  {
		    throw new BioSimException("There is no data for " + species + ". Check if species ids in the target model match the ones from the data.", "Error in learning.");
		  }
			getDiscreteLevels(species, S, E, L, bins);
			getBinAssign(species, S, E, L, bins);
		}
	}

	private void getBinAssign(String species, SpeciesCollection S, Experiments experiments, Encodings L, int bins)
	{
		int col = S.getColumn(species);
		for (int i = 0; i < experiments.getNumOfExperiments(); i++)
		{
			List<List<Double>> experiment = experiments.getExperiments().get(i);

			for (int j = 0; j < experiment.size(); j++)
			{
				double value = experiment.get(j).get(col);

				L.addLevelAssignment(i, j, col, value);

			}
		}

	}

	private void getDiscreteLevels(String species, SpeciesCollection S, Experiments experiments, Encodings L, int levels)
	{
		int col = S.getColumn(species);
		double[] level = new double[levels + 1];
		double v = Double.NaN, vp = Double.NaN;
		int steps = 0;
		level[0] = 0;
		level[levels] = Double.POSITIVE_INFINITY;

		LinkedList<Double> values = new LinkedList<Double>();

		for (List<List<Double>> experiment : experiments.getExperiments())
		{
			for (int i = 0; i < experiment.size(); i++)
			{
				double value = experiment.get(i).get(col);

				values.add(value);
			}
		}

		Collections.sort(values);

		for (int j = 1; j < levels - 1; j++)
		{
			int count = 0;
			level[j] = Double.POSITIVE_INFINITY;

			steps = values.size() / (levels - j + 1);

			while (values.size() > 0 && count < steps)
			{
				v = values.pop();

				count++;
			}

			do
			{
				vp = values.pop();
			}
			while (values.size() > 0 && v == vp);

			if (values.size() > 0)
			{
				level[j] = v;
				values.addFirst(vp);
			}
		}

		L.addDiscreteSpecies(col, level);
	}

	private static double scoreParents(String s, SpeciesCollection S, Set<String> P, Set<String> G, Experiments E, Thresholds T, Encodings L)
	{
		int votes_a = 0, votes_r = 0, votes_u = 0;
		int totalDen = 0, den = 0, totalNum = 0, num = 0;
		int targetCol = S.getColumn(s);
		double probRatio = 0;
		List<Integer> levelAssignmentsP = getLevelAssignments(P, S, L);
		Collections.sort(levelAssignmentsP);
		List<Integer> levelAssignmentsG = getLevelAssignments(G, S, L);
		Collections.sort(levelAssignmentsP);
		int l0 = levelAssignmentsP.remove(0);

		for (int l : levelAssignmentsG)
		{
			for (int lp : levelAssignmentsP)
			{
				for (int i = 0; i < E.getNumOfExperiments(); i++)
				{
					for (int j = 0; j < E.getExperiments().get(i).size(); j++)
					{

						boolean g = checkSameLevel(G, i, j, l, S, L);
						boolean p = checkSameLevel(P, i, j, lp, S, L);
						boolean p0 = checkSameLevel(P, i, j, l0, S, L);

						if (g && p)
						{
							if (isIncreasing(i, j, targetCol, E))
							{
								num++;
							}
							totalNum++;
						}
						else if (g && p0)
						{
							if (isIncreasing(i, j, targetCol, E))
							{
								den++;
							}
							totalDen++;
						}
					}
				}
			}

			if (num == 0 || den == 0)
			{
				probRatio = 1;
			}
			else
			{
				probRatio = (1.0 * num / totalNum) / (1.0 * den / totalDen);
			}

			if (probRatio > T.getTa())
			{
				votes_a++;
			}
			else if (probRatio < T.getTr())
			{
				votes_r++;
			}
			else
			{
				votes_u++;
			}
		}

		return 1.0 * (votes_a - votes_r) / (votes_a + votes_r + votes_u);
	}

	private static boolean checkSameLevel(Set<String> species, int experiment, int row, int bin, SpeciesCollection S, Encodings L)
	{
		for (String s : species)
		{
			int col = S.getColumn(s);

			if (L.getLevelAssignments().get(experiment).get(row).get(col) != bin)
			{
				return false;
			}
		}

		return true;
	}

	private static boolean isIncreasing(int experiment, int row, int targetCol, Experiments E)
	{
		List<List<Double>> list = E.getExperiments().get(experiment);

		if (row + 1 < list.size())
		{
			if (list.get(row).get(targetCol) < list.get(row + 1).get(targetCol))
			{
				return true;
			}
		}

		if (row - 1 >= 0)
		{
			if (list.get(row - 1).get(targetCol) < list.get(row).get(targetCol))
			{
				return true;
			}
		}
		return false;
	}

	private static List<Integer> getLevelAssignments(Set<String> P, SpeciesCollection S, Encodings L)
	{
		List<Integer> set = new ArrayList<Integer>();

		for (String species : P)
		{
			int col = S.getColumn(species);

			for (int i = 0; i < L.size(); i++)
			{
				List<List<Integer>> encodings = L.getLevelAssignments().get(i);

				for (int j = 0; j < encodings.size(); j++)
				{
					int value = encodings.get(j).get(col);
					if (!set.contains(value))
					{
						set.add(encodings.get(j).get(col));
					}
				}
			}
		}

		return set;
	}

	private static void selectInitialParents(String s, SpeciesCollection S, Experiments E, NetCon C, Thresholds T, Encodings L)
	{
		List<String> interestingSpecies = new ArrayList<String>(S.getInterestingSpecies());
		interestingSpecies.remove(s);

		double score;

		for (String p : interestingSpecies)
		{
			score = scoreParents(s, S, new HashSet<String>(Arrays.asList(p)), new HashSet<String>(Arrays.asList(s)), E, T, L);

			if (score >= T.getTv())
			{
				C.addConnection(s, "activator", score, p);
			}
			else if (score <= -T.getTv())
			{
				C.addConnection(s, "repressor", score, p);
			}
		}
	}

	private static void createMultipleParents(String s, SpeciesCollection S, Experiments E, NetCon C, Thresholds T, Encodings L)
	{

		double score1, score2, scoreb;

		List<Connection> connectionsToDelete = new ArrayList<Connection>();
		List<Connection> multipleParents = new ArrayList<Connection>();
		List<Connection> singleParent = C.getListOfConnections(s);

		if (singleParent == null)
		{
			return;
		}

		for (int i = 0; i < singleParent.size(); i++)
		{
			Connection connection1 = singleParent.get(i);

			score1 = connection1.getScore();

			for (int j = i + 1; j < singleParent.size(); j++)
			{
				Connection connection2 = singleParent.get(j);
				score2 = connection2.getScore();

				Set<String> parents = new HashSet<String>();
				parents.addAll(connection1.getParents());
				parents.addAll(connection2.getParents());

				scoreb = scoreParents(s, S, parents, new HashSet<String>(Arrays.asList(s)), E, T, L);
				if (Math.abs(scoreb) >= Math.abs(score1) && Math.abs(scoreb) >= Math.abs(score2))
				{
					multipleParents.add(new Connection(s, scoreb, connection1, connection2));
					connectionsToDelete.add(connection1);
					connectionsToDelete.add(connection2);
				}
			}
		}

		for (Connection connection : connectionsToDelete)
		{
			C.removeConnection(s, connection);
		}

		for (Connection connection : multipleParents)
		{
			C.addConnection(s, connection);
		}
	}

	private static void competeMultipleParents(String s, SpeciesCollection S, Experiments E, NetCon C, Thresholds T, Encodings L)
	{
		List<Connection> potentialParents = C.getListOfConnections(s);
		double scoreq, smallestScore;

		if (potentialParents == null)
		{
			return;
		}

		double Ta = T.getTa();
		double Tr = T.getTr();

		while (potentialParents.size() > 1)
		{
			List<List<String>> Q = getContenders(s, C);
			List<Double> scores = new ArrayList<Double>();
			smallestScore = Double.POSITIVE_INFINITY;

			for (List<String> q : Q)
			{
				HashSet<String> P = new HashSet<String>(q);
				HashSet<String> G = new HashSet<String>();

				for (List<String> qp : Q)
				{
					if (qp != q)
					{
						G.addAll(qp);
					}
				}

				G.add(s);
				scoreq = scoreParents(s, S, P, G, E, T, L);
				scores.add(Math.abs(scoreq));

				if (Math.abs(scoreq) < smallestScore)
				{
					smallestScore = Math.abs(scoreq);
				}
			}

			boolean isScoreTied = isTied(smallestScore, Q, scores);

			if (isScoreTied)
			{
				if (T.getTa() > 4 || T.getTr() < 0)
				{
					removeLosers(s, C, Q);
				}
				else
				{
					T.setTa(T.getTa() + T.getTt());
					T.setTr(T.getTr() - T.getTt());
				}
			}
			else
			{
				removeLosers(s, C, Q, scores, smallestScore);
			}
		}

		T.setTa(Ta);
		T.setTr(Tr);
	}

	private static boolean checkNumParents(List<List<String>> Q)
	{
		boolean sameNum = true;

		if (Q.size() > 0)
		{
			int size = Q.get(0).size();

			for (int i = 1; i < Q.size(); i++)
			{
				if (Q.get(i).size() != size)
				{
					sameNum = false;
					break;
				}
			}
		}

		return sameNum;
	}

	private static boolean isTied(double smallestScore, List<List<String>> Q, List<Double> scores)
	{
		boolean isScoreTied = true;

		for (int i = 0; i < Q.size(); i++)
		{
			if (scores.get(i) != smallestScore)
			{
				isScoreTied = false;
				break;
			}
		}

		return isScoreTied;
	}

	private static void removeLosers(String s, NetCon C, List<List<String>> Q)
	{
		if (checkNumParents(Q))
		{
			int minParents = Integer.MAX_VALUE;
			int index = -1;
			for (int i = 0; i < Q.size(); i++)
			{
				if (Q.get(i).size() < minParents)
				{
					minParents = Q.get(i).size();
					index = i;
				}
			}
			C.removeConnectionByParent(s, Q.get(index));
		}
		else
		{
			C.removeConnection(s, 0);
		}
	}

	private static void removeLosers(String s, NetCon C, List<List<String>> Q, List<Double> scores, double smallestScore)
	{

		for (int i = 0; i < Q.size(); i++)
		{
			if (scores.get(i) == smallestScore)
			{
				C.removeConnectionByParent(s, Q.get(i));
			}
		}
	}

	private static List<List<String>> getContenders(String s, NetCon C)
	{
		List<List<String>> contenders = new ArrayList<List<String>>();
		List<Connection> potentialParents = C.getListOfConnections(s);

		double maxScore = Double.MIN_VALUE;
		double minScore = Double.MAX_VALUE;

		if (potentialParents != null)
		{
			for (Connection connection : potentialParents)
			{
				if (connection.getScore() > maxScore)
				{
					maxScore = connection.getScore();
				}
				else if (connection.getScore() < minScore)
				{
					minScore = connection.getScore();
				}
			}

			for (Connection connection : potentialParents)
			{
				if (connection.getScore() == maxScore || connection.getScore() == minScore)
				{
					contenders.add(connection.getParents());
				}
			}
		}

		return contenders;

	}

	public void getDotFile(String filename, String directory, SpeciesCollection collection, NetCon network) throws BioSimException
	{
		Map<String, String> speciesToNode;
		File fout = new File(directory + GlobalConstants.separator + filename);
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		try
		{
			int index = 0;
			speciesToNode = new HashMap<String, String>();
			fos = new FileOutputStream(fout);
			osw = new OutputStreamWriter(fos);
			osw.write("digraph G { \n");

			for (String s : collection.getInterestingSpecies())
			{
				String node = "s" + ++index;
				osw.write(node + " [shape=ellipse,color=black,label=\"" + s + "\"];\n");
				speciesToNode.put(s, node);
			}
			for (String s : network.getConnections().keySet())
			{
				for (Connection connection : network.getConnections().get(s))
				{
					for (String parent : connection.getParents())
					{
						String type = connection.getParentType(parent).toLowerCase();

						if (type.equals("activator"))
						{
							osw.write(speciesToNode.get(parent) + " -> " + speciesToNode.get(s) + " [color=\"blue4\",arrowhead=vee]\n");
						}
						else if (type.equals("repressor"))
						{
							osw.write(speciesToNode.get(parent) + " -> " + speciesToNode.get(s) + " [color=\"firebrick4\",arrowhead=tee]\n");
						}
					}
				}
			}
			osw.write("} \n");
		}
		catch (FileNotFoundException e)
		{
		  throw new BioSimException("Could not write dot file. File could not be found.", "Error in Learning");
		}
		catch (IOException e)
		{
		  throw new BioSimException("Error when writing dot file.", "Error in Learning");
		}
		finally
		{
			try
			{
				if (osw != null)
				{
					osw.close();
				}
			}
			catch (IOException e)
			{
			  throw new BioSimException("Failed to close writer", "Error in Learning");
			}

			try
			{
				if (fos != null)
				{
					fos.close();
				}
			}
			catch (IOException e)
			{
			  throw new BioSimException("Failed to close outputstream", "Error in Learning");
			}

		}
	}

}
