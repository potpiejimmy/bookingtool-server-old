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
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoRowVo;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoVo;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import java.util.ArrayList;
import java.util.Calendar;
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
        for (BudgetPlan b : assignedBudgetPlans) {
            ForecastBudgetPlan p = new ForecastBudgetPlan();
            p.setForecastId(forecast.getId());
            p.setBudgetPlanId(b.getId());
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
    public ForecastInfoRowVo getForecastInfoForBudget(int forecastId, int budgetId) {
        Forecast forecast = em.find(Forecast.class, forecastId);
        ForecastInfoRowVo row = new ForecastInfoRowVo(budgetsEjb.getBudgetInfo(budgetId));
        for (int month = 0; month < 12; month++) {
            ForecastInfoVo cell = new ForecastInfoVo();
            int year = month < 3 ? forecast.getFiscalYear() - 1 : forecast.getFiscalYear();
            int monthOfYear = month < 3 ? month + 9 : month - 3;
            int period = year * 100 + (monthOfYear+1);
            Calendar start = Calendar.getInstance();
            start.set(Calendar.YEAR, year);
            start.set(Calendar.MONTH, monthOfYear);
            start.set(Calendar.DAY_OF_MONTH, 1);
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.MONTH, 1);
            TimePeriod timePeriod = new TimePeriod(start.getTimeInMillis(), end.getTimeInMillis());
            cell.setPeriod(period);
            cell.setPlannedMinutes(budgetPlansEjb.getPlannedMinutesForPeriod(budgetId, period));
            cell.setBookedMinutes(budgetsEjb.getBudgetInfo(budgetId, timePeriod).getBookedMinutesRecursive());
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
