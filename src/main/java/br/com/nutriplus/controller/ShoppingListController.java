package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.ShoppingListResponse;
import br.com.nutriplus.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping("/latest")
    public ShoppingListResponse getLatest() {
        return shoppingListService.getLatest();
    }
}
