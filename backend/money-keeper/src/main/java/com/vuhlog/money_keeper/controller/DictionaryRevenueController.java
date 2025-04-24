package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.DictionaryRevenueRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.DictionaryRevenueResponse;
import com.vuhlog.money_keeper.service.DictionaryRevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dictionary-revenue")
@RequiredArgsConstructor
public class DictionaryRevenueController {
    private final DictionaryRevenueService dictionaryRevenueService;

    @GetMapping("")
    public ApiResponse<List<DictionaryRevenueResponse>> getDictionaryRevenue(){
        return ApiResponse.<List<DictionaryRevenueResponse>>builder()
                .result(dictionaryRevenueService.getAllDictionaryRevenue())
                .build();
    }

    @GetMapping("/without-transfer")
    public ApiResponse<List<DictionaryRevenueResponse>> getDictionaryRevenueWithoutTransfer(){
        return ApiResponse.<List<DictionaryRevenueResponse>>builder()
                .result(dictionaryRevenueService.getAllDictionaryRevenueWithoutTransfer())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DictionaryRevenueResponse> getDictionaryRevenue(@PathVariable String id){
        return ApiResponse.<DictionaryRevenueResponse>builder()
                .result(dictionaryRevenueService.getDictionaryRevenueById(id))
                .build();
    }

    @PostMapping
    public ApiResponse<DictionaryRevenueResponse> createDictionaryRevenue(@RequestBody DictionaryRevenueRequest request){
        return ApiResponse.<DictionaryRevenueResponse>builder()
                .result(dictionaryRevenueService.createDictionaryRevenue(request))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<DictionaryRevenueResponse> updateDictionaryRevenue(@PathVariable String id, @RequestBody DictionaryRevenueRequest request){
        return ApiResponse.<DictionaryRevenueResponse>builder()
                .result(dictionaryRevenueService.updateDictionaryRevenue(id, request))
                .build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteDictionaryRevenue(@PathVariable String id){
        dictionaryRevenueService.deleteDictionaryRevenueItem(id);
        return ApiResponse.<String>builder()
                .result("Delete dictionary Revenue item success")
                .build();
    }
}
