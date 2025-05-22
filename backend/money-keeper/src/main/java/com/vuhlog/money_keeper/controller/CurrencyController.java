package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dao.httpClient.CurrencyClient;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/currency")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyClient currencyClient;

    @GetMapping("rates/lookup")
    public ApiResponse<ExchangeRateResponse> getExchangeRate(@RequestParam(name = "isoTo", defaultValue = "VND") String isoTo,
                                       @RequestParam(name = "isoFrom", defaultValue = "VND") String isoFrom,
                                       @RequestParam(name = "amount", defaultValue = "1") Long amount) {
        return ApiResponse.<ExchangeRateResponse>builder()
                .result(currencyClient.exchangeRate(isoTo, isoFrom, amount))
                .build();
    }
}
