package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shopping_list_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(nullable = false, length = 100)
    private String quantity;

    @Column(length = 100)
    private String category;
}
