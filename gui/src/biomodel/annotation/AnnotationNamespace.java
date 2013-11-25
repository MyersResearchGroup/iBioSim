package biomodel.annotation;

public class AnnotationNamespace {

	private String prefix;
	private String namespace;
	
	public AnnotationNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public AnnotationNamespace(String prefix, String namespace) {
		this.prefix = prefix;
		this.namespace = namespace;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String toXMLString() {
		if (prefix != null)
			return "xmlns:" + prefix + "=\"" + namespace + "\"";
		return "xmlns=\"" + namespace + "\"";
	}
}
