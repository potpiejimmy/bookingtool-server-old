package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import com.wincor.bcon.bookingtool.server.db.entity.ResourceTeam;
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
import com.wincor.bcon.bookingtool.server.ejb.ResourceTeamsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.ResourcesEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.List;
import javax.faces.model.SelectItem;


@Named
@SessionScoped
public class ExcelExportBean implements Serializable {
		
	private static final long serialVersionUID = 1L;

	@EJB
	private ExcelExportEJBLocal myExcelExportEJB; 
        
        @EJB
        private ResourceTeamsEJBLocal resourceTeamsEJB;
	
        @EJB
        private ResourcesEJBLocal resourcesEJB;
	
	private Integer weeksToExport = 1;
	private Integer monthsToExport = 0;
	private Integer weeksToExportResPlan = 1;
        private Integer teamToExport = 0;
	
        public String getCurrentUserName() {
            return WebUtils.getCurrentPerson();
        }
        
	public StreamedContent getExcelList () {
		
		HSSFWorkbook wb = myExcelExportEJB.getExcelForName(WebUtils.getCurrentPerson(), getWeeksToExport());
		return streamForWorkbook(wb, "buchungen_"+WebUtils.getCurrentPerson());
	}
	
	public StreamedContent getExcelListAdmin () {
		
		HSSFWorkbook wb = myExcelExportEJB.getExcelForAdmin(getMonthsToExport());
		return streamForWorkbook(wb, "buchungen_all");
	}
	
	public StreamedContent getExcelListForBudget(Integer budgetId) {
		
		HSSFWorkbook wb = myExcelExportEJB.getExcelForBudget(budgetId);
		return streamForWorkbook(wb, "buchungen_budget_"+budgetId);
	}
        
        public StreamedContent getResourcePlan() {
		HSSFWorkbook wb = resourcesEJB.exportResourcePlan(teamToExport, weeksToExportResPlan);
		return streamForWorkbook(wb, "resource_plan_team"+teamToExport);
        }
	
	public Integer getWeeksToExport() {
		return weeksToExport;
	}

	public void setWeeksToExport(Integer weeksToExport) {
		this.weeksToExport = weeksToExport;
	}
	
	public Integer getMonthsToExport() {
		return monthsToExport;
	}

	public void setMonthsToExport(Integer monthsToExport) {
		this.monthsToExport = monthsToExport;
	}

        public Integer getWeeksToExportResPlan() {
            return weeksToExportResPlan;
        }

        public void setWeeksToExportResPlan(Integer weeksToExportResPlan) {
            this.weeksToExportResPlan = weeksToExportResPlan;
        }

        public Integer getTeamToExport() {
            return teamToExport;
        }

        public void setTeamToExport(Integer teamToExport) {
            this.teamToExport = teamToExport;
        }

        public List<SelectItem> getManagedResourceTeamItems() {
		List<ResourceTeam> teams = resourceTeamsEJB.getManagedResourceTeams();
		List<SelectItem> result = new ArrayList<SelectItem>(teams.size());
		for (ResourceTeam t : teams) {
			result.add(new SelectItem(t.getId(), t.getName()));
		}
		return result;
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
