/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.wincor.bcon.bookingtool.server.db.entity.Project;

/**
 * Projects administration interface
 */
@Local
public interface ProjectsEJBLocal {
    
    /**
     * Returns all projects that are visible to the current user
     * @return list of projects
     */
    public List<Project> getProjects();

    /**
     * Returns all projects the current user is a manager of.
     * @return list of projects assigned as project manager
     */
    public List<Project> getManagedProjects();

    /**
     * Returns the project with the given ID
     * @param projectId a project ID
     * @return the project
     */
    public Project getProject(int projectId);

    /**
     * Saves or updates the given project
     * @param project a project
     * @param assignedManagers assigned manager user names
     * @return inserted or updated project
     */
    public Project saveProject(Project project, List<String> assignedManagers);

    /**
     * Removes the project with the given project ID
     * @param projectId a project ID
     */
    public void deleteProject(int projectId);
   
    /**
     * Returns the list of all assigned project managers for the given project
     * @param projectId a project ID
     * @return list of user names
     */
    public List<String> getAssignedManagers(int projectId);
}
