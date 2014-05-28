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
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import javax.faces.context.FacesContext;

@Named
@SessionScoped
public class TemplatesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private BookingTemplatesEJBLocal ejb;
	
        @EJB
        private ProjectsEJBLocal projectsEjb;
    
	@EJB
	private BudgetsEJBLocal budgetsEjb;
	
	@Inject
	private BudgetsBean budgetsBean;
	
	private int currentProjectId = 0;
	
	private BookingTemplate currentTemplate = null;
	
	private int budgetFilter = 0;

	public TemplatesBean() {
		clear();
	}
        
        public void checkRequestParams() {
            String paramFilter = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("filter");
            String paramCreate = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("create");
            
            if (paramFilter != null || paramCreate != null) {
                try {
                    int filter = Integer.parseInt(paramFilter!=null ? paramFilter : paramCreate);
                    Budget filterBudget = budgetsEjb.getBudget(filter);
                    this.currentProjectId = filterBudget.getProjectId();
                    this.budgetFilter = filterBudget.getId();
                    
                    if (paramCreate != null) {
                        newForBudget(filterBudget.getId());
                    }
                } catch (Exception ex) {
                    // ignore
                } 
            }
            
            String editTemplate = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editTemplate");
            if (editTemplate != null) {
                try {
                    this.edit(ejb.getBookingTemplate(Integer.parseInt(editTemplate)));
                } catch (Exception ex) {
                    // ignore
                } 
            }
        }

        protected void newForBudget(int budgetId) {
            clear();
            currentTemplate.setBudgetId(budgetId);
            edit(currentTemplate);
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
			List<Project> projects = projectsEjb.getManagedProjects();
			if (projects.size() > 0)
				setCurrentProjectId(projects.get(projects.size()-1).getId());
		}
		return currentProjectId;
	}

	public void setCurrentProjectId(int currentProjectId) {
                // clear filter if project changed:
                if (this.currentProjectId != currentProjectId) budgetFilter = 0;
                this.currentProjectId = currentProjectId;
	}

	public List<SelectItem> getProjectItems() {
		List<Project> projects = projectsEjb.getManagedProjects();
		List<SelectItem> result = new ArrayList<SelectItem>(projects.size());
		for (Project p : projects) {
			result.add(new SelectItem(p.getId(), p.getName()));
		}
		return result;
	}
	
	public List<BookingTemplateRowVo> getTemplates() {
            List<BookingTemplate> templates;
            if (budgetFilter > 0)
                templates = ejb.getBookingTemplatesByBudgetId(budgetFilter);
            else
		templates = ejb.getBookingTemplatesByProjectId(getCurrentProjectId());
            List<BookingTemplateRowVo> result = new ArrayList<BookingTemplateRowVo>(templates.size());
            for (BookingTemplate t : templates) result.add(new BookingTemplateRowVo(t));
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

	public List<SelectItem> getBudgetFilterItems() {
		List<Budget> budgets = budgetsEjb.getBudgets(currentProjectId);
		List<SelectItem> result = new ArrayList<SelectItem>(budgets.size() + 1);
		result.add(new SelectItem(0, "<Show all>"));
		for (Budget b : budgets) {
			result.add(new SelectItem(b.getId(), budgetsBean.getFullBudgetName(b)));
		}
		return WebUtils.sortSelectItems(result);
	}
	
        public int getBudgetFilter() {
                return budgetFilter;
        }

        public void setBudgetFilter(int budgetFilter) {
                this.budgetFilter = budgetFilter;
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
        public void copy(BookingTemplate t) {
	t.setId(null);
        edit(t);
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
        
        public void rowActiveCheckboxClicked(BookingTemplateRowVo vo) {
            vo.setActive(vo.getActive() ^ true);
            ejb.saveBookingTemplate(vo.getTemplate());
        }
        
        /**
         * Wrapper class for row-editing of the 'active' field via selectBooleanCheckbox
         */
        public static class BookingTemplateRowVo implements java.io.Serializable {
            
            private BookingTemplate template;
            
            public BookingTemplateRowVo(BookingTemplate template) {
                this.template = template;
            }
            
            public BookingTemplate getTemplate() {
                return this.template;
            }
            
            public Boolean getActive() {
                return this.template.getActive() == 1;
            }

            public void setActive(Boolean active) {
                this.template.setActive(active ? (byte)1 : (byte)0);
            }
        }
}
