/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wincor.bcon.bookingtool.server.vo;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.util.Utils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Represents an exportable formatted booking for SAP.
 * This class holds hundredths parts of an hour instead of minutes for SAP export.
 */
public class SAPBooking {
    
    protected final static DateFormat DATEFORMATTER = new SimpleDateFormat("E., dd.MM.yyyy");
    
    // Datum
    public String day;
    // Person
    public String person;
    // PSP
    public String psp;
    // PSP-Name
    public String pspLabel;
    // Taetigkeitsart
    public String type;
    // Bezeichnung Taetigkeitsart
    public String typeLabel;
    // Taetigkeit
    public String description;
    // VB-Beauftragter
    public String salesRepresentative;
    // Teilprojekt
    public String subproject;
    // Hundertstel Stunden
    public long hundredthHours;
    
    /**
     * Creates a new SAP booking for the given booking and template
     * 
     * @param booking a booking
     * @param bt connected booking template
     */
    public SAPBooking(Booking booking, BookingTemplate bt) {
        this.day = DATEFORMATTER.format(booking.getDay());
        this.person = booking.getPerson();
        this.psp = bt.getPsp();
        this.pspLabel = bt.getName();
        this.type = bt.getType();
        this.typeLabel = Utils.labelForBookingType(bt.getType(), false);
        this.description = booking.getDescription();
        this.salesRepresentative = booking.getSalesRepresentative();
        this.subproject = bt.getSubproject();
        this.hundredthHours = roundedHundredthHoursForMinutes(booking.getMinutes().doubleValue());
    }
    
    public static long roundedHundredthHoursForMinutes(double minutes) {
        return Math.round(minutes/60*100);
    }
    
    public static SAPBooking[] createSAPBookingsForBooking(Booking booking, BookingTemplate bt) {
        // split the PSP elements and create separate bookings
        String[] psps = bt.getPsp().split(",");
        SAPBooking[] result = new SAPBooking[psps.length];
        
        if (psps.length == 1) {
            // speed optimization:
            result[0] = new SAPBooking(booking, bt);
            return result;
        }
        
        int[] gravities = new int[psps.length];
        int sumGravity = 0;
        for (int i=0; i<psps.length; i++) {
            psps[i] = psps[i].trim();
            int gravityPos = psps[i].indexOf("[");
            if (gravityPos >= 0) {
                try {gravities[i] = Integer.parseInt(psps[i].substring(gravityPos+1, psps[i].length()-1).trim());}
                catch (Exception e) {gravities[i] = 1;}
                psps[i] = psps[i].substring(0, gravityPos);
            } else {
                gravities[i] = 1;
            }
            sumGravity += gravities[i];
        }
        
        long timeSum = roundedHundredthHoursForMinutes(booking.getMinutes().doubleValue());
        long timeIter = 0;
        
        for (int i=0; i<psps.length; i++) {
            SAPBooking sapBooking = new SAPBooking(booking, bt);
            if (i == psps.length -1) {
                // if this is the last split booking, use the unrounded rest of
                // the time so the sum is always equal to the original booked time
                sapBooking.hundredthHours = (timeSum - timeIter);
            } else {
                sapBooking.hundredthHours = roundedHundredthHoursForMinutes(booking.getMinutes().doubleValue()*gravities[i]/sumGravity);
                timeIter += sapBooking.hundredthHours;
            }
            sapBooking.psp = psps[i];
            result[i] = sapBooking;
        }
        return result;
    }
}
