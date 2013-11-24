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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.DualListModel;
import org.primefaces.model.TreeNode;

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
    
    private DualListModel<BudgetPlan> assignedBudgetPlans = null;

    public void clear() {
        newForecast();
    }
    
    protected void newForecast() {
        current = new Forecast();
        
        assignedBudgetPlans = new DualListModel<BudgetPlan>(budgetPlansEjb.getBudgetPlans(), new ArrayList<BudgetPlan>());
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

        List<BudgetPlan> source = new ArrayList<BudgetPlan>(budgetPlansEjb.getBudgetPlans());
        List<BudgetPlan> target = ejb.getAssignedBudgetPlans(current.getId());
        source.removeAll(target);
        assignedBudgetPlans.setSource(source);
        assignedBudgetPlans.setTarget(target);
    }

    public void delete(Forecast f) {
        try {
            ejb.deleteForecast(f.getId());
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }
    
    public TreeNode getForecastRows() {
        if (current.getId() == null) return null;
        TreeNode root = new DefaultTreeNode("root", null);
        for (BudgetPlan p : ejb.getAssignedBudgetPlans(current.getId())) {
            ForecastInfoRowVo row = ejb.getForecastInfoForBudget(current.getId(), p.getBudgetId());
            new DefaultTreeNode(row, root);
        }
        return root;
    }
    
    public List<Integer> getMonthColumns() {
        if (current == null || current.getId() == null) return null;
        return ejb.getMonthsForFiscalYear(current.getFiscalYear());
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
