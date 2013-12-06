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
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import javax.ejb.EJB;
import javax.persistence.TemporalType;

@Stateless
public class BudgetsEJB implements BudgetsEJBLocal {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;
        
        @EJB
        private BookingTemplatesEJBLocal bookingsEjb;
	
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
            return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findByProjectId", Budget.class).setParameter("projectId", projectId).getResultList()), null);
	}

	@Override
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getBudgetInfosForParent(int projectId, Integer parentId) {
            return getBudgetInfosForParent(projectId, parentId, null);
	}

	@Override
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getBudgetInfosForParent(int projectId, Integer parentId, TimePeriod period) {
            if (parentId == null)
                return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findRoots", Budget.class).setParameter("projectId", projectId).getResultList(), period), period);
            else
                return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findByParentId", Budget.class).setParameter("parentId", parentId).getResultList(), period), period);
	}

	@Override
	@RolesAllowed({"admin","user"})
	public Budget getBudget(int budgetId) {
		return em.find(Budget.class, budgetId);
	}

	@Override
	@RolesAllowed({"admin","user"})
	public BudgetInfoVo getBudgetInfo(int budgetId) {
            return getBudgetInfo(budgetId, null);
        }
        
	@Override
	@RolesAllowed({"admin","user"})
	public BudgetInfoVo getBudgetInfo(int budgetId, TimePeriod period) {
            return calculateBudget(toInfoVo(getBudget(budgetId), period), period);
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
	
	protected int getBookedMinutes(int budgetId, long from, long to) {
		Long s = (Long)em.createNamedQuery("Budget.getBookedMinutesInPeriod").setParameter("budgetId", budgetId).
                        setParameter("from", new java.sql.Date(from), TemporalType.DATE).
                        setParameter("to", new java.sql.Date(to), TemporalType.DATE).getSingleResult(); 
		return s != null ? s.intValue() : 0;
	}
	
	protected BudgetInfoVo toInfoVo(Budget budget, TimePeriod period) {
		BudgetInfoVo vo = new BudgetInfoVo(budget, period!=null ? 
                        getBookedMinutes(budget.getId(), period.getFrom(), period.getTo()) :
                        getBookedMinutes(budget.getId()));
                // set the number of associated templates:
                vo.setNumberOfTemplates(bookingsEjb.getBookingTemplatesByBudgetId(budget.getId()).size());
                return vo;
	}

	protected List<BudgetInfoVo> toInfoVos(List<Budget> budgets) {
                return toInfoVos(budgets, null);
	}

	protected List<BudgetInfoVo> toInfoVos(List<Budget> budgets, TimePeriod period) {
		List<BudgetInfoVo> result = new ArrayList<BudgetInfoVo>(budgets.size());
		for (Budget b : budgets) result.add(toInfoVo(b, period));
		return result;
	}

	protected List<BudgetInfoVo> calculateBudgets(List<BudgetInfoVo> budgets, TimePeriod period) {
		for (BudgetInfoVo b : budgets) {
			calculateBudget(b, period);
		}
		return budgets;
	}
	
	protected BudgetInfoVo calculateBudget(BudgetInfoVo budget, TimePeriod period) {
		List<BudgetInfoVo> childBudgets = getBudgetInfosForParent(budget.getBudget().getProjectId(), budget.getBudget().getId(), period);
		if (childBudgets.isEmpty()) {
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
			BudgetInfoVo child = calculateBudget(b, period); 
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
        
        @Override
        public int getBudgetPrognosisOffset(BudgetInfoVo budget) {
            if (budget.getBudget().getWorkProgress() == null ||
                budget.getBudget().getWorkProgress() == 0) return 0;
            int prognosisMinutes = budget.getBookedMinutesRecursive() * 100 / budget.getBudget().getWorkProgress();
            return prognosisMinutes - Math.abs(budget.getBudget().getMinutes());
        }
}
