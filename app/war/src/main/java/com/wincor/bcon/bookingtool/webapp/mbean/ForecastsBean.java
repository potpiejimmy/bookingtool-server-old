package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.Forecast;
import com.wincor.bcon.bookingtool.server.ejb.BudgetPlansEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.ForecastsEJBLocal;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoRowVo;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

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
    
    @Inject
    private BudgetsBean budgetsBean;

    private Forecast current = null;
    private Forecast selected = null;
    
    private List<ForecastInfoRowVo> rows = null;
    
    private DualListModel<BudgetPlan> assignedBudgetPlans = new DualListModel<BudgetPlan>();
    
    private boolean showDetails = false;
    private boolean hidePast = false;
    private boolean showIfrs = false;

    public void clear() {
        current = null;
    }
    
    public void newForecast() {
        current = new Forecast();
        
        assignedBudgetPlans.setSource(budgetPlansEjb.getBudgetPlans());
        assignedBudgetPlans.setTarget(new ArrayList<BudgetPlan>());
        rows = null;
    }

    public Forecast getCurrent() {
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
	
    public Float getCentsPerHourIfrs() {
        if (current.getCentsPerHourIfrs()==null) return null;
        return ((float)current.getCentsPerHourIfrs())/100;
    }

    public void setCentsPerHourIfrs(Float centsPerHourIfrs) {
        current.setCentsPerHourIfrs(Math.round(centsPerHourIfrs*100));
    }
	
    public void save() {
        try {
            current = ejb.saveForecast(current, assignedBudgetPlans.getTarget());
            rows = null; // reset row data
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
    
    public Forecast getSelected() {
		return selected;
	}

	public void setSelected(Forecast selected) {
		this.selected = selected;
	}

	public void deleteSelected() {
        try {
            ejb.deleteForecast(getSelected().getId());
            clear();
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }
    
    public String getBudgetDisplayName(Budget b) {
        // display only budget name and maximum one parent
        StringBuilder stb = new StringBuilder(b.getName());
        if (b.getParentId() != null) {
                b = budgetsEjb.getBudget(b.getParentId());
                stb.insert(0, b.getName() + " \u25B6 ");
        }
        return stb.toString();
    }

    public List<ForecastInfoRowVo> getForecastRows() {
        if (rows != null) return rows;
        
        if (current.getId() == null) return null;
        rows = new ArrayList<ForecastInfoRowVo>();
        for (BudgetPlan p : ejb.getAssignedBudgetPlans(current.getId())) {
            ForecastInfoRowVo parentRow = ejb.getForecastInfoForBudget(current.getId(), p.getBudgetId());
            rows.add(parentRow);

            if (showDetails) {
                // detail view: iterate over all leaf budgets:
                parentRow.setSummaryRow(true);
                for (Budget b : budgetsEjb.getLeafBudgetsForParent(p.getBudgetId())) {
                    ForecastInfoRowVo row = ejb.getForecastInfoForBudget(current.getId(), b.getId());
                    rows.add(row);
                    // subtract planned and booked from parent for the detail view:
                    for (int month : getMonthColumns()) {
                        ForecastInfoVo myCell = row.getMonths().get(month);
                        ForecastInfoVo parentCell = parentRow.getMonths().get(month);
                        parentCell.setBookedMinutes(parentCell.getBookedMinutes() - myCell.getBookedMinutes());
                        parentCell.setPlannedMinutes(parentCell.getPlannedMinutes() - myCell.getPlannedMinutes());
                    }
                }
            }
        }
        return rows;
    }
    
    public List<Integer> getMonthColumns() {
        if (current == null || current.getId() == null) return null;
        return ejb.getMonthsForFiscalYear(current.getFiscalYear());
    }
    
    public List<Integer> getMonthColumnsForDisplay() {
        List<Integer> months = getMonthColumns();
        if (!hidePast || months == null) return months;
        List<Integer> futureMonths = new ArrayList<Integer>(months.size());
        for (int i=0; i<months.size(); i++)
            if (i==months.size()-1 || months.get(i+1) >= getCurrentMonth())
                futureMonths.add(months.get(i));
        return futureMonths;
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

    protected Float minutesToEuro(int minutes) {
        return ((float)minutes)/60 * (showIfrs ? current.getCentsPerHourIfrs() : current.getCentsPerHour()) / 100;
    }
    
    public Float getColumnSumEurosPlanned(int month) {
        return minutesToEuro(getColumnSumMinutesPlanned(month));
    }

    public int getTotalSumMinutesPlanned() {
        int sumMinutes = 0;
        for (int month : getMonthColumns())
            sumMinutes += getColumnSumMinutesPlanned(month);
        return sumMinutes;
    }

    public Float getTotalSumEurosPlanned() {
        return minutesToEuro(getTotalSumMinutesPlanned());
    }

    public Float getColumnSumEurosBooked(int month) {
        return minutesToEuro(getColumnSumMinutesBooked(month));
    }

    public int getTotalSumMinutesBooked() {
        int sumMinutes = 0;
        for (int month : getMonthColumns())
            if (month <= getCurrentMonth())
                sumMinutes += getColumnSumMinutesBooked(month);
        return sumMinutes;
    }

    public Float getTotalSumEurosBooked() {
        return minutesToEuro(getTotalSumMinutesBooked());
    }

    public Float getColumnSumEurosDiff(int month) {
        return getColumnSumEurosPlanned(month) - getColumnSumEurosBooked(month);
    }
    
    public Float getColumnSumEurosDiffFcBudget(int month) {
        return ((float)current.getFcBudgetCents())/1200 - getColumnSumEurosBooked(month);
    }
    
    public Float getTotalSumEurosDiff() {
        int diffMinutes = 0;
        for (int month : getMonthColumns())
            if (month < getCurrentMonth())
                diffMinutes += getColumnSumMinutesPlanned(month) - getColumnSumMinutesBooked(month);
        return minutesToEuro(diffMinutes);
    }

    public Float getTotalSumEurosDiffFcBudget() {
        int bookedSoFar = 0;
        int numberOfMonths = 0;
        for (int month : getMonthColumns())
            if (month < getCurrentMonth()) {
                bookedSoFar += getColumnSumMinutesBooked(month);
                numberOfMonths++;
            }
        return ((float)current.getFcBudgetCents())*numberOfMonths/1200 - minutesToEuro(bookedSoFar);
    }

    public int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH)+1);
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
        this.rows = null; // reset rows
    }

    public boolean isHidePast() {
        return hidePast;
    }

    public void setHidePast(boolean hidePast) {
        this.hidePast = hidePast;
    }

    public boolean isShowIfrs() {
        return showIfrs;
    }

    public void setShowIfrs(boolean showIfrs) {
        this.showIfrs = showIfrs;
    }

    public String getFormattedBudgetOffset(int minutes) {
        if (minutes == 0) return "\u00B10";
        return (minutes > 0 ? "+" : "-") + budgetsBean.getFormattedBudgetTime(minutes);
    }
    
    public StreamedContent getExportPlanData() {
        return ExcelExportBean.streamForWorkbook(ejb.exportPlanData(current.getId()), "plan_data");
    }

    public StreamedContent getSalesReport() {
        return ExcelExportBean.streamForWorkbook(ejb.createSalesReport(current.getId()), "sales_report");
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
