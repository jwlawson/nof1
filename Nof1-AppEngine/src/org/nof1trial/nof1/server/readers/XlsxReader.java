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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * @author John
 * 
 */
public class XlsxReader extends Reader {

	private InputStream stream;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private XSSFRow row;

	private Iterator<Row> rowIter;

	@Override
	public void setInputStream(InputStream stream) {
		this.stream = stream;
		try {
			workbook = new XSSFWorkbook(stream);
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
		XSSFCell cell = row.getCell(0);
		if (cell == null) {
			return "";
		}
		String result;

		switch (cell.getCellType()) {
		case XSSFCell.CELL_TYPE_NUMERIC:
			result = String.valueOf(cell.getNumericCellValue());
			break;
		case XSSFCell.CELL_TYPE_STRING:
			result = cell.getStringCellValue();
			break;
		case XSSFCell.CELL_TYPE_BLANK:
		default:
			result = "";
			break;
		}
		return result;
	}

	@Override
	public String getMin() {
		XSSFCell cell = row.getCell(2);
		String result;
		if (cell == null) {
			return "";
		}

		switch (cell.getCellType()) {
		case XSSFCell.CELL_TYPE_NUMERIC:
			result = String.valueOf(cell.getNumericCellValue());
			break;
		case XSSFCell.CELL_TYPE_STRING:
			result = cell.getStringCellValue();
			break;
		case XSSFCell.CELL_TYPE_BLANK:
		default:
			result = "";
			break;
		}
		return result;
	}

	@Override
	public String getMax() {
		XSSFCell cell = row.getCell(3);
		if (cell == null) {
			return "";
		}

		String result;

		switch (cell.getCellType()) {
		case XSSFCell.CELL_TYPE_NUMERIC:
			result = String.valueOf(cell.getNumericCellValue());
			break;
		case XSSFCell.CELL_TYPE_STRING:
			result = cell.getStringCellValue();
			break;
		case XSSFCell.CELL_TYPE_BLANK:
		default:
			result = "";
			break;
		}
		return result;
	}

	@Override
	public int getType() {
		XSSFCell cell = row.getCell(1);
		if (cell == null) {
			return 0;
		}
		int result;

		switch (cell.getCellType()) {
		case XSSFCell.CELL_TYPE_NUMERIC:
			result = (int) cell.getNumericCellValue();
			break;
		case XSSFCell.CELL_TYPE_STRING:
			result = Integer.parseInt(cell.getStringCellValue());
			break;
		case XSSFCell.CELL_TYPE_BLANK:
		default:
			result = 0;
			break;
		}
		return result;
	}

	@Override
	public boolean hasNext() {
		return rowIter.hasNext();
	}

	@Override
	public void moveToNext() {
		row = (XSSFRow) rowIter.next();
	}

	@Override
	public void closeStream() {
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
