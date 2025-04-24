package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.dao.DictionaryExpenseRepository;
import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
import com.vuhlog.money_keeper.dao.UsersRepository;
import com.vuhlog.money_keeper.dao.specification.DictionaryExpenseSpecification;
import com.vuhlog.money_keeper.dto.request.DictionaryExpenseRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryExpenseResponse;
import com.vuhlog.money_keeper.entity.DictionaryExpense;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.DictionaryExpenseMapper;
import com.vuhlog.money_keeper.service.DictionaryExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryExpenseServiceImpl implements DictionaryExpenseService {
    private final DictionaryExpenseRepository dictionaryExpenseRepository;
    private final ExpenseRegularRepository expenseRegularRepository;
    private final UsersRepository usersRepository;
    private final DictionaryExpenseMapper dictionaryExpenseMapper;

    @Override
    public DictionaryExpenseResponse createDictionaryExpense(DictionaryExpenseRequest request) {
        DictionaryExpense dictionaryExpense = dictionaryExpenseMapper.toDictionaryExpense(request);
        dictionaryExpense.setSystemDefault(false);
        dictionaryExpense.setRegular(false);
        dictionaryExpense.setUser(getMyInfo());
        return dictionaryExpenseMapper.toDictionaryExpenseResponse(dictionaryExpenseRepository.save(dictionaryExpense));
    }

    @Override
    public DictionaryExpenseResponse updateDictionaryExpense(String id, DictionaryExpenseRequest request) {
        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.DICTIONARY_EXPENSE_NOT_EXISTED));
        dictionaryExpenseMapper.updateDictionaryExpenseFromRequest(request, dictionaryExpense);

        dictionaryExpense.setSystemDefault(false);
        return dictionaryExpenseMapper.toDictionaryExpenseResponse(dictionaryExpenseRepository.save(dictionaryExpense));
    }

    @Override
    public void deleteDictionaryExpenseItem(String id) {
        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.DICTIONARY_EXPENSE_NOT_EXISTED));
        if(!dictionaryExpense.isSystemDefault() && dictionaryExpense.getUser().getId().equals(getMyInfo().getId())){
            expenseRegularRepository.unsetDictionaryExpenseInExpenseRegular(id);
            dictionaryExpenseRepository.deleteById(id);
        }
    }

    @Override
    public DictionaryExpenseResponse getDictionaryExpenseById(String id) {
        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findById(id).orElseThrow( () -> new AppException(ErrorCode.DICTIONARY_EXPENSE_NOT_EXISTED));
        return dictionaryExpenseMapper.toDictionaryExpenseResponse(dictionaryExpense);
    }

    @Override
    public List<DictionaryExpenseResponse> getAllDictionaryExpense() {
        Specification<DictionaryExpense> specs = Specification.where(null);

        specs = specs.and(DictionaryExpenseSpecification.equalSystemDefault(true));
        specs = specs.or(DictionaryExpenseSpecification.equalUserId(getMyInfo().getId()).and(DictionaryExpenseSpecification.equalSystemDefault(false)));

        Sort sortable = Sort.by("name").ascending();

        return dictionaryExpenseRepository.findAll(specs, sortable).stream().map(dictionaryExpenseMapper::toDictionaryExpenseResponse).toList();
    }

    @Override
    public List<DictionaryExpenseResponse> getAllDictionaryExpenseWithoutTransfer(String search) {
        Specification<DictionaryExpense> specs = Specification.where(null);

        specs = specs.and(DictionaryExpenseSpecification.equalSystemDefault(true));
        specs = specs.and(DictionaryExpenseSpecification.notEqualName("Chuyển khoản"));
        specs = specs.or(DictionaryExpenseSpecification.equalUserId(getMyInfo().getId()).and(DictionaryExpenseSpecification.equalSystemDefault(false)));

        if(search != null && !search.isEmpty()){
            specs = specs.and(DictionaryExpenseSpecification.likeName(search));
        }

        Sort sortable = Sort.by("name").ascending();

        return dictionaryExpenseRepository.findAll(specs, sortable).stream().map(dictionaryExpenseMapper::toDictionaryExpenseResponse).toList();
    }

    public Users getMyInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return usersRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
