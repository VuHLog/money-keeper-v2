package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.response.BankResponse;

import java.util.List;

public interface BankService {
    List<BankResponse> getAllBanks();
}
