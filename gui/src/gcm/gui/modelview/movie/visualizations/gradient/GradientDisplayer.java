package gcm.gui.modelview.movie.visualizations.gradient;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;


public class GradientDisplayer extends JComboBox {

	private static final long serialVersionUID = 1L;

	
	private MovieGradient movieGradient;
	
	// make a place to store the movieGradient that was passed in
	// so that when we save changes we can modify the original.
	private MovieGradient savedColorGradient;
	
	private JLabel label;
	
	public GradientDisplayer(MovieGradient movieGradient){
		super(MovieGradient.GRADIENT_NAMES);
		this.setLayout(new BorderLayout());
		
		label = new JLabel(movieGradient.getLabel());
		this.add(label, BorderLayout.CENTER);
		
		addListener();
		setup(movieGradient);
		savedColorGradient = movieGradient;
	}
	
	private void setup(MovieGradient movieGradient){
		
		this.movieGradient = movieGradient;
		
		if(movieGradient != null && movieGradient.getLabel() != null)
			label.setText(movieGradient.getLabel());
		else
			label.setText("");
	}

	/**
	 * Adds the listener that applies changes when the user chooses
	 * an item from the dropdown.
	 */
	private void addListener(){
		
		this.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setup(MovieGradient.getGradientFromName((String)getSelectedItem()));
			}
		});
	}

	public void paintComponent(Graphics g) {
		//super.paintComponent(g);
		
        Graphics2D g2d = (Graphics2D)g;
        GradientPaint grad;
        
        if(movieGradient.getStartAppearance().color != null && movieGradient.getEndAppearance().color != null){
	        grad = new GradientPaint(
	        		0, 0, 
	        		movieGradient.getStartAppearance().color, 
	        		this.getWidth(), 0,
	        		movieGradient.getEndAppearance().color);
	        g2d.setPaint(grad);
	        g2d.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 4, 4);
        }
        
        grad = new GradientPaint(0,0, Color.DARK_GRAY, this.getWidth(), 0, Color.DARK_GRAY);
        g2d.setPaint(grad);
        g2d.drawRoundRect(0, 0, this.getWidth()-1, this.getHeight()-1, 4, 4);
        
        
    }
    
	public void saveChanges(){
		savedColorGradient.setStartAppearance(movieGradient.getStartAppearance());
		savedColorGradient.setEndAppearance(movieGradient.getEndAppearance());
		savedColorGradient.setLabel(movieGradient.getLabel());
	}


	
    }
