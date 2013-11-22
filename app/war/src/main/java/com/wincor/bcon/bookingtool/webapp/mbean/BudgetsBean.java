package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.Collections;
import java.util.Comparator;

@Named
@SessionScoped
public class BudgetsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private BudgetsEJBLocal ejb;
	
	private Budget currentBudget = null;
	private int currentBudgetHours = 0; // for editing Budget in hours
	
	private int parentFilter = 0;

	public void clear() {
		newBudget();
	}

	public Budget getCurrentBudget() {
		if (currentBudget == null) newBudget(); // lazy initialization to enable EJB access
		return currentBudget;
	}
	
	public List<SelectItem> getProjectItems() {
		List<Project> projects = ejb.getProjects();
		List<SelectItem> result = new ArrayList<SelectItem>(projects.size());
		for (Project p : projects) {
			result.add(new SelectItem(p.getId(), p.getName()));
		}
		return result;
	}
	
	public int getCurrentProjectId() {
		return getCurrentBudget().getProjectId();
	}

	public void setCurrentProjectId(int currentProjectId) {
		getCurrentBudget().setProjectId(currentProjectId);
		parentFilter = 0; // reset filter if changing project
		newBudget(); // and reset input fields
	}

	public Project getProject(Integer projectId) {
		return ejb.getProject(projectId);
	}
	
	public Budget getBudget(Integer budgetId) {
		return ejb.getBudget(budgetId);
	}
	
	public List<BudgetInfoVo> getBudgets() {
		if (parentFilter < 0) {
			List<BudgetInfoVo> budgets = ejb.getBudgetInfos(currentBudget.getProjectId());
                        // for the full list of budgets, sort by full budget name:
                        for (BudgetInfoVo b : budgets) b.setFullBudgetName(getFullBudgetName(b.getBudget()));
                        Collections.sort(budgets, new BudgetVoComparator());
                        return budgets;
                } else {
			return ejb.getBudgetInfosForParent(currentBudget.getProjectId(), parentFilter == 0 ? null : parentFilter);
                }
	}
	
	public List<SelectItem> getBudgetFilterItems() {
		List<Budget> budgets = ejb.getBudgets(currentBudget.getProjectId());
		List<SelectItem> result = new ArrayList<SelectItem>(budgets.size() + 2);
		result.add(new SelectItem(-1, "<Show all>"));
		result.add(new SelectItem(0, "<Root budgets>"));
		for (Budget b : budgets) {
			result.add(new SelectItem(b.getId(), getFullBudgetName(b)));
		}
		return WebUtils.sortSelectItems(result);
	}
	
	public List<SelectItem> getParentBudgetItems() {
		List<Budget> budgets = ejb.getBudgets(currentBudget.getProjectId());
		List<SelectItem> result = new ArrayList<SelectItem>(budgets.size() + 1);
		result.add(new SelectItem(-1, "<No parent>"));
		for (Budget b : budgets) {
                    //do not add myself or my descendants to the parents list
                    if (currentBudget.getId() == null ||
                        !ejb.isDescendantOf(currentBudget.getId(), b.getId()))
                        result.add(new SelectItem(b.getId(), getFullBudgetName(b)));
		}
		return WebUtils.sortSelectItems(result);
	}
        
	public int getCurrentBudgetHours() {
		return currentBudgetHours;
	}
	
	public String getFormattedBudgetTime(int minutes) {
		int hours = Math.abs(minutes / 60);
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(1);
		return f.format(((float)hours)/8) + " PT";
	}

	public void setCurrentBudgetHours(int currentBudgetHours) {
		this.currentBudgetHours = currentBudgetHours;
		currentBudget.setMinutes(currentBudgetHours * 60);
	}

	public int getParentFilter() {
		return parentFilter;
	}

	public void setParentFilter(int parentFilter) {
		this.parentFilter = parentFilter;
		currentBudget.setParentId(parentFilter);
	}
	
	public String getFullBudgetNameForId(Integer budgetId) {
            return getFullBudgetName(ejb.getBudget(budgetId));
        }
        
	public String getFullBudgetName(Budget b) {
		StringBuilder stb = new StringBuilder(b.getName());
		while (b.getParentId() != null) {
			b = ejb.getBudget(b.getParentId());
			stb.insert(0, b.getName() + " \u25B6 ");
		}
		return stb.toString();
	}

	public void save() {
		try {
			// reset to null if no valid parent ID specified
			if (currentBudget.getParentId() != null &&
			    currentBudget.getParentId().intValue() <= 0)
				currentBudget.setParentId(null);
			
			ejb.saveBudget(currentBudget);
			newBudget();
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
	
	public void edit(Budget b) {
		currentBudget = b;
		currentBudgetHours = b.getMinutes() / 60;
	}
	
	public void delete(Budget b) {
		try {
			ejb.deleteBudget(b.getId());
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
	
	public void showSubBudgets(Budget b) {
		setParentFilter(b.getId());
	}
	
	public void newBudget() {
		int lastUsedProject = currentBudget != null ? currentBudget.getProjectId() : 0;
		currentBudget = new Budget();
		currentBudgetHours = 0;
		
		if (lastUsedProject > 0) {
			if (ejb.getProject(lastUsedProject) == null) /* last project removed? */
				lastUsedProject = 0;
		}
		
		if (lastUsedProject <= 0) {
			// set the default project (the last one)
			List<Project> projects = ejb.getProjects();
			if (projects.size() > 0) {
				lastUsedProject = projects.get(projects.size()-1).getId();
			}
		}
		
		currentBudget.setProjectId(lastUsedProject);
	}
        
        public static class BudgetVoComparator implements Comparator<BudgetInfoVo>
        {
            @Override
            public int compare(BudgetInfoVo o1, BudgetInfoVo o2) {
                return o1.getFullBudgetName().compareTo(o2.getFullBudgetName());
            }
        }
}
