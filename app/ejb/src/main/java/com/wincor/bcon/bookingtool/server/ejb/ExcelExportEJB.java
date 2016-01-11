package com.wincor.bcon.bookingtool.server.ejb;

import java.util.Calendar;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.util.ExcelExportUtil;

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
		
	@RolesAllowed({"admin"})
	public XSSFWorkbook getExcelForProject(Integer projectToExport) {
		List<Booking> bookingList = bookingEJB.getBookingsForProject(projectToExport);
		return ExcelExportUtil.createWorkbookForBookings(bookingTemplateEJB, bookingList, true);
        }
        
	@RolesAllowed({"superuser"})
	public XSSFWorkbook getExcelForAdmin(Integer monthsToExport) {
		
		Calendar lastExportDay = Calendar.getInstance();
		lastExportDay.add(Calendar.MONTH, -monthsToExport);
		lastExportDay.set(Calendar.DAY_OF_MONTH, 1);
		
		List<Booking> bookingList = bookingEJB.getBookingsByLastExportDayForSuperuser(lastExportDay.getTime());
		return ExcelExportUtil.createWorkbookForBookings(bookingTemplateEJB, bookingList, true);
	}
		
	@RolesAllowed({"admin"})
	public XSSFWorkbook getExcelForBudget(int budgetId) {
		List<Booking> bookingList = bookingEJB.getBookingsForBudget(budgetId);
		return ExcelExportUtil.createWorkbookForBookings(bookingTemplateEJB, bookingList, true);
	}
}
