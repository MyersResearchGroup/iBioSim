/*
 * Copyright (c) 2012 - 2015, Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.utah.ece.async.ibiosim.gui.util.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.synbiohub.frontend.SynBioHubFrontend;

import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.ibiosim.gui.util.preferences.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.sbol.editor.Images;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registries;
import edu.utah.ece.async.sboldesigner.sbol.editor.Registry;
import edu.utah.ece.async.sboldesigner.sbol.editor.SynBioHubFrontends;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.OldInputDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryAddDialog;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryLoginDialog;

public enum RegistryPreferencesTab implements PreferencesTab {
	INSTANCE;

	@Override
	public String getTitle() {
		return "Registries";
	}

	@Override
	public String getDescription() {
		return "Configuration options for part registries";
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(Images.getActionImage("registry.png"));
	}

	@Override
	public Component getComponent() {
		final RegistryTableModel tableModel = new RegistryTableModel();
		final JTable table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RegistryTableModel model = (RegistryTableModel) table.getModel();
				Action action = Action.valueOf(e.getActionCommand());
				switch (action) {
				case ADD:
					Registry registry = new RegistryAddDialog(table, null).getInput();
					if (registry != null) {
						model.add(registry);
						Registries.get().save();
					}
					break;
				case REMOVE:
					int row = table.convertRowIndexToModel(table.getSelectedRow());
					model.remove(row);
					Registries.get().save();
					break;
				case LOGIN:
					row = table.convertRowIndexToModel(table.getSelectedRow());
					Registry r = model.getComponent(row);

					if (r.isPath()) {
						JOptionPane.showMessageDialog(getComponent(),
								"It appears this registry is not a SynBioHub instance.  SynBioHub urls start with https://");
						break;
					}

					RegistryLoginDialog loginDialog = new RegistryLoginDialog(getComponent(), r.getLocation(),
							r.getUriPrefix());
					SynBioHubFrontend frontend = loginDialog.getSynBioHubFrontend();
					if (frontend == null) {
						break;
					}

					SynBioHubFrontends frontends = new SynBioHubFrontends();
					if (frontends.hasFrontend(r.getLocation())) {
						frontends.removeFrontend(r.getLocation());
					}
					frontends.addFrontend(r.getLocation(), frontend);
					break;
				case RESTORE:
					model.restoreDefaults();
					Registries.get().save();
					break;
				case EDIT:
					row = table.convertRowIndexToModel(table.getSelectedRow());
					if (row > model.getRowCount()) {
						return;
					} else {
						Registry oldRegistry = model.getComponent(row);
						registry = new RegistryAddDialog(table, oldRegistry).getInput();
						if (registry != null) {
							model.remove(row);
							model.add(registry);
							Registries.get().save();
						}
					}
					break;
				}
			}
		};

		final JButton addButton = new JButton("Add");
		addButton.setActionCommand(Action.ADD.toString());
		addButton.addActionListener(listener);

		final JButton editButton = new JButton("Edit");
		editButton.setActionCommand(Action.EDIT.toString());
		editButton.addActionListener(listener);
		editButton.setEnabled(false);

		final JButton removeButton = new JButton("Remove");
		removeButton.setActionCommand(Action.REMOVE.toString());
		removeButton.addActionListener(listener);
		removeButton.setEnabled(false);

		final JButton loginButton = new JButton("Login");
		loginButton.setActionCommand(Action.LOGIN.toString());
		loginButton.addActionListener(listener);
		loginButton.setEnabled(true);

		final JButton restoreButton = new JButton("Restore defaults");
		restoreButton.setActionCommand(Action.RESTORE.toString());
		restoreButton.addActionListener(listener);
		restoreButton.setEnabled(true);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				// Everything can be removed/edited except Built-In parts.
				removeButton.setEnabled(table.getSelectedRow() >= 2);
				editButton.setEnabled(table.getSelectedRow() >= 2);
			}
		});

		//OldInputDialog.setWidthAsPercentages(table, 0.2, 0.2, 0.6);

		TableRowSorter<RegistryTableModel> sorter = new TableRowSorter<RegistryTableModel>(tableModel);
		table.setRowSorter(sorter);

		JScrollPane tableScroller = new JScrollPane(table);
		tableScroller.setPreferredSize(new Dimension(450, 200));
		tableScroller.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel tablePane = new JPanel();
		tablePane.setLayout(new BoxLayout(tablePane, BoxLayout.PAGE_AXIS));
		JLabel label = new JLabel("Registry list");
		label.setLabelFor(table);
		tablePane.add(label);
		tablePane.add(Box.createRigidArea(new Dimension(0, 5)));
		tablePane.add(tableScroller);
		tablePane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(addButton);
		buttonPane.add(editButton);
		buttonPane.add(removeButton);
		buttonPane.add(loginButton);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(restoreButton);

		Container contentPane = new JPanel(new BorderLayout());
		contentPane.add(tablePane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);
		return contentPane;
	}

	@Override
	public void save() {
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}

	private static enum Action {
		ADD, REMOVE, LOGIN, RESTORE, EDIT
	}

	private static class RegistryTableModel extends AbstractTableModel {
		private static final String[] COLUMNS = { "Name", "URL/Path", "Description" };
		private Registries registries;

		public RegistryTableModel() {
			this.registries = Registries.get();
		}

		public void restoreDefaults() {
			registries.restoreDefaults();
			fireTableDataChanged();
		}

		public void add(Registry registry) {
			registries.add(registry);
			fireTableDataChanged();
		}

		public void remove(int row) {
			registries.remove(row);
			fireTableDataChanged();
		}

		public int getColumnCount() {
			return COLUMNS.length;
		}

		public int getRowCount() {
			return registries.size();
		}

		public String getColumnName(int col) {
			return COLUMNS[col];
		}

		public Registry getComponent(int row) {
			return registries.get(row);
		}

		public Object getValueAt(int row, int col) {
			Registry registry = getComponent(row);
			switch (col) {
			case 0:
				return registry.getName();
			case 1:
				return registry.getLocation();
			case 2:
				return registry.getDescription();
			default:
				throw new IndexOutOfBoundsException();
			}
		}

		public Class<?> getColumnClass(int col) {
			return Object.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}