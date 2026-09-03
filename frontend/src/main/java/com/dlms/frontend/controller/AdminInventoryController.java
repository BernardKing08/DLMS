package com.dlms.frontend.controller;

import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.client.InventoryClient;
import com.dlms.frontend.dto.BookResponseDto;
import com.dlms.frontend.exception.ServiceUnavailableException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Admin-only (see AdminInterceptor, gates everything under /admin/**).
 * Reuses Catalog's own response, which is already enriched with live
 * Inventory stock - no separate join needed for the listing. Updates go
 * through InventoryClient, the same endpoint the domain services use.
 */
@Controller
public class AdminInventoryController {

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    public AdminInventoryController(CatalogClient catalogClient, InventoryClient inventoryClient) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
    }

    @GetMapping("/admin/inventory")
    public String list(@RequestParam(required = false) Boolean updated,
                        Model model, HttpSession session) {
        SessionUser.addToModel(session, model);

        List<AdminInventoryRow> rows = catalogClient.getAllBooks().stream()
                .map(this::toRow)
                .toList();
        model.addAttribute("rows", rows);

        if (Boolean.TRUE.equals(updated)) {
            model.addAttribute("updateSuccess", "Inventory updated.");
        }
        return "admin-inventory";
    }

    @PostMapping("/admin/inventory/{bookId}")
    public String update(@PathVariable Long bookId,
                          @RequestParam int totalCopies,
                          Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        try {
            inventoryClient.update(bookId, totalCopies);
            return "redirect:/admin/inventory?updated=true";
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("updateError", ex.getMessage());
            model.addAttribute("rows", catalogClient.getAllBooks().stream().map(this::toRow).toList());
            return "admin-inventory";
        }
    }

    private AdminInventoryRow toRow(BookResponseDto book) {
        return new AdminInventoryRow(
                book.getId(), book.getTitle(), book.getAuthor(), book.getCategory(),
                book.getTotalCopies(), book.getAvailableCopies()
        );
    }
}
