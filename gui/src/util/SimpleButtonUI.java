package util;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

public class SimpleButtonUI extends BasicButtonUI implements java.io.Serializable {

	private static final long serialVersionUID = 4017702354321432325L;

	public void paint(Graphics g, JComponent c) {
		AbstractButton b = (AbstractButton) c;
		Dimension d = b.getSize();

		g.setFont(c.getFont());
		FontMetrics fm = g.getFontMetrics();

		g.setColor(b.getForeground());
		String caption = b.getText();
		int x = (d.width - fm.stringWidth(caption)) / 2;
		int y = (d.height + fm.getAscent()) / 2;
		g.drawString(caption, x, y);

	}

	public Dimension getPreferredSize(JComponent c) {
		Dimension d = super.getPreferredSize(c);
		d.setSize(d.width, d.height);
		return d;
	}
}