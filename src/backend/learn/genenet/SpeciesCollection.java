package backend.learn.genenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpeciesCollection
{

	private List<String>			interestingSpecies;
	private Map<Integer, String>	columnSpecies;
	private Map<String, Integer>	speciesColumn;

	public SpeciesCollection()
	{
		interestingSpecies = new ArrayList<String>();
		columnSpecies = new HashMap<Integer, String>();
		speciesColumn = new HashMap<String, Integer>();
	}

	public void addSpecies(String id, int index)
	{
		speciesColumn.put(id, index);
		columnSpecies.put(index, id);
	}

	public void addInterestingSpecies(String id)
	{
		interestingSpecies.add(id);
	}

	public int getColumn(String species)
	{
		return speciesColumn.get(species);
	}

	public String getInterestingSpecies(int index)
	{
		return interestingSpecies.get(index);
	}

	public int size()
	{
		return interestingSpecies.size();
	}

	public List<String> getInterestingSpecies()
	{
		return interestingSpecies;
	}

}
