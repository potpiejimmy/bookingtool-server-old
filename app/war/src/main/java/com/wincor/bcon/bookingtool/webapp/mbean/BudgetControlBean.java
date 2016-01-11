/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJB;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJB;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScoped
public class BudgetControlBean implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;

    @EJB
    private BudgetsEJB ejb;
    
    @EJB
    private ProjectsEJB projectsEjb;
    
    @Inject
    private BudgetsBean budgetsBean;
    
    private int projectId = 0;
    
    private Boolean editingAllowed = null;
        
    private List<BudgetInfoVo> rows = null;

    public List<SelectItem> getProjectItems() {
            List<Project> projects = projectsEjb.getProjects();
            List<SelectItem> result = new ArrayList<SelectItem>(projects.size() + 1);
            result.add(new SelectItem(0, "<Please choose>"));
            for (Project p : projects) {
                    result.add(new SelectItem(p.getId(), p.getName()));
            }
            return result;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
        this.editingAllowed = null; // reset editing allowed flag
        this.rows = null; // reset row data:
    }

    public void refreshList() {
        this.rows = null;
    }
	
    public boolean isEditingAllowed() {
        if (editingAllowed == null) {
            editingAllowed = Boolean.valueOf(
                    WebUtils.getHttpServletRequest().isUserInRole("admin") &&
                    projectsEjb.getAssignedManagers(getProjectId()).contains(WebUtils.getCurrentPerson()));
        }
        return editingAllowed;
    }
	
    public List<BudgetInfoVo> getRows() {
        if (rows == null) {
            List<BudgetInfoVo> budgets = ejb.getBudgetInfos(projectId);
            rows = new ArrayList<BudgetInfoVo>();
            for (BudgetInfoVo b : budgets) {
                if (b.getBookedMinutes() > 0 || // only show budgets with booked minutes...
                    (b.getBudget().getWorkProgress()!=null && b.getBudget().getWorkProgress()>0)) {  // ...or already edited work progress
                    b.setFullBudgetName(budgetsBean.getFullBudgetName(b.getBudget()));
                    rows.add(b);
                }
            }
            Collections.sort(rows, new BudgetsBean.BudgetVoComparator());
        }
        return rows;
    }
    
    public int getBudgetPrognosisOffset(BudgetInfoVo budget) {
        return ejb.getBudgetPrognosisOffset(budget);
    }
    
    public String getFormattedBudgetOffset(int minutes) {
        if (minutes == 0) return "";
        return (minutes > 0 ? "+" : "-") + budgetsBean.getFormattedBudgetTime(minutes);
    }
    
    public int getSumBudgetOffset() {
        int sum = 0;
        for (BudgetInfoVo b : getRows()) sum += getBudgetPrognosisOffset(b);
        return sum;
    }
    
    public String getFormattedSumBudgetOffset() {
        return getFormattedBudgetOffset(getSumBudgetOffset());
    }
    
    public void save() {
        for (BudgetInfoVo b : rows) {
            ejb.saveBudget(b.getBudget());
        }
    }
}
