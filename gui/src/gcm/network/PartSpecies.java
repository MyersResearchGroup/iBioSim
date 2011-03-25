package gcm.network;

public class PartSpecies {
	public PartSpecies(String partId, String complexId, double stoichiometry) {
		this.partId = partId;
		this.complexId = complexId;
		this.stoichiometry = stoichiometry;
		
	}
	
	public String getPartId() {
		return partId;
	}
	
	public String getComplexId() {
		return complexId;
	}
	
	public double getStoich() {
		return stoichiometry;
	}
	
	private String partId;
	private String complexId;
	private double stoichiometry;
}
