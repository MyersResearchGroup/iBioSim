package util;

import java.awt.Graphics;
import javax.swing.JButton;
import java.awt.*;

public class myButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = 48205208619794848L;

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Dimension originalSize = super.getPreferredSize();
		int gap = (int) (originalSize.height * 0.2);
		int x = originalSize.width + gap;
		int y = gap;
		int diameter = originalSize.height - (gap * 2);

		g.setColor(Color.BLACK);
		g.fillOval(x, y, diameter, diameter);
	}
}