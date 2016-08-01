package analysis.dynamicsim.hierarchical.util.math;

public class SpeciesReferenceNode extends VariableNode
{
	private SpeciesNode	species;
	private String		speciesRefId;

	public SpeciesReferenceNode()
	{
		super("none", 1);
	}

	public SpeciesReferenceNode(double value)
	{
		super("none", value);
	}

	public SpeciesReferenceNode(String id, double value)
	{
		super(id, value);
	}

	public SpeciesReferenceNode(SpeciesReferenceNode copy)
	{
		super(copy);
		this.species = copy.species.clone();
		this.speciesRefId = copy.speciesRefId;
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

	public double getStoichiometry()
	{
		return value;
	}

	public SpeciesNode getSpecies()
	{
		return species;
	}

	@Override
	public String toString()
	{
		return value + " " + species;
	}

	@Override
	public SpeciesReferenceNode clone()
	{
		return new SpeciesReferenceNode(this);
	}

}
