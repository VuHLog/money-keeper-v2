package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.DictionaryExpenseRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.DictionaryExpenseResponse;
import com.vuhlog.money_keeper.service.DictionaryExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dictionary-expense")
@RequiredArgsConstructor
public class DictionaryExpenseController {
    private final DictionaryExpenseService dictionaryExpenseService;

    @GetMapping("")
    public ApiResponse<List<DictionaryExpenseResponse>> getAllDictionaryExpense(){
        return ApiResponse.<List<DictionaryExpenseResponse>>builder()
                .result(dictionaryExpenseService.getAllDictionaryExpense())
                .build();
    }

    @GetMapping("/without-transfer")
    public ApiResponse<List<DictionaryExpenseResponse>> getAllDictionaryExpenseWithoutTransfer(
            @RequestParam(name = "search", required = false, defaultValue = "") String search
    ){
        return ApiResponse.<List<DictionaryExpenseResponse>>builder()
                .result(dictionaryExpenseService.getAllDictionaryExpenseWithoutTransfer(search))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DictionaryExpenseResponse> getDictionaryExpense(@PathVariable String id){
        return ApiResponse.<DictionaryExpenseResponse>builder()
                .result(dictionaryExpenseService.getDictionaryExpenseById(id))
                .build();
    }

    @PostMapping
    public ApiResponse<DictionaryExpenseResponse> createDictionaryExpense(@RequestBody DictionaryExpenseRequest request){
        return ApiResponse.<DictionaryExpenseResponse>builder()
                .result(dictionaryExpenseService.createDictionaryExpense(request))
                .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<DictionaryExpenseResponse> updateDictionaryExpense(@PathVariable String id, @RequestBody DictionaryExpenseRequest request){
        return ApiResponse.<DictionaryExpenseResponse>builder()
                .result(dictionaryExpenseService.updateDictionaryExpense(id, request))
                .build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteDictionaryExpense(@PathVariable String id){
        dictionaryExpenseService.deleteDictionaryExpenseItem(id);
        return ApiResponse.<String>builder()
                .result("Delete dictionary expense item success")
                .build();
    }
}
