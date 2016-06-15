package analysis.dynamicsim.hierarchical.util.math;

public class SpeciesReferenceNode extends ValueNode
{

	private ValueNode	stoichiometry;
	private SpeciesNode	species;
	private String		speciesRefId;

	public SpeciesReferenceNode()
	{
		super(0);
	}

	public SpeciesReferenceNode(ValueNode stoichiometry)
	{
		super(0);
		this.stoichiometry = stoichiometry;
	}

	public void setStoichiometry(ValueNode stoichiometry)
	{
		this.stoichiometry = stoichiometry;
	}

	public void setSpecies(SpeciesNode species)
	{
		this.species = species;
	}

	public void setSpeciesRefId(String species)
	{
		this.speciesRefId = species;
	}

	public String getSpeciesRefId()
	{
		return speciesRefId;
	}

	public ValueNode getStoichiometry()
	{
		return stoichiometry;
	}

	public SpeciesNode getSpecies()
	{
		return species;
	}

	@Override
	public double getValue()
	{
		if (stoichiometry != null)
		{
			return stoichiometry.getValue();
		}
		else
		{
			return 0;
		}
	}

	@Override
	public String toString()
	{
		return stoichiometry + " " + species;
	}

}
