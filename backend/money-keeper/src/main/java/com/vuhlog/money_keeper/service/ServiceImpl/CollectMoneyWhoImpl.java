package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.dao.CollectMoneyWhoRepository;
import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
import com.vuhlog.money_keeper.dao.RevenueRegularRepository;
import com.vuhlog.money_keeper.dto.request.CollectMoneyWhoRequest;
import com.vuhlog.money_keeper.dto.response.CollectMoneyWhoResponse;
import com.vuhlog.money_keeper.entity.CollectMoneyWho;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.mapper.CollectMoneyWhoMapper;
import com.vuhlog.money_keeper.service.CollectMoneyWhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectMoneyWhoImpl implements CollectMoneyWhoService {
    private final RevenueRegularRepository revenueRegularRepository;
    private final CollectMoneyWhoRepository collectMoneyWhoRepository;
    private final UserCommon userCommon;
    private final CollectMoneyWhoMapper collectMoneyWhoMapper;

    @Override
    public CollectMoneyWhoResponse createCollectMoneyWho(CollectMoneyWhoRequest request) {
        Users user = userCommon.getMyUserInfo();
        CollectMoneyWho collectMoneyWho = collectMoneyWhoMapper.toCollectMoneyWho(request);
        collectMoneyWho.setUser(user);
        return collectMoneyWhoMapper.toCollectMoneyWhoResponse(collectMoneyWhoRepository.save(collectMoneyWho));
    }

    @Override
    public void deleteCollectMoneyWho(String id) {
        revenueRegularRepository.unsetCollectMoneyWhoInRevenueRegular(id);
        collectMoneyWhoRepository.deleteById(id);
    }

    @Override
    public CollectMoneyWhoResponse updateCollectMoneyWho(String id, CollectMoneyWhoRequest request) {
        CollectMoneyWho collectMoneyWho = collectMoneyWhoRepository.findById(id).orElse(null);
        if (collectMoneyWho != null) {
            collectMoneyWhoMapper.updateCollectMoneyWhoFromRequest(request, collectMoneyWho);
            return collectMoneyWhoMapper.toCollectMoneyWhoResponse(collectMoneyWhoRepository.save(collectMoneyWho));
        }
        return null;
    }

    @Override
    public List<CollectMoneyWhoResponse> getAllMyCollectMoneyWho() {
        Users user = userCommon.getMyUserInfo();
        return collectMoneyWhoRepository.findAllByUser_Id(user.getId()).stream().map(collectMoneyWhoMapper::toCollectMoneyWhoResponse).toList();
    }
}
