package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.UpdateLoginEnabledRequest;
import br.com.nutriplus.dto.request.UpdateUserAdminRequest;
import br.com.nutriplus.dto.response.AdminAccessSummaryResponse;
import br.com.nutriplus.dto.response.AdminUserAccessResponse;
import br.com.nutriplus.service.AdminAccessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/access")
public class AdminAccessController {

    private final AdminAccessService adminAccessService;

    public AdminAccessController(AdminAccessService adminAccessService) {
        this.adminAccessService = adminAccessService;
    }

    @GetMapping("/summary")
    public AdminAccessSummaryResponse summary() {
        return adminAccessService.summary();
    }

    @GetMapping("/pending")
    public List<AdminUserAccessResponse> pending() {
        return adminAccessService.listPending();
    }

    @GetMapping("/approved")
    public List<AdminUserAccessResponse> approved() {
        return adminAccessService.listApproved();
    }

    @GetMapping("/admins")
    public List<AdminUserAccessResponse> admins() {
        return adminAccessService.listAdmins();
    }

    @PatchMapping("/users/{id}/login-enabled")
    public AdminUserAccessResponse setLoginEnabled(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateLoginEnabledRequest request) {
        return adminAccessService.setLoginEnabled(id, request);
    }

    @PatchMapping("/users/{id}/admin")
    public AdminUserAccessResponse setUserAdmin(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserAdminRequest request) {
        return adminAccessService.setUserAdmin(id, request);
    }
}
