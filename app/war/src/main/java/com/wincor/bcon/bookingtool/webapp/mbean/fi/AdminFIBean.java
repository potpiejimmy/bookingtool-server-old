package com.wincor.bcon.bookingtool.webapp.mbean.fi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJB;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJB;
import com.wincor.bcon.bookingtool.server.ejb.fi.AdminFIEJB;
import com.wincor.bcon.bookingtool.server.vo.AutoCreateInfoVo;
import com.wincor.bcon.bookingtool.webapp.mbean.BudgetsBean;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

@Named
@SessionScoped
public class AdminFIBean implements Serializable {

	private static final long serialVersionUID = 1L;

        @EJB
        private ProjectsEJB projectsEjb;
    
	@EJB
	private BudgetsEJB budgetsEjb;
	
	@EJB
	private AdminFIEJB adminEjb;
	
	@Inject
	private BudgetsBean budgetsBean;
	
	private int currentProjectId = 0;
	
	private AutoCreateInfoVo current = null;
	
	private Map<Class<?>, List<?>> created = null;

	public AutoCreateInfoVo getCurrent() {
            if (current == null) newVo(null);
            return current;
	}
	
	public int getCurrentProjectId() {
		if (currentProjectId == 0) {
			List<Project> projects = projectsEjb.getManagedProjects();
			if (projects.size() > 0)
				setCurrentProjectId(projects.get(projects.size()-1).getId());
		}
		return currentProjectId;
	}

	public void setCurrentProjectId(int currentProjectId) {
            if (this.currentProjectId != currentProjectId) {
                this.currentProjectId = currentProjectId;
                newVo(null);
                selectDefaults();
            }
	}

	public List<SelectItem> getProjectItems() {
		List<Project> projects = projectsEjb.getManagedProjects();
		List<SelectItem> result = new ArrayList<SelectItem>(projects.size());
		for (Project p : projects) {
			result.add(new SelectItem(p.getId(), p.getName()));
		}
		return result;
	}
	
	public List<SelectItem> getBudgetItems() {
		List<Budget> budgets = budgetsEjb.getBudgets(currentProjectId);
		List<SelectItem> result = new ArrayList<SelectItem>(budgets.size());
		for (Budget b : budgets) {
			result.add(new SelectItem(b.getId(), budgetsBean.getFullBudgetName(b)));
		}
		return WebUtils.sortSelectItems(result);
	}
	
	public int getCurrentBudgetHoursDev() {
		return current.getMinutesDev() / 60;
	}
	
	public void setCurrentBudgetHoursDev(int hours) {
		current.setMinutesDev(hours * 60);
	}
	
	public int getCurrentBudgetHoursQA() {
		return current.getMinutesQA() / 60;
	}
	
	public void setCurrentBudgetHoursQA(int hours) {
		current.setMinutesQA(hours * 60);
	}
	
	@SuppressWarnings("unchecked")
	public List<Budget> getCreatedBudgets() {
		if (created == null) return null;
		return (List<Budget>)created.get(Budget.class);
	}
	
	@SuppressWarnings("unchecked")
	public List<BookingTemplate> getCreatedTemplates() {
		if (created == null) return null;
		return (List<BookingTemplate>)created.get(BookingTemplate.class);
	}
	
	public void save() {
		try {
			current.setProjectId(currentProjectId);
			created = adminEjb.autoCreateBudgetsAndTemplates(current);
			newVo(current);
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
	
	protected void newVo(AutoCreateInfoVo lastUsed) {
                Project project = projectsEjb.getProject(getCurrentProjectId());
		String lastPsp = project.getPsp() != null ? project.getPsp() : "E-122951-02-40";
		String lastPspName = "R_PC/E - " + project.getName();
                int lastParentBudgetId = 0;
                int lastSpecBudgetId = 0;
		if (lastUsed != null) {
			lastPsp = lastUsed.getPspTemplate();
			lastPspName = lastUsed.getPspNameTemplate();
                        lastParentBudgetId = lastUsed.getParentBudgetId();
                        lastSpecBudgetId = lastUsed.getSpecBudgetId();
		}
		current = new AutoCreateInfoVo();
		current.setPspTemplate(lastPsp);
		current.setPspNameTemplate(lastPspName);
                current.setParentBudgetId(lastParentBudgetId);
                current.setSpecBudgetId(lastSpecBudgetId);
	}
	
        protected void selectDefaults() {
            // set default parent budget (look for "CRs" if available)
            for (Budget b : budgetsEjb.getBudgets(currentProjectId))
                if ("crs".equalsIgnoreCase(b.getName())) current.setParentBudgetId(b.getId());
            // set default specification budget (look for "Arbeit an Spezifikationen" if available)
            for (Budget b : budgetsEjb.getBudgets(currentProjectId))
                if ("Arbeit an Spezifikationen".equalsIgnoreCase(b.getName())) current.setSpecBudgetId(b.getId());
        }
        
	public Boolean getBudgetAvailable() {
		return budgetsEjb.getBudgets(getCurrentProjectId()).size() > 0;
	}
}
