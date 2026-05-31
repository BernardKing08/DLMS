package com.dlms.auth.service.Impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.dlms.auth.constants.AuthConstants;
import com.dlms.auth.dto.LoginRequest;
import com.dlms.auth.dto.RegisterRequest;
import com.dlms.auth.model.Role;
import com.dlms.auth.model.User;
import com.dlms.auth.repository.RoleRepository;
import com.dlms.auth.repository.UserRepository;
import com.dlms.auth.service.IAuthService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements IAuthService{
    
    private final UserRepository userRepository; 
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, 
                            RoleRepository roleRepository, 
                            PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterRequest request){
        //check if exists 
        //assign role
        //build user 

        if(userRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException(AuthConstants.EMAIL_ALREADY_EXISTS);
        }

        Role userRole = roleRepository.findByRoleName(AuthConstants.ROLE_USER)
                .orElseThrow(() ->
                        new IllegalStateException(AuthConstants.ROLE_NOT_FOUND)
                );

        User user = User.builder()
                        .email(request.getEmail())
                        .pwd(passwordEncoder.encode(request.getPassword()))
                        .role(userRole)
                        .build();

        return userRepository.save(user);
    }

    @Override
    public User authenticate(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new IllegalArgumentException(AuthConstants.INVALID_CREDENTIALS)
            );

        if (!passwordEncoder.matches(request.getPassword(), user.getPwd())) {
            throw new IllegalArgumentException(AuthConstants.INVALID_CREDENTIALS);
        }

        return user;

    }

}
