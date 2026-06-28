package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.AdminPerformanceSummaryResponse;
import br.com.nutriplus.service.AdminPerformanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/performance")
public class AdminPerformanceController {

    private final AdminPerformanceService adminPerformanceService;

    public AdminPerformanceController(AdminPerformanceService adminPerformanceService) {
        this.adminPerformanceService = adminPerformanceService;
    }

    @GetMapping("/summary")
    public AdminPerformanceSummaryResponse summary() {
        return adminPerformanceService.summary();
    }
}
