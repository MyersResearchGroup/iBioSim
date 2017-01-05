package backend.analysis.dynamicsim.flattened;

public class SpeciesDimensions {
		
	public int numRowsUpper;
	public int numColsUpper;
	public int numRowsLower;
	public int numColsLower;
	
	public SpeciesDimensions(int rLower, int rUpper, int cLower, int cUpper) {
		
		numRowsUpper = rUpper;
		numRowsLower = rLower;
		numColsUpper = cUpper;
		numColsLower = cLower;
	}
}
