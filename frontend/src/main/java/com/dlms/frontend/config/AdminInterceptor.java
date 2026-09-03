package com.dlms.frontend.config;

import com.dlms.frontend.controller.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Gates every /admin/** route. This is the reusable part of "how do I add
 * another admin page": drop a new @Controller method under /admin/** and it
 * is automatically protected - no per-controller auth check needed. Not a
 * replacement for real authorization (Spring Security method/URL security)
 * if this app grows beyond a local dev/demo system.
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession();
        if (SessionUser.isAdmin(session)) {
            return true;
        }
        try {
            response.sendRedirect(request.getContextPath() + "/signin");
        } catch (Exception ignored) {
            // Nothing more we can do if the redirect itself fails.
        }
        return false;
    }
}
