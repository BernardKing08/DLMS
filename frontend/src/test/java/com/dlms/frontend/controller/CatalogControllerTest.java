package com.dlms.frontend.controller;

import com.dlms.frontend.client.CatalogClient;
import com.dlms.frontend.dto.BookPageResponseDto;
import com.dlms.frontend.dto.BookResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogClient catalogClient;

    @Test
    void catalogPageHandlesZeroPageSizeGracefully() throws Exception {
        when(catalogClient.getBooksPage(1, 1, "title,asc", null))
                .thenReturn(emptyPage(1, 1));

        mockMvc.perform(get("/books").param("size", "0"))
                .andExpect(status().isOk());

        verify(catalogClient).getBooksPage(1, 1, "title,asc", null);
    }

    @Test
    void catalogPageIgnoresUnsupportedSortFieldsGracefully() throws Exception {
        when(catalogClient.getBooksPage(1, 9, "title,desc", null))
                .thenReturn(emptyPage(1, 9));

        mockMvc.perform(get("/books").param("sort", "year,desc"))
                .andExpect(status().isOk());

        verify(catalogClient).getBooksPage(1, 9, "title,desc", null);
    }

    @Test
    void catalogPagePassesCategoryAndPaginationToCatalogService() throws Exception {
        when(catalogClient.getBooksPage(2, 9, "author,asc", "Fantasy"))
                .thenReturn(emptyPage(2, 9));

        mockMvc.perform(get("/books")
                        .param("category", "Fantasy")
                        .param("page", "2")
                        .param("sort", "author,asc"))
                .andExpect(status().isOk());

        verify(catalogClient).getBooksPage(2, 9, "author,asc", "Fantasy");
    }

    @Test
    void catalogSidebarShowsBooksFromCatalogResponse() throws Exception {
        BookResponseDto book = new BookResponseDto(42L, "Live Catalog Book", "A. Writer",
                "9780000000001", "Fantasy", "Description", null, true, 2, 1);
        when(catalogClient.getBooksPage(1, 9, "title,asc", null))
                .thenReturn(new BookPageResponseDto(List.of(book), List.of("Fantasy"), 1, 1, 1, 9));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Live Catalog Book")))
                .andExpect(content().string(not(containsString("The Sonic Boom"))));
    }

    private BookPageResponseDto emptyPage(int page, int size) {
        return new BookPageResponseDto(List.of(), List.of(), 0, 0, page, size);
    }
}
