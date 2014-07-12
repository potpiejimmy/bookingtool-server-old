/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.db.entity.ProjectManager;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Projects EJB
 */
@Stateless
public class ProjectsEJB implements ProjectsEJBLocal {
    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @Resource
    private SessionContext ctx;
	
    @Override
    @RolesAllowed({"admin","user"})
    public List<Project> getProjects() {
        if (ctx.isCallerInRole("superuser"))
            return em.createNamedQuery("Project.findAll", Project.class).getResultList();
        else {
            return em.createNamedQuery("Project.findByDomainUserActive", Project.class).setParameter("userName", ctx.getCallerPrincipal().getName()).getResultList();
        }
    }
    
    @Override
    @RolesAllowed("admin")
    public List<Project> getProjectsForAdmin() {
        if (ctx.isCallerInRole("superuser"))
            return em.createNamedQuery("Project.findAll", Project.class).getResultList();
        else {
            return em.createNamedQuery("Project.findByDomainUser", Project.class).setParameter("userName", ctx.getCallerPrincipal().getName()).getResultList();
        }
    }

    @Override
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
    
    @Override
    @RolesAllowed({"admin","user"})
    public Project getProject(int projectId) {
            return em.find(Project.class, projectId);
    }

    @Override
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
    
    @Override
    @RolesAllowed({"admin"})
    public void deleteProject(int projectId) {
        // delete existing user assignments
        em.createNamedQuery("ProjectManager.deleteByProjectId").setParameter("projectId", projectId).executeUpdate();
        em.remove(em.find(Project.class, projectId));
    }
   
    @Override
    @RolesAllowed("superuser")
    public void dropProject(int projectId) {
        // XXX TODO to be implemented
        // Erase all budgets, templates, bookings and budget plans.
        deleteProject(projectId);
    }
   
    @Override
    @RolesAllowed({"admin"})
    public List<String> getAssignedManagers(int projectId) {
        List<ProjectManager> managers = em.createNamedQuery("ProjectManager.findByProjectId", ProjectManager.class).setParameter("projectId", projectId).getResultList();
        List<String> result = new ArrayList<String>(managers.size());
        for (ProjectManager pm : managers) result.add(pm.getUserName());
        return result;
    }
}
