package com.dlms.auth.service;

import com.dlms.auth.dto.LoginRequest;
import com.dlms.auth.dto.RegisterRequest;
import com.dlms.auth.model.User;

public interface IAuthService {

    User register(RegisterRequest request);

    User authenticate(LoginRequest request);

    User getUserById(Long id);
}
