package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.ejb.BookingTemplatesEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BookingsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.server.util.Utils;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.Map;
import org.primefaces.model.chart.PieChartModel;

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
		this.currentTemplate = bt;
		this.current.setSalesRepresentative(bt.getSalesRepresentative()); // adapt Vertriebsbeauftragter
		this.current.setDescription(bt.getDescription()); // adapt description
	}
        
        public BudgetInfoVo getCurrentBudget() {
            if (currentTemplate == null) return null;
            return budgetsEjb.getBudgetInfo(currentTemplate.getBudgetId());
        }
	
	public void save() {
		current.setBookingTemplateId(currentTemplate.getId());
		bookingEjb.saveBooking(current);
		newBooking();
	}
	
	public void edit(Booking v) {
		current = v;
		currentTemplate = bookingTemplateEjb.getBookingTemplate(v.getBookingTemplateId());
	}
	
	public void delete (Booking v) {
		bookingEjb.deleteBooking(v.getId());;
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
        
        public PieChartModel getPieChart() {
            Calendar cal = Calendar.getInstance();
            cal.setTime(getCurrent().getDay());
            Map<String,Number> sums = bookingEjb.getBookingSumsForMonth(
                    WebUtils.getCurrentPerson(), 
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH));
            if (sums.isEmpty()) sums.put("keine Buchungen", 1);
            PieChartModel model = new PieChartModel();
            for (String key : sums.keySet())
                model.set(key + " " + Utils.labelForBookingType(key, true), sums.get(key));
            return model;
        }
        
        public String getPieChartTitle() {
            return "Deine Buchungen im Monat " + MONTH_FORMATTER.format(getCurrent().getDay());
        }
}
