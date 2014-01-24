package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJBLocal;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import javax.faces.model.SelectItem;

/**
 * The "personal resource plan" backing bean.
 */
@Named
@SessionScoped
public class PersPlanBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("dd. MMM");
    private final static DateFormat WEEKDAY_FORMATTER = new SimpleDateFormat("EEEEE", Locale.GERMANY);
    
    public static class RowData implements Serializable {
        
        private int weekOfYear = 0;
        private String formattedWeekDateRange;
        
        private Map<Integer,String> values = new HashMap<Integer,String>();

        public RowData(int weekOfYear) {
            this.weekOfYear = weekOfYear;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
            formattedWeekDateRange = DATE_FORMATTER.format(cal.getTime());
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            formattedWeekDateRange += " - " + DATE_FORMATTER.format(cal.getTime());
        }
        
        public Map<Integer,String> getValues() {
            return values;
        }
        
        public int getWeekOfYear() {
            return weekOfYear;
        }
        
        public String getFormattedWeekDateRange() {
            return formattedWeekDateRange;
        }
    }
    
    private List<RowData> rows = null;
    
    @EJB
    private ProjectsEJBLocal projectsEjb;
    
    public void savePlan() {
        try {
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }

    public String formatWeekdayName(int weekday) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, weekday);
        return WEEKDAY_FORMATTER.format(cal.getTime());
    }
    
    public String getCellStyleClass(Integer weekday, String value) {
        return value != null  && value.length() > 0 ?
            ("U".equals(value) ? "persplancellgone" : "persplancelloccupied") :
            ((weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY) ?
              "" :
              "persplancellfree");
    }
        
    public List<SelectItem> getSelectItems() {
        List<SelectItem> result = new ArrayList<SelectItem>();
        result.add(new SelectItem(null, ""));
        result.add(new SelectItem("A", "A Abwesenheit"));
        result.add(new SelectItem("I", "I Internes Projekt"));
        for (Project p : projectsEjb.getProjects())
            result.add(new SelectItem("P"+p.getId(), "P "+p.getName()));
        result.add(new SelectItem("U", "U Urlaub"));
        return result;
    }
    
    public List<RowData> getRowData() {
        if (rows == null) {
            rows = new ArrayList<RowData>();
            rows.add(new RowData(3));
            rows.add(new RowData(4));
            rows.add(new RowData(5));
            rows.add(new RowData(6));
            rows.add(new RowData(7));
            rows.add(new RowData(8));
        }
        
        return rows;
    }
}
