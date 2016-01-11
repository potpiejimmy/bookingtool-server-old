/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.ResourcePlanItem;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Implementation of the resources EJB.
 */
@Stateless
public class ResourcesEJB {

    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @EJB
    private ResourceTeamsEJB resourceTeamsEjb;
    
    /**
     * Saves the given personal plan items for the given user and time period.
     * @param person a user name
     * @param timePeriod the affected time period (needed to clear all existing plan items in that period)
     * @param items the plan items to save
     */
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

    /**
     * Gets the personal plan items for the given person and time period.
     * @param person a user name
     * @param timePeriod a time period
     * @return list of plan items
     */
    @RolesAllowed({"admin","user"})
    public List<ResourcePlanItem> getPersonalPlan(String person, TimePeriod timePeriod) {
        return em.createNamedQuery("ResourcePlanItem.findByUserAndDateRange", ResourcePlanItem.class).
                setParameter("userName", person).
                setParameter("from", new java.sql.Date(timePeriod.getFrom()), TemporalType.DATE).
                setParameter("to", new java.sql.Date(timePeriod.getTo()), TemporalType.DATE).
                getResultList();
        
    }
    
    /**
     * Export the resource plan for the given team.
     * @param teamId a team ID
     * @param weeksToExport number of weeks to export
     * @return resource plan
     */
    @RolesAllowed({"admin","user"})
    public XSSFWorkbook exportResourcePlan(int teamId, int weeksToExport) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        
        final DateFormat MONTH_FORMATTER = new SimpleDateFormat("MMMMMMMM");
        final DateFormat DAY_FORMATTER = new SimpleDateFormat("dd");
        final DateFormat WEEKDAY_FORMATTER = new SimpleDateFormat("EE");
        
        int daysToExport = 7 * weeksToExport;
        
        Calendar start = Calendar.getInstance(Locale.GERMANY);
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance(Locale.GERMANY);
        end.add(Calendar.DAY_OF_YEAR, daysToExport - 1);
        
        int rowIndex = 0;
        XSSFRow row = sheet.createRow(rowIndex++);
        createTimeLine(row, start, daysToExport, MONTH_FORMATTER, null, null);
        row = sheet.createRow(rowIndex++);
        createTimeLine(row, start, daysToExport, DAY_FORMATTER, null, null);
        row = sheet.createRow(rowIndex++);
        createTimeLine(row, start, daysToExport, WEEKDAY_FORMATTER, null, null);
        
        sheet.createRow(rowIndex++); // empty row
        
        for (String user : resourceTeamsEjb.getAssignedUsers(teamId)) {
            row = sheet.createRow(rowIndex++);
            createTimeLine(row, start, daysToExport, null, user, getPersonalPlan(user, new TimePeriod(start.getTimeInMillis(), end.getTimeInMillis())));
        }
        
        return wb;
    }
    
    protected void createTimeLine(XSSFRow row, Calendar start, int numberOfDays, DateFormat formatter, String user, List<ResourcePlanItem> items) {
        int colIndex = 0;
        XSSFCell cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(user != null ? user : ""));
        
        Calendar iter = Calendar.getInstance(Locale.GERMANY);
        iter.setTime(start.getTime());
        String lastFormatting = null;
        for (int i = 0; i < numberOfDays; i++) {
            cell = row.createCell(colIndex++);
            if (user == null) {
                // header lines:
                String formatting = formatter.format(iter.getTime());
                if (!formatting.equals(lastFormatting)) {
                    cell.setCellValue(new XSSFRichTextString(formatting));
                    lastFormatting = formatting;
                } else {
                    cell.setCellValue(new XSSFRichTextString(""));
                }
            } else {
                // user line:
                for (ResourcePlanItem item : items) {
                    Calendar itemDay = Calendar.getInstance(Locale.GERMANY);
                    itemDay.setTime(item.getDay());
                    itemDay.set(Calendar.HOUR_OF_DAY, 0);
                    itemDay.set(Calendar.MINUTE, 0);
                    itemDay.set(Calendar.SECOND, 0);
                    itemDay.set(Calendar.MILLISECOND, 0);
                    if (itemDay.equals(iter)) {
                        cell.setCellValue(new XSSFRichTextString("" + item.getAvail() + (item.getProjectId() != null ? String.valueOf(item.getProjectId()) : "")));
                    }
                }
            }
            iter.add(Calendar.DAY_OF_YEAR, 1);
        }
    }
}
