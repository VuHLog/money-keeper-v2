package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.constants.TransferType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.TransactionHistory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TransactionHistoryExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<TransactionHistory> transactionHistories;

    public TransactionHistoryExcelExporter(List<TransactionHistory> transactionHistories) {
        this.transactionHistories = transactionHistories;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Lịch sử giao dịch");
        
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
        header.setCenter("BÁO CÁO LỊCH SỬ GIAO DỊCH" + timeRangeTitle);

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
                "ID giao dịch", "Loại giao dịch", "Tên tài khoản", "Tên danh mục",
                "Số tiền", "Ngày giao dịch", "Người thụ hưởng", "Nhận tiền từ",
                "Loại chuyển khoản", "Tên tài khoản thụ hưởng", "Tên tài khoản gửi tiền",
                "Chuyến đi/Sự kiện", "Địa điểm", "Ghi chú"
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
        CellStyle dateStyle = createDateStyle();
        CellStyle expenseStyle = createTransactionTypeStyle(TransactionType.EXPENSE.getType());
        CellStyle revenueStyle = createTransactionTypeStyle(TransactionType.REVENUE.getType());

        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }
        int rowCount = headerRowIndex;

        for (TransactionHistory transaction : transactionHistories) {
            Row row = sheet.createRow(rowCount);

            // ID giao dịch
            Cell cell = row.createCell(0);
            cell.setCellValue(transaction.getId());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(0);

            // Loại giao dịch
            cell = row.createCell(1);
            boolean isExpense = transaction.getTransactionType().equals(TransactionType.EXPENSE.getType());
            cell.setCellValue(isExpense ? "Chi tiêu" : "Thu nhập");
            cell.setCellStyle(isExpense ? expenseStyle : revenueStyle);
            sheet.autoSizeColumn(1);

            // Tên tài khoản
            cell = row.createCell(2);
            cell.setCellValue(transaction.getAccountName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(2);

            // Tên danh mục
            cell = row.createCell(3);
            cell.setCellValue(transaction.getCategoryName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(3);

            // Số tiền
            cell = row.createCell(4);
            cell.setCellValue(transaction.getAmount());
            cell.setCellStyle(amountStyle);
            sheet.autoSizeColumn(4);

            // Ngày giao dịch
            cell = row.createCell(5);
            cell.setCellValue(transaction.getDate());
            cell.setCellStyle(dateStyle);
            sheet.autoSizeColumn(5);

            // Người thụ hưởng
            cell = row.createCell(6);
            cell.setCellValue(transaction.getBeneficiary());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(6);

            // Nhận tiền từ
            cell = row.createCell(7);
            cell.setCellValue(transaction.getCollectMoneyWho());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(7);

            // Loại chuyển khoản
            cell = row.createCell(8);
            cell.setCellValue(transaction.getTransferType().equals(TransferType.NORMAL.getType()) ?
                    "Thông thường" : "Chuyển khoản");
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(8);

            // Tên tài khoản thụ hưởng
            cell = row.createCell(9);
            cell.setCellValue(transaction.getBeneficiaryAccountName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(9);

            // Tên tài khoản gửi tiền
            cell = row.createCell(10);
            cell.setCellValue(transaction.getSenderAccountName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(10);

            // Chuyến đi/Sự kiện
            cell = row.createCell(11);
            cell.setCellValue(transaction.getTripEvent());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(11);

            // Địa điểm
            cell = row.createCell(12);
            cell.setCellValue(transaction.getLocation());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(12);

            // Ghi chú
            cell = row.createCell(13);
            cell.setCellValue(transaction.getInterpretation());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(13);

            rowCount++;
        }

        if (!transactionHistories.isEmpty()) {
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
        double totalRevenue = 0;

        for (TransactionHistory transaction : transactionHistories) {
            if (transaction.getTransactionType().equals(TransactionType.EXPENSE.getType())) {
                totalExpense += transaction.getAmount();
            } else {
                totalRevenue += transaction.getAmount();
            }
        }

        Cell cell = row.createCell(0);
        cell.setCellValue("TỔNG CỘNG");
        cell.setCellStyle(summaryStyle);

        // merge cell 0 -> 3
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));

        for (int i = 1; i <= 3; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(summaryStyle);
        }

        cell = row.createCell(4);
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
        titleCell.setCellValue("BÁO CÁO LỊCH SỬ GIAO DỊCH");
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