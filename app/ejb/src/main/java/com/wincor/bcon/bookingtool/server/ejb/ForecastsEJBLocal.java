/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.Forecast;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoRowVo;
import java.util.List;

/**
 * Forecast enterprise java bean
 */
public interface ForecastsEJBLocal {
    
    /**
     * Returns all forecasts
     * @return list of forecasts
     */
    public List<Forecast> getForecasts();
    
    /**
     * Inserts or updates the given forecast
     * @param forecast a forecast
     * @param assignedPlans assigned budget plans
     * @return the forecast holding new ID if inserted
     */
    public Forecast saveForecast(Forecast forecast, List<BudgetPlan> assignedPlans);
    
    /**
     * Deletes a forecast
     * @param forecastId a forecast ID
     */
    public void deleteForecast(int forecastId);
    
    /**
     * Returns the assigned budget plans for the given forecast ID
     * @param forecastId a forecast ID
     * @return list of budget plans
     */
    public List<BudgetPlan> getAssignedBudgetPlans(int forecastId);
    
    /**
     * Returns the forecast info row for the given budget ID
     * @param forecastId a forecast ID
     * @param budgetId a budget ID
     * @return forecast info
     */
    public ForecastInfoRowVo getForecastInfoForBudget(int forecastId, int budgetId);
    
    /**
     * Returns the forecast info value objects for all children of the given
     * parent budget.
     * @param forecastId a forecast ID
     * @param parentBudgetId a budget ID
     * @return list of forecast info value objects
     */
    public List<ForecastInfoRowVo> getForecastInfosForParentBudget(int forecastId, int parentBudgetId);
}
