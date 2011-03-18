package gcm.network;

public class PartSpecies {
	public PartSpecies(String speciesId, String complexId, double stoichiometry) {
		this.speciesId = speciesId;
		this.complexId = complexId;
		this.stoichiometry = stoichiometry;
		
	}
	
	public String getSpeciesId() {
		return speciesId;
	}
	
	public String getComplexId() {
		return complexId;
	}
	
	public double getStoich() {
		return stoichiometry;
	}
	
	private String speciesId;
	private String complexId;
	private double stoichiometry;
}
