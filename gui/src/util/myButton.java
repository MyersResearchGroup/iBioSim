package util;

import java.awt.Graphics2D;
import java.awt.Graphics;
import javax.swing.JButton;
import java.awt.Paint;
import java.awt.*;

public class myButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 48205208619794848L;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.getBackground();
		Graphics2D g2 = (Graphics2D) g;
		Paint p = this.getBackground();

		Paint oldPaint = g2.getPaint();
		g2.setPaint(p);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setPaint(oldPaint);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	protected void paintBorder(Graphics g) {
		this.getBorder().paintBorder(this, g, 0, 0, 20, 20);
	}
}