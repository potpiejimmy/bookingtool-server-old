package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.wincor.bcon.bookingtool.server.ejb.ExcelExportEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;


@Named
@SessionScoped
public class ExcelExportBean implements Serializable {
		
	private static final long serialVersionUID = 1L;

	@EJB
	private ExcelExportEJBLocal myExcelExportEJB; 
	
	private Integer weeksToExport = 1;
	
        public String getCurrentUserName() {
            return WebUtils.getCurrentPerson();
        }
        
	public StreamedContent getExcelList () {
		
		HSSFWorkbook wb = myExcelExportEJB.getExcelForName(WebUtils.getCurrentPerson(), getWeeksToExport());
		return streamForWorkbook(wb, "buchungen_"+WebUtils.getCurrentPerson());
	}
	
	public StreamedContent getExcelListAdmin () {
		
		HSSFWorkbook wb = myExcelExportEJB.getExcelForAdmin();
		return streamForWorkbook(wb, "buchungen_all");
	}
	
	public StreamedContent getExcelListForBudget(Integer budgetId) {
		
		HSSFWorkbook wb = myExcelExportEJB.getExcelForBudget(budgetId);
		return streamForWorkbook(wb, "buchungen_budget_"+budgetId);
	}
	
	public Integer getWeeksToExport() {
		return weeksToExport;
	}

	public void setWeeksToExport(Integer weeksToExport) {
		this.weeksToExport = weeksToExport;
	}

	public static DefaultStreamedContent streamForWorkbook(HSSFWorkbook wb, String filename) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] content = baos.toByteArray();

		return new DefaultStreamedContent(new ByteArrayInputStream(content), "application/excel", filename+".xls");
	}

	
}
