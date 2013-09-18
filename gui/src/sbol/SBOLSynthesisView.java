package sbol;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import biomodel.util.GlobalConstants;

public class SBOLSynthesisView extends JTabbedPane implements ActionListener, Runnable {

	JComboBox methodBox;
	JComboBox libBox;
	JTextField numSolutionsText;
	
	public SBOLSynthesisView(Properties synthProps) {
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel optionsPanel = new JPanel(new BorderLayout());
		JPanel inputPanel = constructInputPanel(synthProps);
		JPanel methodPanel = constructMethodPanel(synthProps);
		addTab("Synthesis Options", topPanel);
		getComponentAt(getComponents().length - 1).setName("Synthesis Options");
//		JPanel testPanel = new JPanel();
//		addTab("Test", testPanel);
		getComponentAt(getComponents().length - 1).setName("Test");
		topPanel.add(optionsPanel, BorderLayout.CENTER);
		optionsPanel.add(inputPanel, BorderLayout.NORTH);
		optionsPanel.add(methodPanel, BorderLayout.CENTER);
		
	}
	
	private JPanel constructInputPanel(Properties synthProps) {
		JPanel inputPanel = new JPanel(new BorderLayout());
		JPanel specPanel = constructSpecPanel(synthProps);
		JPanel libPanel = constructLibraryPanel(synthProps);
		inputPanel.add(specPanel, BorderLayout.NORTH);
		inputPanel.add(libPanel, BorderLayout.CENTER);
		return inputPanel;
	}
	
	private JPanel constructSpecPanel(Properties synthProps) {
		JPanel specPanel = new JPanel();
		JLabel specLabel = new JLabel("Specification File:");
		JTextField specText = new JTextField(
				synthProps.getProperty(GlobalConstants.SBOL_SYNTH_SPEC_PROPERTY));
		specText.setEditable(false);
		specPanel.add(specLabel);
		specPanel.add(specText);
		return specPanel;
	}
	
	private JPanel constructLibraryPanel(Properties synthProps) {
		JPanel libPanel = new JPanel();
		JLabel libLabel = new JLabel("Library File:");
		libBox = new JComboBox(
				synthProps.getProperty(GlobalConstants.SBOL_SYNTH_LIBS_PROPERTY).split(","));
		libBox.setPrototypeDisplayValue("asynt_test_100_5");
		libPanel.add(libLabel);
		libPanel.add(libBox);
		return libPanel;
	}
	
	private JPanel constructMethodPanel(Properties synthProps) {
		JPanel topPanel = new JPanel();
		JPanel methodPanel = new JPanel(new BorderLayout());
		JPanel labelPanel = constructMethodLabelPanel();
		JPanel inputPanel = constructMethodInputPanel(synthProps);
		topPanel.add(methodPanel);
		methodPanel.add(labelPanel, BorderLayout.WEST);
		methodPanel.add(inputPanel, BorderLayout.CENTER);
		return topPanel;
	}
	
	private JPanel constructMethodLabelPanel() {
		JPanel labelPanel = new JPanel(new GridLayout(2, 1));
		labelPanel.add(new JLabel("Synthesis Method:  "));
		labelPanel.add(new JLabel("Number of Solutions:  "));
		return labelPanel;
	}
	
	private JPanel constructMethodInputPanel(Properties synthProps) {
		JPanel inputPanel = new JPanel(new GridLayout(2, 1));
		methodBox = new JComboBox(GlobalConstants.SBOL_SYNTH_STRUCTURAL_METHODS.split(","));
		methodBox.setSelectedIndex(Integer.parseInt(
				synthProps.getProperty(GlobalConstants.SBOL_SYNTH_METHOD_PROPERTY)));
		numSolutionsText = new JTextField(
				synthProps.getProperty(GlobalConstants.SBOL_SYNTH_NUM_SOLNS_PROPERTY), 39);
		inputPanel.add(methodBox);
		inputPanel.add(numSolutionsText);
		return inputPanel;
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.println("test");
	}
	
	public void run() {
		
	}
	
	public void save(JPanel synthTab) {
		
	}
	
	public boolean hasChanged(JPanel synthTab) {
		return false;
	}
}
