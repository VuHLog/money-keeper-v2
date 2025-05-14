package com.vuhlog.money_keeper.controller;

import com.nimbusds.jose.JOSEException;
import com.vuhlog.money_keeper.dto.request.AuthenticationRequest;
import com.vuhlog.money_keeper.dto.request.IntrospectRequest;
import com.vuhlog.money_keeper.dto.request.LogoutRequest;
import com.vuhlog.money_keeper.dto.request.RefreshRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.AuthenticationOAuthResponse;
import com.vuhlog.money_keeper.dto.response.AuthenticationResponse;
import com.vuhlog.money_keeper.dto.response.IntrospectResponse;
import com.vuhlog.money_keeper.service.AuthenticationService;
import com.vuhlog.money_keeper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request){
        var res = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(res)
                .build();
    }

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationOAuthResponse> outboundAuthenticate(
            @RequestParam("code") String code
    ) throws IOException {
        AuthenticationOAuthResponse result = authenticationService.outboundAuthenticate(code);
        if(result.getNewUserId() != null && !result.getNewUserId().isEmpty()){
            userService.executeSqlScriptForUser(result.getNewUserId());
        }
        return ApiResponse.<AuthenticationOAuthResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var res = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .result(res)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var res = authenticationService.refreshToken(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(res)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> authenticate(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);

        return ApiResponse.<Void>builder()
                .build();
    }
}
