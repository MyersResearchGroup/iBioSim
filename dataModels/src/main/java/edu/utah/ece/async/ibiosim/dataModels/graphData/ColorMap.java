package edu.utah.ece.async.ibiosim.dataModels.graphData;

import java.awt.Paint;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.plot.DefaultDrawingSupplier;

public class ColorMap {
	
	private static final Map<String, Paint> colorMap;
	static {
		DefaultDrawingSupplier draw = new DefaultDrawingSupplier();
		Map<String, Paint> colors = new HashMap<String, Paint>();
		colors.put("Red", draw.getNextPaint());
		colors.put("Blue", draw.getNextPaint());
		colors.put("Green", draw.getNextPaint());
		colors.put("Yellow", draw.getNextPaint());
		colors.put("Magenta", draw.getNextPaint());
		colors.put("Cyan", draw.getNextPaint());
		colors.put("Tan", draw.getNextPaint());
		colors.put("Gray (Dark)", draw.getNextPaint());
		colors.put("Red (Dark)", draw.getNextPaint());
		colors.put("Blue (Dark)", draw.getNextPaint());
		colors.put("Green (Dark)", draw.getNextPaint());
		colors.put("Yellow (Dark)", draw.getNextPaint());
		colors.put("Magenta (Dark)", draw.getNextPaint());
		colors.put("Cyan (Dark)", draw.getNextPaint());
		colors.put("Black", draw.getNextPaint());
		draw.getNextPaint();
		draw.getNextPaint();
		draw.getNextPaint();
		draw.getNextPaint();
		draw.getNextPaint();
		draw.getNextPaint();
		colors.put("Gray", draw.getNextPaint());
		colors.put("Red (Extra Dark)", draw.getNextPaint());
		colors.put("Blue (Extra Dark)", draw.getNextPaint());
		colors.put("Green (Extra Dark)", draw.getNextPaint());
		colors.put("Yellow (Extra Dark)", draw.getNextPaint());
		colors.put("Magenta (Extra Dark)", draw.getNextPaint());
		colors.put("Cyan (Extra Dark)", draw.getNextPaint());
		colors.put("Red (Light)", draw.getNextPaint());
		colors.put("Blue (Light)", draw.getNextPaint());
		colors.put("Green (Light)", draw.getNextPaint());
		colors.put("Yellow (Light)", draw.getNextPaint());
		colors.put("Magenta (Light)", draw.getNextPaint());
		colors.put("Cyan (Light)", draw.getNextPaint());
		colors.put("Gray (Light)", new java.awt.Color(238, 238, 238));
		colorMap = Collections.unmodifiableMap(colors);
	}
	
	/**
	 * @return the colormap
	 */
	public static Map<String, Paint> getColorMap() {
		return colorMap;
	}

}
