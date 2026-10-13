package com.inventory.invtry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventory.invtry.model.Inventory;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByBookId(Long bookId);

    List<Inventory> findByBookIdIn(Collection<Long> bookIds);
}
