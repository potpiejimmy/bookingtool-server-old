package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

/**
 * The "personal resource plan" backing bean.
 */
@Named
@SessionScoped
public class PersPlanBean implements Serializable, Converter {

    private static final long serialVersionUID = 1L;
    
    public static class MyRow implements Serializable {
        private String testProperty = null;
        private String testProperty2 = null;
        private Map<Integer,String> values = new HashMap<Integer,String>();
        public String getTestProperty() {
            return testProperty;
        }

        public void setTestProperty(String testProperty) {
            this.testProperty = testProperty;
        }
        
        public Map<Integer,String> getValues() {
            return values;
        }

        public String getTestProperty2() {
            return testProperty2;
        }

        public void setTestProperty2(String testProperty2) {
            this.testProperty2 = testProperty2;
        }
    }
    
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.");
    private final static DateFormat WEEKDAY_FORMATTER = new SimpleDateFormat("EEEEE", Locale.GERMANY);

    private List<MyRow> rowData = null;
    
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
    
    public List<SelectItem> getSelectItems() {
        List<SelectItem> result = new ArrayList<SelectItem>();
        result.add(new SelectItem("", ""));
        result.add(new SelectItem("U", "U Urlaub"));
        result.add(new SelectItem("P", "P Projekt"));
        return result;
    }
    
    public List<Integer> getWeekdayColumns() {
        return Arrays.asList(new Integer[] {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY});
    }
    
    public List<MyRow> getRowData() {
        if (rowData == null) {
            rowData = new ArrayList<MyRow>();
            rowData.add(new MyRow());
            rowData.add(new MyRow());
        }
        
        return rowData;
    }

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ""+value;
    }
}
