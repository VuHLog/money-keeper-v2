package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.response.BankResponse;
import com.vuhlog.money_keeper.entity.Bank;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BankMapper {
    BankResponse toBankResponse(Bank bank);
}
