package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.ExpenseLimitResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportBucketPayment;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportCategory;
import com.vuhlog.money_keeper.entity.DictionaryExpense;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseLimitExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<ExpenseLimitResponse> data;

    public ExpenseLimitExcelExporter(List<ExpenseLimitResponse> data) {
        this.data = data;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Hạn mức chi");
        
        // Thiết lập thuộc tính trang mà không có thông tin customTimeRange
        setupPageProperties();
    }

    private void setupPageProperties() {
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

        header.setCenter("BÁO CÁO HẠN MỨC CHI CÒN HIỆU LỰC");

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
                "Tên hạn mức", "Tên tài khoản áp dụng", "Danh mục", "Hạn mức",
                "Đã chi tiêu", "Còn lại", "Trạng thái", "Tiến độ", "Thời gian"
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
        totalAmountExpenseStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontExpense = workbook.createFont();
        fontExpense.setFontHeight(12);
        fontExpense.setFontName("Arial");
        fontExpense.setBold(true);
        fontExpense.setColor(IndexedColors.RED.getIndex());
        totalAmountExpenseStyle.setFont(fontExpense);


        CellStyle totalAmountRevenueStyle = workbook.createCellStyle();
        totalAmountRevenueStyle.cloneStyleFrom(baseStyle);
        totalAmountRevenueStyle.setAlignment(HorizontalAlignment.RIGHT);
        totalAmountRevenueStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontRevenue = workbook.createFont();
        fontRevenue.setFontHeight(12);
        fontRevenue.setFontName("Arial");
        fontRevenue.setBold(true);
        fontRevenue.setColor(IndexedColors.GREEN.getIndex());
        totalAmountRevenueStyle.setFont(fontRevenue);

        CellStyle summaryAmountStyle = workbook.createCellStyle();
        summaryAmountStyle.cloneStyleFrom(baseStyle);
        summaryAmountStyle.setAlignment(HorizontalAlignment.RIGHT);
        summaryAmountStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontSummary = workbook.createFont();
        fontSummary.setFontHeight(12);
        fontSummary.setFontName("Arial");
        fontSummary.setBold(true);
        fontSummary.setColor(IndexedColors.BLACK.getIndex());
        summaryAmountStyle.setFont(fontSummary);

        CellStyle redStyle = workbook.createCellStyle();
        redStyle.cloneStyleFrom(baseStyle);
        redStyle.setAlignment(HorizontalAlignment.RIGHT);
        redStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontRed = workbook.createFont();
        fontRed.setFontHeight(12);
        fontRed.setFontName("Arial");
        fontRed.setBold(true);
        fontRed.setColor(IndexedColors.RED.getIndex());
        redStyle.setFont(fontRed);

        CellStyle yellowStyle = workbook.createCellStyle();
        yellowStyle.cloneStyleFrom(baseStyle);
        yellowStyle.setAlignment(HorizontalAlignment.RIGHT);
        yellowStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontYellow = workbook.createFont();
        fontYellow.setFontHeight(12);
        fontYellow.setFontName("Arial");
        fontYellow.setBold(true);
        fontYellow.setColor(IndexedColors.DARK_YELLOW.getIndex());
        yellowStyle.setFont(fontYellow);

        CellStyle greenStyle = workbook.createCellStyle();
        greenStyle.cloneStyleFrom(baseStyle);
        greenStyle.setAlignment(HorizontalAlignment.RIGHT);
        greenStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontGreen = workbook.createFont();
        fontGreen.setFontHeight(12);
        fontGreen.setFontName("Arial");
        fontGreen.setBold(true);
        fontGreen.setColor(IndexedColors.GREEN.getIndex());
        greenStyle.setFont(fontGreen);

        int headerRowIndex = 0;
        while (sheet.getRow(headerRowIndex) != null) {
            headerRowIndex++;
        }
        int rowCount = headerRowIndex;

        DecimalFormat formatter = new DecimalFormat("#,###");

        for (ExpenseLimitResponse expenseLimit : data) {
            Row row = sheet.createRow(rowCount);

            Cell cell = row.createCell(0);
            cell.setCellValue(expenseLimit.getName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(0);

            cell = row.createCell(1);
            cell.setCellValue(expenseLimit.getBucketPayments().getAccountName());
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(1);

            cell = row.createCell(2);
            cell.setCellValue(expenseLimit.getCategories().stream().map(DictionaryExpense::getName).collect(Collectors.joining(",")));
            cell.setCellStyle(baseStyle);
            sheet.setColumnWidth(2, 18000);

            cell = row.createCell(3);
            cell.setCellValue(expenseLimit.getAmount());
            cell.setCellStyle(summaryAmountStyle);
            sheet.autoSizeColumn(3);

            cell = row.createCell(4);
            cell.setCellValue(expenseLimit.getSpentAmount());
            cell.setCellStyle(totalAmountExpenseStyle);
            sheet.autoSizeColumn(4);

            Long remainingAmount = expenseLimit.getAmount() - expenseLimit.getSpentAmount();
            cell = row.createCell(5);
            cell.setCellValue(remainingAmount);
            cell.setCellStyle(remainingAmount > 0 ? totalAmountRevenueStyle : totalAmountExpenseStyle);
            sheet.autoSizeColumn(5);

            long percent = Math.round((double) expenseLimit.getSpentAmount() / expenseLimit.getAmount() * 100);
            cell = row.createCell(6);
            cell.setCellValue(percent < 70 ? "Tốt" : percent < 100 ? "Cảnh báo" : "Vượt hạn mức");
            cell.setCellStyle(percent < 70 ? greenStyle : percent < 100 ? yellowStyle : redStyle);
            sheet.autoSizeColumn(6);

            cell = row.createCell(7);
            cell.setCellValue(percent+ "%");
            cell.setCellStyle(percent < 70 ? greenStyle : percent < 100 ? yellowStyle : redStyle);
            sheet.autoSizeColumn(7);

            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            cell = row.createCell(8);
            cell.setCellValue("Từ " + dateFormat.format(LocalDate.parse(expenseLimit.getStartDateLimit())) + " đến " + dateFormat.format(LocalDate.parse(expenseLimit.getEndDateLimit())));
            cell.setCellStyle(baseStyle);
            sheet.autoSizeColumn(8);

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
        totalAmountExpenseStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontExpense = workbook.createFont();
        fontExpense.setFontHeight(12);
        fontExpense.setFontName("Arial");
        fontExpense.setBold(true);
        fontExpense.setColor(IndexedColors.RED.getIndex());
        totalAmountExpenseStyle.setFont(fontExpense);


        CellStyle totalAmountRevenueStyle = workbook.createCellStyle();
        totalAmountRevenueStyle.cloneStyleFrom(summaryStyle);
        totalAmountRevenueStyle.setAlignment(HorizontalAlignment.RIGHT);
        totalAmountRevenueStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontRevenue = workbook.createFont();
        fontRevenue.setFontHeight(12);
        fontRevenue.setFontName("Arial");
        fontRevenue.setBold(true);
        fontRevenue.setColor(IndexedColors.GREEN.getIndex());
        totalAmountRevenueStyle.setFont(fontRevenue);

        CellStyle summaryAmountStyle = workbook.createCellStyle();
        summaryAmountStyle.cloneStyleFrom(summaryStyle);
        summaryAmountStyle.setAlignment(HorizontalAlignment.RIGHT);
        summaryAmountStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));
        XSSFFont fontSummary = workbook.createFont();
        fontSummary.setFontHeight(12);
        fontSummary.setFontName("Arial");
        fontSummary.setBold(true);
        fontSummary.setColor(IndexedColors.BLACK.getIndex());
        summaryAmountStyle.setFont(fontSummary);

        Long totalAmountLimit = 0L;
        Long totalSpentAmount = 0L;

        for (ExpenseLimitResponse expenseLimit : data) {
            totalAmountLimit += expenseLimit.getAmount();
            totalSpentAmount += expenseLimit.getSpentAmount();
        }

        Long remainingAmount = totalAmountLimit - totalSpentAmount;

        row = sheet.createRow(rowIndex + 2);
        Cell cell = row.createCell(0);
        cell.setCellValue("Tổng :");
        cell.setCellStyle(summaryStyle);

        for (int col = 0; col <= 2; col++) {
            Cell currentCell = row.getCell(col);
            if (currentCell == null) {
                currentCell = row.createCell(col);
            }
            currentCell.setCellStyle(summaryStyle);
        }


        sheet.addMergedRegion(new CellRangeAddress(rowIndex + 2, rowIndex + 2, 0, 2));

        cell = row.createCell(3);
        cell.setCellValue(totalAmountLimit);
        cell.setCellStyle(summaryAmountStyle);


        cell = row.createCell(4);
        cell.setCellValue(totalSpentAmount);
        cell.setCellStyle(totalAmountExpenseStyle);

        cell = row.createCell(5);
        cell.setCellValue(remainingAmount);
        cell.setCellStyle(remainingAmount > 0 ? totalAmountRevenueStyle : totalAmountExpenseStyle);
    }


    private void addTitleToSheet() {
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
        titleCell.setCellValue("BÁO CÁO THEO HẠN MỨC CHI CÒN HIỆU LỰC");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));
    }

    public ByteArrayInputStream export() throws IOException {
        setupPageProperties();
        
        addTitleToSheet();
        
        // Viết header và dữ liệu
        writeHeaderRow();
        writeDataRows();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}