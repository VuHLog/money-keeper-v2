package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.dao.DictionaryRevenueRepository;
import com.vuhlog.money_keeper.dao.UsersRepository;
import com.vuhlog.money_keeper.dao.specification.DictionaryRevenueSpecification;
import com.vuhlog.money_keeper.dto.request.DictionaryRevenueRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryRevenueResponse;
import com.vuhlog.money_keeper.entity.DictionaryRevenue;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.DictionaryRevenueMapper;
import com.vuhlog.money_keeper.service.DictionaryRevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryRevenueServiceImpl implements DictionaryRevenueService {
    private final DictionaryRevenueRepository dictionaryRevenueRepository;
    private final UsersRepository usersRepository;
    private final DictionaryRevenueMapper dictionaryRevenueMapper;

    @Override
    public DictionaryRevenueResponse createDictionaryRevenue(DictionaryRevenueRequest request) {
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueMapper.toDictionaryRevenue(request);
        dictionaryRevenue.setSystemDefault(false);
        dictionaryRevenue.setRegular(false);
        dictionaryRevenue.setUser(getMyInfo());
        return dictionaryRevenueMapper.toDictionaryRevenueResponse(dictionaryRevenueRepository.save(dictionaryRevenue));
    }

    @Override
    public DictionaryRevenueResponse updateDictionaryRevenue(String id, DictionaryRevenueRequest request) {
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.DICTIONARY_REVENUE_NOT_EXISTED));
        dictionaryRevenueMapper.updateDictionaryRevenueFromRequest(request, dictionaryRevenue);

        dictionaryRevenue.setSystemDefault(false);
        return dictionaryRevenueMapper.toDictionaryRevenueResponse(dictionaryRevenueRepository.save(dictionaryRevenue));
    }

    @Override
    public void deleteDictionaryRevenueItem(String id) {
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.DICTIONARY_REVENUE_NOT_EXISTED));
        if(!dictionaryRevenue.isSystemDefault() && dictionaryRevenue.getUser().getId().equals(getMyInfo().getId())){
            dictionaryRevenueRepository.deleteById(id);
        }
    }

    @Override
    public DictionaryRevenueResponse getDictionaryRevenueById(String id) {
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.DICTIONARY_REVENUE_NOT_EXISTED));
        return dictionaryRevenueMapper.toDictionaryRevenueResponse(dictionaryRevenue);
    }

    @Override
    public List<DictionaryRevenueResponse> getAllDictionaryRevenue() {
        Specification<DictionaryRevenue> specs = Specification.where(null);

        specs = specs.and(DictionaryRevenueSpecification.equalUserId(getMyInfo().getId()));

        Sort sortable = Sort.by("name").ascending();

        return dictionaryRevenueRepository.findAll(specs, sortable).stream().map(dictionaryRevenueMapper::toDictionaryRevenueResponse).toList();
    }

    @Override
    public List<DictionaryRevenueResponse> getAllDictionaryRevenueWithoutTransfer() {
        Specification<DictionaryRevenue> specs = Specification.where(null);

        specs = specs.and(DictionaryRevenueSpecification.notEqualName("Chuyển khoản"));
        specs = specs.and(DictionaryRevenueSpecification.equalUserId(getMyInfo().getId()));

        Sort sortable = Sort.by("name").ascending();

        return dictionaryRevenueRepository.findAll(specs, sortable).stream().map(dictionaryRevenueMapper::toDictionaryRevenueResponse).toList();
    }

    public Users getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return usersRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
