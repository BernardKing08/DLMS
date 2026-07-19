package com.dlms.auth.controller;

import com.dlms.auth.constants.AuthConstants;
import com.dlms.auth.dto.LoginRequest;
import com.dlms.auth.dto.RegisterRequest;
import com.dlms.auth.dto.ResponseDto;
import com.dlms.auth.dto.UserResponseDto;
import com.dlms.auth.model.User;
import com.dlms.auth.service.Impl.AuthServiceImpl;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user
     */
    @PostMapping("/register")
    public ResponseEntity<ResponseDto> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(
                        AuthConstants.STATUS_201,
                        AuthConstants.MESSAGE_201
                ));
    }

    /**
     * Authenticates a user (credentials validation only for now)
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseDto> login(
            @Valid @RequestBody LoginRequest request
    ) {
        authService.authenticate(request);

        return ResponseEntity.ok(
                new ResponseDto(
                        AuthConstants.STATUS_200,
                        AuthConstants.MESSAGE_200
                )
        );
    }

    /**
     * Used by other services (e.g. Account) to validate that a userId
     * corresponds to a real, registered user.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = authService.getUserById(id);
        return ResponseEntity.ok(
                new UserResponseDto(user.getId(), user.getName(), user.getEmail())
        );
    }
}
