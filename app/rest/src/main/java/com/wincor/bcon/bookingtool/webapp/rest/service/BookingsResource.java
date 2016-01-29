/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wincor.bcon.bookingtool.webapp.rest.service;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.ejb.BookingsEJB;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

/**
 *
 * @author thorsten.liese
 */
@Path("/bookings")
@RequestScoped
public class BookingsResource {
    
    @EJB
    private BookingsEJB bookingsEjb;
    
    @Consumes("application/json")
    @POST
    public void insertBooking(@Context HttpServletRequest hsr, Booking booking) {
        booking.setPerson(hsr.getUserPrincipal().getName());
        bookingsEjb.saveBooking(booking);
    }
    
    @Produces("application/json")
    @GET
    public List<Booking> getBookingsForToday(@Context HttpServletRequest hsr) {
        return bookingsEjb.getBookings(hsr.getUserPrincipal().getName(), new Date());
    }
}
