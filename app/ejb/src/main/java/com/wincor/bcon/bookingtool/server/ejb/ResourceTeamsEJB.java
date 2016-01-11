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

import com.wincor.bcon.bookingtool.server.db.entity.ResourceTeam;
import com.wincor.bcon.bookingtool.server.db.entity.ResourceTeamMember;

/**
 * ResourceTeams EJB
 */
@Stateless
public class ResourceTeamsEJB {
    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @Resource
    private SessionContext ctx;
	
    /**
     * Returns the list of resource teams. For a superuser role, a complete list
     * of all teams is returned. For an admin or user role, only those
     * teams are returned that belong the user's visible domains.
     * @return list of teams
     */
    @RolesAllowed({"superuser","admin","user"})
    public List<ResourceTeam> getResourceTeams() {
        if (ctx.isCallerInRole("superuser"))
            return em.createNamedQuery("ResourceTeam.findAll", ResourceTeam.class).getResultList();
        else {
            return em.createNamedQuery("ResourceTeam.findByDomainUser", ResourceTeam.class).setParameter("userName", ctx.getCallerPrincipal().getName()).getResultList();
        }
    }
    
    /**
     * Returns the list of managed resource teams. Only those teams are returned
     * that the current user is the manager of.
     * @return list of managed teams
     */
    @RolesAllowed({"superuser","admin","user"})
    public List<ResourceTeam> getManagedResourceTeams() {
        return em.createNamedQuery("ResourceTeam.findByManager", ResourceTeam.class).setParameter("manager", ctx.getCallerPrincipal().getName()).getResultList();
    }
    
    /**
     * Get the team for the given ID
     * @param teamId a team ID
     * @return resource team
     */
    @RolesAllowed({"superuser","admin","user"})
    public ResourceTeam getResourceTeam(int teamId) {
        return em.find(ResourceTeam.class, teamId);
    }

    /**
     * Saves a team.
     * @param team a resource team
     * @param assignedUsers list of assigned user names
     * @return the saved team (holds new ID if inserted)
     */
    @RolesAllowed({"superuser","admin"})
    public ResourceTeam saveResourceTeam(ResourceTeam team, List<String> assignedUsers) {
        if (!ctx.isCallerInRole("superuser"))
            throw new IllegalArgumentException("Sorry, only superuser is allowed to modify teams.");
        
        if (team.getId() == null) {
            em.persist(team);
            em.flush();
        } else {
            em.merge(team);
            // delete existing assignments
            for (ResourceTeamMember user : em.createNamedQuery("ResourceTeamMember.findByResourceTeamId", ResourceTeamMember.class).setParameter("resourceTeamId", team.getId()).getResultList())
                em.remove(user);
        }
        
        // save user assignments:
        for (String user : assignedUsers) {
            ResourceTeamMember du = new ResourceTeamMember();
            du.setResourceTeamId(team.getId());
            du.setUserName(user);
            em.persist(du);
        }
        
        return team;
    }
    
    /**
     * Deletes the given team
     * @param teamId a resource team ID
     */
    @RolesAllowed({"superuser"})
    public void deleteResourceTeam(int teamId) {
        // delete existing user assignments
        em.createNamedQuery("ResourceTeamMember.deleteByResourceTeamId").setParameter("resourceTeamId", teamId).executeUpdate();
        em.remove(em.find(ResourceTeam.class, teamId));
    }
   
    /**
     * Returns the list of all assigned user names for the given team
     * @param teamId a team ID
     * @return list of user names
     */
    @RolesAllowed({"admin"})
    public List<String> getAssignedUsers(int teamId) {
        List<ResourceTeamMember> users = em.createNamedQuery("ResourceTeamMember.findByResourceTeamId", ResourceTeamMember.class).setParameter("resourceTeamId", teamId).getResultList();
        List<String> result = new ArrayList<String>(users.size());
        for (ResourceTeamMember u : users) result.add(u.getUserName());
        return result;
    }
}
