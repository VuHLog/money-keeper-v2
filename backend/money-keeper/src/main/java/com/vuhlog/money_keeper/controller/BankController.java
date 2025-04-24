package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.BankResponse;
import com.vuhlog.money_keeper.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/banks")
@RequiredArgsConstructor
public class BankController {
    private final BankService bankService;

    @GetMapping("")
    public ApiResponse<List<BankResponse>> getBanks(){
        return ApiResponse.<List<BankResponse>>builder()
                .result(bankService.getAllBanks())
                .build();
    }

}
