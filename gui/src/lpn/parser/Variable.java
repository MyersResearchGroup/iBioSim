package lpn.parser;

import java.util.Properties;

public class Variable {
	
	private String name;
	
	private String type;
	
	protected String initValue;
	
	private String initRate;
	
	private String port;
	
	public Variable(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public Variable(String name, String type, String initValue) {
		this.name = name;
		this.type = type;
		this.initValue = initValue;
	}
	
	public Variable(String name, String type, Properties initCond) {
		if (type.equals(CONTINUOUS)) {
			this.name = name;
			this.type = type;
			this.initValue = initCond.getProperty("value");
			this.initRate = initCond.getProperty("rate");
		}
	}
	
	public Variable(String name, String type, String initValue, String port) {
		this.name = name;
		this.type = type;
		this.initValue = initValue;
		this.port = port;
	}
	
	public void addInitValue(String initValue) {
		this.initValue = initValue;
	}
	
	public void addInitRate(String initRate) {
		this.initRate = initRate;
	}
	
	public void addInitCond(Properties initCond) {
		if (type.equals(CONTINUOUS)) {
			this.initValue = initCond.getProperty("value");
			this.initRate = initCond.getProperty("rate");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getInitValue() {
		return initValue;
	}
	
	public String getInitRate() {
		return initRate;
	}
	
	public boolean isInput() {
		if (port != null) {
		return port.equals(INPUT);
		}
		return false;
	}
	
	public boolean isOutput() {
		if (port != null) {
		return port.equals(OUTPUT);
		}
		return false;
	}

	public boolean isInternal() {
		if (port != null) {
		return port.equals(INTERNAL);
		}
		return true;
	}
	
	public String getPort() {
		return port;
	}
	
	public String getType() {
		return type;
	}
	
	public void setPort(String newPort) {
		port = newPort;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public boolean equals(Object var) {
		return name.equals(var.toString());
	}
	
	public static final String BOOLEAN = "boolean";
	
	public static final String INTEGER = "integer";
	
	public static final String CONTINUOUS = "continuous";
	
	public static final String INPUT = "input";
	
	public static final String OUTPUT = "output";
	
	public static final String INTERNAL = "internal";
	
}