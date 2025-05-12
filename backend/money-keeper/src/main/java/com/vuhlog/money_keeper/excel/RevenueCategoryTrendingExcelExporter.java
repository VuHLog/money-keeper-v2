package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportExpenseCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportRevenueCategory;
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

public class RevenueCategoryTrendingExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ReportRevenueCategory> data;
    private ReportFilterOptionsRequest request;

    public RevenueCategoryTrendingExcelExporter(List<ReportRevenueCategory> data) {
        this.data = data;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Xu hướng thu nhập theo danh mục");

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
        header.setCenter("BÁO CÁO XU HƯỚNG THU NHẬP THEO DANH MỤC THU" + timeRangeTitle);

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
        // Thu thập tất cả các danh mục riêng biệt từ dữ liệu
        Map<String, String> uniqueCategories = new LinkedHashMap<>();
        for (ReportRevenueCategory item : data) {
            uniqueCategories.put(item.getCategoryId(), item.getCategoryName());
        }

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

        // Tạo header "Thời gian" đầu tiên
        Cell timeCell = row.createCell(0);
        timeCell.setCellValue("Thời gian");
        timeCell.setCellStyle(headerStyle);
        sheet.autoSizeColumn(0);

        // Thêm các tiêu đề danh mục
        int colIndex = 1;
        for (String categoryName : uniqueCategories.values()) {
            Cell cell = row.createCell(colIndex++);
            cell.setCellValue(categoryName);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(colIndex - 1);
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
        Map<String, String> uniqueCategories = new LinkedHashMap<>();
        for (ReportRevenueCategory item : data) {
            uniqueCategories.put(item.getCategoryId(), item.getCategoryName());
        }

        CellStyle baseStyle = createBaseStyle();
        CellStyle rightStyle = workbook.createCellStyle();
        rightStyle.cloneStyleFrom(baseStyle);
        rightStyle.setAlignment(HorizontalAlignment.RIGHT);
        CellStyle amountStyle = createAmountStyle();

        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }
        int rowCount = headerRowIndex;

        // [time,[categoryId, amount]]
        Map<String, Map<String, Long>> dataMap = new HashMap<>();

        for (ReportRevenueCategory item : data) {
            String time = item.getTime();
            if(request.getTimeOption().equals(ReportTimeOptionType.OPTIONAL.getType())) {
                String[] timeArr = time.split("-");
                ArrayUtils.reverse(timeArr);
                time = String.join("/", timeArr);
            }
            String categoryId = item.getCategoryId();
            Long amount = item.getTotalRevenue();

            if (!dataMap.containsKey(time)) {
                dataMap.put(time, new HashMap<>());
            }

            dataMap.get(time).put(categoryId, amount);
        }

        // Tạo bộ thời gian đầy đủ từ customTimeRange thay vì chỉ từ dữ liệu
        Set<String> allTimePoints = generateAllTimePoints(request);
        
        // Nếu không có thời gian nào được tạo (có thể do không có request), sử dụng thời gian từ dữ liệu
        if (allTimePoints.isEmpty()) {
            for (ReportRevenueCategory item : data) {
                allTimePoints.add(item.getTime());
            }
        }

        for (String time : allTimePoints) {
            Row row = sheet.createRow(rowCount++);

            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(time);
            timeCell.setCellStyle(rightStyle);
            sheet.autoSizeColumn(0);

            int colIndex = 1;
            for (String categoryId : uniqueCategories.keySet()) {
                Cell cell = row.createCell(colIndex++);

                Long value = dataMap.getOrDefault(time, new HashMap<>()).getOrDefault(categoryId, 0L);
                cell.setCellValue(value);
                cell.setCellStyle(amountStyle);
                sheet.autoSizeColumn(colIndex);
            }
        }

        if (!data.isEmpty()) {
            addSummaryRow(rowCount, uniqueCategories.size());
        }
    }

    private void addSummaryRow(int rowIndex, int categoryCount) {
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

        Cell cell = row.createCell(0);
        cell.setCellValue("TỔNG CỘNG");
        cell.setCellStyle(summaryStyle);
        sheet.autoSizeColumn(0);

        Map<String, Long> categoryTotals = new HashMap<>();

        Map<String, String> uniqueCategories = new LinkedHashMap<>();
        for (ReportRevenueCategory item : data) {
            uniqueCategories.put(item.getCategoryId(), item.getCategoryName());

            String categoryId = item.getCategoryId();
            Long amount = item.getTotalRevenue();

            categoryTotals.put(categoryId, categoryTotals.getOrDefault(categoryId, 0L) + amount);
        }

        int colIndex = 1;
        for (String categoryId : uniqueCategories.keySet()) {
            cell = row.createCell(colIndex);
            cell.setCellValue(categoryTotals.getOrDefault(categoryId, 0L));
            cell.setCellStyle(totalAmountStyle);
            sheet.autoSizeColumn(colIndex);
            colIndex++;
        }
    }

    private void addTitleToSheet(ReportFilterOptionsRequest request) {
        // Thu thập tất cả các danh mục riêng biệt
        Map<String, String> uniqueCategories = new LinkedHashMap<>();
        for (ReportRevenueCategory item : data) {
            uniqueCategories.put(item.getCategoryId(), item.getCategoryName());
        }
        int columnCount = uniqueCategories.size() + 1; // +1 cho cột thời gian

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
        titleCell.setCellValue("BÁO CÁO XU HƯỚNG THU NHẬP THEO DANH MỤC THU");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnCount - 1));

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
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, columnCount - 1));

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

        writeHeaderRow();
        writeDataRows();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}