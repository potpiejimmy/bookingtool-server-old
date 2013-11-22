/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlanItem;
import java.util.List;
import javax.ejb.Local;

/**
 * Provides the business logic to manage budget plans
 */
@Local
public interface BudgetPlansEJBLocal {
    
    /**
     * Get the list of budget plans
     * @return list of budget plans
     */
    public List<BudgetPlan> getBudgetPlans();
    
    /**
     * Inserts or updates a budget plan
     * @param plan a budget plan
     * @return the budget plan (holding the new ID if inserted)
     */
    public BudgetPlan saveBudgetPlan(BudgetPlan plan);
    
    /**
     * Deletes a budget plan
     * @param budgetPlanId a budget plan id
     */
    public void deleteBudgetPlan(int budgetPlanId);
    
    /**
     * Returns whether the sum of all budget item minutes of all budgets in the
     * given budget plan is equal to the budget plan's parent budget minutes.
     * @param budgetPlanId
     * @return true or false
     */
    public boolean isPlanComplete(int budgetPlanId);
    
    /**
     * Returns the list of budget plan items for the given leaf budget id
     * @param budgetId a leaf budget id
     * @return list of budget plan items
     */
    public List<BudgetPlanItem> getBudgetPlanItems(int budgetId);
    
    /**
     * Saves the given budget plan items for the given budget
     * @param budgetId a leaf budget id
     * @param items list of budget plan items
     */
    public void saveBudgetPlanItems(int budgetId,  List<BudgetPlanItem> items);

    /**
     * Returns the sum of all planned minutes of all leaf budgets under the
     * given parent budget for the given period
     * @param parentBudgetId a budget ID
     * @param period a period identifier (for instance yyyymm 201303)
     * @return 
     */
    public int getPlannedMinutesForPeriod(int parentBudgetId, int period);
}
