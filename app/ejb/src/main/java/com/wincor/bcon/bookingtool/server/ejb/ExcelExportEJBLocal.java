package com.wincor.bcon.bookingtool.server.ejb;

import javax.ejb.Local;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

@Local
public interface ExcelExportEJBLocal {

	public HSSFWorkbook getExcelForName(String person, Integer weeksToExport);

	public HSSFWorkbook getExcelForAdmin();

	public HSSFWorkbook getExcelForBudget(int budgetId);

}
