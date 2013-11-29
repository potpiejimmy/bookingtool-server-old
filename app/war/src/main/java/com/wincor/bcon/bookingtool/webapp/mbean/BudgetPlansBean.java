package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlanItem;
import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.BudgetPlansEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.webapp.mbean.vo.BudgetPlanVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.inject.Inject;

@Named
@SessionScoped
public class BudgetPlansBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final static DateFormat MONTH_FORMATTER_LONG = new SimpleDateFormat("MMM yyyy");
    private final static DateFormat MONTH_FORMATTER_SHORT = new SimpleDateFormat("MMM");

    @Inject
    private BudgetsBean budgetsBean;

    @EJB
    private BudgetPlansEJBLocal ejb;

    @EJB
    private BudgetsEJBLocal budgetsEjb;

    private int currentProjectId = 0;
    private BudgetPlan currentBudgetPlan = null;
    private String currentBudgetPlanBegin = null;
    private String currentBudgetPlanEnd = null;
    
    private List<BudgetPlanVo> planData = null;

    public void clear() {
            newBudgetPlan();
    }

    public BudgetPlan getCurrentBudgetPlan() {
            if (currentBudgetPlan == null) newBudgetPlan(); // lazy initialization to enable EJB access
            return currentBudgetPlan;
    }

    public List<SelectItem> getProjectItems() {
            List<Project> projects = budgetsEjb.getProjects();
            List<SelectItem> result = new ArrayList<SelectItem>(projects.size()+1);
            result.add(new SelectItem(0, "<Please choose>"));
            for (Project p : projects) {
                    result.add(new SelectItem(p.getId(), p.getName()));
            }
            return result;
    }

    public List<SelectItem> getBudgetItems() {
            List<Budget> budgets = budgetsEjb.getBudgets(currentProjectId);
            List<SelectItem> result = new ArrayList<SelectItem>(budgets.size() + 1);
            result.add(new SelectItem(0, "<Please choose>"));
            for (Budget b : budgets) {
                result.add(new SelectItem(b.getId(), budgetsBean.getFullBudgetName(b)));
            }
            return WebUtils.sortSelectItems(result);
    }
    
    public List<BudgetPlan> getBudgetPlans() {
        return ejb.getBudgetPlans();
    }

    public int getCurrentProjectId() {
            return currentProjectId;
    }

    public void setCurrentProjectId(int currentProjectId) {
            this.currentProjectId = currentProjectId;
    }

    public String getCurrentBudgetPlanBegin() {
        return currentBudgetPlanBegin;
    }

    public void setCurrentBudgetPlanBegin(String currentBudgetPlanBegin) {
        this.currentBudgetPlanBegin = currentBudgetPlanBegin;
    }

    public String getCurrentBudgetPlanEnd() {
        return currentBudgetPlanEnd;
    }

    public void setCurrentBudgetPlanEnd(String currentBudgetPlanEnd) {
        this.currentBudgetPlanEnd = currentBudgetPlanEnd;
    }

    protected static int periodStringToInt(String period) throws NumberFormatException {
        int result = 0;
        for (String s : period.split("/")) {
            result *= 100;
            result += Integer.parseInt(s);
        }
        return result;
    }
    
    protected static String periodIntToString(int period) throws NumberFormatException {
        return (period/100) + "/" + (period%100 < 10 ? "0"+(period%100) : (period%100));
    }
    
    public String formatPeriodNameForColumn(int column) {
        return formatPeriodName(getMonthPeriod(column-1));
    }
    
    public String formatPeriodName(int period) {
        return formatPeriodName(period, period%100 == 1 || period == currentBudgetPlan.getPlanBegin());
    }
    
    public String formatPeriodName(int period, boolean longFormat) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, period / 100);
        cal.set(Calendar.MONTH, (period%100) - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return (longFormat ? MONTH_FORMATTER_LONG : MONTH_FORMATTER_SHORT).format(cal.getTime());
    }
    
    protected void checkValidMonth(int period) {
        int month = (period % 100);
        if (month-1 < Calendar.JANUARY || month-1 > Calendar.DECEMBER)
            throw new IllegalArgumentException("Invalid month: " + month);
    }
    
    public void create() {
        try {
            currentBudgetPlan.setPlanBegin(periodStringToInt(currentBudgetPlanBegin));
            currentBudgetPlan.setPlanEnd(periodStringToInt(currentBudgetPlanEnd));
            
            checkValidMonth(currentBudgetPlan.getPlanBegin());
            checkValidMonth(currentBudgetPlan.getPlanEnd());
            
            if (currentBudgetPlan.getPlanEnd() < currentBudgetPlan.getPlanBegin())
                throw new IllegalArgumentException("Planning Month End must not be before Planning Month Begin");
            int numberOfMonths = getMonthOffset(currentBudgetPlan.getPlanEnd()) + 1;
            if (numberOfMonths > 36)
                throw new IllegalArgumentException("Sorry, planning period too large, maximum 36 months");
            
            currentBudgetPlan = ejb.saveBudgetPlan(currentBudgetPlan);
            
            // start editing:
            edit(currentBudgetPlan);
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }

    public void edit(BudgetPlan b) {
            currentBudgetPlan = b;
            currentBudgetPlanBegin = periodIntToString(currentBudgetPlan.getPlanBegin());
            currentBudgetPlanEnd = periodIntToString(currentBudgetPlan.getPlanEnd());
            
            List<Budget> leafBudgets = budgetsEjb.getLeafBudgets(currentBudgetPlan.getBudgetId());
            planData = new ArrayList<BudgetPlanVo>(leafBudgets.size());
            for (Budget bu : leafBudgets) {
                BudgetPlanVo vo = new BudgetPlanVo(bu);
                loadVo(vo);
                planData.add(vo);
            }
    }
    
    protected void loadVo(BudgetPlanVo vo) {
        List<BudgetPlanItem> items = ejb.getBudgetPlanItems(vo.getBudget().getId());
        for (BudgetPlanItem item : items) {
            vo.getValues().put(item.getPeriod(), ((float)item.getMinutes())/480);
        }
    }

    public void delete(BudgetPlan b) {
        try {
                ejb.deleteBudgetPlan(b.getId());
        } catch (Exception ex) {
                WebUtils.addFacesMessage(ex);
        }
    }

    public void newBudgetPlan() {
            currentBudgetPlan = new BudgetPlan();
    }
    
    // ------------------------------------------------------------

    public List<BudgetPlanVo> getLeafBudgets() {
        return planData;
    }
    
    public List<Integer> getMonthColumns() {
        List<Integer> result = new ArrayList<Integer>();
        int currentMonth = currentBudgetPlan.getPlanBegin();
        for (int i = 1; currentMonth <= currentBudgetPlan.getPlanEnd(); i++) {
            result.add(currentMonth);
            currentMonth = getMonthPeriod(i);
        }
        return result;
    }
    
    protected int getMonthPeriod(int offset) {
        int result = currentBudgetPlan.getPlanBegin() + offset;
        while (result % 100 > 12) result += 88; // next year
        return result;
    }
    
    protected int getMonthOffset(int period) {
        int offset = 0;
        while (period > currentBudgetPlan.getPlanBegin()) {
            offset++; period--;
            if (period % 100 == 0) period -= 88;
        }
        return offset;
    }
    
    public int getPlannedMinutesForRow(int budgetId) {
        int result = 0;
        for (BudgetPlanVo planVo : planData) {
            if (planVo.getBudget().getId().equals(budgetId)) {
                int i = 0;
                while (getMonthPeriod(i) <= currentBudgetPlan.getPlanEnd()) {
                    result += getMinutesForCell(planVo, getMonthPeriod(i));
                    i++;
                }
            }
        }
        return result;
    }
    
    public int getPlannedMinutesForColumn(int period) {
        int result = 0;
        for (BudgetPlanVo planVo : planData) {
            result += getMinutesForCell(planVo, period);
        }
        return result;
    }
    
    protected int getMinutesForCell(BudgetPlanVo vo, int period) {
        if (vo.getValues().get(period) == null) return 0;
        return Math.round(vo.getValues().get(period).floatValue() * 480);
    }
    
    public boolean renderMonthColumn(int column) {
        return getMonthPeriod(column-1) <= currentBudgetPlan.getPlanEnd();
    }
    
    public void savePlan() {
        try {
            for (BudgetPlanVo vo : planData) {
                List<Integer> periods = getMonthColumns();
                List<BudgetPlanItem> items = new ArrayList<BudgetPlanItem>(periods.size());
                for (Integer period : periods) {
                    int minutes = getMinutesForCell(vo, period);
                    if (minutes > 0) {
                        BudgetPlanItem item = new BudgetPlanItem();
                        item.setPeriod(period);
                        item.setMinutes(minutes);
                        items.add(item);
                    }
                }
                ejb.saveBudgetPlanItems(vo.getBudget().getId(), items);
            }
            // leave current editing mode:
            clear();
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }
    
    public boolean isPlanComplete(BudgetPlan p) {
        return ejb.isPlanComplete(p.getId());
    }
}
