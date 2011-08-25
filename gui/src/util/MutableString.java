package util;

import java.io.Serializable;

public class MutableString implements Mutable, Serializable, Comparable {

	private static final long serialVersionUID = 5709256424906470057L;

	private String value;

	public MutableString() {
		super();
		value = null;
	}

	public MutableString(String value) {
		super();
		this.value = value;
	}

	public int compareTo(Object o) {
		return value.compareTo((String) o);
	}

	public boolean equals(Object obj) {
		if (obj instanceof MutableString) {
			return value.equals(((MutableString) obj).getString());
		}
		return false;
	}

	public String toString() {
		return value;
	}

	public Object getValue() {
		return value;
	}

	public String getString() {
		return value;
	}

	public void setString(String value) {
		this.value = value;
	}

	public void setValue(Object value) {
		this.value = (String) value;
	}
}
