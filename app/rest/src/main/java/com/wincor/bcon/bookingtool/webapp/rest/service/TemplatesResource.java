/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wincor.bcon.bookingtool.webapp.rest.service;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.ejb.BookingTemplatesEJB;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 *
 * @author thorsten.liese
 */
@Path("/templates")
@RequestScoped
public class TemplatesResource {
    
    @EJB
    private BookingTemplatesEJB templatesEjb;

    @Produces("application/json")
    @GET
    public List<BookingTemplate> searchTemplate(@QueryParam("search")String searchString) {
        // make sure search string is at least three characters long
        if (searchString!=null && searchString.length()>2)
            return templatesEjb.findBookingTemplates(searchString);
        else
            return new ArrayList<BookingTemplate>();
    }
    
    @Path("{id}")
    @Produces("application/json")
    @GET
    public BookingTemplate getTemplate(@PathParam("id")Integer id) {
        return templatesEjb.getBookingTemplate(id);
    }
}
