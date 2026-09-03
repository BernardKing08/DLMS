package com.dlms.frontend.controller;

import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.dto.BookResponseDto;
import com.dlms.frontend.exception.ServiceUnavailableException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final CatalogClient catalogClient;

    public HomeController(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        SessionUser.addToModel(session, model);

        List<BookResponseDto> featuredBooks;
        try {
            List<BookResponseDto> allBooks = catalogClient.getAllBooks();
            featuredBooks = allBooks.size() > 6 ? allBooks.subList(0, 6) : allBooks;
        } catch (ServiceUnavailableException ex) {
            // Home page should still render even if Catalog happens to be down.
            logger.warn("Catalog unavailable while loading featured books: {}", ex.getMessage());
            featuredBooks = Collections.emptyList();
        }
        model.addAttribute("featuredBooks", featuredBooks);

        return "index";
    }
}
