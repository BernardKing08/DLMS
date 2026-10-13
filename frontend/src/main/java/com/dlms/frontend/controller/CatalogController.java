package com.dlms.frontend.controller;

import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.dto.BookPageResponseDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CatalogController {

    private final CatalogClient catalogClient;

    public CatalogController(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String category,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "9") int size,
                       @RequestParam(defaultValue = "title,asc") String sort,
                       Model model, HttpSession session) {
        SessionUser.addToModel(session, model);

        size = Math.max(size, 1);
        if (page < 1) {
            page = 1;
        }

        String normalizedSort = normalizeSort(sort);
        BookPageResponseDto result = catalogClient.getBooksPage(page, size, normalizedSort, category);
        if (result == null) {
            result = new BookPageResponseDto(List.of(), List.of(), 0, 0, page, size);
        }

        model.addAttribute("books", result.getBooks() != null ? result.getBooks() : List.of());
        model.addAttribute("categories", result.getCategories() != null ? result.getCategories() : List.of());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("totalBookCount", result.getTotalElements());
        model.addAttribute("currentPage", result.getCurrentPage());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("pageSize", result.getPageSize());
        model.addAttribute("sort", normalizedSort);
        return "books";
    }

    private String normalizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "title,asc";
        }
        String[] sortParams = sort.split(",", -1);
        String field = sortParams.length > 0 && "author".equalsIgnoreCase(sortParams[0].trim())
                ? "author"
                : "title";
        String direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1].trim())
                ? "desc"
                : "asc";
        return field + "," + direction;
    }

    @GetMapping("/books/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        model.addAttribute("book", catalogClient.getById(id));
        return "book-detail";
    }
}
