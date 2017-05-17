package edu.utah.ece.async.ibiosim.gui.modelEditor.sbol;
//
//import java.awt.Component;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Set;
//
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JTable;
//import javax.swing.JTextField;
//import javax.swing.ListSelectionModel;
//import javax.swing.RowFilter;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import javax.swing.table.TableRowSorter;
//
//import org.sbolstandard.core2.ComponentDefinition;
//import org.sbolstandard.core2.SBOLDocument;
//import org.sbolstandard.core2.SBOLValidationException;
//import org.sbolstandard.core2.SBOLWriter;
//import org.sbolstandard.core2.SequenceOntology;
//
//import com.google.common.collect.Lists;
//
//import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils;
//import edu.utah.ece.async.sboldesigner.sbol.SBOLUtils.Types;
//import edu.utah.ece.async.sboldesigner.sbol.editor.Part;
//import edu.utah.ece.async.sboldesigner.sbol.editor.Parts;
//import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.InputDialog;
//import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PartCellRenderer;
//import edu.utah.ece.async.sboldesigner.swing.FormBuilder;
//
///**
// * A GUI for choosing a root CD from an SBOLDocument
// * 
// * @author Michael Zhang
// */
//public class RootInputDialog extends InputDialog<SBOLDocument> {
//	private static final String TITLE = "Select a root design to open";
//
//	private JTable table;
//	private JLabel tableLabel;
//
//	private JComboBox<Part> roleSelection;
//	private JComboBox<String> roleRefinement;
//	private JComboBox<Types> typeSelection;
//	private JCheckBox onlyShowRootCDs;
//	private JButton deleteCD;
//	private static final Part ALL_PARTS = new Part("All parts", "All");
//
//	private SBOLDocument doc;
//
//	/**
//	 * this.getInput() returns an SBOLDocument with a single rootCD selected
//	 * from the rootCDs in doc.
//	 */
//	public RootInputDialog(final Component parent, SBOLDocument doc) {
//		super(parent, TITLE);
//
//		this.doc = doc;
//	}
//
//	@Override
//	protected String initMessage() {
//		return "There are multiple designs.  Which would you like to load?  (You will be editing a new partial design)";
//	}
//
//	@Override
//	public void initFormPanel(FormBuilder builder) {
//		typeSelection = new JComboBox<Types>(Types.values());
//		typeSelection.setSelectedItem(Types.DNA);
//		typeSelection.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				updateTable();
//			}
//		});
//		builder.add("Part type", typeSelection);
//
//		List<Part> parts = Lists.newArrayList(Parts.sorted());
//		parts.add(0, ALL_PARTS);
//		roleSelection = new JComboBox<Part>(parts.toArray(new Part[0]));
//		roleSelection.setRenderer(new PartCellRenderer());
//		roleSelection.setSelectedItem(ALL_PARTS);
//		roleSelection.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				updateRoleRefinement();
//				updateTable();
//			}
//		});
//		builder.add("Part role", roleSelection);
//
//		// set up the JComboBox for role refinement
//		roleRefinement = new JComboBox<String>();
//		updateRoleRefinement();
//		roleRefinement.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				updateTable();
//			}
//		});
//		builder.add("Role refinement", roleRefinement);
//
//		onlyShowRootCDs = new JCheckBox("Only show root ComponentDefinitions");
//		onlyShowRootCDs.setSelected(true);
//		onlyShowRootCDs.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent event) {
//				updateTable();
//			}
//		});
//		builder.add("", onlyShowRootCDs);
//
//		deleteCD = new JButton("Delete selected part(s). (This will resave the file)");
//		deleteCD.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				try {
//					int[] rows = table.getSelectedRows();
//					for (int row : rows) {
//						row = table.convertRowIndexToModel(row);
//						ComponentDefinition comp = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);
//						doc.removeComponentDefinition(comp);
//					}
//					File file = SBOLUtils.setupFile();
//					SBOLWriter.write(doc, new FileOutputStream(file));
//					updateTable();
//				} catch (Exception e1) {
//					JOptionPane.showMessageDialog(rootPane, "Failed to delete CD: " + e1.getMessage());
//					e1.printStackTrace();
//				}
//			}
//		});
//		builder.add("", deleteCD);
//
//		final JTextField filterSelection = new JTextField();
//		filterSelection.getDocument().addDocumentListener(new DocumentListener() {
//			@Override
//			public void removeUpdate(DocumentEvent paramDocumentEvent) {
//				updateFilter(filterSelection.getText());
//			}
//
//			@Override
//			public void insertUpdate(DocumentEvent paramDocumentEvent) {
//				updateFilter(filterSelection.getText());
//			}
//
//			@Override
//			public void changedUpdate(DocumentEvent paramDocumentEvent) {
//				updateFilter(filterSelection.getText());
//			}
//		});
//
//		builder.add("Filter parts", filterSelection);
//	}
//
//	@Override
//	protected JPanel initMainPanel() {
//		List<ComponentDefinition> components = new ArrayList<ComponentDefinition>();
//		if (onlyShowRootCDs.isSelected()) {
//			components.addAll(doc.getRootComponentDefinitions());
//		} else {
//			components.addAll(doc.getComponentDefinitions());
//		}
//
//		ComponentDefinitionTableModel tableModel = new ComponentDefinitionTableModel(components);
//		JPanel panel = createTablePanel(tableModel, "Matching parts (" + tableModel.getRowCount() + ")");
//		table = (JTable) panel.getClientProperty("table");
//		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		tableLabel = (JLabel) panel.getClientProperty("label");
//
//		updateTable();
//
//		return panel;
//	}
//
//	@Override
//	protected SBOLDocument getSelection() {
//		try {
//			int row = table.convertRowIndexToModel(table.getSelectedRow());
//			ComponentDefinition comp = ((ComponentDefinitionTableModel) table.getModel()).getElement(row);
//			return doc.createRecursiveCopy(comp);
//		} catch (SBOLValidationException e) {
//			JOptionPane.showMessageDialog(null, "This ComponentDefinition cannot be imported: " + e.getMessage());
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	private void updateRoleRefinement() {
//		roleRefinement.removeAllItems();
//		for (String s : SBOLUtils.createRefinements((Part) roleSelection.getSelectedItem())) {
//			roleRefinement.addItem(s);
//		}
//	}
//
//	private void updateTable() {
//		Part part;
//		String roleName = (String) roleRefinement.getSelectedItem();
//		if (roleName == null || roleName.equals("None")) {
//			part = isRoleSelection() ? (Part) roleSelection.getSelectedItem() : ALL_PARTS;
//		} else {
//			SequenceOntology so = new SequenceOntology();
//			URI role = so.getURIbyName(roleName);
//			part = new Part(role, null, null);
//		}
//
//		Set<ComponentDefinition> CDsToDisplay;
//		if (onlyShowRootCDs.isSelected()) {
//			CDsToDisplay = doc.getRootComponentDefinitions();
//		} else {
//			CDsToDisplay = doc.getComponentDefinitions();
//		}
//
//		List<ComponentDefinition> components = SBOLUtils.getCDOfRole(CDsToDisplay, part);
//		components = SBOLUtils.getCDOfType(components, (Types) typeSelection.getSelectedItem());
//		((ComponentDefinitionTableModel) table.getModel()).setElements(components);
//		tableLabel.setText("Matching parts (" + components.size() + ")");
//	}
//
//	private boolean isRoleSelection() {
//		return roleSelection != null;
//	}
//
//	private void updateFilter(String filterText) {
//		filterText = "(?i)" + filterText;
//		@SuppressWarnings({ "rawtypes", "unchecked" })
//		TableRowSorter<ComponentDefinitionTableModel> sorter = (TableRowSorter) table.getRowSorter();
//		if (filterText.length() == 0) {
//			sorter.setRowFilter(null);
//		} else {
//			try {
//				RowFilter<ComponentDefinitionTableModel, Object> rf = RowFilter.regexFilter(filterText, 0, 1);
//				sorter.setRowFilter(rf);
//			} catch (java.util.regex.PatternSyntaxException e) {
//				sorter.setRowFilter(null);
//			}
//		}
//
//		tableLabel.setText("Matching parts (" + sorter.getViewRowCount() + ")");
//	}
//}
