package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.dao.BeneficiaryRepository;
import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
import com.vuhlog.money_keeper.dto.request.BeneficiaryRequest;
import com.vuhlog.money_keeper.dto.response.BeneficiaryResponse;
import com.vuhlog.money_keeper.entity.Beneficiary;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.mapper.BeneficiaryMapper;
import com.vuhlog.money_keeper.service.BeneficiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BeneficiaryImpl implements BeneficiaryService {
    private final ExpenseRegularRepository expenseRegularRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserCommon userCommon;
    private final BeneficiaryMapper beneficiaryMapper;

    @Override
    public BeneficiaryResponse createBeneficiary(BeneficiaryRequest request) {
        Users user = userCommon.getMyUserInfo();
        Beneficiary beneficiary = beneficiaryMapper.toBeneficiary(request);
        beneficiary.setUser(user);
        return beneficiaryMapper.toBeneficiaryResponse(beneficiaryRepository.save(beneficiary));
    }

    @Override
    public void deleteBeneficiary(String id) {
        expenseRegularRepository.unsetBeneficiaryInExpenseRegular(id);
        beneficiaryRepository.deleteById(id);
    }

    @Override
    public BeneficiaryResponse updateBeneficiary(String id, BeneficiaryRequest request) {
        Beneficiary beneficiary = beneficiaryRepository.findById(id).orElse(null);
        if (beneficiary != null) {
            beneficiaryMapper.updateBeneficiaryFromRequest(request, beneficiary);
            return beneficiaryMapper.toBeneficiaryResponse(beneficiaryRepository.save(beneficiary));
        }
        return null;
    }

    @Override
    public List<BeneficiaryResponse> getAllMyBeneficiary() {
        Users user = userCommon.getMyUserInfo();
        return beneficiaryRepository.findAllByUser_Id(user.getId()).stream().map(beneficiaryMapper::toBeneficiaryResponse).toList();
    }
}
