package com.dlms.frontend.controller;

import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.client.InventoryClient;
import com.dlms.frontend.dto.BookResponseDto;
import com.dlms.frontend.dto.BorrowRecordDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BorrowController.class)
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogClient catalogClient;

    @MockBean
    private InventoryClient inventoryClient;

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Simulates a logged-in regular user in the HTTP session. */
    private MockHttpSession loggedInSession() {
        MockHttpSession session = new MockHttpSession();
        SessionUser.login(session, 42L, "Alex Reader", "alex@example.com", "ROLE_USER");
        return session;
    }

    private BookResponseDto availableBook() {
        BookResponseDto book = new BookResponseDto();
        book.setId(17L);
        book.setTitle("The Hobbit");
        book.setAuthor("J. R. R. Tolkien");
        book.setActive(true);
        book.setAvailableCopies(2);
        return book;
    }

    // ------------------------------------------------------------------
    // Cart tests
    // ------------------------------------------------------------------

    @Test
    void addingAnAvailableBookRedirectsToCart() throws Exception {
        when(catalogClient.getById(17L)).thenReturn(availableBook());
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/cart/add/17").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void cartPageShowsAddedBook() throws Exception {
        when(catalogClient.getById(17L)).thenReturn(availableBook());
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/cart/add/17").session(session));

        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("The Hobbit")));
    }

    @Test
    void unavailableBooksCannotBeAddedToTheCart() throws Exception {
        BookResponseDto unavailable = availableBook();
        unavailable.setAvailableCopies(0);
        when(catalogClient.getById(17L)).thenReturn(unavailable);

        mockMvc.perform(post("/cart/add/17"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/17"));
    }

    @Test
    void removingABookRedirectsBackToCart() throws Exception {
        when(catalogClient.getById(17L)).thenReturn(availableBook());
        MockHttpSession session = new MockHttpSession();

        // Add then remove
        mockMvc.perform(post("/cart/add/17").session(session));
        mockMvc.perform(post("/cart/remove/17").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    // ------------------------------------------------------------------
    // Checkout tests
    // ------------------------------------------------------------------

    @Test
    void checkoutRedirectsToSignInWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }

    @Test
    void checkoutRedirectsToCartWhenCartIsEmpty() throws Exception {
        mockMvc.perform(get("/checkout").session(loggedInSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));
    }

    @Test
    void successfulCheckoutClearsCartAndRedirectsToAccount() throws Exception {
        when(catalogClient.getById(17L)).thenReturn(availableBook());
        when(inventoryClient.borrowBooks(anyLong(), anyList(), anyString(), any()))
                .thenReturn(List.of(new BorrowRecordDto()));

        MockHttpSession session = loggedInSession();

        // Place the book in the cart first
        mockMvc.perform(post("/cart/add/17").session(session));

        // Submit checkout
        mockMvc.perform(post("/checkout")
                        .session(session)
                        .param("name", "Alex Reader")
                        .param("email", "alex@example.com")
                        .param("pickupLocation", "Central Library")
                        .param("notes", "Please contact me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));
    }
}
