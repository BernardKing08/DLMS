package com.dlms.frontend.controller;

import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.dto.BookResponseDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
public class CatalogController {

    private final CatalogClient catalogClient;

    public CatalogController(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String category,
                        Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        List<BookResponseDto> allBooks = catalogClient.getAllBooks();

        List<BookResponseDto> visibleBooks = (category == null || category.isBlank())
                ? allBooks
                : allBooks.stream().filter(book -> category.equals(book.getCategory())).toList();

        model.addAttribute("books", visibleBooks);
        model.addAttribute("categories", distinctCategories(allBooks));
        model.addAttribute("selectedCategory", category);
        model.addAttribute("totalBookCount", allBooks.size());
        return "books";
    }

    private List<String> distinctCategories(List<BookResponseDto> books) {
        return books.stream()
                .map(BookResponseDto::getCategory)
                .filter(category -> category != null && !category.isBlank())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        model.addAttribute("book", catalogClient.getById(id));
        return "book-detail";
    }
}
