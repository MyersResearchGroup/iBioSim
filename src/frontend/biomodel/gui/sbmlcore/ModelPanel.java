package frontend.biomodel.gui.sbmlcore;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.UnitDefinition;

import dataModels.biomodel.annotation.AnnotationUtility;
import dataModels.biomodel.annotation.SBOLAnnotation;
import dataModels.biomodel.parser.BioModel;
import dataModels.biomodel.util.SBMLutilities;
import dataModels.util.GlobalConstants;
import frontend.biomodel.gui.fba.FBAObjective;
import frontend.biomodel.gui.sbol.SBOLField2;
import frontend.biomodel.gui.schematic.ModelEditor;
import frontend.main.Gui;


public class ModelPanel extends JButton implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JTextField modelID; // the model's ID

	private JTextField modelName; // the model's Name

	private JButton fbaoButton;

	private JComboBox substanceUnits, timeUnits, volumeUnits, areaUnits, lengthUnits, extentUnits, conversionFactor, framework;
	
	private JTextField conviIndex;

	private SBOLField2 sbolField;
	
	private BioModel bioModel;
	
	private Model sbmlModel;
	
	private ModelEditor modelEditor;
	
	public ModelPanel(BioModel gcm, ModelEditor modelEditor) {
		super();
		this.bioModel = gcm;
		this.modelEditor = modelEditor;
		sbolField = new SBOLField2(GlobalConstants.SBOL_COMPONENTDEFINITION, modelEditor, 1, true);
		this.sbmlModel = gcm.getSBMLDocument().getModel();
		this.setText("Model");
		this.setToolTipText("Edit Model Attributes");
		this.addActionListener(this);
		if (modelEditor.isParamsOnly()) {
			this.setEnabled(false);
		}
	}

	/**
	 * Creates a frame used to edit parameters or create new ones.
	 */
	private void modelEditor(String option) {
		JPanel modelEditorPanel;
		modelEditorPanel = new JPanel(new GridLayout(13, 2));
		Model model = bioModel.getSBMLDocument().getModel();
		modelName = new JTextField(model.getName(), 50);
		modelID = new JTextField(model.getId(), 16);
		modelName = new JTextField(model.getName(), 40);
		JLabel modelIDLabel = new JLabel("Model ID:");
		JLabel modelNameLabel = new JLabel("Model Name:");
		modelID.setEditable(false);
		modelEditorPanel.add(modelIDLabel);
		modelEditorPanel.add(modelID);
		modelEditorPanel.add(modelNameLabel);
		modelEditorPanel.add(modelName);
		conviIndex = new JTextField(20);
		if (bioModel.getSBMLDocument().getLevel() > 2) {
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
			conversionFactor.addActionListener(this);
			conversionFactor.addItem("( none )");

			for (int i = 0; i < model.getUnitDefinitionCount(); i++) {
				UnitDefinition unit = model.getUnitDefinition(i);
				if ((unit.getUnitCount() == 1)
						&& (unit.getUnit(0).isMole() || unit.getUnit(0).isItem() || unit.getUnit(0).isGram() || unit.getUnit(0).isKilogram())
						&& (unit.getUnit(0).getExponent() == 1)) {
					substanceUnits.addItem(unit.getId());
					extentUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isSecond()) && (unit.getUnit(0).getExponent() == 1)) {
					timeUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isLitre() && unit.getUnit(0).getExponent() == 1)
						|| (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 3)) {
					volumeUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 2)) {
					areaUnits.addItem(unit.getId());
				}
				if ((unit.getUnitCount() == 1) && (unit.getUnit(0).isMetre() && unit.getUnit(0).getExponent() == 1)) {
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
			
			List<URI> sbolURIs = new LinkedList<URI>();
			String sbolStrand = AnnotationUtility.parseSBOLAnnotation(sbmlModel, sbolURIs);
			sbolField.setSBOLURIs(sbolURIs);
			sbolField.setSBOLStrand(sbolStrand);
			
			for (int i = 0; i < model.getParameterCount(); i++) {
				Parameter param = model.getParameter(i);
				if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
					conversionFactor.addItem(param.getId());
				}
			}
			if (option.equals("OK")) {
				substanceUnits.setSelectedItem(model.getSubstanceUnits());
				timeUnits.setSelectedItem(model.getTimeUnits());
				volumeUnits.setSelectedItem(model.getVolumeUnits());
				areaUnits.setSelectedItem(model.getAreaUnits());
				lengthUnits.setSelectedItem(model.getLengthUnits());
				extentUnits.setSelectedItem(model.getExtentUnits());
				conversionFactor.setSelectedItem(model.getConversionFactor());
				
				String freshIndex = "";
				SBMLutilities.getIndicesString(model, "conversionFactor");
				conviIndex.setText(freshIndex);
			}
			
			fbaoButton = new JButton("Edit Objectives");
			fbaoButton.setActionCommand("fluxObjective");
			fbaoButton.addActionListener(this);
			// TODO: interaction?
			framework = new JComboBox(SBMLutilities.getSortedListOfSBOTerms(GlobalConstants.SBO_FRAMEWORK));
			framework.addActionListener(this);
			if (model.isSetSBOTerm()) {
				framework.setSelectedItem(SBMLutilities.sbo.getName(model.getSBOTermID()));
			}
			modelEditorPanel.add(substanceUnitsLabel);
			modelEditorPanel.add(substanceUnits);
			modelEditorPanel.add(timeUnitsLabel);
			modelEditorPanel.add(timeUnits);
			modelEditorPanel.add(volumeUnitsLabel);
			modelEditorPanel.add(volumeUnits);
			modelEditorPanel.add(areaUnitsLabel);
			modelEditorPanel.add(areaUnits);
			modelEditorPanel.add(lengthUnitsLabel);
			modelEditorPanel.add(lengthUnits);
			modelEditorPanel.add(extentUnitsLabel);
			modelEditorPanel.add(extentUnits);
			modelEditorPanel.add(conversionFactorLabel);
			modelEditorPanel.add(conversionFactor);
			modelEditorPanel.add(new JLabel("Conversion Factor Indices:"));
			modelEditorPanel.add(conviIndex);
			modelEditorPanel.add(new JLabel("SBOL ComponentDefinition:"));
			modelEditorPanel.add(sbolField);
			modelEditorPanel.add(new JLabel("Flux Objective:"));
			modelEditorPanel.add(fbaoButton);
			modelEditorPanel.add(new JLabel(GlobalConstants.SBOTERM));
			modelEditorPanel.add(framework);
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, modelEditorPanel, "Model Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value != JOptionPane.YES_OPTION)
			sbolField.resetRemovedBioSimURI();
		String[] dex = new String[]{""};
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			if(!error&&!conversionFactor.getSelectedItem().equals("( none )")){
				SBase variable = SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)conversionFactor.getSelectedItem());
				dex = SBMLutilities.checkIndices(conviIndex.getText(), variable, bioModel.getSBMLDocument(), null, "conversionFactor", 
						null, null, null);
				error = (dex==null);
			}
			// Add SBOL annotation to SBML model itself
			if (!error) {
				if (sbolField.getSBOLURIs().size() > 0) {
					if (!sbmlModel.isSetMetaId() || sbmlModel.getMetaId().equals(""))
						SBMLutilities.setDefaultMetaID(bioModel.getSBMLDocument(), sbmlModel, 
								bioModel.getMetaIDIndex());
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlModel.getMetaId(), sbolField.getSBOLURIs(),
							sbolField.getSBOLStrand());
					AnnotationUtility.setSBOLAnnotation(sbmlModel, sbolAnnot);
				} else 
					AnnotationUtility.removeSBOLAnnotation(sbmlModel);
			}
			if (!error) {
				if (substanceUnits.getSelectedItem().equals("( none )")) {
					model.unsetSubstanceUnits();
				}
				else {
					model.setSubstanceUnits((String) substanceUnits.getSelectedItem());
				}
				if (timeUnits.getSelectedItem().equals("( none )")) {
					model.unsetTimeUnits();
				}
				else {
					model.setTimeUnits((String) timeUnits.getSelectedItem());
				}
				if (volumeUnits.getSelectedItem().equals("( none )")) {
					model.unsetVolumeUnits();
				}
				else {
					model.setVolumeUnits((String) volumeUnits.getSelectedItem());
				}
				if (areaUnits.getSelectedItem().equals("( none )")) {
					model.unsetAreaUnits();
				}
				else {
					model.setAreaUnits((String) areaUnits.getSelectedItem());
				}
				if (lengthUnits.getSelectedItem().equals("( none )")) {
					model.unsetLengthUnits();
				}
				else {
					model.setLengthUnits((String) lengthUnits.getSelectedItem());
				}
				if (extentUnits.getSelectedItem().equals("( none )")) {
					model.unsetExtentUnits();
				}
				else {
					model.setExtentUnits((String) extentUnits.getSelectedItem());
				}
				if (conversionFactor.getSelectedItem().equals("( none )")) {
					model.unsetConversionFactor();
					SBMLutilities.addIndices(model, "conversionFactor", null, 1);
				}
				else {
					model.setConversionFactor((String) conversionFactor.getSelectedItem());
					SBMLutilities.addIndices(model, "conversionFactor", dex, 1);
				}
				if (framework.getSelectedItem().equals("(unspecified)")) {
					model.unsetSBOTerm();
				} else {
					model.setSBOTerm(SBMLutilities.sbo.getId((String)framework.getSelectedItem()));
				}
				model.setName(modelName.getText());
				modelEditor.setDirty(true);
				bioModel.makeUndoPoint();
			}
			if (error) {
				value = JOptionPane.showOptionDialog(Gui.frame, modelEditorPanel, "Model Units Editor", JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if the add unit button is clicked
		// if the add function button is clicked
		if (e.getSource() == this) {
			modelEditor("OK");
		} else if (e.getActionCommand().equals("editDescriptors")) {
//			if (bioModel.getSBOLDescriptors() != null)
//				SBOLDescriptorPanel descriptorPanel = new SBOLDescriptorPanel(sbolField.getSBOLURIs().get(0))
			modelEditor.setDirty(true);
		}
		else if (e.getActionCommand().equals("fluxObjective")){
			FBAObjective fbaObjective = new FBAObjective(bioModel);
			fbaObjective.openGui();
			modelEditor.setDirty(true);
		}
		else if (e.getActionCommand().equals("comboBoxChanged")){
			if (conversionFactor.getSelectedItem().equals("( none )")) {
				conviIndex.setText("");
				conviIndex.setEnabled(false);
			} else {
				if (bioModel.isArray((String)conversionFactor.getSelectedItem())) {
					conviIndex.setEnabled(true);
				} else {
					conviIndex.setText("");
					conviIndex.setEnabled(false);
				}
			}
			//modelEditor.setDirty(true);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * This method currently does nothing.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	public SBOLField2 getSBOLField() {
		return sbolField;
	}
}
