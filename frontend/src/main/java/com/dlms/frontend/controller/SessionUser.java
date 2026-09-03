package com.dlms.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

/**
 * This backend has no JWT/session-token system of its own - login just
 * validates credentials and hands back who the user is (including their
 * role). That's enough for a server-rendered app: we keep "who's currently
 * logged in" in the servlet HttpSession, scoped to this frontend module
 * only. Deliberately simple - not a replacement for real authentication if
 * this app were public-facing.
 */
public final class SessionUser {

    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String USER_EMAIL = "userEmail";
    private static final String USER_ROLE = "userRole";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private SessionUser() {}

    public static void login(HttpSession session, Long userId, String name, String email, String role) {
        session.setAttribute(USER_ID, userId);
        session.setAttribute(USER_NAME, name);
        session.setAttribute(USER_EMAIL, email);
        session.setAttribute(USER_ROLE, role);
    }

    public static void logout(HttpSession session) {
        session.invalidate();
    }

    public static Long getUserId(HttpSession session) {
        return (Long) session.getAttribute(USER_ID);
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getUserId(session) != null;
    }

    public static boolean isAdmin(HttpSession session) {
        return ROLE_ADMIN.equals(session.getAttribute(USER_ROLE));
    }

    /** Adds "loggedIn"/"userName"/"isAdmin" to the model so any page's nav bar can greet the user. */
    public static void addToModel(HttpSession session, Model model) {
        model.addAttribute("loggedIn", isLoggedIn(session));
        model.addAttribute("userName", session.getAttribute(USER_NAME));
        model.addAttribute("isAdmin", isAdmin(session));
    }
}
