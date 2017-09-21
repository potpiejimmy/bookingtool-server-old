package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.db.entity.ResourceTeam;
import com.wincor.bcon.bookingtool.server.ejb.ExcelExportEJB;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJB;
import com.wincor.bcon.bookingtool.server.ejb.ResourceTeamsEJB;
import com.wincor.bcon.bookingtool.server.ejb.ResourcesEJB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.faces.model.SelectItem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Named
@SessionScoped
public class ExcelExportBean implements Serializable {
		
	private static final long serialVersionUID = 1L;

	@EJB
	private ExcelExportEJB myExcelExportEJB; 
        
        @EJB
        private ResourceTeamsEJB resourceTeamsEJB;
	
        @EJB
        private ResourcesEJB resourcesEJB;
	
        @EJB
        private ProjectsEJB projectsEjb;
    
	private Integer weeksToExport = 1;
	private Integer kwToExport = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
	private Integer kwYearToExport = Calendar.getInstance().get(Calendar.YEAR);
	private Integer monthsToExport = 0;
	private Integer weeksToExportResPlan = 1;
        private Integer teamToExport = 0;
        private Integer projectToExport = -1;
	
        public String getCurrentUserName() {
            return WebUtils.getCurrentPerson();
        }
        
	public StreamedContent getExcelList () {
		
                XSSFWorkbook wb = myExcelExportEJB.getExcelForName(WebUtils.getCurrentPerson(), getWeeksToExport());
		return streamForWorkbook(wb, "buchungen_"+WebUtils.getCurrentPerson());
	}
	
	public StreamedContent getExcelPpm () {
		
                XSSFWorkbook wb = myExcelExportEJB.getExcelForNamePpm(WebUtils.getCurrentPerson(), getKwToExport(), getKwYearToExport());
		return streamForWorkbook(wb, "buchungen_ppm_"+getKwYearToExport()+"_"+getKwToExport()+"_"+WebUtils.getCurrentPerson());
	}
	
	public StreamedContent getExcelListProject () {
		
		XSSFWorkbook wb = myExcelExportEJB.getExcelForProject(projectToExport, getMonthsToExport());
		return streamForWorkbook(wb, "buchungen_project_"+projectToExport);
	}
	
	public StreamedContent getExcelListAdmin () {
		
		XSSFWorkbook wb = myExcelExportEJB.getExcelForAdmin(getMonthsToExport());
		return streamForWorkbook(wb, "buchungen_all");
	}
	
	public StreamedContent getExcelListForBudget(Integer budgetId) {
		
		XSSFWorkbook wb = myExcelExportEJB.getExcelForBudget(budgetId);
		return streamForWorkbook(wb, "buchungen_budget_"+budgetId);
	}
        
	public StreamedContent getExcelListForGrindstone() {
		
		XSSFWorkbook wb = myExcelExportEJB.getExcelForGrindstone();
		return streamForWorkbook(wb, "grindstone_tasks");
	}
        
        public StreamedContent getResourcePlan() {
		XSSFWorkbook wb = resourcesEJB.exportResourcePlan(teamToExport, weeksToExportResPlan);
		return streamForWorkbook(wb, "resource_plan_team"+teamToExport);
        }
	
	public Integer getWeeksToExport() {
		return weeksToExport;
	}

	public void setWeeksToExport(Integer weeksToExport) {
		this.weeksToExport = weeksToExport;
	}

        public Integer getKwToExport() {
            return kwToExport;
        }

        public void setKwToExport(Integer kwToExport) {
            this.kwToExport = kwToExport;
        }

        public Integer getKwYearToExport() {
            return kwYearToExport;
        }

        public void setKwYearToExport(Integer kwYearToExport) {
            this.kwYearToExport = kwYearToExport;
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

        public Integer getProjectToExport() {
            return projectToExport;
        }

        public void setProjectToExport(Integer projectToExport) {
            this.projectToExport = projectToExport;
        }

        public List<SelectItem> getManagedResourceTeamItems() {
		List<ResourceTeam> teams = resourceTeamsEJB.getManagedResourceTeams();
		List<SelectItem> result = new ArrayList<SelectItem>(teams.size());
		for (ResourceTeam t : teams) {
			result.add(new SelectItem(t.getId(), t.getName()));
		}
		return result;
	}

        public List<SelectItem> getManagedProjectItems() {
		List<Project> projects = projectsEjb.getManagedProjects();
		List<SelectItem> result = new ArrayList<SelectItem>(projects.size());
		for (Project p : projects) {
			result.add(new SelectItem(p.getId(), p.getName()));
		}
		return result;
	}

	public static DefaultStreamedContent streamForWorkbook(XSSFWorkbook wb, String filename) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] content = baos.toByteArray();

		return new DefaultStreamedContent(new ByteArrayInputStream(content), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filename+".xlsx");
	}

	
}
