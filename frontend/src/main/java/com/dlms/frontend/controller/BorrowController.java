package com.dlms.frontend.controller;

import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.client.InventoryClient;
import com.dlms.frontend.dto.BookResponseDto;
import com.dlms.frontend.dto.BorrowInquiryForm;
import com.dlms.frontend.exception.ServiceUnavailableException;
import com.dlms.frontend.exception.ValidationException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Controller
public class BorrowController {

    private static final String CART_SESSION_KEY = BorrowController.class.getName() + ".bookIds";

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    public BorrowController(CatalogClient catalogClient, InventoryClient inventoryClient) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        SessionUser.addToModel(session, model);
        model.addAttribute("books", getCartBooks(session));
        return "cart";
    }

    @PostMapping("/cart/add/{bookId}")
    public String addBook(@PathVariable Long bookId, HttpSession session,
                          RedirectAttributes redirectAttributes) {
        BookResponseDto book = catalogClient.getById(bookId);
        if (!book.isActive() || book.getAvailableCopies() < 1) {
            redirectAttributes.addFlashAttribute("cartError",
                    "This title is not currently available to request.");
            return "redirect:/books/" + bookId;
        }

        cartIds(session).add(bookId);
        redirectAttributes.addFlashAttribute("cartMessage",
                "Added to your borrowing cart. Availability is checked again before you place your request.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{bookId}")
    public String removeBook(@PathVariable Long bookId, HttpSession session,
                             RedirectAttributes redirectAttributes) {
        cartIds(session).remove(bookId);
        redirectAttributes.addFlashAttribute("cartMessage", "Title removed from your borrowing cart.");
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        if (!SessionUser.isLoggedIn(session)) {
            return "redirect:/signin";
        }

        List<BookResponseDto> books = getCartBooks(session);
        if (books.isEmpty()) {
            return "redirect:/cart";
        }

        SessionUser.addToModel(session, model);
        model.addAttribute("books", books);

        BorrowInquiryForm form = new BorrowInquiryForm();
        Object userName = session.getAttribute("userName");
        Object userEmail = session.getAttribute("userEmail");
        if (userName instanceof String name) {
            form.setName(name);
        }
        if (userEmail instanceof String email) {
            form.setEmail(email);
        }

        model.addAttribute("requestForm", form);
        model.addAttribute("hasUnavailableBooks", hasUnavailableBooks(books));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String prepareInquiry(@Valid @ModelAttribute("requestForm") BorrowInquiryForm requestForm,
                                 BindingResult bindingResult, Model model, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!SessionUser.isLoggedIn(session)) {
            return "redirect:/signin";
        }

        List<BookResponseDto> books = getCartBooks(session);
        if (books.isEmpty()) {
            return "redirect:/cart";
        }

        SessionUser.addToModel(session, model);
        model.addAttribute("books", books);
        model.addAttribute("hasUnavailableBooks", hasUnavailableBooks(books));

        if (bindingResult.hasErrors() || hasUnavailableBooks(books)) {
            return "checkout";
        }

        Long userId = SessionUser.getUserId(session);
        List<Long> bookIds = new ArrayList<>(cartIds(session));

        try {
            inventoryClient.borrowBooks(userId, bookIds, requestForm.getPickupLocation(), requestForm.getNotes());
            cartIds(session).clear();
            redirectAttributes.addFlashAttribute("borrowSuccess", "Your borrowing request has been placed successfully!");
            return "redirect:/account";
        } catch (ValidationException ex) {
            model.addAttribute("borrowError", ex.getMessage());
            return "checkout";
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("borrowError", "The inventory service is temporarily unavailable. Please try again shortly.");
            return "checkout";
        }
    }

    private List<BookResponseDto> getCartBooks(HttpSession session) {
        return cartIds(session).stream()
                .map(catalogClient::getById)
                .toList();
    }

    private LinkedHashSet<Long> cartIds(HttpSession session) {
        Object storedIds = session.getAttribute(CART_SESSION_KEY);
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (storedIds instanceof Iterable<?> values) {
            for (Object value : values) {
                if (value instanceof Long bookId) {
                    ids.add(bookId);
                }
            }
        }
        session.setAttribute(CART_SESSION_KEY, ids);
        return ids;
    }

    private boolean hasUnavailableBooks(List<BookResponseDto> books) {
        return books.stream().anyMatch(book -> !book.isActive() || book.getAvailableCopies() < 1);
    }
}

