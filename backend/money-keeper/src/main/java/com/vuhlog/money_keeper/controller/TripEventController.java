//package com.vuhlog.money_keeper.controller;
//
//import com.vuhlog.money_keeper.dto.request.TripEventRequest;
//import com.vuhlog.money_keeper.dto.response.ApiResponse;
//import com.vuhlog.money_keeper.dto.response.TripEventResponse;
//import com.vuhlog.money_keeper.service.TripEventService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/trip-events")
//@RequiredArgsConstructor
//public class TripEventController {
//    private final TripEventService tripEventService;
//
//    @GetMapping("")
//    public ApiResponse<List<TripEventResponse>> getAllMyTripEvent(){
//        return ApiResponse.<List<TripEventResponse>>builder()
//                .result(tripEventService.getAllMyTripEvent())
//                .build();
//    }
//
//    @PostMapping
//    public ApiResponse<TripEventResponse> createTripEvent(@RequestBody TripEventRequest req){
//        return ApiResponse.<TripEventResponse>builder()
//                .result(tripEventService.createTripEvent(req))
//                .build();
//    }
//
//    @PutMapping("/{id}")
//    public ApiResponse<TripEventResponse> updateTripEvent(@PathVariable String id, @RequestBody TripEventRequest req){
//        return ApiResponse.<TripEventResponse>builder()
//                .result(tripEventService.updateTripEvent(id, req))
//                .build();
//    }
//
//    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
//    public ApiResponse<String> deleteTripEvent(@PathVariable String id){
//        tripEventService.deleteTripEvent(id);
//        return ApiResponse.<String>builder()
//                .result("Deleted trip event successfully")
//                .build();
//    }
//
//}
