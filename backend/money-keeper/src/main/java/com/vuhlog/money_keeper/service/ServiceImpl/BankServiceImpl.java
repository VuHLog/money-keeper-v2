package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.dao.BankRepository;
import com.vuhlog.money_keeper.dto.response.BankResponse;
import com.vuhlog.money_keeper.mapper.BankMapper;
import com.vuhlog.money_keeper.service.BankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    @Override
    public List<BankResponse> getAllBanks() {
        return bankRepository.findAll().stream().map(bankMapper::toBankResponse).toList();
    }
}
