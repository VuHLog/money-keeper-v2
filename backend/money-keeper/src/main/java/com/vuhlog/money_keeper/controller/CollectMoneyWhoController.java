//package com.vuhlog.money_keeper.controller;
//
//import com.vuhlog.money_keeper.dto.request.CollectMoneyWhoRequest;
//import com.vuhlog.money_keeper.dto.response.ApiResponse;
//import com.vuhlog.money_keeper.dto.response.CollectMoneyWhoResponse;
//import com.vuhlog.money_keeper.service.CollectMoneyWhoService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/collect-money-who")
//@RequiredArgsConstructor
//public class CollectMoneyWhoController {
//    private final CollectMoneyWhoService collectMoneyWhoService;
//
//    @GetMapping("")
//    public ApiResponse<List<CollectMoneyWhoResponse>> getAllMyCollectMoneyWho(){
//        return ApiResponse.<List<CollectMoneyWhoResponse>>builder()
//                .result(collectMoneyWhoService.getAllMyCollectMoneyWho())
//                .build();
//    }
//
//    @PostMapping
//    public ApiResponse<CollectMoneyWhoResponse> createCollectMoneyWho(@RequestBody CollectMoneyWhoRequest req){
//        return ApiResponse.<CollectMoneyWhoResponse>builder()
//                .result(collectMoneyWhoService.createCollectMoneyWho(req))
//                .build();
//    }
//
//    @PutMapping("/{id}")
//    public ApiResponse<CollectMoneyWhoResponse> updateCollectMoneyWho(@PathVariable String id, @RequestBody CollectMoneyWhoRequest req){
//        return ApiResponse.<CollectMoneyWhoResponse>builder()
//                .result(collectMoneyWhoService.updateCollectMoneyWho(id, req))
//                .build();
//    }
//
//    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
//    public ApiResponse<String> deleteCollectMoneyWho(@PathVariable String id){
//        collectMoneyWhoService.deleteCollectMoneyWho(id);
//        return ApiResponse.<String>builder()
//                .result("Deleted CollectMoneyWho successfully")
//                .build();
//    }
//
//}
