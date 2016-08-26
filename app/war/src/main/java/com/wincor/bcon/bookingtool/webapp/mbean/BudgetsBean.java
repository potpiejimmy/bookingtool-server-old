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
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJB;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJB;
import com.wincor.bcon.bookingtool.server.util.ExcelExportUtil;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.Collections;
import java.util.Comparator;
import javax.faces.context.FacesContext;
import org.primefaces.model.StreamedContent;

@Named
@SessionScoped
public class BudgetsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private BudgetsEJB ejb;

        @EJB
        private ProjectsEJB projectsEjb;
    
        private final NumberFormat budgetTimeFormatter = NumberFormat.getNumberInstance();
        private final String daysSuffix = " " + WebUtils.getResBundle().getString("days_suffix");
        
	private Budget currentBudget = null;
	private int currentBudgetHours = 0; // for editing Budget in hours
        
        private Budget selectedBudget = null; // selected in list for move/delete
        private Integer moveToProjectId = null;
	
	private int parentFilter = -1; // default <Show All>
        
        private Boolean editingAllowed = null;
        
        private List<BudgetInfoVo> currentRows = null;

	public void clear() {
		newBudget();
	}
        
        public void checkRequestParams() {
            String budget = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("budgetId");
            if (budget != null) {
                try {
                    Budget b = ejb.getBudget(Integer.parseInt(budget));
                    this.setCurrentProjectId(b.getProjectId());
                    this.edit(b);
                } catch (Exception ex) {
                    // ignore
                } 
            }
        }
        
	public Budget getCurrentBudget() {
		if (currentBudget == null) newBudget(); // lazy initialization to enable EJB access
		return currentBudget;
	}
        
	public BudgetInfoVo getCurrentBudgetInfo() {
		return ejb.getBudgetInfo(getCurrentBudget().getId());
	}
	
        public boolean isEditingAllowed() {
            if (editingAllowed == null) {
                editingAllowed =
                        WebUtils.getHttpServletRequest().isUserInRole("admin") &&
                        projectsEjb.getAssignedManagers(getCurrentProjectId()).contains(WebUtils.getCurrentPerson());
            }
            return editingAllowed;
        }
	
	public List<SelectItem> getProjectItems() {
		List<Project> projects = projectsEjb.getProjects();
		List<SelectItem> result = new ArrayList<SelectItem>(projects.size() + 1);
                result.add(new SelectItem(0, "<Please choose>"));
		for (Project p : projects) {
			result.add(new SelectItem(p.getId(), p.getName()));
		}
		return result;
	}
	
	public List<SelectItem> getProjectItemsForMove() {
		List<Project> projects = projectsEjb.getManagedProjects();
		List<SelectItem> result = new ArrayList<SelectItem>(Math.max(0, projects.size() - 1));
		for (Project p : projects) {
                    if (!p.getId().equals(getCurrentProjectId()))
			result.add(new SelectItem(p.getId(), p.getName()));
		}
		return result;
	}
	
	public int getCurrentProjectId() {
		return getCurrentBudget().getProjectId();
	}

	public void setCurrentProjectId(int currentProjectId) {
		getCurrentBudget().setProjectId(currentProjectId);
                currentRows = null; // reset row data if changing project
		parentFilter = -1; // reset filter if changing project
                editingAllowed = null; // reset editing allowed flag
		newBudget(); // and reset input fields
	}

	public Project getProject(Integer projectId) {
		return projectsEjb.getProject(projectId);
	}
	
	public Budget getBudget(Integer budgetId) {
		return ejb.getBudget(budgetId);
	}
	
	public List<BudgetInfoVo> getBudgets() {
            if (currentRows == null) {
		if (parentFilter < 0) { // special filter
			if (parentFilter == -1) // complete list
                            currentRows = ejb.getBudgetInfos(currentBudget.getProjectId());
                        else /* parentFilter == -2 */ // all leaf budgets
                            currentRows = ejb.getLeafBudgetInfos(currentBudget.getProjectId());
                        // for the full list of budgets or the leaf budgets, sort by full budget name:
                        for (BudgetInfoVo b : currentRows) b.setFullBudgetName(getFullBudgetName(b.getBudget()));
                        Collections.sort(currentRows, new BudgetVoComparator());
                } else {
			currentRows = ejb.getBudgetInfosForParent(currentBudget.getProjectId(), parentFilter == 0 ? null : parentFilter);
                }
            }
            return currentRows;
	}
        
        public StreamedContent getExportBudgets() {
            return ExcelExportBean.streamForWorkbook(ExcelExportUtil.createWorkbookForBudgets(getBudgets()), "budgets");
        }
	
	public List<SelectItem> getBudgetFilterItems() {
		List<Budget> budgets = ejb.getBudgets(currentBudget.getProjectId());
		List<SelectItem> result = new ArrayList<SelectItem>(budgets.size() + 3);
		result.add(new SelectItem(0, "<Root budgets>"));
		result.add(new SelectItem(-1, "<Show all>"));
		result.add(new SelectItem(-2, "<Leaf budgets>"));
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
            return getFormattedBudgetTime(minutes, true);
        }
        
	public String getFormattedBudgetTime(int minutes, boolean appendSuffix) {
		float hours = ((float)Math.abs(minutes)) / 60;
		budgetTimeFormatter.setMaximumFractionDigits(2);
		String result = budgetTimeFormatter.format(hours/8);
                if (appendSuffix) result += daysSuffix;
                return result;
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
                refreshList(); // reset row data if changing filter
	}
	
	public String getFullBudgetNameForId(Integer budgetId) {
            return ejb.getFullBudgetName(budgetId);
        }
        
	public String getFullBudgetName(Budget b) {
            return ejb.getFullBudgetName(b.getId());
	}

	public void save() {
		try {
			// reset to null if no valid parent ID specified
			if (currentBudget.getParentId() != null &&
			    currentBudget.getParentId().intValue() <= 0)
				currentBudget.setParentId(null);
			
			ejb.saveBudget(currentBudget);
                        refreshList(); // reset row data on save
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
                        refreshList(); // reset row data on delete
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
        
        public void refreshList() {
            currentRows = null;
        }
	
	public void showSubBudgets(Budget b) {
		setParentFilter(b.getId());
	}
	
	public void newBudget() {
		int lastUsedProject = currentBudget != null ? currentBudget.getProjectId() : 0;
		currentBudget = new Budget();
		currentBudgetHours = 0;
		
		if (lastUsedProject > 0) {
			if (projectsEjb.getProject(lastUsedProject) == null) /* last project removed? */
				lastUsedProject = 0;
		}
		
		if (lastUsedProject <= 0) {
			// set the default project (the last one)
			List<Project> projects = projectsEjb.getProjects();
			if (projects.size() > 0) {
				lastUsedProject = projects.get(projects.size()-1).getId();
			}
		}
		
		currentBudget.setProjectId(lastUsedProject);
	}
        
        public boolean isAllowOverrun() {
            return currentBudget.getAllowOverrun() == 1;
        }
        
        public void setAllowOverrun(boolean allowOverrun) {
            currentBudget.setAllowOverrun(allowOverrun ? (byte)1 : (byte)0);
        }

    public Budget getSelectedBudget() {
        return selectedBudget;
    }

    public void setSelectedBudget(Budget selectedBudget) {
        this.selectedBudget = selectedBudget;
    }
        
    public void moveSelectedBudget() {
        ejb.moveBudget(selectedBudget.getId(), moveToProjectId);
        currentRows = null; // reset row data
    }

    public void cloneSelectedBudget() {
        ejb.cloneBudget(selectedBudget.getId(), moveToProjectId);
        setCurrentProjectId(moveToProjectId); // go to target project after clone
    }

    public Integer getMoveToProjectId() {
        return moveToProjectId;
    }

    public void setMoveToProjectId(Integer moveToProjectId) {
        this.moveToProjectId = moveToProjectId;
    }
        
        public static class BudgetVoComparator implements Comparator<BudgetInfoVo>
        {
            @Override
            public int compare(BudgetInfoVo o1, BudgetInfoVo o2) {
                return o1.getFullBudgetName().compareTo(o2.getFullBudgetName());
            }
        }
}
