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
    
    public static class WeekData implements Serializable {
        
        private int weekOfYear = 0;
        private int year = 0;
        private String formattedWeekDateRange;
        
        private Map<Integer,String> values = new HashMap<Integer,String>();

        public WeekData(int weekOfYear, int year) {
            this.weekOfYear = weekOfYear;
            this.year = year;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
            formattedWeekDateRange = DATE_FORMATTER.format(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 6);
            formattedWeekDateRange += " - " + DATE_FORMATTER.format(cal.getTime());
        }
        
        public Map<Integer,String> getValues() {
            return values;
        }
        
        public int getWeekOfYear() {
            return weekOfYear;
        }
        
        public int getYear() {
            return year;
        }
        
        public String getFormattedWeekDateRange() {
            return formattedWeekDateRange;
        }
    }
    
    private int year = Calendar.getInstance().get(Calendar.YEAR);
    private int weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
    private int numWeeks = 14;
    
    private List<WeekData> rows = null;
    
    @EJB
    private ProjectsEJBLocal projectsEjb;
    
    public void applySettings() {
        this.rows = null; // reset row data
    }
    
    public void savePlan() {
        try {
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
        }
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    public int getNumWeeks() {
        return numWeeks;
    }

    public void setNumWeeks(int numWeeks) {
        this.numWeeks = numWeeks;
    }

    public String formatWeekdayName(int weekday) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, weekday);
        return WEEKDAY_FORMATTER.format(cal.getTime());
    }
    
    public String getCellStyleClass(Integer weekday, String value) {
        return value != null  && value.length() > 0 ?
            ("U".equals(value) ? "persplancellgone" : 
             ("A".equals(value) ? "persplancellofftime" :
              "persplancelloccupied")) :
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
    
    public List<WeekData> getRowData() {
        if (rows == null) {
            rows = new ArrayList<WeekData>(numWeeks);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
            for (int i = 0; i < numWeeks; i++) {
                rows.add(new WeekData(cal.get(Calendar.WEEK_OF_YEAR), cal.get(Calendar.YEAR)));
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            }
        }
        
        return rows;
    }
}
