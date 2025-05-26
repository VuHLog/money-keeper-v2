package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.constants.TransferType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.ReportTotalBucketPaymentDTO;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportBucketPayment;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportTotalBucketPayment;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.TransactionHistory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BucketPaymentExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ReportBucketPayment> data;
    private ReportTotalBucketPaymentDTO totalData;

    public BucketPaymentExcelExporter(List<ReportBucketPayment> data, ReportTotalBucketPaymentDTO totalData) {
        this.data = data;
        this.totalData = totalData;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Tài khoản");
        
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
        header.setCenter("BÁO CÁO THEO TÀI KHOẢN" + timeRangeTitle);

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

    private CellStyle createAmountStyle(String transactionType) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(createBaseStyle());

        style.setAlignment(HorizontalAlignment.RIGHT);

        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        font.setFontName("Arial");
        font.setBold(true);
        if (transactionType.equals(TransactionType.EXPENSE.getType())) {
            font.setColor(IndexedColors.RED.getIndex());
        } else {
            font.setColor(IndexedColors.GREEN.getIndex());
        }

        style.setFont(font);

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
                "Tài khoản", "Số dư ban đầu", "Tổng thu", "Tổng chi",
                "Số dư hiện tại", "Chênh lệch"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeDataRows() {
        CellStyle baseStyle = createBaseStyle();
        CellStyle dateStyle = createDateStyle();

        DataFormat format = workbook.createDataFormat();
        CellStyle totalAmountExpenseStyle = workbook.createCellStyle();
        totalAmountExpenseStyle.cloneStyleFrom(baseStyle);
        totalAmountExpenseStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontExpense = workbook.createFont();
        fontExpense.setFontHeight(12);
        fontExpense.setFontName("Arial");
        fontExpense.setBold(true);
        fontExpense.setColor(IndexedColors.RED.getIndex());
        totalAmountExpenseStyle.setFont(fontExpense);


        CellStyle totalAmountRevenueStyle = workbook.createCellStyle();
        totalAmountRevenueStyle.cloneStyleFrom(baseStyle);
        totalAmountRevenueStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontRevenue = workbook.createFont();
        fontRevenue.setFontHeight(12);
        fontRevenue.setFontName("Arial");
        fontRevenue.setBold(true);
        fontRevenue.setColor(IndexedColors.GREEN.getIndex());
        totalAmountRevenueStyle.setFont(fontRevenue);

        CellStyle summaryAmountStyle = workbook.createCellStyle();
        summaryAmountStyle.cloneStyleFrom(baseStyle);
        summaryAmountStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontSummary = workbook.createFont();
        fontSummary.setFontHeight(12);
        fontSummary.setFontName("Arial");
        fontSummary.setBold(true);
        fontSummary.setColor(IndexedColors.BLACK.getIndex());
        summaryAmountStyle.setFont(fontSummary);

        CellStyle totalBalanceStyle = workbook.createCellStyle();
        totalBalanceStyle.cloneStyleFrom(baseStyle);
        totalBalanceStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontTotalBalance = workbook.createFont();
        fontTotalBalance.setFontHeight(12);
        fontTotalBalance.setFontName("Arial");
        fontTotalBalance.setBold(true);
        fontTotalBalance.setColor(IndexedColors.BLUE.getIndex());
        totalBalanceStyle.setFont(fontTotalBalance);

        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }
        int rowCount = headerRowIndex;

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);
        
        for (ReportBucketPayment bucketPayment : data) {
            Row row = sheet.createRow(rowCount);

            String currency = bucketPayment.getCurrency();
            String currencySymbol = bucketPayment.getCurrencySymbol();
            Boolean isVND = currency.equals("VND");

            Cell cell = row.createCell(0);
            cell.setCellValue(bucketPayment.getAccountName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(0);

            cell = row.createCell(1);
            cell.setCellValue(isVND? (formatter.format(bucketPayment.getInitialBalance()) + " ₫") : (currencySymbol + formatter.format(bucketPayment.getInitialBalance())));
            cell.setCellStyle(summaryAmountStyle);
            sheet.autoSizeColumn(1);

            cell = row.createCell(2);
            cell.setCellValue(isVND? (formatter.format(bucketPayment.getTotalRevenue()) + " ₫") : (currencySymbol + formatter.format(bucketPayment.getConvertedTotalRevenue()) + " ~ " + formatter.format(bucketPayment.getTotalRevenue()) + " ₫"));
            cell.setCellStyle(totalAmountRevenueStyle);
            sheet.autoSizeColumn(2);

            cell = row.createCell(3);
            cell.setCellValue(isVND? (formatter.format(bucketPayment.getTotalExpense()) + " ₫") : (currencySymbol + formatter.format(bucketPayment.getConvertedTotalExpense()) + " ~ " + formatter.format(bucketPayment.getTotalExpense()) + " ₫"));
            cell.setCellStyle(totalAmountExpenseStyle);
            sheet.autoSizeColumn(3);

            cell = row.createCell(4);
            cell.setCellValue(isVND? (formatter.format(bucketPayment.getBalance()) + " ₫") : (currencySymbol + formatter.format(bucketPayment.getBalance())));
            cell.setCellStyle(totalBalanceStyle);
            sheet.autoSizeColumn(4);

            Double disparityPercent = (double) (bucketPayment.getDisparity())/ bucketPayment.getInitialBalance() * 100;
            disparityPercent = Math.round(disparityPercent*10.0)/10.0;
            cell = row.createCell(5);
            cell.setCellValue((isVND? (formatter.format(bucketPayment.getDisparity()) + currencySymbol) : (currencySymbol + formatter.format(bucketPayment.getDisparity()))) + " (" + disparityPercent + "%)");
            cell.setCellStyle(bucketPayment.getDisparity() >= 0 ? totalAmountRevenueStyle : totalAmountExpenseStyle);
            sheet.autoSizeColumn(5);

            rowCount++;
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

        DataFormat format = workbook.createDataFormat();

        CellStyle totalAmountExpenseStyle = workbook.createCellStyle();
        totalAmountExpenseStyle.cloneStyleFrom(summaryStyle);
        totalAmountExpenseStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontExpense = workbook.createFont();
        fontExpense.setFontHeight(12);
        fontExpense.setFontName("Arial");
        fontExpense.setBold(true);
        fontExpense.setColor(IndexedColors.RED.getIndex());
        totalAmountExpenseStyle.setFont(fontExpense);


        CellStyle totalAmountRevenueStyle = workbook.createCellStyle();
        totalAmountRevenueStyle.cloneStyleFrom(summaryStyle);
        totalAmountRevenueStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontRevenue = workbook.createFont();
        fontRevenue.setFontHeight(12);
        fontRevenue.setFontName("Arial");
        fontRevenue.setBold(true);
        fontRevenue.setColor(IndexedColors.GREEN.getIndex());
        totalAmountRevenueStyle.setFont(fontRevenue);

        CellStyle summaryAmountStyle = workbook.createCellStyle();
        summaryAmountStyle.cloneStyleFrom(summaryStyle);
        summaryAmountStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontSummary = workbook.createFont();
        fontSummary.setFontHeight(12);
        fontSummary.setFontName("Arial");
        fontSummary.setBold(true);
        fontSummary.setColor(IndexedColors.BLACK.getIndex());
        summaryAmountStyle.setFont(fontSummary);

        CellStyle totalBalanceStyle = workbook.createCellStyle();
        totalBalanceStyle.cloneStyleFrom(summaryStyle);
        totalBalanceStyle.setAlignment(HorizontalAlignment.RIGHT);
        XSSFFont fontTotalBalance = workbook.createFont();
        fontTotalBalance.setFontHeight(12);
        fontTotalBalance.setFontName("Arial");
        fontTotalBalance.setBold(true);
        fontTotalBalance.setColor(IndexedColors.BLUE.getIndex());
        totalBalanceStyle.setFont(fontTotalBalance);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,###.##", symbols);

        Long totalInitialBalance = totalData.getTotalInitialBalance();
        Long totalExpense = totalData.getTotalExpense();
        Long totalRevenue = totalData.getTotalRevenue();
        Long totalBalance = totalData.getTotalBalance();

        CellStyle totalExpenseStyle = workbook.createCellStyle();
        totalExpenseStyle.cloneStyleFrom(summaryStyle);

        CellStyle totalRevenueStyle = workbook.createCellStyle();
        totalRevenueStyle.cloneStyleFrom(summaryStyle);

        row = sheet.createRow(rowIndex + 2);
        Cell cell = row.createCell(0);
        cell.setCellValue("Tổng :");
        cell.setCellStyle(totalRevenueStyle);

        cell = row.createCell(1);
        cell.setCellValue(formatter.format(totalInitialBalance) + " ₫");
        cell.setCellStyle(summaryAmountStyle);


        cell = row.createCell(2);
        cell.setCellValue(formatter.format(totalRevenue) + " ₫");
        cell.setCellStyle(totalAmountRevenueStyle);

        cell = row.createCell(3);
        cell.setCellValue(formatter.format(totalExpense) + " ₫");
        cell.setCellStyle(totalAmountExpenseStyle);

        cell = row.createCell(4);
        cell.setCellValue(formatter.format(totalBalance) + " ₫");
        cell.setCellStyle(totalBalanceStyle);
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
        titleCell.setCellValue("BÁO CÁO THEO TÀI KHOẢN");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

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
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

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