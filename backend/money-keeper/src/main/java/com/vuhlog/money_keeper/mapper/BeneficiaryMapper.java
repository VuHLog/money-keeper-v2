package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.BeneficiaryRequest;
import com.vuhlog.money_keeper.dto.response.BeneficiaryResponse;
import com.vuhlog.money_keeper.entity.Beneficiary;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BeneficiaryMapper {
    Beneficiary toBeneficiary(BeneficiaryRequest BeneficiaryRequest);
    
    BeneficiaryResponse toBeneficiaryResponse(Beneficiary beneficiary);
    
    void updateBeneficiaryFromRequest(BeneficiaryRequest beneficiaryRequest, @MappingTarget Beneficiary beneficiary);
}
