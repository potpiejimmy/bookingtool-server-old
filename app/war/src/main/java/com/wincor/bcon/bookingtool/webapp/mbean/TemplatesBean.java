package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.BookingTemplatesEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

@Named
@SessionScoped
public class TemplatesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private BudgetsEJBLocal budgetsEjb;
	
	@EJB
	private BookingTemplatesEJBLocal ejb;
	
	@Inject
	private BudgetsBean budgetsBean;
	
	private int currentProjectId = 0;
	
	private BookingTemplate currentTemplate = null;
	
	public TemplatesBean() {
		clear();
	}

	public void clear() {
		currentTemplate = new BookingTemplate();
		currentTemplate.setActive((byte)1);
	}

	public BookingTemplate getCurrentTemplate() {
		return currentTemplate;
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
	
	public List<BookingTemplate> getTemplates() {
		return ejb.getBookingTemplatesByProjectId(this.getCurrentProjectId());
	}
	
	public List<SelectItem> getBudgetItems() {
		List<Budget> budgets = budgetsEjb.getBudgets(currentProjectId);
		List<SelectItem> result = new ArrayList<SelectItem>(budgets.size());
		for (Budget b : budgets) {
			result.add(new SelectItem(b.getId(), budgetsBean.getFullBudgetName(b)));
		}
		return WebUtils.sortSelectItems(result);
	}
        
        public String getBudgetDisplayName(int budgetId) {
            final int MAX_PART_SIZE = 32;
            Budget b = budgetsEjb.getBudget(budgetId);
            String displayName = b.getName();
            if (displayName.length() > MAX_PART_SIZE) displayName = "\u2026" + displayName.substring(displayName.length() - MAX_PART_SIZE);
            if (b.getParentId() != null && displayName.length() < MAX_PART_SIZE) {
                String parentName = budgetsEjb.getBudget(b.getParentId()).getName();
                if (parentName.length() > MAX_PART_SIZE) parentName = "\u2026" + parentName.substring(parentName.length() - MAX_PART_SIZE);
                displayName = parentName + " \u25B6 " + displayName;
            }
            return displayName;
        }
	
	public void save() {
		ejb.saveBookingTemplate(currentTemplate);
		clear();
	}
	
	public void edit(BookingTemplate t) {
		currentTemplate = t;
		currentProjectId = budgetsEjb.getBudget(t.getBudgetId()).getProjectId();
	}
	
	public void delete(BookingTemplate t) {
		try {
			ejb.deleteBookingTemplate(t.getId());
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
	
	public Boolean getActive() {
		return currentTemplate.getActive() == 1;
	}
	
	public void setActive(Boolean active) {
		currentTemplate.setActive(active ? (byte)1 : (byte)0);
	}
	
	public Boolean getBudgetAvailable() {
		return budgetsEjb.getBudgets(getCurrentProjectId()).size() > 0;
	}
}
