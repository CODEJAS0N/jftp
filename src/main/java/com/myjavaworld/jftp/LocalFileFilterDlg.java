/*
 * Copyright 2012 jMethods, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.myjavaworld.jftp;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import com.myjavaworld.gui.GUIUtil;
import com.myjavaworld.gui.MButton;
import com.myjavaworld.gui.MCheckBox;
import com.myjavaworld.gui.MDialog;
import com.myjavaworld.gui.MLabel;
import com.myjavaworld.gui.MRadioButton;
import com.myjavaworld.gui.MTextField;
import com.myjavaworld.util.CommonResources;
import com.myjavaworld.util.DateFilter;
import com.myjavaworld.util.Filter;
import com.myjavaworld.util.RegexFilter;
import com.myjavaworld.util.ResourceLoader;

/**
 * @author Sai Pullabhotla, psai [at] jMethods [dot] com
 * @version 1.0
 */
public class LocalFileFilterDlg extends MDialog implements ActionListener {

	private static ResourceBundle resources = ResourceLoader
			.getBundle("com.myjavaworld.jftp.LocalFileFilterDlg");
	private static final String HELP_ID = "local.filter";
	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd");
	private MRadioButton radioInclusionFilter = null;
	private MRadioButton radioExclusionFilter = null;
	private MTextField tfRegexFilter = null;
	private MCheckBox cbCaseSensitive = null;
	private MTextField tfStartDate = null;
	private MTextField tfEndDate = null;
	private MCheckBox cbShowHiddenFiles = null;
	private MButton butApply = null;
	private MButton butCancel = null;
	private MButton butHelp = null;
	private boolean approved = false;

	/**
	 * Creates an instance of <code>LocalFileFilterDlg</code>.
	 * 
	 * @param parent
	 */
	public LocalFileFilterDlg(Frame parent) {
		super(parent);
		setTitle(resources.getString("title.dialog"));
		setModal(true);
		getContentPane().setLayout(new GridBagLayout());
		JFTPHelp2.getInstance().enableHelpKey(getRootPane(), HELP_ID);
		initComponents();
		pack();
	}

	/**
	 * @return
	 */
	public boolean isApproved() {
		return approved;
	}

	/**
	 * @param filter
	 */
	public void setFilter(Filter filter) {
		if (filter != null) {
			LocalFileFilter lff = (LocalFileFilter) filter;
			radioExclusionFilter.setSelected(lff.isExclusionFilter());
			RegexFilter regexFilter = lff.getRegexFilter();
			DateFilter dateFilter = lff.getDateFilter();
			if (regexFilter != null) {
				tfRegexFilter.setText(regexFilter.getPattern().pattern());
				cbCaseSensitive
						.setSelected((regexFilter.getPattern().flags() & Pattern.CASE_INSENSITIVE) == 0);
			}
			if (dateFilter != null) {
				Date startDate = dateFilter.getStartDate();
				Date endDate = dateFilter.getEndDate();
				if (startDate != null) {
					tfStartDate.setText(SDF.format(startDate));
				}
				if (endDate != null) {
					tfEndDate.setText(SDF.format(endDate));
				}
			}
			cbShowHiddenFiles.setSelected(lff.isShowHiddenFiles());
		} else {
			cbShowHiddenFiles.setSelected(true);
		}
	}

	/**
	 * @return
	 */
	public Filter getFilter() {
		if (!approved) {
			return null;
		}
		try {
			return new LocalFileFilter(getRegexFilter(), getDateFilter(),
					cbShowHiddenFiles.isSelected(),
					radioExclusionFilter.isSelected());
		} catch (Exception exp) {
			return null;
		}

	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == butCancel) {
			setVisible(false);
		} else if (evt.getSource() == butApply) {
			if (!validateInput()) {
				return;
			}
			approved = true;
			setVisible(false);
		}
	}

	@Override
	public void windowOpened(WindowEvent evt) {
		tfRegexFilter.requestFocusInWindow();
	}

	@Override
	protected void escape() {
		butCancel.doClick();
	}

	private boolean validateInput() {
		String error = null;
		Component errorComponent = null;
		try {
			getRegexFilter();
			getDateFilter();
		} catch (PatternSyntaxException exp) {
			MessageFormat mf = new MessageFormat(
					resources.getString("error.pattern.invalid"));
			error = mf.format(new Object[] { exp.getMessage() });
			errorComponent = tfRegexFilter;
		} catch (ParseException exp) {
			MessageFormat mf = new MessageFormat(
					resources.getString("error.date.invalid"));
			error = mf.format(new Object[] { exp.getMessage() });
			errorComponent = tfStartDate;
		}
		if (error != null) {
			GUIUtil.showError(this, error);
			if (errorComponent != null) {
				errorComponent.requestFocusInWindow();
			}
			return false;
		}
		return true;
	}

	private RegexFilter getRegexFilter() {
		String strPattern = tfRegexFilter.getText().trim();
		if (strPattern.length() == 0) {
			return null;
		}
		return new RegexFilter(strPattern, cbCaseSensitive.isSelected() ? 0
				: Pattern.CASE_INSENSITIVE);
	}

	private DateFilter getDateFilter() throws ParseException {
		String strStartDate = tfStartDate.getText().trim();
		String strEndDate = tfEndDate.getText().trim();
		if (strStartDate.length() == 0 && strEndDate.length() == 0) {
			return null;
		}
		Date startDate = null;
		Date endDate = null;
		if (strStartDate.length() > 0) {
			startDate = SDF.parse(strStartDate);
		}
		if (strEndDate.length() > 0) {
			endDate = SDF.parse(strEndDate);
		}
		return new DateFilter(startDate, endDate);
	}

	private void initComponents() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(12, 12, 12, 12);
		getContentPane().add(getFilterTypePanel(), c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 12, 12, 12);
		getContentPane().add(getFileNameFilterPanel(), c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 12, 12, 12);
		getContentPane().add(getDateFilterPanel(), c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 12, 12, 12);
		getContentPane().add(getAttributesFilterPanel(), c);

		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.weightx = 0.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(6, 12, 12, 12);
		c.anchor = GridBagConstraints.SOUTHEAST;
		getContentPane().add(getCommandButtons(), c);
	}

	private Component getFilterTypePanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(resources
				.getString("title.filterType")));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		radioInclusionFilter = new MRadioButton(
				resources.getString("text.inclusionFilter"), true);
		radioExclusionFilter = new MRadioButton(
				resources.getString("text.exclusionFilter"), false);
		ButtonGroup bg = new ButtonGroup();
		bg.add(radioInclusionFilter);
		bg.add(radioExclusionFilter);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(6, 6, 6, 6);
		panel.add(radioInclusionFilter, c);

		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(6, 0, 6, 6);
		panel.add(radioExclusionFilter, c);

		return panel;
	}

	private Component getFileNameFilterPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(resources
				.getString("title.nameFilter")));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		MLabel labFileNameFilter = new MLabel(
				resources.getString("text.regexFilter"));
		tfRegexFilter = new MTextField(30);
		cbCaseSensitive = new MCheckBox(
				resources.getString("text.caseSensitive"), false);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 6, 6, 6);
		panel.add(labFileNameFilter, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 6, 6, 6);
		panel.add(tfRegexFilter, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(0, 6, 6, 6);
		panel.add(cbCaseSensitive, c);

		return panel;
	}

	private Component getDateFilterPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder(resources
				.getString("title.dateFilter")));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		MessageFormat mf = new MessageFormat(
				resources.getString("text.dateFilter"));
		MLabel labDateFilter = new MLabel(
				mf.format(new Object[] { "yyyy-mm-dd" }));
		MLabel labStartDate = new MLabel(resources.getString("text.start"));
		MLabel labEndDate = new MLabel(resources.getString("text.end"));
		tfStartDate = new MTextField(10);
		tfEndDate = new MTextField(10);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(6, 6, 6, 6);
		panel.add(labDateFilter, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 6, 6, 6);
		panel.add(labStartDate, c);

		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 6, 6);
		panel.add(tfStartDate, c);

		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 6, 6);
		panel.add(labEndDate, c);

		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 0, 6, 6);
		panel.add(tfEndDate, c);

		return panel;

	}

	private Component getAttributesFilterPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(resources
				.getString("title.attributesFilter")));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		cbShowHiddenFiles = new MCheckBox(
				resources.getString("text.showHiddenFiles"));

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(6, 6, 6, 6);
		panel.add(cbShowHiddenFiles, c);

		return panel;
	}

	private Component getCommandButtons() {
		Box panel = new Box(BoxLayout.X_AXIS);
		butApply = new MButton(CommonResources.getString("text.apply"));
		butApply.addActionListener(this);
		getRootPane().setDefaultButton(butApply);
		butCancel = new MButton(CommonResources.getString("text.cancel"));
		butCancel.addActionListener(this);
		butHelp = new MButton(CommonResources.getString("text.help"));
		JFTPHelp2.getInstance().enableHelp(butHelp, HELP_ID);

		panel.add(butApply);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(butCancel);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(butHelp);

		return panel;
	}
}