package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportDailyTrend;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DailyTrendExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ReportDailyTrend> data;
    private ReportFilterOptionsRequest request;
    private List<String> timeArr = new ArrayList<>();
    private String transactionType = "";

    public DailyTrendExcelExporter(List<ReportDailyTrend> data) {
        this.data = data;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Xu hướng chi tiêu theo danh mục");
        this.timeArr.addAll(Arrays.asList(ArrayUtils.toArray("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31")));
        
        // Thiết lập thuộc tính trang mà không có thông tin customTimeRange
        setupPageProperties(null);
    }

    private void setupPageProperties(ReportFilterOptionsRequest request) {
        // Cài đặt các thuộc tính trang
        sheet.setFitToPage(true);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        printSetup.setFitHeight((short)0);
        printSetup.setFitWidth((short)1);

        // Thiết lập lề trang
        sheet.setMargin(Sheet.LeftMargin, 0.5);
        sheet.setMargin(Sheet.RightMargin, 0.5);
        sheet.setMargin(Sheet.TopMargin, 0.7);
        sheet.setMargin(Sheet.BottomMargin, 0.7);

        // Thiết lập header và footer
        Header header = sheet.getHeader();
        header.setLeft("MONEY KEEPER");

        header.setCenter("BÁO CÁO XU HƯỚNG " + this.transactionType.toUpperCase() + " THEO NGÀY");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        header.setRight("Ngày xuất: " + dateFormat.format(new Date()));

        Footer footer = sheet.getFooter();
        footer.setLeft("Money Keeper - Quản lý chi tiêu cá nhân");
        footer.setCenter("");
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
                "Ngày", this.transactionType
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeDataRows() {
        CellStyle baseStyle = createBaseStyle();
        CellStyle rightBaseStyle = workbook.createCellStyle();
        rightBaseStyle.cloneStyleFrom(baseStyle);
        rightBaseStyle.setAlignment(HorizontalAlignment.RIGHT);
        CellStyle amountStyle = createAmountStyle();

        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }
        int rowCount = headerRowIndex;

        // Tạo Map để lưu trữ dữ liệu
        Map<String, ReportDailyTrend> dataMap = new HashMap<>();
        for (ReportDailyTrend report : data) {
            String time = report.getDayOfMonth();
            dataMap.put(time, report);
        }

        for (String time : timeArr) {
            Row row = sheet.createRow(rowCount);

            Cell cell = row.createCell(0);
            cell.setCellValue(time);
            cell.setCellStyle(rightBaseStyle);
            sheet.autoSizeColumn(0);

            ReportDailyTrend reportDailyTrend = dataMap.get(time);

            cell = row.createCell(1);
            cell.setCellValue(reportDailyTrend != null ? reportDailyTrend.getTotal() : 0);
            cell.setCellStyle(amountStyle);
            sheet.autoSizeColumn(1);

            rowCount++;
        }

        if (!data.isEmpty()) {
            addSummaryRow(rowCount);
        }
    }

    private void addSummaryRow(int rowIndex) {
        Row row = sheet.createRow(rowIndex + 2);
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

        Long total = 0L;

        for (ReportDailyTrend reportDailyTrend : data) {
            total += reportDailyTrend.getTotal();
        }

        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.cloneStyleFrom(summaryStyle);

        row = sheet.createRow(rowIndex + 2);
        Cell cell = row.createCell(0);
        cell.setCellValue("Tổng " + this.transactionType.toLowerCase() + " :");
        cell.setCellStyle(totalStyle);
        sheet.autoSizeColumn(0);

        cell = row.createCell(1);
        cell.setCellValue(total);
        cell.setCellStyle(totalAmountStyle);
        sheet.autoSizeColumn(1);
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
        titleCell.setCellValue("BÁO CÁO XU HƯỚNG " + this.transactionType.toUpperCase() + " THEO NGÀY");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));
        sheet.createRow(1);
    }

    public ByteArrayInputStream export(ReportFilterOptionsRequest request) throws IOException {
        this.request = request;
        this.transactionType = (request.getTransactionType() != null && request.getTransactionType().equals(TransactionType.EXPENSE.getType()))? "Chi tiêu":"Thu nhập";
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