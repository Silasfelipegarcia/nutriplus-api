package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.UpdateSubscriptionPlanRequest;
import br.com.nutriplus.dto.response.AdminSubscriptionPlanResponse;
import br.com.nutriplus.service.SubscriptionPlanCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/subscription-plans")
public class AdminSubscriptionPlanController {

    private final SubscriptionPlanCatalogService catalogService;

    public AdminSubscriptionPlanController(SubscriptionPlanCatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<AdminSubscriptionPlanResponse> list() {
        return catalogService.listForAdmin();
    }

    @PatchMapping("/{id}")
    public AdminSubscriptionPlanResponse update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        return catalogService.update(id, request);
    }
}
