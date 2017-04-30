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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.common.base.Strings;

import edu.utah.ece.async.ibiosim.dataModels.util.IBioSimPreferences;
import edu.utah.ece.async.ibiosim.dataModels.util.Infos;
import edu.utah.ece.async.ibiosim.dataModels.util.PersonInfo;
import edu.utah.ece.async.ibiosim.gui.ResourceManager;
import edu.utah.ece.async.ibiosim.gui.util.preferences.PreferencesDialog.PreferencesTab;
import edu.utah.ece.async.sboldesigner.sbol.editor.Images;
import edu.utah.ece.async.sboldesigner.swing.FormBuilder;

public enum UserInfoTab implements PreferencesTab {
	INSTANCE;

	private JTextField name;
	private JTextField email;
	private JTextField uri;

	@Override
	public String getTitle() {
		return "User";
	}

	@Override
	public String getDescription() {
		return "User information added to designs";
	}

	@Override
	public Icon getIcon() {
		return ResourceManager.getImageIcon("user.png");
	}

	@Override
	public Component getComponent() {
		PersonInfo info = IBioSimPreferences.INSTANCE.getUserInfo();
		FormBuilder builder = new FormBuilder();
		name = builder.addTextField("Full name", info == null ? null : info.getName());
		email = builder.addTextField("Email",
				info == null || info.getEmail() == null ? null : info.getEmail().toString());
		uri = builder.addTextField("Namespace [required]", info == null ? null : info.getURI().toString());
		JPanel formPanel = builder.build();

		JButton deleteInfo = new JButton("Delete user info");
		deleteInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PersonInfo userInfo = Infos.forPerson(uri.getText());
				IBioSimPreferences.INSTANCE.saveUserInfo(userInfo);
				name.setText(null);
				email.setText(null);
			}
		});
		deleteInfo.setAlignmentX(Component.RIGHT_ALIGNMENT);
		deleteInfo.setEnabled(info != null);

		Box buttonPanel = Box.createHorizontalBox();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(deleteInfo);

		JPanel p = new JPanel(new BorderLayout());
		p.add(formPanel, BorderLayout.NORTH);
		p.add(buttonPanel, BorderLayout.SOUTH);

		return p;
	}

	@Override
	public void save() {
		boolean noURI = Strings.isNullOrEmpty(uri.getText());
		boolean noName = Strings.isNullOrEmpty(name.getText());
		boolean noEmail = Strings.isNullOrEmpty(email.getText());
		if (!(noURI && noName && noEmail)) {
			URI personURI = URI.create(uri.getText());
			String personName = noName ? "" : name.getText();
			String personEmail = noEmail ? null : email.getText();
			PersonInfo info = Infos.forPerson(personURI, personName, personEmail);
			IBioSimPreferences.INSTANCE.saveUserInfo(info);
		}
	}

	@Override
	public boolean requiresRestart() {
		return false;
	}
}