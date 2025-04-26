//package com.vuhlog.money_keeper.controller;
//
//import com.vuhlog.money_keeper.dto.request.BeneficiaryRequest;
//import com.vuhlog.money_keeper.dto.response.ApiResponse;
//import com.vuhlog.money_keeper.dto.response.BeneficiaryResponse;
//import com.vuhlog.money_keeper.service.BeneficiaryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/beneficiaries")
//@RequiredArgsConstructor
//public class BeneficiaryController {
//    private final BeneficiaryService BeneficiaryService;
//
//    @GetMapping("")
//    public ApiResponse<List<BeneficiaryResponse>> getAllMyBeneficiary(){
//        return ApiResponse.<List<BeneficiaryResponse>>builder()
//                .result(BeneficiaryService.getAllMyBeneficiary())
//                .build();
//    }
//
//    @PostMapping
//    public ApiResponse<BeneficiaryResponse> createBeneficiary(@RequestBody BeneficiaryRequest req){
//        return ApiResponse.<BeneficiaryResponse>builder()
//                .result(BeneficiaryService.createBeneficiary(req))
//                .build();
//    }
//
//    @PutMapping("/{id}")
//    public ApiResponse<BeneficiaryResponse> updateBeneficiary(@PathVariable String id, @RequestBody BeneficiaryRequest req){
//        return ApiResponse.<BeneficiaryResponse>builder()
//                .result(BeneficiaryService.updateBeneficiary(id, req))
//                .build();
//    }
//
//    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
//    public ApiResponse<String> deleteBeneficiary(@PathVariable String id){
//        BeneficiaryService.deleteBeneficiary(id);
//        return ApiResponse.<String>builder()
//                .result("Deleted beneficiary successfully")
//                .build();
//    }
//
//}
