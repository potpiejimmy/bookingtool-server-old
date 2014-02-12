/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.ResourcePlanItem;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import java.util.List;
import javax.ejb.Local;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Resource EJB local interface
 */
@Local
public interface ResourcesEJBLocal {
    
    /**
     * Saves the given personal plan items for the given user and time period.
     * @param person a user name
     * @param timePeriod the affected time period (needed to clear all existing plan items in that period)
     * @param items the plan items to save
     */
    public void savePersonalPlan(String person, TimePeriod timePeriod, List<ResourcePlanItem> items);
    
    /**
     * Gets the personal plan items for the given person and time period.
     * @param person a user name
     * @param timePeriod a time period
     * @return list of plan items
     */
    public List<ResourcePlanItem> getPersonalPlan(String person, TimePeriod timePeriod);
    
    /**
     * Export the resource plan for the given team.
     * @param teamId a team ID
     * @param weeksToExport number of weeks to export
     * @return resource plan
     */
    public HSSFWorkbook exportResourcePlan(int teamId, int weeksToExport);
}
