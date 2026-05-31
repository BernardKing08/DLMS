package com.inventory.invtry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inventory.invtry.modal.Inventory;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByBookId(Long bookId);
}
