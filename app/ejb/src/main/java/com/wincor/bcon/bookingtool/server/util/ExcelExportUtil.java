/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wincor.bcon.bookingtool.server.util;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.ejb.BookingTemplatesEJB;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.server.vo.SAPBooking;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Static utility methods for creating tool-specific Excel workbooks
 */
public class ExcelExportUtil {
    
    public static XSSFWorkbook createWorkbookForBookings(BookingTemplatesEJB bookingTemplateEJB, List<Booking> bookingList, boolean withNameColumn) {

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        sheet = createHeaderSheetForBookings(sheet, withNameColumn);

        Date lastDate = null;
        int rowPosition = sheet.getLastRowNum();

        for(Booking booking : bookingList)
        {
            BookingTemplate bt = bookingTemplateEJB.getBookingTemplate(booking.getBookingTemplateId());

            for (SAPBooking sapBooking : SAPBooking.createSAPBookingsForBooking(booking, bt)) {
                
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
                cell.setCellValue(new XSSFRichTextString(sapBooking.day));
                cell.setCellStyle(style);

                if (withNameColumn) {
                        //Person
                        cell = row.createCell(cellPosition++) ;
                        cell.setCellValue(new XSSFRichTextString(sapBooking.person));
                        cell.setCellStyle(style);
                }

                //PSP-Element
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(new XSSFRichTextString(sapBooking.psp));
                cell.setCellStyle(style);
                //Bezeichnung des PSP-Elements
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(new XSSFRichTextString(sapBooking.pspLabel));
                cell.setCellStyle(style);
                //Tätigkeitsart
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(new XSSFRichTextString(sapBooking.type));
                cell.setCellStyle(style);
                //Bezeichnung der Tätigkeitsart
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(new XSSFRichTextString(sapBooking.typeLabel));
                cell.setCellStyle(style);
                //Tätigkeit
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(new XSSFRichTextString(sapBooking.description));
                cell.setCellStyle(style);
                //VB-Beauftragter
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(new XSSFRichTextString(sapBooking.salesRepresentative));
                cell.setCellStyle(style);
                //Teilprojekt
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(new XSSFRichTextString(sapBooking.subproject));
                cell.setCellStyle(style);
                //Stunden
                cell = row.createCell(cellPosition++) ;
                cell.setCellValue(((double)sapBooking.hundredthHours)/100);
                cell.setCellStyle(style);

                lastDate = booking.getDay();

            }
        }

        //autosize every column!
        for(int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells()-1; i++)	
            sheet.autoSizeColumn(i);

        return wb;
    }

    protected static XSSFSheet createHeaderSheetForBookings(XSSFSheet ws, boolean withNameColumn) {

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

    public static XSSFWorkbook createWorkbookForBudgets(List<BudgetInfoVo> budgets) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = createHeaderSheetForBudgets(wb.createSheet());
        int rowPosition = sheet.getLastRowNum();

        for(BudgetInfoVo b : budgets)
        {
            XSSFRow row = sheet.createRow(++rowPosition);
            XSSFCell cell;
            int cellPosition = 0;

            //ID
            cell = row.createCell(cellPosition++);
            cell.setCellValue(b.getBudget().getId());
            
            //Name
            cell = row.createCell(cellPosition++);
            cell.setCellValue(new XSSFRichTextString(b.getFullBudgetName() != null ? b.getFullBudgetName() : b.getBudget().getName()));
            
            //Budget
            cell = row.createCell(cellPosition++);
            cell.setCellValue(((double)Math.round(((double)Math.abs(b.getBudget().getMinutes()))/60*100))/100);
            
            //Used
            cell = row.createCell(cellPosition++);
            cell.setCellValue(((double)Math.round(((double)b.getBookedMinutesRecursive())/60*100))/100);
        }
        // autosize columns
        for(int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells()-1; i++)	
            sheet.autoSizeColumn(i);
        return wb;
    }
    
    protected static XSSFSheet createHeaderSheetForBudgets(XSSFSheet ws) {

        XSSFRow row = ws.createRow(ws.getLastRowNum());
        XSSFCell cell;
        int cellPosition = 0;

        //ID
        cell = row.createCell(cellPosition++) ;
        cell.setCellValue(new XSSFRichTextString("ID"));

        //Name
        cell = row.createCell(cellPosition++) ;
        cell.setCellValue(new XSSFRichTextString("Name"));

        //Budget Time
        cell = row.createCell(cellPosition++) ;
        cell.setCellValue(new XSSFRichTextString("Budget [hours]"));

        //Used Time
        cell = row.createCell(cellPosition++) ;
        cell.setCellValue(new XSSFRichTextString("Used [hours]"));

        return ws;
    }

    public static XSSFWorkbook createWorkbookForTemplates(List<BookingTemplate> templates) {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        int rowPosition = 0;

        for(BookingTemplate t : templates)
        {
            XSSFRow row = sheet.createRow(rowPosition++);
            XSSFCell cell;
            int cellPosition = 0;

            //ID
            cell = row.createCell(cellPosition++);
            cell.setCellValue(t.getId());
            
            //PSP
            cell = row.createCell(cellPosition++);
            cell.setCellValue(new XSSFRichTextString(t.getPsp()));
            
            //Name
            cell = row.createCell(cellPosition++);
            cell.setCellValue(new XSSFRichTextString(t.getName()));
            
            //Description
            cell = row.createCell(cellPosition++);
            cell.setCellValue(new XSSFRichTextString(t.getDescription()));
            
            //AdditionalInfo
            cell = row.createCell(cellPosition++);
            cell.setCellValue(new XSSFRichTextString(t.getAdditionalInfo()));
        }
        // autosize columns
        for(int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells()-1; i++)	
            sheet.autoSizeColumn(i);
        return wb;
    }
}
