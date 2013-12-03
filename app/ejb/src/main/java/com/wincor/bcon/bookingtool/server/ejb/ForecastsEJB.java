/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.Forecast;
import com.wincor.bcon.bookingtool.server.db.entity.ForecastBudgetPlan;
import com.wincor.bcon.bookingtool.server.util.Utils;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoRowVo;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoVo;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class ForecastsEJB implements ForecastsEJBLocal {

    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @EJB
    private BudgetsEJBLocal budgetsEjb;
    
    @EJB
    private BudgetPlansEJBLocal budgetPlansEjb;
    
    @Override
    public List<Forecast> getForecasts() {
        return em.createNamedQuery("Forecast.findAll", Forecast.class).getResultList();
    }

    @Override
    public Forecast saveForecast(Forecast forecast, List<BudgetPlan> assignedBudgetPlans) {
        if (forecast.getId() == null) {
            em.persist(forecast);
            em.flush();
        } else {
            em.merge(forecast);
            // delete existing assignments
            em.createNamedQuery("ForecastBudgetPlan.deleteByForecastId").setParameter("forecastId", forecast.getId()).executeUpdate();
        }
        
        // save budget plan assignments:
        int index = 0;
        for (BudgetPlan b : assignedBudgetPlans) {
            ForecastBudgetPlan p = new ForecastBudgetPlan();
            p.setForecastId(forecast.getId());
            p.setBudgetPlanId(b.getId());
            p.setPosition(index++);
            em.persist(p);
        }
        
        return forecast;
    }

    @Override
    public void deleteForecast(int forecastId) {
        // delete existing assignments
        em.createNamedQuery("ForecastBudgetPlan.deleteByForecastId").setParameter("forecastId", forecastId).executeUpdate();
        em.remove(em.find(Forecast.class, forecastId));
    }
   
    @Override
    public List<BudgetPlan> getAssignedBudgetPlans(int forecastId) {
        List<ForecastBudgetPlan> assigned = em.createNamedQuery("ForecastBudgetPlan.findByForecastId", ForecastBudgetPlan.class).setParameter("forecastId", forecastId).getResultList();
        List<BudgetPlan> result = new ArrayList<BudgetPlan>(assigned.size());
        for (ForecastBudgetPlan p : assigned) result.add(em.find(BudgetPlan.class, p.getBudgetPlanId()));
        return result;
    }

    @Override
    public List<Integer> getMonthsForFiscalYear(int fiscalYear) {
        List<Integer> result = new ArrayList<Integer>(12);
        for (int month = 0; month < 12; month++) {
            int year = month < 3 ? fiscalYear - 1 : fiscalYear;
            int monthOfYear = month < 3 ? month + 9 : month - 3;
            int period = year * 100 + (monthOfYear+1);
            result.add(period);
        }
        return result;
    }
    
    @Override
    public ForecastInfoRowVo getForecastInfoForBudget(int forecastId, int budgetId) {
        Forecast forecast = em.find(Forecast.class, forecastId);
        ForecastInfoRowVo row = new ForecastInfoRowVo(budgetsEjb.getBudgetInfo(budgetId));
        for (int period : getMonthsForFiscalYear(forecast.getFiscalYear())) {
            ForecastInfoVo cell = new ForecastInfoVo();
            int year = period / 100;
            int monthOfYear = (period % 100) - 1;
            TimePeriod timePeriod = Utils.timePeriodForMonth(year, monthOfYear);
            cell.setPeriod(period);
            cell.setPlannedMinutes(budgetPlansEjb.getPlannedMinutesForPeriod(budgetId, period));
            cell.setBookedMinutes(budgetsEjb.getBudgetInfo(budgetId, timePeriod).getBookedMinutesRecursive());
            row.getMonths().put(period, cell);
        }
        return row;
    }

    @Override
    public List<ForecastInfoRowVo> getForecastInfosForParentBudget(int forecastId, int parentBudgetId) {
        List<ForecastInfoRowVo> result = new ArrayList<ForecastInfoRowVo>();
        for (Budget b : budgetsEjb.getBudgetsForParent(parentBudgetId)) {
            result.add(getForecastInfoForBudget(forecastId, b.getId()));
        }
        return result;
    }

}
