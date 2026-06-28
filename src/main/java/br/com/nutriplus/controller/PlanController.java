package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.PlanCatalogResponse;
import br.com.nutriplus.service.PlanCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/plans")
public class PlanController {

    private final PlanCatalogService planCatalogService;

    public PlanController(PlanCatalogService planCatalogService) {
        this.planCatalogService = planCatalogService;
    }

    @GetMapping
    public ResponseEntity<PlanCatalogResponse> catalogo() {
        return ResponseEntity.ok(planCatalogService.catalogo());
    }
}
