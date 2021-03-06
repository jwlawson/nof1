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

import java.util.Date;
import java.util.List;

import org.nof1trial.nof1.shared.DataProxy;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * @author John Lawson
 * 
 */
public class DataTable extends FlexTable {

	private PatientData mData;

	public DataTable() {

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

		int row = 0;
		List<String> headers = mData.getDataHeaders();
		if (headers != null) {
			int i = 0;
			for (String string : headers) {
				this.setText(row, i, string);
				i++;
			}
			row++;
		}
		// Make the table header look nicer
		this.getRowFormatter().addStyleName(0, "table-Header");

		DateTimeFormat df = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM);

		List<DataProxy> rows = mData.getData();
		int i = 1;
		for (DataProxy proxy : rows) {

			this.setText(i, 0, df.format(new Date(proxy.getTime())));
			this.setText(i, 1, String.valueOf(proxy.getDay()));

			int j = 2;

			List<Integer> ans = proxy.getQuestionData();
			for (int ques : ans) {
				this.setText(i, j, String.valueOf(ques));
				j++;
			}
			// Always want comment in the last column
			this.setText(i, getCellCount(0) - 1, proxy.getComment());

			i++;
		}

		applyStyles();
	}

	private void applyStyles() {
		RowFormatter rf = getRowFormatter();
		CellFormatter cf = getCellFormatter();

		for (int row = 0; row < getRowCount(); ++row) {
			for (int col = 1; col < getCellCount(row); col++) {
				cf.addStyleName(row, col, "table-Cell");
			}
			if (row == 0) continue;
			if ((row % 2) != 0) {
				rf.addStyleName(row, "table-OddRow");
			} else {
				rf.addStyleName(row, "table-EvenRow");
			}

		}
	}

}
