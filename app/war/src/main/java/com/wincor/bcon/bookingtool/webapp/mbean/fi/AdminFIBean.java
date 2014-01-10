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
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.fi.AdminFIEJBLocal;
import com.wincor.bcon.bookingtool.server.vo.AutoCreateInfoVo;
import com.wincor.bcon.bookingtool.webapp.mbean.BudgetsBean;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

@Named
@SessionScoped
public class AdminFIBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private BudgetsEJBLocal budgetsEjb;
	
	@EJB
	private AdminFIEJBLocal adminEjb;
	
	@Inject
	private BudgetsBean budgetsBean;
	
	private int currentProjectId = 0;
	
	private AutoCreateInfoVo current = null;
	
	private Map<Class<?>, List<?>> created = null;

	public AdminFIBean() {
		newVo();
	}
	
	public AutoCreateInfoVo getCurrent() {
		return current;
	}
	
	public int getCurrentProjectId() {
		if (currentProjectId == 0) {
			List<Project> projects = budgetsEjb.getProjects();
			if (projects.size() > 0)
				setCurrentProjectId(projects.get(projects.size()-1).getId());
		}
		return currentProjectId;
	}

	public void setCurrentProjectId(int currentProjectId) {
		this.currentProjectId = currentProjectId;
	}

	public List<SelectItem> getProjectItems() {
		List<Project> projects = budgetsEjb.getProjects();
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
			newVo();
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
	
	protected void newVo() {
		String lastPsp = "E-122618-02-14";
		String lastPspName = "CP_PC/E FI Release -14.1.0";
                int lastParentBudgetId = 0;
                int lastSpecBudgetId = 0;
		if (current != null) {
			lastPsp = current.getPspTemplate();
			lastPspName = current.getPspNameTemplate();
                        lastParentBudgetId = current.getParentBudgetId();
                        lastSpecBudgetId = current.getSpecBudgetId();
		}
		current = new AutoCreateInfoVo();
		current.setPspTemplate(lastPsp);
		current.setPspNameTemplate(lastPspName);
                current.setParentBudgetId(lastParentBudgetId);
                current.setSpecBudgetId(lastSpecBudgetId);
	}
	
	public Boolean getBudgetAvailable() {
		return budgetsEjb.getBudgets(getCurrentProjectId()).size() > 0;
	}
}
