/*
 * Copyright (c) 2012 - 2017, Clark & Parsia, LLC. <http://www.clarkparsia.com>
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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.base.Objects;

import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.sbol.editor.dialog.SettingsTab;

/**
 * 
 * @author Evren Sirin
 */
public class PreferencesDialog extends JDialog implements ActionListener {
	private static final String TITLE = "Preferences";

	private static final PreferencesTab[] TABS = { edu.utah.ece.async.sboldesigner.sbol.editor.dialog.UserInfoTab.INSTANCE, edu.utah.ece.async.sboldesigner.sbol.editor.dialog.RegistryPreferencesTab.INSTANCE,
			GeneralPreferencesTab.INSTANCE, SettingsTab.INSTANCE, ModelEditorPreferencesTab.INSTANCE, 
			ModelPreferencesTab.INSTANCE, AnalysisPreferencesTab.INSTANCE, LearnPreferencesTab.INSTANCE, SynthesisPreferencesTab.INSTANCE  
			/**
			 * SOMappingTab.INSTANCE,
			 * VersioningPreferencesTab.INSTANCE
			 **/
	};

	public static void showPreferences(Component parent) {
		showPreferences(parent, null);
	}

	public static void showPreferences(Component parent, String selectTab) {
		PreferencesDialog dialog = new PreferencesDialog(parent, selectTab);
		dialog.setVisible(true);
	}

	private PreferencesDialog(Component parent, String selectTab) {
		super(JOptionPane.getFrameForComponent(parent), TITLE, true);

		final JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		getRootPane().setDefaultButton(closeButton);

		// Create the panel that contains the "cards".
		final JPanel cards = new JPanel(new CardLayout());
		DefaultListModel<PreferencesTab> listModel = new DefaultListModel<PreferencesTab>();
		int selectedIndex = 0;
		for (PreferencesTab tab : TABS) {
			cards.add(new JScrollPane(tab.getComponent()), tab.getTitle());
			if (Objects.equal(tab.getTitle(), selectTab)) {
				selectedIndex = listModel.size();
			}
			listModel.addElement(tab);
		}

		final JList<PreferencesTab> list = new JList<PreferencesTab>(listModel);
		list.setFocusable(false);
		list.setPreferredSize(new Dimension(100, 100));
		list.setCellRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList<?> list, // the
																			// list
					Object value, // value to display
					int index, // cell index
					boolean isSelected, // is the cell selected
					boolean cellHasFocus) // does the cell have focus
			{
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				PreferencesTab tab = (PreferencesTab) value;
				setText(tab.getTitle());
				setIcon(tab.getIcon());
				setToolTipText(tab.getDescription());
				return this;
			}
		});

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					CardLayout layout = (CardLayout) cards.getLayout();
					layout.show(cards, ((PreferencesTab) list.getSelectedValue()).getTitle());
				}
			}
		});
		list.setSelectedIndex(selectedIndex);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(closeButton);

		Container contentPane = getContentPane();
		contentPane.add(new JScrollPane(list), BorderLayout.WEST);
		contentPane.add(cards, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.SOUTH);

		setSize(720, 450);
		setLocationRelativeTo(getOwner());
	}

	public void actionPerformed(ActionEvent e) {
		boolean restart = false;
		for (PreferencesTab tab : TABS) {
			restart = tab.requiresRestart();
			tab.save();
		}

		if (restart) {
			JOptionPane.showMessageDialog(this, "Your changes will take effect next time the program is started");
		}

		setVisible(false);
	}

//	interface PreferencesTab {
//		String getTitle();
//
//		String getDescription();
//
//		Icon getIcon();
//
//		Component getComponent();
//
//		void save();
//
//		boolean requiresRestart();
//	}
}
