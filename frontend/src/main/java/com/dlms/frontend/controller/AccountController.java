package com.dlms.frontend.controller;

import com.dlms.frontend.client.AccountClient;
import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.client.InventoryClient;
import com.dlms.frontend.dto.AccountResponseDto;
import com.dlms.frontend.dto.AccountUpdateRequestDto;
import com.dlms.frontend.dto.BookResponseDto;
import com.dlms.frontend.dto.BorrowRecordDto;
import com.dlms.frontend.exception.ResourceNotFoundException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AccountController {

    private final AccountClient accountClient;
    private final InventoryClient inventoryClient;
    private final CatalogClient catalogClient;

    public AccountController(AccountClient accountClient,
                             InventoryClient inventoryClient,
                             CatalogClient catalogClient) {
        this.accountClient = accountClient;
        this.inventoryClient = inventoryClient;
        this.catalogClient = catalogClient;
    }

    @GetMapping("/account")
    public String view(@RequestParam(required = false) Boolean updated,
                        Model model, HttpSession session) {
        if (!SessionUser.isLoggedIn(session)) {
            return "redirect:/signin";
        }
        SessionUser.addToModel(session, model);
        Long userId = SessionUser.getUserId(session);

        try {
            AccountResponseDto account = accountClient.getByUserId(userId);
            model.addAttribute("account", account);
            model.addAttribute("accountForm", toUpdateForm(account));
            if (Boolean.TRUE.equals(updated)) {
                model.addAttribute("updateSuccess", "Profile updated.");
            }
        } catch (ResourceNotFoundException ex) {
            // Registration provisions the Account profile asynchronously - a
            // user who logs in within moments of registering can legitimately
            // hit this window.
            model.addAttribute("profilePending", true);
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", "Account service is unavailable right now - try again shortly.");
        }

        try {
            List<BorrowRecordDto> loans = inventoryClient.getLoansByUserId(userId);
            for (BorrowRecordDto loan : loans) {
                try {
                    BookResponseDto book = catalogClient.getById(loan.getBookId());
                    loan.setBookTitle(book.getTitle());
                    loan.setBookAuthor(book.getAuthor());
                } catch (Exception ignored) {
                    loan.setBookTitle("Book #" + loan.getBookId());
                    loan.setBookAuthor("Unknown");
                }
            }
            model.addAttribute("loans", loans);
        } catch (Exception ex) {
            model.addAttribute("loans", List.of());
        }

        return "account";
    }

    @PostMapping("/account/return/{loanId}")
    public String returnLoan(@PathVariable Long loanId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!SessionUser.isLoggedIn(session)) {
            return "redirect:/signin";
        }
        try {
            inventoryClient.returnBook(loanId);
            redirectAttributes.addFlashAttribute("returnSuccess", "Book returned successfully.");
        } catch (ValidationException ex) {
            redirectAttributes.addFlashAttribute("returnError", ex.getMessage());
        } catch (ServiceUnavailableException ex) {
            redirectAttributes.addFlashAttribute("returnError", "Inventory service is unavailable right now - try again shortly.");
        }
        return "redirect:/account";
    }

    @PostMapping("/account")
    public String update(@Valid @ModelAttribute("accountForm") AccountUpdateRequestDto accountForm,
                          BindingResult bindingResult,
                          Model model, HttpSession session) {
        if (!SessionUser.isLoggedIn(session)) {
            return "redirect:/signin";
        }
        SessionUser.addToModel(session, model);
        Long userId = SessionUser.getUserId(session);

        if (bindingResult.hasErrors()) {
            model.addAttribute("account", accountClient.getByUserId(userId));
            return "account";
        }

        try {
            AccountResponseDto updated = accountClient.update(userId, accountForm);
            model.addAttribute("account", updated);
            return "redirect:/account?updated=true";
        } catch (ValidationException ex) {
            model.addAttribute("account", accountClient.getByUserId(userId));
            model.addAttribute("updateError", ex.getMessage());
            return "account";
        } catch (ServiceUnavailableException ex) {
            model.addAttribute("serviceError", "Account service is unavailable right now - try again shortly.");
            return "account";
        }
    }

    private AccountUpdateRequestDto toUpdateForm(AccountResponseDto account) {
        AccountUpdateRequestDto form = new AccountUpdateRequestDto();
        form.setFirstName(account.getFirstName());
        form.setLastName(account.getLastName());
        form.setPhoneNumber(account.getPhoneNumber());
        form.setAddress(account.getAddress());
        return form;
    }
}
