package edu.utah.ece.async.ibiosim.dataModels.graphData;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;

public class ShapeAndPaint {
	
	private Shape shape;

	private Paint paint;

	public ShapeAndPaint(Shape s, String p) {
		shape = s;
		if (p.startsWith("Custom_")) {
			paint = new Color(Integer.parseInt(p.replace("Custom_", "")));
		}
		else {
			paint = ColorMap.getColorMap().get(p);
		}
	}

	public Shape getShape() {
		return shape;
	}

	public Paint getPaint() {
		return paint;
	}

	public String getShapeName() {
		Object[] set = ShapeMap.getShapeMap().keySet().toArray();
		for (int i = 0; i < set.length; i++) {
			if (shape == ShapeMap.getShapeMap().get(set[i])) {
				return (String) set[i];
			}
		}
		return "Unknown Shape";
	}

	public String getPaintName() {
		Object[] set = ColorMap.getColorMap().keySet().toArray();
		for (int i = 0; i < set.length; i++) {
			if (paint == ColorMap.getColorMap().get(set[i])) {
				return (String) set[i];
			}
		}
		return "Custom_" + ((Color) paint).getRGB();
	}

	public void setPaint(String paint) {
		if (paint.startsWith("Custom_")) {
			this.paint = new Color(Integer.parseInt(paint.replace("Custom_", "")));
		}
		else {
			this.paint = ColorMap.getColorMap().get(paint);
		}
	}

	public void setShape(String shape) {
		this.shape = ShapeMap.getShapeMap().get(shape);
	}

}
