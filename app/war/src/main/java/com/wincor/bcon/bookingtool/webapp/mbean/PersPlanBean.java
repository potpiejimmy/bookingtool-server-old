package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.db.entity.ResourcePlanItem;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJB;
import com.wincor.bcon.bookingtool.server.ejb.ResourcesEJB;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
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
    
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("dd. MMMMM", Locale.GERMANY);
    private final static DateFormat WEEKDAY_FORMATTER = new SimpleDateFormat("EEEEE", Locale.GERMANY);
    
    public static class WeekData implements Serializable {
        
        private int weekOfYear = 0;
        private int year = 0;
        private String formattedWeekDateRange;
        private Calendar startDate;
        private Calendar endDate;
        
        private Map<String,String> values = new HashMap<String,String>();

        public WeekData(int weekOfYear, int year) {
            this.weekOfYear = weekOfYear;
            this.year = year;
            this.startDate = Calendar.getInstance(Locale.GERMANY);
            this.startDate.set(Calendar.YEAR, year);
            this.startDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            this.startDate.set(Calendar.WEEK_OF_YEAR, weekOfYear);
            this.startDate.set(Calendar.HOUR_OF_DAY, 0);
            this.startDate.set(Calendar.MINUTE, 0);
            this.startDate.set(Calendar.SECOND, 0);
            this.startDate.set(Calendar.MILLISECOND, 0);
            formattedWeekDateRange = DATE_FORMATTER.format(startDate.getTime());
            this.endDate = Calendar.getInstance(Locale.GERMANY);
            this.endDate.setTime(startDate.getTime());
            this.endDate.add(Calendar.DAY_OF_YEAR, 6);
            formattedWeekDateRange += " - " + DATE_FORMATTER.format(endDate.getTime());
        }
        
        public Map<String,String> getValues() {
            return values;
        }
        
        public int getWeekOfYear() {
            return weekOfYear;
        }
        
        public int getYear() {
            return year;
        }
        
        public Calendar getStartDate() {
            return startDate;
        }
        
        public Calendar getEndDate() {
            return endDate;
        }
        
        public String getFormattedWeekDateRange() {
            return formattedWeekDateRange;
        }
    }
    
    private int year = Calendar.getInstance(Locale.GERMANY).get(Calendar.YEAR);
    private int weekOfYear = Calendar.getInstance(Locale.GERMANY).get(Calendar.WEEK_OF_YEAR);
    private int numWeeks = 14;
    
    private List<WeekData> rows = null;
    
    @EJB
    private ResourcesEJB ejb;
    
    @EJB
    private ProjectsEJB projectsEjb;
    
    public void applySettings() {
        this.rows = null; // reset row data
    }
    
    public void savePlan() {
        try {
            Calendar planBegin = rows.get(0).getStartDate();
            Calendar planEnd = rows.get(rows.size()-1).getEndDate();
            TimePeriod period = new TimePeriod(planBegin.getTimeInMillis(), planEnd.getTimeInMillis());
            List<ResourcePlanItem> dataModel = viewModelToDataModel(rows);
            
            ejb.savePersonalPlan(WebUtils.getCurrentPerson(), period, dataModel);
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
        Calendar cal = Calendar.getInstance(Locale.GERMANY);
        cal.set(Calendar.DAY_OF_WEEK, weekday);
        return WEEKDAY_FORMATTER.format(cal.getTime());
    }
    
    public String getCellStyleClass(Integer weekday, String value) {
        return value != null  && value.length() > 0 ?
            (("U".equals(value)||"Z".equals(value)||"K".equals(value)) ? "persplancellgone" : 
             ("F".equals(value) ? "persplancellofftime" :
              "persplancelloccupied")) :
            ((weekday == Calendar.SATURDAY || weekday == Calendar.SUNDAY) ?
              "" :
              "persplancellfree");
    }
        
    public List<SelectItem> getSelectItems() {
        List<SelectItem> result = new ArrayList<SelectItem>();
        result.add(new SelectItem(null, ""));
        result.add(new SelectItem("A", "A Angeboten"));
        result.add(new SelectItem("F", "F Frei"));
        result.add(new SelectItem("I", "I Internes Projekt"));
        result.add(new SelectItem("K", "K Krankheit"));
        for (Project p : projectsEjb.getProjects())
            result.add(new SelectItem("P"+p.getId(), "P "+p.getName()));
        result.add(new SelectItem("S", "S Schulung"));
        result.add(new SelectItem("U", "U Urlaub"));
        result.add(new SelectItem("Z", "Z Zusatztag"));
        return result;
    }
    
    public List<WeekData> getRowData() {
        if (rows == null) {
            
            Calendar cal = new WeekData(weekOfYear, year).getStartDate();
            long periodStart = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, numWeeks);
            cal.add(Calendar.DAY_OF_YEAR, 6);
            long periodEnd = cal.getTimeInMillis();
            TimePeriod timePeriod = new TimePeriod(periodStart, periodEnd);
            List<ResourcePlanItem> dataModel = ejb.getPersonalPlan(WebUtils.getCurrentPerson(), timePeriod);
            
            this.rows = dataModelToViewModel(dataModel, weekOfYear, year, numWeeks);
        }
        
        return rows;
    }
    
    protected static List<WeekData> dataModelToViewModel(List<ResourcePlanItem> dataModel, int weekOfYear, int year, int numWeeks) {
        List<WeekData> rows = new ArrayList<WeekData>(numWeeks);
        Calendar cal = Calendar.getInstance(Locale.GERMANY);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
        for (int i = 0; i < numWeeks; i++) {
            WeekData week = new WeekData(cal.get(Calendar.WEEK_OF_YEAR), cal.get(Calendar.YEAR));
            rows.add(week);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            for (ResourcePlanItem item : dataModel) {
                Calendar itemDay = Calendar.getInstance(Locale.GERMANY);
                itemDay.setTime(item.getDay());
                itemDay.set(Calendar.HOUR_OF_DAY, 0);
                itemDay.set(Calendar.MINUTE, 0);
                itemDay.set(Calendar.SECOND, 0);
                itemDay.set(Calendar.MILLISECOND, 0);
                if (itemDay.compareTo(week.getStartDate()) >= 0 &&
                    itemDay.compareTo(week.getEndDate()) <= 0) {
                    String value = "" + item.getAvail() + (item.getProjectId() != null ? item.getProjectId() : "");
                    week.getValues().put(String.valueOf(itemDay.get(Calendar.DAY_OF_WEEK)), value);
                }
            }
        }
        return rows;
    }

    protected static List<ResourcePlanItem> viewModelToDataModel(List<WeekData> viewModel) {
        List<ResourcePlanItem> items = new ArrayList<ResourcePlanItem>();

        for (WeekData row : viewModel) {
            Calendar day = row.getStartDate();
            for (int i = 0; i < 7; i++) {
                String value = row.getValues().get(String.valueOf(day.get(Calendar.DAY_OF_WEEK)));
                if (value != null && value.length() > 0) {
                    ResourcePlanItem item = new ResourcePlanItem();
                    item.setDay(day.getTime());
                    item.setAvail(value.charAt(0));
                    if (value.startsWith("P"))
                        item.setProjectId(Integer.parseInt(value.substring(1)));
                    items.add(item);
                }
                day.add(Calendar.DAY_OF_YEAR, 1);
            }
        }
        return items;
    }
}
