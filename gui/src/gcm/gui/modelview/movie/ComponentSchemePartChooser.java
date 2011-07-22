package gcm.gui.modelview.movie;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.apache.batik.css.engine.value.svg.SpacingManager;
import org.jfree.ui.tabbedui.VerticalLayout;

import parser.TSDParser;

import sun.java2d.pipe.SolidTextRenderer;

public class ComponentSchemePartChooser extends JPanel {

	private static final long serialVersionUID = 1L;

	private ComponentSchemePart componentSchemePart;
	
	private ColorSchemeChooser colorSchemeChooser;
	private TSDKeyChooser tsdKeyChooser;
	private JRadioButton enabledButton;
	private String compName;
	private TSDParser tsdParser;
	
	
	public ComponentSchemePartChooser(ComponentSchemePart componentSchemePart, String compName, TSDParser tsdParser) {
		// TODO Auto-generated constructor stub
		super();
		
		this.componentSchemePart = componentSchemePart;
		this.compName = compName;
		this.tsdParser = tsdParser;
				
		buildGUI();
	}

	private void buildGUI(){
		this.setLayout(new VerticalLayout());
		
		colorSchemeChooser = new ColorSchemeChooser(componentSchemePart.getColorScheme());
		this.add(colorSchemeChooser);
		
		JLabel l = new JLabel("Choose Internal Species");
		this.add(l);
		
		tsdKeyChooser = new TSDKeyChooser(compName, tsdParser, componentSchemePart.getTsdKey());
		this.add(tsdKeyChooser);
		
		JSeparator sep = new JSeparator();
		sep.setPreferredSize(new Dimension(100, 20));
		this.add(sep);

		this.setBorder(new EmptyBorder(5, 5, 5, 5));
	}
	
	public void saveChanges(){
		colorSchemeChooser.saveChanges();
		componentSchemePart.setTsdKey( tsdKeyChooser.getSelectedKey() );
	}

}
