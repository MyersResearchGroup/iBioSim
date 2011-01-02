package gcm2sbml.gui.modelview.movie.visualizations.gradient;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;


public class GradientDisplayer extends JComboBox {

	private static final long serialVersionUID = 1L;

	
	private ColorGradient colorGradient;
	
	// make a place to store the colorGradient that was passed in
	// so that when we save changes we can modify the original.
	private ColorGradient savedColorGradient;
	
	public GradientDisplayer(ColorGradient colorGradient){
		super(ColorGradient.GRADIENT_NAMES);
		this.setLayout(new BorderLayout());
		addListener();
		setup(colorGradient);
		savedColorGradient = colorGradient;
	}
	
	private void setup(ColorGradient colorGradient){
		
		this.colorGradient = colorGradient;
		
		// Why did this stop working when I switched to a ComboBox?
//		this.removeAll();
//
//		// optionally add a label
//		if(!colorGradient.getLabel().equals("")){
//			JLabel l = new JLabel(colorGradient.getLabel());
//			l.setHorizontalAlignment(JLabel.CENTER);
//			this.add(l, BorderLayout.CENTER);
//			
//		}
	
	}

	/**
	 * Adds the listener that applies changes when the user chooses
	 * an item from the dropdown.
	 */
	private void addListener(){
		
		this.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setup(ColorGradient.getGradientFromName((String)getSelectedItem()));
			}
		});
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
		savedColorGradient.setStartColor(colorGradient.getStartColor());
		savedColorGradient.setEndColor(colorGradient.getEndColor());
		savedColorGradient.setLabel(colorGradient.getLabel());
	}


	
    }
