package com.wincor.bcon.bookingtool.server.ejb;

import javax.ejb.Local;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Local
public interface ExcelExportEJBLocal {

	public XSSFWorkbook getExcelForName(String person, Integer weeksToExport);

	public XSSFWorkbook getExcelForAdmin(Integer monthsToExport);

	public XSSFWorkbook getExcelForBudget(int budgetId);
}
