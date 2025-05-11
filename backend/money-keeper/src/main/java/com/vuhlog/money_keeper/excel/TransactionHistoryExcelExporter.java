package com.vuhlog.money_keeper.excel;

import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.constants.TransferType;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.TransactionHistory;
import com.vuhlog.money_keeper.util.FormatUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class TransactionHistoryExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<TransactionHistory> transactionHistories;

    public TransactionHistoryExcelExporter(List<TransactionHistory> transactionHistories) {
        this.transactionHistories = transactionHistories;
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Transaction History");
    }

    private void writeHeaderRow(){
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        Cell cell = row.createCell(0);
        cell.setCellValue("ID giao dịch");
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue("Loại giao dịch");
        cell.setCellStyle(style);

        cell = row.createCell(2);
        cell.setCellValue("Tên tài khoản");
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue("Tên danh mục");
        cell.setCellStyle(style);

        cell = row.createCell(4);
        cell.setCellValue("Số tiền");
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue("Ngày giao dịch");
        cell.setCellStyle(style);

        cell = row.createCell(6);
        cell.setCellValue("Người thụ hưởng");
        cell.setCellStyle(style);

        cell = row.createCell(7);
        cell.setCellValue("Nhận tiền từ");
        cell.setCellStyle(style);

        cell = row.createCell(8);
        cell.setCellValue("Loại chuyển khoản");
        cell.setCellStyle(style);

        cell = row.createCell(9);
        cell.setCellValue("Tên tài khoản thụ hưởng");
        cell.setCellStyle(style);

        cell = row.createCell(10);
        cell.setCellValue("Tên tài khoản gửi tiền");
        cell.setCellStyle(style);

        cell = row.createCell(11);
        cell.setCellValue("Chuyến đi/Sự kiện");
        cell.setCellStyle(style);

        cell = row.createCell(12);
        cell.setCellValue("Địa điểm");
        cell.setCellStyle(style);

        cell = row.createCell(13);
        cell.setCellValue("Ghi chú");
        cell.setCellStyle(style);
    }

    private void writeDataRows(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        CellStyle amountStyle = workbook.createCellStyle();
        amountStyle.setAlignment(HorizontalAlignment.RIGHT);
        amountStyle.setFont(font);
        DataFormat format = workbook.createDataFormat();
        amountStyle.setDataFormat(format.getFormat("#,##0 [$₫-vi-VN]"));

        for(TransactionHistory transaction : transactionHistories){
            Row row = sheet.createRow(rowCount++);

            Cell cell = row.createCell(0);
            cell.setCellValue(transaction.getId());
            sheet.autoSizeColumn(0);
            cell.setCellStyle(style);

            cell = row.createCell(1);
            cell.setCellValue(transaction.getTransactionType().equals(TransactionType.EXPENSE.getType()) ? "Chi tiêu" : "Thu nhập");
            sheet.autoSizeColumn(1);
            cell.setCellStyle(style);

            cell = row.createCell(2);
            cell.setCellValue(transaction.getAccountName());
            sheet.autoSizeColumn(2);
            cell.setCellStyle(style);

            cell = row.createCell(3);
            cell.setCellValue(transaction.getCategoryName());
            sheet.autoSizeColumn(3);
            cell.setCellStyle(style);

            cell = row.createCell(4);
            cell.setCellValue(transaction.getAmount());
            sheet.autoSizeColumn(4);
            cell.setCellStyle(amountStyle);

            cell = row.createCell(5);
            cell.setCellValue(transaction.getDate());
            sheet.autoSizeColumn(5);
            cell.setCellStyle(style);

            cell = row.createCell(6);
            cell.setCellValue(transaction.getBeneficiary());
            sheet.autoSizeColumn(6);
            cell.setCellStyle(style);

            cell = row.createCell(7);
            cell.setCellValue(transaction.getCollectMoneyWho());
            sheet.autoSizeColumn(7);
            cell.setCellStyle(style);

            cell = row.createCell(8);
            cell.setCellValue(transaction.getTransferType().equals(TransferType.NORMAL.getType())? "Thông thường" : "Chuyển khoản");
            sheet.autoSizeColumn(8);
            cell.setCellStyle(style);

            cell = row.createCell(9);
            cell.setCellValue(transaction.getBeneficiaryAccountName());
            sheet.autoSizeColumn(9);
            cell.setCellStyle(style);

            cell = row.createCell(10);
            cell.setCellValue(transaction.getSenderAccountName());
            sheet.autoSizeColumn(10);
            cell.setCellStyle(style);

            cell = row.createCell(11);
            cell.setCellValue(transaction.getTripEvent());
            sheet.autoSizeColumn(11);
            cell.setCellStyle(style);

            cell = row.createCell(12);
            cell.setCellValue(transaction.getBeneficiary());
            sheet.autoSizeColumn(12);
            cell.setCellStyle(style);

            cell = row.createCell(13);
            cell.setCellValue(transaction.getInterpretation());
            sheet.autoSizeColumn(13);
            cell.setCellStyle(style);
        }
    }

    public ByteArrayInputStream export() throws IOException {
        writeHeaderRow();
        writeDataRows();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
