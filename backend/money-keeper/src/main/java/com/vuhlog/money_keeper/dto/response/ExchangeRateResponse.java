package com.vuhlog.money_keeper.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExchangeRateResponse {
    @JsonProperty("Amount")
    Long Amount;

    @JsonProperty("Rate")
    Double Rate;
}
