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
package org.nof1trial.nof1.server.readers;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * @author John
 * 
 */
public class XlsReader extends Reader {

	private HSSFWorkbook workbook;
	private HSSFSheet sheet;
	private HSSFRow row;

	private Iterator<Row> rowIter;

	@Override
	public void setInputStream(InputStream stream) {
		try {
			workbook = new HSSFWorkbook(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		sheet = workbook.getSheetAt(0);
		rowIter = sheet.rowIterator();

		moveToNext();

	}

	@Override
	public String getQuestion() {
		return row.getCell(0).getStringCellValue();
	}

	@Override
	public String getMin() {
		return row.getCell(2).getStringCellValue();
	}

	@Override
	public String getMax() {
		return row.getCell(3).getStringCellValue();
	}

	@Override
	public int getType() {
		return (int) row.getCell(1).getNumericCellValue();
	}

	@Override
	public boolean hasNext() {
		return rowIter.hasNext();
	}

	@Override
	public void moveToNext() {
		row = (HSSFRow) rowIter.next();
	}

}
