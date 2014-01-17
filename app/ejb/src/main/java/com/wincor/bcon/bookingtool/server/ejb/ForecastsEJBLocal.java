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
import javax.ejb.Local;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Forecast enterprise java bean
 */
@Local
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
     * Returns the forecast info row sum values for all assigned budget plan budgets
     * @param forecastId a forecast ID
     * @return forecast info sum row
     */
    public ForecastInfoRowVo getForecastInfoSummaryRow(int forecastId);

    /**
     * Returns the months for the given fiscal year
     * @param fiscalYear a fiscal year
     * @return list of month periods
     */
    public List<Integer> getMonthsForFiscalYear(int fiscalYear);
    
    /**
     * Exports detailed plan data to excel
     * @param forecastId a forecast ID
     * @return plan data export
     */
    public HSSFWorkbook exportPlanData(int forecastId);

    /**
     * Create a special sales report sheet for the given forecast
     * @param forecastId a forecast ID
     * @return sales report sheet
     */
    public HSSFWorkbook createSalesReport(int forecastId);

}
