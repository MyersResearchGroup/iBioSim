package verification.platu.markovianAnalysis;

public class Property {
	private String label;

	private String property;

	public Property(String label, String property) {
		this.label = label;
		this.property = property;
	}

	String getLabel() {
		return label;
	}

	String getProperty() {
		return property;
	}

	/*
	private void setLabel(String label) {
		this.label = label;
	}

	private void setProperty(String property) {
		this.property = property;
	}
	*/
}