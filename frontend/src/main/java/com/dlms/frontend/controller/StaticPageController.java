package com.dlms.frontend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Pages with no backing service (no blog/news/contact microservice exists
 * in this system) - purely static content, kept only because they were
 * chosen to stay during the template cleanup. Still routed through a real
 * controller (not addViewControllers) so the nav bar's logged-in state
 * stays consistent with every other page.
 */
@Controller
public class StaticPageController {

    @GetMapping("/contact")
    public String contact(Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        return "contact";
    }

    @GetMapping("/news-events")
    public String newsEventsList(Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        return "news-events-list-view";
    }

    @GetMapping("/news-events/{slug}")
    public String newsEventsDetail(Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        return "news-events-detail";
    }
}
