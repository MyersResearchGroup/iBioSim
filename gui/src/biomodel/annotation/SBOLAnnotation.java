package biomodel.annotation;

import java.net.URI;
import java.util.List;

import biomodel.util.GlobalConstants;

public class SBOLAnnotation {
	
	private AnnotationElement modelToSbol;
	
	public SBOLAnnotation(String sbmlMetaId, List<URI> sbolURIs) {
		this(sbmlMetaId, sbolURIs, GlobalConstants.SBOL_ASSEMBLY_PLUS_STRAND);
	}
	
	public SBOLAnnotation(String sbmlMetaId, List<URI> sbolURIs, String sbolStrand) {
		modelToSbol = new AnnotationElement("ModelToSBOL");
		modelToSbol.addNamespace(new AnnotationNamespace("http://sbolstandard.org/modeltosbol/1.0#"));
		
		AnnotationElement rdf = new AnnotationElement("rdf", "RDF");
		rdf.addNamespace(new AnnotationNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		rdf.addNamespace(new AnnotationNamespace("mts", "http://sbolstandard.org/modeltosbol/1.0#"));
		modelToSbol.addChild(rdf);
		
		AnnotationElement description = new AnnotationElement("rdf", "Description");
		description.addAttribute(new AnnotationAttribute("rdf", "about", "#" + sbmlMetaId));
		rdf.addChild(description);
		
		AnnotationElement dnaComponents = new AnnotationElement("mts", "DNAComponents");
		description.addChild(dnaComponents);
		
		AnnotationElement seq = new AnnotationElement("rdf", "Seq");
		dnaComponents.addChild(seq);
		
		AnnotationElement strand = new AnnotationElement("mts", "Strand", sbolStrand);
		description.addChild(strand);
		
		for (URI uri : sbolURIs) {
			AnnotationElement li = new AnnotationElement("rdf", "li");
			li.addAttribute(new AnnotationAttribute("rdf", "resource", uri.toString()));
			seq.addChild(li);
		}
	}
	
	public String toXMLString() {
		return modelToSbol.toXMLString();
	}
}
