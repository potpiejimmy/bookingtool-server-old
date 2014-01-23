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
import javax.faces.model.SelectItem;

/**
 * The "personal resource plan" backing bean.
 */
@Named
@SessionScoped
public class PersPlanBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static class RowData implements Serializable {
        private Map<Integer,String> values = new HashMap<Integer,String>();
        public Map<Integer,String> getValues() {
            return values;
        }
    }
    
    private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.");
    private final static DateFormat WEEKDAY_FORMATTER = new SimpleDateFormat("EEEEE", Locale.GERMANY);
    
    private List<RowData> rows = null;
    
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
    
    public List<RowData> getRowData() {
        if (rows == null) {
            rows = new ArrayList<RowData>();
            rows.add(new RowData());
            rows.add(new RowData());
        }
        
        return rows;
    }
}
