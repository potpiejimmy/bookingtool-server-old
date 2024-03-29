/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import com.wincor.bcon.bookingtool.server.db.entity.DomainUser;
import com.wincor.bcon.bookingtool.server.db.entity.User;

/**
 * Domains EJB
 */
@Stateless
public class DomainsEJB {
    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @Resource
    private SessionContext ctx;
	
    /**
     * Returns the list of domains. For a superuser role, a complete list
     * of all domains is returned. For an admin or user role, only the list
     * of assigned domains (via DomainUser) is returned.
     * @return list of domains
     */
    @RolesAllowed({"superuser","admin","user"})
    public List<Domain> getDomains() {
        if (ctx.isCallerInRole("superuser"))
            return em.createNamedQuery("Domain.findAll", Domain.class).getResultList();
        else {
            List<DomainUser> domains = em.createNamedQuery("DomainUser.findByUserName", DomainUser.class).setParameter("userName", ctx.getCallerPrincipal().getName()).getResultList();
            List<Domain> result = new ArrayList<Domain>(domains.size());
            for (DomainUser d : domains) result.add(em.find(Domain.class, d.getDomainId()));
            return result;
        }
    }
    
    /**
     * Get the domain for the given ID
     * @param domainId a domain ID
     * @return domain
     */
    @RolesAllowed({"superuser","admin","user"})
    public Domain getDomain(int domainId) {
        return em.find(Domain.class, domainId);
    }

    /**
     * Saves a domain.
     * @param domain a domain
     * @param assignedUsers list of assigned user names
     * @return the saved domain (holds new ID if inserted)
     */
    @RolesAllowed({"superuser","admin"})
    public Domain saveDomain(Domain domain, List<String> assignedUsers) {
        if (domain.getId() == null) {
            if (!ctx.isCallerInRole("superuser"))
                throw new IllegalArgumentException("Sorry, only superuser may insert new domains.");
            em.persist(domain);
            em.flush();
        } else {
            em.merge(domain);
            // delete existing assignments
            for (DomainUser user : em.createNamedQuery("DomainUser.findByDomainId", DomainUser.class).setParameter("domainId", domain.getId()).getResultList())
                em.remove(user);
        }
        
        // save user assignments:
        for (String user : assignedUsers) {
            DomainUser du = new DomainUser();
            du.setDomainId(domain.getId());
            du.setUserName(user);
            em.persist(du);
        }
        
        return domain;
    }
    
    /**
     * Deletes the given domain
     * @param domainId a domain ID
     */
    @RolesAllowed({"superuser"})
    public void deleteDomain(int domainId) {
        // delete existing user assignments
        em.createNamedQuery("DomainUser.deleteByDomainId").setParameter("domainId", domainId).executeUpdate();
        em.remove(em.find(Domain.class, domainId));
    }
   
    /**
     * Returns the list of all available user names
     * @return list of user names
     */
    @RolesAllowed({"admin"})
    public List<String> getAllUsers() {
        List<User> users = em.createNamedQuery("User.findAll", User.class).getResultList();
        List<String> result = new ArrayList<String>(users.size());
        for (User u : users) result.add(u.getName());
        return result;
    }
    
    /**
     * Returns the list of all assigned user names for the given domain
     * @param domainId a domain ID
     * @return list of user names
     */
    @RolesAllowed({"admin"})
    public List<String> getAssignedUsers(int domainId) {
        List<DomainUser> users = em.createNamedQuery("DomainUser.findByDomainId", DomainUser.class).setParameter("domainId", domainId).getResultList();
        List<String> result = new ArrayList<String>(users.size());
        for (DomainUser u : users) result.add(u.getUserName());
        return result;
    }
    
    /**
     * Returns the list of all assigned user names for the given domain which have the role 'admin'
     * @param domainId a domain ID
     * @return list of user names
     */
    @RolesAllowed({"admin"})
    public List<String> getAssignedUsersWithAdminRole(int domainId) {
        List<DomainUser> users = em.createNamedQuery("DomainUser.findByDomainIdAndUserRole", DomainUser.class).setParameter("domainId", domainId).setParameter("userRole", "admin").getResultList();
        List<String> result = new ArrayList<String>(users.size());
        for (DomainUser u : users) result.add(u.getUserName());
        return result;
    }
}
