package util;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.GradientPaint;
import javax.swing.JButton;
import java.awt.Paint;
import java.awt.*;

public class myButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 48205208619794848L;

	public void paintComponent(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;
		GradientPaint p;
		p = new GradientPaint(0, 0, Color.RED, 0, getHeight(), Color.BLUE);

		Paint oldPaint = g2.getPaint();
		g2.setPaint(p);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setPaint(oldPaint);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		super.paintComponent(g);
	}
}