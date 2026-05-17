package com.linktalk.controller;

import com.linktalk.dto.AuthLoginRequest;
import com.linktalk.dto.AuthRegisterRequest;
import com.linktalk.dto.UserProfileResponse;
import com.linktalk.dto.UserProfileUpdateRequest;
import com.linktalk.model.User;
import com.linktalk.service.AuthService;
import com.linktalk.model.AuthUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse register(@Valid @RequestBody AuthRegisterRequest request) {
        User user = authService.register(request.toCommand());
        return UserProfileResponse.from(user);
    }

    @PostMapping("/login")
    public void login(@Valid @RequestBody AuthLoginRequest request, HttpServletRequest httpRequest) throws Exception {
        httpRequest.login(request.email(), request.password());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest httpRequest) throws Exception {
        httpRequest.logout();
    }

    @GetMapping("/me")
    public UserProfileResponse me(@AuthenticationPrincipal AuthUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        User user = authService.getById(principal.getId());
        return UserProfileResponse.from(user);
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(@AuthenticationPrincipal AuthUserDetails principal,
                                        @Valid @RequestBody UserProfileUpdateRequest request) {
        if (principal == null) {
            throw new IllegalStateException("Not authenticated");
        }
        User user = authService.updateProfile(principal.getId(), request);
        return UserProfileResponse.from(user);
    }

}
