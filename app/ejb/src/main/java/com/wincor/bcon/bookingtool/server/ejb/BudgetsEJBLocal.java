package com.wincor.bcon.bookingtool.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;

@Local
public interface BudgetsEJBLocal {
	
	/**
	 * Returns all projects that are visible to the current user
	 * @return list of projects
	 */
	public List<Project> getProjects();
	
	/**
	 * Returns the project with the given ID
	 * @param projectId a project ID
	 * @return the project
	 */
	public Project getProject(int projectId);

	/**
	 * Saves or updates the given project
	 * @param project a project
	 */
	public void saveProject(Project project);
	
	/**
	 * Removes the project with the given project ID
	 * @param projectId a project ID
	 */
	public void deleteProject(int projectId);
	
	/**
	 * Returns the list of all budgets found in the "budget" table
	 * for the given project
	 * @param projectId a project ID
	 * @return list of budgets
	 */
	public List<Budget> getBudgets(int projectId);

	/**
	 * Returns the list of budgets found for the given parent budget ID.
	 * 
	 * @param parentId a budget ID
	 * @return list of budgets
	 */
	public List<Budget> getBudgetsForParent(int parentId);
	
	/**
	 * Returns the list of all leaf budgets (recursively) found for the given
         * project
	 * 
	 * @param projectId a project ID
	 * @return list of leaf budgets (recursively)
	 */
        public List<Budget> getLeafBudgets(int projectId);
        
	/**
	 * Returns the list of leaf budgets (recursively) found for the given
         * parent budget ID.
	 * 
	 * @param parentId a budget ID
	 * @return list of leaf budgets (recursively)
	 */
	public List<Budget> getLeafBudgetsForParent(int parentId);
	
	/**
	 * Returns the list of all budgets found in the "budget" table
	 * for the given project
	 * @param projectId a project ID
	 * @return list of budgets
	 */
	public List<BudgetInfoVo> getBudgetInfos(int projectId);

	/**
	 * Returns the list of budgets found for the given parent budget ID.
	 * If null is specified for the parentId, the list of root budgets
	 * without a parent ID is returned.
	 * 
	 * @param projectId a project ID
	 * @param parentId a budget ID or null to retrieve root budgets
	 * @return list of budgets
	 */
	public List<BudgetInfoVo> getBudgetInfosForParent(int projectId, Integer parentId);
	
	/**
	 * Returns the list of budgets found for the given parent budget ID.
	 * If null is specified for the parentId, the list of root budgets
	 * without a parent ID is returned.
	 * 
	 * @param projectId a project ID
	 * @param parentId a budget ID or null to retrieve root budgets
         * @param period  a time period for the sum of booking minutes returned in the info value object
	 * @return list of budgets
	 */
	public List<BudgetInfoVo> getBudgetInfosForParent(int projectId, Integer parentId, TimePeriod period);
	
	/**
	 * Returns the list of all leaf budgets found in the "budget" table
	 * for the given project
	 * @param projectId a project ID
	 * @return list of leaf budgets
	 */
	public List<BudgetInfoVo> getLeafBudgetInfos(int projectId);

	/**
	 * Returns the budget with the given ID
	 * @param budgetId a budget ID
	 * @return the budget
	 */
	public Budget getBudget(int budgetId);

	/**
	 * Returns the budget info value object for the given budget ID
	 * @param budgetId a budget ID
	 * @return the budget info value object
	 */
	public BudgetInfoVo getBudgetInfo(int budgetId);

	/**
	 * Returns the budget info value object for the given budget ID
	 * @param budgetId a budget ID
         * @param period  a time period for the sum of booking minutes returned in the info value object
	 * @return the budget info value object
	 */
	public BudgetInfoVo getBudgetInfo(int budgetId, TimePeriod period);

	/**
	 * Saves or updates the given budget
	 * @param budget a budget
	 */
	public void saveBudget(Budget budget);
	
	/**
	 * Removes the budget with the given budget ID
	 * @param budgetId a budget ID
	 */
	public void deleteBudget(int budgetId);

        /**
         * Returns true if the given budget is either the same as or a recursive
         * descendant of the given parent budget.
         * @param parentBudgetId a parent budget ID
         * @param budgetId a budget ID
         * @return true or false
         */
        public boolean isDescendantOf(int parentBudgetId, int budgetId);

        /**
         * Calculates the budget prognosis and returns the offset to the
         * budget amount.
         * @param budget a budget
         * @return the budget prognosis offset
         */
        public int getBudgetPrognosisOffset(BudgetInfoVo budget);
}
