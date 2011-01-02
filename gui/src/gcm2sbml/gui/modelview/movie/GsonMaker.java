package gcm2sbml.gui.modelview.movie;

import java.awt.Color;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

public class GsonMaker {

	private class ColorInstanceCreator implements InstanceCreator<Color> {

		public Color createInstance(Type type) {
			return new Color(0, 0, 0);
		}
	}
	
	public Gson makeGson(){
		Gson gson = new GsonBuilder().
		setPrettyPrinting().
		registerTypeAdapter(Color.class, new ColorInstanceCreator()).
		create();
		
		return gson;
	}
}
