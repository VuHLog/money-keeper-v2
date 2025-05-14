package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.AccountBalanceFluctuation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AccountBalanceFluctuationExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<AccountBalanceFluctuation> data;
    private ReportFilterOptionsRequest request;

    public AccountBalanceFluctuationExcelExporter(List<AccountBalanceFluctuation> data) {
        this.data = data;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Biến động số dư tài khoản");
        
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
                timeRangeTitle = "THEO THÁNG " + timeRangeTitle;
            }else if (request.getTimeOption().equals(ReportTimeOptionType.YEAR.getType())) {
                timeRangeTitle = "THEO NĂM " + timeRangeTitle;
            }
        }
        header.setCenter("BÁO CÁO BIẾN ĐỘNG SỐ DƯ TÀI KHOẢN" + timeRangeTitle);

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

    private CellStyle createDateStyle() {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(createBaseStyle());

        style.setAlignment(HorizontalAlignment.CENTER);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/mm/yyyy"));

        return style;
    }

    private CellStyle createTransactionTypeStyle(String type) {
        boolean isExpense = type.equals(TransactionType.EXPENSE.getType());
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(createBaseStyle());

        style.setAlignment(HorizontalAlignment.CENTER);

        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        font.setFontName("Arial");
        font.setBold(true);

        if (isExpense) {
            font.setColor(IndexedColors.RED.getIndex());
        } else {
            font.setColor(IndexedColors.GREEN.getIndex());
        }

        style.setFont(font);

        return style;
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

        // Tạo danh sách các tài khoản từ dữ liệu
        // Sử dụng Map để lưu trữ ID tài khoản và tên tài khoản
        Map<String, String> accountMap = new LinkedHashMap<>();
        
        for (AccountBalanceFluctuation item : data) {
            String bucketPaymentId = item.getBucketPaymentId();
            String accountName = item.getAccountName();
            
            if (bucketPaymentId != null && !bucketPaymentId.isEmpty() && accountName != null) {
                accountMap.put(bucketPaymentId, accountName);
            }
        }

        String[] headers = new String[accountMap.size() + 1];
        headers[0] = "Thời gian";
        
        int index = 1;
        for (String accountName : accountMap.values()) {
            headers[index++] = accountName;
        }

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
        CellStyle dateStyle = createDateStyle();
        CellStyle expenseStyle = createTransactionTypeStyle(TransactionType.EXPENSE.getType());
        CellStyle revenueStyle = createTransactionTypeStyle(TransactionType.REVENUE.getType());

        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }
        int rowCount = headerRowIndex;

        // map bucketPayments
        Map<String, String> accountMap = new LinkedHashMap<>();
        
        // balance bucketPayments at time
        Map<String, Map<String, Long>> timeAccountBalanceMap = new LinkedHashMap<>();

        for (AccountBalanceFluctuation item : data) {
            String bucketPaymentId = item.getBucketPaymentId();
            String accountName = item.getAccountName();
            if (bucketPaymentId != null && !bucketPaymentId.isEmpty() && accountName != null) {
                accountMap.put(bucketPaymentId, accountName);
            }
        }

        for (AccountBalanceFluctuation item : data) {
            String time = item.getTime();
            String bucketPaymentId = item.getBucketPaymentId();
            Long balance = item.getBalanceAfterTransaction();

            if (request != null && request.getTimeOption() != null && 
                request.getTimeOption().equals(ReportTimeOptionType.OPTIONAL.getType())) {
                String[] timeArr = time.split("-");
                if (timeArr.length == 3) {
                    ArrayUtils.reverse(timeArr);
                    time = String.join("/", timeArr);
                }
            }
            
            if (bucketPaymentId != null && !bucketPaymentId.isEmpty()) {
                timeAccountBalanceMap.putIfAbsent(time, new HashMap<>());
                timeAccountBalanceMap.get(time).put(bucketPaymentId, balance);
            }
        }

        Set<String> allTimePoints = generateAllTimePoints(request);

        if (allTimePoints.isEmpty()) {
            allTimePoints = new TreeSet<>(timeAccountBalanceMap.keySet());
        }

        // lastValue bucketPayments
        Map<String, Long> lastAccountBalances = new HashMap<>();
        for (String bucketPaymentId : accountMap.keySet()) {
            lastAccountBalances.put(bucketPaymentId, 0L);
        }
        
        for (String time : allTimePoints) {
            Row row = sheet.createRow(rowCount++);
            
            // Thời gian
            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(time);
            timeCell.setCellStyle(rightBaseStyle);
            
            // Số dư cho mỗi tài khoản
            int columnIndex = 1;
            Map<String, Long> timeBalances = timeAccountBalanceMap.get(time);
            
            for (String bucketPaymentId : accountMap.keySet()) {
                Cell balanceCell = row.createCell(columnIndex++);
                
                if (timeBalances != null && timeBalances.containsKey(bucketPaymentId)) {
                    // Cập nhật giá trị hiện tại nếu có dữ liệu mới
                    lastAccountBalances.put(bucketPaymentId, timeBalances.get(bucketPaymentId));
                }
                
                // Sử dụng giá trị hiện tại của tài khoản
                Long currentBalance = lastAccountBalances.get(bucketPaymentId);
                balanceCell.setCellValue(currentBalance);
                balanceCell.setCellStyle(amountStyle);
            }
        }
        
        // Điều chỉnh kích thước cột
        for (int i = 0; i <= accountMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }
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
        titleCell.setCellValue("BÁO CÁO BIẾN ĐỘNG SỐ DƯ TÀI KHOẢN");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

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