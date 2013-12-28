/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import java.util.List;
import javax.ejb.Local;

/**
 * Domain administration interface
 */
@Local
public interface DomainsEJBLocal {
    
    /**
     * Returns the list of domains. For a superuser role, a complete list
     * of all domains is returned. For an admin or user role, only the list
     * of assigned domains (via DomainUser) is returned.
     * @return list of domains
     */
    public List<Domain> getDomains();
    
    /**
     * Saves a domain.
     * @param domain a domain
     * @param assignedUsers list of assigned user names
     * @return the saved domain (holds new ID if inserted)
     */
    public Domain saveDomain(Domain domain, List<String> assignedUsers);

    /**
     * Deletes the given domain
     * @param domainId a domain ID
     */
    public void deleteDomain(int domainId);
   
    /**
     * Returns the list of all available user names
     * @return list of user names
     */
    public List<String> getAllUsers();
    
    /**
     * Returns the list of all assigned user names for the given domain
     * @param domainId a domain ID
     * @return list of user names
     */
    public List<String> getAssignedUsers(int domainId);
}
