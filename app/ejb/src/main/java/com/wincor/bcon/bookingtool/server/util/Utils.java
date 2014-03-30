/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.util;

import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import java.util.Calendar;

/**
 * Static utility methods.
 */
public class Utils {
    
    /**
     * Creates and returns a TimePeriod object for the given month.
     * @param year a year, e.g. 2013
     * @param monthOfYear a month constant as defined by Calendar.JANUARY to Calendar.DECEMBER
     * @return TimePeriod object holding start and end timestamp
     */
    public static TimePeriod timePeriodForMonth(int year, int monthOfYear) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.YEAR, year);
        start.set(Calendar.MONTH, monthOfYear);
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(start.getTimeInMillis());
        end.add(Calendar.MONTH, 1);
        return new TimePeriod(start.getTimeInMillis(), end.getTimeInMillis());
    }
    
    /**
     * Returns the official label associated with the given booking type.
     * @param type booking type, such as 0W or NP
     * @param shorten return a shortened version of the label
     * @return type label
     */
    public static String labelForBookingType(String type, boolean shorten) {
        if ("0W".equals(type)) 
            return "Arbeitszeit";
        else if ("1T".equals(type)) 
            return "Reisezeit";
        else if ("NP".equals(type)) 
            return shorten ? "Nicht-Prod." : "nicht produktive Tätigkeiten";
        else return "";
    }
}
