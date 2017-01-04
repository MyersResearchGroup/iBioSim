package backend.sbol.util;

import java.net.URI;

import backend.biomodel.util.GlobalConstants;

public class MyersOntology {
	
	private MyersOntology() {
		
	}
	
	/**
	 * Creates a new URI from the Myers lab
	 * namespace with the given local name. For example, the function call
	 * <value>term("toggle_switch")</value> will return the 
	 * URI <value>http://www.async.ece.utah.edu/toggle_switch</value>
	 */
	public static final URI type(String localName) {
		return NAMESPACE.resolve(localName);
	}

	/**
	 * Namespace for Myers lab
	 * (<a href="http://www.async.ece.utah.edu/">http://www.async.ece.utah.edu/</a>).
	 */
	public static final URI NAMESPACE = URI.create(GlobalConstants.SBOL_AUTHORITY_DEFAULT + "/");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/degraded">degraded</a>).
	 */
	public static final URI DEGRADED = type("degraded");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/activated">activated</a>).
	 */
	public static final URI ACTIVATED = type("activated");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/repressed">repressed</a>).
	 */
	public static final URI REPRESSED = type("repressed");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/transcription_factor">transcription_factor</a>).
	 */
	public static final URI TF = type("transcription_factor");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/inducer">inducer</a>).
	 */
	public static final URI INDUCER = type("inducer");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/chaperone">chaperone</a>).
	 */
	public static final URI CHAPERONE = type("chaperone");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/reporter">reporter</a>).
	 */
	public static final URI REPORTER = type("reporter");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/transcribed">transcribed</a>).
	 */
	public static final URI TRANSCRIBED = type("transcribed");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/inverter">inverter</a>).
	 */
	public static final URI INVERTER = type("inverter");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/AND_gate">AND_gate</a>).
	 */
	public static final URI AND = type("AND_gate");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/toggle_switch">toggle_switch</a>).
	 */
	public static final URI TOGGLE = type("toggle_switch");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/sensor">sensor</a>).
	 */
	public static final URI SENSOR = type("sensor");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/gene">gene</a>).
	 */
	public static final URI GENE = type("gene");
	
	/**
	 * 	
	 * (<a href="http://www.async.ece.utah.edu/precedes">precedes</a>).
	 */
	public static final URI PRECEDES = type("precedes");

}
