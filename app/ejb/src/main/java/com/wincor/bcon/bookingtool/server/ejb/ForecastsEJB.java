/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.db.entity.BudgetPlan;
import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import com.wincor.bcon.bookingtool.server.db.entity.Forecast;
import com.wincor.bcon.bookingtool.server.db.entity.ForecastBudgetPlan;
import com.wincor.bcon.bookingtool.server.util.Utils;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoRowVo;
import com.wincor.bcon.bookingtool.server.vo.ForecastInfoVo;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Stateless
public class ForecastsEJB implements ForecastsEJBLocal {

    @PersistenceContext(unitName = "EJBsPU")
    private EntityManager em;
    
    @EJB
    private DomainsEJBLocal domainsEjb;
    
    @EJB
    private BudgetsEJBLocal budgetsEjb;
    
    @EJB
    private BudgetPlansEJBLocal budgetPlansEjb;
    
    protected final static int REPORT_COLUMN_YTD_WO_CURRENT_MONTH = -1;
    protected final static int REPORT_COLUMN_CURRENT_MONTH = -2;
    protected final static int REPORT_COLUMN_CURRENT_MONTH_PLUS_1 = -3;
    protected final static int REPORT_COLUMN_CURRENT_MONTH_PLUS_2 = -4;
    protected final static int REPORT_COLUMN_CURRENT_MONTH_PLUS_3_UNTIL_END = -5;
    
    protected final static int REPORT_ROW_SALES_XLE = 1;
    protected final static int REPORT_ROW_SALES_IFRS = 2;
    protected final static int REPORT_ROW_EFFORTS = 3;
    
    @Override
    @RolesAllowed({"admin","user"})
    public List<Forecast> getForecasts() {
        List<Forecast> result = new ArrayList<Forecast>();
        for (Domain domain : domainsEjb.getDomains())
            result.addAll(em.createNamedQuery("Forecast.findByDomainId", Forecast.class).setParameter("domainId", domain.getId()).getResultList());
        return result;
    }

    @Override
    @RolesAllowed({"admin","user"})
    public Forecast saveForecast(Forecast forecast, List<BudgetPlan> assignedBudgetPlans) {
        if (forecast.getId() == null) {
            em.persist(forecast);
            em.flush();
        } else {
            em.merge(forecast);
            // delete existing assignments
            em.createNamedQuery("ForecastBudgetPlan.deleteByForecastId").setParameter("forecastId", forecast.getId()).executeUpdate();
        }
        
        // save budget plan assignments:
        int index = 0;
        for (BudgetPlan b : assignedBudgetPlans) {
            ForecastBudgetPlan p = new ForecastBudgetPlan();
            p.setForecastId(forecast.getId());
            p.setBudgetPlanId(b.getId());
            p.setPosition(index++);
            em.persist(p);
        }
        
        return forecast;
    }

    @Override
    @RolesAllowed({"admin","user"})
    public void deleteForecast(int forecastId) {
        // delete existing assignments
        em.createNamedQuery("ForecastBudgetPlan.deleteByForecastId").setParameter("forecastId", forecastId).executeUpdate();
        em.remove(em.find(Forecast.class, forecastId));
    }
   
    @Override
    public List<BudgetPlan> getAssignedBudgetPlans(int forecastId) {
        List<ForecastBudgetPlan> assigned = em.createNamedQuery("ForecastBudgetPlan.findByForecastId", ForecastBudgetPlan.class).setParameter("forecastId", forecastId).getResultList();
        List<BudgetPlan> result = new ArrayList<BudgetPlan>(assigned.size());
        for (ForecastBudgetPlan p : assigned) result.add(em.find(BudgetPlan.class, p.getBudgetPlanId()));
        return result;
    }

    @Override
    public List<Integer> getMonthsForFiscalYear(int fiscalYear) {
        List<Integer> result = new ArrayList<Integer>(12);
        for (int month = 0; month < 12; month++) {
            int year = month < 3 ? fiscalYear - 1 : fiscalYear;
            int monthOfYear = month < 3 ? month + 9 : month - 3;
            int period = year * 100 + (monthOfYear+1);
            result.add(period);
        }
        return result;
    }
    
    @Override
    public ForecastInfoRowVo getForecastInfoForBudget(int forecastId, int budgetId) {
        Forecast forecast = em.find(Forecast.class, forecastId);
        ForecastInfoRowVo row = new ForecastInfoRowVo(budgetsEjb.getBudgetInfo(budgetId));
        for (int period : getMonthsForFiscalYear(forecast.getFiscalYear())) {
            ForecastInfoVo cell = new ForecastInfoVo();
            int year = period / 100;
            int monthOfYear = (period % 100) - 1;
            TimePeriod timePeriod = Utils.timePeriodForMonth(year, monthOfYear);
            cell.setPeriod(period);
            cell.setPlannedMinutes(budgetPlansEjb.getPlannedMinutesForPeriod(budgetId, period));
            cell.setBookedMinutes(budgetsEjb.getBudgetInfo(budgetId, timePeriod).getBookedMinutesRecursive());
            row.getMonths().put(period, cell);
        }
        return row;
    }

    @Override
    public ForecastInfoRowVo getForecastInfoSummaryRow(int forecastId) {
        Forecast forecast = em.find(Forecast.class, forecastId);
        List<ForecastInfoRowVo> rows = new ArrayList<ForecastInfoRowVo>();
        for (BudgetPlan p : getAssignedBudgetPlans(forecastId)) {
            rows.add(getForecastInfoForBudget(forecastId, p.getBudgetId()));
        }
        if (rows.isEmpty()) return null;
        ForecastInfoRowVo summaryRow = rows.get(0);
        for (int i=1; i<rows.size(); i++) {
            // add all values to row 0:
            for (int period : getMonthsForFiscalYear(forecast.getFiscalYear()))
                summaryRow.getMonths().get(period).add(rows.get(i).getMonths().get(period));
        }
        return summaryRow;
    }
    
    @Override
    public XSSFWorkbook exportPlanData(int forecastId) {
        final DateFormat MONTH_FORMATTER = new SimpleDateFormat("MMMMMMMM");
        Forecast forecast = em.find(Forecast.class, forecastId);
        
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        
        int rowIndex = 0;
        XSSFRow row; XSSFCell cell;
        
        int colIndex = 0;
        row = sheet.createRow(rowIndex++);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Budget"));
        
        for (int period : getMonthsForFiscalYear(forecast.getFiscalYear())) {
            TimePeriod p = Utils.timePeriodForMonth(period / 100, (period % 100)-1);
            cell = row.createCell(colIndex++);
            cell.setCellValue(new XSSFRichTextString(MONTH_FORMATTER.format(p.getFrom())));
        }
        
        for (BudgetPlan plan : getAssignedBudgetPlans(forecastId)) {
            for (Budget leaf : budgetsEjb.getLeafBudgetsForParent(plan.getBudgetId())) {
                ForecastInfoRowVo data = getForecastInfoForBudget(forecastId, leaf.getId());
                row = sheet.createRow(rowIndex++); colIndex = 0;
                cell = row.createCell(colIndex++);
                cell.setCellValue(new XSSFRichTextString(budgetsEjb.getFullBudgetName(leaf.getId())));
                for (int period : getMonthsForFiscalYear(forecast.getFiscalYear())) {
                    cell = row.createCell(colIndex++);
                    cell.setCellValue(((double)data.getMonths().get(period).getPlannedMinutes())/60); // hours
                }
            }
        }
        
        return wb;
    }
    
    /**
     * Extends the forecast summary with aggregated sums for the sales report
     * @param forecastId a forecast ID
     * @param cal report current month
     * @return info row object
     */
    protected ForecastInfoRowVo getForecastInfoReportSummary(int forecastId, Calendar cal) {
        Forecast forecast = em.find(Forecast.class, forecastId);
        ForecastInfoRowVo summaryRow = getForecastInfoSummaryRow(forecastId);
        // summary row contains sums for all months of the year.
        List<Integer> fiscalYearMonths = getMonthsForFiscalYear(forecast.getFiscalYear());
        int currentMonth = cal.get(Calendar.YEAR) * 100 + cal.get(Calendar.MONTH)+1;
        int currentMonthIndex = fiscalYearMonths.size(); // after last fiscal year month
        for (int i=0; i<fiscalYearMonths.size(); i++) {
            if (fiscalYearMonths.get(i).intValue() == currentMonth) currentMonthIndex = i;
        }
        // first, aggregate all months up to current month:
        ForecastInfoVo ytdVo = new ForecastInfoVo(REPORT_COLUMN_YTD_WO_CURRENT_MONTH, 0, 0);
        for (int i=0; i<currentMonthIndex; i++)
            ytdVo.add(summaryRow.getMonths().get(fiscalYearMonths.get(i)));
        summaryRow.getMonths().put(REPORT_COLUMN_YTD_WO_CURRENT_MONTH, ytdVo);
        
        // current month:
        ForecastInfoVo currentMonthVo = new ForecastInfoVo(REPORT_COLUMN_CURRENT_MONTH, 0, 0);
        if (currentMonthIndex < fiscalYearMonths.size())
            currentMonthVo.add(summaryRow.getMonths().get(fiscalYearMonths.get(currentMonthIndex)));
        summaryRow.getMonths().put(REPORT_COLUMN_CURRENT_MONTH, currentMonthVo);

        // current month + 1:
        ForecastInfoVo currentMonthPlus1Vo = new ForecastInfoVo(REPORT_COLUMN_CURRENT_MONTH_PLUS_1, 0, 0);
        if (currentMonthIndex+1 < fiscalYearMonths.size())
            currentMonthPlus1Vo.add(summaryRow.getMonths().get(fiscalYearMonths.get(currentMonthIndex+1)));
        summaryRow.getMonths().put(REPORT_COLUMN_CURRENT_MONTH_PLUS_1, currentMonthPlus1Vo);

        // current month + 2:
        ForecastInfoVo currentMonthPlus2Vo = new ForecastInfoVo(REPORT_COLUMN_CURRENT_MONTH_PLUS_2, 0, 0);
        if (currentMonthIndex+2 < fiscalYearMonths.size())
            currentMonthPlus2Vo.add(summaryRow.getMonths().get(fiscalYearMonths.get(currentMonthIndex+2)));
        summaryRow.getMonths().put(REPORT_COLUMN_CURRENT_MONTH_PLUS_2, currentMonthPlus2Vo);

        // current month + 3 until end:
        ForecastInfoVo currentMonthPlus3Vo = new ForecastInfoVo(REPORT_COLUMN_CURRENT_MONTH_PLUS_3_UNTIL_END, 0, 0);
        for (int i=currentMonthIndex+3; i<fiscalYearMonths.size(); i++)
            currentMonthPlus3Vo.add(summaryRow.getMonths().get(fiscalYearMonths.get(i)));
        summaryRow.getMonths().put(REPORT_COLUMN_CURRENT_MONTH_PLUS_3_UNTIL_END, currentMonthPlus3Vo);
        
        return summaryRow;
    }

    @Override
    public XSSFWorkbook createSalesReport(int forecastId) {
        Calendar reportCurrentMonth = Calendar.getInstance();
        reportCurrentMonth.add(Calendar.MONTH, -1); // last month is the report's current month
        final DateFormat MONTH_FORMATTER = new SimpleDateFormat("MMMMMMMM");
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(reportCurrentMonth.getTimeInMillis());
        String currentMonth = MONTH_FORMATTER.format(cal.getTime());
        cal.add(Calendar.MONTH, 1);
        String currentMonthPlus1 = MONTH_FORMATTER.format(cal.getTime());
        cal.add(Calendar.MONTH, 1);
        String currentMonthPlus2 = MONTH_FORMATTER.format(cal.getTime());

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();

        XSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setWrapText(true);
        headerStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        int rowIndex = 0;
        XSSFRow row;
        XSSFCell cell;
        
        int colIndex = 0;
        row = sheet.createRow(rowIndex++);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Budget"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Actual"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Forecast"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Forecast"));
        cell.setCellStyle(headerStyle);

        colIndex = 0;
        row = sheet.createRow(rowIndex++);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString(""));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Total project"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Prev. years\n2012/2013"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("YTD\nw/o curr.\nmonth"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Curr. month\n\n"+currentMonth));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Month + 1\n\n"+currentMonthPlus1));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Month + 2\n\n"+currentMonthPlus2));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Month + 3\nuntil project\nend"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Total project"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("therof curr.\nFY"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("vs. budget"));
        cell.setCellStyle(headerStyle);

        colIndex = 0;
        row = sheet.createRow(rowIndex++);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("cum. / monthly"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("cum."));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("cum."));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("cum."));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("monthly"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("monthly"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("monthly"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("cum."));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("cum."));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("cum."));
        cell.setCellStyle(headerStyle);

        colIndex = 0;
        row = sheet.createRow(rowIndex++);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("Currency"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("EUR"));
        cell.setCellStyle(headerStyle);
        cell = row.createCell(colIndex++);
        cell.setCellValue(new XSSFRichTextString("(%)"));
        cell.setCellStyle(headerStyle);
        
        ForecastInfoRowVo summaryData = getForecastInfoReportSummary(forecastId, reportCurrentMonth);

        sheet.createRow(rowIndex++);
        
        addSalesReportRow(sheet, rowIndex++, forecastId, summaryData, REPORT_ROW_SALES_XLE);

        sheet.createRow(rowIndex++);
        
        addSalesReportRow(sheet, rowIndex++, forecastId, summaryData, REPORT_ROW_SALES_IFRS);
        
        sheet.createRow(rowIndex++);
        
        addSalesReportRow(sheet, rowIndex++, forecastId, summaryData, REPORT_ROW_EFFORTS);
        
        sheet.autoSizeColumn(0);
                        
        return wb;
    }
    
    protected void addSalesReportRow(XSSFSheet sheet, int rowIndex, int forecastId, ForecastInfoRowVo summaryData, int rowType) {
        XSSFRow row = sheet.createRow(rowIndex);
        
        Forecast forecast = em.find(Forecast.class, forecastId);
        int colIndex = 0;
        
        // Row title
        XSSFCell cell = row.createCell(colIndex++);
        switch (rowType) {
            case REPORT_ROW_SALES_XLE:
                cell.setCellValue(new XSSFRichTextString("Cost of sales XLE"));
                break;
            case REPORT_ROW_SALES_IFRS:
                cell.setCellValue(new XSSFRichTextString("Cost of sales IFRS"));
                break;
            case REPORT_ROW_EFFORTS:
                cell.setCellValue(new XSSFRichTextString("Effort [d]"));
                break;
        }
        
        // Budget Total Project
        cell = row.createCell(colIndex++);
        switch (rowType) {
            case REPORT_ROW_SALES_XLE:
                cell.setCellValue(-forecast.getFcBudgetCents() / 100);
                break;
            case REPORT_ROW_SALES_IFRS:
                cell.setCellValue(-(long)forecast.getFcBudgetCents() * forecast.getCentsPerHourIfrs() / forecast.getCentsPerHour() / 100);
                break;
            case REPORT_ROW_EFFORTS:
                cell.setCellValue(forecast.getFcBudgetCents() / forecast.getCentsPerHour() / 8);
                break;
        }

        // Prev. Years
        cell = row.createCell(colIndex++);
        cell.setCellValue(0); // Prev. years always 0
        
        // Actual YTD w/o current month
        cell = row.createCell(colIndex++);
        int value = summaryData.getMonths().get(REPORT_COLUMN_YTD_WO_CURRENT_MONTH).getBookedMinutes(); // actual booked
        //if (rowType != REPORT_ROW_EFFORTS) value = 0; // Note: ACTUAL FINANCIAL DATA NOT AVAILABLE
        addSalesReportColumn(forecast, cell, value, rowType);

        // Current month (actual)
        cell = row.createCell(colIndex++);
        value = summaryData.getMonths().get(REPORT_COLUMN_CURRENT_MONTH).getBookedMinutes(); // actual booked
        //if (rowType != REPORT_ROW_EFFORTS) value = 0; // Note: ACTUAL FINANCIAL DATA NOT AVAILABLE
        addSalesReportColumn(forecast, cell, value, rowType);
        
        // Current month + 1
        cell = row.createCell(colIndex++);
        value = summaryData.getMonths().get(REPORT_COLUMN_CURRENT_MONTH_PLUS_1).getPlannedMinutes(); // planned
        value = roundForecastEffortForReport(value);
        addSalesReportColumn(forecast, cell, value, rowType);
        
        // Current month + 2
        cell = row.createCell(colIndex++);
        value = summaryData.getMonths().get(REPORT_COLUMN_CURRENT_MONTH_PLUS_2).getPlannedMinutes(); // planned
        value = roundForecastEffortForReport(value);
        addSalesReportColumn(forecast, cell, value, rowType);
        
        // Current month + 3 until end
        cell = row.createCell(colIndex++);
        value = summaryData.getMonths().get(REPORT_COLUMN_CURRENT_MONTH_PLUS_3_UNTIL_END).getPlannedMinutes(); // planned
        value = roundForecastEffortForReport(value);
        addSalesReportColumn(forecast, cell, value, rowType);

        // Total
        cell = row.createCell(colIndex++);
        cell.setCellFormula("SUM("+(char)('A'+colIndex-7)+(rowIndex+1)+":"+(char)('A'+colIndex-2)+(rowIndex+1)+")");

        // Thereof curr. FY
        cell = row.createCell(colIndex++);
        cell.setCellFormula(""+(char)('A'+colIndex-2)+(rowIndex+1)+"-"+(char)('A'+colIndex-8)+(rowIndex+1));
        
        // Forecast vs. budget
        cell = row.createCell(colIndex++);
        cell.setCellFormula(""+(char)('A'+colIndex-3)+(rowIndex+1)+"/"+(char)('A'+colIndex-10)+(rowIndex+1));
    }
    
    protected static void addSalesReportColumn(Forecast forecast, XSSFCell cell, int value, int rowType) {
        switch (rowType) {
            case REPORT_ROW_SALES_XLE:
                cell.setCellValue(-Math.round((double)value * forecast.getCentsPerHour() / 6000));
                break;
            case REPORT_ROW_SALES_IFRS:
                cell.setCellValue(-Math.round((double)value * forecast.getCentsPerHourIfrs() / 6000));
                break;
            case REPORT_ROW_EFFORTS:
                cell.setCellValue(((float)(Math.round(((float)(value * 100)) / 480)))/100);
                break;
        }
    }
    
    protected static int roundForecastEffortForReport(int minutes) {
        int modulo = minutes % 240; // half day modulo
        if (modulo < 120) minutes -= modulo; // floor
        else minutes += 240-modulo; // ceiling
        return minutes;
    }
}
