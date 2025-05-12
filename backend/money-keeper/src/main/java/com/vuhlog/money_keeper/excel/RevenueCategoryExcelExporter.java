package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportExpenseCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportRevenueCategory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RevenueCategoryExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ReportRevenueCategory> data;

    public RevenueCategoryExcelExporter(List<ReportRevenueCategory> data) {
        this.data = data;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Danh mục thu nhập");

        setupPageProperties(null);
    }

    private void setupPageProperties(ReportFilterOptionsRequest request) {
        sheet.setFitToPage(true);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        printSetup.setFitHeight((short)0);
        printSetup.setFitWidth((short)1);

        sheet.setMargin(Sheet.LeftMargin, 0.5);
        sheet.setMargin(Sheet.RightMargin, 0.5);
        sheet.setMargin(Sheet.TopMargin, 0.7);
        sheet.setMargin(Sheet.BottomMargin, 0.7);

        Header header = sheet.getHeader();
        header.setLeft("MONEY KEEPER");

        String timeRangeTitle = "";
        if(request != null){
            timeRangeTitle = createTimeRangeTitle(request.getCustomTimeRange());
            if(request.getTimeOption().equals(ReportTimeOptionType.MONTH.getType())){
                timeRangeTitle = " THEO THÁNG " + timeRangeTitle;
            }else if (request.getTimeOption().equals(ReportTimeOptionType.YEAR.getType())) {
                timeRangeTitle = " THEO NĂM " + timeRangeTitle;
            }
        }
        header.setCenter("BÁO CÁO DANH MỤC THU NHẬP" + timeRangeTitle);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        header.setRight("Ngày xuất: " + dateFormat.format(new Date()));

        Footer footer = sheet.getFooter();
        footer.setLeft("Money Keeper - Quản lý chi tiêu cá nhân");
        footer.setCenter("");
    }

    private String createTimeRangeTitle(List<String> customTimeRange) {
        if (customTimeRange == null || customTimeRange.isEmpty() || customTimeRange.size() < 2) {
            return "";
        }
        
        try {
            String startDate = customTimeRange.get(0);
            String endDate = customTimeRange.get(1);
            if(startDate.equals(endDate)) {
                return startDate;
            }
            return "\nTỪ " + startDate + " ĐẾN " + endDate;
        } catch (Exception e) {
            return "";
        }
    }

    private CellStyle createBaseStyle() {
        CellStyle style = workbook.createCellStyle();

        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        font.setFontName("Arial");
        style.setFont(font);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);

        return style;
    }

    private CellStyle createAmountStyle() {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(createBaseStyle());

        style.setAlignment(HorizontalAlignment.RIGHT);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));

        return style;
    }

    private void writeHeaderRow() {
        // Tìm dòng đầu tiên trống để thêm header
        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }

        Row row = sheet.createRow(headerRowIndex);
        row.setHeightInPoints(25);

        CellStyle headerStyle = workbook.createCellStyle();

        headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeight(12);
        headerFont.setFontName("Arial");
        headerStyle.setFont(headerFont);

        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);

        String[] headers = {
                "Danh mục thu", "Tổng tiền",
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeDataRows() {
        CellStyle baseStyle = createBaseStyle();
        CellStyle amountStyle = createAmountStyle();

        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }
        int rowCount = headerRowIndex;

        for (ReportRevenueCategory category : data) {
            Row row = sheet.createRow(rowCount++);

            // Category ID and Name
            Cell cell = row.createCell(0);
            cell.setCellValue(category.getCategoryName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(0);

            // Amount
            cell = row.createCell(1);
            cell.setCellValue(category.getTotalRevenue());
            cell.setCellStyle(amountStyle);
            sheet.autoSizeColumn(1);
        }

        if (!data.isEmpty()) {
            addSummaryRow(rowCount);
        }
    }

    private void addSummaryRow(int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        row.setHeightInPoints(25);

        CellStyle summaryStyle = workbook.createCellStyle();
        summaryStyle.cloneStyleFrom(createBaseStyle());

        XSSFFont boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeight(12);
        boldFont.setFontName("Arial");
        summaryStyle.setFont(boldFont);

        summaryStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle totalAmountStyle = workbook.createCellStyle();
        totalAmountStyle.cloneStyleFrom(summaryStyle);
        totalAmountStyle.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        totalAmountStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));

        double totalExpense = 0;
        for (ReportRevenueCategory category : data) {
            totalExpense += category.getTotalRevenue();
        }

        Cell cell = row.createCell(0);
        cell.setCellValue("TỔNG CỘNG");
        cell.setCellStyle(summaryStyle);

        cell = row.createCell(1);
        cell.setCellValue(totalExpense);
        cell.setCellStyle(totalAmountStyle);
    }

    private void addTitleToSheet(ReportFilterOptionsRequest request) {
        // Tạo style cho title
        CellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeight(16); // Font lớn hơn cho title
        titleFont.setFontName("Arial");
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // Tạo style cho subtitle
        CellStyle subtitleStyle = workbook.createCellStyle();
        XSSFFont subtitleFont = workbook.createFont();
        subtitleFont.setBold(true);
        subtitleFont.setFontHeight(12);
        subtitleFont.setFontName("Arial");
        subtitleStyle.setFont(subtitleFont);
        subtitleStyle.setAlignment(HorizontalAlignment.CENTER);

        // Tạo dòng tiêu đề chính
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DANH MỤC THU NHẬP");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        // Nếu có request, tạo dòng phụ đề với thông tin khoảng thời gian
        if (request != null && request.getCustomTimeRange() != null) {
            Row subtitleRow = sheet.createRow(1);
            subtitleRow.setHeightInPoints(25);
            Cell subtitleCell = subtitleRow.createCell(0);

            String timeRangeTitle = "";
            timeRangeTitle = createTimeRangeTitle(request.getCustomTimeRange());

            if(request.getTimeOption() != null) {
                if(request.getTimeOption().equals(ReportTimeOptionType.MONTH.getType())){
                    timeRangeTitle = "THEO THÁNG " + timeRangeTitle;
                } else if (request.getTimeOption().equals(ReportTimeOptionType.YEAR.getType())) {
                    timeRangeTitle = "THEO NĂM " + timeRangeTitle;
                } else if (request.getTimeOption().equals(ReportTimeOptionType.OPTIONAL.getType())) {
                    timeRangeTitle = "THEO NGÀY " + timeRangeTitle;
                }
            }

            subtitleCell.setCellValue(timeRangeTitle);
            subtitleCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

            // Thêm dòng trống sau tiêu đề
            sheet.createRow(2);
        } else {
            // Nếu không có request, chỉ thêm dòng trống sau tiêu đề
            sheet.createRow(1);
        }
    }

    public ByteArrayInputStream export(ReportFilterOptionsRequest request) throws IOException {
        if (request != null) {
            setupPageProperties(request);
        }
        
        addTitleToSheet(request);
        
        // Viết header và dữ liệu
        writeHeaderRow();
        writeDataRows();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}