package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.RevenueRegularRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.RevenueRegularResponse;
import com.vuhlog.money_keeper.service.RevenueRegularService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/revenue-regular")
@RequiredArgsConstructor
public class RevenueRegularController {
    private final RevenueRegularService revenueRegularService;

    @GetMapping("")
    public ApiResponse<List<RevenueRegularResponse>> getAllMyRevenueRegular(@RequestParam(name = "dictionaryBucketPaymentId", required = true) String dictionaryBucketPaymentId) {
        return ApiResponse.<List<RevenueRegularResponse>>builder().result(revenueRegularService.getAllMyRevenueRegular(dictionaryBucketPaymentId)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RevenueRegularResponse> getRevenueRegularById(@PathVariable String id) {
        return ApiResponse.<RevenueRegularResponse>builder().result(revenueRegularService.getRevenueRegularById(id)).build();
    }

    @PostMapping("")
    public ApiResponse<RevenueRegularResponse> createRevenueRegular(@RequestBody RevenueRegularRequest req) {
        return ApiResponse.<RevenueRegularResponse>builder().result(revenueRegularService.createRevenueRegular(req)).build();
    }

    @PutMapping("/{id}")
    public ApiResponse<RevenueRegularResponse> updateRevenueRegular(@PathVariable String id, @RequestBody RevenueRegularRequest req) {
        return ApiResponse.<RevenueRegularResponse>builder().result(revenueRegularService.updateRevenueRegular(id, req)).build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteRevenueRegularById(@PathVariable String id) {
        revenueRegularService.deleteRevenueRegular(id);
        return ApiResponse.<String>builder().result("delete Revenue regular successfully").build();
    }
}
