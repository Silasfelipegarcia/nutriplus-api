package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.AdminFinanceOverviewResponse;
import br.com.nutriplus.service.AdminFinanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/finance")
public class AdminFinanceController {

    private final AdminFinanceService adminFinanceService;

    public AdminFinanceController(AdminFinanceService adminFinanceService) {
        this.adminFinanceService = adminFinanceService;
    }

    @GetMapping("/overview")
    public AdminFinanceOverviewResponse overview(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        var now = java.time.YearMonth.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return adminFinanceService.overview(y, m);
    }
}
