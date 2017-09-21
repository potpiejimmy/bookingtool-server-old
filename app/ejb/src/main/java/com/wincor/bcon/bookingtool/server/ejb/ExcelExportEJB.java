package com.wincor.bcon.bookingtool.server.ejb;

import java.util.Calendar;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.util.ExcelExportUtil;
import java.util.Date;

@Stateless
public class ExcelExportEJB {

	@EJB
	private BookingsEJB bookingEJB;
	
	@EJB
	private BookingTemplatesEJB bookingTemplateEJB;
	
	@RolesAllowed({"admin","user"})
	public XSSFWorkbook getExcelForName(String person, Integer weeksToExport) {
		
            Calendar lastExportDay = Calendar.getInstance();
            lastExportDay.add(Calendar.WEEK_OF_YEAR, -weeksToExport);

            List<Booking> bookingList = bookingEJB.getBookingsByLastExportDay(person, lastExportDay.getTime());

            XSSFWorkbook result = ExcelExportUtil.createWorkbookForBookings(bookingTemplateEJB, bookingList, false);
		
            for (Booking booking : bookingList)
                booking.setExportState((byte)1);  

            return result;
	}
		
	@RolesAllowed({"admin","user"})
	public XSSFWorkbook getExcelForNamePpm(String person, int week, int year) {
		
            Calendar fromDay = Calendar.getInstance();
            fromDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            fromDay.set(Calendar.WEEK_OF_YEAR, week);
            fromDay.set(Calendar.YEAR, year);
            
            Calendar toDay = Calendar.getInstance();
            toDay.setTimeInMillis(fromDay.getTimeInMillis());
            toDay.add(Calendar.WEEK_OF_YEAR, 1);

            List<Object[]> bookingList = bookingEJB.getBookingSumsForDayOfWeekAndPerson(person, fromDay.getTime(), toDay.getTime());

            XSSFWorkbook result = ExcelExportUtil.createWorkbookForPpm(bookingTemplateEJB, bookingList, fromDay);
		
            return result;
	}
		
        protected static Date startDayForMonthsToExport(int monthsToExport) {
		Calendar lastExportDay = Calendar.getInstance();
		lastExportDay.add(Calendar.MONTH, -monthsToExport);
		lastExportDay.set(Calendar.DAY_OF_MONTH, 1);
                lastExportDay.set(Calendar.HOUR_OF_DAY, 0);
                lastExportDay.set(Calendar.MINUTE, 0);
                lastExportDay.set(Calendar.SECOND, 0);
                lastExportDay.set(Calendar.MILLISECOND, 0);
                return lastExportDay.getTime();
        }
        
	@RolesAllowed({"admin"})
	public XSSFWorkbook getExcelForProject(Integer projectToExport, Integer monthsToExport) {
		List<Booking> bookingList = bookingEJB.getBookingsForProject(projectToExport, startDayForMonthsToExport(monthsToExport));
		return ExcelExportUtil.createWorkbookForBookings(bookingTemplateEJB, bookingList, true);
        }
        
	@RolesAllowed({"superuser"})
	public XSSFWorkbook getExcelForAdmin(Integer monthsToExport) {
		List<Booking> bookingList = bookingEJB.getBookingsByLastExportDayForSuperuser(startDayForMonthsToExport(monthsToExport));
		return ExcelExportUtil.createWorkbookForBookings(bookingTemplateEJB, bookingList, true);
	}
		
	@RolesAllowed({"admin"})
	public XSSFWorkbook getExcelForBudget(int budgetId) {
		List<Booking> bookingList = bookingEJB.getBookingsForBudget(budgetId);
		return ExcelExportUtil.createWorkbookForBookings(bookingTemplateEJB, bookingList, true);
	}
        
	@RolesAllowed({"user"})
	public XSSFWorkbook getExcelForGrindstone() {
		List<BookingTemplate> templates = bookingTemplateEJB.findBookingTemplates("%");
		return ExcelExportUtil.createWorkbookForTemplates(templates);
	}
}
