package com.vuhlog.money_keeper.dao.httpClient;

import com.vuhlog.money_keeper.dto.response.ExchangeRateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "currency", url = "https://www.exchange-rates.org")
public interface CurrencyClient {

    @GetMapping(value = "/vn/api/v2/rates/lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    ExchangeRateResponse exchangeRate(
            @RequestParam(name = "isoTo") String isoTo,
            @RequestParam(name = "isoFrom") String isoFrom,
            @RequestParam(name = "amount", defaultValue = "1") Long amount
    );
}