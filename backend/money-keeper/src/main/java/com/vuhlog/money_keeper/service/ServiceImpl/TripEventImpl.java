//package com.vuhlog.money_keeper.service.ServiceImpl;
//
//import com.vuhlog.money_keeper.common.UserCommon;
//import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
//import com.vuhlog.money_keeper.dao.TripEventRepository;
//import com.vuhlog.money_keeper.dto.request.TripEventRequest;
//import com.vuhlog.money_keeper.dto.response.TripEventResponse;
//import com.vuhlog.money_keeper.entity.TripEvent;
//import com.vuhlog.money_keeper.entity.Users;
//import com.vuhlog.money_keeper.mapper.TripEventMapper;
//import com.vuhlog.money_keeper.service.TripEventService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class TripEventImpl implements TripEventService {
//    private final TripEventRepository tripEventRepository;
//    private final ExpenseRegularRepository expenseRegularRepository;
//    private final UserCommon userCommon;
//    private final TripEventMapper tripEventMapper;
//
//    @Override
//    public TripEventResponse createTripEvent(TripEventRequest request) {
//        Users user = userCommon.getMyUserInfo();
//        TripEvent tripEvent = tripEventMapper.toTripEvent(request);
//        tripEvent.setUser(user);
//        return tripEventMapper.toTripEventResponse(tripEventRepository.save(tripEvent));
//    }
//
//    @Override
//    public void deleteTripEvent(String id) {
//        expenseRegularRepository.unsetTripEventInExpenseRegular(id);
//        tripEventRepository.deleteById(id);
//    }
//
//    @Override
//    public TripEventResponse updateTripEvent(String id, TripEventRequest request) {
//        TripEvent tripEvent = tripEventRepository.findById(id).orElse(null);
//        if (tripEvent != null) {
//            tripEventMapper.updateTripEventFromRequest(request, tripEvent);
//            return tripEventMapper.toTripEventResponse(tripEventRepository.save(tripEvent));
//        }
//        return null;
//    }
//
//    @Override
//    public List<TripEventResponse> getAllMyTripEvent() {
//        Users user = userCommon.getMyUserInfo();
//        return tripEventRepository.findAllByUser_Id(user.getId()).stream().map(tripEventMapper::toTripEventResponse).toList();
//    }
//}
