package biomodel.gui.sbmlcore;

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

import main.Gui;
import main.util.MutableBoolean;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.UnitDefinition;
import biomodel.annotation.AnnotationUtility;
import biomodel.annotation.SBOLAnnotation;
import biomodel.gui.fba.FBAObjective;
import biomodel.gui.sbol.SBOLField;
import biomodel.gui.schematic.ModelEditor;
import biomodel.parser.BioModel;
import biomodel.util.GlobalConstants;
import biomodel.util.SBMLutilities;


public class ModelPanel extends JButton implements ActionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	private JTextField modelID; // the model's ID

	private JTextField modelName; // the model's Name

	private JButton fbaoButton;

	private JComboBox substanceUnits, timeUnits, volumeUnits, areaUnits, lengthUnits, extentUnits, conversionFactor;
	
	private JTextField conviIndex, convjIndex;

	private SBOLField sbolField;
	
	private BioModel bioModel;
	
	private Model sbmlModel;

	private MutableBoolean dirty;

	public ModelPanel(BioModel gcm, ModelEditor modelEditor) {
		super();
		this.bioModel = gcm;
		sbolField = new SBOLField(GlobalConstants.SBOL_DNA_COMPONENT, modelEditor, 1, true);
		this.sbmlModel = gcm.getSBMLDocument().getModel();
		this.dirty = modelEditor.getDirty();
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
		modelEditorPanel = new JPanel(new GridLayout(12, 2));
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
		conviIndex = new JTextField(10);
		convjIndex = new JTextField(10);
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

			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getUnitDefinitionCount(); i++) {
				UnitDefinition unit = bioModel.getSBMLDocument().getModel().getUnitDefinition(i);
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
			
			for (int i = 0; i < bioModel.getSBMLDocument().getModel().getParameterCount(); i++) {
				Parameter param = bioModel.getSBMLDocument().getModel().getParameter(i);
				if (param.getConstant() && !BioModel.IsDefaultParameter(param.getId())) {
					conversionFactor.addItem(param.getId());
				}
			}
			if (option.equals("OK")) {
				substanceUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getSubstanceUnits());
				timeUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getTimeUnits());
				volumeUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getVolumeUnits());
				areaUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getAreaUnits());
				lengthUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getLengthUnits());
				extentUnits.setSelectedItem(bioModel.getSBMLDocument().getModel().getExtentUnits());
				conversionFactor.setSelectedItem(bioModel.getSBMLDocument().getModel().getConversionFactor());
				
				String[] indices= new String[2];
				// TODO: Scott - change for Plugin reading
				indices[0] = AnnotationUtility.parseConversionRowIndexAnnotation(model);
				if(indices[0]!=null){
					indices[1] = AnnotationUtility.parseConversionColIndexAnnotation(model);
					if(indices[1]==null){
						conviIndex.setText(indices[0]);
						convjIndex.setText("");
					}
					else{
						conviIndex.setText(indices[0]);
						convjIndex.setText(indices[1]);
					}
				}
				else{
					conviIndex.setText("");
					convjIndex.setText("");
				}
			}
			
			fbaoButton = new JButton("Edit Objectives");
			fbaoButton.setActionCommand("fluxObjective");
			fbaoButton.addActionListener(this);
			
			JPanel conversionFactorIndices = new JPanel(new GridLayout(1,2));
			conversionFactorIndices.add(conviIndex);
			conversionFactorIndices.add(convjIndex);
			
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
			modelEditorPanel.add(conversionFactorIndices);
			modelEditorPanel.add(new JLabel("SBOL DNA Component:"));
			modelEditorPanel.add(sbolField);
			modelEditorPanel.add(new JLabel("Flux Objective: "));
			modelEditorPanel.add(fbaoButton);
		}
		Object[] options = { option, "Cancel" };
		int value = JOptionPane.showOptionDialog(Gui.frame, modelEditorPanel, "Model Editor", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (value != JOptionPane.YES_OPTION)
			sbolField.resetRemovedBioSimURI();
		boolean error = true;
		while (error && value == JOptionPane.YES_OPTION) {
			error = false;
			// Add SBOL annotation to SBML model itself
			if (!error) {
				if (sbolField.getSBOLURIs().size() > 0) {
					SBOLAnnotation sbolAnnot = new SBOLAnnotation(sbmlModel.getMetaId(), sbolField.getSBOLURIs(), 
							sbolField.getSBOLStrand());
					AnnotationUtility.setSBOLAnnotation(sbmlModel, sbolAnnot);
				} else 
					AnnotationUtility.removeSBOLAnnotation(sbmlModel);
				// Deletes iBioSim composite components that have been removed from association panel
//				URI deletionURI = sbolField.getDeletionURI();
//				if (deletionURI != null)
//					for (String filePath : gcmEditor.getGui().getFilePaths(".sbol")) {
//						SBOLDocument sbolDoc = SBOLUtility.loadSBOLFile(filePath);
//						SBOLUtility.deleteDNAComponent(deletionURI, sbolDoc);
//						SBOLUtility.writeSBOLDocument(filePath, sbolDoc);
//					}
			}
			if (!error) {
				if (bioModel.getSBMLDocument().getLevel() > 2) {
					if (substanceUnits.getSelectedItem().equals("( none )")) {
						bioModel.getSBMLDocument().getModel().unsetSubstanceUnits();
					}
					else {
						bioModel.getSBMLDocument().getModel().setSubstanceUnits((String) substanceUnits.getSelectedItem());
					}
					if (timeUnits.getSelectedItem().equals("( none )")) {
						bioModel.getSBMLDocument().getModel().unsetTimeUnits();
					}
					else {
						bioModel.getSBMLDocument().getModel().setTimeUnits((String) timeUnits.getSelectedItem());
					}
					if (volumeUnits.getSelectedItem().equals("( none )")) {
						bioModel.getSBMLDocument().getModel().unsetVolumeUnits();
					}
					else {
						bioModel.getSBMLDocument().getModel().setVolumeUnits((String) volumeUnits.getSelectedItem());
					}
					if (areaUnits.getSelectedItem().equals("( none )")) {
						bioModel.getSBMLDocument().getModel().unsetAreaUnits();
					}
					else {
						bioModel.getSBMLDocument().getModel().setAreaUnits((String) areaUnits.getSelectedItem());
					}
					if (lengthUnits.getSelectedItem().equals("( none )")) {
						bioModel.getSBMLDocument().getModel().unsetLengthUnits();
					}
					else {
						bioModel.getSBMLDocument().getModel().setLengthUnits((String) lengthUnits.getSelectedItem());
					}
					if (extentUnits.getSelectedItem().equals("( none )")) {
						bioModel.getSBMLDocument().getModel().unsetExtentUnits();
					}
					else {
						bioModel.getSBMLDocument().getModel().setExtentUnits((String) extentUnits.getSelectedItem());
					}
					if (conversionFactor.getSelectedItem().equals("( none )")) {
						bioModel.getSBMLDocument().getModel().unsetConversionFactor();
					}
					else {
						bioModel.getSBMLDocument().getModel().setConversionFactor((String) conversionFactor.getSelectedItem());

					}
					bioModel.getSBMLDocument().getModel().setName(modelName.getText());
					// TODO: Scott - change for Plugin writing
					if (!conviIndex.getText().equals("")) {
						AnnotationUtility.setConversionRowIndexAnnotation(model,conviIndex.getText());
					} else {
						AnnotationUtility.removeConversionRowIndexAnnotation(model);
					} 
					if (!convjIndex.getText().equals("")) {
						AnnotationUtility.setConversionColIndexAnnotation(model,convjIndex.getText());
					} else {						
						AnnotationUtility.removeConversionColIndexAnnotation(model);
					}
				}
				dirty.setValue(true);
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
		}
		else if (e.getActionCommand().equals("fluxObjective")){
			FBAObjective fbaObjective = new FBAObjective(bioModel);
			fbaObjective.openGui();
		}
		// if the variable is changed
		else if (e.getSource() == conversionFactor) {
			SBase variable = (SBase) SBMLutilities.getElementBySId(bioModel.getSBMLDocument(), (String)conversionFactor.getSelectedItem());
			String[] sizes = new String[2];
			// TODO: Scott - change for Plugin reading
			sizes[0] = AnnotationUtility.parseVectorSizeAnnotation(variable);
			if(sizes[0]==null){
				sizes = AnnotationUtility.parseMatrixSizeAnnotation(variable);
				if(sizes==null){
					conviIndex.setEnabled(false);
					convjIndex.setEnabled(false);
				}
				else{
					conviIndex.setEnabled(true);
					convjIndex.setEnabled(true);
				}
			}
			else{
				conviIndex.setEnabled(true);
				convjIndex.setEnabled(false);
			}
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
	
	public SBOLField getSBOLField() {
		return sbolField;
	}
}
