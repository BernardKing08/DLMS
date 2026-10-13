package com.catalog.ctlog.repository;

import com.catalog.ctlog.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByCategory(String category, Pageable pageable);

    @Query("select distinct b.category from Book b "
            + "where b.category is not null and trim(b.category) <> '' order by b.category")
    List<String> findDistinctCategories();
}