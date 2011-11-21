package biomodel.gui.textualeditor;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;
import main.util.MutableBoolean;

import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.UnitDefinition;


public class ModelPanel extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JTextField modelID; // the model's ID

	private JTextField modelName; // the model's Name

	private JButton modelUnits;

	private JComboBox substanceUnits, timeUnits, volumeUnits, areaUnits, lengthUnits, extentUnits, conversionFactor;

	private SBMLDocument document;

	private MutableBoolean dirty;

	public ModelPanel(SBMLDocument document, MutableBoolean dirty, boolean paramsOnly) {
		super();
		this.document = document;
		this.dirty = dirty;
		Model model = document.getModel();
		modelName = new JTextField(model.getName(), 50);
		modelID = new JTextField(model.getId(), 16);
		modelName = new JTextField(model.getName(), 40);
		JLabel modelIDLabel = new JLabel("Model ID:");
		JLabel modelNameLabel = new JLabel("Model Name:");
		modelUnits = new JButton("Model Units");
		modelUnits.addActionListener((ActionListener) this);
		modelID.setEditable(false);
		this.add(modelIDLabel);
		this.add(modelID);
		this.add(modelNameLabel);
		this.add(modelName);
		if (document.getLevel() > 2) {
			this.add(modelUnits);
		}
		if (paramsOnly) {
			modelName.setEnabled(false);
			modelUnits.setEnabled(false);
		}
	}

	public void setModelId(String newId) {
		modelID.setText(newId);
	}

	/**
	 * Creates a frame used to edit parameters or create new ones.
	 */
	private void modelUnitsEditor(String option) {
		JPanel modelUnitsPanel;
		modelUnitsPanel = new JPanel(new GridLayout(7, 2));
		JLabel substanceUnitsLabel = new JLabel("Substance Units:");
		JLabel timeUnitsLabel = new JLabel("Time Units:");
		JLabel volumeUnitsLabel = new JLabel("Volume Units:");
		JLabel areaUnitsLabel = new JLabel("Area Units:");
		JLabel lengthUnitsLabel = new JLabel("Length Units:");
		JLabel extentUnitsLabel = new JLabel("Extent Units:");
		JLabel conversionFactorLabel = new JLabel("Conversion Factor:");
		substanceUnits = new JComboBox();
		substanceUnits.addItem("( none )");
		timeUnits = new JComboBox();
		timeUnits.addItem("( none )");
		volumeUnits = new JComboBox();
		volumeUnits.addItem("( none )");
		areaUnits = new JComboBox();
		areaUnits.addItem("( none )");
		lengthUnits = new JComboBox();
		lengthUnits.addItem("( none )");
		extentUnits = new JComboBox();
		extentUnits.addItem("( none )");
		conversionFactor = new JComboBox();
		conversionFactor.addItem("( none )");
		ListOf listOfUnits = document.getModel().getListOfUnitDefinitions();
		for (int i = 0; i < document.getModel().getNumUnitDefinitions(); i++) {
			UnitDefinition unit = (UnitDefinition) listOfUnits.get(i);
			if ((unit.getNumUnits() == 1)
					&& (unit.getUnit(0).isMole() || unit.getUnit(0).isItem() || unit.getUnit(0).isGram() || unit.getUnit(0).isKilogram())
					&& (unit.getUnit(0).getExponentAsDouble() == 1)) {
				substanceUnits.addItem(unit.getId());
				extentUnits.addItem(unit.getId());
			}
			if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isSecond()) && (unit.getUnit(0).getExponentAsDouble() == 1)) {
				timeUnits.addItem(unit.getId());
			}
			if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponentAsDouble() == 1)
					|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 3)) {
				volumeUnits.addItem(unit.getId());
			}
			if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 2)) {
				areaUnits.addItem(unit.getId());
			}
			if ((unit.getNumUnits() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponentAsDouble() == 1)) {
				lengthUnits.addItem(unit.getId());
			}
		}
		substanceUnits.addItem("dimensionless");
		substanceUnits.addItem("gram");
		substanceUnits.addItem("item");
		substanceUnits.addItem("kilogram");
		substanceUnits.addItem("mole");
		timeUnits.addItem("dimensionless");
		timeUnits.addItem("second");
		volumeUnits.addItem("dimensionless");
		volumeUnits.addItem("litre");
		areaUnits.addItem("dimensionless");
		lengthUnits.addItem("dimensionless");
		lengthUnits.addItem("metre");
		extentUnits.addItem("dimensionless");
		extentUnits.addItem("gram");
		extentUnits.addItem("item");
		extentUnits.addItem("kilogram");
		extentUnits.addItem("mole");
		ListOf listOfParameters = document.getModel().getListOfParameters();
		for (int i = 0; i < document.getModel().getNumParameters(); i++) {
			Parameter param = (Parameter) listOfParameters.get(i);
			if (param.getConstant()) {
				conversionFactor.addItem(param.getId());
			}
		}
		if (option.equals("OK")) {
			substanceUnits.setSelectedItem(document.getModel().getSubstanceUnits());
			timeUnits.setSelectedItem(document.getModel().getTimeUnits());
			volumeUnits.setSelectedItem(document.getModel().getVolumeUnits());
			areaUnits.setSelectedItem(document.getModel().getAreaUnits());
			lengthUnits.setSelectedItem(document.getModel().getLengthUnits());
			extentUnits.setSelectedItem(document.getModel().getExtentUnits());
			conversionFactor.setSelectedItem(document.getModel().getConversionFactor());
		}
		modelUnitsPanel.add(substanceUnitsLabel);
		modelUnitsPanel.add(substanceUnits);
		modelUnitsPanel.add(timeUnitsLabel);
		modelUnitsPanel.add(timeUnits);
		modelUnitsPanel.add(volumeUnitsLabel);
		modelUnitsPanel.add(volumeUnits);
		modelUnitsPanel.add(areaUnitsLabel);
		modelUnitsPanel.add(areaUnits);
		modelUnitsPanel.add(lengthUnitsLabel);
		modelUnitsPanel.add(lengthUnits);
		modelUnitsPanel.add(extentUnitsLabel);
		modelUnitsPanel.add(extentUnits);
		modelUnitsPanel.add(conversionFactorLabel);
		modelUnitsPanel.add(conversionFactor);
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, modelUnitsPanel, "Model Units Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if (substanceUnits.getSelectedItem().equals("( none )")) {
				document.getModel().unsetSubstanceUnits();
			}
			else {
				document.getModel().setSubstanceUnits((String) substanceUnits.getSelectedItem());
			}
			if (timeUnits.getSelectedItem().equals("( none )")) {
				document.getModel().unsetTimeUnits();
			}
			else {
				document.getModel().setTimeUnits((String) timeUnits.getSelectedItem());
			}
			if (volumeUnits.getSelectedItem().equals("( none )")) {
				document.getModel().unsetVolumeUnits();
			}
			else {
				document.getModel().setVolumeUnits((String) volumeUnits.getSelectedItem());
			}
			if (areaUnits.getSelectedItem().equals("( none )")) {
				document.getModel().unsetAreaUnits();
			}
			else {
				document.getModel().setAreaUnits((String) areaUnits.getSelectedItem());
			}
			if (lengthUnits.getSelectedItem().equals("( none )")) {
				document.getModel().unsetLengthUnits();
			}
			else {
				document.getModel().setLengthUnits((String) lengthUnits.getSelectedItem());
			}
			if (extentUnits.getSelectedItem().equals("( none )")) {
				document.getModel().unsetExtentUnits();
			}
			else {
				document.getModel().setExtentUnits((String) extentUnits.getSelectedItem());
			}
			if (conversionFactor.getSelectedItem().equals("( none )")) {
				document.getModel().unsetConversionFactor();
			}
			else {
				document.getModel().setConversionFactor((String) conversionFactor.getSelectedItem());
			}
			dirty.setValue(true);
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, modelUnitsPanel, "Model Units Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
		if (value == JOptionPane.NO_OPTION) {
			return;
		}
	}

	/**
	 * Set the model ID
	 */
	public void setModelID(String modelID) {
		this.modelID.setText(modelID);
		document.getModel().setId(modelID);
	}

	/**
	 * Get the model name
	 */
	public String getModelName() {
		return modelName.getText().trim();
	}

	public void actionPerformed(ActionEvent e) {
		// if the add unit button is clicked
		// if the add function button is clicked
		if (e.getSource() == modelUnits) {
			modelUnitsEditor("OK");
		}

	}

	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	public void mouseReleased(MouseEvent e) {
	}
}
