package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.Forecast;
import com.wincor.bcon.bookingtool.server.ejb.BudgetPlansEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.ejb.ForecastsEJBLocal;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoRowVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.Calendar;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.primefaces.model.DualListModel;

@Named
@SessionScoped
public class ForecastsBean implements Serializable, Converter {

    private static final long serialVersionUID = 1L;

    @EJB
    private ForecastsEJBLocal ejb;

    @EJB
    private BudgetsEJBLocal budgetsEjb;

    @EJB
    private BudgetPlansEJBLocal budgetPlansEjb;

    private Forecast current = null;
    
    private List<ForecastInfoRowVo> rows = null;
    
    private DualListModel<BudgetPlan> assignedBudgetPlans = null;

    public void clear() {
        newForecast();
    }
    
    protected void newForecast() {
        current = new Forecast();
        
        assignedBudgetPlans = new DualListModel<BudgetPlan>(budgetPlansEjb.getBudgetPlans(), new ArrayList<BudgetPlan>());
        rows = null;
    }

    public Forecast getCurrent() {
        if (current == null) newForecast();
        return current;
    }
    
    public DualListModel<BudgetPlan> getAssignedBudgetPlans() {
        if (current == null) newForecast();
        return assignedBudgetPlans;
    }

    public void setAssignedBudgetPlans(DualListModel<BudgetPlan> assignedBudgetPlans) {
        this.assignedBudgetPlans = assignedBudgetPlans;
    }

    public List<Forecast> getForecasts() {
        return ejb.getForecasts();
    }

    public Float getFcBudgetCents() {
        if (current.getFcBudgetCents()==null) return null;
        return ((float)current.getFcBudgetCents())/100;
    }

    public void setFcBudgetCents(Float fcBudgetCents) {
        current.setFcBudgetCents(Math.round(fcBudgetCents*100));
    }

    public Float getCentsPerHour() {
        if (current.getCentsPerHour()==null) return null;
        return ((float)current.getCentsPerHour())/100;
    }

    public void setCentsPerHour(Float centsPerHour) {
        current.setCentsPerHour(Math.round(centsPerHour*100));
    }
	
    public void save() {
        try {
            current = ejb.saveForecast(current, assignedBudgetPlans.getTarget());
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }

    public void edit(Forecast f) {
        current = f;
        rows = null;

        List<BudgetPlan> source = new ArrayList<BudgetPlan>(budgetPlansEjb.getBudgetPlans());
        List<BudgetPlan> target = ejb.getAssignedBudgetPlans(current.getId());
        source.removeAll(target);
        assignedBudgetPlans.setSource(source);
        assignedBudgetPlans.setTarget(target);
    }

    public void delete(Forecast f) {
        try {
            ejb.deleteForecast(f.getId());
            clear();
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }
    
    public List<ForecastInfoRowVo> getForecastRows() {
        if (rows != null) return rows;
        
        if (current.getId() == null) return null;
        rows = new ArrayList<ForecastInfoRowVo>();
        for (BudgetPlan p : ejb.getAssignedBudgetPlans(current.getId())) {
            ForecastInfoRowVo row = ejb.getForecastInfoForBudget(current.getId(), p.getBudgetId());
            rows.add(row);
            rows.add(row); // display two rows for each budget
        }
        return rows;
    }
    
    public List<Integer> getMonthColumns() {
        if (current == null || current.getId() == null) return null;
        return ejb.getMonthsForFiscalYear(current.getFiscalYear());
    }
    
    public int getColumnSumMinutesPlanned(int month) {
        int result = 0;
        for (ForecastInfoRowVo row : getForecastRows()) {
            result += row.getMonths().get(month).getPlannedMinutes();
        }
        return result;
    }

    public int getColumnSumMinutesBooked(int month) {
        int result = 0;
        for (ForecastInfoRowVo row : getForecastRows()) {
            result += row.getMonths().get(month).getBookedMinutes();
        }
        return result;
    }

    public Float getColumnSumEurosPlanned(int month) {
        return ((float)getColumnSumMinutesPlanned(month))/60 * current.getCentsPerHour() / 100;
    }

    public Float getTotalSumEurosPlanned() {
        int sumMinutes = 0;
        for (int month : getMonthColumns())
            sumMinutes += getColumnSumMinutesPlanned(month);
        return ((float)sumMinutes)/60 * current.getCentsPerHour() / 100;
    }

    public Float getColumnSumEurosBooked(int month) {
        return ((float)getColumnSumMinutesBooked(month))/60 * current.getCentsPerHour() / 100;
    }

    public Float getTotalSumEurosBooked() {
        int sumMinutes = 0;
        for (int month : getMonthColumns())
            if (month <= getCurrentMonth())
                sumMinutes += getColumnSumMinutesBooked(month);
        return ((float)sumMinutes)/60 * current.getCentsPerHour() / 100;
    }

    public Float getColumnSumEurosDiff(int month) {
        return getColumnSumEurosPlanned(month) - getColumnSumEurosBooked(month);
    }
    
    public Float getTotalSumEurosDiff() {
        int diffMinutes = 0;
        for (int month : getMonthColumns())
            if (month <= getCurrentMonth())
                diffMinutes += getColumnSumMinutesPlanned(month) - getColumnSumMinutesBooked(month);
        return ((float)diffMinutes)/60 * current.getCentsPerHour() / 100;
    }

    public int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH)+1);
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return budgetPlansEjb.getBudgetPlan(Integer.parseInt(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((BudgetPlan)value).getId().toString();
    }
}
