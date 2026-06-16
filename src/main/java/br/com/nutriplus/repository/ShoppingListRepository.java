package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

    @Query("SELECT DISTINCT sl FROM ShoppingList sl LEFT JOIN FETCH sl.items WHERE sl.user.id = :userId ORDER BY sl.createdAt DESC")
    java.util.List<ShoppingList> findByUserIdWithItemsOrderByCreatedAtDesc(Long userId);
}
