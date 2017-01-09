package frontend.biomodel.gui.sbol;

import java.awt.GridLayout;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import main.Gui;

import org.sbolstandard.core.DnaComponent;

import sbol.util.SBOLFileManager;
import sbol.util.SBOLIdentityManager;
import biomodel.util.Utility;

public class SBOLDescriptorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private String initialID;
	private JTextField idText, nameText, descriptionText;
	private JComboBox saveFileIDBox;
	private List<String> saveFilePaths;
	private String[] options = {"Ok", "Cancel"};
	private boolean removeBioSimURI = false;
	private boolean display;
	
	public SBOLDescriptorPanel(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		super(new GridLayout(4, 2));
		constructPanel(fileManager.getSBOLFilePaths());
		display = (loadSBOLDescriptors(identityManager) || 
				!identityManager.containsBioSimURI() || identityManager.containsPlaceHolderURI() ||
				loadBioSimComponentDescriptors(identityManager, fileManager));
		/*
		while (display)
			display = panelOpen(identityManager, fileManager);
			*/
	}
	
	public SBOLDescriptorPanel(String SBOLFileName, String displayId, String name, String description) {
		super(new GridLayout(4, 2));
		Set<String> SBOLFilePaths = new HashSet<String>();
		SBOLFilePaths.add(SBOLFileName);
		constructPanel(SBOLFilePaths);
		idText.setText(displayId);
		nameText.setText(name);
		descriptionText.setText(description);
		saveFileIDBox.setEnabled(false);
		idText.setEnabled(false);
		nameText.setEnabled(false);
		descriptionText.setEnabled(false);
	}
	
	public void openViewer() {
		JOptionPane.showOptionDialog(Gui.frame, this,
				"SBOL Descriptors", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}
	
	public void constructPanel(Set<String> sbolFilePaths) {
		idText = new JTextField("", 40);
		nameText = new JTextField("", 40);
		descriptionText = new JTextField("", 40);
		saveFilePaths = new LinkedList<String>(sbolFilePaths);
		saveFileIDBox = new JComboBox();
		for (String saveFilePath : saveFilePaths) {
			String regex = Gui.separator;
			String[] splitPath = saveFilePath.split(regex);
			saveFileIDBox.addItem(splitPath[splitPath.length - 1]);
		}
		add(new JLabel("Save SBOL DNA Component to File:"));
		add(saveFileIDBox);
		add(new JLabel("SBOL DNA Component ID:"));
		add(idText);
		add(new JLabel("SBOL DNA Component Name:"));
		add(nameText);
		add(new JLabel("SBOL DNA Component Description:"));
		add(descriptionText);
	}
	
	private boolean loadSBOLDescriptors(SBOLIdentityManager identityManager) {
		String[] sbolDescriptors = identityManager.getBioModel().getSBOLDescriptors();
		if (sbolDescriptors == null) {
			initialID = identityManager.getBioModel().getSBMLDocument().getModel().getId();
			idText.setText(initialID);
			return false;
		}
		initialID = sbolDescriptors[0];
		idText.setText(initialID);
		nameText.setText(sbolDescriptors[1]);
		descriptionText.setText(sbolDescriptors[2]);
		saveFileIDBox.setSelectedIndex(saveFilePaths.indexOf(identityManager.getBioModel().getSBOLSaveFilePath()));
		return true;
	}
	
	private boolean loadBioSimComponentDescriptors(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		if (identityManager.loadAndLocateBioSimComponent(fileManager)) {
			DnaComponent bioSimComp = identityManager.getBioSimComponent();
			if (bioSimComp != null) {
				saveFileIDBox.setSelectedIndex(saveFilePaths.indexOf(identityManager.getSaveFilePath()));
				initialID = bioSimComp.getDisplayId();
				idText.setText(initialID);
				if (bioSimComp.getName() != null)
					nameText.setText(bioSimComp.getName());
				if (bioSimComp.getDescription() != null)
					descriptionText.setText(bioSimComp.getDescription());
			} else 
				removeBioSimURI = true;
			return true;
		}
		return false;
	}
	
	public boolean panelOpen(SBOLIdentityManager identityManager, SBOLFileManager fileManager) {
		if (!display) return false;
		int option = JOptionPane.showOptionDialog(Gui.frame, this,
				"Composite SBOL Descriptors", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if (option == 0) {
			if (isSourceIdentifierInvalid(idText.getText()) || 
					isSourceIdentifierDuplicate(idText.getText(), 
							saveFilePaths.get(saveFileIDBox.getSelectedIndex()), fileManager))
				return true;
			String[] sbolDescriptors = new String[3];
			sbolDescriptors[0] = idText.getText();
			sbolDescriptors[1] = nameText.getText();
			sbolDescriptors[2] = descriptionText.getText(); 
			identityManager.getBioModel().setSBOLDescriptors(sbolDescriptors);
			identityManager.getBioModel().setSBOLSaveFilePath(saveFilePaths.get(saveFileIDBox.getSelectedIndex()));
			if (removeBioSimURI) {
				try {
					identityManager.replaceBioSimURI(new URI("http://www.async.ece.utah.edu#iBioSimPlaceHolder"));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				identityManager.annotateBioModel();
			}
			return false;
		}
		return false;
	}
	
	private static boolean isSourceIdentifierInvalid(String sourceID) {
		if (sourceID.equals("")) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is blank.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return true;
		} else if (!Utility.isValid(sourceID, Utility.IDstring)) {
			JOptionPane.showMessageDialog(Gui.frame, "Chosen ID is not alphanumeric.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			return true;
		} 
		return false;
	}
	
	private boolean isSourceIdentifierDuplicate(String sourceID, String saveFilePath, SBOLFileManager fileManager) {
		 if (!sourceID.equals(initialID) && fileManager.resolveDisplayID(sourceID, saveFilePath) != null) {
			 JOptionPane.showMessageDialog(Gui.frame, "Chosen SBOL file contains DNA component with chosen ID.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
			 return true;
		 } 
		 return false;
	}
}
