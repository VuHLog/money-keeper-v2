package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ExpenseLimitSearchRequest;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseLimitResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.*;
import com.vuhlog.money_keeper.excel.*;
import com.vuhlog.money_keeper.service.ExpenseLimitService;
import com.vuhlog.money_keeper.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ExpenseLimitService expenseLimitService;

    @PostMapping("transaction-type")
    public ApiResponse<List<ReportTransactionTypeReponse>> getReportTransactionType(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportTransactionTypeReponse>>builder()
                .result(reportService.getReportForTransactionType(request))
                .build();
    }

    @PostMapping("total-expense")
    public ApiResponse<Long> getTotalExpense(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<Long>builder()
                .result(reportService.getTotalExpense(request))
                .build();
    }

    @PostMapping("total-revenue")
    public ApiResponse<Long> getTotalRevenue(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<Long>builder()
                .result(reportService.getTotalRevenue(request))
                .build();
    }

    @PostMapping("transaction-type/export-excel")
    public ResponseEntity<InputStreamResource> exportExcelForTransactionType(
            @RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=transaction-type.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportTransactionTypeReponse> transactionHistories = reportService.getReportForTransactionType(request);

        TransactionTypeExcelExporter exporter = new TransactionTypeExcelExporter(transactionHistories);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("expense-category")
    public ApiResponse<List<ReportExpenseCategory>> getReportExpenseCategory(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportExpenseCategory>>builder()
                .result(reportService.getReportExpenseCategory(request))
                .build();
    }

    @PostMapping("expense-category/export-excel")
    public ResponseEntity<InputStreamResource> exportExpenseCategory(
            @RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=expense-category.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportExpenseCategory> reportExpenseCategories = reportService.getReportExpenseCategory(request);

        ExpenseCategoryExcelExporter exporter = new ExpenseCategoryExcelExporter(reportExpenseCategories);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("expense-category-trending/export-excel")
    public ResponseEntity<InputStreamResource> exportExpenseCategoryTrending(
            @RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=expense-category-trending.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportExpenseCategory> reportExpenseCategories = reportService.getReportExpenseCategory(request);

        ExpenseCategoryTrendingExcelExporter exporter = new ExpenseCategoryTrendingExcelExporter(reportExpenseCategories);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("revenue-category")
    public ApiResponse<List<ReportRevenueCategory>> getReportRevenueCategory(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportRevenueCategory>>builder()
                .result(reportService.getReportRevenueCategory(request))
                .build();
    }

    @PostMapping("revenue-category/export-excel")
    public ResponseEntity<InputStreamResource> exportRevenueCategory(
            @RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=revenue-category.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportRevenueCategory> reportRevenueCategories = reportService.getReportRevenueCategory(request);

        RevenueCategoryExcelExporter exporter = new RevenueCategoryExcelExporter(reportRevenueCategories);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("revenue-category-trending/export-excel")
    public ResponseEntity<InputStreamResource> exportRevenueCategoryTrending(
            @RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=revenue-category-trending.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportRevenueCategory> reportRevenueCategories = reportService.getReportRevenueCategory(request);

        RevenueCategoryTrendingExcelExporter exporter = new RevenueCategoryTrendingExcelExporter(reportRevenueCategories);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("daily-trend")
    public ApiResponse<List<ReportDailyTrend>> getReportDailyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportDailyTrend>>builder()
                .result(reportService.getReportDailyTrend(request))
                .build();
    }

    @PostMapping("daily-trend/export-excel")
    public ResponseEntity<InputStreamResource> exportReportDailyTrend(@RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=daily-trend.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportDailyTrend> reportDailyTrend = reportService.getReportDailyTrend(request);

        DailyTrendExcelExporter exporter = new DailyTrendExcelExporter(reportDailyTrend);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }


    @PostMapping("weekly-trend")
    public ApiResponse<List<ReportWeeklyTrend>> getReportWeeklyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportWeeklyTrend>>builder()
                .result(reportService.getReportWeeklyTrend(request))
                .build();
    }

    @PostMapping("weekly-trend/export-excel")
    public ResponseEntity<InputStreamResource> exportReportWeeklyTrend(@RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=weekly-trend.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportWeeklyTrend> reportWeeklyTrend = reportService.getReportWeeklyTrend(request);

        WeeklyTrendExcelExporter exporter = new WeeklyTrendExcelExporter(reportWeeklyTrend);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("monthly-trend")
    public ApiResponse<List<ReportMonthlyTrend>> getReportMonthlyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportMonthlyTrend>>builder()
                .result(reportService.getReportMonthlyTrend(request))
                .build();
    }

    @PostMapping("monthly-trend/export-excel")
    public ResponseEntity<InputStreamResource> exportReportMonthlyTrend(@RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=monthly-trend.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportMonthlyTrend> reportMonthlyTrend = reportService.getReportMonthlyTrend(request);

        MonthlyTrendExcelExporter exporter = new MonthlyTrendExcelExporter(reportMonthlyTrend);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("yearly-trend")
    public ApiResponse<List<ReportYearlyTrend>> getReportYearlyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportYearlyTrend>>builder()
                .result(reportService.getReportYearlyTrend(request))
                .build();
    }

    @PostMapping("yearly-trend/export-excel")
    public ResponseEntity<InputStreamResource> exportReportYearlyTrend(@RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=yearly-trend.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportYearlyTrend> reportYearlyTrend = reportService.getReportYearlyTrend(request);

        YearlyTrendExcelExporter exporter = new YearlyTrendExcelExporter(reportYearlyTrend);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("account-balance-fluctuation")
    public ApiResponse<List<AccountBalanceFluctuation>> getAccountBalanceFluctuation(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<AccountBalanceFluctuation>>builder()
                .result(reportService.getAccountBalanceFluctuation(request))
                .build();
    }

    @PostMapping("account-balance-fluctuation/export-excel")
    public ResponseEntity<InputStreamResource> exportAccountBalanceFluctuation(@RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=account-balance-fluctuation.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<AccountBalanceFluctuation> accountBalanceFluctuation = reportService.getAccountBalanceFluctuation(request);

        AccountBalanceFluctuationExcelExporter exporter = new AccountBalanceFluctuationExcelExporter(accountBalanceFluctuation);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("bucket-payment-balance")
    public ApiResponse<List<ReportBucketPaymentBalance>> getReportBucketPaymentBalance() {
        return ApiResponse.<List<ReportBucketPaymentBalance>>builder()
                .result(reportService.getReportBucketPaymentBalance())
                .build();
    }

    @PostMapping("bucket-payment-balance/export-excel")
    public ResponseEntity<InputStreamResource> exportBucketPaymentBalance() throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=bucket-payment-balance.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportBucketPaymentBalance> reportBucketPaymentBalance = reportService.getReportBucketPaymentBalance();

        BucketPaymentBalanceExcelExporter exporter = new BucketPaymentBalanceExcelExporter(reportBucketPaymentBalance);
        ByteArrayInputStream in = exporter.export();
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("bucket-payment-type-balance")
    public ApiResponse<List<ReportBucketPaymentTypeBalance>> getReportBucketPaymentTypeBalance() {
        return ApiResponse.<List<ReportBucketPaymentTypeBalance>>builder()
                .result(reportService.getReportBucketPaymentTypeBalance())
                .build();
    }

    @PostMapping("bucket-payment-type-balance/export-excel")
    public ResponseEntity<InputStreamResource> exportBucketPaymentTypeBalance() throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=bucket-payment-type-balance.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportBucketPaymentTypeBalance> reportBucketPaymentTypeBalance = reportService.getReportBucketPaymentTypeBalance();

        BucketPaymentTypeBalanceExcelExporter exporter = new BucketPaymentTypeBalanceExcelExporter(reportBucketPaymentTypeBalance);
        ByteArrayInputStream in = exporter.export();
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("transaction-history")
    public ApiResponse<Page<TransactionHistory>> getAllTransactionHistory(
            @RequestParam(name = "field", required = false, defaultValue = "date") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "desc") String sort,
            @RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<Page<TransactionHistory>>builder()
                .result(reportService.getAllTransactionHistory(field, pageNumber, pageSize, sort, request))
                .build();
    }

    @PostMapping("transaction-history/export-excel")
    public ResponseEntity<InputStreamResource> exportTransactionHistoryExcel(
            @RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=transaction-history.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<TransactionHistory> transactionHistories = reportService.getAllTransactionHistoryNoPaging(request);

        TransactionHistoryExcelExporter exporter = new TransactionHistoryExcelExporter(transactionHistories);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/bucket-payment")
    public ApiResponse<Page<ReportBucketPayment>> getReportBucketPayment(
            @RequestParam(name = "field", required = false, defaultValue = "date") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "desc") String sort,
            @RequestBody ReportFilterOptionsRequest request
    ){
        return ApiResponse.<Page<ReportBucketPayment>>builder()
                .result(reportService.getBucketPaymentReport(field, pageNumber, pageSize, sort, request))
                .build();
    }

    @PostMapping("/total-bucket-payment")
    public ApiResponse<ReportTotalBucketPayment> getReportTotalBucketPayment(
            @RequestBody ReportFilterOptionsRequest request
    ){
        return ApiResponse.<ReportTotalBucketPayment>builder()
                .result(reportService.getReportTotalBucketPayment(request))
                .build();
    }

    @PostMapping("bucket-payment/export-excel")
    public ResponseEntity<InputStreamResource> exportBucketPayment(@RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=bucket-payment.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportBucketPayment> reportBucketPayment = reportService.getBucketPaymentReportNoPaging(request);
        ReportTotalBucketPayment total = reportService.getReportTotalBucketPayment(request);

        BucketPaymentExcelExporter exporter = new BucketPaymentExcelExporter(reportBucketPayment, total);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/category")
    public ApiResponse<Page<ReportCategory>> getReportCategory(
            @RequestParam(name = "field", required = false, defaultValue = "date") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "desc") String sort,
            @RequestBody ReportFilterOptionsRequest request
    ){
        return ApiResponse.<Page<ReportCategory>>builder()
                .result(reportService.getReportCategory(field, pageNumber, pageSize, sort, request))
                .build();
    }

    @PostMapping("/category-no-paging")
    public ApiResponse<List<ReportCategory>> getReportCategoryNoPaging(
            @RequestBody ReportFilterOptionsRequest request
    ){
        return ApiResponse.<List<ReportCategory>>builder()
                .result(reportService.getReportCategoryNoPaging(request))
                .build();
    }

    @PostMapping("category/export-excel")
    public ResponseEntity<InputStreamResource> exportCategory(@RequestBody ReportFilterOptionsRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=category.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ReportCategory> reportCategory = reportService.getReportCategoryNoPaging(request);

        CategoryExcelExporter exporter = new CategoryExcelExporter(reportCategory);
        ByteArrayInputStream in = exporter.export(request);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("expense-limit/export-excel")
    public ResponseEntity<InputStreamResource> exportExpenseLimit(@RequestBody ExpenseLimitSearchRequest request) throws IOException {
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=expense-limit.xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add(headerKey, headerValue);
        List<ExpenseLimitResponse> expenseLimitResponses = expenseLimitService.getAllExpenseLimit(request);

        ExpenseLimitExcelExporter exporter = new ExpenseLimitExcelExporter(expenseLimitResponses);
        ByteArrayInputStream in = exporter.export();
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
