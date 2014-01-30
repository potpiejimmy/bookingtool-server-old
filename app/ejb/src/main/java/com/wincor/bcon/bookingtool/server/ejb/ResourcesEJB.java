/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.ResourcePlanItem;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;

/**
 * Implementation of the resources EJB.
 */
@Stateless
public class ResourcesEJB implements ResourcesEJBLocal {

    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @Override
    @RolesAllowed({"admin","user"})
    public void savePersonalPlan(String person, TimePeriod timePeriod, List<ResourcePlanItem> items) {
        // first, remove all existing items for the affected time period:
        em.createNamedQuery("ResourcePlanItem.deleteByUserAndDateRange").
                setParameter("userName", person).
                setParameter("from", new java.sql.Date(timePeriod.getFrom()), TemporalType.DATE).
                setParameter("to", new java.sql.Date(timePeriod.getTo()), TemporalType.DATE).
                executeUpdate();
        
        for (ResourcePlanItem item : items) {
            item.setUserName(person);
            em.persist(item);
        }
    }

    @Override
    @RolesAllowed({"admin","user"})
    public List<ResourcePlanItem> getPersonalPlan(String person, TimePeriod timePeriod) {
        return em.createNamedQuery("ResourcePlanItem.findByUserAndDateRange", ResourcePlanItem.class).
                setParameter("userName", person).
                setParameter("from", new java.sql.Date(timePeriod.getFrom()), TemporalType.DATE).
                setParameter("to", new java.sql.Date(timePeriod.getTo()), TemporalType.DATE).
                getResultList();
        
    }
}
