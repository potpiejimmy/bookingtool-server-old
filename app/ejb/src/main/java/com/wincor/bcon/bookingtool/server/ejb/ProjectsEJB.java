/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.db.entity.ProjectManager;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Projects EJB
 */
@Stateless
public class ProjectsEJB {
    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @Resource
    private SessionContext ctx;
    
    @EJB
    private BudgetsEJB budgetsEjb;
	
    /**
     * Returns all projects that are visible to the current user. This
     * includes all projects in the user's domains that are active.
     * @return list of projects
     */
    @RolesAllowed({"admin","user"})
    public List<Project> getProjects() {
        if (ctx.isCallerInRole("superuser"))
            return em.createNamedQuery("Project.findAll", Project.class).getResultList();
        else {
            return em.createNamedQuery("Project.findByDomainUserActive", Project.class).setParameter("userName", ctx.getCallerPrincipal().getName()).getResultList();
        }
    }
    
    /**
     * Returns all projects that are visible to the current administration user.
     * This includes all projects in the user's domains, including all statuses.
     * @return list of projects
     */
    @RolesAllowed("admin")
    public List<Project> getProjectsForAdmin() {
        if (ctx.isCallerInRole("superuser"))
            return em.createNamedQuery("Project.findAll", Project.class).getResultList();
        else {
            return em.createNamedQuery("Project.findByDomainUser", Project.class).setParameter("userName", ctx.getCallerPrincipal().getName()).getResultList();
        }
    }

    /**
     * Returns all projects the current user is a manager of.
     * @return list of projects assigned as project manager
     */
    @RolesAllowed({"admin","user"})
    public List<Project> getManagedProjects() {
        if (ctx.isCallerInRole("superuser"))
            return em.createNamedQuery("Project.findAll", Project.class).getResultList();
        else {
            List<ProjectManager> managers = em.createNamedQuery("ProjectManager.findByUserName", ProjectManager.class).setParameter("userName", ctx.getCallerPrincipal().getName()).getResultList();
            List<Project> result = new ArrayList<Project>(managers.size());
            for (ProjectManager m : managers) result.add(em.find(Project.class, m.getProjectId()));
            return result;
        }
    }
    
    /**
     * Returns the project with the given ID
     * @param projectId a project ID
     * @return the project
     */
    @RolesAllowed({"admin","user"})
    public Project getProject(int projectId) {
            return em.find(Project.class, projectId);
    }

    /**
     * Saves or updates the given project
     * @param project a project
     * @param assignedManagers assigned manager user names
     * @return inserted or updated project
     */
    @RolesAllowed({"admin"})
    public Project saveProject(Project project, List<String> assignedManagers) {
        if (!ctx.isCallerInRole("superuser") &&
            !assignedManagers.contains(ctx.getCallerPrincipal().getName()))
            assignedManagers.add(ctx.getCallerPrincipal().getName()); // must at least have myself as manager for non superuser
        
        if (project.getId() == null) {
            em.persist(project);
            em.flush();
        } else {
            if (!ctx.isCallerInRole("superuser") &&
                !getAssignedManagers(project.getId()).contains(ctx.getCallerPrincipal().getName()))
                throw new IllegalArgumentException("Sorry, cannot modify a project that you are not managing.");
            
            em.merge(project);
            // delete existing assignments
            for (ProjectManager user : em.createNamedQuery("ProjectManager.findByProjectId", ProjectManager.class).setParameter("projectId", project.getId()).getResultList())
                em.remove(user);
        }
        
        // save manager assignments:
        for (String user : assignedManagers) {
            ProjectManager pm = new ProjectManager();
            pm.setProjectId(project.getId());
            pm.setUserName(user);
            em.persist(pm);
        }
        
        return project;
    }
    
    /**
     * Removes the project with the given project ID. This will fail if there
     * is data referencing this project.
     * @param projectId a project ID
     */
    @RolesAllowed({"admin"})
    public void deleteProject(int projectId) {
        // delete existing user assignments
        em.createNamedQuery("ProjectManager.deleteByProjectId").setParameter("projectId", projectId).executeUpdate();
        em.remove(em.find(Project.class, projectId));
    }
   
    /**
     * Drops the project with the given project ID. All data related to the
     * project will be erased as well. Use with caution.
     * @param projectId a project ID
     */
    @RolesAllowed("superuser")
    public void dropProject(int projectId) {
        /**
         * Erase all data associated with a project:
         * booking
         * booking_template
         * forecast_budget_plan
         * budget_plan_item
         * budget_plan
         * budget
         * resource_plan_item
         * project
         */
        // first, erase budget plans:
        List<BudgetPlan> budgetPlans = em.createNamedQuery("BudgetPlan.findByProjectId", BudgetPlan.class).setParameter("projectId", projectId).getResultList();
        for (BudgetPlan plan : budgetPlans) {
            em.createNamedQuery("ForecastBudgetPlan.deleteByBudgetPlanId").setParameter("budgetPlanId", plan.getId()).executeUpdate();
            em.remove(plan);
        }
        // erase bookings, templates and budgets and budget_plan_items
        List<Budget> budgets = budgetsEjb.getLeafBudgets(projectId);
        while (!budgets.isEmpty()) {
            for (Budget budget : budgets) {
                List<BookingTemplate> templates = em.createNamedQuery("BookingTemplate.findByBudgetId", BookingTemplate.class).setParameter("budgetId", budget.getId()).getResultList();
                for (BookingTemplate t : templates) {
                    for (Booking booking : em.createNamedQuery("Booking.findByTemplateId", Booking.class).setParameter("bookingTemplateId", t.getId()).getResultList()) {
                        em.remove(booking);
                    }
                    em.remove(t);
                }
                em.createNamedQuery("BudgetPlanItem.deleteByBudgetId").setParameter("budgetId", budget.getId()).executeUpdate();
                em.remove(budget);
            }
            budgets = budgetsEjb.getLeafBudgets(projectId);
        }
        // delete resource plan items:
        em.createNamedQuery("ResourcePlanItem.deleteByProjectId").setParameter("projectId", projectId).executeUpdate();
        // finally, delete project:
        deleteProject(projectId);
    }
   
    /**
     * Returns the list of all assigned project managers for the given project
     * @param projectId a project ID
     * @return list of user names
     */
    @RolesAllowed({"admin"})
    public List<String> getAssignedManagers(int projectId) {
        List<ProjectManager> managers = em.createNamedQuery("ProjectManager.findByProjectId", ProjectManager.class).setParameter("projectId", projectId).getResultList();
        List<String> result = new ArrayList<String>(managers.size());
        for (ProjectManager pm : managers) result.add(pm.getUserName());
        return result;
    }
}
