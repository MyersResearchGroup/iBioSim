package sbol;

import java.awt.*;
import javax.swing.*;

public class SbolBrowser {
	
	private JList libraryList;
	private JList componentList;
	private JList featureList;
	private TextArea libraryText;
	private TextArea componentText;
	private TextArea featureText;
	
	public SbolBrowser() {
		libraryList = new JList();
		componentList = new JList();
		featureList = new JList();
		
		JLabel libraryLabel = new JLabel("Libraries:");
		JLabel componentLabel = new JLabel("DNA Components:");
		JLabel featureLabel = new JLabel("Sequence Features:");
		
		JScrollPane libraryScroll = new JScrollPane();
		libraryScroll.setMinimumSize(new Dimension(260, 200));
		libraryScroll.setPreferredSize(new Dimension(276, 132));
		libraryScroll.setViewportView(libraryList);
		
		JScrollPane componentScroll = new JScrollPane();
		componentScroll.setMinimumSize(new Dimension(260, 200));
		componentScroll.setPreferredSize(new Dimension(276, 132));
		componentScroll.setViewportView(componentList);
		
		JScrollPane featureScroll = new JScrollPane();		
		featureScroll.setMinimumSize(new Dimension(260, 200));
		featureScroll.setPreferredSize(new Dimension(276, 132));
		featureScroll.setViewportView(featureList);
		
		JPanel labelPanel = new JPanel(new GridLayout(1, 3));
		labelPanel.add(libraryLabel);
		labelPanel.add(componentLabel);
		labelPanel.add(featureLabel);
		
		JPanel listPanel = new JPanel(new GridLayout(1, 3));
		listPanel.add(libraryScroll);
		listPanel.add(componentScroll);
		listPanel.add(featureScroll);
		
		JPanel browserPanel = new JPanel(new BorderLayout());
		browserPanel.add(labelPanel, "North");
		browserPanel.add(listPanel, "Center");
		
		libraryText = new TextArea();
	}
}
