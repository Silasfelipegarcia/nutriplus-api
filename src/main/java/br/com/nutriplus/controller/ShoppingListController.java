package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.ApplyShoppingSwapsRequest;
import br.com.nutriplus.dto.response.ApplyShoppingSwapsResponse;
import br.com.nutriplus.dto.response.ShoppingListResponse;
import br.com.nutriplus.service.ShoppingListService;
import br.com.nutriplus.service.ShoppingSwapService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shopping-list")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final ShoppingSwapService shoppingSwapService;

    public ShoppingListController(ShoppingListService shoppingListService,
                                  ShoppingSwapService shoppingSwapService) {
        this.shoppingListService = shoppingListService;
        this.shoppingSwapService = shoppingSwapService;
    }

    @GetMapping("/latest")
    public ShoppingListResponse getLatest() {
        return shoppingListService.getLatest();
    }

    @PostMapping("/apply-swaps")
    public ApplyShoppingSwapsResponse applySwaps(@Valid @RequestBody ApplyShoppingSwapsRequest request) {
        return shoppingSwapService.applySwaps(request);
    }
}
