package backend.sbol.util;

import java.net.URI;

import dataModels.util.GlobalConstants;

public class SBO {
	
	private SBO() {
		
	}
	
	/**
	 * Creates a new URI from the Systems Biology Ontology (SBO)
	 * namespace with the given local name. For example, the function call
	 * <value>term("SBO:0000179")</value> will return the 
	 * URI <value>http://www.ebi.ac.uk/sbo/main/SBO:0000179</value>
	 */
	public static final URI type(String localName) {
		return NAMESPACE.resolve(localName);
	}

	/**
	 * Namespace of SBO 
	 * (<a href="http://www.ebi.ac.uk/sbo/main/">http://www.ebi.ac.uk/sbo/main/</a>).
	 */
	public static final URI NAMESPACE = URI.create("http://www.ebi.ac.uk/sbo/main/");

	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000179">SBO:0000179</a>).
	 */
	public static final URI DEGRADATION = type("SBO:0000" + GlobalConstants.SBO_DEGRADATION);
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000177">SBO:0000177</a>).
	 */
	public static final URI BINDING = type("SBO:0000" + GlobalConstants.SBO_ASSOCIATION);
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000253">SBO:0000253</a>).
	 */
	public static final URI COMPLEX = type("SBO:0000253");
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000280">SBO:0000280</a>).
	 */
	public static final URI LIGAND = type("SBO:0000280");
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000589">SBO:0000589</a>).
	 */
	public static final URI PRODUCTION = type("SBO:0000" + GlobalConstants.SBO_GENETIC_PRODUCTION);
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000170">SBO:0000170</a>).
	 */
	public static final URI ACTIVATION = type("SBO:0000170");
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000459">SBO:0000459</a>).
	 */
	public static final URI ACTIVATOR = type("SBO:0000" + GlobalConstants.SBO_ACTIVATION);
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000169">SBO:0000169</a>).
	 */
	public static final URI REPRESSION = type("SBO:0000169");
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000020">SBO:0000020</a>).
	 */
	public static final URI REPRESSOR = type("SBO:00000" + GlobalConstants.SBO_REPRESSION);
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000598">SBO:0000598</a>).
	 */
	public static final URI PROMOTER = type("SBO:0000" + GlobalConstants.SBO_PROMOTER_MODIFIER);
	
	/**
	 * 	
	 * (<a href="http://www.ebi.ac.uk/sbo/main/SBO:0000011">SBO:0000011</a>).
	 */
	public static final URI PRODUCT = type("SBO:0000011");

}
