package dataModels.biomodel.annotation;

public class AnnotationAttribute {

	private String prefix;
	private String name;
	private String value;
	
	public AnnotationAttribute(String prefix, String name, String value) {
		this.prefix = prefix;
		this.name = name;
		this.value = value;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String toXMLString() {
		return prefix + ":" + name + "=\"" + value + "\"";
	}
}
