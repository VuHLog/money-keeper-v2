package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankResponse {
    private String id;
    private String name;
    private String code;
    private String bin;
    private String shortName;
    private String logo;
    private boolean transferSupported;
    private boolean lookupSupported;
    private boolean support;
    private boolean isTransfer;
    private String swiftCode;
}