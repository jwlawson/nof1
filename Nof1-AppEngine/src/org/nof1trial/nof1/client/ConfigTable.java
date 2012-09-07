/*******************************************************************************
 * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
 * Copyright (C) 2012  John Lawson
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You may obtain a copy of the GNU General Public License at 
 * <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     John Lawson - initial API and implementation
 ******************************************************************************/
package org.nof1trial.nof1.client;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author John Lawson
 * 
 */
public class ConfigTable extends FlexTable {

	private PatientData mData;

	public ConfigTable() {
		this.setCellPadding(1);
		this.setCellSpacing(0);
		this.setWidth("100%");
	}

	public void setPatientData(PatientData data) {
		this.mData = data;

		refreshView();

	}

	public void refreshView() {
		// Remove all rows
		for (int i = this.getRowCount(); i > 0; i--) {
			this.removeRow(0);
		}

		List<String> headers = mData.getConfigHeaders();
		List<String> data = mData.getConfigData();

		for (int i = 0; i < headers.size(); i++) {
			setText(i, 0, headers.get(i));
			setText(i, 1, data.get(i));
		}

		getColumnFormatter().addStyleName(0, "tableHeader");
	}

}
