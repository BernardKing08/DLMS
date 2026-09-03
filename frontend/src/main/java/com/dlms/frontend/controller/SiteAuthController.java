package com.dlms.frontend.controller;

import com.dlms.frontend.client.AuthClient;
import com.dlms.frontend.dto.LoginRequestDto;
import com.dlms.frontend.dto.RegisterRequestDto;
import com.dlms.frontend.dto.UserResponseDto;
import com.dlms.frontend.exception.ServiceUnavailableException;
import com.dlms.frontend.exception.ValidationException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SiteAuthController {

    private final AuthClient authClient;

    public SiteAuthController(AuthClient authClient) {
        this.authClient = authClient;
    }

    @GetMapping("/signin")
    public String signInPage(@RequestParam(required = false) Boolean registered,
                              Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        model.addAttribute("loginForm", new LoginRequestDto());
        model.addAttribute("registerForm", new RegisterRequestDto());
        if (Boolean.TRUE.equals(registered)) {
            model.addAttribute("registerSuccess", "Registration successful - you can now log in.");
        }
        return "signin";
    }

    @PostMapping("/signin/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginRequestDto loginForm,
                         BindingResult bindingResult,
                         Model model, HttpSession session) {
        model.addAttribute("registerForm", new RegisterRequestDto());
        SessionUser.addToModel(session, model);

        if (bindingResult.hasErrors()) {
            return "signin";
        }

        try {
            UserResponseDto user = authClient.login(loginForm);
            SessionUser.login(session, user.getId(), user.getName(), user.getEmail(), user.getRole());
            return "redirect:/account";
        } catch (ValidationException | ServiceUnavailableException ex) {
            model.addAttribute("loginError", ex.getMessage());
            return "signin";
        }
    }

    @PostMapping("/signin/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterRequestDto registerForm,
                            BindingResult bindingResult,
                            Model model, HttpSession session) {
        model.addAttribute("loginForm", new LoginRequestDto());
        SessionUser.addToModel(session, model);

        if (bindingResult.hasErrors()) {
            return "signin";
        }
        if (!registerForm.getPassword().equals(registerForm.getConfirmPassword())) {
            model.addAttribute("registerError", "Passwords do not match");
            return "signin";
        }

        try {
            authClient.register(registerForm);
            return "redirect:/signin?registered=true";
        } catch (ValidationException | ServiceUnavailableException ex) {
            model.addAttribute("registerError", ex.getMessage());
            return "signin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        SessionUser.logout(session);
        return "redirect:/";
    }
}
