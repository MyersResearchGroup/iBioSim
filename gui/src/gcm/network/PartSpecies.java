package gcm.network;

public class PartSpecies {
	public PartSpecies(SpeciesInterface species, double stoichiometry) {
		this.species = species;
		this.stoichiometry = stoichiometry;
	}
	
	public SpeciesInterface getSpecies() {
		return species;
	}
	
	public double getStoich() {
		return stoichiometry;
	}
	
	private SpeciesInterface species;
	private double stoichiometry;
}
