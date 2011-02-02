package gcm.gui.schematic;

import java.awt.AWTEvent;
import java.awt.Event;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Properties;

public class SchematicObjectClickEvent extends AWTEvent {

	private static final long serialVersionUID = 1L;
	
	private Properties _prop;
	private String _type;
	
	public SchematicObjectClickEvent(Event source, Properties prop, String type){
		super(source);
		_prop = prop;
		_type = type;
	}
	
	public Properties getProp(){
		return _prop;
	}
	public String getType(){
		return _type;
	}

}


