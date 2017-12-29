package edu.utah.ece.async.ibiosim.dataModels.graphData;

import java.awt.Shape;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.plot.DefaultDrawingSupplier;

public class ShapeMap {
	
	private static final Map<String, Shape> shapeMap;
	static {
		DefaultDrawingSupplier draw = new DefaultDrawingSupplier();
		Map<String,Shape> shapes = new HashMap<String, Shape>();
		shapes.put("Square", draw.getNextShape());
		shapes.put("Circle", draw.getNextShape());
		shapes.put("Triangle", draw.getNextShape());
		shapes.put("Diamond", draw.getNextShape());
		shapes.put("Rectangle (Horizontal)", draw.getNextShape());
		shapes.put("Triangle (Upside Down)", draw.getNextShape());
		shapes.put("Circle (Half)", draw.getNextShape());
		shapes.put("Arrow", draw.getNextShape());
		shapes.put("Rectangle (Vertical)", draw.getNextShape());
		shapes.put("Arrow (Backwards)", draw.getNextShape());
		shapeMap = Collections.unmodifiableMap(shapes);
	}

	public static Map<String, Shape> getShapeMap() {
		return shapeMap;
	}

}
