package com.wincor.bcon.bookingtool.server.ejb;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.util.Utils;

@Stateless
public class ExcelExportEJB implements ExcelExportEJBLocal {

	@EJB
	private BookingsEJBLocal bookingEJB;
	
	@EJB
	private BookingTemplatesEJBLocal bookingTemplateEJB;
	
	@Override
	@RolesAllowed({"admin","user"})
	public XSSFWorkbook getExcelForName(String person, Integer weeksToExport) {
		
		Calendar lastExportDay = Calendar.getInstance();
		lastExportDay.add(Calendar.WEEK_OF_YEAR, -weeksToExport);
		
		List<Booking> bookingList = bookingEJB.getBookingsByLastExportDay(person, lastExportDay.getTime());
                    
		XSSFWorkbook result = getExcelForBookings(bookingList, false);
		
        for (Booking booking : bookingList)
            booking.setExportState((byte)1);  
        
        return result;
	}
		
	@Override
	@RolesAllowed({"superuser"})
	public XSSFWorkbook getExcelForAdmin(Integer monthsToExport) {
		
		Calendar lastExportDay = Calendar.getInstance();
		lastExportDay.add(Calendar.MONTH, -monthsToExport);
		lastExportDay.set(Calendar.DAY_OF_MONTH, 1);
		
		List<Booking> bookingList = bookingEJB.getBookingsByLastExportDayForSuperuser(lastExportDay.getTime());
		return getExcelForBookings(bookingList, true);
	}
		
	@Override
	@RolesAllowed({"admin"})
	public XSSFWorkbook getExcelForBudget(int budgetId) {
		List<Booking> bookingList = bookingEJB.getBookingsForBudget(budgetId);
		return getExcelForBookings(bookingList, true);
	}
		
	protected XSSFWorkbook getExcelForBookings(List<Booking> bookingList, boolean withNameColumn) {
				
		XSSFWorkbook wb = new XSSFWorkbook();
		SimpleDateFormat sdf = new SimpleDateFormat("E., dd.MM.yyyy");
		XSSFSheet sheet = wb.createSheet();
		sheet = createHeader(sheet, withNameColumn);
		
		Date lastDate = null;
		int rowPosition = sheet.getLastRowNum();
		


		for(Booking booking : bookingList)
		{
			BookingTemplate bt = bookingTemplateEJB.getBookingTemplate(booking.getBookingTemplateId());
			
                        XSSFCellStyle style = wb.createCellStyle();
                        
                        if (booking.getExportState() == 2){
                            style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        }
                        
                        if (!withNameColumn && lastDate != null && lastDate.after(booking.getDay()))
                            sheet.createRow(++rowPosition); // add empty row for each new day if person-based export

			XSSFRow row = sheet.createRow(++rowPosition);
			XSSFCell cell;
			int cellPosition = 0;
			
			//Datum
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(sdf.format(booking.getDay())));
                        cell.setCellStyle(style);

			if (withNameColumn) {
				//Person
				cell = row.createCell(cellPosition++) ;
				cell.setCellValue(new XSSFRichTextString(booking.getPerson()));
                                cell.setCellStyle(style);
			}
                        
			//PSP-Element
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(bt.getPsp()));
                        cell.setCellStyle(style);
			//Bezeichnung des PSP-Elements
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(bt.getName()));
                        cell.setCellStyle(style);
			//Tätigkeitsart
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(bt.getType()));
                        cell.setCellStyle(style);
			//Bezeichnung der Tätigkeitsart
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(Utils.labelForBookingType(bt.getType(), false)));
                        cell.setCellStyle(style);
			//Tätigkeit
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(booking.getDescription()));
                        cell.setCellStyle(style);
			//VB-Beauftragter
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(booking.getSalesRepresentative()));
                        cell.setCellStyle(style);
			//Teilprojekt
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new XSSFRichTextString(bt.getSubproject()));
                        cell.setCellStyle(style);
			//Stunden
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(((double)Math.round(booking.getMinutes().doubleValue()/60*100))/100);
                        cell.setCellStyle(style);
                        
			lastDate = booking.getDay();
                        
		}
                
                //autosize every column!
		for(int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells()-1; i++)	
                    sheet.autoSizeColumn(i);
		
		return wb;
	}
	
	protected XSSFSheet createHeader (XSSFSheet ws, boolean withNameColumn) {
            
		XSSFRow row = ws.createRow(ws.getLastRowNum());
		XSSFCell cell;
		int cellPosition = 0;

		//Datum
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("Datum"));
		
		if (withNameColumn) {
                    //Person
                    cell = row.createCell(cellPosition++) ;
                    cell.setCellValue(new XSSFRichTextString("Person"));
		}
		
		//PSP-Element
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("PSP-Element"));
		
		//Bezeichnung
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("Bezeichnung"));
		
		//Tätigkeitsart
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("Tätigkeitsart"));
		
		//Bezeichnung der Tätigkeitsart
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("Bezeichnung"));
		
		//Tätigkeit
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("Tätigkeit"));
		
		//VB-Beauftragter
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("VB-Beauftragter"));
		
		//Teilprojekt
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("Teilprojekt"));
		
		//Stunden
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new XSSFRichTextString("Stunden"));
                
		return ws;
	}
}
