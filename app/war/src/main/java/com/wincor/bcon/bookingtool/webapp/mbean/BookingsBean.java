package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.model.chart.PieChartModel;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.ejb.BookingTemplatesEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BookingsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJBLocal;
import com.wincor.bcon.bookingtool.server.util.Utils;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.faces.context.FacesContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.chart.MeterGaugeChartModel;

@Named
@SessionScoped
public class BookingsBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@EJB
	private BookingsEJBLocal bookingEjb;
	@EJB
	private BookingTemplatesEJBLocal bookingTemplateEjb;
	@EJB
	private BudgetsEJBLocal budgetsEjb;
        @EJB
        private ProjectsEJBLocal projectsEjb;
	
        private Booking current;
	private Booking selected;
        
        private int numberOfQuickSelectRows = 5;
	
	private BookingTemplate currentTemplate = null;
	
	private final DateFormat DATE_FORMATTER = new SimpleDateFormat("EEEEE", FacesContext.getCurrentInstance().getViewRoot().getLocale());
	private final DateFormat MONTH_FORMATTER = new SimpleDateFormat("MMMMM", FacesContext.getCurrentInstance().getViewRoot().getLocale());
	
	public List<Booking> getBookings() {
		return bookingEjb.getBookings(WebUtils.getCurrentPerson(), current.getDay());
	}

	public void clear() {
		newBooking();
	}

	public Booking getCurrent() {
		if (current == null) newBooking();
		return current;
	}
	
	public String getTemplateDisplay(BookingTemplate t) {
		if (t==null) return "";
		return t.getPsp() + " " + t.getName() + " " + t.getSalesRepresentative() + " " + t.getDescription();
	}
	
	public String getCurrentDayFormatted() {
		return DATE_FORMATTER.format(getCurrent().getDay());
	}
	
	public BookingTemplate getCurrentTemplate() {
		return currentTemplate;
	}
	
	public void setCurrentTemplate(BookingTemplate bt) {
                if (bt == null) return;
		this.currentTemplate = bt;
		this.current.setSalesRepresentative(bt.getSalesRepresentative()); // adapt Vertriebsbeauftragter
                if (this.current.getId() == null)
                    this.current.setDescription(bt.getDescription()); // adapt description, but only for new bookings
	}
        
        public BudgetInfoVo getCurrentBudget() {
            if (currentTemplate == null) return null;
            return budgetsEjb.getBudgetInfo(currentTemplate.getBudgetId());
        }
	
	public void save() {
            try {
		current.setBookingTemplateId(currentTemplate.getId());
		bookingEjb.saveBooking(current);
		newBooking();
            } catch (Exception ex) {
                WebUtils.addFacesMessage(ex);
            }
	}
	
	public void edit(Booking v) {
		current = v;
		currentTemplate = bookingTemplateEjb.getBookingTemplate(v.getBookingTemplateId());
	}
	
	public Booking getSelected() {
		return selected;
	}

	public void setSelected(Booking selected) {
		this.selected = selected;
	}

	public void deleteSelected() {
		bookingEjb.deleteBooking(getSelected().getId());
	}
	
	public List<BookingTemplate> complete(String v) { 
		return bookingTemplateEjb.findBookingTemplates(v);
	} 
        
        public String shortenForCombo(String in) {
            return Utils.shorten(in, 120);
        }

	public String getFormattedBookingTime(int minutes) {
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumFractionDigits(2);
		return f.format(((float)minutes)/60) + " h";
	}

	public String getFormattedBookingTimeSum() {
		int sumMinutes = 0;
		for (Booking b : getBookings())
			sumMinutes += b.getMinutes();
		return getFormattedBookingTime(sumMinutes);
	}
	
	public String getPSPForBooking(Integer bookingTemplateId) {
		return bookingTemplateEjb.getBookingTemplate(bookingTemplateId).getPsp();
	}

	public String getPSPNameForBooking(Integer bookingTemplateId) {
		return bookingTemplateEjb.getBookingTemplate(bookingTemplateId).getName();
	}
	
	public void changeDay(Boolean forward) {
		Calendar c = Calendar.getInstance(Locale.GERMANY);
		c.setTime(getCurrent().getDay());
		
		c.add(Calendar.DAY_OF_MONTH, forward ? 1 : -1);
		
		getCurrent().setDay(c.getTime());
	}

	protected void newBooking() {
		Date lastDay = current != null ? current.getDay() : new Date();
		current = new Booking();
		current.setPerson(WebUtils.getCurrentPerson());
		current.setBookingTemplateId(1);
		current.setDay(lastDay);
		
		currentTemplate = null;
	}
        
        public PieChartModel getPieChartType() {
            return getPieChart(0);
        }
        
        public PieChartModel getPieChartPsp() {
            return getPieChart(1);
        }
        
        protected PieChartModel getPieChart(int chartType) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getCurrent().getDay());
            Map<String,Number> sums = bookingEjb.getBookingSumsForMonth(
                    WebUtils.getCurrentPerson(), 
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    chartType);
            if (sums.isEmpty()) sums.put("keine Buchungen", 1);
            PieChartModel model = new PieChartModel();
            final int MAX_LEGEND = 10;
            String[] keys = sums.keySet().toArray(new String[sums.size()]);
            Arrays.sort(keys);
            for (int i=0; i<keys.length; i++) {
                String key = keys[i];
                String label = chartType == 0 ? key + " " + Utils.labelForBookingType(key, true) : key;
                int value = sums.get(key).intValue();
                if (i >= MAX_LEGEND-1) {
                    label = "weitere";
                    Number wval = model.getData().get(label);
                    if (wval!=null) value += wval.intValue();
                }
                model.set(label, value);
            }
            return model;
        }
        
        public String getPieChartTitle() {
            return MessageFormat.format(WebUtils.getResBundle().getString("booking_yourbookingsinmonth"), MONTH_FORMATTER.format(getCurrent().getDay()));
        }
        
        public MeterGaugeChartModel getMeterChart() {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getCurrent().getDay());
            Map<String,Number> sums = bookingEjb.getBookingSumsForMonth(
                    WebUtils.getCurrentPerson(), 
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    2);
            
            int days = sums.get("days").intValue();
            int minutes = sums.get("sum") != null ? sums.get("sum").intValue() : 0;
            int minutes8h = days * 8 * 60;
            MeterGaugeChartModel model = new MeterGaugeChartModel(days > 0 ? Math.min(((float)minutes) * 100 / minutes8h, 200) : 100, Arrays.asList((Number)100,200));
            model.setTitle(WebUtils.getResBundle().getString("booking_yourhoursvs8h"));
            model.setShowTickLabels(false);
            model.setSeriesColors("cc6666,66cc66");
            model.setLabelHeightAdjust(82);
            String overtime = (minutes > minutes8h ? "+" : (minutes == minutes8h ? "\u00B1" : "")) + getFormattedBookingTime(minutes - minutes8h);
            model.setGaugeLabel(MessageFormat.format(WebUtils.getResBundle().getString("booking_overtimeindays"), overtime, days));
            
            return model;
        }
        
        public boolean isEditingAllowed() {
            return Boolean.valueOf(WebUtils.getHttpServletRequest().isUserInRole("admin")) &&
                                        projectsEjb.getAssignedManagers(budgetsEjb.getBudget(this.currentTemplate.getBudgetId()).getProjectId()).contains(WebUtils.getCurrentPerson());
        }
        
    public List<BookingTemplate> getQuickSelectionList() {
        List<BookingTemplate> list = bookingTemplateEjb.getLastUsedByPerson(WebUtils.getCurrentPerson(), numberOfQuickSelectRows);
        numberOfQuickSelectRows = Math.max(1, list.size());
        return list;
    }
        
    public BookingTemplate getQuickSelectedBookingTemplate() {
        return null; // never show selection after onQuickSelect
    }
    
    public void setQuickSelectedBookingTemplate(BookingTemplate b) {
        // no need to remember selection
    }
    
    public void onQuickSelect(SelectEvent event) {
        setCurrentTemplate((BookingTemplate)event.getObject());
    }

    public void numberOfQuickSelectRowsInc() {
        numberOfQuickSelectRows++;
    }

    public void numberOfQuickSelectRowsDec() {
        numberOfQuickSelectRows = Math.max(1, numberOfQuickSelectRows-1);
    }
    
}
