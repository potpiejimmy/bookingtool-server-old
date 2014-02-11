/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.ResourceTeam;
import java.util.List;
import javax.ejb.Local;

/**
 * Domain administration interface
 */
@Local
public interface ResourceTeamsEJBLocal {
    
    /**
     * Returns the list of resource teams. For a superuser role, a complete list
     * of all teams is returned. For an admin or user role, only those
     * teams are returned that the current user is the manager of.
     * @return list of teams
     */
    public List<ResourceTeam> getResourceTeams();
    
    /**
     * Get the team for the given ID
     * @param teamId a team ID
     * @return resource team
     */
    public ResourceTeam getResourceTeam(int teamId);
    
    /**
     * Saves a team.
     * @param team a resource team
     * @param assignedUsers list of assigned user names
     * @return the saved team (holds new ID if inserted)
     */
    public ResourceTeam saveResourceTeam(ResourceTeam team, List<String> assignedUsers);

    /**
     * Deletes the given team
     * @param teamId a resource team ID
     */
    public void deleteResourceTeam(int teamId);
   
    /**
     * Returns the list of all assigned user names for the given team
     * @param teamId a team ID
     * @return list of user names
     */
    public List<String> getAssignedUsers(int teamId);
}
