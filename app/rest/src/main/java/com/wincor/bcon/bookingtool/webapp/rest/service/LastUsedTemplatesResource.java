/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wincor.bcon.bookingtool.webapp.rest.service;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.ejb.BookingTemplatesEJB;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author thorsten.liese
 */
@Path("/lastused")
@RequestScoped
public class LastUsedTemplatesResource {
    
    @EJB
    private BookingTemplatesEJB templatesEjb;

    @Produces("application/json")
    @GET
    public List<BookingTemplate> getLastUsed(@Context HttpServletRequest hsr, @QueryParam("size")Integer size) {
        return templatesEjb.getLastUsedByPerson(hsr.getUserPrincipal().getName(), size != null ? size : 5);
    }
}
