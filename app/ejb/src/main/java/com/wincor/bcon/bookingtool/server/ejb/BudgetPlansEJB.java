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
public class BudgetPlansEJB implements BudgetPlansEJBLocal {

    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @EJB
    private DomainsEJBLocal domainsEjb;
    
    @EJB
    private BudgetsEJBLocal budgetsEjb;

    @Override
    public BudgetPlan getBudgetPlan(int id) {
        return em.find(BudgetPlan.class, id);
    }
    
    @Override
    public List<BudgetPlan> getBudgetPlans() {
        List<BudgetPlan> result = new ArrayList<BudgetPlan>();
        for (Domain domain : domainsEjb.getDomains())
            result.addAll(em.createNamedQuery("BudgetPlan.findByDomainId", BudgetPlan.class).setParameter("domainId", domain.getId()).getResultList());
        return result;
    }
    
    @Override
    public List<BudgetPlan> getBudgetPlansForProject(int projectId) {
        return em.createNamedQuery("BudgetPlan.findByProjectId", BudgetPlan.class).setParameter("projectId", projectId).getResultList();
    }
    
    @Override
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
    
    @Override
    public void deleteBudgetPlan(int budgetPlanId) {
        em.remove(em.find(BudgetPlan.class, budgetPlanId));
    }
    
    @Override
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
    
    @Override
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
    
    @Override
    public int getPlannedMinutesForPeriod(int parentBudgetId, int period) {
        int sum = 0;
        for (Budget b : budgetsEjb.getLeafBudgetsForParent(parentBudgetId)) {
            BudgetPlanItem item = getBudgetPlanItem(b.getId(), period);
            if (item != null) sum += item.getMinutes();
        }
        return sum;
    }

    @Override
    public void saveBudgetPlanItems(int budgetId,  List<BudgetPlanItem> items) {
        // delete all existing
        em.createNamedQuery("BudgetPlanItem.deleteByBudgetId").setParameter("budgetId", budgetId).executeUpdate();
        
        for (BudgetPlanItem item : items) {
            item.setBudgetId(budgetId);
            em.persist(item);
        }
    }
}
