package sbol.util;

import java.net.URI;

public class SBOLOntology {
	
	private SBOLOntology() {
		
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
	 * Namespace of SBOL
	 * (<a href="http://www.sbolstandard.org">http://www.sbolstandard.org</a>).
	 */
	public static final URI NAMESPACE = URI.create("http://www.sbolstandard.org/");
	
	/**
	 * 	
	 * (<a href="http://www.sbolstandard.org/input">input</a>).
	 */
	public static final URI INPUT = type("input");
	
	/**
	 * 	
	 * (<a href="http://www.sbolstandard.org/output">output</a>).
	 */
	public static final URI OUTPUT = type("output");
	
	/**
	 * 	
	 * (<a href="http://www.sbolstandard.org/none">none</a>).
	 */
	public static final URI NONE = type("none");
	
	/**
	 * 	
	 * (<a href="http://www.sbolstandard.org/public">public</a>).
	 */
	public static final URI PUBLIC = type("public");
	
	/**
	 * 	
	 * (<a href="http://www.sbolstandard.org/private">private</a>).
	 */
	public static final URI PRIVATE = type("private");

}
