package frontend.main.util;

import java.io.Serializable;

public class MutableString implements Mutable, Serializable, Comparable<Object> {

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

	@Override
	public int compareTo(Object o) {
		return value.compareTo((String) o);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MutableString) {
			return value.equals(((MutableString) obj).getString());
		}
		return false;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public Object getValue() {
		return value;
	}

	public String getString() {
		return value;
	}

	public void setString(String value) {
		this.value = value;
	}

	@Override
	public void setValue(Object value) {
		this.value = (String) value;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
