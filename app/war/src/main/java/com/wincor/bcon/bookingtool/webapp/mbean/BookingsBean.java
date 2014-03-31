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
import com.wincor.bcon.bookingtool.server.util.Utils;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.Arrays;

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
	
	private Booking current;
	private Booking selected;
	
	private BookingTemplate currentTemplate = null;
	
	private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("EEEEE", Locale.GERMANY);
	private final static DateFormat MONTH_FORMATTER = new SimpleDateFormat("MMMMM", Locale.GERMANY);
	
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
            return "Deine Buchungen im Monat " + MONTH_FORMATTER.format(getCurrent().getDay());
        }
}
