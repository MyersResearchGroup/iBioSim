package gcm2sbml.gui.modelview.movie;

import gcm2sbml.gui.modelview.movie.visualizations.ColorGradient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GradientEditor extends JButton {

	private static final long serialVersionUID = 1L;
	
	private ColorGradient colorGradient;
	
	public GradientEditor(ColorGradient colorGradient){
		super();
		setup(colorGradient);
	}
	
	private void setup(ColorGradient colorGradient){
		
		this.colorGradient = colorGradient;
		
		this.removeAll();

		// optionally add a label
		if(!colorGradient.getLabel().equals("")){
			this.setLayout(new BorderLayout());
			JLabel l = new JLabel(colorGradient.getLabel());
			l.setHorizontalAlignment(JLabel.CENTER);
			this.add(l, BorderLayout.CENTER);
		}
	
	}


	public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        //drawing messages sent to g
        GradientPaint grad = new GradientPaint(
        		0, 0, 
        		colorGradient.getStartColor(), 
        		this.getWidth(), 0,
        		colorGradient.getEndColor());
        g2d.setPaint(grad);
        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 4, 4);
        
        grad = new GradientPaint(0,0, Color.DARK_GRAY, this.getWidth(), 0, Color.DARK_GRAY);
        g2d.setPaint(grad);
        g2d.drawRoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, 4, 4);
    }
    
	public void saveChanges(){
		// TODO: Implement!
	}
	
    }
