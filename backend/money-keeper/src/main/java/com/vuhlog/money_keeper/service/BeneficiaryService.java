package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.BeneficiaryRequest;
import com.vuhlog.money_keeper.dto.response.BeneficiaryResponse;

import java.util.List;

public interface BeneficiaryService {
    BeneficiaryResponse createBeneficiary(BeneficiaryRequest request);
    void deleteBeneficiary(String id);
    BeneficiaryResponse updateBeneficiary(String id, BeneficiaryRequest request);
    List<BeneficiaryResponse> getAllMyBeneficiary();
}
