package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.CollectMoneyWhoRequest;
import com.vuhlog.money_keeper.dto.response.CollectMoneyWhoResponse;

import java.util.List;

public interface CollectMoneyWhoService {
    CollectMoneyWhoResponse createCollectMoneyWho(CollectMoneyWhoRequest request);
    void deleteCollectMoneyWho(String id);
    CollectMoneyWhoResponse updateCollectMoneyWho(String id, CollectMoneyWhoRequest request);
    List<CollectMoneyWhoResponse> getAllMyCollectMoneyWho();
}
