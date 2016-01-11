/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlanItem;
import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class BudgetPlansEJB {

    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @EJB
    private DomainsEJB domainsEjb;
    
    @EJB
    private BudgetsEJB budgetsEjb;

    /**
     * Get the budget plan for the given ID
     * @param id budget plan id
     * @return budget plan
     */
    public BudgetPlan getBudgetPlan(int id) {
        return em.find(BudgetPlan.class, id);
    }
    
    /**
     * Get the list of budget plans
     * @return list of budget plans
     */
    public List<BudgetPlan> getBudgetPlans() {
        List<BudgetPlan> result = new ArrayList<BudgetPlan>();
        for (Domain domain : domainsEjb.getDomains())
            result.addAll(em.createNamedQuery("BudgetPlan.findByDomainId", BudgetPlan.class).setParameter("domainId", domain.getId()).getResultList());
        return result;
    }
    
    /**
     * Get the list of budget plans for a given project
     * @param projectId a project ID
     * @return list of budget plans
     */
    public List<BudgetPlan> getBudgetPlansForProject(int projectId) {
        return em.createNamedQuery("BudgetPlan.findByProjectId", BudgetPlan.class).setParameter("projectId", projectId).getResultList();
    }
    
    /**
     * Inserts or updates a budget plan
     * @param plan a budget plan
     * @return the budget plan (holding the new ID if inserted)
     */
    @RolesAllowed("admin")
    public BudgetPlan saveBudgetPlan(BudgetPlan plan) {
        if (plan.getId() == null) {
            // check whether there is not already a budget plan for the given
            // budget or any of its parent or child budgets:
            for (BudgetPlan p : getBudgetPlans()) {
                if (budgetsEjb.isDescendantOf(plan.getBudgetId(), p.getBudgetId()) ||
                    budgetsEjb.isDescendantOf(p.getBudgetId(), plan.getBudgetId())) {
                    throw new IllegalArgumentException("Sorry, there is already a budget plan for the budget or any of its parent or child budgets. Budget plans must not overlap with other budget plans.");
                }
            }
            
            em.persist(plan);
            em.flush(); // fetch new ID into object
        } else {
            em.merge(plan);
        }
        return plan;
    }
    
    /**
     * Deletes a budget plan
     * @param budgetPlanId a budget plan id
     */
    public void deleteBudgetPlan(int budgetPlanId) {
        em.remove(em.find(BudgetPlan.class, budgetPlanId));
    }
    
    /**
     * Returns whether the sum of all budget item minutes of all budgets in the
     * given budget plan is equal to the budget plan's parent budget minutes.
     * @param budgetPlanId
     * @return true or false
     */
    public boolean isPlanComplete(int budgetPlanId) {
        int sum = 0;
        BudgetPlan plan = em.find(BudgetPlan.class, budgetPlanId);
        BudgetInfoVo planParentBudget = budgetsEjb.getBudgetInfo(plan.getBudgetId());
        for (Budget b : budgetsEjb.getLeafBudgetsForParent(planParentBudget.getBudget().getId())) {
            for (BudgetPlanItem i : getBudgetPlanItems(b.getId())) {
                sum += i.getMinutes();
            }
        }
        return sum == Math.abs(planParentBudget.getBudget().getMinutes());
    }
    
    /**
     * Returns the list of budget plan items for the given leaf budget id
     * @param budgetId a leaf budget id
     * @return list of budget plan items
     */
    public List<BudgetPlanItem> getBudgetPlanItems(int budgetId) {
        return em.createNamedQuery("BudgetPlanItem.findByBudgetId", BudgetPlanItem.class).setParameter("budgetId", budgetId).getResultList();
    }

    protected BudgetPlanItem getBudgetPlanItem(int budgetId, int period) {
        try {
            return em.createNamedQuery("BudgetPlanItem.findByBudgetIdAndPeriod", BudgetPlanItem.class).
                    setParameter("budgetId", budgetId).
                    setParameter("period", period).
                    getSingleResult();
        } catch (Exception ex) {
            // no result, just ignore
            return null;
        }
    }
    
    /**
     * Returns the sum of all planned minutes of all leaf budgets under the
     * given parent budget for the given period
     * @param parentBudgetId a budget ID
     * @param period a period identifier (for instance yyyymm 201303)
     * @return 
     */
    public int getPlannedMinutesForPeriod(int parentBudgetId, int period) {
        int sum = 0;
        for (Budget b : budgetsEjb.getLeafBudgetsForParent(parentBudgetId)) {
            BudgetPlanItem item = getBudgetPlanItem(b.getId(), period);
            if (item != null) sum += item.getMinutes();
        }
        return sum;
    }

    /**
     * Saves the given budget plan items for the given budget
     * @param budgetId a leaf budget id
     * @param items list of budget plan items
     */
    public void saveBudgetPlanItems(int budgetId,  List<BudgetPlanItem> items) {
        // delete all existing
        em.createNamedQuery("BudgetPlanItem.deleteByBudgetId").setParameter("budgetId", budgetId).executeUpdate();
        
        for (BudgetPlanItem item : items) {
            item.setBudgetId(budgetId);
            em.persist(item);
        }
    }
}
