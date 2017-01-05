package backend.analysis.dynamicsim.hierarchical.math;

public class SpeciesReferenceNode extends VariableNode
{
	private SpeciesNode	species;
	private String		speciesRefId;

	public SpeciesReferenceNode()
	{
		super("none");
	}

	public SpeciesReferenceNode(String id, double value)
	{
		super(id);
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

	public double getStoichiometry(int index)
	{
		return getValue(index);
	}

	public SpeciesNode getSpecies()
	{
		return species;
	}

	@Override
	public SpeciesReferenceNode clone()
	{
		return new SpeciesReferenceNode(this);
	}

}
