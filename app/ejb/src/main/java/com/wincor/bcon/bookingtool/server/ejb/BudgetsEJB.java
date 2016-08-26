package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
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
public class BudgetsEJB {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;
        
        @EJB
        private BookingTemplatesEJB bookingsEjb;
	
	/**
	 * Returns the list of all budgets found in the "budget" table
	 * for the given project
	 * @param projectId a project ID
	 * @return list of budgets
	 */
	@RolesAllowed({"admin","user"})
	public List<Budget> getBudgets(int projectId) {
		return em.createNamedQuery("Budget.findByProjectId", Budget.class).setParameter("projectId", projectId).getResultList();
	}
        
	/**
	 * Returns the list of budgets found for the given parent budget ID.
	 * 
	 * @param parentId a budget ID
	 * @return list of budgets
	 */
	@RolesAllowed({"admin","user"})
        public List<Budget> getBudgetsForParent(int parentId) {
        	return em.createNamedQuery("Budget.findByParentId", Budget.class).setParameter("parentId", parentId).getResultList();
        }

	/**
	 * Returns the list of budgets found for the given parent budget ID
         * in a recursive manner.
	 * 
	 * @param parentId a budget ID
	 * @return list of budgets
	 */
	@RolesAllowed({"admin","user"})
        public List<Budget> getBudgetsForParentRecursive(int parentId) {
            List<Budget> result = new ArrayList<Budget>();
            iterateChildBudgets(result, getBudget(parentId), false);
            return result;
        }

	/**
	 * Returns the list of all leaf budgets (recursively) found for the given
         * project
	 * 
	 * @param projectId a project ID
	 * @return list of leaf budgets (recursively)
	 */
	@RolesAllowed({"admin","user"})
        public List<Budget> getLeafBudgets(int projectId) {
            List<Budget> result = new ArrayList<Budget>();
            for (Budget budget : em.createNamedQuery("Budget.findRoots", Budget.class).setParameter("projectId", projectId).getResultList())
                iterateChildBudgets(result, budget, true);
            return result;
        }
        
	/**
	 * Returns the list of leaf budgets (recursively) found for the given
         * parent budget ID.
	 * 
	 * @param parentId a budget ID
	 * @return list of leaf budgets (recursively)
	 */
	@RolesAllowed({"admin","user"})
        public List<Budget> getLeafBudgetsForParent(int parentId) {
            List<Budget> result = new ArrayList<Budget>();
            iterateChildBudgets(result, getBudget(parentId), true);
            return result;
        }
        
        protected void iterateChildBudgets(List<Budget> collecting, Budget currentBudget, boolean collectLeavesOnly) {
            List<Budget> children = getBudgetsForParent(currentBudget.getId());
            if (children.isEmpty()) {
                // leaf budget:
                collecting.add(currentBudget);
            } else {
                if (!collectLeavesOnly) collecting.add(currentBudget);
                // iterate children:
                for (Budget c : children) {
                    iterateChildBudgets(collecting, c, collectLeavesOnly);
                }
            }
        }

	/**
	 * Returns the list of all budgets found in the "budget" table
	 * for the given project
	 * @param projectId a project ID
	 * @return list of budgets
	 */
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getBudgetInfos(int projectId) {
            return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findByProjectId", Budget.class).setParameter("projectId", projectId).getResultList()), null);
	}

	/**
	 * Returns the list of budgets found for the given parent budget ID.
	 * If null is specified for the parentId, the list of root budgets
	 * without a parent ID is returned.
	 * 
	 * @param projectId a project ID
	 * @param parentId a budget ID or null to retrieve root budgets
	 * @return list of budgets
	 */
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getBudgetInfosForParent(int projectId, Integer parentId) {
            return getBudgetInfosForParent(projectId, parentId, null);
	}

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
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getBudgetInfosForParent(int projectId, Integer parentId, TimePeriod period) {
            if (parentId == null)
                return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findRoots", Budget.class).setParameter("projectId", projectId).getResultList(), period), period);
            else
                return calculateBudgets(toInfoVos(em.createNamedQuery("Budget.findByParentId", Budget.class).setParameter("parentId", parentId).getResultList(), period), period);
	}

	/**
	 * Returns the list of all leaf budgets found in the "budget" table
	 * for the given project
	 * @param projectId a project ID
	 * @return list of leaf budgets
	 */
	@RolesAllowed({"admin","user"})
	public List<BudgetInfoVo> getLeafBudgetInfos(int projectId) {
            return calculateBudgets(toInfoVos(getLeafBudgets(projectId)), null);
	}

	/**
	 * Returns the budget with the given ID
	 * @param budgetId a budget ID
	 * @return the budget
	 */
	@RolesAllowed({"admin","user"})
	public Budget getBudget(int budgetId) {
		return em.find(Budget.class, budgetId);
	}

	/**
	 * Returns the budget info value object for the given budget ID
	 * @param budgetId a budget ID
	 * @return the budget info value object
	 */
	@RolesAllowed({"admin","user"})
	public BudgetInfoVo getBudgetInfo(int budgetId) {
            return getBudgetInfo(budgetId, null);
        }
        
	/**
	 * Returns the budget info value object for the given budget ID
	 * @param budgetId a budget ID
         * @param period  a time period for the sum of booking minutes returned in the info value object
	 * @return the budget info value object
	 */
	@RolesAllowed({"admin","user"})
	public BudgetInfoVo getBudgetInfo(int budgetId, TimePeriod period) {
            return calculateBudget(toInfoVo(getBudget(budgetId), period), period);
        }
        
	/**
	 * Saves or updates the given budget
	 * @param budget a budget
	 */
	@RolesAllowed("admin")
	public void saveBudget(Budget budget) {
		if (budget.getId() != null)
			em.merge(budget);  // update the project
		else
			em.persist(budget);  // insert a new project
	}

	/**
	 * Removes the budget with the given budget ID
	 * @param budgetId a budget ID
	 */
	@RolesAllowed("admin")
	public void deleteBudget(int budgetId) {
		em.remove(getBudget(budgetId));
		
	}
	
        /**
         * Moves the given budget to the given target project. The budget itself
         * and all its recursive children will be moved to the given target project.
         * The parent budget of the given budget is set to null so that the new
         * budget tree can be found at the root position of the target project.
         * 
         * @param budgetId
         * @param targetProjectId 
         */
	@RolesAllowed("admin")
        public void moveBudget(int budgetId, int targetProjectId) {
            Budget budget = em.find(Budget.class, budgetId);
            moveBudgetImpl(budget, targetProjectId);
            // also set parent to null of the moved root budget
            budget.setParentId(null);
            em.merge(budget); // save
        }
        
        protected void moveBudgetImpl(Budget budget, int targetProjectId) {
            budget.setProjectId(targetProjectId);
            em.merge(budget); // save
            // move children, too:
            for (Budget child : getBudgetsForParent(budget.getId()))
                moveBudgetImpl(child, targetProjectId);
        }

        /**
         * Clones the given budget into the given target project. The budget itself
         * and all its recursive children will be cloned to the given target project.
         * The parent budget of the given budget is set to null so that the new
         * budget tree can be found at the root position of the target project.
         * All associated bookings templates are also cloned, replacing the
         * occurrences of default PSP and PSP template. Bookings are not cloned.
         * 
         * @param budgetId
         * @param targetProjectId 
         */
	@RolesAllowed("admin")
        public void cloneBudget(int budgetId, int targetProjectId) {
            Budget budget = em.find(Budget.class, budgetId);
            Project sourceProject = em.find(Project.class, budget.getProjectId());
            Project targetProject = em.find(Project.class, targetProjectId);
            cloneBudgetImpl(budget, null, sourceProject, targetProject);
        }
        
        protected void cloneBudgetImpl(Budget budget, Integer newParentBudgetId, Project sourceProject, Project targetProject) {
            Budget clone = new Budget(budget);
            clone.setProjectId(targetProject.getId());
            clone.setParentId(newParentBudgetId);
            em.persist(clone);
            em.flush(); // fetches new ID
            // clone templates:
            for (BookingTemplate t : bookingsEjb.getBookingTemplatesByBudgetId(budget.getId())) {
                BookingTemplate tclone = new BookingTemplate(t);
                tclone.setBudgetId(clone.getId());
                tclone.setName(tclone.getName().replace(sourceProject.getName(), targetProject.getName()));
                tclone.setPsp(tclone.getPsp().replace(sourceProject.getPsp(), targetProject.getPsp()));
                bookingsEjb.saveBookingTemplate(tclone); // updates searchstring
            }
            // clone children, too:
            for (Budget child : getBudgetsForParent(budget.getId()))
                cloneBudgetImpl(child, clone.getId(), sourceProject, targetProject);
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
		List<Budget> childBudgets = getBudgetsForParent(budget.getBudget().getId());
		if (childBudgets.isEmpty()) {
			// leaf budget
			//change prefix if budget was a root in its previous life
			if(budget.getBudget().getMinutes() < 0)
				budget.getBudget().setMinutes(-budget.getBudget().getMinutes());
			budget.setBookedMinutesRecursive(budget.getBookedMinutes());
			return budget;
		}
		
		int sum = 0;
		int sumBookedRecursive = budget.getBookedMinutes();
		for (Budget b : childBudgets) {
			BudgetInfoVo child = calculateBudget(toInfoVo(b, period), period); 
			sum += Math.abs(child.getBudget().getMinutes());
			sumBookedRecursive += child.getBookedMinutesRecursive();
		}
		budget.getBudget().setMinutes(-sum); // negative means 'calculated'
		budget.setBookedMinutesRecursive(sumBookedRecursive);
		return budget;
	}
        
        /**
         * Returns true if the given budget is either the same as or a recursive
         * descendant of the given parent budget.
         * @param parentBudgetId a parent budget ID
         * @param budgetId a budget ID
         * @return true or false
         */
        public boolean isDescendantOf(int parentBudgetId, int budgetId) {
            if (budgetId == parentBudgetId) return true;
            for (Budget b : getBudgetsForParent(parentBudgetId)) {
                if (isDescendantOf(b.getId(), budgetId)) return true;
            }
            return false;
        }
        
        /**
         * Returns the full path name for the given budget ID
         * @param budgetId a budget ID
         * @return full budget path name
         */
        public String getFullBudgetName(int budgetId) {
            Budget b = em.find(Budget.class, budgetId);
            StringBuilder stb = new StringBuilder(b.getName());
            while (b.getParentId() != null) {
                    b = em.find(Budget.class, b.getParentId());
                    stb.insert(0, b.getName() + " \u25B6 ");
            }
            return stb.toString();
	}
        
        /**
         * Calculates the budget prognosis and returns the offset to the
         * budget amount.
         * @param budget a budget
         * @return the budget prognosis offset
         */
        public int getBudgetPrognosisOffset(BudgetInfoVo budget) {
            if (budget.getBudget().getWorkProgress() == null ||
                budget.getBudget().getWorkProgress() == 0) return 0;
            int prognosisMinutes = budget.getBookedMinutesRecursive() * 100 / budget.getBudget().getWorkProgress();
            return prognosisMinutes - Math.abs(budget.getBudget().getMinutes());
        }
}
