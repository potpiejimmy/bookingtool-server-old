package com.wincor.bcon.bookingtool.server.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;

@Stateless
public class BudgetsEJB implements BudgetsEJBLocal {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;
	
	@Override
	@RolesAllowed({"admin","user"})
	public List<Project> getProjects() {
		return em.createNamedQuery("Project.findAll", Project.class).getResultList();
	}

	@Override
	@RolesAllowed({"admin","user"})
	public Project getProject(int projectId) {
		return em.find(Project.class, projectId);
	}

	@Override
	@RolesAllowed("admin")
	public void saveProject(Project project) {
		if (project.getId() != null)
			em.merge(project);  // update the project
		else
			em.persist(project);  // insert a new project
	}

	@Override
	@RolesAllowed("admin")
	public void deleteProject(int projectId) {
		em.remove(getProject(projectId));
	}

	@Override
	@RolesAllowed({"admin","user"})
	public List<Budget> getBudgets(int projectId) {
		return em.createNamedQuery("Budget.findByProjectId", Budget.class).setParameter("projectId", projectId).getResultList();
	}
        
	@Override
	@RolesAllowed({"admin","user"})
        public List<Budget> getBudgetsForParent(int parentId) {
        	return em.createNamedQuery("Budget.findByParentId", Budget.class).setParameter("parentId", parentId).getResultList();
        }

        @Override
	@RolesAllowed({"admin","user"})
        public List<Budget> getLeafBudgets(int parentId) {
            List<Budget> result = new ArrayList<Budget>();
            iterateLeafBudgets(result, getBudget(parentId));
            return result;
        }
        
        protected void iterateLeafBudgets(List<Budget> leafs, Budget currentBudget) {
            List<Budget> children = getBudgetsForParent(currentBudget.getId());
            if (children.isEmpty()) {
                // leaf budget:
                leafs.add(currentBudget);
            } else {
                // iterate children:
                for (Budget c : children) {
                    iterateLeafBudgets(leafs, c);
                }
            }
        }

	@Override
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getBudgetInfos(int projectId) {
		return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findByProjectId", Budget.class).setParameter("projectId", projectId).getResultList()));
	}

	@Override
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getBudgetInfosForParent(int projectId, Integer parentId) {
		if (parentId == null)
			return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findRoots", Budget.class).setParameter("projectId", projectId).getResultList()));
		else
			return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findByParentId", Budget.class).setParameter("parentId", parentId).getResultList()));
	}

	@Override
	@RolesAllowed({"admin","user"})
	public Budget getBudget(int budgetId) {
		return em.find(Budget.class, budgetId);
	}

	@Override
	@RolesAllowed({"admin","user"})
	public BudgetInfoVo getBudgetInfo(int budgetId) {
            return calculateBudget(toInfoVo(getBudget(budgetId)));
        }
        
	@Override
	@RolesAllowed("admin")
	public void saveBudget(Budget budget) {
		if (budget.getId() != null)
			em.merge(budget);  // update the project
		else
			em.persist(budget);  // insert a new project
	}

	@Override
	@RolesAllowed("admin")
	public void deleteBudget(int budgetId) {
		em.remove(getBudget(budgetId));
		
	}
	
	protected int getBookedMinutes(int budgetId) {
		Long s = (Long)em.createNamedQuery("Budget.getBookedMinutes").setParameter("budgetId", budgetId).getSingleResult(); 
		return s != null ? s.intValue() : 0;
	}
	
	protected BudgetInfoVo toInfoVo(Budget budget) {
		return new BudgetInfoVo(budget, getBookedMinutes(budget.getId()));
	}

	protected List<BudgetInfoVo> toInfoVos(List<Budget> budgets) {
		List<BudgetInfoVo> result = new ArrayList<BudgetInfoVo>(budgets.size());
		for (Budget b : budgets) result.add(toInfoVo(b));
		return result;
	}

	protected List<BudgetInfoVo> calculateBudgets(List<BudgetInfoVo> budgets) {
		for (BudgetInfoVo b : budgets) {
			calculateBudget(b);
		}
		return budgets;
	}
	
	protected BudgetInfoVo calculateBudget(BudgetInfoVo budget) {
		List<BudgetInfoVo> childBudgets = getBudgetInfosForParent(budget.getBudget().getProjectId(), budget.getBudget().getId());
		if (childBudgets.size() == 0) {
			// leaf budget
			//change prefix if budget was a root in his previous life
			if(budget.getBudget().getMinutes() < 0)
				budget.getBudget().setMinutes(-budget.getBudget().getMinutes());
			budget.setBookedMinutesRecursive(budget.getBookedMinutes());
			return budget;
		}
		
		int sum = 0;
		int sumBookedRecursive = budget.getBookedMinutes();
		for (BudgetInfoVo b : childBudgets) {
			BudgetInfoVo child = calculateBudget(b); 
			sum += Math.abs(child.getBudget().getMinutes());
			sumBookedRecursive += child.getBookedMinutesRecursive();
		}
		budget.getBudget().setMinutes(-sum); // negative means 'calculated'
		budget.setBookedMinutesRecursive(sumBookedRecursive);
		return budget;
	}
        
        @Override
        public boolean isDescendantOf(int parentBudgetId, int budgetId) {
            if (budgetId == parentBudgetId) return true;
            for (Budget b : getBudgetsForParent(parentBudgetId)) {
                if (isDescendantOf(b.getId(), budgetId)) return true;
            }
            return false;
        }
}
