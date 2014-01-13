/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.db.entity.ProjectManager;
import com.wincor.bcon.bookingtool.server.db.entity.User;
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
public class ProjectsEJB implements ProjectsEJBLocal {
    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @Resource
    private SessionContext ctx;
	
    @EJB
    private DomainsEJBLocal domainsEjb;
    
    @Override
    @RolesAllowed({"admin","user"})
    public List<Project> getProjects() {
        List<Project> result = new ArrayList<Project>();
        for (Domain domain : domainsEjb.getDomains()) {
            result.addAll(em.createNamedQuery("Project.findByDomainId", Project.class).setParameter("domainId", domain.getId()).getResultList());
        }
        return result;
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
    @RolesAllowed({"superuser","admin"})
    public Project saveProject(Project project, List<String> assignedManagers) {
        if (project.getId() == null) {
            em.persist(project);
            em.flush();
        } else {
            em.merge(project);
            // delete existing assignments
            em.createNamedQuery("ProjectManager.deleteByProjectId").setParameter("projectId", project.getId()).executeUpdate();
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
    @RolesAllowed({"superuser"})
    public void deleteProject(int projectId) {
        // delete existing user assignments
        em.createNamedQuery("ProjectManager.deleteByProjectId").setParameter("projectId", projectId).executeUpdate();
        em.remove(em.find(Project.class, projectId));
    }
   
    @Override
    @RolesAllowed({"admin"})
    public List<String> getAllUsers() {
        List<User> users = em.createNamedQuery("User.findAll", User.class).getResultList();
        List<String> result = new ArrayList<String>(users.size());
        for (User u : users) result.add(u.getName());
        return result;
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
