package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.AdminEmailTestResponse;
import br.com.nutriplus.service.AdminEmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/email")
public class AdminEmailController {

    private final AdminEmailService adminEmailService;

    public AdminEmailController(AdminEmailService adminEmailService) {
        this.adminEmailService = adminEmailService;
    }

    @PostMapping("/test")
    public AdminEmailTestResponse sendTest() {
        return adminEmailService.sendTestEmail();
    }
}
