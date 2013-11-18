package biomodel.gui.util;

/**
 * Objects that implement this interface provide
 * a key/value pair.
 * @author Nam Nguyen
 * @organization University of Utah
 * @email namphuon@cs.utah.edu
 */
public interface PropertyProvider {
	public String getKey();
	public String getValue();
	
	public void setKey(String key);
	public void setValue(String value);
}
