package com.wincor.bcon.bookingtool.server.ejb;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import java.text.NumberFormat;
import java.util.Locale;

@Stateless
public class ExcelExportEJB implements ExcelExportEJBLocal {

	@EJB
	private BookingsEJBLocal bookingEJB;
	
	@EJB
	private BookingTemplatesEJBLocal bookingTemplateEJB;
	
	@Override
	@RolesAllowed({"admin","user"})
	public HSSFWorkbook getExcelForName(String person) {
		List<Booking> bookingList = bookingEJB.getBookings(person);
		return getExcelForBookings(bookingList, false);
	}
		
	@Override
	@RolesAllowed({"superuser"})
	public HSSFWorkbook getExcelForAdmin() {
		List<Booking> bookingList = bookingEJB.getBookings();
		return getExcelForBookings(bookingList, true);
	}
		
	@Override
	@RolesAllowed({"admin"})
	public HSSFWorkbook getExcelForBudget(int budgetId) {
		List<Booking> bookingList = bookingEJB.getBookingsForBudget(budgetId);
		return getExcelForBookings(bookingList, true);
	}
		
	protected HSSFWorkbook getExcelForBookings(List<Booking> bookingList, boolean withNameColumn) {
				
		HSSFWorkbook wb = new HSSFWorkbook();
		SimpleDateFormat sdf = new SimpleDateFormat("E., dd.MM.yyyy");
		NumberFormat df = NumberFormat.getInstance(Locale.GERMANY);
                df.setMaximumFractionDigits(2);
		HSSFSheet sheet = wb.createSheet();
		sheet = createHeader(sheet, withNameColumn);
		
		int rowPosition = sheet.getLastRowNum();
		
		for(Booking booking : bookingList)
		{
			BookingTemplate bt = bookingTemplateEJB.getBookingTemplate(booking.getBookingTemplateId());
			
			HSSFRow row = sheet.createRow(++rowPosition);
			HSSFCell cell;
			int cellPosition = 0;
			
			//Datum
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString(sdf.format(booking.getDay())));

			if (withNameColumn) {
				//Person
				cell = row.createCell(cellPosition++) ;
				cell.setCellValue(new HSSFRichTextString(booking.getPerson()));
			}

			//PSP-Element
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString(bt.getPsp()));
			
			//Bezeichnung des PSP-Elements
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString(bt.getName()));
			
			//Tätigkeitsart
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString(bt.getType()));
			
			//Bezeichnung der Tätigkeitsart
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString("0W".equals(bt.getType()) ? "Arbeitszeit" : "1T".equals(bt.getType()) ? "Reisezeit" : "nicht produktive Tätigkeiten"));
			
			//Tätigkeit
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString(booking.getDescription()));
			
			//VB-Beauftragter
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString(booking.getSalesRepresentative()));
			
			//Teilprojekt
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString(bt.getSubproject()));
			
			//Stunden
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(df.format(((float)Math.round(booking.getMinutes().doubleValue()/60*100))/100));
		}
		
		//autosize every column!
		for(int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells()-1; i++)	
			sheet.autoSizeColumn(i);
	
		return wb;
	}
	
	protected HSSFSheet createHeader (HSSFSheet ws, boolean withNameColumn) {
				
		HSSFRow row = ws.createRow(ws.getLastRowNum());
		HSSFCell cell;
		int cellPosition = 0;

		//Datum
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("Datum"));
		
		if (withNameColumn) {
			//Person
			cell = row.createCell(cellPosition++) ;
			cell.setCellValue(new HSSFRichTextString("Person"));
		}
		
		//PSP-Element
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("PSP-Element"));
		
		//Bezeichnung
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("Bezeichnung"));
		
		//Tätigkeitsart
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("Tätigkeitsart"));
		
		//Bezeichnung der Tätigkeitsart
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("Bezeichnung"));
		
		//Tätigkeit
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("Tätigkeit"));
		
		//VB-Beauftragter
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("VB-Beauftragter"));
		
		//Teilprojekt
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("Teilprojekt"));
		
		//Stunden
		cell = row.createCell(cellPosition++) ;
		cell.setCellValue(new HSSFRichTextString("Stunden"));
		
		return ws;
	}
}
