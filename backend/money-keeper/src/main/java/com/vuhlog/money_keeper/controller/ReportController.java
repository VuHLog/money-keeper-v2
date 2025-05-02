package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportTransactionTypeReponse;
import com.vuhlog.money_keeper.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("transaction-type")
    public ApiResponse<List<ReportTransactionTypeReponse>> getReportTransactionType(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportTransactionTypeReponse>>builder()
                .result(reportService.getReportForTransactionType(request))
                .build();
    }
}
