package com.dlms.frontend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StaticPageController.class)
class StaticPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contactPageRendersItsHeroBanner() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("page-banner contact-banner")));
    }

    @Test
    void newsAndEventsPagesRenderTheirHeroBanners() throws Exception {
        mockMvc.perform(get("/news-events"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("page-banner news-listing-banner news-events-banner")));

        mockMvc.perform(get("/news-events/example"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("page-banner news-listing-banner news-events-banner")));
    }
}
