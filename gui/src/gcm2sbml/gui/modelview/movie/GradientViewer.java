package gcm2sbml.gui.modelview.movie;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Point2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class GradientViewer extends JPanel {

	private Color _startCol;
	private Color _endCol;
	
	public GradientViewer(Color startCol, Color endCol){
		super();
		setup(startCol, endCol, "");
	}
	public GradientViewer(Color startCol, Color endCol, String label){
		super();
		setup(startCol, endCol, label);
	}
	
	private void setup(Color startCol, Color endCol, String label){
		_startCol = startCol;
		_endCol = endCol;

		// optionally add a label
		if(!label.equals("")){
			this.setLayout(new BorderLayout());
			JLabel l = new JLabel(label);
			l.setHorizontalAlignment(l.CENTER);
			this.add(l, BorderLayout.CENTER);
		}
	}
	
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        //drawing messages sent to g
        GradientPaint grad = new GradientPaint(
        		0, 0, 
        		_startCol, 
        		this.getWidth(), 0,
        		_endCol);
        g2d.setPaint(grad);
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 4, 4);
        
        grad = new GradientPaint(0,0, Color.DARK_GRAY, this.getWidth(), 0, Color.DARK_GRAY);
        g2d.setPaint(grad);
        g2d.drawRoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, 4, 4);
        
        
    }
}
