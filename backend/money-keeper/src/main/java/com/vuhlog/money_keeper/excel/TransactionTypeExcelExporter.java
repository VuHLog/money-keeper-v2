package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportTransactionTypeReponse;
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
import java.text.ParseException;
import java.util.*;

public class TransactionTypeExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ReportTransactionTypeReponse> data;
    private ReportFilterOptionsRequest request;

    public TransactionTypeExcelExporter(List<ReportTransactionTypeReponse> data) {
        this.data = data;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Báo cáo giao dịch");
        
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
        
        // Tạo tiêu đề với khoảng thời gian từ customTimeRange
        String timeRangeTitle = "";
        if(request != null){
            timeRangeTitle = createTimeRangeTitle(request.getCustomTimeRange());
            if(request.getTimeOption().equals(ReportTimeOptionType.MONTH.getType())){
                timeRangeTitle = " THEO THÁNG " + timeRangeTitle;
            }else if (request.getTimeOption().equals(ReportTimeOptionType.YEAR.getType())) {
                timeRangeTitle = " THEO NĂM " + timeRangeTitle;
            }
        }
        header.setCenter("BÁO CÁO" + timeRangeTitle);

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
            String[] customTimeRangeFirst = customTimeRange.get(0).split("-");
            ArrayUtils.reverse(customTimeRangeFirst);
            String startDate = String.join("/",customTimeRangeFirst);
            String[] customTimeRangeSecond = customTimeRange.get(1).split("-");
            ArrayUtils.reverse(customTimeRangeSecond);
            String endDate = String.join("/",customTimeRangeSecond);
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
                "Thời gian", "Thu", "Chi", "Số giao dịch",
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private Set<String> generateAllTimePoints(ReportFilterOptionsRequest request) {
        Set<String> allTimePoints = new TreeSet<>();
        
        if (request == null || request.getCustomTimeRange() == null || request.getCustomTimeRange().size() < 2) {
            return allTimePoints;
        }
        
        String startDate = request.getCustomTimeRange().get(0);
        String endDate = request.getCustomTimeRange().get(1);
        
        try {
            if (request.getTimeOption().equals(ReportTimeOptionType.MONTH.getType())) {
                // Định dạng YYYY-MM -> MM/YYYY
                int startYear = Integer.parseInt(startDate.substring(0, 4));
                int startMonth = Integer.parseInt(startDate.substring(5, 7));
                int endYear = Integer.parseInt(endDate.substring(0, 4));
                int endMonth = Integer.parseInt(endDate.substring(5, 7));
                
                int year = startYear;
                int month = startMonth;
                
                while (year < endYear || (year == endYear && month <= endMonth)) {
                    String formattedMonth = String.format("%02d", month);
                    allTimePoints.add(formattedMonth + "/" + year);
                    
                    month++;
                    if (month > 12) {
                        month = 1;
                        year++;
                    }
                }
            } else if (request.getTimeOption().equals(ReportTimeOptionType.YEAR.getType())) {
                // Định dạng YYYY
                int startYear = Integer.parseInt(startDate.substring(0, 4));
                int endYear = Integer.parseInt(endDate.substring(0, 4));
                
                for (int year = startYear; year <= endYear; year++) {
                    allTimePoints.add(String.valueOf(year));
                }
            } else if (request.getTimeOption().equals(ReportTimeOptionType.OPTIONAL.getType())) {
                // Định dạng YYYY-MM-DD -> DD/MM/YYYY
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                
                Date start = inputFormat.parse(startDate);
                Date end = inputFormat.parse(endDate);
                
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(start);
                
                while (!calendar.getTime().after(end)) {
                    allTimePoints.add(outputFormat.format(calendar.getTime()));
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return allTimePoints;
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
        Map<String, ReportTransactionTypeReponse> dataMap = new HashMap<>();
        for (ReportTransactionTypeReponse transaction : data) {
            String time = transaction.getTime();
            if(request.getTimeOption().equals(ReportTimeOptionType.OPTIONAL.getType())) {
                String[] timeArr = time.split("-");
                ArrayUtils.reverse(timeArr);
                time = String.join("/", timeArr);
            }
            dataMap.put(time, transaction);
        }

        // Tạo bộ thời gian đầy đủ từ customTimeRange thay vì chỉ từ dữ liệu
        Set<String> allTimePoints = generateAllTimePoints(request);
        
        // Nếu không có thời gian nào được tạo (có thể do không có request), sử dụng thời gian từ dữ liệu
        if (allTimePoints.isEmpty()) {
            for (ReportTransactionTypeReponse transaction : data) {
                allTimePoints.add(transaction.getTime());
            }
        }

        for (String time : allTimePoints) {
            Row row = sheet.createRow(rowCount);

            Cell cell = row.createCell(0);
            cell.setCellValue(time);
            cell.setCellStyle(rightBaseStyle);
            sheet.autoSizeColumn(0);

            ReportTransactionTypeReponse transaction = dataMap.get(time);
            
            // Thu nhập
            cell = row.createCell(1);
            cell.setCellValue(transaction != null ? transaction.getTotalRevenue() : 0);
            cell.setCellStyle(amountStyle);
            sheet.autoSizeColumn(1);

            // Chi tiêu
            cell = row.createCell(2);
            cell.setCellValue(transaction != null ? transaction.getTotalExpense() : 0);
            cell.setCellStyle(amountStyle);
            sheet.autoSizeColumn(2);

            // Số giao dịch
            cell = row.createCell(3);
            cell.setCellValue(transaction != null ? transaction.getTotalTransaction() : 0);
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(3);

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

        Long totalExpense = 0L;
        Long totalRevenue = 0L;

        for (ReportTransactionTypeReponse transaction : data) {
            totalExpense += transaction.getTotalExpense();
            totalRevenue += transaction.getTotalRevenue();
        }

        Cell cell = row.createCell(4);
        cell.setCellValue(totalRevenue - totalExpense);
        cell.setCellStyle(totalAmountStyle);

        CellStyle totalExpenseStyle = workbook.createCellStyle();
        totalExpenseStyle.cloneStyleFrom(summaryStyle);

        CellStyle totalRevenueStyle = workbook.createCellStyle();
        totalRevenueStyle.cloneStyleFrom(summaryStyle);

        row = sheet.createRow(rowIndex + 2);
        cell = row.createCell(0);
        cell.setCellValue("Tổng chi:");
        cell.setCellStyle(totalExpenseStyle);

        cell = row.createCell(1);
        cell.setCellValue(totalExpense);
        cell.setCellStyle(totalAmountStyle);

        row = sheet.createRow(rowIndex + 3);
        cell = row.createCell(0);
        cell.setCellValue("Tổng thu:");
        cell.setCellStyle(totalRevenueStyle);

        cell = row.createCell(1);
        cell.setCellValue(totalRevenue);
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
        titleCell.setCellValue("BÁO CÁO THỐNG KÊ GIAO DỊCH");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

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
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 13));

            // Thêm dòng trống sau tiêu đề
            sheet.createRow(2);
        } else {
            // Nếu không có request, chỉ thêm dòng trống sau tiêu đề
            sheet.createRow(1);
        }
    }

    public ByteArrayInputStream export(ReportFilterOptionsRequest request) throws IOException {
        this.request = request;
        
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