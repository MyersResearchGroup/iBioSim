package backend.learn.genenet;

import java.util.ArrayList;
import java.util.List;

public class Experiments
{

	private List<List<List<Double>>>	experiments;

	public Experiments()
	{
		experiments = new ArrayList<List<List<Double>>>();
	}

	public void addExperiment(int experiment, int row, int col, double data)
	{
		while (experiments.size() < experiment + 1)
		{
			experiments.add(new ArrayList<List<Double>>());
		}
		while (experiments.get(experiment).size() < row + 1)
		{
			experiments.get(experiment).add(new ArrayList<Double>());
		}
		while (experiments.get(experiment).get(row).size() < col + 1)
		{
			experiments.get(experiment).get(row).add(0.0);
		}
		experiments.get(experiment).get(row).set(col, data);
	}

	public double getDataPoint(int experiment, int row, int col)
	{
		return experiments.get(experiment).get(row).get(col);
	}

	public Experiments removeMutations(String s)
	{
		return this;
	}

	public int getNumOfExperiments()
	{
		return experiments.size();
	}

	public List<List<List<Double>>> getExperiments()
	{
		return experiments;
	}
}
